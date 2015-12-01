var datasource, datasourceType;
var previewData = [];
var columns = [];
var done = false;
var isPaginationSupported = true;

///////////////////////////////////////////// event handlers //////////////////////////////////////////
$(document).ready(function() {
    // $("#dsList").select2({
    //     placeholder: "Select a datasource",
    //     templateResult: formatDS
    // });
});

function formatDS(item) {
    if (!item.id) {
        return item.text;
    }
    var type = $(item.element).data("type");
    var $item;
    if (type === "realtime") {
        $item = $('<div><i class="fa fa-bolt"> </i> ' + item.text + '</div>');
    } else {
        $item = $('<div><i class="fa fa-clock-o"> </i> ' + item.text + '</div>');
    }
    // var $item = $(
    //     '<span><img src="vendor/images/flags/' + item.element.value.toLowerCase() + '.png" class="img-flag" /> '
    // + item.text + '</span>'
    //   );
    return $item;
};

$('#rootwizard').bootstrapWizard({
    onTabShow: function(tab, navigation, index) {
        //console.log("** Index : " + index);
        done = false;
        if (index == 0) {
            getDatasources();
            $("#btnPreview").hide();
            $('#rootwizard').find('.pager .next').addClass("disabled");
            $('#rootwizard').find('.pager .finish').hide();
        } else if (index == 1) {
            $('#rootwizard').find('.pager .finish').show();
            $("#previewChart").hide();
            done = true;
            if (datasourceType === "batch" && isPaginationSupported) {
                fetchData();
            }
            renderChartConfig();
        }
    }
});

$("#dsList").change(function() {
    datasource = $("#dsList").val();
    if (datasource != "-1") {
        $('#rootwizard').find('.pager .next').removeClass("disabled");
        datasourceType = $("#dsList option:selected").attr("data-type");
        getColumns(datasource, datasourceType);

        //check whether the seleced datasource supports pagination as well
        //first, get the recordstore for this table
        var recordStore;
        var tableName = datasource;
        var url = "/portal/apis/analytics?type=27&tableName=" + tableName;
        $.getJSON(url, function(data) {
            if (data.status==="success") {
                recordStore = data.message;
                //then check for pagination support on this recordstore
                recordStore = recordStore.replace(/"/g,"");
                checkPaginationSupported(recordStore);
            }
        });

    } else {
        $('#rootwizard').find('.pager .next').addClass("disabled");
    }
});

$("#btnPreview").click(function() {
    if ($("dsList").val() != -1) {
        fetchData(renderPreviewPane);
    }
});

$("#previewChart").click(function() {
    if (datasourceType === "realtime") {
        var streamId = $("#dsList").val();
        var url = "/portal/apis/rt?action=publisherIsExist&streamId=" + streamId;
        $.getJSON(url, function(data) {
            if (!data) {
                alert("You have not deployed a Publisher adapter UI Corresponding to selected StreamID:" + streamId +
                    " Please deploy an adapter to Preview Data.")
            } else {
                //TODO DOn't do this! read this from a config file
                subscribe(streamId.split(":")[0], streamId.split(":")[1], '10', 'carbon.super',
                    onRealTimeEventSuccessRecieval, onRealTimeEventErrorRecieval,  location.hostname, location.port,
                    'WEBSOCKET', "SECURED");
                var source = $("#wizard-zeroevents-hbs").html();;
                var template = Handlebars.compile(source);
                $("#chartDiv").empty();
                $("#chartDiv").append(template());
            }
        });
    } else {
        var dataTable = makeDataTable(previewData);
        var chartType = $("#chartType").val();
        var width = document.getElementById("chartDiv").offsetWidth;
        var height = 240; //canvas height
        $("#chartDiv").empty(); //clean up the chart canvas

        var config = {
            "yAxis": yAxis,
            "xAxis": xAxis,
            "width": width,
            "height": height,
            "chartType": chartType
        };
        drawChart(config,dataTable);
    }

});

$("#chartType").change(function() {
    $(".attr").hide();
    var className = jQuery(this).children(":selected").val();
    var chartType = this.value;
    $("." + className).show();
    $("#previewChart").show();
    $('#rootwizard').find('.pager .finish').removeClass('disabled');

});

$(".pager .finish").click(function() {
    //do some validations
    if ($("#title").val() == "") {
        alert("Gadget title must be provided!");
        return;
    }
    if (done) {
        console.log("*** Posting data for gadget [" + $("#title").val() + "]");
        //building the chart config depending on the chart type
        var chartType = $("#chartType").val();
        var config = {
            chartType: $("#chartType").val()
        };
        configureChart(config);
        config = chartConfig;
        // console.log(config); 
        var request = {
            id: $("#title").val().replace(/ /g, "_"),
            title: $("#title").val(),
            datasource: $("#dsList").val(),
            type: $("#dsList option:selected").attr("data-type"),
            filter: $("#txtFilter").val(),
            columns: columns,
            maxUpdateValue: 10,
            chartConfig: config

        };
        $.ajax({
            url: "/portal/apis/gadgetgen",
            method: "POST",
            data: JSON.stringify(request),
            contentType: "application/json",
            success: function(d) {
                console.log("***** Gadget [ " + $("#title").val() + " ] has been generated. " + d);
                window.location.href = "/portal/";
            }
        });
    } else {
        //console.log("Not ready");
    }
});

function onRealTimeEventSuccessRecieval(streamId, data) {
    drawRealtimeChart(data);
};

function onRealTimeEventErrorRecieval(dataError) {
    console.log(dataError);
};

////////////////////////////////////////////////// end of event handlers //////////////////////////////////////////////

function getDatasources() {
    $.ajax({
        url: "/portal/apis/rt?action=getDatasources",
        method: "GET",
        contentType: "application/json",
        success: function(data) {
            if (data.length == 0) {
                var source = $("#wizard-zerods-hbs").html();
                var template = Handlebars.compile(source);
                $("#rootwizard").empty();
                $("#rootwizard").append(template());
                return;
            }
            var datasources = data.map(function(element, index) {
                var item = {
                    name: element.name,
                    type: element.type
                };
                return item;
            });
            $("#dsList").empty();
            $("#dsList").append($('<option/>').val("-1")
                    .html("--Select a table/stream--")
                    .attr("type", "-1")
            );
            datasources.forEach(function(datasource, i) {
                var item = $('<option></option>')
                    .val(datasource.name)
                    .html(datasource.name + " [" + datasource.type + "]")
                    .attr("data-type", datasource.type);
                $("#dsList").append(item);
            });
        },
        error: function(xhr,message,errorObj) {
            var source = $("#wizard-error-hbs").html();;
            var template = Handlebars.compile(source);
            $("#rootwizard").empty();
            $("#rootwizard").append(template({
                error: xhr.responseText
            }));
        }
    });
};

function getColumns(datasource, datasourceType) {
    if (datasourceType === "realtime") {
        console.log("Fetching stream definition for stream: " + datasource);
        var url = "/portal/apis/rt?action=getDatasourceMetaData&type=" + datasourceType + "&dataSource=" + datasource;
        $.getJSON(url, function(data) {
            if (data) {
                columns = data;
            }
        });
    } else {
        console.log("Fetching schema for table: " + datasource);
        var url = "/portal/apis/analytics?type=10&tableName=" + datasource;
        $.getJSON(url, function(data) {
            if (data) {
                columns = parseColumns(JSON.parse(data.message));
            }
        });
    }
};

function checkPaginationSupported(recordStore) {
    console.log("Checking pagination support on recordstore : " + recordStore); 
    var url = "/portal/apis/analytics?type=18&recordStore=" + recordStore;
    $.getJSON(url, function(data) {
        if (data.status==="success") {
            if(data.message==="true" && datasourceType==="batch") {
                console.log("Pagination supported for recordstore: " + recordStore); 
                $("#btnPreview").show();
                isPaginationSupported = true;
            } else {
                $("#btnPreview").hide();
                isPaginationSupported = false;
            }
        }
    });
};

function fetchData(callback) {
    var timeFrom = new Date("1970-01-01").getTime();
    var timeTo = new Date().getTime();
    var request = {
        type: 8,
        tableName: $("#dsList").val(),
        timeFrom: timeFrom,
        timeTo: timeTo,
        start: 0,
        count: 10
    };
    $.ajax({
        url: "/portal/apis/analytics",
        method: "GET",
        data: request,
        contentType: "application/json",
        success: function(data) {
            var records = JSON.parse(data.message);
            previewData = makeRows(records);
            if (callback != null) {
                callback(previewData);
            }
        }
    });
};

function renderPreviewPane(rows) {
    $("#previewPane").empty();
    $('#previewPane').show();
    var table = jQuery('<table/>', {
        id: 'tblPreview',
        class: 'table table-bordered'
    }).appendTo('#previewPane');

    //add column headers to the table
    var thead = jQuery("<thead/>");
    thead.appendTo(table);
    var th = jQuery("<tr/>");
    columns.forEach(function(column, idx) {
        var td = jQuery('<th/>');
        td.append(column.name);
        td.appendTo(th);
    });
    th.appendTo(thead);

    rows.forEach(function(row, i) {
        var tr = jQuery('<tr/>');
        columns.forEach(function(column, idx) {
            var td = jQuery('<td/>');
            td.append(row[idx]);
            td.appendTo(tr);
        });

        tr.appendTo(table);

    });
};

function renderChartConfig() {
    //hide all chart controls
    $(".attr").hide();
    initCharts(columns);
};

function getColumnIndex(columnName) {
    for (var i = 0; i < columns.length; i++) {
        if (columns[i].name == columnName) {
            return i;
        }
    }
};

///////////////////////////////////// data formatting related functions ///////////////////////////////////////////////

function parseColumns(data) {
    if (data.columns) {
        var keys = Object.getOwnPropertyNames(data.columns);
        var columns = keys.map(function(key, i) {
            return column = {
                name: key,
                type: data.columns[key].type
            };
        });
        return columns;
    }
};

function makeRows(data) {
    var rows = [];
    for (var i = 0; i < data.length; i++) {
        var record = data[i];
        var row = [];
        for (var j = 0; j < columns.length; j++) {
            row.push("" + record.values[columns[j].name]);
        }
        rows.push(row);
    };
    return rows;
};





function makeMapDataTable(data) {
    var dataTable = new igviz.DataTable();
    if (columns.length > 0) {
        columns.forEach(function (column, i) {
            var type = "N";
            if (column.type == "STRING" || column.type == "string") {
                type = "C";
            }
            dataTable.addColumn(column.name, type);
        });
    }
    data.forEach(function (row, index) {
        for (var i = 0; i < row.length; i++) {
            if (dataTable.metadata.types[i] == "N") {
                data[index][i] = parseInt(data[index][i]);
            }
        }
    });
    dataTable.addRows(data);
    return dataTable;
};

var dataTable;
var chart;
var counter = 0;
var globalDataArray = [];
function drawRealtimeChart(data) {
    console.log("+++++++++++ drawRealtimeChart "); 
    $("#chartDiv").empty();
    var chartType = $("#chartType").val();


    if (chartType == "map") {
        var region = 5;
        if ($("#region").val().trim() != "") {
            region = $("#region").val();
        }
        var xAxis = getColumnIndex($("#xAxis").val());
        var yAxis = getColumnIndex($("#yAxis").val());
        var legendGradientLevel;
        if ($("#legendGradientLevel").val().trim() == ""){
            legendGradientLevel = 5;
        } else {
            legendGradientLevel = $("#legendGradientLevel").val();
        }
        var config = {
            "yAxis": yAxis,
            "xAxis": xAxis,
            "chartType": "map",
            "title": "Map By Country",
            "padding": 65,
            "width": document.getElementById("chartDiv").offsetWidth,
            "height": 240,
            "region": region,
            "legendGradientLevel": legendGradientLevel
        }
        if (counter == 0) {
            dataTable = makeMapDataTable(data);
            console.log(dataTable); 
            chart = igviz.draw("#chartDiv", config, dataTable);
            chart.plot(dataTable.data,null,0);
            counter++;
        } else {
            chart.update(data);
        }
    } else if (chartType === "arc") {
        //WARNING: very ugly code!!!! refactor this immediately
        var percentage = getColumnIndex($("#percentage").val());
        var config = { chartType: "arc", percentage: percentage};
        igviz.draw("#chartDiv",config,createDataTable(data));
    } else {
        dataTable = makeDataTable(data);
        if (counter == 0) {
            var xAxis = getColumnIndex($("#xAxis").val());
            var yAxis = getColumnIndex($("#yAxis").val());
            //console.log("X " + xAxis + " Y " + yAxis);

            var width = document.getElementById("chartDiv").offsetWidth;
            var height = 240; //canvas height
            var config = {
                "yAxis": yAxis,
                "xAxis": xAxis,
                "width": width,
                "height": height,
                "chartType": chartType
            }
            if (chartType === "bar" && dataTable.metadata.types[xAxis] === "N") {
                dataTable.metadata.types[xAxis] = "C";
            }
            chart = igviz.setUp("#chartDiv", config, dataTable);
            chart.setXAxis({
                "labelAngle": -35,
                "labelAlign": "right",
                "labelDy": 0,
                "labelDx": 0,
                "titleDy": 25
            })
                .setYAxis({
                    "titleDy": -30
                })
                .setDimension({
                    height: 270
                })

            globalDataArray.push(dataTable.data[0]);
            chart.plot(globalDataArray);
            counter++;
        } else if (counter == 5) {
            globalDataArray.shift();
            globalDataArray.push(dataTable.data[0]);
            chart.update(dataTable.data[0]);
        } else {
            globalDataArray.push(dataTable.data[0]);
            chart.plot(globalDataArray);
            counter++;
        }

    }

};

// function createDataTable(data) {
//     var realTimeData = new igviz.DataTable();
//     if (columns.length > 0) {
//         columns.forEach(function(column, i) {
//             var type = "N";
//             if (column.type == "STRING" || column.type == "string") {
//                 type = "C";
//             }
//             realTimeData.addColumn(column.name, type);
//         });
//     }
//     for (var i = 0; i < data.length; i++) {
//         realTimeData.addRow(data[i]);
//     }
//     return realTimeData;
// };

function makeDataTable(data) {
    var dataTable = new igviz.DataTable();
    if (columns.length > 0) {
        columns.forEach(function(column, i) {
            var type = "N";
            if (column.type == "STRING" || column.type == "string") {
                type = "C";
            }
            dataTable.addColumn(column.name, type);
        });
    }
    data.forEach(function(row, index) {
        for (var i = 0; i < row.length; i++) {
            if (columns[i].type == "FLOAT" || columns[i].type == "DOUBLE") {
                row[i] = parseFloat(row[i]);
            } else if (columns[i].type == "INTEGER" || columns[i].type == "LONG") {
                row[i] = parseInt(row[i]);
            }
        }
    });
    dataTable.addRows(data);
    return dataTable;
};