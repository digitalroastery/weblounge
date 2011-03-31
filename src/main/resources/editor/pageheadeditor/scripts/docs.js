//js editor/pageheadeditor/scripts/doc.js

load('steal/rhino/steal.js');
steal.plugins("documentjs").then(function(){
	DocumentJS('editor/pageheadeditor/pageheadeditor.html');
});