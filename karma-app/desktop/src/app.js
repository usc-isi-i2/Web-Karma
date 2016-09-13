/*jshint esversion: 6 */
import os from 'os'; // native node.js module
import { remote } from 'electron'; // native electron module
import jetpack from 'fs-jetpack'; // module loaded from npm
import env from './env';
var karma = require('electron').remote.require('./karma');
var Tail = require('tail').Tail;
var path = require("path");
var fs = require("fs");
var D = require('dialogs');
let dialogs = D();
const {ipcRenderer} = require('electron');

console.log('Loaded environment variables:', env);

var app = remote.app;
var appDir = jetpack.cwd(app.getAppPath());

var appName = appDir.read('package.json', 'json').name;
var appVersion= appDir.read('package.json', 'json').version;

var colors = {
  INFO: "#eee",
  ERROR: "red",
  WARNING: "orange",
  DEFAULT: "yellow"
};

document.addEventListener('DOMContentLoaded', function () {
  document.getElementById('greet').innerHTML = appName + " v" + appVersion;

  fs.openSync(karma.tomcat.logFile, 'w');
  let tail = new Tail(karma.tomcat.logFile);
  tail.on("line", function(data) {
    log(data);
  });

  log("Starting Karma...");
  karma.start();

  // Launches Karma in browser after 5 seconds.
  setTimeout(function(){
    karma.launch();
    log("<b>Launching Karma. Go to <a href='http://localhost:8080'>http://localhost:8080</a> if it doesn't launch.</b>");
  }, 5000);

  document.getElementById("launch").onclick = function(){
    log("<b>Launching Karma. Go to <a href='http://localhost:8080'>http://localhost:8080</a> if it doesn't launch.</b>");
    karma.launch();
  };

  ipcRenderer.on('SET_MIN_HEAP', (event) => {
    karma.getMinHeap((value) => {
      dialogs.prompt('Set Min Heap Size (MB)', value, function(value) {
        if (/^\d+$/.test(value)){
          karma.setMinHeap(value);
        }
      });
    });
  });

  ipcRenderer.on('SET_MAX_HEAP', (event) => {
    karma.getMaxHeap((value) => {
      dialogs.prompt('Set Max Heap Size (MB)', value, function(value) {
        if (/^\d+$/.test(value)){
          karma.setMaxHeap(value);
        }
      });
    });
  });
});


function log(data){
  var color = colors.DEFAULT;
  if (data.split(" ").length > 3){
    color = (colors[data.split(" ")[2]]) ? colors[data.split(" ")[2]] : colors.DEFAULT;
  }
  data = "<span style='color: "+ color +"'>" + data + "</span>";
  let log = document.getElementById("log");
  log.innerHTML += data + "<br>";
  log.scrollTop = log.scrollHeight;
}
