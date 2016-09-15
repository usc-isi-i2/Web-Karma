/*jshint esversion: 6 */
var open = require('open');
var path = require("path");
import env from './env';
import jetpack from 'fs-jetpack';

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
exports.tomcat.logFile = exports.tomcat.path + path.sep + "logs" + path.sep + "catalina.out";

// export CATALINA_OPTS="-Xms128M -Xmx512MB"

exports.start = function(){
  let command = (/^win/.test(process.platform) ? "set" : "export") + " JAVA_OPTS=";
  exports.getMinHeap((_min) => {
    command += "-Xms" + _min + "M";
    exports.getMaxHeap((_max) => {
      command += " -Xmx" + _max +"M";
      command += (/^win/.test(process.platform) ? " && " : ";");
      command += exports.tomcat.startcmd;
      console.log(command);
      var exec = require('child_process').exec;
      exec(exports.tomcat.startcmd, function(error, stdout, stderr) {
        console.log(error);
        console.log(stdout);
        console.log(stderr);
      });
    });
  });
};

exports.launch = function(){
  var spawn = require('child_process').spawn;
  open(exports.tomcat.launchURL);
};

exports.restart = function(){
  stop();
  // wait for 5 seconds and try to start
  setTimeout(exports.start, 5000);
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

exports.setMinHeap = function(value){
    var env = jetpack.cwd(__dirname).read('env.json', 'json');
    env.args["-Xms"] = value;
    jetpack.cwd(__dirname).write('env.json', env);
};

exports.getMinHeap = function(callback){
    var env = jetpack.cwd(__dirname).read('env.json', 'json');
    callback(env.args["-Xms"]);
};

exports.setMaxHeap = function(value){
    var env = jetpack.cwd(__dirname).read('env.json', 'json');
    env.args["-Xmx"] = value;
    jetpack.cwd(__dirname).write('env.json', env);
};

exports.getMaxHeap = function(callback){
    var env = jetpack.cwd(__dirname).read('env.json', 'json');
    callback(env.args["-Xmx"]);
};
