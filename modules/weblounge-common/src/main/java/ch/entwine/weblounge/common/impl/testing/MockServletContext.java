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

package ch.entwine.weblounge.common.impl.testing;

import ch.entwine.weblounge.common.impl.request.DefaultResourceLoader;
import ch.entwine.weblounge.common.impl.request.Resource;
import ch.entwine.weblounge.common.impl.request.ResourceLoader;
import ch.entwine.weblounge.common.url.PathUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

/**
 * Mock implementation of the {@link javax.servlet.ServletContext} interface.
 */
public class MockServletContext implements ServletContext {

  /** The java temp dir property name */
  private static final String TEMP_DIR_SYSTEM_PROPERTY = "java.io.tmpdir";

  /** The temp dir context key */
  private static final String TEMP_DIR_CONTEXT_ATTRIBUTE = "javax.servlet.context.tempdir";

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(MockServletContext.class);

  /** The resource loader */
  private final ResourceLoader resourceLoader;

  /** Base path on the server filesystem to context resources */
  private final String resourceBasePath;

  /** The context path */
  private String contextPath = "";

  /** The servlet contexts */
  private final Map<String, ServletContext> contexts = new HashMap<>();

  /** The init parameters */
  private final Hashtable<String, String> initParameters = new Hashtable<>();

  /** The context attributes */
  private final Hashtable<String, Object> attributes = new Hashtable<>();

  /** Name of this context */
  private String servletContextName = "MockServletContext";

  /**
   * Create a new MockServletContext, using no base path and a
   * DefaultResourceLoader (i.e. the classpath root as WAR root).
   * 
   * @see org.springframework.core.io.DefaultResourceLoader
   */
  public MockServletContext() {
    this("", null);
  }

  /**
   * Create a new MockServletContext, using a DefaultResourceLoader.
   * 
   * @param resourceBasePath
   *          the WAR root directory (should not end with a slash)
   * @see org.springframework.core.io.DefaultResourceLoader
   */
  public MockServletContext(String resourceBasePath) {
    this(resourceBasePath, null);
  }

  /**
   * Create a new MockServletContext, using the specified ResourceLoader and no
   * base path.
   * 
   * @param resourceLoader
   *          the ResourceLoader to use (or null for the default)
   */
  public MockServletContext(ResourceLoader resourceLoader) {
    this("", resourceLoader);
  }

  /**
   * Create a new MockServletContext with the given resource loader and resource
   * base path.
   * 
   * @param resourceBasePath
   *          the WAR root directory (should not end with a slash)
   * @param resourceLoader
   *          the ResourceLoader to use (or null for the default)
   */
  public MockServletContext(String resourceBasePath,
      ResourceLoader resourceLoader) {
    this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
    this.resourceBasePath = (resourceBasePath != null ? resourceBasePath : "");

    // Use JVM temp dir as ServletContext temp dir.
    String tempDir = System.getProperty(TEMP_DIR_SYSTEM_PROPERTY);
    if (tempDir != null) {
      this.attributes.put(TEMP_DIR_CONTEXT_ATTRIBUTE, new File(tempDir));
    }
  }

  /**
   * Build a full resource location for the given path, prepending the resource
   * base path of this MockServletContext.
   * 
   * @param path
   *          the path as specified
   * @return the full resource path
   */
  protected String getResourceLocation(String path) {
    return PathUtils.concat(resourceBasePath, path);
  }

  /**
   * Sets the servlet context path.
   * 
   * @param contextPath
   *          the context path
   */
  public void setContextPath(String contextPath) {
    this.contextPath = (contextPath != null ? contextPath : "");
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getContextPath()
   */
  public String getContextPath() {
    return this.contextPath;
  }

  /**
   * Registers another servlet context.
   * 
   * @param contextPath
   *          the context path
   * @param context
   *          the context
   */
  public void registerContext(String contextPath, ServletContext context) {
    this.contexts.put(contextPath, context);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getContext(java.lang.String)
   */
  public ServletContext getContext(String contextPath) {
    if (this.contextPath.equals(contextPath)) {
      return this;
    }
    return this.contexts.get(contextPath);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getMajorVersion()
   */
  public int getMajorVersion() {
    return 2;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getMinorVersion()
   */
  public int getMinorVersion() {
    return 5;
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getMimeType(java.lang.String)
   */
  public String getMimeType(String filePath) {
    return MimeTypeResolver.getMimeType(filePath);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getResourcePaths(java.lang.String)
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Set getResourcePaths(String path) {
    String actualPath = (path.endsWith("/") ? path : path + "/");
    Resource resource = this.resourceLoader.getResource(getResourceLocation(actualPath));
    try {
      File file = resource.getFile();
      String[] fileList = file.list();
      if (fileList == null || fileList.length == 0) {
        return null;
      }
      Set resourcePaths = new LinkedHashSet(fileList.length);
      for (int i = 0; i < fileList.length; i++) {
        String resultPath = PathUtils.concat(actualPath, fileList[i]);
        if (resourceLoader.getResource(resultPath).getFile().isDirectory()) {
          resultPath += "/";
        }
        resourcePaths.add(resultPath);
      }
      return resourcePaths;
    } catch (IOException e) {
      logger.warn("Could not get resource paths for " + resource, e);
      return null;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getResource(java.lang.String)
   */
  public URL getResource(String path) throws MalformedURLException {
    Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
    if (!resource.exists()) {
      return null;
    }
    try {
      return resource.getURL();
    } catch (MalformedURLException e) {
      throw e;
    } catch (IOException e) {
      logger.warn("Could not get URL for " + resource, e);
      return null;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
   */
  public InputStream getResourceAsStream(String path) {
    Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
    if (!resource.exists()) {
      return null;
    }
    try {
      return resource.getInputStream();
    } catch (IOException e) {
      logger.warn("Could not open InputStream for " + resource, e);
      return null;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getRequestDispatcher(java.lang.String)
   */
  public RequestDispatcher getRequestDispatcher(String path) {
    if (!path.startsWith("/")) {
      throw new IllegalArgumentException("RequestDispatcher path at ServletContext level must start with '/'");
    }
    return new MockRequestDispatcher(path);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getNamedDispatcher(java.lang.String)
   */
  public RequestDispatcher getNamedDispatcher(String path) {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getServlet(java.lang.String)
   */
  public Servlet getServlet(String name) {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getServlets()
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Enumeration getServlets() {
    return Collections.enumeration(Collections.EMPTY_SET);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getServletNames()
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Enumeration getServletNames() {
    return Collections.enumeration(Collections.EMPTY_SET);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#log(java.lang.String)
   */
  public void log(String message) {
    logger.info(message);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#log(java.lang.Exception,
   *      java.lang.String)
   */
  public void log(Exception e, String message) {
    logger.info(message, e);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#log(java.lang.String,
   *      java.lang.Throwable)
   */
  public void log(String message, Throwable t) {
    logger.info(message, t);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getRealPath(java.lang.String)
   */
  public String getRealPath(String path) {
    Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
    try {
      return resource.getFile().getAbsolutePath();
    } catch (IOException e) {
      logger.warn("Could not determine real path of resource " + resource, e);
      return null;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getServerInfo()
   */
  public String getServerInfo() {
    return "MockServletContext";
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
   */
  public String getInitParameter(String name) {
    if (name == null)
      throw new IllegalArgumentException("Parameter name must not be null");
    return this.initParameters.get(name);
  }

  /**
   * Adds an init parameter for this servlet context.
   * 
   * @param name
   *          the parameter name
   * @param value
   *          the parameter value
   */
  public void addInitParameter(String name, String value) {
    if (name == null)
      throw new IllegalArgumentException("Parameter name must not be null");
    this.initParameters.put(name, value);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getInitParameterNames()
   */
  public Enumeration<String> getInitParameterNames() {
    return this.initParameters.keys();
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
   */
  public Object getAttribute(String name) {
    if (name == null)
      throw new IllegalArgumentException("Attribute name must not be null");
    return this.attributes.get(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getAttributeNames()
   */
  public Enumeration<String> getAttributeNames() {
    return this.attributes.keys();
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#setAttribute(java.lang.String,
   *      java.lang.Object)
   */
  public void setAttribute(String name, Object value) {
    if (name == null)
      throw new IllegalArgumentException("Attribute name must not be null");
    if (value != null) {
      this.attributes.put(name, value);
    } else {
      this.attributes.remove(name);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
   */
  public void removeAttribute(String name) {
    if (name == null)
      throw new IllegalArgumentException("Attribute name must not be null");
    this.attributes.remove(name);
  }

  /**
   * Sets the servlet context name.
   * 
   * @param servletContextName
   *          the context name
   */
  public void setServletContextName(String servletContextName) {
    this.servletContextName = servletContextName;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletContext#getServletContextName()
   */
  public String getServletContextName() {
    return this.servletContextName;
  }

  /**
   * Inner factory class used to just introduce a Java Activation Framework
   * dependency when actually asked to resolve a MIME type.
   */
  private static final class MimeTypeResolver {

    /**
     * This class is not intended to be instantiated.
     */
    private MimeTypeResolver() {
      // Nothing to be done here
    }

    /**
     * 
     * @param filePath
     * @return
     */
    public static String getMimeType(String filePath) {
      return null;
      // TODO Reactive java action framework (and solve bundle management)
      // return FileTypeMap.getDefaultFileTypeMap().getContentType(filePath);
    }
  }

  @Override
  public int getEffectiveMajorVersion() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getEffectiveMinorVersion() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean setInitParameter(String name, String value) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Dynamic addServlet(String servletName, String className) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Dynamic addServlet(String servletName, Servlet servlet) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Dynamic addServlet(String servletName,
      Class<? extends Servlet> servletClass) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends Servlet> T createServlet(Class<T> clazz)
      throws ServletException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ServletRegistration getServletRegistration(String servletName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, ? extends ServletRegistration> getServletRegistrations() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName,
      String className) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName,
      Filter filter) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName,
      Class<? extends Filter> filterClass) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends Filter> T createFilter(Class<T> clazz)
      throws ServletException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FilterRegistration getFilterRegistration(String filterName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SessionCookieConfig getSessionCookieConfig() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setSessionTrackingModes(
      Set<SessionTrackingMode> sessionTrackingModes) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addListener(String className) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public <T extends EventListener> void addListener(T t) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void addListener(Class<? extends EventListener> listenerClass) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public <T extends EventListener> T createListener(Class<T> clazz)
      throws ServletException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JspConfigDescriptor getJspConfigDescriptor() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ClassLoader getClassLoader() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void declareRoles(String... roleNames) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String getVirtualServerName() {
    // TODO Auto-generated method stub
    return null;
  }

}
