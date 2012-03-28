steal.plugins('jquery/view/tmpl', 'jqueryui/widget')
.views('//editor/resourcebrowser/views/resourcescrollview.tmpl')
.resources('jquery.smoothDivScroll-1.1', 'jquery.lazyload')
.css('resources/smoothDivScroll')
.then('resourceview', 'resourcescrollviewitem')
.then(function($) {
	
	Editor.Resourceview.extend('Editor.Resourcescrollview', 
	{
		init: function(el) {
			$(el).html('//editor/resourcebrowser/views/resourcescrollview.tmpl', {resourceType: this.options.resourceType});
			this._initViewItems();
			this._initButtons();
			this._initFilter();
			this._initDialogs();
			this._initSmoothDivScroll();
			this._initLazyLoading();
		},
		
		update: function(options) {
			this._super(options);
			this.find('div.wbl-scrollViewItem').remove();
			this.divScroll.smoothDivScroll("recalculateScrollableArea");
			this.divScroll.smoothDivScroll("hideHotSpotBackgrounds");
			if($.isEmptyObject(this.options.resources)) return;
			this._initViewItems();
			this.divScroll.smoothDivScroll("recalculateScrollableArea");
			this.divScroll.smoothDivScroll("showHotSpotBackgrounds");
			this.divScroll.smoothDivScroll("startAutoScroll");
			this._initLazyLoading();
		},
		
		_initSmoothDivScroll: function() {
			this.divScroll = this.find('#wbl-makeMeScrollable').smoothDivScroll({
			  	autoScroll: "onstart",
				autoScrollDirection: "left",
				autoScrollStep: 1,
				autoScrollInterval: 15,
				visibleHotSpots: "always"
		  	});
		},
		
		_initLazyLoading: function() {
			var rootPath = this.options.runtime.getRootPath();
			this.element.find('img.wbl-pageThumbnail').lazyload({
				placeholder: rootPath + "/editor/resourcebrowser/images/empty_thumbnail.png",
				event: "scroll",
				container: this.element.find("div.scrollWrapper")
			});
		},
		
		_initViewItems: function() {
			var scrollViewItem = this.element.find('div.scrollableArea');
			scrollViewItem.hide();
			$.each(this.options.resources, $.proxy(function (i, res) {
				if(i == 49) return false;
				scrollViewItem.append('//editor/resourcebrowser/views/resourcescrollviewitem.tmpl', {
					page: res, 
					language: this.options.language,
					runtime: this.options.runtime,
					resourceType: this.options.resourceType
				});
				scrollViewItem.find('div.wbl-scrollViewItem:last').editor_resourcescrollviewitem({
					page: res, 
					runtime: this.options.runtime, 
					language: this.options.language,
					resourceType: this.options.resourceType,
					mode: this.options.mode
				});
			}, this));
			scrollViewItem.show();
		},
		
		_selectResources: function(selection) {
			// Find selected resources by id
			var resources = $.grep(this.element.find('div.wbl-scrollViewItem'), $.proxy(function(elem, index) {
				return jQuery.inArray($(elem).attr('id'), selection) != -1;
			}, this));
			
			// Mark selected resources
			$(resources).addClass('wbl-marked');
			
			// Move to first selected resource
			var index = $(resources).index() + 1;
			this.divScroll.smoothDivScroll("moveToElement", "number", index);
			this.divScroll.smoothDivScroll("stopAutoScroll");
		},
		
		"button.wbl-duplicate click": function(el, ev) {
			this.options.selectedResources = this.find('div.wbl-scrollViewItem.wbl-marked');
			if(this.options.selectedResources.length == 1) {
				this.duplicateDialog.dialog('open');
			} else if(this.options.selectedResources.length > 1) {
				if(this.options.resourceType == 'pages') {
					this.element.trigger('showMessage', 'Es kann nur eine Seite markiert werden.');
				} else {
					this.element.trigger('showMessage', 'Es kann nur ein Media markiert werden.');
				}
			} else {
				if(this.options.resourceType == 'pages') {
					this.element.trigger('showMessage', 'Es wurde keine Seite markiert.');
				} else {
					this.element.trigger('showMessage', 'Es wurde kein Media markiert.');
				}
			}
		},
		
		"button.wbl-delete click": function(el, ev) {
			this.options.selectedResources = this.find('div.wbl-scrollViewItem.wbl-marked');
			if(this.options.selectedResources.length) {
				this.deleteDialog.dialog('open');
			} else {
				if(this.options.resourceType == 'pages') {
					this.element.trigger('showMessage', 'Es wurde keine Seite markiert.');
				} else {
					this.element.trigger('showMessage', 'Es wurde kein Media markiert.');
				}
			}
		},
		
		"button.wbl-favorize click": function(el, ev) {
			this.options.selectedResources = $('div.wbl-scrollViewItem.wbl-marked');
			this.element.trigger('favorizeResources', this.options.selectedResources);
		}
		
	});

});
