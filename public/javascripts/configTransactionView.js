function viewTransactionData(data) {
    enableTransactionButtons();
		if(data.workflowAction=="requestHiring" || data.workflowAction=="actionHiring"){
			  var useremail=data.useremail;
			  var id=data.id;
		      var projectNumber=data.projectNumber;
		      var requester=data.requester;
		      var requetType=data.requetType;
		      var projectTitle=data.projectTitle;
		      var position=data.position;
		      var status=data.status;
		      var approverEmailList=data.approverEmailList;
		      var remark='', display = '';
		      if (null !== data.remarks && "" !== data.remarks) {
		    	    remark = data.remarks;
		    	    remark = remark.substring(0,remark.length).split(',');
				   	for(var m=0;m<remark.length;m++){
				    	var emailAndRemarks=remark[m].substring(0, remark[m].length).split('#');
				    	display += '<font color="102E55"><b>'+emailAndRemarks[0]+'</b></font>#';
				    	if(typeof emailAndRemarks[1]!='undefined'){
				    		display += '<font color="blue">'+emailAndRemarks[1]+'</font><br/>';
				    	}
				    }
			  }
		      var remarks = '<div style="min-height: 80px; max-height: 80px; max-width: 230px; overflow-y: auto; word-wrap: break-word;" id="hiringRemarkDiv_' + id + '">' + display + '</div>'
							+ '<div class="hiringExtra_' + id + '" style="display: none;">Remarks<br/><textarea id="hiringRemarks_' + id + '" style="height: 47px; width: 152px;"></textarea>';
			  var document = '<select id="hiringDocument_' + id + '" style="width: 175px;"><option value="">--Please Select--</option></select>'
					      	+ '<br/><div class="hiringExtra_' + id + '" style="display: none; position: relative; top: 50px;">Upload<br/>'
							+ '<input type="text" style="width:161px;" id="hiringDocuments_' + id + '" name="hiringuploadSuppDocs" readonly="readonly">'
							+ '<input type="button" id="hiringuploadSuppDocs" value="Upload Document" style="width:175px;" class="btn btn-primary btn-idos" onclick="uploadFile(this.id)"></div>';

		      if(data.role.includes("CREATOR")){
				  if(requester==useremail){
					  var labourTxnRow=$("#pendingLabourTable tr[id='labourTransaction"+id+"']").attr('id');
				      if(typeof labourTxnRow=='undefined'){
				      	$("#pendingLabourTable").prepend('<tr id="labourTransaction'+id+'"><td>'+projectNumber+'</td><td>'+requetType+'</td><td>'+projectTitle+'</td><td>'+position+'</td><td>' + document + '</td><td>' + remarks + '</td><td id="actionHiringId"></td></tr>');
				      }
				  }
		      }
		      if(data.role.includes("APPROVER")){
			        if(approverEmailList.indexOf(useremail)!=-1){
			        	var labourTxnRow=$("#pendingLabourTable tr[id='labourTransaction"+id+"']").attr('id');
					    if(typeof labourTxnRow=='undefined'){
					      $("#pendingLabourTable").prepend('<tr id="labourTransaction'+id+'"><td>'+projectNumber+'</td><td>'+requetType+'</td><td>'+projectTitle+'</td><td>'+position+'</td><td>' + document + '</td><td>' + remarks + '</td><td id="actionHiringId"></td></tr>');
					    }
			        }
		      }
		      $('#hiringRemarkDiv_' + id).html(display);
		      var document1='';
			    if (null !== data.document && "" !== data.document) {
			      document1 = data.document;
     			  var transTrID = 'labourTransaction'+id;
				  fillSelectElementWithUploadedDocs(document1, transTrID, 'hiringDocument_'+id);
			    }
		      if(data.role.includes("CREATOR")){
		      	var userroles = data.role;
				if(requester==useremail){
			    	  if(status=="position_requested"){
			    	   $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('Await Approval&nbsp;&nbsp;<span id="' + id + '" class="labour-view" onclick="viewLabourDetails(this);"><font color="blue"></font></span>');
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
			        }
			        if(status=="employee_identified"){
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('Employee Identified&nbsp;&nbsp;<span id="' + id + '" class="labour-print" onclick="printLabourDetails(this);"><font color="blue"></font></span>');
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
			        }
			        if(status=="position_approved"){
		             $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
		             $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('<div class="reg-block" id="logged-out-block"><a id='+id+' href="#labourHiring" class="btn-approve btn btn-primary btn-idos" onClick="javascript:doAction(this);">Submit Employee Details</a></div>');
		             if(!(userroles.indexOf("APPROVER")!=-1)){
		            	 $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').show();
		             } else {
				           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
		             }
		          }
		          if(status=="client_approval_sent"){
		             $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
		             $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('Sent For Client Approval');
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
		          }
		          if(status=="client_approved"){
		             $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
		             $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('<div class="reg-block" id="logged-out-block"><a id='+id+' href="#labourHiring" class="btn-approve btn btn-primary btn-idos" onClick="javascript:action(this);">Issue employment agreement</a></div>');
		             if(!(userroles.indexOf("APPROVER")!=-1)){
		            	 $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').show();
		             } else {
				           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
		             }
		          }
		          if(status=="Employee Agreement Issued"){
		             $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
		             $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('Agreement Issued&nbsp;&nbsp;<span id="' + id + '" class="labour-print" onclick="printLabourDetails(this);"><font color="blue"></font></span>');
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
		          }
			    }
		      }
		      if(data.role.includes("APPROVER")){
				if(approverEmailList.indexOf(useremail)!=-1){
					if($("#pendingLabourTable tr[id='labourTransaction"+id+"']").children().length==7){
						$("#pendingLabourTable tr[id='labourTransaction"+id+"']").prepend('<td>'+requester+'</td>');
				    }
			    	  if(status=="position_requested"){
			    	   $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('<div class="reg-block" id="logged-out-block"><a id='+id+' href="#labourHiring" class="btn-approve btn btn-primary btn-idos" onClick="javascript:doAction(this);">Approve Position</a></div>&nbsp;&nbsp;<span id="' + id + '" class="labour-view" onclick="viewLabourDetails(this);"><font color="blue"></font></span>');
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').show();
			        }
			        if(status=="employee_identified"){
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('<div class="reg-block" id="logged-out-block"><a id='+id+' data-toggle="modal" href="#labourHiring" class="btn-approve btn btn-primary btn-idos" onClick="javascript:action(this);">Obtain client approval</a></div>');
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
			        }
			        if(status=="position_approved"){
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('Position Approved');
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
			        }
			        if(status=="client_approval_sent"){
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('<div class="reg-block" id="logged-out-block"><a id='+id+' href="#labourHiring" class="btn-approve btn btn-primary btn-idos" onClick="javascript:action(this);">Click Once Client Approves</a></div>');
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').show();
			        }
			        if(status=="client_approved"){
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('Client Approved&nbsp;&nbsp;<span id="' + id + '" class="labour-print" onclick="printLabourDetails(this);"><font color="blue"></font></span>');
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
			        }
			        if(status=="Employee Agreement Issued"){
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('Agreement Issued&nbsp;&nbsp;<span id="' + id + '" class="labour-print" onclick="printLabourDetails(this);"><font color="blue"></font></span>');
			           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
			        }
			    }
		    }
		}

		if(data.txnType=="chat"){
			chat.displayMessage(data);
		}else if(data.txnType=="onlineUsers"){
			chatAvailableSuccess(data);
		}else if(data.txnType=="sellExpenseTxn"){
			var TXN_PURPOSE_ID = parseInt(data.transactionPurposeID);
			var useremail=data.useremail;
		    $("#transactionTable").find('tr[id="transactionEntity'+data.id+'"]').remove();
			var tableRecord = "";
			var isAlreadyAdded = 0;
		    if(data.createdBy==useremail){
				//based on transaction status row data is displayed
				if(data.status=='Approved'){
					if(data.transactionPurpose=="Sales returns" || data.transactionPurpose=="Purchase returns" || TXN_PURPOSE_ID ==BILL_OF_MATERIAL || TXN_PURPOSE_ID ==CREATE_PURCHASE_ORDER || TXN_PURPOSE_ID == CREATE_PURCHASE_REQUISITION){ //do not display cash/bank payementDiv
						tableRecord = '<tr id="transactionEntity'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
						'<b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.itemName+'</p><br/><b>IMMEDIATE PARENT:</b><br/><font color="blue">'+data.itemParentName+'</font></div></td><td><div class="rowToExpand"><font color="blue">'+data.customerVendorName+'</font><br/><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><div class="vendorInvDateDiv"><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td>';
						tableRecord += '<td><div class="rowToExpand"><p class="number-right">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b>';
						if(TXN_PURPOSE_ID !=BILL_OF_MATERIAL && TXN_PURPOSE_ID != CREATE_PURCHASE_REQUISITION){
							tableRecord += '<br/><p class="number-right">'+data.unitPrice+'</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">'+data.grossAmount+'</p>';
						}
						tableRecord += '</div></td>';
						tableRecord += '<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/><b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;"></div><div class="outstandings" style="margin-top: 40px;display:none;"><img src="/assets/images/weightedaverage.jpg" onclick="wightedAverageForTransaction(this);"></img><b>&nbsp;&nbsp;PERIOD</b><select style="width:50px;" name="waperiod" id="waperiod"><option value="1">1 month</option><option value="3">3 month</option><option value="6">6 month</option><option value="12">12 month</option></select>'+data.outstandings+'</div></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.status+'</div><div class="poReferenceDiv"></div>'+
						'<br/><input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit btn-idos" onclick="completeAccounting(this)" style="margin-top:10px;margin-left: 0px;"><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div></td>'+
						'<td><div class="rowToExpand"><select name="txnViewListUpload" id="fileDownload"><option value="">--Please Select--</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div>'+
						'<div class="invDateDiv"><b>VENDOR INVOICE DATE:</b><br/><input type="text" name="vendorInvoiceDate" id="vendorInvoiceDate" class="datepicker"></div></div></td>'+
						'</tr>';
						$("#transactionTable").prepend(tableRecord);
					}else{
						isAlreadyAdded = 1;
						tableRecord = '<tr id="transactionEntity'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
						'<b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.itemName+'</p><br/><b>IMMEDIATE PARENT:</b><br/><font color="blue">'+data.itemParentName+'</font></div></td><td><div class="rowToExpand"><font color="blue">'+data.customerVendorName+'</font><br/><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><div class="vendorInvDateDiv"><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td>';
						tableRecord += '<td><div class="rowToExpand"><p class="number-right">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b>';
						if(TXN_PURPOSE_ID !=BILL_OF_MATERIAL && TXN_PURPOSE_ID != CREATE_PURCHASE_REQUISITION){
							tableRecord += '<br/><p class="number-right">'+data.unitPrice+'</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">'+data.grossAmount+'</p>';
						}
						tableRecord += '</div></td>';
						tableRecord += '<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/><b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;"></div><div class="outstandings" style="margin-top: 40px;display:none;"><img src="/assets/images/weightedaverage.jpg" onclick="wightedAverageForTransaction(this);"></img><b>&nbsp;&nbsp;PERIOD</b><select style="width:50px;" name="waperiod" id="waperiod"><option value="1">1 month</option><option value="3">3 month</option><option value="6">6 month</option><option value="12">12 month</option></select>'+data.outstandings+'</div></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.status+'</div><div class="poReferenceDiv"></div>'+
						'<div class="payementDiv"><br/>Select mode of payment:<select name="paymentDetails" id="paymentDetails" class="txnPaymodeCls" onchange="listAllBranchBankAccounts(this);">'+
						'<option value="1">CASH</option><option value="2">BANK</option></select><br/>'+
						'Input payment details:<textarea name="bankDetails" id="bankDetails" class="txnReceptTextCls"></textarea></div>'+
						'<br/><input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit btn-idos" onclick="completeAccounting(this)" style="margin-top:10px;margin-left: 0px;"><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div></td>'+
						'<td><div class="rowToExpand"><select name="txnViewListUpload" id="fileDownload"><option value="">--Please Select--</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div>'+
						'<div class="invDateDiv"><b>VENDOR INVOICE DATE:</b><br/><input type="text" name="vendorInvoiceDate" id="vendorInvoiceDate" class="datepicker"></div></div></td>'+
						'</tr>';
						console.log(1);
						$("#transactionTable").prepend(tableRecord);
					}
				}else{
					if(data.status=='Require Clarification'){
						isAlreadyAdded = 1;
						tableRecord = '<tr id="transactionEntity'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
						'<b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.itemName+'</p><br/><b>IMMEDIATE PARENT:</b><br/><font color="blue">'+data.itemParentName+'</font></div></td><td><div class="rowToExpand"><font color="blue">'+data.customerVendorName+'</font><br/><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><div class="vendorInvDateDiv"><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td>';
						tableRecord += '<td><div class="rowToExpand"><p class="number-right">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b>';
						if(TXN_PURPOSE_ID !=BILL_OF_MATERIAL && TXN_PURPOSE_ID != CREATE_PURCHASE_REQUISITION){
							tableRecord += '<br/><p class="number-right">'+data.unitPrice+'</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">'+data.grossAmount+'</p>';
						}
						tableRecord += '</div></td>';
						tableRecord += '<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/><b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 100px;overflow: auto;"></div><div class="outstandings" style="display:none;"><img src="/assets/images/weightedaverage.jpg" onclick="wightedAverageForTransaction(this);"></img><b>&nbsp;&nbsp;PERIOD</b><select style="width:50px;" name="waperiod" id="waperiod"><option value="1">1 month</option><option value="3">3 month</option><option value="6">6 month</option><option value="12">12 month</option></select>'+data.outstandings+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.status+'</div><div class="poReferenceDiv"></div>'+
						'<input type="button" value="Clarify Transaction" id="clarifyTxn" class="btn btn-submit btn-idos" onclick="clarifyTransaction(this)" style="margin-top:10px;margin-left: 0px;width:170px;"><br/><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font><div class="invoiceForm"></div></div></div></td>'+
						'<td><div class="rowToExpand"><select name="txnViewListUpload" id="fileDownload"><option value="">--Please Select--</option></select><br>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnRemarks(this)"></div></div></td></tr>';
						$("#transactionTable").prepend(tableRecord);
					}else{
						tableRecord = '<tr id="transactionEntity'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
						'<b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.itemName+'</p><br/><b>IMMEDIATE PARENT:</b><br/><font color="blue">'+data.itemParentName+'</font></div></td><td><div class="rowToExpand"><font color="blue">'+data.customerVendorName+'</font><br/><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><div class="vendorInvDateDiv"><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td>';
						tableRecord += '<td><div class="rowToExpand"><p class="number-right">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b>';
						if(TXN_PURPOSE_ID !=BILL_OF_MATERIAL && TXN_PURPOSE_ID != CREATE_PURCHASE_REQUISITION){
							tableRecord += '<br/><p class="number-right">'+data.unitPrice+'</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">'+data.grossAmount+'</p>';
						}
						tableRecord += '</div></td>';
						tableRecord += '<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/><b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 100px;overflow: auto;"></div><div class="outstandings" style="display:none;"><img src="/assets/images/weightedaverage.jpg" onclick="wightedAverageForTransaction(this);"></img><b>&nbsp;&nbsp;PERIOD</b><select style="width:50px;" name="waperiod" id="waperiod"><option value="1">1 month</option><option value="3">3 month</option><option value="6">6 month</option><option value="12">12 month</option></select>'+data.outstandings+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.status+'</div><div class="poReferenceDiv"></div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div><div class="invoiceForm"></div></div></td>'+
						'<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">--Please Select--</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnRemarks(this)"></div></div></td></tr>';
						console.log(2);
						$("#transactionTable").prepend(tableRecord);
					}
				}
				if(data.transactionPurpose=="Receive payment from customer" || data.transactionPurpose=="Receive advance from customer"){
					$("#transactionEntity"+data.id+" div[class='invoiceForm']").append('<form action="/exportReceiptPdf" class="'+data.transactionPurpose+'" name="receiptForm'+data.id+'" id="receiptForm'+data.id+'">'+
					'<input type="button" value="Generate Voucher" id="generateReceipt" class="btn btn-submit btn-idos" onclick="generatePDFReceipt(this,'+data.transactionPurposeID+');"></form>');
				}

				if(TXN_PURPOSE_ID == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER || TXN_PURPOSE_ID == BUY_ON_CREDIT_PAY_LATER || TXN_PURPOSE_ID == PAY_SPECIAL_ADJUSTMENTS_AMOUNT_TO_VENDORS || TXN_PURPOSE_ID == WITHDRAW_CASH_FROM_BANK || TXN_PURPOSE_ID == DEPOSIT_CASH_IN_BANK || TXN_PURPOSE_ID == TRANSFER_FUNDS_FROM_ONE_BANK_TO_ANOTHER || TXN_PURPOSE_ID==TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER || TXN_PURPOSE_ID == INVENTORY_OPENING_BALANCE || TXN_PURPOSE_ID==SALES_RETURNS || TXN_PURPOSE_ID == PURCHASE_RETURNS || TXN_PURPOSE_ID == CANCEL_INVOICE || TXN_PURPOSE_ID == REVERSAL_OF_ITC){
					$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] div[class="payementDiv"]').hide();
				}else{
					$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] div[class="payementDiv"]').show();
					if(data.transactionPurpose=="Buy on Petty Cash Account" || data.transactionPurpose=="Transfer main cash to petty cash"){
						$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] div[class="payementDiv"] select[name="paymentDetails"] option[value="2"]').remove();
						$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] div[class="payementDiv"] select[name="paymentDetails"] option[value="1"]').prop("selected","selected");
					}
				}
				if(data.transactionPurpose=="Buy on cash & pay right away" || data.transactionPurpose=="Buy on credit & pay later" || data.transactionPurpose=="Pay special adjustments amount to vendors" || data.transactionPurpose=="Buy on Petty Cash Account"){
					if(data.status=='Approved'){
						$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] div[class="invDateDiv"]').show();
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
						$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] div[class="invDateDiv"]').hide();
					}
				}else{
					$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] div[class="invDateDiv"]').hide();
				}
		    }

		    //for approver can be same user or not
		    if(data.approverEmails!=null){
				if(data.approverEmails.indexOf(useremail)!=-1){
					if(data.status=='Require Approval' || data.status=='Clarified'){
						//check for user mail existence in the approver usermail list sent by server
						//based on transaction status row data is displayed
						$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"]').remove();
						tableRecord = '<tr id="transactionEntity'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
						'<b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.itemName+'</p><br/><b>IMMEDIATE PARENT:</b><br/><font color="blue">'+data.itemParentName+'</font><br/><b>BUDGET:</b><br/><b>'+data.budgetAllocated+'</b><br/><font color="blue">'+data.budgetAllocatedAmt+'</font><br/><b>'+data.budgetAvailable+'</b><br/><font color="blue">'+data.budgetAvailableAmt+'</font></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.customerVendorName+'</font><br/><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><div class="vendorInvDateDiv"><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td>';
						tableRecord += '<td><div class="rowToExpand"><p class="number-right">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b>';
						if(TXN_PURPOSE_ID !=BILL_OF_MATERIAL && TXN_PURPOSE_ID != CREATE_PURCHASE_REQUISITION){
							tableRecord += '<br/><p class="number-right">'+data.unitPrice+'</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">'+data.grossAmount+'</p>';
						}
						tableRecord += '</div></td>';
						tableRecord += '<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/><b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 125px;overflow: auto;"></div><div class="outstandings" style="margin-top: 50px;display:none;"><img src="/assets/images/weightedaverage.jpg" onclick="wightedAverageForTransaction(this);"></img><b>&nbsp;&nbsp;PERIOD</b><select style="width:50px;" name="waperiod" id="waperiod"><option value="1">1 month</option><option value="3">3 month</option><option value="6">6 month</option><option value="12">12 month</option></select></div></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.status+'</div><div class="poReferenceDiv"></div>'+
						'<br/>Action:<select class="approverActionList" name="approverActionList" id="approverActionList" onchange="pettyCashTransaction(this);"><option value="">--Please Select--</option>'+
						'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>'+
						'<span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
						'<input type="button" id="approverAction" class="btn btn-submit btn-center" value="Submit" onclick="completeAction(this)"><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div></td>'+
						'<td><div class="rowToExpand"><select name="txnViewListUpload" id="fileDownload"><option value="">--Please Select--</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>';
						console.log(3);
						$("#transactionTable").prepend(tableRecord);
					}else if(isAlreadyAdded == 0 && data.selectedAdditionalApproval!=useremail){
						// Sunil: added above check to show that "Approved" append can be shown to user when he is creator + approver.
						$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"]').remove();
						tableRecord = '<tr id="transactionEntity'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
						'<b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.itemName+'</p><br/><b>IMMEDIATE PARENT:</b><br/><font color="blue">'+data.itemParentName+'</font></div></td><td><div class="rowToExpand"><font color="blue">'+data.customerVendorName+'</font><br/><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><div class="vendorInvDateDiv"><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td>';
						tableRecord += '<td><div class="rowToExpand"><p class="number-right">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b>';
						if(TXN_PURPOSE_ID !=BILL_OF_MATERIAL && TXN_PURPOSE_ID != CREATE_PURCHASE_REQUISITION){
							tableRecord += '<br/><p class="number-right">'+data.unitPrice+'</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">'+data.grossAmount+'</p>';
						}
						tableRecord += '</div></td>';
						tableRecord += '<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/><b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 100px;overflow: auto;"></div><div class="outstandings" style="display:none;"><img src="/assets/images/weightedaverage.jpg" onclick="wightedAverageForTransaction(this);"></img><b>&nbsp;&nbsp;PERIOD</b><select style="width:50px;" name="waperiod" id="waperiod"><option value="1">1 month</option><option value="3">3 month</option><option value="6">6 month</option><option value="12">12 month</option></select></div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.status+'</div><div class="poReferenceDiv"></div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div><div class="invoiceForm"></div></div></td>'+
						'<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload" ><option value="">--Please Select--</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnRemarks(this)"></div></div></td></tr>';
						console.log(4);
						$("#transactionTable").prepend(tableRecord);
					}
				}
		    }

		    if(data.selectedAdditionalApproval!=null && data.selectedAdditionalApproval!=""){
				if(data.selectedAdditionalApproval==useremail){
					if(data.status=='Require Additional Approval'){
						$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"]').remove();
						tableRecord = '<tr id="transactionEntity'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
						'<b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.itemName+'</p><br/><b>IMMEDIATE PARENT:</b><br/><font color="blue">'+data.itemParentName+'</font><br/><b>BUDGET:</b><br/><b>'+data.budgetAllocated+'</b><br/><font color="blue">'+data.budgetAllocatedAmt+'</font><br/><b>'+data.budgetAvailable+'</b><br/><font color="blue">'+data.budgetAvailableAmt+'</font></div></td><td><div class="rowToExpand"><font color="blue">'+data.customerVendorName+'</font><br/><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><div class="vendorInvDateDiv"><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td>';
						tableRecord += '<td><div class="rowToExpand"><p class="number-right">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b>';
						if(TXN_PURPOSE_ID !=BILL_OF_MATERIAL && TXN_PURPOSE_ID != CREATE_PURCHASE_REQUISITION){
							tableRecord += '<br/><p class="number-right">'+data.unitPrice+'</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">'+data.grossAmount+'</p>';
						}
						tableRecord += '</div></td>';
						tableRecord += '<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/><b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 125px;overflow: auto;"></div><div class="outstandings" style="margin-top: 50px;display:none;"><b>&nbsp;&nbsp;PERIOD</b><select style="width:50px;" name="waperiod" id="waperiod"><option value="1">1 month</option><option value="3">3 month</option><option value="6">6 month</option><option value="12">12 month</option></select><img src="/assets/images/weightedaverage.jpg" onclick="wightedAverageForTransaction(this);"></img></div></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.status+'</div>'+
						'<div class="poReferenceDiv"></div><br/>Action:<select class="approverActionList" name="approverActionList" id="approverActionList"><option value="">--Please Select--</option>'+
						'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>'+
						'<span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
						'<input type="button" id="approverAction" class="approverAction btn btn-submit" value="Submit" onclick="completeAction(this)"><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div></td>'+
						'<td><div class="rowToExpand"><select name="txnViewListUpload" id="fileDownload"><option value="">--Please Select--</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>';
						console.log(5);
						$("#transactionTable").prepend(tableRecord);
					}else{
						tableRecord = '<tr id="transactionEntity'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
						'<b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.itemName+'</p><br/><b>IMMEDIATE PARENT:</b><br/><font color="blue">'+data.itemParentName+'</font></div></td><td><div class="rowToExpand"><font color="blue">'+data.customerVendorName+'</font><br/><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><div class="vendorInvDateDiv"><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td>'
						tableRecord += '<td><div class="rowToExpand"><p class="number-right">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b>';
						if(TXN_PURPOSE_ID !=BILL_OF_MATERIAL && TXN_PURPOSE_ID != CREATE_PURCHASE_REQUISITION){
							tableRecord += '<br/><p class="number-right">'+data.unitPrice+'</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">'+data.grossAmount+'</p>';
						}
						tableRecord += '</div></td>';
						tableRecord += '<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/><b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 100px;overflow: auto;"></div><div class="outstandings" style="display:none;"><img src="/assets/images/weightedaverage.jpg" onclick="wightedAverageForTransaction(this);"></img><b>&nbsp;&nbsp;PERIOD</b><select style="width:50px;" name="waperiod" id="waperiod"><option value="1">1 month</option><option value="3">3 month</option><option value="6">6 month</option><option value="12">12 month</option></select></div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.status+'</div><div class="poReferenceDiv"></div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div></div></td>'+
						'<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload" ><option value="">--Please Select--</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnRemarks(this)"></div></div></td></tr>';
						console.log(6);
						$("#transactionTable").prepend(tableRecord);
					}
				}else{
					if(data.status=='Require Additional Approval'){
						$("#transactionTable tr[id='transactionEntity"+data.id+"'] td:nth-child(8) div:first").append('<br/><b>Additional Approval Required By:</b><br/>'+data.selectedAdditionalApproval+'');
					}
			    }
		    }else{
				if(data.role.includes("ACCOUNTANT") || data.role.includes("AUDITOR") || data.role.includes("CONTROLLER")){
					var approverEmailVal=false;var selectedAdditionalApproval=false;
					if(data.approverEmails!=null && data.approverEmails!=""){
						 if(data.approverEmails.indexOf(useremail)!=-1){
							 approverEmailVal=true;
						 }
					}
					if(data.selectedAdditionalApproval!=null && data.selectedAdditionalApproval!=""){
						if(data.selectedAdditionalApproval==useremail){
							selectedAdditionalApproval=true;
						}
					}
					if(data.createdBy!=useremail && approverEmailVal==false && selectedAdditionalApproval==false){
						tableRecord = '<tr id="transactionEntity'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
						 '<b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.itemName+'</p><br/><b>IMMEDIATE PARENT:</b><br/><font color="blue">'+data.itemParentName+'</font></div></td><td><div class="rowToExpand"><font color="blue">'+data.customerVendorName+'</font><br/><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
						 '<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><div class="vendorInvDateDiv"><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td>';
						 tableRecord += '<td><div class="rowToExpand"><p class="number-right">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b>';
						if(TXN_PURPOSE_ID !=BILL_OF_MATERIAL && TXN_PURPOSE_ID != CREATE_PURCHASE_REQUISITION){
							tableRecord += '<br/><p class="number-right">'+data.unitPrice+'</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">'+data.grossAmount+'</p>';
						}
						tableRecord += '</div></td>';
						tableRecord += '<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/><b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 100px;overflow: auto;"></div><div class="outstandings" style="display:none;"><img src="/assets/images/weightedaverage.jpg" onclick="wightedAverageForTransaction(this);"></img><b>&nbsp;&nbsp;PERIOD</b><select style="width:50px;" name="waperiod" id="waperiod"><option value="1">1 month</option><option value="3">3 month</option><option value="6">6 month</option><option value="12">12 month</option></select>'+data.outstandings+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.status+'</div><div class="poReferenceDiv"></div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div><div class="invoiceForm"></div></div></td>'+
						 '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload" ><option value="">--Please Select--</option></select>'+
						 '<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						 '<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
	                     '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						 '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnRemarks(this)"></div></div></td></tr>';
						console.log(7);
						$("#transactionTable").prepend(tableRecord);
						if(data.transactionPurpose=="Receive payment from customer" || data.transactionPurpose=="Receive advance from customer"){
							$("#transactionEntity"+data.id+" div[class='invoiceForm']").append('<form action="/exportReceiptPdf" class="'+data.transactionPurpose+'" name="receiptForm'+data.id+'" id="receiptForm'+data.id+'">'+
							'<input type="button" value="Generate Voucher" id="generateReceipt" class="btn btn-submit btn-idos" onclick="generatePDFReceipt(this,'+data.transactionPurposeID+');"></form>');
						}
					}
				}
		    }
			if(data.additionalApprovarUsers!=null){
			    if(typeof data.additionalApprovarUsers!='undefined'){
					var additionalApprovarUsersList=data.additionalApprovarUsers.substring(0,data.additionalApprovarUsers.length).split(',');
					$("tr[id='transactionEntity"+data.id+"'] select[id='userAddApproval']").append('<option value="">--Please Select--</option>');
					for(var i=0;i<additionalApprovarUsersList.length;i++){
						$("tr[id='transactionEntity"+data.id+"'] select[id='userAddApproval']").append('<option value="'+additionalApprovarUsersList[i]+'">'+additionalApprovarUsersList[i]+'</option>');
					}
			    }
			}
			if(data.netAmtDesc!=null && data.netAmtDesc!=""){
			   	var individualNetDesc=data.netAmtDesc.substring(0,data.netAmtDesc.length).split(',');
			   	for(var m=0;m<individualNetDesc.length;m++){
					var labelAndFigure=individualNetDesc[m].substring(0, individualNetDesc[m].length).split(':');
					$('#transactionEntity'+data.id+' div[class="netResultCalcDesc"]').append('<font color="#102E55"><b>'+labelAndFigure[0]+'</b></font><br/>');
					if(typeof labelAndFigure[1]!='undefined'){
						$('#transactionEntity'+data.id+' div[class="netResultCalcDesc"]').append('<font color="blue">'+labelAndFigure[1]+'</font><br/>');
					}
			    }
			}
			if(data.txnRemarks!=null && data.txnRemarks!=""){
			   	var individualRemarks=data.txnRemarks.substring(0,data.txnRemarks.length).split('|');
			   	for(var m=0;m<individualRemarks.length;m++){
					var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
					$('#transactionEntity'+data.id+' div[class="txnWorkflowRemarks"]').append('<font color="102E55"><b>'+emailAndRemarks[0]+'</b></font>#');
					if(typeof emailAndRemarks[1]!='undefined'){
						if(emailAndRemarks[0].indexOf("(Auditor)")!=-1){ //Stored in db as:Manali1atMyidos.Com(Auditor)#Auditor Remark
							$('#transactionEntity'+data.id+' div[class="txnWorkflowRemarks"]').append('<h1auditor>'+emailAndRemarks[1]+'</h1auditor><br/>');
						}
						else {
							$('#transactionEntity'+data.id+' div[class="txnWorkflowRemarks"]').append('<font color="blue">'+emailAndRemarks[1]+'</font><br/>');
						}
					}
			    }
			}
			//Sunil: When user is creator + approver then already we have added the button above.
			if(data.createdBy==data.approverEmail){
				if(data.status=='Approved' && isAlreadyAdded == 0){
					$("tr[id='transactionEntity"+data.id+"'] td:nth-child(9)").append('<input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit btn-idos" onclick="completeAccounting(this)" style="margin-top:10px;margin-left: 0px;">');
				}
			}
		    if(data.txnDocument!="" && data.txnDocument!=null){
				var txndocument=data.txnDocument;
				var transTrID = 'transactionEntity'+data.id;
				fillSelectElementWithUploadedDocs(txndocument, transTrID, 'txnViewListUpload');
			}
		    //add "edit transaction" button in status column if below transaction type for creator and if transaction is approved/require approval
			if(data.transactionPurpose=="Buy on cash & pay right away" || data.transactionPurpose=="Buy on credit & pay later"
				|| data.transactionPurpose=="Pay vendor/supplier" || data.transactionPurpose=="Pay advance to vendor or supplier"
				|| parseInt(data.transactionPurposeID)==PREPARE_QUOTATION || parseInt(data.transactionPurposeID)==PROFORMA_INVOICE
				|| parseInt(data.transactionPurposeID)==SELL_ON_CASH_COLLECT_PAYMENT_NOW || data.transactionPurposeID==SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
				if(data.status=='Require Approval' && data.createdBy==useremail){
					// if(data.txnRemarks.indexOf("edited")==-1){
					// 	$("#transactionTable tr[id='transactionEntity"+data.id+"'] td:nth-child(8) div:first").append('<br/><input type="button" value="Edit Transaction" id="editTxnOnceAllowed" class="btn btn-submit btn-idos" onclick="editTransactionOnceAllowed(this)" style="margin-top:10px;margin-left: 0px;">');
					// }
				}
			}
			    if(data.txnSpecialStatus=="Transaction Exceeding Budget & Rules Not Followed"){
					$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] b[class="txnSpecialStatus"]').html("");
					$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] td:nth-child(2)').addClass("txnBugtExceedAndRulesNotFollow");
			    } else if(data.txnSpecialStatus=="Transaction Exceeding Budget"){
					$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] b[class="txnSpecialStatus"]').html("");
					$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] td:nth-child(2)').addClass("txnBugtExceed");
			    } else if(data.txnSpecialStatus=="Rules Not Followed"){
					$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] b[class="txnSpecialStatus"]').html("");
					$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] td:nth-child(2)').addClass("txnRulesNotFollow");
		    	}
		    if(data.status!="" && data.status=="Rejected"){
				$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstatred');
				$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstatred');
				$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstatred');
		    } else if(data.status!="" && data.status=="Accounted"){
				$("#transactionTable").find('tr[id="transactionEntity'+ data.id+'"] div[class="txnstat"]').attr('class','txnstat');
				$("#transactionTable").find('tr[id="transactionEntity'+ data.id+'"] div[class="txnstatgreen"]').attr('class','txnstat');
				$("#transactionTable").find('tr[id="transactionEntity'+ data.id+'"] div[class="txnstatred"]').attr('class','txnstat');
		    } else if(data.status!="" && (data.status=="Approved" || data.status=="Require Approval" || data.status=="Require Additional Approval")){
				$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstatgreen');
				$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstatgreen');
				$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstatgreen');
		    }

			if(data.transactionPurpose=="Sell on cash & collect payment now" || data.transactionPurpose=="Sell on credit & collect payment later"){
				$("#transactionEntity"+data.id+" div[class='poReferenceDiv']").append('<b>PO REFERENCE:</b><p style="color: blue;">'+data.poReference+'</p>');
			}


		    if(data.role.includes("APPROVER") || data.role.includes("ACCOUNTANT") || data.role.includes("AUDITOR")){
				if(data.transactionPurpose=="Sell on cash & collect payment now" ||  data.transactionPurpose=="Sell on credit & collect payment later" || data.transactionPurpose=="Buy on cash & pay right away" || data.transactionPurpose=="Buy on credit & pay later" || data.transactionPurpose=="Buy on Petty Cash Account"){
					$("tr[id='transactionEntity"+data.id+"'] div[class='outstandings']").show();
				}
		    }

			if(parseInt(data.transactionPurposeID) == PREPARE_QUOTATION || parseInt(data.transactionPurposeID) == PROFORMA_INVOICE || parseInt(data.transactionPurposeID) == PURCHASE_ORDER || parseInt(data.transactionPurposeID) == BILL_OF_MATERIAL || parseInt(data.transactionPurposeID) == CREATE_PURCHASE_ORDER || TXN_PURPOSE_ID == CREATE_PURCHASE_REQUISITION){
				$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] div[class="payementDiv"]').hide();
				$("#transactionEntity"+data.id+" p[class='txnItemDesc']").html("");
				if (parseInt(data.transactionPurposeID) == CREATE_PURCHASE_ORDER) {
					$("#transactionEntity" + data.id + " p[class='txnItemDesc']").append(
						'<a id="multiSellItemsList" onclick="listMultiPOItems(this);">View PO</a>'
					);
				} else if (parseInt(data.transactionPurposeID) == CREATE_PURCHASE_REQUISITION) {
					$("#transactionEntity" + data.id + " p[class='txnItemDesc']").append(
						'<a id="multiSellItemsList" onclick="listMultiPOItems(this);">View Details</a>'
					);
				} else {
					$("#transactionEntity" + data.id + " p[class='txnItemDesc']").append(
						'<a id="multiSellItemsList" onclick="listMultiSellItems(this);">View Details</a>'
					);
				}
				if(data.status=="Accounted"){
					if(parseInt(data.transactionPurposeID) == CREATE_PURCHASE_ORDER){
						$("#transactionEntity"+data.id+" div[class='invoiceForm']").append('<b class="btn btn-submit btn-center">Download Invoice:</b><br><button id="XLSX'+data.id+'" class="exportInvoiceXlsx btn btn-submit exportInvoiceBtn" title="Export Invoice(xlsx)" onclick="createPODocDownload(this);"><i class="fa fa-file-excel-o" aria-hidden="true"></i></button><button id="PDF1'+data.id+'" class="exportInvoicePdf btn btn-submit exportInvoiceBtn" title="Export Invoice(pdf)" onclick="createPODocDownload(this);"><i class="fa fa-file-pdf-o" aria-hidden="true"></i></button>');
					}
					else{
						$("#transactionEntity"+data.id+" div[class='invoiceForm']").append('<b class="btn btn-submit btn-center">Download Invoice:</b><br><button id="XLSX'+data.id+'" class="exportInvoiceXlsx btn btn-submit exportInvoiceBtn" title="Export Invoice(xlsx)" onclick="generatePDFQuotProf(this);"><i class="fa fa-file-excel-o" aria-hidden="true"></i></button><button id="PDF1'+data.id+'" class="exportInvoicePdf btn btn-submit exportInvoiceBtn" title="Export Invoice(pdf)" onclick="generatePDFQuotProf(this);"><i class="fa fa-file-pdf-o" aria-hidden="true"></i></button>');
					}
					if(parseInt(data.transactionPurposeID) == PREPARE_QUOTATION) {
                        $("#transactionEntity" + data.id + " div[class='vendorInvDateDiv']").append('<b>Quotaion No:</b><p style="color: blue;">' + data.invoiceNumber + '</p>');
                    }else{
                        $("#transactionEntity" + data.id + " div[class='vendorInvDateDiv']").append('<b>Proforma No:</b><p style="color: blue;">' + data.invoiceNumber + '</p>');
					}
				}
				if(data.remarksPrivate!=null && data.remarksPrivate!=""){
					var individualRemarks=data.remarksPrivate.substring(0,data.remarksPrivate.length).split('|');
					$('#transactionEntity'+data.id+' div[class="txnWorkflowRemarks"]').after('<div class="txnRemarksPrivate"></div>');
					for(var m=0;m<individualRemarks.length;m++){
						var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
						$('#transactionEntity'+data.id+' div[class="txnRemarksPrivate"]').append('<font color="102E55"><b>'+emailAndRemarks[0]+'</b></font>#');
						if(typeof emailAndRemarks[1]!='undefined'){
							if(emailAndRemarks[0].indexOf("(Auditor)")!=-1){ //Stored in db as:Manali1atMyidos.Com(Auditor)#Auditor Remark
								$('#transactionEntity'+data.id+' div[class="txnRemarksPrivate"]').append('<h1auditor>'+emailAndRemarks[1]+'</h1auditor><br/>');
							}else {
								$('#transactionEntity'+data.id+' div[class="txnRemarksPrivate"]').append('<font color="blue">'+emailAndRemarks[1]+'</font><br/>');
							}
						}
					}
				}
			}else if(parseInt(data.transactionPurposeID) == SELL_ON_CASH_COLLECT_PAYMENT_NOW || parseInt(data.transactionPurposeID) == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER || parseInt(data.transactionPurposeID) == RECEIVE_ADVANCE_FROM_CUSTOMER || parseInt(data.transactionPurposeID) ==SALES_RETURNS || parseInt(data.transactionPurposeID) == CREDIT_NOTE_CUSTOMER || parseInt(data.transactionPurposeID) == DEBIT_NOTE_CUSTOMER || parseInt(data.transactionPurposeID) == CREDIT_NOTE_VENDOR || parseInt(data.transactionPurposeID) == DEBIT_NOTE_VENDOR || parseInt(data.transactionPurposeID) == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER || parseInt(data.transactionPurposeID) == CANCEL_INVOICE || parseInt(data.transactionPurposeID) == REVERSAL_OF_ITC){
				if(parseInt(data.transactionPurposeID) == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER || parseInt(data.transactionPurposeID) == CREDIT_NOTE_CUSTOMER || parseInt(data.transactionPurposeID) == DEBIT_NOTE_CUSTOMER || parseInt(data.transactionPurposeID) == CREDIT_NOTE_VENDOR || parseInt(data.transactionPurposeID) == DEBIT_NOTE_VENDOR || parseInt(data.transactionPurposeID) == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER || parseInt(data.transactionPurposeID) == CANCEL_INVOICE){
					$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] div[class="payementDiv"]').hide();
				}else{
					$("#transactionTable").find('tr[id="transactionEntity'+data.id+'"] div[class="payementDiv"]').show();
				}
				$("#transactionEntity"+data.id+" p[class='txnItemDesc']").html("");
				$("#transactionEntity"+data.id+" p[class='txnItemDesc']").append('<a id="multiSellItemsList" onclick="listMultiSellItems(this);">Items List</a>');
				if(data.status=="Accounted" && parseInt(data.transactionPurposeID) != RECEIVE_ADVANCE_FROM_CUSTOMER && parseInt(data.transactionPurposeID) != DEBIT_NOTE_CUSTOMER && parseInt(data.transactionPurposeID) != CREDIT_NOTE_CUSTOMER && parseInt(data.transactionPurposeID) != CREDIT_NOTE_VENDOR && parseInt(data.transactionPurposeID) != DEBIT_NOTE_VENDOR && parseInt(data.transactionPurposeID) != CANCEL_INVOICE && parseInt(data.transactionPurposeID) != REVERSAL_OF_ITC){
					if(parseInt(data.txnItentifier) != 2){
						$("#transactionEntity"+data.id+" div[class='invoiceForm']").append('<b class="btn btn-submit btn-center">Download Invoice:</b><br><button id="XLSX'+data.id+'" class="exportInvoiceXlsx btn btn-submit exportInvoiceBtn" title="Export Invoice(xlsx)" onclick="generatePDFInvoice(this);"><i class="fa fa-file-excel-o" aria-hidden="true"></i></button><button id="PDF1'+data.id+'" class="exportInvoiceBtn exportInvoicePdf btn btn-submit" title="Export Invoice(pdf)" onclick="generatePDFInvoice(this);"><i class="fa fa-file-pdf-o" aria-hidden="true"></i></button>');
					}
					else {
						$("#transactionEntity"+data.id+" div[class='invoiceForm']").append('<b class="btn btn-submit btn-center">Download Invoice:</b><br><button id="XLSX'+data.id+'" class="exportInvoiceXlsx btn btn-submit exportInvoiceBtn" title="Export Invoice(xlsx)" onclick="grnReportDownload(this);"><i class="fa fa-file-excel-o" aria-hidden="true"></i></button><button id="PDF1'+data.id+'" class="exportInvoiceBtn exportInvoicePdf btn btn-submit" title="Export Invoice(pdf)" onclick="grnReportDownload(this);"><i class="fa fa-file-pdf-o" aria-hidden="true"></i></button>');
					}
					$("#transactionEntity"+data.id+" div[class='vendorInvDateDiv']").append('<b>INVOICE No:</b><p style="color: blue;">'+data.invoiceNumber+'</p>');
				}else if(data.status=="Accounted" && (parseInt(data.transactionPurposeID) == CREDIT_NOTE_CUSTOMER || parseInt(data.transactionPurposeID) == DEBIT_NOTE_CUSTOMER || parseInt(data.transactionPurposeID) == CREDIT_NOTE_VENDOR || parseInt(data.transactionPurposeID) == DEBIT_NOTE_VENDOR)){
					if(parseInt(data.transactionPurposeID) == CREDIT_NOTE_CUSTOMER || parseInt(data.transactionPurposeID) == DEBIT_NOTE_CUSTOMER){
						$("#transactionEntity"+data.id+" div[class='invoiceForm']").append('<b class="btn btn-submit btn-center">Download Invoice:</b><br><button id="XLSX'+data.id+'" class="exportInvoiceXlsx btn btn-submit exportInvoiceBtn" title="Export Invoice(xlsx)" onclick="generateCustCdtDbtNote(this);"><i class="fa fa-file-excel-o" aria-hidden="true"></i></button><button id="PDF1'+data.id+'" class="exportInvoiceBtn exportInvoicePdf btn btn-submit" title="Export Invoice(pdf)" onclick="generateCustCdtDbtNote(this);"><i class="fa fa-file-pdf-o" aria-hidden="true"></i></button>');
					}
					$("#transactionEntity"+data.id+" div[class='vendorInvDateDiv']").append('<b>Note No:</b><p style="color: blue;">'+data.invoiceNumber+'</p>');
				}
			}else if(data.transactionPurpose=="Buy on cash & pay right away" || data.transactionPurpose=="Buy on credit & pay later" || data.transactionPurpose=="Buy on Petty Cash Account"
					|| data.transactionPurpose=="Purchase returns" ){
				$("#transactionEntity"+data.id+" p[class='txnItemDesc']").html("");
				$("#transactionEntity"+data.id+" p[class='txnItemDesc']").append('<a id="multiSellItemsList" onclick="listMultiSellItems(this);">Items List</a>');
				if(data.status=="Accounted"){
				  if(parseInt(data.transactionPurposeID) == PURCHASE_RETURNS) {
				  		$("#transactionEntity"+data.id+" div[class='invoiceForm']").append('<b class="btn btn-submit btn-center">Download Invoice:</b><br><button id="XLSX'+data.id+'" class="exportInvoiceXlsx btn btn-submit exportInvoiceBtn" title="Export Invoice(xlsx)" onclick="generatePDFBuySelfInvoice(this);"><i class="fa fa-file-excel-o" aria-hidden="true"></i></button><button id="PDF1'+data.id+'" class="exportInvoicePdf btn btn-submit exportInvoiceBtn" title="Export Invoice(pdf)" onclick="generatePDFInvoice(this);"><i class="fa fa-file-pdf-o" aria-hidden="true"></i></button>');

				  }
				  if(data.transactionPurpose=="Buy on cash & pay right away" || data.transactionPurpose=="Buy on credit & pay later"){

					 if(data.typeOfSupply=="2" || data.typeOfSupply=="3"){
						   $("#transactionEntity"+data.id+" div[class='invoiceForm']").append('<button id="'+data.id+'" class="exportInvoiceXlsx btn btn-submit exportInvoiceBtn" title="Export GRN/Inovice" onclick="popupBuyTxnInvoiceModal(this);" ><i class="fa fa-file-excel-o" aria-hidden="true"></i>Save Invoice/GRN</button>');
					  }else{
				  $("#transactionEntity"+data.id+" div[class='invoiceForm']").append('<b class="btn btn-submit btn-center">Download Invoice:</b><br><button id="XLSX'+data.id+'" class="exportInvoiceXlsx btn btn-submit exportInvoiceBtn" title="Export Invoice(xlsx)" onclick="grnReportDownload(this);"><i class="fa fa-file-excel-o" aria-hidden="true"></i></button><button id="PDF1'+data.id+'" class="exportInvoiceBtn exportInvoicePdf btn btn-submit" title="Export Invoice(pdf)" onclick="grnReportDownload(this);"><i class="fa fa-file-pdf-o" aria-hidden="true"></i></button>');
				  }
					$("#transactionEntity"+data.id+" div[class='vendorInvDateDiv']").append('<b>INVOICE No:</b><p style="color: blue;">'+data.invoiceNumber+'</p>');

				  }
				}
			}else if((data.transactionPurpose=="Refund Advance Received" || data.transactionPurpose=="Refund Amount Received Against Invoice" ||data.transactionPurpose== "Pay vendor/supplier" ||data.transactionPurpose== "Pay advance to vendor or supplier") && data.status=="Accounted"){
				$("#transactionEntity"+data.id+" div[class='invoiceForm']").append('<form action="/exportReceiptPdf" class="'+data.transactionPurpose+'" name="receiptForm'+data.id+'" id="receiptForm'+data.id+'">'+
				'<input type="button" value="Generate Voucher" id="generateReceipt" class="btn btn-submit btn-idos" onclick="generatePDFReceipt(this,'+data.transactionPurposeID+');"></form>');
				$("#transactionEntity"+data.id+" div[class='vendorInvDateDiv']").append('<b>DOCUMENT SERIAL NO:</b><p style="color: blue;">'+data.invoiceNumber+'</p>');
				if(data.remarksPrivate!=null && data.remarksPrivate!=""){
					var individualRemarks=data.remarksPrivate.substring(0,data.remarksPrivate.length).split('|');
					$('#transactionEntity'+data.id+' div[class="txnWorkflowRemarks"]').after('<div class="txnRemarksPrivate"></div>');
					for(var m=0;m<individualRemarks.length;m++){
						var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
						$('#transactionEntity'+data.id+' div[class="txnRemarksPrivate"]').append('<font color="102E55"><b>'+emailAndRemarks[0]+'</b></font>#');
						if(typeof emailAndRemarks[1]!='undefined'){
							if(emailAndRemarks[0].indexOf("(Auditor)")!=-1){ //Stored in db as:Manali1atMyidos.Com(Auditor)#Auditor Remark
								$('#transactionEntity'+data.id+' div[class="txnRemarksPrivate"]').append('<h1auditor>'+emailAndRemarks[1]+'</h1auditor><br/>');
							}else {
								$('#transactionEntity'+data.id+' div[class="txnRemarksPrivate"]').append('<font color="blue">'+emailAndRemarks[1]+'</font><br/>');
							}
						}
					}
				}
			}
			
			if(COMPANY_OWNER == COMPANY_PWC){
                $("#transactionEntity"+data.id + " .approverActionList").children().remove();
                $("#transactionEntity"+data.id + " .approverActionList").append('<option value="">Select an action</option><option value="4">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option>');
            }
			
		    if((data.role.includes("CREATOR") || data.role.includes("APPROVER") || data.role.includes("ACCOUNTANT") || data.role.includes("AUDITOR")) && (!data.role.includes("MASTER ADMIN") && !data.role.includes("CONTROLLER"))){
				var locHash=window.location.hash;
				if(locHash=="" || locHash=="#pendingExpense"){
					location.hash="#pendingExpense";
				}if(locHash=="#labourHiring"){
					location.hash="#labourHiring";
				}
		    }
			if(data.role.includes("ACCOUNTANT") || data.role.includes("MASTER ADMIN") || data.role.includes("CONTROLLER")){
				getDashboardFinancials();
			}
			getCashBankReceivablePayable();
		} else if(data.txnType=="expenseAssetsLiabilitiesProvisionTxn"){
			var useremail=data.useremail;
			var isPjeApproved = 0;
			$("#transactionTable").find('tr[id="transactionProvisionEntity'+data.id+'"]').remove();
			if(data.createdBy==useremail){
				if(data.status=='Approved'){
					isPjeApproved = 1;
					$("#transactionTable").prepend('<tr id="transactionProvisionEntity'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
		    		'<br/><b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
		    		'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><br/><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td><td><div class="rowToExpand"><b>GROSS:</b><br/><p class="number-right">'+data.grossAmount+'</p></div></td>'+
		    		'<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></div></td>'+
					'<td><div class="rowToExpand"><div class="txnstat">'+data.status+'</div><input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit" onclick="completeProvisionAccounting(this)" style="margin-top:10px;margin-left: 0px;"/>1<b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div></td>'+
		    		'<td><div class="rowToExpand"><select name="txnViewListUpload" id="fileDownload" ><option value="">--Please Select--</option></select>'+
					'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
					'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                    '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
		    		'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td>'+
		    		'</tr>');
					console.log("pje1 = " + data.txnRefNo);
		    	}else{
		    		if(data.status=='Require Clarification'){
			    		$("#transactionTable").prepend('<tr id="transactionProvisionEntity'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
			    		// '<b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
			    		'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><br/><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td><td><div class="rowToExpand"><b>GROSS:</b><br/><p class="number-right">'+data.grossAmount+'</p></div></td>'+
			    		'<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.status+'</div>'+
			    		'<br/><input type="button" value="Clarify Transaction" id="clarifyTxn" class="btn btn-submit btn-idos" onclick="clarifyProvisionTransaction(this)" style="margin-top:10px;margin-left: 0px;width:170px;"><br/><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font><div class="invoiceForm"></div></div></div></td>'+
			    		'<td><div class="rowToExpand"><select name="txnViewListUpload" id="fileDownload" ><option value="">--Please Select--</option></select><br>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
			    		'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>');
						console.log("pje2 = " + data.txnRefNo);
			    	}else{
						$("#transactionTable").prepend('<tr id="transactionProvisionEntity'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
				    	'<b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
				    	'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><br/><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td><td><div class="rowToExpand"><b>GROSS:</b><br/><p class="number-right">'+data.grossAmount+'</p></div></td>'+
				    	'<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.status+'</div><br/><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div><div class="invoiceForm"></div></div></td>'+
				    	'<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">--Please Select--</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
				    	'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>');
						console.log("pje3 = " + data.txnRefNo);
			    	}
		    	}
			}
			//for approver can be same user or not
		    if(data.approverEmails!=null && isPjeApproved != 1){
				if(data.approverEmails.indexOf(useremail)!=-1){
					$("#transactionTable").find('tr[id="transactionProvisionEntity'+data.id+'"]').remove();
					if(data.status=='Require Approval' || data.status=='Clarified'){
						//check for user mail existence in the approver usermail list sent by server
						//based on transaction status row data is displayed
						$("#transactionTable").prepend('<tr id="transactionProvisionEntity'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
						'<b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td>'+
						'<td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><br/><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td><td><div class="rowToExpand"><b>UNITS:</b><b>GROSS:</b><br/><p class="number-right">'+data.grossAmount+'</p></div></td>'+
						'<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.status+'</div>'+
						'<br/>Action:<select class="approverActionList" name="approverActionList" id="approverActionList"><option value="">--Please Select--</option>'+
						'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>'+
						'<span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
						'<input type="button" id="approverAction" class="btn btn-submit btn-center" value="Submit" onclick="completePovisionAction(this)"><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div></td>'+
						'<td><div class="rowToExpand"><select name="txnViewListUpload" id="fileDownload"><option value="">--Please Select--</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>');
						console.log("pje4 = " + data.txnRefNo);
					}else{
						$("#transactionTable").prepend('<tr id="transactionProvisionEntity'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
						'<b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><br/><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td><td><div class="rowToExpand"><b>GROSS:</b><br/><p class="number-right">'+data.grossAmount+'</p></div></td>'+
						'<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.status+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div></div></td>'+
						'<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">--Please Select--</option></select><br>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>');
						console.log("pje5 = " + data.txnRefNo);
					}
				}
		    }
			if(data.selectedAdditionalApproval != null && data.selectedAdditionalApproval != ""){
			    if(data.selectedAdditionalApproval == useremail){
					if(data.status=='Require Additional Approval'){
						$("#transactionTable").find('tr[id="transactionProvisionEntity'+data.id+'"]').remove();
						$("#transactionTable").prepend('<tr id="transactionProvisionEntity'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
						'<b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><br/><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td><td><div class="rowToExpand"><b>GROSS:</b><br/><p class="number-right">'+data.grossAmount+'</p></div></td>'+
						'<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.status+'</div>'+
						'<br/>Action:<select class="approverActionList" name="approverActionList" id="approverActionList"><option value="">--Please Select--</option>'+
						'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>'+
						'<span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
						'<input type="button" id="approverAction" class="approverAction btn btn-submit" value="Submit" onclick="completePovisionAction(this)"><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div></td>'+
						'<td><div class="rowToExpand"><select name="txnViewListUpload" id="fileDownload"><option value="">--Please Select--</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>');
						console.log("pje6 = " + data.txnRefNo);
					}else if(isPjeApproved != 1 && data.status != 'Accounted'){
						$("#transactionTable").prepend('<tr id="transactionProvisionEntity'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
						'<b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><br/><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td><td><div class="rowToExpand"><p class="number-right">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b><br/><p class="number-right">'+data.unitPrice+'</p><div class="FrieghtChargesDiv"></div><b>GROSS:</b><br/><p class="number-right">'+data.grossAmount+'</p></div></td>'+
						'<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.status+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div></div></td>'+
						'<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">--Please Select--</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>');
						console.log("pje7 = " + data.txnRefNo);
					}
				}else{
			    	if(data.status=='Require Additional Approval'){
			    		$("#transactionTable tr[id='transactionProvisionEntity"+data.id+"'] td:nth-child(8) div:first").append('<br/><b>Additional Approval Required By:</b><br/>'+data.selectedAdditionalApproval+'');
			    	}
			    }
			}else{
				if(data.role.includes("ACCOUNTANT") || data.role.includes("AUDITOR") || data.role.includes("CONTROLLER")){
					var approverEmailVal=false;	var selectedAdditionalApproval=false;
		    		 if(data.approverEmails!=null && data.approverEmails!=""){
		    			 if(data.approverEmails.indexOf(useremail)!=-1){
		    				 approverEmailVal=true;
		    			 }
		    		 }
		    		 if(data.selectedAdditionalApproval!=null && data.selectedAdditionalApproval!=""){
						if(data.selectedAdditionalApproval==useremail){
							selectedAdditionalApproval=true;
						}
		    		 }
		    		 if(data.createdBy != useremail && approverEmailVal == false && selectedAdditionalApproval == false){
						$("#transactionTable").prepend('<tr id="transactionProvisionEntity'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
		 				 '<b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><div class="provisionItemName"><p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
		 				 '<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><br/><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td><td><div class="rowToExpand"><b>GROSS:</b><br/><p class="number-right">'+data.grossAmount+'</p></div></td>'+
		 				 '<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.status+'</div><br/><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div><div class="invoiceForm"></div></div></td>'+
		 				 '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload" ><option value="">--Please Select--</option></select>'+
							'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
							'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
	                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
		 				 '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>');
						 console.log("pje8 = " + data.txnRefNo);
		    		 }
				}
			}
		    if(data.additionalApprovarUsers!=null){
			    if(typeof data.additionalApprovarUsers!='undefined'){
			    	var additionalApprovarUsersList=data.additionalApprovarUsers.substring(0,data.additionalApprovarUsers.length).split(',');
			    	$("tr[id='transactionProvisionEntity"+data.id+"'] select[id='userAddApproval']").append('<option value="">--Please Select--</option>');
			    	for(var i=0;i<additionalApprovarUsersList.length;i++){
			    		$("tr[id='transactionProvisionEntity"+data.id+"'] select[id='userAddApproval']").append('<option value="'+additionalApprovarUsersList[i]+'">'+additionalApprovarUsersList[i]+'</option>');
			    	}
			    }
			}

			if(data.txnRemarks!=null && data.txnRemarks!=""){
			   	var individualRemarks=data.txnRemarks.substring(0,data.txnRemarks.length).split('|');
			   	for(var m=0;m<individualRemarks.length;m++){
			    	var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
			    	$('#transactionProvisionEntity'+data.id+' div[class="txnWorkflowRemarks"]').append('<font color="102E55"><b>'+emailAndRemarks[0]+'</b></font>#');
			    	if(typeof emailAndRemarks[1]!='undefined'){
			    		if(emailAndRemarks[0].indexOf("(Auditor)")!=-1){
				    		$('#transactionProvisionEntity'+data.id+' div[class="txnWorkflowRemarks"]').append('<h1auditor>'+emailAndRemarks[1]+'</h1auditor><br/>');
				    	}
			    		else {
			    			$('#transactionProvisionEntity'+data.id+' div[class="txnWorkflowRemarks"]').append('<font color="blue">'+emailAndRemarks[1]+'</font><br/>');
			    		}
			    	}
			    }
			}
		    if(data.txnDocument!="" && data.txnDocument!=null){
		    	var txndocument=data.txnDocument;
				var transTrID = 'transactionProvisionEntity'+data.id;
				fillSelectElementWithUploadedDocs(txndocument, transTrID, 'txnViewListUpload');
	    	}
		    if(data.createdBy==data.approverEmail){
		    	if(data.status=='Approved' && isPjeApproved == 0){
		    		$("tr[id='transactionProvisionEntity"+data.id+"'] td:nth-child(8)").append('<input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit btn-idos" onclick="completeProvisionAccounting(this)" style="margin-top:10px;margin-left: 0px;">');
		        }
		    }
		    if(data.status!="" && data.status=="Rejected"){
		    	$("#transactionTable").find('tr[id="transactionProvisionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstatred');
		    	$("#transactionTable").find('tr[id="transactionProvisionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstatred');
		    	$("#transactionTable").find('tr[id="transactionProvisionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstatred');
		    } else if(data.status!="" && data.status=="Accounted"){
		    	$("#transactionTable").find('tr[id="transactionProvisionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstat');
		    	$("#transactionTable").find('tr[id="transactionProvisionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstat');
		    	$("#transactionTable").find('tr[id="transactionProvisionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstat');
		    } else if(data.status!="" && (data.status=="Approved" || data.status=="Require Approval" || data.status=="Require Additional Approval")){
		    	$("#transactionTable").find('tr[id="transactionProvisionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstatgreen');
		    	$("#transactionTable").find('tr[id="transactionProvisionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstatgreen');
		    	$("#transactionTable").find('tr[id="transatransactionProvisionEntityctionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstatgreen');
		    }
		    if((data.role.includes("CREATOR") || data.role.includes("APPROVER") || data.role.includes("ACCOUNTANT") || data.role.includes("AUDITOR")) && (!data.role.includes("MASTER ADMIN") && !data.role.includes("CONTROLLER"))){
				var locHash=window.location.hash;
				if(locHash=="" || locHash=="#pendingExpense"){
					location.hash="#pendingExpense";
				}else if(locHash=="#labourHiring"){
					location.hash="#labourHiring";
				}
		    }
			
			if(COMPANY_OWNER == COMPANY_PWC){
                $("#transactionProvisionEntity"+data.id + " .approverActionList").children().remove();
                $("#transactionProvisionEntity"+data.id + " .approverActionList").append('<option value="">Select an action</option><option value="4">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option>');
            }
			
		} else if(data.txnType=="processPayroll"){
			var useremail=data.useremail;
			var isprApproved = 0;
			$("#transactionTable").find('tr[id="transactionPayroll'+data.id+'"]').remove();
			if(data.createdBy==useremail){
				if(data.status=='Approved'){
					isprApproved = 1;
					$("#transactionTable").prepend('<tr id="transactionPayroll'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><p color="blue" class="branchDetails" id='+data.branchId+'>'+data.branchName+'</p><br/>'+
		    		'<br/><b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font><br>Payroll Month-Year:</br></div></td>'+
		    		'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><br/><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td><td><div class="rowToExpand"><b>Total Earnings:</b><p class="number-right">'+data.grossAmount+'</p><br><b>Total Deductions:</b><p class="number-right">'+data.totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.workingDays+'</p><div></td>'+
		    		'<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.status+'</div>'+
		    		'<select name="paryollreceiptdetail" id="paryollreceiptdetail" class="txnPaymodeCls" onchange="listAllBranchBankAccounts(this);"><optgroup label="Select Mode of reciept"></optgroup><option value="1">CASH</option><option value="2">BANK</option></select><input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit btn-idos" onclick="completePayrollAccounting(this)" style="margin-top:10px;margin-left: 0px;"><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div></td>'+
		    		'<td><div class="rowToExpand"><select name="txnViewListUpload" id="fileDownload"><option value="">--Please Select--</option></select>'+
					'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
					'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                    '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
		    		'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td>'+
		    		'</tr>');
		    	}else{
		    		if(data.status=='Require Clarification'){
			    		$("#transactionTable").prepend('<tr id="transactionPayroll'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
			    		'<b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
			    		'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><br/><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td><td><div class="rowToExpand"><b>Total Earnings:</b><p class="number-right">'+data.grossAmount+'</p><br><b>Total Deductions:</b><p class="number-right">'+data.totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.workingDays+'</p></div></td>'+
			    		'<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.status+'</div>'+
			    		'<br/><input type="button" value="Clarify Transaction" id="clarifyTxn" class="btn btn-submit btn-idos" onclick="clarifyProvisionTransaction(this)" style="margin-top:10px;margin-left: 0px;width:170px;"><br/><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font><div class="invoiceForm"></div></div></div></td>'+
			    		'<td><div class="rowToExpand"><select name="txnViewListUpload" id="fileDownload"><option value="">--Please Select--</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
			    		'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>');
			    	}else{
						$("#transactionTable").prepend('<tr id="transactionPayroll'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
				    	'<b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
				    	'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><br/><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td><td><div class="rowToExpand"><b>Total Earnings:</b><p class="number-right">'+data.grossAmount+'</p><br><b>Total Deductions:</b><p class="number-right">'+data.totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.workingDays+'</p></div></td>'+
				    	'<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.status+'</div><br/><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div><div class="invoiceForm"></div></div></td>'+
				    	'<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">--Please Select--</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
				    	'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>');
			    	}
		    	}
			}
			//for approver can be same user or not
		    if(data.approverEmails!=null && isprApproved != 1){
				if(data.approverEmails.indexOf(useremail)!=-1){
					$("#transactionTable").find('tr[id="transactionPayroll'+data.id+'"]').remove();
					if(data.status=='Require Approval' || data.status=='Clarified'){
						//check for user mail existence in the approver usermail list sent by server
						//based on transaction status row data is displayed
						$("#transactionTable").prepend('<tr id="transactionPayroll'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
						'<b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td>'+
						'<td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><br/><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td><td><div class="rowToExpand"><b>Total Earnings:</b><p class="number-right">'+data.grossAmount+'</p><br><b>Total Deductions:</b><p class="number-right">'+data.totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.workingDays+'</p></div></td>'+
						'<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.status+'</div>'+
						'<br/>Action:<select class="approverActionList" name="approverActionList" id="approverActionList"><option value="">--Please Select--</option>'+
						'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>'+
						'<span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
						'<input type="button" id="approverAction" class="btn btn-submit btn-center" value="Submit" onclick="completePayrollAction(this)"><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div></td>'+
						'<td><div class="rowToExpand"><select name="txnViewListUpload" id="fileDownload" ><option value="">--Please Select--</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>');
					}else{
						$("#transactionTable").prepend('<tr id="transactionPayroll'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
						'<b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><br/><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td><td><div class="rowToExpand"><b>Total Earnings:</b><p class="number-right">'+data.grossAmount+'</p><br><b>Total Deductions:</b><p class="number-right">'+data.totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.workingDays+'</p></div></td>'+
						'<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.status+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div></div></td>'+
						'<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload" ><option value="">--Please Select--</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>');
					}
				}
		    }
			if(data.selectedAdditionalApproval!=null && data.selectedAdditionalApproval!=""){
			    if(data.selectedAdditionalApproval==useremail){
					if(data.status=='Require Additional Approval'){
						$("#transactionTable").find('tr[id="transactionPayroll'+data.id+'"]').remove();
						$("#transactionTable").prepend('<tr id="transactionPayroll'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
						'<b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><br/><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td><td><div class="rowToExpand"><b>Total Earnings:</b><p class="number-right">'+data.grossAmount+'</p><br><b>Total Deductions:</b><p class="number-right">'+data.totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.workingDays+'</p></div></td>'+
						'<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.status+'</div>'+
						'<br/>Action:<select class="approverActionList" name="approverActionList" id="approverActionList"><option value="">--Please Select--</option>'+
						'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>'+
						'<span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
						'<input type="button" id="approverAction" class="approverAction btn btn-submit" value="Submit" onclick="completePayrollAction(this)"><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div></td>'+
						'<td><div class="rowToExpand"><select name="txnViewListUpload" id="fileDownload"><option value="">--Please Select--</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>');
					}else{
						$("#transactionTable").prepend('<tr id="transactionPayroll'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
						'<b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><br/><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td><td><div class="rowToExpand"><p class="number-right">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b><br/><p class="number-right">'+data.unitPrice+'</p><div class="FrieghtChargesDiv"></div><b>Total Earnings:</b><p class="number-right">'+data.grossAmount+'</p><br><b>Total Deductions:</b><p class="number-right">'+data.totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.workingDays+'</p></div></td>'+
						'<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/><br/></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.status+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div></div></td>'+
						'<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">--Please Select--</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
						'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>');
					}
				}else{
			    	if(data.status=='Require Additional Approval'){
			    		$("#transactionTable tr[id='transactionPayroll"+data.id+"'] td:nth-child(8) div:first").append('<br/><b>Additional Approval Required By:</b><br/>'+data.selectedAdditionalApproval+'');
			    	}
			    }
			}else{
				if(data.role.includes("ACCOUNTANT") || data.role.includes("AUDITOR") || data.role.includes("CONTROLLER")){
					var approverEmailVal=false;var selectedAdditionalApproval=false;
		    		 if(data.approverEmails!=null && data.approverEmails!=""){
		    			 if(data.approverEmails.indexOf(useremail)!=-1){
		    				 approverEmailVal=true;
		    			 }
		    		 }
		    		 if(data.selectedAdditionalApproval!=null && data.selectedAdditionalApproval!=""){
						    if(data.selectedAdditionalApproval==useremail){
						    	selectedAdditionalApproval=true;
						    }
		    		 }
		    		 if(data.createdBy!=useremail && approverEmailVal==false && selectedAdditionalApproval==false){
						$("#transactionTable").prepend('<tr id="transactionPayroll'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><p style="color: blue; cursor: pointer"><a ' + (data.status  == "Accounted" ?  ' style="cursor:pointer;" onclick="getTransacionAccountingInfo(this);"': '' )+ '">'+data.txnRefNo+'</a></p></div></td><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/>'+
		 				 '<b>CREATOR:</b><br/><font color="blue">'+data.createdBy+'</font></div></td><td><div class="rowToExpand"><b><a id="multiSellItemsList" onclick="listPayrollItems(this);">Details:</a></b><div class="provisionItemName"></div></div></td><td><div class="rowToExpand"><b>TRANSACTION PURPOSE:</b><br/><font color="blue">'+data.transactionPurpose+'</font></div></td>'+
		 				 '<td><div class="rowToExpand"><font color="blue">'+data.txnDate+'</font><br/><b>'+data.invoiceDateLabel+'</b><br/><font color="blue">'+data.invoiceDate+'</font></div></td><td><div class="rowToExpand" style="color: blue;">'+data.paymentMode+'<br>' + data.instrumentNumber +'<br>' + data.instrumentDate +'</div></td><td><div class="rowToExpand"><b>Total Earnings:</b><p class="number-right">'+data.grossAmount+'</p><br><b>Total Deductions:</b><p class="number-right">'+data.totalDeductions+'</p><br><b>No. of Working Days</b><p class="number-right">'+data.workingDays+'</p></div></td>'+
		 				 '<td><div class="rowToExpand"><p class="number-right">'+data.netAmount+'</p><br/></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></div></td><td><div class="rowToExpand"><div style="height: 130px;overflow: auto;"><div class="txnstat">'+data.status+'</div><br/><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approverEmail+'</font></div><div class="invoiceForm"></div></div></td>'+
		 				 '<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="fileDownload"><option value="">--Please Select--</option></select>'+
							'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button>'+
							'<button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button>'+
	                        '<button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
		 				 '<td><div class="rowToExpand"><div class="txnWorkflowRemarks"></div><div><textarea rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="givetxnProvisionRemarks(this)"></div></div></td></tr>');
		    		 }
				}
			}
		    if(data.additionalApprovarUsers!=null){
			    if(typeof data.additionalApprovarUsers!='undefined'){
			    	var additionalApprovarUsersList=data.additionalApprovarUsers.substring(0,data.additionalApprovarUsers.length).split(',');
			    	$("tr[id='transactionPayroll"+data.id+"'] select[id='userAddApproval']").append('<option value="">--Please Select--</option>');
			    	for(var i=0;i<additionalApprovarUsersList.length;i++){
			    		$("tr[id='transactionPayroll"+data.id+"'] select[id='userAddApproval']").append('<option value="'+additionalApprovarUsersList[i]+'">'+additionalApprovarUsersList[i]+'</option>');
			    	}
			    }
			}

			if(data.itemName!=null && data.itemName!=""){
				var individualitemName=data.itemName.substring(0, data.itemName.length).split('|');
				if(individualitemName.length > 0){
					$('#transactionPayroll'+data.id+' div[class="provisionItemName"]').append('<b style="color: #102E55;">Debit: </b>');
					$('#transactionPayroll'+data.id+' div[class="provisionItemName"]').append('<p style="color: blue;">'+individualitemName[0]+'</p>');
					$('#transactionPayroll'+data.id+' div[class="provisionItemName"]').append('<b style="color: #102E55;">Credit: </b>');
					if(typeof individualitemName[1]!='undefined'){
						$('#transactionProvisionEntity'+data.id+' div[class="provisionItemName"]').append('<p style="color: blue;">'+individualitemName[1]+'</p>');
					}
				}
			}

			if(data.txnRemarks!=null && data.txnRemarks!=""){
			   	var individualRemarks=data.txnRemarks.substring(0,data.txnRemarks.length).split('|');
			   	for(var m=0;m<individualRemarks.length;m++){
			    	var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
			    	$('#transactionPayroll'+data.id+' div[class="txnWorkflowRemarks"]').append('<font color="102E55"><b>'+emailAndRemarks[0]+'</b></font>#');
			    	if(typeof emailAndRemarks[1]!='undefined'){
			    		if(emailAndRemarks[0].indexOf("(Auditor)")!=-1){
				    		$('#transactionPayroll'+data.id+' div[class="txnWorkflowRemarks"]').append('<h1auditor>'+emailAndRemarks[1]+'</h1auditor><br/>');
				    	}
			    		else {
			    			$('#transactionPayroll'+data.id+' div[class="txnWorkflowRemarks"]').append('<font color="blue">'+emailAndRemarks[1]+'</font><br/>');
			    		}
			    	}
			    }
			}
		    if(data.txnDocument!="" && data.txnDocument!=null){
		    	var txndocument=data.txnDocument;
				var transTrID = 'transactionPayroll'+data.id;
				fillSelectElementWithUploadedDocs(txndocument, transTrID, 'txnViewListUpload');
	    	}
		    if(data.status!="" && data.status=="Rejected"){
		    	$("#transactionTable").find('tr[id="transactionPayroll'+data.id+'"] div[class="txnstat"]').attr('class','txnstatred');
		    	$("#transactionTable").find('tr[id="transactionPayroll'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstatred');
		    	$("#transactionTable").find('tr[id="transactionPayroll'+data.id+'"] div[class="txnstatred"]').attr('class','txnstatred');
		    } else if(data.status!="" && data.status=="Accounted"){
		    	$("#transactionTable").find('tr[id="transactionPayroll'+data.id+'"] div[class="txnstat"]').attr('class','txnstat');
		    	$("#transactionTable").find('tr[id="transactionPayroll'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstat');
		    	$("#transactionTable").find('tr[id="transactionPayroll'+data.id+'"] div[class="txnstatred"]').attr('class','txnstat');
		    } else if(data.status!="" && (data.status=="Approved" || data.status=="Require Approval" || data.status=="Require Additional Approval")){
		    	$("#transactionTable").find('tr[id="transactionPayroll'+data.id+'"] div[class="txnstat"]').attr('class','txnstatgreen');
		    	$("#transactionTable").find('tr[id="transactionPayroll'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstatgreen');
		    	$("#transactionTable").find('tr[id="transactionPayroll'+data.id+'"] div[class="txnstatred"]').attr('class','txnstatgreen');
		    }
		    if((data.role.includes("CREATOR") || data.role.includes("APPROVER") || data.role.includes("ACCOUNTANT") || data.role.includes("AUDITOR")) && (!data.role.includes("MASTER ADMIN") && !data.role.includes("CONTROLLER"))){
				var locHash=window.location.hash;
				if(locHash=="" || locHash=="#pendingExpense"){
					location.hash="#pendingExpense";
				}if(locHash=="#labourHiring"){
					location.hash="#labourHiring";
				}
		    }
		    $("#createExpense").slideUp('slow');
		}else if(data.txnType=="claimTxn"){
			var useremail=data.useremail;
		    $("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"]').remove();
		    //for creator claim detail data display logic
		    if(data.createdBy==useremail){
		    	//now based of claim transaction status display the claim txn row
		    	if(data.claimTxnStatus=='Require Clarification'){
		    		//when approver send a request toclarify the claim transaction created by creator
		    		$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
		    		'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Travel Type:</b><br/><font color="blue">'+data.claimtravelType+'</font>'+
		    		'<br/><b>Number Of Places Visited:</b><br/><font color="blue">'+data.claimnoOfPlacesToVisit+'</font><br/><b>Places Visited:</b><br/><font color="blue">'+data.claimplacesSelectedOrEntered+'</font>'+
		    		'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><font color="blue">'+data.claimtypeOfCity+'</font><br/><b>Approximate Distance:</b><br/><font color="blue">'+data.claimappropriateDiatance+'</font><br/><b>Total Days(Excluding Days Of Travel):</b><br/><font color="blue">'+data.claimtotalDays+'</font><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
		    		'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><font color="blue">'+data.claimexistingAdvance+'</font><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><font color="blue">'+data.claimadjustedAdvance+'</font><br/><b>Entered Advance:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Total Advance:</b><br/><font color="blue">'+data.claimtotalAdvance+'</font>'+
		    		'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Visit:</b><br/><font color="blue">'+data.claimpurposeOfVisit+'</font></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font><br/><input type="button" value="Clarify Transaction" id="clarifyTxn" class="btn btn-submit btn-idos" onclick="claimclarifyTransaction(this)" style="margin-top:10px;margin-left: 0px;width:170px;"></div></td><td><div class="rowToExpand"><select name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select><br>'+
					'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button>'+
                    '</div></td><td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
		    	}else{
		    		$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.txnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
		    		'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Travel Type:</b><br/><font color="blue">'+data.claimtravelType+'</font>'+
		    		'<br/><b>Number Of Places Visited:</b><br/><font color="blue">'+data.claimnoOfPlacesToVisit+'</font><br/><b>Places Visited:</b><br/><font color="blue">'+data.claimplacesSelectedOrEntered+'</font>'+
		    		'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><font color="blue">'+data.claimtypeOfCity+'</font><br/><b>Approximate Distance:</b><br/><font color="blue">'+data.claimappropriateDiatance+'</font><br/><b>Total Days(Excluding Days Of Travel):</b><br/><font color="blue">'+data.claimtotalDays+'</font><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
		    		'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><font color="blue">'+data.claimexistingAdvance+'</font><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><font color="blue">'+data.claimadjustedAdvance+'</font><br/><b>Entered Advance:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Total Advance:</b><br/><font color="blue">'+data.claimtotalAdvance+'</font>'+
		    		'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.claimpurposeOfVisit+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select><br>'+
					'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
		    		'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
		    	}
		    }
		    //for approver can be same user or not
		    if(data.approverEmails!=null){
		    	if(data.approverEmails.indexOf(useremail)!=-1){
		    		$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"]').remove();
					if(data.claimTxnStatus=='Require Approval' || data.claimTxnStatus=='Clarified'){
						$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
					    '<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Travel Type:</b><br/><font color="blue">'+data.claimtravelType+'</font>'+
					    '<br/><b>Number Of Places Visited:</b><br/><font color="blue">'+data.claimnoOfPlacesToVisit+'</font><br/><b>Places Visited:</b><br/><font color="blue">'+data.claimplacesSelectedOrEntered+'</font>'+
					    '</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><font color="blue">'+data.claimtypeOfCity+'</font><br/><b>Approximate Distance:</b><br/><font color="blue">'+data.claimappropriateDiatance+'</font><br/><b>Total Days(Excluding Days Of Travel):</b><br/><font color="blue">'+data.claimtotalDays+'</font><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
					    '<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><font color="blue">'+data.claimexistingAdvance+'</font><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><font color="blue">'+data.claimadjustedAdvance+'</font><br/><b>Entered Advance:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Total Advance:</b><br/><font color="blue">'+data.claimtotalAdvance+'</font>'+
					    '</div></div></td><td><div class="rowToExpand"><b>Purpose Of Visit:</b><br/><font color="blue">'+data.claimpurposeOfVisit+'</font></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><br/>Action:<select class="claimapproverActionList" name="claimapproverActionList" id="claimapproverActionList"><option value="">--Please Select--</option>'+
						'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/><span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
						'<input type="button" id="claimapproverAction" class="btn btn-submit btn-center" value="Submit" onclick="claimcompleteAction(this)"><br/><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select name="txnViewListUpload" id="claimfileDownload""><option value="">-Please Select-</option></select>'+
						'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
					    '<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 195px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>');
					}else{
						$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
					    '<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Travel Type:</b><br/><font color="blue">'+data.claimtravelType+'</font>'+
					    '<br/><b>Number Of Places Visited:</b><br/><font color="blue">'+data.claimnoOfPlacesToVisit+'</font><br/><b>Places Visited:</b><br/><font color="blue">'+data.claimplacesSelectedOrEntered+'</font>'+
					    '</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><font color="blue">'+data.claimtypeOfCity+'</font><br/><b>Approximate Distance:</b><br/><font color="blue">'+data.claimappropriateDiatance+'</font><br/><b>Total Days(Excluding Days Of Travel):</b><br/><font color="blue">'+data.claimtotalDays+'</font><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
					    '<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><font color="blue">'+data.claimexistingAdvance+'</font><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><font color="blue">'+data.claimadjustedAdvance+'</font><br/><b>Entered Advance:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Total Advance:</b><br/><font color="blue">'+data.claimtotalAdvance+'</font>'+
					    '</div></div></td><td><div class="rowToExpand"><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.claimpurposeOfVisit+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select><br>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
					    '<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
					}
		    	}
		    }
		    if(data.role.includes("ACCOUNTANT") || data.role.includes("AUDITOR") || data.role.includes("CONTROLLER")){
		    	 var approverEmailVal=false;var selectedAdditionalApproval=false;
	    		 if(data.approverEmails!=null && data.approverEmails!=""){
	    			 if(data.approverEmails.indexOf(useremail)!=-1){
	    				 approverEmailVal=true;
	    			 }
	    		 }
	    		 if(data.selectedAdditionalApproval!=null && data.selectedAdditionalApproval!=""){
					    if(data.selectedAdditionalApproval==useremail){
					    	selectedAdditionalApproval=true;
					    }
	    		 }
	    		 if(data.createdBy!=useremail && approverEmailVal==false && selectedAdditionalApproval==false){
	    			 $("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
	    			 '<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Travel Type:</b><br/><font color="blue">'+data.claimtravelType+'</font>'+
	    			 '<br/><b>Number Of Places Visited:</b><br/><font color="blue">'+data.claimnoOfPlacesToVisit+'</font><br/><b>Places Visited:</b><br/><font color="blue">'+data.claimplacesSelectedOrEntered+'</font>'+
	    			 '</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><font color="blue">'+data.claimtypeOfCity+'</font><br/><b>Approximate Distance:</b><br/><font color="blue">'+data.claimappropriateDiatance+'</font><br/><b>Total Days(Excluding Days Of Travel):</b><br/><font color="blue">'+data.claimtotalDays+'</font><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
	    			 '<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><font color="blue">'+data.claimexistingAdvance+'</font><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><font color="blue">'+data.claimadjustedAdvance+'</font><br/><b>Entered Advance:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Total Advance:</b><br/><font color="blue">'+data.claimtotalAdvance+'</font>'+
	    			 '</div></div></td><td><div class="rowToExpand"><b>Purpose Of Visit:</b><br/><p style="color:blue;">'+data.claimpurposeOfVisit+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select><br>'+
 					 '<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
	    			 '<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
	    		 }
		    }
		    //for accountant can be same user or not
			if(data.claimTxnStatus=="Approved"){
					if(data.createdBy==useremail) {
						$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"]').remove();
						if(data.claimTxnStatus=="Approved"){
							$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
							'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Travel Type:</b><br/><font color="blue">'+data.claimtravelType+'</font>'+
							'<br/><b>Number Of Places Visited:</b><br/><font color="blue">'+data.claimnoOfPlacesToVisit+'</font><br/><b>Places Visited:</b><br/><font color="blue">'+data.claimplacesSelectedOrEntered+'</font>'+
							'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><font color="blue">'+data.claimtypeOfCity+'</font><br/><b>Approximate Distance:</b><br/><font color="blue">'+data.claimappropriateDiatance+'</font><br/><b>Total Days(Excluding Days Of Travel):</b><br/><font color="blue">'+data.claimtotalDays+'</font><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
							'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><font color="blue">'+data.claimexistingAdvance+'</font><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><font color="blue">'+data.claimadjustedAdvance+'</font><br/><b>Entered Advance:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Total Advance:</b><br/><font color="blue">'+data.claimtotalAdvance+'</font>'+
							'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Visit:</b><br/><font color="blue">'+data.claimpurposeOfVisit+'</font></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div>'+
							'<br/><input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit btn-idos" onclick="claimcompleteAccounting(this)" style="margin-top:10px;margin-left: 0px;"><br/><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select><br>'+
							'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
							'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
						}else{
							$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td>'+ data.id+'<div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
							'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Travel Type:</b><br/><font color="blue">'+data.claimtravelType+'</font>'+
							'<br/><b>Number Of Places Visited:</b><br/><font color="blue">'+data.claimnoOfPlacesToVisit+'</font><br/><b>Places Visited:</b><br/><font color="blue">'+data.claimplacesSelectedOrEntered+'</font>'+
							'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><font color="blue">'+data.claimtypeOfCity+'</font><br/><b>Approximate Distance:</b><br/><font color="blue">'+data.claimappropriateDiatance+'</font><br/><b>Total Days(Excluding Days Of Travel):</b><br/><font color="blue">'+data.claimtotalDays+'</font><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
							'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><font color="blue">'+data.claimexistingAdvance+'</font><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><font color="blue">'+data.claimadjustedAdvance+'</font><br/><b>Entered Advance:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Total Advance:</b><br/><font color="blue">'+data.claimtotalAdvance+'</font>'+
							'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.claimpurposeOfVisit+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select><br>'+
							'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
							'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
						}
					}
			}

			if(data.selectedAdditionalApproval==useremail){
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"]').remove();
				if(data.claimTxnStatus=='Require Additional Approval'){
					$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
					'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Travel Type:</b><br/><font color="blue">'+data.claimtravelType+'</font>'+
					'<br/><b>Number Of Places Visited:</b><br/><font color="blue">'+data.claimnoOfPlacesToVisit+'</font><br/><b>Places Visited:</b><br/><font color="blue">'+data.claimplacesSelectedOrEntered+'</font>'+
					'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><font color="blue">'+data.claimtypeOfCity+'</font><br/><b>Approximate Distance:</b><br/><font color="blue">'+data.claimappropriateDiatance+'</font><br/><b>Total Days(Excluding Days Of Travel):</b><br/><font color="blue">'+data.claimtotalDays+'</font><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
					'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><font color="blue">'+data.claimexistingAdvance+'</font><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><font color="blue">'+data.claimadjustedAdvance+'</font><br/><b>Entered Advance:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Total Advance:</b><br/><font color="blue">'+data.claimtotalAdvance+'</font>'+
					'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Visit:</b><br/><font color="blue">'+data.claimpurposeOfVisit+'</font></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><br/>Action:<select class="claimapproverActionList" name="claimapproverActionList" id="claimapproverActionList"><option value="">--Please Select--</option>'+
					'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/><span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
					'<input type="button" id="claimapproverAction" class="btn btn-submit btn-center" value="Submit" onclick="claimcompleteAction(this)"><br/><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
					'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
					'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
				}else{
					$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
					'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Travel Type:</b><br/><font color="blue">'+data.claimtravelType+'</font>'+
					'<br/><b>Number Of Places Visited:</b><br/><font color="blue">'+data.claimnoOfPlacesToVisit+'</font><br/><b>Places Visited:</b><br/><font color="blue">'+data.claimplacesSelectedOrEntered+'</font>'+
					'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><font color="blue">'+data.claimtypeOfCity+'</font><br/><b>Approximate Distance:</b><br/><font color="blue">'+data.claimappropriateDiatance+'</font><br/><b>Total Days(Excluding Days Of Travel):</b><br/><font color="blue">'+data.claimtotalDays+'</font><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
					'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><font color="blue">'+data.claimexistingAdvance+'</font><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><font color="blue">'+data.claimadjustedAdvance+'</font><br/><b>Entered Advance:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Total Advance:</b><br/><font color="blue">'+data.claimtotalAdvance+'</font>'+
					'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.claimpurposeOfVisit+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
					'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
					'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
				}
		    }else{
		    	if(data.status=='Require Additional Approval'){
		    		$("#claimDetailsTable tr[id='claimsTransactionEntity"+data.id+"'] td:nth-child(8) div:first").append('<br/><b>Additional Approval Required By:</b><br/>'+data.selectedAdditionalApproval+'');
		    	}
		    }
		    $('.claimCommonTable:visible').slideUp();
		    //logic for separation transaction travel eligibility separately into travel eligibility label and travel eligibility figure
		    if(data.claimtravelDetailedConfDescription!=null && data.claimtravelDetailedConfDescription!=""){
			   	var individualclaimtravelDetailedConfDescription=data.claimtravelDetailedConfDescription.substring(0,data.claimtravelDetailedConfDescription.length).split('#');
			   	for(var m=0;m<individualclaimtravelDetailedConfDescription.length;m++){
			    	if(m>0){
			    		var labelAndValue=individualclaimtravelDetailedConfDescription[m].substring(0, individualclaimtravelDetailedConfDescription[m].length).split(':');
			    		$('#claimsTransactionEntity'+data.id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></font>:<br/>');
			    		if(typeof labelAndValue[1]!='undefined'){
				    		$('#claimsTransactionEntity'+data.id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<font color="blue">'+labelAndValue[1]+'</font><br/>');
				    	}
			    	}
			    }
			}
		    //logic for separation transaction advance eligibility separately into advance eligibility label and advance eligibility figure
		    if(data.claimuserAdvanveEligibility!=null && data.claimuserAdvanveEligibility!=""){
			   	var individualclaimuserAdvanveEligibility=data.claimuserAdvanveEligibility.substring(0,data.claimuserAdvanveEligibility.length).split('#');
			   	for(var m=0;m<individualclaimuserAdvanveEligibility.length;m++){
			   		if(m>0){
			    		var labelAndValue=individualclaimuserAdvanveEligibility[m].substring(0, individualclaimuserAdvanveEligibility[m].length).split(':');
			    		$('#claimsTransactionEntity'+data.id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></font>:<br/>');
			    		if(typeof labelAndValue[1]!='undefined'){
				    		$('#claimsTransactionEntity'+data.id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<font color="blue">'+labelAndValue[1]+'</font><br/>');
				    	}
			    	}
			    }
			}
		    //logic for separation transaction remarks separately into useremail and remarks made by them
		    if(data.claimtxnRemarks!=null && data.claimtxnRemarks!=""){
			   	var individualRemarks=data.claimtxnRemarks.substring(0,data.claimtxnRemarks.length).split(',');
			   	for(var m=0;m<individualRemarks.length;m++){
			    	var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
			    	$('#claimsTransactionEntity'+data.id+' div[class="claimtxnWorkflowRemarks"]').append('<font color="#FFOFF"><b>'+emailAndRemarks[0]+'</b></font>#');
			    	if(typeof emailAndRemarks[1]!='undefined'){
			    		$('#claimsTransactionEntity'+data.id+' div[class="claimtxnWorkflowRemarks"]').append('<font color="blue">'+emailAndRemarks[1]+'</font><br/>');
			    	}
			    }
			}
		  //logic for separation transaction remarks separately into useremail and documents uploaded by them
		    if(data.claimsupportingDoc!="" && data.claimsupportingDoc!=null){
		    	var txndocument=data.claimsupportingDoc;
	    		var transTrID = 'claimsTransactionEntity'+data.id;
	    		fillSelectElementWithUploadedDocs(txndocument, transTrID, 'txnViewListUpload');
	    	}
		    if(data.txnSpecialStatus=="Rules Not Followed"){
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] b[class="txnSpecialStatus"]').html("");
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] b[class="txnSpecialStatus"]').append('<img src="assets/images/green.png"></img>');
		    }
		    if(data.claimTxnStatus!="" && data.claimTxnStatus=="Rejected"){
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstatred');
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstatred');
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstatred');
		    }
		    if(data.claimTxnStatus!="" && data.claimTxnStatus=="Accounted"){
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstat');
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstat');
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstat');
		    }
		    if(data.claimTxnStatus!="" && (data.claimTxnStatus=="Approved" || data.claimTxnStatus=="Require Approval" || data.claimTxnStatus=="Require Additional Approval") || data.claimTxnStatus=="Require Accounting"){
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstatgreen');
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstatgreen');
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstatgreen');
		    }
		    if(data.additionalApprovarUsers!=null){
			    if(typeof data.additionalApprovarUsers!='undefined'){
			    	var additionalApprovarUsersList=data.additionalApprovarUsers.substring(0,data.additionalApprovarUsers.length).split(',');
			    	$("tr[id='claimsTransactionEntity"+data.id+"'] select[id='userAddApproval']").append('<option value="">--Please Select--</option>');
			    	for(var i=0;i<additionalApprovarUsersList.length;i++){
			    		$("tr[id='claimsTransactionEntity"+data.id+"'] select[id='userAddApproval']").append('<option value="'+additionalApprovarUsersList[i]+'">'+additionalApprovarUsersList[i]+'</option>');
			    	}
			    }
			}
		    if(data.role.includes("CREATOR") || data.role.includes("APPROVER") || data.role.includes("ACCOUNTANT") || data.role.includes("AUDITOR")){
		    	var locHash=window.location.hash;
		    	if(locHash=="" || locHash=="#claimSetup"){
				    location.hash="#claimSetup";
		    	}
		    }
		    getCashBankReceivablePayable();
		    if(data.role.includes("APPROVER") || data.role.includes("ACCOUNTANT")){
		   		getTravelExpenseClaimCountForApproverAccountant(useremail);
		    }
		  //Edit transaction common for all claims type
			if(data.claimTxnStatus=='Require Approval' && data.createdBy==useremail){
				$("#claimDetailsTable tr[id='claimsTransactionEntity"+data.id+"'] td:nth-child(7) div:first").append('<br/><input type="button" value="Edit Transaction" id="editClaimAllowed" class="editClaimAllowed btn btn-submit btn-idos" onclick="editClaimsOnceAllowed(this)" style="margin-top:10px;margin-left: 0px;">');
			}
			else if((data.txnPurposeId=='18' || data.txnPurposeId=='16') && data.claimTxnStatus!='Accounted' && data.createdBy==useremail){
				$("#claimDetailsTable tr[id='claimsTransactionEntity"+data.id+"'] td:nth-child(7) div:first").append('<br/><input type="button" value="Edit Transaction" id="editClaimAllowed" class="editClaimAllowed btn btn-submit btn-idos" onclick="editClaimsOnceAllowed(this)" style="margin-top:10px;margin-left: 0px;">');
			}
		} else if(data.txnType=="claimSettlementTxn"){
			//claim settlement only for creator and accountant
			var useremail=data.useremail;
			console.log(JSON.stringify(data))
		    $("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"]').remove();
		    //for creator claim detail data display logic
		    if(data.createdBy==useremail){
		    	$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
		    	'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Travel Type:</b><br/><font color="blue">'+data.claimtravelType+'</font>'+
		    	'<br/><b>Number Of Places Visited:</b><br/><font color="blue">'+data.claimnoOfPlacesToVisit+'</font><br/><b>Places Visited:</b><br/><font color="blue">'+data.claimplacesSelectedOrEntered+'</font>'+
		    	'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><font color="blue">'+data.claimtypeOfCity+'</font><br/><b>Approximate Distance:</b><br/><font color="blue">'+data.claimappropriateDiatance+'</font><br/><b>Total Days(Excluding Days Of Travel):</b><br/><font color="blue">'+data.claimtotalDays+'</font><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
		    	'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div></div>'+
		    	'</div></td><td><div class="rowToExpand"><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.purposeOfVisit+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
				'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
		    	'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
		    }
		    if(data.role.includes("ACCOUNTANT") || data.role.includes("AUDITOR") || data.role.includes("CONTROLLER")){
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"]').remove();
		    	$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
				'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Travel Type:</b><br/><font color="blue">'+data.claimtravelType+'</font>'+
				'<br/><b>Number Of Places Visited:</b><br/><font color="blue">'+data.claimnoOfPlacesToVisit+'</font><br/><b>Places Visited:</b><br/><font color="blue">'+data.claimplacesSelectedOrEntered+'</font>'+
				'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><font color="blue">'+data.claimtypeOfCity+'</font><br/><b>Approximate Distance:</b><br/><font color="blue">'+data.claimappropriateDiatance+'</font><br/><b>Total Days(Excluding Days Of Travel):</b><br/><font color="blue">'+data.claimtotalDays+'</font><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
				'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div></div>'+
				'</div></td><td><div class="rowToExpand"><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.purposeOfVisit+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
				'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
				'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
		    }
		    //for accountant can be same user or not
				if(data.createdBy==useremail) {
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"]').remove();
			    	if(data.claimTxnStatus=="Payment Due To Staff" || data.claimTxnStatus=="Payment Due From Staff" || data.claimTxnStatus=="No Due For Settlement"){
			    		var tempHtml='<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
						'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Travel Type:</b><br/><font color="blue">'+data.claimtravelType+'</font>'+
						'<br/><b>Number Of Places Visited:</b><br/><font color="blue">'+data.claimnoOfPlacesToVisit+'</font><br/><b>Places Visited:</b><br/><font color="blue">'+data.claimplacesSelectedOrEntered+'</font>'+
						'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><font color="blue">'+data.claimtypeOfCity+'</font><br/><b>Approximate Distance:</b><br/><font color="blue">'+data.claimappropriateDiatance+'</font><br/><b>Total Days(Excluding Days Of Travel):</b><br/><font color="blue">'+data.claimtotalDays+'</font><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div></div>'+
						'</div></td><td><div class="rowToExpand"><b>Purpose Of Visit:</b><br/><font color="blue">'+data.purposeOfVisit+'</font></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><div class="claimpayementDiv"><br/>';
						if(data.claimTxnStatus == "Payment Due From Staff"){
							tempHtml+='Select mode of payment:<select name="claimpaymentDetails" id="claimpaymentDetails" onchange="listAllBranchBankAccountsClaims(this);">'+
							'<option value="1">CASH</option><option value="2">BANK</option></select><br/>Input payment details:<textarea name="bankDetails" id="bankDetails"></textarea>';
						}
				    	tempHtml+='</div><br/><input type="button" value="Complete Accounting"  id="settleTravelClaimTxn" class="btn btn-submit btn-idos" onclick="claimSettlementAccounting(this)" style="margin-top:10px;width:170px;margin-left: 0px;"><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>';
						$("#claimDetailsTable").prepend(tempHtml);
			    	}else{
			    		$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
						'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Travel Type:</b><br/><font color="blue">'+data.claimtravelType+'</font>'+
						'<br/><b>Number Of Places Visited:</b><br/><font color="blue">'+data.claimnoOfPlacesToVisit+'</font><br/><b>Places Visited:</b><br/><font color="blue">'+data.claimplacesSelectedOrEntered+'</font>'+
						'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><font color="blue">'+data.claimtypeOfCity+'</font><br/><b>Approximate Distance:</b><br/><font color="blue">'+data.claimappropriateDiatance+'</font><br/><b>Total Days(Excluding Days Of Travel):</b><br/><font color="blue">'+data.claimtotalDays+'</font><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td>'+
						'<td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div></div></div></td>'+
						'<td><div class="rowToExpand"><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.purposeOfVisit+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td>'+
						'<td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td>'+
						'<td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
			    	}
			}
			$('.claimCommonTable:visible').slideUp();
		    //logic for separation transaction unsettled Travel claims separately into unsettled label and figure
		    if(data.existingClaimsCurrentSettlementDetails!=null && data.existingClaimsCurrentSettlementDetails!=""){
			   	var individualexistingClaimsCurrentSettlementDetails=data.existingClaimsCurrentSettlementDetails.substring(0,data.existingClaimsCurrentSettlementDetails.length).split('#');
			   	for(var m=0;m<individualexistingClaimsCurrentSettlementDetails.length;m++){
			    	if(m>0){
			    		var labelAndValue=individualexistingClaimsCurrentSettlementDetails[m].substring(0, individualexistingClaimsCurrentSettlementDetails[m].length).split(':');
			    		$('#claimsTransactionEntity'+data.id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></font>:<br/>');
			    		if(typeof labelAndValue[1]!='undefined'){
				    		$('#claimsTransactionEntity'+data.id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<font color="blue">'+labelAndValue[1]+'</font><br/>');
				    	}
			    	}
			    }
			}
		    //logic for separation of user expenditure on the travel claim transaction separatly into label and figure
		    if(data.userExpenditureOnThisTxn!=null && data.userExpenditureOnThisTxn!=""){
			   	var individualuserExpenditureOnThisTxn=data.userExpenditureOnThisTxn.substring(0,data.userExpenditureOnThisTxn.length).split('#');
			   	for(var m=0;m<individualuserExpenditureOnThisTxn.length;m++){
			   		if(m>0){
			    		var labelAndValue=individualuserExpenditureOnThisTxn[m].substring(0, individualuserExpenditureOnThisTxn[m].length).split(':');
			    		$('#claimsTransactionEntity'+data.id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></font>:<br/>');
			    		if(typeof labelAndValue[1]!='undefined'){
				    		$('#claimsTransactionEntity'+data.id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<font color="blue">'+labelAndValue[1]+'</font><br/>');
				    	}
			    	}
			    }
			}
		    //logic for separation transaction remarks separately into useremail and remarks made by them
		    if(data.claimtxnRemarks!=null && data.claimtxnRemarks!=""){
			   	var individualRemarks=data.claimtxnRemarks.substring(0,data.claimtxnRemarks.length).split(',');
			   	for(var m=0;m<individualRemarks.length;m++){
			    	var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
			    	$('#claimsTransactionEntity'+data.id+' div[class="claimtxnWorkflowRemarks"]').append('<font color="#FFOFF"><b>'+emailAndRemarks[0]+'</b></font>#');
			    	if(typeof emailAndRemarks[1]!='undefined'){
			    		$('#claimsTransactionEntity'+data.id+' div[class="claimtxnWorkflowRemarks"]').append('<font color="blue">'+emailAndRemarks[1]+'</font><br/>');
			    	}
			    }
			}
		    //logic for separation transaction remarks separately into useremail and documents uploaded by them
		    if(data.claimsupportingDoc!="" && data.claimsupportingDoc!=null){
		    	var txndocument=data.claimsupportingDoc;
	    		var transTrID = 'claimsTransactionEntity'+data.id;
	    		fillSelectElementWithUploadedDocs(txndocument, transTrID, 'txnViewListUpload');
	    	}
		    if(data.role.includes("CREATOR") || data.role.includes("ACCOUNTANT")){
		    	var locHash=window.location.hash;
		    	if(locHash=="" || locHash=="#claimSetup"){
				    location.hash="#claimSetup";
		    	}
		    }
		    getCashBankReceivablePayable();
		    if(data.role.includes("ACCOUNTANT")){
		   		getTravelExpenseClaimCountForApproverAccountant(useremail);
		    }
		  //Edit transaction common for all claims type
			if(data.claimTxnStatus!='Accounted' && data.createdBy==useremail){
				$("#claimDetailsTable tr[id='claimsTransactionEntity"+data.id+"'] td:nth-child(7) div:first").append('<br/><input type="button" value="Edit Transaction" id="editClaimAllowed" class="editClaimAllowed btn btn-submit btn-idos" onclick="editClaimsOnceAllowed(this)" style="margin-top:10px;margin-left: 0px;">');
			}
		} else if(data.txnType=="expAdvanceSettlementTxn"){
			//for expense advance settlement transaction
			//expense advance settlement only for creator and accountant
			var useremail=data.useremail;
			var currentUserRole= data.role;
		    $("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"]').remove();
		    //for creator claim detail data display logic
		    if(data.createdBy==useremail){
		    	$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
			    '<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
			    '<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
			    '</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
			    '<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Advance Settled :</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Due From Company:</b><br/><font color="blue">'+data.dueFromCompany+'</font><br/><b>Due To Company:</b><br/><font color="blue">'+data.dueToCompany+'</font><br/><b>Amount Returned In Case Of Due To Company:</b><br/><font color="blue">'+data.amountReturnInCaseOfDueToCompany+'</font>'+
			    '</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
				'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
			    '<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
		    }
			if(currentUserRole.indexOf("ACCOUNTANT")!=-1 || currentUserRole.indexOf("AUDITOR")!=-1 || currentUserRole.indexOf("CONTROLLER")!=-1){
		    	$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
				'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
				'<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
				'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
				'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Advance Settled :</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Due From Company:</b><br/><font color="blue">'+data.dueFromCompany+'</font><br/><b>Due To Company:</b><br/><font color="blue">'+data.dueToCompany+'</font><br/><b>Amount Returned In Case Of Due To Company:</b><br/><p style="color: blue;">'+data.amountReturnInCaseOfDueToCompany+'</font>'+
				'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><font color="blue">'+data.expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
				'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
				'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button" value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
		    }
		    //for accountant can be same user or not
				if(data.createdBy==useremail) {
					$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"]').remove();
			    	if(data.claimTxnStatus=="Payment Due To Staff" || data.claimTxnStatus=="Payment Due From Staff" || data.claimTxnStatus=="No Due For Settlement"){
						var tempHtml = '<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
			    		'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
			    		'<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
			    		'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
			    		'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Advance Settled :</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Due From Company:</b><br/><font color="blue">'+data.dueFromCompany+'</font><br/><b>Due To Company:</b><br/><font color="blue">'+data.dueToCompany+'</font><br/><b>Amount Returned In Case Of Due To Company:</b><br/><font color="blue">'+data.amountReturnInCaseOfDueToCompany+'</font>'+
			    		'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><font color="blue">'+data.expenseAdvancepurposeOfExpenseAdvance+'</font></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><div class="claimpayementDiv"><br/>';
						if(data.claimTxnStatus == "Payment Due From Staff"){
							tempHtml+='Select mode of payment:<select name="claimpaymentDetails" id="claimpaymentDetails" onchange="listAllBranchBankAccountsClaims(this);">'+
							'<option value="1">CASH</option><option value="2">BANK</option></select><br/>Input payment details:<textarea name="bankDetails" id="bankDetails"></textarea>';
						}
						tempHtml+='</div><br/><input type="button" value="Complete Accounting" id="settleExpenseAdvanceTxn" class="btn btn-submit btn-idos" onclick="expenseAdvanceSettlementAccounting(this)" style="margin-top:10px;width:170px;margin-left: 0px;"><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
			    		'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>';
						$("#claimDetailsTable").prepend(tempHtml);
					}else{
						$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
					    '<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
					    '<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
					    '</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
					    '<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Advance Settled :</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Due From Company:</b><br/><font color="blue">'+data.dueFromCompany+'</font><br/><b>Due To Company:</b><br/><font color="blue">'+data.dueToCompany+'</font><br/><b>Amount Returned In Case Of Due To Company:</b><br/><font color="blue">'+data.amountReturnInCaseOfDueToCompany+'</font>'+
					    '</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
					    '<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
					}
			}
			 $('.claimCommonTable:visible').slideUp();
			    //logic for separation transaction travel eligibility separately into travel eligibility label and travel eligibility figure
			    if(data.claimuserAdvanveEligibility!=null && data.claimuserAdvanveEligibility!=""){
				   	var individualclaimuserAdvanveEligibility=data.claimuserAdvanveEligibility.substring(0,data.claimuserAdvanveEligibility.length).split('#');
				   	for(var m=0;m<individualclaimuserAdvanveEligibility.length;m++){
				    	if(m>0){
				    		var labelAndValue=individualclaimuserAdvanveEligibility[m].substring(0, individualclaimuserAdvanveEligibility[m].length).split(':');
				    		$('#claimsTransactionEntity'+data.id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></font>:<br/>');
				    		if(typeof labelAndValue[1]!='undefined'){
					    		$('#claimsTransactionEntity'+data.id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<font color="blue">'+labelAndValue[1]+'</font><br/>');
					    	}
				    	}
				    }
				}
			    //logic for separation transaction advance eligibility separately into advance eligibility label and advance eligibility figure
			    if(data.claimuserAdvanveEligibility!=null && data.claimuserAdvanveEligibility!=""){
				   	var individualclaimuserAdvanveEligibility=data.claimuserAdvanveEligibility.substring(0,data.claimuserAdvanveEligibility.length).split('#');
				   	for(var m=0;m<individualclaimuserAdvanveEligibility.length;m++){
				   		if(m>0){
				    		var labelAndValue=individualclaimuserAdvanveEligibility[m].substring(0, individualclaimuserAdvanveEligibility[m].length).split(':');
				    		$('#claimsTransactionEntity'+data.id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></font>:<br/>');
				    		if(typeof labelAndValue[1]!='undefined'){
					    		$('#claimsTransactionEntity'+data.id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<font color="blue">'+labelAndValue[1]+'</font><br/>');
					    	}
				    	}
				    }
				}
			  //logic for separation transaction remarks separately into useremail and remarks made by them
			    if(data.claimtxnRemarks!=null && data.claimtxnRemarks!=""){
				   	var individualRemarks=data.claimtxnRemarks.substring(0,data.claimtxnRemarks.length).split(',');
				   	for(var m=0;m<individualRemarks.length;m++){
				    	var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
				    	$('#claimsTransactionEntity'+data.id+' div[class="claimtxnWorkflowRemarks"]').append('<font color="#FFOFF"><b>'+emailAndRemarks[0]+'</b></font>#');
				    	if(typeof emailAndRemarks[1]!='undefined'){
				    		$('#claimsTransactionEntity'+data.id+' div[class="claimtxnWorkflowRemarks"]').append('<font color="blue">'+emailAndRemarks[1]+'</font><br/>');
				    	}
				    }
				}
			  //logic for separation transaction remarks separately into useremail and documents uploaded by them
			    if(data.claimsupportingDoc!="" && data.claimsupportingDoc!=null){
			    	var txndocument=data.claimsupportingDoc;
		    		var transTrID = 'claimsTransactionEntity'+data.id;
		    		fillSelectElementWithUploadedDocs(txndocument, transTrID, 'txnViewListUpload');
		    	}
			    if(data.role.includes("CREATOR") || data.role.includes("ACCOUNTANT")){
			    	var locHash=window.location.hash;
			    	if(locHash=="" || locHash=="#claimSetup"){
					    location.hash="#claimSetup";
			    	}
			    }
			    getCashBankReceivablePayable();
			    if(data.role.includes("ACCOUNTANT")){
			   		getTravelExpenseClaimCountForApproverAccountant(useremail);
			    }
			  //Edit transaction common for all claims type
				if(data.claimTxnStatus=='Require Approval' && data.createdBy==useremail){
					$("#claimDetailsTable tr[id='claimsTransactionEntity"+data.id+"'] td:nth-child(7) div:first").append('<br/><input type="button" value="Edit Transaction" id="editClaimAllowed" class="editClaimAllowed btn btn-submit btn-idos" onclick="editClaimsOnceAllowed(this)" style="margin-top:10px;margin-left: 0px;">');
				}
				else if((data.txnPurposeId=='18' || data.txnPurposeId=='16') && data.claimTxnStatus!='Accounted' && data.createdBy==useremail){
					$("#claimDetailsTable tr[id='claimsTransactionEntity"+data.id+"'] td:nth-child(7) div:first").append('<br/><input type="button" value="Edit Transaction" id="editClaimAllowed" class="editClaimAllowed btn btn-submit btn-idos" onclick="editClaimsOnceAllowed(this)" style="margin-top:10px;margin-left: 0px;">');
				}
		} else if(data.txnType=="expenseAdvanceTxn"){
			var useremail=data.useremail;
		    $("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"]').remove();
		    //for creator claim detail data display logic
		    if(data.createdBy==useremail){
		    	//now based of claim transaction status display the claim txn row
		    	if(data.claimTxnStatus=='Require Clarification'){
		    		//when approver send a request toclarify the claim transaction created by creator
		    		$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
		    		'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
		    		'<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
		    		'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
		    		'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Entered Advance:</b><br/><b>Total Advance:</b><br/><font color="blue">'+data.expenseAdvanceTotalAdvanceAmount+'</font>'+
		    		'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><font color="blue">'+data.expenseAdvancepurposeOfExpenseAdvance+'</font></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font><br/><input type="button" value="Clarify Transaction" id="clarifyTxn" class="btn btn-submit btn-idos" onclick="advanceExpenseClarifyTransaction(this)" style="margin-top:10px;margin-left: 0px;width:170px;"></div></td><td><div class="rowToExpand"><select name="txnViewListUpload" id="claimfileDownload" ><option value="">-Please Select-</option></select>'+
					'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td><td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
		    	}else{
		    		$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
		    		'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
		    		'<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
		    		'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
		    		'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Total Advance:</b><br/><font color="blue">'+data.expenseAdvanceTotalAdvanceAmount+'</font>'+
		    		'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
					'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
		    		'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
		    	}
		    }
		    //for approver can be same user or not
		    if(data.approverEmails!=null){
		    	if(data.approverEmails.indexOf(useremail)!=-1){
		    		$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"]').remove();
					if(data.claimTxnStatus=='Require Approval' || data.claimTxnStatus=='Clarified'){
						$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
					    '<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
					    '<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
					    '</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
					    '<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Total Advance:</b><br/><font color="blue">'+data.expenseAdvanceTotalAdvanceAmount+'</font>'+
					    '</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><font color="blue">'+data.expenseAdvancepurposeOfExpenseAdvance+'</font></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><br/>Action:<select class="claimapproverActionList" name="claimapproverActionList" id="claimapproverActionList"><option value="">--Please Select--</option>'+
						'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/><span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
						'<input type="button" id="claimapproverAction" class="btn btn-submit btn-center" value="Submit" onclick="advanceExpenseCompleteAction(this)"><br/><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
					    '<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 195px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>');
					}else{
						$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
					    '<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
					    '<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
					    '</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
					    '<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Total Advance:</b><br/><font color="blue">'+data.expenseAdvanceTotalAdvanceAmount+'</font>'+
					    '</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><br/>Supporting Doc:<select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
					    '<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
					}
		    	}
		    }
		    if(data.role.includes("ACCOUNTANT") || data.role.includes("AUDITOR") || data.role.includes("CONTROLLER")){
		    	 var approverEmailVal=false;var selectedAdditionalApproval=false;
	    		 if(data.approverEmails!=null && data.approverEmails!=""){
	    			 if(data.approverEmails.indexOf(useremail)!=-1){
	    				 approverEmailVal=true;
	    			 }
	    		 }
	    		 if(data.selectedAdditionalApproval!=null && data.selectedAdditionalApproval!=""){
					 if(data.selectedAdditionalApproval==useremail){
					     selectedAdditionalApproval=true;
					 }
	    		 }
	    		 if(data.createdBy!=useremail && approverEmailVal==false && selectedAdditionalApproval==false){
	    			 $("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
	    			 '<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
	    			 '<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
	    			 '</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
	    			 '<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Total Advance:</b><br/><font color="blue">'+data.expenseAdvanceTotalAdvanceAmount+'</font>'+
	    			 '</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
	    			 '<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
	    		 }
		    }
		    //for accountant can be same user or not
			if(data.claimTxnStatus=="Approved"){
					if(data.createdBy==useremail) {
						$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"]').remove();
						if(data.claimTxnStatus=="Approved"){
							$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
							'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
							'<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
							'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
							'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Total Advance:</b><br/><font color="blue">'+data.expenseAdvanceTotalAdvanceAmount+'</font>'+
							'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><font color="blue">'+data.expenseAdvancepurposeOfExpenseAdvance+'</font></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div>'+
							'<br/><input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit btn-idos" onclick="advanceExpenseCompleteAccounting(this)" style="margin-top:10px;margin-left: 0px;"><br/><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
							'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
							'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
						}else{
							$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
							'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
							'<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
							'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
							'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Total Advance:</b><br/><font color="blue">'+data.expenseAdvanceTotalAdvanceAmount+'</font>'+
							'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
							'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
							'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
						}
			    }
			}
			if(data.selectedAdditionalApproval==useremail){
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"]').remove();
				if(data.claimTxnStatus=='Require Additional Approval'){
					$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
					'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
					'<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
					'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
					'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Total Advance:</b><br/><font color="blue">'+data.expenseAdvanceTotalAdvanceAmount+'</font>'+
					'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><font color="blue">'+data.expenseAdvancepurposeOfExpenseAdvance+'</font></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><br/>Action:<select class="claimapproverActionList" name="claimapproverActionList" id="claimapproverActionList"><option value="">--Please Select--</option>'+
					'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/><span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
					'<input type="button" id="claimapproverAction" class="btn btn-submit btn-center" value="Submit" onclick="advanceExpenseCompleteAction(this)"><br/><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
					'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
					'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
				}else{
					$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
					'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
					'<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
					'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
					'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font><br/><b>Total Advance:</b><br/><font color="blue">'+data.expenseAdvanceTotalAdvanceAmount+'</font>'+
					'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><br/>Supporting Doc:<select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
					'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
					'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
				}
		    }else{
		    	if(data.status=='Require Additional Approval'){
		    		$("#claimDetailsTable tr[id='claimsTransactionEntity"+data.id+"'] td:nth-child(8) div:first").append('<br/><b>Additional Approval Required By:</b><br/>'+data.selectedAdditionalApproval+'');
		    	}
		    }
		    $('.claimCommonTable:visible').slideUp();
		    //logic for separation transaction travel eligibility separately into travel eligibility label and travel eligibility figure
		    if(data.claimuserAdvanveEligibility!=null && data.claimuserAdvanveEligibility!=""){
			   	var individualclaimuserAdvanveEligibility=data.claimuserAdvanveEligibility.substring(0,data.claimuserAdvanveEligibility.length).split('#');
			   	for(var m=0;m<individualclaimuserAdvanveEligibility.length;m++){
			    	if(m>0){
			    		var labelAndValue=individualclaimuserAdvanveEligibility[m].substring(0, individualclaimuserAdvanveEligibility[m].length).split(':');
			    		$('#claimsTransactionEntity'+data.id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></font>:<br/>');
			    		if(typeof labelAndValue[1]!='undefined'){
				    		$('#claimsTransactionEntity'+data.id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<font color="blue">'+labelAndValue[1]+'</font><br/>');
				    	}
			    	}
			    }
			}
		    //logic for separation transaction advance eligibility separately into advance eligibility label and advance eligibility figure
		    if(data.claimuserAdvanveEligibility!=null && data.claimuserAdvanveEligibility!=""){
			   	var individualclaimuserAdvanveEligibility=data.claimuserAdvanveEligibility.substring(0,data.claimuserAdvanveEligibility.length).split('#');
			   	for(var m=0;m<individualclaimuserAdvanveEligibility.length;m++){
			   		if(m>0){
			    		var labelAndValue=individualclaimuserAdvanveEligibility[m].substring(0, individualclaimuserAdvanveEligibility[m].length).split(':');
			    		$('#claimsTransactionEntity'+data.id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></font>:<br/>');
			    		if(typeof labelAndValue[1]!='undefined'){
				    		$('#claimsTransactionEntity'+data.id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<font color="blue">'+labelAndValue[1]+'</font><br/>');
				    	}
			    	}
			    }
			}
		    //logic for separation transaction remarks separately into useremail and remarks made by them
		    if(data.claimtxnRemarks!=null && data.claimtxnRemarks!=""){
			   	var individualRemarks=data.claimtxnRemarks.substring(0,data.claimtxnRemarks.length).split(',');
			   	for(var m=0;m<individualRemarks.length;m++){
			    	var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
			    	$('#claimsTransactionEntity'+data.id+' div[class="claimtxnWorkflowRemarks"]').append('<font color="#FFOFF"><b>'+emailAndRemarks[0]+'</b></font>#');
			    	if(typeof emailAndRemarks[1]!='undefined'){
			    		$('#claimsTransactionEntity'+data.id+' div[class="claimtxnWorkflowRemarks"]').append('<font color="blue">'+emailAndRemarks[1]+'</font><br/>');
			    	}
			    }
			}
		  //logic for separation transaction remarks separately into useremail and documents uploaded by them
		    if(data.claimsupportingDoc!="" && data.claimsupportingDoc!=null){
		    	var txndocument=data.claimsupportingDoc;
	    		var transTrID = 'claimsTransactionEntity'+data.id;
	    		fillSelectElementWithUploadedDocs(txndocument, transTrID, 'txnViewListUpload');
	    	}
		    if(data.txnSpecialStatus=="Rules Not Followed"){
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] b[class="txnSpecialStatus"]').html("");
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] b[class="txnSpecialStatus"]').append('<img src="assets/images/green.png"></img>');
		    }
		    if(data.claimTxnStatus!="" && data.claimTxnStatus=="Rejected"){
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstatred');
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstatred');
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstatred');
		    }
		    if(data.claimTxnStatus!="" && data.claimTxnStatus=="Accounted"){
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstat');
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstat');
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstat');
		    }
		    if(data.claimTxnStatus!="" && (data.claimTxnStatus=="Approved" || data.claimTxnStatus=="Require Approval" || data.claimTxnStatus=="Require Additional Approval") || data.claimTxnStatus=="Require Accounting"){
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstatgreen');
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstatgreen');
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstatgreen');
		    }
		    if(data.additionalApprovarUsers!=null){
			    if(typeof data.additionalApprovarUsers!='undefined'){
			    	var additionalApprovarUsersList=data.additionalApprovarUsers.substring(0,data.additionalApprovarUsers.length).split(',');
			    	$("tr[id='claimsTransactionEntity"+data.id+"'] select[id='userAddApproval']").append('<option value="">--Please Select--</option>');
			    	for(var i=0;i<additionalApprovarUsersList.length;i++){
			    		$("tr[id='claimsTransactionEntity"+data.id+"'] select[id='userAddApproval']").append('<option value="'+additionalApprovarUsersList[i]+'">'+additionalApprovarUsersList[i]+'</option>');
			    	}
			    }
			}
		    if(data.role.includes("CREATOR") || data.role.includes("APPROVER") || data.role.includes("ACCOUNTANT") || data.role.includes("AUDITOR")){
				var locHash=window.location.hash;
				if(locHash=="" || locHash=="#claimSetup"){
					location.hash="#claimSetup";
				}
		    }
		    getCashBankReceivablePayable();
		    if(data.role.includes("APPROVER") || data.role.includes("ACCOUNTANT")){
				getTravelExpenseClaimCountForApproverAccountant(useremail);
		    }
		  //Edit transaction common for all claims type
			if(data.claimTxnStatus=='Require Approval' && data.createdBy==useremail){
				$("#claimDetailsTable tr[id='claimsTransactionEntity"+data.id+"'] td:nth-child(7) div:first").append('<br/><input type="button" value="Edit Transaction" id="editClaimAllowed" class="editClaimAllowed btn btn-submit btn-idos" onclick="editClaimsOnceAllowed(this)" style="margin-top:10px;margin-left: 0px;">');
			}
			else if((data.txnPurposeId=='18' || data.txnPurposeId=='16') && data.claimTxnStatus!='Accounted' && data.createdBy==useremail){
				$("#claimDetailsTable tr[id='claimsTransactionEntity"+data.id+"'] td:nth-child(7) div:first").append('<br/><input type="button" value="Edit Transaction" id="editClaimAllowed" class="editClaimAllowed btn btn-submit btn-idos" onclick="editClaimsOnceAllowed(this)" style="margin-top:10px;margin-left: 0px;">');
			}
		} else if(data.txnType=="expReimbursementTxn"){
			var useremail=data.useremail;
		    $("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"]').remove();
		    //for creator claim detail data display logic
		    if(data.createdBy==useremail){
		    	//now based of claim transaction status display the claim txn row
		    	if(data.claimTxnStatus=='Require Clarification'){
		    		//when approver send a request toclarify the claim transaction created by creator
		    		$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
		    		'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
		    		'<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
		    		'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
		    		'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Reimbursement Amount:</b><font color="blue">'+data.claimenteredAdvance+'</font>'+
		    		'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Reimbursement:</b><br/><font color="blue">'+data.expenseAdvancepurposeOfExpenseAdvance+'</font></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font><br/><input type="button" value="Clarify Transaction" id="clarifyTxn" class="btn btn-submit btn-idos" onclick="reimbursementClarifyTransaction(this)" style="margin-top:10px;margin-left: 0px;width:170px;"></div></td><td><div class="rowToExpand"><select name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
					'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td><td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveReimbursementTxnRemarks(this)"></div></div></td></tr>');
		    	}else{
		    		$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
		    		'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
		    		'<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
		    		'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
		    		'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Reimbursement Amount:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font>'+
		    		'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Reimbursement:</b><br/><p style="color: blue;">'+data.expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><br/>Supporting Doc:<select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
					'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
		    		'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveReimbursementTxnRemarks(this)"></div></div></td></tr>');
		    	}
		    }
		    //for approver can be same user or not
		    if(data.approverEmails!=null){
		    	if(data.approverEmails.indexOf(useremail)!=-1){
		    		$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"]').remove();
					if(data.claimTxnStatus=='Require Approval' || data.claimTxnStatus=='Clarified'){
						$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
					    '<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
					    '<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
					    '</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
					    '<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Reimbursement Amount:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font>'+
					    '</div></div></td><td><div class="rowToExpand"><b>Purpose Of Reimbursement:</b><br/><font color="blue">'+data.expenseAdvancepurposeOfExpenseAdvance+'</font></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><br/>Action:<select class="claimapproverActionList" name="claimapproverActionList" id="claimapproverActionList"><option value="">--Please Select--</option>'+
						'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/><span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
						'<input type="button" id="claimapproverAction" class="btn btn-submit btn-center" value="Submit" onclick="reimbursementCompleteAction(this)"><br/><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
					    '<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 195px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>');
					}else{
						$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
					    '<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
					    '<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
					    '</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
					    '<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Reimbursement Amount:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font>'+
					    '</div></div></td><td><div class="rowToExpand"><b>Purpose Of Reimbursement:</b><br/><p style="color: blue;">'+data.expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
					    '<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveReimbursementTxnRemarks(this)"></div></div></td></tr>');
					}
		    	}
		    }
		    if(data.role.includes("ACCOUNTANT") || data.role.includes("AUDITOR") || data.role.includes("CONTROLLER")){
		    	 var approverEmailVal=false;var selectedAdditionalApproval=false;
	    		 if(data.approverEmails!=null && data.approverEmails!=""){
	    			 if(data.approverEmails.indexOf(useremail)!=-1){
	    				 approverEmailVal=true;
	    			 }
	    		 }
	    		 if(data.selectedAdditionalApproval!=null && data.selectedAdditionalApproval!=""){
					    if(data.selectedAdditionalApproval==useremail){
					    	selectedAdditionalApproval=true;
					    }
	    		 }
	    		 if(data.createdBy!=useremail && approverEmailVal==false && selectedAdditionalApproval==false){
	    			 $("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
	    			 '<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
	    			 '<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
	    			 '</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
	    			 '<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Reimbursement Amount:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font>'+
	    			 '</div></div></td><td><div class="rowToExpand"><b>Purpose Of Reimbursement:</b><br/><p style="color: blue;">'+data.expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
				     '<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
	    			 '<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveReimbursementTxnRemarks(this)"></div></div></td></tr>');
	    		 }
		    }
		    //for accountant can be same user or not
			if(data.claimTxnStatus=="Approved"){
				if(data.createdBy==useremail) {
						$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"]').remove();
						if(data.claimTxnStatus=="Approved"){
							$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
							'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
							'<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
							'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
							'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Reimbursement Amount:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font>'+
							'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Reimbursement:</b><br/><font color="blue">'+data.expenseAdvancepurposeOfExpenseAdvance+'</font></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div>'+
							'<br/><input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit btn-idos" onclick="reimbursementCompleteAccounting(this)" style="margin-top:10px;margin-left: 0px;"><br/><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
							'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
							'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveReimbursementTxnRemarks(this)"></div></div></td></tr>');
						}else{
							$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
							'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
							'<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
							'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
							'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Reimbursement Amount:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font>'+
							'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Reimbursement:</b><br/><p style="color: blue;">'+data.expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
							'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
							'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveReimbursementTxnRemarks(this)"></div></div></td></tr>');
						}
					}
			}
			 if(data.selectedAdditionalApproval==useremail){
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"]').remove();
					if(data.claimTxnStatus=='Require Additional Approval'){
						$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
						'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
						'<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
						'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Reimbursement Amount:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font>'+
						'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Reimbursement:</b><br/><font color="blue">'+data.expenseAdvancepurposeOfExpenseAdvance+'</font></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><br/>Action:<select class="claimapproverActionList" name="claimapproverActionList" id="claimapproverActionList"><option value="">--Please Select--</option>'+
						'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/><span class="userForAdditionalApprovalClass">User List:</span><br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
						'<input type="button" id="claimapproverAction" class="btn btn-submit btn-center" value="Submit" onclick="advanceExpenseCompleteAction(this)"><br/><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand"><select name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveReimbursementTxnRemarks(this)"></div></div></td></tr>');
					}else{
						$("#claimDetailsTable").prepend('<tr id="claimsTransactionEntity'+data.id+'" txnref="'+data.claimTxnRefNo+'"><td><div class="rowToExpand"><font color="blue">'+data.branchName+'</font><br/><b>PROJECT:</b><br/><font color="blue">'+data.projectName+'</font><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>'+data.creatorLabel+'</b><br/><font color="blue">'+data.createdBy+'</font></div></td>'+
						'<td><div class="rowToExpand"><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><font color="blue">'+data.txnQuestionName+'</font><br/><b>Item Name:</b><p class="txnItemDesc">'+data.itemName+'</p>'+
						'<br/><b>Item Particular Name:</b><br/><font color="blue">'+data.itemParticularName+'</font><br/><b>Parent Specifics:</b><br/><font color="blue">'+data.parentSpecificName+'</font>'+
						'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						'<td><div class="rowToExpand"><font color="blue">'+data.transactionDate+'</font></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Reimbursement Amount:</b><br/><font color="blue">'+data.claimenteredAdvance+'</font>'+
						'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Reimbursement:</b><br/><p style="color: blue;">'+data.expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.approverLabel+'</b><br/><font color="blue">'+data.approvedBy+'</font><br/><b>'+data.accountedLabel+'</b><br/><font color="blue">'+data.accountedBy+'</font></div></td><td><div class="rowToExpand">Supporting Doc:<select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload"><option value="">-Please Select-</option></select>'+
						'<button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea style="width:178px;" rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveReimbursementTxnRemarks(this)"></div></div></td></tr>');
					}
			    }else{
			    	if(data.status=='Require Additional Approval'){
			    		$("#claimDetailsTable tr[id='claimsTransactionEntity"+data.id+"'] td:nth-child(8) div:first").append('<br/><b>Additional Approval Required By:</b><br/>'+data.selectedAdditionalApproval+'');
			    	}
			    }
			$('.claimCommonTable:visible').slideUp();
		    //logic for separation transaction travel eligibility separately into travel eligibility label and travel eligibility figure
		    if(data.claimuserAdvanveEligibility!=null && data.claimuserAdvanveEligibility!=""){
			   	var individualclaimuserAdvanveEligibility=data.claimuserAdvanveEligibility.substring(0,data.claimuserAdvanveEligibility.length).split('#');
			   	for(var m=0;m<individualclaimuserAdvanveEligibility.length;m++){
			    	if(m>0){
			    		var labelAndValue=individualclaimuserAdvanveEligibility[m].substring(0, individualclaimuserAdvanveEligibility[m].length).split(':');
			    		$('#claimsTransactionEntity'+data.id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></font>:<br/>');
			    		if(typeof labelAndValue[1]!='undefined'){
				    		$('#claimsTransactionEntity'+data.id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<font color="blue">'+labelAndValue[1]+'</font><br/>');
				    	}
			    	}
			    }
			}
		    //logic for separation transaction advance eligibility separately into advance eligibility label and advance eligibility figure
		    if(data.claimuserAdvanveEligibility!=null && data.claimuserAdvanveEligibility!=""){
			   	var individualclaimuserAdvanveEligibility=data.claimuserAdvanveEligibility.substring(0,data.claimuserAdvanveEligibility.length).split('#');
			   	for(var m=0;m<individualclaimuserAdvanveEligibility.length;m++){
			   		if(m>0){
			    		var labelAndValue=individualclaimuserAdvanveEligibility[m].substring(0, individualclaimuserAdvanveEligibility[m].length).split(':');
			    		$('#claimsTransactionEntity'+data.id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></font>:<br/>');
			    		if(typeof labelAndValue[1]!='undefined'){
				    		$('#claimsTransactionEntity'+data.id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<font color="blue">'+labelAndValue[1]+'</font><br/>');
				    	}
			    	}
			    }
			}
		    //logic for separation transaction remarks separately into useremail and remarks made by them
		    if(data.claimtxnRemarks!=null && data.claimtxnRemarks!=""){
			   	var individualRemarks=data.claimtxnRemarks.substring(0,data.claimtxnRemarks.length).split(',');
			   	for(var m=0;m<individualRemarks.length;m++){
			    	var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
			    	$('#claimsTransactionEntity'+data.id+' div[class="claimtxnWorkflowRemarks"]').append('<font color="#FFOFF"><b>'+emailAndRemarks[0]+'</b></font>#');
			    	if(typeof emailAndRemarks[1]!='undefined'){
			    		$('#claimsTransactionEntity'+data.id+' div[class="claimtxnWorkflowRemarks"]').append('<font color="blue">'+emailAndRemarks[1]+'</font><br/>');
			    	}
			    }
			}
		  //logic for separation transaction remarks separately into useremail and documents uploaded by them
		    if(data.claimsupportingDoc!="" && data.claimsupportingDoc!=null){
		    	var txndocument=data.claimsupportingDoc;
	    		var transTrID = 'claimsTransactionEntity'+data.id;
	    		fillSelectElementWithUploadedDocs(txndocument, transTrID, 'txnViewListUpload');
	    	}
		    if(data.txnSpecialStatus=="Rules Not Followed"){
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] b[class="txnSpecialStatus"]').html("");
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] b[class="txnSpecialStatus"]').append('<img src="assets/images/green.png"></img>');
		    }
		    if(data.claimTxnStatus!="" && data.claimTxnStatus=="Rejected"){
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstatred');
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstatred');
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstatred');
		    }
		    if(data.claimTxnStatus!="" && data.claimTxnStatus=="Accounted"){
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstat');
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstat');
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstat');
		    }
		    if(data.claimTxnStatus!="" && (data.claimTxnStatus=="Approved" || data.claimTxnStatus=="Require Approval" || data.claimTxnStatus=="Require Additional Approval") || data.claimTxnStatus=="Require Accounting"){
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstatgreen');
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstatgreen');
		    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstatgreen');
		    }
		    if(data.additionalApprovarUsers!=null){
			    if(typeof data.additionalApprovarUsers!='undefined'){
			    	var additionalApprovarUsersList=data.additionalApprovarUsers.substring(0,data.additionalApprovarUsers.length).split(',');
			    	$("tr[id='claimsTransactionEntity"+data.id+"'] select[id='userAddApproval']").append('<option value="">--Please Select--</option>');
			    	for(var i=0;i<additionalApprovarUsersList.length;i++){
			    		$("tr[id='claimsTransactionEntity"+data.id+"'] select[id='userAddApproval']").append('<option value="'+additionalApprovarUsersList[i]+'">'+additionalApprovarUsersList[i]+'</option>');
			    	}
			    }
			}
		    if(data.role.includes("CREATOR") || data.role.includes("APPROVER") || data.role.includes("ACCOUNTANT") || data.role.includes("AUDITOR")){
				var locHash=window.location.hash;
				if(locHash=="" || locHash=="#claimSetup"){
					location.hash="#claimSetup";
				}
		    }
		    getCashBankReceivablePayable();
		  //Edit transaction common for all claims type
			if(data.claimTxnStatus=='Require Approval' && data.createdBy==useremail){
				$("#claimDetailsTable tr[id='claimsTransactionEntity"+data.id+"'] td:nth-child(7) div:first").append('<br/><input type="button" value="Edit Transaction" id="editClaimAllowed" class="editClaimAllowed btn btn-submit btn-idos" onclick="editClaimsOnceAllowed(this)" style="margin-top:10px;margin-left: 0px;">');
			}
			else if((data.txnPurposeId=='18' || data.txnPurposeId=='16') && data.claimTxnStatus!='Accounted' && data.createdBy==useremail){
				$("#claimDetailsTable tr[id='claimsTransactionEntity"+data.id+"'] td:nth-child(7) div:first").append('<br/><input type="button" value="Edit Transaction" id="editClaimAllowed" class="editClaimAllowed btn btn-submit btn-idos" onclick="editClaimsOnceAllowed(this)" style="margin-top:10px;margin-left: 0px;">');
			}
		} // end transaction and claim conditions if
		setTransactionString();
		setClaimsTransactionString();
		$.unblockUI();
}

function completeAction(elem){
	let parentTr = $(elem).closest('tr').attr('id');
    var err=approverAction(parentTr);
	if(err==true){
		return true;
	}
}
function claimcompleteAction(elem){
	var parentTr = $(elem).closest('tr').attr('id');
    var err=claimapproverAction(parentTr);
	if(err==true){
		return true;
	}
}
function completePovisionAction(elem){
	var parentTr = $(elem).closest('tr').attr('id');
    var err=provisionapproverAction(parentTr);
	if(err==true){
		return true;
	}
}
function completePayrollAction(elem){ //wrflow
	var parentTr = $(elem).closest('tr').attr('id');
	
    var err=payrollApproverAction(parentTr);
	if(err==true){
		return true;
	}
}
function advanceExpenseCompleteAction(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	
    var err=advanceExpenseApproverAction(parentTr);
	if(err==true){
		return true;
	}
}
function reimbursementCompleteAction(elem){ //wrflow
	var parentTr = $(elem).closest('tr').attr('id');  //Sunil
	
	var err=reimbursementApproverAction(parentTr);
	if(err==true){
		return true;
	}
}
function doAction(elem) { //wfflow
	
    var $elem=$(elem);
    var err=performAction($elem);
    if(err==true){
		return true;
	}
}
