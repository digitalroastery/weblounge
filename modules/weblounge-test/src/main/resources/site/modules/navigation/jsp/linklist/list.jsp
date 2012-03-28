<%--
 *
 * Linklist
 * Liste mit Links
 *
 * @dependency -
 * @attributes -
 * @parameters -
 *
 * @version    weblounge 2.2
 * @author     Tobias Wunden, Simon Betschmann
 * @link       http://www.swissunihockey.ch
 * @copyright  swiss unihockey
 *
--%>

<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<%@ taglib uri="/WEB-INF/weblounge-resource.tld" prefix="weblr" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ page import="ch.entwine.weblounge.common.content.page.Pagelet" %>
<%@ page import="ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils" %>
<%@ page import="ch.entwine.weblounge.common.content.ResourceUtils" %>
<%@ page import="ch.entwine.weblounge.common.url.UrlUtils" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<%@ page import="java.util.StringTokenizer" %>
<%@ page import="java.lang.Integer" %>
<%@ page import="java.lang.NumberFormatException" %>

<script type="text/javascript">
<!--
	$(document).ready(function() {
		$('div.linklist div.inner').hover(
			function () {$(this).find('a').css('color', '#c00')}, 
			function() {$(this).find('a').css('color', '#000')}
		);
	$('div.linklist div.inner').bind('click', function() {
		var link = $(this).attr('data-url');
		window.location.href = link;
		});
	});
-->
</script>

<webl:context define="user, language, site, url, composer">
<webl:property define="lead, type, categories, show, date, keyword, RSS, archiv, showkeyword">
<webl:element define="title">

  <fmt:setLocale value="${language.locale}"/>

	<%-- Spaltenbreite --%>
	<% 
		String cssClass = "LinkList";
	  String imageStyle = "image-linklist";
	  if ("main".equals(composer.getIdentifier())) {
	    cssClass += "Extended";
	  } else {
	    cssClass += "Small";
	    imageStyle += "Small";
	  }
	%>

	<div class="linklist <%= cssClass %> box red">
		<h1>
			<webl:ifproperty name="RSS">
				<a href="/weblounge-feeds/rss/2.0?subject=<%= categories.replaceAll(" ", "") %>">
			</webl:ifproperty>
			<webl:element name="title" />&nbsp; <%-- prevents the collapse if title is empty --%>
			<webl:ifproperty name="RSS">
				</a>
				<span class="LinkListRSSLink">
					<a href="/weblounge-feeds/rss/2.0?subject=<%= categories.replaceAll(" ", "") %>">
						<img src="<%= site.getOptionValue("media.url") %>/media/icons/rss_feed_12x12.png" alt="RSS Feed" title="RSS Feed" />
					</a>
				</span>
			</webl:ifproperty>
		</h1>

    <%-- Add cache tags for keywords --%>		
		<% for (String kw : categories.split(",")) { %>
			<webl:cachetag name="subject" value="<%= kw.trim() %>"/>
		<% } %>
		
		<% int i = 0; %>
		<webl:pagelist keywords="<%= categories %>" count="<%= show %>" requireheadlines="text/title">

   		<!-- Load the first title and paragraph pagelets -->
   		<%
   			Pagelet[] titlePagelets = previewPage.getStage().getPagelets("text", "title");
 				Pagelet[] imagePagelets = previewPage.getStage().getPagelets("repository", "image");
 				Pagelet titlePagelet = titlePagelets[0];
   		%>
		
			<div class="inner" data-url="<%= previewPage.getURI().getPath() %>">
			  <p class="date"><fmt:formatDate value="${previewPage.publishFrom}" type="date" dateStyle="long" /></p>
				<%-- Display the optional image --%>
				<% if (imagePagelets.length > 0) { %>
					<%
						Pagelet imagePagelet = imagePagelets[0]; 
						String resourceId = imagePagelet.getProperty("resourceid");
						String description = imagePagelet.getContent("description", language);
						String altText = "";
						String titleText = "";
					%>
					<weblr:image uuid="<%= resourceId %>" imagestyle="<%= imageStyle %>">
					  <% description = StringUtils.isNotBlank(description) ? description : image.getDescription(language); %>
					  <% altText = StringUtils.isNotBlank(description) ? "alt=\"" + description.replaceAll("\"", "'") + "\"" : ""; %>
					  <% titleText = StringUtils.isNotBlank(description) ? "title=\"" + description.replaceAll("\"", "'") + "\"" : ""; %>
						<img src="<%= imageUrl %>" <%= altText %> <%= titleText %> class="date" style="width:<%= imageWidth %>px; height:<%= imageHeight %>px;" />
					</weblr:image>	
				<% } else { %>
					<img src="<%= site.getOptionValue("media.url") %>/modules/navigation/jsp/linklist/linklist_dummy.png" alt="Kategorie: <%= titlePagelet.getContent("keyword", language) %>" title="Kategorie: <%= titlePagelet.getContent("keyword", language) %>" class="date" />
				<% } %>
				<p class="LinkListExtendedTitel">
					<a href="<%= previewPage.getURI().getPath() %>">
							<%= titlePagelet.getContent("title", language) %>
					</a>
				</p>
				<webl:ifproperty name="lead">
					<p class="lead">	
							<%= titlePagelet.getContent("lead", language) %>
					</p>
				</webl:ifproperty>
				
				<p class="categorie">
					<webl:ifproperty name="date">
					  <fmt:formatDate value="${previewPage.publishFrom}" type="both" dateStyle="short" timeStyle="short" />,
					</webl:ifproperty>
					<webl:ifproperty name="showkeyword"><span class="categorie"><%= titlePagelet.getContent("keyword", language) %></span>
					</webl:ifproperty>
					<span class="LinkListExtendedMore"><a href="<%= previewPage.getURI().getPath() %>"><webl:i18n key="module.navigation.linklist.more" /></a></span>			
				</p>
			</div>
			<webl:cachetag name="webl:uri" value="<%= previewPage.getURI().getIdentifier() %>"/>
			<% i++; %>
		</webl:pagelist>

		<webl:ifproperty name="archiv">
			<p class="archiv">
				<a href="<%= archiv %>">
					<webl:i18n key="module.navigation.linklist.archiv" />
				</a>
			</p>
		</webl:ifproperty>
	</div>

</webl:element>
</webl:property>
</webl:context>