package owner.leadserviceline.lookup;

public record GeocodedAddress(
		String matchedAddress,
		String city,
		String state,
		String zipCode
) {
}
