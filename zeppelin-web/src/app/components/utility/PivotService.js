angular.module('app.module.utility')
.service('PivotService', function ( _) {

  this.crossData = function (data) {
    // Returns a JSON by crossing data with key
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

  	return all;
  };

  this.getColumn = function(data, column) {
    var arr = [];
    _.each(data, function(record) {
      arr.push(record[column]);
    })

    return arr;
  }

  this.validateType = function(data, column, type) {
    _.each(data, function(record) {
      switch (type) {
        case 'string':
          if (!_.isString(record[column])) 
            throw "Column " + column + " has no strings";
          break;
        case 'number':
          if (!_.isNumber(record[column])) 
            throw "Problem with column " + column;
          break;
      }
    })
  }

});
