<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<%@ taglib uri="http://jsptags.com/tags/navigation/pager" prefix="pg" %>

<%@ page import="java.util.List" %>
<%@ page import="ch.entwine.weblounge.api.repository.ImageResource" %>
<%@ page import="ch.entwine.weblounge.api.repository.ImageStyle" %>

<webl:context define="site, language, toolkit">
<%
	List images = (List) request.getAttribute("images");
   String style = "simple"; //getParam(request, "style", "simple");
   String position = "top"; // getParam(request, "position", "top");
   String index = "center"; //getParam(request, "index", "center");
   int maxPageItems = 10; //getParam(request, "maxPageItems", 9);
   int maxIndexPages = 10; //getParam(request, "maxIndexPages", 10);
   ImageStyle imgStyleSmall = site.getImageStyles().getStyle("gallery3x3");
   ImageStyle imgStyleLarge = site.getImageStyles().getStyle("highresolution");
   String gallery = request.getParameter("gallery");
%>

<pg:pager
   items="<%= images.size() %>"
   index="<%= index %>"
   maxPageItems="<%= maxPageItems %>"
   maxIndexPages="<%= maxIndexPages %>"
   isOffset="true"
   export="offset,currentPageNumber=pageNumber"
	scope="request">

	<%-- keep track of preference --%>
	<pg:param name="gallery"/>
	<pg:param name="layout"/>

	<table class="gallery_3x3">
	<%
		int count = offset.intValue();
		ImageResource image = null;
		String link1x1 = toolkit.linkAction("repository", "gallery") + "?gallery=" + gallery + "&layout=11";
		gallery: for (int i = 0; i < 4 && count < images.size(); i++) 
		{
	%>
		<tr>
		<%
			for (int j=0; j < 2; j++)
				{
					count = offset.intValue() + 2*i + j;
					if (count < images.size()) {
						image = (ImageResource)images.get(count);
		%>
			<td>
				<pg:item>
					<%-- TODO: don't show "null" as imgDescription --%>
					<a href="<%= image.getLinkPath(imgStyleLarge) %>" dojoType="dojox.image.Lightbox" group="slideshow" title="<%= (image.getDescription(language) != null && !image.getDescription(language).equals("") && !image.getDescription(language).equals("null")) ? image.getDescription(language) : "" %>">					
						<img src="<%= image.getLinkPath(imgStyleSmall) %>" style="border:0;" /><br/>
					</a>
				</pg:item>
			</td>
		<%
			} else {
		%>
		<td>&nbsp;</td>
	<%
			}
		}
	%>
	</tr>
	<%
		}
	%>
	</table>

	<p class="galleryIndex">
	<pg:prev export="pageUrl">&nbsp;[<a href="<%= pageUrl %>"><webl:i18n name="module.repository.gallery.prev" /></a>]</pg:prev>
	<pg:pages>
		<%
			if (pageNumber.intValue() < 10) {
		%>&nbsp;<%
			}
			if (pageNumber == currentPageNumber) {
		%><b><%= pageNumber %></b><%
			} else {
		%><a href="<%= pageUrl %>"><%= pageNumber %></a><%
			}
		%>
	</pg:pages>
	<pg:next export="pageUrl">&nbsp;[<a href="<%= pageUrl %>"><webl:i18n name="module.repository.gallery.next" /></a>]</pg:next>
	</p>
	
</pg:pager>
</webl:context>

<%!
	private static final String getParam(ServletRequest request, String name,
	    String defval)
		{
	    String param = request.getParameter(name);
	    return (param != null ? param : defval);
		}
	
	private static final int getParam(ServletRequest request, String name,
	    int defval)
		{
	    String param = request.getParameter(name);
	    int value = defval;
	    if (param != null) {
		try { value = Integer.parseInt(param); }
		catch (NumberFormatException ignore) { }
	    }
	    return value;
		}
%>