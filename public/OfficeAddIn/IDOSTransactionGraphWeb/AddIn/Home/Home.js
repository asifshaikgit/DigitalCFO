﻿var useremail = "manali1";
(function () {
    "use strict";
    // The initialize function must be run each time a new page is loaded
    Office.initialize = function (reason) {
        $(document).ready(function () {
            app.initialize();

            $('#login-Button').click(loginButton);
            $('#idosgrpahs').click(addinmenu);
            // $('#idosgrpahs').click(loginButton);
            $('.addinmenu').hide();

            if (!Office.context.requirements.isSetSupported('ExcelApi', '1.1')) {
                app.showNotification("Need Office 2016 or greater", "Sorry, this add-in only works with newer versions of Excel.");
                return;
            }

        });
    };

    function loginButton() {
        //{"userName":"manali1@myidos.com","loginpwd":"2015"}
        var dataToPassToService = {
            userName: $('#loginuser').val(),
            loginpwd: $('#pass').val()
        };

        $.ajax({
            url: 'http://www.myidos.com/transaction/loginFromAddIn',
            type: 'POST',
            crossDomain: true,
            data: JSON.stringify(dataToPassToService),
            contentType: 'application/json;charset=utf-8',
            success: function (data) {
                if (data.logincredentials[0].message == "Failure") {
                    app.showNotification('Failure', 'Could not login to IDOS using id ' + dataToPassToService.userName);
                }
                if (data.logincredentials[0].message == "Success") {
                    app.showNotification('Success', 'Welcome to ' + data.logincredentials[0].organization + ' Dashboard ' + dataToPassToService.userName);
                    useremail = dataToPassToService.userName;
                    //addinmenu();
                    $('.loginmenu').hide();
                    $('.addinmenu').show();
                }
                if (data.logincredentials[0].message == "UnauthorizedRole") {
                    app.showNotification('Failure', 'Not authorized to access IDOS using ID ' + dataToPassToService.userName);
                }
            },
            error: function (xhr, status, error) {
                app.showNotification('Error', 'Could not communicate with IDOS server.');
            }
        });
    }

    //Radio buttons select with add-in menu options
    function addinmenu() {
        var graphSelected = $('input[id="graph"]:checked').val();
        if (graphSelected == "branchwisesales") {
            populateBranchSales(useremail);
        }
        else if (graphSelected == "branchwiseexpenses") {
            populateBranchExpenses(useremail);
        }
        else if (graphSelected == "branchwisecustomeradvances") {
            populateBranchCustomerAdvances(useremail);
        }
        else if (graphSelected == "branchwisevendoradvances") {
            populateBranchVendorAdvances(useremail);
        }
    }

    //Branch wise customer sales
    function populateBranchSales(useremail) {
        var jsonData = {};
        jsonData.useremail = useremail;
        $.ajax({
            url: 'http://www.myidos.com/transaction/branchwisesales',
            data: JSON.stringify(jsonData),
            type: 'POST',
            crossDomain: true,
            contentType: 'application/json;charset=utf-8',
            success: function (data) {
                if (data.branchSalesData != null && data.branchSalesData.length > 0) {
                    var myTable = new Office.TableData();
                    myTable.headers = ["Branch", "CashSales", "CreditSales"];
                    for (var i = 0; i < data.branchSalesData.length; i++) {
                        myTable.rows[i] = [data.branchSalesData[i].branchName, data.branchSalesData[i].cashSales, data.branchSalesData[i].creditSales];
                    }
                    pouplateIDOSDataInXLS(myTable, "BranchSales", "BRANCH wise Customer Sales (All figures are in Indian Rupees (INR))");
                }
                else {
                    app.showNotification('Error', "No data is available in IDOS for this graph");
                }
            },
            error: function (xhr, status, error) {
                app.showNotification('Error', 'Could not communicate with the server.');
            }
        });
    }


    //Branch wise customer sales
    function populateBranchExpenses(useremail) {
        var jsonData = {};
        jsonData.useremail = useremail;
        $.ajax({
            url: 'http://www.myidos.com/transaction/branchwiseexpense',
            data: JSON.stringify(jsonData),
            type: 'POST',
            crossDomain: true,
            contentType: 'application/json;charset=utf-8',
            success: function (data) {
                if (data.branchExpensesData != null && data.branchExpensesData.length > 0) {
                    var myTable = new Office.TableData();
                    myTable.headers = ["Branch", "CashExpenses", "CreditExpenses"];
                    for (var i = 0; i < data.branchExpensesData.length; i++) {
                        myTable.rows[i] = [data.branchExpensesData[i].branchName, data.branchExpensesData[i].cashExpenses, data.branchExpensesData[i].creditExpenses];
                    }
                    pouplateIDOSDataInXLS(myTable, "BranchExpenses", "BRANCH wise Vendor Purchases (All figures are in Indian Rupees (INR))");
                }
                else {
                    app.showNotification('Error', "No data is available in IDOS for this graph");
                }
            },
            error: function (xhr, status, error) {
                app.showNotification('Error', 'Could not communicate with the server.');
            }
        });
    }

    //Branch wise customer advances received
    function populateBranchCustomerAdvances(useremail) {
        var jsonData = {};
        jsonData.useremail = useremail;

        $.ajax({
            url: 'http://www.myidos.com/transaction/branchwisecustomeradvance',
            data: JSON.stringify(jsonData),
            type: 'POST',
            crossDomain: true,
            contentType: 'application/json;charset=utf-8',
            success: function (data) {
                if (data.branchCustomerAdvanceReceived != null && data.branchCustomerAdvanceReceived.length > 0) {
                    var myTable = new Office.TableData();
                    myTable.headers = ["Branch", "0-30days", "30-60days", "60-90days", "90-180days"];
                    for (var i = 0; i < data.branchCustomerAdvanceReceived.length; i++) {
                        myTable.rows[i] = [data.branchCustomerAdvanceReceived[i].branchName, data.branchCustomerAdvanceReceived[i].advfor0to30days, data.branchCustomerAdvanceReceived[i].advfor30to60days, data.branchCustomerAdvanceReceived[i].advfor60to90days, data.branchCustomerAdvanceReceived[i].advfor90to180days];
                    }
                    pouplateIDOSDataInXLS(myTable, "CustomerAdvances", "BRANCH wise Customer Advances Paid (All figures are in Indian Rupees (INR))");
                }
                else {
                    app.showNotification('Error', "No data is available in IDOS for this graph");
                }
            },
            error: function (xhr, status, error) {
                app.showNotification('Error', 'Could not communicate with the server.');
            }
        });
    }

    //Branch wise vendor advances paid
    function populateBranchVendorAdvances(useremail) {
        var jsonData = {};
        jsonData.useremail = useremail;

        $.ajax({
            url: 'http://www.myidos.com/transaction/branchwisevendoradvance',
            data: JSON.stringify(jsonData),
            type: 'POST',
            crossDomain: true,
            contentType: 'application/json;charset=utf-8',
            success: function (data) {
                if (data.branchVendorAdvancePaid != null && data.branchVendorAdvancePaid.length > 0) {
                    var myTable = new Office.TableData();
                    myTable.headers = ["Branch", "0-30days", "30-60days", "60-90days", "90-180days"];
                    for (var i = 0; i < data.branchVendorAdvancePaid.length; i++) {
                        myTable.rows[i] = [data.branchVendorAdvancePaid[i].branchName, data.branchVendorAdvancePaid[i].advfor0to30days, data.branchVendorAdvancePaid[i].advfor30to60days, data.branchVendorAdvancePaid[i].advfor60to90days, data.branchVendorAdvancePaid[i].advfor90to180days];
                    }
                    pouplateIDOSDataInXLS(myTable, "VendorAdvances", "BRANCH wise Vendor Advances Received (All figures are in Indian Rupees (INR))");
                }
                else {
                    app.showNotification('Error', "No data is available in IDOS for this graph");
                }
            },
            error: function (xhr, status, error) {
                app.showNotification('Error', 'Could not communicate with the server.');
            }
        });
    }

    function pouplateIDOSDataInXLS(myTable, sheetTitle, graphtitle) {

        Excel.run(function (ctx) {
            // Create a proxy object for the worksheets collection
            //var sheets = ctx.workbook.worksheets;

            // Queue a command to add a new sheet called Dashboard
            //var sheet = sheets.add(sheetTitle);
            var sheet = ctx.workbook.worksheets.getActiveWorksheet();
            //Queue commands to set the report title in the worksheet                        
            sheet.getRange("A1").format.font.name = "Century";
            sheet.getRange("A1").format.font.size = 12;
            sheet.getRange("A1:C1").format.font.bold = true;

            // Queue a command to activate the Dashboard sheet
            sheet.activate();
            // sheet.load('name');                        

            // Synchronizes the state between JavaScript proxy objects and real objects in Excel by executing instructions queued on the context 
            return ctx.sync().then(function () {
                // Create a proxy object for the active worksheet
                var sheet = ctx.workbook.worksheets.getActiveWorksheet();
                //window.setTimeout(myFunction, 5000);
                // var sheet = ctx.workbook.worksheets.getItem(sheetTitle);
                //sheet.load('name');
                // Queue a command to write the sample data to Sheet1
                // Write table in xls.
                Office.context.document.setSelectedDataAsync(myTable, { coercionType: Office.CoercionType.Table },
                    function (result) {
                        var error = result.error
                        if (result.status === Office.AsyncResultStatus.Failed) {
                            console.log(error.name + ": " + error.message);
                        }
                    });

                loadDataAndCreateChart(graphtitle);
                console.log("Done");
            });

        }).catch(function (error) {
            // Always be sure to catch any accumulated errors that bubble up from the Excel.run execution
            app.showNotification("Error: " + error);
            console.log("Error: " + error);
            if (error instanceof OfficeExtension.Error) {
                console.log("Debug info: " + JSON.stringify(error.debugInfo));
            }
        });
    }
    /*
        function myFunction() {
            console.log("Waiting....");
        }*/
    // Load some sample data into the worksheet and then create a chart
    function loadDataAndCreateChart(graphtitle) {
        // Run a batch operation against the Excel object model
        Excel.run(function (ctx) {
            // Create a proxy object for the active worksheet
            var sheet = ctx.workbook.worksheets.getActiveWorksheet();

            //Queue a command to write the sample data to the specified range            
            //var range = sheet.getRange("A1:D11");
            var range = sheet.getUsedRange();
            // range.load("address, values");

            sheet.getRange("A1:C1").format.font.bold = true;

            //Queue a command to add a new chart ColumnStacked100
            var chart = sheet.charts.add("ColumnClustered", range, "auto");

            //Queue commands to set the properties and format the chart
            chart.setPosition("G1", "O30");
            chart.title.text = graphtitle;
            chart.legend.position = "right"
            chart.legend.format.fill.setSolidColor("white");
            chart.dataLabels.format.font.size = 25;
            chart.dataLabels.format.font.color = "black";
            // var points = chart.series.getItemAt(0).points;
            //points.getItemAt(0).format.fill.setSolidColor("blue");

            //Run the queued commands, and return a promise to indicate task completion           
            return ctx.sync().then(function () {
                console.log("Done");
            });
        })
            .catch(function (error) {
                // Always be sure to catch any accumulated errors that bubble up from the Excel.run execution
                app.showNotification("Error: " + error);
                console.log("Error: " + error);
                if (error instanceof OfficeExtension.Error) {
                    console.log("Debug info: " + JSON.stringify(error.debugInfo));
                }
            });
    }

})();
