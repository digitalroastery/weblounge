/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
 *  http://weblounge.o2it.ch
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

package ch.o2it.weblounge.workbench;

import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.content.page.Composer;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.content.repository.ContentRepository;
import ch.o2it.weblounge.common.content.repository.ContentRepositoryException;
import ch.o2it.weblounge.common.impl.content.SearchQueryImpl;
import ch.o2it.weblounge.common.impl.testing.MockHttpServletRequest;
import ch.o2it.weblounge.common.impl.testing.MockHttpServletResponse;
import ch.o2it.weblounge.common.impl.url.UrlUtils;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.workbench.suggest.PageSuggestion;
import ch.o2it.weblounge.workbench.suggest.SubjectSuggestion;
import ch.o2it.weblounge.workbench.suggest.UserSuggestion;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

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
   * Returns a list of users From the given site that are suggested based on
   * what is passed in as <code>text</code>. If <code>limit</code> is
   * 
   * @param site
   *          the site
   * @param text
   *          the starting test
   * @param limit
   *          the maximum number of users to return
   * @return the list of suggested users
   * @throws IllegalStateException
   *           if the content repository is not available
   * @throws ContentRepositoryException
   *           if querying fails
   */
  public List<UserSuggestion> suggestUsers(Site site, String text, int limit)
      throws IllegalStateException {
    List<UserSuggestion> users = new ArrayList<UserSuggestion>();
    SearchQuery search = new SearchQueryImpl(site);
    // search.withUser(text + "*");
    // search.withUserFacet();

    // Get hold of the site's content repository
    ContentRepository contentRepository = site.getContentRepository();
    if (contentRepository == null) {
      logger.warn("No content repository found for site '{}'", site);
      return null;
    }

    // TODO: implement search

    return users;
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
  public List<SubjectSuggestion> suggestTags(Site site, String text, int limit)
      throws IllegalStateException {
    List<SubjectSuggestion> tags = new ArrayList<SubjectSuggestion>();
    SearchQuery search = new SearchQueryImpl(site);
    search.withSubject(text + "*");
    search.withSubjectFacet();

    // Get hold of the site's content repository
    ContentRepository contentRepository = site.getContentRepository();
    if (contentRepository == null) {
      throw new IllegalStateException("No content repository found for site '" + site + "'");
    }
    
    return tags;
  }

  /**
   * Returns a list of pages from the given site that are suggested based on
   * what is passed in as <code>text</code>. If <code>limit</code> is
   * 
   * @param site
   *          the site
   * @param text
   *          the starting test
   * @param limit
   *          the maximum number of pages to return
   * @return the list of suggested pages
   * @throws IllegalStateException
   *           if the content repository is not available
   * @throws ContentRepositoryException
   *           if querying fails
   */
  public List<PageSuggestion> suggestPages(Site site, String text, int limit)
      throws IllegalStateException {
    List<PageSuggestion> pages = new ArrayList<PageSuggestion>();
    SearchQuery search = new SearchQueryImpl(site);
    // search.withPage(text + "*");
    // search.withPageFacet();
    // TODO: implement search
    return pages;
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
   * @return the pagelet editor
   * @throws IOException
   *           if reading the pagelet fails
   */
  public PageletEditor getEditor(Site site, ResourceURI pageURI,
      String composerId, int pageletIndex) throws IOException {
    if (site == null)
      throw new IllegalArgumentException("Site must not be null");
    if (composerId == null)
      throw new IllegalArgumentException("Composer must not be null");
    if (pageletIndex < 0)
      throw new IllegalArgumentException("Pagelet index must be a positive integer");

    // Get hold of the site's content repository
    ContentRepository contentRepository = site.getContentRepository();
    if (contentRepository == null) {
      logger.warn("No content repository found for site '{}'", site);
      return null;
    }

    // Load the page
    Page page = null;

    try {
      page = (Page) contentRepository.get(pageURI);
      if (page == null) {
        logger.warn("Client requested pagelet editor for non existing page {}", pageURI);
        return null;
      }
    } catch (ContentRepositoryException e) {
      logger.error("Error trying to access content repository {}: {}", contentRepository, e);
      return null;
    }

    // Load the composer
    Composer composer = page.getComposer(composerId);
    if (composer == null) {
      logger.warn("Client requested pagelet editor for non existing composer {} on page {}", composerId, pageURI);
      return null;
    }

    // Get the pagelet
    if (composer.getPagelets().length < pageletIndex || composer.size() < pageletIndex) {
      logger.warn("Client requested pagelet editor for non existing pagelet on page {}", pageURI);
      return null;
    }

    Pagelet pagelet = composer.getPagelet(pageletIndex);
    pagelet = new TrimpathPageletWrapper(pagelet);
    PageletEditor pageletEditor = new PageletEditor(pagelet, pageURI, composerId, pageletIndex);

    // Load the contents of the renderer url
    URL rendererURL = pageletEditor.getRenderer();
    if (rendererURL != null) {
      String rendererContent = null;
      try {
        rendererContent = loadContents(rendererURL, site, page, composer, pagelet);
        pageletEditor.setRenderer(rendererContent);
      } catch (ServletException e) {
        logger.warn("Error processing the pagelet renderer at {}: {}", rendererURL, e.getMessage());
      }
    }

    // Load the contents of the editor url
    URL editorURL = pageletEditor.getEditorURL();
    if (editorURL != null) {
      String rendererContent = null;
      try {
        rendererContent = loadContents(editorURL, site, page, composer, pagelet);
        pageletEditor.setEditor(rendererContent);
      } catch (ServletException e) {
        logger.warn("Error processing the pagelet renderer at {}: {}", editorURL, e.getMessage());
      }
    }

    return pageletEditor;
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
   * @return the servlet response, serialized to a string
   * @throws IOException
   *           if the servlet fails to create the response
   * @throws ServletException
   *           if an exception occurs while processing
   */
  private String loadContents(URL rendererURL, Site site, Page page,
      Composer composer, Pagelet pagelet) throws IOException, ServletException {

    Servlet servlet = siteServlets.get(site.getIdentifier());

    String httpContextURI = UrlUtils.concat("/weblounge-sites", site.getIdentifier());
    int httpContextURILength = httpContextURI.length();
    String url = rendererURL.toExternalForm();
    int uriInPath = url.indexOf(httpContextURI);

    if (uriInPath > 0) {
      String pathInfo = url.substring(uriInPath + httpContextURILength);

      // Prepare the mock request
      MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
      request.setLocalAddr(site.getURL().toExternalForm());
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
