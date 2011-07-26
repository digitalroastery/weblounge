steal.plugins('jquery/controller', 'jquery/event', 'jquery/controller/view')
.views()
.then('resourceviewitem')
.then(function($) {

  Editor.Resourceviewitem.extend('Editor.Resourcescrollviewitem',
	{
		init: function(el) {
		},
		
		"click": function(el, ev) {
			el.toggleClass('wbl-marked');
		}
		
	});

});
