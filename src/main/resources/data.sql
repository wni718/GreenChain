-- 如果表为空才插入数据
INSERT INTO transport_mode (mode, display_name, emission_factor_per_km_per_ton)
SELECT * FROM (SELECT 'AIR', '空运', 1.2) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM transport_mode WHERE mode = 'AIR');

INSERT INTO transport_mode (mode, display_name, emission_factor_per_km_per_ton)
SELECT * FROM (SELECT 'SEA', '海运', 0.05) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM transport_mode WHERE mode = 'SEA');

INSERT INTO transport_mode (mode, display_name, emission_factor_per_km_per_ton)
SELECT * FROM (SELECT 'TRUCK', '卡车运输', 0.2) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM transport_mode WHERE mode = 'TRUCK');

INSERT INTO transport_mode (mode, display_name, emission_factor_per_km_per_ton)
SELECT * FROM (SELECT 'RAIL', '铁路运输', 0.1) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM transport_mode WHERE mode = 'RAIL');
