steal.plugins('jquery/model').resources('../../resources/Jsonix-all-1.1-SNAPSHOT').then('page-mappings', 'file-mappings', 'image-mappings').then(function($) {

	$.Class('Editor.Jsonix',
	
	{
		context: function() {
			return new Jsonix.Context(
				// Array of mapping modules
				[PageMappings, FileMappings, ImageMappings],
				// Optional properties
				{}
			);
		}
	},
	
	{});
	
});