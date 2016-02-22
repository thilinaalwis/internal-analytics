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
$.post("/portal/controllers/apis/patching-trend-table/table.jag", {
	action : "GET"
}, function(datax) {
	// Get the data from hosted jaggery files and parse it
	var data = JSON.parse(datax);
	// array to store product name,version,patch id,kernel version
	var formated_data = [];
	// array to store product names
	var product_name_list = [];

	for ( var i in data) {
		// var product = $.trim(data[i].product);
		// format the product name by removing the version number and
		// unnecessary spaces
		var product_name = $.trim(data[i].product.replace("WSO2", "").replace(
				/\d+/g, '').split('.').join(""));
		// get the version number by removing the product name
		var version = $.trim(data[i].product.replace(product_name, ''));
		// if there is no version number set it as 0.0.0
		if (version == "") {
			version = "0.0.0";
		}
		// push the product name in to product list if not exists
		if (product_name_list.indexOf(product_name) == -1) {
			product_name_list.push(product_name);
		}
		// push the data in to formatted_data array
		formated_data.push({
			"product_name" : product_name,
			"version" : version,
			"OverviewName" : data[i].OverviewName,
			"kernel" : data[i].platform
		});

	}
	// draw the table and the graph
	drawTable(product_name_list, formated_data);

});
/**
 * This function drwas the data table
 *
 * @param product_name_list-
 *            array with product names
 * @param data -
 *            data array with product name,version,patch id and kernel version
 */

function drawTable(product_name_list, data) {
	// array to store options of the drop down list
	var options = [];
	// enter values to options array to bind with drop down list
	for (var i = 0; i < product_name_list.length; i++) {
		options.push('<option value="', i, '">', product_name_list[i],
				'</option>');
	}
	$("#productList").html(options.join(''));
	// set the initial selection of the drop down list to empty
	$("#productList").prop("selectedIndex", 0);
	// when the user select an option draw the table and the graph for that
	// option
	$("#productList").change(function() {
		// get selected option
		var selection = $(":selected", this).text();
		// clear the data table
		$("#rows tr").remove();
		// clear the existing line graph from the canvas
		var canvas = document.getElementById('line-graph');
		var context = canvas.getContext('2d');
		context.clearRect(0, 0, canvas.width, canvas.height);

		var counter = 0;
		var dataSet = [];
		// get the data set for selected option
		for ( var i in data) {
			if (data[i].product_name == selection) {
				dataSet[counter++] = data[i];
			}
		}
		getPatchCounts(dataSet);
	});
	var counter = 0;
	var dataSet = [];
	// get the data set for selected option
	for ( var i in data) {
		if (data[i].product_name == product_name_list[0]) {
			dataSet[counter++] = data[i];
		}
	}
	getPatchCounts(dataSet);

}
/**
 * this function calculate the patch count of each version of selected product
 *
 * @param dataSet-data
 *            array for selected option
 */

function getPatchCounts(dataSet) {
	// array to store version,patch count,kernel version
	var result = [];
	var flag = 0;
	var patch_count;
	for ( var i in dataSet) {
		// get the product version of the i th object
		var version = dataSet[i].version;
		// set flag to zero
		flag = 0;
		// check the version number is already in result array , if so flag is
		// updated to 1
		for ( var j in result) {
			if (result[j].version == version) {
				flag = 1;
				break;
			}
		}
		// if version of i th element not in result array(flag!=1) get the patch
		// count for this version
		if (flag != 1) {
			patch_count = 0;
			for ( var j in dataSet) {
				if (dataSet[j].version == version) {
					patch_count++;
				}

			}
			result.push({
				"version" : version,
				"patch_count" : patch_count,
				"kernel" : dataSet[i].kernel
			});
		}
	}
	// sort by version number and store in an array
	var sorted_result = sortByVersion(result);
	// if version of the 0 th element is 0.0.0 then it set as Unknown
	if (sorted_result[0].version == "0.0.0") {
		sorted_result[0].version = "Unknown";
	}
	// append the sorted result set with the table
	appendData(sorted_result);
	// draw the graph for sorted data set
	drawGraph(sorted_result);
}
/**
 * this function is to append the data with table
 *
 * @param result-sorted
 *            data set
 */
function appendData(result) {
	// bind the data with the table. Table has 3 columns . They are product
	// version,patch count and kernel version

	for (var i = 0; i < result.length; i++) {
		$(".datatable").append(
				"<tr><td>" + result[i].version + "</td><td>"
				+ result[i].patch_count + "</td><td>"
				+ result[i].kernel + "</td></tr>");
		$("#myTable").trigger("update");

	}
}
/**
 * This function draws the graph
 *
 * @param result-sorted
 *            data set
 */

function drawGraph(result) {
	// array to store points in X-axis
	var x_points = [];
	// store version numbers as x-points
	for ( var i in result) {
		x_points.push(result[i].version);
	}
	// array to store points in Y-axis
	var y_points = [];
	// store patch count of each version as Y-points
	for ( var i in result) {
		y_points.push(result[i].patch_count);
	}

	var line_graph_data = {
		xLabel : 'Patch Count',// X-axis label is named as "Patch Count"
		yLabel : 'Version',// Y-axis label is named as "Version"
		points : x_points,// assigned x-points
		groups : [ {

			values : y_points
		} ]
	};
	// bind the data with the graph
	$('#line-graph').graphly({
		'data' : line_graph_data,
		'type' : 'line',// chart type
		'width' : 1100,// width of the chart
		'height' : 500
		// height of the chart
	});

}
/**
 * this function sort the result set using version number in ascending order
 *
 * @param result-final
 *            result set
 * @returns- sorted result set
 */
function sortByVersion(result) {
	// array to store sorted results
	var sorted_result_set = [];
	var swapped = true;
	var temp_item;
	// bubble sort to sort the array using version numbers
	while (swapped) {
		swapped = false;
		for (var j = 0; j < result.length - 1; j++) {
			if (compare(result[j].version, result[j + 1].version)) {
				temp_item = result[j];
				result[j] = result[j + 1];
				result[j + 1] = temp_item;
				swapped = true;

			}
		}
	}
	return result;
}
/**
 * this function compares two version numbers and return true if version number
 * 1 is bigger than the version number 2
 *
 * @param version_1-version
 *            number of the product
 * @param version_2-version
 *            number of the product
 * @returns {Boolean}
 */
function compare(version_1, version_2) {
	// convert the version numbers in to a JSON object
	var version_num_1 = parseVersionString(version_1);
	var version_num_2 = parseVersionString(version_2);
	// compare the major parts of the two version numbers and if the first one
	// is bigger than the second then returns true
	if (version_num_1.major > version_num_2.major) {
		return true;
		// if major parts are equal then check with the minor part
	} else if (version_num_1.major >= version_num_2.major
			&& version_num_1.minor > version_num_2.minor) {

		return true;
		// else check with the last part of the version number
	} else if (version_num_1.major >= version_num_2.major
			&& version_num_1.minor >= version_num_2.minor
			&& version_num_1.patch > version_num_2.patch) {

		return true;

	} else {

		return false;
	}
}
/**
 *
 * @param str -
 *            version number
 * @returns {JSON object}
 */
function parseVersionString(str) {
	// split at dots
	var x = str.split('.');
	// parse from string or default to 0 if can't parse
	var maj = parseInt(x[0]) || 0;
	var minor = parseInt(x[1]) || 0;
	var patch = parseInt(x[2]) || 0;
	return {
		major : maj,
		minor : minor,
		patch : patch
	}
}