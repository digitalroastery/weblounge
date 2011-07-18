steal.plugins('funcunit').then(function(){

module("Editor.Pagecreator", { 
	setup: function(){
		S.open("//editor/pagecreator/pagecreator.html");
	}
});

test("Text Test", function(){
	equals(S("h1").text(), "Editor.Pagecreator Demo","demo text");
});


});