# Collection EstudoJava com PostgreSQL

Importe `EstudoJava.postman_collection.json` no Postman. Se ela já estiver
importada, substitua a versão anterior. Use `baseUrl=http://localhost:8080`
e mantenha PostgreSQL, RabbitMQ e a aplicação em execução.

## Execução completa

Execute a collection no **Collection Runner**, na ordem das pastas, sem
execuções simultâneas. Habilite a opção de persistir os valores das variáveis
ao final da execução, principalmente para os testes de reinício.

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

As pastas **06** e **07** são opcionais e ignoradas quando a variável
`modoPersistencia` está no padrão `desativado`.

1. Defina `modoPersistencia=preparar` nas variáveis da collection e execute
   **somente a pasta 06**. Ela cria um cálculo e um pedido, aguarda o consumidor
   e guarda os IDs e os snapshots de todas as informações.
2. Persista os valores das variáveis. No Runner, habilite a opção de mantê-los
   ao final; usando Send, execute as três requisições na ordem. Não reimporte
   a collection nem apague suas variáveis entre preparação e conferência.
3. Reinicie a aplicação ou recrie os containers sem remover volumes:

```powershell
docker compose -f docker/docker-compose.yml up -d --force-recreate --wait
```

4. Aguarde a aplicação responder `/hello`. Defina `modoPersistencia=verificar`
   e execute **somente a pasta 07**. Ela faz apenas GET e compara os registros
   completos com os snapshots, incluindo o status PROCESSED do pedido.
5. Retorne `modoPersistencia` para `desativado`.

Os registros desse exercício são mantidos intencionalmente. Seus IDs e
snapshots usam variáveis `persistencia*`, separadas das variáveis do CRUD.
Preparar novamente cria novos registros e substitui os snapshots anteriores.

## Newman

Com Node e Newman disponíveis, na raiz do projeto:

```powershell
newman run postman/EstudoJava.postman_collection.json --iteration-count 2 --timeout-script 90000
```

Para conferir reinício em execuções separadas, exporte o ambiente depois da
preparação. Os scripts espelham as variáveis `persistencia*` no ambiente para
que IDs e snapshots possam ser reaproveitados em outro processo Newman:

```powershell
newman run postman/EstudoJava.postman_collection.json --folder "06 - Persistência - Preparar antes do reinício" --env-var modoPersistencia=preparar --export-environment target/postman-persistencia.environment.json --timeout-script 90000
# Recrie os containers, preserve volumes e espere /hello responder.
newman run postman/EstudoJava.postman_collection.json --folder "07 - Persistência - Conferir após o reinício" --environment target/postman-persistencia.environment.json --env-var modoPersistencia=verificar --timeout-script 90000
```

O timeout de scripts permite a espera do cenário 429. Falhas de conexão,
status HTTP inesperados e pedidos que não foram processados devem continuar
aparecendo como falhas, sem serem ignorados pela collection.
