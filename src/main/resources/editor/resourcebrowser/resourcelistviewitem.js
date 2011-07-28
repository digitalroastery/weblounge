steal.plugins('jquery/controller', 
		'jquery/controller/view', 
		'jquery/event/hover')
.then('resourceviewitem')
.then(function($) {

  Editor.Resourceviewitem.extend('Editor.Resourcelistviewitem',
	{
		init: function(el) {
//			$.Hover.delay = 0;
		},
		
		"td.wbl-action hoverenter": function(el, ev) {
//			el.find('img').show();
		},
		
		"td.wbl-action hoverleave": function(el, ev) {
//			el.find('img').not('.wbl-settings').hide();
		}
		
	});

});
