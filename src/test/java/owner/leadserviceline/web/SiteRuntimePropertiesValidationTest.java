package owner.leadserviceline.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SiteRuntimePropertiesValidationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(TestConfig.class)
			.withPropertyValues("lead-service-line.site-base-url=https://example.test");

	@Test
	void adminEnabledWithoutExplicitCredentialsFailsClosed() {
		contextRunner
				.withPropertyValues("lead-service-line.admin-enabled=true")
				.run(context -> {
					assertNotNull(context.getStartupFailure());
					assertInstanceOf(IllegalStateException.class, rootCause(context.getStartupFailure()));
				});
	}

	@Test
	void adminEnabledWithLegacyDefaultCredentialsFailsClosed() {
		contextRunner
				.withPropertyValues(
						"lead-service-line.admin-enabled=true",
						"lead-service-line.admin-username=admin",
						"lead-service-line.admin-password=tlsgur3108"
				)
				.run(context -> {
					assertNotNull(context.getStartupFailure());
					assertInstanceOf(IllegalStateException.class, rootCause(context.getStartupFailure()));
				});
	}

	@Test
	void adminEnabledWithExplicitNonLegacyCredentialsLoads() {
		contextRunner
				.withPropertyValues(
						"lead-service-line.admin-enabled=true",
						"lead-service-line.admin-username=admin-user",
						"lead-service-line.admin-password=super-secret"
				)
				.run(context -> {
					var properties = context.getBean(SiteRuntimeProperties.class);
					org.junit.jupiter.api.Assertions.assertTrue(properties.adminConfigured());
				});
	}

	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(SiteRuntimeProperties.class)
	static class TestConfig {
	}

	private Throwable rootCause(Throwable throwable) {
		var current = throwable;
		while (current != null && current.getCause() != null) {
			current = current.getCause();
		}
		return current;
	}
}
