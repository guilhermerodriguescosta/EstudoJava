# EstudoJava: PostgreSQL, JPA e RabbitMQ

Aplicacao de estudo com Java 25 e Spring Boot. O historico de calculos e os
pedidos ficam no PostgreSQL; usuarios de exemplo e limite de requisicoes
continuam em memoria. A API e os formatos JSON permanecem iguais.

## Executar localmente

Inicie o Docker Desktop com containers Linux. Execute os comandos abaixo na
raiz do projeto (`C:\Estudo\Java\EstudoJava`).

Para executar Java pela IDE ou Maven, suba apenas os servicos:

```powershell
docker compose -f docker/docker-compose.yml up -d --wait postgres rabbitmq
.\mvnw.cmd spring-boot:run
```

Para executar tudo em containers:

```powershell
docker compose -f docker/docker-compose.yml up -d --build --wait
docker compose -f docker/docker-compose.yml logs -f endpoint
```

Use apenas uma instancia da aplicacao na porta 8080. Banco: `estudojava`,
usuario: `app`, senha local: `app`. PostgreSQL esta publicado somente em
`127.0.0.1:5432`. O JDBC usa `localhost` quando Java roda no Windows e
`postgres` quando roda no Compose. URL, usuario e senha podem ser substituidos
por `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e
`SPRING_DATASOURCE_PASSWORD`. As credenciais de exemplo sao para estudo local.

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
.\mvnw.cmd test
```

Os testes unitarios usam mocks. Os testes de persistencia e HTTP usam
PostgreSQL 18 descartavel via Testcontainers e executam Flyway; nao acessam
o banco local `estudojava`. Docker deve estar disponivel para a suite completa.
Os testes de persistencia isolam as tabelas entre cenarios e cobrem CRUD,
duplicidade concorrente, rollback e falha de envio.

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
