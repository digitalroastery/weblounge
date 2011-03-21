//js editor/menubar/scripts/doc.js

load('steal/rhino/steal.js');
steal.plugins("documentjs").then(function(){
	DocumentJS('editor/menubar/menubar.html');
});