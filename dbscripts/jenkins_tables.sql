CREATE TABLE `JNKS_BUILDDATA` (
  `Timestamp` timestamp NULL DEFAULT NULL,
  `Product` varchar(100) DEFAULT '',
  `Component` varchar(100) DEFAULT '',
  `BuildStatus` varchar(30) DEFAULT NULL,
  `BuildNumber` int(11) DEFAULT NULL,
  KEY `fk_comp` (`Product`,`Component`),
  CONSTRAINT `fk_comp` FOREIGN KEY (`Product`, `Component`) REFERENCES `JNKS_COMPONENTPRODUCT` (`Product`, `Component`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `JNKS_COMPONENTPRODUCT` (
  `Product` varchar(100) NOT NULL,
  `Component` varchar(100) NOT NULL,
  PRIMARY KEY (`Product`,`Component`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;