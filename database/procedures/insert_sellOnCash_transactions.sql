DELIMITER $$
DROP PROCEDURE IF EXISTS insert_sellOnCash_transactions;
CREATE DEFINER=`root`@`localhost` PROCEDURE `insert_sellOnCash_transactions`(IN userId int(40),IN debugStoredPorc int(1))
BEGIN
-- from table
DECLARE v_finished INTEGER DEFAULT 0;

DECLARE v_srNo varchar(100) DEFAULT "";
DECLARE v_date varchar(100) DEFAULT "";
DECLARE v_branch varchar(100) DEFAULT "";
DECLARE v_project varchar(100) DEFAULT NULL;
DECLARE v_typeOfSupply varchar(100) DEFAULT "";
DECLARE v_withOrWithoutGST varchar(100) DEFAULT "";
DECLARE v_cust varchar(100) DEFAULT "";
DECLARE v_poReference varchar(100) DEFAULT "";
DECLARE v_itemCode varchar(100) DEFAULT "";
DECLARE v_price varchar(100) DEFAULT "";
DECLARE v_units varchar(100) DEFAULT "";
DECLARE v_discountPrcnt varchar(100) DEFAULT "";
DECLARE v_advAdjstmnt varchar(100) DEFAULT "";
DECLARE v_receiptType varchar(100) DEFAULT "";
DECLARE v_bankDetails varchar(100) DEFAULT "";
DECLARE v_instrumentNo varchar(100) DEFAULT "";
DECLARE v_instrumentDate varchar(100) DEFAULT "";
DECLARE v_receiptDetails varchar(100) DEFAULT "";
DECLARE v_note varchar(100) DEFAULT "";
DECLARE v_srcStateCode VARCHAR(100) DEFAULT "";
DECLARE v_destStateCode varchar(20) DEFAULT "";

DECLARE v_totalValue DOUBLE(10,2) DEFAULT 0;
DECLARE totalInvoiceValue DOUBLE(10,2) DEFAULT 0;

DECLARE v_previousSrNo varchar(100) default "0";
DECLARE v_previousTranId int(100) DEFAULT 0;

DECLARE v_branchId int (10) DEFAULT 0;
DECLARE v_projectId int (10) DEFAULT 0;
DECLARE v_orgId int (10) DEFAULT 0;
DECLARE v_custId int(10) DEFAULT 0;
DECLARE v_bulkId int(100) DEFAULT 0;
DECLARE v_pend_tran int(100) DEFAULT 0;
DECLARE v_spec int(100) DEFAULT 0;
DECLARE v_particular_id int(100) DEFAULT 0;
DECLARE v_no_units double(10,2) DEFAULT 0;
DECLARE v_priceperunit double(10,2) DEFAULT 0;
DECLARE v_discountPercent double(10,2) DEFAULT 0;
DECLARE v_gross_amt double(10,2) DEFAULT 0;
DECLARE v_payment_due double(10,2) DEFAULT 0;
DECLARE v_opening_bal double(10,2) DEFAULT 0;
DECLARE v_receipt_id int(1) DEFAULT 0;
DECLARE v_tran_id int(100) DEFAULT 0;
DECLARE v_tb_credit double(10,2) DEFAULT 0;
DECLARE v_br_depositbox int(10) DEFAULT NULL;
DECLARE v_tran_date date DEFAULT null;
DECLARE v_instrument_date date DEFAULT null;
DECLARE v_advAdj double(10,2) DEFAULT 0;
DECLARE v_netAmount DOUBLE(10,2) DEFAULT 0;
DECLARE branch_bank_id INT(100) DEFAULT 0;
DECLARE branch_deposit_key int(100) DEFAULT 0;

-- pending transaction i.e. sell on  credit update variables
DECLARE vpend_withholding_tax double(10,2) DEFAULT 0;
DECLARE vpend_cust_netpay double(10,2) DEFAULT 0;
DECLARE vpend_cust_duepay double(10,2) DEFAULT 0;
DECLARE vpend_pay_status varchar(20) DEFAULT "";
DECLARE v_sourceGstCode  varchar(20) DEFAULT "";
DECLARE v_destGstCode varchar(20) DEFAULT "";
DECLARE v_payment DOUBLE(10,2) DEFAULT 0;

DECLARE yearNumber INT(20) DEFAULT 0;
DECLARE monthNo VARCHAR(20) DEFAULT "";
DECLARE yearNo INT(20) DEFAULT 0;
DECLARE branchName VARCHAR(20) DEFAULT 0;
DECLARE off_invoice_serialNo INT(10) DEFAULT 0;
DECLARE invoiceNo VARCHAR(100) DEFAULT "";

DECLARE temp_cursor CURSOR FOR 
 select * from temp_sell_on_cash_trans;
-- declare NOT FOUND handler
DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_finished = 1;
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION 
BEGIN   
 GET DIAGNOSTICS CONDITION 1 @sqlstate = RETURNED_SQLSTATE, 
 @errno = MYSQL_ERRNO, @text = MESSAGE_TEXT;
SET @full_error = CONCAT("ERROR ", @errno, " (", @sqlstate, "): ", @text);
SELECT @full_error;
SELECT 'An error has occurred, operation rollbacked and the stored procedure was terminated' INTO @errorString;
 insert into  LOG_STORED_PROC(MESSAGE,NAME) values('Error SQLEXCEPTION',"insert sellOnCash");
END;
  	


INSERT INTO SELL_ON_CASH_SEQ(counter) VALUES (NULL);
SELECT 
    MAX(counter)
INTO v_bulkId FROM
    sell_on_cash_seq;


SELECT 
    BRANCH_ORGANIZATION_ID
INTO v_orgId FROM
    USERS
WHERE
    id = userId;

OPEN temp_cursor;
get_temp: LOOP
 FETCH temp_cursor INTO v_srNo,v_date,v_branch,v_project,v_typeOfSupply,v_withOrWithoutGST,v_cust,v_poReference,v_itemCode,v_price,v_units,v_discountPrcnt,v_advAdjstmnt,v_receiptType,v_bankDetails,v_instrumentNo,v_instrumentDate,v_receiptDetails,v_note;
 IF v_finished = 1 THEN 
 LEAVE get_temp;
 END IF;
 IF(debugStoredPorc=1) THEN
  insert into LOG_STORED_PROC(MESSAGE,NAME) values('Processing data for Sr. No',"insert sellOnCash");
END IF;  

IF(v_previousSrNo!=v_srNo) THEN
IF(v_date IS NOT NULL) THEN
  SELECT CONVERT(v_date, date) into v_tran_date; 
 -- SET v_tran_date = CONVERT(v_date, '%Y%m%d');
 ELSE
  SET v_tran_date = CURDATE(); 
 END IF;
 IF( v_receiptType LIKE 'BANK') THEN
 IF(v_instrumentDate IS NOT NULL) THEN
  SELECT CONVERT(v_instrumentDate, date) into v_instrument_date; 
 -- SET v_tran_date = CONVERT(v_date, '%Y%m%d');
 ELSE
  SET v_instrument_date = CURDATE(); 
 END IF;
 END IF;

 
IF(v_branch IS NOT NULL) THEN
select  ID,STATE_CODE,GSTIN into v_branchId,v_srcStateCode,v_sourceGstCode from BRANCH where ORGANIZATION_ID=v_orgId and NAME=v_branch;  
END IF;  

IF(v_cust IS NOT NULL) THEN
 select  ID,OPENING_BALANCE,GSTIN into v_custId,v_opening_bal,v_destGstCode from VENDOR where name=v_cust and BRANCH_ORGANIZATION_ID=v_orgId and TYPE=2;  
END IF;
SELECT 
    ID
INTO v_spec FROM
    SPECIFICS
WHERE
    name = v_itemCode
        AND ORGANIZATION_ID = v_orgId;  
SET v_destStateCode= SUBSTRING(v_destGstCode,1,2);

 

  
SET v_no_units=CAST(v_units as DECIMAL(10,2));
SET v_priceperunit=CAST( v_price as DECIMAL(10,2));

insert into  LOG_STORED_PROC(MESSAGE,NAME) values('Discount percent=',v_discountPrcnt);
IF(v_discountPrcnt!="") THEN
SET v_discountPercent=CAST(v_discountPrcnt as DECIMAL(10,2));
ELSE 
SET v_discountPercent=0;
END IF;
insert into  LOG_STORED_PROC(MESSAGE,NAME) values('Advance Adjustment=',v_advAdjstmnt);
IF(v_advAdjstmnt!="") THEN
SET v_advAdj=CAST(v_advAdjstmnt as DECIMAL(10,2));
ELSE
SET v_advAdj=0;
END IF;

set v_gross_amt=v_no_units*v_priceperunit;

SELECT 
    particulars_id
INTO v_particular_id FROM
    SPECIFICS
WHERE
    id = v_spec
LIMIT 1;

set v_netAmount=v_gross_amt-(v_discountPercent*v_gross_amt)/100;
set v_gross_amt=v_netAmount;
insert into  LOG_STORED_PROC(MESSAGE,NAME) values('SOURCE GST CODE',v_srcStateCode);
insert into  LOG_STORED_PROC(MESSAGE,NAME) values('DESTINATION GST CODE',v_destStateCode);
BEGIN
DECLARE v_finished_value int(10) DEFAULT NULL;
DECLARE v_taxName1 VARCHAR(100) DEFAULT NULL;
DECLARE v_taxRate1 double(10,2) DEFAULT NULL;
DECLARE v_taxType1 INT(10) DEFAULT NULL;
DECLARE v_taxName2 VARCHAR(100) DEFAULT NULL;
DECLARE v_taxRate2 double(10,2) DEFAULT NULL;
DECLARE v_taxType2 INT(10) DEFAULT NULL;
DECLARE v_taxName3 VARCHAR(100) DEFAULT NULL;
DECLARE v_taxRate3 double(10,2) DEFAULT 0;
DECLARE v_taxType3 INT(10) DEFAULT NULL;
DECLARE v_taxName4 VARCHAR(100) DEFAULT NULL;
DECLARE v_taxRate4 double(10,2) DEFAULT NULL;
DECLARE v_taxType4 INT(10) DEFAULT NULL;
DECLARE branch_tax_id INT(20) DEFAULT NULL;
DECLARE v_invoice_value DOUBLE(10,2) DEFAULT NULL;

DECLARE v_tax1 DOUBLE(10,2) DEFAULT NULL;
DECLARE v_tax2 DOUBLE(10,2) DEFAULT NULL;
DECLARE v_tax3 DOUBLE(10,2) DEFAULT NULL;
DECLARE v_tax4 DOUBLE(10,2) DEFAULT NULL;
DECLARE v_tax DOUBLE(10,2) DEFAULT NULL;
DECLARE v_taxName VARCHAR(100) DEFAULT NULL;
DECLARE v_taxRate DOUBLE(10,2) DEFAULT NULL;
DECLARE v_taxType INT(10) DEFAULT NULL;

DECLARE v_specTaxFormula int(100) DEFAULT NULL;
DECLARE v_branchTaxId int (100) DEFAULT NULL;
DECLARE v_branchTax_id1 int(100) DEFAULT NULL;
DECLARE v_branchTax_id2 int(100) DEFAULT NULL;
DECLARE v_branchTax_id3 int(100) DEFAULT NULL;
DECLARE v_branchTax_id4 int(100) DEFAULT NULL;
DECLARE v_invoiceValue VARCHAR(100) DEFAULT NULL;
DECLARE v_taxFormula  VARCHAR(200) DEFAULT NULL;
DECLARE v_taxFormula1  VARCHAR(100) DEFAULT NULL;
DECLARE v_taxFormula2  VARCHAR(200) DEFAULT NULL;
DECLARE v_taxFormula3  VARCHAR(200) DEFAULT NULL;
DECLARE v_taxFormula4  VARCHAR(100) DEFAULT NULL;


DECLARE temp_cursor_sell cursor for select ID,BRANCH_TAXESID from BRANCH_SPECIFICS_TAX_FORMULA where BRANCH_ORGANIZATION_ID=v_orgId and BRANCH_ID=v_branchId and specifics_id=v_spec;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_finished_value = 1;
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION 
BEGIN   
 GET DIAGNOSTICS CONDITION 1 @sqlstate = RETURNED_SQLSTATE, 
 @errno = MYSQL_ERRNO, @text = MESSAGE_TEXT;
SET @full_error = CONCAT("ERROR ", @errno, " (", @sqlstate, "): ", @text);
SELECT @full_error;
SELECT 'An error has occurred, operation rollbacked and the stored procedure was terminated' INTO @errorString;
 insert into  LOG_STORED_PROC(MESSAGE,NAME) values('Error SQLEXCEPTION',"insert sell on credit in taxes");
END;
OPEN temp_cursor_sell;
get_temp_sell: LOOP
FETCH temp_cursor_sell  into v_specTaxFormula,v_branchTaxId;
 IF v_finished_value = 1 THEN 
 LEAVE get_temp_sell;
 END IF;
IF(debugStoredPorc=1) THEN
  insert into LOG_STORED_PROC(MESSAGE,NAME) values('Processing data for Sr. No',"insert recPays");
END IF; 

SELECT 
    tax_name, tax_rate, tax_type, ID
INTO v_taxName , v_taxRate , v_taxType , branch_tax_id FROM
    BRANCH_TAXES
WHERE
    BRANCH_ORGANIZATION_ID = v_orgId
        AND ID = v_branchTaxId
LIMIT 1;
IF v_srcStateCode=v_destStateCode THEN
IF(v_taxName LIKE 'SGST%') THEN
set v_branchTax_id1= branch_tax_id;
set v_tax1=v_netAmount*(v_taxRate/100.00);
set v_taxName1=v_taxName;
set v_taxType1=v_taxType;
set v_taxRate1=v_taxRate;
set v_invoice_value=v_netAmount+v_tax1;
SET totalInvoiceValue=v_invoice_value;
set v_taxFormula1=CONCAT(v_taxName1,'(+',v_taxRate1,'%);',v_tax1);
insert into  LOG_STORED_PROC(MESSAGE,NAME) values('TAX FORMULA1',v_taxFormula1);
END IF;
IF(v_taxName LIKE 'CGST%') THEN
set v_branchTax_id2=branch_tax_id;
set v_tax2=v_netAmount*(v_taxRate/100.00);
set v_taxName2=v_taxName;
set v_taxType2=v_taxType;
set v_taxRate2=v_taxRate;
set v_invoice_value=v_invoice_value+v_tax2;

SET totalInvoiceValue=v_invoice_value;
set v_taxFormula2=CONCAT(v_taxName2,'(+',v_taxRate2,'%);',v_tax2);
insert into LOG_STORED_PROC(MESSAGE,NAME) values('Total invoice value=', totalInvoiceValue);
insert into  LOG_STORED_PROC(MESSAGE,NAME) values('TAX FORMULA2',v_taxFormula2);
END IF;
IF(v_taxName LIKE 'CESS%') THEN
set v_branchTax_id4=branch_tax_id;
set v_tax4=v_netAmount*(v_taxRate/100.00);
set v_taxName4=v_taxName;
set v_taxType4=v_taxType;
set v_taxRate4=v_taxRate;
set v_invoice_value=v_invoice_value+v_tax4;
SET totalInvoiceValue=v_invoice_value;
set v_taxFormula4=CONCAT(v_taxName4,'(+',v_taxRate4,'%);',v_tax4);
insert into  LOG_STORED_PROC(MESSAGE,NAME) values('TAX FORMULA4',v_taxFormula4);
END IF;
ELSE
IF(v_taxName LIKE 'IGST%') THEN
set v_branchTax_id3= branch_tax_id;
set v_tax3=v_netAmount*(v_taxRate/100.00);
set v_taxName3=v_taxName;
set v_taxType3=v_taxType;
set v_taxRate3=v_taxRate;
set v_invoice_value=v_netAmount+v_tax3;
SET totalInvoiceValue=v_invoice_value;
set v_tax=v_tax3; 

set v_taxFormula3=CONCAT(v_taxName3,'(+',v_taxRate3,'%);',v_tax3);
END IF;
IF(v_taxName LIKE 'CESS%') THEN
set v_branchTax_id4=branch_tax_id;
set v_tax4=v_netAmount*(v_taxRate/100.00);
set v_taxName4=v_taxName;
set v_taxType4=v_taxType;
set v_taxRate4=v_taxRate;
set v_invoice_value=v_invoice_value+v_tax4;
SET totalInvoiceValue=v_invoice_value;
set v_taxFormula4=CONCAT(v_taxName4,'(+',v_taxRate4,'%);',v_tax4);
insert into  LOG_STORED_PROC(MESSAGE,NAME) values('TAX FORMULA4',v_taxFormula4);
END IF;
END IF;
END LOOP get_temp_sell;
CLOSE temp_cursor_sell;
IF v_srcStateCode=v_destStateCode THEN
IF(v_branchTax_id4!=0) THEN
set v_tax=v_tax1+v_tax2+v_tax4;
set v_taxFormula=CONCAT(v_taxFormula1,'+',v_taxFormula2,'+',v_taxFormula4);
ELSE
set v_tax=v_tax1+v_tax2;
set v_taxFormula=CONCAT(v_taxFormula1,'+',v_taxFormula2);
END IF;
IF(v_invoice_value=0) then
SET v_invoice_value=v_netAmount-v_advAdj;
SET totalInvoiceValue=v_invoice_value;
else
SET v_invoice_value=v_invoice_value-v_advAdj;
SET totalInvoiceValue=v_invoice_value;
END IF;

BEGIN

DECLARE serialNo INT(10) DEFAULT 0;
set yearNumber=YEAR(v_tran_date);

set monthNo=LPAD(MONTH(v_tran_date), 2, '0');
set yearNo=MOD(yearNumber,100);
SET branchName=SUBSTRING(v_branch,1,3);
SELECT OFF_INVOICE_SERIAL into off_invoice_serialNo from ORGANIZATION where ID=v_orgId;
IF(off_invoice_serialNo is NULL) THEN
SET serialNo=1;
ELSE 
SET serialNo=off_invoice_serialNo+1;
END IF;
UPDATE ORGANIZATION SET OFF_INVOICE_SERIAL=serialNo where ID=v_orgId;
SET invoiceNo=UPPER(CONCAT('IV/',branchName,monthNo,yearNo,'/00',serialNo));
insert into  LOG_STORED_PROC(MESSAGE,NAME) values('invoice no',invoiceNo);

insert into LOG_STORED_PROC(MESSAGE,NAME) values('Total invoice value AFTER INVOICE', totalInvoiceValue);

IF( v_receiptType LIKE 'CASH') THEN
insert into TRANSACTION(TRANSACTION_BRANCH,TRANSACTION_BRANCH_ORGANIZATION,TRANSACTION_VENDOR_CUSTOMER,TRANSACTION_REF_NUMBER,PAYMENT_STATUS,
TRANSACTION_STATUS,TRANSACTION_PURPOSE,CUSTOMER_DUE_PAYMENT,NET_AMOUNT,TAX_NAME_1,TAX_VALUE_1,TAX_NAME_2,TAX_VALUE_2,TAX_NAME_4,TAX_VALUE_4,TRANSACTION_SPECIFICS, TRANSACTION_SPECIFICS_PARTICULARS,TYPE_OF_SUPPLY,NO_OF_UNITS,PRICE_PER_UNIT,GROSS_AMOUNT,RECEIPT_DETAILS_TYPE, TRANSACTION_ACTIONDATE,PRESENT_STATUS,SOURCE_GSTIN,DESTINATION_GSTIN,INVOICE_NUMBER,PERFORMA_INVOICE,CREATED_BY, CREATED_AT,PO_REFERENCE,REMARKS) 
values (v_branchId,v_orgId,v_custId,CONCAT("BULK",v_bulkId,"SRNO",v_srNo),"PAID","Accounted",1,0,v_invoice_value,v_taxName1,v_tax1,v_taxName2,v_tax2,v_taxName4,v_tax4,v_spec,v_particular_id,1,v_no_units,v_priceperunit,v_gross_amt,1,v_tran_date,1,v_sourceGstCode,v_destGstCode,invoiceNo,0,userId, now(),v_poReference,v_note);
ELSE
SELECT ID into branch_bank_id FROM BRANCH_BANK_ACCOUNTS where bank_name= v_bankDetails and BRANCH_ORGANIZATION_ID=v_orgId and BRANCH_ID=v_branchId;
insert into TRANSACTION(TRANSACTION_BRANCH,TRANSACTION_BRANCH_ORGANIZATION,TRANSACTION_VENDOR_CUSTOMER,TRANSACTION_REF_NUMBER,PAYMENT_STATUS,
TRANSACTION_STATUS,TRANSACTION_PURPOSE,CUSTOMER_DUE_PAYMENT,NET_AMOUNT,TAX_NAME_1,TAX_VALUE_1,TAX_NAME_2,TAX_VALUE_2,TAX_NAME_4,TAX_VALUE_4,TRANSACTION_SPECIFICS, TRANSACTION_SPECIFICS_PARTICULARS,TYPE_OF_SUPPLY,NO_OF_UNITS,PRICE_PER_UNIT,GROSS_AMOUNT,RECEIPT_DETAILS_TYPE, TRANSACTION_ACTIONDATE,PRESENT_STATUS,SOURCE_GSTIN,DESTINATION_GSTIN,TRANSACTION_BRANCH_BANK,INSTRUMENT_NUMBER,INSTRUMENT_DATE,INVOICE_NUMBER,PERFORMA_INVOICE,CREATED_BY, CREATED_AT,PO_REFERENCE,REMARKS) 
values (v_branchId,v_orgId,v_custId,CONCAT("BULK",v_bulkId,"SRNO",v_srNo),"PAID","Accounted",1,0,v_invoice_value,v_taxName1,v_tax1,v_taxName2,v_tax2,v_taxName4,v_tax4,v_spec,v_particular_id,1,v_no_units,v_priceperunit,v_gross_amt,2,v_tran_date,1,v_sourceGstCode,v_destGstCode,branch_bank_id,v_instrumentDate,v_instrumentNo,invoiceNo,0,userId, now(),v_poReference,v_note);
END IF;    
SELECT 
    MAX(id)
INTO v_tran_id FROM
    TRANSACTION;



insert into TRANSACTION_ITEMS(BRANCH_ID,ORGANIZATION_ID,TRANSACTION_ID,TRANSACTION_SPECIFICS,TRANSACTION_SPECIFICS_PARTICULARS,NO_OF_UNITS,PRICE_PER_UNIT,GROSS_AMOUNT,ADJUSTMENT_FROM_ADVANCE,NET_AMOUNT,INVOICE_VALUE,DISCOUNT_PERCENT,TAX_DESCRIPTION,TOTAL_TAX,TAX1_ID,TAX2_ID,TAX4_ID,TAX_NAME_1,TAX_VALUE_1,TAX_RATE_1,TAX_NAME_2,TAX_VALUE_2,TAX_RATE_2,TAX_NAME_4,TAX_VALUE_4,TAX_RATE_4,PRESENT_STATUS,CREATED_BY, CREATED_AT)
values(v_branchId,v_orgId,v_tran_id,v_spec,v_particular_id,v_no_units,v_priceperunit,v_gross_amt,v_advAdj,v_invoice_value,v_invoice_value,v_discountPercent,v_taxFormula,v_tax,v_branchTax_id1,v_branchTax_id2,v_branchTax_id4,v_taxName1,v_tax1,v_taxRate1,v_taxName2,v_tax2,v_taxRate2,v_taxName4,v_tax4,v_taxRate4,1,userId, now());

IF(v_invoice_value!=v_netAmount) then
insert into TRIALBALANCE_TAXES(TRANSACTION_ID,TRANSACTION_PURPOSE,TRANSACTION_SPECIFICS,TAX_TYPE,TRANSACTION_SPECIFICS_PARTICULARS,CREDIT_AMOUNT,DEBIT_AMOUNT,DATE,BRANCH_TAXESID,BRANCH_ID,BRANCH_ORGNIZATION_ID,PRESENT_STATUS,CREATED_BY,CREATED_AT)
values(v_tran_id,1,v_spec,v_taxType1,v_particular_id,v_tax1,0,v_tran_date,v_branchTax_id1,v_branchId,v_orgId,1, userId, now());
insert into TRIALBALANCE_TAXES(TRANSACTION_ID,TRANSACTION_PURPOSE,TRANSACTION_SPECIFICS,TAX_TYPE,TRANSACTION_SPECIFICS_PARTICULARS,CREDIT_AMOUNT,DEBIT_AMOUNT,DATE,BRANCH_TAXESID,BRANCH_ID,BRANCH_ORGNIZATION_ID,PRESENT_STATUS,CREATED_BY,CREATED_AT)
values(v_tran_id,1,v_spec,v_taxType2,v_particular_id,v_tax2,0,v_tran_date,v_branchTax_id2,v_branchId,v_orgId,1, userId, now());
IF(v_branchTax_id4!=0) THEN
insert into TRIALBALANCE_TAXES(TRANSACTION_ID,TRANSACTION_PURPOSE,TRANSACTION_SPECIFICS,TAX_TYPE,TRANSACTION_SPECIFICS_PARTICULARS,CREDIT_AMOUNT,DEBIT_AMOUNT,DATE,BRANCH_TAXESID,BRANCH_ID,BRANCH_ORGNIZATION_ID,PRESENT_STATUS,CREATED_BY,CREATED_AT)
values(v_tran_id,2,v_spec,v_taxType4,v_particular_id,v_tax4,0,v_tran_date,v_branchTax_id4,v_branchId,v_orgId,1, userId, now());
END IF;
END if;
END;
ELSE
BEGIN


DECLARE serialNo INT(10) DEFAULT 0;
set yearNumber=YEAR(v_tran_date);

set monthNo=LPAD(MONTH(v_tran_date), 2, '0');
set yearNo=MOD(yearNumber,100);
SET branchName=SUBSTRING(v_branch,1,3);
SELECT OFF_INVOICE_SERIAL into off_invoice_serialNo from ORGANIZATION where ID=v_orgId;
insert into  LOG_STORED_PROC(MESSAGE,NAME) values('off_invoice_serialNo',off_invoice_serialNo);
IF(off_invoice_serialNo IS NULL) THEN
SET serialNo=1;
insert into  LOG_STORED_PROC(MESSAGE,NAME) values('inside if serialNo',serialNo);
ELSE 
SET serialNo=off_invoice_serialNo+1;
insert into  LOG_STORED_PROC(MESSAGE,NAME) values('inside else',serialNo);
END IF;
UPDATE ORGANIZATION SET OFF_INVOICE_SERIAL=serialNo where ID=v_orgId;
SET invoiceNo=UPPER(CONCAT('IV/',branchName,monthNo,yearNo,'/00',serialNo));
insert into  LOG_STORED_PROC(MESSAGE,NAME) values('invoice no',invoiceNo);
IF(v_branchTax_id4!=0) THEN
set v_tax=v_tax3+v_tax4;
set v_taxFormula=CONCAT(v_taxFormula3,'+',v_taxFormula4);
ELSE
set v_tax=v_tax3;
set v_taxFormula=CONCAT(v_taxFormula3);
END IF;
IF(v_invoice_value=0) then
SET v_invoice_value=v_netAmount-v_advAdj;
SET totalInvoiceValue=v_invoice_value;
else
SET v_invoice_value=v_invoice_value-v_advAdj;
SET totalInvoiceValue=v_invoice_value;
END IF;
insert into LOG_STORED_PROC(MESSAGE,NAME) values('Total invoice value AFTER INVOICE', totalInvoiceValue);
IF( v_receiptType LIKE 'CASH') THEN
insert into TRANSACTION(TRANSACTION_BRANCH,TRANSACTION_BRANCH_ORGANIZATION,TRANSACTION_VENDOR_CUSTOMER,TRANSACTION_REF_NUMBER,PAYMENT_STATUS,
TRANSACTION_STATUS,TRANSACTION_PURPOSE,CUSTOMER_DUE_PAYMENT,NET_AMOUNT,TAX_NAME_3,TAX_VALUE_3,TAX_NAME_4,TAX_VALUE_4,TRANSACTION_SPECIFICS, TRANSACTION_SPECIFICS_PARTICULARS,TYPE_OF_SUPPLY,NO_OF_UNITS,PRICE_PER_UNIT,GROSS_AMOUNT,RECEIPT_DETAILS_TYPE,TRANSACTION_ACTIONDATE,PRESENT_STATUS,SOURCE_GSTIN,DESTINATION_GSTIN,INVOICE_NUMBER,PERFORMA_INVOICE,CREATED_BY, CREATED_AT,PO_REFERENCE,REMARKS) 
values (v_branchId,v_orgId,v_custId,CONCAT("BULK",v_bulkId,"SRNO",v_srNo),"PAID","Accounted",1,0,v_invoice_value,v_taxName3,v_tax3,v_taxName4,v_tax4,v_spec,v_particular_id,1,v_no_units,v_priceperunit,v_gross_amt,1,v_tran_date,1,v_sourceGstCode,v_destGstCode,invoiceNo,0,userId, now(),v_poReference,v_note);
ELSE
SELECT ID into branch_bank_id FROM BRANCH_BANK_ACCOUNTS where bank_name= v_bankDetails and BRANCH_ORGANIZATION_ID=v_orgId and BRANCH_ID=v_branchId;
insert into TRANSACTION(TRANSACTION_BRANCH,TRANSACTION_BRANCH_ORGANIZATION,TRANSACTION_VENDOR_CUSTOMER,TRANSACTION_REF_NUMBER,PAYMENT_STATUS,
TRANSACTION_STATUS,TRANSACTION_PURPOSE,CUSTOMER_DUE_PAYMENT,NET_AMOUNT,TAX_NAME_3,TAX_VALUE_3,TAX_NAME_4,TAX_VALUE_4,TRANSACTION_SPECIFICS, TRANSACTION_SPECIFICS_PARTICULARS,TYPE_OF_SUPPLY,NO_OF_UNITS,PRICE_PER_UNIT,GROSS_AMOUNT,RECEIPT_DETAILS_TYPE, TRANSACTION_ACTIONDATE,PRESENT_STATUS,SOURCE_GSTIN,DESTINATION_GSTIN,TRANSACTION_BRANCH_BANK,INSTRUMENT_NUMBER,INSTRUMENT_DATE,INVOICE_NUMBER,PERFORMA_INVOICE,CREATED_BY, CREATED_AT,PO_REFERENCE,REMARKS) 
values (v_branchId,v_orgId,v_custId,CONCAT("BULK",v_bulkId,"SRNO",v_srNo),"PAID","Accounted",1,0,v_invoice_value,v_taxName3,v_tax3,v_taxName4,v_tax4,v_spec,v_particular_id,1,v_no_units,v_priceperunit,v_gross_amt,2,v_tran_date,1,v_sourceGstCode,v_destGstCode,branch_bank_id,v_instrumentDate,v_instrumentNo,invoiceNo,0,userId, now(),v_poReference,v_note);
END IF;    
SELECT 
    MAX(id)
INTO v_tran_id FROM
    TRANSACTION;

insert into TRANSACTION_ITEMS(BRANCH_ID,ORGANIZATION_ID,TRANSACTION_ID,TRANSACTION_SPECIFICS,TRANSACTION_SPECIFICS_PARTICULARS,NO_OF_UNITS,PRICE_PER_UNIT,GROSS_AMOUNT,ADJUSTMENT_FROM_ADVANCE,NET_AMOUNT,INVOICE_VALUE,DISCOUNT_PERCENT,TAX_DESCRIPTION,TOTAL_TAX,TAX3_ID,TAX4_ID,TAX_NAME_3,TAX_VALUE_3,TAX_RATE_3,TAX_NAME_4,TAX_VALUE_4,TAX_RATE_4,PRESENT_STATUS,CREATED_BY, CREATED_AT)
values(v_branchId,v_orgId,v_tran_id,v_spec,v_particular_id,v_no_units,v_priceperunit,v_gross_amt,v_advAdj,v_invoice_value,v_invoice_value,v_discountPercent,v_taxFormula,v_tax,v_branchTax_id3,v_branchTax_id4,v_taxName3,v_tax3,v_taxRate3,v_taxName4,v_tax4,v_taxRate4,1,userId, now());

IF(v_invoice_value!=v_netAmount) then
insert into TRIALBALANCE_TAXES(TRANSACTION_ID,TRANSACTION_PURPOSE,TRANSACTION_SPECIFICS,TAX_TYPE,TRANSACTION_SPECIFICS_PARTICULARS,CREDIT_AMOUNT,DEBIT_AMOUNT,DATE,BRANCH_TAXESID,BRANCH_ID,BRANCH_ORGNIZATION_ID,PRESENT_STATUS,CREATED_BY,CREATED_AT)
values(v_tran_id,1,v_spec,v_taxType3,v_particular_id,v_tax3,0,v_tran_date,v_branchTax_id3,v_branchId,v_orgId,1, userId, now());
IF(v_branchTax_id4!=0) THEN
insert into TRIALBALANCE_TAXES(TRANSACTION_ID,TRANSACTION_PURPOSE,TRANSACTION_SPECIFICS,TAX_TYPE,TRANSACTION_SPECIFICS_PARTICULARS,CREDIT_AMOUNT,DEBIT_AMOUNT,DATE,BRANCH_TAXESID,BRANCH_ID,BRANCH_ORGNIZATION_ID,PRESENT_STATUS,CREATED_BY,CREATED_AT)
values(v_tran_id,2,v_spec,v_taxType4,v_particular_id,v_tax4,0,v_tran_date,v_branchTax_id4,v_branchId,v_orgId,1, userId, now());
END IF;
END IF;
END;
END IF;

select ID into branch_deposit_key From BRANCH_DEPOSITBOX_KEY where BRANCH_ID=v_branchId and BRANCH_ORGANIZATION_ID=v_orgId;
insert into TRIALBALANCE_COAITEMS(TRANSACTION_ID,TRANSACTION_PURPOSE,TRANSACTION_SPECIFICS,TRANSACTION_SPECIFICS_PARTICULARS,CREDIT_AMOUNT,DATE,BRANCH_ID,BRANCH_ORGNIZATION_ID,PRESENT_STATUS,CREATED_BY, CREATED_AT)
values(v_tran_id,1,v_spec,v_particular_id,v_gross_amt,v_tran_date,v_branchId,v_orgId,1, userId, now());

IF( v_receiptType LIKE 'CASH') THEN
insert into TRIALBALANCE_BRANCH_CASH(TRANSACTION_ID,TRANSACTION_PURPOSE,BRANCH_ID,BRANCH_ORGNIZATION_ID,DEBIT_AMOUNT,CREDIT_AMOUNT,DATE,CASH_TYPE,PRESENT_STATUS,BRANCH_DEPOSITBOX_ID,CREATED_BY, CREATED_AT) values(v_tran_id,1,v_branchId,v_orgId,v_invoice_value,0,v_tran_date,1,1,branch_deposit_key, userId, now());
ELSE
insert into TRIALBALANCE_BRANCH_BANK(TRANSACTION_ID,TRANSACTION_PURPOSE,BRANCH_ID,BRANCH_ORGNIZATION_ID,BRANCH_BANK_ACCOUNTSID,DEBIT_AMOUNT,CREDIT_AMOUNT,DATE,
PRESENT_STATUS,CREATED_BY, CREATED_AT) values(v_tran_id,1,v_branchId,v_orgId,branch_bank_id,v_invoice_value,0,v_tran_date, 1, userId, now());
END IF;
SET v_previousSrNo=v_srNo;
SET v_previousTranId=v_tran_id;
END;
ELSE
SELECT 
    ID
INTO v_spec FROM
    SPECIFICS
WHERE
    name = v_itemCode
        AND ORGANIZATION_ID = v_orgId;  
SET v_destStateCode= SUBSTRING(v_destGstCode,1,2);

 

  
SET v_no_units=CAST(v_units as DECIMAL(10,2));
SET v_priceperunit=CAST( v_price as DECIMAL(10,2));

insert into  LOG_STORED_PROC(MESSAGE,NAME) values('Discount percent=',v_discountPrcnt);
IF(v_discountPrcnt!="") THEN
SET v_discountPercent=CAST(v_discountPrcnt as DECIMAL(10,2));
ELSE 
SET v_discountPercent=0;
END IF;
insert into  LOG_STORED_PROC(MESSAGE,NAME) values('Advance Adjustment=',v_advAdjstmnt);
IF(v_advAdjstmnt!="") THEN
SET v_advAdj=CAST(v_advAdjstmnt as DECIMAL(10,2));
ELSE
SET v_advAdj=0;
END IF;

set v_gross_amt=v_no_units*v_priceperunit;

SELECT 
    particulars_id
INTO v_particular_id FROM
    SPECIFICS
WHERE
    id = v_spec
LIMIT 1;

set v_netAmount=v_gross_amt-(v_discountPercent*v_gross_amt)/100;
set v_gross_amt=v_netAmount;
insert into  LOG_STORED_PROC(MESSAGE,NAME) values('SOURCE GST CODE',v_srcStateCode);
insert into  LOG_STORED_PROC(MESSAGE,NAME) values('DESTINATION GST CODE',v_destStateCode);
BEGIN
DECLARE v_finished_value int(10) DEFAULT NULL;
DECLARE v_taxName1 VARCHAR(100) DEFAULT NULL;
DECLARE v_taxRate1 double(10,2) DEFAULT NULL;
DECLARE v_taxType1 INT(10) DEFAULT NULL;
DECLARE v_taxName2 VARCHAR(100) DEFAULT NULL;
DECLARE v_taxRate2 double(10,2) DEFAULT NULL;
DECLARE v_taxType2 INT(10) DEFAULT NULL;
DECLARE v_taxName3 VARCHAR(100) DEFAULT NULL;
DECLARE v_taxRate3 double(10,2) DEFAULT 0;
DECLARE v_taxType3 INT(10) DEFAULT NULL;
DECLARE v_taxName4 VARCHAR(100) DEFAULT NULL;
DECLARE v_taxRate4 double(10,2) DEFAULT NULL;
DECLARE v_taxType4 INT(10) DEFAULT NULL;
DECLARE branch_tax_id INT(20) DEFAULT NULL;
DECLARE v_invoice_value DOUBLE(10,2) DEFAULT NULL;

DECLARE v_tax1 DOUBLE(10,2) DEFAULT NULL;
DECLARE v_tax2 DOUBLE(10,2) DEFAULT NULL;
DECLARE v_tax3 DOUBLE(10,2) DEFAULT NULL;
DECLARE v_tax4 DOUBLE(10,2) DEFAULT NULL;
DECLARE v_tax DOUBLE(10,2) DEFAULT NULL;
DECLARE v_taxName VARCHAR(100) DEFAULT NULL;
DECLARE v_taxRate DOUBLE(10,2) DEFAULT NULL;
DECLARE v_taxType INT(10) DEFAULT NULL;

DECLARE v_specTaxFormula int(100) DEFAULT NULL;
DECLARE v_branchTaxId int (100) DEFAULT NULL;
DECLARE v_branchTax_id1 int(100) DEFAULT NULL;
DECLARE v_branchTax_id2 int(100) DEFAULT NULL;
DECLARE v_branchTax_id3 int(100) DEFAULT NULL;
DECLARE v_branchTax_id4 int(100) DEFAULT NULL;
DECLARE v_invoiceValue VARCHAR(100) DEFAULT NULL;
DECLARE v_taxFormula  VARCHAR(200) DEFAULT NULL;
DECLARE v_taxFormula1  VARCHAR(100) DEFAULT NULL;
DECLARE v_taxFormula2  VARCHAR(200) DEFAULT NULL;
DECLARE v_taxFormula3 VARCHAR(200) DEFAULT NULL;
DECLARE v_taxFormula4  VARCHAR(100) DEFAULT NULL;


DECLARE temp_cursor_sell cursor for select ID,BRANCH_TAXESID from BRANCH_SPECIFICS_TAX_FORMULA where BRANCH_ORGANIZATION_ID=v_orgId and BRANCH_ID=v_branchId and specifics_id=v_spec;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_finished_value = 1;
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION 
BEGIN   
 GET DIAGNOSTICS CONDITION 1 @sqlstate = RETURNED_SQLSTATE, 
 @errno = MYSQL_ERRNO, @text = MESSAGE_TEXT;
SET @full_error = CONCAT("ERROR ", @errno, " (", @sqlstate, "): ", @text);
SELECT @full_error;
SELECT 'An error has occurred, operation rollbacked and the stored procedure was terminated' INTO @errorString;
 insert into  LOG_STORED_PROC(MESSAGE,NAME) values('Error SQLEXCEPTION',"insert sell on credit in taxes");
END;
OPEN temp_cursor_sell;
get_temp_sell: LOOP
FETCH temp_cursor_sell  into v_specTaxFormula,v_branchTaxId;
 IF v_finished_value = 1 THEN 
 LEAVE get_temp_sell;
 END IF;
IF(debugStoredPorc=1) THEN
  insert into LOG_STORED_PROC(MESSAGE,NAME) values('Processing data for Sr. No',"insert recPays");
END IF; 

SELECT 
    tax_name, tax_rate, tax_type, ID
INTO v_taxName , v_taxRate , v_taxType , branch_tax_id FROM
    BRANCH_TAXES
WHERE
    BRANCH_ORGANIZATION_ID = v_orgId
        AND ID = v_branchTaxId
LIMIT 1;
IF v_srcStateCode=v_destStateCode THEN
IF(v_taxName LIKE 'SGST%') THEN
set v_branchTax_id1= branch_tax_id;
set v_tax1=v_netAmount*(v_taxRate/100.00);
set v_taxName1=v_taxName;
set v_taxType1=v_taxType;
set v_taxRate1=v_taxRate;
set v_invoice_value=v_netAmount+v_tax1;
set v_taxFormula1=CONCAT(v_taxName1,'(+',v_taxRate1,'%);',v_tax1);
insert into  LOG_STORED_PROC(MESSAGE,NAME) values('TAX FORMULA1',v_taxFormula1);
END IF;
IF(v_taxName LIKE 'CGST%') THEN
set v_branchTax_id2=branch_tax_id;
set v_tax2=v_netAmount*(v_taxRate/100.00);
set v_taxName2=v_taxName;
set v_taxType2=v_taxType;
set v_taxRate2=v_taxRate;
set v_invoice_value=v_invoice_value+v_tax2;



insert into  LOG_STORED_PROC(MESSAGE,NAME) values('previous invoice value',totalInvoiceValue);

set v_taxFormula2=CONCAT(v_taxName2,'(+',v_taxRate2,'%);',v_tax2);
insert into LOG_STORED_PROC(MESSAGE,NAME) values('Total invoice value=', totalInvoiceValue);
insert into  LOG_STORED_PROC(MESSAGE,NAME) values('TAX FORMULA2',v_taxFormula2);
END IF;
IF(v_taxName LIKE 'CESS%') THEN
set v_branchTax_id4=branch_tax_id;
set v_tax4=v_netAmount*(v_taxRate/100.00);
set v_taxName4=v_taxName;
set v_taxType4=v_taxType;
set v_taxRate4=v_taxRate;
set v_invoice_value=v_invoice_value+v_tax4;

set v_taxFormula4=CONCAT(v_taxName4,'(+',v_taxRate4,'%);',v_tax4);
insert into  LOG_STORED_PROC(MESSAGE,NAME) values('TAX FORMULA4',v_taxFormula4);
END IF;
ELSE
IF(v_taxName LIKE 'IGST%') THEN
set v_branchTax_id3= branch_tax_id;
set v_tax3=v_netAmount*(v_taxRate/100.00);
set v_taxName3=v_taxName;
set v_taxType3=v_taxType;
set v_taxRate3=v_taxRate;
set v_invoice_value=v_netAmount+v_tax3;

set v_tax=v_tax3; 

set v_taxFormula3=CONCAT(v_taxName3,'(+',v_taxRate3,'%);',v_tax3);
insert into LOG_STORED_PROC(MESSAGE,NAME) values('Total invoice value=', totalInvoiceValue);
END IF;
IF(v_taxName LIKE 'CESS%') THEN
set v_branchTax_id4=branch_tax_id;
set v_tax4=v_netAmount*(v_taxRate/100.00);
set v_taxName4=v_taxName;
set v_taxType4=v_taxType;
set v_taxRate4=v_taxRate;
set v_invoice_value=v_invoice_value+v_tax4;
set v_taxFormula4=CONCAT(v_taxName4,'(+',v_taxRate4,'%);',v_tax4);
set v_tax=v_tax3+v_tax4;
insert into  LOG_STORED_PROC(MESSAGE,NAME) values('TAX FORMULA4',v_taxFormula4);
END IF;
END IF;
END LOOP get_temp_sell;
CLOSE temp_cursor_sell;
IF v_srcStateCode=v_destStateCode THEN
IF(v_branchTax_id4!=0) THEN
set v_tax=v_tax1+v_tax2+v_tax4;
set v_taxFormula=CONCAT(v_taxFormula1,'+',v_taxFormula2,'+',v_taxFormula4);
ELSE
set v_tax=v_tax1+v_tax2;
set v_taxFormula=CONCAT(v_taxFormula1,'+',v_taxFormula2);
END IF;

IF(v_invoice_value=0) then
set v_totalValue=totalInvoiceValue+v_netAmount-v_advAdj;
SET totalInvoiceValue=v_totalValue;

else
set v_totalValue=totalInvoiceValue+v_invoice_value-v_advAdj;
SET totalInvoiceValue=v_totalValue;
end if;
BEGIN



UPDATE TRANSACTION SET NET_AMOUNT=v_totalValue  WHERE ID=v_tran_id;

insert into TRANSACTION_ITEMS(BRANCH_ID,ORGANIZATION_ID,TRANSACTION_ID,TRANSACTION_SPECIFICS,TRANSACTION_SPECIFICS_PARTICULARS,NO_OF_UNITS,PRICE_PER_UNIT,GROSS_AMOUNT,ADJUSTMENT_FROM_ADVANCE,NET_AMOUNT,INVOICE_VALUE,DISCOUNT_PERCENT,TAX_DESCRIPTION,TOTAL_TAX,TAX1_ID,TAX2_ID,TAX4_ID,TAX_NAME_1,TAX_VALUE_1,TAX_RATE_1,TAX_NAME_2,TAX_VALUE_2,TAX_RATE_2,TAX_NAME_3,TAX_VALUE_3,TAX_RATE_3,TAX_NAME_4,TAX_VALUE_4,TAX_RATE_4,PRESENT_STATUS,CREATED_BY, CREATED_AT)
values(v_branchId,v_orgId,v_tran_id,v_spec,v_particular_id,v_no_units,v_priceperunit,v_gross_amt,v_advAdj,v_invoice_value,v_invoice_value,v_discountPercent,v_taxFormula,v_tax,v_branchTax_id1,v_branchTax_id2,v_branchTax_id4,v_taxName1,v_tax1,v_taxRate1,v_taxName2,v_tax2,v_taxRate2,v_taxName3,v_tax3,v_taxRate3,v_taxName4,v_tax4,v_taxRate4,1,userId, now());

IF(v_invoice_value!=0) then
insert into TRIALBALANCE_TAXES(TRANSACTION_ID,TRANSACTION_PURPOSE,TRANSACTION_SPECIFICS,TAX_TYPE,TRANSACTION_SPECIFICS_PARTICULARS,CREDIT_AMOUNT,DEBIT_AMOUNT,DATE,BRANCH_TAXESID,BRANCH_ID,BRANCH_ORGNIZATION_ID,PRESENT_STATUS,CREATED_BY,CREATED_AT)
values(v_tran_id,1,v_spec,v_taxType1,v_particular_id,v_tax1,0,v_tran_date,v_branchTax_id1,v_branchId,v_orgId,1, userId, now());
insert into TRIALBALANCE_TAXES(TRANSACTION_ID,TRANSACTION_PURPOSE,TRANSACTION_SPECIFICS,TAX_TYPE,TRANSACTION_SPECIFICS_PARTICULARS,CREDIT_AMOUNT,DEBIT_AMOUNT,DATE,BRANCH_TAXESID,BRANCH_ID,BRANCH_ORGNIZATION_ID,PRESENT_STATUS,CREATED_BY,CREATED_AT)
values(v_tran_id,1,v_spec,v_taxType2,v_particular_id,v_tax2,0,v_tran_date,v_branchTax_id2,v_branchId,v_orgId,1, userId, now());
IF(v_branchTax_id4!=0) THEN
insert into TRIALBALANCE_TAXES(TRANSACTION_ID,TRANSACTION_PURPOSE,TRANSACTION_SPECIFICS,TAX_TYPE,TRANSACTION_SPECIFICS_PARTICULARS,CREDIT_AMOUNT,DEBIT_AMOUNT,DATE,BRANCH_TAXESID,BRANCH_ID,BRANCH_ORGNIZATION_ID,PRESENT_STATUS,CREATED_BY,CREATED_AT)
values(v_tran_id,2,v_spec,v_taxType4,v_particular_id,v_tax4,0,v_tran_date,v_branchTax_id4,v_branchId,v_orgId,1, userId, now());
END IF;
END IF;
END;
ELSE
BEGIN
IF(v_branchTax_id4!=0) THEN
set v_tax=v_tax3+v_tax4;
set v_taxFormula=CONCAT(v_taxFormula3,'+',v_taxFormula4);
ELSE
set v_tax=v_tax3;
set v_taxFormula=CONCAT(v_taxFormula3);
END IF;
IF(v_invoice_value=0) then
set v_totalValue=totalInvoiceValue+v_netAmount-v_advAdj;
SET totalInvoiceValue=v_totalValue;
else
set v_totalValue=totalInvoiceValue+v_invoice_value-v_advAdj;
SET totalInvoiceValue=v_totalValue;
END IF;
UPDATE TRANSACTION SET NET_AMOUNT=v_totalValue  WHERE ID=v_tran_id;

IF(v_invoice_value!=0) then
insert into TRIALBALANCE_TAXES(TRANSACTION_ID,TRANSACTION_PURPOSE,TRANSACTION_SPECIFICS,TAX_TYPE,TRANSACTION_SPECIFICS_PARTICULARS,CREDIT_AMOUNT,DEBIT_AMOUNT,DATE,BRANCH_TAXESID,BRANCH_ID,BRANCH_ORGNIZATION_ID,PRESENT_STATUS,CREATED_BY,CREATED_AT)
values(v_tran_id,1,v_spec,v_taxType3,v_particular_id,v_tax3,0,v_tran_date,v_branchTax_id3,v_branchId,v_orgId,1, userId, now());
IF(v_branchTax_id4!=0) THEN
insert into TRIALBALANCE_TAXES(TRANSACTION_ID,TRANSACTION_PURPOSE,TRANSACTION_SPECIFICS,TAX_TYPE,TRANSACTION_SPECIFICS_PARTICULARS,CREDIT_AMOUNT,DEBIT_AMOUNT,DATE,BRANCH_TAXESID,BRANCH_ID,BRANCH_ORGNIZATION_ID,PRESENT_STATUS,CREATED_BY,CREATED_AT)
values(v_tran_id,2,v_spec,v_taxType4,v_particular_id,v_tax4,0,v_tran_date,v_branchTax_id4,v_branchId,v_orgId,1, userId, now());
END IF;
insert into TRANSACTION_ITEMS(BRANCH_ID,ORGANIZATION_ID,TRANSACTION_ID,TRANSACTION_SPECIFICS,TRANSACTION_SPECIFICS_PARTICULARS,NO_OF_UNITS,PRICE_PER_UNIT,GROSS_AMOUNT,ADJUSTMENT_FROM_ADVANCE,NET_AMOUNT,INVOICE_VALUE,DISCOUNT_PERCENT,TAX_DESCRIPTION,TOTAL_TAX,TAX3_ID,TAX4_ID,TAX_NAME_3,TAX_VALUE_3,TAX_RATE_3,TAX_NAME_4,TAX_VALUE_4,TAX_RATE_4,PRESENT_STATUS,CREATED_BY, CREATED_AT)
values(v_branchId,v_orgId,v_tran_id,v_spec,v_particular_id,v_no_units,v_priceperunit,v_gross_amt,v_advAdj,v_invoice_value,v_invoice_value,v_discountPercent,v_taxFormula,v_tax,v_branchTax_id3,v_branchTax_id4,v_taxName3,v_tax3,v_taxRate3,v_taxName4,v_tax4,v_taxRate4,1,userId, now());
ELSE
insert into TRANSACTION_ITEMS(BRANCH_ID,ORGANIZATION_ID,TRANSACTION_ID,TRANSACTION_SPECIFICS,TRANSACTION_SPECIFICS_PARTICULARS,NO_OF_UNITS,PRICE_PER_UNIT,GROSS_AMOUNT,ADJUSTMENT_FROM_ADVANCE,NET_AMOUNT,INVOICE_VALUE,DISCOUNT_PERCENT,TAX_DESCRIPTION,TOTAL_TAX,TAX3_ID,TAX4_ID,TAX_NAME_3,TAX_VALUE_3,TAX_RATE_3,TAX_NAME_4,TAX_VALUE_4,TAX_RATE_4,PRESENT_STATUS,CREATED_BY, CREATED_AT)
values(v_branchId,v_orgId,v_tran_id,v_spec,v_particular_id,v_no_units,v_priceperunit,v_gross_amt,v_advAdj,v_netAmount,v_netAmount,v_discountPercent,v_taxFormula,v_tax,v_branchTax_id3,v_branchTax_id4,v_taxName3,v_tax3,v_taxRate3,v_taxName4,v_tax4,v_taxRate4,1,userId, now());
END IF;
END;
END IF;

select ID into branch_deposit_key From BRANCH_DEPOSITBOX_KEY where BRANCH_ID=v_branchId and BRANCH_ORGANIZATION_ID=v_orgId;
insert into TRIALBALANCE_COAITEMS(TRANSACTION_ID,TRANSACTION_PURPOSE,TRANSACTION_SPECIFICS,TRANSACTION_SPECIFICS_PARTICULARS,CREDIT_AMOUNT,DATE,BRANCH_ID,BRANCH_ORGNIZATION_ID,PRESENT_STATUS,CREATED_BY, CREATED_AT)
values(v_tran_id,1,v_spec,v_particular_id,v_gross_amt,v_tran_date,v_branchId,v_orgId,1, userId, now());

IF( v_receiptType LIKE 'CASH') THEN
UPDATE TRIALBALANCE_BRANCH_CASH SET DEBIT_AMOUNT=v_totalValue  WHERE TRANSACTION_ID=v_tran_id;
ELSE
UPDATE TRIALBALANCE_BRANCH_BANK SET DEBIT_AMOUNT=v_totalValue  WHERE TRANSACTION_ID=v_tran_id;
END IF;

END;
END IF;
END LOOP get_temp;
CLOSE temp_cursor;

END$$
DELEMITER;