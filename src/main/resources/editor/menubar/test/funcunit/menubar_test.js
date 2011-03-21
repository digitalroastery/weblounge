module("menubar test", { 
	setup: function(){
		S.open("//editor/menubar/menubar.html");
	}
});

test("Copy Test", function(){
	equals(S("h1").text(), "Welcome to JavaScriptMVC 3.0!","welcome text");
});