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

package ch.entwine.weblounge.preview.xhtmlrenderer;

import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.image.ImagePreviewGenerator;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.impl.testing.MockHttpServletRequest;
import ch.entwine.weblounge.common.impl.testing.MockHttpServletResponse;
import ch.entwine.weblounge.common.impl.util.html.HTMLUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.Serializer;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;
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
import org.xhtmlrenderer.util.XRRuntimeException;

import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * A <code>PreviewGenerator</code> that will generate previews for pages.
 */
public class XhtmlRendererPagePreviewGenerator implements PreviewGenerator {

  /** Logger factory */
  private static final Logger logger = LoggerFactory.getLogger(XhtmlRendererPagePreviewGenerator.class);

  /** Page request handler path prefix */
  protected static final String PAGE_HANDLER_PREFIX = "/weblounge-pages/";

  /** Format for the preview images */
  private static final String PREVIEW_FORMAT = "png";

  /** Format for the preview images */
  private static final String PREVIEW_CONTENT_TYPE = "image/png";

  /** Default width for taking screenshots */
  private static final int DEFAULT_SCREENSHOT_WIDTH = 1024;

  /** Default height for taking screenshots */
  private static final int DEFAULT_SCREENSHOT_HEIGHT = 768;

  /** The site servlets */
  private static Map<String, Servlet> siteServlets = new HashMap<String, Servlet>();

  /** The preview generators */
  private List<ImagePreviewGenerator> previewGenerators = new ArrayList<ImagePreviewGenerator>();

  /** The user agents per site */
  private static Map<String, WebloungeUserAgent> userAgents = new HashMap<String, WebloungeUserAgent>();

  /** Warning flags */
  private boolean isRenderingEnvironmentSane = true;

  /** The site servlet service tracker */
  private ServiceTracker siteServletTracker = null;

  /** The preview generator service tracker */
  private ServiceTracker previewGeneratorTracker = null;

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
      previewGeneratorTracker = new ImagePreviewGeneratorTracker(ctx.getBundleContext());
      previewGeneratorTracker.open();
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
    if (previewGeneratorTracker != null) {
      previewGeneratorTracker.close();
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.PreviewGenerator#supports(ch.entwine.weblounge.common.content.Resource)
   */
  public boolean supports(Resource<?> resource) {
    return (resource instanceof Page);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.PreviewGenerator#getPriority()
   */
  public int getPriority() {
    return 0;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.PreviewGenerator#createPreview(ch.entwine.weblounge.common.content.Resource,
   *      ch.entwine.weblounge.common.site.Environment,
   *      ch.entwine.weblounge.common.language.Language,
   *      ch.entwine.weblounge.common.content.image.ImageStyle,
   *      String, java.io.InputStream, java.io.OutputStream)
   */
  public void createPreview(Resource<?> resource, Environment environment,
      Language language, ImageStyle style, String format, InputStream is, OutputStream os)
      throws IOException {

    if (!isRenderingEnvironmentSane) {
      logger.debug("Skipping page preview rendering as environment is not sane");
      return;
    }

    ImagePreviewGenerator imagePreviewGenerator = null;
    synchronized (previewGenerators) {
      if (previewGenerators.size() == 0) {
        logger.debug("Unable to generate page previews since no image renderer is available");
        return;
      }
      imagePreviewGenerator = previewGenerators.get(0);
    }

    ResourceURI uri = resource.getURI();
    Site site = uri.getSite();
    String html = null;
    try {
      URL pageURL = new URL(UrlUtils.concat(site.getHostname(environment).toExternalForm(), PAGE_HANDLER_PREFIX, uri.getIdentifier()));
      html = render(pageURL, site, environment, language, resource.getVersion());
      if (StringUtils.isBlank(html)) {
        logger.warn("Error rendering preview of page " + uri.getPath());
        return;
      }
      html = HTMLUtils.escapeHtml(HTMLUtils.unescape(html));
    } catch (ServletException e) {
      logger.warn("Error rendering page " + uri.getPath(), e);
      throw new IOException(e);
    }

    // Try to convert html to xhtml
    HtmlCleaner cleaner = new HtmlCleaner();
    CleanerProperties xhtmlProperties = cleaner.getProperties();
    TagNode xhtmlNode = cleaner.clean(html);
    if (xhtmlNode == null) {
      logger.warn("Error creating well-formed document from page {}", resource);
      return;
    }

    File xhtmlFile = null;
    is = new ByteArrayInputStream(html.getBytes("UTF-8"));

    // Write the resource content to disk. This step is needed, as the preview
    // generator can only handle files.
    try {
      xhtmlFile = File.createTempFile("xhtml", ".xml");
      Serializer xhtmlSerializer = new SimpleXmlSerializer(xhtmlProperties);
      xhtmlSerializer.writeToFile(xhtmlNode, xhtmlFile.getAbsolutePath(), "UTF-8");
    } catch (IOException e) {
      logger.error("Error creating temporary copy of file content at " + xhtmlFile, e);
      FileUtils.deleteQuietly(xhtmlFile);
      throw e;
    } finally {
      IOUtils.closeQuietly(is);
    }

    File imageFile = File.createTempFile("xhtml-preview", "." + PREVIEW_FORMAT);
    FileOutputStream imageFos = null;

    // Render the page and write back to client
    try {
      int screenshotWidth = DEFAULT_SCREENSHOT_WIDTH;
      int screenshotHeight = DEFAULT_SCREENSHOT_HEIGHT;
      if (style.getWidth() > 0 && style.getHeight() > 0) {
        screenshotHeight = (int) ((float) screenshotWidth / (float) style.getWidth() * style.getHeight());
      }

      // Create the renderer. Due to a synchronization bug in the software,
      // this needs to be synchronized
      Java2DRenderer renderer = null;
      try {
        synchronized (this) {
          renderer = new Java2DRenderer(xhtmlFile, screenshotWidth, screenshotHeight);
        }
      } catch (Throwable t) {
        if (isRenderingEnvironmentSane) {
          logger.warn("Error creating Java 2D renderer for previews: {}" + t.getMessage());
          logger.warn("Page preview rendering will be switched off");
          isRenderingEnvironmentSane = false;
        }
        logger.debug("Error creating Java 2D renderer for preview of page {}: {}" + uri.getPath(), t.getMessage());
        return;
      }

      // Configure the renderer
      renderer.getSharedContext().setBaseURL(site.getHostname().toExternalForm());
      renderer.getSharedContext().setInteractive(false);

      // Make sure the renderer is using a user agent that will correctly
      // resolve urls
      WebloungeUserAgent agent = userAgents.get(site.getIdentifier());
      if (agent == null) {
        agent = new WebloungeUserAgent(site.getHostname().getURL());
        userAgents.put(site.getIdentifier(), agent);
      }
      renderer.getSharedContext().setUserAgentCallback(agent);

      // Render the page to an image
      BufferedImage img = renderer.getImage();
      FSImageWriter imageWriter = new FSImageWriter(PREVIEW_FORMAT);
      imageFos = new FileOutputStream(imageFile);
      imageWriter.write(img, imageFos);

    } catch (IOException e) {
      logger.error("Error creating temporary copy of file content at " + xhtmlFile, e);
      throw e;
    } catch (XRRuntimeException e) {
      logger.warn("Error rendering page content at " + uri + ": " + e.getMessage());
      throw e;
    } catch (HeadlessException e) {
      logger.warn("Headless error while trying to render page preview: " + e.getMessage());
      logger.warn("Page preview rendering will be switched off");
      isRenderingEnvironmentSane = false;
      throw e;
    } catch (Throwable t) {
      logger.warn("Error rendering page content at " + uri + ": " + t.getMessage(), t);
      throw new IOException(t);
    } finally {
      IOUtils.closeQuietly(imageFos);
      FileUtils.deleteQuietly(xhtmlFile);
    }

    FileInputStream imageIs = null;

    // Scale the image to the correct size
    try {
      imageIs = new FileInputStream(imageFile);
      imagePreviewGenerator.createPreview(resource, environment, language, style, null, imageIs, os);
    } catch (IOException e) {
      logger.error("Error creating temporary copy of file content at " + xhtmlFile, e);
      throw e;
    } catch (Throwable t) {
      logger.warn("Error scaling page preview at " + uri + ": " + t.getMessage(), t);
      throw new IOException(t);
    } finally {
      IOUtils.closeQuietly(imageIs);
      FileUtils.deleteQuietly(imageFile);
    }

  }

  /**
   * Renders the page located at <code>rendererURL</code> in the given language.
   * 
   * @param rendererURL
   *          the page url
   * @param site
   *          the site
   * @param environment
   *          the environment
   * @param language
   *          the language
   * @param version
   *          the version
   * @return the rendered <code>HTML</code>
   * @throws ServletException
   *           if rendering fails
   * @throws IOException
   *           if reading from the servlet fails
   */
  private String render(URL rendererURL, Site site, Environment environment,
      Language language, long version) throws ServletException, IOException {
    Servlet servlet = siteServlets.get(site.getIdentifier());

    String httpContextURI = UrlUtils.concat("/weblounge-sites", site.getIdentifier());
    int httpContextURILength = httpContextURI.length();
    String url = rendererURL.toExternalForm();
    int uriInPath = url.indexOf(httpContextURI);

    // Are we trying to render a site resource (e. g. a jsp during
    // precompilation)?
    if (uriInPath > 0) {
      String pathInfo = url.substring(uriInPath + httpContextURILength);

      // Prepare the mock request
      MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
      request.setServerName(site.getHostname(environment).getURL().getHost());
      request.setServerPort(site.getHostname(environment).getURL().getPort());
      request.setMethod(site.getHostname(environment).getURL().getProtocol());
      request.setAttribute(WebloungeRequest.LANGUAGE, language);
      request.setPathInfo(pathInfo);
      request.setRequestURI(UrlUtils.concat(httpContextURI, pathInfo));

      MockHttpServletResponse response = new MockHttpServletResponse();
      servlet.service(request, response);
      return response.getContentAsString();
    } else {
      HttpClient httpClient = new DefaultHttpClient();
      httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
      try {
        if (version == Resource.WORK) {
          rendererURL = new URL(UrlUtils.concat(rendererURL.toExternalForm(), "work_" + language.getIdentifier() + ".html"));
        } else {
          rendererURL = new URL(UrlUtils.concat(rendererURL.toExternalForm(), "index_" + language.getIdentifier() + ".html"));
        }
        HttpGet getRequest = new HttpGet(rendererURL.toExternalForm());
        getRequest.addHeader(new BasicHeader("X-Weblounge-Special", "Page-Preview"));
        HttpResponse response = httpClient.execute(getRequest);
        if (response.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK)
          return null;
        String responseText = EntityUtils.toString(response.getEntity(), "utf-8");
        return responseText;
      } finally {
        httpClient.getConnectionManager().shutdown();
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
   * Adds the preview generator to the list of registered preview generators.
   * 
   * @param generator
   *          the generator
   */
  void addPreviewGenerator(ImagePreviewGenerator generator) {
    synchronized (previewGenerators) {
      previewGenerators.add(generator);
      Collections.sort(previewGenerators, new Comparator<PreviewGenerator>() {
        public int compare(PreviewGenerator a, PreviewGenerator b) {
          return Integer.valueOf(a.getPriority()).compareTo(b.getPriority());
        }
      });
    }
  }

  /**
   * Removes the preview generator from the list of registered preview
   * generators.
   * 
   * @param generator
   *          the generator
   */
  void removePreviewGenerator(ImagePreviewGenerator generator) {
    synchronized (previewGenerators) {
      previewGenerators.remove(generator);
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
   * Implementation of a <code>ServiceTracker</code> that is tracking instances
   * of type {@link ImagePreviewGenerator} with an associated <code>site</code>
   * attribute.
   */
  private class ImagePreviewGeneratorTracker extends ServiceTracker {

    /**
     * Creates a new service tracker that is using the given bundle context to
     * look up service instances.
     * 
     * @param ctx
     *          the bundle context
     */
    ImagePreviewGeneratorTracker(BundleContext ctx) {
      super(ctx, ImagePreviewGenerator.class.getName(), null);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Object addingService(ServiceReference reference) {
      ImagePreviewGenerator previewGenerator = (ImagePreviewGenerator) super.addingService(reference);
      addPreviewGenerator(previewGenerator);
      return previewGenerator;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService(ServiceReference reference, Object service) {
      removePreviewGenerator((ImagePreviewGenerator) service);
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
