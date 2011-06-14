steal.plugins('jquery/controller', 'jquery/controller/view')
.views()
.then('resourceviewitem')
.then(function($) {

  Editor.Resourceview.extend('Editor.Resourcescrollviewitem',
	{
	  	defaults: {
	  		pageId: null
	  	}
	},	  
	{
		init: function(el) {
			this.options.pageId = el.id;
		},
		
		"click": function(el, ev) {
			el.toggleClass('marked');
		}
		
	});

});
