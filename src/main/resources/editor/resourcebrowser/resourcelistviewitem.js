steal.plugins('jquery/controller', 
		'jquery/controller/view', 
		'jquery/event/hover')
.then('resourceviewitem')
.then(function($) {

  Editor.Resourceviewitem.extend('Editor.Resourcelistviewitem',
	{
		init: function(el) {
			if(this.options.mode == 'editorSelection' || this.options.mode == 'editorMultiSelection') {
				this.element.find('img.wbl-settings').hide();
			} else {
				this.element.find('img.wbl-settings').show();
				this.element.find('td.wbl-action').hover(
						function () {
							$(this).find('img').show();
						},
						function () {
							$(this).find('img').not('.wbl-settings').hide();
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
		},
		
		"img.wbl-settings click": function(el, ev) {
			ev.stopPropagation();
			this._openSettings(el.parents('tr.wbl-pageEntry'));
		},
		
		"img.wbl-itemDelete click": function(el, ev) {
			ev.stopPropagation();
			el.trigger('deleteResource', el.parent().parent());
		}
		
	});

});
