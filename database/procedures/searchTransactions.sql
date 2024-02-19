DROP PROCEDURE IF EXISTS search_transactions;
DELIMITER //
	CREATE PROCEDURE `search_transactions`(
		IN userId int(40), 
	  	IN orgId int(40), 
	  	IN txnUserType INT(2), 
	  	IN txnQuestion varchar(2000), 
	  	IN searchCategory int(50), 
	  	IN searchItems varchar(5000), 
	  	IN searchTxnStatus varchar(200), 
	  	IN searchTransactionRefNumber varchar(100), 
	  	IN searchTxnBranch varchar(2000), 
	  	IN searchTxnProjects varchar(2000), 
	  	IN searchAmountRanseLimitFrom double, 
	  	IN searchAmountRanseLimitTo double, 
	  	IN searchTxnWithWithoutDoc int(2), 
	  	IN searchTxnPyMode varchar(3), 
	  	IN searchTxnWithWithoutRemarks int(2), 
	  	IN searchTxnException varchar(10), 
	  	IN searchVendors varchar(5000), 
	  	IN searchCustomers varchar(5000), 
	  	IN txnFmDate varchar(50), 
	  	IN txnToDt varchar(50), 
	  	IN debugStoredPorc int(1), 
	  	IN userEmail varchar(255), 
	  	IN fromRecord int(5), 
	  	IN perPage int(5), 
	  	out totalRecords int
	) 
	BEGIN DECLARE v_finished INTEGER DEFAULT 0;
		DECLARE v_roleId int(10) DEFAULT 0;
		DECLARE v_where_creator varchar(1000) DEFAULT "";
		DECLARE v_where_approver varchar(2000) DEFAULT "";
		DECLARE v_where_auditor varchar(2000) DEFAULT "";
		DECLARE v_wh_txnUserType varchar(200) DEFAULT "";
		DECLARE v_wh_pv_txnUserType varchar(200) DEFAULT "";
		DECLARE v_wh_txnQuestion varchar(2000) DEFAULT "";
		DECLARE v_wh_searchCategory varchar(200) DEFAULT "";
		DECLARE v_wh_pv_searchCategory varchar(200) DEFAULT "";
		DECLARE v_wh_pv_searchCatTable varchar(50) DEFAULT "";
		DECLARE v_wh_searchItems varchar(5000) DEFAULT "";
		DECLARE v_wh_pv_searchItems varchar(5000) DEFAULT "";
		DECLARE v_wh_searchTxnStatus varchar(200) DEFAULT "";
		DECLARE v_wh_searchTransactionRefNumber varchar(200) DEFAULT "";
		DECLARE v_wh_searchTxnBranch varchar(2000) DEFAULT "";
		DECLARE v_wh_searchTxnProjects varchar(2000) DEFAULT "";
		DECLARE v_wh_searchAmountRanseLimitFrom varchar(200) DEFAULT "";
		DECLARE v_wh_searchAmountRanseLimitTo varchar(200) DEFAULT "";
		DECLARE v_wh_searchTxnWithWithoutDoc varchar(200) DEFAULT "";
		DECLARE v_wh_searchTxnPyMode varchar(200) DEFAULT "";
		DECLARE v_wh_pv_searchTxnPyMode varchar(200) DEFAULT "";
		DECLARE v_wh_searchTxnWithWithoutRemarks varchar(200) DEFAULT "";
		DECLARE v_wh_searchTxnException varchar(400) DEFAULT "";
		DECLARE v_wh_searchVendors varchar(5000) DEFAULT "";
		DECLARE v_wh_searchCustomers varchar(5000) DEFAULT "";
		DECLARE v_wh_pv_searchCustomers varchar(5000) DEFAULT "";
		DECLARE v_wh_txnFmDate varchar(200) DEFAULT "";
		DECLARE tr_query varchar(10000) DEFAULT "";
		DECLARE tr_queryCount varchar(10000) DEFAULT "";
		DECLARE set_pv_query int(2) DEFAULT 0;
		DECLARE set_txnpur_query int(2) DEFAULT 0;
		DECLARE set_pvtxn_query int(2) DEFAULT 0;
		DECLARE set_bomtxn_query int(2) DEFAULT 0;
		DECLARE set_pv_bom_txn_query int(2) DEFAULT 0;
		DECLARE set_txnpur_bom_txn_query int(2) DEFAULT 0;
		DECLARE v_wh_pv_searchTxnStatus varchar(200) DEFAULT "";
		DECLARE v_wh_pv_searchTransactionRefNumber varchar(200) DEFAULT "";
		DECLARE v_wh_pv_searchTxnBranch varchar(2000) DEFAULT "";
		DECLARE v_wh_pv_searchTxnProjects varchar(2000) DEFAULT "";
		DECLARE v_wh_pv_searchAmountRanseLimitFrom varchar(200) DEFAULT "";
		DECLARE v_wh_pv_searchAmountRanseLimitTo varchar(200) DEFAULT "";
		DECLARE v_wh_pv_searchTxnWithWithoutDoc varchar(200) DEFAULT "";
		DECLARE v_wh_pv_searchTxnWithWithoutRemarks varchar(200) DEFAULT "";
		DECLARE v_wh_pv_txnFmDate varchar(200) DEFAULT "";
		DECLARE v_wh_bom_searchTxnStatus varchar(200) DEFAULT "";
		DECLARE v_wh_bom_searchTransactionRefNumber varchar(200) DEFAULT "";
		DECLARE v_wh_bom_searchTxnBranch varchar(2000) DEFAULT "";
		DECLARE v_wh_bom_txnFmDate varchar(200) DEFAULT "";
		DECLARE v_wh_bom_searchCustomers varchar(5000) DEFAULT "";
		DECLARE v_wh_bom_txnQuestion varchar(2000) DEFAULT "";
		DECLARE pv_query varchar(2000) DEFAULT "";
		DECLARE bom_query varchar(2000) DEFAULT "";
		DECLARE limitStmt varchar(256) DEFAULT "";
		DECLARE txnQUESTION20 INT(2);
		DECLARE txnQuestionOther INT(2);
		DECLARE txnQUESTION_BOM INT(2);
		DECLARE ONLY_AUDITOR INT(2) DEFAULT 0;
		DECLARE ROLE_COUNT INT(2) DEFAULT 0;
		DECLARE user_roles_cursor CURSOR FOR 
			select ROLE_ID 
			from ROLE_has_USERS 
			where USERS_ID = userId 
			and ROLE_ID NOT IN (1, 2, 9) 
			ORDER BY ROLE_ID;
		DECLARE CONTINUE HANDLER FOR NOT FOUND 
		SET v_finished = 1;
		SET @iuserId = userId;
		SET @iorgId = orgId;
		SET txnQUESTION20 = (
		    SELECT COUNT(*) 
		    FROM DUAL 
		    WHERE txnQuestion LIKE '%20%'
		  );
		SET txnQuestionOther = (
		    SELECT char_length(txnQuestion) 
		    FROM DUAL
		  );
		SET txnQUESTION_BOM = (
		    SELECT COUNT(*) 
		    FROM DUAL 
		    WHERE (txnQuestion LIKE '%39%') || (txnQuestion LIKE '%40%') || (txnQuestion LIKE '%41%') || (txnQuestion LIKE '%42%')
		  );
		OPEN user_roles_cursor;
			IF (debugStoredPorc = 1) THEN 
				insert into LOG_STORED_PROC(MESSAGE, NAME) 
				values ('Getting user roles', "Search");
			END IF;
			get_roles : LOOP FETCH user_roles_cursor INTO v_roleId;
				IF v_finished = 1 THEN 
					LEAVE get_roles;
				END IF;
				IF v_roleId = 3 THEN 
					SET v_where_creator = CONCAT(" and CREATED_BY = ", @iuserId);
					SET ROLE_COUNT = ROLE_COUNT + 1;
				ELSEIF v_roleId = 4 THEN 
					IF (v_where_creator = "") THEN 
						SET v_where_approver = CONCAT(" and (APPROVER_ACTION_BY =", userId, " or APPROVER_EMAILS like '%", userEmail, "%' or SELECTED_ADDITIONAL_APPROVER like '%", userEmail, "%') ");
					ELSE 
						SET v_where_creator = "";
						SET v_where_approver = CONCAT(" and (CREATED_BY = ", @iuserId, " or APPROVER_ACTION_BY =", userId, " or APPROVER_EMAILS like '%", userEmail, "%' or SELECTED_ADDITIONAL_APPROVER like '%", userEmail, "%') ");
					END IF;
					SET ROLE_COUNT = ROLE_COUNT + 1;
				ELSEIF v_roleId = 5 THEN 
					SET v_where_creator = "";
					SET v_where_approver = "";
					SET ROLE_COUNT = ROLE_COUNT + 1;
				ELSEIF v_roleId = 6 THEN 
					SET v_where_creator = "";
					SET v_where_approver = "";
					SET ROLE_COUNT = ROLE_COUNT + 1;
				ELSEIF v_roleId = 7 THEN 
					SET v_where_creator = "";
					SET v_where_approver = "";
					SET v_where_auditor = CONCAT(" and TRANSACTION_STATUS = \'Accounted\' ", "");
					IF (ROLE_COUNT = 1) THEN 
						SET ONLY_AUDITOR = 1;
					END IF;
				END IF;
			END LOOP get_roles;
		CLOSE user_roles_cursor;
		IF (debugStoredPorc = 1) THEN 
			insert into LOG_STORED_PROC(MESSAGE, NAME) 
			values ('Got user roles', "Search");
		END IF;
		IF (txnUserType != 0) THEN 
			IF (txnUserType = 1) THEN 
				SET v_wh_txnUserType = CONCAT(" and CREATED_BY = ", userId);
				SET v_wh_pv_txnUserType = CONCAT(" and p.CREATED_BY = ", userId);
			ELSEIF (txnUserType = 2) THEN 
				SET v_wh_txnUserType = CONCAT(" and (APPROVER_ACTION_BY =", userId, " or APPROVER_EMAILS like '%", userEmail, "%' or SELECTED_ADDITIONAL_APPROVER like '%", userEmail, "%') ");
				SET v_wh_pv_txnUserType = CONCAT(" and (p.APPROVER_ACTION_BY =", userId, " or p.APPROVER_EMAILS like '%", userEmail, "%' or p.SELECTED_ADDITIONAL_APPROVER like '%", userEmail, "%') ");
			END IF;
		END IF;
		IF (txnQuestion != "") THEN 
			SET v_wh_txnQuestion = CONCAT(" and TRANSACTION_PURPOSE in( ", txnQuestion, ")");
			SET v_wh_bom_txnQuestion = CONCAT(" and TRANSACTION_PURPOSE in( ", txnQuestion, ")");
			IF (txnQuestion20 = 1) THEN 
				IF (txnQuestionOther > 2) THEN 
					SET set_pvtxn_query = 1;
				ELSE 
					SET set_pv_query = 1;
				END IF;
			ELSE 
				SET set_txnpur_query = 1;
			END IF;
		ELSE 
			SET set_pvtxn_query = 1;
		END IF;
		IF (txnQUESTION_BOM = 1) THEN 
			SET set_bomtxn_query = 1;
		END IF;
		IF (txnQUESTION_BOM = 1 AND set_pvtxn_query = 1) THEN 
			SET set_pv_bom_txn_query = 1;
		END IF;
		IF (txnQUESTION_BOM = 1 AND set_txnpur_query = 1) THEN 
			SET set_txnpur_bom_txn_query = 1;
		END IF;
		IF (searchCategory != 0) THEN 
			SET v_wh_searchCategory = CONCAT(" and TRANSACTION_SPECIFICS_PARTICULARS = ", searchCategory);
			SET v_wh_pv_searchCategory = CONCAT(" and d.HEAD_ID = s.ID and s.PARTICULARS_ID = ", searchCategory);
			SET v_wh_pv_searchCatTable = ", specifics s";
		END IF;
		IF (searchItems != "") THEN 
			SET v_wh_searchItems = CONCAT(" and TRANSACTION_SPECIFICS IN (", searchItems, ")");
			SET v_wh_pv_searchItems = CONCAT(" and d.HEAD_ID IN (", searchItems, ")");
		END IF;
		IF (ONLY_AUDITOR != 1) THEN 
			IF (searchTxnStatus != "") THEN 
				insert into LOG_STORED_PROC(`message`, `name`) 
				values (searchTxnStatus, "searchTxnStatus");
				SET v_wh_searchTxnStatus = CONCAT(" and TRANSACTION_STATUS IN(", searchTxnStatus, ")");
				SET v_wh_pv_searchTxnStatus = CONCAT(" and p.TRANSACTION_STATUS IN(", searchTxnStatus, ")");
				SET v_wh_bom_searchTxnStatus = CONCAT(" and TRANSACTION_STATUS IN(", searchTxnStatus, ")");
			END IF;
		ELSE 
			SET v_wh_searchTxnStatus = CONCAT(" and TRANSACTION_STATUS ='ACCOUNTED' ");
			SET v_wh_pv_searchTxnStatus = CONCAT(" and p.TRANSACTION_STATUS ='ACCOUNTED' ");
			SET v_wh_bom_searchTxnStatus = CONCAT(" and TRANSACTION_STATUS ='ACCOUNTED' ");
		END IF;
		IF (searchTransactionRefNumber != "") THEN 
			SET v_wh_searchTransactionRefNumber = CONCAT(" and (TRANSACTION_REF_NUMBER = '", searchTransactionRefNumber, "' OR INVOICE_NUMBER = '", searchTransactionRefNumber, "') ");
			SET v_wh_pv_searchTransactionRefNumber = CONCAT(" and p.TRANSACTION_REF_NUMBER = '", searchTransactionRefNumber, "' ");
			SET v_wh_bom_searchTransactionRefNumber = CONCAT(" and TRANSACTION_REF_NUMBER = '", searchTransactionRefNumber, "' ");
		END IF;
		IF (searchTxnBranch != "") THEN 
			SET v_wh_searchTxnBranch = CONCAT(" and TRANSACTION_BRANCH IN(", searchTxnBranch, ")");
			SET v_wh_pv_searchTxnBranch = CONCAT(" and p.DEBIT_BRANCH IN( ", searchTxnBranch, ")");
			SET v_wh_bom_searchTxnBranch = CONCAT(" and BRANCH_ID IN( ", searchTxnBranch, ")");
		END IF;
		IF (searchTxnProjects != "") THEN 
			SET v_wh_searchTxnProjects = CONCAT(" and TRANSACTION_PROJECT IN(", searchTxnProjects, ")");
			SET v_wh_pv_searchTxnProjects = CONCAT(" and d.PROJECT_ID IN(", searchTxnProjects, ")");
		END IF;
		IF (debugStoredPorc = 1) THEN 
			insert into LOG_STORED_PROC(MESSAGE, NAME) 
			values ('searchTxnProjects', "Search");
		END IF;
		IF (searchAmountRanseLimitFrom != 0) THEN 
			SET v_wh_searchAmountRanseLimitFrom = CONCAT(" and NET_AMOUNT >= ", searchAmountRanseLimitFrom);
			SET v_wh_pv_searchAmountRanseLimitFrom = CONCAT(" and p.TOTAL_DEBIT_AMOUNT >= ", searchAmountRanseLimitFrom);
		END IF;
		IF (searchAmountRanseLimitTo != 0) THEN 
			SET v_wh_searchAmountRanseLimitTo = CONCAT(" and NET_AMOUNT <= ", searchAmountRanseLimitTo);
			SET v_wh_pv_searchAmountRanseLimitTo = CONCAT(" and p.TOTAL_DEBIT_AMOUNT <= ", searchAmountRanseLimitTo);
		END IF;
		IF (searchTxnWithWithoutDoc = 1) THEN 
			SET v_wh_searchTxnWithWithoutDoc = CONCAT(" and SUPPORTING_DOCS <>'' ", "");
			SET v_wh_pv_searchTxnWithWithoutDoc = CONCAT(" and p.SUPPORTING_DOCUMENTS <>'' ", "");
		ELSEIF (searchTxnWithWithoutDoc = 2) THEN
			SET v_wh_searchTxnWithWithoutDoc = CONCAT(" and (SUPPORTING_DOCS IS NULL OR SUPPORTING_DOCS = '') ", "");
			SET v_wh_pv_searchTxnWithWithoutDoc = CONCAT(" and (p.SUPPORTING_DOCUMENTS IS NULL OR p.SUPPORTING_DOCUMENTS = '') ", "");	
		END IF;
		IF (searchTxnPyMode != "") THEN 
			SET v_wh_searchTxnPyMode = CONCAT(" and RECEIPT_DETAILS_TYPE IN (", searchTxnPyMode, ")");
			IF (searchTxnPyMode = '1') THEN
				SET v_wh_pv_searchTxnPyMode = CONCAT(" and d.HEAD_TYPE = 'cash' ");
			 ELSEIF (searchTxnPyMode = '2') THEN
			 	SET v_wh_pv_searchTxnPyMode = CONCAT(" and d.HEAD_TYPE = 'bank' ");
			 ELSEIF (searchTxnPyMode = '1,2') THEN
			 	SET v_wh_pv_searchTxnPyMode = CONCAT(" and d.HEAD_TYPE in ('cash','bank') ");
			 END IF;
		END IF;
		IF (searchTxnWithWithoutRemarks = 1) THEN 
			SET v_wh_searchTxnWithWithoutRemarks = CONCAT(" and REMARKS is not null ", "");
			SET v_wh_pv_searchTxnWithWithoutRemarks = CONCAT(" and p.REMARKS is not null ", "");
		ELSEIF (searchTxnWithWithoutRemarks = 2) THEN 
			SET v_wh_searchTxnWithWithoutRemarks = CONCAT(" and REMARKS is null ", "");
			SET v_wh_pv_searchTxnWithWithoutRemarks = CONCAT(" and p.REMARKS is null ", "");
		END IF;
		IF (searchTxnException != "") THEN 
			IF (searchTxnException = '1') THEN 
				SET v_wh_searchTxnException = CONCAT(" and TRANSACTION_EXCEEDING_BUDGET=1 and (KLFOLLOWSTATUS in (null,1))", "");
			ELSEIF (searchTxnException = '2') THEN 
				SET v_wh_searchTxnException = CONCAT(" and (KLFOLLOWSTATUS=0 and (TRANSACTION_EXCEEDING_BUDGET is null or TRANSACTION_EXCEEDING_BUDGET!=1) or (DOC_RULE_STATUS is not null and TRANSACTION_EXCEEDING_BUDGET is null))", "");
			ELSEIF (searchTxnException = '3') THEN 
				SET v_wh_searchTxnException = CONCAT(" and (TRANSACTION_EXCEEDING_BUDGET=1 and KLFOLLOWSTATUS=0) ", "");
			ELSEIF (searchTxnException = '1,2') THEN 
				SET v_wh_searchTxnException = CONCAT(" and ((TRANSACTION_EXCEEDING_BUDGET=1 and (KLFOLLOWSTATUS is null or KLFOLLOWSTATUS!=0)) OR (KLFOLLOWSTATUS=0 and (TRANSACTION_EXCEEDING_BUDGET is null or TRANSACTION_EXCEEDING_BUDGET!=1)  or (DOC_RULE_STATUS is not null and TRANSACTION_EXCEEDING_BUDGET is null)))", "");
			ELSEIF (searchTxnException = '1,3') THEN 
				SET v_wh_searchTxnException = CONCAT(" and ((TRANSACTION_EXCEEDING_BUDGET=1 and (KLFOLLOWSTATUS is null or KLFOLLOWSTATUS!=0)) OR (TRANSACTION_EXCEEDING_BUDGET=1 and KLFOLLOWSTATUS=0)) ", "");
			ELSEIF (searchTxnException = '2,3') THEN 
				SET v_wh_searchTxnException = CONCAT(" and ((KLFOLLOWSTATUS=0 and (TRANSACTION_EXCEEDING_BUDGET is null or TRANSACTION_EXCEEDING_BUDGET!=1)) OR (TRANSACTION_EXCEEDING_BUDGET=1 and KLFOLLOWSTATUS=0) or (DOC_RULE_STATUS is not null and TRANSACTION_EXCEEDING_BUDGET is null)) ", "");
			ELSEIF (searchTxnException = '1,2,3') THEN 
				SET v_wh_searchTxnException = CONCAT(" and (TRANSACTION_EXCEEDING_BUDGET in (null,1) or KLFOLLOWSTATUS in (0) or (DOC_RULE_STATUS is not null and TRANSACTION_EXCEEDING_BUDGET is null))", "");
			END IF;
			SET set_txnpur_query = 1;
		ELSE
			SET set_pvtxn_query = 1;
		END IF;
		IF ((searchCustomers != "") AND (searchVendors != "")) THEN 
			SET v_wh_searchCustomers = CONCAT(" and (TRANSACTION_VENDOR_CUSTOMER IN (", searchCustomers, ") OR TRANSACTION_VENDOR_CUSTOMER IN(", searchVendors, "))");
			SET v_wh_pv_searchCustomers = CONCAT(" and (d.HEAD_ID IN (", searchCustomers, ") OR d.HEAD_ID IN(", searchVendors, "))");
			SET v_wh_bom_searchCustomers = CONCAT(" and (TRANSACTION_VENDOR_CUSTOMER IN (", searchCustomers, ") OR TRANSACTION_VENDOR_CUSTOMER IN(", searchVendors, "))");
		ELSEIF (searchVendors != "") THEN 
			SET v_wh_searchVendors = CONCAT(" and TRANSACTION_VENDOR_CUSTOMER = (", searchVendors, ")");
			SET v_wh_pv_searchCustomers = CONCAT(" and d.HEAD_ID = (", searchVendors, ")");
		ELSEIF (searchCustomers != "") THEN 
			SET v_wh_searchCustomers = CONCAT(" and TRANSACTION_VENDOR_CUSTOMER IN (", searchCustomers, ")");
			SET v_wh_pv_searchCustomers = CONCAT(" and d.HEAD_ID = (", searchCustomers, ")");
		END IF;
		IF (txnFmDate != "") THEN 
			IF (txnToDt != "") THEN 
				SET v_wh_txnFmDate = CONCAT(" and TRANSACTION_ACTIONDATE between '", txnFmDate, "' and '", txnToDt, "' ");
				SET v_wh_pv_txnFmDate = CONCAT(" and p.TRANSACTION_DATE between '", txnFmDate, "' and '", txnToDt, "' ");
				SET v_wh_bom_txnFmDate = CONCAT(" and ACTION_DATE between '", txnFmDate, " 00:00:00' and '", txnToDt, " 23:59:59' ");
			ELSE 
				SET v_wh_txnFmDate = CONCAT(" and TRANSACTION_ACTIONDATE = '", txnFmDate, "' ");
				SET v_wh_pv_txnFmDate = CONCAT(" and p.TRANSACTION_DATE = '", txnFmDate, "' ");
				SET v_wh_bom_txnFmDate = CONCAT(" and ACTION_DATE >= '", txnFmDate, " 00:00:00' ");
			END IF;
		ELSEIF (txnToDt != "") THEN 
			SET v_wh_txnFmDate = CONCAT(" and TRANSACTION_ACTIONDATE = '", txnToDt, "' ");
			SET v_wh_pv_txnFmDate = CONCAT(" and p.TRANSACTION_DATE = '", txnToDt, "' ");
			SET v_wh_bom_txnFmDate = CONCAT(" and ACTION_DATE <= '", txnToDt, " 23:59:59' ");
		END IF;
		if(perPage > 0) then 
			set limitStmt = concat(' limit ', perPage, " offset ", fromRecord);
		else 
			set limitStmt = '';
		end if;
		SET tr_query = CONCAT(
		    "select \'TransactionTable\' as TABLENAME, ID, TRANSACTION_BRANCH,TRANSACTION_PURPOSE,TRANSACTION_ACTIONDATE,TRANSACTION_INVOICE_DATE,GROSS_AMOUNT,NET_AMOUNT,NET_AMOUNT_RESULT_DESCRIPTION,TRANSACTION_STATUS,CREATED_BY,SUPPORTING_DOCS,REMARKS,APPROVER_EMAILS,APPROVER_ACTION_BY,ADDITIONAL_APPROVER_USER_EMAILS,SELECTED_ADDITIONAL_APPROVER,INSTRUMENT_NUMBER,INSTRUMENT_DATE,TRANSACTION_PROJECT,TRANSACTION_SPECIFICS,BUDGET_AVAILABLE_DURING_TXN,ACTUAL_ALLOCATED_BUDGET,TRANSACTION_VENDOR_CUSTOMER,TRANSACTION_UNAVAILABLE_VENDOR_CUSTOMER,TRANSACTION_EXCEEDING_BUDGET,KLFOLLOWSTATUS,DOC_RULE_STATUS,RECEIPT_DETAILS_TYPE,NO_OF_UNITS,PRICE_PER_UNIT,PO_REFERENCE,CREATED_AT from TRANSACTION WHERE TRANSACTION_BRANCH_ORGANIZATION=", 
		    @iorgId, v_where_creator, v_where_approver, 
		    v_wh_txnUserType, v_wh_txnQuestion, 
		    v_wh_searchCategory, v_wh_searchItems, 
		    v_wh_searchTxnStatus, v_wh_searchTransactionRefNumber, 
		    v_wh_searchTxnBranch, v_wh_searchTxnProjects, 
		    v_wh_searchAmountRanseLimitFrom, 
		    v_wh_searchAmountRanseLimitTo, 
		    v_wh_searchTxnWithWithoutDoc, v_wh_searchTxnPyMode, 
		    v_wh_searchTxnWithWithoutRemarks, 
		    v_wh_searchTxnException, v_wh_searchVendors, 
		    v_wh_searchCustomers, v_wh_txnFmDate
		);
		set tr_queryCount = CONCAT(
		    "select count(1) into @foundTotal from TRANSACTION WHERE TRANSACTION_BRANCH_ORGANIZATION=", 
		    @iorgId, v_where_creator, v_where_approver, 
		    v_wh_txnUserType, v_wh_txnQuestion, 
		    v_wh_searchCategory, v_wh_searchItems, 
		    v_wh_searchTxnStatus, v_wh_searchTransactionRefNumber, 
		    v_wh_searchTxnBranch, v_wh_searchTxnProjects, 
		    v_wh_searchAmountRanseLimitFrom, 
		    v_wh_searchAmountRanseLimitTo, 
		    v_wh_searchTxnWithWithoutDoc, v_wh_searchTxnPyMode, 
		    v_wh_searchTxnWithWithoutRemarks, 
		    v_wh_searchTxnException, v_wh_searchVendors, 
		    v_wh_searchCustomers, v_wh_txnFmDate
		);
		IF (set_pv_query = 1) THEN 
			SET pv_query = CONCAT(
                "select DISTINCT \'pv\' as TABLENAME, p.ID,p.DEBIT_BRANCH as TRANSACTION_BRANCH,p.TRANSACTION_PURPOSE,p.TRANSACTION_DATE as TRANSACTION_ACTIONDATE,p.REVERSAL_DATE as TRANSACTION_INVOICE_DATE,p.TOTAL_DEBIT_AMOUNT as GROSS_AMOUNT,p.TOTAL_DEBIT_AMOUNT as NET_AMOUNT,p.PURPOSE as NET_AMOUNT_RESULT_DESCRIPTION,p.TRANSACTION_STATUS,p.CREATED_BY,p.SUPPORTING_DOCUMENTS as SUPPORTING_DOCS,p.REMARKS,p.APPROVER_EMAILS,p.APPROVER_ACTION_BY,p.ADDITIONAL_APPROVER_USER_EMAILS,p.SELECTED_ADDITIONAL_APPROVER,p.INSTRUMENT_NUMBER,p.INSTRUMENT_DATE,null,null,null,null,null,null,null,null,null,null,null,null,null,p.CREATED_AT from PROVISION_JOURNAL_ENTRY p, PROVISION_JOURNAL_ENTRY_DETAIL d",v_wh_pv_searchCatTable, " WHERE p.BRANCH_ORGANIZATION=", 
			    @iorgId, " AND p.BRANCH_ORGANIZATION = d.ORGANIZATION_ID AND p.ID = d.PROVISION_JOURNAL_ENTRY_ID ", v_wh_pv_searchTxnStatus, 
			    v_wh_pv_txnUserType, v_wh_pv_searchTransactionRefNumber, 
				v_wh_pv_searchCategory, v_wh_pv_searchItems,
			    v_wh_pv_searchTxnBranch, v_wh_pv_searchTxnProjects, 
			    v_wh_pv_searchAmountRanseLimitFrom, 
			    v_wh_pv_searchAmountRanseLimitTo, 
			    v_wh_pv_searchTxnWithWithoutDoc, v_wh_pv_searchTxnPyMode,
			    v_wh_pv_searchTxnWithWithoutRemarks, 
			    v_wh_pv_searchCustomers, v_wh_pv_txnFmDate
			);
			SET @query = concat(pv_query, " ORDER BY CREATED_AT DESC", limitStmt);
			SET @countSQL = CONCAT(
			    "select count(1) into @foundTotal from PROVISION_JOURNAL_ENTRY p, PROVISION_JOURNAL_ENTRY_DETAIL d",v_wh_pv_searchCatTable, " WHERE p.BRANCH_ORGANIZATION=", 
			    @iorgId, " AND p.BRANCH_ORGANIZATION = d.ORGANIZATION_ID AND p.ID = d.PROVISION_JOURNAL_ENTRY_ID ", v_wh_pv_searchTxnStatus, 
			    v_wh_pv_txnUserType, v_wh_pv_searchTransactionRefNumber, 
				v_wh_pv_searchCategory, v_wh_pv_searchItems,
			    v_wh_pv_searchTxnBranch, v_wh_pv_searchTxnProjects, 
			    v_wh_pv_searchAmountRanseLimitFrom, 
			    v_wh_pv_searchAmountRanseLimitTo, 
			    v_wh_pv_searchTxnWithWithoutDoc, v_wh_pv_searchTxnPyMode,
			    v_wh_pv_searchTxnWithWithoutRemarks, 
			    v_wh_pv_searchCustomers, v_wh_pv_txnFmDate
			);
		ELSEIF (set_txnpur_query = 1) THEN 
			SET @query = concat(tr_query, " ORDER BY CREATED_AT DESC", limitStmt);
			SET @countSQL = tr_queryCount;	
		ELSEIF (set_pvtxn_query = 1) THEN 
			SET pv_query = CONCAT(
			    "select DISTINCT \'pv\' as TABLENAME, p.ID,p.DEBIT_BRANCH as TRANSACTION_BRANCH,p.TRANSACTION_PURPOSE,p.TRANSACTION_DATE as TRANSACTION_ACTIONDATE,p.REVERSAL_DATE as TRANSACTION_INVOICE_DATE,p.TOTAL_DEBIT_AMOUNT as GROSS_AMOUNT,p.TOTAL_DEBIT_AMOUNT as NET_AMOUNT,p.PURPOSE as NET_AMOUNT_RESULT_DESCRIPTION,p.TRANSACTION_STATUS,p.CREATED_BY,p.SUPPORTING_DOCUMENTS as SUPPORTING_DOCS,p.REMARKS,p.APPROVER_EMAILS,p.APPROVER_ACTION_BY,p.ADDITIONAL_APPROVER_USER_EMAILS,p.SELECTED_ADDITIONAL_APPROVER,p.INSTRUMENT_NUMBER,p.INSTRUMENT_DATE,null,null,null,null,null,null,null,null,null,null,null,null,null,p.CREATED_AT from PROVISION_JOURNAL_ENTRY p, PROVISION_JOURNAL_ENTRY_DETAIL d",v_wh_pv_searchCatTable, " WHERE p.BRANCH_ORGANIZATION=", 
			    @iorgId, " AND p.BRANCH_ORGANIZATION = d.ORGANIZATION_ID AND p.ID = d.PROVISION_JOURNAL_ENTRY_ID ", v_wh_pv_searchTxnStatus, 
			    v_wh_pv_txnUserType, v_wh_pv_searchTransactionRefNumber, 
				v_wh_pv_searchCategory, v_wh_pv_searchItems,
			    v_wh_pv_searchTxnBranch, v_wh_pv_searchTxnProjects, 
			    v_wh_pv_searchAmountRanseLimitFrom, 
			    v_wh_pv_searchAmountRanseLimitTo, 
			    v_wh_pv_searchTxnWithWithoutDoc, v_wh_pv_searchTxnPyMode,
			    v_wh_pv_searchTxnWithWithoutRemarks, 
			    v_wh_pv_searchCustomers, v_wh_pv_txnFmDate
			);
			 SET @query = CONCAT(
            "SELECT * FROM (",
            tr_query,
            " UNION ALL ",
            pv_query,
            ") AS COMBINED_TRAN_PROVJOURNAL ORDER BY CREATED_AT DESC",
            limitStmt );
			SET @countSQL = CONCAT(
            "SELECT COUNT(2) INTO @foundTotal FROM (",
            tr_query,
            " UNION ALL ",
            pv_query,
            ") AS COMBINED_TRAN_PROVJOURNAL");
		END IF;
		IF (set_bomtxn_query = 1) THEN 
			SET bom_query = CONCAT(
			    "select \'bom\' as TABLENAME, ID, BRANCH_ID as TRANSACTION_BRANCH, TRANSACTION_PURPOSE, ACTION_DATE as TRANSACTION_ACTIONDATE, null, null, TOTAL_NET_AMOUNT as NET_AMOUNT,null,TRANSACTION_STATUS,CREATED_BY,SUPPORTING_DOCS,REMARKS,APPROVER_EMAILS,APPROVER_ACTION_BY,ADDITIONAL_APPROVER_USER_EMAILS,SELECTED_ADDITIONAL_APPROVER,null,null,PROJECT_ID,null,null,null,CUSTOMER_VENDOR_ID,null,null,null,null,null,INCOME_NO_OF_UNITS as NO_OF_UNITS,null,null,CREATED_AT from BILL_OF_MATERIAL_TXN where ORGANIZATION_ID=", 
			    @iorgId, v_wh_bom_txnQuestion, v_wh_bom_searchTxnStatus, 
			    v_wh_bom_searchTransactionRefNumber, 
			    v_wh_bom_searchTxnBranch, v_wh_bom_txnFmDate, 
			    v_wh_bom_searchCustomers
			);
		END IF;
		IF (set_pv_bom_txn_query = 1) THEN 
			SET @query = CONCAT(
            "SELECT * FROM (",
            tr_query,
            " UNION ALL ",
            pv_query,
            " UNION ALL ",
            bom_query,
            ") AS COMBINED_TRAN_PROVJOURNAL_BOM ORDER BY CREATED_AT DESC",
            limitStmt);
			SET @countSQL = CONCAT(
            "SELECT COUNT(1) INTO @foundTotal FROM (",
            tr_query,
            " UNION ALL ",
            pv_query,
            " UNION ALL ",
            bom_query,
            ") AS COMBINED_TRAN_PROVJOURNAL_BOM");
		ELSEIF (set_txnpur_bom_txn_query = 1) THEN 
			SET @query = CONCAT(
            "SELECT * FROM (",
            tr_query,
            " UNION ALL ",
            bom_query,
            ") AS COMBINED_TRAN_BOM ORDER BY CREATED_AT DESC",
            limitStmt );
			SET @countSQL = CONCAT(
            "SELECT COUNT(1) INTO @foundTotal FROM (",
            tr_query,
            " UNION ALL ",
            bom_query,
            ") AS COMBINED_TRAN_BOM" );
		END IF;
		insert into LOG_STORED_PROC(`message`, `name`) 
		values (@countSQL, "cquery");
		PREPARE cstmt FROM @countSQL;
		EXECUTE cstmt;
		DEALLOCATE PREPARE cstmt;
		set totalRecords = @foundTotal;
		insert into LOG_STORED_PROC(`message`, `name`) 
		values (@query, "query");
		PREPARE stmt FROM @query;
		EXECUTE stmt;
		DEALLOCATE PREPARE stmt;
		IF (debugStoredPorc = 1) THEN 
			insert into LOG_STORED_PROC(MESSAGE, NAME) 
			values ("Proc done", "Search");
		END IF;
	END//
DELIMITER ;