steal.then('jsonix')
.then(function($) {
	
	unmarshalPage = function(data, success) {
		var unmarshaller = Editor.Jsonix.context().createUnmarshaller();
		var json = unmarshaller.unmarshalDocument(data);
		success(json.value.page); 
	};

	$.Model.extend('Composer',
	/* @Static */
	{
		finaAll : "",
		findOne: function(params, success, error) {
			if ('id' in params) {
				$.ajax('/system/weblounge/pages/'+ params.id + '/composer/' + params.composer, {
					success: function(xml) {
						unmarshalPage(xml, success);
					}
				});
			}
		}
	},
	/* @Prototype */
	{});
});