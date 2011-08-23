steal.plugins(
	'jquery/controller',
	'jquery/controller/view',
	'jquery/view',
	'jquery/view/tmpl',
	'editor/massuploader',
	'editor/pagecreator',
	'editor/pageletcreator',
	'editor/pageheadeditor',
	'jqueryui/dialog',
	'jqueryui/draggable',
	'jqueryui/droppable',
	'jqueryui/resizable',
	'jqueryui/mouse',
	'jqueryui/button')
.views(
	'//editor/menubar/views/menubar.tmpl')
.css('menubar')
.then(function($) {

    $.Controller("Editor.Menubar",
    {
    	/**
    	 * Mode 0 = Designer
    	 * Mode 1 = Pages
    	 * Mode 2 = Media
    	 */
    	defaults: {
    		mode: 1,
    		site: {},
    		language: {}
    	}
	},
    /* @prototype */
    {
	    /**
	     * Initialize a new MenuBar controller.
	     */
        init: function(el) {
            $(el).html('//editor/menubar/views/menubar.tmpl', {runtime: this.options.runtime, current: this.options.language});
            this._updateView();
            this._initDialogs();
            this._initDragDrop();
            $('#wbl-pageletcreator').editor_pageletcreator({language: this.options.language, runtime: this.options.runtime});
            this._initPageLocking();
        },
        
        update: function(options) {
        	if(options !== undefined) {
        		this.options.mode = options.mode;
        	}
        	this._updateView();
        },
        
        /**
         * Show selected Tab
         */
        _updateView: function() {
        	switch (this.options.mode) {
	      	  case 0:
	      		  this._toggleTab(this.find('.wbl-tab.wbl-designer'));
	      		  if(!this.disabled) {
	      			  this.find('img.wbl-add').show();
	      			  this.find('img.wbl-more').show();
	      		  }
	      		  this.find('span.wbl-editmode').show();
	      		  this.find('span.wbl-language').show();
	      		  this.find('div#wbl-pageOptions').show();
	      		  $(document.body).css('overflow', 'visible');
	      		  break;
	      	  case 1:
	      		  this._toggleTab(this.find('.wbl-tab.wbl-pages'));
	      		  this.find('img.wbl-add').hide();
	      		  this.find('img.wbl-more').hide();
	      		  this.find('span.wbl-language').hide();
	      		  this.find('span.wbl-editmode').hide();
	      		  this.find('div#wbl-pageOptions').hide();
	      		  window.scrollTo(0, 0);
	      		  $(document.body).css('overflow', 'hidden');
	      		  break;
	      	  case 2:
	      		  this._toggleTab(this.find('.wbl-tab.wbl-media'));
	      		  this.find('img.wbl-add').hide();
	      		  this.find('img.wbl-more').hide();
	      		  this.find('span.wbl-language').hide();
	      		  this.find('span.wbl-editmode').hide();
	      		  this.find('div#wbl-pageOptions').hide();
	      		  window.scrollTo(0, 0);
	      		  $(document.body).css('overflow', 'hidden');
	      		  break;
        	}
        },
        
        _initDialogs: function() {
			this.userDialog = $('<div></div>')
			.load(this.options.runtime.getRootPath() + '/editor/menubar/views/user-dialog.html')
			.dialog({
				modal: true,
				title: 'Einstellungen',
				autoOpen: false,
				resizable: true,
				buttons: {
					Abbrechen: function() {
						$(this).dialog('close');
					},
					OK: $.proxy(function () {
						//TODO Save User Settings
//						this.element.trigger('saveSettings');
						this.userDialog.dialog('close');
					},this)
				}
			});
			
			this.publishDialog = $('<div></div>')
			.load(this.options.runtime.getRootPath() + '/editor/menubar/views/publish-dialog.html')
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
						this.options.page.publish($.proxy(function() {
							this._disableEditing();
						}, this), function() {
							alert('Publish error!');
						});
						this.publishDialog.dialog('close');
					},this)
				}
			});
			
        },
        
        _initDragDrop: function() {
    		this.element.find("#wbl-trashcan").droppable({
    			accept: "div.pagelet",
    			activeClass: "wbl-trashcanActive",
    			hoverClass: "wbl-trashcanHover",
    			tolerance: "pointer",
    			drop: $.proxy(function(event, ui) {
    				ui.draggable.remove();
    			}, this)
    		});
        },
        
        _initPageLocking: function() {
        	var locked = this.options.page.isLocked();
        	var userLocked = this.options.page.isLockedUser(this.options.runtime.getUserLogin())
        	if(locked && !userLocked) {
        		$('input#wbl-editmode', this.element).attr('disabled', 'disabled');
        		this._disableEditing();
        	} 
        	else if(locked && userLocked) {
        		$('input#wbl-editmode', this.element).removeAttr('disabled');
        		$('input#wbl-editmode', this.element).val(['editmode']);
        		this._enableEditing();
        	} 
        	else {
        		$('input#wbl-editmode', this.element).removeAttr('disabled');
        		$('input#wbl-editmode', this.element).val([]);
        		this._disableEditing();
        	}
        },
        
        /**
         * Delete a Cookie with setting expire to currentTime -1
         */
        _delete_cookie: function(cookieName) {
        	$.cookie(cookieName, null, { path: '/' });
        },
        
        _enableEditing: function() {
        	this.disabled = false;
        	if(this.options.page.isWorkVersion())
        		$('.composer').editor_composer('enable');
        	$('#wbl-pageletcreator').editor_pageletcreator('enable');
        	this.element.find('img.wbl-add').show();
        	this.element.find('img.wbl-more').show();
        },
        
        _disableEditing: function() {
        	this.disabled = true;
        	if(this.options.page.isWorkVersion())
        		$('.composer').editor_composer('disable');
        	$('#wbl-pageletcreator').editor_pageletcreator('disable');
        	this.element.find('img.wbl-add').hide();
        	this.element.find('img.wbl-more').hide();
        },
        
        _toggleTab: function(el) {
        	this.element.find('.wbl-tab.wbl-active').removeClass('wbl-active');
        	el.addClass('wbl-active');
        },
        
        ".wbl-tab click": function(el, ev) {
        	this._toggleTab(el);
        },
        
        ".wbl-tab.wbl-designer click": function(el, ev) {
        	el.trigger('showDesigner');
        },
        
		".wbl-tab.wbl-pages click": function(el, ev) {
			el.trigger('showPages');
		},
		
		".wbl-tab.wbl-media click": function(el, ev) {
			el.trigger('showMedia');
		},
		
		"li.wbl-settings click": function(el, ev) {
			$('.wbl-menu').hide();
			this.userDialog.dialog('open');
		},
		
		"li.wbl-publish click": function(el, ev) {
			$('.wbl-menu').hide();
			this.publishDialog.dialog('open');
		},
		
		"li.wbl-pageSettings click": function(el, ev) {
			$('.wbl-menu').hide();
			$('#wbl-pageheadeditor').editor_pageheadeditor({page: this.options.page, language: this.options.language, runtime: this.options.runtime});
		},
		
		"li.wbl-news click": function(el, ev) {
			steal.dev.log('news')
		},
		
		"img.wbl-logout click": function(el, ev) {
			this._delete_cookie("weblounge.editor");
			location.href = location.pathname + '?logout';
		},
		
		"li.wbl-newPage click": function(el, ev) {
			$('.wbl-menu').hide();
			$('#wbl-pagecreator').editor_pagecreator({language: this.options.language, runtime: this.options.runtime});
		},
		
		"li.wbl-newUpload click": function(el, ev) {
			$('.wbl-menu').hide();
			$('#wbl-massuploader').editor_massuploader({language: this.options.language, runtime: this.options.runtime});
		},
		
		"li.wbl-newNote click": function(el, ev) {
			steal.dev.log('note')
		},
		
		"li.wbl-newPagelet click": function(el, ev) {
			$('.wbl-menu').hide();
			$('#wbl-pageletcreator').editor_pageletcreator();
		},
		
		"span.wbl-languageMenu img click": function(el, ev) {
			el.trigger('changeLanguage', el.attr('title'));
		},
		
		// trigger menus
		"img.wbl-add click": function(el, ev) {
			$('.wbl-menu').hide();
//			$('div#wbl-addMenu').show().hover(function() { }, function() {$(this).hide();});
			$('#wbl-pageletcreator').editor_pageletcreator();
		},
		
		"span.wbl-language click": function(el, ev) {
			$('.wbl-menu').hide();
			$('span.wbl-languageMenu').show().hover(function() { }, function() {$(this).hide();});
		},
		
		"img.wbl-more click": function(el, ev) {
			$('.wbl-menu').hide();
			$('div#wbl-moreMenu').show().hover(function() { }, function() {$(this).hide();});
		},
		
		"span.wbl-profileMenu click": function(el, ev) {
			$('.wbl-menu').hide();
			$('div#wbl-profileMenu').show().hover(function() { }, function() {$(this).hide();});
		},
		
		"div#wbl-pageOptions click": function(el, ev) {
			el.toggle(function() {
				$(this).animate({"right": "0"}, "slow")
			}, function() {
				$(this).animate({"right": "-200px"}, "slow")
			});
		},
		
		"input focus": function(el, ev) {
			$('div#wbl-searchResult').show();
		},
		
		"input blur": function() {
			$('div#wbl-searchResult').hide();
		},
		
		"div.wbl-searchResult p.wbl-footer click": function() {
//			$('#editor').dialog( "option", "title", 'Suchresultate' ).dialog('open')
			$('div.wbl-searchResult').hide();
		},
		
		// trigger editmode
		"input#wbl-editmode click": function(el, ev) {
			ev.preventDefault();
			if(el.is(':checked')) {
				var isWorkVersion = this.options.page.isWorkVersion();
				this.options.page.lock(this.options.runtime.getUserLogin(), $.proxy(function() {
					$('input#wbl-editmode', this.element).val(['editmode']);
					// if version is life set page to work and init composer
					if(!isWorkVersion) {
						$('#weblounge-editor').editor_app('_initComposer', this.options.page);
					}
					this._enableEditing();
				}, this), $.proxy(function() {
					$('input#wbl-editmode', this.element).val([]);
					alert('Locking failed!');
				}, this));
			} else {
				this.options.page.unlock($.proxy(function() {
					$('input#wbl-editmode', this.element).val([]);
					this._disableEditing();
				}, this));
				this.publishDialog.dialog('open');
			}
		}
		
    });

});