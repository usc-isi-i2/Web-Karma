var open = require('open');

exports.start = function(){
  var exec = require('child_process').exec;
  var cmd = 'cd ./app/tomcat/bin/; ./catalina.sh jpda start';

  exec(cmd, function(error, stdout, stderr) {
    // TODO log if there is some problem
    console.log(error);
    console.log(stdout);
    console.log(stderr);
  });
  console.log("start karma");
};

exports.launch = function(){
  var spawn = require('child_process').spawn;
  open('http://localhost:8080');
  console.log("launch karma");
};

exports.stop = function(){
  var exec = require('child_process').exec;
  var cmd = 'cd ./app/tomcat/bin/; ./catalina.sh stop';

  exec(cmd, function(error, stdout, stderr) {
    // TODO log if there is some problem
    console.log(error);
    console.log(stdout);
    console.log(stderr);
  });
  console.log("stop karma");
};
