# Vehicle Sales API

API do serviço isolado de vendas de veículos. Mantém uma projeção própria dos veículos, controla disponibilidade e compras e recebe webhooks de pagamento. Os dados cadastrais chegam por HTTP do `vehicle-catalog-api`; este serviço não acessa o banco do projeto principal.

O fluxo legado de oficina permanece separado do domínio de vendas. A API e o PostgreSQL deste serviço usam recursos próprios no Docker Compose ou Kubernetes: não compartilham banco, credenciais, volume, Deployment nem Service com o `vehicle-catalog-api`.

## Tecnologias

- Kotlin 2.2.21, Java 21, Spring Boot 4 e Gradle Wrapper 9.4.1.
- Spring MVC, Spring Data JPA, Spring Security, Bean Validation e Actuator.
- H2 para desenvolvimento local; PostgreSQL 16 no Compose e nos manifests Kubernetes.
- JaCoCo com cobertura mínima obrigatória de 80% das linhas.

## Como executar localmente

### Requisitos

- JDK 21.
- Git para obter o repositório.
- Docker e Docker Compose somente para executar com PostgreSQL.

Confira a versão Java antes de iniciar:

```bash
java -version
```

### API com H2 em memória

Na raiz do repositório, inicie a aplicação:

```bash
./gradlew bootRun
```

O perfil `dev`, com H2 em memória, é o padrão. O banco é recriado quando a aplicação reinicia. Para usar o endpoint legado de login (`POST /api/auth/login`), configure as credenciais e uma chave JWT com pelo menos 32 bytes no ambiente antes de executar a aplicação. Exemplo para Linux/macOS:

```bash
export LOGIN_USERNAME='admin-local'
export LOGIN_PASSWORD='troque-esta-senha-local'
export JWT_SECRET='chave-local-com-pelo-menos-32-bytes'
./gradlew bootRun
```

O receptor de sincronização e o webhook de pagamento exigem `MECH_SHOP_INTERNAL_TOKEN` e `PAYMENT_WEBHOOK_SECRET`, respectivamente. Se esses valores não estiverem configurados, as rotas rejeitam as chamadas. Exporte também esses valores ao testar tais fluxos localmente; não use os exemplos acima como credenciais de produção.

### API com PostgreSQL via Docker Compose

O Compose exige credenciais e segredos explícitos. Crie o arquivo local ignorado pelo Git a partir do exemplo:

```bash
cp .env.example .env
```

Edite `.env` e substitua **todos** os valores de exemplo por valores locais próprios, incluindo credenciais PostgreSQL, token interno, segredo do webhook, login e `JWT_SECRET` (mínimo de 32 bytes). Em seguida, construa e inicie os serviços:

```bash
docker compose up --build -d
```

Confira o estado:

```bash
docker compose ps
docker compose logs -f vehicle-sales-api
```

Somente a porta da API é publicada no host (8080 por padrão, configurável com `API_HOST_PORT`). O PostgreSQL fica acessível apenas na rede interna do Compose. Os dados persistem no volume nomeado `pgdata`. Não execute `docker compose down -v`: isso remove volumes e pode apagar dados.

Se já houver um volume PostgreSQL desta aplicação, mantenha o nome do banco e o usuário/senha usados na inicialização desse volume. Alterar `POSTGRES_*` no `.env` não altera automaticamente as credenciais dentro de um banco existente nem migra seus dados. O perfil `prod` ainda usa Hibernate `ddl-auto: update` e o projeto não possui migrations versionadas; isso não deve ser considerado uma migração segura para banco populado.

A documentação Swagger fica em <http://localhost:8080/swagger-ui/index.html>. Os health checks do Actuator estão em `/actuator/health`, `/actuator/health/liveness` e `/actuator/health/readiness`.

## Configuração

| Variável | Finalidade |
|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil Spring; use `prod` com PostgreSQL. O padrão é `dev`. |
| `SPRING_DATASOURCE_URL` | URL JDBC do PostgreSQL no perfil `prod`. |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | Usuário e senha da conexão PostgreSQL. |
| `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | Inicialização do banco PostgreSQL no Compose/Kubernetes. |
| `MECH_SHOP_INTERNAL_TOKEN` | Token Bearer que autentica a sincronização de veículos. |
| `PAYMENT_WEBHOOK_SECRET` | Segredo validado no header `X-Webhook-Secret`. |
| `LOGIN_USERNAME` / `LOGIN_PASSWORD` | Credenciais do endpoint legado `/api/auth/login`. |
| `JWT_SECRET` | Segredo HMAC usado para assinar JWT; exige no mínimo 32 bytes UTF-8. |
| `API_HOST_PORT` | Porta da API publicada no host pelo Compose; padrão `8080`. |

Mantenha os valores reais em `.env` local (ignorado pelo Git), em um gerenciador de segredos ou em Kubernetes Secret. O arquivo `.env.example` contém apenas placeholders e não deve ser usado sem edição.

## Endpoints do serviço de vendas

| Método | Rota | Descrição |
|---|---|---|
| `PUT` | `/internal/vehicles/{vehicleId}` | Sincroniza a projeção versionada; exige `Authorization: Bearer <MECH_SHOP_INTERNAL_TOKEN>`. |
| `GET` | `/api/vehicles/available` | Lista veículos disponíveis em ordem crescente de preço e ID. |
| `GET` | `/api/vehicles/sold` | Lista veículos vendidos em ordem crescente de preço e ID, incluindo `soldAt`. |
| `POST` | `/api/vehicles/{vehicleId}/purchases` | Reserva veículo disponível; body: `{"buyerCpf":"<cpf-válido>"}`. |
| `POST` | `/api/webhooks/payments` | Processa pagamento aprovado ou cancelado; exige `X-Webhook-Secret`. |

Uma compra bem-sucedida retorna `201` com o ID da venda e do veículo, código de pagamento, estado e `soldAt`. As listagens não expõem CPF nem código de pagamento. Pagamento aprovado marca o veículo como vendido; cancelamento libera o veículo. Eventos de webhook repetidos são idempotentes e transições incompatíveis retornam `409`.

Exemplo de evento de pagamento:

```json
{
  "eventId": "evt-unique-001",
  "paymentCode": "pay-random-code",
  "status": "APPROVED"
}
```

## Como testar e verificar a cobertura

Execute os testes automatizados e a verificação de cobertura com:

```bash
./gradlew clean check
```

O comando executa os testes e falha se a cobertura de linhas JaCoCo ficar abaixo de 80%. Para gerar o relatório HTML:

```bash
./gradlew jacocoTestReport
```

Abra `build/reports/jacoco/test/html/index.html` no navegador. Os resultados individuais dos testes ficam em `build/reports/tests/test/index.html`.

## CI, imagem e Kubernetes

O GitHub Actions executa `./gradlew --no-daemon clean check` em Pull Requests para `main`, usando JDK 21 e o Gradle Wrapper. Após push/merge em `main`, outro workflow executa as verificações e publica a imagem `ghcr.io/vitor-felix/mech-shop-fiap-os`, identificada pelo SHA do commit e pela tag `latest`. O Dockerfile não executa testes; a publicação depende da etapa explícita de testes e cobertura no workflow.

Os manifests declarativos ficam em `k8s/`. Eles criam o namespace `vehicle-sales`, Deployment e Service internos da API, StatefulSet e Service privado do PostgreSQL, volume persistente e NetworkPolicy para restringir o acesso ao banco. A aplicação e o banco têm recursos próprios, sem compartilhar recursos do `vehicle-catalog-api`. A aplicação da NetworkPolicy depende de um CNI que implemente essa funcionalidade.

O ambiente não define cluster, classe de armazenamento, permissão para baixar a imagem do GHCR ou credenciais de deploy. Por isso, o workflow publica a imagem, mas não executa nem declara um deploy real. Antes de aplicar os manifests:

1. Revise capacidade e classe de armazenamento do PVC para o cluster escolhido.
2. Crie no namespace `vehicle-sales` o Secret `vehicle-sales-secrets` com as chaves `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `MECH_SHOP_INTERNAL_TOKEN`, `PAYMENT_WEBHOOK_SECRET`, `LOGIN_USERNAME`, `LOGIN_PASSWORD` e `JWT_SECRET`. Crie-o a partir do gerenciador de segredos aprovado; não versione o arquivo de origem.
3. Se a imagem GHCR for privada, configure um `imagePullSecret` para o namespace.
4. Aplique os manifests com `kubectl apply -f k8s/namespace.yaml` e `kubectl apply -k k8s/`; depois configure a imagem do Deployment com a tag SHA publicada e confira a prontidão da API e o PVC do banco.

Não conecte este serviço ao banco do `vehicle-catalog-api` nem reutilize recursos ou credenciais dele. O CD para um cluster deve ser habilitado somente após definir o destino e o mecanismo de credenciais.
