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

  // create log file if it doesnt already exist and close the file descriptor rightaway
  fs.closeSync(fs.openSync(karma.tomcat.logFile, 'w'));

  let tail = new Tail(karma.tomcat.logFile);
  tail.on("line", function(data) {
    log(data);
  });

  log("Starting Karma...");
  karma.start();

  function m_launch(){
    karma.launch();
    log("<b>Launching Karma. Go to <a href='http://localhost:8080'>http://localhost:8080</a> if it doesn't launch.</b>");
  }
  // Launches Karma in browser after 5 seconds.
  setTimeout(m_launch, 5000);
  document.getElementById("launch").onclick = m_launch;
  document.getElementById("restart").onclick = function(){
    karma.restart();
  };

  // constructing the select boxes
  var dialog = document.querySelector('dialog');
  dialogPolyfill.registerDialog(dialog);
  var selector = document.getElementById("max_heap_size");
  var main_selector = document.getElementById("main_window_set_max_heap_size");
  for(let i=1; i<=16; i++){
    selector.innerHTML += "<option value='" + (i*1024) + "'>" + i + "GB</option>";
    main_selector.innerHTML += "<option value='" + (i*1024) + "'>" + i + "GB</option>";
  }

  // handle from dialog box
  document.getElementById("set_max_heap_size").onclick = function(){
    let value = document.getElementById("max_heap_size").value;
    karma.setMaxHeap(value);
    log("Max Heap changed to " + (value/1024) + "GB. Restart Karma to see changes.");
  };

  // handle from main window
  main_selector.onchange = function(){
    let memory = this.value;
    karma.setMaxHeap(memory);
    log("Max Heap changed to " + (this.value/1024) + "GB. Restart Karma to see changes.");
    main_selector.innerHTML = "";
    for(let i=1; i<=16; i++){
      main_selector.innerHTML += "<option value='" + (i*1024) + "'>" + i + "GB</option>";
    }
    main_selector.value = memory;
    main_selector.options[main_selector.selectedIndex].text = "Max Heap: " + main_selector.options[main_selector.selectedIndex].text;
  };
  karma.getMaxHeap((value) => {
    main_selector.value = value;
    main_selector.options[main_selector.selectedIndex].text = "Max Heap: " + main_selector.options[main_selector.selectedIndex].text;
  });

  ipcRenderer.on('SET_MAX_HEAP', (event) => {
    karma.getMaxHeap((value) => {
      document.getElementById("max_heap_size").value = value;
      dialog.showModal();
    });
  });
});


function log(data){
  data = data.trim();
  let cue = data.split(" ")[0].replace(":", "");
  var color = (colors[cue]) ? colors[cue] : colors.DEFAULT;
  data = "<span style='color: "+ color +"'>" + data + "</span>";
  let log = document.getElementById("log");
  log.innerHTML += data + "<br>";
  log.scrollTop = log.scrollHeight;
}
