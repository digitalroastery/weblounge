module("editor test", { 
	setup: function(){
		S.open("//editor/editor.html");
	}
});

test("Copy Test", function(){
	equals(S("h1").text(), "Welcome to JavaScriptMVC 3.0!","welcome text");
});