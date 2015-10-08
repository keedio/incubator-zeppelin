angular.module('app.module.leaflet')
.service('LeafletService', function ( _) {
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

  this.draw = function (data, id, latStr, lonStr, valueStr, textStr) {
    d3.select(id + ' div').remove();
    d3.select(id).append('div').style({width: '500px', height: '300px', overflow: 'hidden'});
    var div = d3.select(id + ' div').node();
    var map = L.map(div).setView([51.505, -0.09], 13);

    // add base map tiles from OpenStreetMap and attribution info to 'map' div
    L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    // Recorremos todos los datos pintando los elementos
    _.each(data, function(record){
      var icon = L.MakiMarkers.icon({icon: "rocket", color: "#b0b", size: "m"});
      L.marker([record[latStr], record[lonStr]], {icon: icon}).
      addTo(map).
      bindPopup('<strong>' + record[valueStr] +'</strong><br>' + record[textStr]).
      openPopup();
    })
  };

  this.undraw = function (id) {
    d3.select('#' + id).selectAll("*").remove();    
  }

});
