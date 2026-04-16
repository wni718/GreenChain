package com.greenchain.backend.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class GeoLocationService {
    
    private static final Map<String, double[]> LOCATION_MAP = new HashMap<>();
    
    static {
        LOCATION_MAP.put("New York, USA", new double[]{40.7128, -74.0060});
        LOCATION_MAP.put("Los Angeles, USA", new double[]{34.0522, -118.2437});
        LOCATION_MAP.put("Chicago, USA", new double[]{41.8781, -87.6298});
        LOCATION_MAP.put("San Francisco, USA", new double[]{37.7749, -122.4194});
        LOCATION_MAP.put("Long Beach, USA", new double[]{33.7701, -118.1937});
        LOCATION_MAP.put("Seattle, USA", new double[]{47.6062, -122.3321});
        LOCATION_MAP.put("Miami, USA", new double[]{25.7617, -80.1918});
        LOCATION_MAP.put("Dallas, USA", new double[]{32.7767, -96.7970});
        LOCATION_MAP.put("Houston, USA", new double[]{29.7604, -95.3698});
        LOCATION_MAP.put("Phoenix, USA", new double[]{33.4484, -112.0740});
        LOCATION_MAP.put("Denver, USA", new double[]{39.7392, -104.9903});
        LOCATION_MAP.put("Boston, USA", new double[]{42.3601, -71.0589});
        LOCATION_MAP.put("Atlanta, USA", new double[]{33.7490, -84.3880});
        LOCATION_MAP.put("Philadelphia, USA", new double[]{39.9526, -75.1652});
        LOCATION_MAP.put("Washington, USA", new double[]{38.9072, -77.0369});
        
        LOCATION_MAP.put("Toronto, Canada", new double[]{43.6532, -79.3832});
        LOCATION_MAP.put("Vancouver, Canada", new double[]{49.2827, -123.1207});
        LOCATION_MAP.put("Montreal, Canada", new double[]{45.5017, -73.5673});
        LOCATION_MAP.put("Calgary, Canada", new double[]{51.0447, -114.0719});
        LOCATION_MAP.put("Ottawa, Canada", new double[]{45.4215, -75.6972});
        
        LOCATION_MAP.put("Mexico City, Mexico", new double[]{19.4326, -99.1332});
        LOCATION_MAP.put("Guadalajara, Mexico", new double[]{20.6597, -103.3496});
        LOCATION_MAP.put("Monterrey, Mexico", new double[]{25.6866, -100.3161});
        LOCATION_MAP.put("Cancun, Mexico", new double[]{21.1619, -86.8515});
        
        LOCATION_MAP.put("London, UK", new double[]{51.5074, -0.1278});
        LOCATION_MAP.put("Manchester, UK", new double[]{53.4808, -2.2426});
        LOCATION_MAP.put("Birmingham, UK", new double[]{52.4862, -1.8904});
        LOCATION_MAP.put("Edinburgh, UK", new double[]{55.9533, -3.1883});
        
        LOCATION_MAP.put("Dublin, Ireland", new double[]{53.3498, -6.2603});
        LOCATION_MAP.put("Cork, Ireland", new double[]{51.8985, -8.4756});
        
        LOCATION_MAP.put("Paris, France", new double[]{48.8566, 2.3522});
        LOCATION_MAP.put("Lyon, France", new double[]{45.7640, 4.8357});
        LOCATION_MAP.put("Marseille, France", new double[]{43.2965, 5.3698});
        LOCATION_MAP.put("Nice, France", new double[]{43.7102, 7.2620});
        
        LOCATION_MAP.put("Berlin, Germany", new double[]{52.5200, 13.4050});
        LOCATION_MAP.put("Frankfurt, Germany", new double[]{50.1109, 8.6821});
        LOCATION_MAP.put("Munich, Germany", new double[]{48.1351, 11.5820});
        LOCATION_MAP.put("Hamburg, Germany", new double[]{53.5511, 9.9937});
        LOCATION_MAP.put("Cologne, Germany", new double[]{50.9375, 6.9603});
        
        LOCATION_MAP.put("Madrid, Spain", new double[]{40.4168, -3.7038});
        LOCATION_MAP.put("Barcelona, Spain", new double[]{41.3851, 2.1734});
        LOCATION_MAP.put("Valencia, Spain", new double[]{39.4699, -0.3763});
        LOCATION_MAP.put("Seville, Spain", new double[]{37.3891, -5.9845});
        
        LOCATION_MAP.put("Rome, Italy", new double[]{41.9028, 12.4964});
        LOCATION_MAP.put("Milan, Italy", new double[]{45.4642, 9.1900});
        LOCATION_MAP.put("Naples, Italy", new double[]{40.8518, 14.2681});
        LOCATION_MAP.put("Florence, Italy", new double[]{43.7696, 11.2558});
        
        LOCATION_MAP.put("Amsterdam, Netherlands", new double[]{52.3676, 4.9041});
        LOCATION_MAP.put("Rotterdam, Netherlands", new double[]{51.9244, 4.4777});
        LOCATION_MAP.put("The Hague, Netherlands", new double[]{52.0705, 4.3007});
        
        LOCATION_MAP.put("Zurich, Switzerland", new double[]{47.3769, 8.5417});
        LOCATION_MAP.put("Geneva, Switzerland", new double[]{46.2044, 6.1432});
        LOCATION_MAP.put("Bern, Switzerland", new double[]{46.9480, 7.4474});
        
        LOCATION_MAP.put("Vienna, Austria", new double[]{48.2082, 16.3738});
        LOCATION_MAP.put("Salzburg, Austria", new double[]{47.8095, 13.0550});
        
        LOCATION_MAP.put("Brussels, Belgium", new double[]{50.8503, 4.3517});
        LOCATION_MAP.put("Antwerp, Belgium", new double[]{51.2194, 4.4025});
        
        LOCATION_MAP.put("Warsaw, Poland", new double[]{52.2297, 21.0122});
        LOCATION_MAP.put("Krakow, Poland", new double[]{50.0647, 19.9450});
        LOCATION_MAP.put("Gdansk, Poland", new double[]{54.3520, 18.6466});
        
        LOCATION_MAP.put("Tokyo, Japan", new double[]{35.6762, 139.6503});
        LOCATION_MAP.put("Osaka, Japan", new double[]{34.6937, 135.5023});
        LOCATION_MAP.put("Nagoya, Japan", new double[]{35.1815, 136.9066});
        LOCATION_MAP.put("Kyoto, Japan", new double[]{35.0116, 135.7681});
        LOCATION_MAP.put("Yokohama, Japan", new double[]{35.4437, 139.6380});
        
        LOCATION_MAP.put("Beijing, China", new double[]{39.9042, 116.4074});
        LOCATION_MAP.put("Shanghai, China", new double[]{31.2304, 121.4737});
        LOCATION_MAP.put("Guangzhou, China", new double[]{23.1291, 113.2644});
        LOCATION_MAP.put("Shenzhen, China", new double[]{22.5431, 114.0579});
        LOCATION_MAP.put("Hong Kong, China", new double[]{22.3193, 114.1694});
        LOCATION_MAP.put("Chengdu, China", new double[]{30.5728, 104.0668});
        LOCATION_MAP.put("Xian, China", new double[]{34.3416, 108.9398});
        
        LOCATION_MAP.put("Seoul, South Korea", new double[]{37.5665, 126.9780});
        LOCATION_MAP.put("Busan, South Korea", new double[]{35.1796, 129.0756});
        LOCATION_MAP.put("Incheon, South Korea", new double[]{37.4563, 126.7052});
        
        LOCATION_MAP.put("Singapore", new double[]{1.3521, 103.8198});
        LOCATION_MAP.put("Kuala Lumpur, Malaysia", new double[]{3.1390, 101.6869});
        LOCATION_MAP.put("Penang, Malaysia", new double[]{5.4164, 100.3327});
        
        LOCATION_MAP.put("Bangkok, Thailand", new double[]{13.7563, 100.5018});
        LOCATION_MAP.put("Phuket, Thailand", new double[]{7.8804, 98.3923});
        LOCATION_MAP.put("Chiang Mai, Thailand", new double[]{18.7883, 98.9853});
        
        LOCATION_MAP.put("Sydney, Australia", new double[]{-33.8688, 151.2093});
        LOCATION_MAP.put("Melbourne, Australia", new double[]{-37.8136, 144.9631});
        LOCATION_MAP.put("Brisbane, Australia", new double[]{-27.4698, 153.0251});
        LOCATION_MAP.put("Perth, Australia", new double[]{-31.9505, 115.8605});
        LOCATION_MAP.put("Adelaide, Australia", new double[]{-34.9285, 138.6007});
        
        LOCATION_MAP.put("Auckland, New Zealand", new double[]{-36.8485, 174.7633});
        LOCATION_MAP.put("Wellington, New Zealand", new double[]{-41.2865, 174.7762});
        LOCATION_MAP.put("Christchurch, New Zealand", new double[]{-43.5321, 172.6362});
        
        LOCATION_MAP.put("Mumbai, India", new double[]{19.0760, 72.8777});
        LOCATION_MAP.put("Delhi, India", new double[]{28.7041, 77.1025});
        LOCATION_MAP.put("Bangalore, India", new double[]{12.9716, 77.5946});
        LOCATION_MAP.put("Chennai, India", new double[]{13.0827, 80.2707});
        LOCATION_MAP.put("Kolkata, India", new double[]{22.5726, 88.3639});
        
        LOCATION_MAP.put("Dubai, UAE", new double[]{25.2048, 55.2708});
        LOCATION_MAP.put("Abu Dhabi, UAE", new double[]{24.4539, 54.3773});
        LOCATION_MAP.put("Sharjah, UAE", new double[]{25.3463, 55.4209});
        
        LOCATION_MAP.put("Istanbul, Turkey", new double[]{41.0082, 28.9784});
        LOCATION_MAP.put("Ankara, Turkey", new double[]{39.9334, 32.8597});
        LOCATION_MAP.put("Antalya, Turkey", new double[]{36.8969, 30.7133});
        
        LOCATION_MAP.put("Cairo, Egypt", new double[]{30.0444, 31.2357});
        LOCATION_MAP.put("Alexandria, Egypt", new double[]{31.2001, 29.9187});
        LOCATION_MAP.put("Luxor, Egypt", new double[]{25.6872, 32.6396});
        
        LOCATION_MAP.put("Lagos, Nigeria", new double[]{6.5244, 3.3792});
        LOCATION_MAP.put("Abuja, Nigeria", new double[]{9.0765, 7.3986});
        LOCATION_MAP.put("Nairobi, Kenya", new double[]{-1.2921, 36.8219});
        LOCATION_MAP.put("Mombasa, Kenya", new double[]{-4.0435, 39.6682});
        
        LOCATION_MAP.put("Cape Town, South Africa", new double[]{-33.9249, 18.4241});
        LOCATION_MAP.put("Johannesburg, South Africa", new double[]{-26.2041, 28.0473});
        LOCATION_MAP.put("Durban, South Africa", new double[]{-29.8587, 31.0218});
        
        LOCATION_MAP.put("Sao Paulo, Brazil", new double[]{-23.5505, -46.6333});
        LOCATION_MAP.put("Rio de Janeiro, Brazil", new double[]{-22.9068, -43.1729});
        LOCATION_MAP.put("Brasilia, Brazil", new double[]{-15.7975, -47.8919});
        LOCATION_MAP.put("Salvador, Brazil", new double[]{-12.9714, -38.5014});
        
        LOCATION_MAP.put("Buenos Aires, Argentina", new double[]{-34.6037, -58.3816});
        LOCATION_MAP.put("Cordoba, Argentina", new double[]{-31.4201, -64.1888});
        LOCATION_MAP.put("Mendoza, Argentina", new double[]{-32.8895, -68.8458});
        
        LOCATION_MAP.put("Santiago, Chile", new double[]{-33.4489, -70.6693});
        LOCATION_MAP.put("Valparaiso, Chile", new double[]{-33.0472, -71.6127});
        LOCATION_MAP.put("Concepcion, Chile", new double[]{-36.8201, -73.0444});
        
        LOCATION_MAP.put("Lima, Peru", new double[]{-12.0464, -77.0428});
        LOCATION_MAP.put("Cusco, Peru", new double[]{-13.5319, -71.9675});
        LOCATION_MAP.put("Arequipa, Peru", new double[]{-16.4090, -71.5375});
        
        LOCATION_MAP.put("Bogota, Colombia", new double[]{4.7110, -74.0721});
        LOCATION_MAP.put("Medellin, Colombia", new double[]{6.2442, -75.5812});
        LOCATION_MAP.put("Cali, Colombia", new double[]{3.4516, -76.5320});
        
        LOCATION_MAP.put("Moscow, Russia", new double[]{55.7558, 37.6173});
        LOCATION_MAP.put("Saint Petersburg, Russia", new double[]{59.9311, 30.3609});
        LOCATION_MAP.put("Novosibirsk, Russia", new double[]{55.0084, 82.9357});
        
        LOCATION_MAP.put("Tel Aviv, Israel", new double[]{32.0853, 34.7818});
        LOCATION_MAP.put("Jerusalem, Israel", new double[]{31.7683, 35.2137});
        
        LOCATION_MAP.put("Athens, Greece", new double[]{37.9838, 23.7275});
        LOCATION_MAP.put("Thessaloniki, Greece", new double[]{40.6401, 22.9444});
        
        LOCATION_MAP.put("Lisbon, Portugal", new double[]{38.7223, -9.1393});
        LOCATION_MAP.put("Porto, Portugal", new double[]{41.1579, -8.6291});
        
        LOCATION_MAP.put("Copenhagen, Denmark", new double[]{55.6761, 12.5683});
        LOCATION_MAP.put("Aarhus, Denmark", new double[]{56.1629, 10.2031});
        
        LOCATION_MAP.put("Stockholm, Sweden", new double[]{59.3293, 18.0686});
        LOCATION_MAP.put("Gothenburg, Sweden", new double[]{57.7089, 11.9746});
        
        LOCATION_MAP.put("Oslo, Norway", new double[]{59.9139, 10.7522});
        LOCATION_MAP.put("Bergen, Norway", new double[]{60.3913, 5.3221});
        
        LOCATION_MAP.put("Helsinki, Finland", new double[]{60.1699, 24.9384});
        LOCATION_MAP.put("Tampere, Finland", new double[]{61.4981, 23.7877});
        
        LOCATION_MAP.put("Prague, Czech Republic", new double[]{50.0755, 14.4378});
        LOCATION_MAP.put("Bratislava, Slovakia", new double[]{48.1486, 17.1077});
        
        LOCATION_MAP.put("Budapest, Hungary", new double[]{47.4979, 19.0402});
        LOCATION_MAP.put("Debrecen, Hungary", new double[]{47.5317, 21.6243});
        
        LOCATION_MAP.put("Bucharest, Romania", new double[]{44.4268, 26.1025});
        LOCATION_MAP.put("Cluj-Napoca, Romania", new double[]{46.7712, 23.6236});
        
        LOCATION_MAP.put("Jakarta, Indonesia", new double[]{-6.2088, 106.8456});
        LOCATION_MAP.put("Bali, Indonesia", new double[]{-8.3405, 115.0920});
        LOCATION_MAP.put("Surabaya, Indonesia", new double[]{-7.2575, 112.7521});
        
        LOCATION_MAP.put("Manila, Philippines", new double[]{14.5995, 120.9842});
        LOCATION_MAP.put("Cebu, Philippines", new double[]{10.3157, 123.8854});
        
        LOCATION_MAP.put("Ho Chi Minh City, Vietnam", new double[]{10.8231, 106.6297});
        LOCATION_MAP.put("Hanoi, Vietnam", new double[]{21.0285, 105.8542});
        LOCATION_MAP.put("Da Nang, Vietnam", new double[]{16.0544, 108.2022});
    }
    
    public double[] getCoordinates(String location) {
        if (location == null || location.isEmpty()) {
            return new double[]{0, 0};
        }
        
        double[] coords = LOCATION_MAP.get(location);
        if (coords != null) {
            return coords;
        }
        
        String[] parts = location.split(",");
        if (parts.length >= 2) {
            String city = parts[0].trim();
            String country = parts[1].trim();
            
            for (Map.Entry<String, double[]> entry : LOCATION_MAP.entrySet()) {
                String key = entry.getKey();
                if (key.contains(city) && key.contains(country)) {
                    return entry.getValue();
                }
            }
            
            for (Map.Entry<String, double[]> entry : LOCATION_MAP.entrySet()) {
                if (keyContainsCountry(entry.getKey(), country)) {
                    return entry.getValue();
                }
            }
        }
        
        return new double[]{0, 0};
    }
    
    private boolean keyContainsCountry(String key, String country) {
        String keyCountry = key.substring(key.indexOf(",") + 1).trim();
        return keyCountry.equalsIgnoreCase(country);
    }
    
    public double getLatitude(String location) {
        return getCoordinates(location)[0];
    }
    
    public double getLongitude(String location) {
        return getCoordinates(location)[1];
    }
}