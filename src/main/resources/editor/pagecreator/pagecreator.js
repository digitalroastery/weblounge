steal.plugins('jquery',
		'jquery/controller/view',
		'jquery/view',
		'jquery/view/tmpl',
		'jqueryui/widget')
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
			
			this.element.html('//editor/pagecreator/views/init.tmpl', {pages: pages, options: this.options.runtime.getSiteLayouts(), language: this.options.language});
			
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
			this.element.find('table').dataTable({
				"bPaginate": true,
				"bLengthChange": true,
				"bFilter": true,
				"bSort": true,
				"bInfo": true,
				"bAutoWidth": true,
				"bJQueryUI": true
			});
			
			// ThumbnailView
			var divScroll = this.element.find('#wbl-makeMeScrollablePageCreator').smoothDivScroll({
			  	autoScroll: "onstart" ,
				autoScrollDirection: "backandforth", 
				autoScrollStep: 1, 
				autoScrollInterval: 15,	
				visibleHotSpots: "always"
		  	});
			
			// TreeView
			this.element.find("#wbl-tree").treeview({
				collapsed: true
			});
			
			// TODO Load AvailableTags
			// Autocomplete Tags
			var availableTags = ["ActionScript","Scheme"];
			this.element.find("input[name=tags]").autocomplete({
				source: function(request, response) {
					// delegate back to autocomplete, but extract the last term
					response($.ui.autocomplete.filter(availableTags, request.term.split(/,\s*/).pop()));
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
							this.element.find('input[name=url]').val(this.parent);
							this.nextButton.text('Fertig');
							this._showStep(step2);
							return;
						}
						
						this.element.find("form#wbl-validate").submit();
						if(!this.element.find("form#wbl-validate").valid()) return;
						
						$.each(this.element.find('form#wbl-validate :input'), function(i, input) {
							pageData[$(input).attr('name')] = $(input).val();
						});
						
						// Create page and update pageData
						Page.create({path: pageData.url}, $.proxy(function(page) {
							page.saveCreationData(pageData, this.options.language, function() {
								location.href = page.getPath() + "?edit";
							});
						}, this));
						
						this.element.dialog('destroy');
						this.destroy();
					}, this)
				},
				close: $.proxy(function () {
					divScroll.smoothDivScroll('destroy');
					this.element.dialog('destroy');
					this.destroy();
				},this)
			});
			
			this.step = step1;
			this.nextButton = this.element.parent().find(".ui-dialog-buttonpane span.ui-button-text:contains('Weiter')");
			this.element.find("form#wbl-validate").validate();
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
			this.element.find('input[name=url]').val(this.parent + encodeURI(el.val()));
		}
	    
	})

});