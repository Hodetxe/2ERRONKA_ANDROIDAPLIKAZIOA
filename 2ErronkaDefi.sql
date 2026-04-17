-- MySQL dump 10.13  Distrib 8.0.38, for Win64 (x86_64)
--
-- Host: localhost    Database: 2erronka
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `erosketa`
--

DROP TABLE IF EXISTS `erosketa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erosketa` (
  `id` int NOT NULL AUTO_INCREMENT,
  `hornitzailea_id` int NOT NULL,
  `osagaia_id` int DEFAULT NULL,
  `prezioa` double NOT NULL,
  `kantitatea` int NOT NULL,
  `materiala_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `hornitzailea_id_idx` (`hornitzailea_id`),
  KEY `osagaia_id_idx` (`osagaia_id`),
  KEY `materiala_id_idx` (`materiala_id`),
  CONSTRAINT `hornitzailea_id` FOREIGN KEY (`hornitzailea_id`) REFERENCES `hornitzaileak` (`id`),
  CONSTRAINT `materiala_id` FOREIGN KEY (`materiala_id`) REFERENCES `materialak` (`id`),
  CONSTRAINT `osagaia_id` FOREIGN KEY (`osagaia_id`) REFERENCES `osagaiak` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erosketa`
--

LOCK TABLES `erosketa` WRITE;
/*!40000 ALTER TABLE `erosketa` DISABLE KEYS */;
INSERT INTO `erosketa` VALUES (1,1,1,18.5,10,NULL),(2,2,2,9.6,12,NULL),(3,3,6,7.2,6,NULL),(4,1,NULL,48,24,1),(5,2,NULL,36,18,2);
/*!40000 ALTER TABLE `erosketa` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erreserbak`
--

DROP TABLE IF EXISTS `erreserbak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erreserbak` (
  `id` int NOT NULL AUTO_INCREMENT,
  `bezero_izena` varchar(45) NOT NULL,
  `telefonoa` varchar(9) NOT NULL,
  `pertsona_kopurua` int NOT NULL,
  `eguna_ordua` datetime NOT NULL,
  `prezio_totala` double DEFAULT NULL,
  `ordainduta` tinyint NOT NULL,
  `faktura_ruta` varchar(45) DEFAULT NULL,
  `langileak_id` int NOT NULL,
  `mahaiak_id` int NOT NULL,
  PRIMARY KEY (`id`,`langileak_id`,`mahaiak_id`),
  KEY `fk_erreserbak_langileak1_idx` (`langileak_id`),
  KEY `fk_erreserbak_mahaiak1_idx` (`mahaiak_id`),
  KEY `langileak_id` (`langileak_id`),
  KEY `mahaiak_id` (`mahaiak_id`),
  CONSTRAINT `FK_411AD20C` FOREIGN KEY (`langileak_id`) REFERENCES `langileak` (`id`),
  CONSTRAINT `FK_9CEB933F` FOREIGN KEY (`mahaiak_id`) REFERENCES `mahaiak` (`id`),
  CONSTRAINT `fk_erreserbak_langileak1` FOREIGN KEY (`langileak_id`) REFERENCES `langileak` (`id`),
  CONSTRAINT `fk_erreserbak_mahaiak1` FOREIGN KEY (`mahaiak_id`) REFERENCES `mahaiak` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erreserbak`
--

LOCK TABLES `erreserbak` WRITE;
/*!40000 ALTER TABLE `erreserbak` DISABLE KEYS */;
INSERT INTO `erreserbak` VALUES (1,'Iker Gomez','688112233',4,'2026-03-27 14:00:00',58.5,0,NULL,2,2),(2,'Nora Ruiz','699223344',2,'2026-03-27 21:00:00',31,1,'faktura_2.pdf',4,5),(3,'Ane Soto','677445566',6,'2026-03-28 15:30:00',96,0,NULL,2,7);
/*!40000 ALTER TABLE `erreserbak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eskariak`
--

DROP TABLE IF EXISTS `eskariak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eskariak` (
  `id` int NOT NULL AUTO_INCREMENT,
  `prezioa` double NOT NULL,
  `egoera` varchar(45) NOT NULL,
  `erreserbak_id` int NOT NULL,
  `erreserbak_langileak_id` int NOT NULL,
  `erreserbak_mahaiak_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_eskariak_erreserbak1_idx` (`erreserbak_id`,`erreserbak_langileak_id`,`erreserbak_mahaiak_id`),
  KEY `erreserbak_id` (`erreserbak_id`),
  KEY `erreserbak_langileak_id` (`erreserbak_langileak_id`),
  KEY `erreserbak_mahaiak_id` (`erreserbak_mahaiak_id`),
  CONSTRAINT `FK_82A96F40` FOREIGN KEY (`erreserbak_id`) REFERENCES `erreserbak` (`id`),
  CONSTRAINT `FK_8E810A89` FOREIGN KEY (`erreserbak_mahaiak_id`) REFERENCES `mahaiak` (`id`),
  CONSTRAINT `FK_A612A34C` FOREIGN KEY (`erreserbak_langileak_id`) REFERENCES `langileak` (`id`),
  CONSTRAINT `fk_eskariak_erreserbak1` FOREIGN KEY (`erreserbak_id`, `erreserbak_langileak_id`, `erreserbak_mahaiak_id`) REFERENCES `erreserbak` (`id`, `langileak_id`, `mahaiak_id`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eskariak`
--

LOCK TABLES `eskariak` WRITE;
/*!40000 ALTER TABLE `eskariak` DISABLE KEYS */;
INSERT INTO `eskariak` VALUES (1,58.5,'prestatzen',1,2,2),(2,31,'amaituta',2,4,5),(3,96,'zerbitzatuta',3,2,7);
/*!40000 ALTER TABLE `eskariak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eskariak_has_produktuak`
--

DROP TABLE IF EXISTS `eskariak_has_produktuak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eskariak_has_produktuak` (
  `eskariak_id` int NOT NULL,
  `produktuak_id` int NOT NULL,
  `kantitatea` int NOT NULL,
  `prezioa` double NOT NULL,
  PRIMARY KEY (`eskariak_id`,`produktuak_id`),
  KEY `fk_eskariak_has_produktuak_produktuak1_idx` (`produktuak_id`),
  KEY `fk_eskariak_has_produktuak_eskariak1_idx` (`eskariak_id`),
  KEY `eskariak_id` (`eskariak_id`),
  KEY `produktuak_id` (`produktuak_id`),
  CONSTRAINT `FK_496A0619` FOREIGN KEY (`produktuak_id`) REFERENCES `produktuak` (`id`),
  CONSTRAINT `FK_E9A6D6AA` FOREIGN KEY (`eskariak_id`) REFERENCES `eskariak` (`id`),
  CONSTRAINT `fk_eskariak_has_produktuak_eskariak1` FOREIGN KEY (`eskariak_id`) REFERENCES `eskariak` (`id`),
  CONSTRAINT `fk_eskariak_has_produktuak_produktuak1` FOREIGN KEY (`produktuak_id`) REFERENCES `produktuak` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eskariak_has_produktuak`
--

LOCK TABLES `eskariak_has_produktuak` WRITE;
/*!40000 ALTER TABLE `eskariak_has_produktuak` DISABLE KEYS */;
INSERT INTO `eskariak_has_produktuak` VALUES (1,1,2,24),(1,5,3,4.5),(1,6,3,6),(2,2,1,11.5),(2,7,1,6.5),(2,8,1,13),(3,3,2,25),(3,4,2,17),(3,5,4,6),(3,6,4,8);
/*!40000 ALTER TABLE `eskariak_has_produktuak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hornitzaileak`
--

DROP TABLE IF EXISTS `hornitzaileak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hornitzaileak` (
  `id` int NOT NULL AUTO_INCREMENT,
  `izena` varchar(45) NOT NULL,
  `kontaktua` varchar(50) NOT NULL,
  `helbidea` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hornitzaileak`
--

LOCK TABLES `hornitzaileak` WRITE;
/*!40000 ALTER TABLE `hornitzaileak` DISABLE KEYS */;
INSERT INTO `hornitzaileak` VALUES (1,'Baserri Fresh','944000111','Bilbo, Zorrotzaurre 12'),(2,'Edari Banaketa','943111222','Donostia, Portua 8'),(3,'Ipar Haragia','945222333','Gasteiz, Industrialdea 4');
/*!40000 ALTER TABLE `hornitzaileak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `langileak`
--

DROP TABLE IF EXISTS `langileak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `langileak` (
  `id` int NOT NULL AUTO_INCREMENT,
  `izena` varchar(20) NOT NULL,
  `abizena` varchar(45) NOT NULL,
  `erabiltzaile_izena` varchar(20) NOT NULL,
  `langile_kodea` int NOT NULL,
  `pasahitza` longtext NOT NULL,
  `rola_id` int NOT NULL,
  `ezabatua` tinyint(1) NOT NULL DEFAULT '0',
  `chat` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_langileak_rola` (`rola_id`),
  CONSTRAINT `fk_langileak_rola` FOREIGN KEY (`rola_id`) REFERENCES `rolak` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `langileak`
--

LOCK TABLES `langileak` WRITE;
/*!40000 ALTER TABLE `langileak` DISABLE KEYS */;
INSERT INTO `langileak` VALUES (1,'Aitor','Auzmend','Admin',1,'123',1,0,0),(2,'Leire','Agirre','leire',1002,'1234',2,0,1),(3,'Unai','Mendizabal','unai',1003,'1234',3,0,0),(4,'June','Ibarra','june',1004,'1234',2,0,1),(5,'Mikel','Otaegi','mikel',1005,'1234',3,0,0);
/*!40000 ALTER TABLE `langileak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mahaiak`
--

DROP TABLE IF EXISTS `mahaiak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mahaiak` (
  `id` int NOT NULL AUTO_INCREMENT,
  `zenbakia` int NOT NULL,
  `pertsona_kopurua` int NOT NULL,
  `kokapena` varchar(25) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mahaiak`
--

LOCK TABLES `mahaiak` WRITE;
/*!40000 ALTER TABLE `mahaiak` DISABLE KEYS */;
INSERT INTO `mahaiak` VALUES (1,1,2,'barra'),(2,2,4,'interior'),(3,3,4,'interior'),(4,4,6,'terraza'),(5,5,2,'terraza'),(6,6,8,'interior'),(7,7,6,'terraza'),(8,8,10,'ekitaldi');
/*!40000 ALTER TABLE `mahaiak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `materialak`
--

DROP TABLE IF EXISTS `materialak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `materialak` (
  `id` int NOT NULL AUTO_INCREMENT,
  `izena` varchar(40) NOT NULL,
  `prezioa` double NOT NULL,
  `stock` int NOT NULL,
  `hornitzaileak_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_materialak_hornitzaileak1_idx` (`hornitzaileak_id`),
  CONSTRAINT `fk_materialak_hornitzaileak1` FOREIGN KEY (`hornitzaileak_id`) REFERENCES `hornitzaileak` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `materialak`
--

LOCK TABLES `materialak` WRITE;
/*!40000 ALTER TABLE `materialak` DISABLE KEYS */;
INSERT INTO `materialak` VALUES (1,'Kutxak',2,120,1),(2,'Ezpainzapiak',1.5,200,2),(3,'Plater txikiak',3.2,80,1);
/*!40000 ALTER TABLE `materialak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `osagaiak`
--

DROP TABLE IF EXISTS `osagaiak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `osagaiak` (
  `id` int NOT NULL AUTO_INCREMENT,
  `izena` varchar(20) NOT NULL,
  `prezioa` double NOT NULL,
  `stock` int NOT NULL,
  `hornitzaileak_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_osagaiak_hornitzaileak1_idx` (`hornitzaileak_id`),
  CONSTRAINT `fk_osagaiak_hornitzaileak1` FOREIGN KEY (`hornitzaileak_id`) REFERENCES `hornitzaileak` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=216 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `osagaiak`
--

LOCK TABLES `osagaiak` WRITE;
/*!40000 ALTER TABLE `osagaiak` DISABLE KEYS */;
INSERT INTO `osagaiak` VALUES (1,'Ogia',0.45,80,1),(2,'Tomatea',0.3,60,1),(3,'Urdaiazpikoa',1.1,35,3),(4,'Gazta',0.8,40,3),(5,'Letxuga',0.25,30,1),(6,'Oilaskoa',1.6,25,3),(7,'Kafe aleak',0.2,120,2),(8,'Ura',0.15,200,2),(9,'Pasta orea',0.9,28,1),(10,'Arrautza',0.22,90,1),(11,'Patatak',0.35,75,1),(12,'Hanburgesa haragia',1.9,22,3);
/*!40000 ALTER TABLE `osagaiak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `produktuak`
--

DROP TABLE IF EXISTS `produktuak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `produktuak` (
  `id` int NOT NULL AUTO_INCREMENT,
  `izena` varchar(60) NOT NULL,
  `prezioa` double NOT NULL,
  `mota` varchar(20) NOT NULL,
  `stock` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `produktuak`
--

LOCK TABLES `produktuak` WRITE;
/*!40000 ALTER TABLE `produktuak` DISABLE KEYS */;
INSERT INTO `produktuak` VALUES (1,'Hanburgesa klasikoa',12,'nagusia',18),(2,'Caesar entsalada',11.5,'hasierakoa',14),(3,'Pizza margarita',12.5,'nagusia',10),(4,'Kroketak',8.5,'anoa',24),(5,'Kafea',1.5,'edaria',90),(6,'Ura 50cl',2,'edaria',120),(7,'Sandwich mistoa',6.5,'mokadua',20),(8,'Txuleta',13,'nagusia',12);
/*!40000 ALTER TABLE `produktuak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `produktuak_has_osagaiak`
--

DROP TABLE IF EXISTS `produktuak_has_osagaiak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `produktuak_has_osagaiak` (
  `produktuak_id` int NOT NULL,
  `osagaiak_id` int NOT NULL,
  `kantitatea` int NOT NULL,
  PRIMARY KEY (`produktuak_id`,`osagaiak_id`),
  KEY `fk_produktuak_has_osagaiak_osagaiak1_idx` (`osagaiak_id`),
  KEY `fk_produktuak_has_osagaiak_produktuak1_idx` (`produktuak_id`),
  KEY `produktuak_id` (`produktuak_id`),
  KEY `osagaiak_id` (`osagaiak_id`),
  CONSTRAINT `FK_200FAAF` FOREIGN KEY (`osagaiak_id`) REFERENCES `osagaiak` (`id`),
  CONSTRAINT `FK_980955FB` FOREIGN KEY (`produktuak_id`) REFERENCES `produktuak` (`id`),
  CONSTRAINT `fk_produktuak_has_osagaiak_osagaiak1` FOREIGN KEY (`osagaiak_id`) REFERENCES `osagaiak` (`id`),
  CONSTRAINT `fk_produktuak_has_osagaiak_produktuak1` FOREIGN KEY (`produktuak_id`) REFERENCES `produktuak` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `produktuak_has_osagaiak`
--

LOCK TABLES `produktuak_has_osagaiak` WRITE;
/*!40000 ALTER TABLE `produktuak_has_osagaiak` DISABLE KEYS */;
INSERT INTO `produktuak_has_osagaiak` VALUES (1,1,1),(1,2,1),(1,4,1),(1,12,1),(2,2,1),(2,4,1),(2,5,1),(2,6,1),(3,2,2),(3,4,2),(3,9,1),(4,4,1),(4,10,2),(5,7,1),(6,8,1),(7,1,2),(7,3,1),(7,4,1),(8,11,2),(8,12,1);
/*!40000 ALTER TABLE `produktuak_has_osagaiak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rolak`
--

DROP TABLE IF EXISTS `rolak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rolak` (
  `id` int NOT NULL AUTO_INCREMENT,
  `izena` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_rolak_izena` (`izena`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rolak`
--

LOCK TABLES `rolak` WRITE;
/*!40000 ALTER TABLE `rolak` DISABLE KEYS */;
INSERT INTO `rolak` VALUES (1,'administratzailea'),(3,'sukaldaria'),(2,'zerbitzaria');
/*!40000 ALTER TABLE `rolak` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-13 13:34:13
