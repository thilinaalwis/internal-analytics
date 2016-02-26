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
$.post("/portal/controllers/apis/development-support-hours/dev_support_hours.jag", {
    action: "GET"
}, function (datax) {
    // Get the data from hosted Jaggery files and parse it
    var data = JSON.parse(datax);
    dataBind(data);
});

function dataBind(data) {
    //sort the array in ascending order based on remaining hours
    data.sort(function (a, b) {
        return a.remaining_hours - b.remaining_hours
    });

//bind the data with the table
    for (var i = 0; i < data.length; i++) {
        $("#rows").append(
            "<tr><td>" + data[i].SupportAccountKey + "</td><td>"
            + data[i].Account_Name + "</td><td>" + data[i].remaining_hours + "</td><td>" + data[i].EndDate + "</td></tr>");
        $("#Table").trigger("update");

    }

}
