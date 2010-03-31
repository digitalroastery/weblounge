/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.dispatcher.impl;

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

  /** The bundle */
  private Bundle bundle;

  /** Root path inside the bundle */
  private String bundlePath;

  /**
   * Creates a new <code>HttpContext</code> which will be able to load resources
   * located inside the bundle <code>bundle</code>.
   * 
   * @param bundle
   */
  public BundleHttpContext(Bundle bundle) {
    this(bundle, null);
  }

  /**
   * Creates a new <code>HttpContext</code> which will be able to load resources
   * located as a child of path <code>bundlePath</code> from the bundle
   * <code>bundle</code>.
   * 
   * @param bundle
   *          the bundle
   * @param bundlePath
   *          the context path inside the bundle
   */
  public BundleHttpContext(Bundle bundle, String bundlePath) {
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
  @SuppressWarnings("unchecked")
  public URL getResource(String resourceName) {
    if (bundlePath != null)
      resourceName = bundlePath + resourceName;

    int lastSlash = resourceName.lastIndexOf('/');
    if (lastSlash == -1)
      return null;

    String path = resourceName.substring(0, lastSlash);
    if (path.length() == 0)
      path = "/";
    String file = resourceName.substring(lastSlash + 1);
    Enumeration<URL> entryPaths = bundle.findEntries(path, file, false);

    if (entryPaths != null && entryPaths.hasMoreElements())
      return entryPaths.nextElement();

    return null;
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
