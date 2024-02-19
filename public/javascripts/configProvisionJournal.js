var isDebitInstrumentDetailSet = 0;
var isCreditInstrumentDetailSet = 0;

var restPJEDatePicker = function () {
    if (COMPANY_OWNER == 'PWC') {
        $(".journalDatePicker").datepicker('destroy');
        let maximumYr = new Date().getFullYear();
        $(".journalDatePicker").datepicker({
            changeMonth: true,
            changeYear: true,
            dateFormat: 'MM d,yy',
            yearRange: '' + new Date().getFullYear() - 1 + ':' + maximumYr + '',
            /*minDate: "-1M",
            maxDate: "+0D",*/
            onSelect: function (x, y) {
                $(this).focus();
            }
        });
    } else {
        $(".journalDatePicker").datepicker({
            changeMonth: true,
            changeYear: true,
            dateFormat: 'MM d,yy',
            yearRange: '' + new Date().getFullYear() - 2 + ':' + new Date().getFullYear() + '',
            onSelect: function (x, y) {
                $(this).focus();
            }
        });
    }
}

function resetTransactionPage(isDebitBranch) {
    if (isDebitBranch === "true") {
        $("#mtefpedebitAmount").val("");
        $("#mtefpecreditAmount").val("");
        //$(".dynmBnchBankActList").remove();
        $("#mtefpetrid button[id='debitAccountHeadsdropdown']").html("None Selected &nbsp;&nbsp;<span class='caret'></span>");
        $("#debitAccountHeadsList li[id='debitaccountheadlist'] input[type='checkbox'][name='debitaccountHeadsRadio']").attr('checked', false);
        $("#mtefpetrid button[id='creditAccountHeadsdropdown']").html("None Selected &nbsp;&nbsp;<span class='caret'></span>");
        $("#creditAccountHeadsList li[id='creditaccountheadlist'] input[type='checkbox'][name='creditaccountHeadsRadio']").attr('checked', false);
    } else {
        let creditBnchSelected = $("#mtefpeTxnForBranchesCredit").val();
        let debitBnchSelected = $("#mtefpeTxnForBranchesDebit").val();
        /* both branch can be same hence commented
        if(creditBnchSelected == debitBnchSelected){
            alert("Credit Branch cannot be same as debit, please select different branch.");
            $("#mtefpeTxnForBranchesCredit").val("");
        } */
        $("#mtefpecreditAmount").val("");
        //$(".dynmBnchBankActList").remove();
        $("#mtefpetrid button[id='creditAccountHeadsdropdown']").html("None Selected &nbsp;&nbsp;<span class='caret'></span>");
        $("#creditAccountHeadsList li[id='creditaccountheadlist'] input[type='checkbox'][name='creditaccountHeadsRadio']").attr('checked', false);
    }
}

//provision logic start
function onSelectionAlertForReversal(elem) {
    let value = $(elem).val();
    if (value == "1") {
        $(".alertForReversalRequiredDateDiv").show();
        $("#alertForReversalRequiredDateOfReversal").val("");
    } else {
        $(".alertForReversalRequiredDateDiv").hide();
        $("#alertForReversalRequiredDateOfReversal").val("");
    }
}

//function to populate chart of accounts in trial balance format start
function showChartofAccountsInProvisionalAndJournalEntry(rowId, elem, isDebitBranch) {
    let selectedBranchID = $(elem).find('option:selected').val();
    let jsonData = {};
    jsonData.email = $("#hiddenuseremail").text();
    jsonData.tableRowId = rowId;
    jsonData.txnBranchID = selectedBranchID;
    if (isDebitBranch == "true") {
        ajaxCall('/data/getCoaForBranch', jsonData, '', '', '', '', 'showDebitChartofAccountsInProvisionalAndJournalEntrySuccess', '', true);
    } else {
        ajaxCall('/data/getCoaForBranch', jsonData, '', '', '', '', 'showCreditChartofAccountsInProvisionalAndJournalEntrySuccess', '', true);
    }
}

function showDebitChartofAccountsInProvisionalAndJournalEntrySuccess(data) {
    let tableRowId = data.tableRowIdData[0].tableRowId;
    $("#" + tableRowId + " #debitAccountHeadsList").children().remove();
    if (data.incomeresult) {
        $("#" + tableRowId + " #debitAccountHeadsList").append('<li id="debitaccountheadlisthead" class="accountheadlistheadIncomes"><b id="debitaccountHeadsLabelId">Incomes</b></li>');
        for (let i = 0; i < data.incomeCOAData.length; i++) {
            $("#" + tableRowId + " #debitAccountHeadsList").append('<li id="debitaccountheadlist" class="accountlistIncomes"><input type="checkbox" name="debitaccountHeadsRadio" id="debitaccountheadlistRadioId" value="' + data.incomeCOAData[i].id + '" class="incomes" onclick="checkUncheckListItem(this,debitAccountHeadsdropdown, mtefpedebitAmount); showAccountHeadDebitButtonLebel(this,\'' + data.incomeCOAData[i].name + '\');"/><b id="debitaccountHeadsLabelId">' + data.incomeCOAData[i].name + '</b><input type="text" placeholder="Debit Amount" name="mtefpedebitAmount" id="mtefpedebitAmount" item="' + data.incomeCOAData[i].id + '" onkeypress="return onlyDotsAndNumbers(event);" onblur="calculteDebitAccount(this);"></li>');
        }
    }

    if (data.expenseresult) {
        $("#" + tableRowId + " #debitAccountHeadsList").append('<li id="debitaccountheadlisthead" class="accountheadlistheadExpenses"><b id="creditaccountHeadsLabelId">Expenses</b></li>');
        for (let i = 0; i < data.expenseCOAData.length; i++) {
            if (data.expenseCOAData[i].name.toUpperCase().indexOf("VENDOR OPENING BALANCE") == -1 || data.expenseCOAData[i].name.toUpperCase().indexOf("VENDOROPENING BALANCE") == -1 || data.expenseCOAData[i].name.toUpperCase().indexOf("VENDOR OPENINGBALANCE") == -1 || data.expenseCOAData[i].name.toUpperCase().indexOf("VENDOROPENINGBALANCE") == -1) {

                $("#" + tableRowId + " #debitAccountHeadsList").append('<li id="debitaccountheadlist" class="accountlistExpenses"><input type="checkbox" name="debitaccountHeadsRadio" id="debitaccountheadlistRadioId" value="' + data.expenseCOAData[i].id + '" class="expenses" onclick="checkUncheckListItem(this,debitAccountHeadsdropdown, mtefpedebitAmount); showAccountHeadDebitButtonLebel(this,\'' + data.expenseCOAData[i].name + '\');"/><b id="debitaccountHeadsLabelId">' + data.expenseCOAData[i].name + '</b><input type="text" placeholder="Debit Amount" name="mtefpedebitAmount" id="mtefpedebitAmount" item="' + data.expenseCOAData[i].id + '" onkeypress="return onlyDotsAndNumbers(event);" onblur="calculteDebitAccount(this);"></li>');
            }
        }
    }

    if (data.assetsresult) {
        $("#" + tableRowId + " #debitAccountHeadsList").append('<li id="debitaccountheadlisthead" class="accountheadlistheadAssets"><b id="creditaccountHeadsLabelId">Assets</b></li>');
        for (let i = 0; i < data.assetsCOAData.length; i++) {
            $("#" + tableRowId + " #debitAccountHeadsList").append('<li id="debitaccountheadlist" class="accountlistAssets"><input type="checkbox" name="debitaccountHeadsRadio" id="debitaccountheadlistRadioId" value="' + data.assetsCOAData[i].id + '" class="assets" onclick="checkUncheckListItem(this,debitAccountHeadsdropdown, mtefpedebitAmount); showAccountHeadDebitButtonLebel(this,\'' + data.assetsCOAData[i].name + '\');"><b id="debitaccountHeadsLabelId">' + data.assetsCOAData[i].name + '</b><input type="text" placeholder="Debit Amount" name="mtefpedebitAmount" id="mtefpedebitAmount" item="' + data.assetsCOAData[i].id + '" onkeypress="return onlyDotsAndNumbers(event)" onblur="calculteDebitAccount(this);"></li>');
        }
    }

    if (data.liabilitiesresult) {
        $("#" + tableRowId + " #debitAccountHeadsList").append('<li id="debitaccountheadlisthead" class="accountheadlistheadLiabilities"><b id="creditaccountHeadsLabelId">Liabilities</b></li>');
        for (let i = 0; i < data.liabilitiesCOAData.length; i++) {
            $("#" + tableRowId + " #debitAccountHeadsList").append('<li id="debitaccountheadlist" class="accountlistLiabilities"><input type="checkbox" name="debitaccountHeadsRadio" id="debitaccountheadlistRadioId" value="' + data.liabilitiesCOAData[i].id + '" class="liabilities" onclick="checkUncheckListItem(this,debitAccountHeadsdropdown, mtefpedebitAmount); showAccountHeadDebitButtonLebel(this,\'' + data.liabilitiesCOAData[i].name + '\');"><b id="debitaccountHeadsLabelId">' + data.liabilitiesCOAData[i].name + '</b><input type="text" placeholder="Debit Amount" name="mtefpedebitAmount" id="mtefpedebitAmount" item="' + data.liabilitiesCOAData[i].id + '" onkeypress="return onlyDotsAndNumbers(event)" onblur="calculteDebitAccount(this);"></li>');
        }
    }
}

function showCreditChartofAccountsInProvisionalAndJournalEntrySuccess(data) {
    let tableRowId = data.tableRowIdData[0].tableRowId;
    $("#" + tableRowId + " #creditAccountHeadsList").children().remove();

    if (data.incomeresult) {
        $("#" + tableRowId + " #creditAccountHeadsList").append('<li id="creditaccountheadlisthead" class="accountheadlistheadIncomes"><b id="creditaccountHeadsLabelId">Incomes</b></li>');

        for (let i = 0; i < data.incomeCOAData.length; i++) {
            $("#" + tableRowId + " #creditAccountHeadsList").append('<li id="creditaccountheadlist" class="accountlistIncomes"><input type="checkbox" name="creditaccountHeadsRadio" id="creditaccountheadlistRadioId" value="' + data.incomeCOAData[i].id + '" class="incomes" onclick="checkUncheckListItem(this, creditAccountHeadsdropdown, mtefpecreditAmount); checkDebitAccountHeadAccordingly(this,\'' + data.incomeCOAData[i].name + '\');"><b id="creditaccountHeadsLabelId">' + data.incomeCOAData[i].name + '</b><input type="text" placeholder="Credit Amount" name="mtefpecreditAmount" id="mtefpecreditAmount" item="' + data.incomeCOAData[i].id + '" onkeypress="return onlyDotsAndNumbers(event)" onblur="calculteCreditAccount();checkDebitAccountHeadAccordingly(this,\'' + data.incomeCOAData[i].name + '\');"></li>');
        }
    }

    if (data.expenseresult) {
        $("#" + tableRowId + " #creditAccountHeadsList").append('<li id="creditaccountheadlisthead" class="accountheadlistheadExpenses"><b id="creditaccountHeadsLabelId">Expenses</b></li>');
        for (let i = 0; i < data.expenseCOAData.length; i++) {
            if (data.expenseCOAData[i].name.toUpperCase().indexOf("VENDOR OPENING BALANCE") == -1 || data.expenseCOAData[i].name.toUpperCase().indexOf("VENDOROPENING BALANCE") == -1 || data.expenseCOAData[i].name.toUpperCase().indexOf("VENDOR OPENINGBALANCE") == -1 || data.expenseCOAData[i].name.toUpperCase().indexOf("VENDOROPENINGBALANCE") == -1) {
                $("#" + tableRowId + " #creditAccountHeadsList").append('<li id="creditaccountheadlist" class="accountlistExpenses"><input type="checkbox" name="creditaccountHeadsRadio" id="creditaccountheadlistRadioId" value="' + data.expenseCOAData[i].id + '" class="expenses" onclick="checkUncheckListItem(this, creditAccountHeadsdropdown, mtefpecreditAmount); checkDebitAccountHeadAccordingly(this,\'' + data.expenseCOAData[i].name + '\');"><b id="creditaccountHeadsLabelId">' + data.expenseCOAData[i].name + '</b><input type="text" placeholder="Credit Amount" name="mtefpecreditAmount" id="mtefpecreditAmount" item="' + data.expenseCOAData[i].id + '" onkeypress="return onlyDotsAndNumbers(event);" onblur="calculteCreditAccount();checkDebitAccountHeadAccordingly(this,\'' + data.expenseCOAData[i].name + '\');"></li>');
            }
        }
    }

    if (data.assetsresult) {
        $("#" + tableRowId + " #creditAccountHeadsList").append('<li id="creditaccountheadlisthead" class="accountheadlistheadAssets"><b id="creditaccountHeadsLabelId">Assets</b></li>');
        for (let i = 0; i < data.assetsCOAData.length; i++) {
            $("#" + tableRowId + " #creditAccountHeadsList").append('<li id="creditaccountheadlist" class="accountlistAssets"><input type="checkbox" name="creditaccountHeadsRadio" id="creditaccountheadlistRadioId" value="' + data.assetsCOAData[i].id + '" class="assets" onclick="checkUncheckListItem(this, creditAccountHeadsdropdown, mtefpecreditAmount); checkDebitAccountHeadAccordingly(this,\'' + data.assetsCOAData[i].name + '\');"><b id="creditaccountHeadsLabelId">' + data.assetsCOAData[i].name + '</b><input type="text" placeholder="Credit Amount" name="mtefpecreditAmount" id="mtefpecreditAmount" item="' + data.assetsCOAData[i].id + '" onkeypress="return onlyDotsAndNumbers(event)" onblur="calculteCreditAccount();checkDebitAccountHeadAccordingly(this,\'' + data.assetsCOAData[i].name + '\');"></li>');
        }
    }

    if (data.liabilitiesresult) {
        $("#" + tableRowId + " #creditAccountHeadsList").append('<li id="creditaccountheadlisthead" class="accountheadlistheadLiabilities"><b id="creditaccountHeadsLabelId">Liabilities</b></li>');
        for (let i = 0; i < data.liabilitiesCOAData.length; i++) {
            $("#" + tableRowId + " #creditAccountHeadsList").append('<li id="creditaccountheadlist" class="accountlistLiabilities"><input type="checkbox" name="creditaccountHeadsRadio" id="creditaccountheadlistRadioId" value="' + data.liabilitiesCOAData[i].id + '" class="liabilities" onclick="checkUncheckListItem(this, creditAccountHeadsdropdown, mtefpecreditAmount); checkDebitAccountHeadAccordingly(this,\'' + data.liabilitiesCOAData[i].name + '\');"><b id="creditaccountHeadsLabelId">' + data.liabilitiesCOAData[i].name + '</b><input type="text" placeholder="Credit Amount" name="mtefpecreditAmount" id="mtefpecreditAmount" item="' + data.liabilitiesCOAData[i].id + '" onkeypress="return onlyDotsAndNumbers(event)" onblur="calculteCreditAccount();checkDebitAccountHeadAccordingly(this,\'' + data.liabilitiesCOAData[i].name + '\');"></li>');
        }
    }
}

function showAccountHeadDebitButtonLebel(elem) {
    let provJournalBnchSelected = $("#mtefpeTxnForBranchesDebit").val();
    let parentTr = $(elem).closest('tr');
    $(".dynmBnchBankActList").remove();
    if (provJournalBnchSelected === "") {
        swal("Error","Please Select Debit branch for which you are creating Make Provision/Journal Entry.","error");
        $(elem).find('option:first').prop("selected", "selected");
        isDebitInstrumentDetailSet = 0;
        return false;
    }
    let txnTrID = $(elem).parent().parent().parent().parent().parent().closest('tr').attr('id');
    let headtype = parentTr.find("td .accountItems option:selected").attr('headtype');
    if (headtype === "bank") {
        $("#" + txnTrID + " > td:nth-child(3)").append('<div class="dynmBnchBankActList"></div>');
        addBankInstrumentDetail(txnTrID);
        isDebitInstrumentDetailSet = 1;
    } else {
        isDebitInstrumentDetailSet = 0;
    }
}

function checkDebitAccountHeadAccordingly(elem) {
    let creditBnchSelected = $("#mtefpeTxnForBranchesCredit").val();
    if (creditBnchSelected == "") {
        swal("Error!","Please select credit branch for which you are creating Make Provision/Journal Entry.","error");
        return false;
    }
    let parentTr = $(elem).closest('tr');
    let txnTrID = $(elem).parent().parent().parent().parent().parent().closest('tr').attr('id');
    let headtype = parentTr.find("td .accountItems option:selected").attr('headtype');
    if (headtype === "bank") {
        $(".dynmBnchBankActList").remove();
        $("#" + txnTrID + "> td:nth-child(3)").append('<div class="dynmBnchBankActList"></div>');
        addBankInstrumentDetail(txnTrID);
    } else {
        if (isDebitInstrumentDetailSet === 0) {
            $(".dynmBnchBankActList").remove();
        }
    }
    let debitAmount = $("#" + txnTrID + " input[id='totalPjeDebitAmt']").val();
    if (debitAmount === "") {
        swal("Invalid field!","Please Enter The Debit Amount First for an item.","error");
        $(elem).find('option:first').prop("selected", "selected");
        if (isDebitInstrumentDetailSet == 0) {
            $(".dynmBnchBankActList").remove();
        }
        return false;
    }
}

$(document).ready(function () {
    $('#debitAccountHeadsdropdown').click(function () {
        //Sunil var parentTr=$(this).parent().parent().parent().parent().attr('id');
        let parentTr = $(this).closest('tr').attr('id');
        let classval = $(this).attr('class');
        if (classval == "multiselect dropdown-toggle btn") {
            let newclassval = classval + " " + "open";
            $(this).attr('class', newclassval);
            let divdropdown = "opendebitAccountHeadsdropdown-menu"
            $("#" + parentTr + " div[class='debitAccountHeadsdropdown-menu']").attr('class', divdropdown);
        }
        if (classval == "multiselect dropdown-toggle btn open") {
            let newclassval = "multiselect dropdown-toggle btn";
            $(this).attr('class', newclassval);
            let divdropdown = "debitAccountHeadsdropdown-menu";
            $("#" + parentTr + " div[class='opendebitAccountHeadsdropdown-menu']").attr('class', divdropdown);
        }
    });

    $('#creditAccountHeadsdropdown').click(function () {
        //Sunil let parentTr=$(this).parent().parent().parent().parent().attr('id');
        let parentTr = $(this).closest('tr').attr('id');
        let classval = $(this).attr('class');
        if (classval == "multiselect dropdown-toggle btn") {
            let newclassval = classval + " " + "open";
            $(this).attr('class', newclassval);
            let divdropdown = "opencreditAccountHeadsdropdown-menu"
            $("#" + parentTr + " div[class='creditAccountHeadsdropdown-menu']").attr('class', divdropdown);
        }
        if (classval == "multiselect dropdown-toggle btn open") {
            let newclassval = "multiselect dropdown-toggle btn";
            $(this).attr('class', newclassval);
            let divdropdown = "creditAccountHeadsdropdown-menu";
            $("#" + parentTr + " div[class='opencreditAccountHeadsdropdown-menu']").attr('class', divdropdown);
        }
    });
});

$(document).ready(function () {
    $('#debitmtefjetridAccountHeadsdropdown').click(function () {
        let parentTr = $(this).parent().parent().parent().parent().attr('id');
        let classval = $(this).attr('class');
        if (classval == "multiselect dropdown-toggle btn") {
            let newclassval = classval + " " + "open";
            $(this).attr('class', newclassval);
            let divdropdown = "opendebitAccountHeadsdropdown-menu";
            $("#" + parentTr + " div[class='debitAccountHeadsdropdown-menu']").attr('class', divdropdown);
        }
        if (classval == "multiselect dropdown-toggle btn open") {
            let newclassval = "multiselect dropdown-toggle btn";
            $(this).attr('class', newclassval);
            let divdropdown = "debitAccountHeadsdropdown-menu";
            $("#" + parentTr + " div[class='opendebitAccountHeadsdropdown-menu']").attr('class', divdropdown);
        }
    });
});

$(document).ready(function () {
    $('#creditmtefjetridAccountHeadsdropdown').click(function () {
        let parentTr = $(this).parent().parent().parent().parent().attr('id');
        let classval = $(this).attr('class');
        if (classval == "multiselect dropdown-toggle btn") {
            let newclassval = classval + " " + "open";
            $(this).attr('class', newclassval);
            let divdropdown = "opencreditAccountHeadsdropdown-menu"
            $("#" + parentTr + " div[class='creditAccountHeadsdropdown-menu']").attr('class', divdropdown);
        }
        if (classval == "multiselect dropdown-toggle btn open") {
            let newclassval = "multiselect dropdown-toggle btn";
            $(this).attr('class', newclassval);
            let divdropdown = "creditAccountHeadsdropdown-menu";
            $("#" + parentTr + " div[class='opencreditAccountHeadsdropdown-menu']").attr('class', divdropdown);
        }
    });
});

function checkUncheckListItem(elem, dropdown, textBox) {
    let dropdownID = $(dropdown).attr('id');
    let textBoxID = $(textBox).attr('id');
    let checked = $(elem).is(':checked');
    let elementName = $(elem).attr('name');
    let check_box_values = $('input[name="' + elementName + '"]:checkbox:checked').map(function () {
        return this.value;
    }).get();
    if (check_box_values.length > 0) {
        $("#" + elementName).innerText = "Selected";
    } else {
        $("#" + elementName).innerText = "None Selected";
    }
    if (checked == true) {
        let checkvalue = $(elem).val();
        if (checkvalue == "") {
            $('input[name="' + elementName + '"]').each(function () {
                $(this).prop("checked", true);
            });
            $('input[id*="' + textBoxID + '"]').each(function () {
                if ($(this).val() == "0.0" || $(this).val() == "") {
                    $(this).val("0.0");
                }
            });
        }
        let check_box_values = $('input[name="' + elementName + '"]:checkbox:checked').map(function () {
            return this.value;
        }).get();
        let length = check_box_values.length;
        if (length > 0) {
            let text = length + " " + "Items Selected";
            $("#" + dropdownID).text(text);
            $("#" + dropdownID).append("&nbsp;&nbsp;<b class='caret'></b>");
        }
        if (check_box_values == 0) {
            $("#" + dropdownID).text("None Selected");
            $("#" + dropdownID).append("&nbsp;&nbsp;<b class='caret'></b>");
        }
    } else if (checked == false) {
        let checkvalue = $(elem).val();
        if (checkvalue == "") {
            if (confirm("Do you want to remove all your selected customer item and their discount rate!")) {
                $('input[name="' + elementName + '"]').each(function () {
                    $(this).prop("checked", false);
                });
                $('input[id*="' + textBoxID + '"]').each(function () {
                    $(this).val("0.0");
                });
            }
        }
        let check_box_values = $('input[name="' + elementName + '"]:checkbox:checked').map(function () {
            return this.value;
        }).get();
        let length = check_box_values.length;
        if (length > 0) {
            let text = length + " " + "Items Selected";
            $("#" + dropdownID).text(text);
            $("#" + dropdownID).append("&nbsp;&nbsp;<b class='caret'></b>");
        }
        if (check_box_values == 0) {
            $("#" + dropdownID).text("None Selected");
            $("#" + dropdownID).append("&nbsp;&nbsp;<b class='caret'></b>");
        }
        let value = $(elem).val();
        $("#" + textBoxID + value + "").val("0.0");
    }
}

function calculteDebitAccount(elem) {
    let debitAmountList = 0;
    $('input[name="mtefpedebitAmount"]').map(function () {
        if ($(this).val().trim() != "") {
            debitAmountList += parseFloat(this.value);
            let item = $(this).attr('item');
            $('#debitAccountHeadsList input[value="' + item + '"]:checkbox').prop("checked", true);
        } else {
            let item = $(this).attr('item');
            $('#debitAccountHeadsList input[value="' + item + '"]:checkbox').prop("checked", false);
        }
    }).get();
    debitAmountList = debitAmountList.toFixed(2);
    $("#totalDebitAmountDiv").remove();
    $("#transactionDetailsMTEFPETable tr[id='mtefpetrid'] td:nth-child(2)").append("<div id='totalDebitAmountDiv'>Total Debit Amount&nbsp;&nbsp;<input id='totalDebitAmount' type='text' value='" + debitAmountList + "' readonly='readonly'></div>");
    let check_box_values = $('#debitAccountHeadsList input[name="debitaccountHeadsRadio"]:checkbox:checked').map(function () {
        return this.value;
    }).get();
    let length = check_box_values.length;
    if (length > 0) {
        let text = length + " " + "Items Selected";
        $("#debitAccountHeadsdropdown").text(text);
        $("#debitAccountHeadsdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
    }
    if (check_box_values == 0) {
        $("#debitAccountHeadsdropdown").text("None Selected");
        $("#debitAccountHeadsdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
    }
}

function calculteCreditAccount() {
    let creditAmountList = 0;
    $('input[name="mtefpecreditAmount"]').map(function () {
        if ($(this).val().trim() != "") {
            creditAmountList += parseFloat(this.value);
            let item = $(this).attr('item');
            $('#creditAccountHeadsList input[value="' + item + '"]:checkbox').prop("checked", true);
        } else {
            let item = $(this).attr('item');
            $('#creditAccountHeadsList input[value="' + item + '"]:checkbox').prop("checked", false);
        }
    }).get();
    creditAmountList = creditAmountList.toFixed(2);
    $("#totalCreditAmountPara").remove();
    $("#totalDebitAmountDiv").append("<p id='totalCreditAmountPara'>Total Credit Amount <input id='totalCreditAmount' type='text' value='" + creditAmountList + "' readonly='readonly'></p>");
    let check_box_values = $('#creditAccountHeadsList input[name="creditaccountHeadsRadio"]:checkbox:checked').map(function () {
        return this.value;
    }).get();
    let length = check_box_values.length;
    if (length > 0) {
        let text = length + " " + "Items Selected";
        $("#creditAccountHeadsdropdown").text(text);
        $("#creditAccountHeadsdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
    }
    if (check_box_values == 0) {
        $("#creditAccountHeadsdropdown").text("None Selected");
        $("#creditAccountHeadsdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
    }
}

function submitForApprovalProvisionJournal(whatYouWantToDo, whatYouWantToDoVal, parentTr) {
    
    let purpose = "";
    let mtefpeRemarks = "";
    let supportingDoc = "";
    let debitBranchId = $("#" + parentTr + " select[name='mtefpeTxnForBranchesDebit'] option:selected").val();
    if (debitBranchId == ""){
        swal("Error on approval!", "Please Select Debit Branch", "error");
        enableTransactionButtons();
        return false;
    }
    let creditBranchId = $("#" + parentTr + " select[name='mtefpeTxnForBranchesCredit'] option:selected").val();

    let debitHeadList = convertMultiItemsPjeDataToArray('debitAccountHeadTable');
    let creditHeadList = convertMultiItemsPjeDataToArray('creditAccountHeadTable');
    if(debitHeadList == "" || creditHeadList == ""){
        swal("Error on approval!", "Please check GL selected & amount", "error");
        enableTransactionButtons();
        return false;
    }

    let alertForReversalRequired = "";
    if (whatYouWantToDo == "Make Provision/Journal Entry") {
        purpose = $("#mtefpePurpose").val();
        mtefpeRemarks = $("#mtefpeRemarks").val();
        alertForReversalRequired = $("#alertForReversalRequired option:selected").val();
    }
    let supportingDocTmp = $("#" + parentTr + " select[class='txnUploadSuppDocs'] option").map(function () {
        if ($(this).val() != "") {
            return $(this).val();
        }
    }).get();
    supportingDoc = supportingDocTmp.join(',');
    if (purpose == "") {
		swal("Error on approval!", "Please Enter The Purpose For Provision/Journal Entry.", "error");
        enableTransactionButtons();
        return false;
    }

    let transactionDate = $("#transactionDate").val();
    if(transactionDate == "" || transactionDate === "undefined"){
        swal("Error on approval!", "Transaction Date cannot be empty.", "error");
        enableTransactionButtons();
        return false;
    }

    let instrumentNumber = $("#" + parentTr + " input[name='txtInstrumentNumber']").val();
    let instrumentDate = $("#" + parentTr + " input[name='txtInstrumentDate']").val();
    if (instrumentNumber == "") {
		swal("Error on approval!", "Instrument Number cannot be empty.", "error");
        enableTransactionButtons();
        return false;
    }
    if (instrumentDate == "") {
		swal("Error on approval!", "Instrument Date cannot be empty.", "error");
        enableTransactionButtons();
        return false;
    }

    let totalDebitAmount = $("#ttlPjeDebitAmt").val();
    let totalCreditAmount = $("#ttlPjeCreditAmt").val();
    if (totalDebitAmount != totalCreditAmount) {
		swal("Error on approval!", "Amount for Debit and Credit should be same, please adjust the amount.", "error");
        enableTransactionButtons();
        return false;
    }

	let pjeCreditRoundOff = $("#pjeCreditRoundOff").val();
	let pjeDebitRoundOff = $("#pjeDebitRoundOff").val();
    if (pjeCreditRoundOff > 1 || pjeDebitRoundOff > 1) {
		swal("Error on approval!", "Rounded Off amount is greater than 1, please adjust the amount.", "error");
        enableTransactionButtons();
        return false;
    }

    let txnJsonData = {};
    txnJsonData.txnPurpose = whatYouWantToDo;
    txnJsonData.txnPurposeVal = whatYouWantToDoVal;
    txnJsonData.txnDebitBranchId = debitBranchId;
    txnJsonData.txnCreditBranchId = creditBranchId;
    txnJsonData.debitHeadList = debitHeadList;
    txnJsonData.creditHeadList = creditHeadList;
    txnJsonData.txnTotalDebitAmount = totalDebitAmount;
    txnJsonData.txnTotalCreditAmount = totalCreditAmount;
    txnJsonData.txnpurpose = purpose;
    txnJsonData.txnmtefpeRemarks = mtefpeRemarks;
    txnJsonData.supportingdoc = supportingDoc;
    txnJsonData.txnalertForReversalRequired = alertForReversalRequired;
    txnJsonData.transactionDate = transactionDate;
    txnJsonData.txnDebitRoundOff = pjeDebitRoundOff;
    txnJsonData.txnCreditRoundOff = pjeCreditRoundOff;
    if (alertForReversalRequired == "1") {
        let alertForReversalRequiredDateOfReversal = $("#alertForReversalRequiredDateOfReversal").val();
        if (alertForReversalRequiredDateOfReversal != "") {
            txnJsonData.txnalertForReversalRequiredDateOfReversal = alertForReversalRequiredDateOfReversal;
        }
    }
    txnJsonData.useremail = $("#hiddenuseremail").text();
    txnJsonData.txnInstrumentNum = instrumentNumber;
    txnJsonData.txnInstrumentDate = instrumentDate;

    let url = "/transaction/submitForApproval";
    $.ajax({
        url: url,
        data: JSON.stringify(txnJsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        method: "POST",
        contentType: 'application/json',
        success: function (data) {
            if (typeof data.message !== 'undefined' && data.message != "") {
                swal("Error!", data.message, "error");
                enableTransactionButtons();
                return false;
            }
            if (data.validTransactionDate == 0) {
                $(".btn-custom").removeAttr("disabled");
                $(".btn-customred").removeAttr("disabled");
                $(".approverAction").removeAttr("disabled");
                $("#completeTxn").removeAttr("disabled");
                swal("Error on transaction date!", "Transaction date is out of financial date range or Financial year date is not set.", "error");
                return false;
            }
            approvealForSingleUser(data);	// Single User
            cancel();
            //getUserTransactions(2, 100);
            viewTransactionData(data); // to render the updated transaction recored
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on submit for approval!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
            enableTransactionButtons();
        }
    });
}

function provisionapproverAction(parentTr) {
    //alert(">>>>>15"); //sunil
    disableTransactionButtons();
    $(".btn-custom").attr("disabled", "disabled");
    $(".btn-customred").attr("disabled", "disabled");
    $(".approverAction").attr("disabled", "disabled");
    $("#completeTxn").attr("disabled", "disabled");
    let selectedAction = $("#" + parentTr + " select[id='approverActionList'] option:selected").val();
    let transactionEntityId = parentTr.substring(26, parentTr.length);
    let supportingDocTmp = $("#" + parentTr + " select[name='txnViewListUpload'] option").map(function () {
        if ($(this).val() != "") {
            return $(this).val();
        }
    }).get();
    let supportingDoc = supportingDocTmp.join(',');
    let remarks = $("#" + parentTr + " textarea[id='txnRemarks']").val();
    let selectedAddApproverVal = "";
    if (selectedAction == "") {
        swal("Error!","Please choose your next action from the Approver action list","error");
        $(".btn-custom").removeAttr("disabled");
        $(".btn-customred").removeAttr("disabled");
        $(".approverAction").removeAttr("disabled");
        $("#completeTxn").removeAttr("disabled");
        enableTransactionButtons();
        return true;
    }
    if (selectedAction == "3") {
        selectedAddApproverVal = $("#" + parentTr + " select[id='userAddApproval'] option:selected").val();
        if (selectedAddApproverVal == "") {
            swal("Error!","Please choose the user to whom you want to send for additional approval","error");
            $(".btn-custom").removeAttr("disabled");
            $(".btn-customred").removeAttr("disabled");
            $(".approverAction").removeAttr("disabled");
            $("#completeTxn").removeAttr("disabled");
            enableTransactionButtons();
            return true;
        } else {
            let txnJsonData = {};
            txnJsonData.useremail = $("#hiddenuseremail").text();
            txnJsonData.selectedApproverAction = selectedAction;
            txnJsonData.transactionPrimId = transactionEntityId;
            txnJsonData.selectedAddApproverEmail = selectedAddApproverVal;
            txnJsonData.suppDoc = supportingDoc;
            txnJsonData.txnRmarks = remarks;
            let url = "/transactionProvision/approverAction";
            $.ajax({
                url: url,
                data: JSON.stringify(txnJsonData),
                type: "text",
                headers: {
                    "X-AUTH-TOKEN": window.authToken
                },
                method: "POST",
                contentType: 'application/json',
                success: function (data) {
                    if (data.resultantAmount < 0) {
                        swal("Error!","Insufficient balance in the bank account. Use alternative payment mode or infuse funds into the bank account. Effective Bank Balance is: " + data.resultantAmount,"error");
                    }
                    if (data.resultantCash < 0) {
                        swal("Error!","Insufficient balance in the cash account. Use alternative payment mode or infuse funds into the cash account. Effective cash balance is: " + data.resultantCash,"error");
                    }

                    if (data.resultantPettyCashAmount < 0) {
                        swal("Error!","Insufficient balance in the petty cash account. Use alternative payment mode or infuse funds into petty cash account. Effective petty cash balance is: " + data.resultantPettyCashAmount,"error");
                    }
                    if (typeof data.message !== 'undefined' && data.message != "") {
                        swal("Error!", data.message, "error");
                    }
                    viewTransactionData(data)
                },
                error: function (xhr, status, error) {
                    if (xhr.status == 401) {
                        doLogout();
                    } else if (xhr.status == 500) {
                        swal("Error!", "Please retry, if persists contact support team", "error");
                    }

                },
                complete: function (data) {
                    enableTransactionButtons();
                }
            });
        }
    } else {
        //send server for action to complete
        let txnJsonData = {};
        txnJsonData.useremail = $("#hiddenuseremail").text();
        txnJsonData.selectedApproverAction = selectedAction;
        txnJsonData.transactionPrimId = transactionEntityId;
        txnJsonData.selectedAddApproverEmail = selectedAddApproverVal;
        txnJsonData.suppDoc = supportingDoc;
        txnJsonData.txnRmarks = remarks;
        let url = "/transactionProvision/approverAction";
        $.ajax({
            url: url,
            data: JSON.stringify(txnJsonData),
            type: "text",
            headers: {
                "X-AUTH-TOKEN": window.authToken
            },
            method: "POST",
            contentType: 'application/json',
            success: function (data) {
                if (data.resultantAmount < 0) {
                    swal("Error!","Insufficient balance in the bank account. Use alternative payment mode or infuse funds into the bank account. Effective Bank Balance is: " + data.resultantAmount,"error");
                }
                if (data.resultantCash < 0) {
                    swal("Error!","Insufficient balance in the cash account. Use alternative payment mode or infuse funds into the cash account. Effective cash balance is: " + data.resultantCash,"error");
                }
                if (data.resultantPettyCashAmount < 0) {
                    swal("Error!","Insufficient balance in the petty cash account. Use alternative payment mode or infuse funds into petty cash account. Effective petty cash balance is: " + data.resultantPettyCashAmount,"error");
                }
                if (typeof data.message !== 'undefined') {
                    swal("Error!", data.message, "error");
                }
                viewTransactionData(data)
            },
            error: function (xhr, status, error) {
                if (xhr.status == 401) {
                    doLogout();
                } else if (xhr.status == 500) {
                    swal("Error!", "Please retry, if persists contact support team", "error");
                }

            },
            complete: function (data) {
                enableTransactionButtons();
            }
        });
    }
}

function completeProvisionAccounting(elem) {
    disableTransactionButtons();
    let parentTr = $(elem).closest('tr').attr('id');
    let selectedAction = "4";
    let transactionEntityId = parentTr.substring(26, parentTr.length);
    let supportingDocTmp = $("#" + parentTr + " select[name='txnViewListUpload'] option").map(function () {
        if ($(this).val() != "") {
            return $(this).val();
        }
    }).get();
    let supportingDoc = supportingDocTmp.join(',');
    let remarks = $("#" + parentTr + " textarea[id='txnRemarks']").val();
    let paymentOption = $("#" + parentTr + " select[id='paymentDetails'] option:selected").val();
    let bankDetails = $("#" + parentTr + " textarea[id='bankDetails']").val();
    let transactionInvoiceDate = $("#" + parentTr + " input[name='vendorInvoiceDate']").val();
    let selectedAddApproverVal = "";
    let paymentBank = "";
    let txnJsonData = {};
    txnJsonData.useremail = $("#hiddenuseremail").text();
    txnJsonData.selectedApproverAction = selectedAction;
    txnJsonData.transactionPrimId = transactionEntityId;
    txnJsonData.selectedAddApproverEmail = selectedAddApproverVal;
    txnJsonData.suppDoc = supportingDoc;
    txnJsonData.txnRmarks = remarks;
    txnJsonData.txnInvDate = transactionInvoiceDate;
    txnJsonData.paymentDetails = paymentOption;
    if (paymentOption == "2") {
        paymentBank = $("#availableBank option:selected").val();
        if (typeof paymentBank != 'undefined') {
            txnJsonData.txnPaymentBank = paymentBank;
        }
    }
    txnJsonData.bankInf = bankDetails;
    let url = "/transactionProvision/approverAction";
    $.ajax({
        url: url,
        data: JSON.stringify(txnJsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        method: "POST",
        contentType: 'application/json',
        success: function (data) {
            if (data.resultantAmount < 0) {
                if (data.branchBankDetailEntered === false) {
                    swal("Insufficient balance in the bank account!", "Use alternative payment mode or infuse funds into the bank account. Current bank balance is:" + data.resultantAmount, "error");
                    disableTransactionButtons();
                    return false;
                } else {
                    swal("Bank Balance is in negative!", "This bank account type allows -ve balance so transaction is successful. Note current bank balance is:" + data.resultantAmount, "warning");
                }
            }
            if (data.resultantCash < 0) {
                swal("Insufficient balance in the cash account!", "Use alternative payment mode or infuse funds into the cash account. Effective cash balance is: " + data.resultantCash, "warning");
            }

            if (data.resultantPettyCashAmount < 0) {
                swal("Insufficient balance in the petty cash account!", "Use alternative payment mode or infuse funds into petty cash account. Effective petty cash balance is: " + data.resultantPettyCashAmount, "warning");
            }
            if (typeof data.message !== 'undefined') {
                swal("Error!", data.message, "error");
            }
            viewTransactionData(data)
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on Complete Accounting!", "Please retry, if persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
            enableTransactionButtons();
        }
    });
}

/*----------------------Start new change -------------------------------------------------*/

var addDebitCreditBranchItems = function (elem) {
    // called after click add Button
    let parentId = $(elem).closest('div').attr('id');
    let rowId = $("#" + parentId).closest('tr').attr('id');

    let countTr = $(elem).closest('div').find("table > tbody tr").length;
    if (countTr > 0) {
        let lastTr = $(elem).closest('div').find("table > tbody").last();
        //if($.trim(lastTr.find('.accountUnit').val()) == "" || $.trim(lastTr.find('.accountPrice').val()) == "" || $.trim(lastTr.find('.accountTotalAmt').val()) == "") {
        if ($.trim(lastTr.find('.netAmountVal').val()) == "") {
            swal("Incomplete data!", "Please fill all fields.", "error");
            return false;
        }
    }
    let selectedBranchID = $(elem).closest('td').find('select option:selected').val();
    if (selectedBranchID == "") {
        swal("Insufficiant Data!", "Please Select a Branch.", "error");
        return false;
    }
    getItemsAndProjectDataForAccountHeadsSelect(rowId, selectedBranchID, parentId, countTr);
}

var removeDebitCreditBranchItems = function (elem) {
    let parentId = $(elem).closest('div').attr('id');
    let tableId = $("#" + parentId).find('table').attr('id');
    if (validateRemovalTr(parentId)) {
        $("#" + parentId).find(".removeCheckBox:checkbox:checked").each(function () {
            $(this).closest('tr').remove();
        });
        calculateGrandTotal(tableId);
    }
}

function getItemsAndProjectDataForAccountHeadsSelect(rowId, selectedBranchID, parentId, countTr) {
    let resultData = {};
    resultData.itemsString = "";
    resultData.projectString = "";
    if (selectedBranchID !== "") {
        $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
        let jsonData = {};
        jsonData.email = $("#hiddenuseremail").text();
        jsonData.tableRowId = rowId;
        jsonData.txnBranchID = selectedBranchID;
        jsonData.projectReq = "true";
        let url = "/data/coaUserBranch";
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
                let string = [];
                let projectsSring = [];
                let i = 0, j = 0;
                if (data.incomeCOAData) {
                    string[i++] = '<optgroup class="green" label="Income">';
                    for (let index in data.incomeCOAData) {
                        string[i++] = '<option value="';
                        string[i++] = data.incomeCOAData[index].id;
                        string[i++] = '" inventory="';
                        string[i++] = data.incomeCOAData[index].isinventory;
                        string[i++] = '" headType="';
                        string[i++] = data.incomeCOAData[index].headType;
                        string[i++] = '" >';
                        string[i++] = data.incomeCOAData[index].name;
                        string[i++] = '</option>';
                    }
                    string[i++] = '</optgroup>';
                }
                if (data.expenseCOAData) {
                    string[i++] = '<optgroup class="blue" label="Expense">';
                    for (let index in data.expenseCOAData) {
                        string[i++] = '<option style="color:blue;" value="';
                        string[i++] = data.expenseCOAData[index].id;
                        string[i++] = '" inventory="';
                        string[i++] = data.expenseCOAData[index].isinventory;
                        string[i++] = '" headType="';
                        string[i++] = data.expenseCOAData[index].headType;
                        string[i++] = '" >';
                        string[i++] = data.expenseCOAData[index].name;
                        string[i++] = '</option>';
                    }
                    string[i++] = '</optgroup>';
                }
                if (data.assetsCOAData) {
                    string[i++] = '<optgroup label="Assets">';
                    for (let index in data.assetsCOAData) {
                        string[i++] = '<option value="';
                        string[i++] = data.assetsCOAData[index].id;
                        string[i++] = '" inventory="';
                        string[i++] = data.assetsCOAData[index].isinventory;
                        string[i++] = '" headType="';
                        string[i++] = data.assetsCOAData[index].headType;
                        string[i++] = '" >';
                        string[i++] = data.assetsCOAData[index].name;
                        string[i++] = '</option>';
                    }
                    string[i++] = '</optgroup>';
                }
                if (data.liabilitiesCOAData) {
                    string[i++] = '<optgroup label="Liabilities">';
                    for (let index in data.liabilitiesCOAData) {
                        string[i++] = '<option value="';
                        string[i++] = data.liabilitiesCOAData[index].id;
                        string[i++] = '" inventory="';
                        string[i++] = data.liabilitiesCOAData[index].isinventory;
                        string[i++] = '" headType="';
                        string[i++] = data.liabilitiesCOAData[index].headType;
                        string[i++] = '" >';
                        string[i++] = data.liabilitiesCOAData[index].name;
                        string[i++] = '</option>';
                    }
                    string[i++] = '</optgroup>';
                }

                if (data.projectData) {
                    for (let index in data.projectData) {
                        projectsSring[j++] = '<option value="';
                        projectsSring[j++] = data.projectData[index].id;
                        projectsSring[j++] = '" >';
                        projectsSring[j++] = data.projectData[index].name;
                        projectsSring[j++] = '</option>';
                    }
                }
                resultData.itemsString = string.join('');
                resultData.projectString = projectsSring.join('');
                let itemRow = "";
                if (parentId === "debitaccountHeads") {
                    itemRow = getDebitAccountHeadItemRow(resultData, countTr);
                } else if (parentId === "creditaccountHeads") {
                    itemRow = getCreditAccountHeadItemRow(resultData, countTr);
                } else {
                    itemRow = "";
                }
                if (itemRow != "") {
                    $("#" + parentId).find("table > tbody").append(itemRow);
                }

            },
            error: function (xhr, status, error) {
                if (xhr.status === 401) {
                    doLogout();
                } else if (xhr.status === 500) {
                    swal("Error on fetching items!", "Please retry, if problem persists contact support team", "error");
                }
            },
            complete: function (data) {
                $.unblockUI();
                bindSelect("accountItems");
                return resultData;

            }
        });
    } else {
        return resultData;
    }
}

function branchChangeForProvisionalAndJournalEntry(elem) {
    let selectedBranchID = $(elem).find('option:selected').val();
    let rowId = $(elem).closest('tr').attr('id');
    if (selectedBranchID == "") {
        swal("Insufficiant Data!", "Please Select Branch First !!", "error");
        return false;
    }
    let parentId = $(elem).closest('td').attr('id');
    $("#" + parentId).find("table > tbody").html("");
    $("#" + parentId).find(".addnewItemAccountHead").click();
}

function getDebitAccountHeadItemRow(resultData, countTr) {
    let debitItems = "";
    let debitProjects = "";
    let debitRow = [];
    let i = 0;
    debitRow[i++] = '<tr id="mtefpd' + countTr + '"><td><select class="accountItems"  name="debitAccountItems" id="debitAccountItems" onChange="onPjeItemSelection(this); showAccountHeadDebitButtonLebel(this);"><option value="">Please Select</option>';
    debitRow[i++] = resultData.itemsString;
    debitRow[i++] = '</select></td><td><input class="accountUnit" placeholder="Units" style="width:60px; display: none;" type="text" name="debitAccountUnit" id="debitAccountUnit" onkeyup="calculateTotalProvisionalAndJournalEntryItem(this);" onkeypress="return onlyDotsAndNumbers(event);" /></td>';
    debitRow[i++] = '<td><input class="accountPrice" placeholder="Price Per Unit" style="width:60px; display: none;" type="text" name="debitAccountPrice" id="debitAccountPrice" onkeyup="calculateTotalProvisionalAndJournalEntryItem(this);" onkeypress="return onlyDotsAndNumbers(event);" /></td>';
    debitRow[i++] = '<td><input class="netAmountVal" placeholder="Total Amount" style="width:90px;" type="text" name="debitAccountTotalAmt" id="debitAccountTotalAmt" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateTotalProvisionalAndJournalEntryItem(this);"/></td>';
    debitRow[i++] = '<td><select class="accountProject"  name="debitAccountProject" id="debitAccountProject" onChange=""><option value="">Please Select</option>';
    debitRow[i++] = resultData.projectString;
    debitRow[i++] = '</select></td>';
    debitRow[i++] = '<td><input type="checkbox" class="removeCheckBox"/></td></tr>';
    return debitRow.join('');
}

function getCreditAccountHeadItemRow(resultData, countTr) {
    let creditItems = "";
    let creditProject = "";
    let creditRow = [];
    let i = 0;
    creditRow[i++] = '<tr id="mtefpc' + countTr + '"><td><select class="accountItems"  name="creditAccountItems" id="creditAccountItems" onChange="onPjeItemSelection(this); checkDebitAccountHeadAccordingly(this);"><option value="">Please Select</option>';
    creditRow[i++] = resultData.itemsString;
    creditRow[i++] = '</select></td><td><input class="accountUnit" placeholder="Units" style="width:60px; display: none;" type="text" name="creditAccountUnit" id="creditAccountUnit" onkeyup="calculateTotalProvisionalAndJournalEntryItem(this);" onkeypress="return onlyDotsAndNumbers(event);" /></td>';
    creditRow[i++] = '<td><input class="accountPrice" placeholder="Price Per Unit" style="width:60px; display: none;" type="text" name="creditAccountPrice" id="creditAccountPrice" onkeyup="calculateTotalProvisionalAndJournalEntryItem(this);" onkeypress="return onlyDotsAndNumbers(event);" /></td>';
    creditRow[i++] = '<td><input class="netAmountVal" placeholder="Total Amount" style="width:90px;" type="text" name="creditAccountTotalAmt" id="creditAccountTotalAmt"  onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateTotalProvisionalAndJournalEntryItem(this);"/></td>';
    creditRow[i++] = '<td><select class="accountProject" name="creditAccountProject" id="creditAccountProject" onChange=""><option value="">Please Select</option>';
    creditRow[i++] = resultData.projectString;
    creditRow[i++] = '</select></td>';
    creditRow[i++] = '<td><input type="checkbox" class="removeCheckBox"/></td></tr>';
    return creditRow.join('');
}

var onPjeItemSelection = function (elem) {
    let parentTr = $(elem).closest('tr');
    let isInventory = parentTr.find("td .accountItems option:selected").attr('inventory');
    if (isInventory === "1") {
        parentTr.find('.accountUnit').show();
        parentTr.find('.accountPrice').show();
        parentTr.find('.netAmountVal').attr("disabled", "disabled");
    } else {
        parentTr.find('.accountUnit').hide();
        parentTr.find('.accountPrice').hide();
        parentTr.find('.netAmountVal').removeAttr("disabled");
    }
}

function calculateTotalProvisionalAndJournalEntryItem(elem) {
    let parentTr = $(elem).closest('tr');
    let isInventory = parentTr.find("td .accountItems option:selected").attr('inventory');
    let tableId = $(elem).closest('table').attr("id");
    let units = $.trim(parentTr.find('.accountUnit').val());
    let price = $.trim(parentTr.find('.accountPrice').val());
    let itemId = $.trim(parentTr.find('.accountItems').val());
    if (itemId == "") {
        swal("Insufficient Data!", "Please Select Item First !!", "error");
        return false;
    }
    if (isInventory === "1") {
        if ($.isNumeric(units) && $.isNumeric(price)) {
            units = Number(units);
            price = Number(price);
            let total = price * units;
            //parentTr.find('.netAmountVal').val(total.toFixed(2));

            let amountDbl = Math.round(total * 100.00) / 100.00;
            parentTr.find('.netAmountVal').val(amountDbl);
        } else {
            parentTr.find('.netAmountVal').val("");
        }
    }
    calculateGrandTotal(tableId);
    let totalCredit = $("#totalPjeCreditAmt").val();
    let totalDebit = $("#totalPjeDebitAmt").val();
    if ((Number(totalDebit) > Number(totalCredit)) && (totalDebit !== "" && totalCredit !== "")) {
        let pjeDebitRoundOff = Number(totalDebit) - Number(totalCredit);
        pjeDebitRoundOff = Math.round(pjeDebitRoundOff * 100.00) / 100.00;
        $("#pjeCreditRoundOff").val(pjeDebitRoundOff);
        $("#pjeDebitRoundOff").val(0.0);
        totalCredit = Number(totalCredit) + Number(pjeDebitRoundOff);
    } else if ((Number(totalCredit) > Number(totalDebit)) && (totalDebit !== "" && totalCredit !== "")) {
        let pjeCreditRoundOff = Number(totalCredit) - Number(totalDebit);
        pjeCreditRoundOff = Math.round(pjeCreditRoundOff * 100.00) / 100.00;
        $("#pjeDebitRoundOff").val(pjeCreditRoundOff);
        $("#pjeCreditRoundOff").val(0.0);
        totalDebit = Number(totalDebit) + Number(pjeCreditRoundOff);
    }else if ((Number(totalCredit) === Number(totalDebit)) && (totalDebit !== "" && totalCredit !== "")) {
        let pjeCreditRoundOff = Number(totalCredit) - Number(totalDebit);
        pjeCreditRoundOff = Math.round(pjeCreditRoundOff * 100.00) / 100.00;
        $("#pjeDebitRoundOff").val(0.0);
        $("#pjeCreditRoundOff").val(0.0);
        totalDebit = Number(totalDebit) + Number(pjeCreditRoundOff);
    }
    $("#ttlPjeDebitAmt").val(totalDebit);
    $("#ttlPjeCreditAmt").val(totalCredit);

}

/*function calculateGrandTotal(tableId) {
    let grandTotal = 0;
    $("#"+tableId).find('.grandTotalAmt').val("");
    $("#"+tableId).find(".netAmountVal").each(function(){
        let total = Number($.trim($(this).val()));
        if($.isNumeric(total)) {
            grandTotal += total;
        }
    });
    $("#"+tableId).find('.grandTotalAmt').val(grandTotal);
}*/


var convertMultiItemsPjeDataToArray = function (tableName) {
    let parentTable = $("#" + tableName).parents().closest('table').attr('id');
    let multipleItemsData = [];
    $("#" + tableName + " > tbody > tr").each(function () {
        let jsonData = {};
        let itemId = $(this).find("td .accountItems option:selected").val();
        let grossAmt = $(this).find("td input[class='netAmountVal']").val();
        if (grossAmt == 0) {
            return false;
        }
        if (itemId !== "" && typeof itemId !== 'undefined' && grossAmt !== "" && typeof grossAmt !== 'undefined') {
            jsonData.txnItems = itemId;
            let headType = $(this).find("td .accountItems option:selected").attr('headType');
            jsonData.headType = headType;
            jsonData.txnNoOfUnit = $(this).find("td input[class='accountUnit']").val();
            jsonData.txnUnitPrice = $(this).find("td input[class='accountPrice']").val();
            jsonData.headTotalAmt = grossAmt;
            jsonData.projectid = $(this).find("td select[class='accountProject'] option:selected").val();
            multipleItemsData.push(JSON.stringify(jsonData));
        }
    });
    return multipleItemsData;
}

function debitBranchChangePJE(elem) {
    let selectedBranchID = $(elem).find('option:selected').val();
    if (selectedBranchID == "") {
        $("#mtefpeTxnForBranchesCredit").val("");
    } else {
        $("#mtefpeTxnForBranchesCredit").val(selectedBranchID);
    }
    $("#mtefpeTxnForBranchesCredit").trigger("change");
}

function getProvisionJournalTransactionDetails(elem) {
    let parentTr = $(elem).closest('tr').attr('id');
    let transactionEntityId = parentTr.substring(26, parentTr.length);
    let jsonData = {};
    let useremail = $("#hiddenuseremail").text();
    jsonData.transactionEntityId = transactionEntityId;
    $("#provisionJournallDetails").attr('data-toggle', 'modal');
    $("#provisionJournallDetails").modal('show');
    let url = "/transactionProvision/getProvisionJournalEntryDetails";
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
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
            $("#provisionJournallDetails div[class='modal-body']").html('<div class="datascrolltable" style="height: 100%; overflow: auto;">' +
                '<table align="center"><tr><td><table id="debitBranch" class="table table-hover table-striped excelFormTable transaction-create"><thead><tr><th>Debit Branch</th></tr></thead><tbody><tr><td>' + data.debitBranch + '</td></tr></tbody></table><br>' +
                '<table id="provisionJournallDebitDetails" class="table table-hover table-striped excelFormTable transaction-create"><thead><tr><th class="pjeTableItemDetailsParticularHeads">Particulars</th><th>Units</th><th>Price</th><th>Amounts</th><th class="pjeTableItemDetailsProjectHeads">Project</th></tr></thead><tbody><tr><tr/></tbody></table></td>' +
                '<td><table id="creditBranch" class="table table-hover table-striped excelFormTable transaction-create"><thead><tr><th>Credit Branch</th></tr></thead><tbody><tr><td>' + data.creditBranch + '</td></tr></tbody></table>' +
                '<table id="provisionJournallCreditDetails" class="table table-hover table-striped excelFormTable transaction-create"><thead><tr><th class="pjeTableItemDetailsParticularHeads">Particulars</th><th>Units</th><th>Price</th><th>Amounts</th><th  class="pjeTableItemDetailsProjectHeads">Project</th></tr></thead><tbody><tr><tr/></tbody></table></td></tr></table></div>');
            for (let i = 0; i < data.PjeItemDetailList.length; i++) {
                let units = "", unitPrice = "", headAmount = "", projectName = "";

                if (data.PjeItemDetailList[i].units != null)
                    units = data.PjeItemDetailList[i].units.toFixed(2);
                if (data.PjeItemDetailList[i].unitPrice != null)
                    unitPrice = data.PjeItemDetailList[i].unitPrice.toFixed(2);
                if (data.PjeItemDetailList[i].headAmount != null)
                    headAmount = data.PjeItemDetailList[i].headAmount.toFixed(2);
                if (data.PjeItemDetailList[i].projectName != null)
                    projectName = data.PjeItemDetailList[i].projectName;

                if (data.PjeItemDetailList[i].isDebit == "1") {
                    $("#provisionJournallDetails div[class='modal-body'] table[id='provisionJournallDebitDetails'] tbody").append('<tr><td>' + data.PjeItemDetailList[i].itemName + '</td>' +
                        '<td style="text-align: right">' + units + '</td>' +
                        '<td style="text-align: right">' + unitPrice + '</td>' +
                        '<td style="text-align: right;">' + headAmount + '</td>' +
                        '<td>&nbsp;&nbsp;' + projectName + '</td></tr>');
                }
                if (data.PjeItemDetailList[i].isDebit == "0") {
                    $("#provisionJournallDetails div[class='modal-body'] table[id='provisionJournallCreditDetails'] tbody").append('<tr><td>' + data.PjeItemDetailList[i].itemName + '</td>' +
                        '<td style="text-align: right">' + units + '</td>' +
                        '<td style="text-align: right">' + unitPrice + '</td>' +
                        '<td style="text-align: right">' + headAmount + '</td>' +
                        '<td>&nbsp;&nbsp;' + projectName + '</td></tr>');
                }
            }
            $("#provisionJournallDetails div[class='modal-body'] table[id='provisionJournallDebitDetails'] tbody").append('<tr><td><b>Debit Total</b></td><td></td><td></td><td style="text-align: right">' + data.debitTotalAmount + '</td><td></td></tr>');
            $("#provisionJournallDetails div[class='modal-body'] table[id='provisionJournallCreditDetails'] tbody").append('<tr><td><b>Credit Total</b></td><td></td><td></td><td style="text-align: right">' + data.creditTotalAmount + '</td><td></td></tr>');
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
            enableTransactionButtons();
        }
    });
}
