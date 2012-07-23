/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software Foundation
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.entwine.weblounge.workbench;

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.Composer;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.content.page.PageletRenderer;
import ch.entwine.weblounge.common.content.repository.ContentRepository;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.impl.content.page.PageReader;
import ch.entwine.weblounge.common.impl.testing.MockHttpServletRequest;
import ch.entwine.weblounge.common.impl.testing.MockHttpServletResponse;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.workbench.suggest.SimpleSuggestion;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Implementation of a weblounge workbench. The workbench provides support for
 * management applications and the page editor.
 */
public class WorkbenchService {

  /** The logging facility */
  private static Logger logger = LoggerFactory.getLogger(WorkbenchService.class);

  /** The site servlets */
  private static Map<String, Servlet> siteServlets = new HashMap<String, Servlet>();

  /** The cache service tracker */
  private ServiceTracker siteServletTracker = null;

  /** Filter expression used to look up site servlets */
  private static final String serviceFilter = "(&(objectclass=" + Servlet.class.getName() + ")(" + Site.class.getName().toLowerCase() + "=*))";

  /**
   * Callback from OSGi declarative services on component startup.
   * 
   * @param ctx
   *          the component context
   */
  void activate(ComponentContext ctx) {
    try {
      Filter filter = ctx.getBundleContext().createFilter(serviceFilter);
      siteServletTracker = new SiteServletTracker(ctx.getBundleContext(), filter);
      siteServletTracker.open();
    } catch (InvalidSyntaxException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Callback from OSGi declarative services on component shutdown.
   */
  void deactivate() {
    if (siteServletTracker != null) {
      siteServletTracker.close();
    }
  }

  /**
   * Returns a list of tags from the given site that are suggested based on what
   * is passed in as <code>text</code>. If <code>limit</code> is larger than
   * <code>0</code>, then this is the maximum number of facet values returned.
   * 
   * @param site
   *          the site
   * @param text
   *          the starting test
   * @param limit
   *          the maximum number of tags to return
   * @return the list of suggested tags
   * @throws IllegalStateException
   *           if the content repository is not available
   * @throws ContentRepositoryException
   *           if querying fails
   */
  public List<SimpleSuggestion> suggestTags(Site site, String text, int limit)
      throws IllegalStateException {
    List<SimpleSuggestion> tags = new ArrayList<SimpleSuggestion>();
    List<String> suggestions;
    try {
      suggestions = site.getContentRepository().suggest("subject", text, limit);
      for (String s : suggestions) {
        tags.add(new SimpleSuggestion("subject", s));
      }
    } catch (ContentRepositoryException e) {
      logger.warn("Error loading subject suggestions for '" + text + "'", e);
      return null;
    }
    return tags;
  }

  /**
   * Returns the pagelet editor or <code>null</code> if either one of the page,
   * the composer or the is not available.
   * 
   * @param site
   *          the site
   * @param pageURI
   *          the page uri
   * @param composerId
   *          the composer id
   * @param pageletIndex
   *          the pagelet index
   * @param language
   *          the environment
   * @param environment
   *          the execution environment
   * @return the pagelet editor
   * @throws IOException
   *           if reading the pagelet fails
   */
  public PageletEditor getEditor(Site site, ResourceURI pageURI,
      String composerId, int pageletIndex, String language,
      Environment environment) throws IOException {
    if (site == null)
      throw new IllegalArgumentException("Site must not be null");
    if (composerId == null)
      throw new IllegalArgumentException("Composer must not be null");
    if (pageletIndex < 0)
      throw new IllegalArgumentException("Pagelet index must be a positive integer");

    Page page = getPage(site, pageURI);
    if (page == null) {
      logger.warn("Client requested pagelet editor for non existing page {}", pageURI);
      return null;
    }

    // Load the composer
    Composer composer = page.getComposer(composerId);
    if (composer == null) {
      logger.warn("Client requested pagelet editor for non existing composer '{}' on page {}", composerId, pageURI);
      return null;
    }

    // Get the pagelet
    if (composer.getPagelets().length <= pageletIndex || composer.size() <= pageletIndex) {
      logger.warn("Client requested pagelet editor for non existing pagelet on page {}", pageURI);
      return null;
    }

    Pagelet pagelet = composer.getPagelet(pageletIndex);
    pagelet = new TrimpathPageletWrapper(pagelet);
    PageletEditor pageletEditor = new PageletEditor(pagelet, pageURI, composerId, pageletIndex, environment);

    // Load the contents of the editor url
    URL editorURL = pageletEditor.getEditorURL();
    if (editorURL != null) {
      String rendererContent = null;
      try {
        rendererContent = loadContents(editorURL, site, page, composer, pagelet, environment, language);
        pageletEditor.setEditor(rendererContent);
      } catch (ServletException e) {
        logger.warn("Error processing the pagelet renderer at {}: {}", editorURL, e.getMessage());
      }
    }

    return pageletEditor;
  }

  /**
   * Returns the page or <code>null</code> if the page is not found.
   * 
   * @param site
   *          the site
   * @param pageURI
   *          the pareURI
   * @return the page
   */
  private Page getPage(Site site, ResourceURI pageURI) {
    // Get hold of the site's content repository
    ContentRepository contentRepository = site.getContentRepository();
    if (contentRepository == null) {
      logger.warn("No content repository found for site '{}'", site);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Load the page
    Page page = null;
    try {
      page = (Page) contentRepository.get(pageURI);
    } catch (ContentRepositoryException e) {
      logger.error("Error trying to access content repository {}: {}", contentRepository, e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    return page;
  }

  /**
   * Returns the pagelet renderer or <code>null</code> if either one of the
   * page, the composer or the is not available.
   * 
   * @param site
   *          the site
   * @param pageURI
   *          the page uri
   * @param composerId
   *          the composer id
   * @param pageletIndex
   *          the pagelet index
   * @param language
   *          the language
   * @param environment
   *          the environment
   * @return the pagelet renderer
   * @throws IOException
   *           if reading the pagelet fails
   */
  public String getRenderer(Site site, ResourceURI pageURI, String composerId,
      int pageletIndex, String language, Environment environment)
          throws IOException {

    Page page = getPage(site, pageURI);
    if (page == null) {
      logger.warn("Client requested pagelet renderer for non existing page {}", pageURI);
      return null;
    }

    // Load the composer
    Composer composer = page.getComposer(composerId);
    if (composer == null) {
      logger.warn("Client requested pagelet renderer for non existing composer {} on page {}", composerId, pageURI);
      return null;
    }

    // Get the pagelet
    if (composer.getPagelets().length <= pageletIndex || composer.size() <= pageletIndex) {
      logger.warn("Client requested pagelet renderer for non existing pagelet on page {}", pageURI);
      return null;
    }

    Pagelet pagelet = composer.getPagelet(pageletIndex);
    Module module = site.getModule(pagelet.getModule());
    if (module == null) {
      logger.warn("Client requested pagelet renderer for non existing module {}", pagelet.getModule());
      return null;
    }

    PageletRenderer renderer = module.getRenderer(pagelet.getIdentifier());
    if (renderer == null) {
      logger.warn("Client requested pagelet renderer for non existing renderer on pagelet {}", pagelet.getIdentifier());
      return null;
    }

    // Load the contents of the renderer url
    renderer.setEnvironment(environment);
    URL rendererURL = renderer.getRenderer();
    String rendererContent = null;
    if (rendererURL != null) {
      try {
        rendererContent = loadContents(rendererURL, site, page, composer, pagelet, environment, language);
      } catch (ServletException e) {
        logger.warn("Error processing the pagelet renderer at {}: {}", rendererURL, e.getMessage());
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
    }

    return rendererContent;
  }

  public String getRenderer(Site site, ResourceURI pageURI, String composerId,
      int pageletIndex, String pageXml, String language, Environment environment)
          throws IOException, ParserConfigurationException, SAXException {

    InputStream is = null;
    Page page = null;
    try {
      PageReader pageReader = new PageReader();
      is = IOUtils.toInputStream(pageXml, "utf-8");
      page = pageReader.read(is, site);
    } finally {
      IOUtils.closeQuietly(is);
    }

    // Load the composer
    Composer composer = page.getComposer(composerId);
    if (composer == null) {
      logger.warn("Client requested pagelet renderer for non existing composer {} on page {}", composerId, pageURI);
      return null;
    }

    // Get the pagelet
    if (composer.getPagelets().length <= pageletIndex || composer.size() <= pageletIndex) {
      logger.warn("Client requested pagelet renderer for non existing pagelet on page {}", pageURI);
      return null;
    }

    Pagelet pagelet = composer.getPagelet(pageletIndex);
    Module module = site.getModule(pagelet.getModule());
    if (module == null) {
      logger.warn("Client requested pagelet renderer for non existing module {}", pagelet.getModule());
      return null;
    }

    PageletRenderer renderer = module.getRenderer(pagelet.getIdentifier());
    if (renderer == null) {
      logger.warn("Client requested pagelet renderer for non existing renderer on pagelet {}", pagelet.getIdentifier());
      return null;
    }

    // Load the contents of the renderer url
    renderer.setEnvironment(environment);
    URL rendererURL = renderer.getRenderer();
    String rendererContent = null;
    if (rendererURL != null) {
      try {
        rendererContent = loadContents(rendererURL, site, page, composer, pagelet, environment, language);
      } catch (ServletException e) {
        logger.warn("Error processing the pagelet renderer at {}: {}", rendererURL, e.getMessage());
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
    }

    return rendererContent;
  }

  /**
   * Asks the site servlet to render the given url using the page, composer and
   * pagelet as the rendering environment. If the no servlet is available for
   * the given site, the contents are loaded from the url directly.
   * 
   * @param rendererURL
   *          the renderer url
   * @param site
   *          the site
   * @param page
   *          the page
   * @param composer
   *          the composer
   * @param pagelet
   *          the pagelet
   * @param environment
   *          the environment
   * @param language
   *          the language
   * @return the servlet response, serialized to a string
   * @throws IOException
   *           if the servlet fails to create the response
   * @throws ServletException
   *           if an exception occurs while processing
   */
  private String loadContents(URL rendererURL, Site site, Page page,
      Composer composer, Pagelet pagelet, Environment environment,
      String language) throws IOException, ServletException {

    Servlet servlet = siteServlets.get(site.getIdentifier());

    String httpContextURI = UrlUtils.concat("/weblounge-sites", site.getIdentifier());
    int httpContextURILength = httpContextURI.length();
    String url = rendererURL.toExternalForm();
    int uriInPath = url.indexOf(httpContextURI);

    if (uriInPath > 0) {
      String pathInfo = url.substring(uriInPath + httpContextURILength);

      // Prepare the mock request
      MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
      request.setServerName(site.getHostname(environment).getURL().getHost());
      request.setServerPort(site.getHostname(environment).getURL().getPort());
      request.setMethod(site.getHostname(environment).getURL().getProtocol());
      if (language != null)
        request.addPreferredLocale(new Locale(language));
      request.setAttribute(WebloungeRequest.PAGE, page);
      request.setAttribute(WebloungeRequest.COMPOSER, composer);
      request.setAttribute(WebloungeRequest.PAGELET, pagelet);
      request.setPathInfo(pathInfo);
      request.setRequestURI(UrlUtils.concat(httpContextURI, pathInfo));

      MockHttpServletResponse response = new MockHttpServletResponse();
      servlet.service(request, response);
      return response.getContentAsString();
    } else {
      InputStream is = null;
      try {
        is = rendererURL.openStream();
        return IOUtils.toString(is, "utf-8");
      } finally {
        IOUtils.closeQuietly(is);
      }
    }
  }

  /**
   * Adds the site servlet to the list of servlets.
   * 
   * @param id
   *          the site identifier
   * @param servlet
   *          the site servlet
   */
  void addSiteServlet(String id, Servlet servlet) {
    logger.debug("Site servlet attached to {} workbench", id);
    siteServlets.put(id, servlet);
  }

  /**
   * Removes the site servlet from the list of servlets
   * 
   * @param site
   *          the site identifier
   */
  void removeSiteServlet(String id) {
    logger.debug("Site servlet detached from {} workbench", id);
    siteServlets.remove(id);
  }

  /**
   * Implementation of a <code>ServiceTracker</code> that is tracking instances
   * of type {@link Servlet} with an associated <code>site</code> attribute.
   */
  private class SiteServletTracker extends ServiceTracker {

    /**
     * Creates a new servlet tracker that is using the given bundle context to
     * look up service instances.
     * 
     * @param ctx
     *          the bundle context
     * @param filter
     *          the service filter
     */
    SiteServletTracker(BundleContext ctx, Filter filter) {
      super(ctx, filter, null);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Object addingService(ServiceReference reference) {
      Servlet servlet = (Servlet) super.addingService(reference);
      String site = (String) reference.getProperty(Site.class.getName().toLowerCase());
      addSiteServlet(site, servlet);
      return servlet;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService(ServiceReference reference, Object service) {
      String site = (String) reference.getProperty("site");
      removeSiteServlet(site);
    }

  }

}
