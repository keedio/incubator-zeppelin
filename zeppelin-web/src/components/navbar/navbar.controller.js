/* global $:false */
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

angular.module('zeppelinWebApp')
.run(function($http, LS, $base64) {
  
  var encoded = $base64.encode(LS.getData('userInfo.principal') + ":" + LS.getData('userInfo.password'));
  $http.defaults.headers.common.Authorization = 'Basic ' + encoded;
})
.controller('NavCtrl', function($scope, $rootScope, $routeParams, notebookListDataFactory, websocketMsgSrv, arrayOrderingSrv, $http, baseUrlSrv, LS, $location, $route) {
 
  /** Current list of notes (ids) */

  var vm = this;
  vm.notes = notebookListDataFactory;
  vm.connected = websocketMsgSrv.isConnected();
  vm.websocketMsgSrv = websocketMsgSrv;
  vm.arrayOrderingSrv = arrayOrderingSrv;

  //$('#notebook-list').perfectScrollbar({suppressScrollX: true});
  $scope.logout = function() {
    LS.clear();
    delete $rootScope.ticket;
    delete $http.defaults.headers.common.Authorization;
    $http.get(baseUrlSrv.getRestApiBase()+'/security/ticket');
  }

  $scope.$on('setNoteMenu', function(event, notes) {
    notebookListDataFactory.setNotes(notes);
  });

  $scope.$on('setConnectedStatus', function(event, param) {
    vm.connected = param;
  });

  function loadNotes() {
    websocketMsgSrv.getNotebookList();
  }

  /** ask for a ticket for websocket access
   * Shiro will require credentials here
   * */
  $http.get(baseUrlSrv.getRestApiBase() + '/security/ticket').
    success(function(ticket, status, headers, config) {
      $rootScope.ticket = angular.fromJson(ticket).body;
      vm.loadNotes = loadNotes;
      vm.isActive = isActive;

      vm.loadNotes();
    }).
    error(function(data, status, headers, config) {
      console.log('Could not get ticket');
    });

  function isActive(noteId) {
    return ($routeParams.noteId === noteId);
  }

});
