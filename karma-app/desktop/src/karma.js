/*jshint esversion: 6 */
var open = require('open');
var path = require("path");
import env from './env';

exports.links = {
  license: "https://github.com/usc-isi-i2/Web-Karma/blob/master/LICENSE.txt",
  documentation: "https://github.com/usc-isi-i2/Web-Karma/wiki",
  issues: "https://github.com/usc-isi-i2/Web-Karma/issues",
  about_karma: "https://github.com/usc-isi-i2/Web-Karma/blob/master/README.md",
  about_isi: "http://www.isi.edu/about/"
};

exports.tomcat = {
  // path : __dirname + path.sep + ((env.name == 'production') ? "Resources" + path.sep + "app" + path.sep + "tomcat" : "tomcat"),
  path : __dirname + path.sep + "tomcat",
  launchURL : "http://localhost:8080"
};

exports.tomcat.catalina_home = exports.tomcat.path + path.sep + "bin";
exports.tomcat.catalina = exports.tomcat.catalina_home + path.sep + "catalina" + ((/^win/.test(process.platform)) ? ".bat" : ".sh");
exports.tomcat.startcmd = ((/^win/.test(process.platform)) ? "cd /d " + exports.tomcat.catalina_home + " && " : "") + exports.tomcat.catalina + " jpda start";
exports.tomcat.stopcmd = ((/^win/.test(process.platform)) ? "cd /d " + exports.tomcat.catalina_home + " && " : "") + exports.tomcat.catalina + " stop";
exports.tomcat.logFile= exports.tomcat.path + path.sep + "logs" + path.sep + "catalina.out";

exports.start = function(){
  var exec = require('child_process').exec;
  exec(exports.tomcat.startcmd, function(error, stdout, stderr) {
    // TODO log if there is some problem
    console.log(error);
    console.log(stdout);
    console.log(stderr);
  });
};

exports.launch = function(){
  var spawn = require('child_process').spawn;
  open(exports.tomcat.launchURL);
};

export function stop(){
  var exec = require('child_process').exec;
  exec(exports.tomcat.stopcmd, function(error, stdout, stderr) {
    // TODO log if there is some problem
    console.log(error);
    console.log(stdout);
    console.log(stderr);
  });
}
exports.stop = stop;
