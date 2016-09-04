/*jshint esversion: 6 */
import os from 'os'; // native node.js module
import { remote } from 'electron'; // native electron module
import jetpack from 'fs-jetpack'; // module loaded from npm
import env from './env';
var karma = require('electron').remote.require('./karma');

console.log('Loaded environment variables:', env);

var app = remote.app;
var appDir = jetpack.cwd(app.getAppPath());

var appName = appDir.read('package.json', 'json').name;
var appVersion= appDir.read('package.json', 'json').version;

document.addEventListener('DOMContentLoaded', function () {
    document.getElementById('greet').innerHTML = appName + " " + appVersion;
    document.getElementById("start").onclick = function(){
      log("Starting Karma...");
      karma.start();
    };
    document.getElementById("launch").onclick = function(){
      karma.launch();
      alert("Launching Karma. Go to http://localhost:8080 if it doesn't launch.");
    };
    document.getElementById("stop").onclick = function(){
      log("Stopping Karma...");
      karma.stop();
    };
});

function log(string){
  var d_log = document.getElementById("log");
  d_log.innerHTML += string  + "<br>";
  d_log.scrollTop = d_log.scrollHeight;
}
