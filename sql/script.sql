-- TP 11 : schéma complet (Ex. 5.1 à 5.5)
DROP SCHEMA IF EXISTS `hb-01-one-to-one-uni`;
CREATE SCHEMA `hb-01-one-to-one-uni`;
USE `hb-01-one-to-one-uni`;

-- Ex. 5.1 : relation 1-1 (Instructor -> InstructorDetail)
CREATE TABLE instructor_detail (
                                   id              INT NOT NULL AUTO_INCREMENT,
                                   youtube_channel VARCHAR(128) DEFAULT NULL,
                                   hobby           VARCHAR(45)  DEFAULT NULL,
                                   PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE instructor (
                            id                   INT NOT NULL AUTO_INCREMENT,
                            first_name           VARCHAR(45) DEFAULT NULL,
                            last_name            VARCHAR(45) DEFAULT NULL,
                            email                VARCHAR(45) DEFAULT NULL,
                            instructor_detail_id INT DEFAULT NULL,
                            PRIMARY KEY (id),
                            KEY FK_DETAIL_idx (instructor_detail_id),
                            CONSTRAINT FK_DETAIL FOREIGN KEY (instructor_detail_id)
                                REFERENCES instructor_detail (id)
) ENGINE=InnoDB;

-- Ex. 5.4 : relation 1-N / N-1 (Instructor <-> Course) ; la FK est dans "course"
CREATE TABLE course (
                        id            INT NOT NULL AUTO_INCREMENT,
                        title         VARCHAR(128) DEFAULT NULL,
                        instructor_id INT DEFAULT NULL,
                        PRIMARY KEY (id),
                        UNIQUE KEY TITLE_UNIQUE (title),
                        KEY FK_INSTRUCTOR_idx (instructor_id),
                        CONSTRAINT FK_INSTRUCTOR FOREIGN KEY (instructor_id)
                            REFERENCES instructor (id)
) ENGINE=InnoDB;

-- Ex. 5.5 : relation 1-N unidirectionnelle (Course -> Review) ; la FK est dans "review"
CREATE TABLE review (
                        id        INT NOT NULL AUTO_INCREMENT,
                        comment   VARCHAR(256) DEFAULT NULL,
                        course_id INT DEFAULT NULL,
                        PRIMARY KEY (id),
                        KEY FK_COURSE_ID_idx (course_id),
                        CONSTRAINT FK_COURSE FOREIGN KEY (course_id)
                            REFERENCES course (id)
) ENGINE=InnoDB;