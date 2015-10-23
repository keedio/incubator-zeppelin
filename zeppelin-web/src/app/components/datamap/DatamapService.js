angular.module('app.module.datamap')
.service('DatamapService', function ( _) {
  this.addNums = function (text) {

    return text + "123";
  };
  
  this.suma = function (a,b) {
  	console.log(_.isString(""))
    return a+b;
  };

  this.validateData = function(keys, values) {
    // Comprobamos que los datos de entrada sean
    var areStringKeys = _.map(keys, function(val){return _.isString(val)});
    var allStringKeys = _.reduce(areStringKeys, function(memo, val) {return memo&&val;}, true);

    if (!allStringKeys) throw "Keys are not strings"

    // Comprobamos que los datos de entrada sean
    var areNumberValues = _.map(values, function(val){return _.isNumber(val)});
    var allNumberValues = _.reduce(areNumberValues, function(memo, val) {return memo&&val;}, true);

    if (!allNumberValues) throw "Values are not numbers"

  }

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

  this.draw = function (data, id) {
    this.undraw(id)
    var div = document.getElementById(id);
    div.style.width = '600px';
    div.style.height = '300px';
    var map = new Datamap({
      element: div,
      scope: 'world',
      options: {
        legendHeight: 40 // optionally set the padding for the legend
      },
      geographyConfig: {
        highlighBorderColor: '#EAA9A8',
        highlighBorderWidth: 2
      },
      data: data
    })
  };

  this.undraw = function (id) {
    d3.select('#' + id).selectAll("*").remove();    
  }
});
