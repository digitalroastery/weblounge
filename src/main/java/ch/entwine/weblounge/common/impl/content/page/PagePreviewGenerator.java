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

package ch.entwine.weblounge.common.impl.content.page;

import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.entwine.weblounge.common.impl.testing.MockHttpServletRequest;
import ch.entwine.weblounge.common.impl.testing.MockHttpServletResponse;
import ch.entwine.weblounge.common.impl.url.UrlUtils;
import ch.entwine.weblounge.common.impl.util.html.HTMLUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.swing.NaiveUserAgent;
import org.xhtmlrenderer.util.FSImageWriter;
import org.xhtmlrenderer.util.XRLog;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

/**
 * A <code>PreviewGenerator</code> that will generate previews for pages.
 */
public class PagePreviewGenerator implements PreviewGenerator {

  /** Logger factory */
  private static final Logger logger = LoggerFactory.getLogger(PagePreviewGenerator.class);

  /** Format for the preview images */
  private static final String PREVIEW_FORMAT = "png";

  /** Format for the preview images */
  private static final String PREVIEW_CONTENT_TYPE = "image/png";

  /** Default width for taking screenshots */
  private static final int DEFAULT_SCREENSHOT_WIDTH = 1024;

  /** The site servlets */
  private static Map<String, Servlet> siteServlets = new HashMap<String, Servlet>();

  /** The user agents per site */
  private static Map<String, WebloungeUserAgent> userAgents = new HashMap<String, WebloungeUserAgent>();

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
  public void activate(ComponentContext ctx) {
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
  public void deactivate() {
    if (siteServletTracker != null) {
      siteServletTracker.close();
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.PreviewGenerator#createPreview(ch.entwine.weblounge.common.content.Resource,
   *      ch.entwine.weblounge.common.language.Language,
   *      ch.entwine.weblounge.common.content.image.ImageStyle,
   *      java.io.InputStream, java.io.OutputStream)
   */
  public void createPreview(Resource<?> resource, Language language,
      ImageStyle style, InputStream is, OutputStream os) throws IOException {

    ResourceURI uri = resource.getURI();
    Site site = uri.getSite();
    String path = uri.getPath();
    String html = null;
    try {
      URL pageURL = new URL(UrlUtils.concat(site.getURL().toExternalForm(), path));
      html = render(pageURL, site, language);
      html = HTMLUtils.escapeHtml(HTMLUtils.unescape(html));
    } catch (ServletException e) {
      logger.warn("Error rendering page " + uri.getPath(), e);
      throw new IOException(e);
    }

    File f = null;
    FileOutputStream fos = null;
    is = new ByteArrayInputStream(html.getBytes("UTF-8"));

    // Write the resource content to disk. This step is needed, as the preview
    // generator can only handle files.
    try {
      f = File.createTempFile("preview", null);
      fos = new FileOutputStream(f);
      IOUtils.copy(is, fos);
    } catch (IOException e) {
      logger.error("Error creating temporary copy of file content at " + f, e);
      IOUtils.closeQuietly(fos);
      FileUtils.deleteQuietly(f);
      throw e;
    } finally {
      IOUtils.closeQuietly(fos);
    }

    // Render the page and write back to client
    try {
      int screenshotWidth = DEFAULT_SCREENSHOT_WIDTH;
      float screenshotScale = screenshotWidth / style.getWidth();
      int screenshotHeight = (int) screenshotScale * style.getHeight();

      Java2DRenderer renderer = new Java2DRenderer(f, screenshotWidth, screenshotHeight);
      renderer.getSharedContext().setBaseURL(site.getURL().toExternalForm());
      renderer.getSharedContext().setInteractive(false);

      // Make sure the renderer is using a user agent that will correctly
      // resolve urls
      WebloungeUserAgent agent = userAgents.get(site.getIdentifier());
      if (agent == null) {
        agent = new WebloungeUserAgent(site.getURL());
        userAgents.put(site.getIdentifier(), agent);
      }
      renderer.getSharedContext().setUserAgentCallback(agent);

      // Render the page to an image
      BufferedImage img = renderer.getImage();
      FSImageWriter imageWriter = new FSImageWriter(PREVIEW_FORMAT);
      ByteArrayOutputStream tos = new ByteArrayOutputStream(screenshotWidth * screenshotHeight);
      imageWriter.write(img, tos);

      // Scale the image to the correct size
      ImageStyleUtils.style(new ByteArrayInputStream(tos.toByteArray()), os, html, style);
    } catch (IOException e) {
      logger.error("Error creating temporary copy of file content at " + f, e);
      throw e;
    } catch (Throwable t) {
      logger.warn("Error rendering page content at " + uri + ": " + t.getMessage(), t);
      throw new IOException(t);
    } finally {
      FileUtils.deleteQuietly(f);
    }

  }

  /**
   * Renders the page located at <code>rendererURL</code> in the given language.
   * 
   * @param rendererURL
   *          the page url
   * @param site
   *          the site
   * @param language
   *          the language
   * @return the rendered <code>HTML</code>
   * @throws ServletException
   *           if rendering fails
   * @throws IOException
   *           if reading from the servlet fails
   */
  private String render(URL rendererURL, Site site, Language language)
      throws ServletException, IOException {
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
      request.setAttribute(WebloungeRequest.LANGUAGE, language);
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
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.PreviewGenerator#getContentType(ch.entwine.weblounge.common.content.Resource,
   *      ch.entwine.weblounge.common.language.Language,
   *      ch.entwine.weblounge.common.content.image.ImageStyle)
   */
  public String getContentType(Resource<?> resource, Language language,
      ImageStyle style) {
    return PREVIEW_CONTENT_TYPE;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.PreviewGenerator#getSuffix(ch.entwine.weblounge.common.content.Resource,
   *      ch.entwine.weblounge.common.language.Language,
   *      ch.entwine.weblounge.common.content.image.ImageStyle)
   */
  public String getSuffix(Resource<?> resource, Language language,
      ImageStyle style) {
    return PREVIEW_FORMAT;
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
    userAgents.remove(id);
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

  /**
   * This class provides a bug fix to the {@link NaiveUserAgent} class from the
   * xhtml renderer.
   */
  static class WebloungeUserAgent extends NaiveUserAgent {

    /** The base URL */
    private String baseURL = null;

    /**
     * Creates a user agent that will use <code>baseURL</code> to resolve uris
     * without a protocol (paths, that is).
     * 
     * @param baseURL
     *          the base url
     */
    WebloungeUserAgent(URL baseURL) {
      this.baseURL = baseURL.toExternalForm();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xhtmlrenderer.swing.NaiveUserAgent#getBaseURL()
     */
    @Override
    public String getBaseURL() {
      return baseURL;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xhtmlrenderer.swing.NaiveUserAgent#resolveURI(java.lang.String)
     */
    @Override
    public String resolveURI(String uri) {
      if (uri == null)
        return null;
      try {
        URL result = new URL(uri);
        return result.toExternalForm();
      } catch (MalformedURLException e1) {
        try {
          URL result = new URL(UrlUtils.concat(baseURL, uri));
          return result.toString();
        } catch (MalformedURLException e2) {
          XRLog.exception("The default NaiveUserAgent cannot resolve the URL " + uri + " with base URL " + getBaseURL());
          return null;
        }
      }
    }

  }

}
