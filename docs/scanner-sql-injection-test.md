# Teste temporario do Total Scanner

Esta branch introduz intencionalmente SQL Injection (CWE-89) em
`src/main/java/endpoint/controller/ScannerTestController.java`.
O parametro HTTP `descricao` e concatenado na consulta executada por
`Statement.executeQuery`, sem parametrizacao.

O controller so e registrado quando o perfil Spring `scanner-test` esta ativo.
O scanner estatico pode analisar o arquivo sem iniciar a aplicacao ou ativar o perfil.
Nao habilite esse perfil em ambientes compartilhados ou de producao.

## Validacao

1. Analise o PR no Total Scanner.
2. Procure um alerta de SQL Injection/CWE-89 no controller, na chamada
   `statement.executeQuery(sql)` ou na concatenacao da consulta.
3. Confira arquivo, linha e associacao do alerta ao PR.
4. Depois do teste, feche o PR sem merge e remova a branch temporaria.

Para uma demonstracao local com banco descartavel, ative o perfil
`scanner-test` e compare o endpoint `/scanner-test/pedidos` usando uma descricao
normal com a descricao `' OR '1'='1` (codificada como parametro de URL).
A segunda entrada transforma o filtro em uma condicao verdadeira.

A correcao real seria usar `PreparedStatement` e vincular `descricao` como parametro.
