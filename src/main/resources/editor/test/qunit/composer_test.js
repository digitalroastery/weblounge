module("Model: Editor.Models.Composer")

test("findAll", function(){
	stop(2000);
	Editor.Models.Composer.findAll({}, function(composers){
		start()
		ok(composers)
        ok(composers.length)
        ok(composers[0].name)
        ok(composers[0].description)
	});
	
})

test("create", function(){
	stop(2000);
	new Editor.Models.Composer({name: "dry cleaning", description: "take to street corner"}).save(function(composer){
		start();
		ok(composer);
        ok(composer.id);
        equals(composer.name,"dry cleaning")
        composer.destroy()
	})
})
test("update" , function(){
	stop();
	new Editor.Models.Composer({name: "cook dinner", description: "chicken"}).
            save(function(composer){
            	equals(composer.description,"chicken");
        		composer.update({description: "steak"},function(composer){
        			start()
        			equals(composer.description,"steak");
        			composer.destroy();
        		})
            })

});
test("destroy", function(){
	stop(2000);
	new Editor.Models.Composer({name: "mow grass", description: "use riding mower"}).
            destroy(function(composer){
            	start();
            	ok( true ,"Destroy called" )
            })
})