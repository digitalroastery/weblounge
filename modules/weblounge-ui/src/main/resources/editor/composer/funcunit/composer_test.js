steal.plugins('funcunit').then(function(){

	module("editor/composer", { 
		setup: function(){
			S.open("http://localhost:8080/?edit");
		}
	});
	
	test("drag & drop composer inline", function(){
		S(".pagelet:first").drag(".pagelet:last");
		
		S.wait(20, function(){
			equal(S('.pagelet:last').attr('index'), 0, "check if index is 0")
		})
	})
	
	test("open pagelet editor", function(){
		S(".pagelet:first").move();
		S('div.icon_editing').exists();
		S("div.icon_editing").click();
		S('.ui-dialog').exists();
	});

});