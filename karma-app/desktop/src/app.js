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
var glob = require("glob");

console.log('Loaded environment variables:', env);

var app = remote.app;
var appDir = jetpack.cwd(app.getAppPath());

var appName = appDir.read('package.json', 'json').name;

var colors = {
  INFO: "#eee",
  ERROR: "red",
  WARNING: "orange",
  DEFAULT: "yellow"
};

document.addEventListener('DOMContentLoaded', function () {
  document.getElementById('greet').innerHTML = appName;

  // create log file if it doesnt already exist and close the file descriptor rightaway
  fs.closeSync(fs.openSync(karma.tomcat.logFile, 'w'));

  let tail = new Tail(karma.tomcat.logFile);
  tail.on("line", function(data) {
    log(data);
  });

  function m_launch(){
    karma.launch();
    log("<b>Launching Karma. Go to <a href='http://localhost:8080'>http://localhost:8080</a> if it doesn't launch.</b>");
  }

  // set java home to java shipped with app if JAVA_HOME is not set
  karma.getJavaHome((_java_home) => {
    if (!_java_home){
        glob(path.join(__dirname, "jre*"), (err, files) => {
          if (files[0]){
            if (fs.existsSync(path.join(files[0], "bin", "java" + (/^win/.test(process.platform) ? ".exe" : "")))) {
              karma.setJavaHome(files[0]);
            }
          }
        });
    }
  });
  setTimeout(() => {
    log("Starting Karma...");
    karma.start();
  }, 1000);
  setTimeout(m_launch, 10000);
  document.getElementById("launch").onclick = m_launch;
  document.getElementById("restart").onclick = function(){
    karma.restart();
  };
  document.querySelector(".help").onclick = function(){
    karma.setJavaHomeHelp();
  };

  // register dialogs with dialogPolyfill
  var dialog_setMaxHeap = document.getElementById("dialog_setMaxHeap");
  dialogPolyfill.registerDialog(dialog_setMaxHeap);
  var dialog_setJavaHome = document.getElementById("dialog_setJavaHome");
  dialogPolyfill.registerDialog(dialog_setJavaHome);

  // set java home button handle
  document.getElementById("submit_setJavaHome").onclick = function(){
    let value = document.getElementById("input_setJavaHome").value;
    if (!fs.existsSync(path.join(value, "bin", "java" + (/^win/.test(process.platform) ? ".exe" : "")))) {
      document.querySelector("#dialog_setJavaHome .warning").innerHTML = "Invalid path for Java";
      setTimeout(() => {dialog_setJavaHome.showModal();}, 1000);
    } else {
      karma.setJavaHome(value);
      log("Your JAVA_HOME is set. Restart karma.");
    }
  };

  // construct the values for selector for set max heap
  var selector = document.getElementById("max_heap_size");
  var main_selector = document.getElementById("main_window_set_max_heap_size");
  for(let i=1; i<=16; i++){
    selector.innerHTML += "<option value='" + (i*1024) + "'>" + i + "GB</option>";
    main_selector.innerHTML += "<option value='" + (i*1024) + "'>" + i + "GB</option>";
  }

  // handle for max heap dialog box
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

  // Handle all the IPCs
  ipcRenderer.on('SET_MAX_HEAP', (event) => {
    karma.getMaxHeap((value) => {
      document.getElementById("max_heap_size").value = value;
      dialog_setMaxHeap.showModal();
    });
  });

  ipcRenderer.on('SET_JAVA_HOME', (event) => {
    karma.getJavaHome((value) => {
      document.getElementById("input_setJavaHome").value = value ? value : "";
      document.querySelector("#dialog_setJavaHome .warning").innerHTML = "";
      dialog_setJavaHome.showModal();
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
