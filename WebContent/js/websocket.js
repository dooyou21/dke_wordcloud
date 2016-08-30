
function websocket_init() {
	if('WebSocket' in window) {
		var host="ws://localhost:8080/d3js/websocket";
		var socket = new WebSocket(host);
		
		socket.oneror = function(event) {
			//err
		};
		
		socket.onopen = function() {
			//open
			console.log("opened");
			
			socket.send("<%=keyword%>");
		};
		
		socket.onmessage = function(event) {
			//message
			console.log("message received: "+event.data);
		};
		
		socket.onclose = function() {
			//close
			console.log("closed");
		};
	} else {
		alert("websocket not supported in this browser!");
	}
}