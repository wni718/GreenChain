-- Insert data only when the table is empty
INSERT INTO transport_mode (mode, display_name, emission_factor_per_km_per_ton)
SELECT * FROM (SELECT 'AIR', 'Air Freight', 1.2) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM transport_mode WHERE mode = 'AIR');

INSERT INTO transport_mode (mode, display_name, emission_factor_per_km_per_ton)
SELECT * FROM (SELECT 'SEA', 'Sea Freight', 0.05) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM transport_mode WHERE mode = 'SEA');

INSERT INTO transport_mode (mode, display_name, emission_factor_per_km_per_ton)
SELECT * FROM (SELECT 'TRUCK', 'Truck Transport', 0.2) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM transport_mode WHERE mode = 'TRUCK');

INSERT INTO transport_mode (mode, display_name, emission_factor_per_km_per_ton)
SELECT * FROM (SELECT 'RAIL', 'Rail Transport', 0.1) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM transport_mode WHERE mode = 'RAIL');
