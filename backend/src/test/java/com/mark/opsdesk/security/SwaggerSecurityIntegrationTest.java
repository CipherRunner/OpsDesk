package com.mark.opsdesk.security;

import com.mark.opsdesk.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SwaggerSecurityIntegrationTest extends IntegrationTestBase {

	@Test
	void swaggerUiIsPublic() throws Exception {
		mockMvc.perform(get("/swagger-ui/index.html"))
				.andExpect(status().isOk());
	}

	@Test
	void openApiDocsArePublicAndGeneratedFromExistingControllers() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.openapi").exists())
				.andExpect(jsonPath("$.paths['/api/tickets']").exists());
	}

	@Test
	void securedApiEndpointsRemainProtected() throws Exception {
		mockMvc.perform(get("/api/tickets"))
				.andExpect(status().isUnauthorized());
	}
}
