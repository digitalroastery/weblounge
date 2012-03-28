module("Model: Editor.Models.Pagelet")

test("findAll", function(){
	stop(2000);
	Editor.Models.Pagelet.findAll({}, function(pagelets){
		start()
		ok(pagelets)
        ok(pagelets.length)
        ok(pagelets[0].name)
        ok(pagelets[0].description)
	});
	
})

test("create", function(){
	stop(2000);
	new Editor.Models.Pagelet({name: "dry cleaning", description: "take to street corner"}).save(function(pagelet){
		start();
		ok(pagelet);
        ok(pagelet.id);
        equals(pagelet.name,"dry cleaning")
        pagelet.destroy()
	})
})
test("update" , function(){
	stop();
	new Editor.Models.Pagelet({name: "cook dinner", description: "chicken"}).
            save(function(pagelet){
            	equals(pagelet.description,"chicken");
        		pagelet.update({description: "steak"},function(pagelet){
        			start()
        			equals(pagelet.description,"steak");
        			pagelet.destroy();
        		})
            })

});
test("destroy", function(){
	stop(2000);
	new Editor.Models.Pagelet({name: "mow grass", description: "use riding mower"}).
            destroy(function(pagelet){
            	start();
            	ok( true ,"Destroy called" )
            })
})