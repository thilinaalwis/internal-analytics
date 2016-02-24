/*
 ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~      http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 */
//publish the selected option when dashboard is loading
gadgets.HubSettings.onConnect = function () {
    //set date pickers
    datePickerController();
    //end date of the selected time period
    var end_date = new Date();
    //get date one year back from today
    var today = new Date();
    today.setDate(today.getDate() - 365);
    //set start date as one year back from current date
    var start_date = today;
    //set initial time perion on date pickers
    $('#datepicker_from').datepicker('setDate', start_date);
    $('#datepicker_to').datepicker('setDate', end_date);
    //array to store start_date and end_date
    var dates = [];
    dates.push({
        "start_date": start_date,
        "end_date": end_date
    });
    //publish dates object to subscribers
    setTimeout(function () {
        gadgets.Hub.publish('date-publisher', {
            msg: dates
        });
    }, 200);
};
/**
 * this function set the date picker configurations
 */
function datePickerController() {
    // date picker to select begin date of the time period
    $('#datepicker_from').datepicker({
        changeMonth: true,// add the month selector
        changeYear: true,// add the year selector
        dateFormat: "yy-mm-dd",// keep date format as yy-mm-dd
        maxDate: new Date(),// maximum active date is set to today
        onSelect: function () {
            //get selected date of this date picker
            var selected_date = $('#datepicker_from').datepicker("getDate");
            //calculate the date 3 years back from current date
            var date_3yrs_back = new Date();
            date_3yrs_back.setFullYear(date_3yrs_back.getFullYear() - 3);
            /**
             * if start date is older than the date which is 3 yr back from current date,
             * then limit the max date of date picker 2 .
             * (this is done bcz when the time range is larger than 3 years then the graphs are not clear)
             */
            if (selected_date.getFullYear() < date_3yrs_back) {
                var maxDate = new Date();
                maxDate.setFullYear(selected_date.getFullYear() + 3);

                $('#datepicker_to').datepicker('option', 'maxDate', maxDate);
            }
            else {
                $('#datepicker_to').datepicker('option', 'maxDate', new Date());
            }

        }

    });
    $('#datepicker_to').datepicker({
        changeMonth: true,// add the month selector
        changeYear: true,// add the year selector
        dateFormat: "yy-mm-dd"// keep date format as yy-mm-dd

    });
}
/**
 * this function is used to publish the data to subscribers
 * when the button is clicked
 */
function buttonClick() {
    //get dates from date pickers
    var start_date = $('#datepicker_from').datepicker("getDate");
    var end_date = $('#datepicker_to').datepicker("getDate");
    //array to keep start and end dates
    var dates = [];
    dates.push({
        "start_date": start_date,
        "end_date": end_date
    });
    //publish the dates object to subscribers
    gadgets.Hub.publish('date-publisher', {
        msg: dates

    });

}
