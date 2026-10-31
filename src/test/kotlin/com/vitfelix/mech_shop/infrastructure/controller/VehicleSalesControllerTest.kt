package com.vitfelix.mech_shop.infrastructure.controller

import com.vitfelix.mech_shop.domain.enums.SaleStatus
import com.vitfelix.mech_shop.domain.enums.VehicleAvailability
import com.vitfelix.mech_shop.application.dto.request.VehicleSyncRequest
import com.vitfelix.mech_shop.application.service.VehicleSyncService
import com.vitfelix.mech_shop.domain.model.VehicleProjectionEntity
import com.vitfelix.mech_shop.domain.repository.ProcessedPaymentEventRepository
import com.vitfelix.mech_shop.domain.repository.SaleRepository
import com.vitfelix.mech_shop.domain.repository.VehicleProjectionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest(
    properties = [
        "MECH_SHOP_INTERNAL_TOKEN=local-internal-token",
        "PAYMENT_WEBHOOK_SECRET=local-payment-secret"
    ]
)
class VehicleSalesControllerTest {
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var vehicleRepository: VehicleProjectionRepository

    @Autowired
    private lateinit var saleRepository: SaleRepository

    @Autowired
    private lateinit var paymentEventRepository: ProcessedPaymentEventRepository

    @Autowired
    private lateinit var vehicleSyncService: VehicleSyncService

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()

        paymentEventRepository.deleteAll()
        saleRepository.deleteAll()
        vehicleRepository.deleteAll()
    }

    @Test
    fun `lists available vehicles sorted by price and excludes payment data`() {
        vehicle(42L, "125000.00")
        vehicle(7L, "90000.00")
        vehicle(9L, "90000.00")

        mockMvc.perform(get("/api/vehicles/available"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].vehicleId").value(7))
            .andExpect(jsonPath("$[0].price").value(90000.0))
            .andExpect(jsonPath("$[1].vehicleId").value(9))
            .andExpect(jsonPath("$[2].vehicleId").value(42))
            .andExpect(jsonPath("$[0].buyerCpf").doesNotExist())
            .andExpect(jsonPath("$[0].paymentCode").doesNotExist())
            .andExpect(jsonPath("$[0].soldAt").doesNotExist())
    }

    @Test
    fun `purchase reserves the vehicle and snapshots its price without returning cpf`() {
        vehicle(42L, "125000.00")

        val result = mockMvc.perform(purchaseRequest(42L))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.saleId").isNumber)
            .andExpect(jsonPath("$.vehicleId").value(42))
            .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
            .andExpect(jsonPath("$.soldAt").value(org.hamcrest.Matchers.nullValue()))
            .andExpect(jsonPath("$.buyerCpf").doesNotExist())
            .andReturn()

        val sale = saleRepository.findAll().single()
        assertEquals(BigDecimal("125000.00"), sale.priceSnapshot)
        assertEquals("12345678909", sale.buyerCpf)
        assertTrue(sale.paymentCode.matches(Regex("^pay-[A-Za-z0-9_-]{43}$")))
        assertEquals(VehicleAvailability.RESERVED, vehicleRepository.findById(42L).orElseThrow().availability)
        assertTrue(result.response.contentAsString.contains(sale.paymentCode))

        mockMvc.perform(purchaseRequest(42L))
            .andExpect(status().isConflict)
    }

    @Test
    @WithMockUser
    fun `admin can inspect vehicle availability and latest sale status`() {
        vehicle(42L, "125000.00")

        mockMvc.perform(get("/api/admin/vehicles/42/sale-status"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.vehicleId").value(42))
            .andExpect(jsonPath("$.availability").value("AVAILABLE"))
            .andExpect(jsonPath("$.saleStatus").value(org.hamcrest.Matchers.nullValue()))
            .andExpect(jsonPath("$.paymentCode").value(org.hamcrest.Matchers.nullValue()))

        val paymentCode = createPurchase()
        mockMvc.perform(get("/api/admin/vehicles/42/sale-status"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.availability").value("RESERVED"))
            .andExpect(jsonPath("$.saleStatus").value("PENDING_PAYMENT"))
            .andExpect(jsonPath("$.paymentCode").value(paymentCode))
            .andExpect(jsonPath("$.createdAt").isNotEmpty)
            .andExpect(jsonPath("$.soldAt").value(org.hamcrest.Matchers.nullValue()))

        mockMvc.perform(
            webhookRequest("""{"eventId":"evt-status-approved","paymentCode":"$paymentCode","status":"APPROVED"}""")
        ).andExpect(status().isNoContent)

        mockMvc.perform(get("/api/admin/vehicles/42/sale-status"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.availability").value("SOLD"))
            .andExpect(jsonPath("$.saleStatus").value("PAID"))
            .andExpect(jsonPath("$.paymentCode").value(paymentCode))
            .andExpect(jsonPath("$.soldAt").isNotEmpty)
    }

    @Test
    fun `sale status endpoint requires authentication and returns not found for unknown vehicle`() {
        vehicle(42L, "125000.00")

        mockMvc.perform(get("/api/admin/vehicles/42/sale-status"))
            .andExpect(status().isForbidden)

        mockMvc.perform(get("/api/admin/vehicles/404/sale-status").with(user("admin")))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `later catalog price changes do not change the purchase snapshot`() {
        vehicle(42L, "125000.00")
        val paymentCode = createPurchase()

        vehicleSyncService.synchronize(
            42L,
            VehicleSyncRequest("Toyota", "Corolla", 2024, "Prata", BigDecimal("130000.00"), 2)
        )

        assertEquals(BigDecimal("125000.00"), saleRepository.findAll().single().priceSnapshot)
        assertEquals(BigDecimal("130000.00"), vehicleRepository.findById(42L).orElseThrow().price)
        assertEquals(paymentCode, saleRepository.findAll().single().paymentCode)
    }

    @Test
    fun `rejects invalid cpf and unknown vehicle`() {
        vehicle(42L, "125000.00")

        mockMvc.perform(purchaseRequest(42L, """{"buyerCpf":"00000000000"}"""))
            .andExpect(status().isBadRequest)

        mockMvc.perform(purchaseRequest(404L))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `concurrent purchases reserve a vehicle only once`() {
        vehicle(42L, "125000.00")
        val start = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(2)
        try {
            val attempts = (1..2).map {
                pool.submit<Int> {
                    start.await(5, TimeUnit.SECONDS)
                    mockMvc.perform(purchaseRequest(42L)).andReturn().response.status
                }
            }
            start.countDown()

            val statuses = attempts.map { it.get(20, TimeUnit.SECONDS) }.sorted()

            assertEquals(listOf(201, 409), statuses)
            assertEquals(1, saleRepository.count())
            assertEquals(VehicleAvailability.RESERVED, vehicleRepository.findById(42L).orElseThrow().availability)
        } finally {
            pool.shutdownNow()
        }
    }

    @Test
    fun `webhook authentication is required and unknown payment code returns not found`() {
        mockMvc.perform(
            post("/api/webhooks/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"eventId":"evt-auth","paymentCode":"missing","status":"APPROVED"}""")
        )
            .andExpect(status().isUnauthorized)

        mockMvc.perform(
            post("/api/webhooks/payments")
                .header("X-Webhook-Secret", "invalid-secret")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"eventId":"evt-auth","paymentCode":"missing","status":"APPROVED"}""")
        ).andExpect(status().isUnauthorized)

        mockMvc.perform(
            webhookRequest("""{"eventId":"evt-missing","paymentCode":"unknown","status":"APPROVED"}""")
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `approved payment sells the vehicle and duplicate event has no repeated effect`() {
        vehicle(42L, "125000.00")
        val paymentCode = createPurchase()
        val payload = """{"eventId":"evt-approved","paymentCode":"$paymentCode","status":"APPROVED"}"""

        mockMvc.perform(webhookRequest(payload)).andExpect(status().isNoContent)
        val paidSale = saleRepository.findAll().single()
        val soldVehicle = vehicleRepository.findById(42L).orElseThrow()
        assertEquals(SaleStatus.PAID, paidSale.status)
        assertTrue(paidSale.soldAt != null)
        assertEquals(VehicleAvailability.SOLD, soldVehicle.availability)

        mockMvc.perform(webhookRequest(payload)).andExpect(status().isNoContent)
        assertEquals(1, paymentEventRepository.count())

        mockMvc.perform(get("/api/vehicles/available"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isEmpty)

        mockMvc.perform(get("/api/vehicles/sold"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].vehicleId").value(42))
            .andExpect(jsonPath("$[0].soldAt").isNotEmpty)
            .andExpect(jsonPath("$[0].buyerCpf").doesNotExist())
            .andExpect(jsonPath("$[0].paymentCode").doesNotExist())
    }

    @Test
    @WithMockUser
    fun `canceled payment releases the vehicle and permits a later purchase`() {
        vehicle(42L, "125000.00")
        val paymentCode = createPurchase()
        val payload = """{"eventId":"evt-canceled","paymentCode":"$paymentCode","status":"CANCELED"}"""

        mockMvc.perform(webhookRequest(payload)).andExpect(status().isNoContent)
        mockMvc.perform(webhookRequest(payload)).andExpect(status().isNoContent)

        assertEquals(SaleStatus.CANCELED, saleRepository.findAll().single().status)
        assertEquals(VehicleAvailability.AVAILABLE, vehicleRepository.findById(42L).orElseThrow().availability)
        assertEquals(1, paymentEventRepository.count())
        mockMvc.perform(get("/api/admin/vehicles/42/sale-status"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.availability").value("AVAILABLE"))
            .andExpect(jsonPath("$.saleStatus").value("CANCELED"))
            .andExpect(jsonPath("$.paymentCode").value(paymentCode))
        mockMvc.perform(purchaseRequest(42L)).andExpect(status().isCreated)
    }

    @Test
    fun `rejects conflicting events and payment transitions`() {
        vehicle(42L, "125000.00")
        val paymentCode = createPurchase()

        mockMvc.perform(
            webhookRequest("""{"eventId":"evt-conflict","paymentCode":"$paymentCode","status":"APPROVED"}""")
        ).andExpect(status().isNoContent)

        mockMvc.perform(
            webhookRequest("""{"eventId":"evt-conflict","paymentCode":"$paymentCode","status":"CANCELED"}""")
        ).andExpect(status().isConflict)

        mockMvc.perform(
            webhookRequest("""{"eventId":"evt-second","paymentCode":"$paymentCode","status":"CANCELED"}""")
        ).andExpect(status().isConflict)
    }

    @Test
    fun `rejects malformed webhook payload`() {
        mockMvc.perform(webhookRequest("""{"eventId":"evt-invalid","paymentCode":"pay-code","status":"REFUNDED"}"""))
            .andExpect(status().isBadRequest)
    }

    private fun purchaseRequest(vehicleId: Long, body: String = """{"buyerCpf":"12345678909"}""") =
        post("/api/vehicles/$vehicleId/purchases")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)

    private fun webhookRequest(body: String) =
        post("/api/webhooks/payments")
            .header("X-Webhook-Secret", "local-payment-secret")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)

    private fun createPurchase(): String {
        mockMvc.perform(purchaseRequest(42L)).andExpect(status().isCreated)
        return saleRepository.findAll().single().paymentCode
    }

    private fun vehicle(
        vehicleId: Long,
        price: String,
        availability: VehicleAvailability = VehicleAvailability.AVAILABLE
    ) = vehicleRepository.save(
        VehicleProjectionEntity(
            vehicleId = vehicleId,
            brand = "Toyota",
            model = "Corolla",
            year = 2024,
            color = "Prata",
            price = BigDecimal(price),
            version = 1,
            availability = availability
        )
    )
}
