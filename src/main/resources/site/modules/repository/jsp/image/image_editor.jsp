<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<%@ page import="java.util.HashSet"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="ch.entwine.weblounge.common.content.image.ImageStyle" %>
<%@ page import="ch.entwine.weblounge.common.site.Module" %>

<script>
	$(document).bind('pageletEditorOpen', function(event, options) {
		$('#wbl-pageleteditor div#wbl-imageEditor').editor_image(options);
	});
</script>

<webl:context define="site, language">
<div id="wbl-imageEditor">
	<p>
	    <label for="wbl-imageTitle"><webl:i18n key="module.repository.image.title"/></label> <input id="wbl-imageTitle" type="text" name="element:title" />
   		<webl:i18n key="module.repository.image.description"/><br />
       	<input name="element:description" type="text" /><br />
       	<span class="editor-sample"><webl:i18n key="module.repository.image.description.sample"/></span><br /><br />
		<button id="wbl-imageFileButton" type="button"><webl:i18n key="module.repository.image.button"/></button><br />
   	    <img src=""></img>
	    <input id="wbl-imageId" type="hidden" name="property:resourceid" />
	</p>
	<p class="editor-left">
		<webl:i18n key="module.repository.image.style"/><br />
		<select name="property:style">
			<%
				HashSet<ImageStyle> set = new HashSet<ImageStyle>();
			    for (Module m : site.getModules()) {
			      ImageStyle[] styles = m.getImageStyles();
			      for (ImageStyle style : styles) {
			        if(style.isComposeable()) set.add(style);
			      }
			    }
			
			    for(ImageStyle style : set) {
	     	%>
				<option value="<%= style.getIdentifier() %>"><%= style.getName() %></option>
	 		<%
			    }
	       	%>
		</select>
	    <webl:i18n key="module.repository.image.alignment"/><br />
		<select name="property:alignment">
			 <option value="center"><webl:i18n key="module.repository.image.center"/></option>
			 <option value="right"><webl:i18n key="module.repository.image.right"/></option>
			 <option value="left"><webl:i18n key="module.repository.image.left"/></option>
			 <option value="right_floated"><webl:i18n key="module.repository.image.right_floated"/></option>
			 <option value="left_floated"><webl:i18n key="module.repository.image.left_floated"/></option>
		</select>
		<webl:i18n key="module.repository.image.photographer"/><br />
		<input name="property:photographer" type="text" /><br />
		<span class="editor-sample"><webl:i18n key="module.repository.image.photographer.sample"/></span>
	   	<webl:i18n key="module.repository.image.link"/><br />
	    <input name="property:link" type="text" /><br />
	    <span class="editor-sample"><webl:i18n key="module.repository.image.link.sample"/></span>
	</p>
	<p class="editor-right">
		<input name="property:enlarge" type="checkbox" checked="checked" />&nbsp;<webl:i18n key="module.repository.image.enlarge"/><br />
		<input name="property:border" type="checkbox" checked="checked" />&nbsp;<webl:i18n key="module.repository.image.border"/><br />
		<input name="property:description_preview" type="checkbox" />&nbsp;<webl:i18n key="module.repository.image.description_preview"/><br />
		<input name="property:group" type="checkbox" />&nbsp;<webl:i18n key="module.repository.image.slideshow"/>
	</p>
</div>
</webl:context>