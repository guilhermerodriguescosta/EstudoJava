# Resultado dos testes no Postman — 09/10/2026

Testes executados pela interface do Postman Desktop 12.29.1, no Collection
Runner, contra `http://localhost:8080`. Os 12 endpoints do controller foram
exercitados pelos 42 cenários da collection, incluindo a conferência de
persistência depois de reiniciar a aplicação. As consultas finais passaram
na repetição, após aguardar a aplicação ficar disponível.

## Execuções observadas

| Execução | Resultado apresentado no Postman | Duração |
|---|---|---|
| Collection principal, modo de persistência desativado | 37 cenários ativos; 57 verificações; `Errors: 0`; conclusão com sucesso. Os cinco cenários opcionais foram ignorados. | 1 min 33 s |
| Cópia temporária com preparação e conferência de persistência | 62 verificações antes da interrupção; um erro de conexão na primeira consulta após reinício. | 2 min 48 s |
| Repetição das duas consultas finais e `/hello`, após prontidão | 3 requisições; 6 verificações com `PASS`; `Errors: 0`. | 1 s 160 ms |

Os 42 cenários foram cobertos ao combinar a execução principal, a preparação
da prova e a repetição das consultas finais. A execução ampla com reinício
não passou integralmente em uma única rodada: o erro e a repetição estão
registrados acima. As esperas do limitador e da prova de reinício fazem parte
da duração total e não representam o tempo de resposta dos endpoints.

## Resultado por endpoint

| Método e rota | Cenários e resultados confirmados |
|---|---|
| `GET /hello` | `200`, corpo `Hello World`, inclusive após o reinício. |
| `GET /somar` | `10 + 5 = 15`, `200`; parâmetro ausente ou texto em campo numérico retorna `400`. |
| `GET /subtrair` | `10 - 5 = 5`, `200`. |
| `GET /multiplicar` | `10 × 5 = 50`, `200`. |
| `GET /dividir` | `10 ÷ 5 = 2`, `200`; divisão por zero retorna `400`. |
| `GET /calcular` | `8 × 4 = 32` com `operacao=MULTIPLICACAO`, `200`. |
| `POST /historico` | Criação `201`, ID gerado e `Location` correspondente; repetição do cálculo `409`; número acima de 1.000.000 retorna `422`; sem autenticação, `401`. |
| `GET /historico/{id}` | Registro criado e atualizado retorna `200` e os campos esperados; registro excluído retorna `404`; registro da prova permanece disponível após reinício. |
| `PUT /historico/{id}` | Admin atualiza com `200`, preservando ID; usuário comum recebe `403`; sem autenticação, `401`; ID inexistente, `404`; atualização duplicada, `409`, com dados anteriores preservados na consulta seguinte. |
| `DELETE /historico/{id}` | Admin exclui com `204` e corpo vazio; nova exclusão, `404`; usuário comum, `403`; sem autenticação, `401`. |
| `POST /pedidos` | `202`, UUID, descrição, fila `pedidos` e status inicial `PENDING` corretos. |
| `GET /pedidos/{id}` | Pedido chega a `PROCESSED` pelo consumidor RabbitMQ; UUID não cadastrado retorna `404`; pedido da prova permanece processado após reinício. |

No cenário do usuário `limitado`, três criações consecutivas retornaram `201`.
A quarta retornou `429`, com `Retry-After: 60`. Os cálculos criados pelo CRUD
e pelo cenário de limite foram removidos usando apenas os IDs capturados
na própria execução.

## Persistência após reinício

Antes do reinício, o Postman salvou um cálculo e um pedido, e confirmou o
processamento do pedido. O container `docker-endpoint-1` foi reiniciado por
`docker compose -f docker/docker-compose.yml restart endpoint`. PostgreSQL,
RabbitMQ e seus volumes foram preservados.

Depois de a aplicação ficar disponível, as consultas feitas no Postman
confirmaram:

- Cálculo `24`: `numero1=570340`, `numero2=657109`, operação `DIVISAO`, resultado
  `570340 / 657109`. A comparação de todos os campos passou.
- Pedido `32ee0c40-9c69-4783-b582-40efec04f062`: descrição
  `Postman-eafb342d-599d-4077-8f80-62297462ffe6` e status `PROCESSED`. A comparação
  de ID, descrição e status passou.

Esses registros foram mantidos intencionalmente para estudo. Pedidos dos
outros cenários também permanecem, pois a API não oferece exclusão de pedidos.

## Problemas encontrados e resolução

1. A janela de importação do Postman deixou de expor seus controles; a captura
   retornou `FrameArrived timed out`. A conexão foi reinicializada e, como o
   problema persistiu, o Postman foi reiniciado. As coleções e o resultado
   anterior continuaram disponíveis, e as importações seguintes funcionaram.
2. A cópia temporária usava uma pausa fixa de 90 segundos antes da consulta
   após reinício. Essa pausa começou antes de o container terminar de parar.
   O log da aplicação registrou aproximadamente 88 segundos de processo até
   concluir a inicialização. A consulta ocorreu cedo demais e ficou sem
   resposta. A execução foi corrigida aguardando prontidão real e repetindo
   os testes finais com os mesmos IDs, sem criar novos registros. Os seis
   testes da repetição passaram.

Não foi necessária alteração no código da aplicação. A collection principal
foi preservada. Duas coleções temporárias de validação foram importadas no
Postman: `EstudoJava - Teste completo com reinício` e
`EstudoJava - Conferência após reinício`. O Postman foi deixado aberto no
resultado bem-sucedido da conferência.

Para repetir o exercício, siga o procedimento em `postman/README.md`: preparar,
reiniciar, aguardar `/hello` responder e só então conferir. A pausa fixa usada
na cópia temporária não substitui a verificação de prontidão.

## Evidências e alcance

Os arquivos abaixo estão em `target/` e podem ser removidos por `mvn clean`:

- `postman-desktop-observations.json`: observações da execução principal.
- `postman-desktop-main-summary.txt`: resumo exibido pelo Runner principal.
- `postman-desktop-full-observations.json`: observações da rodada com reinício.
- `postman-desktop-full-summary.txt`: resumo da interrupção por conexão.
- `postman-desktop-recovery-observation.json`: resultado final, incluindo os
  seis `PASS` e as URLs com os mesmos IDs.
- `postman-desktop-full.postman_collection.json` e
  `postman-desktop-recovery.postman_collection.json`: cópias de validação.

Os endpoints foram testados no Postman. O Docker CLI foi usado para consultar
o estado dos serviços, reiniciar a aplicação e conferir prontidão. Esta rodada
não simulou indisponibilidade do RabbitMQ durante publicação, mensagens
repetidas nem inserções concorrentes; esses são cenários de integração
distintos dos testes funcionais da collection. Também não houve recriação dos
containers do banco ou remoção de volumes nesta rodada.
