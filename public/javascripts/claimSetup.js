/*Claim Search Starts*/
$(document).ready(function(){
	$('#searchClaimCriteriaBased').on('click',function(){
		//alert("searchClaimCriteriaBased") ;
		var json={};
		json.email=$("#hiddenuseremail").text();
		json.txnRefNumber=$("#claimTransactionNumber").val();
		json.txnType=$("#claimSearchParticular").val();
		json.item=$("#claimSearchSpecific").val();
		json.status=$("#claimSearchTransStatus").val();
		json.claimType=$("#claimSearchType").val();
		json.payMode=$("#claimSearchPayMode").val();
		json.travelMode=$("#claimSearchTravelMode option:selected").val();
		json.accomodationMode=$("#claimSearchAccType option:selected").val();
		json.fromDate=$("#claimSearchFromDate").val();
		json.toDate=$("#claimSearchToDate").val();
		json.branch=$("#claimSearchBranch").val();
		json.project=$("#claimSearchProject").val();
		json.fromAmount=$("#claimSearchFromAmt").val();
		json.toAmount=$("#claimSearchToDate").val();
		json.remarks=$("#claimSearchRemarks").val();
		json.documents=$("#claimSearchSupportDoc").val();
		json.claimSearchTxnQuestion=$("#claimSearchTxnQuestion").val();
		json.claimSearchUserType=$("#claimSearchUserType").val();
		ajaxCall('/claims/search', json, '', '', '', '', 'getClaimsTransactions1Success', '', true);
		$('#claimsTable').show();
	//		ajaxCall('/claims/search', json, '', '', '', '', 'getClaimsTransactionsSuccess', '', false);
	});
});

function getClaimsTransactions1Success(data) {
	//alert("getClaimsTransactions1Success") ;
	if(data.result){
		getClaimsTransactionsSuccess(data);
	}
}
/*Claim Search Ends*/


function getClaimsTransactions(limit){
	//alert("Start getClaimsTransactions"); //sunil
	$('#claimDetailsTable tbody').empty();
	var jsonData={};
	var useremail=$("#hiddenuseremail").text();
	jsonData.email=useremail;
	jsonData.limit=limit;
	ajaxCall('user/userClaimsTransactions', jsonData, '', '', '', '', 'getClaimsTransactionsSuccess', '', true);

	//alert("End getClaimsTransactions");
}

function getClaimsTransactionsSuccess(data){
	//alert("getClaimsTransactionsSuccess")
	if(data.result){
		claimTxnMsg=data.approval;
		claimTxnMsg+='<br>'+data.approved;
		var role=data.userClaimTxnData[0].userroles;
	/*	if(role.indexOf("CREATOR")!=-1 || role.indexOf("APPROVER")!=-1 || role.indexOf("AUDITOR")!=-1 || role.indexOf("ACCOUNTANT")!=-1){
			if(!isClaimTxnShow){
				$.growl.notice({title: "Claim Transaction Status", message: claimTxnMsg, duration : 15000});
				isClaimTxnShow=true;
			}
		}*/
		for(var i=0;i<data.userClaimTxnData.length;i++){
			if(data.userClaimTxnData[i].txnPurposeId=='15'){
				var useremail=$("#hiddenuseremail").text();
				var role=data.userClaimTxnData[i].userroles;
			 $("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
			    //for creator claim detail data display logic
			    if(data.userClaimTxnData[i].createdBy==useremail){
			    	//now based of claim transaction status display the claim txn row
			    	if(data.userClaimTxnData[i].claimTxnStatus=='Require Clarification'){
			    		//when approver send a request toclarify the claim transaction created by creator
			    		$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
			    		'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
			    		'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
			    		'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Tot02-02-201602-02-2016al Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
			    		'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimexistingAdvance+'</p><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><p style="color: blue;">'+data.userClaimTxnData[i].claimadjustedAdvance+'</p><br/><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalAdvance+'</p>'+
			    		'</div></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p><br/><input type="button" value="Clarify Transaction" id="clarifyTxn" class="btn btn-submit btn-idos" onclick="claimclarifyTransaction(this)" style="margin-top:10px;margin-left: 0px;width:170px;"></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
			    		'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>');
			    	}else{
			    		$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
			    		'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
			    		'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
			    		'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
			    		'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td>'+
						'<td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimexistingAdvance+'</p><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><p style="color: blue;">'+data.userClaimTxnData[i].claimadjustedAdvance+'</p><br/><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalAdvance+'</p></div></div></td>'+
						'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'<br>'+data.userClaimTxnData[i].paymentMode+'<br>'+data.userClaimTxnData[i].instrumentNumber+'<br>'+data.userClaimTxnData[i].instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
					    '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
			    		'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
			    	}
			    }
			    //for approver can be same user or not
			    if(data.userClaimTxnData[i].approverEmails!=null){
			    	if(data.userClaimTxnData[i].approverEmails.indexOf(useremail)!=-1){
			    		$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
						if(data.userClaimTxnData[i].claimTxnStatus=='Require Approval' || data.userClaimTxnData[i].claimTxnStatus=='Clarified'){
							$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
						    '<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
						    '<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
						    '</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						    '<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimexistingAdvance+'</p><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><p style="color: blue;">'+data.userClaimTxnData[i].claimadjustedAdvance+'</p><br/><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalAdvance+'</p>'+
						    '</div></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><br/>Action:<select class="claimapproverActionList" name="claimapproverActionList" id="claimapproverActionList"><option value="">--Please Select--</option>'+
							'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>User List:<br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
							'<input type="button" id="claimapproverAction" class="btn btn-submit btn-center" value="Submit" onclick="claimcompleteAction(this)"><br/><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
							'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						    '<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>');
						}else{
							$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
						    '<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
						    '<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
						    '</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						    '<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimexistingAdvance+'</p><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><p style="color: blue;">'+data.claimadjustedAdvance+'</p><br/><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalAdvance+'</p>'+
						    '</div></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'<br>'+data.userClaimTxnData[i].paymentMode+'<br>'+data.userClaimTxnData[i].instrumentNumber+'<br>'+data.userClaimTxnData[i].instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
						    '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						    '<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
						}
			    	}
			    }
			    if(data.userClaimTxnData[i].selectedAdditionalApproval==useremail){
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
					if(data.userClaimTxnData[i].claimTxnStatus=='Require Additional Approval'){
						$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
						'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
						'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
						'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimexistingAdvance+'</p><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><p style="color: blue;">'+data.userClaimTxnData[i].claimadjustedAdvance+'</p><br/><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalAdvance+'</p>'+
						'</div></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><br/>Action:<select class="claimapproverActionList" name="claimapproverActionList" id="claimapproverActionList"><option value="">--Please Select--</option>'+
						'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>User List:<br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
						'<input type="button" id="claimapproverAction" class="btn btn-submit btn-center" value="Submit" onclick="claimcompleteAction(this)"><br/><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select  class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
						'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
					}else{
						$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
						'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
						'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
						'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><p style="color: blue;">'+data.claimexistingAdvance+'</p><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><p style="color: blue;">'+data.userClaimTxnData[i].claimadjustedAdvance+'</p><br/><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalAdvance+'</p>'+
						'</div></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
						'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
						'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
					}
			    }else{
			    	if(data.userClaimTxnData[i].claimTxnStatus=='Require Additional Approval'){
			    		$("#claimDetailsTable tr[id='claimsTransactionEntity"+data.userClaimTxnData[i].id+"'] td:nth-child(7) div:first").append('<br/><b>Additional Approval Required By:</b><br/>'+data.userClaimTxnData[i].selectedAdditionalApproval+'');
			    	}
			    }
			    if(role.indexOf("ACCOUNTANT")!=-1 || role.indexOf("AUDITOR")!=-1 || role.indexOf("CONTROLLER")!=-1){
			    	 var approverEmailVal=false;var selectedAdditionalApproval=false;
		    		 if(data.userClaimTxnData[i].approverEmails!=null && data.userClaimTxnData[i].approverEmails!=""){
		    			 if(data.userClaimTxnData[i].approverEmails.indexOf(data.userClaimTxnData[i].useremail)!=-1){
		    				 approverEmailVal=true;
		    			 }
		    		 }
		    		 if(data.userClaimTxnData[i].selectedAdditionalApproval!=null && data.userClaimTxnData[i].selectedAdditionalApproval!=""){
						    if(data.userClaimTxnData[i].selectedAdditionalApproval==data.userClaimTxnData[i].useremail){
						    	selectedAdditionalApproval=true;
						    }
		    		 }
		    		 if(data.userClaimTxnData[i].createdBy!=useremail && approverEmailVal==false && selectedAdditionalApproval==false){
		    			 $("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
		 					'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
		 					'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
		 					'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
		 					'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimexistingAdvance+'</p><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><p style="color: blue;">'+data.userClaimTxnData[i].claimadjustedAdvance+'</p><br/><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalAdvance+'</p>'+
		 					'</div></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'<br>'+data.userClaimTxnData[i].paymentMode+'<br>'+data.userClaimTxnData[i].instrumentNumber+'<br>'+data.userClaimTxnData[i].instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select  class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
		 					'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
		    			 	'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
		    		 }
			    }
			    //for accountant can be same user or not
			    if(data.userClaimTxnData[i].claimTxnStatus=='Approved') {
				   // if(data.userClaimTxnData[i].accountantEmails!=null){
				    //	if(data.userClaimTxnData[i].accountantEmails.indexOf(useremail)!=-1){
				    	if(data.userClaimTxnData[i].createdBy==useremail) {
				    	
				    		$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
				    		if(data.userClaimTxnData[i].claimTxnStatus=="Approved"){
				    			$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
								'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
								'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
								'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
								'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimexistingAdvance+'</p><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><p style="color: blue;">'+data.userClaimTxnData[i].claimadjustedAdvance+'</p><br/><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalAdvance+'</p>'+
								'</div></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div>'+
//								'<div class="claimpayementDiv"><br/>Select mode of payment:<select name="claimpaymentDetails" id="claimpaymentDetails" onchange="listAllBranchBankAccountsClaims(this);">'+
//					    		'<option value="1">CASH</option><option value="2">BANK</option></select><br/>Input payment details:<textarea name="bankDetails" id="bankDetails"></textarea></div>'+
					    		'<br/><input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit btn-idos" onclick="claimcompleteAccounting(this)" style="margin-top:10px;margin-left: 0px;"><br/><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select  class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
					    		'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
				    			'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
				    		}else{
				    			$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
								'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
								'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
								'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
								'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimexistingAdvance+'</p><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><p style="color: blue;">'+data.userClaimTxnData[i].claimadjustedAdvance+'</p><br/><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalAdvance+'</p>'+
								'</div></div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'<br>'+data.userClaimTxnData[i].paymentMode+'<br>'+data.userClaimTxnData[i].instrumentNumber+'<br>'+data.userClaimTxnData[i].instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
								'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
				    			'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
				    		}
				    	}
				    //}
			    }
			    $('.claimCommonTable:visible').slideUp();
			    //logic for separation transaction travel eligibility separately into travel eligibility label and travel eligibility figure
			    if(data.userClaimTxnData[i].claimtravelDetailedConfDescription!=null && data.userClaimTxnData[i].claimtravelDetailedConfDescription!=""){
				   	var individualclaimtravelDetailedConfDescription=data.userClaimTxnData[i].claimtravelDetailedConfDescription.substring(0,data.userClaimTxnData[i].claimtravelDetailedConfDescription.length).split('#');
				   	for(var m=0;m<individualclaimtravelDetailedConfDescription.length;m++){
				    	if(m>0){
				    		var labelAndValue=individualclaimtravelDetailedConfDescription[m].substring(0, individualclaimtravelDetailedConfDescription[m].length).split(':');
				    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<p style="color: black;"><b>'+labelAndValue[0]+'</b></p>:<br/>');
				    		if(typeof labelAndValue[1]!='undefined'){
					    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<p style="color: blue;">'+labelAndValue[1]+'</p><br/>');
					    	}
				    	}
				    }
				}
			    //logic for separation transaction advance eligibility separately into advance eligibility label and advance eligibility figure
			    if(data.userClaimTxnData[i].claimuserAdvanveEligibility!=null && data.userClaimTxnData[i].claimuserAdvanveEligibility!=""){
				   	var individualclaimuserAdvanveEligibility=data.userClaimTxnData[i].claimuserAdvanveEligibility.substring(0,data.userClaimTxnData[i].claimuserAdvanveEligibility.length).split('#');
				   	for(var m=0;m<individualclaimuserAdvanveEligibility.length;m++){
				   		if(m>0){
				    		var labelAndValue=individualclaimuserAdvanveEligibility[m].substring(0, individualclaimuserAdvanveEligibility[m].length).split(':');
				    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<p style="color: black;"><b>'+labelAndValue[0]+'</b></p>:<br/>');
				    		if(typeof labelAndValue[1]!='undefined'){
					    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<p style="color: blue;">'+labelAndValue[1]+'</p><br/>');
					    	}
				    	}
				    }
				}
			    //logic for separation transaction remarks separately into useremail and remarks made by them
			    if(data.userClaimTxnData[i].claimtxnRemarks!=null && data.userClaimTxnData[i].claimtxnRemarks!=""){
				   	var individualRemarks=data.userClaimTxnData[i].claimtxnRemarks.substring(0,data.userClaimTxnData[i].claimtxnRemarks.length).split(',');
				   	for(var m=0;m<individualRemarks.length;m++){
				    	var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
				    	$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtxnWorkflowRemarks"]').append('<p style="color: #ffoff;"><b>'+emailAndRemarks[0]+'</b></p>#');
				    	if(typeof emailAndRemarks[1]!='undefined'){
				    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtxnWorkflowRemarks"]').append('<p style="color: blue;">'+emailAndRemarks[1]+'</p><br/>');
				    	}
				    }
				}
			  //logic for separation transaction remarks separately into useremail and documents uploaded by them
			    if(data.userClaimTxnData[i].claimsupportingDoc!="" && data.userClaimTxnData[i].claimsupportingDoc!=null){
			    	var txndocument=data.userClaimTxnData[i].claimsupportingDoc;
			    	var rowTxnId=data.userClaimTxnData[i].id;
			    	var transTrID = 'claimsTransactionEntity'+rowTxnId;
			    	fillSelectElementWithUploadedDocs( txndocument, transTrID, 'claimfileDownload');
			    	
		    	}
			    if(data.userClaimTxnData[i].txnSpecialStatus=="Rules Not Followed"){
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] b[class="txnSpecialStatus"]').html("");
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] b[class="txnSpecialStatus"]').append('<img src="assets/images/green.png"></img>');
			    }
			    if(data.userClaimTxnData[i].claimTxnStatus!="" && data.userClaimTxnData[i].claimTxnStatus=="Rejected"){
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] div[class="txnstat"]').attr('class','txnstatred');
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] div[class="txnstatgreen"]').attr('class','txnstatred');
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] div[class="txnstatred"]').attr('class','txnstatred');
			    }
			    if(data.userClaimTxnData[i].claimTxnStatus!="" && data.userClaimTxnData[i].claimTxnStatus=="Accounted"){
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] div[class="txnstat"]').attr('class','txnstat');
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] div[class="txnstatgreen"]').attr('class','txnstat');
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] div[class="txnstatred"]').attr('class','txnstat');
			    }
			    if(data.userClaimTxnData[i].claimTxnStatus!="" && (data.userClaimTxnData[i].claimTxnStatus=="Approved" || data.userClaimTxnData[i].claimTxnStatus=="Require Approval" || data.userClaimTxnData[i].claimTxnStatus=="Require Additional Approval") || data.userClaimTxnData[i].claimTxnStatus=="Require Accounting"){
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] div[class="txnstat"]').attr('class','txnstatgreen');
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] div[class="txnstatgreen"]').attr('class','txnstatgreen');
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] div[class="txnstatred"]').attr('class','txnstatgreen');
			    }
			    if(data.userClaimTxnData[i].additionalApprovarUsers!=null){
				    if(typeof data.userClaimTxnData[i].additionalApprovarUsers!='undefined'){
				    	var additionalApprovarUsersList=data.userClaimTxnData[i].additionalApprovarUsers.substring(0,data.userClaimTxnData[i].additionalApprovarUsers.length).split(',');
				    	$("tr[id='claimsTransactionEntity"+data.userClaimTxnData[i].id+"'] select[id='userAddApproval']").append('<option value="">--Please Select--</option>');
				    	for(var k=0;k<additionalApprovarUsersList.length;k++){
				    		$("tr[id='claimsTransactionEntity"+data.userClaimTxnData[i].id+"'] select[id='userAddApproval']").append('<option value="'+additionalApprovarUsersList[k]+'">'+additionalApprovarUsersList[k]+'</option>');
				    	}
				    }
				}
			   // getCashBankReceivablePayable();
//			    if(role.indexOf("APPROVER")!=-1 || role.indexOf("ACCOUNTANT")!=-1){
//			   		getTravelExpenseClaimCountForApproverAccountant(useremail);
//			    }
			}
			if(data.userClaimTxnData[i].txnPurposeId=='16'){
				//travel advance settlement user transaction list
				//claim settlement only for creator and accountant
				var role=data.userClaimTxnData[i].userroles;
				var useremail=$("#hiddenuseremail").text();
			    $("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
			    //for creator claim detail data display logic
			    if(data.userClaimTxnData[i].createdBy==useremail){
			    	$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
			    	'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
			    	'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
			    	'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
			    	'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div></div>'+
			    	'</div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'<br>'+data.userClaimTxnData[i].paymentMode+'<br>'+data.userClaimTxnData[i].instrumentNumber+'<br>'+data.userClaimTxnData[i].instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
			    	'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
			    	'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
			    }
			    //for other accountant and auditor of the branch
			    if(role.indexOf("ACCOUNTANT")!=-1 || role.indexOf("AUDITOR")!=-1 || role.indexOf("CONTROLLER")!=-1){
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
			    	$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
					'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
					'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
					'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
					'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div></div>'+
					'</div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'<br>'+data.userClaimTxnData[i].paymentMode+'<br>'+data.userClaimTxnData[i].instrumentNumber+'<br>'+data.userClaimTxnData[i].instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select  class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
					'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
			    	'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
			    }
			    //for accountant can be same user or not
			    if(data.userClaimTxnData[i].createdBy==useremail){
				    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
				    	if(data.userClaimTxnData[i].claimTxnStatus=="Payment Due To Staff" || data.userClaimTxnData[i].claimTxnStatus=="Payment Due From Staff" || data.userClaimTxnData[i].claimTxnStatus=="No Due For Settlement"){
				    		var tempHtml='<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
							'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
							'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
							'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
							'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div></div>'+
							'</div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><div class="claimpayementDiv"><br/>';
				    		if(data.userClaimTxnData[i].claimTxnStatus == "Payment Due From Staff"){
								tempHtml+='Select mode of payment:<select name="claimpaymentDetails" id="claimpaymentDetails" onchange="listAllBranchBankAccountsClaims(this);">'+
					    		'<option value="1">CASH</option><option value="2">BANK</option></select><br/>Input payment details:<textarea name="bankDetails" id="bankDetails"></textarea>';
							}
					    	tempHtml+='</div><br/><input type="button" value="Complete Accounting" id="settleTravelClaimTxn" class="btn btn-submit btn-idos" onclick="claimSettlementAccounting(this)" style="margin-top:10px;width:170px;margin-left: 0px;"><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select  class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
					    	'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
					    	'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>';
							$("#claimDetailsTable").append(tempHtml);

				    	}else{
				    		$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
							'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
							'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
							'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
							'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div></div>'+
							'</div></td><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'<br>'+data.userClaimTxnData[i].paymentMode+'<br>'+data.userClaimTxnData[i].instrumentNumber+'<br>'+data.userClaimTxnData[i].instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
							'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
				    		'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
				    	}
				    }
				
			    //logic for separation transaction unsettled Travel claims separately into unsettled label and figure
			    if(data.userClaimTxnData[i].existingClaimsCurrentSettlementDetails!=null && data.userClaimTxnData[i].existingClaimsCurrentSettlementDetails!=""){
				   	var individualexistingClaimsCurrentSettlementDetails=data.userClaimTxnData[i].existingClaimsCurrentSettlementDetails.substring(0,data.userClaimTxnData[i].existingClaimsCurrentSettlementDetails.length).split('#');
				   	for(var m=0;m<individualexistingClaimsCurrentSettlementDetails.length;m++){
				    	if(m>0){
				    		var labelAndValue=individualexistingClaimsCurrentSettlementDetails[m].substring(0, individualexistingClaimsCurrentSettlementDetails[m].length).split(':');
				    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<p style="color: black;"><b>'+labelAndValue[0]+'</b></p>:<br/>');
				    		if(typeof labelAndValue[1]!='undefined'){
					    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<p style="color: blue;">'+labelAndValue[1]+'</p><br/>');
					    	}
				    	}
				    }
				}
			    //logic for separation of user expenditure on the travel claim transaction separatly into label and figure
			    if(data.userClaimTxnData[i].userExpenditureOnThisTxn!=null && data.userClaimTxnData[i].userExpenditureOnThisTxn!=""){
				   	var individualuserExpenditureOnThisTxn=data.userClaimTxnData[i].userExpenditureOnThisTxn.substring(0,data.userClaimTxnData[i].userExpenditureOnThisTxn.length).split('#');
				   	for(var m=0;m<individualuserExpenditureOnThisTxn.length;m++){
				   		if(m>0){
				    		var labelAndValue=individualuserExpenditureOnThisTxn[m].substring(0, individualuserExpenditureOnThisTxn[m].length).split(':');
				    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<p style="color: black;"><b>'+labelAndValue[0]+'</b></p>:<br/>');
				    		if(typeof labelAndValue[1]!='undefined'){
					    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<p style="color: blue;">'+labelAndValue[1]+'</p><br/>');
					    	}
				    	}
				    }
				}
			    //logic for separation transaction remarks separately into useremail and remarks made by them
			    if(data.userClaimTxnData[i].claimtxnRemarks!=null && data.userClaimTxnData[i].claimtxnRemarks!=""){
				   	var individualRemarks=data.userClaimTxnData[i].claimtxnRemarks.substring(0,data.userClaimTxnData[i].claimtxnRemarks.length).split(',');
				   	for(var m=0;m<individualRemarks.length;m++){
				    	var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
				    	$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtxnWorkflowRemarks"]').append('<p style="color:#FFOFF;"><b>'+emailAndRemarks[0]+'</b></p>#');
				    	if(typeof emailAndRemarks[1]!='undefined'){
				    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtxnWorkflowRemarks"]').append('<p style="color: blue;">'+emailAndRemarks[1]+'</p><br/>');
				    	}
				    }
				}
			    //logic for separation transaction remarks separately into useremail and documents uploaded by them
			    if(data.userClaimTxnData[i].claimsupportingDoc!="" && data.userClaimTxnData[i].claimsupportingDoc!=null){
			    	var txndocument=data.userClaimTxnData[i].claimsupportingDoc;
		    		var rowTxnId=data.userClaimTxnData[i].id;
	    			var transTrID = 'claimsTransactionEntity'+rowTxnId;
		    		fillSelectElementWithUploadedDocs(txndocument, transTrID, 'claimfileDownload');
		    	}
			    if(role.indexOf("CREATOR")!=-1 || role.indexOf("ACCOUNTANT")!=-1){
			    	var locHash=window.location.hash;
			    	if(locHash=="" || locHash=="#claimSetup"){
					    location.hash="#claimSetup";
					    showdivandactiveleftmenu("#claimSetup");
			    	}
			    }
//			    getCashBankReceivablePayable();
//			    if(role.indexOf("ACCOUNTANT")!=-1){
//			   		getTravelExpenseClaimCountForApproverAccountant(useremail);
//			    }
			}
			if(data.userClaimTxnData[i].txnPurposeId=='17' || data.userClaimTxnData[i].txnPurposeId=='19'){
				var useremail=$("#hiddenuseremail").text();
				var role=data.userClaimTxnData[i].userroles;
				 $("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
				    //for creator claim detail data display logic
				    if(data.userClaimTxnData[i].createdBy==useremail){
				    	//now based of claim transaction status display the claim txn row
				    	if(data.userClaimTxnData[i].claimTxnStatus=='Require Clarification'){
				    		//when approver send a request toclarify the claim transaction created by creator
				    		$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
						    '<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
						    '<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
						    '</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						    '<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Entered Advance:</b><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvanceTotalAdvanceAmount+'</p>'+
						    '</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p><br/><input type="button" value="Clarify Transaction" id="clarifyTxn" class="btn btn-submit btn-idos" onclick="advanceExpenseClarifyTransaction(this)" style="margin-top:10px;margin-left: 0px;width:170px;"></div></td><td><div class="rowToExpand"><select  class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
						    '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
				    		'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
				    	}else{
				    		$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
						    '<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
						    '<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
						    '</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						    '<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvanceTotalAdvanceAmount+'</p>'+
						    '</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.userClaimTxnData[i].paymentMode+'<br>'+data.userClaimTxnData[i].instrumentNumber+'<br>'+data.userClaimTxnData[i].instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
						    '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
				    		'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
				    	}
				    }
				    //for approver can be same user or not
				    if(data.userClaimTxnData[i].approverEmails!=null){
				    	if(data.userClaimTxnData[i].approverEmails.indexOf(useremail)!=-1){
				    		$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
							if(data.userClaimTxnData[i].claimTxnStatus=='Require Approval' || data.userClaimTxnData[i].claimTxnStatus=='Clarified'){
								$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
								'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
								'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
								'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
								'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvanceTotalAdvanceAmount+'</p>'+
								'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.userClaimTxnData[i].paymentMode+'<br>'+data.userClaimTxnData[i].instrumentNumber+'<br>'+data.userClaimTxnData[i].instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><br/>Action:<select class="claimapproverActionList" name="claimapproverActionList" id="claimapproverActionList"><option value="">--Please Select--</option>'+
								'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>User List:<br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
								'<input type="button" id="claimapproverAction" class="btn btn-submit btn-center" value="Submit" onclick="advanceExpenseCompleteAction(this)"><br/><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select  class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
								'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
								'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>');
							}else{
								$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
								'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
								'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
								'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
								'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvanceTotalAdvanceAmount+'</p>'+
								'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.userClaimTxnData[i].paymentMode+'<br>'+data.userClaimTxnData[i].instrumentNumber+'<br>'+data.userClaimTxnData[i].instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
								'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
								'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
							}
				    	}
				    }
				    if(data.userClaimTxnData[i].selectedAdditionalApproval==useremail){
				    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
						if(data.userClaimTxnData[i].claimTxnStatus=='Require Additional Approval'){
							$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
							'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
							'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
							'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
							'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><p style="color: blue;">'+data.claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.expenseAdvanceTotalAdvanceAmount+'</p>'+
							'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><br/>Action:<select class="claimapproverActionList" name="claimapproverActionList" id="claimapproverActionList"><option value="">--Please Select--</option>'+
							'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>User List:<br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
							'<input type="button" id="claimapproverAction" class="btn btn-submit btn-center" value="Submit" onclick="advanceExpenseCompleteAction(this)"><br/><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select  class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
							'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
							'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
						}else{
							$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
							'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
							'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
							'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
							'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvanceTotalAdvanceAmount+'</p>'+
							'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.userClaimTxnData[i].paymentMode+'<br>'+data.userClaimTxnData[i].instrumentNumber+'<br>'+data.userClaimTxnData[i].instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
							'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
							'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
						}
				    }else{
				    	if(data.userClaimTxnData[i].claimTxnStatus=='Require Additional Approval'){
				    		$("#claimDetailsTable tr[id='claimsTransactionEntity"+data.userClaimTxnData[i].id+"'] td:nth-child(7) div:first").append('<br/><b>Additional Approval Required By:</b><br/>'+data.userClaimTxnData[i].selectedAdditionalApproval+'');
				    	}
				    }
				    if(role.indexOf("ACCOUNTANT")!=-1 || role.indexOf("AUDITOR")!=-1 || role.indexOf("CONTROLLER")!=-1){
				    	var approverEmailVal=false;var selectedAdditionalApproval=false;
			    		 if(data.userClaimTxnData[i].approverEmails!=null && data.userClaimTxnData[i].approverEmails!=""){
			    			 if(data.userClaimTxnData[i].approverEmails.indexOf(data.userClaimTxnData[i].useremail)!=-1){
			    				 approverEmailVal=true;
			    			 }
			    		 }
			    		 if(data.userClaimTxnData[i].selectedAdditionalApproval!=null && data.userClaimTxnData[i].selectedAdditionalApproval!=""){
							    if(data.userClaimTxnData[i].selectedAdditionalApproval==data.userClaimTxnData[i].useremail){
							    	selectedAdditionalApproval=true;
							    }
			    		 }
			    		 if(data.userClaimTxnData[i].createdBy!=useremail && approverEmailVal==false && selectedAdditionalApproval==false){
			    			 $("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
							 '<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
							 '<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
							 '</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
							 '<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvanceTotalAdvanceAmount+'</p>'+
							 '</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.userClaimTxnData[i].paymentMode+'<br>'+data.userClaimTxnData[i].instrumentNumber+'<br>'+data.userClaimTxnData[i].instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select  class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
							 '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
			    			 '<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
			    		 }
				    }
				    //for accountant can be same user or not
				    if(data.userClaimTxnData[i].claimTxnStatus=='Approved'){
				    	 if(data.userClaimTxnData[i].createdBy==useremail){
					    		$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
					    		if(data.userClaimTxnData[i].claimTxnStatus=="Approved"){
					    			$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
									'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
									'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
									'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
									'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvanceTotalAdvanceAmount+'</p>'+
									'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div>'+
//									'<div class="claimpayementDiv"><br/>Select mode of payment:<select name="claimpaymentDetails" id="claimpaymentDetails" onchange="listAllBranchBankAccountsClaims(this);">'+
//									'<option value="1">CASH</option><option value="2">BANK</option></select><br/>Input payment details:<textarea name="bankDetails" id="bankDetails"></textarea></div>'+
									'<br/><input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit btn-idos" onclick="advanceExpenseCompleteAccounting(this)" style="margin-top:10px;margin-left: 0px;"><br/><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select  class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
									'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
									'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
					    		}else{
					    			$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
									'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
									'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
									'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
									'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvanceTotalAdvanceAmount+'</p>'+
									'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.userClaimTxnData[i].paymentMode+'<br>'+data.userClaimTxnData[i].instrumentNumber+'<br>'+data.userClaimTxnData[i].instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
									'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
					    			'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
					    		}
					    	}
				    }
				    $('.claimCommonTable:visible').slideUp();
				    //logic for separation transaction travel eligibility separately into travel eligibility label and travel eligibility figure
				    if(data.userClaimTxnData[i].claimuserAdvanveEligibility!=null && data.userClaimTxnData[i].claimuserAdvanveEligibility!=""){
					   	var individualclaimuserAdvanveEligibility=data.userClaimTxnData[i].claimuserAdvanveEligibility.substring(0,data.userClaimTxnData[i].claimuserAdvanveEligibility.length).split('#');
					   	for(var m=0;m<individualclaimuserAdvanveEligibility.length;m++){
					    	if(m>0){
					    		var labelAndValue=individualclaimuserAdvanveEligibility[m].substring(0, individualclaimuserAdvanveEligibility[m].length).split(':');
					    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<p style="color: black;"><b>'+labelAndValue[0]+'</b></p>:<br/>');
					    		if(typeof labelAndValue[1]!='undefined'){
						    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<p style="color: blue;">'+labelAndValue[1]+'</p><br/>');
						    	}
					    	}
					    }
					}
				    //logic for separation transaction advance eligibility separately into advance eligibility label and advance eligibility figure
				/* no need to show this information again on column 5th
				    if(data.userClaimTxnData[i].claimuserAdvanveEligibility!=null && data.userClaimTxnData[i].claimuserAdvanveEligibility!=""){
					   	var individualclaimuserAdvanveEligibility=data.userClaimTxnData[i].claimuserAdvanveEligibility.substring(0,data.userClaimTxnData[i].claimuserAdvanveEligibility.length).split('#');
					   	for(var m=0;m<individualclaimuserAdvanveEligibility.length;m++){
					   		if(m>0){
					    		var labelAndValue=individualclaimuserAdvanveEligibility[m].substring(0, individualclaimuserAdvanveEligibility[m].length).split(':');
					    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<p style="color: black;"><b>'+labelAndValue[0]+'</b></p>:<br/>');
					    		if(typeof labelAndValue[1]!='undefined'){
						    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<p style="color: blue;">'+labelAndValue[1]+'</p><br/>');
						    	}
					    	}
					    }
					} */
				    //logic for separation transaction remarks separately into useremail and remarks made by them
				    if(data.userClaimTxnData[i].claimtxnRemarks!=null && data.userClaimTxnData[i].claimtxnRemarks!=""){
					   	var individualRemarks=data.userClaimTxnData[i].claimtxnRemarks.substring(0,data.userClaimTxnData[i].claimtxnRemarks.length).split(',');
					   	for(var m=0;m<individualRemarks.length;m++){
					    	var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
					    	$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtxnWorkflowRemarks"]').append('<p style="color: #FFOFF;"><b>'+emailAndRemarks[0]+'</b></p>#');
					    	if(typeof emailAndRemarks[1]!='undefined'){
					    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtxnWorkflowRemarks"]').append('<p style="color: blue;">'+emailAndRemarks[1]+'</p><br/>');
					    	}
					    }
					}
				  //logic for separation transaction remarks separately into useremail and documents uploaded by them
				    if(data.userClaimTxnData[i].claimsupportingDoc!="" && data.userClaimTxnData[i].claimsupportingDoc!=null){
				    	var txndocument=data.userClaimTxnData[i].claimsupportingDoc;
				    	var rowTxnId=data.userClaimTxnData[i].id;
				    	var transTrID = 'claimsTransactionEntity'+rowTxnId;
				    	fillSelectElementWithUploadedDocs(txndocument, transTrID, 'txnViewListUpload');
			    	}
				    if(data.userClaimTxnData[i].txnSpecialStatus=="Rules Not Followed"){
				    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] b[class="txnSpecialStatus"]').html("");
				    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] b[class="txnSpecialStatus"]').append('<img src="assets/images/green.png"></img>');
				    }
				    if(data.userClaimTxnData[i].claimTxnStatus!="" && data.userClaimTxnData[i].claimTxnStatus=="Rejected"){
				    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] div[class="txnstat"]').attr('class','txnstatred');
				    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] div[class="txnstatgreen"]').attr('class','txnstatred');
				    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] div[class="txnstatred"]').attr('class','txnstatred');
				    }
				    if(data.userClaimTxnData[i].claimTxnStatus!="" && data.userClaimTxnData[i].claimTxnStatus=="Accounted"){
				    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] div[class="txnstat"]').attr('class','txnstat');
				    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] div[class="txnstatgreen"]').attr('class','txnstat');
				    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] div[class="txnstatred"]').attr('class','txnstat');
				    }
				    if(data.userClaimTxnData[i].claimTxnStatus!="" && (data.userClaimTxnData[i].claimTxnStatus=="Approved" || data.userClaimTxnData[i].claimTxnStatus=="Require Approval" || data.userClaimTxnData[i].claimTxnStatus=="Require Additional Approval") || data.userClaimTxnData[i].claimTxnStatus=="Require Accounting"){
				    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] div[class="txnstat"]').attr('class','txnstatgreen');
				    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] div[class="txnstatgreen"]').attr('class','txnstatgreen');
				    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"] div[class="txnstatred"]').attr('class','txnstatgreen');
				    }
				    if(data.userClaimTxnData[i].additionalApprovarUsers!=null){
					    if(typeof data.userClaimTxnData[i].additionalApprovarUsers!='undefined'){
					    	var additionalApprovarUsersList=data.userClaimTxnData[i].additionalApprovarUsers.substring(0,data.userClaimTxnData[i].additionalApprovarUsers.length).split(',');
					    	$("tr[id='claimsTransactionEntity"+data.userClaimTxnData[i].id+"'] select[id='userAddApproval']").append('<option value="">--Please Select--</option>');
					    	for(var k=0;k<additionalApprovarUsersList.length;k++){
					    		$("tr[id='claimsTransactionEntity"+data.userClaimTxnData[i].id+"'] select[id='userAddApproval']").append('<option value="'+additionalApprovarUsersList[k]+'">'+additionalApprovarUsersList[k]+'</option>');
					    	}
					    }
					}
//				    getCashBankReceivablePayable();
//				    if(role.indexOf("APPROVER")!=-1 || role.indexOf("ACCOUNTANT")!=-1){
//				   		getTravelExpenseClaimCountForApproverAccountant(useremail);
//				    }
			}
			if(data.userClaimTxnData[i].txnPurposeId=='18'){
				//expense advance settlement only for creator and accountant
				var useremail=$("#hiddenuseremail").text();
				var role=data.userClaimTxnData[i].userroles;
			    $("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
			    //for creator claim detail data display logic
			    if(data.userClaimTxnData[i].createdBy==useremail){
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
			    	$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
				    '<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
				    '<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
				    '</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
				    '<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Advance Settled :</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Due From Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].dueFromCompany+'</p><br/><b>Due To Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].dueToCompany+'</p><br/><b>Amount Returned In Case Of Due To Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].amountReturnInCaseOfDueToCompany+'</p>'+
				    '</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.userClaimTxnData[i].paymentMode+'<br>'+data.userClaimTxnData[i].instrumentNumber+'<br>'+data.userClaimTxnData[i].instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
				    '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
			    	'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
			    }
			  //for other accountant and auditor of the branch
			    if(role.indexOf("ACCOUNTANT")!=-1 || role.indexOf("AUDITOR")!=-1 || role.indexOf("CONTROLLER")!=-1){
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
			    	$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
					'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
					'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
					'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
					'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Advance Settled :</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Due From Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].dueFromCompany+'</p><br/><b>Due To Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].dueToCompany+'</p><br/><b>Amount Returned In Case Of Due To Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].amountReturnInCaseOfDueToCompany+'</p>'+
					'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.userClaimTxnData[i].paymentMode+'<br>'+data.userClaimTxnData[i].instrumentNumber+'<br>'+data.userClaimTxnData[i].instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
					'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
			    	'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
			    }
			    //for accountant can be same user or not
			    if(data.userClaimTxnData[i].createdBy==useremail){
				    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
				    	if(data.userClaimTxnData[i].claimTxnStatus=="Payment Due To Staff" || data.userClaimTxnData[i].claimTxnStatus=="Payment Due From Staff" || data.userClaimTxnData[i].claimTxnStatus=="No Due For Settlement"){
				    		var tmpHtml='<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
				    		'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
				    		'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
				    		'</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
				    		'<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Advance Settled :</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Due From Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].dueFromCompany+'</p><br/><b>Due To Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].dueToCompany+'</p><br/><b>Amount Returned In Case Of Due To Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].amountReturnInCaseOfDueToCompany+'</p>'+
				    		'</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><div class="claimpayementDiv"><br/>';
							if(data.userClaimTxnData[i].claimTxnStatus == "Payment Due From Staff"){
								tmpHtml+='Select mode of payment:<select name="claimpaymentDetails" id="claimpaymentDetails" onchange="listAllBranchBankAccountsClaims(this);">'+
					    		'<option value="1">CASH</option><option value="2">BANK</option></select><br/>Input payment details:<textarea name="bankDetails" id="bankDetails"></textarea>';
							}
							tmpHtml+='</div><br/><input type="button" value="Complete Accounting" id="settleExpenseAdvanceTxn" class="btn btn-submit btn-idos" onclick="expenseAdvanceSettlementAccounting(this)" style="margin-top:10px;width:170px;margin-left: 0px;"><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
							'<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
							'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>';
							$("#claimDetailsTable").append(tmpHtml);
						}else{
							$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'" txnref="'+data.userClaimTxnData[i].claimTxnReferenceNo+'"><td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
						    '<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b class="txnSpecialStatus"></b><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
						    '<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
						    '</div></td><td><div class="rowToExpand"><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						    '<td><div class="rowToExpand"><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div class="rowToExpand"><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Advance Settled :</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Due From Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].dueFromCompany+'</p><br/><b>Due To Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].dueToCompany+'</p><br/><b>Amount Returned In Case Of Due To Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].amountReturnInCaseOfDueToCompany+'</p>'+
						    '</div></div></td><td><div class="rowToExpand"><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'<br>'+data.userClaimTxnData[i].paymentMode+'<br>'+data.userClaimTxnData[i].instrumentNumber+'<br>'+data.userClaimTxnData[i].instrumentDate+'</p></div></td><td><div class="rowToExpand"><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div class="rowToExpand"><select class="auditorAccountantSelect" name="txnViewListUpload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
						    '<br><button id="txnViewListUploadBtn" class="fa fa-upload btn btn-submit" onclick="uploadFileBlob(this);"></button><button id="txnViewListUploadDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><button id="txnViewListUploadDelBtn" class="fa fa-trash btn btn-cancel" style="margin-left:14px;" onclick="deleteUploadedFile(this);"></button></div></td>'+
							'<td><div class="rowToExpand"><div class="claimtxnWorkflowRemarks"></div><div><textarea  rows="3" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-center" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
						}
				    }
				
				 $('.claimCommonTable:visible').slideUp();
				//logic for separation transaction travel eligibility separately into travel eligibility label and travel eligibility figure
				    if(data.userClaimTxnData[i].claimuserAdvanveEligibility!=null && data.userClaimTxnData[i].claimuserAdvanveEligibility!=""){
					   	var individualclaimuserAdvanveEligibility=data.userClaimTxnData[i].claimuserAdvanveEligibility.substring(0,data.userClaimTxnData[i].claimuserAdvanveEligibility.length).split('#');
					   	for(var m=0;m<individualclaimuserAdvanveEligibility.length;m++){
					    	if(m>0){
					    		var labelAndValue=individualclaimuserAdvanveEligibility[m].substring(0, individualclaimuserAdvanveEligibility[m].length).split(':');
					    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<p style="color: black;"><b>'+labelAndValue[0]+'</b></p>:<br/>');
					    		if(typeof labelAndValue[1]!='undefined'){
						    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<p style="color: blue;">'+labelAndValue[1]+'</p><br/>');
						    	}
					    	}
					    }
					}
				    //logic for separation transaction advance eligibility separately into advance eligibility label and advance eligibility figure
					/* no need to show this information again on column 5th
					if(data.userClaimTxnData[i].claimuserAdvanveEligibility!=null && data.userClaimTxnData[i].claimuserAdvanveEligibility!=""){
					   	var individualclaimuserAdvanveEligibility=data.userClaimTxnData[i].claimuserAdvanveEligibility.substring(0,data.userClaimTxnData[i].claimuserAdvanveEligibility.length).split('#');
					   	for(var m=0;m<individualclaimuserAdvanveEligibility.length;m++){
					   		if(m>0){
					    		var labelAndValue=individualclaimuserAdvanveEligibility[m].substring(0, individualclaimuserAdvanveEligibility[m].length).split(':');
					    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<p style="color: black;"><b>'+labelAndValue[0]+'</b></p>:<br/>');
					    		if(typeof labelAndValue[1]!='undefined'){
						    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<p style="color: blue;">'+labelAndValue[1]+'</p><br/>');
						    	}
					    	}
					    }
					} */
				    //logic for separation transaction remarks separately into useremail and remarks made by them
				    if(data.userClaimTxnData[i].claimtxnRemarks!=null && data.userClaimTxnData[i].claimtxnRemarks!=""){
					   	var individualRemarks=data.userClaimTxnData[i].claimtxnRemarks.substring(0,data.userClaimTxnData[i].claimtxnRemarks.length).split(',');
					   	for(var m=0;m<individualRemarks.length;m++){
					    	var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
					    	$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtxnWorkflowRemarks"]').append('<p style="color: #FFOFF;"><b>'+emailAndRemarks[0]+'</b></p>#');
					    	if(typeof emailAndRemarks[1]!='undefined'){
					    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtxnWorkflowRemarks"]').append('<p style="color: blue;">'+emailAndRemarks[1]+'</p><br/>');
					    	}
					    }
					}
				    //logic for separation transaction remarks separately into useremail and documents uploaded by them
				    if(data.userClaimTxnData[i].claimsupportingDoc!="" && data.userClaimTxnData[i].claimsupportingDoc!=null){
				    	var txndocument=data.userClaimTxnData[i].claimsupportingDoc;
			    		var rowTxnId=data.userClaimTxnData[i].id;
						var transTrID = 'claimsTransactionEntity'+rowTxnId;
						fillSelectElementWithUploadedDocs(txndocument, transTrID, 'claimfileDownload');
				    	
			    	}
				    if(role.indexOf("CREATOR")!=-1 || role.indexOf("ACCOUNTANT")!=-1){
				    	var locHash=window.location.hash;
				    	if(locHash=="" || locHash=="#claimSetup"){
						    location.hash="#claimSetup";
						    showdivandactiveleftmenu("#claimSetup");
				    	}
				    }
//				    getCashBankReceivablePayable();
//				    if(role.indexOf("ACCOUNTANT")!=-1){
//				   		getTravelExpenseClaimCountForApproverAccountant(useremail);
//				    }
			}
			//Edit transaction common for all claims type
			if(data.userClaimTxnData[i].claimTxnStatus=='Require Approval' && data.userClaimTxnData[i].createdBy==useremail){
				$("#claimDetailsTable tr[id='claimsTransactionEntity"+data.userClaimTxnData[i].id+"'] td:nth-child(7) div:first").append('<br/><input type="button" value="Edit Transaction" id="editClaimAllowed" class="editClaimAllowed btn btn-submit btn-center" onclick="editClaimsOnceAllowed(this)">');
			}
			else if((data.userClaimTxnData[i].txnPurposeId=='18' || data.userClaimTxnData[i].txnPurposeId=='16') && data.userClaimTxnData[i].claimTxnStatus!='Accounted' && data.userClaimTxnData[i].createdBy==useremail){
				$("#claimDetailsTable tr[id='claimsTransactionEntity"+data.userClaimTxnData[i].id+"'] td:nth-child(7) div:first").append('<br/><input type="button" value="Edit Transaction" id="editClaimAllowed" class="editClaimAllowed btn btn-submit btn-center" onclick="editClaimsOnceAllowed(this)">');
			}
		}
		userClaimTransactionListString=$("#claimDetailsTable tbody").html();
	}
	setPagingDetail('claimDetailsTable', 20, 'pagingCalimNavPosition');
}

var editClaimsOnceAllowed = function(elem){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var parentTr = $(elem).closest('tr').attr('id'); //claimsTransactionEntity280
	var transactionEntityId=parentTr.substring(23, parentTr.length);
	var jsonData = {};
	jsonData.transactionEntityId = transactionEntityId;
	jsonData.email = $("#hiddenuseremail").text();
	var url="/expenseclaims/showExpenseClaimDetails";
	$.ajax({
		url         : url,
		data        : JSON.stringify(jsonData),
		type        : "text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		async 		: false,
		method      : "POST",
		contentType : 'application/json',
		success     : function (data) {
			$('#newClaimButton').click();
			$("#whatYouWantToDoClaim").val(data.transactiondetailsData[0].transactionPurposeVal).change();
			var transactionPurposeVal = data.transactiondetailsData[0].transactionPurposeVal;
			var transactionTableTr="";
			if(transactionPurposeVal == 15){
				transactionTableTr = "cRTAtrid";
				$("#" + transactionTableTr).attr("name", transactionEntityId);
				$("#" + transactionTableTr +" select[class='claimBranch']").val(data.transactiondetailsData[0].branchId).change();
				$("#" + transactionTableTr +" select[class='claimProject']").val(data.transactiondetailsData[0].projectID);
				$("#" + transactionTableTr +" select[id='travelType']").val(data.transactiondetailsData[0].travelTypeVal).change();
				$("#" + transactionTableTr +" div[class='numberOfFromToTravelOption'] input[id='fromPlace']").val(data.transactiondetailsData[0].fromPlace);
				$("#" + transactionTableTr +" div[class='numberOfFromToTravelOption']  input[id='toPlace']").val(data.transactiondetailsData[0].toPlace);
				$("#" + transactionTableTr +" select[class='typeOfCity']").val(data.transactiondetailsData[0].typeOfCityVal).change();
				$("#" + transactionTableTr +" select[id='appropriateDistance']").val(data.transactiondetailsData[0].appropriateDistanceVal).change();
				$("#" + transactionTableTr +" input[class='totalDays']").val(data.transactiondetailsData[0].totalDays);
				$("#" + transactionTableTr +" input[id='advanceRequestedOnThistxn']").val(data.transactiondetailsData[0].grossAmount).keyup();
			}
			else if(transactionPurposeVal == 16){
				transactionTableTr = "cSTAtrid";
				$("#" + transactionTableTr).attr("name", transactionEntityId);
				//$("#" + transactionTableTr +" select[id='availableUnsettledClaimAdvances'] option:selected").text(data.transactiondetailsData[0].claimSettlementRefNo);
				$("#" + transactionTableTr +" select[id='availableUnsettledClaimAdvances']").val(data.transactiondetailsData[0].claimSettlementId).change();
				$("#" + transactionTableTr +" input[id='expenseIncurredTravel']").val(data.transactiondetailsData[0].expenseIncurredTravel);
				$("#clmTravelExpensesTR").find('.clmExpenceAmt').val(data.transactiondetailsData[0].expenseIncurredTravelItemAmount);
				$("#clmTravelExpensesTR").find('.clmExpenceTax').val(data.transactiondetailsData[0].expenseIncurredTravelItemTax);
				$("#" + transactionTableTr +" input[id='expenseIncurredBnL']").val(data.transactiondetailsData[0].expenseIncurredBnL);
				$("#clmLodgingExpensesTR").find('.clmExpenceAmt').val(data.transactiondetailsData[0].expenseIncurredBnLItemAmount);
				$("#clmLodgingExpensesTR").find('.clmExpenceTax').val(data.transactiondetailsData[0].expenseIncurredBnLItemTax);
				$("#" + transactionTableTr +" input[id='expenseIncurredOtherExpenses']").val(data.transactiondetailsData[0].expenseIncurredOtherExpenses);
				$("#clmOtherExpensesTR").find('.clmExpenceAmt').val(data.transactiondetailsData[0].expenseIncurredOtherExpensesItemAmount);
				$("#clmOtherExpensesTR").find('.clmExpenceTax').val(data.transactiondetailsData[0].expenseIncurredOtherExpensesItemTax);
				$("#" + transactionTableTr +" input[id='expenseIncurredFixedPerDiam']").val(data.transactiondetailsData[0].expenseIncurredFixedPerDiam);
				$("#clmFixedDIAMTR").find('.clmExpenceAmt').val(data.transactiondetailsData[0].expenseIncurredFixedPerDiamItemAmount);
				$("#clmFixedDIAMTR").find('.clmExpenceTax').val(data.transactiondetailsData[0].expenseIncurredFixedPerDiamItemTax);
				$("#" + transactionTableTr +" input[id='totalExpenseIncurred']").val(data.transactiondetailsData[0].grossAmount);
				$("#" + transactionTableTr +" input[id='balanceAmount']").val(data.transactiondetailsData[0].balanceAmount);
				$("#" + transactionTableTr +" input[id='balanceDueToTheCompanyAmount']").val(data.transactiondetailsData[0].amtReturnInCaseOfDueToCompany);
				$("#" + transactionTableTr +" input[id='dueFromCompanyAmount']").val(data.transactiondetailsData[0].amtDueFromCompany);
				$("#" + transactionTableTr +" input[id='dueToCompanyAmount']").val(data.transactiondetailsData[0].amtDueToCompany);
				$("#" + transactionTableTr +" input[id='updatedUnsettledAmount']").val(data.transactiondetailsData[0].updatedUnsettledAmount);
				$("#" + transactionTableTr +" #balanceUnsettledAgainstThisTxn").html(data.transactiondetailsData[0].balanceUnsettledAgainstThisTxn);
				
				setBillwiseDetails(data.transactiondetailsData[0].expenseDetailsArray,"clmTravelExpensesModule");
				setBillwiseDetails(data.transactiondetailsData[0].lodgingAndBoardDetailsArray,"clmLodgingExpensesModule");
				setBillwiseDetails(data.transactiondetailsData[0].otherExpensesDetailsArray,"clmOtherExpensesModule");
				setBillwiseDetails(data.transactiondetailsData[0].fixedPerDiamDetailsArray,"clmFixedDIAMModule");
			}
			else if(transactionPurposeVal == 17) {
				transactionTableTr = "cREAtrid";
				$("#" + transactionTableTr).attr("name", transactionEntityId);
				$("#" + transactionTableTr +" select[class='claimBranch']").val(data.transactiondetailsData[0].branchId).change();
				$("#" + transactionTableTr +" select[class='claimProject']").val(data.transactiondetailsData[0].projectID).change();
				$("#" + transactionTableTr +" select[class='expenseAdvanceItems']").val(data.transactiondetailsData[0].itemId).change();
			}else if(transactionPurposeVal == 18) {
				transactionTableTr = "cSEAtrid";
				$("#" + transactionTableTr).attr("name", transactionEntityId);
				//$("#" + transactionTableTr +" select[class='claimBranch']").val(data.transactiondetailsData[0].branchId).change();
				//$("#" + transactionTableTr +" select[class='claimProject']").val(data.transactiondetailsData[0].projectID).change();
				$("#" + transactionTableTr +" select[class='availableUnsettledExpenseAdvances'] option:selected").text(data.transactiondetailsData[0].claimSettlementRefNo);
				$("#" + transactionTableTr +" select[class='availableUnsettledExpenseAdvances'] option:selected").val(data.transactiondetailsData[0].claimSettlementId).change();
				$("#" + transactionTableTr +" input[id='item1ExpIncurredAmount']").val(data.transactiondetailsData[0].grossAmount).keyup();
				$("#" + transactionTableTr +" input[id='balanceDueToTheCompanyExpAdvAmount']").val(data.transactiondetailsData[0].amtReturnInCaseOfDueToCompany);
				$("#" + transactionTableTr +" input[id='dueFromCompanyExpAdvAmount']").val(data.transactiondetailsData[0].amtDueFromCompany);
				$("#" + transactionTableTr +" input[id='dueToCompanyExpAdvAmount']").val(data.transactiondetailsData[0].amtDueToCompany);
				setBillwiseDetails(data.transactiondetailsData[0].incurredExpensesDetailssArray,"clmIncurredExpensesModule");
				$("#clmIncurredExpensesTR").find(".clmExpenceAmt").val(data.transactiondetailsData[0].claimNetAmt);
				$("#clmIncurredExpensesTR").find(".clmExpenceTax").val(data.transactiondetailsData[0].claimNetTax);
			}else if(transactionPurposeVal==19) {
				transactionTableTr= "cREEtrid";
				$("#" + transactionTableTr).attr("name", transactionEntityId);
				$("#" + transactionTableTr +" select[class='claimBranch']").val(data.transactiondetailsData[0].branchId).change();
				$("#" + transactionTableTr +" select[class='claimProject']").val(data.transactiondetailsData[0].projectID).change();
				$("#" + transactionTableTr +" select[class='expenseClaimItem']").val(data.transactiondetailsData[0].itemId).change();
				setBillwiseDetails(data.transactiondetailsData[0].reiEmbExpensesDetailsArray,"clmReiEmbExpensesModule");
				$("#cREEtrid").find(".clmExpenceAmt").val(data.transactiondetailsData[0].claimNetAmt);
				$("#cREEtrid").find(".clmExpenceTax").val(data.transactiondetailsData[0].claimNetTax);
				$("#cREEtrid").find(".clmExpenceGross").val(data.transactiondetailsData[0].expenseReimbursementAmountRequired);
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout();
			}else if(xhr.status == 500){
				enableTransactionButtons();
	    		swal("Error on fetching transtion detail to Edit", "Please contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
			$(".whatYouWantToDoClaim").attr("disabled", "disabled");
		}
	});
}

function giveclaimtxnRemarks(elem){
	//var parentTr=$(elem).parent().parent().parent('tr:first').attr('id');
	var parentTr = $(elem).closest('tr').attr('id');
	var transactionEntityId=parentTr.substring(23, parentTr.length);
	var useremail=$("#hiddenuseremail").text();
	var transactionRmarks=$("#claimDetailsTable tr[id='"+parentTr+"'] textarea[name='txnRemarks']").val();
	var jsonData = {};
	jsonData.transactionPrimId = transactionEntityId;
	jsonData.email = useremail;
	jsonData.txnRmarks=transactionRmarks;
	jsonData.suppDoc="";
	jsonData.selectedApproverAction="7";
	if(transactionRmarks!=""){
	var url="/claims/approverAction";
		$.ajax({
			url : url,
			data : JSON.stringify(jsonData),
			type : "text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method : "POST",
			contentType : 'application/json',
			success : function(data) {
			},
			error: function(xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	}
}


/*Create Claim Starts*/
/*function getClaimData(){
	//alert("getClaimData") ;
	var jsonData = {};
	jsonData.email = $("#hiddenuseremail").text();
	ajaxCall('/claims/getUserRelatedClaimsAvailable', jsonData, '', '', '', '', 'newClaimSuccess', '', false);
}*/

$(document).ready(function() {
	$('#newClaimButton').on('click', function() {
		var jsonData = {};
		jsonData.email = $("#hiddenuseremail").text();
		//ajaxCall('/claims/getUserRelatedClaimsAvailable', jsonData, '', '', '', '', 'newClaimSuccess', '', false);
		newClaimSuccess();
		$('.claimCommonTable').hide();
		$('#searchClaim:visible').slideUp();
		$('.whatDoINeedToDoClaimContent').slideUp().html('');
		$('.openWhatDoINeedToDoClaim').hide();
		$('#claimTransWhatToDo span').empty();
		$('#createClaim').slideDown('slow');
	});

	$('.cancelCreateClaim').on('click', function() {
		$('.openWhatDoINeedToDoClaim').fadeOut('fast');
		$('.whatDoINeedToDoClaimContent').slideUp().html('');
		$('#createClaim:visible').slideUp();
		$('#searchClaim:visible').slideUp();
		$("#claimDetailsTable tbody").html("");
		$("#claimDetailsTable tbody").html(userClaimTransactionListString);
		clearDivContents();
	});

	$('#newSearchClaimButton').on('click', function() {
		//var jsonData = {};
		//jsonData.email = $("#hiddenuseremail").text();
		//ajaxCall('/claims/getSearchCriteriaData', jsonData, '', '', '', '', 'claimSearchTableSuccess', '', false);
		$('.openWhatDoINeedToDoClaim').fadeOut('fast');
		$('.whatDoINeedToDoClaimContent').hide().html('');
		$('#createClaim:visible').slideUp();
		$('#claimsSearchTable').find('input').val('');
		$('#claimsSearchTable select option:first').prop('selected', 'selected');
		$('#searchClaim').slideDown('slow');
	});

	$('#whatDoINeedToDoClaimButton').on('mouseover', function() {
		if ($('.claimCommonTable:visible').attr('id') === 'claimRequestTravelAdvanceTable') {
			$('#claimTransWhatToDo span').html('Use this option to apply for travel advance from your company. In the first column, please select whether your travel is within your country or international. Then select which country (if international travel) or state (if domestic travel). Then select from where to where you are traveling. If you are visiting multiple places in one trip, then complete filing details of From / To of the first place of visit and then fill in details of next place and so on. Then select the distance from place to place (select approximate distance) and finally input the total days of your trip excluding days of travel. Once you provide the details in the first column, it will automatically display your eligibility details in columns 2 and 3. Then in column 4 please enter manually, if you wish to adjust any of your previous unsettled travel advances, if not, ignore that field and below that enter how much travel advance you want now. Read instructions in column 5, select purpose of visit in column 6, upload any documents for your travel advance in column 7 and then submit for approval. Once you receive approval, you can collect the advance amount from your accountant and then click on "Confirm receipt" to confirm that you have received the travel advance.');
		} else if ($('.claimCommonTable:visible').attr('id') === 'claimSettleTravelAdvanceTable') {
			$('#claimTransWhatToDo span').html('This option allows you to settle any of the travel advances you have taken earlier. In the first column, select from the dropdown which of the earlier travel advance you wish to settle now. Column 2 wil be automatically populated by system and you will be able to see, how much advance is pending settlement. In column 3 manually input how much amount has been spent / claimed against each of the categories (Travel, Boarding & Lodging, Other Expenses & Fixed Per diam) and in 4th column upload the corresponding bills and statements for the amounts input in column 3. system will display in column 5, balance due from company to you or balance due from you to the company as the case maybe.');
		} else if ($('.claimCommonTable:visible').attr('id') === 'claimRequestExpenseAdvanceTable') {
			$('#claimTransWhatToDo span').html('Request advance for expense.');
		} else if ($('.claimCommonTable:visible').attr('id') === 'claimSettleExpenseAdvanceTable') {
			$('#claimTransWhatToDo span').html('Settle advance for expense.');
		} else if ($('.claimCommonTable:visible').attr('id') === 'claimRequestExpenseReimbursementTable') {
			$('#claimTransWhatToDo span').html('Request for expense reimbursement.');
		}
	});

	$('body').on('click', '#whatToDoContentCancel', function() {
		$('.whatDoINeedToDoClaimContent').slideUp().html('');
	});
});


//$('#whatYouWantToDoClaim').on('change', function() {
function whatYouWantToDoClaimFun(elem){
	var value=$(elem).val();
	$('.claimCommonTable:visible').slideUp();
	//if (!isEmpty(this.value)){
	if (!isEmpty(value)){
		var jsonData = {};
		jsonData.email = $("#hiddenuseremail").text();
		$('.clmSettleModule').slideUp('fast');
		$('.claimSettlementDetailsTable').find("tbody").html("");
		if ('15' === value) {
			jsonData.type = "travel";
			$('#claimRequestTravelAdvanceTable').slideDown('slow');
			$('.openWhatDoINeedToDoClaim').fadeIn('fast');
		} else if ('16' === value) {
			jsonData.type = "travel";
			$('#claimSettleTravelAdvanceTable').slideDown('slow');
			$('.openWhatDoINeedToDoClaim').fadeIn('fast');
		} else if ('17' === value) {
			jsonData.type = "expense";
			$('#claimRequestExpenseAdvanceTable').slideDown('slow');
			$('.openWhatDoINeedToDoClaim').fadeIn('fast');
		} else if ('18' == value) {
			jsonData.type = "expense";
			$('#claimSettleExpenseAdvanceTable').slideDown('slow');
			$('.openWhatDoINeedToDoClaim').fadeIn('fast');
		} else if ('19' === value) {
			jsonData.type = "expense";
			$('#claimRequestExpenseReimbursementTable').slideDown('slow');
			$('.openWhatDoINeedToDoClaim').fadeIn('fast');
		} else {
			$('.openWhatDoINeedToDoClaim').fadeOut('fast');
		}
		$('.numberOfFromToTravelOption').html('');
		$('select option:first').prop('selected','selected');
		$('input[type="text"]').val("");
		$('textares').val('');
		$("#travelType option:first").prop('selected','selected');
		$("#typeOfCity option:first").prop('selected','selected');
		$("#appropriateDistance option:first").prop('selected','selected');
		$(".monLimitReimbursement").text("");
		$(".reimbursementAccountedDiv").text("");
		$(".reimbursementInProgressDiv").text("");
		$("#expenseReimbursementAmountRequired").val();
		clearDivContents();
		$("#travelPlaceToVisit").val("1");
		$(".numberOfFromToTravelOption").append('<div class="dynamicFromToTravelDropdown">From:<select class="fromToTravelDropdown" name="fromToTravelDropdown" style="width:148px;display:none;"><option value="">-Please Select-</option></select><br/><input type="text" name="fromToTravelDropdownOther" id="fromPlace"  class="fromToTravelDropdownOther" placeholder="Location" style="width:128px;"/></div>');

		$(".numberOfFromToTravelOption").append('<div class="dynamicFromToTravelDropdown" style="margin-top:16px;">To:<select class="fromToTravelDropdown" name="fromToTravelDropdown" style="width:148px;display:none;"><option value="">-Please Select-</option></select><br/><input  type="text"  name="fromToTravelDropdownOther" class="fromToTravelDropdownOther" id="toPlace" placeholder="Location" style="width:128px;"/></div>');
		claimTableSuccess(jsonData);
		$(".closeClaimSettlementModule").trigger("click");
		//ajaxCall('/claims/getUserClaimBranchProject', jsonData, '', '', '', '', 'claimTableSuccess', '', false);
		$(".fromToTravelDropdownOther").autocomplete({
			source: function( request, response ) {
				var val=$('#travelType').val();
				if(!isEmpty(val)){
					var url='/get/claimCountries/'+val+'/'+request.term
					$.ajax({
					  url: url,
						headers:{
							"X-AUTH-TOKEN": window.authToken
						},
					  success: function(data) {
						  if(data.result){
							  response(data.countryStateCityList);
						  }
					  }
					});
				}
			  },
			  minLength: 2,
			  open: function() {
				$( this ).removeClass( "ui-corner-all" ).addClass( "ui-corner-top" );
			  },
			  close: function() {
				$( this ).removeClass( "ui-corner-top" ).addClass( "ui-corner-all" );
			  }
			});
	}
}//);

function claimSearchTableSuccess(response) {
	var data = '';
	if (!isEmpty(response.branches)) {
		data = response.branches;
		fillClaimSelectOptions(data, 'claimSearchBranch', 'id', 'name');
		data = response.projects;
		fillClaimSelectOptions(data, 'claimSearchProject', 'id', 'name');
		data = response.particulars;
		fillClaimSelectOptions(data, 'claimSearchParticular', 'id', 'name');
		data = response.specifics;
		fillClaimSelectOptions(data, 'claimSearchSpecific', 'id', 'name');
		data = response.travelModes;
		fillClaimSelectOptions(data, 'claimSearchTravelMode', 'id', 'name');
		data = response.accomodationTypes;
		fillClaimSelectOptions(data, 'claimSearchAccType', 'id', 'name');
	}
}

function fillClaimSelectOptions(data, elementId, value, text) {
	if (data.length > 0) {
		$('#' + elementId).html('<option value="">--Please Select--</option>');
		for (var i = 0; i < data.length; i++) {
			$('#' + elementId).append('<option value="' + data[i][value] + '">' + data[i][text] + '</option>');
		}
	}
}

function newClaimSuccess(){
	$("#whatYouWantToDoClaim").children().remove();
	var fullname =  $("#hiddenfullName").text();
	$("#whatYouWantToDoClaim").append('<option value="">---- Hi ' + fullname + '! Please select what you want to do? ----</option>');
	var jsonData = {};
	jsonData.email = $("#hiddenuseremail").text();
	var url="/claims/getUserRelatedClaimsAvailable"
	$.ajax({
      url: url,
	headers:{
			"X-AUTH-TOKEN": window.authToken
		},
      data:JSON.stringify(jsonData),
      type:"text",
      method:"POST",
      async : false,
      contentType:'application/json',
      success: function (data) {
		data = data.groups;
		if (!isEmpty(data)) {
			if (data.length > 0) {
				$('#whatYouWantToDoClaim').html('<option value="">---- Hi ' + fullname + '! Please select what you want to do? ----</option>');
				for (var i = 0; i < data.length; i++) {
					$('#whatYouWantToDoClaim').append('<option value="' + data[i].id + '">' + data[i].transactionPurpose + '</option>');
				}
			}
		}
      },
      error: function (xhr, status, error) {
      	if(xhr.status == 401){ doLogout();
      	}else if(xhr.status == 500){
    		swal("Error on fetching Transaction purposes!", "Please retry, if problem persists contact support team", "error");
    	}
      }
	});
}
/*
function newClaimSuccess(data) {
	var fullname =  $("#hiddenfullName").text();
	data = data.groups;
	if (!isEmpty(data)) {
		if (data.length > 0) {
			//$('#whatYouWantToDoClaim').html('<option value="">--------------------------------------Please Select---------------------------------------</option>'); Sunil

			$('#whatYouWantToDoClaim').html('<option value="">---- Hi ' + fullname + '! Please select what you want to do? ----</option>');
			for (var i = 0; i < data.length; i++) {
				$('#whatYouWantToDoClaim').append('<option value="' + data[i].id + '">' + data[i].transactionPurpose + '</option>');
			}
		}
	} else {
		//$('#whatYouWantToDoClaim').html('<option value="">--------------------------------------Please Select---------------------------------------</option>');
		$('#whatYouWantToDoClaim').html('<option value="">---- Hi ' + fullname + '! Please select what you want to do? ----</option>');
	}
}*/

function claimTableSuccess(jsonData){
	var url="/claims/getUserClaimBranchProject"
	$.ajax({
      url: url,
	headers:{
			"X-AUTH-TOKEN": window.authToken
		},
      data:JSON.stringify(jsonData),
      type:"text",
      method:"POST",
      async : false,
      contentType:'application/json',
      success: function (data) {
    	  var claimWhatDoYouWantToDoValue=$('#whatYouWantToDoClaim option:selected').val();
    		if (!isEmpty(data.branches)) {
    			if (data.branches.length > 0) {
    				$('.claimCommonTable .claimBranch').html('<option value="">-Please Select-</option>');
    				for (var i = 0; i < data.branches.length; i++) {
    					$('.claimCommonTable .claimBranch').append('<option value="' + data.branches[i].branchId + '">' + data.branches[i].branchName + '</option>');
    				}
    			}
    		} else {
    			$('.claimCommonTable .claimBranch').html('<option value="">-Please Select-</option>');
    		}
    		if (!isEmpty(data.projects)) {
    			if (data.projects.length > 0) {
    				$('.claimCommonTable .claimProject').html('<option value="">-Please Select-</option>');
    				for (var i = 0; i < data.projects.length; i++) {
    					$('.claimCommonTable .claimProject').append('<option value="' + data.projects[i].projectId + '">' + data.projects[i].projectName + '</option>');
    				}
    			}
    		} else {
    			$('.claimCommonTable .claimProject').html('<option value="">-Please Select-</option>');
    		}
    		if(claimWhatDoYouWantToDoValue==16){
    			var jsonClaimData = {};
    			jsonClaimData.email=$("#hiddenuseremail").text();
    			//ajaxCall('/claims/populateUserUnsettledTravelClaimAdvances', jsonClaimData, '', '', '', '', 'unsettledTravelClaimAdvancesSuccess', '', false);
    			var url="/claims/populateUserUnsettledTravelClaimAdvances"
    				$.ajax({
    			      url: url,
    				headers:{
    						"X-AUTH-TOKEN": window.authToken
    					},
    			      data:JSON.stringify(jsonData),
    			      type:"text",
    			      method:"POST",
    			      async : false,
    			      contentType:'application/json',
    			      success: function (data) {
    			    	  if(data.result){
    		    				$("#availableUnsettledClaimAdvances").children().remove();
    		    				$("#availableUnsettledClaimAdvances").append('<option value="">-Please Select-</option>');
    		    				for(var i=0;i<data.userUnsettledClaimAdvances.length;i++){
    		    					$("#availableUnsettledClaimAdvances").append('<option value="'+data.userUnsettledClaimAdvances[i].id+'">'+data.userUnsettledClaimAdvances[i].refNumberAmount+'</option>');
    		    				}
    		    			}else{
    		    				$("#availableUnsettledClaimAdvances").children().remove();
    		    				$("#availableUnsettledClaimAdvances").append('<option value="">-Please Select-</option>');
    		    			}
    			      },
    			      error: function (xhr, status, error) {
    			      	if(xhr.status == 401){ doLogout();
    			      	}else if(xhr.status == 500){
    			    		swal("Error on fetching Transaction purposes!", "Please retry, if problem persists contact support team", "error");
    			    	}
    			      }
    				});

    		}
    		if(claimWhatDoYouWantToDoValue==17){
    			var jsonClaimData={};
    			jsonClaimData.email=$("#hiddenuseremail").text();
    			//ajaxCall('/advance/userAdvanceForExpenseItems', jsonClaimData, '', '', '', '', 'advanceForExpenseSuccess', '', false);
    			var url="/advance/userAdvanceForExpenseItems"
				$.ajax({
			      url: url,
				headers:{
						"X-AUTH-TOKEN": window.authToken
					},
			      data:JSON.stringify(jsonData),
			      type:"text",
			      method:"POST",
			      async : false,
			      contentType:'application/json',
			      success: function (data) {
		    	  if(data.result){
	    				$("#expenseAdvanceItems").children().remove();
	    				$("#expenseAdvanceItems").append('<option value="">-Please Select-</option>');
	    				$("#expenseClaimItem").children().remove();
	    				$("#expenseClaimItem").append('<option value="">-Please Select-</option>');
	    				for(var i=0;i<data.expenseAdvanceItemsData.length;i++){
	    					$("#expenseAdvanceItems").append('<option value="'+data.expenseAdvanceItemsData[i].id+'">'+data.expenseAdvanceItemsData[i].itemName+'</option>');
	    					$("#expenseClaimItem").append('<option value="'+data.expenseAdvanceItemsData[i].id+'">'+data.expenseAdvanceItemsData[i].itemName+'</option>');
	    				}
	    			}
			      },
			      error: function (xhr, status, error) {
			      	if(xhr.status == 401){ doLogout();
			      	}else if(xhr.status == 500){
			    		swal("Error on fetching Transaction purposes!", "Please retry, if problem persists contact support team", "error");
			    	}
			      }
				});
    		}
    		if(claimWhatDoYouWantToDoValue==18){
    			var jsonClaimData={};
    			jsonClaimData.email=$("#hiddenuseremail").text();
    			//ajaxCall('/advance/populateUserUnsettledExpenseAdvances', jsonClaimData, '', '', '', '', 'unsettledExpensesAdvancesSuccess', '', false);
    			var url="/advance/populateUserUnsettledExpenseAdvances"
    				$.ajax({
    			      url: url,
    				headers:{
    						"X-AUTH-TOKEN": window.authToken
    					},
    			      data:JSON.stringify(jsonData),
    			      type:"text",
    			      method:"POST",
    			      async : false,
    			      contentType:'application/json',
    			      success: function (data) {
    			    	  if(data.result){
    		    				$("#availableUnsettledExpenseAdvances").children().remove();
    		    				$("#availableUnsettledExpenseAdvances").append('<option value="">-Please Select-</option>');
    		    				for(var i=0;i<data.expenseAdvanceUnsettledData.length;i++){
    		    					$("#availableUnsettledExpenseAdvances").append('<option value="'+data.expenseAdvanceUnsettledData[i].id+'">'+data.expenseAdvanceUnsettledData[i].refNumberAmount+'</option>');
    		    				}
    		    			}
    			      },
    			      error: function (xhr, status, error) {
    			      	if(xhr.status == 401){ doLogout();
    			      	}else if(xhr.status == 500){
    			    		swal("Error on fetching Transaction purposes!", "Please retry, if problem persists contact support team", "error");
    			    	}
    			      }
    				});
    		}
    		if(claimWhatDoYouWantToDoValue==19){
    			var jsonClaimData={};
    			jsonClaimData.email=$("#hiddenuseremail").text();
    			//ajaxCall('/advance/userAdvanceForExpenseItems', jsonClaimData, '', '', '', '', 'advanceForExpenseSuccess', '', false);
    			var url="/advance/userAdvanceForExpenseItems"
				$.ajax({
			      url: url,
				headers:{
						"X-AUTH-TOKEN": window.authToken
					},
			      data:JSON.stringify(jsonData),
			      type:"text",
			      method:"POST",
			      async : false,
			      contentType:'application/json',
			      success: function (data) {
		    	  if(data.result){
	    				$("#expenseAdvanceItems").children().remove();
	    				$("#expenseAdvanceItems").append('<option value="">-Please Select-</option>');
	    				$("#expenseClaimItem").children().remove();
	    				$("#expenseClaimItem").append('<option value="">-Please Select-</option>');
	    				for(var i=0;i<data.expenseAdvanceItemsData.length;i++){
	    					$("#expenseAdvanceItems").append('<option value="'+data.expenseAdvanceItemsData[i].id+'">'+data.expenseAdvanceItemsData[i].itemName+'</option>');
	    					$("#expenseClaimItem").append('<option value="'+data.expenseAdvanceItemsData[i].id+'">'+data.expenseAdvanceItemsData[i].itemName+'</option>');
	    				}
	    			}
			      },
			      error: function (xhr, status, error) {
			      	if(xhr.status == 401){ doLogout();
			      	}else if(xhr.status == 500){
			    		swal("Error on fetching Transaction purposes!", "Please retry, if problem persists contact support team", "error");
			    	}
			      }
				});
    		}
      },
      error: function (xhr, status, error) {
      	if(xhr.status == 401){ doLogout();
      	}else if(xhr.status == 500){
    		swal("Error on fetching Transaction purposes!", "Please retry, if problem persists contact support team", "error");
    	}
      }
	});
}

/*function claimTableSuccess(response) {
	var claimWhatDoYouWantToDoValue=$('#whatYouWantToDoClaim option:selected').val();
	var data = response.branches;
	if (!isEmpty(data)) {
		if (data.length > 0) {
			$('.claimCommonTable .claimBranch').html('<option value="">-Please Select-</option>');
			for (var i = 0; i < data.length; i++) {
				$('.claimCommonTable .claimBranch').append('<option value="' + data[i].branchId + '">' + data[i].branchName + '</option>');
			}
		}
	} else {
		$('.claimCommonTable .claimBranch').html('<option value="">-Please Select-</option>');
	}
	data = response.projects;
	if (!isEmpty(data)) {
		if (data.length > 0) {
			$('.claimCommonTable .claimProject').html('<option value="">-Please Select-</option>');
			for (var i = 0; i < data.length; i++) {
				$('.claimCommonTable .claimProject').append('<option value="' + data[i].projectId + '">' + data[i].projectName + '</option>');
			}
		}
	} else {
		$('.claimCommonTable .claimProject').html('<option value="">-Please Select-</option>');
	}
//	if(claimWhatDoYouWantToDoValue==15){
//		var jsonData = {};
//		jsonData.email = $("#hiddenuseremail").text();
//		jsonData.claimTravelType = "2";
//		ajaxCall('/claims/locationOnTravelType', jsonData, '', true, '', '', 'locationOnTravelTypeSuccess', '', false);
//	}
	if(claimWhatDoYouWantToDoValue==16){
		var jsonClaimData = {};
		jsonClaimData.email=$("#hiddenuseremail").text();
		ajaxCall('/claims/populateUserUnsettledTravelClaimAdvances', jsonClaimData, '', '', '', '', 'unsettledTravelClaimAdvancesSuccess', '', false);
	}
	if(claimWhatDoYouWantToDoValue==17){
		var jsonClaimData={};
		jsonClaimData.email=$("#hiddenuseremail").text();
		ajaxCall('/advance/userAdvanceForExpenseItems', jsonClaimData, '', '', '', '', 'advanceForExpenseSuccess', '', false);
	}
	if(claimWhatDoYouWantToDoValue==18){
		var jsonClaimData={};
		jsonClaimData.email=$("#hiddenuseremail").text();
		ajaxCall('/advance/populateUserUnsettledExpenseAdvances', jsonClaimData, '', '', '', '', 'unsettledExpensesAdvancesSuccess', '', false);
	}
	if(claimWhatDoYouWantToDoValue==19){
		var jsonClaimData={};
		jsonClaimData.email=$("#hiddenuseremail").text();
		ajaxCall('/advance/userAdvanceForExpenseItems', jsonClaimData, '', '', '', '', 'advanceForExpenseSuccess', '', false);
	}
}*/

function unsettledExpensesAdvancesSuccess(data){
	if(data.result){
		$("#availableUnsettledExpenseAdvances").children().remove();
		$("#availableUnsettledExpenseAdvances").append('<option value="">-Please Select-</option>');
		for(var i=0;i<data.expenseAdvanceUnsettledData.length;i++){
			$("#availableUnsettledExpenseAdvances").append('<option value="'+data.expenseAdvanceUnsettledData[i].id+'">'+data.expenseAdvanceUnsettledData[i].refNumberAmount+'</option>');
		}
	}
}

/*function advanceForExpenseSuccess(data){
	if(data.result){
		$("#expenseAdvanceItems").children().remove();
		$("#expenseAdvanceItems").append('<option value="">-Please Select-</option>');
		$("#expenseClaimItem").children().remove();
		$("#expenseClaimItem").append('<option value="">-Please Select-</option>');
		for(var i=0;i<data.expenseAdvanceItemsData.length;i++){
			$("#expenseAdvanceItems").append('<option value="'+data.expenseAdvanceItemsData[i].id+'">'+data.expenseAdvanceItemsData[i].itemName+'</option>');
			$("#expenseClaimItem").append('<option value="'+data.expenseAdvanceItemsData[i].id+'">'+data.expenseAdvanceItemsData[i].itemName+'</option>');
		}
	}
}*/
/*
function unsettledTravelClaimAdvancesSuccess(data){
	if(data.result){
		$("#availableUnsettledClaimAdvances").children().remove();
		$("#availableUnsettledClaimAdvances").append('<option value="">-Please Select-</option>');
		for(var i=0;i<data.userUnsettledClaimAdvances.length;i++){
			$("#availableUnsettledClaimAdvances").append('<option value="'+data.userUnsettledClaimAdvances[i].id+'">'+data.userUnsettledClaimAdvances[i].refNumberAmount+'</option>');
		}
	}else{
		$("#availableUnsettledClaimAdvances").children().remove();
		$("#availableUnsettledClaimAdvances").append('<option value="">-Please Select-</option>');
	}
}*/
/*Create Claim Ends*/


function giveAdvanceExpenseTxnRemarks(elem){
	//var parentTr=$(elem).parent().parent().parent('tr:first').attr('id'); Sunil
	var parentTr = $(elem).closest('tr').attr('id');
	var transactionEntityId=parentTr.substring(23, parentTr.length);
	var useremail=$("#hiddenuseremail").text();
	var transactionRmarks=$("#claimDetailsTable tr[id='"+parentTr+"'] textarea[name='txnRemarks']").val();
	var jsonData = {};
	jsonData.transactionPrimId = transactionEntityId;
	jsonData.email = useremail;
	jsonData.txnRmarks=transactionRmarks;
	jsonData.suppDoc="";
	jsonData.selectedApproverAction="7";
	if(transactionRmarks!=""){
	var url="/advanceExpense/approverAction";
		$.ajax({
			url : url,
			data : JSON.stringify(jsonData),
			type : "text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method : "POST",
			contentType : 'application/json',
			success : function(data) {
			},
			error: function(xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	}
}


function claimclarifyTransaction(elem){
	var parentTr = $(elem).closest('tr').attr('id');
//	var parentTr=$(elem).parent().parent().attr('id'); Sunil
	var selectedAction="6";
	var transactionEntityId=parentTr.substring(23, parentTr.length);
	var supportingDoc=$("#"+parentTr+" input[name='actionFileUpload']").val();
	var remarks=$("#"+parentTr+" textarea[id='txnRemarks']").val();
	if(confirm("Are You Sure,You Made Necessary Clarification About Claim Transaction!")){
		var selectedAddApproverVal="";var paymentBank="";
		var txnJsonData={};
		txnJsonData.email=$("#hiddenuseremail").text();
		txnJsonData.selectedApproverAction=selectedAction;
		txnJsonData.transactionPrimId=transactionEntityId;
		txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
		txnJsonData.suppDoc=supportingDoc;
		txnJsonData.txnRmarks=remarks;
		var url="/claims/approverAction";
		$.ajax({
			url: url,
			data:JSON.stringify(txnJsonData),
			type:"text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method:"POST",
			contentType:'application/json',
			success: function (data) {
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	}else{
		$("#"+parentTr+" textarea[id='txnRemarks']").focus();
	}
}



function reimbursementCompleteAccounting(elem){
	//alert("reimbursementCompleteAccounting");
	$(".btn-custom").attr("disabled", "disabled");
	$(".btn-customred").attr("disabled", "disabled");
	$(".approverAction").attr("disabled", "disabled");
	$("#completeTxn").attr("disabled", "disabled");

//	var parentTr=$(elem).parent().parent().attr('id'); Sunil
	var parentTr = $(elem).closest('tr').attr('id');
	var selectedAction="4";
	var transactionEntityId=parentTr.substring(23, parentTr.length);
	var supportingDoc=$("#"+parentTr+" input[name='actionFileUpload']").val();
	var remarks=$("#"+parentTr+" textarea[id='txnRemarks']").val();
	var paymentOption=$("#"+parentTr+" select[id='claimpaymentDetails'] option:selected").val();
	var bankDetails=$("#"+parentTr+" textarea[id='bankDetails']").val();
	var selectedAddApproverVal="";var paymentBank="";
	var txnJsonData={};
	txnJsonData.email=$("#hiddenuseremail").text();
	txnJsonData.selectedApproverAction=selectedAction;
	txnJsonData.transactionPrimId=transactionEntityId;
	txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
	txnJsonData.suppDoc=supportingDoc;
	txnJsonData.txnRmarks=remarks;
	txnJsonData.paymentDetails=paymentOption;
	if(paymentOption=="2"){
		paymentBank=$("#availableBank option:selected").val();
		if(typeof paymentBank!='undefined'){
			if(paymentBank == ""){
				// alert("Select a bank from list");
				swal("Empty data error!", "Select a bank from list", "error");
				enableTransactionButtons();
				return false;
			}
			var instrumentDate=$("#instrumentDate").val();
			if(instrumentDate == ""){
				// alert("Instrument Date cannot be empty.");
				swal("Invalid data error!", "Instrument Date cannot be empty.", "error");
				enableTransactionButtons();
				return false;
			}
			txnJsonData.txnPaymentBank=paymentBank;
			txnJsonData.txnInstrumentNum=$("#instrumentNumber").val();
			txnJsonData.txnInstrumentDate=instrumentDate;
	    }
	}
	txnJsonData.bankInf=bankDetails;
	var url="/reimbursement/reimbursementApproverAction";
	$.ajax({
		url: url,
		data:JSON.stringify(txnJsonData),
		type:"text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method:"POST",
		contentType:'application/json',
		success: function (data) {
			if(data.resultantAmount < 0){
						if(data.branchBankDetailEntered === false){
							swal("Insufficient balance in the bank account!", "Use alternative payment mode or infuse funds into the bank account. Current bank balance is:"+data.resultantAmount, "error");
							disableTransactionButtons();
							return false;
						}else{					
							swal("Bank Balance is in negative!", "This bank account type allows -ve balance so transaction is successful. Note current bank balance is:"+data.resultantAmount, "warning");
						}
			}
			if(data.resultantCash < 0){
				// alert("Insufficient balance in the cash account. Use alternative payment mode or infuse funds into the cash account. Effective cash balance is: " + data.resultantCash);
				swal("Insufficient balance in the cash account!", "Use alternative payment mode or infuse funds into the cash account. Effective cash balance is: " + data.resultantCash, "error");
			}
			if(data.resultantPettyCashAmount < 0){
				// alert("Insufficient balance in the petty cash account. Use alternative payment mode or infuse funds into petty cash account. Effective petty cash balance is: " + data.resultantPettyCashAmount);
				swal("Insufficient balance in the petty cash account!", "Use alternative payment mode or infuse funds into petty cash account. Effective petty cash balance is: " + data.resultantPettyCashAmount, "error");
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

function advanceExpenseCompleteAccounting(elem){
	//alert("advanceExpenseCompleteAccounting");
	$(".btn-custom").attr("disabled", "disabled");
	$(".btn-customred").attr("disabled", "disabled");
	$(".approverAction").attr("disabled", "disabled");
	$("#completeTxn").attr("disabled", "disabled");
//	var parentTr=$(elem).parent().parent().attr('id');
	var parentTr = $(elem).closest('tr').attr('id');
	var selectedAction="4";
	var transactionEntityId=parentTr.substring(23, parentTr.length);
	var supportingDoc=$("#"+parentTr+" input[name='actionFileUpload']").val();
	var remarks=$("#"+parentTr+" textarea[id='txnRemarks']").val();
	var paymentOption=$("#"+parentTr+" select[id='claimpaymentDetails'] option:selected").val();
	var bankDetails=$("#"+parentTr+" textarea[id='bankDetails']").val();
	var selectedAddApproverVal="";var paymentBank="";
	var txnJsonData={};
	txnJsonData.email=$("#hiddenuseremail").text();
	txnJsonData.selectedApproverAction=selectedAction;
	txnJsonData.transactionPrimId=transactionEntityId;
	txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
	txnJsonData.suppDoc=supportingDoc;
	txnJsonData.txnRmarks=remarks;
	txnJsonData.paymentDetails=paymentOption;
	if(paymentOption=="2"){
		paymentBank=$("#availableBank option:selected").val();
		if(typeof paymentBank!='undefined'){
			if(paymentBank == ""){
				// alert("Select a bank from list");
				swal("Empty data error!", "Select a bank from list", "error");
				enableTransactionButtons();
				return false;
			}
			var instrumentDate=$("#instrumentDate").val();
			if(instrumentDate == ""){
				// alert("Instrument Date cannot be empty.");
				swal("Empty data error!", "Instrument Date cannot be empty.", "error");
				enableTransactionButtons();
				return false;
			}
			txnJsonData.txnPaymentBank=paymentBank;
			txnJsonData.txnInstrumentNum=$("#instrumentNumber").val();
			txnJsonData.txnInstrumentDate=instrumentDate;
	    }
	}
	txnJsonData.bankInf=bankDetails;
	var url="/advanceExpense/approverAction";
	$.ajax({
		url: url,
		data:JSON.stringify(txnJsonData),
		type:"text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method:"POST",
		contentType:'application/json',
		success: function (data) {
			if(data.resultantAmount < 0){
						if(data.branchBankDetailEntered === false){
							swal("Insufficient balance in the bank account!", "Use alternative payment mode or infuse funds into the bank account. Current bank balance is:"+data.resultantAmount, "error");
							disableTransactionButtons();
							return false;
						}else{					
							swal("Bank Balance is in negative!", "This bank account type allows -ve balance so transaction is successful. Note current bank balance is:"+data.resultantAmount, "warning");
						}
					}
			if(data.resultantCash < 0){
				// alert("Insufficient balance in the cash account!","Use alternative payment mode or infuse funds into the cash account. Effective cash balance is: " + data.resultantCash);
				swal("Insufficient balance in the cash account!", "Use alternative payment mode or infuse funds into the cash account. Effective cash balance is: " + data.resultantCash, "warning");
			}
			if(data.resultantPettyCashAmount < 0){
				// alert("Insufficient balance in the petty cash account!", "Use alternative payment mode or infuse funds into petty cash account. Effective petty cash balance is: " + data.resultantPettyCashAmount);
				swal("Insufficient balance in the petty cash account!", "Use alternative payment mode or infuse funds into petty cash account. Effective petty cash balance is: " + data.resultantPettyCashAmount, "warning");

			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){
				doLogout();
			}else{
				swal("Error","Error: on complete accounting","error");
			}
		}
	});
}

function claimcompleteAccounting(elem){
	//alert("claimcompleteAccounting") ;
	$(".btn-custom").attr("disabled", "disabled");
	$(".btn-customred").attr("disabled", "disabled");
	$(".approverAction").attr("disabled", "disabled");
	$("#completeTxn").attr("disabled", "disabled");

	var parentTr = $(elem).closest('tr').attr('id');

	//var parentTr=$(elem).parent().parent().attr('id');
	var selectedAction="4";
	var transactionEntityId=parentTr.substring(23, parentTr.length);
	var supportingDocTmp = $("#"+parentTr+" select[name='txnViewListUpload'] option").map(function () {
		if($(this).val() != ""){
			return $(this).val();
		}
	}).get();
	var supportingDoc = supportingDocTmp.join(',');
	var remarks=$("#"+parentTr+" textarea[id='txnRemarks']").val();
	var paymentOption=$("#"+parentTr+" select[id='claimpaymentDetails'] option:selected").val();
	var bankDetails=$("#"+parentTr+" textarea[id='bankDetails']").val();
	var selectedAddApproverVal="";var paymentBank="";
	var txnJsonData={};
	txnJsonData.email=$("#hiddenuseremail").text();
	txnJsonData.selectedApproverAction=selectedAction;
	txnJsonData.transactionPrimId=transactionEntityId;
	txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
	txnJsonData.suppDoc=supportingDoc;
	txnJsonData.txnRmarks=remarks;
	txnJsonData.paymentDetails=paymentOption;
	if(paymentOption=="2"){
		paymentBank=$("#availableBank option:selected").val();
		if(typeof paymentBank!='undefined'){
			if(paymentBank == ""){
				// alert("Select a bank from list");
				swal("Empty data error!", "Select a bank from list", "error");
				enableTransactionButtons();
				return false;
			}
			var instrumentDate=$("#instrumentDate").val();
			if(instrumentDate == ""){
				// alert("Instrument Date cannot be empty.");
				swal("Empty data error!", "Instrument Date cannot be empty.", "error");
				enableTransactionButtons();
				return false;
			}
			txnJsonData.txnPaymentBank=paymentBank;
			txnJsonData.txnInstrumentNum=$("#instrumentNumber").val();
			txnJsonData.txnInstrumentDate=instrumentDate;
	    }
	}
	txnJsonData.bankInf=bankDetails;
	var url="/claims/approverAction";
	$.ajax({
		url: url,
		data:JSON.stringify(txnJsonData),
		type:"text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method:"POST",
		contentType:'application/json',
		success: function (data) {
			if(data.resultantAmount < 0){
				if(data.branchBankDetailEntered === false){
					swal("Insufficient balance in the bank account!", "Use alternative payment mode or infuse funds into the bank account. Current bank balance is:"+data.resultantAmount, "error");
					disableTransactionButtons();
					return false;
				}else{					
					swal("Negative Bank balance","Bank Balance is in -ve, But Due To Account Type You Are ALLOWED to Withdraw Amount Greeater Than The Available Amount In The Bank.","error");
				}
			}	
			/*if(data.resultantAmount < 0){
				alert("Insufficient balance in the bank account. Use alternative payment mode or infuse funds into the bank account. Effective Bank Balance is: " + data.resultantAmount);
			}*/
			if(data.resultantCash < 0){
				swal("Insufficient Balance!","Insufficient balance in the cash account. Use alternative payment mode or infuse funds into the cash account. Effective cash balance is: " + data.resultantCash,"error");
			}
			if(data.resultantPettyCashAmount < 0){
				swal("Insufficient Balance!","Insufficient balance in the petty cash account. Use alternative payment mode or infuse funds into petty cash account. Effective petty cash balance is: " + data.resultantPettyCashAmount,"error");
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}


/* not getting used */
function reimbursementClarifyTransaction(elem){
	//alert("reimbursementClarifyTransaction");
	var parentTr = $(elem).closest('tr').attr('id');
	var selectedAction="6";
	var transactionEntityId=parentTr.substring(23, parentTr.length);
	var supportingDoc=$("#"+parentTr+" input[name='actionFileUpload']").val();
	var remarks=$("#"+parentTr+" textarea[id='txnRemarks']").val();
	if(confirm("Are You Sure,You Made Necessary Clarification About Claim Transaction!")){
		var selectedAddApproverVal="";var paymentBank="";
		var txnJsonData={};
		txnJsonData.useremail=$("#hiddenuseremail").text();
		txnJsonData.selectedApproverAction=selectedAction;
		txnJsonData.transactionPrimId=transactionEntityId;
		txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
		txnJsonData.suppDoc=supportingDoc;
		txnJsonData.txnRmarks=remarks;
		var url="/reimbursement/reimbursementApproverAction";
		$.ajax({
			url: url,
			data:JSON.stringify(txnJsonData),
			type:"text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method:"POST",
			contentType:'application/json',
			success: function (data) {
				if(data.resultantAmount < 0){
						if(data.branchBankDetailEntered === false){
							swal("Insufficient balance in the bank account!", "Use alternative payment mode or infuse funds into the bank account. Current bank balance is:"+data.resultantAmount, "error");
							disableTransactionButtons();
							return false;
						}else{					
							swal("Bank Balance is in negative!", "This bank account type allows -ve balance so transaction is successful. Note current bank balance is:"+data.resultantAmount, "warning");
						}
					}
				if(data.resultantCash < 0){
					swal("Insufficient Balance!","Insufficient balance in the cash account. Use alternative payment mode or infuse funds into the cash account. Effective cash balance is: " + data.resultantCash,"warning");
				}
				if(data.resultantPettyCashAmount < 0){
					swal("Insufficient Balance!","Insufficient balance in the petty cash account. Use alternative payment mode or infuse funds into petty cash account. Effective petty cash balance is: " + data.resultantPettyCashAmount,"warning");
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	}else{
		$("#"+parentTr+" textarea[id='txnRemarks']").focus();
	}
}

function advanceExpenseClarifyTransaction(elem){
	//alert("advanceExpenseClarifyTransaction");
	var parentTr = $(elem).closest('tr').attr('id');
	var selectedAction="6";
	var transactionEntityId=parentTr.substring(23, parentTr.length);
	var supportingDoc=$("#"+parentTr+" input[name='actionFileUpload']").val();
	var remarks=$("#"+parentTr+" textarea[id='txnRemarks']").val();
	if(confirm("Are You Sure,You Made Necessary Clarification About Claim Transaction!")){
		var selectedAddApproverVal="";var paymentBank="";
		var txnJsonData={};
		txnJsonData.useremail=$("#hiddenuseremail").text();
		txnJsonData.selectedApproverAction=selectedAction;
		txnJsonData.transactionPrimId=transactionEntityId;
		txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
		txnJsonData.suppDoc=supportingDoc;
		txnJsonData.txnRmarks=remarks;
		var url="/advanceExpense/approverAction";
		$.ajax({
			url: url,
			data:JSON.stringify(txnJsonData),
			type:"text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method:"POST",
			contentType:'application/json',
			success: function (data) {
				if(data.resultantAmount < 0){
						if(data.branchBankDetailEntered === false){
							swal("Insufficient balance in the bank account!", "Use alternative payment mode or infuse funds into the bank account. Current bank balance is:"+data.resultantAmount, "error");
							disableTransactionButtons();
							return false;
						}else{					
							swal("Bank Balance is in negative!", "This bank account type allows -ve balance so transaction is successful. Note current bank balance is:"+data.resultantAmount, "warning");
						}
					}
				if(data.resultantCash < 0){
					swal("Insufficient Balance!","Insufficient balance in the cash account. Use alternative payment mode or infuse funds into the cash account. Effective cash balance is: " + data.resultantCash,"warning");
				}
				if(data.resultantPettyCashAmount < 0){
					swal("Insufficient Balance!","Insufficient balance in the petty cash account. Use alternative payment mode or infuse funds into petty cash account. Effective petty cash balance is: " + data.resultantPettyCashAmount,"warning");
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	}else{
		$("#"+parentTr+" textarea[id='txnRemarks']").focus();
	}
}

function giveReimbursementTxnRemarks(elem){
//	var parentTr=$(elem).parent().parent().parent('tr:first').attr('id');
	var parentTr = $(elem).closest('tr').attr('id');
	var transactionEntityId=parentTr.substring(23, parentTr.length);
	var useremail=$("#hiddenuseremail").text();
	var transactionRmarks=$("#claimDetailsTable tr[id='"+parentTr+"'] textarea[name='txnRemarks']").val();
	var jsonData = {};
	jsonData.transactionPrimId = transactionEntityId;
	jsonData.email = useremail;
	jsonData.txnRmarks=transactionRmarks;
	jsonData.suppDoc="";
	jsonData.selectedApproverAction="7";
	if(transactionRmarks!=""){
	var url="/reimbursement/reimbursementApproverAction";
		$.ajax({
			url : url,
			data : JSON.stringify(jsonData),
			type : "text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method : "POST",
			contentType : 'application/json',
			success : function(data) {
			},
			error: function(xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	}
}



function populateTravelEligibility(elem){
	var noOfDays=$(elem).val();
	if(noOfDays!=""){
		clearDivContents();
		var selectedappropriateDistance=$("#appropriateDistance").find('option:selected').val();
		if(selectedappropriateDistance==""){
			swal("Field can not be empty.","Please Select Approximate Distance","error");
			$("#appropriateDistance").focus();
			$(elem).blur();
			return true;
		}else{
			var typeOfCity=$("#typeOfCity").find('option:selected').val();
			var appropriateDistance=$("#appropriateDistance").find('option:selected').val();
			var jsonData = {};
			jsonData.email = $("#hiddenuseremail").text();
			jsonData.typeOfCityStr=typeOfCity;
			jsonData.appropriateDistanceStr=appropriateDistance;
			ajaxCall('/claims/displayTravelEligibility', jsonData, '', '', '', '', 'displayTravelEligibilitySuccess', '', false);
		}
	}
}


function displayTravelEligibilitySuccess(data){
	if(data.result){
		var totalTravelEligibilityConfAmount=parseFloat(0.0);
		for(var i=0;i<data.travelData.length;i++){
			$(".travelEligibilityDetailedConf").append(""+data.travelData[i].allowedTravelModes+"<br/>");
			$(".travelEligibilityDetailedConf").append("#Maximum One Way Fare: " +data.travelData[i].maxOneWayFare+"<br/>");
			var one=parseFloat(data.travelData[i].maxOneWayFare);
			var two=parseFloat(data.travelData[i].maxReturnFare);
			// Changed
			var totalValue=(one + two).toFixed(2);
			$(".travelAllowedAmountConf").append(""+totalValue+"<br/>");
			$(".travelEligibilityDetailedConf").append("#Maximum Return Fare: " +data.travelData[i].maxReturnFare+"");
			totalTravelEligibilityConfAmount=(parseFloat(totalTravelEligibilityConfAmount)+parseFloat(totalValue)).toFixed(2);
			// commented disscussed with Nagendra for Change GST-89 Issues
			/*if(one>=two){
				var floatValue=parseFloat(data.travelData[i].maxOneWayFare).toFixed(2);
				$(".travelAllowedAmountConf").append(""+floatValue+"<br/>");
				totalTravelEligibilityConfAmount=(parseFloat(totalTravelEligibilityConfAmount)+parseFloat(one)).toFixed(2);
			}
			$(".travelEligibilityDetailedConf").append("#Maximum Return Fare: " +data.travelData[i].maxReturnFare+"");
			if(one<two){
				var floatValue=parseFloat(data.travelData[i].maxReturnFare).toFixed(2)
				$(".travelAllowedAmountConf").append(""+floatValue+"<br/>");
				totalTravelEligibilityConfAmount=(parseFloat(totalTravelEligibilityConfAmount)+parseFloat(two)).toFixed(2);
			} */
		}
		for(var k=0;k<data.bnLData.length;k++){
			$(".bnlConfDetails").append(""+data.bnLData[k].accomodationType+"<br/>");
			var value=(parseFloat($("#totalDays").val())*(parseFloat(data.bnLData[k].maxPermittedRoomCostPerNight)+parseFloat(data.bnLData[k].maxPermittedFoodCostPerDay))).toFixed(2);
			totalTravelEligibilityConfAmount=(parseFloat(totalTravelEligibilityConfAmount)+parseFloat(value)).toFixed(2);
			$(".bnlConfDetails").append("#Maximum Permitted Room Cost Per Night: "+data.bnLData[k].maxPermittedRoomCostPerNight+"<br/>");
			$(".bnlConfDetails").append("#Maximum Permitted Food Cost Per Day: "+data.bnLData[k].maxPermittedFoodCostPerDay+"");
			$(".bnlAllowedAmountConf").append(""+value+"<br/>");
		}
		for(var j=0;j<data.dOthExpensesData.length;j++){
			var value=(parseFloat($("#totalDays").val())*(parseFloat(data.dOthExpensesData[j].dailyOtherExpenses))).toFixed(2);
			totalTravelEligibilityConfAmount=(parseFloat(totalTravelEligibilityConfAmount)+parseFloat(value)).toFixed(2);
			$(".otherExpensesDetailedConf").append(""+data.dOthExpensesData[j].dailyOtherExpenses+"");
			$(".otherExpensesAllowedAmountConf").append(""+value+"<br/>");
		}
		for(var z=0;z<data.fixedPerDIAMData.length;z++){
			var value=(parseFloat($("#totalDays").val())*(parseFloat(data.fixedPerDIAMData[z].dailyPerDIAM))).toFixed(2);
			totalTravelEligibilityConfAmount=(parseFloat(totalTravelEligibilityConfAmount)+parseFloat(value)).toFixed(2);
			$(".fixedPerDiamDetailedConf").append(""+data.fixedPerDIAMData[z].dailyPerDIAM+"");
			$("#existingUserTravelAdvance").val(data.fixedPerDIAMData[z].availableAdvance);
			$(".fixedPerDiamAllowedAmountConf").append(""+value+"<br/>");
		}
		$(".totalTravelEligibilityConf").append(""+totalTravelEligibilityConfAmount+"");
		for(var i=0;i<data.tGroupKLData.length;i++){
		  var parentTr="cRTAtrid";
		  var klcount=i+1;
  		  if(i==0){
  			  if(data.tGroupKLData[i].klIsMandatory=="1"){
  				  var followedkl=$("#"+parentTr+" div[class='travelTxnKlContents'] input[name='klfollowed']").attr('id');
  				  if(typeof followedkl=='undefined'){
  					  $("#"+parentTr+" div[class='travelTxnKlContents']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes &nbsp;&nbsp;<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
  				  }
  				  $("#"+parentTr+" div[class='travelTxnKlContents']").append('<br/>('+klcount+')<i class="icon-star"></i>'+data.tGroupKLData[i].klContent+'.');
  			  }
  			  if(data.tGroupKLData[i].klIsMandatory=="0"){
  				  $("#"+parentTr+" div[class='travelTxnKlContents']").append('<br/>('+klcount+')'+data.tGroupKLData[i].klContent+'.');
  			  }
  		  }else{
  			  if(data.tGroupKLData[i].klIsMandatory=="1"){
  				  var followedkl=$("#"+parentTr+" div[class='travelTxnKlContents'] input[name='klfollowed']").attr('id');
  				  if(typeof followedkl=='undefined'){
  					  $("#"+parentTr+" div[class='travelTxnKlContents']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes &nbsp;&nbsp;<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
  				  }
  				  $("#"+parentTr+" div[class='travelTxnKlContents']").append('<br/>('+klcount+')<i class="icon-star"></i>'+data.tGroupKLData[i].klContent+'.');
  			  }
  			  if(data.tGroupKLData[i].klIsMandatory=="0"){
  				  $("#"+parentTr+" div[class='travelTxnKlContents']").append('<br/>('+klcount+')'+data.tGroupKLData[i].klContent+'.');
  			  }
  		  }
		}
	}
}



function selectFromToClaimPlaces(elem){
	clearDivContents();
	var enteredNumber=$("#travelPlaceToVisit").val();
	if(enteredNumber==""){
		swal("Error in number of places.","Please Provide Number Of Places To Vist During This Business Trip.","error");
		$(elem).find('option:first').prop('selected','selected');
		$(elem).blur();
		return true;
	}else{
		$(".dynamicFromToTravelDropdown").each(function(){
			//alert("selectFromToClaimPlaces") ;
			var selectValue=$(this).find('select:first option:selected').val();
			var inputValue=$(this).find('input:first').val();
			if(selectValue=="" && inputValue==""){
				swal("Error in Travel To and From Location.","Please Select Or Provide All Travel From And To Location Before Proceeding.","error");
				$(this).find('select:first option:first').prop("selected","selected");
				$(this).find('select:first').focus();
				$(elem).find('option:first').prop('selected','selected');
				$(elem).blur();
				return false;
			}
		});
		//$(elem).blur();
	}

}

function checkForAllFromToOption(elem){
	clearDivContents();
//	alert("checkForAllFromToOption");
	var enteredNumber=$("#travelPlaceToVisit").val();
	if(enteredNumber==""){
		swal("Error in number of places.","Please Provide Number Of Places To Vist During This Business Trip.");
		$(elem).find('option:first').prop('selected','selected');
		$(elem).blur();
		return true;
	}else{
		$(".dynamicFromToTravelDropdown").each(function(){
			var selectValue=$(this).find('select:first option:selected').val();
			var inputValue=$(this).find('input:first').val();
			if(selectValue=="" && inputValue==""){
				swal("Error in number of places.","Please Select Or Provide All Travel From And To Location Before Proceeding.");
				$(this).find('select:first option:first').prop("selected","selected");
				$(this).find('select:first').focus();
				$(elem).find('option:first').prop('selected','selected');
				$(elem).blur();
				return false;
			}
		});
		$(elem).blur();
	}
}


function listAllBranchBankAccountsClaims(elem){
	$(".dynmBnchBankActList").remove();
	var text=$("#whatYouWantToDoClaim").find('option:selected').text();
	//var parentTr=$(elem).parent().parent().parent().attr('id');  //Sunil
	//alert(parentTr);
	var parentTr = $(elem).closest('tr').attr('id');
	// alert(parentTr);
	var modeOption=$("#"+parentTr+" select[id='claimpaymentDetails'] option:selected").val();
	if(modeOption=="2"){
		var transactionEntityId=parentTr.substring(23, parentTr.length);
		if(!transactionEntityId==""){
			var jsonData={};
			var useremail=$("#hiddenuseremail").text();
			jsonData.email = useremail;
			jsonData.txnEntityId=transactionEntityId;
			ajaxCall('/claimsbranch/bankAccountsForPayment', jsonData, '', '', '', '', 'claimbankAccountsForPaymentSuccess', '', false);
		}
	}
}

function claimbankAccountsForPaymentSuccess(data){
	var parentTr="claimsTransactionEntity"+data.entityId+"";
	if(data.result){
		$(".dynmBnchBankActList").remove();
		$("#"+parentTr+" select[id='claimpaymentDetails']").after('<div class="dynmBnchBankActList"><select name="availableBank" id="availableBank"><option value="">Select Bank</option></select></div>');
		if(data.availableBranchBankData.length>0){
			for(var i=0;i<data.availableBranchBankData.length;i++){
				$("#"+parentTr+" select[id='availableBank']").append('<option value="'+data.availableBranchBankData[i].bnchBankAccountsId+'">'+data.availableBranchBankData[i].bnchBankAccountsName+'</option>')
			}
			addBankInstrumentDetail(parentTr);
		}else{
			$(".dynmBnchBankActList").remove();
			swal("Account not configured.","Bank account is not configured for the branch for which you want to process the transaction.","error");
			$("#"+parentTr+" select[id='claimpaymentDetails']").find('option:first').prop("selected","selected");
		}
	}else{
		$(".dynmBnchBankActList").remove();
		swal("Account not configured.","Bank account is not configured for the branch for which you want to process the transaction.","error");
		$("#"+parentTr+" select[id='claimpaymentDetails']").find('option:first').prop("selected","selected");
	}
}

/* Claims Functionality Starts*/
function cancelAndClearDivContents(){
	var parentTr="cREAtrid";
	$(".travelEligibilityDetailedConf").html("");
	$(".bnlConfDetails").html("");
	$(".otherExpensesDetailedConf").html("");
	$(".fixedPerDiamDetailedConf").html("");
	$(".totalTravelEligibilityConf").html("");
	$(".travelTxnKlContents").html("");
	$("#maxAdvanceEligibleTravelAdvanceClaim").html("");
	$("#travelTxnKlContents").html("");
	$("#moreSupportingDocDiv").html("");
	$(".travelAllowedAmountConf").html('');
	$(".bnlAllowedAmountConf").html('');
	$(".fixedPerDiamAllowedAmountConf").html('');
	$(".otherExpensesAllowedAmountConf").html('');
	$(".totalAdvanceAgainstTxn").html("");
	$(".settledTillDate").html("");
	$(".settledAgainstAdvanceInOtherTxn").html("");
	$(".balanceUnsettledAgainstThisTxn").html("");
	$(".claimCommonTable").each(function(){
		$('input[type="text"]').val("");
		$('select').find('option:first').prop("selected","selected");
		$('textarea').val("");
		$(this).hide();
	});
	$("#existingAdvanceClaimsTxn").children().remove();
	$("#existingAdvanceClaimsTxn").append('-Please Select-');
	$("#createClaim").hide();
	$("#"+parentTr+" .expenseAdvanceMaxPermittedAdvance").html("");
	$("#"+parentTr+" .expenseAdvanseUnsettledExistingAdvances").html("");
	$("#"+parentTr+" .expenseAdvanceRequestInProgress").html("");
	$("div[class='expenseAdvanceTxnKlContents']").html("");
	$("#expenseAdvanceTotalAdvance").val("");
	$("#expenseAdvanceRequired").val("");
	$("#travelType").find('option:first').prop("selected","selected");
	$(".monLimitReimbursement").text("");
	$(".reimbursementAccountedDiv").text("");
	$(".reimbursementInProgressDiv").text("");
}

function clearIfClaimBranchEmpty(elem){
	var value=$(elem).val();
	if(value==""){
		cancelAndClearDivContents();
	}
}

function clearDivContents(){
	$(".travelEligibilityDetailedConf").html("");
	$(".bnlConfDetails").html("");
	$(".otherExpensesDetailedConf").html("");
	$(".fixedPerDiamDetailedConf").html("");
	$(".totalTravelEligibilityConf").html("");
	$(".travelTxnKlContents").html("");
	$("#maxAdvanceEligibleTravelAdvanceClaim").html("");
	$(".otherExpensesAllowedAmountConf").html('');
	$(".totalAdvanceAgainstTxn").html("");
	$(".settledTillDate").html("");
	$(".settledAgainstAdvanceInOtherTxn").html("");
	$(".balanceUnsettledAgainstThisTxn").html("");
	$("#travelTxnKlContents").html("");
	$(".travelAllowedAmountConf").html('');
	$(".bnlAllowedAmountConf").html('');
	$("#moreSupportingDocDiv").html("");
	$(".fixedPerDiamAllowedAmountConf").html('');
	$("#advanceRequestedOnThistxn").val("");
	$("#totalTravelAdvanceTxnAmount").val("");
	$("#existingUserTravelAdvance").val("");
	$("#adjustmentFromExistingAdvance").val("");
	$("#existingAdvanceClaimsTxn").children().remove();
	$("#existingAdvanceClaimsTxn").append('-Please Select-');
}

function populateFromToBasedOnTravelType(elem){
	var travelTypeValue=$(elem).val();
	var claimTxnBnch=$(".claimBranch option:selected").val();
	if(claimTxnBnch==""){
		swal("Error in Branch","Please Select Transaction Branch For Travel Claim","error");
		$(elem).find('option:first').prop("selected","selected");
		$(elem).blur();
		return true;
	}else{
		if(travelTypeValue!=""){
			if(travelTypeValue=="1"){
				var jsonData = {};
				jsonData.email = $("#hiddenuseremail").text();
				jsonData.claimTravelType = travelTypeValue;
				ajaxCall('/claims/locationOnTravelType', jsonData, '', true, '', '', 'locationOnTravelTypeSuccess', '', true);
			}
			if(travelTypeValue=="2"){
				if(internationalOptionItems==""){
					var jsonData = {};
					jsonData.email = $("#hiddenuseremail").text();
					jsonData.claimTravelType = travelTypeValue;
					ajaxCall('/claims/locationOnTravelType', jsonData, '', true, '', '', 'locationOnTravelTypeSuccess', '', true);
				}else{
					$(".fromToTravelDropdown").children().remove();
					$(".fromToTravelDropdown").append('<option value="">--Please Select--</option>');
					$(".fromToTravelDropdown").append(internationalOptionItems);
				}
			}
		}else{
			$(".fromToTravelDropdown").children().remove();
			$(".fromToTravelDropdown").append('<option value="">--Please Select--</option>');
			clearDivContents();
		}
	}
}

function locationOnTravelTypeSuccess(data){
	if(data.result){
		$(".fromToTravelDropdown").children().remove();
		$(".fromToTravelDropdown").append('<option value="">--Please Select--</option>');
		for(var i=0;i<data.fromToOptionData.length;i++){
			if (data.fromToOptionData[i].type==1){
				$(".fromToTravelDropdown").append(data.fromToOptionData[i].fromToOptionStr);
			}else if (data.fromToOptionData[i].type==2){
//				internationalOptionItems=data.fromToOptionData[i].fromToOptionStr;
				$(".fromToTravelDropdown").append(data.fromToOptionData[i].fromToOptionStr);
			}
		}
	}else{
	   swal("Error!!","Problem In Request Processing","error");
		return true;
	}
}

function displayNumberOfPlaceToTravel(elem){
	clearDivContents();
	var enteredNumber=$(elem).val();
	$(".numberOfFromToTravelOption").html("");
	if(enteredNumber!=""){
		for(var i=0;i<parseInt(enteredNumber);i++){
			$(".numberOfFromToTravelOption").append('<div class="dynamicFromToTravelDropdown">From:<br/><select class="fromToTravelDropdown" name="fromToTravelDropdown" style="display:none;"></select><br/>'+
			'<input name="fromToTravelDropdownOther" class="fromToTravelDropdownOther" placeholder="Other" style="width:120px;"/></div>');
			$(".numberOfFromToTravelOption").append('<div class="dynamicFromToTravelDropdown">To:<br/><select class="fromToTravelDropdown" name="fromToTravelDropdown" style="display:none;"></select><br/>'+
			'<input name="fromToTravelDropdownOther" class="fromToTravelDropdownOther" placeholder="Other" style="width:120px;"/></div>');
		}
		$("#travelType").trigger('change');
	}
}

function selectTravelType(elem){
	clearDivContents();
	var selectedTravelType=$("#travelType option:selected").val();
	if(selectedTravelType==""){
	    swal("Invalid!","Please Select Your Travel Type","error");
		$(elem).blur();
		return true;
	}
}

function checkAppropriateDistanceSelection(elem){

	var valueTotalDays=$("#totalDays").val();
	if(valueTotalDays==""){
		clearDivContents();
		var selectedAppropriateDistance=$("#appropriateDistance option:selected").val();
		if(selectedAppropriateDistance==""){
			swal("Invalid!!","Please Select Approximate Distance","error");
			$("#appropriateDistance").focus();
			$(elem).blur();
			return true;
		}
	}

}

function selectClaimTxnBranch(elem){
	clearDivContents();
	var claimTxnBnch=$(".claimBranch option:selected").val();
	if(claimTxnBnch==""){
		swal("Invalid!","Please Select Transaction Branch For Travel Claim","error");
		$(elem).find('option:first').prop("selected","selected");
		$(elem).blur();
		return true;
	}
}




function selectTypeOfCity(elem){
	clearDivContents();
	var selectedTypeOfCity=$("#typeOfCity").find('option:selected').val();
	if(selectedTypeOfCity==""){
		swal("Invalid!","Please Select The Type Of City","error");
		$("#typeOfCity").focus();
		$(elem).blur();
		return true;
	}
}

function adjustFromAvailableAdvance(elem){
	var adjustmentValue=$(elem).val();
	if(adjustmentValue!=""){
		var existingadvance=$("#existingUserTravelAdvance").val();
		if(existingadvance==""){
			swal("Pleae Select !","Travel Advance Amount Not Available For Adjustment.","error");
			$(elem).val("");
			return true;
		}
		if(existingadvance!=""){
			if(parseFloat(adjustmentValue)>parseFloat(existingadvance)){
				swal("Error","You Cannot Adjust Travel Advance Amount Greater Then The Available Travel Advance Amount","error");
				$(elem).val("");
				return true;
			}
		}
	}
}

function displayTotalAdvanceTxnAmount(elem){
	$("#totalTravelAdvanceTxnAmount").val("");
	var enteredAdvanceRequested=$(elem).val();
	var selectedExistingClaims=$("#existingAdvanceClaimsTxn option:selected").val();
	if(enteredAdvanceRequested!=""){
		var availableTravelAdvance=$("#existingUserTravelAdvance").val();
		var advanceAdjustment=$("#adjustmentFromExistingAdvance").val();
		if(advanceAdjustment!="" && selectedExistingClaims==""){
			swal("Error","Please Select The Travel Claim Exiating Transaction's From Which You Want To Adjust Your Advance.","error");
			$(elem).val("");
			return true;
		}
		var value=parseFloat(0.0);
		if(enteredAdvanceRequested!="" && advanceAdjustment!=""){
			var first=parseFloat(enteredAdvanceRequested);
			var second=parseFloat(advanceAdjustment);
			var third=parseFloat(availableTravelAdvance);
			value=first+third-second;
			var totalValue=parseFloat(first+second);
			var totalTravelEligibility=$(".totalTravelEligibilityConf").text();
			var totalEligibility=parseFloat(totalTravelEligibility);
			if(totalValue>totalEligibility){
				swal("Error","Sorry, please input lower amount since you are requesting for an amount which exceeds your eligibility.","error");
				$("#totalTravelAdvanceTxnAmount").val("");
				$("#advanceRequestedOnThistxn").val("");
				return true;
			}
		}
		if(enteredAdvanceRequested!="" && advanceAdjustment==""){
			value=parseFloat(enteredAdvanceRequested).toFixed(2);
			var totalValue=parseFloat(value);
			var totalTravelEligibility=$(".totalTravelEligibilityConf").text();
			var totalEligibility=parseFloat(totalTravelEligibility);
			if(totalValue>totalEligibility){
				swal("Error","Sorry, please input lower amount since you are requesting for an amount which exceeds your eligibility.","error");
				$("#totalTravelAdvanceTxnAmount").val("");
				$("#advanceRequestedOnThistxn").val("");
				return true;
			}
		}
		var totalAmonut=parseFloat(value).toFixed(2);
		$("#totalTravelAdvanceTxnAmount").val(totalAmonut);
	}else{
		$("#totalTravelAdvanceTxnAmount").val("");
	}
}

function populateExistingTravelClaimAdvanceAndAlert(elem){
	var advanceAdjustment=$("#adjustmentFromExistingAdvance").val();
	if(advanceAdjustment!=""){
		var jsonData = {};
		jsonData.email = $("#hiddenuseremail").text();
		jsonData.advanceAdjustmentAmount=advanceAdjustment;
		ajaxCall('/claims/exitingClaimsAdvanceTxnRefAndAmount', jsonData, '', '', '', '', 'exitingClaimsAdvanceTxnRefAndAmountSuccess', '', false);
	}
}

function exitingClaimsAdvanceTxnRefAndAmountSuccess(data){
	if(data.result){
		$("#existingAdvanceClaimsTxn").children().remove();
		$("#existingAdvanceClaimsTxn").append('<option value="">-Please Select-</option>');
		for(var i=0;i<data.existingClimsData.length;i++){
			$("#existingAdvanceClaimsTxn").append('<option value="'+data.existingClimsData[i].id+'">'+data.existingClimsData[i].refNumberAmount+'</option>');
		}
		swal("Error","Please Select The Travel Claim Exiating Transaction's From Which You Want To Adjust Your Advance.","error");
		$("#existingAdvanceClaimsTxn").focus();
		return true;
	}
}

function validateReimbursementAmount(elem){
	var expenseReimbursementAmountRequired=$(elem).val();
	var monLimitReimbursement=$(".monLimitReimbursement").text();
	var reimbursementAccountedDiv=$(".reimbursementAccountedDiv").text();
	var reimbursementInProgressDiv=$(".reimbursementInProgressDiv").text();
	var maxAmountThatCanBeReimburse=parseFloat(0.0);
	if(monLimitReimbursement!=""){
		var newMonLimitReimbursement=parseFloat(monLimitReimbursement);
		maxAmountThatCanBeReimburse=maxAmountThatCanBeReimburse+newMonLimitReimbursement;
	}
	if(reimbursementAccountedDiv!=""){
		var newReimbursementAccountedDiv=parseFloat(reimbursementAccountedDiv);
		maxAmountThatCanBeReimburse=maxAmountThatCanBeReimburse-newReimbursementAccountedDiv;
	}
	if(reimbursementInProgressDiv!=""){
		var newReimbursementInProgressDiv=parseFloat(reimbursementInProgressDiv);
		maxAmountThatCanBeReimburse=maxAmountThatCanBeReimburse-newReimbursementInProgressDiv;
	}
	var maxAmountThatCanBeReimburseInThisTxn=maxAmountThatCanBeReimburse.toFixed(2);
	var amountEnteredForReimbursement=parseFloat(expenseReimbursementAmountRequired).toFixed(2);
	if(parseFloat(amountEnteredForReimbursement) > parseFloat(maxAmountThatCanBeReimburseInThisTxn)){
		$("#expenseReimbursementAmountRequired").val("");
		swal("Error","You cannot reimburse amount greater than "+maxAmountThatCanBeReimburseInThisTxn+ " this month.","error");
		return true;
	}
}


function submitForApprovalClaim(whatYouWantToDo,whatYouWantToDoVal,parentTr){
	/*$(".btn-custom").attr("disabled", "disabled");
	$(".btn-customred").attr("disabled", "disabled");
	$(".approverAction").attr("disabled", "disabled");
	$("#completeTxn").attr("disabled", "disabled");*/
	if(whatYouWantToDo=="Request For Travel Advance"){
		var txnBranch="";var txnProject="";var travelType="";var noOfPlacesToVisit="";var placesSelectedOrEntered="";var txnEntityID="";
		var typeOfCity="";var appropriateDiatance="";var totalDays="";var travelDetailedConfDescription="";var existingAdvance="";var userAdvanveEligibility="";var adjustedAdvance="";
		var selectedExistingClaimAdvanceTxn="";var enteredAdvance="";var totalAdvance="";var klmandatoryfollowednotfollowed="";var klcontents="";var purposeOfVisit="";var txnRemarks="";var supportingDoc="";
		enteredAdvance=$("#advanceRequestedOnThistxn").val();
		adjustedAdvance=$("#adjustmentFromExistingAdvance").val();
		selectedExistingClaimAdvanceTxn=$("#cRTAtrid select[id='existingAdvanceClaimsTxn'] option:selected").val();
		totalAdvance=$("#totalTravelAdvanceTxnAmount").val();
		if(selectedExistingClaimAdvanceTxn==""){
			swal("Error","Please Select The Travel Advance Claim Transaction From Which You Want To Adjust Your Advance","error");
			$("#cRTAtrid select[id='existingAdvanceClaimsTxn'] option:selected").focus();
			$(".btn-custom").removeAttr("disabled");
			$(".btn-customred").removeAttr("disabled");
			$(".approverAction").removeAttr("disabled");
			$("#completeTxn").removeAttr("disabled");
			return true;
		}
		if(enteredAdvance=="" || totalAdvance==""){
			swal("Error","Please Complete Travel Advance Claims Application Before Submitting For Approval","error");
			$(".btn-custom").removeAttr("disabled");
			$(".btn-customred").removeAttr("disabled");
			$(".approverAction").removeAttr("disabled");
			$("#completeTxn").removeAttr("disabled");
			return true;
		}
		var followedkl=$("#"+parentTr+" div[class='travelTxnKlContents'] input[name='klfollowed']").attr('id');
		if(typeof followedkl!='undefined'){
			var klfollowednotfollowed=$("#"+parentTr+" div[class='travelTxnKlContents'] input[name='klfollowed']").is(':checked');
			if(klfollowednotfollowed==false){
				swal("Error","Please read and accept the madatory transaction rules before submitting travel transaction for approval","error");
				$(".btn-custom").removeAttr("disabled");
				$(".btn-customred").removeAttr("disabled");
				$(".approverAction").removeAttr("disabled");
				$("#completeTxn").removeAttr("disabled");
				return true;
			}
		}
		txnEntityID = $("#"+parentTr).attr("name");
		klcontents=$('div[class="travelTxnKlContents"]').text();
		txnBranch=$("#cRTAtrid select[class='claimBranch'] option:selected").val();
		txnProject=$("#cRTAtrid select[class='claimProject'] option:selected").val();
		travelType=$("#travelType option:selected").text();
		noOfPlacesToVisit=$("#travelPlaceToVisit").val();
		$(".dynamicFromToTravelDropdown").each(function(){
			var fromDropDown=$(this).find('select[name="fromToTravelDropdown"] option:selected').val();
			if(fromDropDown!=""){
					placesSelectedOrEntered+=fromDropDown+",";
			}else{
				var fromInput=$(this).find('input[name="fromToTravelDropdownOther"]').val();
				if(fromInput!=""){
					placesSelectedOrEntered+=fromInput+",";
				}
			}
		});
		placesSelectedOrEntered=placesSelectedOrEntered.substring(0, placesSelectedOrEntered.length-1);
		typeOfCity=$("#typeOfCity option:selected").text();
		appropriateDiatance=$("#appropriateDistance option:selected").text();
		totalDays=$("#totalDays").val();
		travelDetailedConfDescription=$(".travelDetailedConfDescription").text();
		userAdvanveEligibility=$(".maxAdvanceEligibleTravelAdvanceClaim").text();
		existingAdvance=$("#existingUserTravelAdvance").val();
		adjustedAdvance=$("#adjustmentFromExistingAdvance").val();
		enteredAdvance=$("#advanceRequestedOnThistxn").val();
		totalAdvance=$("#totalTravelAdvanceTxnAmount").val();
		var followedkl=$("#"+parentTr+" div[class='travelTxnKlContents'] input[name='klfollowed']").attr('id');
		if(typeof followedkl!='undefined'){
			if($("#"+parentTr+" div[class='travelTxnKlContents'] input[id='klfollowedyes']").is(':checked')==true){
				klmandatoryfollowednotfollowed="1";
			}
			if($("#"+parentTr+" div[class='travelTxnKlContents'] input[id='klfollowedno']").is(':checked')==true){
				klmandatoryfollowednotfollowed="0";
			}
		}
		purposeOfVisit=$("#purposeOfVisitTravelAdvance option:selected").text();
		var purposeOfVisitValue=$("#purposeOfVisitTravelAdvance option:selected").val();
		if(purposeOfVisitValue == ""){
			swal("Invalid!","Choose Purpose of Visit","error");
			enableTransactionButtons();
			return false;
		}
		if(purposeOfVisitValue==15){
			var othersInputValue=$("#othersPurposeOfVisit").val();
			if(othersInputValue==""){
				swal("Invalid!","Please Specify Other Purpose Of Visit","error");
				$("#othersPurposeOfVisit").focus();
				enableTransactionButtons();
				return true;
			}else{
				purposeOfVisit=$("#othersPurposeOfVisit").val();
			}
		}
		txnRemarks=$("#claimRequestTravelAdvanceRemarks").val();
		var supportingDocTmp = $("#"+parentTr+" select[name='claimRequestTravelAdvanceuploadSuppDocs'] option").map(function () {
			if($(this).val() != ""){
				return $(this).val();
			}
		}).get();
		var supportingDoc = supportingDocTmp.join(',');
		var proceed=false;
		var userCheckRule=checkForConfiguredApproverAndAccountants(txnBranch,"",enteredAdvance,"travelclaims");
		if(userCheckRule==true){
			proceed=true;
		}
		if(userCheckRule==false){
			proceed=false;
		}
		if(proceed){
		var claimJsosData={};
			claimJsosData.email = $("#hiddenuseremail").text();
			claimJsosData.txnEntityID = txnEntityID;
			claimJsosData.claimtxnBranch=txnBranch;
			claimJsosData.claimtxnProject=txnProject;
			claimJsosData.claimtravelType=travelType;
			claimJsosData.claimnoOfPlacesToVisit=noOfPlacesToVisit;
			claimJsosData.claimplacesSelectedOrEntered=placesSelectedOrEntered;
			claimJsosData.claimtypeOfCity=typeOfCity;
			claimJsosData.claimappropriateDiatance=appropriateDiatance;
			claimJsosData.claimtotalDays=totalDays;
			claimJsosData.claimtravelDetailedConfDescription=travelDetailedConfDescription;
			claimJsosData.claimuserAdvanveEligibility=userAdvanveEligibility;
			claimJsosData.claimexistingAdvance=existingAdvance;
			claimJsosData.claimadjustedAdvance=adjustedAdvance;
			claimJsosData.claimSelectedClaimTxnForAdjustment=selectedExistingClaimAdvanceTxn;
			claimJsosData.claimenteredAdvance=enteredAdvance;
			claimJsosData.claimtotalAdvance=totalAdvance;
			claimJsosData.claimklmandatoryfollowednotfollowed=klmandatoryfollowednotfollowed;
			claimJsosData.claimklcontents=klcontents;
			claimJsosData.claimpurposeOfVisit=purposeOfVisit;
			claimJsosData.claimtxnRemarks=txnRemarks;
			claimJsosData.claimsupportingDoc=supportingDoc;
			claimJsosData.claimTxnPurposeText=whatYouWantToDo
			claimJsosData.claimTxnPurposeVal=whatYouWantToDoVal;
			ajaxCall('/claims/submitForApproval', claimJsosData, '', '', '', '', 'claimSubmitForApprovalSuccess', '', true);
			viewTransactionData(data); // to render the updated transaction recored
		}
	}else if(whatYouWantToDo=="Settle Travel Advance"){
		$('.clmSettleModule').slideUp('fast');
		
		var dueFromCompany = $("#dueFromCompanyAmount").val();
		var dueToCompany = $("#dueToCompanyAmount").val();
		if(dueFromCompany=="" && dueToCompany==""){
			$(".btn-custom").removeAttr("disabled");
			$(".btn-customred").removeAttr("disabled");
			$(".approverAction").removeAttr("disabled");
			$("#completeTxn").removeAttr("disabled");
			swal("Invalid!","Please Properly Enter Your Expenses Incurred","error");
			return true;
		}
		/*if(dueToCompany!=""){ this check is alredy there in calculateTotalExpenseIncurredOnThisTxn, which doesn't allow to enter more amt
			var amountReturnInCaseOfDueToCompany=$("#balanceDueToTheCompanyAmount").val();
			var value1=parseFloat(dueToCompany);
			var value2=parseFloat(amountReturnInCaseOfDueToCompany);
			if(value2>value1){
				alert("Amount Being Returned To The Company Cannot Be Greater Than The Amount Due To The Company.");
				$("#balanceDueToTheCompanyAmount").val("");
				$(".btn-custom").removeAttr("disabled");
				$(".btn-customred").removeAttr("disabled");
				$(".approverAction").removeAttr("disabled");
				$("#completeTxn").removeAttr("disabled");
				return true;
			}
		}*/
		
		
		
		var txnEntityID = $("#"+parentTr).attr("name");
		var availableUnsettledClaimAdvances=$("#availableUnsettledClaimAdvances option:selected").val();
		var balanceUnsettledAgainstThisTxn=$("#balanceUnsettledAgainstThisTxn").text();
		var unsettledClaimAdvancesDetails=$(".unsettledClaimAdvancesDetails").text();
		
		var userTravelExpenditure= $("#expenseIncurredTravel").val();  // Traval Expenses Total
		var userBnLExpenditure = $("#expenseIncurredBnL").val();		// Boarding & Lodging  Total
		var expenseIncurredOtherExpenses = $("#expenseIncurredOtherExpenses").val();		// other  Total
		var expenseIncurredFixedPerDiam=$("#expenseIncurredFixedPerDiam").val();		//Fixed Per DM  Total
		var totalExpenseIncurred=$("#totalExpenseIncurred").val();  // Total 
		/*
		if(balanceUnsettledAgainstThisTxn != "") {
			 var balanceUnsettled=parseFloat(balanceUnsettledAgainstThisTxn);
			 if(totalExpenseIncurred > balanceUnsettled) {
			 	swal("Incorrect Expenses Amount!", "Expenses amount is more than Advance available.", "error");
				return false;
			 }
		}else {
			swal("Incorrect Balance Unsettled!", "Balance Unsettled is not valid.", "error");
			return false;
		}
		*/
		var expenseDetails = fetchClaimMultiItemData("clmTravelExpensesModule");
		var lodgingAndBoardDetails = fetchClaimMultiItemData("clmLodgingExpensesModule");
		var otherExpensesDetails = fetchClaimMultiItemData("clmOtherExpensesModule");
		var fixedPerDiamDetails = fetchClaimMultiItemData("clmFixedDIAMModule");
		
		var travelExpenceTotalAmt = $("#clmTravelExpensesTR").find(".clmExpenceAmt").val();
		var travelExpenceTotalTax = $("#clmTravelExpensesTR").find(".clmExpenceTax").val();
		
		var lnbExpenceTotalAmt = $("#clmLodgingExpensesTR").find(".clmExpenceAmt").val();
		var lnbExpenceTotalTax = $("#clmLodgingExpensesTR").find(".clmExpenceTax").val();
		
		var otherExpenceTotalAmt = $("#clmOtherExpensesTR").find(".clmExpenceAmt").val();
		var otherExpenceTotalTax = $("#clmOtherExpensesTR").find(".clmExpenceTax").val();
		
		var fixedPerDiamExpenceTotalAmt = $("#clmFixedDIAMTR").find(".clmExpenceAmt").val();
		var fixedPerDiamtravelExpenceTotalTax = $("#clmFixedDIAMTR").find(".clmExpenceTax").val();
		
		
		var userExpenditureOnThisTxn = "#Travel:"+userTravelExpenditure+",";
			userExpenditureOnThisTxn += "#Boarding & Lodging:"+userBnLExpenditure+",";
			userExpenditureOnThisTxn += "#Other Expenses:" + expenseIncurredOtherExpenses+ ",";
			userExpenditureOnThisTxn += "#Fixed Per Diam:"+expenseIncurredFixedPerDiam+"#Total:"+totalExpenseIncurred;

		var amountReturnInCaseOfDueToCompany=$("#balanceDueToTheCompanyAmount").val();
		var totalExpensesIncurredOnThisTxn=$("#totalExpenseIncurred").val();
		var updatedUnsettledAmount=$("#updatedUnsettledAmount").val();
		var txnRemarks=$("#claimSettleTravelAdvanceRemarks").val();
		var useruploadOtherExpensesBills="";var useruploadFixedPerDiamBills="";var useruploadTravelBills="";var useruploadBnLBills="";
		if($("#uploadTravelBills").val()!=""){
			useruploadTravelBills=$("#uploadTravelBills").val()+",";
		}
		if($("#uploadBnLBills").val()!=""){
			useruploadBnLBills=$("#uploadBnLBills").val()+",";
		}
		if($("#uploadOtherExpensesBills").val()!=""){
			useruploadOtherExpensesBills=$("#uploadOtherExpensesBills").val()+",";
		}
		if($("#uploadFixedPerDiamBills").val()!=""){
			useruploadFixedPerDiamBills=$("#uploadFixedPerDiamBills").val()+",";
		}
		var uploadedDocs=useruploadTravelBills+useruploadBnLBills+useruploadOtherExpensesBills+useruploadFixedPerDiamBills;
		var supportingDocTmp = $("#"+parentTr+" select[name='claimSettleTravelAdvanceuploadSuppDocs'] option").map(function () {
			if($(this).val() != ""){
				return $(this).val();
			}
		}).get();
		var supportingDoc = supportingDocTmp.join(',');
		if(uploadedDocs!=""){
			supportingDoc=uploadedDocs+supportingDoc;
		}
		var proceed=false;
		var userCheckRule=checkForOnlyConfiguredAccountants(availableUnsettledClaimAdvances,updatedUnsettledAmount,"travelclaimssettlemet");
		if(userCheckRule==true){
			proceed=true;
		}
		if(userCheckRule==false){
			proceed=false;
		}
		if(proceed){
			var jsonData={};
			jsonData.email = $("#hiddenuseremail").text();
			jsonData.txnEntityID = txnEntityID;
			jsonData.settlementClaimavailableUnsettledClaimAdvances=availableUnsettledClaimAdvances;
			jsonData.settlementClaimunsettledClaimAdvancesDetails=unsettledClaimAdvancesDetails;
			jsonData.settlementClaimuserExpenditureOnThisTxn=userExpenditureOnThisTxn;
			jsonData.settlementClaimtotalExpensesIncurredOnThisTxn=totalExpensesIncurredOnThisTxn;
			jsonData.settlementClaimdueFromCompany=dueFromCompany;
			jsonData.settlementClaimdueToCompany=dueToCompany;
			//jsonData.settlementClaimdueToCompany=updatedUnsettledAmount;
			jsonData.settlementClaimamountReturnInCaseOfDueToCompany=amountReturnInCaseOfDueToCompany;
			jsonData.settlementClaimupdatedUnsettledAmount=updatedUnsettledAmount;
			jsonData.settlementClaimtxnRemarks=txnRemarks;
			jsonData.settlementClaimsupportingDoc=supportingDoc;
			jsonData.claimTxnPurposeText=whatYouWantToDo
			jsonData.claimTxnPurposeVal=whatYouWantToDoVal;
			jsonData.claimTxnTravelExpenses = userTravelExpenditure;
			jsonData.claimTxnBoardingLodging = userBnLExpenditure;
			jsonData.claimTxnOtherExpenses = expenseIncurredOtherExpenses;
			jsonData.claimTxnIncurredFixedPerDiam = expenseIncurredFixedPerDiam;
			
			jsonData.expenseDetails = expenseDetails;
			jsonData.lodgingAndBoardDetails = lodgingAndBoardDetails;
			jsonData.otherExpensesDetails = otherExpensesDetails;
			jsonData.fixedPerDiamDetails = fixedPerDiamDetails;
			
			jsonData.travelExpenceTotalAmt = travelExpenceTotalAmt;
			jsonData.travelExpenceTotalTax = travelExpenceTotalTax;
			jsonData.lnbExpenceTotalAmt = lnbExpenceTotalAmt;
			jsonData.lnbExpenceTotalTax = lnbExpenceTotalTax;
			jsonData.otherExpenceTotalAmt = otherExpenceTotalAmt;
			jsonData.otherExpenceTotalTax = otherExpenceTotalTax;
			jsonData.fixedPerDiamExpenceTotalAmt = fixedPerDiamExpenceTotalAmt;
			jsonData.fixedPerDiamtravelExpenceTotalTax = fixedPerDiamtravelExpenceTotalTax;
			ajaxCall('/claims/submitForApproval', jsonData, '', '', '', '', 'claimSubmitForApprovalSuccess', '', true);
			viewTransactionData(data); // to render the updated transaction recored
		}
	}else if(whatYouWantToDo=="Request Advance For Expense"){
		var parentTr="cREAtrid";
		var txnBranch="";var txnProject="";var txnItemSpecifics="";var expenseAdvanceConfDetails="";var expenseAdvanceRequired="";
		var expenseAdvanceTotalAdvance="";var txnRemarks="";var supportingDoc="";var klcontents="";var klmandatoryfollowednotfollowed="";
		var purposeOfExpenseAdvance="";var txnEntityID="";
		expenseAdvanceRequired=$("#expenseAdvanceRequired").val();
		expenseAdvanceTotalAdvance=$("#expenseAdvanceTotalAdvance").val();
		var followedkl=$("#"+parentTr+" div[class='expenseAdvanceTxnKlContents'] input[name='klfollowed']").attr('id');
		if(typeof followedkl!='undefined'){
			var klfollowednotfollowed=$("#"+parentTr+" div[class='expenseAdvanceTxnKlContents'] input[name='klfollowed']").is(':checked');
			if(klfollowednotfollowed==false){
				swal("Invalid!","Please read and accept the madatory transaction rules before submitting advance for expenses transaction for approval","error");
				$(".btn-custom").removeAttr("disabled");
				$(".btn-customred").removeAttr("disabled");
				$(".approverAction").removeAttr("disabled");
				$("#completeTxn").removeAttr("disabled");
				return true;
			}
		}
		if(expenseAdvanceRequired=="" || expenseAdvanceTotalAdvance==""){
			swal("Invalid!","Please Complete Request Advance For Expense Transaction Completely Before Submitting For Approval.","error");
			$(".btn-custom").removeAttr("disabled");
			$(".btn-customred").removeAttr("disabled");
			$(".approverAction").removeAttr("disabled");
			$("#completeTxn").removeAttr("disabled");
			return true;
		}else{
			txnEntityID = $("#"+parentTr).attr("name");
			txnBranch=$("#cREAtrid select[class='claimBranch'] option:selected").val();
			txnProject=$("#cREAtrid select[class='claimProject'] option:selected").val();
			txnItemSpecifics=$("#expenseAdvanceItems option:selected").val();
			expenseAdvanceConfDetails=$(".expenseAdvanceConfDetails").text();
			expenseAdvanceRequired=$("#expenseAdvanceRequired").val();
			expenseAdvanceTotalAdvance=$("#expenseAdvanceTotalAdvance").val();
			klcontents=$('div[class="expenseAdvanceTxnKlContents"]').text();
			var followedkl=$("#"+parentTr+" div[class='expenseAdvanceTxnKlContents'] input[name='klfollowed']").attr('id');
			if(typeof followedkl!='undefined'){
				if($("#"+parentTr+" div[class='expenseAdvanceTxnKlContents'] input[id='klfollowedyes']").is(':checked')==true){
					klmandatoryfollowednotfollowed="1";
				}
				if($("#"+parentTr+" div[class='expenseAdvanceTxnKlContents'] input[id='klfollowedno']").is(':checked')==true){
					klmandatoryfollowednotfollowed="0";
				}
			}
			purposeOfExpenseAdvance=$("#expenseAdvancePurpose").val();
			txnRemarks=$("#claimRequestExpenseAdvanceRemarks").val();
			var supportingDocTmp = $("#"+parentTr+" select[name='claimRequestExpenseAdvanceuploadSuppDocs'] option").map(function () {
				if($(this).val() != ""){
					return $(this).val();
				}
			}).get();
			supportingDoc = supportingDocTmp.join(',');
			var proceed=false;
			var userCheckRule=checkForConfiguredApproverAndAccountants(txnBranch,txnItemSpecifics,expenseAdvanceRequired,"expenseclaims");
			if(userCheckRule==true){
				proceed=true;
			}
			if(userCheckRule==false){
				proceed=false;
			}
			if(proceed){
				var jsonData={};
				jsonData.email = $("#hiddenuseremail").text();
				jsonData.txnEntityID = txnEntityID;
				jsonData.advanceForExpTxnBnch=txnBranch;
				jsonData.advanceForExpTxnPjct=txnProject;
				jsonData.advanceForExpTxnItemSpecf=txnItemSpecifics;
				jsonData.expenseAdvanceConfDetailsStr=expenseAdvanceConfDetails;
				jsonData.expenseAdvanceRequiredAmount=expenseAdvanceRequired;
				jsonData.expenseAdvanceTotalAdvanceAmount=expenseAdvanceTotalAdvance;
				jsonData.expenseAdvanceKlContents=klcontents;
				jsonData.expenseAdvanceklmandatoryfollowednotfollowed=klmandatoryfollowednotfollowed;
				jsonData.expenseAdvancepurposeOfExpenseAdvance=purposeOfExpenseAdvance;
				jsonData.expenseAdvancetxnRemarks=txnRemarks;
				jsonData.expenseAdvanceSupportingDocuments=supportingDoc;
				jsonData.claimTxnPurposeText=whatYouWantToDo
				jsonData.claimTxnPurposeVal=whatYouWantToDoVal;
				ajaxCall('/expenseclaims/submitForApproval', jsonData, '', '', '', '', 'expenseClaimSubmitForApprovalSuccess', '', true);
				viewTransactionData(data); // to render the updated transaction recored
			}
		}
	} else if(whatYouWantToDo=="Settle Advance For Expense"){

		$('.clmSettleModule').slideUp('fast');
		
		var parentTr="cSEAtrid";
		var availableUnsettledExpenseAdvances="";var unsettledExpenseAdvancesDetails="";var item1ExpIncurredAmount="";
		var txnRemarks="";var supportingDoc="";var totalExpensesIncurredOnThisTxn="";var txnEntityID="";
		var dueFromCompany="";var dueToCompany="";var amountReturnInCaseOfDueToCompany="";var updatedUnsettledAmount="";
		txnEntityID = $("#"+parentTr).attr("name");
		dueFromCompany=$("#dueFromCompanyExpAdvAmount").val();
		dueToCompany=$("#dueToCompanyExpAdvAmount").val();
		if(dueFromCompany=="" && dueToCompany==""){
			$(".btn-custom").removeAttr("disabled");
			$(".btn-customred").removeAttr("disabled");
			$(".approverAction").removeAttr("disabled");
			$("#completeTxn").removeAttr("disabled");
			swal("Error","Please Properly Enter Your Expenses Incurred","error");
			return true;
		}
		
		var incurredExpensesDetails = fetchClaimMultiItemData("clmIncurredExpensesModule");
		var travelExpenceTotalAmt = $("#clmIncurredExpensesTR").find(".clmExpenceAmt").val();
		var travelExpenceTotalTax = $("#clmIncurredExpensesTR").find(".clmExpenceTax").val();
		/*if(dueToCompany!=""){
			var amountReturnInCaseOfDueToCompany=$("#balanceDueToTheCompanyExpAdvAmount").val();
			var value1=parseFloat(dueToCompany);
			var value2=parseFloat(amountReturnInCaseOfDueToCompany);
			if(value2>value1){
				alert("Amount Being Returned To The Company Cannot Be Greater Than The Amount Due To The Company.");
				$("#balanceDueToTheCompanyExpAdvAmount").val("");
				$(".btn-custom").removeAttr("disabled");
				$(".btn-customred").removeAttr("disabled");
				$(".approverAction").removeAttr("disabled");
				$("#completeTxn").removeAttr("disabled");
				return true;
			}
		}*/
		availableUnsettledExpenseAdvances=$("#availableUnsettledExpenseAdvances option:selected").val();
		unsettledExpenseAdvancesDetails=$(".unsettledExpenseAdvancesDetails").text();
		var balanceUnsettledAgainstThisTxn=$(".unsettledExpenseAdvancesDetails").find("#balanceUnsettledAgainstThisTxn").text();
		item1ExpIncurredAmount=$("#item1ExpIncurredAmount").val();
		dueFromCompany=$("#dueFromCompanyExpAdvAmount").val();
		dueToCompany=$("#dueToCompanyExpAdvAmount").val();
		amountReturnInCaseOfDueToCompany=$("#balanceDueToTheCompanyExpAdvAmount").val();
		updatedUnsettledAmount=$("#updatedUnsettledExpAdvAmount").val();
		totalExpensesIncurredOnThisTxn=$("#item1ExpIncurredAmount").val();
		txnRemarks=$("#expAdvSettleTravelAdvanceRemarks").val();
		/*if(balanceUnsettledAgainstThisTxn != "") {
			 var balanceUnsettled=parseFloat(balanceUnsettledAgainstThisTxn);
			 if(item1ExpIncurredAmount > balanceUnsettled) {
			 	swal("Incorrect Expenses Amount!", "Expenses amount is more than Advance available.", "error");
				return false;
			 }
		}else {
			swal("Incorrect Balance Unsettled!", "Balance Unsettled is not valid.", "error");
			return false;
		}*/
		var uploadExpItemDoc="";
		if($("#uploadItem1Bills").val()!=""){
			uploadExpItemDoc=$("#uploadItem1Bills").val()+",";
		}
		var uploadedDocs=uploadExpItemDoc;
		
		var supportingDocTmp = $("#"+parentTr+" select[name='itemExpenseAdvanceuploadSuppDocs'] option").map(function () {
			if($(this).val() != ""){
				return $(this).val();
			}
		}).get();
		supportingDoc = supportingDocTmp.join(',');
		if(uploadedDocs!=""){
			supportingDoc=uploadedDocs+supportingDoc;
		}
		var proceed=false;
		var userCheckRule=checkForOnlyConfiguredAccountants(availableUnsettledExpenseAdvances,updatedUnsettledAmount,"expenseclaimssettlement");
		if(userCheckRule==true){
			proceed=true;
		}
		if(userCheckRule==false){
			proceed=false;
		}
		if(proceed){
			var jsonData={};
			jsonData.email = $("#hiddenuseremail").text();
			jsonData.txnEntityID = txnEntityID;
			jsonData.availableUserUnsettledExpenseAdvances=availableUnsettledExpenseAdvances;
			jsonData.unsettledUserExpenseAdvancesDetails=unsettledExpenseAdvancesDetails;
			jsonData.item1ExpIncurredOnThisTxnAmount=item1ExpIncurredAmount;
			jsonData.amtDueFromCompany=dueFromCompany;
			jsonData.amtDueToCompany=dueToCompany;
			jsonData.amtReturnInCaseOfDueToCompany=amountReturnInCaseOfDueToCompany;
			jsonData.amtUpdatedUnsettledAmount=updatedUnsettledAmount;
			jsonData.amtTotalExpensesIncurredOnThisTxn=totalExpensesIncurredOnThisTxn;
			jsonData.expenseAdvancetxnRemarks=txnRemarks;
			jsonData.expenseAdvanceSupportingDocuments=supportingDoc;
			jsonData.claimTxnPurposeText=whatYouWantToDo
			jsonData.claimTxnPurposeVal=whatYouWantToDoVal;
			jsonData.incurredExpensesDetails = incurredExpensesDetails;
			jsonData.travelExpenceTotalAmt = travelExpenceTotalAmt;
			jsonData.travelExpenceTotalTax = travelExpenceTotalTax;
			ajaxCall('/expenseclaims/submitForApproval', jsonData, '', '', '', '', 'expenseClaimSubmitForApprovalSuccess', '', true);
			viewTransactionData(data); // to render the updated transaction recored
		}
	} else if(whatYouWantToDo=="Request For Expense Reimbursement"){
	
		getDetailsTotalForClaims("clmReiEmbExpensesModule","cREEtrid");
		$('.clmSettleModule').slideUp('fast');
		
		var parentTr="cREEtrid";
		var txnBranch="";var txnProject="";var txnItemSpecifics="";var expenseReimbursementEligibilityDetailsDiv="";var klcontents="";var klmandatoryfollowednotfollowed="";
		var expenseReimbursementPurpose="";var expenseReimbursementAmountRequired="";var txnRemarks="";var supportingDoc="";var txnEntityID="";
		expenseReimbursementAmountRequired=$("#expenseReimbursementAmountRequired").val();
		var monLimitReimbursement=$(".monLimitReimbursement").text();
		var reimbursementAccountedDiv=$(".reimbursementAccountedDiv").text();
		var reimbursementInProgressDiv=$(".reimbursementInProgressDiv").text();
		if(monLimitReimbursement=="" && reimbursementAccountedDiv=="" && reimbursementInProgressDiv==""){
			swal("Invalid","Monthly Monetory Limit For Reimbursement Amount Limit is Not Configured For your Account.You Cannot Proceed With the Transaction.");
			$(".btn-custom").removeAttr("disabled");
			$(".btn-customred").removeAttr("disabled");
			$(".approverAction").removeAttr("disabled");
			$("#completeTxn").removeAttr("disabled");
			return true;
		}
		if(expenseReimbursementAmountRequired==""){
			swal("Invalid!","Please Provide The Amount To Reimburse.","error");
			$(".btn-custom").removeAttr("disabled");
			$(".btn-customred").removeAttr("disabled");
			$(".approverAction").removeAttr("disabled");
			$("#completeTxn").removeAttr("disabled");
			return true;
		}
		var maxAmountThatCanBeReimburse=parseFloat(0.0);
		if(monLimitReimbursement!=""){
			var newMonLimitReimbursement=parseFloat(monLimitReimbursement);
			maxAmountThatCanBeReimburse=maxAmountThatCanBeReimburse+newMonLimitReimbursement;
		}
		if(reimbursementAccountedDiv!=""){
			var newReimbursementAccountedDiv=parseFloat(reimbursementAccountedDiv);
			maxAmountThatCanBeReimburse=maxAmountThatCanBeReimburse-newReimbursementAccountedDiv;
		}
		if(reimbursementInProgressDiv!=""){
			var newReimbursementInProgressDiv=parseFloat(reimbursementInProgressDiv);
			maxAmountThatCanBeReimburse=maxAmountThatCanBeReimburse-newReimbursementInProgressDiv;
		}
		var maxAmountThatCanBeReimburseInThisTxn=maxAmountThatCanBeReimburse.toFixed(2);
		var amountEnteredForReimbursement=parseFloat(expenseReimbursementAmountRequired).toFixed(2);
		if(parseFloat(amountEnteredForReimbursement) > parseFloat(maxAmountThatCanBeReimburseInThisTxn)){
			$("#expenseReimbursementAmountRequired").val("");
			swal("Error!!","You Can Reimburse Amount Greater Than "+maxAmountThatCanBeReimburseInThisTxn+ "Amount This Month","error");
			$(".btn-custom").removeAttr("disabled");
			$(".btn-customred").removeAttr("disabled");
			$(".approverAction").removeAttr("disabled");
			$("#completeTxn").removeAttr("disabled");
			return true;
		}
		var followedkl=$("#"+parentTr+" div[class='expenseReimbursementItemTxnKlContents'] input[name='klfollowed']").attr('id');
		if(typeof followedkl!='undefined'){
			var klfollowednotfollowed=$("#"+parentTr+" div[class='expenseReimbursementItemTxnKlContents'] input[name='klfollowed']").is(':checked');
			if(klfollowednotfollowed==false){
				swal("Error!","Please read and accept the madatory transaction rules before submitting reimbursement transaction for approval","error");
				$(".btn-custom").removeAttr("disabled");
				$(".btn-customred").removeAttr("disabled");
				$(".approverAction").removeAttr("disabled");
				$("#completeTxn").removeAttr("disabled");
				return true;
			}
		}
		txnEntityID = $("#"+parentTr).attr("name");
		txnBranch=$("#cREEtrid select[class='claimBranch'] option:selected").val();
		txnProject=$("#cREEtrid select[class='claimProject'] option:selected").val();
		txnItemSpecifics=$("#expenseClaimItem option:selected").val();
		expenseReimbursementEligibilityDetailsDiv=$(".expenseReimbursementEligibilityDetailsDiv").text();
		klcontents=$('div[class="expenseReimbursementItemTxnKlContents"]').text();
		var followedkl=$("#"+parentTr+" div[class='expenseReimbursementItemTxnKlContents'] input[name='klfollowed']").attr('id');
		if(typeof followedkl!='undefined'){
			if($("#"+parentTr+" div[class='expenseReimbursementItemTxnKlContents'] input[id='klfollowedyes']").is(':checked')==true){
				klmandatoryfollowednotfollowed="1";
			}
			if($("#"+parentTr+" div[class='expenseReimbursementItemTxnKlContents'] input[id='klfollowedno']").is(':checked')==true){
				klmandatoryfollowednotfollowed="0";
			}
		}
		expenseReimbursementPurpose=$("#expenseReimbursementPurpose").val();
		txnRemarks=$("#claimRequestExpenseAdvanceRemarks").val();
		 var supportingDocTmp = $("#"+parentTr+" select[name='expenseReimbursementuploadSuppDocs'] option").map(function () {
			if($(this).val() != ""){
				return $(this).val();
			}
		}).get();
		supportingDoc = supportingDocTmp.join(',');
	 	var reiEmbExpensesDetails = fetchClaimMultiItemData("clmReiEmbExpensesModule");
	 	var travelExpenceTotalAmt = $("#cREEtrid").find(".clmExpenceAmt").val();
		var travelExpenceTotalTax = $("#cREEtrid").find(".clmExpenceTax").val();
		var proceed=false;
		var userCheckRule=checkForConfiguredApproverAndAccountants(txnBranch,txnItemSpecifics,amountEnteredForReimbursement,"expensereimbursementclaims");
		if(userCheckRule==true){
			proceed=true;
		}
		if(userCheckRule==false){
			proceed=false;
		}
		if(proceed){
			var jsonData={};
			jsonData.email = $("#hiddenuseremail").text();
			jsonData.txnEntityID = txnEntityID;
			jsonData.reimbursementTxnBnch=txnBranch;
			jsonData.reimbursementTxnPjct=txnProject;
			jsonData.reimbursementTxnItemSpecf=txnItemSpecifics;
			jsonData.reimbursementExpenseReimbursementEligibilityDetailsDiv=expenseReimbursementEligibilityDetailsDiv;
			jsonData.reimbursementAmountEntered=amountEnteredForReimbursement;
			jsonData.reimbursementklcontents=klcontents;
			jsonData.reimbursementfollowedkl=klmandatoryfollowednotfollowed;
			jsonData.reimbursementPurpose=expenseReimbursementPurpose;
			jsonData.reimbursementtxnRemarks=txnRemarks;
			jsonData.reimbursementSupportingDocuments=supportingDoc;
			jsonData.claimTxnPurposeText=whatYouWantToDo
			jsonData.claimTxnPurposeVal=whatYouWantToDoVal;
			jsonData.reiEmbExpensesDetails = reiEmbExpensesDetails;
			jsonData.travelExpenceTotalAmt = travelExpenceTotalAmt;
			jsonData.travelExpenceTotalTax = travelExpenceTotalTax;
			ajaxCall('/expenseclaims/submitForApproval', jsonData, '', '', '', '', 'expenseClaimSubmitForApprovalSuccess', 'claimSubmitForApprovalError', true);
			viewTransactionData(data); // to render the updated transaction recored
		}
	}
}

$(document).ready(function() {
	$('.submitForApprovalClaim').click(function(){
		var parentTrId=$(this).attr('id');
		var parentTr=parentTrId.substring(0,4)+"trid";
		var whatYouWantToDo=$("#whatYouWantToDoClaim").find('option:selected').text();
		var whatYouWantToDoVal=$("#whatYouWantToDoClaim").find('option:selected').val();
		
		submitForApprovalClaim(whatYouWantToDo,whatYouWantToDoVal,parentTr);
	 });
});


function expenseClaimSubmitForApprovalSuccess(data){

}

function claimSubmitForApprovalSuccess(data){
	if(data){
		if(data.errorMessage){
            swal("Error on submit for approval!", data.errorMessage, "warning");
		}
	}
}

var claimSubmitForApprovalError = function(data){
    swal("Error on submit for approval!", data.errorMessage, "error");
}

function checkOthersContentPurposeOfVisit(elem){
	var valueSelected=$(elem).val();
	if(valueSelected==15){
		var othersInputValue=$("#othersPurposeOfVisit").val();
		if(othersInputValue==""){
			swal("Invalid!","Please Specify Other Purpose Of Visit","error");
			$("#othersPurposeOfVisit").focus();
			return true;
		}
	}
}

function getTravelExpenseClaimCountForApproverAccountant(useremail){
	$(".tacount b[id='tacount']").text("");
	var jsonData = {};
	jsonData.email = useremail;
	ajaxCall('/claims/userAdvancesTxnApprovedButNotAccountedCount', jsonData, '', '', '', '', 'userAdvancesTxnApprovedButNotAccountedCountSuccess', '', true);
}

function userAdvancesTxnApprovedButNotAccountedCountSuccess(data){
	if(data.result){
		$(".tacount b[id='tacount']").text(data.claimApprovedNotAccounted[0].count);
	}else{
		$(".tacount b[id='tacount']").text("0");
	}
	if(data.settlementresult){
		$(".tasettlementcount b[id='tasettlementcount']").text(data.claimSettlementCount[0].settlementCount);
	}else{
		$(".tasettlementcount b[id='tasettlementcount']").text("0");
	}
	if(data.expenseItemAdvanceResult){
		$(".eacount b[id='eacount']").text(data.expenseItemAdvanceCount[0].expenseItemAdvanceCount);
	}else{
		$(".eacount b[id='eacount']").text("0");
	}
	if(data.expenseItemAdvanceSettlementResult){
		$(".easettlementcount b[id='easettlementcount']").text(data.expenseItemAdvanceSettlementResultCount[0].expenseItemAdvanceSettlementResultCount);
	}else{
		$(".easettlementcount b[id='easettlementcount']").text("0");
	}
}

function displayAvailableAndSettledAmountFromAdvances(elem){
	var selectedPreviousClaimTxn=$(elem).val();
	if(selectedPreviousClaimTxn!=""){
		var jsonData = {};
		jsonData.email = $("#hiddenuseremail").text();
		jsonData.previousClaimTxnPrimId=selectedPreviousClaimTxn;
		//ajaxCall('/claims/displayUnsettledAdvances', jsonData, '', '', '', '', 'userTxnUnsettledAdvancesSuccess', '', false);
		var url="/claims/displayUnsettledAdvances"
			$.ajax({
		      url: url,
			headers:{
					"X-AUTH-TOKEN": window.authToken
				},
		      data:JSON.stringify(jsonData),
		      type:"text",
		      method:"POST",
		      async : false,
		      contentType:'application/json',
		      success: function (data) {
		    	$(".totalAdvanceAgainstTxn").html("");
				$(".settledTillDate").html("");
				$(".settledAgainstAdvanceInOtherTxn").html("");
				$(".balanceUnsettledAgainstThisTxn").html("");
				$("#claimSettleTravelAdvanceTable").find("#balanceAmount").val("");
				if(data.result){
					var totalAdvanceAgainstTxn=data.userTxnUnsettledClaimAdvancesDetails[0].totalAdvanceAgainstTxn;
					var settledTillDateTxn=data.userTxnUnsettledClaimAdvancesDetails[0].settledTillDateTxn;
					var settledAgainstAdvanceInOtherTxn=data.userTxnUnsettledClaimAdvancesDetails[0].settledAgainstAdvanceInOtherTxn;
					var balanceUnsettledAgainstThisTxn=data.userTxnUnsettledClaimAdvancesDetails[0].balanceUnsettledAgainstThisTxn;
					$(".totalAdvanceAgainstTxn").html(''+totalAdvanceAgainstTxn+'');
					$(".settledTillDate").html(''+settledTillDateTxn+'');
					$(".settledAgainstAdvanceInOtherTxn").html(''+settledAgainstAdvanceInOtherTxn+'');
					$(".balanceUnsettledAgainstThisTxn").html(''+balanceUnsettledAgainstThisTxn+'');
					$("#claimSettleTravelAdvanceTable").find("#balanceAmount").val(balanceUnsettledAgainstThisTxn);
				}
		      },
		      error: function (xhr, status, error) {
		      	if(xhr.status == 401){ doLogout();
		      	}else if(xhr.status == 500){
		    		swal("Error on fetching Transaction purposes!", "Please retry, if problem persists contact support team", "error");
		    	}
		      }
			});
	}else{
		$(".totalAdvanceAgainstTxn").html("");
		$(".settledTillDate").html("");
		$(".settledAgainstAdvanceInOtherTxn").html("");
		$(".balanceUnsettledAgainstThisTxn").html("");
		$("#claimSettleTravelAdvanceTable").find("#balanceAmount").val("");
		$("#expenseIncurredTravel").val("");
		$("#expenseIncurredOtherExpenses").val("");
		$("#expenseIncurredFixedPerDiam").val("");
		$(".balanceUnsettledAgainstThisTxn").text("");
		$("#balanceAmount").val("");
		$("#dueFromCompanyAmount").val("");
		$("#dueToCompanyAmount").val("");
		$("#balanceDueToTheCompanyAmount").val("");
		$("#updatedUnsettledAmount").val("");
		$("#expenseIncurredBnL").val("");
		$("#totalExpenseIncurred").val("");
	}
	
}

function displayAvailableAndSettledAmountFromExpenseAdvances(elem){
	var selectedPreviousAdvExpTxn=$(elem).val();
	if(selectedPreviousAdvExpTxn!=""){
		var jsonData={};
		jsonData.email = $("#hiddenuseremail").text();
		jsonData.previousAdvanceExpTxnPrimId=selectedPreviousAdvExpTxn;
		ajaxCall('/advanceExpense/displayUnsettledAdvances', jsonData, '', '', '', '', 'userAdvExpTxnUnsettledAdvancesSuccess', '', false);
	}else{
		var parentTr="cSEAtrid";
		$("#"+parentTr+" .totalAdvanceAgainstTxn").html("");
		$("#"+parentTr+" .settledTillDate").html("");
		$("#"+parentTr+" .balanceUnsettledAgainstThisTxn").html("");
		$("#"+parentTr+" .item1ExpIncurred").html("");
		$("#item1ExpIncurredAmount").val("");
		$("#dueFromCompanyExpAdvAmount").val("");
		$("#dueToCompanyExpAdvAmount").val("");
		$("#dueToCompanyExpAdvAmount").val("");
		$("#balanceDueToTheCompanyExpAdvAmount").val("");
		$("#updatedUnsettledExpAdvAmount").val("");
	}
}

function userAdvExpTxnUnsettledAdvancesSuccess(data){
	var parentTr="cSEAtrid";
	$("#"+parentTr+" .totalAdvanceAgainstTxn").html("");
	$("#"+parentTr+" .settledTillDate").html("");
	$("#"+parentTr+" .balanceUnsettledAgainstThisTxn").html("");
	$("#"+parentTr+" .item1ExpIncurred").html("");
	$("#item1ExpIncurredAmount").val("");
	$("#dueFromCompanyExpAdvAmount").val("");
	$("#dueToCompanyExpAdvAmount").val("");
	$("#dueToCompanyExpAdvAmount").val("");
	$("#balanceDueToTheCompanyExpAdvAmount").val("");
	$("#updatedUnsettledExpAdvAmount").val("");
	if(data.result){
		var parentTr="cSEAtrid";
		$("#"+parentTr+" .totalAdvanceAgainstTxn").html(data.userAdvExpTxnUnsettledAdvances[0].totalAdvanceAgainstTxn);
		$("#"+parentTr+" .settledTillDate").html(data.userAdvExpTxnUnsettledAdvances[0].settledTillDate);
		$("#"+parentTr+" .balanceUnsettledAgainstThisTxn").html(data.userAdvExpTxnUnsettledAdvances[0].balanceUnsettledAgainstThisTxn);
		$("#"+parentTr+" .item1ExpIncurred").html(data.userAdvExpTxnUnsettledAdvances[0].itemNameExpIncurred);
	}
}

/*
function userTxnUnsettledAdvancesSuccess(data){
	$(".totalAdvanceAgainstTxn").html("");
	$(".settledTillDate").html("");
	$(".settledAgainstAdvanceInOtherTxn").html("");
	$(".balanceUnsettledAgainstThisTxn").html("");
	if(data.result){
		var totalAdvanceAgainstTxn=data.userTxnUnsettledClaimAdvancesDetails[0].totalAdvanceAgainstTxn;
		var settledTillDateTxn=data.userTxnUnsettledClaimAdvancesDetails[0].settledTillDateTxn;
		var settledAgainstAdvanceInOtherTxn=data.userTxnUnsettledClaimAdvancesDetails[0].settledAgainstAdvanceInOtherTxn;
		var balanceUnsettledAgainstThisTxn=data.userTxnUnsettledClaimAdvancesDetails[0].balanceUnsettledAgainstThisTxn;
		$(".totalAdvanceAgainstTxn").html(''+totalAdvanceAgainstTxn+'');
		$(".settledTillDate").html(''+settledTillDateTxn+'');
		$(".settledAgainstAdvanceInOtherTxn").html(''+settledAgainstAdvanceInOtherTxn+'');
		$(".balanceUnsettledAgainstThisTxn").html(''+balanceUnsettledAgainstThisTxn+'');
	}
}*/

function calculateTotalExpenseIncurredOnExpAdvanceThisTxn(elem){
	$("#cSEAAdvanceSettlementSubmitForApproval").attr('title','Submit For Payment');
	document.getElementById("cSEAAdvanceSettlementSubmitForApproval").innerText='Submit For Payment';
	$("#balanceDueToTheCompanyExpAdvAmount").val();
	$("#updatedUnsettledExpAdvAmount").val("");
	var item1ExpIncurredAmount=$("#item1ExpIncurredAmount").val();
	var balanceUnsettledAgainstThisTxn=$("#cSEAtrid .balanceUnsettledAgainstThisTxn").text();
	if(balanceUnsettledAgainstThisTxn!=""){
		var total=0;var balanceUnsettled=parseFloat(balanceUnsettledAgainstThisTxn);var balance=0;
		if(item1ExpIncurredAmount!=""){
			var value=parseFloat(item1ExpIncurredAmount);
			total=total+value;
		}
	/*	
		var newTotal=total.toFixed(2);
		if(newTotal > balanceUnsettled) {
			swal("Incorrect Expenses Amount!", "Expenses amount is more than Advance available.", "error");
			return false;
		}
	*/
		if(item1ExpIncurredAmount==""){
			$("#balanceExpAdvAmount").val("");
			$("#dueFromCompanyExpAdvAmount").val("");
			$("#dueToCompanyExpAdvAmount").val("");
		}
		balance=balanceUnsettled-total;
		var newBalanceUptoTwoFixed=balance.toFixed(2);
		if(balance<0){
			$("#balanceExpAdvAmount").val(newBalanceUptoTwoFixed);
			var newBalance=total-balanceUnsettled;
			var newBalanceFloat=newBalance.toFixed(2);
			$("#dueFromCompanyExpAdvAmount").val(newBalanceFloat);
			$("#dueToCompanyExpAdvAmount").val("0.0");
			$("#cSEAAdvanceSettlementSubmitForApproval").attr('title','Submit To Receive');
			document.getElementById("cSEAAdvanceSettlementSubmitForApproval").innerText='Submit To Receive';
		}else if(balance>0){
			$("#balanceExpAdvAmount").val(newBalanceUptoTwoFixed);
			$("#dueFromCompanyExpAdvAmount").val("0.0");
			$("#dueToCompanyExpAdvAmount").val(newBalanceUptoTwoFixed);
			$("#cSEAAdvanceSettlementSubmitForApproval").attr('title','Submit For Payment');
			document.getElementById("cSEAAdvanceSettlementSubmitForApproval").innerText='Submit For Payment';
		}else if(balance==0){
			$("#balanceExpAdvAmount").val(newBalanceUptoTwoFixed);
			$("#dueFromCompanyExpAdvAmount").val("0.0");
			$("#dueToCompanyExpAdvAmount").val("0.0");
			$("#cSEAAdvanceSettlementSubmitForApproval").attr('title','Submit For Payment');
			document.getElementById("cSEAAdvanceSettlementSubmitForApproval").innerText='Submit For Payment';
		}
	}else{
		$("#balanceExpAdvAmount").val("");
		$("#dueFromCompanyExpAdvAmount").val("");
		$("#dueToCompanyExpAdvAmount").val("");
	}
}

function calculateTotalExpenseIncurredOnThisTxn(elem){
	$("#cSTAAdvanceSettlementSubmitForApproval").attr('title','Submit For Payment');
	document.getElementById("cSTAAdvanceSettlementSubmitForApproval").innerText='Submit For Payment';
	$("#balanceDueToTheCompanyAmount").val("");
	$("#updatedUnsettledAmount").val("");
	var expenseIncurredTravel=$("#expenseIncurredTravel").val();
	var expenseIncurredBnL=$("#expenseIncurredBnL").val();
	var expenseIncurredOtherExpenses=$("#expenseIncurredOtherExpenses").val();
	var expenseIncurredFixedPerDiam=$("#expenseIncurredFixedPerDiam").val();
	var balanceUnsettledAgainstThisTxn=$("#balanceUnsettledAgainstThisTxn").text();
	//var balanceUnsettledAgainstThisTxn=$("#cSEAtrid .balanceUnsettledAgainstThisTxn").text();
	if(balanceUnsettledAgainstThisTxn!=""){
		var total=0;var balanceUnsettled=parseFloat(balanceUnsettledAgainstThisTxn);var balance=0;
		if(expenseIncurredTravel!=""){
			var value=parseFloat(expenseIncurredTravel);
			total=total+value;
		}
		if(expenseIncurredBnL!=""){
			var value=parseFloat(expenseIncurredBnL);
			total=total+value;
		}
		if(expenseIncurredOtherExpenses!=""){
			var value=parseFloat(expenseIncurredOtherExpenses);
			total=total+value;
		}
		if(expenseIncurredFixedPerDiam!=""){
			var value=parseFloat(expenseIncurredFixedPerDiam);
			total=total+value;
		}
		if(expenseIncurredTravel=="" && expenseIncurredBnL=="" && expenseIncurredOtherExpenses=="" && expenseIncurredFixedPerDiam==""){
			$("#balanceAmount").val("");
			$("#dueFromCompanyAmount").val("");
			$("#dueToCompanyAmount").val("");
			$("#totalExpenseIncurred").val("");
		}
		
		var newTotal=total.toFixed(2);
		/*
		if(newTotal > balanceUnsettled) {
			swal("Incorrect Expenses Amount!", "Expenses amount is more than Advance available.", "error");
			return false;
		}
		*/
		$("#totalExpenseIncurred").val(newTotal);
		balance=balanceUnsettled-total;
		var newBalanceUptoTwoFixed=balance.toFixed(2);
		if(balance<0){
			$("#balanceAmount").val(newBalanceUptoTwoFixed);
			var newBalance=total-balanceUnsettled;
			var newBalanceUnsettled=newBalance.toFixed(2);
			$("#dueFromCompanyAmount").val(newBalanceUnsettled);
			$("#dueToCompanyAmount").val("0.0");
			$("#cSTAAdvanceSettlementSubmitForApproval").attr('title','Submit To Receive');
			document.getElementById("cSTAAdvanceSettlementSubmitForApproval").innerText='Submit To Receive';
		}else if(balance>0){
			$("#balanceAmount").val(newBalanceUptoTwoFixed);
			$("#dueFromCompanyAmount").val("0.0");
			$("#dueToCompanyAmount").val(newBalanceUptoTwoFixed);
			$("#cSTAAdvanceSettlementSubmitForApproval").attr('title','Submit For Payment');
			document.getElementById("cSTAAdvanceSettlementSubmitForApproval").innerText='Submit For Payment';
		}else if(balance==0){
			$("#balanceAmount").val(newBalanceUptoTwoFixed);
			$("#dueFromCompanyAmount").val("0.0");
			$("#dueToCompanyAmount").val("0.0");
			$("#cSTAAdvanceSettlementSubmitForApproval").attr('title','Submit For Payment');
			document.getElementById("cSTAAdvanceSettlementSubmitForApproval").innerText='Submit For Payment';
		}
	}else{
		//alert("Please Select Available Travel Claim Adavances For Settlement");
		$("#balanceAmount").val("");
		$("#dueFromCompanyAmount").val("");
		$("#dueToCompanyAmount").val("");
		return true;
	}
}

function checkDueToTheCompany(elem){
	var dueToCompanyAmount=$("#balanceAmount").val();
	var value=$(elem).val();
	if(value!=""){
		if(dueToCompanyAmount==""){
			swal("Error!","There Is Not Any Due To the Company For This Transaction.So You Cannot Return Any Amount To The Company","error");
			$(elem).val("");
			return true;
		}else{
			var dueToComp=parseFloat(dueToCompanyAmount);
			var amountReturnedToCompany=parseFloat(value);
			if(amountReturnedToCompany>dueToComp){
				swal("Error!!","You Cannot Return Amount Greater Than The Amount Due To The Company;","error");
				$(elem).val("");
				return true;
			}else{
				var valueTmp=dueToComp-amountReturnedToCompany;
				$("#updatedUnsettledAmount").val(valueTmp);
				$("#dueToCompanyAmount").val(valueTmp);
			}
		}
	}else{
		$("#dueToCompanyAmount").val(dueToCompanyAmount);
		$(elem).val("");
		$("#updatedUnsettledAmount").val("");
	}
}

function checkDueToTheCompanyExpAdv(elem){
	var dueToCompanyAmount=$("#balanceExpAdvAmount").val();
	var value=$(elem).val();
	if(value!=""){
		if(dueToCompanyAmount==""){
			swal("Error!","There Is Not Any Due To the Company For This Transaction.So You Cannot Return Any Amount To The Company","error");
			$(elem).val("");
			return true;
		}else{
			var dueToComp=parseFloat(dueToCompanyAmount);
			var amountReturnedToCompany=parseFloat(value);
			if(amountReturnedToCompany>dueToComp){
				swal("Error!","You Cannot Return Amount Greater Than The Amount Due To The Company;","error");
				$(elem).val("");
				return true;
			}else{
				var valueTmp=dueToComp-amountReturnedToCompany;
				$("#updatedUnsettledExpAdvAmount").val(valueTmp);
				$("#dueToCompanyExpAdvAmount").val(valueTmp);
			}
		}
	}else{
		$(elem).val("");
		$("#updatedUnsettledExpAdvAmount").val("");
	}
}

function expenseAdvanceSettlementAccounting(elem){
	$("#settleExpenseAdvanceTxn").attr("disabled", "disabled");
	//var parentTr=$(elem).parent().parent().attr('id');
	var parentTr = $(elem).closest('tr').attr('id');
	var settlementValue=$(elem).attr('value');
	var transactionEntityId=parentTr.substring(23, parentTr.length);
	var supportingDoc=$("#"+parentTr+" input[name='actionFileUpload']").val();
	var remarks=$("#"+parentTr+" textarea[id='txnRemarks']").val();
	var paymentOption=$("#"+parentTr+" select[id='claimpaymentDetails'] option:selected").val();
	var bankDetails=$("#"+parentTr+" textarea[id='bankDetails']").val();
	var jsonData = {};
	jsonData.email = $("#hiddenuseremail").text();
	jsonData.claimTxnPrimId=transactionEntityId;
	jsonData.claimSettlementValue=settlementValue;
	jsonData.suppDoc=supportingDoc;
	jsonData.txnRmarks=remarks;
	jsonData.paymentDetails=paymentOption;
	if(paymentOption=="2"){
		paymentBank=$("#availableBank option:selected").val();
		if(typeof paymentBank!='undefined'){
			if(paymentBank == ""){
				// alert("Select a bank from list");
				swal("Empty data error!", "Select a bank from list", "error");
				enableTransactionButtons();
				return false;
			}
			var instrumentDate=$("#instrumentDate").val();
			if(instrumentDate == ""){
				// alert("Instrument Date cannot be empty.");
				swal("Empty data error!", "Instrument Date cannot be empty.", "error");
				enableTransactionButtons();
				return false;
			}
			jsonData.txnPaymentBank=paymentBank;
			jsonData.txnInstrumentNum=$("#instrumentNumber").val();
			jsonData.txnInstrumentDate=instrumentDate;
	    }
	}


	jsonData.bankInf=bankDetails;
	ajaxCall('/advanceExpenses/expAdvanceSettlementAccountantAction', jsonData, '', '', '', '', 'expAdvanceSettlementAccountantActionSuccess', '', false);
}

function expAdvanceSettlementAccountantActionSuccess(data){

}

function claimSettlementAccounting(elem){
	$("#settleTravelClaimTxn").attr("disabled", "disabled");
	//var parentTr=$(elem).parent().parent().attr('id');
	var parentTr = $(elem).closest('tr').attr('id');
	var settlementValue=$(elem).attr('value');
	var transactionEntityId=parentTr.substring(23, parentTr.length);
	var supportingDoc=$("#"+parentTr+" input[name='actionFileUpload']").val();
	var remarks=$("#"+parentTr+" textarea[id='txnRemarks']").val();
	var paymentOption=$("#"+parentTr+" select[id='claimpaymentDetails'] option:selected").val();
	var bankDetails=$("#"+parentTr+" textarea[id='bankDetails']").val();
	var jsonData = {};
	jsonData.email = $("#hiddenuseremail").text();
	jsonData.claimTxnPrimId=transactionEntityId;
	jsonData.claimSettlementValue=settlementValue;
	jsonData.suppDoc=supportingDoc;
	jsonData.txnRmarks=remarks;
	jsonData.paymentDetails=paymentOption;
	if(paymentOption=="2"){
		paymentBank=$("#availableBank option:selected").val();
		if(typeof paymentBank!='undefined'){
			if(paymentBank == ""){
				// alert("Select a bank from list");
				swal("Empty data error!", "Select a bank from list", "error");
				enableTransactionButtons();
				return false;
			}
			var instrumentDate=$("#instrumentDate").val();
			if(instrumentDate == ""){
				// alert("Instrument Date cannot be empty.");
				swal("Empty data error!", "Instrument Date cannot be empty.", "error");
				enableTransactionButtons();
				return false;
			}
			jsonData.txnPaymentBank=paymentBank;
			jsonData.txnInstrumentNum=$("#instrumentNumber").val();
			jsonData.txnInstrumentDate=instrumentDate;
	    }
	}
	jsonData.bankInf=bankDetails;
	ajaxCall('/claims/claimSettlementAccountantAction', jsonData, '', '', '', '', 'claimSettlementAccountantActionSuccess', '', false);
}

function claimSettlementAccountantActionSuccess(data){
				if(data.resultantAmount < 0){
					if(data.branchBankDetailEntered === false){
						swal("Insufficient balance in the bank account!", "Use alternative payment mode or infuse funds into the bank account. Current bank balance is:"+data.resultantAmount, "error");
						disableTransactionButtons();
						return false;
					}else{					
						swal("Bank Balance is in negative!", "This bank account type allows -ve balance so transaction is successful. Note current bank balance is:"+data.resultantAmount, "warning");
					}
				}	
				if(data.resultantCash < 0){
					swal("Insufficient balance in the bank account!","Insufficient balance in the cash account. Use alternative payment mode or infuse funds into the cash account. Effective cash balance is: " + data.resultantCash,"warning");
				}
				if(data.resultantPettyCashAmount < 0){
					swal("Insufficient balance in the bank account!","Insufficient balance in the petty cash account. Use alternative payment mode or infuse funds into petty cash account. Effective petty cash balance is: " + data.resultantPettyCashAmount,"warning");
				}
}

function displayUserAdvanceForExpenseEligibility(elem){
	var parentTr=$(elem).parent().parent().attr('id');
	$("#"+parentTr+" .expenseAdvanceMaxPermittedAdvance").html("");
	$("#"+parentTr+" .expenseAdvanseUnsettledExistingAdvances").html("");
	$("#"+parentTr+" .expenseAdvanceRequestInProgress").html("");
	$("div[class='expenseAdvanceTxnKlContents']").html("");
	$("#expenseAdvanceRequired").val("");
	$("#expenseAdvanceTotalAdvance").val("");
	var value=$(elem).val();
	var selectedBranchValue=$("#"+parentTr+" select[class='claimBranch'] option:selected").val();
	if(selectedBranchValue==""){
		swal("Invalid!","Please Select The Branch Of The User For Which You Want To Receive Advance For Expenses","error");
		$("#"+parentTr+" select[class='claimBranch']").focus();
		return true;
	}else{
		if(value!=""){
			var jsonData={};
			jsonData.email = $("#hiddenuseremail").text();
			jsonData.specificsPrimKeyId=value;
			jsonData.selBranch=selectedBranchValue;
			ajaxCall('/expenseAdvances/displayUserEligibility', jsonData, '', '', '', '', 'userExpenseAdvanceEligibilitySuccess', '', false);
		}
	}
}

function userExpenseAdvanceEligibilitySuccess(data){
	var parentTr="cREAtrid";
	if(data.result){
		$("#"+parentTr+" .expenseAdvanceMaxPermittedAdvance").html(data.expenseAdvanceEligibilityData[0].expenseAdvanceMaxPermittedAdvance);
		$("#"+parentTr+" .expenseAdvanseUnsettledExistingAdvances").html(data.expenseAdvanceEligibilityData[0].expenseAdvanseUnsettledExistingAdvances);
		$("#expenseAdvanceTotalAdvance").val(data.expenseAdvanceEligibilityData[0].expenseAdvanseUnsettledExistingAdvances);
		$("#"+parentTr+" .expenseAdvanceRequestInProgress").html(data.expenseAdvanceEligibilityData[0].expenseAdvanceRequestInProgress);
	}
	if(data.klcontentresult){
		for(var i=0;i<data.expenseAdvanceEligibilitySpecfKlData.length;i++){
			var parentTr="cREAtrid";
			var klcount=i+1;
	  		if(i==0){
	  			if(data.expenseAdvanceEligibilitySpecfKlData[i].klIsMandatory=="1"){
	  				var followedkl=$("#"+parentTr+" div[class='expenseAdvanceTxnKlContents'] input[name='klfollowed']").attr('id');
	  				if(typeof followedkl=='undefined'){
	  				   $("#"+parentTr+" div[class='expenseAdvanceTxnKlContents']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes &nbsp;&nbsp;<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
	  				}
	  				$("#"+parentTr+" div[class='expenseAdvanceTxnKlContents']").append('<br/>('+klcount+')<i class="icon-star"></i>'+data.expenseAdvanceEligibilitySpecfKlData[i].klContent+'.');
	  			}
	  			if(data.expenseAdvanceEligibilitySpecfKlData[i].klIsMandatory=="0"){
	  				  $("#"+parentTr+" div[class='expenseAdvanceTxnKlContents']").append('<br/>('+klcount+')'+data.expenseAdvanceEligibilitySpecfKlData[i].klContent+'.');
	  			}
	  		}else{
	  			if(data.expenseAdvanceEligibilitySpecfKlData[i].klIsMandatory=="1"){
	  				var followedkl=$("#"+parentTr+" div[class='expenseAdvanceTxnKlContents'] input[name='klfollowed']").attr('id');
	  				if(typeof followedkl=='undefined'){
	  				  $("#"+parentTr+" div[class='expenseAdvanceTxnKlContents']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes &nbsp;&nbsp;<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
	  				}
	  				$("#"+parentTr+" div[class='expenseAdvanceTxnKlContents']").append('<br/>('+klcount+')<i class="icon-star"></i>'+data.expenseAdvanceEligibilitySpecfKlData[i].klContent+'.');
	  			}
	  			if(data.expenseAdvanceEligibilitySpecfKlData[i].klIsMandatory=="0"){
	  				$("#"+parentTr+" div[class='expenseAdvanceTxnKlContents']").append('<br/>('+klcount+')'+data.expenseAdvanceEligibilitySpecfKlData[i].klContent+'.');
	  			}
	  		}
		}
	}
}

function validateUserExpenseAdvance(elem){
	$("#expenseAdvanceTotalAdvance").val("");
	var expenseAdvanceMaxPermittedAdvance=$(".expenseAdvanceMaxPermittedAdvance").html();
	var expenseAdvanseUnsettledExistingAdvances=$(".expenseAdvanseUnsettledExistingAdvances").html();
	var expenseAdvanceRequestInProgress=$(".expenseAdvanceRequestInProgress").html();
	var enteredValue=$(elem).val();
	var expAdvMaxPermAdvInitialVal=parseFloat(0.0);
	var expAdvUnsettledExisAdv=parseFloat(0.0);
	var expAdvReqInProgress=parseFloat(0.0);
	var maxExpenseAdvanceAllowedOnThisTxn=parseFloat(0.0);
	var enteredFloat=parseFloat(0.0);
	if(expenseAdvanceMaxPermittedAdvance!=""){
		expAdvMaxPermAdvInitialVal=parseFloat(expenseAdvanceMaxPermittedAdvance);
	}
	if(expenseAdvanseUnsettledExistingAdvances!=""){
		expAdvUnsettledExisAdv=parseFloat(expenseAdvanseUnsettledExistingAdvances);
	}
	if(expenseAdvanceRequestInProgress!=""){
		expAdvReqInProgress=parseFloat(expenseAdvanceRequestInProgress);
	}
	if(enteredValue!=""){
		enteredFloat=parseFloat(enteredValue);
	}
	maxExpenseAdvanceAllowedOnThisTxn=expAdvMaxPermAdvInitialVal-expAdvUnsettledExisAdv-expAdvReqInProgress;
	if(enteredFloat>maxExpenseAdvanceAllowedOnThisTxn){
		swal("Error!","You Are Not Allowed To Take Expense Advance For This Much Amount.Please Enter An Lesser Amount.","error");
		$("#expenseAdvanceTotalAdvance").val($(".expenseAdvanseUnsettledExistingAdvances").html());
		$(elem).val("");
		return true;
	}else{
		var value=expAdvUnsettledExisAdv+enteredFloat;
		$("#expenseAdvanceTotalAdvance").val(value);
	}
}


function populateUserExpenseItemReimbursementKlDetails(elem){
	var parentTr="cREEtrid";
	var expenseItemId=$(elem).val();
	if(expenseItemId!=""){
		var selectedBranchValue=$("#"+parentTr+" select[class='claimBranch'] option:selected").val();
		if(selectedBranchValue==""){
			swal("Empty field Error!","Please Select The Branch Of The User For Which You Want To Reimburse Amount.","error");
			$("#"+parentTr+" select[class='claimBranch']").focus();
			return true;
		}else{
			var jsonData = {};
			jsonData.email = $("#hiddenuseremail").text();
			jsonData.expenseItemValue=expenseItemId;
			jsonData.selectedUserBranch=selectedBranchValue;
			ajaxCall('/reimbursement/getUserExpenseItemReimbursementAmountKl', jsonData, '', '', '', '', 'userExpenseItemReimbursementAmountKlSuccess', '', false);
		}
	}else{
		$(".monLimitReimbursement").text("");
		$(".reimbursementAccountedDiv").text("");
		$(".reimbursementInProgressDiv").text("");
		$("#expenseReimbursementAmountRequired").val();
		return true;
	}
}

function userExpenseItemReimbursementAmountKlSuccess(data){
	$(".monLimitReimbursement").text("");
	$(".reimbursementAccountedDiv").text("");
	$(".reimbursementInProgressDiv").text("");
	if(data.reimbursementresult){
		$(".monLimitReimbursement").text(data.reimbursementAmountData[0].totalMonthlyMonetoryLimitForReimbursementForThisItem);
		$(".reimbursementAccountedDiv").text(data.reimbursementAmountData[0].thisMonthEmployeeAmountReimbursementForThisItemAccounted);
		$(".reimbursementInProgressDiv").text(data.reimbursementAmountData[0].thisMonthEmployeeAmountReimbursementForThisItemInProgress);
		if(data.expenseitemklresult){
			$("#"+parentTr+" div[class='expenseReimbursementItemTxnKlContents']").text();
			for(var i=0;i<data.expenseItemSpecfKlData.length;i++){
				var parentTr="cREAtrid";
				var klcount=i+1;
		  		if(i==0){
		  			if(data.expenseItemSpecfKlData[i].klIsMandatory=="1"){
		  				var followedkl=$("#"+parentTr+" div[class='expenseReimbursementItemTxnKlContents'] input[name='klfollowed']").attr('id');
		  				if(typeof followedkl=='undefined'){
		  				   $("#"+parentTr+" div[class='expenseReimbursementItemTxnKlContents']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes &nbsp;&nbsp;<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
		  				}
		  				$("#"+parentTr+" div[class='expenseReimbursementItemTxnKlContents']").append('<br/>('+klcount+')<i class="icon-star"></i>'+data.expenseItemSpecfKlData[i].klContent+'.');
		  			}
		  			if(data.expenseItemSpecfKlData[i].klIsMandatory=="0"){
		  				  $("#"+parentTr+" div[class='expenseReimbursementItemTxnKlContents']").append('<br/>('+klcount+')'+data.expenseItemSpecfKlData[i].klContent+'.');
		  			}
		  		}else{
		  			if(data.expenseItemSpecfKlData[i].klIsMandatory=="1"){
		  				var followedkl=$("#"+parentTr+" div[class='expenseReimbursementItemTxnKlContents'] input[name='klfollowed']").attr('id');
		  				if(typeof followedkl=='undefined'){
		  				  $("#"+parentTr+" div[class='expenseReimbursementItemTxnKlContents']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes &nbsp;&nbsp;<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
		  				}
		  				$("#"+parentTr+" div[class='expenseReimbursementItemTxnKlContents']").append('<br/>('+klcount+')<i class="icon-star"></i>'+data.expenseItemSpecfKlData[i].klContent+'.');
		  			}
		  			if(data.expenseItemSpecfKlData[i].klIsMandatory=="0"){
		  				$("#"+parentTr+" div[class='expenseReimbursementItemTxnKlContents']").append('<br/>('+klcount+')'+data.expenseItemSpecfKlData[i].klContent+'.');
		  			}
		  		}
			}
		}
	}
}

function advanceExpenseApproverAction(parentTr){
	//alert(">>>>>12"); //sunil
	$(".btn-custom").attr("disabled", "disabled");
	$(".btn-customred").attr("disabled", "disabled");
	$(".approverAction").attr("disabled", "disabled");
	var selectedAction=$("#"+parentTr+" select[id='claimapproverActionList'] option:selected").val();
	var transactionEntityId=parentTr.substring(23, parentTr.length);
	var supportingDoc=$("#"+parentTr+" input[name='actionFileUpload']").val();
	var remarks=$("#"+parentTr+" textarea[id='txnRemarks']").val();
	var selectedAddApproverVal="";
	if(selectedAction==""){
		swal("Error!","Please choose your next action from the Approver action list","error");
		$(".btn-custom").removeAttr("disabled");
		$(".btn-customred").removeAttr("disabled");
		$(".approverAction").removeAttr("disabled");
		$("#completeTxn").removeAttr("disabled");
		return true;
	}
	if(selectedAction=="3"){
		if(selectedAddApproverVal==""){
			swal("Error!","Please choose the user to whom you want to send for additional approval","error");
			$(".btn-custom").removeAttr("disabled");
			$(".btn-customred").removeAttr("disabled");
			$(".approverAction").removeAttr("disabled");
			$("#completeTxn").removeAttr("disabled");
			return true;
		}else{
			var txnJsonData={};
			txnJsonData.email=$("#hiddenuseremail").text();
			txnJsonData.selectedApproverAction=selectedAction;
			txnJsonData.transactionPrimId=transactionEntityId;
			txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
			txnJsonData.suppDoc=supportingDoc;
			txnJsonData.txnRmarks=remarks;
			var url="/advanceExpense/approverAction";
			$.ajax({
				url: url,
				data:JSON.stringify(txnJsonData),
				type:"text",
				headers:{
					"X-AUTH-TOKEN": window.authToken
				},
				method:"POST",
				contentType:'application/json',
				success: function (data) {
				},
				error: function (xhr, status, error) {
					if(xhr.status == 401){ doLogout(); }
				}
			});
		}
	}else{
		//send server for action to complete
		var txnJsonData={};
		txnJsonData.email=$("#hiddenuseremail").text();
		txnJsonData.selectedApproverAction=selectedAction;
		txnJsonData.transactionPrimId=transactionEntityId;
		txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
		txnJsonData.suppDoc=supportingDoc;
		txnJsonData.txnRmarks=remarks;
		var url="/advanceExpense/approverAction";
		$.ajax({
			url: url,
			data:JSON.stringify(txnJsonData),
			type:"text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method:"POST",
			contentType:'application/json',
			success: function (data) {
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	}
}

function claimapproverAction(parentTr){
	//alert(">>>>>14"); //sunil
	$(".btn-custom").attr("disabled", "disabled");
	$(".btn-customred").attr("disabled", "disabled");
	$(".approverAction").attr("disabled", "disabled");
	// update MultiItem Table Total 
		getDetailsTotalForClaims("clmTravelExpensesModule","clmTravelExpensesTR");
		getDetailsTotalForClaims("clmLodgingExpensesModule","clmLodgingExpensesTR");
		getDetailsTotalForClaims("clmOtherExpensesModule","clmOtherExpensesTR");
		getDetailsTotalForClaims("clmFixedDIAMModule","clmFixedDIAMTR");
		getDetailsTotalForClaims("clmIncurredExpensesModule","clmIncurredExpensesTR");
		getDetailsTotalForClaims("clmReiEmbExpensesModule","cREEtrid");
		$('.clmSettleModule').slideUp('fast');
		
	var selectedAction=$("#"+parentTr+" select[id='claimapproverActionList'] option:selected").val();
	var transactionEntityId=parentTr.substring(23, parentTr.length);
	var supportingDocTmp = $("#"+parentTr+" select[name='txnViewListUpload'] option").map(function () {
		if($(this).val() != ""){
			return $(this).val();
		}
	}).get();
	var supportingDoc = supportingDocTmp.join(',');
	var remarks=$("#"+parentTr+" textarea[id='txnRemarks']").val();
	var selectedAddApproverVal="";
	if(selectedAction==""){
		swal("Empty Field Error!","Please choose your next action from the Approver action list","error");
		$(".btn-custom").removeAttr("disabled");
		$(".btn-customred").removeAttr("disabled");
		$(".approverAction").removeAttr("disabled");
		$("#completeTxn").removeAttr("disabled");
		return true;
	}
	if(selectedAction=="3"){
		selectedAddApproverVal=$("#"+parentTr+" select[id='userAddApproval'] option:selected").val();
		if(selectedAddApproverVal==""){
			swal("Empty Field Error!","Please choose the user to whom you want to send for additional approval","error");
			$(".btn-custom").removeAttr("disabled");
			$(".btn-customred").removeAttr("disabled");
			$(".approverAction").removeAttr("disabled");
			$("#completeTxn").removeAttr("disabled");
			return true;
		}else{
			var txnJsonData={};
			txnJsonData.email=$("#hiddenuseremail").text();
			txnJsonData.selectedApproverAction=selectedAction;
			txnJsonData.transactionPrimId=transactionEntityId;
			txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
			txnJsonData.suppDoc=supportingDoc;
			txnJsonData.txnRmarks=remarks;
			var url="/claims/approverAction";
			$.ajax({
				url: url,
				data:JSON.stringify(txnJsonData),
				type:"text",
				headers:{
					"X-AUTH-TOKEN": window.authToken
				},
				method:"POST",
				contentType:'application/json',
				success: function (data) {
					if(data.resultantAmount < 0){
						if(data.branchBankDetailEntered === false){
							swal("Insufficient balance in the bank account!", "Use alternative payment mode or infuse funds into the bank account. Current bank balance is:"+data.resultantAmount, "error");
							disableTransactionButtons();
							return false;
						}else{					
							swal("Bank Balance is in negative!", "This bank account type allows -ve balance so transaction is successful. Note current bank balance is:"+data.resultantAmount, "warning");
						}
					}	
					if(data.resultantCash < 0){
						swal("Insufficient balance in the bank account!","Insufficient balance in the cash account. Use alternative payment mode or infuse funds into the cash account. Effective cash balance is: " + data.resultantCash,"warning");
					}
					if(data.resultantPettyCashAmount < 0){
						swal("Insufficient balance in the bank account!","Insufficient balance in the petty cash account. Use alternative payment mode or infuse funds into petty cash account. Effective petty cash balance is: " + data.resultantPettyCashAmount,"warning");
					}
				},
				error: function (xhr, status, error) {
					if(xhr.status == 401){ doLogout(); }
				}
			});
		}
	}else{
		//send server for action to complete
		var txnJsonData={};
		txnJsonData.email=$("#hiddenuseremail").text();
		txnJsonData.selectedApproverAction=selectedAction;
		txnJsonData.transactionPrimId=transactionEntityId;
		txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
		txnJsonData.suppDoc=supportingDoc;
		txnJsonData.txnRmarks=remarks;
		var url="/claims/approverAction";
		$.ajax({
			url: url,
			data:JSON.stringify(txnJsonData),
			type:"text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method:"POST",
			contentType:'application/json',
			success: function (data) {
				if(data.resultantAmount < 0){
						if(data.branchBankDetailEntered === false){
							swal("Insufficient balance in the bank account!", "Use alternative payment mode or infuse funds into the bank account. Current bank balance is:"+data.resultantAmount, "error");
							disableTransactionButtons();
							return false;
						}else{					
							swal("Bank Balance is in negative!", "This bank account type allows -ve balance so transaction is successful. Note current bank balance is:"+data.resultantAmount, "warning");
						}
				}
				if(data.resultantCash < 0){
					swal("Insufficient balance in the bank account!","Insufficient balance in the cash account. Use alternative payment mode or infuse funds into the cash account. Effective cash balance is: " + data.resultantCash,"warning");
				}
				if(data.resultantPettyCashAmount < 0){
					swal("Insufficient balance in the bank account!","Insufficient balance in the petty cash account. Use alternative payment mode or infuse funds into petty cash account. Effective petty cash balance is: " + data.resultantPettyCashAmount,"warning");
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	}
}


// ************** Fill Module Claim Details **************


$(function() {
	$(".clmInvoiceDate").datepicker({
		changeMonth : true,
		changeYear : true,
		dateFormat:  'MM d,yy',
		yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
		onSelect: function(x,y){
	        $(this).focus();
	    }
	});
	
	$(".closeClaimSettlementModule").on('click',function(){
		 getDetailsTotalForClaims("clmTravelExpensesModule","clmTravelExpensesTR");
		 getDetailsTotalForClaims("clmLodgingExpensesModule","clmLodgingExpensesTR");
		 getDetailsTotalForClaims("clmOtherExpensesModule","clmOtherExpensesTR");
		 getDetailsTotalForClaims("clmFixedDIAMModule","clmFixedDIAMTR");
		 getDetailsTotalForClaims("clmIncurredExpensesModule","clmIncurredExpensesTR");
		 getDetailsTotalForClaims("clmReiEmbExpensesModule","cREEtrid");
			$('.clmSettleModule').slideUp('fast');
	});
	
});

function showSettleClaimDetails(elem) {
	var parentTr = $(elem).closest('tr').attr('id');
		$('.clmSettleModule').slideUp('fast');
	if(parentTr == "clmTravelExpensesTR" || parentTr == "clmLodgingExpensesTR" || parentTr == "clmOtherExpensesTR" || parentTr == "clmFixedDIAMTR") {
		var claimSelected = $("#availableUnsettledClaimAdvances").val();
		if(claimSelected == "") {
			swal("Incomplete detail!", "Please select Unsettled Claim Advances", "error");
			return false;
		}
		if(parentTr == "clmTravelExpensesTR") {
			validateTRFonOpen("clmTravelExpensesModule");
			$('#clmTravelExpensesModule').slideDown('slow');
		}else if(parentTr == "clmLodgingExpensesTR") {
			validateTRFonOpen("clmLodgingExpensesModule");
			$('#clmLodgingExpensesModule').slideDown('slow');
		}else if(parentTr == "clmOtherExpensesTR") {
			validateTRFonOpen("clmOtherExpensesModule");
			$('#clmOtherExpensesModule').slideDown('slow');
		}else if(parentTr == "clmFixedDIAMTR") {
			validateTRFonOpen("clmFixedDIAMModule");
			$('#clmFixedDIAMModule').slideDown('slow');
		}
	}else if(parentTr == "clmIncurredExpensesTR") {
		var claimSelected = $("#availableUnsettledExpenseAdvances").val();
		if(claimSelected == "") {
			swal("Incomplete detail!", "Please select Unsettled Expense Advances", "error");
			return false;
		}
		validateTRFonOpen("clmIncurredExpensesModule");
		$('#clmIncurredExpensesModule').slideDown('slow');
	}else if(parentTr == "cREEtrid") {
		var branchSelected = $("#cREEtrid").find(".claimBranch").val();
		var itemSelected = $("#cREEtrid").find(".expenseClaimItem").val();
		if(branchSelected == "") {
			swal("Incomplete detail!", "Please select Branch First", "error");
			return false;
		}
		if(itemSelected == "") {
			swal("Incomplete detail!", "Please select Expense Claim Item", "error");
			return false;
		}
		validateTRFonOpen("clmReiEmbExpensesModule");
		$('#clmReiEmbExpensesModule').slideDown('slow');
	}
}

function validateTRFonOpen(modalId){
	
	var count = $("#"+modalId).find("tbody tr").length;
	if(count == 0) {
		$("#"+modalId).find("#addClaimDetailsRow").click();
	}
}


var calculateClaimGross = function(elem) {

	var parentTr = $(elem).closest('tr');
	var qty = parentTr.find('.clmQuantity').val();
	var rate = parentTr.find('.clmRate').val();
	if(qty == "" || qty == "0") {
		parentTr.find('.clmGross').val("");
        return false;
	}
	if(rate == "" || rate == "0") {
		parentTr.find('.clmGross').val("");
        return false;
	}
	 if ($.isNumeric(qty) && $.isNumeric(rate)) {
            qty = Number(qty);
            rate = Number(rate);
            var gross = rate * qty;
            parentTr.find('.clmGross').val(gross);
            grossChange(elem,gross);
        } else {
            parentTr.find('.clmGross').val("");
            grossChange(elem,"");
        }
	 
	 netClaimRowAmount(elem);
}



function getClaimSettleRow(data){
	var cgstlistData = [];
	var i=0;
	for(var j=0; j < data.cgstRateList.length;j++) {
		cgstlistData[i++] = '<option value="'+data.cgstRateList[j].id+'" rate="'+data.cgstRateList[j].rate+'">';
		cgstlistData[i++] = data.cgstRateList[j].name;
		cgstlistData[i++] = '</option>';
	}
	var sgstlistData = [];
	 i=0;
	for(var j=0; j < data.sgstRateList.length;j++) {
		sgstlistData[i++] = '<option value="'+data.sgstRateList[j].id+'" rate="'+data.sgstRateList[j].rate+'">';
		sgstlistData[i++] = data.sgstRateList[j].name;
		sgstlistData[i++] = '</option>';
	}
	var igstlistData = [];
	 i=0;
	for(var j=0; j < data.igstRateList.length;j++) {
		igstlistData[i++] = '<option value="'+data.igstRateList[j].id+'" rate="'+data.igstRateList[j].rate+'">';
		igstlistData[i++] = data.igstRateList[j].name;
		igstlistData[i++] = '</option>';
	}
	var cesslistData = [];
	 i=0;
	for(var j=0; j < data.cessRateList.length;j++) {
		cesslistData[i++] = '<option value="'+data.cessRateList[j].id+'" rate="'+data.cessRateList[j].rate+'">';
		cesslistData[i++] = data.cessRateList[j].name;
		cesslistData[i++] = '</option>';
	}
	var itemData = [];
	 i=0;
	for(var j=0; j < data.itemDataList.length;j++) {
		itemData[i++] = '<option value="'+data.itemDataList[j].id+'">';
		itemData[i++] = data.itemDataList[j].name;
		itemData[i++] = '</option>';
	}
    var creditRow = [];
     i = 0;
    creditRow[i++] = '<tr><td><select class="clmItem" onchange="validateClaimItem(this);" ><option value="">--Please Select--</option>';
    creditRow[i++] =  itemData.join('');
    creditRow[i++] = '</select></td>';
    creditRow[i++] = '<td><input type="text" class="clmVendorName"/><input type="hidden" class="claimDetailsId"/></td>';
	creditRow[i++] = '<td><select class="clmRegistered" onchange="isGstApplicaleClaim(this);" ><option value="">--Please Select--</option><option value="1">YES</option><option value="0">NO</option></select></td>';
	creditRow[i++] = '<td><input type="text" class="clmVendorGstIn" readonly onkeyup="validateFullGSTINClaim(this);" maxlength="15"/>';
	creditRow[i++] = '</br><select id="vendorState" class="clmVendorState" ></select></td>';
	creditRow[i++] = '<td><input type="text" class="clmInvoiceNo" placeholder="Invoice No" onkeypress="validateIsRegistered(this);" />';
	creditRow[i++] = '</br><input class="clmInvoiceDate datepicker" placeholder="Invoice Date" type="text"></td>';
//	creditRow[i++] = '<td><input type="text" class="clmItemName" onkeypress="validateIsRegistered(this);" /></td>';
//	creditRow[i++] = '<td><input type="text" class="clmHsnCode" onkeypress="validateIsRegistered(this);" /></td>';
//	creditRow[i++] = '<td><input type="text" class="clmProductDesc" onkeypress="validateIsRegistered(this);" /></td>';
	creditRow[i++] = '<td><input type="text" class="clmUqc" onkeypress="validateIsRegistered(this);"/></td>';
	creditRow[i++] = '<td><input type="text" class="clmQuantity" onkeyup="calculateClaimGross(this);" onkeypress="return onlyDotsAndNumbers(event); validateIsRegistered(this);" /></td>';
	creditRow[i++] = '<td><input type="text" class="clmRate" onkeyup="calculateClaimGross(this);" onkeypress="return onlyDotsAndNumbers(event); validateIsRegistered(this);" /></td>';
	creditRow[i++] = '<td><input type="text" class="clmGross" readonly /></td>';
    creditRow[i++] = '<td><select class="clmSgstRate" onchange="validateGstClaim(this);calculateClaimRowTax(this);" ><option value="">--Please Select--</option>';
    creditRow[i++] =  sgstlistData.join('');
    creditRow[i++] = '</select>';
    creditRow[i++] = '</br><input type="text" class="clmSgstAmount" onkeypress="return onlyDotsAndNumbers(event);validateIsRegistered(this);" onkeyup="checkLedgerSelected(this);netClaimRowAmount(this);" /></td>';
	creditRow[i++] = '<td><select class="clmCgstRate" onchange="validateGstClaim(this);calculateClaimRowTax(this); " ><option value="">--Please Select--</option>';
	creditRow[i++] = cgstlistData.join('');
	creditRow[i++] = '</select>';
	creditRow[i++] = '</br><input type="text" class="clmCgstAmount" onkeypress="return onlyDotsAndNumbers(event); validateIsRegistered(this);" onkeyup="checkLedgerSelected(this);netClaimRowAmount(this);" /></td>';
	creditRow[i++] = '<td><select class="clmIgstRate" onchange="validateGstClaim(this);calculateClaimRowTax(this);" ><option value="">--Please Select--</option>';
	creditRow[i++] =  igstlistData.join('');
	creditRow[i++] = '</select>';
	creditRow[i++] = '</br><input type="text" class="clmIgstAmount" onkeypress="return onlyDotsAndNumbers(event);validateIsRegistered(this);" onkeyup="checkLedgerSelected(this);netClaimRowAmount(this);" /></td>';
	creditRow[i++] = '<td><select class="clmCessRate" onchange="calculateClaimRowTax(this);" ><option value="">--Please Select--</option>';
	creditRow[i++] =  cesslistData.join('');
	creditRow[i++] = '</select>';
	creditRow[i++] = '</br><input type="text" class="clmCessAmount" onkeypress="return onlyDotsAndNumbers(event);validateIsRegistered(this);" onkeyup="checkLedgerSelected(this);netClaimRowAmount(this);" /></td>';
	creditRow[i++] = '<td><input type="text" class="clmNetTotal"></td>';
	creditRow[i++] = '<td><input type="checkbox" class="removeCheckBox"></td>';
	creditRow[i++] = '</tr>';
    return creditRow.join('');
}


var addClaimSettlementRow = function(elem) {
	var parentTable = $(elem).closest('div').find('table');
	var parentId = $(elem).closest('.clmSettleModule').attr('id');
	var mappingId = ""; 
	if(parentId == "clmTravelExpensesModule") {
		mappingId = "24";
	}else if(parentId == "clmLodgingExpensesModule") {
		mappingId = "25";
	}else if(parentId == "clmOtherExpensesModule") {
		mappingId = "26";
	}else if(parentId == "clmFixedDIAMModule") {
		mappingId = "27";
	}else {
		mappingId = "";
	}
	if(parentTable.find("tbody tr").length > 0) {
		var lastTr = parentTable.find("tbody tr:last");
		var qty = $.trim(lastTr.find(".clmQuantity").val());
		var rate = $.trim(lastTr.find(".clmRate").val());
		var isRegistered = $.trim(lastTr.find(".clmRegistered").val());
		var gstIn = $.trim(lastTr.find(".clmVendorGstIn").val());
		if(mappingId !== "") {
			var item = $.trim(lastTr.find(".clmItem option:selected").val());
			if(item == "") {
				swal("Incomplete detail!", "Please Select Claim Item First", "error");
				return false;
			}
		}
		if(qty == "0" || qty == "") {
			swal("Incomplete detail!", "Please Enter Quantity in Last Row.", "error");
			return false;
		}
		if(rate == "0" || rate == "") {
			swal("Incomplete detail!", "Please Enter Rate in Last Row.", "error");
			return false;
		}
		if(isRegistered == "1" && gstIn == "") {
		 	swal("Incomplete detail!", "Please Enter GSTIN of Vendor/Supplier", "error");
		 	return false;
		}
	}
	
	
	var jsonData={};
		jsonData.email=$("#hiddenuseremail").text();
		jsonData.mappingId = mappingId;
		var url="/claims/getclaimgstdata";
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
				
				var itemRow = getClaimSettleRow(data);
				 parentTable.find("tbody").append(itemRow);
				 parentTable.find("tbody tr:last").find(".clmInvoiceDate").datepicker({
					changeMonth : true,
					changeYear : true,
					dateFormat:  'MM d,yy',
					yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
					onSelect: function(x,y){
				        $(this).focus();
				    }
				});
				 parentTable.find("tbody tr:last").find(".clmVendorState").bfhstates({country:'IN', blank:false});
				if(parentId == "clmIncurredExpensesModule" || parentId == "clmReiEmbExpensesModule" ) {
					parentTable.find(".clmItem").closest("td").hide();
					
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});

}

var removeClaimSettlementRow = function(elem) {
	  var parentTable= $(elem).closest('div').find('table');
	        parentTable.find(".removeCheckBox:checkbox:checked").each(function(){
	            $(this).closest('tr').remove();
	        });
}

var getDetailsTotalForClaims = function(moduleId,rowId) {
	var grossTotal = 0;
	var taxTotal = 0;
	$("#"+moduleId).find('table tbody').find('tr').each(function(){
		var tax = 0;
		var gross = $(this).find('.clmGross').val();
		var cgst = $(this).find('.clmCgstAmount').val();
		var sgst = $(this).find('.clmSgstAmount').val();
		var igst = $(this).find('.clmIgstAmount').val();
		var cess = $(this).find('.clmCessAmount').val();
		
		if(gross !== "" && $.isNumeric(gross)) {
			grossTotal += Number(gross);
		}
		if(cgst !== "" && $.isNumeric(cgst)) {
			tax += Number(cgst);
		}
		if(sgst !== "" && $.isNumeric(sgst)) {
			tax += Number(sgst);
		}
		if(igst !== "" && $.isNumeric(igst)) {
			tax += Number(igst);
		}
		if(cess !== "" && $.isNumeric(cess)) {
			tax += Number(cess);
		}
		taxTotal += tax;
	});
/*	var balanceUnsettledAgainstThisTxn=$("#balanceUnsettledAgainstThisTxn").text();
	var expIncurredAmount = grossTotal+taxTotal;
	if(balanceUnsettledAgainstThisTxn != "" ) {
		var balanceUnsettled=parseFloat(balanceUnsettledAgainstThisTxn);
			 if(expIncurredAmount > balanceUnsettled) {
			 	swal("Incorrect Amount!", "Total amount is more than Advance available.", "error");
				return false;
			 }
		$('#'+rowId).find('.clmExpenceAmt').val(grossTotal);
		$('#'+rowId).find('.clmExpenceTax').val(taxTotal);
		$('#'+rowId).find('.clmExpenceGross').val((grossTotal+taxTotal));
	}else {
			if(moduleId == "clmReiEmbExpensesModule" || moduleId == "clmIncurredExpensesModule") {
				$('#'+rowId).find('.clmExpenceAmt').val(grossTotal);
				$('#'+rowId).find('.clmExpenceTax').val(taxTotal);
				$('#'+rowId).find('.clmExpenceGross').val((grossTotal+taxTotal));
			}else {
				$('#'+rowId).find('.clmExpenceAmt').val("0");
				$('#'+rowId).find('.clmExpenceTax').val("0");
				$('#'+rowId).find('.clmExpenceGross').val("0");
				return false;
			}
	}*/
	$('#'+rowId).find('.clmExpenceAmt').val(grossTotal);
	$('#'+rowId).find('.clmExpenceTax').val(taxTotal);
	$('#'+rowId).find('.clmExpenceGross').val((grossTotal+taxTotal));
	
	if(moduleId == "clmIncurredExpensesModule") {
		calculateTotalExpenseIncurredOnExpAdvanceThisTxn($("#item1ExpIncurredAmount"));
	}else if(moduleId == "clmReiEmbExpensesModule") {
		validateReimbursementAmount($("#expenseReimbursementAmountRequired"));
	}else {
		calculateTotalExpenseIncurredOnThisTxn($("#expenseIncurredTravel"));
	}
	return true;
} 

var validateGstClaim = function(elem) {
	var parentTr = $(elem).closest('tr');
	var patentTD = $(elem).closest('td');
	var grossAmount = parentTr.find(".clmGross").val();
	var rate =  $(elem).val();
	if(grossAmount == "") {
		swal("Invalid Gross Amount", "Gross Amount Must be getter than 0.", "error");
		$(elem).val("");
		return false;
	}
	var cgstRate = parentTr.find("td .clmCgstRate option:selected").val();
	var sgstRate = parentTr.find("td .clmSgstRate option:selected").val();
	var igstRate = parentTr.find("td .clmIgstRate option:selected").val();
	if(cgstRate != "" || sgstRate != "" || igstRate != "") {
		if(cgstRate != "" || sgstRate != "") {
			parentTr.find("td .clmIgstRate option:first").prop("selected", "selected");
			parentTr.find("td input[class='clmIgstAmount']").val("");
			parentTr.find("td .clmIgstRate").prop('disabled', 'disabled');
			parentTr.find("td input[class='clmIgstAmount']").prop('disabled', 'disabled');
			
			parentTr.find("td .clmCgstRate").prop('disabled', false);
			parentTr.find("td input[class='clmCgstAmount']").prop('disabled', false);
			parentTr.find("td .clmSgstRate").prop('disabled', false);
			parentTr.find("td input[class='clmSgstAmount']").prop('disabled', false);
			
		}else if(igstRate != "") {
			parentTr.find("td .clmCgstRate option:first").prop("selected", "selected");
			parentTr.find("td input[class='clmCgstAmount']").val("");
			parentTr.find("td .clmSgstRate option:first").prop("selected", "selected");
			parentTr.find("td input[class='clmSgstAmount']").val("");
			
			parentTr.find("td .clmCgstRate").prop('disabled', 'disabled');
			parentTr.find("td input[class='clmCgstAmount']").prop('disabled', 'disabled');
			parentTr.find("td .clmSgstRate").prop('disabled', 'disabled');
			parentTr.find("td input[class='clmSgstAmount']").prop('disabled', 'disabled');
			parentTr.find("td .clmIgstRate").prop('disabled', false);
			parentTr.find("td input[class='clmIgstAmount']").prop('disabled', false);
		}
	}else {
		parentTr.find("td input[class='clmCgstAmount']").val("");
		parentTr.find("td input[class='clmSgstAmount']").val("");
		parentTr.find("td input[class='clmIgstAmount']").val("");
		parentTr.find("td .clmCgstRate").prop('disabled', false);
		parentTr.find("td input[class='clmCgstAmount']").prop('disabled', false);
		parentTr.find("td .clmSgstRate").prop('disabled', false);
		parentTr.find("td input[class='clmSgstAmount']").prop('disabled', false);
		parentTr.find("td .clmIgstRate").prop('disabled', false);
		parentTr.find("td input[class='clmIgstAmount']").prop('disabled', false);
	}
}



var fetchClaimMultiItemData = function(modalId) {
	 var parentTable = $("#"+modalId).find('table');
     var multipleItemsData = [];
   	 parentTable.find("tbody>tr").each(function() {
       	 var json = {};
     	 var grossAmt = $(this).find("td input[class='clmGross']").val();
     	 if(grossAmt !== "" && typeof grossAmt!=='undefined' ){
			json.claimDetailsId = $.trim($(this).find("td input[class='claimDetailsId']").val());
			json.claimItem = $.trim($(this).find("td .clmItem option:selected").val());
			json.vendorName = $.trim($(this).find("td input[class='clmVendorName']").val());
			json.isRegistered = $.trim($(this).find("td .clmRegistered option:selected").val());
			json.vendorGstin = $.trim($(this).find("td input[class='clmVendorGstIn']").val());
			json.vendorState =$.trim( $(this).find("td .clmVendorState option:selected").val());
			json.invoiceNo = $.trim($(this).find("td input[class='clmInvoiceNo']").val());
			json.invoiceDate = $.trim($(this).find(".clmInvoiceDate").val());
//			json.itemName = $.trim($(this).find("td input[class='clmItemName']").val());
//			json.hsnCode = $.trim($(this).find("td input[class='clmHsnCode']").val());
//			json.productDesc = $.trim($(this).find("td input[class='clmProductDesc']").val());
			json.uqc = $.trim($(this).find("td input[class='clmUqc']").val());
			json.quantity = $.trim($(this).find("td input[class='clmQuantity']").val());
			json.rate = $.trim($(this).find("td input[class='clmRate']").val());
			json.grossAmt =  grossAmt;
			json.sgstID = $.trim($(this).find("td .clmSgstRate option:selected").val());
			json.sgstAmt = $.trim($(this).find("td input[class='clmSgstAmount']").val());
			json.sgstRate = $.trim($(this).find("td .clmSgstRate option:selected").attr("rate"));
			json.cgstID = $.trim($(this).find("td .clmCgstRate option:selected").val());
			json.cgstAmt = $.trim($(this).find("td input[class='clmCgstAmount']").val());
			json.cgstRate = $.trim($(this).find("td .clmCgstRate option:selected").attr("rate"));
			json.igstID = $.trim($(this).find("td .clmIgstRate option:selected").val());
			json.igstAmt = $.trim($(this).find("td input[class='clmIgstAmount']").val());
			json.igstRate = $.trim($(this).find("td .clmIgstRate option:selected").attr("rate"));
			json.cessID = $.trim($(this).find("td .clmCessRate option:selected").val());
			json.cessAmt = $.trim($(this).find("td input[class='clmCessAmount']").val());
            json.cessRate = $.trim($(this).find("td .clmCessRate option:selected").attr("rate"));
            json.netAmt = $.trim($(this).find("td input[class='clmNetTotal']").val());
            multipleItemsData.push(JSON.stringify(json));
       	 }
    });
    return multipleItemsData;
}

var validateIsRegistered = function(elem) {
	
	var parentTr = $(elem).closest('tr');
	var registared = parentTr.find("td .clmRegistered option:selected").val();
	var gstIn = $.trim(parentTr.find("td input[class='clmVendorGstIn']").val());
	if(registared == "1" && gstIn == "") {
		swal("Incomplete Claim detail!", "Please Enter Vendor/Supplier GSTIN.", "error");
		return false;
	}
}

var setBillwiseDetails = function(dataList,modalId) {
			$("#"+modalId).find('table tbody').html("");;
			if(dataList.length > 0) {
			for(var i=0; i< dataList.length; i++) {
					addRowForUpdate(modalId,dataList,i);
				}
			}
}

function addRowForUpdate(modalId,dataList,index) {
	var parentTable = $("#"+modalId).find('table');
		var jsonData={};
		jsonData.email=$("#hiddenuseremail").text();
		
		var url="/claims/getclaimgstdata";
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
				var cgstlistData = [];
				var i=0;
				for(var j=0; j < data.cgstRateList.length;j++) {
					if(dataList[index].cgstRate == data.cgstRateList[j].id) {
						cgstlistData[i++] = '<option value="'+data.cgstRateList[j].id+'" rate="'+data.cgstRateList[j].rate+'"  selected="selected">';
					}else {
						cgstlistData[i++] = '<option value="'+data.cgstRateList[j].id+'" rate="'+data.cgstRateList[j].rate+'">';
					}
					
					cgstlistData[i++] = data.cgstRateList[j].name;
					cgstlistData[i++] = '</option>';
				}
			var sgstlistData = [];
				 i=0;
				for(var j=0; j < data.sgstRateList.length;j++) {
					if(dataList[index].sgstRate == data.sgstRateList[j].id) {
						sgstlistData[i++] = '<option value="'+data.sgstRateList[j].id+'" rate="'+data.sgstRateList[j].rate+'"  selected="selected">';
					}else {
						sgstlistData[i++] = '<option value="'+data.sgstRateList[j].id+'" rate="'+data.sgstRateList[j].rate+'">';
					}
					
					sgstlistData[i++] = data.sgstRateList[j].name;
					sgstlistData[i++] = '</option>';
				}
			var igstlistData = [];
				 i=0;
				for(var j=0; j < data.igstRateList.length;j++) {
					if(dataList[index].igstRate == data.igstRateList[j].id) {
						igstlistData[i++] = '<option value="'+data.igstRateList[j].id+'" rate="'+data.igstRateList[j].rate+'"  selected="selected">';
					}else {
						igstlistData[i++] = '<option value="'+data.igstRateList[j].id+'" rate="'+data.igstRateList[j].rate+'">';
					}
					
					
					igstlistData[i++] = data.igstRateList[j].name;
					igstlistData[i++] = '</option>';
				}
				var cesslistData = [];
				 i=0;
				for(var j=0; j < data.cessRateList.length;j++) {
					if(dataList[index].cessRate == data.cessRateList[j].id) {
						cesslistData[i++] = '<option value="'+data.cessRateList[j].id+'" rate="'+data.cessRateList[j].rate+'"  selected="selected">';
					}else {
						cesslistData[i++] = '<option value="'+data.cessRateList[j].id+'" rate="'+data.cessRateList[j].rate+'">';
					}
					cesslistData[i++] = data.cessRateList[j].name;
					cesslistData[i++] = '</option>';
				}	
				var itemData = [];
				 i=0;
				for(var j=0; j < data.itemDataList.length;j++) {
					if(dataList[index].cessRate == data.itemDataList[j].id) {
						itemData[i++] = '<option value="'+data.itemDataList[j].id+'" selected="selected">';
					}else {
						itemData[i++] = '<option value="'+data.itemDataList[j].id+'">';
					}
					itemData[i++] = data.itemDataList[j].name;
					itemData[i++] = '</option>';
				}
    var creditRow = [];
     i = 0;
    creditRow[i++] = '<tr><td><select class="clmItem" onchange="validateClaimItem(this);" ><option value="">--Please Select--</option>';
    creditRow[i++] =  itemData.join('');
    creditRow[i++] = '</select></td>';
    creditRow[i++] = '<td><input type="text" class="clmVendorName"/><input type="hidden" class="claimDetailsId"/></td>';
	creditRow[i++] = '<td><select class="clmRegistered" onchange="isGstApplicaleClaim(this);" ><option value="">--Please Select--</option><option value="1">YES</option><option value="0">NO</option></select></td>';
	creditRow[i++] = '<td><input type="text" class="clmVendorGstIn" readonly maxlength="15" onkeyup="validateFullGSTINClaim(this);"/>';
	creditRow[i++] = '</br><select id="vendorState" class="clmVendorState" ></select></td>';
	creditRow[i++] = '<td><input type="text" class="clmInvoiceNo" placeholder="Invoice No" onkeypress="validateIsRegistered(this);" />';
	creditRow[i++] = '</br><input class="clmInvoiceDate datepicker" placeholder="Invoice Date" type="text"></td>';
//	creditRow[i++] = '<td><input type="text" class="clmItemName" onkeypress="validateIsRegistered(this);" /></td>';
//	creditRow[i++] = '<td><input type="text" class="clmHsnCode" onkeypress="validateIsRegistered(this);" /></td>';
//	creditRow[i++] = '<td><input type="text" class="clmProductDesc" onkeypress="validateIsRegistered(this);" /></td>';
	creditRow[i++] = '<td><input type="text" class="clmUqc" onkeypress="validateIsRegistered(this);"/></td>';
	creditRow[i++] = '<td><input type="text" class="clmQuantity" onkeyup="calculateClaimGross(this);" onkeypress="return onlyDotsAndNumbers(event); validateIsRegistered(this);" /></td>';
	creditRow[i++] = '<td><input type="text" class="clmRate" onkeyup="calculateClaimGross(this);" onkeypress="return onlyDotsAndNumbers(event); validateIsRegistered(this);" /></td>';
	creditRow[i++] = '<td><input type="text" class="clmGross" readonly /></td>';
    creditRow[i++] = '<td><select class="clmSgstRate" onchange="validateGstClaim(this);calculateClaimRowTax(this);" ><option value="">--Please Select--</option>';
    creditRow[i++] =  sgstlistData.join('');
    creditRow[i++] = '</select>';
    creditRow[i++] = '</br><input type="text" class="clmSgstAmount" onkeypress="return onlyDotsAndNumbers(event);validateIsRegistered(this);" onkeyup="checkLedgerSelected(this);netClaimRowAmount(this);" /></td>';
	creditRow[i++] = '<td><select class="clmCgstRate" onchange="validateGstClaim(this);calculateClaimRowTax(this); " ><option value="">--Please Select--</option>';
	creditRow[i++] = cgstlistData.join('');
	creditRow[i++] = '</select>';
	creditRow[i++] = '</br><input type="text" class="clmCgstAmount" onkeypress="return onlyDotsAndNumbers(event); validateIsRegistered(this);" onkeyup="checkLedgerSelected(this);netClaimRowAmount(this);" /></td>';
	creditRow[i++] = '<td><select class="clmIgstRate" onchange="validateGstClaim(this);calculateClaimRowTax(this);" ><option value="">--Please Select--</option>';
	creditRow[i++] =  igstlistData.join('');
	creditRow[i++] = '</select>';
	creditRow[i++] = '</br><input type="text" class="clmIgstAmount" onkeypress="return onlyDotsAndNumbers(event);validateIsRegistered(this);" onkeyup="checkLedgerSelected(this);netClaimRowAmount(this);" /></td>';
	creditRow[i++] = '<td><select class="clmCessRate" onchange="calculateClaimRowTax(this);" ><option value="">--Please Select--</option>';
	creditRow[i++] =  cesslistData.join('');
	creditRow[i++] = '</select>';
	creditRow[i++] = '</br><input type="text" class="clmCessAmount" onkeypress="return onlyDotsAndNumbers(event);validateIsRegistered(this);" onkeyup="checkLedgerSelected(this);netClaimRowAmount(this);" /></td>';
	creditRow[i++] = '<td><input type="text" class="clmNetTotal"></td>';
	creditRow[i++] = '<td><input type="checkbox" class="removeCheckBox"></td>';
	creditRow[i++] = '</tr>';
				var itemRow = creditRow.join('');
				 parentTable.find("tbody").append(itemRow);
				 parentTable.find("tbody tr:last").find(".clmInvoiceDate").datepicker({
					changeMonth : true,
					changeYear : true,
					dateFormat:  'MM d,yy',
					yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
					onSelect: function(x,y){
				        $(this).focus();
				    }
				});
				 parentTable.find("tbody tr:last").find(".clmVendorState").bfhstates({country:'IN', blank:false});
				 parentTable.find("tbody tr:last").find(".claimDetailsId").val(dataList[index].claimDetailsId);
				 parentTable.find("tbody tr:last").find(".clmVendorName").val(dataList[index].vendorName);
		       	 parentTable.find("tbody tr:last").find(".clmRegistered").val(dataList[index].isRegistered).change();
		       	 parentTable.find("tbody tr:last").find("td input[class='clmVendorGstIn']").val(dataList[index].vendorGstin);
		       	  parentTable.find("tbody tr:last").find('select[id="vendorState"] option').filter(function () {return $(this).text()==dataList[index].vendorState}).prop("selected", "selected");
		         parentTable.find("tbody tr:last").find("td input[class='clmInvoiceNo']").val(dataList[index].invoiceNo);
		         var dateId = parentTable.find("tbody tr:last").find('.clmInvoiceDate').attr("id");
		         $("#"+dateId).val(dataList[index].invoiceDate);
//		         parentTable.find("tbody tr:last").find("td input[class='clmItemName']").val(dataList[index].itemName);
//		         parentTable.find("tbody tr:last").find("td input[class='clmHsnCode']").val(dataList[index].hsnCode);
//		         parentTable.find("tbody tr:last").find("td input[class='clmProductDesc']").val(dataList[index].productDesc);
		       	 parentTable.find("tbody tr:last").find("td input[class='clmUqc']").val(dataList[index].uqc);
            	 parentTable.find("tbody tr:last").find("td input[class='clmQuantity']").val(dataList[index].quantity);
        		 parentTable.find("tbody tr:last").find("td input[class='clmRate']").val(dataList[index].rate);
        		 parentTable.find("tbody tr:last").find("td input[class='clmGross']").val(dataList[index].grossAmt);
        		 parentTable.find("tbody tr:last").find("td input[class='clmNetTotal']").val(dataList[index].netAmt);
        		//  parentTable.find("tbody tr:last").find('.clmCgstRate option[value='+dataList[index].cgstRate+']').attr("selected","selected");
        		 //  parentTable.find("tbody tr:last").find('.clmCgstRate').change();
        		// parentTable.find("tbody tr:last").find('.clmCgstRate option[value="'+dataList[index].cgstRate+'"]').attr('selected','selected');
	         	 parentTable.find("tbody tr:last").find("td input[class='clmCgstAmount']").val(dataList[index].cgstAmt);
	         	 //parentTable.find("tbody tr:last").find('.clmSgstRate option[value="'+dataList[index].sgstRate+'"]').attr('selected','selected');
	        	 parentTable.find("tbody tr:last").find("td input[class='clmSgstAmount']").val(dataList[index].sgstAmt);
	        	// parentTable.find("tbody tr:last").find('.clmIgstRate option[value="'+dataList[index].igstRate+'"]').attr('selected','selected');
	         	 parentTable.find("tbody tr:last").find("td input[class='clmIgstAmount']").val(dataList[index].igstAmt);
	         	// parentTable.find("tbody tr:last").find('.clmCessRate option[value="'+dataList[index].cessRate+'"]').attr('selected','selected');
	         	 parentTable.find("tbody tr:last").find("td input[class='clmCessAmount']").val(dataList[index].cessAmt);
				
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
}
var checkLedgerSelected = function(elem) {
	var parentTr = $(elem).closest('tr');
	var cls = $(elem).attr('class');
	var amt = $.trim($(elem).val());
	
	var ledgerClass = "";
	if(amt != "") {
		if(cls.indexOf("Cgst") != -1) {
			ledgerClass = "clmCgstRate";
		}else if(cls.indexOf("Sgst") != -1) {
			ledgerClass = "clmSgstRate";
		}else if(cls.indexOf("Igst") != -1) {
			ledgerClass = "clmIgstRate";
		}else if(cls.indexOf("Cess") != -1) {
			ledgerClass = "clmCessRate";
		}
		var selectedLedger = parentTr.find("."+ledgerClass).val();
		if(selectedLedger == "") {
			$(elem).val("");
			if(ledgerClass.indexOf("Igst") != -1){
   				swal("IGST detail!", "Please Select Igst Tax Rate First.", "error");
   				return false;
			}
			if(ledgerClass.indexOf("Cgst") != -1){
   				swal("CGST detail!", "Please Select Cgst Tax Rate First.", "error");
   				return false;
			}
			if(ledgerClass.indexOf("Sgst") != -1){
   				swal("SGST detail!", "Please Select Sgst Tax Rate First.", "error");
   				return false;
			}
			if(ledgerClass.indexOf("Cess") != -1){
   				swal("CESS detail!", "Please Select Cess Tax Rate First.", "error");
   				return false;
			}
			
		}
	}
}

function validateFullGSTINClaim(elem) {
	var value = $.trim($(elem).val());
	if(value !== "" && value.length > 2) {
			var code = value.substring(0, 2);
			var gstIn = value.substring(2, value.length);
			var status = validateGSTINStateCode(code);
    		if(status == true) {
				var pos = gstIn.length-1;
				var ch = gstIn.charAt(gstIn.length-1);
				var codeStatus = validatePositionINGSTIN(pos,ch);
				if(codeStatus == false) {
					var newGSTIN = gstIn.substr(0, pos);
					newGSTIN = code + newGSTIN;
					$(elem).val(newGSTIN);
					return false;
				}
			}else {
				 swal("Invalid state!", "provide valid Indian state code in GSTIN", "error");
				 $(elem).val("");
				 $(elem).focus();
				 return false;
			}
	}
	return true;
}

function isGstApplicaleClaim(elem) {
	var val = $(elem).val();
	if(val == 1) {
		$(elem).closest("tr").find(".clmVendorGstIn").val("");
		$(elem).closest("tr").find(".clmVendorGstIn").attr('readonly', false);
	}else {
		$(elem).closest("tr").find(".clmVendorGstIn").val("");
		$(elem).closest("tr").find(".clmVendorGstIn").attr('readonly', true);
	}
}

function calculateClaimRowTax(elem) {
	var parentTr = $(elem).closest('tr');
	var parentTd = $(elem).closest('td');
	var grossAmount = parentTr.find(".clmGross").val();
	var rate =  $(elem).find('option:selected').attr('rate');
	if(grossAmount == "") {
		swal("Invalid Gross Amount", "Gross Amount Must be getter than 0.", "error");
		$(elem).val("");
		return false;
	}
	if(rate !== "" && grossAmount !== "") {
		var tax = (parseFloat(grossAmount) * parseFloat(rate)) / 100;
		parentTd.find('input').val(parseFloat(tax));
	}else {
		parentTd.find('input').val("");
	} 
	
	netClaimRowAmount(elem);
}

function netClaimRowAmount(comp) {
	var parentTr = $(comp).closest('tr');
		var tax = 0;
		var gross = parentTr.find('.clmGross').val();
		var cgst = parentTr.find('.clmCgstAmount').val();
		var sgst = parentTr.find('.clmSgstAmount').val();
		var igst = parentTr.find('.clmIgstAmount').val();
		var cess = parentTr.find('.clmCessAmount').val();
		if(gross !== "" && $.isNumeric(gross)) {
			gross = Number(gross);
		}
		if(cgst !== "" && $.isNumeric(cgst)) {
			tax += Number(cgst);
		}
		if(sgst !== "" && $.isNumeric(sgst)) {
			tax += Number(sgst);
		}
		if(igst !== "" && $.isNumeric(igst)) {
			tax += Number(igst);
		}
		if(cess !== "" && $.isNumeric(cess)) {
			tax += Number(cess);
		}
		parentTr.find('.clmNetTotal').val(parseFloat(gross + tax));
}

function grossChange(elem,grossAmount){
	var parentTr = $(elem).closest('tr');
	if(grossAmount == "") {
		parentTr.find('.clmCgstRate').val("");
		parentTr.find('.clmSgstRate').val("");
		parentTr.find('.clmIgstRate').val("");
		parentTr.find('.clmCessRate').val("");
		
		parentTr.find('.clmCgstAmount').val("");
		parentTr.find('.clmSgstAmount').val("");
		parentTr.find('.clmIgstAmount').val("");
		parentTr.find('.clmCessAmount').val("");
		
	}else {
		var cgstselect = parentTr.find("td .clmCgstRate option:selected").val();
		if(cgstselect !== "") {
			var cgstRate = parentTr.find("td .clmCgstRate option:selected").attr('rate');
			parentTr.find('.clmCgstAmount').val(((parseFloat(grossAmount) * parseFloat(cgstRate)) / 100));
		}
		
		var sgstSelect = parentTr.find("td .clmSgstRate option:selected").val();
		if(sgstSelect !== "") {
			var sgstRate = parentTr.find("td .clmSgstRate option:selected").attr('rate');
			parentTr.find('.clmSgstAmount').val(((parseFloat(grossAmount) * parseFloat(sgstRate)) / 100));
		}
		
		var igstSelect = parentTr.find("td .clmIgstRate option:selected").val();
		if(igstSelect !== "") {
			var igstRate = parentTr.find("td .clmIgstRate option:selected").attr('rate');
			parentTr.find('.clmIgstAmount').val(((parseFloat(grossAmount) * parseFloat(igstRate)) / 100));
		}
		
		var igstSelect = parentTr.find("td .clmCessRate option:selected").val();
		if(igstSelect !== "") {
			var cessRate = parentTr.find("td .clmCessRate option:selected").attr('rate');
			parentTr.find('.clmCessAmount').val(((parseFloat(grossAmount) * parseFloat(cessRate)) / 100));
		}
	}
}


$(function(){
	$("#employeeClaimsButton").on('click',function(){
		// Show Pending Claims
		$('#employeeClaimsDiv').fadeIn('slow');
		showClaimPendingPaidClaims();
//		$('#claimsTable').hide();
	});
	
	$('.cancelemployeeClaim').on('click', function() {
		$('#employeeClaimsDiv:visible').slideUp();
		$('#claimsTable').fadeIn('slow');
	});
	
	$('.claimsetuptabCls').on('click',function(){
		var tabClass = $(this).parent().attr("class");
		if(tabClass == "active"){
			return false;
		}
		var tab = $(this).parent().attr("id");
		$('.claimSetupcls').hide();
		if('pendingClaimTab'==tab){
			$("#paidClaimTab").removeAttr("class", "active");
			$("#pendingClaimTab").attr("class", "active");
			showClaimPendingPaidClaims();
			$('#pendingClaimDiv').fadeIn('normal',function(){
			});
		}else if('paidClaimTab'==tab){
			$("#pendingClaimTab").removeAttr("class", "active");
			$("#paidClaimTab").attr("class", "active");
			showClaimPaidClaims();
			$('#paidClaimDiv').fadeIn('normal',function(){
			});
		}
	});
});

function showClaimPendingPaidClaims() {
	var txnJsonData={};
	txnJsonData.email=$("#hiddenuseremail").text();
	var url="/claims/getPendingEmployeeClaims";
	$("#employeeClaimPendingTable > tbody").html("");
	$.ajax({
		url: url,
		data:JSON.stringify(txnJsonData),
		type:"text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method:"POST",
		contentType:'application/json',
		success: function (data) {
			if(data.result == true) {
			
				for(var j=0; j < data.pendingClaims.length;j++) {
					var pendingClaimsRow = [];
					var i=0;
					pendingClaimsRow[i++] = '<tr><td><input type="hidden" class="claimPendingId" value="'+data.pendingClaims[j].id+'">';
					pendingClaimsRow[i++] = data.pendingClaims[j].date;
					pendingClaimsRow[i++] = '</td><td>';
					pendingClaimsRow[i++] = data.pendingClaims[j].userName;
					pendingClaimsRow[i++] = '</td><td>';
					pendingClaimsRow[i++] = data.pendingClaims[j].branch;
					pendingClaimsRow[i++] = '</td><td>';
					pendingClaimsRow[i++] = data.pendingClaims[j].purpose;
					pendingClaimsRow[i++] = '</td><td>';
					pendingClaimsRow[i++] = data.pendingClaims[j].approvedAmount;
					pendingClaimsRow[i++] = '</td><td>';
					pendingClaimsRow[i++] = data.pendingClaims[j].status
					pendingClaimsRow[i++] = '</td><td><button onclick="addSubmitForAccountingPopup('+data.pendingClaims[j].id+','+data.pendingClaims[j].approvedAmount+',\''+data.pendingClaims[j].date+'\');" style="cursor: pointer;" ><i class="fa fa-plus-circle fa-lg"></i> Pay Now</button></td></tr>';
					$("#employeeClaimPendingTable > tbody").append(pendingClaimsRow.join(''));
				}
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
};
function showClaimPaidClaims() {
	var txnJsonData={};
	txnJsonData.email=$("#hiddenuseremail").text();
	var url="/claims/getPaidEmployeeClaims";
	$("#employeeClaimPaidTable > tbody").html("");
	$.ajax({
		url: url,
		data:JSON.stringify(txnJsonData),
		type:"text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method:"POST",
		contentType:'application/json',
		success: function (data) {
			if(data.result == true) {
			
				for(var j=0; j < data.paidClaims.length;j++) {
					var paidClaimsRow = [];
					var i=0;
					paidClaimsRow[i++] = '<tr><td><input type="hidden" class="claimPaidId" value="'+data.paidClaims[j].id+'">';
					paidClaimsRow[i++] = data.paidClaims[j].date;
					paidClaimsRow[i++] = '</td><td>';
					paidClaimsRow[i++] = data.paidClaims[j].userName;
					paidClaimsRow[i++] = '</td><td>';
					paidClaimsRow[i++] = data.paidClaims[j].branch;
					paidClaimsRow[i++] = '</td><td>';
					paidClaimsRow[i++] = data.paidClaims[j].purpose;
					paidClaimsRow[i++] = '</td><td>';
					paidClaimsRow[i++] = data.paidClaims[j].approvedAmount;
					paidClaimsRow[i++] = '</td><td>';
					paidClaimsRow[i++] = data.paidClaims[j].status;
					paidClaimsRow[i++] = '</td><td>';
					paidClaimsRow[i++] = data.paidClaims[j].payMode;
					paidClaimsRow[i++] = '</td><td>';
					paidClaimsRow[i++] = data.paidClaims[j].payDate;
					paidClaimsRow[i++] = '</td></tr>';
					$("#employeeClaimPaidTable > tbody").append(paidClaimsRow.join(''));
				}
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
};

function clearPendingClaimsModal() {
	$("#pendingClaimModal").find("#paymentDatePendingClaim").val("");
	$("#pendingClaimModal").find("#pendingClaimId").val("");
	$(".dynmBnchBankActList").remove();
	$("#pendingClaimModal select[id='claimpaymentDetails']").find('option:first').prop("selected","selected");
}

function addSubmitForAccountingPopup(pendingClaimId,pendingAmt,claimDate) {
	clearPendingClaimsModal();
	$("#pendingClaimModal").find("#pendingClaimId").val(pendingClaimId);
	$("#pendingClaimModal").find("#pendingDueAmount").html(pendingAmt);
	$("#pendingClaimModal").find("#pendingDueAmount").html(pendingAmt);
	$("#pendingClaimModal").find("#paymentDatePendingClaim").datepicker({
		changeMonth : true,
		changeYear : true,
		dateFormat:  'M d,yy',
		yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
		maxDate: 'today',
		onSelect: function(x,y){
	        $(this).focus();
	    }
	});
	$("#pendingClaimModal").find("#paymentDatePendingClaim").datepicker("option", "minDate", claimDate);
	$("#pendingClaimModal").find("#paymentDatePendingClaim").datepicker().datepicker("setDate", new Date());
	$("#pendingClaimModal").modal('show');
}

function listAllBranchBankAccountsPendingClaims(elem){
	$(".dynmBnchBankActList").remove();
	var parentTr = "pendingClaimParent";
	var modeOption=$("#"+parentTr+" select[id='claimpaymentDetails'] option:selected").val();
	if(modeOption=="2"){
		var transactionEntityId=$("#"+parentTr).find("#pendingClaimId").val();
		if(!transactionEntityId==""){
			var jsonData={};
			var useremail=$("#hiddenuseremail").text();
			jsonData.email = useremail;
			jsonData.txnEntityId=transactionEntityId;
			ajaxCall('/claimsbranch/bankAccountsForPayment', jsonData, '', '', '', '', 'claimbankAccountsForPaymentSuccessPendingClaims', '', false);
		}
	}
}

function claimbankAccountsForPaymentSuccessPendingClaims(data){
	var parentTr= "pendingClaimParent";
	if(data.result){
		$(".dynmBnchBankActList").remove();
		$("#"+parentTr+" select[id='claimpaymentDetails']").after('<div class="dynmBnchBankActList"><select name="availableBank" id="availableBank"><option value="">Select Bank</option></select></div>');
		if(data.availableBranchBankData.length>0){
			for(var i=0;i<data.availableBranchBankData.length;i++){
				$("#"+parentTr+" select[id='availableBank']").append('<option value="'+data.availableBranchBankData[i].bnchBankAccountsId+'">'+data.availableBranchBankData[i].bnchBankAccountsName+'</option>')
			}
			addBankInstrumentDetail(parentTr);
		}else{
			$(".dynmBnchBankActList").remove();
			swal("Error!","Bank account is not configured for the branch for which you want to process the transaction.","error");
			$("#"+parentTr+" select[id='claimpaymentDetails']").find('option:first').prop("selected","selected");
		}
	}else{
		$(".dynmBnchBankActList").remove();
		swal("Error!","Bank account is not configured for the branch for which you want to process the transaction.","error");
		$("#"+parentTr+" select[id='claimpaymentDetails']").find('option:first').prop("selected","selected");
	}
}


function submitForAccountinPendingClaims(comp) {
	var selectedAction="4";
	var parentTr = "pendingClaimParent";
	var transactionEntityId = $("#"+parentTr).find("#pendingClaimId").val();
//	var supportingDoc=$("#"+parentTr+" input[name='actionFileUpload']").val();
	var remarks=$("#"+parentTr+" textarea[id='txnRemarks']").val();
	var paymentOption=$("#"+parentTr+" select[id='claimpaymentDetails'] option:selected").val();
	var bankDetails=$("#"+parentTr+" textarea[id='bankDetails']").val();
	var selectedAddApproverVal="";var paymentBank="";
	var txnJsonData={};
	txnJsonData.email=$("#hiddenuseremail").text();
	txnJsonData.selectedApproverAction=selectedAction;
	txnJsonData.transactionPrimId=transactionEntityId;
	txnJsonData.txnRmarks=remarks;
	txnJsonData.paymentDetails=paymentOption;
	if(paymentOption=="2"){
		paymentBank=$("#"+parentTr).find("#availableBank option:selected").val();
		if(typeof paymentBank!='undefined'){
			if(paymentBank == ""){
				// alert("Select a bank from list");
				swal("Empty data error!", "Select a bank from list", "error");
				return false;
			}
			var instrumentNo=$("#"+parentTr).find("#txtInstrumentNumber").val();
			if(instrumentNo == ""){
				// alert("Instrument No cannot be empty.");
				swal("Empty data error!", "Instrument No cannot be empty.", "error");
				return false;
			}
			var instrumentDate=$("#"+parentTr).find("#txtInstrumentDate").val();
			if(instrumentDate == ""){
				// alert("Instrument Date cannot be empty.");
				swal("Empty data error!", "Instrument Date cannot be empty.", "error");
				return false;
			}
			txnJsonData.txnPaymentBank=paymentBank;
			txnJsonData.txnInstrumentNum=instrumentNo;
			txnJsonData.txnInstrumentDate=instrumentDate;
	    }
	}
	txnJsonData.bankInf=bankDetails;
	var url="/claims/empPendingClaimSettlement";
	$.ajax({
		url: url,
		data:JSON.stringify(txnJsonData),
		type:"text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method:"POST",
		contentType:'application/json',
		success: function (data) {
			if(data.resultantAmount < 0){
				if(data.branchBankDetailEntered === false){
					swal("Insufficient balance in the bank account!", "Use alternative payment mode or infuse funds into the bank account. Current bank balance is:"+data.resultantAmount, "error");
					disableTransactionButtons();
					return false;
				}else{					
					swal("Bank Balance is in -ve!","Bank Balance is in -ve, But Due To Account Type You Are ALLOWED to Withdraw Amount Greeater Than The Available Amount In The Bank.","warning");
				}
			}	
			if(data.resultantCash < 0){
				swal("Insufficient balance in the bank account!","Insufficient balance in the cash account. Use alternative payment mode or infuse funds into the cash account. Effective cash balance is: " + data.resultantCash,"warning");
			}
			if(data.resultantPettyCashAmount < 0){
				swal("Insufficient balance in the bank account!","Insufficient balance in the petty cash account. Use alternative payment mode or infuse funds into petty cash account. Effective petty cash balance is: " + data.resultantPettyCashAmount,"warning");
			}
			clearPendingClaimsModal();
			$("#pendingClaimModal").modal('hide');
			showClaimPendingPaidClaims();
			
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}
// ************** Fill Module Claim Details **************

function validateClaimItem(elem) {
	var item = $(elem).val();
	if(item == "") {
		return true;
	}
	
	var table = $(elem).closest('tr').closest('table');
	var status = 0;
	table.find('tbody > tr').each(function(){
		var itemId = $(this).find('.clmItem').val();
		if(itemId == item) {
			status++;
		}
	});
	if(status > 1) {
		$(elem).val("");
		swal("Error!","Already selected ","error");
	}
}





