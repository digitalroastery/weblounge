steal.plugins('jquery/controller', 
		'jquery/event',
		'jquery/controller/view', 
		'jquery/event/hover')
.views()
.then('resourceviewitem')
.then(function($) {

  Editor.Resourceviewitem.extend('Editor.Resourcelistviewitem',
	{
		init: function(el) {

		},
		
		"td.action hoverenter": function(el, ev) {
			el.find('img').show();
		},
		
		"td.action hoverleave": function(el, ev) {
			el.find('img').not('.settings').hide();
		}
		
	});

});
