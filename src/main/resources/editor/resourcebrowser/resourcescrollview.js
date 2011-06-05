steal.plugins('jquery/view/tmpl','jqueryui/widget')
.views('//editor/resourcebrowser/views/resourcescrollview.tmpl')
.resources('jquery.smoothDivScroll-1.1')
.css('smoothDivScroll')
.then('resourceview', 'resourcescrollviewitem')
.then(function($) {

  Editor.Resourceview.extend('Editor.Resourcescrollview', 
{},
	{
		
		init: function(el) {
			$(el).html('//editor/resourcebrowser/views/resourcescrollview.tmpl', {});
			this._initViewItems();
			
			$(el).find('#makeMeScrollable').smoothDivScroll({
				  	autoScroll: "onstart" , 
					autoScrollDirection: "backandforth", 
					autoScrollStep: 1, 
					autoScrollInterval: 15,	
					visibleHotSpots: "always"
			  	});
		},
		
		_initViewItems: function() {
			$.each(this.options.resources, function(i, res) {
				$('div.scrollableArea').append('//editor/resourcebrowser/views/resourcescrollviewitem.tmpl', {page: res});
				steal.dev.log('res: ' + res);
			});
			
			$('div.scrollviewitem').editor_resourcescrollviewitem();
		}
		
	});

});
