# Inicia a primeira etapa da imagem, usada apenas para compilar a aplicação.
FROM maven:3.9-eclipse-temurin-25 AS build

# Define /app como a pasta de trabalho para os próximos comandos desta etapa.
WORKDIR /app

# Copia o arquivo de configuração do Maven para dentro da imagem.
COPY pom.xml .
# Copia o código-fonte Java para a pasta /app/src da imagem.
COPY src ./src

# Executa o Maven para limpar, compilar e gerar o arquivo .jar; os testes são ignorados neste build.
RUN mvn clean package -DskipTests

# Inicia a segunda etapa, menor, que conterá somente o necessário para executar a aplicação.
FROM eclipse-temurin:25-jre

# Define /app como a pasta de trabalho da aplicação em execução.
WORKDIR /app

# Copia somente o .jar gerado na etapa "build" e o renomeia para app.jar.
COPY --from=build /app/target/*.jar app.jar

# Documenta que a aplicação aceita conexões na porta 8080.
EXPOSE 8080

# Define o comando executado automaticamente quando um container desta imagem é iniciado.
ENTRYPOINT ["java", "-jar", "app.jar"]
