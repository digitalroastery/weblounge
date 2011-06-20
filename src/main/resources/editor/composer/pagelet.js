steal.plugins('jqueryui/widget')
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
		steal.dev.log('init pagelet with index ' + this.index);
    },
    
    _openPageEditor: function(editor) {
    	// TODO Editordialog
//    	var data = editor.getData();
//    	var template = editor.getTemplate();
//    	
//    	var templateObject = TrimPath.parseTemplate (template);
//    	var result  = templateObject.process(data);
//    	
//    	steal.dev.log(editor);
//		this.editorDialog = $('<div></div>').html('')
//		.dialog({
//			modal: true,
//			title: 'Seite bearbeiten',
//			resizable: true,
//			draggable: true,
//			buttons: {
//				Abbrechen: function() {
//					$(this).dialog('close');
//				},
//				OK: $.proxy(function () {
////					this.element.trigger('duplicatePages', [this.options.selectedPages]);
////					this._showMessage('Seite dupliziert!');
//					this.editorDialog.dialog('close');
//				}, this)
//			}
//		});
    	
    },

	'hoverenter': function(ev, hover) {
      this.element.append('<div class="icon_editing"></div>');
    },

	'hoverleave': function(ev, hover) {
      this.element.find('div.icon_editing').remove();
    },

	'div.icon_editing click': function(ev) {
		Workbench.findOne({ id: this.options.composer.pageId, composer: this.options.composer.id, pagelet: this.index }, this.callback('_openPageEditor'));
		steal.dev.log('editing pagelet with index ' + this.index + ' in composer ' + this.options.composer.id);
	}

  });

});
