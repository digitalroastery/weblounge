steal.plugins('jquery',
		'jquery/controller/view',
		'jquery/view',
		'jquery/view/tmpl',
		'jqueryui/autocomplete',
		'jqueryui/dialog',
		'jqueryui/draggable',
		'jqueryui/resizable',
		'jqueryui/mouse')
.models('../../models/workbench')
.views('//editor/mediaeditor/views/init.tmpl', '//editor/mediaeditor/views/content.tmpl')
.css('mediaeditor')
.then(function($) {
	
	$.Controller("Editor.Mediaeditor",	
	/* @static */
	{
  	},
  	/* @prototype */
  	{
		/**
		 * Initialize a new MediaEditor controller.
		 */
		init: function(el) {
			$(el).html('//editor/mediaeditor/views/init.tmpl', {map : this.options.map, language: this.options.language, runtime: this.options.runtime});
			
			// Initialize Buttons
			this.element.find('div.wbl-buttonLeft:first').hide();
			if(this.options.map.length < 2) {
				this.element.find('div.wbl-buttonRight:first').hide();
			}
			
			this.index = 0;
			this.metadata = new Array();
			this.file = new Array();
			
			this._initAutoComplete();
			this._initDialog();
			
			this._loadMetadata($.proxy(function() {
				this._showMetadata(0);
			}, this));
	    },
	    
	    _initDialog: function() {
			this.element.dialog({
				modal: true,
				title: 'Metadaten eingeben: Datei 1 / ' + this.options.map.length,
				autoOpen: true,
				resizable: true,
				draggable: true,
				width: 700,
				height: 650,
				buttons: {
					Abbrechen: function() {
						$(this).dialog('close');
					},
					Fertig: $.proxy(function () {
						$.each(this.metadata, $.proxy(function(key, value) {
							this.file[key].saveMetadata(value, this.options.language, null, $.proxy(function() {
								if(this.metadata.length == key + 1 && $.isFunction(this.options.success)) 
									this.options.success();
							}, this));
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
	    
	    _initAutoComplete: function() {
//	    	var tags = [
//	    	                     "ActionScript",
//	    	                     "AppleScript",
//	    	                     "Asp",
//	    	                     "BASIC",
//	    	                     "C",
//	    	                     "C++",
//	    	                     "Clojure",
//	    	                     "COBOL",
//	    	                     "ColdFusion",
//	    	                     "Erlang",
//	    	                     "Fortran",
//	    	                     "Groovy",
//	    	                     "Haskell",
//	    	                     "Java",
//	    	                     "JavaScript",
//	    	                     "Lisp",
//	    	                     "Perl",
//	    	                     "PHP",
//	    	                     "Python",
//	    	                     "Ruby",
//	    	                     "Scala",
//	    	                     "Scheme"
//	    	                 ];
	    	Workbench.suggestTags({test: 'tag'}, $.proxy(function(tags) {
				if(tags == null || tags == undefined) return;
				var inputTags = this.element.find("input[name=tags]").autocomplete({
					source: function(request, response) {
						// delegate back to autocomplete, but extract the last term
						response($.ui.autocomplete.filter(tags, request.term.split(/,\s*/).pop()));
					},
					focus: function() {
						// prevent value inserted on focus
						return false;
					},
					select: $.proxy(function(ev, ui) {
						var terms = inputTags.val().split(/,\s*/);
						// remove the current input
						terms.pop();
						// add the selected item
						terms.push(ui.item.value);
						this._saveMetadata(this.index, {inputTags: terms.toString()});
						// add placeholder to get the comma-and-space at the end
						terms.push("");
						inputTags.val(terms.join(", "));
						return false;
					},this)
				});
			}, this));
			
			Workbench.suggestTags({test: 'user'}, $.proxy(function(userTags) {
				if(userTags == null || userTags == undefined) return;
				this.element.find("input[name=author]").autocomplete({
					source: userTags,
					select: $.proxy(function(ev, ui) {
						this._saveMetadata(this.index, {author: ui.item.value});
					}, this)
				});
			}, this));
	    },
	    
	    _loadMetadata: function(success) {
	    	$.each(this.options.map, $.proxy(function(key, value) {
	    		Editor.File.findOne({id: value.resourceId}, $.proxy(function(file) {
	    			this.file[key] = file;
	    			this.metadata[key] = file.getMetadata(this.options.language);

	    			success();
	    		}, this));
	    		
	    	}, this));
	    },
	    
	    _showMetadata: function(index) {
	    	// Clear input fields
	    	this.element.find(':input').each(function() {
	    		$(this).val('');
	    	});
	    	
	    	var content = this.file[index].getContent(this.options.language);
	    	var container = this.element.find('.wbl-taggerImage');
	    	// show image
	    	if(this.file[index].name.localPart == 'movie') {
				var videoUrl = '/system/weblounge/files/' + this.file[index].value.id + '/content/' + content.language;
				container.html('<video height="270px" preload="preload" controls="controls"></video>');
				var videoTag = container.find('video').html('<source src="' + videoUrl + '" type="' + content.mimetype + '" />');
				var videoWidth = videoTag.width();
				videoTag.attr('width', videoWidth + 'px');
				var left = Math.max((container.outerWidth({margin:true}) - videoWidth) / 2, 0);
				player = new MediaElementPlayer(videoTag, {});
				container.find('div.mejs-container').css('left', left + 'px');
	    	} else if(this.file[index].name.localPart == 'image'){
	    		var previewUrl = '/system/weblounge/previews/' + this.file[index].value.id + '/locales/' + this.options.language + '/styles/weblounge-ui-preview?force=true';
	    		container.html('<img src="' + previewUrl + '" alt="Preview Image" />');
	    		var imgTag = container.find('img');
	    		var left = Math.max((container.outerWidth({margin:true}) - imgTag.width()) / 2, 0);
	    		imgTag.css('left', left + 'px');
	    	} else {
	    		container.html('No preview image');
	    		container.css('text-align', 'center');
	    	}
	    	
	    	// show metadata
			$.each(this.metadata[index], $.proxy(function(key, value) {
				this.element.find('div.wbl-metadata input[name=' + key + ']').val(value);
			},this));
			
			// show Referrers
			Editor.File.findReferrer({id: this.file[index].value.id}, $.proxy(function(referrer) {
				if(referrer == undefined) {
					this.element.find('div.wbl-referrerMediaEditor').html('Keine Verweise');
					return;
				}
				$.each(referrer, $.proxy(function(index, ref) {
			    	var page = new Page({value: ref});
					this.element.find('div.wbl-referrerMediaEditor').append(page.getTitle(this.options.language))
					.append(': <a href="' + page.getPath() + '?_=' + new Date().getTime() + '">' + page.getPath() + '</a><br />');
				}, this));
			}, this));
			
			// show content metadata
			var duration = this._formatDuration(content.duration);
			this.element.find('div.wbl-contentData').html('//editor/mediaeditor/views/content.tmpl', {file: this.file[index], content: content, duration: duration});
	    },
	    
	    _formatDuration: function(milis){
	    	if(milis == undefined) return;
	    	var duration = new Object();
	    	var temp = milis;
 
	    	duration.days = Math.floor(temp/1000/60/60/24);
	    	temp -= duration.days*1000*60*60*24;
 
	    	duration.hours = Math.floor(temp/1000/60/60);
	    	temp -= duration.hours*1000*60*60;
 
	    	duration.minutes = Math.floor(temp/1000/60);
	    	temp -= duration.minutes*1000*60;
 
	    	duration.seconds = Math.floor(temp/1000);
	    	return duration.hours + ':' + this._formatNumberLength(duration.minutes, 2) + ':' + this._formatNumberLength(duration.seconds, 2);
	    },
	    
	    _formatNumberLength: function(num, length) {
	        var number = "" + num;
	        while (number.length < length) {
	        	number = "0" + number;
	        }
	        return number;
	    },
	    
	    _saveMetadata: function(id, params) {
	    	$.each(params,$.proxy(function(key, value) {
	    		if($.isEmptyObject(this.metadata[id])) this.metadata[id] = {};
	    		this.metadata[id][key] = value;
	    	},this));
	    },
	    
	    "div.wbl-metadata input[name=title] change": function(el, ev) {
	    	this._saveMetadata(this.index, {title: el.val()});
	    },
	    
	    "div.wbl-metadata input[name=description] change": function(el, ev) {
	    	this._saveMetadata(this.index, {description: el.val()});
	    },
	    
	    "div.wbl-metadata input[name=tags] change": function(el, ev, test) {
	    	this._saveMetadata(this.index, {tags: el.val()});
	    },
	    
	    "div.wbl-metadata input[name=author] change": function(el, ev) {
	    	this._saveMetadata(this.index, {author: el.val()});
	    },
	    
	    "div.wbl-metadata input[name=path] change": function(el, ev) {
	    	this._saveMetadata(this.index, {path: el.val()});
	    },
	    
	    "div.wbl-buttonLeft click": function(el, ev) {
	    	if(this.index == 0) return;
	    	this.index--;
	    	if(this.index == 0) el.hide();
	    	el.next().show();
	    	
	    	this._showMetadata(this.index);
	    	this.element.dialog('option', 'title', 'Metadaten eingeben: Datei ' + (this.index + 1) + ' / ' + this.options.map.length);
	    },
	    
	    "div.wbl-buttonRight click": function(el, ev) {
	    	if(this.index + 1 == this.options.map.length) return;
	    	this.index++;
	    	if(this.index + 1 == this.options.map.length) el.hide();
	    	el.prev().show();
	    	
	    	this._showMetadata(this.index);
	    	this.element.dialog('option', 'title', 'Metadaten eingeben: Datei ' + (this.index + 1) + ' / ' + this.options.map.length);
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
