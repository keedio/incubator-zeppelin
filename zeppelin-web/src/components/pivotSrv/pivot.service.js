/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

angular.module('zeppelinWebApp').service('pivotSrv', function( _) {

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
