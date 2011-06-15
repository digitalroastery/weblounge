steal.plugins('jquery/controller', 'jquery/controller/view', 'jquery/controller/subscribe')
.views()
.then('resourceviewitem')
.then(function($) {

  Editor.Resourceview.extend('Editor.Resourcescrollviewitem',
	{
		init: function(el) {
			this.pageId = el.id;
		},
		
		"click": function(el, ev) {
			el.toggleClass('marked');
		},
		
		"a.pagePath click": function(el, ev) {
			ev.preventDefault();
			this.publish('designer.open', this.pageId);
		}
		
	});

});
