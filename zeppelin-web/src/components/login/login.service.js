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

angular.module('zeppelinWebApp').service('loginSrv', function($http, $base64, baseUrlSrv, $q, LS) {
  this.getUser = function() {
  	var obj = new Object();
  	obj['principal'] = LS.getData('userInfo.principal');
	obj['password'] = LS.getData('userInfo.password');
	obj['ticket'] = LS.getData('userInfo.pticket');
  	
  	return obj;
  }

  this.login = function(user, password) {
	
	var deferred = $q.defer();

  	if (user.ticket == undefined) {

	    var encoded = $base64.encode(user + ":" + password);
    	$http.defaults.headers.common.Authorization = 'Basic ' + encoded;
    	$http.defaults.cache = false;	    	
	    
	    $http.get(baseUrlSrv.getRestApiBase()+'/security/ticket').
	    success(function(ticket, status, headers, config) {
	      var userObj = new Object();
	      var ticket = angular.fromJson(ticket).body;
	      userObj['principal'] = user;
	      userObj['password'] = password;
	      userObj['ticket'] = ticket.ticket;

	      LS.setData('userInfo.principal', user);
	      LS.setData('userInfo.password', password);
	      LS.setData('userInfo.pticket', ticket.ticket);
	      deferred.resolve(userObj);
	    }).
	    error(function(msg, code) {
	      console.log('Could not get ticket');
	      deferred.reject(msg);
	    });
  	} else {
  		deferred.resolve(userObj);
  	}

  	return deferred.promise;

  }

  this.logout = function() {
	
	var deferred = $q.defer();
    
    $http.get(baseUrlSrv.getRestApiBase()+'/security/logout').
    	success(function(ticket, status, headers, config) {
      		LS.clear();
      		deferred.resolve();
    	}).
    	error(function(msg, code) {
      		console.log('Could not logout');
      		deferred.reject(msg);
    	}
    );
	
	return deferred.promise;
	
  }

});
