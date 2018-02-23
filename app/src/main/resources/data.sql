-- --------------------------------------------------------
--
-- PaaSword DB Initialization Script 1.0.0
--
-- --------------------------------------------------------

--
-- Table `user`
--

SELECT * FROM `user`;


INSERT INTO `user` (`id`, `date_created`, `email`, `enabled`, `first_login`, `first_name`, `last_name`, `password`, `username`) VALUES
(1, '2016-04-26 14:32:07', 'info@paasword.eu', 1, 0, 'PaaSword', 'Product Manager', '029a186c955cdf72fc32a42199b40048cdac5dc4', 'paasword');

-- --------------------------------------------------------

--
-- Table `user_role`
--

INSERT INTO `user_role` (`id`, `role`, `user_id`) VALUES
(1, 'ROLE_PRODUCT_MANAGER', 1);

-- --------------------------------------------------------

--
-- Table `namespace`
--

INSERT INTO `namespace` (`id`, `enabled`, `last_modified`, `name`, `prefix`, `uri`) VALUES
(1, b'1', '2016-05-23 06:40:56', 'RDF', 'rdf', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'),
(2, b'1', '2016-05-23 06:28:07', 'RDFS', 'rdfs', 'http://www.w3.org/2000/01/rdf-schema#'),
(3, b'1', '2016-05-23 06:28:19', 'XSD', 'xsd', 'http://www.w3.org/2001/XMLSchema#'),
(4, b'1', '2016-05-23 06:28:34', 'DCTERMS', 'dcterms', 'http://purl.org/dc/terms/'),
(5, b'1', '2016-05-23 06:28:48', 'PAC', 'pac', 'http://www.paasword.eu/security-policy/seerc/pac#'),
(6, b'1', '2016-05-23 06:29:00', 'PWD', 'pwd', 'http://www.paasword.eu/security-policy/seerc/pwd#'),
(7, b'1', '2016-09-26 14:10:29', 'PCM', 'pcm', 'http://www.paasword-project.eu/ontologies/casm/2015/11/30#'),
(8, b'1', '2016-05-23 06:29:33', 'PDM', 'pdm', 'http://www.paasword-project.eu/ontologies/dsm/2015/11/30#'),
(9, b'1', '2016-05-23 06:29:53', 'PPM', 'ppm', 'http://www.paasword-project.eu/ontologies/psm/2015/11/30#'),
(10, b'1', '2016-05-23 06:30:04', 'PCPM', 'pcpm', 'http://www.paasword-project.eu/ontologies/cpsm/2015/11/30#'),
(11, b'1', '2016-05-23 13:27:49', 'EXAMPLE', 'ex', 'http://www.example.com/test/1#'),
(12, b'1', '2016-05-26 12:40:17', 'USDL-CORE', 'usdl-core', 'http://www.linked-usdl.org/ns/usdl-core#'),
(13, b'1', '2016-05-26 14:30:02', 'AGGR', 'aggr', 'http://ontology.ihmc.us/temporalAggregates.owl#'),
(14, b'1', '2016-05-26 13:08:02', 'GR', 'gr', 'http://purl.org/goodrelations/v1#'),
(15, b'1', '2016-06-02 14:03:17', 'SCHEMA', 'schema', 'http://www.schema.org/'),
(16, b'1', '2016-07-04 08:20:56', 'PBE', 'pbe', 'http://www.paasword.eu/security-policy/seerc/pbe#'),
(17, b'1', '2016-07-04 08:21:19', 'PBDFD', 'pbdfd', 'http://www.paasword.eu/security-policy/seerc/pbdfd#'),
(18, b'1', '2016-09-22 09:27:16', 'FOAF', 'foaf', 'http://xmlns.com/foaf/0.1/');

-- --------------------------------------------------------

--
-- Table `class`
--

INSERT INTO `class` (`id`, `name`, `last_modified`, `parent_id`, `root_id`, `namespace_id`, `is_deletable`) VALUES
(1, 'Security Context Element Root Class', '2017-01-04 15:00:19', NULL, NULL, NULL, b'0'),
(2, 'Object Root Class', '2017-01-19 13:14:32', NULL, NULL, NULL, b'0'),
(3, 'Request Root Class', '2017-01-19 13:21:29', NULL, NULL, NULL, b'0'),
(4, 'Subject Root Class', '2017-01-19 13:23:44', NULL, NULL, NULL, b'0'),
(5, 'Context Pattern Element Root Class', '2017-01-19 13:14:24', NULL, NULL, NULL, b'0'),
(6, 'Permission Root Class', '2017-01-19 13:28:36', NULL, NULL, NULL, b'0'),
(7, 'Security Context Element', '2017-01-04 14:59:44', 1, 1, 7, b'0'),
(8, 'Location', '2017-01-04 14:51:45', 7, 1, 7, b'1'),
(9, 'Coordinates', '2016-10-31 09:18:47', 7, 1, 7, b'1'),
(10, 'DateTime', '2016-10-31 09:18:47', 7, 1, 7, b'1'),
(12, 'Subject', '2017-01-19 13:24:44', 4, 4, 7, b'0'),
(13, 'Object', '2017-01-19 13:15:39', 2, 2, 7, b'0'),
(14, 'Connectivity', '2016-10-31 09:18:47', 7, 1, 7, b'1'),
(15, 'Identity Type', '2016-10-31 09:18:47', 7, 1, 7, b'1'),
(16, 'Authentication Method', '2016-10-31 09:18:47', 7, 1, 7, b'1'),
(17, 'Authorization Method', '2016-10-31 09:18:47', 7, 1, 7, b'1'),
(18, 'Role', '2016-10-31 09:18:47', 7, 1, 12, b'1'),
(19, 'PhysicalLocation', '2017-01-04 14:51:45', 8, 1, 7, b'1'),
(20, 'Network Location', '2016-10-31 09:18:47', 8, 1, 7, b'1'),
(21, 'DateTime Interval', '2016-10-31 09:18:47', 10, 1, 7, b'1'),
(22, 'Instant', '2016-10-31 09:18:47', 10, 1, 7, b'1'),
(23, 'Organization', '2017-01-19 13:25:07', 12, 4, 7, b'1'),
(24, 'Group', '2017-01-19 13:25:10', 12, 4, 7, b'1'),
(25, 'Person', '2017-01-19 13:25:11', 12, 4, 7, b'1'),
(26, 'Software', '2017-01-19 13:25:13', 12, 4, 7, b'1'),
(27, 'Data Artefact', '2017-01-19 13:16:22', 13, 2, 7, b'1'),
(28, 'Software Artefact', '2017-01-19 13:16:25', 13, 2, 7, b'1'),
(29, 'Infrastructure Artefact', '2017-01-19 13:16:26', 13, 2, 7, b'1'),
(30, 'DeviceType', '2017-01-04 14:51:45', 14, 1, 7, b'1'),
(31, 'Connection Type', '2016-10-31 09:18:47', 14, 1, 7, b'1'),
(32, 'Connection Security', '2016-10-31 09:18:47', 14, 1, 7, b'1'),
(33, 'Connection Metrics', '2016-10-31 09:18:47', 14, 1, 7, b'1'),
(34, 'Permanent', '2016-10-31 09:18:47', 15, 1, 7, b'1'),
(35, 'Ephemeral', '2016-10-31 09:18:47', 15, 1, 7, b'1'),
(36, 'Address', '2016-10-31 09:18:47', 19, 1, 7, b'1'),
(37, 'Point', '2016-10-31 09:18:47', 19, 1, 7, b'1'),
(38, 'Area', '2017-01-19 13:31:18', 19, 1, 7, b'1'),
(39, 'Abstract Location', '2016-10-31 09:18:47', 19, 1, 7, b'1'),
(40, 'POI', '2016-10-31 09:18:47', 19, 1, 7, b'1'),
(41, 'Security Protocol', '2016-10-31 09:18:47', 32, 1, 7, b'1'),
(42, 'Connection Cyphersuite', '2016-10-31 09:18:47', 32, 1, 7, b'1'),
(43, 'Stationary', '2016-10-31 09:18:47', 30, 1, 7, b'1'),
(44, 'Mobile', '2016-10-31 09:18:47', 30, 1, 7, b'1'),
(45, 'Notebook', '2016-10-31 09:18:47', 44, 1, 7, b'1'),
(46, 'Tablet', '2016-10-31 09:18:47', 44, 1, 7, b'1'),
(47, 'Smartphone', '2016-10-31 09:18:47', 44, 1, 7, b'1'),
(48, 'Relational', '2017-01-19 13:17:26', 27, 2, 7, b'1'),
(49, 'Non-relational', '2017-01-19 13:17:28', 27, 2, 7, b'1'),
(50, 'File', '2017-01-19 13:17:30', 27, 2, 7, b'1'),
(51, 'Method', '2017-01-19 13:18:55', 28, 2, 7, b'1'),
(52, 'Service', '2017-01-19 13:18:57', 28, 2, 7, b'1'),
(53, 'Volume', '2017-01-19 13:19:30', 29, 2, 7, b'1'),
(54, 'Hierarchical Data Structure', '2017-01-19 13:18:28', 49, 2, 7, b'1'),
(55, 'DAO', '2017-01-19 13:19:07', 51, 2, 7, b'1'),
(56, 'Context Pattern Element', '2017-01-19 13:12:41', 5, 5, 10, b'1'),
(57, 'Location Pattern', '2017-01-19 13:13:22', 56, 5, 10, b'1'),
(58, 'DateTime Pattern', '2017-01-19 13:13:22', 56, 5, 10, b'1'),
(59, 'Access Sequence Pattern', '2017-01-19 13:13:22', 56, 5, 10, b'1'),
(60, 'Connectivity Pattern', '2017-01-19 13:13:22', 56, 5, 10, b'1'),
(61, 'Object Pattern', '2017-01-19 13:13:22', 56, 5, 10, b'1'),
(62, 'Permission Pattern', '2017-01-19 13:13:22', 56, 5, 10, b'1'),
(63, 'TemporalSeq', '2017-01-19 13:13:22', 56, 5, 10, b'1'),
(64, 'TemporalSeqMember', '2017-01-19 13:13:22', 56, 5, 10, b'1'),
(65, 'Recent Access Location', '2017-01-19 13:13:22', 57, 5, 10, b'1'),
(66, 'Most Frequent Access Location', '2017-01-19 13:13:22', 57, 5, 10, b'1'),
(67, 'Least Frequent Access Location', '2017-01-19 13:13:22', 57, 5, 10, b'1'),
(68, 'Usual Access Location', '2017-01-19 13:13:22', 57, 5, 10, b'1'),
(69, 'Usual DateTime Instance', '2017-01-19 13:13:22', 58, 5, 10, b'1'),
(70, 'Most Frequent DateTime Instance', '2017-01-19 13:13:22', 58, 5, 10, b'1'),
(71, 'Least Frequent DateTime Instance', '2017-01-19 13:13:22', 58, 5, 10, b'1'),
(72, 'Usual DateTime Interval', '2017-01-19 13:13:22', 58, 5, 10, b'1'),
(73, 'Most Frequent DateTime Interval', '2017-01-19 13:13:22', 58, 5, 10, b'1'),
(74, 'Least Frequent DateTime Interval', '2017-01-19 13:13:22', 58, 5, 10, b'1'),
(75, 'Usual Device Type', '2017-01-19 13:13:22', 60, 5, 10, b'1'),
(76, 'Most Frequent Device Type', '2017-01-19 13:13:22', 60, 5, 10, b'1'),
(77, 'Usual Connection Type', '2017-01-19 13:13:22', 60, 5, 10, b'1'),
(78, 'Most Frequent Connection Type', '2017-01-19 13:13:22', 60, 5, 10, b'1'),
(79, 'Usual Connection Security', '2017-01-19 13:13:22', 60, 5, 10, b'1'),
(80, 'Usually Accessed Object', '2017-01-19 13:13:22', 61, 5, 10, b'1'),
(81, 'Most Frequently Accessed Object', '2017-01-19 13:13:22', 61, 5, 10, b'1'),
(82, 'Least Accessed Object', '2017-01-19 13:13:22', 61, 5, 10, b'1'),
(83, 'Recently Accessed Object', '2017-01-19 13:13:22', 61, 5, 10, b'1'),
(84, 'Usually Granted Permission', '2017-01-19 13:13:22', 62, 5, 10, b'1'),
(85, 'Most Frequently Denied Permission', '2017-01-19 13:13:22', 62, 5, 10, b'1'),
(86, 'Most Frequently Granted Permission', '2017-01-19 13:13:22', 62, 5, 10, b'1'),
(87, 'Recently Denied Permission', '2017-01-19 13:13:22', 62, 5, 10, b'1'),
(88, 'Recently Granted Permission', '2017-01-19 13:13:22', 62, 5, 10, b'1'),
(89, 'Permission Element', '2017-01-19 13:29:16', 3, 6, 9, b'1'),
(90, 'Data Permission', '2017-01-19 13:29:16', 89, 6, 9, b'1'),
(91, 'DDL Permission', '2017-01-19 13:29:16', 89, 6, 9, b'1'),
(92, 'File Permission', '2017-01-19 13:29:16', 90, 6, 9, b'1'),
(93, 'Datastore Permission', '2017-01-19 13:29:16', 90, 6, 9, b'1'),
(94, 'Web Endpoint Permission', '2017-01-19 13:29:16', 90, 6, 9, b'1'),
(95, 'Volume Permission', '2017-01-19 13:29:16', 90, 6, 9, b'1'),
(96, 'Datastore DDL Permission', '2017-01-19 13:29:16', 91, 6, 9, b'1'),
(97, 'File System Structure Permission', '2017-01-19 13:29:16', 91, 6, 9, b'1'),
(125, 'City', '2016-10-31 09:18:47', 38, 1, 7, b'1'),
(126, 'Request', '2017-01-19 13:31:58', 3, 3, 7, b'0'),
(127, 'Country', '2017-02-24 15:11:17', 38, 1, 7, b'1'),
(128, 'IPAddress', '2017-03-15 10:48:20', 7, 1, 7, b'1'),
(129, 'OperatingSystem', '2017-06-08 16:48:46', 7, 1, 7, b'1'),
(130, 'DayOfWeek', '2017-06-09 12:36:12', 7, 1, 7, b'1'),
(131, 'HourOfDay', '2017-06-09 12:38:17', 7, 1, 7, b'1');

-- --------------------------------------------------------

--
-- Table `property_type`
--

INSERT INTO `property_type` (`id`, `enabled`, `last_modified`, `name`, `regexp_rule`, `schema_xsd`) VALUES
(1, b'1', '2016-04-26 14:32:07', 'Integer', '\\d+', 'xsd:integer'),
(2, b'1', '2016-04-26 14:32:07', 'String', '.*', 'xsd:string'),
(3, b'1', '2016-04-26 14:32:07', 'Float', '[0-9]*\\.?[0-9]+', 'xsd:float'),
(4, b'1', '2016-04-26 14:32:07', 'Date Time', NULL, 'xsd:dateTime'),
(5, b'1', '2016-05-30 07:46:23', 'Positive Integer', NULL, 'xsd:integer'),
(6, b'1', '2016-05-30 07:46:26', 'Negative Integer', NULL, 'xsd:integer'),
(7, b'1', '2016-05-30 07:46:28', 'Non-negative Integer', NULL, 'xsd:integer'),
(8, b'1', '2016-06-02 13:40:18', 'Boolean', NULL, 'xsd:boolean');

-- --------------------------------------------------------

--
-- Dumping data for table `property`
--

INSERT INTO `property` (`id`, `enabled`, `last_modified`, `name`, `object_property`, `class_id`, `object_property_class_id`, `property_type_id`, `namespace_id`, `sub_property_of_id`, `transitivity`) VALUES
(4, b'1', '2016-05-31 13:38:21', 'hasIdentityType', b'1', 12, 15, NULL, 7, NULL, 0),
(5, b'1', '2016-05-31 13:39:13', 'hasAuthenticationMethod', b'1', 12, 16, NULL, 7, NULL, 0),
(6, b'1', '2016-05-31 13:39:13', 'hasAuthorizationMethod', b'1', 12, 17, NULL, 7, NULL, 0),
(7, b'1', '2016-05-31 13:40:03', 'Latitude', b'0', 9, NULL, 3, 7, NULL, 0),
(8, b'1', '2016-05-31 13:40:03', 'Longitude', b'0', 9, NULL, 3, 7, NULL, 0),
(9, b'1', '2017-02-24 15:52:33', 'Elevation', b'0', 9, NULL, 3, 7, NULL, 0),
(12, b'1', '2016-05-31 13:42:14', 'hasName', b'0', 39, NULL, 2, 7, NULL, 0),
(13, b'1', '2016-05-31 13:42:14', 'hasArea', b'1', 39, 38, NULL, 7, NULL, 0),
(14, b'1', '2016-05-31 13:43:30', 'hasPOIName', b'0', 40, NULL, 2, 7, NULL, 0),
(15, b'1', '2016-05-31 13:43:30', 'hasAddress', b'1', 40, 36, NULL, 7, NULL, 0),
(16, b'1', '2016-05-31 13:44:13', 'hasIPAddress', b'0', 20, NULL, 2, 7, NULL, 0),
(17, b'1', '2016-05-31 13:44:13', 'hasDomain', b'0', 20, NULL, 2, 7, NULL, 0),
(18, b'1', '2016-05-31 13:44:36', 'hasSubnet', b'0', 20, NULL, 2, 7, NULL, 0),
(19, b'1', '2016-05-31 13:44:36', 'hasZone', b'0', 20, NULL, 2, 7, NULL, 0),
(20, b'1', '2016-05-31 13:45:02', 'hasIPAddressRange', b'0', 20, NULL, 2, 7, NULL, 0),
(21, b'1', '2016-05-31 13:45:02', 'hasPort', b'0', 20, NULL, 1, 7, NULL, 0),
(22, b'1', '2016-05-31 13:45:59', 'hasTimezone', b'0', 10, NULL, 4, 7, NULL, 0),
(23, b'1', '2016-05-31 13:45:59', 'before', b'1', 10, 10, NULL, 7, NULL, 0),
(24, b'1', '2016-05-31 13:46:26', 'after', b'1', 10, 10, NULL, 7, NULL, 0),
(25, b'1', '2016-05-31 13:46:26', 'hasYear', b'0', 22, NULL, 4, 7, NULL, 0),
(26, b'1', '2016-05-31 13:46:52', 'hasMonth', b'0', 22, NULL, 4, 7, NULL, 0),
(27, b'1', '2016-05-31 13:46:52', 'hasDay', b'0', 22, NULL, 4, 7, NULL, 0),
(28, b'1', '2016-05-31 13:47:15', 'hasHour', b'0', 22, NULL, 4, 7, NULL, 0),
(29, b'1', '2016-05-31 13:47:15', 'hasMinute', b'0', 22, NULL, 4, 7, NULL, 0),
(30, b'1', '2016-05-31 13:47:28', 'hasSecond', b'0', 22, NULL, 4, 7, NULL, 0),
(31, b'1', '2016-05-31 13:48:33', 'hasBegining', b'0', 21, NULL, 4, 7, NULL, 0),
(32, b'1', '2016-05-31 13:48:33', 'hasEnd', b'0', 21, NULL, 4, 7, NULL, 0),
(33, b'1', '2016-05-31 13:49:45', 'hasStationaryOS', b'0', 43, NULL, 2, 7, NULL, 0),
(34, b'1', '2016-05-31 13:49:45', 'supportsEncryptedStorageStationary', b'0', 43, NULL, 2, 7, NULL, 0),
(35, b'1', '2016-09-23 13:40:57', 'hasMobileOS', b'0', 30, NULL, 2, 7, NULL, 0),
(36, b'1', '2016-05-31 13:50:10', 'hasIMEI', b'0', 44, NULL, 2, 7, NULL, 0),
(37, b'1', '2016-05-31 13:50:27', 'hasIMSI', b'0', 44, NULL, 2, 7, NULL, 0),
(38, b'1', '2016-05-31 13:51:42', 'supportsEncryptedStorageMobile', b'0', 44, NULL, 2, 7, NULL, 0),
(39, b'1', '2016-05-31 13:51:42', 'hasTelecommunicationsProvider', b'0', 31, NULL, 2, 7, NULL, 0),
(40, b'1', '2016-05-31 13:52:26', 'hasConnectionMetric', b'1', 31, 33, NULL, 7, NULL, 0),
(41, b'1', '2016-05-31 13:52:26', 'hasUploadRate', b'0', 33, NULL, 1, 7, NULL, 0),
(42, b'1', '2016-05-31 13:53:21', 'hasDownloadRate', b'0', 33, NULL, 1, 7, NULL, 0),
(43, b'1', '2016-05-31 13:53:21', 'hasMetricUnit', b'0', 33, NULL, 2, 7, NULL, 0),
(44, b'1', '2016-05-31 13:54:36', 'hasActionStatus', b'0', 90, NULL, 2, 9, NULL, 0),
(45, b'1', '2016-05-31 13:54:36', 'hasStartTime', b'0', 90, NULL, 4, 9, NULL, 0),
(46, b'1', '2016-05-31 13:59:40', 'hasEndTime', b'0', 90, NULL, 4, 9, NULL, 0),
(47, b'1', '2016-05-31 13:59:40', 'hasResult', b'0', 90, NULL, 2, 9, NULL, 0),
(48, b'1', '2016-05-31 14:00:27', 'hasDDLActionStatus', b'0', 91, NULL, 2, 9, NULL, 0),
(49, b'1', '2016-05-31 14:00:27', 'hasDDLStartTime', b'0', 91, NULL, 4, 9, NULL, 0),
(50, b'1', '2016-05-31 14:01:01', 'hasDDLEndTime', b'0', 91, NULL, 4, 9, NULL, 0),
(51, b'1', '2016-05-31 14:01:01', 'hasDDLResult', b'0', 91, NULL, 2, 9, NULL, 0),
(52, b'1', '2016-05-31 14:01:59', 'hasUAThreshold', b'0', 68, NULL, 1, 10, NULL, 0),
(53, b'1', '2016-05-31 14:01:59', 'hasUTThreshold', b'0', 69, NULL, 1, 10, NULL, 0),
(54, b'1', '2016-05-31 14:02:38', 'hasUTIThreshold', b'0', 72, NULL, 1, 10, NULL, 0),
(55, b'1', '2016-05-31 14:02:38', 'hasUDTThreshold', b'0', 75, NULL, 1, 10, NULL, 0),
(56, b'1', '2016-05-31 14:03:14', 'hasUCTThreshold', b'0', 77, NULL, 1, 10, NULL, 0),
(57, b'1', '2016-05-31 14:03:14', 'hasUCSThreshold', b'0', 79, NULL, 1, 10, NULL, 0),
(58, b'1', '2016-05-31 14:09:19', 'hasUAOThreshold', b'0', 80, NULL, 1, 10, NULL, 0),
(59, b'1', '2016-05-31 14:09:19', 'hasUGTThreshold', b'0', 84, NULL, 1, 10, NULL, 0),
(73, b'1', '2016-05-31 17:17:08', 'subjectHasRole', b'1', 12, 18, NULL, 7, NULL, 1),
(74, b'1', '2016-05-31 14:17:08', 'hasMember', b'1', 63, 64, NULL, 13, NULL, 0),
(75, b'1', '2016-05-31 14:17:47', 'hasPosition', b'0', 64, NULL, 1, 13, NULL, 0),
(76, b'1', '2016-05-31 17:17:47', 'hasBuildingNumber', b'0', 36, NULL, 1, 7, NULL, 0),
(77, b'1', '2016-05-31 17:18:26', 'hasRoomNumber', b'0', 36, NULL, 3, 7, NULL, 0),
(78, b'1', '2016-05-31 14:18:26', 'hasFloorNumber', b'0', 36, NULL, 1, 7, NULL, 0),
(79, b'1', '2016-05-31 14:19:42', 'refersToContinentalUnion', b'0', 36, NULL, 2, 7, NULL, 0),
(80, b'1', '2016-05-31 14:19:42', 'refersToEconomicUnion', b'0', 36, NULL, 2, 7, NULL, 0),
(81, b'1', '2016-05-31 23:20:07', 'hasaddressCountry', b'0', 36, 127, 2, 7, NULL, 0),
(82, b'1', '2016-05-31 17:20:07', 'hasaddressLocality', b'0', 36, NULL, 2, 7, NULL, 0),
(83, b'1', '2016-05-31 17:20:38', 'hasaddressRegion', b'0', 36, NULL, 2, 7, NULL, 0),
(84, b'1', '2016-05-31 17:20:38', 'haspostalCode', b'0', 36, NULL, 1, 7, NULL, 0),
(85, b'1', '2016-05-31 14:22:10', 'streetAddress', b'0', 36, NULL, 2, 7, NULL, 0),
(86, b'1', '2016-05-31 14:22:10', 'hasSecurityProtocolImplementation', b'0', 41, NULL, 2, 7, NULL, 0),
(87, b'1', '2016-05-31 14:23:18', 'hasTarget', b'0', 13, NULL, 2, 7, NULL, 0),
(88, b'1', '2016-05-31 14:23:18', 'hasRelationalType', b'0', 48, NULL, 2, 7, NULL, 0),
(89, b'1', '2016-05-31 14:23:40', 'hasFileType', b'0', 50, NULL, 2, 7, NULL, 0),
(90, b'1', '2016-05-31 14:23:40', 'hasFileName', b'0', 50, NULL, 2, 7, NULL, 0),
(91, b'1', '2016-05-31 14:24:37', 'hasFilePath', b'0', 50, NULL, 2, 7, NULL, 0),
(92, b'1', '2016-05-31 14:24:37', 'hasNonRelationalType', b'0', 49, NULL, 2, 7, NULL, 0),
(93, b'1', '2016-05-31 14:25:07', 'category', b'0', 52, NULL, 2, 7, NULL, 0),
(94, b'1', '2016-05-31 14:25:07', 'hasEndpoint', b'0', 52, NULL, 2, 7, NULL, 0),
(95, b'1', '2016-05-31 14:25:32', 'hasMName', b'0', 51, NULL, 2, 7, NULL, 0),
(96, b'1', '2016-05-31 14:25:32', 'hasVolumeType', b'0', 53, NULL, 2, 7, NULL, 0),
(97, b'1', '2016-05-31 14:26:10', 'isVolumeEncrypted', b'0', 53, NULL, 2, 7, NULL, 0),
(98, b'1', '2016-05-31 14:26:10', 'volumeInputSpeed', b'0', 53, NULL, 2, 7, NULL, 0),
(99, b'1', '2016-05-31 14:26:40', 'volumeOutputSpeed', b'0', 53, NULL, 2, 1, NULL, 0),
(106, b'1', '2016-05-31 14:28:21', 'hasTemporalSeq', b'1', 59, 63, NULL, 13, NULL, 0),
(110, b'1', '2016-09-26 08:24:19', 'hasPattern', b'1', 56, 56, NULL, 10, NULL, 0),
(113, b'1', '2016-09-26 16:36:17', 'areaIsLocatedIn', b'1', 38, 38, NULL, 7, NULL, 0),
(114, b'1', '2016-10-27 16:42:45', 'hasRequestorLocation', b'1', 25, 19, NULL, 11, NULL, 0),
(122, b'1', '2016-11-04 12:17:57', 'hasBrowser', b'0', 30, NULL, 2, 7, NULL, 0),
(123, b'1', '2016-11-08 22:23:40', 'requestHasDeviceType', b'1', 126, 30, NULL, 7, NULL, 1),
(124, b'1', '2016-11-25 09:57:44', 'hasDeviceHandler', b'0', 30, NULL, 2, 7, NULL, 0),
(126, b'1', '2016-12-06 12:49:54', 'subjectIsLocatedIn', b'1', 25, 19, NULL, 7, NULL, 0),
(127, b'1', '2017-01-19 13:01:07', 'subjectHasLocation', b'1', 12, 125, NULL, 7, NULL, 0),
(129, b'1', '2017-02-24 15:26:51', 'hasCircularRadius', b'0', 38, NULL, 3, 7, NULL, 0),
(130, b'1', '2017-02-24 15:27:24', 'hasRectangularRangeWidth', b'0', 38, NULL, 3, 7, NULL, 0),
(131, b'1', '2017-02-24 15:27:43', 'hasRectangularRangeHeight', b'0', 38, NULL, 3, 7, NULL, 0),
(132, b'1', '2017-02-24 15:50:29', 'hasCoordinates', b'1', 19, 9, NULL, 7, NULL, 0),
(3, b'1', '2016-05-31 16:38:21', 'hasPointCoordinates', b'1', 37, 9, NULL, 7, 132, 0),
(128, b'1', '2017-02-24 17:24:57', 'hasAreaCoordinates', b'1', 38, 9, NULL, 7, 132, 0),
(134, b'1', '2017-03-01 16:58:06', 'requestHasCity', b'1', 126, 125, NULL, 7, NULL, 1),
(135, b'1', '2017-03-15 10:49:06', 'hasIP', b'1', 126, 128, NULL, 7, NULL, 1),
(136, b'1', '2017-03-15 14:08:52', 'requestHasSubject', b'1', 126, 12, NULL, 7, NULL, 1),
(137, b'1', '2017-03-15 14:09:14', 'requestHasObject', b'1', 126, 13, NULL, 7, NULL, 1),
(138, b'1', '2017-03-15 14:09:59', 'requestHasAction', b'1', 126, 89, NULL, 7, NULL, 1),
(139, b'1', '2017-06-08 16:49:45', 'requestHasOperatingSystem', b'1', 126, 129, NULL, 7, NULL, 0),
(140, b'1', '2017-06-09 09:42:04', 'requestInSpecificDay', b'1', 126, 130, NULL, 7, NULL, 0),
(141, b'1', '2017-06-09 09:42:25', 'requestInSpecificHour', b'1', 126, 131, NULL, 7, NULL, 0);

--
-- Table `instance`
--

INSERT INTO `instance` (`id`, `instance_name`, `last_modified`, `class_id`, `namespace_id`) VALUES
(1, 'Any Request', '2017-01-19 08:47:31', 126, 11),
(2, 'Any Subject', '2017-01-19 08:47:27', 12, 11),
(9, 'CALL_WEP', '2016-09-16 15:05:04', 94, 11),
(10, 'READ', '2016-05-31 14:40:23', 93, 11),
(11, 'WRITE', '2016-05-31 14:40:55', 93, 11),
(12, 'READ_FILE', '2016-05-31 14:41:33', 92, 11),
(13, 'WRITE_FILE', '2016-05-31 14:42:31', 92, 11),
(14, 'DELETE_FILE', '2016-05-31 14:42:31', 92, 11),
(15, 'MOVE_FILE', '2016-05-31 14:42:57', 92, 11),
(16, 'CREATE_FILE', '2016-05-31 14:42:57', 92, 11),
(17, 'GET', '2017-03-15 10:11:04', 94, 11),
(18, 'POST', '2017-03-15 10:11:07', 94, 11),
(19, 'PUT', '2017-03-15 10:11:10', 94, 11),
(20, 'DELETE', '2017-03-15 10:11:12', 94, 11),
(21, 'DDL_CREATE', '2016-05-31 14:44:46', 96, 11),
(22, 'DDL_ALTER', '2016-05-31 14:45:39', 96, 11),
(23, 'DDL_DROP', '2016-05-31 14:45:42', 96, 11),
(24, 'DDL_SELECT', '2016-05-31 14:45:30', 96, 11),
(25, 'FS_DIR_CREATE', '2016-05-31 14:46:35', 97, 11),
(26, 'FS_DIR_DELETE', '2016-05-31 14:46:35', 97, 11),
(27, 'FS_DIR_MOVE', '2016-05-31 14:47:03', 97, 11),
(28, 'FS_DIR_ACCESS', '2016-05-31 14:47:03', 97, 11),
(29, 'FS_DIR_LIST', '2016-05-31 14:47:34', 97, 11),
(30, 'FS_DIR_CHANGE_PERMS', '2016-05-31 14:47:34', 97, 11),
(31, 'FS_DIR_CHANGE_OWNERSHIP', '2016-05-31 14:48:05', 97, 11),
(32, 'FS_MOUNT', '2016-05-31 14:48:05', 97, 11),
(33, 'FS_UNMOUNT', '2016-05-31 14:48:28', 97, 11),
(34, 'FS_FORMAT', '2016-05-31 14:48:28', 97, 11),
(74, 'SouthEurope', '2016-09-26 13:40:47', 38, 11),
(75, 'NorthEurope', '2016-09-26 13:41:07', 38, 11),
(76, 'CentralEurope', '2016-09-26 13:41:28', 38, 11),
(84, 'Brussels', '2016-09-26 13:50:57', 125, 11),
(85, 'BE', '2016-09-26 13:57:12', 38, 11),
(86, 'GR', '2016-09-26 13:59:14', 38, 11),
(87, 'Athens', '2016-09-26 14:00:01', 125, 11),
(90, 'WestEurope', '2016-10-19 07:33:42', 38, 11),
(91, 'ES', '2016-10-19 07:34:39', 38, 11),
(92, 'Alcobendas', '2016-10-19 07:35:30', 125, 11),
(103, 'Undefined', '2017-03-15 09:59:36', 128, 7),
(104, 'Unauthorized', '2017-03-15 09:59:54', 12, 7),
(114, 'Android', '2017-06-08 13:48:58', 129, 7),
(115, 'iOS', '2017-06-08 13:49:05', 129, 7),
(116, 'OS X', '2017-06-08 16:49:15', 129, 7),
(117, 'Smartphone', '2017-06-08 13:56:40', 30, 7),
(118, 'Personal computer', '2017-06-08 16:56:51', 30, 7),
(120, 'Monday', '2017-06-09 06:36:27', 130, 7),
(121, 'Tuesday', '2017-06-09 06:36:34', 130, 7),
(122, 'Wednesday', '2017-06-09 06:36:55', 130, 7),
(123, 'Thursday', '2017-06-09 06:37:01', 130, 7),
(124, 'Friday', '2017-06-09 06:37:09', 130, 7),
(125, 'Saturday', '2017-06-09 06:37:23', 130, 7),
(126, 'Sunday', '2017-06-09 06:37:30', 130, 7),
(127, 'Working days', '2017-06-09 06:37:45', 130, 7),
(128, 'Weekend', '2017-06-09 06:37:56', 130, 7),
(129, 'Working hours', '2017-06-09 06:38:43', 131, 7);

-- --------------------------------------------------------

--
-- Table `combining_algorithm`
--

INSERT INTO `combining_algorithm` (`id`, `enabled`, `last_modified`, `name`, `uri`) VALUES
(1, b'1', '2017-03-22 07:45:56', 'Deny Overrides', 'xca:denyOverrides'),
(2, b'1', '2017-03-22 07:46:03', 'Permit Overrides', 'xca:permitOverrides'),
(3, b'1', '2017-03-22 07:46:16', 'Deny Unless Permit', 'xca:denyUnlessPermit'),
(4, b'1', '2017-03-22 07:46:25', 'Permit Unless Deny', 'xca:permitUnlessDeny');

-- --------------------------------------------------------

--
-- Table `iaas_provider_type`
--

INSERT INTO `iaas_provider_type` (`id`, `adapter_implementation`, `enabled`, `last_modified`, `name`) VALUES
(1, 'eu.paasword.adapter.openstack.OpenStackAdapter', b'1', '2016-07-20 07:04:35', 'OpenStack');

-- --------------------------------------------------------

--
-- Table `iaas_provider`
--

INSERT INTO `iaas_provider` (`id`, `connection_url`, `date_created`, `enabled`, `friendly_name`, `last_modified`, `password`, `tenant_name`, `project`, `username`, `network_id`, `iaas_provider_type_id`, `user_id`) VALUES
(1, 'http://192.168.3.253:5000/v3/', '2016-07-20 12:21:46', b'1', 'paasword-openstack-1', '2017-02-03 08:39:45', '!1q2w3e!', 'default', 'defaultproject', 'demo1', 'eaa3021a-9ac8-4ce5-b837-9b6291758f0f', 1, 1),
(2, 'http://192.168.3.253:5000/v3/', '2016-07-20 12:30:17', b'1', 'paasword-openstack-2', '2017-02-03 08:39:47', '!1q2w3e!', 'default', 'defaultproject', 'demo1', 'eaa3021a-9ac8-4ce5-b837-9b6291758f0f', 1, 1),
(3, 'http://192.168.3.253:5000/v3/', '2016-09-21 00:00:00', b'1', 'paasword-openstack-3', '2017-02-03 08:39:52', '!1q2w3e!', 'default', 'defaultproject', 'demo1', 'eaa3021a-9ac8-4ce5-b837-9b6291758f0f', 1, 1),
(11, 'http://147.102.23.40:5000/v3/', '2016-11-07 11:26:02', b'1', 'iccs-openstack', '2017-02-03 08:39:59', '!1q2w3e!', 'default', 'paasword', 'admin', '2a24c559-2c9e-4d0b-b636-9920d193c0b3', 1, 1);

-- --------------------------------------------------------

--
-- Table `iaas_provider_image`
--

INSERT INTO `iaas_provider_image` (`id`, `date_created`, `enabled`, `friendly_name`, `image_id`, `last_modified`, `iaas_provider_id`, `user_id`) VALUES
(1, '2016-09-21 00:00:00', b'1', 'PaaSwordDBInstance', '4fcf658b-3d8c-4571-8ffb-8fcdc23c2993', '2016-10-13 08:04:45', 1, 1),
(2, '2016-09-07 00:00:00', b'1', 'PaaSwordDBInstance', 'd7f259cf-ee03-40ea-bd6b-687e66dc305f', '2017-01-10 10:10:01', 2, 1);

-- --------------------------------------------------------

--
-- Table `paas_provider_type`
--

INSERT INTO `paas_provider_type` (`id`, `adapter_implementation`, `enabled`, `last_modified`, `name`, `authorization_type`, `deployment_type`) VALUES
(1, 'eu.paasword.adapter.cloudfoundry.CloudFoundryAdapter', b'1', '2017-01-10 15:52:33', 'CloudFountry', 'Basic', 'Archive'),
(2, 'eu.paasword.adapter.heroku.HerokuAdapter', b'1', '2017-01-05 09:48:13', 'Heroku', 'Basic', 'Git'),
(3, 'eu.paasword.adapter.openshift.OpenShiftAdapter', b'1', '2017-01-05 09:48:13', 'OpenShift', 'Basic', 'Git'),
(4, 'eu.paasword.adapter.bluemix.BluemixAdapter', b'1', '2017-01-10 13:52:30', 'Bluemix', 'Basic', 'Archive'),
(5, 'eu.paasword.adapter.amazon.AmazonAdapter', b'1', '2017-01-10 13:52:36', 'Amazon Beanstalk', 'APIKey', 'Archive');

-- --------------------------------------------------------

--
-- Table `paas_provider`
--

INSERT INTO `paas_provider` (`id`, `date_created`, `enabled`, `friendly_name`, `last_modified`, `password`, `username`, `paas_provider_type_id`, `user_id`, `connection_url`) VALUES
(1, '2016-09-24 15:56:45', b'1', 'pivotal-free-account', '2016-09-24 12:56:26', '77Spyros&&', 'mantzouratos.s@gmail.com', 1, 1, 'https://api.run.pivotal.io'),
(4, '2017-01-10 14:52:28', b'1', 'bluemix-free-account', '2017-01-10 12:52:25', 'paasport', 'g.ledakis@gmail.com', 4, 1, 'https://api.eu-gb.bluemix.net');

-- --------------------------------------------------------

--
-- Table `proxy_cloud_provider`
--

INSERT INTO `proxy_cloud_provider` (`id`, `connection_url`, `date_created`, `enabled`, `friendly_name`, `last_modified`, `user_id`, `adapter_implementation`) VALUES
(1, 'https://nuv.la/', '2016-12-23 00:00:00', b'1', 'SlipStream', '2016-12-23 15:34:25', 1, 'eu.paasword.adapter.slipstream.SlipStreamAdapter');

-- --------------------------------------------------------
