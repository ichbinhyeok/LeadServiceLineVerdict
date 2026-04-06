package owner.leadserviceline.lookup;

import java.util.Optional;

public interface AddressGeocoder {

	Optional<GeocodedAddress> geocode(String query, String city, String state);
}
