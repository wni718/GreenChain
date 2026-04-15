-- Sample global supplier and shipment data

-- Insert global suppliers
INSERT INTO supplier (name, country, has_environmental_certification, emission_factor_per_unit, contact_email)
SELECT * FROM (
    -- North America suppliers
    SELECT 'EcoMaterials Inc.' AS name, 'United States' AS country, true AS has_environmental_certification, 0.15 AS emission_factor_per_unit, 'contact@ecomaterials.com' AS contact_email UNION
    SELECT 'Canadian Green Solutions' AS name, 'Canada' AS country, true AS has_environmental_certification, 0.12 AS emission_factor_per_unit, 'info@canadiangreen.ca' AS contact_email UNION
    SELECT 'Mexican Sustainable Industries' AS name, 'Mexico' AS country, false AS has_environmental_certification, 0.25 AS emission_factor_per_unit, 'sustainability@mexind.com' AS contact_email UNION
    
    -- Europe suppliers
    SELECT 'European Eco Products' AS name, 'Germany' AS country, true AS has_environmental_certification, 0.10 AS emission_factor_per_unit, 'info@europeaneco.de' AS contact_email UNION
    SELECT 'French Green Technologies' AS name, 'France' AS country, true AS has_environmental_certification, 0.13 AS emission_factor_per_unit, 'contact@frenchtech.fr' AS contact_email UNION
    SELECT 'Italian Sustainable Materials' AS name, 'Italy' AS country, false AS has_environmental_certification, 0.18 AS emission_factor_per_unit, 'sustainability@italianmaterials.it' AS contact_email UNION
    SELECT 'Swedish Environmental Solutions' AS name, 'Sweden' AS country, true AS has_environmental_certification, 0.08 AS emission_factor_per_unit, 'info@swedishenviro.se' AS contact_email UNION
    
    -- Asia suppliers
    SELECT 'Japan Green Industries' AS name, 'Japan' AS country, true AS has_environmental_certification, 0.11 AS emission_factor_per_unit, 'contact@japangreen.co.jp' AS contact_email UNION
    SELECT 'Chinese Sustainable Manufacturing' AS name, 'China' AS country, false AS has_environmental_certification, 0.22 AS emission_factor_per_unit, 'sustainability@chinasustainable.cn' AS contact_email UNION
    SELECT 'Indian Eco Products' AS name, 'India' AS country, false AS has_environmental_certification, 0.28 AS emission_factor_per_unit, 'info@indianeco.in' AS contact_email UNION
    SELECT 'South Korean Green Tech' AS name, 'South Korea' AS country, true AS has_environmental_certification, 0.14 AS emission_factor_per_unit, 'contact@koreangreentech.kr' AS contact_email UNION
    
    -- Oceania suppliers
    SELECT 'Australian Environmental Solutions' AS name, 'Australia' AS country, true AS has_environmental_certification, 0.16 AS emission_factor_per_unit, 'info@australianenv.com.au' AS contact_email UNION
    SELECT 'New Zealand Sustainable Industries' AS name, 'New Zealand' AS country, true AS has_environmental_certification, 0.09 AS emission_factor_per_unit, 'contact@nzsustainable.co.nz' AS contact_email UNION
    
    -- South America suppliers
    SELECT 'Brazilian Green Materials' AS name, 'Brazil' AS country, false AS has_environmental_certification, 0.20 AS emission_factor_per_unit, 'sustainability@braziliangreen.com.br' AS contact_email UNION
    SELECT 'Chilean Sustainable Resources' AS name, 'Chile' AS country, true AS has_environmental_certification, 0.17 AS emission_factor_per_unit, 'info@chilesustainable.cl' AS contact_email UNION
    
    -- Africa suppliers
    SELECT 'South African Eco Industries' AS name, 'South Africa' AS country, false AS has_environmental_certification, 0.30 AS emission_factor_per_unit, 'contact@southafricaneco.co.za' AS contact_email UNION
    SELECT 'Moroccan Green Solutions' AS name, 'Morocco' AS country, true AS has_environmental_certification, 0.23 AS emission_factor_per_unit, 'info@moroccangreen.ma' AS contact_email
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM supplier WHERE name = tmp.name);

-- Insert sample shipments for the suppliers
-- First, let's get the supplier IDs and transport mode IDs
-- Then insert shipments with realistic global routes

-- Get supplier IDs for reference
SET @supplier_us = (SELECT id FROM supplier WHERE name = 'EcoMaterials Inc.');
SET @supplier_ca = (SELECT id FROM supplier WHERE name = 'Canadian Green Solutions');
SET @supplier_mx = (SELECT id FROM supplier WHERE name = 'Mexican Sustainable Industries');
SET @supplier_de = (SELECT id FROM supplier WHERE name = 'European Eco Products');
SET @supplier_fr = (SELECT id FROM supplier WHERE name = 'French Green Technologies');
SET @supplier_jp = (SELECT id FROM supplier WHERE name = 'Japan Green Industries');
SET @supplier_cn = (SELECT id FROM supplier WHERE name = 'Chinese Sustainable Manufacturing');
SET @supplier_au = (SELECT id FROM supplier WHERE name = 'Australian Environmental Solutions');

-- Get transport mode IDs
SET @mode_air = (SELECT id FROM transport_mode WHERE mode = 'AIR');
SET @mode_sea = (SELECT id FROM transport_mode WHERE mode = 'SEA');
SET @mode_truck = (SELECT id FROM transport_mode WHERE mode = 'TRUCK');
SET @mode_rail = (SELECT id FROM transport_mode WHERE mode = 'RAIL');

-- Insert global shipments
INSERT INTO shipment (supplier_id, transport_mode_id, origin, destination, distance_km, cargo_weight_tons, shipment_date, calculated_carbon_emission, calculation_timestamp)
SELECT * FROM (
    -- North America shipments
    SELECT @supplier_us AS supplier_id, @mode_truck AS transport_mode_id, 'New York, USA' AS origin, 'Los Angeles, USA' AS destination, 4501.2 AS distance_km, 15.5 AS cargo_weight_tons, '2024-01-15' AS shipment_date, 1365.36 AS calculated_carbon_emission, NOW() AS calculation_timestamp UNION
    SELECT @supplier_ca AS supplier_id, @mode_rail AS transport_mode_id, 'Toronto, Canada' AS origin, 'Vancouver, Canada' AS destination, 3559.4 AS distance_km, 22.8 AS cargo_weight_tons, '2024-01-20' AS shipment_date, 811.26 AS calculated_carbon_emission, NOW() AS calculation_timestamp UNION
    SELECT @supplier_mx AS supplier_id, @mode_truck AS transport_mode_id, 'Mexico City, Mexico' AS origin, 'Guadalajara, Mexico' AS destination, 535.2 AS distance_km, 8.7 AS cargo_weight_tons, '2024-01-22' AS shipment_date, 230.16 AS calculated_carbon_emission, NOW() AS calculation_timestamp UNION
    
    -- Transatlantic shipments
    SELECT @supplier_us AS supplier_id, @mode_sea AS transport_mode_id, 'New York, USA' AS origin, 'Rotterdam, Netherlands' AS destination, 5849.0 AS distance_km, 120.5 AS cargo_weight_tons, '2024-01-10' AS shipment_date, 350.94 AS calculated_carbon_emission, NOW() AS calculation_timestamp UNION
    SELECT @supplier_de AS supplier_id, @mode_air AS transport_mode_id, 'Berlin, Germany' AS origin, 'Chicago, USA' AS destination, 6789.5 AS distance_km, 5.2 AS cargo_weight_tons, '2024-01-18' AS shipment_date, 4269.88 AS calculated_carbon_emission, NOW() AS calculation_timestamp UNION
    
    -- Europe shipments
    SELECT @supplier_de AS supplier_id, @mode_rail AS transport_mode_id, 'Berlin, Germany' AS origin, 'Paris, France' AS destination, 878.6 AS distance_km, 18.3 AS cargo_weight_tons, '2024-01-12' AS shipment_date, 159.95 AS calculated_carbon_emission, NOW() AS calculation_timestamp UNION
    SELECT @supplier_fr AS supplier_id, @mode_truck AS transport_mode_id, 'Paris, France' AS origin, 'Milan, Italy' AS destination, 851.4 AS distance_km, 12.7 AS cargo_weight_tons, '2024-01-14' AS shipment_date, 348.59 AS calculated_carbon_emission, NOW() AS calculation_timestamp UNION
    
    -- Asia shipments
    SELECT @supplier_jp AS supplier_id, @mode_sea AS transport_mode_id, 'Tokyo, Japan' AS origin, 'Shanghai, China' AS destination, 1752.0 AS distance_km, 85.3 AS cargo_weight_tons, '2024-01-08' AS shipment_date, 74.61 AS calculated_carbon_emission, NOW() AS calculation_timestamp UNION
    SELECT @supplier_cn AS supplier_id, @mode_truck AS transport_mode_id, 'Shanghai, China' AS origin, 'Beijing, China' AS destination, 1318.0 AS distance_km, 25.6 AS cargo_weight_tons, '2024-01-16' AS shipment_date, 659.20 AS calculated_carbon_emission, NOW() AS calculation_timestamp UNION
    
    -- Transpacific shipments
    SELECT @supplier_cn AS supplier_id, @mode_sea AS transport_mode_id, 'Shanghai, China' AS origin, 'Los Angeles, USA' AS destination, 11067.0 AS distance_km, 250.7 AS cargo_weight_tons, '2024-01-05' AS shipment_date, 1383.38 AS calculated_carbon_emission, NOW() AS calculation_timestamp UNION
    SELECT @supplier_jp AS supplier_id, @mode_air AS transport_mode_id, 'Tokyo, Japan' AS origin, 'Singapore' AS destination, 5327.0 AS distance_km, 3.8 AS cargo_weight_tons, '2024-01-25' AS shipment_date, 2413.43 AS calculated_carbon_emission, NOW() AS calculation_timestamp UNION
    
    -- Oceania shipments
    SELECT @supplier_au AS supplier_id, @mode_truck AS transport_mode_id, 'Sydney, Australia' AS origin, 'Melbourne, Australia' AS destination, 878.0 AS distance_km, 14.2 AS cargo_weight_tons, '2024-01-19' AS shipment_date, 359.56 AS calculated_carbon_emission, NOW() AS calculation_timestamp UNION
    SELECT @supplier_au AS supplier_id, @mode_sea AS transport_mode_id, 'Melbourne, Australia' AS origin, 'Auckland, New Zealand' AS destination, 2225.0 AS distance_km, 65.8 AS cargo_weight_tons, '2024-01-21' AS shipment_date, 72.39 AS calculated_carbon_emission, NOW() AS calculation_timestamp UNION
    
    -- Intercontinental shipments
    SELECT @supplier_de AS supplier_id, @mode_sea AS transport_mode_id, 'Hamburg, Germany' AS origin, 'Singapore' AS destination, 10350.0 AS distance_km, 185.2 AS cargo_weight_tons, '2024-01-03' AS shipment_date, 964.35 AS calculated_carbon_emission, NOW() AS calculation_timestamp UNION
    SELECT @supplier_us AS supplier_id, @mode_air AS transport_mode_id, 'Los Angeles, USA' AS origin, 'Sydney, Australia' AS destination, 12050.0 AS distance_km, 4.5 AS cargo_weight_tons, '2024-01-23' AS shipment_date, 6507.00 AS calculated_carbon_emission, NOW() AS calculation_timestamp UNION
    
    -- More European shipments
    SELECT @supplier_fr AS supplier_id, @mode_rail AS transport_mode_id, 'Marseille, France' AS origin, 'Barcelona, Spain' AS destination, 313.0 AS distance_km, 9.2 AS cargo_weight_tons, '2024-01-11' AS shipment_date, 28.72 AS calculated_carbon_emission, NOW() AS calculation_timestamp UNION
    SELECT @supplier_de AS supplier_id, @mode_truck AS transport_mode_id, 'Munich, Germany' AS origin, 'Vienna, Austria' AS destination, 443.0 AS distance_km, 11.5 AS cargo_weight_tons, '2024-01-13' AS shipment_date, 177.20 AS calculated_carbon_emission, NOW() AS calculation_timestamp UNION
    
    -- More Asian shipments
    SELECT @supplier_cn AS supplier_id, @mode_sea AS transport_mode_id, 'Guangzhou, China' AS origin, 'Bangkok, Thailand' AS destination, 1439.0 AS distance_km, 72.5 AS cargo_weight_tons, '2024-01-07' AS shipment_date, 50.37 AS calculated_carbon_emission, NOW() AS calculation_timestamp UNION
    SELECT @supplier_jp AS supplier_id, @mode_rail AS transport_mode_id, 'Osaka, Japan' AS origin, 'Tokyo, Japan' AS destination, 406.0 AS distance_km, 16.8 AS cargo_weight_tons, '2024-01-17' AS shipment_date, 68.54 AS calculated_carbon_emission, NOW() AS calculation_timestamp
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM shipment WHERE origin = tmp.origin AND destination = tmp.destination AND supplier_id = tmp.supplier_id);
