angular.module('app.util')
.service('DatamapsService', function ( _) {
  this.addNums = function (text) {

    return text + "123";
  };
  this.suma = function (a,b) {
  	console.log(_.isString(""))
    return a+b;
  };
  this.getData = function (data, key, value) {
  	var all = [];

  	// Construimos el JSON con los datos de la cabecera
  	var headers = data.columnNames;
  	_.each(data.rows, function(record) {
  		var item = new Object();
  		for (var i=0;i<headers.length;i++){
  			item[headers[i].name] = record[i];
  		}
  		all.push(item);
  	});
  	var min = parseInt(_.min(all, function(record){return parseInt(record[value])})[value]);
  	var max = parseInt(_.max(all, function(record){return parseInt(record[value])})[value]);
  	var paletteScale = d3.scale.linear()
            .domain([min,max])
            .range(["#EFEFFF","#02386F"]); 
  	var dataset = {};
  	// Filtramos los datos de los key-value
  	_.each(all, function(record){
		dataset[record[key]] =  { value: record[value], fillColor: paletteScale(record[value]) }; 
  	});

  	return dataset;
  };

  this.draw = function (data, paragraph) {
    var chartEl = document.getElementById('p'+ paragraph +'_mapChart')
    var map = new Datamap({
  		scope: 'world',
  		options: {
    		width: 1110,
    		legendHeight: 60 // optionally set the padding for the legend
  		},
  		geographyConfig: {
    		highlighBorderColor: '#EAA9A8',
      		highlighBorderWidth: 2
  		},
        element: chartEl,
        data: data
    });
  };
});
