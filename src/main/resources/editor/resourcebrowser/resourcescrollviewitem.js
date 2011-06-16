steal.plugins('jquery/controller', 'jquery/event', 'jquery/controller/view')
.views()
.then('resourceviewitem')
.then(function($) {

  Editor.Resourceviewitem.extend('Editor.Resourcescrollviewitem',
	{
		init: function(el) {
			this.pageId = el.id;
		},
		
		"click": function(el, ev) {
			el.toggleClass('marked');
			// event select page (pageObject)
		}
		
	});

});
