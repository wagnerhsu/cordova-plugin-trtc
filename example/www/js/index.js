/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    // deviceready Event Handler
    //
    // Bind any cordova events here. Common events are:
    // 'pause', 'resume', etc.
    onDeviceReady: function() {
        this.receivedEvent('deviceready');
    },

    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');
        var startConference = parentElement.querySelector('#startConference');
        var startWithWeb = parentElement.querySelector('#startWithWeb');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        startConference.addEventListener('click', this.startConference.bind(this), false);
        startWithWeb.addEventListener('click', this.startWithWeb.bind(this), false);
        document.getElementById('name').value = 'u' + Math.floor((Math.random() * 1000) + 1);

        console.log('Received Event: ' + id);
    },
    startConference: function() {
        this.start(false);
    },
    start: function(isWithWeb) {
        if (typeof TRTC == 'undefined') {
            alert('TRTC plugin not found');
            return;
        }
        var appId = '1400547367';
        TRTC.init({
            sdkappid: appId,
            user_info_url: 'http://your.domain.com/api/<USER_ID>', //提供参会者信息的，RESTful JSON接口，<USER_ID>会被替换成实际值，返回用户信息的结构为 { "name":"foo", "avatar":"http://your.domain.com/avatar.jpg" }
        });
        var roomId = document.getElementById('room').value;
        var userId = "u001";
        var userSig = "eJyrVgrxCdYrSy1SslIy0jNQ0gHzM1NS80oy0zLBwqUGBoZQ8eKU7MSCgswUJStDEwMDUxNzYzNziExqRUFmUSpQ3NTU1MjAwAAiWpKZCxIzMzKzMLE0MzSBmpKZDjQ2rzyp1Cco2dlXOzzM3T3RzDPVOyk8NDs11LEs3MI8qqgw0NLcvCDFoiTCxFapFgCdijBH";

        TRTC.enterRoom(userId, userSig, roomId);
    }

};

app.initialize();