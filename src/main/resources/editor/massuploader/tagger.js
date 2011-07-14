steal.plugins('jquery',
		'jquery/controller/view',
		'jquery/view',
		'jquery/view/tmpl',
		'jqueryui/autocomplete',
		'jqueryui/dialog',
		'jqueryui/draggable',
		'jqueryui/resizable',
		'jqueryui/mouse')
.views('//editor/massuploader/views/tagger.tmpl')
.css('tagger')
.then(function($) {
	
	$.Controller("Editor.Tagger",	
	/* @static */
	{
  	},
  	/* @prototype */
  	{
		/**
		 * Initialize a new MassUploader controller.
		 */
		init: function(el) {
			$(el).html('//editor/massuploader/views/tagger.tmpl', {map : this.options.map, language: this.options.language, runtime: this.options.runtime});
			this.img = this.element.find('.taggerImage img:first').show();
			this.element.find('div.buttonLeft:first').hide();
			
			this.index = 1;
			
			this.metadata = new Array();
			this._loadMetadata(this.img.index());
			
			if(this.options.map.length < 2) {
				this.element.find('div.buttonRight:first').hide();
			}
			
			var availableTags = ["ActionScript","Scheme"];
			var availableAuthors = ["Lukas","Markus", this.options.user];
			
			this.element.find("input[name=tags]").autocomplete({
				source: availableTags,
				select: $.proxy(function(ev, ui) {
					this._saveMetadata(this.img.index(), {tags: ui.item.value});
				},this)
			});
			this.element.find("input[name=author]").autocomplete({
				source: availableAuthors,
				select: $.proxy(function(ev, ui) {
					this._saveMetadata(this.img.index(), {author: ui.item.value});
				},this)
			});
			
			this.element.dialog({
				modal: true,
				title: 'Metadaten eingeben: Datei 1 / ' + this.options.map.length,
				autoOpen: true,
				resizable: true,
				buttons: this.options.buttons,
				width: 900,
				height: 800,
				buttons: {
					Abbrechen: function() {
						$(this).dialog('close');
					},
					Fertig: $.proxy(function () {
						$.each(this.metadata, $.proxy(function(key, value) {
							var resourceId = this.options.map[key];
							// SAVE Metadata REST Endpoint resourceid value
							steal.dev.log('save metadata id: ' + resourceId + 'value: ' + value);
				    	},this));
						
						this.element.dialog('close');
					},this)
				},
				close: $.proxy(function () {
					this.element.dialog('destroy');
					this.destroy();
				},this)
			});
	    },
	    
	    _loadMetadata: function(index) {
	    	if(this.metadata[index] == undefined) {
				// Load Metadata from Server!
	    		var resourceId = this.options.map[index];
				steal.dev.log('load metadata for: ' + resourceId);
				this.metadata[index] = {};
			}
	    	
	    	// Clear input fields
	    	this.element.find(':input').each(function() {
	    		$(this).val('');
	    	});
	    	
	    	// show metadata
			$.each(this.metadata[index], $.proxy(function(key, value) {
				this.element.find('div.metadata input[name=' + key + ']').val(value);
			},this));
	    },
	    
	    _saveMetadata: function(id, params) {
	    	var index = id;
	    	var resourceId = this.options.map[index];
	    	$.each(params,$.proxy(function(key, value) {
	    		if($.isEmptyObject(this.metadata[index])) this.metadata[index] = {};
	    		this.metadata[index][key] = value;
	    	},this));
	    },
	    
	    "div.metadata input[name=title] change": function(el, ev) {
	    	this._saveMetadata(this.img.index(), {title: el.val()});
	    },
	    
	    "div.metadata input[name=description] change": function(el, ev) {
	    	this._saveMetadata(this.img.index(), {description: el.val()});
	    },
	    
	    "div.metadata input[name=tags] change": function(el, ev, test) {
	    	this._saveMetadata(this.img.index(), {tags: el.val()});
	    },
	    
	    "div.metadata input[name=author] change": function(el, ev) {
	    	this._saveMetadata(this.img.index(), {author: el.val()});
	    },
	    
	    "div.buttonLeft click": function(el, ev) {
	    	var prevImg = this.img.prev();
	    	if(prevImg.length < 1) return;
	    	this.img.toggle();
	    	this.img = prevImg.show();
	    	if(prevImg.prev().length < 1) el.hide();
	    	el.next().show();
	    	this._loadMetadata(this.img.index());
	    	this.index--;
	    	this.element.dialog('option', 'title', 'Metadaten eingeben: Datei ' + this.index + ' / ' + this.options.map.length);
	    },
	    
	    "div.buttonRight click": function(el, ev) {
	    	var nextImg = this.img.next();
	    	if(nextImg.length < 1) return;
	    	this.img.toggle();
	    	this.img = nextImg.show();
	    	if(nextImg.next().length < 1) el.hide();
	    	el.prev().show();
	    	this._loadMetadata(this.img.index());
	    	this.index++;
	    	this.element.dialog('option', 'title', 'Metadaten eingeben: Datei ' + this.index + ' / ' + this.options.map.length);
	    },
	    
	    "img.copyMetadata click": function(el, ev) {
	    	var instance = this;
	    	$.each(this.options.map, function(key, value) {
	    		var name = el.prev().attr('name');
	    		switch(name) {
	    			case 'title': instance._saveMetadata(key, {title: el.prev().val()});
	    			break;
	    			case 'description': instance._saveMetadata(key, {description: el.prev().val()});
	    			break;
	    			case 'tags': instance._saveMetadata(key, {tags: el.prev().val()});
	    			break;
	    			case 'author': instance._saveMetadata(key, {author: el.prev().val()});
	    			break;
	    		}
	    	});
	    }
	    
  	});
});
