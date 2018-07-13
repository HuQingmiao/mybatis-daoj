CREATE TABLE `book` (
  `book_id` bigint(15) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(80) NOT NULL,
  `price` float(10,2) NOT NULL,
  `publish_time` datetime DEFAULT NULL,
  `blob_content` longblob,
  `text_content` longtext,
  PRIMARY KEY (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `editor` (
  `editor_id` bigint(15) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(80) NOT NULL,
  `sex` set('m','f') DEFAULT NULL,
  PRIMARY KEY (`editor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `book_editor` (
  `book_id` bigint(15) unsigned NOT NULL,
  `editor_id` bigint(15) unsigned NOT NULL,
  PRIMARY KEY (`book_id`,`editor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


