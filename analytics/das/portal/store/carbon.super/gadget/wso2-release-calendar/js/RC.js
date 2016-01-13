var result = [];
var future_result=[];
var updated_result=[];
var today=new Date();

$.post( "/portal/controllers/apis/rc.jag",{action:"GET"}, function(datax) {
    //Get the data from hosted jaggery files and parse it
    var data = JSON.parse(datax);

    for(var i in data) {

        var item = data[i];
        //remove the GA tag
        var version= item.version_name.replace("- GA",' ').replace("-GA"," ");

        // check the expired due dates and push to an array
        if(new Date(item.due_date).getTime()<new Date(today).getTime())
        {
            result.push({
                "name" : item.Project_Name,
                "desc"  : version,
                "values" : [{
                    "from":"/Date(" + new Date(item.due_date).getTime() + ")/",
                    "to":"/Date("+ today.getTime() + ")/",
                    "lable":"Not Updated",
                    "customClass": "ganttRed"
                }]
            });
        }

        else
        {
            // push future releases data in to an array
            if(item.old_due_dates.length===0){

                future_result.push({
                    "name" : item.Project_Name,
                    "desc"  : version,
                    "values" : [{
                        "from":"/Date(" + new Date(item.due_date).getTime() + ")/",
                        "to":"/Date(" + new Date(item.due_date).getTime() + ")/",
                        "lable":" ",
                        "customClass": "ganttGreen"
                    }]
                });
            }



            else
            {
                // push updated releases in to an array
                var values=[];
                var dates=item.old_due_dates;
                dates[dates.length]=item.due_date;

                for(var l=0;l<dates.length-1;l++)
                {

                    values.push({
                            "from":"/Date(" + new Date(dates[l]).getTime() + ")/",
                            "to":"/Date(" + new Date(dates[l+1]).getTime() + ")/",
                            "lable":" ",
                            "customClass": "ganttOrange"
                        },

                        {"from":"/Date(" + new Date(dates[l]).getTime() + ")/",
                            "to":"/Date(" + new Date(dates[l]).getTime() + ")/",
                            "lable":" ",
                            "customClass": "ganttRed"}
                    );

                }

                values.push({
                        "from":"/Date(" + new Date(dates[dates.length-1]).getTime() + ")/",
                        "to":"/Date(" + new Date(dates[dates.length-1]).getTime() + ")/",
                        "lable":" ",
                        "customClass": "ganttGreen"
                    }
                );


                updated_result.push({
                    "name" : item.Project_Name,
                    "desc"  : version,
                    "values" : values
                });

            }

        }
    }
    // concat above 3 arrays in to one array
    var temp=result.concat(updated_result);
    temp=temp.concat(future_result);
    //bind the data
    getdata(temp);

});


function getdata(res) {
    $(".gantt").gantt({
        source:res,
        navigate: "scroll",
        scale:"weeks",
        minScale:"months",
        maxScale:"months",
        itemsPerPage: 16,
        onRender: function() {

            if (window.console && typeof console.log === "function") {
                console.log("chart rendered");
            }
        }
    });
    
};


