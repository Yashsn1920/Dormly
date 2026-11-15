CREATE DATABASE IF NOT EXISTS hostel_db;
USE hostel_db;

CREATE TABLE users (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  usn           VARCHAR(20)  UNIQUE,
  email         VARCHAR(120) NOT NULL UNIQUE,
  phone         VARCHAR(15)  NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role          VARCHAR(10)  NOT NULL,
  block         VARCHAR(10),
  room_no       VARCHAR(10),
  status        VARCHAR(10)  NOT NULL DEFAULT 'PENDING',
  created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE complaints (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  student_id   INT          NOT NULL,
  category     VARCHAR(20)  NOT NULL,
  title        VARCHAR(150) NOT NULL,
  description  TEXT         NOT NULL,
  priority     VARCHAR(10)  NOT NULL,
  status       VARCHAR(15)  NOT NULL DEFAULT 'SUBMITTED',
  image_path   VARCHAR(255),
  assigned_to  VARCHAR(100),
  created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  resolved_at  TIMESTAMP    NULL,
  rating       TINYINT      NULL,
  feedback     VARCHAR(500) NULL,
  CONSTRAINT fk_c_student FOREIGN KEY (student_id) REFERENCES users(id),
  CONSTRAINT chk_rating CHECK (rating IS NULL OR rating BETWEEN 1 AND 5),
  INDEX idx_c_status (status),
  INDEX idx_c_category (category),
  INDEX idx_c_student (student_id)
);

CREATE TABLE complaint_updates (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  complaint_id INT          NOT NULL,
  status       VARCHAR(15)  NOT NULL,
  remark       VARCHAR(500),
  updated_by   INT          NOT NULL,
  updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_u_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
  CONSTRAINT fk_u_user FOREIGN KEY (updated_by) REFERENCES users(id)
);

CREATE TABLE notices (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  title      VARCHAR(150) NOT NULL,
  message    TEXT         NOT NULL,
  posted_by  INT          NOT NULL,
  posted_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_n_user FOREIGN KEY (posted_by) REFERENCES users(id)
);
