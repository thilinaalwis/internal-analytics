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
var data = [];
$.post("/portal/controllers/apis/product-selector/product-selector.jag", {
    action: "GET"
}, function (datax) {
    // Get the data from hosted Jaggery files and parse it
    data = JSON.parse(datax);
    //array to store options of the selector
    var options = [];
    //assign product names as the options of the selector
    for (var i = 0; i < data.length; i++) {
        options
            .push('<option value="', i, '">', data[i].PRODUCT_NAME,
                '</option>');
    }
    //join the option array with the selector
    $("#productList").html(options.join(''));
    //set initial selected option
    $("#productList").prop("selectedIndex", 0);
    //when selector get changed
    $("#productList").change(function () {
        // get selected option of the selector
        var selection = $(":selected", this).text();
        //array to store product mapping names of each system.(currently we have Redmine,PMT,Jenkins)
        var product_mapping_names = [];
        //find the mapping names for the selected product from 3 systems
        for (var i in data) {
            if (selection == data[i].PRODUCT_NAME) {
                product_mapping_names.push({
                    //product name in REDMINE
                    "redmine_name": data[i].REDMINE,
                    // product name in PMT
                    "PMT_name": data[i].PMT,
                    //product name in JENKINS
                    "Jenkins_name": data[i].JENKINS
                });
                break;
            }
        }
        //publish the product_mapping_names to the subscribers
        gadgets.Hub.publish('select', {
            msg: product_mapping_names
        });


    });

});
//publish the selected option when dashboard is loading
gadgets.HubSettings.onConnect = function () {
    var selected_option = $("#productList option:selected").text();
    var product_mapping_names = [];
    //find the mapping names for the selected product from 3 systems
    for (var i in data) {
        if (selected_option == data[i].PRODUCT_NAME) {
            product_mapping_names.push({
                //product name in REDMINE
                "redmine_name": data[i].REDMINE,
                // product name in PMT
                "PMT_name": data[i].PMT,
                //product name in JENKINS
                "Jenkins_name": data[i].JENKINS
            });
            break;
        }
    }
    setTimeout(function () {
        gadgets.Hub.publish('select', {
            msg: product_mapping_names

        });
    }, 200);
}
