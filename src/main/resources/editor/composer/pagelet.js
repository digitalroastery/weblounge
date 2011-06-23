steal.plugins('jqueryui/dialog',
		'jqueryui/draggable',
		'jqueryui/resizable',
		'jqueryui/mouse')
.models('../../models/workbench',
		'../../models/pagelet')
.resources('trimpath-template')
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
		this.index = this.element.index();
		this.element.attr('index', this.index);
    },
    
    update: function(options) {
    	if(options === undefined) return;
    	this.options.composer = options.composer;
    },
    
    _openPageEditor: function(pageletEditor) {
    	// Parse Pagelet-Editor Data
    	this.pagelet = this.options.composer.page.getEditorPagelet(this.options.composer.id, this.index, this.options.composer.language);
    	
//    	var pageletData = Page.parseXML($(pageletEditor).find('pagelet:first')[0]);
//    	pageletData = Pagelet.addCurrentLanguage(pageletData, 'fr');
    	this.renderer = $(pageletEditor).find('renderer')[0].firstChild.nodeValue.trim();
    	var editor = $(pageletEditor).find('editor')[0].firstChild.nodeValue;
    	
    	// Process Template Engine
    	var templateObject = TrimPath.parseTemplate(editor);
    	var result  = templateObject.process(this.pagelet);
    	
		this.editorDialog = $('<div></div>').html(result)
		.dialog({
			modal: true,
			title: 'Seite bearbeiten',
			resizable: true,
			buttons: {
				Abbrechen: function() {
					$(this).dialog('close');
				},
				OK: $.proxy(function () {
					// Get new values and set current to current or original
					var allInputs = this.editorDialog.find(':input');
					var newPagelet = this.pagelet;
					
					if(newPagelet.locale.current == undefined) {
						newPagelet = this._createNewLocale(newPagelet, this.options.composer.language);
					}
					
					$.each(allInputs, function(i, input) {
						if(input.value == '') return;
						var element = input.name.split(':')
						if(element[0] == 'property') {
							newPagelet.properties.property[element[1]] = input.value;
						} 
						else if(element[0] == 'element') {
							newPagelet.locale.current.text[element[1]] = input.value;
						}
					});
					
					// Process Renderer Template with
					var templateObject = TrimPath.parseTemplate(this.renderer);
					var result  = templateObject.process(newPagelet);
					
					// Remove Current and Original and Save
			    	this.options.composer.page.insertPagelet(newPagelet, this.options.composer.id, this.index);
					
					// Render site
					this.element.html(result);
					this.editorDialog.dialog('close');
				}, this)
			}
		});
    	
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
		Workbench.findOne({ id: this.options.composer.page.value.id, composer: this.options.composer.id, pagelet: this.index }, this.callback('_openPageEditor'));
	}

  });

});
