# Vehicle Sales API

Serviço de vendas isolado com projeção própria dos veículos. O fluxo legado de oficina permanece temporariamente no projeto, separado da aplicação de vendas.

## Tecnologias

- Kotlin 2.2.21 e Java 21
- Spring Boot 4
- H2 em memória no perfil `dev`; PostgreSQL no perfil `prod`
- Docker e Docker Compose

## Execução local

Execute `./gradlew bootRun` para iniciar com H2. Swagger fica em <http://localhost:8080/swagger-ui/index.html>.

Para executar API e PostgreSQL, use `docker compose up -d`.
