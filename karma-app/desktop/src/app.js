/*jshint esversion: 6 */
import os from 'os'; // native node.js module
import { remote } from 'electron'; // native electron module
import jetpack from 'fs-jetpack'; // module loaded from npm
import env from './env';
var karma = require('electron').remote.require('./karma');
var Tail = require('tail').Tail;
var path = require("path");
var fs = require("fs");
var dialogPolyfill = require("dialog-polyfill");
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

  // set max heap dialog
  var dialog = document.querySelector('dialog');
  dialogPolyfill.registerDialog(dialog);
  var selector = document.getElementById("max_heap_size");
  for(let i=1; i<=16; i++){
    selector.innerHTML += "<option value='" + (i*1024) + "'>" + i + "GB</option>";
  }
  document.getElementById("set_max_heap_size").onclick = function(){
    let value = document.getElementById("max_heap_size").value;
    karma.setMaxHeap(value);
  };
  ipcRenderer.on('SET_MAX_HEAP', (event) => {
    karma.getMaxHeap((value) => {
      document.getElementById("max_heap_size").value = value;
      dialog.showModal();
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
