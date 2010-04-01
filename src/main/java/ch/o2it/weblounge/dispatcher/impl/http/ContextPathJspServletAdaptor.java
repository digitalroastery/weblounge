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

package ch.o2it.weblounge.dispatcher.impl.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * TODO: Comment ContextPathServletAdaptor
 */
public class ContextPathJspServletAdaptor implements Servlet {

  private Servlet delegate;
  String contextPath;
  String baseResourcePath;

  public ContextPathJspServletAdaptor(Servlet delegate, String contextPath, String baseResourcePath) {
    this.delegate = delegate;
    this.contextPath = (contextPath == null || contextPath.equals("/")) ? "" : contextPath;
    this.baseResourcePath = (baseResourcePath == null || baseResourcePath.equals("/")) ? "" : baseResourcePath;
  }

  public void init(ServletConfig config) throws ServletException {
    delegate.init(new ServletConfigAdaptor(config));
  }

  public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
    delegate.service(new HttpServletRequestAdaptor((HttpServletRequest) request), response);
  }

  public void destroy() {
    delegate.destroy();
  }

  public ServletConfig getServletConfig() {
    return delegate.getServletConfig();
  }

  public String getServletInfo() {
    return delegate.getServletInfo();
  }

  private class ServletConfigAdaptor implements ServletConfig {
    private ServletConfig config;
    private ServletContext context;

    public ServletConfigAdaptor(ServletConfig config) {
      this.config = config;
      this.context = new ServletContextAdaptor(config.getServletContext());
    }

    public String getInitParameter(String arg0) {
      return config.getInitParameter(arg0);
    }

    public Enumeration getInitParameterNames() {
      return config.getInitParameterNames();
    }

    public ServletContext getServletContext() {
      return context;
    }

    public String getServletName() {
      return config.getServletName();
    }
  }

  private class ServletContextAdaptor implements ServletContext {

    private ServletContext delegate;

    public ServletContextAdaptor(ServletContext delegate) {
      this.delegate = delegate;
    }

    public RequestDispatcher getRequestDispatcher(String path) {
      if (contextPath.equals("/")) //$NON-NLS-1$
        return new RequestDispatcherAdaptor(delegate.getRequestDispatcher(path));
      return new RequestDispatcherAdaptor(delegate.getRequestDispatcher(contextPath + path));
    }

    public URL getResource(String name) throws MalformedURLException {
      return delegate.getResource(baseResourcePath + name);
    }

    public InputStream getResourceAsStream(String name) {
      try {
        URL resourceURL = getResource(name);
        if (resourceURL != null)
          return resourceURL.openStream();
      } catch (IOException e) {
        log("Error opening stream for resource '" + name + "'", e); //$NON-NLS-1$ //$NON-NLS-2$
      }
      return null;
    }

    public Set getResourcePaths(String name) {
      Set delegateResources = delegate.getResourcePaths(baseResourcePath + name);
      if (delegateResources == null)
        return null;
      
      Set result = new HashSet();
      for (Iterator it = delegateResources.iterator(); it.hasNext();) {
        String resourcePath = (String) it.next();
        result.add(resourcePath.substring(baseResourcePath.length()));
      }
      return result;
    }

    public Object getAttribute(String arg0) {
      return delegate.getAttribute(arg0);
    }

    public Enumeration getAttributeNames() {
      return delegate.getAttributeNames();
    }

    public ServletContext getContext(String arg0) {
      return delegate.getContext(arg0);
    }

    public String getContextPath() {
      return delegate.getContextPath();
    }
    
    public String getInitParameter(String arg0) {
      return delegate.getInitParameter(arg0);
    }

    public Enumeration getInitParameterNames() {
      return delegate.getInitParameterNames();
    }

    public int getMajorVersion() {
      return delegate.getMajorVersion();
    }

    public String getMimeType(String arg0) {
      return delegate.getMimeType(arg0);
    }

    public int getMinorVersion() {
      return delegate.getMinorVersion();
    }

    public RequestDispatcher getNamedDispatcher(String arg0) {
      return new RequestDispatcherAdaptor(delegate.getNamedDispatcher(arg0));
    }

    public String getRealPath(String arg0) {
      return delegate.getRealPath(arg0);
    }

    public String getServerInfo() {
      return delegate.getServerInfo();
    }

    /** @deprecated **/
    public Servlet getServlet(String arg0) throws ServletException {
      return delegate.getServlet(arg0);
    }

    public String getServletContextName() {
      return delegate.getServletContextName();
    }

    /** @deprecated **/
    public Enumeration getServletNames() {
      return delegate.getServletNames();
    }

    /** @deprecated **/
    public Enumeration getServlets() {
      return delegate.getServlets();
    }

    /** @deprecated **/
    public void log(Exception arg0, String arg1) {
      delegate.log(arg0, arg1);
    }

    public void log(String arg0, Throwable arg1) {
      delegate.log(arg0, arg1);
    }

    public void log(String arg0) {
      delegate.log(arg0);
    }

    public void removeAttribute(String arg0) {
      delegate.removeAttribute(arg0);
    }

    public void setAttribute(String arg0, Object arg1) {
      delegate.setAttribute(arg0, arg1);
    }
  }

  private class HttpServletRequestAdaptor extends HttpServletRequestWrapper {
    static final String INCLUDE_REQUEST_URI_ATTRIBUTE = "javax.servlet.include.request_uri"; //$NON-NLS-1$
    static final String INCLUDE_CONTEXT_PATH_ATTRIBUTE = "javax.servlet.include.context_path"; //$NON-NLS-1$
    static final String INCLUDE_SERVLET_PATH_ATTRIBUTE = "javax.servlet.include.servlet_path"; //$NON-NLS-1$
    static final String INCLUDE_PATH_INFO_ATTRIBUTE = "javax.servlet.include.path_info"; //$NON-NLS-1$
    private boolean isRequestDispatcherInclude;

    public HttpServletRequestAdaptor(HttpServletRequest req) {
      super(req);
      isRequestDispatcherInclude = req.getAttribute(HttpServletRequestAdaptor.INCLUDE_REQUEST_URI_ATTRIBUTE) != null;
    }

    public String getServletPath() {
      if (isRequestDispatcherInclude)
        return super.getServletPath();

      String fullPath = super.getServletPath();
      return fullPath.substring(contextPath.length());
    }

    public String getContextPath() {
      if (isRequestDispatcherInclude)
        return super.getContextPath();

      return super.getContextPath() + contextPath;
    }

    public Object getAttribute(String attributeName) {
      if (isRequestDispatcherInclude) {
        if (attributeName.equals(HttpServletRequestAdaptor.INCLUDE_CONTEXT_PATH_ATTRIBUTE)) {
          String contextPathAttribute = (String) super.getAttribute(HttpServletRequestAdaptor.INCLUDE_CONTEXT_PATH_ATTRIBUTE);
          if (contextPathAttribute == null || contextPathAttribute.equals("/")) //$NON-NLS-1$
            return contextPath;

          return contextPathAttribute + contextPath;
        } else if (attributeName.equals(HttpServletRequestAdaptor.INCLUDE_SERVLET_PATH_ATTRIBUTE)) {
          String servletPath = (String) super.getAttribute(HttpServletRequestAdaptor.INCLUDE_SERVLET_PATH_ATTRIBUTE);
          return servletPath.substring(contextPath.length());
        }
      }
      return super.getAttribute(attributeName);
    }

    public RequestDispatcher getRequestDispatcher(String arg0) {
      return new RequestDispatcherAdaptor(super.getRequestDispatcher(contextPath + arg0));
    }
  }
  
  private class RequestDispatcherAdaptor implements RequestDispatcher {

    private RequestDispatcher requestDispatcher;
    public RequestDispatcherAdaptor(RequestDispatcher requestDispatcher) {
      this.requestDispatcher = requestDispatcher;
    }

    public void forward(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
      if (req instanceof HttpServletRequestAdaptor)
        req = ((HttpServletRequestAdaptor) req).getRequest();
      
      requestDispatcher.forward(req, resp);
    }

    public void include(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
      if (req instanceof HttpServletRequestAdaptor)
        req = ((HttpServletRequestAdaptor) req).getRequest();
      
      requestDispatcher.include(req, resp);
    }
  }
}