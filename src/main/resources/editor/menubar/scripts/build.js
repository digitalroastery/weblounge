//steal/js editor/menubar/scripts/compress.js

load("steal/rhino/steal.js");
steal.plugins('steal/build','steal/build/scripts','steal/build/styles',function(){
	steal.build('editor/menubar/scripts/build.html',{to: 'editor/menubar'});
});
