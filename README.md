# Mech Shop

Sistema de gestão de ordens de serviço para oficina mecânica.

## 🛠️ Tecnologias
- **Linguagem:** Kotlin (Java 21)
- **Framework:** Spring Boot 4
- **Banco de Dados:** H2 (Dev) / PostgreSQL (Prod)
- **Containers:** Docker / Docker Compose

---

## 🚀 Como Executar

O projeto está configurado com perfis automáticos para facilitar a sua rotina:

### Opção 1: Desenvolvimento Rápido (Via IntelliJ / H2)
*Ideal para codificar e testar rápido. Usa banco de dados H2 em memória (zera ao reiniciar).*

*   **Pela IDE:** Abra o projeto no IntelliJ e clique no botão **Run** na classe `MechShopApplication.kt`.
*   **Pelo Terminal:**
    ```bash
    ./gradlew bootRun

### Opção 2: Ambiente Completo (Via Docker / Postgres)
*Ideal para testar a versão final isolada. Persiste os dados na sua máquina física.*

- Subir tudo (API + Banco): `docker compose up -d`
- Acompanhar logs: `docker compose logs -f`
- Parar o ambiente (Mantendo os dados): `docker compose down`
- Parar e APAGAR o banco (Reset total): `docker compose down -v`

## 📖 Documentação da API (Swagger)

Com a aplicação rodando (seja por IntelliJ ou Docker), acesse o link abaixo para testar os endpoints:

🔗 http://localhost:8080/swagger-ui/index.html

> [!NOTE]
> **Autenticação:** Para acessar as rotas de administrador (`/api/admin/`), gere um token no endpoint `/api/auth/login`, clique no botão **Authorize** (ícone de cadeado no topo da página) e cole o token lá.
