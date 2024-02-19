function getChildChartOfAccount(elem, identForDataValid) {
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var useremail = $("#hiddenuseremail").text();
    var accountCode = $(elem).attr('id');
    var jsonData = {};
    var identDataFlag = identForDataValid;
    jsonData.usermail = useremail;
    jsonData.identForDataValid = identForDataValid;
    jsonData.coaAccountCode = accountCode;
    var url = "/config/getCoaChild";
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
            logDebug("Start getChildChartOfAccount");
            if (data.coaSpecfChildData.length > 0) {
                $("li[id=" + accountCode + "]").append('<ul id="mainChartOfAccount" class="treeview-black mainChartOfAccount"></ul>');
                for (var i = 0; i < data.coaSpecfChildData.length; i++) {
                    var topLevelAccountCode = data.coaSpecfChildData[i].topLevelAccountCode;
                    if (data.coaSpecfChildData[i].iscoachild == "1") {
                        if (identDataFlag >= 39 && identDataFlag <= 46 || identDataFlag >= 53 && identDataFlag <= 56) {
                            $("li[id=" + accountCode + "]").find("ul[id='mainChartOfAccount']").append('<li id=' + data.coaSpecfChildData[i].specfaccountCode + '><div class="chartOfAccountContainer"><p class="color-grey"><img id="' + data.coaSpecfChildData[i].specfaccountCode + '" src="/assets/images/minus.png" class="coaPlusMinusImage" id="getNodeChild"></img><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details' + data.coaSpecfChildData[i].id + '" href="#itemSetUp" class="color-grey" onclick="showItemEntityDetails(this,0)">' + data.coaSpecfChildData[i].name + '</a></span></b>' +
                                '<button style="float:right" id="openingBalanceModal" class="btn btn-tree btn-idos" title="Update Opening Balance" onclick="popupForUpdatingOpeningBalance(this,' + data.coaSpecfChildData[i].id + ',' + identForDataValid + ');"><i class="fa fa-pencil-square-o pr-5"></i>Update Opening Balance</button></p></li>');
                        } else if (identDataFlag === '57' || identDataFlag === '60' || identDataFlag === '61') {
                            $("li[id=" + accountCode + "]").find("ul[id='mainChartOfAccount']").append('<li id=' + data.coaSpecfChildData[i].specfaccountCode + '><div class="chartOfAccountContainer"><p class="color-grey"><img id="' + data.coaSpecfChildData[i].specfaccountCode + '" src="/assets/images/minus.png" class="coaPlusMinusImage" id="getNodeChild"></img><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details' + data.coaSpecfChildData[i].id + '" href="#itemSetUp" class="color-grey" onclick="showItemEntityDetails(this,0)">' + data.coaSpecfChildData[i].name + '</a></span></b>' +
                                '<button style="float:right" id="openingBalanceModal" class="btn btn-tree btn-idos" title="Update Opening Balance" onclick="popupForUpdatingOpeningBalance(this,' + data.coaSpecfChildData[i].id + ',' + identForDataValid + ');"><i class="fa fa-pencil-square-o pr-5"></i>Update Opening Balance</button></p></li>');
                        } else if (identDataFlag === '58' || identDataFlag === '59') {
                            $("li[id=" + accountCode + "]").find("ul[id='mainChartOfAccount']").append('<li id=' + data.coaSpecfChildData[i].specfaccountCode + '><div class="chartOfAccountContainer"><p class="color-grey"><img id="' + data.coaSpecfChildData[i].specfaccountCode + '" src="/assets/images/minus.png" class="coaPlusMinusImage" id="getNodeChild"></img><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details' + data.coaSpecfChildData[i].id + '" href="#itemSetUp" class="color-grey" onclick="showItemEntityDetails(this,0)">' + data.coaSpecfChildData[i].name + '</a></span></b>' +
                                '<button style="float:right" id="openingBalanceModal" class="btn btn-tree btn-idos" title="Update Opening Balance" onclick="popupForUpdatingOpeningBalance(this,' + data.coaSpecfChildData[i].id + ',' + identForDataValid + ');"><i class="fa fa-pencil-square-o pr-5"></i>Update Opening Balance</button></p></li>');
                        } else {
                            $("li[id=" + accountCode + "]").find("ul[id='mainChartOfAccount']").append('<li id=' + data.coaSpecfChildData[i].specfaccountCode + '><div class="chartOfAccountContainer"><p class="color-grey"><img id="' + data.coaSpecfChildData[i].specfaccountCode + '" src="/assets/images/minus.png" class="coaPlusMinusImage" id="getNodeChild"></img><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details' + data.coaSpecfChildData[i].id + '" href="#itemSetUp" class="color-grey" onclick="showItemEntityDetails(this,0)">' + data.coaSpecfChildData[i].name + '</a></span></b></p></li>');
                        }
                    } else {
                        var txtidentificationForDataValid = data.coaSpecfChildData[i].identificationForDataValid;
                        if (txtidentificationForDataValid == "" || txtidentificationForDataValid == null || txtidentificationForDataValid == "52" || txtidentificationForDataValid == "24" || txtidentificationForDataValid == "25" || txtidentificationForDataValid == "26" || txtidentificationForDataValid == "27") { //if comb sales, then allowed to enter subaccounts from COA
                            if (txtidentificationForDataValid == "52") {
                                txtidentificationForDataValid = "";
                            }
                            if (data.coaSpecfChildData[i].isTxnAndOpBalPresent ==true || data.coaSpecfChildData[i].isTxnAndOpBalPresent == "true") {
                                $("li[id=" + accountCode + "]").find("ul[id='mainChartOfAccount']").append('<li id=' + data.coaSpecfChildData[i].specfaccountCode + '><div class="chartOfAccountContainer"><p class="color-grey"><img id="' + data.coaSpecfChildData[i].specfaccountCode + '" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this,\'' + txtidentificationForDataValid + '\');"></img><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details' + data.coaSpecfChildData[i].id + '" href="#itemSetUp" class="color-grey" onclick="showItemEntityDetails(this,' + topLevelAccountCode + ')">' + data.coaSpecfChildData[i].name + '</a></span></b></p></li>');
                            } else {
                                $("li[id=" + accountCode + "]").find("ul[id='mainChartOfAccount']").append('<li id=' + data.coaSpecfChildData[i].specfaccountCode + '><div class="chartOfAccountContainer"><p class="color-grey"><img id="' + data.coaSpecfChildData[i].specfaccountCode + '" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this,\'' + txtidentificationForDataValid + '\');"></img><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details' + data.coaSpecfChildData[i].id + '" href="#itemSetUp" class="color-grey" onclick="showItemEntityDetails(this,' + topLevelAccountCode + ')">' + data.coaSpecfChildData[i].name + '</a></span></b><button style="float:right" id="newItemform-container" name="' + data.coaSpecfChildData[i].name + '" class="newEntityCreateButton btn btn-tree btn-idos" title="Create new Sub Account" onclick="javascript:createCOA(this,' + data.coaSpecfChildData[i].id + ',' + topLevelAccountCode + ',' + txtidentificationForDataValid + ');"><i class="fa fa-plus pr-3"></i>Add Sub Account</button></p></li>');
                            }
                        } else {
                            /*if((topLevelAccountCode == 3000000000000000000 && data.coaSpecfChildData[i].identificationForDataValid == 4)||(topLevelAccountCode == 4000000000000000000 && data.coaSpecfChildData[i].identificationForDataValid == 5)){
                                if(data.coaSpecfChildData[i].isbankAccount == 0){
                                    $("li[id=" + accountCode + "]").find("ul[id='mainChartOfAccount']").append('<li id=' + data.coaSpecfChildData[i].specfaccountCode + '><div class="chartOfAccountContainer"><p class="color-grey"><img id="' + data.coaSpecfChildData[i].specfaccountCode + '" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this,\'' + txtidentificationForDataValid + '\');"></img><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details' + data.coaSpecfChildData[i].id + '" href="#itemSetUp" class="color-grey" onclick="showItemBankEntityDetails(this,' + topLevelAccountCode + ')">' + data.coaSpecfChildData[i].name + '</a></span></b><button style="float:right" id="newItemform-container" name="' + data.coaSpecfChildData[i].name + '" class="newEntityCreateButton btn btn-tree btn-idos" title="Create new Bank Account" onclick="javascript:createbankCOA(this,' + data.coaSpecfChildData[i].id + ',' + topLevelAccountCode + ',' + txtidentificationForDataValid + ');"><i class="fa fa-plus pr-3"></i>Add Bank Account</button></p></li>');
                                }else{
                                    $("li[id=" + accountCode + "]").find("ul[id='mainChartOfAccount']").append('<li id=' + data.coaSpecfChildData[i].specfaccountCode + '><div class="chartOfAccountContainer"><p class="color-grey"><img id="' + data.coaSpecfChildData[i].specfaccountCode + '" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this,\'' + txtidentificationForDataValid + '\');"></img><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details' + data.coaSpecfChildData[i].id + '" href="#itemSetUp" class="color-grey" onclick="showItemBankEntityDetails(this,' + topLevelAccountCode + ')">' + data.coaSpecfChildData[i].name + '</a></span></b></p></li>');
                                }
                            }else{*/
                            if (data.coaSpecfChildData[i].isTxnAndOpBalPresent = true || data.coaSpecfChildData[i].isTxnAndOpBalPresent == "true") {
                                /*if((data.coaSpecfChildData[i].identificationForDataValid == 4) && (data.coaSpecfChildData[i].identificationForDataValid == 5)){
                                    if(data.coaSpecfChildData[i].isbankAccount == 0){
                                        $("li[id=" + accountCode + "]").find("ul[id='mainChartOfAccount']").append('<li id=' + data.coaSpecfChildData[i].specfaccountCode + '><div class="chartOfAccountContainer"><p class="color-grey"><img id="' + data.coaSpecfChildData[i].specfaccountCode + '" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this,\'' + txtidentificationForDataValid + '\');"></img><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details' + data.coaSpecfChildData[i].id + '" href="#itemSetUp" class="color-grey" onclick="showItemEntityDetails(this,' + topLevelAccountCode + ')">' + data.coaSpecfChildData[i].name + '</a></span></b><button style="float:right" id="newItemform-container" name="' + data.coaSpecfChildData[i].name + '" class="newEntityCreateButton btn btn-tree btn-idos" title="Create new Bank Account" onclick="javascript:createbankCOA(this,' + data.coaSpecfChildData[i].id + ',' + topLevelAccountCode + ',' + txtidentificationForDataValid + ');"><i class="fa fa-plus pr-3"></i>Add Bank Account</button></p></li>');
                                    }else{
                                        $("li[id=" + accountCode + "]").find("ul[id='mainChartOfAccount']").append('<li id=' + data.coaSpecfChildData[i].specfaccountCode + '><div class="chartOfAccountContainer"><p class="color-grey"><img id="' + data.coaSpecfChildData[i].specfaccountCode + '" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this,\'' + txtidentificationForDataValid + '\');"></img><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details' + data.coaSpecfChildData[i].id + '" href="#itemSetUp" class="color-grey" onclick="showItemBankEntityDetails(this,' + topLevelAccountCode + ')">' + data.coaSpecfChildData[i].name + '</a></span></b></p></li>');
                                    }
                                }else{*/
                                    $("li[id=" + accountCode + "]").find("ul[id='mainChartOfAccount']").append('<li id=' + data.coaSpecfChildData[i].specfaccountCode + '><div class="chartOfAccountContainer"><p class="color-grey"><img id="' + data.coaSpecfChildData[i].specfaccountCode + '" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this,\'' + txtidentificationForDataValid + '\');"></img><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details' + data.coaSpecfChildData[i].id + '" href="#itemSetUp" class="color-grey" onclick="showItemEntityDetails(this,' + topLevelAccountCode + ')">' + data.coaSpecfChildData[i].name + '</a></span></b></p></li>');
                                /*}*/
                            } else {
                                /*if((data.coaSpecfChildData[i].identificationForDataValid == 4) || (data.coaSpecfChildData[i].identificationForDataValid == 5)){
                                    if(data.coaSpecfChildData[i].isbankAccount == 0){
                                    $("li[id=" + accountCode + "]").find("ul[id='mainChartOfAccount']").append('<li id=' + data.coaSpecfChildData[i].specfaccountCode + '><div class="chartOfAccountContainer"><p class="color-grey"><img id="' + data.coaSpecfChildData[i].specfaccountCode + '" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this,\'' + txtidentificationForDataValid + '\');"></img><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details' + data.coaSpecfChildData[i].id + '" href="#itemSetUp" class="color-grey" onclick="showItemBankEntityDetails(this,' + topLevelAccountCode + ')">' + data.coaSpecfChildData[i].name + '</a></span></b><button style="float:right" id="newItemform-container" name="' + data.coaSpecfChildData[i].name + '" class="newEntityCreateButton btn btn-tree btn-idos" title="Create new Bank Account" onclick="javascript:createbankCOA(this,' + data.coaSpecfChildData[i].id + ',' + topLevelAccountCode + ',' + txtidentificationForDataValid + ');"><i class="fa fa-plus pr-3"></i>Add Bank Account</button></p></li>');
                                }else{
                                    $("li[id=" + accountCode + "]").find("ul[id='mainChartOfAccount']").append('<li id=' + data.coaSpecfChildData[i].specfaccountCode + '><div class="chartOfAccountContainer"><p class="color-grey"><img id="' + data.coaSpecfChildData[i].specfaccountCode + '" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this,\'' + txtidentificationForDataValid + '\');"></img><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details' + data.coaSpecfChildData[i].id + '" href="#itemSetUp" class="color-grey" onclick="showItemBankEntityDetails(this,' + topLevelAccountCode + ')">' + data.coaSpecfChildData[i].name + '</a></span></b></p></li>');
                                }
                                }else{*/
                                    $("li[id=" + accountCode + "]").find("ul[id='mainChartOfAccount']").append('<li id=' + data.coaSpecfChildData[i].specfaccountCode + '><div class="chartOfAccountContainer"><p class="color-grey"><img id="' + data.coaSpecfChildData[i].specfaccountCode + '" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this,\'' + txtidentificationForDataValid + '\');"></img><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details' + data.coaSpecfChildData[i].id + '" href="#itemSetUp" class="color-grey" onclick="showItemEntityDetails(this,' + topLevelAccountCode + ')">' + data.coaSpecfChildData[i].name + '</a></span></b><button style="float:right" id="newItemform-container" name="' + data.coaSpecfChildData[i].name + '" class="newEntityCreateButton btn btn-tree btn-idos" title="Create new Sub Account" onclick="javascript:createCOA(this,' + data.coaSpecfChildData[i].id + ',' + topLevelAccountCode + ',' + txtidentificationForDataValid + ');"><i class="fa fa-plus pr-3"></i>Add Sub Account</button></p></li>');
                                /*}*/

                            /*}*/
                            }
                        }
                    }
                }
                $(elem).attr("src", "assets/images/minus.png");
                $(elem).attr("onclick", 'javascript:removeCOA(this,\'' + identForDataValid + '\');');
            } else {
                $(elem).attr("src", "assets/images/minus.png");
            }
            logDebug("End getChildChartOfAccount");
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

function popupForUpdatingOpeningBalance(elemt, id, mappingid) {
    var jsonData = {};
    var methodType = "GET";
    jsonData.useremail = $("#hiddenuseremail").text();
    var url = "/tax/getBranchOpeningAmt/" + id;
    if (mappingid === 57) {
        url = "/coa/getInterBranch/" + id;
    } else if (mappingid === 60 || mappingid === 61) {
        url = "/user/getUserAdvClaim";
        methodType = "POST";
        jsonData.mappingId = mappingid;
        jsonData.userId = id;
    } else if (mappingid === 58 || mappingid === 59) {
        url = "/payroll/getPayrollOpeningBalance";
        methodType = "POST";
        jsonData.mappingId = mappingid;
        jsonData.itemId = id;
    }
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        async: false,
        method: methodType,
        contentType: 'application/json',
        success: function (data) {
            $('#addTaxOpeningBalanceModal').find('.taxNameHeader').html(data.name);
            $('#addTaxOpeningBalanceModal').data('tax-id', data.id);
            $('#inputOpeningBalance').val(data.openingBalance);
            $('#addTaxOpeningBalanceModal').modal("show");
            $('#addTaxOpeningBalanceModal #coaMappingId').val(mappingid);
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error in Retriving Opening Balance!", "Please retry, if problem persists contact support team", "error");
            }
        }
    });
}

$(document).ready(function () {
    $('#addUpdateOpenBalanceButton').unbind('click');
    $('#addUpdateOpenBalanceButton').on('click', function () {
        if ($.trim($('#inputOpeningBalance').val()) == '') {
            return;
        }
        var mappingid = $('#addTaxOpeningBalanceModal #coaMappingId').val();
        var useremail = $("#hiddenuseremail").text();
        var jsonData = {};
        jsonData.useremail = useremail;
        jsonData.id = $('#addTaxOpeningBalanceModal').data('tax-id');
        jsonData.openingBalance = $('#inputOpeningBalance').val();
        var url = "/tax/addUpdateBranchTax";
        if (mappingid === '57') {
            url = "/coa/saveInterBranchOpeningBalance";
        } else if (mappingid === '60' || mappingid === '61') {
            url = "/user/saveUserAdvClaim";
            jsonData.mappingId = mappingid;
        } else if (mappingid === '58' || mappingid === '59') {
            url = "/payroll/savePayrollOpeningBalance";
            jsonData.mappingId = mappingid;
        }

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
                $('#addTaxOpeningBalanceModal').modal('hide');
            },
            error: function (xhr, status, error) {
                if (xhr.status == 401) {
                    doLogout();
                } else if (xhr.status == 500) {
                    swal("Error on Updating Opening Balance!", "Please retry, if problem persists contact support team", "error");
                }
            }
        });
    });
});

function removeCOA(elem, identForDataValid) {
    var accountCode = $(elem).attr('id');
    $("li[id=" + accountCode + "]").find("ul[id='mainChartOfAccount']").remove();
    $(elem).attr("src", "/assets/images/new.v1370889834.png");
    $(elem).attr("onclick", "javascript:getChildChartOfAccount(this,'" + identForDataValid + "');");
}

function createCOA(elem, coaId, parentaccountCode, identForDataValid) {
    $('#item-form-container input[id="itemEntityHiddenId"]').val("");
    $('#item-form-container input[type="text"]').val("");
    $('#item-form-container input[type="hidden"]').val("");
    $('#item-form-container input[type="password"]').val("");
    $("#disableSpecfBtn").hide();
    $("#noOfDaysText").hide();
    $("#noOfDaysForCredit").hide();
    $("#item-form-container select[class='multiBranch'] option:selected").each(function () {
        $(this).removeAttr('selected');
    });
    $("#item-form-container select[id='partBasedTransaction'] option:selected").each(function () {
        $(this).removeAttr('selected');
    });
    $("#partBasedTransaction").multiselect('rebuild');
    $("#item-form-container select[class='multiBranch']").each(function () {
        $(this).multiselect('rebuild');
    });
    $('select').each(function () {
        $(this).val($('option:first').val());
    });
    $('#item-form-container #itemBranch2Btn').html('None Selected&nbsp;<b class="caret" style="margin: 5px auto;"></b>');
    $('#item-form-container input[class="itemBranch2Class-cb"]').each(function () {
        $(this).prop("checked", false);
    });
    addBranchForAddUpdateCoa(parentaccountCode);
    var coaName = $(elem).attr('name');
    $("#itemCategory").children().remove();
    $("#topLevelParentActCode").val(parentaccountCode);
    $(".newItemform-container").slideDown('slow');
    $('.newItemform-container input[type="text"]').val("");
    $('.newItemform-container textarea').val("");
    $("a[id='newItemform-container-close']").attr("href", location.hash);
    $("#itemCategory").append('<option value="' + coaId + '">' + coaName + '</option>');
    coaBasedOnParentAccounCode(parentaccountCode, coaId, identForDataValid);
    enableDisableCoaButton(true);
    var isSingleUserDeploy = $("#isDeploymentSingleUser").html();
    if (isSingleUserDeploy == "true") { // Single user only
        $('#item-form-container #itemBranch2BtnList li:first input[type="checkbox"]').trigger("click");
    }
}

/*function createbankCOA(elem, coaId, parentaccountCode, identForDataValid) {
    $('#item-form-container input[id="itemEntityHiddenId"]').val("");
    $('#item-form-container input[type="text"]').val("");
    $('#item-form-container input[type="hidden"]').val("");
    $('#item-form-container input[type="password"]').val("");
    $("#disableSpecfBtn").hide();
    $("#noOfDaysText").hide();
    $("#noOfDaysForCredit").hide();
    $("#item-form-container select[class='multiBranch'] option:selected").each(function () {
        $(this).removeAttr('selected');
    });
    $("#item-form-container select[id='partBasedTransaction'] option:selected").each(function () {
        $(this).removeAttr('selected');
    });
    $("#partBasedTransaction").multiselect('rebuild');
    $("#item-form-container select[class='multiBranch']").each(function () {
        $(this).multiselect('rebuild');
    });
    $('select').each(function () {
        $(this).val($('option:first').val());
    });
    $('#item-form-container #itemBranch2Btn').html('None Selected&nbsp;<b class="caret" style="margin: 5px auto;"></b>');
    $('#item-form-container input[class="itemBranch2Class-cb"]').each(function () {
        $(this).prop("checked", false);
    });
    addBranchForAddUpdateCoa(parentaccountCode);
    var coaName = $(elem).attr('name');
    $("#itemCategory").children().remove();
    $("#topLevelParentActCode").val(parentaccountCode);
    $(".newItemform-container").slideDown('slow');
    $('.newItemform-container input[type="text"]').val("");
    $('.newItemform-container textarea').val("");
    $("a[id='newItemform-container-close']").attr("href", location.hash);
    $("#itemCategory").append('<option value="' + coaId + '">' + coaName + '</option>');
    coaBasedOnParentAccounCode(parentaccountCode, coaId, identForDataValid);
    enableDisableCoaButton(true);
    var isSingleUserDeploy = $("#isDeploymentSingleUser").html();
    if (isSingleUserDeploy == "true") { // Single user only
        $('#item-form-container #itemBranch2BtnList li:first input[type="checkbox"]').trigger("click");
    }
}*/

function coaBasedOnParentAccounCode(parentaccountCode, coaId, identForDataValid) {
    //alert("coaBasedOnParentAccounCode");
    $('#chartOfAccountTable tbody td:nth-child(5)').children().show();
    $('#incomeHead').empty();
    $("#chartOfAccountTable thead[class='tablehead1'] tr th[id='isFixedAssetsth']").remove();
    $("#chartOfAccountTable thead[class='tablehead1'] tr th[id='isMovableImmovableth']").remove();
    $("#chartOfAccountTable thead[class='tablehead1'] tr th[id='isTaggableth']").remove();
    $("#chartOfAccountTable tbody tr td[id='isFixedAssetstd']").remove();
    $("#chartOfAccountTable tbody tr td[id='isMovableImmovabletd']").remove();
    $(".tdsSetupDiv").remove();
    $(".isVendTds").remove();
    $(".isCompItemDiv").remove();
    $("#chartOfAccountTable tbody tr td[id='isTaggabletd']").remove();
    $("#chartOfAccountTable tr td:nth-child(2) div[id='invoiceDescription']").html("");
    $("#tax-coa-container").hide();
    if (parentaccountCode == "1000000000000000000") {
        // $("#tax-coa-container").show();
        $(".forAssetLiability").each(function () {
            $(this).hide();
        });
        $("#forIncomesTh").text("");
        $("#forIncomesTh").each(function () {
            $(this).show();
        });

        checkIfCombinationSalesIncomeItem(coaId);

        $("#forOthersTh").each(function () {
            $(this).hide();
        });
        $("#forOthersTd").each(function () {
            $(this).hide();
        });
        $(".isEmpClaim").remove();
        $(".isInputTaxCredit").remove();
        $(".isLoanItem").remove();
        removeAllTransactionType();

        $("#partBasedTransaction").append('<option value="1">Sell on cash & collect payment now</option>');
        $("#partBasedTransaction").append('<option value="2">Sell on credit & collect payment later</option>');
        $("#partBasedTransaction").append('<option value="5">Receive payment from customer</option>');
        $("#partBasedTransaction").append('<option value="6">Receive advance from customer</option>');

        $("#partBasedTransaction").multiselect('rebuild');
        $("#chartOfAccountTable tr th:nth-child(7)").text("");
        $("#chartOfAccountTable tr td:nth-child(7)").html("");
        $("#chartOfAccountTable tr th:nth-child(8)").text("");
        $("#chartOfAccountTable tr td:nth-child(8)").html("");
        $("#chartOfAccountTable tr th:nth-child(9)").text("");
        $("#chartOfAccountTable tr td:nth-child(9)").html("");
        $("#customerVendor").show();
        $("#dynamicHeading1").show();
        $("#dynamicHeading2").show();
        $("#partBasedTransactiontd").show();
        $("#partBasedDynmDatatd").show();
        $("#partBasedTransactionth").show();
        $("#partBasedDynmDatath").show();
        $("#dynamicHeading1").text("Transaction Type");
        $("#dynamicHeading2").text("Domestic/International");
        $("#dynamicHeading2ImgId1").show();
        $("#dynamicHeading2ImgId2").hide();
        $("#dynamicHeading3ImgId1").hide();
        $("#dynamicCustVend2ImgId1").show();
        $("#dynamicCustVend2ImgId2").hide();
        $("#expItemDocUploadRule").hide();
        $("#partBasedDynmHeader").text("Add/Update Customer Details");
        $("#contractpopricelistth").text("Upload Customer Contract");
        $("#partBasedDynmData").children().remove();
        $("#partBasedDynmData").append('<option value="">--Please Select--</option></option><option value="1">Domestic</option><option value="2">International</option><option value="3">Both</option>');
        $("input[name='validityFrom']").remove();
        $("input[name='validityTo']").remove();
        $("input[name='specfPerUnitPrice']").remove();
        $(".discountPercentageSpan").remove();
        $("tr[id*='dynCustomerVendor']").remove();
        $("tr[id*='dynKnowledgeLibrary']").remove();

        //$('#chartOfAccountTable tbody td:nth-child(5)').children().hide();
        $('#item-form-container #itemBranch2BtnList').find("input[type='checkbox']").prop('checked', false);
        $('#itemBranch2Btn').html('None Selected&nbsp;<b class="caret" style="margin: 5px auto;"></b>');
        $('#incomeHead').html('/ Max. Discount (%) For Walkin Customer');
        $('.itemBranch2-input').val('0.0');
        $("#chartOfAccountTable tr th:nth-child(7)").text("");
        $("#chartOfAccountTable tr th:nth-child(7)").text("Map Income To Expense Item");
        $("#chartOfAccountTable tr td:nth-child(7)").html("");
        $("#chartOfAccountTable tr td:nth-child(7)").append('<select name="incomeToExpName" class="incomeToExpClass" id="incomeToExpId" onchange="getSelectedExpenseItemUnit(this);getBuyInventoryStockAvailable(this);"><option value="">--Please Select--</option></select>');
        $("#chartOfAccountTable tr th:nth-child(7)").append("</br>Map Expense to Income Units");
        $("#chartOfAccountTable tr td:nth-child(7)").append('</br><div class="mapExpenseToIncome" style="width:265px; top: 10px;">No.&nbsp;&nbsp;Expense Unit &nbsp;&nbsp;&nbsp; = &nbsp;&nbsp;&nbsp; No. &nbsp;&nbsp; Income Unit');
        $("#chartOfAccountTable tr td:nth-child(7)").append('<input style="width:30px;" type="text" name="noOfExpUnit" id="noOfExpUnit" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);"/>&nbsp;&nbsp&nbsp;&nbsp<input style="width:80px;padding-right: 15px;margin-left: -8px;" type="text" name="expUnitMeasure" id="expUnitMeasure" readonly="readonly"></input>&nbsp;&nbsp&nbsp;&nbsp=&nbsp;&nbsp&nbsp;&nbsp<input style="width:30px;" type="text" name="noOfIncUnit" id="noOfIncUnit" value="100" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);"/>&nbsp;&nbsp&nbsp;&nbsp<input style="width:80px;padding-right: 15px;margin-left: -8px;" type="text" name="incUnitMeasure" id="incUnitMeasure"></input>');
        $("#chartOfAccountTable tr td:nth-child(7)").append('<br/>Valuation Method');
        $("#chartOfAccountTable tr td:nth-child(7)").append('<br/><select name="tradingInvCalcMethod" class="tradingInvCalcMethod" id="tradingInvCalcMethod"><option value="FIFO">FIFO</option><option value="WAC">Weighted Average</option></select></div>');
        $("#chartOfAccountTable tr td:nth-child(2) div[id='invoiceDescription']").append('<input type="checkbox" id="itemInvoiceDesc1Check">Use Description1 in Invoice<br><input type="text" name="itemInvoiceDescription1" id="itemInvoiceDescription1" placeholder="Description 1">');

        $("#chartOfAccountTable tr td:nth-child(2) div[id='invoiceDescription']").append('</br><input type="checkbox" id="itemInvoiceDesc2Check">Use Description2 in Invoice</br><input type="text" name="itemInvoiceDescription2" id="itemInvoiceDescription2" placeholder="Description 2"></br><label style="display:inline-block;">Barcode</label><input type="text" class="itemBarcode" name="itemBarcodeInc" id="itemBarcodeInc" style="display: block;" onchange="checkBarcode(this);" placeholder="Barcode">');
        $(".isTranEditable").remove();
        $("#chartOfAccountTable tr td:nth-child(6)").append('<div class="isTranEditable"></br><input type="checkbox" id="isTranEditableCheck">Is Transaction Editable?</div>');
        fillExpenseItemToMapIncomeToExpense();
    } else if (parentaccountCode == "2000000000000000000") {
        $(".forAssetLiability").each(function () {
            $(this).hide();
        });
        $("#forIncomesTh").each(function () {
            $(this).hide();
        });
        $("#forIncomesTd").each(function () {
            $(this).hide();
        });
        $("#forIncomesExpenseTh").text("");
        $("#forIncomesExpenseTh").each(function () {
            $(this).show();
        });
        $("#forIncomesExpenseTh").text("GST Input");
        $("#forIncomesExpenseTd").html("");
        $("#forIncomesExpenseTd").each(function () {
            $(this).show();
        });
        $("#forIncomesExpenseTd").append('Goods/Services:');
        $("#forIncomesExpenseTd").append('<select name="GSTtypeOfSupply" id="GSTtypeOfSupply"><option>Please Select..</option><option value="1">Goods</option><option value="2">Services</option></select>');
        $("#forIncomesExpenseTd").append('Description:');
        $("#forIncomesExpenseTd").append('<div style="width:155px;"><input type="text" name="GSTDesc" id="GSTDesc"></input><button id="searchGSTItem" style="display: none;" class="searchGSTItem btn btn-submit btn-idos" onclick="searchGSTItemBasedOnDesc(this);" title="Search GST Items"><i class="fa fa-search pr-5"></i> Search</button></div>');
        $("#forIncomesExpenseTd").append('<div style="display: none;" id="GSTItemsList"></div>');
        $("#forIncomesExpenseTd").append('HSN/SAC Code:');
        $("#forIncomesExpenseTd").append('</br><input type="text" name="GSTCode" id="GSTCode" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);"></input>');
        $("#forIncomesExpenseTd").append('</br>Type of Goods/Services:');
        $("#forIncomesExpenseTd").append('</br><select name="GSTItemCategory" id="GSTItemCategory" onchange="onGSTCategoryChange(this);"><option value="">Please Select..</option><option value="1">GST Exempt Goods/Services</option><option value="2">Nil Rate Goods /Services</option><option value="3">Non GST Goods/ Services</option></select>');
        $("#forIncomesExpenseTd").append('</br>Input Tax on purchase of Goods/Service(%)');
        $("#forIncomesExpenseTd").append('</br><select multiple="multiple" name="GSTRateSelect" class="multiselect" onchange="onGSTRateSelect(this);" id="GSTRateSelect"><option value="0">0%</option><option value="5">5%</option><option value="12">12%</option><option value="18">18%</option><option value="28">28%</option><option value="0.125">0.125%</option><option value="OTHER">Any Other Rate</option></select>');
        $("#forIncomesExpenseTd").append('</br><input type="text"  style="display:none;" onkeypress="return onlyDotsAndNumbers(event);" id="GSTTaxRate" name="GSTTaxRate" placeholder="Enter GST Tax Rate">');
        $("#forIncomesExpenseTd").append('</br>Cess Tax On purchase of Goods/Service(%)');
        $("#forIncomesExpenseTd").append('</br><select multiple="multiple" name="CessRateSelect" class="multiselect" onchange="onCessRateSelect(this);" id="CessRateSelect"><option value="1">1%</option><option value="3">3%</option><option value="12">12%</option><option value="15">15%</option><option value="OTHER">Any Other Rate</option></select>');
        $("#forIncomesExpenseTd").append('</br><input type="text" style="display:none;"  onkeypress="return onlyDotsAndNumbers(event);" id="cessTaxRate" name="cessTaxRate" placeholder="Enter Cess Tax Rate">');
        $("#forOthersTh").each(function () {
            $(this).show();
        });
        $("#forOthersTd").each(function () {
            $(this).show();
        });
        $("#GSTRateSelect").multiselect({
            buttonWidth: '180px',
            maxHeight: 150,
            overflow: 'scroll',
            includeSelectAllOption: true,
            enableFiltering: true,
            onChange: function (element, checked) {
            }
        });
        $("#CessRateSelect").multiselect({
            buttonWidth: '180px',
            maxHeight: 150,
            overflow: 'scroll',
            includeSelectAllOption: true,
            enableFiltering: true,
            onChange: function (element, checked) {
            }
        });
        $(".isEmpClaim").remove();
        $(".isInputTaxCredit").remove();
        $(".isLoanItem").remove();
        $(".isTranEditable").remove();
        //$('#chartOfAccountTable tbody td:nth-child(5)').children().show();
        removeAllTransactionType();
        $("#partBasedTransaction").append('<option value="3">Buy on cash & pay right away</option>');
        $("#partBasedTransaction").append('<option value="4">Buy on credit & pay later</option>');
        $("#partBasedTransaction").append('<option value="11">Buy on Petty Cash Account</option>');
        $("#partBasedTransaction").append('<option value="7">Pay vendor/supplier</option>');
        $("#partBasedTransaction").append('<option value="8">Pay advance to vendor or supplier</option>');
        $("#partBasedTransaction").append('<option value="25">Transfer Inventory Item From One Branch To Another</option>');
        $("#partBasedTransaction").multiselect('rebuild');
        $("#partBasedTransactiontd").append('<div class="tdsSetupDiv"><button type="button" class="btn btn-submit" onclick="showTdsSetupPopup(this);">TDS Setup</button>&nbsp;&nbsp;<div class="dropdown"><button class="btn btn-primary dropdown-toggle" type="button" data-toggle="dropdown">TDS HISTORY<span class="caret"></span></button><ul class="dropdown-menu" id="tdsCoaHistoryList" style="max-height: 300px;overflow: scroll;"></ul></div></div>');
        $("#partBasedTransactiontd").append('<div class="isVendTds"><input type="checkbox" id="isTDsVendSpecfic">Is TDS Vendor Specific ?</div>');
        var isCompositionSchemeApply = $("#isCompositionSchemeApply").val();
        if (isCompositionSchemeApply == 1) {
            $("#partBasedTransactiontd").append('<div class="isCompItemDiv"><input type="checkbox" id="isCompositionItem">Is this item includible in Taxable items of Composition Scheme ?</div>');
        }
        $("#chartOfAccountTable tr td:nth-child(2)").append("<div class='isEmpClaim'>Is this eligible for expense claims? &nbsp;&nbsp;<i class='fa fa-info-circle pl-5 help-info userpopoverinfo' width='16px;' longdesc='System enables you to manage employee claims. Once you select an expense item to be eligible for expense claim, only such items will be listed for employees for making claims. Expenses that are not marked for employee claims will not be available for employees for making a claim.'></i><br/><select name='isItemEmpClaimItem' id='isItemEmpClaimItem'><option value='1'>Yes</option><option value='0'>No</option></select></br><label style='display: block;'>Barcode</label><input type='text' class='itemBarcode' name='itemBarcodeExp' style='display: block' id='itemBarcodeExp' onchange='checkBarcode(this);' placeholder='Barcode'></div>");
        $("#chartOfAccountTable tr td:nth-child(2)").append("<div class='isInputTaxCredit'>Is this eligible for input tax credit ? <br/><select name='isInputTaxCreditItem' id='isInputTaxCreditItem'><option value='1'>Yes</option><option value='0' selected>No</option></select></div>");
        $('.isEmpClaim').find('.userpopoverinfo').each(function () {
            var infocontent = $(this).attr('longdesc')
            var $this = $(this);
            $this.popover({
                trigger: 'manual',
                animate: false,
                placement: 'right',
                html: true,
                content: infocontent
            }).on("mouseenter", function () {
                $(this).popover("show");
                $(this).siblings(".popover").on("mouseleave", function () {
                    $(this).hide();
                });
            }).on("mouseleave", function () {
                var _this = this;
                setTimeout(function () {
                    if (!$(".popover:hover").length) {
                        $(_this).popover("hide");
                    }
                }, 100);
            });
        });


        if (identForDataValid == 24 || identForDataValid == 25 || identForDataValid == 26 || identForDataValid == 27) {
            $("#chartOfAccountTable tr th:nth-child(7)").text("");
            $("#chartOfAccountTable tr td:nth-child(7)").html("");
            $("#chartOfAccountTable tr th:nth-child(7)").hide();
            $("#chartOfAccountTable tr td:nth-child(7)").hide();
        } else {
            $("#chartOfAccountTable tr th:nth-child(7)").text("");
            $("#chartOfAccountTable tr td:nth-child(7)").html("");
            $("#chartOfAccountTable tr th:nth-child(7)").text("Inventory Unit/Rate");
            //$("#chartOfAccountTable tr td:nth-child(7)").html('<select name="withHoldingApplicable" id="withHoldingApplicable">'+
            //'<option value="1">Yes</option><option value="0">No</option></select>');
            $("#chartOfAccountTable tr td:nth-child(7)").append('Unit of Measure');
            $("#chartOfAccountTable tr td:nth-child(7)").append('<input type="text" name="expUnitMeasure" id="expUnitMeasure" value="E.g. Carton"></input>');
            $("#chartOfAccountTable tr td:nth-child(7)").append('<br/>Total No. Of Units');
            $("#chartOfAccountTable tr td:nth-child(7)").append('<br/><input type="text" name="expNoOfOpeningBalUnits" id="expNoOfOpeningBalUnits" value="0" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateExpenseItemOpeningBal(this);" readonly/>');
            $("#chartOfAccountTable tr td:nth-child(7)").append('<br/>RATE');
            $("#chartOfAccountTable tr td:nth-child(7)").append('<br/><input type="text" name="expRateOpeningBalUnits" id="expRateOpeningBalUnits" value="0" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateExpenseItemOpeningBal(this);" readonly/>');
            $("#chartOfAccountTable tr td:nth-child(7)").append('<br/>Total Inventory Opening Bal');
            $("#chartOfAccountTable tr td:nth-child(7)").append('<br/><input type="text" name="expOpeningBal" id="expOpeningBal" value="0" readonly="readonly" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateExpenseItemOpeningBal(this);" readonly/>');
        }
        $("#chartOfAccountTable tr th:nth-child(8)").html("");
        $("#chartOfAccountTable tr th:nth-child(8)").html("Withholding Rate/Capture Input Taxes &nbsp;&nbsp;<i class='fa fa-info-circle pl-5 help-info userpopoverinfowtinfo' longdesc='System lets you set up Withholding taxes (Tax deducted at source) on payments if such taxes are applicable for such payments. In the box provided below, please enter the rate at which withholding tax is applicable for this item of expense. In the column to the right, please enter transaction limit, if such tax is applicable only if the transaction exceeds the limit specified. For example, if the tax is applicable only if the amount of transaction exceeds $2000, then enter 2000 in the box below <b>Withholding transaction limit</b>. Further if the tax is applicable only if the payments to a specific vendor exceed a particular limit, then enter such monetary limit in the box provided below <b>Withholding monetary limit</b>. For example, if the tax is required to be deducted only if total purchases from a vendor exceed $5000, then it will automatically apply withholding tax on payments to the vendor of this item of purchase after purchases from this vendor exceed $5000.'></i>");
        $('#chartOfAccountTable tr th:nth-child(8)').find('.userpopoverinfowtinfo').each(function () {
            var infocontent = $(this).attr('longdesc')
            var $this = $(this);
            $this.popover({
                trigger: 'manual',
                animate: false,
                placement: 'right',
                html: true,
                content: infocontent
            }).on("mouseenter", function () {
                $(this).popover("show");
                $(this).siblings(".popover").on("mouseleave", function () {
                    $(this).hide();
                });
            }).on("mouseleave", function () {
                var _this = this;
                setTimeout(function () {
                    if (!$(".popover:hover").length) {
                        $(_this).popover("hide");
                    }
                }, 100);
            });
        });
        // Changes for TDS
        $("#chartOfAccountTable tr th:nth-child(8)").hide();
        $("#chartOfAccountTable tr th:nth-child(9)").hide();
        $("#chartOfAccountTable tr td:nth-child(8)").hide();
        $("#chartOfAccountTable tr td:nth-child(9)").hide();
        //
        $("#chartOfAccountTable tr td:nth-child(8)").html("");
        $("#chartOfAccountTable tr td:nth-child(8)").html("Is Withholding Applicable?");
        $("#chartOfAccountTable tr td:nth-child(8)").append('<select name="withHoldingApplicable" id="withHoldingApplicable">' +
            '<option value="0">No</option><option value="1">Yes</option></select>');
        $("#chartOfAccountTable tr td:nth-child(8)").append("Withholding Types");
        $("#chartOfAccountTable tr td:nth-child(8)").append('<select name="withHoldingType" id="withHoldingType">' +
            '<option value="">--Please Select--</option><option value="31">Sec192-Payment of Salary</option><option value="32">Sec194A-Income by way of Interest other than Interest on Securities</option>' +
            '<option value="33">Sec194C-Payment to Contractors/SubContractors - Individuals / HUF</option><option value="34">Sec194C-Payment to Contractors/SubContractors - Others</option>' +
            '<option value="35">Sec194H-Commission or Brokerage</option><option value="36">Sec194-I-Rent-(a) Plant and Machinery</option>' +
            '<option value="37">Sec194-I-Rent-(b)-Land or building or furniture or fitting</option><option value="38">Sec-194J-Fees for Professional/Technical Service etc.</option></select>');
        $("#chartOfAccountTable tr td:nth-child(8)").append('<br/>Rate in %');
        $("#chartOfAccountTable tr td:nth-child(8)").append('<br/><input name="withHoldingRate" id="withHoldingRate" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);" style="padding-right: 15px;" type="text"><br/>Capture Input Taxes&nbsp;<i class="fa fa-info-circle pl-5 help-info userpopoverinfocit" longdesc="If you want your staff to capture input taxes (as per supplier invoice) for purchase of this item, then select <b>Yes</b>. When users process purchase of this item, it will provide them with fields where they can provide details of input taxes as per supplier invoice. This will help you in month end reconciliation and also for filing taxes."></i><br/>' +
            '<select name="captureInputTaxesApplicable" id="captureInputTaxesApplicable"><option value="1">Yes</option><option value="0">No</option></select>');
        $('.userpopoverinfocit').each(function () {
            var infocontent = $(this).attr('longdesc')
            var $this = $(this);
            $this.popover({
                trigger: 'manual',
                animate: false,
                placement: 'right',
                html: true,
                content: infocontent
            }).on("mouseenter", function () {
                $(this).popover("show");
                $(this).siblings(".popover").on("mouseleave", function () {
                    $(this).hide();
                });
            }).on("mouseleave", function () {
                var _this = this;
                setTimeout(function () {
                    if (!$(".popover:hover").length) {
                        $(_this).popover("hide");
                    }
                }, 100);
            });
        });
        $("#chartOfAccountTable tr th:nth-child(9)").text("");
        $("#chartOfAccountTable tr th:nth-child(9)").text("Withholding limits");
        $("#chartOfAccountTable tr td:nth-child(9)").html("");
        $("#chartOfAccountTable tr td:nth-child(9)").html('<input title="Withholding Transaction Limit" placeholder="Withholding Transaction Limit" name="withHoldingLimit" id="withHoldingLimit" type="text" class="m-bottom-10" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);"><br/><input type="text" title="Withholding Monetary Limit" placeholder="Withholding Monetary Limit" name="withHoldingMonetoryLimit" id="withHoldingMonetoryLimit" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);">');
        $("#customerVendor").show();
        $("#dynamicHeading1").show();
        $("#dynamicHeading2").show();
        $("#expItemDocUploadRule").show();
        $('input[id*="monetoryLimit"]').each(function () {
            $(this).val("0.0");
        });
        $('input[name="customdoccheckBranch"]').each(function () {
            $(this).prop("checked", false);
        });
        $("#docuploadrulecustomdropdown").text("None Selected");
        $("#docuploadrulecustomdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
        $("#partBasedTransactiontd").show();
        $("#partBasedDynmDatatd").show();
        $("#partBasedTransactionth").show();
        $("#partBasedDynmDatath").show();
        $("#dynamicHeading2ImgId1").hide();
        $("#dynamicHeading2ImgId2").show();
        $("#dynamicCustVend2ImgId2").show();
        $("#dynamicCustVend2ImgId1").hide();
        $("#dynamicHeading3ImgId1").show();
        $("#expSpecfUploadDocMonetoryLimit").val("");
        $("#docuploadmandatory option:first").prop("selected", "selected");
        $("select[name='docUploadMandatoryInBranches'] option:selected").each(function () {
            $(this).removeAttr('selected');
        });
        $("select[name='docUploadMandatoryInBranches']").multiselect('rebuild');
        $("#dynamicHeading1").text("Transaction Type");
        $("#dynamicHeading2").text("Capital/Revenue");
        $("#partBasedDynmHeader").text("Add/Update Vendor Details");
        $("#partBasedDynmData").children().remove();
        $("#partBasedDynmData").append('<option value="">--Please Select--</option><option value="1">Capital</option><option value="2">Revenue</option>');
        $('.userpopoverinfoupbv').each(function () {
            var infocontent = $(this).attr('longdesc')
            var $this = $(this);
            $this.popover({
                trigger: 'manual',
                animate: false,
                placement: 'right',
                html: true,
                content: infocontent
            }).on("mouseenter", function () {
                $(this).popover("show");
                $(this).siblings(".popover").on("mouseleave", function () {
                    $(this).hide();
                });
            }).on("mouseleave", function () {
                var _this = this;
                setTimeout(function () {
                    if (!$(".popover:hover").length) {
                        $(_this).popover("hide");
                    }
                }, 100);
            });
        });
        $("tr[id*='dynCustomerVendor']").remove();
        $("tr[id*='dynKnowledgeLibrary']").remove();
        $("input[name='validityFrom']").remove();
        $("input[name='validityTo']").remove();
        $("input[name='specfPerUnitPrice']").remove();

        var maxYear = new Date().getFullYear() + 30;
        $(function () {
            $(".datepicker").datepicker({
                changeMonth: true,
                changeYear: true,
                dateFormat: 'MM d,yy',
                yearRange: '' + new Date().getFullYear() - 100 + ':' + maxYear + '',
                onSelect: function (x, y) {
                    $(this).focus();
                }
            });
            $(".datepicker").bind("keypress", function (event) {
                var charCode = event.which;
                var keyChar = String.fromCharCode(charCode);
                return /[]/.test(keyChar);
            });
        });

        var isCompositionSchemeApply = $("#isCompositionSchemeApply").val();
        if (isCompositionSchemeApply == 1) {
            $("#isInputTaxCreditItem").prop('disabled', true);
        }

    } else if (parentaccountCode == "3000000000000000000" || parentaccountCode == "4000000000000000000") {
        $("#forIncomesExpenseTh").each(function () {
            $(this).hide();
        });
        $("#forIncomesExpenseTd").each(function () {
            $(this).hide();
        });
        $(".forAssetLiability").each(function () {
            $(this).hide();
        });
        $("#forIncomesTh").each(function () {
            $(this).hide();
        });
        $("#forIncomesTd").each(function () {
            $(this).hide();
        });
        $("#forOthersTh").each(function () {
            $(this).show();
        });
        $("#forOthersTd").each(function () {
            $(this).show();
        });
        $(".forAssetLiability").each(function () {
            $(this).show();
        });
        $(".isEmpClaim").remove();
        $(".isInputTaxCredit").remove();
        //$(".isLoanItem").remove();
        $("#chartOfAccountTable tr th:nth-child(7)").text("");
        $("#chartOfAccountTable tr td:nth-child(7)").html("");
        $("#chartOfAccountTable tr th:nth-child(8)").text("");
        $("#chartOfAccountTable tr td:nth-child(8)").html("");
        $("#chartOfAccountTable tr th:nth-child(9)").text("");
        $("#chartOfAccountTable tr td:nth-child(9)").html("");
        $("#customerVendor").hide();
        $("#dynamicHeading1").text("Transaction Type");
        $("#dynamicHeading1").show(); // Sunil
        $("#dynamicHeading2").hide();
        $("#expItemDocUploadRule").hide();
        $("#partBasedTransactiontd").show(); // Sunil
        $("#partBasedDynmDatatd").hide();
        $("#partBasedTransactionth").show(); //sunil
        $("#partBasedDynmDatath").hide();

        /*if((identForDataValid == 4) || (identForDataValid == 5)){
            $("#coaBranchbankSetup").show();
            $("#addSpecfBrnchBtn").show();
            $("#addSpecfBtn").hide();
            $(".isTranEditable").hide();
            if(identForDataValid == 4){
                $("#bnkActType option[value='3']").prop("selected", "selected");
            }else{
                $("#bnkActType option[value='7']").prop("selected", "selected");
            }
        }else{
            $("#coaBranchbankSetup").hide();
            $("#addSpecfBrnchBtn").hide();
            $("#addSpecfBtn").show();
        }*/
    }

    if (parentaccountCode == "3000000000000000000") {
        removeAllTransactionType();
        $("#partBasedTransaction").append('<option value="1">Sell on cash & collect payment now</option>');
        $("#partBasedTransaction").append('<option value="2">Sell on credit & collect payment later</option>');
        $("#partBasedTransaction").append('<option value="3">Buy on cash & pay right away</option>');
        $("#partBasedTransaction").append('<option value="4">Buy on credit & pay later</option>');
        $("#partBasedTransaction").append('<option value="5">Receive payment from customer</option>');
        $("#partBasedTransaction").append('<option value="6">Receive advance from customer</option>');
        $("#partBasedTransaction").append('<option value="7">Pay vendor/supplier</option>');
        $("#partBasedTransaction").append('<option value="8">Pay advance to vendor or supplier</option>');
        $("#partBasedTransaction").append('<option value="11">Buy on Petty Cash Account</option>');
        $("#partBasedTransaction").append('<option value="14">Transfer main cash to petty cash</option>');
        $("#partBasedTransaction").append('<option value="22">Withdraw Cash From Bank</option>');
        $("#partBasedTransaction").append('<option value="23">Deposit Cash In Bank</option>');
        $("#partBasedTransaction").append('<option value="24">Transfer Funds From One Bank To Another</option>');
        $("#partBasedTransaction").append('<option value="25">Transfer Inventory Item From One Branch To Another</option>');
        $("#partBasedTransaction").multiselect('rebuild');

    } else if (parentaccountCode == "4000000000000000000") {
        removeAllTransactionType();
        $("#partBasedTransaction").append('<option value="7">Pay vendor/supplier</option>');
        $("#partBasedTransaction").append('<option value="8">Pay advance to vendor or supplier</option>');
        $("#partBasedTransaction").append('<option value="24">Transfer Funds From One Bank To Another</option>');
        $("#partBasedTransaction").multiselect('rebuild');
    }
    var isSingleUserDeploy = $("#isDeploymentSingleUser").html();
    if (isSingleUserDeploy == "true") { // Single user only
        $("#partBasedTransaction option").attr("selected", "selected");
        $("#partBasedTransaction").multiselect('rebuild');
    }
    if ('1000000000000000000' == parentaccountCode) {
        $("#chartOfAccountTable tr th:nth-child(8)").html("");
        $("#chartOfAccountTable tr th:nth-child(8)").html("Item Stock Available/<br/>Reorder Level");
        $("#chartOfAccountTable tr td:nth-child(8)").html("");
        $("#chartOfAccountTable tr td:nth-child(8)").append('<input type="text" style="width:150px;" name="incomeItemStockAvailable" id="incomeItemStockAvailableId" class="incomeItemStockAvailableClass" readonly="readonly">' +
            '<br/><div class="btn-group m-bottom-10" style="margin-top:10px;"><button id="reorderleveldropdown" class="multiselect dropdown-toggle btn" style="width: 150px;">None Selected<b class="caret"></b></button>' +
            '<div id="reorderleveldropdown-menuid" class="reorderleveldropdown-menu">' +
            '<ul id="reorderLevelBranchList"><li id="reorderlevelbranchlist">&nbsp;<input style="margin-bottom:5px;" type="checkbox" id="branchcheckboxid" name="branchcheck" value="" onClick="branchcheckUncheck(this)"/>&nbsp;Select All</li></ul>' +
            '</div></div>');
        //reorderleveldropdown button click start
        $(function () {
            $('#reorderleveldropdown').bind("click", function (event) {
                var classval = $(this).attr('class');
                if (classval == "multiselect dropdown-toggle btn") {
                    var newclassval = classval + " " + "open";
                    $(this).attr('class', newclassval);
                    var divdropdown = "openreorderleveldropdown-menu"
                    $(".reorderleveldropdown-menu").attr('class', divdropdown);
                }
                if (classval == "multiselect dropdown-toggle btn open") {
                    var newclassval = "multiselect dropdown-toggle btn";
                    $(this).attr('class', newclassval);
                    var divdropdown = "reorderleveldropdown-menu"
                    $(".openreorderleveldropdown-menu").attr('class', divdropdown);
                }
            });
            $('#knowledgeLibraryInBranches option').each(function () {
                if ($(this).val() != "multiselect-all" && $(this).val() != "") {
                    $("#reorderLevelBranchList").append('<li id="reorderlevelbranchlist">&nbsp;<input style="margin-bottom:5px;" type="checkbox" id="branchcheckboxid' + $(this).val() + '" name="branchcheck1" value=' + $(this).val() + ' onClick="branchcheckUncheck(this)"/>&nbsp;<span id="bnchtext" title=' + $(this).text() + '>' + $(this).text() + '</span><select style="margin-left:-70px;" name="reorderlevelselectalertuser" id="reorderlevelselectalertuserid' + $(this).val() + '" ><option value="" >Select Alert User </option></select> &nbsp;<input type="text" name="reorderlevelinput" id="reorderlevelinputid' + $(this).val() + '" style="width:110px;" placeholder="Reorder Level" onkeypress="return isNumber(event)" onkeyup="toggleBranchCheck(this);"/></li>');
                }
            });
            console.log("Form COA");
            var jsonData = {};
            jsonData.useremail = $("#hiddenuseremail").text();
            var url = "/config/allUsers";
            $.ajax({
                url: url,
                data: JSON.stringify(jsonData),
                type: "text",
                method: "POST",
                headers: {
                    "X-AUTH-TOKEN": window.authToken
                },
                contentType: 'application/json',
                async: false,
                success: function (data) {
                    $("#usersTable tbody").html("");
                    var html = '<option value="">Select Alert User</option>';
                    for (var i = 0; i < data.userListData.length; i++) {
                        html += '<option value="' + data.userListData[i].userEmail + '">' + data.userListData[i].fullName + ' (' + data.userListData[i].userEmail + ')</option>';
                    }
                    $('select[name="reorderlevelselectalertuser"]').html(html);
                },
                error: function (xhr, status, error) {
                    if (xhr.status == 401) {
                        doLogout();
                    }
                }
            });
        });
        //reorderleveldropdown button click stop
        var check_box_values = $('input[name="branchcheck1"]:checkbox:checked').map(function () {
            return this.value;
        }).get();
        var length = check_box_values.length;
        if (length > 0) {
            var text = length + " " + "Items Selected";
            $("#reorderleveldropdown").text(text);
            $("#reorderleveldropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
        }
        if (check_box_values == 0) {
            $("#reorderleveldropdown").text("None Selected");
            $("#reorderleveldropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
        }
    }
    if (identForDataValid == 24 || identForDataValid == 25 || identForDataValid == 26 || identForDataValid == 27) {
        if (parentaccountCode == "1000000000000000000" || parentaccountCode == "3000000000000000000" || parentaccountCode == "4000000000000000000") {
            $(".isInputTaxCredit").remove();
            $("#chartOfAccountTable tr td:nth-child(2)").append("<div class='isInputTaxCredit'>Is this eligible for input tax credit ? <br/><select name='isInputTaxCreditItem' id='isInputTaxCreditItem'><option value='1'>Yes</option><option value='0' selected>No</option></select></div>");
        }
        $(".customerVendorBranchCls").find("input[type='text']").hide();
    }
    alwaysScrollTop();
}

function checkBarcode(elem) {
    var barcodeElemId = $(elem).attr('id');
    var barcodeNo = $(elem).val();
    var jsonData = {};
    jsonData.barcodeElemId = barcodeElemId;
    jsonData.barcodeNo = barcodeNo;

    var url = "/specifics/checkBarcode";
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
            //alert("data.barcodeExistsData[0].barcodeMessage:::"+data.barcodeExistsData[0].barcodeMessage);
            if (data.barcodeExistsData[0].barcodeMessage == "Barcode is present") {
                // alert("Barcode already used. Please enter a different barcode.");
                swal("Duplicate data error!", "Barcode already used. Please enter a different barcode.", "error");
                //$(elem).val("");
                $(elem).focus();
            }
        }, error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        }
    });
}

function calculateExpenseItemOpeningBal(elem) {
    var expNoOfOpeningBalUnits = $('#expNoOfOpeningBalUnits').val();
    var expRateOpeningBalUnits = $('#expRateOpeningBalUnits').val();
    var openingBal = expNoOfOpeningBalUnits * expRateOpeningBalUnits;
    if (isNaN(openingBal)) {
        swal("Error!","Calculated opening balance for Inventory is not valid, new value will not be applied","error");
        return false;
    } else {
        $("#expOpeningBal").val(openingBal);
    }
}

function checkIfCombinationSalesIncomeItem(coaId) {
    var jsonData = {};
    jsonData.coaId = coaId;
    var url = "/config/checkIfCombinationSalesIncomeItem"
    $.ajax({
        url: url,
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        data: JSON.stringify(jsonData),
        type: "text",
        method: "POST",
        async: false,
        contentType: 'application/json',
        success: function (data) {
            if (data.combinationSalesData[0].isCombinationSales == "1") {
                var incomeItemsListTemp = "<option value='-1' units='-1' id='-1'>Please Select..</option>";
                for (var i = 0; i < data.incomeSepecificsData.length; i++) {
                    incomeItemsListTemp += ('<option value="' + data.incomeSepecificsData[i].id + '" units="' + data.incomeSepecificsData[i].openBalUnits + '" rates="' + data.incomeSepecificsData[i].openBalRate + '">' + data.incomeSepecificsData[i].name + '</option>');
                }
                $("#forIncomesTh").text("Select CombinationSales Items");
                $("#forIncomesTd").html("");
                $("#forIncomesTd").each(function () {
                    $(this).show();
                });
                var customerGstinTblTrData = "";
                customerGstinTblTrData += ('<button id="addnewItemForCombSales" class="addnewItemForCombSales" onclick="addnewItemForCombinationSales(this);">Add More</button>');
                customerGstinTblTrData += ('<div id="combinationSalesDiv" style="width:189px; margin-top: 5px; ">');
                customerGstinTblTrData += ('<table class="table excelFormTable table-hover table-striped" id="combSalesTbl">');
                customerGstinTblTrData += ('<thead class="head1"><tr><th style="width:100px">Item</th><th style="width:40px">Units</th><th style="width:40px">Rate</th></tr></thead>');
                customerGstinTblTrData += ('<tbody></tbody></table></div>');
                $("#forIncomesTd").append(customerGstinTblTrData);

                $("#combSalesTbl tbody").append('<tr id="combSales0"><td><select style="width:100px" id="incomeItemsForCombSales" name="incomeItemsForCombSales" onchange="getItemsPriceAndUnits(this)"></td><td><input style="width:40px"  type="text" name="openBalUnitsForCombSales" id="openBalUnitsForCombSales" onkeyup="calculateCombSalesItemPrice(this);"></input></td><td><input style="width:60px" type="text"  name="openBalRateForCombSales" id="openBalRateForCombSales" onkeyup="calculateCombSalesItemPrice(this);"></input></td></tr>');
                $("#combSalesTbl > tbody > tr[id='combSales0'] > td > select[id=incomeItemsForCombSales]").append(incomeItemsListTemp);

                $("#forIncomesExpenseTh").text("");
                $("#forIncomesExpenseTh").each(function () {
                    $(this).show();
                });
                $("#forIncomesExpenseTh").text("Price Per Unit");
                $("#forIncomesExpenseTd").html("");
                $("#forIncomesExpenseTd").each(function () {
                    $(this).show();
                });
                $("#forIncomesExpenseTd").append('<input type="text" value="1" style="margin-left: 5px; " name="incomespecfPerUnitPrice" id="incomespecfPerUnitPrice" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);" readonly></input>');
            } else {
                $("#forIncomesTh").text("Price Per Unit");
                $("#forIncomesTd").html("");
                $("#forIncomesTd").each(function () {
                    $(this).show();
                });
                $("#forIncomesTd").append('<input type="text" value="1" name="incomespecfPerUnitPrice" id="incomespecfPerUnitPrice" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);"></input>');
                $("#forIncomesTd").append('</br><input type="checkbox" id="itemPriceInclusive">Is Price Inclusive Of Tax');
                $("#forIncomesExpenseTh").text("");
                $("#forIncomesExpenseTh").each(function () {
                    $(this).show();
                });
                $("#forIncomesExpenseTh").text("GST Input");
                $("#forIncomesExpenseTd").html("");
                $("#forIncomesExpenseTd").each(function () {
                    $(this).show();
                });
                $("#forIncomesExpenseTd").append('Goods/Services:');
                $("#forIncomesExpenseTd").append('<select name="GSTtypeOfSupply" id="GSTtypeOfSupply"><option>Please Select..</option><option value="1">Goods</option><option value="2">Services</option></select>');
                $("#forIncomesExpenseTd").append('Description:');
                /*$("#forIncomesExpenseTd").append(`<div class="btn-group" id="GSTtypeOfSupply">
                                        <button id="itemGST2Btn" class="multiselect dropdown-toggle btn" onclick="customClaimsDropDownToggle('itemGST2Btn-menuid')">None Selected<b class="caret" style="margin:5px auto;"></b></button> 
                                        <div id="itemGST2Btn-menuid" class="statutoryidnumberdropdown-menu" style="width: 150px;">
                                        <ul id ="itemGST" class="no-bullets">
                                        <li> Goods/ Services <b style="float:right"> GST on Adv</b></li>
                                        <br/>
                                        <li id="itemGST2BtnList" style="padding-top:4px">
                                        <input id ="goodsCheckbox1" type ="checkbox" style="margin-bottom:5px;" onclick="handleCheckboxClick('goodsCheckbox1')"/>Goods  
                                        <b style="float:right">
                                        <input  id = "goodsCheckbox2" type ="checkbox" style="margin-bottom:5px;" onclick="handleCheckboxClick('goodsCheckbox2')" /> 
                                        </b>
                                        </li> 
                                        <li id="itemGST2BtnList1">
                                        <input  id = "servicesCheckbox1"  type ="checkbox" style="margin-bottom:5px;"  onclick="handleCheckboxClick('servicesCheckbox1')"/>Services 
                                        <b style="float:right">
                                        <input   id = "servicesCheckbox2" type ="checkbox" style="margin-bottom:5px;"  onclick="handleCheckboxClick('servicesCheckbox2')"/>
                                        </b>
                                        </li>
                                        </ul></div></div>`);      
                $("#forIncomesExpenseTd").append('<p style="margin: 0;padding-top:7px;">Description:<p>');*/
                $("#forIncomesExpenseTd").append('<div style="width:155px;"><input type="text" name="GSTDesc" id="GSTDesc"></input><button id="searchGSTItem" style="display:none;" class="searchGSTItem btn btn-submit btn-idos" onclick="searchGSTItemBasedOnDesc(this);" title="Search GST Items"><i class="fa fa-search pr-5"></i> Search</button></div>');
                $("#forIncomesExpenseTd").append('<div style="display: none;" id="GSTItemsList"></div>');
                $("#forIncomesExpenseTd").append('HSN/SAC Code:');
                $("#forIncomesExpenseTd").append('</br><input type="text" name="GSTCode" id="GSTCode" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);"></input>');
                $("#forIncomesExpenseTd").append('</br>Date of tax applicability:');
                $("#forIncomesExpenseTd").append('</br><input class="applicableDate" id="applicableDate" type="text" name="applicableDate" placeholder="Date">');
                $("#forIncomesExpenseTd").append('</br><div class="dropdown"><button class="btn btn-primary dropdown-toggle" type="button" data-toggle="dropdown">TAX HISTORY<span class="caret"></span></button><ul class="dropdown-menu" id="taxHistoryList" style="max-height: 300px;overflow: scroll;"></ul></div>');
                $("#forIncomesExpenseTd").append('</br>Type of Goods/Services:');
                $("#forIncomesExpenseTd").append('</br><select name="GSTItemCategory" id="GSTItemCategory" onchange="onGSTCategoryChange(this);"><option value="" >Please Select..</option><option value="1">GST Exempt Goods/Services</option><option value="2">Nil Rate Goods /Services</option><option value="3">Non GST Goods/ Services</option></select>');
                $("#forIncomesExpenseTd").append('</br>Output Tax on sale of Goods/Service(%)');
                $("#forIncomesExpenseTd").append('</br><input type="text" onkeypress="return onlyDotsAndNumbers(event);" onchange="onTaxRateChange(this);" id="GSTTaxRate" name="GSTTaxRate">');
                $("#forIncomesExpenseTd").append('</br>Cess Tax on sale of Goods/Service(%)');
                $("#forIncomesExpenseTd").append('</br><input type="text" onkeypress="return onlyDotsAndNumbers(event);" onchange="onCessRateChange(this);" id="cessTaxRate" name="cessTaxRate">');
                $("#applicableDate").datepicker({
                    changeMonth: true,
                    changeYear: true,
                    dateFormat: 'MM d,yy',
                    yearRange: '' + new Date().getFullYear() - 100 + ':' + maximumYear + '',
                    onSelect: function (x, y) {
                        $(this).focus();
                    }
                });
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching Transaction purposes!", "Please retry, if problem persists contact support team", "error");
            }
        }
    });
}

function getItemsPriceAndUnits(elem) {
    var parentTr = $(elem).closest('tr').attr('id');
    var value = $(elem).val();
    var rate = $("#combSalesTbl > tbody > tr[id='" + parentTr + "'] > td > select[id=incomeItemsForCombSales] option:selected").attr("rates");
    var units = $("#combSalesTbl > tbody > tr[id='" + parentTr + "'] > td > select[id=incomeItemsForCombSales] option:selected").attr("units");
    $("#" + parentTr + " input[id='openBalRateForCombSales']").val(rate);
    $("#" + parentTr + " input[id='openBalUnitsForCombSales']").val(units);
    calculateCombSalesItemPrice(elem);
}

function calculateCombSalesItemPrice(elem) {
    var combination_sales_units = $('input[id="openBalUnitsForCombSales"]').map(function () {
        return this.value;
    }).get();
    var combination_sales_rates = $('input[id="openBalRateForCombSales"]').map(function () {
        return this.value;
    }).get();
    var price = 0;
    for (var i = 0; i < combination_sales_rates.length; i++) {
        var rate = 0;
        var units = 0;
        if (combination_sales_rates[i] != "") {
            rate = combination_sales_rates[i];
        }
        if (combination_sales_units[i] != "") {
            units = combination_sales_units[i];
        }
        price = price + rate * units;
    }
    $("input[id='incomespecfPerUnitPrice']").val(price);
}

function addnewItemForCombinationSales(elem) {
    var jsonData = {};
    jsonData.useremail = $("#hiddenuseremail").text();
    var url = "/data/getcoaincomeitems";
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        async: false,
        method: "GET",
        contentType: 'application/json',
        success: function (data) {
            var incomeItemsListTemp = "<option value='-1' units='-1' id='-1'>Please Select..</option>";
            for (var i = 0; i < data.coaItemData.length; i++) {
                incomeItemsListTemp += ('<option value="' + data.coaItemData[i].id + '" units="' + data.coaItemData[i].openBalUnits + '" rates="' + data.coaItemData[i].openBalRate + '">' + data.coaItemData[i].name + '</option>');
            }
            var length = $("#combSalesTbl tbody tr").length;
            customerGstinTblTrData = "";
            customerGstinTblTrData += ('<tr id="combSales' + length + '"><td><select style="width:100px" id="incomeItemsForCombSales" name="incomeItemsForCombSales" onchange="getItemsPriceAndUnits(this)"></td>');
            customerGstinTblTrData += ('<td><input style="width:40px"  type="text"  name="openBalUnitsForCombSales" id="openBalUnitsForCombSales" onkeyup="calculateCombSalesItemPrice(this);"></td><td><input style="width:60px"  type="text"  name="openBalRateForCombSales" id="openBalRateForCombSales" onkeyup="calculateCombSalesItemPrice(this);"></td></tr>');

            $("#combSalesTbl tbody").append(customerGstinTblTrData);
            $("#combSalesTbl > tbody > tr[id='combSales" + length + "'] > td > select[id=incomeItemsForCombSales]").append(incomeItemsListTemp);
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        }
    });
}

function getSelectedExpenseItemUnit(elem) {
    var useremail = $("#hiddenuseremail").text();
    var jsonData = {};
    jsonData.email = useremail;
    jsonData.incometoexpensemapping = $("#incomeToExpId option:selected").val();
    var url = "/config/getSelectedExpenseItemUnit";
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
            $("#expUnitMeasure").val(data.expenseSepecificsData[0].expUnitMeasure);
            $('#expUnitMeasure').prop('readonly', true);
            if (data.expenseSepecificsData[0].noOfExpUnit != null) {
                $("#noOfExpUnit").val(data.expenseSepecificsData[0].noOfExpUnit);
                $('#noOfExpUnit').prop('readonly', true);
            }
            if (data.expenseSepecificsData[0].noOfIncUnit != null) {
                $("#noOfIncUnit").val(data.expenseSepecificsData[0].noOfIncUnit);
                $('#noOfIncUnit').prop('readonly', true);
            }
            if (data.expenseSepecificsData[0].incUnitMeasure != null) {
                $("#incUnitMeasure").val(data.expenseSepecificsData[0].incUnitMeasure);
                $('#incUnitMeasure').prop('readonly', true);
            }
            if (data.expenseSepecificsData[0].calcMethod != null) {
                $("#tradingInvCalcMethod").find("option[value='" + data.expenseSepecificsData[0].calcMethod + "']").prop("selected", "selected");
                $("#tradingInvCalcMethod").prop('disabled', true);
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        }
    });
}

function showItemEntityDetails(elem, toplevelaccountcode) {
    if (toplevelaccountcode == "0" || toplevelaccountcode === 0) { //Sunil: when child is from other than specifics table.
        swal("Operation not allowed!","This operation is not allowed for the item.","error");
        return false;
    }
    //enableDisableCoaButton(true);
    let headTypeStartNumber = toplevelaccountcode.toString().charAt(0);
    if (headTypeStartNumber == 1) {
        $("#subAccountNameDivId").text(">Income");
    } else if (headTypeStartNumber == 2) {
        $("#subAccountNameDivId").text(">Expense");
    } else if (headTypeStartNumber == 3) {
        $("#subAccountNameDivId").text(">Assets");
    } else if (headTypeStartNumber == 4) {
        $("#subAccountNameDivId").text(">Liabilties");
    }

    $("#chartOfAccountVendorCustomerTable input[type='text']").val("");
    $("#hiddenRequestLabourId").val("");
    $("#chartOfAccountVendorCustomerTable textarea").val("");
    $("#chartOfAccountVendorCustomerTable select option:selected").each(function () {
        $(this).removeAttr('selected');
    });
    $("#chartOfAccountVendorCustomerTable select option:first").prop("selected", "selected");
    $("#chartOfAccountVendorCustomerTable select[class='multiBranch']").multiselect('rebuild');
    $("#partBasedTransaction option:selected").each(function () {
        $(this).removeAttr('selected');
    });
    $('#partBasedTransaction').multiselect('rebuild');
    var entityId = $(elem).attr('id');
    var origEntityId = entityId.substring(19, entityId.length);
    var detailForm = "newItemform-container";

    $('.' + detailForm + ' input[type="hidden"]').val("");
    $('.' + detailForm + ' input[type="text"]').val("");
    $('.' + detailForm + ' textarea').val("");
    $('.' + detailForm + ' input[type="password"]').val("");
    $('.' + detailForm + ' select option:first').prop("selected", "selected");
    $('.' + detailForm + ' select[class="countryPhnCode"]').each(function () {
        $(this).find('option:first').prop("selected", "selected");
    });
    $('.' + detailForm + ' select[class="countryDropDown"]').each(function () {
        $(this).find('option:first').prop("selected", "selected");
    });
    $("#chartOfAccountKnowledgeLibraryTable tr[id*='dynKnowledgeLibrary']").remove();
    var specfKl = $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='copyContentKnowledgeLibrary']").html();
    $("#topLevelParentActCode").val(toplevelaccountcode);
    $(".logo-upload-button").attr("href", location.hash);
    $("a[id*='form-container-close']").attr("href", location.hash);
    coaBasedOnParentAccounCode(toplevelaccountcode, origEntityId);
    $("#disableSpecfBtn").hide();
    $("#coaActionButtons").show();
    var jsonData = {};
    jsonData.entityPrimaryId = origEntityId;
    var url = "/config/itemDetails";
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
            $("#disableSpecfBtn").hide();
            for (var i = 0; i < data.itemdetailsData.length; i++) {
                $("#itemCategory").children().remove();
                $(".multiBranch div[class='btn-group']").each(function () {
                    $(this).remove();
                });
                $(".multiBranch option").each(function () {
                    $(this).removeAttr('selected');
                });
                $('#item-form-container input[id="itemEntityHiddenId"]').val(data.itemdetailsData[i].id);
                $('#item-form-container input[id="items"]').val(data.itemdetailsData[i].name);
                $('#item-form-container textarea[id="itemKnowLib"]').val(data.itemdetailsData[i].specKnowLib);
                if (data.itemdetailsData[i].incomeSpecfPerUnitPrice == "" || data.itemdetailsData[i].incomeSpecfPerUnitPrice == null) {
                    $('#item-form-container input[id="incomespecfPerUnitPrice"]').val("");
                } else {
                    $('#item-form-container input[id="incomespecfPerUnitPrice"]').val(parseFloat(data.itemdetailsData[i].incomeSpecfPerUnitPrice).toFixed(2));
                }
                $('#item-form-container select[id="withHoldingApplicable"]').find("option[value='" + data.itemdetailsData[i].isWithholdingApplicable + "']").prop("selected", "selected");
                $('#item-form-container select[id="withHoldingType"]').find("option[value='" + data.itemdetailsData[i].withHoldingType + "']").prop("selected", "selected");
                $('#item-form-container select[id="captureInputTaxesApplicable"]').find("option[value='" + data.itemdetailsData[i].captureInputTaxes + "']").prop("selected", "selected");
                $('#item-form-container input[id="withHoldingRate"]').val(data.itemdetailsData[i].withholdingRate);
                $('#item-form-container input[id="withHoldingRate"]').val(data.itemdetailsData[i].withholdingRate);
                $('#item-form-container input[id="withHoldingLimit"]').val(data.itemdetailsData[i].withholdingLimit);
                $('#item-form-container input[id="withHoldingMonetoryLimit"]').val(data.itemdetailsData[i].withholdingMonetoryLimit);
                $('#item-form-container input[id="GSTCode"]').val(data.itemdetailsData[i].GSTItemCode);
                $('#item-form-container input[id="applicableDate"]').val(data.itemdetailsData[i].taxApplicableDate);
                if (!data.itemdetailsData[i].isSpecificUsedInTransactions) {
                    $("#disableSpecfBtn").show();
                }
                if ('1000000000000000000' == toplevelaccountcode) {
                    $("#taxHistoryList").html("");
                    if (data.itemdetailsData[i].taxHistoryList != "") {
                        var taxHistoryList = data.itemdetailsData[i].taxHistoryList;
                        taxHistoryList = taxHistoryList.substring(0, taxHistoryList.length - 1);
                        var taxHistoryArray = taxHistoryList.split("|");
                        for (var j = 0; j < taxHistoryArray.length; j++) {
                            $("#taxHistoryList").append('<li><a href="#"><b>' + taxHistoryArray[j] + '</b></a></li>');
                        }
                    }
                    if (data.itemdetailsData[i].currentTaxRulesAvailable) {
                        var gstRate = data.itemdetailsData[i].GSTTaxRate;
                        var cessRate = data.itemdetailsData[i].cessTaxRate;
                        if (gstRate != "") {
                            $("#gstTaxRuleDetailsTable tr[id='1'] input[id='itemgstrate'").val((gstRate / 2));
                            $("#gstTaxRuleDetailsTable tr[id='2'] input[id='itemgstrate'").val((gstRate / 2));
                            $("#gstTaxRuleDetailsTable tr[id='3'] input[id='itemgstrate'").val(gstRate);
                            $("#tax-coa-container").show();
                        }
                        if (cessRate != "") {
                            $("#gstTaxRuleDetailsTable tr[id='4'] input[id='itemgstrate'").val(cessRate);
                            $("#tax-coa-container").show();
                        }

                    }

                }
                if ('2000000000000000000' == toplevelaccountcode) {
                    if (data.itemdetailsData[i].gstTaxRateSelected != "") {
                        if (data.itemdetailsData[i].gstTaxRateSelected.indexOf("OTHER") != -1) {
                            $('#item-form-container input[id="GSTTaxRate"]').val(data.itemdetailsData[i].GSTTaxRate);
                            $('#item-form-container input[id="GSTTaxRate"]').show();
                        }
                    }
                    $("#tdsCoaHistoryList").html("");
                    if (data.itemdetailsData[i].tdsHistoryList != "") {
                        var tdsHistoryList = data.itemdetailsData[i].tdsHistoryList;
                        tdsHistoryList = tdsHistoryList.substring(0, tdsHistoryList.length - 1);
                        var tdsHistoryListArray = tdsHistoryList.split("|");
                        for (var j = 0; j < tdsHistoryListArray.length; j++) {
                            $("#tdsCoaHistoryList").append('<li><a href="#"><b>' + tdsHistoryListArray[j] + '</b></a></li>');
                        }
                    }

                    if (data.itemdetailsData[i].cessTaxRateSelected != "") {
                        if (data.itemdetailsData[i].cessTaxRateSelected.indexOf("OTHER") != -1) {
                            $('#item-form-container input[id="cessTaxRate"]').val(data.itemdetailsData[i].cessTaxRate);
                            $('#item-form-container input[id="cessTaxRate"]').show();
                        }
                    }
                    var gstRateArray = data.itemdetailsData[i].gstTaxRateSelected.split(",");
                    var cessRateArray = data.itemdetailsData[i].cessTaxRateSelected.split(",");
                    $('#item-form-container #GSTRateSelect').val(gstRateArray);
                    $('#item-form-container #CessRateSelect').val(cessRateArray);

                    $('#item-form-container #GSTRateSelect').multiselect("refresh");
                    $('#item-form-container #CessRateSelect').multiselect("refresh");
                } else {
                    $('#item-form-container input[id="GSTTaxRate"]').val(data.itemdetailsData[i].GSTTaxRate);
                    $('#item-form-container input[id="cessTaxRate"]').val(data.itemdetailsData[i].cessTaxRate);
                    $('#item-form-container input[id="GSTTaxRate"]').attr('gstRateOld', data.itemdetailsData[i].GSTTaxRate);
                    $('#item-form-container input[id="cessTaxRate"]').attr('cessRateOld', data.itemdetailsData[i].cessTaxRate);
                }

                $('#item-form-container select[id="GSTItemCategory"]').find("option[value='" + data.itemdetailsData[i].GstItemCategory + "']").prop("selected", "selected");
                $('#item-form-container select[id="GSTtypeOfSupply"]').find("option[value='" + data.itemdetailsData[i].GSTtypeOfSupply + "']").prop("selected", "selected");
                $('#item-form-container input[id="GSTDesc"]').val(data.itemdetailsData[i].GstItemDesc);
                var isEmpClaimItem = data.itemdetailsData[i].isEmployeeClaimItem;
                if (!isEmpty(isEmpClaimItem)) {
                    $('#item-form-container select[id="isItemEmpClaimItem"]').find("option[value='" + isEmpClaimItem + "']").prop("selected", "selected");
                }
                if (data.itemdetailsData[i].expUnitMeasure != "") {
                    $('#item-form-container input[id="expUnitMeasure"]').val(data.itemdetailsData[i].expUnitMeasure);
                }
                if (data.itemdetailsData[i].expNoOfOpeningBalUnits != "") {
                    $('#item-form-container input[id="expNoOfOpeningBalUnits"]').val(data.itemdetailsData[i].expNoOfOpeningBalUnits);
                }
                if (data.itemdetailsData[i].expRateOpeningBalUnits != "") {
                    $('#item-form-container input[id="expRateOpeningBalUnits"]').val(data.itemdetailsData[i].expRateOpeningBalUnits);
                }
                if (data.itemdetailsData[i].expOpeningBal != "") {
                    $('#item-form-container input[id="expOpeningBal"]').val(data.itemdetailsData[i].expOpeningBal);
                }
                $('#item-form-container input[id="itemInvoiceDescription1"]').val(data.itemdetailsData[i].invoiceItemDescription1);
                $('#item-form-container input[id="itemInvoiceDescription2"]').val(data.itemdetailsData[i].invoiceItemDescription2);
                $('#item-form-container input[class="itemBarcode"]').val(data.itemdetailsData[i].itemBarcodeNo);
                if (data.itemdetailsData[i].itemInvoiceDesc1Check == 1) {
                    $('#item-form-container input[id="itemInvoiceDesc1Check"]').prop('checked', true);
                } else {
                    $('#item-form-container input[id="itemInvoiceDesc1Check"]').prop('checked', false);
                }

                if (data.itemdetailsData[i].itemPriceInclusive == 1) {
                    $('#item-form-container input[id="itemPriceInclusive"]').prop('checked', true);
                } else {
                    $('#item-form-container input[id="itemPriceInclusive"]').prop('checked', false);
                }
                if (data.itemdetailsData[i].itemInvoiceDesc2Check == 1) {
                    $('#item-form-container input[id="itemInvoiceDesc2Check"]').prop('checked', true);
                } else {
                    $('#item-form-container input[id="itemInvoiceDesc2Check"]').prop('checked', false);
                }
                if (data.itemdetailsData[i].isTranEditableCheck == 1) {
                    $('#item-form-container input[id="isTranEditableCheck"]').prop('checked', true);
                } else {
                    $('#item-form-container input[id="isTranEditableCheck"]').prop('checked', false);
                }
                if (data.itemdetailsData[i].isEligibleInputTaxCredit != "") {
                    $("#isInputTaxCreditItem").val(data.itemdetailsData[i].isEligibleInputTaxCredit);
                }

                if (data.itemdetailsData[i].isTDsVendSpecfic == 1) {
                    $('#item-form-container input[id="isTDsVendSpecfic"]').prop('checked', true);
                } else {
                    $('#item-form-container input[id="isTDsVendSpecfic"]').prop('checked', false);
                }

                if (data.itemdetailsData[i].isCompositionItem == 1) {
                    $('#item-form-container input[id="isCompositionItem"]').prop('checked', true);
                } else {
                    $('#item-form-container input[id="isCompositionItem"]').prop('checked', false);
                }

                if (data.itemdetailsData[i].linkincomeExpense != "") {
                    $('#item-form-container select[id="incomeToExpId"]').find("option[value='" + data.itemdetailsData[i].linkincomeExpense + "']").prop("selected", "selected");
                }
                if (data.itemdetailsData[i].noOfExpUnit != "") {
                    $('#item-form-container input[id="noOfExpUnit"]').val(data.itemdetailsData[i].noOfExpUnit);
                }
                if (data.itemdetailsData[i].noOfIncUnit != "") {
                    $('#item-form-container input[id="noOfIncUnit"]').val(data.itemdetailsData[i].noOfIncUnit);
                }
                if (data.itemdetailsData[i].incUnitMeasure != "") {
                    $('#item-form-container input[id="incUnitMeasure"]').val(data.itemdetailsData[i].incUnitMeasure);
                }
                if (data.itemdetailsData[i].tradingInvCalcMethod != "") {
                    $('#item-form-container select[id="tradingInvCalcMethod"]').find("option[value='" + data.itemdetailsData[i].tradingInvCalcMethod + "']").prop("selected", "selected");
                }

                $('#item-form-container select[id="itemCategory"]').append('<option value="' + data.itemdetailsData[i].itemparent + '">' + data.itemdetailsData[i].itemparenttext + '</option>')

                addBranchForAddUpdateCoa(toplevelaccountcode);

                var itemBranchesList = data.itemdetailsData[i].itemBranches.split(',');
                var walkinCustDiscountArr = data.itemdetailsData[i].walkinCustDiscount.split(',');
                var branchOpeningBalanceArr = data.itemdetailsData[i].branchOpeningBalance.split(',');
                var branchInvOpeningBalanceArr = data.itemdetailsData[i].branchInvOpeningBalance.split(',');
                var branchInvUnitsArr = data.itemdetailsData[i].branchInvUnits.split(',');
                var branchInvRateArr = data.itemdetailsData[i].branchInvRate.split(',');
                $('#item-form-container #itemBranch2BtnList').find("input[type='checkbox']").prop('checked', false);
                var branchSelectedCount = 0;
                for (var j = 0; j < itemBranchesList.length; j++) {
                    if (!isEmpty(itemBranchesList[j])) {
                        $('#item-form-container #itemBranch2BtnList').find("input[type='checkbox'][value='" + itemBranchesList[j] + "']").prop("checked", true);
                        branchSelectedCount++;
                        var iv = "0.0";
                        if (!isEmpty(walkinCustDiscountArr[j])) {
                            iv = walkinCustDiscountArr[j];
                        }
                        if (branchOpeningBalanceArr[j] == "" || branchOpeningBalanceArr[j] == undefined) {
                            $('#item-form-container #itemBranch2BtnList').find('#itemBranch2Class-cb' + itemBranchesList[j]).val(iv);
                        } else {
                            $('#item-form-container #itemBranch2BtnList').find('#itemBranch2Class-cb' + itemBranchesList[j]).val((parseFloat(iv)).toFixed(2));
                        }
                        if (branchOpeningBalanceArr[j] == "" || branchOpeningBalanceArr[j] == undefined) {
                            $('#item-form-container #itemBranch2BtnList').find("li[id='" + itemBranchesList[j] + "'] input[class='openingBalance']").val(branchOpeningBalanceArr[j]);
                        } else {
                            $('#item-form-container #itemBranch2BtnList').find("li[id='" + itemBranchesList[j] + "'] input[class='openingBalance']").val(parseFloat(branchOpeningBalanceArr[j]).toFixed(2));
                        }
                        $('#item-form-container #itemBranch2BtnList').find("li[id='" + itemBranchesList[j] + "'] input[id='noOfUnits']").val(branchInvUnitsArr[j]);
                        $('#item-form-container #itemBranch2BtnList').find("li[id='" + itemBranchesList[j] + "'] input[id='inventoryRate']").val(branchInvRateArr[j]);
                        if (branchInvOpeningBalanceArr[j] == "" || branchInvOpeningBalanceArr[j] == undefined) {
                            $('#item-form-container #itemBranch2BtnList').find("li[id='" + itemBranchesList[j] + "'] input[id='inventoryValue']").val(branchInvOpeningBalanceArr[j]);
                        } else {
                            $('#item-form-container #itemBranch2BtnList').find("li[id='" + itemBranchesList[j] + "'] input[id='inventoryValue']").val((parseFloat(branchInvOpeningBalanceArr[j])).toFixed(2));
                        }
                    }
                } // end for loop for branches
                if (parseInt(branchSelectedCount) > 0) {
                    $('#itemBranch2Btn').html(branchSelectedCount + ' Selected&nbsp;<b class="caret" style="margin: 5px auto;"></b>');
                }

                var itemTransactionPurposeList = data.itemdetailsData[i].specfTxnPurpose.split(',');
                for (var j = 0; j < itemTransactionPurposeList.length; j++) {
                    if (itemTransactionPurposeList[j] != "") {
                        $('#item-form-container select[id="partBasedTransaction"]').find("option[value='" + itemTransactionPurposeList[j] + "']").prop("selected", "selected");
                    }
                }
                /*if ('2000000000000000000' == toplevelaccountcode) {
                	$("#partBasedTransaction option[value='27']").remove();
                	$("#partBasedTransaction option[value='28']").remove();
                } else{
                	$("#partBasedTransaction option[value='27']").remove();
                	$("#partBasedTransaction option[value='28']").remove();
                	$("#partBasedTransaction").prepend('<option value="28">Prepare Proforma Invoice</option>');
                	$("#partBasedTransaction").prepend('<option value="27">Prepare Quotation</option>');
                }*/
                $('#partBasedTransaction').multiselect('rebuild');
                if (data.itemdetailsData[i].incomeExpense != "") {
                    $('#item-form-container select[id="partBasedDynmData"]').find("option[value='" + data.itemdetailsData[i].incomeExpense + "']").prop("selected", "selected");
                }
                /*$("#identBody").show();
                $("#identHead").show();
                if ('3000000000000000000' == toplevelaccountcode || '4000000000000000000' == toplevelaccountcode) {
                    if((data.itemdetailsData[i].identificationForDataValid == 4 && data.itemdetailsData[i].parentIndentForDataValid == 4) || (data.itemdetailsData[i].identificationForDataValid == 5 && data.itemdetailsData[i].parentIndentForDataValid == 5)){
                    $("#coaBranchbankSetup").show();
                    $("#identBody").hide();
                    $("#identHead").hide();
                    $(".isTranEditable").hide();
                    if(data.itemdetailsData[i].identificationForDataValid == 4){
                        $("#bnkActType option[value='3']").prop("selected", "selected");
                    }else{
                        $("#bnkActType option[value='7']").prop("selected", "selected");
                    }
                    //branch bank account edit data
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
                        var branchBankAccountIfscCode ="";

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
                        if (data.branchBankActData[j].branchBankAccountIfscCode != null && data.branchBankActData[j].branchBankAccountIfscCode != "") {
                            branchBankAccountIfscCode = data.branchBankActData[j].branchBankAccountIfscCode;
                        }
                        if (data.branchBankActData[j].branchBankAccountRoutingNumber != null && data.branchBankActData[j].branchBankAccountRoutingNumber != "") {
                            branchBankAccountRoutingNumber = data.branchBankActData[j].branchBankAccountRoutingNumber;
                        }
                        $("#branchBankAccountTableR1 tbody tr[id='copyBranchBankAccount'] input[id='bnchBnkActhiddenId']").val(bankActPrimKey);
                        $("#branchBankAccountTableR1 tbody tr[id='copyBranchBankAccount'] input[id='bnkActName']").val(branchBankAccountBankName);
                        $("#branchBankAccountTableR1 tbody tr[id='copyBranchBankAccount'] select[id='bnkActType']").find("option[value='" + branchBankAccountType + "']").prop("selected", "selected");
                        $("#branchBankAccountTableR1 tbody tr[id='copyBranchBankAccount'] input[id='bnkActNumber']").val(branchBankAccountNumber);
                        $("#branchBankAccountTableR2 tbody tr[id='copyBranchBankAccount'] input[id='authSignName']").val(branchBankAccounttAuthSignName);
                        $("#branchBankAccountTableR2 tbody tr[id='copyBranchBankAccount'] input[id='authSignEmail']").val(branchBankAccounttAuthSignEmail);
                        $("#branchBankAccountTableR2 tbody tr[id='copyBranchBankAccount'] textarea[id='bnkAddress']").val(branchBankAccounttAddress);
                        $("#branchBankAccountTableR2 tbody tr[id='copyBranchBankAccount'] select[id='bnchbnkactPhnNocountryCode'] option").filter(function () {
                            return $(this).html() == branchBankAccounttPhnNoCtryCode;
                        }).prop("selected", "selected");
                        $("#branchBankAccountTableR2 tbody tr[id='copyBranchBankAccount'] input[id='bankPhnNumber1']").val(branchBankAccounttPhnNo.substring(0, 3));
                        $("#branchBankAccountTableR2 tbody tr[id='copyBranchBankAccount'] input[id='bankPhnNumber2']").val(branchBankAccounttPhnNo.substring(3, 6));
                        $("#branchBankAccountTableR2 tbody tr[id='copyBranchBankAccount'] input[id='bankPhnNumber3']").val(branchBankAccounttPhnNo.substring(6, 10));
                        $("#branchBankAccountTableR1 tbody tr[id='copyBranchBankAccount'] input[id='bnkSwiftCode']").val(branchBankAccountSwiftCode);
                        $("#branchBankAccountTableR1 tbody tr[id='copyBranchBankAccount'] input[id='bnkIfscCode']").val(branchBankAccountIfscCode);
                        $("#branchBankAccountTableR1 tbody tr[id='copyBranchBankAccount'] input[id='routingNumber']").val(branchBankAccountRoutingNumber);
                        
                        }
                    } else {
                        $("#coaBranchbankSetup").hide();
                    }
                }*/

                if ('3000000000000000000' == toplevelaccountcode) {
                    $("#isFixedAssetsSelectId").find("option[value='" + data.itemdetailsData[i].isFixedAssetsSelectValue + "']").prop("selected", "selected");
                    if (data.itemdetailsData[i].isFixedAssetsSelectValue == "1") {
                        $("#isFixedAssetsCapitalizaAmountInputId").val(data.itemdetailsData[i].isFixedAssetsCapitalizaAmountInput);
                        $("#isFixedAssetsThresholdLimitInputId").val(data.itemdetailsData[i].isFixedAssetsThresholdLimitInput);
                        $("#isFixedAssetsLifeSpanInputId").val(data.itemdetailsData[i].isFixedAssetsLifeSpanInputId);
                    }
                    $("#isMovableImmovableSelectId").find("option[value='" + data.itemdetailsData[i].isMovableImmovableSelectValue + "']").prop("selected", "selected");
                    $("#isTaggableSelectId").find("option[value='" + data.itemdetailsData[i].isTaggableSelectValue + "']").prop("selected", "selected");
                    $("#taggableCode").val(data.itemdetailsData[i].taggableCode);
                }
                // Add by Sunil for mapping
                $("#datavalidationall").find("option[value='" + data.itemdetailsData[i].identificationForDataValid + "']").prop("selected", "selected");
                $("#openingBalance").val(data.itemdetailsData[i].openingBalance);


            }

            for (var i = 0; i < data.itemKlData.length; i++) {
                var specfklprimkeyid = data.itemKlData[i].klPrimKeyId;
                var specfklcontent = "";
                var specfklismandatory = "";
                var specfklisforbranches = "";
                if (data.itemKlData[i].klContent != "" && data.itemKlData[i].klContent != null) {
                    specfklcontent = data.itemKlData[i].klContent;
                }
                if (data.itemKlData[i].mandatory != "" && data.itemKlData[i].mandatory != null) {
                    specfklismandatory = data.itemKlData[i].mandatory;
                }
                if (data.itemKlData[i].klforbnch != "" && data.itemKlData[i].klforbnch != null) {
                    var klforbnch = data.itemKlData[i].klforbnch;
                    specfklisforbranches = klforbnch.split(',');
                }
                if (i == 0) {
                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='copyContentKnowledgeLibrary'] input[name='specfHidPrimKey']").val(specfklprimkeyid);
                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='copyContentKnowledgeLibrary'] textarea[name='knowledgeLibRaryContent']").val(specfklcontent)
                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='copyContentKnowledgeLibrary'] select[name='mandatory'] option").filter(function () {
                        return $(this).html() == specfklismandatory;
                    }).prop("selected", "selected");
                    for (var j = 0; j < specfklisforbranches.length; j++) {
                        $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='copyContentKnowledgeLibrary'] select[id='knowledgeLibraryInBranches']").find("option[value='" + specfklisforbranches[j] + "']").prop("selected", "selected");
                    }
                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='copyContentKnowledgeLibrary'] select[id='knowledgeLibraryInBranches']").multiselect('rebuild');
                }
                if (i > 0) {
                    $("#chartOfAccountKnowledgeLibraryTable tbody").append('<tr id="dynKnowledgeLibrary' + i + '">' + specfKl + '</tr>');
                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='dynKnowledgeLibrary" + i + "'] div[class='btn-group']").each(function () {
                        $(this).remove();
                    });

                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='dynKnowledgeLibrary" + i + "'] select[class='multiBranch'] option:selected").each(function () {
                        $(this).removeAttr('selected');
                    });
                    var newTrId = "dynKnowledgeLibrary" + i;
                    $('#' + newTrId + ' select[class="multiBranch"]').multiselect({
                        buttonWidth: '150px',
                        maxHeight: 150,
                        includeSelectAllOption: true,
                        enableFiltering: true,
                        onChange: function (element, checked) {
                        }
                    });

                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='dynKnowledgeLibrary" + i + "'] input[name='specfHidPrimKey']").val(specfklprimkeyid);
                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='dynKnowledgeLibrary" + i + "'] textarea[name='knowledgeLibRaryContent']").val(specfklcontent);
                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='dynKnowledgeLibrary" + i + "'] select[id='mandatory'] option").filter(function () {
                        return $(this).html() == specfklismandatory;
                    }).prop("selected", "selected");
                    for (var j = 0; j < specfklisforbranches.length; j++) {
                        $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='dynKnowledgeLibrary" + i + "'] select[id='knowledgeLibraryInBranches']").find("option[value='" + specfklisforbranches[j] + "']").prop("selected", "selected");
                    }
                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='dynKnowledgeLibrary" + i + "'] select[id='knowledgeLibraryInBranches']").multiselect('rebuild');
                }
            }
            if (typeof data.itemDocUploadRuleData != "undefined") {
                for (var i = 0; i < data.itemDocUploadRuleData.length; i++) {
                    var specItemIndividualBranches = data.itemDocUploadRuleData[i].specItemIndividualBranches.split(",");
                    var specItemIndividualBranchesMonetoryLimit = data.itemDocUploadRuleData[i].specItemIndividualBranchesMonetoryLimit.split(",");
                    for (var k = 0; k < specItemIndividualBranches.length; k++) {
                        $(".docuploadrulecustomdropdownBranchList li[id='docuploadrulecustomdropdownBranchlist'] input[name='customdoccheckBranch'][value='" + specItemIndividualBranches[k] + "']").prop("checked", true);
                        $(".docuploadrulecustomdropdownBranchList li[id='docuploadrulecustomdropdownBranchlist'] input[id='monetoryLimit" + specItemIndividualBranches[k] + "']").val(specItemIndividualBranchesMonetoryLimit[k]);
                    }
                }
            }
            var check_box_values = $('input[name="customdoccheckBranch"]:checkbox:checked').map(function () {
                return this.value;
            }).get();
            var length = check_box_values.length;
            if (length > 0) {
                var text = length + " " + "Items Selected";
                $("#docuploadrulecustomdropdown").text(text);
                $("#docuploadrulecustomdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
            }
            if (check_box_values == 0) {
                $("#docuploadrulecustomdropdown").text("None Selected");
                $("#docuploadrulecustomdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
            }
            $('.notify-success').hide();
            $("." + detailForm + "").slideDown('slow');
            if ('1000000000000000000' == toplevelaccountcode) {
                $("#chartOfAccountTable tr th:nth-child(8)").html("");
                $("#chartOfAccountTable tr th:nth-child(8)").html("Item Stock Available/<br/>Reorder Level");
                $("#chartOfAccountTable tr td:nth-child(8)").html("");
                $("#chartOfAccountTable tr td:nth-child(8)").append('<input type="text" style="width:150px;" name="incomeItemStockAvailable" id="incomeItemStockAvailableId" class="incomeItemStockAvailableClass" readonly="readonly">' +
                    '<br/><div class="btn-group m-bottom-10" style="margin-top:10px;"><button id="reorderleveldropdown" class="multiselect dropdown-toggle btn" style="width: 150px;">None Selected<b class="caret"></b></button>' +
                    '<div id="reorderleveldropdown-menuid" class="reorderleveldropdown-menu">' +
                    '<ul id="reorderLevelBranchList"><li id="reorderlevelbranchlist">&nbsp;<input style="margin-bottom:5px;" type="checkbox" id="branchcheckboxid" name="branchcheck" value="" onClick="branchcheckUncheck(this)"/>&nbsp;Select All</li></ul>' +
                    '</div></div>');
                //reorderleveldropdown button click start
                $(function () {
                    $('#reorderleveldropdown').bind("click", function (event) {
                        var classval = $(this).attr('class');
                        if (classval == "multiselect dropdown-toggle btn") {
                            var newclassval = classval + " " + "open";
                            $(this).attr('class', newclassval);
                            var divdropdown = "openreorderleveldropdown-menu";
                            $(".reorderleveldropdown-menu").attr('class', divdropdown);
                        }
                        if (classval == "multiselect dropdown-toggle btn open") {
                            var newclassval = "multiselect dropdown-toggle btn";
                            $(this).attr('class', newclassval);
                            var divdropdown = "reorderleveldropdown-menu";
                            $(".openreorderleveldropdown-menu").attr('class', divdropdown);
                        }
                    });
                    $('#knowledgeLibraryInBranches option').each(function () {
                        if ($(this).val() != "multiselect-all" && $(this).val() != "") {
                            $("#reorderLevelBranchList").append('<li id="reorderlevelbranchlist">&nbsp;<input style="margin-bottom:5px;" type="checkbox" id="branchcheckboxid' + $(this).val() + '" name="branchcheck1" value=' + $(this).val() + ' onClick="branchcheckUncheck(this)"/>&nbsp;<span id="bnchtext" title=' + $(this).text() + '>' + $(this).text() + '</span><select style="margin-left:-70px;" name="reorderlevelselectalertuser" id="reorderlevelselectalertuserid' + $(this).val() + '" ><option value="" >Select Alert User </option></select> &nbsp;<input type="text" name="reorderlevelinput" id="reorderlevelinputid' + $(this).val() + '" style="width:110px;" placeholder="Reorder Level" onkeypress="return isNumber(event)" onkeyup="toggleBranchCheck(this);"/></li>');
                        }
                    });
                    console.log("Form COA");
                    var jsonData = {};
                    jsonData.useremail = $("#hiddenuseremail").text();
                    var url = "/config/allUsers";
                    $.ajax({
                        url: url,
                        data: JSON.stringify(jsonData),
                        type: "text",
                        method: "POST",
                        headers: {
                            "X-AUTH-TOKEN": window.authToken
                        },
                        contentType: 'application/json',
                        async: false,
                        success: function (data) {
                            $("#usersTable tbody").html("");
                            var html = '<option value="">Select Alert User</option>';
                            for (var i = 0; i < data.userListData.length; i++) {
                                html += '<option value="' + data.userListData[i].userEmail + '">' + data.userListData[i].fullName + ' (' + data.userListData[i].userEmail + ')</option>';
                            }
                            $('select[name="reorderlevelselectalertuser"]').html(html);
                        },
                        error: function (xhr, status, error) {
                            if (xhr.status == 401) {
                                doLogout();
                            }
                        }
                    });
                });
                //reorderleveldropdown button click stop
                if (data.itemdetailsData[0].reorderlevelbranchIds != "") {
                    var warehouseStockReorderLevelList = data.itemdetailsData[0].reorderlevelbranchIds.split(',');
                    var reorderlevelalertUserEmailsList = data.itemdetailsData[0].reorderlevelalertUserEmails.split(',');
                    var reorderlevelreorderLevelsList = data.itemdetailsData[0].reorderlevelreorderLevels.split(',');
                    for (var z = 0; z < warehouseStockReorderLevelList.length; z++) {
                        $('#item-form-container #reorderLevelBranchList').find("input[type='checkbox'][value=" + warehouseStockReorderLevelList[z] + "]").prop("checked", true);
                        $('#item-form-container #reorderLevelBranchList select[id="reorderlevelselectalertuserid' + warehouseStockReorderLevelList[z] + '"]').find("option[value='" + reorderlevelalertUserEmailsList[z] + "']").prop("selected", "selected");
                        $('#item-form-container #reorderLevelBranchList input[type="text"][id="reorderlevelinputid' + warehouseStockReorderLevelList[z] + '"]').val(reorderlevelreorderLevelsList[z]);
                    }
                }
                var check_box_values = $('input[name="branchcheck1"]:checkbox:checked').map(function () {
                    return this.value;
                }).get();
                var length = check_box_values.length;
                if (length > 0) {
                    var text = length + " " + "Items Selected";
                    $("#reorderleveldropdown").text(text);
                    $("#reorderleveldropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
                }
                if (check_box_values == 0) {
                    $("#reorderleveldropdown").text("None Selected");
                    $("#reorderleveldropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
                }
                displayIncomeStockAvailable();
            }
            if ('1000000000000000000' == toplevelaccountcode) {
                displayCombinationSales();
            }

            if ('2000000000000000000' == toplevelaccountcode && (data.itemdetailsData[i].parentIndentForDataValid != "" && data.itemdetailsData[i].parentIndentForDataValid == '24' || data.itemdetailsData[i].parentIndentForDataValid == '25' || data.itemdetailsData[i].parentIndentForDataValid == '26' || data.itemdetailsData[i].parentIndentForDataValid == '27')) {
                $(".isInputTaxCredit").remove();
                $("#chartOfAccountTable tr td:nth-child(2)").append("<div class='isInputTaxCredit'>Is this eligible for input tax credit ? <br/><select name='isInputTaxCreditItem' id='isInputTaxCreditItem'><option value='1'>Yes</option><option value='0' selected>No</option></select></div>");
                $(".customerVendorBranchCls").find("input[type='text']").hide();
                $("#chartOfAccountTable tr th:nth-child(7)").text("");
                $("#chartOfAccountTable tr td:nth-child(7)").html("");
                $("#chartOfAccountTable tr th:nth-child(7)").hide();
                $("#chartOfAccountTable tr td:nth-child(7)").hide();
                $("#isInputTaxCreditItem").val(data.itemdetailsData[i].isEligibleInputTaxCredit);
            }

            if ('4000000000000000000' == toplevelaccountcode && data.itemdetailsData[i].identificationForDataValid == '67'){
                $("#coaActionButtons").hide();
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

/*function showItemBankEntityDetails(elem, toplevelaccountcode) {
    if (toplevelaccountcode == "0" || toplevelaccountcode === 0) {
        alert("This operation is not allowed for the item.");
        return false;
    }
    let headTypeStartNumber = toplevelaccountcode.toString().charAt(0);
    if (headTypeStartNumber == 1) {
        $("#subAccountNameDivId").text(">Income");
    } else if (headTypeStartNumber == 2) {
        $("#subAccountNameDivId").text(">Expense");
    } else if (headTypeStartNumber == 3) {
        $("#subAccountNameDivId").text(">Assets");
    } else if (headTypeStartNumber == 4) {
        $("#subAccountNameDivId").text(">Liabilties");
    }

    $("#chartOfAccountVendorCustomerTable input[type='text']").val("");
    $("#hiddenRequestLabourId").val("");
    $("#chartOfAccountVendorCustomerTable textarea").val("");
    $("#chartOfAccountVendorCustomerTable select option:selected").each(function () {
        $(this).removeAttr('selected');
    });
    $("#chartOfAccountVendorCustomerTable select option:first").prop("selected", "selected");
    $("#chartOfAccountVendorCustomerTable select[class='multiBranch']").multiselect('rebuild');
    $("#partBasedTransaction option:selected").each(function () {
        $(this).removeAttr('selected');
    });
    $('#partBasedTransaction').multiselect('rebuild');
    var entityId = $(elem).attr('id');
    var origEntityId = entityId.substring(19, entityId.length);
    var detailForm = "newItemform-container";

    $('.' + detailForm + ' input[type="hidden"]').val("");
    $('.' + detailForm + ' input[type="text"]').val("");
    $('.' + detailForm + ' textarea').val("");
    $('.' + detailForm + ' input[type="password"]').val("");
    $('.' + detailForm + ' select option:first').prop("selected", "selected");
    $('.' + detailForm + ' select[class="countryPhnCode"]').each(function () {
        $(this).find('option:first').prop("selected", "selected");
    });
    $('.' + detailForm + ' select[class="countryDropDown"]').each(function () {
        $(this).find('option:first').prop("selected", "selected");
    });
    $("#chartOfAccountKnowledgeLibraryTable tr[id*='dynKnowledgeLibrary']").remove();
    var specfKl = $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='copyContentKnowledgeLibrary']").html();
    $("#topLevelParentActCode").val(toplevelaccountcode);
    $(".logo-upload-button").attr("href", location.hash);
    $("a[id*='form-container-close']").attr("href", location.hash);
    coaBasedOnParentAccounCode(toplevelaccountcode, origEntityId);
    $("#disableSpecfBtn").hide();
    $("#coaActionButtons").show();
    var jsonData = {};
    jsonData.entityPrimaryId = origEntityId;
    var url = "/config/itemDetails";
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
            $("#disableSpecfBtn").hide();
            enableDisableCoaBranchButton(true);
            
            for (var i = 0; i < data.itemdetailsData.length; i++) {
                $("#itemCategory").children().remove();
                $(".multiBranch div[class='btn-group']").each(function () {
                    $(this).remove();
                });
                $(".multiBranch option").each(function () {
                    $(this).removeAttr('selected');
                });
                $('#item-form-container input[id="itemEntityHiddenId"]').val(data.itemdetailsData[i].id);
                $('#item-form-container input[id="items"]').val(data.itemdetailsData[i].name);
                $('#item-form-container textarea[id="itemKnowLib"]').val(data.itemdetailsData[i].specKnowLib);
                if (data.itemdetailsData[i].incomeSpecfPerUnitPrice == "" || data.itemdetailsData[i].incomeSpecfPerUnitPrice == null) {
                    $('#item-form-container input[id="incomespecfPerUnitPrice"]').val("");
                } else {
                    $('#item-form-container input[id="incomespecfPerUnitPrice"]').val(parseFloat(data.itemdetailsData[i].incomeSpecfPerUnitPrice).toFixed(2));
                }
                $('#item-form-container select[id="withHoldingApplicable"]').find("option[value='" + data.itemdetailsData[i].isWithholdingApplicable + "']").prop("selected", "selected");
                $('#item-form-container select[id="withHoldingType"]').find("option[value='" + data.itemdetailsData[i].withHoldingType + "']").prop("selected", "selected");
                $('#item-form-container select[id="captureInputTaxesApplicable"]').find("option[value='" + data.itemdetailsData[i].captureInputTaxes + "']").prop("selected", "selected");
                $('#item-form-container input[id="withHoldingRate"]').val(data.itemdetailsData[i].withholdingRate);
                $('#item-form-container input[id="withHoldingRate"]').val(data.itemdetailsData[i].withholdingRate);
                $('#item-form-container input[id="withHoldingLimit"]').val(data.itemdetailsData[i].withholdingLimit);
                $('#item-form-container input[id="withHoldingMonetoryLimit"]').val(data.itemdetailsData[i].withholdingMonetoryLimit);
                $('#item-form-container input[id="GSTCode"]').val(data.itemdetailsData[i].GSTItemCode);
                $('#item-form-container input[id="applicableDate"]').val(data.itemdetailsData[i].taxApplicableDate);
                if (!data.itemdetailsData[i].isSpecificUsedInTransactions) {
                    $("#disableSpecfBtn").show();
                }

                $('#item-form-container select[id="GSTItemCategory"]').find("option[value='" + data.itemdetailsData[i].GstItemCategory + "']").prop("selected", "selected");
                $('#item-form-container select[id="GSTtypeOfSupply"]').find("option[value='" + data.itemdetailsData[i].GSTtypeOfSupply + "']").prop("selected", "selected");
                $('#item-form-container input[id="GSTDesc"]').val(data.itemdetailsData[i].GstItemDesc);
                var isEmpClaimItem = data.itemdetailsData[i].isEmployeeClaimItem;
                if (!isEmpty(isEmpClaimItem)) {
                    $('#item-form-container select[id="isItemEmpClaimItem"]').find("option[value='" + isEmpClaimItem + "']").prop("selected", "selected");
                }
                if (data.itemdetailsData[i].expUnitMeasure != "") {
                    $('#item-form-container input[id="expUnitMeasure"]').val(data.itemdetailsData[i].expUnitMeasure);
                }
                if (data.itemdetailsData[i].expNoOfOpeningBalUnits != "") {
                    $('#item-form-container input[id="expNoOfOpeningBalUnits"]').val(data.itemdetailsData[i].expNoOfOpeningBalUnits);
                }
                if (data.itemdetailsData[i].expRateOpeningBalUnits != "") {
                    $('#item-form-container input[id="expRateOpeningBalUnits"]').val(data.itemdetailsData[i].expRateOpeningBalUnits);
                }
                if (data.itemdetailsData[i].expOpeningBal != "") {
                    $('#item-form-container input[id="expOpeningBal"]').val(data.itemdetailsData[i].expOpeningBal);
                }
                $('#item-form-container input[id="itemInvoiceDescription1"]').val(data.itemdetailsData[i].invoiceItemDescription1);
                $('#item-form-container input[id="itemInvoiceDescription2"]').val(data.itemdetailsData[i].invoiceItemDescription2);
                $('#item-form-container input[class="itemBarcode"]').val(data.itemdetailsData[i].itemBarcodeNo);
                if (data.itemdetailsData[i].itemInvoiceDesc1Check == 1) {
                    $('#item-form-container input[id="itemInvoiceDesc1Check"]').prop('checked', true);
                } else {
                    $('#item-form-container input[id="itemInvoiceDesc1Check"]').prop('checked', false);
                }

                if (data.itemdetailsData[i].itemPriceInclusive == 1) {
                    $('#item-form-container input[id="itemPriceInclusive"]').prop('checked', true);
                } else {
                    $('#item-form-container input[id="itemPriceInclusive"]').prop('checked', false);
                }
                if (data.itemdetailsData[i].itemInvoiceDesc2Check == 1) {
                    $('#item-form-container input[id="itemInvoiceDesc2Check"]').prop('checked', true);
                } else {
                    $('#item-form-container input[id="itemInvoiceDesc2Check"]').prop('checked', false);
                }
                if (data.itemdetailsData[i].isEligibleInputTaxCredit != "") {
                    $("#isInputTaxCreditItem").val(data.itemdetailsData[i].isEligibleInputTaxCredit);
                }

                if (data.itemdetailsData[i].isTDsVendSpecfic == 1) {
                    $('#item-form-container input[id="isTDsVendSpecfic"]').prop('checked', true);
                } else {
                    $('#item-form-container input[id="isTDsVendSpecfic"]').prop('checked', false);
                }

                if (data.itemdetailsData[i].isCompositionItem == 1) {
                    $('#item-form-container input[id="isCompositionItem"]').prop('checked', true);
                } else {
                    $('#item-form-container input[id="isCompositionItem"]').prop('checked', false);
                }

                if (data.itemdetailsData[i].linkincomeExpense != "") {
                    $('#item-form-container select[id="incomeToExpId"]').find("option[value='" + data.itemdetailsData[i].linkincomeExpense + "']").prop("selected", "selected");
                }
                if (data.itemdetailsData[i].noOfExpUnit != "") {
                    $('#item-form-container input[id="noOfExpUnit"]').val(data.itemdetailsData[i].noOfExpUnit);
                }
                if (data.itemdetailsData[i].noOfIncUnit != "") {
                    $('#item-form-container input[id="noOfIncUnit"]').val(data.itemdetailsData[i].noOfIncUnit);
                }
                if (data.itemdetailsData[i].incUnitMeasure != "") {
                    $('#item-form-container input[id="incUnitMeasure"]').val(data.itemdetailsData[i].incUnitMeasure);
                }
                if (data.itemdetailsData[i].tradingInvCalcMethod != "") {
                    $('#item-form-container select[id="tradingInvCalcMethod"]').find("option[value='" + data.itemdetailsData[i].tradingInvCalcMethod + "']").prop("selected", "selected");
                }

                $('#item-form-container select[id="itemCategory"]').append('<option value="' + data.itemdetailsData[i].itemparent + '">' + data.itemdetailsData[i].itemparenttext + '</option>')

                addBranchForAddUpdateCoa(toplevelaccountcode);

                var itemBranchesList = data.itemdetailsData[i].itemBranches.split(',');
                var walkinCustDiscountArr = data.itemdetailsData[i].walkinCustDiscount.split(',');
                var branchOpeningBalanceArr = data.itemdetailsData[i].branchOpeningBalance.split(',');
                var branchInvOpeningBalanceArr = data.itemdetailsData[i].branchInvOpeningBalance.split(',');
                var branchInvUnitsArr = data.itemdetailsData[i].branchInvUnits.split(',');
                var branchInvRateArr = data.itemdetailsData[i].branchInvRate.split(',');
                $('#item-form-container #itemBranch2BtnList').find("input[type='checkbox']").prop('checked', false);
                var branchSelectedCount = 0;
                for (var j = 0; j < itemBranchesList.length; j++) {
                    if (!isEmpty(itemBranchesList[j])) {
                        $('#item-form-container #itemBranch2BtnList').find("input[type='checkbox'][value='" + itemBranchesList[j] + "']").prop("checked", true);
                        branchSelectedCount++;
                        var iv = "0.0";
                        if (!isEmpty(walkinCustDiscountArr[j])) {
                            iv = walkinCustDiscountArr[j];
                        }
                        if (branchOpeningBalanceArr[j] == "" || branchOpeningBalanceArr[j] == undefined) {
                            $('#item-form-container #itemBranch2BtnList').find('#itemBranch2Class-cb' + itemBranchesList[j]).val(iv);
                        } else {
                            $('#item-form-container #itemBranch2BtnList').find('#itemBranch2Class-cb' + itemBranchesList[j]).val((parseFloat(iv)).toFixed(2));
                        }
                        if (branchOpeningBalanceArr[j] == "" || branchOpeningBalanceArr[j] == undefined) {
                            $('#item-form-container #itemBranch2BtnList').find("li[id='" + itemBranchesList[j] + "'] input[class='openingBalance']").val(branchOpeningBalanceArr[j]);
                        } else {
                            $('#item-form-container #itemBranch2BtnList').find("li[id='" + itemBranchesList[j] + "'] input[class='openingBalance']").val(parseFloat(branchOpeningBalanceArr[j]).toFixed(2));
                        }
                        $('#item-form-container #itemBranch2BtnList').find("li[id='" + itemBranchesList[j] + "'] input[id='noOfUnits']").val(branchInvUnitsArr[j]);
                        $('#item-form-container #itemBranch2BtnList').find("li[id='" + itemBranchesList[j] + "'] input[id='inventoryRate']").val(branchInvRateArr[j]);
                        if (branchInvOpeningBalanceArr[j] == "" || branchInvOpeningBalanceArr[j] == undefined) {
                            $('#item-form-container #itemBranch2BtnList').find("li[id='" + itemBranchesList[j] + "'] input[id='inventoryValue']").val(branchInvOpeningBalanceArr[j]);
                        } else {
                            $('#item-form-container #itemBranch2BtnList').find("li[id='" + itemBranchesList[j] + "'] input[id='inventoryValue']").val((parseFloat(branchInvOpeningBalanceArr[j])).toFixed(2));
                        }
                    }
                } // end for loop for branches
                if (parseInt(branchSelectedCount) > 0) {
                    $('#itemBranch2Btn').html(branchSelectedCount + ' Selected&nbsp;<b class="caret" style="margin: 5px auto;"></b>');
                }

                var itemTransactionPurposeList = data.itemdetailsData[i].specfTxnPurpose.split(',');
                for (var j = 0; j < itemTransactionPurposeList.length; j++) {
                    if (itemTransactionPurposeList[j] != "") {
                        $('#item-form-container select[id="partBasedTransaction"]').find("option[value='" + itemTransactionPurposeList[j] + "']").prop("selected", "selected");
                    }
                }
                
                $('#partBasedTransaction').multiselect('rebuild');
                if (data.itemdetailsData[i].incomeExpense != "") {
                    $('#item-form-container select[id="partBasedDynmData"]').find("option[value='" + data.itemdetailsData[i].incomeExpense + "']").prop("selected", "selected");
                }
                
                $("#identBody").show();
                $("#identHead").show();
                if ('3000000000000000000' == toplevelaccountcode || '4000000000000000000' == toplevelaccountcode) {
                    if((data.itemdetailsData[i].identificationForDataValid == 4 && data.itemdetailsData[i].parentIndentForDataValid == 4) || (data.itemdetailsData[i].identificationForDataValid == 5 && data.itemdetailsData[i].parentIndentForDataValid == 5)){
                    $("#coaBranchbankSetup").show();
                    $("#addSpecfBrnchBtn").show();
                    $("#addSpecfBtn").hide();
                    $("#identBody").hide();
                    $("#identHead").hide();
                    $(".isTranEditable").hide();
                    if(data.itemdetailsData[i].identificationForDataValid == 4){
                        $("#bnkActType option[value='3']").prop("selected", "selected");
                    }else{
                        $("#bnkActType option[value='7']").prop("selected", "selected");
                    }
                    //branch bank account edit data
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
                        var branchBankAccountIfscCode = "";

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
                        if (data.branchBankActData[j].branchBankAccountIfscCode != null && data.branchBankActData[j].branchBankAccountIfscCode != "") {
                            branchBankAccountIfscCode = data.branchBankActData[j].branchBankAccountIfscCode;
                        }
                        if (data.branchBankActData[j].branchBankAccountRoutingNumber != null && data.branchBankActData[j].branchBankAccountRoutingNumber != "") {
                            branchBankAccountRoutingNumber = data.branchBankActData[j].branchBankAccountRoutingNumber;
                        }
                        $("#branchBankAccountTableR1 tbody tr[id='copyBranchBankAccount'] input[id='bnchBnkActhiddenId']").val(bankActPrimKey);
                        $("#branchBankAccountTableR1 tbody tr[id='copyBranchBankAccount'] input[id='bnkActName']").val(branchBankAccountBankName);
                        $("#branchBankAccountTableR1 tbody tr[id='copyBranchBankAccount'] select[id='bnkActType']").find("option[value='" + branchBankAccountType + "']").prop("selected", "selected");
                        $("#branchBankAccountTableR1 tbody tr[id='copyBranchBankAccount'] input[id='bnkActNumber']").val(branchBankAccountNumber);
                        $("#branchBankAccountTableR2 tbody tr[id='copyBranchBankAccount'] input[id='authSignName']").val(branchBankAccounttAuthSignName);
                        $("#branchBankAccountTableR2 tbody tr[id='copyBranchBankAccount'] input[id='authSignEmail']").val(branchBankAccounttAuthSignEmail);
                        $("#branchBankAccountTableR2 tbody tr[id='copyBranchBankAccount'] textarea[id='bnkAddress']").val(branchBankAccounttAddress);
                        $("#branchBankAccountTableR2 tbody tr[id='copyBranchBankAccount'] select[id='bnchbnkactPhnNocountryCode'] option").filter(function () {
                            return $(this).html() == branchBankAccounttPhnNoCtryCode;
                        }).prop("selected", "selected");
                        $("#branchBankAccountTableR2 tbody tr[id='copyBranchBankAccount'] input[id='bankPhnNumber1']").val(branchBankAccounttPhnNo.substring(0, 3));
                        $("#branchBankAccountTableR2 tbody tr[id='copyBranchBankAccount'] input[id='bankPhnNumber2']").val(branchBankAccounttPhnNo.substring(3, 6));
                        $("#branchBankAccountTableR2 tbody tr[id='copyBranchBankAccount'] input[id='bankPhnNumber3']").val(branchBankAccounttPhnNo.substring(6, 10));
                        $("#branchBankAccountTableR1 tbody tr[id='copyBranchBankAccount'] input[id='bnkSwiftCode']").val(branchBankAccountSwiftCode);
                        $("#branchBankAccountTableR1 tbody tr[id='copyBranchBankAccount'] input[id='bnkIfscCode']").val(branchBankAccountIfscCode);
                        $("#branchBankAccountTableR1 tbody tr[id='copyBranchBankAccount'] input[id='routingNumber']").val(branchBankAccountRoutingNumber);
                        
                        }
                    } else {
                        $("#coaBranchbankSetup").hide();
                    }
                }

                if ('3000000000000000000' == toplevelaccountcode) {
                    $("#isFixedAssetsSelectId").find("option[value='" + data.itemdetailsData[i].isFixedAssetsSelectValue + "']").prop("selected", "selected");
                    if (data.itemdetailsData[i].isFixedAssetsSelectValue == "1") {
                        $("#isFixedAssetsCapitalizaAmountInputId").val(data.itemdetailsData[i].isFixedAssetsCapitalizaAmountInput);
                        $("#isFixedAssetsThresholdLimitInputId").val(data.itemdetailsData[i].isFixedAssetsThresholdLimitInput);
                        $("#isFixedAssetsLifeSpanInputId").val(data.itemdetailsData[i].isFixedAssetsLifeSpanInputId);
                    }
                    $("#isMovableImmovableSelectId").find("option[value='" + data.itemdetailsData[i].isMovableImmovableSelectValue + "']").prop("selected", "selected");
                    $("#isTaggableSelectId").find("option[value='" + data.itemdetailsData[i].isTaggableSelectValue + "']").prop("selected", "selected");
                    $("#taggableCode").val(data.itemdetailsData[i].taggableCode);
                }
                // Add by Sunil for mapping
                $("#datavalidationall").find("option[value='" + data.itemdetailsData[i].identificationForDataValid + "']").prop("selected", "selected");
                $("#openingBalance").val(data.itemdetailsData[i].openingBalance);


            }

            for (var i = 0; i < data.itemKlData.length; i++) {
                var specfklprimkeyid = data.itemKlData[i].klPrimKeyId;
                var specfklcontent = "";
                var specfklismandatory = "";
                var specfklisforbranches = "";
                if (data.itemKlData[i].klContent != "" && data.itemKlData[i].klContent != null) {
                    specfklcontent = data.itemKlData[i].klContent;
                }
                if (data.itemKlData[i].mandatory != "" && data.itemKlData[i].mandatory != null) {
                    specfklismandatory = data.itemKlData[i].mandatory;
                }
                if (data.itemKlData[i].klforbnch != "" && data.itemKlData[i].klforbnch != null) {
                    var klforbnch = data.itemKlData[i].klforbnch;
                    specfklisforbranches = klforbnch.split(',');
                }
                if (i == 0) {
                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='copyContentKnowledgeLibrary'] input[name='specfHidPrimKey']").val(specfklprimkeyid);
                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='copyContentKnowledgeLibrary'] textarea[name='knowledgeLibRaryContent']").val(specfklcontent)
                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='copyContentKnowledgeLibrary'] select[name='mandatory'] option").filter(function () {
                        return $(this).html() == specfklismandatory;
                    }).prop("selected", "selected");
                    for (var j = 0; j < specfklisforbranches.length; j++) {
                        $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='copyContentKnowledgeLibrary'] select[id='knowledgeLibraryInBranches']").find("option[value='" + specfklisforbranches[j] + "']").prop("selected", "selected");
                    }
                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='copyContentKnowledgeLibrary'] select[id='knowledgeLibraryInBranches']").multiselect('rebuild');
                }
                if (i > 0) {
                    $("#chartOfAccountKnowledgeLibraryTable tbody").append('<tr id="dynKnowledgeLibrary' + i + '">' + specfKl + '</tr>');
                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='dynKnowledgeLibrary" + i + "'] div[class='btn-group']").each(function () {
                        $(this).remove();
                    });

                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='dynKnowledgeLibrary" + i + "'] select[class='multiBranch'] option:selected").each(function () {
                        $(this).removeAttr('selected');
                    });
                    var newTrId = "dynKnowledgeLibrary" + i;
                    $('#' + newTrId + ' select[class="multiBranch"]').multiselect({
                        buttonWidth: '150px',
                        maxHeight: 150,
                        includeSelectAllOption: true,
                        enableFiltering: true,
                        onChange: function (element, checked) {
                        }
                    });

                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='dynKnowledgeLibrary" + i + "'] input[name='specfHidPrimKey']").val(specfklprimkeyid);
                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='dynKnowledgeLibrary" + i + "'] textarea[name='knowledgeLibRaryContent']").val(specfklcontent);
                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='dynKnowledgeLibrary" + i + "'] select[id='mandatory'] option").filter(function () {
                        return $(this).html() == specfklismandatory;
                    }).prop("selected", "selected");
                    for (var j = 0; j < specfklisforbranches.length; j++) {
                        $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='dynKnowledgeLibrary" + i + "'] select[id='knowledgeLibraryInBranches']").find("option[value='" + specfklisforbranches[j] + "']").prop("selected", "selected");
                    }
                    $("#chartOfAccountKnowledgeLibraryTable tbody tr[id='dynKnowledgeLibrary" + i + "'] select[id='knowledgeLibraryInBranches']").multiselect('rebuild');
                }
            }
            if (typeof data.itemDocUploadRuleData != "undefined") {
                for (var i = 0; i < data.itemDocUploadRuleData.length; i++) {
                    var specItemIndividualBranches = data.itemDocUploadRuleData[i].specItemIndividualBranches.split(",");
                    var specItemIndividualBranchesMonetoryLimit = data.itemDocUploadRuleData[i].specItemIndividualBranchesMonetoryLimit.split(",");
                    for (var k = 0; k < specItemIndividualBranches.length; k++) {
                        $(".docuploadrulecustomdropdownBranchList li[id='docuploadrulecustomdropdownBranchlist'] input[name='customdoccheckBranch'][value='" + specItemIndividualBranches[k] + "']").prop("checked", true);
                        $(".docuploadrulecustomdropdownBranchList li[id='docuploadrulecustomdropdownBranchlist'] input[id='monetoryLimit" + specItemIndividualBranches[k] + "']").val(specItemIndividualBranchesMonetoryLimit[k]);
                    }
                }
            }
            var check_box_values = $('input[name="customdoccheckBranch"]:checkbox:checked').map(function () {
                return this.value;
            }).get();
            var length = check_box_values.length;
            if (length > 0) {
                var text = length + " " + "Items Selected";
                $("#docuploadrulecustomdropdown").text(text);
                $("#docuploadrulecustomdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
            }
            if (check_box_values == 0) {
                $("#docuploadrulecustomdropdown").text("None Selected");
                $("#docuploadrulecustomdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
            }
            $('.notify-success').hide();
            $("." + detailForm + "").slideDown('slow');

            if ('4000000000000000000' == toplevelaccountcode && data.itemdetailsData[i].identificationForDataValid == '67'){
                $("#coaActionButtons").hide();
            }
            alwaysScrollTop();
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        }
    });
}*/

function displayCombinationSales() {
    var incomeItemsListTemp = "<option value='-1' units='-1' id='-1'>Please Select..</option>";
    var jsonData = {};
    jsonData.useremail = $("#hiddenuseremail").text();
    var url = "/data/getcoaincomeitems";
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        async: false,
        method: "GET",
        contentType: 'application/json',
        success: function (data) {
            for (var i = 0; i < data.coaItemData.length; i++) {
                incomeItemsListTemp += ('<option value="' + data.coaItemData[i].id + '" units="' + data.coaItemData[i].openBalUnits + '" rates="' + data.coaItemData[i].openBalRate + '">' + data.coaItemData[i].name + '</option>');
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        }
    });
    var jsonData = {};
    jsonData.email = $("#hiddenuseremail").text();
    jsonData.incomeSpecificsId = $('#item-form-container input[id="itemEntityHiddenId"]').val();
    var url = "/config/getListOfCombinationSalesItems";
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
            if (data.combSalesListData != null) {
                $("#combSalesTbl tbody tr:first").remove();
                for (var i = 0; i < data.combSalesListData.length; i++) {
                    var length = $("#combSalesTbl tbody tr").length;
                    customerGstinTblTrData = "";
                    customerGstinTblTrData += ('<tr id="combSales' + length + '"><td><select style="width:100px" id="incomeItemsForCombSales" name="incomeItemsForCombSales" onchange="getItemsPriceAndUnits(this)"></td>');
                    customerGstinTblTrData += ('<td><input style="width:40px"  type="text"  name="openBalUnitsForCombSales" id="openBalUnitsForCombSales" value="' + data.combSalesListData[i].openBalUnits + '" onkeyup="calculateCombSalesItemPrice(this);"></td>');
                    customerGstinTblTrData += ('<td><input style="width:60px"  type="text"  name="openBalRateForCombSales" id="openBalRateForCombSales" value="' + data.combSalesListData[i].openingBalRate + '" onkeyup="calculateCombSalesItemPrice(this);"></td></tr>');

                    $("#combSalesTbl tbody").append(customerGstinTblTrData);
                    $("#combSalesTbl > tbody > tr[id='combSales" + length + "'] > td > select[id=incomeItemsForCombSales]").append(incomeItemsListTemp);
                    $("#combSalesTbl > tbody > tr[id='combSales" + length + "'] > td > select[id=incomeItemsForCombSales]").val(data.combSalesListData[i].specificsId).change();
                    $("#combSalesTbl > tbody > tr[id='combSales" + length + "'] > td > input[id=openBalUnitsForCombSales]").val(data.combSalesListData[i].openBalUnits).change();
                    $("#combSalesTbl > tbody > tr[id='combSales" + length + "'] > td > input[id=openBalRateForCombSales]").val(data.combSalesListData[i].openingBalRate).change();
                    calculateCombSalesItemPrice(this);
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

function displayIncomeStockAvailable() {
    var jsonData = {};
    jsonData.email = $("#hiddenuseremail").text();
    jsonData.incomeSpecificsId = $('#item-form-container input[id="itemEntityHiddenId"]').val();
    jsonData.incometoexpensemapping = $("#incomeToExpId option:selected").val();
    var url = "/specifics/incomeAvailableStock";
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
            $("#incomeItemStockAvailableId").val("");
            if (data.result) {
                $("#incomeItemStockAvailableId").val(data.incomeStockData[0].stockAvailable);
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        }
    });
}

function getBuyInventoryStockAvailable(elem) {
    var jsonData = {};
    jsonData.email = $("#hiddenuseremail").text();
    jsonData.expenseSpecificsId = $("#incomeToExpId option:selected").val();
    var url = "/specifics/buyInventoryStockAvailable";
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
            $("#incomeItemStockAvailableId").val("");
            if (data.result) {
                $("#incomeItemStockAvailableId").val(data.expInventoryStockData[0].stockAvailable);
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        }
    });
}

function changeIfFixedAssets(elem) {
    var value = $(elem).val();
    if (value != "") {
        if (value == "1") {
            $("#chartOfAccountTable thead[class='tablehead1'] tr th[id='isMovableImmovableth']").before('<th id="isFixedAssetsCapitalizaAmountth"><b id="isFixedAssetsCapitalizaAmountLabel">Fixed Asset Capitalize Amount Rule?</b></th>');
            $("#chartOfAccountTable thead[class='tablehead1'] tr th[id='isMovableImmovableth']").before('<th id="isFixedAssetsThresholdLimitth"><b id="isFixedAssetsThresholdLimitLabel">Fixed Asset Threshold Limit?</b></th>');
            $("#chartOfAccountTable thead[class='tablehead1'] tr th[id='isMovableImmovableth']").before('<th id="isFixedAssetsLifeSpanth"><b id="isFixedAssetsLifeSpanLabel">Fixed Asset Capitalize Life Sapn Rule?</b></th>');
            $("#chartOfAccountTable tbody tr td[id='isMovableImmovabletd']").before('<td id="isFixedAssetsCapitalizaAmounttd"><input name="isFixedAssetsCapitalizaAmountInput" id="isFixedAssetsCapitalizaAmountInputId" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);"></td>');
            $("#chartOfAccountTable tbody tr td[id='isMovableImmovabletd']").before('<td id="isFixedAssetsThresholdLimittd"><input name="isFixedAssetsThresholdLimitInput" id="isFixedAssetsThresholdLimitInputId" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);"></td>');
            $("#chartOfAccountTable tbody tr td[id='isMovableImmovabletd']").before('<td id="isFixedAssetsLifeSpantd"><input name="isFixedAssetsLifeSapnInput" id="isFixedAssetsLifeSpanInputId" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);"></td>');
        } else {
            $("#chartOfAccountTable thead[class='tablehead1'] tr th[id='isFixedAssetsCapitalizaAmountth']").remove();
            $("#chartOfAccountTable thead[class='tablehead1'] tr th[id='isFixedAssetsThresholdLimitth']").remove();
            $("#chartOfAccountTable thead[class='tablehead1'] tr th[id='isFixedAssetsLifeSpanth']").remove();
            $("#chartOfAccountTable tbody tr td[id='isFixedAssetsCapitalizaAmounttd']").remove();
            $("#chartOfAccountTable tbody tr td[id='isFixedAssetsThresholdLimittd']").remove();
            $("#chartOfAccountTable tbody tr td[id='isFixedAssetsLifeSpantd']").remove();
        }
    } else {
        $("#chartOfAccountTable thead[class='tablehead1'] tr th[id='isFixedAssetsCapitalizaAmountth']").remove();
        $("#chartOfAccountTable thead[class='tablehead1'] tr th[id='isFixedAssetsThresholdLimitth']").remove();
        $("#chartOfAccountTable thead[class='tablehead1'] tr th[id='isFixedAssetsLifeSpanth']").remove();
        $("#chartOfAccountTable tbody tr td[id='isFixedAssetsCapitalizaAmounttd']").remove();
        $("#chartOfAccountTable tbody tr td[id='isFixedAssetsThresholdLimittd']").remove();
        $("#chartOfAccountTable tbody tr td[id='isFixedAssetsLifeSpantd']").remove();
    }
}

function getTransactionType() {
    var useremail = $("#hiddenuseremail").text();
    var jsonData = {};
    jsonData.email = useremail;
    var url = "/config/getdatasellreceicve";
    console.log(window);
    console.log(document.authToken);
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        method: "GET",
        contentType: 'application/json',
        success: function (data) {
            for (var i = 0; i < data.sellReceiveData.length; i++) {
                if (data.sellReceiveData[i].id != "9" && data.sellReceiveData[i].id != "10" &&
                    data.sellReceiveData[i].id != "20" && data.sellReceiveData[i].id != "21") {
                    	$("#partBasedTransaction").append('<option value="' + data.sellReceiveData[i].id + '">' + data.sellReceiveData[i].name + '');
            			$("#userTxnQuestion").append('<option value="' + data.sellReceiveData[i].id + '">' + data.sellReceiveData[i].name + '');
                }
            }
            $('#partBasedTransaction').multiselect({
                buttonWidth: '180px',
                maxHeight: 150,
                overflow: 'scroll',
                includeSelectAllOption: true,
                enableFiltering: true,
                onChange: function (element, checked) {
                }
            });
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        }
    });
}

function removeAllTransactionType() {
    $("#partBasedTransaction option[value='1']").remove();
    $("#partBasedTransaction option[value='2']").remove();
    $("#partBasedTransaction option[value='3']").remove();
    $("#partBasedTransaction option[value='4']").remove();
    $("#partBasedTransaction option[value='5']").remove();
    $("#partBasedTransaction option[value='6']").remove();
    $("#partBasedTransaction option[value='7']").remove();
    $("#partBasedTransaction option[value='8']").remove();
    $("#partBasedTransaction option[value='11']").remove();
    $("#partBasedTransaction option[value='14']").remove();
    $("#partBasedTransaction option[value='22']").remove();
    $("#partBasedTransaction option[value='23']").remove();
    $("#partBasedTransaction option[value='24']").remove();
    $("#partBasedTransaction option[value='25']").remove();
    $("#partBasedTransaction").multiselect('rebuild');
}

$(document).ready(function () {
    $('#addCatBtn').click(function () {
        var categoryEntityHiddenId = $("#categoryEntityHiddenId").val();
        var btnname = this.name;
        var catDescription = $("#catdescription").val();
        var partName = $("#category").val();
        // $("a[id*='form-container-close']").attr("href", location.hash);
        $("#newItemform-container-close").attr("href", location.hash);
        if (partName == "") {
            swal("Error!","Provide Category Name.","error");
            return true;
        }
        var jsonData = {};
        jsonData.categoryHiddenPrimaryKey = categoryEntityHiddenId;
        jsonData.particularName = partName;
        jsonData.btnName = btnname;
        jsonData.categoryDesc = catDescription;
        var url = "/particulars/addParticular";
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
                var catTable = $("#categoryTable");
                var catId = data.newparticularsData[0].id;
                var catName = data.newparticularsData[0].name;
                var btnName = data.newparticularsData[0].btnName;
                $("#category").val("");
                $("#cataccountcode").val("");
                if (btnName == "addConfigCatBtn") {
                    if (categoryEntityHiddenId != "") {
                        $("#mainChartOfAccount").find('li[id="' + data.newparticularsData[0].accountCode + '"]').html("");
                        $("#mainChartOfAccount").find('li[id="' + data.newparticularsData[0].accountCode + '"]').append('<div class="chartOfAccountContainer"><img id="' + data.newparticularsData[0].accountCode + '" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this);"></img><font class="color-grey"><b style="margin-left:2px;"><a id="show-entity-details' + data.newparticularsData[0].id + '" href="#itemSetUp" onClick="">' + data.newparticularsData[0].name + '</a></b></p><button style="float:right" id="newItemform-container" name="' + data.newparticularsData[0].name + '" class="newEntityCreateButton btn btn-tree btn-idos" title="Create new Sub Account" onclick="javascript:createCOA(this,' + data.newparticularsData[0].id + ');"><i class="fa fa-plus pr-3"></i>Add Sub Account</button>');
                    } else {
                        $("#mainChartOfAccount").append('<li id=' + data.newparticularsData[0].accountCode + '><div class="chartOfAccountContainer"><img id="' + data.newparticularsData[0].accountCode + '" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this);"></img><font class="color-grey"><b style="margin-left:2px;"><a id="show-entity-details' + data.newparticularsData[0].id + '" href="#itemSetUp" onClick="">' + data.newparticularsData[0].name + '</a></b></p><button style="float:right" id="newItemform-container" name="' + data.newparticularsData[0].name + '" class="newEntityCreateButton btn btn-tree btn-idos" title="Create new Sub Account" onclick="javascript:createCOA(this,' + data.newparticularsData[0].id + ');"><i class="fa fa-plus pr-3"></i>Add Sub Account</button></li>');
                    }
                }
                $("#notificationMessage").html("Account has been added/Updated successfully.");
                $("#newItemform-container-close']").trigger('click');
                $('.notify-success').show();
                alwaysScrollTop();
            },
            error: function (xhr, status, error) {
                if (xhr.status == 401) {
                    doLogout();
                }
            }
        });
    });
});

function checkDuplicacy(domId, entityName, entityColName) {
    var parentDiv = $("#" + domId + "").parent("div:first").attr('class');
    var value = $("#" + domId + "").val();
    if (enteredBranchSpecifics == value) {
        enteredBranchSpecifics = "";
        return false;
    }
    let topLevelParentActCode = $("#topLevelParentActCode").val();
    let headType = "1";
    if(topLevelParentActCode = "2000000000000000000"){
        headType = "2"
    }else if(topLevelParentActCode = "3000000000000000000"){
        headType = "3"
    }else if(topLevelParentActCode = "4000000000000000000"){
        headType = "4";
    }
    if(entityName == 'Specifics')
        var url = "/config/" + entityName + "/checkDuplicacy/" + value + "/" + headType;
    else
        var url = "/config/" + entityName + "/checkDuplicacy/" + value;
    $.ajax({
        url: url,
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        method: "GET",
        contentType: 'application/json',
        success: function (data) {
            if (data.ispresent == 'true') {
                swal("Duplicate data error!", "Item already present, please add different item.", "error");
                $("#" + domId + "").val("");
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        }
    });
}

var enableDisableCoaButton = function (isEnable) {
    if (!isEnable) {
        $("#addSpecfBtn").attr("disabled", "disabled");
    } else {
        $("#addSpecfBtn").removeAttr("disabled");
        $.unblockUI();
    }
}

/*var enableDisableCoaBranchButton = function (isEnable) {
    if (!isEnable) {
        $("#addSpecfBrnchBtn").attr("disabled", "disabled");
    } else {
        $("#addSpecfBrnchBtn").removeAttr("disabled");
        $.unblockUI();
    }
}*/

function addSpecifics() {
    enableDisableCoaButton(false);
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var topLevelParentActCode = $("#topLevelParentActCode").val();
    var itemEntityHiddenId = $("#itemEntityHiddenId").val();
    var btnname = this.name;
    var specName = $("#items").val();
    var taxFormulaData = {};
    var parentId = $('select[name="itemCategory"]').val();
    var parentText = $('select[name="itemCategory"] option:selected').text();
    var item_branch_values = $('#itemBranch2BtnList').find('.itemBranch2Class-cb:checked').map(function () {
        return this.value;
    }).get();
    if(item_branch_values.toString() == ""){
        swal("Invalid branch", "Please provide valid branches", "error");
        enableDisableCoaButton(true);
        return false;
    }
    var branchOpeningBalance = "";
    var walkinCustDiscount = "";
    var branchInvNoOfUnit = "";
    var branchInvRate = "";
    var branchInvOpeningBalance = "";
    if (topLevelParentActCode == "1000000000000000000") {
        var gstTaxRate = $("#GSTTaxRate").val();
        var cessTaxRate = $("#cessTaxRate").val();
        if (cessTaxRate != "" || gstTaxRate != "") {
            var taxApplicableDate = $("#applicableDate").val();
            if (taxApplicableDate == "") {
                swal("Invalid Date", "Applicable Date should be filled", "error");
                enableDisableCoaButton(true);
                return false;
            }
        }
        for (var i in item_branch_values) {
            branchOpeningBalance += $("#item-form-container li[id='" + item_branch_values[i] + "'] input[class='openingBalance']").val() + ",";
            walkinCustDiscount += $('#item-form-container #itemBranch2Class-cb' + item_branch_values[i]).val() + ",";
        }
        if (walkinCustDiscount.length > 0) {
            walkinCustDiscount = walkinCustDiscount.substring(0, walkinCustDiscount.length);
        }
        // ****************************************************************************************************************
        var branch_tax_formula_hiddenKeys = $('#tax-coa-container input[name="branchIncomeCoaHidPrimKey"]').map(function () {
            return this.value;
        }).get();

        var taxNames = $('#tax-coa-container input[id="taxName"]').map(function () {
            return this.value;
        }).get();
        if ((taxNames.indexOf('SGST') !== -1 && taxNames.indexOf('CGST') === -1) || (taxNames.indexOf('SGST') === -1 && taxNames.indexOf('CGST') !== -1)) {
            swal("Error on Tax setup!", "SGST and CGST should be configured together.", "error");
            enableDisableCoaButton(true);
            return false;
        }
        var taxRates = $('#tax-coa-container input[id="itemgstrate"]').map(function () {
            return this.value;
        }).get();
        var add_deduct_values = $('#tax-coa-container select[id="taxAddDeduct"] option:selected').map(function () {
            return this.value;
        }).get();
        var rate_applied_to = $('#tax-coa-container select[id="taxApplyTo"] option:selected').map(function () {
            return this.value;
        }).get();
        var tax_formula = $('#tax-coa-container input[id="taxFormula"]').map(function () {
            return this.value;
        }).get();
        var invoice_value = $('#tax-coa-container input[id="taxInvoiceValue"]').map(function () {
            return this.value;
        }).get();

        var addsDeducts = "";
        var appliedTos = "";
        var formulas = "";
        var invoiceValues = "";
        var taxNameList = "";
        var taxRateList = "";
        for (var i = 0; i < taxNames.length; i++) {
            if(add_deduct_values[i] == "" && taxRates[i] != ""){
                swal("Error on Tax setup!", "Invalid 'Add/Deduct' for " + taxNames[i], "error");
                enableDisableCoaButton(true);
                return false;
            }else {
                addsDeducts += add_deduct_values[i] + ",";
            }
            if(rate_applied_to[i] == ""  && taxRates[i] != ""){
                swal("Error on Tax setup!", "Invalid 'Apply To' for " + taxNames[i], "error");
                enableDisableCoaButton(true);
                return false;
            }else {
                appliedTos += rate_applied_to[i] + ",";
            }
            if(tax_formula[i] == ""  && taxRates[i] != ""){
                swal("Error on Tax setup!", "Invalid Tax Amount for " + taxNames[i], "error");
                enableDisableCoaButton(true);
                return false;
            }else {
                formulas += tax_formula[i] + ",";
            }
            if(invoice_value[i] == ""  && taxRates[i] != ""){
                swal("Error on Tax setup!", "Invalid Invoice Value(IV) for " + taxNames[i], "error");
                enableDisableCoaButton(true);
                return false;
            }else {
                invoiceValues += invoice_value[i] + ",";
            }
            taxNameList += taxNames[i] + ",";
            taxRateList += taxRates[i] + ",";
        }
        taxFormulaData.taxNames = taxNameList.substring(0, taxNameList.length - 1);
        taxFormulaData.taxRates = taxRateList.substring(0, taxRateList.length - 1);
        taxFormulaData.applyRulesToMultiItemsList = $("#multiItemsListHidden").val();
        taxFormulaData.addsDeducts = addsDeducts.substring(0, addsDeducts.length - 1);
        taxFormulaData.appliedTos = appliedTos.substring(0, appliedTos.length - 1);
        taxFormulaData.formulas = formulas.substring(0, formulas.length - 1);
        taxFormulaData.invoiceValues = invoiceValues.substring(0, invoiceValues.length - 1);
        //**************************************************************************************************************
    } else if (topLevelParentActCode == "2000000000000000000") {
        for (var i in item_branch_values) {
            branchOpeningBalance += $("#item-form-container li[id='" + item_branch_values[i] + "'] input[class='openingBalance']").val() + ",";
            branchInvNoOfUnit += $("#item-form-container li[id='" + item_branch_values[i] + "'] input[id='noOfUnits']").val() + ",";
            branchInvRate += $("#item-form-container li[id='" + item_branch_values[i] + "'] input[id='inventoryRate']").val() + ",";
            branchInvOpeningBalance += $("#item-form-container li[id='" + item_branch_values[i] + "'] input[id='inventoryValue']").val() + ",";
        }
        if (branchInvNoOfUnit.length > 0) {
            branchInvNoOfUnit = branchInvNoOfUnit.substring(0, branchInvNoOfUnit.length);
        }
        if (branchInvRate.length > 0) {
            branchInvRate = branchInvRate.substring(0, branchInvRate.length);
        }
        if (branchInvOpeningBalance.length > 0) {
            branchInvOpeningBalance = branchInvOpeningBalance.substring(0, branchInvOpeningBalance.length);
        }
    } else if (topLevelParentActCode == "3000000000000000000" || topLevelParentActCode == "4000000000000000000") {
        for (var i in item_branch_values) {
            branchOpeningBalance += $("#item-form-container li[id='" + item_branch_values[i] + "'] input[class='openingBalance']").val() + ",";
        }
    }
    if (branchOpeningBalance.length > 0) {
        branchOpeningBalance = branchOpeningBalance.substring(0, branchOpeningBalance.length);
    }
    // $("a[id*='form-container-close']").attr("href", location.hash);
    $("#newItemform-container-close").attr("href", location.hash);
    if (specName == "") {
        swal("Data error!", "Invalid Item Name.", "error");
        enableDisableCoaButton(true);
        return false;
    }
    var domId = 'items';
    if (enteredBranchSpecifics == specName) {
        enteredBranchSpecifics = "";
        enableDisableCoaButton(true);
        return false;
    }

    let headType = "1";
    if(topLevelParentActCode == "2000000000000000000"){
        headType = "2";
    }else if(topLevelParentActCode == "3000000000000000000"){
        headType = "3";
        //datavalidationall = "4";
    }else if(topLevelParentActCode == "4000000000000000000"){
        headType = "4";
        //datavalidationall = "5";
    }
    var url = "/config/Specifics/checkDuplicacy/" + specName + "/" + headType;
    $.ajax({
        url: url,
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        method: "GET",
        contentType: 'application/json',
        success: function (data) {
            if (data.ispresent == 'true') {
                if (data.dataid != itemEntityHiddenId) {
                    $("#" + domId + "").val("");
                    enableDisableCoaButton(true);
                    return false;
                }
            }
            var jsonData = {};
            jsonData.datavalidationall = $("#datavalidationall option:selected").val();
            jsonData.openingBalance = $("#openingBalance").val();
            var item_transaction_purposes = $('#partBasedTransaction option[value!="multiselect-all"]:selected').map(function () {
                return this.value;
            }).get();
            jsonData.itemTransactionPurpose = item_transaction_purposes.toString();
            var isInputTaxCreditItem = $("#isInputTaxCreditItem").val();
            if (typeof isInputTaxCreditItem != 'undefined') {
                jsonData.isInputTaxCreditItem = isInputTaxCreditItem;
            } else {
                jsonData.isInputTaxCreditItem = '0';
            }
            if (topLevelParentActCode == "1000000000000000000" || topLevelParentActCode == "2000000000000000000") {
                jsonData.GSTDesc = $("#GSTDesc").val();
                jsonData.taxApplicableDate = $("#applicableDate").val();
                var GSTItemSelected = $('select[name="GSTItems"]').map(function () {
                    return this.value;
                }).get();
                jsonData.GSTItemSelected = GSTItemSelected.toString();
                jsonData.GSTCode = $("#GSTCode").val();
                if (topLevelParentActCode == "2000000000000000000") {
                    var gstTaxRateSelected = $("#GSTRateSelect").val();
                    var cessTaxRateSelected = $("#CessRateSelect").val();
                    var gstTaxRateSelected = $('#GSTRateSelect option[value!="multiselect-all"]:selected').map(function () {
                        return this.value;
                    }).get();
                    gstTaxRateSelected = gstTaxRateSelected.toString();

                    var cessTaxRateSelected = $('#CessRateSelect option[value!="multiselect-all"]:selected').map(function () {
                        return this.value;
                    }).get();
                    cessTaxRateSelected = cessTaxRateSelected.toString();

                    jsonData.gstTaxRateSelected = gstTaxRateSelected;
                    jsonData.cessTaxRateSelected = cessTaxRateSelected;
                    if (gstTaxRateSelected != "" && gstTaxRateSelected.indexOf("OTHER") != -1) {
                        jsonData.GSTTaxRate = $("#GSTTaxRate").val();
                    } else {
                        jsonData.GSTTaxRate = "";
                    }

                    if (cessTaxRateSelected != "" && cessTaxRateSelected.indexOf("OTHER") != -1) {
                        jsonData.cessTaxRate = $("#cessTaxRate").val();
                    } else {
                        jsonData.cessTaxRate = "";
                    }
                } else {
                    jsonData.GSTTaxRate = $("#GSTTaxRate").val();
                    jsonData.cessTaxRate = $("#cessTaxRate").val();

                }

                var GSTItemCategory = $('select[name="GSTItemCategory"]').map(function () {
                    return this.value;
                }).get();
                jsonData.GSTItemCategory = GSTItemCategory.toString();
                var GSTtypeOfSupply = $('select[name="GSTtypeOfSupply"]').map(function () {
                    return this.value;
                }).get();
                jsonData.GSTtypeOfSupply = GSTtypeOfSupply.toString();
                if (topLevelParentActCode == "1000000000000000000") {
                    /*var GSTtypeOfSupply = ($('#goodsCheckbox1').prop('checked') ? '1' : '') || ($('#servicesCheckbox1').prop('checked') ? '2' : '');
                    jsonData.GSTtypeOfSupply = GSTtypeOfSupply.toString();
                    jsonData.GSTApplicable= $('#goodsCheckbox2').prop('checked') || $('#servicesCheckbox2').prop('checked');*/
                                         
                    var combination_sales_items_list = "";
                    var combination_sales_items = $('select[id="incomeItemsForCombSales"]').map(function () {
                        return this.value;
                    }).get();
                    for (var i = 0; i < combination_sales_items.length; i++) {
                        if (combination_sales_items[i] != "") {
                            combination_sales_items_list += combination_sales_items[i] + ",";
                        } else {
                            combination_sales_items_list += " " + ",";
                        }
                    }
                    jsonData.combinationSalesItems = combination_sales_items_list.substring(0, combination_sales_items_list.length - 1);
                    var combination_sales_units_list = "";
                    var combination_sales_units = $('input[id="openBalUnitsForCombSales"]').map(function () {
                        return this.value;
                    }).get();
                    for (var i = 0; i < combination_sales_units.length; i++) {
                        if (combination_sales_units[i] != "") {
                            combination_sales_units_list += combination_sales_units[i] + ",";
                        } else {
                            combination_sales_units_list += " " + ",";
                        }
                    }
                    jsonData.combinationSalesUnits = combination_sales_units_list.substring(0, combination_sales_units_list.length - 1);
                    var combination_sales_rates_list = "";
                    var combination_sales_rates = $('input[id="openBalRateForCombSales"]').map(function () {
                        return this.value;
                    }).get();
                    for (var i = 0; i < combination_sales_rates.length; i++) {
                        if (combination_sales_rates[i] != "") {
                            combination_sales_rates_list += combination_sales_rates[i] + ",";
                        } else {
                            combination_sales_rates_list += " " + ",";
                        }
                    }
                    jsonData.combinationSalesRates = combination_sales_rates_list.substring(0, combination_sales_rates_list.length - 1);
                    jsonData.incomeSpecificPerUnitPrice = $("#incomespecfPerUnitPrice").val();
                    jsonData.incomeexpense = $("#partBasedDynmData option:selected").val();
                    jsonData.incometoexpensemapping = $("#incomeToExpId option:selected").val();
                    jsonData.noOfExpUnit = $("#noOfExpUnit").val();
                    jsonData.noOfIncUnit = $("#noOfIncUnit").val();
                    jsonData.incUnitMeasure = $("#incUnitMeasure").val();
                    jsonData.expUnitMeasure = $("#expUnitMeasure").val();
                    jsonData.tradingInvCalcMethod = $("#tradingInvCalcMethod option:selected").val();
                    var branchAlertUserForReorderLevel = "";
                    var branchId = "";
                    var branchReorderLevel = "";
                    if ($("#incomeToExpId option:selected").val() != "") {
                        $('input[name="branchcheck1"]:checkbox:checked').map(function () {
                            if (this.value != "") {
                                branchId = branchId + this.value + ",";
                                branchAlertUserForReorderLevel = branchAlertUserForReorderLevel + $("#reorderlevelselectalertuserid" + this.value + "").val() + ",";
                                branchReorderLevel = branchReorderLevel + $("#reorderlevelinputid" + this.value + "").val() + ",";
                            }
                        }).get();
                    }
                    if (branchId != "") {
                        branchId = branchId.substring(0, branchId.length - 1);
                        branchAlertUserForReorderLevel = branchAlertUserForReorderLevel.substring(0, branchAlertUserForReorderLevel.length - 1);
                        branchReorderLevel = branchReorderLevel.substring(0, branchReorderLevel.length - 1);
                    }
                    jsonData.reorderLevelbranchIds = branchId;
                    jsonData.branchAlertUserForReorderLevels = branchAlertUserForReorderLevel;
                    jsonData.branchReorderLevels = branchReorderLevel;
                    jsonData.invoiceItemDescription1 = $("#itemInvoiceDescription1").val();
                    jsonData.invoiceItemDescription2 = $("#itemInvoiceDescription2").val();
                    jsonData.itemBarcodeNo = $(".itemBarcode").val();
                    var itemInvoiceDesc1CheckTmp = $("#itemInvoiceDesc1Check").is(':checked');
                    if (itemInvoiceDesc1CheckTmp) {
                        jsonData.itemInvoiceDesc1Check = 1;
                    } else {
                        jsonData.itemInvoiceDesc1Check = 0;
                    }
                    var itemPriceInclusiveTmp = $("#itemPriceInclusive").is(':checked');
                    if (itemPriceInclusiveTmp) {
                        jsonData.itemPriceInclusive = 1;
                    } else {
                        jsonData.itemPriceInclusive = 0;
                    }
                    var itemInvoiceDesc2CheckTmp = $("#itemInvoiceDesc2Check").is(':checked');
                    if (itemInvoiceDesc2CheckTmp) {
                        jsonData.itemInvoiceDesc2Check = 1;
                    } else {
                        jsonData.itemInvoiceDesc2Check = 0;
                    }
                    var isTranEditableCheckTmp = $("#isTranEditableCheck").is(':checked');
                    if (isTranEditableCheckTmp) {
                        jsonData.isTranEditableCheck = 1;
                    } else {
                        jsonData.isTranEditableCheck = 0;
                    }
                } else if (topLevelParentActCode == "2000000000000000000") {
                    jsonData.expenseSpecfWithholdingApplicable = $("#withHoldingApplicable option:selected").val();
                    jsonData.expenseSpecfWithholdingType = $("#withHoldingType option:selected").val();
                    jsonData.expenseSpecfCaptureInputTaxes = $("#captureInputTaxesApplicable option:selected").val();
                    jsonData.expenseSpecfWithholdingRate = $("#withHoldingRate").val();
                    jsonData.expenseSpecfWithholdingLimit = $("#withHoldingLimit").val();
                    jsonData.expenseSpecfWithholdingMonetoryLimit = $("#withHoldingMonetoryLimit").val();
                    jsonData.itemBarcodeNo = $(".itemBarcode").val();
                    var specf_unit_price = "";
                    var specifics_unit_price = $('input[name="specfPerUnitPrice"]').map(function () {
                        return this.value;
                    }).get();
                    for (var i = 0; i < specifics_unit_price.length; i++) {
                        if (specifics_unit_price[i] != "") {
                            specf_unit_price += specifics_unit_price[i] + ",";
                        } else {
                            specf_unit_price += " " + ",";
                        }
                    }
                    jsonData.specfUnitPrice = specf_unit_price.substring(0, specf_unit_price.length - 1);
                    jsonData.incomeexpense = $("#partBasedDynmData option:selected").val();
                    var docUploadRuleMonetoryLimitForExpenseItemInIndividualBranches = "";
                    var docUploadRuleExpenseItemBranches = $('.docuploadrulecustomdropdownBranchList li[id="docuploadrulecustomdropdownBranchlist"] input[name="customdoccheckBranch"]:checkbox:checked').map(function () {
                        var value = this.value;
                        if (value != "") {
                            docUploadRuleMonetoryLimitForExpenseItemInIndividualBranches += $(".docuploadrulecustomdropdownBranchList li[id='docuploadrulecustomdropdownBranchlist'] input[id='monetoryLimit" + value + "']").val() + ",";
                            return value;
                        }
                    }).get();
                    jsonData.docUploadRuleExpenseItemBranchesStr = docUploadRuleExpenseItemBranches.toString();
                    jsonData.docUploadRuleMonetoryLimitsForExpenseItemInIndividualBranches = docUploadRuleMonetoryLimitForExpenseItemInIndividualBranches.substring(0, docUploadRuleMonetoryLimitForExpenseItemInIndividualBranches.length - 1);

                    jsonData.isEmployeeClaimItem = $("select[name='isItemEmpClaimItem'] option:selected").val();
                    jsonData.expUnitMeasure = $("#expUnitMeasure").val();
                    jsonData.expNoOfOpeningBalUnits = $('#expNoOfOpeningBalUnits').val();
                    jsonData.expRateOpeningBalUnits = $('#expRateOpeningBalUnits').val();
                    jsonData.expOpeningBal = $('#expOpeningBal').val();
                    jsonData.branchInvNoOfUnit = branchInvNoOfUnit;
                    jsonData.branchInvRate = branchInvRate;
                    jsonData.branchInvOpeningBalance = branchInvOpeningBalance;
                }
                jsonData.datavalidation_pl_bs = "";

                jsonData.coaTDSData = getCOATdsData();
                var isTDsVendSpecfic = $("#isTDsVendSpecfic").is(':checked');
                if (isTDsVendSpecfic) {
                    jsonData.isTdsVendSpecific = 1;
                } else {
                    jsonData.isTdsVendSpecific = 0;
                }

                var isCompositionItem = $("#isCompositionItem").is(':checked');
                if (isTDsVendSpecfic) {
                    jsonData.isCompositionItem = 1;
                } else {
                    jsonData.isCompositionItem = 0;
                }
            } else if (topLevelParentActCode == "3000000000000000000") {
                jsonData.isFixedAssetsSelectValue = $("select[name='isFixedAssetsSelectId'] option:selected").val();
                if ($("select[name='isFixedAssetsSelectId'] option:selected").val() == "1") {
                    jsonData.isFixedAssetsCapitalizaAmountInput = $("#isFixedAssetsCapitalizaAmountInputId").val();
                    jsonData.isFixedAssetsThresholdLimitInput = $("#isFixedAssetsThresholdLimitInputId").val();
                    jsonData.isFixedAssetsLifeSpanInput = $("#isFixedAssetsLifeSpanInputId").val();
                }
                jsonData.isMovableImmovableSelectValue = $("select[name='isMovableImmovableSelect'] option:selected").val();
                jsonData.isTaggableSelectValue = $("select[name='isTaggableSelect'] option:selected").val();
                var taggableCode = "";
                if ($("select[name='isTaggableSelect'] option:selected").val() == "1") {
                    taggableCode = $("#taggableCode").val();
                }
                jsonData.taggableCodeValue = taggableCode;
            }
            jsonData.useremail = $("#hiddenuseremail").text();
            jsonData.itemHiddenPrimaryKey = itemEntityHiddenId;
            jsonData.topMostParentCode = topLevelParentActCode;
            jsonData.specificsName = specName;
            jsonData.specificsParentId = parentId;
            jsonData.specificsParentText = parentText;
            jsonData.btnName = btnname;
            jsonData.itemBchValues = item_branch_values.toString();
            jsonData.walkinCustDiscount = walkinCustDiscount;
            jsonData.branchOpeningBalance = branchOpeningBalance;
            var specf_knowledge_Library_HidIds = "";
            var knowledge_Library_Hidden_Ids = $('input[name="specfHidPrimKey"]').map(function () {
                return this.value;
            }).get();
            for (var i = 0; i < knowledge_Library_Hidden_Ids.length; i++) {
                if (knowledge_Library_Hidden_Ids[i] != "") {
                    specf_knowledge_Library_HidIds += knowledge_Library_Hidden_Ids[i] + ",";
                } else {
                    specf_knowledge_Library_HidIds += " " + ",";
                }
            }
            jsonData.knowledgeLibraryHiddenIds = specf_knowledge_Library_HidIds.substring(0, specf_knowledge_Library_HidIds.length - 1);
            var specf_knowledge_Library_Content = "";
            var knowledge_Library_Content = $('textarea[name="knowledgeLibRaryContent"]').map(function () {
                return this.value
            }).get();
            for (var i = 0; i < knowledge_Library_Content.length; i++) {
                if (knowledge_Library_Content[i] != "") {
                    specf_knowledge_Library_Content += knowledge_Library_Content[i] + ",";
                } else {
                    specf_knowledge_Library_Content += " " + ",";
                }
            }
            jsonData.knowledgeLibraryContent = specf_knowledge_Library_Content.substring(0, specf_knowledge_Library_Content.length - 1);
            var is_knowledge_Library_Mandatory = $('select[name="mandatory"]').map(function () {
                return this.value;
            }).get();
            jsonData.isknowledgeLibraryMandatory = is_knowledge_Library_Mandatory.toString();
            var knowledge_library_for_branches = "";
            $("select[name='knowledgeLibraryInBranches']").each(function () {
                var knowledge_library_branches = $(this).find('option[value!="multiselect-all"]:selected').map(function () {
                    return this.value;
                }).get();
                if (knowledge_library_branches != "") {
                    var knowledge_liab_for_bnch = knowledge_library_branches.toString();
                    knowledge_library_for_branches += knowledge_liab_for_bnch + "#";
                } else {
                    knowledge_library_for_branches += " " + "#";
                }
            });
            jsonData.knowledgeLibraryForBranches = knowledge_library_for_branches.substring(0, knowledge_library_for_branches.length - 1);
            jsonData.taxFormulaData = taxFormulaData;
            var url = "/specifics/addSpecifics";
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
                    //showChartOFAccount();
                    enableDisableCoaButton(true);
                    if(data.role.includes("MASTER ADMIN")){
                        var itemTable=$("#itemTable");
                        var itemId=data.id;
                        var itemName=data.name;
                        var btnName=data.btnName;
                        var toplevelaccountCode=data.topLevelAccountCode;
                        var parentAccountCode=data.parentAccountCode;
                        var identDataValid=data.identDataValid;
                        var existingul=$("li[id="+parentAccountCode+"]").find("ul[id='mainChartOfAccount']").attr('id');
                        if(typeof existingul!='undefined'){
                            var eistingCoa=$("li[id="+parentAccountCode+"] ul[id='mainChartOfAccount']").find("li[id="+data.accountCode+"]").attr('id');
                            if(typeof eistingCoa!='undefined'){
                                $("li[id="+parentAccountCode+"] ul[id='mainChartOfAccount']").find("li[id="+data.accountCode+"]").html("");
                                if (identDataValid == "" || identDataValid == null || typeof identDataValid =='undefined' || identDataValid=="52" || identDataValid=="24" || identDataValid=="25" || identDataValid=="26" || identDataValid=="27") {
                                    $("li[id="+parentAccountCode+"] ul[id='mainChartOfAccount']").find("li[id="+data.accountCode+"]").append('<div class="chartOfAccountContainer"><img id="'+data.accountCode+'" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this);"></img><font class="color-grey"><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details'+data.id+'" href="#itemSetUp" class="color-grey" onclick="showItemEntityDetails(this,'+toplevelaccountCode+')">'+data.name+'</a></b></font></img><button style="float:right" id="newItemform-container" name="'+data.name+'" class="newEntityCreateButton btn btn-tree btn-idos" title="Create new Sub Account" onclick="javascript:createCOA(this,'+data.id+','+toplevelaccountCode+');"><i class="fa fa-plus"></i>Add Sub Account</button>');
                                }else{
                                    $("li[id="+parentAccountCode+"] ul[id='mainChartOfAccount']").find("li[id="+data.accountCode+"]").append('<div class="chartOfAccountContainer"><img id="'+data.accountCode+'" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this);"></img><font class="color-grey"><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details'+data.id+'" href="#itemSetUp" class="color-grey" onclick="showItemEntityDetails(this,'+toplevelaccountCode+')">'+data.name+'</a></span></b></font>');
                                }
                            }else if(typeof eistingCoa=='undefined'){
                                if (identDataValid == "" || identDataValid == null || typeof identDataValid =='undefined' || identDataValid=="52" || identDataValid=="24" || identDataValid=="25" || identDataValid=="26" || identDataValid=="27") {
                                    $("li[id="+parentAccountCode+"]").find("ul[id='mainChartOfAccount']").append('<li id='+data.accountCode+'><div class="chartOfAccountContainer"><img id="'+data.accountCode+'" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this);"></img><font class="color-grey"><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details'+data.id+'" href="#itemSetUp" class="color-grey" onclick="showItemEntityDetails(this,'+toplevelaccountCode+')">'+data.name+'</a></span></b></font></img><button style="float:right" id="newItemform-container" name="'+data.name+'" class="newEntityCreateButton btn btn-tree btn-idos" title="Create new Sub Account" onclick="javascript:createCOA(this,'+data.id+','+toplevelaccountCode+');"><i class="fa fa-plus"></i>Add Sub Account</button></li>');
                                }else{
                                    $("li[id="+parentAccountCode+"]").find("ul[id='mainChartOfAccount']").append('<li id='+data.accountCode+'><div class="chartOfAccountContainer"><img id="'+data.accountCode+'" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this);"></img><font class="color-grey"><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details'+data.id+'" href="#itemSetUp" class="color-grey" onclick="showItemEntityDetails(this,'+toplevelaccountCode+')">'+data.name+'</a></span></b></font></img>');
                                }
                            }
                        }
                        if(typeof existingul=='undefined'){
                            $("li[id="+parentAccountCode+"]").append('<ul id="mainChartOfAccount" class="treeview-black mainChartOfAccount"></ul>');
                            if (identDataValid == "" || identDataValid == null || typeof identDataValid =='undefined' || identDataValid=="52" || identDataValid=="24" || identDataValid=="25" || identDataValid=="26" || identDataValid=="27") {
                                $("li[id="+parentAccountCode+"]").find("ul[id='mainChartOfAccount']").append('<li id='+data.accountCode+'><div class="chartOfAccountContainer"><img id="'+data.accountCode+'" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this);"></img><font class="color-grey"><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details'+data.id+'" href="#itemSetUp" class="color-grey" onclick="showItemEntityDetails(this,'+toplevelaccountCode+')">'+data.name+'</a></span></b></font></img><button style="float:right" id="newItemform-container" name="'+data.name+'" class="newEntityCreateButton btn btn-tree btn-idos" title="Create new Sub Account" onclick="javascript:createCOA(this,'+data.id+','+toplevelaccountCode+');"><i class="fa fa-plus"></i>Add Sub Account</button></li>');
                            }else{
                                $("li[id="+parentAccountCode+"]").find("ul[id='mainChartOfAccount']").append('<li id='+data.accountCode+'><div class="chartOfAccountContainer"><img id="'+data.accountCode+'" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this);"></img><font class="color-grey"><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details'+data.id+'" href="#itemSetUp" class="color-grey" onclick="showItemEntityDetails(this,'+toplevelaccountCode+')">'+data.name+'</a></span></b></font></img></li>');
                            }
                        }
                        $("img[id="+parentAccountCode+"]").attr("src","assets/images/minus.png");
                        $("img[id="+parentAccountCode+"]").attr("onclick","javascript:removeCOA(this);");
                        $(".multiBranch option:selected").each(function () {
                            $(this).removeAttr('selected');
                        });
                        $('.multiBranch').multiselect('rebuild');
                        $("#items").val("");
                        $("#itemaccountcode").val("");
                        $("#itemCategory").val($("#itemCategory option:first").val());
                        var sel = $("#vendorItemList > li:first");
                        var custsel=$("#customerItemList > li:first");
                        if(parentAccountCode.startsWith("2")){
                            $("#vendorItemList li[name='"+itemId+"']").remove();
                            if(itemName.length > 25){
                                sel.after('<li id="vendoritemlist" name="'+itemId+'" isTdsSpecific="false">&nbsp;<input style="margin-bottom:5px;float: left;margin-left: 4px;" type="checkbox" id="checkboxid" name="vendoritemcheck" value="'+itemId+'" onClick="checkUncheck(this)">&nbsp;&nbsp;&nbsp;<input class="span1" type="text" style="width:100px;height:20px;float: left;margin-left: 10px;" id="unitPrice'+itemId+'" name="unitPrice" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="toggleCheck(this)"><span class="itemLabelClass">'+itemName+'</span><b style="float:right;"><input type="text" onkeypress="return onlyDotsAndNumbers(event);" onchange="onRcmTaxChange(this);"  class="cesRateVendItem" id="cesRateVendItem'+itemId+'" name="cesRateVendItem" rcmcessrate="" placeholder="Cess Rate" onkeyup="rcmToggleCheck(this);"><input type="text" onkeypress="return onlyDotsAndNumbers(event);" onchange="onRcmTaxChange(this);" rcmtaxrate="" class="rcmRateVendItem" name="rcmRateVendItem" placeholder="RCM Tax Rate" id="rcmRateVendItem'+itemId+'" onkeyup="rcmToggleCheck(this);"><input type="text" readonly class="VendRcmApplicableDate" onkeypress="return onlyDateAllow(event);" placeholder="Applicable Date" name="VendRcmApplicableDate" id="VendRcmApplicableDate'+itemId+'"></b></li>');
                            }else {
                                sel.after('<li id="vendoritemlist" name="'+itemId+'" isTdsSpecific="false">&nbsp;<input style="margin-bottom:5px;" type="checkbox" id="checkboxid" name="vendoritemcheck" value="'+itemId+'" onClick="checkUncheck(this)">&nbsp;&nbsp;&nbsp;<input class="span1" type="text" style="width:100px;height:20px;" id="unitPrice'+itemId+'" name="unitPrice" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="toggleCheck(this)">&nbsp;&nbsp;&nbsp;<span class="itemLabelClass">'+itemName+'</span>&nbsp;&nbsp;&nbsp;<b style="float:right;"><input type="text" onkeypress="return onlyDotsAndNumbers(event);" onchange="onRcmTaxChange(this);"  class="cesRateVendItem" id="cesRateVendItem'+itemId+'" name="cesRateVendItem" rcmcessrate="" placeholder="Cess Rate" onkeyup="rcmToggleCheck(this);"><input type="text" onkeypress="return onlyDotsAndNumbers(event);" onchange="onRcmTaxChange(this);" rcmtaxrate="" class="rcmRateVendItem" name="rcmRateVendItem" placeholder="RCM Tax Rate" id="rcmRateVendItem'+itemId+'" onkeyup="rcmToggleCheck(this);"><input type="text" readonly class="VendRcmApplicableDate" onkeypress="return onlyDateAllow(event);" placeholder="Applicable Date" name="VendRcmApplicableDate" id="VendRcmApplicableDate'+itemId+'"></b></li>');
                            }
                        }else if(parentAccountCode.startsWith("1")){
                            $("#customerItemList li[name='"+itemId+"']").remove();
                            custsel.after('<li id="customeritemlist" style="margin-top: 10px;" name="'+ itemId +'">&nbsp;<input style="margin-bottom:5px;float: left;margin-left: 3px;" class="custSinUsrCheck" type="checkbox" id="checkboxid" name="customeritemcheck" checked="true" value="'+ itemId +'" onClick="custcheckUncheck(this)">&nbsp;&nbsp;&nbsp;<input class="span1 custDiscSinUsr" type="text" id="custDiscount'+itemId+'" name="custDiscount" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="custtoggleCheck(this)" style="padding-right: 26px;width: 50px;margin-left: 5px;float: left;"><span style="position: relative;top: 8px;float: left;">%</span>&nbsp;<span class="itemLabelClass">'+ itemName +'</span>');
                        }
                        $("#notificationMessage").html("Items has been added/Updated successfully.");
                        $('.notify-success').show();
                        $("#newItemform-container-close").trigger('click');
                        $(".VendRcmApplicableDate").datepicker({
                                changeMonth : true,
                                changeYear : true,
                                dateFormat:  'MM d,yy',
                                yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
                                onSelect: function(x,y){
                                    $(this).focus();
                                }
                        });
                        $.unblockUI();
                        getVendorData();
                        getAllChartOfAccount();
                        $('html, body').animate({
                            scrollTop:  $("li[id="+parentAccountCode+"]").find("ul[id='mainChartOfAccount'] li[id="+data.accountCode+"]").offset().top-50
                        },20);
                    }
                },
                error: function (xhr, status, error) {
                    if (xhr.status == 401) {
                        doLogout();
                    } else if (xhr.status == 500) {
                        swal("Error on Saving/update COA!", "Please retry, if problem persists contact support team", "error");
                    }
                },
                complete: function (data) {
                    $.unblockUI();
                    enableDisableCoaButton(true);
                    resetCOATdsScreen();
                }
            });
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        },
        complete: function (data) {
            //$.unblockUI();
            enableDisableCoaButton(true);
        }
    });
}

/*function addBranchSpecifics() {
    enableDisableCoaBranchButton(false);
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var topLevelParentActCode = $("#topLevelParentActCode").val();
    var itemEntityHiddenId = $("#itemEntityHiddenId").val();
    var btnname = this.name;
    var specName = $("#items").val();
    var taxFormulaData = {};
    var parentId = $('select[name="itemCategory"]').val();
    var parentText = $('select[name="itemCategory"] option:selected').text();
    var item_branch_values = $('#itemBranch2BtnList').find('.itemBranch2Class-cb:checked').map(function () {
        return this.value;
    }).get();
    if(item_branch_values.toString() == ""){
        swal("Invalid branch", "Please provide valid branches", "error");
        enableDisableCoaBranchButton(true);
        return false;
    }
    var branchOpeningBalance = "";
    var walkinCustDiscount = "";
    var branchInvNoOfUnit = "";
    var branchInvRate = "";
    var branchInvOpeningBalance = "";
    if (topLevelParentActCode == "3000000000000000000" || topLevelParentActCode == "4000000000000000000") {
        for (var i in item_branch_values) {
            branchOpeningBalance += $("#item-form-container li[id='" + item_branch_values[i] + "'] input[class='openingBalance']").val() + ",";
        }
    }
    if (branchOpeningBalance.length > 0) {
        branchOpeningBalance = branchOpeningBalance.substring(0, branchOpeningBalance.length);
    }
    // $("a[id*='form-container-close']").attr("href", location.hash);
    $("#newItemform-container-close").attr("href", location.hash);
    if (specName == "") {
        swal("Data error!", "Invalid Item Name.", "error");
        enableDisableCoaBranchButton(true);
        return false;
    }
    var domId = 'items';
    if (enteredBranchSpecifics == specName) {
        enteredBranchSpecifics = "";
        enableDisableCoaBranchButton(true);
        return false;
    }

    let headType = "1";
    let datavalidationall = "";
    if(topLevelParentActCode == "3000000000000000000"){
        headType = "3";
        datavalidationall = "4";
    }else if(topLevelParentActCode == "4000000000000000000"){
        headType = "4";
        datavalidationall = "5";
    }
    var url = "/config/Specifics/checkDuplicacy/" + specName + "/" + headType;
    $.ajax({
        url: url,
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        method: "GET",
        contentType: 'application/json',
        success: function (data) {
            if (data.ispresent == 'true') {
                if (data.dataid != itemEntityHiddenId) {
                    $("#" + domId + "").val("");
                    enableDisableCoaBranchButton(true);
                    return false;
                }
            }
            var jsonData = {};
            jsonData.datavalidationall = datavalidationall;
            for (var i in item_branch_values) {
                branchOpeningBalance += $("#item-form-container li[id='" + item_branch_values[i] + "'] input[class='openingBalance']").val() + ",";
                branchInvNoOfUnit += $("#item-form-container li[id='" + item_branch_values[i] + "'] input[id='noOfUnits']").val() + ",";
                branchInvRate += $("#item-form-container li[id='" + item_branch_values[i] + "'] input[id='inventoryRate']").val() + ",";
                branchInvOpeningBalance += $("#item-form-container li[id='" + item_branch_values[i] + "'] input[id='inventoryValue']").val() + ",";
            }
            jsonData.openingBalance = $("#openingBalance").val();
            var item_transaction_purposes = $('#partBasedTransaction option[value!="multiselect-all"]:selected').map(function () {
                return this.value;
            }).get();
            jsonData.itemTransactionPurpose = item_transaction_purposes.toString();
            var isInputTaxCreditItem = $("#isInputTaxCreditItem").val();
            if (typeof isInputTaxCreditItem != 'undefined') {
                jsonData.isInputTaxCreditItem = isInputTaxCreditItem;
            } else {
                jsonData.isInputTaxCreditItem = '0';
            }
            jsonData.useremail = $("#hiddenuseremail").text();
            jsonData.itemHiddenPrimaryKey = itemEntityHiddenId;
            jsonData.topMostParentCode = topLevelParentActCode;
            jsonData.specificsName = specName;
            jsonData.specificsParentId = parentId;
            jsonData.specificsParentText = parentText;
            jsonData.btnName = btnname;
            jsonData.itemBchValues = item_branch_values.toString();
            jsonData.walkinCustDiscount = walkinCustDiscount;
            jsonData.branchOpeningBalance = branchOpeningBalance;

            //branch bank accounts data start
            var bnchBankActHidId = $('input[name="bnchBnkActhiddenId"]').val();
            
            var bnchBankActBankName = $('input[name="items"]').val();

            var bnchBankActAccountNumber = $('input[name="bnkActNumber"]').val();
            if(bnchBankActAccountNumber == "" || bnchBankActAccountNumber == null){
                swal("Error", "Please enter Bank Account Number", "error");
            }

            var bnchBankActAuthSignName = $('input[name="authSignName"]').val();

            var bnchBankActAuthSignEmail = $('input[name="authSignEmail"]').val();

            var bnchBankActAccountType = $('select[name="bnkActType"] option:selected').val();

            var bnchBankActOpeningBalance = $('input[name="bnkActOpeningBalance"]').val();

            var bnchBankActAddress = $('textarea[name="bnkAddress"]').val();
            var bnchBankActPhnNoCtryCodeVal = $('select[name="bnchbnkactPhnNocountryCode"] option:selected').val();
            var bnchBankActPhnNoCtryCodeText = $('select[name="bnchbnkactPhnNocountryCode"] option:selected').text();
            var bnchBankActPhnNo1 = $('input[name="bankPhnNumber1"]').val();
            var bnchBankActPhnNo2 = $('input[name="bankPhnNumber2"]').val();
            var bnchBankActPhnNo3 = $('input[name="bankPhnNumber3"]').val();
            var bnchBankActSwiftCode = $('input[name="bnkSwiftCode"]').val();
            
            var bnchBankActIfscCode = $('input[name="bnkIfscCode"]').val();

            var bnchBankActRoutingNumber = $('input[name="routingNumber"]').val();
            
            var branchBankActHidId = "";
            var branchBankActBankName = "";
            var branchBankActAccountType = "";
            var branchBankActAccountNumber = "";
            var branchBankActRoutingNumber = "";
            var branchBankActSwiftCode = "";
            var branchBankActIfscCode = "";
            var branchBankActAddress = "";
            var branchBankActPhnNoCtryCode = "";
            var branchBankActPhnNo = "";
            var branchBankActAuthSignName = "";
            var branchBankActAuthSignEmail = "";

            var branchBankActOpeningBal = "";

            branchBankActHidId = bnchBankActHidId;
            branchBankActBankName = bnchBankActBankName;
            branchBankActAccountType = bnchBankActAccountType;
            branchBankActAccountNumber = bnchBankActAccountNumber;
            branchBankActOpeningBal = bnchBankActOpeningBalance;
            branchBankActAuthSignName = bnchBankActAuthSignName;
            branchBankActAuthSignEmail = bnchBankActAuthSignEmail
            branchBankActAddress = bnchBankActAddress;
            branchBankActPhnNoCtryCode = bnchBankActPhnNoCtryCodeText;
            branchBankActPhnNo = bnchBankActPhnNoCtryCodeVal + "-" + bnchBankActPhnNo1 + bnchBankActPhnNo2 + bnchBankActPhnNo3;
            branchBankActSwiftCode = bnchBankActSwiftCode;
            branchBankActIfscCode = bnchBankActIfscCode;
            branchBankActRoutingNumber = bnchBankActRoutingNumber;

            if(topLevelParentActCode == "3000000000000000000" && branchBankActAccountNumber !=""){
                jsonData.datavalidationall = 4;
            }else if(topLevelParentActCode == "4000000000000000000" && branchBankActAccountNumber !=""){
                jsonData.datavalidationall = 5;
            }
            jsonData.branchBankAccountHidId = branchBankActHidId;
            jsonData.branchBankAccountBankName = branchBankActBankName;
            jsonData.branchBankAccountType = branchBankActAccountType;
            jsonData.branchBankAccountNumber = branchBankActAccountNumber;
            jsonData.branchBankAccountOpeningBalance = branchBankActOpeningBal;
            jsonData.branchBankAccounttAuthSignName = branchBankActAuthSignName;
            jsonData.branchBankAccounttAuthSignEmail = branchBankActAuthSignEmail;
            jsonData.branchBankAccounttAddress = branchBankActAddress;
            jsonData.branchBankAccounttPhnNoCtryCode = branchBankActPhnNoCtryCode;
            jsonData.branchBankAccountPhnNo = branchBankActPhnNo;
            jsonData.branchBankAccountSwiftCode = branchBankActSwiftCode;
            jsonData.branchBankAccounttIfscCode = branchBankActIfscCode;
            jsonData.branchBankAccountRoutingNumber = branchBankActRoutingNumber;
            //branch bank accounts data end

            var specf_knowledge_Library_HidIds = "";
            var knowledge_Library_Hidden_Ids = $('input[name="specfHidPrimKey"]').map(function () {
                return this.value;
            }).get();
            for (var i = 0; i < knowledge_Library_Hidden_Ids.length; i++) {
                if (knowledge_Library_Hidden_Ids[i] != "") {
                    specf_knowledge_Library_HidIds += knowledge_Library_Hidden_Ids[i] + ",";
                } else {
                    specf_knowledge_Library_HidIds += " " + ",";
                }
            }
            jsonData.knowledgeLibraryHiddenIds = specf_knowledge_Library_HidIds.substring(0, specf_knowledge_Library_HidIds.length - 1);
            var specf_knowledge_Library_Content = "";
            var knowledge_Library_Content = $('textarea[name="knowledgeLibRaryContent"]').map(function () {
                return this.value
            }).get();
            for (var i = 0; i < knowledge_Library_Content.length; i++) {
                if (knowledge_Library_Content[i] != "") {
                    specf_knowledge_Library_Content += knowledge_Library_Content[i] + ",";
                } else {
                    specf_knowledge_Library_Content += " " + ",";
                }
            }
            jsonData.knowledgeLibraryContent = specf_knowledge_Library_Content.substring(0, specf_knowledge_Library_Content.length - 1);
            var is_knowledge_Library_Mandatory = $('select[name="mandatory"]').map(function () {
                return this.value;
            }).get();

            jsonData.isknowledgeLibraryMandatory = is_knowledge_Library_Mandatory.toString();
            var knowledge_library_for_branches = "";
            $("select[name='knowledgeLibraryInBranches']").each(function () {
                var knowledge_library_branches = $(this).find('option[value!="multiselect-all"]:selected').map(function () {
                    return this.value;
                }).get();
                if (knowledge_library_branches != "") {
                    var knowledge_liab_for_bnch = knowledge_library_branches.toString();
                    knowledge_library_for_branches += knowledge_liab_for_bnch + "#";
                } else {
                    knowledge_library_for_branches += " " + "#";
                }
            });
            jsonData.knowledgeLibraryForBranches = knowledge_library_for_branches.substring(0, knowledge_library_for_branches.length - 1);
            jsonData.taxFormulaData = taxFormulaData;
            var url = "/specifics/addSpecifics";
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
                    //showChartOFAccount();
                    enableDisableCoaBranchButton(true);
                    if(data.role.includes("MASTER ADMIN")){
                        var itemTable=$("#itemTable");
                        var itemId=data.id;
                        var itemName=data.name;
                        var btnName=data.btnName;
                        var toplevelaccountCode=data.topLevelAccountCode;
                        var parentAccountCode=data.parentAccountCode;
                        var identDataValid=data.identDataValid;
                        var existingul=$("li[id="+parentAccountCode+"]").find("ul[id='mainChartOfAccount']").attr('id');
                        if(typeof existingul!='undefined'){
                            var eistingCoa=$("li[id="+parentAccountCode+"] ul[id='mainChartOfAccount']").find("li[id="+data.accountCode+"]").attr('id');
                            if(typeof eistingCoa!='undefined'){
                                $("li[id="+parentAccountCode+"] ul[id='mainChartOfAccount']").find("li[id="+data.accountCode+"]").html("");
                                $("li[id="+parentAccountCode+"] ul[id='mainChartOfAccount']").find("li[id="+data.accountCode+"]").append('<div class="chartOfAccountContainer"><img id="'+data.accountCode+'" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this);"></img><font class="color-grey"><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details'+data.id+'" href="#itemSetUp" class="color-grey" onclick="showItemBankEntityDetails(this,'+toplevelaccountCode+')">'+data.name+'</a></span></b></font>');
                            }else if(typeof eistingCoa=='undefined'){
                                $("li[id="+parentAccountCode+"]").find("ul[id='mainChartOfAccount']").append('<li id='+data.accountCode+'><div class="chartOfAccountContainer"><img id="'+data.accountCode+'" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this);"></img><font class="color-grey"><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details'+data.id+'" href="#itemSetUp" class="color-grey" onclick="showItemBankEntityDetails(this,'+toplevelaccountCode+')">'+data.name+'</a></span></b></font></img>');
                            }
                        }
                        if(typeof existingul=='undefined'){
                            $("li[id="+parentAccountCode+"]").append('<ul id="mainChartOfAccount" class="treeview-black mainChartOfAccount"></ul>');
                            $("li[id="+parentAccountCode+"]").find("ul[id='mainChartOfAccount']").append('<li id='+data.accountCode+'><div class="chartOfAccountContainer"><img id="'+data.accountCode+'" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this);"></img><font class="color-grey"><b style="margin-left:2px;"><span class="coaNameDisplay" ><a id="show-entity-details'+data.id+'" href="#itemSetUp" class="color-grey" onclick="showItemBankEntityDetails(this,'+toplevelaccountCode+')">'+data.name+'</a></span></b></font></img></li>');
                        }
                        $("img[id="+parentAccountCode+"]").attr("src","assets/images/minus.png");
                        $("img[id="+parentAccountCode+"]").attr("onclick","javascript:removeCOA(this);");
                        $(".multiBranch option:selected").each(function () {
                            $(this).removeAttr('selected');
                        });
                        $('.multiBranch').multiselect('rebuild');
                        $("#items").val("");
                        $("#itemaccountcode").val("");
                        $("#itemCategory").val($("#itemCategory option:first").val());
                        $("#notificationMessage").html("Items has been added/Updated successfully.");
                        $('.notify-success').show();
                        $("#newItemform-container-close").trigger('click');
                        $(".VendRcmApplicableDate").datepicker({
                                changeMonth : true,
                                changeYear : true,
                                dateFormat:  'MM d,yy',
                                yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
                                onSelect: function(x,y){
                                    $(this).focus();
                                }
                        });
                        $.unblockUI();
                        getVendorData();
                        getAllChartOfAccount();
                        $('html, body').animate({
                            scrollTop:  $("li[id="+parentAccountCode+"]").find("ul[id='mainChartOfAccount'] li[id="+data.accountCode+"]").offset().top-50
                        },20);
                    }
                },
                error: function (xhr, status, error) {
                    if (xhr.status == 401) {
                        doLogout();
                    } else if (xhr.status == 500) {
                        swal("Error on Saving/update COA!", "Please retry, if problem persists contact support team", "error");
                    }
                },
                complete: function (data) {
                    $.unblockUI();
                    enableDisableCoaBranchButton(true);
                    resetCOATdsScreen();
                }
            });
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        },
        complete: function (data) {
            //$.unblockUI();
            enableDisableCoaBranchButton(true);
        }
    });
}*/

$(document).ready(function() {
    $("#addSpecfBtn").click(function() {
        $("#addSpecfBtn").attr("disabled", "disabled");
        addSpecifics();
    });

    /*$("#addSpecfBrnchBtn").click(function() {
        $("#addSpecfBrnchBtn").attr("disabled", "disabled");
        addBranchSpecifics();
    });*/
});

$(document).ready(function () {
    $(".coaFreeTextSearchButton").click(function () {
        var freeTextSearchCoa = $("#existingCOA").val();
        if (freeTextSearchCoa != "") {
            var jsonData = {};
            jsonData.freeTextSearchCoaVal = freeTextSearchCoa;
            jsonData.usermail = $("#hiddenuseremail").text();
            var url = "/specifics/searchCoa"
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
                    $(".searchedContent").html("");
                    for (var i = 0; i < data.coaParaListData.length; i++) {
                        $(".searchedContent").append('<p><b>' + data.coaParaListData[i].para + '</b></p>');
                    }
                    $(".searched-coadata-container").show();
                },
                error: function (xhr, status, error) {
                    if (xhr.status == 401) {
                        doLogout();
                    }
                }
            });
        }
    });
});

function avoidAddingChild() {
    var parentaccountCode = $("#topLevelParentActCode").val();
    var selectedValue = $('select[name="datavalidationall"] option:selected').val();
    var selectedText = $('select[name="datavalidationall"] option:selected').text();

    var url = '/specifics/isSpecificExists/' + selectedValue + "/" + parentaccountCode;
    $.ajax({
        url: url,
        type: "text",
        method: "GET",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        contentType: 'application/json',
        success: function (data) {
            if (data.isSpecificExists == "0") {
                if (parseFloat(selectedValue) > 23 && parseFloat(selectedValue) < 28) {
                    if (parentaccountCode.startsWith("2")) {
                        $("#isItemEmpClaimItem").val(0);
                        ("#isItemEmpClaimItem").prop('disabled', true);
                    }
                }
                return true;
            } else if (data.isSpecificExists == selectedValue) {
                swal("Error!","'" + selectedText + "' is not allowed to add from Chart of account, add from specific page.","error");
                $("#datavalidationall").find("option[value='']").prop("selected", "selected");
                ("#isItemEmpClaimItem").prop('disabled', false);
                return false;
            }
        },
        error: function (xhr, status, error) {
            $("#datavalidationall").find("option[value='']").prop("selected", "selected");
            if (xhr.status == 401) {
                doLogout();
            }
        }
    });
}

function showChartOFAccount() {
    var jsonData = {};
    jsonData.usermail = $("#hiddenuseremail").text();
    var url = "/config/showChartOFAccount";
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        method: "GET",
        contentType: 'application/json',
        success: function (data) {
            $(".chartofaccountrefresh").remove();
            for (var i = 0; i < data.partData.length; i++) {
                var coaId = data.partData[i].id;
                var coaName = data.partData[i].name;
                $("#mainChartOfAccount").append('<li id=' + data.partData[i].accountCode + ' class="chartofaccountrefresh"><div class="chartOfAccountContainer"><p class="color-grey"><img id="' + data.partData[i].accountCode + '" src="/assets/images/new.v1370889834.png" class="coaPlusMinusImage" id="getNodeChild" onclick="javascript:getChildChartOfAccount(this, 0 );"></img><b style="margin-left:2px;"><a id="show-entity-details' + data.partData[i].id + '" class="color-grey" href="#itemSetUp" onClick="">' + data.partData[i].name + '</a></b><button style="float:right" id="newItemform-container" name="' + coaName + '" class="newEntityCreateButton btn btn-tree btn-idos" title="Create new Sub Account" onclick="javascript:createCOA(this,' + coaId + ',' + data.partData[i].accountCode + ');"><i class="fa fa-plus pr-3"></i>Add Sub Account</button></p></li>');
            }

            $("#invoiceSerialNo").val(data.invoiceSerialNo);
            $("#proformaSerialNo").val(data.proformaSerialNo);
            $("#quotationSerialNo").val(data.quotationSerialNo);
            $("#receiptSerialNo").val(data.receiptSerialNo);
            $("#advanceReceiptSerialNo").val(data.advanceReceiptSerialNo);
            $("#debitNoteCustSerialNo").val(data.debitNoteCustSerialNo);
            $("#creditNoteCustSerialNo").val(data.creditNoteCustSerialNo);
            $("#purchaseOrderSerialNo").val(data.purchaseOrderSerialNo);
            $("#refundAdvReceiptSerialNo").val(data.refundAdvReceiptSerialNo);
            $("#refundAmtAgainstInvoiceReceiptSerialNo").val(data.refundAmtAgainstInvoiceReceiptSerialNo);
            $("#deliveryChallanReceiptSerialNo").val(data.deliveryChallanReceiptSerialNo);
            $("#paymentVoucherSerialNo").val(data.paymentVoucherSerialNo);
            $("#selfInvoiceSerialNo").val(data.selfInvoiceSerialNo);
            $("#createpurchaseOrderSerialNo").val(data.createpurchaseOrderSerialNo);

            $("#invoiceInterval").find("option[value='" + data.invoiceInterval + "']").prop("selected", "selected");
            $("#proformaInterval").find("option[value='" + data.proformaInterval + "']").prop("selected", "selected");
            $("#quotationInterval").find("option[value='" + data.quotationInterval + "']").prop("selected", "selected");
            $("#receiptInterval").find("option[value='" + data.receiptInterval + "']").prop("selected", "selected");
            $("#advanceReceiptInterval").find("option[value='" + data.advanceReceiptInterval + "']").prop("selected", "selected");
            $("#debitNoteCustInterval").find("option[value='" + data.debitNoteCustInterval + "']").prop("selected", "selected");
            $("#creditNoteCustInterval").find("option[value='" + data.creditNoteCustInterval + "']").prop("selected", "selected");
            $("#purchaseOrderInterval").find("option[value='" + data.purchaseOrderInterval + "']").prop("selected", "selected");
            $("#refundAdvReceiptInterval").find("option[value='" + data.refundAdvReceiptInterval + "']").prop("selected", "selected");
            $("#refundAmtAgainstInvoiceReceiptInterval").find("option[value='" + data.refundAmtAgainstInvoiceReceiptInterval + "']").prop("selected", "selected");
            $("#deliveryChallanReceiptInterval").find("option[value='" + data.deliveryChallanReceiptInterval + "']").prop("selected", "selected");
            $("#paymentVoucherInterval").find("option[value='" + data.paymentVoucherInterval + "']").prop("selected", "selected");
            $("#selfInvoiceInterval").find("option[value='" + data.selfInvoiceInterval + "']").prop("selected", "selected");
            $("#createpurchaseOrderInterval").find("option[value='" + data.createpurchaseOrderInterval + "']").prop("selected", "selected");

            $("#serialNoCategory").find("option[value='" + data.serialNoCategory + "']").prop("selected", "selected");
            $("#serialNoCategory").change();
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        }
    });
}

function downloadOrganizationCOA() {
    var jsonData = {};
    jsonData.useremail = $("#hiddenuseremail").text();
    var url = "/specifics/downloadOrgChartOfAccounts";
    downloadFile(url, "POST", jsonData, "Error on downloading COA!");
}

function downloadOrganizationCOATemplate(particularValue) {
    var jsonData = {};
    jsonData.useremail = $("#hiddenuseremail").text();
    var url = "/specifics/downloadOrgCOATemplate/" + particularValue;
    downloadFile(url, "POST", jsonData, "Error on downloading COA Template!");
}

function uploadChartOFAccount() {
    var selectedCoaUploadType = $("#coaUploadType option:selected").val();
    if (selectedCoaUploadType == "") {
        swal("Error!","Please select chart of accounts type","error");
        return false;
    }
    var chatofacturl = $("#uploadchartofact").val();
    if (chatofacturl == "") {
        swal("Error!","please upload your company chart of accounts","error");
        return true;
    }
    var ext = chatofacturl.substring(chatofacturl.lastIndexOf('.') + 1);
    var jsonData = {};
    if (ext != "xls" && ext != "xlsx") {
        swal("Error!","Only Excel files are allowed for chart of account upload","error");
        $("#uploadchartofact").val("");
        return true;
    } else {

        var useremail = $("#hiddenuseremail").text();
        jsonData.usermail = useremail;
    }
    var form = $('#myForm');
    var data = new FormData();
    jQuery.each($('#uploadchartofact')[0].files, function (i, file) {
        data.append('file-' + i, file);
    });

    //data.append('selectedCoaUploadType',selectedCoaUploadType);

    var url = "/config/uploadcoact/" + selectedCoaUploadType;

    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    $.ajax({
        method: "POST",
        url: url,
        data: data,
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        cache: false,
        contentType: false,
        processData: false,
        success: function (data) {
            $("#uploadchartofact").val("");
            swal("Success","Total records in xls: " + data.totalRowsInXls + ", Inserted in system: " + data.totalRowsInserted + ". " + data.uploadIssue,"success");
            $.unblockUI();
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        }
    });
}

function fillExpenseItemToMapIncomeToExpense() {
    //alert("fillExpenseItemToMapIncomeToExpense");
    var jsonData = {};
    jsonData.useremail = $("#hiddenuseremail").text();
    var url = "/chartOfAccounts/allChartOfAccounts";
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        type: "text",
        async: false,
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        method: "POST",
        contentType: 'application/json',
        success: function (data) {
            $("#incomeToExpId").children().remove();
            $("#incomeToExpId").append('<option value="">--Please Select--</option>');
            for (var i = 0; i < data.allCoaData.length; i++) {
                if (data.allCoaData[i].accountCode == "2000000000000000000") {
                    $("#incomeToExpId").append('<option value="' + data.allCoaData[i].id + '">' + data.allCoaData[i].name + '</option>');
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

/** also used in user setup **/
function getAllChartOfAccount() {
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var jsonData = {};
    jsonData.useremail = $("#hiddenuseremail").text();
    var url = "/chartOfAccounts/allChartOfAccounts";
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        type: "text",
        method: "POST",
        contentType: 'application/json',
        success: function (data) {
            logDebug("Start getAllChartOfAccount");
            $("#procurementItem").children().remove();
            $("#procurementItem").append('<option value="">--Please Select--</option>');
            var bnchOption = "";
            var auditorSelectOptions = "";
            var coaItemsList = "";
            var transCoaItemList = "";
            var procurementItemList = "";
            for (var i = 0; i < data.allCoaData.length; i++) {
                if (data.allCoaData[i].accountCode == "2000000000000000000" || data.allCoaData[i].accountCode == "3000000000000000000") {
                    procurementItemList += '<option value="' + data.allCoaData[i].id + '">' + data.allCoaData[i].name + '</option>';
                }
            }
            $("#procurementItem").append(procurementItemList);
            logDebug("End getAllChartOfAccount");
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching COA items for User setup!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });
}

function getAllChartOfAccountsLRUCache() {
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var jsonData = {};
    jsonData.useremail = $("#hiddenuseremail").text();
    var url = "/chartOfAccounts/allChartOfAccountsLRUCache";
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        type: "text",
        method: "POST",
        contentType: 'application/json',
        success: function (data) {
            logDebug("Start getAllChartOfAccount");
            var bnchOption = "";
            var auditorSelectOptions = [];
            var coaItemsList = [];
            var transCoaItemList = "";
            var procurementItemList = "";
            var itr = 0;
            var opItr = 0;
            $("#newuserTxnCreationExcelFormTable tr[id='userTransactionRow'] ul[id='txnInCoaCreatorList']").html("");
            $("#newuserTxnCreationExcelFormTable tr[id='userTransactionRow'] ul[id='txnInCoaCreatorList']").append('<li id="txnIncoalist">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA" value="" onClick="transactioncheckUncheck(this)"/>&nbsp;&nbsp;<input type="text" class="input-small" name="limitallfrom" id="limitallfrom" onkeyup="fillAllChartOfAccountLimit(this);"/>&nbsp;&nbsp;To<input type="text" class="input-small" name="limitallto" id="limitallto" onkeyup="fillAllChartOfAccountLimit(this);"/>&nbsp;&nbsp;Select All</li>');

            $("#newuserTxnApproverExcelFormTable tr[id='userTransactionRow'] ul[id='txnInCoaApproverList']").html("");
            $("#newuserTxnApproverExcelFormTable tr[id='userTransactionRow'] ul[id='txnInCoaApproverList']").append('<li id="txnIncoalist">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA" value="" onClick="transactioncheckUncheck(this)"/>&nbsp;&nbsp;<input type="text" class="input-small" name="limitallfrom" id="limitallfrom" onkeyup="fillAllChartOfAccountLimit(this);"/>&nbsp;&nbsp;To<input type="text" class="input-small" name="limitallto" id="limitallto" onkeyup="fillAllChartOfAccountLimit(this);"/>&nbsp;&nbsp;Select All</li>');

            for (var i = 0; i < data.inCoaData.length; i++) {

                coaItemsList[itr++] = '<li id="txnIncoalist">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA" value=';
                coaItemsList[itr++] = data.inCoaData[i].id;
                coaItemsList[itr++] = ' onClick="transactioncheckUncheck(this)"/>&nbsp;&nbsp;<input type="text" class="input-small" name="coaAmountLimit" value="0.0" id="coaAmountLimit';
                coaItemsList[itr++] = data.inCoaData[i].id;
                coaItemsList[itr++] = '" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);" onkeyup="transactiontoggleCheck(this)"/>&nbsp;&nbsp;To <input type="text" class="input-small" name="coaAmountLimitTo" value="0.0" id="coaAmountLimitTo';
                coaItemsList[itr++] = data.inCoaData[i].id;
                coaItemsList[itr++] = '" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);" onkeyup="transactiontoggleCheck(this)"/>&nbsp;&nbsp;&nbsp;<span class="itemLabelClass">';
                coaItemsList[itr++] = data.inCoaData[i].name;
                coaItemsList[itr++] = '</span></li>';
                //transCoaItemList += '<li id="txnIncoalist">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA" value='+data.inCoaData[i].id+' onClick="transactioncheckUncheck(this)"/>&nbsp;&nbsp;<span class="itemLabelClass">'+data.inCoaData[i].name+'</span></li>'
                auditorSelectOptions[opItr++] = '<option value="';
                auditorSelectOptions[opItr++] = data.inCoaData[i].id;
                auditorSelectOptions[opItr++] = '">';
                auditorSelectOptions[opItr++] = data.inCoaData[i].name;
                auditorSelectOptions[opItr++] = '</option>';
            }
            $("#newuserTxnCreationExcelFormTable tr[id='userTransactionRow'] ul[id='txnInCoaCreatorList']").append(coaItemsList.join(''));
            $("#newuserTxnApproverExcelFormTable tr[id='userTransactionRow'] ul[id='txnInCoaApproverList']").append(coaItemsList.join(''));

            $("#newuserTxnAuditorExcelFormTable #transactionInCoaAuditorList").children().remove();
            $("#newuserTxnAuditorExcelFormTable #transactionInCoaAuditorList").append(auditorSelectOptions.join(''));
            $("#newuserTxnAuditorExcelFormTable #transactionInCoaAuditorList").multiselect('rebuild');
            auditorSelectOptions = [];
            coaItemsList = [];
            itr = 0;
            opItr = 0;
            $("#newuserTxnCreationExcelFormTable tr[id='userTransactionRow'] ul[id='txnExCoaCreatorList']").html("");
            $("#newuserTxnCreationExcelFormTable tr[id='userTransactionRow'] ul[id='txnExCoaCreatorList']").append('<li id="txnExcoalist">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA" value="" onClick="transactioncheckUncheck(this)"/>&nbsp;&nbsp;<input type="text" class="input-small" name="limitallfrom" id="limitallfrom" onkeyup="fillAllChartOfAccountLimit(this);"/>&nbsp;&nbsp;To<input type="text" class="input-small" name="limitallto" id="limitallto" onkeyup="fillAllChartOfAccountLimit(this);"/>&nbsp;&nbsp;Select All</li>');
            $("#newuserTxnApproverExcelFormTable tr[id='userTransactionRow'] ul[id='txnExCoaApproverList']").html("");
            $("#newuserTxnApproverExcelFormTable tr[id='userTransactionRow'] ul[id='txnExCoaApproverList']").append('<li id="txnIncoalist">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA" value="" onClick="transactioncheckUncheck(this)"/>&nbsp;&nbsp;<input type="text" class="input-small" name="limitallfrom" id="limitallfrom" onkeyup="fillAllChartOfAccountLimit(this);"/>&nbsp;&nbsp;To<input type="text" class="input-small" name="limitallto" id="limitallto" onkeyup="fillAllChartOfAccountLimit(this);"/>&nbsp;&nbsp;Select All</li>');
            for (var i = 0; i < data.exCoaData.length; i++) {
                coaItemsList[itr++] = '<li id="txnExcoalist">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA" value=';
                coaItemsList[itr++] = data.exCoaData[i].id;
                coaItemsList[itr++] = ' onClick="transactioncheckUncheck(this)"/>&nbsp;&nbsp;<input type="text" class="input-small" name="coaAmountLimit" value="0.0" id="coaAmountLimit';
                coaItemsList[itr++] = data.exCoaData[i].id;
                coaItemsList[itr++] = '" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);" onkeyup="transactiontoggleCheck(this)"/>&nbsp;&nbsp;To <input type="text" class="input-small" name="coaAmountLimitTo" value="0.0" id="coaAmountLimitTo';
                coaItemsList[itr++] = data.exCoaData[i].id;
                coaItemsList[itr++] = '" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);" onkeyup="transactiontoggleCheck(this)"/>&nbsp;&nbsp;&nbsp;<span class="itemLabelClass">';
                coaItemsList[itr++] = data.exCoaData[i].name;
                coaItemsList[itr++] = '</span></li>';
                //transCoaItemList += '<li id="txnExcoalist">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA" value='+data.exCoaData[i].id+' onClick="transactioncheckUncheck(this)"/>&nbsp;&nbsp;<span class="itemLabelClass">'+data.exCoaData[i].name+'</span></li>'
                auditorSelectOptions[opItr++] = '<option value="';
                auditorSelectOptions[opItr++] = data.exCoaData[i].id;
                auditorSelectOptions[opItr++] = '">';
                auditorSelectOptions[opItr++] = data.exCoaData[i].name;
                auditorSelectOptions[opItr++] = '</option>';
            }
            $("#newuserTxnCreationExcelFormTable tr[id='userTransactionRow'] ul[id='txnExCoaCreatorList']").append(coaItemsList.join(''));
            $("#newuserTxnApproverExcelFormTable tr[id='userTransactionRow'] ul[id='txnExCoaApproverList']").append(coaItemsList.join(''));
            $("#newuserTxnAuditorExcelFormTable #transactionExCoaAuditorList").children().remove();
            $("#newuserTxnAuditorExcelFormTable #transactionExCoaAuditorList").append(auditorSelectOptions.join(''));
            $("#newuserTxnAuditorExcelFormTable #transactionExCoaAuditorList").multiselect('rebuild');
            auditorSelectOptions = [];
            coaItemsList = [];
            itr = 0;
            opItr = 0;
            $("#newuserTxnCreationExcelFormTable tr[id='userTransactionRow'] ul[id='txnAsCoaCreatorList']").html("");
            $("#newuserTxnCreationExcelFormTable tr[id='userTransactionRow'] ul[id='txnAsCoaCreatorList']").append('<li id="txnAscoalist">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA" value="" onClick="transactioncheckUncheck(this)"/>&nbsp;&nbsp;<input type="text" class="input-small" name="limitallfrom" id="limitallfrom" onkeyup="fillAllChartOfAccountLimit(this);"/>&nbsp;&nbsp;To<input type="text" class="input-small" name="limitallto" id="limitallto" onkeyup="fillAllChartOfAccountLimit(this);"/>&nbsp;&nbsp;Select All</li>');
            $("#newuserTxnApproverExcelFormTable tr[id='userTransactionRow'] ul[id='txnAsCoaApproverList']").html("");
            $("#newuserTxnApproverExcelFormTable tr[id='userTransactionRow'] ul[id='txnAsCoaApproverList']").append('<li id="txnIncoalist">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA" value="" onClick="transactioncheckUncheck(this)"/>&nbsp;&nbsp;<input type="text" class="input-small" name="limitallfrom" id="limitallfrom" onkeyup="fillAllChartOfAccountLimit(this);"/>&nbsp;&nbsp;To<input type="text" class="input-small" name="limitallto" id="limitallto" onkeyup="fillAllChartOfAccountLimit(this);"/>&nbsp;&nbsp;Select All</li>');
            for (var i = 0; i < data.asCoaData.length; i++) {
                coaItemsList[itr++] = '<li id="txnAscoalist">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA" value=';
                coaItemsList[itr++] = data.asCoaData[i].id;
                coaItemsList[itr++] = ' onClick="transactioncheckUncheck(this)"/>&nbsp;&nbsp;<input type="text" class="input-small" name="coaAmountLimit" value="0.0" id="coaAmountLimit';
                coaItemsList[itr++] = data.asCoaData[i].id;
                coaItemsList[itr++] = '" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);" onkeyup="transactiontoggleCheck(this)"/>&nbsp;&nbsp;To <input type="text" class="input-small" name="coaAmountLimitTo" value="0.0" id="coaAmountLimitTo';
                coaItemsList[itr++] = data.asCoaData[i].id;
                coaItemsList[itr++] = '" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);" onkeyup="transactiontoggleCheck(this)"/>&nbsp;&nbsp;&nbsp;<span class="itemLabelClass">';
                coaItemsList[itr++] = data.asCoaData[i].name;
                coaItemsList[itr++] = '</span></li>';
                //transCoaItemList += '<li id="txnAscoalist">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA" value='+data.asCoaData[i].id+' onClick="transactioncheckUncheck(this)"/>&nbsp;&nbsp;<span class="itemLabelClass">'+data.asCoaData[i].name+'</span></li>'
                auditorSelectOptions[opItr++] = '<option value="';
                auditorSelectOptions[opItr++] = data.asCoaData[i].id;
                auditorSelectOptions[opItr++] = '">';
                auditorSelectOptions[opItr++] = data.asCoaData[i].name;
                auditorSelectOptions[opItr++] = '</option>';
            }
            $("#newuserTxnCreationExcelFormTable tr[id='userTransactionRow'] ul[id='txnAsCoaCreatorList']").append(coaItemsList.join(''));
            $("#newuserTxnApproverExcelFormTable tr[id='userTransactionRow'] ul[id='txnAsCoaApproverList']").append(coaItemsList.join(''));
            $("#newuserTxnAuditorExcelFormTable #transactionAsCoaAuditorList").children().remove();
            $("#newuserTxnAuditorExcelFormTable #transactionAsCoaAuditorList").append(auditorSelectOptions.join(''));
            $("#newuserTxnAuditorExcelFormTable #transactionAsCoaAuditorList").multiselect('rebuild');
            auditorSelectOptions = [];
            coaItemsList = [];
            itr = 0;
            opItr = 0;
            $("#newuserTxnCreationExcelFormTable tr[id='userTransactionRow'] ul[id='txnLiCoaCreatorList']").html("");
            $("#newuserTxnCreationExcelFormTable tr[id='userTransactionRow'] ul[id='txnLiCoaCreatorList']").append('<li id="txnLicoalist">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA" value="" onClick="transactioncheckUncheck(this)"/>&nbsp;&nbsp;<input type="text" class="input-small" name="limitallfrom" id="limitallfrom" onkeyup="fillAllChartOfAccountLimit(this);"/>&nbsp;&nbsp;To<input type="text" class="input-small" name="limitallto" id="limitallto" onkeyup="fillAllChartOfAccountLimit(this);"/>&nbsp;&nbsp;Select All</li>');
            $("#newuserTxnApproverExcelFormTable tr[id='userTransactionRow'] ul[id='txnLiCoaApproverList']").html("");
            $("#newuserTxnApproverExcelFormTable tr[id='userTransactionRow'] ul[id='txnLiCoaApproverList']").append('<li id="txnIncoalist">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA" value="" onClick="transactioncheckUncheck(this)"/>&nbsp;&nbsp;<input type="text" class="input-small" name="limitallfrom" id="limitallfrom" onkeyup="fillAllChartOfAccountLimit(this);"/>&nbsp;&nbsp;To<input type="text" class="input-small" name="limitallto" id="limitallto" onkeyup="fillAllChartOfAccountLimit(this);"/>&nbsp;&nbsp;Select All</li>');
            for (var i = 0; i < data.liCoaData.length; i++) {
                coaItemsList[itr++] = '<li id="txnLicoalist">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA" value=';
                coaItemsList[itr++] = data.liCoaData[i].id;
                coaItemsList[itr++] = ' onClick="transactioncheckUncheck(this)"/>&nbsp;&nbsp;<input type="text" class="input-small" name="coaAmountLimit" value="0.0" id="coaAmountLimit';
                coaItemsList[itr++] = data.liCoaData[i].id;
                coaItemsList[itr++] = '" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);" onkeyup="transactiontoggleCheck(this)"/>&nbsp;&nbsp;To <input type="text" class="input-small" name="coaAmountLimitTo" value="0.0" id="coaAmountLimitTo';
                coaItemsList[itr++] = data.liCoaData[i].id;
                coaItemsList[itr++] = '" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event);" onkeyup="transactiontoggleCheck(this)"/>&nbsp;&nbsp;&nbsp;<span class="itemLabelClass">';
                coaItemsList[itr++] = data.liCoaData[i].name;
                coaItemsList[itr++] = '</span></li>';
                //transCoaItemList += '<li id="txnLicoalist">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA" value='+data.liCoaData[i].id+' onClick="transactioncheckUncheck(this)"/>&nbsp;&nbsp;<span class="itemLabelClass">'+data.liCoaData[i].name+'</span></li>'
                auditorSelectOptions[opItr++] = '<option value="';
                auditorSelectOptions[opItr++] = data.liCoaData[i].id;
                auditorSelectOptions[opItr++] = '">';
                auditorSelectOptions[opItr++] = data.liCoaData[i].name;
                auditorSelectOptions[opItr++] = '</option>';
            }
            $("#newuserTxnCreationExcelFormTable tr[id='userTransactionRow'] ul[id='txnLiCoaCreatorList']").append(coaItemsList.join(''));
            $("#newuserTxnApproverExcelFormTable tr[id='userTransactionRow'] ul[id='txnLiCoaApproverList']").append(coaItemsList.join(''));
            $("#newuserTxnAuditorExcelFormTable #transactionLiCoaAuditorList").children().remove();
            $("#newuserTxnAuditorExcelFormTable #transactionLiCoaAuditorList").append(auditorSelectOptions.join(''));
            $("#newuserTxnAuditorExcelFormTable #transactionLiCoaAuditorList").multiselect('rebuild');
            //$("#newuserTxnAuditorExcelFormTable tr[id='userTransactionRow'] ul[class='transactionCoaList']").append(transCoaItemList);

            logDebug("End getAllChartOfAccount");
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching COA items for User setup!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });
}

$(document).ready(function () {
    $("#addupdateserials").click(function () {
        var invoiceSerialNo = $('#invoiceSerialNo').val();
        var proformaSerialNo = $('#proformaSerialNo').val();
        var quotationSerialNo = $('#quotationSerialNo').val();
        var receiptSerialNo = $('#receiptSerialNo').val();
        var advanceReceiptSerialNo = $('#advanceReceiptSerialNo').val();
        var debitNoteCustSerialNo = $('#debitNoteCustSerialNo').val();
        var creditNoteCustSerialNo = $('#creditNoteCustSerialNo').val();
        var purchaseOrderSerialNo = $('#purchaseOrderSerialNo').val();
        var refundAdvReceiptSerialNo = $('#refundAdvReceiptSerialNo').val();
        var refundAmtAgainstInvoiceReceiptSerialNo = $('#refundAmtAgainstInvoiceReceiptSerialNo').val();
        var deliveryChallanReceiptSerialNo = $('#deliveryChallanReceiptSerialNo').val();
        var paymentVoucherSerialNo = $('#paymentVoucherSerialNo').val();
        var selfInvoiceSerialNo = $('#selfInvoiceSerialNo').val();
        var createpurchaseOrderSerialNo = $('#createpurchaseOrderSerialNo').val();

        var invoiceInterval = $('#invoiceInterval option:selected').val();
        var quotationInterval = $('#quotationInterval option:selected').val();
        var proformaInterval = $('#proformaInterval option:selected').val();
        var receiptInterval = $('#receiptInterval option:selected').val();
        var advanceReceiptInterval = $('#advanceReceiptInterval option:selected').val();
        var debitNoteCustInterval = $('#debitNoteCustInterval option:selected').val();
        var creditNoteCustInterval = $('#creditNoteCustInterval option:selected').val();
        var purchaseOrderInterval = $('#purchaseOrderInterval option:selected').val();
        var refundAdvReceiptInterval = $('#refundAdvReceiptInterval option:selected').val();
        var refundAmtAgainstInvoiceReceiptInterval = $('#refundAmtAgainstInvoiceReceiptInterval option:selected').val();
        var deliveryChallanReceiptInterval = $('#deliveryChallanReceiptInterval').val();
        var paymentVoucherInterval = $('#paymentVoucherInterval').val();
        var selfInvoiceInterval = $('#selfInvoiceInterval').val();
        var createpurchaseOrderInterval = $('#createpurchaseOrderInterval').val();

        var jsonData = {};
        jsonData.usermail = $("#hiddenuseremail").text();
        jsonData.invoiceSerialNo = invoiceSerialNo;
        jsonData.proformaSerialNo = proformaSerialNo;
        jsonData.quotationSerialNo = quotationSerialNo;
        jsonData.receiptSerialNo = receiptSerialNo;
        jsonData.advanceReceiptSerialNo = advanceReceiptSerialNo;
        jsonData.debitNoteCustSerialNo = debitNoteCustSerialNo;
        jsonData.creditNoteCustSerialNo = creditNoteCustSerialNo;
        jsonData.purchaseOrderSerialNo = purchaseOrderSerialNo;
        jsonData.refundAdvReceiptSerialNo = refundAdvReceiptSerialNo;
        jsonData.refundAmtAgainstInvoiceReceiptSerialNo = refundAmtAgainstInvoiceReceiptSerialNo;
        jsonData.deliveryChallanReceiptSerialNo = deliveryChallanReceiptSerialNo;
        jsonData.paymentVoucherSerialNo = paymentVoucherSerialNo;
        jsonData.selfInvoiceSerialNo = selfInvoiceSerialNo;
        jsonData.createpurchaseOrderSerialNo = createpurchaseOrderSerialNo;

        jsonData.invoiceInterval = invoiceInterval;
        jsonData.proformaInterval = proformaInterval;
        jsonData.quotationInterval = quotationInterval;
        jsonData.receiptInterval = receiptInterval;
        jsonData.advanceReceiptInterval = advanceReceiptInterval;
        jsonData.debitNoteCustInterval = debitNoteCustInterval;
        jsonData.creditNoteCustInterval = creditNoteCustInterval;
        jsonData.purchaseOrderInterval = purchaseOrderInterval;
        jsonData.refundAdvReceiptInterval = refundAdvReceiptInterval;
        jsonData.refundAmtAgainstInvoiceReceiptInterval = refundAmtAgainstInvoiceReceiptInterval;
        jsonData.deliveryChallanReceiptInterval = deliveryChallanReceiptInterval;
        jsonData.paymentVoucherInterval = paymentVoucherInterval;
        jsonData.selfInvoieInterval = selfInvoiceInterval;
        jsonData.createpurchaseOrderInterval = createpurchaseOrderInterval;
        jsonData.serialNoCategory = $('#serialNoCategory option:selected').val();
        var url = "/orgnization/saveserial";
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
                swal("Serials saved!", "Given entities serial numbers saved.", "success");
            },
            error: function (xhr, status, error) {
                if (xhr.status == 401) {
                    doLogout();
                }
            }
        });
    });
});

$(document).ready(function () {
    $('body').on('click', '.itemBranch2Class-cb', function () {
        var num = $('#itemBranch2BtnList').find('.itemBranch2Class-cb:checked').length;
        if (num != 0) {
            $('#itemBranch2Btn').html(num + ' Selected&nbsp;<b class="caret" style="margin:5px auto;"></b>');
        } else {
            $('#itemBranch2Btn').html('None Selected&nbsp;<b class="caret" style="margin:5px auto;"></b>');
        }
    });

    $('body').on('keyup', '.itemBranch2-input', function () {
        var leng = $(this).val().length;
        (leng == 0) ? $(this).parent().find('.itemBranch2Class-cb').prop('checked', false) : $(this).parent().find('.itemBranch2Class-cb').prop('checked', true);
    });
});

function checkUncheckIncomeCOA(elem, coaType) {
    var checked = $(elem).is(':checked');
    var checkvalue = $(elem).val();
    $("#itemBranch2Btn").append("&nbsp;&nbsp;<b class='caret'></b>");
    if (checked == true) {
        if (checkvalue == "") {
            $('#item-form-container input[class="itemBranch2Class-cb"]').each(function () {
                $(this).prop("checked", true);
            });
        }
    } else if (checked == false) {
        if (checkvalue == "") {
            if (confirm("Do u want to remove all your selected branches!")) {
                $('#item-form-container input[class="itemBranch2Class-cb"]').each(function () {
                    $(this).prop("checked", false);
                });
            }
        }
        $("#item-form-container input[id='openingBalance']").val('');
        $("#item-form-container input[id='expNoOfOpeningBalUnits']").val('0');
        $("#item-form-container input[id='expOpeningBal']").val('0');
    }
    var check_box_values = $('#item-form-container input[class="itemBranch2Class-cb"]:checkbox:checked').map(function () {
        return this.value;
    }).get();
    var length = check_box_values.length;
    if (length > 0) {
        var text = length + " " + "Items Selected";
        $("#itemBranch2Btn").text(text);
        $("#itemBranch2Btn").append("&nbsp;&nbsp;<b class='caret'></b>");
    }
    if (check_box_values == 0) {
        $("#itemBranch2Btn").text("None Selected");
        $("#itemBranch2Btn").append("&nbsp;&nbsp;<b class='caret'></b>");
    }

    $('#item-form-container input[class="openingBalance"]').each(function () {
        var tmpValue = $(this).val();
        if (tmpValue == null || tmpValue == "") {
            $(this).val(0.0);
        } else if (checked == false) {
            $(this).val('');
        }
    });
    if (coaType === 1) {
        $('#item-form-container input[name="incomeItemDiscount"]').each(function () {
            var tmpValue = $(this).val();
            if (tmpValue == null || tmpValue == "") {
                $(this).val(0.0);
            } else if (checked == false) {
                $(this).val('');
            }
        });
    } else if (coaType === 2) {
        $('#item-form-container input[id="noOfUnits"]').each(function () {
            var tmpValue = $(this).val();
            if (tmpValue == null || tmpValue == "") {
                $(this).val(0.0);
            } else if (checked == false) {
                $(this).val('');
            }
        });

        $('#item-form-container input[id="inventoryRate"]').each(function () {
            var tmpValue = $(this).val();
            if (tmpValue == null || tmpValue == "") {
                $(this).val(0.0);
            } else if (checked == false) {
                $(this).val('');
            }
        });

        $('#item-form-container input[id="inventoryValue"]').each(function () {
            var tmpValue = $(this).val();
            if (tmpValue == null || tmpValue == "") {
                $(this).val(0.0);
            } else if (checked == false) {
                $(this).val('');
            }
        });
    }
}

var validateCoaBranchData = function (elem, branchid, coaType) {
    var valueTmp = $("li[id='" + branchid + "'] input[class='openingBalance']").val();
    if (valueTmp != "") {
        $("li[id='" + branchid + "'] input[value='" + branchid + "']:checkbox").prop("checked", true);
    }
    var amount = 0;
    $("#item-form-container input[class='openingBalance']").map(function () {
        if ($(this).val().trim() != "") {
            amount += parseFloat(this.value);
        }
    }).get();
    if (parseFloat(amount) > 0 || parseFloat(amount) < 0) {
        $("#item-form-container input[id='openingBalance']").val(parseFloat(amount).toFixed(2));
    } else {
        $("#item-form-container input[id='openingBalance']").val('');
    }

    if (coaType === 1) {
        var valueTmp2 = $("li[id='" + branchid + "'] input[name='incomeItemDiscount']").val();
        if (valueTmp == "" && valueTmp2 == "") {
            $("li[id='" + branchid + "'] input[value='" + branchid + "']:checkbox").prop("checked", false);
        }
    } else if (coaType === 2) {
        var valueTmp2 = $("li[id='" + branchid + "'] input[id='noOfUnits']").val();
        var valueTmp3 = $("li[id='" + branchid + "'] input[id='inventoryRate']").val();
        var valueTmp4 = $("li[id='" + branchid + "'] input[id='inventoryValue']").val();
        if (typeof valueTmp2 != 'undefined' && typeof valueTmp3 != 'undefined' && valueTmp2 != "" && valueTmp3 != "") {
            var amount = parseFloat(valueTmp2) * parseFloat(valueTmp3);
            $("li[id='" + branchid + "'] input[id='inventoryValue']").val(parseFloat(amount).toFixed(2));
        } else {
            $("li[id='" + branchid + "'] input[id='inventoryValue']").val('');
        }
        if (valueTmp == "" && valueTmp2 == "" && valueTmp3 == "") {
            $("li[id='" + branchid + "'] input[value='" + branchid + "']:checkbox").prop("checked", false);
        }
        amount = 0;
        $("#item-form-container input[id='noOfUnits']").map(function () {
            if ($(this).val().trim() != "") {
                amount += parseFloat(this.value);
            }
        }).get();
        if (parseFloat(amount) > 0) {
            $("#item-form-container input[id='expNoOfOpeningBalUnits']").val(parseFloat(amount).toFixed(2));
        } else {
            $("#item-form-container input[id='expNoOfOpeningBalUnits']").val('');
        }

        amount = 0;
        $("#item-form-container input[id='inventoryValue']").map(function () {
            if ($(this).val().trim() != "") {
                amount += parseFloat(this.value);
            }
        }).get();
        if (parseFloat(amount) > 0) {
            $("#item-form-container input[id='expOpeningBal']").val(parseFloat(amount).toFixed(2));
        } else {
            $("#item-form-container input[id='expOpeningBal']").val('');
        }
    } else if (coaType === 34) {
        if (valueTmp == "") {
            $("li[id='" + branchid + "'] input[value='" + branchid + "']:checkbox").prop("checked", false);
        }
    }
    var btn = $("#item-form-container button[id='itemBranch2Btn']");
    var chkBox = $("li[id='" + branchid + "'] input[value='" + branchid + "']:checkbox");
    checkUncheckBranches(chkBox, btn);
}
var populateSerials = function (elem) {
    var selected = $(elem).val();
    if (selected == "2") {
        PopulateDataForGSTSerials();
        $("#gstSerialNumberDiv").css('display', 'block');
        $("#serialNumberDiv").css('display', 'none');
    } else {
        $("#gstSerialNumberDiv").css('display', 'none');
        $("#serialNumberDiv").css('display', 'block');
    }
}


var PopulateDataForGSTSerials = function () {
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var jsonData = {};
    jsonData.useremail = $("#hiddenuseremail").text();
    var url = "/orgnization/orgGstSerials";
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        type: "text",
        method: "POST",
        contentType: 'application/json',
        success: function (data) {
            $("#gstInAllInterval").find("option[value='" + data.gstInInterval + "']").prop("selected", "selected");
            for (var a = 0; a < 13; a++) {

                var dataList = [];
                var compId = "";
                var SerialsData = [];
                var i = 0;
                if (a == 0) {
                    dataList = data.salesInvoiceGstSerials;
                    compId = "gstSalesInvoiceSerialList";
                } else if (a == 1) {
                    dataList = data.proformaGstSerials;
                    compId = "gstProformaSerialList";
                } else if (a == 2) {
                    dataList = data.quotationGstSerials;
                    compId = "gstQuotationSerialList";
                } else if (a == 3) {
                    dataList = data.receiptGstSerials;
                    compId = "gstReceiptSerialList";
                } else if (a == 4) {
                    dataList = data.advanceReceiptGstSerials;
                    compId = "gstAdvanceReceiptSerialList";
                } else if (a == 5) {
                    dataList = data.debitNoteToCustGstSerials;
                    compId = "gstDebitNoteSerialList";
                } else if (a == 6) {
                    dataList = data.creditNoteToCustGstSerials;
                    compId = "gstCreditNoteSerialList";
                } else if (a == 7) {
                    dataList = data.purchaseOrderGstSerials;
                    compId = "gstPOSerialList";
                } else if (a == 8) {
                    dataList = data.refundAdvGstSerials;
                    compId = "gstRefundAdvSerialList";
                } else if (a == 9) {
                    dataList = data.refundAmtAgainstInvoiceSerials;
                    compId = "gstRefundAmtAgainstInvoiceSerialList";
                } else if (a == 10) {
                    dataList = data.paymentVoucherSerials;
                    compId = "gstPaymentVoucherSerialList";
                } else if (a == 11) {
                    dataList = data.gstSelfInvoiceSerials;
                    compId = "gstSelfInvoiceSerialList";
                } else if (a == 12) {
                    dataList = data.gstCreatePurchaseOrderSerials;
                    compId = "gstCreatePurchaseOrderSerialList";
                }

                for (var j = 0; j < dataList.length; j++) {
                    SerialsData[i++] = '<li><input class="gstSerial-id" type="hidden" value="';
                    SerialsData[i++] = dataList[j].id;
                    SerialsData[i++] = '"/><input class="gstSerial-gst" type="hidden" value="';
                    SerialsData[i++] = dataList[j].gstIn;
                    SerialsData[i++] = '"/><span>';
                    SerialsData[i++] = dataList[j].gstIn;
                    SerialsData[i++] = '</span><input class="gstSerial-cat" type="hidden" value="';
                    SerialsData[i++] = dataList[j].catId;
                    SerialsData[i++] = '"/><input type="text" class="gstSerial-serial" style="margin-left:20px;" onkeypress="return onlyDotsAndNumbers(event);" value="';
                    SerialsData[i++] = dataList[j].serialNo;
                    SerialsData[i++] = '"/></li>';
                }
                $("#" + compId).html("");
                $("#" + compId).html(SerialsData.join(" "));
            }


        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching GST wise Serial No!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });
}


$(function () {

    $("#addUpdateGstSerials").on('click', function () {
        // $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
        var jsonData = {};
        jsonData.useremail = $("#hiddenuseremail").text();
        jsonData.salesInvoiceGstSerials = collectDataFromGstSerialList("gstSalesInvoiceSerialList");
        jsonData.proformaGstSerials = collectDataFromGstSerialList("gstProformaSerialList");
        jsonData.quotationGstSerials = collectDataFromGstSerialList("gstQuotationSerialList");
        jsonData.receiptGstSerials = collectDataFromGstSerialList("gstReceiptSerialList");
        jsonData.advanceReceiptGstSerials = collectDataFromGstSerialList("gstAdvanceReceiptSerialList");
        jsonData.debitNoteToCustGstSerials = collectDataFromGstSerialList("gstDebitNoteSerialList");
        jsonData.creditNoteToCustGstSerials = collectDataFromGstSerialList("gstCreditNoteSerialList");
        jsonData.purchaseOrderGstSerials = collectDataFromGstSerialList("gstPOSerialList");
        jsonData.refundAdvGstSerials = collectDataFromGstSerialList("gstRefundAdvSerialList");
        jsonData.refundAmtAgainstInvoiceSerials = collectDataFromGstSerialList("gstRefundAmtAgainstInvoiceSerialList");
        jsonData.paymentVoucherSerials = collectDataFromGstSerialList("gstPaymentVoucherSerialList");
        jsonData.gstSelfInvoiceSerials = collectDataFromGstSerialList("gstSelfInvoiceSerialList");
        jsonData.createPurchaseOrderSerials = collectDataFromGstSerialList("gstCreatePurchaseOrderSerialList");
        jsonData.serialNoCategory = $('#serialNoCategory option:selected').val();
        jsonData.gstInAllInterval = $('#gstInAllInterval option:selected').val();
        var url = "/orgnization/savegstserial";
        $.ajax({
            url: url,
            data: JSON.stringify(jsonData),
            headers: {
                "X-AUTH-TOKEN": window.authToken
            },
            type: "text",
            method: "POST",
            contentType: 'application/json',
            success: function (data) {
                swal("Serials saved!", "Given entities serials Mapped with GSTIN", "success");
            },
            error: function (xhr, status, error) {
                if (xhr.status == 401) {
                    doLogout();
                } else if (xhr.status == 500) {
                    swal("Error on fetching GST wise Serial No!", "Please retry, if problem persists contact support team", "error");
                }
            },
            complete: function (data) {
                $.unblockUI();
            }
        });
    });

    $("#disableSpecfBtn").on('click', function () {
        $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
        var specificId = $("#itemEntityHiddenId").val();
        var jsonData = {};
        jsonData.useremail = $("#hiddenuseremail").text();
        jsonData.specificId = specificId;
        var url = "/specifics/disableSpecifics";
        $.ajax({
            url: url,
            data: JSON.stringify(jsonData),
            headers: {
                "X-AUTH-TOKEN": window.authToken
            },
            type: "text",
            method: "POST",
            contentType: 'application/json',
            success: function (data) {
                if (data.status) {
                    swal("Succesfully Removed", "", "success");
                    $("a[id*='form-container-close']").trigger('click');
                    if (data.parentAccountCode != "") {
                        $("li[id=" + data.parentAccountCode + "] ul[id='mainChartOfAccount']").find("li[id=" + data.accountCode + "]").remove("");
                    } else {
                        $("ul[id='mainChartOfAccount']").find("li[id=" + data.accountCode + "]").remove("");
                    }
                    $.unblockUI();
                    getVendorData();
                    getAllChartOfAccount();
                } else {
                    $.unblockUI();
                    swal("Error on Ledger removal!", "Please retry, if problem persists contact support team", "error");
                }
            },
            error: function (xhr, status, error) {
                if (xhr.status == 401) {
                    doLogout();
                } else if (xhr.status == 500) {
                    swal("Error on fetching GST wise Serial No!", "Please retry, if problem persists contact support team", "error");
                }
            },
            complete: function (data) {
                $.unblockUI();
                enableDisableCoaButton(true);
                resetCOATdsScreen();
            }
        });
    });

});

function collectDataFromGstSerialList(listId) {
    var multipleItemsData = [];
    $("#" + listId).find("li").each(function () {
        var jsonData = {};
        jsonData.id = $.trim($(this).find('.gstSerial-id').val());
        jsonData.gstIn = $.trim($(this).find('.gstSerial-gst').val());
        jsonData.catId = $.trim($(this).find('.gstSerial-cat').val());
        jsonData.serialNo = $.trim($(this).find('.gstSerial-serial').val());
        multipleItemsData.push(JSON.stringify(jsonData));
    });

    return multipleItemsData;
}

var addBranchForAddUpdateCoa = function (toplevelaccountcode) {
    $("#item-form-container #itemBranch2BtnList").children().remove();
    $("#item-form-container #itemBranch2BtnList").append('<li id="itemBranch2list"><input style="margin-bottom:5px;" type="checkbox" id="checkboxid" name="branchitemcheck" value="" onClick="checkUncheckIncomeCOA(this,2)">&nbsp;&nbsp;&nbsp;Select All</li>');
    var branchlistArray = [];
    if ('3000000000000000000' == toplevelaccountcode || '4000000000000000000' == toplevelaccountcode) {
        branchlistArray = Object.values(COA_ASSET_LIAB_BRANCHES_MAP);
    } else if ('1000000000000000000' == toplevelaccountcode) {
        branchlistArray = Object.values(COA_INCOME_BRANCHES_MAP);
    } else if ('2000000000000000000' == toplevelaccountcode) {
        branchlistArray = Object.values(COA_EXPENSE_BRANCHES_MAP);
    }
    var branchlist = branchlistArray.join('');
    $("#item-form-container #itemBranch2BtnList").append(branchlist);
}


// ***************************** TAX CHANGES ADDED IN COA **********************************************************


function onTaxRateChange(comp) {
    // Check Tax Rate and Tax Formulas for That Rate in Applicable Date
    var gstRate = $(comp).val();
    var gstRateOld = $(comp).attr("gstRateOld");

    //
    if (gstRate != "" && gstRateOld != gstRate) {
        $("#gstTaxRuleDetailsTable tr[id='1'] input[id='itemgstrate'").val((gstRate / 2));
        $("#gstTaxRuleDetailsTable tr[id='2'] input[id='itemgstrate'").val((gstRate / 2));
        $("#gstTaxRuleDetailsTable tr[id='3'] input[id='itemgstrate'").val(gstRate);
        $("#tax-coa-container").show();
    } else {
        $("#gstTaxRuleDetailsTable tr[id='1'] input[id='itemgstrate'").val("");
        $("#gstTaxRuleDetailsTable tr[id='2'] input[id='itemgstrate'").val("");
        $("#gstTaxRuleDetailsTable tr[id='3'] input[id='itemgstrate'").val("");
        $("#tax-coa-container").hide();
    }
}

function onGSTRateSelect(comp) {
    var gstRateArr = $(comp).val();
    var flag = false;
    if (gstRateArr != "" && gstRateArr.length > 0) {
        for (var i = 0; i < gstRateArr.length; i++) {
            if (gstRateArr[i] == "OTHER") {
                flag = true;
            }
        }
        if (flag) {
            $("#GSTTaxRate").show();
        } else {
            $("#GSTTaxRate").val("");
            $("#GSTTaxRate").hide();
        }
    } else {
        $("#GSTTaxRate").val("");
        $("#GSTTaxRate").hide();
    }
}

function onCessRateSelect(comp) {
    var cessRateArr = $(comp).val();
    var flag = false;
    if (cessRateArr != "" && cessRateArr.length > 0) {
        for (var i = 0; i < cessRateArr.length; i++) {
            if (cessRateArr[i] == "OTHER") {
                flag = true;
            }
        }
        if (flag) {
            $("#cessTaxRate").show();
        } else {
            $("#cessTaxRate").val("");
            $("#cessTaxRate").hide();
        }
    } else {
        $("#cessTaxRate").val("");
        $("#cessTaxRate").hide();
    }
}

// Check Tax Rate and Tax Formulas for That Rate in Applicable Date
function onCessRateChange(comp) {
    var cessRate = $(comp).val();
    var cessRateOld = $(comp).attr("cessRateOld");
    if (cessRate != "" && cessRateOld != cessRate) {
        $("#gstTaxRuleDetailsTable tr[id='4'] input[id='itemgstrate'").val(cessRate);
        $("#tax-coa-container").show();
    } else {
        $("#gstTaxRuleDetailsTable tr[id='4'] input[id='itemgstrate'").val("");
    }
}

function onGSTCategoryChange(comp) {
    var gstCategory = $(comp).val();
    if (gstCategory != "") {
        $("#GSTTaxRate").val("");
        $("#cessTaxRate").val("");
        $('#GSTTaxRate').prop('readonly', true);
        $('#cessTaxRate').prop('readonly', true);
    } else {
        $('#GSTTaxRate').prop('readonly', false);
        $('#cessTaxRate').prop('readonly', false);
    }
}

function rcmToggleCheck(elem) {
    var rcmRate = $(elem).val();
    var value = $(elem).attr('id');
    var val = value.substring(15, value.length);
    if (rcmRate == "") {
        $('input[name="vendoritemcheck"][value=' + val + ']').prop('checked', false);
        $('input[name="vendoritemcheck"][value=' + val + ']').attr('checked', false);
    } else {
        $('input[name="vendoritemcheck"][value=' + val + ']').prop("checked", true);
        $('input[name="vendoritemcheck"][value=' + val + ']').attr('checked', true);
    }
    var check_box_values = $('input[name="vendoritemcheck"]:checkbox:checked').map(function () {
        return this.value;
    }).get();
    var length = check_box_values.length;
    if (length > 0) {
        var text = length + " " + "Items Selected";
        $("#vendordropdown").text(text);
        $("#vendordropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
    }
    if (check_box_values == 0) {
        $("#vendordropdown").text("None Selected");
        $("#vendordropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
    }
}

//******************TDS

function showTdsSetupPopup(elem) {
    $("#TDSSetupModal").modal('show');
}

function showVendTDSSetup(elem, id, name) {
    $("#tdsSpecificId").val(id);
    $("#specificTdsName").val(name);
    var tdsWhType = $(elem).closest("div").find(".tdsWhType").val();
    var tdsTaxRate = $(elem).closest("div").find(".tdsTaxRate").val();
    var tdsTaxTransLimit = $(elem).closest("div").find(".tdsTaxTransLimit").val();
    var tdsTaxOverallLimitApply = $(elem).closest("div").find(".tdsTaxOverallLimitApply").val();
    var overallLimit = $(elem).closest("div").find(".overallLimit").val();
    var tdsFromDate = $(elem).closest("div").find(".tdsFromDate").val();
    var tdsToDate = $(elem).closest("div").find(".tdsToDate").val();
    $("#vendTdsTaxWHType").val(tdsWhType);
    $("#vendTdsTaxRate").val(tdsTaxRate);
    $("#vendTdsTaxRate").attr("coaTaxRate", tdsTaxRate);
    $("#vendTdsTaxTransLimit").val(tdsTaxTransLimit);
    $("#vendTdsTaxOverallLimitApply").val(tdsTaxOverallLimitApply);
    $("#vendTdsTaxOverallLimitApply").trigger("change");
    $("#vendTdsTaxOverallLimit").val(overallLimit);
    $("#vendTdsFromDate").val(tdsFromDate);
    $("#vendTdsToDate").val(tdsToDate);
    $("#VendTDSSetupModal").modal('show');
}

$(function () {

    $("#tdsFromDate").datepicker({
        changeMonth: true,
        changeYear: true,
        dateFormat: 'MM d,yy',
        yearRange: '' + new Date().getFullYear() - 100 + ':' + maximumYear + '',
        onSelect: function (x, y) {
            $(this).focus();
        }
    });

    $("#tdsToDate").datepicker({
        changeMonth: true,
        changeYear: true,
        dateFormat: 'MM d,yy',
        yearRange: '' + new Date().getFullYear() - 100 + ':' + maximumYear + '',
        onSelect: function (x, y) {
            $(this).focus();
        }
    });

    $("#vendTdsFromDate").datepicker({
        changeMonth: true,
        changeYear: true,
        dateFormat: 'MM d,yy',
        yearRange: '' + new Date().getFullYear() - 100 + ':' + maximumYear + '',
        onSelect: function (x, y) {
            $(this).focus();
        }
    });

    $("#vendTdsToDate").datepicker({
        changeMonth: true,
        changeYear: true,
        dateFormat: 'MM d,yy',
        yearRange: '' + new Date().getFullYear() - 100 + ':' + maximumYear + '',
        onSelect: function (x, y) {
            $(this).focus();
        }
    });

    $("#vendTdsUptoDate").datepicker({
        changeMonth: true,
        changeYear: true,
        dateFormat: 'MM d,yy',
        yearRange: '' + new Date().getFullYear() - 100 + ':' + maximumYear + '',
        onSelect: function (x, y) {
            $(this).focus();
        }
    });


});

function resetCOATdsScreen() {
    $("#tdsTaxWHType").val("");
    $("#tdsTaxRate").val("");
    $("#tdsTaxTransLimit").val("");
    $("#tdsTaxOverallLimitApply").val("");
    $("#tdsTaxOverallLimit").val("");
    $("#tdsFromDate").val("");
    $("#tdsToDate").val("");
    $(".overallLimit").css("display", "none");
}

function resetVendTdsScreen() {
    $("#tdsSpecificId").val("");
    $("#specificTdsName").html("");
    $("#vendTdsTaxWHType").val("");
    $("#vendTdsTaxRate").val("");
    $("#vendTdsTaxTransLimit").val("");
    $("#vendTdsTaxOverallLimitApply").val("");
    $("#vendTdsTaxOverallLimit").val("");
    $("#vendTdsFromDate").val("");
    $("#vendTdsToDate").val("");
    $("#vendTdsExpenceAmount").val("");
    $("#vendTdsAlreadyEffected").val("");
    $("#vendTdsUptoDate").val("");
    $("#vendTdsSupportingDoc").val("");
    $(".overallLimit").css("display", "none");
}

function checkApplicableDate(comp) {
    var date = $("#applicableDate").val();
    if (date == "") {
        swal("Invalid Date", "Applicable Date should be filled", "error");
        $(comp).val("");
    }
}

// ***************************** TAX CHANGES ADDED IN COA **********************************************************



