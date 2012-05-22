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

package ch.entwine.weblounge.kernel.site;

import ch.entwine.weblounge.common.content.Renderer.RendererType;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.content.page.PageletRenderer;
import ch.entwine.weblounge.common.content.page.PageletURI;
import ch.entwine.weblounge.common.impl.content.page.MockPageImpl;
import ch.entwine.weblounge.common.impl.content.page.PageletImpl;
import ch.entwine.weblounge.common.impl.content.page.PageletURIImpl;
import ch.entwine.weblounge.common.impl.security.Guest;
import ch.entwine.weblounge.common.impl.testing.MockHttpServletRequest;
import ch.entwine.weblounge.common.impl.testing.MockHttpServletResponse;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.security.SecurityService;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This precompiler searches an OSGi bundle for Java Server Pages (JSP) and
 * sends a request to <code>JspC</code>, the java server page compiler provided
 * by Jasper in order to get the compilation work done before a user request
 * hits the jsp.
 */
public class Precompiler {

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(SiteDispatcherServiceImpl.class);

  /** The site servlet */
  protected SiteServlet servlet = null;

  /** The worker */
  protected PrecompileWorker worker = null;

  /** Flag to indicate whether to keep working or not */
  protected boolean keepGoing = true;

  /** Running flag */
  protected boolean isRunning = true;

  /** Switch for precompiler error logging */
  protected boolean logErrors = true;

  /** The default environment */
  protected Environment environment = null;

  /** The security service */
  protected SecurityService security = null;

  /** The key used to identify this compilation process */
  protected String compilerKey = null;

  /**
   * Creates a new precompiler for the site identified by the servlet.
   * 
   * @param key
   *          the compiler key
   * @param servlet
   *          the site servlet
   * @param environment
   *          the environment to use
   * @param security
   *          the security service
   * @param logErrors
   *          <code>true</code> to log precompilation errors
   */
  public Precompiler(String key, SiteServlet servlet, Environment environment,
      SecurityService security, boolean logErrors) {
    this.compilerKey = key;
    this.servlet = servlet;
    this.environment = environment;
    this.security = security;
    this.logErrors = logErrors;
  }

  /**
   * Precompiles all of the bundle's server pages intoto the output directory as
   * specified in the <code>scratchDir</code> setting of the compiler
   * configuration.
   * 
   * @param outputDir
   *          the path to the output directory
   */
  public void precompile() {
    worker = new PrecompileWorker(servlet);
    Thread workerThread = new Thread(worker);
    workerThread.setPriority(Thread.MIN_PRIORITY);
    workerThread.setDaemon(true);
    workerThread.start();
  }

  /**
   * Returns <code>true</code> if the precompiler is still running.
   * 
   * @return <code>true</code> if the compiler is still running
   */
  public boolean isRunning() {
    return isRunning;
  }

  /**
   * Returns the key that is used to identify this compilation process.
   * 
   * @return the compiler key
   */
  public String getCompilerKey() {
    return compilerKey;
  }

  /**
   * Stops the current precompilation work.
   */
  public void stop() {
    logger.debug("Asking precompiler for '{}' to stop", servlet.getSite());
    keepGoing = false;
  }

  class PrecompileWorker implements Runnable {

    /** The servlet to use */
    private SiteServlet servlet = null;

    /**
     * Creates a new jsp precompilation worker.
     * 
     * @param servlet
     *          the servlet
     */
    public PrecompileWorker(SiteServlet servlet) {
      this.servlet = servlet;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {

      Site site = servlet.getSite();

      // Prepare the mock request and response objects
      MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
      request.setServerName(site.getHostname(environment).getURL().getHost());
      request.setServerPort(site.getHostname(environment).getURL().getPort());
      request.setMethod(site.getHostname(environment).getURL().getProtocol());
      request.setServletPath("");
      MockHttpServletResponse response = new MockHttpServletResponse();

      // Prepare a fake page in order to prevent erratic behavior during
      // precompilation
      Page page = new MockPageImpl(site);
      Pagelet pagelet = null;
      for (Module m : site.getModules()) {
        if (m.getRenderers().length > 0) {
          PageletRenderer r = m.getRenderers()[0];
          PageletURI pageletURI = new PageletURIImpl(page.getURI(), PageTemplate.DEFAULT_STAGE, 0);
          pagelet = new PageletImpl(pageletURI, m.getIdentifier(), r.getIdentifier());
        }
      }

      // Collect all renderers (from modules and templates) and ask for
      // precompilation
      List<URL> rendererUrls = new ArrayList<URL>();
      for (Module m : site.getModules()) {
        if (!m.isEnabled())
          break;
        for (PageletRenderer p : m.getRenderers()) {
          if (p.getRenderer() != null)
            rendererUrls.add(p.getRenderer());
          if (p.getRenderer(RendererType.Feed.name()) != null)
            rendererUrls.add(p.getRenderer(RendererType.Feed.name()));
          if (p.getRenderer(RendererType.Search.name()) != null)
            rendererUrls.add(p.getRenderer(RendererType.Search.name()));
          if (p.getEditor() != null)
            rendererUrls.add(p.getEditor());
        }
      }
      for (PageTemplate t : site.getTemplates()) {
        if (t.getRenderer() != null)
          rendererUrls.add(t.getRenderer());
      }

      if (rendererUrls.size() < 1) {
        logger.debug("No java server pages found to precompile for {}", site);
        return;
      }

      // Make sure there is a user
      security.setUser(new Guest(site.getIdentifier()));
      security.setSite(site);

      logger.info("Precompiling java server pages for '{}'", site);
      int errorCount = 0;
      Iterator<URL> rendererIterator = rendererUrls.iterator();
      while (keepGoing && rendererIterator.hasNext()) {
        URL entry = rendererIterator.next();
        String path = entry.getPath();
        String pathInfo = path.substring(path.indexOf(site.getIdentifier()) + site.getIdentifier().length());
        request.setPathInfo(pathInfo);
        request.setRequestURI(pathInfo);

        request.setAttribute(WebloungeRequest.PAGE, page);
        request.setAttribute(WebloungeRequest.COMPOSER, page.getComposer(PageTemplate.DEFAULT_STAGE));
        if (pagelet != null)
          request.setAttribute(WebloungeRequest.PAGELET, pagelet);

        try {
          logger.debug("Precompiling {}:/{}", site, pathInfo);
          servlet.service(request, response);
        } catch (Throwable t) {
          while (t != t.getCause() && t.getCause() != null)
            t = t.getCause();
          if (logErrors)
            logger.warn("Error precompiling {}:/{}: {}", new Object[] {
                site,
                pathInfo,
                t.getMessage() });
          errorCount++;
        }
      }

      isRunning = false;

      security.setUser(null);
      security.setSite(null);

      // Log the precompilation results
      if (!keepGoing) {
        logger.info("Precompilation for '{}' canceled", site);
      } else if (errorCount > 0) {
        String compilationResult = "finished";
        compilationResult += " with " + errorCount + " errors";
        logger.warn("Precompilation for '{}' {}", site, compilationResult);
        if (!logErrors)
          logger.info("Precompilation error logging can be enabled in the site dispatcher service");
      } else {
        logger.info("Precompilation for '{}' finished", site);
      }
    }

  }

}
