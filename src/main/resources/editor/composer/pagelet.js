steal.plugins('jqueryui/dialog',
		'jqueryui/draggable',
		'jqueryui/resizable',
		'jqueryui/mouse')
.models('../../models/workbench')
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
    
    _openPageEditor: function(pageletEditor) {
    	// Parse Pagelet-Editor Data
    	var data = Page.parseXML($(pageletEditor).find('pagelet:first')[0]);
    	
    	// TODO beim Parsen von Data muss currentLanguage hinzugefügt werden.
    	
    	var editor = $(pageletEditor).find('editor')[0].firstChild.nodeValue;
    	var renderer = $(pageletEditor).find('renderer')[0].firstChild.nodeValue;
    	
    	// Process Template Engine
    	var templateObject = TrimPath.parseTemplate(editor);
    	var result  = templateObject.process(data.value);
    	
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
					// TODO SAVE JSON DATA
//					this.element.trigger('duplicatePages', [this.options.selectedPages]);
//					this._showMessage('Seite dupliziert!');
					this.editorDialog.dialog('close');
				}, this)
			}
		});
    	
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
