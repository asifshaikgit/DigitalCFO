/**
 * Display trial balance similar to COA with opening bal and credit-debit and closing bal
 */

var PROFIT_LOSS_DATA = "";
Number.prototype.format = function (n, x) {
    let re = '\\d(?=(\\d{' + (x || 3) + '})+' + (n > 0 ? '\\.' : '$') + ')';
    return this.toFixed(Math.max(0, ~~n)).replace(new RegExp(re, 'g'), '$&,');
};

function escapeSingleDoubleQuotes(string) {
    return string.replace(/([\"\'])/g,'\\$&');
}

function showTrialBalance(elem) {
    let mouldesRights = $("#usermoduleshidden").val();
    showHideModuleTabs(mouldesRights);
    $("#pendingExpense").hide();
    $("#trialBalance").show();
    $("#bankBookDiv").hide();
    $("#cashBookDiv").hide();
    $('#periodicInventory').hide();
    $('#reportInventory').hide();
    $("#reportAllInventory").hide();
    $("#plbsCoaMapping").hide();
    $("#profitloss").hide();
    $("#balanceSheet").hide();

    $(".bnchCashnBankTrialBalance option:first").prop('selected', 'selected');
    $("#trialBalanceFromDate").val("");
    $("#trialBalanceToDate").val("");
    // displayTrialBalance();
    getBranchData();
    //getParticularsForTB();

    //$("#trialBalance #selectBranchTrialBalanceId").find('option[value=""]').text("Organization");
    //$("#trialBalance #selectBranchTrialBalanceId").attr('value', "").html('Organization');

    var $option = $('#trialBalance #selectBranchTrialBalanceId option:contains("--Please select--")');
    //$option.attr('value', 'New Value');
    $option.html('Organization');
}

function getParticularsForTB() {
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    let useremail = $("#hiddenuseremail").text();
    let jsonData = {};
    jsonData.usermail = useremail;
    let fromDate = $("#trialBalanceFromDate").val();
    let toDate = $("#trialBalanceToDate").val();
    jsonData.email = useremail;
    jsonData.trialBalanceFromDate = fromDate;
    jsonData.trialBalanceToDate = toDate;
    let url = "/config/getParticularsForOrg";
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
            if (data.partData.length > 0) {
                for (let i = 0; i < data.partData.length; i++) {
                    let openingBalanceTmp = data.partData[i].openingBalance;
                    let closingBalanceTmp = data.partData[i].closingBalance;
                    let creditAmtTmp = data.partData[i].credit;
                    let debitAmtTmp = data.partData[i].debit;
                    let accountCode = data.partData[i].accountCode;
                    $("li[id=" + accountCode + "]").remove();
                    $("#mainChartOfAccountTB").append('<li id=' + data.partData[i].accountCode + '><div class="chartOfAccountContainer"><img id="' + data.partData[i].accountCode + '" src="/assets/images/new.v1370889834.png" style="margin-top:-2px;" id="getNodeChild" onclick="javascript:getChildChartOfAccountTB(this, 0 );"></img><b style="margin-left:2px;">' + data.partData[i].name + '</b><b class="tree-inline-box-head">' + closingBalanceTmp + '</b><b class="tree-inline-box-head">' + creditAmtTmp + '</b><b class="tree-inline-box-head">' + debitAmtTmp + '</b><b class="tree-inline-box-head">' + openingBalanceTmp + '</b></div></li>');
                    $("#mainChartOfAccountTB li[id='" + data.partData[i].accountCode + "']").find("ul[id='mainChartOfAccountTB']").remove();
                    $("#mainChartOfAccountTB li[id='" + data.partData[i].accountCode + "']").find("img[id='" + data.partData[i].accountCode + "']").attr('src', "/assets/images/new.v1370889834.png");
                    $("#mainChartOfAccountTB li[id='" + data.partData[i].accountCode + "']").find("img[id='" + data.partData[i].accountCode + "']").attr('onclick', "javascript:getChildChartOfAccountTB(this,0)");
                }
                PROFIT_LOSS_DATA = data.coaSpecfChildData;
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }else if(xhr.status == 500){
                swal("Error on fetching Trial Balance!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });
}


function getChildChartOfAccountTB(elem, identForDataValid) {
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    let useremail = $("#hiddenuseremail").text();
    let accountCode = $(elem).attr('id');
    let trialBalBrach = $("#selectBranchTrialBalanceId option:selected").val();
    let fromDate = $("#trialBalanceFromDate").val();
    let toDate = $("#trialBalanceToDate").val();
    let jsonData = {};
    jsonData.email = useremail;
    jsonData.coaAccountCode = accountCode;
    jsonData.identForDataValid = identForDataValid;
    jsonData.trialBalanceForBranch = trialBalBrach;
    jsonData.trialBalanceFromDate = fromDate;
    jsonData.trialBalanceToDate = toDate;
    let url = "/trialBalance/display";
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
            if (data.coaSpecfChildData.length > 0) {
                $("li[id=" + accountCode + "]").append('<ul id="mainChartOfAccountTB" class="treeview-black mainChartOfAccountTB"></ul>');
                for (let i = 0; i < data.coaSpecfChildData.length; i++) {
                    let topLevelAccountCode = data.coaSpecfChildData[i].topLevelAccountCode;
                    let specificID = data.coaSpecfChildData[i].specId;
                    let headid2 = data.coaSpecfChildData[i].headid2;
                    let accountNameTmp = data.coaSpecfChildData[i].accountName;
                    let escapedAccountNameTmp = escapeSingleDoubleQuotes(accountNameTmp);
                    let identificationForDataValidTmp = data.coaSpecfChildData[i].identificationForDataValid;
                    let openingBalanceTmp = data.coaSpecfChildData[i].openingBalance;
                    let closingBalanceTmp = data.coaSpecfChildData[i].closingBalance;
                    let creditAmtTmp = data.coaSpecfChildData[i].credit;
                    let debitAmtTmp = data.coaSpecfChildData[i].debit;
                    let headType = data.coaSpecfChildData[i].headType;
                    $("li[id=" + accountCode + "]").find("ul[id='mainChartOfAccountTB']").append('<li id=' + data.coaSpecfChildData[i].specfaccountCode + '><div class="chartOfAccountContainer"><img id=\'' + data.coaSpecfChildData[i].specfaccountCode + '\' src="/assets/images/new.v1370889834.png" style="margin-top:-2px;float: left;margin-top: 5px;" id="getNodeChild" onclick="getChildChartOfAccountTB(this,\'' + identificationForDataValidTmp + '\');"></img><b style="margin-left:2px; word-wrap: break-word;"><span style="width: 350px;display: inline-block;white-space: nowrap;overflow-x: auto;" ><a id="show-entity-details' + data.coaSpecfChildData[i].specId + '" href="#pendingExpense" onclick="showItemTransactionDetails(this,\'' + topLevelAccountCode + '\', \'' + specificID + '\',\'' + identificationForDataValidTmp + '\',\'' + openingBalanceTmp + '\',\'' + closingBalanceTmp + '\',\'' + creditAmtTmp + '\',\'' + debitAmtTmp + '\',\'' + escapedAccountNameTmp + '\',\'' + headType + '\',\'' + headid2 + '\');" class="color-grey">' + accountNameTmp + '</a></span></b><b class="tree-inline-box">' + closingBalanceTmp + '</b><b class="tree-inline-box">' + creditAmtTmp + '</b><b class="tree-inline-box">' + debitAmtTmp + '</b><b class="tree-inline-box">' + openingBalanceTmp + '</b></div></li>');
                }
                $(elem).attr("src", "assets/images/minus.png");
                $(elem).attr("onclick", 'javascript:removeCOATB(this,\'' + identForDataValid + '\');');
            } else {
                $(elem).attr("src", "assets/images/minus.png");
            }

            /*if(data.coaSpecfChildData.length>0){
                $("li[id="+accountCode+"]").append('<ul id="mainChartOfAccount" class="treeview-black mainChartOfAccount"></ul>');
                for(let i=0;i<data.coaSpecfChildData.length;i++){

                    let topLevelAccountCode=data.coaSpecfChildData[i].topLevelAccountCode;

                        $("li[id="+accountCode+"]").find("ul[id='mainChartOfAccount']").append('<li id='+data.coaSpecfChildData[i].specfaccountCode+'><div class="chartOfAccountContainer"><p class="color-grey"><img id="'+data.coaSpecfChildData[i].specfaccountCode+'" src="/assets/images/new.v1370889834.png" style="margin-top:-2px;" id="getNodeChild"></img><b style="margin-left:2px;"><a id="show-entity-details'+data.coaSpecfChildData[i].id+'" href="#itemSetUp" class="color-grey" onclick="showItemEntityDetails(this,0)">'+data.coaSpecfChildData[i].name+'</a></b></p></li>');
                    }else{
                        $("li[id="+accountCode+"]").find("ul[id='mainChartOfAccount']").append('<li id='+data.coaSpecfChildData[i].specfaccountCode+'><div class="chartOfAccountContainer"><p class="color-grey"><img id="'+data.coaSpecfChildData[i].specfaccountCode+'" src="/assets/images/new.v1370889834.png" style="margin-top:-2px;" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this,' + data.coaSpecfChildData[i].identificationForDataValid + ');"></img><b style="margin-left:2px;"><a id="show-entity-details'+data.coaSpecfChildData[i].id+'" href="#itemSetUp" class="color-grey" onclick="showItemEntityDetails(this,'+topLevelAccountCode+')">'+data.coaSpecfChildData[i].name+'</a></b><button style="float:right" id="newItemform-container" name="'+data.coaSpecfChildData[i].name+'" class="newEntityCreateButton btn btn-tree btn-idos" title="Create new Sub Account" onclick="javascript:createCOA(this,'+data.coaSpecfChildData[i].id+','+topLevelAccountCode+');"><i class="fa fa-plus"></i>Add Sub Account</button></p></li>');
                    }
                }
                $(elem).attr("src","assets/images/minus.png");
                $(elem).attr("onclick", 'javascript:removeCOA(this,' + identForDataValid + ');');
            }*/
            if(PROFIT_LOSS_DATA !== "" && accountCode == '4000000000000000000') {
                //$("li[id='4000000000000000000']").append('<ul id="mainChartOfAccountTB" class="treeview-black mainChartOfAccountTB"></ul>');
                for (let i = 0; i < PROFIT_LOSS_DATA.length; i++) {
                    let topLevelAccountCode = '4000000000000000000';
                    let specificID = PROFIT_LOSS_DATA[i].specId;
                    let headid2 = PROFIT_LOSS_DATA[i].headid2;
                    let accountNameTmp = PROFIT_LOSS_DATA[i].accountName;
                    let escapedAccountNameTmp = escapeSingleDoubleQuotes(accountNameTmp);
                    let identificationForDataValidTmp = PROFIT_LOSS_DATA[i].identificationForDataValid;
                    let openingBalanceTmp = PROFIT_LOSS_DATA[i].openingBalance;
                    let closingBalanceTmp = PROFIT_LOSS_DATA[i].closingBalance;
                    let creditAmtTmp = PROFIT_LOSS_DATA[i].credit;
                    let debitAmtTmp = PROFIT_LOSS_DATA[i].debit;
                    let headType = PROFIT_LOSS_DATA[i].headType;
                    $("li[id=" + accountCode + "]").find("ul[id='mainChartOfAccountTB']").append('<li id=' + PROFIT_LOSS_DATA[i].specfaccountCode + '><div class="chartOfAccountContainer"><img id=\'' + PROFIT_LOSS_DATA[i].specfaccountCode + '\' src="/assets/images/minus.png" style="margin-top:-2px;float: left;margin-top: 5px;" id="getNodeChild"></img><b style="margin-left:2px; word-wrap: break-word;"><span style="width: 350px;display: inline-block;white-space: nowrap;overflow-x: auto;" ><a id="show-entity-details' + PROFIT_LOSS_DATA[i].specId + '" href="#pendingExpense"  class="color-grey">' + accountNameTmp + '</a></span></b><b class="tree-inline-box">' + closingBalanceTmp + '</b><b class="tree-inline-box">' + creditAmtTmp + '</b><b class="tree-inline-box">' + debitAmtTmp + '</b><b class="tree-inline-box">' + openingBalanceTmp + '</b></div></li>');
                }
                //$(elem).attr("src", "assets/images/minus.png");
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }else if(xhr.status == 500){
                swal("Error on fetching Trial Balance!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });
}

function removeCOATB(elem, identForDataValid) {
    let accountCode = $(elem).attr('id');
    $("li[id=" + accountCode + "]").find("ul[id='mainChartOfAccountTB']").remove();
    $(elem).attr("src", "/assets/images/new.v1370889834.png");
    $(elem).attr("onclick", "javascript:getChildChartOfAccountTB(this," + identForDataValid + ");");
}


$(document).ready(function () {
    $(".searchTrialBalance").on('click', function () {
        let trialBalBrach = $("#selectBranchTrialBalanceId option:selected").val();
        let fromDate = $("#trialBalanceFromDate").val();
        let toDate = $("#trialBalanceToDate").val();
        if (fromDate == "" || typeof fromDate == 'undefined' || toDate == "" || typeof toDate == 'undefined') {
            swal("Error!","Please Choose Appropriate Date Range For Searching Trial Balance.","error");
            return true;
        }
        if (fromDate != "" && toDate != "") {
            $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
            let useremail = $("#hiddenuseremail").text();
            let fromDate = $("#trialBalanceFromDate").val();
            let toDate = $("#trialBalanceToDate").val();
            let jsonData = {};
            jsonData.email = useremail;
            jsonData.trialBalanceFromDate = fromDate;
            jsonData.trialBalanceToDate = toDate;
            jsonData.trialBalanceForBranch = trialBalBrach;
            let url = "/config/getParticularsForOrg";
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
                    if (data.partData.length > 0) {
                        for (let i = 0; i < data.partData.length; i++) {
                            let openingBalanceTmp = data.partData[i].openingBalance;
                            let closingBalanceTmp = data.partData[i].closingBalance;
                            let creditAmtTmp = data.partData[i].credit;
                            let debitAmtTmp = data.partData[i].debit;
                            accountCode = data.partData[i].accountCode;
                            $("li[id=" + accountCode + "]").remove();
                            //$("#mainChartOfAccountTB").append('<li id='+data.partData[i].accountCode+'><div class="chartOfAccountContainer"><img id="'+data.partData[i].accountCode+'" src="/assets/images/new.v1370889834.png" style="margin-top:-2px;" id="getNodeChild" onclick="javascript:getChildChartOfAccountTB(this,0);"></img><font class="color-grey"><b style="margin-left:2px; word-wrap: break-word;"><a id="show-entity-details'+data.partData[i].id+'" class="color-grey" href="#itemSetUp" onClick="">'+data.partData[i].name+'</a></b></font></li>');
                            $("#mainChartOfAccountTB").append('<li id=' + data.partData[i].accountCode + '><div class="chartOfAccountContainer"><img id="' + data.partData[i].accountCode + '" src="/assets/images/new.v1370889834.png" style="margin-top:-2px;" id="getNodeChild" onclick="javascript:getChildChartOfAccountTB(this, 0 );"></img><b onclick="showTransactionForParticulars(this,\'' + accountCode + '\',\'' + openingBalanceTmp + '\',\'' + closingBalanceTmp + '\',\'' + creditAmtTmp + '\',\'' + debitAmtTmp + '\',\'' + data.partData[i].name + '\');" style="margin-left:2px; cursor: pointer;">' + data.partData[i].name + '</b><b class="tree-inline-box-head">' + closingBalanceTmp + '</b><b class="tree-inline-box-head">' + creditAmtTmp + '</b><b class="tree-inline-box-head">' + debitAmtTmp + '</b><b class="tree-inline-box-head">' + openingBalanceTmp + '</b></div></li>');

                            $("#mainChartOfAccountTB li[id='" + data.partData[i].accountCode + "']").find("ul[id='mainChartOfAccountTB']").remove();
                            $("#mainChartOfAccountTB li[id='" + data.partData[i].accountCode + "']").find("img[id='" + data.partData[i].accountCode + "']").attr('src', "/assets/images/new.v1370889834.png");
                            $("#mainChartOfAccountTB li[id='" + data.partData[i].accountCode + "']").find("img[id='" + data.partData[i].accountCode + "']").attr('onclick', "javascript:getChildChartOfAccountTB(this,0)");
                            /*if(data.partData[i].name == "Incomes"){ //Expand only uppermost tree
                                showSearchedTrialBalance(accountCode,trialBalBrach,fromDate,toDate,0);
                            }*/
                        }
                        PROFIT_LOSS_DATA = data.coaSpecfChildData;
                    }
                },
                error: function (xhr, status, error) {
                    if (xhr.status == 401) {
                        doLogout();
                    }else if(xhr.status == 500){
                        swal("Error on fetching Trial Balance!", "Please retry, if problem persists contact support team", "error");
                    }
                },
                complete: function (data) {
                    $.unblockUI();
                }
            });
        }
    });
});

function showSearchedTrialBalance(accountCode, trialBalBrach, fromDate, toDate, identForDataValid) {
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    let jsonData = {};
    jsonData.email = $("#hiddenuseremail").text();
    jsonData.trialBalanceForBranch = trialBalBrach;
    jsonData.trialBalanceFromDate = fromDate;
    jsonData.trialBalanceToDate = toDate;
    jsonData.identForDataValid = identForDataValid;
    jsonData.coaAccountCode = accountCode;
    let url = "/trialBalance/display";
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
            if (data.coaSpecfChildData.length > 0) {
                $("li[id=" + accountCode + "]").append('<ul id="mainChartOfAccountTB" class="treeview-black mainChartOfAccountTB"></ul>');
                for (let i = 0; i < data.coaSpecfChildData.length; i++) {
                    let topLevelAccountCode = data.coaSpecfChildData[i].topLevelAccountCode;
                    let specificID = data.coaSpecfChildData[i].specId;
                    let headid2 = data.coaSpecfChildData[i].headid2;
                    let accountNameTmp = data.coaSpecfChildData[i].accountName;
                    let escapedAccountNameTmp = escapeSingleDoubleQuotes(accountNameTmp);
                    let identificationForDataValidTmp = data.coaSpecfChildData[i].identificationForDataValid;
                    let openingBalanceTmp = data.coaSpecfChildData[i].openingBalance;
                    let closingBalanceTmp = data.coaSpecfChildData[i].closingBalance;
                    let creditAmtTmp = data.coaSpecfChildData[i].credit;
                    let debitAmtTmp = data.coaSpecfChildData[i].debit;
                    let headType = data.coaSpecfChildData[i].headType;
                    //$("li[id="+accountCode+"]").find("ul[id='mainChartOfAccountTB']").append('<li id='+data.coaSpecfChildData[i].specfaccountCode+'><div class="chartOfAccountContainer"><img id="'+data.coaSpecfChildData[i].specfaccountCode+'" src="/assets/images/new.v1370889834.png" style="margin-top:-2px;" id="getNodeChild" onclick="getChildChartOfAccountTB(this,' + data.coaSpecfChildData[i].identificationForDataValid + ');"></img><b style="margin-left:2px;">'+data.coaSpecfChildData[i].accountName+'</b><b style="position: absolute; left: 250px; top:2px">'+data.coaSpecfChildData[i].openingBalance+'</b><b style="position: absolute; left: 450px; top:2px">'+data.coaSpecfChildData[i].debit+'</b><b style="position: absolute; left: 350px; top:2px">'+data.coaSpecfChildData[i].credit+'</b><b style="position: absolute; left: 550px; top:2px">'+data.coaSpecfChildData[i].closingBalance+'</b></li>');

                    $("li[id=" + accountCode + "]").find("ul[id='mainChartOfAccountTB']").append('<li id=' + data.coaSpecfChildData[i].specfaccountCode + '><div class="chartOfAccountContainer"><img id=\'' + data.coaSpecfChildData[i].specfaccountCode + '\' src="/assets/images/new.v1370889834.png" style="margin-top:-2px;" id="getNodeChild" onclick="getChildChartOfAccountTB(this,\'' + data.coaSpecfChildData[i].identificationForDataValid + '\');"></img><b style="margin-left:2px; word-wrap: break-word;"><a id="show-entity-details' + data.coaSpecfChildData[i].id + '" href="#pendingExpense" onclick="showItemTransactionDetails(this,\'' + topLevelAccountCode + '\', \'' + specificID + '\',\'' + identificationForDataValidTmp + '\',\'' + openingBalanceTmp + '\',\'' + closingBalanceTmp + '\',\'' + creditAmtTmp + '\',\'' + debitAmtTmp + '\',\'' + escapedAccountNameTmp + '\',\'' + headType + '\',\'' + headid2 + '\');" class="color-grey">' + accountNameTmp + '</a></b><b class="tree-inline-box">' + data.coaSpecfChildData[i].closingBalance + '</b><b class="tree-inline-box">' + data.coaSpecfChildData[i].credit + '</b><b class="tree-inline-box">' + data.coaSpecfChildData[i].debit + '</b><b class="tree-inline-box">' + data.coaSpecfChildData[i].openingBalance + '</b></div></li>');

                }
                $("#mainChartOfAccountTB li[id='" + accountCode + "']").find("img[id='" + accountCode + "']").attr('src', "assets/images/minus.png");
                $("#mainChartOfAccountTB li[id='" + accountCode + "']").find("img[id='" + accountCode + "']").attr("onclick", 'javascript:removeCOATB(this,' + identForDataValid + ');');
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }else if(xhr.status == 500){
                swal("Error on fetching Trial Balance!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });
}

/*
function displayTrialBalance(){
	let tbBranch=$("#selectBranchTrialBalanceId option:selected").val();
	let tbFromDate=$("#trialBalanceFromDate").val();
	let tbToDate=$("#trialBalanceToDate").val();
	trialBalanceBranch=tbBranch;
	trialBalanceFromDate=tbFromDate;
	trialBalanceToDate=tbToDate;
	let jsonData={};
	jsonData.email =$("#hiddenuseremail").text();
	jsonData.trialBalanceForBranch=tbBranch;
	jsonData.trialBalanceFromDate=tbFromDate;
	jsonData.trialBalanceToDate=tbToDate;
	ajaxCall('/trialBalance/display', jsonData, '', '', '', '', 'trialBalanceDisplaySuccess', '', true);
}*/

/*
function trialBalanceDisplaySuccess(data){
	$("#trialBalanceTableTable tbody").html("");
	if(data.result){
		for(let i=0;i<data.trialBalanceData.length;i++){
			$("#trialBalanceTableTable tbody").append('<tr id="trialBalanceDataTable"'+i+'><td><a href="#pendingExpense" onclick="">'+data.trialBalanceData[i].accountName+'</a></td><td>'+data.trialBalanceData[i].openingBalance+'</td><td>'+data.trialBalanceData[i].debit+'</td><td>'+data.trialBalanceData[i].credit+'</td><td>'+data.trialBalanceData[i].closingBalance+'</td></tr>')
		}
		$("#selectBranchTrialBalanceId option:first").prop("selected",'selected');
		$("#trialBalanceFromDate").val("");
		$("#trialBalanceToDate").val("");
	}
}*/

function showItemTransactionDetails(elem, toplevelaccountcode, specificid, identForDataValid, openingBalance, closingBalance, creditAmt, debitAmt, accountNameTmp, headType, headid2) {
    if (specificid == "0") { //Sunil: when child is from other than specifics table.
        swal("Operation not allowed","This operation is not allowed for the item.","error");
        return false;
    }
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    $("#itemsTransactionsTb").attr('data-toggle', 'modal');

    $("#itemsTransactionsTb div[class='modal-body']").html("");
    let trialBalBranch = $("#selectBranchTrialBalanceId option:selected").val();
    let fromDate = $("#trialBalanceFromDate").val();
    let toDate = $("#trialBalanceToDate").val();
    let jsonData = {};
    jsonData.specificid = specificid;
    jsonData.headid2 = headid2;
    jsonData.identForDataValid = identForDataValid;
    jsonData.toplevelaccountcode = toplevelaccountcode;
    jsonData.fromDate = fromDate;
    jsonData.toDate = toDate;
    jsonData.headType = headType;
    jsonData.trialBalBranch = trialBalBranch;
    let url = "/ledger/itemtransactions";
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
            $("#itemsTransactionsTb").modal('show');
            $("#ledgerTranDiv").html("");
            $("#ledgerTranDiv").append('<input type="hidden" id="specificidTmp" value="' + specificid + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="identForDataValidTmp" value="' + identForDataValid + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="toplevelaccountcodeTmp" value="' + toplevelaccountcode + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="fromDateTmp" value="' + fromDate + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="toDateTmp" value="' + toDate + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="headTypeTmp" value="' + headType + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="accountNameTmp" value="' + accountNameTmp + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="openingBalanceTmp" value="' + openingBalance + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="debitAmtTmp" value="' + debitAmt + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="creditAmtTmp" value="' + creditAmt + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="closingBalanceTmp" value="' + closingBalance + '">');
            $("#itemsTransactionsTb div[class='modal-body']").append('<div style="padding: 0px; margin: 0px;"><div style="float: left; width: 70%; max-width: 70%; text-align:left; padding: 0px; margin: 0px; overflow: auto;"><b>Ledger Name:</b> ' + accountNameTmp + '</div><div style="float: right; width: 30%; text-align:right; padding: 0px; margin: 0px;"><b>Period: </b>' + data.period + '</div></div><br><div style="width:99.9%; float:right; padding: 0px; margin: 0px;"><b>Opening Balance: </b>' + Number(openingBalance).format(2) + ' <b>&nbsp;&nbsp;&nbsp;&nbsp;Total Debit: </b>' + Number(debitAmt).format(2) + '<b>&nbsp;&nbsp;&nbsp;&nbsp;Total Credit: </b>' + Number(creditAmt).format(2) + ' <b>&nbsp;&nbsp;&nbsp;&nbsp;Closing Balance: </b>' + Number(closingBalance).format(2) + '</div>');

            $("#itemsTransactionsTb div[class='modal-body']").append('<div style="margin:0px; padding:0px;  height:50%; max-height: 420px; width: 100%; overflow: scroll;"><table id="itemTransactionTable" class="table table-hover table-striped table-bordered" style="margin-top: 2px;"><thead class="tablehead1"><tr><th>TRANSACTION REF.</th><th>DATE</th><th>USER</th><th>BRANCH</th><th>PROJECT</th><th>TRANSACTION TYPE</th><th>TYPE OF SUPPLY</th><th>CUSTOMER/ VENDOR</th><th>PLACE OF SUPPLY</th></tr></thead><tbody></tbody></table></div>');
            if ((headType == 'item' || headType == "") && toplevelaccountcode == "1000000000000000000") {
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").append('<th>INV NO.</th><th>HSN/SAC</th><th>PO REF.</th>');
            } else if (headType == 'cust' && toplevelaccountcode == "4000000000000000000") { //cAdv
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").append('<th>INV REF NO.</th><th>REF NO.</th>');
            } else if ((headType == 'taxs' || headType == 'sgst' || headType == 'cgst' || headType == 'igst' || headType == 'cess' || headType == 'tds') && toplevelaccountcode == "4000000000000000000") {
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").append('<th>INV REF NO.</th><th>REF NO.</th><th>PO REF.</th>');
            } else if (headType == 'cust') {
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").append('<th>INV NO.</th><th>REF NO.</th><th>PO REF.</th>');
            } else if ((headType == 'item' || headType == "" || headType == "vend" || headType == "vAdv") || ((headType == 'taxs' || headType == 'sgst' || headType == 'cgst' || headType == 'igst' || headType == 'cess' || headType == 'tds') && toplevelaccountcode == "3000000000000000000")) {
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").append('<th>PURCHASE ORDER</th>');
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").append('<th>INV/REF DATE</th><th>INV/REF NO.</th><th>DC/GRN REF DATE</th><th>DC/GRN REF NO.</th><th>IMPORT REF DATE</th><th>IMPORT REF NO.</th>');
            } else if ((headType == 'pexp' && toplevelaccountcode == "2000000000000000000") || (headType == 'pded' && toplevelaccountcode == "4000000000000000000")) {
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").html("");
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").append('<th>TRANSACTION REF NO</th><th>DATE</th><th>USER</th><th>BRANCH</th><th>TRANSACTION</th>');
            }
            if ((headType == 'item' || headType == "") || ((headType == 'taxs' || headType == 'sgst' || headType == 'cgst' || headType == 'igst' || headType == 'cess' || headType == 'tds') && toplevelaccountcode != "4000000000000000000")) {
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").append('<th>TAX RATE</th><th>CESS RATE</th>');
            }

            $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").append('<th>PAY MODE</th><th>PARTICULARS</th><th>DEBIT</th><th>CREDIT</th><th>REMARKS</th>');
            let previousItemId = "";
            let tableHead = $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").clone();
            let tableHeadLength = $(tableHead).children('th').length;
            let tableHeadHtml = $(tableHead).html();
            tableHeadHtml = '<tr class="tbledgerSubHead">' + tableHeadHtml + '</tr>';

            for (let i = 0; i < data.itemTransData.length; i++) {
                let transactionRecords = "";
                let transactionRef = data.itemTransData[i].txnRef;
                if (i == 0) {
                    //$("#itemsTransactionsTb div[class='modal-body']").append('<div style="margin-left: 10px; text-align: left; font-size: 11px;" itemid="' + data.itemTransData[0].ledgerId + '">' + data.itemTransData[0].ledgerName + '</div>');
                    $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] tbody").append('<tr itemid="' + data.itemTransData[i].ledgerId + '" class="tbledgerSubHead"><td colspan="' + tableHeadLength + '"><div class="tbLedgerItemSubHeadDiv">' + data.itemTransData[i].ledgerName + '</div><div class="tbLedgerSubHeadDiv">Opening Balance: ' + Number(data.itemTransData[i].openingBalance).format(2) + '</div><div class="tbLedgerSubHeadDiv">Debit Amount: ' + Number(data.itemTransData[i].debitAmount).format(2) + '</div><div class="tbLedgerSubHeadDiv">Credit Amount: ' + Number(data.itemTransData[i].creditAmount).format(2) + '</div><div class="tbLedgerSubHeadDiv"> Closing Balance: ' + Number(data.itemTransData[i].closingBalance).format(2) + '</div></td></tr>');
                }
                console.log("--------" + previousItemId);
                if (previousItemId != "" && typeof data.itemTransData[i].ledgerId != 'undefined' && (data.itemTransData[i].ledgerId).indexOf(previousItemId) == -1) {
                    console.log(">>> " + data.itemTransData[i].ledgerId);
                    //$("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] tbody").append('<tr itemid="' + data.itemTransData[i].ledgerId + '" class="tbledgerSubHead"><td colspan="' + tableHeadLength + '">' + data.itemTransData[i].ledgerName + '</td></tr>');
                    //$("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable']").append(tableHeadHtml);
                    $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] tbody").append('<tr itemid="' + data.itemTransData[i].ledgerId + '" class="tbledgerSubHead"><td colspan="' + tableHeadLength + '"><div class="tbLedgerItemSubHeadDiv">' + data.itemTransData[i].ledgerName + '</div><div class="tbLedgerSubHeadDiv">Opening Balance: ' + Number(data.itemTransData[i].openingBalance).format(2) + '</div><div class="tbLedgerSubHeadDiv">Debit Amount: ' + Number(data.itemTransData[i].debitAmount).format(2) + '</div><div class="tbLedgerSubHeadDiv">Credit Amount: ' + Number(data.itemTransData[i].creditAmount).format(2) + '</div><div class="tbLedgerSubHeadDiv"> Closing Balance: ' + Number(data.itemTransData[i].closingBalance).format(2) + '</div></td></tr>');
                } else if (typeof data.itemTransData[i].ledgerId == 'undefined') {
                    console.log("#####" + data.itemTransData[i].itemName);
                }

                if (transactionRef.startsWith("PROVTXN")) {
                    let projectName = "";
                    if (data.itemTransData[i].debit == "" || data.itemTransData[i].debit == "0.00") {
                        projectName = data.itemTransData[i].creditProjectName;
                    } else {
                        projectName = data.itemTransData[i].debitProjectName;
                    }
                    transactionRecords += "<tr id='" + transactionRef + "' onclick='displayTransctionModal(this);' style='cursor: pointer;'><td>" + transactionRef + "</td><td>" + data.itemTransData[i].tranDate + "</td><td>" + data.itemTransData[i].email + "</td><td>" + data.itemTransData[i].branchName + "</td><td>" + projectName + "</td><td>" + data.itemTransData[i].transactionPurpose + "</td><td>" + data.itemTransData[i].typeOfSupply + "</td><td>" + data.itemTransData[i].custVendName + "</td><td>" + data.itemTransData[i].placeOfSupply + "</td>";
                    if ((headType == 'item' || headType == "") && toplevelaccountcode == "1000000000000000000") {
                        transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td>" + data.itemTransData[i].hsnSac + "</td><td>" + data.itemTransData[i].poRef + "</td>";
                    } else if (headType == 'cust' && toplevelaccountcode == "4000000000000000000") {
                        transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td>" + data.itemTransData[i].refNo + "</td>";
                    } else if ((headType == 'taxs' || headType == 'sgst' || headType == 'cgst' || headType == 'igst' || headType == 'cess' || headType == 'tds') && toplevelaccountcode == "4000000000000000000") {
                        transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td>" + data.itemTransData[i].refNo + "</td><td>" + data.itemTransData[i].poRef + "</td>";
                    } else if (headType == 'cust') {
                        transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td>" + data.itemTransData[i].refNo + "</td><td>" + data.itemTransData[i].poRef + "</td>";
                    } else if ((headType == 'item' || headType == "" || headType == "vend" || headType == "vAdv") || ((headType == 'taxs' || headType == 'sgst' || headType == 'cgst' || headType == 'igst' || headType == 'cess' || headType == 'tds') && toplevelaccountcode == "3000000000000000000")) {
                        transactionRecords += "<td>" + data.itemTransData[i].poRef + "</td>";
                        transactionRecords += "<td>" + data.itemTransData[i].invoiceDate + "</td><td>" + data.itemTransData[i].invoiceNo + "</td><td>" + data.itemTransData[i].grnDate + "</td><td>" + data.itemTransData[i].grnNo + "</td><td>" + data.itemTransData[i].impDate + "</td><td>" + data.itemTransData[i].impNo + "</td>";
                    }
                    if ((headType == 'item' || headType == "") || ((headType == 'taxs' || headType == 'sgst' || headType == 'cgst' || headType == 'igst' || headType == 'cess' || headType == 'tds') && toplevelaccountcode != "4000000000000000000")) {
                        transactionRecords += "<td>" + data.itemTransData[i].taxRate + "</td><td>" + data.itemTransData[i].cessRate + "</td>";
                    }

                    transactionRecords += "<td>" + data.itemTransData[i].paymode + "</td><td><b>Debit: </b><div class='tbLedgerDivCls' >" + data.itemTransData[i].itemName + " </div><br><hr><b>Credit: </b>" + data.itemTransData[i].creditItemsName + "</td><td style='text-align: right;'>" + Number(data.itemTransData[i].debit).format(2) + "</td><td style='text-align: right;'>" + Number(data.itemTransData[i].credit).format(2) + "</td><td style='max-width: 120px; word-wrap: break-word;'>" + data.itemTransData[i].remarks + "</td></tr>";
                } else if (transactionRef.startsWith("PRTXN")) {
                    if (headType == 'pexp') {
                        transactionRecords += "<tr><td>" + transactionRef + "</td><td>" + data.itemTransData[i].tranDate + "</td><td>" + data.itemTransData[i].email + "</td><td>" + data.itemTransData[i].branchName + "</td><td>" + data.itemTransData[i].transactionPurpose + "</td><td>" + data.itemTransData[i].paymode + "</td><td style='text-align: right;'>" + data.itemTransData[i].particulars + "</td><td style='text-align: right;'>" + data.itemTransData[i].debit + "</td><td></td><td>" + data.itemTransData[i].remarks + "</td></tr>";
                    } else if (headType == 'pded') {
                        transactionRecords += "<tr><td>" + transactionRef + "</td><td>" + data.itemTransData[i].tranDate + "</td><td>" + data.itemTransData[i].email + "</td><td>" + data.itemTransData[i].branchName + "</td><td>" + data.itemTransData[i].transactionPurpose + "</td><td>" + data.itemTransData[i].paymode + "</td><td>" + data.itemTransData[i].particulars + "</td><td></td><td style='text-align: right;'>" + data.itemTransData[i].credit + "</td><td>" + data.itemTransData[i].remarks + "</td></tr>";
                    } else {
                        transactionRecords += "<tr><td>" + transactionRef + "</td><td>" + data.itemTransData[i].tranDate + "</td><td>" + data.itemTransData[i].email + "</td><td>" + data.itemTransData[i].branchName + "</td><td></td><td>" + data.itemTransData[i].transactionPurpose + "</td><td></td><td></td><td></td><td>" + data.itemTransData[i].paymode + "</td><td>" + data.itemTransData[i].particulars + "</td><td style='text-align: right;'>" + Number(data.itemTransData[i].debit).format(2) + "</td><td style='text-align: right;'>" + Number(data.itemTransData[i].credit).format(2) + "</td><td>" + data.itemTransData[i].remarks + "</td></tr>";
                    }
                } else {
                    transactionRecords += "<tr id='" + transactionRef + "' onclick='displayTransctionModal(this);' style='cursor: pointer;'><td>" + transactionRef + "</td><td>" + data.itemTransData[i].tranDate + "</td><td>" + data.itemTransData[i].email + "</td><td>" + data.itemTransData[i].branchName + "</td><td>" + data.itemTransData[i].projectName + "</td><td>" + data.itemTransData[i].transactionPurpose + "</td><td>" + data.itemTransData[i].typeOfSupply + "</td><td>" + data.itemTransData[i].custVendName + "</td><td>" + data.itemTransData[i].placeOfSupply + "</td>";
                    if ((headType == 'item' || headType == "") && toplevelaccountcode == "1000000000000000000") {
                        if (data.itemTransData[i].transactionPurpose == "Buy on cash & pay right away" || data.itemTransData[i].transactionPurpose == "Buy on credit & pay later" || data.itemTransData[i].transactionPurpose == "Buy on Petty Cash Account" || data.itemTransData[i].transactionPurpose == "Pay advance to vendor or supplier" || data.itemTransData[i].transactionPurpose == "Pay vendor/supplier" || data.itemTransData[i].transactionPurpose == "Credit Note for vendor" || data.itemTransData[i].transactionPurpose == "Debit Note for vendor") {
                            transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td></td><td>" + data.itemTransData[i].poRef + "</td>";
                        } else {
                            transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td>" + data.itemTransData[i].hsnSac + "</td><td>" + data.itemTransData[i].poRef + "</td>";
                        }
                    } else if (headType == 'cust' && toplevelaccountcode == "4000000000000000000") {
                        transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td>" + data.itemTransData[i].refNo + "</td>";
                    } else if ((headType == 'taxs' || headType == 'sgst' || headType == 'cgst' || headType == 'igst' || headType == 'cess' || headType == 'tds') && toplevelaccountcode == "4000000000000000000") {
                        if (data.itemTransData[i].transactionPurpose == "Buy on cash & pay right away" || data.itemTransData[i].transactionPurpose == "Buy on credit & pay later" || data.itemTransData[i].transactionPurpose == "Buy on Petty Cash Account" || data.itemTransData[i].transactionPurpose == "Pay advance to vendor or supplier" || data.itemTransData[i].transactionPurpose == "Pay vendor/supplier" || data.itemTransData[i].transactionPurpose == "Credit Note for vendor" || data.itemTransData[i].transactionPurpose == "Debit Note for vendor") {
                            transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td></td><td>" + data.itemTransData[i].poRef + "</td>";
                        } else {
                            transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td>" + data.itemTransData[i].refNo + "</td><td>" + data.itemTransData[i].poRef + "</td>";
                        }
                    } else if (headType == 'cust') {
                        transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td>" + data.itemTransData[i].refNo + "</td><td>" + data.itemTransData[i].poRef + "</td>";
                    } else if ((headType == 'item' || headType == "" || headType == "vend" || headType == "vAdv") || ((headType == 'taxs' || headType == 'sgst' || headType == 'cgst' || headType == 'igst' || headType == 'cess' || headType == 'tds') && toplevelaccountcode == "3000000000000000000")) {
                        if (typeof data.itemTransData[i].poRef != undefined && data.itemTransData[i].poRef != null) {
                            transactionRecords += "<td>" + data.itemTransData[i].poRef + "</td>";
                        } else {
                            transactionRecords += "<td></td>";
                        }
                        if (typeof data.itemTransData[i].invoiceDate != undefined && data.itemTransData[i].invoiceDate != null) {
                            transactionRecords += "<td>" + data.itemTransData[i].invoiceDate + "</td>";
                        } else {
                            transactionRecords += "<td></td>";
                        }
                        if (typeof data.itemTransData[i].invoiceNo != undefined && data.itemTransData[i].invoiceNo != null) {
                            transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td>";
                        } else {
                            transactionRecords += "<td></td>";
                        }
                        if (typeof data.itemTransData[i].grnDate != undefined && data.itemTransData[i].grnDate != null) {
                            transactionRecords += "<td>" + data.itemTransData[i].grnDate + "</td>";
                        } else {
                            transactionRecords += "<td></td>";
                        }

                        if (typeof data.itemTransData[i].grnNo != undefined && data.itemTransData[i].grnNo != null) {
                            transactionRecords += "<td>" + data.itemTransData[i].grnNo + "</td>";
                        } else {
                            transactionRecords += "<td></td>";
                        }

                        if (typeof data.itemTransData[i].impDate != undefined && data.itemTransData[i].impDate != null) {
                            transactionRecords += "<td>" + data.itemTransData[i].impDate + "</td>";
                        } else {
                            transactionRecords += "<td></td>";
                        }

                        if (typeof data.itemTransData[i].impNo != undefined && data.itemTransData[i].impNo != null) {
                            transactionRecords += "<td>" + data.itemTransData[i].impNo + "</td>";
                        } else {
                            transactionRecords += "<td></td>";
                        }
                    }
                    if ((headType == 'item' || headType == "") || ((headType == 'taxs' || headType == 'sgst' || headType == 'cgst' || headType == 'igst' || headType == 'cess' || headType == 'tds') && toplevelaccountcode != "4000000000000000000")) {
                        if (typeof data.itemTransData[i].taxRate != undefined && data.itemTransData[i].taxRate != null) {
                            transactionRecords += "<td>" + data.itemTransData[i].taxRate + "</td>";
                        } else {
                            transactionRecords += "<td></td>";
                        }
                        if (typeof data.itemTransData[i].cessRate != undefined && data.itemTransData[i].cessRate != null) {
                            transactionRecords += "<td>" + data.itemTransData[i].cessRate + "</td>";
                        } else {
                            transactionRecords += "<td></td>";
                        }
                    }

                    transactionRecords += "<td>" + data.itemTransData[i].paymode + "</td><td><div class='tbLedgerDivCls' >" + data.itemTransData[i].itemName + " </div></td><td style='text-align: right;'>" + Number(data.itemTransData[i].debit).format(2) + "</td><td style='text-align: right;'>" + Number(data.itemTransData[i].credit).format(2) + "</td><td style='max-width: 120px; word-wrap: break-word;'>" + data.itemTransData[i].remarks + "</td></tr>";
                }
                previousItemId = data.itemTransData[i].ledgerId;
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] tbody").append(transactionRecords);
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });
}

function showTransactionForParticulars(elem, toplevelaccountcode, openingBalance, closingBalance, creditAmt, debitAmt, accountNameTmp) {
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    $("#itemsTransactionsTb").attr('data-toggle', 'modal');

    $("#itemsTransactionsTb div[class='modal-body']").html("");
    let trialBalBranch = $("#selectBranchTrialBalanceId option:selected").val();
    let fromDate = $("#trialBalanceFromDate").val();
    let toDate = $("#trialBalanceToDate").val();
    let jsonData = {};
    jsonData.specificid = "0";
    jsonData.headid2 = "null";
    jsonData.identForDataValid = "0";
    jsonData.toplevelaccountcode = toplevelaccountcode;
    jsonData.fromDate = fromDate;
    jsonData.toDate = toDate;
    jsonData.headType = "null";
    jsonData.trialBalBranch = trialBalBranch;

    let specificid = 0;
    let identForDataValid = 0;
    let headType = 0;

    let url = "/ledger/itemtransactions";
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
            $("#itemsTransactionsTb").modal('show');
            $("#ledgerTranDiv").html("");
            $("#ledgerTranDiv").append('<input type="hidden" id="specificidTmp" value="' + specificid + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="identForDataValidTmp" value="' + identForDataValid + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="toplevelaccountcodeTmp" value="' + toplevelaccountcode + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="fromDateTmp" value="' + fromDate + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="toDateTmp" value="' + toDate + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="headTypeTmp" value="' + headType + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="accountNameTmp" value="' + accountNameTmp + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="openingBalanceTmp" value="' + openingBalance + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="debitAmtTmp" value="' + debitAmt + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="creditAmtTmp" value="' + creditAmt + '">');
            $("#ledgerTranDiv").append('<input type="hidden" id="closingBalanceTmp" value="' + closingBalance + '">');
            $("#itemsTransactionsTb div[class='modal-body']").append('<div style="padding: 0px; margin: 0px;"><div style="float: left; width: 70%; max-width: 70%; text-align:left; padding: 0px; margin: 0px; overflow: auto;"><b>Ledger Name:</b> ' + accountNameTmp + '</div><div style="float: right; width: 30%; text-align:right; padding: 0px; margin: 0px;"><b>Period: </b>' + data.period + '</div></div><br><div style="width:99.9%; float:right; padding: 0px; margin: 0px;"><b>Opening Balance: </b>' + Number(openingBalance).format(2) + ' <b>&nbsp;&nbsp;&nbsp;&nbsp;Total Debit: </b>' + Number(debitAmt).format(2) + '<b>&nbsp;&nbsp;&nbsp;&nbsp;Total Credit: </b>' + Number(creditAmt).format(2) + ' <b>&nbsp;&nbsp;&nbsp;&nbsp;Closing Balance: </b>' + Number(closingBalance).format(2) + '</div>');

            $("#itemsTransactionsTb div[class='modal-body']").append('<div style="margin:0px; padding:0px;  height:50%; max-height: 420px; width: 100%; overflow: scroll;"><table id="itemTransactionTable" class="table table-hover table-striped table-bordered" style="margin-top: 2px;"><thead class="tablehead1"><tr><th>TRANSACTION REF.</th><th>DATE</th><th>USER</th><th>BRANCH</th><th>PROJECT</th><th>TRANSACTION TYPE</th><th>TYPE OF SUPPLY</th><th>CUSTOMER/ VENDOR</th><th>PLACE OF SUPPLY</th></tr></thead><tbody></tbody></table></div>');
            if ((headType == 'item' || headType == "") && toplevelaccountcode == "1000000000000000000") {
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").append('<th>INV NO.</th><th>HSN/SAC</th><th>PO REF.</th>');
            } else if (headType == 'cust' && toplevelaccountcode == "4000000000000000000") { //cAdv
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").append('<th>INV REF NO.</th><th>REF NO.</th>');
            } else if ((headType == 'taxs' || headType == 'sgst' || headType == 'cgst' || headType == 'igst' || headType == 'cess' || headType == 'tds') && toplevelaccountcode == "4000000000000000000") {
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").append('<th>INV REF NO.</th><th>REF NO.</th><th>PO REF.</th>');
            } else if (headType == 'cust') {
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").append('<th>INV NO.</th><th>REF NO.</th><th>PO REF.</th>');
            } else if ((headType == 'item' || headType == "" || headType == "vend" || headType == "vAdv") || ((headType == 'taxs' || headType == 'sgst' || headType == 'cgst' || headType == 'igst' || headType == 'cess' || headType == 'tds') && toplevelaccountcode == "3000000000000000000")) {
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").append('<th>PURCHASE ORDER</th>');
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").append('<th>INV/REF DATE</th><th>INV/REF NO.</th><th>DC/GRN REF DATE</th><th>DC/GRN REF NO.</th><th>IMPORT REF DATE</th><th>IMPORT REF NO.</th>');
            } else if ((headType == 'pexp' && toplevelaccountcode == "2000000000000000000") || (headType == 'pded' && toplevelaccountcode == "4000000000000000000")) {
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").html("");
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").append('<th>TRANSACTION REF NO</th><th>DATE</th><th>USER</th><th>BRANCH</th><th>TRANSACTION</th>');
            }
            if ((headType == 'item' || headType == "") || ((headType == 'taxs' || headType == 'sgst' || headType == 'cgst' || headType == 'igst' || headType == 'cess' || headType == 'tds') && toplevelaccountcode != "4000000000000000000")) {
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").append('<th>TAX RATE</th><th>CESS RATE</th>');
            }

            $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").append('<th>PAY MODE</th><th>PARTICULARS</th><th>DEBIT</th><th>CREDIT</th><th>REMARKS</th>');
            let previousItemId = "";
            let tableHead = $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] thead tr").clone();
            let tableHeadLength = $(tableHead).children('th').length;
            let tableHeadHtml = $(tableHead).html();
            tableHeadHtml = '<tr class="tbledgerSubHead">' + tableHeadHtml + '</tr>';

            for (let i = 0; i < data.itemTransData.length; i++) {
                let transactionRecords = "";
                let transactionRef = data.itemTransData[i].txnRef;
                if (i == 0) {
                    //$("#itemsTransactionsTb div[class='modal-body']").append('<div style="margin-left: 10px; text-align: left; font-size: 11px;" itemid="' + data.itemTransData[0].ledgerId + '">' + data.itemTransData[0].ledgerName + '</div>');
                    $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] tbody").append('<tr itemid="' + data.itemTransData[i].ledgerId + '" class="tbledgerSubHead"><td colspan="' + tableHeadLength + '"><div class="tbLedgerItemSubHeadDiv">' + data.itemTransData[i].ledgerName + '</div><div class="tbLedgerSubHeadDiv">Opening Balance: ' + Number(data.itemTransData[i].openingBalance).format(2) + '</div><div class="tbLedgerSubHeadDiv">Debit Amount: ' + Number(data.itemTransData[i].debitAmount).format(2) + '</div><div class="tbLedgerSubHeadDiv">Credit Amount: ' + Number(data.itemTransData[i].creditAmount).format(2) + '</div><div class="tbLedgerSubHeadDiv"> Closing Balance: ' + Number(data.itemTransData[i].closingBalance).format(2) + '</div></td></tr>');
                }
                console.log("--------" + previousItemId);
                if (previousItemId != "" && typeof data.itemTransData[i].ledgerId != 'undefined' && (data.itemTransData[i].ledgerId).indexOf(previousItemId) == -1) {
                    console.log(">>> " + data.itemTransData[i].ledgerId);
                    //$("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] tbody").append('<tr itemid="' + data.itemTransData[i].ledgerId + '" class="tbledgerSubHead"><td colspan="' + tableHeadLength + '">' + data.itemTransData[i].ledgerName + '</td></tr>');
                    //$("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable']").append(tableHeadHtml);
                    $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] tbody").append('<tr itemid="' + data.itemTransData[i].ledgerId + '" class="tbledgerSubHead"><td colspan="' + tableHeadLength + '"><div class="tbLedgerItemSubHeadDiv">' + data.itemTransData[i].ledgerName + '</div><div class="tbLedgerSubHeadDiv">Opening Balance: ' + Number(data.itemTransData[i].openingBalance).format(2) + '</div><div class="tbLedgerSubHeadDiv">Debit Amount: ' + Number(data.itemTransData[i].debitAmount).format(2) + '</div><div class="tbLedgerSubHeadDiv">Credit Amount: ' + Number(data.itemTransData[i].creditAmount).format(2) + '</div><div class="tbLedgerSubHeadDiv"> Closing Balance: ' + Number(data.itemTransData[i].closingBalance).format(2) + '</div></td></tr>');
                } else if (typeof data.itemTransData[i].ledgerId == 'undefined') {
                    console.log("#####" + data.itemTransData[i].itemName);
                }

                if (transactionRef.startsWith("PROVTXN")) {
                    let projectName = "";
                    if (data.itemTransData[i].debit == "" || data.itemTransData[i].debit == "0.00") {
                        projectName = data.itemTransData[i].creditProjectName;
                    } else {
                        projectName = data.itemTransData[i].debitProjectName;
                    }
                    transactionRecords += "<tr id='" + transactionRef + "' onclick='displayTransctionModal(this);' style='cursor: pointer;'><td>" + transactionRef + "</td><td>" + data.itemTransData[i].tranDate + "</td><td>" + data.itemTransData[i].email + "</td><td>" + data.itemTransData[i].branchName + "</td><td>" + projectName + "</td><td>" + data.itemTransData[i].transactionPurpose + "</td><td>" + data.itemTransData[i].typeOfSupply + "</td><td>" + data.itemTransData[i].custVendName + "</td><td>" + data.itemTransData[i].placeOfSupply + "</td>";
                    if ((headType == 'item' || headType == "") && toplevelaccountcode == "1000000000000000000") {
                        transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td>" + data.itemTransData[i].hsnSac + "</td><td>" + data.itemTransData[i].poRef + "</td>";
                    } else if (headType == 'cust' && toplevelaccountcode == "4000000000000000000") {
                        transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td>" + data.itemTransData[i].refNo + "</td>";
                    } else if ((headType == 'taxs' || headType == 'sgst' || headType == 'cgst' || headType == 'igst' || headType == 'cess' || headType == 'tds') && toplevelaccountcode == "4000000000000000000") {
                        transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td>" + data.itemTransData[i].refNo + "</td><td>" + data.itemTransData[i].poRef + "</td>";
                    } else if (headType == 'cust') {
                        transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td>" + data.itemTransData[i].refNo + "</td><td>" + data.itemTransData[i].poRef + "</td>";
                    } else if ((headType == 'item' || headType == "" || headType == "vend" || headType == "vAdv") || ((headType == 'taxs' || headType == 'sgst' || headType == 'cgst' || headType == 'igst' || headType == 'cess' || headType == 'tds') && toplevelaccountcode == "3000000000000000000")) {
                        transactionRecords += "<td>" + data.itemTransData[i].poRef + "</td>";
                        transactionRecords += "<td>" + data.itemTransData[i].invoiceDate + "</td><td>" + data.itemTransData[i].invoiceNo + "</td><td>" + data.itemTransData[i].grnDate + "</td><td>" + data.itemTransData[i].grnNo + "</td><td>" + data.itemTransData[i].impDate + "</td><td>" + data.itemTransData[i].impNo + "</td>";
                    }
                    if ((headType == 'item' || headType == "") || ((headType == 'taxs' || headType == 'sgst' || headType == 'cgst' || headType == 'igst' || headType == 'cess' || headType == 'tds') && toplevelaccountcode != "4000000000000000000")) {
                        transactionRecords += "<td>" + data.itemTransData[i].taxRate + "</td><td>" + data.itemTransData[i].cessRate + "</td>";
                    }

                    transactionRecords += "<td>" + data.itemTransData[i].paymode + "</td><td><b>Debit: </b><div class='tbLedgerDivCls' >" + data.itemTransData[i].itemName + " </div><br><hr><b>Credit: </b>" + data.itemTransData[i].creditItemsName + "</td><td style='text-align: right;'>" + Number(data.itemTransData[i].debit).format(2) + "</td><td style='text-align: right;'>" + Number(data.itemTransData[i].credit).format(2) + "</td><td style='max-width: 120px; word-wrap: break-word;'>" + data.itemTransData[i].remarks + "</td></tr>";
                } else if (transactionRef.startsWith("PRTXN")) {
                    if (headType == 'pexp') {
                        transactionRecords += "<tr><td>" + transactionRef + "</td><td>" + data.itemTransData[i].tranDate + "</td><td>" + data.itemTransData[i].email + "</td><td>" + data.itemTransData[i].branchName + "</td><td>" + data.itemTransData[i].transactionPurpose + "</td><td>" + data.itemTransData[i].paymode + "</td><td style='text-align: right;'>" + data.itemTransData[i].particulars + "</td><td style='text-align: right;'>" + data.itemTransData[i].debit + "</td><td></td><td>" + data.itemTransData[i].remarks + "</td></tr>";
                    } else if (headType == 'pded') {
                        transactionRecords += "<tr><td>" + transactionRef + "</td><td>" + data.itemTransData[i].tranDate + "</td><td>" + data.itemTransData[i].email + "</td><td>" + data.itemTransData[i].branchName + "</td><td>" + data.itemTransData[i].transactionPurpose + "</td><td>" + data.itemTransData[i].paymode + "</td><td>" + data.itemTransData[i].particulars + "</td><td></td><td style='text-align: right;'>" + data.itemTransData[i].credit + "</td><td>" + data.itemTransData[i].remarks + "</td></tr>";
                    } else {
                        transactionRecords += "<tr><td>" + transactionRef + "</td><td>" + data.itemTransData[i].tranDate + "</td><td>" + data.itemTransData[i].email + "</td><td>" + data.itemTransData[i].branchName + "</td><td></td><td>" + data.itemTransData[i].transactionPurpose + "</td><td></td><td></td><td></td><td>" + data.itemTransData[i].paymode + "</td><td>" + data.itemTransData[i].particulars + "</td><td style='text-align: right;'>" + Number(data.itemTransData[i].debit).format(2) + "</td><td style='text-align: right;'>" + Number(data.itemTransData[i].credit).format(2) + "</td><td>" + data.itemTransData[i].remarks + "</td></tr>";
                    }
                } else {
                    transactionRecords += "<tr id='" + transactionRef + "' onclick='displayTransctionModal(this);' style='cursor: pointer;'><td>" + transactionRef + "</td><td>" + data.itemTransData[i].tranDate + "</td><td>" + data.itemTransData[i].email + "</td><td>" + data.itemTransData[i].branchName + "</td><td>" + data.itemTransData[i].projectName + "</td><td>" + data.itemTransData[i].transactionPurpose + "</td><td>" + data.itemTransData[i].typeOfSupply + "</td><td>" + data.itemTransData[i].custVendName + "</td><td>" + data.itemTransData[i].placeOfSupply + "</td>";
                    if ((headType == 'item' || headType == "") && toplevelaccountcode == "1000000000000000000") {
                        if (data.itemTransData[i].transactionPurpose == "Buy on cash & pay right away" || data.itemTransData[i].transactionPurpose == "Buy on credit & pay later" || data.itemTransData[i].transactionPurpose == "Buy on Petty Cash Account" || data.itemTransData[i].transactionPurpose == "Pay advance to vendor or supplier" || data.itemTransData[i].transactionPurpose == "Pay vendor/supplier" || data.itemTransData[i].transactionPurpose == "Credit Note for vendor" || data.itemTransData[i].transactionPurpose == "Debit Note for vendor") {
                            transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td></td><td>" + data.itemTransData[i].poRef + "</td>";
                        } else {
                            transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td>" + data.itemTransData[i].hsnSac + "</td><td>" + data.itemTransData[i].poRef + "</td>";
                        }
                    } else if (headType == 'cust' && toplevelaccountcode == "4000000000000000000") {
                        transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td>" + data.itemTransData[i].refNo + "</td>";
                    } else if ((headType == 'taxs' || headType == 'sgst' || headType == 'cgst' || headType == 'igst' || headType == 'cess' || headType == 'tds') && toplevelaccountcode == "4000000000000000000") {
                        if (data.itemTransData[i].transactionPurpose == "Buy on cash & pay right away" || data.itemTransData[i].transactionPurpose == "Buy on credit & pay later" || data.itemTransData[i].transactionPurpose == "Buy on Petty Cash Account" || data.itemTransData[i].transactionPurpose == "Pay advance to vendor or supplier" || data.itemTransData[i].transactionPurpose == "Pay vendor/supplier" || data.itemTransData[i].transactionPurpose == "Credit Note for vendor" || data.itemTransData[i].transactionPurpose == "Debit Note for vendor") {
                            transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td></td><td>" + data.itemTransData[i].poRef + "</td>";
                        } else {
                            transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td>" + data.itemTransData[i].refNo + "</td><td>" + data.itemTransData[i].poRef + "</td>";
                        }
                    } else if (headType == 'cust') {
                        transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td><td>" + data.itemTransData[i].refNo + "</td><td>" + data.itemTransData[i].poRef + "</td>";
                    } else if ((headType == 'item' || headType == "" || headType == "vend" || headType == "vAdv") || ((headType == 'taxs' || headType == 'sgst' || headType == 'cgst' || headType == 'igst' || headType == 'cess' || headType == 'tds') && toplevelaccountcode == "3000000000000000000")) {
                        if (typeof data.itemTransData[i].poRef != undefined && data.itemTransData[i].poRef != null) {
                            transactionRecords += "<td>" + data.itemTransData[i].poRef + "</td>";
                        } else {
                            transactionRecords += "<td></td>";
                        }
                        if (typeof data.itemTransData[i].invoiceDate != undefined && data.itemTransData[i].invoiceDate != null) {
                            transactionRecords += "<td>" + data.itemTransData[i].invoiceDate + "</td>";
                        } else {
                            transactionRecords += "<td></td>";
                        }
                        if (typeof data.itemTransData[i].invoiceNo != undefined && data.itemTransData[i].invoiceNo != null) {
                            transactionRecords += "<td>" + data.itemTransData[i].invoiceNo + "</td>";
                        } else {
                            transactionRecords += "<td></td>";
                        }
                        if (typeof data.itemTransData[i].grnDate != undefined && data.itemTransData[i].grnDate != null) {
                            transactionRecords += "<td>" + data.itemTransData[i].grnDate + "</td>";
                        } else {
                            transactionRecords += "<td></td>";
                        }

                        if (typeof data.itemTransData[i].grnNo != undefined && data.itemTransData[i].grnNo != null) {
                            transactionRecords += "<td>" + data.itemTransData[i].grnNo + "</td>";
                        } else {
                            transactionRecords += "<td></td>";
                        }

                        if (typeof data.itemTransData[i].impDate != undefined && data.itemTransData[i].impDate != null) {
                            transactionRecords += "<td>" + data.itemTransData[i].impDate + "</td>";
                        } else {
                            transactionRecords += "<td></td>";
                        }

                        if (typeof data.itemTransData[i].impNo != undefined && data.itemTransData[i].impNo != null) {
                            transactionRecords += "<td>" + data.itemTransData[i].impNo + "</td>";
                        } else {
                            transactionRecords += "<td></td>";
                        }
                    }
                    if ((headType == 'item' || headType == "") || ((headType == 'taxs' || headType == 'sgst' || headType == 'cgst' || headType == 'igst' || headType == 'cess' || headType == 'tds') && toplevelaccountcode != "4000000000000000000")) {
                        if (typeof data.itemTransData[i].taxRate != undefined && data.itemTransData[i].taxRate != null) {
                            transactionRecords += "<td>" + data.itemTransData[i].taxRate + "</td>";
                        } else {
                            transactionRecords += "<td></td>";
                        }
                        if (typeof data.itemTransData[i].cessRate != undefined && data.itemTransData[i].cessRate != null) {
                            transactionRecords += "<td>" + data.itemTransData[i].cessRate + "</td>";
                        } else {
                            transactionRecords += "<td></td>";
                        }
                    }

                    transactionRecords += "<td>" + data.itemTransData[i].paymode + "</td><td><div class='tbLedgerDivCls' >" + data.itemTransData[i].itemName + " </div></td><td style='text-align: right;'>" + Number(data.itemTransData[i].debit).format(2) + "</td><td style='text-align: right;'>" + Number(data.itemTransData[i].credit).format(2) + "</td><td style='max-width: 120px; word-wrap: break-word;'>" + data.itemTransData[i].remarks + "</td></tr>";
                }
                previousItemId = data.itemTransData[i].ledgerId;
                $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] tbody").append(transactionRecords);
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });
}

function displayTransctionModal(elem) {

    let type = "";

    let txnRef = $(elem).attr('id');
    if (txnRef.startsWith("TXN")) {
        type = "transactionLookUp";
    } else if (txnRef.startsWith("CLAIMTXN")) {
        type = "claimTransactionLookUp";
    } else if (txnRef.startsWith("PROVTXN")) {
        type = "provisionalTransactionLookUp";
    } else if (txnRef.startsWith("BULK")) {
        type = "BulkTransactionLookUp";
    }
    if (isEmpty(txnRef)) {
        swal('Error!','Transaction ref not specified.','error');
    } else if (type == "") {
        swal('Error!','Transaction type not specified.','error');
    } else {
        let url = '/cashnbank/getData/' + txnRef + '/' + type;
        ajaxCall(url, '', '', '', 'GET', '', 'displayCashNBankModal', '', true);
    }
}


function exportTbLedger(exportType) {
    let specificid = $("#itemsTransactions div[id='ledgerTranDiv'] input[id='specificidTmp']").val();
    specificid = $("#specificidTmp").val();
    let identForDataValid = $("#itemsTransactions div[id='ledgerTranDiv'] input[id='identForDataValidTmp']").val();
    let toplevelaccountcode = $("#toplevelaccountcodeTmp").val();
    let fromDate = $("#fromDateTmp").val();
    let toDate = $("#toDateTmp").val();
    let headType = $("#headTypeTmp").val();
    let accountName = $("#accountNameTmp").val();
    let openingBalance = $("#openingBalanceTmp").val();
    let debitAmt = $("#debitAmtTmp").val();
    let creditAmt = $("#creditAmtTmp").val();
    let closingBalance = $("#closingBalanceTmp").val();
    let tableLen = $("#itemsTransactionsTb div[class='modal-body'] table[id='itemTransactionTable'] tbody tr").length;
    let branchId = $("#selectBranchTrialBalanceId").val();
    if (parseInt(tableLen) < 1) {
        swal("No Transaction!", "Transaction not found for the Particular", "success");
        return false;
    }
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    let jsonData = {};
    jsonData.specificid = specificid;
    jsonData.identForDataValid = identForDataValid;
    jsonData.toplevelaccountcode = toplevelaccountcode;
    jsonData.fromDate = fromDate;
    jsonData.toDate = toDate;
    jsonData.headType = headType;
    jsonData.exportType = exportType;
    jsonData.accountName = accountName;
    jsonData.debitAmt = debitAmt;
    jsonData.creditAmt = creditAmt;
    jsonData.closingBalance = closingBalance;
    jsonData.openingBalance = openingBalance;
    jsonData.trialBalBranch = branchId;
    let url = "/trialBalance/exportLedger";
    downloadFile(url, "POST", jsonData, "Error on downloading Trial Balance Ledger!");
}


function downloadTrialBalance(elem) {
    let fmDate = $("#trialBalanceFromDate").val();
    let tDate = $("#trialBalanceToDate").val();
    if (fmDate == "" || typeof fmDate == "undefined" || tDate == "" || typeof tDate == "undefined") {
        swal("Error!","Please Provide The Date Range For Your Trial Balance","error");
        return true;
    }
    //$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    let jsonData = {};
    jsonData.accessKey = "IDKrg0f9dHMhVROS";
    jsonData.email = $("#hiddenuseremail").text();
    jsonData.fromDate = fmDate;
    jsonData.toDate = tDate;
    jsonData.trialBalanceForBranch = $("#selectBranchTrialBalanceId").val();
    let url = "/trialBalance/downloadTrialBalance";
    if (elem === 'pdf') {
        url = "/trialBalance/exportPDF";
    }
    downloadFile(url, "POST", jsonData, "Error on downloading TrialBalance!");
}
