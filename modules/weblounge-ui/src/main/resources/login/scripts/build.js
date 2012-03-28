//steal/js login/scripts/compress.js

load("steal/rhino/steal.js");
steal.plugins('steal/build','steal/build/scripts','steal/build/styles',function(){
	steal.build('login/scripts/build.html',{to: 'login'});
});
