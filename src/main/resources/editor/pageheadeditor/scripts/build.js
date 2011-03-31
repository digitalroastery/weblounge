//steal/js editor/pageheadeditor/scripts/compress.js

load("steal/rhino/steal.js");
steal.plugins('steal/build','steal/build/scripts','steal/build/styles',function(){
	steal.build('editor/pageheadeditor/scripts/build.html',{to: 'editor/pageheadeditor'});
});
