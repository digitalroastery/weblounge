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

package ch.o2it.weblounge.contentrepository;

import org.junit.Ignore;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Utility implementation for bundle related tests.
 */
@Ignore
public class StandaloneBundle implements Bundle {

  /** The test resources class loader */
  protected ClassLoader classLoader = null;

  /** The bundle entries */
  protected List<URL> entries = new ArrayList<URL>();

  /**
   * Create a standalone bundle, i. e. one that is "running" outside of the OSGi
   * context.
   * 
   * @param classLoader
   *          the class loader to use
   */
  public StandaloneBundle(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  /**
   * Registers a new entry with this bundle.
   * 
   * @param entry
   *          the entry
   */
  public void addEntry(URL entry) {
    entries.add(entry);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#findEntries(java.lang.String,
   *      java.lang.String, boolean)
   */
  @SuppressWarnings("rawtypes")
  public Enumeration findEntries(String path, String filePattern, boolean recurse) {
    List<URL> result = new ArrayList<URL>();
    for (URL url : entries) {
      String urlPath = url.getPath();
      if (!urlPath.startsWith(path))
        continue;
      if (filePattern != null && !url.getPath().matches(filePattern))
        continue;
      if (!recurse && path.indexOf("/", path.length()) > -1)
        continue;
      result.add(url);
    }
    return Collections.enumeration(result);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#getBundleId()
   */
  public long getBundleId() {
    return 0;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#getEntry(java.lang.String)
   */
  public URL getEntry(String name) {
    return classLoader.getResource(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#getEntryPaths(java.lang.String)
   */
  @SuppressWarnings("rawtypes")
  public Enumeration getEntryPaths(String path) {
    List<URL> result = new ArrayList<URL>();
    for (URL url : entries) {
      String urlPath = url.getPath();
      if (urlPath.startsWith(path) && path.indexOf("/", path.length()) == -1)
        result.add(url);
    }
    return Collections.enumeration(result);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#getHeaders()
   */
  @SuppressWarnings("rawtypes")
  public Dictionary getHeaders() {
    return new Hashtable();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#getHeaders(java.lang.String)
   */
  @SuppressWarnings("rawtypes")
  public Dictionary getHeaders(String locale) {
    return new Hashtable();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#getLastModified()
   */
  public long getLastModified() {
    return 0;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#getLocation()
   */
  public String getLocation() {
    return "bundle:0";
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#getRegisteredServices()
   */
  public ServiceReference[] getRegisteredServices() {
    return new ServiceReference[] {};
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#getResource(java.lang.String)
   */
  public URL getResource(String name) {
    return classLoader.getResource(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#getResources(java.lang.String)
   */
  @SuppressWarnings("rawtypes")
  public Enumeration getResources(String name) throws IOException {
    return classLoader.getResources(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#getServicesInUse()
   */
  public ServiceReference[] getServicesInUse() {
    return new ServiceReference[] {};
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#getState()
   */
  public int getState() {
    return Bundle.INSTALLED;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#getSymbolicName()
   */
  public String getSymbolicName() {
    return "standalone-bundle";
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#hasPermission(java.lang.Object)
   */
  public boolean hasPermission(Object permission) {
    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#loadClass(java.lang.String)
   */
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    return classLoader.loadClass(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#start()
   */
  public void start() throws BundleException {
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#stop()
   */
  public void stop() throws BundleException {
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#uninstall()
   */
  public void uninstall() throws BundleException {
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#update()
   */
  public void update() throws BundleException {
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.Bundle#update(java.io.InputStream)
   */
  public void update(InputStream in) throws BundleException {
  }

  /**
   * {@inheritDoc}
   *
   * @see org.osgi.framework.Bundle#getBundleContext()
   */
  public BundleContext getBundleContext() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.osgi.framework.Bundle#getSignerCertificates(int)
   */
  public Map<?,?> getSignerCertificates(int signersType) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.osgi.framework.Bundle#getVersion()
   */
  public Version getVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.osgi.framework.Bundle#start(int)
   */
  public void start(int options) throws BundleException {
    // TODO Auto-generated method stub
    
  }

  /**
   * {@inheritDoc}
   *
   * @see org.osgi.framework.Bundle#stop(int)
   */
  public void stop(int options) throws BundleException {
    // TODO Auto-generated method stub
    
  }

}
