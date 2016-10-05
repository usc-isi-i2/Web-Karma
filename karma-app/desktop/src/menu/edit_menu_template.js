/*jshint esversion: 6 */
var open = require('open');
var karma = require("./karma");
import { app } from 'electron';

export var editMenuTemplate = {
  label: 'Help',
  submenu: [
    { label: "View License", click() { open(karma.links.license);}},
    { label: "Documentation", click() { open(karma.links.documentation);}},
    { label: "Issues", click() { open(karma.links.issues);}},
    { type: "separator" },
    { label: "About Karma", click() { open(karma.links.about_karma);}},
    { label: "About ISI", click() {  open(karma.links.about_isi);}},
  ]
};

export var fileMenuTemplate = {
  label: "File",
  submenu: [
    { label: "Open New Window", click(){ karma.launch();} },
    { label: "Restart Karma", click(){ karma.restart();} },
    // { label: "Set Min Heap", click(item, focusedWindow){
    // if(focusedWindow){
    // focusedWindow.send('SET_MIN_HEAP');
    // }
    // } },
    { label: "Set Max Heap", click(item, focusedWindow){
      if(focusedWindow){
        focusedWindow.send('SET_MAX_HEAP');
      }
    } },
    { label: "Set Java Home", accelerator: 'CmdOrCtrl+J',click(item, focusedWindow){
      if(focusedWindow){
        focusedWindow.send('SET_JAVA_HOME');
      }
    } },
    { label: "Exit", accelerator: 'CmdOrCtrl+Q', click(){ app.quit(); } },
  ]
};
