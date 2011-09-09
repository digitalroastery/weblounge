<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<%@ taglib uri="/WEB-INF/weblounge-resource.tld" prefix="weblr" %>

<webl:context define="site">
	<script type="text/javascript" src="http://127.0.0.1:8080/weblounge-sites/weblounge-test/modules/repository/js/galleria/galleria-1.2.5.min.js" ></script>
	<script>
		$(document).ready(function() {
		    // Load the classic theme
	  		Galleria.loadTheme('/weblounge-sites/<%= site.getIdentifier() %>/modules/repository/js/galleria/themes/classic/galleria.classic.min.js');
	
		  	// Initialize Galleria
		  	$("#galleria").galleria({
		        width: 500,
		        height: 500
		    });
		});
	</script>

	<webl:property define="resourceid, path, style, layout, description_alignment">
	<webl:element define="title, description">
		
		<div class="galleryPreview_<%= description_alignment %>">
			<div id="galleria">
				<%
					String[] imageIds = resourceid.split(",");
					for(String imageId : imageIds) {
			  	%>
					<weblr:image uuid="<%= imageId %>" imagestyle="<%= style %>">
		          		<img src="<%= imageUrl %>" style="width:<%= imageWidth %>px; height:<%= imageHeight %>px;" title="<%= title %>" class="galleryPreview" />
					</weblr:image>
			  	<%
			    	}
	       		%>
			</div>
			<weblr:gallery subjects='<%= "set:" + path %>' normalstyle="normal" thumbstyle="thumbnail" bigstyle="gallery1x1" />
		</div>
	
		<p class="galleryTitle"><webl:element name="title" /></p>
		<webl:ifelement name="description">
			<p class="galleryDescription"><%= description %></p>
		</webl:ifelement>
		<p style="clear:both"></p>

	</webl:element>
	</webl:property>
</webl:context>