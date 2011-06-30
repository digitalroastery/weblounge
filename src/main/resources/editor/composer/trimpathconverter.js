$.Class.extend('TrimPathConverter',
/* @static */
{
	convertText: function(input, element, pagelet) {
		if(element[0] == 'element') {
			if(!$.isEmptyObject(pagelet.locale.current)) {
				input.attr('value', pagelet.locale.current.text[element[1]]);
			}
			if(!$.isEmptyObject(pagelet.locale.original)) {
				input.attr('placeholder', pagelet.locale.original.text[element[1]]);
			}
		} 
		else if(element[0] == 'property') {
			input.attr('value', pagelet.properties.property[element[1]]);
		}
	},
	
	convertCheckbox: function(input, element, pagelet) {
		if(element[0] == 'element') {
			if(!$.isEmptyObject(pagelet.locale.current)) {
				if(pagelet.locale.current.text[element[1]] == true)
					input.attr('checked', 'checked');
				else if(pagelet.locale.current.text[element[1]] == false)
					input.removeAttr('checked');
			}
			else if(!$.isEmptyObject(pagelet.locale.original)) {
				if(pagelet.locale.original.text[element[1]] == true)
					input.attr('checked', 'checked');
				else if(!pagelet.locale.original.text[element[1]] == false)
					input.removeAttr('checked');
			}
		} 
		else if(element[0] == 'property') {
			if(pagelet.properties.property[element[1]] == true) 
				input.attr('checked', 'checked');
			else if(!pagelet.properties.property[element[1]] == false)
				input.removeAttr('checked');
		}
	},
	
	convertRadio: function(input, element, pagelet) {
		if(element[0] == 'element') {
			if(!$.isEmptyObject(pagelet.locale.current)) {
				var value = pagelet.locale.current.text[element[1]];
				if(value == undefined) return;
				if(input.val() == value) {
					input.attr('checked', 'checked');
				}
				else {
					input.removeAttr('checked');
				}
			}
			else if(!$.isEmptyObject(pagelet.locale.original)) {
				var value = pagelet.locale.original.text[element[1]];
				if(value == undefined) return;
				if(input.val() == value) {
					input.attr('checked', 'checked');
				}
				else {
					input.removeAttr('checked');
				}
			}
		} 
		else if(element[0] == 'property') {
			var value = pagelet.properties.property[element[1]];
			if(value == undefined) return;
			if(input.val() == value) {
				input.attr('checked', 'checked');
			}
			else {
				input.removeAttr('checked');
			}
		}
	},
	
	convertTextarea: function(textarea, element, pagelet) {
		if(element[0] == 'element') {
			if(!$.isEmptyObject(pagelet.locale.current)) {
				textarea.html(pagelet.properties.property[element[1]]);
			}
			if(!$.isEmptyObject(pagelet.locale.original)) {
				textarea.html('Original: ' + pagelet.locale.original.text[element[1]]);
			}
		} 
		else if(element[0] == 'property') {
			textarea.html(pagelet.properties.property[element[1]]);
		}
	},
	
	convertSelect: function(select, element) {
	},

	convertFile: function(input, element) {
	}
	
},
/* @prototype */
{
	
});