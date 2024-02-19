----------------End R170601-----------------
insert into TRANSACTION_PURPOSE(ID,PRESENT_STATUS,TRANSACTION_PURPOSE,NEXT_QUESTION,QUESTION_TYPE) values(29,1,'Create a Purchase Order or Requisition','What',1);	

insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (31,"Is this the account where you classify withholding tax (TDS) Sec 192 - Payment of Salary",1);
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (32,"Is this the account where you classify withholding tax (TDS) Sec 194A - Income by way of Interest other than 'Interest on Securities'",1);
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (33,"Is this the account where you classify withholding tax (TDS) Sec 194C - Payment to Contractors / Sub Contractors - Individuals / HUF",1);
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (34,"Is this the account where you classify withholding tax (TDS) Sec 194C - Payment to Contractors / Sub Contractors - Others",1);
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (35,"Is this the account where you classify withholding tax (TDS) Sec 194H - Commission or Brokerage",1);
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (36,"Is this the account where you classify withholding tax (TDS) Sec 194-I - Rent - (a) Plant and Machinery",1);
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (37,"Is this the account where you classify withholding tax (TDS) Sec 194-I - Rent - (b) - Land or building or furniture or fitting",1);
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (38,"Is this the account where you classify withholding tax (TDS) Sec 194J - Fees for Professional / Technical Service etc.",1);
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (39,"Is this the account where you classify SGST (State Goods and Service Tax ) input receivable ?",1);			
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (40,"Is this the account where you classify CGST (Central Goods and Service Tax) input receivable ?",1);			
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (41,"Is this the account where you classify IGST (Integrated Goods and Service Tax) input receivable ?",1);			
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (42,"Is this the account where you classify Cess input receivable ?",1);			
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (43,"Is this the account where you classify SGST (State Goods and Service Tax )Output Payable ?",1);			
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (44,"Is this the account where you classify CGST(Central Goods and Service Tax) Output Payable?",1);			
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (45,"Is this the account where you classify IGST (Integrated Goods and Service Tax) Output Payable ?",1);			
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (46,"Is this the account where you classify Cess Output Payable ?",1);			
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (47,"Is this the account where you classify SGST (State Goods and Service Tax )Output Payable on Reverse Charge?",1);			
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (48,"Is this the account where you classify CGST(Central Goods and Service Tax) Output Payable on Reverse Charge ?",1);			
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (49,"Is this the account where you classify IGST (Integrated Goods and Service Tax) Output Payable on Reverse Charge?",1);			
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (50,"Is this the account where you classify Cess Output Payable on reverse charge?",1);			
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (51,"Is this where you classify rounding off amounts on Incomes?",1);
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (52,"Is this where you classify Combination Sales Using Income Items?",1);			
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (53,"Is this the account where you classify SGST (State Goods and Services Tax) - Reverse Charge (RCM) - Input",1);
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (54,"Is this the account where you classify CGST (Central Goods and Services Tax) - Reverse Charge (RCM) - Input",1);			
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (55,"Is this the account where you classify IGST (Integrated Goods and Services Tax) - Reverse Charge (RCM) - Input",1);
insert into COA_VALIDATION_MAPPING(ID,MAPPING_NAME,PRESENT_STATUS) values (56,"Is this the account where you classify Cess - Reverse Charge (RCM) - Input",1);			
		
insert into `TRANSACTION_PURPOSE`(`ID`,`CREATED_AT`,`CREATED_BY`,`MODIFIED_AT`,`MODIFIED_BY`,`PRESENT_STATUS`,`TRANSACTION_PURPOSE`,`NEXT_QUESTION`,`QUESTION_TYPE`) values (30,null,null,null,null,1,'Credit Note for customer','What',1);
insert into `TRANSACTION_PURPOSE`(`ID`,`CREATED_AT`,`CREATED_BY`,`MODIFIED_AT`,`MODIFIED_BY`,`PRESENT_STATUS`,`TRANSACTION_PURPOSE`,`NEXT_QUESTION`,`QUESTION_TYPE`) values (31,null,null,null,null,1,'Debit Note for customer','What',1);
insert into `TRANSACTION_PURPOSE`(`ID`,`CREATED_AT`,`CREATED_BY`,`MODIFIED_AT`,`MODIFIED_BY`,`PRESENT_STATUS`,`TRANSACTION_PURPOSE`,`NEXT_QUESTION`,`QUESTION_TYPE`) values (32,null,null,null,null,1,'Credit Note for vendor','What',1);
insert into `TRANSACTION_PURPOSE`(`ID`,`CREATED_AT`,`CREATED_BY`,`MODIFIED_AT`,`MODIFIED_BY`,`PRESENT_STATUS`,`TRANSACTION_PURPOSE`,`NEXT_QUESTION`,`QUESTION_TYPE`) values (33,null,null,null,null,1,'Debit Note for vendor','What',1);

insert into TRANSACTION_PURPOSE(ID,PRESENT_STATUS,TRANSACTION_PURPOSE,NEXT_QUESTION,QUESTION_TYPE) values(34,1,'Process Payroll','What',1);	

UPDATE ORGANIZATION SET SERIAL_YEAR_CHANGED_DATE = (case when (month(now()) > 3) then CONCAT(YEAR(now()),"-03-31"," 00:00:01") else CONCAT((YEAR(now())-1),"-03-31"," 00:00:01") end);

---execute this before dropping TRIALBALANCE_DISCOUNT---
INSERT INTO TRIALBALANCE_COAITEMS(CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, 
PRESENT_STATUS, TRANSACTION_PURPOSE, DATE, BRANCH_ID, BRANCH_ORGNIZATION_ID, 
CREDIT_AMOUNT, DEBIT_AMOUNT,TRANSACTION_SPECIFICS, TRANSACTION_ID)
SELECT CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, PRESENT_STATUS,
TRANSACTION_PURPOSE, DATE, BRANCH_ID, BRANCH_ORGNIZATION_ID, 
CREDIT_AMOUNT, DEBIT_AMOUNT,TRANSACTION_SPECIFICS, TRANSACTION_ID FROM TRIALBALANCE_DISCOUNT where TRANSACTION_SPECIFICS is not null order by ID ;

insert into withholding_type_details(ID,NAME,PRESENT_STATUS) values(31,'Sec192-Payment of Salary.',1);
insert into withholding_type_details(ID,NAME,PRESENT_STATUS) values(32,'Sec194A-Income by way of Interest other than Interest on Securities',1);
insert into withholding_type_details(ID,NAME,PRESENT_STATUS) values(33,'Sec194C-Payment to Contractors/SubContractors - Individuals / HUF',1);
insert into withholding_type_details(ID,NAME,PRESENT_STATUS) values(34,'Sec194C-Payment to Contractors/SubContractors - Others',1);
insert into withholding_type_details(ID,NAME,PRESENT_STATUS) values(35,'Sec194H-Commission or Brokerage',1);
insert into withholding_type_details(ID,NAME,PRESENT_STATUS) values(36,'Sec194-I-Rent-(a) Plant and Machinery',1);
insert into withholding_type_details(ID,NAME,PRESENT_STATUS) values(37,'Sec194-I-Rent-(b)-Land or building or furniture or fitting',1);
insert into withholding_type_details(ID,NAME,PRESENT_STATUS) values(38,'Sec-194J-Fees for Professional/Technical Service etc.',1);


LOCK TABLES `IDOS_CONFIG_PARAM` WRITE;
/*!40000 ALTER TABLE `IDOS_CONFIG_PARAM` DISABLE KEYS */;
INSERT INTO `IDOS_CONFIG_PARAM` VALUES (1,NULL,NULL,NULL,NULL,1,'CREATOR.FILE.UPLOAD','00011111'),
(2,NULL,NULL,NULL,NULL,1,'CREATOR.FILE.DELETE','00011111'),(3,NULL,NULL,NULL,NULL,1,'ACCOUNTANT.FILE.UPLOAD','00011111'),
(4,NULL,NULL,NULL,NULL,1,'ACCOUNTANT.FILE.DELETE','00011111'),(5,NULL,NULL,NULL,NULL,1,'APPROVER.FILE.UPLOAD','10001111'),
(6,NULL,NULL,NULL,NULL,1,'APPROVER.FILE.DELETE','10001111');
INSERT INTO `IDOS_CONFIG_PARAM` VALUES (7,NULL,NULL,NULL,NULL,1,'HIDE.VIEW.ELEMENT','sellerLoginDivId,supportLiveChat,supportFeedback,calendarLiId,gstCenterId,securitySetting,billOfMaterialId,coaUploadType,coaUploadDivId,uploadChartOfAccount,coaTemplateDownloadDivId,supportMobileId');

INSERT INTO `IDOS_CONFIG_PARAM` VALUES  (8,NULL,NULL,NULL,NULL,1,'EMAIL.SYSTEM.OFF','0');
/*!40000 ALTER TABLE `IDOS_CONFIG_PARAM` ENABLE KEYS */;
UNLOCK TABLES;

//by default param value  should be Zero
INSERT INTO ORGANIZATION_CONFIG (PRESENT_STATUS,ORGANIZATION_ID,PARAM_NAME,PARAM_VALUE,PARAM_DESCRIPTION) VALUES (1,66,'TRANSACTION.DUPLICATE.ITEMS.ALLOWED',0,'Same item is allowed to use in a transactions, 1 means yes');

UPDATE IDOS_CONFIG_PARAM SET PARAM_VALUE= CONCAT(PARAM_VALUE, ',branchBulkUpload,downloadOrganizationBranch,bnchButton,confirmgstin,keyOffCountry,keyOffCountryTh,keyOffCity,keyOffCityTh,CustodianNameEmail,CustodianNameEmailTd,digiSignSetup,CustSafeDepositOpenBalTh,calendarLiId,myPayrollSetupId,CustPhoneNoTh,CustEmailTh,CustSafeDepositOpenBalTd,CustPhoneNoTd,downloadJson,tdsReport,checkOfconfigOrg') WHERE ID=7; 

/* UPDATE/INSERT TABLE  IDOS_CONFIG_PARAM FOR KPMG ORGANIZATION PAGE CHANGES */
UPDATE IDOS_CONFIG_PARAM SET PARAM_VALUE= CONCAT(PARAM_VALUE, ',uploadAccountingManual,isCompositionScheme,isCompositionSchemeSpan,organizationDetailsDiv,uploadPartnershipDeed,vendorSpecificOrgDetailsDiv') WHERE ID=7; 
