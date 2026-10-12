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

# Cria uma conta dedicada, sem shell de login e com UID/GID estáveis.
# Usar identificadores numéricos também evita depender da resolução de nomes no runtime.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 10001 app \
    && useradd --system --uid 10001 --gid app --home-dir /app --no-create-home \
        --shell /usr/sbin/nologin app

# Copia somente o .jar gerado na etapa "build" e o renomeia para app.jar.
COPY --from=build --chown=10001:10001 /app/target/*.jar app.jar

# Documenta que a aplicação aceita conexões na porta 8080.
EXPOSE 8080

USER 10001:10001

# Verifica se a API responde; permite 60 segundos para a inicialização da aplicação.
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl --fail --silent --show-error --max-time 3 http://127.0.0.1:8080/hello || exit 1

# Define o comando executado automaticamente quando um container desta imagem é iniciado.
ENTRYPOINT ["java", "-jar", "app.jar"]
