/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.page;

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.content.RenderException;
import ch.o2it.weblounge.common.content.Renderer;
import ch.o2it.weblounge.common.impl.content.GeneralComposeable;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.request.SiteRequestWrapper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletResponse;

/**
 * Thread safe base implementation for <code>PageTemplate</code>s and
 * <code>PageletRenderer</code>s.
 */
public abstract class AbstractRenderer extends GeneralComposeable implements Renderer {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(AbstractRenderer.class);

  /** Renderer URL */
  protected URL renderer = null;

  /** The supported flavors */
  protected Set<RequestFlavor> flavors = new HashSet<RequestFlavor>();

  /**
   * Creates a renderer with a recheck time of one day and a valid time of one
   * week.
   */
  public AbstractRenderer() {
    super(Times.MS_PER_DAY, Times.MS_PER_WEEK);
  }

  /**
   * Creates a renderer with a recheck time of one day and a valid time of one
   * week.
   * 
   * @param identifier
   *          the renderer identifier
   */
  public AbstractRenderer(String identifier) {
    this(identifier, null);
  }

  /**
   * Creates a renderer with a recheck time of one day and a valid time of one
   * week.
   * 
   * @param identifier
   *          the renderer identifier
   * @param renderer
   *          url of the renderer
   */
  public AbstractRenderer(String identifier, URL renderer) {
    super(identifier, Times.MS_PER_DAY, Times.MS_PER_WEEK);
    this.renderer = renderer;
  }

  /**
   * Adds the given flavor to the list of supported flavors.
   * 
   * @param flavor
   *          the supported flavor
   * @see RequestFlavor
   */
  public void addFlavor(RequestFlavor flavor) {
    if (flavor == null)
      throw new IllegalArgumentException("Flavor must not be null");
    flavors.add(flavor);
  }

  /**
   * Removes the specified flavor from the list of supported flavors.
   * 
   * @param flavor
   *          the flavor
   */
  public void removeFlavor(RequestFlavor flavor) {
    if (flavor == null)
      throw new IllegalArgumentException("Flavor must not be null");
    flavors.remove(flavor);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Renderer#getFlavors()
   */
  public RequestFlavor[] getFlavors() {
    return flavors.toArray(new RequestFlavor[flavors.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Composeable#supportsFlavor(java.lang.String)
   */
  public boolean supportsFlavor(RequestFlavor flavor) {
    return flavors.contains(flavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Renderer#setRenderer(java.net.URL)
   */
  public void setRenderer(URL renderer) {
    if (renderer == null)
      throw new IllegalArgumentException("Renderer must not be null");
    this.renderer = renderer;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Renderer#getRenderer()
   */
  public URL getRenderer() {
    return renderer;
  }

  /**
   * Convenience implementation for JSP renderer. The <code>renderer</code> url
   * is first looked up using the available language information from request
   * and site. Then it is included in the response.
   * 
   * @param request
   *          the request
   * @param response
   *          the response
   * @param renderer
   *          the renderer
   * @throws RenderException
   *           if an error occurs while rendering
   */
  protected void includeJSP(WebloungeRequest request,
      WebloungeResponse response, URL renderer) throws RenderException {

    Site site = request.getSite();
    Language language = request.getLanguage();
    File jsp = null;

    try {
      if ("file".equals(renderer.getProtocol())) {
        // Find the best match for the template
        String[] filePaths = LanguageSupport.getLanguageVariantsByPriority(renderer.toExternalForm(), language, site.getDefaultLanguage());
        for (String path : filePaths) {
          logger.trace("Looking for jsp {}", path);
          File f = new File(path);
          if (f.exists()) {
            logger.debug("Found jsp at {}", path);
            jsp = f;
            break;
          }
        }
  
        // Did we find a suitable JSP?
        if (jsp == null) {
          response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          throw new RenderException(this, "No suitable java server page found for " + renderer + " and language '" + language.getIdentifier() + "'");
        }
  
        // Check readability
        if (!jsp.canRead()) {
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
          throw new RenderException(this, "Java server page at " + jsp + " cannot be read");
        }
  
        // No directory listings allowed
        if (!jsp.isFile()) {
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
          throw new RenderException(this, "Java server page at " + jsp + " is not a file");
        }
        
        renderer = jsp.toURI().toURL();
      }
      
      // Prepare a request to site resources
      String requestPath = renderer.getPath();
      if (!request.getContextPath().equals(""))
        requestPath = requestPath.substring(request.getContextPath().length());
      SiteRequestWrapper siteRequest = new SiteRequestWrapper(request, requestPath, false);

      RequestDispatcher dispatcher = request.getRequestDispatcher(siteRequest.getServletPath());
      if (dispatcher == null)
        throw new IllegalStateException("No dispatcher found for site '" + site + "'");
        
      // Finally serve the JSP
      logger.debug("Including jsp {}", renderer);
      dispatcher.include(siteRequest, response);
      
    } catch (IOException e) {
      logger.error("Exception while including jsp {}", renderer, e);
    } catch (Throwable t) {
      throw new RenderException(this, t);
    }
  }

}
