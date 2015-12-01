/**
 * Created by Chanuka on 11/28/15 AD.
 */



$.post( "/portal/controllers/apis/supportdashboard.jag",{
    action: "getCorrelatedSfJiraData"
}, function(data) {

        var result = JSON.parse(data);
        console.log(result[0].projectKey);
        $("#tableid").append("<tr><td> JIRA Support ID </td> <td > Customer Name </td> <td> Total Sold Hours </td> <td> Total Used Hours </td><td> Pending Hours </td> </tr>");

        for (var i=0; i<result.length; i++) {
            $("#tableid").append("<tr><td>" + result[i].projectKey + "</td><td>" + result[i].Line_Item_Customer__c +
            "</td><td>" + result[i].Dev_Support_hours + "</td><td>" + result[i].hoursConsumed +
            "</td><td>" + result[i].pending_hours + "</td></tr>");

}

});







