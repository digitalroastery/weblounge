steal.plugins('jquery/view/tmpl')
.views('//editor/resourcebrowser/views/resourcelistview.tmpl')
.then('resourceview')
.then(function($) {

	Editor.Resourceview.extend('Editor.Resourcelistview', 
	{	
		init: function(el) {
			$(el).html('//editor/resourcebrowser/views/resourcelistview.tmpl', {});
			this._initViewItems();
		},
		
		_initViewItems: function() {
			$.each(this.options.resources, function(i, res) {
				steal.dev.log('res list: ' + res);
				$('#listViewContent').append('//editor/resourcebrowser/views/resourcelistviewitem.tmpl', {page: res});
			});
			
//			$('div.scrollviewitem').editor_resourcescrollviewitem();
		},
		
		"img.settings click": function(el, ev) {
			steal.dev.log('settings')
		},
		
		"img.delete click": function(el, ev) {
			steal.dev.log('delete')
		},
		
		"img.favorite click": function(el, ev) {
			steal.dev.log('favorize')
		},
		
		"button.duplicate click": function(el, ev) {
			steal.dev.log('duplicate')
		},
		
		"button.delete click": function(el, ev) {
			steal.dev.log('delete')
		},
		
		"button.favorize click": function(el, ev) {
			steal.dev.log('favorize')
		}
		
	});

});
