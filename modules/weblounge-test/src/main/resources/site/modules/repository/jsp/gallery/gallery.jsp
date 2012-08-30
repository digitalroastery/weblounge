<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>
<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/resources" prefix="weblr" %>

<webl:context define="site">
	<script>
		var galleriaData = [];
		jQueryGalleria(document).ready(function() {
		    // Load the classic theme
	  		Galleria.loadTheme('/weblounge-sites/<%= site.getIdentifier() %>/modules/repository/js/galleria/themes/classic/galleria.classic.min.js');
	
		  	// Initialize Galleria
		  	jQueryGalleria("#galleria").galleria({
		        width: 500,
		        height: 500,
		        autoplay: 6000,
		        data_source: galleriaData,
		  	    extend: function(options) {
		  	        // listen to when an image is shown
		  	        this.bind('image', function(e) {
		  	            // lets make galleria open a lightbox when clicking the main image:
		  	            jQueryGalleria(e.imageTarget).click(this.proxy(function() {
		  	               this.openLightbox();
		  	            }));
		  	        });
		  	    }
		    });
		});
	</script>

	<webl:property define="resourceid">
	<webl:element define="title, description">
		<% String[] imageIds = resourceid.split(","); %>
		<div id="galleria">
			<%
				for(String imageId : imageIds) {
		  	%>
				<weblr:image uuid="<%= imageId %>" >
			  		<script type="text/javascript">
			  		galleriaData.push({
		            	image: '<%= imageUrl %>',
		              	title: '<%= imageTitle %>',
		              	description: '<%= imageDescription %>'
  		          	});
			  		</script>
				</weblr:image>
		  	<%
		    	}
       		%>
		</div>
	</webl:element>
	</webl:property>
</webl:context>