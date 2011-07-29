steal.plugins('jquery/controller', 
		'jquery/controller/view', 
		'jquery/event/hover')
.then('resourceviewitem')
.then(function($) {

  Editor.Resourceviewitem.extend('Editor.Resourcelistviewitem',
	{
		init: function(el) {
			// TODO
			this.element.find('td.wbl-action').hover(
				function () {
					$(this).find('img').show();
				},
				function () {
					$(this).find('img').not('.wbl-settings').hide();
				}
			);
		}
		
	});

});
