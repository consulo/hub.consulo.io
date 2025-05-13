window.connectToConsulo = function(url, element) {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (xhttp.readyState == 4) {
      const body = xhttp.status == 200 ? xhttp.responseText : "";

      element.$server.handleConsuloResponse(xhttp.readyState, body);
    }
  };
  xhttp.open("GET", url);
  xhttp.setRequestHeader("Content-Type", "application/json");
  xhttp.send();
}

window.installPluginToConsulo = function(url, element) {
  var xhttp = new XMLHttpRequest();
  xhttp.open("GET", url);
  xhttp.setRequestHeader("Content-Type", "application/json");
  xhttp.send();
}