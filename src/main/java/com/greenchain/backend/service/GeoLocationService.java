package com.greenchain.backend.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class GeoLocationService {

    private static final Map<String, double[]> LOCATION_MAP = new HashMap<>();

    static {
        LOCATION_MAP.put("New York, USA", new double[] { 40.7128, -74.0060 });
        LOCATION_MAP.put("Los Angeles, USA", new double[] { 34.0522, -118.2437 });
        LOCATION_MAP.put("Chicago, USA", new double[] { 41.8781, -87.6298 });
        LOCATION_MAP.put("Houston, USA", new double[] { 29.7604, -95.3698 });
        LOCATION_MAP.put("Phoenix, USA", new double[] { 33.4484, -112.0740 });
        LOCATION_MAP.put("Philadelphia, USA", new double[] { 39.9526, -75.1652 });
        LOCATION_MAP.put("San Antonio, USA", new double[] { 29.4241, -98.4936 });
        LOCATION_MAP.put("San Diego, USA", new double[] { 32.7157, -117.1611 });
        LOCATION_MAP.put("Dallas, USA", new double[] { 32.7767, -96.7970 });
        LOCATION_MAP.put("San Jose, USA", new double[] { 37.3382, -121.8863 });
        LOCATION_MAP.put("Austin, USA", new double[] { 30.2672, -97.7431 });
        LOCATION_MAP.put("Jacksonville, USA", new double[] { 30.3322, -81.6557 });
        LOCATION_MAP.put("Fort Worth, USA", new double[] { 32.7555, -97.3308 });
        LOCATION_MAP.put("Columbus, USA", new double[] { 39.9612, -82.9988 });
        LOCATION_MAP.put("Charlotte, USA", new double[] { 35.2271, -80.8431 });
        LOCATION_MAP.put("San Francisco, USA", new double[] { 37.7749, -122.4194 });
        LOCATION_MAP.put("Indianapolis, USA", new double[] { 39.7684, -86.1581 });
        LOCATION_MAP.put("Seattle, USA", new double[] { 47.6062, -122.3321 });
        LOCATION_MAP.put("Denver, USA", new double[] { 39.7392, -104.9903 });
        LOCATION_MAP.put("Washington, USA", new double[] { 38.9072, -77.0369 });

        LOCATION_MAP.put("Toronto, Canada", new double[] { 43.6532, -79.3832 });
        LOCATION_MAP.put("Montreal, Canada", new double[] { 45.5017, -73.5673 });
        LOCATION_MAP.put("Vancouver, Canada", new double[] { 49.2827, -123.1207 });
        LOCATION_MAP.put("Calgary, Canada", new double[] { 51.0447, -114.0719 });
        LOCATION_MAP.put("Edmonton, Canada", new double[] { 53.5444, -113.4909 });
        LOCATION_MAP.put("Ottawa, Canada", new double[] { 45.4215, -75.6972 });
        LOCATION_MAP.put("Winnipeg, Canada", new double[] { 49.8951, -97.1384 });
        LOCATION_MAP.put("Quebec City, Canada", new double[] { 46.8139, -71.2080 });
        LOCATION_MAP.put("Hamilton, Canada", new double[] { 43.2557, -79.8711 });
        LOCATION_MAP.put("Kitchener, Canada", new double[] { 43.4516, -80.4925 });
        LOCATION_MAP.put("London, Canada", new double[] { 42.9849, -81.2330 });
        LOCATION_MAP.put("Victoria, Canada", new double[] { 48.4284, -123.3656 });
        LOCATION_MAP.put("Halifax, Canada", new double[] { 44.6488, -63.5752 });
        LOCATION_MAP.put("Oshawa, Canada", new double[] { 43.8946, -78.8656 });
        LOCATION_MAP.put("Windsor, Canada", new double[] { 42.3077, -83.0158 });
        LOCATION_MAP.put("Saskatoon, Canada", new double[] { 52.1332, -106.6700 });
        LOCATION_MAP.put("Regina, Canada", new double[] { 50.4452, -104.6189 });
        LOCATION_MAP.put("St. Johns, Canada", new double[] { 47.5615, -52.7126 });
        LOCATION_MAP.put("Barrie, Canada", new double[] { 44.3871, -79.6992 });
        LOCATION_MAP.put("Kelowna, Canada", new double[] { 49.8844, -119.4939 });

        LOCATION_MAP.put("Mexico City, Mexico", new double[] { 19.4326, -99.1332 });
        LOCATION_MAP.put("Guadalajara, Mexico", new double[] { 20.6597, -103.3496 });
        LOCATION_MAP.put("Monterrey, Mexico", new double[] { 25.6866, -100.3161 });
        LOCATION_MAP.put("Puebla, Mexico", new double[] { 19.0414, -98.2063 });
        LOCATION_MAP.put("Tijuana, Mexico", new double[] { 32.5228, -117.0056 });
        LOCATION_MAP.put("León, Mexico", new double[] { 21.1225, -101.6868 });
        LOCATION_MAP.put("Juárez, Mexico", new double[] { 31.7312, -106.4850 });
        LOCATION_MAP.put("Zapopan, Mexico", new double[] { 20.7007, -103.4157 });
        LOCATION_MAP.put("Merida, Mexico", new double[] { 20.9672, -89.6232 });
        LOCATION_MAP.put("Culiacán, Mexico", new double[] { 24.7850, -107.3909 });
        LOCATION_MAP.put("Chihuahua, Mexico", new double[] { 28.6353, -106.0889 });
        LOCATION_MAP.put("Saltillo, Mexico", new double[] { 25.4333, -101.0000 });
        LOCATION_MAP.put("Torreón, Mexico", new double[] { 25.5333, -103.4167 });
        LOCATION_MAP.put("Mazatlán, Mexico", new double[] { 23.2466, -106.4194 });
        LOCATION_MAP.put("Aguascalientes, Mexico", new double[] { 21.8853, -102.2915 });
        LOCATION_MAP.put("Hermosillo, Mexico", new double[] { 29.0938, -110.9675 });
        LOCATION_MAP.put("San Luis Potosí, Mexico", new double[] { 22.1565, -100.9855 });
        LOCATION_MAP.put("Toluca, Mexico", new double[] { 19.2891, -99.6556 });
        LOCATION_MAP.put("Tampico, Mexico", new double[] { 22.2711, -97.8484 });
        LOCATION_MAP.put("Morelia, Mexico", new double[] { 19.7014, -101.1831 });

        LOCATION_MAP.put("London, UK", new double[] { 51.5074, -0.1278 });
        LOCATION_MAP.put("Birmingham, UK", new double[] { 52.4862, -1.8904 });
        LOCATION_MAP.put("Manchester, UK", new double[] { 53.4808, -2.2426 });
        LOCATION_MAP.put("Leeds, UK", new double[] { 53.8008, -1.5491 });
        LOCATION_MAP.put("Glasgow, UK", new double[] { 55.8642, -4.2518 });
        LOCATION_MAP.put("Sheffield, UK", new double[] { 53.3811, -1.4701 });
        LOCATION_MAP.put("Bradford, UK", new double[] { 53.7932, -1.7577 });
        LOCATION_MAP.put("Liverpool, UK", new double[] { 53.4084, -2.9916 });
        LOCATION_MAP.put("Edinburgh, UK", new double[] { 55.9533, -3.1883 });
        LOCATION_MAP.put("Bristol, UK", new double[] { 51.4545, -2.5879 });
        LOCATION_MAP.put("Cardiff, UK", new double[] { 51.4816, -3.1791 });
        LOCATION_MAP.put("Newcastle upon Tyne, UK", new double[] { 54.9783, -1.6178 });
        LOCATION_MAP.put("Leicester, UK", new double[] { 52.6369, -1.1398 });
        LOCATION_MAP.put("Nottingham, UK", new double[] { 52.9548, -1.1581 });
        LOCATION_MAP.put("Portsmouth, UK", new double[] { 50.8058, -1.0872 });
        LOCATION_MAP.put("Brighton, UK", new double[] { 50.8225, -0.1372 });
        LOCATION_MAP.put("Southampton, UK", new double[] { 50.9097, -1.4044 });
        LOCATION_MAP.put("Swansea, UK", new double[] { 51.6214, -3.9436 });
        LOCATION_MAP.put("Aberdeen, UK", new double[] { 57.1497, -2.0943 });
        LOCATION_MAP.put("Plymouth, UK", new double[] { 50.3755, -4.1427 });

        LOCATION_MAP.put("Dublin, Ireland", new double[] { 53.3498, -6.2603 });
        LOCATION_MAP.put("Cork, Ireland", new double[] { 51.8985, -8.4756 });

        LOCATION_MAP.put("Paris, France", new double[] { 48.8566, 2.3522 });
        LOCATION_MAP.put("Marseille, France", new double[] { 43.2965, 5.3698 });
        LOCATION_MAP.put("Lyon, France", new double[] { 45.7640, 4.8357 });
        LOCATION_MAP.put("Toulouse, France", new double[] { 43.6047, 1.4442 });
        LOCATION_MAP.put("Nice, France", new double[] { 43.7102, 7.2620 });
        LOCATION_MAP.put("Nantes, France", new double[] { 47.2184, -1.5536 });
        LOCATION_MAP.put("Strasbourg, France", new double[] { 48.5734, 7.7521 });
        LOCATION_MAP.put("Montpellier, France", new double[] { 43.6108, 3.8767 });
        LOCATION_MAP.put("Bordeaux, France", new double[] { 44.8378, -0.5792 });
        LOCATION_MAP.put("Lille, France", new double[] { 50.6366, 3.0635 });
        LOCATION_MAP.put("Rennes, France", new double[] { 48.1173, -1.6778 });
        LOCATION_MAP.put("Reims, France", new double[] { 49.2583, 4.0317 });
        LOCATION_MAP.put("Le Havre, France", new double[] { 49.4943, 0.1079 });
        LOCATION_MAP.put("Saint-Étienne, France", new double[] { 45.4397, 4.3872 });
        LOCATION_MAP.put("Toulon, France", new double[] { 43.1242, 5.9280 });
        LOCATION_MAP.put("Grenoble, France", new double[] { 45.1834, 5.7246 });
        LOCATION_MAP.put("Dijon, France", new double[] { 47.3220, 5.0415 });
        LOCATION_MAP.put("Angers, France", new double[] { 47.4784, -0.5632 });
        LOCATION_MAP.put("Nîmes, France", new double[] { 43.8373, 4.3601 });
        LOCATION_MAP.put("Villeurbanne, France", new double[] { 45.7772, 4.8963 });

        LOCATION_MAP.put("Berlin, Germany", new double[] { 52.5200, 13.4050 });
        LOCATION_MAP.put("Hamburg, Germany", new double[] { 53.5511, 9.9937 });
        LOCATION_MAP.put("Munich, Germany", new double[] { 48.1351, 11.5820 });
        LOCATION_MAP.put("Cologne, Germany", new double[] { 50.9375, 6.9603 });
        LOCATION_MAP.put("Frankfurt, Germany", new double[] { 50.1109, 8.6821 });
        LOCATION_MAP.put("Stuttgart, Germany", new double[] { 48.7758, 9.1829 });
        LOCATION_MAP.put("Düsseldorf, Germany", new double[] { 51.2277, 6.7735 });
        LOCATION_MAP.put("Dortmund, Germany", new double[] { 51.5136, 7.4653 });
        LOCATION_MAP.put("Essen, Germany", new double[] { 51.4545, 7.0116 });
        LOCATION_MAP.put("Leipzig, Germany", new double[] { 51.3434, 12.3731 });
        LOCATION_MAP.put("Bremen, Germany", new double[] { 53.0793, 8.8017 });
        LOCATION_MAP.put("Dresden, Germany", new double[] { 51.0504, 13.7373 });
        LOCATION_MAP.put("Hanover, Germany", new double[] { 52.3702, 9.7333 });
        LOCATION_MAP.put("Nuremberg, Germany", new double[] { 49.4478, 11.0683 });
        LOCATION_MAP.put("Duisburg, Germany", new double[] { 51.4346, 6.7612 });
        LOCATION_MAP.put("Bochum, Germany", new double[] { 51.4816, 7.2167 });
        LOCATION_MAP.put("Wuppertal, Germany", new double[] { 51.2667, 7.1833 });
        LOCATION_MAP.put("Bielefeld, Germany", new double[] { 52.0302, 8.5325 });
        LOCATION_MAP.put("Bonn, Germany", new double[] { 50.7374, 7.0982 });
        LOCATION_MAP.put("Münster, Germany", new double[] { 51.9607, 7.6261 });

        LOCATION_MAP.put("Madrid, Spain", new double[] { 40.4168, -3.7038 });
        LOCATION_MAP.put("Barcelona, Spain", new double[] { 41.3851, 2.1734 });
        LOCATION_MAP.put("Valencia, Spain", new double[] { 39.4699, -0.3763 });
        LOCATION_MAP.put("Seville, Spain", new double[] { 37.3891, -5.9845 });

        LOCATION_MAP.put("Rome, Italy", new double[] { 41.9028, 12.4964 });
        LOCATION_MAP.put("Milan, Italy", new double[] { 45.4642, 9.1900 });
        LOCATION_MAP.put("Naples, Italy", new double[] { 40.8518, 14.2681 });
        LOCATION_MAP.put("Turin, Italy", new double[] { 45.0703, 7.6869 });
        LOCATION_MAP.put("Palermo, Italy", new double[] { 38.1157, 13.3615 });
        LOCATION_MAP.put("Genoa, Italy", new double[] { 44.4056, 8.9463 });
        LOCATION_MAP.put("Bologna, Italy", new double[] { 44.4949, 11.3426 });
        LOCATION_MAP.put("Florence, Italy", new double[] { 43.7696, 11.2558 });
        LOCATION_MAP.put("Venice, Italy", new double[] { 45.4408, 12.3155 });
        LOCATION_MAP.put("Verona, Italy", new double[] { 45.4384, 10.9916 });
        LOCATION_MAP.put("Bari, Italy", new double[] { 41.1253, 16.8661 });
        LOCATION_MAP.put("Catania, Italy", new double[] { 37.5022, 15.0873 });
        LOCATION_MAP.put("Messina, Italy", new double[] { 38.1875, 15.5553 });
        LOCATION_MAP.put("Padua, Italy", new double[] { 45.4047, 11.8737 });
        LOCATION_MAP.put("Trieste, Italy", new double[] { 45.6579, 13.7719 });
        LOCATION_MAP.put("Taranto, Italy", new double[] { 40.4637, 17.2384 });
        LOCATION_MAP.put("Prato, Italy", new double[] { 43.8716, 11.0315 });
        LOCATION_MAP.put("Modena, Italy", new double[] { 44.6463, 10.9264 });
        LOCATION_MAP.put("Reggio Emilia, Italy", new double[] { 44.6963, 10.6280 });
        LOCATION_MAP.put("Parma, Italy", new double[] { 44.8014, 10.3280 });

        LOCATION_MAP.put("Amsterdam, Netherlands", new double[] { 52.3676, 4.9041 });
        LOCATION_MAP.put("Rotterdam, Netherlands", new double[] { 51.9244, 4.4777 });
        LOCATION_MAP.put("The Hague, Netherlands", new double[] { 52.0705, 4.3007 });

        LOCATION_MAP.put("Zurich, Switzerland", new double[] { 47.3769, 8.5417 });
        LOCATION_MAP.put("Geneva, Switzerland", new double[] { 46.2044, 6.1432 });
        LOCATION_MAP.put("Bern, Switzerland", new double[] { 46.9480, 7.4474 });

        LOCATION_MAP.put("Vienna, Austria", new double[] { 48.2082, 16.3738 });
        LOCATION_MAP.put("Salzburg, Austria", new double[] { 47.8095, 13.0550 });

        LOCATION_MAP.put("Brussels, Belgium", new double[] { 50.8503, 4.3517 });
        LOCATION_MAP.put("Antwerp, Belgium", new double[] { 51.2194, 4.4025 });

        LOCATION_MAP.put("Warsaw, Poland", new double[] { 52.2297, 21.0122 });
        LOCATION_MAP.put("Krakow, Poland", new double[] { 50.0647, 19.9450 });
        LOCATION_MAP.put("Gdansk, Poland", new double[] { 54.3520, 18.6466 });

        LOCATION_MAP.put("Tokyo, Japan", new double[] { 35.6762, 139.6503 });
        LOCATION_MAP.put("Yokohama, Japan", new double[] { 35.4437, 139.6380 });
        LOCATION_MAP.put("Osaka, Japan", new double[] { 34.6937, 135.5023 });
        LOCATION_MAP.put("Nagoya, Japan", new double[] { 35.1815, 136.9066 });
        LOCATION_MAP.put("Sapporo, Japan", new double[] { 43.0618, 141.3545 });
        LOCATION_MAP.put("Fukuoka, Japan", new double[] { 33.5902, 130.4017 });
        LOCATION_MAP.put("Kobe, Japan", new double[] { 34.6901, 135.1812 });
        LOCATION_MAP.put("Kyoto, Japan", new double[] { 35.0116, 135.7681 });
        LOCATION_MAP.put("Kawasaki, Japan", new double[] { 35.5312, 139.7004 });
        LOCATION_MAP.put("Saitama, Japan", new double[] { 35.8617, 139.6453 });
        LOCATION_MAP.put("Hiroshima, Japan", new double[] { 34.3964, 132.4597 });
        LOCATION_MAP.put("Sendai, Japan", new double[] { 38.2688, 140.8694 });
        LOCATION_MAP.put("Chiba, Japan", new double[] { 35.6056, 140.1236 });
        LOCATION_MAP.put("Kitakyushu, Japan", new double[] { 33.8333, 130.8333 });
        LOCATION_MAP.put("Tokushima, Japan", new double[] { 34.0667, 134.5500 });
        LOCATION_MAP.put("Okayama, Japan", new double[] { 34.6617, 133.9367 });
        LOCATION_MAP.put("Fukushima, Japan", new double[] { 37.7500, 140.4667 });
        LOCATION_MAP.put("Nagasaki, Japan", new double[] { 32.7442, 129.8731 });
        LOCATION_MAP.put("Kumamoto, Japan", new double[] { 32.7892, 130.7417 });
        LOCATION_MAP.put("Kagoshima, Japan", new double[] { 31.5603, 130.5587 });

        LOCATION_MAP.put("Beijing, China", new double[] { 39.9042, 116.4074 });
        LOCATION_MAP.put("Shanghai, China", new double[] { 31.2304, 121.4737 });
        LOCATION_MAP.put("Guangzhou, China", new double[] { 23.1291, 113.2644 });
        LOCATION_MAP.put("Shenzhen, China", new double[] { 22.5431, 114.0579 });
        LOCATION_MAP.put("Chengdu, China", new double[] { 30.5728, 104.0668 });
        LOCATION_MAP.put("Wuhan, China", new double[] { 30.5928, 114.3055 });
        LOCATION_MAP.put("Tianjin, China", new double[] { 39.3434, 117.3616 });
        LOCATION_MAP.put("Chongqing, China", new double[] { 29.4316, 106.9123 });
        LOCATION_MAP.put("Nanjing, China", new double[] { 32.0603, 118.7969 });
        LOCATION_MAP.put("Hangzhou, China", new double[] { 30.2741, 120.1551 });
        LOCATION_MAP.put("Suzhou, China", new double[] { 31.2989, 120.5853 });
        LOCATION_MAP.put("Dalian, China", new double[] { 38.9140, 121.6147 });
        LOCATION_MAP.put("Qingdao, China", new double[] { 36.0611, 120.3826 });
        LOCATION_MAP.put("Xiamen, China", new double[] { 24.4798, 118.0819 });
        LOCATION_MAP.put("Changsha, China", new double[] { 28.2278, 112.9388 });
        LOCATION_MAP.put("Nanning, China", new double[] { 22.8170, 108.3665 });
        LOCATION_MAP.put("Kunming, China", new double[] { 25.0389, 102.7183 });
        LOCATION_MAP.put("Xian, China", new double[] { 34.3416, 108.9398 });
        LOCATION_MAP.put("Wuxi, China", new double[] { 31.5583, 120.3456 });
        LOCATION_MAP.put("Fuzhou, China", new double[] { 26.0745, 119.2965 });

        LOCATION_MAP.put("Seoul, South Korea", new double[] { 37.5665, 126.9780 });
        LOCATION_MAP.put("Busan, South Korea", new double[] { 35.1796, 129.0756 });
        LOCATION_MAP.put("Incheon, South Korea", new double[] { 37.4563, 126.7052 });
        LOCATION_MAP.put("Daegu, South Korea", new double[] { 35.8714, 128.6014 });
        LOCATION_MAP.put("Daejeon, South Korea", new double[] { 36.3414, 127.3850 });
        LOCATION_MAP.put("Gwangju, South Korea", new double[] { 35.1595, 126.8526 });
        LOCATION_MAP.put("Suwon, South Korea", new double[] { 37.2636, 127.0286 });
        LOCATION_MAP.put("Ulsan, South Korea", new double[] { 35.5384, 129.3114 });
        LOCATION_MAP.put("Changwon, South Korea", new double[] { 35.2281, 128.6747 });
        LOCATION_MAP.put("Goyang, South Korea", new double[] { 37.6594, 126.8325 });
        LOCATION_MAP.put("Ansan, South Korea", new double[] { 37.3185, 126.8316 });
        LOCATION_MAP.put("Seongnam, South Korea", new double[] { 37.4569, 127.1230 });
        LOCATION_MAP.put("Bucheon, South Korea", new double[] { 37.5081, 126.7640 });
        LOCATION_MAP.put("Jeonju, South Korea", new double[] { 35.8242, 127.1489 });
        LOCATION_MAP.put("Cheongju, South Korea", new double[] { 36.6496, 127.4895 });
        LOCATION_MAP.put("Pohang, South Korea", new double[] { 36.0393, 129.3444 });
        LOCATION_MAP.put("Gimhae, South Korea", new double[] { 35.2381, 128.8962 });
        LOCATION_MAP.put("Dangjin, South Korea", new double[] { 36.9350, 126.6431 });
        LOCATION_MAP.put("Guri, South Korea", new double[] { 37.6392, 127.1286 });
        LOCATION_MAP.put("Yongin, South Korea", new double[] { 37.2079, 127.2126 });

        LOCATION_MAP.put("Singapore", new double[] { 1.3521, 103.8198 });
        LOCATION_MAP.put("Kuala Lumpur, Malaysia", new double[] { 3.1390, 101.6869 });
        LOCATION_MAP.put("Penang, Malaysia", new double[] { 5.4164, 100.3327 });

        LOCATION_MAP.put("Bangkok, Thailand", new double[] { 13.7563, 100.5018 });
        LOCATION_MAP.put("Phuket, Thailand", new double[] { 7.8804, 98.3923 });
        LOCATION_MAP.put("Chiang Mai, Thailand", new double[] { 18.7883, 98.9853 });

        LOCATION_MAP.put("Sydney, Australia", new double[] { -33.8688, 151.2093 });
        LOCATION_MAP.put("Melbourne, Australia", new double[] { -37.8136, 144.9631 });
        LOCATION_MAP.put("Brisbane, Australia", new double[] { -27.4698, 153.0251 });
        LOCATION_MAP.put("Perth, Australia", new double[] { -31.9505, 115.8605 });
        LOCATION_MAP.put("Adelaide, Australia", new double[] { -34.9285, 138.6007 });
        LOCATION_MAP.put("Gold Coast, Australia", new double[] { -28.0167, 153.4000 });
        LOCATION_MAP.put("Newcastle, Australia", new double[] { -32.9283, 151.7817 });
        LOCATION_MAP.put("Canberra, Australia", new double[] { -35.2809, 149.1300 });
        LOCATION_MAP.put("Wollongong, Australia", new double[] { -34.4250, 150.8931 });
        LOCATION_MAP.put("Logan City, Australia", new double[] { -27.6667, 153.1333 });
        LOCATION_MAP.put("Geelong, Australia", new double[] { -38.1499, 144.3617 });
        LOCATION_MAP.put("Hobart, Australia", new double[] { -42.8821, 147.3272 });
        LOCATION_MAP.put("Townsville, Australia", new double[] { -19.2583, 146.8167 });
        LOCATION_MAP.put("Cairns, Australia", new double[] { -16.9203, 145.7669 });
        LOCATION_MAP.put("Darwin, Australia", new double[] { -12.4634, 130.8456 });
        LOCATION_MAP.put("Toowoomba, Australia", new double[] { -27.5599, 151.9500 });
        LOCATION_MAP.put("Ballarat, Australia", new double[] { -37.5622, 143.8503 });
        LOCATION_MAP.put("Bendigo, Australia", new double[] { -36.7594, 144.2780 });
        LOCATION_MAP.put("Albury-Wodonga, Australia", new double[] { -36.0833, 146.9167 });
        LOCATION_MAP.put("Launceston, Australia", new double[] { -41.4545, 147.1667 });

        LOCATION_MAP.put("Auckland, New Zealand", new double[] { -36.8485, 174.7633 });
        LOCATION_MAP.put("Wellington, New Zealand", new double[] { -41.2865, 174.7762 });
        LOCATION_MAP.put("Christchurch, New Zealand", new double[] { -43.5321, 172.6362 });
        LOCATION_MAP.put("Hamilton, New Zealand", new double[] { -37.7870, 175.2793 });
        LOCATION_MAP.put("Tauranga, New Zealand", new double[] { -37.6877, 176.1655 });
        LOCATION_MAP.put("Napier-Hastings, New Zealand", new double[] { -39.4944, 176.9189 });
        LOCATION_MAP.put("Dunedin, New Zealand", new double[] { -45.8742, 170.5036 });
        LOCATION_MAP.put("Palmerston North, New Zealand", new double[] { -40.3583, 175.6092 });
        LOCATION_MAP.put("Nelson, New Zealand", new double[] { -41.2700, 173.2833 });
        LOCATION_MAP.put("Rotorua, New Zealand", new double[] { -38.1456, 176.2417 });
        LOCATION_MAP.put("New Plymouth, New Zealand", new double[] { -39.0595, 174.0756 });
        LOCATION_MAP.put("Whangarei, New Zealand", new double[] { -35.7306, 174.3286 });
        LOCATION_MAP.put("Invercargill, New Zealand", new double[] { -46.4133, 168.3550 });
        LOCATION_MAP.put("Gisborne, New Zealand", new double[] { -38.6546, 178.0041 });
        LOCATION_MAP.put("Whanganui, New Zealand", new double[] { -39.9333, 175.0500 });
        LOCATION_MAP.put("Timaru, New Zealand", new double[] { -44.4000, 171.2472 });
        LOCATION_MAP.put("Blenheim, New Zealand", new double[] { -41.5036, 173.9500 });
        LOCATION_MAP.put("Queenstown, New Zealand", new double[] { -45.0312, 168.6626 });
        LOCATION_MAP.put("Rangiora, New Zealand", new double[] { -43.3000, 172.5667 });
        LOCATION_MAP.put("Taupo, New Zealand", new double[] { -38.6856, 176.0717 });

        LOCATION_MAP.put("Mumbai, India", new double[] { 19.0760, 72.8777 });
        LOCATION_MAP.put("Delhi, India", new double[] { 28.7041, 77.1025 });
        LOCATION_MAP.put("Bangalore, India", new double[] { 12.9716, 77.5946 });
        LOCATION_MAP.put("Hyderabad, India", new double[] { 17.3850, 78.4867 });
        LOCATION_MAP.put("Chennai, India", new double[] { 13.0827, 80.2707 });
        LOCATION_MAP.put("Kolkata, India", new double[] { 22.5726, 88.3639 });
        LOCATION_MAP.put("Ahmedabad, India", new double[] { 23.0225, 72.5714 });
        LOCATION_MAP.put("Pune, India", new double[] { 18.5204, 73.8567 });
        LOCATION_MAP.put("Jaipur, India", new double[] { 26.9124, 75.7873 });
        LOCATION_MAP.put("Lucknow, India", new double[] { 26.8467, 80.9462 });
        LOCATION_MAP.put("Kanpur, India", new double[] { 26.4499, 80.3319 });
        LOCATION_MAP.put("Nagpur, India", new double[] { 21.1458, 79.0882 });
        LOCATION_MAP.put("Indore, India", new double[] { 22.7196, 75.8577 });
        LOCATION_MAP.put("Thane, India", new double[] { 19.1804, 72.9647 });
        LOCATION_MAP.put("Bhopal, India", new double[] { 23.2599, 77.4126 });
        LOCATION_MAP.put("Visakhapatnam, India", new double[] { 17.6868, 83.2185 });
        LOCATION_MAP.put("Pimpri-Chinchwad, India", new double[] { 18.6298, 73.7989 });
        LOCATION_MAP.put("Patna, India", new double[] { 25.5941, 85.1376 });
        LOCATION_MAP.put("Vadodara, India", new double[] { 22.3072, 73.1812 });
        LOCATION_MAP.put("Ghaziabad, India", new double[] { 28.6692, 77.4538 });

        LOCATION_MAP.put("Dubai, UAE", new double[] { 25.2048, 55.2708 });
        LOCATION_MAP.put("Abu Dhabi, UAE", new double[] { 24.4539, 54.3773 });
        LOCATION_MAP.put("Sharjah, UAE", new double[] { 25.3463, 55.4209 });

        LOCATION_MAP.put("Istanbul, Turkey", new double[] { 41.0082, 28.9784 });
        LOCATION_MAP.put("Ankara, Turkey", new double[] { 39.9334, 32.8597 });
        LOCATION_MAP.put("Antalya, Turkey", new double[] { 36.8969, 30.7133 });

        LOCATION_MAP.put("Cairo, Egypt", new double[] { 30.0444, 31.2357 });
        LOCATION_MAP.put("Alexandria, Egypt", new double[] { 31.2001, 29.9187 });
        LOCATION_MAP.put("Luxor, Egypt", new double[] { 25.6872, 32.6396 });

        // Morocco cities
        LOCATION_MAP.put("Casablanca, Morocco", new double[] { 33.5731, -7.5898 });
        LOCATION_MAP.put("Rabat, Morocco", new double[] { 34.0209, -6.8416 });
        LOCATION_MAP.put("Fes, Morocco", new double[] { 34.0331, -4.9998 });
        LOCATION_MAP.put("Marrakech, Morocco", new double[] { 31.6318, -7.9964 });
        LOCATION_MAP.put("Tangier, Morocco", new double[] { 35.7596, -5.8330 });
        LOCATION_MAP.put("Agadir, Morocco", new double[] { 30.4200, -9.5981 });
        LOCATION_MAP.put("Meknes, Morocco", new double[] { 33.8869, -5.5228 });
        LOCATION_MAP.put("Oujda, Morocco", new double[] { 34.6876, -1.9040 });
        LOCATION_MAP.put("Kenitra, Morocco", new double[] { 34.2612, -6.5810 });
        LOCATION_MAP.put("Tetouan, Morocco", new double[] { 35.5718, -5.3654 });
        LOCATION_MAP.put("Safi, Morocco", new double[] { 32.2900, -9.2311 });
        LOCATION_MAP.put("Mohammedia, Morocco", new double[] { 33.6367, -7.3530 });
        LOCATION_MAP.put("Khouribga, Morocco", new double[] { 32.8691, -6.9350 });
        LOCATION_MAP.put("El Jadida, Morocco", new double[] { 33.2165, -8.5940 });
        LOCATION_MAP.put("Beni Mellal, Morocco", new double[] { 32.3333, -6.3333 });
        LOCATION_MAP.put("Nador, Morocco", new double[] { 35.1676, -2.9281 });
        LOCATION_MAP.put("Taza, Morocco", new double[] { 34.1769, -4.1188 });
        LOCATION_MAP.put("Larache, Morocco", new double[] { 35.1667, -6.1333 });
        LOCATION_MAP.put("Settat, Morocco", new double[] { 32.8333, -6.5000 });
        LOCATION_MAP.put("Khemisset, Morocco", new double[] { 33.8000, -5.7000 });

        LOCATION_MAP.put("Lagos, Nigeria", new double[] { 6.5244, 3.3792 });
        LOCATION_MAP.put("Abuja, Nigeria", new double[] { 9.0765, 7.3986 });
        LOCATION_MAP.put("Nairobi, Kenya", new double[] { -1.2921, 36.8219 });
        LOCATION_MAP.put("Mombasa, Kenya", new double[] { -4.0435, 39.6682 });

        LOCATION_MAP.put("Cape Town, South Africa", new double[] { -33.9249, 18.4241 });
        LOCATION_MAP.put("Johannesburg, South Africa", new double[] { -26.2041, 28.0473 });
        LOCATION_MAP.put("Durban, South Africa", new double[] { -29.8587, 31.0218 });
        LOCATION_MAP.put("Pretoria, South Africa", new double[] { -25.7463, 28.2292 });
        LOCATION_MAP.put("Port Elizabeth, South Africa", new double[] { -33.9180, 25.5701 });
        LOCATION_MAP.put("Bloemfontein, South Africa", new double[] { -29.1212, 26.2140 });
        LOCATION_MAP.put("East London, South Africa", new double[] { -33.0153, 27.9116 });
        LOCATION_MAP.put("Pietermaritzburg, South Africa", new double[] { -29.6100, 30.3794 });
        LOCATION_MAP.put("Kimberley, South Africa", new double[] { -28.7410, 24.7680 });
        LOCATION_MAP.put("Polokwane, South Africa", new double[] { -23.9070, 29.4653 });
        LOCATION_MAP.put("Benoni, South Africa", new double[] { -26.1833, 28.3000 });
        LOCATION_MAP.put("Brakpan, South Africa", new double[] { -26.3000, 28.3167 });
        LOCATION_MAP.put("Germiston, South Africa", new double[] { -26.2469, 28.1794 });
        LOCATION_MAP.put("Krugersdorp, South Africa", new double[] { -26.0833, 27.7833 });
        LOCATION_MAP.put("Randburg, South Africa", new double[] { -26.0986, 28.0728 });
        LOCATION_MAP.put("Roodepoort, South Africa", new double[] { -26.1167, 27.8000 });
        LOCATION_MAP.put("Soweto, South Africa", new double[] { -26.2708, 27.8497 });
        LOCATION_MAP.put("Springs, South Africa", new double[] { -26.3500, 28.4000 });
        LOCATION_MAP.put("Vereeniging, South Africa", new double[] { -26.6667, 28.1833 });
        LOCATION_MAP.put("Welkom, South Africa", new double[] { -27.9000, 26.7333 });

        LOCATION_MAP.put("São Paulo, Brazil", new double[] { -23.5505, -46.6333 });
        LOCATION_MAP.put("Rio de Janeiro, Brazil", new double[] { -22.9068, -43.1729 });
        LOCATION_MAP.put("Brasília, Brazil", new double[] { -15.7975, -47.8919 });
        LOCATION_MAP.put("Salvador, Brazil", new double[] { -12.9714, -38.5014 });
        LOCATION_MAP.put("Fortaleza, Brazil", new double[] { -3.7172, -38.5434 });
        LOCATION_MAP.put("Belo Horizonte, Brazil", new double[] { -19.9167, -43.9345 });
        LOCATION_MAP.put("Manaus, Brazil", new double[] { -3.1019, -60.0250 });
        LOCATION_MAP.put("Curitiba, Brazil", new double[] { -25.4297, -49.2733 });
        LOCATION_MAP.put("Recife, Brazil", new double[] { -8.0476, -34.8770 });
        LOCATION_MAP.put("Porto Alegre, Brazil", new double[] { -30.0346, -51.2177 });
        LOCATION_MAP.put("Goiânia, Brazil", new double[] { -16.6869, -49.2648 });
        LOCATION_MAP.put("Belém, Brazil", new double[] { -1.4558, -48.4904 });
        LOCATION_MAP.put("Guarulhos, Brazil", new double[] { -23.4628, -46.5333 });
        LOCATION_MAP.put("Campinas, Brazil", new double[] { -22.9056, -47.0611 });
        LOCATION_MAP.put("São Luís, Brazil", new double[] { -2.5333, -44.3000 });
        LOCATION_MAP.put("São Gonçalo, Brazil", new double[] { -23.0667, -43.2944 });
        LOCATION_MAP.put("Maceió, Brazil", new double[] { -9.6658, -35.7353 });
        LOCATION_MAP.put("Duque de Caxias, Brazil", new double[] { -22.7744, -43.3656 });
        LOCATION_MAP.put("Natal, Brazil", new double[] { -5.7945, -35.2110 });
        LOCATION_MAP.put("Nova Iguaçu, Brazil", new double[] { -22.7522, -43.4906 });

        LOCATION_MAP.put("Buenos Aires, Argentina", new double[] { -34.6037, -58.3816 });
        LOCATION_MAP.put("Cordoba, Argentina", new double[] { -31.4201, -64.1888 });
        LOCATION_MAP.put("Mendoza, Argentina", new double[] { -32.8895, -68.8458 });

        LOCATION_MAP.put("Santiago, Chile", new double[] { -33.4489, -70.6693 });
        LOCATION_MAP.put("Valparaíso, Chile", new double[] { -33.0472, -71.6127 });
        LOCATION_MAP.put("Concepción, Chile", new double[] { -36.8201, -73.0444 });
        LOCATION_MAP.put("La Serena, Chile", new double[] { -29.9022, -71.2675 });
        LOCATION_MAP.put("Antofagasta, Chile", new double[] { -23.6500, -70.4000 });
        LOCATION_MAP.put("Iquique, Chile", new double[] { -20.2208, -70.1431 });
        LOCATION_MAP.put("Talca, Chile", new double[] { -35.4372, -71.6570 });
        LOCATION_MAP.put("Temuco, Chile", new double[] { -38.7333, -72.6000 });
        LOCATION_MAP.put("Arica, Chile", new double[] { -18.4833, -70.3333 });
        LOCATION_MAP.put("Coquimbo, Chile", new double[] { -29.9533, -71.3433 });
        LOCATION_MAP.put("Rancagua, Chile", new double[] { -34.1700, -70.7500 });
        LOCATION_MAP.put("Chillán, Chile", new double[] { -36.6167, -72.0833 });
        LOCATION_MAP.put("Osorno, Chile", new double[] { -40.5756, -73.1333 });
        LOCATION_MAP.put("Puerto Montt, Chile", new double[] { -41.4642, -72.9375 });
        LOCATION_MAP.put("Viña del Mar, Chile", new double[] { -33.0292, -71.5383 });
        LOCATION_MAP.put("Valdivia, Chile", new double[] { -39.8167, -73.2500 });
        LOCATION_MAP.put("Quilpué, Chile", new double[] { -33.0758, -71.5217 });
        LOCATION_MAP.put("San Bernardo, Chile", new double[] { -33.5639, -70.6808 });
        LOCATION_MAP.put("Puente Alto, Chile", new double[] { -33.5997, -70.5725 });
        LOCATION_MAP.put("Maipú, Chile", new double[] { -33.5253, -70.7603 });

        LOCATION_MAP.put("Lima, Peru", new double[] { -12.0464, -77.0428 });
        LOCATION_MAP.put("Cusco, Peru", new double[] { -13.5319, -71.9675 });
        LOCATION_MAP.put("Arequipa, Peru", new double[] { -16.4090, -71.5375 });

        LOCATION_MAP.put("Bogota, Colombia", new double[] { 4.7110, -74.0721 });
        LOCATION_MAP.put("Medellin, Colombia", new double[] { 6.2442, -75.5812 });
        LOCATION_MAP.put("Cali, Colombia", new double[] { 3.4516, -76.5320 });

        LOCATION_MAP.put("Moscow, Russia", new double[] { 55.7558, 37.6173 });
        LOCATION_MAP.put("Saint Petersburg, Russia", new double[] { 59.9311, 30.3609 });
        LOCATION_MAP.put("Novosibirsk, Russia", new double[] { 55.0084, 82.9357 });

        LOCATION_MAP.put("Tel Aviv, Israel", new double[] { 32.0853, 34.7818 });
        LOCATION_MAP.put("Jerusalem, Israel", new double[] { 31.7683, 35.2137 });

        LOCATION_MAP.put("Athens, Greece", new double[] { 37.9838, 23.7275 });
        LOCATION_MAP.put("Thessaloniki, Greece", new double[] { 40.6401, 22.9444 });

        LOCATION_MAP.put("Lisbon, Portugal", new double[] { 38.7223, -9.1393 });
        LOCATION_MAP.put("Porto, Portugal", new double[] { 41.1579, -8.6291 });

        LOCATION_MAP.put("Copenhagen, Denmark", new double[] { 55.6761, 12.5683 });
        LOCATION_MAP.put("Aarhus, Denmark", new double[] { 56.1629, 10.2031 });

        LOCATION_MAP.put("Stockholm, Sweden", new double[] { 59.3293, 18.0686 });
        LOCATION_MAP.put("Gothenburg, Sweden", new double[] { 57.7089, 11.9746 });
        LOCATION_MAP.put("Malmö, Sweden", new double[] { 55.6052, 13.0038 });
        LOCATION_MAP.put("Uppsala, Sweden", new double[] { 59.8586, 17.6389 });
        LOCATION_MAP.put("Linköping, Sweden", new double[] { 58.4108, 15.6214 });
        LOCATION_MAP.put("Västerås, Sweden", new double[] { 59.6000, 16.5497 });
        LOCATION_MAP.put("Örebro, Sweden", new double[] { 59.2756, 15.2103 });
        LOCATION_MAP.put("Helsingborg, Sweden", new double[] { 56.0465, 12.6944 });
        LOCATION_MAP.put("Jönköping, Sweden", new double[] { 57.7863, 14.1667 });
        LOCATION_MAP.put("Norrköping, Sweden", new double[] { 58.5804, 16.1960 });
        LOCATION_MAP.put("Lund, Sweden", new double[] { 55.7048, 13.1913 });
        LOCATION_MAP.put("Umeå, Sweden", new double[] { 63.8258, 20.3014 });
        LOCATION_MAP.put("Gävle, Sweden", new double[] { 60.6749, 17.1413 });
        LOCATION_MAP.put("Södertälje, Sweden", new double[] { 59.2044, 17.6986 });
        LOCATION_MAP.put("Eskilstuna, Sweden", new double[] { 59.3780, 16.5130 });
        LOCATION_MAP.put("Borås, Sweden", new double[] { 57.7387, 12.9346 });
        LOCATION_MAP.put("Täby, Sweden", new double[] { 59.4195, 18.0879 });
        LOCATION_MAP.put("Halmstad, Sweden", new double[] { 56.6720, 12.8547 });
        LOCATION_MAP.put("Växjö, Sweden", new double[] { 56.8771, 14.8063 });
        LOCATION_MAP.put("Karlstad, Sweden", new double[] { 59.3978, 13.5058 });

        LOCATION_MAP.put("Oslo, Norway", new double[] { 59.9139, 10.7522 });
        LOCATION_MAP.put("Bergen, Norway", new double[] { 60.3913, 5.3221 });

        LOCATION_MAP.put("Helsinki, Finland", new double[] { 60.1699, 24.9384 });
        LOCATION_MAP.put("Tampere, Finland", new double[] { 61.4981, 23.7877 });

        LOCATION_MAP.put("Prague, Czech Republic", new double[] { 50.0755, 14.4378 });
        LOCATION_MAP.put("Bratislava, Slovakia", new double[] { 48.1486, 17.1077 });

        LOCATION_MAP.put("Budapest, Hungary", new double[] { 47.4979, 19.0402 });
        LOCATION_MAP.put("Debrecen, Hungary", new double[] { 47.5317, 21.6243 });

        LOCATION_MAP.put("Bucharest, Romania", new double[] { 44.4268, 26.1025 });
        LOCATION_MAP.put("Cluj-Napoca, Romania", new double[] { 46.7712, 23.6236 });

        LOCATION_MAP.put("Jakarta, Indonesia", new double[] { -6.2088, 106.8456 });
        LOCATION_MAP.put("Bali, Indonesia", new double[] { -8.3405, 115.0920 });
        LOCATION_MAP.put("Surabaya, Indonesia", new double[] { -7.2575, 112.7521 });

        LOCATION_MAP.put("Manila, Philippines", new double[] { 14.5995, 120.9842 });
        LOCATION_MAP.put("Cebu, Philippines", new double[] { 10.3157, 123.8854 });

        LOCATION_MAP.put("Ho Chi Minh City, Vietnam", new double[] { 10.8231, 106.6297 });
        LOCATION_MAP.put("Hanoi, Vietnam", new double[] { 21.0285, 105.8542 });
        LOCATION_MAP.put("Da Nang, Vietnam", new double[] { 16.0544, 108.2022 });
    }

    public double[] getCoordinates(String location) {
        if (location == null || location.isEmpty()) {
            return new double[] { 0, 0 };
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

        return new double[] { 0, 0 };
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