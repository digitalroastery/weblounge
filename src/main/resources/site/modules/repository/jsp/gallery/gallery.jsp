<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<%@ taglib uri="/WEB-INF/weblounge-resource.tld" prefix="weblr" %>

<webl:context define="site">
	<webl:property define="resourceid, path, style, layout, description_alignment">
	<webl:element define="title, description">
		
		<div class="galleryPreview_<%= description_alignment %>">
		
					<weblr:image uuid="<%= resourceid %>" imagestyle="<%= style %>">
          <img src="<%= imageUrl %> style="width:<%= imageWidth %>px; height:<%= imageHeight %>px;" title="<%= title %>" class="galleryPreview" /><br />
					</weblr:image>
					<div id="galleria">
					<weblr:gallery subjects='<%= "set:" + path %>' normalstyle="normal" thumbstyle="thumbnail" bigstyle="gallery1x1" />
					</div>
		</div>
	
		<p class="galleryTitle"><webl:element name="title" /></p>
		<webl:ifelement name="description">
			<p class="galleryDescription"><%= description %></p>
		</webl:ifelement>
		<p style="clear:both"></p>
		<script src="/weblounge-sites/rivellagames/modules/repository/js/galleria/galleria-1.2.5.min.js"></script>
		<script>

    // Load the classic theme
  Galleria.loadTheme('/weblounge-sites/rivellagames/modules/repository/js/galleria/themes/classic/galleria.classic.min.js');

  // Initialize Galleria
  $('p.keyword').galleria();

    </script>
	</webl:element>
	</webl:property>
</webl:context>