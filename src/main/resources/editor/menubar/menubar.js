steal.plugins(
	'jquery/controller',
	'jquery/controller/view',
	'jquery/view',
	'jquery/view/tmpl',
	'jqueryui/dialog',
	'jqueryui/button')
.views(
	'//editor/menubar/views/menubar.tmpl')
.css(
	'menubar',
	'css/blitzer/jquery-ui-1.8.11')
.then(function($) {

    $.Controller("Editor.Menubar",
    {
    	/**
    	 * Mode 0 = Designer
    	 * Mode 1 = Pages
    	 * Mode 2 = Media
    	 */
    	defaults: {
    		mode: 1
    	}
	},
    /* @prototype */
    {
	    /**
	     * Initialize a new MenuBar controller.
	     */
        init: function(el) {
            $(el).html('//editor/menubar/views/menubar.tmpl', {});
            this._updateView();
            this._initDialogs();
        },
        
        update: function(options) {
        	if(options !== undefined) {
        		this.options.mode = options.mode;
        	}
        	this._updateView();
        },
        
        _updateView: function() {
        	switch (this.options.mode) {
	      	  case 0:
	      		  this.toolbarMore = this.find('img.more').show();
	      		  this.toolbarEdit = this.find('span.editmode').show();
	      		  this.pageOptions = this.find('div#page_options').show();
	      		  this.element.find('.tab.active').removeClass('active');
	      		  break;
	      	  case 1:
	      		  this.toolbarMore = this.find('img.more').hide();
	      		  this.toolbarEdit = this.find('span.editmode').hide();
	      		  this.pageOptions = this.find('div#page_options').hide();
	      		  break;
	      	  case 2:
	      		  this.toolbarMore = this.find('img.more').hide();
	      		  this.toolbarEdit = this.find('span.editmode').hide();
	      		  this.pageOptions = this.find('div#page_options').hide();
	      		  break;
        	}
        },
        
        _initDialogs: function() {
			this.addDialog = $('<div></div>')
			.load('weblounge/editor/menubar/views/add-dialog.html')
			.dialog({
				modal: true,
				title: 'Seite hinzufügen',
				autoOpen: false,
				resizable: false,
				buttons: {
					Abbrechen: function() {
						$(this).dialog('close');
					},
					OK: $.proxy(function () {
//						this.element.trigger('addPages');
						this.addDialog.dialog('close');
					},this)
				},
			});
			
			this.userDialog = $('<div></div>')
			.load('weblounge/editor/menubar/views/user-dialog.html')
			.dialog({
				modal: true,
				title: 'Einstellungen',
				autoOpen: false,
				resizable: false,
				buttons: {
					Abbrechen: function() {
						$(this).dialog('close');
					},
					OK: $.proxy(function () {
//						this.element.trigger('addPages');
						this.userDialog.dialog('close');
					},this)
				},
			});
			
			this.publishDialog = $('<div></div>')
			.load('weblounge/editor/menubar/views/publish-dialog.html')
			.dialog({
				modal: true,
				title: 'Seite publizieren',
				autoOpen: false,
				resizable: false,
				buttons: {
					Nein: function() {
						$(this).dialog('close');
					},
					Ja: $.proxy(function () {
//						this.element.trigger('addPages');
						this.userDialog.dialog('close');
					},this)
				},
			});
        },
        
        
        ".tab click": function(el, ev) {
        	this.element.find('.tab.active').removeClass('active');
        	el.addClass('active');
        },
        
		".tab.pages click": function(el, ev) {
			el.trigger('showPages');
		},
		
		".tab.media click": function(el, ev) {
			el.trigger('showMedia');
		},
		
		"li.settings click": function(el, ev) {
			$('.menu').hide();
			this.userDialog.dialog('open');
		},
		
		"li.news click": function(el, ev) {
			// TODO
			steal.dev.log('news')
		},
		
		"li.logout click": function(el, ev) {
			// TODO
			steal.dev.log('logout')
		},
		
		"li.new_page click": function(el, ev) {
			$('.menu').hide();
			this.addDialog.dialog('open');
		},
		
		"li.new_upload click": function(el, ev) {
			// TODO
			steal.dev.log('upload')
		},
		
		"li.new_note click": function(el, ev) {
			// TODO
			steal.dev.log('note')
		},
	
		// trigger menus
		".editor_menubar img.add click": function(el, ev) {
			$('.menu').hide();
			$('div#add-menu').show().hover(function() { }, function() {$(this).hide();});
		},
		
		".editor_menubar img.more click": function(el, ev) {
			$('.menu').hide();
			$('div#more-menu').show().hover(function() { }, function() {$(this).hide();});
		},
		
		".editor_menubar span.profile-menu click": function(el, ev) {
			$('.menu').hide();
			$('div#profile-menu').show().hover(function() { }, function() {$(this).hide();});
		},
		
		"div#page_options click": function(el, ev) {
			el.toggle(function() {
				$(this).animate({"right": "0"}, "slow")
			}, function() {
				$(this).animate({"right": "-200px"}, "slow")
			});
		},
		
		".editor_menubar input focus": function(el, ev) {
			$('div#search-result').show();
		},
		
		".editor_menubar input blur": function() {
			$('div#search-result').hide();
		},
		
		".div.search-result p.footer click": function() {
//			$('#editor').dialog( "option", "title", 'Suchresultate' ).dialog('open')
			$('div.search-result').hide();
		},
		
		/* move to new plugin "designer" */
		".pagelet hover": function() {
			$(this).addClass('hover');
		},
		
		// trigger editmode
		"input#editmode click": function(el, ev) {
			if(el.is(':checked')) {
//				steal.dev.log(location.href);
//				steal.dev.log(window.location.search);
				$('body').load(location.href + '?mode=edit');
				// Reload Page with Parameter mode=edit oder so
//				steal.dev.log('editmode is enabled');
			} else {
				// Open Publish Dialog an reload Page in normal mode
				steal.dev.log('editmode is disabled');
				this.publishDialog.dialog('open');
			}
		}
		
    });

});