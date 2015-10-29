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

angular.module('zeppelinWebApp').controller('MainCtrl', function($scope, $rootScope, $window) {
  $rootScope.compiledScope = $scope.$new(true, $rootScope);
  $scope.looknfeel = 'default';
  
  // set the default theme     
  if (localStorage.getItem("lsCSS") === undefined || localStorage.getItem("lsCSS") === null) {
    $rootScope.newCSS = 'kata';
  }
  else {
    $rootScope.newCSS = localStorage.getItem("lsCSS");
  };   
  
  // create the list of themes. Same name that css file.  
  $rootScope.newStyles = [  
    { name: 'default', url: 'default', arrayColors: []},  
    { name: 'keedio', url: 'keedio', arrayColors: ['#87BAAC', '#666484', '#55A08E', '#352B5B']},
    { name: 'kata', url: 'kata', arrayColors: ['#573C84', '#B3A3D1', '#3E2E60', '#896FCD', '#837D93', '#7C2DAA']},
    { name: 'produban', url: 'produban', arrayColors: ['#C02844', '#58212A', '#88182A', '#D284A1']}    
  ];

  var init = function() {
    $scope.asIframe = (($window.location.href.indexOf('asIframe') > -1) ? true : false);
  };
  
  init();

  $rootScope.$on('setIframe', function(event, data) {
    if (!event.defaultPrevented) {
      $scope.asIframe = data;
      event.preventDefault();
    }
  });

  $rootScope.$on('setLookAndFeel', function(event, data) {
    if (!event.defaultPrevented && data && data !== '' && data !== $scope.looknfeel) {
      $scope.looknfeel = data;
      event.preventDefault();
    }
  });
  
  // Set The lookAndFeel to default on every page
  $rootScope.$on('$routeChangeStart', function(event, next, current) {
    $rootScope.$broadcast('setLookAndFeel', 'default');
  });

  $rootScope.setNewCSS = function(newCSS) {
    $rootScope.newCSS = newCSS;  
    localStorage.setItem("lsCSS", $rootScope.newCSS);    
    $window.location.reload();
  };

});
