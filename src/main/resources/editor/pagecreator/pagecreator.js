steal.plugins('jquery',
		'jquery/controller/view',
		'jquery/view',
		'jquery/view/tmpl',
		'jqueryui/widget')
.models('../../models/workbench')
.views('//editor/pagecreator/views/init.tmpl')
.css('pagecreator')
.then(function($) {

	/**
	 * @class Editor.Pagecreator
	 */
	$.Controller('Editor.Pagecreator',
	/* @Static */
	{
		defaults : {
		
		}
	},
	/* @Prototype */
	{
		init : function(el){
			Page.findAll({}, this.callback('_initViews'));
		},
		
		_initViews: function(pages) {
			var pageData = new Object();
			
			this.element.html('//editor/pagecreator/views/init.tmpl', {
				pages: pages, 
				runtime: this.options.runtime,
				language: this.options.language
			});
			
			this.scrollView = this.find('div.wbl-thumbnailView').show();
			this.listView = this.find('div.wbl-listView').hide();
			this.treeView = this.find('div.wbl-treeView').hide();
			this.view = this.scrollView;
			
			var step1 = this.element.find('div#wbl-pagecreatorStep1').show();
			var step2 = this.element.find('div#wbl-pagecreatorStep2').hide();
			
			
			// Buttons
			this.element.find('button.wbl-list').button({
				icons: {primary: "wbl-iconList"},
				text: false });
			this.element.find('button.wbl-tree').button({
				icons: {primary: "wbl-iconTree"},
				disabled: false,
				text: false });
			this.element.find('button.wbl-thumbnails').button({
				disabled: false,
				icons: {primary: "wbl-iconThumbnails"},
				text: false });
			
			// TableView
			this.table = this.find('table').tablesorter({
				sortList: [[0,0]],
		        widgets: ['zebra']
			}).tablesorterPager({
				container: this.element.find("#wbl-pager"),
				positionFixed: false,
				cssNext: '.wbl-next',
				cssPrev: '.wbl-prev',
				cssFirst: '.wbl-first',
				cssLast: '.wbl-last',
				cssPageDisplay: '.wbl-pageDisplay',
				cssPageSize: '.wbl-pageSize'
			});
			
			// ThumbnailView
			this.divScroll = this.element.find('#wbl-makeMeScrollablePageCreator').smoothDivScroll({
			  	autoScroll: "onstart",
				autoScrollDirection: "left",
				autoScrollStep: 1,
				autoScrollInterval: 15,
				visibleHotSpots: "always"
		  	});
			
			// Lazy loading images
			var rootPath = this.options.runtime.getRootPath();
			this.element.find('img.wbl-pageThumbnail').lazyload({         
				placeholder: rootPath + "/editor/resourcebrowser/images/empty_thumbnail.png",
				event: "scroll",
				container: this.element.find("div.scrollWrapper")
			}).one("error", function() {
				$(this).hide().attr('src', rootPath + '/editor/resourcebrowser/images/empty_thumbnail.png').show();
			});
			
			// TreeView
			this.element.find("#wbl-tree").treeview({
				collapsed: true
			});
			
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
				title: 'Neue Seite anlegen',
				autoOpen: true,
				resizable: true,
				draggable: true,
				width: 1024,
				height: 800,
				buttons: {
					Abbrechen: $.proxy(function() {
						if(this.step.is(step2)) {
							this.nextButton.text('Weiter');
							this._showStep(step1);
							return;
						}
						
						this.element.dialog('close');
					}, this),
					Weiter: $.proxy(function() {
						if(this.step.is(step1)) {
							this.element.find('input[name=url]').before(this.parent);
							this.nextButton.text('Fertig');
							this._showStep(step2);
							return;
						}
						
						this.element.find("form#wbl-validate").submit();
						if(!this.element.find("form#wbl-validate").valid()) return;
						
						$.each(this.element.find('form#wbl-validate :input'), function(i, input) {
							pageData[$(input).attr('name')] = $(input).val();
						});
						
						pageData.url = this.parent + pageData.url;
						
						// Create page and update pageData
						Page.create({path: pageData.url}, $.proxy(function(page) {
							var path = page.getPath(); // Let this line at this position!!
							page.saveMetadata(pageData, this.options.language, $.proxy(function() {
								page.lock(this.options.runtime.getUserLogin());
								location.href = path + "?edit&_=" + new Date().getTime();
							}, this));
						}, this));
						
						this.destroy();
					}, this)
				},
				close: $.proxy(function () {
					this.destroy();
				},this)
			});
			
			this.divScroll.smoothDivScroll('recalculateScrollableArea');
			this.step = step1;
			this.nextButton = this.element.parent().find(".ui-dialog-buttonpane span.ui-button-text:contains('Weiter')");
			this.element.find("form#wbl-validate").validate();
		},
		
		destroy: function() {
			this.divScroll.smoothDivScroll('destroy');
			this.element.dialog('destroy');
			this._super();
		},
		
	    update: function(options) {
	    	this.options = options;
	    	this.element.dialog('open');
	    },
	    
		_showView: function(view) {
        	this.view.hide();
        	this.view = view;
        	view.show();
        },
	    
	    _showStep : function(step){
	    	this.step.hide();
	    	this.step = step;
	    	step.show();
	    },
	    
		"button.wbl-list click": function(el, ev) {
			this._showView(this.listView);
		},
		
		"button.wbl-tree click": function(el, ev) {
			this._showView(this.treeView);
		},
		
		"button.wbl-thumbnails click": function(el, ev) {
			this._showView(this.scrollView);
		},
		
		".filetree span.file click": function(el, ev) {
			if(this.active) this.active.removeClass('wbl-active');
			this.active = el;
			el.addClass('wbl-active');
			this.parent = el.text();
		},
		
		"div.wbl-page click": function(el, ev) {
			if(this.active) this.active.removeClass('wbl-active');
			this.active = el;
			el.addClass('wbl-active');
			this.parent = el.attr('id');
		},
		
		"input[name=title] change": function(el, ev) {
			this.element.find('input[name=url]').val(encodeURI(el.val().toLowerCase()));
		}
	    
	})

});