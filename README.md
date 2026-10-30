# Vehicle Sales API

Serviço isolado responsável pela projeção local de veículos, disponibilidade, compras e pagamentos. A sincronização cadastral versionada, as listagens, a reserva de compra e o webhook de pagamento estão implementados. O fluxo legado da oficina permanece temporariamente separado da projeção de vendas.

## Tecnologias

- Kotlin 2.2.21 e Java 21
- Spring Boot 4, Spring MVC, Spring Data JPA e Spring Security
- H2 em memória no perfil `dev`; PostgreSQL no perfil `prod`
- Docker e Docker Compose

## Execução local

Execute `./gradlew bootRun` para iniciar com H2. A documentação Swagger fica em <http://localhost:8080/swagger-ui/index.html>.

Para executar API e PostgreSQL:

```bash
MECH_SHOP_INTERNAL_TOKEN='configure-aqui' \
PAYMENT_WEBHOOK_SECRET='configure-aqui' \
docker compose up -d
```

Os dois segredos são opcionais para iniciar o serviço, mas os respectivos endpoints retornam `401` se não estiverem configurados. Não os grave no Git. O PostgreSQL e o volume deste serviço são independentes do `vehicle-catalog-api`.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `PUT` | `/internal/vehicles/{vehicleId}` | Sincroniza veículo do principal; exige `Authorization: Bearer <MECH_SHOP_INTERNAL_TOKEN>`. |
| `GET` | `/api/vehicles/available` | Lista veículos disponíveis ordenados por preço e ID. |
| `GET` | `/api/vehicles/sold` | Lista veículos vendidos ordenados por preço e ID, incluindo `soldAt`. |
| `POST` | `/api/vehicles/{vehicleId}/purchases` | Reserva veículo disponível; body `{"buyerCpf":"12345678909"}`. |
| `POST` | `/api/webhooks/payments` | Processa `APPROVED` ou `CANCELED`; exige `X-Webhook-Secret: <PAYMENT_WEBHOOK_SECRET>`. |

A compra responde `201` com `saleId`, `vehicleId`, `paymentCode`, `status` e `soldAt`. O CPF e o código de pagamento não aparecem nas listagens. O preço fica registrado como snapshot na venda.

Exemplo de webhook:

```json
{
  "eventId": "evt-unique-001",
  "paymentCode": "pay-random-code",
  "status": "APPROVED"
}
```

Webhooks aceitos respondem `204`. Repetir o mesmo `eventId` e conteúdo é idempotente; transições incompatíveis retornam `409`.

## Testes

Execute `./gradlew test`. A suíte contém testes de persistência e integração das regras de listagem, concorrência de reserva, snapshot, aprovação/cancelamento, duplicidade e autenticação.
