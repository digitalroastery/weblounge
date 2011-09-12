<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<%@ taglib uri="/WEB-INF/weblounge-resource.tld" prefix="weblr" %>

<webl:context define="site">
	<script>
		jQueryGallerie(document).ready(function() {
		    // Load the classic theme
	  		Galleria.loadTheme('/weblounge-sites/<%= site.getIdentifier() %>/modules/repository/js/galleria/themes/classic/galleria.classic.min.js');
	
		  	// Initialize Galleria
		  	jQueryGallerie("#galleria").galleria({
		        width: 500,
		        height: 500
		    });
		});
	</script>

	<webl:property define="resourceid, path, style, layout, description_alignment">
	<webl:element define="title, description">
		<%
			String[] imageIds = resourceid.split(",");
			if(imageIds.length > 1) {
	  	%>
		
		<div class="galleryPreview_<%= description_alignment %>">
			<div id="galleria">
				<%
					for(String imageId : imageIds) {
			  	%>
					<weblr:image uuid="<%= imageId %>" imagestyle="<%= style %>">
		          		<a href="<%= imageUrl %>"><img src="<%= imageUrl %>" alt="<%= description %>" title="<%= title %>" /></a>
					</weblr:image>
			  	<%
			    	}
	       		%>
			</div>
		</div>
	
	  	<%
	    	} else {
    	%>
		<weblr:image uuid="<%= imageIds[0] %>" imagestyle="<%= style %>">
    		<img src="<%= imageUrl %>" alt="<%= description %>" title="<%= title %>" />
		</weblr:image>
		<p class="galleryTitle"><webl:element name="title" /></p>
		<webl:ifelement name="description">
			<p class="galleryDescription"><%= description %></p>
		</webl:ifelement>
		<p style="clear:both"></p>
	  	<%
	    	}
      	%>
	</webl:element>
	</webl:property>
</webl:context>