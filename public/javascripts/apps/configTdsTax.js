// ********** TDS setup and Calulation JS ***********


$(function () {
// ************ Initial Calls ******************
    $("#tdsTaxFromDate").datepicker();
    $("#tdsTaxToDate").datepicker();

    $("#addTdsBasicButton").on("click", addTdsBasicDetails);
    $("#removeTdsBasicButton").on("click", removeTdsBasicDetails);

    $("#addTdsAdvButton").on("click", addTdsAdvanceDetails);
    $("#removeTdsAdvButton").on("click", removeTdsAdvanceDetails);

    $("#tdsTaxSetupType").on("change", function () {
        var value = $(this).val();
        if (value == "BASIC") {
            $("#tdsAdvanceSetupDiv").hide();
            displayTdsBasicDetails();
            $("#tdsTaxTableHistory").show();
            $("#tdsTaxTableAdvHistory").hide();
            var rows = $("#tdsTaxBasicTable > tbody > tr").length;
            if (rows < 1) {
                $("#addTdsBasicButton").trigger("click");
            }
            $("#tdsBasicSetupDiv").show();
            $("#fromToDateDiv").find('label').show();
            $("#fromToDateDiv").find('input').show();

        } else if (value == "ADVANCE") {
            $("#tdsBasicSetupDiv").hide();
            displayTdsAdvanceDetails();
            $("#tdsTaxTableHistory").hide();
            $("#tdsTaxTableAdvHistory").show();

            var rows = $("#tdsTaxAdvanceTable > tbody > tr").length;
            if (rows < 1) {
                $("#addTdsAdvButton").trigger("click");
            }
            $("#tdsAdvanceSetupDiv").show();
            $("#fromToDateDiv").find('label').hide();
            $("#fromToDateDiv").find('input').hide();
        } else {
            $("#fromToDateDiv").find('label').show();
            $("#fromToDateDiv").find('input').show();
            $("#tdsBasicSetupDiv").hide();
            $("#tdsAdvanceSetupDiv").hide();

        }
    });

//	$("#addTdsBasicButton").trigger("click");
//	$("#addTdsAdvButton").trigger("click");

    $("#addNewTdsTax").on("click", function () {
        $("#fromToDateDiv").css("display", "inline-block");
    });

    $("#tdsApplyForTrans").multiselect({
        includeSelectAllOption: true,
        enableFiltering: true,
        nonSelectedText: 'Select TDS applicable Transactions',
        onChange: function (element, checked) {
            if (checked == true) {
            } else if (checked == false) {
            }
        }
    });

//	$("#configTdsOnTransactions").on("click",function(){
//		resetVendorTdsScreen();
//		$("#tdsApplyForTrans").multiselect({
//		    includeSelectAllOption: true,
//		    enableFiltering :true,
//		    onChange: function(element, checked) {
//		      if(checked == true) {
//		      }
//		      else if(checked == false) {
//		      }
//		    }
//		  });
//		$("#applyTdsForTransDiv").css("display","inline-block");
//	});
});

//************ END Initial Calls ******************


function resetCOATdsScreen() {
    $("#tdsTaxWHType").val("");
    $("#tdsTaxRate").val("");
    $("#tdsTaxTransLimit").val("");
    $("#tdsTaxOverallLimitApply").val("");
    $("#tdsTaxOverallLimit").val("");
    $("#tdsFromDate").val("");
    $("#tdsToDate").val("");
}

function resetApplyTransScreen() {
    $("#configTdsOnTransactions").click();
    resetVendorTdsScreen();
}


// ************** START TDS BASIC TABLE DETAILS ******************
function checkForTdsOverallLimit(comp) {
    var parentTr = $(comp).closest("tr");
    var element = parentTr.find("input[class*='overallLimit']")
    var selected = $(comp).val();
    if (selected == "") {
        element.val("");
        element.css("display", "none");
        return false;
    }
    if (selected == "1") {
        element.css("display", "inline-block");
    } else {
        element.val("");
        element.css("display", "none");
    }
}

function addTdsBasicDetails() {
    if (!validateBasicTds()) {
        return false;
    }

    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var jsonData = {};
    var useremail = $("#hiddenuseremail").text();
    jsonData.usermail = useremail;
    var url = "/vendorTds/getBasicRow";
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        async: false,
        method: "POST",
        contentType: 'application/json',
        success: function (data) {
            $("#tdsTaxBasicTable > tbody").append(basicTdsRowTemplate(data));
            // For Upload Docs
            var count = $("#tdsTaxBasicTable > tbody > tr").length;
            $("#tdsTaxBasicTable > tbody > tr:last").find('#docuploadurl').addClass("tdsUpload" + count);
            $("#tdsTaxBasicTable > tbody > tr:last").find('.docuploadurlSpan').attr('id', 'tdsUpload' + count);
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching Transaction detail!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });
}

function basicTdsRowTemplate(data) {
    var itemData = [];
    var i = 0;
    for (var j = 0; j < data.expenceSpecificsList.length; j++) {
        itemData[i++] = '<option value="' + data.expenceSpecificsList[j].id + '">';
        itemData[i++] = data.expenceSpecificsList[j].name;
        itemData[i++] = '</option>';
    }
    var vendorData = [];
    i = 0;
    for (var j = 0; j < data.vendorsList.length; j++) {
        vendorData[i++] = '<option value="' + data.vendorsList[j].id + '">';
        vendorData[i++] = data.vendorsList[j].name;
        vendorData[i++] = '</option>';
    }
    var whData = [];
    i = 0;
    for (var j = 0; j < data.tdsWHSectionList.length; j++) {
        whData[i++] = '<option value="' + data.tdsWHSectionList[j].id + '">';
        whData[i++] = data.tdsWHSectionList[j].name;
        whData[i++] = '</option>';
    }
    var basicTdsRow = [];
    var i = 0;
    basicTdsRow[i++] = '<tr id="tdsTaxTR">';
    basicTdsRow[i++] = '<td><select class="tdsItem" name="tdsTaxItem" id="tdsTaxItem" onChange=""><option value="">--Please Select--</option>';
    basicTdsRow[i++] = itemData.join(" ");
    basicTdsRow[i++] = '</select></td>';
    basicTdsRow[i++] = '<td class="tdsVendorTd"><select class="masterList" style="width: 200px;" name="tdsTaxVendor" id="tdsTaxVendor"><option value="">--Please Select--</option>';
    basicTdsRow[i++] = vendorData.join(" ");
    basicTdsRow[i++] = '</select></td>';
    basicTdsRow[i++] = '<td><select class="whType" name="tdsTaxWHType" id="tdsTaxWHType"><option value="">--Please Select--</option>';
    basicTdsRow[i++] = whData.join(" ");
    basicTdsRow[i++] = '</select></td>';
    basicTdsRow[i++] = '<td><select class="modeOfcalc" name="tdsTaxModeOfCompute" id="tdsTaxModeOfCompute" onchange="changeOnModeOfCompute(this);" ><option value="">--Please Select--</option><option value="1">Automatic</option><option value="2">Manual</option></select></td>';
    basicTdsRow[i++] = '<td><input class="taxRate" placeholder="Tax Rate" type="text" name="tdsTaxRate" id="tdsTaxRate" onkeypress="return onlyDotsAndNumbers(event);" onchange="	(this);"/></td>';
    basicTdsRow[i++] = '<td><input class="transLimit" placeholder="Limit" type="text" name="tdsTaxTransLimit" id="tdsTaxTransLimit" onkeypress="return onlyDotsAndNumbers(event);" onkeyup=""/></td>';
    basicTdsRow[i++] = '<td colspan="2"><select class="overallLimitApply" name="tdsTaxOverallLimitApply" id="tdsTaxOverallLimitApply" onchange="checkForTdsOverallLimit(this);"><option value="">--Please Select--</option><option value="1">Applicable</option><option value="2">Not Applicable</option></select>';
    basicTdsRow[i++] = '<input class="overallLimit" style="display:none;" placeholder="Limit" type="text" name="tdsTaxOverallLimit" id="tdsTaxOverallLimit" onkeypress="return onlyDotsAndNumbers(event);" onkeyup=""/></td>';
    basicTdsRow[i++] = '<td><input type="text" id="docuploadurl" class="docuploadurl" style="display:inline;" name="tdsUploads" readonly="readonly"/><span id="tdsUploads" class="btn-idos-flat-white btn-upload m-top-10 docuploadurlSpan" style="margin-left: 5px;display:inline;" onclick="uploadFileMultipleWithClass(this.id,this)"><i class="fa fa-2x fa-upload pr-5"></i></span></td>';
    basicTdsRow[i++] = '<td><input type="checkbox" class="removeCheckBox" style="height: 18px;width: 18px;margin: 10px;"></td></tr>';
    return basicTdsRow.join(" ");
}

function removeTdsBasicDetails(comp) {
    if (validateRemovalTr("tdsTaxBasicTable")) {
        $("#tdsTaxBasicTable").find(".removeCheckBox:checkbox:checked").each(function () {
            $(this).closest('tr').remove();
        });
    }
}

function validateBasicTds() {

    if ($("#tdsTaxBasicTable > tbody > tr").length > 0) {
        var lastTr = $("#tdsTaxBasicTable > tbody > tr:last");
        var item = $.trim(lastTr.find(".tdsItem").val());
        var vendor = $.trim(lastTr.find(".masterList").val());
        var whSection = $.trim(lastTr.find(".whType").val());
        var mode = $.trim(lastTr.find(".modeOfcalc").val());
        var taxRate = $.trim(lastTr.find(".taxRate").val());
        var transLimit = $.trim(lastTr.find(".transLimit").val());

        if (item == "" || vendor == "" || whSection == "" || mode == "") {
            swal("Incomplete detail!", "Please Enter/Select Mandatory fields values.", "error");
            return false;
        } else {
            if (mode == "1" || mode == 1) {
                if (taxRate == "" || transLimit == "") {
                    swal("Incomplete detail!", "Please Enter/Select Mandatory fields values.", "error");
                    return false;
                }
            }
        }
    }
    return true;
}

function validateCOATdsTaxes() {


    var whSection = $("#tdsTaxWHType").val();
    if (whSection == "") {
        swal("Incomplete detail!", "Please Select Witholding Tax - Section", "error");
        return false;
    }
    var taxRate = $("#tdsTaxRate").val();
    if (taxRate == "") {
        swal("Incomplete detail!", "Please Enter Tax Rate", "error");
        return false;
    }
    var transLimit = $("#tdsTaxTransLimit").val();
    if (transLimit == "") {
        swal("Incomplete detail!", "Please Select Transaction Limit Apply", "error");
        return false;
    }
    var overAllLimit = "";
    var overAllLimitApply = $("#tdsTaxOverallLimitApply").val();
    if (overAllLimitApply == "") {
        swal("Incomplete detail!", "Please Select Overall Limit Apply", "error");
        return false;
    } else {
        if (overAllLimitApply == 1) {
            overAllLimit = $("#tdsTaxOverallLimit").val();
            if (overAllLimit == "") {
                swal("Incomplete detail!", "Please Enter OverallLimit", "error");
                return false;
            } else if (parseFloat(overAllLimit) < parseFloat(transLimit)) {
                swal("Please check Overall Limit", " Transaction Limit cannot exceed Overall Limit", "error");
            }
        }
    }

    var fromDate = $("#tdsFromDate").val();
    if (fromDate == "") {
        swal("Incomplete detail!", "Please Select From Date", "error");
        return false;
    }
    var toDate = $("#tdsToDate").val();
    if (toDate == "") {
        swal("Incomplete detail!", "Please Select To Date", "error");
        return false;
    }

    if (Date.parse(toDate) < Date.parse(fromDate)) {
        swal("Wrong To Date selected", "To date must be greater than From date", "error");
        return false;
    }

    $("#TDSSetupModal").modal('hide');
}

function validateVendTdsTaxes() {
    var specificId = $("#tdsSpecificId").val();

    var whSection = $("#vendTdsTaxWHType").val();
    if (whSection == "") {
        swal("Incomplete detail!", "Please Select Witholding Tax - Section", "error");
        return false;
    }
    var taxRate = $("#vendTdsTaxRate").val();
    var coaTaxRate = $("#vendTdsTaxRate").attr("coaTaxRate");
    if (taxRate == "") {
        swal("Incomplete detail!", "Please Enter Tax Rate", "error");
        return false;
    }
    var transLimit = $("#vendTdsTaxTransLimit").val();
    if (transLimit == "") {
        swal("Incomplete detail!", "Please Select Transaction Limit Apply", "error");
        return false;
    }
    var overAllLimit = "";
    var overAllLimitApply = $("#vendTdsTaxOverallLimitApply").val();
    if (overAllLimitApply == "") {
        swal("Incomplete detail!", "Please Select Overall Limit Apply", "error");
        return false;
    } else {
        if (overAllLimitApply == 1) {
            overAllLimit = $("#vendTdsTaxOverallLimit").val();
            if (overAllLimit == "") {
                swal("Incomplete detail!", "Please Enter OverallLimit", "error");
                return false;
            } else if (parseFloat(overAllLimit) < parseFloat(transLimit)) {
                swal("Please check Overall Limit", " Transaction Limit cannot exceed Overall Limit", "error");
            }
        }
    }

    var fromDate = $("#vendTdsFromDate").val();
    if (fromDate == "") {
        swal("Incomplete detail!", "Please Select From Date", "error");
        return false;
    }
    var toDate = $("#vendTdsToDate").val();
    if (toDate == "") {
        swal("Incomplete detail!", "Please Select To Date", "error");
        return false;
    }


    if (Date.parse(toDate) < Date.parse(fromDate)) {
        swal("Wrong To Date selected", "To date must be greater than From date", "error");
        return false;
    }
    var expenceAmount = $("#vendTdsExpenceAmount").val();
    var alredyTdseffected = $("#vendTdsAlreadyEffected").val();
    var uptoDate = $("#vendTdsUptoDate").val();
    //var suportDoc = $("#vendTdsSupportingDoc").val();
    var suportDoc="";
    $('select[id="vendTdsSupportingDoc"] option').each(function () {

        if(suportDoc==""){
            suportDoc= this.value;
        }else{
            suportDoc+= ','+this.value;
        }
    });

    if (coaTaxRate != "" && coaTaxRate == taxRate) {
        swal({
                title: "Are you sure?",
                text: "TDS for this item will be calculated on the rates given in COA",
                type: "warning",
                showCancelButton: true,
                confirmButtonColor: '#DD6B55',
                confirmButtonText: 'Yes',
                cancelButtonText: "No",
                closeOnConfirm: true,
                closeOnCancel: true
            },
            function (isConfirm) {
                if (isConfirm) {
                    var expenceAmount = $("#vendTdsExpenceAmount").val();
                    var alredyTdseffected = $("#vendTdsAlreadyEffected").val();
                    var uptoDate = $("#vendTdsUptoDate").val();
                    //var suportDoc = $("#vendTdsSupportingDoc").val();
                    var suportDoc="";
                    $('select[id="vendTdsSupportingDoc"] option').each(function () {
                        if(suportDoc==""){
                            suportDoc= this.value;
                        }else{
                            suportDoc+= ','+this.value;
                        }
                    });

                    $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsSpecificId").val(specificId);
                    $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsWhType").val(whSection);
                    $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsTaxRate").val(taxRate);
                    $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsTaxTransLimit").val(transLimit);
                    $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsTaxOverallLimitApply").val(overAllLimitApply);
                    $("#vendTdsItemList li[name='" + specificId + "']").find(".overallLimit").val(overAllLimit);
                    $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsFromDate").val(fromDate);
                    $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsToDate").val(toDate);
                    $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsExpenceAmount").val(expenceAmount);
                    $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsAlreadyEffected").val(alredyTdseffected);
                    $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsUptoDate").val(uptoDate);
                    $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsSupportingDoc").val(suportDoc);
                    if (!$("#vendTdsItemList li[name='" + specificId + "']").find("input[name='vendTdscheck']").is(':checked')) {
                        $("#vendTdsItemList li[name='" + specificId + "']").find("input[name='vendTdscheck']").click();
                    }
                    $("#VendTDSSetupModal").modal('hide');

                } else {
                    return false;
                }
            });
    } else {
        var expenceAmount = $("#vendTdsExpenceAmount").val();
        var alredyTdseffected = $("#vendTdsAlreadyEffected").val();
        var uptoDate = $("#vendTdsUptoDate").val();
        //var suportDoc = $("#vendTdsSupportingDoc").val();
        var suportDoc="";
        $('select[id="vendTdsSupportingDoc"] option').each(function () {

            if(suportDoc==""){
                suportDoc= this.value;
            }else{
                suportDoc+= ','+this.value;
            }
        });

        $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsSpecificId").val(specificId);
        $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsWhType").val(whSection);
        $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsTaxRate").val(taxRate);
        $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsTaxTransLimit").val(transLimit);
        $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsTaxOverallLimitApply").val(overAllLimitApply);
        $("#vendTdsItemList li[name='" + specificId + "']").find(".overallLimit").val(overAllLimit);
        $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsFromDate").val(fromDate);
        $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsToDate").val(toDate);
        $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsExpenceAmount").val(expenceAmount);
        $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsAlreadyEffected").val(alredyTdseffected);
        $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsUptoDate").val(uptoDate);
        $("#vendTdsItemList li[name='" + specificId + "']").find(".tdsSupportingDoc").val(suportDoc);
        if (!$("#vendTdsItemList li[name='" + specificId + "']").find("input[name='vendTdscheck']").is(':checked')) {
            $("#vendTdsItemList li[name='" + specificId + "']").find("input[name='vendTdscheck']").click();
        }
        $("#VendTDSSetupModal").modal('hide');

    }


}

function getCOATdsData() {
    var whSection = $("#tdsTaxWHType").val();
    var taxRate = $("#tdsTaxRate").val();
    var transLimit = $("#tdsTaxTransLimit").val();
    var overAllLimitApply = $("#tdsTaxOverallLimitApply").val();
    var overAllLimit = $("#tdsTaxOverallLimit").val();
    var fromDate = $("#tdsFromDate").val();
    var toDate = $("#tdsToDate").val();
    var rowData = {};
    if (whSection != "" && taxRate != "" && transLimit != "" && overAllLimitApply != "" && fromDate != "" && toDate != "") {
        rowData.tdsWHSection = whSection;
        rowData.tdsTaxRate = taxRate;
        rowData.tdsTransLimit = transLimit;
        rowData.tdsOverAllLimitApply = overAllLimitApply;
        rowData.tdsOverAllLimit = overAllLimit;
        rowData.fromDate = fromDate;
        rowData.toDate = toDate;
    }
    return rowData;
}

function resetVendTdsScreen() {
    $("input[name='vendTdscheck']").prop("checked", false);
    $(".tdsSpecificId").val("");
    $("#tdsSpecificId").val("");
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
}

function getVendTdsData() {
    var vendTDSData = [];
    $('input[name="vendTdscheck"]:checkbox:checked').each(function () {
        var rowData = {};
        var vendSpecificId = $(this).closest("li").find(".tdsSpecificId").val();
        if (vendSpecificId != "") {
            var whSection = $(this).closest("li").find(".tdsWhType").val();
            var taxRate = $(this).closest("li").find(".tdsTaxRate").val();
            var transLimit = $(this).closest("li").find(".tdsTaxTransLimit").val();
            var overAllLimitApply = $(this).closest("li").find(".tdsTaxOverallLimitApply").val();
            var overAllLimit = $(this).closest("li").find(".overallLimit").val();
            var fromDate = $(this).closest("li").find(".tdsFromDate").val();
            var toDate = $(this).closest("li").find(".tdsToDate").val();
            var expenceAmount = $(this).closest("li").find(".tdsExpenceAmount").val();
            var tdsAlreadyEffected = $(this).closest("li").find(".tdsAlreadyEffected").val();
            var uptoDate = $(this).closest("li").find(".tdsUptoDate").val();
            var supportingDocs = $(this).closest("li").find(".tdsSupportingDoc").val();

            rowData.tdsSpecificId = vendSpecificId;
            rowData.tdsWHSection = whSection;
            rowData.tdsTaxRate = taxRate;
            rowData.tdsTransLimit = transLimit;
            rowData.tdsOverAllLimitApply = overAllLimitApply;
            rowData.tdsOverAllLimit = overAllLimit;
            rowData.fromDate = fromDate;
            rowData.toDate = toDate;
            rowData.expenceAmount = expenceAmount;
            rowData.tdsAlreadyEffected = tdsAlreadyEffected;
            rowData.uptoDate = uptoDate;
            rowData.supportingDocs = supportingDocs;
            vendTDSData.push(rowData);
        }
    });
    return vendTDSData;
}

function tdsRateValidation(comp) {
    var rate = $(comp).val();
    if (rate != "") {
        if (parseFloat(rate) > 100) {
            swal("Invalid Tax Rate!", "Tax Rate must be Less then or Equal to 100%", "error");
            $(comp).val("");
            return false;
        }
    }
}

function displayTdsBasicDetails() {
    var useremail = $("#hiddenuseremail").text();
    var jsonData = {};
    jsonData.usermail = useremail;
    var url = "/vendorTds/displayTdsDetails";
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        async: false,
        method: "POST",
        contentType: 'application/json',
        success: function (data) {

            $("#tdsTaxTableHistory > tbody").html("");
            for (var j = 0; j < data.tdsHistoryData.length; j++) {
                var row = [];
                var i = 0;
                row[i++] = "<tr><td>" + data.tdsHistoryData[j].expenceLedger;
                row[i++] = "</td><td>" + data.tdsHistoryData[j].vendorName;
                row[i++] = "</td><td>" + data.tdsHistoryData[j].tdsSection;
                row[i++] = "</td><td>" + data.tdsHistoryData[j].tdsRate;
                row[i++] = "</td><td>" + data.tdsHistoryData[j].mode;
                row[i++] = "</td><td>" + data.tdsHistoryData[j].transLimit;
                row[i++] = "</td><td>" + data.tdsHistoryData[j].transOverAllLimit;
                row[i++] = '</td><td><button class="btn btn-submit btn-idos editTds" style="display:none;" title="Edit" onclick="editTdsRow();"><i class="fa fa-pencil pr-5"></i>Edit</button>';
                row[i++] = "</td></tr>";

                $("#tdsTaxTableHistory > tbody").append(row.join(" "));
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching Transaction detail!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
        }
    });

}

//************** END TDS BASIC TABLE DETAILS **********************
// ************* START TDS ADVANCE TABLE DETAILS*******************
function addTdsAdvanceDetails() {
    if (!validateAdvanceTds()) {
        return false;
    }
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var jsonData = {};
    var useremail = $("#hiddenuseremail").text();
    jsonData.usermail = useremail;
    var url = "/vendorTds/getAdvanceRow";
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        async: false,
        method: "POST",
        contentType: 'application/json',
        success: function (data) {
            $("#tdsTaxAdvanceTable > tbody").append(advanceTdsRowTemplate(data));
            $("#tdsTaxAdvanceTable > tbody").find("table > tbody > tr:last").find('.uptoDate').datepicker({
                changeMonth: true,
                changeYear: true,
                dateFormat: 'MM d,yy',
                yearRange: '' + new Date().getFullYear() - 100 + ':' + maximumYear + '',
                onSelect: function (x, y) {
                    $(this).focus();
                }
            });
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching Transaction detail!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });

}

function advanceTdsRowTemplate(data) {

    var itemData = [];
    var i = 0;
    for (var j = 0; j < data.expenceSpecificsList.length; j++) {
        itemData[i++] = '<option value="' + data.expenceSpecificsList[j].id + '">';
        itemData[i++] = data.expenceSpecificsList[j].name;
        itemData[i++] = '</option>';
    }

    var advTdsRow = [];
    i = 0;
    advTdsRow[i++] = '<tr><td><input type="hidden" class="advTdsId" /><select class="tdsItems" name="tdsTaxItem" id="tdsTaxItem" onChange="expenceLedgerChanged(this);"><option value="">--Please Select--</option>';
    advTdsRow[i++] = itemData.join(" ");
    advTdsRow[i++] = '</select></td><td colspan="7"><table class="table innerTdsAdvTable"><tbody>';
    advTdsRow[i++] = innerTableRowTemplate(data);
    advTdsRow[i++] = '</tbody></table></td><td><input type="checkbox" class="removeCheckBox" style="height: 18px;width: 18px;margin: 10px;"></td></tr>';
    return advTdsRow.join(" ");
}

function removeTdsAdvanceDetails(comp) {
    if (validateRemovalTr("tdsTaxAdvanceTable")) {
        $("#tdsTaxAdvanceTable").find(".removeCheckBox:checkbox:checked").each(function () {
            $(this).closest('tr').remove();
        });
    }
}

function validateAdvanceTds() {
    if ($("#tdsTaxAdvanceTable > tbody > tr").length > 0) {
        var lastTr = $("#tdsTaxAdvanceTable > tbody > tr:last");
        var item = $.trim(lastTr.find(".tdsItems").val());
        if (item == "") {
            swal("Incomplete detail!", "Please Select Expence Ledger", "error");
            return false;
        }
        var parentInnerTable = lastTr.find("table");
        if (parentInnerTable.find("tbody tr").length > 0) {
            var vendor = parentInnerTable.find("tbody tr:last").find('.masterList').val();
            if (vendor == "") {
                swal("Incomplete detail!", "Please Select Vendor", "error");
                return false;
            }
        }
    }
    return true;
}

function saveTdsAdvanceTaxes() {
    if (!validateAdvanceTds()) {
        return false;
    }
    var jsonData = {};
    var tdsRowData = [];
    $("#tdsTaxAdvanceTable > tbody > tr").each(function () {
        var rowData = {};
        var tdsInnerRowData = [];
        var id = $.trim($(this).find(".advTdsId").val());
        var item = $.trim($(this).find(".tdsItems").val());
        $(this).find("table > tbody > tr").each(function () {
            var innerRowData = {};
            var vendor = $.trim($(this).find(".masterList").val());
            var expenceAmt = $.trim($(this).find(".expenceAmt").val());
            var tdsEffected = $.trim($(this).find(".tdsEffected").val());

            var uptoDate = $.trim($(this).find(".uptoDate").val());
            innerRowData.vendor = vendor;
            innerRowData.expenceAmt = expenceAmt;
            innerRowData.tdsEffected = tdsEffected;
            innerRowData.uptoDate = uptoDate;
            tdsInnerRowData.push(innerRowData);
        });
        rowData.id = id;
        rowData.tdsItem = item;
        rowData.tdsDetails = tdsInnerRowData;
        tdsRowData.push(rowData);
    });
    var useremail = $("#hiddenuseremail").text();
    jsonData.usermail = useremail;
    jsonData.tdsAdvanceRowData = tdsRowData;
    var url = "/vendorTds/saveAdvanceTds";
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        async: false,
        method: "POST",
        contentType: 'application/json',
        success: function (data) {
            if (data.status == true) {
                resetVendorTdsScreen();
                displayTdsAdvanceDetails();
                $("#tdsAdvanceSetupDiv").show();
                $("#addTdsAdvButton").trigger("click");
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching Transaction detail!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });

}

function displayTdsAdvanceDetails() {
    var useremail = $("#hiddenuseremail").text();
    var jsonData = {};
    jsonData.usermail = useremail;
    var url = "/vendorTds/displayTdsAdvanceDetails";
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        async: false,
        method: "POST",
        contentType: 'application/json',
        success: function (data) {

            $("#tdsTaxTableAdvHistory > tbody").html("");
            for (var j = 0; j < data.tdsAdvHistoryData.length; j++) {
                var row = [];
                var dataString = JSON.stringify(data.tdsAdvHistoryData[j]);
                var i = 0;
                row[i++] = "<tr><td>" + data.tdsAdvHistoryData[j].expenceLedger;
                row[i++] = "</td><td>" + data.tdsAdvHistoryData[j].vendorName;
                row[i++] = "</td><td>" + data.tdsAdvHistoryData[j].expenceAmount;
                row[i++] = "</td><td>" + data.tdsAdvHistoryData[j].tdsAlreadyEffect;
                row[i++] = "</td><td>" + data.tdsAdvHistoryData[j].uptoDate;
                row[i++] = "</td><td><button class='btn btn-submit btn-idos editTds' title='Edit' onclick='editTdsAdvRow(" + dataString + ");'><i class='fa fa-pencil pr-5'></i>Edit</button>";
                row[i++] = "</td></tr>";

                $("#tdsTaxTableAdvHistory > tbody").append(row.join(" "));
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching Transaction detail!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
        }
    });

}

function editTdsAdvRow(dataString) {


    $("#tdsTaxAdvanceTable > tbody").html("");
    $("#addTdsAdvButton").trigger("click");
    var parentTr = $("#tdsTaxAdvanceTable > tbody > tr:last");
    parentTr.find('.advTdsId').val(dataString.id);
    parentTr.find('.tdsItems').val(dataString.expenceLedgerId);
    parentTr.find('.tdsItems').prop('disabled', true);
    parentTr.find('.masterList').html("<option value='" + dataString.vendorId + "' selected >" + dataString.vendorName + "</option>")
    parentTr.find('.masterList').prop('disabled', true);
    parentTr.find('.expenceAmt').val(dataString.expenceAmount);
    parentTr.find('.tdsEffected').val(dataString.tdsAlreadyEffect);
    parentTr.find('.uptoDate').val(dataString.uptoDate);
    parentTr.find('button').hide();
    custVendSelect2();
}

//****************** END ADVANCE TABLE DETAILS*****************************
// *************** START TDS INNER TABLE FOR ADVANCE TABLE DETAILS **********

function expenceLedgerChanged(comp) {
    $(comp).closest('tr').find("table > tbody > tr:last").find(".addInnerTdsRow").trigger("click");
    var count = $(comp).closest('tr').find("table > tbody > tr").length;
    if (count > 1) {
        for (var i = 0; i < count - 1; i++) {
            $(comp).closest('tr').find("table > tbody  tr:First").remove();
        }
    }
}

var addInnerTdsRow = function (comp) {
    var parentInnerTable = $(comp).closest("table");
    var mainRow = parentInnerTable.closest("tr");
    var expenceLedger = mainRow.find(".tdsItems").val();
    if (expenceLedger == "") {
        return false;
    }
    if (parentInnerTable.find("tbody tr").length > 1) {
        var vendor = parentInnerTable.find("tbody tr:last").find('.masterList').val();
        if (vendor == "") {
            swal("Incomplete detail!", "Please Select Vendor", "error");
            return false;
        }
    }

    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var jsonData = {};
    var useremail = $("#hiddenuseremail").text();
    jsonData.usermail = useremail;
    jsonData.expenceLedger = expenceLedger;
    var url = "/vendorTds/getAdvanceRow";
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        async: false,
        method: "POST",
        contentType: 'application/json',
        success: function (data) {
            parentInnerTable.find("tbody").append(innerTableRowTemplate(data));
            parentInnerTable.find("tbody tr:last").find('.uptoDate').datepicker({
                changeMonth: true,
                changeYear: true,
                dateFormat: 'MM d,yy',
                yearRange: '' + new Date().getFullYear() - 100 + ':' + maximumYear + '',
                onSelect: function (x, y) {
                    $(this).focus();
                }
            });
            $(comp).hide();
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching Transaction detail!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });

}

function innerTableRowTemplate(data) {
    var vendorData = [];
    i = 0;
    for (var j = 0; j < data.vendorsList.length; j++) {
        vendorData[i++] = '<option value="' + data.vendorsList[j].id + '">';
        vendorData[i++] = data.vendorsList[j].name;
        vendorData[i++] = '</option>';
    }
    var innerTdsRow = [];
    var i = 0;
    innerTdsRow[i++] = '<tr><td><select class="masterList" name="tdsTaxVendor" id="tdsTaxVendor" onChange=""><option value="">--Please Select--</option>';
    innerTdsRow[i++] = vendorData.join(" ");
    innerTdsRow[i++] = '</select></td><td><input class="expenceAmt" type="text" name="tdsTaxExpenceAmt" placeholder="Expence Amount" id="tdsTaxExpenceAmt" onkeypress="return onlyDotsAndNumbers(event);" onkeyup=""/></td>';
    innerTdsRow[i++] = '<td><input class="tdsEffected"  type="text" placeholder="TDs already effected" name="tdsTaxTdsEffected" id="tdsTaxTdsEffected" onkeypress="return onlyDotsAndNumbers(event);" onkeyup=""/></td>';
    innerTdsRow[i++] = '<td><input class="uptoDate" type="text" name="tdsTaxUptoDate" placeholder="Date"></td>';
    innerTdsRow[i++] = '<td><button class="addInnerTdsRow" title="add Row" onclick="addInnerTdsRow(this);"><i class="fa fa-plus-circle fa-lg"></i></button>';
    innerTdsRow[i++] = '<button class="resetInnerTdsRow" title="Reset Row" onclick="resetInnerTdsRow(this);"><i class="fa fa-refresh fa-lg"></i></button>';
    innerTdsRow[i++] = '<button class="removeInnerTdsRow" title="Remove Row" onclick="removeInnerTdsRow(this);"><i class="fa fa-minus-circle fa-lg"></i></button><td></tr>';
    return innerTdsRow.join(" ");
}

var resetInnerTdsRow = function (comp) {
    var parentTR = $(comp).closest("tr");
    parentTR.find(".masterList option:first").prop("selected", "selected");
    parentTR.find(".expenceAmt").val("");
    parentTR.find(".tdsEffected").val("");
    parentTR.find(".uptoDate").val("");
}

var removeInnerTdsRow = function (comp) {
    var parentInnerTable = $(comp).closest("table");
    var count = parentInnerTable.find("tbody tr").length;
    if (count > 1) {
        $(comp).closest("tr").remove();
    }
    parentInnerTable.find("tbody tr:last").find("button[class='addInnerTdsRow']").show();
}

//************** END TDS INNER TABLE FOR ADVANCE TABLE DETAILS *********

function saveTdsApplyTransactions(elem) {
    var tdsTrans = $("#tdsApplyForTrans").val();
    if (tdsTrans.length <= 0) {
        swal("Invalid Details!", "Please Select TDS applicable Transactions", "error");
        return false;
    }
    var tdsTransValue = "";
    for (var i = 0; i < tdsTrans.length; i++) {
        tdsTransValue += tdsTrans[i] + ",";
    }
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var jsonData = {};
    var useremail = $("#hiddenuseremail").text();
    jsonData.usermail = useremail;
    jsonData.tdsTrans = tdsTransValue;
    var url = "/vendorTds/saveTdsApplyedTrans";
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        async: false,
        method: "POST",
        contentType: 'application/json',
        success: function (data) {
            if (data.status == false) {
                swal("Error on Save Transaction detail!", "Please retry, if problem persists contact support team", "success");
                return;
            }
            resetApplyTransScreen();
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching Transaction detail!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });

}

function getTdsApplicableTransactions(elem) {
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var jsonData = {};
    var useremail = $("#hiddenuseremail").text();
    jsonData.usermail = useremail;
    var url = "/vendorTds/getTdsApplyedTrans";
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        async: false,
        method: "POST",
        contentType: 'application/json',
        success: function (data) {
            if (data.status == true) {
                var dataArray = data.transactions.split(",");
                $("#tdsApplyForTrans").val(dataArray);
                $("#tdsApplyForTrans").multiselect("refresh");
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching Transaction detail!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });
}

function changeOnModeOfCompute(comp) {
    var parentTr = $(comp).closest('tr');
    var val = $(comp).val();
    if (val == "1") {
        parentTr.find('.taxRate').prop('disabled', false);
        parentTr.find('.transLimit').prop('disabled', false);
        parentTr.find('.overallLimitApply').prop('disabled', false);
        parentTr.find('.overallLimit').prop('disabled', false);
    } else if (val == "2") {
        parentTr.find('.taxRate').val("");
        parentTr.find('.transLimit').val("");
        parentTr.find('.overallLimit').val("");
        parentTr.find('.overallLimitApply').val("");
        parentTr.find('.taxRate').prop('disabled', true);
        parentTr.find('.transLimit').prop('disabled', true);
        parentTr.find('.overallLimitApply').prop('disabled', true);
        parentTr.find('.overallLimit').prop('disabled', true);
        parentTr.find('.overallLimit').hide();
    }
}

function tdsSelectChangeOnItemsVend() {
    $("#vendTdsItemList li").hide();
    $('input[name="vendoritemcheck"]:checkbox:checked').each(function () {
        var itemId = $(this).closest("li").attr("name");
        var isTdsSpecific = $(this).closest("li").attr("isTdsSpecific");
        if (itemId != undefined && itemId != "" && isTdsSpecific == "true") {
            $("#vendTdsItemList li[name='" + itemId + "']").show();
        }
    });
}

function checkUncheckTds(elem) {
    var checked = $(elem).is(':checked');
    var check_box_values = $('input[name="vendTdscheck"]:checkbox:checked').map(function () {
        return this.value;
    }).get();
    if (check_box_values > 0) {
        $("#vendTdsdropdown").innerText = "Selected";
    } else {
        $("#vendTdsdropdown").innerText = "None Selected";
    }
    $("#vendTdsdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
    if (checked == true) {
        var checkvalue = $(elem).val();

        var check_box_values = $('input[name="vendTdscheck"]:checkbox:checked').map(function () {
            return this.value;
        }).get();
        var length = check_box_values.length;
        if (length > 0) {
            var text = length + " " + "Items Selected";
            $("#vendTdsdropdown").text(text);
            $("#vendTdsdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
        }
        if (check_box_values == 0) {
            $("#vendTdsdropdown").text("None Selected");
            $("#vendTdsdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
        }
    }
    if (checked == false) {
        var checkvalue = $(elem).val();
        if (checkvalue == "") {
            if (confirm("Do u want to remove all your selected vendor items and their unit prices!")) {
                $('input[name="vendTdscheck"]').each(function () {
                    $(this).prop("checked", false);
                });

            }
        }
        var check_box_values = $('input[name="vendTdscheck"]:checkbox:checked').map(function () {
            return this.value;
        }).get();
        var length = check_box_values.length;
        if (length > 0) {
            var text = length + " " + "Items Selected";
            $("#vendTdsdropdown").text(text);
            $("#vendTdsdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
        }
        if (check_box_values == 0) {
            $("#vendTdsdropdown").text("None Selected");
            $("#vendTdsdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
        }
        var value = $(elem).val();
    }
}





