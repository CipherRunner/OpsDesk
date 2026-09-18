package com.mark.opsdesk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mark.opsdesk.ticket.TicketAuditEntryRepository;
import com.mark.opsdesk.ticket.TicketCommentRepository;
import com.mark.opsdesk.ticket.TicketRepository;
import com.mark.opsdesk.user.Role;
import com.mark.opsdesk.user.User;
import com.mark.opsdesk.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Shared base for HTTP-level tests. One PostgreSQL container and one Spring context serve every
 * subclass: the container is started on first use (not as a JUnit-managed {@code @Container}, which
 * would stop it after the first class) and Testcontainers' Ryuk removes it when the JVM exits.
 * Tables are emptied before each test instead of rebuilding the context.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
public abstract class IntegrationTestBase {

	protected static final String TEST_PASSWORD = "password123";

	private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private TicketCommentRepository ticketCommentRepository;

	@Autowired
	private TicketAuditEntryRepository ticketAuditEntryRepository;

	@DynamicPropertySource
	static void registerPostgresProperties(DynamicPropertyRegistry registry) {
		if (!POSTGRES.isRunning()) {
			POSTGRES.start();
		}
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
		registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
		registry.add("opsdesk.demo-data.enabled", () -> "false");
		registry.add("opsdesk.security.jwt.secret", () -> "integration-test-secret-integration-test-secret");
	}

	@BeforeEach
	void cleanDatabase() {
		ticketAuditEntryRepository.deleteAllInBatch();
		ticketCommentRepository.deleteAllInBatch();
		ticketRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();
	}

	protected User createTestUser(String username, Role role) {
		return userRepository.save(User.create(username, passwordEncoder.encode(TEST_PASSWORD), role));
	}

	protected String login(String username) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(Map.of(
								"username", username,
								"password", TEST_PASSWORD
						))))
				.andExpect(status().isOk())
				.andReturn();

		return objectMapper.readTree(result.getResponse().getContentAsString()).path("token").asText();
	}

	protected String bearer(String token) {
		return "Bearer " + token;
	}

	protected String json(Object value) throws JsonProcessingException {
		return objectMapper.writeValueAsString(value);
	}
}
