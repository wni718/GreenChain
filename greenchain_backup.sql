-- MySQL dump 10.13  Distrib 9.2.0, for Win64 (x86_64)
--
-- Host: localhost    Database: greenchain
-- ------------------------------------------------------
-- Server version	9.2.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `shipment`
--

DROP TABLE IF EXISTS `shipment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shipment` (
  `calculated_carbon_emission` double DEFAULT NULL,
  `cargo_weight_tons` double DEFAULT NULL,
  `distance_km` double DEFAULT NULL,
  `shipment_date` date DEFAULT NULL,
  `calculation_timestamp` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `supplier_id` bigint DEFAULT NULL,
  `transport_mode_id` bigint DEFAULT NULL,
  `destination` varchar(255) DEFAULT NULL,
  `origin` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9g2a3xvkou96gf3wfwj5flhka` (`supplier_id`),
  KEY `FKo6ghvsejrlomp5r0opy351adu` (`transport_mode_id`),
  CONSTRAINT `FK9g2a3xvkou96gf3wfwj5flhka` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`),
  CONSTRAINT `FKo6ghvsejrlomp5r0opy351adu` FOREIGN KEY (`transport_mode_id`) REFERENCES `transport_mode` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipment`
--

LOCK TABLES `shipment` WRITE;
/*!40000 ALTER TABLE `shipment` DISABLE KEYS */;
INSERT INTO `shipment` VALUES (1365.36,15.5,4501.2,'2024-01-15','2026-04-17 17:00:37.000000',1,1,3,'Los Angeles, USA','New York, USA'),(8115.432000000002,22.8,3559.4,'2024-02-20','2026-04-17 10:12:25.046480',2,2,4,'Vancouver, Canada','Toronto, Canada'),(931.248,8.7,535.2,'2024-03-22','2026-04-17 10:12:28.836885',3,3,3,'Guadalajara, Mexico','Mexico City, Mexico'),(35240.225,120.5,5849,'2024-04-10','2026-04-17 10:12:33.267258',4,1,2,'Rotterdam, Netherlands','New York, USA'),(43181.219999999994,5.3,6789.5,'2024-05-18','2026-04-17 10:23:56.455173',5,4,1,'Chicago, USA','Berlin, Germany'),(1607.8380000000004,18.3,878.6,'2024-06-12','2026-04-17 10:12:40.645042',6,4,4,'Paris, France','Berlin, Germany'),(2162.556,12.7,851.4,'2024-07-14','2026-04-17 10:12:46.951960',7,5,3,'Milan, Italy','Paris, France'),(7472.280000000001,85.3,1752,'2024-08-08','2026-04-17 10:12:51.391194',8,8,2,'Shanghai, China','Tokyo, Japan'),(6748.160000000001,25.6,1318,'2024-09-16','2026-04-17 10:12:55.694071',9,9,3,'Beijing, China','Shanghai, China'),(138724.845,250.7,11067,'2024-10-05','2026-04-17 10:12:59.766243',10,9,2,'Los Angeles, USA','Shanghai, China'),(24291.12,3.8,5327,'2024-11-25','2026-04-17 10:13:06.563682',11,8,1,'Singapore','Tokyo, Japan'),(2493.52,14.2,878,'2024-12-19','2026-04-17 10:13:12.380311',12,12,3,'Melbourne, Australia','Sydney, Australia'),(7320.25,65.8,2225,'2025-01-21','2026-04-17 10:13:27.140826',13,12,2,'Auckland, New Zealand','Melbourne, Australia'),(95841,185.2,10350,'2025-02-03','2026-04-17 10:13:37.711073',14,4,2,'Singapore','Hamburg, Germany'),(65070,4.5,12050,'2025-03-23','2026-04-17 10:13:45.961371',15,1,1,'Sydney, Australia','Los Angeles, USA'),(287.96,9.2,313,'2025-04-11','2026-04-17 10:13:54.286436',16,5,4,'Barcelona, Spain','Marseille, France'),(1018.9000000000001,11.5,443,'2025-05-13','2026-04-17 10:14:00.993925',17,4,3,'Vienna, Austria','Munich, Germany'),(5216.375,72.5,1439,'2025-06-07','2026-04-17 10:14:08.120917',18,9,2,'Bangkok, Thailand','Guangzhou, China'),(68.54,16.8,406,'2025-08-17','2026-04-17 17:00:37.000000',19,8,4,'Tokyo, Japan','Osaka, Japan'),(455,18.2,1478,'2025-09-15','2026-04-17 17:00:37.000000',20,1,3,'Dallas, USA','Chicago, USA'),(896.56,2.8,1482,'2025-10-20','2026-04-17 17:00:37.000000',21,4,1,'Madrid, Spain','Frankfurt, Germany'),(157.5,150,3752,'2025-11-10','2026-04-17 17:00:37.000000',22,9,2,'Singapore','Shanghai, China'),(4851.000000000001,22.5,4312,'2025-12-22','2026-04-17 10:32:45.326502',23,12,2,'Perth, Australia','Brisbane, Australia'),(47.43,15.3,473,'2026-01-18','2026-04-17 17:00:37.000000',24,5,4,'Zurich, Switzerland','Lyon, France'),(576.9,1.9,945,'2026-02-28','2026-04-17 17:00:37.000000',25,8,1,'Seoul, South Korea','Tokyo, Japan'),(955.65,180.5,8230,'2026-03-05','2026-04-17 17:00:37.000000',26,1,2,'Rotterdam, Netherlands','Long Beach, USA'),(234,10.2,234,'2026-03-14','2026-04-17 17:00:37.000000',27,2,3,'Seattle, USA','Vancouver, Canada'),(70.4,12.8,575,'2026-03-20','2026-04-17 17:00:37.000000',28,4,4,'Warsaw, Poland','Berlin, Germany'),(2358.96,3.2,6497,'2026-04-01','2026-04-17 17:00:37.000000',29,9,1,'Dubai, UAE','Beijing, China'),(475,95,7800,'2026-04-10','2026-04-17 17:00:37.000000',30,12,2,'Tokyo, Japan','Sydney, Australia'),(82.65,8.7,304,'2026-04-16','2026-04-17 17:00:37.000000',31,5,3,'Brussels, Belgium','Paris, France'),(16837.152000000002,165.72,2032,'2026-04-19','2026-04-17 09:08:09.839769',32,10,2,'Dubai, UAE','Mumbai,  India'),(27682.8,23,1003,'2026-04-01','2026-04-17 11:11:04.166243',34,14,1,'Buenos Aires, Argentina','Sao Paulo, Brazil'),(16968,10,1414,'2026-04-17','2026-04-17 09:47:54.865407',35,10,1,'Delhi, India','Mumbai, India'),(15300,102,3000,'2025-07-18','2026-04-17 10:09:11.935796',36,17,2,'Xian, China','Fes, Morocco'),(12826.500000000002,63.75,2012,'2026-04-17','2026-04-17 10:32:28.930565',38,7,4,'Fes, Morocco','Stockholm, Sweden'),(23907,156,3065,'2026-04-17','2026-04-17 10:37:31.749812',39,32,2,'Seoul, South Korea','Manchester, UK'),(4677.183,45.99,2034,'2026-04-17','2026-04-17 10:46:25.946663',40,8,2,'Lima, Peru','Tokyo, Japan'),(34679.232,200.69,3456,'2026-04-17','2026-04-17 10:49:28.537347',41,15,2,'Saint Petersburg, Russia','Concepcion, Chile'),(6512.099999999999,88.6,735,'2026-04-17','2026-04-17 12:11:38.967030',42,7,4,'Copenhagen, Denmark','Gothenburg, Sweden');
/*!40000 ALTER TABLE `shipment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `supplier`
--

DROP TABLE IF EXISTS `supplier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier` (
  `emission_factor_per_unit` double DEFAULT NULL,
  `has_environmental_certification` bit(1) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `contact_email` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_afxol104prrysgdv6xe8nmt56` (`user_id`),
  CONSTRAINT `FKs1dd5csqciyb73tm0vep2slsy` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplier`
--

LOCK TABLES `supplier` WRITE;
/*!40000 ALTER TABLE `supplier` DISABLE KEYS */;
INSERT INTO `supplier` VALUES (0.15,_binary '',1,3,'contact@ecomaterials.com','United States','EcoMaterials Inc.'),(0.12,_binary '',2,4,'info@canadiangreen.ca','Canada','Canadian Green Solutions'),(0.25,_binary '\0',3,5,'sustainability@mexind.com','Mexico','Mexican Sustainable Industries'),(0.1,_binary '',4,6,'info@europeaneco.de','Germany','European Eco Products'),(0.13,_binary '',5,7,'contact@frenchtech.fr','France','French Green Technologies'),(0.18,_binary '\0',6,8,'sustainability@italianmaterials.it','Italy','Italian Sustainable Materials'),(0.08,_binary '',7,9,'info@swedishenviro.se','Sweden','Swedish Environmental Solutions'),(0.12,_binary '',8,10,'contact@japangreen.co.jp','Japan','Japan Green Industries'),(0.22,_binary '\0',9,11,'sustainability@chinasustainable.cn','China','Chinese Sustainable Manufacturing'),(0.26,_binary '',10,12,'info@indianeco.in','India','Indian Eco Products'),(0.14,_binary '',11,13,'contact@koreangreentech.kr','South Korea','South Korean Green Tech'),(0.16,_binary '',12,14,'info@australianenv.com.au','Australia','Australian Environmental Solutions'),(0.09,_binary '',13,15,'contact@nzsustainable.co.nz','New Zealand','New Zealand Sustainable Industries'),(0.23,_binary '',14,16,'sustainability@braziliangreen.com.br','Brazil','Brazilian Green Materials'),(0.17,_binary '',15,17,'info@chilesustainable.cl','Chile','Chilean Sustainable Resources'),(0.3,_binary '\0',16,18,'contact@southafricaneco.co.za','South Africa','South African Eco Industries'),(0.24,_binary '',17,19,'info@moroccangreen.ma','Morocco','Moroccan Green Solutions'),(0.08,_binary '',32,NULL,'info@unitedkingdom.ecg','UK','ECG Tech for Green');
/*!40000 ALTER TABLE `supplier` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transport_mode`
--

DROP TABLE IF EXISTS `transport_mode`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transport_mode` (
  `emission_factor_per_km_per_ton` double DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `display_name` varchar(255) DEFAULT NULL,
  `mode` enum('AIR','SEA','TRUCK','RAIL') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transport_mode`
--

LOCK TABLES `transport_mode` WRITE;
/*!40000 ALTER TABLE `transport_mode` DISABLE KEYS */;
INSERT INTO `transport_mode` VALUES (1.2,1,'Air Freight','AIR'),(0.05,2,'Sea Freight','SEA'),(0.2,3,'Truck Transport','TRUCK'),(0.1,4,'Rail Transport','RAIL');
/*!40000 ALTER TABLE `transport_mode` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `enabled` bit(1) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_name` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `role` enum('ADMIN','SUSTAINABILITY_MANAGER','SUPPLIER','VIEWER') DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UK_r43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (_binary '',1,'GreenChain Admin','admin@greenchains.com','$2a$10$WoRO45rx0aBhzznBmTc3Euh2KdU/Sl.AvlWJNwPCnvyQYP0pqegJ.','admin','ADMIN'),(_binary '',2,'Sustainability Team','manager@greenchains.com','$2a$10$E3QnX2NfG9a8c4vB5d6eFgHhIjKlMnOpQrStUvWxYzAbCdEfGhIiJ','manager','SUSTAINABILITY_MANAGER'),(_binary '',3,'EcoMaterials Inc.','contact@ecomaterials.com','$2a$10$LrefusC.h2cepI2N086X3OU3hSobXfVZ4vRaO6BqNwleD/xjwKeV6','ecomaterials_inc._supplier','SUPPLIER'),(_binary '',4,'Canadian Green Solutions','info@canadiangreen.ca','$2a$10$dGJpCjlMgiJeTwMiyiJx7uQWTQwyFdcLqC1upJ6xIjuEFX4pzY8a2','canadian_green_solutions_supplier','SUPPLIER'),(_binary '',5,'Mexican Sustainable Industries','sustainability@mexind.com','$2a$10$33OUdnGtXiPCahwN8tjrluvmgZWAVJaA4JO4PUdDDN85/cP10l5/q','mexican_sustainable_industries_supplier','SUPPLIER'),(_binary '',6,'European Eco Products','info@europeaneco.de','$2a$10$QsfcVSlvaR3QpGuACxmn.uHol0B6odrUgIwBYrot4HcbwMvQNOdSG','european_eco_products_supplier','SUPPLIER'),(_binary '',7,'French Green Technologies','contact@frenchtech.fr','$2a$10$RQah8d6r5x3eIGUjdrAsRuaocBmvatQguEU57MxL86pIglxYzoMui','french_green_technologies_supplier','SUPPLIER'),(_binary '',8,'Italian Sustainable Materials','sustainability@italianmaterials.it','$2a$10$uqw35KX7Svr8HJM0MbkdVuPH6TkZrkHtx9SkGzi4s7Ove6PrK.Oyq','italian_sustainable_materials_supplier','SUPPLIER'),(_binary '',9,'Swedish Environmental Solutions','info@swedishenviro.se','$2a$10$2IrDVzaVWeLSWdQB21h0LuXjyoCWqImRKGYfIh6pc/uzlMSgRGJgK','swedish_environmental_solutions_supplier','SUPPLIER'),(_binary '',10,'Japan Green Industries','contact@japangreen.co.jp','$2a$10$J7iC5x8sluPn.FoT3SqUNOq5Z4tVmFBHsfuawORemnBga8BhzpdrC','japan_green_industries_supplier','SUPPLIER'),(_binary '',11,'Chinese Sustainable Manufacturing','sustainability@chinasustainable.cn','$2a$10$gBCY.mjl4mBFReUHxbdhqOCMmYTycOHnDCmotU.7qrTSAxJNoHRlO','chinese_sustainable_manufacturing_supplier','SUPPLIER'),(_binary '',12,'Indian Eco Products','info@indianeco.in','$2a$10$HZpj2VRHA0WZ5BKhhyrezeXMKt/Kj7A5dmM0x0WvqzRyFifYwUjI.','indian_eco_products_supplier','SUPPLIER'),(_binary '',13,'South Korean Green Tech','contact@koreangreentech.kr','$2a$10$JsQSLrNyJf3l6VXHGrvx.uEvBk0YdlUEaRi3W.dvdqK6Z1CwGSxz2','south_korean_green_tech_supplier','SUPPLIER'),(_binary '',14,'Australian Environmental Solutions','info@australianenv.com.au','$2a$10$Sep0Cvw7y.vM2U89KJbbBetPnJWlftWHX5YyWOUZSwmrQk5zEBY7e','australian_environmental_solutions_supplier','SUPPLIER'),(_binary '',15,'New Zealand Sustainable Industries','contact@nzsustainable.co.nz','$2a$10$xYyetlR2Idwh1badCMjPY.60i.CRl7H1dEAuALZgz.nSwzv10BT4i','new_zealand_sustainable_industries_supplier','SUPPLIER'),(_binary '',16,'Brazilian Green Materials','sustainability@braziliangreen.com.br','$2a$10$u566aFQClX7aCtd6fk.bFOOGEdJfYXWCz5RAKpTZcMXoBRmdiosXO','brazilian_green_materials_supplier','SUPPLIER'),(_binary '',17,'Chilean Sustainable Resources','info@chilesustainable.cl','$2a$10$UVCPu7R1oaDyUA3hdn3Y2uwjtfVVPzF9ta5.uDIj4rUB6OEVk5COO','chilean_sustainable_resources_supplier','SUPPLIER'),(_binary '',18,'South African Eco Industries','contact@southafricaneco.co.za','$2a$10$ymXjClGsX2Le2AVkYGIYsuxj44NOOIkS7waclJ4a.JZNTJ6Ly1euG','south_african_eco_industries_supplier','SUPPLIER'),(_binary '',19,'Moroccan Green Solutions','info@moroccangreen.ma','$2a$10$BspLSMn4h2E.V9yChLro.ewHbDFY.UKWErhyVMiMpnqEID6JefK9W','moroccan_green_solutions_supplier','SUPPLIER');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-20 22:05:51
