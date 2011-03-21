//steal/js editor/massuploader/scripts/compress.js

load("steal/rhino/steal.js");
steal.plugins('steal/build','steal/build/scripts','steal/build/styles',function(){
	steal.build('editor/massuploader/scripts/build.html',{to: 'editor/massuploader'});
});
