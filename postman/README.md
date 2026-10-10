# Collection EstudoJava com PostgreSQL

Importe `EstudoJava.postman_collection.json` no Postman. Se ela já estiver
importada, substitua a versão anterior. Use `baseUrl=http://localhost:8080`
e mantenha PostgreSQL, RabbitMQ e a aplicação em execução.

## Execução completa

Execute a collection no **Collection Runner**, na ordem das pastas, sem
execuções simultâneas. A collection contém 37 requisições para os testes
funcionais do dia a dia.

- Cálculos usam números aleatórios abaixo do limite da API. O teste de 409
  reutiliza os mesmos números e operação; a atualização duplicada verifica
  também que o registro original não foi alterado.
- IDs são capturados das respostas; nenhum teste supõe que o ID começará em 1.
- Cálculos criados pelo CRUD e pelo cenário de limite são excluídos com o
  usuário admin. Somente os IDs capturados nessas criações são removidos.
- Pedidos têm descrição com UUID e são consultados até chegar a PROCESSED,
  por até 10 segundos. Falha de processamento faz o teste falhar.
- Pedidos ficam no banco: a API não oferece exclusão de pedidos. Uma execução
  interrompida também pode deixar cálculos de teste; não limpe o banco inteiro.

A pasta **05** espera até 61 segundos antes da primeira requisição para que
as três criações permitidas sejam feitas em uma janela livre. Ela registra o
horário da última tentativa para calcular a espera na próxima execução.
Não configure atraso entre as quatro criações desta pasta, pois o cenário
precisa enviar a quarta dentro da mesma janela de um minuto. Evite usar o
usuário `limitado` por outro cliente durante a execução.

As consultas de IDs inexistentes usam o ID excluído pelo próprio CRUD,
`-1` (não gerado pela API) ou um novo UUID. Um banco já populado não exige reset.

## Conferir persistência após reiniciar containers

Esta verificação pode ser feita manualmente, sem pastas específicas na collection.

1. Envie `POST /historico` com Basic Auth e números escolhidos por você.
   Exemplo: `/historico?numero1=41&numero2=2&operacao=SOMA`. Se retornar 409,
   escolha outro par de números. Guarde o ID e o JSON completo da resposta.
2. Envie `POST /pedidos?descricao=EstudoPersistencia`. Guarde o UUID e consulte
   `GET /pedidos/{id}` até retornar `PROCESSED`. Guarde esse JSON também.
3. Reinicie a aplicação ou recrie os containers sem remover volumes:

```powershell
docker compose -f docker/docker-compose.yml up -d --force-recreate --wait
```

4. Aguarde `GET /hello` retornar 200. Consulte `GET /historico/{id}` e
   `GET /pedidos/{id}` usando os IDs que você guardou. Compare todos os campos
   com as respostas anteriores; o pedido deve continuar `PROCESSED`.

Faça esse exercício usando requisições manuais separadas das do Runner,
para preservar os IDs e evitar a limpeza automática do cenário CRUD.
O cálculo pode ser excluído depois com `DELETE /historico/{id}` usando admin;
o pedido permanece no banco, pois não existe endpoint de exclusão de pedidos.

## Newman

Com Node e Newman disponíveis, na raiz do projeto:

```powershell
newman run postman/EstudoJava.postman_collection.json --iteration-count 2 --timeout-script 90000
```

O timeout de scripts permite a espera do cenário 429. Falhas de conexão,
status HTTP inesperados e pedidos que não foram processados devem continuar
aparecendo como falhas, sem serem ignorados pela collection.
