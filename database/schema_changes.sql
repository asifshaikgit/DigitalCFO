-- Changes related to Bank accounts moved from Branch setup to Chart of Accounts

-- Dropping foreign key constraint from BRANCH_BANK_ACCOUNTS Table
ALTER TABLE BRANCH_BANK_ACCOUNTS
DROP FOREIGN KEY fk_branch_banl_accounts_branchid;

-- Dropping index key constraint from BRANCH_BANK_ACCOUNTS Table
ALTER TABLE BRANCH_BANK_ACCOUNTS
DROP INDEX BRANCH_BANL_ACCOUNTS_BRANCHID_idx;

-- Dropping foreign key constraint from BRANCH_BANK_ACCOUNT_BALANCE_DETAILS Table
ALTER TABLE BRANCH_BANK_ACCOUNT_BALANCE_DETAILS
DROP FOREIGN KEY fk_branch_bank_account_balance_details_branchid;

-- Dropping compound primary key and adding ID as primary key in BRANCH_BANK_ACCOUNTS table.
ALTER TABLE `BRANCH_BANK_ACCOUNTS`
  MODIFY COLUMN `ID` bigint(50) NOT NULL AUTO_INCREMENT,
  DROP PRIMARY KEY,
  ADD PRIMARY KEY (`ID`);

-- Adding new column IFSC_CODE to the BRANCH_BANK_ACCOUNTS table
ALTER TABLE `BRANCH_BANK_ACCOUNTS`
ADD COLUMN `IFSC_CODE` TEXT NULL DEFAULT NULL COLLATE 'ascii_general_ci' AFTER `OPENING_BALANCE`;

-- Creating a new table to map Bank accounts with multiple branches with existing tables
CREATE TABLE `branch_bank_account_mapping` (
  `ID` bigint(50) NOT NULL AUTO_INCREMENT,
  `BRANCH_ID` int(32) NOT NULL,
  `BRANCH_ORGANIZATION_ID` int(32) NOT NULL,
  `BANK_ACCOUNT_ID` bigint(50) NOT NULL,
  `BANK_ACCOUNT_DETAILS_ID` BIGINT(50) NULL DEFAULT NULL,
  `SPECIFICS_ID` bigint(50) NOT NULL,
  `CREATED_AT` timestamp NULL DEFAULT current_timestamp(),
  `MODIFIED_AT` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `CREATED_BY` bigint(20) DEFAULT NULL,
  `MODIFIED_BY` bigint(20) DEFAULT NULL,
  `PRESENT_STATUS` int(11) NOT NULL DEFAULT 1,
  PRIMARY KEY (`ID`),
  KEY `fk_BRANCH_BANK_ACCOUNT_MAPPING_BANK_ACCOUNT` (`BANK_ACCOUNT_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=ascii

-- Added new column in specifics table to check if GST is applicable or not.
ALTER TABLE specifics ADD IS_GST_APPLICABLE VARCHAR(255);