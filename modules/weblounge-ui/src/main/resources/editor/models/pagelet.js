steal.then('jsonix')
.then(function($) {
	
	unmarshalPage = function(data, success) {
		var unmarshaller = Editor.Jsonix.context().createUnmarshaller();
		var json = unmarshaller.unmarshalDocument(data);
		success(json.value.page); 
	};
	
	$.Model.extend('Pagelet',
	/* @Static */
	{
		finaAll : "",
		
		findOne: function(params, success, error) {
			if ('id' in params) {
				$.ajax('/system/weblounge/pages/'+ params.id + '/composer/' + params.composer + 'pagelets' + params.index, {
					success: function(xml) {
						unmarshalPage(xml, success);
					}
				});
			}
		},
		
		addCurrentLanguage: function(pagelet, language) {
			$.each(pagelet.value.locale, function(i, locale) {
				if(locale.language == language) {
					pagelet.value.locale.current = locale;
				}
				if(locale.original == true) {
					pagelet.value.locale.original = locale;
				}
			});
			return pagelet;
		},
		
		removeCurrentLanguage: function(pagelet) {
			delete pagelet.value.locale.current;
			delete pagelet.value.locale.original;
			return pagelet;
		}
	},
	/* @Prototype */
	{});
});