# EstudoJava: PostgreSQL, JPA e RabbitMQ

Aplicacao de estudo com Java 25 e Spring Boot. O historico de calculos e os
pedidos ficam no PostgreSQL; usuarios de exemplo e limite de requisicoes
continuam em memoria. A API e os formatos JSON permanecem iguais.

## Executar localmente no Windows e no macOS

O mesmo `pom.xml`, Dockerfile, Compose e configuracoes Java atendem aos dois
sistemas. Use Docker Desktop com **containers Linux** e Docker Compose.
Para executar tudo em containers, nao precisa instalar Java nem Maven no host.
Para usar Maven, testes ou a IDE, instale JDK 25, configure `JAVA_HOME` e confira
`java -version`. O Maven Wrapper baixa o Maven; use `mvnw.cmd` no Windows e
`mvnw` no macOS. Configure a IDE com o mesmo JDK 25 do terminal.

No Windows, siga a [instalacao oficial do Docker Desktop](https://docs.docker.com/desktop/setup/install/windows-install/)
e habilite o backend WSL 2 com containers Linux. Execute os comandos abaixo
no PowerShell 5.1 ou superior, na raiz de seu checkout. No macOS, abra o Docker Desktop com
`open -a Docker`. A [instalacao oficial para Mac](https://docs.docker.com/desktop/setup/install/mac-install/)
tambem permite copiar o aplicativo para Applications; o script Bash encontra
o executavel nesse local se ele ainda nao estiver no PATH.

### Windows / PowerShell

```powershell
.\scripts\local.ps1 up       # PostgreSQL, RabbitMQ e API em containers
.\scripts\local.ps1 test     # Suite completa via Maven Wrapper
.\scripts\local.ps1 status   # Estado dos containers
.\scripts\local.ps1 logs     # Logs da API (Ctrl+C encerra acompanhamento)
.\scripts\local.ps1 db       # Terminal SQL do PostgreSQL
.\scripts\local.ps1 down     # Parar containers preservando os dados
```

Se o PowerShell bloquear scripts locais, use uma politica limitada ao processo
para executar o script do repositorio que voce revisou, sem alterar a politica
permanente do Windows:

```powershell
powershell.exe -NoProfile -ExecutionPolicy RemoteSigned -File .\scripts\local.ps1 up
```

### macOS / Bash

```bash
./scripts/local.sh up       # PostgreSQL, RabbitMQ e API em containers
./scripts/local.sh test     # Suite completa via Maven Wrapper
./scripts/local.sh status   # Estado dos containers
./scripts/local.sh logs     # Logs da API (Ctrl+C encerra acompanhamento)
./scripts/local.sh db       # Terminal SQL do PostgreSQL
./scripts/local.sh down     # Parar containers preservando os dados
```

Se o checkout nao preservar a permissao de execucao, use
`bash scripts/local.sh up` e `bash mvnw test`. O `.gitattributes` preserva
finais de linha LF no Bash, Dockerfile e Compose, e CRLF em `mvnw.cmd`,
independentemente do sistema usado para clonar.

### Executar Java pelo terminal ou pela IDE

Use apenas uma instancia da API na porta 8080. Os scripts `run` sobem
PostgreSQL e RabbitMQ e executam Maven em primeiro plano; Ctrl+C encerra Java.
Se a API estiver em container, execute `stop` antes de `run`:

```powershell
# Windows
.\scripts\local.ps1 stop
.\scripts\local.ps1 run
```

```bash
# macOS
./scripts/local.sh stop
./scripts/local.sh run
```

Para depurar pela IDE, execute `deps` no script de seu sistema e inicie
`endpoint.EndpointApplication` com JDK 25. Se precisar, execute `stop` antes.
O Maven carrega o agente Mockito para os testes; use o runner Maven da IDE
para aproveitar essa configuracao.

### Conexoes e verificacao HTTP

API: http://localhost:8080/hello. RabbitMQ Management:
http://localhost:15672 (`app` / `app`). PostgreSQL: `localhost:5432`, banco
`estudojava`, usuario e senha `app`. As portas ficam restritas a este
computador. Se alguma ja estiver ocupada, pare a outra instancia.

O JDBC usa `localhost` quando Java roda no host (Windows ou macOS) e
`postgres` quando roda no Compose. URL, usuario e senha podem ser substituidos
por `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e
`SPRING_DATASOURCE_PASSWORD`. As credenciais de exemplo sao para estudo local.

```powershell
# Windows
Invoke-RestMethod 'http://localhost:8080/hello'
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/pedidos?descricao=EstudoPostgreSQL'
```

```bash
# macOS
curl --fail http://localhost:8080/hello
curl --fail -u usuario:123 -X POST \
  'http://localhost:8080/historico?numero1=41&numero2=2&operacao=SOMA'
curl --fail -X POST 'http://localhost:8080/pedidos?descricao=EstudoPostgreSQL'
```

Consulte os IDs retornados em `/historico/{id}` e `/pedidos/{id}`.
Repetir os mesmos numeros e operacao retorna 409; escolha outro par.

## Estudar a persistencia

1. Leia `src/main/resources/db/migration/V1__criar_calculos_e_pedidos.sql`:
   ele cria tabelas, chave primaria, identidade, restricoes de enums e
   unicidade dos numeros e operacao.
2. Compare os records de resposta em `endpoint.model` com as entidades em
   `endpoint.persistence`. Os records definem o JSON; as entidades mapeiam
   tabelas. Os enums sao gravados pelo nome e os numeros continuam `double`.
3. Acompanhe controller → service → repository → JPA → PostgreSQL.
   O ID de calculo vem do banco. Uma transacao agrupa cada gravacao; atualizar
   um ID inexistente nao cria registro. Duplicidade retorna HTTP 409.
4. Salve um calculo, consulte-o, atualize-o e exclua-o. Observe as linhas no
   banco depois de cada chamada. Os usuarios de estudo sao `usuario`,
   `admin` e `limitado`, todos com senha `123`; atualizar e excluir exige admin.
5. Envie um pedido e acompanhe `PENDING` mudar para `PROCESSED` pelo consumidor.

```powershell
$calculo = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/historico?numero1=41&numero2=2&operacao=SOMA' -Headers @{ Authorization = 'Basic dXN1YXJpbzoxMjM=' }
Invoke-RestMethod "http://localhost:8080/historico/$($calculo.id)"
$pedido = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/pedidos?descricao=EstudoPostgreSQL'
Invoke-RestMethod "http://localhost:8080/pedidos/$($pedido.id)"
```

Abra o terminal SQL:

```powershell
docker compose -f docker/docker-compose.yml exec postgres psql -U app -d estudojava
```

Dentro do `psql`:

```sql
\dt
\d calculos
SELECT * FROM calculos ORDER BY id;
SELECT * FROM pedidos ORDER BY id;
SELECT version, description, success FROM flyway_schema_history;
\q
```

Flyway executa a migracao automaticamente na inicializacao. Hibernate apenas
valida o esquema (`ddl-auto=validate`); nao cria nem recria tabelas. Mudancas
futuras de esquema devem ganhar uma nova migracao V2, V3 etc.; nao altere V1
depois de aplicada. `open-in-view` esta desativado.

## Pedidos e falhas

O pedido PENDING e confirmado em uma transacao propria antes de publicar no
RabbitMQ. Se o banco falhar, nao ha publicacao. Se a publicacao lancar erro,
a requisicao falha e o registro permanece PENDING. Uma interrupcao entre
gravar e publicar tambem pode deixar um pedido sem mensagem. Nao ha outbox
nem reenvio automatico, e o envio simples nao oferece garantia de entrega.

O consumidor confirma PROCESSED no banco antes de retornar ao listener.
Mensagens repetidas preservam esse status; IDs desconhecidos nao criam
registros. Dados que existiam somente em memoria nao sao importados.

## Testes e prova de reinicio

```powershell
# Windows
.\scripts\local.ps1 test
```

```bash
# macOS
./scripts/local.sh test
```

Os testes unitarios usam mocks. Os testes de persistencia e HTTP usam
PostgreSQL 18 descartavel via Testcontainers e executam Flyway; nao acessam
o banco local `estudojava`. Docker deve estar disponivel para a suite completa.
Os testes de persistencia isolam as tabelas entre cenarios e cobrem CRUD,
duplicidade concorrente, rollback e falha de envio.

Os exemplos PowerShell abaixo podem ser usados no Windows; no macOS, use
`curl` nos mesmos endpoints e guarde os IDs retornados.
Para demonstrar a persistencia, mantenha os IDs de `$calculo` e `$pedido`,
espere o pedido ficar PROCESSED e execute:

```powershell
docker compose -f docker/docker-compose.yml restart endpoint
docker compose -f docker/docker-compose.yml up -d --force-recreate --wait
Invoke-RestMethod "http://localhost:8080/historico/$($calculo.id)"
Invoke-RestMethod "http://localhost:8080/pedidos/$($pedido.id)"
```

Confirme que os valores do calculo e o status do pedido continuam iguais.
Se Java estiver na IDE, reinicie-o e recrie apenas PostgreSQL e RabbitMQ.

`docker compose -f docker/docker-compose.yml down` preserva volumes.
**Adicionar `-v` apaga os volumes e os dados do PostgreSQL e RabbitMQ.**
Nao remova os volumes durante a prova de persistencia.

## Postman

Importe `postman/EstudoJava.postman_collection.json`. O roteiro de execucao,
a limpeza dos calculos de teste e a verificacao de persistencia depois de
reiniciar containers estao em [postman/README.md](postman/README.md).
