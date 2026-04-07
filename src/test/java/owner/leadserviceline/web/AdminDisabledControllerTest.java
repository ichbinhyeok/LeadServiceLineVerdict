package owner.leadserviceline.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"lead-service-line.census-geocoder-enabled=false",
		"lead-service-line.site-base-url=https://example.test"
})
@AutoConfigureMockMvc
class AdminDisabledControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void adminRouteReturnsNotFoundWhenAdminIsDisabled() throws Exception {
		mockMvc.perform(get("/admin"))
				.andExpect(status().isNotFound());
	}
}
