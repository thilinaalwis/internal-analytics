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
$.post("/portal/controllers/apis/table.jag", {
        action: "GET"
    },
    function (datax) {
        // Get the data from hosted jaggery files and parse it
        var data = JSON.parse(datax);
        //remove the version number and unneccessary white spaces
        for (var i in data) {
            var temp_name = data[i].product.replace(/\d+/g, '').split('.')
                .join("");
            data[i].product = $.trim(temp_name);

        }
        //set the start date of the time preiod to 365 days back from current date
        var begin = new Date();
        begin.setDate(begin.getDate() - 365);
        //draw the table to initial time period
        drawTable(begin, new Date(), data);
        //draw the table to selected time periods
        selectAndDraw(data);
        $('#datepicker').datepicker('setDate', new Date(begin));
        $('#datepicker2').datepicker('setDate', new Date());
    });

function selectAndDraw(data) {
    // date picker to select begin date of the time period
    $('#datepicker').datepicker({
        changeMonth: true,// add the month selector
        changeYear: true,// add the year selector
        dateFormat: "yy-mm-dd",// keep date format as yy-mm-dd
        maxDate: new Date(),// maximum active date is set to today
        onSelect: checkDate  // when date is selected the checkDate method is executed
    });

    $('#datepicker2').datepicker({
        changeMonth: true,
        changeYear: true,
        dateFormat: "yy-mm-dd",
        maxDate: new Date(),
        onSelect: function () {
            // get selected start date from date picker 1
            var start_date = $('#datepicker').datepicker("getDate");
            // get selected start date from date picker 2
            var end_date = $('#datepicker2').datepicker("getDate");
            //clear the table
            $("#rows tr").remove();
            //draw the new table for selected time range
            drawTable(start_date, end_date, data);
            //disable the date picker 2
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
    $('#datepicker2').datepicker('option', 'disabled', false);
    $('#datepicker2').datepicker('option', 'minDate', startDate);

}
/**
 * this function calculate the patch count for selected time period
 *
 * @param start_date -
 *            date selected from date picker 1
 * @param end_date -
 *            date selected from date picker 2
 * @param data -
 *            data set from api
 */
function drawTable(start_date, end_date, data) {
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
        if (data[i].DateReleasedOn != null && data[i].DateReleasedOn > from
            && data[i].DateReleasedOn < to) {
            dataSet[k++] = data[i];
        }
    }

    var result = [];
    var flag = 0;
    for (var i in dataSet) {
        flag = 0;
        //check i th element is already in result array
        if (result != null) {
            for (var j in result) {
                if (result[j].product == dataSet[i].product) {
                    flag = 1;
                    break;
                }
            }
        }
        //if i th element is already not in result array , calculate the patch count for given time period and put it in to result array
        if (flag == 0) {
            var patch_count = 0;
            for (var j in dataSet) {
                if (dataSet[j].product == dataSet[i].product) {
                    patch_count++;
                }
            }

            result.push({
                "product": dataSet[i].product,
                "patches": patch_count,
                "kernel": dataSet[i].platform
            });

        }

    }
    //draw the table for result array
    buildTable(result);

}
/**
 *
 * @param result-data set to bind with the table
 */
function buildTable(result) {
    //sort the array in descending order based on patch count
    result.sort(function (a, b) {
        return b.patches - a.patches
    });
    //bind the data with the table
    for (var i = 0; i < result.length; i++) {
        $("#rows").append(
            "<tr><td>" + result[i].product + "</td><td>"
            + result[i].patches +  "</td></tr>");
        $("#myTable").trigger("update");

    }


}

