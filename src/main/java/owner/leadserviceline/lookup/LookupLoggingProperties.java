package owner.leadserviceline.lookup;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lead-service-line")
public record LookupLoggingProperties(
		boolean lookupLogEnabled,
		String lookupLogPath,
		int lookupLogRetentionDays
) {

	public LookupLoggingProperties {
		if (lookupLogRetentionDays <= 0) {
			lookupLogRetentionDays = 14;
		}
	}
}
