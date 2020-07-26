/*
 Navicat Premium Data Transfer

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80019
 Source Host           : localhost:3306
 Source Schema         : dw_warehouse

 Target Server Type    : MySQL
 Target Server Version : 80019
 File Encoding         : 65001

 Date: 27/07/2020 06:40:35
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for classes
-- ----------------------------
DROP TABLE IF EXISTS `classes`;
CREATE TABLE `classes`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `subject_id` int(0) NULL DEFAULT NULL,
  `year` int(0) NULL DEFAULT NULL,
  `dt_expired` date NOT NULL DEFAULT '9999-12-31',
  `create_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course_regis
-- ----------------------------
DROP TABLE IF EXISTS `course_regis`;
CREATE TABLE `course_regis`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `student_id` int(0) NULL DEFAULT NULL,
  `class_id` int(0) NULL DEFAULT NULL,
  `time_regis` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `dt_expired` date NOT NULL DEFAULT '9999-12-31',
  `create_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for date_dim
-- ----------------------------
DROP TABLE IF EXISTS `date_dim`;
CREATE TABLE `date_dim`  (
  `date_sk` int(0) NOT NULL,
  `full_date` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `day_since_1980` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `month_since_1980` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `day_of_week` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calendar_month` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calendar_year` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calendar_year_month` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `day_of_month` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `day_of_year` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `week_of_year_sunday` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `year_week_sunday` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `week_sunday_start` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `week_of_year_monday` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `year_week_monday` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `week_monday_start` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `holiday` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `day_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`date_sk`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for students
-- ----------------------------
DROP TABLE IF EXISTS `students`;
CREATE TABLE `students`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `lastname` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `firstname` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `dob_sk` int(0) NULL DEFAULT NULL,
  `class_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `class_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `phone` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `hometown` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `note` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `dt_expired` date NOT NULL DEFAULT '9999-12-31',
  `created_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `log_id` int(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 987 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for subjects
-- ----------------------------
DROP TABLE IF EXISTS `subjects`;
CREATE TABLE `subjects`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `no` int(0) NULL DEFAULT NULL,
  `code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `credits_num` int(0) NULL DEFAULT NULL,
  `faculty_manage` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `faculty_using` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `note` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `dt_expired` date NOT NULL DEFAULT '9999-12-31',
  `created_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Procedure structure for add_class
-- ----------------------------
DROP PROCEDURE IF EXISTS `add_class`;
delimiter ;;
CREATE PROCEDURE `add_class`(code VARCHAR(250), subjectID VARCHAR(250), Year VARCHAR(250))
BEGIN
	IF EXISTS (SELECT 1 FROM classes WHERE classes.code = code) THEN
		UPDATE `classes` SET dt_expired = NOW() WHERE classes.code = code;
	END IF;
	
	
	
	SET @subjectSK := (SELECT subjects.id 
										FROM subjects
										WHERE subjects.code = subjectID AND DATE(subjects.dt_expired) = '9999-12-31');
	
	IF @subjectSK IS NULL THEN
		SET @subjectSK := -1;
	END IF;
	
	SELECT @subjectSK;
	INSERT INTO `classes` (
													code,
													subject_id,
													year)
									VALUES (
													code,
													@subjectSK,
													Year);
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for add_course_regis
-- ----------------------------
DROP PROCEDURE IF EXISTS `add_course_regis`;
delimiter ;;
CREATE PROCEDURE `add_course_regis`(code VARCHAR(250), studentCode VARCHAR(250),classID VARCHAR(250), timeRegis VARCHAR(250))
BEGIN
	IF EXISTS (SELECT 1 FROM `course_regis` WHERE course_regis.code = code) THEN
		UPDATE `course_regis` SET dt_expired = NOW() WHERE course_regis.code = code;
	END IF;
	
	
	
	SET @studentSK := (SELECT students.id 
										FROM students
										WHERE students.code = CAST(studentCode AS UNSIGNED) AND DATE(students.dt_expired) = '9999-12-31');
	IF @studentSK IS NULL THEN
		SET @studentSK := -1;
	END IF;
	SELECT @studentSK;
	
	SET @classSK := (SELECT classes.id 
										FROM classes
										WHERE classes.code = classID AND DATE(classes.dt_expired) = '9999-12-31');
	IF @classSK IS NULL THEN
		SET @classSK := -1;
	END IF;
	SELECT @classSK;
	
	
	
	
-- 	SET @dateSK := (SELECT date_dim.date_sk 
-- 								FROM date_dim
-- 								WHERE date_dim.full_date = dateRegis);
-- 	IF @dateSK IS NULL THEN
-- 		SET @dateSK := (SELECT date_dim.date_sk 
-- 										FROM date_dim
-- 										WHERE date_dim.full_date = '2020-06-01');
-- 	END IF;
-- 	
-- 	SELECT @dateSK;

	
	
	INSERT INTO `course_regis` (
													code,
													student_id,
													class_id,
													time_regis)
									VALUES (
													code,
													@studentSK,
													@classSK,
													timeRegis);
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for add_student
-- ----------------------------
DROP PROCEDURE IF EXISTS `add_student`;
delimiter ;;
CREATE PROCEDURE `add_student`(student_code VARCHAR(255), lname VARCHAR(255), fname VARCHAR(255), dob VARCHAR(255), clasID VARCHAR(255), className VARCHAR(255), phone VARCHAR(255), email VARCHAR(255), hometown VARCHAR(255), note VARCHAR(255))
BEGIN

	SET @dobSK := (SELECT date_dim.date_sk 
								FROM date_dim
								WHERE date_dim.full_date = dob);
	
	IF @dobSK IS NULL THEN
		SET @dobSK := (SELECT date_dim.date_sk 
										FROM date_dim
										WHERE date_dim.full_date = '2000-01-01');
	END IF;
	
	SELECT @dobSK;
	

	
	IF EXISTS (SELECT 1 FROM `students` WHERE code = CAST(student_code AS UNSIGNED)) THEN
		UPDATE `students` SET dt_expired = NOW() WHERE students.code = CAST(student_code AS UNSIGNED);
	END IF;
	
	INSERT INTO `students` (
													code,
													lastname,
													firstname,
													dob_sk,
													class_id,
													class_name,
													phone,
													email,
													hometown,
													note
													) 
									VALUES (student_code,
													lname,
													fName,
													@dobSK,
													clasID,
													className,
													phone,
													email,
													hometown,
													note);
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for add_subject
-- ----------------------------
DROP PROCEDURE IF EXISTS `add_subject`;
delimiter ;;
CREATE PROCEDURE `add_subject`(num_order VARCHAR(250), code VARCHAR(250), name VARCHAR(250), credits_num VARCHAR(250), faculty_manage VARCHAR(250),  faculty_using VARCHAR(250), note VARCHAR(250))
BEGIN
	IF EXISTS (SELECT 1 FROM subjects WHERE no = CAST(num_order AS UNSIGNED)) THEN
		UPDATE `subjects` SET dt_expired = NOW() WHERE subjects.no = CAST(num_order AS UNSIGNED);
	END IF;
	
	
	INSERT INTO `subjects` (
													no,
													code,
													name,
													credits_num,
													faculty_manage,
													faculty_using,
													note)
									VALUES (
													CAST(num_order AS UNSIGNED),
													code,
													name,
													CAST(credits_num AS UNSIGNED),
													faculty_manage,
													faculty_using,
													note);
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
