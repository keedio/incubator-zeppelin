angular.module('app.module.multiple')
.service('MultipleService', function ( _, PivotService) {
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
    Array.prototype.push.apply(xarr, PivotService.getColumn(data, xserie.name));
    obj.data.columns.push(xarr);
    _.each(yseries, function(record) {
      var aux = [record.name];
      Array.prototype.push.apply(aux, PivotService.getColumn(data, record.name));
      obj.data.columns.push(aux);
      obj.data.types[record.name] = record.type;
    })

    c3.generate(obj);
  };

  this.undraw = function (id) {
    d3.select('#' + id).selectAll("*").remove();    
  }

});
