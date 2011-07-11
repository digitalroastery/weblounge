steal.plugins('jquery/view/tmpl', 'jquery/event/key')
.views('//editor/resourcebrowser/views/resourcepagessearch.tmpl', '//editor/resourcebrowser/views/resourcemediasearch.tmpl')
.then(function($) {

	$.Controller('Editor.Resourcesearch', 
	{	
		init: function(el) {
			if(this.options.resourceType == 'pages') {
				$(el).html('//editor/resourcebrowser/views/resourcepagessearch.tmpl', {});
			}
			else if(this.options.resourceType == 'media') {
				$(el).html('//editor/resourcebrowser/views/resourcemediasearch.tmpl', {});
			}
			
			$("input#resourceSearch").keypress(function(ev){
				if(ev.key() == '\r') {
					ev.preventDefault();
					if(this.options.resourceType == 'pages') {
						$(this).trigger('searchPages', $(this).val());
					}
					else if(this.options.resourceType == 'media') {
						$(this).trigger('searchMedia', $(this).val());
					}
				}
			});
		}
		
	});

});
