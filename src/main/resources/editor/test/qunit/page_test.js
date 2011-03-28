module("Model: Editor.Models.Page")

test("findAll", function(){
	stop(2000);
	Editor.Models.Page.findAll({}, function(pages){
		start()
		ok(pages)
        ok(pages.length)
        ok(pages[0].name)
        ok(pages[0].description)
	});
	
})

test("create", function(){
	stop(2000);
	new Editor.Models.Page({name: "dry cleaning", description: "take to street corner"}).save(function(page){
		start();
		ok(page);
        ok(page.id);
        equals(page.name,"dry cleaning")
        page.destroy()
	})
})
test("update" , function(){
	stop();
	new Editor.Models.Page({name: "cook dinner", description: "chicken"}).
            save(function(page){
            	equals(page.description,"chicken");
        		page.update({description: "steak"},function(page){
        			start()
        			equals(page.description,"steak");
        			page.destroy();
        		})
            })

});
test("destroy", function(){
	stop(2000);
	new Editor.Models.Page({name: "mow grass", description: "use riding mower"}).
            destroy(function(page){
            	start();
            	ok( true ,"Destroy called" )
            })
})