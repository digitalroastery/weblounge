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
//    	<script type="text/javascript" src="http://scripts.swissunihockey.ch/js/jQuery/plugins/tablednd-0.5.js"></script>
		// make the table sortable
//		this.element.find('#enumeration').tableDnD({
//			dragHandle: "dragHandle"
//		});
		// add a hover-efect
		this.element.find("#enumeration tr").hover(function() {
			$(this.cells[0]).addClass('showDragHandle');
		}, function() {
			$(this.cells[0]).removeClass('showDragHandle');
		});
    },
    
	//add a new element
	_addLi: function() {
		this.element.find('tr:last').parent().append('<tr id="NEU"><td class="dragHandle">&nbsp;</td><td><textarea name="element:text" description="enumeration n" value=" " /></td></tr>');
//		dojo.query("table#enumeration > tbody > tr", document).forEach(
//			function(tr, i) {
//				tr.id = (i+1);
//			}
//		);
//		$('#enumeration').tableDnD({
//			dragHandle: "dragHandle"
//		});
		this.element.find("#enumeration tr").hover(function() {
			$(this.cells[0]).addClass('showDragHandle');
		}, function() {
			$(this.cells[0]).removeClass('showDragHandle');
		});
	},
	
	//remove an element
	_removeLi: function(element) {
		this.element.find('tr#' + element).remove();
	},
	
	"p.add-icon a click": function(el, ev) {
		this._addLi();
	},
	
	"img click": function(el, ev) {
		this._removeLi(el.attr('id'));
	}
    
});

$('#wbl-pageleteditor form').editor_enumeration();