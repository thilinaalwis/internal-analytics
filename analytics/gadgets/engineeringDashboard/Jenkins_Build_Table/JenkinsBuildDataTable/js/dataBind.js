$.post("/portal/controllers/apis/JenkinsBuildData.jag", {
    action: "getData"
}, function(data) {

    //Get the data from hosted jaggery file and parse it 
    var result = JSON.parse(data);
    console.log("----------------------------------start----------------------------");

    var sortOrder = ['Failed', 'Aborted', 'Success', 'null']; //Table sorting order
    var sortOrder2 = ['Stormy', 'Rainy', 'Cloudy', 'PartlyCloudy', 'Sunny']; //Table sorting order
    //function to sort the build status
    var firstBy = function() {
        function n(n, t) {
            if ("function" != typeof n) {
                var r = n;
                n = function(n, t) {
                    return n[r] < t[r] ? -1 : n[r] > t[r] ? 1 : 0
                }
            }
            return -1 === t ? function(t, r) {
                return -n(t, r)
            } : n
        }

        function t(t, u) {
            return t = n(t, u), t.thenBy = r, t
        }

        function r(r, u) {
            var f = this;
            return r = n(r, u), t(function(n, t) {
                return f(n, t) || r(n, t)
            })
        }
        return t
    }();

    //Sorting last 07 days' build status to the order of failed,aborted and success
    result = result.sort(
    firstBy(function(v1, v2) {
        return sortOrder.indexOf(v1.Current.Status) - sortOrder.indexOf(v2.Current.Status);
    })
        .thenBy(function(v1, v2) {
        return sortOrder.indexOf(v1.Day07.Status) - sortOrder.indexOf(v2.Day07.Status);
    })
        .thenBy(function(v1, v2) {
        return sortOrder.indexOf(v1.Day06.Status) - sortOrder.indexOf(v2.Day06.Status);
    })
        .thenBy(function(v1, v2) {
        return sortOrder.indexOf(v1.Day05.Status) - sortOrder.indexOf(v2.Day05.Status);
    })
        .thenBy(function(v1, v2) {
        return sortOrder.indexOf(v1.Day04.Status) - sortOrder.indexOf(v2.Day04.Status);
    })
        .thenBy(function(v1, v2) {
        return sortOrder.indexOf(v1.Day03.Status) - sortOrder.indexOf(v2.Day03.Status);
    })
        .thenBy(function(v1, v2) {
        return sortOrder.indexOf(v1.Day02.Status) - sortOrder.indexOf(v2.Day02.Status);
    })
        .thenBy(function(v1, v2) {
        return sortOrder.indexOf(v1.Day01.Status) - sortOrder.indexOf(v2.Day01.Status);
    })
        .thenBy(function(v1, v2) {
        return sortOrder2.indexOf(v1.WeeklyWeather.Weather) - sortOrder2.indexOf(v2.WeeklyWeather.Weather);
    })
        .thenBy(function(v1, v2) {
        return sortOrder2.indexOf(v1.MonthlyWeather.Weather) - sortOrder2.indexOf(v2.MonthlyWeather.Weather);
    }));

    //console.log(JSON.stringify(result));

    //Last 6 days
    var date = [];
    var currentDate = new Date();
    for (var i = 0; i < 7; i++) {
        currentDate.setDate(currentDate.getDate() - 1);
        date.push(new Date(currentDate).toString());
    }
    //console.log(date);
    var Title01 = JSON.stringify(result[i].MonthlyWeather.Percentage).substring(0, 4);


    $("#report").append("<tr><th> Product </th><th >" + date[6].substring(3, 10) + "</th> <th >" + date[5].substring(3, 10) + "</th> <th>" + date[4].substring(3, 10) + "</th> <th>" + date[3].substring(3, 10) + "</th><th>" + date[2].substring(3, 10) + "</th><th>" + date[1].substring(3, 10) + "</th><th>" + date[0].substring(3, 10) + "</th><th> Current </th><th> Weekly </th><th> Monthly </th><th></th></tr>");
    for (var i = 0; i < result.length; i++) {
        $("#report").append("<tr><td>" + result[i].product + "</td><td><img height='20' width='20' title=" + result[i].Day01.Component + " src='/portal/store/carbon.super/gadget/JenkinsBuildDataTable/images/" + result[i].Day01.Status + ".png' /></td><td><img height='20' width='20' title=" + result[i].Day02.Component + " src='/portal/store/carbon.super/gadget/JenkinsBuildDataTable/images/" + result[i].Day02.Status + ".png' /><td><img height='20' width='20' title=" + result[i].Day03.Component + " src='/portal/store/carbon.super/gadget/JenkinsBuildDataTable/images/" + result[i].Day03.Status + ".png' /></td><td><img height='20' width='20' title=" + result[i].Day04.Component + " src='/portal/store/carbon.super/gadget/JenkinsBuildDataTable/images/" + result[i].Day04.Status + ".png' /></td><td><img height='20' width='20' title=" + result[i].Day05.Component + " src='/portal/store/carbon.super/gadget/JenkinsBuildDataTable/images/" + result[i].Day05.Status + ".png' /></td><td><img height='20' width='20' title=" + result[i].Day06.Component + " src='/portal/store/carbon.super/gadget/JenkinsBuildDataTable/images/" + result[i].Day06.Status + ".png' /></td><td><img height='20' width='20' title=" + result[i].Day07.Component + " src='/portal/store/carbon.super/gadget/JenkinsBuildDataTable/images/" + result[i].Day07.Status + ".png' /></td><td><img height='20' width='20' title=" + result[i].Current.Component + " src='/portal/store/carbon.super/gadget/JenkinsBuildDataTable/images/" + result[i].Current.Status + ".png' /></td><td><img height='24' width='24' title="+"Success:&nbsp;"+ JSON.stringify(result[i].WeeklyWeather.Percentage).substring(0, 4) + "%" +" src='/portal/store/carbon.super/gadget/JenkinsBuildDataTable/images/" + result[i].WeeklyWeather.Weather + ".png' /></td><td><img height='24' width='24' title="+"Success:&nbsp;" + JSON.stringify(result[i].MonthlyWeather.Percentage).substring(0, 4) + "%" +" src='/portal/store/carbon.super/gadget/JenkinsBuildDataTable/images/" + result[i].MonthlyWeather.Weather + ".png' /></td><td><div class='arrow'></div></td></tr>");
        $("#report").append("<tr class='expandable'><td colspan='12'><div>Currently Failed Components :       " + result[i].FailedDetails.Component + "<br>"+"   Failed Duration :       " + result[i].FailedDetails.Duration + "</div></td></tr>")
    }

    $('#report').on('click', 'tr:nth-child(even)', function() {
        $(this).next().toggleClass('expand');
    });

    $(document).ready(function() {
        $("#report tr:odd").addClass("odd");
        $("#report tr:not(.odd)").hide();
        $("#report tr:first-child").show();

        $("#report tr.odd").click(function() {
            $(this).next("tr").toggle();
            $(this).find(".arrow").toggleClass("up");
        });
        //$("#report").jExpand();
    });
});