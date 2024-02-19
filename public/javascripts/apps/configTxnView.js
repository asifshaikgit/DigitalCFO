var CALLED_METHOD = 'txn';
var PER_PAGE_TXN = 50;
function getUserTransactions(fromRecord, toRecord) {
    var jsonData = {};
    var useremail = $("#hiddenuseremail").text();
    jsonData.usermail = useremail;
    jsonData.perPage = PER_PAGE_TXN;
    jsonData.fromRecord = fromRecord;
    jsonData.toRecord = toRecord;
    cancel();
    var url="/user/userTransactions";
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    $.ajax({
        url         : url,
        data        : JSON.stringify(jsonData),
        type        : "text",
        headers:{
            "X-AUTH-TOKEN": window.authToken
        },
        method      : "POST",
        contentType : 'application/json',
        success     : function (data) {
            let totalRecords=data.totalRecords;
            let sessionuser="";
            if(data.sessionuserTxnData){
                sessionuser=data.sessionuserTxnData[0].sessemail;
            }
            if(sessionuser!='null'){
                txnMsg=data.approval;
                txnMsg+='<br>'+data.approved;
                displayTransactionRecords(data);
                getCashBankReceivablePayable();
                if(parseInt(fromRecord) === 0) {
                    setPagingDetailTxn('transactionTable', PER_PAGE_TXN, 'pagingTransactionNavPosition', totalRecords);
                }
            }else if(sessionuser=='null'){
                window.location.href="/logout";
            }
            CALLED_METHOD = 'txn';
        },
        error : function (xhr, status, error) {
            if(xhr.status == 401){ doLogout();
            }else if(xhr.status == 500){
                swal("Error on fetching transactions!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
        }
    })
}

function displayTransactionRecords(data){
    if(!data){
        return false;
    }
    if(typeof data.userTxnData == 'undefined'){
        return false;
    }
    $("#transactionTable tbody").html("");

    var txnListTableTr = '';
    for(var i=0;i<data.userTxnData.length;i++){
        if(data.userTxnData[i].isParent == 1) {
            //skipping the Purchase order transaction to display if its a parent PO
            continue;
        }
        if(data.userTxnData[i].transactionPurpose!="Make Provision/Journal Entry" && data.userTxnData[i].transactionPurpose!="Process Payroll"){
            var loggedUser=$("#hiddenuseremail").text();
            $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"]').remove();
            var roles=data.userTxnData[i].roles;
            var testRestult = roles.indexOf("CREATOR");
            var transStatus = data.userTxnData[i].status;
            var isAlreadyAdded = 0;
            if(data.userTxnData[i].createdBy==loggedUser){
                if(data.userTxnData[i].status=="Approved"){
                    if(data.userTxnData[i].transactionPurpose=="Sales returns" || data.userTxnData[i].transactionPurpose=="Purchase returns" || parseInt(data.userTxnData[i].transactionPurposeID) == BILL_OF_MATERIAL || parseInt(data.userTxnData[i].transactionPurposeID) == CREATE_PURCHASE_ORDER || parseInt(data.userTxnData[i].transactionPurposeID) == CREATE_PURCHASE_REQUISITION){
                        txnListTableTr = '<tr id="transactionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p>'+
                            '<b>PROJECT:</b><p style="color: blue;">'+data.userTxnData[i].projectName+'</p><b>CREATOR:</b><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td>'+

                            '<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.userTxnData[i].itemName+'</p><b>IMMEDIATE PARENT:</b><p style="color: blue;">'+data.userTxnData[i].itemParentName+'</p></div></td>'+

                            '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+

                            '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.userTxnData[i].invoiceDateLabel+'</b><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></div></td>' +

                            '<td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td>' +

                            '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].noOfUnit+'</p>';
                        if(parseInt(data.userTxnData[i].transactionPurposeID) != BILL_OF_MATERIAL && parseInt(data.userTxnData[i].transactionPurposeID) != CREATE_PURCHASE_REQUISITION) {
                            txnListTableTr += '<br/><b>PRICE\\UNIT:</b><br/><p class="number-right">' + data.userTxnData[i].unitPrice + '</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">' + data.userTxnData[i].grossAmount + '</p>';
                        }
                        txnListTableTr += '</div></td>';
                        txnListTableTr += '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/><b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;"></div><div class="outstandings" style="margin-top: 40px;display:none;"><img src="/assets/images/weightedaverage.jpg" onclick="wightedAverageForTransaction(this);"></img><b>&nbsp;&nbsp;PERIOD</b><select style="width:50px;" name="waperiod" id="waperiod"><option value="1">1 month</option><option value="3">3 month</option><option value="6">6 month</option><option value="12">12 month</option></select></div></div></td>'+

                            '<td><div class="rowToExpand"><div class="txnstat">'+data.userTxnData[i].status+'</div><div class="poReferenceDiv"></div>'+
                            '<input type="button" id="completeTxn" class="completeTxn btn btn-submit btn-idos" value="Complete Accounting" onclick="completeAccounting(this)" style="margin-top:5px;margin-left: 0px;"><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></td>'+

                            '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                            '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+

                            '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div>'+
                            '<div class="invDateDiv"><b>VENDOR INVOICE DATE:</b><input type="text" name="vendorInvoiceDate" id="vendorInvoiceDate" class="datepicker"></div></div></td>'+
                            '</tr>';
                        $("#transactionTable").append(txnListTableTr);
                        console.log("j1");
                    }else{
                        isAlreadyAdded = 1;
                        txnListTableTr = '<tr id="transactionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p>'+
                            '<b>PROJECT:</b><p style="color: blue;">'+data.userTxnData[i].projectName+'</p><b>CREATOR:</b><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td>'+

                            '<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.userTxnData[i].itemName+'</p><b>IMMEDIATE PARENT:</b><p style="color: blue;">'+data.userTxnData[i].itemParentName+'</p></div></td>'+

                            '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+

                            '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></div></td>' +

                            '<td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td>' +
                            '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].noOfUnit+'</p>';
                        if(parseInt(data.userTxnData[i].transactionPurposeID) != BILL_OF_MATERIAL && parseInt(data.userTxnData[i].transactionPurposeID) != CREATE_PURCHASE_REQUISITION) {
                            txnListTableTr += '<br/><b>PRICE\\UNIT:</b><br/><p class="number-right">' + data.userTxnData[i].unitPrice + '</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">' + data.userTxnData[i].grossAmount + '</p>';
                        }
                        txnListTableTr += '</div></td>';
                        txnListTableTr += '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/><b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;"></div><div class="outstandings" style="margin-top: 40px;display:none;"><img src="/assets/images/weightedaverage.jpg" onclick="wightedAverageForTransaction(this);"></img><b>&nbsp;&nbsp;PERIOD</b><select style="width:50px;" name="waperiod" id="waperiod"><option value="1">1 month</option><option value="3">3 month</option><option value="6">6 month</option><option value="12">12 month</option></select></div></div></td>'+

                            '<td><div class="rowToExpand"><div class="txnstat">'+data.userTxnData[i].status+'</div><div class="poReferenceDiv"></div>'+
                            '<div class="payementDiv">Select mode of payment:<select name="paymentDetails" class="txnPaymodeCls" id="paymentDetails" onchange="listAllBranchBankAccounts(this);">'+
                            '<option value="1">CASH</option><option value="2">BANK</option></select> <br>'+
                            'Input payment details:<textarea name="bankDetails" id="bankDetails" class="txnReceptTextCls"></textarea></div>'+
                            '<input type="button" id="completeTxn" class="completeTxn btn btn-submit btn-idos" value="Complete Accounting" onclick="completeAccounting(this)" style="margin-top:5px;margin-left: 0px;"><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></td>'+

                            '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                            '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+

                            '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div>'+
                            '<div class="invDateDiv"><b>VENDOR INVOICE DATE:</b><input type="text" name="vendorInvoiceDate" id="vendorInvoiceDate" class="datepicker"></div></div></td>'+
                            '</tr>';
                        $("#transactionTable").append(txnListTableTr);
                        console.log("j2");
                    }
                }else{
                    if(data.userTxnData[i].status=='Require Clarification'){
                        isAlreadyAdded = 1;
                        txnListTableTr = '<tr id="transactionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                            '<b>PROJECT:</b><br/><p style="color: blue;">'+data.userTxnData[i].projectName+'</p><br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.userTxnData[i].itemName+'</p><b>IMMEDIATE PARENT:</b><p style="color: blue;">'+data.userTxnData[i].itemParentName+'</p></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                            '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td>'+

                            '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].noOfUnit+'</p>';
                        if(parseInt(data.userTxnData[i].transactionPurposeID) != BILL_OF_MATERIAL && parseInt(data.userTxnData[i].transactionPurposeID) != CREATE_PURCHASE_REQUISITION) {
                            txnListTableTr += '<br/><b>PRICE\\UNIT:</b><br/><p class="number-right">' + data.userTxnData[i].unitPrice + '</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">' + data.userTxnData[i].grossAmount + '</p>';
                        }
                        txnListTableTr += '</div></td>';
                        txnListTableTr += '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/>'+
                            '<b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 100px;overflow: auto;"></div><div class="outstandings" style="display:none;"><img src="/assets/images/weightedaverage.jpg" onclick="wightedAverageForTransaction(this);"></img><b>&nbsp;&nbsp;PERIOD</b><select style="width:50px;" name="waperiod" id="waperiod"><option value="1">1 month</option><option value="3">3 month</option><option value="6">6 month</option><option value="12">12 month</option></select></div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><div class="poReferenceDiv"></div>'+
                            '<br/><input type="button" value="Clarify Transaction" id="clarifyTxn" class="btn btn-submit btn-idos" onclick="clarifyTransaction(this)" style="margin-top:10px;margin-left: 0px;width:170px;"><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div><div class="invoiceForm"></div></div></td>'+

                            '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                            '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                            '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnRemarks(this)"></div></div></td></tr>';
                        $("#transactionTable").append(txnListTableTr);
                        console.log("j3");
                    }else{
                        if(data.userTxnData[i].transactionPurpose=="Sell on cash & collect payment now" || data.userTxnData[i].transactionPurpose=="Sell on credit & collect payment later"){
                            txnListTableTr = '<tr id="transactionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p>'+
                                '<b>PROJECT:</b><p style="color: blue;">'+data.userTxnData[i].projectName+'</p><b>CREATOR:</b><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.userTxnData[i].itemName+'</p><b>IMMEDIATE PARENT:</b><p style="color: blue;">'+data.userTxnData[i].itemParentName+'</p></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><b>TRANSACTION PURPOSE:</b><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.userTxnData[i].invoiceDateLabel+'</b><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].noOfUnit+'</p>';
                            if(parseInt(data.userTxnData[i].transactionPurposeID) != BILL_OF_MATERIAL && parseInt(data.userTxnData[i].transactionPurposeID) != CREATE_PURCHASE_REQUISITION) {
                                txnListTableTr += '<br/><b>PRICE\\UNIT:</b><br/><p class="number-right">' + data.userTxnData[i].unitPrice + '</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">' + data.userTxnData[i].grossAmount + '</p>';
                            }
                            txnListTableTr += '</div></td>';
                            txnListTableTr += '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p>'+
                                '<b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 100px;overflow: auto;"></div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><div class="poReferenceDiv"></div><b>'+data.userTxnData[i].approverLabel+'</b><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div><div class="invoiceForm"></div></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+
                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnRemarks(this)"></div></div></td></tr>';
                            $("#transactionTable").append(txnListTableTr);
                            console.log("j4");
                        }else{
                            txnListTableTr = '<tr id="transactionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>PROJECT:</b><br/><p style="color: blue;">'+data.userTxnData[i].projectName+'</p><br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.userTxnData[i].itemName+'</p><b>IMMEDIATE PARENT:</b><p style="color: blue;">'+data.userTxnData[i].itemParentName+'</p></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].noOfUnit+'</p>';
                            if(parseInt(data.userTxnData[i].transactionPurposeID) != BILL_OF_MATERIAL && parseInt(data.userTxnData[i].transactionPurposeID) != CREATE_PURCHASE_REQUISITION) {
                                txnListTableTr += '<br/><b>PRICE\\UNIT:</b><br/><p class="number-right">' + data.userTxnData[i].unitPrice + '</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">' + data.userTxnData[i].grossAmount + '</p>';
                            }
                            txnListTableTr += '</div></td>';
                            txnListTableTr += '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/>'+
                                '<b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 100px;overflow: auto;"></div><div class="outstandings" style="display:none;"><img src="/assets/images/weightedaverage.jpg" onclick="wightedAverageForTransaction(this);"></img><b>&nbsp;&nbsp;PERIOD</b><select style="width: 70px;" name="waperiod" id="waperiod"><option value="1">1 month</option><option value="3">3 month</option><option value="6">6 month</option><option value="12">12 month</option></select></div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><div class="poReferenceDiv"></div><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div><div class="invoiceForm"></div></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+
                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnRemarks(this)"></div></div></td></tr>';
                            $("#transactionTable").append(txnListTableTr);
                            console.log("j5 = " +  data.userTxnData[i].txnReferenceNo);
                        }
                    }
                }

                if(parseInt(data.userTxnData[i].transactionPurposeID) == RECEIVE_PAYMENT_FROM_CUSTOMER || parseInt(data.userTxnData[i].transactionPurposeID) ==RECEIVE_ADVANCE_FROM_CUSTOMER){
                    $("#transactionEntity"+data.userTxnData[i].id+" div[class='invoiceForm']").append('<form action="/exportReceiptPdf" class="'+data.userTxnData[i].transactionPurpose+'" name="receiptForm'+data.userTxnData[i].id+'" id="receiptForm'+data.userTxnData[i].id+'">'+
                        '<input type="button" value="Generate Voucher" id="generateReceipt" class="btn btn-submit btn-idos" onclick="generatePDFReceipt(this,'+data.userTxnData[i].transactionPurposeID+');"></form>');
                }

                if(parseInt(data.userTxnData[i].transactionPurposeID) == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER || parseInt(data.userTxnData[i].transactionPurposeID) == BUY_ON_CREDIT_PAY_LATER || parseInt(data.userTxnData[i].transactionPurposeID) == PAY_SPECIAL_ADJUSTMENTS_AMOUNT_TO_VENDORS || parseInt(data.userTxnData[i].transactionPurposeID) ==WITHDRAW_CASH_FROM_BANK || parseInt(data.userTxnData[i].transactionPurposeID) ==DEPOSIT_CASH_IN_BANK || parseInt(data.userTxnData[i].transactionPurposeID) ==TRANSFER_FUNDS_FROM_ONE_BANK_TO_ANOTHER || parseInt(data.userTxnData[i].transactionPurposeID) ==TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER || parseInt(data.userTxnData[i].transactionPurposeID) ==INVENTORY_OPENING_BALANCE || parseInt(data.userTxnData[i].transactionPurposeID) ==SALES_RETURNS || parseInt(data.userTxnData[i].transactionPurposeID) ==PURCHASE_RETURNS || parseInt(data.userTxnData[i].transactionPurposeID) == CREDIT_NOTE_CUSTOMER || parseInt(data.userTxnData[i].transactionPurposeID) ==DEBIT_NOTE_CUSTOMER || parseInt(data.userTxnData[i].transactionPurposeID) == CREDIT_NOTE_VENDOR || parseInt(data.userTxnData[i].transactionPurposeID) == DEBIT_NOTE_VENDOR || parseInt(data.userTxnData[i].transactionPurposeID) == CANCEL_INVOICE){
                    $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="payementDiv"]').hide();
                }else{
                    $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="payementDiv"]').show();
                    if(data.userTxnData[i].transactionPurpose=="Buy on Petty Cash Account" || data.userTxnData[i].transactionPurpose=="Transfer main cash to petty cash"){
                        $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="payementDiv"] select[name="paymentDetails"] option[value="2"]').remove();
                        $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="payementDiv"] select[name="paymentDetails"] option[value="1"]').prop("selected","selected");
                    }
                }

                if(data.userTxnData[i].transactionPurpose=="Buy on cash & pay right away" || data.userTxnData[i].transactionPurpose=="Buy on credit & pay later" || data.userTxnData[i].transactionPurpose=="Pay special adjustments amount to vendors" || data.userTxnData[i].transactionPurpose=="Buy on Petty Cash Account"){
                    if(data.userTxnData[i].status=='Approved'){
                        $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="invDateDiv"]').show();
                        $("input[name='vendorInvoiceDate']").datepicker({
                            changeMonth : true,
                            changeYear : true,
                            dateFormat:  'MM d,yy',
                            yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
                            onSelect: function(x,y){
                                $(this).focus();
                            }
                        });
                    }else{
                        $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="invDateDiv"]').hide();
                    }
                }else{
                    $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="invDateDiv"]').hide();
                }
            }

            //for approver can be same user or not
            if(data.userTxnData[i].approverEmails!=null && data.userTxnData[i].approverEmails!=""){
                if(data.userTxnData[i].approverEmails.indexOf(data.userTxnData[i].useremail)!=-1){
                    //check for user mail existence in the approver usermail list sent by server
                    //based on transaction status row data is displayed
                    if(data.userTxnData[i].status=="Require Approval" || data.userTxnData[i].status=='Clarified'){
                        var existingtxn=$("#transactionTable tr[id='transactionEntity"+data.userTxnData[i].id+"']").attr('id');
                        if(typeof existingtxn!='undefined'){
                            $("#transactionTable tr[id='transactionEntity"+data.userTxnData[i].id+"']").remove();
                        }
                        txnListTableTr = '<tr id="transactionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                            '<b>PROJECT:</b><br/><p style="color: blue;">'+data.userTxnData[i].projectName+'</p><br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.userTxnData[i].itemName+'</p><b>IMMEDIATE PARENT:</b><p style="color: blue;">'+data.userTxnData[i].itemParentName+'</p><b>BUDGET:</b><br/><b>'+data.userTxnData[i].budgetAllocated+'</b><br/><p style="color: blue;">'+data.userTxnData[i].budgetAllocatedAmt+'</p><b>'+data.userTxnData[i].budgetAvailable+'</b><br/><p style="color: blue;">'+data.userTxnData[i].budgetAvailableAmt+'</p></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                            '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td>' +
                            '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].noOfUnit+'</p>';
                        if(parseInt(data.userTxnData[i].transactionPurposeID) != BILL_OF_MATERIAL && parseInt(data.userTxnData[i].transactionPurposeID) != CREATE_PURCHASE_REQUISITION) {
                            txnListTableTr += '<br/><b>PRICE\\UNIT:</b><br/><p class="number-right">' + data.userTxnData[i].unitPrice + '</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">' + data.userTxnData[i].grossAmount + '</p>';
                        }
                        txnListTableTr += '</div></td>';
                        txnListTableTr += '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/><b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 125px;overflow: auto;"></div><div class="outstandings" style="margin-top: 50px;display:none;"><img src="/assets/images/weightedaverage.jpg" onclick="wightedAverageForTransaction(this);"></img><b>&nbsp;&nbsp;PERIOD</b><select style="width:50px;" name="waperiod" id="waperiod"><option value="1">1 month</option><option value="3">3 month</option><option value="6">6 month</option><option value="12">12 month</option></select></div></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userTxnData[i].status+'</div><div class="poReferenceDiv"></div>'+
                            '<br/>Action:<select class="approverActionList" name="approverActionList" id="approverActionList" onchange="pettyCashTransaction(this);"><option value="">--Please Select--</option>'+
                            '<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>'+
                            '<span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
                            '<input type="button" id="approverAction" class="approverAction btn btn-submit btn-center" value="Submit" onclick="completeAction(this)"><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></td>'+
                            '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+
                            '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                            '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>';
                        $("#transactionTable").append(txnListTableTr);
                        console.log("j6= " +  data.userTxnData[i].txnReferenceNo);
                    }else if(isAlreadyAdded == 0){
                        //else if( data.userTxnData[i].status =='Approved' && roles.indexOf("CREATOR")==-1){
                        //Only approver should also see approved transactions
                        // Sunil: added above check to show that "Approved" append can be shown to user when he is creator + approver.
                        var existingtxn=$("#transactionTable tr[id='transactionEntity"+data.userTxnData[i].id+"']").attr('id');
                        if(typeof existingtxn!='undefined'){
                            $("#transactionTable tr[id='transactionEntity"+data.userTxnData[i].id+"']").remove();
                        }
                        txnListTableTr = '<tr id="transactionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                            '<b>PROJECT:</b><br/><p style="color: blue;">'+data.userTxnData[i].projectName+'</p><br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.userTxnData[i].itemName+'</p><b>IMMEDIATE PARENT:</b><p style="color: blue;">'+data.userTxnData[i].itemParentName+'</p></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                            '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td>'+
                            '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].noOfUnit+'</p>';
                        if(parseInt(data.userTxnData[i].transactionPurposeID) != BILL_OF_MATERIAL && parseInt(data.userTxnData[i].transactionPurposeID) != CREATE_PURCHASE_REQUISITION) {
                            txnListTableTr += '<br/><b>PRICE\\UNIT:</b><br/><p class="number-right">' + data.userTxnData[i].unitPrice + '</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">' + data.userTxnData[i].grossAmount + '</p>';
                        }
                        txnListTableTr += '</div></td>';
                        txnListTableTr += '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/>'+
                            '<b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 100px;overflow: auto;"></div><div class="outstandings" style="display:none;"><img src="/assets/images/weightedaverage.jpg" onclick="wightedAverageForTransaction(this);"></img><b>&nbsp;&nbsp;PERIOD</b><select style="width:50px;" name="waperiod" id="waperiod"><option value="1">1 month</option><option value="3">3 month</option><option value="6">6 month</option><option value="12">12 month</option></select></div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><div class="poReferenceDiv"></div><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div><div class="invoiceForm"></div></div></td>'+
                            '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+
                            '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                            '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnRemarks(this)"></div></div></td></tr>';
                        $("#transactionTable").append(txnListTableTr);
                        console.log("j7= " +  data.userTxnData[i].txnReferenceNo);
                    }
                }
            }

            if(data.userTxnData[i].selectedAdditionalApproval!=null && data.userTxnData[i].selectedAdditionalApproval!=""){
                if(data.userTxnData[i].selectedAdditionalApproval==data.userTxnData[i].useremail){
                    if(data.userTxnData[i].status=='Require Additional Approval'){
                        var existingtxn=$("#transactionTable tr[id='transactionEntity"+data.userTxnData[i].id+"']").attr('id');
                        if(typeof existingtxn!='undefined'){
                            $("#transactionTable tr[id='transactionEntity"+data.userTxnData[i].id+"']").remove();
                        }
                        txnListTableTr = '<tr id="transactionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                            '<b>PROJECT:</b><br/><p style="color: blue;">'+data.userTxnData[i].projectName+'</p><br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.userTxnData[i].itemName+'</p><b>IMMEDIATE PARENT:</b><p style="color: blue;">'+data.userTxnData[i].itemParentName+'</p><b>'+data.userTxnData[i].budgetAllocated+'</b><p style="color: blue;">'+data.userTxnData[i].budgetAllocatedAmt+'</p><b>'+data.userTxnData[i].budgetAvailable+'</b><p style="color: blue;">'+data.userTxnData[i].budgetAvailableAmt+'</p></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                            '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td>' +
                            '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].noOfUnit+'</p>';
                        if(parseInt(data.userTxnData[i].transactionPurposeID) != BILL_OF_MATERIAL && parseInt(data.userTxnData[i].transactionPurposeID) != CREATE_PURCHASE_REQUISITION) {
                            txnListTableTr += '<br/><b>PRICE\\UNIT:</b><br/><p class="number-right">' + data.userTxnData[i].unitPrice + '</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">' + data.userTxnData[i].grossAmount + '</p>';
                        }
                        txnListTableTr += '</div></td>';
                        txnListTableTr += '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/><b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 125px;overflow: auto;"></div><div class="outstandings" style="margin-top: 50px;display:none;"><img src="/assets/images/weightedaverage.jpg" onclick="wightedAverageForTransaction(this);"></img><b>&nbsp;&nbsp;PERIOD</b><select style="width:50px;" name="waperiod" id="waperiod"><option value="1">1 month</option><option value="3">3 month</option><option value="6">6 month</option><option value="12">12 month</option></select></div></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userTxnData[i].status+'</div><div class="poReferenceDiv"></div>'+
                            '<br/>Action:<select class="approverActionList" name="approverActionList" id="approverActionList"><option value="">--Please Select--</option>'+
                            '<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>'+
                            '<span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
                            '<input type="button" id="approverAction" class="approverAction btn btn-submit btn-center" value="Submit" onclick="completeAction(this)"><br/><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></td>'+
                            '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+
                            '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                            '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>';
                        $("#transactionTable").append(txnListTableTr);
                        console.log("j8= " +  data.userTxnData[i].txnReferenceNo);
                    }else{
                        var existingtxn=$("#transactionTable tr[id='transactionEntity"+data.userTxnData[i].id+"']").attr('id');
                        if(typeof existingtxn!='undefined'){
                            $("#transactionTable tr[id='transactionEntity"+data.userTxnData[i].id+"']").remove();
                        }
                        txnListTableTr = '<tr id="transactionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                            '<b>PROJECT:</b><br/><p style="color: blue;">'+data.userTxnData[i].projectName+'</p><br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.userTxnData[i].itemName+'</p><b>IMMEDIATE PARENT:</b><p style="color: blue;">'+data.userTxnData[i].itemParentName+'</p></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                            '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td>'+
                            '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].noOfUnit+'</p>';
                        if(parseInt(data.userTxnData[i].transactionPurposeID) != BILL_OF_MATERIAL && parseInt(data.userTxnData[i].transactionPurposeID) != CREATE_PURCHASE_REQUISITION) {
                            txnListTableTr += '<br/><b>PRICE\\UNIT:</b><br/><p class="number-right">' + data.userTxnData[i].unitPrice + '</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">' + data.userTxnData[i].grossAmount + '</p>';
                        }
                        txnListTableTr += '</div></td>';
                        txnListTableTr += '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/>'+
                            '<b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 100px;overflow: auto;"></div><div class="outstandings" style="display:none;"><img src="/assets/images/weightedaverage.jpg" onclick="wightedAverageForTransaction(this);"></img><b>&nbsp;&nbsp;PERIOD</b><select style="width:50px;" name="waperiod" id="waperiod"><option value="1">1 month</option><option value="3">3 month</option><option value="6">6 month</option><option value="12">12 month</option></select></div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><div class="poReferenceDiv"></div><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></div></td>'+
                            '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+
                            '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                            '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnRemarks(this)"></div></div></td></div></td></tr>';
                        $("#transactionTable").append(txnListTableTr);
                        console.log("j9= " +  data.userTxnData[i].txnReferenceNo);
                    }
                }else if(data.userTxnData[i].status=='Require Additional Approval'){
                    $("#transactionTable tr[id='transactionEntity"+data.userTxnData[i].id+"'] td:nth-child(8) div:first").append('<br/><b>Additional Approval Required By:</b><br/>'+data.userTxnData[i].selectedAdditionalApproval+'');
                }
            }
            //var roles=data.userTxnData[i].roles;
            if(roles.indexOf("ACCOUNTANT")!=-1 || roles.indexOf("AUDITOR")!=-1 || roles.indexOf("CONTROLLER")!=-1){
                var approverEmailVal=false;var selectedAdditionalApproval=false;
                if(data.userTxnData[i].approverEmails!=null && data.userTxnData[i].approverEmails!=""){
                    if(data.userTxnData[i].approverEmails.indexOf(data.userTxnData[i].useremail)!=-1){
                        approverEmailVal=true;
                    }
                }
                if(data.userTxnData[i].selectedAdditionalApproval!=null && data.userTxnData[i].selectedAdditionalApproval!=""){
                    if(data.userTxnData[i].selectedAdditionalApproval==data.userTxnData[i].useremail){
                        selectedAdditionalApproval=true;
                    }
                }
                if(data.userTxnData[i].createdBy!=loggedUser && approverEmailVal==false && selectedAdditionalApproval==false){
                    var existingtxn=$("#transactionTable tr[id='transactionEntity"+data.userTxnData[i].id+"']").attr('id');
                    if(typeof existingtxn!='undefined'){
                        $("#transactionTable tr[id='transactionEntity"+data.userTxnData[i].id+"']").remove();
                    }
                    /*
                    $("#transactionTable").append('<tr id="transactionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                    '<b>PROJECT:</b><br/><p style="color: blue;">'+data.userTxnData[i].projectName+'</p><br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><p style="color: blue;">'+data.userTxnData[i].itemName+'</p><b>IMMEDIATE PARENT:</b><p style="color: blue;">'+data.userTxnData[i].itemParentName+'</p></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                    '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td>'+
                    '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].noOfUnit+'</p><br/><b>PRICE\\UNIT:</b><br/><p class="number-right">'+data.userTxnData[i].unitPrice+'</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">'+data.userTxnData[i].grossAmount+'</p></div></td><td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/>'+
                    '<b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 100px;overflow: auto;"></div><div class="outstandings" style="display:none;"><img src="/assets/images/weightedaverage.jpg" onclick="wightedAverageForTransaction(this);"></img><b>&nbsp;&nbsp;PERIOD</b><select style="width:50px;" name="waperiod" id="waperiod"><option value="1">1 month</option><option value="3">3 month</option><option value="6">6 month</option><option value="12">12 month</option></select></div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><div class="poReferenceDiv"></div><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div><div class="invoiceForm"></div></div></td>'+
                    '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                    '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFileTxn(this);"></div></td>'+

                    '<td><div class="rowToExpand"><div class="txnWorkflowRemarks" style="height: 150px;overflow: auto;"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnRemarks(this)"></div></div></td></div></td></tr>');*/
                    /*$("#transactionTable").append('<tr id="transactionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+

                    '<td><div class="rowToExpand"><div class="txnWorkflowRemarks" style="height: 150px;overflow: auto;"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="givetxnRemarks(this)"></div></div></td></div></td></tr>');*/
                    txnListTableTr = '<tr id="transactionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+

                        '<b>PROJECT:</b><br/><p style="color: blue;">'+data.userTxnData[i].projectName+'</p><br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.userTxnData[i].itemName+'</p><b>IMMEDIATE PARENT:</b><p style="color: blue;">'+data.userTxnData[i].itemParentName+'</p></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                        '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td>'+
                        '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].noOfUnit+'</p>';
                    if(parseInt(data.userTxnData[i].transactionPurposeID) != BILL_OF_MATERIAL && parseInt(data.userTxnData[i].transactionPurposeID) != CREATE_PURCHASE_REQUISITION) {
                        txnListTableTr += '<br/><b>PRICE\\UNIT:</b><br/><p class="number-right">' + data.userTxnData[i].unitPrice + '</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">' + data.userTxnData[i].grossAmount + '</p>';
                    }
                    txnListTableTr += '</div></td>';
                    txnListTableTr += '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/>'+
                        '<b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 100px;overflow: auto;"></div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><div class="poReferenceDiv"></div><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div><div class="invoiceForm"></div></div></td>'+
                        '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+
                        '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                        '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnRemarks(this)"></div></div></td></div></td></tr>';
                    $("#transactionTable").append(txnListTableTr);
                    if(data.userTxnData[i].transactionPurpose=="Receive payment from customer" || data.userTxnData[i].transactionPurpose=="Receive advance from customer"){
                        $("#transactionEntity"+data.userTxnData[i].id+" div[class='invoiceForm']").append('<form action="/exportReceiptPdf" class="'+data.userTxnData[i].transactionPurpose+'" name="receiptForm'+data.userTxnData[i].id+'" id="receiptForm'+data.userTxnData[i].id+'">'+
                            '<input type="button" value="Generate Voucher" id="generateReceipt" class="btn btn-submit btn-idos" onclick="generatePDFReceipt(this,'+data.userTxnData[i].transactionPurposeID+');"></form>');
                    }
                    console.log("j10= " +  data.userTxnData[i].txnReferenceNo);
                }
            }

            if(data.userTxnData[i].additionalapproverEmails!=null && data.userTxnData[i].additionalapproverEmails!=""){
                var additionalApprovarUsersList=data.userTxnData[i].additionalapproverEmails.substring(0,data.userTxnData[i].additionalapproverEmails.length).split(',');
                $("tr[id='transactionEntity"+data.userTxnData[i].id+"'] select[id='userAddApproval']").append('<option value="">--Please Select--</option>');
                for(var j=0;j<additionalApprovarUsersList.length;j++){
                    $("tr[id='transactionEntity"+data.userTxnData[i].id+"'] select[id='userAddApproval']").append('<option value="'+additionalApprovarUsersList[j]+'">'+additionalApprovarUsersList[j]+'</option>');
                }
            }

            if(data.userTxnData[i].netAmtDesc!=null && data.userTxnData[i].netAmtDesc!=""){
                var individualNetDesc=data.userTxnData[i].netAmtDesc.substring(0,data.userTxnData[i].netAmtDesc.length).split(',');
                for(var m=0;m<individualNetDesc.length;m++){
                    var labelAndFigure=individualNetDesc[m].substring(0, individualNetDesc[m].length).split(':');
                    $('#transactionEntity'+data.userTxnData[i].id+' div[class="netResultCalcDesc"]').append('<p style="color: #102E55;"><b>'+labelAndFigure[0]+'</b></p>');
                    if(typeof labelAndFigure[1]!='undefined'){
                        $('#transactionEntity'+data.userTxnData[i].id+' div[class="netResultCalcDesc"]').append('<p style="color: blue;">'+labelAndFigure[1]+'</p>');
                    }
                }
            }

            if(data.userTxnData[i].txnRemarks!=null && data.userTxnData[i].txnRemarks!=""){
                var individualRemarks=data.userTxnData[i].txnRemarks.substring(0,data.userTxnData[i].txnRemarks.length).split('|');
                for(var m=0;m<individualRemarks.length;m++){
                    var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
                    $('#transactionEntity'+data.userTxnData[i].id+' div[class="txnWorkflowRemarks"]').append('<p style="color: #004e00;"><b>'+emailAndRemarks[0]+'</b></p>#');
                    if(typeof emailAndRemarks[1]!='undefined'){
                        if(emailAndRemarks[0].indexOf("Auditor")!=-1){
                            $('#transactionEntity'+data.userTxnData[i].id+' div[class="txnWorkflowRemarks"]').append('<h1auditor>'+emailAndRemarks[1]+'</h1auditor><br/>');
                        }else{
                            $('#transactionEntity'+data.userTxnData[i].id+' div[class="txnWorkflowRemarks"]').append('<p style="color: blue;">'+emailAndRemarks[1]+'</p><br/>');
                        }
                    }
                }
            }
            if(data.userTxnData[i].roles.indexOf("APPROVER")!=-1 || data.userTxnData[i].roles.indexOf("ACCOUNTANT")!=-1 || data.userTxnData[i].roles.indexOf("AUDITOR")!=-1){
                if(data.userTxnData[i].transactionPurpose=="Sell on cash & collect payment now" ||  data.userTxnData[i].transactionPurpose=="Sell on credit & collect payment later" || data.userTxnData[i].transactionPurpose=="Buy on cash & pay right away" || data.userTxnData[i].transactionPurpose=="Buy on credit & pay later" || data.userTxnData[i].transactionPurpose=="Buy on Petty Cash Account"){
                    $('#transactionEntity'+data.userTxnData[i].id+' div[class="outstandings"]').show();
                }
            }

            //Sunil: When user is creator + approver then already we have added the button above.
            if(data.userTxnData[i].createdBy==data.userTxnData[i].approverEmail){
                if(data.userTxnData[i].status=='Approved' && isAlreadyAdded == 0){
                    $("tr[id='transactionEntity"+data.userTxnData[i].id+"'] td:nth-child(9)").append('<input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit btn-idos" onclick="completeAccounting(this)" style="margin-top:10px;margin-left: 0px;">');
                }
            }

            if(data.userTxnData[i].txnDocument!="" && data.userTxnData[i].txnDocument!=null){
                var txndocument=data.userTxnData[i].txnDocument;
                var mainTblTrId = "transactionEntity" + data.userTxnData[i].id;
                fillSelectElementWithUploadedDocs(txndocument, mainTblTrId, 'txnViewListUpload');
            }
            if(data.userTxnData[i].txnSpecialStatus=="Transaction Exceeding Budget & Rules Not Followed"){
                $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] b[class="txnSpecialStatus"]').html("");
                //$("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] b[class="txnSpecialStatus"]').append('<img src="assets/images/blue.png"></img>');
                $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] td:nth-child(2)').addClass("txnBugtExceedAndRulesNotFollow");
            } else if(data.userTxnData[i].txnSpecialStatus=="Transaction Exceeding Budget"){
                $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] b[class="txnSpecialStatus"]').html("");
                //$("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] b[class="txnSpecialStatus"]').append('<img src="assets/images/red.png"></img>');
                $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] td:nth-child(2)').addClass("txnBugtExceed");
            } else if(data.userTxnData[i].txnSpecialStatus=="Rules Not Followed"){
                $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] b[class="txnSpecialStatus"]').html("");
                //$("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] b[class="txnSpecialStatus"]').append('<img src="assets/images/green.png"></img>');
                $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] td:nth-child(2)').addClass("txnRulesNotFollow");
            }
            if(data.userTxnData[i].status!="" && data.userTxnData[i].status=="Rejected"){
                $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="txnstat"]').attr('class','txnstatred');
                $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="txnstatgreen"]').attr('class','txnstatred');
                $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="txnstatred"]').attr('class','txnstatred');
            } else if(data.userTxnData[i].status!="" && data.userTxnData[i].status=="Accounted"){
                $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="txnstat"]').attr('class','txnstat');
                $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="txnstatgreen"]').attr('class','txnstat');
                $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="txnstatred"]').attr('class','txnstat');
            } else if(data.userTxnData[i].status!="" && (data.userTxnData[i].status=="Approved" || data.userTxnData[i].status=="Require Approval" || data.userTxnData[i].status=="Require Additional Approval")){
                $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="txnstat"]').attr('class','txnstatgreen');
                $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="txnstatgreen"]').attr('class','txnstatgreen');
                $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="txnstatred"]').attr('class','txnstatgreen');
            }
            //add "edit transaction" button in status column if below transaction type for creator and if transaction require approval
            if(data.userTxnData[i].transactionPurpose=="Buy on cash & pay right away" || data.userTxnData[i].transactionPurpose=="Buy on credit & pay later"
                || data.userTxnData[i].transactionPurpose=="Pay vendor/supplier" || data.userTxnData[i].transactionPurpose=="Pay advance to vendor or supplier"
                || parseInt(data.userTxnData[i].transactionPurposeID) ==PREPARE_QUOTATION || parseInt(data.userTxnData[i].transactionPurposeID) ==PROFORMA_INVOICE
                || parseInt(data.userTxnData[i].transactionPurposeID) ==SELL_ON_CASH_COLLECT_PAYMENT_NOW || parseInt(data.userTxnData[i].transactionPurposeID) ==SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
                if(data.userTxnData[i].status=='Require Approval' && data.userTxnData[i].createdBy==loggedUser){
                    // if(data.userTxnData[i].txnRemarks.indexOf("edited")==-1){
                    //     $("#transactionTable tr[id='transactionEntity"+data.userTxnData[i].id+"'] td:nth-child(8) div:first").append('<br/><input type="button" value="Edit Transaction" id="editTxnOnceAllowed" class="editTxnOnceAllowed btn btn-submit btn-idos" onclick="editTransactionOnceAllowed(this)" style="margin-top:10px;margin-left: 0px;">');
                    // }
                }
            }

            if(parseInt(data.userTxnData[i].transactionPurposeID) == PREPARE_QUOTATION || parseInt(data.userTxnData[i].transactionPurposeID) == PROFORMA_INVOICE || parseInt(data.userTxnData[i].transactionPurposeID)== PURCHASE_ORDER){
                $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="payementDiv"]').hide();
                $("#transactionEntity"+data.userTxnData[i].id+" p[class='txnItemDesc']").html("");
                $("#transactionEntity"+data.userTxnData[i].id+" p[class='txnItemDesc']").append('<a id="multiSellItemsList" onclick="listMultiSellItems(this);">Items List</a>');

                if(data.userTxnData[i].status=='Accounted'){
                    $("#transactionEntity"+data.userTxnData[i].id+" div[class='invoiceForm']").append('<b class="btn btn-submit btn-center">Download Invoice:</b><br><b class="btn btn-submit btn-center">Download Invoice:</b></b><button id="XLSX'+data.userTxnData[i].id+'" class="exportInvoiceXlsx btn btn-submit exportInvoiceBtn" title="Export Invoice(xlsx)" onclick="generatePDFQuotProf(this);"><i class="fa fa-file-excel-o fa-lg" aria-hidden="true"></i></button><button id="PDF1'+data.userTxnData[i].id+'" class="exportInvoicePdf btn btn-submit exportInvoiceBtn" title="Export Invoice(pdf)" onclick="generatePDFQuotProf(this);"><i class="fa fa-file-pdf-o fa-lg" aria-hidden="true"></i></button>');
                    if(parseInt(data.userTxnData[i].transactionPurposeID) == PREPARE_QUOTATION) {
                        $("#transactionEntity" + data.userTxnData[i].id + " div[class='vendorInvDateDiv']").append('<b>Quotaion No:</b><p style="color: blue;">' + data.userTxnData[i].invoiceNumber + '</p>');
                    }else{
                        $("#transactionEntity" + data.userTxnData[i].id + " div[class='vendorInvDateDiv']").append('<b>Proforma No:</b><p style="color: blue;">' + data.userTxnData[i].invoiceNumber + '</p>');
                    }
                }
                if(data.userTxnData[i].remarksPrivate !=null && data.userTxnData[i].remarksPrivate!=""){
                    var individualRemarks=data.userTxnData[i].remarksPrivate.substring(0,data.userTxnData[i].remarksPrivate.length).split('|');
                    $('#transactionEntity'+data.userTxnData[i].id+' div[class="txnWorkflowRemarks"]').after('<div class="txnRemarksPrivate"></div>');
                    for(var m=0;m<individualRemarks.length;m++){
                        var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
                        $('#transactionEntity'+data.userTxnData[i].id+' div[class="txnRemarksPrivate"]').append('<font color="102E55"><b>'+emailAndRemarks[0]+'</b></font>#');
                        if(typeof emailAndRemarks[1]!='undefined'){
                            if(emailAndRemarks[0].indexOf("(Auditor)")!=-1){
                                $('#transactionEntity'+data.userTxnData[i].id+' div[class="txnRemarksPrivate"]').append('<h1auditor>'+emailAndRemarks[1]+'</h1auditor><br/>');
                            }
                            else {
                                $('#transactionEntity'+data.userTxnData[i].id+' div[class="txnRemarksPrivate"]').append('<font color="blue">'+emailAndRemarks[1]+'</font><br/>');
                            }
                        }
                    }
                }
            }else if(parseInt(data.userTxnData[i].transactionPurposeID) == SELL_ON_CASH_COLLECT_PAYMENT_NOW || parseInt(data.userTxnData[i].transactionPurposeID) == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER || parseInt(data.userTxnData[i].transactionPurposeID) == RECEIVE_ADVANCE_FROM_CUSTOMER || parseInt(data.userTxnData[i].transactionPurposeID) ==SALES_RETURNS || parseInt(data.userTxnData[i].transactionPurposeID) == CREDIT_NOTE_CUSTOMER || parseInt(data.userTxnData[i].transactionPurposeID) == DEBIT_NOTE_CUSTOMER || parseInt(data.userTxnData[i].transactionPurposeID) == CREDIT_NOTE_VENDOR || parseInt(data.userTxnData[i].transactionPurposeID) == DEBIT_NOTE_VENDOR || parseInt(data.userTxnData[i].transactionPurposeID) == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER || parseInt(data.userTxnData[i].transactionPurposeID) == CANCEL_INVOICE){
                if(parseInt(data.userTxnData[i].transactionPurposeID) == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER || parseInt(data.userTxnData[i].transactionPurposeID) == CREDIT_NOTE_CUSTOMER || parseInt(data.userTxnData[i].transactionPurposeID) ==DEBIT_NOTE_CUSTOMER || parseInt(data.userTxnData[i].transactionPurposeID) == CREDIT_NOTE_VENDOR || parseInt(data.userTxnData[i].transactionPurposeID) == DEBIT_NOTE_VENDOR || parseInt(data.userTxnData[i].transactionPurposeID) == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER || parseInt(data.userTxnData[i].transactionPurposeID) == CANCEL_INVOICE){
                    $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="payementDiv"]').hide();
                }else{
                    $("#transactionTable").find('tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="payementDiv"]').show();
                }
                if(parseInt(data.userTxnData[i].transactionPurposeID) == SELL_ON_CASH_COLLECT_PAYMENT_NOW || parseInt(data.userTxnData[i].transactionPurposeID) == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
                    //$("#transactionEntity"+data.userTxnData[i].id+" div[class='FrieghtChargesDiv']").append('<b>FREIGHT CHARGES:</b><p style="color: blue;">'+data.userTxnData[i].frieghtCharges+'</p>');
                    $("#transactionEntity"+data.userTxnData[i].id+" div[class='poReferenceDiv']").append('<b>PO REFERENCE:</b><p style="color: blue;">'+data.userTxnData[i].poReference+'</p>');
                }
                $("#transactionEntity"+data.userTxnData[i].id+" p[class='txnItemDesc']").html("");
                //$("#transactionEntity"+data.userTxnData[i].id+" p[class='txnItemDesc']").append('<input type="button" value="Items List" id="multiSellItemsList" class="btn btn-submit btn-idos" onclick="listMultiSellItems(this);">');
                $("#transactionEntity"+data.userTxnData[i].id+" p[class='txnItemDesc']").append('<a id="multiSellItemsList" href="#pendingExpense" onclick="listMultiSellItems(this);">Items List</a>');
                if(data.userTxnData[i].status=='Accounted' && parseInt(data.userTxnData[i].transactionPurposeID) != RECEIVE_ADVANCE_FROM_CUSTOMER && parseInt(data.userTxnData[i].transactionPurposeID) != CREDIT_NOTE_CUSTOMER && parseInt(data.userTxnData[i].transactionPurposeID) != DEBIT_NOTE_CUSTOMER && parseInt(data.userTxnData[i].transactionPurposeID) != CREDIT_NOTE_VENDOR && parseInt(data.userTxnData[i].transactionPurposeID) != DEBIT_NOTE_VENDOR && parseInt(data.userTxnData[i].transactionPurposeID) != CANCEL_INVOICE){
                    if( data.userTxnData[i].txnIdentifier != "2")
                        $("#transactionEntity"+data.userTxnData[i].id+" div[class='invoiceForm']").append('<b class="btn btn-submit btn-center">Download Invoice:</b><br><button id="XLSX'+data.userTxnData[i].id+'" class="exportInvoiceXlsx btn btn-submit exportInvoiceBtn" title="Export Invoice(xlsx)" onclick="generatePDFInvoice(this);"><i class="fa fa-file-excel-o fa-lg" aria-hidden="true"></i></button><button id="PDF1'+data.userTxnData[i].id+'" class="exportInvoicePdf btn btn-submit exportInvoiceBtn" title="Export Invoice(pdf)" onclick="generatePDFInvoice(this);"><i class="fa fa-file-pdf-o fa-lg" aria-hidden="true"></i></button>');
                    else if( data.userTxnData[i].txnIdentifier == "2"){
                        $("#transactionEntity"+data.userTxnData[i].id+" div[class='invoiceForm']").append('<b class="btn btn-submit btn-center">Download Invoice:</b><br><button id="XLSX'+data.userTxnData[i].id+'" class="exportInvoiceXlsx btn btn-submit exportInvoiceBtn" title="Export Invoice(xlsx)" onclick="grnReportDownload(this);"><i class="fa fa-file-excel-o" aria-hidden="true"></i></button>');
                    }

                    $("#transactionEntity"+data.userTxnData[i].id+" div[class='vendorInvDateDiv']").append('<b>INVOICE No:</b><p style="color: blue;">'+data.userTxnData[i].invoiceNumber+'</p>');
                }else if(data.userTxnData[i].status=='Accounted' && (parseInt(data.userTxnData[i].transactionPurposeID) == CREDIT_NOTE_CUSTOMER || parseInt(data.userTxnData[i].transactionPurposeID) == DEBIT_NOTE_CUSTOMER || parseInt(data.userTxnData[i].transactionPurposeID) == CREDIT_NOTE_VENDOR || parseInt(data.userTxnData[i].transactionPurposeID) == DEBIT_NOTE_VENDOR)){
                    if(parseInt(data.userTxnData[i].transactionPurposeID) == CREDIT_NOTE_CUSTOMER || parseInt(data.userTxnData[i].transactionPurposeID) == DEBIT_NOTE_CUSTOMER) {
                        $("#transactionEntity" + data.userTxnData[i].id + " div[class='invoiceForm']").append('<b class="btn btn-submit btn-center">Download Invoice:</b><br><button id="XLSX' + data.userTxnData[i].id + '" class="exportInvoiceXlsx btn btn-submit exportInvoiceBtn" title="Export Invoice(xlsx)" onclick="generateCustCdtDbtNote(this);"><i class="fa fa-file-excel-o fa-lg" aria-hidden="true"></i></button><button id="PDF1' + data.userTxnData[i].id + '" class="exportInvoicePdf btn btn-submit exportInvoiceBtn" title="Export Invoice(pdf)" onclick="generateCustCdtDbtNote(this);"><i class="fa fa-file-pdf-o fa-lg" aria-hidden="true"></i></button>');
                    }
                    $("#transactionEntity"+data.userTxnData[i].id+" div[class='vendorInvDateDiv']").append('<b>Note No:</b><p style="color: blue;">'+data.userTxnData[i].invoiceNumber+'</p>');
                }
            }else if(parseInt(data.userTxnData[i].transactionPurposeID) == BUY_ON_CASH_PAY_RIGHT_AWAY || parseInt(data.userTxnData[i].transactionPurposeID) == BUY_ON_CREDIT_PAY_LATER || parseInt(data.userTxnData[i].transactionPurposeID)== BUY_ON_PETTY_CASH_ACCOUNT || parseInt(data.userTxnData[i].transactionPurposeID)== PURCHASE_RETURNS || parseInt(data.userTxnData[i].transactionPurposeID) == BILL_OF_MATERIAL || parseInt(data.userTxnData[i].transactionPurposeID) == CREATE_PURCHASE_ORDER || parseInt(data.userTxnData[i].transactionPurposeID) == CREATE_PURCHASE_REQUISITION){
                $("#transactionEntity"+data.userTxnData[i].id+" p[class='txnItemDesc']").html("");
                if (
                  data.userTxnData[i].transactionPurposeID ==
                  CREATE_PURCHASE_ORDER
                ) {
                  $(
                    "#transactionEntity" +
                      data.userTxnData[i].id +
                      " p[class='txnItemDesc']"
                  ).append(
                    '<a id="multiSellItemsList" onclick="listMultiPOItems(this);">View PO</a>'
                  );
                } else if (
                    data.userTxnData[i].transactionPurposeID ==
                    CREATE_PURCHASE_REQUISITION
                  ) {
                    $(
                      "#transactionEntity" +
                        data.userTxnData[i].id +
                        " p[class='txnItemDesc']"
                    ).append(
                      '<a id="multiSellItemsList" onclick="listMultiPOItems(this);">View Details</a>'
                    );
                } else {
                  $(
                    "#transactionEntity" +
                      data.userTxnData[i].id +
                      " p[class='txnItemDesc']"
                  ).append(
                    '<a id="multiSellItemsList" onclick="listMultiSellItems(this);">Items List</a>'
                  );
                }
                if(data.userTxnData[i].status=="Accounted"){
                    if(parseInt(data.userTxnData[i].transactionPurposeID) == PURCHASE_RETURNS) {
                        $("#transactionEntity"+data.userTxnData[i].id+" div[class='invoiceForm']").append('<b class="btn btn-submit btn-center">Download Invoice:</b><br><button id="XLSX'+data.userTxnData[i].id+'" class="exportInvoiceXlsx btn btn-submit exportInvoiceBtn" title="Export Invoice(xlsx)" onclick="generatePDFBuySelfInvoice(this);"><i class="fa fa-file-excel-o fa-lg" aria-hidden="true"></i></button><button id="PDF1'+data.userTxnData[i].id+'" class="exportInvoicePdf btn btn-submit exportInvoiceBtn" title="Export Invoice(pdf)" onclick="generatePDFInvoice(this);"><i class="fa fa-file-pdf-o fa-lg" aria-hidden="true"></i></button>');
                    }
                    else if(parseInt(data.userTxnData[i].transactionPurposeID) == CREATE_PURCHASE_ORDER ){
                        $("#transactionEntity"+data.userTxnData[i].id+" div[class='invoiceForm']").append('<b class="btn btn-submit btn-center">Download Invoice:</b><br><button id="XLSX'+data.userTxnData[i].id+'" class="exportInvoiceXlsx btn btn-submit exportInvoiceBtn" title="Export Invoice(xlsx)" onclick="createPODocDownload(this);"><i class="fa fa-file-excel-o fa-lg" aria-hidden="true"></i></button><button id="PDF1'+data.userTxnData[i].id+'" class="exportInvoicePdf btn btn-submit exportInvoiceBtn" title="Export Invoice(pdf)" onclick="createPODocDownload(this);"><i class="fa fa-file-pdf-o fa-lg" aria-hidden="true"></i></button>');
                    }
                    if(parseInt(data.userTxnData[i].transactionPurposeID) == BUY_ON_CASH_PAY_RIGHT_AWAY || parseInt(data.userTxnData[i].transactionPurposeID) == BUY_ON_CREDIT_PAY_LATER){
                        if((data.userTxnData[i].typeOfSupply == "2")||(data.userTxnData[i].typeOfSupply == "3")){
                            $("#transactionEntity"+data.userTxnData[i].id+" div[class='invoiceForm']").append('<button id="'+data.userTxnData[i].id+'" class="exportInvoiceXlsx btn btn-submit exportInvoiceBtn" title="Export GRN/Inovice" onclick="popupBuyTxnInvoiceModal(this);" ><i class="fa fa-file-excel-o" aria-hidden="true"></i>Save Invoice/GRN</button>');
                        }
                        else {
                            $("#transactionEntity"+data.userTxnData[i].id+" div[class='invoiceForm']").append('<b class="btn btn-submit btn-center">Download Invoice:</b><br><button id="XLSX'+data.userTxnData[i].id+'" class="exportInvoiceXlsx btn btn-submit exportInvoiceBtn" title="Export Invoice(xlsx)" onclick="grnReportDownload(this);"><i class="fa fa-file-excel-o" aria-hidden="true"></i></button><button id="PDF1'+data.userTxnData[i].id+'" class="exportInvoiceBtn exportInvoicePdf btn btn-submit" title="Export Invoice(pdf)" onclick="grnReportDownload(this);"><i class="fa fa-file-pdf-o" aria-hidden="true"></i></button>');
                        }
                    }
                    $("#transactionEntity"+data.userTxnData[i].id+" div[class='vendorInvDateDiv']").append('<b>INVOICE No:</b><p style="color: blue;">'+data.userTxnData[i].invoiceNumber+'</p>');
                }
            }else if((parseInt(data.userTxnData[i].transactionPurposeID) == REFUND_ADVANCE_RECEIVED || parseInt(data.userTxnData[i].transactionPurposeID) == REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE || parseInt(data.userTxnData[i].transactionPurposeID) ==PAY_VENDOR_SUPPLIER || parseInt(data.userTxnData[i].transactionPurposeID) == PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER) && data.userTxnData[i].status=="Accounted"){
                $("#transactionEntity"+data.userTxnData[i].id+" div[class='invoiceForm']").append('<form action="/exportReceiptPdf" class="'+data.userTxnData[i].transactionPurpose+'" name="receiptForm'+data.userTxnData[i].id+'" id="receiptForm'+data.userTxnData[i].id+'">'+
                    '<input type="button" value="Generate Voucher" id="generateReceipt" class="btn btn-submit btn-idos" onclick="generatePDFReceipt(this,'+data.userTxnData[i].transactionPurposeID+');"></form>');
                if(parseInt(data.userTxnData[i].transactionPurposeID) != PAY_VENDOR_SUPPLIER)
                    $("#transactionEntity"+data.userTxnData[i].id+" div[class='vendorInvDateDiv']").append('<b>DOCUMENT SERIAL NO:</b><p style="color: blue;">'+data.userTxnData[i].invoiceNumber+'</p>');
            }
            if(COMPANY_OWNER == COMPANY_PWC){
                $("#transactionEntity"+data.userTxnData[i].id + " .approverActionList").children().remove();
                $("#transactionEntity"+data.userTxnData[i].id + " .approverActionList").append('<option value="">Select an action</option><option value="4">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option>');
            }
        } else if(data.userTxnData[i].transactionPurpose=="Make Provision/Journal Entry"){
            var isPjeApproved = 0;
            var loggedUser=$("#hiddenuseremail").text();
            $("#transactionTable").find('tr[id="transactionProvisionEntity'+data.userTxnData[i].id+'"]').remove();
            if(data.userTxnData[i].createdBy==loggedUser){
                if(data.userTxnData[i].status=='Approved'){
                    isPjeApproved =1;
                    var $tr=$("#transactionTable tr:contains('"+data.userTxnData[i].txnDate+"'):last");
                    if($tr.length>0){
                        $($tr).after('<tr id="transactionProvisionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                            '<br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                            '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>GROSS:</b><br/><p class="number-right">'+data.userTxnData[i].grossAmount+'</p></div></td>'+
                            '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div><br/></div></td>'+
                            '<td><div class="rowToExpand"><div class="txnstat">'+data.userTxnData[i].status+'</div>'+
                            '<input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit btn-idos" onclick="completeProvisionAccounting(this)" style="margin-top:10px;margin-left: 0px;"/><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></td>'+

                            '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+
                            '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                            '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td>'+
                            '</tr>').after($("#transactionTable tr[id='"+$tr+"']"));
                        console.log("PJE1= " +  data.userTxnData[i].txnReferenceNo);
                    }else{
                        $("#transactionTable").append('<tr id="transactionProvisionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                            '<br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                            '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>GROSS:</b><br/><p class="number-right">'+data.userTxnData[i].grossAmount+'</p></div></td>'+
                            '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div><br/></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userTxnData[i].status+'</div>'+
                            '<input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit btn-idos" onclick="completeProvisionAccounting(this)" style="margin-top:10px;margin-left: 0px;"><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></td>'+
                            '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+
                            '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                            '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td>'+
                            '</tr>');
                        console.log("PJE2= " +  data.userTxnData[i].txnReferenceNo);
                    }
                }else{
                    if(data.userTxnData[i].status=='Require Clarification'){
                        var $tr=$("#transactionTable tr:contains('"+data.userTxnData[i].txnDate+"'):last");
                        if($tr.length>0){
                            $($tr).after('<tr id="transactionProvisionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>GROSS:</b><br/><p class="number-right">'+data.userTxnData[i].grossAmount+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div><br/></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div>'+
                                '<br/><input type="button" value="Clarify Transaction" id="clarifyTxn" class="btn btn-submit btn-idos" onclick="clarifyProvisionTransaction(this)" style="margin-top:10px;margin-left: 0px;width:170px;"><br/><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p><div class="invoiceForm"></div></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+
                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></div></td></tr>').after($("#transactionTable tr[id='"+$tr+"']"));
                            console.log("PJE3= " +  data.userTxnData[i].txnReferenceNo);
                        }else{
                            $("#transactionTable").append('<tr id="transactionProvisionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>GROSS:</b><br/><p class="number-right">'+data.userTxnData[i].grossAmount+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div><br/></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div>'+
                                '<br/><input type="button" value="Clarify Transaction" id="clarifyTxn" class="btn btn-submit btn-idos" onclick="clarifyProvisionTransaction(this)" style="margin-top:10px;margin-left: 0px;width:170px;"><br/><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p><div class="invoiceForm"></div></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select><br>'+
                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></div></td></tr>');
                            console.log("PJE4= " +  data.userTxnData[i].txnReferenceNo);
                        }
                    }else{
                        var $tr=$("#transactionTable tr:contains('"+data.userTxnData[i].txnDate+"'):last");
                        if($tr.length>0){
                            $($tr).after('<tr id="transactionProvisionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>GROSS:</b><br/><p class="number-right">'+data.userTxnData[i].grossAmount+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div><br/></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><br/><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div><div class="invoiceForm"></div></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+
                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>').after($("#transactionTable tr[id='"+$tr+"']"));
                            console.log("PJE5= " +  data.userTxnData[i].txnReferenceNo);
                        }else{
                            $("#transactionTable").append('<tr id="transactionProvisionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>GROSS:</b><br/><p class="number-right">'+data.userTxnData[i].grossAmount+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div><br/></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><br/><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div><div class="invoiceForm"></div></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+
                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>');
                            console.log("PJE6= " +  data.userTxnData[i].txnReferenceNo);
                        }
                    }
                }
            }

            //for approver can be same user or not
            if(data.userTxnData[i].approverEmails !=null && data.userTxnData[i].approverEmails!="" && isPjeApproved != 1){
                if(data.userTxnData[i].approverEmails.indexOf(data.userTxnData[i].useremail)!=-1){
                    $("#transactionTable").find('tr[id="transactionProvisionEntity'+data.userTxnData[i].id+'"]').remove();
                    if(data.userTxnData[i].status=='Require Approval' || data.userTxnData[i].status=='Clarified'){
                        //check for user mail existence in the approver usermail list sent by server
                        //based on transaction status row data is displayed
                        var $tr=$("#transactionTable tr:contains('"+data.userTxnData[i].txnDate+"'):last");
                        if($tr.length>0){
                            $($tr).after('<tr id="transactionProvisionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>UNITS:</b><b>GROSS:</b><br/><p class="number-right">'+data.userTxnData[i].grossAmount+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userTxnData[i].status+'</div>'+
                                '<br/>Action:<select class="approverActionList" name="approverActionList" id="approverActionList"><option value="">--Please Select--</option>'+
                                '<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>'+
                                '<span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
                                '<input type="button" id="approverAction" class="btn btn-submit btn-idos" value="Submit" onclick="completePovisionAction(this)"><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+
                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>').after($("#transactionTable tr[id='"+$tr+"']"));
                            console.log("PJE7= " +  data.userTxnData[i].txnReferenceNo);
                        }else{
                            $("#transactionTable").append('<tr id="transactionProvisionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>UNITS:</b><b>GROSS:</b><br/><p class="number-right">'+data.userTxnData[i].grossAmount+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userTxnData[i].status+'</div>'+
                                '<br/>Action:<select class="approverActionList" name="approverActionList" id="approverActionList"><option value="">--Please Select--</option>'+
                                '<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>'+
                                '<span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
                                '<input type="button" id="approverAction" class="btn btn-submit btn-idos" value="Submit" onclick="completePovisionAction(this)"><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>');
                            console.log("PJE8= " +  data.userTxnData[i].txnReferenceNo);
                        }
                    }else{
                        var $tr=$("#transactionTable tr:contains('"+data.userTxnData[i].txnDate+"'):last");
                        if($tr.length>0){
                            $($tr).after('<tr id="transactionProvisionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>GROSS:</b><br/><p class="number-right">'+data.userTxnData[i].grossAmount+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>').after($("#transactionTable tr[id='"+$tr+"']"));
                            console.log("PJE9= " +  data.userTxnData[i].txnReferenceNo);
                        }else{
                            $("#transactionTable").append('<tr id="transactionProvisionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><b>BRANCH1:</b><br/><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>GROSS:</b><br/><p class="number-right">'+data.userTxnData[i].grossAmount+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>');
                            console.log("PJE10= " +  data.userTxnData[i].txnReferenceNo);
                        }
                    }
                }
            }

            if(data.userTxnData[i].selectedAdditionalApproval != 'null' && data.userTxnData[i].selectedAdditionalApproval !=""){
                if(data.userTxnData[i].selectedAdditionalApproval == data.userTxnData[i].useremail){
                    if(data.userTxnData[i].status=='Require Additional Approval'){
                        $("#transactionTable").find('tr[id="transactionProvisionEntity'+data.userTxnData[i].id+'"]').remove();
                        var $tr=$("#transactionTable tr:contains('"+data.userTxnData[i].txnDate+"'):last");
                        if($tr.length>0){
                            $($tr).after('<tr id="transactionProvisionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>GROSS:</b><br/><p class="number-right">'+data.userTxnData[i].grossAmount+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userTxnData[i].status+'</div>'+
                                '<br/>Action:<select class="approverActionList" name="approverActionList" id="approverActionList"><option value="">--Please Select--</option>'+
                                '<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>'+
                                '<span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
                                '<input type="button" id="approverAction" class="approverAction btn btn-submit btn-center" value="Submit" onclick="completePovisionAction(this)"><br/><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>').after($("#transactionTable tr[id='"+$tr+"']"));
                            console.log("PJE11= " +  data.userTxnData[i].txnReferenceNo);
                        }else{
                            $("#transactionTable").append('<tr id="transactionProvisionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>GROSS:</b><br/><p class="number-right">'+data.userTxnData[i].grossAmount+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userTxnData[i].status+'</div>'+
                                '<br/>Action:<select class="approverActionList" name="approverActionList" id="approverActionList"><option value="">--Please Select--</option>'+
                                '<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>'+
                                '<span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
                                '<input type="button" id="approverAction" class="approverAction btn btn-submit btn-center" value="Submit" onclick="completePovisionAction(this)"><br/><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>');
                            console.log("PJE12= " +  data.userTxnData[i].txnReferenceNo);
                        }
                    }else{
                        var $tr=$("#transactionTable tr:contains('"+data.userTxnData[i].txnDate+"'):last");
                        if($tr.length>0 && isPjeApproved != 1 && data.userTxnData[i].status != 'Accounted'){
                            $($tr).after('<tr id="transactionProvisionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].noOfUnit+'</p><br/><b>PRICE\\UNIT:</b><br/><p class="number-right">'+data.userTxnData[i].unitPrice+'</p><br/><b>GROSS:</b><br/><p class="number-right">'+data.userTxnData[i].grossAmount+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>').after($("#transactionTable tr[id='"+$tr+"']"));
                            console.log("PJE13= " +  data.userTxnData[i].txnReferenceNo);
                        }else if(isPjeApproved != 1){
                            $("#transactionTable").find('tr[id="transactionProvisionEntity'+data.userTxnData[i].id+'"]').remove();
                            $("#transactionTable").append('<tr id="transactionProvisionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].noOfUnit+'</p><br/><b>PRICE\\UNIT:</b><br/><p class="number-right">'+data.userTxnData[i].unitPrice+'</p><br/><b>GROSS:</b><br/><p class="number-right">'+data.userTxnData[i].grossAmount+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+
                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>');
                            console.log("PJE14= " +  data.userTxnData[i].txnReferenceNo);
                        }
                    }
                }else if(data.userTxnData[i].status=='Require Additional Approval'){
                    $("#transactionTable tr[id='transactionProvisionEntity"+data.userTxnData[i].id+"'] td:nth-child(8) div:first").append('<br/><b>Additional Approval Required By:</b><br/>'+data.userTxnData[i].selectedAdditionalApproval+'');
                }
            }
            var roles=data.userTxnData[i].roles;
            if(roles.indexOf("ACCOUNTANT")!=-1 || roles.indexOf("AUDITOR")!=-1 || roles.indexOf("CONTROLLER")!=-1){
                var approverEmailVal=false;var selectedAdditionalApproval=false;
                if(data.userTxnData[i].approverEmails!=null && data.userTxnData[i].approverEmails!=""){
                    if(data.userTxnData[i].approverEmails.indexOf(data.userTxnData[i].useremail)!=-1){
                        approverEmailVal=true;
                    }
                }
                if(data.userTxnData[i].selectedAdditionalApproval!=null && data.userTxnData[i].selectedAdditionalApproval!=""){
                    if(data.userTxnData[i].selectedAdditionalApproval==data.userTxnData[i].useremail){
                        selectedAdditionalApproval=true;
                    }
                }
                if(data.userTxnData[i].createdBy!=loggedUser && approverEmailVal==false && selectedAdditionalApproval==false){
                    $("#transactionTable").append('<tr id="transactionProvisionEntity'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"> <a ' + (data.userTxnData[i].status  == "Accounted" ?  ' ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '"': '' )+ '>'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                        '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                        '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].noOfUnit+'</p><br/><b>PRICE\\UNIT:</b><br/><p class="number-right">'+data.userTxnData[i].unitPrice+'</p><br/><b>GROSS:</b><br/><p class="number-right">'+data.userTxnData[i].grossAmount+'</p></div></td>'+
                        '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><b>PROVISION PURPOSE:</b><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></div></td>'+
                        '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+
                        '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                        '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>');
                    console.log("PJE16= " +  data.userTxnData[i].txnReferenceNo);
                }
            }

            if(data.userTxnData[i].additionalapproverEmails!=null && data.userTxnData[i].additionalapproverEmails!=""){
                var additionalApprovarUsersList=data.userTxnData[i].additionalapproverEmails.substring(0,data.userTxnData[i].additionalapproverEmails.length).split(',');
                $("tr[id='transactionProvisionEntity"+data.userTxnData[i].id+"'] select[id='userAddApproval']").append('<option value="">--Please Select--</option>');
                for(var j=0;j<additionalApprovarUsersList.length;j++){
                    $("tr[id='transactionProvisionEntity"+data.userTxnData[i].id+"'] select[id='userAddApproval']").append('<option value="'+additionalApprovarUsersList[j]+'">'+additionalApprovarUsersList[j]+'</option>');
                }
            }
            /*if(data.userTxnData[i].debitItemsName !=null && data.userTxnData[i].debitItemsName!=""){
                    $('#transactionProvisionEntity'+data.userTxnData[i].id+' div[class="provisionItemName"]').append('<b style="color: #102E55;">Debit: </b>');
                $('#transactionProvisionEntity'+data.userTxnData[i].id+' div[class="provisionItemName"]').append('<p style="color: blue;">'+data.userTxnData[i].debitItemsName+'</p>');
            }
            if(data.userTxnData[i].creditItemsName !=null && data.userTxnData[i].creditItemsName!=""){
                    $('#transactionProvisionEntity'+data.userTxnData[i].id+' div[class="provisionItemName"]').append('<b style="color: #102E55;">Credit: </b>');
                $('#transactionProvisionEntity'+data.userTxnData[i].id+' div[class="provisionItemName"]').append('<p style="color: blue;">'+data.userTxnData[i].creditItemsName+'</p>');
            }*/
            if(data.userTxnData[i].txnRemarks!=null && data.userTxnData[i].txnRemarks!=""){
                var individualRemarks;
                if(data.userTxnData[i].txnRemarks.lastIndexOf("|") != -1){
                    individualRemarks = data.userTxnData[i].txnRemarks.substring(0,data.userTxnData[i].txnRemarks.length).split('|');
                }else{
                    individualRemarks = data.userTxnData[i].txnRemarks.substring(0,data.userTxnData[i].txnRemarks.length).split(',');
                }
                for(var m=0;m<individualRemarks.length;m++){
                    var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
                    $('#transactionProvisionEntity'+data.userTxnData[i].id+' div[class="txnWorkflowRemarks"]').append('<p style="color: #004e00;"><b>'+emailAndRemarks[0]+'</b></p>#');
                    if(typeof emailAndRemarks[1]!='undefined'){
                        if(emailAndRemarks[0].indexOf("Auditor")!=-1){
                            $('#transactionProvisionEntity'+data.userTxnData[i].id+' div[class="txnWorkflowRemarks"]').append('<h1auditor>'+emailAndRemarks[1]+'</h1auditor><br/>');
                        }else{
                            $('#transactionProvisionEntity'+data.userTxnData[i].id+' div[class="txnWorkflowRemarks"]').append('<p style="color: blue;">'+emailAndRemarks[1]+'</p><br/>');
                        }
                    }
                }
            }
            if(data.userTxnData[i].createdBy==data.userTxnData[i].approverEmail){
                if(data.userTxnData[i].status=='Approved' && isPjeApproved == 0){
                    $("tr[id='transactionProvisionEntity"+data.userTxnData[i].id+"'] td:nth-child(9)").append('<input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit btn-idos" onclick="completeProvisionAccounting(this);"/>');
                }
            }
            if(data.userTxnData[i].txnDocument!="" && data.userTxnData[i].txnDocument!=null){
                var txndocument=data.userTxnData[i].txnDocument;
                var mainTblTrId = "transactionProvisionEntity" + data.userTxnData[i].id;
                fillSelectElementWithUploadedDocs(txndocument, mainTblTrId, 'txnViewListUpload');
            }
            if(data.userTxnData[i].status!="" && data.userTxnData[i].status=="Rejected"){
                $("#transactionTable").find('tr[id="transactionProvisionEntity'+data.userTxnData[i].id+'"] div[class="txnstat"]').attr('class','txnstatred');
                $("#transactionTable").find('tr[id="transactionProvisionEntity'+data.userTxnData[i].id+'"] div[class="txnstatgreen"]').attr('class','txnstatred');
                $("#transactionTable").find('tr[id="transactionProvisionEntity'+data.userTxnData[i].id+'"] div[class="txnstatred"]').attr('class','txnstatred');
            } else if(data.userTxnData[i].status!="" && data.userTxnData[i].status=="Accounted"){
                $("#transactionTable").find('tr[id="transactionProvisionEntity'+data.userTxnData[i].id+'"] div[class="txnstat"]').attr('class','txnstat');
                $("#transactionTable").find('tr[id="transactionProvisionEntity'+data.userTxnData[i].id+'"] div[class="txnstatgreen"]').attr('class','txnstat');
                $("#transactionTable").find('tr[id="transactionProvisionEntity'+data.userTxnData[i].id+'"] div[class="txnstatred"]').attr('class','txnstat');
            } else if(data.userTxnData[i].status!="" && (data.userTxnData[i].status=="Approved" || data.userTxnData[i].status=="Require Approval" || data.userTxnData[i].status=="Require Additional Approval")){
                $("#transactionTable").find('tr[id="transactionProvisionEntity'+data.userTxnData[i].id+'"] div[class="txnstat"]').attr('class','txnstatgreen');
                $("#transactionTable").find('tr[id="transactionProvisionEntity'+data.userTxnData[i].id+'"] div[class="txnstatgreen"]').attr('class','txnstatgreen');
                $("#transactionTable").find('tr[id="transatransactionProvisionEntityctionEntity'+data.userTxnData[i].id+'"] div[class="txnstatred"]').attr('class','txnstatgreen');
            }
            if(COMPANY_OWNER == COMPANY_PWC){
                $("#transactionProvisionEntity"+data.userTxnData[i].id + " .approverActionList").children().remove();
                $("#transactionProvisionEntity"+data.userTxnData[i].id + " .approverActionList").append('<option value="">Select an action</option><option value="4">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option>');
            }
        }else if(data.userTxnData[i].transactionPurpose=="Process Payroll"){
            var isprApproved = 0;
            var loggedUser=$("#hiddenuseremail").text();
            $("#transactionTable").find('tr[id="transactionPayroll'+data.userTxnData[i].id+'"]').remove();
            if(data.userTxnData[i].createdBy==loggedUser){
                if(data.userTxnData[i].status=='Approved'){
                    isprApproved =1;
                    var $tr=$("#transactionTable tr:contains('"+data.userTxnData[i].txnDate+"'):last");
                    if($tr.length>0){
                        $($tr).after('<tr id="transactionPayroll'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;" class="branchDetails" id='+data.userTxnData[i].branchId+'>'+data.userTxnData[i].branchName+'</p><br/>'+
                            '<br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                            '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>Total Earnings:</b><p class="number-right">'+data.userTxnData[i].grossAmount+'</p><br><b>Total Deductions:</b><p class="number-right">'+data.userTxnData[i].totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.userTxnData[i].workingDays+'</p></div></td>'+
                            '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div><br/></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userTxnData[i].status+'</div>'+
                            '<select name="paryollreceiptdetail" id="paryollreceiptdetail" class="txnPaymodeCls" onchange="listAllBranchBankAccounts(this);"><optgroup label="Select Mode of reciept"></optgroup><option value="1">CASH</option><option value="2">BANK</option></select><input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit btn-idos" onclick="completePayrollAccounting(this)" style="margin-top:10px;margin-left: 0px;"><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></td>'+
                            '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+
                            '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                            '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td>'+
                            '</tr>').after($("#transactionTable tr[id='"+$tr+"']"));
                    }else{
                        $("#transactionTable").append('<tr id="transactionPayroll'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;" class="branchDetails" id='+data.userTxnData[i].branchId+'>'+data.userTxnData[i].branchName+'</p><br/>'+
                            '<br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                            '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>Total Earnings:</b><p class="number-right">'+data.userTxnData[i].grossAmount+'</p><br><b>Total Deductions:</b><p class="number-right">'+data.userTxnData[i].totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.userTxnData[i].workingdays+'</p></div></td>'+
                            '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div><br/></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userTxnData[i].status+'</div>'+
                            '<select name="paryollreceiptdetail" id="paryollreceiptdetail" class="txnPaymodeCls" onchange="listAllBranchBankAccounts(this);"><optgroup label="Select Mode of reciept"></optgroup><option value="1">CASH</option><option value="2">BANK</option></select><input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit btn-idos" onclick="completePayrollAccounting(this)" style="margin-top:10px;margin-left: 0px;"><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></td>'+
                            '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+
                            '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                            '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td>'+
                            '</tr>');
                    }
                }else{
                    if(data.userTxnData[i].status=='Require Clarification'){
                        var $tr=$("#transactionTable tr:contains('"+data.userTxnData[i].txnDate+"'):last");
                        if($tr.length>0){
                            $($tr).after('<tr id="transactionPayroll'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>Total Earnings:</b><p class="number-right">'+data.userTxnData[i].grossAmount+'</p><br><b>Total Deductions:</b><p class="number-right">'+data.userTxnData[i].totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.userTxnData[i].workingDays+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.netAmount+'</p></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div><br/></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div>'+
                                '<br/><input type="button" value="Clarify Transaction" id="clarifyTxn" class="btn btn-submit btn-idos" onclick="clarifyProvisionTransaction(this)" style="margin-top:10px;margin-left: 0px;width:170px;"><br/><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p><div class="invoiceForm"></div></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></div></td></tr>').after($("#transactionTable tr[id='"+$tr+"']"));
                        }else{
                            $("#transactionTable").append('<tr id="transactionPayroll'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>Total Earnings:</b><p class="number-right">'+data.userTxnData[i].grossAmount+'</p><br><b>Total Deductions:</b><p class="number-right">'+data.userTxnData[i].totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.userTxnData[i].workingDays+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.netAmount+'</p></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div><br/></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div>'+
                                '<br/><input type="button" value="Clarify Transaction" id="clarifyTxn" class="btn btn-submit btn-idos" onclick="clarifyProvisionTransaction(this)" style="margin-top:10px;margin-left: 0px;width:170px;"><br/><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p><div class="invoiceForm"></div></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></div></td></tr>');
                        }
                    }else{
                        var $tr=$("#transactionTable tr:contains('"+data.userTxnData[i].txnDate+"'):last");
                        if($tr.length>0){
                            $($tr).after('<tr id="transactionPayroll'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>Total Earnings:</b><p class="number-right">'+data.userTxnData[i].grossAmount+'</p><br><b>Total Deductions:</b><p class="number-right">'+data.userTxnData[i].totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.userTxnData[i].workingdays+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div><br/></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><br/><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div><div class="invoiceForm"></div></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>').after($("#transactionTable tr[id='"+$tr+"']"));
                        }else{
                            $("#transactionTable").append('<tr id="transactionPayroll'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>Total Earnings:</b><p class="number-right">'+data.userTxnData[i].grossAmount+'</p><br><b>TotalDeductions:</b><p class="number-right">'+data.userTxnData[i].totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.userTxnData[i].workingDays+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div><br/></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><br/><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div><div class="invoiceForm"></div></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>');
                        }
                    }
                }
            }

            //for approver can be same user or not
            if(data.userTxnData[i].approverEmails!=null && data.userTxnData[i].approverEmails!="" && isprApproved != 1){
                if(data.userTxnData[i].approverEmails.indexOf(data.userTxnData[i].useremail)!=-1){
                    $("#transactionTable").find('tr[id="transactionPayroll'+data.userTxnData[i].id+'"]').remove();
                    if(data.userTxnData[i].status=='Require Approval' || data.userTxnData[i].status=='Clarified'){
                        //check for user mail existence in the approver usermail list sent by server
                        //based on transaction status row data is displayed
                        var $tr=$("#transactionTable tr:contains('"+data.userTxnData[i].txnDate+"'):last");
                        if($tr.length>0){
                            $($tr).after('<tr id="transactionPayroll'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>Total Earnings:</b><p class="number-right">'+data.userTxnData[i].grossAmount+'</p><br><b>TotalDeductions:</b><p class="number-right">'+data.userTxnData[i].totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.userTxnData[i].workingDays+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userTxnData[i].status+'</div>'+
                                '<br/>Action:<select class="approverActionList" name="approverActionList" id="approverActionList"><option value="">--Please Select--</option>'+
                                '<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>'+
                                '<span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
                                '<input type="button" id="approverAction" class="btn btn-submit btn-idos" value="Submit" onclick="completePayrollAction(this)"><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>').after($("#transactionTable tr[id='"+$tr+"']"));
                        }else{
                            $("#transactionTable").append('<tr id="transactionPayroll'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>Total Earnings:</b><p class="number-right">'+data.userTxnData[i].grossAmount+'</p><br><b>TotalDeductions:</b><p class="number-right">'+data.userTxnData[i].totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.userTxnData[i].workingDays+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userTxnData[i].status+'</div>'+
                                '<br/>Action:<select class="approverActionList" name="approverActionList" id="approverActionList"><option value="">--Please Select--</option>'+
                                '<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>'+
                                '<span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
                                '<input type="button" id="approverAction" class="btn btn-submit btn-idos" value="Submit" onclick="completePayrollAction(this)"><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>');
                        }
                    }else{
                        var $tr=$("#transactionTable tr:contains('"+data.userTxnData[i].txnDate+"'):last");
                        if($tr.length>0){
                            $($tr).after('<tr id="transactionPayroll'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>Total Earnings:</b><p class="number-right">'+data.userTxnData[i].grossAmount+'</p><br><b>TotalDeductions:</b><p class="number-right">'+data.userTxnData[i].totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.userTxnData[i].workingDays+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>').after($("#transactionTable tr[id='"+$tr+"']"));
                        }else{
                            $("#transactionTable").append('<tr id="transactionPayroll'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>Total Earnings:</b><p class="number-right">'+data.userTxnData[i].grossAmount+'</p><br><b>Total Deductions:</b><p class="number-right">'+data.userTxnData[i].totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.userTxnData[i].workingDays+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>');
                        }
                    }
                }
            }

            if(data.userTxnData[i].selectedAdditionalApproval!=null && data.userTxnData[i].selectedAdditionalApproval!=""){
                if(data.userTxnData[i].selectedAdditionalApproval==data.userTxnData[i].useremail){
                    $("#transactionTable").find('tr[id="transactionPayroll'+data.userTxnData[i].id+'"]').remove();
                    if(data.userTxnData[i].status=='Require Additional Approval'){
                        var $tr=$("#transactionTable tr:contains('"+data.userTxnData[i].txnDate+"'):last");
                        if($tr.length>0){
                            $($tr).after('<tr id="transactionPayroll'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>Total Earnings:</b><p class="number-right">'+data.userTxnData[i].grossAmount+'</p><br><b>TotalDeductions:</b><p class="number-right">'+data.userTxnData[i].totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.userTxnData[i].workingDays+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userTxnData[i].status+'</div>'+
                                '<br/>Action:<select class="approverActionList" name="approverActionList" id="approverActionList"><option value="">--Please Select--</option>'+
                                '<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>'+
                                '<span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
                                '<input type="button" id="approverAction" class="approverAction btn btn-submit btn-center" value="Submit" onclick="completePayrollAction(this)"><br/><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>').after($("#transactionTable tr[id='"+$tr+"']"));
                        }else{
                            $("#transactionTable").append('<tr id="transactionPayroll'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><b>Total Earnings:</b><p class="number-right">'+data.userTxnData[i].grossAmount+'</p><br><b>TotalDeductions:</b><p class="number-right">'+data.userTxnData[i].totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.userTxnData[i].workingDays+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userTxnData[i].status+'</div>'+
                                '<br/>Action:<select class="approverActionList" name="approverActionList" id="approverActionList"><option value="">--Please Select--</option>'+
                                '<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>'+
                                '<span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
                                '<input type="button" id="approverAction" class="approverAction btn btn-submit btn-center" value="Submit" onclick="completePayrollAction(this)"><br/><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>');
                        }
                    }else{
                        var $tr=$("#transactionTable tr:contains('"+data.userTxnData[i].txnDate+"'):last");
                        if($tr.length>0){
                            $($tr).after('<tr id="transactionPayroll'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].noOfUnit+'</p><br/><b>PRICE\\UNIT:</b><br/><p class="number-right">'+data.userTxnData[i].unitPrice+'</p><br/><b>Total Earnings:</b><p class="number-right">'+data.userTxnData[i].grossAmount+'</p><br><b>Total Deductions:</b><p class="number-right">'+data.userTxnData[i].totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.userTxnData[i].workingDays+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>').after($("#transactionTable tr[id='"+$tr+"']"));
                        }else{
                            $("#transactionTable").append('<tr id="transactionPayroll'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                                '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].noOfUnit+'</p><br/><b>PRICE\\UNIT:</b><br/><p class="number-right">'+data.userTxnData[i].unitPrice+'</p><br/><b>Total Earnings:</b><p class="number-right">'+data.userTxnData[i].grossAmount+'</p><br><b>TotalDeductions:</b><p class="number-right">'+data.userTxnData[i].totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.userTxnData[i].workingDays+'</p></div></td>'+
                                '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></div></td>'+
                                '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                                '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                                '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>');
                        }
                    }
                }else if(data.userTxnData[i].status=='Require Additional Approval'){
                    $("#transactionTable tr[id='transactionPayroll"+data.userTxnData[i].id+"'] td:nth-child(8) div:first").append('<br/><b>Additional Approval Required By:</b><br/>'+data.userTxnData[i].selectedAdditionalApproval+'');
                }
            }
            var roles=data.userTxnData[i].roles;
            if(roles.indexOf("ACCOUNTANT") != -1 || roles.indexOf("AUDITOR") != -1 || roles.indexOf("CONTROLLER") != -1){
                var approverEmailVal=false; var selectedAdditionalApproval=false;
                if(data.userTxnData[i].approverEmails != null && data.userTxnData[i].approverEmails != ""){
                    if(data.userTxnData[i].approverEmails.indexOf(data.userTxnData[i].useremail)!=-1){
                        approverEmailVal=true;
                    }
                }
                if(data.userTxnData[i].selectedAdditionalApproval != 'null' && data.userTxnData[i].selectedAdditionalApproval != ""){
                    if(data.userTxnData[i].selectedAdditionalApproval==data.userTxnData[i].useremail){
                        selectedAdditionalApproval=true;
                    }
                }
                if(data.userTxnData[i].createdBy != loggedUser && approverEmailVal == false && selectedAdditionalApproval == false){
                    var $tr=$("#transactionTable tr:contains('"+data.userTxnData[i].txnDate+"'):last");
                    if($tr.length>0){
                        $($tr).after('<tr id="transactionPayroll'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                            '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                            '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].noOfUnit+'</p><br/><b>PRICE\\UNIT:</b><br/><p class="number-right">'+data.userTxnData[i].unitPrice+'</p><br/><b>Total Earnings:</b><p class="number-right">'+data.userTxnData[i].grossAmount+'</p><br><b>Total Deductions:</b><p class="number-right">'+data.userTxnData[i].totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.userTxnData[i].workingDays+'</p></div></td>'+
                            '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></div></td>'+
                            '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                            '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                            '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>').after($("#transactionTable tr[id='"+$tr+"']"));
                    }else{
                        $("#transactionTable").append('<tr id="transactionPayroll'+data.userTxnData[i].id+'" txnref="'+data.userTxnData[i].txnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.userTxnData[i].status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.userTxnData[i].txnReferenceNo+'</a></p></div></td>'+'<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
                            '<b>CREATOR:</b><br/><p style="color: blue;">'+data.userTxnData[i].createdBy+'</p></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
                            '<td><div class="rowToExpand"><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><br/><b>'+data.userTxnData[i].invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td><div class="rowToExpand" style="color: blue;">'+data.userTxnData[i].paymentMode+'<br>' + data.userTxnData[i].instrumentNumber +'<br>' + data.userTxnData[i].instrumentDate +'</div></td><td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].noOfUnit+'</p><br/><b>PRICE\\UNIT:</b><br/><p class="number-right">'+data.userTxnData[i].unitPrice+'</p><br/><b>Total Earnings:</b><p class="number-right">'+data.userTxnData[i].grossAmount+'</p><br><b>Total Deductions:</b><p class="number-right">'+data.userTxnData[i].totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.userTxnData[i].workingDays+'</p></div></td>'+
                            '<td><div class="rowToExpand"><p class="number-right">'+data.userTxnData[i].netAmount+'</p></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.userTxnData[i].netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.userTxnData[i].status+'</div><b>'+data.userTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userTxnData[i].approverEmail+'</p></div></div></td>'+
                            '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">Select a file</option></select>'+

                            '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
                            '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>');
                    }
                }
            }

            if(data.userTxnData[i].additionalapproverEmails!=null && data.userTxnData[i].additionalapproverEmails!=""){
                var additionalApprovarUsersList=data.userTxnData[i].additionalapproverEmails.substring(0,data.userTxnData[i].additionalapproverEmails.length).split(',');
                $("tr[id='transactionPayroll"+data.userTxnData[i].id+"'] select[id='userAddApproval']").append('<option value="">--Please Select--</option>');
                for(var j=0;j<additionalApprovarUsersList.length;j++){
                    $("tr[id='transactionPayroll"+data.userTxnData[i].id+"'] select[id='userAddApproval']").append('<option value="'+additionalApprovarUsersList[j]+'">'+additionalApprovarUsersList[j]+'</option>');
                }
            }
            if(data.userTxnData[i].itemName!=null && data.userTxnData[i].itemName!=""){
                var individualitemName=data.userTxnData[i].itemName.substring(0,data.userTxnData[i].itemName.length).split('|');
                if(individualitemName.length > 0){
                    $('#transactionPayroll'+data.userTxnData[i].id+' div[class="provisionItemName"]').append('<b style="color: #102E55;">Debit: </b>');
                    $('#transactionPayroll'+data.userTxnData[i].id+' div[class="provisionItemName"]').append('<p style="color: blue;">'+individualitemName[0]+'</p>');
                    $('#transactionPayroll'+data.userTxnData[i].id+' div[class="provisionItemName"]').append('<b style="color: #102E55;">Credit: </b>');
                    if(typeof individualitemName[1]!='undefined'){
                        $('#transactionPayroll'+data.userTxnData[i].id+' div[class="provisionItemName"]').append('<p style="color: blue;">'+individualitemName[1]+'</p>');
                    }
                }
            }
            if(data.userTxnData[i].txnRemarks!=null && data.userTxnData[i].txnRemarks!=""){
                var individualRemarks;
                if(data.userTxnData[i].txnRemarks.lastIndexOf("|") != -1){
                    individualRemarks = data.userTxnData[i].txnRemarks.substring(0,data.userTxnData[i].txnRemarks.length).split('|');
                }else{
                    individualRemarks = data.userTxnData[i].txnRemarks.substring(0,data.userTxnData[i].txnRemarks.length).split(',');
                }
                for(var m=0;m<individualRemarks.length;m++){
                    var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
                    $('#transactionPayroll'+data.userTxnData[i].id+' div[class="txnWorkflowRemarks"]').append('<p style="color: #004e00;"><b>'+emailAndRemarks[0]+'</b></p>#');
                    if(typeof emailAndRemarks[1]!='undefined'){
                        if(emailAndRemarks[0].indexOf("Auditor")!=-1){
                            $('#transactionPayroll'+data.userTxnData[i].id+' div[class="txnWorkflowRemarks"]').append('<h1auditor>'+emailAndRemarks[1]+'</h1auditor><br/>');
                        }else{
                            $('#transactionPayroll'+data.userTxnData[i].id+' div[class="txnWorkflowRemarks"]').append('<p style="color: blue;">'+emailAndRemarks[1]+'</p><br/>');
                        }
                    }
                }
            }

            if(data.userTxnData[i].txnDocument!="" && data.userTxnData[i].txnDocument!=null){
                var txndocument=data.userTxnData[i].txnDocument;
                var rowTxnId=data.userTxnData[i].id;
                var mainTblTrId = "transactionPayroll" + data.userTxnData[i].id;
                fillSelectElementWithUploadedDocs(txndocument, mainTblTrId, 'txnViewListUpload')
            }

            if(data.userTxnData[i].status!="" && data.userTxnData[i].status=="Rejected"){
                $("#transactionTable").find('tr[id="transactionPayroll'+data.userTxnData[i].id+'"] div[class="txnstat"]').attr('class','txnstatred');
                $("#transactionTable").find('tr[id="transactionPayroll'+data.userTxnData[i].id+'"] div[class="txnstatgreen"]').attr('class','txnstatred');
                $("#transactionTable").find('tr[id="transactionPayroll'+data.userTxnData[i].id+'"] div[class="txnstatred"]').attr('class','txnstatred');
            } else if(data.userTxnData[i].status!="" && data.userTxnData[i].status=="Accounted"){
                $("#transactionTable").find('tr[id="transactionPayroll'+data.userTxnData[i].id+'"] div[class="txnstat"]').attr('class','txnstat');
                $("#transactionTable").find('tr[id="transactionPayroll'+data.userTxnData[i].id+'"] div[class="txnstatgreen"]').attr('class','txnstat');
                $("#transactionTable").find('tr[id="transactionPayroll'+data.userTxnData[i].id+'"] div[class="txnstatred"]').attr('class','txnstat');
            } else if(data.userTxnData[i].status!="" && (data.userTxnData[i].status=="Approved" || data.userTxnData[i].status=="Require Approval" || data.userTxnData[i].status=="Require Additional Approval")){
                $("#transactionTable").find('tr[id="transactionPayroll'+data.userTxnData[i].id+'"] div[class="txnstat"]').attr('class','txnstatgreen');
                $("#transactionTable").find('tr[id="transactionPayroll'+data.userTxnData[i].id+'"] div[class="txnstatgreen"]').attr('class','txnstatgreen');
                $("#transactionTable").find('tr[id="transatransactionPayrollctionEntity'+data.userTxnData[i].id+'"] div[class="txnstatred"]').attr('class','txnstatgreen');
            }
        }

    }
}

var isPopulated=0;
$(document).ready(function(){
    $('.newSearchTransactionButton').click(function(){
        getVendorData();
        getBranchData();
        getProjectData();
        getCategoryData();
        if(isPopulated==0){
            getTransactionList();
            getTransactionStatuses();
            getPayModes();
            getTransactionExceptions();
            isPopulated=1;
        }
        $(".dynmBnchBankActList").remove();
        $("#socpnreceiptdetail").find('option:first').prop("selected","selected");
        $("#rcpfccpaymentdetail").find('option:first').prop("selected","selected");
        $("#rcafccpaymentdetail").find('option:first').prop("selected","selected");
        $("#rsaafvpaymentdetail").find('option:first').prop("selected","selected");
        $("#paymentDetails").find('option:first').prop("selected","selected");
        $("#searchTransaction").slideDown('slow');
        $("#searchTransaction input[type='text']").val("");
        //$('#searchTransaction select').find('option:first').prop("selected","selected");
        $('#searchTransaction textarea').val("");
        $("#pendingExpenseId").attr("class","active");
        $('#createExpense select').find('option:first').prop("selected","selected");
        $('#createExpense input[type="text"]').val("");
        $('#createExpense textarea').val("");
        $(".klBranchSpecfTd").text("");
        $(".itemParentNameDiv").text("");
        $(".combSalesItemDiv").text("");
        $(".inventoryItemInStock").text("");
        $(".customerVendorExistingAdvance").text("");
        $(".resultantAdvance").text("");
        $(".resultantAdvance").text("");
        $(".budgetDisplay").text("");
        $(".inputtaxbuttondiv").html("");
        $(".inputtaxcomponentsdiv").html("");
        $(".vendorActPayment").text("");
        $(".withholdingtaxcomponentdiv").text("");
        $("individualtaxdiv").text("");
        $("#bocaplunits").attr("readonly","");
        $("#bocpraunits").attr("readonly","");
        $(".actualbudgetDisplay").text("");
        $(".branchAvailablePettyCash").html("");
        $(".amountRangeLimitRule").text("");
        $(".discountavailable").text("");
        $(".netAmountDescriptionDisplay").text("");
        $("#procurementRequestRemarks").text("");
        $("#rcpfccvendcustoutstandingsgross").text("");
        $("#rcpfccvendcustoutstandingsnet").text("");
        $("#rcpfccvendcustoutstandingsnetdescription").text("");
        $("#rcpfccvendcustoutstandingspaid").text("");
        $("#rcpfccvendcustoutstandingsnotpaid").text("");
        $("#rcpfccvendcustoutstandingssalesreturn").text("");
        $("#mcpfcvvendcustoutstandingsgross").text("");
        $("#mcpfcvvendcustoutstandingsnet").text("");
        $("#mcpfcvvendcustoutstandingsnetdescription").text("");
        $("#mcpfcvvendcustoutstandingspaid").text("");
        $("#mcpfcvvendcustoutstandingsnotpaid").text("");
        $("#mcpfcvtxninprogress").text("");
        $("#mcpfcvvendcustoutstandingspurchasereturn").text("");
        $(".transactionDetailsTable").each(function(){
            $(this).hide();
        });
        $("#createExpense").slideUp('slow');
    });
});

//search functionality
var searchTransactionCriteriaBased = function(fromRecord, toRecord) {
    var chartOfAccountCategory=$("#searchCategory option:selected").val();
    var txnRefNumber=$("#searchTxnRefNumber").val();
    var chartOfAccountItem=$('#searchItems option:selected').map(function () {
        if(this.value!="multiselect-all"){
            return this.value;
        }
    }).get();
    var txnStatus1=$('#searchTxnStatus option:selected').map(function () {
        if(this.value!="multiselect-all"){
            return this.value;
        }
    }).get();
    // var txnStatus = '\'' + txnStatus1.toString().split(',').join('\',\'') + '\'';
    var txnStatus = "'" + txnStatus1.join("','") + "'";
    var fromTxnDate=$("#searchFromDate").val();
    var toTxnDate=$("#searchToDate").val();
    var txnBranch=$('#searchBranch option:selected').map(function () {
        if(this.value!="multiselect-all"){
            return this.value;
        }
    }).get();
    var txnProject=$('#searchProject option:selected').map(function () {
        if(this.value!="multiselect-all"){
            return this.value;
        }
    }).get();
    var amountRangeLimtFrom=$("#amountFrom").val();
    var amountRangeLimtTo=$("#amountTo").val();
    var withwithoutdocument=$("#searchWithWithoutSuppDoc option:selected").val();
    var payMode=$('#txnSearchPayMode option:selected').map(function () {
        if(this.value!="multiselect-all"){
            return this.value;
        }
    }).get();
    var withwithoutremarks=$("#txnSearchRemarks option:selected").val();
    var txnException=$('#txnSearchException option:selected').map(function () {
        if(this.value!="multiselect-all"){
            return this.value;
        }
    }).get();
    /*var txnpurchaseVendor=$('#searchVendor option:selected').map(function () {
        if(this.value!="multiselect-all"){
            return this.value;
        }
    }).get();
    var txnsalesCustomer=$('#searchCustomer option:selected').map(function () {
        if(this.value!="multiselect-all"){
            return this.value;
        }
    }).get();*/
    var txnpurchaseVendor = "";
    if($("#vInput").val() != ""){
        txnpurchaseVendor=$("#vendIDDiv").val();    
    }
    var txnsalesCustomer="";
    if($("#cInput").val() != ""){
        txnsalesCustomer=$("#custIDDiv").val();
    }
    var jsonData={};
    if(chartOfAccountCategory!=""){
        jsonData.searchCategory=chartOfAccountCategory;
    }
    if(txnRefNumber!=""){
        jsonData.searchTransactionRefNumber=txnRefNumber;
    }
    if(chartOfAccountItem!=""){
        jsonData.searchItems=chartOfAccountItem.toString();
    }
    if(txnStatus!=""){
        jsonData.searchTxnStatus=txnStatus.toString();
    }
    if(txnStatus1==""){
        jsonData.searchTxnStatus="";
    }
    if(fromTxnDate!=""){
        jsonData.searchTxnFromDate=fromTxnDate;
    }
    if(toTxnDate!=""){
        jsonData.searchTxnToDate=toTxnDate;
    }
    if(txnBranch!=""){
        jsonData.searchTxnBranch=txnBranch.toString();
    }
    if(txnProject!=""){
        jsonData.searchTxnProjects=txnProject.toString();
    }
    if(amountRangeLimtFrom!=""){
        jsonData.searchAmountRanseLimitFrom=amountRangeLimtFrom;
    }
    if(amountRangeLimtTo!=""){
        jsonData.searchAmountRanseLimitTo=amountRangeLimtTo;
    }
    if(withwithoutdocument!=""){
        jsonData.searchTxnWithWithoutDoc=withwithoutdocument;
    }
    if(payMode!=""){
        jsonData.searchTxnPyMode=payMode.toString();
    }
    if(withwithoutremarks!=""){
        jsonData.searchTxnWithWithoutRemarks=withwithoutremarks;
    }
    if(txnException!=""){
        jsonData.searchTxnException=txnException.toString();
    }
    if(txnpurchaseVendor!=""){
        jsonData.searchVendors=txnpurchaseVendor.toString();
    }
    if(txnsalesCustomer!=""){
        jsonData.searchCustomers=txnsalesCustomer.toString();
    }
    var txnUserType=$("#searchUserType option:selected").val();
    var txnQuestion1=$('#searchTxnQuestion option:selected').map(function () {
        if(this.value!="multiselect-all"){
            return this.value;
        }
    }).get();

    if(txnQuestion1!=""){
        jsonData.txnQuestion=txnQuestion1.toString();
    }
    if(txnUserType!=""){
        jsonData.txnUserType=txnUserType;
    }
    jsonData.useremail=$("#hiddenuseremail").text();

    jsonData.perPage = PER_PAGE_TXN;
    jsonData.fromRecord = fromRecord;
    jsonData.toRecord = toRecord;

    var url="/transaction/userTxnSearchBased";
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    $.ajax({
        url: url,
        data:JSON.stringify(jsonData),
        type:"text",
        headers:{
            "X-AUTH-TOKEN": window.authToken
        },
        method:"POST",
        contentType:'application/json',
        success: function (data) {
            $("#transactionTable tbody").html("");
            //displayTransactionRecords(data);
            //setPagingDetail('transactionTable', 20, 'pagingTransactionNavPosition');
            let totalRecords = data.totalRecords;
            displayTransactionRecords(data);

            if(parseInt(fromRecord) === 0) {
                setPagingDetailTxn('transactionTable', PER_PAGE_TXN, 'pagingTransactionNavPosition', totalRecords);
            }
            CALLED_METHOD = 'srch';
        },
        error: function (xhr, status, error) {
            if(xhr.status == 401){
                doLogout();
            }else if(xhr.status == 500){
                swal("Error on search Transaction!", "Please retry, if problem persists contact support team", "error");
            }else if(xhr.status == 502){
                swal("Error on search Transaction!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
}

function getTransacionAccountingReport(elem){
    var jsonData = {};
    jsonData.entityTxnId=document.getElementById('transactionId').innerHTML;
    var url="/exportTxnAccountingData";
    jsonData.reportType = $(elem).attr('id') == "downloadAsPDF" ? "pdf" : "xlsx";
    downloadFile(url, "POST", jsonData, "Error on invoice generation!");

}

function getTransacionAccountingInfo(elem) {
    let parentTr = $(elem).closest('tr').attr('id');
    let transactionEntityId = parentTr.substring(26, parentTr.length);
    let transactionRefNumber =   $(elem).closest('tr').attr('txnref');
    let jsonData = {};
    jsonData.transactionEntityId = transactionEntityId;
    jsonData.transactionNumber = transactionRefNumber;
    let url="/transaction/getAccountingInfo";
    $.ajax({
        url: url,
        data:JSON.stringify(jsonData),
        type:"text",
        headers:{
            "X-AUTH-TOKEN": window.authToken
        },
        async: false,
        method:"POST",
        contentType:'application/json',
        success: function (data) {
            $("#myAccoungtingModal").attr('data-toggle', 'modal');
            $("#myAccoungtingModal").modal('show');


            $("#accountingTableBody").empty();

            document.getElementById('accountingDate').innerHTML = 'newtext';
            document.getElementById('transactionId').innerHTML = transactionRefNumber;

            if(data.accountingItems.length>0){

                for (var i = 0; i < data.accountingItems.length; i++) {
                    $("#myAccoungtingModal div[class='modal-body'] table[id='accountingTable'] tbody")
                        .append('' +
                            '<tr>' +
                            '<td style="width:40px">'+ data.accountingItems[i].sroId +'</td>' +
                            '<td>' +  data.accountingItems[i].accountingDetails + '</td>' +
                            '<td style="text-align: right">' + data.accountingItems[i].debitBalance + '</td>' +
                            '<td style="text-align: right">' + data.accountingItems[i].creditBalance + '</td>' +
                            '</tr>');
                }
                console.log("TOTAL: " + data.totalDebit);
                document.getElementById('totalDebit').innerHTML = data.totalDebit;
                document.getElementById('totalCredit').innerHTML = data.totalCredit;
                document.getElementById('accountingDate').innerHTML = data.createdAt;
            }
        },
        error: function (xhr, status, error) {
            if(xhr.status == 401){ doLogout(); }else if(xhr.status == 500){
                swal("Error on fetching accounting information of transaction!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
    //downloadFile(url, "POST", jsonData, "Error on accouting generation!");

}
