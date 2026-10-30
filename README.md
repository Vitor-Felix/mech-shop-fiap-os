# Vehicle Sales API

`vehicle-sales-api` is the isolated vehicle-sales service. It maintains its own vehicle projection, handles availability and purchases, and receives authenticated payment webhooks. Vehicle catalog data arrives over HTTP from `vehicle-catalog-api`; this service never connects to that project's database.

The legacy workshop endpoints remain separate. The sales API and its PostgreSQL database run in their own Compose network or Kubernetes namespace, with no shared database, credentials, volume, Deployment, or Service.

## Stack

- Kotlin 2.2.21, Java 21, Spring Boot 4, Gradle Wrapper 9.4.1
- Spring MVC, Spring Data JPA, Spring Security, Bean Validation and Actuator
- H2 for local development; PostgreSQL 16 for Compose/Kubernetes
- JaCoCo line coverage gate: at least 80%

## Local development

Run the API with the in-memory H2 database:

```bash
./gradlew bootRun
```

The `dev` profile is active by default. To use the legacy admin login locally, set `LOGIN_USERNAME`, `LOGIN_PASSWORD` and a `JWT_SECRET` of at least 32 bytes in the shell. Internal vehicle synchronization and payment webhooks also require `MECH_SHOP_INTERNAL_TOKEN` and `PAYMENT_WEBHOOK_SECRET`; without them, those routes reject requests.

For PostgreSQL and Docker Compose, copy `.env.example` to `.env`, replace every placeholder with unique local values, and run:

```bash
docker compose up --build -d
```

Compose requires all values in `.env`, including PostgreSQL credentials and the internal, webhook and legacy-login secrets. The database is reachable only by the API on the private Compose network; only the API port is published to the host. The named `pgdata` volume remains persistent. For an existing database volume, retain the database name, user and password used when that volume was initialized: changing `POSTGRES_*` environment values does not rotate credentials or migrate data. Never run `docker compose down -v` to troubleshoot or upgrade this service.

The application still uses Hibernate `ddl-auto: update` in the production profile and has no versioned database migrations. Do not treat this as a safe migration strategy for a populated database; back up and plan schema changes separately.

Swagger UI is available at <http://localhost:8080/swagger-ui/index.html> and health checks at `/actuator/health`.

## Configuration

| Variable | Purpose |
|---|---|
| `SPRING_PROFILES_ACTIVE` | Use `prod` for PostgreSQL; defaults to `dev` in `application.yaml`. |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL in the `prod` profile. |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | PostgreSQL credentials. |
| `MECH_SHOP_INTERNAL_TOKEN` | Bearer token accepted by vehicle synchronization. |
| `PAYMENT_WEBHOOK_SECRET` | Secret accepted in `X-Webhook-Secret`. |
| `LOGIN_USERNAME` / `LOGIN_PASSWORD` | Credentials for the legacy `/api/auth/login` endpoint. |
| `JWT_SECRET` | HMAC signing secret; must contain at least 32 UTF-8 bytes. |
| `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | PostgreSQL initialization values for Compose/Kubernetes. |
| `API_HOST_PORT` | Optional host port for Compose; defaults to `8080`. |

Keep actual values in an ignored local `.env`, a secret manager, or a Kubernetes Secret. The committed `.env.example` contains placeholders only.

## Sales endpoints

| Method | Route | Description |
|---|---|---|
| `PUT` | `/internal/vehicles/{vehicleId}` | Versioned vehicle projection; requires `Authorization: Bearer <MECH_SHOP_INTERNAL_TOKEN>`. |
| `GET` | `/api/vehicles/available` | Lists available vehicles ordered by price and ID. |
| `GET` | `/api/vehicles/sold` | Lists sold vehicles ordered by price and ID, including `soldAt`. |
| `POST` | `/api/vehicles/{vehicleId}/purchases` | Reserves an available vehicle; body: `{"buyerCpf":"<valid-cpf>"}`. |
| `POST` | `/api/webhooks/payments` | Processes `APPROVED` or `CANCELED`; requires `X-Webhook-Secret`. |

A successful purchase returns `201` with the sale ID, vehicle ID, one-time payment code, status and `soldAt`. Public vehicle listings do not expose the buyer CPF or payment code. An approved payment marks the vehicle sold; cancellation releases it. Repeated webhook event IDs are idempotent and conflicting transitions return `409`.

Example payment event:

```json
{
  "eventId": "evt-unique-001",
  "paymentCode": "pay-random-code",
  "status": "APPROVED"
}
```

## Tests and coverage

Run the full test suite and the mandatory JaCoCo line-coverage check:

```bash
./gradlew clean check
```

`check` fails when line coverage is below 80%. Generate/view the HTML report at `build/reports/jacoco/test/html/index.html` with:

```bash
./gradlew jacocoTestReport
```

## CI, image publication and Kubernetes

GitHub Actions runs `clean check` with JDK 21 and the Gradle Wrapper on pull requests targeting `main`. After a push to `main` (including a merge), it runs the same checks and publishes `ghcr.io/vitor-felix/mech-shop-fiap-os` tagged with the commit SHA and `latest`. The Dockerfile does not run tests itself, so publication is gated by the workflow checks.

Declarative Kubernetes resources are in `k8s/`. They create a dedicated `vehicle-sales` namespace, an internal API Service and Deployment, and a separate PostgreSQL StatefulSet, private Service, persistent volume claim and database ingress policy. The database has no externally published Service. NetworkPolicy enforcement depends on a cluster CNI that supports it.

No cluster, namespace ownership, registry pull access or deployment credentials were provided for this project. Therefore, the workflow publishes the tested image but intentionally does not claim or perform a live cluster deployment. To deploy to a selected cluster:

1. Provide the cluster's storage class and review the PostgreSQL PVC size/resources for that environment.
2. Create the namespace and a `vehicle-sales-secrets` Secret containing `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `MECH_SHOP_INTERNAL_TOKEN`, `PAYMENT_WEBHOOK_SECRET`, `LOGIN_USERNAME`, `LOGIN_PASSWORD` and `JWT_SECRET`. Keep the source file outside the repository and populate the Secret from the approved secret manager.
3. If GHCR is private, configure an `imagePullSecret` in this namespace before applying the API Deployment.
4. Apply the manifests with `kubectl apply -f k8s/namespace.yaml` followed by `kubectl apply -k k8s/`, then set the Deployment image to the published SHA tag. Verify the API readiness probe and database PVC before routing traffic.

Do not point this deployment at the `vehicle-catalog-api` database or reuse its namespace-scoped resources or secrets. Actual CD to a cluster should only be enabled after its target and credential mechanism are agreed.
