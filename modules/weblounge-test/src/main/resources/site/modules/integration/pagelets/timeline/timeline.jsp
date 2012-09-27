<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>

<webl:context define="uri, site, language, pagelet">
<webl:property define="height,timeline">

  <div id="timeline-embed"></div>
	<script type="text/javascript">
	    var timeline_config = {
			width:				     '100%',
			height:				      '<%= height %>',
			source:				      '<%= timeline %>',
			embed_id:			     'timeline-embed',	//OPTIONAL USE A DIFFERENT DIV ID FOR EMBED
			start_at_end: 		 false,							//OPTIONAL START AT LATEST DATE
			start_at_slide:		 '1',							  //OPTIONAL START AT SPECIFIC SLIDE
			start_zoom_adjust: '1',							  //OPTIONAL TWEAK THE DEFAULT ZOOM LEVEL
			hash_bookmark:		 true,							//OPTIONAL LOCATION BAR HASHES
			lang:				       'de',							//OPTIONAL LANGUAGE
			maptype:		       'watercolor',			//OPTIONAL MAP STYLE
			css:				       '/weblounge-sites/<%= site.getIdentifier() %>/modules/integration/css/timeline/timeline.css',		//OPTIONAL PATH TO CSS
			js:					       '/weblounge-sites/<%= site.getIdentifier() %>/modules/integration/js/timeline/timeline.js'	      //OPTIONAL PATH TO JS
		}
	</script>

</webl:property>
</webl:context>