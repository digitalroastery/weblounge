//js editor/massuploader/scripts/doc.js

load('steal/rhino/steal.js');
steal.plugins("documentjs").then(function(){
	DocumentJS('editor/massuploader/massuploader.html');
});