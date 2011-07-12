steal.plugins('jqueryui/dialog',
		'jqueryui/draggable',
		'jqueryui/droppable',
		'jqueryui/resizable',
		'jqueryui/mouse')
.models('../../models/workbench',
		'../../models/pagelet')
.resources('trimpath-template', 'jquery.validate.min')
.css('validation')
.then('inputconverter')
.then(function($) {

  $.Controller("Editor.Pagelet",
  /* @static */
  {
  },

  /* @prototype */
  {
    /**
     * Initialize a new Pagelet controller.
     */
    init: function(el) {
		this.element.attr('index', this.element.index());
    },
    
    update: function(options) {
    	if(options === undefined) return;
    	this.element.attr('index', this.element.index());
    	this.options.composer = options.composer;
    },
    
    _openPageEditor: function(pageletEditor) {
    	// Parse Pagelet-Editor Data
    	this.pagelet = this.options.composer.page.getEditorPagelet(this.options.composer.id, this.element.index(), this.options.composer.language);
    	
    	this.renderer = $(pageletEditor).find('renderer')[0].firstChild.nodeValue.trim();
    	var editor = $(pageletEditor).find('editor')[0].firstChild.nodeValue;
    	
    	// Load Pagelet CSS
    	$(pageletEditor).find('link').each(function(index) {
    		var test = $('head link[href="' + $(this).attr('href') + '"][rel="stylesheet"]');
    		if(test.length > 0) return;
    		$("head").append("<link>");
    	    var css = $("head").children(":last");
    	    css.attr({
    	      rel:  "stylesheet",
    	      type: $(this).attr('type'),
    	      href: $(this).attr('href')
    	    });
    	});
    	
    	// Load Pagelet Javascript
    	$(pageletEditor).find('script').each(function(index) {
    		$.getScript($(this).attr('src'));
    	});
    	
    	// Process Editor Template
    	var templateObject = TrimPath.parseTemplate(editor);
    	var result  = templateObject.process(this.pagelet);
    	
    	// Hack to create new Dom element
    	var resultDom = $('<div></div>').html(result);
    	this._convertInputs(resultDom, this.pagelet);
    	
    	$.getScript('/weblounge/editor/composer/resources/localization/messages_' + this.options.composer.language + '.js');
		this.editorDialog = $('#pageleteditor').html('<form id="validate" onsubmit="return false;">' + resultDom.html() + '</form>')
		.dialog({
			title: 'Pagelet bearbeiten',
			width: 900,
			height: 800,
			modal: true,
			autoOpen: true,
			resizable: true,
			buttons: {
				Abbrechen: function() {
					$(this).editor_dialog('close');
				},
				OK: $.proxy(function () {
					this.editorDialog.find("form#validate").submit();
					if(!this.editorDialog.find("form#validate").valid()) return;
					var newPagelet = this._getNewEditorPagelet();
					
					// Process Renderer Template
					var templateObject = TrimPath.parseTemplate(this.renderer);
					var result  = templateObject.process(newPagelet);
					
					// Save New Pagelet
			    	this.options.composer.page.insertPagelet(newPagelet, this.options.composer.id, this.element.index());
					
					// Render site
					this.element.html(result);
					this.editorDialog.editor_dialog('close');
				}, this)
			},
			close: function() {
				$(this).dialog('destroy');
			}
		});
		this.editorDialog.find("form#validate").validate();
    },
    
    // Converts Editor Input Fields with Trimpath syntax
    _convertInputs: function(editor, pagelet) {
    	$(editor).find(':input').each(function(index) {
    		var element = $(this).attr('name').split(':')
    		
    		if($(this).attr('type') == 'text' || $(this).attr('type') == 'hidden') {
    			InputConverter.convertText($(this), element, pagelet);
    		}
    		else if($(this).attr('type') == 'checkbox') {
    			InputConverter.convertCheckbox($(this), element, pagelet);
    		}
    		else if($(this).attr('type') == 'radio') {
    			InputConverter.convertRadio($(this), element, pagelet);
    		}
    		else if(this.tagName == 'TEXTAREA') {
    			InputConverter.convertTextarea($(this), element, pagelet);
    		}
    		else if(this.tagName == 'SELECT') {
    			InputConverter.convertSelect($(this), element, pagelet);
    		}
    	});
    },
    
    _getNewEditorPagelet: function() {
		// Get new values and set current to current or original
		var allInputs = this.editorDialog.find(':input');
		var newPagelet = this.pagelet;
		
		if(newPagelet.locale.current == undefined) {
			newPagelet = this._createNewLocale(newPagelet, this.options.composer.language);
		}
		
		$.each(allInputs, function(i, input) {
			var element = input.name.split(':')
    		if(input.type == 'select-one' || input.type == 'select-multiple') {
    			var optionArray = new Array();
    			$(input).find('option:selected').each(function(){
    				optionArray.push($(this).val());
    			});
    			if($.isEmptyObject(optionArray)) return;
    			if(element[0] == 'property') {
    				newPagelet.properties.property[element[1]] = optionArray.toString();
    			} 
    			else if(element[0] == 'element') {
    				newPagelet.locale.current.text[element[1]] = optionArray.toString();
    			}
    		}
    		else if(input.type == 'checkbox') {
    			if(element[0] == 'property') {
					newPagelet.properties.property[element[1]] = input.checked ? "true" : "false";
    			} 
    			else if(element[0] == 'element') {
					newPagelet.locale.current.text[element[1]] = input.checked ? "true" : "false";
    			}
    		}
    		else if(input.type == 'radio') {
    			if(input.checked == false) return;
    			if(element[0] == 'property') {
    				newPagelet.properties.property[element[1]] = input.value;
    			} 
    			else if(element[0] == 'element') {
    				newPagelet.locale.current.text[element[1]] = input.value;
    			}
    		}
    		else {
    			if(input.value == '') return;
    			if(element[0] == 'property') {
    				newPagelet.properties.property[element[1]] = input.value;
    			} 
    			else if(element[0] == 'element') {
    				newPagelet.locale.current.text[element[1]] = input.value;
    			}
    		}
		});
		return newPagelet;
    },
    
    _createNewLocale: function(pagelet, language) {
    	pagelet.locale.current = {};
    	pagelet.locale.current.text = {};
    	pagelet.locale.current.language = language;
    	pagelet.locale.current.original = false;
    	pagelet.locale.current.modified = pagelet.locale.original.modified;
    	pagelet.locale.push(pagelet.locale.current);
		return pagelet;
    },

	'hoverenter': function(ev, hover) {
      this.element.append('<div class="icon_editing"></div>');
    },

	'hoverleave': function(ev, hover) {
      this.element.find('div.icon_editing').remove();
    },

	'div.icon_editing click': function(ev) {
		Workbench.findOne({ id: this.options.composer.page.value.id, composer: this.options.composer.id, pagelet: this.element.index() }, this.callback('_openPageEditor'));
	}

  });

});
