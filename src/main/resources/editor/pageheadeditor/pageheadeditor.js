steal.plugins('jquery',
		'jquery/controller/view',
		'jquery/view',
		'jquery/view/tmpl',
		'jqueryui/widget')
.models('../../models/workbench')
.views('//editor/pageheadeditor/views/init.tmpl')
.css('pageheadeditor')
.then(function($) {

	/**
	 * @class Editor.Pageheadeditor
	 */
	$.Controller('Editor.Pageheadeditor', 
	/* @Static */
	{
		defaults : {
		
		}
	},
	/* @Prototype */
	{
		init: function(el) {
			var pageData = new Object();
			
			$(el).html('//editor/pageheadeditor/views/init.tmpl', {
				page: this.options.page, 
				language: this.options.language, 
				options: this.options.runtime.getSiteLayouts()
			});
			this.element.find("select[name=layout]").val(this.options.page.getTemplate());
			
			Page.findReferrer({id: this.options.page.value.id}, $.proxy(function(referrer) {
				if(referrer == undefined) {
					this.element.find('div.wbl-referrerPageSettings').html('Keine Verweise');
					return;
				}
				$.each(referrer, $.proxy(function(index, ref) {
			    	var page = new Page({value: ref});
					this.element.find('div.wbl-referrerPageSettings').append(page.getTitle(this.options.language))
					.append(': <a href="' + page.getPath() + '?_=' + new Date().getTime() + '">' + page.getPath() + '</a><br />');
				}, this));
			}, this));
			
			// TODO Load AvailableTags
			Workbench.suggestTags({}, $.proxy(function(tags) {
				if(tags == null || tags == undefined) return;
//				var availableTags = ["ActionScript","Scheme"];
				this.element.find("input[name=tags]").autocomplete({
					source: function(request, response) {
						// delegate back to autocomplete, but extract the last term
						response($.ui.autocomplete.filter(tags, request.term.split(/,\s*/).pop()));
					},
					focus: function() {
						// prevent value inserted on focus
						return false;
					},
					select: function(ev, ui) {
						var terms = this.value.split(/,\s*/);
						// remove the current input
						terms.pop();
						// add the selected item
						terms.push(ui.item.value);
						// add placeholder to get the comma-and-space at the end
						terms.push("");
						this.value = terms.join(", ");
						return false;
					}
				});
			}, this));
			
			// Dialog
			this.element.dialog({
				modal: true,
				title: 'Seite bearbeiten',
				autoOpen: true,
				resizable: true,
				draggable: true,
				width: 700,
				height: 650,
				buttons: {
					Abbrechen: $.proxy(function() {
						this.element.dialog('close');
					}, this),
					Speichern: $.proxy(function() {
						this.element.find("form#wbl-validatePageSettings").submit();
						if(!this.element.find("form#wbl-validatePageSettings").valid()) return;
						
						$.each(this.element.find(':input'), function(i, input) {
							pageData[$(input).attr('name')] = $(input).val();
						});
						
						// update pageData
						this.options.page.saveMetadata(pageData, this.options.language, $.proxy(function() {
							location.href = this.options.page.getPath() + "?edit&_=" + new Date().getTime();
						}, this));
						
						this.element.trigger('closeeditor');
						this.element.dialog('destroy');
						this.destroy();
					}, this)
				},
				close: $.proxy(function () {
					this.element.trigger('closeeditor');
					this.element.dialog('destroy');
					this.destroy();
				},this)
			});
			
			this.element.find("form#wbl-validatePageSettings").validate();
		},
	
	    update: function(options) {
	    	this.options = options;
	    	this.element.dialog('open');
	    },
	    
		"input[name=url] change": function(el, ev) {
			var url = el.val().trim().toLowerCase();
			el.val(encodeURI(url));
		}
	});
});