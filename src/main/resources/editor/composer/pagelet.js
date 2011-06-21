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
    	// Evtl. einlesen mit marshaller oder JSON in data muss von Server korrekt oder als xml kommen
    	var data = jQuery.parseJSON(pageletEditor.getElementsByTagName('data')[0].firstChild.nodeValue);
    	var editor = pageletEditor.getElementsByTagName('editor')[0].firstChild.nodeValue;
    	var renderer = pageletEditor.getElementsByTagName('renderer')[0].firstChild.nodeValue;
    	
    	var templateObject = TrimPath.parseTemplate(editor);
    	var result  = templateObject.process(data);
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
					// SAVE JSON DATA
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
		Workbench.findOne({ id: this.options.composer.page.id, composer: this.options.composer.id, pagelet: this.index }, this.callback('_openPageEditor'));
	}

  });

});
