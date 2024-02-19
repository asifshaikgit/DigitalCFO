//tansaction constants
const SELL_ON_CASH_COLLECT_PAYMENT_NOW = 1;
const SELL_ON_CREDIT_COLLECT_PAYMENT_LATER = 2;
const BUY_ON_CASH_PAY_RIGHT_AWAY = 3;
const BUY_ON_CREDIT_PAY_LATER = 4;
const RECEIVE_PAYMENT_FROM_CUSTOMER = 5;
const RECEIVE_ADVANCE_FROM_CUSTOMER = 6;
const PAY_VENDOR_SUPPLIER = 7;
const PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER = 8;
const RECEIVE_SPECIAL_ADJUSTMENTS_AMOUNT_FROM_VENDORS = 9;
const PAY_SPECIAL_ADJUSTMENTS_AMOUNT_TO_VENDORS = 10;
const BUY_ON_PETTY_CASH_ACCOUNT = 11;
const SALES_RETURNS = 12;
const PURCHASE_RETURNS = 13;
const TRANSFER_MAIN_CASH_TO_PETTY_CASH = 14;
const REQUEST_FOR_TRAVEL_ADVANCE = 15;
const SETTLE_TRAVEL_ADVANCE = 16;
const REQUEST_ADVANCE_FOR_EXPENSE = 17;
const SETTLE_ADVANCE_FOR_EXPENSE = 18;
const REQUEST_FOR_EXPENSE_REIMBURSEMENT = 19;
const MAKE_PROVISION_JOURNAL_ENTRY = 20;
const JOURNAL_ENTRY = 21;
const WITHDRAW_CASH_FROM_BANK = 22;
const DEPOSIT_CASH_IN_BANK = 23;
const TRANSFER_FUNDS_FROM_ONE_BANK_TO_ANOTHER = 24;
const TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER = 25;
const INVENTORY_OPENING_BALANCE = 26;
const PREPARE_QUOTATION = 27;
const PROFORMA_INVOICE = 28;
const PURCHASE_ORDER = 29;
const CREDIT_NOTE_CUSTOMER = 30;
const DEBIT_NOTE_CUSTOMER = 31
const CREDIT_NOTE_VENDOR = 32;
const DEBIT_NOTE_VENDOR = 33;
const PROCESS_PAYROLL = 34;
const REFUND_ADVANCE_RECEIVED = 35;
const REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE=36;
const REVERSAL_OF_ITC = 37;
const CANCEL_INVOICE = 38;
const BILL_OF_MATERIAL = 39;
const CREATE_PURCHASE_REQUISITION = 40;
const CREATE_PURCHASE_ORDER = 41;
const MATERIAL_ISSUE_NOTE = 42;

//for IdosFileUploadLogs
const BOM_TXN_TYPE = "BOM";
const MAIN_TXN_TYPE = "TXN";
const PJE_TXN_TYPE = "PJE";
const CLAIM_TXN_TYPE = "CLM";
const ORG_MODULE_TYPE = "ORG";
const BRANCH_MODULE_TYPE = "BRN";
const CUSTOMER_MODULE_TYPE = "CUS";
const VENDOR_MODULE_TYPE = "VEN";
const BRANCH_MODULE_TYPE_FOR_INDEX = "BRANCH";
const PJE_TXN_TYPE_FOR_INDEX = "PROV";
const CLAIM_TXN_TYPE_FOR_INDEX = "CLAIM";
var GST_COUNTRY_CODE = '';
var DEBUG = true;
// used to edit transaction
var isTransactionEditEnabled=false;

//global variables start
var dynmkeyoff=0; var activeTab;
var dynmkeydep=0; var cashNBankBranch="";
var dynmbnkAccount=0; var cashNBankBank="";
var enteredUserEmail=""; var cashNBankFromDate="";
var enteredBranchSpecifics=""; var cashNBankToDate="";
var hiringRequest = {}; var trialBalanceBranch=""; var periodicInventoryBranch=""; var reportInventoryBranch="";
var replotGraph = []; var trialBalanceFromDate=""; var periodicInventoryFromDate=""; var reportInventoryFromDate="";
var onFocusClickEmail=""; var trialBalanceToDate=""; var periodicInventoryToDate=""; var reportInventoryToDate="";
var pushToTop = ""; var userTransactionListString=""; var reportAllInventorySpecifics=""; var reportAllInventoryBranch="";
var userClaimTransactionListString="";
var userTransactionListTwoFiftyString="";
var userTransactionListFiveHundredString="";
var userTransactionListThousandString="";
var internationalOptionItems="";



//global variables ends
//datepicker for date input fields
var maximumYear=new Date().getFullYear()+30;

/*Sunil: This is used to search text case-insensitive, unlike contains
	$("div:containsCI('\\bup\\b')") (Matches "Up" or "up", but not "upper", "wakeup", etc.)
	$("div:containsCI('(?:Red|Blue) state')") (Matches "red state" or "blue state", but not "up state", etc.)
	$("div:containsCI('^\\s*Stocks?')") (Matches "stock" or "stocks", but only at the start of the paragraph (ignoring any leading whitespace).)
*/
jQuery.extend (
    jQuery.expr[':'].containsCI = function (a, i, m) {
        //-- faster than jQuery(a).text()
        var sText   = (a.textContent || a.innerText || "");
        var zRegExp = new RegExp (m[3], 'i');
        return zRegExp.test (sText);
    }
);

function escapeHtml(str) {
    if (typeof(str) == "string"){
        try{
            var newStr = "";
            var nextCode = 0;
            for (var i = 0;i < str.length;i++){
                nextCode = str.charCodeAt(i);
                if (nextCode > 0 && nextCode < 128){
                    newStr += "&#"+nextCode+";";
                }
                else{
                    newStr += "?";
                }
             }
             return newStr;
        }
        catch(err){
        }
    }
    else{
        return str;
    }
}


$(function() {
	$(".datepicker").datepicker({
		changeMonth : true,
		changeYear : true,
		dateFormat:  'MM d,yy',
		yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
		onSelect: function(x,y){
			var elemName=$(this).attr('name');
			if(elemName=='bnchPremiseValidityTo'){
				if($("#bnchPremiseValidityFrom").val()==""){
					$(this).val("");
				}
				var d1 = new Date($("#bnchPremiseValidityTo").val())
				var d2 = new Date($("#bnchPremiseValidityFrom").val())
				if (d1 < d2) {
					swal("Branch Premise validity To cannot be less than validity from");
				   $(this).val("");
				   return true;
				}
			}
			if(elemName=='bnchPremiseAlertValidityFrom'){
				var validityEndsOn=$("div[class='branchPremiseValidityEndStatic']").text();
				var validityEndsOnArr=validityEndsOn.split(":");
				var validityEndsOnDate=validityEndsOnArr[1];
				var d1 = new Date($("#bnchPremiseAlertValidityFrom").val());
				var d2 = new Date(validityEndsOnDate);
				if (d1 < d2) {
				   alert ("Branch Premise Updated Validity From cannot be less than validity Ends Date.");
				   $(this).val("");
				   return true;
				}
			}
			if(elemName=='bnchPremiseAlertValidityTo'){
				var validityEndsOn=$("div[class='branchPremiseValidityEndStatic']").text();
				var validityEndsOnArr=validityEndsOn.split(":");
				var validityEndsOnDate=validityEndsOnArr[1];
				var d1 = new Date($("#bnchPremiseAlertValidityTo").val());
				var d2 = new Date(validityEndsOnDate);
				var d3 = new Date($("#bnchPremiseAlertValidityFrom").val());
				if(d1 < d2 || d1 < d3){
					 alert ("Branch Premise Updated Validity To cannot be less than Validity From or Validity Ends Date.");
					 $(this).val("");
					 return true;
				}
			}
			if(elemName=='bnchPremiseAlertRentRevisedDueOn'){
				var rentRivisionEndsOn=$("div[class='branchPremiseRentRevisionStatic']").text();
				var rentRivisionOnArr=rentRivisionEndsOn.split(":");
				var rentRivisionOnDate=rentRivisionOnArr[1];
				var d1 = new Date($("#bnchPremiseAlertRentRevisedDueOn").val());
				var d2 = new Date(rentRivisionOnDate);
				if (d1 < d2) {
				   swal("Error!","Branch Premise Renewed Rent Rivision Date cannot be less than Rent Renewal Ends Date.","error");
				   $(this).val("");
				   return true;
				}
			}
			if(elemName=='bnchStatutoryAlertvalidFrom'){
				var validityEndsOn=$("div[class='branchStatutoryValidityEndStatic']").text();
				var validityEndsOnArr=validityEndsOn.split(":");
				var validityEndsOnDate=validityEndsOnArr[1];
				var d1 = new Date($("#bnchStatutoryAlertvalidFrom").val());
				var d2 = new Date(validityEndsOnDate);
				if (d1 < d2) {
				   swal("Error!","Branch Statutory Updated Validity From cannot be less than validity Ends Date.","warning");
				   $(this).val("");
				   return true;
				}
			}
			if(elemName=='bnchStatutoryAlertvalidTo'){
				var validityEndsOn=$("div[class='branchStatutoryValidityEndStatic']").text();
				var validityEndsOnArr=validityEndsOn.split(":");
				var validityEndsOnDate=validityEndsOnArr[1];
				var d1 = new Date($("#bnchStatutoryAlertvalidTo").val());
				var d2 = new Date(validityEndsOnDate);
				var d3 = new Date($("#bnchStatutoryAlertvalidFrom").val());
				if(d1 < d2 || d1 < d3){
					 swal("Error!","Branch Statutory Updated Validity To cannot be less than Validity From or Validity Ends Date.","error");
					 $(this).val("");
					 return true;
				}
			}
			if(elemName=='alertinsuranceValidityFrom'){
				var validityEndsOn=$("div[class='branchInsuranceValidityEndStatic']").text();
				var validityEndsOnArr=validityEndsOn.split(":");
				var validityEndsOnDate=validityEndsOnArr[1];
				var d1 = new Date($("#alertinsuranceValidityFrom").val());
				var d2 = new Date(validityEndsOnDate);
				if (d1 < d2) {
				   swal("Error!","Branch Insurance Updated Validity From cannot be less than validity Ends Date.","error");
				   $(this).val("");
				   return true;
				}
			}
			if(elemName=='alertinsuranceValidityTo'){
				var validityEndsOn=$("div[class='branchInsuranceValidityEndStatic']").text();
				var validityEndsOnArr=validityEndsOn.split(":");
				var validityEndsOnDate=validityEndsOnArr[1];
				var d1 = new Date($("#alertinsuranceValidityTo").val());
				var d2 = new Date(validityEndsOnDate);
				var d3 = new Date($("#alertinsuranceValidityFrom").val());
				if(d1 < d2 || d1 < d3){
					 swal("Error!","Branch Insurance Updated Validity To cannot be less than Validity From or Validity Ends Date.","error");
					 $(this).val("");
					 return true;
				}
			}
			if(elemName=='bnchStatutoryvalidTo'){
				var parentTr=$(this).parent().parent().attr('id');
				if($("#"+parentTr+" input[id='bnchStatutoryvalidFrom']").val()==""){
					$(this).val("");
				}
				var d1 = new Date($("#"+parentTr+" input[id='bnchStatutoryvalidTo']").val())
				var d2 = new Date($("#"+parentTr+" input[id='bnchStatutoryvalidFrom']").val())
				if (d1 < d2) {
					swal("Error!","Branch Statutory validity To cannot be less than validity from","error");
				   $(this).val("");
				   return true;
				}
			}
			if(elemName=='bnchPremiseValidityTo'){
				var d1 = new Date($("#bnchPremiseValidityTo").val());
				var d2 = new Date($("#bnchPremiseValidityFrom").val());
				if (d1 < d2) {
					swal("Error!","Branch Statutory validity To cannot be less than validity from","error");
					$(this).val("");
					return true;
				}
			}
			if(elemName=='rentRevisedDueOn'){
				var d1 = new Date($("#rentRevisedDueOn").val());
				var d2 = new Date($("#bnchPremiseValidityTo").val());
				var d3 = new Date($("#bnchPremiseValidityFrom").val());
				if (d1 < d2 || d1<d3) {
					swal("Error!","Branch Rent Revision Due On Date cannot be less than validity from or validity to date.","error");
					$(this).val("");
					return true;
				}
			}
			if(elemName=='insuranceValidityTo'){
				var parentTr=$(this).parent().parent().attr('id');
				if($("#"+parentTr+" input[id='insuranceValidityFrom']").val()==""){
					$(this).val("");
				}
				var d1 = new Date($("#"+parentTr+" input[id='insuranceValidityTo']").val())
				var d2 = new Date($("#"+parentTr+" input[id='insuranceValidityFrom']").val())
				if (d1 < d2) {
					swal("Error!","Branch Insurence validity To cannot be less than validity from","error");
				   $(this).val("");
				   return true;
				}
			}
			if(elemName=='projectenddate'){
				if($("#projectstartdate").val()==""){
					$(this).val("");
				}
				var d1 = new Date($("#projectenddate").val())
				var d2 = new Date($("#projectstartdate").val())
				if (d1 < d2) {
					swal("Error!","project end date cannot be less than project start date","error");
				   $(this).val("");
				   return true;
				}
			}
			if(elemName=='agreementValidityTo'){
				if($("#vendoeContractalidityFrom").val()==""){
					$(this).val("");
				}
				var d1 = new Date($("#vendoeContractalidityTo").val())
				var d2 = new Date($("#vendoeContractalidityFrom").val())
				if (d1 < d2) {
					swal("Error!","vendor contract validity to date cannot be before vendor contract validity from date ","error");
				   $(this).val("");
				   return true;
				}
			}
			if(elemName=='alertForReversalRequiredDateOfReversal'){
				var d1=new Date();
				var d2=new Date($("#alertForReversalRequiredDateOfReversal").val());
				if(d2<d1){
					swal("Error!","Date Of Reversal of Provision Cannot Be Before Current Date","error");
					$(this).val("");
					return true;
				}
				if(d2>d1){
					var daysDiff=parseInt((d2-d1)/(24*3600*1000));
					if(daysDiff>10){
						swal("Error!","Provision Transaction Must Be Reversed Within 10 Days Of Creation.","error");
						$(this).val("");
						return true;
					}
				}
			}
	        $(this).focus();
	    }
	});
});

var maximumYear=new Date().getFullYear()+30;
$(function() {
	$("#end-date").datepicker({
		changeMonth : true,
		changeYear : true,
		dateFormat:  'MM d,yy',
		yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
		onSelect: function(x,y){
	        $(this).focus();
	    }
	});
});

$(function() {
	$(".fincyear").datepicker({
		changeMonth : true,
		changeYear : false,
		dateFormat:  'MM d',
		yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
		 beforeShow: function (input, inst) {
             inst.dpDiv.addClass('fnchDatePicker');
         },
         onClose: function(dateText, inst){
             inst.dpDiv.removeClass('fnchDatePicker');
         },
         onSelect: function(x,y){
        	var elemName=$(this).attr('name');
 	        $(this).focus();
 	        if(elemName=="fincendyear"){
	 	        if($("#fincstartyear").val()==""){
	 		        $("#fincendyear").val("");
	 		        swal("Invalid data field!!","Please provid valid Financial Start Date","error");
	 		        return true;
	 	        }
 	        }
 	     }
	});
});

//to scroll vertical scrollbar on the top during page load
function alwaysScrollTop(){
	$("html, body").animate({ scrollTop: 0 }, "fast");
	$(".datascrolltable").each(function(){
		$(this).animate({scrollTop:0}, 'fast');
	});
    return false;
}

$(document).ready(function(){
    $("#itemBranch").change(function(){
       var isMapAllBranches=$('#mapallbranches').is(':checked');
       if(isMapAllBranches==true){
       	   $('#mapallbranches').attr('checked',false);
       }
    });
});

$(document).ready(function(){
    $("div[id*='search-button']").click(function(){
       $(".notify-success").hide();
    });
});

function autotab(original){
	if (original.getAttribute&&original.value.length==original.getAttribute("maxlength")){
		var divName=$(original).parent("div:first").attr('id');
	    var index;
	    if(original.name.substring(original.name.length-1,original.name.length)=="1"){
	    	index="2";
	    }
	    if(original.name.substring(original.name.length-1,original.name.length)=="2"){
	    	index="3";
	    }
	    var newdest=original.name.substring(0,original.name.length-1)+index;
	    if(divName==undefined){
	        var flag=1;
			$("input[name="+newdest+"]").each(function(){
				if(flag==1){
					$(this).focus();
					flag=0;
				}else{
					$(this).blur();
				}
			});
		}else{
		    $("#"+divName+" input[name="+newdest+"]").focus();
		}
	}
}

var autotab2 = function(elem){
    if (elem.value.length === elem.maxLength) {
      $(elem).next('.contactNoCls').focus();
    }
}


jQuery(document).keyup(function(event){
    if (event.which == 13 || event.keyCode == 13) {
    	var sourceTagname=event.target.tagName.toLowerCase() || event.srcElement.tagName.toLowerCase();
    	if(sourceTagname!="select" && sourceTagname!='textarea'){
	    	var divarr=["signUpDiv","logindiv","subslogindiv","forgotlogindiv","resetLoginCred","staticsystemconfig","staticchangepassword","vendCustAccount","vendCustForgotAccount","sellerforgotlogindiv","sellerresetLoginCred","sellerAccountLoginDiv","sellerAccountRegDiv","outsideTableDivId"];
	    	for(var i=0;i<divarr.length;i++){
	    		if ($("#"+divarr[i]+"").is(':visible')){
	    			var buttonClass=$("#"+divarr[i]+" button[class!='multiselect dropdown-toggle btn']:first").attr('class');
	    	    	var n=buttonClass.indexOf(' ');
	    	    	var btncls=buttonClass.substring(0,n);
	    	    	$("."+btncls+"").click();
	    		}
	    	}
    	}
    }
    if (event.which == 32 || event.keyCode == 32) {
    	var elemId=event.target.id || event.srcElement.id;
    	var value=$("#"+elemId+"").val();
    	if(value==" "){
    		$("#"+elemId+"").val("");
    		return false;
    	}
    }
});

$(document).ready(function(){
	$('input').keyup(function(event){
		var elemId=event.target.id || event.srcElement.id;
    	var value=$("#"+elemId+"").val();
		if (event.which == 188 || event.keyCode == 188) {
			var newVal=value.substring(0, value.length-1);
			swal("Invalid data field!!","(,{}[]) are not allowed as special characters in sentances","error");
			$("#"+elemId+"").val(newVal);
		}
		if (event.which == 219 || event.keyCode == 219) {
			var newVal=value.substring(0, value.length-1);
			swal("Invalid data field!!","(,{}[]) are not allowed as special characters in sentances","error");
			$("#"+elemId+"").val(newVal);
		}
		if (event.which == 221 || event.keyCode == 221) {
			var newVal=value.substring(0, value.length-1);
			swal("Invalid data field!!","(,{}[]) are not allowed as special characters in sentances","error");
			$("#"+elemId+"").val(newVal);
		}
	});
});

$(document).ready(function(){
	$('textarea').keyup(function(event){
		var elemId=event.target.id || event.srcElement.id;
    	var value=$("#"+elemId+"").val();
		/* should be allowed in address, remakrs: Sunil
		if (event.which == 188 || event.keyCode == 188) {
			var newVal=value.substring(0, value.length-1);
			alert("({}[]) are not allowed as special characters in sentances");
			$("#"+elemId+"").val(newVal);
		} */
		if (event.which == 219 || event.keyCode == 219) {
			var newVal=value.substring(0, value.length-1);
			swal("Invalid data field!!","({}[]) are not allowed as special characters in sentances","error");
			$("#"+elemId+"").val(newVal);
		}
		if (event.which == 221 || event.keyCode == 221) {
			var newVal=value.substring(0, value.length-1);
			swal("Invalid data field!!","({}[]) are not allowed as special characters in sentances","error");
			$("#"+elemId+"").val(newVal);
		}
	});
});


$(document).ready(function(){
	$(".preinstallcatsubcat").treeview({
		animated: "fast",
		collapsed: false,
		unique: true,
		toggle: function() {
		}
	});
});

$(document).ready(function(){
	$(".mainChartOfAccount").treeview({
		animated: "fast",
		collapsed: true,
		unique: true,
		toggle: function() {
		}
	});
});


$(document).ready(function(){
	$(window).bind("hashchange", function(e) {
		window.history.forward();
	});
});

$(document).ready(function(){
	$(window).bind("popstate", function(e) {
		window.history.forward();
	});
});

$(document).ready(function(){
	$(window).on('beforeunload ',function(){
		console.log("in beforeunload");
		//return sendUser();
	});
});

function sendUser(){
	 var jsonData = {};
	 var useremail=$("#hiddenuseremail").text();
	 jsonData.usermail = useremail;
	 var url="/config/setPageUser";
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
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){
				doLogout();
			}
		}
	 });
}



function scrollTransactionBody() {
    $('#transactionTable').Scrollable(350, 1300);
}

$(document).ready(function(){
    $('input[name="projectstartdate"]').change(function(){
        $("#projectenddate").val("");
    	$("#projectenddate").attr("class","");
    	var maximumYear=new Date().getFullYear()+30;
        $("#projectenddate" ).datepicker({
             changeMonth : true,
             changeYear: true,
             minDate: new Date($("#projectstartdate").val()),
             dateFormat:  'MM d,yy',
             yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
             onSelect: function(x,y){
					var elemName=$(this).attr('name');
					if(elemName=='projectenddate'){
						if($("#projectstartdate").val()==""){
							$(this).val("");
						}
						var d1 = new Date($("#projectenddate").val())
						var d2 = new Date($("#projectstartdate").val())
						if (d1 < d2) {
							swal("Invalid data field!!","project end date cannot be less than project start date","error");
						   $(this).val("");
						   return true;
						}
					}
					$(this).focus();
				}
        });
    });
});

$(document).ready(function(){
    $('input[name="agreementValidityFrom"]').change(function(){
    	var maxYear=new Date().getFullYear()+30;
        $("#agreementValidityTo").val("");
        $("#agreementValidityTo").attr("class","");
         $("#agreementValidityTo" ).datepicker({
             changeMonth : true,
             changeYear: true,
             minDate: new Date($("#agreementValidityFrom").val()),
             dateFormat:  'MM d,yy',
             yearRange: ''+new Date().getFullYear()+':'+maxYear+'',
             onSelect: function(x,y){
     	        $(this).focus();
     	     }
        });
        var datepickerclass=$("#agreementValidityTo").attr("class");
        datepickerclass+=" "+"input-medium";
        $("#agreementValidityTo").attr("class",datepickerclass);
    });
});

$(document).ready(function(){
    $('input[id="start-date"]').change(function(){
    	var maxYear=new Date().getFullYear()+30;
        $("#end-date").val("");
    	$("#end-date").attr("class","");
         $("#end-date" ).datepicker({
             changeMonth : true,
             changeYear: true,
             minDate: new Date($("#start-date").val()),
             dateFormat:  'MM d,yy',
             yearRange: ''+new Date().getFullYear()+':'+maxYear+'',
             onSelect: function(x,y){
     	        $(this).focus();
     	     }
        });
    });
});


$(document).ready(function(){
    $('input[name="projectenddate"]').change(function(){
        if($("#projectstartdate").val()==""){
	        $("#projectenddate").val("");
	        swal("Invalid data field!!","Please provid valid Project Start Date","error");
        }
    });
});

$(document).ready(function(){
    $('input[id="end-date"]').change(function(){
        if($("#start-date").val()==""){
	        $("#end-date").val("");
	        swal("Invalid data field!!","Please provid valid Start Date","error");
        }
    });
});

$(document).ready(function(){
    $('input[id="agreementValidityTo"]').change(function(){
        if($("#agreementValidityFrom").val()==""){
	        $("#agreementValidityTo").val("");
	        swal("Invalid data field!!","Please provid valid Start Date","error");
        }
    });
});

$(document).ready(function(){
    $('input[name="fincstartyear"]').change(function(){
        $("#fincendyear").val("");
    	$("#fincendyear").attr("class","");
         $("#fincendyear" ).datepicker({
             changeMonth : true,
             changeYear: false,
             minDate: new Date($("#fincstartyear").val()),
             dateFormat:  'MM d',
             yearRange: ''+new Date().getFullYear()-100+':'+new Date().getFullYear()+'',
             beforeShow: function (input, inst) {
                 inst.dpDiv.addClass('fnchDatePicker');
             },
             onClose: function(dateText, inst){
                 inst.dpDiv.removeClass('fnchDatePicker');
             },
             onSelect: function(x,y){
     	        $(this).focus();
     	     }
        });
    });
});

$(document).ready(function(){
    $('input[name="fincendyear"]').change(function(){
    });
});

$(document).ready(function(){
	$('#successmsgclose'). click(function(){
		$(this).attr("href",location.hash);
		$("#successregdiv").slideUp('slow');
	});
});

$(document).ready(function(){
	$('.addNewReportTo'). click(function(){
		var newReportsToName=$("#newReportToName").val();
		if(newReportsToName!=""){
			var jsonData = {};
			jsonData.usermail = $("#hiddenuseremail").text();
			jsonData.newReportsToDesignationName=newReportsToName;
			var url="/addNewReportTo";
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
					var reportsTo=data.addedReprtsToDesignation[0].addedReportTo;
					$("#reportsTo").append('<option>'+reportsTo+'</option>');
					$("#newReportToName").val("");
				},
				error: function (xhr, status, error) {
					if(xhr.status == 401){
						doLogout();
					}
				}
			});
		}else{
			swal("Invalid data field!!","Please enter name of the new designation you want to add in the report to list before submitting","error");
			return true;
		}
	});
});

function populateProjectHiringPositionData(elem){
	$("#projectTitle").val("");
	$("#projectNumber").val("");
	$("#positionName").children().remove();
	$("#positionName").append('<option value="">-Please Select-</option>');
	$('#lhAgreement').fadeOut();
	$('#lhAgreementDoc').html('').removeAttr('onclick');
	var projectId=$(elem).val();
	if(projectId!=""){
	var jsonData = {};
		jsonData.userEmail = $("#hiddenuseremail").text();
		jsonData.pjctId=projectId;
		ajaxCall('/labour/getProjectDatas', jsonData, '', '', '', '', 'getProjectDatasSuccess', '', false);
	}
}

function getProjectDatasSuccess(data){
	for(var i=0;i<data.projectDatas.length;i++){
		$("#projectTitle").val(data.projectDatas[i].projectname);
		$("#projectNumber").val(data.projectDatas[i].projectnumber);
	}
	for(var j=0;j<data.projectPositionDatas.length;j++){
		$("#positionName").append('<option value="'+data.projectPositionDatas[j].projectLabPosId+'">'+data.projectPositionDatas[j].projectLabPosName+'</option>');
	}
}

function populateJobDetails(elem){
	var projectPositionId=$(elem).val();
	$("#requestAllowedForBranches").children().remove();
	$("#requestAllowedForBranches").append('<option value="">-Please Select-</option>');
	$("#experienceLevel option:first").prop("selected","selected");
	$("#positionQualification").val("");
	$(".jobDetailsLanguages").html("");
	$("#jobDescription").val("");
	$("#advertisingPlace").val("");
	$("#advertisingDate").val("");
	if(projectPositionId!=""){
		var jsonData = {};
		jsonData.userEmail = $("#hiddenuseremail").text();
		jsonData.pjctPosId=projectPositionId;
		ajaxCall('/labour/getJobDetailsDatas', jsonData, '', '', '', '', 'getJobDetailsDatasSuccess', '', false);
	}
}

function getJobDetailsDatasSuccess(data){
	for(var i=0;i<data.jobDetailsDatas.length;i++){
		if(data.jobDetailsDatas[i].requestAllowedForBranches!=null && data.jobDetailsDatas[i].requestAllowedForBranches!=""){
			$("#requestAllowedForBranches").append('<option value="'+data.jobDetailsDatas[i].requestAllowedForBranches+'">'+data.jobDetailsDatas[i].requestAllowedForBranches+'</option>');
		}
		$("#requestAllowedForBranches option[value='"+data.jobDetailsDatas[i].requestAllowedForBranches+"']").prop("selected","selected");
		var qualValue=data.jobDetailsDatas[i].projLabQualification+data.jobDetailsDatas[i].projLabQualificationDegree;
		$("#positionQualification").val(qualValue);
		$("#jobDescription").val(data.jobDetailsDatas[i].jobDescription);
		$("#advertisingPlace").val(data.jobDetailsDatas[i].advertisingPlace);
		$("#advertisingDate").val(data.jobDetailsDatas[i].advertisingDate);
		$("#experienceLevel option[value='"+data.jobDetailsDatas[i].expReq+"']").prop("selected","selected");
		if(!isEmpty(data.jobDetailsDatas[i].agreement)){
			$('#lhAgreementDoc').html(data.jobDetailsDatas[i].agreement);
			$('#lhAgreementDoc').attr('onclick','downloadfile("'+data.jobDetailsDatas[i].agreement+'")');
			$('#lhAgreement').fadeIn();
		}else{
			$('#lhAgreement').hide();
			$('#lhAgreementDoc').html('').removeAttr('onclick');
		}
	}
	for(var k=0;k<data.jobDetailsLanguageDatas.length;k++){
		$(".jobDetailsLanguages").append('<div><b class="individualLangProf">'+data.jobDetailsLanguageDatas[k].langProf+'</b></div>');
	}
}

function cancelSearch(){
	$("#searchTransaction").slideUp('slow', function(){
		//$("#transactionTable tbody").html("");
		//$("#transactionTable tbody").html(userTransactionListString);
	});
}

function cancelCustomerVendorSearch(){
	$("#searchtransaction-form-container").slideUp('slow', function(){
	});
}

$(document).ready(function(){
	$('.whatDoINeedToDoButton').click(function(){
		$(".whatDoINeedToDoContent").show();
	});
});


$(document).ready(function(){
	$( 'ul.custom-font-on-white-hover a').click(function() {
		alwaysScrollTop();
		var id=this.id;
		var href=this.href;
		leftmenuhideunhide(id,href);
		var anchorClass=$(this).attr('class');
		if(anchorClass==""){
			$( 'ul.custom-font-on-white-hover a[id!='+this.id+']').each(function() {
				$(this).attr('class',"");
			});
		}
		$( 'ul.custom-font-on-white-hover li').each(function() {
			$(this).attr('class',"");
		});
		$(this).parent().parent().attr('class',"first");
		$(this).attr('class',"on custom-background font-on-custom-background");
		$("li#"+this.id+"").attr('class',"wiz_arrow wiz_on_right custom-background");
	});
});

function returnToMainLogin(){
	$("#logindiv input:enabled:visible:first").focus();
	$(".mainDiv").hide();
	$("#logindiv").show();
	window.location.href="/signIn#logindiv";
}

$(document).ready(function(){
$('.multipleDropdown').multiselect({
    maxHeight: 150,
    enableFiltering :true,
    includeSelectAllOption: true,
    enableCaseInsensitiveFiltering: true,
    onChange: function(element, checked) {
      var elemId=$(element).context.id;
      if(elemId=="userRole"){
    	  var elemValue=$(element).val();
    	  if(checked == true) {
    		  /*
    		   * Existing problem:
    		   * 1. When Auditor is selected all other roles are disabled which is correct.
    		   * 2. So ideally it should show 1 selected which is Auditor but it shows 3 selected etc which is previous selection too which is wrong.
    		   * 3. For userRole selectAll option should be disabled.
    		   */
   	    	 if(elemValue==7){ //If Auditor role selected then disable all other roles
   	    		  $('input:checkbox[value=3]').prop("checked",false); //even if creator is selected, uncheck that
   	    		  $('input:checkbox[value=4]').prop("checked",false);
   	    		  $('input:checkbox[value=5]').prop("checked",false);
   	    		  $('input:checkbox[value=6]').prop("checked",false);
   	    		  $('input:checkbox[value=3]').prop("disabled",true); //Creator option disabled
   	    		  $('input:checkbox[value=4]').prop("disabled",true); //Approver
   	    		  $('input:checkbox[value=5]').prop("disabled",true); //Accountant
   	    		  $('input:checkbox[value=6]').prop("disabled",true); //Controller

   	    		 // $('.multipleDropdown').multiselect('rebuild');
   	    		//$('input:checkbox[value=3]').remove();
   	    		//$("#userRole").find('option[value="4"]').remove();
   	    	 }
   	      }
    	  else if(checked == false) {
	    	  if(elemValue==1 || elemValue==8 || elemValue==9 || elemValue==12){
	    		  $('input:checkbox[value='+elemValue+']').prop("checked",true);
	    		  $("select[name='userRole'] option").filter(function () {return $(this).val()==elemValue;}).prop("selected", "selected");
	    	  }
	    	  else if(elemValue==7){ //if auditor role deslected, enable all other roles
  	    		  $('input:checkbox[value=3]').prop("disabled",false);
  	    		  $('input:checkbox[value=4]').prop("disabled",false);
  	    		  $('input:checkbox[value=5]').prop("disabled",false);
  	    		  $('input:checkbox[value=6]').prop("disabled",false);
  	    	  }
	      }
      }
    }
  });
});

// search Transaction only

$(document).ready(function(){
$('.multipleDropdownForSearch').multiselect({
    maxHeight: 110,
    enableFiltering :true,
    includeSelectAllOption: true,
    numberDisplayed: 0,
    enableCaseInsensitiveFiltering: true,
    onChange: function(element, checked) {
      if (element) {
		var elemId=$(element).attr('id');
	  } else {
		var elemId=null;
	  }
      if(elemId=="userRole"){
    	  var elemValue=$(element).val();
    	  if(checked == true) {
    		  /*
    		   * Existing problem:
    		   * 1. When Auditor is selected all other roles are disabled which is correct.
    		   * 2. So ideally it should show 1 selected which is Auditor but it shows 3 selected etc which is previous selection too which is wrong.
    		   * 3. For userRole selectAll option should be disabled.
    		   */
   	    	 if(elemValue==7){ //If Auditor role selected then disable all other roles
   	    		  $('input:checkbox[value=3]').prop("checked",false); //even if creator is selected, uncheck that
   	    		  $('input:checkbox[value=4]').prop("checked",false);
   	    		  $('input:checkbox[value=5]').prop("checked",false);
   	    		  $('input:checkbox[value=6]').prop("checked",false);
   	    		  $('input:checkbox[value=3]').prop("disabled",true); //Creator option disabled
   	    		  $('input:checkbox[value=4]').prop("disabled",true); //Approver
   	    		  $('input:checkbox[value=5]').prop("disabled",true); //Accountant
   	    		  $('input:checkbox[value=6]').prop("disabled",true); //Controller

   	    		 // $('.multipleDropdown').multiselect('rebuild');
   	    		//$('input:checkbox[value=3]').remove();
   	    		//$("#userRole").find('option[value="4"]').remove();
   	    	 }
   	      }
    	  else if(checked == false) {
	    	  if(elemValue==1 || elemValue==8 || elemValue==9 || elemValue==12){
	    		  $('input:checkbox[value='+elemValue+']').prop("checked",true);
	    		  $("select[name='userRole'] option").filter(function () {return $(this).val()==elemValue;}).prop("selected", "selected");
	    	  }
	    	  else if(elemValue==7){ //if auditor role deslected, enable all other roles
  	    		  $('input:checkbox[value=3]').prop("disabled",false);
  	    		  $('input:checkbox[value=4]').prop("disabled",false);
  	    		  $('input:checkbox[value=5]').prop("disabled",false);
  	    		  $('input:checkbox[value=6]').prop("disabled",false);
  	    	  }
	      }
      }
    }
  });
});




/*
$(document).ready(function(){
$("select[name='userRole']").multiselect({
			        buttonClass: 'btn',
			        buttonWidth: '130px',
			        maxHeight:   90,
			        includeSelectAllOption: false,
			        enableFiltering :true,
			        buttonText: function(options) {
			          if (options.length == 0) {
			                  return 'None selected <b class="caret"></b>';
			              }
			              else if (options.length > 6) {
			                  return options.length + ' selected  <b class="caret"></b>';
			              }
			              else {
			                  var selected = '';
			                  options.each(function() {
			              selected += $(this).text() + ', ';
			                  });

			                  return options.length + ' selected  <b class="caret"></b>';
			          }
			        },
			        onChange: function(element, checked) {
			        	var elemId=$(element).context.id;
			            if(elemId=="userRole"){
			          	  var elemValue=$(element).val();
			      	      if(checked == true) {
			      	    	 if(elemValue==7){ //If Auditor role selected then disable all other roles
			      	    		  $('input:checkbox[value=3]').prop("checked",false); //even if creator is selected, uncheck that
			      	    		  $('input:checkbox[value=4]').prop("checked",false);
			      	    		  $('input:checkbox[value=5]').prop("checked",false);
			      	    		  $('input:checkbox[value=6]').prop("checked",false);
			      	    		  $('input:checkbox[value=3]').prop("disabled",true); //Creator option disabled
			      	    		  $('input:checkbox[value=4]').prop("disabled",true); //Approver
			      	    		  $('input:checkbox[value=5]').prop("disabled",true); //Accountant
			      	    		  $('input:checkbox[value=6]').prop("disabled",true); //Controller
			      	    		//$('#userRole').multiselect('rebuild');
			      	    		//$('input:checkbox[value=3]').remove();
			      	    		//$("#userRole").find('option[value="4"]').remove();
			      	    	 }
			      	      }
			      	      else if(checked == false) {
			      	    	  if(elemValue==1 || elemValue==8 || elemValue==9 || elemValue==12){
			      	    		  $('input:checkbox[value='+elemValue+']').prop("checked",true);
			      	    		  $("select[name='userRole'] option").filter(function () {return $(this).val()==elemValue;}).prop("selected", "selected");
			      	    	  }
			      	    	  else if(elemValue==7){ //if auditor role deslected, enable all other roles
			      	    		  $('input:checkbox[value=3]').prop("disabled",false);
			      	    		  $('input:checkbox[value=4]').prop("disabled",false);
			      	    		  $('input:checkbox[value=5]').prop("disabled",false);
			      	    		  $('input:checkbox[value=6]').prop("disabled",false);
			      	    	  }
			      	      }
			           }
			        }
		});
});*/

$(document).ready(function(){
	$('select[name="futurePayment"]').change(function(){
		var value=$(this).val();
		if(value==1){
			$("#daysCreditLabel").hide();
			$("#daysOfCredit").hide();
		}
		if(value==0 || value==2){
			$("#daysCreditLabel").show();
			$("#daysOfCredit").show();
		} else {
			
		}
	});
});

$(document).ready(function(){
	$('select[name="custfutPayment"]').change(function(){
		var value=$(this).val();
		if(value==1){
			$("#custdaysCreditLabel").hide();
			$("#custdaysOfCredit").hide();
			$(".para-tm3-bm0").hide();
			$("#custCreditLimit").hide();
			$("#custTranExceedCredLim").hide();
			$("#custOpeningBalance").show();
			$("#custOpeningBalanceAdvPaid").show();
			$(".para-tm3-bm01").show();
		}
		if(value==0 || value==2){
			$("#custdaysCreditLabel").show();
			$("#custdaysOfCredit").show();
			$(".para-tm3-bm0").show();
			$("#custCreditLimit").show();
			$("#custTranExceedCredLim").show();
			$("#custOpeningBalance").show();
			$("#custOpeningBalanceAdvPaid").show();
			$(".para-tm3-bm01").show();
		}
	});
});

$(document).ready(function(){
	$('select[name="transactionPurpose"]').change(function(){
		var value=$(this).val();
		if(value==1){
			$('select[name="specifics"]').children().remove();
			$('select[name="vendor"]').children().remove();
			 var jsonData = {};
			 var useremail=$("#hiddenuseremail").text();
			 jsonData.usermail = useremail;
			 jsonData.transactionPurpose=value;
			 var url="/expenses/getTransaction";
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
					 var specsel = $("#specifics");
					 var vensel = $("#vendor");
					 if(data.specificsData[0].noData!="No Data"){
						 for (var i=0;i<data.specificsData.length;i++) {
							 specsel.append('<option value="'+data.specificsData[i].id+'">' +data.specificsData[i].name + '</option>');
						 }
					 }
					 if(data.vendorData[0].noData!="No Data"){
						 for (var i=0;i<data.vendorData.length;i++) {
							 vensel.append('<option value="'+data.vendorData[i].id+'">' +data.vendorData[i].name + '</option>');
						 }
						 $("#unitcost").val(data.vendorData[0].unitcost);
					 }
					 if(data.vendorData[0].noData=="No Data"){
						 $("#unitcost").val("");
					 }
					 alwaysScrollTop();
				},
				error: function (xhr, status, error) {
					if(xhr.status == 401){
						doLogout();
					}
				}
			 });
		}
		if(value==2){
			$('select[name="specifics"]').children().remove();
			$('select[name="vendor"]').children().remove();
			 var jsonData = {};
			 var useremail=$("#hiddenuseremail").text();
			 jsonData.usermail = useremail;
			 jsonData.transactionPurpose=value;
			 var url="/expenses/getTransaction";
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
					 var specsel = $("#specifics");
					 var vensel = $("#vendor");
					 if(data.specificsData[0].noData!="No Data"){
						 for (var i=0;i<data.specificsData.length;i++) {
							 specsel.append('<option value="'+data.specificsData[i].id+'">' +data.specificsData[i].name + '</option>');
						 }
					 }
					 if(data.vendorData[0].noData!="No Data"){
						 for (var i=0;i<data.vendorData.length;i++) {
							 vensel.append('<option value="'+data.vendorData[i].id+'">' +data.vendorData[i].name + '</option>');
						 }
						 $("#unitcost").val(data.vendorData[0].unitcost);
					 }
					 if(data.vendorData[0].noData=="No Data"){
						 $("#unitcost").val("");
					 }
					 alwaysScrollTop();
				},
				error: function (xhr, status, error) {
					if(xhr.status == 401){
						doLogout();
					}
				}
			 });
		}
	});
});

$(document).ready(function(){
	$('select[name="particulars"]').change(function(){
		 var particular=this.value;
		 var optName = $(this).find("option:selected").text();
		 $("#unitcost").val("");
		 $('#noofitems').val("");
		 $('#tamount').val("");
		 $('#remark').val("");
		 $('#docuploadurl').val("");
		 $('#remark').val("");
		 var jsonData = {};
		 jsonData.id = particular;
		 var url="/expenses/partBasedDetails";
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
				$('#specifics').children().remove();
				$('#vendor').children().remove();
				var specsel = $("#specifics");
				var vensel = $("#vendor");
				if(data.specificsData!=""){
				for (var i=0;i<data.specificsData.length;i++) {
					specsel.append('<option value="'+data.specificsData[i].id+'">' +data.specificsData[i].name + '</option>');
				}
				}
				if(data.vendorData!=""){
				for (var i=0;i<data.vendorData.length;i++) {
					vensel.append('<option value="'+data.vendorData[i].id+'">' +data.vendorData[i].name + '</option>');
				}
				}
				 $("#unitcost").val(data.vendorData[0].unitcost);
				 alwaysScrollTop();
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){
					doLogout();
				}
			}
		 });
	});
});

$(document).ready(function(){
	$('select[name="specifics"]').change(function(){
		var particular=this.value;
		 $('#noofitems').val("");
		 $('#tamount').val("");
		 $('#unitcost').val("");
		 $('#remark').val("");
		 $('#docuploadurl').val("");
		 $('#remark').val("");
		 var jsonData = {};
		 jsonData.id = particular;
		 var url="/expenses/vendors";
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
				if(typeof data.vendorData!='undefined'){
					 $('#vendor').children().remove();
					 var sel = $("#vendor");
					 for (var i=0;i<data.vendorData.length;i++) {
						 sel.append('<option value="'+data.vendorData[i].id+'">' +data.vendorData[i].name + '</option>');
					 }
					 $("#unitcost").val(data.vendorData[0].unitcost);
					 alwaysScrollTop();
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){
					doLogout();
				}
			}
		 });
	});
});

$(document).ready(function(){
	$('select[name="vendor"]').change(function(){
		$('#noofitems').val("");
		$('#tamount').val("");
		$('#remark').val("");
		$('#docuploadurl').val("");
		$('#remark').val("");
		$('#unitcost').val("");
		var vendorId=this.value;
		var specificsId=$('select[name="specifics"]').val();
		var jsonData = {};
		jsonData.specifics = specificsId;
		jsonData.vendor = vendorId;
		var url="/expenses/getcost";
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
				for (var i=0;i<data.unitcostData.length;i++) {
					$("#unitcost").val(data.unitcostData[i].unitcost);
				}
				alwaysScrollTop();
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){
					doLogout();
				}
			}
		});
	});
});

/*19th July 2016, procurement request part is commented from buy on cah/sell as well as from User setup
function populatebasedonprocurementrequest(elem){
	//alert("populatebasedonprocurementrequest") ;
	var parentTr=$(elem).parent().parent().attr('id');
	var text=$("#whatYouWantToDo").find('option:selected').text();
	var procurementRequest=$(elem).val();
	$("#rcpfccvendcustoutstandingsgross").text("");
	$(".dynmBnchBankActList").remove();
	$("#socpnreceiptdetail").find('option:first').prop("selected","selected");
	$("#rcpfccpaymentdetail").find('option:first').prop("selected","selected");
	$("#rcafccpaymentdetail").find('option:first').prop("selected","selected");
	$("#rsaafvpaymentdetail").find('option:first').prop("selected","selected");
	$("#paymentDetails").find('option:first').prop("selected","selected");
	$("#rcpfccvendcustoutstandingsnet").text("");
	$("#rcpfccvendcustoutstandingsnetdescription").text("");
	$("#rcpfccvendcustoutstandingspaid").text("");
	$("#rcpfccvendcustoutstandingsnotpaid").text("");
	$("#mcpfcvvendcustoutstandingsgross").text("");
	$("#mcpfcvvendcustoutstandingsnet").text("");
	$("#mcpfcvvendcustoutstandingsnetdescription").text("");
	$("#mcpfcvvendcustoutstandingspaid").text("");
	$("#mcpfcvvendcustoutstandingsnotpaid").text("");
	$("#mcpfcvtxninprogress").text("");

	$('#createExpense input[type="text"]').val("");
	$('#createExpense textarea').val("");
	$(".klBranchSpecfTd").text("");
	$(".itemParentNameDiv").text("");
	$(".inventoryItemInStock").text("");
	$(".customerVendorExistingAdvance").text("");
	$(".resultantAdvance").text("");
	$(".discountavailable").text("");
	$("#procurementRequestRemarks").text("");
	$(".netAmountDescriptionDisplay").text("");
	$("#bocpraTxnForProjects").children().remove();
	$("#bocpraTxnForProjects").append('<option value="">--Please Select--</option>');
	$("#bocaplTxnForProjects").children().remove();
	$("#bocaplTxnForProjects").append('<option value="">--Please Select--</option>');
	$(".netAmountVal").val("");
	$(".budgetDisplay").text("");
	$(".amountRangeLimitRule").text("");
	$("#bocaplunits").removeAttr("readonly");
	$("#bocpraunits").removeAttr("readonly");
	$(".actualbudgetDisplay").text("");
	$(".branchAvailablePettyCash").html("");
	$("#rcpfccvendcustoutstandingsgross").text("");
	$("#rcpfccvendcustoutstandingsnet").text("");
	$(".txnBranches option[value='']").prop("selected","selected");
	$("#procurementRequestRemarks").text("");
	$(".txnItems").children().remove();
	$(".txnItems").append('<option value="">--Please Select--</option>');
	if(procurementRequest!=""){
		var jsonData = {};
		jsonData.usermail = $("#hiddenuseremail").text();
		jsonData.procRequestId = procurementRequest;
		var url="/transaction/getprocurementbasedcreatordata";
		$.ajax({
			url: url,
			data:JSON.stringify(jsonData),
			type:"text",
		    method:"POST",
		    contentType:'application/json',
			success: function (data) {
				for(var i=0;i<data.procurementRequestBasedData.length;i++){
					if(text=="Buy on cash & pay right away"){
						$("#bocpraTxnForProjects").children().remove();
						$("#bocpraTxnForProjects").append('<option value="">--Please Select--</option>');
						$("select[name='bocpraTxnForBranches'] option[value='"+data.procurementRequestBasedData[i].procBranch+"']").prop("selected","selected");
						$("select[name='bocpraItem']").children().remove();
						$("select[name='bocpraItem']").append('<option value="">--Please Select--</option>');
						$("select[name='bocpraItem']").append('<option value="'+data.procurementRequestBasedData[i].procItem+'">'+data.procurementRequestBasedData[i].procItemName+'</option>');
						$("select[name='bocpraItem'] option[value='"+data.procurementRequestBasedData[i].procItem+"']").prop("selected","selected");
						for(var j=0;j<data.pjctData.length;j++){
							$("#bocpraTxnForProjects").append('<option value="'+data.pjctData[j].id+'">'+data.pjctData[j].name+'</option>');
						}
						$("select[name='bocpraItem']").trigger('change');
						$("#"+parentTr+" select[name='procurementRequestForCreator'] option[value='"+procurementRequest+"']").prop("selected","selected");
						$("#bocpraunits").val(data.procurementRequestBasedData[i].procNoOfUnit);
						$("#bocpraunits").attr("readonly","readonly");
						$("#"+parentTr+" div[id='procurementRequestRemarks']").text(data.procurementRequestBasedData[i].procRemarks);
					}
					if(text=="Buy on credit & pay later"){
						$("#bocaplTxnForProjects").children().remove();
						$("#bocaplTxnForProjects").append('<option value="">--Please Select--</option>');
						$("select[name='bocaplTxnForBranches'] option[value='"+data.procurementRequestBasedData[i].procBranch+"']").prop("selected","selected");
						$("select[name='bocaplItem']").children().remove();
						$("select[name='bocaplItem']").append('<option value="">--Please Select--</option>');
						$("select[name='bocaplItem']").append('<option value="'+data.procurementRequestBasedData[i].procItem+'">'+data.procurementRequestBasedData[i].procItemName+'</option>');
						$("select[name='bocaplItem'] option[value='"+data.procurementRequestBasedData[i].procItem+"']").prop("selected","selected");
						for(var j=0;j<data.pjctData.length;j++){
							$("#bocaplTxnForProjects").append('<option value="'+data.pjctData[j].id+'">'+data.pjctData[j].name+'</option>');
						}
						$("select[name='bocaplItem']").trigger('change');
						$("#"+parentTr+" select[name='procurementRequestForCreator'] option[value='"+procurementRequest+"']").prop("selected","selected");
						$("#bocaplunits").val(data.procurementRequestBasedData[i].procNoOfUnit);
						$("#bocaplunits").attr("readonly","readonly");
						$("#"+parentTr+" div[id='procurementRequestRemarks']").text(data.procurementRequestBasedData[i].procRemarks);
					}
				}
			},
			error: function (xhr, status, error) {

			}
		});
	}
}*/

$(document).ready(function(){
	$(".totalCostParam").keyup(function (){
		var noofitems=$("#noofitems").val();
		var itemId=$("#specifics").val();
		if(noofitems!=""){
			var unitPrice= $("#unitcost").val();
			calculateTax((noofitems*unitPrice),itemId);
		}else{
			$("#noofitems").val("1");
			$("#tamount").val(($("#noofitems").val()*$("#unitcost").val()).toFixed(2));
		}
	});
});

function calculateTax(totalCost,itemId){
        var jsonData = {};
		jsonData.itemId = itemId;
		var url="/expense/getTax";
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
				if(data.taxData[0].noTaxes!='noTaxes'){
					var rate=data.taxData[0].rate.split(",");
					var impType=data.taxData[0].impType.split(",");
					var impOn=data.taxData[0].impOn.split(",");
					var newCost=totalCost
					for(var i=0;i<rate.length;i++){
						if(impOn[i]==1){
							if(impType[i]==1){
								newCost=newCost+(totalCost*rate[i])/100;
							}else{
								newCost=newCost-(totalCost*rate[i])/100;
							}
						}
					}
					if(newCost!=0){
					totalCost=newCost;
					}
					for(var i=0;i<rate.length;i++){
						if(impOn[i]==0){
							if(impType[i]==1){
								totalCost=totalCost+(newCost*rate[i])/100;
							}else{
								totalCost=totalCost-(newCost*rate[i])/100;
							}
						}
					}
					totalCost=Math.round(totalCost*100);
					swal("Error!",totalCost,"error");
					$("#tamount").val(totalCost/100);
				}else{
					$("#tamount").val(totalCost);
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){
					doLogout();
				}
			}
		});
}

function knowledgeLibrary(){
	$("#itemlibrary").hide();
	if($("#particulars").val()!="" && $("#specifics").val()!=""){
		 var specificId=$("#specifics").val();
		 $("#componentContainer").html("");
		 var jsonData = {};
		 jsonData.specfId = specificId;
		 var url="/expense/getLibrary";
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
				for(var i=0;i<data.itemkLibrary.length;i++){
					if(data.itemkLibrary[0].knowledgeLibrary!=""){
						$("#itemlibrary").show();
						$("#kLibrary").html(data.itemkLibrary[0].knowledgeLibrary);
					}
				}
				alwaysScrollTop();
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){
					doLogout();
				}
			}
		});
	}
}

function taxTable(){
	$("#taxList").hide();
	if($("#specifics").val()!=""){
        var specificId=$("#specifics").val();
        $("#taxList").hide();
        $('#itemTaxTable tbody').children().remove();
		var jsonData = {};
		jsonData.itemId = specificId;
		var url="/expense/getTax";
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
				var itype,ion;
				var userItemlisttable=$("#itemTaxTable");
				if(data.taxData[0].noTaxes!="noTaxes"){
					for(var i=0;i<data.taxData.length;i++){
						var rate=data.taxData[i].rate;
						var impType=data.taxData[i].impType;
						var impOn=data.taxData[i].impOn;
						var tName=data.taxData[i].taxName;
						var tId=data.taxData[i].taxId;
						if(impType==1){
						    itype="Added";
						}else{
						    itype="Deducted";
						}
						if(impOn==1){
						    ion="Gross";
						}else{
						    ion="Net";
						}
			            $("#taxList").show();
			            userItemlisttable.append('<tr name="itemTaxEntity'+tId+'"><td>'+tName+'</td><td>'+itype+'</td><td>'+ion+'</td><td>'+rate+'%</td></tr>');
					}
				}
				alwaysScrollTop();
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){
					doLogout();
				}
			}
		});
	}
}

function completeTxn() {
	var txnPurpose = $('select[name="transactionPurpose"]').val();
	var txnPurposeText = $('select[name="transactionPurpose"]').find("option:selected").text();
	var txnPurchaseType = $('select[name="paymentType"]').val();
	var txnPurchaseTypeText = $('select[name="paymentType"]').find("option:selected").text();
	var partoptName = $('select[name="particulars"]').find("option:selected").text();
	var specificsId = $('select[name="specifics"]').val();
	var specoptName = $('select[name="specifics"]').find("option:selected").text();
	var vendorId = $('select[name="vendor"]').val();
	var userVendorNewEntry=$("#newVendUserSide").val();
	if(vendorId==null && userVendorNewEntry==""){
		swal("Invalid data field!!","Please provide vendor for the transaction.","error");
		return true;
	}
	var venoptName = $('select[name="vendor"]').find("option:selected").text();
	var unitCost = $("#unitcost").val();
	var noOfItems = $('#noofitems').val();
	var totalCost = $('#tamount').val();
	var remark = $('#remark').val();
	var fileurl = $('#docuploadurl').val();
	var hiddenId = $('#hiddenId').val();
	if (noOfItems == "") {
		swal("Invalid data field!!","Please provide number of items","error")
		return true;
	}
	$("#createExpenses").attr("class", "linkcolor");
	$("#approvalSubmit").attr("class", "linklist");
	$("#expenseFormCreate").hide();
	$("#expenseFormShow").show();
	$("#statictransactionPurpose").append('<option value="' + txnPurpose + '">' + txnPurposeText + '</option>');
	$("#staticpaymentType").append('<option value="' + txnPurchaseType + '">' + txnPurchaseTypeText + '</option>');
	$("#specstatic").append('<option value="' + specificsId + '">' + specoptName + '</option>');
	$("#venstatic").append('<option value="' + vendorId + '">' + venoptName + '</option>');
	$("#newstaticVendUserSide").val(userVendorNewEntry);
	$("#statichiddenId").val(hiddenId);
	$("#unitcoststatic").val(unitCost);
	$("#noofitemsstatic").val(noOfItems);
	$("#tamountstatic").val(totalCost);
	$("#docuploadurlstatic").attr("disabled","disabled");
	$("#submitApprovalButton").show();
	$("#submitAccountingButton").show();
	$("#additionalUserDiv").hide();
	$("#additionalApproval").hide();
	$("#compcancelButton").show();
	$("#accountingcancelButton").hide();
	if(fileurl!=""){
		$("#docuploadurlstatic").val(fileurl);
	}else{
		$("#docuploadurlstatic").val("");
	}
	$("#remarkstatic").val(remark);
}

function sendMail(elem){
	var expenseId=$(elem).attr('id');
	var jsonData = {};
	jsonData.expense = expenseId;
	var url = "/expenses/sendMail";
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
			window.location.reload(true);
			alwaysScrollTop();
		},
		error : function(xhr, errorMessage, error) {
			if(xhr.status == 401){
				doLogout();
			}
		}
	});
}

function reload(){
	window.location.reload(true);
}

function cancelApproval(){
	$("#expenseFormCreate").show();
	$("#expenseFormShow").hide();
	$("#createExpenses").attr("class","linklist");
	$("#approvalSubmit").attr("class","linkcolor");
	$('#docuploadurl').val("");
}


$(document).ready(function(){
	$('.deleteExpenseButton').click(function(){
		var check_box_values = $('input[name="checkExpenses"]:checkbox:checked').map(function () {
		    return this.value;
		}).get();
		if(check_box_values.length==0){
			swal("Invalid data field!!","Please check the expenses you want to delete","error");
			return true;
		}
		var val="";
		for(var i=0;i<check_box_values.length;i++){
			val+=check_box_values[i]+",";
		}
		var jsonData = {};
		jsonData.expenseIds=val.substring(0, val.length-1);;
		var url = "/expenses/deleteExpense";
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
				window.location.reload(true);
				alwaysScrollTop();
			},
			error : function(xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});

$(document).ready(function(){
	$('#particularsNewId').click(function(){
		$("#newParticularName").val("");
		$("#newParticularLabel").html("");
		var particularsId=$('select[name="particulars"]').val();
		$("#newParticularLabel").append("Create Particulars");
		$('#particularsNewId').attr('data-toggle', 'model');
		$('#newParticularModal').modal('show');
		$(".newParticularSubmitButton").attr('id', particularsId);
	});
});

$(document).ready(function(){
	$('#specificsNewId').click(function(){
		$("#newSpecificsName").val("");
		var specificsId=$('select[name="specifics"]').val();
		$("#newSpecificsLabel").html("");
		$('#newSpecifics').children().remove();
		$("#newSpecificsLabel").append("Create Specifics");
		$('#specificsNewId').attr('data-toggle', 'model');
		$('#newSpecificsModal').modal('show');
		$(".newSpecificsSubmitButton").attr('id', specificsId);
		$.get('/expenses/particulars',function(data) {
			 var sel = $("#newSpecifics");
			 for (var i=0;i<data.particularsData.length;i++) {
				 sel.append('<option value="'+data.particularsData[i].id+'">' +data.particularsData[i].name + '</option>');
			 }
			 alwaysScrollTop();
		})
	});
});

$(document).ready(function(){
	$('#vendorNewId').click(function(){
		$("#newVendorName").val("");
		$("#unitPriceVendor").val("");
		var specificsId=$('select[name="specifics"]').val();
		$("#newVendorLabel").html("");
		$('#newVendor').children().remove();
		$("#newVendorLabel").append("Create Vendors");
		$('#vendorNewId').attr('data-toggle', 'model');
		$('#newVendorModal').modal('show');
		$(".newVendorSubmitButton").attr('id', specificsId);
		$.get('/expenses/allspecifics',function(data) {
			 var sel = $("#newVendor");
			 for (var i=0;i<data.specificallData.length;i++) {
				 sel.append('<option value="'+data.specificallData[i].id+'">' +data.specificallData[i].name + '</option>');
			 }
			 alwaysScrollTop();
		})
	});
});

function logon(elem){
	var entityId=$(elem).attr('id');
	var role=$("#roles").val();
	var jsonData = {};
	jsonData.userId=entityId;
	jsonData.roleId=role;
	var url = "/users/roles";
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
			window.location.href="/expenseslist";
			alwaysScrollTop();
		},
		error : function(xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

$(document).ready(function(){
	$('select[name="roles"]').change(function(){
		 var role=this.value;
		 var jsonData = {};
		 jsonData.roleid = role;
		 var url = "/users/changeRole";
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
				window.location.href="/expenseslist";
				alwaysScrollTop();
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		 });
	 });
});

function changeCashCredit(elem){
	var val=$(elem).val();
	var trRowId=$(elem).parent().parent("tr:first").attr('id');
	if(val==2 || val==3){
		$("#"+trRowId+" b[id='noOfDaysText']").show();
		$("#"+trRowId+" input[id='noOfDaysForCredit']").show()
	}else{
		$("#"+trRowId+" b[id='noOfDaysText']").hide();
		$("#"+trRowId+" input[id='noOfDaysForCredit']").hide();
	}
}

$(document).ready(function(){
	$('#addOrgBtn'). click(function(){
		var orgName=$("#orgnName").val();
		var orgList=$('#orgLabellist ul.label-list').children().length;
		if(orgName==""){
			swal("Invalid data field!!","Please enter the organization name.","error");
			return true;
		}
		if(orgList>0){
			swal("Invalid data field!!","Already Organization Is Configured.Cannot Create A new Organization.","error");
			$("#orgnName").val("");
			return true;
		}
		var jsonData = {};
		jsonData.org = orgName;
		var url = "/config/addOrganization";
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
				var orgId=data.organizationData[0].id;
				var orgName=data.organizationData[0].name;
				var sel=$("#orgLabel");
				$("#orgnName").val("");
				sel.append('<li id="org'+orgId+'"><a id="org'+orgId+'" title="Organization Name" href="#" class="filter-item color-label labelstyle-fc2929" onClick="changeLabel(this);"><input style="display:none" type="checkbox" id="'+orgId+'" value="'+orgId+'"><span class="color"></span><span>'+orgName+'</span><img id="'+orgId+'" src="/assets/images/cross_icon1.gif" onClick="removeOrg(this)"></img></a></li>');
				alwaysScrollTop();
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		 });
	 });
});

$(document).ready(function(){
	$('#addRoleBtn'). click(function(){
		var roleName=$("#roleName").val();
		if(roleName==""){
			swal("Invalid data field!!","Please enter the Role name.","error");
			return true;
		}
		var jsonData = {};
		jsonData.role = roleName;
		$('#addRoleBtn').html("Adding Role ...");
		var url = "/config/addRole";
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
                $("#roleLabel").html("");
				var roleId=data.roleData[0].id;
				var roleName=data.roleData[0].name;
				var sel=$("#roleLabel");
				$("#roleName").val("");
				sel.append('<li id="role'+roleId+'"><a id="role'+roleId+'" title="Role Label" href="#" class="filter-item color-label labelstyle-fc2929" onClick="changeLabel(this);"><input style="display:none" type="checkbox" id="'+roleId+'" value="'+roleId+'"><span class="color" style="background-color: #4183c4"></span><span>'+roleName+'</span><img id="'+roleId+'" src="/assets/images/cross_icon1.gif" onClick="removeRole(this)"></img></a></li>');
				var userrolesel=$("#userRole");
				userrolesel.append('<option value="'+roleId+'">' +roleName+ '</option>');
				$('.multipleDropdown').multiselect('rebuild');
				$('#addRoleBtn').html("Add");
				alwaysScrollTop();
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		 });
	 });
});

function addVendorGroup(){
	var vendorGroupId = $("#vendorGroupEntityHiddenId").val();
	var vendorGroupName=$("#vendGroupName").val();
	var vendorGroupKL=$("#vendGroupKL").val();
	if(vendorGroupName==""){
    	swal("Invalid data field!!","Please enter vendor group name","error");
    	return true;
    }
	var jsonData = {};
	jsonData.useremail =$("#hiddenuseremail").text();
	if(vendorGroupId!=""){
		jsonData.vendGrpId=vendorGroupId;
	}
    jsonData.vendGroupName=vendorGroupName;
    jsonData.vendGroupKl=vendorGroupKL;
    var url = "/vendor/addVendorGroup";
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
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
			if(data.role.includes("MASTER ADMIN") || data.canCreateCustomer == true || data.canActivateCustomer == true || data.canCreateVendor == true || data.canActivateVendor == true){
				var entityType=data.entityType;
				if(entityType=="vendorCustomerGroup"){
					var vendGroupId=data.id;
					var vendGroupName=data.groupname;
					var type=data.type;
					if(type==1){
						$("#vendGroupName").val("");
						$("#vendGroupKL").val("");
						var existingVendorGroup=$("#vendorGroupDetailsListTable").find('tr[name="vendorGroupEntity'+data.id+'"]').attr('name');
						if(typeof(existingVendorGroup)!='undefined'){
							$("#vendorGroupDetailsListTable").find('tr[name="vendorGroupEntity'+data.id+'"]').html("");
							$("#vendorGroupDetailsListTable").find('tr[name="vendorGroupEntity'+data.id+'"]').append('<td>'+vendGroupName+'</td><td><button class="btn btn-submit" onClick="showVendorGroupEntityDetails(this)" id="show-entity-details'+data.id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td>');
						}
						if(typeof(existingVendorGroup)=='undefined'){
							$("#vendorGroupDetailsListTable").append('<tr id="vendorGroupEntity'+data.id+'" name="vendorGroupEntity'+data.id+'"><td>'+vendGroupName+'</td><td><button class="btn btn-submit"  onClick="showVendorGroupEntityDetails(this)" id="show-entity-details'+data.id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td></tr>');
						}
					}
				} 
				$.unblockUI();
				$('.notify-success').show();
				alwaysScrollTop();
			}
			if(data.role.includes("CONTROLLER")){
				getDashboardFinancials();
			}
			$("#vendorGroupEntityHiddenId").val("");
			$("#vendGroupName").val("");
			$("#vendGroupKL").val("");
			populatevendorgrouplist();
			$('#vendorgroup-form-container').hide();
            $("#notificationMessage").html("Vendor Group has been added/Updated successfully.");
      },
      error : function(xhr, status, error) {
		  if(xhr.status == 401){ doLogout(); }
      }
   });
}

$(document).ready(function() {
    $("#addVendGroupBtn").click(function() {
        addVendorGroup();
    });
});

function addCustomerGroup(){
	var customerGroupId = $("#customerGroupEntityHiddenId").val();
	var customerGroupName=$("#custGroupName").val();
	var customerGroupKL=$("#custGroupKL").val();
	if(customerGroupName==""){
    	swal("Invalid data field!!","Please enter customer group name","error");
    	return true;
    }
	var jsonData = {};
	jsonData.useremail =$("#hiddenuseremail").text();
	if(customerGroupId!=""){
		jsonData.custGrpId=customerGroupId;
	}
    jsonData.custGroupName=customerGroupName;
    jsonData.custGroupKl=customerGroupKL;
    var url = "/customer/addCustomerGroup";
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
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
			if(data.role.includes("MASTER ADMIN") || data.canCreateCustomer == true || data.canActivateCustomer == true || data.canCreateVendor == true || data.canActivateVendor == true){
				var entityType=data.entityType;
				if(entityType=="vendorCustomerGroup"){
					var vendGroupId=data.id;
					var vendGroupName=data.groupname;
					var type=data.type;
					if(type==2){
						$("#custGroupName").val("");
						$("#custGroupKL").val("");
						var existingCustomerGroup=$("#customerGroupDetailsListTable").find('tr[name="customerGroupEntity'+data.id+'"]').attr('name');
						if(typeof(existingCustomerGroup)!='undefined'){
							$("#customerGroupDetailsListTable").find('tr[name="customerGroupEntity'+data.id+'"]').html("");
							$("#customerGroupDetailsListTable").find('tr[name="customerGroupEntity'+data.id+'"]').append('<td>'+vendGroupName+'</td><td><button class="btn btn-submit" onClick="showCustomerGroupEntityDetails(this)" id="show-entity-details'+data.id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td>');
						}
						if(typeof(existingCustomerGroup)=='undefined'){
							$("#customerGroupDetailsListTable").append('<tr id="customerGroupEntity'+data.id+'" name="customerGroupEntity'+data.id+'"><td>'+vendGroupName+'</td><td><button class="btn btn-submit" onClick="showCustomerGroupEntityDetails(this)" id="show-entity-details'+data.id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td></tr>');
						}
					}
				} 
				$.unblockUI();
				$('.notify-success').show();
				alwaysScrollTop();
			}
			if(data.role.includes("CONTROLLER")){
				getDashboardFinancials();
			}
			$("#customerGroupEntityHiddenId").val("");
			$("#custGroupName").val("");
			$("#custGroupKL").val("");
			populatecustomergrouplist();
			$('#customergroup-form-container').hide();
            $("#notificationMessage").html("Customer Group has been added/Updated successfully.");
      },
      error : function(xhr, status, error) {
		  if(xhr.status == 401){ doLogout(); }
      }
   });
}

$(document).ready(function() {
    $("#addCustGroupBtn").click(function() {
        addCustomerGroup();
    });
});

function checkForDocumentUploadRulePVS(txnInvoice,paymentReceived){
	if(txnInvoice == "" || paymentReceived == ""){
		swal("Mandatory data is not provided!", "Please provide complete data for transaction.", "error");
		enableTransactionButtons();
		return  false;
	}
	var jsonData = {};
	jsonData.useremail =$("#hiddenuseremail").text();
	jsonData.txnInv=txnInvoice;
	jsonData.txnpaymentReceived=paymentReceived;
	var url="/transaction/checkForDocumentUploadingRulePVS";
	var bool;
	$.ajax({
		url: url,
		data:JSON.stringify(jsonData),
		type:"text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method:"POST",
		async:false,
		contentType:'application/json',
		success: function (data) {
			var ruleMessage=data.documentRuleMessage[0].ruleMessage;
			if(ruleMessage=="Required"){
				bool= true;
			}
			if(ruleMessage=="Not Required"){
				bool= false;
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
	return bool;
}
/*function handleCheckboxClick(clickedCheckboxId) {
    switch (clickedCheckboxId) {
        case 'goodsCheckbox1':
            uncheckCheckboxes(['servicesCheckbox1', 'servicesCheckbox2']);
            disableCheckbox('servicesCheckbox2');
			console.log(document.getElementById('goodsCheckbox1').checked);
			if (!document.getElementById('goodsCheckbox1').checked) {
                let check = document.getElementById('servicesCheckbox2');
				check.disabled = false;
				if(document.getElementById('goodsCheckbox2').checked){
					let uncheckCheck = document.getElementById('goodsCheckbox2');
				    uncheckCheck.checked = false;
				}
            }
			if (document.getElementById('goodsCheckbox2').disabled) {
                let check = document.getElementById('goodsCheckbox2');
				check.disabled = false;
            }
            break;
        case 'goodsCheckbox2':
            uncheckCheckboxes(['servicesCheckbox1', 'servicesCheckbox2']);
            if (!document.getElementById('goodsCheckbox1').checked) {
                checkCheckbox('goodsCheckbox1');
            }
            break;
		case 'servicesCheckbox1':
            uncheckCheckboxes(['goodsCheckbox1', 'goodsCheckbox2']);
			disableCheckbox('goodsCheckbox2');
            if (!document.getElementById('servicesCheckbox1').checked) {
                let check = document.getElementById('goodsCheckbox2');
				check.disabled = false;
				if(document.getElementById('servicesCheckbox2').checked){
					let uncheckCheck = document.getElementById('servicesCheckbox2');
				    uncheckCheck.checked = false;
				}
            }
			if (document.getElementById('servicesCheckbox2').disabled) {
                let check = document.getElementById('servicesCheckbox2');
				check.disabled = false;
            }
            break;
		case 'servicesCheckbox2':
            uncheckCheckboxes(['goodsCheckbox1', 'goodsCheckbox2']);
            if (!document.getElementById('servicesCheckbox1').checked) {
                checkCheckbox('servicesCheckbox1');
            }
            break;
    }
}

function uncheckCheckboxes(checkboxIds) {
    checkboxIds.forEach(checkboxId => {
        let checkbox = document.getElementById(checkboxId);
        checkbox.checked = false;
    });
}

function disableCheckbox(checkboxId) {
    let checkbox = document.getElementById(checkboxId);
    checkbox.disabled = true;
    checkbox.checked = false;
}

function checkCheckbox(checkboxId) {
    let checkbox = document.getElementById(checkboxId);
    checkbox.checked = true;
}*/

function checkForDocumentUploadRule(txnForItem,txnForBranch,txnNetAmount){
	var jsonData = {};
	jsonData.useremail =$("#hiddenuseremail").text();
	jsonData.txnForExpItem=txnForItem;
	jsonData.txnForExpBranch=txnForBranch;
	jsonData.txnForExpNetAmount=txnNetAmount;
	var url="/transaction/checkForDocumentUploadingRule";
	var bool;
	$.ajax({
		url: url,
		data:JSON.stringify(jsonData),
		type:"text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method:"POST",
		async:false,
		contentType:'application/json',
		success: function (data) {
			var ruleMessage=data.documentRuleMessage[0].ruleMessage;
			if(ruleMessage=="Required"){
				bool= true;
			}
			if(ruleMessage=="Not Required"){
				bool= false;
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
	return bool;
}

function performAction(elem) {
	  var optName = $(elem).text();
	  var expenseId=$(elem).attr('id');
	  var jsonData = {};
	  if(optName == 'Approve Position'){
		  var jsonData = {};
		  jsonData.useremail =$("#hiddenuseremail").text();
		  jsonData.hiringRequestId=expenseId;
		  jsonData.status="position_approved";
		  var remarks = $('#hiringRemarks_' + expenseId).val();
		  var document = $('#hiringDocuments_' + expenseId).val();
		  if (!isEmpty(remarks)) {
			  jsonData.remarks = $("#hiddenuseremail").text() + '#' + remarks;
		  }
		  if (!isEmpty(document)) {
			  jsonData.document = $("#hiddenuseremail").text() + '#' + document;
		  }
		  var url = "/labour/action";
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
			error : function(xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		 });
	  }
	  if(optName == 'Submit Employee Details'){
		  var jsonData = {};
		  jsonData.useremail =$("#hiddenuseremail").text();
		  jsonData.hiringRequestId=expenseId;
		  var url = "/labour/employeeDetails";
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
				  $('#hiringDiv').remove();
				  if(data.job.availableProjects!=null && data.job.availableProjects!=""){
					  $("#availableProjects").children().remove();
					  $("#availableProjects").append('<option value="'+data.job.availableProjectsValue+'">'+data.job.availableProjects+'</option>');
				  }
				  if(!isEmpty(data.job.agreement)){
					  $('#lhAgreementDoc').html(data.job.agreement);
					  $('#lhAgreementDoc').attr('onclick','downloadfile("'+data.job.agreement+'")');
					  $('#lhAgreement').fadeIn();
				  }else{
					  $('#lhAgreementDoc').html('').removeAttr('onclick');
					  $('#lhAgreement').hide();
				  }
				  $("#hiddenRequestLabourId").val(data.hiringLabourPrimKey);
				  $("#projectNumber").val(data.project.project_number);
				  $("#projectTitle").val(data.project.project_name);
				  $("#jobDescription1").val(data.job.jobDescription);
				  $("#start-date").val(data.job.jobStartDate);
				  $("#end-date").val(data.job.jobEndDate);
				  if(data.job.jobPosition!=null && data.job.jobPosition!=""){
					  $("#positionName").children().remove();
					  $("#positionName").append('<option value="'+data.job.jobPositionValue+'">'+data.job.jobPosition+'</option>');
				  }
				  if(data.job.requestAllowedForBranches!=null && data.job.requestAllowedForBranches!=""){
					  $("#requestAllowedForBranches").children().remove();
					  $("#requestAllowedForBranches").append('<option value="'+data.job.requestAllowedForBranches+'">'+data.job.requestAllowedForBranches+'</option>');
				  }
				  $("#positionQualification").val(data.job.positionQualification);
				  $("#experienceLevel").val(data.job.jobExperience);
				  if(data.job.jobReportsTo!=null && data.job.jobReportsTo!=""){
					  $("#reportsTo").children().remove();
					  $("#reportsTo").append('<option value="'+data.job.jobReportsTo+'">'+data.job.jobReportsTo+'</option>');
				  }
				  $("#advertisingDate").val(data.job_ad.dateOfAd);
				  $("#advertisingPlace").val(data.job_ad.placeOfAd);
				  var jobDetailsLangProf=data.job.jobDetailsLangProf;
				  $(".jobDetailsLanguages").html("");
				  if(typeof jobDetailsLangProf!='undefined' && jobDetailsLangProf!=""){
					  var creationRigthInBranchId=jobDetailsLangProf.split('@');
					  for(var k=0;k<creationRigthInBranchId.length;k++){
						  $(".jobDetailsLanguages").append('<div><b class="individualLangProf">'+creationRigthInBranchId[k]+'</b></div>');
					  }
				  }
				  //Apppend additional details to the creator template
				  $('#advertisingDiv').append('<div id = "hiringDiv" class="box-no-border">');
				  $('#hiringDiv').append('<div class="cell-50-left-ar clear-both" style="width:710px;"><b>Employee Details</b></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar clear-both">First'+
					' Name<input type="text" id = "employeeName"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar " style="margin-left:165px;margin-right:0px;">'+
					'Middle Name<input type="text" id = "middleName"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar clear-both">Last'+
					' Name<input type="text" id = "lastName"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar " style="margin-left:165px;margin-right:0px;">'+
					'Employee Address<input type="text" id = "employeeAddress"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar clear-both">Employee Salary'+
					'<input type="text" id = "employeeSalary"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar" style="margin-left:165px;margin-right:0px;">Contact No.'+
					'<input type="text" id = "contactNo"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar  clear-both">'+
					'Duration Of Contract<input type="text" id = "duration"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar" style="margin-left:165px;margin-right:0px;">Citizenship'+
					'<input type="text" id = "citizenship"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar clear-both">Place Of Birth'+
					'<input type="text" id = "placeOfBirth"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar" style="margin-left:165px;margin-right:0px;">Pan Card Number'+
					'<input type="text" id = "panCard"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar clear-both">Passport Number'+
					'<input type="text" id = "passport"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar clear-both" style="width:825px;">'+
					'<b>Authorized To Work In Country</b></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar  clear-both">'+
					'Country Name<input type="text" id = "countryName1"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar" style="margin-left:-170px;margin-right:0px;">Visa Number'+
					'<input type="text" id = "visaNumber1"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar" style="margin-left:-80px;margin-right:0px;">Visa Type'+
					'<input type="text" id = "visaType1"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar  clear-both">'+
					'Country Name<input type="text" id = "countryName2"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar" style="margin-left:-170px;margin-right:0px;">Visa Number'+
					'<input type="text" id = "visaNumber2"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar" style="margin-left:-80px;margin-right:0px;">Visa Type'+
					'<input type="text" id = "visaType2"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar  clear-both">'+
					'Country Name<input type="text" id = "countryName3"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar" style="margin-left:-170px;margin-right:0px;">Visa Number'+
					'<input type="text" id = "visaNumber3"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar" style="margin-left:-80px;margin-right:0px;">Visa Type'+
					'<input type="text" id = "visaType3"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar clear-both" style="width:1030px;">'+
					'<b>Name, Ages and Relationship of dependents to accompany individual to country of assignment</b></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar clear-both">'+
					'Name <input type="text" id = "dependents"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar" style="margin-left:-170px;margin-right:0px;">Age <input type="text" id = "dependentsAge"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar" style="margin-left:-80px;margin-right:0px;"> Relationship <input type="text" id = "dependentsRelationship"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar clear-both">'+
					'Name <input type="text" id = "dependents1"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar" style="margin-left:-170px;margin-right:0px;">Age <input type="text" id = "dependentsAge1"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar" style="margin-left:-80px;margin-right:0px;"> Relationship <input type="text" id = "dependentsRelationship1"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar clear-both">'+
					'Name <input type="text" id = "dependents2"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar" style="margin-left:-170px;margin-right:0px;">Age <input type="text" id = "dependentsAge2"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar" style="margin-left:-80px;margin-right:0px;"> Relationship <input type="text" id = "dependentsRelationship2"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar clear-both" style="margin-left:330px;"><h4>Education</h4></div>');
				  //$('#hiringDic').append('</div><div class="container clear-both"');
				  $('#hiringDiv').append('<div class="span12" style="margin-left:240px;"><div class="span3">Name</br>'+
					'<input style="width:200px" type="text" id = "institute1"></br>'+
					'<input style="width:200px" type="text" id = "institute2"></br>'+
					'<input style="width:200px" type="text" id = "institute3"></div>'+
					'<div class="span3">Major</br><input style="width:150px" type="text" id = "major1">'+
					'</br><input style="width:150px" type="text" id = "major2"></br>'+
					'<input style="width:150px" type="text" id = "major3"></div>'+
					'<div class="span2">Degree</br><input style="width:150px" type="text" id = "degree1">'+
					'</br><input style="width:150px" type="text" id = "degree2"></br>'+
					'<input style="width:150px" type="text" id = "degree3"></div><div class="span2">'+
					'Date</br><input style="width:185px" type="text" id = "date1" class="labdatepicker"></br>'+
					'<input style="width:185px" type="text" id = "date2" class="labdatepicker"></br>'+
					'<input style="width:185px" type="text" id = "date3" class="labdatepicker"></div></div><br/>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar clear-both" style="margin-left:385px;"><h4>Professional Certifications</h4></div>');
				  $('#hiringDiv').append('<div class="span12" style="margin-left:265px;"><div class="span3">Name</br>'+
					'<input style="width:200px" type="text" id = "certification1"></br>'+
					'<input style="width:200px" type="text" id = "certification2"></br>'+
					'<input style="width:200px" type="text" id = "certification3"></div>'+
					'<div class="span3">Number</br><input style="width:150px" type="text" id = "certificationNumber1">'+
					'</br><input style="width:150px" type="text" id = "certificationNumber2"></br>'+
					'<input style="width:150px" type="text" id = "certificationNumber3"></div>'+
					'<div class="span2">Technology</br><input style="width:150px" type="text" id = "technology1">'+
					'</br><input style="width:150px" type="text" id = "technology2"></br>'+
					'<input style="width:150px" type="text" id = "technology3"></div><div class="span2">'+
					'Date</br><input style="width:215px" type="text" id = "certificationdate1" class="labdatepicker"></br>'+
					'<input style="width:215px" type="text" id = "certificationdate2" class="labdatepicker"></br>'+
					'<input style="width:215px" type="text" id = "certificationdate3" class="labdatepicker"></div></div><br/>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar clear-both" style="margin-left:355px;"><h4>'+
					'Language Proficiency</h4s></div>');
				  $('#hiringDiv').append('<div class="span12" style="margin-left:265px;"><div class="span3">Language'+
					'<br/><input type="text" style="width:200px" id = "language1"></br><input type="text" style="width:200px" id = "language2">'+
					'</br><input type="text" style="width:200px" id = "language3"></div><div class="span3">'+
					'Proficiency Speaking<input type="text" style="width:150px" id = "proficiencySpeaking1">'+
					'</br><input type="text" style="width:150px" id = "proficiencySpeaking2"></br>'+
					'<input type="text" style="width:150px" id = "proficiencySpeaking3"></div><div class="span2">'+
					'Proficiency Reading<input type="text" style="width:150px" id = "proficiencyReading1"></br>'+
					'<input type="text" style="width:150px" id = "proficiencyReading2"></br>'+
					'<input type="text" style="width:150px" id = "proficiencyReading3"></div><div class="span2">'+
					'Proficiency Writing<input type="text" style="width:215px" id = "proficiencyWriting1"></br>'+
					'<input type="text" style="width:215px" id = "proficiencyWriting2"></br>'+
					'<input type="text" style="width:215px" id = "proficiencyWriting3"></div></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar clear-both" style="margin-left:365px;"><h4><strong>'+
					'Employment History</strong></h4></div>');
				  $('#hiringDiv').append('<div class="span12" style="margin-left:265px;"><div class="span3"><strong>Position Title'+
					'</strong></br></br><input style="width:200px" type="text" id = "positionTitle1">'+
					'</br><input style="width:200px" type="text" id = "positionTitle2"></br>'+
					'<input style="width:200px" type="text" id = "positionTitle3"></br>'+
					'<input style="width:200px" type="text" id = "positionTitle4"></br>'+
					'<input style="width:200px" type="text" id = "positionTitle5"></div><div class="span3">'+
					'<strong>Name And Address Of Employer</strong></br></br>'+
					'<input type="text" style="width:150px" id = "employer1"></br>'+
					'<input style="width:150px" type="text" id = "employer2"></br>'+
					'<input style="width:150px" type="text" id = "employer3"><br/>'+
					'<input style="width:150px" type="text" id = "employer4"><br/>'+
					'<input style="width:150px" type="text" id = "employer5"></div>'+
					'<div class="span3"><strong style="margin-left:30px;">Dates Of Employment</strong></br>'+
					'<div class="span">From</br><input style="width:70px" type="text" id = "from1">'+
					'</br><input style="width:70px" type="text" id = "from2"></br>'+
					'<input style="width:70px" type="text" id = "from3"><br/><input style="width:70px" type="text" id = "from4"><br/>'+
					'<input style="width:70px" type="text" id = "from5"></div><div class="span">To</br>'+
					'<input style="width:70px" type="text" id = "to1"></br>'+
					'<input style="width:70px" type="text" id = "to2"></br>'+
					'<input style="width:70px" type="text" id = "to3"></br>'+
					'<input style="width:70px" type="text" id = "to4"></br>'+
					'<input style="width:70px"type="text" id = "to5"></div></div>'+
					'<div class="span2"><strong>Annual Salary</strong></br></br>'+
					'<input type="text" style="width:150px" id = "salary1"></br>'+
					'<input type="text" style="width:150px" id = "salary2"></br>'+
					'<input type="text" style="width:150px" id = "salary3"></br>'+
					'<input type="text" style="width:150px" id = "salary4"></br>'+
					'<input type="text" style="width:150px" id = "salary5"></div></div><div class="cell-50-left-ar"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar clear-both" style="margin-left:365px;width:40%;"><h4><strong>'+
					'Professional Reference (Apart from current employer)</strong></h4></div>');
				  $('#hiringDiv').append('<div class="span12" style="margin-left:265px;"><div class="span3"><strong>Name'+
					'</strong></br></br><input style="width:200px" type="text" id = "prName1">'+
					'</br><input style="width:200px" type="text" id = "prName2"></br>'+
					'<input style="width:200px" type="text" id = "prName3"></br>'+
					'</div><div class="span3"><strong>Employer Detail</strong></br></br>'+
					'<input type="text" style="width:150px" id = "prEmployer1"></br>'+
					'<input style="width:150px" type="text" id = "prEmployer2"></br>'+
					'<input style="width:150px" type="text" id = "prEmployer3"><br/></div>'+
					'<div class="span3"><strong style="margin-left:30px;">Email</strong><br><br>'+
					'<input style="width:215px" type="text" id = "prEmail1">'+
					'</br><input style="width:215px" type="text" id = "prEmail2"></br>'+
					'<input style="width:215px" type="text" id = "prEmail3"><br/></div>'+
					'<div class="span2"><strong>Phone</strong></br></br>'+
					'<input type="text" style="width:150px" id = "prPhone1"></br>'+
					'<input type="text" style="width:150px" id = "prPhone2"></br>'+
					'<input type="text" style="width:150px" id = "prPhone3"></br>'+
					'</div></div><div class="cell-50-left-ar"></div>');
				  $('#hiringDiv').append('<div class="cell-50-left-ar clear-both" style="margin-left:365px;"><h4><strong>'+
					'Consulting Services</strong></h4></div>');
				  $('#hiringDiv').append('<div class="span12" style="margin-left:265px;"><div class="span3"><strong>Consulting Service Name'+
							'</strong></br></br><input style="width:200px" type="text" id = "consultingServiceName1">'+
							'</br><input style="width:200px" type="text" id = "consultingServiceName2"></br>'+
							'<input style="width:200px" type="text" id = "consultingServiceName3"></br>'+
							'<input style="width:200px" type="text" id = "consultingServiceName4"></br>'+
							'<input style="width:200px" type="text" id = "consultingServiceName5"></div><div class="span3">'+
							'<strong>Employer Details</strong></br></br>'+
							'<input type="text" style="width:150px;" id = "consultingServiceEmployer1"></br>'+
							'<input style="width:150px" type="text" id = "consultingServiceEmployer2"></br>'+
							'<input style="width:150px" type="text" id = "consultingServiceEmployer3"><br/>'+
							'<input style="width:150px" type="text" id = "consultingServiceEmployer4"><br/>'+
							'<input style="width:150px" type="text" id = "consultingServiceEmployer5"></div>'+
							'<div class="span3" style="width: 150px;margin-left: -45px;"><strong style="margin-left:30px;">Date</strong></br>'+
							'</br><input style="width:70px" type="text" id = "consultingServicefrom1">'+
							'</br><input style="width:70px" type="text" id = "consultingServicefrom2"></br>'+
							'<input style="width:70px" type="text" id = "consultingServicefrom3"><br/><input style="width:70px" type="text" id = "consultingServicefrom4"><br/>'+
							'<input style="width:70px" type="text" id = "consultingServicefrom5"></div>'+
							'<div class="span2" style="margin-left: -40px;"><strong>No. Of Hours</strong></br></br>'+
							'<input type="text" style="width:120px" id = "consultingServiceHours1"></br>'+
							'<input type="text" style="width:120px" id = "consultingServiceHours2"></br>'+
							'<input type="text" style="width:120px" id = "consultingServiceHours3"></br>'+
							'<input type="text" style="width:120px" id = "consultingServiceHours4"></br>'+
							'<input type="text" style="width:120px" id = "consultingServiceHours5"></div>'+
							'<div class="span2" style="margin-left: 25px;"><strong>Rate/Hour</strong></br></br>'+
							'<input type="text" style="width:150px" id = "consultingServiceRate1"></br>'+
							'<input type="text" style="width:150px" id = "consultingServiceRate2"></br>'+
							'<input type="text" style="width:150px" id = "consultingServiceRate3"></br>'+
							'<input type="text" style="width:150px" id = "consultingServiceRate4"></br>'+
							'<input type="text" style="width:150px" id = "consultingServiceRate5"></div></div><div class="cell-50-left-ar"></div>');
				  $('#hiringDiv').last().append($('<div class="cell-50-left-ar clear-both" style="margin-left:70px;">Resume'+
					'<input type="filepicker" id = "docuploadurl" name="fileName"><input type="button" id="fileName" value="Upload" onclick="uploadFile(this.id)">&nbsp;</div>'));
				  $('#hiringDiv').append('<div class="clear-both"></div> </div>');
				  $("#pendingHiring").hide();
				  $("#newHiring").show();
				  $(".submitHiringRequest").text("Submit Employee Details");
				  $(".submitHiringRequest").parent().css('margin-left', '770px');
				  $(".submitHiringRequest").removeAttr("disabled");
				  var maxYear=new Date().getFullYear()+30;
				  $(".labdatepicker" ).datepicker({
						 changeMonth : true,
						 changeYear: true,
						 dateFormat:  'yy',
						 yearRange: ''+new Date().getFullYear()-100+':'+maxYear+'',
						 onSelect: function(x,y){
							$(this).focus();
						 }
					});
			  },
			  error: function(xhr, status, error) {
				  if(xhr.status == 401){ doLogout(); }
			  }
		   });
	  }
	}

function submitHiringRequest(){
	 var projectDetails = {};
	 $(".submitHiringRequest").attr("disabled", "disabled");
	    if($("#projectTitle").val()==""){
			swal("Invalid data field!!","Please provide project title","error");
	      $("#projectTitle").focus();
	      alwaysScrollTop();
	      $(".submitHiringRequest").removeAttr("disabled");
	      return true;
	    }
	    if($("#projectNumber").val()==""){
			swal("Invalid data field!!","Please provide project Number","error");
	      $("#projectNumber").focus();
	      alwaysScrollTop();
	      $(".submitHiringRequest").removeAttr("disabled");
	      return true;
	    }
	    if($("#positionName").val()==""){
	    	swal("Invalid data field!!","Please choose a position for hiring request","error");
		      alwaysScrollTop();
		      $(".submitHiringRequest").removeAttr("disabled");
		      return true;
	    }
	    var jobDetailsLangProfStr="";
	    var jobDetailsLangProf = $('b[class="individualLangProf"]').map(function () {
	    	jobDetailsLangProfStr+=$(this).text()+":";
	 	}).get().toString();
	    projectDetails.project_number = $("#projectNumber").val();
	    projectDetails.project_name = $("#projectTitle").val();
	    projectDetails.request_type = $("#requestTypeId").val();
	    hiringRequest.useremail =$("#hiddenuseremail").text();
	    var jobDetails = {};
	    jobDetails.availableProjectsValue = $("#availableProjects").val();
	    jobDetails.agreement=$('#lhAgreementDoc').html();
	    jobDetails.availableProjects = $("#availableProjects option:selected").text();
	    jobDetails.jobDescription = $("#jobDescription").val();
	    jobDetails.jobStartDate = $("#start-date").val();
	    jobDetails.jobEndDate = $("#end-date").val();
	    jobDetails.requestAllowedForBranches=$("#requestAllowedForBranches").val();
	    jobDetails.jobPositionValue = $("#positionName").val();
	    jobDetails.positionQualification=$("#positionQualification").val();
	    jobDetails.jobPosition = $("#positionName option:selected").text();
	    jobDetails.jobExperience = $("#experienceLevel").val();
	    jobDetails.jobReportsTo = $("#reportsTo option:selected").text();
	    if(typeof jobDetailsLangProfStr!='undefined' && jobDetailsLangProfStr!=""){
	    	jobDetails.jobDetailsLangProf = jobDetailsLangProfStr;
	    }
	    var jobAd = {};
	    jobAd.dateOfAd = $("#advertisingDate").val();
	    jobAd.placeOfAd = $("#advertisingPlace").val();
	    hiringRequest.project = projectDetails;
	    hiringRequest.job_ad = jobAd;
	    var emp_name = $('#employeeName').val();
	    if (emp_name !== undefined) {
	      hiringRequest.status = "employee_identified"
	      var emp_details = {};
	      emp_details.emp_name = emp_name;
	      emp_details.middle_name = $('#middleName').val();
	      emp_details.last_name = $('#lastName').val();
	      emp_details.salary = $('#employeeSalary').val();
	      emp_details.employeeAddress = $('#employeeAddress').val();
	      emp_details.duration= $('#duration').val();
	      emp_details.placeOfBirth= $('#placeOfBirth').val();
	      emp_details.citizenship= $('#citizenship').val();
	      emp_details.contactNo= $('#contactNo').val();
	      emp_details.panCard=$("#panCard").val();
	      emp_details.passport=$("#passport").val();
	      emp_details.countryName1=$("#countryName1").val();
	      emp_details.countryName2=$("#countryName2").val();
	      emp_details.countryName3=$("#countryName3").val();
	      emp_details.visaNumber1=$("#visaNumber1").val();
	      emp_details.visaNumber2=$("#visaNumber2").val();
	      emp_details.visaNumber3=$("#visaNumber3").val();
	      emp_details.visaType1=$("#visaType1").val();
	      emp_details.visaType2=$("#visaType2").val();
	      emp_details.visaType3=$("#visaType3").val();
	      emp_details.dependents= $('#dependents').val();
	      emp_details.dependents1= $('#dependents1').val();
	      emp_details.dependents2= $('#dependents2').val();
	      emp_details.dependentsAge= $('#dependentsAge').val();
	      emp_details.dependentsAge1= $('#dependentsAge1').val();
	      emp_details.dependentsAge2= $('#dependentsAge2').val();
	      emp_details.dependentsRelationship= $('#dependentsRelationship').val();
	      emp_details.dependentsRelationship1= $('#dependentsRelationship1').val();
	      emp_details.dependentsRelationship2= $('#dependentsRelationship2').val();
	      emp_details.institute1= $('#institute1').val();
	      emp_details.institute2= $('#institute2').val();
	      emp_details.institute3= $('#institute3').val();
	      emp_details.major1= $('#major1').val();
	      emp_details.major2= $('#major2').val();
	      emp_details.major3= $('#major3').val();
	      emp_details.degree1= $('#degree1').val();
	      emp_details.degree2= $('#degree2').val();
	      emp_details.degree3= $('#degree3').val();
	      emp_details.date1= $('#date1').val();
	      emp_details.date2= $('#date2').val();
	      emp_details.date3= $('#date3').val();
	      emp_details.certification1=$('#certification1').val();
	      emp_details.certification2=$('#certification2').val();
	      emp_details.certification3=$('#certification3').val();
	      emp_details.certificationNumber1=$('#certificationNumber1').val();
	      emp_details.certificationNumber2=$('#certificationNumber2').val();
	      emp_details.certificationNumber3=$('#certificationNumber3').val();
	      emp_details.technology1=$('#technology1').val();
	      emp_details.technology2=$('#technology2').val();
	      emp_details.technology3=$('#technology3').val();
	      emp_details.certificationdate1=$('#certificationdate1').val();
	      emp_details.certificationdate2=$('#certificationdate2').val();
	      emp_details.certificationdate3=$('#certificationdate3').val();
	      emp_details.language1= $('#language1').val();
	      emp_details.language2= $('#language2').val();
	      emp_details.language3= $('#language3').val();
	      emp_details.proficiencySpeaking1= $('#proficiencySpeaking1').val();
	      emp_details.proficiencySpeaking2= $('#proficiencySpeaking2').val();
	      emp_details.proficiencySpeaking3= $('#proficiencySpeaking3').val();
	      emp_details.proficiencyReading1= $('#proficiencyReading1').val();
	      emp_details.proficiencyReading2= $('#proficiencyReading2').val();
	      emp_details.proficiencyReading3= $('#proficiencyReading3').val();
	      emp_details.proficiencyWriting1=$('#proficiencyWriting1').val();
	      emp_details.proficiencyWriting2=$('#proficiencyWriting2').val();
	      emp_details.proficiencyWriting3=$('#proficiencyWriting3').val();
	      emp_details.positionTitle1= $('#positionTitle1').val();
	      emp_details.positionTitle2= $('#positionTitle2').val();
	      emp_details.positionTitle3= $('#positionTitle3').val();
	      emp_details.positionTitle4= $('#positionTitle4').val();
	      emp_details.positionTitle5= $('#positionTitle5').val();

	      emp_details.prName1= $('#prName1').val();
	      emp_details.prName2= $('#prName2').val();
	      emp_details.prName3= $('#prName3').val();
	      emp_details.prEmployer1= $('#prEmployer1').val();
	      emp_details.prEmployer2= $('#prEmployer2').val();
	      emp_details.prEmployer3=$('#prEmployer3').val();
	      emp_details.prEmail1=$('#prEmail1').val();
	      emp_details.prEmail2=$('#prEmail2').val();
	      emp_details.prEmail3= $('#prEmail3').val();
	      emp_details.prPhone1= $('#prPhone1').val();
	      emp_details.prPhone2= $('#prPhone2').val();
	      emp_details.prPhone3= $('#prPhone3').val();

	      emp_details.employer1= $('#employer1').val();
	      emp_details.employer2= $('#employer2').val();
	      emp_details.employer3= $('#employer3').val();
	      emp_details.employer4= $('#employer4').val();
	      emp_details.employer5= $('#employer5').val();
	      emp_details.from1= $('#from1').val();
	      emp_details.from2= $('#from2').val();
	      emp_details.from3= $('#from3').val();
	      emp_details.from4= $('#from4').val();
	      emp_details.from5= $('#from5').val();
	      emp_details.to1= $('#to1').val();
	      emp_details.to2= $('#to2').val();
	      emp_details.to3= $('#to3').val();
	      emp_details.to4= $('#to4').val();
	      emp_details.to5= $('#to5').val();
	      emp_details.salary1= $('#salary1').val();
	      emp_details.salary2= $('#salary2').val();
	      emp_details.salary3= $('#salary3').val();
	      emp_details.salary4= $('#salary4').val();
	      emp_details.salary5= $('#salary5').val();
	      emp_details.consultingServiceName1= $('#consultingServiceName1').val();
	      emp_details.consultingServiceName2= $('#consultingServiceName2').val();
	      emp_details.consultingServiceName3= $('#consultingServiceName3').val();
	      emp_details.consultingServiceName4= $('#consultingServiceName4').val();
	      emp_details.consultingServiceName5= $('#consultingServiceName5').val();
	      emp_details.consultingServiceEmployer1= $('#consultingServiceEmployer1').val();
	      emp_details.consultingServiceEmployer2= $('#consultingServiceEmployer2').val();
	      emp_details.consultingServiceEmployer3= $('#consultingServiceEmployer3').val();
	      emp_details.consultingServiceEmployer4= $('#consultingServiceEmployer4').val();
	      emp_details.consultingServiceEmployer5= $('#consultingServiceEmployer5').val();
	      emp_details.consultingServicefrom1= $('#consultingServicefrom1').val();
	      emp_details.consultingServicefrom2= $('#consultingServicefrom2').val();
	      emp_details.consultingServicefrom3= $('#consultingServicefrom3').val();
	      emp_details.consultingServicefrom4= $('#consultingServicefrom4').val();
	      emp_details.consultingServicefrom5= $('#consultingServicefrom5').val();
	      emp_details.consultingServiceHours1= $('#consultingServiceHours1').val();
	      emp_details.consultingServiceHours2= $('#consultingServiceHours2').val();
	      emp_details.consultingServiceHours3= $('#consultingServiceHours3').val();
	      emp_details.consultingServiceHours4= $('#consultingServiceHours4').val();
	      emp_details.consultingServiceHours5= $('#consultingServiceHours5').val();
	      emp_details.consultingServiceRate1= $('#consultingServiceRate1').val();
	      emp_details.consultingServiceRate2= $('#consultingServiceRate2').val();
	      emp_details.consultingServiceRate3= $('#consultingServiceRate3').val();
	      emp_details.consultingServiceRate4= $('#consultingServiceRate4').val();
	      emp_details.consultingServiceRate5= $('#consultingServiceRate5').val();
	      emp_details.docuploadurl=$('#docuploadurl').val();
	      hiringRequest.empDetails = emp_details;
	      jobDetails.jobId = id;
	      hiringRequest.job = jobDetails;
	    } else {
	      id = getRandomArbitrary();
	      jobDetails.jobId = id;
	      hiringRequest.job = jobDetails;
	      hiringRequest.status = "position_requested"
	    }
	    hiringRequest.hiringLabourPrimKey=$("#hiddenRequestLabourId").val();
	    var url="/hiringRequest"
	    $.ajax({
	    url: url,
	      data:JSON.stringify(hiringRequest),
	      type:"text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
	      method:"POST",
	      contentType:'application/json',
	      success: function (data) {
			viewTransactionData(data); // to render the updated transaction recored
	      },
	      error: function (xhr, status, error) {
			  if(xhr.status == 401){ doLogout(); }
	      }
	     });
}

$(document).ready(function() {
	$(".submitHiringRequest").on('click',function(e) {
		e.preventDefault();
		submitHiringRequest();
	  });
});

function removeOrg(elem){
	var entityId=$(elem).attr('id');
	var jsonData = {};
	jsonData.orgId = entityId;
	var url = "/config/removeOrganization";
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
			$('li').remove('#org'+entityId+'');
			alwaysScrollTop();
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	 });
}

function removeBnch(elem){
	var entityId=$(elem).attr('id');
	var jsonData = {};
	jsonData.bnchId = entityId;
	var url = "/config/removeBranch";
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
			$('li').remove('#branch'+entityId+'');
			$("#userBranch option[value="+entityId+"]").remove();
			$("#projectBranch option[value="+entityId+"]").remove();
			alwaysScrollTop();
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	 });
}

function removeRole(elem){
	var entityId=$(elem).attr('id');
	var jsonData = {};
	jsonData.roleId = entityId;
	var url = "/config/removeRole";
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
			$('li').remove('#role'+entityId+'');
			$("#userRole option[value="+entityId+"]").remove();
			$('.multipleDropdown').multiselect('rebuild');
			alwaysScrollTop();
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	 });
}

function removeCategory(elem){
	var entityId=$(elem).attr('id');
	var jsonData = {};
	jsonData.categoryId = entityId;
	var url = "/config/removeCategory";
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
			$('li').remove('#cat'+entityId+'');
			$("#itemCategory option[value="+entityId+"]").remove();
			alwaysScrollTop();
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){
				doLogout();
			}
		}
	 });
}

function removeItem(elem){
	var entityId=$(elem).attr('id');
	var jsonData = {};
	jsonData.itemId = entityId;
	var url = "/config/removeItem";
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
			$('li').remove('#item'+entityId+'');
			$("#vendorItem option[value="+entityId+"]").remove();
			$('li:has(input[id="unitPrice'+entityId+'"])').remove();
			alwaysScrollTop();
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	 });
}

function removeVendor(elem){
	var entityId=$(elem).attr('id');
	var jsonData = {};
	jsonData.vendorId = entityId;
	var url = "/config/removeVendor";
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
			$('li').remove('#vend'+entityId+'');
			alwaysScrollTop();
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	 });
}

function removeUser(elem){
	var entityId=$(elem).attr('id');
	var jsonData = {};
	jsonData.userId = entityId;
	var url = "/config/removeUser";
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
			$('li').remove('#user'+entityId+'');
			alwaysScrollTop();
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	 });
}

function removeProject(elem){
	var entityId=$(elem).attr('id');
	var jsonData = {};
	jsonData.projectId = entityId;
	var url = "/config/removeProject";
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
			$('li').remove('#project'+entityId+'');
			alwaysScrollTop();
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	 });
}

function changeLabel(elem){
	var classval=$(elem).attr('class');
	if(classval=="filter-item color-label labelstyle-fc2929"){
		var newClassVal=classval+ " "+"selected";
		$(elem).attr('class',newClassVal);
	}else{
		var newClassVal="filter-item color-label labelstyle-fc2929";
		$(elem).attr('class',newClassVal);
	}
}

function langCheckUncheck(elem){
	var check_box_values = $('#projectLabourProficiencyList input[class="langProfEnable"]:checkbox:checked').map(function () {
	    return this.value;
	}).get();
	var length=check_box_values.length;
	if(length>0){
		var text=length+" "+"Items Selected";
		$("#projectLabourProficiency").text(text);
		$("#projectLabourProficiency").append("&nbsp;&nbsp;<b class='caret'></b>");
	}if(check_box_values==0){
		$("#projectLabourProficiency").text("None Selected");
		$("#projectLabourProficiency").append("&nbsp;&nbsp;<b class='caret'></b>");
	}
}

function branchcheckUncheck(elem){
	var checked=$(elem).is(':checked');
	var check_box_values = $('input[name="branchcheck1"]:checkbox:checked').map(function () {
	    return this.value;
	}).get();
	if(check_box_values>0){
		$("#reorderleveldropdown").innerText="Selected";
	}else{
		$("#reorderleveldropdown").innerText="None Selected";
	}
	if(checked==true){
		var checkedName=$(elem).attr('name');
		if(checkedName=="branchcheck"){
			$('input[name="branchcheck1"]').each(function () {
	        $(this).prop("checked" ,true);
		});
		}else{
			$(this).prop("checked" ,true);
		}
		$('input[id*="reorderlevelinputid"]').each(function () {
			if($(this).val()=="0" || $(this).val()==""){
				$(this).val("0");
			}
		});
		var check_box_values = $('input[name="branchcheck1"]:checkbox:checked').map(function () {
		    return this.value;
		}).get();
		var length=check_box_values.length;
		if(length>0){
			var text=length+" "+"Items Selected";
			$("#reorderleveldropdown").text(text);
			$("#reorderleveldropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
		}if(check_box_values==0){
			$("#reorderleveldropdown").text("None Selected");
			$("#reorderleveldropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
		}
	}
	if(checked==false){
		var checkvalue=$(elem).val();
		if(checkvalue==""){
			if(confirm("Do you want to remove all your selected branch && inventory item reorder level setup?")){
				$('input[name="branchcheck1"]').each(function () {
			        $(this).prop("checked" ,false);
				});
				$('input[id*="reorderlevelinputid"]').each(function () {
					if($(this).val()=="0" || $(this).val()==""){
						$(this).val("0");
					}
				});
				var check_box_values = $('input[name="branchcheck1"]:checkbox:checked').map(function () {
				    return this.value;
				}).get();
			}
		}
		var length=check_box_values.length;
		if(length>0){
			var text=length+" "+"Items Selected";
			$("#reorderleveldropdown").text(text);
			$("#reorderleveldropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
		}if(check_box_values==0){
			$("#reorderleveldropdown").text("None Selected");
			$("#reorderleveldropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
		}
	}
}

function checkUncheck(elem){
	var checked=$(elem).is(':checked');
	var check_box_values = $('input[name="vendoritemcheck"]:checkbox:checked').map(function () {
	    return this.value;
	}).get();
	if(check_box_values>0){
		$("#vendordropdown").innerText="Selected";
	}else{
		$("#vendordropdown").innerText="None Selected";
	}
	$("#vendordropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
	if(checked==true){
		var checkvalue=$(elem).val();
		if(checkvalue==""){
			var unitPriceVal=$('input[id="unitPrice"]').val();
			$('input[name="vendoritemcheck"]').each(function () {
		        $(this).prop("checked" ,true);
			});
			$('input[id*="unitPrice"]').each(function () {
				if($(this).val()=="0.0" || $(this).val()==""){
					$(this).val("0.0");
				}
			});
		}
		var check_box_values = $('input[name="vendoritemcheck"]:checkbox:checked').map(function () {
		    return this.value;
		}).get();
		var length=check_box_values.length;
		if(length>0){
			var text=length+" "+"Items Selected";
			$("#vendordropdown").text(text);
			$("#vendordropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
		}if(check_box_values==0){
			$("#vendordropdown").text("None Selected");
			$("#vendordropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
		}
	}
	if(checked==false){
		var checkvalue=$(elem).val();
		if(checkvalue==""){
			if(confirm("Do u want to remove all your selected vendor items and their unit prices!")){
				$('input[name="vendoritemcheck"]').each(function () {
			        $(this).prop("checked" ,false);
				});
				$('input[id*="unitPrice"]').each(function () {
			        $(this).val("0.0");
				});
			}
		}
		var check_box_values = $('input[name="vendoritemcheck"]:checkbox:checked').map(function () {
		    return this.value;
		}).get();
		var length=check_box_values.length;
		if(length>0){
			var text=length+" "+"Items Selected";
			$("#vendordropdown").text(text);
			$("#vendordropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
		}if(check_box_values==0){
			$("#vendordropdown").text("None Selected");
			$("#vendordropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
		}
		var value=$(elem).val();
		$("#unitPrice"+value+"").val("0.0");
	}

		// Change TDS Setup on selection of item
		tdsSelectChangeOnItemsVend();
}

function custcheckUncheck(elem){
	var checked=$(elem).is(':checked');
	var check_box_values = $('input[name="customeritemcheck"]:checkbox:checked').map(function () {
	    return this.value;
	}).get();
	if(check_box_values>0){
		$("#customerdropdown").innerText="Selected";
	}else{
		$("#customerdropdown").innerText="None Selected";
	}
	$("#customerdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
	if(checked==true){
		var checkvalue=$(elem).val();
		if(checkvalue==""){
			$('input[name="customeritemcheck"]').each(function () {
		        $(this).prop("checked" ,true);
			});
			$('input[id*="custDiscount"]').each(function () {
				if($(this).val()=="0.0" || $(this).val()==""){
					$(this).val("0.0");
				}
			});
		}
		var check_box_values = $('input[name="customeritemcheck"]:checkbox:checked').map(function () {
		    return this.value;
		}).get();
		var length=check_box_values.length;
		if(length>0){
			var text=length+" "+"Items Selected";
			$("#customerdropdown").text(text);
			$("#customerdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
		}if(check_box_values==0){
			$("#customerdropdown").text("None Selected");
			$("#customerdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
		}
	}
	if(checked==false){
		var checkvalue=$(elem).val();
		if(checkvalue==""){
			if(confirm("Do u want to remove all your selected customer item and their discount rate!")){
				$('input[name="customeritemcheck"]').each(function () {
			        $(this).prop("checked" ,false);
				});
				$('input[id*="custDiscount"]').each(function () {
			        $(this).val("0.0");
				});
			}
		}
		var check_box_values = $('input[name="customeritemcheck"]:checkbox:checked').map(function () {
		    return this.value;
		}).get();
		var length=check_box_values.length;
		if(length>0){
			var text=length+" "+"Items Selected";
			$("#customerdropdown").text(text);
			$("#customerdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
		}if(check_box_values==0){
			$("#customerdropdown").text("None Selected");
			$("#customerdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
		}
		var value=$(elem).val();
		$("#custDiscount"+value+"").val("0.0");
	}
}

function checkUncheckPermission(elem){
	var elemid=$(elem).attr('id');
	var checked=$('input[id='+elemid+']:checkbox').is(':checked');
	if(checked==true){
		$(".permissions li[id="+elemid+"]").attr("class","enabled");
	}
	if(checked==false){
		$(".permissions li[id="+elemid+"]").attr("class","");
	}
}

function toggleCheck(elem){
	var unitPriceVal=$(elem).val();
	var value=$(elem).attr('id');
	var val=value.substring(9,value.length);
	if(unitPriceVal==""){
		$('input[name="vendoritemcheck"][value='+val+']').prop('checked', false);
		$('input[name="vendoritemcheck"][value='+val+']').attr('checked', false);
	}else{
		$('input[name="vendoritemcheck"][value='+val+']').prop("checked" ,true);
		$('input[name="vendoritemcheck"][value='+val+']').attr('checked', true);
	}
	var check_box_values = $('input[name="vendoritemcheck"]:checkbox:checked').map(function () {
	    return this.value;
	}).get();
	var length=check_box_values.length;
	if(length>0){
		var text=length+" "+"Items Selected";
		$("#vendordropdown").text(text);
		$("#vendordropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
	}if(check_box_values==0){
		$("#vendordropdown").text("None Selected");
		$("#vendordropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
	}

function toggleCheck(elem){
	var unitPriceVal=$(elem).val();
	var value=$(elem).attr('id');
	var val=value.substring(9,value.length);
	if(unitPriceVal==""){
		$('input[name="vendoritemcheck"][value='+val+']').prop('checked', false);
		$('input[name="vendoritemcheck"][value='+val+']').attr('checked', false);
	}else{
		$('input[name="vendoritemcheck"][value='+val+']').prop("checked" ,true);
		$('input[name="vendoritemcheck"][value='+val+']').attr('checked', true);
	}
	var check_box_values = $('input[name="vendoritemcheck"]:checkbox:checked').map(function () {
	    return this.value;
	}).get();
	var length=check_box_values.length;
	if(length>0){
		var text=length+" "+"Items Selected";
		$("#vendordropdown").text(text);
		$("#vendordropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
	}if(check_box_values==0){
		$("#vendordropdown").text("None Selected");
		$("#vendordropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
	}
}
}

function custtoggleCheck(elem){
	var unitPriceVal=$(elem).val();
	var value=$(elem).attr('id');
	var val=value.substring(12,value.length);
	if(unitPriceVal==""){
		$('input[name="customeritemcheck"][value='+val+']').prop('checked', false);
		$('input[name="customeritemcheck"][value='+val+']').attr('checked', false);
	}else{
		$('input[name="customeritemcheck"][value='+val+']').prop("checked" ,true);
		$('input[name="customeritemcheck"][value='+val+']').attr('checked', true);
	}
	var check_box_values = $('input[name="customeritemcheck"]:checkbox:checked').map(function () {
	    return this.value;
	}).get();
	var length=check_box_values.length;
	if(length>0){
		var text=length+" "+"Items Selected";
		$("#customerdropdown").text(text);
		$("#customerdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
	}if(check_box_values==0){
		$("#customerdropdown").text("None Selected");
		$("#customerdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
	}
}

$(document).ready(function(){
	$('#vendordropdown').click(function(){
		var classval=$(this).attr('class');
		if(classval=="multiselect dropdown-toggle btn"){
			var newclassval=classval+ " " +"open";
			$(this).attr('class',newclassval);
			var divdropdown="openvendoritemdropdown-menu"
			$(".vendoritemdropdown-menu").attr('class',divdropdown);
		}
		if(classval=="multiselect dropdown-toggle btn open"){
			var newclassval="multiselect dropdown-toggle btn";
			$(this).attr('class',newclassval);
			var divdropdown="vendoritemdropdown-menu"
			$(".openvendoritemdropdown-menu").attr('class',divdropdown);
		}
	});

	$('#vendTdsdropdown').click(function(){
		var classval=$(this).attr('class');
		if(classval=="multiselect dropdown-toggle btn"){
			var newclassval=classval+ " " +"open";
			$(this).attr('class',newclassval);
			var divdropdown="openvendTdsdropdown-menu"
			$(".vendTdsdropdown-menu").attr('class',divdropdown);
		}
		if(classval=="multiselect dropdown-toggle btn open"){
			var newclassval="multiselect dropdown-toggle btn";
			$(this).attr('class',newclassval);
			var divdropdown="vendTdsdropdown-menu"
			$(".openvendTdsdropdown-menu").attr('class',divdropdown);
		}
	});
});

$(document).ready(function(){
	$('#docuploadrulecustomdropdown').click(function(){
		var classval=$(this).attr('class');
		if(classval=="multiselect dropdown-toggle btn"){
			var newclassval=classval+ " " +"open";
			$(this).attr('class',newclassval);
			var divdropdown="opendocuploadrulecustomdropdown-menu"
			$(".docuploadrulecustomdropdown-menu").attr('class',divdropdown);
		}
		if(classval=="multiselect dropdown-toggle btn open"){
			var newclassval="multiselect dropdown-toggle btn";
			$(this).attr('class',newclassval);
			var divdropdown="docuploadrulecustomdropdown-menu"
			$(".opendocuploadrulecustomdropdown-menu").attr('class',divdropdown);
		}
	});
});



$(document).ready(function(){
	$('#statutoryidnumber').click(function(){
		var classval=$(this).attr('class');
		if(classval=="multiselect dropdown-toggle btn"){
			var newclassval=classval+ " " +"open";
			$(this).attr('class',newclassval);
			var divdropdown="openstatutoryidnumberdropdown-menu"
			$(".statutoryidnumberdropdown-menu").attr('class',divdropdown);
		}
		if(classval=="multiselect dropdown-toggle btn open"){
			var newclassval="multiselect dropdown-toggle btn";
			$(this).attr('class',newclassval);
			var divdropdown="statutoryidnumberdropdown-menu"
			$(".openstatutoryidnumberdropdown-menu").attr('class',divdropdown);
		}
	});
});

$(document).ready(function(){
	$('#vendorspecialadjustmentbuttonid').click(function(){
		var classval=$(this).attr('class');
		if(classval=="multiselect dropdown-toggle btn"){
			if($("#vendorEntityHiddenId").val()==""){
				$("b[class='adjustmentsName']").hide();
				$("#adjustmentName").val("");
				$("div[class='adjustmentsBasis']").hide();
				$("#allowedAdjustments option:first").prop("selected",'selected');
				$("#adjustmentBais option:first").prop("selected",'selected');
				$("b[class='adjustmentsBasisRate']").hide();
				$("#percentageAdjustmentRate").val("");
			}
			var newclassval=classval+ " " +"open";
			$(this).attr('class',newclassval);
			var divdropdown="openvendorspecialadjustmentdropdown-menu"
			$(".vendorspecialadjustmentdropdown-menu").attr('class',divdropdown);
		}
		if(classval=="multiselect dropdown-toggle btn open"){
			var newclassval="multiselect dropdown-toggle btn";
			$(this).attr('class',newclassval);
			var divdropdown="vendorspecialadjustmentdropdown-menu"
			$(".openvendorspecialadjustmentdropdown-menu").attr('class',divdropdown);
		}
	});
});

$(document).ready(function(){
	$('#customerstatutoryidnumber').click(function(){
		var classval=$(this).attr('class');
		if(classval=="multiselect dropdown-toggle btn"){
			var newclassval=classval+ " " +"open";
			$(this).attr('class',newclassval);
			var divdropdown="opencustomerstatutoryidnumberdropdown-menu"
			$(".customerstatutoryidnumberdropdown-menu").attr('class',divdropdown);
		}
		if(classval=="multiselect dropdown-toggle btn open"){
			var newclassval="multiselect dropdown-toggle btn";
			$(this).attr('class',newclassval);
			var divdropdown="customerstatutoryidnumberdropdown-menu"
			$(".opencustomerstatutoryidnumberdropdown-menu").attr('class',divdropdown);
		}
	});
});

function toggleRadio(elem){
	var elemName=$(elem).attr('name');
	var value=$(elem).val();
	var parentTr=$(elem).closest("ul").attr('id');
	if(value=="1"){
		$("#"+parentTr+" input[name="+elemName+"][value='1']").attr("checked",true);
		$("#"+parentTr+" input[name="+elemName+"][value='2']").attr("checked",false);
	}
	if(value=="2"){
		$("#"+parentTr+" input[name="+elemName+"][value='2']").attr("checked",true);
		$("#"+parentTr+" input[name="+elemName+"][value='1']").attr("checked",false);
	}

}

function populateSearchItemBasedOnCategory(elem){
	//alert(" Start populateSearchItemBasedOnCategory") ;
	var value=$(elem).val();
	if(value==""){
		getAllChartOfAccount();
	}else{
		var jsonData={};
		jsonData.useremail=$("#hiddenuseremail").text();
		jsonData.categoryId=value;
		var url="/chartOfAccounts/categoryBasedChartOfAccounts";
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
				$("#searchItems").children().remove();
				//$("#searchItems").append('<option value="">--Please Select--</option>');
				var bnchOption="";
				for(var i=0;i<data.categoryBasedCoaData.length;i++){
					$("#searchItems").append('<option value="'+data.categoryBasedCoaData[i].id+'">' +data.categoryBasedCoaData[i].name+ '</option>');
				}
				$('#searchItems').multiselect('rebuild');
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}


		});
	}
}

function validateStockAvailableForSaleTransaction(elem){
	var inputQty="";var returnString=false;
	var parentTr=$(elem).parent().parent().parent('tr:first').attr('id');
	var specId="";
	if(parentTr=="soccpntrid"){
		inputQty=$("#soccpnunits").val();
		specId=$("#soccpnItem option:selected").val();
	}
	if(parentTr=="soccpltrid"){
		inputQty=$("#soccplunits").val();
		specId=$("#soccplItem option:selected").val();
	}
	var jsonData={};
	jsonData.email=$("#hiddenuseremail").text();
	jsonData.incomeSpecificsId=specId;
	var url="/specifics/incomeAvailableStock";
	$.ajax({
		url         : url,
		data        : JSON.stringify(jsonData),
		type        : "text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method      : "POST",
		contentType : 'application/json',
		async: false,
		success     : function (data) {
			if(data.result){
				if(inputQty>data.incomeStockData[0].stockAvailable){
					swal("Error!","You Are Selling An Inventory Item.The Stock Available Of this item is " +data.incomeStockData[0].stockAvailable+ "Item is out of stock.You can create the stock for this item inorder to sell for this much quantity.","error");
					returnString=true;
					return returnString;
				}
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}
function customDropDownToggle(elem){
	var classval=$(elem).attr('class');
	var menuId=$(elem).attr('id');
	if(classval=="multiselect dropdown-toggle btn"){
		var newclassval=classval+ " " +"open";
		$(elem).attr('class',newclassval);
		if(menuId=="txnIncoacreatordropdown"){
			var divdropdown="opentxnIncoacreatordropdown-menu"
			$(".txnIncoacreatordropdown-menu").attr('class',divdropdown);
		}else if(menuId=="txnExcoacreatordropdown"){
			var divdropdown="opentxnExcoacreatordropdown-menu"
			$(".txnExcoacreatordropdown-menu").attr('class',divdropdown);
		}else if(menuId=="txnAscoacreatordropdown"){
			var divdropdown="opentxnAscoacreatordropdown-menu"
			$(".txnAscoacreatordropdown-menu").attr('class',divdropdown);
		}else if(menuId=="txnLicoacreatordropdown"){
			var divdropdown="opentxnLicoacreatordropdown-menu"
			$(".txnLicoacreatordropdown-menu").attr('class',divdropdown);
		}else if(menuId=="txnIncoaapproverdropdown"){
			var divdropdown="opentxnIncoaapproverdropdown-menu"
			$(".txnIncoaapproverdropdown-menu").attr('class',divdropdown);
		}else if(menuId=="txnExcoaapproverdropdown"){
			var divdropdown="opentxnExcoaapproverdropdown-menu"
			$(".txnExcoaapproverdropdown-menu").attr('class',divdropdown);
		}else if(menuId=="txnAscoaapproverdropdown"){
			var divdropdown="opentxnAscoaapproverdropdown-menu"
			$(".txnAscoaapproverdropdown-menu").attr('class',divdropdown);
		}else if(menuId=="txnLicoaapproverdropdown"){
			var divdropdown="opentxnLicoaapproverdropdown-menu"
			$(".txnLicoaapproverdropdown-menu").attr('class',divdropdown);
		}
	}else if(classval=="multiselect dropdown-toggle btn open"){
		var menuId=$(elem).attr('id');
		var newclassval="multiselect dropdown-toggle btn";
		$(elem).attr('class',newclassval);
		if(menuId=="txnIncoacreatordropdown"){
			var divdropdown="txnIncoacreatordropdown-menu"
			$(".opentxnIncoacreatordropdown-menu").attr('class',divdropdown);
		}else if(menuId=="txnExcoacreatordropdown"){
			var divdropdown="txnExcoacreatordropdown-menu"
			$(".opentxnExcoacreatordropdown-menu").attr('class',divdropdown);
		}else if(menuId=="txnAscoacreatordropdown"){
			var divdropdown="txnAscoacreatordropdown-menu"
			$(".opentxnAscoacreatordropdown-menu").attr('class',divdropdown);
		}else if(menuId=="txnLicoacreatordropdown"){
			var divdropdown="txnLicoacreatordropdown-menu"
			$(".opentxnLicoacreatordropdown-menu").attr('class',divdropdown);
		}else if(menuId=="txnIncoaapproverdropdown"){
			var divdropdown="txnIncoaapproverdropdown-menu"
			$(".opentxnIncoaapproverdropdown-menu").attr('class',divdropdown);
		}else if(menuId=="txnExcoaapproverdropdown"){
			var divdropdown="txnExcoaapproverdropdown-menu"
			$(".opentxnExcoaapproverdropdown-menu").attr('class',divdropdown);
		}else if(menuId=="txnAscoaapproverdropdown"){
			var divdropdown="txnAscoaapproverdropdown-menu"
			$(".opentxnAscoaapproverdropdown-menu").attr('class',divdropdown);
		}else if(menuId=="txnLicoaapproverdropdown"){
			var divdropdown="txnLicoaapproverdropdown-menu"
			$(".opentxnLicoaapproverdropdown-menu").attr('class',divdropdown);
		}
	}
}

function changeClass(event) {
	var id = event.target.id;
	var name = event.target.name;
	if (id != "vendordropdown" && id !="vendorItemList" && id!="checkboxid" && id!="vendoritemlist" && name!="unitPrice" && name!="VendRcmApplicableDate" && name!="cesRateVendItem" && name!="rcmRateVendItem" && id!="venderitemcontentsearchinput") {
		var classval = $('#vendordropdown').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#vendordropdown').attr('class', newclassval);
			var divdropdown = "vendoritemdropdown-menu"
			$(".openvendoritemdropdown-menu").attr('class', divdropdown);
		}
	}
	if (id != "vendTdsdropdown" && id !="vendTdsItemList" && id!="checkboxid" && id!="vendTdsitemlist" && name!="vendTdsSetupButton" && id!="venderitemcontentsearchinput") {
		var classval = $('#vendTdsdropdown').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#vendTdsdropdown').attr('class', newclassval);
			var divdropdown = "vendTdsdropdown-menu"
			$(".openvendTdsdropdown-menu").attr('class', divdropdown);
		}
	}
	if (id != "customerdropdown" && id !="customerItemList" && id!="checkboxid" && id!="customeritemlist" && name!="custDiscount" && id!="customeritemcontentsearchinput") {
		var classval = $('#customerdropdown').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#customerdropdown').attr('class', newclassval);
			var divdropdown = "customeritemdropdown-menu"
			$(".opencustomeritemdropdown-menu").attr('class', divdropdown);
		}
	}
	if(id !="reorderleveldropdown" && id!="reorderlevelbranchlist" && id!="reorderLevelBranchList" && name!="branchcheck" && name!="reorderlevelselectalertuser" && id!="bnchtext" && name!="reorderlevelinput"){
		var classval = $('#reorderleveldropdown').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#reorderleveldropdown').attr('class', newclassval);
			var divdropdown = "reorderleveldropdown-menu"
			$(".openreorderleveldropdown-menu").attr('class', divdropdown);
		}
	}
	if(id!="txnIncoacreatordropdown" && id!="limitallfrom" && id!="limitallto" && id!="transactionCoaCreatorList" && id!="transactioncoalist" && id!="checkCOA" && id!="radioCOA" &&  name!="coaAmountLimit" && name!="coaAmountLimitTo" && name!="transactioncreatorcontentsearchinput"){
		var classval = $('#txnIncoacreatordropdown').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#txnIncoacreatordropdown').attr('class', newclassval);
			var divdropdown = "txnIncoacreatordropdown-menu"
			$(".opentxnIncoacreatordropdown-menu").attr('class', divdropdown);
		}
	}
	if(id!="txnExcoacreatordropdown" && id!="limitallfrom" && id!="limitallto" && id!="transactionCoaCreatorList" && id!="transactioncoalist" && id!="checkCOA" && id!="radioCOA" &&  name!="coaAmountLimit" && name!="coaAmountLimitTo" && name!="transactioncreatorcontentsearchinput"){
		var classval = $('#txnExcoacreatordropdown').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#txnExcoacreatordropdown').attr('class', newclassval);
			var divdropdown = "txnExcoacreatordropdown-menu"
			$(".opentxnExcoacreatordropdown-menu").attr('class', divdropdown);
		}
	}
	if(id!="txnAscoacreatordropdown" && id!="limitallfrom" && id!="limitallto" && id!="transactionCoaCreatorList" && id!="transactioncoalist" && id!="checkCOA" && id!="radioCOA" &&  name!="coaAmountLimit" && name!="coaAmountLimitTo" && name!="transactioncreatorcontentsearchinput"){
		var classval = $('#txnAscoacreatordropdown').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#txnAscoacreatordropdown').attr('class', newclassval);
			var divdropdown = "txnAscoacreatordropdown-menu"
			$(".opentxnAscoacreatordropdown-menu").attr('class', divdropdown);
		}
	}
	if(id!="txnLicoacreatordropdown" && id!="limitallfrom" && id!="limitallto" && id!="transactionCoaCreatorList" && id!="transactioncoalist" && id!="checkCOA" && id!="radioCOA" &&  name!="coaAmountLimit" && name!="coaAmountLimitTo" && name!="transactioncreatorcontentsearchinput"){
		var classval = $('#txnLicoacreatordropdown').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#txnLicoacreatordropdown').attr('class', newclassval);
			var divdropdown = "txnLicoacreatordropdown-menu"
			$(".opentxnLicoacreatordropdown-menu").attr('class', divdropdown);
		}
	}
	if(id!="txnIncoaapproverdropdown" && id!="limitallfrom" && id!="limitallto" && id!="transactionCoaApproverList" && id!="transactioncoalist" && id!="checkCOA" && id!="radioCOA" &&  name!="coaAmountLimit" && name!="coaAmountLimitTo" && id!="transactionapprovercontentsearchinput"){
		var classval = $('#txnIncoaapproverdropdown').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#txnIncoaapproverdropdown').attr('class', newclassval);
			var divdropdown = "txnIncoaapproverdropdown-menu"
			$(".opentxnIncoaapproverdropdown-menu").attr('class', divdropdown);
		}
	}
	if(id!="txnExcoaapproverdropdown" && id!="limitallfrom" && id!="limitallto" && id!="transactionCoaApproverList" && id!="transactioncoalist" && id!="checkCOA" && id!="radioCOA" &&  name!="coaAmountLimit" && name!="coaAmountLimitTo" && id!="transactionapprovercontentsearchinput"){
		var classval = $('#txnExcoaapproverdropdown').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#txnExcoaapproverdropdown').attr('class', newclassval);
			var divdropdown = "txnExcoaapproverdropdown-menu"
			$(".opentxnExcoaapproverdropdown-menu").attr('class', divdropdown);
		}
	}
	if(id!="txnAscoaapproverdropdown" && id!="limitallfrom" && id!="limitallto" && id!="transactionCoaApproverList" && id!="transactioncoalist" && id!="checkCOA" && id!="radioCOA" &&  name!="coaAmountLimit" && name!="coaAmountLimitTo" && id!="transactionapprovercontentsearchinput"){
		var classval = $('#txnAscoaapproverdropdown').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#txnAscoaapproverdropdown').attr('class', newclassval);
			var divdropdown = "txnAscoaapproverdropdown-menu"
			$(".opentxnAscoaapproverdropdown-menu").attr('class', divdropdown);
		}
	}
	if(id!="txnLicoaapproverdropdown" && id!="limitallfrom" && id!="limitallto" && id!="transactionCoaApproverList" && id!="transactioncoalist" && id!="checkCOA" && id!="radioCOA" &&  name!="coaAmountLimit" && name!="coaAmountLimitTo" && id!="transactionapprovercontentsearchinput"){
		var classval = $('#txnLicoaapproverdropdown').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#txnLicoaapproverdropdown').attr('class', newclassval);
			var divdropdown = "txnLicoaapproverdropdown-menu"
			$(".opentxnLicoaapproverdropdown-menu").attr('class', divdropdown);
		}
	}
	if(id!="userEarningdropdown" && id!="usrEarningslist" && id!="transactionCoaList" && name!="checkCOA" && name!="earningItemName" &&  name!="earningAnnual" && name!="earningMonthly" ){
		var classval = $('#userEarningdropdown').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#userEarningdropdown').attr('class', newclassval);
			var divdropdown = "userEarningdropdown-menu"
			$(".openuserEarningdropdown-menu").attr('class', divdropdown);
		}
	}
	if(id!="userDeductionsdropdown" && id!="usrDeductionsList" && id!="transactionCoaList" && name!="checkCOA" && name!="deductionItemName" &&  name!="deductionAnnual" && name!="deductionMonthly" ){
		var classval = $('#userDeductionsdropdown').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#userDeductionsdropdown').attr('class', newclassval);
			var divdropdown = "userDeductionsdropdown-menu"
			$(".openuserDeductionsdropdown-menu").attr('class', divdropdown);
		}
	}
	if(id!="statutoryidnumber" && id!="statutoryidnumberItemList" && id!="vendorStatutoryName1" && id!="statutoryidnumberitemList" && id!="vendorStatutoryNumber1" && id!="vendorStatutoryName2" && id!="vendorStatutoryNumber2" && id!="vendorStatutoryName3" && id!="vendorStatutoryNumber3" && id!="vendorStatutoryName4" && id!="vendorStatutoryNumber4"){
		var classval = $('#statutoryidnumber').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#statutoryidnumber').attr('class', newclassval);
			var divdropdown = "statutoryidnumberdropdown-menu"
			$(".openstatutoryidnumberdropdown-menu").attr('class', divdropdown);
		}
	}
	if(id!="customerstatutoryidnumber" && id!="customerstatutoryidnumberItemList" && id!="customerStatutoryName1" && id!="customerstatutoryidnumberitemList" && id!="customerStatutoryNumber1" && id!="customerStatutoryName2" && id!="customerStatutoryNumber2" && id!="customerStatutoryName3" && id!="customerStatutoryNumber3" && id!="customerStatutoryName4" && id!="customerStatutoryNumber4"){
		var classval = $('#customerstatutoryidnumber').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#customerstatutoryidnumber').attr('class', newclassval);
			var divdropdown = "customerstatutoryidnumberdropdown-menu"
			$(".opencustomerstatutoryidnumberdropdown-menu").attr('class', divdropdown);
		}
	}
	if(id!="vendorspecialadjustmentbuttonid" && id!="vendorspecialAdjustmentList" && id!="allowedAdjustments" && id!="vendorspecialadjustmentList" && id!="adjustmentName" && id!="adjustmentBais" && id!="percentageAdjustmentRate"){
		var classval = $('#vendorspecialadjustmentbuttonid').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#vendorspecialadjustmentbuttonid').attr('class', newclassval);
			var divdropdown = "vendorspecialadjustmentdropdown-menu"
			$(".openvendorspecialadjustmentdropdown-menu").attr('class', divdropdown);
		}
	}
	var className = event.target.className;
	if ("claim-list" !== className && "travelclaimList" !== className && "travelClaimList" !== className && "travelClaim-menuid" !== className && "multiselect dropdown-toggle btn" !== className && "travelClaimSearch" !== name && "travelClaimDistance" !== name
		&& "travelClaimOneWay" !== name && "travelClaimReturn" !== name && "claimsLabel" !== className && !/travelClaim/i.test(id) && "travelBoardingLodging" !==className
		&& "travelBoarding_room" !== name && "travelBoarding_food" !== name && "travelBoardingLodgingSearchInput" !== name && "travelBoardingListHeader" !== className) {
		$('.travelClaim-menuid').removeClass('opentravelClaim-menuid');
	}

	if ("claim-list" !== className && "expenseClaimsClass" !== className && "expenseClaim-menuid" !== className && "multiselect dropdown-toggle btn" !== className && "expenseClaimSearchInput" !== name
		&& "expenseClaims" !== className && "expenseClaim_maxAdvance" !== name && "expenseClaim_monthlyMoney" !== name && "claimsLabel" !== className
		&& "expenseClaimsHeader" !== className) {
		$('.expenseClaim-menuid').removeClass('opentravelClaim-menuid');
	}
	if(id!="docuploadrulecustomdropdownBranchList" && id!="customdoccheckBranch" && name!="monetoryLimit" && id!="docuploadrulecustomdropdown" && id!="docuploadrulecustomdropdownBranchlist"){
		var classval = $('#docuploadrulecustomdropdown').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#docuploadrulecustomdropdown').attr('class', newclassval);
			var divdropdown = "docuploadrulecustomdropdown-menu";
			$(".opendocuploadrulecustomdropdown-menu").attr('class', divdropdown);
		}
	}
	if ("travelClaim-menuid" !== className && "claim-list" !== className && "notesUsersSearchInput" !== name && "notesUsersCheck" !== className && "notesUsersSpan" !== className
			&& "notesuserLi" !== className && "multiselect dropdown-toggle btn notes-users-select" !== className && "notesUsersList" !== className && "notesUsersSearchDiv" !== className) {
		$('.notesUsers-menuid').removeClass('opentravelClaim-menuid');
	}


	if(id!="debitAccountHeadsdropdown" && id!="debitmtefjetridAccountHeadsdropdown" && id!="debitAccountHeadsList" && id!="debitaccountheadlisthead" && id!="debitaccountheadlist" && id!="debitaccountheadlistRadioId" && id!="debitaccountheadlistLabelId" && id!="debitAccountHeadscontentsearchinput" && id!="debitaccountHeadsLabelId" && id != "mtefpedebitAmount"){
		var classval = $('#debitAccountHeadsdropdown').attr('class');
		var otherclassval=$('#debitmtefjetridAccountHeadsdropdown').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#debitAccountHeadsdropdown').attr('class', newclassval);
			var divdropdown = "debitAccountHeadsdropdown-menu";
			$(".opendebitAccountHeadsdropdown-menu").attr('class', divdropdown);
		}
		if (otherclassval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#debitmtefjetridAccountHeadsdropdown').attr('class', newclassval);
			var divdropdown = "debitAccountHeadsdropdown-menu";
			$(".opendebitAccountHeadsdropdown-menu").attr('class', divdropdown);
		}
	}
	if(id!="creditAccountHeadsdropdown" && id!="creditmtefjetridAccountHeadsdropdown" && id!="creditAccountHeadsList" && id!="creditaccountheadlisthead" && id!="creditaccountheadlist" && id!="creditaccountheadlistRadioId" && id!="creditaccountheadlistLabelId" && id!="creditAccountHeadscontentsearchinput" && id!="creditaccountHeadsLabelId" && id != "mtefpecreditAmount"){
		var classval = $('#creditAccountHeadsdropdown').attr('class');
		var otherclassval=$('#creditmtefjetridAccountHeadsdropdown').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#creditAccountHeadsdropdown').attr('class', newclassval);
			var divdropdown = "creditAccountHeadsdropdown-menu";
			$(".opencreditAccountHeadsdropdown-menu").attr('class', divdropdown);
		}
		if (otherclassval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#creditmtefjetridAccountHeadsdropdown').attr('class', newclassval);
			var divdropdown = "creditAccountHeadsdropdown-menu";
			$(".opencreditAccountHeadsdropdown-menu").attr('class', divdropdown);
		}
	}


	if(id!="customerBranchDropdownBtn" && id!="customerBranchList" && id!="customerBranchContentSearchInput" && id!="custOpenBalance" && id!="custOpenBalanceAdvPaid" && id!="custBranchCheck"){
		var classval = $('#customerBranchDropdownBtn').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#customerBranchDropdownBtn').attr('class', newclassval);
			var divdropdown = "customerBranchDropdown-menu";
			$(".openCustomerBranchDropdown-menu").attr('class', divdropdown);
		}
	}

	if(id!="vendorBranchDropdownBtn" && id!="vendorBranchList" && id!="vendorBranchContentSearchInput" && id!="vendorBranchCheck" && id!="vendorOpenBalance" && id!="vendorOpenBalanceAdvPaid" && className != 'vendCustSelectAllCls'){
		var classval = $('#vendorBranchDropdownBtn').attr('class');
		if (classval == "multiselect dropdown-toggle btn open") {
			var newclassval = "multiselect dropdown-toggle btn";
			$('#vendorBranchDropdownBtn').attr('class', newclassval);
			var divdropdown = "customerBranchDropdown-menu";
			$(".openCustomerBranchDropdown-menu").attr('class', divdropdown);
		}
	}

	if($('div[data-auto=auto-complete]').is(':visible')){
		var dataId=$('div[data-auto=auto-complete]').attr('data-attr'),dataAttr=$(event.target).attr('data-attr');
		if(undefined===dataAttr && id!==dataId){
			$('div[data-auto=auto-complete]').empty();
		}
	}
	if("checkboxid" != id && "itemBranch2Btn" != id && "itemBranch2Btn-menuid" != id && "itemBranch2BtnList" !== id && "openingBalance" !== className && "noOfUnits" != id && "inventoryRate" != id && "inventoryValue" != id && "itemBranch2Class-cb" != className && "txtBox80" !== className) {
		$('#itemBranch2Btn-menuid').removeClass('opentravelClaim-menuid');
	}
}

function showAdjustmentList(elem){
	var value=$(elem).val();
	if(value==1){
		$(".adjustmentsName").show();
		$(".adjustmentsBasis").show();
		$("#adjustmentName").val("");
		$("#percentageAdjustmentRate").val("");
		$(".adjustmentsBasisRate").hide();
		$("select[id='adjustmentBais'] option:first").prop("selected", "selected");
	}
	if(value==0){
		$("select[id='adjustmentBais'] option:first").prop("selected", "selected");
		$(".adjustmentsName").hide();
		$("#adjustmentName").val("");
		$("#adjustmentName").val("");
		$("#percentageAdjustmentRate").val("");
		$(".adjustmentsBasis").hide();
		$(".adjustmentsBasisRate").hide();
	}
}

function adjustmentBasis(elem){
	var value=$(elem).val();
	if(value==1){
		$(".adjustmentsBasisRate").hide();
		$("#percentageAdjustmentRate").val("");
	}
	if(value==2){
		$(".adjustmentsBasisRate").show();
		$("#percentageAdjustmentRate").val("");
	}
}

//selectall functionality for monetorydocupload rule
function customdoccheckUncheck(elem){
	var checked=$(elem).is(':checked');
	var check_box_values = $('input[name="customdoccheckBranch"]:checkbox:checked').map(function () {
	    return this.value;
	}).get();
	if(check_box_values>0){
		$("#docuploadrulecustomdropdown").innerText="Selected";
	}else{
		$("#docuploadrulecustomdropdown").innerText="None Selected";
	}
	if(checked==true){
		var checkvalue=$(elem).val();
		if(checkvalue==""){
			var monetoryVal=$('input[id="monetoryLimit"]').val();
			$('input[name="customdoccheckBranch"]').each(function () {
		        $(this).prop("checked" ,true);
			});
			$('input[id*="monetoryLimit"]').each(function () {
				if($(this).val()=="0.0" || $(this).val()==""){
					$(this).val("0.0");
				}
			});
		}
		var check_box_values = $('input[name="customdoccheckBranch"]:checkbox:checked').map(function () {
		    return this.value;
		}).get();
		var length=check_box_values.length;
		if(length>0){
			var text=length+" "+"Items Selected";
			$("#docuploadrulecustomdropdown").text(text);
			$("#docuploadrulecustomdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
		}if(check_box_values==0){
			$("#docuploadrulecustomdropdown").text("None Selected");
			$("#docuploadrulecustomdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
		}
	}
	if(checked==false){
		var checkvalue=$(elem).val();
		if(checkvalue==""){
			if(confirm("Do u want to remove all your selected branch and their monetory limit!")){
				$('input[name="customdoccheckBranch"]').each(function () {
			        $(this).prop("checked" ,false);
				});
				$('input[id*="monetoryLimit"]').each(function () {
			        $(this).val("0.0");
				});
			}
		}
		var check_box_values = $('input[name="customdoccheckBranch"]:checkbox:checked').map(function () {
		    return this.value;
		}).get();
		var length=check_box_values.length;
		if(length>0){
			var text=length+" "+"Items Selected";
			$("#docuploadrulecustomdropdown").text(text);
			$("#docuploadrulecustomdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
		}if(check_box_values==0){
			$("#docuploadrulecustomdropdown").text("None Selected");
			$("#docuploadrulecustomdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
		}
		var value=$(elem).val();
		$("#monetoryLimit"+value+"").val("0.0");
	}
}

function transactioncheckUncheck(elem){
	var checked=$(elem).is(':checked');
	var parentTr=$(elem).closest("ul").attr('id');
	var parentTable=$(elem).closest("td").attr('id');
	if(checked==true){
		var coaFromLimit=$('#'+parentTr+' input[name="limitallfrom"]').val();
		var coaFromLimitTo=$('#'+parentTr+' input[name="limitallto"]').val();
		var checkvalue=$(elem).val();
		if(checkvalue==""){
			$('#'+parentTr+' input[name="checkCOA"]').each(function () {
		        $(this).prop("checked" ,true);
			});
			if(coaFromLimit!=""){
				$('#'+parentTr+' input[id*="coaAmountLimit"]').each(function () {
					$(this).val(coaFromLimit);
				});
			}else{
				$('#'+parentTr+' input[id*="coaAmountLimit"]').each(function () {
					if($(this).val()=="0.0" || $(this).val()==""){
						$(this).val("0.0");
					}
				});
			}
			if(coaFromLimitTo!=""){
				$('#'+parentTr+' input[id*="coaAmountLimitTo"]').each(function () {
					$(this).val(coaFromLimitTo);
				});
			}else{
				$('#'+parentTr+' input[id*="coaAmountLimitTo"]').each(function () {
					if($(this).val()=="0.0" || $(this).val()==""){
						$(this).val("0.0");
					}
				});
			}
		}
		var check_box_values = $('#'+parentTr+' input[name="checkCOA"]:checked').map(function () {
			return this.value;
		   }).get();
		var length=check_box_values.length;
		if(length>0){
			var text=length+" "+"Items Selected";
			$("#"+parentTable+" button[name^='transactioncoa']").text(text);
			$("#"+parentTable+" button[name^='transactioncoa']").append("<b>&nbsp;&nbsp;&#8711;</b>");
		}if(check_box_values==0){
			$("#"+parentTable+" button[name^='transactioncoa']").text("None Selected");
			$("#"+parentTable+" button[name^='transactioncoa']").append("<b>&nbsp;&nbsp;&#8711;</b>");
		}
	}
	if(checked==false){
		var checkvalue=$(elem).val();
		if(checkvalue==""){
			if(confirm("Do you want to remove items transaction rights for user.This will also remove transaction from-to amount limit.")){
				$('#'+parentTr+' input[name="checkCOA"]').each(function () {
			        $(this).prop("checked" ,false);
				});
				$('#'+parentTr+' input[id*="coaAmountLimit"]').each(function () {
			        $(this).val("0.0");
				});
				$('#'+parentTr+' input[name="limitallfrom"]').val("");
				$('#'+parentTr+' input[name="limitallto"]').val("");
			}
		}else{
			$('#'+parentTr+' input[name="checkCOA"][value=""]').prop("checked" ,false);
		}
		var check_box_values = $('#'+parentTr+' input[name="checkCOA"]:checked').map(function () {
		    return this.value;
		}).get();
		var length=check_box_values.length;
		$('#'+parentTr+' input[name="checkCOA"][value=\''+checkvalue+'\']').prop("checked" ,false);
		if(length>0){
			var text=length+" "+"Items Selected";
			$("#"+parentTable+" button[name^='transactioncoa']").text(text);
			$("#"+parentTable+" button[name^='transactioncoa']").append("<b>&nbsp;&nbsp;&#8711;</b>");
		}if(check_box_values==0){
			$("#"+parentTable+" button[name^='transactioncoa']").text("None Selected");
			$("#"+parentTable+" button[name^='transactioncoa']").append("<b>&nbsp;&nbsp;&#8711;</b>");
		}
		var value=$(elem).val();
		$("#"+parentTr+" input[id='coaAmountLimit"+value+"']").val("0.0");
		$("#"+parentTr+" input[id='coaAmountLimitTo"+value+"']").val("0.0");
	}
}

function transactiontoggleCheck(elem){
	var amountLimitVal=$(elem).val();
	var parentTr=$(elem).closest("ul").attr('id');
	var parentTable=$(elem).closest("table").attr('id');
	var value=$(elem).attr('id');
	var val="";
	if(value.indexOf("To")!=-1){
		val=value.substring(16,value.length);
	}else{
		val=value.substring(14,value.length);
	}
	if(amountLimitVal==""){
		$('#'+parentTr+' input[name="checkCOA"]').filter(function () {return $(this).val()==val;}).prop('checked', false);
		if(val==""){
			$('#'+parentTr+' input[name="checkCOA"]').each(function () {
		        $(this).prop("checked" ,false);
			});
			if(value.indexOf("To")==-1){
				$('#'+parentTr+' input[id*="coaAmountLimit"]').each(function () {
			        $(this).val("");
				});
			}
			if(value.indexOf("To")!=-1){
				$('#'+parentTr+' input[id*="coaAmountLimitTo"]').each(function () {
			        $(this).val("");
				});
			}
		}
	}else{
		$('#'+parentTr+' input[name="checkCOA"]').filter(function () {return $(this).val()==val;}).prop('checked', true);
		var amountLimit=$("#"+parentTr+" input[id*='coaAmountLimit']").val();
		var amountLimitTo=$("#"+parentTr+" input[id*='coaAmountLimitTo']").val();
		if(val==""){
			$('#'+parentTr+' input[name="checkCOA"]').each(function () {
		        $(this).prop("checked" ,true);
			});
			if(value.indexOf("To")==-1){
				$('#'+parentTr+' input[id*="coaAmountLimit"]').each(function () {
			        $(this).val(amountLimit);
				});
			}
			if(value.indexOf("To")!=-1){
				$('#'+parentTr+' input[id*="coaAmountLimitTo"]').each(function () {
			        $(this).val(amountLimitTo);
				});
			}
		}
	}
	var check_box_values = $('#'+parentTr+' input[name="checkCOA"]:checked').map(function () {
	    return this.value;
	}).get();
	var length=check_box_values.length;
	if(length>0){
		var text=length+" "+"Items Selected";
		$("#"+parentTable+" button[id^='transactioncoa']").text(text);
		$("#"+parentTable+" button[id^='transactioncoa']").append("<b>&nbsp;&nbsp;&#8711;</b>");
	}if(check_box_values==0){
		$("#"+parentTable+" button[id^='transactioncoa']").text("None Selected");
		$("#"+parentTable+" button[id^='transactioncoa']").append("<b>&nbsp;&nbsp;&#8711;</b>");
	}
}

function customdoctoggleCheck(elem){
	var unitPriceVal=$(elem).val();
	var value=$(elem).attr('id');
	var val=value.substring(13,value.length);
	if(unitPriceVal==""){
		$('input[name="customdoccheckBranch"][value='+val+']').prop('checked', false);
		$('input[name="customdoccheckBranch"][value='+val+']').attr('checked', false);
	}else{
		$('input[name="customdoccheckBranch"][value='+val+']').prop("checked" ,true);
		$('input[name="customdoccheckBranch"][value='+val+']').attr('checked', true);
	}
	var check_box_values = $('input[name="customdoccheckBranch"]:checkbox:checked').map(function () {
	    return this.value;
	}).get();
	var length=check_box_values.length;
	if(length>0){
		var text=length+" "+"Items Selected";
		$("#docuploadrulecustomdropdown").text(text);
		$("#docuploadrulecustomdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
	}if(check_box_values==0){
		$("#docuploadrulecustomdropdown").text("None Selected");
		$("#docuploadrulecustomdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
	}
}

function fillAllChartOfAccountLimit(elem){
	var amountLimitVal=$(elem).val();
	var parentTr=$(elem).closest("ul").attr('id');
	var elemId=$(elem).attr('id');
	if(elemId=="limitallfrom"){
		$('#'+parentTr+' input[id*="coaAmountLimit"]').each(function () {
	        $(this).val(amountLimitVal);
		});
	}
	if(elemId=="limitallto"){
		$('#'+parentTr+' input[id*="coaAmountLimitTo"]').each(function () {
	        $(this).val(amountLimitVal);
		});
	}
}

function fillAllInputBasedOnSelectAllinputValue(elem){
	var parentUl=$(elem).closest("ul").attr('id');
	var parentLi=$(elem).closest("li").attr('id');
	var parentCustomContainerDiv=$(elem).parent().parent().parent().attr('id');
	var firstSelectAllInputValue=$("#"+parentUl+" li[id="+parentLi+"] input[type='text']:nth-child(3)").val();
	var secondSelectAllInputValue=$("#"+parentUl+" li[id="+parentLi+"] input[type='text']:nth-child(4)").val();
	var currentID = $(elem).attr('id');
	$('#'+parentUl+' li input[type="text"]:nth-child(3)').each(function () {
		var otherID = $(this).attr('id');
		if(otherID !== currentID){
			$(this).val(firstSelectAllInputValue);
		}
	});
	$('#'+parentUl+' li input[type="text"]:nth-child(4)').each(function () {
		var otherID = $(this).attr('id');
		if(otherID !== currentID){
			$(this).val(secondSelectAllInputValue);
		}
	});
}


function showUserSetup(){
	$("#userRoleSpecs").hide();
	$("#usersSetup").show();
}

function userRoleSpecs(role){
	$("#role"+role).show();
}

$(document).ready(function(){
	$('#addUserSpecification'). click(function(){
	removeUserSpecs();
	var role3=$('#checkrole3').is(':checked');
	var role4=$('#checkrole4').is(':checked');
	var role5=$('#checkrole5').is(':checked');
		if(role3==true){
			if(saveUserSpecs(3)==true){
				return true;
			}
		}
		if(role4==true){
			if(saveUserSpecs(4)==true){
				return true;
			}
		}
		if(role5==true){
			if(saveUserSpecs(5)==true){
				return true;
			}
		}
	$("#usersSetup").show();
    $("#userRoleSpecs").hide();
    $("#notificationMessage").html("User Specification has been added/Updated successfully.");
	});
});

function removeUserSpecs()
{
	var userId = $("#userRoleHiddenId").val();
	var jsonData = {};
	jsonData.userId = userId;
	var url = "/config/removeUserRoleSpec";
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
			alwaysScrollTop();
		},
		error: function () {
			if(xhr.status == 401){ doLogout(); }
		}
 	});
}

function saveUserSpecs(id){
	var item   = $("#userItem"+id).val();
	var amount = $("#amount"+id).val();
	var userId = $("#userRoleHiddenId").val();
	if(item==null){
		swal("Invalid data field!!","Please Select a Valid Item","error");
		return true;
	}
	if(amount==""){
		swal("Invalid data field!!","Please Enter Amount","error");
		return true;
	}
	var items="";
    for(var i=0;i<item.length;i++){
		items=items+item[i]+",";
	}
	var jsonData = {};
		jsonData.items = items;
		jsonData.amount = amount;
		jsonData.userId = userId;
		jsonData.roleId = id;
		var url = "/config/addUserRoleSpec";
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
				alwaysScrollTop();
			},
			error: function () {
				if(xhr.status == 401){ doLogout(); }
			}
		 });
}


$(document).ready(function() {
	$(".vendCustresetlogincred").click(function(){
		var vendcustnewpassword=$("#vendCustresetnewpass").val();
		var vendcustconfirmpassword=$("#vendCustresetconfirmpass").val();
		if(vendcustnewpassword=="" || vendcustconfirmpassword==""){
			notifyLogin.show("Please provide new password and confirm the same.",true,true);
			return true;
		}
		if(vendcustnewpassword!=vendcustconfirmpassword){
			notifyLogin.show("Password and confirm password seems to be different.",true,true);
			return true;
		}
		var jsonData = {};
		jsonData.accountOrganization = GetURLParameter('accountOrganization');
		jsonData.accountEmail = GetURLParameter('accountEmail');
		jsonData.entityType = GetURLParameter('entityType');
		jsonData.accountPassword=vendcustnewpassword;
		var url="/account/resetVendorAccount";
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
				if(data.vendorCustomerAccountData[0].message=='success'){
					$("#vendCustresetnewpass").val("");
					$("#vendCustresetconfirmpass").val("");
					$("#vendCustForgotAccount").hide();
					$("#vendCustAccount").show();
					$("#vendcustloginsuccessaccountmsg").text("Successfully reset vendor/customer account.");
					notifyLogin.show("Successfully reset vendor/customer account.",true,false);
					$("#vendcustloginsuccessregdiv").show();
				}
				if(data.vendorCustomerAccountData[0].message=='failure'){
					$("#vendCustresetnewpass").val("");
					$("#vendCustresetconfirmpass").val("");
					$("#vendCustForgotAccount").show();
					$("#vendCustAccount").hide();
					$("#vendCustresetsuccessaccountmsg").text("Not able to locate vendor/customer account in idos.");
					notifyLogin.show("Not able to locate vendor/customer account in idos.",true,true);
					$("#vendCustresetsuccessregdiv").show();
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		})
	});
});


function login(){
	//alert("Loging") ;
	$("#successaccountmsg").html("");
	$("#accountspanstat").html("");
	$("#accountspanstat").html("Please log in to your account.");
	$("#companyname").val("");
	$("#companybnchname").val("");
	$("#corporateemail").val("");
	$("#orgpwd").val("");
	$("#dupchecklabel").html("");
	$("#forgotlogindiv").hide();
	$("#signUpDiv").hide();
	$("#logindiv").show();
	$("#newSignUp").show();
	$("#leftboxtd").attr("valign","bottom");
}

function signup(){
	//alert("signup") ;
	location.hash = "#signUpDiv";
	$("#successaccountmsg").html("");
	$("#accountspanstat").html("");
	$("#accountspanstat").html("Sign Up For Your Account.");
	$("#forgotlogindiv").hide();
	$("#signUpDiv").show();
	$("#logindiv").hide();
	$("#newSignUp").hide();
	$("#leftboxtd").attr("valign","middle");
}

function onlyDotsAndNumbers(event) {
	var k = event.which;
    var ok = k >= 48 && k <= 57  // 0-9
    || k == 46;   // dot
    if (!ok){
        event.preventDefault();
    }
}

function onlyDateAllow(event) {
        event.preventDefault();
}

function onlyDotsAndNumbersWithMultipleIDs(event, elem) {
	var enteredValue=$(elem).val();
    if(isNaN(enteredValue) == true){
		swal("Invalid data field!!","Only Decimals Value are Allowed","error");
		$(elem).val(0);
    	return false;
	}
    var charCode = (event.which) ? event.which : event.keyCode
    if (charCode == 46) {
    	console.log(enteredValue);
    	var n = enteredValue.split(".").length-1;
    	if(n>0){
    		swal("Invalid data field!!","Invalid Decimal Value Format,Please Provide Valid Decimal Value","error");
    		$(elem).val(0);
    		return false;
    	}else{
    		return true;
    	}
    }
    if (charCode > 31 && (charCode < 48 || charCode > 57)){
    	swal("Invalid data field!!","Only Decimals Value are Allowed","error");
    	$(elem).val(0);
        return false;
    }
    return true;
}



function onlyDotsAndNumbersandMinus(event, elem) {
	var enteredValue=$(elem).val();
	var charCode = (event.which) ? event.which : event.keyCode;
	if(isNaN(enteredValue) == true && enteredValue != "-"){
		swal("Invalid data field!!","Only Decimals Value are Allowed","error");
		$(elem).val(0.0);
    	return false;
	}


    if (charCode == 46) {
    	var n = enteredValue.split(".").length-1;
    	if(n>0){
    		swal("Invalid Decimal Value Format, Please Provide Valid Decimal Value", "Invalid Number", "error");
    		return false;
    	}else{
    		return true;
    	}
    }
	if (charCode == 45) {
    	//alert(enteredValue);
    	var n = enteredValue.split(".").length-1;
    	if(n>0){
    		swal("Invalid Decimal Value Format, Please Provide Valid Decimal Value", "Invalid Number", "error");
    		return false;
    	}else{
    		return true;
    	}
    }
    if (charCode > 31 && (charCode < 48 || charCode > 57)){
    	swal("Only Decimals Value are Allowed", "Invalid Number", "error");
        return false;
    }
    return true;
}


var allowOnlyNumbers = function(event){
	var k = event.which;
    var ok = k >= 48 && k <= 57; // 0-9
    if (!ok){
        event.preventDefault();
    }
}

var allowAlphaNumeric = function(event){
	var k = event.which;
    var ok = k >= 65 && k <= 90 || // A-Z
        k >= 97 && k <= 122 || // a-z
        k >= 48 && k <= 57; // 0-9

    if (!ok){
        event.preventDefault();
    }
}

var allowOnlyAlpha = function(event){
	var k = event.which;
    var ok = k >= 65 && k <= 90 || // A-Z
        k >= 97 && k <= 122 || // a-z
				(k == 32); // space

    if (!ok){
        event.preventDefault();
    }
}

var restrictNumbers = function(event){
	var k = event.which;
    var ok = k >= 48 && k <= 57;  // 0-9

    if (ok){
        event.preventDefault();
    }
}


function onlyBackAndDelete(event){
	var charCode = (event.which) ? event.which : event.keyCode
	if (charCode == 8 || charCode == 0) {
		return;
	}
	else{
		$("#"+event.id+"").val("");
		return false;
	}
	return true;
}

function clearDateFieldOnBachAndDelete(elem,event){
	var charCode = event.which;
	if (event.keyCode == 8 || event.keyCode == 46) {
		$(elem).val("");
	}
}

$(document).ready(function() {
	$(".myPicker").bind("keypress", function(event) {
		var charCode = event.which;
		var keyChar = String.fromCharCode(charCode);
		return /[]/.test(keyChar);
	}).on('keydown', function(e) {
		var id = this.id;
		if (e.keyCode == 8 || e.keyCode == 46) {
			$("#" + id + "").val("");
		}
	});
});

$(document).ready(function() {
	$(".datepicker").bind("keypress", function(event) {
		var charCode = event.which;
		var keyChar = String.fromCharCode(charCode);
		return /[]/.test(keyChar);
	}).on('keydown', function(e) {
		var id = this.id;
		if (e.keyCode == 8 || e.keyCode == 46) {
			$("#" + id + "").val("");
			if (id == "projectstartdate") {
				var maxYear=new Date().getFullYear()+30;
				$("#projectenddate").val("");
				$("#projectenddate").attr("class", "datepicker");
				$("#projectenddate").addClass('calendar');
				$(".datepicker").datepicker({
					changeMonth : true,
					changeYear : true,
					dateFormat : 'MM d,yy',
					yearRange: ''+new Date().getFullYear()-100+':'+maxYear+'',
					 onSelect: function(x,y){
							var elemName=$(this).attr('name');
							if(elemName=='projectenddate'){
								if($("#projectstartdate").val()==""){
									$(this).val("");
								}
								var d1 = new Date($("#projectenddate").val())
								var d2 = new Date($("#projectstartdate").val())
								if (d1 < d2) {
									swal("Invalid data field!!","project end date cannot be less than project start date","error");
								   $(this).val("");
								   return true;
								}
							}
							$(this).focus();
						}
				});
			}
			if(id=="start-date"){
				var maxYear=new Date().getFullYear()+30;
		        $("#end-date").val("");
		    	$("#end-date").attr("class","");
		        $("#end-date" ).datepicker({
		             changeMonth : true,
		             changeYear: true,
		             minDate: new Date($("#start-date").val()),
		             dateFormat:  'MM d,yy',
		             yearRange: ''+new Date().getFullYear()-100+':'+maxYear+'',
		             onSelect: function(x,y){
		      	        $(this).focus();
		      	     }
		        });
			}
			if(id=="agreementValidityFrom"){
				var maxYear=new Date().getFullYear()+30;
		        $("#agreementValidityTo").val("");
		        $("#agreementValidityTo").attr("class","");
		        $("#agreementValidityTo" ).datepicker({
		             changeMonth : true,
		             changeYear: true,
		             minDate: new Date($("#agreementValidityFrom").val()),
		             dateFormat:  'MM d,yy',
		             yearRange: ''+new Date().getFullYear()-100+':'+maxYear+'',
		             onSelect: function(x,y){
		      	        $(this).focus();
		      	     }
		        });
		        var datepickerclass=$("#agreementValidityTo").attr("class");
		        datepickerclass+=" "+"input-medium";
		        $("#agreementValidityTo").attr("class",datepickerclass);
			}
			if (id == "fincstartyear") {
				var maxYear=new Date().getFullYear()+30;
				$("#fincendyear").val("");
				$("#fincendyear").attr("class", "datepicker");
				$(".datepicker").datepicker({
					changeMonth : true,
					changeYear : false,
					dateFormat : 'MM d',
					 yearRange: ''+new Date().getFullYear()-100+':'+maxYear+'',
					beforeShow: function (input, inst) {
			             inst.dpDiv.addClass('fnchDatePicker');
			         },
			         onClose: function(dateText, inst){
			             inst.dpDiv.removeClass('fnchDatePicker');
			         },
			         onSelect: function(x,y){
			     	     $(this).focus();
			     	 }
				});
			}
		}
	});
});

$(document).ready(function() {
	$(".fincyear").bind("keypress", function(event) {
		var charCode = event.which;
		var keyChar = String.fromCharCode(charCode);
		return /[]/.test(keyChar);
	}).on('keydown', function(e) {
		var id = this.id;
		if (e.keyCode == 8 || e.keyCode == 46) {
			$("#" + id + "").val("");
		}
	});
});

function isNumber(evt) {
    evt = (evt) ? evt : window.event;
    var charCode = (evt.which) ? evt.which : evt.keyCode;
    if (charCode > 31 && (charCode < 48 || charCode > 57)) {
    	swal("Invalid data field!!","Only Numeric Value Allowed","error")
        return false;
    }
    return true;
}


function onlyBackAndDelete(event){
	var charCode = (event.which) ? event.which : event.keyCode
	if (charCode == 8 || charCode == 0) {
		return;
	}
	else{
		$("#"+event.id+"").val("");
		return false;
	}
	return true;
}

function checkusername(elem){
	var parentDiv=$(elem).parent("div:first").attr('class');
	var username=$('input[name="username"]').val();
	var jsonData = {};
	jsonData.uName = username;
	var url="/users/checkuser";
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
			$("#dupchecklabel").html("");
			if(data.userExistData[0].dupusrmessage!='Username Available.'){
				$("."+parentDiv+" b[id='dupchecklabel']").append('<b><font color="red">'+data.userExistData[0].dupusrmessage+'</p></b>');
			}else{
				$("."+parentDiv+" b[id='dupchecklabel']").html("");
			}
			alwaysScrollTop();
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}


function getExistedEmail(elem){
	var existedValue=$(elem).val();
	enteredUserEmail=existedValue;
}

function getExistedData(elem){
	var existedValue=$(elem).val();
	enteredBranchSpecifics=existedValue;
}

var checkEditEmailResult = "";
function checkEditEmail(elem){
	if ("" === elem) {
		checkEditEmailResult = true;
		return;
	}
	var emailid=$(elem).val();
	if(enteredUserEmail==emailid){
		enteredUserEmail="";
		checkEditEmailResult = true;
		return;
	}
	if(enteredUserEmail!=emailid){
		if(emailid.length>1){
			var jsonData = {};
			jsonData.email = emailid;
			var url="/users/checkemail";
			$.ajax({
				url: url,
				data:JSON.stringify(jsonData),
				type:"text",
				headers:{
					"X-AUTH-TOKEN": window.authToken
				},
				method:"POST",
				contentType:'application/json',
				async: false,
				success: function (data) {
					$("#dupemaillabel").html("");
					if(data.userExistData[0].dupusrmessage!='Email Available.'){
						if(enteredUserEmail==""){
							checkEditEmailResult = true;
						}else{
							$(elem).val("");
							$(elem).focus();
							$('html, body').animate({
							       scrollTop: $(elem).offset().top-50
							}, 2000);
							checkEditEmailResult = false;
						}
					}
					if(data.userExistData[0].dupusrmessage=='Email Available.'){
						checkEditEmailResult = true;
					}
				},
				error: function (xhr, status, error) {
					if(xhr.status == 401){ doLogout(); }
				}
			});
		}else{
			checkEditEmailResult = true;
		}
	}
}


function getBranchData(){
	var jsonData = {};
	var useremail=$("#hiddenuseremail").text();
	jsonData.usermail = useremail;
	var url="/user/getBranchData";
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
			$("#searchBranch").children().remove();
			//$("#searchBranch").append('<option value="">--Please select--</option>');
			$(".bnchCashnBankTrialBalance").children().remove();
			$(".bnchCashnBankTrialBalance").append('<option value="">--Please select--</option>');
			$("#mtefpeTxnForBranchesDebit").children().remove();
			$("#mtefpeTxnForBranchesDebit").append('<option value="">--Please select--</option>');
			$("#mtefpeTxnForBranchesCredit").children().remove();
			$("#mtefpeTxnForBranchesCredit").append('<option value="">--Please select--</option>');
			customMethod7();
			for(var i=0;i<data.branchData.length;i++){
				$("#searchBranch").append('<option value="'+data.branchData[i].id+'">'+data.branchData[i].name+'</option>');
				$(".bnchCashnBankTrialBalance").append('<option value="'+data.branchData[i].id+'">'+data.branchData[i].name+'</option>');
				$("#mtefpeTxnForBranchesDebit").append('<option value="'+data.branchData[i].id+'">'+data.branchData[i].name+'</option>');
				$("#mtefpeTxnForBranchesCredit").append('<option value="'+data.branchData[i].id+'">'+data.branchData[i].name+'</option>');
			}
			$('#searchBranch').multiselect('rebuild');
			$('.bnchCashnBankTrialBalance option:eq(1)').attr('selected','selected');
			var noOfOptions = $('#selectBranchTrialBalanceId option').length;
			if(parseInt(noOfOptions) == 2){
				$('#selectBranchTrialBalanceId option:eq(1)').attr("selected", true);
				$('#selectBranchCashBankBookId option:eq(1)').attr("selected", true);
				$('#selectBranchCashBookId option:eq(1)').attr("selected", true);
				$('#selectBranchCashBankBookId').trigger("change");
			}
			else{
				$('#selectBranchTrialBalanceId option:first').attr('selected',true);
				$('#selectBranchCashBankBookId option:first').attr('selected',true);
				$('#selectBranchCashBookId option:first').attr('selected',true);
			}
		},
		error: function(xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}


function getProjectData(){
	//alert("getProjectData");
	var jsonData = {};
	var useremail=$("#hiddenuseremail").text();
	jsonData.usermail = useremail;
	var url="/user/getProjectData"
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
				$("#searchProject").children().remove();
				//$("#searchProject").append('<option value="">--Please select--</option>');
				for(var i=0;i<data.projectData.length;i++){
					$("#searchProject").append('<option value="'+data.projectData[i].id+'">'+data.projectData[i].name+'</option>');
				}
				$('#searchProject').multiselect('rebuild');
			},
			error: function(xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
}

function getCategoryData(){
	//alert("getCategoryData") ;
	var jsonData = {};
	var useremail=$("#hiddenuseremail").text();
	jsonData.usermail = useremail;
	var url="/user/getParticularsData"
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
				$("#searchCategory").children().remove();
				$("#searchCategory").append('<option value="">--Please select--</option>');
				for(var i=0;i<data.particularsData.length;i++){
					$("#searchCategory").append('<option value="'+data.particularsData[i].id+'">'+data.particularsData[i].name+'</option>');
				}
			},
			error: function(xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
}

$(document).ready(function(){
	$("select[name='projectpositionqualification']").multiselect({
			        buttonWidth: '150px',
			        maxHeight:   150,
			        includeSelectAllOption: true,
			        enableFiltering :true,
			        /*
			        buttonText: function(options) {
			          if (options.length == 0) {
			                  return 'None selected <b class="caret"></b>';
			              }
			              else if (options.length > 6) {
			                  return options.length + ' selected  <b class="caret"></b>';
			              }
			              else {
			                  var selected = '';
			                  options.each(function() {
			              selected += $(this).text() + ', ';
			                  });

			                  return options.length + ' selected  <b class="caret"></b>';
			          }
			        },*/
			        onChange: function(element, checked) {
			        }
		});
});

$(document).ready(function() {
	$(".addnew").click(function(){
		var arefid=this.id;
		var locHash=location.hash;
		if(arefid=="newWarehouseSection"){
			var length=$("#warehouseBinSectionTable tbody tr").length;
			var content=$("#warehouseBinSectionTable tbody tr[id='binSection1']").html();
			$("#warehouseBinSectionTable tbody").append('<tr id="dynWarehouseBin'+length+'">'+content+'</tr>');
			$("#warehouseBinSectionTable tbody tr[id='dynWarehouseBin"+length+"'] input[type='hidden']").val("");
		}else if(arefid=="newCashCountNotesId"){
			var length=$("#cashCountNotesDetailsTable tbody tr").length;
			var content=$("#cashCountNotesDetailsTable tbody tr[id='cashCountNotesRow']").html();
			$("#cashCountNotesDetailsTable tbody").append('<tr id="dyncashCountNotesRow'+length+'">'+content+'</tr>');
			$("#cashCountNotesDetailsTable tbody tr[id='dyncashCountNotesRow"+length+"'] input[type='hidden']").val("");
		}else if(arefid=="newCashCountCoinsId"){
			var length=$("#cashCountCoinDetailsTable tbody tr").length;
			var content=$("#cashCountCoinDetailsTable tbody tr[id='cashCountCoinsRow']").html();
			$("#cashCountCoinDetailsTable tbody").append('<tr id="dyncashCountCoinsRow'+length+'">'+content+'</tr>');
			$("#cashCountCoinDetailsTable tbody tr[id='dyncashCountCoinsRow"+length+"'] input[type='hidden']").val("");
		}else if(arefid=="newKeyOffId"){//for dynm branch officer
			var length=$("#branchOfficersTable tbody tr").length;
			var content=$("#branchOfficersTable tbody tr[id='copyContentBranchOfficer']").html();
			$("#branchOfficersTable tbody").append('<tr id="dynBranchOfficer'+length+'">'+content+'</tr>');
			$("#branchOfficersTable tbody tr").last().find(".keyOffIdProof").addClass("keyOffIdProof"+length);
			$("#branchOfficersTable tbody tr").last().find(".keyOffIdProof").removeClass("keyOffIdProof");
			$("#branchOfficersTable tbody tr").last().find("#keyOffIdProof").attr("id","keyOffIdProof"+length);
			$("#branchOfficersTable tbody tr[id='dynBranchOfficer"+length+"'] input[type='hidden']").val("");
			$("#branchOfficersTable tbody tr[id='dynBranchOfficer"+length+"'] input[name='keyoffEmailId']").removeAttr("disabled");
		}else if(arefid=="newProjectHiringId"){
			var length=$("#projectPositionFormTable tbody tr").length;
			var content=$("#projectPositionFormTable tbody tr[id='copyContentProjectPosition']").html();
			$("#projectPositionFormTable tbody").append('<tr id="dynProjPos'+length+'">'+content+'</tr>');
			$("#projectPositionFormTable tbody tr[id='dynProjPos"+length+"'] input[type='hidden']").val("");
			var i=0;
			$("#projectPositionFormTable tbody input[class*='datepicker']").each(function(){
				i++;
				$(this).attr('class','datepicker');
				var attrId=$(this).attr('id');
				var newAttrId=attrId+i;
				$(this).attr('id',newAttrId)
			});
			var maxYear=new Date().getFullYear()+30;
			$(function() {
				$("#projectPositionFormTable tbody input[class*='datepicker']").datepicker({
					changeMonth : true,
					changeYear : true,
					dateFormat:  'MM d,yy',
					yearRange: ''+new Date().getFullYear()-100+':'+maxYear+''
				});
			});
			$("#projectPositionFormTable tbody tr[id='dynProjPos"+length+"'] div[class='btn-group']").each(function(){
				$(this).remove();
			});
			$("#projectPositionFormTable tbody tr[id='dynProjPos"+length+"'] select[name='projectpositionqualification'] option:selected").each(function () {
				$(this).removeAttr('selected');
			});
			$("#projectPositionFormTable tbody select[name='projectpositionqualification']").each(function(){
				var trId=$(this).parent().parent('tr:first').attr('id');
				$('#'+trId+' select[name="projectpositionqualification"]').multiselect({
					buttonWidth: '150px',
					maxHeight:   150,
					includeSelectAllOption: true,
					enableFiltering :true,
					/*
					buttonText: function(options) {
					  if (options.length == 0) {
							  return 'None selected <b class="caret"></b>';
						  }
						  else if (options.length > 6) {
							  return options.length + ' selected  <b class="caret"></b>';
						  }
						  else {
							  var selected = '';
							  options.each(function() {
						  selected += $(this).text() + ', ';
							  });

							  return options.length + ' selected  <b class="caret"></b>';
					  }
					}, */
					onChange: function(element, checked) {
					}
				  });
			});
		}else if(arefid=="newBnchStatutoryId"){//for dynm branch statutory
			var length=$("#branchStatutoryTable tbody tr").length;
			var content=$("#branchStatutoryTable tbody tr[id='copyContentBranchStatutory']").html();
			$("#branchStatutoryTable tbody").append('<tr id="dynBranchStatutory'+length+'">'+content+'</tr>');
			$("#branchStatutoryTable tbody tr").last().find(".bnchStatutorySupportingDoc").addClass("bnchStatutorySupportingDoc"+length);
			$("#branchStatutoryTable tbody tr").last().find(".bnchStatutorySupportingDoc").removeClass("bnchStatutorySupportingDoc");
			$("#branchStatutoryTable tbody tr").last().find("#bnchStatutorySupportingDoc").attr("id","bnchStatutorySupportingDoc"+length);
			$("#branchStatutoryTable tbody tr[id='dynBranchStatutory"+length+"'] input[type='hidden']").val("");
			var i=0;
			$("#branchStatutoryTable tbody input[class*='datepicker']").each(function(){
				i++;
				$(this).attr('class','datepicker m-bottom-10 calendar');
				var attrId=$(this).attr('id');
				var newAttrId=attrId+i;
				$(this).attr('id',newAttrId)
			});
			var maxYear=new Date().getFullYear()+30;
			$(function() {
				$("#branchStatutoryTable tbody input[class*='datepicker']").datepicker({
					changeMonth : true,
					changeYear : true,
					dateFormat:  'MM d,yy',
					yearRange: ''+new Date().getFullYear()-100+':'+maxYear+'',
					onSelect: function(x,y){
						var elemName=$(this).attr('name');
						if(elemName=='bnchStatutoryvalidTo'){
							var parentTr=$(this).parent().parent().attr('id');
							if($("#"+parentTr+" input[id='bnchStatutoryvalidFrom']").val()==""){
								$(this).val("");
							}
							var d1 = new Date($("#"+parentTr+" input[name='bnchStatutoryvalidTo']").val())
							var d2 = new Date($("#"+parentTr+" input[name='bnchStatutoryvalidFrom']").val())
							if (d1 < d2) {
								swal("Invalid data field!!","Branch Statutory validity To cannot be less than validity from","error");
							   $(this).val("");
							   return true;
							}
						}
						$(this).focus();
					}
				});
				$("#branchStatutoryTable tbody input[class*='datepicker']").bind("keypress", function(event) {
					var charCode = event.which;
					var keyChar = String.fromCharCode(charCode);
					return /[]/.test(keyChar);
				}).on('keydown', function(e) {
					var id = this.id;
					if (e.keyCode == 8 || e.keyCode == 46) {
						$("#" + id + "").val("");
					}
				});
			});
		}else if(arefid=="newOperRemId"){//for dynm operational remainders
			var length=$("#branchOperationalRemainderTable tbody tr").length;
			var content=$("#branchOperationalRemainderTable tbody tr[id='copyContentBranchOpRem']").html();
			$("#branchOperationalRemainderTable tbody").append('<tr id="dynBranchOperRem'+length+'">'+content+'</tr>');
			$("#branchOperationalRemainderTable tbody tr[id='dynBranchOperRem"+length+"'] input[type='hidden']").val("");
			var i=0;
			$("#branchOperationalRemainderTable tbody input[class*='datepicker']").each(function(){
				i++;
				$(this).attr('class','datepicker');
				var attrId=$(this).attr('id');
				var newAttrId=attrId+i;
				$(this).attr('id',newAttrId)
			});
			var maxYear=new Date().getFullYear()+30;
			$(function() {
				$("#branchOperationalRemainderTable tbody input[class*='datepicker']").datepicker({
					changeMonth : true,
					changeYear : true,
					dateFormat:  'MM d,yy',
					yearRange: ''+new Date().getFullYear()-100+':'+maxYear+'',
					onSelect: function(x,y){
						$(this).focus();
					}
				});
			});
		}
		/* Not needed confirmed on 17 June 2016 only one record needs to be added.
		if(arefid=="newSafeDepositBoxId"){
			var length=$("#branchSafeDepositBoxTable tbody tr").length;
			var content=$("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep']").html();
			$("#branchSafeDepositBoxTable tbody").append('<tr id="dynBranchKeyDep'+length+'">'+content+'</tr>');
			$("#branchSafeDepositBoxTable tbody tr[id='dynBranchKeyDep"+length+"'] input[type='hidden']").val("");
			$("#branchSafeDepositBoxTable tbody tr[id='dynBranchKeyDep"+length+"'] td[class='cashierData']").each(function(){
				$(this).html("");
			})
		} */
		else if(arefid=="newBranchInsurance"){
			var length=$("#branchInsurenceTable tbody tr").length;
			var content=$("#branchInsurenceTable tbody tr[id='copyBranchInsurence']").html();
			$("#branchInsurenceTable tbody").append('<tr id="dynBranchInsurence'+length+'">'+content+'</tr>');
			$("#branchInsurenceTable tbody tr").last().find(".insurancePolicy").addClass("insurancePolicy"+length);
			$("#branchInsurenceTable tbody tr").last().find(".insurancePolicy").removeClass("insurancePolicy");
			$("#branchInsurenceTable tbody tr").last().find("#insurancePolicy").attr("id","insurancePolicy"+length);
			$("#branchInsurenceTable tbody tr[id='dynBranchInsurence"+length+"'] input[type='hidden']").val("");
			var i=0;
			$("#branchInsurenceTable tbody input[class*='datepicker']").each(function(){
				i++;
				$(this).attr('class','datepicker calendar m-bottom-10');
				var attrId=$(this).attr('id');
				var newAttrId=attrId+i;
				$(this).attr('id',newAttrId)
			});
			var maxYear=new Date().getFullYear()+30;
			$(function() {
				$("#branchInsurenceTable tbody input[class*='datepicker']").datepicker({
					changeMonth : true,
					changeYear : true,
					dateFormat:  'MM d,yy',
					yearRange: ''+new Date().getFullYear()-100+':'+maxYear+'',
					onSelect: function(x,y){
						var elemName=$(this).attr('name');
						if(elemName=='insuranceValidityTo'){
							var parentTr=$(this).parent().parent().attr('id');
							if($("#"+parentTr+" input[id='insuranceValidityFrom']").val()==""){
								$(this).val("");
							}
							var d1 = new Date($("#"+parentTr+" input[name='insuranceValidityTo']").val())
							var d2 = new Date($("#"+parentTr+" input[name='insuranceValidityFrom']").val())
							if (d1 < d2) {
								swal("Invalid data field!!","Branch Insurence validity To cannot be less than validity from","error");
							   $(this).val("");
							   return true;
							}
						}
						$(this).focus();
					}
				});
				$("#branchInsurenceTable tbody input[class*='datepicker']").bind("keypress", function(event) {
					var charCode = event.which;
					var keyChar = String.fromCharCode(charCode);
					return /[]/.test(keyChar);
				}).on('keydown', function(e) {
					var id = this.id;
					if (e.keyCode == 8 || e.keyCode == 46) {
						$("#" + id + "").val("");
					}
				});
			});
		}else if(arefid=="newBankAccountId"){
			var length=$("#branchBankAccountTable tbody tr").length;
			var content=$("#branchBankAccountTable tbody tr[id='copyBranchBankAccount']").html();
			$("#branchBankAccountTable tbody").append('<tr id="dynBranchBnkAct'+length+'">'+content+'</tr>');
			$("#branchBankAccountTable tbody tr[id='dynBranchBnkAct"+length+"'] input[type='hidden']").val("");
			var i=0;
			$("#branchBankAccountTable tbody input[class*='datepicker']").each(function(){
				i++;
				$(this).attr('class','datepicker calendar m-bottom-10');
				var attrId=$(this).attr('id');
				var newAttrId=attrId+i;
				$(this).attr('id',newAttrId)
			});
			var maxYear=new Date().getFullYear()+30;
			$(function() {
				$("#branchBankAccountTable tbody input[class*='datepicker']").datepicker({
					changeMonth : true,
					changeYear : true,
					dateFormat:  'MM d,yy',
					yearRange: ''+new Date().getFullYear()-100+':'+maxYear+'',
					onSelect: function(x,y){
					   $(this).focus();
					}
				});
			});
		}else if(arefid=="newBranchTaxId"){
			var length=$("#branchTaxTable tbody tr").length;
			var content=$("#branchTaxTable tbody tr[id='copyBranchTax']").html();
			$("#branchTaxTable tbody").append('<tr id="dynBranchTax'+length+'">'+content+'</tr>');
			$("#branchTaxTable tbody tr[id='dynBranchTax"+length+"'] input[type='hidden']").val("");
		}else if(arefid=="newBranchInputTaxId"){
			var length=$("#branchInputTaxTable tbody tr").length;
			var content=$("#branchInputTaxTable tbody tr[id='copyBranchInputTax']").html();
			$("#branchInputTaxTable tbody").append('<tr id="dynBranchInputTax'+length+'">'+content+'</tr>');
			$("#branchInputTaxTable tbody tr[id='dynBranchInputTax"+length+"'] input[type='hidden']").val("");
		}else if(arefid=="newVendorChartOfAccountId"){//add more Vendor/Customer
			var length=$("#chartOfAccountVendorCustomerTable tbody tr").length;
			var content=$("#chartOfAccountVendorCustomerTable tbody tr[id='copyContentCustomerVendor']").html();
			$("#chartOfAccountVendorCustomerTable tbody").append('<tr id="dynCustomerVendor'+length+'">'+content+'</tr>');
			$("#chartOfAccountVendorCustomerTable tbody tr[id='dynCustomerVendor"+length+"'] input[type='hidden']").val("");
			$("#chartOfAccountVendorCustomerTable tbody tr[id='dynCustomerVendor"+length+"'] div[class='btn-group']").each(function(){
				$(this).remove();
			});
			$("#chartOfAccountVendorCustomerTable tbody tr[id='dynCustomerVendor"+length+"'] select[class='multiBranch'] option:selected").each(function () {
				$(this).removeAttr('selected');
			});
			$("#chartOfAccountVendorCustomerTable tbody select[class='multiBranch']").each(function(){
				var trId=$(this).parent().parent('tr:first').attr('id');
				$('#'+trId+' select[class="multiBranch"]').multiselect({
					buttonWidth: '150px',
					maxHeight:   150,
					includeSelectAllOption: true,
					enableFiltering :true,
					/*
					buttonText: function(options) {
					  if (options.length == 0) {
							  return 'None selected <b class="caret"></b>';
						  }
						  else if (options.length > 6) {
							  return options.length + ' selected  <b class="caret"></b>';
						  }
						  else {
							  var selected = '';
							  options.each(function() {
						  selected += $(this).text() + ', ';
							  });

							  return options.length + ' selected  <b class="caret"></b>';
					  }
					}, */
					onChange: function(element, checked) {
					}
				  });
			});
			var i=0;
			$("#chartOfAccountVendorCustomerTable tbody input[class*='datepicker']").each(function(){
				i++;
				$(this).attr('class','datepicker calendar m-bottom-10');
				var attrId=$(this).attr('id');
				var newAttrId=attrId+i;
				$(this).attr('id',newAttrId)
			});
			var maxYear=new Date().getFullYear()+30;
			$(function() {
				$("#chartOfAccountVendorCustomerTable tbody input[class*='datepicker']").datepicker({
					changeMonth : true,
					changeYear : true,
					dateFormat:  'MM d,yy',
					yearRange: ''+new Date().getFullYear()-100+':'+maxYear+'',
					onSelect: function(x,y){
					   $(this).focus();
					}
				});
			});
		}else if(arefid=="newKnowledgeLibraryChartOfAccountId"){//add more KnowledgeLibrary
			var length=$("#chartOfAccountKnowledgeLibraryTable tbody tr").length;
			var content=$("#chartOfAccountKnowledgeLibraryTable tbody tr[id='copyContentKnowledgeLibrary']").html();
			$("#chartOfAccountKnowledgeLibraryTable tbody").append('<tr id="dynKnowledgeLibrary'+length+'">'+content+'</tr>');
			$("#chartOfAccountKnowledgeLibraryTable tbody tr[id='dynKnowledgeLibrary"+length+"'] input[type='hidden']").val("");
			$("#chartOfAccountKnowledgeLibraryTable tbody tr[id='dynKnowledgeLibrary"+length+"'] div[class='btn-group']").each(function(){
				$(this).remove();
			});
			$("#chartOfAccountKnowledgeLibraryTable tbody tr[id='dynKnowledgeLibrary"+length+"'] select[class='multiBranch'] option:selected").each(function () {
				$(this).removeAttr('selected');
			});
			var newTrId = "dynKnowledgeLibrary"+length;
			//$("#chartOfAccountKnowledgeLibraryTable tbody select[class='multiBranch']").each(function(){
				//var trId=$(this).closest('tr:first').attr('id');
				$('#'+newTrId+' select[class="multiBranch"]').multiselect({
					buttonWidth: '150px',
					maxHeight:   150,
					includeSelectAllOption: true,
					enableFiltering :true,
					onChange: function(element, checked) {
					}
				  });
			//});
			//$('.multiBranch').multiselect('rebuild');
		}
	});
});

function openOldAccount(oldAct,dynmBnkAct) {
   $("a[id="+oldAct+"]").attr("class","oldBankAccount link-old");
   $("a[id="+oldAct+"]").attr("onClick","closeOldAccount('"+oldAct+"','"+dynmBnkAct+"')");
   if (!$("div[id="+oldAct+"]").is(':visible')){
	   $("div[id="+oldAct+"]").slideDown('slow');
	   if($('a[id="removeBankAccountId"]').is(':visible')==true){
		   $("div[id="+oldAct+"]").after('<h5 id="bnkLabel'+dynmBnkAct+'" class="alert success"><b>Branch Bank Account Details</b></h5>');
	   }
   }
}

function removeNewKeyOff(dynmkeyoff) {
	$("#dynmKeyOfficialDiv"+dynmkeyoff+"").remove();
	$("#break"+dynmkeyoff+"").remove();
}

function removeBranchInsurance(dynmbnchIns) {
	$("#dynmbnchInsuranceDetail"+dynmbnchIns+"").remove();
	$("#breakbnchins"+dynmbnchIns+"").remove();
}

function removeNewBankAccount(dynmBnkAct){
	$("div[id='dynmbranchBankAccountDiv"+dynmBnkAct+"']").remove();
	var inc=dynmBnkAct+1;
	$("a[name='addView"+inc+"']").remove();
	$("#breakact"+dynmBnkAct+"").remove();
	$("#bnkLabel"+dynmBnkAct+"").remove();
}

function closeOldAccount(oldAct,dynmBnkAct){;
	if($("div[id="+oldAct+"]").is(':visible')){
		$("div[id="+oldAct+"]").slideUp('slow');
		$("a[id="+oldAct+"]").attr("class","oldBankAccount link-new");
		$("a[id="+oldAct+"]").attr("onClick","openOldAccount('"+oldAct+"','"+dynmBnkAct+"')");
		$("#bnkLabel"+dynmBnkAct+"").remove();
	}
}

$(document).ready(function() {
	$("button[name='cancelTxn']").click(function(){
		$("#usercatcreate").hide();
		$("#useritemcreate").hide();
		$("#uservendorcreate").hide();
	});
});

$(document).ready(function() {
	$('.hintPopOver').each(function() {
	      var $this = $(this);
	      $this.popover({
	      trigger: 'hover',
	      placement: 'right'
	    });
	});
});

$(document).ready(function() {
	$('.logoPopOver').each(function() {
		  var locHash=location.hash;
		  if(locHash==""){
			  locHash="#categorySetUp";
		  }
	      var $this = $(this);
	      $this.popover({
	      trigger: 'manual',
          animate: false,
	      placement: 'right',
	      html: true,
	      content: '<a href='+locHash+' class="logo-upload-button" id="nav-upload-logo" onClick="openLogoDiv();">Upload your logo</a>'
	    }).on("mouseenter", function() {
	          if($("#customize-account-dialog").is(":hidden")){
	    	  $(this).popover("show");
	    	  $(".logo-upload-button").attr("href",location.hash);
	    	  $("#logo-browse-link").attr("href",location.hash);
	    	  $(this).siblings(".popover").on("mouseleave", function() {
	    	    $(this).hide();
	    	  });
	    	  }
	    }).on("mouseleave", function() {
	    	  var _this = this;
	    	  setTimeout(function() {
	    	  if (!$(".popover:hover").length) {
	    	     $(_this).popover("hide")
	    	  }
	    	}, 100);
	    });
	});
});

$(document).ready(function() {
	$('#companyActSetting').each(function() {
		  var locHash=location.hash;
		  if(locHash==""){
			  locHash="#categorySetUp";
		  }
	      var $this = $(this);
	      $this.popover({
	      trigger: 'manual',
          animate: false,
	      placement: 'bottom',
	      html: true,
	      content: '<ul style="list-style-type:none;"><li><a href="#companyDetails" class="accountSetting" id="companySetting">Company Settings</a></li><li><a href='+location.hash+' class="accountSetting" id="changePassword" onClick="openChangePwd();">Change Password</a></li></ul>'
	    }).on("mouseenter", function() {
	    	  $(this).popover("show");
	    	  $(this).siblings(".popover").on("mouseleave", function() {
	    	    $(this).hide();
	    	  });
	    }).on("mouseleave", function() {
	    	  var _this = this;
	    	  setTimeout(function() {
	    	  if (!$(".popover:hover").length) {
	    	     $(_this).popover("hide")
	    	  }
	    	}, 100);
	    });
	});
});

function openLogoDiv(){
	$(".close-button-blue").attr("href",location.hash);
	$('.logoPopOver').popover('hide');
	$("#customize-account-dialog").show();
}

$(document).ready(function() {
	$(".close-button-blue").click(function(){
		$("#customize-account-dialog").hide();
		$('#browse').show();
        $('#logo').hide();
		$('#compLogo').hide();
		$("#upload-logo").val(null);
    });
});

$(document).ready(function(){
	$('#upload-logo').change(function() {
	        if($("#upload-logo").val()!=null){
	        $('#browse').hide();
			$('#logo').hide();
	        $('#loading').show();
	        $(".imgLiquidFill").imgLiquid({fill:true});
	    	var form=$('#logo-upload-dialog');
			var data = new FormData();
			jQuery.each($('#upload-logo')[0].files, function(i, file) {
			    data.append('file-'+i, file);
			});
			var input = document.getElementById('upload-logo');
	        var fReader = new FileReader();
	        fReader.readAsDataURL(input.files[0]);
	        fReader.onloadend = function(event){
			$.ajax({
				method: "POST",
				url: form.attr('action'),
				data: data,
				headers:{
					"X-AUTH-TOKEN": window.authToken
				},
				cache: false,
			    contentType: false,
			    processData: false,
				success: function(data) {
			    $('#compLogo').attr('src',event.target.result);
			    $(".imgLiquidFill").imgLiquid({fill:true});
			    $('#loading').hide();
			    $('#logo').show();
			    $('#companyLogo').attr('src',event.target.result);
			    $(".imgLiquidFill").imgLiquid({fill:true});
			    $("#upload-logo").val(null);
			    alwaysScrollTop();
			    },
				error: function (xhr, status, error) {
					if(xhr.status == 401){ doLogout(); }
				}
				});
			}
			}
		});
	});

function browseLogo(){
    $('input[type=file]').click();
    return false;
}

$(document).ready(function(){
    $('.link_blue').click(function(){
    	$('input[type=file]').click();
        return false;
    });
});

$(document).ready(function() {
	$(".staticsyscnf").click(function(){
		var chartOfAccountstatval=$('button[id=radiochecked][name=chartOfAccountradio]').val();
		var orgnId= $("#orgnid").val();
		if(chartOfAccountstatval==undefined){
			swal("Error","Please tell us that your company has chart of accounts or not?","error")
			return true;
		}
		var url="/config/updateOrganization";
		var jsonData = {};
		if(chartOfAccountstatval==1){
			var chatofacturl=$("#uploadchartofact").val();
			if(chatofacturl==""){
				swal("Error!","please upload your company chart of accounts","error");
				return true;
			}
			var ext = chatofacturl.substring(chatofacturl.lastIndexOf('.') + 1);
			if(ext != "xls" && ext != "xlsx"){
				swal("Error!","Only Excel files are allowed for chart of account upload","error");
				$("#uploadchartofact").val("");
				return true;
			}
			else{
				jsonData.chtOfActStatus = chartOfAccountstatval;
				jsonData.organizationId=orgnId;
			}
		}
		if(chartOfAccountstatval==0){
			//send to server radio button values and perform update on organization
			var parentChAcc = $('input[name="parentCat"]').map(function () {
		             	return this.value;
		             }).get();
		    //var bankActSubCat = $('input[name="bankActSubCat"]').map(function () {
		     //        	return this.value;
		    //         }).get();
		    //var bankAccNo = $('input[name="bankAccNo"]').map(function () {
		     //        	return this.value;
		     //        }).get();
           // var capActSubCat = $('input[name="capActSubCat"]').map(function () {
		    //         	return this.value;
		   //          }).get();
		   // var fxdAstSubCat = $('input[name="fxdAstSubCat"]').map(function () {
		    //         	return this.value;
		   //          }).get();
		   // var currentAstSubCat=$('input[name="curntAstSubCat"]').map(function () {
             //			return this.value;
            //		 }).get();
		    var currentLiabSubCat=$('input[name="curntLiabSubCat"]').map(function () {
			        	return this.value;
	         		}).get();
		    //var curntLiabProvisionSubCat=$('input[name="curntLiabProvisionSubCat"]').map(function () {
	        //			return this.value;
     		//		}).get();
		    var directExpSubCat=$('input[name="directexpSubCat"]').map(function () {
    					return this.value;
					}).get();
		    var directExpenseUtilitiesSubCat=$('input[name="directExpenseUtilitiesSubCat"]').map(function () {
						return this.value;
					}).get();
		    var indirectexpSubCat=$('input[name="indirectexpSubCat"]').map(function () {
						return this.value;
					}).get();
		    var indirectExpensePettyCashSubCat=$('input[name="indirectExpensePettyCashSubCat"]').map(function () {
						return this.value;
					}).get();
		    var indirectExpenseBdSubCat=$('input[name="indirectExpenseBdSubCat"]').map(function () {
						return this.value;
					}).get();
		    var parentChAc      = "";
 //   		var bankAcSubCat    = "";
 //   		var bankAcNo        = "";
 //   		var capAcSubCat     = "";
 //   		var fxdAsSubCat     = "";
 //   		var cntAstSubCat    = "";
    		var cntLiabSubCat   = "";
 //   		var cntLiabProvSubCat="";
    		var dctExpSubCat="";
    		var dctExpUtilSubCat="";
    		var indctExpSubCat="";
    		var indctExpPtyCshSubCat="";
    		var indctExpBdSubCat="";
    		for (var i=0;i<parentChAcc.length;i++){
	    		if(parentChAcc[i]!=""){
	    	    	parentChAc+=parentChAcc[i]+",";
//	    			if(i==0){
//			    		for (var j=0;j<bankActSubCat.length;j++){
//			    	    	if(bankActSubCat[j]!=""){
//				    	    	bankAcSubCat+=bankActSubCat[j]+",";
//				    	    	if(bankAccNo[j]!=""){
//				    	        	bankAcNo+=bankAccNo[j]+",";
//				    	        }else{
//				    	        	bankAcNo=bankAcNo;
//				    	        }
//			    	        }
//			    		}
//	    			}
//		    		if(i==1){
//			    		for (var j=0;j<capActSubCat.length;j++){
//				    		if(capActSubCat[j]!=""){
//				    	    	capAcSubCat+=capActSubCat[j]+",";
//				    	    }
//			    		}
//		    		}
//		    		if(i==2){
//			    		for (var j=0;j<fxdAstSubCat.length;j++){
//				    		if(fxdAstSubCat[j]!=""){
//				    	    	fxdAsSubCat+=fxdAstSubCat[j]+",";
//				    	    }
//		    			}
//		    		}
//		    		if(i==3){
//		    			for(var j=0;j<currentAstSubCat.length;j++){
//		    				if(currentAstSubCat[j]!=""){
//		    					cntAstSubCat+=currentAstSubCat[j]+",";
//		    				}
//		    			}
//		    		}
		    		if(i==0){
		    			for(var j=0;j<currentLiabSubCat.length;j++){
		    				if(currentLiabSubCat[j]!=""){
		    					cntLiabSubCat+=currentLiabSubCat[j]+",";
		    				}
		    			}
//		    			if($("#parentCntLiabProv").val()!=""){
//			    			for(var j=0;j<curntLiabProvisionSubCat.length;j++){
//			    				if(curntLiabProvisionSubCat[j]!=""){
//			    					cntLiabProvSubCat+=curntLiabProvisionSubCat[j]+",";
//			    				}
//			    			}
//		    			}
		    		}
		    		if(i==1){
		    			for(var j=0;j<directExpSubCat.length;j++){
		    				if(directExpSubCat[j]!=""){
		    					dctExpSubCat+=directExpSubCat[j]+",";
		    				}
		    			}
		    			if($("#parentDctExpUtil").val()!=""){
			    			for(var j=0;j<directExpenseUtilitiesSubCat.length;j++){
			    				if(directExpenseUtilitiesSubCat[j]!=""){
			    					dctExpUtilSubCat+=directExpenseUtilitiesSubCat[j]+",";
			    				}
			    			}
		    			}
		    		}
		    		if(i==2){
		    			for(var j=0;j<indirectexpSubCat.length;j++){
		    				if(indirectexpSubCat[j]!=""){
		    					indctExpSubCat+=indirectexpSubCat[j]+",";
		    				}
		    			}
		    			if($("#parentindctExpPtyCsh").val()!=""){
			    			for(var j=0;j<indirectExpensePettyCashSubCat.length;j++){
			    				if(indirectExpensePettyCashSubCat[j]!=""){
			    					indctExpPtyCshSubCat+=indirectExpensePettyCashSubCat[j]+",";
			    				}
			    			}
		    			}
		    			if($("#parentindctExpBd").val()!=""){
			    			for(var j=0;j<indirectExpenseBdSubCat.length;j++){
			    				if(indirectExpenseBdSubCat[j]!=""){
			    					indctExpBdSubCat+=indirectExpenseBdSubCat[j]+",";
			    				}
			    			}
		    			}
		    		}
	    		}
    		}
			jsonData.chtOfActStatus = chartOfAccountstatval;
			jsonData.organizationId=orgnId;
			jsonData.parentChartAc=parentChAc;
//			jsonData.bankActSubCat=bankAcSubCat;
//			jsonData.bankActNo=bankAcNo;
//			jsonData.capActSubCat=capAcSubCat;
//			jsonData.fxdAstSubCat=fxdAsSubCat;
//			jsonData.currenttAstSubCat=cntAstSubCat;
			jsonData.currenttLiabSubCat=cntLiabSubCat;
//			jsonData.currenttLiabProvSubCat=cntLiabProvSubCat;
			jsonData.directtExpSubCat=dctExpSubCat;
			jsonData.directtExpUtilSubCat=dctExpUtilSubCat;
			jsonData.indirectExpSubCat=indctExpSubCat;
			jsonData.indirectExpPtyCshSubCat=indctExpPtyCshSubCat;
			jsonData.indirectExpBdSubCat=indctExpBdSubCat;
		}
		if(chartOfAccountstatval==1){
		var form=$('#myForm');
		var data = new FormData();
		jQuery.each($('#uploadchartofact')[0].files, function(i, file) {
		    data.append('file-'+i, file);
		});
		$.ajax({
			method: "POST",
			url: form.attr('action'),
			data: data,
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			cache: false,
		    contentType: false,
		    processData: false,
			success: function(data) {
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
						window.location.href="/config";
						alwaysScrollTop();
					},
					error: function () {
						if(xhr.status == 401){ doLogout(); }
					}
				});
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	  }else{
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
				window.location.href="/config";
				 alwaysScrollTop();
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	  }
	});
});




/*$(document).ready(function() {
	$(".uploadTransaction").click(function(){
		alert("inside upload transaction");
		var transactionurl=$("#uploadtransaction").val();
		alert("url="+transactionurl);
		if(transactionurl==""){
			alert("please upload your transactions");
			return true;
		}
		var ext = transactionurl.substring(transactionurl.lastIndexOf('.') + 1);
		if(ext != "xls" && ext != "xlsx"){
			alert("Only Excel files are allowed for transaction upload");
			$("#uploadtransaction").val("");
			return true;
		}
		else{
			var jsonData={};
			var useremail=$("#hiddenuseremail").text();
			alert("user mail="+useremail);
			jsonData.usermail = useremail;
		}
		var form=$('#myTransactionForm');
		var data = new FormData();
		jQuery.each($('#uploadtransaction')[0].files, function(i, file) {
		    data.append('file-'+i, file);
		});
		alert("inside upload");
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		$.ajax({
			method: "POST",
			url: form.attr('action'),
			data: data,
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			cache: false,
		    contentType: false,
		    processData: false,
			success: function(data) {
				$("#uploadtransaction").val("");
				alert("Total records in xls: " +data.totalRowsInXls + ", Inserted in system: " + data.totalRowsInserted);
				$.unblockUI();
				location.reload(true);
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});
*/
$(document).ready(function() {
	$(".uploadBranch").click(function(){
		var chatofacturl=$("#uploadbranch").val();
		if(chatofacturl==""){
			swal("Invalid!!","please upload your branch","error");
			return true;
		}
		var ext = chatofacturl.substring(chatofacturl.lastIndexOf('.') + 1);
		if(ext != "xls" && ext != "xlsx"){
			swal("Invalid data field!!","Only Excel files are allowed for Branch upload","error");
			$("#uploadbranch").val("");
			return true;
		}
		else{
			var jsonData={};
			var useremail=$("#hiddenuseremail").text();
			jsonData.usermail = useremail;
		}
		var form=$('#myBranchForm');
		var data = new FormData();
		jQuery.each($('#uploadbranch')[0].files, function(i, file) {
		    data.append('file-'+i, file);
		});
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		$.ajax({
			method: "POST",
			url: form.attr('action'),
			data: data,
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			cache: false,
		    contentType: false,
		    processData: false,
			success: function(data) {
				$("#uploadbranch").val("");
				$.unblockUI();
				location.reload(true);
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});


function getDefaultAdmin(value){
	if(value==0){
		$("#newadmindiv").show();
		$('.and').attr('class',"newand");
	}
	if(value==1){
		$("#newadmindiv").hide();
		$('.newand').attr('class',"and");
	}
}

function getChartOfAccount(value){
    if(value==0){
    	$('button[name=chartOfAccountradio][value=0]').attr('class',"styleradio");
    	$('button[name=chartOfAccountradio][value=1]').attr('class',"btn");
    	$('button[name=chartOfAccountradio][value=0]').attr('id', "radiochecked");
    	$('button[name=chartOfAccountradio][value=1]').attr('id', "");
    	$("#uploadchtofact").hide();
    	$(".preInstalled").show();
    	$("#bankaccounttree li[id='bankcat']").attr('class',"collapsable lastCollapsable");
    	$("#bankaccounttree div").attr('class',"hitarea collapsable-hitarea lastCollapsable-hitarea");
    	$("#capitalacttree li[id='capactcat']").attr('class',"collapsable lastCollapsable");
    	$("#capitalacttree div").attr('class',"hitarea collapsable-hitarea lastCollapsable-hitarea");
    	$("#fixedassettree li[id='fxdastcat']").attr('class',"collapsable lastCollapsable");
    	$("#fixedassettree div").attr('class',"hitarea collapsable-hitarea lastCollapsable-hitarea");
    	$("#currentassettree li[id='curntastcat']").attr('class',"collapsable lastCollapsable");
    	$("#currentassettree div").attr('class',"hitarea collapsable-hitarea lastCollapsable-hitarea");
    	$("#currentliabilitiestree li[id='curntliabcat']").attr('class',"collapsable lastCollapsable");
    	$("#currentliabilitiestree div").attr('class',"hitarea collapsable-hitarea lastCollapsable-hitarea");
    	$("#directexpensestree li[id='directexpcat']").attr('class',"collapsable lastCollapsable");
    	$("#directexpensestree div").attr('class',"hitarea collapsable-hitarea lastCollapsable-hitarea");
    	$("#indctexptree li[id='indirectexpcat']").attr('class',"collapsable lastCollapsable");
    	$("#indctexptree div").attr('class',"hitarea collapsable-hitarea lastCollapsable-hitarea");
	}
	if(value==1){
		$('button[name=chartOfAccountradio][value=1]').attr('class',"styleradio");
    	$('button[name=chartOfAccountradio][value=0]').attr('class',"btn");
		$('button[name=chartOfAccountradio][value=1]').attr('id', "radiochecked");
		$('button[name=chartOfAccountradio][value=0]').attr('id', "");
		$("#uploadchtofact").show();
		$(".preInstalled").hide();
		$(".acNo").val("");
	}
}



function emailValidation(email){
    var goodEmail = email.match(/\b(^(\S+@).+((\.com)|(\.net)|(\.edu)|(\.mil)|(\.gov)|(\.org)|(\.info)|(\.sex)|(\.biz)|(\.aero)|(\.coop)|(\.museum)|(\.name)|(\.pro)|(\.arpa)|(\.asia)|(\.cat)|(\.int)|(\.jobs)|(\.tel)|(\.travel)|(\.xxx)|(\..{2,2}))$)\b/gi);
    apos=email.indexOf("@");dotpos = email.lastIndexOf(".");lastpos=email.length-1;
    var badEmail    = (apos<1 || dotpos-apos<2 || lastpos-dotpos<2);
    return(!goodEmail || badEmail);
}

function emailFieldValidation(elem){
	var email=$(elem).val();
	if(email!=""){
	    var goodEmail = email.match(/\b(^(\S+@).+((\.com)|(\.net)|(\.edu)|(\.mil)|(\.gov)|(\.org)|(\.info)|(\.sex)|(\.biz)|(\.aero)|(\.coop)|(\.museum)|(\.name)|(\.pro)|(\.arpa)|(\.asia)|(\.cat)|(\.int)|(\.jobs)|(\.tel)|(\.travel)|(\.xxx)|(\..{2,2}))$)\b/gi);
	    apos=email.indexOf("@");dotpos = email.lastIndexOf(".");lastpos=email.length-1;
	    var badEmail    = (apos<1 || dotpos-apos<2 || lastpos-dotpos<2);
	    if(!goodEmail || badEmail){
		    swal("Error in data field!","Please Provide Proper Email-id","error");
		    $(elem).val("");
		    return true;
	    }
	}
}

function addProject(){
	var projId          =  $("#projectEntityHiddenId").val();
    var projName        =  $("#projectname").val();
    var projNumber      =  $("#projectnumber").val();
    var projStartDate   =  $("#projectstartdate").val();
    var projEndDate     =  $("#projectenddate").val();
    var pjctCountry     =  $("#projectCountry").val();
    var projLocation    =  $("#projectlocation").val();
    var projDirName     =  $("#projectdirectorname").val();
    var pdctryCodeVal  =   $("#projectdirectorcountryPhnCode option:selected").val();
    var pdctryCodeText  =  $("#projectdirectorcountryPhnCode option:selected").text();
    var projDirNumber   =  pdctryCodeVal+"-"+$("#projectdirectorphnumber1").val()+$("#projectdirectorphnumber2").val()+$("#projectdirectorphnumber3").val();
    var projMangName    =  $("#projectmanagername").val();
    var pmctryCodeVal  =   $("#projectmanagercountryPhnCode option:selected").val();
    var pmctryCodeText  =  $("#projectmanagercountryPhnCode option:selected").text();
    var projMangNumber  =  pmctryCodeVal+"-"+$("#projectmanagerphnumber1").val()+$("#projectmanagerphnumber2").val()+$("#projectmanagerphnumber3").val();
    $("a[id*='form-container-close']").attr("href",location.hash);
   	if(projName==""){
   	    swal("Error in data field!","Please Fill in the Project Name","error");
	    return true;
    }
   	if(projNumber==""){
		swal("Error in data field!","Please Fill in the Project Number","error");
	    return true;
    }
	var projBranchTmp = $("#projectBranch").val();
	var projBranch = "";
	if(projBranchTmp !== null){
		projBranch = projBranchTmp.toString();
	}
	/*    $("#projectBranch option[value!='multiselect-all']").each(function() {
		if($(this).attr('selected')=='selected'){
			projBranch+=$(this).val()+",";
		}
    }); */
	var jsonData = {};
	if(projId!=""){
	jsonData.projId         = projId;
	}
	var useremail=$("#hiddenuseremail").text();
	jsonData.usermail = useremail;
	jsonData.projName       = projName;
	jsonData.projNumber     = projNumber;
	jsonData.projStartDate  = projStartDate;
	jsonData.projEndDate    = projEndDate;
	jsonData.projLocation   = projLocation;
	jsonData.projBranch     = projBranch;
	jsonData.projDirName    = projDirName;
	jsonData.projCountry     = pjctCountry
	jsonData.projDirCtryCodeText  = pdctryCodeText;
	jsonData.projDirNumber  = projDirNumber;
	jsonData.projMangName   = projMangName;
	jsonData.projManCtryCodeText  = pmctryCodeText;
	jsonData.projMangNumber = projMangNumber;
	//projec position data
	var projectPosHiddenIds = $('input[name="projectpositionHidId"]').map(function () {
			return this.value;
		  }).get();
	var projPosName = $('input[name="positionname"]').map(function () {
	 		return this.value;
	 	}).get();
	var projectpositionvalidity=$('input[name="projectpositionvalidity"]').map(function () {
     	return this.value;
    }).get();
	var projectpositionvalidityTo=$('input[name="projectpositionvalidityTo"]').map(function () {
     	return this.value;
    }).get();
	var projectPositionBranch    = $('select[name="projectPositionBranch"] option:selected').map(function () {
 		return this.value;
 	}).get();
	var projectpositionlocation    = $('input[name="projectpositionlocation"]').map(function () {
 		return this.value;
 	}).get();
	var projPosQual="";
	$('select[name="projectpositionqualification"]').each(function(){
		var projectPositionQual=$(this).find('option:selected').map(function(){
			if(this.value!="multiselect-all"){
			 	return this.value;
			}
		}).get();
		if(projectPositionQual!=""){
			projPosQual+=projectPositionQual+"@"
		}else{
			projPosQual+=" "+"@"
		}
	});
	var qualificationDegree=$('input[name="qualificationDegree"]').map(function(){
		return this.value;
	}).get();
	var requiredExp   = $('select[name="requiredExp"] option:selected').map(function () {
		return this.value;
	}).get();
	var languageProfSpeaking="";
	var languageProfReading="";
	var languageProfWriting="";
	var language=$('#projectLabourProficiencyList input[class="langProfEnable"]:checkbox:checked').map(function () {
	var value=this.value;
	if(value!=""){
		if($("#projectLabourProficiencyList li[id='"+value+"'] input[id='langProfValues1']").val()!=""){
			languageProfSpeaking+=$("#projectLabourProficiencyList li[id='"+value+"'] input[id='langProfValues1']").val()+",";
		}else{
			languageProfSpeaking+=" "+",";
		}
		if($("#projectLabourProficiencyList li[id='"+value+"'] input[id='langProfValues2']").val()!=""){
			languageProfReading+=$("#projectLabourProficiencyList li[id='"+value+"'] input[id='langProfValues2']").val()+",";
		}else{
			languageProfReading+=" "+",";
		}
		if($("#projectLabourProficiencyList li[id='"+value+"'] input[id='langProfValues3']").val()!=""){
			languageProfWriting+=$("#projectLabourProficiencyList li[id='"+value+"'] input[id='langProfValues3']").val()+",";
		}else{
			languageProfWriting+=" "+",";
		}
		return value;
	}
	}).get();
	var jobDescription    = $('textarea[name="jobDescription"]').map(function () {
	 		return this.value;
	 	}).get();
	var positionRequiresApproval=$('select[name="positionRequiresApproval"] option:selected').map(function () {
		return this.value;
		   }).get();
	var placeOfAdvertisement    = $('input[name="placeOfAdvertisement"]').map(function () {
	 		return this.value;
	 	}).get();
	var hiringBudget    = $('input[name="hiringBudget"]').map(function () {
	 		return this.value;
	 	}).get();
	var empAggreementDoc    = $('input[name="empAggreementDoc"]').map(function () {
	 		return this.value;
	 	}).get();
	var projectPosHidIds="";var projectPosName="";var pjctpositionvalidity="";var pjctpositionvalidityto="";var projectPosBranch="";var projectposlocation="";
	var posqualificationDegree="";var posrequiredExp="";var posreqlanguages="";var poslangproficiency="";var posjobDescription="";var posRequiresApproval="";
	var posplaceOfAdvertisement="";var poshiringBudget="";var posempAggreementDoc="";
	// $("a[id*='form-container-close']").attr("href",location.hash);
	$("#newProjectform-container-close").attr("href",location.hash);
	for(var i=0;i<projectPosHiddenIds.length;i++){
		projectPosHidIds+=projectPosHiddenIds[i]+",";
	}
 	for (var i=0;i<projPosName.length;i++){
		if(projPosName[i]!=""){
			projectPosName+=projPosName[i]+",";
		}else{
			projectPosName+=" "+",";
		}
		if(projectpositionvalidity[i]!=""){
			pjctpositionvalidity+=projectpositionvalidity[i]+"@";
		}else{
			pjctpositionvalidity+=" "+"@";
		}
		if(projectpositionvalidityTo[i]!=""){
			pjctpositionvalidityto+=projectpositionvalidityTo[i]+"@";
		}else{
			pjctpositionvalidityto+=" "+"@";
		}
		if(projectPositionBranch[i]!=""){
			projectPosBranch+=projectPositionBranch[i]+",";
		}else{
			projectPosBranch+=" "+",";
		}
		if(projectpositionlocation[i]!=""){
			projectposlocation+=projectpositionlocation[i]+",";
		}else{
			projectposlocation+=" "+",";
		}
		if(qualificationDegree[i]!=""){
			posqualificationDegree+=qualificationDegree[i]+"@";
		}else{
			posqualificationDegree+=" "+"@";
		}
		if(requiredExp[i]!=""){
			posrequiredExp+=requiredExp[i]+",";
		}else{
			posrequiredExp+=" "+",";
		}
		if(jobDescription[i]!=""){
			posjobDescription+=jobDescription[i]+",";
		}else{
			posjobDescription+=" "+",";
		}
		if(positionRequiresApproval[i]!=""){
			posRequiresApproval+=positionRequiresApproval[i]+",";
		}else{
			posRequiresApproval+=" "+",";
		}
		if(placeOfAdvertisement[i]!=""){
			posplaceOfAdvertisement+=placeOfAdvertisement[i]+",";
		}else{
			posplaceOfAdvertisement+=" "+",";
		}
		if(hiringBudget[i]!=""){
			poshiringBudget+=hiringBudget[i]+",";
		}else{
			poshiringBudget+=" "+",";
		}
		if(empAggreementDoc[i]!=""){
			posempAggreementDoc+=empAggreementDoc[i]+",";
		}else{
			posempAggreementDoc+=" "+",";
		}
 	}
 var positionLisingAllowed="";
 if($("input[name='projectJobPositionListing']").prop("checked")){
	 positionLisingAllowed="1";
 }else{
	 positionLisingAllowed="0";
 }
 jsonData.posreqlanguages=language.toString();
 jsonData.poslangproficiencyspeaking=languageProfSpeaking.substring(0, languageProfSpeaking.length-1);
 jsonData.poslangproficiencyreading=languageProfReading.substring(0, languageProfReading.length-1);
 jsonData.poslangproficiencywriting=languageProfWriting.substring(0, languageProfWriting.length-1);
 jsonData.projectPosHidIds=projectPosHidIds.substring(0, projectPosHidIds.length-1);
 jsonData.projectPosName=projectPosName.substring(0, projectPosName.length-1);
 jsonData.pjctpositionvalidity=pjctpositionvalidity.substring(0, pjctpositionvalidity.length-1);
 jsonData.pjctpositionvalidityto=pjctpositionvalidityto.substring(0, pjctpositionvalidityto.length-1);
 jsonData.projectPosBranch=projectPosBranch.substring(0, projectPosBranch.length-1);
 jsonData.projectposlocation=projectposlocation.substring(0, projectposlocation.length-1);
 jsonData.projPosQual=projPosQual.substring(0, projPosQual.length-1);
 jsonData.posqualificationDegree=posqualificationDegree.substring(0, posqualificationDegree.length-1);
 jsonData.posrequiredExp=posrequiredExp.substring(0, posrequiredExp.length-1);
 jsonData.posjobDescription=posjobDescription.substring(0, posjobDescription.length-1);
 jsonData.posRequiresApproval=posRequiresApproval.substring(0, posRequiresApproval.length-1);
 jsonData.posplaceOfAdvertisement=posplaceOfAdvertisement.substring(0, posplaceOfAdvertisement.length-1);
 jsonData.poshiringBudget=poshiringBudget.substring(0, poshiringBudget.length-1);
 jsonData.posempAggreementDoc=posempAggreementDoc.substring(0, posempAggreementDoc.length-1);
 jsonData.projectPositionLisingAllowed=positionLisingAllowed;
	var url="/config/CreateProject";
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
				if (data.ispresent == 'true') {
					swal("Duplicate data error!", "Project already present, please add different project.", "error");
					$("#projectname").val("");
					$("#projectnumber").val("");
				} else {
					var projectlisttable=$("#projectTable");
					$("#projectenddate").attr("class","datepicker");
					$("#projectenddate").addClass('calendar');
					var maximumYear=new Date().getFullYear()+30;
					$(".datepicker").datepicker({
						changeMonth : true,
						changeYear : true,
						dateFormat:  'MM d,yy',
						yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+''
					});
					var projectId=data.id;
					var projectName=data.name;
					$('#projectSetup input[type="text"]').val('');
					$('#projectSetup textarea').val('');
					$('#projectSetup select option:first').prop("selected","selected");
					$("#projectBranch option:selected").each(function () {
						$(this).removeAttr('selected');
					});
					$("#searchProject option[value="+projectId+"]").remove();
					$("#searchProject").append('<option value="'+projectId+'">' +projectName+ '</option>');
					$('#projectBranch').multiselect('rebuild');
					$("#projectTable tr[name='projectEntity"+data.id+"']").remove();
					projectlisttable.append('<tr name="projectEntity'+data.id+'"><td>'+data.name+'</td><td>'+data.number+'</td><td>'+data.startDate+'</td><td>'+data.endDate+'</td><td>'+data.location+'</td><td><button class="btn btn-submit" onClick="showProjectEntityDetails(this)" id="show-entity-details'+data.id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td><td><button class="btn btn-submit"  onClick="deactivateProjectEntityDetails(this)" id="deactivate-entity-details'+data.id+'"><i class="fa fa-trash-o fa-lg pr-5"></i>'+data.actionText+'</button></td></tr>');
					$("#transactionCreationForProject option[value="+projectId+"]").remove();
					$("#transactionApprovalForProject option[value="+projectId+"]").remove();
					$("#transactionCreationForProject").append('<option value="'+data.id+'">' +data.name + '</option>');
					$("#transactionApprovalForProject").append('<option value="'+data.id+'">' +data.name + '</option>');
					$('.multipleDropdown').multiselect('rebuild');
					$("#notificationMessage").html("Project has been added/Updated successfully.");
					$("#newProjectform-container-close").trigger('click');
					$('.notify-success').show();
					$.unblockUI();
					alwaysScrollTop();
				}

			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		})
}
$(document).ready(function() {
    // Assuming "createProject" is a button, adjust the selector accordingly
    $(".createProject").click(function() {
        // Call the addProject function here
        addProject();
    });
});

$(document).ready(function() {
	$(".changePassword").click(function(){
		var actCred=$("#userCred").val();
		var oldPassword=$("#oldPassword").val();
		var newPassword=$("#newPassword").val();
		var confirmPassword=$("#confirmNewPassword").val();
		if(actCred==""){
	   	    swal("Error in data field!","Please Provide Account Username Or Email Id","error");
		    return true;
	    }

	   	if(oldPassword==""){
			swal("Error in data field!","Please Provide Password For The Account","error");
		    return true;
	    }
	   	if(newPassword==""){
			swal("Error in data field!","Enter Account New Password","error");
		    return true;
	    }
	   	if(confirmPassword==""){
			swal("Error in data field!","Please Confirm The New Password For The Account","error");
		    return true;
	   	}
	   	if(newPassword!=confirmPassword){
			swal("Error in data field!","Mismatch During Confirm Password","error");
		    return true;
	   	}
	   	var jsonData = {};
		jsonData.accounttCred= actCred;
		jsonData.oldPwd= oldPassword;
		jsonData.newPwd  = newPassword;
		var url="/user/ChangePassword";
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
				$('#staticchangepassword input[type="text"]').val('');
				$('#staticchangepassword input[type="password"]').val('');
				var pwdchangedStat=data.userChangedPwdData[0].pwdchanged;
				if(pwdchangedStat!="Not Able to Find Account with Provided Account Credential."){
					var lochash=$("#fancybox-close").attr('href');
					$("#notificationMessage").html("Account Password Changed Successfully.");
					$('.notify-success').show();
					$("#staticchangepassword").attr('data-toggle', 'modal');
			        $("#staticchangepassword").modal('hide');
			        location.hash=lochash;
				}
				if(pwdchangedStat=="Not Able to Find Account with Provided Account Credential."){
					$("#wrongActCred").append('<b><font color="red">'+pwdchangedStat+'</p></b>');
					$('.changePassword').html("Change Password");
				}
				alwaysScrollTop();
	        },
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		})
	});
});

$(document).ready(function() {
	$('select[name="bnchFacility"]').change(function() {
		var particular = this.value;
		var optName = $(this).find("option:selected").text();
		if (optName == "Leased" || optName == "Rented") {
			$("#branchPremiseDiv").show();
		} else {
			$("#branchPremiseDiv").hide();
		}
	});
});

$(document).ready(function() {
	$('select[name="bnchDeposit"]').change(function() {
		var particular = this.value;
		var optName = $(this).find("option:selected").text();
		if (optName == "Yes") {
			$("#depositKey").show();
		} else {
			$("#depositKey").hide();
		}
	});
});

$(document).ready(function() {
	$('select[name="bnchassetInsured"]').change(function() {
		var particular = this.value;
		var optName = $(this).find("option:selected").text();
		if (optName == "Yes") {
			$("#bnchAstInsured").show();
		} else {
			$("#bnchAstInsured").hide();
		}
	});
});

function removeNewKeyDep(dynmkeydep){
	$("#dynmKeydepositLab"+dynmkeydep+"").remove();
	$("#dynmKeyDepositDiv"+dynmkeydep+"").remove();
	$("#break"+dynmkeydep+"").remove();
}

$(document).ready(function() {
	$('select[name="noOfKeys"]').change(function() {
		var keys = this.value;
		$("#dynmKeyDepositDiv").html("");
		for(var i=1;i<keys;i++){
			$("#newKeyDepositId").trigger('click');
		}
	});
});

function labourhiringcancel(){
	$(".newlabourform-container").hide();
	$(".hiredLabourform-container").show();
	$("#hiredLabourFreeTextSearch").val();
	$('.hiredLabourFreeTextSearchButton').trigger('click');
}


function populateprojectsreportsto(){
	var jsonData={};
	jsonData.useremail=$("#hiddenuseremail").text();
	var url="/hiring/listProjectsReportsto";
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
			$("#availableProjects").children().remove();
			$("#availableProjects").append('<option value="">--Please Select--</option>');
			$("#reportsTo").children().remove();
			$("#reportsTo").append('<option value="">--Please Select--</option>');
			for(var i=0;i<data.organizationprojects.length;i++){
				$("#availableProjects").append('<option value="'+data.organizationprojects[i].id+'">'+data.organizationprojects[i].projectName+'</option>');
			}
			for(var j=0;j<data.organizationreportsto.length;j++){
				$("#reportsTo").append('<option>'+data.organizationreportsto[j].reportToName+'</option>');
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	})
}


function populatevendorgrouplist(){
	var jsonData={};
	jsonData.useremail=$("#hiddenuseremail").text();
	var url="/vendor/listVendorGroup";
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
			$("#vendorGroupDetailsListTable tbody").html("");
			for(var i=0;i<data.vendorGroupList.length;i++){
				$("#vendorGroupDetailsListTable").append('<tr id="vendorGroupEntity'+data.vendorGroupList[i].id+'" name="vendorGroupEntity'+data.vendorGroupList[i].id+'"><td>'+data.vendorGroupList[i].vendGroupName+'</td><td><div class="search"><div id="search-launch" style="display: block;"><a href="#vendorSetup" class="btn btn-submit" onClick="showVendorGroupEntityDetails(this)" id="show-entity-details'+data.vendorGroupList[i].id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</a></div></div></td></tr>');
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

function populatevendorgroupdropdown(){
	var jsonData={};
	jsonData.useremail=$("#hiddenuseremail").text();
	var url="/vendor/listVendorGroup";
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
			$("#vendorGroup").children().remove();
			$("#vendorGroup").append('<option value="">--Please Select--</option>');
			for(var i=0;i<data.vendorGroupList.length;i++){
				console.log("-----" + data.vendorGroupList[i].id);
				$("#vendorGroup").append('<option value="'+data.vendorGroupList[i].id+'">'+data.vendorGroupList[i].vendGroupName+'</option>');
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	})
}

$(document).ready(function() {
	$("a[id*='form-container-close']").click(function(){
		var formcloseid=this.id;
		var formdivtoclose=formcloseid.replace('-close','');
		$(".logo-upload-button").attr("href",location.hash);
		$('.notify-success').hide();
		$("."+formdivtoclose+"").hide();
		alwaysScrollTop();
	});
});

function formCancel(){
	$('div[id*="-form-container"], .btn-div-top').hide();
	$('div[id*="-container"], .btn-div-top').hide();
	alwaysScrollTop();
}

$(document).ready(function() {
	$(".savePermissionButton").click(function(){
		var admin_tab_check_values = $('input[name="admintab"]:checkbox:checked').map(function () {
		    return this.value;
		}).get();
		var creator_tab_check_values = $('input[name="creatortab"]:checkbox:checked').map(function () {
		    return this.value;
		}).get();
		var approver_tab_check_values = $('input[name="approvertab"]:checkbox:checked').map(function () {
		    return this.value;
		}).get();
		if(admin_tab_check_values.length==0 && creator_tab_check_values.length==0 && approver_tab_check_values.length==0){
			swal("Error in data field!","Please Select Permissions and then save user permissions.","error");
		}
		var adminTabValues="";
		for(var i=0;i<admin_tab_check_values.length;i++){
			adminTabValues+=check_box_values[i]+",";
		}
		var creatorTabValues="";
		for(var i=0;i<creator_tab_check_values.length;i++){
			creatorTabValues+=check_box_values[i]+",";
		}
		var approverTabValues="";
		for(var i=0;i<approver_tab_check_values.length;i++){
			approverTabValues+=check_box_values[i]+",";
		}
		var jsonData = {};
		jsonData.adminTabPermission= adminTabValues;
		jsonData.creatorTabPermission= creatorTabValues;
		jsonData.approverTabPermission  = approverTabValues;
		var url="/role/saverolePermissions";
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
				alwaysScrollTop();
	        },
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		})
	});
});

function deactivateUserEntityDetails(elem){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var entityId=$(elem).attr('id');
	var origEntityId=entityId.substring(25, entityId.length);
	var jsonData = {};
	jsonData.entityPrimaryId= origEntityId;
	var url="user/deactivateUser"
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
			$("#usersTable tr[name='userEntity"+origEntityId+"']").remove();
			$.unblockUI();
		},
		error: function () {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

function deactivateProjectEntityDetails(elem){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var entityId=$(elem).attr('id');
	var actionText=$(elem).text();
	var origEntityId=entityId.substring(25, entityId.length);
	var jsonData = {};
	jsonData.email =$("#hiddenuseremail").text();
	jsonData.entityPrimaryId= origEntityId;
	jsonData.projectActionText=actionText;
	var url="project/deactivateProject"
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
			$(elem).html("");
			if(data.projectActionData[0].actionText=="Activate"){
				$(elem).html('<i class="far fa-check-square fa-lg pr-5"></i>' + data.projectActionData[0].actionText);
			}else if(data.projectActionData[0].actionText=="Deactivate"){
				$(elem).html('<i class="far fa-trash-alt fa-lg pr-5"></i>'+data.projectActionData[0].actionText);
			}
			$.unblockUI();
		},
		error: function () {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

function deactivateBranchEntityDetails(elem){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var entityId=$(elem).attr('id');
	var actionText=$(elem).text();
	var origEntityId=entityId.substring(25, entityId.length);
	var jsonData = {};
	jsonData.email =$("#hiddenuseremail").text();
	jsonData.entityPrimaryId= origEntityId;
	jsonData.branchActionText=actionText;
	var url="branch/deactivateBranch"
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
			if(data.branchActionData[0].successmsg=="Subscribed"){
				$(elem).html("");
				if(data.branchActionData[0].actionText=="Activate"){
					$(elem).html('<i class="far fa-check-square fa-lg pr-5"></i>' + data.branchActionData[0].actionText );
				}
				if(data.branchActionData[0].actionText=="Deactivate"){
					$(elem).html('<i class="far fa-trash-alt fa-lg pr-5"></i>'+data.branchActionData[0].actionText);
				}
			}else{
				//redirect to payment subscription page when payment gateway module is done
			}
			$.unblockUI();
		},
		error: function(xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}


function showCategoryEntityDetails(elem){
	var entityId=$(elem).attr('id');
	var origEntityId=entityId.substring(19, entityId.length);
	var detailForm="newCategoryform-container";
	var jsonData = {};
	jsonData.entityPrimaryId= origEntityId;
	$(".logo-upload-button").attr("href",location.hash);
	$("a[id*='form-container-close']").attr("href",location.hash);
	var url="/config/categoryDetails";
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
			for(var i=0;i<data.categorydetailsData.length;i++){
				$('#category-form-container input[id="categoryEntityHiddenId"]').val(data.categorydetailsData[i].id);
				$('#category-form-container input[id="category"]').val(data.categorydetailsData[i].name);
				$('#category-form-container input[id="cataccountcode"]').val(data.categorydetailsData[i].actCode);
				$('#category-form-container input[id="catdescription"]').val(data.categorydetailsData[i].description);
				$("."+detailForm+"").slideDown('slow');
			}
			alwaysScrollTop();
        },
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

function showExpenseEntityDetails(elem){
	var entityId=$(elem).attr('id');
	var origEntityId=entityId.substring(19, entityId.length);
	$("a[id='createExpense']").attr("href",location.hash);
	var jsonData = {};
	jsonData.useremail =$("#hiddenuseremail").text();
	jsonData.entityPrimaryId= origEntityId;
	var url="/expense/showExpenseDetails";
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
			for(var i=0;i<data.expensedetailsData.length;i++){
				var entityPrimKey=data.expensedetailsData[0].id;
				var txnPurpose=data.expensedetailsData[0].txnPurpose;
				var item=data.expensedetailsData[0].item;
				var payType=data.expensedetailsData[0].payType;
				var vendor=data.expensedetailsData[0].vendor;
				var perunitcost=data.expensedetailsData[0].perunitcost;
				var noofunits=data.expensedetailsData[0].noofunits;
				var tamount=data.expensedetailsData[0].tamount;
				var docuploadurl=data.expensedetailsData[0].docuploadurl;
				$("#createExpense input[id='hiddenId']").val(entityPrimKey);
				$("#createExpense select[id='transactionPurpose']").find("option[value='"+txnPurpose+"']").prop("selected","selected");
				$("#createExpense select[id='specifics']").find("option[value='"+item+"']").prop("selected","selected");
				$("#createExpense select[id='paymentType']").find("option[value='"+payType+"']").prop("selected","selected");
				var jsonData = {};
				jsonData.id = item;
				 var url="/expenses/vendors";
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
						$('#vendor').children().remove();
						 var sel = $("#vendor");
						 for (var i=0;i<data.vendorData.length; ++i) {
							 sel.append('<option value="'+data.vendorData[i].id+'">' +data.vendorData[i].name + '</option>');
						 }
						 alwaysScrollTop();
					},
					error: function (xhr, status, error) {
						if(xhr.status == 401){ doLogout(); }
					}
				 });
				$("#createExpense select[id='vendor']").find("option[value='"+vendor+"']").prop("selected","selected");
				$("#unitcost").val(perunitcost);
				$("#noofitems").val(noofunits);
				$("#tamount").val(tamount);
				$("#docuploadurl").val(docuploadurl);
				$("#createExpense").slideDown('slow');
				$("#expenseFormCreate").show();
				$("#expenseFormShow").hide();
				alwaysScrollTop();
			  }
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

function accountExpense(elem){
	var usremail=$("#hiddenuseremail").text();
	var origEntityId=$(elem).attr('id');
	var optName = $(elem).text();
	var jsonData = {};
	jsonData.useremail=usremail;
	jsonData.entityPrimaryId= origEntityId;
	var url="/expense/showExpenseDetails";
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
			for(var i=0;i<data.expensedetailsData.length;i++){
				var entityPrimKey=data.expensedetailsData[0].id;
				var txnPurpose=data.expensedetailsData[0].txnPurpose;
				var txnPurposeText=data.expensedetailsData[0].txnPurposeText;
				var item=data.expensedetailsData[0].item;
				var itemName=data.expensedetailsData[0].itemName;
				var payType=data.expensedetailsData[0].payType;
				var payTypeText=data.expensedetailsData[0].payTypeText;
				var vendor=data.expensedetailsData[0].vendor;
				var vendorName=data.expensedetailsData[0].vendorName;
				var perunitcost=data.expensedetailsData[0].perunitcost;
				var noofunits=data.expensedetailsData[0].noofunits;
				var tamount=data.expensedetailsData[0].tamount;
				var docuploadurl=data.expensedetailsData[0].docuploadurl;
				$("#expenseFormShow input[id='statichiddenId']").val(entityPrimKey);
				$("#expenseFormShow select[id='statictransactionPurpose']").append("<option value='"+txnPurpose+"'>"+txnPurposeText+"</option>");
				$("#expenseFormShow select[id='specstatic']").append("<option value='"+item+"'>"+itemName+"</option>");
				$("#expenseFormShow select[id='staticpaymentType']").append("<option value='"+payType+"'>"+payTypeText+"</option>");
				$("#expenseFormShow select[id='venstatic']").append("<option value='"+vendor+"'>"+vendorName+"</option>");
				$("#unitcoststatic").val(perunitcost);
				$("#noofitemsstatic").val(noofunits);
				$("#tamountstatic").val(tamount);
				$("#docuploadurlstatic").val();
				$("#docuploadurlstatic").attr("disabled","");
				$("#createExpense").slideDown('slow');
				$("#expenseFormShow").show();
				$("#expenseFormCreate").hide();
				$("#submitApprovalButton").hide();
				$("#submitAccountingButton").show();
				$("#additionalApproval").hide();
				$("#additionalUserDiv").hide();
				$("#compcancelButton").hide();
				$("#accountingcancelButton").show();
				$("#additionaluserApproval").children().remove();
				alwaysScrollTop();
			  }
			  if(optName=="Send For Additional Approval"){
				for(var j=0;j<data.additionalUsers.length;j++){
					$("#additionaluserApproval").append('<option value="'+data.additionalUsers[j].id+'">' +data.additionalUsers[j].email+ '</option>');
				}
				$("#additionalUserDiv").show();
				$("#submitAccountingButton").hide();
				$("#additionalApproval").show();
			  }
		},
		error: function(xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}



$(document).ready(function(){
	$( 'div.addNewTaxLink a' ).click(function() {
		var options=$('.multiBranch').html();
		var anotherDynmtaxtableData=$("#taxTable");
		var taxTableLength=$("#taxTable tbody").children().length;
		anotherDynmtaxtableData.append('<tr id="anotherDynmtaxtableData'+taxTableLength+'"><td><input type="hidden" name="taxHiddenId" id="taxHiddenId" value=""><input type="text" id="taxName" name="taxName" placeholder="Tax Name"></td><td><input type="text" id="taxRate" name="taxRate" class="input-small" placeholder="Tax Rate" onkeypress="return onlyDotsAndNumbers(event)">%</td><td><select id="taximpacttype" name="taximpacttype" style="width: 123px;"><option value="1">Added</option><option value="0">Deducted</option></select></td><td><select id="taximpacton" name="taximpacton" style="width: 123px;"><option value="1">Gross</option><option value="0">Net</option></select> <td><select id="taxApplicableBranchnew'+taxTableLength+'" class="multiBranch" name="taxApplicableBranch" multiple="multiple" style="width: 223px;"></select></td></td><td class="tax_delete"><a id="anotherDynmtaxtableData'+taxTableLength+'" href="#taxSetup" class="cross-button" onClick="removeDynmTaxRow(this.id)">Delete</a></td></tr>');
		$("#taxTable tr[id='anotherDynmtaxtableData"+taxTableLength+"'] select[class='multiBranch']").append(options);
		$('#taxTable tr[id="anotherDynmtaxtableData'+taxTableLength+'"] select[class="multiBranch"]').multiselect({
	        buttonWidth: '140px',
	        maxHeight:   150,
	        includeSelectAllOption: true,
	        enableFiltering :true,
	        /*
	        buttonText: function(options) {
	          if (options.length == 0) {
	                  return 'None selected <b class="caret"></b>';
	              }
	              else if (options.length > 6) {
	                  return options.length + ' selected  <b class="caret"></b>';
	              }
	              else {
	                  var selected = '';
	                  options.each(function() {
	              selected += $(this).text() + ', ';
	                  });

	                  return options.length + ' selected  <b class="caret"></b>';
	          }
	        },*/
	        onChange: function(element, checked) {
	        }
	      });
	});
});

function showVendorGroupEntityDetails(elem){
	var entityId=$(elem).attr('id');
	var origEntityId=entityId.substring(19, entityId.length);
	var detailForm="newVendorGroupform-container";
	$('.'+detailForm+' input[type="hidden"]').val("");
	$('.'+detailForm+' input[type="text"]').val("");
	$('.'+detailForm+' textarea').val("");
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.entityPrimaryId= origEntityId;
	var url="/vendor/vendorGroupDetails";
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
			var id=data.vendorGroupDetails[0].id;
			var groupName=data.vendorGroupDetails[0].groupName;
			var groupKl=data.vendorGroupDetails[0].groupKl;
			$("#vendorGroupEntityHiddenId").val(id);
			$("#vendGroupName").val(groupName);
			$("#vendGroupKL").val(groupKl);
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	})
}

function showCustomerGroupEntityDetails(elem){
	var entityId=$(elem).attr('id');
	var origEntityId=entityId.substring(19, entityId.length);
	var detailForm="newVendorGroupform-container";
	$('.'+detailForm+' input[type="hidden"]').val("");
	$('.'+detailForm+' input[type="text"]').val("");
	$('.'+detailForm+' textarea').val("");
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.entityPrimaryId= origEntityId;
	var url="/vendor/customerGroupDetails";
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
			var id=data.customerGroupDetails[0].id;
			var groupName=data.customerGroupDetails[0].groupName;
			var groupKl=data.customerGroupDetails[0].groupKl;
			$("#customerGroupEntityHiddenId").val(id);
			$("#custGroupName").val(groupName);
			$("#custGroupKL").val(groupKl);
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	})
}


function showProjectEntityDetails(elem){
	var entityId=$(elem).attr('id');
	var origEntityId=entityId.substring(19, entityId.length);
	var detailForm="newProjectform-container";
	$('.'+detailForm+' input[type="hidden"]').val("");
	$('.'+detailForm+' input[type="text"]').val("");
	$('.'+detailForm+' textarea').val("");
	$('.'+detailForm+' input[type="password"]').val("");
	$('.'+detailForm+' select option:first').prop("selected", "selected");
	$('.'+detailForm+' select[class="countryPhnCode"]').each(function () {
		$(this).find('option:first').prop("selected", "selected");
    });
	$('.'+detailForm+' select[class="countryDropDown"]').each(function () {
		$(this).find('option:first').prop("selected", "selected");
    });
	$(".logo-upload-button").attr("href",location.hash);
	$("a[id*='form-container-close']").attr("href",location.hash);
	$("#projectPositionFormTable tr[id*='dynProjPos']").remove();
	$("#projectPositionFormTable input[type='text']").val("");
	$("#projectPositionFormTable textarea").val("");
	$("select[name='projectpositionqualification'] option:selected").each(function () {
		$(this).removeAttr('selected');
	});
	$("select[name='projectpositionqualification']").multiselect('rebuild');
	$("#projectPositionFormTable select[name!='projectpositionqualification']").each(function() {
		$(this).val($('option:first').val());
	});
	var jsonData = {};
	jsonData.entityPrimaryId= origEntityId;
	var url="/config/projectDetails";
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
			for(var i=0;i<data.projectdetailsData.length;i++){
				$("#projectBranch option:selected").each(function () {
			           $(this).removeAttr('selected');
				});
				$('#project-form-container input[id="projectEntityHiddenId"]').val(data.projectdetailsData[i].id);
				$('#project-form-container input[id="projectname"]').val(data.projectdetailsData[i].projectName);
				$('#project-form-container input[id="projectnumber"]').val(data.projectdetailsData[i].projectNumber);
				$('#project-form-container select[id="projectCountry"] option').filter(function () {return $(this).val()==data.projectdetailsData[i].country;}).prop("selected", "selected");
				$('#project-form-container input[id="projectlocation"]').val(data.projectdetailsData[i].projectLocation);
				$('#project-form-container input[id="projectdirectorname"]').val(data.projectdetailsData[i].projectDirectorName);
				$('#project-form-container select[id="projectdirectorcountryPhnCode"] option').filter(function () {return $(this).html()==data.projectdetailsData[i].pjctDirPhoneNumberCtryCd;}).prop("selected", "selected");
				$('#project-form-container input[id="projectdirectorphnumber1"]').val(data.projectdetailsData[i].projectDirectorNumber.substring(0,3));
				$('#project-form-container input[id="projectdirectorphnumber2"]').val(data.projectdetailsData[i].projectDirectorNumber.substring(3,6));
				$('#project-form-container input[id="projectdirectorphnumber3"]').val(data.projectdetailsData[i].projectDirectorNumber.substring(6,10));
				$('#project-form-container input[id="projectmanagername"]').val(data.projectdetailsData[i].projectManagerName);
				$('#project-form-container select[id="projectmanagercountryPhnCode"] option').filter(function () {return $(this).html()==data.projectdetailsData[i].pjctManPhoneNumberCtryCd;}).prop("selected", "selected");
				$('#project-form-container input[id="projectmanagerphnumber1"]').val(data.projectdetailsData[i].projectManagerNumber.substring(0,3));
				$('#project-form-container input[id="projectmanagerphnumber2"]').val(data.projectdetailsData[i].projectManagerNumber.substring(3,6));
				$('#project-form-container input[id="projectmanagerphnumber3"]').val(data.projectdetailsData[i].projectManagerNumber.substring(6,10));
				$('#project-form-container input[id="projectstartdate"]').val(data.projectdetailsData[i].projectStartDate);
				if(data.projectdetailsData[i].allowedRecruitmentServices=="1"){
					$("input[name='projectJobPositionListing']").prop("checked","checked");
				}
				if(data.projectdetailsData[i].allowedRecruitmentServices=="0"){
					$("input[name='projectJobPositionListing']").prop("checked","");
				}
				$("#projectenddate").attr("class","");
				var maximumYear=new Date().getFullYear()+30;
		         $("#projectenddate" ).datepicker({
		             changeMonth : true,
		             changeYear: true,
		             minDate: new Date($("#projectstartdate").val()),
		             dateFormat:  'MM d,yy',
		             yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
		             onSelect: function(x,y){
							var elemName=$(this).attr('name');
							if(elemName=='projectenddate'){
								if($("#projectstartdate").val()==""){
									$(this).val("");
								}
								var d1 = new Date($("#projectenddate").val())
								var d2 = new Date($("#projectstartdate").val())
								if (d1 < d2) {
								   swal("Invalid data!","project end date cannot be less than project start date","error");
								   $(this).val("");
								   return true;
								}
							}
							$(this).focus();
						}
		        });
				$('#project-form-container input[id="projectenddate"]').val(data.projectdetailsData[i].projectEndDate);
				var projectBranchesList=data.projectdetailsData[i].projectBranches.split(',');
				for(var j=0;j<projectBranchesList.length;j++){
					//$('#project-form-container select[id="projectBranch"]').find("option[value='"+projectBranchesList[j]+"']").attr("selected", "selected");
					$("#project-form-container #projectBranch").find("option[value='"+projectBranchesList[j]+"']").prop("selected", "selected");
				}
				$('#projectBranch').multiselect('rebuild');
				$('.notify-success').hide();
				$("."+detailForm+"").slideDown('slow');
			}
			alwaysScrollTop();
        },
		error: function (xhr, status, error){
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

function removeDynmTaxRow(id){
	$("#"+id+"").remove();
}

function removeDefaultRow(){
	$("#taxTable tr[id='newrow']").remove();
}

function newAdmin(domId){
	if(domId=="add-new-admin"){
		$("div[id='newadmin']").show();
		$("div[id='newadminButton']").show();
	}
	if(domId=="adminClose"){
		$("div[id='newadmin']").hide();
		$("div[id='newadminButton']").hide();
	}
	if(domId.indexOf('dynmorgDynmStatutory')!=-1){
		$("#"+domId+"").remove();
		$("br[id="+domId+"]").remove();
	}
	if(domId.indexOf('dynmbnchDynmStatutory')!=-1){
		$("#"+domId+"").remove();
		$("br[id="+domId+"]").remove();
	}
}

$(document).ready(function(){
	$('.addUpdateTax').click(function() {
		var tax_hidden_values = $('input[name="taxHiddenId"]').map(function () {
		    return this.value;
		}).get();
		var tax_name_values = $('input[name="taxName"]').map(function () {
		    return this.value;
		}).get();
		var tax_rate_values = $('input[name="taxRate"]').map(function () {
		    return this.value;
		}).get();
		var tax_impact_type_values = $('select[name="taximpacttype"]').map(function () {
		    return this.value;
		}).get();
		var tax_impact_on_values = $('select[name="taximpacton"]').map(function () {
		    return this.value;
		}).get();
		var tax_applicable_branches_values="";
		var nonSelectableTaxAppBnch=false;
		$('select[name="taxApplicableBranch"]').each(function() {
			if($(this).val()=="" || $(this).val()==null){
				nonSelectableTaxAppBnch=true;
				return false;
			}
			tax_applicable_branches_values+=$(this).val()+":";
		})
		if(nonSelectableTaxAppBnch){
			swal("Invalid data!","Please map the tax with barnches","error");
			return true;
		}
		for(var i=0;i<tax_name_values.length;i++){
			 if(tax_name_values[i]==""){
				swal("Invalid data!","Please provide tax name","error");
				 return true;
			 }
			 if(tax_rate_values[i]==""){
				swal("Invalid data!","Please provide rate applicable for the tax.","error");
				 return true;
			 }
		}
		var taxName="";
		var taxRate="";
		var taxImpact="";
		var taxHidVal="";
		var taxImpactOn="";
		for (var i=0;i<tax_name_values.length;i++){
			taxHidVal+=tax_hidden_values[i]+",";
			taxName+=tax_name_values[i]+",";
			taxRate+=tax_rate_values[i]+",";
			taxImpact+=tax_impact_type_values[i]+",";
			taxImpactOn+=tax_impact_on_values[i]+",";
		}
		var taxHiddenJsonValue=taxHidVal.substring(0, taxHidVal.length-1);
		var taxNameJsonValue=taxName.substring(0, taxName.length-1);
		var taxRateJsonValue=taxRate.substring(0, taxRate.length-1);
		var taxImpactTypeJsonValue=taxImpact.substring(0, taxImpact.length-1);
		var taxImpactOnJsonValue=taxImpact.substring(0, taxImpact.length-1);
		var taxAppBnchsJsonValue=tax_applicable_branches_values.substring(0, tax_applicable_branches_values.length-1);
		var jsonData = {};
		jsonData.taxentityHiddenIds= taxHiddenJsonValue;
		jsonData.taxentityNames= taxNameJsonValue;
		jsonData.taxentityRates= taxRateJsonValue;
		jsonData.taxentityImpacts= taxImpactTypeJsonValue;
		jsonData.taxentityImpactOn= taxImpactOnJsonValue;
		jsonData.taxentityAppBnchs= taxAppBnchsJsonValue;
		$('#addUpdateTax').html("Saving Taxes ...");
		var url="/config/addUpdateTaxes";
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
				$("#taxTable tbody tr").each(function() {
					$(this).remove();
				});
				for(var i=0;i<data.taxUpdateListData.length;i++){
			    	var selectedTaxImpactType=data.taxUpdateListData[i].taxImpactType;
			    	var selectedTaxImpactOn=data.taxUpdateListData[i].taxImpactOn;
			    	$("#taxTable tbody").append('<tr id="taxAppBranch'+data.taxUpdateListData[i].id+'"><td><input type="hidden" name="taxHiddenId" id="taxHiddenId" value='+data.taxUpdateListData[i].id+'><input type="text" id="taxName" name="taxName" placeholder="Tax Name" value="'+data.taxUpdateListData[i].taxName+'"></td><td><input type="text" id="taxRate" name="taxRate" class="input-small" placeholder="Tax Rate" value="'+data.taxUpdateListData[i].taxRate+'" onkeypress="return onlyDotsAndNumbers(event)">%</td><td><select id="taximpacttype'+data.taxUpdateListData[i].id+'" name="taximpacttype" style="width: 123px;"><option value="1">Added</option><option value="0">Deducted</option></select></td><td><select id="taximpacton'+data.taxUpdateListData[i].id+'" name="taximpacton" style="width: 123px;"><option value="1">Gross</option><option value="0">Net</option></select></td><td><select id="taxApplicableBranch'+data.taxUpdateListData[i].id+'" class="multiBranch" name="taxApplicableBranch" multiple="multiple" style="width: 223px;"></select></td></tr>');
			    	$("#taxTable tr[id='taxAppBranch"+data.taxUpdateListData[i].id+"'] select[id='taxApplicableBranch"+data.taxUpdateListData[i].id+"']").append('<option value="Select All">Select All</option>');
			    	$("#taximpacttype"+data.taxUpdateListData[i].id+"").find("option[value="+selectedTaxImpactType+"]").attr("selected", "selected");
			    	$("#taximpacton"+data.taxUpdateListData[i].id+"").find("option[value='"+selectedTaxImpactOn+"']").prop("selected", "selected");
			    	for(var j=0;j<data.bnchListData.length;j++){
			    		$("#taxTable tr[id='taxAppBranch"+data.taxUpdateListData[i].id+"'] select[id='taxApplicableBranch"+data.taxUpdateListData[i].id+"']").append('<option value="'+data.bnchListData[j].id+'">' +data.bnchListData[j].branchName + '</option>');
			    	}
			    	var taxSelcBranches=(data.taxUpdateListData[i].taxSelectedBranches).split(',');
			    	for(var j=0;j<taxSelcBranches.length;j++){
			    		$("#taxTable tr[id='taxAppBranch"+data.taxUpdateListData[i].id+"'] select[id='taxApplicableBranch"+data.taxUpdateListData[i].id+"']").find("option[value='"+taxSelcBranches[j]+"']").prop("selected", "selected");
			    	}
				}
				$("#taxTable tbody").append('<tr id="newrow"><td><input type="hidden" name="taxHiddenId" id="taxHiddenId" value=""><input type="text" id="taxName" name="taxName" placeholder="Tax Name"></td><td><input type="text" id="taxRate" name="taxRate" class="input-small" placeholder="Tax Rate" onkeypress="return onlyDotsAndNumbers(event)">%</td><td><select id="taximpacttype" name="taximpacttype" style="width: 123px;"><option value="1">Added</option><option value="0">Deducted</option></select></td><td><select id="taximpacton" name="taximpacton" style="width: 123px;"><option value="1">Gross</option><option value="0">Net</option></select></td><td><select id="taxApplicableBranchNew" class="multiBranch" name="taxApplicableBranch" multiple="multiple" style="width: 223px;"></select></td><td class="tax_delete"><a href="#taxSetup" class="cross-button" onClick="removeDefaultRow()">Delete</a></td></tr>');
				var taxAppSelNew=$("#taxApplicableBranchNew");
				taxAppSelNew.append('<option value="Select All">Select All</option>');
				for(var i=0;i<data.bnchListData.length;i++){
					taxAppSelNew.append('<option value="'+data.bnchListData[i].id+'">' +data.bnchListData[i].branchName + '</option>');
		    	}
				$('.notify-success').show();
	            $("#notificationMessage").html("Your taxes have been added/Updated successfully.");
	            $('#addUpdateTax').html("Add/Update Tax");
	            $('.multiBranch').multiselect({
	                buttonWidth: '150px',
	                maxHeight:   150,
	                enableFiltering :true,
	                /*
	                buttonText: function(options) {
	                  if (options.length == 0) {
	                          return 'None selected <b class="caret"></b>';
	                      }
	                      else if (options.length > 6) {
	                          return options.length + ' selected  <b class="caret"></b>';
	                      }
	                      else {
	                          var selected = '';
	                          options.each(function() {
	                      selected += $(this).text() + ', ';
	                          });

	                          return options.length + ' selected  <b class="caret"></b>';
	                  }
	                },*/
	                onChange: function(element, checked) {
	                    if(checked == true) {
	                      var elemId=element.context.id;
	                      var elemName=element.context.name;
	                      if(elemName=="taxApplicableBranch"){
	                        var elemVal=$("#"+elemId+"").val();
	                        if(elemVal.indexOf('Select All') != -1){
	                         $("#"+elemId+" > option").each(function() {
	                           $(this).attr('selected','selected');
	                         });
	                        }
	                        $('.multiBranch').multiselect('rebuild');
	                      }
	                    }
	                    else if(checked == false) {
	                      var elemId=element.context.id;
	                      var elemName=element.context.name;
	                      if(elemName=="taxApplicableBranch"){
	                        var elemVal=$("#"+elemId+"").val();
	                        if(elemVal.indexOf('Select All') == -1){
	                        	$("#"+elemId+" > option").each(function() {
	                        		$(this).removeAttr('selected');
	                          });
	                        }else{
	                        	$("#"+elemId+" > option[value='Select All']").removeAttr('selected');
	                        	$(this).removeAttr('selected');
	                        }
	                        $('.multiBranch').multiselect('rebuild');
	                      }
	                    }
	                  }
	            });
	            alwaysScrollTop();
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		})
	});
});

$(document).ready(function(){
	$(".staticsystemconfigclose").click(function() {
		window.location.href="/";
	});
});

function GetURLParameter(sParam){
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');
    for (var i = 0; i < sURLVariables.length; i++){
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] == sParam){
            return sParameterName[1];
        }
    }
}

function getBranchTaxes(){
		var itemBnchVal="";
		var branchTaxsel=$("#branchTaxesId");
		$("#itemBranch option[value!='multiselect-all']").each(function() {
			if($(this).attr('selected')=='selected'){
				itemBnchVal+=$(this).val()+",";
			}
        });
		var itemBnchValues=itemBnchVal.substring(0, itemBnchVal.length-1);
		var jsonData = {};
  	    jsonData.taxBranch = itemBnchValues;
  		var url="/config/getBranchTax"
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
        	$("#branchTaxesId").children().remove();
         	$("#bnchTaxDisplay").hide();
            for(var i=0;i<data.brancTaxData.length;i++){
         	   var taxId=data.brancTaxData[i].id;
         	   var taxName=data.brancTaxData[i].taxName;
         	   branchTaxsel.append('<li id="branchTax'+taxId+'"><div><label class="branchTaxlabel" id="branchTax'+taxId+'"><input id="branchTax'+taxId+'" type="checkbox" value="1" name="branchTax" style="vertical-align: top;" onClick="checkUncheckPermission(this);"/>'+taxName+'</label></div></li>');
            }
            if(data.brancTaxData.length>0){
            	$("#bnchTaxDisplay").show();
            }
            alwaysScrollTop();
          },
          error: function (xhr, status, error) {
          		if(xhr.status == 401){ doLogout(); }
          }
       });
}

function openChangePwd(){
	$("#staticchangepassword").attr('data-toggle', 'modal');
    $("#staticchangepassword").modal('show');
    $(".staticchangepasswordclose").attr("href",location.hash);
}

$(document).ready(function() {
	$(".staticchangepasswordclose").click(function() {
		location.hash=$(this).attr('href');
		$("div[class='modal-backdrop fade in']").remove();
		$("#staticchangepassword").attr('data-toggle', 'modal');
		$("#staticchangepassword").modal('hide');
	});
});

$(document).ready(function() {
	$(".staticdashboardindividualbreakupclose").click(function() {
		location.hash=$(this).attr('href');
		$("div[class='modal-backdrop fade in']").remove();
		$("#staticdashboardindividualbreakup").attr('data-toggle', 'modal');
		$("#staticdashboardindividualbreakup").modal('hide');
	});
});


$(document).ready(function() {
	$(".staticdashboardaggregatedataclose").click(function() {
		location.hash=$(this).attr('href');
		$("div[class='modal-backdrop fade in']").remove();
		$("#staticdashboardaggregatedata").attr('data-toggle', 'modal');
		$("#staticdashboardaggregatedata").modal('hide');
		$("#dashboardAutoSuggestId").show();
		$("#dashboardLocationFirstAutoSuggest").show();
		$("#dashboardLocationSecondAutoSuggest").show();
	});
});

$(document).ready(function() {
	$(".staticsupplierwapclose").click(function() {
		location.hash=$(this).attr('href');
		$("div[class='modal-backdrop fade in']").remove();
		$("#staticsupplierwap").attr('data-toggle', 'modal');
		$("#staticsupplierwap").modal('hide');
	});
});

$(document).ready(function() {
	$(".addNewAdmin").click(function() {
		swal("Error!","add new admin ","error");
		var newAdminFullName=$("#newadminfullname").val();
		var newAdminEmail=$("#newadminemail").val();
		if(newAdminFullName==""){
			swal("Invalid data!","Please Enter New Admin Full Name","error");
			return true;
		}
		if(newAdminEmail=="" || emailValidation(newAdminEmail)){
			swal("Invalid data!","Please Enter Proper Admin Email Id","error");
			return true;
		}
		var jsonData = {};
			jsonData.newAdminFname = newAdminFullName;
			jsonData.newAdminEmail = newAdminEmail;
			jsonData.loggedUser = $("#hiddenuseremail").text();
			var url="/config/newAdmin"
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
					  swal("Error!",data.message,"error");
					  if (data.result) {
						  location.href="/accountChanged";
						  alwaysScrollTop();
					  } else {
						  $("#newadminemail").val('').focus();
					  }
				  },
				  error: function (xhr, status, error) {
					if(xhr.status == 401){ doLogout(); }
				  }
		   });
	});
});

function listAlertUser(branchEntityId){
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.branchPrimKey=branchEntityId;
	var url="/config/getAlertUser"
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
    	  $(".alertDropDown").each(function(){
    		  $(this).children().remove();
    		  $(this).append('<option value="">--Please select--</option>');
    		  for(var i=0;i<data.alertUser.length;i++){
    			 var name=data.alertUser[i].name;
    			 var email=data.alertUser[i].email
    			 var text=email+"("+name+")";
    			 $(this).append('<option value="'+data.alertUser[i].email+'">'+text+'</option>');
    		  }
    	 });
      },
      error: function (xhr, status, error) {
      	 if(xhr.status == 401){ doLogout(); }
      }
   });
}

function changeCountry(elem){
	 var divName=$(elem).parent().parent("tr:first").attr('id');
	 var country=$(elem).find("option:selected").text();
	 var jsonData = {};
	 var selectedCountry=$(elem).val();
	 if(selectedCountry != "") {
		 jsonData.countryName = country;
		 var url="/config/getCurrency";
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
				var currData=data.currencyData[0].currency;
				if(currData!=null){
					if(confirm("Please Confirm '"+currData+"' as the Currency for '"+country+"'")){
						$("#"+divName+" select[class='currencyDropDown']").find("option[value='"+selectedCountry+"']").prop("selected","selected");
					}else{
						$("#"+divName+" select[class='currencyDropDown']").find("option:first").prop("selected","selected");
					}
				}else{
					$("#"+divName+" select[class='currencyDropDown']").find("option:first").prop("selected","selected");
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		 });
	 }
}

$(document).ready(function(){
	$("#statutoryDetails").change(function(){
	});
});

function changeLabel(elem){
	 bootbox.prompt("Do you want to change label for the field?Please enter new label.", function(result) {
	 if (result === null) {
	 }else{
		var labelDispVal=$(".input-block-level").val();
		var labelDomId=$(elem).attr('id');
		var jsonData = {};
		jsonData.labelDispValue = labelDispVal;
		jsonData.labelelemId = labelDomId;
		var oldwidth=$(elem).width();
		var url="/config/changeLabel"
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
	    	$("#"+labelDomId+"").text(data.newLabel[0].text);
	    	var margin_space=oldwidth-$(elem).width();
	    	if(margin_space>0)
	    	$(elem).attr('style',"margin-left: "+margin_space+"px");
	    },
	    error: function (xhr, status, error) {
	    	if(xhr.status == 401){ doLogout(); }
	    }
	  });
	  }
	 });
	 $(".input-block-level").val($(elem).text());
}

$(document).ready(function(){
	$('.multiBranch').each(function(){
		$(this).multiselect({
	        buttonWidth: '150px',
	        maxHeight:   150,
	        includeSelectAllOption: true,
	        enableFiltering :true,
	        /*
	        buttonText: function(options) {
	          if (options.length == 0) {
	                  return 'None selected <b class="caret"></b>';
	              }
	              else if (options.length > 6) {
	                  return options.length + ' selected  <b class="caret"></b>';
	              }
	              else {
	                  var selected = '';
	                  options.each(function() {
	              selected += $(this).text() + ', ';
	                  });

	                  return options.length + ' selected  <b class="caret"></b>';
	          }
	        }, */
	        onChange: function(element, checked) {
	        }
	      });
	});
})

//$(document).ready(function() {
//	$('.settingIcon').each(function() {
//		var $this = $(this);
//	    $this.popover({
//	    trigger: 'manual',
//	    animate: false,
//	    placement: 'left',
//	    html: true,
//	    content: $(".popoverContent").html()
//	  }).on("mouseenter", function() {
//		  $(this).popover("show");
//		  var headId=$(".mainnav").attr('id');
//		  if(headId=="head-nav"){
//			  $(this).popover("show");
//			  $(".mainnav").attr("id","header-nav");
//			  $(this).siblings(".popover").on("mouseleave", function() {
//		    	 $(this).hide();
//		      });
//			  $(".popover").css('left',1080);
//		  }
//	  }).on("mouseleave", function() {
//    	  var _this = this;
//    	  setTimeout(function() {
//    	  if (!$(".popover:hover").length) {
//    	     $(_this).popover("hide");
//    	     $(".mainnav").attr("id","head-nav");
//    	  }
//    	}, 100);
//    });
//  });
//});

//$(document).ready(function() {
//	$('.procurementicon').each(function() {
//		var $this = $(this);
//	    $this.popover({
//	    trigger: 'manual',
//	    animate: false,
//	    placement: 'left',
//	    html: true,
//	    content: $(".procPopoverContent").html()
//	  }).on("mouseenter", function() {
//		  var proccount=$("#proccount").text();
//		  if(proccount!=""){
//			  $(this).popover("show");
//			  var procContent=$(".procPopoverContent").html();
//			  var headId=$(".mainnav").attr('id');
//			  if(headId=="head-nav"){
//				  $(this).popover("show");
//				  $(".mainnav").attr("id","header-nav");
//				  $(this).siblings(".popover").on("mouseleave", function() {
//			    	 $(this).hide();
//			      });
//				  $(".popover-content").html(procContent);
//				  $(".popover").css('left',1080);
//			  }
//		  }
//	  }).on("mouseleave", function() {
//    	  var _this = this;
//    	  setTimeout(function() {
//    	  if (!$(".popover:hover").length) {
//    	     $(_this).popover("hide");
//    	     $(".mainnav").attr("id","head-nav");
//    	  }
//    	}, 100);
//    });
//  });
//});

$(document).ready(function() {
	$('.userpopoverinfo').each(function() {
		var infocontent=$(this).attr('longdesc')
		var $this = $(this);
	    $this.popover({
	    trigger: 'manual',
	    animate: false,
	    placement: 'right',
	    html: true,
	    content: infocontent
	  }).on("mouseenter", function() {
		   $(this).popover("show");
		   $(this).siblings(".popover").on("mouseleave", function() {
			   $(this).hide();
		   });
	  }).on("mouseleave", function() {
    	  var _this = this;
    	  setTimeout(function() {
    	  if (!$(".popover:hover").length) {
    	     $(_this).popover("hide");
    	  }
    	}, 100);
    });
  });

	$('.userpopoverinfoleft').each(function() {
		var infocontent=$(this).attr('longdesc')
		var $this = $(this);
	    $this.popover({
	    trigger: 'manual',
	    animate: false,
	    placement: 'custom_left',
	    html: true,
	    content: infocontent
	  }).on("mouseenter", function() {
		   $(this).popover("show");
		   $(this).siblings(".popover").on("mouseleave", function() {
			   $(this).hide();
		   });
	  }).on("mouseleave", function() {
    	  var _this = this;
    	  setTimeout(function() {
    	  if (!$(".popover:hover").length) {
    	     $(_this).popover("hide");
    	  }
    	}, 100);
    });
  });
});

$(document).ready(function(){
	$('#userapproverright').each(function() {
		var infocontent="System lets you set up rules for the users assigned with approval role. Below please select the branch for which this user can approve transactions. If you have 2 branches say, A & B, you can assign either branch A or branch B to this user and when the user logs in to approve any transaction, he will see only those items from Chart of account which are assigned to this branch. Further it allows you to set up monetary limits for the approver whereby only those transactions which fall in his limits will be directed to him from the creators. Therefore you can set up more than one approver per branch based on monetary limits. Alternatively, you can assign approval rights linked to chart of account items, whereby only certain account codes or account items can be directed to an approver. Its important to note that when a transaction goes to approver assigned to that branch, the approver can either approve, reject or send it for additional approval to another official in the business. It is recommended that you dont create more than one approver for any single monetary limit of item of chart of account. If you have not setup projects, you can ignore that column.";
		var $this = $(this);
	    $this.popover({
	    trigger: 'manual',
	    animate: false,
	    placement: 'right',
	    html: true,
	    content: infocontent
		}).on("mouseenter", function() {
			   $(this).popover("show");
			   $(this).siblings(".popover").on("mouseleave", function() {
				   $(this).hide();
			   });
		}).on("mouseleave", function() {
	    	  var _this = this;
	    	  setTimeout(function() {
	    	  if (!$(".popover:hover").length) {
	    	     $(_this).popover("hide");
	    	  }
	    	}, 100);
	    });
	});
});

function getRandomArbitrary() {
   return Math.floor((Math.random()*1000)+1);
}

function action(elem){
	 var optName = $(elem).text();
	 var hiringLabourId=$(elem).attr('id');
	 var jsonData = {};
    jsonData.useremail=$("#hiddenuseremail").text();
    jsonData.hiringRequestId=hiringLabourId;
    if(optName=="Obtain client approval" || optName=="Issue employment agreement"){
    var url = "/labour/labourDetails";
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
      	  var individualRecord=data;
      	  $("#myModal").remove();
      	  var modalToBeDisplayed=displayEmployeeDetails(individualRecord);
      	  $("#pendingHiring").append(modalToBeDisplayed);
      	  $("#labourHiring").show();
      	  $('#myModal').attr('data-toggle', 'modal');
      	  $("#myModal").modal('show');
      	  var jsonData={};
      	  jsonData.useremail =$("#hiddenuseremail").text();
      	  jsonData.hiringRequestId=hiringLabourId;
      	  if(optName=="Obtain client approval"){
      		  jsonData.status="client_approval_sent";
      	  }
      	  if(optName=="Issue employment agreement"){
      		jsonData.status="Employee Agreement Issued";
          	 var remarks = $('#hiringRemarks_' + hiringLabourId).val();
          	  var document = $('#hiringDocuments_' + hiringLabourId).val();
          	  if (!isEmpty(remarks)) {
          	   jsonData.remarks = $("#hiddenuseremail").text() + '#' + remarks;
          	  }
          	  if (!isEmpty(document)) {
          	   jsonData.document = $("#hiddenuseremail").text() + '#' + document;
          	  }
      	  }
      	  var url="/labour/action";
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
        },
        error: function(xhr, status, error) {
        	if(xhr.status == 401){ doLogout(); }
        }
     });
  }
  if(optName=="Click Once Client Approves"){
	  var jsonData={};
     jsonData.useremail =$("#hiddenuseremail").text();
     jsonData.hiringRequestId=hiringLabourId;
     jsonData.status="client_approved";
     var remarks = $('#hiringRemarks_' + hiringLabourId).val();
	  var document = $('#hiringDocuments_' + hiringLabourId).val();
	  if (!isEmpty(remarks)) {
	   jsonData.remarks = $("#hiddenuseremail").text() + '#' + remarks;
	  }
	  if (!isEmpty(document)) {
	   jsonData.document = $("#hiddenuseremail").text() + '#' + document;
	  }
     var url="/labour/action";
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

function displayEmployeeDetails(individualRecord) {
    var modal = '<div id="myModal" class="modal hide fade" data-toggle="modal" tabindex="-1"'+
    ' style="width:800px;margin-left:10%" role="dialog" aria-labelledby="myModalLabel"'+
    ' aria-hidden="true">' +
    '<div class="modal-header">' +
    '<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>' +
    '<h3 id="myModalLabel">Employee Details</h3> </div>' +
    '<div class="modal-body"  style="width:750px;margin-left:5px" id="Print_Div">'+
    '<table class="table table-bordered">'+
    '<tr class="info"><td colspan="3"><strong>First Name</strong> </br> '+
     individualRecord['empDetails']['emp_name']+'</td><td colspan="3"><strong>Middle Name</strong> </br> '+
     individualRecord['empDetails']['middle_name']+'</td><td colspan="3"><strong>Last Name</strong> </br> '+
     individualRecord['empDetails']['last_name']+'</td></tr>'+
    '<tr class="info"><td colspan="4"><strong>Employee Address</strong> </br> '+
    individualRecord['empDetails']['employeeAddress']+'<td colspan="2"><strong> Proposed Salary</strong></br>'+
    individualRecord['empDetails']['salary']+'</td><td colspan="3"><strong>Duration Of contract</strong></br>'+
    individualRecord['empDetails']['duration']+'</td></tr>'+
    '<tr class="info"><td colspan="2"><strong> Contact No.</strong></br>' +
    individualRecord['empDetails']['contactNo']+'</td><td colspan="2"><strong>Place Of Birth</strong></br>'+
    individualRecord['empDetails']['placeOfBirth']+'</td><td colspan="2"><strong>Citizenship</strong></br>' +
    individualRecord['empDetails']['citizenship']+'</td><td colspan="2"><strong>Pan Card</strong></br>' +
    individualRecord['empDetails']['panCard']+'</td><td colspan="2"><strong>Passport</strong></br>' +
    individualRecord['empDetails']['passport']+'</td></tr>'+
    '<tr class="info"><td colspan="9"><strong>Authorized To Work In</strong></td><tr>'+
    '<tr class="info"><td colspan="3"><strong> Country</strong></br></br>' +
    individualRecord['empDetails']['countryName1']+'</br>'+
    individualRecord['empDetails']['countryName2']+'</br>'+
    individualRecord['empDetails']['countryName3']+'</td><td colspan="3"><strong>Visa Number</strong></br></br>'+
    individualRecord['empDetails']['visaNumber1']+'</br>'+
    individualRecord['empDetails']['visaNumber2']+'</br>'+
    individualRecord['empDetails']['visaNumber3']+'</td><td colspan="3"><strong>Visa Type</strong></br></br>'+
    individualRecord['empDetails']['visaType1']+'</br>'+
    individualRecord['empDetails']['visaType2']+'</br>'+
    individualRecord['empDetails']['visaType3']+'</td></tr>'+
    '<tr class="info"><td colspan="9"><strong>Name, Ages and Relationship of dependents to accompany individual to country of assignment</strong></td><tr>'+
    '<tr class="info"><td colspan="3"><strong>Dependent Name</strong></br></br>'+
    individualRecord['empDetails']['dependents']+'</br>'+
    individualRecord['empDetails']['dependents1']+'</br>'+
    individualRecord['empDetails']['dependents2']+'</td><td colspan="3"><strong>Dependent Age</strong></br></br>'+
    individualRecord['empDetails']['dependentsAge']+'</br>'+
    individualRecord['empDetails']['dependentsAge1']+'</br>'+
    individualRecord['empDetails']['dependentsAge2']+'</td><td colspan="3"><strong>Dependent Relationship</strong></br></br>'+
    individualRecord['empDetails']['dependentsRelationship']+'</br>'+
    individualRecord['empDetails']['dependentsRelationship1']+'</br>'+
    individualRecord['empDetails']['dependentsRelationship2']+'</td></tr>'+
    '<tr class="success"><td colspan="5"><h5><strong> Education</strong></h5>'+
    '(include all college and universities)</td><td colspan="4"><strong><h5>'+
    'Language Proficiency</strong></h5></td></tr>'+
    '<tr class="success "><td colspan="2"><strong> Name And Location Of Institute</strong></br></br>'+
    individualRecord['empDetails']['institute1']+'</br>'+ individualRecord['empDetails']['institute2']+'</br>'+
    individualRecord['empDetails']['institute3']+'</td><td><strong>Major</strong></br></br>'+
    individualRecord['empDetails']['major1']+'</br>'+ individualRecord['empDetails']['major2']+'</br>'+
    individualRecord['empDetails']['major3']+'</td><td><strong>Degree</strong></br></br>' +
    individualRecord['empDetails']['degree1']+'</br>'+ individualRecord['empDetails']['degree2']+'</br>'+
    individualRecord['empDetails']['degree3']+'</td><td><strong>Date</strong></br></br>'+
    individualRecord['empDetails']['date1']+'</br>'+ individualRecord['empDetails']['date2']+'</br>'+
    individualRecord['empDetails']['date3']+'</td><td><strong>Language</strong></br></br>'+
    individualRecord['empDetails']['language1']+'</br>' + individualRecord['empDetails']['language2']+'</br>' +
    individualRecord['empDetails']['language3']+'</td><td><strong>Proficiency Speaking</strong></br></br>'+
    individualRecord['empDetails']['proficiencySpeaking1']+'</br>'+
    individualRecord['empDetails']['proficiencySpeaking2']+'</br>'+
    individualRecord['empDetails']['proficiencySpeaking3']+'</td><td><strong>Proficiency Reading</strong></br></br>'+
    individualRecord['empDetails']['proficiencyReading1']+'</br>'+
    individualRecord['empDetails']['proficiencyReading2']+'</br>'+
    individualRecord['empDetails']['proficiencyReading3']+'</td><td><strong>Proficiency Writing</strong></br></br>'+
    individualRecord['empDetails']['proficiencyWriting1']+'</br>'+
    individualRecord['empDetails']['proficiencyWriting2']+'</br>'+
    individualRecord['empDetails']['proficiencyWriting3']+'</td></tr>'+
    '<tr class="info"><td colspan="9"><h5><strong> Employment History</strong></h5></td><tr>'+
    '<tr class="info"><td colspan="2" rowspan="2">Position Title</br></br>' +
    individualRecord['empDetails']['positionTitle1']+'</br>'+ individualRecord['empDetails']['positionTitle2']+'</br>'+
    individualRecord['empDetails']['positionTitle3']+'<br/>'+individualRecord['empDetails']['positionTitle4']+'<br/>'+
    individualRecord['empDetails']['positionTitle5']+'</td><td colspan="3" rowspan="2">'+
    'Name And Address Of Employer</br></br>' + individualRecord['empDetails']['employer1']+'</br>'+
    individualRecord['empDetails']['employer2']+'</br>'+ individualRecord['empDetails']['employer3']+
    individualRecord['empDetails']['employer4']+'<br/>'+ individualRecord['empDetails']['employer5']+'</td><td colspan="3">Dates Of Employment</td><td>Annual Salary</td></tr>'+
    '<tr class="info"><td>From</br>' + individualRecord['empDetails']['from1']+'</br>'+
    individualRecord['empDetails']['from2']+'</br>'+ individualRecord['empDetails']['from3']+'<br/>'+
    individualRecord['empDetails']['from4']+'<br/>'+ individualRecord['empDetails']['from5']+'</td><td>To</br>' +
    individualRecord['empDetails']['to1']+'</br>'+ individualRecord['empDetails']['to2']+'</br>'+
    individualRecord['empDetails']['to3']+'<br/>'+individualRecord['empDetails']['to4']+'</br>'+
    individualRecord['empDetails']['to5']+'</td><td colspan="3">CTC</br>' + individualRecord['empDetails']['salary1']+'</br>'+
    individualRecord['empDetails']['salary2']+'</br>'+ individualRecord['empDetails']['salary3']+'<br/>'+
    individualRecord['empDetails']['salary4']+'</br>'+ individualRecord['empDetails']['salary5']+'</td></tr>'+

    '<tr class="info"><td colspan="9"><h5><strong>Professional Reference (Apart from current employer)</strong></h5></td><tr>'+
    '<tr class="info"><td colspan="2" rowspan="2">Name</br></br>' +
    individualRecord['empDetails']['prName1']+'</br>'+ individualRecord['empDetails']['prName2']+'</br>'+
    individualRecord['empDetails']['prName3']+'</td><td colspan="3" rowspan="2">'+
    'Employer Details</br></br>' + individualRecord['empDetails']['prEmployer1']+'</br>'+
    individualRecord['empDetails']['prEmployer2']+'</br>'+ individualRecord['empDetails']['prEmployer3']+
    '</td><td colspan="3" rowspan="2">Email</br></br>' + individualRecord['empDetails']['prEmail1']+'</br>'+
    individualRecord['empDetails']['prEmail2']+'</br>'+ individualRecord['empDetails']['prEmail3']+
    '</td><td colspan="3" rowspan="2">Phone</br></br>' + individualRecord['empDetails']['prPhone1']+'</br>'+
    individualRecord['empDetails']['prPhone2']+'</br>'+ individualRecord['empDetails']['prPhone3']+'</td></tr><tr></tr>'+

    '<tr class="info"><td colspan="9"><h5><strong>Consulting Services</strong></h5></td><tr>'+
    '<tr class="info"><td colspan="2"><strong>Consulting Service Name</strong></br></br>' +
    individualRecord['empDetails']['consultingServiceName1']+'</br>'+ individualRecord['empDetails']['consultingServiceName2']+'</br>'+
    individualRecord['empDetails']['consultingServiceName3']+'<br/>'+individualRecord['empDetails']['consultingServiceName4']+'<br/>'+
    individualRecord['empDetails']['consultingServiceName5']+'</td><td colspan="2"><strong>Name And Address Of Employer</strong></br></br>' + individualRecord['empDetails']['consultingServiceEmployer1']+'</br>'+
    individualRecord['empDetails']['consultingServiceEmployer2']+'</br>'+ individualRecord['empDetails']['consultingServiceEmployer3']+'</br>'+
    individualRecord['empDetails']['consultingServiceEmployer4']+'<br/>'+ individualRecord['empDetails']['consultingServiceEmployer5']+'</td><td colspan="2"><strong>Date</strong><br/><br/>'+
    individualRecord['empDetails']['consultingServicefrom1']+'</br>'+
    individualRecord['empDetails']['consultingServicefrom2']+'</br>'+ individualRecord['empDetails']['consultingServicefrom3']+'<br/>'+
    individualRecord['empDetails']['consultingServicefrom4']+'<br/>'+ individualRecord['empDetails']['consultingServicefrom5']+'</td><td colspan="1"><strong>No. Of Hours</strong><br/><br/>' + individualRecord['empDetails']['consultingServiceHours1']+'</br>'+
    individualRecord['empDetails']['consultingServiceHours2']+'</br>'+ individualRecord['empDetails']['consultingServiceHours3']+'<br/>'+
    individualRecord['empDetails']['consultingServiceHours4']+'</br>'+ individualRecord['empDetails']['consultingServiceHours5']+'</td><td colspan="2"><strong>Rate/Hour</strong><br/><br/>' + individualRecord['empDetails']['consultingServiceRate1']+'</br>'+
    individualRecord['empDetails']['consultingServiceRate2']+'</br>'+ individualRecord['empDetails']['consultingServiceRate3']+'<br/>'+
    individualRecord['empDetails']['consultingServiceRate4']+'</br>'+ individualRecord['empDetails']['consultingServiceRate5']+'</td></tr>'+
    '<tr class="info"><td colspan="9"><strong>Professional Certifications</strong></td><tr>'+
    '<tr class="info"><td colspan="2"><strong> Certification Name</strong></br></br>' +
    individualRecord['empDetails']['certification1']+'</br>'+
    individualRecord['empDetails']['certification2']+'</br>'+
    individualRecord['empDetails']['certification3']+'</td><td colspan="3"><strong>Number</strong></br></br>'+
    individualRecord['empDetails']['certificationNumber1']+'</br>'+
    individualRecord['empDetails']['certificationNumber2']+'</br>'+
    individualRecord['empDetails']['certificationNumber3']+'</td><td colspan="3"><strong>Technology</strong></br></br>'+
    individualRecord['empDetails']['technology1']+'</br>'+
    individualRecord['empDetails']['technology2']+'</br>'+
    individualRecord['empDetails']['technology3']+'</td><td colspan="3"><strong>Date</strong></br></br>'+
    individualRecord['empDetails']['certificationdate1']+'</br>'+
    individualRecord['empDetails']['certificationdate2']+'</br>'+
    individualRecord['empDetails']['certificationdate3']+'</td></tr>'+
    '<tr class="success"><td colspan="5">Signature Of Employee</td><td colspan="4">Date</td></tr>'+
    '</table> </div>' +
    '<div class="modal-footer"> <button id = "close_client_approval" class="btn"'+
    ' data-dismiss="modal" aria-hidden="true">Close</button>' +
    '<button class="btn btn-primary btn-idos" onClick=printEmployeeData($("#Print_Div"))>'+
    'Print</button> </div> </div>';
    return modal;
 }

function printEmployeeData(element){
    var data = element.html();
    var mywindow=window.open('','Print_Div', 'height=400, width=600');
    mywindow.document.write('<html><head><title>Employee Details</title>');
    mywindow.document.write('<link rel = "stylesheet" href = "assets/css/bootstrap.css">'+
      ' </head><body><h1>Employee Details</h1>');
    mywindow.document.write(data);
    mywindow.document.write('</body></html>');
    mywindow.print();
    mywindow.close();
    return true;
  }

function showTaggableCode(elem){
	var taggableValue=$(elem).val();
	if(taggableValue==""){
		$('#taggableCode').css('display', 'none');
	}else{
		if(taggableValue=="1"){
			$('#taggableCode').css('display', 'block');
		}else{
			$('#taggableCode').css('display', 'none');
		}
	}
}

function autotab1(original){
	if (original.getAttribute&&original.value.length==original.getAttribute("maxlength")){
		var divName=$(original).parent().parent("tr:first").attr('id');
	    var index;
	    if(original.name.substring(original.name.length-1,original.name.length)=="1"){
	    	index="2";
	    }
	    if(original.name.substring(original.name.length-1,original.name.length)=="2"){
	    	index="3";
	    }
	    var newdest=original.name.substring(0,original.name.length-1)+index;
	    if(divName==undefined){
	        var flag=1;
			$("input[name="+newdest+"]").each(function(){
			if(flag==1){
			$(this).focus();
			flag=0;
			}else{
			$(this).blur();
			}
			});
		}else{
		    $("#"+divName+" input[name="+newdest+"]").focus();
		}
	}
}

function createCOAVendor(elem,coaId){
	var coaName=$(elem).attr('name');
	var htmlform=$("#vendor-form-container").html();
	$(".vendor-form-container").slideDown('slow');
}

function changeHeaderclass(){
	$(".mainnav").attr("id","head-nav");
}

function displayCashierInformation(){
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	var url="/config/getCashierKl"
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
    	 $("#branchCahCountId").val("");
    	 if(data.cashierInformationData[0].branchCashCountId!=null && data.cashierInformationData[0].branchCashCountId!=""){
    		 $("#branchCahCountId").val(data.cashierInformationData[0].branchCashCountId);
    	 }
    	 $("#cashierInstruction").val(data.cashierInformationData[0].cashierKl);
    	 if(data.cashierInformationData[0].cashCreditedTotal!=null && data.cashierInformationData[0].cashCreditedTotal!=""){
    		 var creditAmount=data.cashierInformationData[0].cashCreditedTotal;
        	 $("#cashCreditedTotal").val(creditAmount.toFixed(2));
    	 }
    	 if(data.cashierInformationData[0].cashDebitedTotal!=null && data.cashierInformationData[0].cashDebitedTotal!=""){
	    	 var debitAmount=data.cashierInformationData[0].cashDebitedTotal;
	    	 $("#cashDebitedTotal").val(debitAmount.toFixed(2));
    	 }
    	 if(data.cashierInformationData[0].resultantCashTotal!=null && data.cashierInformationData[0].resultantCashTotal!=""){
    		 var resultantCash=data.cashierInformationData[0].resultantCashTotal;
    		 $("#resultantCashTotal").val(resultantCash.toFixed(2));
    	 }
    	 if(data.cashierInformationData[0].notesTotal!=null && data.cashierInformationData[0].notesTotal!=""){
    		 var notesTotal=data.cashierInformationData[0].notesTotal;
    		 $("#prevNotesTotal").val(notesTotal.toFixed(2));
    	 }
    	 if(data.cashierInformationData[0].coinsTotal!=null && data.cashierInformationData[0].coinsTotal!=""){
    		 var coinsTotal=data.cashierInformationData[0].coinsTotal;
    		 $("#prevCoinsTotal").val(coinsTotal.toFixed(2));
    	 }
    	 if(data.cashierInformationData[0].smallerCoinsTotal!=null && data.cashierInformationData[0].smallerCoinsTotal!=""){
    		 var smallerCoinsTotal=data.cashierInformationData[0].smallerCoinsTotal;
    		 $("#prevSmallerCoinsTotal").val(smallerCoinsTotal.toFixed(2));
    	 }
    	 if(data.cashierInformationData[0].grandTotal!=null && data.cashierInformationData[0].grandTotal!=""){
    		 var grandTotal=data.cashierInformationData[0].grandTotal;
    		 $("#prevGrandTotal").val(grandTotal.toFixed(2));
    	 }
    	 if(data.cashierInformationData[0].mainToPettyTotal!=null && data.cashierInformationData[0].mainToPettyTotal!=""){
    		 var mainToPettyTotal=data.cashierInformationData[0].mainToPettyTotal;
    		 $("#mainToPettyTotal").val(mainToPettyTotal.toFixed(2));
    	 }
    	 if(data.cashierInformationData[0].resultantPettyCash!=null && data.cashierInformationData[0].resultantPettyCash!=""){
    		 var resultantPettyCash=data.cashierInformationData[0].resultantPettyCash;
    		 $("#resultantPettyCash").val(resultantPettyCash.toFixed(2));
    	 }
    	 if(data.cashierInformationData[0].debbitedPettyCash!=null && data.cashierInformationData[0].debbitedPettyCash!=""){
    		 var debbitedPettyCash=data.cashierInformationData[0].debbitedPettyCash;
    		 $("#debittedPettyCashTotal").val(debbitedPettyCash.toFixed(2));
    	 }
    	 $("#bankBalanceTable tbody").html("");
    	 if(data.branchBankAccountsData.length>0){
	    	 for(var i=0;i<data.branchBankAccountsData.length;i++){
	    		 $("#bankBalanceTable").append('<tr id="branchBankAct'+data.branchBankAccountsData[i].id+'"><td><input type="hidden" name="bankId" id="bankId" value="'+data.branchBankAccountsData[i].id+'">Last Recorded Account Balance<br/><input type="text" name="bankAccountBalance'+data.branchBankAccountsData[i].id+'" id="bankAccountBalance'+data.branchBankAccountsData[i].id+'" value="'+data.branchBankAccountsData[i].lastRecordedAmountEntered+'" readonly="readonly" style="width:200px;">Bank Name<br/><input type="text" name="bankName" id="bankName" readonly="readonly" value="'+data.branchBankAccountsData[i].name+'" style="width:200px;"></td><td>Total Credit<br/><input type="text" name="bankTotlCredit'+data.branchBankAccountsData[i].id+'" id="bankTotlCredit'+data.branchBankAccountsData[i].id+'" value="'+data.branchBankAccountsData[i].totalCredit+'" readonly="readonly">Total Debit<br/><input type="text" name="bankTotalDebit'+data.branchBankAccountsData[i].id+'" id="bankTotalDebit'+data.branchBankAccountsData[i].id+'" value="'+data.branchBankAccountsData[i].totalDebit+'" readonly="readonly">Enter Amount<br/><input type="text" name="bankAmount" id="bankAmount" onkeypress="return onlyDotsAndNumbers(event)" onblur="javascript:calculatebankbalances(this);"></td><td style="width:200px;">Resultant Balance<br/><input type="text" name="bankResultantBalance'+data.branchBankAccountsData[i].id+'" id="bankResultantBalance'+data.branchBankAccountsData[i].id+'" value="'+data.branchBankAccountsData[i].resultantBalanceInAccount+'" readonly="readonly">Upload Statement<br/><input type="text" id="docuploadurl" name="bankstatementdocupload" readonly="readonly">'+
//	    		    	 '<input type="button" class="btn btn-primary btn-idos" id="bankstatementdocupload" value="Upload" onclick="uploadFile(this.id,this)">'
	    				 '<span id="bankstatementdocupload" class="btn-idos-flat-white btn-upload" style="margin-top: 25px;" onclick="uploadFile(this.id,this)"><i class="fa fa-upload pr-5"></i>Upload</span></td></tr>');
	    	 }
	    	 $("#bankBalanceTable").append('<tr><td></td><td><b>Total: </b><input type="text" name="grandBankBalanceTotal" id="grandBankBalanceTotal" readonly="readonly"></td><td></td><tr>');
    	 }
    	 $(".approverControllerUserList").children().remove();
    	 $(".approverControllerUserList").append('<option value="">Please Select</option>');
    	 for(var i=0;i<data.approverControllerUserData.length;i++){
    		 $(".approverControllerUserList").append('<option value="'+data.approverControllerUserData[i].userID+'">'+data.approverControllerUserData[i].userEmail+'</option>');
    	 }
      },
      error: function (xhr, status, error) {
      	if(xhr.status == 401){ doLogout(); }
      }
   });
}

$(document).ready(function() {
	$(".completeCashCountForTheDay").click(function() {
		var totalNote=$("#noteTotal").val();
		var totalCoin=$("#coinTotal").val();
		var grandTotal=$("#grandTotal").val();
		var smallerCoinsTotal=$("#smallerCoinsTotal").val();
		if(grandTotal=="" || grandTotal==0){
			swal("Invalid data!","Please Enter The Cash Count Denomination Details","error");
		    return true;
	    }
		var notesDenomination    = $('input[name="notesDenomination"]').map(function () {
	 		return this.value;
	 	}).get();
	    var notesQuantity    = $('input[name="notesCount"]').map(function () {
	 		return this.value;
	 	}).get();
	    var notesTotal    = $('input[name="notesTotal"]').map(function () {
	 		return this.value;
	 	}).get();
	    var coinsDenomination    = $('input[name="coinsDenomination"]').map(function () {
	 		return this.value;
	 	}).get();
	    var coinsQuantity    = $('input[name="coinsCount"]').map(function () {
	 		return this.value;
	 	}).get();
	    var coinsTotal    = $('input[name="coinsTotal"]').map(function () {
	 		return this.value;
	 	}).get();
	    var branchBankAct=$('input[name="bankId"]').map(function () {
	 		return this.value;
	 	}).get();
	    var branchBankActBalance=$('input[name="bankAmount"]').map(function () {
	 		return this.value;
	 	}).get();
	    var branchBankActStatements=$('input[name="bankstatementdocupload"]').map(function () {
	 		return this.value;
	 	}).get();
	    var noteDen="";var noteQty="";var noteTotal="";var coinDen="";var coinQty="";var coinTotal="";
	    var branchBankId="";var branchBankAcountBalance="";var branchBankActStatement="";
	    for (var i=0;i<notesDenomination.length;i++){
	    	if(notesDenomination[i]!=""){
	    		noteDen+=notesDenomination[i]+",";
	    	}else{
	    		noteDen+=" "+",";
	    	}
	    	if(notesQuantity[i]!=""){
	    		noteQty+=notesQuantity[i]+",";
	    	}else{
	    		noteQty+=" "+",";
	    	}
	    	if(notesTotal[i]!=""){
	    		noteTotal+=notesTotal[i]+",";
	    	}else{
	    		noteTotal+=" "+",";
	    	}
	    }
	    for(var i=0;i<coinsDenomination.length;i++){
	    	if(coinsDenomination[i]!=""){
	    		coinDen+=coinsDenomination[i]+",";
	    	}else{
	    		coinDen+=" "+",";
	    	}
	    	if(coinsQuantity[i]!=""){
	    		coinQty+=coinsQuantity[i]+",";
	    	}else{
	    		coinQty+=" "+",";
	    	}
	    	if(coinsTotal[i]!=""){
	    		coinTotal+=coinsTotal[i]+",";
	    	}else{
	    		coinTotal+=" "+",";
	    	}
	    }
	    for(var i=0;i<branchBankAct.length;i++){
	    	if(branchBankAct[i]!=""){
	    		branchBankId+=branchBankAct[i]+",";
	    		if(branchBankActBalance[i]!=""){
	    			branchBankAcountBalance+=branchBankActBalance[i]+",";
	    		}else{
	    			branchBankAcountBalance+="0.0"+",";
	    		}
	    		if(branchBankActStatements[i]!=""){
	    			branchBankActStatement+=branchBankActStatements[i]+",";
	    		}else{
	    			branchBankActStatement+=" "+",";
	    		}
	    	}
	    }
	   	var jsonData = {};
	   	jsonData.useremail=$("#hiddenuseremail").text();
  	    jsonData.newNotesTotal = totalNote;
  	    jsonData.newCoinsTotal = totalCoin;
  	    jsonData.newGrandTotal = grandTotal;
  	    jsonData.newSmallerCoinTotal=smallerCoinsTotal;
  	    jsonData.newNotesDenomination=noteDen.substring(0, noteDen.length-1);
  	    jsonData.newNotesQuantity=noteQty.substring(0, noteQty.length-1);
  	    jsonData.newNoteTotal=noteTotal.substring(0, noteTotal.length-1);
  	    jsonData.newCoinsDenomination=coinDen.substring(0, coinDen.length-1);
	    jsonData.newCoinsQuantity=coinQty.substring(0, coinQty.length-1);
	    jsonData.newCoinTotal=coinTotal.substring(0, coinTotal.length-1);
	    jsonData.branchBankActId=branchBankId.substring(0, branchBankId.length-1);
	    jsonData.branchBankActBalance=branchBankAcountBalance.substring(0, branchBankAcountBalance.length-1);
	    jsonData.branchBankActBalanceStatement=branchBankActStatement;
  		var url="/cashier/configCashCount"
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
        	  $("#cashCountNotesDetailsTable tbody tr[id*=dyncashCountNotesRow]").remove();
        	  $("#cashCountCoinDetailsTable tbody tr[id*=dyncashCountCoinsRow]").remove();
        	  $("input[id='denomination']").val("");$("#noteTotal").val("");
        	  $("input[id='count']").val("");$("#coinTotal").val("");
        	  $("input[id='total']").val("");$("#grandTotal").val("");
        	  $("input[name='bankAmount']").val("");
        	  $('input[name="bankstatementdocupload"]').val("");
        	  $('input[name="cashcountdocupload"]').val("");
        	  $("input[id='smallerCoinsTotal']").val("");
        	  $("input[name='grandBankBalanceTotal']").val("");
        	  $("#cashCreditedTotal").val(data.prevRecordedData[0].cashCreditedTotal);
        	  $("#cashDebitedTotal").val(data.prevRecordedData[0].cashDebitedTotal);
        	  $("#resultantCashTotal").val(data.prevRecordedData[0].resultantCashTotal);
        	  $("#prevNotesTotal").val(data.prevRecordedData[0].notesTotal);
        	  $("#prevCoinsTotal").val(data.prevRecordedData[0].coinsTotal);
        	  $("#prevSmallerCoinsTotal").val(data.prevRecordedData[0].smallerCoinsTotal);
        	  $("#prevGrandTotal").val(data.prevRecordedData[0].grandTotal);
         	  $("#mainToPettyTotal").val(data.prevRecordedData[0].mainToPettyTotal);
        	  $("#resultantPettyCash").val(data.prevRecordedData[0].resultantPettyCash);
        	  $("#debittedPettyCashTotal").val(data.prevRecordedData[0].debbitedPettyCash);
        	  $("#bankBalanceTable tbody").html("");
        	  if(data.branchBankAccountsData.length>0){
     	    	 for(var i=0;i<data.branchBankAccountsData.length;i++){
     	    		 $("#bankBalanceTable").append('<tr id="branchBankAct'+data.branchBankAccountsData[i].id+'"><td><input type="hidden" name="bankId" id="bankId" value="'+data.branchBankAccountsData[i].id+'">Last Recorded Account Balance<br/><input type="text" name="bankAccountBalance'+data.branchBankAccountsData[i].id+'" id="bankAccountBalance'+data.branchBankAccountsData[i].id+'" value="'+data.branchBankAccountsData[i].lastRecordedAmountEntered+'" readonly="readonly" style="width:200px;">Bank Name<br/><input type="text" name="bankName" id="bankName" readonly="readonly" value="'+data.branchBankAccountsData[i].name+'" style="width:200px;"></td><td>Total Credit<br/><input type="text" name="bankTotlCredit'+data.branchBankAccountsData[i].id+'" id="bankTotlCredit'+data.branchBankAccountsData[i].id+'" value="'+data.branchBankAccountsData[i].totalCredit+'" readonly="readonly">Total Debit<br/><input type="text" name="bankTotalDebit'+data.branchBankAccountsData[i].id+'" id="bankTotalDebit'+data.branchBankAccountsData[i].id+'" value="'+data.branchBankAccountsData[i].totalDebit+'" readonly="readonly">Enter Amount<br/><input type="text" name="bankAmount" id="bankAmount" onkeypress="return onlyDotsAndNumbers(event)" onblur="javascript:calculatebankbalances(this);"></td><td style="width:200px;">Resultant Balance<br/><input type="text" name="bankResultantBalance'+data.branchBankAccountsData[i].id+'" id="bankResultantBalance'+data.branchBankAccountsData[i].id+'" value="'+data.branchBankAccountsData[i].resultantBalanceInAccount+'" readonly="readonly">Upload Balance Statement<br/><input type="text" id="docuploadurl" name="bankstatementdocupload" readonly="readonly">'+
//     	    				'<input type="button" class="btn btn-primary btn-idos" id="bankstatementdocupload" value="Upload" onclick="uploadFile(this.id,this)">'
	    				 '<span id="bankstatementdocupload" class="btn-idos-flat-white btn-upload" style="margin-top: 25px;" onclick="uploadFile(this.id)"><i class="fa fa-upload pr-5"></i>Upload</span></td></tr>');
     	    	 }
     	    	 $("#bankBalanceTable").append('<tr><td></td><td><b>Total: </b><input type="text" name="grandBankBalanceTotal" id="grandBankBalanceTotal" readonly="readonly"></td><td></td><tr>');
         	 }
        	  $('.notify-success').show();
	          $("#notificationMessage").html("Branch cash balance,denomination and bank balance has been added successfully.");
	          alwaysScrollTop();
          },
          error: function (xhr, status, error) {
          		if(xhr.status == 401){ doLogout(); }
          }
       });
	});
});

function calculatebankbalances(elem){
	var totalBalances=0;
	$("input[name='bankAmount']").each(function(){
		if($(this).val()!=""){
			totalBalances+=parseFloat($(this).val());
		}
	});
	$("#grandBankBalanceTotal").val(parseFloat(totalBalances));
}

function calculaterowdenomination(elem){
	var noteTotal=0;var coinTotal=0;var smallerCoinTotal=0;
	$("#notesTotal").val("");
	$("#coinsTotal").val("");
	var elemTr=$(elem).parent().parent('tr:first').attr('id');
	var elemTrDenomination=$("#"+elemTr+" input[id='denomination']").val();
	var elemTrCount=$("#"+elemTr+" input[id='count']").val();
	if(elemTrDenomination!="" && elemTrCount!=""){
		var total=elemTrDenomination*elemTrCount;
		$("#"+elemTr+" input[id='total']").val(total);
	}else{
		$("#"+elemTr+" input[id='total']").val("");
	}
	$("input[name='notesTotal']").each(function(){
		if($(this).val()!=""){
			noteTotal+=parseInt($(this).val());
		}
	});
	$("#noteTotal").val(noteTotal);
	$("input[name='coinsTotal']").each(function(){
		if($(this).val()!=""){
			coinTotal+=parseInt($(this).val());
		}
	});
	$("#coinTotal").val(coinTotal);
	if($("#smallerCoinsTotal").val()!=""){
		smallerCoinTotal=$("#smallerCoinsTotal").val();
	}
	var grandTotal=parseInt(noteTotal)+parseInt(coinTotal)+parseFloat(smallerCoinTotal);
	$("#grandTotal").val(grandTotal);
}

$(document).ready(function(){
	$('#searched-coadata-container-close'). click(function(){
		$(".searchedContent").html("");
		$(".searched-coadata-container").hide();
		$("#existingCOA").val("");
	});
});

$(document).ready(function() {
	//changed from searchControllerOperData to dashboard-search
	$(".dashboard-search").click(function() {
		var operDataFor=$("#operationDataFor option:selected").val();
		var returnErr=operationalDataSearch(operDataFor);
		var selected = $('.operationalSearchData #operationDataFor').val();
		var subType = '';
		var jsonData = {};
		jsonData.useremail=$("#hiddenuseremail").text();
		jsonData.type = selected;
		if (2 == selected) {
			subType = $('.operationalSearchData #operavailableBranchesDataTypes').val()
			jsonData.subType = subType;
			jsonData.fetchDataId = $('.operationalSearchData #operavailableBranches').val();
		} else if (3 == selected) {
			jsonData.fetchDataId = $('.operationalSearchData #operavailableChartsOfAcconts').val();
		} else if (4 == selected) {
			jsonData.fetchDataId = $('.operationalSearchData #operavailableProjects').val();
		} else if (5 == selected) {
			jsonData.fetchDataId = $('.operationalSearchData #operavailableVendors').val();
		} else if (6 == selected) {
			jsonData.fetchDataId = $('.operationalSearchData #operavailableCustomers').val();
		} else if (7 == selected) {
			jsonData.fetchDataId = $('.operationalSearchData #operavailableUsers').val();
		} else if (8 == selected) {
			jsonData.fetchDataId = $('.operationalSearchData #operavailableTravelGroup').val();
		} else if (9 == selected) {
			jsonData.fetchDataId = $('.operationalSearchData #operavailableExpenseGroup').val();
		}
		if(returnErr==false){
			var url="/dashboard/operationalDataSearch"
			$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="60px" width="60px" />' });
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
					$.unblockUI();
					if (null != data || undefined != data) {
						if (1 == selected) {
							searchOperationalData.createOrganizationDataTable(data);
						} else if (2 == selected) {
							if (1 == subType) {
								searchOperationalData.createOperaBranchOfficialsDetails(data);
							} else if (2 == subType) {
								searchOperationalData.createOperaBranchStatutoryDetails(data);
							} else if (3 == subType) {
								searchOperationalData.createOperaOperationalReminders(data);
							} else if (4 == subType) {
								searchOperationalData.createOperaDepositBoxDetails(data);
							} else if (5 == subType) {
								searchOperationalData.createOperaBranchInsurance(data);
							} else if (6 == subType) {
								searchOperationalData.createOperaBankAccountDetails(data);
							}
						} else if (3 == selected) {
							searchOperationalData.createChartOfAccounts(data);
						} else if (4 == selected) {
							searchOperationalData.createProjects(data);
						} else if (5 == selected || 6 == selected) {
							searchOperationalData.createVendorOrCustomerDataTable(data, selected);
						} else if (7 == selected) {
							searchOperationalData.createUser(data);
						} else if (8 == selected) {
							searchOperationalData.createTravelGroup(data);
						} else if (9 == selected) {
							searchOperationalData.createExpenseGroup(data);
						}
					}
				},
				error: function (xhr, status, error) {
					$.unblockUI();
					if(xhr.status == 401){ doLogout(); }
				}
			});
		}
	});

	$('.operationalSearchData #operationDataFor').on('change', function() {
		var selected = $(this).val();
		var url = '';
		var dataId = '';
		var jsonData = {};
		var result = '';
		jsonData.useremail = $("#hiddenuseremail").text();
		if (selected == 1) {
			searchOperationalData.resetOperationalDropdowns();
		} else if (selected == 2) {
			dataId = '#operavailableBranches';
			jsonData.fetch = 'branch';
			jsonData.type = selected;
			url = "/dashboard/dashboardGetBranchProjectOperation";
		} else if (selected == 3) {
			dataId = '#operavailableChartsOfAcconts';
			jsonData.fetch = 'accounts';
			jsonData.type = selected;
			url = "/dashboard/dashboardGetBranchProjectOperation";
		} else if (selected == 4) {
			dataId = '#operavailableProjects';
			jsonData.fetch = 'project';
			jsonData.type = selected;
			url = "/dashboard/dashboardGetBranchProjectOperation";
		} else if (selected == 5) {
			dataId = '#operavailableVendors';
			jsonData.fetch = 'vendors';
			jsonData.type = 1;
			url = "/dashboard/dashboardGetVendorsOrCustomers";
		} else if (selected == 6) {
			dataId = '#operavailableCustomers';
			jsonData.fetch = 'customers';
			jsonData.type = 2;
			url = "/dashboard/dashboardGetVendorsOrCustomers";
		} else if (selected == 7 || selected == 8 || selected == 9) {
			if (selected == 7) {
				dataId = '#operavailableUsers';
				jsonData.fetch = 'users';
			} else if (selected == 8) {
				dataId = '#operavailableTravelGroup';
				jsonData.fetch = 'travelGroup';
			} else if (selected == 9) {
				dataId = '#operavailableExpenseGroup';
				jsonData.fetch = 'expenseGroup';
			}
			jsonData.type = selected;
			url = "/dashboard/dashboardGetBranchProjectOperation";
		}
		if ('' != url) {
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
					searchOperationalData.operationalDataSuccess(data, selected, dataId);
				},
				error: function (xhr, status, error) {
					if(xhr.status == 401){ doLogout(); }
				}
			});
		} else {
			searchOperationalData.resetOperationalDropdowns();
		}
	});

	$(".operationalSearchData #operavailableBranches").on('change', function() {
		if ($('.operationalSearchData #operavailableBranchesDataTypes').children().length < 7) {
			$('.operationalSearchData #operavailableBranchesDataTypes').append('<option value="1">Branch Officers</option>'
					+ '<option value="2">Branch Statutory</option><option value="3">Operational Reminders</option>'
					+ '<option value="4">Branch Safe Deposit Box</option><option value="5">Branch Insurance</option>'
					+ '<option value="6">Bank Account Details</option>');
		}
	});
});

function operationalDataSearch(operDataFor){
	if(operDataFor==1){
		return false;
	} else if(operDataFor==2){
		var selectedBranch=$("#operavailableBranches option:selected").val();
		if(selectedBranch==""){
			swal("Empty field!","Please select the branch for which you want to search operational data","error");
			return true;
		}
		if(selectedBranch!=""){
			var selectedBranchOperationalDataType=$("#operavailableBranchesDataTypes option:selected").val();
			if(selectedBranchOperationalDataType==""){
				swal("Empty field!","Please select branch operational data type.","error");
				return true;
			}
			return false;
		}else{
			return false;
		}
	} else if (operDataFor == 3) {
		var selectedVendor = $('.operationalSearchData #operavailableChartsOfAcconts').val();
		if ('' == selectedVendor) {
			swal('Empty field!','Please select a chart of account.','error');
			return true;
		}
		return false;
	} else if (operDataFor == 4) {
		var selectedVendor = $('.operationalSearchData #operavailableProjects').val();
		if ('' == selectedVendor) {
			swal('Empty field!','Please select a project.','error');
			return true;
		}
		return false;
	} else if (operDataFor == 5) {
		var selectedVendor = $('.operationalSearchData #operavailableVendors').val();
		if ('' == selectedVendor) {
			swal('Empty field','Please select a vendor.','error');
			return true;
		}
		return false;
	} else if (operDataFor == 6) {
		var selectedCustomer = $('.operationalSearchData #operavailableCustomers').val();
		if ('' == selectedCustomer) {
			swal('Empty field','Please select a customer.','error');
			return true;
		}
		return false;
	} else if (operDataFor == 7) {
		var selectedUser = $('.operationalSearchData #operavailableUsers').val();
		if ('' == selectedUser) {
			swal('Empty field','Please select a user.','error');
			return true;
		}
		return false;
	} else if (operDataFor == 8) {
		var selectedUser = $('.operationalSearchData #operavailableTravelGroup').val();
		if ('' == selectedUser) {
			swal('Empty field','Please select a travel group.','error');
			return true;
		}
		return false;
	} else if (operDataFor == 9) {
		var selectedUser = $('.operationalSearchData #operavailableExpenseGroup').val();
		if ('' == selectedUser) {
			swal('Empty field','Please select a expense group.','error');
			return true;
		}
		return false;
	}
}

var searchOperationalData = {
	clearOperationalDataStr : '<div id="search-launch-top"><i class="fa fa-times fa-1x close-legend" style="top: -17px;" id="dashBoard" onclick="searchOperationalData.resetOperationalDataResult()"></i></div>',
	operationalDataSuccess : function(data, selected, dataId) {
		searchOperationalData.resetOperationalDropdowns();
		var result = data.result;
		for (var i = 0; i < result.length; i++) {
			$('.operationalSearchData ' + dataId).append('<option value="' + result[i]['itemId'] + '">' + result[i]['itemName'] + '</option>');
		}
	},
	resetOperationalDropdowns : function() {
		$('.operationalSearchData #operavailableBranches').html('<option value="">---Please Select---</option>');
		$('.operationalSearchData #operavailableBranchesDataTypes').html('<option value="">---Please Select---</option>');
		$('.operationalSearchData #operavailableChartsOfAcconts').html('<option value="">---Please Select---</option>');
		$('.operationalSearchData #operavailableProjects').html('<option value="">---Please Select---</option>');
		$('.operationalSearchData #operavailableVendors').html('<option value="">---Please Select---</option>');
		$('.operationalSearchData #operavailableCustomers').html('<option value="">---Please Select---</option>');
		$('.operationalSearchData #operavailableUsers').html('<option value="">---Please Select---</option>');
		$('.operationalSearchData #operavailableTravelGroup').html('<option value="">---Please Select---</option>');
		$('.operationalSearchData #operavailableExpenseGroup').html('<option value="">---Please Select---</option>');
	},
	resetOperationalDataResult : function() {
		$('#operationalDataResult').hide();
		$('#operationalDataResult').empty();
	},
	downloadOperationalDocument : function(ele) {
		var value = $(ele).val();
		if (1 == value) {
			downloadOrganizationCOA();
		} else if (2 == value) {
			searchOperationalData.downloadDetails("/dashboard/downloadOperationalVendorCustomer", 1);
		} else if (3 == value) {
			searchOperationalData.downloadDetails("/dashboard/downloadOperationalVendorCustomer", 2);
		} else if (4 == value) {
			searchOperationalData.downloadDetails("/dashboard/downloadBudgetDetails", 0);
		}
	},
	downloadDetails : function(url, type) {
		var jsonData = {};
		jsonData.useremail=$("#hiddenuseremail").text();
		jsonData.type = type;
		downloadFile(url, "POST", jsonData, "Error on download!");
//			var url="/dashboard/downloadOperationalVendorCustomer"
		/*$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
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
				var dt = new Date().toString();
				var fileName=data.filename;
				var url='assets/OrgVendorCustomer/'+fileName+'?unique='+dt;
				$.unblockUI();
				window.open(url);
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});*/
	},
	createOrganizationDataTable : function(data) {
		searchOperationalData.resetOperationalDataResult();
		var result = data.organization[0];
		var accessObj = result.documents;
		$('#operationalDataResult').empty();
		var url = result.webUrl;
		if (url.indexOf('http://') == -1 || url.indexOf('https://') == -1) {
			url = 'http://' + url;
		}
		var downloadDocs = '<select id=operaOrgSecond" onchange="searchOperationalData.downloadOperationalDocument(this)"><option value="">---Download---</option>'
							+ '<option value="1">Chart Of Accounts</option>'
							+ '<option value="2">Vendor Details</option>'
							+ '<option value="3">Customer Details</option>'
							+ '<option value="4">Budget Details</option></select>';
		var organizationTable = searchOperationalData.clearOperationalDataStr + '<h5 class="alert success">Organization</h5>'
							+ '<div id="operaOrganizationDetails">'
							+ '<table class="table table-bordered operationalDataDashBoard transaction-create" id="operaOrganizationDetailsTable" style="margin-top: 0px;">'
							+ '<thead class="tablehead1">'
							+ '<tr class="operaOrgDetails"><th>Company Name & Address</th><th>Email & Phone Number</th><th>Website</th><th>HO Country</th><th>HO Currency</th>'
							+ '<th>Financial Start Year</th><th>Financial End Year</th><th>Organization Documents</th></tr></thead>'
							+ '<tbody><tr class="operaOrgDetails">'
							+ '<td><b>Company: </b>' + result.name + '<br /><b>Address:</b> ' + result.address + '</td>'
							+ '<td><b>Email:</b>' + result.email + '<br /><b>Phone:</b> ' + result.phone + '</td>'
							+ '<td><a href="' + url + '" target="_blank">' + result.webUrl + '</a></td><td>' + result.country + '</td>'
							+ '<td>' + result.currency + '</td><td>' + result.financialStart + '</td>'
							+ '<td>' + result.financialEnd + '</td><td id="operaOrgMultipleSelect"><select id="orgDocuments" name="orgDocuments"><option value="">---Download---</option></select><br><button id="orgDocumentsDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button><br /><b>Download Documents</b>' + downloadDocs + '</td></tr></tbody></table></div>'
		$('#operationalDataResult').append(organizationTable);
		$('.operationalDataDashBoard td b').after('<br/>');
		        //var inkblob, fileName = '';
				/*$(accessObj).each(function(i) {
					if ('' != accessObj[i]) {
						//inkblob = { url: accessObj[i]};
						//filepicker.setKey('A6zVM3VmDQhqeTq6sF209z');
						//filepicker.setKey('A7ucPpqRuR46F7OVE8CHJz');
						//filepicker.stat(inkblob, {filename: true},function(metadata) {
						//    fileName=JSON.stringify(metadata.filename);
						/*filepicker.retrieve(inkblob, { metadata: true }).then(function(response){
							  var fileName=JSON.stringify(response.filename);
							$("#operaOrganizationDetails select[id='orgDocuments']").append('<option value="'+accessObj[i]+'">'+fileName+'</option>');
						});*//*


						var transTrID = 'operaOrganizationDetails';
						var fileURLWithUser=accessObj[i].substring(0,accessObj[i].length).split(',');

					for(var j=0;j<fileURLWithUser.length;j++){
						var fileUrl=fileURLWithUser[j].substring(0,fileURLWithUser[j].length).split('#')
						   retrieveFile("", fileUrl[1], transTrID, 'orgDocuments');
					}
					}
				});*/
		var transTrID = 'operaOrganizationDetails';
		accessObj = accessObj.toString();
		fillSelectElementWithUploadedDocs(accessObj, transTrID, 'orgDocuments');
		$('#operationalDataResult').fadeIn(500);
	},
	fetchBranchFileName : function(result, fetchId, updateId) {
		$(result).each(function(i) {
			searchOperationalData.fetchSingleFileName(result[i], fetchId, updateId, i);
		});
	},
	fetchSingleFileName : function(result, fetchId, updateId, i) {
		var inkblob, fileName = '';
		if ('' != result[fetchId]) {
			var fileUrl = result[fetchId];
			var fileUrlTmp = fileUrl.substring(fileUrl.lastIndexOf('/')+1);
			filepicker.retrieve(fileUrlTmp, { metadata: true }).then(function(response){
	  			fileName=JSON.stringify(response.filename);
				$(updateId + i).html('<a href="#" onclick="downloadfile(\'' + result[fetchId] + '\')">' + fileName + '</a>');
			});
		} else {
			fileName = '';
			$(updateId + i).html('');
		}
	}, /*fetchMultipleFileName : function(result, fetchId, updateId, i) {
        if(i==5){

            var fileURLWithUser=result.contractPO.substring(0,result.contractPO.length).split(',');
        }else if(i==6){

            var fileURLWithUser=result.priceList.substring(0,result.priceList.length).split(',');
        }
        for(var j=0;j<fileURLWithUser.length;j++){
        var inkblob, fileName = '';
        if ('' != fileURLWithUser[j]) {

            var fileUrl = fileURLWithUser[j].substring(0,fileURLWithUser[j].length).split('#');
            var fileUrlWithoutUser=fileUrl[1];

            var fileUrlTmp = fileUrl[1].substring(fileUrl[1].lastIndexOf('/')+1);
            filepicker.retrieve(fileUrlTmp, { metadata: true }).then(function(response){
                  fileName=JSON.stringify(response.filename);

                  var data='<a href="#" onclick="downloadfile(\''+fileUrlWithoutUser+ '\')">' + fileName + '</a><br />'
                //$(updateId).html('<a href="#" onclick="downloadfile("' + fileURLWithUser[j] +'")">' + fileName + '</a><br />');
                  $(updateId).append(data);
            });
        } else {
            fileName = '';
            $(updateId).html('');
        }
        }
    },*/
	createOperaBranchOfficialsDetails : function(data) {
		searchOperationalData.resetOperationalDataResult();
		var result = data.officials;
		$('#operationalDataResult').empty();
		var officersTable = searchOperationalData.clearOperationalDataStr;
		if (undefined !== result && result.length > 0) {
			officersTable += '<h5 class="alert success">Branch Officers: ' + data.branchname + '</h5>'
						+ '<div id="operaBranchOfficersDetails">'
						+ '<table class="table table-hover table-striped table-bordered operationalDataDashBoard transaction-create" id="operaBranchOfficialsTable" style="margin-top: 0px;">'
						+ '<thead class="tablehead1">'
						+ '<tr><th>Officer Name</th><th>Designation</th><th>Country</th><th>City</th><th>Email</th>'
						+ '<th>Official Phone Number</th><th>Personal Phone Number</th><th>Download ID</th></tr></thead>'
						+ '<tbody>';
//					if (result.length > 0) {
			$(result).each(function(i) {
				officersTable += ' <tr><td>' + result[i]['officialName'] + '</td><td>' + result[i]['designation'] + '</td>'
						+ '<td>' + result[i]['country'] + '</td><td>' + result[i]['city'] + '</td>'
						+ '<td>' + result[i]['email'] + '</td><td>' + result[i]['officialPhoneNumber'] + '</td>'
						+ '<td>' + result[i]['personalPhoneNumber'] + '</td><td id="operaBranchOfficersFile' + i + '"></td></tr>';
			});
			officersTable += '</tbody></table></div>';
			searchOperationalData.fetchBranchFileName(result, 'uploadId', 'td#operaBranchOfficersFile');
		} else {
			officersTable += '<div style="text-align: center">No Data Available for the selected value!</div>';
		}
		$('#operationalDataResult').append(officersTable);
		$('.operationalDataDashBoard td b').after('<br/>');
		$('#operationalDataResult').fadeIn(500);
	},
	createOperaBranchStatutoryDetails : function(data) {
		searchOperationalData.resetOperationalDataResult();
		var result = data.statutoryDetails;
		$('#operationalDataResult').empty();
		var branchStatutoryTable = searchOperationalData.clearOperationalDataStr;
		var displayInInvoice = 'No';
		if (undefined !== result && result.length > 0) {
			branchStatutoryTable += '<h5 class="alert success">Branch Statutory Details: ' + data.branchname + '</h5>'
						+ '<div id="operaBranchStatutoryDetails">'
						+ '<table class="table table-hover table-striped table-bordered operationalDataDashBoard transaction-create" id="operaBranchStatutoryTable" style="margin-top: 0px;">'
						+ '<thead class="tablehead1">'
						+ '<tr><th>Statutory Details</th><th>Registration Number</th><th>Supporting Documents</th><th>Validity</th><th>Alert For Action</th>'
						+ '<th>Alert For Information</th><th>Name & Address of Consultant </th><th>Remarks</th></tr></thead>'
						+ '<tbody>';
//					if (result.length > 0) {
			$(result).each(function(i) {
				if (1 == parseInt(result[i]['inInvoice'])) {
					displayInInvoice = 'Yes';
				} else {
					displayInInvoice = 'No';
				}
				branchStatutoryTable += ' <tr><td>' + result[i]['statDetails'] + '</td><td>' + result[i]['regNumber'] + '<br /><b>Display in invoice?</b> ' + displayInInvoice + '</td>'
						+ '<td id="operaBranchStatFile' + i + '"></td><td><b>Valid From:</b>' + result[i]['validFrom'] + '<br /><b>Valid To:</b>' +  result[i]['validTo'] + '</td>'
						+ '<td>' + result[i]['alertForAction'] + '</td><td>' + result[i]['alertForInfo'] + '</td>'
						+ '<td>' + result[i]['nameAddress'] + '</td><td>' + result[i]['remarks'] + '</td></tr>';
			});
			branchStatutoryTable += '</tbody></table></div>';
			searchOperationalData.fetchBranchFileName(result, 'regDocument', 'td#operaBranchStatFile');
		} else {
			branchStatutoryTable += '<div style="text-align: center">No Data Available for the selected value!</div>';
		}
		$('#operationalDataResult').append(branchStatutoryTable);
		$('.operationalDataDashBoard td b').after('<br/>');
		$('#operationalDataResult').fadeIn(500);
	},
	createOperaOperationalReminders : function(data) {
		searchOperationalData.resetOperationalDataResult();
		var result = data.reminders;
		$('#operationalDataResult').empty();
		var remindersTable = searchOperationalData.clearOperationalDataStr;
		if (undefined !== result && result.length > 0) {
			remindersTable += '<h5 class="alert success">Operational Reminder: ' + data.branchname + '</h5>'
						+ '<div id="operaOperationalReminderDetails">'
						+ '<table class="table table-hover table-striped table-bordered operationalDataDashBoard transaction-create" id="operaOperationalReminderTable" style="margin-top: 0px;">'
						+ '<thead class="tablehead1">'
						+ '<tr><th>Requirements</th><th>Due On</th><th>Recurrence</th><th>Alert For Action</th>'
						+ '<th>Alert For Information</th><th>Remarks</th></tr></thead>'
						+ '<tbody>';
//					if (result.length > 0) {
			$(result).each(function(i) {
				remindersTable += ' <tr><td>' + result[i]['requirements'] + '</td><td>' + result[i]['dueOn'] + '</td>'
						+ '<td>' + result[i]['recurrence'] + '</td><td>' + result[i]['alertForAction'] + '</td>'
						+ '<td>' + result[i]['alertForInfo'] + '</td><td>' + result[i]['remarks'] + '</td></tr>';
			});
			remindersTable += '</tbody></table></div>';
		} else {
			remindersTable += '<div style="text-align: center">No Data Available for the selected value!</div>';
		}
		$('#operationalDataResult').append(remindersTable);
		$('.operationalDataDashBoard td b').after('<br/>');
		$('#operationalDataResult').fadeIn(500);
	},
	createOperaDepositBoxDetails : function(data) {
		searchOperationalData.resetOperationalDataResult();
		var result = data.depositBoxKeys;
		$('#operationalDataResult').empty();
		var depositBoxTable = searchOperationalData.clearOperationalDataStr;
		var pettyTransactionApprove = 'No';
		if (undefined !== result && result.length > 0) {
			depositBoxTable += '<h5 class="alert success">Branch Safe Deposit Box: ' + data.branchname + '</h5>'
						+ '<div id="operaBranchSafeDepositBoxDetails">'
						+ '<table class="table table-hover table-striped table-bordered operationalDataDashBoard transaction-create" id="operaBranchSafeDepositBoxTable" style="margin-top: 0px;">'
						+ '<thead class="tablehead1">'
						+ '<tr><th>Custodian For Safe Deposit</th><th>Custodian Phone Number/Email</th><th>Cashier Name</th><th>Cashier Phone Number/Email</th>'
						+ '<th>Cashier Knowledge Library</th><th>Petty Cash Transaction Approval Required</th><th>Cash Details</th><th>Petty Cash Details</th></tr></thead>'
						+ '<tbody>';
//						if (result.length > 0) {
			var cashierPhoneEmail = '', cashierName = '', cashierKnowLedgeLib = '', pettyCashTrans = '', cashDetails = '', pettyCashDetails = '';
			$(result).each(function(i) {
				if ('' != result[i]['cashierEmail'] && '' != result[i]['cashierName'] && '' != result[i]['cashierPhone']) {
					cashierPhoneEmail = '<b>Phone:</b> ' + result[i]['cashierPhone'] + '<br /><b>Email:</b> ' + result[i]['cashierEmail'];
					cashierName = result[i]['cashierName'];
					cashierKnowLedgeLib = result[i]['cashierKnowledge'];
					pettyCashTrans = result[i]['cashTransApprove'];
					cashDetails = '<b>Last Recorded Cash:</b>' + result[i]['cashLastRecord'] + '<br/><b>Cash Credit:</b>' + result[i]['cashCredit'] + '<br/>'
								+ '<b>Cash Debit:</b>' + result[i]['cashDebit'] + '<br/><b>Resultant Cash:</b>' + result[i]['cashResult'];
					pettyCashDetails = '<b>Total Petty Cash:</b>' + result[i]['cashTotalToPetty'] + '<br/><b>Resultant Petty Cash:</b>' + result[i]['cashPettyResult'] + '<br/>'
									+ '<b>Debitted Petty Cash:</b>' + result[i]['cashPettyDebit'];
				} else {
					cashierPhoneEmail = '';
					cashierName = '';
					cashierKnowLedgeLib = '';
					pettyCashTrans = '';
					cashDetails = '';
					pettyCashDetails = '';
				}
				depositBoxTable += ' <tr><td>' + result[i]['custodianName'] + '</td><td><b>Phone:</b>' + result[i]['custodianPhone'] + '<br /><b>Email:</b>' + result[i]['custodianEmail'] + '</td>'
						+ '<td>' + cashierName + '</td><td>' + cashierPhoneEmail + '</td>'
						+ '<td>' + cashierKnowLedgeLib + '</td><td>' + pettyCashTrans + '</td>'
						+ '<td>' + cashDetails + '</td><td>' + pettyCashDetails + '</td></tr>';
			});
			depositBoxTable += '</tbody></table></div>';
		} else {
			depositBoxTable += '<div style="text-align: center">No Data Available for the selected value!</div>';
		}
		$('#operationalDataResult').append(depositBoxTable);
		$('.operationalDataDashBoard td b').after('<br/>');
		$('#operationalDataResult').fadeIn(500);
	},
	createOperaBranchInsurance : function(data) {
		searchOperationalData.resetOperationalDataResult();
		var result = data.insurances;
		$('#operationalDataResult').empty();
		var branchInsuranceTable = searchOperationalData.clearOperationalDataStr;
		if (undefined !== result && result.length > 0) {
			branchInsuranceTable += '<h5 class="alert success">Branch Insurance: ' + data.branchname + '</h5>'
						+ '<div id="operaBranchInsuranceDetails">'
						+ '<table class="table table-hover table-striped table-bordered operationalDataDashBoard transaction-create" id="operaBranchInsuranceTable" style="margin-top: 0px;">'
						+ '<thead class="tablehead1">'
						+ '<tr><th>Policy Type</th><th>Policy Number & Insurance Company</th><th>Insurance Document</th><th>Validity</th>'
						+ '<th>Annual Premium</th><th>Alert For Action</th><th>Alert For Information</th><th>Remarks</th></tr></thead>'
						+ '<tbody>';
//					if (result.length > 0) {
			$(result).each(function(i) {
				branchInsuranceTable += ' <tr><td>' + result[i]['policyType'] + '</td><td><b>Policy Number:</b>' + result[i]['policyNumber'] + '<br /><b>Insurance Company:</b>' + result[i]['insuranceCompany'] + '</td>'
						+ '<td id="operaBranchInsurance' + i + '"></td><td><b>Valid From:</b>' + result[i]['validFrom'] + '<br /><b>Valid To:</b>' + result[i]['validTo'] + '</td>'
						+ '<td>' + result[i]['annualPremium'] + '</td><td>' + result[i]['alertForAction'] + '</td>'
						+ '<td>' + result[i]['alertForInfo'] + '</td><td>' + result[i]['remarks'] + '</td></tr>';
			});
			branchInsuranceTable += '</tbody></table></div>';
			searchOperationalData.fetchBranchFileName(result, 'insurancePolicyDoc', 'td#operaBranchInsurance');
		} else {
			branchInsuranceTable += '<div style="text-align: center">No Data Available for the selected value!</div>';
		}
		$('#operationalDataResult').append(branchInsuranceTable);
		$('.operationalDataDashBoard td b').after('<br/>');
		$('#operationalDataResult').fadeIn(500);
	},
	createOperaBankAccountDetails : function(data) {
		searchOperationalData.resetOperationalDataResult();
		var result = data.bankAccounts;
		$('#operationalDataResult').empty();
		var bankAccountsTable = searchOperationalData.clearOperationalDataStr;
		var accountType = '';
		if (undefined !== result && result.length > 0) {
			bankAccountsTable += '<h5 class="alert success">Bank Accounts: ' + data.branchname + '</h5>'
						+ '<div id="operaBankAccountDetails">'
						+ '<table class="table table-hover table-striped table-bordered operationalDataDashBoard transaction-create" id="operaBankAccountTable" style="margin-top: 0px;">'
						+ '<thead class="tablehead1">'
						+ '<tr><th>Bank Name</th><th>Account Number/Type</th><th>Routing Number & Swift Code</th><th>Bank Address & Phone Number</th>'
						+ '<th>Bank Instrument Custodian Name & Email</th><th>Authorized Signatory Name & Email</th><th>Cash Details</th><th>Balance Statement</th></tr></thead>'
						+ '<tbody>';
//						if (result.length > 0) {
			$(result).each(function(i) {
				bankAccountsTable += ' <tr><td>' + result[i]['bankName'] + '</td><td><b>Account Number:</b> ' + result[i]['accountNumber'] + '<br/><b>Account Type:</b> ' + result[i]['accountType'] + '</td>'
						+ '<td><b>Routing Number:</b> ' + result[i]['routingNumber'] + '<br /><b>Swift Code:</b> ' + result[i]['swiftCode'] + '</td>'
						+ '<td><b>Address:</b> ' + result[i]['bankAddress'] + '<br/><b>Phone Number:</b> ' + result[i]['phoneNumber'] + '</td>'
						+ '<td><b>Bank Instrument Custodian</b><br /><b>Name:</b> ' + result[i]['custodianName'] + '<br /><b>Email:</b> ' + result[i]['custodianEmail'] + '</td>'
						+ '<td><b>Authorized Signatory</b><br /><b>Name:</b> ' + result[i]['authorizedName'] + '<br /><b>Email:</b> ' + result[i]['authorizedEmail'] + '</td>'
						+ '<td><b>Amount Balance:</b> ' + result[i]['amountBalance'] + '<br/><b>Credit Amount:</b> ' + result[i]['creditAmount'] + '<br/>'
						+ '<b>Debit Amount:</b> ' + result[i]['debitAmount'] + '<br/><b>Resultant Amount:</b> ' + result[i]['resultantAmount'] + '</td>'
						+ '<td id="operaBankAccountBalanceStatement' + i + '"></td></tr>';
			});
			bankAccountsTable += '</tbody></table></div>';
			searchOperationalData.fetchBranchFileName(result, 'balanceStatement', 'td#operaBankAccountBalanceStatement');
		} else {
			bankAccountsTable += '<div style="text-align: center">No Data Available for the selected value!</div>';
		}
		$('#operationalDataResult').append(bankAccountsTable);
		$('.operationalDataDashBoard td b').after('<br/>');
		$('#operationalDataResult').fadeIn(500);
	},
	createChartOfAccounts : function(data) {
		var result = data.result;
		searchOperationalData.resetOperationalDataResult();
		$('#operationalDataResult').empty();
		var chartOfAccountsTable = searchOperationalData.clearOperationalDataStr;
		if (undefined !== result && result.length > 0) {
			var account = '', subAccount = '', branches = '', transPurpose = '', knowledgeLib = '', checkedManYes = '', checkedManNo = '', vendors = '';
			chartOfAccountsTable += '<h5 class="alert success">Chart Of Accounts: ' + result[0].parent + '</h5>'
						+ '<div id="operaChartOfAccountDetails">'
						+ '<table class="table table-hover table-striped table-bordered operationalDataDashBoard transaction-create" id="operaChartOfAccountTable" style="margin-top: 0px;">'
						+ '<thead class="tablehead1">';
			result = result[0];
			if ('' === result.parentSpecific) {
				account = result.specificName;
				subAccount = '';
			} else {
				account = result.parentSpecific;
				subAccount = result.specificName;
			}
			knowledgeLib = '<table style="margin:0; width: 100%;" class="operaInnerTable"><tr><th>Content</th><th>Mandatory</th><th>Branches</th></tr>';
			if (undefined !== result.libraries && result['libraries'].length > 0) {
				$(result.libraries).each(function(i) {
					if (0 == result.libraries[i]['libraryIsMandatInt']) {
						checkedManNo = 'checked="checked"';
						checkedManYes = '';
					} else if (1 == result.libraries[i]['libraryIsMandatInt']) {
						checkedManYes = 'checked="checked"';
						checkedManNo = '';
					} else {
						checkedManYes = '';
						checkedManNo = '';
					}
					knowledgeLib += '<tr><td><div style="width: 181px;">' + result.libraries[i]['libraryGuidance'] + '</div></td>'
								+ '<td><div><input type="radio" name="operaMandatoryKL_' + i + '" disabled="disabled" style="cursor: default;" ' + checkedManYes + ' />Yes'
								+ '<input type="radio" name="operaMandatoryKL_' + i + '" disabled="disabled" style="cursor: default;" ' + checkedManNo + ' />No</div></td>'
								+ '<td><div>';
					if (undefined !== result.libraries[i]['libraryBranches'] && result.libraries[i]['libraryBranches'].length > 0) {
						knowledgeLib += '<b>Branches:</b> <select>';
						$(result.libraries[i]['libraryBranches']).each(function(j) {
							knowledgeLib += '<option>' + result.libraries[i]['libraryBranches'][j] + '</option>';
						});
						knowledgeLib += '</select>';
					}
					knowledgeLib += '</div></td></tr>';
				});
			}
			knowledgeLib += '</table>';
			if (undefined !== result.branches && result['branches'].length > 0) {
				branches = '<select id="operaChartOfAccountsBranch">';
				if ('incomes' === result.parent.toLowerCase()) {
					branches += '<option>---Please Select---</option>';
//					} else if ('expenses' === result.parent.toLowerCase()) {
				} else {
					branches += '';
				}
				var tax = '';
				$(result.branches).each(function(i) {
					if ('incomes' === result.parent.toLowerCase()) {
						branches += '<optgroup label="' + result.branches[i]['branchName'] + '"></optgroup>';
						tax = result.branches[i]['branchTax'];
						if (undefined != tax) {
							$(tax).each(function(i) {
								branches += '<option>' + tax[i]['taxName'] + '-' + tax[i]['taxRate'] + '</option>';
							});
						}
					} else {
						branches += '<option>' + result.branches[i]['branchName'] + '</option>';
					}
				});
				branches += '</select>';
			} else {
				branches = '<span class="operaNoData">No branches available.</span>';
			}
			if (undefined !== result.transactionPurposes && result['transactionPurposes'].length > 0) {
				transPurpose = '<select id="operaChartOfAccountsTransactions">';
				$(result.transactionPurposes).each(function(i) {
					transPurpose += '<option>' + result.transactionPurposes[i] + '</option>';
				});
				transPurpose += '</select>';
			} else {
				transPurpose = '<span class="operaNoData">No transactions available.</span>';
			}
			if ('incomes' === result.parent.toLowerCase() || 'expenses' === result.parent.toLowerCase()) {
				if (undefined !== result.vendors && result.vendors.length > 0) {
					vendors = '<select id="operaChartOfAccountsVendors">';
					$(result.vendors).each(function(i) {
						vendors += '<option>' + result.vendors[i] + '</option>';
					});
					vendors += '</select>';
				} else {
					vendors = '<span class="operaNoData">No vendors available.</span>';
				}
			}
			if ('incomes' === result.parent.toLowerCase()) {
				chartOfAccountsTable += '<tr class="operaIncomes"><th>Particulars</th><th>Account/Sub-Account Name</th><th>Price Per Unit</th>'
								+ '<th>Branch/Applicable Taxes & Customers</th><th>Sell/Receive</th><th>Domestic/International</th><th>Knowledge Library</th></tr></thead>'
								+ '<tbody>';
				if (undefined !== result) {
					chartOfAccountsTable += '<tr class="operaIncomes"><td>' + result.parent + '</td>'
									+ '<td><b>Account:</b> ' + account + '<br/><b>Sub-Account:</b> ' + subAccount + '</td><td>' + result.pricePerUnit + '</td>'
									+ '<td><b>Branch:</b>' + branches + '<br/><b>Customers:</b>' + vendors + '</td>'
									+ '<td>' + transPurpose + '</td>' + '<td>' + result.incomeExpense + '</td><td>' + knowledgeLib + '</td></tr>';
				}
			} else if ('expenses' === result.parent.toLowerCase()) {
				chartOfAccountsTable += '<tr class="operaExpenses"><th>Particulars & Account/Sub-Account Name</th><th>Branch & Vendors</th><th>Buy/Pay</th><th>Capital/Revenue</th>'
								+ '<th>Is Withholding Applicable</th><th>Withholding Rate/Capture Input Taxes</th><th>Withholding Limits</th><th>Knowledge Library</th></tr></thead>'
								+ '<tbody>';
				if (undefined !== result) {
					subAccount += '<br/><b>Is Item an Employee Claim Item?</b> ' + result.empClaimItem;
					chartOfAccountsTable += '<tr class="operaExpenses"><td><b>Particulars:</b>' + result.parent + '<br/><b>Account:</b> ' + account + '<br/><b>Sub-Account:</b> ' + subAccount + '</td>'
									+ '<td><b>Branch:</b>' + branches + '<br/><b>Vendors:</b>' + vendors + '</td>'
									+ '<td>' + transPurpose + '</td>' + '<td>' + result.incomeExpense + '</td><td>' + result.withHoldingApplicable + '</td>'
									+ '<td><b>Withholding Rate:</b> ' + result.withHoldingRate + '<br/><b>Capture Input Taxes:</b> ' + result.captureInputTaxes + '</td>'
									+ '<td><b>Withholding Transaction Limit:</b>' + result.withHoldingTransLimit + '<br/><b>Withholding Moneytory Limit:</b>' + result.withHoldingMonetoryLimit + '</td>'
									+ '<td>' + knowledgeLib + '</td></tr>';
				}
			} else if ('assets' === result.parent.toLowerCase()) {
				chartOfAccountsTable += '<tr class="operaAssests"><th>Parent</th><th>Account/Sub-Account Name</th><th>Branch</th><th>Knowledge Library</th></tr></thead>'
									+ '<tbody>';
				if (undefined !== result) {
					subAccount += '<br/><b>Is Item an Employee Claim Item?</b> ' + result.empClaimItem;
					chartOfAccountsTable += '<tr class="operaAssests"><td>' + result.parent + '</td><td><b>Account:</b> ' + account + '<br/>'
										+ '<b>Sub-Account:</b> ' + subAccount + '</td><td>' + branches + '</td><td>' + knowledgeLib + '</td></tr>';
				}
			} else if ('liabilities' === result.parent.toLowerCase()) {
				chartOfAccountsTable += '<tr class="operaLiablities"><th>Parent</th><th>Account/Sub-Account Name</th><th>Branch</th><th>Knowledge Library</th></tr></thead>'
									+ '<tbody>';
				if (undefined !== result) {
					chartOfAccountsTable += '<tr class="operaLiablities"><td>' + result.parent + '</td><td><b>Account:</b> ' + account + '<br/>'
										+ '<b>Sub-Account:</b> ' + subAccount + '</td><td>' + branches + '</td><td>' + knowledgeLib + '</td></tr>';
				}
			}
			chartOfAccountsTable += '</tbody></table></div>';
		} else {
			chartOfAccountsTable += '<div style="text-align: center">No Data Available for the selected value!</div>';
		}
		$('#operationalDataResult').append(chartOfAccountsTable);
		$('.operationalDataDashBoard td b').after('<br/>');
		$('#operationalDataResult').fadeIn(500);
	},
	createProjects : function(data) {
		searchOperationalData.resetOperationalDataResult();
		$('#operationalDataResult').empty();
		var projectsTable = searchOperationalData.clearOperationalDataStr;
		if (undefined !== data) {
			var positions = '', branches = '';
			projectsTable += '<h5 class="alert success">Project Name: ' + data.projectName + '</h5>'
						+ '<div id="operaProjectDetails">'
						+ '<table class="table table-hover table-striped table-bordered operationalDataDashBoard transaction-create" id="operaProjectTable" style="margin-top: 0px;">'
						+ '<thead class="tablehead1">'
						+ '<tr class="operaProjects"><th>Project Name & Number</th><th>Project Start Date & End Date</th><th>Country & Location</th><th>Project Branches</th>'
						+ '<th>Project Director Name & Phone Number</th><th>Project Manager Name & Phone Number</th><th>Position Name & Validity & Location</th></tr></thead>'
						+ '<tbody>';
			if (data.projectBranches.length > 0) {
				branches = '<select>';
				$(data.projectBranches).each(function(i) {
					branches += '<option>' + data.projectBranches[i] + '</option>';
				});
				branches += '</select>';
			}
			if (undefined !== data.labourPositions && data.labourPositions.length > 0) {
				positions = '<table style="margin: 0; width: 100%;" class="operaInnerTable"><tr><th>Name</th><th>Validity</th><th>Location</th></tr>'
				$(data.labourPositions).each(function(i) {
					positions += '<tr><td>' + data.labourPositions[i].positionName + '</td>'
							+ '<td>' + data.labourPositions[i].positionValidity + '</td>'
							+ '<td>' + data.labourPositions[i].positionLocation + '</td></tr>'
				});
			}
			positions += '</table>';
			projectsTable += '<tr class="operaProjects"><td><b>Name:</b> ' + data.projectName + '<br/><b>Number:</b> ' + data.projectNumber + '</td>'
						+ '<td><b>Start Date:</b> ' + data.projectStartDate + '<br/><b>End Date:</b> ' + data.projectEndDate + '</td>'
						+ '<td><b>Country:</b> ' + data.projectCountry + '<br/><b>Location:</b> ' + data.projectLocation + '</td>'
						+ '<td>' + branches + '</td>'
						+ '<td><b>Director Name:</b> ' + data.projectDirector + '<br/><b>Phone Number:</b> ' + data.projectDirectorPhone + '</td>'
						+ '<td><b>Manager Name:</b> ' + data.projectManager + '<br/><b>Phone Number:</b> ' + data.projectManagerPhone + '</td>'
						+ '<td>' + positions + '</td>';
		} else {
			projectsTable += '<div style="text-align: center">No Data Available for the selected value!</div>';
		}
		$('#operationalDataResult').append(projectsTable);
		$('.operationalDataDashBoard td b').after('<br/>');
		$('#operationalDataResult').fadeIn(500);
	},
	createVendorOrCustomerDataTable : function(data, selected) {
		var result = data.result;
		var vendCustDetails = searchOperationalData.clearOperationalDataStr, branchArrayData = '', statData = '', lastColData = '';
		var custVend = '', purSell = '', lastCol = '', contractFetch = '', contract = '', vendorSpecifics = '', daysForCredit = '', vendorAdjustAllowed = '', optLabel = '', documentsList='';
		$('#operationalDataResult').empty();
		if (undefined != result && result.length == 1) {
			result = result[0];
			if (undefined != result || null != result) {
				if (5 == selected) {
					custVend = 'Vendor';
					optLabel = 'Purchase Item - Discount - Unit Price';
					contractFetch = 'contractPO';
					purSell = 'Purchase Item/Price';
					contract = 'Contract/PO Documents';
					lastCol = 'Validity';
					vendorAdjustAllowed = '<br /><b>Adjust Allowed?</b>' + result.vendorAdjustAllowed;
					lastColData = '<b>Valid From:</b> ' + result.validFrom + '<br /><b>Valid To:</b> ' + result.validTo;
					documentsList = result.contractPO;
				} else if (6 == selected) {
					custVend = 'Customer';
					optLabel = 'Item of Sale - Price - Unit Price';
					contractFetch = 'priceList';
					purSell = 'Item Of Sale/Discount(%)/Price';
					contract = 'Contract Documents';
					lastCol = 'Remarks';
					vendorAdjustAllowed = '';
					lastColData = result.remarks;
					documentsList = result.priceList;
				}
				if (undefined != result.vendorBranches && result.vendorBranches.length > 0) {
					branchArrayData = '<select>';
					$(result.vendorBranches).each(function(i) {
						branchArrayData += '<option>' + result.vendorBranches[i] + '</option>';
					});
					branchArrayData += '</select>';
				} else {
					branchArrayData = '<span class="operaNoData">No branch available.</span>';
				}
				for (var i = 1; i <= 4; i++) {
					if ('' != result['statName' + i]) {
						statData += result['statName' + i];
					}
					statData += ' - ';
					if ('' != result['statNumber' + i]) {
						statData += result['statNumber' + i];
					}
					statData += '<br/>';
				}
				if (undefined != result.vendorSpecifics && result.vendorSpecifics.length > 0) {
					vendorSpecifics = '<select><optgroup label="' + optLabel + '"></optgroup>';
					$(result.vendorSpecifics).each(function(i) {
						vendorSpecifics += '<option>' + result.vendorSpecifics[i] + '</option>';
					});
					vendorSpecifics += '</select>';
				} else {
					vendorSpecifics = '-';
				}
				if (0 == result.purchaseTypeNumber || 2 == result.purchaseTypeNumber) {
					daysForCredit = '<br /><b>Days For Credit:</b> ' + result.daysForCredit;
				} else {
					daysForCredit = '';
				}
				vendCustDetails += '<h5 class="alert success">' + custVend + '</h5>'
								+ '<div id="operaVendCustDetails">'
								+ '<table class="table table-hover table-striped table-bordered operationalDataDashBoard transaction-create" id="operaVendCustTable" style="margin-top: 0px;">'
								+ '<thead class="tablehead1">'
								+ '<tr class="operVendCust"><th>Name & Email</th><th>Address</th><th>Phone Number</th><th>' + custVend + ' Group/Branches</th><th>' + purSell + '</th>'
								+ '<th>Cash/Credit</th><th>' + contract + '</th><th>' + lastCol + '</th></tr></thead>'
								+ '<tbody><tr class="operVendCust">'
								+ '<td><b>Name:</b>' + result.vendorName + '<br /><b>Email:</b>' + result.vendorEmail + '</td>'
								+ '<td><b>Address:</b>' + result.vendorAddress + '<br /><b>Country:</b>' + result.vendorCountry + '<br /><b>Location:</b>' + result.vendorLocation + '</td>'
								+ '<td>' + result.vendorPhone + '</td>'
								+ '<td><b>Vendor Group:</b>' + result.vendorGroup + '<br /><b>Branches:</b>' + branchArrayData + vendorAdjustAllowed + '</td>'
								+ '<td><b>' + purSell + ':</b>' + vendorSpecifics + '<br /><b>Statutory Details</b><br />' + statData + '</td>'
								+ '<td>' + result.purchaseType + daysForCredit + '</td><td id="operaVendCustFile"><select id="operaVendCustDocuments" name="operaVendCustDocuments"><option value="">---Download---</option></select><br><button id="operaVendCustDocumentsDownloadBtn" class="fa fa-download btn btn-submit" onclick="downloadFileBlob(this);"></button></td>'
								+ '<td>' + lastColData + '</td>'
								+ '</tr></tbody></table></div>';
				$('#operationalDataResult').append(vendCustDetails);
				fillSelectElementWithUploadedDocs(documentsList, 'operaVendCustDetails', 'operaVendCustDocuments');
			} else {
				vendCustDetails += '<div style="text-align: center">No Data Available for the selected value!</div>';
				$('#operationalDataResult').append(vendCustDetails);
			}
		} else {
			vendCustDetails += '<div style="text-align: center">No Data Available for the selected value!</div>';
			$('#operationalDataResult').append(vendCustDetails);
		}
		$('.operationalDataDashBoard td b').after('<br/>');
		$('#operationalDataResult').fadeIn(500);
	},
	createUser : function(data) {
		$('#operationalDataResult').empty();
		var userTable = '', creatorTable = '', approverTable = '', travelGroup = '', expenseGroup = '';
		if (data.result) {
			var role = '', transactionPurpose = '',transactionCreatorBranch = '', transactionCreatorProject = '',
			transactionCreatorCOA = '', transactionApproverBranch = '', transactionApproverProject = '',
			transactionApproverCOA = '', travelGroupPurpose = '', expenseGroupPurpose = '';
			if (!isEmpty(data.roles) && data.roles.length > 0) {
				role = '<div>';
				for (var i = 0; i < data.roles.length; i++) {
					role += '<span>' + (i + 1) + '] '  + data.roles[i].role + '</span><br/>';
				}
				role += '</div>';
			}
			if (!isEmpty(data.transactionPurpose) && data.transactionPurpose.length > 0) {
				transactionPurpose = '<div>';
				for (var i = 0; i < data.transactionPurpose.length; i++) {
					transactionPurpose += '<span>' + (i + 1) + '] '  + data.transactionPurpose[i].transaction + '</span>';
				}
				transactionPurpose += '</div>';
			}
			userTable = searchOperationalData.clearOperationalDataStr + '<h5 class="alert success">User</h5>'
					+ '<div id="operaUserDetails">'
					+ '<table class="table table-hover table-striped table-bordered operationalDataDashBoard transaction-create" id="operaUserTable" style="margin-top: 0px; width: 1320px !important;">'
					+ '<thead class="tablehead1">'
					+ '<tr class="operUser"><th>User Name</th><th>User Email</th><th>Phone Number</th><th>Address</th><th>Role</th>'
					+ '<th>Branch</th><th>Date Of Birth & Blood Group</th><th>ID Proof</th></tr></thead>'
					+ '<tbody><tr><td>' + data.name + '</td><td>' + data.email + '</td><td>' + data.phone + '</td><td>' + data.address +'</td>'
					+ '<td><span style="color: black; font-weight: bold;">Role:</span><br/>' + role + '<br/>'
					+ '<span style="color: black; font-weight: bold;">Transaction Purpose:</span><br/>' + transactionPurpose + '</td><td>' + data.branch + '</td>'
					+ '<td><span style="color: black; font-weight: bold;">Date Of Birth:</span> ' + data.dob + '<br/>'
					+ '<span style="color: black; font-weight: bold;">Blood Group:</span> ' + data.bloodGroup + '</td><td id="orgUserIdProof0"></td>'
					+ '</tbody></table>';
			if (!isEmpty(data.transactionCreatorBranch) && data.transactionCreatorBranch.length > 0) {
				transactionCreatorBranch = '<div style="max-height: 240px; min-height: 240px; height: 240px; overflow: auto;">';
				for (var i = 0; i < data.transactionCreatorBranch.length; i++) {
					transactionCreatorBranch += '<span><span class="number">' + (i + 1) + ']</span> ' + data.transactionCreatorBranch[i].creator + '</span><br/>';
				}
				transactionCreatorBranch += '</div>';
			}
			if (!isEmpty(data.transactionCreatorProject) && data.transactionCreatorProject.length > 0) {
				transactionCreatorProject = '<div style="max-height: 240px; min-height: 240px; height: 240px; overflow: auto;">';
				for (var i = 0; i < data.transactionCreatorProject.length; i++) {
					transactionCreatorProject += '<span><span class="number">' + (i + 1) + ']</span> ' +  data.transactionCreatorProject[i].creator + '</span><br/>';
				}
				transactionCreatorProject += '</div>';
			}
			if (!isEmpty(data.transactionCreatorCOA) && data.transactionCreatorCOA.length > 0) {
				transactionCreatorCOA = '<div style="max-height: 240px; min-height: 240px; height: 240px; overflow: auto;">';
				for (var i = 0; i < data.transactionCreatorCOA.length; i++) {
					transactionCreatorCOA += '<span><span class="number">' + (i + 1) + ']</span> ' + data.transactionCreatorCOA[i].creator + '</span><br/>';
				}
				transactionCreatorCOA += '</div>';
			}
			creatorTable += '<div id="operaTransCreatorDetails" style="float:left; width: 830px;"><h5 class="alert success" style="width: 730px !important;">Transaction Creator Rules</h5>'
					+ '<table class="table table-hover table-striped table-bordered operationalDataDashBoard transaction-create" id="operaTransCreatorDetails" style="margin-top: 0px; width: 780px !important;">'
					+ '<thead class="tablehead1">'
					+ '<tr class="operTransCreator"><th style="height: 45px;">Transaction Creation For Branches</th><th style="height: 45px;">Transaction Creation For Projects</th>'
					+ '<th style="height: 45px;">Transaction Creation For Chart Of Accounts</th></tr></thead>'
					+ '<tbody><td>' + transactionCreatorBranch + '</td><td>' + transactionCreatorProject + '</td><td>' + transactionCreatorCOA + '</td>'
					+ '</tbody></table>';
			if (!isEmpty(data.transactionApproverBranch) && data.transactionApproverBranch.length > 0) {
				transactionApproverBranch = '<div style="max-height: 240px; min-height: 240px; height: 240px; overflow: auto;">';
				for (var i = 0; i < data.transactionApproverBranch.length; i++) {
					transactionApproverBranch += '<span><span class="number">' + (i + 1) + ']</span> ' + data.transactionApproverBranch[i].approver + '</span><br/>';
				}
				transactionApproverBranch += '</div>';
			}
			if (!isEmpty(data.transactionApproverProject) && data.transactionApproverProject.length > 0) {
				transactionApproverProject = '<div style="max-height: 240px; min-height: 240px; height: 240px; overflow: auto;">';
				for (var i = 0; i < data.transactionApproverProject.length; i++) {
					transactionApproverProject += '<span><span class="number">' + (i + 1) + ']</span> ' +  data.transactionApproverProject[i].approver + '</span><br/>';
				}
				transactionApproverProject += '</div>';
			}
			if (!isEmpty(data.transactionApproverCOA) && data.transactionApproverCOA.length > 0) {
				transactionApproverCOA = '<div style="max-height: 240px; min-height: 240px; height: 240px; overflow: auto;">';
				for (var i = 0; i < data.transactionApproverCOA.length; i++) {
					transactionApproverCOA += '<span><span class="number">' + (i + 1) + ']</span> ' + data.transactionApproverCOA[i].approver + '</span><br/>';
				}
				transactionApproverCOA += '</div>';
			}
			approverTable += '<div id="operaTransApproverDetails" style="float:left; width: 830px;"><h5 class="alert success" style="width: 730px !important;">Transaction Approver Rules</h5>'
					+ '<table class="table table-hover table-striped table-bordered operationalDataDashBoard transaction-create" id="operaTransApproverDetails" style="margin-top: 0px; width: 780px !important;">'
					+ '<thead class="tablehead1">'
					+ '<tr class="operTransApprover"><th style="height: 45px;">Transaction Approval For Branches</th><th style="height: 45px;">Transaction Approval For Projects</th>'
					+ '<th style="height: 45px;">Transaction Approval For Chart Of Accounts</th></tr></thead>'
					+ '<tbody><td>' + transactionApproverBranch + '</td><td>' + transactionApproverProject + '</td><td>' + transactionApproverCOA + '</td>'
					+ '</tbody></table>';
			if (!isEmpty(data.travelGroupPurpose) && data.travelGroupPurpose.length > 0) {
				travelGroupPurpose = '<div style="max-height: 240px; min-height: 240px; height: 240px; overflow: auto;">';
				for (var i = 0; i < data.travelGroupPurpose.length; i++) {
					travelGroupPurpose += '<span><span class="number">' + (i + 1) + ']</span> ' + data.travelGroupPurpose[i].purpose + '</span><br/>';
				}
				travelGroupPurpose += '</div>';
			}
			travelGroup += '<div id="operaTravelClaimDetails" style="float:left; position: relative; left: 10px;">'
					+ '<h5 class="alert success" style="width: 420px !important;">Travel Claim Rules</h5>'
					+ '<table class="table table-hover table-striped table-bordered operationalDataDashBoard transaction-create" id="operaTravelClaimDetails" style="margin-top: 0px; width: 470px !important;">'
					+ '<thead class="tablehead1">'
					+ '<tr class="operTravelClaim"><th style="height: 45px;">Travel Eligibility</th><th style="height: 45px;">Travel Transaction Purpose</th></tr></thead>'
					+ '<tbody><td>' + data.travelGroup + '</td><td>' + travelGroupPurpose + '</td>'
					+ '</tbody></table>';
			if (!isEmpty(data.expenseGroupPurpose) && data.expenseGroupPurpose.length > 0) {
				expenseGroupPurpose = '<div style="max-height: 240px; min-height: 240px; height: 240px; overflow: auto;">';
				for (var i = 0; i < data.expenseGroupPurpose.length; i++) {
					expenseGroupPurpose += '<span><span class="number">' + (i + 1) + ']</span> ' + data.expenseGroupPurpose[i].purpose + '</span><br/>';
				}
				expenseGroupPurpose += '</div>';
			}
			expenseGroup += '<div id="operaExpenseClaimDetails" style="float:left; position: relative; left: 10px;">'
					+ '<h5 class="alert success" style="width: 420px !important;">Expense Claim Rules</h5>'
					+ '<table class="table table-hover table-striped table-bordered operationalDataDashBoard transaction-create" id="operaExpenseClaimDetails" style="margin-top: 0px; width: 470px !important;">'
					+ '<thead class="tablehead1">'
					+ '<tr class="operExpenseClaim"><th style="height: 45px;">Expense Eligibility</th><th style="height: 45px;">Expense Transaction Purpose</th></tr></thead>'
					+ '<tbody><td>' + data.expenseGroup + '</td><td>' + expenseGroupPurpose + '</td>'
					+ '</tbody></table>';
		} else {
			userTable += '<div style="text-align: center">No Data Available for the selected value!</div>';
			creatorTable += '<div style="text-align: center">No Data Available for the selected value!</div>';
			travelGroup += '<div style="text-align: center">No Data Available for the selected value!</div>';
			approverTable += '<div style="text-align: center">No Data Available for the selected value!</div>';
			expenseGroup += '<div style="text-align: center">No Data Available for the selected value!</div>';
		}
		$('#operationalDataResult').append(userTable);
		$('#operationalDataResult').append(creatorTable);
		$('#operationalDataResult').append(travelGroup);
		$('#operationalDataResult').append(approverTable);
		$('#operationalDataResult').append(expenseGroup);
		$('.operationalDataDashBoard span.number').css('color', 'black');
		searchOperationalData.fetchSingleFileName(data, 'idProof', '#orgUserIdProof', 0);
		$('#operationalDataResult').fadeIn(500);
	},
	createTravelGroup : function(data) {
		$('#operationalDataResult').empty();
		var distTable = '', otherExpense = '', fixedDaily = '', klTable = '';
		if (data.result) {
			if (!isEmpty(data.travelModes) && data.travelModes.length > 0) {
				var distHead = '', namePlate = '', distBody = '';
				for (var i = 0; i < data.travelModes.length; i++) {
					namePlate = data.travelModes[i].name;
					distHead += '<th>' + namePlate +'</th>';
					namePlate += 'Array';
					if (data.travelModes[i][namePlate].length > 0) {
						distBody += '<td><select style="width: 97px;"><option>-----</option><optgroup label="Name - One Way Fare - Return Fare"></optgroup>';
						for (var j = 0; j < data.travelModes[i][namePlate].length; j++) {
							 distBody += '<option>' + data.travelModes[i][namePlate][j].name + ' - '
							 		+ data.travelModes[i][namePlate][j].oneWay + ' - '
							 		+ data.travelModes[i][namePlate][j]['return'] + '</option>';
						}
						distBody += '</select></td>';
					}
				}
				if ('' !== distHead) {
					distTable = searchOperationalData.clearOperationalDataStr + '<div id="operaDistanceDetails">'
							+ '<h5 class="alert success">Distance (Miles/Kilometers)</h5>'
							+ '<table class="table table-hover table-striped table-bordered operationalDataDashBoard transaction-create" id="operaDistanceDetails" style="margin-top: 0px; width: 1320px !important;">'
							+ '<thead class="tablehead1"><tr>' + distHead + '</tr></thead>'
							+ '<tbody><tr>' + distBody + '</tr>'
							+ '</tbody></table>';
				}
				$('#operationalDataResult').append(distTable);
			}
			if (!isEmpty(data.otherExpenses) && data.otherExpenses.length > 0) {
				var expenseBody = '', style = ' style="width: 100px;"';
				if ('' === distHead) {
					otherExpense = searchOperationalData.clearOperationalDataStr;
				}
				otherExpense += '<div id="operaOtherExpenseDetails">'
							+ '<h5 class="alert success">Maximum Daily Limit For Other Expenses (For Official Purpose)</h5>'
							+ '<table class="table table-hover table-striped table-bordered operationalDataDashBoard transaction-create" id="operaOtherExpenseDetails" style="margin-top: 0px; width: 1320px !important;">'
							+ '<thead class="tablehead1"><tr>'
							+ '<th' + style + '>Country Capital</th><th' + style + '>State Capital</th><th' + style + '>Metro City</th><th' + style + '>Other Cities</th>'
							+ '<th' + style + '>Town</th><th' + style + '>Country</th><th' + style + '>Municipality</th><th' + style + '>Village</th><th' + style + '>Remote Location</th>'
							+ '<th' + style + '>20 Miles Away From City Or Town</th><th' + style + '>Hill Station</th><th' + style + '>Resort</th><th>Places Of Conflict/War Zone</th></tr></thead>'
							+ '<tbody><tr id="expenseBody">' + expenseBody + '</tr>'
							+ '</tbody></table>';
				$('#operationalDataResult').append(otherExpense);
				for (var i = 1; i <= $('table#operaOtherExpenseDetails thead tr').children().length; i++) {
					expenseBody += '<td id="expenseBody_' + i + '">' + data.otherExpenses[0][i] + '</td>';
				}
				$('table#operaOtherExpenseDetails #expenseBody').html(expenseBody);
				if (!isEmpty(data.lodgings) && data.lodgings.length > 0) {
					var constStr = '<br/><span style="font-weight: bold; color: black;">Boarding & Lodging:</span><br/>'
								+ '<select style="width: 97px;"><option>-----</option><optgroup label="Boarding & Lodging Type - Max. Room Cost Per Night - Max. Food Cost Per Day"></optgroup>';
					for (var i = 0; i < data.lodgings.length; i++) {
						$('table#operaOtherExpenseDetails #expenseBody_' + data.lodgings[i].city).append(constStr
								+ '<option>' + data.lodgings[i].name + ' - ' + data.lodgings[i].night + ' - ' + data.lodgings[i].day + '</optiion>');
					}
				}
			}
			if (!isEmpty(data.fixedDaily) && data.fixedDaily.length > 0) {
				var fixedDailyBody = '', style = ' style="width: 100px;"';
				for (var i = 0; i < data.fixedDaily.length; i++) {
					fixedDailyBody += '<td>' + data.fixedDaily[i][i + 1] + '</td>';
				}
				if ('' === distHead && '' === otherExpense) {
					fixedDaily = searchOperationalData.clearOperationalDataStr;
				}
				fixedDaily += '<div id="operaFixedDailyDetails">'
							+ '<h5 class="alert success">Fixed Daily Per Diam</h5>'
							+ '<table class="table table-hover table-striped table-bordered operationalDataDashBoard transaction-create" id="operaFixedDailyDetails" style="margin-top: 0px; width: 1320px !important;">'
							+ '<thead class="tablehead1"><tr>'
							+ '<th' + style + '>Country Capital</th><th' + style + '>State Capital</th><th' + style + '>Metro City</th><th' + style + '>Other Cities</th>'
							+ '<th' + style + '>Town</th><th' + style + '>Country</th><th' + style + '>Municipality</th><th' + style + '>Village</th><th' + style + '>Remote Location</th>'
							+ '<th' + style + '>20 Miles Away From City Or Town</th><th' + style + '>Hill Station</th><th' + style + '>Resort</th><th>Places Of Conflict/War Zone</th></tr></thead>'
							+ '<tbody><tr>' + fixedDailyBody + '</tr>'
							+ '</tbody></table>';
				$('#operationalDataResult').append(fixedDaily);
			}
			if (!isEmpty(data.knowledgeLibrary) && data.knowledgeLibrary.length > 0) {
				var klBody = '', style = ' style="width: 99px;"';
				klTable += '<div id="operaKLDetails">'
					+ '<h5 class="alert success">Knowledge Library</h5>'
					+ '<table class="table table-hover table-striped table-bordered operationalDataDashBoard transaction-create" id="operaKLDetails" style="margin-top: 0px; width: 1320px !important;">'
					+ '<thead class="tablehead1"><tr><td' + style + '>Remarks</td><td' + style + '>Mandatory</td><td' + style + '>Remarks</td><td' + style + '>Mandatory</td>'
					+ '<td' + style + '>Remarks</td><td' + style + '>Mandatory</td><td' + style + '>Remarks</td><td' + style + '>Mandatory</td>'
					+ '<td' + style + '>Remarks</td><td' + style + '>Mandatory</td><td' + style + '>Remarks</td><td' + style + '>Mandatory</td></tr></thead>'
					+ '<tbody><tr id="klDetails"></tr>'
					+ '</tbody></table>';
				$('#operationalDataResult').append(klTable);
				for (var i = 0; i < ($('table#operaKLDetails thead tr').children().length / 2); i++) {
					if (!isEmpty(data.knowledgeLibrary[i])) {
						klBody += '<td>' + data.knowledgeLibrary[i].content + '</td><td>' + data.knowledgeLibrary[i].klMandatory + '</td>';
					} else {
						klBody += '<td></td><td></td>';
					}
				}
				$('table#operaKLDetails #klDetails').html(klBody);
			}
		} else {
			distTable += '<div style="text-align: center">No Data Available for the selected value!</div>';
			otherExpense += '<div style="text-align: center">No Data Available for the selected value!</div>';
			fixedDaily += '<div style="text-align: center">No Data Available for the selected value!</div>';
			klTable += '<div style="text-align: center">No Data Available for the selected value!</div>';
			$('#operationalDataResult').append(distTable);
			$('#operationalDataResult').append(otherExpense);
			$('#operationalDataResult').append(fixedDaily);
			$('#operationalDataResult').append(klTable);
		}
		$('#operationalDataResult').fadeIn(500);
	},
	createExpenseGroup : function(data) {
		$('#operationalDataResult').empty();
		var expenseGroup = '';
		if (data.result) {
			var claims = '';
			if (!isEmpty(data.claims) && data.claims.length > 0) {
				claims = '<div style="max-height: 240px; min-height: 240px; height: 240px; overflow: auto;">'
					+ '<span class="number"></span><span style="color: black; font-weight: bold; width: 250px; margin-left: 18px; text-align: center; display: inline-block;">Item</span>'
					+ '<span style="color: black; font-weight: bold; width: 250px; text-align: center; display: inline-block;">Max. Permitted Advance</span>'
					+ '<span style="color: black; font-weight: bold; width: 400px; text-align: center; display: inline-block;">Monthly Monetory Limit For Reimbursement</span><br/>';
				for (var i = 0; i < data.claims.length; i++) {
					claims += '<span class="number">' + (i + 1) + '] </span><span style="width: 250px; text-align: center; display: inline-block;">' + data.claims[i].name + '</span>'
						+ '<span style="width: 250px; text-align: center; display: inline-block;">' + data.claims[i].advance + '</span>'
						+ '<span style="width: 400px; text-align: center; display: inline-block;">' + data.claims[i].limit + '</span><br/>';
				}
				claims += '</div>';
			}
			expenseGroup = searchOperationalData.clearOperationalDataStr + '<div id="operaExpenseClaimDetails">'
					+ '<h5 class="alert success">Expense Claims</h5>'
					+ '<table class="table table-hover table-striped table-bordered operationalDataDashBoard transaction-create" id="operaExpenseClaimDetails" style="margin-top: 0px;">'
					+ '<thead class="tablehead1">'
					+ '<tr class="operExpenseClaim"><th style="width: 350px;">Group Name</th><th>Expense Claim Items Max. Permitted Advance & Monthly Monetory Limit For Reimbursement</th></tr></thead>'
					+ '<tbody><td>' + data.name + '</td><td>' + claims + '</td></tbody></table>';
		} else {
			expenseGroup += '<div style="text-align: center">No Data Available for the selected value!</div>';
		}
		$('#operationalDataResult').append(expenseGroup);
		$('.operationalDataDashBoard span.number').css('color', 'black');
		$('#operationalDataResult').fadeIn(500);
	}
};

function checkStockTransferCheck(elem){
	var inventoryItemId=$("#tifbtbItemsId option:selected").val();
	var inventoryFromBranch=$("#inventoryTransferFromBranchId option:selected").val();
	var available=$(".availableStock").text();
	var inprogress=$(".stockTransferInProgress").text();
	if(inventoryItemId=="" || inventoryFromBranch=="" || available=="" || inprogress==""){
		swal("Empty field","Please select inventory item and branch from which you want to transfer item stock","error");
		return true;
	}else{
		var inputValue=$(elem).val();
		if(parseInt(inputValue)>(parseInt(available)+parseInt(inprogress))){
			swal("Error!","You cannot transfer the stock greater then available stock in branch","error");
			$(elem).val("");
			return true;
		}
	}
}

function populateInventoryItems(value,text,parentTr){
	var jsonData = {};
	jsonData.email=$("#hiddenuseremail").text();
	jsonData.txnPurposeId=value;
	jsonData.txnPurposeText=text;
	ajaxCall('/stock/getInventoryItems', jsonData, '', '', '', '', 'populateInventoryItemsSuccess', '', false);
}

function populateInventoryItemsSuccess(data){
	if(data.result){
		$("#tifbtbItemsId").children().remove();
		$("#tifbtbItemsId").append('<option value="">--Please Select--</option>');
		$("#openingBalanceInvItemNamesId").children().remove();
		$("#openingBalanceInvItemNamesId").append('<option value="">--Please Select--</option>');
		for(var i=0;i<data.inventoryItemsData.length;i++){
			$("#tifbtbItemsId").append('<option value="'+data.inventoryItemsData[i].id+'">'+data.inventoryItemsData[i].name+'</option>');
			$("#openingBalanceInvItemNamesId").append('<option value="'+data.inventoryItemsData[i].id+'">'+data.inventoryItemsData[i].name+'</option>');
		}
	}else{
		$("#tifbtbItemsId").children().remove();
		$("#tifbtbItemsId").append('<option value="">--Please Select--</option>');
	}
}

function getItemStockBranches(elem){
	var inventoryItemId=$("#tifbtbItemsId option:selected").val();
	$(".inventoryTxnBranch").children().remove();
	$(".inventoryTxnBranch").append('<option value="">--Please Select--</option>');
	$(".availableStock").text("");
	$(".stockTransferInProgress").text("");
	$(".availableToStock").text("");
	$("#resultantStockId").val("");
	$("#resultantTransferringStockId").val("");
	$("#unitToTransferId").val("");
	if(inventoryItemId!=""){
		var jsonData = {};
		jsonData.email=$("#hiddenuseremail").text();
		jsonData.invItemId=inventoryItemId;
		ajaxCall('/stock/inventoryStockTransferBranches', jsonData, '', '', '', '', 'inventoryStockTransferBranchesSuccess', '', false);
	}else{
		$(".inventoryTxnBranch").children().remove();
		$(".inventoryTxnBranch").append('<option value="">--Please Select--</option>');
		$(".availableStock").text("");
		$(".stockTransferInProgress").text("");
		$(".availableToStock").text("");
		$("#resultantStockId").val("");
		$("#resultantTransferringStockId").val("");
		$("#unitToTransferId").val("");
	}
}

function getItemOpeningStockBranches(elem){
	var inventoryItemId=$("#openingBalanceInvItemNamesId option:selected").val();
	$(".inventoryTxnBranch").children().remove();
	$(".inventoryTxnBranch").append('<option value="">--Please Select--</option>');
	if(inventoryItemId!=""){
		var jsonData = {};
		jsonData.email=$("#hiddenuseremail").text();
		jsonData.invItemId=inventoryItemId;
		jsonData.invOpeningBalance="OpeningBalance";
		ajaxCall('/stock/inventoryStockTransferBranches', jsonData, '', '', '', '', 'inventoryStockTransferBranchesSuccess', '', false);
	}else{
		$("#obfinvAvailableStockId").val("");
		$(".stockTransferInProgress").text("");
		$(".openingStockInProgress").text("");
		$(".inventoryTxnBranch").children().remove();
		$(".inventoryTxnBranch").append('<option value="">--Please Select--</option>');
	}
}

function inventoryStockTransferBranchesSuccess(data){
	if(data.result){
		$(".inventoryTxnBranch").children().remove();
		$(".inventoryTxnBranch").append('<option value="">--Please Select--</option>');
		for(var i=0;i<data.inventoryBranchesData.length;i++){
			$("#inventoryTransferFromBranchId").append('<option value="'+data.inventoryBranchesData[i].id+'">'+data.inventoryBranchesData[i].name+'</option>');
		}
	}
}

function getToBranches(elem){
	var parentTr=$(elem).parent().parent().attr('id');
	var elemId=$(elem).attr('id');
	var inventoryItemId=$("#tifbtbItemsId option:selected").val();
	var inventoryBranch=$(elem).val();
	if(inventoryItemId!="" && inventoryBranch!=""){
		var jsonData = {};
		jsonData.email=$("#hiddenuseremail").text();
		jsonData.invItemId=inventoryItemId;
		jsonData.invBranchId=inventoryBranch;
		jsonData.elementId=elemId;
		ajaxCall('/stock/inventoryToBranches', jsonData, '', '', '', '', 'inventoryToBranchSuccess', '', false);
	}else{
		if(elemId=="inventoryTransferFromBranchId"){
			$(".availableStock").text("");
			$(".stockTransferInProgress").text("");
			$(".availableToStock").text("");
			$("#resultantStockId").val("");
			$("#resultantTransferringStockId").val("");
			$("#unitToTransferId").val("");
			$("#inventoryTransferToBranchId").find('option:first').prop("selected","selected");
		}
	}
}

function inventoryToBranchSuccess(data){
	if(data.result){
		if(data.inventoryTransferToBranches!=null){
			$("#inventoryTransferToBranchId").children().remove();
			$("#inventoryTransferToBranchId").append('<option value="">--Please Select--</option>');
			for(var i=0;i<data.inventoryTransferToBranches.length;i++){
				$("#inventoryTransferToBranchId").append('<option value="'+data.inventoryTransferToBranches[i].id+'">'+data.inventoryTransferToBranches[i].name+'</option>');
			}
		}
	}
}

function getStocks(elem){
	var parentTr=$(elem).parent().parent().attr('id');
	var elemId=$(elem).attr('id');
	if(elemId=="inventoryTransferToBranchId"){
		var stockToTransfer=$("#unitToTransferId").val();
		var inventoryFromBranch=$("#inventoryTransferFromBranchId option:selected").val();
		var inventoryBranch=$(elem).val();
		if(inventoryFromBranch==""){
			swal("Empty field","Pease select  the branch from which you want to transfer stock.","error");
			return true;
		}
		if(inventoryFromBranch==inventoryBranch){
			swal("Error!","You cannot transfer stock between same branch.Please transfer to different branch","error");
			$(elem).find('option:first').prop("selected","selected");
			return true;
		}
		if(stockToTransfer==""){
			swal("Empty field","Pease input number of unit of stock to transfer","error");
			return true;
		}
	}
	var inventoryItemId="";
	if(parentTr=="tifbtbtrid"){
		inventoryItemId=$("#tifbtbItemsId option:selected").val();
	}
	if(parentTr=="obfinvtrid"){
		inventoryItemId=$("#openingBalanceInvItemNamesId option:selected").val();
	}
	var inventoryBranch=$(elem).val();
	if(inventoryItemId!="" && inventoryBranch!=""){
		var jsonData = {};
		jsonData.email=$("#hiddenuseremail").text();
		jsonData.invItemId=inventoryItemId;
		jsonData.invBranchId=inventoryBranch;
		jsonData.elementId=elemId;
		ajaxCall('/stock/inventoryStockInBranch', jsonData, '', '', '', '', 'inventoryStockInBranchSuccess', '', false);
	}else{
		if(elemId=="inventoryTransferFromBranchId"){
			$(".availableStock").text("");
			$(".stockTransferInProgress").text("");
			$(".availableToStock").text("");
			$("#resultantStockId").val("");
			$("#resultantTransferringStockId").val("");
			$("#unitToTransferId").val("");
			$("#inventoryTransferToBranchId").find('option:first').prop("selected","selected");
		}
		if(elemId=="inventoryTransferToBranchId"){
			$(".availableToStock").text("");
			$("#resultantStockId").val("");
			$("#resultantTransferringStockId").val("");
			$("#resultantTransferringStockId").val("");
		}
		if(elemId=="invOpeningTxnForBranchesId"){
			$("#obfinvAvailableStockId").val("");
			$(".stockTransferInProgress").text("");
			$(".openingStockInProgress").text("");
			$("#obfinvOpeningStockId").val("");
		}
	}
}

function checkOpeningInventoryStock(elem){
	var value=$(elem).val();
	var availableStock=$("#obfinvAvailableStockId").val();
	var stockTransferInProgress=$(".stockTransferInProgress").text();
	var openingStockInProgress=$(".openingStockInProgress").text();
	if(value!=""){
		var compValue=parseInt(value);
		var compAvailable=parseInt(availableStock);
		var stockTransferAvailable=parseInt(stockTransferInProgress);
		var stockOpeningAvailable=parseInt(openingStockInProgress);
		if(compAvailable>0 || stockTransferAvailable>0 || stockOpeningAvailable>0){
			swal("Error!","You Cannot Enter Opening Stock For The Item.","error");
			$("#obfinvOpeningStockId").val("");
			return true;
		}
	}
}

function inventoryStockInBranchSuccess(data){
	if(data.result){
		for(var i=0;i<data.inventoryItemBranchesStockData.length;i++){
			var elementId=data.inventoryItemBranchesStockData[0].elementId;
			var stockAvailable=data.inventoryItemBranchesStockData[0].stockAvailable;
			var stockTransferInProgress=data.inventoryItemBranchesStockData[0].stockTransferInProgress;
			if(elementId=="inventoryTransferFromBranchId"){
				$(".availableStock").text(stockAvailable);
				$(".stockTransferInProgress").text(stockTransferInProgress);
			}
			if(elementId=="inventoryTransferToBranchId"){
				$(".availableToStock").text(stockAvailable);
				var stockToTransfer=$("#unitToTransferId").val();
				if(stockToTransfer!=""){
					var resultantStock=parseInt(stockToTransfer)+parseInt(stockAvailable);
					var resultantStockTransferringBranch=parseInt($(".availableStock").text())-parseInt(stockToTransfer);
					$("#resultantStockId").val(resultantStock);
					$("#resultantTransferringStockId").val(resultantStockTransferringBranch);
				}
			}
			if(elementId=="invOpeningTxnForBranchesId"){
				var openingStockInProgress=data.inventoryItemBranchesStockData[0].openingStockInProgress;
				$("#obfinvAvailableStockId").val(stockAvailable);
				$(".stockTransferInProgress").text(stockTransferInProgress);
				$(".openingStockInProgress").text(openingStockInProgress);
			}
		}

	}
}

function populateallowedspecialadjustmentvendorsprojects(value,text,parentTr){
	//alert("populateallowedspecialadjustmentvendorsprojects");
	$(".klBranchSpecfTd").text("");
	$(".dynmBnchBankActList").remove();
	$("#socpnreceiptdetail").find('option:first').prop("selected","selected");
	$("#rcpfccpaymentdetail").find('option:first').prop("selected","selected");
	$("#rcafccpaymentdetail").find('option:first').prop("selected","selected");
	$("#rsaafvpaymentdetail").find('option:first').prop("selected","selected");
	$("#paymentDetails").find('option:first').prop("selected","selected");
	$(".availableSpcAdjustName").text("");
	$(".availableSpcAdjustVendorAccount").text("");
	$(".adjustmentTotalAmount").text("");
	$("#"+parentTr+" .masterListCustVend").children().remove();
	$("#"+parentTr+" select[class='masterListProject']").children().remove();
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.txnPurposeId=value;
	jsonData.txnPurposeText=text;
	var url="/specialadjustments/vendorProjects"
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
			$("#"+parentTr+" .masterListCustVend").children().remove();
			$("#"+parentTr+" .masterListCustVend").append('<option value="">----Please Select----</option>');
			$("#"+parentTr+" select[class='masterListProject']").children().remove();
			$("#"+parentTr+" select[class='masterListProject']").append('<option value="">----Please Select----</option>');
			for(var i=0;i<data.vendSpecAdjustData.length;i++){
				$("#"+parentTr+" .masterListCustVend").append('<option value="'+data.vendSpecAdjustData[i].id+'">'+data.vendSpecAdjustData[i].name+'</option>');
			}
			for(var j=0;j<data.projSpecAdjustData.length;j++){
				$("#"+parentTr+" select[class='masterListProject']").append('<option value="'+data.projSpecAdjustData[j].id+'">'+data.projSpecAdjustData[j].name+'</option>');
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

function getSpecialAdjustments(elem){
	var parentTr=$(elem).parent().parent().attr('id');
	$("#"+parentTr+" div[class='klBranchSpecfTd']").text("");
	$("#"+parentTr+" div[class='availableSpcAdjustName']").text("");
	$("#"+parentTr+" div[class='availableSpcAdjustVendorAccount']").text("");
	var vendId=$(elem).val();
	var text=$("#whatYouWantToDo").find('option:selected').text();
	var value=$("#whatYouWantToDo").find('option:selected').val();
	if(vendId!=""){
		var jsonData = {};
		jsonData.useremail=$("#hiddenuseremail").text();
		jsonData.txnVendorId=vendId;
		jsonData.txnPurposeId=value;
		jsonData.txnPurposeText=text;
		var url="/specialadjustments/amountName"
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
				if(data.amountNameData[0].kLibrary!=""){
					$("#"+parentTr+" div[class='klBranchSpecfTd']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes &nbsp;&nbsp;<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No<br/>'+data.amountNameData[0].kLibrary);
				}else{
					$("#"+parentTr+" div[class='klBranchSpecfTd']").append("");
				}
				$("#"+parentTr+" div[class='availableSpcAdjustName']").text(data.amountNameData[0].adjustmentName);
				$("#"+parentTr+" div[class='availableSpcAdjustVendorAccount']").text(data.amountNameData[0].adjustmentAmount);
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	}
}

function validateTotal(elem){
	var parentTr=$(elem).parent().parent().attr('id');
	var text=$("#whatYouWantToDo").find('option:selected').text();
	if(text=="Receive special adjustments amount from vendors"){
		$("#"+parentTr+" div[class='adjustmentTotalAmount']").text("");
		var availableAmount=parseFloat($("#"+parentTr+" div[class='availableSpcAdjustVendorAccount']").text());
		if($("#"+parentTr+" div[class='availableSpcAdjustVendorAccount']").text()!=""){
			var enteredValue=$(elem).val();
			if(enteredValue!=""){
				var totalValue=availableAmount+parseFloat(enteredValue);
				$("#"+parentTr+" div[class='adjustmentTotalAmount']").text(totalValue);
			}else{
				$("#"+parentTr+" div[class='adjustmentTotalAmount']").text("");
			}
		}else{
			$(elem).val("");
		}
	}
	if(text=="Pay special adjustments amount to vendors"){
		$("#"+parentTr+" div[class='adjustmentTotalAmount']").text("");
		var availableAmount=parseFloat($("#"+parentTr+" div[class='availableSpcAdjustVendorAccount']").text());
		if($("#"+parentTr+" div[class='availableSpcAdjustVendorAccount']").text()!=""){
			var enteredValue=$(elem).val();
			if(enteredValue!=""){
				if(parseFloat(enteredValue)>availableAmount){
					swal("Cannot pay more than the special adjustment amount in vendor account");
					$(elem).val("");
					return true;
				}else{
					var totalValue=availableAmount-parseFloat(enteredValue);
					$("#"+parentTr+" div[class='adjustmentTotalAmount']").text(totalValue);
				}
			}else{
				$("#"+parentTr+" div[class='adjustmentTotalAmount']").text("");
			}
		}else{
			$(elem).val("");
		}
	}
}



function validateReturn(elem){
	var parentTr=$(elem).parent().parent().attr('id');
	var returnValue=$(elem).val();
	var maxPossibleAmount=$("#"+parentTr+" div[class='maxReturnAmount']").html();
	var amount=maxPossibleAmount.split(':');
	var amountValue=parseFloat(amount[1]);
	if(returnValue>amountValue){
		$(elem).val("");
		swal("Error!","Return amount cannot be greater than the maximum possible return amount","error");
		return true;
	}
	if(returnValue!=""){
		var leftAmount=parseFloat(amountValue)-parseFloat(returnValue);
		$("#"+parentTr+" div[class='balanceDueReturns']").text(leftAmount);
	}
}

function getInvoiceValue(elem){
	var text=$("#whatYouWantToDo").find('option:selected').text();
	var value=$("#whatYouWantToDo").find('option:selected').val();
    var parentTr=$(elem).parent().parent('tr:first').attr('id');
    var parentOfparentTr = $(elem).closest('div').attr('id');
	var branchId=$("#"+parentTr+" select[class='txnBranches'] option:selected").val();
	var custvendid=$("#"+parentTr+" .masterList option:selected").val();
    var invoiceTxnEntityId= "";
	// var openingBalAdvId = "";
    if(value == REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE){
    	invoiceTxnEntityId=$("#rfndamntrcvdItems").find('option:selected').val();
    }else{
		invoiceTxnEntityId=$(elem).val();
    }
	// if(value == REFUND_ADVANCE_RECEIVED){
	// 	var selectedOption = $('#mkrfndInvoices option:selected');
	// 	openingBalAdvId = selectedOption.data('opening-adv-id');
	// }
	if(invoiceTxnEntityId == "" || invoiceTxnEntityId == null || typeof invoiceTxnEntityId == 'undefined'){
        if(value == CREDIT_NOTE_CUSTOMER || value == DEBIT_NOTE_CUSTOMER || value == CANCEL_INVOICE){
            resetMultiItemsTableLength(parentOfparentTr);
            resetMainTransTableFields(parentTr);
            resetMultiItemsTableFieldsData('#'+parentTr);
        }else if(value == CREDIT_NOTE_VENDOR || value == DEBIT_NOTE_VENDOR){
            resetMultiItemsTableLength(parentOfparentTr);
            resetMainTransTableFields(parentTr);
            resetMultiItemsTableFieldsData('#'+parentTr);
        }
		return false;
	}
	var selectedInvoice=$(elem).val();
	$("#"+parentTr+" input[class='invValue']").val("");
	$("#"+parentTr+" div[class='returnDetails']").html("");
	$(".balanceDueReturns").text("");

	$("#"+parentTr+" div[class='maxReturnAmount']").html("");
	if(selectedInvoice != ""){
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var jsonData = {};
		 jsonData.useremail=$("#hiddenuseremail").text();
		 jsonData.txnPurposeId=value;
		 jsonData.invTxnEntityId=invoiceTxnEntityId;
		//  jsonData.openingBalAdvId=openingBalAdvId;
		 jsonData.branchId=branchId;
		 jsonData.custvendid= custvendid;
		 var url="/transaction/invoiceData";
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
		    	var totalNet=0;
		    	var transactionTableHeadTr = "";
		    	if(value==SALES_RETURNS){
					transactionTableTr = "srtfcctrid";
				}else if (value==PURCHASE_RETURNS) {
					transactionTableTr = "prtfcvtrid";
				}else if(value == BUY_ON_CASH_PAY_RIGHT_AWAY || value == BUY_ON_CREDIT_PAY_LATER){
					transactionTableTr = parentOfparentTr;
					var optionId = data.txnItemData[0].placeOfSupply;
					$("#" + transactionTableTr +" select[class='placeOfSply txnDestGstinCls'] option[id='"+optionId+"']").attr("selected", "selected");
				}else if(value == BUY_ON_CREDIT_PAY_LATER){
					transactionTableTr = "bocapltrid";
				}else if(value == CREDIT_NOTE_CUSTOMER || value == DEBIT_NOTE_CUSTOMER){
                    transactionTableTr = "cdtdbttrid";
                    resetMultiItemsTableLength('creditDebitTxnDiv');
                    resetMainTransTableFields('cdtdbttrid');
                    resetMultiItemsTableFieldsData('#cdtdbttrid');
                    transactionTableTr = parentOfparentTr;
				}else if(value == CREDIT_NOTE_VENDOR || value == DEBIT_NOTE_VENDOR){
					resetMultiItemsTableLength('creditDebitVendTxnDiv');
					resetMainTransTableFields('cdtdbvtrid');
					resetMultiItemsTableFieldsData('#cdtdbvtrid');
					transactionTableTr = parentOfparentTr;
				}else if(value == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER){
					if(data.txnItemData.length === 0){
						swal("warning!", "No inventory mapping items found or Input tax is not found for taxes used in outward transaction", "warning");
						return false;
					}
      	            resetMultiItemsTableLength('transactionDetailsTIFBTBTable');
                    //resetMainTransTableFields('tifbtbtrid');
                    resetMultiItemsTableFieldsData('#tifbtbtrid');
                    transactionTableTr = parentOfparentTr;
                    transactionTableHeadTr = "tifbtbHeadTr";
				}else if(value == REFUND_ADVANCE_RECEIVED){
					transactionTableTr = parentOfparentTr;
					transactionTableHeadTr = "mkrfndHeadTr";
					resetMultiItemsTableLength('transactionDetailsMkrfndTable');
                    resetMainTransTableFields('mkrfndtrid');
					resetMultiItemsTableFieldsData('#mkrfndtrid');
				}else if(value == REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE){
                    transactionTableTr = parentOfparentTr;
                    transactionTableHeadTr = "rfndAmntRcvdHeadTr";
                    resetMultiItemsTableLength('transactionDetailsRfndAmntRcvdTable');
                    resetMainTransTableFields('mkrfndtrid');
                    resetMultiItemsTableFieldsData('#mkrfndtrid');
                }else if(value == CANCEL_INVOICE){
					transactionTableTr = "caninvtrid";
					resetMultiItemsTableLength('cancelInvoiceTxnDiv');
					resetMainTransTableFields('caninvtrid');
					resetMultiItemsTableFieldsData('#caninvtrid');
					transactionTableTr = parentOfparentTr;
                }
				/*if(value != BUY_ON_CASH_PAY_RIGHT_AWAY && value != BUY_ON_CREDIT_PAY_LATER && value != CREDIT_NOTE_VENDOR && value != DEBIT_NOTE_VENDOR && value != REFUND_ADVANCE_RECEIVED && value != CREDIT_NOTE_CUSTOMER && value != DEBIT_NOTE_CUSTOMER){
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr").remove();
				}*/

		    	for (var i = 0; i < data.txnItemData.length; i++){
		    		if(i > 0 ){
                        $("#" + transactionTableTr +" button[class='addnewItemForTransaction']").click();
					}
					var length=$("#" + transactionTableTr +" tbody tr").length;
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnItemTableIdHid']").val(data.txnItemData[i].id);

					var itemVale = data.txnItemData[i].itemId;
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last .txnItems").val(itemVale);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last .txnItems").change();
					initMultiItemsSelect2();
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last .txnItems").prop('disabled', true);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnPerUnitPrice']").val(data.txnItemData[i].pricePerUnit);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnPerUnitPriceHid']").val(data.txnItemData[i].pricePerUnit);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnNoOfUnit']").val(data.txnItemData[i].noOfUnits);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnNoOfUnitHid']").val(data.txnItemData[i].noOfUnits);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='originalNoOfUnitsHid']").val(data.txnItemData[i].noOfUnits);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnDiscountPercent']").val(data.txnItemData[i].discountPer);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnDiscountPercentHid']").val(data.txnItemData[i].discountPer);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnDiscountAmount']").val(data.txnItemData[i].discountAmt);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnGross']").val(data.txnItemData[i].grossAmount);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnTaxTypes']").val(data.txnItemData[i].taxDescription);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnTaxAmount']").val(data.txnItemData[i].itemTotalTax);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='itemTaxAmount']").val(data.txnItemData[i].itemTotalTax);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='invoiceValue']").val(data.txnItemData[i].invoiceValue);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='withholdingtaxcomponenetdiv']").val(data.txnItemData[i].withholdingAmount);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='customerAdvance']").val(data.txnItemData[i].availableAdvance);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='howMuchAdvance']").val(data.txnItemData[i].adjFromAdvance);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='netAmountVal']").val(data.txnItemData[i].netAmount);
					totalNet = totalNet + data.txnItemData[i].netAmount;
					if(value == CREDIT_NOTE_CUSTOMER || value == DEBIT_NOTE_CUSTOMER || value == CANCEL_INVOICE){
						setCreditDebitCustomerTaxDetail(data, value, i, transactionTableTr);
					}else if(value == CREDIT_NOTE_VENDOR || value == DEBIT_NOTE_VENDOR){
						if(data.txnDetailsData){
							if(data.txnDetailsData[0].typeOfSupplyNo == 4 || data.txnDetailsData[0].typeOfSupplyNo == 5) {

								$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnDutiesAndTaxesHid']").val(data.txnItemData[i].dutiesAndTaxes);
								$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnDutiesAndTaxes']").val(data.txnItemData[i].dutiesAndTaxes);
								if(data.txnItemData[i].rcmTaxName != "") {
									var option = '<option value="'+data.txnItemData[i].rcmTaxId+'" selected>'+data.txnItemData[i].rcmTaxName+' </option>';
									$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last .txnRcmTaxItem").append(option);
								}
								$("#multipleItemsTablecdtdbv").find(".rcmCompToshowGood").show();
							}else {

								$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnDutiesAndTaxesHid']").val("");
								$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnDutiesAndTaxes']").val("");
								$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last .txnRcmTaxItem").val("");
								$("#multipleItemsTablecdtdbv").find(".rcmCompToshowGood").hide();
							}
						}
						$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last .txnRcmTaxItem").prop('disabled', true);
						setCreditDebitVendorTaxDetail(data, value, i, transactionTableTr);
						$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='howMuchAdvance']").prop('readonly',false);
						if(data.txnItemData[i].modeOfTdsCompute == 1)  {
							$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='withholdingtaxcomponenetdiv']").prop('readonly',true);
						}else {
							$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='withholdingtaxcomponenetdiv']").prop('readonly',false);
						}
					}else if(value == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER){
						setCreditDebitCustomerTaxDetail(data, value, i, transactionTableTr);
						enableDisableTrElements($("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last"), false);
					}else if(value == REFUND_ADVANCE_RECEIVED){
						$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last .txnItems").prop('disabled', true);
						$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='advAvailForRefund']").val(data.txnItemData[i].availableAdvance);
						$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='tdsAvailForRefund']").val(data.txnItemData[i].withholdingAmount);
						// var newInput = $('<input>', {
						// 	'id': 'openingBalAdvId',
						// 	'type': 'hidden',
						// 	'value': data.txnItemData[i].openingBalAdvId
						// });
						// $("#" + transactionTableTr + " table.multipleItemsTable tbody tr:last").append(newInput);
						//setMakeRefundTaxDetail(data, value, i, transactionTableHeadTr, transactionTableTr);
					} else if(value ==REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE){
						/*$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last .txnItems").prop('disabled', true);*/
						$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='amtAvailForRefund']").val(data.txnItemData[i].netAmount);
						$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='tdsAvailForRefund']").val(data.txnItemData[i].withholdingAmount);
						setRefundTaxDetail(data, value, i, transactionTableHeadTr, transactionTableTr);

					}
				}
		    	if(data.txnDetailsData){
		    		$("#mkrfndWithWithoutTaxPara").hide();
		    		$("#" + transactionTableTr +" input[class='netAmountValTotal']").val(data.txnDetailsData[0].totalNetAmount);
		    		$("#" + transactionTableTr +" input[class='totalInvoiceValue']").val(data.txnDetailsData[0].totalInvoiceValue);
		    		if(value != TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER){
		    			if(value == CREDIT_NOTE_CUSTOMER || value == DEBIT_NOTE_CUSTOMER || value == CREDIT_NOTE_VENDOR || value == DEBIT_NOTE_VENDOR || value == CANCEL_INVOICE){
		    				$("#" + transactionTableTr +" select[class='txnTypeOfSupply']").prop('disabled', false);
			    			$("#" + transactionTableTr +" select[class='txnTypeOfSupply']").val(data.txnDetailsData[0].typeOfSupplyNo);
			    			$("#" + transactionTableTr +" select[class='txnTypeOfSupply']").prop('disabled', true);
								$('#' + transactionTableTr +" input[class='withholdingtaxcomponenetdiv'").prop('readonly', true);
		    			}else {
		    				$("#" + transactionTableTr +" select[class='txnTypeOfSupply']").val(data.txnDetailsData[0].typeOfSupplyNo);
		    			}

		    			if(data.txnDetailsData[0].typeOfSupplyNo == 3 || data.txnDetailsData[0].typeOfSupplyNo == 4 || data.txnDetailsData[0].typeOfSupplyNo == 5){
		    				$("#mkrfndWithWithoutTaxPara").show();
		    				$("#" + transactionTableTr +" p[name='withWithoutTaxPara']").show();
		    				$("#" + transactionTableTr +" select[class='txnWithWithoutTaxCls']").val(data.txnDetailsData[0].withWithoutTax);
		    			}else{
		    				$("#" + transactionTableTr +" select[class='txnWithWithoutTaxCls']").val('');
		    				$("#" + transactionTableTr +" p[name='withWithoutTaxPara']").hide();
		    			}
		    		}
		    		$("#" + transactionTableTr +" select[name='txnPlaceOfSply']").val(data.txnDetailsData[0].placeOfSupplyGstin);
		    	}else{
		    		$("#" + transactionTableTr +" input[class='netAmountValTotal']").val(Math.round(totalNet));
		    	}
		    	if(value != REFUND_ADVANCE_RECEIVED){
		    		$("#" + transactionTableTr +" div[class='netAmountDescriptionDisplay']").html(data.txnItemData[data.txnItemData.length-1].taxDescription);
		    	}
		    },
		    error: function (xhr, status, error) {
		      	if(xhr.status == 401){ doLogout();
		      	}else if(xhr.status == 500){
					swal("Error on fetching invoice/transaction detail!", "Please retry, if problem persists contact support team", "error");
				}
			},
			complete: function(data) {
				$.unblockUI();
			}
		});
	}
}



/*Now not in use for Receive advance from cutomer */
function whenNotExistingCustInsteadWalkInCust(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var masterListSelected=$("#"+parentTr+" .masterListCustVend option:selected").val();
	var txnForUnavailableCustomer=$("#rcafccWalkInCustomers").val();
	if(masterListSelected="" || txnForUnavailableCustomer==null){
		swal("Empty field","Please Choose from customer list or provide manual entry for WalkIn Customer.","error");
		$("#rcafccssubmitForAccounting").hide();
		$(elem).val("");
		return true;
	}
}

function getAdvanceDiscountForEditedTran(transactionPurpose,txnVendorCustomer,txnBranch,txnitemSpecifics,parentTr){
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.userTxnPurposeText=transactionPurpose;
	jsonData.txnVendCustId=txnVendorCustomer;
	jsonData.txnVendCustItemId=txnitemSpecifics;
	if(txnVendorCustomer!="" && txnitemSpecifics!=""){
		var url="/transaction/getAdvanceDiscount"
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
	    	 if(transactionPurpose=="Buy on cash & pay right away"){
	    		 //unit price and advance if any for the vendor
	    		 $("#"+parentTr+" input[class='customerAdvance']").val(data.vendorAdvanceUnitPriceData[0].vendAdvanceMoney);
	    	 }
	    	 if(transactionPurpose=="Buy on credit & pay later"){
	    		 //unit price and advance if any for the vendor
	    		 $("#"+parentTr+" input[class='customerAdvance']").val(data.vendorAdvanceUnitPriceData[0].vendAdvanceMoney);
	    	 }
	      },
	      error: function (xhr, status, error) {
	      	if(xhr.status == 401){ doLogout(); }
	      }
	   });
   }
}

/*
function dynmsubmitForAccounting(elem){
	//alert(">>>>>17"); //sunil
	 var parentTrId=$(elem).attr('id');
	 var parentTr=parentTrId.substring(0,6)+"trid";
	 var whatYouWantToDo=$("#whatYouWantToDo").find('option:selected').text();
	 var whatYouWantToDoVal=$("#whatYouWantToDo").find('option:selected').val();
	 var err=submitForAccounting(whatYouWantToDo,whatYouWantToDoVal,parentTr);
	 if(err==true){
		return true;
	 }
}*/

function dynmsubmitForApproval(elem){
	//alert(">>>>>18"); //sunil
	 var parentTrId=$(elem).attr('id');
	 var parentTr=parentTrId.substring(0,6)+"trid";
	 var whatYouWantToDo=$("#whatYouWantToDo").find('option:selected').text();
	 var whatYouWantToDoVal=$("#whatYouWantToDo").find('option:selected').val();
		submitForApproval(whatYouWantToDo,whatYouWantToDoVal,parentTr);
}


function expandRow(trr){
	$(trr).each(function(){
		var divH = $(this).children("td").children("div").css( "height");
		//alert(divH);
		if( divH == "20px"){
			$(this).children("td").children("div[class='rowToExpand']").css( "height", "auto" );

			//var totalLen = $(this).children().length ;
			//alert();
			//for(var cnt = 0; cnt < totalLen; cnt++)
				//$(this).children().eq(cnt).children("div[class='rowToExpand']").css( "height", "200px" );


		}else{
			//$(this).children("td").children("div[class='rowToExpand']").css( "height", "50px" );
			$(this).children("td").children("div[class='rowToExpand']").css( "height", "20px" );
		}
	});
}

var MAX_RECORDS_NAVIGATION = 40;
function resultPager(tableName, itemsPerPage) {
	this.tableName = tableName;
	this.itemsPerPage = itemsPerPage;
	this.currentPage = 1;
	this.pages = 0;
	this.inited = false;

	this.showRecords = function(from, to) {
		var rows = document.getElementById(tableName).rows;
		// i starts from 1 to skip table header row
		for (var i = 1; i < rows.length; i++) {
			if (i < from || i > to)
				rows[i].style.display = 'none';
			else
				rows[i].style.display = '';
		}
	}

	this.showPage = function(pageNumber) {
		if (! this.inited) {
			alert("not inited");
			return;
		}

		var oldPageAnchor = document.getElementById('pg'+this.currentPage);
		oldPageAnchor.className = 'pg-normal';

		this.currentPage = pageNumber;
		var newPageAnchor = document.getElementById('pg'+this.currentPage);
		newPageAnchor.className = 'pg-selected';

		var previousButtonPaging = document.getElementById("previousBtnPaging");
		var nextButtonPaging = document.getElementsByName("nextBtnPaging")[0];
		if( parseInt(pageNumber) === 1){
			//previousButtonPaging.className = "pg-selected" ;
			//nextButtonPaging.className = 'pg-normal' ;
			//nextButtonPaging.setAttribute("style", "color: blue;");
			//getElementsByName("hi")[0].

			nextButtonPaging.setAttribute("style", "font-weight: bold;  font-size:1em; vertical-align:15%; text-decoration: none;  cursor: pointer;");
			previousButtonPaging.setAttribute("style", "color: black;  font-size:1em; vertical-align:15%; font-weight: normal;");
		}
		else if( pageNumber == this.pages){
			//previousButtonPaging.className = "pg-normal";
			//nextButtonPaging.className = "pg-selected";

			previousButtonPaging.setAttribute("style", "font-weight: bold;  font-size:1em; vertical-align:15%; text-decoration: none;  cursor: pointer;");
			nextButtonPaging.setAttribute("style", "color: black;  font-size:1em; vertical-align:15%; font-weight: normal;");

		}
		else{
			//previousButtonPaging.className = "pg-normal" ;
			//nextButtonPaging.className = "pg-normal" ;
			previousButtonPaging.setAttribute("style", "font-weight: bold;  font-size:1em; vertical-align:15%; text-decoration: none;  cursor: pointer;");
			nextButtonPaging.setAttribute("style", "font-weight: bold;  font-size:1em; vertical-align:15%; text-decoration: none;  cursor: pointer;");
		}

		var from = (pageNumber - 1) * itemsPerPage + 1;
		var to = from + itemsPerPage - 1;
		this.showRecords(from, to);
	}

	this.prev = function() {
		if (this.currentPage > 1)
			this.showPage(this.currentPage - 1);
	}

	this.next = function() {
		if (this.currentPage < this.pages) {
			this.showPage(this.currentPage + 1);
		}
	}

	this.init = function() {
		var rows = document.getElementById(tableName).rows;
		var records = (rows.length - 1);
		this.pages = Math.ceil(records / itemsPerPage);
		this.inited = true;

	}

	this.showPageNav = function(pagerName, positionId) {
		if (! this.inited) {
			alert("not inited");
			return;
		}
		var element = document.getElementById(positionId);
		var pagerHtml = pagerHtml =	'<span id="previousBtnPaging" onclick="' + pagerName + '.prev();"><i class="fa fa-angle-double-left fa-2x"></i><i style="font-size:1em; vertical-align:15%;">&nbsp;Prev</i></span>&nbsp;&nbsp;';
		for (var page = 1; page <= this.pages; page++){
			pagerHtml += '<span id="pg' + page + '" class="pg-normal" onclick="' + pagerName + '.showPage(' + page + ');"><i style="font-size:1em; vertical-align:15%;">' + page + '</i></span>  &nbsp; ';
		}
		pagerHtml += '<span name="nextBtnPaging" id="nextBtnPaging" onclick="'+pagerName+'.next();"><i style="font-size:1em; vertical-align:15%;">Next&nbsp;</i><i class="fa fa-angle-double-right fa-2x"></i></span>';
		element.innerHTML = pagerHtml;
	}
}

var pagingResult ='';
function setPagingDetail(tableForPaging, perPageRecords, resultSetPageNav){
	pagingResult ='';
	$('#pagingCalimNavPosition').text("");
	$('#pagingTransactionNavPosition').text("");
	$('#pagingTxnItemsNavPosition').text("");
	$('#pagingInventoryNavPosition').text("");
	$('#pagingBomSetupNavPosition').text('');
	pagingResult = new resultPager(tableForPaging, perPageRecords);
	pagingResult.init();
	pagingResult.showPageNav('pagingResult', resultSetPageNav);
	var rows = document.getElementById(tableForPaging).rows;

	var rowCount = $('#' + tableForPaging +' >tbody >tr').length;

	//alert("rows =" + rows.length + " cnt= " + rowCount);
	if(parseInt(rowCount) > 0){
		//alert("rows =" + rows.length + " cnt= " + rowCount);
		pagingResult.showPage(1);
	}else{
		//$('#' + tableForPaging).hide();
		$('#' + resultSetPageNav).text("");
		//$('#' + tableForPaging).parent().text("Records not found.");
	}
}

function resultPagerTxn(tableName, itemsPerPage, rowCount) {
	this.tableName = tableName;
	this.itemsPerPage = itemsPerPage;
	this.currentPage = 1;
	this.pages = 0;
	this.inited = false;
	this.rowCount = rowCount;

	this.showRecords = function(from, to) {

		/*var rows = document.getElementById(tableName).rows;

		for (var i = 1; i < rowCount; i++) {
			if (i < from || i > to)
				rows[i].style.display = 'none';
			else
				rows[i].style.display = '';
		}*/
	}

	this.showPage = function(pageNumber) {
		if (!this.inited) {
			swal("Error!","not inited","error");
			return;
		}
		let isCallNeeded = true;
		if(parseInt(pageNumber) === 0){
			isCallNeeded = false;
			pageNumber = 1;
		}
		let oldPageAnchor = document.getElementById('pg'+this.currentPage);
		oldPageAnchor.className = 'pg-normal';

		this.currentPage = parseInt(pageNumber);
		let newPageAnchor = document.getElementById('pg'+this.currentPage);
		newPageAnchor.className = 'pg-selected';

		let previousButtonPaging = document.getElementById("previousBtnPaging");
		let nextButtonPaging = document.getElementsByName("nextBtnPaging")[0];
		if( parseInt(pageNumber) === 1){
			nextButtonPaging.setAttribute("style", "font-weight: bold;  font-size:1em; vertical-align:15%; text-decoration: none;  cursor: pointer;");
			previousButtonPaging.setAttribute("style", "color: black;  font-size:1em; vertical-align:15%; font-weight: normal;");
		}else if( pageNumber == this.pages){
			previousButtonPaging.setAttribute("style", "font-weight: bold;  font-size:1em; vertical-align:15%; text-decoration: none;  cursor: pointer;");
			nextButtonPaging.setAttribute("style", "color: black;  font-size:1em; vertical-align:15%; font-weight: normal;");
		}else{
			previousButtonPaging.setAttribute("style", "font-weight: bold;  font-size:1em; vertical-align:15%; text-decoration: none;  cursor: pointer;");
			nextButtonPaging.setAttribute("style", "font-weight: bold;  font-size:1em; vertical-align:15%; text-decoration: none;  cursor: pointer;");
		}

		if(parseInt(pageNumber) >= 50){
			//nextButtonPaging.className =  'pg-normal';
			//previousButtonPaging.className =  'pg-normal-hide';
			if(oldPageAnchor.id !== 'pg1')
				oldPageAnchor.className = 'pg-normal-hide';
			//newPageAnchor.className = 'pg-normal';
		}

		let from = (pageNumber - 1) * itemsPerPage + 1;
		let to = from + itemsPerPage - 1;
		this.showRecords(from, to);
		if( parseInt(pageNumber) >= 1 && isCallNeeded){
			if(CALLED_METHOD === 'txn'){
				getUserTransactions(from, to);
			} else {
				searchTransactionCriteriaBased(from, to);
			}
		}
	}

	this.prev = function() {
		if (this.currentPage > 1)
			this.showPage(this.currentPage - 1);
	}

	this.next = function() {
		if (this.currentPage < this.pages) {
			this.showPage(this.currentPage + 1);
		}
	}

	this.init = function(rowCount) {
		//var rows = document.getElementById(tableName).rows;
		var records = (rowCount);
		this.pages = Math.ceil(records / itemsPerPage);
		this.inited = true;

	}

	this.showPageNav = function(pagerName, positionId) {
		if (! this.inited) {
			swal("Error!","not inited","error");
			return;
		}
		let element = document.getElementById(positionId);
		let pagerHtml =	'<span id="previousBtnPaging" onclick="' + pagerName + '.prev();"><i class="fa fa-angle-double-left fa-2x"></i><i style="font-size:1em; vertical-align:15%;">&nbsp;Prev</i></span>&nbsp;&nbsp;';
		for (let page = 1; page <= this.pages; page++){
			if(page > 50) {
				pagerHtml += '<span id="pg' + page + '" class="pg-normal-hide" onclick="' + pagerName + '.showPage(' + page + ');"><i style="font-size:1em; vertical-align:15%;">' + page + '</i></span>';

			}else{
				pagerHtml += '<span id="pg' + page + '" class="pg-normal" onclick="' + pagerName + '.showPage(' + page + ');"><i style="font-size:1em; vertical-align:15%;">' + page + '</i></span>';
			}
		}
		pagerHtml += '<span name="nextBtnPaging" id="nextBtnPaging" onclick="'+pagerName+'.next();"><i style="font-size:1em; vertical-align:15%;">Next&nbsp;</i><i class="fa fa-angle-double-right fa-2x"></i></span>';
		//element.innerHTML = ;

		if(parseInt(this.pages) > 50) {
			let listOfPages = '<select id="listOfPages" onchange="pagingResult.showPage(parseInt(this.value));"><option value="0">select</option>';
			for (let j = PER_PAGE_TXN + 1; j <= this.pages; j++) {
				listOfPages += '<option value="' + j + '">' + j + '</option>';
			}
			listOfPages += '</select>';
			element.innerHTML = pagerHtml + '<span class="ml-2"><b>Total:</b> ' + this.rowCount + '&nbsp; goto' + listOfPages + ' </span>';
		}else{
			element.innerHTML = pagerHtml;
		}
	}
}

function setPagingDetailTxn(tableForPaging, perPageRecords, resultSetPageNav, rowCount){
	pagingResult ='';
	$('#pagingCalimNavPosition').text("");
	$('#pagingTransactionNavPosition').text("");
	$('#pagingTxnItemsNavPosition').text("");
	$('#pagingInventoryNavPosition').text("");
	$('#pagingBomSetupNavPosition').text('');
	pagingResult = new resultPagerTxn(tableForPaging, perPageRecords, rowCount);
	pagingResult.init(rowCount);
	pagingResult.showPageNav('pagingResult', resultSetPageNav);
	if(parseInt(rowCount) > 0){
		pagingResult.showPage(0);
	}else{
		$('#' + resultSetPageNav).text("");
	}
}

function specifyRow(){
	var x=document.getElementById('transactionTable').rows;
	var len = x.length;

	for(var cnt = 0; cnt < len; cnt++)
		x[cnt].style.height="10px" ;
}

function setTransactionString(){
	userTransactionListString=$("#transactionTable tbody").html();
}

function setClaimsTransactionString(){
	userClaimTransactionListString=$("#claimDetailsTable tbody").html();
}

function getFinancials(useremail){
	//alert(">>>>>26"); //sunil
	var jsonData = {};
	jsonData.usermail = useremail;
	var url="/organization/getFinancials";
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
			var sessionuser=data.sessionuserTxnData[0].sessemail;
			if(sessionuser!='null'){
				replotGraph = [];
				$("#auditorFinancialTable tr[class='auditorCashExpense'] div[id='addedCustomers']").html("");
				$("#auditorFinancialTable tr[class='auditorCashExpense'] div[id='addedCustomers']").append('<select style="width:300px;" class="addedCustomersDropdown" name="addedCustomersDropdown" id="addedCustomersDropdown"><option>Names of new customers</option></select>');
				$("#auditorFinancialTable tr[class='auditorCashExpense'] div[id='addedCustomersBranches']").html("");
				$("#auditorFinancialTable tr[class='auditorCashExpense'] div[id='addedCustomersBranches']").append('<select style="width:300px;" class="addedCustomersBranchesDropdown" name="addedCustomersBranchesDropdown" id="addedCustomersBranchesDropdown"><option>Branches where new customers were added</option></select>');
				$("#auditorFinancialTable tr[class='auditorCashExpense'] div[id='addedCustomersItems']").html("");
				$("#auditorFinancialTable tr[class='auditorCashExpense'] div[id='addedCustomersItems']").append('<select style="width:300px;" class="addedCustomersItemsDropdown" name="addedCustomersItemsDropdown" id="addedCustomersItemsDropdown"><option>Items linked to customers</option></select>');
				$("#auditorFinancialTable tr[class='auditorCreditExpense'] div[id='addedVendors']").html("");
				$("#auditorFinancialTable tr[class='auditorCreditExpense'] div[id='addedVendors']").append('<select style="width:300px;" class="addedVendorsDropdown" name="addedVendorsDropdown" id="addedVendorsDropdown"><option>Name of new & updated vendors</option></select>');
				$("#auditorFinancialTable tr[class='auditorCreditExpense'] div[id='addedVendorsBranches']").html("");
				$("#auditorFinancialTable tr[class='auditorCreditExpense'] div[id='addedVendorsBranches']").append('<select style="width:300px;" class="addedVendorsBranchesDropdown" name="addedVendorsBranchesDropdown" id="addedVendorsBranchesDropdown"><option>Branches linked with the vendors</option></select>');
				$("#auditorFinancialTable tr[class='auditorCreditExpense'] div[id='addedVendorsItems']").html("");
				$("#auditorFinancialTable tr[class='auditorCreditExpense'] div[id='addedVendorsItems']").append('<select style="width:300px;" class="addedVendorsItemsDropdown" name="addedVendorsItemsDropdown" id="addedVendorsItemsDropdown"><option>Items linked with these vendors</option></select>');
				$("#auditorFinancialTable tr[class='auditorCashIncome'] div[id='addedUsers']").html("");
				$("#auditorFinancialTable tr[class='auditorCashIncome'] div[id='addedUsers']").append('<select style="width:300px;" class="addedUsersDropdown" name="addedUsersDropdown" id="addedUsersDropdown"><option>Names of new & updated users</option></select>');
				$("#auditorFinancialTable tr[class='auditorCashIncome'] div[id='addedUsersBranches']").html("");
				$("#auditorFinancialTable tr[class='auditorCashIncome'] div[id='addedUsersBranches']").append('<select style="width:300px;" class="addedUsersBranchesDropdown" name="addedUsersBranchesDropdown" id="addedUsersBranchesDropdown"><option>Branches where users were added/updated</option></select>');
				$("#auditorFinancialTable tr[class='auditorCashIncome'] div[id='addedUsersRoles']").html("");
				$("#auditorFinancialTable tr[class='auditorCashIncome'] div[id='addedUsersRoles']").append('<select style="width:300px;" class="addedUsersRolesDropdown" name="addedUsersRolesDropdown" id="addedUsersRolesDropdown"><option>Roles added / updated for these users</option></select>');
				$("#auditorFinancialTable tr[class='auditorCashIncome'] div[id='addedUsersItems']").html("");
				$("#auditorFinancialTable tr[class='auditorCashIncome'] div[id='addedUsersItems']").append('<select style="width:300px;" class="addedUsersItemsDropdown" name="addedUsersItemsDropdown" id="addedUsersItemsDropdown"><option>Items linked to these users</option></select>');
				$("#auditorFinancialTable tr[class='auditorCreditIncome'] div[id='addedTxnPendingApproval']").html("");
				$("#auditorFinancialTable tr[class='auditorCreditIncome'] div[id='addedTxnPendingApproval']").append('<select style="width:300px;" class="addedTxnPendingApprovalDropdown" name="addedTxnPendingApprovalDropdown" id="addedTxnPendingApprovalDropdown" onchange="viewTransactionDetails(this);"><option>Transaction Pending Approval</option></select>');
				$("#auditorFinancialTable tr[class='auditorTotalPayables'] div[id='bnchWiseTxnExceedingKlNotFollowed']").html("");
				$("#auditorFinancialTable tr[class='auditorTotalPayables'] div[id='bnchWiseTxnExceedingKlNotFollowed']").append('<select style="width:300px;" class="txnExceedingBudgetKlNotFollowedDropdown" name="txnExceedingBudgetKlNotFollowedDropdown" id="txnExceedingBudgetKlNotFollowedDropdown" onchange="viewTransactionDetails(this);"><option>Transaction Exceeding Budget</option></select>');
				$("#auditorFinancialTable tr[class='auditorTotalPayables'] div[id='bnchWiseTxnExceedingKlNotFollowed']").append('<br/><br/><select style="width:300px;" class="txnExceedingBudgetKlNotFollowedGroupDropdown" name="txnExceedingBudgetKlNotFollowedGroupDropdown" id="txnExceedingBudgetKlNotFollowedGroupDropdown" onchange=""><option>Transaction Exceeded Budget For Account Heads</option></select>');
				$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(2)").html("");
				$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(3)").html("");
				$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(4)").html("");
				$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(2)").html("");
				$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(3)").html("");
				$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(4)").html("");
				$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(2)").html("");
				$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(3)").html("");
				$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(4)").html("");
				$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(2)").html("");
				$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(3)").html("");
				$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(4)").html("");
				$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] td:nth-child(2)").html("");
				$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] td:nth-child(3)").html("");
				$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] td:nth-child(4)").html("");
				$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(2)").html("");
				$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(3)").html("");
				$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(4)").html("");
				$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(2)").html("");
				$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(3)").html("");
				$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(4)").html("");
				//cashExpense
				var thisWeekCashExpense="";var previousWeekCashExpense="";var cashExpenseVariance="";
				if(data.dashBoardData[0].cashExpense!=null && data.dashBoardData[0].cashExpense!=""){
					thisWeekCashExpense=data.dashBoardData[0].cashExpense;
				}
				if(data.dashBoardData[0].previousWeekcashExpense!=null && data.dashBoardData[0].previousWeekcashExpense!=""){
					previousWeekCashExpense=data.dashBoardData[0].previousWeekcashExpense;
				}
				if(thisWeekCashExpense!="" && previousWeekCashExpense!=""){
					if(parseFloat(thisWeekCashExpense)>=parseFloat(previousWeekCashExpense)){
						cashExpenseVariance=parseFloat(thisWeekCashExpense)-parseFloat(previousWeekCashExpense);
						$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(4)").append("&#8593;"+parseFloat(cashExpenseVariance));
						$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(4)").append('<div id="auditorCashExpenseJqPieChart4" class="auditorCashExpenseJqPieChart4" STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
					if(parseFloat(previousWeekCashExpense)>=parseFloat(thisWeekCashExpense)){
						cashExpenseVariance=parseFloat(previousWeekCashExpense)-parseFloat(thisWeekCashExpense);
						$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(4)").append("&#8595;"+parseFloat(cashExpenseVariance));
						$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(4)").append('<div id="auditorCashExpenseJqPieChart4" class="auditorCashExpenseJqPieChart4" STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
				}else{
					if(thisWeekCashExpense!="" && previousWeekCashExpense==""){
						$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(4)").append("&#8593;"+parseFloat(thisWeekCashExpense));
						$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(4)").append('<div id="auditorCashExpenseJqPieChart4" class="auditorCashExpenseJqPieChart4" STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
					if(previousWeekCashExpense!="" && thisWeekCashExpense==""){
						$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(4)").append("&#8595;"+parseFloat(previousWeekCashExpense));
						$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(4)").append('<div id="auditorCashExpenseJqPieChart4" class="auditorCashExpenseJqPieChart4" STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
					if(thisWeekCashExpense=="" && previousWeekCashExpense==""){
						$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(4)").append(cashExpenseVariance);
						$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(4)").append('<div id="auditorCashExpenseJqPieChart4" class="auditorCashExpenseJqPieChart4" STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
				}
				$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(2)").append(thisWeekCashExpense);
				$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(2)").append('<div id="auditorCashExpenseJqPieChart2" class="auditorCashExpenseJqPieChart2"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
				$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(3)").append(previousWeekCashExpense);
				$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(3)").append('<div id="auditorCashExpenseJqPieChart3" class="auditorCashExpenseJqPieChart3" STYLE="height:317px; width: 290px; overflow: auto;"></div>');
				if(thisWeekCashExpense!=""){
				var s1 = data.thisWeekBranchWiseCashExpenseData;
				var plotData=[];
				var ticks=[];
				for(var i=0;i<s1.length;i++){
					var arr=JSON.stringify(s1[i]);
					var newarr=arr.substring(1, arr.length-1);
					var bnchName=newarr.split(":");
					var name=bnchName[0].substring(1, bnchName[0].length-1);
					ticks.push(name);
					plotData.push(bnchName[1]);
					$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(2)").attr('data-tick', ticks);
					$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(2)").attr('data-plot', plotData);
				}
			    var plot1 = $.jqplot('auditorCashExpenseJqPieChart2', [plotData], {
			    	seriesColors: [ "#958c12", "#c5b47f", "#EAA228", "#579575", "#839557", "#958c12",
			    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
			        seriesDefaults:{
			            renderer:$.jqplot.BarRenderer,
			            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
			            rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
			        },
			        axes: {
			            xaxis: {
			            	pad: 1.00,
			            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
			                tickOptions: {
			                	formatString: '%d',
			                	angle: -30,
			                	textColor :'#0000FF'
			                }
			            },
			            yaxis: {
			            	renderer: $.jqplot.CategoryAxisRenderer,
			                ticks: ticks,
			                tickRenderer: $.jqplot.CanvasAxisTickRenderer,
			                tickOptions: {
			                	angle: -30,
				                fontSize: '8pt',
				                textColor : '#FF0000'
			                }
			            }
			        }
			    });
			    replotGraph.push(plot1);
			    $('#auditorCashExpenseJqPieChart2').bind('jqplotDataClick',
				    function (ev, seriesIndex, pointIndex, data) {
				    	var bnchName=ticks[pointIndex];
			    	    var amountValue=data.toString().split(",");
				    	//send individual data toserver get chart of account wise breakups and fill in the data modal
			    	    dispalyAndPopulateModal(bnchName,amountValue,"thisWeekCashExpense");
				    }
				);
				}
				if(previousWeekCashExpense!=""){
			    var s2 = data.previousWeekBranchWiseCashExpenseData;
			    var plotData1=[];
				var ticks1=[];
				for(var i=0;i<s2.length;i++){
					var arr=JSON.stringify(s2[i]);
					var newarr=arr.substring(1, arr.length-1);
					var bnchName=newarr.split(":");
					var name=bnchName[0].substring(1, bnchName[0].length-1);
					ticks1.push(name);
					plotData1.push(bnchName[1]);
					$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(3)").attr('data-tick', ticks);
					$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(3)").attr('data-plot', plotData);
				}
			    var plot1 = $.jqplot('auditorCashExpenseJqPieChart3', [plotData1], {
			    	seriesColors: ["#ff5800", "#EAA228", "#579575", "#958c12",
			    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
			        seriesDefaults:{
			            renderer:$.jqplot.BarRenderer,
			            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
			            rendererOptions: {fillToZero: true, barDirection: 'horizontal',barPadding: 8,barMargin: 10,barWidth: 15}
			        },
			        axes: {
			            xaxis: {
			            	pad: 1.00,
			            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
			                tickOptions: {
			                	formatString: '%d',
			                	angle: -30,
			                	textColor :'#0000FF'
			                }
			            },
			            yaxis: {
			            	renderer: $.jqplot.CategoryAxisRenderer,
			                ticks: ticks1,
			                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
			                tickOptions: {
			                	angle: -30,
				                fontSize: '8pt',
				                textColor : '#FF0000'
				            }
			            }
			        }
			    });
			    replotGraph.push(plot1);
			    $('#auditorCashExpenseJqPieChart3').bind('jqplotDataClick',
					 function (ev, seriesIndex, pointIndex, data) {
			    	    var bnchName=ticks[pointIndex];
			    	    var amountValue=data.toString().split(",");
				    	//send individual data toserver get chart of account wise breakups and fill in the data modal
			    	    dispalyAndPopulateModal(bnchName,amountValue,"previousWeekCashExpense");
					 }
				);
				}
				if(thisWeekCashExpense!="" || previousWeekCashExpense!=""){
				var thisWeekPrevWeekVariance=$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(4)").text();
				var minAmt="";var maxAmt="";var thisWeekPrevWeekVarianceAmount="";
				if(thisWeekPrevWeekVariance!=""){
					thisWeekPrevWeekCashExpenseVarianceAmount=parseFloat(thisWeekPrevWeekVariance.substring(1, thisWeekPrevWeekVariance.length));
					minAmt=-thisWeekPrevWeekCashExpenseVarianceAmount;
					maxAmt=thisWeekPrevWeekCashExpenseVarianceAmount;
				}
			    var s3 = data.thisWeekPrevWeekVarianceBranchWiseCashExpenseData;
			    var plotData2=[];
				var ticks2=[];
				for(var i=0;i<s3.length;i++){
					var arr=JSON.stringify(s3[i]);
					var newarr=arr.substring(1, arr.length-1);
					var bnchName=newarr.split(":");
					var name=bnchName[0].substring(1, bnchName[0].length-1);
					ticks2.push(name);
					plotData2.push(bnchName[1]);
					$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(4)").attr('data-tick', ticks);
					$("#auditorFinancialTable tr[class='auditorCashExpense'] td:nth-child(4)").attr('data-plot', plotData);
				}
			    var plot1 = $.jqplot('auditorCashExpenseJqPieChart4', [plotData2], {
			    	seriesColors: ["#EAA228", "#579575", "#839557", "#958c12",
			    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
			        seriesDefaults:{
			            renderer:$.jqplot.BarRenderer,
			            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
			            rendererOptions: {fillToZero: true, barDirection: 'horizontal',barPadding: 8,barMargin: 10,barWidth: 15}
			        },
			        axes: {
			            xaxis: {
			            	pad: 1.00,
			            	min:minAmt,
			            	max: maxAmt,
			            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
			                tickOptions: {
			                	formatString: '%d',
			                	angle: -30,
			                	textColor :'#0000FF'
			                }
			            },
			            yaxis: {
			            	renderer: $.jqplot.CategoryAxisRenderer,
			                ticks: ticks2,
			                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
			                tickOptions: {
			                	angle: -30,
				                fontSize: '8pt',
				                textColor : '#FF0000'
				            }
			            }
			        }
			    });
			    replotGraph.push(plot1);
			    $('#auditorCashExpenseJqPieChart4').bind('jqplotDataClick',
					function (ev, seriesIndex, pointIndex, data) {
				    	var bnchName=ticks[pointIndex];
			    	    var amountValue=data.toString().split(",");
				    	//send individual data toserver get chart of account wise breakups and fill in the data modal
			    	    dispalyAndPopulateModal(bnchName,amountValue,"thisWeekPreviousWeekCashExpenseVarience");
					}
				);
				}
				//cashIncome
				var thisWeekCashIncome="";var previousWeekCashIncome="";var cashIncomeVariance="";
				if(data.dashBoardData[0].cashIncome!=null && data.dashBoardData[0].cashIncome!=""){
					thisWeekCashIncome=data.dashBoardData[0].cashIncome;
				}
				if(data.dashBoardData[0].previousWeekcashIncome!=null && data.dashBoardData[0].previousWeekcashIncome!=""){
					previousWeekCashIncome=data.dashBoardData[0].previousWeekcashIncome;
				}
				if(thisWeekCashIncome!="" && previousWeekCashIncome!=""){
					if(parseFloat(thisWeekCashIncome)>=parseFloat(previousWeekCashIncome)){
						cashIncomeVariance=parseFloat(thisWeekCashIncome)-parseFloat(previousWeekCashIncome);
						$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(4)").append("&#8593;"+parseFloat(cashIncomeVariance));
						$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(4)").append('<div id="auditorCashIncomeJqPieChart4" class="auditorCashIncomeJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
					if(parseFloat(previousWeekCashIncome)>=parseFloat(thisWeekCashIncome)){
						cashIncomeVariance=parseFloat(previousWeekCashIncome)-parseFloat(thisWeekCashIncome);
						$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(4)").append("&#8595;"+parseFloat(cashIncomeVariance));
						$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(4)").append('<div id="auditorCashIncomeJqPieChart4" class="auditorCashIncomeJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
				}else{
					if(thisWeekCashIncome!="" && previousWeekCashIncome==""){
						$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(4)").append("&#8593;"+parseFloat(thisWeekCashIncome));
						$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(4)").append('<div id="auditorCashIncomeJqPieChart4" class="auditorCashIncomeJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
					if(previousWeekCashIncome!="" && thisWeekCashIncome==""){
						$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(4)").append("&#8595;"+parseFloat(previousWeekCashIncome));
						$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(4)").append('<div id="auditorCashIncomeJqPieChart4" class="auditorCashIncomeJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
					if(thisWeekCashIncome=="" && previousWeekCashIncome==""){
						$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(4)").append(cashIncomeVariance);
						$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(4)").append('<div id="auditorCashIncomeJqPieChart4" class="auditorCashIncomeJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
				}
				$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(2)").append(thisWeekCashIncome);
				$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(2)").append('<div id="auditorCashIncomeJqPieChart2" class="auditorCashIncomeJqPieChart2"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
				$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(3)").append(previousWeekCashIncome);
				$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(3)").append('<div id="auditorCashIncomeJqPieChart3" class="auditorCashIncomeJqPieChart3"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
				if(thisWeekCashIncome!=""){
					var s1 = data.thisWeekBranchWiseCashIncome;
					var plotData=[];
					var ticks=[];
					for(var i=0;i<s1.length;i++){
						var arr=JSON.stringify(s1[i]);
						var newarr=arr.substring(1, arr.length-1);
						var bnchName=newarr.split(":");
						var name=bnchName[0].substring(1, bnchName[0].length-1);
						ticks.push(name);
						plotData.push(bnchName[1]);
						$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(2)").attr('data-tick', ticks);
						$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(2)").attr('data-plot', plotData);
					}
				    var plot1 = $.jqplot('auditorCashIncomeJqPieChart2', [plotData], {
				    	seriesColors: [ "#958c12", "#c5b47f", "#EAA228", "#579575", "#839557", "#958c12",
				    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
				        seriesDefaults:{
				            renderer:$.jqplot.BarRenderer,
				            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
				            rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
				        },
				        axes: {
				            xaxis: {
				            	pad: 1.00,
				            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	formatString: '%d',
				                	angle: -30,
				                	textColor :'#0000FF'
				                }
				            },
				            yaxis: {
				            	renderer: $.jqplot.CategoryAxisRenderer,
				                ticks: ticks,
				                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	angle: -30,
					                fontSize: '8pt',
					                textColor : '#FF0000'
					            }
				            }
				        }
				    });
			    	    replotGraph.push(plot1);
				    $('#auditorCashIncomeJqPieChart2').bind('jqplotDataClick',
						function (ev, seriesIndex, pointIndex, data) {
					    	var bnchName=ticks[pointIndex];
				    	    var amountValue=data.toString().split(",");
					    	//send individual data toserver get chart of account wise breakups and fill in the data modal
				    	    dispalyAndPopulateModal(bnchName,amountValue,"thisWeekCashIncome");
						}
					);
				}
				if(previousWeekCashIncome!=""){
					var s1 = data.previousWeekBranchWiseCashIncomeData;
					var plotData=[];
					var ticks=[];
					for(var i=0;i<s1.length;i++){
						var arr=JSON.stringify(s1[i]);
						var newarr=arr.substring(1, arr.length-1);
						var bnchName=newarr.split(":");
						var name=bnchName[0].substring(1, bnchName[0].length-1);
						ticks.push(name);
						plotData.push(bnchName[1]);
						$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(3)").attr('data-tick', ticks);
						$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(3)").attr('data-plot', plotData);
					}
				    var plot1 = $.jqplot('auditorCashIncomeJqPieChart3', [plotData], {
				    	seriesColors: ["#ff5800", "#EAA228", "#579575", "#958c12",
				    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
				        seriesDefaults:{
				            renderer:$.jqplot.BarRenderer,
				            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
				            rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
				        },
				        axes: {
				            xaxis: {
				            	pad: 1.00,
				            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	formatString: '%d',
				                	angle: -30,
				                	textColor :'#0000FF'
				                }
				            },
				            yaxis: {
				            	renderer: $.jqplot.CategoryAxisRenderer,
				                ticks: ticks,
				                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	angle: -30,
					                fontSize: '8pt',
					                textColor : '#FF0000'
					            }
				            }
				        }
				    });
			    	    replotGraph.push(plot1);
				    $('#auditorCashIncomeJqPieChart3').bind('jqplotDataClick',
						function (ev, seriesIndex, pointIndex, data) {
					    	var bnchName=ticks[pointIndex];
				    	    var amountValue=data.toString().split(",");
					    	//send individual data toserver get chart of account wise breakups and fill in the data modal
				    	    dispalyAndPopulateModal(bnchName,amountValue,"previousWeeKCashIncome");
						}
					);
				}
				if(thisWeekCashIncome!="" || previousWeekCashIncome!=""){
					var thisWeekPrevWeekVariance=$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(4)").text();
					var minAmt="";var maxAmt="";var thisWeekPrevWeekVarianceAmount="";
					if(thisWeekPrevWeekVariance!=""){
						thisWeekPrevWeekCashExpenseVarianceAmount=parseFloat(thisWeekPrevWeekVariance.substring(1, thisWeekPrevWeekVariance.length));
						minAmt=-thisWeekPrevWeekCashExpenseVarianceAmount;
						maxAmt=thisWeekPrevWeekCashExpenseVarianceAmount;
					}
					var s1 = data.thisWeekPrevWeekVarianceBranchWiseCashIncomeData;
					var plotData=[];
					var ticks=[];
					for(var i=0;i<s1.length;i++){
						var arr=JSON.stringify(s1[i]);
						var newarr=arr.substring(1, arr.length-1);
						var bnchName=newarr.split(":");
						var name=bnchName[0].substring(1, bnchName[0].length-1);
						ticks.push(name);
						plotData.push(bnchName[1]);
						$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(4)").attr('data-tick', ticks);
						$("#auditorFinancialTable tr[class='auditorCashIncome'] td:nth-child(4)").attr('data-plot', plotData);
					}
				    var plot1 = $.jqplot('auditorCashIncomeJqPieChart4', [plotData], {
				    	seriesColors: ["#EAA228", "#579575", "#839557", "#958c12",
				    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
				        seriesDefaults:{
				            renderer:$.jqplot.BarRenderer,
				            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
				            rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
				        },
				        axes: {
				            xaxis: {
				            	pad: 1.00,
				            	min:minAmt,
				            	max: maxAmt,
				            	numberTicks: 8,
				            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	formatString: '%d',
				                	angle: -30,
				                	textColor :'#0000FF'
				                }
				            },
				            yaxis: {
				            	renderer: $.jqplot.CategoryAxisRenderer,
				                ticks: ticks,
				                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	angle: -30,
					                fontSize: '8pt',
					                textColor : '#FF0000'
					            }
				            }
				        }
				    });
			    	    replotGraph.push(plot1);
				    $('#auditorCashIncomeJqPieChart4').bind('jqplotDataClick',
						function (ev, seriesIndex, pointIndex, data) {
					    	var bnchName=ticks[pointIndex];
				    	    var amountValue=data.toString().split(",");
					    	//send individual data toserver get chart of account wise breakups and fill in the data modal
				    	    dispalyAndPopulateModal(bnchName,amountValue,"thisWeekPreviousWeekCashIncomeVarience");
						}
					);
				}
				//creditExpense
				var thisWeekCreditExpense="";var previousWeekCreditExpense="";var creditExpenseVariance="";
				if(data.dashBoardData[0].creditExpense!=null && data.dashBoardData[0].creditExpense!=""){
					thisWeekCreditExpense=data.dashBoardData[0].creditExpense;
				}
				if(data.dashBoardData[0].previousWeekcreditExpense!=null && data.dashBoardData[0].previousWeekcreditExpense!=""){
					previousWeekCreditExpense=data.dashBoardData[0].previousWeekcreditExpense;
				}
				if(thisWeekCreditExpense!="" && previousWeekCreditExpense!=""){
					if(parseFloat(thisWeekCreditExpense)>=parseFloat(previousWeekCreditExpense)){
						creditExpenseVariance=parseFloat(thisWeekCreditExpense)-parseFloat(previousWeekCreditExpense);
						$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(4)").append("&#8593;"+parseFloat(creditExpenseVariance));
						$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(4)").append('<div id="auditorCreditExpenseJqPieChart4" class="auditorCreditExpenseJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
					if(parseFloat(previousWeekCreditExpense)>=parseFloat(thisWeekCreditExpense)){
						creditExpenseVariance=parseFloat(previousWeekCreditExpense)-parseFloat(thisWeekCreditExpense);
						$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(4)").append("&#8595;"+parseFloat(creditExpenseVariance));
						$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(4)").append('<div id="auditorCreditExpenseJqPieChart4" class="auditorCreditExpenseJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
				}else{
					if(thisWeekCreditExpense!="" && previousWeekCreditExpense==""){
						$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(4)").append("&#8593;"+parseFloat(thisWeekCreditExpense));
						$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(4)").append('<div id="auditorCreditExpenseJqPieChart4" class="auditorCreditExpenseJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
					if(previousWeekCreditExpense!="" && thisWeekCreditExpense==""){
						$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(4)").append("&#8595;"+parseFloat(previousWeekCreditExpense));
						$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(4)").append('<div id="auditorCreditExpenseJqPieChart4" class="auditorCreditExpenseJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
					if(thisWeekCreditExpense=="" && previousWeekCreditExpense==""){
						$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(4)").append(creditExpenseVariance);
						$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(4)").append('<div id="auditorCreditExpenseJqPieChart4" class="auditorCreditExpenseJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
				}
				$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(2)").append(thisWeekCreditExpense);
				$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(2)").append('<div id="auditorCreditExpenseJqPieChart2" class="auditorCreditExpenseJqPieChart2"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
				$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(3)").append(previousWeekCreditExpense);
				$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(3)").append('<div id="auditorCreditExpenseJqPieChart3" class="auditorCreditExpenseJqPieChart3"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
				if(thisWeekCreditExpense!=""){
					var s1 = data.thisWeekBranchWiseCreditExpenseData;
					var plotData=[];
					var ticks=[];
					for(var i=0;i<s1.length;i++){
						var arr=JSON.stringify(s1[i]);
						var newarr=arr.substring(1, arr.length-1);
						var bnchName=newarr.split(":");
						var name=bnchName[0].substring(1, bnchName[0].length-1);
						ticks.push(name);
						plotData.push(bnchName[1]);
						$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(2)").attr('data-tick', ticks);
						$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(2)").attr('data-plot', plotData);
					}
				    var plot1 = $.jqplot('auditorCreditExpenseJqPieChart2', [plotData], {
				    	seriesColors: [ "#958c12", "#c5b47f", "#EAA228", "#579575", "#839557", "#958c12",
				    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
				        seriesDefaults:{
				            renderer:$.jqplot.BarRenderer,
				            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
				            rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
				        },
				        axes: {
				            xaxis: {
				            	pad: 1.00,
				            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	formatString: '%d',
				                	angle: -30,
				                	textColor :'#0000FF'
				                }
				            },
				            yaxis: {
				            	renderer: $.jqplot.CategoryAxisRenderer,
				                ticks: ticks,
				                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	angle: -30,
					                fontSize: '8pt',
					                textColor : '#FF0000'
					            }
				            }
				        }
				    });
			   	    replotGraph.push(plot1);
				    $('#auditorCreditExpenseJqPieChart2').bind('jqplotDataClick',
						function (ev, seriesIndex, pointIndex, data) {
					    	var bnchName=ticks[pointIndex];
				    	    var amountValue=data.toString().split(",");
					    	//send individual data toserver get chart of account wise breakups and fill in the data modal
				    	    dispalyAndPopulateModal(bnchName,amountValue,"thisWeekCreditExpense");
						}
					);
				}
				if(previousWeekCreditExpense!=""){
					var s1 = data.previousWeekBranchWiseCreditExpenseData;
					var plotData=[];
					var ticks=[];
					for(var i=0;i<s1.length;i++){
						var arr=JSON.stringify(s1[i]);
						var newarr=arr.substring(1, arr.length-1);
						var bnchName=newarr.split(":");
						var name=bnchName[0].substring(1, bnchName[0].length-1);
						ticks.push(name);
						plotData.push(bnchName[1]);
						$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(3)").attr('data-tick', ticks);
						$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(3)").attr('data-plot', plotData);
					}
				    var plot1 = $.jqplot('auditorCreditExpenseJqPieChart3', [plotData], {
				    	seriesColors: ["#ff5800", "#EAA228", "#579575", "#958c12",
				    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
				        seriesDefaults:{
				            renderer:$.jqplot.BarRenderer,
				            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
				            rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
				        },
				        axes: {
				            xaxis: {
				            	pad: 1.00,
				            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	formatString: '%d',
				                	angle: -30,
				                	textColor :'#0000FF'
				                }
				            },
				            yaxis: {
				            	renderer: $.jqplot.CategoryAxisRenderer,
				                ticks: ticks,
				                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	angle: -30,
					                fontSize: '8pt',
					                textColor : '#FF0000'
					            }
				            }
				        }
				    });
			    	    replotGraph.push(plot1);
				    $('#auditorCreditExpenseJqPieChart3').bind('jqplotDataClick',
						function (ev, seriesIndex, pointIndex, data) {
					    	var bnchName=ticks[pointIndex];
				    	    var amountValue=data.toString().split(",");
					    	//send individual data toserver get chart of account wise breakups and fill in the data modal
				    	    dispalyAndPopulateModal(bnchName,amountValue,"previousWeekCreditExpense");
						}
					);
				}
				if(thisWeekCreditExpense!="" || previousWeekCreditExpense!=""){
					var thisWeekPrevWeekVariance=$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(4)").text();
					var minAmt="";var maxAmt="";var thisWeekPrevWeekVarianceAmount="";
					if(thisWeekPrevWeekVariance!=""){
						thisWeekPrevWeekCashExpenseVarianceAmount=parseFloat(thisWeekPrevWeekVariance.substring(1, thisWeekPrevWeekVariance.length));
						minAmt=-thisWeekPrevWeekCashExpenseVarianceAmount;
						maxAmt=thisWeekPrevWeekCashExpenseVarianceAmount;
					}
					var s1 = data.thisWeekPrevWeekVarianceBranchWiseCreditExpenseData;
					var plotData=[];
					var ticks=[];
					for(var i=0;i<s1.length;i++){
						var arr=JSON.stringify(s1[i]);
						var newarr=arr.substring(1, arr.length-1);
						var bnchName=newarr.split(":");
						var name=bnchName[0].substring(1, bnchName[0].length-1);
						ticks.push(name);
						plotData.push(bnchName[1]);
						$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(4)").attr('data-tick', ticks);
						$("#auditorFinancialTable tr[class='auditorCreditExpense'] td:nth-child(4)").attr('data-plot', plotData);
					}
				    var plot1 = $.jqplot('auditorCreditExpenseJqPieChart4', [plotData], {
				    	seriesColors: ["#EAA228", "#579575", "#839557", "#958c12",
				    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
				        seriesDefaults:{
				            renderer:$.jqplot.BarRenderer,
				            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
				            rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
				        },
				        axes: {
				            xaxis: {
				            	pad: 1.00,
				            	min:minAmt,
				            	max: maxAmt,
				            	numberTicks: 8,
				            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	formatString: '%d',
				                	angle: -30,
				                	textColor :'#0000FF'
				                }
				            },
				            yaxis: {
				            	renderer: $.jqplot.CategoryAxisRenderer,
				                ticks: ticks,
				                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	angle: -30,
					                fontSize: '8pt',
					                textColor : '#FF0000'
					            }
				            }
				        }
				    });
			    	    replotGraph.push(plot1);
				    $('#auditorCreditExpenseJqPieChart4').bind('jqplotDataClick',
						function (ev, seriesIndex, pointIndex, data) {
					    	var bnchName=ticks[pointIndex];
				    	    var amountValue=data.toString().split(",");
					    	//send individual data toserver get chart of account wise breakups and fill in the data modal
				    	    dispalyAndPopulateModal(bnchName,amountValue,"thisWeekPreviousWeekCreditExpenseVarience");
						}
					);
				}
				//creditIncome
				var thisWeekCreditIncome="";var previousWeekCreditIncome="";var creditIncomeVariance="";
				if(data.dashBoardData[0].creditIncome!=null && data.dashBoardData[0].creditIncome!=""){
					thisWeekCreditIncome=data.dashBoardData[0].creditIncome;
				}
				if(data.dashBoardData[0].previousWekcreditIncome!=null && data.dashBoardData[0].previousWekcreditIncome!=""){
					previousWeekCreditIncome=data.dashBoardData[0].previousWekcreditIncome;
				}
				if(thisWeekCreditIncome!="" && previousWeekCreditIncome!=""){
					if(parseFloat(thisWeekCreditIncome)>=parseFloat(previousWeekCreditIncome)){
						creditIncomeVariance=parseFloat(thisWeekCreditIncome)-parseFloat(previousWeekCreditIncome);
						$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(4)").append("&#8593;"+parseFloat(creditIncomeVariance));
						$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(4)").append('<div id="auditorCreditIncomeJqPieChart4" class="auditorCreditIncomeJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
					if(parseFloat(previousWeekCreditIncome)>=parseFloat(thisWeekCreditIncome)){
						creditIncomeVariance=parseFloat(previousWeekCreditIncome)-parseFloat(thisWeekCreditIncome);
						$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(4)").append("&#8595;"+parseFloat(creditIncomeVariance));
						$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(4)").append('<div id="auditorCreditIncomeJqPieChart4" class="auditorCreditIncomeJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
				}else{
					if(thisWeekCreditIncome!="" && previousWeekCreditIncome==""){
						$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(4)").append("&#8593;"+parseFloat(thisWeekCreditIncome));
						$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(4)").append('<div id="auditorCreditIncomeJqPieChart4" class="auditorCreditIncomeJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
					if(previousWeekCreditIncome!="" && thisWeekCreditIncome==""){
						$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(4)").append("&#8595;"+parseFloat(previousWeekCreditIncome));
						$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(4)").append('<div id="auditorCreditIncomeJqPieChart4" class="auditorCreditIncomeJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
					if(thisWeekCreditIncome=="" && previousWeekCreditIncome==""){
						$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(4)").append(creditIncomeVariance);
						$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(4)").append('<div id="auditorCreditIncomeJqPieChart4" class="auditorCreditIncomeJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
				}
				$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(2)").append(thisWeekCreditIncome);
				$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(2)").append('<div id="auditorCreditIncomeJqPieChart2" class="auditorCreditIncomeJqPieChart2"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
				$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(3)").append(previousWeekCreditIncome);
				$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(3)").append('<div id="auditorCreditIncomeJqPieChart3" class="auditorCreditIncomeJqPieChart3"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
				if(thisWeekCreditIncome!=""){
					var s1 = data.thisWeekBranchWiseCreditIncomeData;
					var plotData=[];
					var ticks=[];
					for(var i=0;i<s1.length;i++){
						var arr=JSON.stringify(s1[i]);
						var newarr=arr.substring(1, arr.length-1);
						var bnchName=newarr.split(":");
						var name=bnchName[0].substring(1, bnchName[0].length-1);
						ticks.push(name);
						plotData.push(bnchName[1]);
						$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(2)").attr('data-tick', ticks);
						$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(2)").attr('data-plot', plotData);
					}
				    var plot1 = $.jqplot('auditorCreditIncomeJqPieChart2', [plotData], {
				    	seriesColors: [ "#958c12", "#c5b47f", "#EAA228", "#579575", "#839557", "#958c12",
				    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
				        seriesDefaults:{
				            renderer:$.jqplot.BarRenderer,
				            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
				            rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
				        },
				        axes: {
				            xaxis: {
				            	pad: 1.00,
				            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	formatString: '%d',
				                	angle: -30,
				                	textColor :'#0000FF'
				                }
				            },
				            yaxis: {
				            	renderer: $.jqplot.CategoryAxisRenderer,
				                ticks: ticks,
				                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	angle: -30,
					                fontSize: '8pt',
					                textColor : '#FF0000'
					            }
				            }
				        }
				    });
			    	    replotGraph.push(plot1);
				    $('#auditorCreditIncomeJqPieChart2').bind('jqplotDataClick',
						function (ev, seriesIndex, pointIndex, data) {
					    	var bnchName=ticks[pointIndex];
				    	    var amountValue=data.toString().split(",");
					    	//send individual data toserver get chart of account wise breakups and fill in the data modal
				    	    dispalyAndPopulateModal(bnchName,amountValue,"thisWeekCreditIncome");
						}
					);
				}
				if(previousWeekCreditIncome!=""){
					var s1 = data.previousWeekBranchWiseCreditIncomeData;
					var plotData=[];
					var ticks=[];
					for(var i=0;i<s1.length;i++){
						var arr=JSON.stringify(s1[i]);
						var newarr=arr.substring(1, arr.length-1);
						var bnchName=newarr.split(":");
						var name=bnchName[0].substring(1, bnchName[0].length-1);
						ticks.push(name);
						plotData.push(bnchName[1]);
						$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(3)").attr('data-tick', ticks);
						$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(3)").attr('data-plot', plotData);
					}
				    var plot1 = $.jqplot('auditorCreditIncomeJqPieChart3', [plotData], {
				    	seriesColors: ["#ff5800", "#EAA228", "#579575", "#958c12",
				    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
				        seriesDefaults:{
				            renderer:$.jqplot.BarRenderer,
				            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
				            rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
				        },
				        axes: {
				            xaxis: {
				            	pad: 1.00,
				            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	formatString: '%d',
				                	angle: -30,
				                	textColor :'#0000FF'
				                }
				            },
				            yaxis: {
				            	renderer: $.jqplot.CategoryAxisRenderer,
				                ticks: ticks,
				                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	angle: -30,
					                fontSize: '8pt',
					                textColor : '#FF0000'
					            }
				            }
				        }
				    });
			    	    replotGraph.push(plot1);
				    $('#auditorCreditIncomeJqPieChart3').bind('jqplotDataClick',
						function (ev, seriesIndex, pointIndex, data) {
					    	var bnchName=ticks[pointIndex];
				    	    var amountValue=data.toString().split(",");
					    	//send individual data toserver get chart of account wise breakups and fill in the data modal
				    	    dispalyAndPopulateModal(bnchName,amountValue,"previousWeekCreditIncome");
						}
					);
				}
				if(previousWeekCreditIncome!="" || thisWeekCreditIncome!=""){
					var thisWeekPrevWeekVariance=$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(4)").text();
					var minAmt="";var maxAmt="";var thisWeekPrevWeekVarianceAmount="";
					if(thisWeekPrevWeekVariance!=""){
						thisWeekPrevWeekCashExpenseVarianceAmount=parseFloat(thisWeekPrevWeekVariance.substring(1, thisWeekPrevWeekVariance.length));
						minAmt=-thisWeekPrevWeekCashExpenseVarianceAmount;
						maxAmt=thisWeekPrevWeekCashExpenseVarianceAmount;
					}
					var s1 = data.thisWeekPrevWeekVarianceBranchWiseCreditIncomeData;
					var plotData=[];
					var ticks=[];
					for(var i=0;i<s1.length;i++){
						var arr=JSON.stringify(s1[i]);
						var newarr=arr.substring(1, arr.length-1);
						var bnchName=newarr.split(":");
						var name=bnchName[0].substring(1, bnchName[0].length-1);
						ticks.push(name);
						plotData.push(bnchName[1]);
						$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(4)").attr('data-tick', ticks);
						$("#auditorFinancialTable tr[class='auditorCreditIncome'] td:nth-child(4)").attr('data-plot', plotData);
					}
				    var plot1 = $.jqplot('auditorCreditIncomeJqPieChart4', [plotData], {
				    	seriesColors: ["#EAA228", "#579575", "#839557", "#958c12",
				    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
				        seriesDefaults:{
				            renderer:$.jqplot.BarRenderer,
				            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
				            rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
				        },
				        axes: {
				            xaxis: {
				            	pad: 1.00,
				            	min:minAmt,
				            	max: maxAmt,
				            	numberTicks: 8,
				            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	formatString: '%d',
				                	angle: -30,
				                	textColor :'#0000FF'
				                }
				            },
				            yaxis: {
				            	renderer: $.jqplot.CategoryAxisRenderer,
				                ticks: ticks,
				                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	angle: -30,
					                fontSize: '8pt',
					                textColor : '#FF0000'
					            }
				            }
				        }
				    });
			    	    replotGraph.push(plot1);
				    $('#auditorCreditIncomeJqPieChart4').bind('jqplotDataClick',
						function (ev, seriesIndex, pointIndex, data) {
					    	var bnchName=ticks[pointIndex];
				    	    var amountValue=data.toString().split(",");
					    	//send individual data toserver get chart of account wise breakups and fill in the data modal
				    	    dispalyAndPopulateModal(bnchName,amountValue,"thisWeekPreviousWeekCreditIncomeVarience");
						}
					);
				}
				//Expense Budget
				var thisWeekExpenseBudgetAvailable="";var previousWeekExpenseBudgetAvailable;expenseBudgetAvailableVariance="";
				if(data.dashBoardData[0].expBudgetAvail!=null && data.dashBoardData[0].expBudgetAvail!=""){
					thisWeekExpenseBudgetAvailable=data.dashBoardData[0].expBudgetAvail;
				}
				if(data.dashBoardData[0].previousWeekexpBudgetAvail!=null && data.dashBoardData[0].previousWeekexpBudgetAvail!=""){
					previousWeekExpenseBudgetAvailable=data.dashBoardData[0].previousWeekexpBudgetAvail;
				}
				if(thisWeekExpenseBudgetAvailable!="" && previousWeekExpenseBudgetAvailable!=""){
					if(parseFloat(previousWeekExpenseBudgetAvailable)>=parseFloat(thisWeekExpenseBudgetAvailable)){
						expenseBudgetAvailableVariance=parseFloat(previousWeekExpenseBudgetAvailable)-parseFloat(thisWeekExpenseBudgetAvailable);
						$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] td:nth-child(4)").append("&#8595;"+parseFloat(expenseBudgetAvailableVariance));
						$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] td:nth-child(4)").append('<div id="auditorExpenseBudgetJqPieChart4" class="auditorExpenseBudgetJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
				}else{
					$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] td:nth-child(4)").append(expenseBudgetAvailableVariance);
					$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] td:nth-child(4)").append('<div id="auditorExpenseBudgetJqPieChart4" class="auditorExpenseBudgetJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
				}
				$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] td:nth-child(2)").append(thisWeekExpenseBudgetAvailable);
				$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] td:nth-child(2)").append('<div id="auditorExpenseBudgetJqPieChart2" class="auditorExpenseBudgetJqPieChart2"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
				$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] td:nth-child(3)").append(previousWeekExpenseBudgetAvailable);
				$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] td:nth-child(3)").append('<div id="auditorExpenseBudgetJqPieChart3" class="auditorExpenseBudgetJqPieChart3"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
				if(thisWeekExpenseBudgetAvailable!=""){
					var s1 = data.thisWeekBranchWiseBudgetAvailableData;
					var plotData=[];
					var ticks=[];
					for(var i=0;i<s1.length;i++){
						var arr=JSON.stringify(s1[i]);
						var newarr=arr.substring(1, arr.length-1);
						var bnchName=newarr.split(":");
						var name=bnchName[0].substring(1, bnchName[0].length-1);
						ticks.push(name);
						plotData.push(bnchName[1]);
						$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] td:nth-child(2)").attr('data-tick', ticks);
						$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] td:nth-child(2)").attr('data-plot', plotData);
					}
					$.jqplot.config.enablePlugins = true;
				    var plot1 = $.jqplot('auditorExpenseBudgetJqPieChart2', [plotData], {
				    	seriesColors: [ "#958c12", "#c5b47f", "#EAA228", "#579575", "#839557", "#958c12",
				    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
				        seriesDefaults:{
				            renderer:$.jqplot.BarRenderer,
				            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
				            rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
				        },
				        axes: {
				            xaxis: {
				            	pad: 1.00,
				            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	formatString: '%d',
				                	angle: -30,
				                	textColor :'#0000FF'
				                }
				            },
				            yaxis: {
				            	renderer: $.jqplot.CategoryAxisRenderer,
				                ticks: ticks,
				                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	angle: -30,
					                fontSize: '8pt',
					                textColor : '#FF0000'
					            }
				            }
				        }
				    });
			    	    replotGraph.push(plot1);
				    $('#auditorExpenseBudgetJqPieChart2').bind('jqplotDataClick',
				    	function (ev, seriesIndex, pointIndex, data) {
					    	var bnchName=ticks[pointIndex];
				    	    var amountValue=data.toString().split(",");
					    	//send individual data toserver get chart of account wise breakups and fill in the data modal
				    	    dispalyAndPopulateModal(bnchName,amountValue,"thisWeekExpenseBudgetAllocated");
				    	}
				    );
				}
				if(previousWeekExpenseBudgetAvailable!=""){
					var s1 = data.previousWeekBranchWiseBudgetAvailableData;
					var plotData=[];
					var ticks=[];
					for(var i=0;i<s1.length;i++){
						var arr=JSON.stringify(s1[i]);
						var newarr=arr.substring(1, arr.length-1);
						var bnchName=newarr.split(":");
						var name=bnchName[0].substring(1, bnchName[0].length-1);
						ticks.push(name);
						plotData.push(bnchName[1]);
						$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] td:nth-child(3)").attr('data-tick', ticks);
						$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] td:nth-child(3)").attr('data-plot', plotData);
					}
				    var plot1 = $.jqplot('auditorExpenseBudgetJqPieChart3', [plotData], {
				    	seriesColors: [ "#ff5800", "#c5b47f", "#EAA228", "#579575", "#839557", "#958c12",
				    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
				        seriesDefaults:{
				            renderer:$.jqplot.BarRenderer,
				            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
				            rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
				        },
				        axes: {
				            xaxis: {
				            	pad: 1.00,
				            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	formatString: '%d',
				                	angle: -30,
				                	textColor :'#0000FF'
				                }
				            },
				            yaxis: {
				            	renderer: $.jqplot.CategoryAxisRenderer,
				                ticks: ticks,
				                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	angle: -30,
					                fontSize: '8pt',
					                textColor : '#FF0000'
					            }
				            }
				        }
				    });
			    	    replotGraph.push(plot1);
				    $('#auditorExpenseBudgetJqPieChart3').bind('jqplotDataClick',
						function (ev, seriesIndex, pointIndex, data) {
					    	var bnchName=ticks[pointIndex];
				    	    var amountValue=data.toString().split(",");
					    	//send individual data toserver get chart of account wise breakups and fill in the data modal
				    	    dispalyAndPopulateModal(bnchName,amountValue,"previousWeekExpenseBudgetAllocated");
						}
					);
				}
				if(thisWeekExpenseBudgetAvailable!="" || previousWeekExpenseBudgetAvailable!=""){
					var thisWeekPrevWeekVariance=$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] td:nth-child(4)").text();
					var minAmt="";var maxAmt="";var thisWeekPrevWeekVarianceAmount="";
					if(thisWeekPrevWeekVariance!=""){
						thisWeekPrevWeekCashExpenseVarianceAmount=parseFloat(thisWeekPrevWeekVariance.substring(1, thisWeekPrevWeekVariance.length));
						minAmt=-thisWeekPrevWeekCashExpenseVarianceAmount;
						maxAmt=thisWeekPrevWeekCashExpenseVarianceAmount;
					}
					var s1 = data.thisWeekPrevWeekVarianceBranchWiseBudgetAvailableData;
					var plotData=[];
					var ticks=[];
					for(var i=0;i<s1.length;i++){
						var arr=JSON.stringify(s1[i]);
						var newarr=arr.substring(1, arr.length-1);
						var bnchName=newarr.split(":");
						var name=bnchName[0].substring(1, bnchName[0].length-1);
						ticks.push(name);
						plotData.push(bnchName[1]);
						$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] td:nth-child(4)").attr('data-tick', ticks);
						$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] td:nth-child(4)").attr('data-plot', plotData);
					}
				    var plot1 = $.jqplot('auditorExpenseBudgetJqPieChart4', [plotData], {
				    	seriesColors: ["#0085cc", "#579575", "#839557", "#958c12",
				    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
				        seriesDefaults:{
				            renderer:$.jqplot.BarRenderer,
				            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
				            rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
				        },
				        axes: {
				            xaxis: {
				            	pad: 1.00,
				            	min:minAmt,
				            	max: maxAmt,
				            	numberTicks: 8,
				            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	formatString: '%d',
				                	angle: -30,
				                	textColor :'#0000FF'
				                }
				            },
				            yaxis: {
				            	renderer: $.jqplot.CategoryAxisRenderer,
				                ticks: ticks,
				                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	angle: -30,
					                fontSize: '8pt',
					                textColor : '#FF0000'
					            }
				            }
				        }
				    });
			    	    replotGraph.push(plot1);
				    $('#auditorExpenseBudgetJqPieChart4').bind('jqplotDataClick',
						function (ev, seriesIndex, pointIndex, data) {
					    	var bnchName=ticks[pointIndex];
				    	    var amountValue=data.toString().split(",");
					    	//send individual data toserver get chart of account wise breakups and fill in the data modal
				    	    dispalyAndPopulateModal(bnchName,amountValue,"thisWeekPreviousWeekExpenseBudgetVarience");
						}
					);
				}
				//totalReceivables
				var thisWeekTotalReceivables="";var previousWeekTotalReceivables="";var totalReceivablesVariance="";
				if(data.dashBoardData[0].netRecievableThisWeek!=null && data.dashBoardData[0].netRecievableThisWeek!=""){
					thisWeekTotalReceivables=data.dashBoardData[0].netRecievableThisWeek;
				}
				if(data.dashBoardData[0].netRecievablePreviousWeek!=null && data.dashBoardData[0].netRecievablePreviousWeek!=""){
					previousWeekTotalReceivables=data.dashBoardData[0].netRecievablePreviousWeek;
				}
				if(thisWeekTotalReceivables!="" && previousWeekTotalReceivables!=""){
					if(parseFloat(thisWeekTotalReceivables)>=parseFloat(previousWeekTotalReceivables)){
						totalReceivablesVariance=parseFloat(thisWeekTotalReceivables)-parseFloat(previousWeekTotalReceivables);
						$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(4)").append("&#8593;"+parseFloat(totalReceivablesVariance));
						$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(4)").append('<div id="auditorTotalReceivablesJqPieChart4" class="auditorTotalReceivablesJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
					if(parseFloat(previousWeekTotalReceivables)>=parseFloat(thisWeekTotalReceivables)){
						totalReceivablesVariance=parseFloat(previousWeekTotalReceivables)-parseFloat(thisWeekTotalReceivables);
						$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(4)").append("&#8595;"+parseFloat(totalReceivablesVariance));
						$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(4)").append('<div id="auditorTotalReceivablesJqPieChart4" class="auditorTotalReceivablesJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
				}else{
					if(thisWeekTotalReceivables!="" && previousWeekTotalReceivables==""){
						$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(4)").append("&#8593;"+parseFloat(thisWeekTotalReceivables));
						$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(4)").append('<div id="auditorTotalReceivablesJqPieChart4" class="auditorTotalReceivablesJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
					if(previousWeekTotalReceivables!="" && thisWeekTotalReceivables==""){
						$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(4)").append("&#8595;"+parseFloat(previousWeekTotalReceivables));
						$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(4)").append('<div id="auditorTotalReceivablesJqPieChart4" class="auditorTotalReceivablesJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
					if(thisWeekTotalReceivables=="" && previousWeekTotalReceivables==""){
						$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(4)").append(totalReceivablesVariance);
						$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(4)").append('<div id="auditorTotalReceivablesJqPieChart4" class="auditorTotalReceivablesJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
				}
				$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(2)").append(thisWeekTotalReceivables);
				$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(2)").append('<div id="auditorTotalReceivablesJqPieChart2" class="auditorTotalReceivablesJqPieChart2"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
				$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(3)").append(previousWeekTotalReceivables);
				$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(3)").append('<div id="auditorTotalReceivablesJqPieChart3" class="auditorTotalReceivablesJqPieChart3"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
				if(thisWeekTotalReceivables!=""){
					var s1 = data.thisWeekBranchWiseTotalReceivablesData;
					var plotData=[];
					var ticks=[];
					for(var i=0;i<s1.length;i++){
						var arr=JSON.stringify(s1[i]);
						var newarr=arr.substring(1, arr.length-1);
						var bnchName=newarr.split(":");
						var name=bnchName[0].substring(1, bnchName[0].length-1);
						ticks.push(name);
						plotData.push(parseFloat(bnchName[1]));
						$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(2)").attr('data-tick', ticks);
						$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(2)").attr('data-plot', plotData);
					}
				    var plot1 = $.jqplot('auditorTotalReceivablesJqPieChart2', [plotData], {
				    	seriesColors: [ "#958c12", "#c5b47f", "#EAA228", "#579575", "#839557", "#958c12",
				    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
				        seriesDefaults:{
				            renderer:$.jqplot.BarRenderer,
				            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
				            rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
				        },
				        axes: {
				            xaxis: {
				            	pad: 1.00,
				            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	formatString: '%d',
				                	angle: -30,
				                	textColor :'#0000FF'
				                }
				            },
				            yaxis: {
				            	renderer: $.jqplot.CategoryAxisRenderer,
				                ticks: ticks,
				                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	angle: -30,
					                fontSize: '8pt',
					                textColor : '#FF0000'
					            }
				            }
				        }
				    });
			    	    replotGraph.push(plot1);
				    $('#auditorTotalReceivablesJqPieChart2').bind('jqplotDataClick',
						function (ev, seriesIndex, pointIndex, data) {
					    	var bnchName=ticks[pointIndex];
				    	    var amountValue=data.toString().split(",");
					    	//send individual data toserver get chart of account wise breakups and fill in the data modal
				    	    dispalyAndPopulateModal(bnchName,amountValue,"thisWeekTotalReceivables");
						}
					);
				}
				if(previousWeekTotalReceivables!=""){
					var s1 = data.previousWeekBranchWiseTotalReceivablesData;
					var plotData=[];
					var ticks=[];
					for(var i=0;i<s1.length;i++){
						var arr=JSON.stringify(s1[i]);
						var newarr=arr.substring(1, arr.length-1);
						var bnchName=newarr.split(":");
						var name=bnchName[0].substring(1, bnchName[0].length-1);
						ticks.push(name);
						plotData.push(parseFloat(bnchName[1]));
						$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(3)").attr('data-tick', ticks);
						$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(3)").attr('data-plot', plotData);
					}
				    var plot1 = $.jqplot('auditorTotalReceivablesJqPieChart3', [plotData], {
				    	seriesColors: [ "#EAA228", "#c5b47f", "#EAA228", "#579575", "#839557", "#958c12",
				    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
				        seriesDefaults:{
				            renderer:$.jqplot.BarRenderer,
				            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
				            rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
				        },
				        axes: {
				            xaxis: {
				            	pad: 1.00,
				            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	formatString: '%d',
				                	angle: -30,
				                	textColor :'#0000FF'
				                }
				            },
				            yaxis: {
				            	renderer: $.jqplot.CategoryAxisRenderer,
				                ticks: ticks,
				                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	angle: -30,
					                fontSize: '8pt',
					                textColor : '#FF0000'
					            }
				            }
				        }
				    });
			    	    replotGraph.push(plot1);
				    $('#auditorTotalReceivablesJqPieChart3').bind('jqplotDataClick',
						function (ev, seriesIndex, pointIndex, data) {
					    	var bnchName=ticks[pointIndex];
				    	    var amountValue=data.toString().split(",");
					    	//send individual data toserver get chart of account wise breakups and fill in the data modal
				    	    dispalyAndPopulateModal(bnchName,amountValue,"previousWeekTotalReceivables");
						}
					);
				}
				if(thisWeekTotalReceivables!="" || previousWeekTotalReceivables!=""){
					var thisWeekPrevWeekVariance=$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(4)").text();
					var minAmt="";var maxAmt="";var thisWeekPrevWeekVarianceAmount="";
					if(thisWeekPrevWeekVariance!=""){
						thisWeekPrevWeekCashExpenseVarianceAmount=parseFloat(thisWeekPrevWeekVariance.substring(1, thisWeekPrevWeekVariance.length));
						minAmt=-thisWeekPrevWeekCashExpenseVarianceAmount;
						maxAmt=thisWeekPrevWeekCashExpenseVarianceAmount;
					}
					var s1 = data.thisWeekPrevWeekVarianceBranchWiseTotalReceivablesData;
					var plotData=[];
					var ticks=[];
					for(var i=0;i<s1.length;i++){
						var arr=JSON.stringify(s1[i]);
						var newarr=arr.substring(1, arr.length-1);
						var bnchName=newarr.split(":");
						var name=bnchName[0].substring(1, bnchName[0].length-1);
						ticks.push(name);
						plotData.push(parseFloat(bnchName[1]));
						$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(4)").attr('data-tick', ticks);
						$("#auditorFinancialTable tr[class='auditorTotalReceivables'] td:nth-child(4)").attr('data-plot', plotData);
					}
				    var plot1 = $.jqplot('auditorTotalReceivablesJqPieChart4', [plotData], {
				    	seriesColors: ["#d8b83f", "#579575", "#839557", "#958c12",
				    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
				        seriesDefaults:{
				            renderer:$.jqplot.BarRenderer,
				            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
				            rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
				        },
				        axes: {
				            xaxis: {
				            	pad: 1.00,
				            	min:minAmt,
				            	max: maxAmt,
				            	numberTicks: 8,
				            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	formatString: '%d',
				                	angle: -30,
				                	textColor :'#0000FF'
				                }
				            },
				            yaxis: {
				            	renderer: $.jqplot.CategoryAxisRenderer,
				                ticks: ticks,
				                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	angle: -30,
					                fontSize: '8pt',
					                textColor : '#FF0000'
					            }
				            }
				        }
				    });
			    	    replotGraph.push(plot1);
				    $('#auditorTotalReceivablesJqPieChart4').bind('jqplotDataClick',
						function (ev, seriesIndex, pointIndex, data) {
					    	var bnchName=ticks[pointIndex];
				    	    var amountValue=data.toString().split(",");
					    	//send individual data toserver get chart of account wise breakups and fill in the data modal
				    	    dispalyAndPopulateModal(bnchName,amountValue,"thisWeekPreviousWeekTotalReceivablesVarience");
						}
					);
				}
				//totalPayables
				var thisWeekTotalPayables="";var previousWeekTotalPayables="";var totalPayablesVariance="";
				if(data.dashBoardData[0].netPayableThisWeek!=null && data.dashBoardData[0].netPayableThisWeek!=""){
					thisWeekTotalPayables=data.dashBoardData[0].netPayableThisWeek;
				}
				if(data.dashBoardData[0].netPayablePreviousWeek!=null && data.dashBoardData[0].netPayablePreviousWeek!=""){
					previousWeekTotalPayables=data.dashBoardData[0].netPayablePreviousWeek;
				}
				if(thisWeekTotalPayables!="" && previousWeekTotalPayables!=""){
					if(parseFloat(thisWeekTotalPayables)>=parseFloat(previousWeekTotalPayables)){
						totalPayablesVariance=parseFloat(thisWeekTotalPayables)-parseFloat(previousWeekTotalPayables);
						$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(4)").append("&#8593;"+parseFloat(totalPayablesVariance));
						$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(4)").append('<div id="auditorTotalPayablesJqPieChart4" class="auditorTotalPayablesJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
					if(parseFloat(previousWeekTotalPayables)>=parseFloat(thisWeekTotalPayables)){
						totalPayablesVariance=parseFloat(previousWeekTotalPayables)-parseFloat(thisWeekTotalPayables);
						$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(4)").append("&#8595;"+parseFloat(totalPayablesVariance));
						$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(4)").append('<div id="auditorTotalPayablesJqPieChart4" class="auditorTotalPayablesJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
				}else{
					if(thisWeekTotalPayables!="" && previousWeekTotalPayables==""){
						$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(4)").append("&#8593;"+parseFloat(thisWeekTotalPayables));
						$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(4)").append('<div id="auditorTotalPayablesJqPieChart4" class="auditorTotalPayablesJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
					if(previousWeekTotalPayables!="" && thisWeekTotalPayables==""){
						$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(4)").append("&#8595;"+parseFloat(previousWeekTotalPayables));
						$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(4)").append('<div id="auditorTotalPayablesJqPieChart4" class="auditorTotalPayablesJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
					if(thisWeekTotalPayables=="" && previousWeekTotalPayables==""){
						$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(4)").append(totalPayablesVariance);
						$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(4)").append('<div id="auditorTotalPayablesJqPieChart4" class="auditorTotalPayablesJqPieChart4"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
					}
				}
				$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(2)").append(thisWeekTotalPayables);
				$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(2)").append('<div id="auditorTotalPayablesJqPieChart2" class="auditorTotalPayablesJqPieChart2"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
				$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(3)").append(previousWeekTotalPayables);
				$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(3)").append('<div id="auditorTotalPayablesJqPieChart3" class="auditorTotalPayablesJqPieChart3"  STYLE="height:317px; width: 290px; overflow: auto;"></div>');
				if(thisWeekTotalPayables!=""){
					var s1 = data.thisWeekBranchWiseTotalPayablesData;
					var plotData=[];
					var ticks=[];
					for(var i=0;i<s1.length;i++){
						var arr=JSON.stringify(s1[i]);
						var newarr=arr.substring(1, arr.length-1);
						var bnchName=newarr.split(":");
						var name=bnchName[0].substring(1, bnchName[0].length-1);
						ticks.push(name);
						plotData.push(parseFloat(bnchName[1]));
						$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(2)").attr('data-tick', ticks);
						$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(2)").attr('data-plot', plotData);
					}
				    var plot1 = $.jqplot('auditorTotalPayablesJqPieChart2', [plotData], {
				    	seriesColors: [ "#579575", "#c5b47f", "#EAA228", "#579575", "#839557", "#958c12",
				    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
				        seriesDefaults:{
				            renderer:$.jqplot.BarRenderer,
				            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
				            rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
				        },
				        axes: {
				            xaxis: {
				            	pad: 1.00,
				            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	formatString: '%d',
				                	angle: -30,
				                	textColor :'#0000FF'
				                }
				            },
				            yaxis: {
				            	renderer: $.jqplot.CategoryAxisRenderer,
				                ticks: ticks,
				                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	angle: -30,
					                fontSize: '8pt',
					                textColor : '#FF0000'
					            }
				            }
				        }
				    });
			    	    replotGraph.push(plot1);
				    $('#auditorTotalPayablesJqPieChart2').bind('jqplotDataClick',
						function (ev, seriesIndex, pointIndex, data) {
					    	var bnchName=ticks[pointIndex];
				    	    var amountValue=data.toString().split(",");
					    	//send individual data toserver get chart of account wise breakups and fill in the data modal
				    	    dispalyAndPopulateModal(bnchName,amountValue,"thisWeekTotalPayables");
						}
					);
				}
				if(previousWeekTotalPayables!=""){
					var s1 = data.previousWeekBranchWiseTotalPayablesData;
					var plotData=[];
					var ticks=[];
					for(var i=0;i<s1.length;i++){
						var arr=JSON.stringify(s1[i]);
						var newarr=arr.substring(1, arr.length-1);
						var bnchName=newarr.split(":");
						var name=bnchName[0].substring(1, bnchName[0].length-1);
						ticks.push(name);
						plotData.push(parseFloat(bnchName[1]));
						$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(3)").attr('data-tick', ticks);
						$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(3)").attr('data-plot', plotData);
					}
				    var plot1 = $.jqplot('auditorTotalPayablesJqPieChart3', [plotData], {
				    	seriesColors: [ "#d8b83f", "#c5b47f", "#EAA228", "#579575", "#839557", "#958c12",
				    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
				        seriesDefaults:{
				            renderer:$.jqplot.BarRenderer,
				            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
				            rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
				        },
				        axes: {
				            xaxis: {
				            	pad: 1.00,
				            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	formatString: '%d',
				                	angle: -30,
				                	textColor :'#0000FF'
				                }
				            },
				            yaxis: {
				            	renderer: $.jqplot.CategoryAxisRenderer,
				                ticks: ticks,
				                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	angle: -30,
					                fontSize: '8pt',
					                textColor : '#FF0000'
					            }
				            }
				        }
				    });
			   	    replotGraph.push(plot1);
				    $('#auditorTotalPayablesJqPieChart3').bind('jqplotDataClick',
						function (ev, seriesIndex, pointIndex, data) {
					    	var bnchName=ticks[pointIndex];
				    	    var amountValue=data.toString().split(",");
					    	//send individual data toserver get chart of account wise breakups and fill in the data modal
				    	    dispalyAndPopulateModal(bnchName,amountValue,"previousWeekTotalPayables");
						}
					);
				}
				if(thisWeekTotalPayables!="" || previousWeekTotalPayables!=""){
					var thisWeekPrevWeekVariance=$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(4)").text();
					var minAmt="";var maxAmt="";var thisWeekPrevWeekVarianceAmount="";
					if(thisWeekPrevWeekVariance!=""){
						thisWeekPrevWeekCashExpenseVarianceAmount=parseFloat(thisWeekPrevWeekVariance.substring(1, thisWeekPrevWeekVariance.length));
						minAmt=-thisWeekPrevWeekCashExpenseVarianceAmount;
						maxAmt=thisWeekPrevWeekCashExpenseVarianceAmount;
					}
					var s1 = data.thisWeekPrevWeekVarianceBranchWiseTotalPayablesData;
					var plotData=[];
					var ticks=[];
					for(var i=0;i<s1.length;i++){
						var arr=JSON.stringify(s1[i]);
						var newarr=arr.substring(1, arr.length-1);
						var bnchName=newarr.split(":");
						var name=bnchName[0].substring(1, bnchName[0].length-1);
						ticks.push(name);
						plotData.push(parseFloat(bnchName[1]));
						$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(4)").attr('data-tick', ticks);
						$("#auditorFinancialTable tr[class='auditorTotalPayables'] td:nth-child(4)").attr('data-plot', plotData);
					}
				    var plot1 = $.jqplot('auditorTotalPayablesJqPieChart4', [plotData], {
				    	seriesColors: ["#4b5de4", "#579575", "#839557", "#958c12",
				    	                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
				        seriesDefaults:{
				            renderer:$.jqplot.BarRenderer,
				            pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
				            rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
				        },
				        axes: {
				            xaxis: {
				            	pad: 1.00,
				            	min:minAmt,
				            	max: maxAmt,
				            	numberTicks: 8,
				            	tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	formatString: '%d',
				                	angle: -30,
				                	textColor :'#0000FF'
				                }
				            },
				            yaxis: {
				            	renderer: $.jqplot.CategoryAxisRenderer,
				                ticks: ticks,
				                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
				                tickOptions: {
				                	angle: -30,
					                fontSize: '8pt',
					                textColor : '#FF0000'
					            }
				            }
				        }
				    });
			    	    replotGraph.push(plot1);
				    $('#auditorTotalPayablesJqPieChart4').bind('jqplotDataClick',
						function (ev, seriesIndex, pointIndex, data) {
					    	var bnchName=ticks[pointIndex];
				    	    var amountValue=data.toString().split(",");
					    	//send individual data toserver get chart of account wise breakups and fill in the data modal
				    	    dispalyAndPopulateModal(bnchName,amountValue,"thisWeekPreviousWeekTotalPayablesVarience");
						}
					);
				}
				for(var i=0;i<data.lastForteenDaysCustomerData.length;i++){
					$("select[class='addedCustomersDropdown']").append('<option>'+data.lastForteenDaysCustomerData[i].lastForteenDaysCustomers+'</option>');
					$("select[class='addedCustomersBranchesDropdown']").append('<optgroup label="'+data.lastForteenDaysCustomerData[i].lastForteenDaysCustomers+'/Branches">');
					var custBnch=data.lastForteenDaysCustomerData[i].lastForteenDaysCustomersBranches.split(",");
					for(var j=0;j<custBnch.length;j++){
						$("select[class='addedCustomersBranchesDropdown']").append('<option>'+custBnch[j]+'</option>');
					}
					$("select[class='addedCustomersBranchesDropdown']").append('</optgroup>');
					$("select[class='addedCustomersItemsDropdown']").append('<optgroup label="'+data.lastForteenDaysCustomerData[i].lastForteenDaysCustomers+'/Items">');
					var custItems=data.lastForteenDaysCustomerData[i].lastForteenDaysCustomersItems.split(",");
					for(var k=0;k<custItems.length;k++){
						$("select[class='addedCustomersItemsDropdown']").append('<option>'+custItems[k]+'</option>');
					}
					$("select[class='addedCustomersItemsDropdown']").append('</optgroup>');
				}
				for(var i=0;i<data.lastForteenDaysVendorsData.length;i++){
					$("select[class='addedVendorsDropdown']").append('<option>'+data.lastForteenDaysVendorsData[i].lastForteenDaysVendors+'</option>');
					$("select[class='addedVendorsBranchesDropdown']").append('<optgroup label="'+data.lastForteenDaysVendorsData[i].lastForteenDaysVendors+'/Branches">');
					var vendBnch=data.lastForteenDaysVendorsData[i].lastForteenDaysVendorBranches.split(",");
					for(var j=0;j<vendBnch.length;j++){
						$("select[class='addedVendorsBranchesDropdown']").append('<option>'+vendBnch[j]+'</option>');
					}
					$("select[class='addedVendorsBranchesDropdown']").append('</optgroup>');
					$("select[class='addedVendorsItemsDropdown']").append('<optgroup label="'+data.lastForteenDaysVendorsData[i].lastForteenDaysVendors+'/Items">');
					var vendItems=data.lastForteenDaysVendorsData[i].lastForteenDaysVendorItems.split(",");
					for(var k=0;k<vendItems.length;k++){
						$("select[class='addedVendorsItemsDropdown']").append('<option>'+vendItems[k]+'</option>');
					}
					$("select[class='addedVendorsItemsDropdown']").append('</optgroup>');
				}
				for(var i=0;i<data.lastForteenDaysUsersData.length;i++){
					$("select[class='addedUsersDropdown']").append('<option>'+data.lastForteenDaysUsersData[i].lastForteenDaysUsers+'</option>');
					$("select[class='addedUsersBranchesDropdown']").append('<optgroup label="'+data.lastForteenDaysUsersData[i].lastForteenDaysUsers+'/Branches">');
					$("select[class='addedUsersBranchesDropdown']").append('<optgroup label="'+data.lastForteenDaysUsersData[i].lastForteenDaysUsers+'/Transaction Creation Right In Branches">');
					var creationRightBnchs=data.lastForteenDaysUsersData[i].lastForteenDaysUsersCreationRightForBranches.split(",");
					for(var j=0;j<creationRightBnchs.length;j++){
						$("select[class='addedUsersBranchesDropdown']").append('<option>'+creationRightBnchs[j]+'</option>');
					}
					$("select[class='addedUsersBranchesDropdown']").append('</optgroup>');
					$("select[class='addedUsersBranchesDropdown']").append('<optgroup label="'+data.lastForteenDaysUsersData[i].lastForteenDaysUsers+'/Transaction Approval Right In Branches">');
					var approverRightBnchs=data.lastForteenDaysUsersData[i].lastForteenDaysUsersApprovalRightForBranches.split(",");
					for(var k=0;k<approverRightBnchs.length;k++){
						$("select[class='addedUsersBranchesDropdown']").append('<option>'+approverRightBnchs[k]+'</option>');
					}
					$("select[class='addedUsersBranchesDropdown']").append('</optgroup>');
					$("select[class='addedUsersBranchesDropdown']").append('</optgroup>');
					$("select[class='addedUsersRolesDropdown']").append('<optgroup label="'+data.lastForteenDaysUsersData[i].lastForteenDaysUsers+'/Roles">');
					var userRoles=data.lastForteenDaysUsersData[i].lastForteenDaysUsersRoles.split(",");
					for(var l=0;l<userRoles.length;l++){
						$("select[class='addedUsersRolesDropdown']").append('<option>'+userRoles[l]+'</option>');
					}
					$("select[class='addedUsersRolesDropdown']").append('</optgroup>');
					$("select[class='addedUsersItemsDropdown']").append('<optgroup label="'+data.lastForteenDaysUsersData[i].lastForteenDaysUsers+'/Items">');
					$("select[class='addedUsersItemsDropdown']").append('<optgroup label="'+data.lastForteenDaysUsersData[i].lastForteenDaysUsers+'/Transaction Creation Right For Items">');
					var creationRightForItems=data.lastForteenDaysUsersData[i].lastForteenDaysUsersCreationRightForItems.split(",");
					for(var m=0;m<creationRightForItems.length;m++){
						$("select[class='addedUsersItemsDropdown']").append('<option>'+creationRightForItems[m]+'</option>');
					}
					$("select[class='addedUsersItemsDropdown']").append('</optgroup>');
					$("select[class='addedUsersItemsDropdown']").append('<optgroup label="'+data.lastForteenDaysUsersData[i].lastForteenDaysUsers+'/Transaction Approval Right For Items">');
					var approverRightForItems=data.lastForteenDaysUsersData[i].lastForteenDaysUsersApprovalRightForItems.split(",");
					for(var n=0;n<approverRightForItems.length;n++){
						$("select[class='addedUsersItemsDropdown']").append('<option>'+approverRightForItems[n]+'</option>');
					}
					$("select[class='addedUsersItemsDropdown']").append('</optgroup>');
					$("select[class='addedUsersItemsDropdown']").append('</optgroup>');
				}
				$("#auditorFinancialTable tr[class='auditorCreditIncome'] div[id='addedTxnPendingApproval']").prepend(data.dashBoardData[0].forteenBackDatePendingApproval);
				for(var z=0;z<data.lastForteenDaysPendingApprovalData.length;z++){
					$("select[class='addedTxnPendingApprovalDropdown']").append('<option>'+data.lastForteenDaysPendingApprovalData[z].forteenBackDatePendingApprovalTxnRef+'</option>');
				}
				$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('<optgroup label="Transaction Exceeding Budget">');
				for(q=0;q<data.lastForteenDaysTxnExceedingBudgetAWHData.length;q++){
					$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('<optgroup id="txnSpecificsBudgetRuleNotFollowed" classname="'+data.lastForteenDaysTxnExceedingBudgetAWHData[q].txnExceedingBudgetBranchRefNoAHSpecificsId+'" label="'+data.lastForteenDaysTxnExceedingBudgetAWHData[q].txnExceedingBudgetBranchRefNoAH+'">');
					var ahwTxnExceedingBudgedRefNumber=data.lastForteenDaysTxnExceedingBudgetAWHData[q].txnExceedingBudgetBranchRefNo.split(',');
					for(var u=0;u<ahwTxnExceedingBudgedRefNumber.length;u++){
						$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('<option>'+ahwTxnExceedingBudgedRefNumber[u]+'</option>');
					}
					$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('</optgroup>');
					$("select[class='txnExceedingBudgetKlNotFollowedGroupDropdown']").append('<option value="'+data.lastForteenDaysTxnExceedingBudgetAWHData[q].txnExceedingBudgetBranchRefNoAHSpecificsId+'">'+data.lastForteenDaysTxnExceedingBudgetAWHData[q].txnExceedingBudgetBranchRefNoAH+'</option>');
				}
				$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('</optgroup>');
				$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('<optgroup label="Rules Not Followed">');
				for(var q1=0;q1<data.lastForteenDaysTxnKlNotFollwedAWHData.length;q1++){
					$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('<optgroup id="txnSpecificsBudgetRuleNotFollowed" classname="'+data.lastForteenDaysTxnKlNotFollwedAWHData[q1].txnKlNotFollowedBranchRefNoAHSpecificsId+'" label="'+data.lastForteenDaysTxnKlNotFollwedAWHData[q1].txnKlNotFollowedBranchRefNoAH+'">');
					var ahwTxnKlNotFollwedRefNumber=data.lastForteenDaysTxnKlNotFollwedAWHData[q1].txnKlNotFollowedBranchRefNo.split(',');
					for(var v=0;v<ahwTxnKlNotFollwedRefNumber.length;v++){
						$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('<option>'+ahwTxnKlNotFollwedRefNumber[v]+'</option>');
					}
					$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('</optgroup>');
				}
				$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('</optgroup>');
				$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] div[id='bnchWithHighestExpense']").html("");
				$("#auditorFinancialTable tr[class='auditorExpenseBudgetAvailable'] div[id='bnchWithHighestExpense']").append(data.dashBoardData[0].maxExpenseBranch);
				$("#auditorFinancialTable tr[class='auditorTotalReceivables'] div[id='bnchWithHighestIncome']").html("");
				$("#auditorFinancialTable tr[class='auditorTotalReceivables'] div[id='bnchWithHighestIncome']").append(data.dashBoardData[0].maxIncomeBranch);
			}else{
				doLogout();
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

function createUserClaimSetUp(){
	//alert("createUserClaimSetUp"); //sunil
	$('#user-form-container fieldset').append('<div id="userClaimSetUpRule" style="margin-left: 0px;">'+
	'<h5 class="alert success" style="width: 730px;"><b>User Claims SetUp Rules</b>&nbsp;&nbsp;<img longdesc="" id="userclaimsetup" src="/assets/images/eye.png" width="16px;"></img></h5>'+
	'<table class="table table-hover table-striped table-bordered" id="newuserClaimSetupExcelFormTable" style="margin-top: 0px; width: 780px;">'+
	'<thead class="tablehead1"><tr><th>User Claims For Chart OF Account Item</th><th>Eligibility for mode of travel / monetary limit</th>'+
	'<th>Hotel - Daily limit</th><th>Per diam (excluding inter city travel)</th></tr></thead><tbody>'+
	'<tr id="userClaimRow"><td><select multiple="multiple" name="userClaimCOA" id="userClaimCOA"></select></td>'+
	'<td><div class="btn-group"><button id="userclaimsetuptravelmode" class="multiselect dropdown-toggle btn" style="width: 130px;"> None Selected  &#8711;</button>'+
	'<div id="userclaimsetuptravelmodedropdown-menuid" class="userclaimsetuptravelmodedropdown-menu">'+
	'<ul class="userclaimsetupTravelModeList" id="userclaimsetupTravelModeList"></ul></div></div></td>'+
	'<td><div class="btn-group"><button id="userclaimsetuphotel" class="multiselect dropdown-toggle btn" style="width: 130px;"> None Selected  &#8711;</button>'+
	'<div id="userclaimsetuphoteldropdown-menuid" class="userclaimsetuphoteldropdown-menu">'+
	'<ul class="userclaimsetupHotelList" id="userclaimsetupHotelList"></ul></div></div></td>'+
	'<td><div class="btn-group"><button id="userclaimsetupperdiam" class="multiselect dropdown-toggle btn" style="width: 130px;"> None Selected  &#8711;</button>'+
	'<div id="userclaimsetupperdiamdropdown-menuid" class="userclaimsetupperdiamdropdown-menu">'+
	'<ul class="userclaimsetupPerDiamList" id="userclaimsetupPerDiamList"></ul></div></div></td></tr></tbody></table></div>');
	var jsonData = {};
	jsonData.usermail = $("#hiddenuseremail").text();
	var url="/users/getUsersClaimData";
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
			$("select[name='userClaimCOA']").children().remove();
			for(var i=0;i<data.userClaimCoaDatas.length;i++){
				$("select[name='userClaimCOA']").append('<option value='+data.userClaimCoaDatas[i].id+'>'+data.userClaimCoaDatas[i].name+'</option>')
			}
			$('#userClaimCOA').multiselect({
			    buttonWidth: '150px',
			    maxHeight:   150,
			    includeSelectAllOption: true,
			    enableFiltering :false,
			    /*
			    buttonText: function(options) {
			      if (options.length == 0) {
			              return 'None selected <b class="caret"></b>';
			          }
			          else if (options.length > 6) {
			              return options.length + ' selected  <b class="caret"></b>';
			          }
			          else {
			              var selected = '';
			              options.each(function() {
			          selected += $(this).text() + ', ';
			              });

			              return options.length + ' selected  <b class="caret"></b>';
			      }
			    }, */
			    onChange: function(element, checked) {
			      if(checked == true) {
			      }
			      else if(checked == false) {
			      }
			    }
			  });
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}


function getBranchPremiseDetailsForUpdate(orgEntity,branchEntity,bnchPremiseDiv){
	//alert(">>>>>34"); //sunil
	var jsonData = {};
	jsonData.organizationEntityId = orgEntity;
	jsonData.branchEntityId=branchEntity;
	var url="/branch/branchPremiseEntityInformation";
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
			$(".branchName").text(data.branchPremiseEntityInfo[0].branchName);
			if(bnchPremiseDiv=="bnchPremiseDivForValidity"){
				if(data.branchPremiseEntityInfo[0].premiseRentRevisionDueOn!=null && data.branchPremiseEntityInfo[0].premiseRentRevisionDueOn!=""){
					$("#bnchPremiseDiv input[id='bnchPremiseAlertRentRevisedDueOn']").val(data.branchPremiseEntityInfo[0].premiseRentRevisionDueOn);
				}
				$("#bnchPremiseDiv input[name='bnchPremiseAlertValidityFrom']").removeAttr("readonly");
				$("#bnchPremiseDiv input[name='bnchPremiseAlertValidityTo']").removeAttr("readonly");
				$("div[class='branchPremiseValidityEndStatic']").text("Validity Ends On:"+data.branchPremiseEntityInfo[0].premiseAgreementValidityTo);
				$("div[class='branchPremiseValidityLastUpdatedStatic']").text(data.branchPremiseEntityInfo[0].premisePremiseValidityLastUpdated);
				$("div[class='branchPremiseRentRevisionStatic']").text("");
				$("div[class='branchPremiseRentRevisionLastUpdatedStatic']").text("");
				$("div[class='branchPremiseRentRevisionLastUpdatedStatic']").text("");
				$("div[class='branchPremiseLastTimeRentPaymentDueDatedStatic']").text("");
				$("div[class='branchPremiseLastTimeRentPaymentStatic']").text("");
			}
			if(bnchPremiseDiv=="bnchPremiseDivForRentRenewal"){
				if(data.branchPremiseEntityInfo[0].premiseaggreement!=null && data.branchPremiseEntityInfo[0].premiseaggreement!=""){
					$("#bnchPremiseDiv input[name='bnchPremiseAlertAggreement']").val(data.branchPremiseEntityInfo[0].premiseaggreement);
				}
				if(data.branchPremiseEntityInfo[0].premiseAgreementValidityFrom!=null && data.branchPremiseEntityInfo[0].premiseAgreementValidityFrom!=""){
					$("#bnchPremiseDiv input[name='bnchPremiseAlertValidityFrom']").val(data.branchPremiseEntityInfo[0].premiseAgreementValidityFrom);
				}
				if(data.branchPremiseEntityInfo[0].premiseAgreementValidityTo!=null && data.branchPremiseEntityInfo[0].premiseAgreementValidityTo!=""){
					$("#bnchPremiseDiv input[name='bnchPremiseAlertValidityTo']").val(data.branchPremiseEntityInfo[0].premiseAgreementValidityTo);
				}
				$("#bnchPremiseDiv input[id='bnchPremiseAlertRentRevisedDueOn']").removeAttr("readonly");
				$("#bnchPremiseDiv input[name='bnchPremiseAlertValidityFrom']").attr('readonly','readonly');
				$("#bnchPremiseDiv input[name='bnchPremiseAlertValidityTo']").attr('readonly','readonly');
				$("div[class='branchPremiseRentRevisionStatic']").text("Rental Renewable Date:"+data.branchPremiseEntityInfo[0].premiseRentRevisionDueOn);
				$("div[class='branchPremiseRentRevisionLastUpdatedStatic']").text(data.branchPremiseEntityInfo[0].premisePremiseLastRentRenewable);
				$("div[class='branchPremiseValidityEndStatic']").text("");
				$("div[class='branchPremiseValidityLastUpdatedStatic']").text("");
				$("div[class='branchPremiseLastTimeRentPaymentDueDatedStatic']").text("");
				$("div[class='branchPremiseLastTimeRentPaymentStatic']").text("");
			}
			if(bnchPremiseDiv=="bnchPremiseDivForRentPayment"){
				if(data.branchPremiseEntityInfo[0].premiseaggreement!=null && data.branchPremiseEntityInfo[0].premiseaggreement!=""){
					$("#bnchPremiseDiv input[name='bnchPremiseAlertAggreement']").val(data.branchPremiseEntityInfo[0].premiseaggreement);
				}
				if(data.branchPremiseEntityInfo[0].premiseAgreementValidityFrom!=null && data.branchPremiseEntityInfo[0].premiseAgreementValidityFrom!=""){
					$("#bnchPremiseDiv input[name='bnchPremiseAlertValidityFrom']").val(data.branchPremiseEntityInfo[0].premiseAgreementValidityFrom);
				}
				if(data.branchPremiseEntityInfo[0].premiseAgreementValidityTo!=null && data.branchPremiseEntityInfo[0].premiseAgreementValidityTo!=""){
					$("#bnchPremiseDiv input[name='bnchPremiseAlertValidityTo']").val(data.branchPremiseEntityInfo[0].premiseAgreementValidityTo);
				}
				if(data.branchPremiseEntityInfo[0].premiseRentRevisionDueOn!=null && data.branchPremiseEntityInfo[0].premiseRentRevisionDueOn!=""){
					$("#bnchPremiseDiv input[id='bnchPremiseAlertRentRevisedDueOn']").val(data.branchPremiseEntityInfo[0].premiseRentRevisionDueOn);
				}
				$("#bnchPremiseDiv input[id='bnchPremiseAlertRentRevisedDueOn']").attr('readonly','readonly');
				$("#bnchPremiseDiv input[name='bnchPremiseAlertValidityFrom']").attr('readonly','readonly');
				$("#bnchPremiseDiv input[name='bnchPremiseAlertValidityTo']").attr('readonly','readonly');
				$("div[class='branchPremiseLastTimeRentPaymentDueDatedStatic']").text(data.branchPremiseEntityInfo[0].branchPremiseLastPaymentDueDated);
				$("div[class='branchPremiseLastTimeRentPaymentStatic']").text(data.branchPremiseEntityInfo[0].branchPremiseLastPaymentPaidDate);
				$("div[class='branchPremiseValidityEndStatic']").text("");
				$("div[class='branchPremiseRentRevisionStatic']").text("");
				$("div[class='branchPremiseRentRevisionStatic']").text("");
				$("div[class='branchPremiseRentRevisionLastUpdatedStatic']").text("");
			}
			if(data.branchPremiseEntityInfo[0].premisePaymentPeriodicity!=null && data.branchPremiseEntityInfo[0].premisePaymentPeriodicity!=""){
				$("#bnchPremiseDiv select[id='bnchPremiseAlertPeriodicityOfPayment'] option").filter(function () {return $(this).val()==data.branchPremiseEntityInfo[0].premisePaymentPeriodicity;}).prop("selected", "selected");
			}
			if(data.branchPremiseEntityInfo[0].premiseRentPayable!=null && data.branchPremiseEntityInfo[0].premiseRentPayable!=""){
				$("#bnchPremiseDiv input[id='bnchPremiseAlertRentPayable']").val(data.branchPremiseEntityInfo[0].premiseRentPayable);
			}
			if(data.branchPremiseEntityInfo[0].premiseLandlordname!=null && data.branchPremiseEntityInfo[0].premiseLandlordname!=""){
				$("#bnchPremiseDiv input[id='bnchPremiseAlertLandlordName']").val(data.branchPremiseEntityInfo[0].premiseLandlordname);
			}
			if(data.branchPremiseEntityInfo[0].premiseLandlordaddress!=null && data.branchPremiseEntityInfo[0].premiseLandlordaddress!=""){
				$("#bnchPremiseDiv textarea[id='bnchPremiseAlertLandlordAddress']").val(data.branchPremiseEntityInfo[0].premiseLandlordaddress);
			}
			if(data.branchPremiseEntityInfo[0].premiseLandlordbankaccountname!=null && data.branchPremiseEntityInfo[0].premiseLandlordbankaccountname!=""){
				$("#bnchPremiseDiv input[id='bnchPremiseAlertBankAccountName']").val(data.branchPremiseEntityInfo[0].premiseLandlordbankaccountname);
			}
			if(data.branchPremiseEntityInfo[0].premiseLandlordbankaccountnumber!=null && data.branchPremiseEntityInfo[0].premiseLandlordbankaccountnumber!=""){
				$("#bnchPremiseDiv input[id='bnchPremiseAlertBankAccountNumber']").val(data.branchPremiseEntityInfo[0].premiseLandlordbankaccountnumber);
			}
			if(data.branchPremiseEntityInfo[0].premiseLandlordbankaccountbranch!=null && data.branchPremiseEntityInfo[0].premiseLandlordbankaccountbranch!=""){
				$("#bnchPremiseDiv input[id='bnchPremiseAlertBankAccountBranch']").val(data.branchPremiseEntityInfo[0].premiseLandlordbankaccountbranch);
			}
			if(data.branchPremiseEntityInfo[0].premiseRemarks!=null && data.branchPremiseEntityInfo[0].premiseRemarks!=""){
				$("#bnchPremiseDiv textarea[id='bnchPremiseAlertRentDueOnRemarks']").val(data.branchPremiseEntityInfo[0].premiseRemarks);
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

$(document).ready(function(){

	$('.updateBranchPremiseValidty'). click(function(){
		//alert(">>>>>35"); //sunil
		var alertUserEmail=GetURLParameter('alertUserEmail');
		var orgEntity=GetURLParameter('orgEntity');
		var branchEntity=GetURLParameter('branchEntity');
		var taskAlertGroupingDate=GetURLParameter('taskAlertGroupingDate');
		var validityFrom=$("#bnchPremiseDiv input[id='bnchPremiseAlertValidityFrom']").val();
		var validityTo=$("#bnchPremiseDiv input[id='bnchPremiseAlertValidityTo']").val();
		if(validityFrom=="" || validityTo==""){
			swal("Invalid data!","Please provide Branch Premise Agreement Proper validity dates","error");
			return true;
		}
		var agreementUrl=$("#bnchPremiseDiv input[name='bnchPremiseAlertAggreement']").val();
		var jsonData = {};
		jsonData.userEmail=alertUserEmail;
		jsonData.organizationEntityId = orgEntity;
		jsonData.branchEntityId=branchEntity;
		jsonData.branchValidityFrom=validityFrom;
		jsonData.branchValidityTo=validityTo;
		jsonData.branchAgreemnet=agreementUrl;
		jsonData.branchtaskAlertGroupingDate=taskAlertGroupingDate;
		var url="/branch/updateBranchPremiseValidity";
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
				$("#successalertmsg").show();
				$(".successtext").text(data.alertmsg[0].success);
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});

$(document).ready(function(){
	$('.updateBranchPremiseRentRenewalDate'). click(function(){
		var alertUserEmail=GetURLParameter('alertUserEmail');
		var orgEntity=GetURLParameter('orgEntity');
		var branchEntity=GetURLParameter('branchEntity');
		var taskAlertGroupingDate=GetURLParameter('taskAlertGroupingDate');
		var rentRenewalDue=$("#bnchPremiseDiv input[id='bnchPremiseAlertRentRevisedDueOn']").val();
		if(rentRenewalDue==""){
			swal("Invalid data!","Please provide Branch Premise Rent Renewal date","error");
			return true;
		}
		var agreementUrl=$("#bnchPremiseDiv input[name='bnchPremiseAlertAggreement']").val();
		var jsonData = {};
		jsonData.userEmail=alertUserEmail;
		jsonData.organizationEntityId = orgEntity;
		jsonData.branchEntityId=branchEntity;
		jsonData.branchrentRenewalDue=rentRenewalDue;
		jsonData.branchAgreemnet=agreementUrl;
		jsonData.branchtaskAlertGroupingDate=taskAlertGroupingDate;
		var url="/branch/updateBranchPremiseRentRenewalDate";
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
				$("#successalertmsg").show();
				$(".successtext").text(data.alertmsg[0].success);
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});

$(document).ready(function(){
	$('.paidBranchPremiseRent'). click(function(){
		var rentPaymentDate=GetURLParameter('rentPaymentDate');
		var alertUserEmail=GetURLParameter('alertUserEmail');
		var orgEntity=GetURLParameter('orgEntity');
		var branchEntity=GetURLParameter('branchEntity');
		var taskAlertGroupingDate=GetURLParameter('taskAlertGroupingDate');
		var agreementUrl=$("#bnchPremiseDiv input[name='bnchPremiseAlertAggreement']").val();
		var jsonData = {};
		jsonData.userEmail=alertUserEmail;
		jsonData.organizationEntityId = orgEntity;
		jsonData.branchEntityId=branchEntity;
		jsonData.branchrentDueDated=rentPaymentDate;
		jsonData.branchtaskAlertGroupingDate=taskAlertGroupingDate;
		var url="/branch/confirmBranchPremiseRentPayment";
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
				$("#successalertmsg").show();
				$(".successtext").text(data.alertmsg[0].success);
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});

function getBranchStatutoryDetailsForUpdate(orgEntity,branchEntity,branchStatutoryEntity,bnchStatutoryDiv){
	var jsonData = {};
	jsonData.organizationEntityId = orgEntity;
	jsonData.branchEntityId=branchEntity;
	jsonData.branchStatutoryEntityId=branchStatutoryEntity;
	var url="/branch/branchIndividualStatutoryInfo";
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
			$(".statutoryBranch").text(data.branchStatutoryEntityInfo[0].branchName);
			if(data.branchStatutoryEntityInfo[0].statutoryDetails!=null && data.branchStatutoryEntityInfo[0].statutoryDetails!=""){
				$(".statutoryDetails").text(data.branchStatutoryEntityInfo[0].statutoryDetails);
				$("#bnchStatutoryDiv textarea[id='statutoryAlertDetails']").val(data.branchStatutoryEntityInfo[0].statutoryDetails);
			}
			if(data.branchStatutoryEntityInfo[0].statutoryRegistrationNumber!=null && data.branchStatutoryEntityInfo[0].statutoryRegistrationNumber!=""){
				$("#bnchStatutoryDiv input[id='bnchStatutoryAlertRegistrationNumber']").val(data.branchStatutoryEntityInfo[0].statutoryRegistrationNumber);
			}
			if(data.branchStatutoryEntityInfo[0].statAltForAction!=null && data.branchStatutoryEntityInfo[0].statAltForAction!=""){
				$("#bnchStatutoryDiv select[id='statalertForAction']").append('<option value="'+data.branchStatutoryEntityInfo[0].statAltForAction+'" selected="selected">'+data.branchStatutoryEntityInfo[0].statAltForAction+'</option>');
			}
			if(data.branchStatutoryEntityInfo[0].statAltForInformation!=null && data.branchStatutoryEntityInfo[0].statAltForInformation!=""){
				$("#bnchStatutoryDiv select[id='statalertForInformation']").append('<option value="'+data.branchStatutoryEntityInfo[0].statAltForInformation+'" selected="selected">'+data.branchStatutoryEntityInfo[0].statAltForInformation+'</option>');
			}
			if(data.branchStatutoryEntityInfo[0].statConsultantNameAddress!=null && data.branchStatutoryEntityInfo[0].statConsultantNameAddress!=""){
				$("#bnchStatutoryDiv textarea[id='alertnameAddressOfConsultant']").val(data.branchStatutoryEntityInfo[0].statConsultantNameAddress);
			}
			if(data.branchStatutoryEntityInfo[0].statRemarks!=null && data.branchStatutoryEntityInfo[0].statRemarks!=""){
				$("#bnchStatutoryDiv textarea[id='statutoryAlertRemarks']").val(data.branchStatutoryEntityInfo[0].statRemarks);
			}
			if(data.branchStatutoryEntityInfo[0].branchStatutoryEndsOn!=null && data.branchStatutoryEntityInfo[0].branchStatutoryEndsOn!=""){
				$("div[class='branchStatutoryValidityEndStatic']").text("Branch Statutory Validity Ends On:"+data.branchStatutoryEntityInfo[0].branchStatutoryEndsOn);
			}
			if(data.branchStatutoryEntityInfo[0].branchStatutoryLastUpdatedDate!=null && data.branchStatutoryEntityInfo[0].branchStatutoryLastUpdatedDate!=""){
				$("div[class='branchStatutoryValidityLastUpdatedStatic']").text(data.branchStatutoryEntityInfo[0].branchStatutoryLastUpdatedDate);
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}


function getBranchOperationalRemaindersForUpdate(orgEntity,branchEntity,branchOperationalRemainderEntity,operRemGoingDate,bnchOpeRemAlertDiv){
	var jsonData = {};
	jsonData.organizationEntityId = orgEntity;
	jsonData.branchEntityId=branchEntity;
	jsonData.branchOperationalRemainderEntityId=branchOperationalRemainderEntity;
	var url="/branch/branchIndividualOperationalRemInfo";
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
			$(".operationalRemBranch").text(data.branchOperationalRemEntityInfo[0].branchName);
			$(".operationalRemDueDated").text(operRemGoingDate);
			if(data.branchOperationalRemEntityInfo[0].operationalRequirements!=null && data.branchOperationalRemEntityInfo[0].operationalRequirements!=""){
				$(".operationalRemRequirements").text(data.branchOperationalRemEntityInfo[0].operationalRequirements);
				$("#bnchOpeRemAlertDiv textarea[id='operationalRemAlertRequirements']").val(data.branchOperationalRemEntityInfo[0].operationalRequirements);
			}
			if(data.branchOperationalRemEntityInfo[0].operationalRemDueOn!=null && data.branchOperationalRemEntityInfo[0].operationalRemDueOn!=""){
				$("#bnchOpeRemAlertDiv input[id='alertdueOn']").val(data.branchOperationalRemEntityInfo[0].operationalRemDueOn);
			}
			if(data.branchOperationalRemEntityInfo[0].operationalRemRecurrence!=null && data.branchOperationalRemEntityInfo[0].operationalRemRecurrence!=""){
				$("#bnchOpeRemAlertDiv select[id='alertrecurrences'] option").filter(function () {return $(this).val()==data.branchOperationalRemEntityInfo[0].operationalRemRecurrence;}).prop("selected", "selected");
			}
			if(data.branchOperationalRemEntityInfo[0].operationalRemAlertForAction!=null && data.branchOperationalRemEntityInfo[0].operationalRemAlertForAction!=""){
				$("#bnchOpeRemAlertDiv select[id='alertbnchOperRemalertForAction']").append('<option value="'+data.branchOperationalRemEntityInfo[0].operationalRemAlertForAction+'" selected="selected">'+data.branchOperationalRemEntityInfo[0].operationalRemAlertForAction+'</option>');
			}
			if(data.branchOperationalRemEntityInfo[0].operationalRemAlertForInformation!=null && data.branchOperationalRemEntityInfo[0].operationalRemAlertForInformation!=""){
				$("#bnchOpeRemAlertDiv select[id='alertbnchOperRemalertForInformation']").append('<option value="'+data.branchOperationalRemEntityInfo[0].operationalRemAlertForInformation+'" selected="selected">'+data.branchOperationalRemEntityInfo[0].operationalRemAlertForInformation+'</option>');
			}
			if(data.branchOperationalRemEntityInfo[0].operationalRemRemarks!=null && data.branchOperationalRemEntityInfo[0].operationalRemRemarks!=""){
				$("#bnchOpeRemAlertDiv textarea[id='operationalRemAlertRemarks']").val(data.branchOperationalRemEntityInfo[0].operationalRemRemarks);
			}
			if(data.branchOperationalRemEntityInfo[0].branchOperationalRemainderLastActionDueDated!=null && data.branchOperationalRemEntityInfo[0].branchOperationalRemainderLastActionDueDated!=""){
				$("div[class='branchOperationalRemainderLastTimeActionDueDatedStatic']").text(data.branchOperationalRemEntityInfo[0].branchOperationalRemainderLastActionDueDated);
			}
			if(data.branchOperationalRemEntityInfo[0].branchOperationalRemainderLastActionDate!=null && data.branchOperationalRemEntityInfo[0].branchOperationalRemainderLastActionDate!=""){
				$("div[class='branchOperationalRemainderLastTimeRemindedDateStatic']").text(data.branchOperationalRemEntityInfo[0].branchOperationalRemainderLastActionDate);
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

$(document).ready(function(){
	$('.confirmBranchOperationalRemainder'). click(function(){
		var alertUserEmail=GetURLParameter('alertUserEmail');
		var orgEntity=GetURLParameter('orgEntity');
		var branchEntity=GetURLParameter('branchEntity');
		var taskAlertGroupingDate=GetURLParameter('taskAlertGroupingDate');
		var branchOperationalRemainderEntity=GetURLParameter('branchOperationalRemainderEntity');
		var operRemGoingDate=GetURLParameter('operRemGoingDate');
		var jsonData = {};
		jsonData.userEmail=alertUserEmail;
		jsonData.organizationEntityId = orgEntity;
		jsonData.branchEntityId=branchEntity;
		jsonData.branchOperationalRemainderEntityId=branchOperationalRemainderEntity;
		jsonData.branchOperRemGoingDate=operRemGoingDate;
		jsonData.branchtaskAlertGroupingDate=taskAlertGroupingDate;
		var url="/branch/confirmBranchOperationalRemAction";
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
				$("#successalertmsg").show();
				$(".successtext").text(data.alertmsg[0].success);
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});

function getBranchPolicyForUpdate(orgEntity,branchEntity,branchInsurenceEntity,bnchInsurenceDiv){
	var jsonData = {};
	jsonData.organizationEntityId = orgEntity;
	jsonData.branchEntityId=branchEntity;
	jsonData.branchInsurenceEntityId=branchInsurenceEntity;
	var url="/branch/branchIndividualPolicyInfo";
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
			$(".insurenceBranch").text(data.branchInsurencePolicyEntityInfo[0].branchName);
			if(data.branchInsurencePolicyEntityInfo[0].policyType!=null && data.branchInsurencePolicyEntityInfo[0].policyType!=""){
				$("#bnchInsurenceDiv input[id='alerttypeOfPolicy']").val(data.branchInsurencePolicyEntityInfo[0].policyTypes);
			}
			if(data.branchInsurencePolicyEntityInfo[0].policyNumber!=null && data.branchInsurencePolicyEntityInfo[0].policyNumber!=""){
				$(".insurenceNumber").text(data.branchInsurencePolicyEntityInfo[0].policyNumber);
				$("#bnchInsurenceDiv input[id='alertpolicyNumber']").val(data.branchInsurencePolicyEntityInfo[0].policyNumber);
			}
			if(data.branchInsurencePolicyEntityInfo[0].insurenceCompany!=null && data.branchInsurencePolicyEntityInfo[0].insurenceCompany!=""){
				$("#bnchInsurenceDiv input[id='alertinsurenceCompany']").val(data.branchInsurencePolicyEntityInfo[0].insurenceCompany);
			}
			if(data.branchInsurencePolicyEntityInfo[0].policyAnnualPremium!=null && data.branchInsurencePolicyEntityInfo[0].policyAnnualPremium!=""){
				$("#bnchInsurenceDiv input[id='alertpremiumAmount']").val(data.branchInsurencePolicyEntityInfo[0].policyAnnualPremium);
			}
			if(data.branchInsurencePolicyEntityInfo[0].policyAlertForAction!=null && data.branchInsurencePolicyEntityInfo[0].policyAlertForAction!=""){
				$("#bnchInsurenceDiv select[id='alertbnchInsalertForAction']").append('<option value="'+data.branchInsurencePolicyEntityInfo[0].policyAlertForAction+'" selected="selected">'+data.branchInsurencePolicyEntityInfo[0].policyAlertForAction+'</option>');
			}
			if(data.branchInsurencePolicyEntityInfo[0].policyAlertForInformation!=null && data.branchInsurencePolicyEntityInfo[0].policyAlertForInformation!=""){
				$("#bnchInsurenceDiv select[id='alertbnchInsalertForInformation']").append('<option value="'+data.branchInsurencePolicyEntityInfo[0].policyAlertForInformation+'" selected="selected">'+data.branchInsurencePolicyEntityInfo[0].policyAlertForInformation+'</option>');
			}
			if(data.branchInsurencePolicyEntityInfo[0].policyRemarks!=null && data.branchInsurencePolicyEntityInfo[0].policyRemarks!=""){
				$("#bnchInsurenceDiv textarea[id='alertremarks']").val(data.branchInsurencePolicyEntityInfo[0].policyRemarks);
			}
			$("div[class='branchInsuranceValidityEndStatic']").text("Validity Ends On:"+data.branchInsurencePolicyEntityInfo[0].policyValidTo);
			$("div[class='branchInsuranceValidityLastUpdatedStatic']").text(data.branchInsurencePolicyEntityInfo[0].branchInsuranceLastUpdatedValidityDate);
			$("div[class='branchInsuranceLastPremiumPaymentDueDated']").text("");
			$("div[class='branchInsuranceLastPremiumPaymentPaidDate']").text("");
			$("#updateInsurencePolicyValidty").show();
			$("#confirmBranchInsurencePremiumPayment").hide();
			$("#bnchInsurenceDiv input[id='alertinsuranceValidityFrom']").removeAttr("readonly");
			$("#bnchInsurenceDiv input[id='alertinsuranceValidityFrom']").removeAttr("readonly");
			if(bnchInsurenceDiv=="bnchInsurencePremDiv"){
				$("#updateInsurencePolicyValidty").hide();
				$("#confirmBranchInsurencePremiumPayment").show();
				if(data.branchInsurencePolicyEntityInfo[0].policyValidFrom!=null && data.branchInsurencePolicyEntityInfo[0].policyValidFrom!=""){
					$("#bnchInsurenceDiv input[id='alertinsuranceValidityFrom']").val(data.branchInsurencePolicyEntityInfo[0].policyValidFrom);
				}
				if(data.branchInsurencePolicyEntityInfo[0].policyValidTo!=null && data.branchInsurencePolicyEntityInfo[0].policyValidTo!=""){
					$("#bnchInsurenceDiv input[id='alertinsuranceValidityTo']").val(data.branchInsurencePolicyEntityInfo[0].policyValidTo);
				}
				if(data.branchInsurencePolicyEntityInfo[0].insurencePolicyDocUrl!=null && data.branchInsurencePolicyEntityInfo[0].insurencePolicyDocUrl!=""){
					$("#bnchInsurenceDiv input[name='alertinsurancePolicy']").val(data.branchInsurencePolicyEntityInfo[0].insurencePolicyDocUrl);
				}
				$("div[class='branchInsuranceLastPremiumPaymentDueDated']").text(data.branchInsurencePolicyEntityInfo[0].branchInsuranceAnnualPremiumDueDated);
				$("div[class='branchInsuranceLastPremiumPaymentPaidDate']").text(data.branchInsurencePolicyEntityInfo[0].branchInsuranceAnnualPremiumLastPaidDate);
				$("div[class='branchInsuranceValidityEndStatic']").text("");
				$("div[class='branchInsuranceValidityLastUpdatedStatic']").text("");
				$("#bnchInsurenceDiv input[id='alertinsuranceValidityFrom']").attr('readonly','readonly');
				$("#bnchInsurenceDiv input[id='alertinsuranceValidityFrom']").attr('readonly','readonly');
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

$(document).ready(function(){
	$('.updateInsurencePolicyValidty'). click(function(){
		var alertUserEmail=GetURLParameter('alertUserEmail');
		var orgEntity=GetURLParameter('orgEntity');
		var branchEntity=GetURLParameter('branchEntity');
		var taskAlertGroupingDate=GetURLParameter('taskAlertGroupingDate');
		var branchInsurenceEntity=GetURLParameter('branchInsurenceEntity');
		var validityFrom=$("#bnchInsurenceDiv input[id='alertinsuranceValidityFrom']").val();
		var validityTo=$("#bnchInsurenceDiv input[id='alertinsuranceValidityTo']").val();
		if(validityFrom=="" || validityTo==""){
			swal("Invalid data!","Please provide Proper Branch Insurence Validity dates","error");
			return true;
		}
		var insurencePolicyDocUrl=$("#bnchInsurenceDiv input[name='alertinsurancePolicy']").val();
		var jsonData = {};
		jsonData.userEmail=alertUserEmail;
		jsonData.organizationEntityId = orgEntity;
		jsonData.branchEntityId=branchEntity;
		jsonData.branchInsurenceEntityId=branchInsurenceEntity;
		jsonData.branchInsurenceValidityFrom=validityFrom;
		jsonData.branchInsurenceValidityTo=validityTo;
		jsonData.branchInsurencePolicyDocUrl=insurencePolicyDocUrl;
		jsonData.branchtaskAlertGroupingDate=taskAlertGroupingDate;
		var url="/branch/updateBranchInsurenceValidity";
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
				$("#successalertmsg").show();
				$(".successtext").text(data.alertmsg[0].success);
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});

$(document).ready(function(){
	$('.confirmBranchInsurencePremiumPayment'). click(function(){
		var premiumGoingDt=GetURLParameter('premiumGoingDt');
		var alertUserEmail=GetURLParameter('alertUserEmail');
		var orgEntity=GetURLParameter('orgEntity');
		var branchEntity=GetURLParameter('branchEntity');
		var taskAlertGroupingDate=GetURLParameter('taskAlertGroupingDate');
		var branchInsurenceEntity=GetURLParameter('branchInsurenceEntity');
		var insurencePolicyDocUrl=$("#bnchInsurenceDiv input[name='alertinsurancePolicy']").val();
		var jsonData = {};
		jsonData.userEmail=alertUserEmail;
		jsonData.organizationEntityId = orgEntity;
		jsonData.branchEntityId=branchEntity;
		jsonData.branchInsurenceEntityId=branchInsurenceEntity;
		jsonData.branchpremiumGoingDt=premiumGoingDt;
		jsonData.branchInsurencePolicyDocUrl=insurencePolicyDocUrl;
		jsonData.branchtaskAlertGroupingDate=taskAlertGroupingDate;
		var url="/branch/confirmBranchInsurencePremiumPayment";
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
				$("#successalertmsg").show();
				$(".successtext").text(data.alertmsg[0].success);
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});

function checkScrollBar(){
    var hContent = $("body").height();
    var hWindow = $(window).height();
    console.log(hContent);
    console.log(hWindow);
    if(hContent>=hWindow) {
    	 $("table[id='fixedheadertransactionTable'] th[class='dynmtxnwidthth']").css("width",180);
    }else{
    	 $("table[id='fixedheadertransactionTable'] th[class='dynmtxnwidthth']").css("width",194);
    }
}

//function to detect browser visibility
$(document).on({
    'show.visibility': function() {
    	outOfSession();
    },
});

function outOfSession(){
	 $.get('/config/getsessionuser',function(data) {
	   console.log(data.configUserInSessionData[0].sessionemail);
  	   if(data.configUserInSessionData[0].sessionemail=='null'){
  		 console.log(data.configUserInSessionData[0].sessionemail);
  		 window.location.href="/logout";
  	   }
     });
}

//for odd transaction columns
var clean1 = function(){
	$('#transactionTable td:nth-child(1)').removeClass('txnoddcolHover');
}
$(document).on("mouseenter", "#transactionTable td:nth-child(1)" ,function (){
	clean1();
	$('#transactionTable td:nth-child('+($(this).index()+1)+')').addClass('txnoddcolHover');
});
$(document).on("mouseleave", "#transactionTable td:nth-child(1)" ,function (){
	 clean1();
});
var clean3 = function(){
	$('#transactionTable td:nth-child(3)').removeClass('txnoddcolHover');
}
$(document).on("mouseenter", "#transactionTable td:nth-child(3)" ,function (){
	clean3();
	$('#transactionTable td:nth-child('+($(this).index()+1)+')').addClass('txnoddcolHover');
});
$(document).on("mouseleave", "#transactionTable td:nth-child(3)" ,function (){
	clean3();
});
var clean5 = function(){
	$('#transactionTable td:nth-child(5)').removeClass('txnoddcolHover');
}
$(document).on("mouseenter", "#transactionTable td:nth-child(5)" ,function (){
	clean5();
	$('#transactionTable td:nth-child('+($(this).index()+1)+')').addClass('txnoddcolHover');
});
$(document).on("mouseleave", "#transactionTable td:nth-child(5)" ,function (){
	clean5();
});
var clean7 = function(){
	$('#transactionTable td:nth-child(7)').removeClass('txnoddcolHover');
}
$(document).on("mouseenter", "#transactionTable td:nth-child(7)" ,function (){
	clean7();
	$('#transactionTable td:nth-child('+($(this).index()+1)+')').addClass('txnoddcolHover');
});
$(document).on("mouseleave", "#transactionTable td:nth-child(7)" ,function (){
	clean7();
});
var clean9 = function(){
	$('#transactionTable td:nth-child(9)').removeClass('txnoddcolHover');
}
$(document).on("mouseenter", "#transactionTable td:nth-child(9)" ,function (){
	clean9();
	$('#transactionTable td:nth-child('+($(this).index()+1)+')').addClass('txnoddcolHover');
});
$(document).on("mouseleave", "#transactionTable td:nth-child(9)" ,function (){
	clean9();
});

//for all excel table form
//for odd transaction columns
var clean11 = function(){
	$('.transactionDetailsTable table[class*="excelFormTable"] td:nth-child(1)').removeClass('tableColHover');
}
$(document).on("mouseenter", ".transactionDetailsTable table[class*='excelFormTable'] td:nth-child(1)" ,function (){
	clean11();
	$('.transactionDetailsTable table[class*="excelFormTable"] td:nth-child('+($(this).index()+1)+')').addClass('tableColHover');
});
$(document).on("mouseleave", ".transactionDetailsTable table[class*='excelFormTable'] td:nth-child(1)" ,function (){
	 clean11();
});
var clean22 = function(){
	$('.transactionDetailsTable table[class*="excelFormTable"] td:nth-child(2)').removeClass('tableColHover');
}
$(document).on("mouseenter", ".transactionDetailsTable table[class*='excelFormTable'] td:nth-child(2)" ,function (){
	clean22();
	$('.transactionDetailsTable table[class*="excelFormTable"] td:nth-child('+($(this).index()+1)+')').addClass('tableColHover');
});
$(document).on("mouseleave", ".transactionDetailsTable table[class*='excelFormTable'] td:nth-child(2)" ,function (){
	 clean22();
});
var clean33 = function(){
	$('.transactionDetailsTable table[class*="excelFormTable"] td:nth-child(3)').removeClass('tableColHover');
}
$(document).on("mouseenter", ".transactionDetailsTable table[class*='excelFormTable'] td:nth-child(3)" ,function (){
	clean33();
	$('.transactionDetailsTable table[class*="excelFormTable"] td:nth-child('+($(this).index()+1)+')').addClass('tableColHover');
});
$(document).on("mouseleave", ".transactionDetailsTable table[class*='excelFormTable'] td:nth-child(3)" ,function (){
	 clean33();
});
var clean44 = function(){
	$('.transactionDetailsTable table[class*="excelFormTable"] td:nth-child(4)').removeClass('tableColHover');
}
$(document).on("mouseenter", ".transactionDetailsTable table[class*='excelFormTable'] td:nth-child(4)" ,function (){
	clean44();
	$('.transactionDetailsTable table[class*="excelFormTable"] td:nth-child('+($(this).index()+1)+')').addClass('tableColHover');
});
$(document).on("mouseleave", ".transactionDetailsTable table[class*='excelFormTable'] td:nth-child(4)" ,function (){
	 clean44();
});
var clean55 = function(){
	$('.transactionDetailsTable table[class*="excelFormTable"] td:nth-child(5)').removeClass('tableColHover');
}
$(document).on("mouseenter", ".transactionDetailsTable table[class*='excelFormTable'] td:nth-child(5)" ,function (){
	clean55();
	$('.transactionDetailsTable table[class*="excelFormTable"] td:nth-child('+($(this).index()+1)+')').addClass('tableColHover');
});
$(document).on("mouseleave", ".transactionDetailsTable table[class*='excelFormTable'] td:nth-child(5)" ,function (){
	 clean55();
});
var clean66 = function(){
	$('.transactionDetailsTable table[class*="excelFormTable"] td:nth-child(6)').removeClass('tableColHover');
}
$(document).on("mouseenter", ".transactionDetailsTable table[class*='excelFormTable'] td:nth-child(6)" ,function (){
	clean66();
	$('.transactionDetailsTable table[class*="excelFormTable"] td:nth-child('+($(this).index()+1)+')').addClass('tableColHover');
});
$(document).on("mouseleave", ".transactionDetailsTable table[class*='excelFormTable'] td:nth-child(6)" ,function (){
	 clean66();
});
var clean77 = function(){
	$('.transactionDetailsTable table[class*="excelFormTable"] td:nth-child(7)').removeClass('tableColHover');
}
$(document).on("mouseenter", ".transactionDetailsTable table[class*='excelFormTable'] td:nth-child(7)" ,function (){
	clean77();
	$('.transactionDetailsTable table[class*="excelFormTable"] td:nth-child('+($(this).index()+1)+')').addClass('tableColHover');
});
$(document).on("mouseleave", ".transactionDetailsTable table[class*='excelFormTable'] td:nth-child(7)" ,function (){
	 clean77();
});
var clean88 = function(){
	$('.transactionDetailsTable table[class*="excelFormTable"] td:nth-child(8)').removeClass('tableColHover');
}
$(document).on("mouseenter", ".transactionDetailsTable table[class*='excelFormTable'] td:nth-child(8)" ,function (){
	clean88();
	$('.transactionDetailsTable table[class*="excelFormTable"] td:nth-child('+($(this).index()+1)+')').addClass('tableColHover');
});
$(document).on("mouseleave", ".transactionDetailsTable table[class*='excelFormTable'] td:nth-child(8)" ,function (){
	 clean88();
});
var clean99 = function(){
	$('.transactionDetailsTable table[class*="excelFormTable"] td:nth-child(9)').removeClass('tableColHover');
}
$(document).on("mouseenter", ".transactionDetailsTable table[class*='excelFormTable'] td:nth-child(9)" ,function (){
	clean99();
	$('.transactionDetailsTable table[class*="excelFormTable"] td:nth-child('+($(this).index()+1)+')').addClass('tableColHover');
});
$(document).on("mouseleave", ".transactionDetailsTable table[class*='excelFormTable'] td:nth-child(9)" ,function (){
	 clean99();
});
//end for all excel table form

//for even transaction columns
var clean2 = function(){
	$('#transactionTable td:nth-child(2)').removeClass('txnevencolHover');
}
$(document).on("mouseenter", "#transactionTable td:nth-child(2)" ,function (){
	clean2();
	$('#transactionTable td:nth-child('+($(this).index()+1)+')').addClass('txnevencolHover');
});
$(document).on("mouseleave", "#transactionTable td:nth-child(2)" ,function (){
	 clean2();
});

var clean4 = function(){
	$('#transactionTable td:nth-child(4)').removeClass('txnevencolHover');
}
$(document).on("mouseenter", "#transactionTable td:nth-child(4)" ,function (){
	clean4();
	$('#transactionTable td:nth-child('+($(this).index()+1)+')').addClass('txnevencolHover');
});
$(document).on("mouseleave", "#transactionTable td:nth-child(4)" ,function (){
	clean4();
});

var clean6 = function(){
	$('#transactionTable td:nth-child(6)').removeClass('txnevencolHover');
}
$(document).on("mouseenter", "#transactionTable td:nth-child(6)" ,function (){
	clean6();
	$('#transactionTable td:nth-child('+($(this).index()+1)+')').addClass('txnevencolHover');
});
$(document).on("mouseleave", "#transactionTable td:nth-child(6)" ,function (){
	clean6();
});

var clean8 = function(){
	$('#transactionTable td:nth-child(8)').removeClass('txnevencolHover');
}
$(document).on("mouseenter", "#transactionTable td:nth-child(8)" ,function (){
	clean8();
	$('#transactionTable td:nth-child('+($(this).index()+1)+')').addClass('txnevencolHover');
});
$(document).on("mouseleave", "#transactionTable td:nth-child(8)" ,function (){
	clean8();
});

var clean10 = function(){
	$('#transactionTable td:nth-child(10)').removeClass('txnevencolHover');
}
$(document).on("mouseenter", "#transactionTable td:nth-child(10)" ,function (){
	clean10();
	$('#transactionTable td:nth-child('+($(this).index()+1)+')').addClass('txnevencolHover');
});
$(document).on("mouseleave", "#transactionTable td:nth-child(10)" ,function (){
	clean10();
});


function transcribe(elem) {
	var parentTr=$(elem).parent().parent().parent().attr('id');
	var words=$(elem).val();
	$("#"+parentTr+" textarea[class='voiceRemarksClass']").val(words);
	$("#"+parentTr+" input[id='mic']").val("");
	$("#"+parentTr+" textarea[class='voiceRemarksClass']").focus();
}

$(document).ready(function(){
	$('#transferAmountFromMainToPettyCashButton').click(function(){
		var mainToPettyAmount=$("#amountFromMainToPetty").val();
		var transferSuppDocs=$("#cashTransferDocs").val();
		var resultantCashTotal=$("#resultantCashTotal").val();
		var approverUser=$(".approverControllerUserList option:selected").val();
		var approverUserEmailText=$(".approverControllerUserList option:selected").text();
		if(mainToPettyAmount==""){
			swal("Invalid data!","Please provide amount to be transferred from main to petty cash account.","error");
			return true;
		}
		if(resultantCashTotal!="" && mainToPettyAmount!=""){
			var resCashAmount=parseFloat(resultantCashTotal);
			var mainToPettyAmt=parseFloat(mainToPettyAmount);
			if(mainToPettyAmt>resCashAmount){
				swal("Invalid data!","Amount transferred from main to petty cash account cannot be greater than the cash amount available in the account","error");
				return true;
			}
		}
		if(approverUser==""){
			swal("Invalid data!","Please select the approver to approve petty cash transfer from main cash account.","error");
			return true;
		}
		var jsonData = {};
		jsonData.branchCashCountEntityId=$("#branchCahCountId").val();
		jsonData.resultantCashBranchCashAccount=resultantCashTotal;
		jsonData.cashMainActToPettyAccount=mainToPettyAmount;
		jsonData.cashTransferSuppDocs=transferSuppDocs;
		jsonData.approverUserEmail=approverUserEmailText;
		jsonData.useremail=$("#hiddenuseremail").text();
		var url = "/cash/transferMainToPetty";
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
				 $("#amountFromMainToPetty").val("");
				 $("#cashTransferDocs").val("");
				 if(data.pettyCashTransferResultData[0].pettyCashTransferStat=='Success'){
					 $("#resultantCashTotal").val(data.pettyCashTransferResultData[0].resultantCash);
					 $("#mainToPettyTotal").val(data.pettyCashTransferResultData[0].totalMainToPetty);
					 $("#resultantPettyCash").val(data.pettyCashTransferResultData[0].resultantPettyCash);
					 $("#debittedPettyCashTotal").val(data.pettyCashTransferResultData[0].debbittedPettyCash);
					 swal("Success!!","Successfully transferred main cash amount from branch main cash account to petty cash account.","success");
				 }
				 if(data.pettyCashTransferResultData[0].pettyCashTransferStat=='Failure'){
					 swal("Error!","Oops fish, cannot proceed with the transaction try again.","error");
					 return true;
				 }
			},
			error: function(xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
    });
});

function branchPettyTxnApprovalRule(elem){
	var optValue=$(elem).val();
	if(optValue==1){
		$(".approvalAboveLimit").show();
		$(".approvalAboveLimit").text("Approval Limit");
		$("#approvalAboveLimit").show();
		$("#approvalAboveLimit").val("");
	}
	if(optValue==0){
		$(".approvalAboveLimit").hide();
		$(".approvalAboveLimit").text("");
		$("#approvalAboveLimit").hide();
		$("#approvalAboveLimit").val("");
	}
}

$(document).ready(function() {
	$('.company-nav li a').click(function() {
		$('.mainContent').hide();
		var divRef=this.href;
		var n=divRef.lastIndexOf("#");
		var str=divRef.substring(n, divRef.length);
		$(str).show();
		alwaysScrollTop();
	});
});

//recoincillation of branch cash account
$(document).ready(function() {
	$('.recoincileCashAccountButton').click(function() {
		var jsonData = {};
		 var useremail=$("#hiddenuseremail").text();
		 jsonData.usermail = useremail;
		 var url="/cashier/recoincileCashAccount";
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
				 $("#branchCahCountId").val("");
		    	 $("#branchCahCountId").val(data.cashierInformationData[0].branchCashCountId);
		    	 $("#cashierInstruction").val(data.cashierInformationData[0].cashierKl);
		    	 $("#cashCreditedTotal").val(data.cashierInformationData[0].cashCreditedTotal);
		    	 $("#cashDebitedTotal").val(data.cashierInformationData[0].cashDebitedTotal);
		    	 $("#resultantCashTotal").val(data.cashierInformationData[0].resultantCashTotal);
		    	 $("#prevNotesTotal").val(data.cashierInformationData[0].notesTotal);
		    	 $("#prevCoinsTotal").val(data.cashierInformationData[0].coinsTotal);
		    	 $("#prevSmallerCoinsTotal").val(data.cashierInformationData[0].smallerCoinsTotal);
		    	 $("#prevGrandTotal").val(data.cashierInformationData[0].grandTotal);
		    	 $("#mainToPettyTotal").val(data.cashierInformationData[0].mainToPettyTotal);
		    	 $("#resultantPettyCash").val(data.cashierInformationData[0].resultantPettyCash);
		    	 $("#debittedPettyCashTotal").val(data.cashierInformationData[0].debbitedPettyCash);
		    	 swal("Success","Successfully Recoinciled Branch cash account with the transactions.","success");
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		 });
	});
});

//recoincillation of individual branch bank account balance
function recoincileBranchBankAccount(elem){
	var parentTr=$(elem).parent().parent().attr('id');
	var jsonData = {};
	var useremail=$("#hiddenuseremail").text();
	jsonData.usermail = useremail;
	jsonData.bnchBankActId=$("#"+parentTr+" input[id='bankId']").val();
	var url="/cashier/recoincileBankAccountBalance";
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
			$("#"+parentTr+" input[id='bankAccountBalance"+data.cashierBankInformationData[0].bankActId+"']").val(data.cashierBankInformationData[0].lastRecordedAmountEntered);
			$("#"+parentTr+" input[id='bankTotlCredit"+data.cashierBankInformationData[0].bankActId+"']").val(data.cashierBankInformationData[0].totalCredit);
			$("#"+parentTr+" input[id='bankTotalDebit"+data.cashierBankInformationData[0].bankActId+"']").val(data.cashierBankInformationData[0].totalDebit);
			$("#"+parentTr+" input[id='bankResultantBalance"+data.cashierBankInformationData[0].bankActId+"']").val(data.cashierBankInformationData[0].resultantBalanceInAccount);
			idosalert.show('Successfully Recoinciled Branch bank account amount with the transactions.', 'alert_warning');
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

//detect scrollbar for the html body or any div
$(document).ready(function(){
var mousewheelevt = (/Firefox/i.test(navigator.userAgent)) ? "DOMMouseScroll" : "mousewheel" //FF doesn't recognize mousewheel as of FF3.x
	$("html, body").bind('mousewheel', function(e){
	    var evt = window.event || e //equalize event object
	    evt = evt.originalEvent ? evt.originalEvent : evt; //convert to originalEvent if possible
	    var delta = evt.detail ? evt.detail*(-40) : evt.wheelDelta //check for detail first, because it is used by Opera and FF
	    if(delta > 0) {
	    }
	    else{
	    }
	});
});

function showCustVend(elem){
	var useremail=$("#hiddenuseremail").text();
	var modelFor=$(elem).attr('class');
    var branchID = $("#staticcashbankreceivablepayablebranchwisebreakup #staticBranchID").val();
	var jsonData = {};
	jsonData.bnchName = $(elem).attr('name');
	jsonData.branchID = branchID;
	jsonData.usermail = useremail;
	jsonData.txnModelFor=modelFor;
	ajaxCall('/user/branchCustomerVendorReceivablePayables', jsonData, '', '', '', '', 'displayCustVendSuccess', '', true);
}


function contactSupplierVendor(elem){
	var jsonData = {};
	var useremail=$("#hiddenuseremail").text();
	var vendorSupplierEmail=$(elem).parent().parent().attr('class');
	var minWAPitemName=$(".minWAPitemName").text();
	var minWAPLocation=$(".minWAPLocation").text();
	jsonData.usermail = useremail;
	jsonData.supplierVendorEmail=vendorSupplierEmail;
	jsonData.itemName=minWAPitemName;
	jsonData.location=minWAPLocation;
	var url="/ecommerce/contactSupplierVendor";
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
			if(data.supplierContact[0].successConnect='success'){
				swal("Success","You Had Successfully Contacted The Supplier","success");
				return true;
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

$(document).ready(function() {
	$('.operationalCalendar').fullCalendar({
		header: {
			left: 'prev',
			center: 'title',
			right: 'next'
		},
		editable: true
	});
});

$(document).ready(function() {
	$('body').on('click', '#projectSetupId', function() {
        handleProjectData();
    });

	$('body').on('click', '#projectFreeTextSearchButton', function() {
        var projectSearch = $.trim($(this).next('input[type=text]').val());
        handleProjectSearch(projectSearch);
    });

	function handleProjectSearch(searchText) {
        var jsonData = {
            usermail: $("#hiddenuseremail").text(),
            searchProject: searchText
        };

        $.ajax({
            url: '/project/searchProject',
            data: JSON.stringify(jsonData),
            async: false,
            headers: {
                "X-AUTH-TOKEN": window.authToken
            },
            method: "POST",
            contentType: 'application/json',
            success: function(data) {
                $('#projectFreeTextSearchButton').next('input[type=text]').val('');
                var projectlisttable = $("#projectTable tbody");
                projectlisttable.html('');
                for (var i = 0; i < data.projectListData.length; i++) {
                    projectlisttable.append('<tr name="projectEntity'+data.projectListData[i].id+'"><td>'+data.projectListData[i].name+'</td><td>'+data.projectListData[i].number+'</td><td>'+data.projectListData[i].startDate+'</td><td>'+data.projectListData[i].endDate+'</td><td>'+data.projectListData[i].location+'</td><td><button href="#projectSetup" class="btn btn-submit" onClick="showProjectEntityDetails(this)" id="show-entity-details'+data.projectListData[i].id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td><td><button href="#projectSetup" class="btn btn-submit" onClick="deactivateProjectEntityDetails(this)" id="deactivate-entity-details'+data.projectListData[i].id+'"><i class="far fa-trash-alt fa-lg pr-5"></i>'+data.projectListData[i].actionText+'</button></td></tr>');
                }
            }
        });
    }

	function handleProjectData() {
		var jsonData = {
            usermail: $("#hiddenuseremail").text(),
            searchProject:""
        };

        $.ajax({
            url: '/project/searchProject',
            data: JSON.stringify(jsonData),
            async: false,
            headers: {
                "X-AUTH-TOKEN": window.authToken
            },
            method: "POST",
            contentType: 'application/json',
            success: function(data) {
                var projectlisttable = $("#projectTable tbody");
                projectlisttable.html('');
				for (var i = 0; i < data.projectListData.length; i++) {
                    projectlisttable.append('<tr name="projectEntity'+data.projectListData[i].id+'"><td>'+data.projectListData[i].name+'</td><td>'+data.projectListData[i].number+'</td><td>'+data.projectListData[i].startDate+'</td><td>'+data.projectListData[i].endDate+'</td><td>'+data.projectListData[i].location+'</td><td><button href="#projectSetup" class="btn btn-submit" onClick="showProjectEntityDetails(this)" id="show-entity-details'+data.projectListData[i].id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td><td><button href="#projectSetup" class="btn btn-submit" onClick="deactivateProjectEntityDetails(this)" id="deactivate-entity-details'+data.projectListData[i].id+'"><i class="far fa-trash-alt fa-lg pr-5"></i>'+data.projectListData[i].actionText+'</button></td></tr>');
                }
            }
        });
    }

	
});


function allIdosEccomerceItems(){
	var jsonData = {};
	var url="/vendor/ecommerceItems";
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
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

function allIdosAvailableLocations(){
	var jsonData = {};
	var url="/vendor/availableLocations";
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
			$("#vendCustTransactions select[id='vendorsellerBranches']").children().remove();
			$("#vendSellerAccounts select[id='vendorsellerBranches']").children().remove();
			for(var i=0;i<data.ecommerceAllLocationData.length;i++){
				$("#vendCustTransactions select[id='vendorsellerBranches']").append('<option value="'+data.ecommerceAllLocationData[i].locationName+'">'+data.ecommerceAllLocationData[i].locationName+'</option>');
				$("#vendSellerAccounts select[id='vendorsellerBranches']").append('<option value="'+data.ecommerceAllLocationData[i].locationName+'">'+data.ecommerceAllLocationData[i].locationName+'</option>');
			}
//			for(var j=0;j<data.restLocationData.length;j++){
//				if(data.restLocationData[j].restlocationName!=""){
//					$("#vendCustTransactions select[id='vendorsellerBranches']").append('<option value="'+data.restLocationData[j].restlocationName+'">'+data.restLocationData[j].restlocationName+'</option>');
//					$("#vendSellerAccounts select[id='vendorsellerBranches']").append('<option value="'+data.restLocationData[j].restlocationName+'">'+data.restLocationData[j].restlocationName+'</option>');
//				}
//			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

function getDashboardAllFirstLocations(elem){
	var jsonData = {};
	var url="/dashboard/availableLocations";
	var value=$(elem).val();
	$("div[class='dashboardLocationFirstAutoSuggest']").html("");
	jsonData.enteredValue=value;
	if(value!=""){
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
				$('#dashboardAutoSuggestId,#dashboardLocationSecondAutoSuggest').empty();
				for(var i=0;i<data.ecommerceAllLocationData.length;i++){
					$("div[class='dashboardLocationFirstAutoSuggest']").append('<div id="childtext'+i+'" onClick="fillDashboardLocatioFirstText('+i+');" class="dashboardLocationFirstchildtext">'+data.ecommerceAllLocationData[i].locationName+'</div>');

					console.log('<div id="childtext'+i+'" onclick="javascript:fillDashboardLocatioFirstText('+i+');" class="dashboardLocationFirstchildtext">'+data.ecommerceAllLocationData[i].locationName+'</div>');

				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	}
}

function fillDashboardLocatioFirstText(datas){

	var textvalue=$("#childtext"+datas+"").text();
	$("input[name='aggregateDataCompareFirstLocation']").val(textvalue);
	$("div[class='dashboardLocationFirstAutoSuggest']").html('');
}

function getDashboardAllSecondLocations(elem){
	var jsonData = {};
	var url="/dashboard/availableLocations";
	var value=$(elem).val();
	$("div[class='dashboardLocationSecondAutoSuggest']").html("");
	jsonData.enteredValue=value;
	if(value!=""){
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
				$('#dashboardAutoSuggestId,#dashboardLocationFirstAutoSuggest').empty();
				for(var i=0;i<data.ecommerceAllLocationData.length;i++){
					$("div[class='dashboardLocationSecondAutoSuggest']").append('<div id="childtext'+i+'" onClick="fillDashboardLocatioSecondText("'+i+'");" class="dashboardLocationSecondchildtext">'+data.ecommerceAllLocationData[i].locationName+'</div>');
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	}
}


function fillDashboardLocatioSecondText(datas){
	var textvalue=$("#childtext"+datas+"").text();
	$("input[name='aggregateDataCompareSecondLocation']").val(textvalue);
	$("div[class='dashboardLocationSecondAutoSuggest']").html('');
}

function keyUp(event, invokeFunction) {
	var key = event.which ? event.which : event.keyCode;
	if (13 === key) {
		window[invokeFunction](null);
	}
}

function isEmpty(check) {
	if (undefined === check || '' === check || null === check || typeof check === 'undefined') {
		return true;
	} else {
		return false;
	}
}

function ajaxCall(url, jsonData, type, async, method, contentType, successFunction, errorFunction, blockUI) {
	if (isEmpty(type)) {
		type = 'text';
	}
	if (isEmpty(async)) {
		async = true;
	}
	if (isEmpty(method)) {
		method = 'POST';
	}
	if (isEmpty(contentType)) {
		contentType = 'application/json';
	}
	if (!isEmpty(url)) {
		if (blockUI) {
			//$.blockUI({ message: '<i class="fa fa-spinner fa-pulse fa-5x" style="color: #006DCC;"></i>' });
			$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		}
		$.ajax({
			url: url,
			data: JSON.stringify(jsonData),
			type: type,
			async: async,
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method: method,
			contentType: contentType,
			success: function (data) {
				blockUI ? $.unblockUI() : false;
				window[successFunction](data);
			},
			error: function (jqXHR, status, error) {
				blockUI ? $.unblockUI() : false;
				//window[errorFunction]();
				if(jqXHR.status == 401){
					var token = window.authToken;
					if(typeof token !== 'undefined' && token.indexOf('se11') == 0){
						return doSellerLogout();
					}else{
						return doLogout();
					}
				}else if(jqXHR.status == 500){
                    window[errorFunction](data);
				}
			}
		});
	}
}
/*Utils - Ends*/

/*Seller SignUp and Registration - Starts*/
function sellerLogin(user,pass) {
	swal("Error!","sellerLogin","error");
	if ('' !== user && '' !== pass) {
		var jsonData = {};
		jsonData.email = user;
		jsonData.password = pass;
		ajaxCall('/seller', jsonData, '', '', '', '', 'sellerLoginSuccess', '', true);
	}
}

function sellerLoginSuccess(data) {
	data = data.toLowerCase();
	if ('success' === data) {
		idosalert.show('Found Seller/Vendor.', 'alert_warning');
	} else if ('failure' === data) {
		idosalert.show('No Vendor/Seller Found.', 'alert_warning');
	} else if ('activate' === data) {
		idosalert.show('Please activate your account.', 'alert_warning');
	}
}

function sellerSignUpSuccess(data) {
	data = data.toLowerCase();
	if ('success' === data) {
		$("#successsignupregdiv span").append('Welcome, You are successfully registered in Idos.Please activate you account following activation link sent to your email.');
		notifyLogin.show('Welcome, You are successfully registered in Idos.Please activate you account following activation link sent to your email.',true,false);
	} else if ('failure' === data) {
		$("#successsignupregdiv span").append('Problem in registering your account. Please try agian later.');
		notifyLogin.show('Problem in registering your account. Please try agian later.',true,true);
	}
	$('#sellerAccountname').val("");
	$('#sellerAccountemail').val("");
	$('#sellerAccountpwd').val("");
	$("#successsignupregdiv").fadeIn();
	alwaysScrollTop();
}

function isSellerAlreadyRegistered(email) {
	var locHash=window.location.hash;
	if(locHash != 'sellerAccountRegDiv'){
		return false;
	}
	email = email ? email : $.trim($('#sellerAccountemail').val());
	var goodEmail = email.match(/\b(^(\S+@).+((\.com)|(\.net)|(\.edu)|(\.mil)|(\.gov)|(\.org)|(\.info)|(\.sex)|(\.biz)|(\.aero)|(\.coop)|(\.museum)|(\.name)|(\.pro)|(\.arpa)|(\.asia)|(\.cat)|(\.int)|(\.jobs)|(\.tel)|(\.travel)|(\.xxx)|(\..{2,2}))$)\b/gi);
    var apos=email.indexOf("@"), dotpos = email.lastIndexOf("."), lastpos=email.length-1;
    var badEmail = (apos<1 || dotpos-apos<2 || lastpos-dotpos<2);
	if ('' !== email && goodEmail && !badEmail) {
		var jsonData = {};
		jsonData.email = email;
		ajaxCall('/checkSellerEmail', jsonData, '', '', '', '', 'sellerEmailCheckSuccess', '', false);
	}else {
		notifyLogin.show('Please provide a valid email id.',true,true);
	}
}

function sellerEmailCheckSuccess(data) {
	if ('failure' === data) {
		notifyLogin.show('Email ID Already Registered.',true,true);
		$('#sellerAccountemail').val('').focus();
	}
	alwaysScrollTop();
}

/*Seller SignUp and Registration - Ends*/
function showFeedback(data) {
	var areaCodesLen = $('#feedbackAreaCodes').children().length;
	var hiddenEmail = $("#hiddenuseremail").text();
	if ('' === hiddenEmail || undefined === hiddenEmail) {
		$('#feedbackDiv').css('display', 'block');
		$('#feedbackModal').css('top', '7%');
    } else {
    	$('#feedbackDiv').css('display', 'none');
    	$('#feedbackModal').css('top', '10%');
    }
	if (undefined === data) {
		if (areaCodesLen <= 1) {
			ajaxCall('/feedback/getAreaCodes', '', '', '', '', '', 'showFeedback', '', true);
		}
		$("#feedbackModal").attr('data-toggle', 'modal');
	    $("#feedbackModal").modal('show');
	    $("#feedbackModal a[id='fancybox-close']").attr("href",location.hash);
	} else {
		if (areaCodesLen <= 1) {
			if (areaCodesLen < 1) {
				$('#feedbackAreaCodes').append('---Please Select---');
			}
			data = data.result;
			$(data).each(function(i) {
				$('#feedbackAreaCodes').append('<option value="' + data[i].areaCode + '">' + data[i].countryCode + '</option>');
			});
		}
	}
}

function getCountryPhoneCode(data){
	var areaCodesLen = $('#vendorsellercountryPhnCode').children().length;
	if (undefined === data) {
		if (areaCodesLen <= 1) {
			ajaxCall('/feedback/getAreaCodes', '', '', false, '', '', 'getCountryPhoneCode', '', true);
		}
	} else {
		if (areaCodesLen <= 1) {
			if (areaCodesLen < 1) {
				$('#vendorsellercountryPhnCode').append('---Please Select---');
			}
			data = data.result;
			$(data).each(function(i) {
				$('#vendorsellercountryPhnCode').append('<option value="' + data[i].areaCode + '">' + data[i].countryCode + '</option>');
			});
		}
	}
}

function hideFeedback() {
	$("div[class='modal-backdrop fade in']").remove();
	$("#feedbackModal").attr('data-toggle', 'modal');
	$("#feedbackModal").modal('hide');
}

function feedbackSubmit(data) {
	if (undefined === data) {
		var jsonData = {};
		var email, number = $('#feedbackAreaCodes').val(),
		name = $.trim($('#feedbackName').val()),
		subject = $.trim($('#feedbackSubject').val()),
		text = $.trim($('#feedbackText').val());
		if ('' !== number) {
			for (var i = 1; i <= 3; i++) {
				number += $('#feedbackPhoneNumber' + i).val();
			}
		}
		email = $("#hiddenuseremail").text();
		if ('' === email || undefined === email) {
			email = $.trim($('#feedbackEmail').val());
		}
		if ('' !== subject && '' !== text && '' !== email && '' !== name) {
			jsonData.email = email;
			jsonData.name = name;
			jsonData.number = number;
			jsonData.subject = subject;
			jsonData.text = text;
			ajaxCall('/feedback/submit', jsonData, '', '', '', '', 'feedbackSubmit', '', true);
		} else if ('' === subject) {
			idosalert.show('Please Provide the Subject.', 'alert_warning');
		} else if ('' === text) {
			idosalert.show('Please Provide the Text.', 'alert_warning');
		} else if ('' === email) {
			if ($('#feedbackDiv').is(':visible')) {
				idosalert.show('Please Provide the Email.', 'alert_warning');
			}
		} else if ('' === name) {
			idosalert.show('Please Provide the Text.', 'alert_warning');
		}
	} else {
		data = data.toLowerCase();
		hideFeedback();
		/*if ('success' === data) {
			idosalert.show('Please Provide the Text.', 'alert_warning');
		} else if ('failure' === data) {
			idosalert.show('Please Provide the Text.', 'alert_warning');
		}*/
	}
}

function feedbackAreaCodeChanged(value) {
	if ('' !== value) {
		for (var i = 1; i <= 3; i++) {
			$('#feedbackPhoneNumber' + i).removeAttr('disabled');
		}
	}
}
/*Feedback Ends*/
/*idos custom alert dialog */
var idosalert = {
	show : function (msg, className) {
		$('body').find('#fullOverlay, #alertBox').remove();
		if (undefined === className || null === className) {
			className = '';
		}
		var alertHTML = '<div id="fullOverlay" onclick="idosalert.hide();"></div>'
					+ '<div id="alertBox">'
					+ '<div class="' + className + ' adjust_left"></div>'
					+ '<div id="alertMessage">' + msg + '</div>'
					+ '<div id="alertButton" onclick="idosalert.hide();">OK</div>'
					+ '</div>';
		$('body').append(alertHTML);
		$('#fullOverlay, #alertBox').slideDown();
	},
	hide : function() {
		$('body').find('#fullOverlay, #alertBox').slideUp();
		setTimeout(function() {
			$('body').find('#fullOverlay, #alertBox').remove();
		}, 1000);
	}
};
/*idos custom alert dialog */

//function to get data in autosuggest div
function autoSuggestAvailableItems(elem){
	var jsonData = {};
	var parentTr=$(elem).parent().parent().attr('class');
	$("."+parentTr+" div[class='parentAutoSuggest']").html('');
	var value=$(elem).val();
	if(value!=""){
		var fillElem=$(elem);
		jsonData.enteredValue = value;
		var url="/ecommerce/populateAllPossibleItems";
		$.ajax({
			url: url,
			data: JSON.stringify(jsonData),
			type:"text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method:"POST",
			contentType:'application/json',
			success: function (data) {
				for(var i=0;i<data.ecommerceAvailableItemData.length;i++){
					$("."+parentTr+" div[class='parentAutoSuggest']").append('<div id="childtext'+i+'" onclick="fillText('+i+')" class="childtext">'+data.ecommerceAvailableItemData[i].itemName+'</div>');
				}
				$("."+parentTr+" input[name='idosRegVendSellerHiddenId']").val("");
				$("."+parentTr+" textarea[name='vendorselleritemdescription']").val("");
				$("."+parentTr+" select[name='vendorsellerBranches'] option:selected").each(function () {
					$(this).removeAttr('selected');
				});
				$("."+parentTr+" select[name='vendorsellerBranches']").multiselect('rebuild');
				$("."+parentTr+" input[name='vendorsellerretailerprice']").val("");
				$("."+parentTr+" input[name='vendorsellerwholesellerprice']").val("");
				$("."+parentTr+" input[name='vendorsellerspecialofferedprice']").val("");
				$("."+parentTr+" textarea[name='vendorsellerspecialofferedpricerequirements']").val("");
			},
			error: function (jqXHR, status, error) {
				if(jqXHR.status == 401){ doLogout(); }
			}
		});
	}else{
		$("."+parentTr+" input[name='idosRegVendSellerHiddenId']").val("");
		$("."+parentTr+" textarea[name='vendorselleritemdescription']").val("");
		$("."+parentTr+" select[name='vendorsellerBranches'] option:selected").each(function () {
			$(this).removeAttr('selected');
		});
		$("."+parentTr+" select[name='vendorsellerBranches']").multiselect('rebuild');
		$("."+parentTr+" input[name='vendorsellerretailerprice']").val("");
		$("."+parentTr+" input[name='vendorsellerwholesellerprice']").val("");
		$("."+parentTr+" input[name='vendorsellerspecialofferedprice']").val("");
		$("."+parentTr+" textarea[name='vendorsellerspecialofferedpricerequirements']").val("");
	}
}

function fillText(datas){
	var textvalue=$("#childtext"+datas+"").text();
	var jsonData = {};
	var parentTr="newSupplierVendorItemsRegister";
	jsonData.supplierEmail=$("#vendorAccountEmail").val();
	jsonData.selectedTextValue = textvalue;
	var url="/ecommerce/itemDataExist";
	$.ajax({
		url: url,
		data: JSON.stringify(jsonData),
		type:"text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method:"POST",
		contentType:'application/json',
		success: function (data) {
			if(data.itemDataExist[0].exist=="Already Entered Item Pricings For the supplier"){
				//populate fields with the existing supplier data
				$("."+parentTr+" select[name='vendorsellerBranches'] option:selected").each(function () {
					$(this).removeAttr('selected');
				});
				$("."+parentTr+" div[class='parentAutoSuggest']").html('');
				$("."+parentTr+" input[name='idosRegVendSellerHiddenId']").val(data.itemDataExist[0].primId);
				$("."+parentTr+" input[name='supplierVendorItem']").val(data.itemDataExist[0].itemName);
				$("."+parentTr+" textarea[name='vendorselleritemdescription']").val(data.itemDataExist[0].itemDescription);
				var itemExistBranches=data.itemDataExist[0].itemSuppliedInBranches;
				var itemExistBnchsArr=itemExistBranches.split(",");
				for(var i=0;i<itemExistBnchsArr.length;i++){
					$("."+parentTr+" select[name='vendorsellerBranches'] option").filter(function () {return $(this).val()==itemExistBnchsArr[i];}).prop("selected", "selected");
				}
				$("."+parentTr+" select[name='vendorsellerBranches']").multiselect('rebuild');
				$("."+parentTr+" input[name='vendorsellerretailerprice']").val(data.itemDataExist[0].retailerPrice);
				$("."+parentTr+" input[name='vendorsellerwholesellerprice']").val(data.itemDataExist[0].wholeSellerPrice);
				$("."+parentTr+" input[name='vendorsellerspecialofferedprice']").val(data.itemDataExist[0].specialPrice);
				$("."+parentTr+" textarea[name='vendorsellerspecialofferedpricerequirements']").val(data.itemDataExist[0].specialPriceRequirements);
			}
			if(data.itemDataExist[0].exist=="Not Exist"){
				$("."+parentTr+" input[name='supplierVendorItem']").val(textvalue);
				$("."+parentTr+" div[class='parentAutoSuggest']").html('');
				$("."+parentTr+" input[name='idosRegVendSellerHiddenId']").val("");
				$("."+parentTr+" textarea[name='vendorselleritemdescription']").val("");
				$("."+parentTr+" select[name='vendorsellerBranches'] option:selected").each(function () {
					$(this).removeAttr('selected');
				});
				$("."+parentTr+" select[name='vendorsellerBranches']").multiselect('rebuild');
				$("."+parentTr+" input[name='vendorsellerretailerprice']").val("");
				$("."+parentTr+" input[name='vendorsellerwholesellerprice']").val("");
				$("."+parentTr+" input[name='vendorsellerspecialofferedprice']").val("");
				$("."+parentTr+" textarea[name='vendorsellerspecialofferedpricerequirements']").val("");
			}
		},
		error: function (jqXHR, status, error) {
			if(jqXHR.status == 401){ doLogout(); }
		}
	});
}

//ends

$(document).ready(function() {
	$('.compareAggregateData').click(function() {
		var itemname=$("#aggregateDataCompareInput").val();
		var period=$("select[name='aggregateDataPeriod'] option:selected").val();
		var locationfirst=$("#aggregateDataCompareFirstLocation").val();
		var locationsecond=$("#aggregateDataCompareSecondLocation").val();
		if(itemname=="" || locationfirst=="" || locationsecond==""){
			swal("Insufficient Data!!","please provide item and location in which you want to compare aggregate data.","error");
			return true;
		}
		var jsonData = {};
		 var useremail=$("#hiddenuseremail").text();
		 jsonData.usermail = useremail;
		 jsonData.entereditemname=itemname;
		 jsonData.enteredlocationfirst=locationfirst;
		 jsonData.enteredlocationsecond=locationsecond;
		 jsonData.enteredPeriod=period;
		 var url="/dashboard/plotbranchAggregateData";
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
				$("#dashboardAutoSuggestId").hide();
				$("#dashboardLocationFirstAutoSuggest").hide();
				$("#dashboardLocationSecondAutoSuggest").hide();
				$("#staticdashboardaggregatedata").attr('data-toggle', 'modal');
		    	$("#staticdashboardaggregatedata").modal('show');
		    	//$(".staticdashboardaggregatedataclose").attr("href",location.hash);
		    	$("#staticdashboardaggregatedata div[class='modal-body']").html("");
		    	$("#staticdashboardaggregatedata div[class='modal-body']").html('<table class="table table-hover table-striped table-bordered excelFormTable transaction-create" id="locationsItemAggregateDataTable" style="margin-top: 0px; width:613px;">'+
	    		'<thead class="tablehead1"><th>First Location</th><th>Second Location</th><tr></thead><tbody></tbody></table><div id="aggregateDataPieChartrPlot"></div>');
		    	$("#staticdashboardaggregatedata h4[class='aggregateDataSpan']").html("");
		    	$("#staticdashboardaggregatedata h4[class='aggregateDataSpan']").html("Aggregate Data for Item:"+data.dashboardAggregateData[0].itemName);
		    	$("#staticdashboardaggregatedata div[class='modal-body'] table[id='locationsItemAggregateDataTable'] tbody").append('<tr><td>'+data.dashboardAggregateData[0].firstLocation+'</td><td>'+data.dashboardAggregateData[0].enteredlocationsecond+'</td><tr>');
		    	$("#staticdashboardaggregatedata div[class='modal-body'] table[id='locationsItemAggregateDataTable'] tbody").append('<tr><td>'+data.dashboardAggregateData[0].firstBranchNetAmount+'</td><td>'+data.dashboardAggregateData[0].secondBranchNetAmount+'</td><tr>');
		    	var s1 = [[''+data.dashboardAggregateData[0].firstLocation+'',data.dashboardAggregateData[0].firstBranchNetAmount], [''+data.dashboardAggregateData[0].enteredlocationsecond+'',data.dashboardAggregateData[0].secondBranchNetAmount]];
		    	setTimeout(function(){
			    	var plot8 = $.jqplot('aggregateDataPieChartrPlot', [s1], {
			            grid: {
			                drawBorder: false,
			                drawGridlines: false,
			                background: '#ffffff',
			                shadow:false
			            },
			            axesDefaults: {
			            },
			            seriesDefaults:{
			                renderer:$.jqplot.PieRenderer,
			                rendererOptions: {
			                    showDataLabels: true
			                }
			            },
			            legend: {
			                show: true,
			                rendererOptions: {
			                    numberRows: 1
			                },
			                location: 's'
			            }
			        });
		    	},2000);
		    	$("#aggregateDataCompareInput").val("");
		    	$("select[name='aggregateDataPeriod'] option:first").prop("selected","selected");
		    	$("#aggregateDataCompareFirstLocation").val("");
		    	$("#aggregateDataCompareSecondLocation").val("");
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		 });
	});
});


function prependMatchingItemFirstInCustomContainer(elem){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
	var searchInputId=$(elem).attr('id');
	var searchInputValue=$(elem).val();
	var $allChildreanData="";
	var $labelHtml="";
	var $selectAllHtml="";
	var $matchedLiFromContainer="";
	var $leftUnmatchedLiFromContainer="";
	var $ulcontainer=$(elem).parent().parent().find('ul').attr('id');
	var $licontainer=$("#"+$ulcontainer+" li:nth-child(2)").attr('class');
	var $firstInputFieldName=$("#"+$ulcontainer+" li[class='"+$licontainer+"']:nth-child(3) input[type='text']:nth-child(3)").attr('name');
	var $secondInputFieldName=$("#"+$ulcontainer+" li[class='"+$licontainer+"']:nth-child(3) input[type='text']:nth-child(4)").attr('name');
	var $spanClass=$("#"+$ulcontainer+" li[class='"+$licontainer+"']:nth-child(3) span").attr('class');

	if(searchInputId=="creditAccountHeadscontentsearchinput"){
		var journalArray=new Array();
		$("#creditAccountHeadsList input[name='creditaccountHeadsRadio']").each(function(){
			var thisId=$(this).attr('id');
			var checkBoxValue=$(this).val();
			var checkBoxChecked=$("#creditAccountHeadsList input[value='"+checkBoxValue+"']").is(':checked');
			var amount = $("#creditAccountHeadsList input[item='"+checkBoxValue+"']").val();
			var arrayVal=checkBoxValue+":"+checkBoxChecked+":"+amount;
			journalArray.push(arrayVal);
		});
		if(searchInputValue!=""){
			$("#creditAccountHeadsList li[id='creditaccountheadlist'] b[id='creditaccountHeadsLabelId']:containsCI("+searchInputValue+")").each(function(){
				$matchedLiFromContainer+=$(this).parent()[0].outerHTML;
			});
			$("#creditAccountHeadsList li[id='creditaccountheadlist'] b[id='creditaccountHeadsLabelId']:not(:containsCI("+searchInputValue+"))").each(function(){
				$leftUnmatchedLiFromContainer+=$(this).parent()[0].outerHTML;
			});
			$("#creditAccountHeadsList").empty();
			var combinedHTML=$matchedLiFromContainer+$leftUnmatchedLiFromContainer;
			$("#creditAccountHeadsList").html(combinedHTML);
		}else{
			$("#creditAccountHeadsList li[id='creditaccountheadlist']").each(function(){
				$allChildreanData+=$(this)[0].outerHTML;
			});
			$("#creditAccountHeadsList").empty();
			$("#creditAccountHeadsList").html($allChildreanData);
		}
		for (var i=0;i<journalArray.length;i++){
			var elem=journalArray[i].split(":");
			var checkValue=elem[0];
			var checkStatus=elem[1];
			var amount=elem[2];
			var ele=$("#creditAccountHeadsList input[value='"+checkValue+"']");
			if(checkStatus=="true"){
				$(ele).prop('checked', true);
			}else{
				$(ele).prop('checked', false);
			}
			$("#creditAccountHeadsList input[item='"+checkValue+"']").val(amount);
		}
	}else if(searchInputId=="debitAccountHeadscontentsearchinput"){
		var journalArray=new Array();
		$("#debitAccountHeadsList input[name='debitaccountHeadsRadio']").each(function(){
			var thisId=$(this).attr('id');
			var checkBoxValue=$(this).val();
			var checkBoxChecked=$("#debitAccountHeadsList input[value='"+checkBoxValue+"']").is(':checked');
			var amount = $("#debitAccountHeadsList input[item='"+checkBoxValue+"']").val();
			var arrayVal=checkBoxValue+":"+checkBoxChecked+":"+amount;
			journalArray.push(arrayVal);
		});
		if(searchInputValue!=""){
			$("#debitAccountHeadsList li[id='debitaccountheadlist'] b[id='debitaccountHeadsLabelId']:containsCI("+searchInputValue+")").each(function(){
				$matchedLiFromContainer+=$(this).parent()[0].outerHTML;
			});
			$("#debitAccountHeadsList li[id='debitaccountheadlist'] b[id='debitaccountHeadsLabelId']:not(:containsCI("+searchInputValue+"))").each(function(){
				$leftUnmatchedLiFromContainer+=$(this).parent()[0].outerHTML;
			});
			$("#debitAccountHeadsList").empty();
			var combinedHTML=$matchedLiFromContainer+$leftUnmatchedLiFromContainer;
			$("#debitAccountHeadsList").html(combinedHTML);
		}else{
			$("#debitAccountHeadsList li[id='debitaccountheadlist']").each(function(){
				$allChildreanData+=$(this)[0].outerHTML;
			});
			$("#debitAccountHeadsList").empty();
			$("#debitAccountHeadsList").html($allChildreanData);
		}
		for (var i=0;i<journalArray.length;i++){
			var elem=journalArray[i].split(":");
			var checkValue=elem[0];
			var checkStatus=elem[1];
			var amount=elem[2];
			var ele=$("#debitAccountHeadsList input[value='"+checkValue+"']");
			if(checkStatus=="true"){
				$(ele).prop('checked', true);
			}else{
				$(ele).prop('checked', false);
			}
			$("#debitAccountHeadsList input[item='"+checkValue+"']").val(amount);
		}
	}else if(searchInputId=="venderitemcontentsearchinput"){
		var vendorItemPricingsArray=new Array();
		var vendorItemRCMGSTRateArray=new Array();
		var vendorItemRCMCESSRateArray=new Array();
		var vendorItemApplicableDateArray=new Array();
		$("#vendorItemList input[name='unitPrice']").each(function(){
			var thisId=$(this).attr('id');
			var checkBoxValue=thisId.substring(9,thisId.length);
			var checkBoxChecked=$("#vendorItemList input[value='"+checkBoxValue+"']").is(':checked');
			var thisValue=$(this).val();
			var arrayVal=thisId+":"+thisValue+":"+checkBoxValue+":"+checkBoxChecked;
			vendorItemPricingsArray.push(arrayVal);
		});



		$("#vendorItemList input[name='cesRateVendItem']").each(function(){
			var thisId=$(this).attr('id');
			var thisValue=$(this).val();
			var arrayVal=thisId+":"+thisValue;
			vendorItemRCMCESSRateArray.push(arrayVal);
		});

		$("#vendorItemList input[name='rcmRateVendItem']").each(function(){
			var thisId=$(this).attr('id');
			var thisValue=$(this).val();
			var arrayVal=thisId+":"+thisValue;
			vendorItemRCMGSTRateArray.push(arrayVal);
		});

		$("#vendorItemList input[name='VendRcmApplicableDate']").each(function(){
			var thisId=$(this).attr('id');
			var thisValue=$(this).val();
			var arrayVal=thisId+":"+thisValue;
			vendorItemApplicableDateArray.push(arrayVal);
		});

		if(searchInputValue!=""){
			$selectAllHtml=$("#vendorItemList li[id='vendoritemlist']:first")[0].outerHTML;
			$("#vendorItemList li[id='vendoritemlist'] span[class='itemLabelClass']:containsCI("+searchInputValue+")").each(function(){
				$matchedLiFromContainer+=$(this).parent()[0].outerHTML;
			});
			$("#vendorItemList li[id='vendoritemlist'] span[class='itemLabelClass']:not(:containsCI("+searchInputValue+"))").each(function(){
				$leftUnmatchedLiFromContainer+=$(this).parent()[0].outerHTML;
			});
			$("#vendorItemList").children().remove();
			var combinedHTML=$selectAllHtml+$matchedLiFromContainer+$leftUnmatchedLiFromContainer;
			$("#vendorItemList").html(combinedHTML);
		}else if(searchInputValue==""){
			$("#vendorItemList li[id='vendoritemlist']").each(function(){
				$allChildreanData+=$(this)[0].outerHTML;
			});
			$("#vendorItemList").children().remove();
			$("#vendorItemList").html($allChildreanData);
		}
		for (var i=0;i<vendorItemPricingsArray.length;i++){
			var elem=vendorItemPricingsArray[i].split(":");
			var elemId=elem[0];
			var elemValue=elem[1];
			var checkValue=elem[2];
			var checkStatus=elem[3];
			$("#vendorItemList input[id="+elemId+"]").val(elemValue);
			if(checkStatus=="true"){
				$("#vendorItemList input[value='"+checkValue+"']").prop('checked', true);
			}else{
				$("#vendorItemList input[value='"+checkValue+"']").prop('checked', false);
			}
		}
		// RCM CESS RATE
		for (var i=0;i<vendorItemRCMCESSRateArray.length;i++){
			var elem=vendorItemRCMCESSRateArray[i].split(":");
			var elemId=elem[0];
			var elemValue=elem[1];
			$("#vendorItemList input[id="+elemId+"]").val(elemValue);
		}

		for (var i=0;i<vendorItemRCMGSTRateArray.length;i++){
			var elem=vendorItemRCMGSTRateArray[i].split(":");
			var elemId=elem[0];
			var elemValue=elem[1];
			$("#vendorItemList input[id="+elemId+"]").val(elemValue);
		}

		for (var i=0;i<vendorItemApplicableDateArray.length;i++){
			var elem=vendorItemApplicableDateArray[i].split(":");
			var elemId=elem[0];
			var elemValue=elem[1];
			$("#vendorItemList input[id="+elemId+"]").attr("disabled", false);
			$("#vendorItemList input[id="+elemId+"]").removeClass("hasDatepicker");
			$("#vendorItemList input[id="+elemId+"]").datepicker({
				changeMonth : true,
				changeYear : true,
				dateFormat:  'MM d,yy',
				yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
				onSelect: function(x,y){
					$(this).focus();
				}
			});
			$("#vendorItemList input[id="+elemId+"]").val(elemValue);
		}


	}else if(searchInputId=="customeritemcontentsearchinput"){
		var customerDiscountArray=new Array();
		$("#customerItemList input[name='custDiscount']").each(function(){
			var thisId=$(this).attr('id');
			var checkBoxValue=thisId.substring(12,thisId.length);
			var checkBoxChecked=$("#customerItemList input[value='"+checkBoxValue+"']").is(':checked');
			var thisValue=$(this).val();
			var arrayVal=thisId+":"+thisValue+":"+checkBoxValue+":"+checkBoxChecked;
			customerDiscountArray.push(arrayVal);
		});
		if(searchInputValue!=""){
			$selectAllHtml=$("#customerItemList li[id='customeritemlist']:first")[0].outerHTML;
			$("#customerItemList li[id='customeritemlist'] span[class='itemLabelClass']:containsCI("+searchInputValue+")").each(function(){
				$matchedLiFromContainer+=$(this).parent()[0].outerHTML;
			});
			$("#customerItemList li[id='customeritemlist'] span[class='itemLabelClass']:not(:containsCI("+searchInputValue+"))").each(function(){
				$leftUnmatchedLiFromContainer+=$(this).parent()[0].outerHTML;
			});
			$("#customerItemList").children().remove();
			var combinedHTML=$selectAllHtml+$matchedLiFromContainer+$leftUnmatchedLiFromContainer;
			$("#customerItemList").html(combinedHTML);
		}else if(searchInputValue==""){
			$("#customerItemList li[id='customeritemlist']").each(function(){
				$allChildreanData+=$(this)[0].outerHTML;
			});
			$("#customerItemList").children().remove();
			$("#customerItemList").html($allChildreanData);
		}
		for (var i=0;i<customerDiscountArray.length;i++){
			var elem=customerDiscountArray[i].split(":");
			var elemId=elem[0];
			var elemValue=elem[1];
			var checkValue=elem[2];
			var checkStatus=elem[3];
			$("#customerItemList input[id="+elemId+"]").val(elemValue);
			if(checkStatus=="true"){
				$("#customerItemList input[value='"+checkValue+"']").prop('checked', true);
			}else{
				$("#customerItemList input[value='"+checkValue+"']").prop('checked', false);
			}
		}
	}else{
		var customArray=new Array();
		$("#"+$ulcontainer+" li[class='"+$licontainer+"']").each(function(){
			var thisInputFirstId=$(this).find('input[name="'+$firstInputFieldName+'"]').attr('id');
			var thisInputFirstValue=$(this).find('input[name="'+$firstInputFieldName+'"]').val();
			var thisInputSecondId=$(this).find('input[name="'+$secondInputFieldName+'"]').attr('id');
			var thisInputSecondValue=$(this).find('input[name="'+$secondInputFieldName+'"]').val();
			if(typeof thisInputFirstId!='undefined' && typeof thisInputFirstValue!='undefined' && typeof thisInputSecondId!='undefined' && typeof thisInputSecondValue!='undefined'){
				var checkBoxValue=$(this).find('input[type="checkbox"]').val();
				var checkBoxChecked=$(this).find('input[type="checkbox"]').is(':checked');
				var radioBoxValue=$(this).find('input[type="radio"]').value;
				var radioBoxChecked=$(this).find('input[type="radio"]').is(':checked');
				var arrayVal="";
				if(typeof checkBoxValue!='undefined' && typeof checkBoxChecked!='undefined'){
					arrayVal=thisInputFirstId+":"+thisInputFirstValue+":"+thisInputSecondId+":"+thisInputSecondValue+":"+checkBoxValue+":"+checkBoxChecked;
				}
				if(typeof radioBoxValue!='undefined' && typeof radioBoxChecked!='undefined'){
					arrayVal=thisInputFirstId+":"+thisInputFirstValue+":"+thisInputSecondId+":"+thisInputSecondValue+":"+radioBoxValue+":"+radioBoxChecked;
				}
				customArray.push(arrayVal);
			}
		});
		if(searchInputValue!=""){
			var label=$("#"+$ulcontainer+" li[id='travelClaim_Header']").attr('id');
			if(typeof label!='undefined'){
				$labelHtml=$("#"+$ulcontainer+" li[id='travelClaim_Header']")[0].outerHTML;
			}
			var label1=$("#"+$ulcontainer+" li[id='travelBoardingLodging_Header']").attr('id');
			if(typeof label1!='undefined'){
				$labelHtml=$("#"+$ulcontainer+" li[id='travelBoardingLodging_Header']")[0].outerHTML;
			}
			var label2=$("#"+$ulcontainer+" li[id='expenseClaims_Header']").attr('id');
			if(typeof label2!='undefined'){
				$labelHtml=$("#"+$ulcontainer+" li[id='expenseClaims_Header']")[0].outerHTML;
			}
			var selectAlLabel=$("#"+$ulcontainer+" li[id='"+$ulcontainer+"']:first").attr('id');
			if(typeof selectAlLabel!='undefined'){
				$selectAllHtml=$("#"+$ulcontainer+" li[id='"+$ulcontainer+"']:first")[0].outerHTML;
			}
			var expenseSelectAllLabel=$("#"+$ulcontainer+" li[id='expenseClaims_List']:first").attr('id');
			if(typeof expenseSelectAllLabel!='undefined'){
				$selectAllHtml=$("#"+$ulcontainer+" li[id='expenseClaims_List']:first")[0].outerHTML;
			}
			$("#"+$ulcontainer+" li[class='"+$licontainer+"'] span[class='"+$spanClass+"']:containsCI("+searchInputValue+")").each(function(){
				var value=$(this).parent().find('input[type="checkbox"]').val();
				var value1=$(this).parent().find('input[type="radio"]').val();
				if(value!="" && typeof value!='undefined'){
					$matchedLiFromContainer+=$(this).parent()[0].outerHTML;
				}
				if(value1!="" && typeof value1!='undefined'){
					$matchedLiFromContainer+=$(this).parent()[0].outerHTML;
				}
			});
			$("#"+$ulcontainer+" li[class='"+$licontainer+"'] span[class='"+$spanClass+"']:not(:containsCI("+searchInputValue+"))").each(function(){
				var value=$(this).parent().find('input[type="checkbox"]').val();
				var value1=$(this).parent().find('input[type="radio"]').val();
				if(value!="" && typeof value!='undefined'){
					$leftUnmatchedLiFromContainer+=$(this).parent()[0].outerHTML;
				}
				if(value1!="" && typeof value1!='undefined'){
					$leftUnmatchedLiFromContainer+=$(this).parent()[0].outerHTML;
				}
			});
			$("#"+$ulcontainer+"").children().remove();
			if(typeof $labelHtml=='undefined'){
				$labelHtml="";
			}
			if(typeof $selectAllHtml=='undefined'){
				$selectAllHtml="";
			}
			var combinedHTML=$labelHtml+$selectAllHtml+$matchedLiFromContainer+$leftUnmatchedLiFromContainer;
			$("#"+$ulcontainer+"").html(combinedHTML);
		}else if(searchInputValue==""){
			$("#"+$ulcontainer+" li[class='"+$licontainer+"']").each(function(){
				$allChildreanData+=$(this)[0].outerHTML;
			});
			$("#"+$ulcontainer+"").children().remove();
			$("#"+$ulcontainer+"").html($allChildreanData);
		}
		for (var i=0;i<customArray.length;i++){
			var elem=customArray[i].split(":");
			var inputFirstElemId=elem[0];
			var inputFirstElemValue=elem[1];
			var inputSecondElemId=elem[2];
			var inputSecondElemValue=elem[3];
			var checkValue=elem[4];
			var checkStatus=elem[5];
			$("#"+$ulcontainer+" input[id="+inputFirstElemId+"]").val(inputFirstElemValue);
			$("#"+$ulcontainer+" input[id="+inputSecondElemId+"]").val(inputSecondElemValue);
			if(checkStatus=="true"){
				$("#"+$ulcontainer+" input[value='"+checkValue+"']").prop('checked', true);
			}else{
				$("#"+$ulcontainer+" input[value='"+checkValue+"']").prop('checked', false);
			}
		}
	}
	$.unblockUI();
}

function hideModal(id) {
	if ('' !== id || null !== id || undefined !== id) {
		$("div[class='modal-backdrop fade in']").remove();
		$("#" + id).attr('data-toggle', 'modal');
		$("#" + id).modal('hide');
		$("#dashboardAutoSuggestId").show();
		$("#dashboardLocationFirstAutoSuggest").show();
		$("#dashboardLocationSecondAutoSuggest").show();
	}
}

function showAdvanceTransactionModal(elem){
	 var entityId=$(elem).attr('id');
	 var advancePendingAmount=$(elem).attr('name');
	 if(parseFloat(advancePendingAmount)>0){
		 var jsonData = {};
		 var useremail=$("#hiddenuseremail").text();
		 jsonData.usermail = useremail;
		 jsonData.vendCustEntityId=entityId;
		 jsonData.pendAmount=advancePendingAmount;
		 var url="/dashboard/customerVendorAdvanceTransactionBI";
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
				if(data.dashboardCustomerVendorAdvanceTransactionData.length>0){
					$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
					$("#advancePendingTransaction table[id='advancePendingTransactionTable'] tbody").html("");
					$("#dashboardAutoSuggestId").hide();
					$("#dashboardLocationFirstAutoSuggest").hide();
					$("#dashboardLocationSecondAutoSuggest").hide();
					$("#customerAdvancePending").modal('hide');
					$("#vendorAdvancePending").modal('hide');
					$("#advancePendingTransaction").attr('data-toggle', 'modal');
					$("#advancePendingTransaction").modal('show');
				    $("#advancePendingTransaction a[id='fancybox-close']").attr("href",location.hash);
				    $("#advancePendingTransaction span[class='advanceInEntityAccount']").text("");
				    $("#advancePendingTransaction span[class='advanceInAccount']").text("");
				    $("#advancePendingTransaction span[class='advanceInEntityAccount']").text(data.dashboardCustomerVendorAdvanceTransactionData[0].name);
				    $("#advancePendingTransaction span[class='advanceInAccount']").text(data.dashboardCustomerVendorAdvanceTransactionData[0].pendingAmount);
				    for(var i=0;i<data.dashboardCustomerVendorAdvanceTransactionData.length;i++){
				    	$("#advancePendingTransaction table[id='advancePendingTransactionTable'] tbody").append('<tr><td><b>'+data.dashboardCustomerVendorAdvanceTransactionData[i].totalAdvanceCollected+'</b></td><td><b>'+data.dashboardCustomerVendorAdvanceTransactionData[i].advanceCollecteddate+'</b></td><td><b>'+data.dashboardCustomerVendorAdvanceTransactionData[i].totalAdvancePendingSince+'</b></td></tr>');
				    }
				    $.unblockUI();
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	}
}




/*Session timeout Starts*/
var time = 1800000, timeInterval = 0;
$(document).ready(function() {
	timeout();
	$('body').on('click', function() {
		var url = window.location.pathname;
		if ('/config' === url || '/passwordExpiry' === url || '/vendCustConfig' === url || '/seller' === url) {
			clearTimeout(timeInterval);
			timeout();
		}
	});

	function timeout() {
		var url = window.location.pathname;
		if ('/config' === url || '/passwordExpiry' === url) {
			timeInterval = window.setTimeout(function() {
				//window.location.href = '/logout#sessionExpired';
				doLogout();
			}, time);
		} else if ('/vendCustConfig' === url) {
			timeInterval = window.setTimeout(function() {
				var url = $('.vendcustsignoutlink').attr('href');
				url = url.split('#');
				url = url[0];
				window.location.href = url + '#sessionExpired';
			}, time);
		} else if ('/seller' === url) {
			timeInterval = window.setTimeout(function() {
				//window.location.href = '/sellerlogout#sellerAccountLoginDiv';
				doSellerLogout();
			}, time);
		}
	}
});
/*Session timeout Ends*/

/*Application Loading Starts*/
function loginAjaxSetUp() {
	$('.blockUI').hide();
	$('#mainBody').hide();
	$('#loadingIdos').show();
}

function ajaxSetUpReset() {
	$.ajaxSetup({
		beforeSend : function(xhr) {
		},
		complete : function(xhr,status) {
		}
	});
}
/*Application Loading Ends*/

/*Cookie Functionality Starts*/
function setCookie(cname,cvalue,exdays) {
	var d = new Date();
	d.setTime(d.getTime()+(exdays*24*60*60*1000));
	var expires = "expires="+d.toGMTString();
	document.cookie = cname + "=" + cvalue + "; " + expires;
}

function getCookie(cname) {
	var name = cname + "=";
	var ca = document.cookie.split(';');
	for(var i=0; i<ca.length; i++) {
		var c = ca[i].trim();
		if (c.indexOf(name)==0) return c.substring(name.length,c.length);
	}
	return "";
}
/*Cookie Functionality Ends*/
/*Support Starts*/
function sellerShowPage() {
	$('#supportCenter').hide();
	$('#vendSellerAccounts').fadeIn(1000);
}
$(document).ready(function() {

	/* Sunil
	$('#supportIcon').on('click', function() {
		if($('#supportCenter').is(':visible')){
			$('#supportCenter').slideUp('slow',function(){
				$('#supportMyCases').css({'width': '0', 'min-width': '0'});
				$('#supportExtra').css({'top': '0'});
				$('#supportHistory').hide();
			});
		}else{
			if($('#createNewNote').is(':visible')){
				$('#createNewNote').slideUp('slow', function(){
					$('#supportCenter').slideDown('slow');
					animateNotesTable(false);
					$('#notesRemarks').hide();
				});
			}else{
				$('#supportCenter').slideDown('slow');
			}
		}
	});
	*/
	$('#supportAttachment').on('change', function() {
		//ert("Work in progress!, currently, attachment is not allowed");
		//turn false;
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var name = this.files[0].name; var fileUrl="";
		$('#supportFileName').html(name);
		var input=this.files[0];
		//filepicker.setKey('A6zVM3VmDQhqeTq6sF209z');
		//filepicker.setKey('A7ucPpqRuR46F7OVE8CHJz');
        //uploadFile(this.id);
		uploadFileBlob(this);
		/*filepicker.upload(input, function(InkBlob){
		  fileUrl=JSON.stringify(InkBlob.url);
		  $('#supportFPLink').val(fileUrl);
		  $.unblockUI();
		});*/

		$.unblockUI();
	});

	$('#downloadSupportAttachment').on('click',function(){
		var url=$(this).attr('data-file');
		downloadfile(url);
	});

	$('#supportSend').on('click', function(e) {
		e.preventDefault();
		var email;
		if (/seller/i.test(window.location.href)) {
			email = $("#hiddenvendcustemail").text();
		} else {
			email = $("#hiddenuseremail").text();
		}
		var subject = $('#supportSubject').val();
		var message = $('#supportMessage').val();
		if (!isEmpty(subject) && !isEmpty(message)) {
			$('#supportUserEmail').val(email);
			var form=$('#supportTable');
			var data = new FormData();
			$.each($('#supportAttachment')[0].files, function(i, file) {
			    data.append('file-'+i, file);
			});
			var other_data = $('#supportTable').serializeArray();
			$.each(other_data,function(key,input){
				data.append(input.name,input.value);
		    });
			if (/seller/i.test(window.location.href)) {
				data.append('accType', 'seller');
			} else {
				data.append('accType', 'user');
			}
			$.ajax({
				method: "POST",
				url: form.attr('action'),
				data: data,
				headers:{
					"X-AUTH-TOKEN": window.authToken
				},
				cache: false,
			    contentType: false,
			    processData: false,
				success: function(data) {
					if (data.result) {
						$('#supportTicketmsg').html('Thank you for contacting Support Center.<br/>'
								+ 'Your query will be addressed soon. Please note you Case ID : <span class="idos-font">' + data.caseId + '</span>');
					} else {
						$('#supportTicketmsg').html(data.message);
					}
					$('#supportTable input').val('');
					$('#supportTable textarea').val('');
					$('#supportFileName').html('');
				},
				error: function (xhr, status, error) {
					if(xhr.status == 401){ doLogout(); }
				}
			});
		} else {
			if (isEmpty(subject)) {
				$('#supportSubject').focus().val('');
				$('#supportSubLabel span').css('color', 'red');
				setTimeout(function() {
					$('#supportSubLabel span').css('color', '');
				}, 1000);
			} else if (isEmpty(message)) {
				$('#supportMessage').focus().val('');
				$('#supportMsgLabel span').css('color', 'red');
				setTimeout(function() {
					$('#supportMsgLabel span').css('color', '');
				}, 1000);
			}
		}
	});



	$('#supportCasesButton').on('click', function() {
			var jsonData = {};
			if (/seller/i.test(window.location.href)) {
				jsonData.userEmail = $("#hiddenvendcustemail").text();
				jsonData.accType = 'seller';
			} else {
				jsonData.userEmail = $("#hiddenuseremail").text();
				jsonData.accType = 'user';
			}
			jsonData.filterValue = "";
			jsonData.filter = "other";
			ajaxCall('/support/getSupportTicketById', jsonData, '', '', '', '', 'supportTicketByIdSuccess', '', true);

			$('#supportMyCases').slideDown('slow');
	});



	$('#supportFilter').on('change', function() {
		var filterValue = this.value;
		if ($('#supportOpenOnly').is(':checked')) {
			filterValue += "/active";
		} else {
			filterValue += "/ ";
		}
		var jsonData = {};
		if (/seller/i.test(window.location.href)) {
			jsonData.userEmail = $("#hiddenvendcustemail").text();
			jsonData.accType = 'seller';
		} else {
			jsonData.userEmail = $("#hiddenuseremail").text();
			jsonData.accType = 'user';
		}
		jsonData.filterValue = filterValue;
		jsonData.filter = "other";
		ajaxCall('/support/getSupportTicketById', jsonData, '', '', '', '', 'supportTicketByIdSuccess', '', true);
	});

	$('#supportOpenOnly').on('change', function() {
		var filterValue = $('#supportFilter').val();
		if ($(this).is(':checked')) {
			filterValue += "/active";
		} else {
			filterValue += "/ ";
		}
		var jsonData = {};
		if (/seller/i.test(window.location.href)) {
			jsonData.userEmail = $("#hiddenvendcustemail").text();
			jsonData.accType = 'seller';
		} else {
			jsonData.userEmail = $("#hiddenuseremail").text();
			jsonData.accType = 'user';
		}
		jsonData.filterValue = filterValue;
		jsonData.filter = "other";
		ajaxCall('/support/getSupportTicketById', jsonData, '', '', '', '', 'supportTicketByIdSuccess', '', true);
	});

	$('#supportSearch').on('keyup', function() {
		var filterValue = $('#supportFilter').val();
		if ($('#supportOpenOnly').is(':checked')) {
			filterValue += "/active";
		} else {
			filterValue += "/ ";
		}
		filterValue += "/" + this.value;
		var jsonData = {};
		if (/seller/i.test(window.location.href)) {
			jsonData.userEmail = $("#hiddenvendcustemail").text();
			jsonData.accType = 'seller';
		} else {
			jsonData.userEmail = $("#hiddenuseremail").text();
			jsonData.accType = 'user';
		}
		jsonData.filterValue = filterValue;
		if (this.value !== "") {
			jsonData.filter = "searchtext";
		} else {
			jsonData.filter = "other";
		}
		ajaxCall('/support/getSupportTicketById', jsonData, '', '', '', '', 'supportTicketByIdSuccess', '', false);
	});
	$('#closeSupportCases').on('click', function() {
        animateSupportIssues(false);
    });
});

function supportTicketByIdSuccess(data) {

	//alert("supportTicketByIdSuccess");

	data = data.results;
	if (data.length > 0) {
		$('#supportMyCasesTable').html('');
		var score = 0;
		for (var i = 0; i < data.length; i++) {
			score = 0;
			var rowData = '<tr id="' + data[i].ticketId + '"><td><input type="hidden" value="' + data[i].ticketId + '" id="supportCaseId" /><span class="commentHistory">' + data[i].caseId + '</span></td>'
					+ '<td>' + data[i].subject + '</td><td>' + data[i].status + '</td><td>' + data[i].created + '</td><td>' + data[i].updated + '</td>'
					+ '<td><input type="radio" name="' + data[i].ticketId + '" class="support-helpful" value="1"/>Yes'
					+ '<input type="radio" name="' + data[i].ticketId + '" class="support-helpful" value="0"/>No<br/>'
					+ '<div class="support-rate" id="' + data[i].ticketId + '"></div><br/></td>';
			$('#supportMyCasesTable').append(rowData);
			if (!isEmpty(data[i].helpful)) {
				$('#supportMyCasesTable').find('tr[id="' + data[i].ticketId + '"] input[name="' + data[i].ticketId + '"][value="' + data[i].helpful + '"]').prop('checked', true);
			}
			if (!isEmpty(data[i].rating)) {
				score = data[i].rating;
			}
			$('div[id="' + data[i].ticketId + '"][class="support-rate"]').raty({
			    cancel      : true,
			    targetKeep  : true,
			    precision   : true,
			    score		: score,
			    path		: '/assets/images',
			    click: function(score, evt) {
			    	var jsonData = {};
			    	if (/seller/i.test(window.location.href)) {
						jsonData.userEmail = $("#hiddenvendcustemail").text();
						jsonData.accType = 'seller';
					} else {
						jsonData.userEmail = $("#hiddenuseremail").text();
						jsonData.accType = 'user';
					}
					jsonData.id = this.id;
					jsonData.rate = score;
					ajaxCall('/support/updateRate', jsonData, '', '', '', '', 'updateRateSuccess', '', false);
			    }
			});
		}
	} else {
		$('#supportMyCasesTable').html('<div style="text-align: center;">No Results Found</div>');
	}
	//animateSupportIssues(true);
}

function supportTicketHistorySuccess(data) {
	data = data.results;
	if (data.length > 0) {
		data = data[0];
		if (!isEmpty(data)) {
			$('#supportHistoryTicketId').val(data.ticketId);
			$('#supportHistoryTicketNumber').html(data.caseId);
			$('#supportHistoryTicketCreate').html(data.created);
			$('#supportHistoryTicketCreatedBy').html(data.createdBy);
			$('#supportHistoryTicketAssignedTo').html(data.assigned);
			$('#supportHistoryTicketAttendedBy').html(data.attended);
			$('#downloadSupportAttachment').html(data.attachment);
			$('#downloadSupportAttachment').attr('title', data.attachment);
			$('#downloadSupportAttachment').attr('data-file', data.attachmentFile.replace(/"/g, ''));
			$('#supportBodyView').html(data.message);
			if (1 === data.statusNumber) {
				$('#supportOpenCloseIssue').html('<span issue="0">Close the issue</span>');
			} else {
				$('#supportOpenCloseIssue').html('<span issue="1">Open the issue</span>');
			}
			if (!isEmpty(data.comments) && data.comments.length > 0) {
				$('#commentHistory').empty();
				var comments = data.comments;
				for (var i = 0; i < comments.length; i++) {
					$('#commentHistory').append('<div class="comments-replies">'
								+ '<span>' + comments[i].createdBy + '</span>'
								+ '<span class="reported-time">' + comments[i].created + '</span><br/>'
								+ '<span><font>' + comments[i].comment + '</p></span><br/>'
								+ '<span>Attachment : </span><span class="download-file" title="Download File" id="comment_' + comments[i].id + '"></span></div>');
					getFileNameForSupport(comments[i].attachment, '#comment_' + comments[i].id);
				}
			} else {
				$('#commentHistory').html('No comments recorded yet. Be the first one to write a comment.');
			}
			if (!isEmpty(data.replies) && data.replies.length > 0) {
				$('#replyHistory').empty();
				var replies = data.replies;
				for (var i = 0; i < replies.length; i++) {
					$('#replyHistory').append('<div class="comments-replies">'
							+ '<span>' + replies[i].createdBy + '</span>'
							+ '<span class="reported-time">' + replies[i].created + '</span><br/>'
							+ '<div class="replies"><span>To: <font>' + replies[i].to + '<font></span><br/>'
							+ '<span>Subject : <font>' + replies[i].subject + '</p><span><br/>'
							+ '<span>Message : <font>' + replies[i].message + '</p></span><br/>'
							+ '<span>Attachment : </span><span class="download-file" title="Download File" id="reply_' + replies[i].id + '"></span></div></div>');
					getFileNameForSupport(replies[i].attachment, '#reply_' + replies[i].id);
				}
			} else {
				$('#replyHistory').html('No replies recorded yet.');
			}
			$('#supportHistory').slideDown('slow');
		} else {
			swal('Error!','Something went wrong. Please try again later.','error');
		}
	}
}

function updateSupportHistoryTicketSuccess(data) {
	if (data.result) {
		if ($('#supportHistoryTicketId').val() == data.id) {
			if ($('#supportOpenCloseIssue span').attr('issue') == data.status) {
				if (data.status === 0) {
					$('#supportOpenCloseIssue').html('<span issue="1">Open the issue</span>');
				} else {
					$('#supportOpenCloseIssue').html('<span issue="0">Close the issue</span>');
				}
			}
		}
	}
}

$(document).ready(function() {
	$('body').on('click', '#supportOpenCloseIssue span', function() {
		var status = $(this).attr('issue');
		var ticketId = $('#supportHistoryTicketId').val();
		var ticketNumber = $('#supportHistoryTicketNumber').text();
		if (!isEmpty(ticketId) && !isEmpty(ticketNumber) && !isEmpty(status)) {
			var jsonData = {};
			if (/seller/i.test(window.location.href)) {
				jsonData.email = $("#hiddenvendcustemail").text();
				jsonData.accType = 'seller';
			} else {
				jsonData.email = $("#hiddenuseremail").text();
				jsonData.accType = 'user';
			}
			jsonData.ticketId = ticketId;
			jsonData.ticketNumber = ticketNumber;
			jsonData.status = status;
			ajaxCall('/support/openCloseIssue', jsonData, '', '', '', '', 'updateSupportHistoryTicketSuccess', '', false);
		}
	});

	$('body').on('click', 'span.download-file', function() {
		var url = $(this).find('.attachment-url').val();
		if (!isEmpty(url)) {
			downloadfile(url);
		}
	});
});

function getFileNameForSupport(url, appendId) {
	if ((undefined !== url || '' !== url || null !== url) && (undefined !== appendId || '' !== appendId || null !== appendId)) {
		var inkblob, fileName = '';
		if ('' != url) {
			inkblob = { url: url};
			//filepicker.setKey('A6zVM3VmDQhqeTq6sF209z');
			//filepicker.setKey('A7ucPpqRuR46F7OVE8CHJz');
			//filepicker.stat(inkblob, {filename: true},function(metadata) {
			//	fileName=JSON.stringify(metadata.filename);
			filepicker.retrieve(inkblob, { metadata: true }).then(function(response){
	  			fileName=JSON.stringify(response.filename);
				fileName = fileName.replace(/"/g, '');
				$(appendId).append('<input type="hidden" value="' + url + '" class="attachment-url"/>' + fileName);
			});
		}
	}
}

$(document).ready(function() {
	$('body').on('click', '.commentHistory', function() {
		var ticketId = $(this).prev().val();
		var ticketNumber = $(this).text();
		if (!isEmpty(ticketId) && !isEmpty(ticketNumber)) {
			var filterValue = ' / /' + ticketNumber;
			var jsonData = {};
			if (/seller/i.test(window.location.href)) {
				jsonData.userEmail = $("#hiddenvendcustemail").text();
				jsonData.accType = 'seller';
			} else {
				jsonData.userEmail = $("#hiddenuseremail").text();
				jsonData.accType = 'user';
			}
			jsonData.filterValue = filterValue;
			jsonData.filter = "searchtext";
			ajaxCall('/support/getSupportTicketById', jsonData, '', '', '', '', 'supportTicketHistorySuccess', '', false);
		}
	});

	$('body').on('click', '.support-helpful', function() {
		var val = this.value;
		var name = this.name;
		var jsonData = {};
		if (/seller/i.test(window.location.href)) {
			jsonData.userEmail = $("#hiddenvendcustemail").text();
			jsonData.accType = 'seller';
		} else {
			jsonData.userEmail = $("#hiddenuseremail").text();
			jsonData.accType = 'user';
		}
		jsonData.id = name;
		jsonData.help = val;
		ajaxCall('/support/updateHelp', jsonData, '', '', '', '', 'updateHelpSuccess', '', false);
	});

	$('#supportHistoryComment').on('click', function() {
		var ticketId = $('#supportHistoryTicketId').val();
		var ticketNumber = $('#supportHistoryTicketNumber').text();
		var comments = $('#userComments').val();
		if (!isEmpty(ticketId) && !isEmpty(ticketNumber) && !isEmpty(comments)) {
			var jsonData = {};
			if (/seller/i.test(window.location.href)) {
				jsonData.email = $("#hiddenvendcustemail").text();
				jsonData.accType = 'seller';
			} else {
				jsonData.email = $("#hiddenuseremail").text();
				jsonData.accType = 'user';
			}
			jsonData.ticketId = ticketId;
			jsonData.ticketNumber = ticketNumber;
			jsonData.comments = comments;
			jsonData.attachment = $('#commentUploadUrl').val();
			ajaxCall('/support/addComment', jsonData, '', '', '', '', 'addCommentSuccess', 'errorNotify', true);
		}
	});

	$('#closeSupportHistory').on('click', function() {
		$('#supportReplyOverlay, #supportHistory').slideUp('slow');
		$('#supportHistory').find('textarea').val('');
	});

	$('#writeComment').on('click', function() {
		$('#userComments').val('');
		if ($('#commentArea').is(':visible')) {
			$('#commentArea').slideUp('slow');
		} else {
			$('#commentArea').slideDown('slow');
		}
	});
	$('#supportHistoryCommentCancel').on('click', function() {
		$('#commentArea').slideUp('slow');
		$('#userComments').val('');
	});
	$('.close-bottom').on('click', function(){
		if($('#supportCenter').is(':visible')){
			$('#supportCenter').slideUp('slow',function(){
				//$('#supportMyCases').css({'width': '0', 'min-width': '0'}); Sunil
				$('#supportExtra').css({'top': '0'});
				$('#supportHistory').hide();
			});
		}else if($('#createNewNote').is(':visible')){
			$('#createNewNote').slideUp('slow',function(){
				animateNotesTable(false);
				$('#notesRemarks').hide();
			});
		}
	});
});

function addCommentSuccess(data) {
	$('#commentArea').slideUp();
	if (data.result) {
		supportTicketHistorySuccess(data);
	}
}

/*function updateHelpSuccess(data) {
	if (data.result) {
		$('#supportMyCasesTable').find('input[name="' + data.id + '"]').attr('readonly', true);
	}
}*/

function animateSupportIssues(show) {
	if(show) {
		$('#supportMyCases').animate({'width': '52%', 'min-width': '700px'}, 1500);
		$('#supportExtra').animate({'top': '185px'}, 1500);
	} else {
		$('#supportMyCases').animate({'width': '0', 'min-width': '0'}, 1500);
		$('#supportExtra').animate({'top': '0'}, 1500);
	}
}
function performClick(node) {
   var evt = document.createEvent("MouseEvents");
   evt.initEvent("click", true, false);
   node.dispatchEvent(evt);
}

function uploadCommentFile(nameId, urlId){
	//filepicker.setKey('A6zVM3VmDQhqeTq6sF209z');
	//filepicker.setKey('A7ucPpqRuR46F7OVE8CHJz');
	/*	var fileName="", fileurl = '';
		filepicker.pickAndStore({multiple:"true"},{location:"S3"},function(fpfiles){
			jQuery.each(fpfiles, function() {
				fileurl+=this.url+",";
				fileName += this.filename + ',';
			});
		var uploadName=fileName.substring(0, fileName.length-1);
		var uploadUrl = fileurl.substring(0, fileurl.length-1);
		$('input[name="'+nameId+'"]').val(uploadName);
		$('input[name="'+urlId+'"]').val(uploadUrl);
		filepicker.stat(uploadUrl, {filename: true,size:true},function(metadata){
			fileFileFullName=JSON.stringify(metadata.filename);
			fileSize=JSON.stringify(metadata.size);
			inserIntoIdosFileUploadLogs(fileFileFullName,fileSize,uploadUrl);
		});
	});*/
	uploadFile(nameId);
}
/*Support Ends*/

/*Forgot Security Answer Starts*/
$(document).ready(function() {
	$('#forgotSecurityAnswer').on('click', function() {
		var email = GetURLParameter('email');
		var token = GetURLParameter('token');
		if (!isEmpty(email) && !isEmpty(token)) {
			var jsonData = {};
			jsonData.email = email;
			jsonData.token = token;
			ajaxCall('/user/resetSecurityAnswerLink', jsonData, '', '', '', '', 'resetSecurityAnswer', '', false);
		} else {
			$('#resetsuccessregdiv').html("Something went wrong. Please follow the link from the email again.");
			$('#resetsuccessregdiv').fadeIn();
		}
	});

	$('body').on('click', '#resetSecurityQuestionsBtn', function() {
		var length = $('#securityQuestions').find('input[type=text]').length;
		var answer = '';
		var jsonData = {};
		var email = $('#emailUserSecurity').val();
		jsonData.userEmail = email;
		jsonData.length = length;
		for (var i = 0; i < length; i++) {
			answer = $('#securityAnswer_' + i).val();
			if (!isEmpty(answer)) {
				jsonData['question_' + i] = $('#securityQuestion_' + i).text();
				jsonData['answer_' + i] = answer;
			}
		}
		ajaxCall('/accountSetting/saveUserSecurityAnswers', jsonData, '', '', '', '', 'successSecurityAnswer', '', false);
	});
});

function successSecurityAnswer(data) {
	console.log(data);
}

function resetSecurityAnswer(data) {
	if (data.result) {
		$('#vendCustresetsuccessaccountmsg').html(data.message);
		notifyLogin.show(data.message,true,false);
		$('#resetsuccessregdiv').fadeIn();
	}
}

function userSecurityQuestion(data) {
	if (data.result) {
		data = data.questions;
		if (data.length > 0) {
			var email = GetURLParameter('email');
			$('#emailUserSecurity').val(email);
			for (var i = 0; i < data.length; i++) {
				$('#securityQuestions').append('<div class="resetQuestionDiv"><span class="resetQuestion" id="securityQuestion_' + i + '">' + data[i].question + '</span>'
						+ '<input type="text" name="securityAnswer" id="securityAnswer_' + i + '"/>'
						+ '<input type="hidden" id="securityQuestionId_' + i + ' value="' + data[i].questionId + '""/></div>');
			}
			$('#securityQuestions').append('<div class="resetQuestionDiv" style="border: 0;">'
						+ '<button class="btn btn-primary btn-idos" id="resetSecurityQuestionsBtn">Reset Answers</button></div>');
		} else {
			window.location.href = '/logout';
		}
	}
}
/*Forgot Security Answer Ends*/

/*User Extra Options Starts*/
$(document).ready(function() {
	/*$('.settingIcon').on('click', function() {
		if ($('#userOptions').height() < 75) {
			animateUserOptions(true);
		} else {
			animateUserOptions(false);
		}
	});*/

	$(".settingIcon").mouseenter(function(e) {
		 animateProcurement(false, e);
		 animateUserOptions(true, e);
	})/*.mouseleave(function(e) {
		console.log($(e.target).attr('id'));
		if ($(e.target).attr('id') !== "userOptions") {
			animateUserOptions(false);
		}
	})*/;

	$('body').on('click', function(e) {
		animateUserOptions(false,e);
		animateProcurement(false, e);
	});

	$('div, span, table').scroll(function(e) {
		recordScroll(this);
		if (this.id !== 'procurementOptions') {
			$('#userOptions:visible, #procurementOptions:visible').css({'height': '0px', 'top': '35px'});
	        $('#userOptions div:visible, #procurementOptions ul:visible').hide();
		}
	});

	$(window).scroll(function(e) {
		$('#userOptions:visible, #procurementOptions:visible').css({'height': '0px', 'top': '35px'});
        $('#userOptions div:visible, #procurementOptions ul:visible').hide();
	});

	$('.go-to-top').on('click', function() {
		$(pushToTop).animate({ scrollTop: 0 }, "fast");
	});

	$(".procurementicon").mouseenter(function(e) {
		animateUserOptions(false, e);
		animateProcurement(true, e);
	});
});

function recordScroll(ele) {
	pushToTop = ele;
	if ($(ele).height() > 250 && $(ele).scrollTop() > 250) {
		$('.go-to-top').fadeIn();
	} else {
		$('.go-to-top').fadeOut();
	}
}

function animateUserOptions(show, e) {
	if (show) {
		if($('#userOptions #hiddenuseremail').length==1){

			if ($('#userOptions').height() < 100) {
				//alert("animateUserOptions") ;
				$('.user-settings').css('top','54px');
				$('.user-logout').css('top','77px');
				$('#userOptions').css({'height': '100px', 'top': '45px'});
				$('#userOptions div').show();
			}
		}else{

			if ($('#userOptions').height() < 75) {
				//alert("animateUserOptions2") ;
				$('.user-settings').css('top','28px');
				$('.user-logout').css('top','54px');
				$('#userOptions').css({'height': '75px', 'top': '45px'});
				$('#userOptions div').show();
			}
		}
	} else {

		if ($(e.target).parent().parent().attr('id') !== 'userOptions') {
			$('#userOptions').css({'height': '0px', 'top': '35px'});
			$('#userOptions div').hide();
		}
	}
}

function animateProcurement(show, e) {
	if (show) {
		if ($('#procurementOptions').height() < 120 && $('#procurementRequestsList').children().length > 0) {
			$('#procurementOptions').css({'height': '120px', 'top': '45px'});
			$('#procurementOptions ul').show();
			setTimeout(function() {
				$('#procurementOptions ul li').width(0);
				var width = $('#procurementOptions ul')[0].scrollWidth;
				$('#procurementOptions ul li').width(width);
			}, 10);
		}
	} else {
		if ($(e.target).attr('id') != 'procurementOptions' && $(e.target).parent().attr('id') != 'procurementRequestsList') {
			$('#procurementOptions').css({'height': '0px', 'top': '35px'});
			$('#procurementOptions ul').hide();
			setTimeout(function() {
				$('#procurementOptions ul li').width(0);
			}, 10);
		}
	}
}
/*User Extra Options Ends*/



/*Project Hiring changes Starts*/
$(document).ready(function() {
		$('#projectLabourProficiency').on('click', function() {
			if (!$('#projectLabourProficiency-menuid').hasClass('opentravelClaim-menuid')) {
				$('#projectLabourProficiency-menuid').addClass('opentravelClaim-menuid');
			} else {
				$('#projectLabourProficiency-menuid').removeClass('opentravelClaim-menuid');
			}
		});

		$('body').on('click', function(e) {
			if ($(e.target).attr('id') !== "projectLabourProficiency-menuid"  && $(e.target).attr('id') !== "projectLabourProficiency" && $(e.target).attr('id') !== "projectLabourProficiencySearchInput"
				&& $(e.target).attr('id') !== "projectLabourProficiencyList" && !$(e.target).hasClass('lanProf') && !$(e.target).hasClass('langProf') && !$(e.target).hasClass('lanProfHead') && $(e.target).attr('name') !== "langProf"
					&& $(e.target).attr('type') !== "checkbox" && !$(e.target).hasClass('langProfValues1') && !$(e.target).hasClass('langProfValues2') && !$(e.target).hasClass('langProfValues3')) {
				$('#projectLabourProficiency-menuid').removeClass('opentravelClaim-menuid');
			} else {
				if($(e.target).attr('id') == "projectLabourProficiency"){
					if (!$('#projectLabourProficiency-menuid').hasClass('opentravelClaim-menuid')) {
						$('#projectLabourProficiency-menuid').removeClass('opentravelClaim-menuid');
					} else {
						$('#projectLabourProficiency-menuid').addClass('opentravelClaim-menuid');
					}
				}else{
					$('#projectLabourProficiency-menuid').addClass('opentravelClaim-menuid');
				}
			}
		});
});
/*Project Hiring changes Ends*/
/*Labour Changes Starts*/
function getFileNameForLabourSelect(url, appendId) {
	if (!isEmpty(url) && !isEmpty(appendId)) {
		var inkblob, fileName = '';
		if ('' != url) {
			inkblob = { url: url};
			//filepicker.setKey('A6zVM3VmDQhqeTq6sF209z');
			//filepicker.setKey('A7ucPpqRuR46F7OVE8CHJz');
			//filepicker.stat(inkblob, {filename: true},function(metadata) {
			//	fileName=JSON.stringify(metadata.filename);
			filepicker.retrieve(inkblob, { metadata: true }).then(function(response){
	  			fileName=JSON.stringify(response.filename);
				$(appendId).append('<option value="' + url + '">' + fileName + '</option>');
			});
		}
	}
}

function printLabourDetails(ele) {
	var hiringLabourId=$(ele).attr('id');
	 var jsonData = {};
     jsonData.useremail=$("#hiddenuseremail").text();
     jsonData.hiringRequestId=hiringLabourId;
	var url = "/labour/labourDetails";
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
    	  $("#myModal").remove();
    	  var modalToBeDisplayed=displayEmployeeDetails(data);
    	  $("#pendingHiring").append(modalToBeDisplayed);
    	  $("#labourHiring").show();
    	  $('#myModal').attr('data-toggle', 'modal');
    	  $("#myModal").modal('show');
      },
      error: function(xhr, status, error) {
      	if(xhr.status == 401){ doLogout(); }
      }
    });
}

function viewLabourDetails(ele) {
	var hiringLabourId=$(ele).attr('id');
	 var jsonData = {};
    jsonData.useremail=$("#hiddenuseremail").text();
    jsonData.hiringRequestId=hiringLabourId;
	var url = "/labour/labourDetails";
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
    	 console.log(data);
   	  $("#hiringBasicModal").remove();
   	  var modalToBeDisplayed=displayBasicHiringDetails(data);
   	  $("#pendingHiring").append(modalToBeDisplayed);
   	  $("#labourHiring").show();
   	  $('#hiringBasicModal').attr('data-toggle', 'modal');
   	  $("#hiringBasicModal").modal('show');
     },
     error: function(xhr, status, error) {
     	if(xhr.status == 401){ doLogout(); }
     }
   });
}

function displayBasicHiringDetails(individualRecord) {
	var modal = '<div id="hiringBasicModal" class="modal hide fade" data-toggle="modal" tabindex="-1"'+
	    ' style="width:700px; left: 26%;" role="dialog" aria-labelledby="myModalLabel"'+
	    ' aria-hidden="true">' +
	    '<div class="modal-header">' +
	    '<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>' +
	    '<h3 id="myModalLabel">Job Details</h3> </div>' +
	    '<div class="modal-body" style="width:645px;margin-left:5px; overflow: hidden;" id="Print_Div">'+
	    '<table class="table table-bordered" style="width:645px;">'+
	    '<tr><td class="hiringModalHead"><strong>Project Name</strong></td><td class="hiringModalContent">'+ individualRecord['project']['project_name']+'</td></tr>' +
	    '<tr><td class="hiringModalHead"><strong>Project Number</strong></td><td class="hiringModalContent">'+ individualRecord['project']['project_number']+'</td></tr>' +
	    '<tr><td class="hiringModalHead"><strong>Request Type</strong></td><td class="hiringModalContent">'+ individualRecord['project']['request_type']+'</td></tr>' +
	    '<tr><td class="hiringModalHead"><strong>Project</strong></td><td class="hiringModalContent">'+ individualRecord['job']['availableProjects']+'</td></tr>'+
	    '<tr><td class="hiringModalHead"><strong>Project Description</strong></td><td class="hiringModalContent">' + individualRecord['job']['jobDescription']+'</td></tr>' +
	    '<tr><td class="hiringModalHead"><strong>Langauage Proficiency</strong></td><td class="hiringModalContent">'+ individualRecord['job']['jobDetailsLangProf']+'</td></tr>' +
	    '<tr><td class="hiringModalHead"><strong>Experience Required</strong></td><td class="hiringModalContent">' + individualRecord['job']['jobExperience']+'</td></tr>' +
	    '<tr><td class="hiringModalHead"><strong>Job Position</strong></td><td class="hiringModalContent">' + individualRecord['job']['jobPosition']+'</td></tr>' +
	    '<tr><td class="hiringModalHead"><strong>Qualification Required</strong></td><td class="hiringModalContent">' + individualRecord['job']['positionQualification']+'</td></tr>' +
	    '<tr><td class="hiringModalHead"><strong>Branch</strong></td><td class="hiringModalContent">' + individualRecord['job']['requestAllowedForBranches']+'</td></tr>' +
	    '<tr><td class="hiringModalHead"><strong>Start Date</strong></td><td class="hiringModalContent">' + individualRecord['job']['jobStartDate']+'</td></tr>' +
	    '<tr><td class="hiringModalHead"><strong>End Date</strong></td><td class="hiringModalContent">' + individualRecord['job']['jobEndDate']+'</td></tr>' +
	    '<tr><td class="hiringModalHead"><strong>Job Ad Budget</strong></td><td class="hiringModalContent">' + individualRecord['job_ad']['dateOfAd']+'</td></tr>' +
	    '<tr><td class="hiringModalHead"><strong>Job Ad Place</strong></td><td class="hiringModalContent">' + individualRecord['job_ad']['placeOfAd']+'</td></tr>'+
	    '</table></div></div>';
	    return modal;
}

function getLabourData(useremail){
	//alert("getLabourData") ;
	  $.get('/labour/getlabdatas',function(data) {
		  $("#pendingLabourTable tbody").html("");
		  for(var i=0;i<data.labourHiring.length;i++){
		  var id=data.labourHiring[i].id;
	      var projectNumber=data.labourHiring[i].projectNumber;
	      var requester=data.labourHiring[i].requester;
	      var requetType=data.labourHiring[i].requetType;
	      var projectTitle=data.labourHiring[i].projectTitle;
	      var position=data.labourHiring[i].position;
	      var status=data.labourHiring[i].status;
	      var remark=data.labourHiring[i].remarks;
	      var documents=data.labourHiring[i].document;
	      var approverEmailList=data.labourHiring[i].approverEmailList;
	      var userroles=data.labourHiring[i].userroles;
	      var display = '';
	      if (undefined !== remark && null !== remark && "" !== remark) {
	    	    remark = remark.substring(0,remark.length).split(',');
			   	for(var m=0;m<remark.length;m++){
			    	var emailAndRemarks=remark[m].substring(0, remark[m].length).split('#');
			    	display += '<font color="FF00FF"><b>'+emailAndRemarks[0]+'</b></p>#';
			    	if(typeof emailAndRemarks[1]!='undefined'){
			    		display += '<p style="color: blue;">'+emailAndRemarks[1]+'</p><br/>';
			    	}
			    }
		  } else {
	    	  remark = '';
	      }
	      var remarks = '<div style="min-height: 80px; max-height: 80px; max-width: 230px; overflow-y: auto; word-wrap: break-word;" id="hiringRemarkDiv_' + id + '">' + display + '</div>'
	      				+ '<div class="hiringExtra_' + id + '" style="display: none;">Remarks<br/><textarea id="hiringRemarks_' + id + '" style="height: 47px; width: 152px;"></textarea>';
	      var document = '<select id="hiringDocument_' + id + '" style="width: 175px;" onchange="downloadfile(this.value)"><option value="">--Please Select--</option></select>'
				      	+ '<br/><div class="hiringExtra_' + id + '" style="display: none; position: relative; top: 50px;">Upload<br/>'
						+ '<input type="text" style="width:161px;" id="hiringDocuments_' + id + '" name="hiringuploadSuppDocs" readonly="readonly">'
						+ '<input type="button" id="hiringuploadSuppDocs" value="Upload Document" style="width:175px;" class="btn btn-primary btn-idos" onclick="uploadFile(this.id)"></div>';
		 if(userroles.indexOf("CREATOR")!=-1){
			 if(requester==useremail){
				var labourTxnRow=$("#pendingLabourTable tr[id='labourTransaction"+id+"']").attr('id');
				 if(typeof labourTxnRow=='undefined'){
					 $("#pendingLabourTable").append('<tr id="labourTransaction'+id+'"><td class="labour_project">'+projectNumber+'</td><td>'+requetType+'</td><td>'+projectTitle+'</td><td>'+position+'</td><td>' + document + '</td><td>' + remarks + '</td><td id="actionHiringId"></td></tr>');
				 }
			 }
		 }
		 if(userroles.indexOf("APPROVER")!=-1){
			 if(approverEmailList.indexOf(useremail)!=-1){
				 var labourTxnRow=$("#pendingLabourTable tr[id='labourTransaction"+id+"']").attr('id');
				 if(typeof labourTxnRow=='undefined'){
					 $("#pendingLabourTable").append('<tr id="labourTransaction'+id+'"><td class="labour_project">'+projectNumber+'</td><td>'+requetType+'</td><td>'+projectTitle+'</td><td>'+position+'</td><td>' + document + '</td><td>' + remarks + '</td><td id="actionHiringId"></td></tr>');
				 }
			 }
		 }
		 if(userroles.indexOf("CONTROLLER")!=-1){
			 var labourTxnRow=$("#pendingLabourTable tr[id='labourTransaction"+id+"']").attr('id');
			 if(typeof labourTxnRow=='undefined'){
				 $("#pendingLabourTable").append('<tr id="labourTransaction'+id+'"><td class="labour_project">'+projectNumber+'</td><td>'+requetType+'</td><td>'+projectTitle+'</td><td>'+position+'</td><td>' + document + '</td><td>' + remarks + '</td><td id="actionHiringId">'+status+'&nbsp;&nbsp;<span id="' + id + '" class="labour-print" onclick="printLabourDetails(this);"><p style="color: blue;"></p></span></td></tr>');
			 }
			 $("#pendingLabourTable tr[id='labourTransaction"+id+"']").prepend('<td>'+requester+'</td>');
		 }
		 if (undefined !== documents && '' !== documents && null !== documents) {
		      documents = documents.split(',');
		      var optionDocs = '';
		      if (documents.length > 0) {
	    		  $(documents).each(function(k) {
	    			  if ("" !== documents[k] && null !== documents[k]) {
		    			  var doc = documents[k].split('#');
		    			  doc = doc[1];
		    			  getFileNameForLabourSelect(doc, '#hiringDocument_' + id);
	    			  }
	    		  });
		      }
	      }
		 if(userroles.indexOf("CREATOR")!=-1){
	    	  if(requester==useremail){
	    	  if(status=="position_requested"){
	    	   $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('Await Approval&nbsp;&nbsp;<span id="' + id + '" class="labour-view" onclick="viewLabourDetails(this);"><p style="color: blue;"></p></span>');
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
	        }
	        if(status=="employee_identified"){
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('Employee Identified&nbsp;&nbsp;<span id="' + id + '" class="labour-print" onclick="printLabourDetails(this);"><p style="color: blue;"></p></span>');
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
	        }
	        if(status=="position_approved"){
         $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
         $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('<div class="reg-block" id="logged-out-block"><a id='+id+' href="#labourHiring" class="btn-approve btn btn-primary btn-idos" onClick="javascript:doAction(this);" style="height: auto;">Submit Employee Details</a></div>');
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
         $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('<div class="reg-block" id="logged-out-block"><a id='+id+' href="#labourHiring" class="btn-approve btn btn-primary btn-idos" onClick="javascript:action(this);" style="height: auto;">Issue employment agreement</a></div>');
         if(!(userroles.indexOf("APPROVER")!=-1)){
        	 $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').show();
         } else {
		           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
         }
      }
      if(status=="Employee Agreement Issued"){
         $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
         $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('Agreement Issued&nbsp;&nbsp;<span id="' + id + '" class="labour-print" onclick="printLabourDetails(this);"><p style="color: blue;"></p></span>');
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
      }
	      }
	      }
	      if(userroles.indexOf("APPROVER")!=-1){
	      if(approverEmailList.indexOf(useremail)!=-1){
	    	  if($("#pendingLabourTable tr[id='labourTransaction"+id+"']").children().length==7){
		    	 $("#pendingLabourTable tr[id='labourTransaction"+id+"']").prepend('<td>'+requester+'</td>');
		      }
	    	  if(status=="position_requested"){
	    	   $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('<div class="reg-block" id="logged-out-block"><a id='+id+' href="#labourHiring" class="btn-approve btn btn-primary btn-idos" onClick="javascript:doAction(this);" style="height: auto;">Approve Position</a></div>&nbsp;&nbsp;<span id="' + id + '" class="labour-view" onclick="viewLabourDetails(this);"><p style="color: blue;"></p></span>');
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').show();
	        }
	        if(status=="employee_identified"){
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('<div class="reg-block" id="logged-out-block"><a id='+id+' data-toggle="modal" href="#labourHiring" class="btn-approve btn btn-primary btn-idos" onClick="javascript:action(this);" style="height: auto;">Obtain client approval</a></div>');
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
	        }
	        if(status=="position_approved"){
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('Position Approved');
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
	        }
	        if(status=="client_approval_sent"){
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('<div class="reg-block" id="logged-out-block"><a id='+id+' href="#labourHiring" class="btn-approve btn btn-primary btn-idos" onClick="javascript:action(this);" style="height: auto;">Click Once Client Approves</a></div>');
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').show();
	        }
	        if(status=="client_approved"){
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('Client Approved&nbsp;&nbsp;<span id="' + id + '" class="labour-print" onclick="printLabourDetails(this);"><p style="color: blue;"></p></span>');
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
	        }
	        if(status=="Employee Agreement Issued"){
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('Agreement Issued&nbsp;&nbsp;<span id="' + id + '" class="labour-print" onclick="printLabourDetails(this);"><p style="color: blue;"></p></span>');
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
	        }
	      }
	      }
		  }
	  });
}
/*Labour Changes Ends*/

$(document).ready(function(){
	$('.hiredLabourFreeTextSearchButton').on('click', function() {
		var hiredLabourFreeTextSearch=$("#hiredLabourFreeTextSearch").val();
		var jsonData = {};
		jsonData.userEmail = $("#hiddenuseremail").text();
		jsonData.hiredLabourFreeTextSearchStr=hiredLabourFreeTextSearch;
		ajaxCall('/labour/searchHiredLabour', jsonData, '', '', '', '', 'searchHiredLabourSuccess', '', true);
	});
});

function searchHiredLabourSuccess(data){
	 $("#pendingLabourTable tbody").html("");
	 var useremail=$("#hiddenuseremail").text();
	 for(var i=0;i<data.labourHiring.length;i++){
		  var id=data.labourHiring[i].id;
	      var projectNumber=data.labourHiring[i].projectNumber;
	      var requester=data.labourHiring[i].requester;
	      var requetType=data.labourHiring[i].requetType;
	      var projectTitle=data.labourHiring[i].projectTitle;
	      var position=data.labourHiring[i].position;
	      var status=data.labourHiring[i].status;
	      var remark=data.labourHiring[i].remarks;
	      var documents=data.labourHiring[i].document;
	      var approverEmailList=data.labourHiring[i].approverEmailList;
	      var userroles=data.labourHiring[i].userroles;
	      var display = '';
	      if (undefined !== remark && null !== remark && "" !== remark) {
	    	    remark = remark.substring(0,remark.length).split(',');
			   	for(var m=0;m<remark.length;m++){
			    	var emailAndRemarks=remark[m].substring(0, remark[m].length).split('#');
			    	display += '<font color="FF00FF"><b>'+emailAndRemarks[0]+'</b></p>#';
			    	if(typeof emailAndRemarks[1]!='undefined'){
			    		display += '<p style="color: blue;">'+emailAndRemarks[1]+'</p><br/>';
			    	}
			    }
		  } else {
	    	  remark = '';
	      }
	      var remarks = '<div style="min-height: 80px; max-height: 80px; max-width: 230px; overflow-y: auto; word-wrap: break-word;" id="hiringRemarkDiv_' + id + '">' + display + '</div>'
	      				+ '<div class="hiringExtra_' + id + '" style="display: none;">Remarks<br/><textarea id="hiringRemarks_' + id + '" style="height: 47px; width: 152px;"></textarea>';
	      var document = '<select id="hiringDocument_' + id + '" style="width: 175px;" onchange="downloadfile(this.value)"><option value="">--Please Select--</option></select>'
				      	+ '<br/><div class="hiringExtra_' + id + '" style="display: none; position: relative; top: 50px;">Upload<br/>'
						+ '<input type="text" style="width:161px;" id="hiringDocuments_' + id + '" name="hiringuploadSuppDocs" readonly="readonly">'
						+ '<input type="button" id="hiringuploadSuppDocs" value="Upload Document" style="width:175px;" class="btn btn-primary btn-idos" onclick="uploadFile(this.id)"></div>';
		 if(userroles.indexOf("CREATOR")!=-1){
			 if(requester==useremail){
			 	$("#pendingLabourTable").append('<tr id="labourTransaction'+id+'"><td>'+projectNumber+'</td><td>'+requetType+'</td><td>'+projectTitle+'</td><td>'+position+'</td><td>' + document + '</td><td>' + remarks + '</td><td id="actionHiringId"></td></tr>');
			 }
		 }
		 if(userroles.indexOf("APPROVER")!=-1){
			 if(approverEmailList.indexOf(useremail)!=-1){
			 	$("#pendingLabourTable").append('<tr id="labourTransaction'+id+'"><td>'+projectNumber+'</td><td>'+requetType+'</td><td>'+projectTitle+'</td><td>'+position+'</td><td>' + document + '</td><td>' + remarks + '</td><td id="actionHiringId"></td></tr>');
			 }
		 }
		 if(userroles.indexOf("CONTROLLER")!=-1){
			 $("#pendingLabourTable").append('<tr id="labourTransaction'+id+'"><td>'+projectNumber+'</td><td>'+requetType+'</td><td>'+projectTitle+'</td><td>'+position+'</td><td>' + document + '</td><td>' + remarks + '</td><td id="actionHiringId">'+status+'&nbsp;&nbsp;<span id="' + id + '" class="labour-print" onclick="printLabourDetails(this);"><p style="color: blue;"></p></span></td></tr>');
			 $("#pendingLabourTable tr[id='labourTransaction"+id+"']").prepend('<td>'+requester+'</td>');
		 }
		 if (undefined !== documents && '' !== documents && null !== documents) {
		      documents = documents.split(',');
		      var optionDocs = '';
		      if (documents.length > 0) {
	    		  $(documents).each(function(k) {
	    			  if ("" !== documents[k] && null !== documents[k]) {
		    			  var doc = documents[k].split('#');
		    			  doc = doc[1];
		    			  getFileNameForLabourSelect(doc, '#hiringDocument_' + id);
	    			  }
	    		  });
		      }
	      }
		 if(userroles.indexOf("CREATOR")!=-1){
	    	  if(requester==useremail){
	    	  if(status=="position_requested"){
	    	   $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('Await Approval&nbsp;&nbsp;<span id="' + id + '" class="labour-view" onclick="viewLabourDetails(this);"><p style="color: blue;"></p></span>');
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
	        }
	        if(status=="employee_identified"){
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('Employee Identified&nbsp;&nbsp;<span id="' + id + '" class="labour-print" onclick="printLabourDetails(this);"><p style="color: blue;"></p></span>');
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
           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('Agreement Issued&nbsp;&nbsp;<span id="' + id + '" class="labour-print" onclick="printLabourDetails(this);"><p style="color: blue;"></p></span>');
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
        }
	      }
	      }
	      if(userroles.indexOf("APPROVER")!=-1){
	      if(approverEmailList.indexOf(useremail)!=-1){
	    	  if($("#pendingLabourTable tr[id='labourTransaction"+id+"']").children().length==7){
		    	 $("#pendingLabourTable tr[id='labourTransaction"+id+"']").prepend('<td>'+requester+'</td>');
		      }
	    	  if(status=="position_requested"){
	    	   $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('<div class="reg-block" id="logged-out-block"><a id='+id+' href="#labourHiring" class="btn-approve btn btn-primary btn-idos" onClick="javascript:doAction(this);">Approve Position</a></div>&nbsp;&nbsp;<span id="' + id + '" class="labour-view" onclick="viewLabourDetails(this);"><p style="color: blue;"></p></span>');
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
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('Client Approved&nbsp;&nbsp;<span id="' + id + '" class="labour-print" onclick="printLabourDetails(this);"><p style="color: blue;"></p></span>');
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
	        }
	        if(status=="Employee Agreement Issued"){
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').html("");
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('td[id="actionHiringId"]').append('Agreement Issued&nbsp;&nbsp;<span id="' + id + '" class="labour-print" onclick="printLabourDetails(this);"><p style="color: blue;"></p></span>');
	           $("#pendingLabourTable tr[id='labourTransaction"+id+"']").find('div[class="hiringExtra_' + id + '"]').hide();
	        }
	      }
	      }
		  }
}

/*View All Project Hiring Starts*/
$(document).ready(function() {
	$('#viewAllProjectHiringPositions').on('click', function() {
		var jsonData = {};
		jsonData.email = $("#hiddenuseremail").text();
		ajaxCall('/project/getAllHirings', jsonData, '', '', '', '', 'getAllHiringSuccess', '', false);
		var existingProjectSearchSpan=$("#projectSetup span[id='projectHiringFreeTextSearchButtonSpan']").attr('id');
		if (typeof existingProjectSearchSpan == 'undefined') {
			var strHtml = '<span id="projectHiringFreeTextSearchButtonSpan" style="display: none;"><button id="projectHiringFreeTextSearchButtonSpan" class="projectHiringFreeTextSearchButtonSpan btn btn-primary btn-idos" title="Search Project Hiring Positions" style="float:right;margin-left:5px;">Search Project Hiring Positions</button>'
                + '<input type="text" class="input-xlarge search-image" placeholder="Search Project Hiring Postions" name="projectHiringFreeTextSearch" id="projectHiringFreeTextSearch" style="float:right;margin-top: -1px; margin-right:-1px;width: 280px;"></span>';
			$('#projectSetup .actions #newProjectform-container').after(strHtml);
		}
		$("#projectSetup span[id='projectFreeTextSearchButtonSpan']").hide();
		$("#projectSetup span[id='projectHiringFreeTextSearchButtonSpan']").fadeIn(3000);
		$('#projectHiringTable').slideDown();
		$('table#projectTable').parent().parent().slideUp();
		$('#project-form-container:visible').slideUp();
	});

	$('#projectHiringTableClose').on('click', function() {
		hideProjectHiringTable();
	});

	$('body').on('click', '#projectHiringFreeTextSearchButtonSpan', function() {
		var jsonData = {};
		jsonData.email = $("#hiddenuseremail").text();
		jsonData.search = $('#projectHiringFreeTextSearch').val();
		ajaxCall('/project/getAllHirings', jsonData, '', '', '', '', 'getAllHiringSuccess', '', false);
	});
});

function hideProjectHiringTable() {
	$("#projectSetup span[id='projectHiringFreeTextSearchButtonSpan']").hide();
	$("#projectSetup span[id='projectFreeTextSearchButtonSpan']").fadeIn(3000);
	$('table#projectTable').parent().parent().slideDown();
	$('#projectHiringTable').slideUp();
}

function getAllHiringSuccess(data) {
	data = data.result;
	if (data.length > 0) {
		$('#projectHiringExcelFormTable #projectHiringTableBody').empty();
		for (var i = 0; i < data.length; i++) {
			$('#projectHiringExcelFormTable #projectHiringTableBody').append('<tr>'
				+ '<td>' + data[i].projectName + '</td><td>' + data[i].projectNumber + '</td>'
				+ '<td>' + data[i].startDate + '</td><td>' + data[i].endDate + '</td>'
				+ '<td>' + data[i].position + '</td><td>' + data[i].location + '</td><td>' + data[i].status + '</td></tr>');
		}
	}
}
/*View All Project Hiring Ends*/
/*Customer Setting Starts*/
function openCustomerPrivacySetting() {
	var areaCodesLen = $('#settingPhoneCode').children().length;
	if (areaCodesLen <= 1) {
		ajaxCall('/app/getPhoneCountry', '', '', '', 'GET', '', 'populateCountryAreaCodes', '', false);
	}
	var accountEmail=GetURLParameter('accountEmail');
	var email=accountEmail.substring(0, accountEmail.length);
	var accountOrganization=GetURLParameter('accountOrganization');
	var orgId=accountOrganization.substring(0, accountOrganization.length);
	var entityType=GetURLParameter('entityType');
	var entity=entityType.substring(0, entityType.length);
	var jsonData = {};
	if(entity=="Vendor"){
		jsonData.type = 1;
	}else{
		jsonData.type = 2;
	}
	jsonData.email = email;
	jsonData.orgId = orgId;
	ajaxCall('/privacy/customerDetails', jsonData, '', '', '', '', 'customerDetailsSuccess', '', false);
	$('#userOptions:visible').css({'height': '0px', 'top': '35px'});
    $('#userOptions div:visible').hide();
}

function customerDetailsSuccess(data) {
	if (data.result) {
		$('#settingUserId').val(data.id);
		$('#settingName').val(data.name);
		$('#disName').html(data.name);
		$('#settingEmail, #settingUserEmail').val(data.email);
		$('#disEmail').html(data.email);
		$('#disOrg').html(data.org);
		$('#settingLocation').val(data.location);
		$('#settingCountryCode').val(data.country);
		$('#settingPhoneCode').val(data.phoneCode);
		$('#settingPhone1').val(data.phone1);
		$('#settingPhone2').val(data.phone2);
		$('#settingPhone3').val(data.phone3);
		$('#settingAddress').val(data.address);
		$('#vendCustTransactions, .common-rightpanel').hide();
		$('#privaySettingDiv, #generalCustomerSetting_content').fadeIn();
	} else{
		swal('Error!!','Unable to fetch the details. Please try again later.','error');
	}
}

function populateCountryAreaCodes(data) {
	if (data.result) {
		var codes = data.codes;
		var children = $('#settingPhoneCode').children().length;
		if (codes.length > 0 && children == 1) {
			for (var i = 0; i < codes.length; i++) {
				$('#settingPhoneCode').append('<option value="' + codes[i].code + '">' + codes[i].country + '</option>');
			}
		}
		codes = data.countries;
		children = $('#settingCountryCode').children().length;
		if (codes.length > 0 && children == 1) {
			for (var i = 0; i < codes.length; i++) {
				$('#settingCountryCode').append('<option value="' + codes[i].code + '">' + codes[i].country + '</option>');
			}
		}
	}
}

$(document).ready(function() {
	$('a#vendCustTransactionId').on('click', function() {
		$('#privaySettingDiv').hide();
		$('#supportCenter').hide();
		$('#vendCustTransactions').fadeIn();
	});

	$('.common-image-leftpanel').on('click', function() {
		var id = $(this).attr('id');
		$('.common-rightpanel').hide();
		$('#' + id + '_content').fadeIn();
	});

	$('#settingSaveCustomerProfile').on('click', function() {
		var id = $('#settingUserId').val();
		var oldEmail = $('#settingUserEmail').val();
		var newEmail = $('#settingEmail').val();
		var accountOrganization=GetURLParameter('accountOrganization');
		var orgId=accountOrganization.substring(0, accountOrganization.length);
		if (!isEmpty(id) && !isEmpty(oldEmail) && !isEmpty(newEmail) && !isEmpty(orgId)) {
			var phone = $('#settingPhoneCode').val() + '-' + $('#settingPhone1').val() + $('#settingPhone2').val() + $('#settingPhone3').val();
			var jsonData = {};
			var entityType=GetURLParameter('entityType');
			var entity=entityType.substring(0, entityType.length);
			if(entity=="Vendor"){
				jsonData.type = 1;
			}else{
				jsonData.type = 2;
			}
			jsonData.id = id;
			jsonData.oldEmail = oldEmail;
			jsonData.newEmail = newEmail;
			jsonData.orgId = orgId;
			jsonData.name = $('#settingName').val();
			jsonData.location = $('#settingLocation').val();
			jsonData.country = $('#settingCountryCode').val();
			jsonData.phoneCode = $('#settingPhoneCode').val();
			jsonData.phone = phone;
			jsonData.address = $('#settingAddress').val();
			ajaxCall('/privacy/updateCustomerDetails', jsonData, '', '', '', '', 'saveCustomerDetailsSuccess', '', false);
		}
	});

	$('#settingSaveVendorProfile').on('click', function() {
		var id = $('#settingUserId').val();
		var email = $('#settingUserEmail').val();
		if (!isEmpty(id) && !isEmpty(email)) {
			var jsonData = {};
			jsonData.id = id;
			jsonData.email = email;
			jsonData.name = $('#settingName').val();
			var phone = $('#settingPhoneCode').val() + '-' + $('#settingPhone1').val() + $('#settingPhone2').val() + $('#settingPhone3').val();
			jsonData.phone = phone;
			jsonData.reg = $('#settingRegNo').val();
			ajaxCall('/privacy/updateVendorDetails', jsonData, '', '', '', '', 'saveVendorDetailsSuccess', '', false);
		}
	});

	$('.settingSaveVendorProfileExit').on('click', function() {
		$('#privaySettingDiv').hide();
		$('#vendSellerAccounts').fadeIn();
	});
});

function saveVendorDetailsSuccess(data) {
	if (data.result) {
		swal('Success!','Details updated successfully.','success');
		vendorSettingDetailsSuccess(data);
	} else {
		swal('Error!','Problem in updation. Please try again later.','error');
	}
}

function saveCustomerDetailsSuccess(data) {
	if (data.result) {
		swal('Success!','Details updated successfully.','success');
		customerDetailsSuccess(data);
	} else {
		swal('Error!','Problem in updation. Please try again later.','error');
	}
}

function openVendorPrivacySetting() {
	var areaCodesLen = $('#settingPhoneCode').children().length;
	if (areaCodesLen <= 1) {
		ajaxCall('/app/getPhoneCountry', '', '', '', 'GET', '', 'populateCountryAreaCodes', '', false);
	}
	var email=$('#settingUserEmail').val();
	var orgId=$('#settingOrgId').val();
	var jsonData = {};
	jsonData.type = 1;
	jsonData.email = email;
	jsonData.orgId = orgId;
	ajaxCall('/privacy/vendorDetails', jsonData, '', '', '', '', 'vendorSettingDetailsSuccess', '', false);
	$('#userOptions:visible').css({'height': '0px', 'top': '35px'});
    $('#userOptions div:visible').hide();
}

function vendorSettingDetailsSuccess(data) {
	if (data.result) {
		$('#settingUserId').val(data.id);
		$('#settingName').val(data.name);
		$('#disName').html(data.name);
		$('#settingUserEmail').val(data.email);
		$('#settingLastLogin').html(data.lastLogin);
		if (!isEmpty(data.lastPwdChange)) {
			var days = data.lastPwdChangeDays, disDays = 'day';
			if (days > 1) {
				disDays += 's';
			}
			$('#settingPwdChange').html(data.lastPwdChange + ' (' + days + ' ' + disDays + ') ago.');
		} else {
			$('#settingPwdChange').html('Never.');
		}
		$('#disEmail').html(data.email);
		$('#settingPhoneCode').val(data.phoneCode);
		$('#settingPhone1').val(data.phone1);
		$('#settingPhone2').val(data.phone2);
		$('#settingPhone3').val(data.phone3);
		$('#settingRegNo').val(data.reg);
		$('#vendSellerAccounts, .common-rightpanel').hide();
		$('#privaySettingDiv, #generalCustomerSetting_content').fadeIn();
	} else{
		swal('Error!!','Unable to fetch the details. Please try again later.','error');
	}
}

function generateCustomerInvoice(elem) {
	//$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var formName=$(elem).parent().attr('name');
	var id=formName.substring(11, formName.length);
	var email = $(elem).attr('email');
	var url=$("#"+formName+"").attr('action');
	var jsonData = {};
	var useremail=$("#hiddenuseremail").text();
	jsonData.usermail = email;
	jsonData.entityTxnId=id;
	jsonData.exportType = "pdf";
	downloadFile(url, "POST", jsonData, "Error on invoice generation!");
}
/*Customer Setting Ends*/



/*Notes Starts*/
function getNotesCountSuccess(data) {
	if (!isEmpty(data)) {
		$('#notesSharedCount').html(data.sharedNotes);
		$('#notesTotalCount').html(data.totalNotes);
	}
}
function openNotes(){
	var jsonData = {};
	jsonData.email = $("#hiddenuseremail").text();
	jsonData.role = $("#userRoleHidden").val();
	ajaxCall('/notes/getUsersAndProjects', jsonData, '', '', '', '', 'notesUsersProjectsSuccess', '', false);
}
$(document).ready(function() {
	$('#notesTotalCount, #notesSharedCount').on('click', function() {
		$('#notesCases').trigger('click');
	});

	$('#notesIcon').on('click', function(){
		if (!$('#createNewNote').is(':visible')) {
			$('#notesSearch').val('');
			$("#noteSubject").val('');
			$("#noteMessage").val('');
			$('#notesFilter option:first').prop('selected', true);
			if($('#supportCenter').is(':visible')){
				$('#supportCenter').slideUp('slow', function(){
					$('#createNewNote').slideDown('slow', function(){
						openNotes();
					});
					$('#supportMyCases').css({'width': '0', 'min-width': '0'});
					$('#supportExtra').css({'top': '0'});
					$('#supportHistory').hide();
				});
			}else{
				$('#createNewNote').slideDown('slow', function(){
					openNotes();
				});
			}
		} else {
			$('#createNewNote').slideUp('slow', function(){
				animateNotesTable(false);
				$('#notesRemarks').hide();
			});
		}
	});

	$('#notesSend').on('click', function () {
		var sub = $('#noteSubject').val();
		var note = $('#noteMessage').val();
		if (isEmpty(sub)) {
			swal('Invalid data field!!','Please provide a subject.','error');
		} else if (isEmpty(note)) {
			swal('Invalid data field!!','Please provide the note.','error');
		} else {
			var json = {};
			json.email = $("#hiddenuseremail").text();
			json.id = $('#noteId').val();
			json.sub = sub;
			json.note = note;
			json.file = $('#noteFileUrl').val();
			json.transaction = $('#notesTransactions').val();
			var users = $('#notesUsersUl input[class="notesUsersCheck"]:checked').map(function() {
				return this.value;
			}).get();
			json.project = $('#notesProjects').val();
			json.branch = $('#notesBranches').val();
			json.users = users.join();
			ajaxCall('/notes/saveNote', json, '', '', '', '', 'saveNoteSuccess', '', true);
		}
	});

	$('#notesCases').on('click', function() {
		if($('#notesMyCases').width() === 0) {
			var jsonData = {};
			jsonData.email = $("#hiddenuseremail").text();
			ajaxCall('/notes/getNotes', jsonData, '', '', '', '', 'getNotesSuccess', '', true);
			animateNotesTable(true);
		} else {
			animateNotesTable(false);
		}
	});

	$('body').on('click', '.editNotes', function () {
		if (!isEmpty(this.id)) {
			var id = this.id;
			id = id.split('_');
			id = id[1];
			var json = {};
			json.email = $("#hiddenuseremail").text();
			json.id = id;
			ajaxCall('/notes/getNoteById', json, '', '', '', '', 'editNoteSuccess', '', true);
		} else {
			swal('Error!!','Something went wrong. Please try again later.','error');
		}
	});

	$('body').on('click', '.notesHistory', function () {
		if (!isEmpty(this.id)) {
			var id = this.id;
			id = id.split('_');
			id = id[1];
			var json = {};
			json.email = $("#hiddenuseremail").text();
			json.id = id;
			ajaxCall('/notes/getNoteById', json, '', '', '', '', 'noteRemarksSuccess', '', true);
		} else {
			swal('Error!!','Something went wrong. Please try again later.','error');
		}
	});

	$('#addRemark').on('click', function () {
		if (!$('#remarkArea').is(':visible')) {
			$('#remarkArea').find('input[type="text"], textarea').val('');
			$('#remarkArea').slideDown();
		} else {
			$('#remarkArea').slideUp();
		}
	});

	$('#noteRemarkSend').on('click', function () {
		var remark = $('#userRemark').val();
		if (!isEmpty(remark)) {
			var json = {};
			json.email = $("#hiddenuseremail").text();
			json.id = $('#noteRemarkId').val();
			json.remark = remark;
			json.file = $('#remarkUploadUrl').val();
			ajaxCall('/notes/addRemark', json, '', '', '', '', 'addRemarkSuccess', '', true);
		} else {
			swal('Invalid data field!!','Please provide a remark.','error');
		}
	});

	$('#notesFilter').on('change', function () {
		searchNotes();
	});

	$('#notesSearch').on('keyup', function () {
		searchNotes();
	});

	$('body').on('click', 'li.notesuserLi', function () {
		var length = $('#notesUsersUl input[class="notesUsersCheck"]:checked').map(function() {
			return this.value;
		}).get().length;
		if (length > 0) {
			$('#notesUsers').html(length + ' Selected &#8711;');
		} else {
			$('#notesUsers').html('None Selected &#8711;');
		}
	});
});

function searchNotes() {
	var json = {};
	json.email = $("#hiddenuseremail").text();
	json.keyword = $('#notesSearch').val();
	json.days = $('#notesFilter').val();
	ajaxCall('/notes/search', json, '', '', '', '', 'searchNotesSuccess', '', true);
}

function searchNotesSuccess(data) {
	if (data.result) {
		$('#notesMyCasesTable').empty();
		data = data.notes;
		if (!isEmpty(data) && data.length > 0) {
			for (var i = 0; i < data.length; i++) {
				populateNote(data[i], false);
			}
		}
	}
}

function addRemarkSuccess(data) {
	if (data.result) {
		fillData.populateRemarks(data);
		$('#remarkArea').find('input[type="text"], textarea').val('');
		$('#remarkArea').slideUp();
	} else {
		swal("Error!",data.message,"error");
	}
}

function noteRemarksSuccess(data) {
	(data.result) ? fillData.noteRemarks(data) : swal("Error!",data.message,"error");
}

var fillData = {
	notes	: function (data) {
		$('#noteId').val(data.id);
		$('#noteSubject').val(data.subject);
		$('#noteMessage').val(data.note);
		$('#notesProjects').val(data.projectId);
		$('#notesBranches').val(data.branchId);
		$('#notesTransactions').val(data.transaction);
		if (data.users.length > 0) {
			for (var i = 0; i < data.users.length; i++) {
				$('#notesUsersUl').find('input[value="' + data.users[i].email + '"]').prop('checked', true);
			}
			$('#notesUsers').html(data.users.length + ' Selected &#8711;');
		}
	},
	noteRemarks : function (data) {
		$('#remarkArea').find('input[type="text"], textarea').val('');
		$('#remarkArea').slideUp();
		$('#noteRefNumber').html(data.refNumber);
		$('#noteTransactionref').html(data.transactionRef);
		$('#noteRemarkId').val(data.id);
		$('#noteCreated').html(data.created);
		$('#noteCreatedBy').html(data.createdBy);
		$('#noteModified').html(data.modified);
		$('#noteModifiedBy').html(data.modifiedBy);
		$('#notesBodyView').html(data.note);
		this.populateRemarks(data);
		$('#notesRemarks').fadeIn();
	},
	populateRemarks : function (data, id) {
		if (!isEmpty(data.remarks) && data.remarks.length > 0) {
			$('#noteRemarks').empty();
			var remarks = data.remarks;
			for (var i = 0; i < remarks.length; i++) {
				$('#noteRemarks').append('<div class="comments-replies">'
						+ '<span>' + remarks[i].email + '</span>'
						+ '<span class="reported-time">' + remarks[i].created + '</span><br/>'
						+ '<span><font>' + remarks[i].remark + '</p></span><br/>'
						+ '<span>Attachment : </span><span class="download-file" title="Download File" id="remark_' + i + '"></span></div>');
				getFileNameForSupport(remarks[i].remarkAttachment, '#remark_' + i);
			}
		} else {
			$('#noteRemarks').html('No remarks recorded yet. Be the first one to write a remark.');
		}
	}
};

function editNoteSuccess(data) {
	(data.result) ? fillData.notes(data) : swal("Error!",data.message,"error");
}

function animateNotesTable(show) {
	(show) ? $('#notesMyCases').animate({'width': '52%', 'min-width': '700px'}, 1500) :	$('#notesMyCases').animate({'width': '0', 'min-width': '0'}, 1500);
}

function saveNoteSuccess(resp) {
	if (resp.result) {
		($('#notesMyCases').width() > 0) ? populateNote(resp, true) : $('#notesMsg').html('Your note has been saved. Note Reference Number is <br/><span style="color:red; font-size: 18px;">' + resp.refNumber + '</span>');
		$('#notesTable').find('input[type="text"], textarea').val('');
		$('#notesTable select').find('option:first').prop('selected', true);
		$('#notesTable').find('input[class="notesUsersCheck"]').prop('checked', false);
		$('#notesUsers').html('None Selected &#8711;');
	} else {
		swal('Error!','Something went wrong. Please try again later.','error');
	}
}

function getNotesSuccess(resp) {
	if (resp.result) {
		$('#notesMyCasesTable').empty();
		var result = resp.projects[0], data;
		if (!isEmpty(result.createdNotes)) {
			data = result.createdNotes;
			if (data.length > 0) {
				for (var i = 0; i < data.length; i++) {
					populateNote(data[i], false);
				}
			}
		}
		result = resp.projects[1];
		if (!isEmpty(result.sharedNotes)) {
			data = result.sharedNotes;
			if (data.length > 0) {
				for (var i = 0; i < data.length; i++) {
					populateNote(data[i], false);
				}
			}
		}
	} else {
		swal('Error!','Something went wrong. Please try again later.','error');
	}
}

function populateNote(data, isPrepend) {
	var sharedUsers = '', edit = '';
	if (!isEmpty(data.users) && data.users.length > 0) {
		for (var i = 0; i < data.users.length; i++) {
			sharedUsers += '<option>' + data.users[i].email + '</option>';
		}
	}
	if ($("#hiddenuseremail").text() === data.createdBy) {
		edit = '<span id="edit_' + data.id + '" class="editNotes">Edit</span>';
	}
	var html = '<tr id="' + data.id + '">'
			 + '<td><span class="notesHistory" id="note_' + data.id + '">' + data.refNumber + '</span></td>'
			 + '<td>' + data.subject + '</td>'
			 + '<td><select style="width: 100px"><option>-----</option>' + sharedUsers + '</select></td>'
			 + '<td><span>BRANCH:</span><br/>' + data.branchName + '<br/><span>PROJECT:</span><br/>' + data.projectName + '</td>'
			 + '<td>' + data.created + '</td>'
			 + '<td>' + edit + '<td></tr>';
	if (isPrepend) {
		$('#notesMyCasesTable #' + data.id).remove();
		$('#notesMyCasesTable').prepend(html);
	} else {
		$('#notesMyCasesTable').append(html);
	}
}

function notesUsersProjectsSuccess(resp) {
	if (resp.result) {
		var data = resp.projects;
		appendProjectsUsersBranchesOfNotes(data, '#notesProjects');
		data = resp.branches;
		appendProjectsUsersBranchesOfNotes(data, '#notesBranches');
		data = resp.users[0].result;
		if (!isEmpty(data)) {
			var value = '';
			$('#notesUsersUl').empty();
            $("#notesUsers").text("None Selected");
			$("#notesUsers").append("&nbsp;&nbsp;<b class='caret'></b>");
			for (var i = 0; i < data.length; i++) {
				value = data[i].itemName;
				value = $.trim(value.substring(value.indexOf('(') + 1, value.length - 1));
				if (value !== $("#hiddenuseremail").text()) {
					$('#notesUsersUl').append('<li class="notesuserLi">&nbsp;&nbsp;<input type="checkbox" style="margin-top: -2px;" class="notesUsersCheck" value="' + value + '" />&nbsp;&nbsp;<span class="notesUsersSpan">' + data[i].itemName + '</span></li>');
				}
			}
		}
		if (resp.isTransaction) {
			$('#notesTransactions').html('<option value="">--Please Select--</option>');
			data = resp.transactions[0].result;
			if (data.length >0) {
				$('#notesTransactions').append('<optgroup label="Transactions">');
				for(var i=0;i<data.length;i++){
					$('#notesTransactions').append('<option value="txn_' + data[i].itemId + '">' + data[i].itemName + '</option>');
				}
			}
			data = resp.claimTransactions[0].result;
			if (data.length >0) {
				$('#notesTransactions').append('<optgroup label="Claim Transactions">');
				for(var i=0;i<data.length;i++){
					$('#notesTransactions').append('<option value="clm_' + data[i].itemId + '">' + data[i].itemName + '</option>');
				}
			}
		}
	}
}

function appendProjectsUsersBranchesOfNotes(data, appendId) {
	if (!isEmpty(data) && data.length > 0) {
		data = data[0];
		if (!isEmpty(data)) {
			data = data.result;
			if (!isEmpty(data) && !isEmpty(appendId) && data.length > 0) {
				if (!$(appendId).attr('multiple')) {
					$(appendId).html('<option value="">--Please Select--</option>');
				}
				for (var i = 0; i < data.length; i++) {
					if (data[i].itemName.indexOf($("#hiddenuseremail").text()) < 0) {
						$(appendId).append('<option value="' + data[i].itemId + '">' + data[i].itemName + '</option>');
					}
				}
			}
		}
	}
}
/*Notes Ends*/

//function to check approver accountants for the smmoth transaction processing start
function checkForOnlyConfiguredApprover(specificsId,branchId,txnNetAmount,txnParameter){
	var jsonData = {};
	jsonData.email =$("#hiddenuseremail").text();
	jsonData.txnItemId=specificsId;
	jsonData.txnBranchId=branchId;
	jsonData.transactionNetAmount=txnNetAmount;
	jsonData.transactionParameter=txnParameter;
	var url="/transaction/ruleBasedUserExistence";
	var bool;
	$.ajax({
		url: url,
		data:JSON.stringify(jsonData),
		type:"text",
		method:"POST",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		async:false,
		contentType:'application/json',
		success: function (data) {
			if(data.result){
				var userExistence=data.transactionRuleBasedExistence[0].userExistence;
				if(userExistence=="Exist"){
					bool= true;
				}
				if(userExistence=="Does Not Exist"){
					swal("Warning!!","There Is No Any Approver Configured For Approval Process.Once Approver Is Configured You Can Proceed With The Transaction.","error");
					$(".btn-custom").removeAttr("disabled");
					$(".btn-customred").removeAttr("disabled");
					$(".approverAction").removeAttr("disabled");
					$("#completeTxn").removeAttr("disabled");
					bool= false;
				}
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); } else if(xhr.status == 500){
				swal("Error!", "Please retry, if problem persists contact support team", "error");
			}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
	return bool;
}

function checkForOnlyConfiguredApproverPSAATV(creditVendor,txnNetAmount,txnParameter){
	var jsonData = {};
	jsonData.email =$("#hiddenuseremail").text();
	jsonData.txnCreditVendor=creditVendor;
	jsonData.transactionNetAmount=txnNetAmount;
	jsonData.transactionParameter=txnParameter;
	var url="/transaction/ruleBasedUserExistence";
	var bool;
	$.ajax({
		url: url,
		data:JSON.stringify(jsonData),
		type:"text",
		method:"POST",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		async:false,
		contentType:'application/json',
		success: function (data) {
			if(data.result){
				var userExistence=data.transactionRuleBasedExistence[0].userExistence;
				if(userExistence=="Exist"){
					bool= true;
				}
				if(userExistence=="Does Not Exist"){
					swal("Warning!","There Is No Any Approver or Accountant Configured For Approval/Accounting Process.Once Approver/Accountant Is Configured You Can Proceed With The Transaction.","warning");
					$(".btn-custom").removeAttr("disabled");
					$(".btn-customred").removeAttr("disabled");
					$(".approverAction").removeAttr("disabled");
					$("#completeTxn").removeAttr("disabled");
					bool= false;
				}
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); } else if(xhr.status == 500){
				swal("Error!", "Please retry, if problem persists contact support team", "error");
			}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
	return bool;
}

function checkForConfiguredApproverInventoryTransfer(transferFromBranch,inventoryItem,txnParameter){
	var jsonData = {};
	jsonData.email =$("#hiddenuseremail").text();
	jsonData.txnBranchId=transferFromBranch;
	jsonData.txnItemId=inventoryItem;
	jsonData.transactionParameter=txnParameter;
	var url="/transaction/ruleBasedUserExistence";
	var bool;
	$.ajax({
		url: url,
		data:JSON.stringify(jsonData),
		type:"text",
		method:"POST",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		async:false,
		contentType:'application/json',
		success: function (data) {
			if(data.result){
				var userExistence=data.transactionRuleBasedExistence[0].userExistence;
				if(userExistence=="Exist"){
					bool= true;
				}
				if(userExistence=="Does Not Exist"){
					swal("Warning!","There is no Approver Configured For Approval Process. Once Approver Is Configured You Can Proceed With The Transaction.","warning");
					$(".btn-custom").removeAttr("disabled");
					$(".btn-customred").removeAttr("disabled");
					$(".approverAction").removeAttr("disabled");
					$("#completeTxn").removeAttr("disabled");
					bool= false;
				}
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); } else if(xhr.status == 500){
				swal("Error!", "Please retry, if problem persists contact support team", "error");
			}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
	return bool;
}

function checkForOnlyConfiguredApproverTMTPC(branchId,txnNetAmount,txnParameter){
	var jsonData = {};
	jsonData.email =$("#hiddenuseremail").text();
	jsonData.txnBranchId=branchId;
	jsonData.transactionNetAmount=txnNetAmount;
	jsonData.transactionParameter=txnParameter;
	var url="/transaction/ruleBasedUserExistence";
	var bool;
	$.ajax({
		url: url,
		data:JSON.stringify(jsonData),
		type:"text",
		method:"POST",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		async:false,
		contentType:'application/json',
		success: function (data) {
			if(data.result){
				var userExistence=data.transactionRuleBasedExistence[0].userExistence;
				if(userExistence=="Exist"){
					bool= true;
				}
				if(userExistence=="Does Not Exist"){
					swal("Warning!","There Is No Any Approver Configured For Approval Process.Once Approver Is Configured You Can Proceed With The Transaction.","warning");
					$(".btn-custom").removeAttr("disabled");
					$(".btn-customred").removeAttr("disabled");
					$(".approverAction").removeAttr("disabled");
					$("#completeTxn").removeAttr("disabled");
					bool= false;
				}
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
	return bool;
}

function checkForOnlyConfiguredApproverPATVS(specificsId,txnNetAmount,txnParameter){
	var jsonData = {};
	jsonData.email =$("#hiddenuseremail").text();
	jsonData.txnItemId=specificsId;
	jsonData.transactionNetAmount=txnNetAmount;
	jsonData.transactionParameter=txnParameter;
	var url="/transaction/ruleBasedUserExistence";
	var bool;
	$.ajax({
		url: url,
		data:JSON.stringify(jsonData),
		type:"text",
		method:"POST",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		async:false,
		contentType:'application/json',
		success: function (data) {
			if(data.result){
				var userExistence=data.transactionRuleBasedExistence[0].userExistence;
				if(userExistence=="Exist"){
					bool= true;
				}
				if(userExistence=="Does Not Exist"){
					swal("Warning!","There Is No Any Approver Configured For Approval Process.Once Approver Is Configured You Can Proceed With The Transaction.","warning");
					$(".btn-custom").removeAttr("disabled");
					$(".btn-customred").removeAttr("disabled");
					$(".approverAction").removeAttr("disabled");
					$("#completeTxn").removeAttr("disabled");
					bool= false;
				}
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
	return bool;
}

function checkForOnlyConfiguredApproverPVS(txnIvoice,txnNetAmount,txnParameter){
	var jsonData = {};
	jsonData.email =$("#hiddenuseremail").text();
	jsonData.txnInv=txnIvoice;
	jsonData.transactionNetAmount=txnNetAmount;
	jsonData.transactionParameter=txnParameter;
	var url="/transaction/ruleBasedUserExistence";
	var bool;
	$.ajax({
		url: url,
		data:JSON.stringify(jsonData),
		type:"text",
		method:"POST",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		async:false,
		contentType:'application/json',
		success: function (data) {
			if(data.result){
				var userExistence=data.transactionRuleBasedExistence[0].userExistence;
				if(userExistence=="Exist"){
					bool= true;
				}
				if(userExistence=="Does Not Exist"){
					swal("Warning!","There is No Approver or Accountant Configured For Approval/Accounting Process.Once Approver/Accountant Is Configured You Can Proceed With The Transaction.","warning");
					$(".btn-custom").removeAttr("disabled");
					$(".btn-customred").removeAttr("disabled");
					$(".approverAction").removeAttr("disabled");
					$("#completeTxn").removeAttr("disabled");
					bool= false;
				}
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); } else if (xhr.status == 500) {
				swal("Error!", "Please contact support team or retry.", "error");
			}
		},
		complete: function (data) {
			$.unblockUI();
			enableTransactionButtons();
		}
	});
	return bool;
}

function checkForConfiguredApproverAndAccountants(branchId,specificsId,txnNetAmount,txnParameter){
	var jsonData = {};
	jsonData.email =$("#hiddenuseremail").text();
	jsonData.txnItemId=specificsId;
	jsonData.txnBranchId=branchId;
	jsonData.transactionNetAmount=txnNetAmount;
	jsonData.transactionParameter=txnParameter;
	var url="/transaction/ruleBasedUserExistence";
	var bool;
	$.ajax({
		url: url,
		data:JSON.stringify(jsonData),
		type:"text",
		method:"POST",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		async:false,
		contentType:'application/json',
		success: function (data) {
			if(data.result){
				var userExistence=data.transactionRuleBasedExistence[0].userExistence;
				if(userExistence=="Exist"){
					bool= true;
				}
				if(userExistence=="Does Not Exist"){
					swal("Warning!","There Is No Any Approver or Accountant Configured For Approval/Accounting Process.Once Approver/Accountant Is Configured You Can Proceed With The Transaction.","warning");
					$(".btn-custom").removeAttr("disabled");
					$(".btn-customred").removeAttr("disabled");
					$(".approverAction").removeAttr("disabled");
					$("#completeTxn").removeAttr("disabled");
					bool= false;
				}
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
	return bool;
}

function checkForOnlyConfiguredAccountants(claimTxnRefNumber,txnNetAmount,txnParameter){
	var jsonData = {};
	jsonData.email =$("#hiddenuseremail").text();
	jsonData.claimTransactionRefNumber=claimTxnRefNumber;
	jsonData.transactionNetAmount=txnNetAmount;
	jsonData.transactionParameter=txnParameter;
	var url="/transaction/ruleBasedUserExistence";
	var bool;
	$.ajax({
		url: url,
		data:JSON.stringify(jsonData),
		type:"text",
		method:"POST",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		async:false,
		contentType:'application/json',
		success: function (data) {
			if(data.result){
				var userExistence=data.transactionRuleBasedExistence[0].userExistence;
				if(userExistence=="Exist"){
					bool= true;
				}
				if(userExistence=="Does Not Exist"){
					swal("Warning!","There Is No Any Accountant Configured For Accounting Process.Once Accountant Is Configured You Can Proceed With The Transaction.","warning");
					$(".btn-custom").removeAttr("disabled");
					$(".btn-customred").removeAttr("disabled");
					$(".approverAction").removeAttr("disabled");
					$("#completeTxn").removeAttr("disabled");
					bool= false;
				}
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
	return bool;
}
//function to check approver accountants for the smmoth transaction processing end
//function to populate chart of accounts in trial balance format end


/*New UI Starts*/
$(document).ready(function() {
	$('body').on('DOMNodeInserted', '#google_translate_element', function(e) {
		if(isGoogleLangLoaded == false){
			var mainEle = $('#google_translate_element').find('.goog-te-gadget>.goog-te-gadget-simple');
			if (mainEle.length>0) {
				$(mainEle).find('img.goog-te-gadget-icon').remove();
				var ele = $(mainEle).find('a.goog-te-menu-value');
				if (ele.length>0) {

						//$(ele).find('span:nth-child(1)').html('');
					  $(ele).find('span:nth-child(1)').append('Language');
					  $(ele).find('span:nth-child(5)').empty().attr('class', 'fa fa-chevron-down').css('color', 'white');
					  $(ele).find('span:nth-child(3)').remove();
				}
			}
			isGoogleLangLoaded = true;
		}
	});
	$('body').on('DOMNodeInserted', 'iframe', function(e) {
		setTimeout(function() {
			if ($(e.target).hasClass('goog-te-menu-frame')) {
				var iframe = $('iframe.goog-te-menu-frame');
				$(iframe).addClass('google-te-iframe');
				var css = '<style type="text/css">div.goog-te-menu2 {border: 5px solid #4D4F4D !important;}</style>';
				var iframeHead = $('iframe.goog-te-menu-frame').contents().find('head');
				$(iframeHead).append(css);
			}
		}, 1500);
	});
});


$(document).ready(function(){
	$('.settingAlertIcon').on('mouseenter', function(){
		if ($('#userAlertOptions').height() < 50) {
			$('#userAlertOptions').css({'height': '50px', 'top': '45px'}).show();
			$('#userAlertOptions div').show();
		}
	});
	$('body').on('click', function(){
		if ($('#userAlertOptions').height() > 0) {
			$('#userAlertOptions').css({'height': '0px', 'top': '35px'});
			$('#userAlertOptions div').hide();
		}
	});
});
/*New UI Ends*/

function showAllBanksAccountsForTheBranch(elem){
	customMethod6(elem);
	var branchPrimKey=$(elem).val();
	var branchContainerId=$(elem).attr('id');
	$(".txnBranchBankDetails").html("");
	if(branchPrimKey!=""){
		//alert("showAllBanksAccountsForTheBranch " + branchPrimKey);
		var jsonData = {};
		jsonData.email =$("#hiddenuseremail").text();
		jsonData.branchId =branchPrimKey;
		jsonData.branchSelectId=branchContainerId;
		ajaxCall('/data/branchBank', jsonData, '', '', '', '', 'showAllBanksAccountsForTheBranchSuccess', '', false);
	}else{
		$("#fromBranchBankAccounts").children().remove();
		$("#fromBranchBankAccounts").append('<option value="">--Please Select--</option>');
		$("#toBranchBankAccounts").children().remove();
		$("#toBranchBankAccounts").append('<option value="">--Please Select--</option>');
		$(".txnBranchBanks").children().remove();
		$(".txnBranchBanks").append('<option value="">--Please Select--</option>');
		$(".txnBranches").children().remove();
		$(".txnBranches").append('<option value="">--Please Select--</option>');
		$(".klBranchCashierTd").text("");
		$(".txnFromBranchBankDetails").html("");
		$(".txnToBranchBankDetails").text("");
		$(".txnBranchBankDetails").html("");
		$("#totalAmountInBank").val("");
		$("#withdrawalAmount").val("");
		$("#leftAmountInBank").val("");
		$("#depositAmount").val("");
		$("#totalAmountInBank").val("");
		$("#transferAmount").val("");
		$("#totalAmtInBank").val("");
	}
}

function showAllBanksAccountsForTheBranchSuccess(data){
	if(data.result){
		data=data.branchBankList;
		var branchSelectId=data[0].branchSelectId;
		if(branchSelectId!=""){
			if(branchSelectId=="tfftbnkTxnFromBranches"){
				$("#fromBranchBankAccounts").children().remove();
				$("#fromBranchBankAccounts").append('<option value="">--Please Select--</option>');
				var fromBankList = "";
				for(var i=0;i<data.length;i++){
					fromBankList = fromBankList + ('<option value="'+data[i].id+'">'+data[i].bankName+'-'+data[i].bankNumber+'('+data[i].bankAmount+')</option>');
				}
				$("#fromBranchBankAccounts").append(fromBankList);
			}else if(branchSelectId=="tfftbnkTxnToBranches"){
				$("#toBranchBankAccounts").children().remove();
				$("#toBranchBankAccounts").append('<option value="">--Please Select--</option>');
				var toBankList = "";
				var selectedFromBank = $("#fromBranchBankAccounts option:selected").val();
				for(var i=0;i<data.length;i++){
					if(selectedFromBank != data[i].id){
						toBankList = toBankList + ('<option value="'+data[i].id+'">'+data[i].bankName+'-'+data[i].bankNumber+'('+data[i].bankAmount+')</option>');
					}
				}
				$("#toBranchBankAccounts").append(toBankList);
			}else if(branchSelectId=="selectBranchCashBankBookId"){
				$("#selectBankCashBankBookId").children().remove();
				$("#selectBankCashBankBookId").append('<option value="">--Please Select--</option>');
				for(var i=0;i<data.length;i++){
					$("#selectBankCashBankBookId").append('<option value="'+data[i].id+'">'+data[i].bankName+'</option>');
				}
			}else{
				if(branchSelectId!="tfftbnkTxnFromBranches" && branchSelectId!="tfftbnkTxnToBranches"){
					$(".txnBranchBanks").children().remove();
					$(".txnBranchBanks").append('<option value="">--Please Select--</option>');
					for(var i=0;i<data.length;i++){
						$(".txnBranchBanks").append('<option value="'+data[i].id+'">'+data[i].bankName+'-'+data[i].bankNumber+'('+data[i].bankAmount+')</option>');
					}
				}
			}
		}
		$(".klBranchCashierTd").text("");
	}
}

function checkForAmountInLimit(elem){
	var amountEntered=$(elem).val();
	var parentTr=$(elem).parent().parent().attr('id');
	var originalAmountInTheBank="";
	var enteredFloat = "";
	if(parentTr=="wcabnktrid"){
		if(amountEntered == ""){
			$(elem).val("");
			$("input[name='leftAmountInBank']").val("");
			return true;
		}
		 enteredFloat=parseFloat(amountEntered);
			var originalAmountBank=$("#"+parentTr+" div[class='txnBranchBankDetails'] b[id='originalAmount'] ").text();
			var originalAmountFloat=parseFloat(originalAmountBank);
			var branchTypeAllowsNegBal =$(".branchTypeAllowsNegBal").val();
			if(enteredFloat>originalAmountFloat && branchTypeAllowsNegBal=="false" ){
				swal("Warning!","You Are Not Allowed To Withdraw Amount Greater Than The Available Amount In The Bank","warning");
				$(elem).val("");
				$("input[name='leftAmountInBank']").val("");
				return true;
			}else{
				if(enteredFloat>originalAmountFloat && branchTypeAllowsNegBal=="true"){
					swal("Warning!","Bank Balance is in -ve, but due to Account type You Are ALLOWED To Withdraw Amount Greater Than The Available Amount In The Bank","warning");
				}
				var result=(originalAmountFloat-enteredFloat).toFixed(2);
				$("input[name='leftAmountInBank']").val(result);
			}
		}
		if(parentTr=="dcabnktrid"){
			if(amountEntered == ""){
				$(elem).val("");
				$("input[name='totalAmountInBank']").val("");
				return true;
			}
		 	enteredFloat=parseFloat(amountEntered);
			var originalAmountBank=$("#"+parentTr+" div[class='txnBranchBankDetails'] b[id='originalAmount'] ").text();
			var originalAmountFloat=parseFloat(originalAmountBank);
			var enteredFloat=parseFloat(amountEntered);
			var result=(originalAmountFloat+enteredFloat).toFixed(2);
			$("input[name='totalAmountInBank']").val(result);
		}
		if(parentTr=="tffbnktrid"){
			if(amountEntered == ""){
				$(elem).val("");
				$("input[name='totalAmtInBank']").val("");
				return true;
			}
			 enteredFloat=parseFloat(amountEntered);
			var toBranchBankAccounts = $("#"+parentTr+" select[id='toBranchBankAccounts'] option:selected").val();
			if(toBranchBankAccounts == "" || typeof toBranchBankAccounts == 'undefined'){
				swal("Invalid to bank!", "To Bank is not selected.", "error");
				return false;
			}
			var originalAmountBank=$("#"+parentTr+" div[class='txnFromBranchBankDetails'] b[id='originalAmount'] ").text();
			var originalAmountFloat=parseFloat(originalAmountBank);
			var branchTypeAllowsNegBal =$(".branchTypeAllowsNegBal").val();
			var transferToBankAmount=$("#"+parentTr+" div[class='txnToBranchBankDetails'] b[id='originalAmount'] ").text();
			var originalToBankAmountFloat=parseFloat(transferToBankAmount);
			if(enteredFloat>originalAmountFloat && branchTypeAllowsNegBal == "false"){
				swal("Warning!","You Are Not Allowed To Transfer Amount Greater Than The Available Amount In The Bank","warning");
				$(elem).val("");
				$("input[name='totalAmtInBank']").val("");
				return true;
			}else if(enteredFloat>originalAmountFloat && branchTypeAllowsNegBal=="true"){
				swal("Warning!","From Bank Balance is in -ve, but due to Account type You Are Not Allowed To Withdraw Amount Greater Than The Available Amount In The Bank","warning");
				var result=(originalToBankAmountFloat+enteredFloat).toFixed(2);
				$("input[name='totalAmtInBank']").val(result);
			}else if(enteredFloat<originalAmountFloat){
				var result=(originalToBankAmountFloat+enteredFloat).toFixed(2);
				$("input[name='totalAmtInBank']").val(result);
			}
		}

}

$(document).ready(function(){
	$('.listTransaction').on('click', function(){
	//	alert("listTransaction") ;
		if(!$('.listTxnOptions').is(':visible')){
			$('.listTxnOptions').slideDown('normal', function(){
				$('.listTransaction').find('i.fa').removeClass('fa-chevron-down').addClass('fa-chevron-up');
			});
		}else{
			$('.listTxnOptions').slideUp('normal', function(){
				$('.listTransaction').find('i.fa').removeClass('fa-chevron-up').addClass('fa-chevron-down');
			});
		}
	});
	$('body').on('click', function(e){
		if($(e.target).parent().attr('id') !== 'listTransaction' && $(e.target).parent().attr('id') !== 'listClaimTransaction'){
			$('.listTxnOptions:visible').slideUp('normal', function(){
				$('.listTransaction').find('i.fa').removeClass('fa-chevron-up').addClass('fa-chevron-down');
			});
		}
	});
	$('.listTxnOptions span').on('click', function(){
		if($(this).parent().attr('id')==='listTxnOptions'){
			if($(this).attr('data-value')==100){
				if(userTransactionListString!=""){
					$("#transactionTable tbody").html("");
					$("#transactionTable tbody").html(userTransactionListString);
				}else{
					getUserTransactions($(this).attr('data-value'));
				}
			}
			if($(this).attr('data-value')==250){
				if(userTransactionListTwoFiftyString!=""){
					$("#transactionTable tbody").html("");
					$("#transactionTable tbody").html(userTransactionListTwoFiftyString);
				}else{
					getUserTransactions($(this).attr('data-value'));
				}
			}
			if($(this).attr('data-value')==500){
				if(userTransactionListFiveHundredString!=""){
					$("#transactionTable tbody").html("");
					$("#transactionTable tbody").html(userTransactionListFiveHundredString);
				}else{
					getUserTransactions($(this).attr('data-value'));
				}
			}
			if($(this).attr('data-value')==1000){
				if(userTransactionListThousandString!=""){
					$("#transactionTable tbody").html("");
					$("#transactionTable tbody").html(userTransactionListThousandString);
				}else{
					getUserTransactions($(this).attr('data-value'));
				}
			}
		}else if($(this).parent().attr('id')==='listClaimTxnOptions'){
			getClaimsTransactions($(this).attr('data-value'));
		}
	});
});




function periodicInventory(elem){
	$("#pendingExpense").hide();
	$("#trialBalance").hide();
	 $("#bankBookDiv").hide();
	 $("#cashBookDiv").hide();
	$('#periodicInventory').show();
	$('#reportInventory').hide();
	$("#reportAllInventory").hide();
	$(".bnchCashnBankTrialBalance option:first").prop('selected','selected');
	$("#periodicInventoryFromDate").val("");
	$("#periodicInventoryToDate").val("");
	displayPeriodicInventory();
}

function reportInventory(elem){
	$("#pendingExpense").hide();
	$("#trialBalance").hide();
	$("#cashAndBank").hide();
	$("#bankBookDiv").hide();
	$("#cashBookDiv").hide();
	$('#periodicInventory').hide();
	$('#reportAllInventory').hide();
	$('#reportInventory').show();
	$("#selectBnchReportInventoryId option:first").prop('selected','selected');
	$("#reportInventoryFromDate").val("");
	$("#reportInventoryToDate").val("");
	displayReportInventory();
}

function reportAllInventory(elem){
    var mouldesRights = $("#usermoduleshidden").val();
    showHideModuleTabs(mouldesRights);
	getBranchData();
	$("#pendingExpense").hide();
	$("#trialBalance").hide();
	$("#bankBookDiv").hide();
	$("#cashBookDiv").hide();
	$("#plbsCoaMapping").hide();
	$("#profitloss").hide();
	$("#balanceSheet").hide();
	$('#periodicInventory').hide();
	$('#reportInventory').hide();
	$('#reportAllInventory').show();
	$("#allInventorySpecificsItemId option:first").prop('selected','selected');
	$("#selectBnchReportAllInventoryId option:first").prop('selected','selected');
	displayReportAllInventoryItems();
}


function displayPeriodicInventory(){
	var piBranch=$("#selectBnchPeriodicInventoryId option:selected").val();
	var piFromDate=$("#periodicInventoryFromDate").val();
	var piToDate=$("#periodicInventoryToDate").val();
	periodicInventoryBranch=piBranch;
	periodicInventoryFromDate=piFromDate;
	periodicInventoryToDate=piToDate;
	var jsonData={};
	jsonData.email =$("#hiddenuseremail").text();
	jsonData.periodicInventoryForBranch=piBranch;
	jsonData.periodicInventoryFromDate=piFromDate;
	jsonData.periodicInventoryToDate=piToDate;
	ajaxCall('/periodicInventory/display', jsonData, '', '', '', '', 'periodicInventorySuccess', '', true);
}

function displayReportInventory(){
	var irBranch=$("#selectBnchReportInventoryId option:selected").val();
	var irFromDate=$("#reportInventoryFromDate").val();
	var irToDate=$("#reportInventoryToDate").val();
	reportInventoryBranch=irBranch;
	reportInventoryFromDate=irFromDate;
	reportInventoryToDate=irToDate;
	var jsonData={};
	jsonData.email =$("#hiddenuseremail").text();
	jsonData.reportInventoryForBranch=irBranch;
	jsonData.reportInventoryFromDate=irFromDate;
	jsonData.reportInventoryToDate=irToDate;
	ajaxCall('/reportInventory/display', jsonData, '', '', '', '', 'reportInventorySuccess', '', true);
}

function displayReportAllInventoryItems(){
	$("#reportAllInventoryTable tbody").html("");
	var irSpecifics=$("#allInventorySpecificsItemId option:selected").val();
	var irBranch=$("#selectBnchReportAllInventoryId option:selected").val();
	var reportInventoryFromDate = $("#allreportInventoryFromDate").val();
	var reportInventoryToDate = $("#allreportInventoryToDate").val();
	reportAllInventorySpecifics=irSpecifics;
	reportAllInventoryBranch=irBranch;
	var jsonData={};
	jsonData.email =$("#hiddenuseremail").text();
	jsonData.reportAllInventorySpecifics=irSpecifics;
	jsonData.reportAllInventoryBranch=irBranch;
	jsonData.reportInventoryFromDate = reportInventoryFromDate;
	jsonData.reportInventoryToDate = reportInventoryToDate;

	ajaxCall('/reportInventory/displayAllInventory', jsonData, '', '', '', '', 'displayReportAllInventoryItemsSuccess', '', true);
}

function exportReportAllInventoryItems(elem){
	var elemId=$(elem).attr('id');
	var irSpecifics=$("#allInventorySpecificsItemId option:selected").val();
	var irBranch=$("#selectBnchReportAllInventoryId option:selected").val();
	var reportInventoryFromDate = $("#allreportInventoryFromDate").val();
	var reportInventoryToDate = $("#allreportInventoryToDate").val();
	reportAllInventorySpecifics=irSpecifics;
	reportAllInventoryBranch=irBranch;
	var exportType="";
	if(elemId=="exportReportAllInventoryItemsXLSX"){
		exportType="xlsx";
	}else if(elemId=="exportReportAllInventoryItemsPDF"){
		exportType="pdf";
	}
	var jsonData={};
	jsonData.email =$("#hiddenuseremail").text();
	jsonData.accessKey="IDKrg0f9dHMhVROS";
	jsonData.reportAllInventorySpecifics=irSpecifics;
	jsonData.reportAllInventoryBranch=irBranch;
	jsonData.reportInventoryFromDate = reportInventoryFromDate;
	jsonData.reportInventoryToDate = reportInventoryToDate;
	jsonData.irexporttype=exportType;
	downloadFile('/reportInventory/exportInventory', "POST", jsonData, "Error on downloading Inventory Items!");
	//ajaxCall('/reportInventory/exportInventory', jsonData, '', '', '', '', 'exportInventorySuccess', '', true);
}
/*
function exportInventorySuccess(data){
	if(data.result){
		var dt = new Date().toString();
		var fileName=data.riFileData[0].fileName;
		var url='assets/report/'+fileName+'?unique='+dt;
	    var childwindow=window.open(url);
	}else{
		$.unblockUI();
	}
}*/

function periodicInventorySuccess(data){
	$("#periodicInventoryTable tbody").html("");
	if(data.result){
		for(var i=0;i<data.periodicInventoryData.length;i++){
			$("#periodicInventoryTable tbody").append('<tr class="periodicInventoryRowData"><td>'+data.periodicInventoryData[i].inventoryIncomeExpenseItemName+'</td><td>'+data.periodicInventoryData[i].createdDate+'</td><td>'+data.periodicInventoryData[i].inventoryStockType+'</td><td>'+data.periodicInventoryData[i].units+'</td><td>'+data.periodicInventoryData[i].price+'</td><td>'+data.periodicInventoryData[i].amount+'</td></tr>');
		}
	}
}

function reportInventorySuccess(data){
	$("#reportInventoryTable tbody").html("");
	if(data.result){
		for(var i=0;i<data.inventoryReportData.length;i++){
			$("#reportInventoryTable tbody").append('<tr class="reportInventoryRowData"><td>'+data.inventoryReportData[i].particulars+'</td><td>'+data.inventoryReportData[i].createdDate+'</td><td>'+data.inventoryReportData[i].openingUnits+'</td><td>'+data.inventoryReportData[i].purchaseUnit+'</td><td>'+data.inventoryReportData[i].sellUnit+'</td><td>'+data.inventoryReportData[i].closingUnits+'</td></tr>');
		}
	}
}

function displayReportAllInventoryItemsSuccess(data){
	$("#reportAllInventoryTable tbody").html("");
	var selectedVal=$("#allInventorySpecificsItemId option:selected").val();
	$("#allInventorySpecificsItemId").children().remove();
	$("#allInventorySpecificsItemId").append('<option value="">--Please Select--</option>');
	for(var i=0;i<data.displayInventory.length;i++){
		$("#allInventorySpecificsItemId").append('<option value="'+data.displayInventory[i].itemId+'">'+data.displayInventory[i].itemName+'</option>');
	}

	$("#allInventorySpecificsItemId").find("option[value='"+selectedVal+"']").prop("selected","selected");

	if(data.result){
		for(var i=0;i<data.displayInventory.length;i++){
			$("#reportAllInventoryTable tbody").append('<tr id="reportAllInventoryRowData'+data.displayInventory[i].itemId+'"><td><a href="#periodicInventory" class="tradeInventory" onclick="displayInventoryReportMid(this, \''+ data.displayInventory[i].itemId +'\');">'+data.displayInventory[i].itemName+'</a></td><td>'+data.displayInventory[i].sellItemNames+'</td><td>'+data.displayInventory[i].unitOfMeasure+'</td><td>'+data.displayInventory[i].openingQty+'</td><td>'+data.displayInventory[i].openingBal+'</td><td>'+data.displayInventory[i].buyQty+'</td><td>'+data.displayInventory[i].buyVal+'</td><td>'+data.displayInventory[i].sellQty+'</td><td>'+data.displayInventory[i].sellVal+'</td><td>'+data.displayInventory[i].closingQty+'</td><td>'+data.displayInventory[i].closingBal+'</td><td>'+data.displayInventory[i].salesAmount+'</td><td>'+data.displayInventory[i].salesMarginAmount+'</td><td>'+data.displayInventory[i].salesMarginPercent+'</td></tr>');
		}
	}
}


var displayInventoryReportMid = function(elem, specificsID){
	var jsonData = {};
	var reportInventoryFromDate = $("#allreportInventoryFromDate").val();
	var reportInventoryToDate = $("#allreportInventoryToDate").val();
	jsonData.fromDateStr = reportInventoryFromDate;
	jsonData.toDateStr = reportInventoryToDate;
	var useremail=$("#hiddenuseremail").text();
	jsonData.specificsID = specificsID;
	jsonData.usermail = useremail;
	var url="/reportInventory/midInventory";
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
			$("#staticTradingInventory").attr('data-toggle', 'modal');
	    	$("#staticTradingInventory").modal('show');
	    	$(".staticTradingInventory").attr("href",location.hash);
	    	//$("#staticTradingInventory div[class='modal-body']").html("");
	    	$("#inventoryBranchTbl tbody").html("");
	    	var tmpTr = "";
	    	if(data && data.status){
	    		$("#staticTradingInventory h4[class='panel-title']").text('Branch Trading Inventory: ' + data.itemName);
	    		$("#staticTradingInventory h4[class='panel-title']").append('<br>'+data.method);
	    		for(var i = 0; i < data.midInventory.length; i++){
	    			tmpTr += '<tr id=\'branch'+data.midInventory[i].branchid+'\'><td  style="cursor:pointer; color:blue;" onclick="displayInventoryItem(\''+data.midInventory[i].branchid+'\',\''+data.midInventory[i].itemid+'\');">'+data.midInventory[i].branch+'</td>';
	    			tmpTr += "<td>"+data.midInventory[i].openingBalanceUnit+'</td>';
	    			tmpTr += "<td>"+data.midInventory[i].openingBalance+'</td>';
	    			tmpTr += "<td>"+data.midInventory[i].buyUnit+'</td>';
	    			tmpTr += "<td>"+data.midInventory[i].buyAmount+'</td>';
	    			tmpTr += "<td>"+data.midInventory[i].saleUnit+'</td>';
	    			tmpTr += "<td>"+data.midInventory[i].saleAmount+'</td>';
	    			tmpTr += "<td>"+data.midInventory[i].closingUnit+'</td>';
	    			tmpTr += "<td>"+data.midInventory[i].closingAmount+'</td>';
	    			tmpTr += "<td>"+data.midInventory[i].txnSaleAmount+'</td>';
	    			tmpTr += "<td>"+data.midInventory[i].saleMarginAmount+'</td>';
	    			tmpTr += "<td>"+data.midInventory[i].saleMarginPercent+'</td></tr>';
	    		}

	    	}
	    	$("#inventoryBranchTbl tbody").append(tmpTr);
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){
				doLogout();
			}else if (xhr.status == 500) {
                swal("Error on fetching inventory for Item!", "Please retry, if problem persists contact support team", "error");
            }else{
            	 swal(error, "Please retry, if problem persists contact support team", "error");
            }
		}
	});
}

function displayInventoryItem(branchid, specificsID){
 	var branchName = $("#branch"+branchid +" td:nth-child(1)").text();
	var openingBalanceUnit = $("#branch"+branchid +" td:nth-child(2)").text();
	var openingBalance = $("#branch"+branchid +" td:nth-child(3)").text();
	var buyUnit = $("#branch"+branchid +" td:nth-child(4)").text();
	var buyAmount = $("#branch"+branchid +" td:nth-child(5)").text();
	var saleUnit = $("#branch"+branchid +" td:nth-child(6)").text();
	var saleAmount = $("#branch"+branchid +" td:nth-child(7)").text();
	var closingUnit = $("#branch"+branchid +" td:nth-child(8)").text();
	var closingAmount = $("#branch"+branchid +" td:nth-child(9)").text();
	var txnSaleAmount = $("#branch"+branchid +" td:nth-child(10)").text();
	var saleMarginAmount = $("#branch"+branchid +" td:nth-child(11)").text();
	var saleMarginPercent = $("#branch"+branchid +" td:nth-child(12)").text();
	var reportInventoryFromDate = $("#allreportInventoryFromDate").val();
	var reportInventoryToDate = $("#allreportInventoryToDate").val();

	var jsonData = {};
	var useremail=$("#hiddenuseremail").text();
	jsonData.fromDateStr = reportInventoryFromDate;
	jsonData.toDateStr = reportInventoryToDate;
	jsonData.branchid= branchid;
	jsonData.specificsID = specificsID;
	jsonData.usermail = useremail;
	jsonData.openingBalanceUnit = openingBalanceUnit;
	jsonData.openingBalance = openingBalance;
	jsonData.buyUnit = buyUnit;
	jsonData.buyAmount = buyAmount;
	jsonData.saleUnit = saleUnit;
	jsonData.saleAmount = saleAmount;
	jsonData.closingUnit = closingUnit;
	jsonData.closingAmount = closingAmount;
	jsonData.txnSaleAmount = txnSaleAmount;
	jsonData.saleMarginAmount = saleMarginAmount;
	jsonData.saleMarginPercent = saleMarginPercent;
	var url="/reportInventory/displayDetailInventory";
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
				$("#staticDetailedTradingInventory").attr('data-toggle', 'modal');
		    	$("#staticDetailedTradingInventory").modal('show');
		    	$(".staticDetailedTradingInventory").attr("href",location.hash);
		    	$("#inventoryItemsBreakupTable tbody").html("");
		    	$("#staticDetailedTradingInventory h4[class='panel-title']").text('Detailed Trading Inventory: ' + branchName);
		    	var tmpTr = '<tr id="detailedRowData"><td></td><td></td><td></td><td>'+openingBalanceUnit+'</td><td></td><td>'+openingBalance+'</td><td>'+buyUnit+'</td><td></td><td>'+buyAmount+'</td><td>'+saleUnit+'</td><td></td><td>'+saleAmount+'</td><td>'+closingUnit+'</td><td></td><td>'+closingAmount+'</td><td>'+txnSaleAmount+'</td><td>'+saleMarginAmount+'</td><td>'+saleMarginPercent+'</td></tr>';

		    	if(data && data.status){
		    		for(var i = 0; i < data.inventory.length; i++){
		    			tmpTr += '<tr><td>'+data.inventory[i].date+'</td>';
		    			tmpTr += '<td>'+data.inventory[i].txnName+'</td>';
		    			tmpTr += '<td>'+data.inventory[i].txnRef+'</td>';
		    			tmpTr += "<td>"+data.inventory[i].openingBalanceUnit+'</td>';
		    			tmpTr += "<td>"+data.inventory[i].openingBalanceRate+'</td>';
		    			tmpTr += "<td>"+data.inventory[i].openingBalance+'</td>';
		    			tmpTr += "<td>"+data.inventory[i].buyUnit+'</td>';
		    			tmpTr += "<td>"+data.inventory[i].buyRate+'</td>';
		    			tmpTr += "<td>"+data.inventory[i].buyAmount+'</td>';
		    			tmpTr += "<td>"+data.inventory[i].saleUnit+'</td>';
		    			tmpTr += "<td>"+data.inventory[i].saleRate+'</td>';
		    			tmpTr += "<td>"+data.inventory[i].saleAmount+'</td>';
		    			tmpTr += "<td>"+data.inventory[i].closingUnit+'</td>';
		    			tmpTr += "<td>"+data.inventory[i].closingRate+'</td>';
		    			tmpTr += "<td>"+data.inventory[i].closingAmount+'</td>';
		    			tmpTr += "<td>"+data.inventory[i].txnSaleAmount+'</td>';
		    			tmpTr += "<td>"+data.inventory[i].saleMarginAmount+'</td>';
		    			tmpTr += "<td>"+data.inventory[i].saleMarginPercent+'</td></tr>';
		    		}

		    	}
		    	$("#inventoryItemsBreakupTable tbody").append(tmpTr);
            	setPagingDetail('inventoryItemsBreakupTable', 35, 'pagingInventoryNavPosition');
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
}

$(document).ready(function(){
	$('body').on('click', '.modal-backdrop', function(){
		var ele=$('.modal:visible');
		$(ele).attr('data-toggle', 'modal');
		$(ele).modal('hide');
		$(this).remove();
		$('#fullPlot').remove();
	});
	$('body').on('click', '#auditorFinancialTable tbody td', function(e){
		var tick=$(this).attr('data-tick');
		var plot=$(this).attr('data-plot');
		if (!isEmpty(tick) && !isEmpty(plot) && !$(e.target).hasClass('jqplot-event-canvas')){
			plot=plot.split(',');
			tick=tick.split(',');
			var html='<div class="modal-backdrop fade in"></div>'
				+'<div id="fullPlot" class="full-plot"><span id="disLabelFullPlot"></span>'
				+'<a id="fullPlotClose"></a><div id="jqplotFull" style="top:35px; height: 90%;"></div>'
				+'</div>';
			$('body').append(html);
			$('#fullPlot>#fancybox-close').css('display', 'inline');
			var req, parEle=$(this).parent(), label;
			if($(parEle).hasClass('auditorCashExpense')){
				label='Cash Expense';
				if($(this).is('td:nth-child(2)')){
					req='thisWeekCashExpense';
				} else if($(this).is('td:nth-child(3)')){
					req='previousWeekCashExpense';
				} else if($(this).is('td:nth-child(4)')){
					req='thisWeekPreviousWeekCashExpenseVarience';
				}
			}else if($(parEle).hasClass('auditorCreditExpense')){
				label='Credit Expense';
				if($(this).is('td:nth-child(2)')){
					req='thisWeekCreditExpense';
				} else if($(this).is('td:nth-child(3)')){
					req='previousWeekCreditExpense';
				} else if($(this).is('td:nth-child(4)')){
					req='thisWeekPreviousWeekCreditExpenseVarience';
				}
			}else if($(parEle).hasClass('auditorCashIncome')){
				label='Cash Income';
				if($(this).is('td:nth-child(2)')){
					req='thisWeekCashIncome';
				} else if($(this).is('td:nth-child(3)')){
					req='previousWeeKCashIncome';
				} else if($(this).is('td:nth-child(4)')){
					req='thisWeekPreviousWeekCashIncomeVarience';
				}
			}else if($(parEle).hasClass('auditorCreditIncome')){
				label='Credit Income';
				if($(this).is('td:nth-child(2)')){
					req='thisWeekCreditIncome';
				} else if($(this).is('td:nth-child(3)')){
					req='previousWeekCreditIncome';
				} else if($(this).is('td:nth-child(4)')){
					req='thisWeekPreviousWeekCreditIncomeVarience';
				}
			}else if($(parEle).hasClass('auditorExpenseBudgetAvailable')){
				label='Performa Invoice Income';
				if($(this).is('td:nth-child(2)')){
					req='thisWeekExpenseBudgetAllocated';
				} else if($(this).is('td:nth-child(3)')){
					req='previousWeekExpenseBudgetAllocated';
				} else if($(this).is('td:nth-child(4)')){
					req='thisWeekPreviousWeekExpenseBudgetVarience';
				}
			}else if($(parEle).hasClass('auditorTotalReceivables')){
				label='Total Receivables';
				if($(this).is('td:nth-child(2)')){
					req='thisWeekTotalReceivables';
				} else if($(this).is('td:nth-child(3)')){
					req='previousWeekTotalReceivables';
				} else if($(this).is('td:nth-child(4)')){
					req='thisWeekPreviousWeekTotalReceivablesVarience';
				}
			}else if($(parEle).hasClass('auditorTotalPayables')){
				label='Total Payables';
				if($(this).is('td:nth-child(2)')){
					req='thisWeekTotalPayables';
				} else if($(this).is('td:nth-child(3)')){
					req='previousWeekTotalPayables';
				} else if($(this).is('td:nth-child(4)')){
					req='thisWeekPreviousWeekTotalPayablesVarience';
				}
			}
			if (!isEmpty(req)){
				if($(this).is('td:nth-child(2)')){
					label+=' - This Week (Till Date)';
				} else if($(this).is('td:nth-child(3)')){
					label+=' - Previous Week';
				} else if($(this).is('td:nth-child(4)')){
					label+=' - Variance';
				}
			}
			$('#disLabelFullPlot').html(label);
			$.jqplot('jqplotFull', [plot], {
				seriesColors: [ "#958c12", "#c5b47f", "#EAA228", "#579575", "#839557", "#958c12",
								 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],
				seriesDefaults:{
					renderer:$.jqplot.BarRenderer,
					pointLabels: { show: true, location: 'e',stackedValue:true,edgeTolerance: -50 ,hideZeros:true},
					rendererOptions: {fillToZero: true,barDirection: 'horizontal', barPadding: 8,barMargin: 10,barWidth: 15}
				},
				axes: {
					xaxis: {
						pad: 1.00,
						tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
						tickOptions: {
							formatString: '%d',
							angle: -30,
							textColor :'#0000FF'
						}
					},
					yaxis: {
						renderer: $.jqplot.CategoryAxisRenderer,
						ticks: tick,
						tickRenderer: $.jqplot.CanvasAxisTickRenderer,
						tickOptions: {
							angle: -30,
							fontSize: '8pt',
							textColor : '#FF0000'
						}
					}
				}
			});
			$('#jqplotFull').bind('jqplotDataClick',function (ev, seriesIndex, pointIndex, data) {
				$('#fullPlotClose').trigger('click');
				var bnchName=tick[pointIndex];
				var amountValue=data.toString().split(",");
				dispalyAndPopulateModal(bnchName,amountValue,req);
			});
		}
	});
	$('body').on('click', '#fullPlotClose', function(){
		$('.modal-backdrop, #fullPlot').fadeOut('normal', function(){
			$(this).remove();
		});
	});
});



/*Vendor Customer Search Starts*/
function getVendorCustomerBPSuccess(data) {
	if(data.result){
		var res=data.branch[0],html='';
		if(!isEmpty(res)){
			res=res.result;
			html=getVendorCustomerBPHTML(res);
			$('#customerVendorSearchBranch').html(html);
			$('#bnchTransactionStatementId').html(html);
		}
		res=data.project[0];
		if(!isEmpty(res)){
			res=res.result;
			html=getVendorCustomerBPHTML(res);
			$('#customerVendorSearchProject').html(html);
		}
	}else{
		swal("Error!",data.message,"error");
	}
}
function getVendorCustomerBPHTML(res){
	var html='';
	if(!isEmpty(res) && res.length > 0){
		html='<option value="">--Please Select--</option>';
		for(var i in res){
			html+='<option value="'+res[i].itemId+'">'+res[i].itemName+'</option>';
		}
	}
	return html;
}
$(document).ready(function(){
	$('#newSearchTransactionform-container').on('click',function(){
		var accountOrganization=GetURLParameter('accountOrganization');
		ajaxCall('/vendorcustomer/branchProject/'+accountOrganization, '', '', '', 'GET', '', 'getVendorCustomerBPSuccess', '', false);
		var txnType={},category={},html='';
		var entityType=GetURLParameter('entityType');
		if(entityType==='Vendor'){
			txnType['3']='Buy on cash & pay right away';
			txnType['4']='Buy on credit & pay later';
			txnType['7']='Pay vendor/supplier';
			txnType['8']='Pay advance to vendor or supplier';
			txnType['10']='Pay special adjustments amount to vendors';
			txnType['11']='Buy on Petty Cash Account';
			txnType['13']='Purchase returns';
			category['2']='Expenses';
			category['4']='Liabilities';
		}else if(entityType==='Customer'){
			txnType['1']='Sell on cash & collect payment now';
			txnType['2']='Sell on credit & collect payment later';
			txnType['5']='Receive payment from customer';
			txnType['6']='Receive advance from customer';
			txnType['12']='Sales returns';
			category['1']='Incomes';
			category['3']='Assets';
		}
		html='<option value="">--Please Select--</option>';
		for(var key in txnType){
			html+='<option value="'+key+'">'+txnType[key]+'</option>';
		}
		$('#customerVendorTransactionType').html(html);
		html='<option value="">--Please Select--</option>';
		for(var key in category){
			html+='<option value="'+key+'">'+category[key]+'</option>';
		}
		$('#customerVendorSearchCategory').html(html);
	});
	$('#customerVendorSearchCategory').on('change',function(){
		var val=this.value;
		if(!isEmpty(val)){
			var accountOrganization=GetURLParameter('accountOrganization');
			var url='/vendorcustomer/getItems/'+accountOrganization+'/'+val;
			ajaxCall(url, '', '', '', 'GET', '', 'getVendorCustomerItemsSuccess', '', false);
		}
	});
	$('#searchVendorTxnBtn').on('click',function(){
		var id=GetURLParameter('entityId'),type=GetURLParameter('entityType');
		if(isEmpty(id)){
			var msg='Cannot locate';
			if('Vendor'===type){
				msg+=' the vendor';
			}else if('Customer'===type){
				msg+=' the customer';
			}
			swal("Error!",msg,"error");
		}else{
			var json={};
			json.id=id;
			json.type=type;
			json.txnRefNumber=$('#customerVendorSearchTxnRefNumber').val();
			json.txnType=$('#customerVendorTransactionType').val();
			json.category=$('#customerVendorSearchCategory').val();
			json.item=$('#customerVendorSearchItems').val();
			json.fromDate=$('#customerVendorSearchFromDate').val();
			json.toDate=$('#customerVendorSearchToDate').val();
			json.branch=$('#customerVendorSearchBranch').val();
			json.project=$('#customerVendorSearchProject').val();
			json.fromAmount=$('#customerVendorAmountFrom').val();
			json.toAmount=$('#customerVendorAmountTo').val();
			json.status=$('#customerVendorSearchTxnStatus').val();
			ajaxCall('/vendorcustomer/search', json, '', '', '', '', 'getVendorCustomerSearchSuccess', '', false);
		}
	});
});
function getVendorCustomerSearchSuccess(data){
	$("#vendCustTransactions table[id='transactionTable'] tbody").empty();
	if(data.result){
		 $(".currentOutstandings").text();
		 $(".currentOutstandings").text(data.totalOutstandingsData[0].currentOutstandings);
		 if(data.totalOutstandingsData[0].type==1){
			 $(".specialAdjustmentsOutstandings").text();
			 $(".specialAdjustmentsOutstandings").text("Special Adjustments Outstandings:" +data.totalOutstandingsData[0].outstandingVendorSpecialAdjustments);
		 }
		 for(var i=0;i<data.userTxnData.length;i++){
			 $("#vendCustTransactions table[id='transactionTable']").prepend('<tr id="transactionEntity'+data.userTxnData[i].id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
			 '<b>PROJECT:</b><br/><p style="color: blue;">'+data.userTxnData[i].projectName+'</p></td><td><b>ITEM:</b><br/><p style="color: blue;">'+data.userTxnData[i].itemName+'</p></td><td><b>OUTSTANDINGS:</b><br/><div class="vendCustOutstandings" style="height: 130px;overflow: auto;"></div></p></td>'+
			 '<td><b>TRANSACTION DATE:</b><br/><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><div class="vendorInvDateLabelDiv"><b>'+data.userTxnData[i].invoiceDateLabel+'</b></div><div class="vendorInvDateDiv"><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td>'+data.userTxnData[i].paymentMode+'</td><td><b>UNIT:</b><br/><p style="color: blue;">'+data.userTxnData[i].noOfUnit+'</p><br/>'+
			 '<b>PRICE/UNIT:</b><br/><p style="color: blue;">'+data.userTxnData[i].unitPrice+'</p><br/><b>GROSS:</b><br/><p style="color: blue;">'+data.userTxnData[i].grossAmount+'</p></td><td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.userTxnData[i].netAmount+'</p><br/>'+
			 '<b>CALCULATION/DESCRIPTION:</b><br/><div class="netResultCalcDesc" style="height: 130px;overflow: auto;"></div><br/></td><td><div><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
			 '<td><div class="vendcustRemarks"></div></td>'+
			 '<td><div class="vendcustAcceptenceDiv"></div></td></tr>');
			 if(data.userTxnData[i].vendCustAcceptence=="1"){
				 $("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity"+data.userTxnData[i].id+"'] div[class='vendcustAcceptenceDiv']").html("");
				 $("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity"+data.userTxnData[i].id+"'] div[class='vendcustAcceptenceDiv']").html("<font color='green' size='4'><b>Accepted</b></p>");
				 $("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity"+data.userTxnData[i].id+"'] div[class='vendcustRemarks']").html("");
				 $("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity"+data.userTxnData[i].id+"'] div[class='vendcustRemarks']").html(data.userTxnData[i].vendCustRemarks);
			 }
			 if(data.userTxnData[i].vendCustAcceptence=="0"){
				 $("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity"+data.userTxnData[i].id+"'] div[class='vendcustRemarks']").html("");
				 $("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity"+data.userTxnData[i].id+"'] div[class='vendcustRemarks']").html('<input type="text" style="width: 140px;" id="vendcustuploadSuppDocs'+data.userTxnData[i].id+'" name="vendcustuploadSuppDocs'+data.userTxnData[i].id+'" readonly="readonly"><input type="button" id="vendcustuploadSuppDocs'+data.userTxnData[i].id+'" value="Upload" class="btn btn-primary btn-idos" onclick="uploadFile(this.id)"><br/>Remarks:<br/><textarea rows="1" name="vendcusttxnRemarks" id="vendcusttxnRemarks"></textarea>');
				 $("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity"+data.userTxnData[i].id+"'] div[class='vendcustAcceptenceDiv']").html("");
				 $("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity"+data.userTxnData[i].id+"'] div[class='vendcustAcceptenceDiv']").html("<input type='button' value='Accept Transaction' id='acceptVendCustTransaction' class='btn btn-primary btn-idos' onclick='vendCustAcceptTransaction(this)'>");
			 }
			 if ('customer' === entityType.toLowerCase()) {
				 if(parseInt(data.userTxnData[i].transactionPurposeID) == RECEIVE_PAYMENT_FROM_CUSTOMER || parseInt(data.userTxnData[i].transactionPurposeID) ==RECEIVE_ADVANCE_FROM_CUSTOMER){
					 $("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity" + data.userTxnData[i].id + "'] div[class='vendcustAcceptenceDiv']").append('<form action="/exportReceiptPdf" class="'+data.userTxnData[i].transactionPurpose+'" name="receiptForm'+data.userTxnData[i].id+'" id="receiptForm'+data.userTxnData[i].id+'">'+
						 '<input type="button" value="Generate Voucher" id="generateReceipt" class="btn btn-submit btn-idos" onclick="generatePDFReceipt(this,'+data.userTxnData[i].transactionPurposeID+');"></form>');
				}else {
					$("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity" + data.userTxnData[i].id + "'] div[class='vendcustAcceptenceDiv']").append('<div class="invoiceForm"><form action="/exportInvoicePdf" name="invoiceForm' + data.userTxnData[i].id + '" id="invoiceForm' + data.userTxnData[i].id + '"><input type="button" value="Generate Invoice" id="generateCustomerInvoiceBtn" email="' + data.userTxnData[i].email + '" class="btn btn-primary btn-idos" onclick="generateCustomerInvoice(this);"/></form></div>');
				}
			 }
			 if(data.userTxnData[i].netAmtDesc!=null && data.userTxnData[i].netAmtDesc!=""){
				var individualNetDesc=data.userTxnData[i].netAmtDesc.substring(0,data.userTxnData[i].netAmtDesc.length).split(',');
				for(var m=0;m<individualNetDesc.length;m++){
				    var labelAndFigure=individualNetDesc[m].substring(0, individualNetDesc[m].length).split(':');
				    $('#vendCustTransactions table[id="transactionTable"] tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="netResultCalcDesc"]').append('<font color="FF00FF"><b>'+labelAndFigure[0]+'</b></p><br/>');
				    if(typeof labelAndFigure[1]!='undefined'){
				    	$('#vendCustTransactions table[id="transactionTable"] tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="netResultCalcDesc"]').append('<p style="color: blue;">'+labelAndFigure[1]+'</p><br/>');
				    }
				}
			 }
			 if(data.userTxnData[i].transactionVendCustOutstandings!=null && data.userTxnData[i].transactionVendCustOutstandings!=""){
				var individualtransactionVendCustOutstandings=data.userTxnData[i].transactionVendCustOutstandings.substring(0,data.userTxnData[i].transactionVendCustOutstandings.length).split(',');
				for(var m=0;m<individualtransactionVendCustOutstandings.length;m++){
					var labelAndFigure=individualtransactionVendCustOutstandings[m].substring(0, individualtransactionVendCustOutstandings[m].length).split(':');
					$('#vendCustTransactions table[id="transactionTable"] tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="vendCustOutstandings"]').append('<b>'+labelAndFigure[0]+'</b><br/>');
					if(typeof labelAndFigure[1]!='undefined'){
					   $('#vendCustTransactions table[id="transactionTable"] tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="vendCustOutstandings"]').append('<p style="color: blue;">'+labelAndFigure[1]+'</p><br/>');
					}
				}
			}
		 }
		 if(data.userTxnData.length>0){
				var payMode=parseInt($("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(5)").css("width"));
				var status=parseInt($("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(8)").css("width"));
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(1)").css("width",$("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(1)").css("width"));
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(2)").css("width",$("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(2)").css("width"));
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(3)").css("width",$("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(3)").css("width"));
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(4)").css("width",$("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(4)").css("width"));
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(5)").css("width",payMode);
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(6)").css("width",$("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(6)").css("width"));
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(7)").css("width",$("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(7)").css("width"));
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(8)").css("width",status);
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(9)").css("width",$("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(9)").css("width"));
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(10)").css("width",$("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(10)").css("width"));
			}else{
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(1)").css("width",118);
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(2)").css("width",130);
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(3)").css("width",110);
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(4)").css("width",80);
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(5)").css("width",60);
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(6)").css("width",98);
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(7)").css("width",187);
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(8)").css("width",170);
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(9)").css("width",145);
				$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(10)").css("width",214);
			}

	}else{
		swal("Error!",data.message,"error");
	}
}
function getVendorCustomerItemsSuccess(data){
	if(data.result){
		data=data.items;
		var html=getVendorCustomerBPHTML(data);
		$('#customerVendorSearchItems').html(html);
	}else{
		swal("Error!",data.message,"error");
	}
}
/*Vendor Customer Search Starts*/


/*Vendor/Customer Statement Starts*/
$(document).ready(function(){
	$('a#transactionStatementId').on('click',function(){
		$("#supportCenter").fadeOut(100);
		getVendorCustomerStatements();
		$('#transactionStatementCriteriaTable').find('input,select').val('');
		$('#transactionStatementTable tbody').empty();
	});
	$('.txnStmt').on('click',function(){
		getVendorCustomerStatements($(this).attr('data-type'));
	});
});

function getVendorCustomerStatements(getType){
	var json={},id=GetURLParameter('entityId'),type=GetURLParameter('entityType'),success;
	var org=GetURLParameter('accountOrganization');
	if(isEmpty(id)) {
		swal('Invalid!','Cannot find the details. Please follow the link from the mail again.','error');
	}else{
		json.id=id;
		json.type=type;
		json.org=org;
		json.branch=$('#bnchTransactionStatementId').val();
		json.from=$('#transactionStatementFromDate').val();
		json.to=$('#transactionStatementToDate').val();
		if(isEmpty(getType)){
			json.getType='1';
			success='displayTransactionStatementSuccess';
		}else{
			json.getType=getType;
			if (getType==2||getType==3) {
				(''==$('#bnchTransactionStatementId').val())?json.branchName='':json.branchName=$('#bnchTransactionStatementId option:selected').text();
				success='exportTransactionStatementSuccess';
			}else{
				success='displayTransactionStatementSuccess';
			}
		}
		ajaxCall('/vendorCustomer/statements', json, '', '', '', '', success, '', true);
	}
}
function exportTransactionStatementSuccess(data){
	(data.result) ? window.open(data.message) : swal("Error!",data.message,"error");
}

function displayTransactionStatementSuccess(data){
	if(data.result){
		data=data.statements;
		var html='';
		if(!isEmpty(data) && data.length>0){
			for(var i in data){
				if(0==i){
					html+='<tr><td></<td><td></td><td></td><td></td><td>'+data[i].balance+'</td></tr>';
				}else{
					html+='<tr><td><a class="transactionLookUp" href="#pendingExpense" onclick="showCashNBankModal(this);">'+data[i].name+'</a></<td><td>'+data[i].created+'</td><td>'+data[i].debit+'</td>';
					html+=('credit'===data[i].credit)?'<td>'+data[i].debit+'</td>':'<td>'+data[i].credit+'</td>';
					html+='<td>'+data[i].balance+'</td></tr>';
				}
			}
			$('#transactionStatementTable>tbody').html(html);
		}
	}
}

/*Vendor/Customer Statement Ends*/




/*Warehouse Starts*/
$(document).ready(function(){
	$('.createWarehouseCancel').on('click',function(){
		$('#warehouse-form-container').slideUp();
		$('#branchesList').slideDown();
	});
	$('#viewwarehouseform-container').on('click',function(){
		$('.viewwarehouseform-container').is(':visible')?$('.viewwarehouseform-container').slideUp():$('.viewwarehouseform-container').slideDown();
	});
	$('.viewwarehouseform-container-close').on('click',function(){
		$('.viewwarehouseform-container').slideUp();
	});
});
/*Warehouse Ends*/
/*vendor supplier add location to the master database start*/
function addLocationToContainer(elem){
	var multipleVendorSellerInput=$("#multipleVendorSellerInput").val();
	if(multipleVendorSellerInput!=""){
		var jsonData = {};
		jsonData.userEmail = $("#hiddenvendcustemail").text();
		jsonData.locationName=multipleVendorSellerInput;
		ajaxCall('/add/vendSupplierLocation', jsonData, '', '', '', '', 'vendSupplierLocationSuccess', '', false);
	}
}
function vendSupplierLocationSuccess(data){
	if(data.result){
		//add to the select optio and rebuild the multiselect container
		$("#vendCustTransactions select[id='vendorsellerBranches']").append('<option value="'+data.listedLocationData[0].listedLocation+'">'+data.listedLocationData[0].listedLocation+'</option>');
		$("#vendSellerAccounts select[id='vendorsellerBranches']").append('<option value="'+data.listedLocationData[0].listedLocation+'" selected="selected">'+data.listedLocationData[0].listedLocation+'</option>');
		$('.multipleVendorSeller').multiselect('rebuild');
		$("input[type='checkbox'][value="+data.listedLocationData[0].locationName+"]").attr('checked',true);
		var length=0;
		$("#vendSellerAccounts select[id='vendorsellerBranches'] option").each(function () {
			if($(this).val()!="" && $(this).attr('selected')=="selected"){
				length=length+1;
			}
		});
		var buttonText="";
		if (length == 0) {
			buttonText='None selected <b class="caret"></b>';
        }
        else if (length > 6) {
        	buttonText=length + ' selected  <b class="caret"></b>';
        }else{
        	buttonText=length + ' selected  <b class="caret"></b>';
        }
		$(".newSupplierVendorItemsRegister").find('button[class="multiselect dropdown-toggle btn"]').html(buttonText);
		$("#multipleVendorSellerInput").val("");
	}
}
/*vendor supplier add location to the master database end*/

function exportReportInventory(elem){
	var exportType="";
	var elemType=$(elem).attr('id');
	if(elemType=="exportReportInventoryXLSX"){
		exportType="xlsx";
	}
	if(elemType=="exportReportInventoryPDF"){
		exportType="pdf";
	}
	var jsonData = {};
	var fmDate=$("#reportInventoryFromDate").val();
	var tDate=$("#reportInventoryToDate").val();
	jsonData.accessKey="IDKrg0f9dHMhVROS";
	jsonData.email=$("#hiddenuseremail").text();
	jsonData.branch=$("#selectBnchReportInventoryId option:selected").val();
	jsonData.fromDate=fmDate;
	jsonData.toDate=tDate;
	jsonData.riexporttype=exportType;
	if(fmDate=="" || typeof fmDate=="undefined" || tDate=="" || typeof tDate=="undefined"){
		swal("Invalid!","Please Provide The Date Range For Your Inventory Report","error");
		return true;
	}
	var url = '/export/reportInventory';
	if(fmDate!="" && typeof fmDate!="undefined" && tDate!="" && typeof tDate!="undefined"){
		//ajaxCall('/export/reportInventory', jsonData, '', '', '', '', 'exportReportInventorySuccess', '', true);
		downloadFile(url, "POST", jsonData, "Error on downloading Inventory report!");
	}
}

function exportPeriodicInventory(elem){
	var exportType="";
	var elemType=$(elem).attr('id');
	if(elemType=="exportPeriodicInventoryXLSX"){
		exportType="xlsx";
	}else if(elemType=="exportPeriodicInventoryPDF"){
		exportType="pdf";
	}
	var jsonData = {};
	var fmDate=$("#periodicInventoryFromDate").val();
	var tDate=$("#periodicInventoryToDate").val();
	jsonData.accessKey="IDKrg0f9dHMhVROS";
	jsonData.email=$("#hiddenuseremail").text();
	jsonData.branch=$("#selectBnchPeriodicInventoryId option:selected").val();
	jsonData.fromDate=fmDate;
	jsonData.toDate=tDate;
	jsonData.piexporttype=exportType;
	if(fmDate=="" || typeof fmDate=="undefined" || tDate=="" || typeof tDate=="undefined"){
		swal("Invalid!","Please Provide The Date Range For Your Periodic Inventory","error");
		return true;
	}
	if(fmDate!="" && typeof fmDate!="undefined" && tDate!="" && typeof tDate!="undefined"){
		//ajaxCall('/export/periodicInventory', jsonData, '', '', '', '', 'exportPeriodicInventorySuccess', '', true);
		downloadFile('/export/periodicInventory', "POST", jsonData, "Error on downloading periodic inventory!");
	}
}

/*
function exportPeriodicInventorySuccess(data){
	if(data.result){
		var dt = new Date().toString();
		var fileName=data.piFileData[0].fileName;
		var url='assets/report/'+fileName+'?unique='+dt;
	    var childwindow=window.open(url);
	}else{
		$.unblockUI();
	}
}

function exportReportInventorySuccess(data){
	if(data.result){
		var dt = new Date().toString();
		var fileName=data.riFileData[0].fileName;
		var url='assets/report/'+fileName+'?unique='+dt;
	    var childwindow=window.open(url);
	}else{
		$.unblockUI();
	}
}
*/
$(document).ready(function(){
	$('#userOptions a .click-options').bind("DOMSubtreeModified",function(){
	  if($('iframe.goog-te-banner-frame').parent().is(':visible')){
		  $('#head-nav').css('top','40px');
		  $('.google-te-iframe').addClass('gt-open');
	  }else{
		  $('#head-nav').css('top','0px');
		  $('.google-te-iframe').removeClass('gt-open');
	  }
	});
});




$(document).on('change', '.btn-file :file', function() {
  var input = $(this),
      numFiles = input.get(0).files ? input.get(0).files.length : 1,
      label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
  input.trigger('fileselect', [numFiles, label]);
});

$(document).ready( function() {
    $('.btn-file :file').on('fileselect', function(event, numFiles, label){
        var input = $(this).parents('.input-group').find(':text'),
            log = numFiles > 1 ? numFiles + ' files selected' : label;
        if( input.length ) {
            input.val(log);
        } else {
            if( log ) swal("Error!",log,"error");
        }
    });
});

function getNotesCount(){
	var jsonData = {};
	jsonData.email = '@email';
	ajaxCall('/notes/count', jsonData, '', true, '', '', 'getNotesCountSuccess', '', false);
}

$(document).ready(function() {
	$(".newEntityCreateButton").click(function(){
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var isSingleUserDeploy = $("#isDeploymentSingleUser").html();
		$("#limitallfrom").each(function () {
			   $(this).val("");
		});
		$("#limitallto").each(function () {
			   $(this).val("");
		});
		$("#userRoleSpecs").hide();
		$("#usraddress").val("");
		$(".duplabel").html("");
		$("#passsword").show();
		var entityform=this.id;
		if(entityform=="newVendorform-container"){
			$(".newVendorGroupform-container").hide();
			clearVendorPopupHidden();
			clearVendorForm("newVendorform-container");
			populatevendorgroupdropdown();
			
		}
		if(entityform=="newVendorGroupform-container"){
			$(".newVendorform-container").hide();
			//function to populate list of vendor group
			populatevendorgrouplist();
		}
		if(entityform=="newCustomerform-container"){
			$(".newCustomerGroupform-container").hide();
			clearCustomerPopupHidden(); //to clear hidden fields
			clearCustomerForm("newCustomerform-container");
			$(".para-tm3-bm0").hide();
			// $('#custOpeningBalanceAdvPaid').hide();
			// $('#custOpeningBalance').hide();
			$("#custOpeningBalance").show();
			$("#custOpeningBalanceAdvPaid").show();
			// $('#exculdeAdvCreLimCheck').hide();
			$('#custTranExceedCredLim').hide();
			$('#custCreditLimit').hide();
			$('#custdaysOfCredit').hide();
			populatecustomergroupdropdown();
		}
		if(entityform=="newCustomerGroupform-container"){
			$(".newCustomerform-container").hide();
			//function to populate list of customer group
			populatecustomergrouplist();
		}
		if(entityform=="hiredLabourform-container"){
			$(".newlabourform-container").hide();
		}
		if(entityform=="newbranchform-container"){
			customMethod1();
			var branchEntityHidId="";
			listAlertUser(branchEntityHidId);
			$('.bnchButtonDiv').fadeIn();
			$('#assignBranchAdmin').html('<option value="">--Please Select--</option>');
			$('#branchesList').slideDown();
			$('#warehouse-form-container,.viewwarehouseform-container').slideUp();
			$('#confirmgstin').show();
			$('#isnewbranchHidden').val(1);
			$(".gstinInputCls").removeAttr("disabled");
			$("#branchstate").removeAttr("disabled");
			$("#branchstate option").filter(function () {return $(this).val()=='';}).prop("selected", "selected");
		}
		if(entityform=="newwarehouseform-container"){
			$('.bnchButtonDiv').hide();
			$('#branchesList,#branch-form-container,.viewwarehouseform-container').slideUp();
		}
		if(entityform=="newSearchTransactionform-container"){
			$(".newVendorAccountDetailsform-container").hide();
		}
		if(entityform=="newVendorAccountDetailsform-container"){
			$(".newSearchTransactionform-container").hide();
		}
		if(entityform=="newUserform-container"){
			$('.claim-container').slideUp(400);
		}
		if(entityform=="newProjectform-container"){
			hideProjectHiringTable();
		}
		if(entityform=="newlabourform-container"){
			$(".submitHiringRequest").text("Submit Request");
			 $(".submitHiringRequest").parent().css('margin-left','835px');
			$(".submitHiringRequest").removeAttr("disabled");
			$(".hiredLabourform-container").hide();
			$('#hiringDiv').remove();
			$("#hiddenRequestLabourId").val("");
			$("#newlabourform-container input").val("");
			$("#newlabourform-container textarea").val("");
			$("#newlabourform-container select option:selected").each(function () {
				   $(this).removeAttr('selected');
			});
			$("#newlabourform-container select option:first").prop("selected", "selected");
			populateprojectsreportsto();
		}
		$("#bnchDynmStatutory").html("");
		$(".newItemform-container").hide();
		$(".newCategoryform-container").hide();
		$("."+entityform+"").slideDown('slow');
		$("#keyOffCity").val("");
		$('.notify-success').hide();
		$("a[id*='form-container-close']").attr("href",location.hash);
		$('.'+entityform+' input[type="hidden"]').val("");
		$('.'+entityform+' input[type="text"]').val("");
		$('.'+entityform+' textarea').val("");
		$('.'+entityform+' input[type="password"]').val("");
		$('.'+entityform+' select option:first').prop("selected", "selected");
		$('.'+entityform+' select[class="countryPhnCode"]').each(function () {
			$(this).find('option:first').prop("selected", "selected");
		});
		$('.'+entityform+' select[class="countryDropDown"]').each(function () {
			$(this).find('option:first').prop("selected", "selected");
		});
		$("#recurrences").find('option:first').prop("selected", "selected");
		$("#bnkActType").find('option:first').prop("selected", "selected");
		$("#custfutPayment").find('option:first').prop("selected", "selected");
		$(".budgetDisplay").text("");
		$(".actualbudgetDisplay").text("");
		$(".branchAvailablePettyCash").html("");
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
		$("#projectenddate").attr("class","datepicker");
		$("#projectenddate").addClass('calendar');
		$(".label-list li[class!='orgclass']").html("");
		$("tr[id*='dynBranchOfficer']").remove();
		$("tr[id*='dynBranchStatutory']").remove();
		$("tr[id*='dynBranchOperRem']").remove();
		$("tr[id*='dynBranchKeyDep']").remove();
		$("tr[id*='dynBranchInsurence']").remove();
		$("tr[id*='dynBranchBnkAct']").remove();
		$("tr[id*='dynBranchTax']").remove();
		$("tr[id*='dynBranchInputTax']").remove();
		$("tr[id*='dynProjPos']").remove();
		$("#branchPremiseDiv").hide();
		$("#bnchcurrency option:first").prop("selected", "selected");
		$("#bnchFacility").val("");
		$("#taxName").val("");
		$("#taxRate").val("");
		$("#inputTaxName").val("");
		$("#inputTaxRate").val("");
		$("#panNoVend").val("");
		$("#natureOfVend option:first").prop("selected", "selected");

		$("#bnchDeposit").val("");
		$("#noOfKeys option:first").prop("selected","selected");
		$("#bnchAddress").val("");
		$("#custdaysCreditLabel").hide();
		$("#custdaysOfCredit").hide();
		var maximumYear=new Date().getFullYear()+30;
		$(".datepicker").datepicker({
			changeMonth : true,
			changeYear : true,
			dateFormat:  'MM d,yy',
			yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
			onSelect: function(x,y){
				var elemName=$(this).attr('name');
				if(elemName=='projectenddate'){
					if($("#projectstartdate").val()==""){
						$(this).val("");
					}
					var d1 = new Date($("#projectenddate").val())
					var d2 = new Date($("#projectstartdate").val())
					if (d1 < d2) {
					   swal("Invalid data!","project end date cannot be less than project start date","error");
					   $(this).val("");
					   return true;
					}
				}
				$(this).focus();
			}
		});
		$("#userRole").find('option[value="1"]').remove();
		$("#userRole").find('option[value="2"]').remove();
		$("#userRole").find('option[value="8"]').remove();
		$("#userRole").find('option[value="9"]').remove();
		$("#userRole").find('option[value="12"]').remove();
		$("#userRole option:selected").each(function () {
			   $(this).removeAttr('selected');
		});
		$('#userRole').multiselect('rebuild');
		$("#projectpositionqualification option:selected").each(function () {
			   $(this).removeAttr('selected');
		});
		$('#projectpositionqualification').multiselect('rebuild');
		$("#projectPositionBranch option:first").prop("selected", "selected");
		$('#userTxnQuestion').multiselect({
			buttonWidth: '150px',
			maxHeight:   150,
			includeSelectAllOption: true,
			enableFiltering :true,
			/*
			buttonText: function(options) {
			  if (options.length == 0) {
					  return 'None selected <b class="caret"></b>';
				  }
				  else if (options.length > 6) {
					  return options.length + ' selected  <b class="caret"></b>';
				  }
				  else {
					  var selected = '';
					  options.each(function() {
				  selected += $(this).text() + ', ';
					  });

					  return options.length + ' selected  <b class="caret"></b>';
			  }
			},*/
			onChange: function(element, checked) {
			}
		});
		$("#userTxnQuestion option:selected").each(function () {
			   $(this).removeAttr('selected');
		});
		$('#userTxnQuestion').multiselect('rebuild');
		$(".multipleDropdown option:selected").each(function () {
			   $(this).removeAttr('selected');
		});
		$('.multipleDropdown').multiselect('rebuild');
		$("#projectBranch option:selected").each(function () {
			   $(this).removeAttr('selected');
		});
		$('#projectBranch').multiselect('rebuild');
		$("#partBasedTransaction option:selected").each(function () {
			   $(this).removeAttr('selected');
		});
		$('#partBasedTransaction').multiselect('rebuild');

		clearBranchBillDetails();
		/*
		$("#vendorBranch option:selected").each(function () {
			   $(this).removeAttr('selected');
		});
		$('#vendorBranch').multiselect('rebuild');
		$("#customerBranch option:selected").each(function () {
			   $(this).removeAttr('selected');
		});
		$('#customerBranch').multiselect('rebuild'); */
		resetVendorCustomerBranch('customerBranchDiv');
		resetVendorCustomerBranch('vendorBranchDiv');
		$("#userBranch option:first").prop("selected","selected");
		$("#transactionCreationInBranch option:selected").each(function () {
			$(this).removeAttr('selected');
		});
		$("#transactionCreationInBranch option:selected").each(function () {
			$(this).removeAttr('selected');
		});
		$('#transactionCreationInBranch').multiselect('rebuild');
		$("#transactionApprovalInBranch option:selected").each(function () {
			$(this).removeAttr('selected');
		});
		$('#transactionApprovalInBranch').multiselect('rebuild');
		$("#transactionAuditorInBranch option:selected").each(function () {
			$(this).removeAttr('selected');
		});
		$('#transactionAuditorInBranch').multiselect('rebuild');
		$("#transactionCreationForProject option:selected").each(function () {
			$(this).removeAttr('selected');
		});
		$('#transactionCreationForProject').multiselect('rebuild');
		$("#transactionApprovalForProject option:selected").each(function () {
			$(this).removeAttr('selected');
		});
		$('#transactionApprovalForProject').multiselect('rebuild');
		$("#partBasedTransaction option:selected").each(function () {
			$(this).removeAttr('selected');
		});
		$('#partBasedTransaction').multiselect('rebuild');
		$('input[name="checkCOA"]:checkbox:checked').map(function () {
			$('input[name="checkCOA"][value="'+this.value+'"]').prop('checked', false);
		}).get();
		$('input[name="coaAmountLimit"]').map(function () {
			if($(this).val().trim()!=""){
				$(this).val("0.0");
			}
		}).get();
		$('input[name="coaAmountLimitTo"]').map(function () {
			if($(this).val().trim()!=""){
				$(this).val("0.0");
			}
		}).get();
		$('input[name="checkAction"]:checkbox:checked').map(function () {
			$(this).prop('checked', false);
		}).get();
		$('input[name="vendoritemcheck"]:checkbox:checked').map(function () {
			$(this).prop('checked', false);
		}).get();
		$('input[class="langProfEnable"]:checkbox:checked').map(function () {
			$(this).prop('checked', false);
		}).get();
		$('input[name="langProfValues1"]').each(function () {
			$(this).val("");
		});
		$('input[name="langProfValues2"]').each(function () {
			$(this).val("");
		});
		$('input[name="langProfValues3"]').each(function () {
			$(this).val("");
		});
		$('input[name="unitPrice"]').each(function () {
			$(this).val("0.0");
		});

		$('input[name="customeritemcheck"]:checkbox:checked').each(function () {
			$(this).prop('checked', false);
		});
		$('input[class="langProfEnable"]:checkbox:checked').each(function () {
			$(this).prop('checked', false);
		});
		$('input[class="langProfValues1"]').map(function () {
			$(this).val("");
		}).get();
		$('input[class="langProfValues2"]').map(function () {
			$(this).val("");
		}).get();
		$('input[class="langProfValues3"]').map(function () {
			$(this).val("");
		}).get();
		$('input[name="custDiscount"]').each(function () {
			$(this).val("0.0");
		});
		$('input[name="coaAmountLimit"]').each(function () {
			$(this).val("0.0");
		});
		$('input[name="coaAmountLimitTo"]').each(function () {
			$(this).val("0.0");
		});
		$("#vendordropdown").text("None Selected");
		$("#vendordropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
		$("#customerdropdown").text("None Selected");
		$("#customerdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
		$("#transactioncoacreatordropdown").html("None Selected &#8711;");
		$("#txnIncoaapproverdropdown").html("None Selected &#8711;");

		$("#projectLabourProficiency").html("None Selected &#8711;");
		$("#address").val("");
		$(".multiBranch").each(function () {
			$(this).removeAttr('selected');
		});
		$('.multiBranch').multiselect('rebuild');
		$('.hiddenmodelid').each(function() {
			$(this).val('');
		});
		$(".travelClaim-max-daily-table input[type='text']").each(function() {
			if (!$(this).hasClass('search-image')) {
				$(this).val("0.0");
			}
		});
		$(".travelClaim-fixed-daily-table input[type='text']").each(function() {
			if (!$(this).hasClass('search-image')) {
				$(this).val("0.0");
			}
		});
		$(".travelClaimKnowledgeLibrary textarea").each(function() {
			$(this).val("");
		});
		$(".travelClaimKnowledgeLibrary select").each(function() {
			$(this).find('option:first').prop("selected","selected");
		});
		$('.travelClaim-max-daily-table .multiselect').html("None Selected &#8711;");
		$("#travelBoardingLodgingDropDown").html("None Selected &#8711;");
		$("#expenseClaimDropDown").html("None Selected &#8711;");
		$("#travelClaimGroupName").val("");
		$("#travelGroupEntityHidden").val("");
		$("#expenseClaimGroupName").val("");
		$("#expenseGroupEntityHidden").val("");
		if(isSingleUserDeploy == "true") {
			var custCount = 0;
			var vendCount = 0;
			var branchCustomerCount = 0;
			var branchVendorCount = 0;
			$('input[name="customeritemcheck"]').map(function () {
				$(this).prop('checked', true);
				custCount += 1;
			}).get();

			$('input[name="vendoritemcheck"]').map(function () {
				$(this).prop('checked', true);
				vendCount += 1;
			}).get();

			$('input[name="custDiscount"]').each(function () {
				$(this).val("100");
			});

			$('#customerSetup .customerVendorBranchCls').each(function () {
				$(this).find('input[type="checkbox"]').prop('checked', true);
				branchCustomerCount += 1;
			});

			$('#vendorSetup .customerVendorBranchCls').each(function () {
				$(this).find('input[type="checkbox"]').prop('checked', true);
				branchVendorCount += 1;
			});

			$("#vendordropdown").text(""+vendCount+" Items Selected");
			$("#vendordropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
			$("#customerdropdown").text(""+custCount+" Items Selected");
			$("#customerdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
			$("#customerSetup").find("#customerBranchDropdownBtn").text(""+branchCustomerCount+" Items Selected");
			$("#customerSetup").find("#customerBranchDropdownBtn").append("&nbsp;&nbsp;<b class='caret'></b>");
			$("#vendorSetup").find("#vendorBranchDropdownBtn").text(""+branchVendorCount+" Items Selected");
			$("#vendorSetup").find("#vendorBranchDropdownBtn").append("&nbsp;&nbsp;<b class='caret'></b>");
			$("#custfutPayment").val("2");
			//$("#futurePayment").val("2");

		}else {
			$('input[name="customeritemcheck"]:checkbox:checked').each(function(){
		 		$(this).prop('checked', false);
			});

			$('input[name="vendoritemcheck"]:checkbox:checked').each(function(){
				 $(this).prop('checked', false);
			});

			$('input[name="custDiscount"]').each(function () {
				$(this).val("0");
			});
			$("#vendordropdown").text("None Selected");
			$("#vendordropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
			$("#customerdropdown").text("None Selected");
			$("#customerdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");


		}
		if(entityform=="newVendorAccountDetailsform-container"){
			sellerVendorItemsListWithPricings();
		}
		if(entityform=="newSellerAccountDetailsform-container"){
			sellerVendorItemsListWithPricings();
			var jsonData = {};
			jsonData.email = $('#hiddenvendcustemail').text();
			ajaxCall('/seller/getPricings', jsonData, '', true, '', '', 'getPricingsSuccess', '', true);
		}
		$.unblockUI();
	});
});

var downloadFile = function(url, method, jsonData, errorMessage){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });

	// Use XMLHttpRequest instead of Jquery $ajax
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function() {
	    var a;
	    if (xhttp.readyState === 4 && xhttp.status === 200) {
    	    // Trick for making downloadable link
	        a = document.createElement('a');
	        a.href = window.URL.createObjectURL(xhttp.response);
	        // Give filename you wish to download
	        let disposition = xhttp.getResponseHeader('Content-Disposition');
	        if(disposition == null){
	        	swal("Records not found", "Please try for other period ", "error");
	        }else{
		        a.download = getDownloadFileName(disposition);
		        a.style.display = 'none';
		        document.body.appendChild(a);
		        a.click();
	    	}
			setTimeout(function(){
				$.unblockUI(); },5000);
	    }else if(xhttp.status == 401){
			doLogout();
			$.unblockUI();
		}else if(xhttp.status == 500){
			$.unblockUI();
    		swal(errorMessage, "Please retry, if problem persists contact support team", "error");
    	}

	};
	// Post data to URL which handles post request
	xhttp.open(method, url, true);
	xhttp.setRequestHeader("Content-Type", "application/json");
	xhttp.setRequestHeader("X-AUTH-TOKEN", window.authToken);
	// You should set responseType as blob for binary responses
	xhttp.responseType = 'blob';
	xhttp.send(JSON.stringify(jsonData));
}

var getDownloadFileName = function(disposition){
    var filename = "";
    if (disposition && disposition.indexOf('attachment') !== -1) {
        var filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
        var matches = filenameRegex.exec(disposition);
        if (matches != null && matches[1]) {
          filename = matches[1].replace(/['"]/g, '');
        }
    }
    return filename;
}

var logDebug = function(methodName){
	if(!DEBUG){
		return false;
	}
	var dateTmp = new Date();
	var millisecon = dateTmp.getTime();
	console.log(methodName +": " +millisecon);
}

function validateFullGSTIN(elem,stateCodeId) {
	var gstIn = $.trim($(elem).val());
	if(gstIn !== "" && gstIn.length > 0) {
			var code = $(elem).closest("div").find("#"+stateCodeId).val();
			var status = validateGSTINStateCode(code);
    		if(status == true) {
				var pos = gstIn.length-1;
				var ch = gstIn.charAt(gstIn.length-1);
				var codeStatus = validatePositionINGSTIN(pos,ch);
				if(codeStatus == false) {
					$(elem).val("");
					return false;
				}
			}else {
				 swal("Invalid state!", "provide valid Indian state code in GSTIN", "error");
				 $(elem).val("");
				 $(elem).prev('.gstinInputCls').focus();
				 return false;
			}
	}
	return true;
}

function validatePositionINGSTIN(pos,ch) {
			if(pos >= 0 && pos <= 4) {
    			 var letters = /^[a-zA-Z]+$/;
  				 if(!ch.match(letters)) {
  				 	 swal("Invalid GSTIN!", "In GSTIN 3rd to 7th digit allows only Alphabets", "error");
     				 return false;
     			 }
			}
			if(pos >=5 && pos < 9) {
    			 var number = /^[0-9]+$/;
  				 if(!ch.match(number)) {
  				 	 swal("Invalid GSTIN!", "In GSTIN 8th to 11th digit allows only Numbers", "error");
     				 return false;
     			 }
			}
			if(pos === 9) {
    			 var letters = /^[a-zA-Z]+$/;
  				 if(!ch.match(letters)) {
  				 	 swal("Invalid GSTIN!", "In GSTIN 12th digit allows only Alphabets", "error");
     				 return false;
     			 }
			}
			if(pos === 10) {
    			 var number = /^[0-9a-zA-Z]+$/;
  				 if(!ch.match(number)) {
  				 	 swal("Invalid GSTIN!", "In GSTIN 13th digit allows Alphabets and Numbers", "error");
     				 return false;
     			 }
			}
			if(pos === 11) {
   			 var number = /^[zZ]+$/;
 				 if(!ch.match(number)) {
 				 	 swal("Invalid GSTIN!", "In GSTIN 14th digit allows only z or Z", "error");
    				 return false;
    			 }
			}
			if(pos === 12) {
   			 var number = /^[0-9a-zA-Z]+$/;
 				 if(!ch.match(number)) {
 				 	 swal("Invalid GSTIN!", "In GSTIN 15th digit allows Alphabets and Numbers", "error");
    				 return false;
    			 }
			}

	return true;
}
