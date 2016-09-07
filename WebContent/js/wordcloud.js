$(document).ready(function() {

	var WIDTH = 800, HEIGHT = 600;
	
	var fill = d3.scale.category20b();
 	
	var data = [ {word: "Hello", weight: 22},
	             {word: "Begin Again", weight: 60},
	             {word: "Sing Street", weight: 35},
	             {word: "Notebook", weight: 18},
	             {word: "AboutTime", weight: 10},
	             {word: "Jason Bourn", weight: 42},
	             {word: "Mission Impossible", weight: 24},
	             {word: "GodFather", weight: 25},
	             {word: "Up", weight: 14},
	             {word: "Gattaca", weight: 10},
	             {word: "Good Will Hunting", weight: 16},
	             {word: "d3js", weight: 22},
	             {word: "Lion King", weight: 35},
	             {word: "GalaxyNote3", weight: 18},
	             {word: "usbdisk", weight: 10},
	             {word: "sandisk", weight: 13},
	             {word: "STARBUCKS", weight: 31},
	             {word: "Redondo", weight: 16},
	             {word: "taylors", weight: 14},
	             {word: "AHMAD", weight: 10},
	             {word: "Winter Charm", weight: 16},
	             {word: "fruit herb", weight: 60},
	             {word: "black tea", weight: 35},
	             {word: "greenfield", weight: 18},
	             {word: "Orange Blossom", weight: 10},
	             {word: "English Breakfast", weight: 36},
	             {word: "Cylon", weight: 22},
	             {word: "TWININGS", weight: 43},
	             {word: "LadyGrey", weight: 14},
	             {word: "EarlGrey", weight: 10},
	             {word: "Rooibos", weight: 16},
	             {word: "Ninas", weight: 22},
	             {word: "Mixed Berries", weight: 35},
	             {word: "Once", weight: 18},
	             {word: "Natural", weight: 10},
	             {word: "Peppermint", weight: 15},
	             {word: "GreenTea", weight: 17},
	             {word: "Vanila", weight: 42},
	             {word: "Chocolate", weight: 14},
	             {word: "coffee", weight: 10},
	             {word: "Blur", weight: 16},
	             {word: "Oasis", weight: 30} ];
	
	data = data.slice();
	
	var scale = d3.scale.linear()
		.domain(d3.extent(data, function(d) { return d.weight; }))
		.range([15,60]);
	
	var layout = d3.layout.cloud()
			.timeInterval(Infinity)
			.size([WIDTH, HEIGHT])
			.padding(0)
			.font("Impact")
			.fontSize(function(d) { return d.size; })
			.text(function(d) { return d.text; })
			.on("end", draw);
	
	var svg = d3.select("#word-cloud").append("svg").attr("width", WIDTH).attr("height", HEIGHT);

	var wordcloud = svg.append("g").attr("transform", "translate(" + [WIDTH>>1, HEIGHT>>1] + ")");
	
	update();
	
	$('#addBtn').on('click', function() {
		
		//웹소켓에서 받아온 데이터 계산->data update->다시그려주기
		//input가정
		var input = '{"Hello":3, "AboutTime":2, "Sing Street":5, "Mission Impossible":9, "Notebook":1}';
		
		var tmp = JSON.parse(input);

		var keyset = [];
		var key, hashtag, weight;

		for (key in tmp) {
			_word = key;
			_weight = tmp[key];

			if (data.length > 0) {

				var idx = -1;// 
				for (var i = 0; i < data.length; i++) {
					if (data[i].word == _word) {idx = i; break;}
				}

				if (idx === -1) {// 새로운 단어일 때 데이터에 추가
					data.push({word : _word, weight : _weight});
				} else {// 이미 존재하는 단어일 때 weight변경
					data[idx].weight = data[idx].weight + _weight;
				}
			} else {// 데이터 배열이 비어있을 때
				data.push({word : _word, weight : _weight});
			}
		}
		scale = d3.scale.linear()
		.domain(d3.extent(data, function(d) { return d.weight; }))
		.range([15,60]);
		
		update();
	});
	
	$('#removeBtn').on('click', function() {
		data.pop();
		update();
	});
	
	
	
	function draw(newdata, bounds) {

		wordcloud.attr("width", WIDTH).attr("height", HEIGHT);
		
		var text = wordcloud.selectAll("text")
				.data(newdata, function(d) { return d.text; });
		
		text.transition().duration(1000) //already exist data
		.attr("transform", function(d) { return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")"; })
		.style("font-size", function(d) { return d.size + "px"; });

		text.enter().append("text") //append new data
			.text(function(d) { return d.text; })
			.style("fill", function(d, i) { return fill(i); })
			.attr("text-anchor", "middle")
			.style("font-family", "Impact")
			.style("font-size", function(d) { return d.size + "px"; })  //size need scaling
			.attr("transform", function(d) { return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")"; })
			.style("opacity", 1e-6)
			.transition().duration(1000)
			.style("opacity", 1); 
		
		text.exit().remove(); // remove text that doesn't exist in data
	}

	function update() {
	    layout.spiral('archimedean');
	    
	    layout
	    	.stop()
	    	.words(data.map(function(d) { return { text:d.word, size:scale(d.weight) }; }))
	    	.start();
	}
	
});