module("designer test", { 
	setup: function(){
		S.open("//editor/designer/designer.html");
	}
});

test("Copy Test", function(){
	equals(S("h1").text(), "Welcome to JavaScriptMVC 3.0!","welcome text");
});