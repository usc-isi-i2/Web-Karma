/*
 * Simple jQuery logger / debugger.
 * Based on: http://jquery.com/plugins/Authoring/
 * See var DEBUG below for turning debugging/logging on and off.
 *
 * @version   20070111
 * @since     2006-07-10
 * @copyright Copyright (c) 2006 Glyphix Studio, Inc. http://www.glyphix.com
 * @author    Brad Brizendine <brizbane@gmail.com>
 * @license   MIT http://www.opensource.org/licenses/mit-license.php
 * @requires  >= jQuery 1.0.3
 */
// global debug switch ... add DEBUG = true; somewhere after jquery.debug.js is loaded to turn debugging on
var DEBUG = true;
// shamelessly ripped off from http://getfirebug.com/
if (!("console" in window) || !("firebug" in console)){
	var names = ["log", "debug", "info", "warn", "error", "assert", "dir", "dirxml", "group", "groupEnd", "time", "timeEnd", "count", "trace", "profile", "profileEnd"];
	// create the logging div
	jQuery(document).ready(
		function(){
			$(document.body).append('<div id="DEBUG"><ol></ol></div>');
		}
	);
	// attach a function to each of the firebug methods
	window.console = {};
	for (var i = 0; i < names.length; ++i){
		window.console[names[i]] = function(msg){ $('#DEBUG ol').append( '<li>' + msg + '</li>' ); }
	}
}

/*
 * debug
 * Simply loops thru each jquery item and logs it
 */
jQuery.fn.debug = function() {
	return this.each(function(){
		$.log(this);
	});
};

/*
 * log
 * Send it anything, and it will add a line to the logging console.
 * If firebug is installed, it simple send the item to firebug.
 * If not, it creates a string representation of the html element (if message is an object), or just uses the supplied value (if not an object).
 */
jQuery.log = function(message){
	// only if debugging is on
	if( window.DEBUG ){
		// if no firebug, build a debug line from the actual html element if it's an object, or just send the string
		var str = message;
		if( !('firebug' in console) ){
			if( typeof(message) == 'object' ){
				str = '&lt;';
				str += message.nodeName.toLowerCase();
				for( var i = 0; i < message.attributes.length; i++ ){
					str += ' ' + message.attributes[i].nodeName.toLowerCase() + '="' + message.attributes[i].nodeValue + '"';
				}
				str += '&gt;';
			}
		}
		console.debug(str);
	}
};
