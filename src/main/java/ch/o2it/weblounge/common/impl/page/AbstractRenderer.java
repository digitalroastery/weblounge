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
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.RenderException;
import ch.o2it.weblounge.common.site.Renderer;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

/**
 * Thread safe base implementation for <code>PageTemplate</code>s and
 * <code>PageletRenderer</code>s.
 */
public abstract class AbstractRenderer extends GeneralComposeable implements Renderer {

  /** The logging facility */
  private static final Logger log_ = LoggerFactory.getLogger(AbstractRenderer.class);

  /** Renderer URL */
  protected URL renderer = null;

  /** The supported flavors */
  protected Set<String> flavors = new HashSet<String>();

  /** Size of the temporary buffer */
  private static final int BUFFER_SIZE = 8 * 1024;

  /** A temporary buffer for data copying */
  private static final ThreadLocal<byte[]> buffer = new ThreadLocal<byte[]>();

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
    if (renderer == null)
      throw new IllegalArgumentException("Renderer must not be null");
    this.renderer = renderer;
  }

  /**
   * Adds the given flavor to the list of supported flavors.
   * 
   * @param flavor
   *          the supported flavor
   * @see RequestFlavor
   */
  public void addFlavor(String flavor) {
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
  public void removeFlavor(String flavor) {
    if (flavor == null)
      throw new IllegalArgumentException("Flavor must not be null");
    flavors.remove(flavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Composeable#getFlavors()
   */
  public String[] getFlavors() {
    return flavors.toArray(new String[flavors.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Composeable#supportsFlavor(java.lang.String)
   */
  public boolean supportsFlavor(String flavor) {
    return flavors.contains(flavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Renderer#setRenderer(java.net.URL)
   */
  public void setRenderer(URL renderer) {
    if (renderer == null)
      throw new IllegalArgumentException("Renderer must not be null");
    this.renderer = renderer;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.Renderer#getRenderer()
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
      if (!"file".equals(renderer.getProtocol()))
        throw new RenderException(this, renderer.toExternalForm() + " is not on the local file system");

      // Find the best match for the template
      // TODO: Cache the results
      String[] filePaths = LanguageSupport.getLanguageVariantsByPriority(renderer.toExternalForm(), language, site.getDefaultLanguage());
      for (String path : filePaths) {
        log_.trace("Looking for jsp {}", path);
        File f = new File(path);
        if (f.exists()) {
          log_.debug("Found jsp at {}", path);
          jsp = f;
          break;
        }
      }

      // Did we find a suitable JSP?
      if (jsp == null) {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        throw new RenderException(this, "No suitable java server page found for " + renderer + " and language " + language.getIdentifier());
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

      // Finally serve the JSP
      log_.info("Including jsp {}", jsp);
      InputStream is = new FileInputStream(jsp);
      try {
        int read = 0;
        byte tmp[] = buffer.get();
        if (tmp == null) {
          tmp = new byte[BUFFER_SIZE];
          buffer.set(tmp);
        }
        OutputStream os = response.getOutputStream();
        while ((read = is.read(tmp)) >= 0) {
          os.write(tmp, 0, read);
        }
        os.flush();
        os.close();
      } catch (SocketException e) {
        log_.debug("Request for {} canceled by client", jsp);
      } finally {
        is.close();
      }

    } catch (IOException e) {
      log_.error("Exception while including jsp {}", jsp, e);
    } catch (Throwable t) {
      throw new RenderException(this, t);
    }
  }

}
