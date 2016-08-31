<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
	String keyword = request.getParameter("search");
	String[] keywords = keyword.split(",");
	int length = keywords.length;
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>D3js Tutorial</title>
<script type="text/javascript" src="./js/jquery-3.1.0.js"></script>
<script type="text/javascript" src="./js/d3.min.js"></script>
<script type="text/javascript" src="./js/d3.layout.cloud.js"></script>
<script type="text/javascript" src="./js/wordcloud.js"></script>
<script type="text/javascript" src="./js/websocket.js"></script>
<script>
$('document').ready(function () {
	websocket_init();
	ui_init();
});

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

function ui_init() {
<%
	for(int i=0;i<length;++i) {
%>	
		$('#header').append("<span class='span_word' id='<%=keywords[i] %>'><%=keywords[i] %> </span>");
<%
	}
%>
}
</script>
<link rel="stylesheet" type="text/css" href="./style/layout.css" />
<link rel="stylesheet" type="text/css" href="./style/style.css" />
</head>
<body>
	<div class="wrapper">
		<div id="header">
		
		</div>
			<div>
				<button id="addBtn">Add data!</button>
				<button id="removeBtn">Remove data!</button>
			</div>
		<div id="contents">
		 	<div id="word-cloud"></div>
		</div>
	 	<div id="footer">
	 		<p>Copyrightⓒ2016 by SuJeongLee</p>
	 	</div>
	</div>
</body>
</html>