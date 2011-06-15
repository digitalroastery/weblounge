steal.plugins('jquery/controller', 
		'jquery/controller/view', 
		'jquery/controller/subscribe',
		'jquery/event/hover')
.views()
.then('resourceviewitem')
.then(function($) {

  Editor.Resourceview.extend('Editor.Resourcelistviewitem',
	{
		init: function(el) {
			this.pageId = el.id;
		},
		
		"a.pagePath click": function(el, ev) {
			ev.preventDefault();
			this.publish('designer.open', this.pageId);
		},
		
		"td.action hoverenter": function(el, ev) {
			el.find('img').show();
		},
		
		"td.action hoverleave": function(el, ev) {
			el.find('img').not('.settings').hide();
		}
		
	});

});
