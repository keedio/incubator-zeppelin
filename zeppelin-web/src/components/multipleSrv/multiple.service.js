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

angular.module('zeppelinWebApp').service('multipleSrv', function( _, pivotSrv) {
  this.addNums = function (text) {

    return text + "123";
  };
  
  this.suma = function (a,b) {
  	console.log(_.isString(""))
    return a+b;
  };

  this.draw = function (data, id, xserie, yseries) {
    // Construimos el json para el grafico
    var obj = {};
    obj.bindto = id;
    obj.data = {};
    obj.data.types = {};
    obj.data.x = xserie.name;
    obj.data.columns = [];
    var xarr = [xserie.name];
    Array.prototype.push.apply(xarr, pivotSrv.getColumn(data, xserie.name));
    obj.data.columns.push(xarr);
    _.each(yseries, function(record) {
      var aux = [record.name];
      Array.prototype.push.apply(aux, pivotSrv.getColumn(data, record.name));
      obj.data.columns.push(aux);
      obj.data.types[record.name] = record.type;
    })

    c3.generate(obj);
  };

  this.undraw = function (id) {
    d3.select('#' + id).selectAll("*").remove();    
  }

});
