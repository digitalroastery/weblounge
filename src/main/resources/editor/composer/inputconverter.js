$.Class.extend('InputConverter',
/* @static */
{
	convertText: function(input, element, pagelet) {
		if(element[0] == 'element') {
			if(InputConverter.existsCurrent(pagelet, element[1])) {
				input.attr('value', pagelet.locale.current.text[element[1]].toString());
			}
			if(InputConverter.existsOriginal(pagelet, element[1])) {
				input.attr('placeholder', pagelet.locale.original.text[element[1]].toString());
			}
		} 
		else if(element[0] == 'property') {
			if(!InputConverter.existsProperty(pagelet, element[1])) return;
			input.attr('value', pagelet.properties.property[element[1]].toString());
		}
	},
	
	convertCheckbox: function(input, element, pagelet) {
		InputConverter.convert(element, pagelet,
		function() {
			if(pagelet.locale.current.text[element[1]].toString() == "true")
				input.attr('checked', 'checked');
			else if(pagelet.locale.current.text[element[1]].toString() == "false")
				input.removeAttr('checked');
		}, 
		function() {
			if(pagelet.locale.original.text[element[1]].toString() == "true")
				input.attr('checked', 'checked');
			else if(pagelet.locale.original.text[element[1]].toString() == "false")
				input.removeAttr('checked');
		},
		function() {
			if(pagelet.properties.property[element[1]].toString() == "true") 
				input.attr('checked', 'checked');
			else if(pagelet.properties.property[element[1]].toString() == "false")
				input.removeAttr('checked');
		});
	},
	
	convertRadio: function(input, element, pagelet) {
		InputConverter.convert(element, pagelet,
		function() {
			var value = pagelet.locale.original.text[element[1]].toString();
			if(input.val() == value)
				input.attr('checked', 'checked');
			else input.removeAttr('checked');
		}, 
		function() {
			var value = pagelet.locale.original.text[element[1]].toString();
			if(input.val() == value) 
				input.attr('checked', 'checked');
			else input.removeAttr('checked');
		},
		function() {
			if(input.val() == pagelet.properties.property[element[1]].toString()) 
				input.attr('checked', 'checked');
			else input.removeAttr('checked');
		});
	},
	
	convertTextarea: function(textarea, element, pagelet) {
		InputConverter.convert(element, pagelet,
		function() {
			textarea.html(pagelet.locale.original.text[element[1]].toString());
		}, 
		function() {
			textarea.html(pagelet.locale.original.text[element[1]].toString());
		},
		function() {
			textarea.html(pagelet.properties.property[element[1]].toString());
		});
	},
	
	convertSelect: function(select, element, pagelet) {
		$(select).find('option').each(function(){
			InputConverter.convert(element, pagelet,
			$.proxy(function () {
				var array = pagelet.locale.current.text[element[1]];
				
				if($.inArray($(this).val(), array) == -1) 
					$(this).removeAttr('selected');
				else $(this).attr('selected', 'selected');
			}, this),
			$.proxy(function () {
				var array = pagelet.locale.original.text[element[1]];
				if($.inArray($(this).val(), array) == -1) 
					$(this).removeAttr('selected');
				else $(this).attr('selected', 'selected');
			}, this),
			$.proxy(function () {
				var array = pagelet.properties.property[element[1]];
				if($.inArray($(this).val(), array) == -1) 
					$(this).removeAttr('selected');
				else $(this).attr('selected', 'selected');
			}, this));
		});
	},
	
	existsCurrent: function(pagelet, element) {
		if($.isEmptyObject(pagelet.locale.current)) return false;
		return !$.isEmptyObject(pagelet.locale.current.text[element]);
	},
	
	existsOriginal: function(pagelet, element) {
		if($.isEmptyObject(pagelet.locale.original)) return false;
		return !$.isEmptyObject(pagelet.locale.original.text[element]);
	},
	
	existsProperty: function(pagelet, property) {
		return !$.isEmptyObject(pagelet.properties.property[property]);
	},
	
	convert: function(element, pagelet, currentFunction, originalFunction, propertyFuction) {
		if(element[0] == 'element') {
			if(InputConverter.existsCurrent(pagelet, element[1])) {
				currentFunction();
			}
			else if(InputConverter.existsOriginal(pagelet, element[1])) {
				originalFunction();
			}
		} 
		else if(element[0] == 'property') {
			if(!InputConverter.existsProperty(pagelet, element[1])) return;
			propertyFuction();
		}
	}

},
/* @prototype */
{
	
});