/*jshint esversion: 6 */
var open = require('open');
var path = require("path");
import env from './env';

exports.tomcat = {
  // path : __dirname + path.sep + ((env.name == 'production') ? "Resources" + path.sep + "app" + path.sep + "tomcat" : "tomcat"),
  path : __dirname + path.sep + "tomcat",
  launchURL : "http://localhost:8080"
};

exports.tomcat.catalina = exports.tomcat.path + path.sep + "bin" + path.sep + "catalina" + ((/^win/.test(process.platform)) ? ".bat" : ".sh");
exports.tomcat.startcmd = exports.tomcat.catalina + " jpda start";
exports.tomcat.stopcmd = exports.tomcat.catalina + " stop";
exports.tomcat.logFile= exports.tomcat.path + path.sep + "logs" + path.sep + "catalina.out";

exports.start = function(){
  var exec = require('child_process').exec;
  exec(exports.tomcat.startcmd, function(error, stdout, stderr) {
    // TODO log if there is some problem
    console.log(error);
    console.log(stdout);
    console.log(stderr);
  });
  console.log("start karma");
};

exports.launch = function(){
  var spawn = require('child_process').spawn;
  open(exports.tomcat.launchURL);
  console.log("launch karma");
};

exports.stop = function(){
  var exec = require('child_process').exec;
  exec(exports.tomcat.stopcmd, function(error, stdout, stderr) {
    // TODO log if there is some problem
    console.log(error);
    console.log(stdout);
    console.log(stderr);
  });
  console.log("Stop Karma");
};
