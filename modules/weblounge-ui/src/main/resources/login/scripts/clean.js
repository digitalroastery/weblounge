//steal/js login/scripts/compress.js

load("steal/rhino/steal.js");
steal.plugins('steal/clean',function(){
	steal.clean('login/login.html',{indent_size: 1, indent_char: '\t'});
});
