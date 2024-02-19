DELIMITER $$
DROP PROCEDURE IF EXISTS insert_PayVendor_transactions;
CREATE PROCEDURE `insert_PayVendor_transactions`(IN userId int(40),IN debugStoredPorc int(1))
BEGIN
-- from table
DECLARE v_finished INTEGER DEFAULT 0;
DECLARE v_srNo varchar(100) DEFAULT "";
DECLARE v_date varchar(30) DEFAULT "";
DECLARE v_branch varchar(100) DEFAULT "";
DECLARE v_vend varchar(100) DEFAULT "";
DECLARE v_inv varchar(250) DEFAULT "";
DECLARE v_payment varchar(100) DEFAULT "";
DECLARE v_tax varchar(100) DEFAULT "";
DECLARE v_receipt varchar(20) DEFAULT "";
DECLARE v_bank varchar(10) DEFAULT "";
DECLARE v_instNo varchar(10) DEFAULT "";
DECLARE v_instDate varchar(10) DEFAULT "";
DECLARE v_receiptDet varchar(100) DEFAULT "";
DECLARE v_note varchar(100) DEFAULT "";
DECLARE v_inventory varchar(100) DEFAULT"";
-- used in procedure
DECLARE yearNumber INT(20) DEFAULT 0;
DECLARE monthNo VARCHAR(20) DEFAULT "";
DECLARE yearNo INT(20) DEFAULT 0;
DECLARE branchName VARCHAR(20) DEFAULT 0;
DECLARE off_invoice_serialNo INT(10) DEFAULT 0;
DECLARE invoiceNo VARCHAR(100) DEFAULT "";
DECLARE v_instrument_date date DEFAULT null;
DECLARE v_branchId int (10) DEFAULT 0;
DECLARE v_orgId int (10) DEFAULT 0;
DECLARE v_vendId int(10) DEFAULT 0;
DECLARE v_bulkId int(100) DEFAULT 0;
DECLARE v_pend_tran int(100) DEFAULT 0;
DECLARE v_paid_inv_no varchar(50) DEFAULT 0;
DECLARE v_spec int(100) DEFAULT NULL;
DECLARE v_particular_id int(100) DEFAULT NULL;
DECLARE v_units double(10,2) DEFAULT 0;
DECLARE v_priceperunit double(10,2) DEFAULT 0;
DECLARE v_gross_amt double(10,2) DEFAULT 0;
DECLARE v_payment_due double(10,2) DEFAULT 0;
DECLARE v_opening_bal double(10,2) DEFAULT 0;
DECLARE v_receipt_id int(1) DEFAULT 0;
DECLARE v_tran_id int(100) DEFAULT 0;
DECLARE v_tb_credit double(10,2) DEFAULT 0;
DECLARE v_br_depositbox int(10) DEFAULT NULL;
DECLARE v_tran_date date DEFAULT null;
DECLARE branch_bank_id INT(20) DEFAULT 0;
-- pending transaction i.e. sell on credit update variables

DECLARE vpend_vend_netpay double(10,2) DEFAULT 0;
DECLARE vpend_vend_duepay double(10,2) DEFAULT 0;
DECLARE vpend_pay_status varchar(20) DEFAULT "";


DEClARE temp_cursor CURSOR FOR 
 select * from temp_pay_vend_trans;


DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_finished = 1;
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION 
BEGIN   
 GET DIAGNOSTICS CONDITION 1 @sqlstate = RETURNED_SQLSTATE, 
 @errno = MYSQL_ERRNO, @text = MESSAGE_TEXT;
SET @full_error = CONCAT("ERROR ", @errno, " (", @sqlstate, "): ", @text);
SELECT @full_error;
SELECT 'An error has occurred, operation rollbacked and the stored procedure was terminated' INTO @errorString;
 insert into  LOG_STORED_PROC(MESSAGE,NAME) values('Error SQLEXCEPTION',"insert pay vendor");
END;
  	
SET @iuserId=userId;

-- get unique bulk id from mysql seq
INSERT INTO pay_vend_seq(counter) VALUES (NULL);
SELECT MAX(counter) INTO v_bulkId FROM pay_vend_seq;

-- get orgid and branch from user
select  BRANCH_ORGANIZATION_ID into v_orgId from USERS where id=userId;

OPEN temp_cursor;
get_temp: LOOP
 FETCH temp_cursor INTO v_srNo,v_date,v_branch,v_vend,v_inv,v_payment,v_receipt,v_bank,v_instNo, v_instDate,v_receiptDet,v_note;
 IF v_finished = 1 THEN 
 LEAVE get_temp;
 END IF;
 IF(debugStoredPorc=1) THEN
  insert into LOG_STORED_PROC(MESSAGE,NAME) values('Processing data for Sr. No',"insert pay vendor");
END IF;  
insert into LOG_STORED_PROC(MESSAGE,NAME) values('Reciept number=',v_receipt);
 IF(v_date IS NOT NULL) THEN
  -- SELECT STR_TO_DATE(v_date, '%m/%d/%Y') into v_tran_date; 
   SELECT CONVERT(v_date, date) into v_tran_date;
 ELSE
  SET v_tran_date = CURDATE(); 
 END IF;
 IF(v_branch IS NOT NULL) THEN
  select  ID into v_branchId from BRANCH where name=v_branch and ORGANIZATION_ID=v_orgId;  
END IF;  
IF(v_vend IS NOT NULL) THEN
  select  ID,OPENING_BALANCE into v_vendId,v_opening_bal from VENDOR where name=v_vend and BRANCH_ORGANIZATION_ID=v_orgId and type=1;  
END IF;
IF(v_receipt IS NOT NULL) THEN
	IF(v_receipt LIKE 'CASH') THEN
		SET v_receipt_id=1;
    ELSE  
		SET v_receipt_id=2;
    END IF;
END IF; 
IF( v_receipt LIKE 'BANK') THEN
 IF(v_instDate IS NOT NULL) THEN
  SELECT CONVERT(v_instDate, date) into v_instrument_date; 
 ELSE
  SET v_instrument_date = CURDATE(); 
 END IF;
 END IF;   
SET v_tb_credit = v_payment;
SELECT SUBSTRING_INDEX(v_inv, '-', 1 ) into v_inventory;
insert into LOG_STORED_PROC(MESSAGE,NAME) values('Organization id=',v_orgId);
insert into LOG_STORED_PROC(MESSAGE,NAME) values('Branch id=',v_branchId);
IF(v_inventory IS NOT NULL) THEN
 IF(v_inventory = 'OPENING BALANCE') THEN
	  SET v_paid_inv_no=-1; 
    SET v_gross_amt = v_opening_bal;
    SET v_payment_due = v_opening_bal - v_payment;    
    update VENDOR set OPENING_BALANCE=v_payment_due where id=v_vendId and BRANCH_ORGANIZATION_ID=v_orgId and type=1;
 ELSE
	select ID,TRANSACTION_SPECIFICS,NO_OF_UNITS,PRICE_PER_UNIT,GROSS_AMOUNT,VENDOR_NET_PAYMENT,VENDOR_DUE_PAYMENT,TRANSACTION_REF_NUMBER 
  into v_pend_tran,v_spec,v_units,v_priceperunit,v_gross_amt,vpend_vend_netpay,vpend_vend_duepay,v_paid_inv_no
  from TRANSACTION where TRANSACTION_REF_NUMBER=v_inventory and TRANSACTION_BRANCH_ORGANIZATION=v_orgId and TRANSACTION_PURPOSE=4; 
  
  
	insert into LOG_STORED_PROC(MESSAGE,NAME) values('Transcation ref number=',v_inventory);
	insert into LOG_STORED_PROC(MESSAGE,NAME) values('specifics=',v_spec);
  select particulars_id into v_particular_id from SPECIFICS where id = v_spec;
  
      IF(v_payment IS NOT NULL) THEN
        IF(vpend_vend_duepay IS NOT NULL) THEN
          SET vpend_vend_duepay = vpend_vend_duepay - v_payment;
        ELSE
          SET vpend_vend_duepay = v_gross_amt - v_payment;
        END IF;  
        IF (vpend_vend_duepay > 0) THEN
          SET vpend_pay_status = "PARTLY-PAID";
        ELSE  
          SET vpend_pay_status = "PAID";
        END IF;
        IF(vpend_vend_netpay IS NOT NULL) THEN
          SET vpend_vend_netpay = vpend_vend_netpay + v_payment;
        ELSE
          SET vpend_vend_netpay = v_payment;
        END IF;        
      END IF;  
            
 END IF;

 update TRANSACTION set VENDOR_NET_PAYMENT=vpend_vend_netpay,VENDOR_DUE_PAYMENT=vpend_vend_duepay,
                        PAYMENT_STATUS =vpend_pay_status where id=v_pend_tran; 
 
END IF;    
insert into LOG_STORED_PROC(MESSAGE,NAME) values('insert transaction=',v_pend_tran);
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
IF(v_receipt LIKE 'CASH') THEN
insert into TRANSACTION(TRANSACTION_BRANCH,TRANSACTION_BRANCH_ORGANIZATION,TRANSACTION_VENDOR_CUSTOMER,TRANSACTION_REF_NUMBER,
TRANSACTION_STATUS,PAID_INVOICE_REF_NUMBER,TRANSACTION_PURPOSE,PAYMENT_STATUS,INVOICE_NUMBER,NET_AMOUNT,RECEIPT_DETAILS_TYPE,
VENDOR_DUE_PAYMENT,VENDOR_NET_PAYMENT, TRANSACTION_SPECIFICS, TRANSACTION_SPECIFICS_PARTICULARS,NO_OF_UNITS,PRICE_PER_UNIT,GROSS_AMOUNT, TRANSACTION_ACTIONDATE,PRESENT_STATUS,CREATED_BY, CREATED_AT,REMARKS) 
values (v_branchId,v_orgId,v_vendId,CONCAT("BULK",v_bulkId,"_SRNO",v_srNo),"Accounted",v_paid_inv_no,7,"PAID",invoiceNo,v_payment,v_receipt_id,
0,v_payment,v_spec,v_particular_id,v_units,v_priceperunit,v_gross_amt,v_tran_date, 1,userId, now(),v_note);
ELSE 
SELECT ID into branch_bank_id FROM BRANCH_BANK_ACCOUNTS where bank_name= v_bank and BRANCH_ORGANIZATION_ID=v_orgId and BRANCH_ID=v_branchId;
insert into TRANSACTION(TRANSACTION_BRANCH,TRANSACTION_BRANCH_ORGANIZATION,TRANSACTION_VENDOR_CUSTOMER,TRANSACTION_REF_NUMBER,
TRANSACTION_STATUS,PAID_INVOICE_REF_NUMBER,TRANSACTION_PURPOSE,PAYMENT_STATUS,INVOICE_NUMBER,NET_AMOUNT,RECEIPT_DETAILS_TYPE,
VENDOR_DUE_PAYMENT,VENDOR_NET_PAYMENT, TRANSACTION_SPECIFICS, TRANSACTION_SPECIFICS_PARTICULARS,NO_OF_UNITS,PRICE_PER_UNIT,GROSS_AMOUNT, TRANSACTION_ACTIONDATE,PRESENT_STATUS,TRANSACTION_BRANCH_BANK,INSTRUMENT_NUMBER,INSTRUMENT_DATE,CREATED_BY, CREATED_AT,REMARKS) 
values (v_branchId,v_orgId,v_vendId,CONCAT("BULK",v_bulkId,"_SRNO",v_srNo),"Accounted",v_paid_inv_no,7,"PAID",invoiceNo,v_payment,v_receipt_id,
0,v_payment,v_spec,v_particular_id,v_units,v_priceperunit,v_gross_amt,v_tran_date, 1,branch_bank_id,v_instrument_date,v_instNo,userId, now(),v_note);
END IF;
END;
insert into LOG_STORED_PROC(MESSAGE,NAME) values('Bulk id=',CONCAT("BULK",v_bulkId,"_SRNO",v_srNo));
select max(id) into v_tran_id from Transaction;



insert into TRIALBALANCE_VENDOR_CUSTOMER(TRANSACTION_ID,TRANSACTION_PURPOSE,TRANSACTION_SPECIFICS,TRANSACTION_SPECIFICS_PARTICULARS,BRANCH_ID,
BRANCH_ORGNIZATION_ID,VENDOR_ID,VENDOR_TYPE,DEBIT_AMOUNT,CREDIT_AMOUNT,CLOSING_BALANCE,DATE,PRESENT_STATUS,CREATED_BY, CREATED_AT) 
values(v_tran_id,7,v_spec,v_particular_id,v_branchId,v_orgId,v_vendId,1,v_tb_credit,0,0,v_tran_date, 1,userId, now());


IF(v_receipt IS NOT NULL) THEN
	IF(v_receipt LIKE 'CASH') THEN
	insert into LOG_STORED_PROC(MESSAGE,NAME) values('receipt=',v_receipt);
    select id into v_br_depositbox from BRANCH_DEPOSITBOX_KEY where BRANCH_ID=v_branchId and BRANCH_ORGANIZATION_ID=v_orgId;
    insert into TRIALBALANCE_BRANCH_CASH(TRANSACTION_ID,TRANSACTION_PURPOSE,BRANCH_DEPOSITBOX_ID,BRANCH_ID,BRANCH_ORGNIZATION_ID,CREDIT_AMOUNT,DEBIT_AMOUNT,DATE,CASH_TYPE,PRESENT_STATUS,CREATED_BY, CREATED_AT)
    values(v_tran_id,7,v_br_depositbox,v_branchId,v_orgId,v_payment,0,v_tran_date,1,1,userId, now());
  ELSE
    insert into LOG_STORED_PROC(MESSAGE,NAME) values('receipt=',v_receipt);
    select id into v_br_depositbox from BRANCH_BANK_ACCOUNTS where BRANCH_ID=v_branchId and BRANCH_ORGANIZATION_ID=v_orgId;
    insert into TRIALBALANCE_BRANCH_BANK(TRANSACTION_ID,TRANSACTION_PURPOSE,BRANCH_BANK_ACCOUNTSID,BRANCH_ID,BRANCH_ORGNIZATION_ID,CREDIT_AMOUNT,DEBIT_AMOUNT,DATE,PRESENT_STATUS,CREATED_BY, CREATED_AT)
    values(v_tran_id,5,v_br_depositbox,v_branchId,v_orgId,v_payment,0,v_tran_date, 1,userId, now());
  END IF;  
END IF;
insert into LOG_STORED_PROC(MESSAGE,NAME) values('payment mode=',v_receipt);
insert into LOG_STORED_PROC(MESSAGE,NAME) values('payment=',v_payment);

 
 END LOOP get_temp;
CLOSE temp_cursor;
END$$
DELEMITER;