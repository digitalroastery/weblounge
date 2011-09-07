module("mediaeditor test", { 
	setup: function(){
		S.open("//editor/mediaeditor/mediaeditor.html");
	}
});

test("Copy Test", function(){
	equals(S("h1").text(), "Welcome to JavaScriptMVC 3.0!","welcome text");
});