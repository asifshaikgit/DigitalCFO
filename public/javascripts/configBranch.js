$(document).ready(function () {
    $('body').on('blur', 'input[name="keyoffEmailId"]', function () {
        repopulateBranchUsers();
    });
});

$(document).ready(function () {
   $('#branchSetupId').click(function () {
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var url = "/branch/getAllBranchDetails";
    $.ajax({
        url: url,
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        method: "GET",
        contentType: 'application/json',
        success: function (data) {
            let incomeExpenseItems = ""; let expenseItems = ""; let expenseTaxTable = ""; let branchTrList = "";
            var projectlisttable = $("#branchTable tbody");
            projectlisttable.html('');
			for(let i=0;i<data.branchListData.length;i++){
				let countryName="";
				if(data.branchListData[i].country!=""){
					countryName = $("#branchCountry").find('option[value='+data.branchListData[i].country+']').text();
				}
				incomeExpenseItems += ('<li class="branchEntity'+data.branchListData[i].id+'"><div class="chartOfAccountContainer"><img id="branchEntity'+data.branchListData[i].id+'" src="/assets/images/minus.png" style="margin-top:-2px;" id="getNodeChild"></img><font class="color-grey"><b id="branchEntity'+data.branchListData[i].id+'" style="margin-left:2px;">'+data.branchListData[i].name+'</b></font></div>');

				incomeExpenseItems += ('<ul id="mainBranchIncomeChartOfAccount" class="treeview-black mainBranchIncomeChartOfAccount">');
				incomeExpenseItems += ('<li id="branchEntity'+data.branchListData[i].id+'"><div class="chartOfAccountContainer"><img id="branchEntity'+data.branchListData[i].id+'" src="/assets/images/new.v1370889834.png" style="margin-top:-2px;" id="getNodeChild" onclick="javascript:getChildIncomesChartOfAccount(this,\''+data.gstCountryCode+'\');"></img><font class="color-grey"><b style="margin-left:2px;">Incomes</b></font></div></li></ul></li>');
            
				expenseItems += ('<li class="branchEntityRcm'+data.branchListData[i].id+'"><div class="chartOfAccountContainer"><img id="branchEntityRcm'+data.branchListData[i].id+'" src="/assets/images/minus.png" style="margin-top:-2px;" id="getNodeChild"></img><font class="color-grey"><b id="branchEntityRcm'+data.branchListData[i].id+'" style="margin-left:2px;">'+data.branchListData[i].name+'</b></font></div>');
				expenseItems += ('<ul id="mainBranchExpenceChartOfAccount" class="treeview-black mainBranchExpenceChartOfAccount">');
				expenseItems += ('<li id="branchEntityRcm'+data.branchListData[i].id+'"><div class="chartOfAccountContainer"><img id="branchEntityRcm'+data.branchListData[i].id+'" src="/assets/images/new.v1370889834.png" style="margin-top:-2px;" id="getNodeChild" onclick="javascript:getChildRcmChartOfAccount(this,\''+data.gstCountryCode+'\');"></img><font class="color-grey"><b style="margin-left:2px;">Expence</b></font></div></li></ul></li>');

				branchTrList  += '<tr name="branchEntity'+data.branchListData[i].id+'"><td>'+data.branchListData[i].name+'</td><td>'+data.branchListData[i].branchgstin+'</td><td>'+countryName+'</td><td>'+(data.branchListData[i].location==null?"":data.branchListData[i].location)+'</td><td>'+(data.branchListData[i].phone==null?"":data.branchListData[i].phone)+'</td><td><button href="#branchSetup" class="btn btn-submit" onClick="showBranchEntityDetails(this);" id="show-entity-details'+data.branchListData[i].id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td>';
				if(data.branchListData[i].actionText == "Deactivate") {
					branchTrList += '<td><button href="#branchSetup" class="btn btn-submit" onClick="deactivateBranchEntityDetails(this);" id="deactivate-entity-details' + data.branchListData[i].id + '"><i class="far fa-trash-alt fa-lg pr-5"></i>' + data.branchListData[i].actionText + '</button></td></tr>';
				}else{
					branchTrList += '<td><button href="#branchSetup" class="btn btn-submit" onClick="deactivateBranchEntityDetails(this);" id="deactivate-entity-details' + data.branchListData[i].id + '"><i class="far fa-check-square fa-lg pr-5"></i>' + data.branchListData[i].actionText + '</button></td></tr>';
				}
			}
			$("#branchTable tbody").append(branchTrList);
			$("#mainBranchIncomeChartOfAccount").append(incomeExpenseItems);
			$("#mainBranchRCMChartOfAccount").append(expenseItems);
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on Save/Update Branch!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
            $('#addBranch').removeAttr("disabled");
        }
    }).done($('.btn-div-top').fadeOut());
   });
});
// 

function downloadOrganizationBranchTemplate() {
    var jsonData = {};
    jsonData.useremail = $("#hiddenuseremail").text();
    var url = "/config/downloadOrgBranchTemplate";
    downloadFile(url, "POST", jsonData, "Error on downloading Branch Template!");
}

function repopulateBranchUsers() {
    var val = $('#assignBranchAdmin').val();
    $('#assignBranchAdmin').html('<option value="">--Please Select</option>');
    $('input[name="keyoffEmailId"]').each(function () {
        $('#assignBranchAdmin').append('<option value="' + $(this).val() + '">' + $(this).val() + '</option>');
    });
    $('#assignBranchAdmin').find('option[value="' + val + '"]').prop('selected', true);
}

function addBranch() {
	$("#addBranch").attr("disabled", "disabled");
    checkEditEmail(onFocusClickEmail);
    if (checkEditEmailResult || checkEditEmailResult === undefined) {
        onFocusClickEmail = "";
        checkEditEmailResult = "";
        var branchName = $("#bnchName").val();
        if (branchName === "") {
            swal("Invalid Branch Name!", "Please Fill in the Branch Name", "error");
            return true;
        }
        var branchAddress = $("#branchAddress").val();
        if (branchAddress === null || branchAddress === "") {
            swal("Invalid Branch Address!", "Please provide valid address for the Branch", "error");
            return false;
        }
        var gstinCode = "";
        var stateCode = "";
        var gstCountryCode = $("#gstCountryCode").val();
        if (gstCountryCode != "" && gstCountryCode != null) {
            var stateGstinCode = $("#gstinput").val();
            var gstinsecondPart = $("#gstinput2").val();
            gstinCode = stateGstinCode + gstinsecondPart;
            // if(stateGstinCode === "" && gstinsecondPart !== ""){
            if (stateGstinCode === "") {
                swal("Invalid GSTIN!", "State code in GSTIN cannot be empty", "error");
				$('#addBranch').removeAttr("disabled");
                return false;
            } else if (stateGstinCode !== "" && gstinsecondPart === "") {
                swal("Invalid GSTIN!", "Second part of GSTIN cannot be empty", "error");
				$('#addBranch').removeAttr("disabled");
                return false;
            }

            if ((gstinCode.length > 2 && gstinCode.length < 15) || gstinCode.length > 15) {
                swal("Invalid GSTIN!", "Please provide correct GSTIN", "error");
				$('#addBranch').removeAttr("disabled");
                return false;
            }
            var stateGstinCodeconf = $("#gstinputconfirm").val();
            var gstinsecondParconf = $("#gstinput2Confirm").val();
            var gstinCodeconf = stateGstinCodeconf + gstinsecondParconf;
            if (gstinCode !== gstinCodeconf) {
                swal("Invalid GSTIN!", "GSTIN and confirm GSTIN does not match.", "error");
				$('#addBranch').removeAttr("disabled");
                return false;
            }
            stateCode = $("#branchstate option:selected").val();
            if (stateCode !== stateGstinCode) {
                swal("Invalid GSTIN or State!", "Please provide correct GSTIN and State", "error");
				$('#addBranch').removeAttr("disabled");
                return false;
            }
        }

        var bnchBankActAccountType = $('select[name="bnkActType"] option:selected').map(function () {
            return this.value;
        }).get();
        var bnchBankActOpeningBalance = $('input[name="bnkActOpeningBalance"]').map(function () {
            return this.value;
        }).get();

        //check for negative balance
        /* for (var i = 0; i < bnchBankActOpeningBalance.length; i++) {
            if (bnchBankActOpeningBalance[i] < 0 || bnchBankActOpeningBalance[i] >= 0) {
                if (bnchBankActAccountType[i] == 2 || bnchBankActAccountType[i] == 3 || bnchBankActAccountType[i] == 4 || bnchBankActAccountType[i] == 8) {
                    swal("Invalid Opening Balance!", "For this account type opening balance should be greater than 0.", "error");
					$('#addBranch').removeAttr("disabled");
                    return false;
                }
            }
         }*/

        //branch basic data start
        var jsonData = {};
        var bnchId = $("#branchEntityHiddenId").val();
        var bnchName = branchName
        var bnchOpenDate = $("#bnchOpenDate").val();
        var bnchCountry = $("#branchCountry").val();
        var bnchCurrency = $("#bnchcurrency").val();
        var bnchLocation = $("#bnchLocation").val();
        var bnchPhNoCtryCd = $("#bnchregPhnNocountryCode").val();
        var ctryCodeText = $("#bnchregPhnNocountryCode option:selected").text();
        var bnchPhoneNumber = bnchPhNoCtryCd + "-" + $("#bnchPhoneNumber1").val() + $("#bnchPhoneNumber2").val() + $("#bnchPhoneNumber3").val();

        var useremail = $("#hiddenuseremail").text();
        jsonData.usermail = useremail;
        jsonData.branchId = bnchId;
        jsonData.branchName = bnchName;
        jsonData.branchOpenDate = bnchOpenDate;
        jsonData.branchCountry = bnchCountry
        jsonData.branchCurrency = bnchCurrency;
        jsonData.branchLocation = bnchLocation;
        jsonData.regphnoccode = ctryCodeText;
        jsonData.branchPhoneNumber = bnchPhoneNumber;
        jsonData.branchAddress = branchAddress;
        jsonData.branchStateCode = stateCode;
        jsonData.branchGstin = gstinCode;
        var bnchFacilityCode = $("#bnchFacility").val();
        jsonData.bnchFacility = bnchFacilityCode;

        //branch premise data start
        if (bnchFacilityCode == 1 || bnchFacilityCode == 2) {
            var bnchAggreement = $('input[name$="bnchAggreement"]').val();
            var bnchAggreementValidFrom = $('#bnchPremiseValidityFrom').val();
            var bnchAggreementValidTo = $('#bnchPremiseValidityTo').val();
            var periodicityOfPayment = $("#periodicityOfPayment").val();
            var rentPayable = $("#rentPayable").val();
            var landLordName = $("#landlordName").val();
            var landLordAddress = $("#landlordAddress").val();
            var bankAccountName = $("#bankAccountName").val();
            var bankAccountNumber = $("#bankAccountNumber").val();
            var bankAccountBranch = $("#bankAccountBranch").val();
            var rentRevisionDueOn = $("#rentRevisedDueOn").val();
            var bnchPremiseAlertForAction = $("select[name='bnchPremisealertForAction'] option:selected").val();
            var bnchPremiseAlertForInformation = $("select[name='bnchPremisealertForInformation'] option:selected").val();
            var rentRevisionDueOnRemarks = $("#rentDueOnRemarks").val();
            jsonData.branchAggreement = bnchAggreement;
            jsonData.branchAggreementValidFrom = bnchAggreementValidFrom;
            jsonData.branchAggreementValidTo = bnchAggreementValidTo;
            jsonData.periodOfPayment = periodicityOfPayment;
            jsonData.branchRentPayable = rentPayable;
            jsonData.branchLandlordName = landLordName;
            jsonData.branchlandLordAddress = landLordAddress;
            jsonData.branchbankAccountName = bankAccountName;
            jsonData.branchbankAccountNumber = bankAccountNumber;
            jsonData.branchbankAccountBranch = bankAccountBranch;
            jsonData.branchrentRevisionDueOn = rentRevisionDueOn;
            jsonData.branchPremiseAlertForAction = bnchPremiseAlertForAction;
            jsonData.branchPremiseAlertForInformation = bnchPremiseAlertForInformation;
            jsonData.branchrentRevisionDueOnRemarks = rentRevisionDueOnRemarks;
        }
        //branch officers data start
        var keyOffHiddenIds = $('input[name="bnchkeyOffhiddenId"]').map(function () {
            return this.value;
        }).get();
        var keyOffName = $('input[name="keyoffName"]').map(function () {
            return this.value;
        }).get();

        var keyDesignation = $('input[name="keyoffDesignation"]').map(function () {
            return this.value;
        }).get();
        var keyOffCountry = $('select[name="keyOffCountry"] option:selected').map(function () {
            return this.value;
        }).get();
        var keyOffCity = $('input[name="keyOffCity"]').map(function () {
            return this.value;
        }).get();
        var keyEmail = $('input[name="keyoffEmailId"]').map(function () {
            return this.value;
        }).get();
        var keyOffPhCtryCdVal = $('select[name="bnchKeyOffcountryPhnCode"] option:selected').map(function () {
            return this.value;
        }).get();
        var keyOffPhCtryCd = $('select[name="bnchKeyOffcountryPhnCode"] option:selected').map(function () {
            return this.text;
        }).get();
        var keyPhNo1 = $('input[name="keyoffPhoneNumber1"]').map(function () {
            return this.value;
        }).get();
        var keyPhNo2 = $('input[name="keyoffPhoneNumber2"]').map(function () {
            return this.value;
        }).get();
        var keyPhNo3 = $('input[name="keyoffPhoneNumber3"]').map(function () {
            return this.value;
        }).get();
        var keyOffpersPhCtryCdVal = $('select[name="bnchKeyOffperscountryPhnCode"] option:selected').map(function () {
            return this.value;
        }).get();
        var keyOffpersPhCtryCd = $('select[name="bnchKeyOffperscountryPhnCode"] option:selected').map(function () {
            return this.text;
        }).get();
        var keypersPhNo1 = $('input[name="keyoffpersPhoneNumber1"]').map(function () {
            return this.value;
        }).get();
        var keypersPhNo2 = $('input[name="keyoffpersPhoneNumber2"]').map(function () {
            return this.value;
        }).get();
        var keypersPhNo3 = $('input[name="keyoffpersPhoneNumber3"]').map(function () {
            return this.value;
        }).get();
        var keyOffIdProof = $('input[name="keyOffIdProof"]').map(function () {
            return this.value;
        }).get();
        var keyKYCDoc = $('input[name="keyKYCDoc"]').map(function () {
            return this.value;
        }).get();

        var keyOffHidIds = "";
        var kOffName = "";
        var kDesignation = "";
        var kOffCountry = "";
        var kOffCity = "";
        var kEmail = "";
        var kPhNoCtryCd = "";
        var kPhNo = "";
        var kPersPhNoCtryCd = "";
        var kPersPhNo = "";
        var kIdProof = "";
        var kycDoc ="";
        // $("a[id*='form-container-close']").attr("href", location.hash);
        $("#newbranchform-container-close").attr("href", location.hash);
        for (var i = 0; i < keyOffHiddenIds.length; i++) {
            keyOffHidIds += keyOffHiddenIds[i] + ",";
        }
        for (var i = 0; i < keyOffName.length; i++) {
            if (keyOffName[i] != "") {
                kOffName += keyOffName[i] + ",";
            } else {
                kOffName += " " + ",";
            }
            if (keyDesignation[i] != "") {
                kDesignation += keyDesignation[i] + ",";
            } else {
                kDesignation += " " + ",";
            }
            if (keyOffCountry[i] != "") {
                kOffCountry += keyOffCountry[i] + ",";
            } else {
                kOffCountry += " " + ",";
            }
            if (keyOffCity[i] != "") {
                kOffCity += keyOffCity[i] + ",";
            } else {
                kOffCity += " " + ",";
            }
            if (keyEmail[i] != "") {
                kEmail += keyEmail[i] + ",";
            } else {
                kEmail += " " + ",";
            }
            kPhNoCtryCd += keyOffPhCtryCd[i] + ",";
            kPhNo += keyOffPhCtryCdVal[i] + "-" + keyPhNo1[i] + keyPhNo2[i] + keyPhNo3[i] + ",";
            kPersPhNoCtryCd += keyOffpersPhCtryCd[i] + ",";
            kPersPhNo += keyOffpersPhCtryCdVal[i] + "-" + keypersPhNo1[i] + keypersPhNo2[i] + keypersPhNo3[i] + ",";
            if (keyOffIdProof[i] != "") {
                kIdProof += keyOffIdProof[i] + ",";
            } else {
                kIdProof += " " + ",";
            }

            if (keyKYCDoc[i] != "") {
            	kycDoc += keyKYCDoc[i] + ",";
            } else {
            	kycDoc += " " + ",";
            }
        }
        jsonData.koffhiddIds = keyOffHidIds.substring(0, keyOffHidIds.length - 1);
        jsonData.koffname = kOffName.substring(0, kOffName.length - 1);
        jsonData.koffDesignation = kDesignation.substring(0, kDesignation.length - 1);
        jsonData.koffCountry = kOffCountry.substring(0, kOffCountry.length - 1);
        jsonData.koffCity = kOffCity.substring(0, kOffCity.length - 1);
        jsonData.kemail = kEmail.substring(0, kEmail.length - 1);
        jsonData.kphnoccode = kPhNoCtryCd.substring(0, kPhNoCtryCd.length - 1);
        jsonData.kphno = kPhNo.substring(0, kPhNo.length - 1);
        jsonData.kpersPhNoCtryCd = kPersPhNoCtryCd.substring(0, kPersPhNoCtryCd.length - 1);
        jsonData.kpersPhNo = kPersPhNo.substring(0, kPersPhNo.length - 1);
        jsonData.kidProof = kIdProof.substring(0, kIdProof.length - 1);
        jsonData.kycDoc = kycDoc.substring(0, kycDoc.length - 1);

        //statutory details data
        var statutoryHidIds = $('input[name="bnchstatutoryhiddenId"]').map(function () {
            return this.value;
        }).get();
        var statutoryDetails = $('textarea[name="statutoryDetails"]').map(function () {
            return this.value;
        }).get();
        var registraionNumber = $('input[name="bnchStatutoryegistrationNumber"]').map(function () {
            return this.value;
        }).get();
        var statutoryForInvoice = $('input[name="bnchStatInvoiceDisplayable"]:checkbox').map(function () {
            return this.checked;
        }).get();
        /*var statutorySupportingDoc = $("select[name='bnchStatutorySupportingDoc'] option").map(function () {
        if($(this).val() != ""){
            return $(this).val();
        }
    }).get();*/
        var statutoryValidityFrom = $('input[name="bnchStatutoryvalidFrom"]').map(function () {
            return this.value;
        }).get();
        var statutoryValidityTo = $('input[name="bnchStatutoryvalidTo"]').map(function () {
            return this.value;
        }).get();
        var alertforAction = $('select[name="alertForAction"] option:selected').map(function () {
            return this.value;
        }).get();
        var alertForInformation = $('select[name="alertForInformation"] option:selected').map(function () {
            return this.value;
        }).get();
        var nameAddressOfConsultant = $('textarea[name="nameAddressOfConsultant"]').map(function () {
            return this.value;
        }).get();
        var statutoryAlertRemarks = $('textarea[name="statutoryRemarks"]').map(function () {
            return this.value;
        }).get();
        var statHidIds = "";
        var statDetails = "";
        var statRegNum = "";
        var statForInvoice = "";
        var statRegSupportingDoc = "";
        var statRegValidFrom = "";
        var statRegValidTo = "";
        var statAltForAction = "";
        var statAltForInf = "";
        var statNameAddConsultant = "";
        var statAltRemark = "";
        for (var i = 0; i < statutoryHidIds.length; i++) {
            statHidIds += statutoryHidIds[i] + ",";
        }
        for (var i = 0; i < statutoryDetails.length; i++) {
            if (statutoryDetails[i] != "") {
                statDetails += statutoryDetails[i] + ",";
            } else {
                statDetails += " " + ",";
            }
            if (registraionNumber[i] != "") {
                statRegNum += registraionNumber[i] + ",";
            } else {
                statRegNum += " " + ",";
            }
            if (statutoryForInvoice[i] != "") {
                statForInvoice += statutoryForInvoice[i] + ",";
            } else {
                statForInvoice += " " + ",";
            }
            /*if(statutorySupportingDoc[i]!=""){
    		statRegSupportingDoc+=statutorySupportingDoc[i]+",";
    	}else{
    		statRegSupportingDoc+=" "+",";
    	}*/
            $('select[name="bnchStatutorySupportingDoc"] option').each(function () {
                if (statRegSupportingDoc == "") {
                    statRegSupportingDoc = this.value;
                } else {
                    statRegSupportingDoc += ',' + this.value;
                }
            });
            if (statutoryValidityFrom[i] != "") {
                statRegValidFrom += statutoryValidityFrom[i] + "@";
            } else {
                statRegValidFrom += " " + "@";
            }
            if (statutoryValidityTo[i] != "") {
                statRegValidTo += statutoryValidityTo[i] + "@";
            } else {
                statRegValidTo += " " + "@";
            }
            if (alertforAction[i] != "") {
                statAltForAction += alertforAction[i] + ",";
            } else {
                statAltForAction += " " + ",";
            }
            if (alertForInformation[i] != "") {
                statAltForInf += alertForInformation[i] + ",";
            } else {
                statAltForInf += " " + ",";
            }
            if (nameAddressOfConsultant[i] != "") {
                statNameAddConsultant += nameAddressOfConsultant[i] + "}";
            } else {
                statNameAddConsultant += " " + "}";
            }
            if (statutoryAlertRemarks[i] != "") {
                statAltRemark += statutoryAlertRemarks[i] + "}";
            } else {
                statAltRemark += " " + "}";
            }
        }
        jsonData.stathidIds = statHidIds.substring(0, statHidIds.length - 1);
        jsonData.statdetails = statDetails.substring(0, statDetails.length - 1);
        jsonData.statRegNo = statRegNum.substring(0, statRegNum.length - 1);
        jsonData.statForInv = statForInvoice.substring(0, statForInvoice.length - 1);
        jsonData.statRegDoc = statRegSupportingDoc;
        jsonData.statRegValidFrom = statRegValidFrom.substring(0, statRegValidFrom.length - 1);
        jsonData.statRegValidTo = statRegValidTo.substring(0, statRegValidTo.length - 1);
        jsonData.statAlertForAction = statAltForAction.substring(0, statAltForAction.length - 1);
        jsonData.statAlertForInformation = statAltForInf.substring(0, statAltForInf.length - 1);
        jsonData.statNameAddConsultant = statNameAddConsultant.substring(0, statNameAddConsultant.length - 1);
        jsonData.statAlertRemarks = statAltRemark.substring(0, statAltRemark.length - 1);


        //operational remainder details data
        var operRemHidIds = $('input[name="bnchOperationalRemHidId"]').map(function () {
            return this.value;
        }).get();
        var opeRemRequirements = $('textarea[name="operationalRemRequirements"]').map(function () {
            return this.value;
        }).get();
        var opeRemDueOn = $('input[name="dueOn"]').map(function () {
            return this.value;
        }).get();
        var operRemindersValidToMap = $('input[name="validTo"]').map(function () {
            return this.value;
        }).get();
        var opeRemRecurrences = $('select[name="recurrences"] option:selected').map(function () {
            return this.value;
        }).get();
        var opeRemalertforAction = $('select[name="bnchOperRemalertForAction"] option:selected').map(function () {
            return this.value;
        }).get();
        var opeRemalertforInformation = $('select[name="bnchOperRemalertForInformation"] option:selected').map(function () {
            return this.value;
        }).get();
        var opeRemAlertRemarks = $('textarea[name="operationalRemRemarks"]').map(function () {
            return this.value;
        }).get();
        var operemhidIds = "";
        var operemRequirements = "";
        var operemdueOn = "";
        var operRemindersValidTo = "";
        var operemRecurrences = "";
        var operemaltForAction = "";
        var operemaltForInformation = "";
        var operemaltRemarks = "";
        for (var i = 0; i < operRemHidIds.length; i++) {
            operemhidIds += operRemHidIds[i] + ",";
        }
        for (var i = 0; i < opeRemRequirements.length; i++) {
            if (opeRemRequirements[i] != "") {
                operemRequirements += opeRemRequirements[i] + ",";
            } else {
                operemRequirements += " " + ",";
            }
            if (opeRemDueOn[i] != "") {
                operemdueOn += opeRemDueOn[i] + "@";
            } else {
                operemdueOn += " " + "@";
            }
            if (operRemindersValidTo[i] != "") {
                operRemindersValidTo += operRemindersValidToMap[i] + "@";
            } else {
                operRemindersValidTo += " " + "@";
            }
            if (opeRemRecurrences[i] != "") {
                operemRecurrences += opeRemRecurrences[i] + ",";
            } else {
                operemRecurrences += " " + ",";
            }
            if (opeRemalertforAction[i] != "") {
                operemaltForAction += opeRemalertforAction[i] + ",";
            } else {
                operemaltForAction += " " + ",";
            }
            if (opeRemalertforInformation[i] != "") {
                operemaltForInformation += opeRemalertforInformation[i] + ",";
            } else {
                operemaltForInformation += " " + ",";
            }
            if (opeRemAlertRemarks[i] != "") {
                operemaltRemarks += opeRemAlertRemarks[i] + "}";
            } else {
                operemaltRemarks += " " + "}";
            }
        }
        jsonData.opeRemhidIds = operemhidIds.substring(0, operemhidIds.length - 1);
        jsonData.opeRemRequirements = operemRequirements.substring(0, operemRequirements.length - 1);
        jsonData.opeRemRecurrences = operemRecurrences.substring(0, operemRecurrences.length - 1);
        jsonData.opeRemDueOn = operemdueOn.substring(0, operemdueOn.length - 1);
        jsonData.operRemindersValidTo = operRemindersValidTo.substring(0, operRemindersValidTo.length - 1);
        jsonData.opeRemAltForAction = operemaltForAction.substring(0, operemaltForAction.length - 1);
        jsonData.opeRemAltForInformation = operemaltForInformation.substring(0, operemaltForInformation.length - 1);
        jsonData.opeRemAltRemarks = operemaltRemarks.substring(0, operemaltRemarks.length - 1);

        //start branch cash/safe deposit box details
        var keyDepositHiddenIds = $('input[name="bnchkeyDephiddenId"]').val();
        var keyDepositName = $('input[name="keyDepositName"]').val();
        var keyDepositOpeningBalance = $('input[name="keyDepositOpeningBalance"]').val();
        var keyDepPhCtryCdVal = $('select[name="bnchKeyDepositcountryPhnCode"] option:selected').val();
        var keyDepPhCtryCd = $('select[name="bnchKeyDepositcountryPhnCode"] option:selected').text();
        var keyDepositPhoneNumber1 = $('input[name="keyDepositPhoneNumber1"]').val();
        var keyDepositPhoneNumber2 = $('input[name="keyDepositPhoneNumber2"]').val();
        var keyDepositPhoneNumber3 = $('input[name="keyDepositPhoneNumber3"]').val();
        var keyDepositEmailId = $('input[name="keyDepositEmailId"]').val();
        var cashierName = $('input[name="cashierName"]').val();
        var cashierPhCtryCdVal = $('select[name="cashiercountryPhnCode"] option:selected').val();
        var cashierPhCtryCd = $('select[name="cashiercountryPhnCode"] option:selected').text();
        var cashierPhNo1 = $('input[name="cashierPhoneNumber1"]').val();
        var cashierPhNo2 = $('input[name="cashierPhoneNumber2"]').val();
        var cashierPhNo3 = $('input[name="cashierPhoneNumber3"]').val();
        var cashierEmailId = $('input[name="cashierEmailId"]').val();
        var cashierKL = $('textarea[name="cashierKl"]').val();
        var pettyTxnAppReq = $('select[name="txnApprovalReq"] option:selected').val();
        var cashierNameDepoOpenBal =$('input[name="cashierNameDepoOpenBal"]').val();

        var approvalAmountLimit = "";
        if (pettyTxnAppReq == 1) {
            approvalAmountLimit = $('input[name="approvalAboveLimit"]').val();
        }
        var pettyCashOpeningBalance = $('input[name="pettyCashOpeningBalance"]').val();

        var keyDepPhNo = keyDepPhCtryCdVal + "-" + keyDepositPhoneNumber1 + keyDepositPhoneNumber2 + keyDepositPhoneNumber3;
        var cashierPhNo = cashierPhCtryCdVal + "-" + cashierPhNo1 + cashierPhNo2 + cashierPhNo3;
        //jsonData.keyDepHidIds=keyDepHidIds.substring(0, keyDepHidIds.length-1);
        jsonData.keyDepHidIds = keyDepositHiddenIds;
        jsonData.keyDepName = keyDepositName;
        jsonData.keyDepositOpeningBalance = cashierNameDepoOpenBal;
        jsonData.kPhNoCtryCd = keyDepPhCtryCd;
        jsonData.keyDepPhNo = keyDepPhNo;
        jsonData.keyDepEmail = keyDepositEmailId;
        jsonData.cashName = cashierName;
        jsonData.cashierCtryCode = cashierPhCtryCd;
        jsonData.cashierNo = cashierPhNo;
        jsonData.cashierMail = cashierEmailId;
        jsonData.cashierKL = cashierKL;
        jsonData.cashierPettyTxnApprReqd = pettyTxnAppReq;
        jsonData.cashierPettyTxnApprAmtLimit = approvalAmountLimit;
        jsonData.pettyCashOpeningBalance = pettyCashOpeningBalance;
        // jsonData.pettyCashOpeningBalance = cashierNameDepoOpenBal;

        //branch insurance detail data
        var bnchInsHiddenIds = $('input[name="bnchInshiddenId"]').map(function () {
            return this.value;
        }).get();
        var bnchInsPolicyType = $('input[name="typeOfPolicy"]').map(function () {
            return this.value;
        }).get();
        var bnchInsPolicyNumber = $('input[name="policyNumber"]').map(function () {
            return this.value;
        }).get();
        var bnchInsCompany = $('input[name="insurenceCompany"]').map(function () {
            return this.value;
        }).get();
        var bnchInsPolicyDoc = $("select[name='insurancePolicy'] option").map(function () {
            if ($(this).val() != "") {
                return $(this).val();
            }
        }).get();
        var bnchInsValidityFrom = $('input[name*="insuranceValidityFrom"]').map(function () {
            return this.value;
        }).get();
        var bnchInsValidityTo = $('input[name*="insuranceValidityTo"]').map(function () {
            return this.value;
        }).get();
        var bnchInsAnnualPremium = $('input[name*="premiumAmount"]').map(function () {
            return this.value;
        }).get();
        var insalertforAction = $('select[name="bnchInsalertForAction"] option:selected').map(function () {
            return this.value;
        }).get();
        var insalertForInformation = $('select[name="bnchInsalertForInformation"] option:selected').map(function () {
            return this.value;
        }).get();
        var bnchInsAltRemarks = $('textarea[name="remarks"]').map(function () {
            return this.value;
        }).get();
        var bnchInsHidIs = "";
        var bnchIncPolType = "";
        var bnchInsPolNumber = "";
        var bnchInsurenceCompany = "";
        var bnchInsPolDoc = "";
        var bnchInsAnnualPrem = "";
        var bnchInsurenceValidityFrom = "";
        var bnchInsurenceValidityTo = "";
        var bnchInsAlertRemarks = "";
        var bnchinsalertforAction = "", bnchinsalertforInformation = "";
        for (var i = 0; i < bnchInsHiddenIds.length; i++) {
            bnchInsHidIs += bnchInsHiddenIds[i] + ",";
        }
        for (var i = 0; i < bnchInsPolicyType.length; i++) {
            if (bnchInsPolicyType[i] != "") {
                bnchIncPolType += bnchInsPolicyType[i] + ",";
            } else {
                bnchIncPolType += " " + ",";
            }
            if (bnchInsPolicyNumber[i] != "") {
                bnchInsPolNumber += bnchInsPolicyNumber[i] + ",";
            } else {
                bnchInsPolNumber += " " + ",";
            }
            if (insalertforAction[i] != "") {
                bnchinsalertforAction += insalertforAction[i] + ",";
            } else {
                bnchinsalertforAction += " " + ",";
            }
            if (insalertForInformation[i] != "") {
                bnchinsalertforInformation += insalertForInformation[i] + ",";
            } else {
                bnchinsalertforInformation += " " + ",";
            }
            if (bnchInsCompany[i] != "") {
                bnchInsurenceCompany += bnchInsCompany[i] + ",";
            } else {
                bnchInsurenceCompany += " " + ",";
            }
            /*if(bnchInsPolicyDoc[i]!=""){
			   bnchInsPolDoc+=bnchInsPolicyDoc[i]+",";
		   }else{
			   bnchInsPolDoc+=" "+",";
		   }*/
            $('select[name="insurancePolicy"] option').each(function () {
                if (bnchInsPolDoc == "") {
                    bnchInsPolDoc = this.value;
                } else {
                    bnchInsPolDoc += ',' + this.value;
                }
            });
            if (bnchInsValidityFrom[i] != "") {
                bnchInsurenceValidityFrom += bnchInsValidityFrom[i] + "@";
            } else {
                bnchInsurenceValidityFrom += " " + "@";
            }
            if (bnchInsValidityTo[i] != "") {
                bnchInsurenceValidityTo += bnchInsValidityTo[i] + "@";
            } else {
                bnchInsurenceValidityTo += " " + "@";
            }
            if (bnchInsAnnualPremium[i] != "") {
                bnchInsAnnualPrem += bnchInsAnnualPremium[i] + ",";
            } else {
                bnchInsAnnualPrem += " " + ",";
            }
            if (bnchInsAltRemarks[i] != "") {
                bnchInsAlertRemarks += bnchInsAltRemarks[i] + "}";
            } else {
                bnchInsAlertRemarks += " " + "}";
            }
        }
        jsonData.bnchInsHiddenIds = bnchInsHidIs.substring(0, bnchInsHidIs.length - 1);
        jsonData.bnchInsPolicyType = bnchIncPolType.substring(0, bnchIncPolType.length - 1);
        jsonData.bnchInsPolNum = bnchInsPolNumber.substring(0, bnchIncPolType.length - 1);
        jsonData.bnchInsPolComp = bnchInsurenceCompany.substring(0, bnchInsurenceCompany.length - 1);
        jsonData.bnchInsPolicyDoc = bnchInsPolDoc;
        jsonData.bnchInsPolValidityFm = bnchInsurenceValidityFrom.substring(0, bnchInsurenceValidityFrom.length - 1);
        jsonData.bnchInsPolValidityTo = bnchInsurenceValidityTo.substring(0, bnchInsurenceValidityTo.length - 1);
        jsonData.bnchInsPolYearlyPremium = bnchInsAnnualPrem.substring(0, bnchInsAnnualPrem.length - 1);
        jsonData.bnchInsAltRmrks = bnchInsAlertRemarks.substring(0, bnchInsAlertRemarks.length - 1);
        jsonData.bnchinsalertforActn = bnchinsalertforAction.substring(0, bnchinsalertforAction.length - 1);
        jsonData.bnchinsalertforInfn = bnchinsalertforInformation.substring(0, bnchinsalertforInformation.length - 1);

        //branch bank accounts data start
	     customMethod9(jsonData);
         var branchBankArray = customMethod9();
         jsonData.branchBankArray=branchBankArray;
         var bnchBankActHidId = $('input[name="bnchBnkActhiddenId"]').map(function () {
             return this.value;
         }).get();
         var bnchBankActBankName = $('input[name="bnkActName"]').map(function () {
             return this.value;
         }).get();
 
         var bnchBankActAccountNumber = $('input[name="bnkActNumber"]').map(function () {
             return this.value;
         }).get();
 
         var bnchBankActAuthSignName = $('input[name="authSignName"]').map(function () {
             return this.value;
         }).get();
         var bnchBankActAuthSignEmail = $('input[name="authSignEmail"]').map(function () {
             return this.value;
         }).get();
         var bnchBankActAddress = $('textarea[name="bnkAddress"]').map(function () {
             return this.value;
         }).get();
         var bnchBankActPhnNoCtryCodeVal = $('select[name="bnchbnkactPhnNocountryCode"] option:selected').map(function () {
             return this.value;
         }).get();
         var bnchBankActPhnNoCtryCodeText = $('select[name="bnchbnkactPhnNocountryCode"] option:selected').map(function () {
             return this.text;
         }).get();
         var bnchBankActPhnNo1 = $('input[name="bankPhnNumber1"]').map(function () {
             return this.value;
         }).get();
         var bnchBankActPhnNo2 = $('input[name="bankPhnNumber2"]').map(function () {
             return this.value;
         }).get();
         var bnchBankActPhnNo3 = $('input[name="bankPhnNumber3"]').map(function () {
             return this.value;
         }).get();
         var bnchBankActSwiftCode = $('input[name="bnkSwiftCode"]').map(function () {
             return this.value;
         }).get();
         var bnchBankActRoutingNumber = $('input[name="routingNumber"]').map(function () {
             return this.value;
         }).get();
         var bnchBankActCheckbookCustody = $('input[name="checkBookCustody"]').map(function () {
             return this.value;
         }).get();
         var bnchBankActCheckbookCustodyEmail = $('input[name="checkBookCustodyEmail"]').map(function () {
             return this.value;
         }).get();
         var branchBankActHidId = "";
         var branchBankActBankName = "";
         var branchBankActAccountType = "";
         var branchBankActAccountNumber = "";
         var branchBankActRoutingNumber = "";
         var branchBankActSwiftCode = "";
         var branchBankActAddress = "";
         var branchBankActPhnNoCtryCode = "";
         var branchBankActPhnNo = "";
         var branchBankActCheckbookCustody = "";
         var branchBankActCheckbookCustodyEmail = "";
         var branchBankActAuthSignName = "";
         var branchBankActAuthSignEmail = "";
 
         var branchBankActOpeningBal = "";
 
         for (var i = 0; i < bnchBankActHidId.length; i++) {
             branchBankActHidId += bnchBankActHidId[i] + ",";
         }
         for (var i = 0; i < bnchBankActBankName.length; i++) {
             if (bnchBankActBankName[i] != "") {
                 branchBankActBankName += bnchBankActBankName[i] + ",";
             } else {
                 branchBankActBankName += " " + ",";
             }
 
             if (bnchBankActAccountType[i] != "") {
                 branchBankActAccountType += bnchBankActAccountType[i] + ",";
             } else {
                 branchBankActAccountType += " " + ",";
             }
             if (bnchBankActAccountNumber[i] != "") {
                 branchBankActAccountNumber += bnchBankActAccountNumber[i] + ",";
             } else {
                 branchBankActAccountNumber += " " + ",";
             }
 
             if (bnchBankActOpeningBalance[i] != "") {
                 branchBankActOpeningBal += bnchBankActOpeningBalance[i] + ",";
             } else {
                 branchBankActOpeningBal += " " + ",";
             }
 
             if (bnchBankActAuthSignName[i] != "") {
                 branchBankActAuthSignName += bnchBankActAuthSignName[i] + ",";
             } else {
                 branchBankActAuthSignName += " " + ",";
             }
             if (bnchBankActAuthSignEmail[i] != "") {
                 branchBankActAuthSignEmail += bnchBankActAuthSignEmail[i] + ",";
             } else {
                 branchBankActAuthSignEmail += " " + ",";
             }
             if (bnchBankActAddress[i] != "") {
                 branchBankActAddress += bnchBankActAddress[i] + "}";
             } else {
                 branchBankActAddress += " " + "}";
             }
             branchBankActPhnNoCtryCode += bnchBankActPhnNoCtryCodeText[i] + ",";
             branchBankActPhnNo += bnchBankActPhnNoCtryCodeVal[i] + "-" + bnchBankActPhnNo1[i] + bnchBankActPhnNo2[i] + bnchBankActPhnNo3[i] + ",";
             if (bnchBankActSwiftCode[i] != "") {
                 branchBankActSwiftCode += bnchBankActSwiftCode[i] + ",";
             } else {
                 branchBankActSwiftCode += " " + ",";
             }
             if (bnchBankActRoutingNumber[i] != "") {
                 branchBankActRoutingNumber += bnchBankActRoutingNumber[i] + ",";
             } else {
                 branchBankActRoutingNumber += " " + ",";
             }
             if (bnchBankActCheckbookCustody[i] != "") {
                 branchBankActCheckbookCustody += bnchBankActCheckbookCustody[i] + ",";
             } else {
                 branchBankActCheckbookCustody += " " + ",";
             }
             if (bnchBankActCheckbookCustodyEmail[i] != "") {
                 branchBankActCheckbookCustodyEmail += bnchBankActCheckbookCustodyEmail[i] + ",";
             } else {
                 branchBankActCheckbookCustodyEmail += " " + ",";
             }
         }
         jsonData.branchBankAccountHidId = branchBankActHidId.substring(0, branchBankActHidId.length - 1);
         jsonData.branchBankAccountBankName = branchBankActBankName.substring(0, branchBankActBankName.length - 1);
         jsonData.branchBankAccountType = branchBankActAccountType.substring(0, branchBankActAccountType.length - 1);
         jsonData.branchBankAccountNumber = branchBankActAccountNumber.substring(0, branchBankActAccountNumber.length - 1);
         jsonData.branchBankAccountOpeningBalance = branchBankActOpeningBal.substring(0, branchBankActOpeningBal.length - 1);
         jsonData.branchBankAccounttAuthSignName = branchBankActAuthSignName.substring(0, branchBankActAuthSignName.length - 1);
         jsonData.branchBankAccounttAuthSignEmail = branchBankActAuthSignEmail.substring(0, branchBankActAuthSignEmail.length - 1);
         jsonData.branchBankAccounttAddress = branchBankActAddress.substring(0, branchBankActAddress.length - 1);
         jsonData.branchBankAccounttPhnNoCtryCode = branchBankActPhnNoCtryCode.substring(0, branchBankActPhnNoCtryCode.length - 1);
         jsonData.branchBankAccountPhnNo = branchBankActPhnNo.substring(0, branchBankActPhnNo.length - 1);
         jsonData.branchBankAccountSwiftCode = branchBankActSwiftCode.substring(0, branchBankActSwiftCode.length - 1);
         jsonData.branchBankAccountRoutingNumber = branchBankActRoutingNumber.substring(0, branchBankActRoutingNumber.length - 1);
         jsonData.branchBankAccountCheckbookCustody = branchBankActCheckbookCustody.substring(0, branchBankActCheckbookCustody.length - 1);
         jsonData.branchBankAccountCheckbookCustodyEmail = branchBankActCheckbookCustodyEmail.substring(0, branchBankActCheckbookCustodyEmail.length - 1); 
        
        //start with tax details data
        var bnchTaxHidId = $('input[name="banchTaxHidId"]').map(function () {
            return this.value;
        }).get();
        var bnchTaxName = $('input[name="taxName"]').map(function () {
            return this.value;
        }).get();
        var bnchTaxRate = $('input[name="taxRate"]').map(function () {
            return this.value;
        }).get();
        var salesTaxOpeningBalance = $('input[name="salesTaxOpeningBalance"]').map(function () {
            return this.value;
        }).get();
        var bnchTaxHiddenIds = "";
        var bnchTaxNames = "";
        var bnchTaxRates = "";
        var salesTaxOpeningBalances = "";
        for (var i = 0; i < bnchTaxHidId.length; i++) {
            bnchTaxHiddenIds += bnchTaxHidId[i] + ",";
        }
        for (var i = 0; i < bnchTaxName.length; i++) {
            if (bnchTaxName[i] != "") {
                bnchTaxNames += bnchTaxName[i] + ",";
            } else {
                bnchTaxNames += " " + ",";
            }
            if (bnchTaxRate[i] != "") {
                bnchTaxRates += bnchTaxRate[i] + ",";
            } else {
                bnchTaxRates += " " + ",";
            }
            if (salesTaxOpeningBalance[i] != "") {
                salesTaxOpeningBalances += salesTaxOpeningBalance[i] + ",";
            } else {
                salesTaxOpeningBalances += " " + ",";
            }
        }

        var bnchInputTaxHidId = $('input[name="banchInputTaxHidId"]').map(function () {
            return this.value;
        }).get();
        var bnchInputTaxName = $('input[name="inputTaxName"]').map(function () {
            return this.value;
        }).get();
        var bnchInputTaxRate = $('input[name="inputTaxRate"]').map(function () {
            return this.value;
        }).get();
        var buyingTaxOpeningBalance = $('input[name="buyingTaxOpeningBalance"]').map(function () {
            return this.value;
        }).get();
        var bnchInputTaxHiddenIds = "";
        var bnchInputTaxNames = "";
        var bnchInputTaxRates = "";
        var buyingTaxOpeningBalances = "";

        var digitalSignDocumentsList = $('#digiSignDocSelection option:selected').map(function () {
            if (this.value != "multiselect-all") {
                return this.value;
            }
        }).get();
        var dsPersonName = $("#dsPersonName").val();
        var dsPersonDesignation = $("#dsPersonDesignation").val();
        var dsPersonPhoneNo = $("#dsPersonPhoneNo").val();
        var dsPersonEmailId = $("#dsPersonEmailId").val();
        var dsRefNo = $("#dsRefNo").val();
        var dsKYC = $("#dsKYC").val();
        var dsValidityFrom = $("#dsValidityFrom").val();
        var dsValidityTo = $("#dsValidityTo").val();
        jsonData.dsPersonName = dsPersonName;
        jsonData.dsPersonDesignation = dsPersonDesignation;
        jsonData.dsPersonPhoneNo = dsPersonPhoneNo;
        jsonData.dsPersonEmailId = dsPersonEmailId;
        jsonData.dsRefNo = dsRefNo;
        jsonData.dsKYC = dsKYC;
        jsonData.digitalSignDocumentsList = digitalSignDocumentsList.toString();
        jsonData.dsValidityFrom = dsValidityFrom;
        jsonData.dsValidityTo = dsValidityTo;

        for (var i = 0; i < bnchInputTaxHidId.length; i++) {
            bnchInputTaxHiddenIds += bnchInputTaxHidId[i] + ",";
        }
        for (var i = 0; i < bnchInputTaxName.length; i++) {
            if (bnchInputTaxName[i] != "") {
                bnchInputTaxNames += bnchInputTaxName[i] + ",";
            } else {
                bnchInputTaxNames += " " + ",";
            }
            if (bnchInputTaxRate[i] != "") {
                bnchInputTaxRates += bnchInputTaxRate[i] + ",";
            } else {
                bnchInputTaxRates += " " + ",";
            }
            if (buyingTaxOpeningBalance[i] != "") {
                buyingTaxOpeningBalances += buyingTaxOpeningBalance[i] + ",";
            } else {
                buyingTaxOpeningBalances += " " + ",";
            }
        }
        var branchAdminForSingleUser = $("#assignBranchAdmin option:eq(1)").val();
        jsonData.bnchTaxesHidIds = bnchTaxHiddenIds.substring(0, bnchTaxHiddenIds.length - 1);
        jsonData.bnchTaxesNames = bnchTaxNames.substring(0, bnchTaxNames.length - 1);
        jsonData.bnchTaxesRates = bnchTaxRates.substring(0, bnchTaxRates.length - 1);
        jsonData.salesTaxOpeningBalances = salesTaxOpeningBalances.substring(0, salesTaxOpeningBalances.length - 1);

        jsonData.bnchInputTaxesHidIds = bnchInputTaxHiddenIds.substring(0, bnchInputTaxHiddenIds.length - 1);
        jsonData.bnchInputTaxesNames = bnchInputTaxNames.substring(0, bnchInputTaxNames.length - 1);
        jsonData.bnchInputTaxesRates = bnchInputTaxRates.substring(0, bnchInputTaxRates.length - 1);
        jsonData.buyingTaxOpeningBalances = buyingTaxOpeningBalances.substring(0, buyingTaxOpeningBalances.length - 1);
        if (branchAdminForSingleUser != undefined && branchAdminForSingleUser != null && branchAdminForSingleUser != "")
            jsonData.branchAdminForSingleUser = branchAdminForSingleUser;
        else
            jsonData.branchAdminForSingleUser = "";
        jsonData.branchAdmin = $('#assignBranchAdmin').val();
		$.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
        var url = "/config/addBranch";
        $.ajax({
            url: url,
            data: JSON.stringify(jsonData),
            type: "text",
            headers: {
                "X-AUTH-TOKEN": window.authToken
            },
            method: "POST",
            contentType: 'application/json',
            success: function (data) {
                if (!isEmpty(data.type) && data.type == "chat"){
                    chat.displayMessage(data);
                }else if (!isEmpty(data.type) && data.type == "onlineUsers"){
                    chatAvailableSuccess(data);
                }else{
                    var branchId=data.id;
                    var branchName=data.branchName;
                    var countryName="";
                    if(data.country!=""){
                        countryName=$("#branchCountry").find('option[value='+data.country+']').text();
                    }
                    $('#branchSetup input[type="text"]').val('');
                    $('#branchSetup input[type="textarea"]').val('');
                    $("#branchTable tr[name='branchEntity"+data.id+"']").remove();
                    $(".multiBranch option[value="+data.id+"]").remove();
                    $('#itemBranch2BtnList li').find('input[value='+data.id+']').parent().remove();
                    $("#usersTable tr td:contains("+data.oldName+")").each(function(){
                        $(this).html(data.branchName);
                    });
                    $(".docuploadrulecustomdropdownBranchList li[id='docuploadrulecustomdropdownBranchlist'] input[name='customdoccheckBranch'][value='"+branchId+"']").parent().remove();
                    $('#itemBranch2BtnList').append('<li class="itemBranch2Class"><input class="itemBranch2Class-cb" style="margin:auto 10px;" type="checkbox" value="'+branchId+'"><span class="itemBranch2-name" title="'+branchName+'">'+branchName+'</span><input style="width:30px;" onkeypress="return isNumber(event)" type="text" id="itemBranch2Class-cb'+branchId+'" class="itemBranch2-input percent"></li>');
                    $(".multiBranch").append('<option value="'+branchId+'">' +branchName+ '</option>');
                    $("#projectPositionBranch").append('<option value="'+branchId+'">' +branchName+ '</option>');
                    $(".multiBranch").multiselect('rebuild');
                    $(".docuploadrulecustomdropdownBranchList").append('<li id="docuploadrulecustomdropdownBranchlist">&nbsp;&nbsp;<input type="checkbox" name="customdoccheckBranch" id="customdoccheckBranch" value="'+branchId+'" onclick="customdoccheckUncheck(this);"/>&nbsp;&nbsp;<input type="text" class="input-small" name="monetoryLimit" id="monetoryLimit'+branchId+'" value="0.0" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="customdoctoggleCheck(this)">&nbsp;&nbsp;'+branchName+'</li>');
                    $("#branchTable").prepend('<tr name="branchEntity'+data.id+'"><td>'+data.branchName+'</td><td>'+data.branchgstin+'</td><td>'+countryName+'</td><td>'+data.location+'</td><td>'+data.phoneNumber+'</td><td><button class="btn btn-submit" onClick="showBranchEntityDetails(this)" id="show-entity-details'+data.id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td><td><button class="btn btn-submit"  onClick="deactivateBranchEntityDetails(this)" id="deactivate-entity-details'+data.id+'"><i class="fa fa-trash-o fa-lg pr-5"></i>'+data.actionText+'</button></td></tr>');
                    var existingBranchInTaxUl=$("#mainBranchIncomeChartOfAccount b[id='branchEntity"+branchId+"']").attr('id');
                    if(typeof existingBranchInTaxUl!='undefined'){
                        $("#mainBranchIncomeChartOfAccount b[id='branchEntity"+branchId+"']").text("");
                        $("#mainBranchIncomeChartOfAccount b[id='branchEntity"+branchId+"']").text(branchName);
                    }else if(typeof existingBranchInTaxUl=='undefined'){
                        $("#mainBranchIncomeChartOfAccount").append('<li class="branchEntity'+branchId+'"><div class="chartOfAccountContainer"><img id="branchEntity'+branchId+'" src="/assets/images/minus.png" class="coaPlusMinusImage" id="getNodeChild"></img><font class="color-grey"><b id="branchEntity'+branchId+'" style="margin-left:2px;">'+branchName+'</b></font></div>'+
                        '<ul id="mainBranchIncomeChartOfAccount" class="treeview-black mainBranchIncomeChartOfAccount"><li id="branchEntity'+branchId+'"><div class="chartOfAccountContainer"><img id="branchEntity'+branchId+'" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildIncomesChartOfAccount(this);"></img><font class="color-grey"><b style="margin-left:2px;">Incomes</b></font></div></li></ul></li>');
                    }
                    refreshBranchOnSaveOrUpdate(branchId, branchName, data.branchgstin);
    
                    $("#notificationMessage").html("Branch has been added/Updated successfully.");
                    $("#newbranchform-container-close").trigger('click');
                    
                    $('.addBranch').html("Save Branch Details");
                    $(".btn-div-top").hide();
                    $('.notify-success').show();
                    $.unblockUI();
                    alwaysScrollTop();
                }
				formCancel();
            },
            error: function (xhr, status, error) {
                if (xhr.status == 401) {
                    doLogout();
                } else if (xhr.status == 500) {
                    swal("Error on Save/Update Branch!", "Please retry, if problem persists contact support team", "error");
                }
            },
            complete: function (data) {
                $.unblockUI();
				$('#addBranch').removeAttr("disabled");
            }
        }).done($('.btn-div-top').fadeOut());
    }
}

$(document).ready(function() {
    // Assuming "addBranch" is a button, adjust the selector accordingly
    $(".addBranch").click(function() {
        // Call the addBranch function here
        addBranch();
    });
});

function populateDigiSignDocList() {
    var documentsListPopulate = "";
    documentsListPopulate += '<option value="1">Invoice</option>';
    documentsListPopulate += '<option value="2">Quotation</option>';
    documentsListPopulate += '<option value="3">Advance Receipt</option>';
    documentsListPopulate += '<option value="4">Purchase Order</option>';
    documentsListPopulate += '<option value="5">Refund Advance Receipt</option>';
    documentsListPopulate += '<option value="6">Delivery Challan</option>';
    documentsListPopulate += '<option value="7">Receipt Starting Number</option>';
    documentsListPopulate += '<option value="8">Debit Note</option>';
    documentsListPopulate += '<option value="9">Credit Note</option>';
    documentsListPopulate += '<option value="10">Refund Amount received against Invoice</option>';
    $('#digiSignDocSelection').append(documentsListPopulate);
    $('#digiSignDocSelection').multiselect('rebuild');
}

var checkDuplicateTaxName = function (elem) {
    var tableName = $(elem).closest('table').attr('id');
    var trid = $(elem).closest('tr').attr('id');
    var fieldName = 'taxName';
    if (tableName === 'branchInputTaxTable') {
        fieldName = 'inputTaxName';
    }
    var taxName = $(elem).val();
    $("#" + tableName + " > tbody > tr").each(function () {
        if (trid !== this.id) {
            var otherTaxNane = $(this).find("td #" + fieldName).val();
            if (otherTaxNane === taxName) {
                $(elem).val('');
                swal("Error duplicate tax name!", "Duplicate tax name is not allowed.", "error");
            }
        }
    });
}

function showBranchEntityDetails(elem) {
    $("#branchPremiseDiv").hide();
	customMethod2();
    $('#isnewbranchHidden').val('');
    var entityId = $(elem).attr('id');
    var origEntityId = entityId.substring(19, entityId.length);
    listAlertUser(origEntityId);
    enteredUserEmail = "";
    onFocusClickEmail = "";
    var detailForm = "newbranchform-container";
    $('.' + detailForm + ' input[type="hidden"]').val("");
    $('.' + detailForm + ' input[type="text"]').val("");
    $('.' + detailForm + ' textarea').val("");
    $("input[name='taxName']").val("");
    $("input[name='taxRate']").val("");
    $("input[name='salesTaxOpeningBalance']").val("");
    $("input[name='inputTaxName']").val("");
    $("input[name='inputTaxRate']").val("");
    $("input[name='buyingTaxOpeningBalance']").val("");
    $('.' + detailForm + ' input[type="password"]').val("");
    $("." + detailForm + " select[name='bnchStatutorySupportingDoc']").children().remove();
    $("." + detailForm + " select[name='bnchStatutorySupportingDoc']").append("<option value=''>Select a File</option>");
    $("." + detailForm + " select[name='insurancePolicy']").children().remove();
    $("." + detailForm + " select[name='insurancePolicy']").append("<option value=''>Select a File</option>");
    $('.' + detailForm + ' select option:first').prop("selected", "selected");
    $('.' + detailForm + ' select[class="countryPhnCode"]').each(function () {
        $(this).find('option:first').prop("selected", "selected");
    });
    $('.' + detailForm + ' select[class="countryDropDown"]').each(function () {
        $(this).find('option:first').prop("selected", "selected");
    });
    $("#digiSignDocSelection").children().remove();
    $("#digiSignDocSelection").multiselect('rebuild');
    $("a[id*='form-container-close']").attr("href", location.hash);
    var jsonData = {};
    jsonData.entityPrimaryId = origEntityId;
    var url = "/config/branchDetails";
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        method: "POST",
        contentType: 'application/json',
        success: function (data) {
			customMethod3();
            $("#branchOfficersTable tr[id*=dynBranchOfficer]").remove();
            var bocontent = $("#branchOfficersTable tbody tr[id='copyContentBranchOfficer']").html();
            $("#branchStatutoryTable tr[id*=dynBranchStatutory]").remove("");
            var bstat = $("#branchStatutoryTable tbody tr[id='copyContentBranchStatutory']").html();
            $("#branchOperationalRemainderTable tr[id*=dynBranchOperRem]").remove();
            var opeRem = $("#branchOperationalRemainderTable tbody tr[id='copyContentBranchOpRem']").html();
            $("#branchSafeDepositBoxTable tr[id*=dynBranchKeyDep]").remove();
            var bdepbox = $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep']").html();
            $("#branchInsurenceTable tr[id*=dynBranchInsurence]").remove();
            var bins = $("#branchInsurenceTable tbody tr[id='copyBranchInsurence']").html();
            $("#branchBankAccountTable tr[id*=dynBranchBnkAct]").remove();
            var bbnkact = $("#branchBankAccountTable tbody tr[id='copyBranchBankAccount']").html();

            $("#branchTaxTable tr[id*=dynBranchTax]").remove();
            var branchTaxTr = $("#branchTaxTable tbody tr[id='copyBranchTax']").html();
            $("#branchInputTaxTable tr[id*=dynBranchInputTax]").remove();
            var branchInputTaxTr = $("#branchInputTaxTable tbody tr[id='copyBranchInputTax']").html();

            //branch basic edit details
            for (var i = 0; i < data.branchdetailsData.length; i++) {
                $('#branch-form-container input[id="branchEntityHiddenId"]').val(data.branchdetailsData[i].id);
                $('#branch-form-container input[id="bnchName"]').val(data.branchdetailsData[i].branchName);
                $('#branch-form-container input[id="bnchOpenDate"]').val(data.branchdetailsData[i].branchOpenDate);
                $('#branch-form-container input[id="bnchOpenDate"]').val(data.branchdetailsData[i].branchOpenDate);
                $("#branch-form-container select[id='branchCountry'] option").filter(function () {
                    return $(this).val() == data.branchdetailsData[i].country;
                }).prop("selected", "selected");
                $('#branch-form-container select[id="bnchcurrency"]').find("option[value='" + data.branchdetailsData[i].branchCurrency + "']").prop("selected", "selected");
                $('#branch-form-container input[id="bnchLocation"]').val(data.branchdetailsData[i].branchLocation);
                $('#branch-form-container select[id="bnchregPhnNocountryCode"] option').filter(function () {
                    return $(this).html() == data.branchdetailsData[i].branchPhoneNumberCtryCd;
                }).prop("selected", "selected");
                $('#branch-form-container input[id="bnchPhoneNumber1"]').val(data.branchdetailsData[i].branchPhoneNumber.substring(0, 3));
                $('#branch-form-container input[id="bnchPhoneNumber2"]').val(data.branchdetailsData[i].branchPhoneNumber.substring(3, 6));
                $('#branch-form-container input[id="bnchPhoneNumber3"]').val(data.branchdetailsData[i].branchPhoneNumber.substring(6, 10));

                //console.log("==" + data.branchdetailsData[i].gstCountryCode);
                //console.log("=" + data.branchdetailsData[i].branchGstinPart1);

                if ((data.branchdetailsData[i].gstCountryCode !== null && data.branchdetailsData[i].gstCountryCode !== "")
                    && (data.branchdetailsData[i].branchGstinPart1 === "" || data.branchdetailsData[i].branchGstinPart1 === null)) {
                    $('#confirmgstin').show();
                    $(".gstinInputCls").removeAttr("disabled");
                    $("#branchstate").removeAttr("disabled");
                    //$("#branchstate option").filter(function () {return $(this).val()=='';}).prop("selected", "selected");
                } else {
                    $('#confirmgstin').hide();
                    $('#branch-form-container input[id="gstinput"]').attr("disabled", "disabled");
                    $('#branch-form-container input[id="gstinput2"]').attr("disabled", "disabled");
                    $('#branch-form-container input[id="gstinputconfirm"]').attr("disabled", "disabled");
                    $('#branch-form-container input[id="gstinput2Confirm"]').attr("disabled", "disabled");
                    $("#branchstate").attr("disabled", "disabled");
                }

                if (data.branchdetailsData[i].branchGstinPart2 === "" || data.branchdetailsData[i].branchGstinPart2 === null) {
                    $('#confirmgstin').show();
                    $('#branch-form-container input[id="gstinputconfirm"]').val(data.branchdetailsData[i].branchGstinPart1);
                    $('#branch-form-container input[id="gstinput2"]').removeAttr("disabled");
                    $('#branch-form-container input[id="gstinput2Confirm"]').removeAttr("disabled");
                    $("#branchstate").removeAttr("disabled");
                    //$("#branchstate option").filter(function () {return $(this).val()=='';}).prop("selected", "selected");
                }

                $('#branch-form-container input[id="gstinput"]').val(data.branchdetailsData[i].branchGstinPart1);
                $('#branch-form-container input[id="gstinput2"]').val(data.branchdetailsData[i].branchGstinPart2);
                $('#branch-form-container input[id="gstinputconfirm"]').val(data.branchdetailsData[i].branchGstinPart1);
                $('#branch-form-container input[id="gstinput2Confirm"]').val(data.branchdetailsData[i].branchGstinPart2);
                $('#branch-form-container textarea[id="branchAddress"]').val(data.branchdetailsData[i].branchAddress);
                $('#branch-form-container select[id="branchstate"] option').filter(function () {
                    return $(this).val() == data.branchdetailsData[i].branchStateCode;
                }).prop("selected", "selected");

                $('#branch-form-container select[id="bnchFacility"]').find("option[value='" + data.branchdetailsData[i].branchFacility + "']").prop("selected", "selected");
                if (data.branchdetailsData[i].branchFacility == 3 || data.branchdetailsData[i].branchFacility == "") {
                    $("#branchPremiseDiv").hide();
                }

                if (data.branchdetailsData[i].branchFacility == 1 || data.branchdetailsData[i].branchFacility == 2) {
                    var altForActions = "";
                    var altForInformations = "";
                    $('#branch-form-container input[name="bnchAggreement"]').val(data.branchdetailsData[i].branchAggreement);
                    $('#branch-form-container input[id="bnchPremiseValidityFrom"]').val(data.branchdetailsData[i].aggreementValididtyFrom);
                    $('#branch-form-container input[id="bnchPremiseValidityTo"]').val(data.branchdetailsData[i].aggreementValididtyTo);
                    $("#branch-form-container select[id='periodicityOfPayment'] option").filter(function () {
                        return $(this).val() == data.branchdetailsData[i].periodicityOfPayment;
                    }).prop("selected", "selected");
                    $('#branch-form-container input[id="rentPayable"]').val(data.branchdetailsData[i].rentPayable);
                    $('#branch-form-container input[id="landlordName"]').val(data.branchdetailsData[i].landlordName);
                    $('#branch-form-container textarea[id="landlordAddress"]').val(data.branchdetailsData[i].landlordAddress);
                    $('#branch-form-container input[id="bankAccountName"]').val(data.branchdetailsData[i].bankAccountName);
                    $('#branch-form-container input[id="bankAccountNumber"]').val(data.branchdetailsData[i].bankAccountNumber);
                    $('#branch-form-container input[id="bankAccountBranch"]').val(data.branchdetailsData[i].bankAccountBranch);
                    $('#branch-form-container input[id="rentRevisedDueOn"]').val(data.branchdetailsData[i].rentRevisionDueOn);
                    if (data.branchdetailsData[i].alertForActions != null && data.branchdetailsData[i].alertForActions != "") {
                        altForActions = data.branchdetailsData[i].alertForActions;
                    }
                    if (data.branchdetailsData[i].alertForInformations != null && data.branchdetailsData[i].alertForInformations != "") {
                        altForInformations = data.branchdetailsData[i].alertForInformations;
                    }
                    $("#branchPremiseTable tbody tr[id='branchPremiseType'] select[name='bnchPremisealertForAction'] option").filter(function () {
                        return $(this).val() == altForActions;
                    }).prop("selected", "selected");
                    $("#branchPremiseTable tbody tr[id='branchPremiseType'] select[name='bnchPremisealertForInformation'] option").filter(function () {
                        return $(this).val() == altForInformations;
                    }).prop("selected", "selected");
                    if (altForActions.trim() != "") {
                        var adminAlertActionInCaseOfDeactivateBranchOfficerUser = $("#branchPremiseTable tbody tr[id='branchPremiseType'] select[name='bnchPremisealertForAction'] option").filter(function () {
                            return $(this).val() == altForActions;
                        }).attr('value');
                        if (typeof adminAlertActionInCaseOfDeactivateBranchOfficerUser == "undefined") {
                            $("#branchPremiseTable tbody tr[id='branchPremiseType'] select[name='bnchPremisealertForAction']").append('<option value="' + altForActions + '" selected="selected">' + altForActions + '</option>');
                        }
                    }
                    if (altForInformations.trim() != "") {
                        var adminAlertInformationInCaseOfDeactivateBranchOfficerUser = $("#branchPremiseTable tbody tr[id='branchPremiseType'] select[name='bnchPremisealertForInformation'] option").filter(function () {
                            return $(this).val() == altForInformations;
                        }).attr('value');
                        if (typeof adminAlertInformationInCaseOfDeactivateBranchOfficerUser == "undefined") {
                            $("#branchPremiseTable tbody tr[id='branchPremiseType'] select[name='bnchPremisealertForInformation']").append('<option value="' + altForInformations + '" selected="selected">' + altForInformations + '</option>');
                        }
                    }
                    $('#branch-form-container textarea[id="rentDueOnRemarks"]').val(data.branchdetailsData[i].rentRevisionDueRemarks);
                    $("#branchPremiseDiv").show();
                }
                $('#branch-form-container select[id="digiSignDocSelection"]').children().remove();
                populateDigiSignDocList();
                $('#branch-form-container input[id="dsPersonName"]').val(data.branchdetailsData[i].dsPersonName);
                $('#branch-form-container input[id="dsPersonDesignation"]').val(data.branchdetailsData[i].dsDesignation);
                $('#branch-form-container input[id="dsPersonPhoneNo"]').val(data.branchdetailsData[i].dsPhoneNo);
                $('#branch-form-container input[id="dsPersonEmailId"]').val(data.branchdetailsData[i].dsEmailId);
                $('#branch-form-container input[id="dsRefNo"]').val(data.branchdetailsData[i].dsRefNo);
                $('#branch-form-container input[id="dsKYC"]').val(data.branchdetailsData[i].dsKycDetails);
                $('#branch-form-container input[id="dsValidityFrom"]').val(data.branchdetailsData[i].dsValidityFrom);
                $('#branch-form-container input[id="dsValidityTo"]').val(data.branchdetailsData[i].dsValidityTo);
                if (data.branchdetailsData[i].digitalSignDocuments != null && data.branchdetailsData[i].digitalSignDocuments != "") {
                    var digitalSignDocsArr = data.branchdetailsData[i].digitalSignDocuments.toString().split(",");
                    for (var i = 0; i < digitalSignDocsArr.length; i++) {
                        $('#branch-form-container select[id="digiSignDocSelection"]').find("option[value='" + digitalSignDocsArr[i] + "']").prop("selected", "selected");
                    }
                    $('#branch-form-container select[id="digiSignDocSelection"]').multiselect('rebuild');
                }
                //branch officer edit details
                for (var j = 0; j < data.branchKeyOffData.length; j++) {
                    var keyOffId = data.branchKeyOffData[j].keyoffId;
                    var keyOffName = "";
                    var keyOffDesignation = "";
                    var keyOffCountry = "";
                    var keyOffCity = "";
                    var keyOffPhnNo1 = "";
                    var keyOffPhnNo2 = "";
                    var keyOffPhnNo3 = "";
                    var keyOffEmail = "";
                    var keyOffphnNumberCtryCode = "";
                    var keyOffPersPhnNoCtryCode = "", keyOffPersPhNo1 = "", keyOffPersPhNo2 = "", keyOffPersPhNo3 = "",
                        keyOffIdProof = "";
                    if (data.branchKeyOffData[j].keyoffname != null && data.branchKeyOffData[j].keyoffname != "") {
                        keyOffName = data.branchKeyOffData[j].keyoffname;
                    }
                    if (data.branchKeyOffData[j].keyoffdesignation != null && data.branchKeyOffData[j].keyoffdesignation != "") {
                        keyOffDesignation = data.branchKeyOffData[j].keyoffdesignation;
                    }
                    if (data.branchKeyOffData[j].keyoffcountry != null && data.branchKeyOffData[j].keyoffcountry != "") {
                        keyOffCountry = data.branchKeyOffData[j].keyoffcountry;
                    }
                    if (data.branchKeyOffData[j].keyoffcity != null && data.branchKeyOffData[j].keyoffcity != "") {
                        keyOffCity = data.branchKeyOffData[j].keyoffcity;
                    }
                    if (data.branchKeyOffData[j].keyoffEmail != null && data.branchKeyOffData[j].keyoffEmail != "") {
                        keyOffEmail = data.branchKeyOffData[j].keyoffEmail;
                    }
                    if (data.branchKeyOffData[j].keyoffphnNumberCtryCode != null && data.branchKeyOffData[j].keyoffphnNumberCtryCode != "") {
                        keyOffphnNumberCtryCode = data.branchKeyOffData[j].keyoffphnNumberCtryCode;
                    }
                    if (data.branchKeyOffData[j].keyoffphnNumber != null && data.branchKeyOffData[j].keyoffphnNumber != "") {
                        keyOffPhnNo1 = data.branchKeyOffData[j].keyoffphnNumber.substring(0, 3);
                        keyOffPhnNo2 = data.branchKeyOffData[j].keyoffphnNumber.substring(3, 6);
                        keyOffPhnNo3 = data.branchKeyOffData[j].keyoffphnNumber.substring(6, 10);
                    }
                    if (data.branchKeyOffData[j].keyoffpersphnNumberCtryCode != null && data.branchKeyOffData[j].keyoffpersphnNumberCtryCode != "") {
                        keyOffPersPhnNoCtryCode = data.branchKeyOffData[j].keyoffpersphnNumberCtryCode;
                    }
                    if (data.branchKeyOffData[j].keyoffpersphnNumber != null && data.branchKeyOffData[j].keyoffpersphnNumber != "") {
                        keyOffPersPhNo1 = data.branchKeyOffData[j].keyoffpersphnNumber.substring(0, 3);
                        keyOffPersPhNo2 = data.branchKeyOffData[j].keyoffpersphnNumber.substring(3, 6);
                        keyOffPersPhNo3 = data.branchKeyOffData[j].keyoffpersphnNumber.substring(6, 10);
                    }
                    if (data.branchKeyOffData[j].keyoffidproof != null && data.branchKeyOffData[j].keyoffidproof != "") {
                        keyOffIdProof = data.branchKeyOffData[j].keyoffidproof;
                    }
                    if (j == 0) {
                        $("#branchOfficersTable tbody tr[id='copyContentBranchOfficer'] input[name='bnchkeyOffhiddenId']").val(keyOffId);
                        $("#branchOfficersTable tbody tr[id='copyContentBranchOfficer'] input[name='keyoffName']").val(keyOffName);
                        $("#branchOfficersTable tbody tr[id='copyContentBranchOfficer'] input[name='keyoffDesignation']").val(keyOffDesignation);
                        $("#branchOfficersTable tbody tr[id='copyContentBranchOfficer'] select[name='keyOffCountry'] option").filter(function () {
                            return $(this).val() == keyOffCountry;
                        }).prop("selected", "selected");
                        $("#branchOfficersTable tbody tr[id='copyContentBranchOfficer'] input[name='keyOffCity']").val(keyOffCity);
                        $("#branchOfficersTable tbody tr[id='copyContentBranchOfficer'] input[name='keyoffEmailId']").val(keyOffEmail);
                        // $("#branchOfficersTable tbody tr[id='copyContentBranchOfficer'] input[name='keyoffEmailId']").attr("disabled", "disabled");
                        $("#branchOfficersTable tbody tr[id='copyContentBranchOfficer'] select[name='bnchKeyOffcountryPhnCode'] option").filter(function () {
                            return $(this).html() == keyOffphnNumberCtryCode;
                        }).prop("selected", "selected");
                        $("#branchOfficersTable tbody tr[id='copyContentBranchOfficer'] input[name='keyoffPhoneNumber1']").val(keyOffPhnNo1);
                        $("#branchOfficersTable tbody tr[id='copyContentBranchOfficer'] input[name='keyoffPhoneNumber2']").val(keyOffPhnNo2);
                        $("#branchOfficersTable tbody tr[id='copyContentBranchOfficer'] input[name='keyoffPhoneNumber3']").val(keyOffPhnNo3);
                        $("#branchOfficersTable tbody tr[id='copyContentBranchOfficer'] select[name='bnchKeyOffperscountryPhnCode'] option").filter(function () {
                            return $(this).html() == keyOffPersPhnNoCtryCode;
                        }).prop("selected", "selected");
                        $("#branchOfficersTable tbody tr[id='copyContentBranchOfficer'] input[name='keyoffpersPhoneNumber1']").val(keyOffPersPhNo1);
                        $("#branchOfficersTable tbody tr[id='copyContentBranchOfficer'] input[name='keyoffpersPhoneNumber2']").val(keyOffPersPhNo2);
                        $("#branchOfficersTable tbody tr[id='copyContentBranchOfficer'] input[name='keyoffpersPhoneNumber3']").val(keyOffPersPhNo3);
                        $("#branchOfficersTable tbody tr[id='copyContentBranchOfficer'] input[name='keyOffIdProof']").val(keyOffIdProof);
                    }
                    if (j > 0) {
                        $("#branchOfficersTable tbody").append('<tr id="dynBranchOfficer' + j + '">' + bocontent + '</tr>');
                        $("#branchOfficersTable tbody tr").last().find(".keyOffIdProof").addClass("keyOffIdProof" + j);
                        $("#branchOfficersTable tbody tr").last().find(".keyOffIdProof").removeClass("keyOffIdProof");
                        $("#branchOfficersTable tbody tr").last().find("#keyOffIdProof").attr("id", "keyOffIdProof" + j);
                        $("#branchOfficersTable tbody tr[id='dynBranchOfficer" + j + "'] input[name='bnchkeyOffhiddenId']").val(keyOffId);
                        $("#branchOfficersTable tbody tr[id='dynBranchOfficer" + j + "'] input[name='keyoffName']").val(keyOffName);
                        $("#branchOfficersTable tbody tr[id='dynBranchOfficer" + j + "'] input[name='keyoffDesignation']").val(keyOffDesignation);
                        $("#branchOfficersTable tbody tr[id='dynBranchOfficer" + j + "'] select[name='keyOffCountry'] option").filter(function () {
                            return $(this).val() == keyOffCountry;
                        }).prop("selected", "selected");
                        $("#branchOfficersTable tbody tr[id='dynBranchOfficer" + j + "'] input[name='keyOffCity']").val(keyOffCity);
                        $("#branchOfficersTable tbody tr[id='dynBranchOfficer" + j + "'] input[name='keyoffEmailId']").val(keyOffEmail);
                        // $("#branchOfficersTable tbody tr[id='dynBranchOfficer" + j + "'] input[name='keyoffEmailId']").attr("disabled", "disabled");
                        $("#branchOfficersTable tbody tr[id='dynBranchOfficer" + j + "'] select[name='bnchKeyOffcountryPhnCode'] option").filter(function () {
                            return $(this).html() == keyOffphnNumberCtryCode;
                        }).prop("selected", "selected");
                        $("#branchOfficersTable tbody tr[id='dynBranchOfficer" + j + "'] input[name='keyoffPhoneNumber1']").val(keyOffPhnNo1);
                        $("#branchOfficersTable tbody tr[id='dynBranchOfficer" + j + "'] input[name='keyoffPhoneNumber2']").val(keyOffPhnNo2);
                        $("#branchOfficersTable tbody tr[id='dynBranchOfficer" + j + "'] input[name='keyoffPhoneNumber3']").val(keyOffPhnNo3);
                        $("#branchOfficersTable tbody tr[id='dynBranchOfficer" + j + "'] select[name='bnchKeyOffperscountryPhnCode'] option").filter(function () {
                            return $(this).html() == keyOffPersPhnNoCtryCode;
                        }).prop("selected", "selected");
                        $("#branchOfficersTable tbody tr[id='dynBranchOfficer" + j + "'] input[name='keyoffpersPhoneNumber1']").val(keyOffPersPhNo1);
                        $("#branchOfficersTable tbody tr[id='dynBranchOfficer" + j + "'] input[name='keyoffpersPhoneNumber2']").val(keyOffPersPhNo2);
                        $("#branchOfficersTable tbody tr[id='dynBranchOfficer" + j + "'] input[name='keyoffpersPhoneNumber3']").val(keyOffPersPhNo3);
                        $("#branchOfficersTable tbody tr[id='dynBranchOfficer" + j + "'] input[name='keyOffIdProof']").val(keyOffIdProof);
                    }
                }
                //branch statutory edit
                for (var j = 0; j < data.branchdynmStatData.length; j++) {
                    var dynmStaHidId = data.branchdynmStatData[j].dynmStatHidnId;
                    var dynmStatRegNo = "";
                    var isStatAvailForInvoice = "";
                    var dynmStatDtails = "";
                    var dynmStatRegDoc = "";
                    var dynmStatRegVaidFrom = "";
                    var dynmStatRegValidTo = "";
                    var altForActions = "";
                    var altForInformations = "";
                    var nameAddOfConsultant = "";
                    var altRemarks = "";
                    if (data.branchdynmStatData[j].dynmStatDetails != null && data.branchdynmStatData[j].dynmStatDetails != "") {
                        dynmStatDtails = data.branchdynmStatData[j].dynmStatDetails;
                    }
                    if (data.branchdynmStatData[j].dynmStatRegNo != null && data.branchdynmStatData[j].dynmStatRegNo != "") {
                        dynmStatRegNo = data.branchdynmStatData[j].dynmStatRegNo;
                    }
                    if (data.branchdynmStatData[j].isStatAvailForInvoice != null && data.branchdynmStatData[j].isStatAvailForInvoice != "") {
                        isStatAvailForInvoice = data.branchdynmStatData[j].isStatAvailForInvoice;
                    }
                    if (data.branchdynmStatData[j].dynmStatRegDocUrl != null && data.branchdynmStatData[j].dynmStatRegDocUrl != "") {
                        dynmStatRegDoc = data.branchdynmStatData[j].dynmStatRegDocUrl;
                    }
                    if (data.branchdynmStatData[j].dynmStatRegValidFrom != null && data.branchdynmStatData[j].dynmStatRegValidFrom != "") {
                        dynmStatRegVaidFrom = data.branchdynmStatData[j].dynmStatRegValidFrom;
                    }
                    if (data.branchdynmStatData[j].dynmStatRegValidTo != null && data.branchdynmStatData[j].dynmStatRegValidTo != "") {
                        dynmStatRegValidTo = data.branchdynmStatData[j].dynmStatRegValidTo;
                    }
                    if (data.branchdynmStatData[j].alertForActions != null && data.branchdynmStatData[j].alertForActions != "") {
                        altForActions = data.branchdynmStatData[j].alertForActions;
                    }
                    if (data.branchdynmStatData[j].alertForInformations != null && data.branchdynmStatData[j].alertForInformations != "") {
                        altForInformations = data.branchdynmStatData[j].alertForInformations;
                    }
                    if (data.branchdynmStatData[j].nameAddressOfConsultant != null && data.branchdynmStatData[j].nameAddressOfConsultant != "") {
                        nameAddOfConsultant = data.branchdynmStatData[j].nameAddressOfConsultant;
                    }
                    if (data.branchdynmStatData[j].alertRemarks != null && data.branchdynmStatData[j].alertRemarks != "") {
                        altRemarks = data.branchdynmStatData[j].alertRemarks;
                    }
                    if (j == 0) {
                        $("#branchStatutoryTable tbody tr[id='copyContentBranchStatutory'] input[name='bnchstatutoryhiddenId']").val(dynmStaHidId);
                        $("#branchStatutoryTable tbody tr[id='copyContentBranchStatutory'] textarea[name='statutoryDetails']").val(dynmStatDtails);
                        $("#branchStatutoryTable tbody tr[id='copyContentBranchStatutory'] input[name='bnchStatutoryegistrationNumber']").val(dynmStatRegNo);
                        if (isStatAvailForInvoice == "1") {
                            $("#branchStatutoryTable tbody tr[id='copyContentBranchStatutory'] input[name='bnchStatInvoiceDisplayable']:checkbox").prop("checked", true);
                        }
                        if (isStatAvailForInvoice == "0") {
                            $("#branchStatutoryTable tbody tr[id='copyContentBranchStatutory'] input[name='bnchStatInvoiceDisplayable']:checkbox").prop("checked", false);
                        }
//						 $("#branchStatutoryTable tbody tr[id='copyContentBranchStatutory'] input[name='bnchStatutorySupportingDoc']").val(dynmStatRegDoc);
                        fillSelectElementWithUploadedDocs(data.branchdynmStatData[j].dynmStatRegDocUrl, 'copyContentBranchStatutory', 'bnchStatutorySupportingDoc');
                        $("#branchStatutoryTable tbody tr[id='copyContentBranchStatutory'] input[name='bnchStatutoryvalidFrom']").val(dynmStatRegVaidFrom);
                        $("#branchStatutoryTable tbody tr[id='copyContentBranchStatutory'] input[name='bnchStatutoryvalidTo']").val(dynmStatRegValidTo);
                        $("#branchStatutoryTable tbody tr[id='copyContentBranchStatutory'] select[name='alertForAction'] option").filter(function () {
                            return $(this).val() == altForActions;
                        }).prop("selected", "selected");
                        $("#branchStatutoryTable tbody tr[id='copyContentBranchStatutory'] select[name='alertForInformation'] option").filter(function () {
                            return $(this).val() == altForInformations;
                        }).prop("selected", "selected");
                        if (altForActions.trim() != "") {
                            var adminAlertActionInCaseOfDeactivateBranchOfficerUser = $("#branchStatutoryTable tbody tr[id='copyContentBranchStatutory'] select[name='alertForAction'] option").filter(function () {
                                return $(this).val() == altForActions;
                            }).attr('value');
                            if (typeof adminAlertActionInCaseOfDeactivateBranchOfficerUser == "undefined") {
                                $("#branchStatutoryTable tbody tr[id='copyContentBranchStatutory'] select[name='alertForAction']").append('<option value="' + altForActions + '" selected="selected">' + altForActions + '</option>');
                            }
                        }
                        if (altForInformations.trim() != "") {
                            var adminAlertInformationInCaseOfDeactivateBranchOfficerUser = $("#branchStatutoryTable tbody tr[id='copyContentBranchStatutory'] select[name='alertForInformation'] option").filter(function () {
                                return $(this).val() == altForInformations;
                            }).attr('value');
                            if (typeof adminAlertInformationInCaseOfDeactivateBranchOfficerUser == "undefined") {
                                $("#branchStatutoryTable tbody tr[id='copyContentBranchStatutory'] select[name='alertForInformation']").append('<option value="' + altForInformations + '" selected="selected">' + altForInformations + '</option>');
                            }
                        }
                        $("#branchStatutoryTable tbody tr[id='copyContentBranchStatutory'] textarea[name='nameAddressOfConsultant']").val(nameAddOfConsultant);
                        $("#branchStatutoryTable tbody tr[id='copyContentBranchStatutory'] textarea[name='statutoryRemarks']").val(altRemarks);
                    }
                    if (j > 0) {
                        $("#branchStatutoryTable tbody").append('<tr id="dynBranchStatutory' + j + '">' + bstat + '</tr>');
                        $("#branchStatutoryTable tbody tr").last().find(".bnchStatutorySupportingDoc").addClass("bnchStatutorySupportingDoc" + j);
                        $("#branchStatutoryTable tbody tr").last().find(".bnchStatutorySupportingDoc").removeClass("bnchStatutorySupportingDoc");
                        $("#branchStatutoryTable tbody tr").last().find("#bnchStatutorySupportingDoc").attr("id", "bnchStatutorySupportingDoc" + j);
                        $("#branchStatutoryTable tbody tr[id='dynBranchStatutory" + j + "'] input[name='bnchstatutoryhiddenId']").val(dynmStaHidId);
                        $("#branchStatutoryTable tbody tr[id='dynBranchStatutory" + j + "'] textarea[name='statutoryDetails']").val(dynmStatDtails);
                        $("#branchStatutoryTable tbody tr[id='dynBranchStatutory" + j + "'] input[name='bnchStatutoryegistrationNumber']").val(dynmStatRegNo);
                        if (isStatAvailForInvoice == "1") {
                            $("#branchStatutoryTable tbody tr[id='dynBranchStatutory" + j + "'] input[name='bnchStatInvoiceDisplayable']").prop("checked", true);
                        }
                        if (isStatAvailForInvoice == "0") {
                            $("#branchStatutoryTable tbody tr[id='dynBranchStatutory" + j + "'] input[name='bnchStatInvoiceDisplayable']").prop("checked", false);
                        }
//						 $("#branchStatutoryTable tbody tr[id='dynBranchStatutory"+j+"'] input[name='bnchStatutorySupportingDoc']").val(dynmStatRegDoc);
                        fillSelectElementWithUploadedDocs(data.branchdynmStatData[j].dynmStatRegDocUrl, 'copyContentBranchStatutory', 'bnchStatutorySupportingDoc');
                        $("#branchStatutoryTable tbody tr[id='dynBranchStatutory" + j + "'] input[name='bnchStatutoryvalidFrom']").val(dynmStatRegVaidFrom);
                        $("#branchStatutoryTable tbody tr[id='dynBranchStatutory" + j + "'] input[name='bnchStatutoryvalidTo']").val(dynmStatRegValidTo);
                        $("#branchStatutoryTable tbody tr[id='dynBranchStatutory" + j + "'] select[name='alertForAction'] option").filter(function () {
                            return $(this).val() == altForActions;
                        }).prop("selected", "selected");
                        $("#branchStatutoryTable tbody tr[id='dynBranchStatutory" + j + "'] select[name='alertForInformation'] option").filter(function () {
                            return $(this).val() == altForInformations;
                        }).prop("selected", "selected");
                        if (altForActions.trim() != "") {
                            var adminAlertActionInCaseOfDeactivateBranchOfficerUser = $("#branchStatutoryTable tbody tr[id='dynBranchStatutory" + j + "'] select[name='alertForAction'] option").filter(function () {
                                return $(this).val() == altForActions;
                            }).attr('value');
                            if (typeof adminAlertActionInCaseOfDeactivateBranchOfficerUser == "undefined") {
                                $("#branchStatutoryTable tbody tr[id='dynBranchStatutory" + j + "'] select[name='alertForAction']").append('<option value="' + altForActions + '" selected="selected">' + altForActions + '</option>');
                            }
                        }
                        if (altForInformations.trim() != "") {
                            var adminAlertInformationInCaseOfDeactivateBranchOfficerUser = $("#branchStatutoryTable tbody tr[id='dynBranchStatutory" + j + "'] select[name='alertForInformation'] option").filter(function () {
                                return $(this).val() == altForInformations;
                            }).attr('value');
                            if (typeof adminAlertInformationInCaseOfDeactivateBranchOfficerUser == "undefined") {
                                $("#branchStatutoryTable tbody tr[id='dynBranchStatutory" + j + "'] select[name='alertForInformation']").append('<option value="' + altForInformations + '" selected="selected">' + altForInformations + '</option>');
                            }
                        }
                        $("#branchStatutoryTable tbody tr[id='dynBranchStatutory" + j + "'] textarea[name='nameAddressOfConsultant']").val(nameAddOfConsultant);
                        $("#branchStatutoryTable tbody tr[id='dynBranchStatutory" + j + "'] textarea[name='statutoryRemarks']").val(altRemarks);
                        var i = 0;
                        $("#branchStatutoryTable tbody input[class*='datepicker']").each(function () {
                            i++;
                            $(this).attr('class', 'datepicker m-bottom-10 calendar');
                            var attrId = $(this).attr('id');
                            var newAttrId = attrId + i;
                            $(this).attr('id', newAttrId)
                        });
                        var maxYear = new Date().getFullYear() + 30;
                        $(function () {
                            $("#branchStatutoryTable tbody input[class*='datepicker']").datepicker({
                                changeMonth: true,
                                changeYear: true,
                                dateFormat: 'MM d,yy',
                                yearRange: '' + new Date().getFullYear() - 100 + ':' + maxYear + '',
                                onSelect: function (x, y) {
                                    var elemName = $(this).attr('name');
                                    if (elemName == 'bnchStatutoryvalidTo') {
                                        var parentTr = $(this).parent().parent().attr('id');
                                        if ($("#" + parentTr + " input[id='bnchStatutoryvalidFrom']").val() == "") {
                                            $(this).val("");
                                        }
                                        var d1 = new Date($("#" + parentTr + " input[name='bnchStatutoryvalidTo']").val())
                                        var d2 = new Date($("#" + parentTr + " input[name='bnchStatutoryvalidFrom']").val())
                                        if (d1 < d2) {
                                            swal("Error","Branch Statutory validity To cannot be less than validity from","error");
                                            $(this).val("");
                                            return true;
                                        }
                                    }
                                    $(this).focus();
                                }
                            });
                        });
                    }
                }
                //optional remainder edit details
                for (var j = 0; j < data.branchOperRemaindersData.length; j++) {
                    var optRemHidId = data.branchOperRemaindersData[j].operationRemHidIds;
                    var requirements = "";
                    var dueOn = "";
                    var recurrence = "";
                    var operRemindersValidTo = "";
                    var altForActions = "";
                    var altForInformations = "";
                    var altRemarks = "";
                    if (data.branchOperRemaindersData[j].requirements != null && data.branchOperRemaindersData[j].requirements != "") {
                        requirements = data.branchOperRemaindersData[j].requirements;
                    }
                    if (data.branchOperRemaindersData[j].dueOn != null && data.branchOperRemaindersData[j].dueOn != "") {
                        dueOn = data.branchOperRemaindersData[j].dueOn;
                    }
                    if (data.branchOperRemaindersData[j].operRemindersValidTo != null && data.branchOperRemaindersData[j].operRemindersValidTo != "") {
                        operRemindersValidTo = data.branchOperRemaindersData[j].operRemindersValidTo;
                    }
                    if (data.branchOperRemaindersData[j].recurrence != null && data.branchOperRemaindersData[j].recurrence != "") {
                        recurrence = data.branchOperRemaindersData[j].recurrence;
                    }
                    if (data.branchOperRemaindersData[j].alertforaction != null && data.branchOperRemaindersData[j].alertforaction != "") {
                        altForActions = data.branchOperRemaindersData[j].alertforaction;
                    }
                    if (data.branchOperRemaindersData[j].alertforinformation != null && data.branchOperRemaindersData[j].alertforinformation != "") {
                        altForInformations = data.branchOperRemaindersData[j].alertforinformation;
                    }
                    if (data.branchOperRemaindersData[j].remarks != null && data.branchOperRemaindersData[j].remarks != "") {
                        altRemarks = data.branchOperRemaindersData[j].remarks;
                    }
                    if (j == 0) {
                        $("#branchOperationalRemainderTable tbody tr[id='copyContentBranchOpRem'] input[name='bnchOperationalRemHidId']").val(optRemHidId);
                        $("#branchOperationalRemainderTable tbody tr[id='copyContentBranchOpRem'] textarea[name='operationalRemRequirements']").val(requirements);
                        $("#branchOperationalRemainderTable tbody tr[id='copyContentBranchOpRem'] input[name='dueOn']").val(dueOn);
                        $("#branchOperationalRemainderTable tbody tr[id='copyContentBranchOpRem'] input[name='validTo']").val(operRemindersValidTo);
                        $("#branchOperationalRemainderTable tbody tr[id='copyContentBranchOpRem'] select[name='recurrences'] option").filter(function () {
                            return $(this).val() == recurrence;
                        }).prop("selected", "selected");
                        $("#branchOperationalRemainderTable tbody tr[id='copyContentBranchOpRem'] select[name='bnchOperRemalertForAction'] option").filter(function () {
                            return $(this).val() == altForActions;
                        }).prop("selected", "selected");
                        $("#branchOperationalRemainderTable tbody tr[id='copyContentBranchOpRem'] select[name='bnchOperRemalertForInformation'] option").filter(function () {
                            return $(this).val() == altForInformations;
                        }).prop("selected", "selected");
                        if (altForActions.trim() != "") {
                            var adminAlertActionInCaseOfDeactivateBranchOfficerUser = $("#branchOperationalRemainderTable tbody tr[id='copyContentBranchOpRem'] select[name='bnchOperRemalertForAction'] option").filter(function () {
                                return $(this).val() == altForActions;
                            }).attr('value');
                            if (typeof adminAlertActionInCaseOfDeactivateBranchOfficerUser == "undefined") {
                                $("#branchOperationalRemainderTable tbody tr[id='copyContentBranchOpRem'] select[name='bnchOperRemalertForAction']").append('<option value="' + altForActions + '" selected="selected">' + altForActions + '</option>');
                            }
                        }
                        if (altForInformations.trim() != "") {
                            var adminAlertInformationInCaseOfDeactivateBranchOfficerUser = $("#branchOperationalRemainderTable tbody tr[id='copyContentBranchOpRem'] select[name='bnchOperRemalertForInformation'] option").filter(function () {
                                return $(this).val() == altForInformations;
                            }).attr('value');
                            if (typeof adminAlertInformationInCaseOfDeactivateBranchOfficerUser == "undefined") {
                                $("#branchOperationalRemainderTable tbody tr[id='copyContentBranchOpRem'] select[name='bnchOperRemalertForInformation']").append('<option value="' + altForInformations + '" selected="selected">' + altForInformations + '</option>');
                            }
                        }
                        $("#branchOperationalRemainderTable tbody tr[id='copyContentBranchOpRem'] textarea[name='operationalRemRemarks']").val(altRemarks);
                    }
                    if (j > 0) {
                        $("#branchOperationalRemainderTable tbody").append('<tr id="dynBranchOperRem' + j + '">' + opeRem + '</tr>');
                        $("#branchOperationalRemainderTable tbody tr[id='dynBranchOperRem" + j + "'] input[name='bnchOperationalRemHidId']").val(optRemHidId);
                        $("#branchOperationalRemainderTable tbody tr[id='dynBranchOperRem" + j + "'] textarea[name='operationalRemRequirements']").val(requirements);
                        $("#branchOperationalRemainderTable tbody tr[id='dynBranchOperRem" + j + "'] input[name='dueOn']").val(dueOn);
                        $("#branchOperationalRemainderTable tbody tr[id='dynBranchOperRem" + j + "'] input[name='validTo']").val(operRemindersValidTo);
                        $("#branchOperationalRemainderTable tbody tr[id='dynBranchOperRem" + j + "'] select[name='recurrences'] option").filter(function () {
                            return $(this).val() == recurrence;
                        }).prop("selected", "selected");
                        $("#branchOperationalRemainderTable tbody tr[id='dynBranchOperRem" + j + "'] select[name='bnchOperRemalertForAction'] option").filter(function () {
                            return $(this).val() == altForActions;
                        }).prop("selected", "selected");
                        $("#branchOperationalRemainderTable tbody tr[id='dynBranchOperRem" + j + "'] select[name='bnchOperRemalertForInformation'] option").filter(function () {
                            return $(this).val() == altForInformations;
                        }).prop("selected", "selected");
                        if (altForActions.trim() != "") {
                            var adminAlertActionInCaseOfDeactivateBranchOfficerUser = $("#branchOperationalRemainderTable tbody tr[id='dynBranchOperRem" + j + "'] select[name='bnchOperRemalertForAction'] option").filter(function () {
                                return $(this).val() == altForActions;
                            }).attr('value');
                            if (typeof adminAlertActionInCaseOfDeactivateBranchOfficerUser == "undefined") {
                                $("#branchOperationalRemainderTable tbody tr[id='dynBranchOperRem" + j + "'] select[name='bnchOperRemalertForAction']").append('<option value="' + altForActions + '" selected="selected">' + altForActions + '</option>');
                            }
                        }
                        if (altForInformations.trim() != "") {
                            var adminAlertInformationInCaseOfDeactivateBranchOfficerUser = $("#branchOperationalRemainderTable tbody tr[id='dynBranchOperRem" + j + "'] select[name='bnchOperRemalertForInformation'] option").filter(function () {
                                return $(this).val() == altForInformations;
                            }).attr('value');
                            if (typeof adminAlertInformationInCaseOfDeactivateBranchOfficerUser == "undefined") {
                                $("#branchOperationalRemainderTable tbody tr[id='dynBranchOperRem" + j + "'] select[name='bnchOperRemalertForInformation']").append('<option value="' + altForInformations + '" selected="selected">' + altForInformations + '</option>');
                            }
                        }
                        $("#branchOperationalRemainderTable tbody tr[id='dynBranchOperRem" + j + "'] textarea[name='operationalRemRemarks']").val(altRemarks);
                        var i = 0;
                        $("#branchOperationalRemainderTable tbody input[class*='datepicker']").each(function () {
                            i++;
                            $(this).attr('class', 'datepicker');
                            var attrId = $(this).attr('id');
                            var newAttrId = attrId + i;
                            $(this).attr('id', newAttrId)
                        });
                        var maxYear = new Date().getFullYear() + 30;
                        $(function () {
                            $("#branchOperationalRemainderTable tbody input[class*='datepicker']").datepicker({
                                changeMonth: true,
                                changeYear: true,
                                dateFormat: 'MM d,yy',
                                yearRange: '' + new Date().getFullYear() - 100 + ':' + maxYear + ''
                            });
                        });
                    }
                }
                //branch safe deposit box
                for (var j = 0; j < data.branchSafeDepositBoxData.length; j++) {
                    var safeDepositBoxHidIds = data.branchSafeDepositBoxData[j].safeDepositBoxHidIds;
                    var keyCustodianPhNoCtryCode = "";
                    var keyCustphnNumber = "";
                    var keyCustPhNo1 = "";
                    var keyCustPhNo2 = "";
                    var keyCustPhNo3 = "";
                    var keyCustodianName = "";
                    var keyCustodianemail = "";
                    var safeDepBoxCashier = "";
                    var cashierPhNoCtryCode = "";
                    var cashierphnnumber = "";
                    var keyCashPhNo1 = "";
                    var keyCashPhNo2 = "";
                    var keyCashPhNo3 = "";
                    var cashieremail = "";
                    var cashierkl = "";
                    var pettyCashTxnApprovalReqd = "";
                    var pettyCashTxnApprovalAmtLimit = "";
                    var keyCustodianOpeningBalance = "";
                    if (data.branchSafeDepositBoxData[j].keyCustodianName != null && data.branchSafeDepositBoxData[j].keyCustodianName != "") {
                        keyCustodianName = data.branchSafeDepositBoxData[j].keyCustodianName;
                    }

                    if (data.branchSafeDepositBoxData[j].keyCustodianOpeningBalance != null && data.branchSafeDepositBoxData[j].keyCustodianOpeningBalance != "") {
                        keyCustodianOpeningBalance = data.branchSafeDepositBoxData[j].keyCustodianOpeningBalance;
                    }

                    if (data.branchSafeDepositBoxData[j].keyCustodianPhNoCtryCode != null && data.branchSafeDepositBoxData[j].keyCustodianPhNoCtryCode != "") {
                        keyCustodianPhNoCtryCode = data.branchSafeDepositBoxData[j].keyCustodianPhNoCtryCode;
                    }
                    if (data.branchSafeDepositBoxData[j].keyCustphnNumber != null && data.branchSafeDepositBoxData[j].keyCustphnNumber != "") {
                        keyCustphnNumber = data.branchSafeDepositBoxData[j].keyCustphnNumber;
                        keyCustPhNo1 = data.branchSafeDepositBoxData[j].keyCustphnNumber.substring(0, 3);
                        keyCustPhNo2 = data.branchSafeDepositBoxData[j].keyCustphnNumber.substring(3, 6);
                        keyCustPhNo3 = data.branchSafeDepositBoxData[j].keyCustphnNumber.substring(6, 10);
                    }
                    if (data.branchSafeDepositBoxData[j].keyCustodianemail != null && data.branchSafeDepositBoxData[j].keyCustodianemail != "") {
                        keyCustodianemail = data.branchSafeDepositBoxData[j].keyCustodianemail;
                    }
                    if (data.branchSafeDepositBoxData[j].safeDepBoxCashier != null && data.branchSafeDepositBoxData[j].safeDepBoxCashier != "") {
                        safeDepBoxCashier = data.branchSafeDepositBoxData[j].safeDepBoxCashier;
                    }
                    if (data.branchSafeDepositBoxData[j].cashierPhNoCtryCode != null && data.branchSafeDepositBoxData[j].cashierPhNoCtryCode != "") {
                        cashierPhNoCtryCode = data.branchSafeDepositBoxData[j].cashierPhNoCtryCode;
                    }
                    if (data.branchSafeDepositBoxData[j].cashierphnnumber != null && data.branchSafeDepositBoxData[j].cashierphnnumber != "") {
                        cashierphnnumber = data.branchSafeDepositBoxData[j].cashierphnnumber;
                        keyCashPhNo1 = data.branchSafeDepositBoxData[j].cashierphnnumber.substring(0, 3);
                        keyCashPhNo2 = data.branchSafeDepositBoxData[j].cashierphnnumber.substring(3, 6);
                        keyCashPhNo3 = data.branchSafeDepositBoxData[j].cashierphnnumber.substring(6, 10);
                    }
                    if (data.branchSafeDepositBoxData[j].cashieremail != null && data.branchSafeDepositBoxData[j].cashieremail != "") {
                        cashieremail = data.branchSafeDepositBoxData[j].cashieremail;
                    }
                    if (data.branchSafeDepositBoxData[j].cashierkl != null && data.branchSafeDepositBoxData[j].cashierkl != "") {
                        cashierkl = data.branchSafeDepositBoxData[j].cashierkl;
                    }
                    if (data.branchSafeDepositBoxData[j].pettyCashTnApprovalRequired != null && data.branchSafeDepositBoxData[j].pettyCashTnApprovalRequired != "") {
                        pettyCashTxnApprovalReqd = data.branchSafeDepositBoxData[j].pettyCashTnApprovalRequired;
                        if (data.branchSafeDepositBoxData[j].pettyCashTnApprovalRequired == "1") {
                            if (data.branchSafeDepositBoxData[j].pettyCashTnApprovalAmountLimit != null && data.branchSafeDepositBoxData[j].pettyCashTnApprovalAmountLimit != "") {
                                pettyCashTxnApprovalAmtLimit = data.branchSafeDepositBoxData[j].pettyCashTnApprovalAmountLimit;
                            }
                        }
                    }

                    $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] input[name='bnchkeyDephiddenId']").val(safeDepositBoxHidIds);
                    $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] input[name='keyDepositName']").val(keyCustodianName);
                    $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] input[name='cashierNameDepoOpenBal']").val(keyCustodianOpeningBalance);
                    // $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] input[name='cashierNameDepoOpenBal']").val(keyCustodianOpeningBalance);
                    $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] select[name='bnchKeyDepositcountryPhnCode'] option").filter(function () {
                        return $(this).html() == keyCustodianPhNoCtryCode;
                    }).prop("selected", "selected");
                    $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] input[name='keyDepositPhoneNumber1']").val(keyCustPhNo1);
                    $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] input[name='keyDepositPhoneNumber2']").val(keyCustPhNo2);
                    $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] input[name='keyDepositPhoneNumber3']").val(keyCustPhNo3);
                    $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] input[name='keyDepositEmailId']").val(keyCustodianemail);
                    $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] input[name='cashierName']").val(safeDepBoxCashier);
                    $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] select[name='cashiercountryPhnCode'] option").filter(function () {
                        return $(this).html() == cashierPhNoCtryCode;
                    }).prop("selected", "selected");
                    $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] input[name='cashierPhoneNumber1']").val(keyCashPhNo1);
                    $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] input[name='cashierPhoneNumber2']").val(keyCashPhNo2);
                    $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] input[name='cashierPhoneNumber3']").val(keyCashPhNo3);
                    $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] input[name='cashierEmailId']").val(cashieremail);
                    $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] textarea[name='cashierKl']").val(cashierkl);
                    $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] select[name='txnApprovalReq'] option").filter(function () {
                        return $(this).val() == pettyCashTxnApprovalReqd;
                    }).prop("selected", "selected");
                    if (pettyCashTxnApprovalReqd == "1") {
                        $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] b[class='approvalAboveLimit']").show();
                        $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] b[class='approvalAboveLimit']").text("Approval Amount Limit");
                        $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] input[name='approvalAboveLimit']").show();
                    }
                    if (pettyCashTxnApprovalReqd == "0") {
                        $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] b[class='approvalAboveLimit']").hide();
                        $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] b[class='approvalAboveLimit']").text("");
                        $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] input[name='approvalAboveLimit']").hide();
                    }
                    $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] input[name='approvalAboveLimit']").val(pettyCashTxnApprovalAmtLimit);
                    $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] input[name='pettyCashOpeningBalance']").val(data.branchSafeDepositBoxData[j].pettyCashOpeningBalance);
                    $("#branchSafeDepositBoxTable tbody tr[id='copyContentBranchKeyDep'] input[name='pettyCashOpeningBalanceHid']").val(data.branchSafeDepositBoxData[j].pettyCashOpeningBalance);
                }

                //branch insurence edit data
                for (var j = 0; j < data.branchInsurenceData.length; j++) {
                    var bnchInsId = data.branchInsurenceData[j].bnchInsId;
                    var bnchInsPolType = "";
                    var policyNumber = "";
                    var insurenceComp = "";
                    var insurenceDoc = "";
                    var policyValidityFrom = "";
                    var policyValidityTo = "";
                    var annualPremium = "";
                    var alertForActions = "";
                    var alertForInformations = "";
                    var remarks = "";
                    if (data.branchInsurenceData[j].bnchInsPolType != null && data.branchInsurenceData[j].bnchInsPolType != "") {
                        bnchInsPolType = data.branchInsurenceData[j].bnchInsPolType;
                    }
                    if (data.branchInsurenceData[j].policyNumber != null && data.branchInsurenceData[j].policyNumber != "") {
                        policyNumber = data.branchInsurenceData[j].policyNumber;
                    }
                    if (data.branchInsurenceData[j].insurenceComp != null && data.branchInsurenceData[j].insurenceComp != "") {
                        insurenceComp = data.branchInsurenceData[j].insurenceComp;
                    }
                    if (data.branchInsurenceData[j].insurenceDoc != null && data.branchInsurenceData[j].insurenceDoc != "") {
                        insurenceDoc = data.branchInsurenceData[j].insurenceDoc;
                    }
                    if (data.branchInsurenceData[j].policyValidityFrom != null && data.branchInsurenceData[j].policyValidityFrom != "") {
                        policyValidityFrom = data.branchInsurenceData[j].policyValidityFrom;
                    }
                    if (data.branchInsurenceData[j].policyValidityTo != null && data.branchInsurenceData[j].policyValidityTo != "") {
                        policyValidityTo = data.branchInsurenceData[j].policyValidityTo;
                    }
                    if (data.branchInsurenceData[j].annualPremium != null && data.branchInsurenceData[j].annualPremium != "") {
                        annualPremium = data.branchInsurenceData[j].annualPremium;
                    }
                    if (data.branchInsurenceData[j].alertForActions != null && data.branchInsurenceData[j].alertForActions != "") {
                        alertForActions = data.branchInsurenceData[j].alertForActions;
                    }
                    if (data.branchInsurenceData[j].alertForInformations != null && data.branchInsurenceData[j].alertForInformations != "") {
                        alertForInformations = data.branchInsurenceData[j].alertForInformations;
                    }
                    if (data.branchInsurenceData[j].remarks != null && data.branchInsurenceData[j].remarks != "") {
                        remarks = data.branchInsurenceData[j].remarks;
                    }
                    if (j == 0) {
                        $("#branchInsurenceTable tbody tr[id='copyBranchInsurence'] input[name='bnchInshiddenId']").val(bnchInsId);
                        $("#branchInsurenceTable tbody tr[id='copyBranchInsurence'] input[name='typeOfPolicy']").val(bnchInsPolType);
                        $("#branchInsurenceTable tbody tr[id='copyBranchInsurence'] input[name='policyNumber']").val(policyNumber);
                        $("#branchInsurenceTable tbody tr[id='copyBranchInsurence'] input[name='insurenceCompany']").val(insurenceComp);
//						 $("#branchInsurenceTable tbody tr[id='copyBranchInsurence'] input[name='insurancePolicy']").val(insurenceDoc);
                        fillSelectElementWithUploadedDocs(data.branchInsurenceData[j].insurenceDoc, 'copyBranchInsurence', 'insurancePolicy');
                        $("#branchInsurenceTable tbody tr[id='copyBranchInsurence'] input[name='insuranceValidityFrom']").val(policyValidityFrom);
                        $("#branchInsurenceTable tbody tr[id='copyBranchInsurence'] input[name='insuranceValidityTo']").val(policyValidityTo);
                        $("#branchInsurenceTable tbody tr[id='copyBranchInsurence'] input[name='premiumAmount']").val(annualPremium);
                        $("#branchInsurenceTable tbody tr[id='copyBranchInsurence'] select[name='bnchInsalertForAction'] option").filter(function () {
                            return $(this).val() == alertForActions;
                        }).prop("selected", "selected");
                        $("#branchInsurenceTable tbody tr[id='copyBranchInsurence'] select[name='bnchInsalertForInformation'] option").filter(function () {
                            return $(this).val() == alertForInformations;
                        }).prop("selected", "selected");
                        $("#branchInsurenceTable tbody tr[id='copyBranchInsurence'] textarea[name='remarks']").val(remarks);
                    }
                    if (j > 0) {
                        $("#branchInsurenceTable tbody").append('<tr id="dynBranchInsurence' + j + '">' + bins + '</tr>');
                        $("#branchInsurenceTable tbody tr").last().find(".insurancePolicy").addClass("insurancePolicy" + j);
                        $("#branchInsurenceTable tbody tr").last().find(".insurancePolicy").removeClass("insurancePolicy");
                        $("#branchInsurenceTable tbody tr").last().find("#insurancePolicy").attr("id", "insurancePolicy" + j);
                        $("#branchInsurenceTable tbody tr[id='dynBranchInsurence" + j + "'] input[name='bnchInshiddenId']").val(bnchInsId);
                        $("#branchInsurenceTable tbody tr[id='dynBranchInsurence" + j + "'] input[name='typeOfPolicy']").val(bnchInsPolType);
                        $("#branchInsurenceTable tbody tr[id='dynBranchInsurence" + j + "'] input[name='policyNumber']").val(policyNumber);
                        $("#branchInsurenceTable tbody tr[id='dynBranchInsurence" + j + "'] input[name='insurenceCompany']").val(insurenceComp);
//						 $("#branchInsurenceTable tbody tr[id='dynBranchInsurence"+j+"'] input[name='insurancePolicy']").val(insurenceDoc);
                        fillSelectElementWithUploadedDocs(data.branchInsurenceData[j].insurenceDoc, 'copyBranchInsurence', 'insurancePolicy');
                        $("#branchInsurenceTable tbody tr[id='dynBranchInsurence" + j + "'] input[name='insuranceValidityFrom']").val(policyValidityFrom);
                        $("#branchInsurenceTable tbody tr[id='dynBranchInsurence" + j + "'] input[name='insuranceValidityTo']").val(policyValidityTo);
                        $("#branchInsurenceTable tbody tr[id='dynBranchInsurence" + j + "'] input[name='premiumAmount']").val(annualPremium);
                        $("#branchInsurenceTable tbody tr[id='dynBranchInsurence" + j + "'] select[name='bnchInsalertForAction'] option").filter(function () {
                            return $(this).val() == alertForActions;
                        }).prop("selected", "selected");
                        $("#branchInsurenceTable tbody tr[id='dynBranchInsurence" + j + "'] select[name='bnchInsalertForInformation'] option").filter(function () {
                            return $(this).val() == alertForInformations;
                        }).prop("selected", "selected");
                        $("#branchInsurenceTable tbody tr[id='dynBranchInsurence" + j + "'] textarea[name='remarks']").val(remarks);
                        var i = 0;
                        $("#branchInsurenceTable tbody input[class*='datepicker']").each(function () {
                            i++;
                            $(this).attr('class', 'datepicker m-bottom-10 calendar');
                            var attrId = $(this).attr('id');
                            var newAttrId = attrId + i;
                            $(this).attr('id', newAttrId)
                        });
                        var maxYear = new Date().getFullYear() + 30;
                        $(function () {
                            $("#branchInsurenceTable tbody input[class*='datepicker']").datepicker({
                                changeMonth: true,
                                changeYear: true,
                                dateFormat: 'MM d,yy',
                                yearRange: '' + new Date().getFullYear() - 100 + ':' + maxYear + '',
                                onSelect: function (x, y) {
                                    var elemName = $(this).attr('name');
                                    if (elemName == 'insuranceValidityTo') {
                                        var parentTr = $(this).parent().parent().attr('id');
                                        if ($("#" + parentTr + " input[id='insuranceValidityFrom']").val() == "") {
                                            $(this).val("");
                                        }
                                        var d1 = new Date($("#" + parentTr + " input[name='insuranceValidityTo']").val())
                                        var d2 = new Date($("#" + parentTr + " input[name='insuranceValidityFrom']").val())
                                        if (d1 < d2) {
                                            swal("Error!","Branch Insurence validity To cannot be less than validity from","error");
                                            $(this).val("");
                                            return true;
                                        }
                                    }
                                    $(this).focus();
                                }
                            });
                        });
                    }
                }

                for (var j = 0; j < data.branchBankActData.length; j++) {
                    var bankActPrimKey = data.branchBankActData[j].branchBankAccounId;
                    var branchBankAccountBankName = "";
                    var branchBankAccountType = "";
                    var branchBankAccountNumber = "";
                    var branchBankAccounttAuthSignName = "";
                    var branchBankAccounttAuthSignEmail = "";
                    var branchBankAccounttAddress = "";
                    var branchBankAccounttPhnNoCtryCode = "";
                    var branchBankAccounttPhnNo = "";
                    var branchBankAccountSwiftCode = "";
                    var branchBankAccountRoutingNumber = "";
                    var branchBankAccountCheckbookCustody = "";
                    var branchBankAccountCheckbookCustodyEmail = "";
                    var branchBankAccountOpeningBalance = "";
					var isBranchBankCreated="";

					//this is used only for bhive
					if( typeof  data.branchBankActData[j].branchBankAccountBankCreationStatus != 'undefined' && data.branchBankActData[j].branchBankAccountBankCreationStatus != null && data.branchBankActData[j].branchBankAccountBankCreationStatus!=""){
					isBranchBankCreated = data.branchBankActData[j].branchBankAccountBankCreationStatus;
				    }

                    if (data.branchBankActData[j].branchBankAccountBankName != null && data.branchBankActData[j].branchBankAccountBankName != "") {
                        branchBankAccountBankName = data.branchBankActData[j].branchBankAccountBankName;
                    }
                    if (data.branchBankActData[j].branchBankAccountType != null && data.branchBankActData[j].branchBankAccountType != "") {
                        branchBankAccountType = data.branchBankActData[j].branchBankAccountType;
                    }
                    if (data.branchBankActData[j].branchBankAccountNumber != null && data.branchBankActData[j].branchBankAccountNumber != "") {
                        branchBankAccountNumber = data.branchBankActData[j].branchBankAccountNumber;
                    }

                    if (data.branchBankActData[j].branchBankAccountOpeningBalance != null && data.branchBankActData[j].branchBankAccountOpeningBalance != "") {
                        branchBankAccountOpeningBalance = data.branchBankActData[j].branchBankAccountOpeningBalance;
                    }

                    if (data.branchBankActData[j].branchBankAccounttAuthSignName != null && data.branchBankActData[j].branchBankAccounttAuthSignName != "") {
                        branchBankAccounttAuthSignName = data.branchBankActData[j].branchBankAccounttAuthSignName;
                    }
                    if (data.branchBankActData[j].branchBankAccounttAuthSignEmail != null && data.branchBankActData[j].branchBankAccounttAuthSignEmail != "") {
                        branchBankAccounttAuthSignEmail = data.branchBankActData[j].branchBankAccounttAuthSignEmail;
                    }
                    if (data.branchBankActData[j].branchBankAccounttAddress != null && data.branchBankActData[j].branchBankAccounttAddress != "") {
                        branchBankAccounttAddress = data.branchBankActData[j].branchBankAccounttAddress;
                    }
                    if (data.branchBankActData[j].branchBankAccounttPhnNoCtryCode != null && data.branchBankActData[j].branchBankAccounttPhnNoCtryCode != "") {
                        branchBankAccounttPhnNoCtryCode = data.branchBankActData[j].branchBankAccounttPhnNoCtryCode;
                    }
                    if (data.branchBankActData[j].branchBankAccounttPhnNo != null && data.branchBankActData[j].branchBankAccounttPhnNo != "") {
                        branchBankAccounttPhnNo = data.branchBankActData[j].branchBankAccounttPhnNo;
                    }
                    if (data.branchBankActData[j].branchBankAccountSwiftCode != null && data.branchBankActData[j].branchBankAccountSwiftCode != "") {
                        branchBankAccountSwiftCode = data.branchBankActData[j].branchBankAccountSwiftCode;
                    }
                    if (data.branchBankActData[j].branchBankAccountRoutingNumber != null && data.branchBankActData[j].branchBankAccountRoutingNumber != "") {
                        branchBankAccountRoutingNumber = data.branchBankActData[j].branchBankAccountRoutingNumber;
                    }
                    if (data.branchBankActData[j].branchBankAccountCheckbookCustody != null && data.branchBankActData[j].branchBankAccountCheckbookCustody != "") {
                        branchBankAccountCheckbookCustody = data.branchBankActData[j].branchBankAccountCheckbookCustody;
                    }
                    if (data.branchBankActData[j].branchBankAccountCheckbookCustodyEmail != null && data.branchBankActData[j].branchBankAccountCheckbookCustodyEmail != "") {
                        branchBankAccountCheckbookCustodyEmail = data.branchBankActData[j].branchBankAccountCheckbookCustodyEmail;
                    }
                    if (j == 0) {
                        $("#branchBankAccountTable tbody tr[id='copyBranchBankAccount'] input[id='bnchBnkActhiddenId']").val(bankActPrimKey);
                        $("#branchBankAccountTable tbody tr[id='copyBranchBankAccount'] input[id='bnkActName']").val(branchBankAccountBankName);
						customMethod4(isBranchBankCreated, branchBankAccountBankName, bankActPrimKey, j);
                        $("#branchBankAccountTable tbody tr[id='copyBranchBankAccount'] select[id='bnkActType']").find("option[value='" + branchBankAccountType + "']").prop("selected", "selected");
                        $("#branchBankAccountTable tbody tr[id='copyBranchBankAccount'] input[id='bnkActNumber']").val(branchBankAccountNumber);

                        $("#branchBankAccountTable tbody tr[id='copyBranchBankAccount'] input[id='bnkActOpeningBalance']").val(branchBankAccountOpeningBalance);
                        $("#branchBankAccountTable tbody tr[id='copyBranchBankAccount'] input[id='bankOpeningBalanceHid']").val(branchBankAccountOpeningBalance);
                        $("#branchBankAccountTable tbody tr[id='copyBranchBankAccount'] input[id='authSignName']").val(branchBankAccounttAuthSignName);
                        $("#branchBankAccountTable tbody tr[id='copyBranchBankAccount'] input[id='authSignEmail']").val(branchBankAccounttAuthSignEmail);
                        $("#branchBankAccountTable tbody tr[id='copyBranchBankAccount'] textarea[id='bnkAddress']").val(branchBankAccounttAddress);
                        $("#branchBankAccountTable tbody tr[id='copyBranchBankAccount'] select[id='bnchbnkactPhnNocountryCode'] option").filter(function () {
                            return $(this).html() == branchBankAccounttPhnNoCtryCode;
                        }).prop("selected", "selected");
                        $("#branchBankAccountTable tbody tr[id='copyBranchBankAccount'] input[id='bankPhnNumber1']").val(branchBankAccounttPhnNo.substring(0, 3));
                        $("#branchBankAccountTable tbody tr[id='copyBranchBankAccount'] input[id='bankPhnNumber2']").val(branchBankAccounttPhnNo.substring(3, 6));
                        $("#branchBankAccountTable tbody tr[id='copyBranchBankAccount'] input[id='bankPhnNumber3']").val(branchBankAccounttPhnNo.substring(6, 10));
                        $("#branchBankAccountTable tbody tr[id='copyBranchBankAccount'] input[id='bnkSwiftCode']").val(branchBankAccountSwiftCode);
                        $("#branchBankAccountTable tbody tr[id='copyBranchBankAccount'] input[id='routingNumber']").val(branchBankAccountRoutingNumber);
                        $("#branchBankAccountTable tbody tr[id='copyBranchBankAccount'] input[id='checkBookCustody']").val(branchBankAccountCheckbookCustody);
                        $("#branchBankAccountTable tbody tr[id='copyBranchBankAccount'] input[id='checkBookCustodyEmail']").val(branchBankAccountCheckbookCustodyEmail);
                    }
                    if (j > 0) {
                        $("#branchBankAccountTable tbody").append('<tr id="dynBranchBnkAct' + j + '">' + bbnkact + '</tr>');
                        $("#branchBankAccountTable tbody tr[id='dynBranchBnkAct" + j + "'] input[id='bnchBnkActhiddenId']").val(bankActPrimKey);
						customMethod5(isBranchBankCreated, branchBankAccountBankName, bankActPrimKey, j);
                        $("#branchBankAccountTable tbody tr[id='dynBranchBnkAct" + j + "'] input[id='bnkActName']").val(branchBankAccountBankName);
                        $("#branchBankAccountTable tbody tr[id='dynBranchBnkAct" + j + "'] select[id='bnkActType']").find("option[value='" + branchBankAccountType + "']").prop("selected", "selected");
                        $("#branchBankAccountTable tbody tr[id='dynBranchBnkAct" + j + "'] input[id='bnkActNumber']").val(branchBankAccountNumber);
                        $("#branchBankAccountTable tbody tr[id='dynBranchBnkAct" + j + "'] input[id='bnkActOpeningBalance']").val(branchBankAccountOpeningBalance);
                        $("#branchBankAccountTable tbody tr[id='dynBranchBnkAct" + j + "'] input[id='bankOpeningBalanceHid']").val(branchBankAccountOpeningBalance);
                        $("#branchBankAccountTable tbody tr[id='dynBranchBnkAct" + j + "'] input[id='authSignName']").val(branchBankAccounttAuthSignName);
                        $("#branchBankAccountTable tbody tr[id='dynBranchBnkAct" + j + "'] input[id='authSignEmail']").val(branchBankAccounttAuthSignEmail);
                        $("#branchBankAccountTable tbody tr[id='dynBranchBnkAct" + j + "'] textarea[id='bnkAddress']").val(branchBankAccounttAddress);
                        $("#branchBankAccountTable tbody tr[id='dynBranchBnkAct" + j + "'] select[id='bnchbnkactPhnNocountryCode'] option").filter(function () {
                            return $(this).html() == branchBankAccounttPhnNoCtryCode;
                        }).prop("selected", "selected");
                        $("#branchBankAccountTable tbody tr[id='dynBranchBnkAct" + j + "'] input[id='bankPhnNumber1']").val(branchBankAccounttPhnNo.substring(0, 3));
                        $("#branchBankAccountTable tbody tr[id='dynBranchBnkAct" + j + "'] input[id='bankPhnNumber2']").val(branchBankAccounttPhnNo.substring(3, 6));
                        $("#branchBankAccountTable tbody tr[id='dynBranchBnkAct" + j + "'] input[id='bankPhnNumber3']").val(branchBankAccounttPhnNo.substring(6, 10));
                        $("#branchBankAccountTable tbody tr[id='dynBranchBnkAct" + j + "'] input[id='bnkSwiftCode']").val(branchBankAccountSwiftCode);
                        $("#branchBankAccountTable tbody tr[id='dynBranchBnkAct" + j + "'] input[id='routingNumber']").val(branchBankAccountRoutingNumber);
                        $("#branchBankAccountTable tbody tr[id='dynBranchBnkAct" + j + "'] input[id='checkBookCustody']").val(branchBankAccountCheckbookCustody);
                        $("#branchBankAccountTable tbody tr[id='dynBranchBnkAct" + j + "'] input[id='checkBookCustodyEmail']").val(branchBankAccountCheckbookCustodyEmail);
                    }
                }

                //Branch taxes
                var inputTaxCounter = 0;
                var outputTaxCounter = 0;

                for (var j = 0; j < data.branchTaxData.length; j++) {
                    if (data.branchTaxData[j].branchTaxType == "1") {
                        var inputTaxPrimId = data.branchTaxData[j].branchTaxHidIds;
                        var branchInputTaxName = "";
                        var branchInputTaxRates = "";
                        var branchTaxOpeningBalances = "";
                        if (data.branchTaxData[j].bnchTaxName != null && data.branchTaxData[j].bnchTaxName != "") {
                            branchInputTaxName = data.branchTaxData[j].bnchTaxName;
                        }
                        if (data.branchTaxData[j].bnchTaxRates != null && data.branchTaxData[j].bnchTaxRates != "") {
                            branchInputTaxRates = data.branchTaxData[j].bnchTaxRates;
                        }
                        if (data.branchTaxData[j].bnchTaxOpeningBal != null && data.branchTaxData[j].bnchTaxOpeningBal != "") {
                            branchTaxOpeningBalances = data.branchTaxData[j].bnchTaxOpeningBal;
                        }

                        if (inputTaxCounter == 0) {
                            $("#branchInputTaxTable tbody tr[id='copyBranchInputTax'] input[id='banchInputTaxHidId']").val(inputTaxPrimId);
                            $("#branchInputTaxTable tbody tr[id='copyBranchInputTax'] input[id='inputTaxName']").val(branchInputTaxName);
                            $("#branchInputTaxTable tbody tr[id='copyBranchInputTax'] input[id='inputTaxRate']").val(branchInputTaxRates);
                            $("#branchInputTaxTable tbody tr[id='copyBranchInputTax'] input[id='buyingTaxOpeningBalance']").val(branchTaxOpeningBalances);
                        }
                        if (inputTaxCounter > 0) {
                            $("#branchInputTaxTable tbody").append('<tr id="dynBranchInputTax' + j + '">' + branchInputTaxTr + '</tr>');
                            $("#branchInputTaxTable tbody tr[id='dynBranchInputTax" + j + "'] input[id='banchInputTaxHidId']").val(inputTaxPrimId);
                            $("#branchInputTaxTable tbody tr[id='dynBranchInputTax" + j + "'] input[id='inputTaxName']").val(branchInputTaxName);
                            $("#branchInputTaxTable tbody tr[id='dynBranchInputTax" + j + "'] input[id='inputTaxRate']").val(branchInputTaxRates);
                            $("#branchInputTaxTable tbody tr[id='dynBranchInputTax" + j + "'] input[id='buyingTaxOpeningBalance']").val(branchTaxOpeningBalances);
                        }
                        inputTaxCounter++;

                    } else {
                        var taxPrimId = data.branchTaxData[j].branchTaxHidIds;
                        var bnchTaxName = "";
                        var bnchTaxRates = "";
                        var branchTaxOpeningBalances = "";
                        if (data.branchTaxData[j].bnchTaxName != null && data.branchTaxData[j].bnchTaxName != "") {
                            bnchTaxName = data.branchTaxData[j].bnchTaxName;
                        }
                        if (data.branchTaxData[j].bnchTaxRates != null && data.branchTaxData[j].bnchTaxRates != "") {
                            bnchTaxRates = data.branchTaxData[j].bnchTaxRates;
                        }
                        if (data.branchTaxData[j].bnchTaxOpeningBal != null && data.branchTaxData[j].bnchTaxOpeningBal != "") {
                            branchTaxOpeningBalances = data.branchTaxData[j].bnchTaxOpeningBal;
                        }

                        if (outputTaxCounter == 0) {
                            $("#branchTaxTable tbody tr[id='copyBranchTax'] input[id='banchTaxHidId']").val(taxPrimId);
                            $("#branchTaxTable tbody tr[id='copyBranchTax'] input[id='taxName']").val(bnchTaxName);
                            $("#branchTaxTable tbody tr[id='copyBranchTax'] input[id='taxRate']").val(bnchTaxRates);
                            $("#branchTaxTable tbody tr[id='copyBranchTax'] input[id='salesTaxOpeningBalance']").val(branchTaxOpeningBalances);
                        }
                        if (outputTaxCounter > 0) {
                            $("#branchTaxTable tbody").append('<tr id="dynBranchTax' + j + '">' + branchTaxTr + '</tr>');
                            $("#branchTaxTable tbody tr[id='dynBranchTax" + j + "'] input[id='banchTaxHidId']").val(taxPrimId);
                            $("#branchTaxTable tbody tr[id='dynBranchTax" + j + "'] input[id='taxName']").val(bnchTaxName);
                            $("#branchTaxTable tbody tr[id='dynBranchTax" + j + "'] input[id='taxRate']").val(bnchTaxRates);
                            $("#branchTaxTable tbody tr[id='dynBranchTax" + j + "'] input[id='salesTaxOpeningBalance']").val(branchTaxOpeningBalances);
                        }
                        outputTaxCounter++;
                    }
                }
                repopulateBranchUsers();
                $('#assignBranchAdmin').find('option[value="' + data.branchAdmin + '"]').prop('selected', true);
                $('.notify-success').hide();
                $("." + detailForm + "").slideDown('slow');
                $('.bnchButtonDiv').fadeIn();
            }
            alwaysScrollTop();
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        }
    });
}

function checkOpeningBalance(element, hiddenElement) {
    var hiddenName = $(hiddenElement).attr('name');
    var hidOpeningBalance = $(element).closest("tr").find('input[id="' + hiddenName + '"]').val();
    if (hiddenName == "pettyCashOpeningBalanceHid") {
        var type = "PettyCash";
        var id = $(element).closest("tr").find('input[id="bnchkeyDephiddenId"]').val();
        return checkAccountInvolmentInTrans(element, type, id);
    } else if (hiddenName == "cashOpeningBalanceHid") {
        var type = "Cash";
        var id = $(element).closest("tr").find('input[id="bnchkeyDephiddenId"]').val();
        return checkAccountInvolmentInTrans(element, type, id);
    } else if (hiddenName == "bankOpeningBalanceHid") {
        var type = "Bank";
        var id = $(element).closest("tr").find('input[id="bnchBnkActhiddenId"]').val();
        return checkAccountInvolmentInTrans(element, type, id);
    }

}
// Bank Setup is Removed from Branch Setup
function checkAccountInvolmentInTrans(elem, type, accountId) {
    var jsonData = {};
    jsonData.useremail = $("#hiddenuseremail").text();
    jsonData.branchId = $("#branchEntityHiddenId").val();
    jsonData.type = type;
    jsonData.accountId = accountId;
    var url = "/config/verifyaccountforopenbal";
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        method: "POST",
        contentType: 'application/json',
        success: function (data) {
            if (data.status == true) {
                $(elem).blur();
                if (type == "PettyCash") {
                    swal("Error!","Petty Cash Opening Balance cannot be altered. Please pass journal entry in case of any adjustments","error");
                } else if (type == "Cash") {
                    swal("Error!","Cash Opening Balance cannot be altered. Please pass journal entry in case of any adjustments","error");
                } else if (type == "Bank") {
                    swal("Error!","Bank Opening Balance cannot be altered.Please pass journal entry in case of any adjustments","error");
                }
                return true;
            } else {
                return false;
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        }
    });
}


var getbranchadministratordatas = function (data) {
    if (data) {
        $('.bnchButtonDiv').append('<button id="formCancel" class="formCancel btn btn-cancel btn-idos" title="Cancel" onClick="formCancel();">Cancel</button></br>');
        $("#periodicityOfPayment").children().remove();
        $("#periodicityOfPayment").append('<option value="" selected>--Select--</option>' +
            '<option value="1">Weekly</option>' +
            '<option value="2">Monthly</option>' +
            '<option value="3">Quarterly</option>' +
            '<option value="4">Half Yearly</option>' +
            '<option value="5">Annually</option>' +
            '<option value="6">Once In 2 Years</option>' +
            '<option value="7">Once In 3 years</option>' +
            '<option value="8">One Time</option>');
        $("#recurrences").html($("#periodicityOfPayment").html());
        $("#branchPremiseTable tr[id='branchPremiseType'] td:nth-child(8)").append('<br/>' +
            '<select class="alertDropDown" name="bnchPremisealertForAction" id="bnchPremisealertForAction">' +
            '<option value="">--Please Select--</option>' +
            '</select><br/><select style="margin-top:10px;" class="alertDropDown" name="bnchPremisealertForInformation" id="bnchPremisealertForInformation">' +
            '<option value="">--Please Select--</option>' +
            '</select>');
        $("#branchTable tbody").html('');
        var branchlisttable = $("#branchTable");
        var currencysel = $('.currencyDropDown');
        var countrysel = $(".countryDropDown");
        var countryPhnCodesel = $(".countryPhnCode");
        var bnkActTypeSel = $("#bnkActType");
        var countryName = "";
        $("#orgLabel").html('');
        var orgsel = $("#orgLabel");
        for (var i = 0; i < data.organizationData.length; ++i) {
            var orgId = data.organizationData[i].id;
            var orgName = data.organizationData[i].name;
            var corporateEmail = data.organizationData[i].corporateEmail;
            orgsel.append('<li id="org' + orgId + '" class="orgclass"><a id="org' + orgId + '" title="Organization Label" href="#branchSetup" class="filter-item color-label labelstyle-fc2929 selected"><input style="display:none" type="checkbox" id="' + orgId + '" value="' + orgId + '"><span class="color"></span><span>' + orgName + '</span></a></li>')
        }
        for (var i = 0; i < data.currencyData.length; ++i) {
            currencysel.append('<option value="' + data.currencyData[i].id + '">' + data.currencyData[i].name + '</option>');
            countrysel.append('<option value="' + data.currencyData[i].countryId + '">' + data.currencyData[i].countryName + '</option>');
        }
        for (var i = 0; i < data.phoneCodeData.length; ++i) {
            countryPhnCodesel.append('<option value="' + data.phoneCodeData[i].id + '">' + data.phoneCodeData[i].name + '</option>');
        }
        for (var i = 0; i < data.bnkactTypean.length; ++i) {
            bnkActTypeSel.append('<option value="' + data.bnkactTypean[i].id + '">' + data.bnkactTypean[i].name + '</option>');
        }
        if (data.branchListData[0].country != "") {
            countryName = $("#branchCountry").find('option[value=' + data.branchListData[0].country + ']').text();
        }
        branchlisttable.append('<tr name="branchEntity' + data.branchListData[0].id + '"><td>' + data.branchListData[0].name + '</td><td>' + countryName + '</td><td>' + (data.branchListData[0].location == null ? "" : data.branchListData[0].location) + '</td><td>' + (data.branchListData[0].phone == null ? "" : data.branchListData[0].phone) + '</td><td><div class="search btn-align-right edit-right"><div id="search-launch" style="display: block;"> <a href="#branchSetup" class="button small search-open btn-idos-flat-white fs-16" onClick="showBranchEntityDetails(this)" id="show-entity-details' + data.branchListData[0].id + '"><i class="fa fa-edit fa-lg pr-5"></i>Edit</a></div></div></td><td><div class="search btn-align-right edit-right"><div id="search-launch" style="display: block;"><a href="#branchSetup" class="button small search-open btn-idos-flat-white fs-16" onClick="deactivateBranchEntityDetails(this)" id="deactivate-entity-details' + data.branchListData[0].id + '"><i class="far fa-trash-alt fa-lg pr-5"></i>' + data.branchListData[0].actionText + '</a></div></div></td></tr>');

        //console.log('<tr name="branchEntity'+data.branchListData[0].id+'"><td>'+data.branchListData[0].name+'</td><td>'+countryName+'</td><td>'+(data.branchListData[0].location==null?"":data.branchListData[0].location)+'</td><td>'+(data.branchListData[0].phone==null?"":data.branchListData[0].phone)+'</td><td><div class="search btn-align-right edit-right"><div id="search-launch" style="display: block;"> <a href="#branchSetup" class="button small search-open btn-idos-flat-white fs-16" onClick="showBranchEntityDetails(this)" id="show-entity-details'+data.branchListData[0].id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</a></div></div></td><td><div class="search btn-align-right edit-right"><div id="search-launch" style="display: block;"><a href="#branchSetup" class="button small search-open btn-idos-flat-white fs-16" onClick="deactivateBranchEntityDetails(this)" id="deactivate-entity-details'+data.branchListData[0].id+'"><i class="far fa-trash-alt fa-lg pr-5"></i>'+data.branchListData[0].actionText+'</a></div></div></td></tr>');
    }
}

function populateAlersDropDown() {
    $(".alertDropDown").each(function () {
        var selectedItem = $(this).find('option:selected').val();
        var selectedItemFullText = $(this).find('option:selected').text();
        var startbrac = parseInt(selectedItemFullText.indexOf("("));
        var endbrac = parseInt(selectedItemFullText.indexOf(")"));
        var selectedItemName = selectedItemFullText.substring(startbrac, selectedItemFullText.length);
        var val = "--Please Select--";
        $(this).children().remove();
        $(this).append('<option value="">' + val + '</option>');
        var $alertBox = $(this);
        $("#branchOfficersTable tbody input[name='keyoffName']").each(function () {
            var name = $(this).val();
            name = escapeHtml(name);
            var trName = $(this).parent().parent("tr:first").attr('id');
            var email = $("#" + trName + " input[id='keyoffEmailId']").val();
            var text = email + "(" + name + ")";
            if (name != "" || email != "") {
                if (email == selectedItem || selectedItemName == "(" + name + ")") {
                    if (email != "" && selectedItem != "") {
                        $($alertBox).append('<option value="' + email + '" selected="selected">' + text + '</option>');
                    } else {
                        $($alertBox).append('<option value="' + email + '">' + text + '</option>');
                    }
                } else {
                    $($alertBox).append('<option value="' + email + '">' + text + '</option>');
                }
            }
        });
        listAlertHqUser($alertBox)
    });
}

function listAlertHqUser($alertBox) {
    var jsonData = {};
    jsonData.useremail = $("#hiddenuseremail").text();
    var url = "/config/getHqAlertUser";
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        method: "POST",
        contentType: 'application/json',
        success: function (data) {
            for (var i = 0; i < data.alertHqUser.length; i++) {
                var name = data.alertHqUser[i].name;
                var email = data.alertHqUser[i].email
                var text = email + "(" + name + ")";
                var exist = $($alertBox).find('option[value="' + data.alertHqUser[i].email + '"]').attr('value');
                if (typeof (exist) == 'undefined') {
                    $($alertBox).append('<option value="' + data.alertHqUser[i].email + '">' + text + '</option>');
                }
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        }
    });
}

/* Sunil: Not needed
$(document).ready(function(){
    $('.updateBranchStatutoryValidty'). click(function(){
        var alertUserEmail=GetURLParameter('alertUserEmail');
        var orgEntity=GetURLParameter('orgEntity');
        var branchEntity=GetURLParameter('branchEntity');
        var taskAlertGroupingDate=GetURLParameter('taskAlertGroupingDate');
        var branchStatutoryEntity=GetURLParameter('branchStatutoryEntity');
        var validityFrom=$("#bnchStatutoryDiv input[id='bnchStatutoryAlertvalidFrom']").val();
        var validityTo=$("#bnchStatutoryDiv input[id='bnchStatutoryAlertvalidTo']").val();
        if(validityFrom=="" || validityTo==""){
            alert("Please provide Proper Branch Statutory Validity dates");
            return false;
        }
        var suppDocUrl=$("#bnchStatutoryDiv input[name='bnchStatutoryAlertSupportingDoc']").val();
        var jsonData = {};
        jsonData.userEmail=alertUserEmail;
        jsonData.organizationEntityId = orgEntity;
        jsonData.branchEntityId=branchEntity;
        jsonData.branchStatutoryEntityId=branchStatutoryEntity;
        jsonData.branchStatutoryValidityFrom=validityFrom;
        jsonData.branchStatutoryValidityTo=validityTo;
        jsonData.branchStatutorySuppDocUrl=suppDocUrl;
        jsonData.branchtaskAlertGroupingDate=taskAlertGroupingDate;
        var url="/branch/updateBranchStatutoryValidity";
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
*/


$(document).ready(function () {
    $('.bfh-states').bfhstates({country: 'IN', blank: false});

    $(".gstinInputCls").keyup(function () {
        if (this.value.length === this.maxLength) {
            $(this).next('.gstinInputCls').focus();
        }
    });

});
var stateArray = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '29', '30', '31', '32', '33', '34', '35', '36', '37'];

var validateGSTINStateCode = function (code) {
    if (stateArray.indexOf(code) === -1) {
        return false;
    }
    return true;
}

var validateGstinState = function (elem) {
    var parentTbl = $(elem).closest('table').attr('id');
    var currValue = $(elem).val();
    var gstinPart2 = $("#" + parentTbl + " input[name='gstinPart2']").val();
    var status = true;
    if (currValue !== "") {
        status = validateGSTINStateCode(currValue);
    }
    if (status == false) {
        swal("Invalid state!", "provide valid Indian state code in GSTIN", "error");
        $(elem).val('');
        $(elem).focus();
        $("#" + parentTbl + " .bfh-states option").filter(function () {
            return $(this).val() == '';
        }).prop("selected", "selected");
        return false;
    } else {
        $("#" + parentTbl + " .bfh-states option").filter(function () {
            return $(this).val() == currValue;
        }).prop("selected", "selected");
    }
}


function changeHiddenOpeningBalance(element, hiddenElement) {
    var hiddenName = $(hiddenElement).attr('name');
    var hidOpeningBalance = $(element).closest("tr").find('input[id="' + hiddenName + '"]').val();
    var value = $(element).val();
    $(element).closest("tr").find('input[id="' + hiddenName + '"]').val(value);
}

var refreshBranchOnSaveOrUpdate = function (branchid, branchName, branchGstin) {
    if (branchid != 'load') {
        ALL_BRANCH_OF_ORG_MAP[branchid] = '<option id="' + branchGstin + '" value="' + branchid + '">' + branchName + '</option>';
        $(".docuploadrulecustomdropdownBranchList").append('<li id="docuploadrulecustomdropdownBranchlist">&nbsp;&nbsp;<input type="checkbox" name="customdoccheckBranch" id="customdoccheckBranch" value="' + branchid + '" onclick="customdoccheckUncheck(this);"/>&nbsp;&nbsp;<input type="text" class="input-small" name="monetoryLimit" id="monetoryLimit' + branchid + '" value="0.0" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="customdoctoggleCheck(this)">&nbsp;&nbsp;' + branchName + '</li>');

        COA_INCOME_BRANCHES_MAP[branchid] = ('<li class="customerVendorBranchCls" id="' + branchid + '"><input type="checkbox" id="coaBranchCheck" class="itemBranch2Class-cb" value="' + branchid + '">' + branchName + '<b style="float:right;"><input type="text" id="incBranchItemOB" class="openingBalance" placeholder="Opening Balance" onkeypress="return onlyDotsAndNumbersandMinus(event, this);" onblur="validateCoaBranchData(this, ' + branchid + ', 1);"><input type="text" name="incomeItemDiscount" id="itemBranch2Class-cb' + branchid + '" class="txtBox80" onkeypress="return onlyDotsAndNumbersandMinus(event, this);" onblur="validateCoaBranchData(this, ' + branchid + ', 1);" placeholder="Discount %"></b></li>');

        COA_EXPENSE_BRANCHES_MAP[branchid] = ('<li class="customerVendorBranchCls" id="' + branchid + '"><input type="checkbox" id="coaBranchCheck" class="itemBranch2Class-cb" value="' + branchid + '"><span class="branchNameLabel">' + branchName + '</span><b style="float:right;"><input type="text" id="expBranchItemOB" class="openingBalance" placeholder="Opening Balance" onkeypress="return onlyDotsAndNumbersandMinus(event, this)" onblur="validateCoaBranchData(this, ' + branchid + ', 2);"><input type="text" id="noOfUnits" placeholder="No of Units" onkeypress="return onlyDotsAndNumbersandMinus(event, this)" onblur="validateCoaBranchData(this, ' + branchid + ',2);" class="txtBox80"><input type="text" id="inventoryRate" placeholder="Rate" onkeypress="return onlyDotsAndNumbersandMinus(event, this)" onblur="validateCoaBranchData(this, ' + branchid + ', 2);" class="txtBox80"><input type="text" id="inventoryValue" placeholder="Inventory Value" onkeypress="return onlyDotsAndNumbersandMinus(event, this);" onblur="validateCoaBranchData(this, ' + branchid + ', 2);" readonly></b></li>');

        COA_ASSET_LIAB_BRANCHES_MAP[branchid] = ('<li class="customerVendorBranchCls" id="' + branchid + '"><input type="checkbox" id="coaBranchCheck" class="itemBranch2Class-cb" value="' + branchid + '">' + branchName + '<b style="float:right;"><input type="text" id="astlibBranchItemOB" class="openingBalance" placeholder="Opening Balance" onkeypress="return onlyDotsAndNumbersandMinus(event, this);" onblur="validateCoaBranchData(this, ' + branchid + ', 34);"></b></li>');

        CUSTOMER_BRANCH_MAP[branchid] = '<li class="customerVendorBranchCls" id="' + branchid + '"><input type="checkbox" id="custBranchCheck" onclick="checkUncheckBranches(this,customerBranchDropdownBtn);" value="' + branchid + '">' + branchName + '<b style="float:right;"><input onkeypress="return onlyDotsAndNumbersandMinus(event, this);" type="text" id="custOpenBalance" class="openingBalance" placeholder="Opening balance" onblur="totalOpeningBalances(this,' + branchid + ', \'ob\');"><input onkeypress="return onlyDotsAndNumbersandMinus(event, this);" type="text" id="custOpenBalanceAdvPaid" class="openingBalanceAP" placeholder="Advance Paid Opening Balance" onblur="totalOpeningBalances(this,' + branchid + ', \'obap\');"></b></li>';

        VENDOR_BRANCH_MAP[branchid] = '<li class="customerVendorBranchCls" id="' + branchid + '"><input type="checkbox" id="vendorBranchCheck" onclick="checkUncheckBranches(this,vendorBranchDropdownBtn);" value="' + branchid + '">' + branchName + '<b style="float:right;"><input onkeypress="return onlyDotsAndNumbersandMinus(event, this);" type="text" id="vendorOpenBalance" class="openingBalance" placeholder="Opening balance" onblur="totalOpeningBalances(this,' + branchid + ', \'ob\');"><input onkeypress="return onlyDotsAndNumbersandMinus(event, this);" type="text" id="vendorOpenBalanceAdvPaid" class="openingBalanceAP" placeholder="Advance Paid Opening Balance" onblur="totalOpeningBalances(this,' + branchid + ', \'obap\');"></b></li>';
    }

    $("#userBranch").children().remove();
    $("#userBranch").append('<option value="">--Please Select--</option>');
    $("#inputTaxDiv #inputTaxBranch").children().remove();
    $("#inputTaxDiv #inputTaxBranch").append('<option value="">--Please Select--</option>');
    $("#tifbtbToBranch").children().remove();
    $("#tifbtbToBranch").append('<option value="">--Please Select--</option>');
    $("#projectPositionBranch").children().remove();
    $("#projectPositionBranch").append('<option value="">--Please Select--</option>');
    $("#customerBranchList").children().remove();
    $("#vendorBranchList").children().remove();
    $("#customerBranchList").append('<li class="customerVendorBranchCls"><input style="margin-bottom:5px;" type="checkbox" class="vendCustSelectAllCls" value="" onClick="checkUncheckAllVendCustBranches(this,\'customerBranchList\');">&nbsp;&nbsp;&nbsp;Select All</li>');
    $("#vendorBranchList").append('<li class="customerVendorBranchCls" style="display:flex; justify-content:space-evenly"><label>Branch</label><label style="margin-left:134px;">Opening Balance</label><label>Advance Paid Opening Balance</label></li><li class="customerVendorBranchCls"><input style="margin-bottom:5px;" type="checkbox" class="vendCustSelectAllCls" value="" onClick="checkUncheckAllVendCustBranches(this,\'vendorBranchList\');">&nbsp;&nbsp;&nbsp;Select All</li>');

    let tempArr = Object.values(ALL_BRANCH_OF_ORG_MAP);
    let branchlist = tempArr.join('');
    $("#userBranch").append(branchlist);
    $("#rcmTaxDiv #rcmTaxBranch").append(branchlist);
    $("#tifbtbToBranch").append(branchlist);
    $("#projectPositionBranch").append(branchlist);
    $("#inputTaxDiv #inputTaxBranch").append(branchlist);

    tempArr = Object.values(CUSTOMER_BRANCH_MAP);
    branchlist = tempArr.join('');
    $('#customerBranchList').append(branchlist);
    tempArr = Object.values(VENDOR_BRANCH_MAP);
    branchlist = tempArr.join('');
    $("#vendorBranchList").append(branchlist);
    $('.multiBranch').multiselect('rebuild');
    $('.multipleDropdown').multiselect('rebuild');
}
