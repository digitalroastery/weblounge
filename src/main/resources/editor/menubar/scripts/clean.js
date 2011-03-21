//steal/js editor/menubar/scripts/compress.js

load("steal/rhino/steal.js");
steal.plugins('steal/clean',function(){
	steal.clean('editor/menubar/menubar.html',{
		indent_size: 1, 
		indent_char: '\t', 
		jslint : false,
		ignore: /jquery\/jquery.js/,
		predefined: {
			steal: true, 
			jQuery: true, 
			$ : true,
			window : true
			}
	});
});
