-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: i13e201.p.ssafy.io    Database: business_card_zip
-- ------------------------------------------------------
-- Server version	8.0.43-0ubuntu0.22.04.1

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
-- Table structure for table `company_histories`
--

DROP TABLE IF EXISTS `company_histories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `company_histories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `changed_at` datetime(6) NOT NULL,
  `company` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `is_confirmed` bit(1) DEFAULT NULL,
  `card_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKah6rn3gonngebvdhth7n4xpkp` (`card_id`),
  CONSTRAINT `FKah6rn3gonngebvdhth7n4xpkp` FOREIGN KEY (`card_id`) REFERENCES `digital_cards` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=203 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `company_histories`
--

LOCK TABLES `company_histories` WRITE;
/*!40000 ALTER TABLE `company_histories` DISABLE KEYS */;
INSERT INTO `company_histories` VALUES (1,'2025-08-13 05:30:23.974714','네이버',_binary '\0',1),(19,'2025-08-13 14:58:27.152405','SSAFY',_binary '\0',19),(20,'2025-08-13 15:03:27.973970','SSAFY',_binary '\0',20),(21,'2025-08-13 15:15:08.219784','싸피',_binary '\0',21),(22,'2025-08-13 15:22:31.003862','싸피',_binary '\0',22),(23,'2025-08-13 15:27:26.805539','싸피',_binary '\0',23),(24,'2025-08-13 15:34:13.416026','싸피',_binary '\0',24),(27,'2025-08-13 15:42:35.060284','싸피',_binary '\0',27),(28,'2025-08-13 15:47:26.588572','싸피',_binary '\0',28),(29,'2025-08-13 15:59:00.694566','싸피',_binary '\0',29),(30,'2025-08-13 16:04:47.918832','싸피',_binary '\0',30),(32,'2025-08-13 16:16:10.101066','싸피',_binary '\0',32),(35,'2025-08-13 16:40:28.240394','싸피',_binary '\0',35),(36,'2025-08-13 16:41:10.251441','싸피',_binary '\0',36),(39,'2025-08-13 16:59:39.909321','싸피',_binary '\0',39),(46,'2025-08-13 23:59:33.545525','SSAFY',_binary '\0',46),(47,'2025-08-14 00:23:12.163169','싸피',_binary '\0',47),(52,'2025-08-14 01:02:10.994933','ㅎ너노',_binary '\0',52),(55,'2025-08-14 01:30:48.341624','싸피',_binary '\0',55),(57,'2025-08-14 01:34:11.427270','ㅎㅎ',_binary '\0',57),(60,'2025-08-14 01:56:06.128747','SSAFY',_binary '\0',60),(63,'2025-08-14 02:21:40.610997','싸피',_binary '\0',63),(85,'2025-08-14 06:06:02.622947','네이버',_binary '',85),(99,'2025-08-15 08:43:26.385374','떡잎물산',_binary '\0',99),(100,'2025-08-15 09:13:57.806221','떡잎물산',_binary '\0',100),(101,'2025-08-15 09:13:57.863364','떡잎물산',_binary '\0',101),(110,'2025-08-15 13:38:09.859098','싸피',_binary '\0',110),(111,'2025-08-15 13:51:23.001294','싸피',_binary '\0',111),(112,'2025-08-15 13:54:23.614643','33',_binary '\0',112),(117,'2025-08-16 10:02:23.377203','22',_binary '\0',117),(118,'2025-08-16 10:02:45.664591','33',_binary '\0',118),(137,'2025-08-17 08:11:10.923975','싸피',_binary '\0',143),(148,'2025-08-17 14:59:58.458524','ㅎㅎ',_binary '\0',154),(149,'2025-08-17 15:30:45.950808','4',_binary '\0',155),(154,'2025-08-17 16:34:37.986695','33ㅅ',_binary '\0',112),(157,'2025-08-17 17:19:26.013060','2',_binary '\0',162),(158,'2025-08-17 17:22:32.222461','88',_binary '\0',163),(159,'2025-08-17 17:24:33.576328','66',_binary '\0',164),(177,'2025-08-17 23:22:05.277761','야여렬',_binary '\0',182),(178,'2025-08-17 23:59:54.884649','여랴',_binary '\0',183),(179,'2025-08-18 00:03:13.211148','ㅑㅅ아ㅛㅇ',_binary '\0',184),(180,'2025-08-18 00:06:12.861359','ㅑㅅ아ㅛㅇ',_binary '\0',185),(181,'2025-08-18 00:11:31.649037','야야',_binary '\0',186),(183,'2025-08-18 00:25:49.038298','ㅁㅇㄴㄹㅁㄴㅇ',_binary '\0',188),(184,'2025-08-18 00:26:49.879933','ㅁㅇㄴㄹㅁㄴㅇ',_binary '\0',189),(192,'2025-08-18 00:53:59.668026','asdfa',_binary '\0',197),(193,'2025-08-18 00:59:25.214915','싸피',_binary '\0',198),(200,'2025-08-18 01:11:14.747988','ddzxcv',_binary '\0',205),(202,'2025-08-18 01:46:35.983952','싸피',_binary '\0',207);
/*!40000 ALTER TABLE `company_histories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `company_verifications`
--

DROP TABLE IF EXISTS `company_verifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `company_verifications` (
  `id` int NOT NULL AUTO_INCREMENT,
  `card_id` int NOT NULL,
  `code` varchar(6) COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_qgkirtso4sr0wu30mqgi4xc9b` (`card_id`),
  UNIQUE KEY `UKqgkirtso4sr0wu30mqgi4xc9b` (`card_id`),
  CONSTRAINT `FKdmtwtuxl4yudwbyaeitfxsq2a` FOREIGN KEY (`card_id`) REFERENCES `digital_cards` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `company_verifications`
--

LOCK TABLES `company_verifications` WRITE;
/*!40000 ALTER TABLE `company_verifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `company_verifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `digital_card_fields`
--

DROP TABLE IF EXISTS `digital_card_fields`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `digital_card_fields` (
  `id` int NOT NULL AUTO_INCREMENT,
  `field_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `field_order` int NOT NULL,
  `field_value` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `card_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk7fpc04y4dxqn9aw1hivpfl3k` (`card_id`),
  CONSTRAINT `FKk7fpc04y4dxqn9aw1hivpfl3k` FOREIGN KEY (`card_id`) REFERENCES `digital_cards` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=120 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `digital_card_fields`
--

LOCK TABLES `digital_card_fields` WRITE;
/*!40000 ALTER TABLE `digital_card_fields` DISABLE KEYS */;
INSERT INTO `digital_card_fields` VALUES (1,'깃허브',1,'ss깃허브url',1),(2,'이메일',2,'ssmj.kim@ssafy.com',1),(16,'부서',1,'E201',19),(17,'역할',2,'백엔드, 인프라',19),(18,'부서',1,'모바일1등팀',20),(19,'역할',2,'백엔드, 인프라',20),(22,'부서',1,'모바일1등팀',60),(40,'INSTA',1,'ㅇㄹㅁㄴㅇ',99),(41,'INSTA',1,'ㅇㄹㅁㄴㅇ',100),(42,'INSTA',1,'ㅇㄹㅁㄴㅇ',101),(79,'55',1,'55',154),(99,'어애덩',1,'터냐거고',182),(100,'러대엉',2,'오애ㅕ올',182),(101,'ㅑㄹ탓',1,'ㅑㅅ카ㅛㅇ',184),(102,'ㅑㅅ캿ㅇ',2,'ㅓㄹ캇탓ㅌ',184),(103,'ㅑㄹ탓',1,'ㅑㅅ카ㅛㅇ',185),(104,'ㅑㅅ캿ㅇ',2,'ㅓㄹ캇탓ㅌ',185),(105,'토ㅓㅇ',0,'오ㅗㅇ',186),(106,'토영',0,'터야',186),(109,'ㅁㅇㄹ',0,'ㅁㄴㅇㄹㅁㄴ',189),(115,'asdfa',0,'asdfasd',197),(116,'mbti',0,'isfj',198),(117,'zxcv',0,'zcv',205),(118,'zcv',0,'zcvcz',205),(119,'mbti',0,'isfj',207);
/*!40000 ALTER TABLE `digital_card_fields` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `digital_cards`
--

DROP TABLE IF EXISTS `digital_cards`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `digital_cards` (
  `id` int NOT NULL AUTO_INCREMENT,
  `background_image_num` int DEFAULT NULL,
  `company` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `is_confirmed` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `custom_image_url` varchar(2048) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `is_digital` bit(1) NOT NULL,
  `email` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `font_color` bit(1) DEFAULT NULL,
  `image_url_horizontal` varchar(2048) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `image_url_vertical` varchar(2048) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `position` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `qr_code_url` varchar(2048) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `share_token` varchar(36) COLLATE utf8mb4_general_ci NOT NULL,
  `share_url` varchar(2048) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_o90wflivnug7l0tm8uvwgbqom` (`share_token`),
  KEY `FK4j1q20in6bnekta80j839x9h2` (`user_id`),
  CONSTRAINT `FK4j1q20in6bnekta80j839x9h2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=208 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `digital_cards`
--

LOCK TABLES `digital_cards` WRITE;
/*!40000 ALTER TABLE `digital_cards` DISABLE KEYS */;
INSERT INTO `digital_cards` VALUES (1,NULL,'네이버',_binary '\0','2025-08-13 05:30:23.961544',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/fc0c60f4-57c4-4a35-9011-3ebadc9eb813_20250813164817.png','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/ebf8eadc-a5bf-446c-9105-97322bb6c53e_20250813164817.png','윤설아','01031198268',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/3c97cc8d-7a1e-4bc1-820c-f6e03a1d0616_20250813053023.png','2fd8c751-c4c7-43ba-a4c6-df89b55b0010','https://i13e201.p.ssafy.io/api/cards/share/2fd8c751-c4c7-43ba-a4c6-df89b55b0010','2025-08-13 05:30:23.961582',1),(19,1,'SSAFY',_binary '\0','2025-08-13 14:58:27.146141',NULL,_binary '','dus1094@naver.com',_binary '',NULL,NULL,'염아연','01024298065','교육생','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/304ae686-4014-456b-a5f5-3c852f8f4fd7_20250813145827.png','24cc4837-8202-4c87-9eaa-a945aa412aad','https://i13e201.p.ssafy.io/api/cards/share/24cc4837-8202-4c87-9eaa-a945aa412aad','2025-08-13 14:58:27.146170',1),(20,1,'SSAFY',_binary '\0','2025-08-13 15:03:27.968776',NULL,_binary '','dus1094@naver.com',_binary '',NULL,NULL,'염아연','01024298065','교육생','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/fb0a2c2c-ebdb-4dc3-8457-b2b7275eb8d3_20250813150327.png','70a60707-ed1f-41c7-a4b2-18f9ef4a1e27','https://i13e201.p.ssafy.io/api/cards/share/70a60707-ed1f-41c7-a4b2-18f9ef4a1e27','2025-08-13 15:03:27.968808',9),(21,1,'싸피',_binary '\0','2025-08-13 15:15:08.217156',NULL,_binary '',NULL,_binary '',NULL,NULL,'염아연','01024298065',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/0d6d6db2-43b8-4c4d-8df8-f6f6f188bc03_20250813151508.png','1abe2304-8890-4509-a9c1-8199814d67fa','https://i13e201.p.ssafy.io/api/cards/share/1abe2304-8890-4509-a9c1-8199814d67fa','2025-08-13 15:15:08.217188',9),(22,1,'싸피',_binary '\0','2025-08-13 15:22:31.001196',NULL,_binary '',NULL,_binary '',NULL,NULL,'김명주','01012341242',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/8e63952b-20ca-4584-87d2-700cc8326f24_20250813152230.png','7d4784d6-e369-4f3f-8781-d8e3ab83d0c3','https://i13e201.p.ssafy.io/api/cards/share/7d4784d6-e369-4f3f-8781-d8e3ab83d0c3','2025-08-13 15:22:31.001228',8),(23,1,'싸피',_binary '\0','2025-08-13 15:27:26.803225',NULL,_binary '',NULL,_binary '',NULL,NULL,'염아연','01024298065',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/4ae3bc8d-bb15-4934-877b-74e4b824a1a2_20250813152726.png','bd7197ce-730d-47aa-bee4-e57dd2c0ab93','https://i13e201.p.ssafy.io/api/cards/share/bd7197ce-730d-47aa-bee4-e57dd2c0ab93','2025-08-13 15:27:26.803253',9),(24,1,'싸피',_binary '\0','2025-08-13 15:34:13.413616',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/c7eea044-15bb-49b7-90a9-d960995675d5_20250813083807.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/c7eea044-15bb-49b7-90a9-d960995675d5_20250813083807.jpg','염아연','01012345678',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/8efa6aa6-dbee-408b-804e-6f1fb8e6f30b_20250813153413.png','df896de1-9c67-438f-86d6-746c98886b80','https://i13e201.p.ssafy.io/api/cards/share/df896de1-9c67-438f-86d6-746c98886b80','2025-08-13 15:34:13.413640',9),(27,1,'싸피',_binary '\0','2025-08-13 15:42:35.057896',NULL,_binary '',NULL,_binary '',NULL,NULL,'염아연','01012345678',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/8b80f39c-037d-464c-b1f7-033867d68895_20250813154234.png','838bcf06-8edb-4b59-ae32-8303850acdca','https://i13e201.p.ssafy.io/api/cards/share/838bcf06-8edb-4b59-ae32-8303850acdca','2025-08-13 15:42:35.057926',9),(28,1,'싸피',_binary '\0','2025-08-13 15:47:26.586242',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/c7eea044-15bb-49b7-90a9-d960995675d5_20250813083807.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/c7eea044-15bb-49b7-90a9-d960995675d5_20250813083807.jpg','김싸피','01012345678',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/99309464-4d4d-419b-895c-4c08a53ba1ca_20250813154726.png','d2f59d22-80ce-4cf0-887e-fd66acacbbeb','https://i13e201.p.ssafy.io/api/cards/share/d2f59d22-80ce-4cf0-887e-fd66acacbbeb','2025-08-13 15:47:26.586275',9),(29,1,'싸피',_binary '\0','2025-08-13 15:59:00.692258',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/c7eea044-15bb-49b7-90a9-d960995675d5_20250813083807.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/c7eea044-15bb-49b7-90a9-d960995675d5_20250813083807.jpg','ㅇㅇㅇ','01012345678',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/6571a419-9e6b-45b4-8a8b-75b088da9a53_20250813155900.png','9261ca45-07d8-4aca-b9c6-260e8d1101ca','https://i13e201.p.ssafy.io/api/cards/share/9261ca45-07d8-4aca-b9c6-260e8d1101ca','2025-08-13 15:59:00.692286',9),(30,1,'싸피',_binary '\0','2025-08-13 16:04:47.916520',NULL,_binary '',NULL,_binary '',NULL,NULL,'ㅇㅇㅇ','01012345678',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/ba336c99-bad7-4f3a-ab07-4a85b5416570_20250813160447.png','9bb3e694-5ae0-4948-960b-b6378c2b02cf','https://i13e201.p.ssafy.io/api/cards/share/9bb3e694-5ae0-4948-960b-b6378c2b02cf','2025-08-13 16:04:47.916572',9),(32,1,'싸피',_binary '\0','2025-08-13 16:16:10.098767',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/c7eea044-15bb-49b7-90a9-d960995675d5_20250813083807.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/c7eea044-15bb-49b7-90a9-d960995675d5_20250813083807.jpg','ㅇㅇㅇ','01012341234',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/e1369456-8f9f-434e-b67c-1225d09dca50_20250813161610.png','6057047f-6150-40e5-aa12-c8ddb529a522','https://i13e201.p.ssafy.io/api/cards/share/6057047f-6150-40e5-aa12-c8ddb529a522','2025-08-13 16:16:10.098794',9),(35,1,'싸피',_binary '\0','2025-08-13 16:40:28.238126',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/c7eea044-15bb-49b7-90a9-d960995675d5_20250813083807.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/c7eea044-15bb-49b7-90a9-d960995675d5_20250813083807.jpg','염아연','01024298065',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/22b28b39-480f-476a-a0f1-3321da711e3d_20250813164028.png','1a5f5f3e-55f1-45bd-b5d6-be21b24d2507','https://i13e201.p.ssafy.io/api/cards/share/1a5f5f3e-55f1-45bd-b5d6-be21b24d2507','2025-08-13 16:40:28.238155',9),(36,1,'싸피',_binary '\0','2025-08-13 16:41:10.249331',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/c7eea044-15bb-49b7-90a9-d960995675d5_20250813083807.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/c7eea044-15bb-49b7-90a9-d960995675d5_20250813083807.jpg','염아연','01024298065',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/a25ec229-412f-44e0-9867-c215814f86ba_20250813164110.png','0bdfc6aa-af7c-4eb4-a9bf-5d6abcc294a8','https://i13e201.p.ssafy.io/api/cards/share/0bdfc6aa-af7c-4eb4-a9bf-5d6abcc294a8','2025-08-13 16:41:10.249357',9),(39,1,'싸피',_binary '\0','2025-08-13 16:59:39.907095',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/0e8847f8-cedd-493b-811b-47c7a3331e44_20250813165939.png','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/49600f3f-11e1-4f42-9cda-1ccd838a1593_20250813165939.png','염아연','01012341234',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/059b4c7d-4a86-496d-a7f5-d525973b96a6_20250813165939.png','dad3ee35-1596-4fef-bfb5-d8d89750436b','https://i13e201.p.ssafy.io/api/cards/share/dad3ee35-1596-4fef-bfb5-d8d89750436b','2025-08-13 16:59:39.907125',9),(46,1,'SSAFY',_binary '\0','2025-08-13 23:59:33.543356',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/511943ca-4342-456f-92e9-18ce4d4f53e7_20250813235933.png','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/fea3332d-a4d5-4885-9aa8-0bcafb8b97c0_20250813235933.png','염아연','01024298065',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/61c0a183-5790-4f66-934c-5b77b2355517_20250813235933.png','a858e25a-a8dd-401c-8d79-2dac739e60f7','https://i13e201.p.ssafy.io/api/cards/share/a858e25a-a8dd-401c-8d79-2dac739e60f7','2025-08-13 23:59:33.543385',8),(47,1,'싸피',_binary '\0','2025-08-14 00:23:12.160999',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/03290815-963c-4274-b600-ee9b0ce2ac87_20250814002311.png','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/a5a2a45f-909e-444f-9cb1-f73a23e159bb_20250814002311.png','염아연','01024298065',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/cb85a888-42cb-4eaf-b7ca-a531991eeb0e_20250814002312.png','f953ed63-2ef2-48e9-a51b-483295a1fc98','https://i13e201.p.ssafy.io/api/cards/share/f953ed63-2ef2-48e9-a51b-483295a1fc98','2025-08-14 00:23:12.161021',8),(52,1,'ㅎ너노',_binary '\0','2025-08-14 01:02:10.992746',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/cb2c5914-463d-4b67-9ed3-9e0d8281e17f_20250814010210.png','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/7f1e4fb3-a393-4c67-bb3e-c3a28605ef11_20250814010210.png','김민지','008612657',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/246e2fef-4faf-409a-8679-4153743b8e17_20250814010210.png','899b2a99-d90d-469e-adf5-fd4ace4102a7','https://i13e201.p.ssafy.io/api/cards/share/899b2a99-d90d-469e-adf5-fd4ace4102a7','2025-08-14 01:02:10.992771',8),(55,1,'싸피',_binary '\0','2025-08-14 01:30:48.338680',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/cddf66f8-3ebd-41ae-b92c-391f3da78b21_20250814013048.png','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/853a7312-7c0a-4781-b6b4-bdb6a8576f53_20250814013048.png','염아연','01012341234',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/f5e26633-8f2f-4357-b559-7807909d1b7d_20250814013048.png','33e8a847-075d-4edd-a12a-aca6af673e08','https://i13e201.p.ssafy.io/api/cards/share/33e8a847-075d-4edd-a12a-aca6af673e08','2025-08-14 01:30:48.338713',8),(57,1,'ㅎㅎ',_binary '\0','2025-08-14 01:34:11.420824',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/87670d10-aaf1-4494-97ec-adc748f978f6_20250814013411.png','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/fb8a096b-a3e4-42c8-81a3-a2b3f84b0a0e_20250814013411.png','ㅎㅎ','00',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/51f4937a-e9b6-4f72-a72a-67c4e7ef184f_20250814013411.png','ff91ec1a-99f9-4cc8-8df3-737824b76bc7','https://i13e201.p.ssafy.io/api/cards/share/ff91ec1a-99f9-4cc8-8df3-737824b76bc7','2025-08-14 01:34:11.420854',8),(60,1,'SSAFY',_binary '\0','2025-08-14 01:56:06.123916',NULL,_binary '','dus1094@naver.com',_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/e57e87b7-06a8-461e-8008-b3b833698e16_20250814015605.png','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/b977df59-363f-4b65-b742-08baee0fd50c_20250814015605.png','염아연','01024298065','E201','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/12660e29-d9b8-4f76-b5ff-9d3853e56218_20250814015606.png','16106f0e-6060-4d62-b0f0-7f508ea42305','https://i13e201.p.ssafy.io/api/cards/share/16106f0e-6060-4d62-b0f0-7f508ea42305','2025-08-14 01:56:06.123946',8),(63,1,'싸피',_binary '\0','2025-08-14 02:21:40.608467',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/b735a014-78f0-4216-971b-e55d1f0df1bd_20250814022140.png','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/417095f6-6ebb-419a-bd51-72fc8feb27b3_20250814022140.png','염아연','01012341234',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/e7abc6d0-1150-4908-971c-17150395203d_20250814022140.png','1852271f-9c11-4bf3-beab-f756efd3f3d3','https://i13e201.p.ssafy.io/api/cards/share/1852271f-9c11-4bf3-beab-f756efd3f3d3','2025-08-14 02:21:40.608497',8),(85,1,'네이버',_binary '','2025-08-14 06:05:11.265798',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/02b1d2e7-b64a-4d35-9d1d-abd0306f11c1_20250814060511.png','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/dad13b71-f2a7-453b-8197-1bb0b85e52e7_20250814060511.png','윤설아','01031198268',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/9619f204-2ac2-46e1-bf42-c7e181c812fd_20250814060511.png','7d434afc-9d6c-42f2-8d5e-c95e488916ec','https://i13e201.p.ssafy.io/api/cards/share/7d434afc-9d6c-42f2-8d5e-c95e488916ec','2025-08-14 06:06:02.624006',1),(99,1,'떡잎물산',_binary '\0','2025-08-15 08:43:26.378356',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/0fde259f-e2e1-4fd6-ad01-f92a945173e0_20250815084326.png','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/d199f6bd-dcda-4f26-ad50-3a7c8c5bc66e_20250815084326.png','신짱구','01011111111','대리','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/1f432c09-b3bf-4e67-a310-448079008ecc_20250815084326.png','c244e576-2bf3-4555-a801-5a31deb3143e','https://i13e201.p.ssafy.io/api/cards/share/c244e576-2bf3-4555-a801-5a31deb3143e','2025-08-15 08:43:26.378381',2),(100,1,'떡잎물산',_binary '\0','2025-08-15 09:13:57.800159',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/f2c68bb5-44c3-4885-97c3-85d6cb4bb561_20250815091357.png','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/a3a07c1a-5c03-4267-a283-b49ff0099903_20250815091357.png','신짱구','01011111111','대리','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/81471c29-bf50-4e46-9f67-fd83000edd86_20250815091357.png','8e62d6a5-544e-4ea4-bf72-89dda54702cc','https://i13e201.p.ssafy.io/api/cards/share/8e62d6a5-544e-4ea4-bf72-89dda54702cc','2025-08-15 09:13:57.800189',2),(101,1,'떡잎물산',_binary '\0','2025-08-15 09:13:57.858034',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/3e79e2bf-d9ea-40dd-b3b5-f253f4d7e70c_20250815091357.png','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/a3118e9a-761f-444c-9322-de14c30bf3f2_20250815091357.png','신짱구','01011111111','대리','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/2f05a688-172a-400f-a2fd-970bed8ca27f_20250815091357.png','579cdc5e-6bc3-4852-80c2-8cd461f933e4','https://i13e201.p.ssafy.io/api/cards/share/579cdc5e-6bc3-4852-80c2-8cd461f933e4','2025-08-15 09:13:57.858069',2),(110,1,'싸피',_binary '\0','2025-08-15 13:38:09.856509',NULL,_binary '','seola8268@naver.com',_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/f9817cd6-e857-4abe-884e-d7c1ee2c4672_20250815133809.png','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/27ae96b0-3b67-424c-9df4-90b8c02ea95d_20250815133809.png','윤설아','01031198268','교육생','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/4e1c7cc5-44e4-473d-a94d-feab085745c2_20250815133809.png','dcf2db4d-93ee-449f-a42f-f75b99a0d326','https://i13e201.p.ssafy.io/api/cards/share/dcf2db4d-93ee-449f-a42f-f75b99a0d326','2025-08-15 13:38:09.856558',1),(111,1,'싸피',_binary '\0','2025-08-15 13:51:22.998780',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/bb60adaf-77e4-4792-a30f-3eea74dcbede_20250815135122.png','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/6b3f783b-ddff-4217-8e73-47166eed8113_20250815135122.png','윤설아','01031198268','교육생','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/198862d1-23bd-478f-9e0f-fd63e02da9b3_20250815135122.png','56cd7fd4-08c0-472e-a7c7-4f5898d020c5','https://i13e201.p.ssafy.io/api/cards/share/56cd7fd4-08c0-472e-a7c7-4f5898d020c5','2025-08-15 13:51:22.998808',1),(112,1,'33ㅅ',_binary '\0','2025-08-15 13:54:23.612281',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/ac981a24-5f52-434d-bc3a-eabe8f5e0b96_20250817163437.png','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/3a0f5958-68b5-4b5c-b21e-6a081394c389_20250817163437.png','dd3','33',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/1cbdfab3-a319-4abe-a5f4-1ffdca07c766_20250815135423.png','c6217d44-71f6-4e5d-b551-a26689e731a6','https://i13e201.p.ssafy.io/api/cards/share/c6217d44-71f6-4e5d-b551-a26689e731a6','2025-08-17 16:34:37.988505',4),(117,1,'22',_binary '\0','2025-08-16 10:02:23.374775',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/644eaafd-2252-49da-aaa7-946dd7975a2b_20250816100223.png','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/58523a5a-32d0-43e9-8a12-9c8e1ac836b1_20250816100223.png','rr22','22',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/acf96d86-95ba-4637-9c9e-094898f92ba9_20250816100223.png','f1ef247a-a6c8-4467-bfb1-821008d5a338','https://i13e201.p.ssafy.io/api/cards/share/f1ef247a-a6c8-4467-bfb1-821008d5a338','2025-08-16 10:02:23.374810',4),(118,1,'33',_binary '\0','2025-08-16 10:02:45.662341',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/3b865e5d-fa72-402f-a81a-7e1449452000_20250816100245.png','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/7bd87530-1308-4f2e-a755-984c003d7d17_20250816100245.png','33','33',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/36b64728-3321-4ea9-a348-9b9c689781ee_20250816100245.png','53e34cc8-e064-4837-adce-367fd668c674','https://i13e201.p.ssafy.io/api/cards/share/53e34cc8-e064-4837-adce-367fd668c674','2025-08-16 10:02:45.662364',4),(143,0,'싸피',_binary '\0','2025-08-17 08:11:10.921829',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/a2c165ce-78e1-413e-9950-0950a10c14e1_20250817082033.png','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/ee5517f6-7e71-498c-9dfa-9df0bf0456f5_20250817082033.png','차민규','01024668768','프론트엔드','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/3f32d83f-2afa-48df-95f7-51aa8c3a9f05_20250817081110.png','2174d941-4ffd-42b8-a018-5227e03564bf','https://i13e201.p.ssafy.io/api/cards/share/2174d941-4ffd-42b8-a018-5227e03564bf','2025-08-17 08:20:33.574343',7),(154,101,'ㅎㅎ',_binary '\0','2025-08-17 14:59:58.455310','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/f5435f8f-b2eb-4d54-ac29-6245282492a6_20250817145958.jpg',_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/37893bf0-203a-4f2d-b035-6f2e8b217d4e_20250817145958.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/9c6c50d8-f2ca-4dbe-99e3-2638750561ae_20250817145958.jpg','ㅅㅅ','44','ㅅㅅ','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/18723f7a-4901-44d9-99af-dca225455e80_20250817145958.png','a05e660c-ab46-4584-9cc1-10a953458508','https://i13e201.p.ssafy.io/api/cards/share/a05e660c-ab46-4584-9cc1-10a953458508','2025-08-17 14:59:58.455329',4),(155,103,'4',_binary '\0','2025-08-17 15:30:45.948910',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/57143ec9-b5df-4439-b0ad-031f74ee28c3_20250817153045.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/e03d24a1-d6ba-4444-bd6f-bc640889e00d_20250817153045.jpg','ㅛ','4','4','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/ed36c98e-54c9-43e3-ab34-0fb4fa2f26b0_20250817153045.png','dd5c0350-825e-4386-aa59-0950f37b2389','https://i13e201.p.ssafy.io/api/cards/share/dd5c0350-825e-4386-aa59-0950f37b2389','2025-08-17 15:30:45.948927',4),(162,108,'2',_binary '\0','2025-08-17 17:19:26.011126',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/6ee568b6-2798-4a40-89bc-779da0a44325_20250817171925.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/b4d09a9e-408e-4f9a-bb76-f3487f0d3b3a_20250817171925.jpg','ㅇ','2',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/099c2000-a114-410c-9127-2044459a346f_20250817171925.png','1f765ba8-8c4a-40e0-bcf5-1a32184a5e6f','https://i13e201.p.ssafy.io/api/cards/share/1f765ba8-8c4a-40e0-bcf5-1a32184a5e6f','2025-08-17 17:19:26.011157',4),(163,108,'88',_binary '\0','2025-08-17 17:22:32.220393',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/f19583a7-8f79-44a6-a0e6-7c0e9691bb11_20250817172232.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/03b7d620-56db-42f1-b4ee-3cbd75cf3b1d_20250817172232.jpg','88','88',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/5eef6cae-55fd-4eb2-984e-7559fb6c30b5_20250817172232.png','03dd8248-00d5-4f50-a941-1b895fd71b7a','https://i13e201.p.ssafy.io/api/cards/share/03dd8248-00d5-4f50-a941-1b895fd71b7a','2025-08-17 17:22:32.220426',4),(164,106,'66',_binary '\0','2025-08-17 17:24:33.574455','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/22dc83fd-dc82-4d31-a9ef-bb5f6be04195_20250817172433.jpg',_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/c0b0cbe0-7faf-41ac-99ba-61e7599aa4de_20250817172433.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/68b4f674-6b17-44a9-91cd-77f5b07722d5_20250817172433.jpg','66','66',NULL,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/eaaa9fff-7441-48a8-b682-f64dc13fcbd3_20250817172433.png','75070bc9-38c7-4293-ab3d-e5be40519b2b','https://i13e201.p.ssafy.io/api/cards/share/75070bc9-38c7-4293-ab3d-e5be40519b2b','2025-08-17 17:24:33.574472',4),(182,106,'야여렬',_binary '\0','2025-08-17 23:22:05.274013',NULL,_binary '','어겯',_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/126175ca-8a41-43c5-92cf-ed14339ba74a_20250817232205.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/20cc5012-0668-413b-b843-2a2b25e1691e_20250817232205.jpg','제효정','2074','러ㅕㄷ덩','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/99ea4f09-3aed-44da-977d-8a0326996767_20250817232205.png','6dff48f8-321d-4da5-84a5-274c3defce41','https://i13e201.p.ssafy.io/api/cards/share/6dff48f8-321d-4da5-84a5-274c3defce41','2025-08-17 23:22:05.274030',2),(183,108,'여랴',_binary '\0','2025-08-17 23:59:54.882745',NULL,_binary '','어어아',_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/63cead1f-eadd-48fb-b7b9-2b3438d926f8_20250817235954.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/dd76d22a-8b06-4ab9-9c4f-433100c782c8_20250817235954.jpg','더댇','1038','러턍','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/bdd66a3e-8603-4024-a1e6-0abdb5220e5c_20250817235954.png','9b6f6b5b-2e88-4a66-b0ef-ebfacef97163','https://i13e201.p.ssafy.io/api/cards/share/9b6f6b5b-2e88-4a66-b0ef-ebfacef97163','2025-08-17 23:59:54.882762',2),(184,106,'ㅑㅅ아ㅛㅇ',_binary '\0','2025-08-18 00:03:13.206907',NULL,_binary '','ㅓㅎ캉',_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/550f1e6e-6249-4fb5-b767-4ae67beba527_20250818000312.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/ef9d6896-4697-48fa-b163-8f12c56d0220_20250818000312.jpg','7ㅅ먀5내ㅛㅇ','0138','ㅑㅅ캇ㄴ','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/c90bf558-105e-4199-a712-7513a32e9307_20250818000313.png','8f875575-a573-4027-bdc4-4cea1d4dd876','https://i13e201.p.ssafy.io/api/cards/share/8f875575-a573-4027-bdc4-4cea1d4dd876','2025-08-18 00:03:13.206925',2),(185,106,'ㅑㅅ아ㅛㅇ',_binary '\0','2025-08-18 00:06:12.857688',NULL,_binary '','ㅓㅎ캉',_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/bbf8ba20-54db-4de3-80d1-d7bed49b919d_20250818000612.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/7493ee0f-5234-4dbd-bf27-c3ca9f63cfd7_20250818000612.jpg','7ㅅ먀5내ㅛㅇ','0138','ㅑㅅ캇ㄴ','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/22f12765-0d4d-4fbe-90b0-97e78d2d826b_20250818000612.png','b5d968ec-f75d-404d-8c2b-3b8edb3a0ffd','https://i13e201.p.ssafy.io/api/cards/share/b5d968ec-f75d-404d-8c2b-3b8edb3a0ffd','2025-08-18 00:06:12.857707',2),(186,102,'야야',_binary '\0','2025-08-18 00:11:31.645197','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/f7d13f5e-0fa0-4919-b1ca-91665ad3fa83_20250818001131.jpg',_binary '','어영',_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/eb721fbd-c048-4978-b704-e8b471c9b531_20250818001131.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/cc37c1b0-a629-497d-b6a7-765a38c5c402_20250818001131.jpg','오야ㅑㄷ','0183','오ㅑㅇ','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/70c19d33-b954-4032-b7c7-5abb94d4d312_20250818001131.png','5f00d860-538e-4dcc-bfca-7fe6a6123784','https://i13e201.p.ssafy.io/api/cards/share/5f00d860-538e-4dcc-bfca-7fe6a6123784','2025-08-18 00:11:31.645214',4),(188,108,'ㅁㅇㄴㄹㅁㄴㅇ',_binary '\0','2025-08-18 00:25:49.036502',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/dd5608e5-8369-4f5a-b776-7d59ffc207b1_20250818002548.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/93b1455c-8153-4bbf-b08c-01ff01c9bdf8_20250818002548.jpg','ㅁㄴㅇㄹㅁㄴㅇ','657567','ㅁㄴㅇㄹㅁ','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/d272578b-386a-4750-9360-df283e8f1f65_20250818002548.png','d11494d2-a264-4d18-ba45-969cfa5dd38a','https://i13e201.p.ssafy.io/api/cards/share/d11494d2-a264-4d18-ba45-969cfa5dd38a','2025-08-18 00:25:49.036519',2),(189,108,'ㅁㅇㄴㄹㅁㄴㅇ',_binary '\0','2025-08-18 00:26:49.876939',NULL,_binary '',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/50001342-ab9c-4bd6-9b45-27771ddde5f9_20250818002649.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/1265208b-2a4d-4348-9dc5-fb77c806009a_20250818002649.jpg','ㅁㄴㅇㄹㅁㄴㅇ','657567','ㅁㄴㅇㄹㅁ','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/eff22b5c-85e2-4a78-a6f6-3ed96f423434_20250818002649.png','74f91e6e-80b3-40a9-bd10-83ab92491c7c','https://i13e201.p.ssafy.io/api/cards/share/74f91e6e-80b3-40a9-bd10-83ab92491c7c','2025-08-18 00:26:49.876955',2),(197,102,'asdfa',_binary '\0','2025-08-18 00:53:59.587923','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/3ca5b0f7-860e-4983-a5c0-cb679161b0c5_20250818005359.jpg',_binary '','asdfa',_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/8b26582e-e492-492e-893b-d60423d251ef_20250818005359.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/17c8710a-e642-4261-a928-b5cbecb142f9_20250818005359.jpg','dfgadf','435234','asdfas','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/89ec1dd8-e2d7-4a89-972c-8f90dd33ae82_20250818005359.png','b7a54ca0-04ab-4539-8e21-a081fe3a82ee','https://i13e201.p.ssafy.io/cards/197','2025-08-18 00:53:59.672678',2),(198,101,'싸피',_binary '\0','2025-08-18 00:59:25.015044','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/f2022411-d3d1-4441-874e-70ea45283c19_20250818005924.jpg',_binary '','chac_2@naver.com',_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/4042a130-f2ed-4717-95c8-97b2e69253b0_20250818005924.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/8992aeec-2daf-4667-9293-9311d2a67bda_20250818005924.jpg','차민규','01024668768','프론트엔드','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/c72d87a6-9aaa-4690-b298-ce6d13c7edf4_20250818005925.png','9564abd6-2586-4286-945d-3ba58bc2b1ac','https://i13e201.p.ssafy.io/cards/198','2025-08-18 00:59:25.249740',10),(205,105,'ddzxcv',_binary '\0','2025-08-18 01:11:14.629110','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/a9355117-41b8-4690-9c61-03a04e79648d_20250818011114.jpg',_binary '','zxcvz',_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/802adb67-7174-4ecd-b6dd-8d56f0183f0e_20250818011114.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/1a2174e6-d06d-4d52-a7ca-c0974c242b1a_20250818011114.jpg','sdfa','34545','zxcv','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/8c28c22a-8dc2-424e-93da-3e57e66607a0_20250818011114.png','18081196-15b0-411e-ae33-a16615817027','https://i13e201.p.ssafy.io/cards/205','2025-08-18 01:11:14.752105',2),(207,102,'싸피',_binary '\0','2025-08-18 01:46:35.899664','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/88d7f0fa-73e0-450e-bbf3-01991e5aa2ef_20250818014635.jpg',_binary '','chac_2@naver.com',_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/6f4972f5-1769-44bb-8437-04ace9ad8f20_20250818014635.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/digital/13cd26f1-a529-4ae3-82f1-f0d554b21954_20250818014635.jpg','차민규','01012345679','프론트엔드','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/qrcodes/3718900f-cabd-41cd-bf8c-7b1194ded1c8_20250818014635.png','26593981-a772-47e2-94ec-e2d8af539da2','https://i13e201.p.ssafy.io/cards/207','2025-08-18 01:46:35.987972',10);
/*!40000 ALTER TABLE `digital_cards` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_members`
--

DROP TABLE IF EXISTS `group_members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_members` (
  `id` int NOT NULL AUTO_INCREMENT,
  `digital_card_id` int DEFAULT NULL,
  `group_id` int NOT NULL,
  `paper_card_id` int DEFAULT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKk19ma410e73aflmaxmd094hhs` (`group_id`,`digital_card_id`),
  UNIQUE KEY `UK2p7x16h4sdgmib5wfvsfxu5on` (`group_id`,`paper_card_id`),
  KEY `FK1vy27mifsbka5hpae8ou2yfmo` (`digital_card_id`),
  KEY `FKdhoh4xtwwt2vcr5mcdy26khv4` (`paper_card_id`),
  KEY `FKnr9qg33qt2ovmv29g4vc3gtdx` (`user_id`),
  CONSTRAINT `FK1vy27mifsbka5hpae8ou2yfmo` FOREIGN KEY (`digital_card_id`) REFERENCES `digital_cards` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKdhoh4xtwwt2vcr5mcdy26khv4` FOREIGN KEY (`paper_card_id`) REFERENCES `paper_cards` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKnr9qg33qt2ovmv29g4vc3gtdx` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKrpgq4bl4kui39wk9mlkl26ib` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_members`
--

LOCK TABLES `group_members` WRITE;
/*!40000 ALTER TABLE `group_members` DISABLE KEYS */;
INSERT INTO `group_members` VALUES (4,NULL,2,12,4),(10,NULL,5,22,10),(12,NULL,5,24,10),(13,NULL,5,25,10),(16,NULL,6,16,1),(17,NULL,3,14,1),(18,63,6,NULL,1),(24,110,10,NULL,10),(25,NULL,11,24,10),(26,NULL,11,25,10),(27,NULL,1,33,4),(28,NULL,2,33,4);
/*!40000 ALTER TABLE `group_members` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `groups`
--

DROP TABLE IF EXISTS `groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `groups` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `headcount` int NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_groups_user_name` (`user_id`,`name`),
  CONSTRAINT `FKgdoumn9ory94sgkoiw0em01sk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `groups`
--

LOCK TABLES `groups` WRITE;
/*!40000 ALTER TABLE `groups` DISABLE KEYS */;
INSERT INTO `groups` VALUES (1,'2025-08-13 08:42:06.818737',1,'칭구들',4),(2,'2025-08-13 18:14:18.423010',0,'tt',4),(3,'2025-08-14 00:40:55.074345',1,'친구',1),(5,'2025-08-14 05:53:05.695707',3,'주식 회사',10),(6,'2025-08-14 15:12:48.360841',2,'동기',1),(8,'2025-08-17 04:07:16.019780',1,'CJ',10),(10,'2025-08-17 09:15:29.313587',1,'친구',10),(11,'2025-08-17 09:16:08.541755',2,'농협',10);
/*!40000 ALTER TABLE `groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `images_histories`
--

DROP TABLE IF EXISTS `images_histories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `images_histories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `image1url` varchar(2048) COLLATE utf8mb4_general_ci NOT NULL,
  `image2url` varchar(2048) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `uploaded_at` datetime(6) NOT NULL,
  `paper_card_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfkk7h4rsnindpoqlmuoab3j4h` (`paper_card_id`),
  CONSTRAINT `FKfkk7h4rsnindpoqlmuoab3j4h` FOREIGN KEY (`paper_card_id`) REFERENCES `paper_cards` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `images_histories`
--

LOCK TABLES `images_histories` WRITE;
/*!40000 ALTER TABLE `images_histories` DISABLE KEYS */;
INSERT INTO `images_histories` VALUES (2,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/b41d066d-1bfe-4dfd-8d03-4ec4b215f3b4_20250814015224.jpg',NULL,'2025-08-14 02:45:37.189735',17),(3,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/227cad57-ae9f-48e0-bd75-dedafb97b6fb_20250814024537.jpg',NULL,'2025-08-14 02:46:46.169097',17),(4,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/1bef5145-ed43-4187-86b5-ec02741b55d1_20250814024646.jpg',NULL,'2025-08-14 02:50:49.946200',17),(5,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/ce6e2b66-a0ca-4712-bf50-8a8cfb70a4eb_20250814025310.jpg',NULL,'2025-08-14 02:53:45.281770',19),(6,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/04e41007-2801-4364-a029-bac13fe9417d_20250814025422.jpg',NULL,'2025-08-14 02:54:46.061959',20),(7,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/afee6695-a74a-4321-911f-ada14216bb92_20250814025446.jpg',NULL,'2025-08-14 02:58:34.423360',20),(8,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/6b7db021-b5cd-4f11-9044-70fcc1e9b768_20250814025049.jpg',NULL,'2025-08-14 11:55:00.514594',17),(9,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/9be414b2-d553-4aa1-adbc-14556b3b4664_20250813163319.jpg',NULL,'2025-08-14 13:08:44.697384',14),(10,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/efeb8436-db1c-4e05-bdfd-1191eacb0108_20250814130844.jpg',NULL,'2025-08-14 13:18:49.772964',14),(11,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/267a8e86-4a86-49a6-a75d-cc23f214802c_20250814131849.jpg',NULL,'2025-08-14 13:23:56.417244',14),(12,'https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/acbf0811-2481-4678-b983-fa8e33ef256d_20250814132356.jpg',NULL,'2025-08-14 14:11:52.494282',14);
/*!40000 ALTER TABLE `images_histories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `memos`
--

DROP TABLE IF EXISTS `memos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `memos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `etc` text COLLATE utf8mb4_general_ci,
  `meeting_notes` text COLLATE utf8mb4_general_ci,
  `personality` text COLLATE utf8mb4_general_ci,
  `relationship` text COLLATE utf8mb4_general_ci,
  `summary` text COLLATE utf8mb4_general_ci,
  `work_style` text COLLATE utf8mb4_general_ci,
  `digital_card_id` int DEFAULT NULL,
  `paper_card_id` int DEFAULT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_fhl6m99xgusih6xuwpxsng2ju` (`digital_card_id`),
  UNIQUE KEY `UK_hkbtbdmbvuu6tkbag14pjkfih` (`paper_card_id`),
  KEY `FKjfl1v48y7d1vlk2jw1qqm3x42` (`user_id`),
  CONSTRAINT `FK2txk26n71y36vn5r37anywxta` FOREIGN KEY (`paper_card_id`) REFERENCES `paper_cards` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKjfl1v48y7d1vlk2jw1qqm3x42` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKrpce9glplw2nt8r9e0qvgyibc` FOREIGN KEY (`digital_card_id`) REFERENCES `digital_cards` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `memos`
--

LOCK TABLES `memos` WRITE;
/*!40000 ALTER TABLE `memos` DISABLE KEYS */;
INSERT INTO `memos` VALUES (10,'','','','','','',NULL,1,2),(11,'','','','','','',1,NULL,7),(15,'','','','','','',NULL,6,2),(16,'','','','','','',NULL,7,2),(17,'','','isfj','남남','조용하고 섬세한 스타일로 대화하세요.','',198,NULL,7),(18,'','','','','','',NULL,9,2),(19,'','','','ㅇㅇ','죄송합니다, 준비된 정보가 부족합니다.','',NULL,10,7),(20,'','','굿\n','','굿','',NULL,11,4),(21,'','','','친구\n','친구','',NULL,12,4),(23,'','','','','','',NULL,14,1),(24,'','','','','','',NULL,15,4),(25,'','','','','','',NULL,16,1),(26,'','','','','','',NULL,17,7),(27,'','','','','','',NULL,18,1),(28,'','','','','','',NULL,19,7),(29,'','','','','','',NULL,20,7),(30,'','','','','','',NULL,21,1),(31,'','','','친구','친구','',NULL,22,10),(32,'','','','','','',63,NULL,1),(34,'','','','','','',NULL,24,10),(35,'','8월 20일에 다시 미팅하기로 함','밝고 활발함','비지니스','비지니스','단시간에 집중하여 일을 처리함',NULL,25,10),(44,'','','','','','',101,NULL,4),(46,'','','','','','',100,NULL,4),(54,'','','','','','',99,NULL,4),(55,'','','','','','',NULL,28,7),(56,'','','','ㅎㅎ','전화로 상황을 가볍게 살펴보세요.','',NULL,29,7),(59,'','','','','','',NULL,32,7),(60,'','','','','','',NULL,33,4),(62,'','','','','','',NULL,35,10);
/*!40000 ALTER TABLE `memos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `is_read` bit(1) NOT NULL,
  `message` text COLLATE utf8mb4_general_ci NOT NULL,
  `card_id` int NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnfi6cf9oopyvrepjok6it2y1n` (`card_id`),
  KEY `FK9y21adhxn0ayjhfocscqox7bh` (`user_id`),
  CONSTRAINT `FK9y21adhxn0ayjhfocscqox7bh` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKnfi6cf9oopyvrepjok6it2y1n` FOREIGN KEY (`card_id`) REFERENCES `digital_cards` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (3,'2025-08-18 00:59:25.239540',_binary '\0','차민규님의 디지털 명함으로 자동 연결되었습니다.',198,7);
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `paper_card_fields`
--

DROP TABLE IF EXISTS `paper_card_fields`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `paper_card_fields` (
  `id` int NOT NULL AUTO_INCREMENT,
  `field_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `field_value` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `paper_card_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlnr58y8uulgxe83v9x26ntqg5` (`paper_card_id`),
  CONSTRAINT `FKlnr58y8uulgxe83v9x26ntqg5` FOREIGN KEY (`paper_card_id`) REFERENCES `paper_cards` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=154 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paper_card_fields`
--

LOCK TABLES `paper_card_fields` WRITE;
/*!40000 ALTER TABLE `paper_card_fields` DISABLE KEYS */;
INSERT INTO `paper_card_fields` VALUES (1,'직책','대표',1),(2,'부서','AS 아이폰 사설수리센터',1),(3,'이메일','zayitun@naver.comergeongengeaom',1),(4,'회사 주소','부산시',1),(5,'부서','AS 아이폰 사설수리센터',1),(6,'회사 주소','부산시',1),(7,'웹사이트','www.사설수리센터.com',1),(8,'회사 번호','0513621615',1),(15,'이메일','ush2711@hitei.cro.com',6),(16,'회사 주소','경상남도 창원시 의창구 반계로 43 (팔용동), 우:51396',6),(17,'회사 주소','경상남도 창원시 의창구 반계로 43 (팔용동), 우:51396',6),(18,'회사 번호','0552998801',6),(19,'전화번호','01032536743',6),(20,'직책','사원',7),(21,'부서','사원 몰랫품개발팀',7),(22,'이메일','seongmin42@hnanwha.com',7),(23,'회사 주소','07325 서울특별시 영등포구 여의대로 56 16층',7),(24,'부서','사원 몰랫품개발팀',7),(25,'회사 주소','07325 서울특별시 영등포구 여의대로 56 16층',7),(28,'직책','대표오세건',9),(29,'부서',') 아이픈 사설수리센터',9),(30,'이메일','zayitun@naver.ccom',9),(31,'회사 주소','부산시',9),(32,'부서',') 아이픈 사설수리센터',9),(33,'회사 주소','부산시',9),(34,'웹사이트','www.사설수리센터 .com',9),(35,'회사 번호','0513621615',9),(56,'직책','대표',21),(57,'회사 주소','창원시의창구동음노연로228번길37-1',21),(58,'회사 주소','창원시의창구동음노연로228번길37-1',21),(59,'회사 번호','0552922181',21),(60,'팩스 번호','0552922182',21),(61,'전화번호','01036602181',21),(62,'직책','대리',22),(64,'부서','',22),(65,'회사 주소','',22),(66,'전화번호','01092134467',22),(71,'직책','지점장',24),(72,'회사 주소','마산합포구어시장7길 78',24),(73,'회사 주소','마산합포구어시장7길 78',24),(74,'전화번호','01035774896',24),(75,'직책','기능계장',25),(76,'회사 주소','경남 창원시 의창구 대산면 주남로 561',25),(77,'회사 주소','경남 창원시 의창구 대산면 주남로 561',25),(78,'회사 번호','0552914641',25),(79,'팩스 번호','0552914168',25),(80,'회사 전화번호','',22),(81,'팩스','',22),(82,'웹사이트','',22),(95,'부서','',18),(96,'회사 주소','',18),(97,'회사 전화번호','',18),(98,'팩스','',18),(99,'웹사이트','',18),(101,'직책','과장 ',14),(102,'부서','',14),(103,'회사 주소','',14),(104,'회사 전화번호','',14),(105,'팩스','',14),(106,'웹사이트','',14),(107,'직책','프론트엔드',28),(108,'이메일','chac_2@naver.com',28),(109,'부서','',28),(110,'회사 주소','',28),(111,'회사 전화번호','',28),(112,'팩스','',28),(113,'웹사이트','',28),(114,'직책','시장',29),(115,'이메일','chac_2@naver.com',29),(128,'직책','학생',32),(129,'이메일','chac_2@naver.com',32),(130,'직책','5',33),(131,'이메일','5',33),(132,'부서','',33),(133,'회사 주소','',33),(134,'회사 전화번호','',33),(135,'팩스','',33),(136,'웹사이트','',33),(137,'부서','',10),(138,'회사 주소','',10),(139,'회사 전화번호','',10),(140,'팩스','',10),(141,'웹사이트','',10),(148,'직책','대표',35),(149,'회사 주소','창원시 노원구 동읍 노연로228번길37-1',35),(150,'회사 주소','창원시 노원구 동읍 노연로228번길37-1',35),(151,'회사 번호','0552922181',35),(152,'팩스 번호','0552922182',35),(153,'전화번호','01036602181',35);
/*!40000 ALTER TABLE `paper_card_fields` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `paper_cards`
--

DROP TABLE IF EXISTS `paper_cards`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `paper_cards` (
  `id` int NOT NULL AUTO_INCREMENT,
  `company` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `is_digital` bit(1) NOT NULL,
  `email` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `is_favorite` bit(1) NOT NULL,
  `image1_url` varchar(2048) COLLATE utf8mb4_general_ci NOT NULL,
  `image2_url` varchar(2048) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `position` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK75fx5dvlrd52ebnnkmjfvjtxk` (`user_id`),
  CONSTRAINT `FK75fx5dvlrd52ebnnkmjfvjtxk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paper_cards`
--

LOCK TABLES `paper_cards` WRITE;
/*!40000 ALTER TABLE `paper_cards` DISABLE KEYS */;
INSERT INTO `paper_cards` VALUES (1,'아이폰','2025-08-13 06:06:38.511778',_binary '\0','zayitun@naver.comergeongengeaom',_binary '\0','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/6b68b12a-e287-4bdc-96cc-0f5308cf7222_20250813060638.jpg',NULL,'오세건','0513621615','대표','2025-08-13 06:06:38.511815',2),(6,'HITEJInro','2025-08-13 06:30:19.706321',_binary '\0','ush2711@hitei.cro.com',_binary '\0','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/7b749a20-069e-43a0-b1d2-6352be5ad047_20250813063019.jpg',NULL,'창원시','01032536743',NULL,'2025-08-13 06:30:19.706355',2),(7,'박성민','2025-08-13 06:45:01.277078',_binary '\0','seongmin42@hnanwha.com',_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/991045ca-8422-4d1f-8c14-62420baac254_20250813064501.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/6f5336d5-57c3-4bbe-acff-d2f6a0a5accf_20250813064501.jpg','박성민','01025089652','사원','2025-08-17 16:25:14.033807',2),(9,'아이폰','2025-08-13 07:56:31.522334',_binary '\0','zayitun@naver.ccom',_binary '\0','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/0da658de-5c60-4d92-aac2-f2556971a6d3_20250813075631.jpg',NULL,'아이폰','0513621615','대표오세건','2025-08-13 07:56:31.522361',2),(10,'통영시','2025-08-13 08:31:40.453717',_binary '\0','chac_2@naver.com',_binary '\0','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/df79adbb-fad1-4d02-bcd9-b62be983fb7c_20250813083140.jpg',NULL,'차민규','01024668768','프론트엔드','2025-08-17 12:17:00.090199',7),(11,'아이폰','2025-08-13 08:34:26.159503',_binary '\0','zayitun@naver.com',_binary '\0','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/1afd40fb-407b-4bb3-bb89-1256e19e86bc_20250813083426.jpg',NULL,'오세건','01023401615','대표','2025-08-13 17:00:57.844624',4),(12,'KIA','2025-08-13 08:38:07.287517',_binary '\0','macdocmm@daum.net',_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/c7eea044-15bb-49b7-90a9-d960995675d5_20250813083807.jpg',NULL,'김기아','01022828119',NULL,'2025-08-14 00:21:49.786965',4),(14,'싸피','2025-08-13 16:33:19.525954',_binary '\0','',_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/2328234a-f26a-4cd4-97ab-c56dbbe38173_20250814141152.jpg',NULL,'제효정','01012345678','과장 ','2025-08-14 17:26:49.694994',1),(15,'회사','2025-08-14 00:16:45.016606',_binary '\0',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/8c13219f-65ab-49b1-8536-ff13d06d750d_20250814001644.jpg','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/c99b105e-aa45-429a-bbf2-a320c49f3257_20250814001644.jpg','ㅂㅂㅇㅇ','01086126764','ㅇㅇ','2025-08-17 16:01:22.313650',4),(16,'구글','2025-08-14 01:14:57.436924',_binary '\0','cha@google.com',_binary '\0','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/d38b37f7-fc62-461d-acf7-c3592e8a15a3_20250814011457.jpg',NULL,'차민규','01012344445','팀장','2025-08-14 01:15:16.799014',1),(17,'ㅊㅊ','2025-08-14 01:52:24.465847',_binary '\0',NULL,_binary '\0','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/50cb803d-3079-42ca-8a89-d1e672f3b3fb_20250814115500.jpg',NULL,'차민규555','010',NULL,'2025-08-16 04:36:05.080275',7),(18,'SSAFY','2025-08-14 02:31:40.131950',_binary '\0','',_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/bf566baa-9e00-4c1f-a059-5cef07b6fdc3_20250814023140.jpg',NULL,'윤설아','01031198268','교육생','2025-08-14 17:26:44.733922',1),(19,'차차차','2025-08-14 02:53:10.400325',_binary '\0',NULL,_binary '\0','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/8d6da73b-a5f1-4b73-9cab-09188313f951_20250814025345.jpg',NULL,'차민규규규규','010',NULL,'2025-08-14 02:53:45.416168',7),(20,'삼성청년SW아카데미','2025-08-14 02:54:22.943095',_binary '\0','삼성청년SW아카데미',_binary '\0','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/a30010a8-aeef-4ec7-b7a0-8715a43d1426_20250814025834.png',NULL,'김명주','01037772277','삼성청년SW아카데미','2025-08-17 12:00:58.079623',7),(21,'CJ','2025-08-14 05:19:10.412187',_binary '\0',NULL,_binary '\0','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/9bd2e18d-d29e-41b3-8bda-68bedf105fad_20250814051910.jpg',NULL,'한진환','01036602181','대표','2025-08-14 05:19:10.412211',1),(22,'대영 지에스','2025-08-14 05:26:12.951544',_binary '\0','al1990@dygs.co.kr',_binary '\0','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/1df2e752-54da-4c6a-b67d-5aa1c2d7b5ae_20250814052612.jpg',NULL,'박진성','01092134467','대리','2025-08-14 05:26:12.951567',10),(24,'NH','2025-08-14 05:39:19.668345',_binary '\0',NULL,_binary '\0','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/cf699f40-6120-4e53-bc6f-4f01eeda031c_20250814053919.jpg',NULL,'박정호','01035774896','지점장','2025-08-14 05:39:19.668419',10),(25,'NH','2025-08-14 05:41:39.447303',_binary '\0','youngju486@nonghyup.com',_binary '\0','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/be06513d-69fb-45ea-8f8c-1f96d07b5718_20250814054139.jpg',NULL,'김영주','0552914641','기능계장','2025-08-18 01:32:01.026377',10),(28,'SSAFY','2025-08-17 07:33:59.205497',_binary '\0','chac_2@naver.com',_binary '\0','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/c47749b7-2168-4790-b657-6a90a191c11d_20250817073359.jpg',NULL,'차민규','01024668768','프론트엔드','2025-08-17 07:41:53.453799',7),(29,'통영시','2025-08-17 09:00:44.889834',_binary '\0','chac_2@naver.com',_binary '\0','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/704ffe63-512f-4a94-9ec4-7a0e45790fdc_20250817090044.jpg',NULL,'차민규','01024668768','시장','2025-08-17 09:00:56.539535',7),(32,'경상대','2025-08-17 12:20:57.966485',_binary '\0','chac_2@naver.com',_binary '\0','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/083cd73e-8732-4812-a4ae-38f53118b10b_20250817122057.jpg',NULL,'차민규','01024668768','학생','2025-08-17 12:26:44.400542',7),(33,'5','2025-08-17 17:50:22.773573',_binary '\0','5',_binary '\0','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/42f7fc61-d1b2-4268-a483-b3e0b3cb76da_20250817175022.jpg',NULL,'ㅏ','5','5','2025-08-17 17:50:22.773590',4),(35,'CJ','2025-08-18 01:50:00.779297',_binary '\0',NULL,_binary '','https://s3.ap-northeast-2.amazonaws.com/businesscard.zip/paper/9a422788-ce64-4ce9-a8ed-d81e25ec6d64_20250818015000.jpg',NULL,'한진환','01036602181','대표','2025-08-18 01:50:14.309912',10);
/*!40000 ALTER TABLE `paper_cards` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `relation`
--

DROP TABLE IF EXISTS `relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `relation` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `is_favorite` tinyint(1) NOT NULL DEFAULT '0',
  `card_id` int NOT NULL,
  `giver_id` int NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKje3w9qlg6kxgm1ndk4tlp7chr` (`card_id`),
  KEY `FKqoj4bdk6s1lf3mrb61aerf4ui` (`giver_id`),
  KEY `FK1rxinahqqnxd9m7ih3pf8t5f1` (`user_id`),
  CONSTRAINT `FK1rxinahqqnxd9m7ih3pf8t5f1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKje3w9qlg6kxgm1ndk4tlp7chr` FOREIGN KEY (`card_id`) REFERENCES `digital_cards` (`id`),
  CONSTRAINT `FKqoj4bdk6s1lf3mrb61aerf4ui` FOREIGN KEY (`giver_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `relation`
--

LOCK TABLES `relation` WRITE;
/*!40000 ALTER TABLE `relation` DISABLE KEYS */;
INSERT INTO `relation` VALUES (4,'2025-08-14 05:32:08.254221',0,63,8,9),(5,'2025-08-14 06:05:11.270989',1,85,1,8),(6,'2025-08-14 13:30:06.000000',0,63,8,1),(8,'2025-08-15 09:37:14.175203',1,101,2,4),(9,'2025-08-15 10:26:37.408695',0,100,2,4),(15,'2025-08-15 12:03:25.151605',0,99,2,4),(16,'2025-08-15 13:39:33.000000',1,110,1,10),(17,'2025-08-18 00:59:25.228916',0,198,10,7);
/*!40000 ALTER TABLE `relation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'2025-08-13 02:56:57.162745','seola8268@naver.com','윤설아'),(2,'2025-08-13 03:21:43.090975','a23782089@gmail.com','hyojeong Je'),(4,'2025-08-13 05:51:27.177402','minji67641@gmail.com','김민지'),(7,'2025-08-13 06:08:36.285656','chacmanz@gmail.com','차민규[부울경_2반_E201]팀장'),(8,'2025-08-13 06:24:26.366457','ttangju02@gmail.com','김명주'),(9,'2025-08-13 15:01:49.818534','ayeon200@gmail.com','염아연'),(10,'2025-08-14 01:40:19.943266','rlaaudwn02@kyungsung.ac.kr','김명주');
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

-- Dump completed on 2025-08-18 11:17:23
