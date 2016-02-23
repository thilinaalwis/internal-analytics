/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
$.post("/portal/controllers/apis/pmt-line-chart/pmt-line-chart-data.jag", {
    action: "GET"
}, function (datax) {
    // Get the data from hosted Jaggery files and parse it
    var data = JSON.parse(datax);
    // go through each data element and reformat the product name by removing
    // version number and unnecessary spaces
    for (var i in data) {
        var temp_name = data[i].OverviewProduct.replace(/\d+/g, '').split('.')
            .join("");
        data[i].OverviewProduct = $.trim(temp_name);
    }
    // set begining of the initial time period as 365 days back from current
    // date
    var begin = new Date();
    begin.setDate(begin.getDate() - 365);
    $("#first_row tr").hide();
    // this function create the graph for initial time range. (12 months back
    // from current date)
    drawLineGraph(begin, new Date(), data);
    // this create the graph when time period is selected through the date
    // pickers
    dataBind(data);
    //set the initial time range to two date pickers
    $('#datepicker').datepicker('setDate', new Date(begin));
    $('#datepicker2').datepicker('setDate', new Date());
});

function dataBind(data) {
    // date picker to select begin date of the time period
    $('#datepicker').datepicker({
        changeMonth: true,// add the month selector
        changeYear: true,// add the year selector
        dateFormat: "yy-mm-dd",// keep date format as yy-mm-dd
        maxDate: new Date(),// maximum active date is set to today
        onSelect: checkDate
        // when date is selected the checkDate method is executed
    });

    $('#datepicker2').datepicker({
        changeMonth: true,// add the month selector
        changeYear: true,// add the year selector
        dateFormat: "yy-mm-dd",// keep date format as yy-mm-dd
        onSelect: function () {
            // get selected start date from date picker 1
            var start_date = $('#datepicker').datepicker("getDate");
            // get selected end date from date picker 2
            var end_date = $('#datepicker2').datepicker("getDate");
            // draw the graph for selected time period
            drawLineGraph(start_date, end_date, data);
            // disable the datepicker 2
            $('#datepicker2').datepicker('option', 'disabled', true);
        }
    });
    // disable the date picker 2 until select a date from datepicker 1
    $('#datepicker2').datepicker('option', 'disabled', true);
}
/**
 *
 * @param startDate-start
 *            date of the time period selected from the date picker this
 *            function is to set min date in date picker 1 and to disable the
 *            date picker 2
 */
function checkDate(startDate) {
    // clear the existing bar chart from the canvas
    var canvas = document.getElementById('line-graph');
    var context = canvas.getContext('2d');
    context.clearRect(0, 0, canvas.width, canvas.height);
    //clear the date picker 2 text box
    $('#datepicker2').datepicker('setDate', null);
    // enable the date picker 2
    $('#datepicker2').datepicker('option', 'disabled', false);
    // set minimum date as start date in date picker 2
    $('#datepicker2').datepicker('option', 'minDate', startDate);
    var selected_Date = $('#datepicker').datepicker("getDate");
    var date_3_yr_back = new Date();
    date_3_yr_back.setFullYear(date_3_yr_back.getFullYear() - 3);
    if (selected_Date < date_3_yr_back) {
        var maxDate = new Date();
        maxDate.setFullYear(selected_Date.getFullYear() + 3);
        $('#datepicker2').datepicker('option', 'maxDate', maxDate);
    }
    else {
        $('#datepicker2').datepicker('option', 'maxDate', new Date());
    }
    // set the selected option of the selector to empty
    $("#productList").prop("selectedIndex", 0);

}
/**
 * this function is to get the month difference between given two days
 *
 * @param date1-start
 *            date of the time period
 * @param date2-end
 *            date of the time period
 * @returns month difference
 */
function diff_months(date1, date2) {
    var months;
    // count the year difference by months
    months = (date2.getFullYear() - date1.getFullYear()) * 12;
    // minus the months before start month
    months -= date1.getMonth();
    // add the months of end date
    months += date2.getMonth() + 1;
    // if months count is minus then return zero else return month count
    return months <= 0 ? 0 : months;
}
/**
 * this function returns the month name for the given month number
 */
function GetMonthName(monthNumber) {
    var months = ['January', 'February', 'March', 'April', 'May', 'June', 'July',
        'August', 'September', 'October', 'November', 'December'];
    // resturn the month name for the given month number
    return months[monthNumber];
}
/**
 * this function is to calculate the patch count of each product and draw the
 * line
 *
 * @param start_date-begin
 *            date of the time period
 * @param end_date-end
 *            date of the time period
 * @param data-data
 *            set from api
 */
function drawLineGraph(start_date, end_date, data) {
    // convert date to yy-mm-dd to display in html labels
    var from = [start_date.getFullYear(),
        ('0' + (start_date.getMonth() + 1)).slice(-2),
        ('0' + start_date.getDate()).slice(-2)].join('-');
    var to = [end_date.getFullYear(),
        ('0' + (end_date.getMonth() + 1)).slice(-2),
        ('0' + end_date.getDate()).slice(-2)].join('-');
    var k = 0;
    // array to store the data set between the selected time period
    var dataSet = [];
    // select data set between selected time period and store it in the array
    // called dataSet
    for (var i in data) {
        if (data[i].DateReleasedOn != null && data[i].OverviewProduct != "-"
            && data[i].DateReleasedOn >= from
            && data[i].DateReleasedOn <= to) {
            dataSet[k++] = data[i];
        }

    }
    //if there is no patch data for selected time period then alert it
    if (dataSet.length == 0) {
        alert("Patches not exists for the selected time period.");
    }
    // get the month difference of selected time period
    var months_diff = diff_months(start_date, end_date);
    // array to store month numbers of the time period
    var month_number_array = [];
    //array to store month number and the year. This is used in table
    var dates = [];
    //change the format of the start date and set it to new variable
    var date = new Date(start_date);
    // create the array of the month numbers of selected time period
    for (var i = 0; i < months_diff; i++) {
        //put the month numbers in to an array
        month_number_array[i] = date.getMonth();
        dates.push({
            "month_number": date.getMonth(),
            "year": date.getFullYear()
        });
        //date is set to 1 . Let's think the date is 31 and month is January , then if we increment the month number then it goes to March. to avoid this the date is set to 1
        date.setDate(1);
        date.setMonth(date.getMonth() + 1);
    }
    // array to store product name and the patch counts for each month
    var checked_product = [];
    var flag = 0;
    var patch_count = 0;
    // go through the each element in dataSet[]
    for (var i in dataSet) {
        // assign the i th element to a variable
        var product = dataSet[i];
        // initially flag set to zero
        flag = 0;
        if (checked_product != null) {
            for (var j in checked_product) {
                if (checked_product[j].product == product.OverviewProduct) {
                    // flag is updated as 1 if the product name is already in checked product list
                    flag = 1;
                    break;
                }
            }
        }
        // if the product is not in checked_product list get the patch count and
        // push in to the checked_product array
        if (flag != 1) {
            // set start_date to date format and assign to date variable
            var date = new Date(start_date);
            // get month number of the start date of the time period
            var month_number = date.getMonth();
            // get the year of the date
            var year = date.getFullYear();
            // array to store patch count of each month
            var value = [];
            // calculate the patch count for each month of the selected time
            // period and store it in value[] array
            for (var j = 0; j < months_diff; j++) {
                // patch count of the month set to zero
                patch_count = 0;
                // go through the each element in dataSet and get the patch
                // count by matching the patch release date of the selected
                // product
                for (var k in dataSet) {
                    if (product.OverviewProduct == dataSet[k].OverviewProduct
                        && new Date(dataSet[k].DateReleasedOn)
                            .getFullYear() == year
                        && new Date(dataSet[k].DateReleasedOn).getMonth() == month_number) {
                        patch_count++;
                    }
                }
                // store the patch count of the month
                value[j] = patch_count;
                // get the next month number
                date.setDate(1);
                date.setMonth(date.getMonth() + 1);
                month_number = date.getMonth();
                // get the year
                year = date.getFullYear();

            }
            // push the product name and the patch counts of the months
            checked_product.push({
                "product": product.OverviewProduct,
                "value": value,

            });
        }
    }
    // create JSON array and bind the data with graph
    createArray(checked_product, month_number_array, dates);
}
/**
 *
 */
function createArray(result, month_number, dates) {

    // array to store names of the months of selected time period
    var month_name = [];
    // get month names of the selected time period
    for (var i in month_number) {
        var temp_month_name = GetMonthName(month_number[i]);
        if (temp_month_name.length > 5) {
            month_name[i] = GetMonthName(month_number[i]).slice(0, 3);
        }
        else {
            month_name[i] = GetMonthName(month_number[i]);
        }
    }
    // array to store product name,patch counts for each month and the total
    // patch count of the selected time period
    var data = [];
    // for each product, calculate the total patch count
    for (var i in result) {
        var tot_patches = 0;
        for (j = 0; j < result[i].value.length; j++) {
            tot_patches += result[i].value[j];
        }
        // push the product name , patch counts of months and the total patch
        // count of the product for selected time period
        data.push({
            "label": result[i].product,
            "values": result[i].value,
            "tot_patches": tot_patches
        });
    }
    //array to store products which are in top ten patch count list
    var top_ten_patch_list = [];
    //sort the array in descending order of the total patch count
    data.sort(function (a, b) {
        return b.tot_patches - a.tot_patches
    });
    //select the ten product with highest patch count
    if (data.length > 10) {
        for (var i = 0; i < 10; i++) {
            top_ten_patch_list.push(data[i]);
        }
    } else {
        top_ten_patch_list = data;
    }
    //array to store the options of the drop down list
    var options = [];
    options.push('<option value="', 0, '">', "Top Ten Products", '</option>');
    options.push('<option value="', 1, '">', "All Products", '</option>');
    //assign product names as the options
    for (var i = 0; i < data.length; i++) {
        options
            .push('<option value="', i + 2, '">', data[i].label,
                '</option>');
    }
    //set initial value of the selector as "Top Ten Product List"
    $("#productList").html(options.join(''));
    $("#productList").prop("selectedIndex", 0);
    //when select an option from the drop down list get the relevant result set from the data array
    $("#productList").change(function () {
        var selection = $(":selected", this).text();

        var res = [];
        if (selection == "Top Ten Products") {
            res = top_ten_patch_list;
            $("#first_row tr").hide();
            $("#rows tr").remove();

        } else if (selection == "All Products") {
            res = data;
            $("#first_row tr").hide();
            $("#rows tr").remove();

        } else {

            for (var i in result) {
                if (selection == data[i].label) {
                    res.push(data[i]);
                    break;
                }

            }

            drawTable(dates, res);
        }

        var canvas = document.getElementById('line-graph');
        var context = canvas.getContext('2d');
        context.clearRect(0, 0, canvas.width, canvas.height);

        bindDatatoGraph(month_name, res);

    });
//draw the graph for top ten list
    bindDatatoGraph(month_name, top_ten_patch_list);

}
/**
 *
 * @param month_name - array of months of the selected time period
 * @param data -data array to bind with the graph
 */
function bindDatatoGraph(month_name, data) {
//create array to bind with graph
    var line_graph_data = {
        xLabel: 'Month',//label of x axis
        yLabel: 'Patch Count',//label of y axis
        points: month_name,//month names as the points of x axis
        groups: data //product and patch counts to draw the lines
    };
//create the graph for the above data
    $('#line-graph').graphly({
        'data': line_graph_data,
        'type': 'line',//type of the graph
        'width': 1000,//width of the graph
        'height': 500  //height of the graph
    });

}

function drawTable(dates, res) {
    var table_data = [];

    // clear the table
    $("#rows tr").remove();
    $("#first_row tr").show();
    for (var i in dates) {
        var month_column = dates[i].year + " " + GetMonthName(dates[i].month_number)
        table_data.push({
            "month": month_column,
            "patch_count": res[0].values[i]
        });
    }


    for (var i = 0; i < table_data.length; i++) {
        $("#rows").append(
            "<tr><td>" + table_data[i].month + "</td><td>"
            + table_data[i].patch_count + "</td></tr>");
        $("#myTable").trigger("update");
    }

}
