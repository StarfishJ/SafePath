package com.safepath.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Decodes Google Maps encoded polyline strings into lat/lng coordinate pairs.
 * Implementation follows the algorithm described in:
 * https://developers.google.com/maps/documentation/utilities/polylinealgorithm
 */
public class PolylineDecoder {

    /**
     * Simple lat/lng coordinate pair.
     */
    public static class LatLng {
        public final double lat;
        public final double lng;

        public LatLng(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }

    /**
     * Decodes a Google polyline string into a list of coordinates.
     *
     * @param encoded the encoded polyline string from Google Directions API
     * @return list of lat/lng points along the path
     */
    public static List<LatLng> decode(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        if (encoded == null || encoded.isEmpty()) {
            return poly;
        }

        int index = 0;
        int len = encoded.length();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            double latitude = lat / 1E5;
            double longitude = lng / 1E5;
            poly.add(new LatLng(latitude, longitude));
        }

        return poly;
    }
}

