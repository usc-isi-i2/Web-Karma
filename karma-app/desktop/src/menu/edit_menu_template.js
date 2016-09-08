/*jshint esversion: 6 */
var open = require('open');
var karma = require("./karma");
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
