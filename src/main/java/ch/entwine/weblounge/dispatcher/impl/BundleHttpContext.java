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

package ch.entwine.weblounge.dispatcher.impl;

import ch.entwine.weblounge.common.impl.url.UrlUtils;

import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class is a <code>HttpContext</code> that is able to load resources from
 * an <code>OSGi</code> bundle.
 */
public class BundleHttpContext implements HttpContext {

  /** WEB-INF directory */
  private static final String WEB_INF = "/WEB-INF/";

  /** The bundle */
  private Bundle bundle = null;

  /** The site uri */
  private String siteURI = null;

  /** Root path inside the bundle */
  private String bundlePath = null;

  /**
   * Creates a new <code>HttpContext</code> which will be able to load resources
   * located inside the bundle <code>bundle</code>.
   * 
   * @param bundle
   */
  public BundleHttpContext(Bundle bundle) {
    this(bundle, null, null);
  }

  /**
   * Creates a new <code>HttpContext</code> which will be able to load resources
   * either located as a child of path <code>bundlePath</code> from the bundle
   * <code>bundle</code> or inside jasper's site work directory.
   * 
   * @param bundle
   *          the bundle
   * @param siteURI
   *          the site's mountpoint, e. g. <code>/weblounge-sites/mysite</code>
   * @param bundlePath
   *          the context path inside the bundle
   */
  public BundleHttpContext(Bundle bundle, String siteURI, String bundlePath) {
    if (bundle == null)
      throw new IllegalArgumentException("Bundle must not be null");
    this.bundle = bundle;
    if (bundlePath != null) {
      if (bundlePath.endsWith("/"))
        bundlePath = bundlePath.substring(0, bundlePath.length() - 1);
      if (bundlePath.length() == 0)
        bundlePath = null;
    }
    this.bundlePath = bundlePath;
    this.siteURI = siteURI;
  }

  /**
   * Returns the bundle that was used to create this context.
   * 
   * @return the bundle
   */
  public Bundle getBundle() {
    return bundle;
  }

  /**
   * Returns the path to the site's content inside the bundle.
   * 
   * @return the path to the site content
   */
  public String getBundlePath() {
    return bundlePath;
  }

  /**
   * Returns the uri that is mapped to the bundle content.
   * 
   * @return the uri
   */
  public String getSiteURI() {
    return siteURI;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.http.HttpContext#getMimeType(java.lang.String)
   */
  public String getMimeType(String name) {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.http.HttpContext#handleSecurity(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  public boolean handleSecurity(HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.http.HttpContext#getResource(java.lang.String)
   */
  public URL getResource(String resourceName) {
    if (resourceName.startsWith(siteURI))
      resourceName = resourceName.substring(siteURI.length());
    if (bundlePath != null && !resourceName.startsWith(WEB_INF))
      resourceName = UrlUtils.concat(bundlePath, resourceName);
    return bundle.getResource(resourceName);
  }

  /**
   * Returns a set of <code>URL</code>s identifying the resources located at
   * <code>path</code>.
   * 
   * @param path
   *          path into the bundle
   * @return the resources located at <code>path</code>
   */
  @SuppressWarnings("unchecked")
  public Set<String> getResourcePaths(String path) {
    if (bundlePath != null)
      path = bundlePath + path;

    Enumeration<URL> entryPaths = bundle.findEntries(path, null, false);
    if (entryPaths == null)
      return null;

    Set<String> result = new HashSet<String>();
    while (entryPaths.hasMoreElements()) {
      URL entryURL = entryPaths.nextElement();
      String entryPath = entryURL.getFile();
      if (bundlePath == null)
        result.add(entryPath);
      else
        result.add(entryPath.substring(bundlePath.length()));
    }
    return result;
  }

}
