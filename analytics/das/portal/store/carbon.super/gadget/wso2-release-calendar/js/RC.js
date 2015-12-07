var result = [];// Array to store JSON objects
var today = new Date();// get today date
var begin = new Date();
begin.setDate(begin.getDate() - 56);// define the begining of the time period
var end = new Date();
end.setDate(end.getDate() + 112);// define the end of the time period
$.post("/portal/controllers/apis/rc.jag", {
	action : "GET"
},
		function(datax) {
			// Get the data from hosted jaggery files and parse it
			var data = JSON.parse(datax);
			// go through the each JSON object
			for ( var i in data) {
				var item = data[i];
				// check the product is not released ? and the due_date is in
				// the specified time range?
				if (item.status === "open"
						&& new Date(item.due_date).getTime() >= new Date(begin)
								.getTime()
						&& new Date(item.due_date).getTime() <= new Date(end)
								.getTime()) {
					// check due_date is expired or not
					if (new Date(item.due_date).getTime() < new Date(today)
							.getTime()) {
						result.push({
							"name" : item.Project_Name,
							"desc" : item.version_name,
							"values" : [ {
								"from" : "/Date("
										+ new Date(item.due_date).getTime()
										+ ")/",
								"to" : "/Date(" + today.getTime() + ")/",
								"lable" : " ",
								"customClass" : "ganttRed"
							} ]
						});
					} else {

						result.push({
							"name" : item.Project_Name,
							"desc" : item.version_name,
							"values" : [ {
								"from" : "/Date("
										+ new Date(item.due_date).getTime()
										+ ")/",
								"to" : "/Date("
										+ new Date(item.due_date).getTime()
										+ ")/",
								"lable" : " ",
								"customClass" : "ganttGreen"
							} ]
						});
					}

				}

			}

		});

function getdata() {

	$(".gantt").gantt({
		source : result,
		navigate : "scroll",
		scale : "weeks",
		minScale : "months",
		maxScale : "months",
		itemsPerPage : 100,

		onRender : function() {
			if (window.console && typeof console.log === "function") {
				console.log("chart rendered");
			}
		}
	});

};

