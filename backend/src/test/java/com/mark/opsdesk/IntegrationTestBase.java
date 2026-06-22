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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.Constructor;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers(disabledWithoutDocker = true)
public abstract class IntegrationTestBase {

	protected static final String TEST_PASSWORD = "password123";

	@Container
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
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
		registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
		registry.add("opsdesk.demo-data.enabled", () -> "false");
	}

	@BeforeEach
	void cleanDatabase() {
		ticketAuditEntryRepository.deleteAllInBatch();
		ticketCommentRepository.deleteAllInBatch();
		ticketRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();
	}

	protected User createTestUser(String username, Role role) {
		User user = newTestUser(username, passwordEncoder.encode(TEST_PASSWORD), role);
		return userRepository.save(user);
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

	private User newTestUser(String username, String passwordHash, Role role) {
		try {
			Constructor<User> constructor = User.class.getDeclaredConstructor(String.class, String.class, Role.class);
			constructor.setAccessible(true);
			return constructor.newInstance(username, passwordHash, role);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Unable to create test user", exception);
		}
	}
}
