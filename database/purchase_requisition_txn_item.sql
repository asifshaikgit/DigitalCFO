CREATE TABLE `purchase_requisition_txn_item` (
  `ID` int(32) NOT NULL AUTO_INCREMENT,
  `CREATED_AT` datetime DEFAULT NULL,
  `CREATED_BY` int(32) DEFAULT NULL,
  `MODIFIED_AT` datetime DEFAULT NULL,
  `MODIFIED_BY` int(32) DEFAULT NULL,
  `PRESENT_STATUS` int(1) DEFAULT '1',
  `PUR_REQ_TXN_ID` bigint(50) NOT NULL,
  `ORGANIZATION_ID` int(32) NOT NULL,
  `EXPENSE_ID` bigint(50) NOT NULL,
  `NO_OF_UNITS` double DEFAULT NULL,
  `MEASURE_NAME` varchar(256) DEFAULT NULL,
  `VENDOR_ID` bigint(50) DEFAULT NULL,
  `OEM` varchar(256) DEFAULT NULL,
  `EXPECTED_DATETIME` datetime DEFAULT CURRENT_TIMESTAMP,  
 `TYPE_OF_MATERIAL` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`ID`)
  
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=ascii;