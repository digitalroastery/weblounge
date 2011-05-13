module("menubar test", { 
	setup: function(){
		S.open("//editor/menubar/menubar.html");
	}
});

test("menubar loaded", function(){
	ok(S('#weblounge-editor').exists(), "menubar ist loaded sucessfully");
	ok(S('#weblounge-editor img.add').click(function() {
		ok(S('div#add-menu').visible(), "add menu is loaded");
		ok(S('div#add-menu').position({top: 54, left: 250}), "add menu in position");
		equals(S('div#add-menu').width(), "200", "add menu has a correct width");
	}), "add menu was clicked");
});