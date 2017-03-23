var retryIntervalInMilliseconds = 10000;
var numberOfRetries = 0;
var maxRetries = 30;

function reloadCommandCenterWhenDataReady() {
    getUser(function (user) {
        if (user["hasApplications"] === true) {
            location.reload(true);
        } else if (numberOfRetries < maxRetries) {
            numberOfRetries++;
            setTimeout(reloadCommandCenterWhenDataReady, retryIntervalInMilliseconds);
        }
    })
}

function getUser(callback) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
            var user = JSON.parse(xmlHttp.responseText);
            callback(user);
        }
    };
    xmlHttp.open("GET", "/user", true);
    xmlHttp.send(null);
}
