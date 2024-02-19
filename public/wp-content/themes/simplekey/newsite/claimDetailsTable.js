function getClaimsTransactionsSuccess(data){
	alert("getClaimsTransactionsSuccess")
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
			    		$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
			    		'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
			    		'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
			    		'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
			    		'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimexistingAdvance+'</p><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><p style="color: blue;">'+data.userClaimTxnData[i].claimadjustedAdvance+'</p><br/><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalAdvance+'</p>'+
			    		'</div></div></td><td><div><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p><br/><input type="button" value="Clarify Transaction" id="clarifyTxn" class="btn btn-submit btn-idos" onclick="claimclarifyTransaction(this)" style="margin-top:10px;margin-left: 0px;width:170px;"></div></td><td><div><select name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select><b>Supporting Doc:</b><br/><input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
				        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td><td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks">Sunil</textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
			    	}else{
			    		$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
			    		'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
			    		'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
			    		'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
			    		'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimexistingAdvance+'</p><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><p style="color: blue;">'+data.userClaimTxnData[i].claimadjustedAdvance+'</p><br/><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalAdvance+'</p>'+
			    		'</div></div></td><td><div><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select class="auditorAccountantSelect" name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
					    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
				        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
			    		'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
			    	}
			    }
			    //for approver can be same user or not
			    if(data.userClaimTxnData[i].approverEmails!=null){
			    	if(data.userClaimTxnData[i].approverEmails.indexOf(useremail)!=-1){
			    		$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
						if(data.userClaimTxnData[i].claimTxnStatus=='Require Approval' || data.userClaimTxnData[i].claimTxnStatus=='Clarified'){
							$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
						    '<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
						    '<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
						    '</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						    '<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimexistingAdvance+'</p><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><p style="color: blue;">'+data.userClaimTxnData[i].claimadjustedAdvance+'</p><br/><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalAdvance+'</p>'+
						    '</div></div></td><td><div><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><br/>Action:<select class="claimapproverActionList" name="claimapproverActionList" id="claimapproverActionList"><option value="">--Please Select--</option>'+
							'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>User List:<br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
							'<input type="button" id="claimapproverAction" class="btn btn-submit btn-idos" value="Submit" onclick="claimcompleteAction(this)"><br/><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select><b>Supporting Doc:</b><br/><input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
					        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
						    '<td><div><div class="claimtxnWorkflowRemarks" style="height: 195px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>');
						}else{
							$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
						    '<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
						    '<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
						    '</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						    '<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimexistingAdvance+'</p><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><p style="color: blue;">'+data.claimadjustedAdvance+'</p><br/><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalAdvance+'</p>'+
						    '</div></div></td><td><div><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select class="auditorAccountantSelect" name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
						    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
					        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
						    '<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
						}
			    	}
			    }
			    if(data.userClaimTxnData[i].selectedAdditionalApproval==useremail){
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
					if(data.userClaimTxnData[i].claimTxnStatus=='Require Additional Approval'){
						$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
						'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
						'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
						'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimexistingAdvance+'</p><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><p style="color: blue;">'+data.userClaimTxnData[i].claimadjustedAdvance+'</p><br/><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalAdvance+'</p>'+
						'</div></div></td><td><div><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><br/>Action:<select class="claimapproverActionList" name="claimapproverActionList" id="claimapproverActionList"><option value="">--Please Select--</option>'+
						'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>User List:<br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
						'<input type="button" id="claimapproverAction" class="btn btn-submit btn-idos" value="Submit" onclick="claimcompleteAction(this)"><br/><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select><b>Supporting Doc:</b><br/><input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
				        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
						'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
					}else{
						$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
						'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
						'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
						'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><p style="color: blue;">'+data.claimexistingAdvance+'</p><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><p style="color: blue;">'+data.userClaimTxnData[i].claimadjustedAdvance+'</p><br/><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalAdvance+'</p>'+
						'</div></div></td><td><div><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select class="auditorAccountantSelect" name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
					    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
				        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
						'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
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
		    			 $("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
		 					'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
		 					'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
		 					'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
		 					'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimexistingAdvance+'</p><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><p style="color: blue;">'+data.userClaimTxnData[i].claimadjustedAdvance+'</p><br/><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalAdvance+'</p>'+
		 					'</div></div></td><td><div><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
						    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
					        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
		 					'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
		    		 }
			    }
			    //for accountant can be same user or not
			    if(data.userClaimTxnData[i].claimTxnStatus=='Approved'){
				    if(data.userClaimTxnData[i].accountantEmails!=null){
				    	if(data.userClaimTxnData[i].accountantEmails.indexOf(useremail)!=-1){
				    		$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
				    		if(data.userClaimTxnData[i].claimTxnStatus=="Approved"){
				    			$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
								'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
								'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
								'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
								'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimexistingAdvance+'</p><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><p style="color: blue;">'+data.userClaimTxnData[i].claimadjustedAdvance+'</p><br/><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalAdvance+'</p>'+
								'</div></div></td><td><div><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><div class="claimpayementDiv"><br/>Select mode of payment:<select name="claimpaymentDetails" id="claimpaymentDetails" onchange="listAllBranchBankAccountsClaims(this);">'+
					    		'<option value="1">CASH</option><option value="2">BANK</option></select><br/>Input payment details:<textarea name="bankDetails" id="bankDetails"></textarea></div>'+
					    		'<br/><input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit btn-idos" onclick="claimcompleteAccounting(this)" style="margin-top:10px;margin-left: 0px;"><br/><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select><b>Supporting Doc:</b><br/><input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
						        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
								'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
				    		}else{
				    			$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
								'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
								'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
								'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
								'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimexistingAdvance+'</p><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><p style="color: blue;">'+data.userClaimTxnData[i].claimadjustedAdvance+'</p><br/><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalAdvance+'</p>'+
								'</div></div></td><td><div><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select class="auditorAccountantSelect" name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
							    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
						        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
								'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
				    		}
				    	}
				    }
			    }
			    $('.claimCommonTable:visible').slideUp();
			    //logic for separation transaction travel eligibility separately into travel eligibility label and travel eligibility figure
			    if(data.userClaimTxnData[i].claimtravelDetailedConfDescription!=null && data.userClaimTxnData[i].claimtravelDetailedConfDescription!=""){
				   	var individualclaimtravelDetailedConfDescription=data.userClaimTxnData[i].claimtravelDetailedConfDescription.substring(0,data.userClaimTxnData[i].claimtravelDetailedConfDescription.length).split('#');
				   	for(var m=0;m<individualclaimtravelDetailedConfDescription.length;m++){
				    	if(m>0){
				    		var labelAndValue=individualclaimtravelDetailedConfDescription[m].substring(0, individualclaimtravelDetailedConfDescription[m].length).split(':');
				    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></p>:<br/>');
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
				    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></p>:<br/>');
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
				    	$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtxnWorkflowRemarks"]').append('<font color="#FFOFF"><b>'+emailAndRemarks[0]+'</b></p>#');
				    	if(typeof emailAndRemarks[1]!='undefined'){
				    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtxnWorkflowRemarks"]').append('<p style="color: blue;">'+emailAndRemarks[1]+'</p><br/>');
				    	}
				    }
				}
			  //logic for separation transaction remarks separately into useremail and documents uploaded by them
			    if(data.userClaimTxnData[i].claimsupportingDoc!="" && data.userClaimTxnData[i].claimsupportingDoc!=null){
			    	var txndocument=data.userClaimTxnData[i].claimsupportingDoc;
			    	$("tr[id='claimsTransactionEntity"+data.userClaimTxnData[i].id+"'] select[id='claimfileDownload']").children().remove();
			    	$("tr[id='claimsTransactionEntity"+data.userClaimTxnData[i].id+"'] select[id='claimfileDownload']").append('<option value="">--Please Select--</option>');
			    	var fileURLWithUser=txndocument.substring(0,txndocument.length).split(',');
			    	for(var k=0;k<fileURLWithUser.length;k++){
			    		var fileURLWithoutUser=fileURLWithUser[k].substring(0, fileURLWithUser[k].length).split('#');
			    		var fileName="";
			    		var rowTxnId=data.userClaimTxnData[i].id;
			    		var inkblob = { url: fileURLWithoutUser[1]};
			    		//filepicker.setKey('A6zVM3VmDQhqeTq6sF209z');
			    		filepicker.setKey('A7ucPpqRuR46F7OVE8CHJz');
			    		filepicker.stat(inkblob, {filename: true},function(metadata){
			    		    fileName=JSON.stringify(metadata.filename);
			    		    var newFileName=fileURLWithoutUser[0]+"#"+fileName;
			    		    var id=rowTxnId;
				    		$("tr[id='claimsTransactionEntity"+id+"'] select[id='claimfileDownload']").append('<option value='+fileURLWithoutUser[1]+'>'+newFileName+'</option>');
			    		});
			    	}
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
			    	$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
			    	'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
			    	'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
			    	'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
			    	'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div></div>'+
			    	'</div></td><td><div><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select class="auditorAccountantSelect" name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
				    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
			        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
			    	'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
			    }
			    //for other accountant and auditor of the branch
			    if(role.indexOf("ACCOUNTANT")!=-1 || role.indexOf("AUDITOR")!=-1 || role.indexOf("CONTROLLER")!=-1){
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
			    	$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
					'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
					'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
					'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
					'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div></div>'+
					'</div></td><td><div><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
				    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
			        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
					'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
			    }
			    //for accountant can be same user or not
				if(data.userClaimTxnData[i].accountantEmails!=null){
				    if(data.userClaimTxnData[i].accountantEmails.indexOf(useremail)!=-1){
				    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
				    	if(data.userClaimTxnData[i].claimTxnStatus=="Payment Due To Staff" || data.userClaimTxnData[i].claimTxnStatus=="Payment Due From Staff" || data.userClaimTxnData[i].claimTxnStatus=="No Due For Settlement"){
				    		$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
							'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
							'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
							'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
							'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div></div>'+
							'</div></td><td><div><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><div class="claimpayementDiv"><br/>Select mode of payment:<select name="claimpaymentDetails" id="claimpaymentDetails" onchange="listAllBranchBankAccountsClaims(this);">'+
					    	'<option value="1">CASH</option><option value="2">BANK</option></select><br/>Input payment details:<textarea name="bankDetails" id="bankDetails"></textarea></div>'+
					    	'<br/><input type="button" value="'+data.userClaimTxnData[i].claimTxnStatus+'" id="settleTravelClaimTxn" class="btn btn-submit btn-idos" onclick="claimSettlementAccounting(this)" style="margin-top:10px;width:170px;margin-left: 0px;"><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select><b>Supporting Doc:</b><br/><input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
						    '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
							'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
				    	}else{
				    		$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
							'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtravelType+'</p>'+
							'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimplacesSelectedOrEntered+'</p>'+
							'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
							'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div></div>'+
							'</div></td><td><div><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimpurposeOfVisit+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select class="auditorAccountantSelect" name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
						    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
					        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
							'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveclaimtxnRemarks(this)"></div></div></td></tr>');
				    	}
				    }
				}
			    //logic for separation transaction unsettled Travel claims separately into unsettled label and figure
			    if(data.userClaimTxnData[i].existingClaimsCurrentSettlementDetails!=null && data.userClaimTxnData[i].existingClaimsCurrentSettlementDetails!=""){
				   	var individualexistingClaimsCurrentSettlementDetails=data.userClaimTxnData[i].existingClaimsCurrentSettlementDetails.substring(0,data.userClaimTxnData[i].existingClaimsCurrentSettlementDetails.length).split('#');
				   	for(var m=0;m<individualexistingClaimsCurrentSettlementDetails.length;m++){
				    	if(m>0){
				    		var labelAndValue=individualexistingClaimsCurrentSettlementDetails[m].substring(0, individualexistingClaimsCurrentSettlementDetails[m].length).split(':');
				    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></p>:<br/>');
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
				    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></p>:<br/>');
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
				    	$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtxnWorkflowRemarks"]').append('<font color="#FFOFF"><b>'+emailAndRemarks[0]+'</b></p>#');
				    	if(typeof emailAndRemarks[1]!='undefined'){
				    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtxnWorkflowRemarks"]').append('<p style="color: blue;">'+emailAndRemarks[1]+'</p><br/>');
				    	}
				    }
				}
			    //logic for separation transaction remarks separately into useremail and documents uploaded by them
			    if(data.userClaimTxnData[i].claimsupportingDoc!="" && data.userClaimTxnData[i].claimsupportingDoc!=null){
			    	var txndocument=data.userClaimTxnData[i].claimsupportingDoc;
			    	$("tr[id='claimsTransactionEntity"+data.userClaimTxnData[i].id+"'] select[id='claimfileDownload']").children().remove();
			    	$("tr[id='claimsTransactionEntity"+data.userClaimTxnData[i].id+"'] select[id='claimfileDownload']").append('<option value="">--Please Select--</option>');
			    	var fileURLWithUser=txndocument.substring(0,txndocument.length).split(',');
			    	for(var k=0;k<fileURLWithUser.length;k++){
			    		var fileURLWithoutUser=fileURLWithUser[k].substring(0, fileURLWithUser[k].length).split('#');
			    		var fileName="";
			    		var rowTxnId=data.userClaimTxnData[i].id;
			    		var inkblob = { url: fileURLWithoutUser[1]};
			    		//filepicker.setKey('A6zVM3VmDQhqeTq6sF209z');
			    		filepicker.setKey('A7ucPpqRuR46F7OVE8CHJz');
			    		filepicker.stat(inkblob, {filename: true},function(metadata){
			    		    fileName=JSON.stringify(metadata.filename);
			    		    var newFileName=fileURLWithoutUser[0]+"#"+fileName;
			    		    var id=rowTxnId;
				    		$("tr[id='claimsTransactionEntity"+id+"'] select[id='claimfileDownload']").append('<option value='+fileURLWithoutUser[1]+'>'+newFileName+'</option>');
			    		});
			    	}
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
				    		$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
						    '<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
						    '<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
						    '</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						    '<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Entered Advance:</b><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvanceTotalAdvanceAmount+'</p>'+
						    '</div></div></td><td><div><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p><br/><input type="button" value="Clarify Transaction" id="clarifyTxn" class="btn btn-submit btn-idos" onclick="advanceExpenseClarifyTransaction(this)" style="margin-top:10px;margin-left: 0px;width:170px;"></div></td><td><div><select name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select><b>Supporting Doc:</b><br/><input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
							'<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td><td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
				    	}else{
				    		$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
						    '<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
						    '<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
						    '</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						    '<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvanceTotalAdvanceAmount+'</p>'+
						    '</div></div></td><td><div><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select class="auditorAccountantSelect" name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
						    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
					        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
						    '<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
				    	}
				    }
				    //for approver can be same user or not
				    if(data.userClaimTxnData[i].approverEmails!=null){
				    	if(data.userClaimTxnData[i].approverEmails.indexOf(useremail)!=-1){
				    		$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
							if(data.userClaimTxnData[i].claimTxnStatus=='Require Approval' || data.userClaimTxnData[i].claimTxnStatus=='Clarified'){
								$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
								'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
								'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
								'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
								'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvanceTotalAdvanceAmount+'</p>'+
								'</div></div></td><td><div><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><br/>Action:<select class="claimapproverActionList" name="claimapproverActionList" id="claimapproverActionList"><option value="">--Please Select--</option>'+
								'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>User List:<br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
								'<input type="button" id="claimapproverAction" class="btn btn-submit btn-idos" value="Submit" onclick="advanceExpenseCompleteAction(this)"><br/><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select><b>Supporting Doc:</b><br/><input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
								'<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
								'<td><div><div class="claimtxnWorkflowRemarks" style="height: 195px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>');
							}else{
								$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
								'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
								'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
								'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
								'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvanceTotalAdvanceAmount+'</p>'+
								'</div></div></td><td><div><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select class="auditorAccountantSelect" name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
							    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
						        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
								'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
							}
				    	}
				    }
				    if(data.userClaimTxnData[i].selectedAdditionalApproval==useremail){
				    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
						if(data.userClaimTxnData[i].claimTxnStatus=='Require Additional Approval'){
							$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
							'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
							'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
							'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
							'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><p style="color: blue;">'+data.claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.expenseAdvanceTotalAdvanceAmount+'</p>'+
							'</div></div></td><td><div><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><br/>Action:<select class="claimapproverActionList" name="claimapproverActionList" id="claimapproverActionList"><option value="">--Please Select--</option>'+
							'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>User List:<br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
							'<input type="button" id="claimapproverAction" class="btn btn-submit btn-idos" value="Submit" onclick="advanceExpenseCompleteAction(this)"><br/><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select><b>Supporting Doc:</b><br/><input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
							'<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
							'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
						}else{
							$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
							'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
							'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
							'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
							'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvanceTotalAdvanceAmount+'</p>'+
							'</div></div></td><td><div><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select class="auditorAccountantSelect" name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
						    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
					        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
							'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
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
			    			 $("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
							 '<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
							 '<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
							 '</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
							 '<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvanceTotalAdvanceAmount+'</p>'+
							 '</div></div></td><td><div><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
							 '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
						     '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
							 '<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
			    		 }
				    }
				    //for accountant can be same user or not
				    if(data.userClaimTxnData[i].claimTxnStatus=='Approved'){
					    if(data.userClaimTxnData[i].accountantEmails!=null){
					    	if(data.userClaimTxnData[i].accountantEmails.indexOf(useremail)!=-1){
					    		$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
					    		if(data.userClaimTxnData[i].claimTxnStatus=="Approved"){
					    			$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
									'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
									'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
									'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
									'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvanceTotalAdvanceAmount+'</p>'+
									'</div></div></td><td><div><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><div class="claimpayementDiv"><br/>Select mode of payment:<select name="claimpaymentDetails" id="claimpaymentDetails" onchange="listAllBranchBankAccountsClaims(this);">'+
									'<option value="1">CASH</option><option value="2">BANK</option></select><br/>Input payment details:<textarea name="bankDetails" id="bankDetails"></textarea></div>'+
									'<br/><input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit btn-idos" onclick="advanceExpenseCompleteAccounting(this)" style="margin-top:10px;margin-left: 0px;"><br/><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select><b>Supporting Doc:</b><br/><input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
									'<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
									'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
					    		}else{
					    			$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
									'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
									'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
									'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
									'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvanceTotalAdvanceAmount+'</p>'+
									'</div></div></td><td><div><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select class="auditorAccountantSelect" name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
								    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
							        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
									'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
					    		}
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
					    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></p>:<br/>');
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
					    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></p>:<br/>');
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
					    	$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtxnWorkflowRemarks"]').append('<font color="#FFOFF"><b>'+emailAndRemarks[0]+'</b></p>#');
					    	if(typeof emailAndRemarks[1]!='undefined'){
					    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtxnWorkflowRemarks"]').append('<p style="color: blue;">'+emailAndRemarks[1]+'</p><br/>');
					    	}
					    }
					}
				  //logic for separation transaction remarks separately into useremail and documents uploaded by them
				    if(data.userClaimTxnData[i].claimsupportingDoc!="" && data.userClaimTxnData[i].claimsupportingDoc!=null){
				    	var txndocument=data.userClaimTxnData[i].claimsupportingDoc;
				    	$("tr[id='claimsTransactionEntity"+data.userClaimTxnData[i].id+"'] select[id='claimfileDownload']").children().remove();
				    	$("tr[id='claimsTransactionEntity"+data.userClaimTxnData[i].id+"'] select[id='claimfileDownload']").append('<option value="">--Please Select--</option>');
				    	var fileURLWithUser=txndocument.substring(0,txndocument.length).split(',');
				    	for(var k=0;k<fileURLWithUser.length;k++){
				    		var fileURLWithoutUser=fileURLWithUser[k].substring(0, fileURLWithUser[k].length).split('#');
				    		var fileName="";
				    		var rowTxnId=data.userClaimTxnData[i].id;
				    		var inkblob = { url: fileURLWithoutUser[1]};
				    		//filepicker.setKey('A6zVM3VmDQhqeTq6sF209z');
				    		filepicker.setKey('A7ucPpqRuR46F7OVE8CHJz');
				    		filepicker.stat(inkblob, {filename: true},function(metadata){
				    		    fileName=JSON.stringify(metadata.filename);
				    		    var newFileName=fileURLWithoutUser[0]+"#"+fileName;
				    		    var id=rowTxnId;
					    		$("tr[id='claimsTransactionEntity"+id+"'] select[id='claimfileDownload']").append('<option value='+fileURLWithoutUser[1]+'>'+newFileName+'</option>');
				    		});
				    	}
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
			    	$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
				    '<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
				    '<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
				    '</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
				    '<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Advance Settled :</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Due From Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].dueFromCompany+'</p><br/><b>Due To Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].dueToCompany+'</p><br/><b>Amount Returned In Case Of Due To Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].amountReturnInCaseOfDueToCompany+'</p>'+
				    '</div></div></td><td><div><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select class="auditorAccountantSelect" name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
				    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
			        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
				    '<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
			    }
			  //for other accountant and auditor of the branch
			    if(role.indexOf("ACCOUNTANT")!=-1 || role.indexOf("AUDITOR")!=-1 || role.indexOf("CONTROLLER")!=-1){
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
			    	$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
					'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
					'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
					'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
					'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Advance Settled :</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Due From Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].dueFromCompany+'</p><br/><b>Due To Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].dueToCompany+'</p><br/><b>Amount Returned In Case Of Due To Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].amountReturnInCaseOfDueToCompany+'</p>'+
					'</div></div></td><td><div><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
				    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
			        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
					'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
			    }
			    //for accountant can be same user or not
				if(data.userClaimTxnData[i].accountantEmails!=null){
				    if(data.userClaimTxnData[i].accountantEmails.indexOf(useremail)!=-1){
				    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
				    	if(data.userClaimTxnData[i].claimTxnStatus=="Payment Due To Staff" || data.userClaimTxnData[i].claimTxnStatus=="Payment Due From Staff" || data.userClaimTxnData[i].claimTxnStatus=="No Due For Settlement"){
				    		$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
				    		'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
				    		'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
				    		'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
				    		'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Advance Settled :</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Due From Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].dueFromCompany+'</p><br/><b>Due To Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].dueToCompany+'</p><br/><b>Amount Returned In Case Of Due To Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].amountReturnInCaseOfDueToCompany+'</p>'+
				    		'</div></div></td><td><div><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><div class="claimpayementDiv"><br/>Select mode of payment:<select name="claimpaymentDetails" id="claimpaymentDetails" onchange="listAllBranchBankAccountsClaims(this);">'+
					    	'<option value="1">CASH</option><option value="2">BANK</option></select><br/>Input payment details:<textarea name="bankDetails" id="bankDetails"></textarea></div>'+
					    	'<br/><input type="button" value="'+data.userClaimTxnData[i].claimTxnStatus+'" id="settleExpenseAdvanceTxn" class="btn btn-submit btn-idos" onclick="expenseAdvanceSettlementAccounting(this)" style="margin-top:10px;width:170px;margin-left: 0px;"><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select class="auditorAccountantSelect" name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
						    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
					        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
				    		'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
						}else{
							$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
						    '<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
						    '<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
						    '</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						    '<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Advance Settled :</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p><br/><b>Due From Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].dueFromCompany+'</p><br/><b>Due To Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].dueToCompany+'</p><br/><b>Amount Returned In Case Of Due To Company:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].amountReturnInCaseOfDueToCompany+'</p>'+
						    '</div></div></td><td><div><b>Purpose Of Advance:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select class="auditorAccountantSelect" name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
						    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
					        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
						    '<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveAdvanceExpenseTxnRemarks(this)"></div></div></td></tr>');
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
					    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></p>:<br/>');
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
					    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></p>:<br/>');
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
					    	$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtxnWorkflowRemarks"]').append('<font color="#FFOFF"><b>'+emailAndRemarks[0]+'</b></p>#');
					    	if(typeof emailAndRemarks[1]!='undefined'){
					    		$('#claimsTransactionEntity'+data.userClaimTxnData[i].id+' div[class="claimtxnWorkflowRemarks"]').append('<p style="color: blue;">'+emailAndRemarks[1]+'</p><br/>');
					    	}
					    }
					}
				    //logic for separation transaction remarks separately into useremail and documents uploaded by them
				    if(data.userClaimTxnData[i].claimsupportingDoc!="" && data.userClaimTxnData[i].claimsupportingDoc!=null){
				    	var txndocument=data.userClaimTxnData[i].claimsupportingDoc;
				    	$("tr[id='claimsTransactionEntity"+data.userClaimTxnData[i].id+"'] select[id='claimfileDownload']").children().remove();
				    	$("tr[id='claimsTransactionEntity"+data.userClaimTxnData[i].id+"'] select[id='claimfileDownload']").append('<option value="">--Please Select--</option>');
				    	var fileURLWithUser=txndocument.substring(0,txndocument.length).split(',');
				    	for(var k=0;k<fileURLWithUser.length;k++){
				    		var fileURLWithoutUser=fileURLWithUser[k].substring(0, fileURLWithUser[k].length).split('#');
				    		var fileName="";
				    		var rowTxnId=data.userClaimTxnData[i].id;
				    		var inkblob = { url: fileURLWithoutUser[1]};
				    		//filepicker.setKey('A6zVM3VmDQhqeTq6sF209z');
				    		filepicker.setKey('A7ucPpqRuR46F7OVE8CHJz');
				    		filepicker.stat(inkblob, {filename: true},function(metadata){
				    		    fileName=JSON.stringify(metadata.filename);
				    		    var newFileName=fileURLWithoutUser[0]+"#"+fileName;
				    		    var id=rowTxnId;
					    		$("tr[id='claimsTransactionEntity"+id+"'] select[id='claimfileDownload']").append('<option value='+fileURLWithoutUser[1]+'>'+newFileName+'</option>');
				    		});
				    	}
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
			
			
			/*if(data.userClaimTxnData[i].txnPurposeId=='19'){
				var useremail=$("#hiddenuseremail").text();
				var role=data.userClaimTxnData[i].userroles;
			    $("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
			    //for creator claim detail data display logic 
			    if(data.userClaimTxnData[i].createdBy==useremail){
			    	//now based of claim transaction status display the claim txn row
			    	if(data.userClaimTxnData[i].claimTxnStatus=='Require Clarification'){
			    		//when approver send a request toclarify the claim transaction created by creator
			    		$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
			    		'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
			    		'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
			    		'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
			    		'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Reimbursement Amount:</b><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p>'+
			    		'</div></div></td><td><div><b>Purpose Of Reimbursement:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p><br/><input type="button" value="Clarify Transaction" id="clarifyTxn" class="btn btn-submit btn-idos" onclick="reimbursementClarifyTransaction(this)" style="margin-top:10px;margin-left: 0px;width:170px;"></div></td><td><div><select name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select><b>Supporting Doc:</b><br/><input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
				        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td><td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveReimbursementTxnRemarks(this)"></div></div></td></tr>');
			    	}else{
			    		$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
			    		'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
			    		'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
			    		'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
			    		'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Reimbursement Amount:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p>'+
			    		'</div></div></td><td><div><b>Purpose Of Reimbursement:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select class="auditorAccountantSelect" name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
					    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
				        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
			    		'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveReimbursementTxnRemarks(this)"></div></div></td></tr>');
			    	}
			    }
			    //for approver can be same user or not
			    if(data.userClaimTxnData[i].approverEmails!=null){
			    	if(data.userClaimTxnData[i].approverEmails.indexOf(useremail)!=-1){
			    		$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
						if(data.userClaimTxnData[i].claimTxnStatus=='Require Approval' || data.userClaimTxnData[i].claimTxnStatus=='Clarified'){
//							$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
//						    '<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
//						    '<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
//						    '</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
//						    '<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Reimbursement Amount:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p>'+
//						    '</div></div></td><td><div><b>Purpose Of Reimbursement:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><br/>Action:<select class="claimapproverActionList" name="claimapproverActionList" id="claimapproverActionList"><option value="">--Please Select--</option>'+
//							'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>User List:<br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
//							'<input type="button" id="claimapproverAction" class="btn btn-submit btn-idos" value="Submit" onclick="reimbursementCompleteAction(this)"><br/><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select><b>Supporting Doc:</b><br/><input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
//					        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
//						    '<td><div><div class="claimtxnWorkflowRemarks" style="height: 195px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea></div></div></td></tr>');
						}else{
							$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
						    '<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
						    '<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
						    '</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						    '<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Reimbursement Amount:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p>'+
						    '</div></div></td><td><div><b>Purpose Of Reimbursement:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select class="auditorAccountantSelect" name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
						    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
					        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
						    '<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveReimbursementTxnRemarks(this)"></div></div></td></tr>');
						}
			    	}
			    }
			    if(data.userClaimTxnData[i].selectedAdditionalApproval==useremail){
			    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
					if(data.userClaimTxnData[i].claimTxnStatus=='Require Additional Approval'){
						$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
						'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
						'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
						'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Reimbursement Amount:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p>'+
						'</div></div></td><td><div><b>Purpose Of Reimbursement:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><br/>Action:<select class="claimapproverActionList" name="claimapproverActionList" id="claimapproverActionList"><option value="">--Please Select--</option>'+
						'<option value="1">Approve</option><option value="2">Reject</option><option value="3">Additional Approval</option><option value="5">Clarification</option></select><br/>User List:<br/><select class="userForAdditionalApprovalClass" name="userAddApproval" id="userAddApproval"></select>'+
						'<input type="button" id="claimapproverAction" class="btn btn-submit btn-idos" value="Submit" onclick="advanceExpenseCompleteAction(this)"><br/><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select><b>Supporting Doc:</b><br/><input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
				        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
						'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveReimbursementTxnRemarks(this)"></div></div></td></tr>');
					}else{
						$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
						'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
						'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
						'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Reimbursement Amount:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p>'+
						'</div></div></td><td><div><b>Purpose Of Reimbursement:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select class="auditorAccountantSelect" name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
					    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
				        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
						'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveReimbursementTxnRemarks(this)"></div></div></td></tr>');
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
		    			 $("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
						 '<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
						 '<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
						 '</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
						 '<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Reimbursement Amount:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p>'+
						 '</div></div></td><td><div><b>Purpose Of Reimbursement:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
						 '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
					     '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
						 '<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveReimbursementTxnRemarks(this)"></div></div></td></tr>');
		    		 }
			    }
			  //for accountant can be same user or not
				if(data.userClaimTxnData[i].accountantEmails!=null){
				    if(data.userClaimTxnData[i].accountantEmails.indexOf(useremail)!=-1){
				    	$("#claimDetailsTable").find('tr[id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"]').remove();
				    	if(data.userClaimTxnData[i].claimTxnStatus=="Approved"){
				    		$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
							'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
							'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
							'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
							'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Reimbursement Amount:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p>'+
							'</div></div></td><td><div><b>Purpose Of Reimbursement:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><div class="claimpayementDiv"><br/>Select mode of payment:<select name="claimpaymentDetails" id="claimpaymentDetails" onchange="listAllBranchBankAccountsClaims(this);">'+
					    	'<option value="1">CASH</option><option value="2">BANK</option></select><br/>Input payment details:<textarea name="bankDetails" id="bankDetails"></textarea></div>'+
					    	'<br/><input type="button" value="Complete Accounting" id="completeTxn" class="btn btn-submit btn-idos" onclick="reimbursementCompleteAccounting(this)" style="margin-top:10px;margin-left: 0px;"><br/><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select><b>Supporting Doc:</b><br/><input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
						    '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
							'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveReimbursementTxnRemarks(this)"></div></div></td></tr>');
				    	}else{
				    		$("#claimDetailsTable").append('<tr id="claimsTransactionEntity'+data.userClaimTxnData[i].id+'"><td><div><b>BRANCH:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>'+data.userClaimTxnData[i].creatorLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].createdBy+'</p></div></td>'+
							'<td><div><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].txnQuestionName+'</p><br/><b>Item Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemName+'</p>'+
							'<br/><b>Item Particular Name:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].itemParticularName+'</p><br/><b>Parent Specifics:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].parentSpecificName+'</p>'+
							'</div></td><td><div><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><div class="claimtravelDetailedConfDescriptionDiv"></div></div></div></td>'+
							'<td><div><p style="color: blue;">'+data.userClaimTxnData[i].transactionDate+'</p></div></td><td><div><div class="advanceDetConf" style="height:220px;overflow:auto;"><div class="claimuserAdvanveEligibilityDiv"></div><b>Entered Reimbursement Amount:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].claimenteredAdvance+'</p>'+
							'</div></div></td><td><div><b>Purpose Of Reimbursement:</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].expenseAdvancepurposeOfExpenseAdvance+'</p></div></td><td><div><div class="txnstat">'+data.userClaimTxnData[i].claimTxnStatus+'</div><b>'+data.userClaimTxnData[i].approverLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].approvedBy+'</p><br/><b>'+data.userClaimTxnData[i].accountedLabel+'</b><br/><p style="color: blue;">'+data.userClaimTxnData[i].accountedBy+'</p></div></td><td><div><select class="auditorAccountantSelect" name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select>'+
						    '<br/>Supporting Doc:<input type="text" id="actionFileUpload" name="actionFileUpload" readonly="readonly">'+
					        '<input type="button" id="actionFileUpload" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div></td>'+
							'<td><div><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div><div><textarea  rows="1" name="txnRemarks" id="txnRemarks"></textarea><br/><input type="button"  value="Submit Remarks" id="givetxnRemarks" class="btn btn-submit btn-idos" onclick="giveReimbursementTxnRemarks(this)"></div></div></td></tr>');
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
				    		$('#claimsTransactionEntity'+data.id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></p>:<br/>');
				    		if(typeof labelAndValue[1]!='undefined'){
					    		$('#claimsTransactionEntity'+data.id+' div[class="claimtravelDetailedConfDescriptionDiv"]').append('<p style="color: blue;">'+labelAndValue[1]+'</p><br/>');
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
				    		$('#claimsTransactionEntity'+data.id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<font color="black"><b>'+labelAndValue[0]+'</b></p>:<br/>');
				    		if(typeof labelAndValue[1]!='undefined'){
					    		$('#claimsTransactionEntity'+data.id+' div[class="claimuserAdvanveEligibilityDiv"]').append('<p style="color: blue;">'+labelAndValue[1]+'</p><br/>');
					    	}
				    	}
				    }
				}
			    //logic for separation transaction remarks separately into useremail and remarks made by them
			    if(data.userClaimTxnData[i].claimtxnRemarks!=null && data.userClaimTxnData[i].claimtxnRemarks!=""){
				   	var individualRemarks=data.userClaimTxnData[i].claimtxnRemarks.substring(0,data.userClaimTxnData[i].claimtxnRemarks.length).split(',');
				   	for(var m=0;m<individualRemarks.length;m++){
				    	var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
				    	$('#claimsTransactionEntity'+data.id+' div[class="claimtxnWorkflowRemarks"]').append('<font color="#FFOFF"><b>'+emailAndRemarks[0]+'</b></p>#');
				    	if(typeof emailAndRemarks[1]!='undefined'){
				    		$('#claimsTransactionEntity'+data.id+' div[class="claimtxnWorkflowRemarks"]').append('<p style="color: blue;">'+emailAndRemarks[1]+'</p><br/>');
				    	}
				    }
				}
			    //logic for separation transaction remarks separately into useremail and documents uploaded by them
			    if(data.userClaimTxnData[i].claimsupportingDoc!="" && data.userClaimTxnData[i].claimsupportingDoc!=null){
			    	var txndocument=data.userClaimTxnData[i].claimsupportingDoc;
			    	$("tr[id='claimsTransactionEntity"+data.userClaimTxnData[i].id+"'] select[id='claimfileDownload']").children().remove();
			    	$("tr[id='claimsTransactionEntity"+data.userClaimTxnData[i].id+"'] select[id='claimfileDownload']").append('<option value="">--Please Select--</option>');
			    	var fileURLWithUser=txndocument.substring(0,txndocument.length).split(',');
			    	for(var k=0;k<fileURLWithUser.length;k++){
			    		var fileURLWithoutUser=fileURLWithUser[k].substring(0, fileURLWithUser[k].length).split('#');
			    		var fileName="";
			    		var rowTxnId=data.userClaimTxnData[i].id;
			    		var inkblob = { url: fileURLWithoutUser[1]};
			    		//filepicker.setKey('A6zVM3VmDQhqeTq6sF209z');
			    		filepicker.setKey('A7ucPpqRuR46F7OVE8CHJz');
			    		filepicker.stat(inkblob, {filename: true},function(metadata){
			    		    fileName=JSON.stringify(metadata.filename);
			    		    var newFileName=fileURLWithoutUser[0]+"#"+fileName;
			    		    var id=rowTxnId;
				    		$("tr[id='claimsTransactionEntity"+id+"'] select[id='claimfileDownload']").append('<option value='+fileURLWithoutUser[1]+'>'+newFileName+'</option>');
			    		});
			    	}
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
				    	for(var i=0;i<additionalApprovarUsersList.length;i++){
				    		$("tr[id='claimsTransactionEntity"+data.userClaimTxnData[i].id+"'] select[id='userAddApproval']").append('<option value="'+additionalApprovarUsersList[i]+'">'+additionalApprovarUsersList[i]+'</option>');
				    	}
				    }
				}
			    if(role.indexOf("ACCOUNTANT")!=-1 || role.indexOf("APPROVER")!=-1 || role.indexOf("ACCOUNTANT")!=-1 || role.indexOf("AUDITOR")!=-1){
			    	var locHash=window.location.hash;
			    	if(locHash=="" || locHash=="#claimSetup"){
					    location.hash="#claimSetup";
					    showdivandactiveleftmenu("#claimSetup");
			    	}
			    }
			    //getCashBankReceivablePayable();
			    
			}*/
		}
		userClaimTransactionListString=$("#claimDetailsTable tbody").html();
		
	}
	
	setPagingDetail('claimDetailsTable', 10, 'pagingCalimNavPosition');
}
