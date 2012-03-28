steal.plugins('jquery/controller', 
		'jquery/controller/view', 
		'jquery/event/hover')
.views('//editor/resourcebrowser/views/resourcelistviewitem.tmpl')
.then('resourceviewitem')
.then(function($) {

  Editor.Resourceviewitem.extend('Editor.Resourcelistviewitem',
	{
		init: function(el) {
			this._initImages();
		},
		
		_updateView: function(resource) {
			this.options.page = resource;
			this.element.html('//editor/resourcebrowser/views/resourcelistviewitem.tmpl', {
				page: this.options.page, 
				language: this.options.language,
				runtime: this.options.runtime,
				resourceType: this.options.resourceType
			});
			$(this.element.find('td:first')).unwrap();
			this._initImages();
			this.element.trigger('updateResource', resource);
		},
		
		_initImages: function() {
			if(this.options.mode == 'normal') {
				this.element.find('td.wbl-action').hover(
						function () {
							$(this).find('img').not('.wbl-overlayPreviewImage').show();
						},
						function () {
							$(this).find('img').not('.wbl-listitem-read,.wbl-overlayPreviewImage').hide();
						}
				);
			}
		},
		
		'input[type="checkbox"] click': function(el, ev) {
			var isMulti = (this.options.mode == 'editorMultiSelection');
			if(this.options.mode != 'normal' && !isMulti) {
				this.element.parent().find('input:checked').each(function(index, elem) {
					$(elem).removeAttr('checked');
				});
				$(el).attr('checked', 'checked');
			}
			
			// Enable or disable delete button
			if(this.element.parent().find('input:checked').length > 0) {
				$('button.wbl-delete').button("enable");
			} else {
				$('button.wbl-delete').button("disable");
			}
		},
		
		"img.wbl-listitem-read click": function(el, ev) {
			ev.stopPropagation();
			this._showResource(el.parents('tr.wbl-pageEntry'));
		},

		"img.wbl-listitem-settings click": function(el, ev) {
			ev.stopPropagation();
			this._openSettings(el.parents('tr.wbl-pageEntry'));
		},
		
		"img.wbl-listitem-delete click": function(el, ev) {
			ev.stopPropagation();
			el.trigger('deleteResource', el.parent().parent());
		}
		
	});

});
