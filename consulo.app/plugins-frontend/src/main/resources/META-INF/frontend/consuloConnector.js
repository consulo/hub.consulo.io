window.connectToConsulo = function(element) {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (xhttp.readyState == 4) {
      const body = xhttp.status == 200 ? xhttp.responseText : "";

      element.$server.handleConsuloResponse(xhttp.readyState, body);
    }
  };
  xhttp.open("GET", "http://localhost:62242/api/about");
  xhttp.setRequestHeader("Content-Type", "application/json");
  xhttp.send();
}