angular.module('app.util')
.service('utilService', function () {
  this.addNums = function (text) {
    return text + "123";
  };
  this.suma = function (a,b) {
    return a+b;
  }  
});
