/* global confirm:false, alert:false, _:false */
/* jshint loopfunc: true */
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

angular.module('zeppelinWebApp').controller('loginCtrl', function($scope, $route, $routeParams, $location, $rootScope,
                                                                         $http, $base64, baseUrlSrv, loginSrv) {

  $scope.login = function() {

    var promise = loginSrv.login($scope.login.email, $scope.login.password);
    promise.then(function(user) {
      if (user != null) {
        $rootScope.ticket = {
          'principal':user.principal,
          'ticket':user.ticket
        };

        $location.path('/');    
      }
    }

    )
    
  }

});
