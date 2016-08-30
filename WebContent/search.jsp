<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Search Keyword from twitter!</title>
<script type="text/javascript" src="./js/jquery-3.1.0.js"></script>
<link rel="stylesheet" type="text/css" href="./style/layout.css" />
<link rel="stylesheet" type="text/css" href="./style/style.css" />
<script>
$('document').ready(function() {
	$('#word').hide();
	$('#search-word').keydown(function(e) {
		var code = e.keyCode || e.which;
		if(code == 9) {
			e.preventDefault();
			add_word();
		} else if (code == 13) {
			e.preventDefault();
			search_word();
		}
	});
});

var words = [];

var addflag = true;

function add_word(){
	var newword = $('#search-word').val();
	
	
	if(newword == "") {
		alert('단어를 입력해주세요');
		
	} else {
		var word = $('#words').children('span').first();
		while(word.next().length > 0) {
			word = word.next();
			console.log(word.text());
			console.log(newword);
			if(word.text() == newword) {
				alert('이미 존재하는 단어입니다.');
				addflagy = false;
				break;
			} 
		}
		if(addflag) {
			var span = $('#word').clone();
			span.attr('id', newword);
			span.html(newword);
			span.on('click', function() {//word remove
				if(confirm($(this).text()+'을(를) 삭제할까요?')) {
					$(this).remove();
				}
			});
		}
	}
	
	$('#search-word').val("");
	$('#words').append(span.show());
	$('#search-word').blur(); 
	$('#search-word').focus();
}

function search_word() {
	var word = $('#words').children('span').first();
	while(word.next().length > 0){
		word = word.next();
		words.push(word.text());
	}
 	if(words.length < 1) {
		alert('단어를 입력한 후 검색해주세요!');
	} else {
		$('#search_words').attr('value', words);
		$('form').submit(); 
	} 
}
</script>
</head>
<body>
	<div class="wrapper">
		<h2>keyword 검색하기</h2>
		<div id="words">
			<p>keywords</p>
			<span class='span_word' id="word">example word</span>
		</div>
		<div id="contents">
			<form action="wordcloud.jsp" method="post" id="form_words" method="post">
				<p>Press 'Tab' to add words and press 'Enter' to search hashtag!</p>
				<input type="text" id="search-word" size="40" />
				<input type="hidden" name='search' id="search_words" />
			</form>
		</div>
		<div id="footer">
			<p>Copyrightⓒ2016 by SuJeongLee</p>
		</div>
	</div>
</body>
</html>