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
			this.img = this.element.find('.wbl-taggerImage img:first').show();
			this.element.find('div.wbl-buttonLeft:first').hide();
			
			this.index = 1;
			
			this.metadata = new Array();
			this.file = new Array();
			this._loadMetadata();
			
			if(this.options.map.length < 2) {
				this.element.find('div.wbl-buttonRight:first').hide();
			}
			
			var availableTags = ["ActionScript","Scheme"];
			var availableAuthors = ["Lukas","Markus", this.options.user];
			
			var tags = this.element.find("input[name=tags]").autocomplete({
				source: function(request, response) {
					// delegate back to autocomplete, but extract the last term
					response($.ui.autocomplete.filter(availableTags, request.term.split(/,\s*/).pop()));
				},
				focus: function() {
					// prevent value inserted on focus
					return false;
				},
				select: $.proxy(function(ev, ui) {
					var terms = tags.val().split(/,\s*/);
					// remove the current input
					terms.pop();
					// add the selected item
					terms.push(ui.item.value);
					this._saveMetadata(this.img.index(), {tags: terms.toString()});
					// add placeholder to get the comma-and-space at the end
					terms.push("");
					tags.val(terms.join(", "));
					return false;
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
							this.file[key].saveMetadata(value, this.options.language);
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
	    
	    _loadMetadata: function() {
	    	$.each(this.options.map, $.proxy(function(key, value) {
	    		Editor.File.findOne({id: value.resourceId}, $.proxy(function(file) {
	    			this.file[key] = file;
	    			this.metadata[key] = file.getMetadata(this.options.language);
	    		}, this));
	    	}, this));
	    },
	    
	    _showMetadata: function(index) {
	    	// Clear input fields
	    	this.element.find(':input').each(function() {
	    		$(this).val('');
	    	});
	    	
	    	// show metadata
			$.each(this.metadata[index], $.proxy(function(key, value) {
				this.element.find('div.wbl-metadata input[name=' + key + ']').val(value);
			},this));
	    },
	    
	    _saveMetadata: function(id, params) {
	    	$.each(params,$.proxy(function(key, value) {
	    		if($.isEmptyObject(this.metadata[id])) this.metadata[id] = {};
	    		this.metadata[id][key] = value;
	    	},this));
	    },
	    
	    "div.wbl-metadata input[name=title] change": function(el, ev) {
	    	this._saveMetadata(this.img.index(), {title: el.val()});
	    },
	    
	    "div.wbl-metadata input[name=description] change": function(el, ev) {
	    	this._saveMetadata(this.img.index(), {description: el.val()});
	    },
	    
	    "div.wbl-metadata input[name=tags] change": function(el, ev, test) {
	    	this._saveMetadata(this.img.index(), {tags: el.val()});
	    },
	    
	    "div.wbl-metadata input[name=author] change": function(el, ev) {
	    	this._saveMetadata(this.img.index(), {author: el.val()});
	    },
	    
	    "div.wbl-buttonLeft click": function(el, ev) {
	    	var prevImg = this.img.prev();
	    	if(prevImg.length < 1) return;
	    	this.img.toggle();
	    	this.img = prevImg.show();
	    	if(prevImg.prev().length < 1) el.hide();
	    	el.next().show();
	    	this._showMetadata(this.img.index());
	    	this.index--;
	    	this.element.dialog('option', 'title', 'Metadaten eingeben: Datei ' + this.index + ' / ' + this.options.map.length);
	    },
	    
	    "div.wbl-buttonRight click": function(el, ev) {
	    	var nextImg = this.img.next();
	    	if(nextImg.length < 1) return;
	    	this.img.toggle();
	    	this.img = nextImg.show();
	    	if(nextImg.next().length < 1) el.hide();
	    	el.prev().show();
	    	this._showMetadata(this.img.index());
	    	this.index++;
	    	this.element.dialog('option', 'title', 'Metadaten eingeben: Datei ' + this.index + ' / ' + this.options.map.length);
	    },
	    
	    "img.wbl-copyMetadata click": function(el, ev) {
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
