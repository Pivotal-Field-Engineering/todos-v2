DROP TABLE IF EXISTS `todos`;
CREATE TABLE `todos` (
  `id`        VARCHAR(64) DEFAULT NOT NULL,
  `title`     VARCHAR(255) DEFAULT NULL,
  `complete`  BIT DEFAULT FALSE,
  CONSTRAINT pk_todos PRIMARY KEY (`id`)
);

CREATE INDEX idx_todos_title ON `todos` (`title`);