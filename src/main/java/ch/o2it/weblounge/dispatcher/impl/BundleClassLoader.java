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

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Class loader that uses the a bundle in order to implement class loader
 * functionality.
 */
public class BundleClassLoader extends ClassLoader {

  /** An empty enumeration */
  private static final EmptyEnumeration<URL> EMPTY_URL_ENUMERATION = new EmptyEnumeration<URL>();

  /**
   * Bundle used for class loading.
   */
  private final Bundle bundle;

  /**
   * Privileged factory method.
   * 
   * @param bundle
   *          bundle to be used for class loading. Cannot be null.
   * 
   * @return created bundle class loader
   * 
   * @see BundleClassLoader#BundleClassLoader(Bundle)
   */
  public static BundleClassLoader newPriviledged(final Bundle bundle) {
    return newPriviledged(bundle, null);
  }

  /**
   * Privileged factory method.
   * 
   * @param bundle
   *          bundle to be used for class loading. Cannot be null.
   * @param parent
   *          parent class loader
   * 
   * @return created bundle class loader
   * 
   * @see BundleClassLoader#BundleClassLoader(Bundle,ClassLoader)
   */
  public static BundleClassLoader newPriviledged(final Bundle bundle,
      final ClassLoader parent) {
    return AccessController.doPrivileged(new PrivilegedAction<BundleClassLoader>() {
      public BundleClassLoader run() {
        return new BundleClassLoader(bundle, parent);
      }
    });
  }

  /**
   * Creates a bundle class loader with no parent.
   * 
   * @param bundle
   *          bundle to be used for class loading. Cannot be null.
   */
  public BundleClassLoader(final Bundle bundle) {
    this(bundle, null);
  }

  /**
   * Creates a bundle class loader.
   * 
   * @param bundle
   *          bundle to be used for class loading. Cannot be null.
   * @param parent
   *          parent class loader
   */
  public BundleClassLoader(final Bundle bundle, final ClassLoader parent) {
    super(parent);
    if (bundle == null)
      throw new IllegalArgumentException("Bundle cannot be null");
    this.bundle = bundle;
  }

  /**
   * Getter.
   * 
   * @return the bundle the class loader loads from
   */
  public Bundle getBundle() {
    return bundle;
  }

  /**
   * If there is a parent class loader use the super implementation that will
   * first use the parent and as a fallback it will call findResource(). In case
   * there is no parent directly use findResource() as if we call the super
   * implementation it will use the VMClassLoader, fact that should be avoided.
   * 
   * @see ClassLoader#getResource(String)
   */
  @Override
  public URL getResource(final String name) {
    if (getParent() != null) {
      return super.getResource(name);
    }
    return findResource(name);
  }

  /**
   * If there is a parent class loader use the super implementation that will
   * first use the parent and as a fallback it will call findResources(). In
   * case there is no parent directly use findResources() as if we call the
   * super implementation it will use the VMClassLoader, fact that should be
   * avoided.
   * 
   * @see ClassLoader#getResources(String)
   */
  @Override
  public Enumeration<URL> getResources(final String name) throws IOException {
    if (getParent() != null) {
      return super.getResources(name);
    } else {
      return findResources(name);
    }
  }

  /**
   * Use bundle to find find the class.
   * 
   * @see ClassLoader#findClass(String)
   */
  @Override
  protected Class<?> findClass(final String name) throws ClassNotFoundException {
    return bundle.loadClass(name);
  }

  /**
   * If there is a parent class loader use the super implementation that will
   * first use the parent and as a fallback it will call findClass(). In case
   * there is no parent directly use findClass() as if we call the super
   * implementation it will use the VMClassLoader, fact that should be avoided.
   * 
   * @see ClassLoader#getResource(String)
   */
  @Override
  protected synchronized Class<?> loadClass(final String name,
      final boolean resolve) throws ClassNotFoundException {
    if (getParent() != null) {
      return super.loadClass(name, resolve);
    }
    final Class<?> classToLoad = findClass(name);
    if (resolve) {
      resolveClass(classToLoad);
    }
    return classToLoad;
  }

  /**
   * Use bundle to find resource.
   * 
   * @see ClassLoader#findResource(String)
   */
  @Override
  protected URL findResource(final String name) {
    return bundle.getResource(name);
  }

  /**
   * Use bundle to find resources.
   * 
   * @see ClassLoader#findResources(String)
   */
  @Override
  @SuppressWarnings("unchecked")
  protected Enumeration<URL> findResources(final String name)
      throws IOException {
    Enumeration resources = bundle.getResources(name);
    // Bundle.getResources may return null, in such case return empty
    // enumeration
    if (resources == null) {
      return EMPTY_URL_ENUMERATION;
    } else {
      return resources;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return (bundle != null ? bundle.hashCode() : 0) * 37 + (getParent() != null ? getParent().hashCode() : 0);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BundleClassLoader that = (BundleClassLoader) o;

    if (bundle != null ? !bundle.equals(that.bundle) : that.bundle != null) {
      return false;
    }

    if (getParent() != null ? !getParent().equals(that.getParent()) : that.getParent() != null) {
      return false;
    }

    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new StringBuffer().append(this.getClass().getSimpleName()).append("{").append("bundle=").append(bundle).append(",parent=").append(getParent()).append("}").toString();
  }

  /**
   * Utility implementation that provides an empty enumeration for the given
   * type <code>T</code>.
   */
  protected static final class EmptyEnumeration<T> implements Enumeration<T> {

    /**
     * {@inheritDoc}
     * 
     * @see java.util.Enumeration#hasMoreElements()
     */
    public boolean hasMoreElements() {
      return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.util.Enumeration#nextElement()
     */
    public T nextElement() {
      throw new NoSuchElementException();
    }

  }

}
