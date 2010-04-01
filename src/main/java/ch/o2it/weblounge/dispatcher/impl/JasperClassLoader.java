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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Jasper enforces a URLClassLoader so he can lookup the jars in order to get
 * the TLDs. This class loader will use the Bundle-ClassPath to get the list of
 * classloaders and delegate class loading to a bundle class loader.
 */
public final class JasperClassLoader extends URLClassLoader {

  /**
   * Internal bundle class loader.
   */
  private final BundleClassLoader bundleClassLoader;

  /** The logging facility */
  private static final Logger LOG = LoggerFactory.getLogger(JasperClassLoader.class);

  public JasperClassLoader(final Bundle bundle, final ClassLoader parent) {
    super(getClassPathJars(bundle));
    bundleClassLoader = new BundleClassLoader(bundle, parent);
  }

  /**
   * Delegate to bundle class loader.
   * 
   * @see BundleClassLoader#getResource(String)
   */
  public URL getResource(String name) {
    return bundleClassLoader.getResource(name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    // the class may be either JasperClassLoader or BundleClassLoader
    if (o == null || !(o.getClass() == getClass() || o.getClass() == BundleClassLoader.class)) {
      return false;
    }

    final Bundle thisBundle = bundleClassLoader.getBundle();
    final Bundle thatBundle = ((BundleClassLoader) o).getBundle();

    if (thisBundle != null) {
      return thisBundle.equals(thatBundle);
    }
    return thatBundle == null;
  }

  @Override
  public int hashCode() {
    return bundleClassLoader.hashCode();
  }

  /**
   * Delegate to bundle class loader.
   * 
   * @see BundleClassLoader#getResources(String)
   */
  public Enumeration<URL> getResources(String name) throws IOException {
    return bundleClassLoader.getResources(name);
  }

  /**
   * Delegate to bundle class loader.
   * 
   * @see BundleClassLoader#loadClass(String)
   */
  public Class<?> loadClass(final String name) throws ClassNotFoundException {
    return bundleClassLoader.loadClass(name);
  }

  @Override
  public String toString() {
    return new StringBuffer().append(this.getClass().getSimpleName()).append("{").append("bundleClassLoader=").append(bundleClassLoader).append("}").toString();
  }

  /**
   * Returns a list of urls to jars that composes the Bundle-ClassPath.
   * 
   * @param bundle
   *          the bundle from which the class path should be taken
   * 
   * @return list or urls to jars that composes the Bundle-ClassPath.
   */
  private static URL[] getClassPathJars(final Bundle bundle) {
    final List<URL> urls = new ArrayList<URL>();
    final String bundleClasspath = (String) bundle.getHeaders().get("Bundle-ClassPath");
    if (bundleClasspath != null) {
      String[] segments = bundleClasspath.split(",");
      for (String segment : segments) {
        final URL url = bundle.getEntry(segment);
        if (url != null) {
          if (url.toExternalForm().endsWith("jar")) {
            LOG.debug("Using url: " + url);
            try {
              URL jarUrl = new URL("jar:" + url.toExternalForm() + "!/");
              urls.add(jarUrl);
            } catch (MalformedURLException ignore) {
              LOG.debug(ignore.getMessage());
            }
          }
        }
      }
    }
    LOG.debug("Bundle-ClassPath URLs: " + urls);
    return urls.toArray(new URL[urls.size()]);
  }

}
