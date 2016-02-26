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
$.post("/portal/controllers/apis/pmt-bar-chart/pmt-patch-data.jag", {
    action: "GET"
}, function (datax) {
    // Get the data from hosted jaggery files and parse it
    var data = JSON.parse(datax);
    // set begin date as 365 days from current date
    var begin = new Date();
    begin.setDate(begin.getDate() - 365);

    // draw the barchart with initial time period (12-months)
    drawBarChart(begin, new Date(), data);
    // draw the bar chart for selected time period
    pickDateAndDraw(data);
    $('#datepicker').datepicker('setDate', new Date(begin));
    $('#datepicker2').datepicker('setDate', new Date());
});
/**
 * this function is to draw bar chart for selected dates from date pickers
 *
 * @param data-data
 *            set from api
 *
 */

function pickDateAndDraw(data) {
    // date picker to select begin date of the time period
    $('#datepicker').datepicker({
        changeMonth: true, // add the month selector
        changeYear: true, // add the year selector
        dateFormat: "yy-mm-dd", // keep date format as yy-mm-dd
        maxDate: new Date(),// maximum active date is set to today
        onSelect: checkDate
        // when date is selected the checkDate method is executed
    });
    // date picker to select end date of the time period
    $('#datepicker2').datepicker({
        changeMonth: true,
        changeYear: true,
        dateFormat: "yy-mm-dd",
        onSelect: function () {
            // get selected start date from date picker 1
            var start_date = $('#datepicker').datepicker("getDate");
            // get selected end date from date picker 2
            var end_date = $('#datepicker2').datepicker("getDate");
            // draw the barchart for selected time period
            drawBarChart(start_date, end_date, data);
            // disable the datepicker 2
            $('#datepicker2').datepicker('option', 'disabled', true);
        }
    });
    // disable the date picker 2 until select a date from datepicker 1
    $('#datepicker2').datepicker('option', 'disabled', true);
}
/**
 *
 * this function is to set min date in date picker 1 and to disable the date
 * picker 2
 */
function checkDate(startDate) {
    // clear the existing bar chart from the canvas
    var canvas = document.getElementById('canvas');
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
//limit the time range to 3 years.The reason for this is the chart is not clear when the time period is larger than 3 years
    if (selected_Date < date_3_yr_back) {

        var maxDate = new Date();
        maxDate.setFullYear(selected_Date.getFullYear() + 3);

        $('#datepicker2').datepicker('option', 'maxDate', maxDate);
    }
    else {
        $('#datepicker2').datepicker('option', 'maxDate', new Date());
    }
}
/**
 * this function calculate the patch count for each month
 *
 * @param start_date -
 *            date selected from date picker 1
 * @param end_date -
 *            date selected from date picker 2
 * @param data -
 *            data set from api
 */
function drawBarChart(start_date, end_date, data) {
    // set the start date in yyyy-mm-dd format to display on label
    var from = [start_date.getFullYear(),
        ('0' + (start_date.getMonth() + 1)).slice(-2),
        ('0' + start_date.getDate()).slice(-2)].join('-');
    // set end date in yyyy-mm-dd format to display on label
    var to = [end_date.getFullYear(),
        ('0' + (end_date.getMonth() + 1)).slice(-2),
        ('0' + end_date.getDate()).slice(-2)].join('-');
    var k = 0;
    // array to store the data set between the selected time period
    var dataSet = [];
    // select data set between selected time period and store it in the array
    // called dataSet
    for (var i in data) {
        if (data[i].DateReleasedOn != null && data[i].DateReleasedOn >= from
            && data[i].DateReleasedOn <= to) {
            dataSet[k++] = data[i];
        }
    }
    // get the month difference of selected time period
    var months_diff = diff_months(start_date, end_date);
    // set start_date to date format and assign to date variable
    var date = new Date(start_date);
    // get the month number of begin date
    var month_number = date.getMonth();
    // counter to count the patch count of the month
    var patch_count;
    // array to store month_name and patch count of the month
    var result = [];
    // get the year of the begin date
    var year = date.getFullYear();
    // for loop to get the patch count of each month of the time period
    for (var i = 0; i < months_diff; i++) {
        // counter is set to zero for each month
        patch_count = 0;
        // go through the selected data set and count the patches with matching
        // the year and the month number of the patch released date
        for (var j in dataSet) {
            // alert(new Date(dataSet[i].DateReleasedOn).getMonth());

            if ((new Date(dataSet[j].DateReleasedOn).getFullYear()) == year
                && (new Date(dataSet[j].DateReleasedOn).getMonth()) == month_number) {
                patch_count++;
            }
        }
        // get the month name using month number
        var month_name = GetMonthName(month_number);
        // push the patch count,month name and the color of the bar to array
        // called result
        result.push({
            "label": month_name,
            "value": patch_count

        });
        // get the next month number using previous month
        date.setDate(1);
        date.setMonth(date.getMonth() + 1);
        month_number = date.getMonth();
        // get the year from new date
        year = date.getFullYear();
    }
    // create array to bind the data with UI
    createArray(result);
}

/**
 * this function is to calculate the month difference between two dates
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
    var months = ['Jan', 'Feb', 'March', 'April', 'May', 'June', 'July',
        'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    // resturn the month name for the given month number
    return months[monthNumber];
}
/**
 *this function bind the data array with the bar chart
 * @param result -
 *            final calculated data set this function is to create the JSON
 *            array and bind it with the graph
 */
function createArray(result) {
    var data = {
        xLabel: 'Months',// label of X-axis
        yLabel: 'Patch Count',// label of Y-axis
        groups: [{
            label: 'patch Count',
            values: result
            // set the result array to values
        }]
    };
    // bind the above array to graph
    $('#canvas').graphly({
        'data': data
    });

}
