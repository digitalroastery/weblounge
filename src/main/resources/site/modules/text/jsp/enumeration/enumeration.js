$.Controller("Editor.Enumeration",
/* @static */
{
},

/* @prototype */
{
	
    /**
     * Initialize a new EnumerationEditor controller.
     */
    init: function(el) {
    	this.element.find('#enumeration').tableDnD({
    		dragHandle: "dragHandle",
    		onDrop: $.proxy(function(table, row) {
    			this._updateIndex();
            }, this)
    	});

		// add a hover-efect
		this.element.find("#enumeration tr").hover(function() {
			$(this.cells[0]).addClass('showDragHandle');
		}, function() {
			$(this.cells[0]).removeClass('showDragHandle');
		});
    },
    
	//add a new element
	_addLi: function() {
		this.element.find('tr:last').parent().append('<tr id="x"><td class="dragHandle">&nbsp;</td><td><textarea name="element:text"></textarea> <img id="x" src="/weblounge-sites/' + 
				this.options.siteId + '/modules/text/jsp/enumeration/delete.png" alt="del" title="' + 
				this.options.title + '" /></td></tr>');
		
		this._updateIndex();
		
		$('#enumeration').tableDnD({
			dragHandle: "dragHandle",
    		onDrop: $.proxy(function(table, row) {
    			this._updateIndex();
            }, this)
		});
		
		this.element.find("#enumeration tr").hover(function() {
			$(this.cells[0]).addClass('showDragHandle');
		}, function() {
			$(this.cells[0]).removeClass('showDragHandle');
		});
	},
	
	_updateIndex: function() {
		this.element.find('table#enumeration > tbody > tr').each(function(i, tr) {
			$(tr).attr('id', i);
			$(tr).find('img').attr('id', i);
			$(tr).find('textarea').attr('name', 'element:text' + i);
		});
	},
	
	//remove an element
	_removeLi: function(element) {
		this.element.find('tr#' + element).remove();
		this._updateIndex();
	},
	
	"p.add-icon a click": function(el, ev) {
		ev.preventDefault();
		ev.stopPropagation();
		this._addLi();
	},
	
	"img click": function(el, ev) {
		this._removeLi(el.attr('id'));
	}
    
});