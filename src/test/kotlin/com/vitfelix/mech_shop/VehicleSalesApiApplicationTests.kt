package com.vitfelix.mech_shop

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
class VehicleSalesApiApplicationTests {

	@Autowired
	private lateinit var webApplicationContext: WebApplicationContext

	private lateinit var mockMvc: MockMvc

	@BeforeEach
	fun setup() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(webApplicationContext)
			.apply<DefaultMockMvcBuilder>(springSecurity())
			.build()
	}

	@Test
	fun contextLoads() {
	}

	@Test
	fun `health endpoint is available without authentication`() {
		listOf(
			"/actuator/health",
			"/actuator/health/liveness",
			"/actuator/health/readiness"
		).forEach { path ->
			mockMvc.perform(get(path))
				.andExpect(status().isOk)
		}
	}
}
