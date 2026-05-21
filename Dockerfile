# Estágio 1: Build da aplicação
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copia os arquivos de configuração do Gradle primeiro para cachear as dependências
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Garante permissão de execução e baixa as dependências
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# Copia o código fonte e gera o JAR (pulando os testes para o build ser ultra rápido)
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# Estágio 2: Ambiente de execução (Runtime)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia o JAR gerado no estágio anterior
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar

# Porta padrão do Spring Boot
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
