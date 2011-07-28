steal.plugins('jquery/view/tmpl', 'jqueryui/widget')
.views('//editor/resourcebrowser/views/resourcescrollview.tmpl')
.resources('jquery.smoothDivScroll-1.1', 'jquery.lazyload')
.css('resources/smoothDivScroll')
.then('resourceview', 'resourcescrollviewitem')
.then(function($) {
	
	Editor.Resourceview.extend('Editor.Resourcescrollview', 
	{
		init: function(el) {
			$(el).html('//editor/resourcebrowser/views/resourcescrollview.tmpl', {});
			this._initViewItems();
			this._initButtons();
			this._initDialogs();
			this._initSmoothDivScroll();
		},
		
		update: function(options) {
			this.options.resources = options.resources;
			this.find('div.wbl-scrollviewitem').remove();
			this._initViewItems();
		},
		
		_initSmoothDivScroll: function() {
			this.find('#wbl-makeMeScrollable').smoothDivScroll({
			  	autoScroll: "onstart",
				autoScrollDirection: "left",
				autoScrollStep: 1,
				autoScrollInterval: 15,
				visibleHotSpots: "always"
		  	});
			
			// Update evtl
//			this.element.find('img.wbl-pageThumbnail').lazyload({         
//				placeholder: "images/grey.gif",
//				event: "mouseover",
//				container: this.element.find("div.scrollableArea")
//			});
		},
		
		_initViewItems: function() {
			$.each(this.options.resources, $.proxy(function (i, res) {
				var scrollViewItem = this.element.find('div.scrollableArea').append('//editor/resourcebrowser/views/resourcescrollviewitem.tmpl', {
					page: res, 
					language: this.options.language,
					runtime: this.options.runtime
				});
				scrollViewItem.find('div.wbl-scrollViewItem').editor_resourcescrollviewitem({page: res, runtime: this.options.runtime});
			}, this));
		},
		
		"img.wbl-trashPage deletePage": function(el, ev) {
			this.options.selectedPages = el.parent();
			this.deleteDialog.dialog('open');
		},
		
		"button.wbl-duplicate click": function(el, ev) {
			this.options.selectedPages = this.find('div.wbl-resourceScrollViewItem.wbl-marked');
			if(this.options.selectedPages.length == 1) {
				this.duplicateDialog.dialog('open');
			} else if(this.options.selectedPages.length > 1) {
				this._showMessage('Es kann nur eine Seite markiert werden.')
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		},
		
		"button.wbl-delete click": function(el, ev) {
			this.options.selectedPages = this.find('div.wbl-resourceScrollViewItem.wbl-marked');
			if(this.options.selectedPages.length) {
				this.deleteDialog.dialog('open');
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		},
		
		"button.wbl-favorize click": function(el, ev) {
			this.options.selectedPages = $('div.wbl-resourceScrollViewItem.wbl-marked');
			if(this.options.selectedPages.length) {
				this.options.selectedPages.trigger('favorizePages', [this.options.selectedPages]);
				this._showMessage('Zu Favoriten hinzugefügt');
			} else {
				this._showMessage('Es wurde keine Seite markiert.');
			}
		}
		
	});

});
