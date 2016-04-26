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

import ch.entwine.weblounge.common.impl.request.DelegatingServletInputStream;
import ch.entwine.weblounge.common.impl.request.HeaderValueCollection;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

/**
 * Mock implementation of the Servlet 2.4 API
 * {@link javax.servlet.http.HttpServletRequest} interface.
 */
public class MockHttpServletRequest implements HttpServletRequest {

  /** The default protocol: 'http' */
  public static final String DEFAULT_PROTOCOL = "http";

  /** The default server address: '127.0.0.1' */
  public static final String DEFAULT_SERVER_ADDR = "127.0.0.1";

  /** The default server name: 'localhost' */
  public static final String DEFAULT_SERVER_NAME = "localhost";

  /** The default server port: '80' */
  public static final int DEFAULT_SERVER_PORT = 80;

  /** The default remote address: '127.0.0.1' */
  public static final String DEFAULT_REMOTE_ADDR = "127.0.0.1";

  /** The default remote host: 'localhost' */
  public static final String DEFAULT_REMOTE_HOST = "localhost";

  private boolean active = true;

  /** Request attributes */
  private final Hashtable<String, Object> attributes = new Hashtable<String, Object>();

  /** The character encoding */
  private String characterEncoding = null;

  /** The request content */
  private byte[] content = null;

  /** The request content type */
  private String contentType = null;

  /** The request parameters */
  private final Map<String, String[]> parameters = new LinkedHashMap<>();

  /** The request protocol */
  private String protocol = DEFAULT_PROTOCOL;

  /** The request scheme */
  private String scheme = DEFAULT_PROTOCOL;

  /** The server name */
  private String serverName = DEFAULT_SERVER_NAME;

  /** The server port number */
  private int serverPort = DEFAULT_SERVER_PORT;

  /** The remote address */
  private String remoteAddr = DEFAULT_REMOTE_ADDR;

  /** The remote host name */
  private String remoteHost = DEFAULT_REMOTE_HOST;

  /** List of locales in descending order */
  private final Vector<Locale> locales = new Vector<Locale>();

  /** Determines whether this connection is secure */
  private boolean secure = false;

  /** The servlet context */
  private final ServletContext servletContext;

  /** The remote port number */
  private int remotePort = DEFAULT_SERVER_PORT;

  /** The local server name */
  private String localName = DEFAULT_SERVER_NAME;

  /** The local server address */
  private String localAddr = DEFAULT_SERVER_ADDR;

  /** The local server port */
  private int localPort = DEFAULT_SERVER_PORT;

  /** The authentication type */
  private String authType = null;

  /** The request cookies */
  private Cookie[] cookies;

  /** Http request headers */
  private final Hashtable<String, HeaderValueCollection> headers = new Hashtable<String, HeaderValueCollection>();

  /** The request method */
  private String method = null;

  /** The path info */
  private String pathInfo = null;

  /** The request context path */
  private String contextPath = "";

  /** The request string */
  private String queryString = null;

  /** The remote user name */
  private String remoteUser = null;

  /** The set of user roles */
  private final Set<String> userRoles = new HashSet<String>();

  /** The user principal */
  private Principal userPrincipal = null;

  /** The request uri */
  private String requestURI = null;

  /** The servlet path */
  private String servletPath = "";

  /** The Http session */
  private HttpSession session = null;

  /** Determines whether the requested session is valid */
  private boolean requestedSessionIdValid = true;

  /** Determines whether the session will be returned from the cookies */
  private boolean requestedSessionIdFromCookie = true;

  /** Determines whether the session will be returned from the url */
  private boolean requestedSessionIdFromURL = false;

  /**
   * Create a new MockServletRequest with a default {@link MockServletContext}.
   * 
   * @see MockServletContext
   */
  public MockHttpServletRequest() {
    this(null, "", "");
  }

  /**
   * Create a new MockServletRequest with a default {@link MockServletContext}.
   * 
   * @param method
   *          the request method such as <code>GET</code> or <code>POST</code> (may be <code>null</code>)
   * @param requestURI
   *          the request URI (may be <code>null</code>)
   * @see #setMethod
   * @see #setRequestURI
   * @see MockServletContext
   */
  public MockHttpServletRequest(String method, String requestURI) {
    this(null, method, requestURI);
  }

  /**
   * Create a new MockServletRequest.
   * 
   * @param servletContext
   *          the ServletContext that the request runs in (may be
   *          <code>null</code> to use a default MockServletContext)
   * @see MockServletContext
   */
  public MockHttpServletRequest(ServletContext servletContext) {
    this(servletContext, "", "");
  }

  /**
   * Create a new MockServletRequest.
   * 
   * @param servletContext
   *          the ServletContext that the request runs in (may be
   *          <code>null</code> to use a default MockServletContext)
   * @param method
   *          the request method such as <code>GET</code> or <code>POST</code> (may be <code>null</code>)
   * @param requestURI
   *          the request URI (may be <code>null</code>)
   * @see #setMethod
   * @see #setRequestURI
   * @see MockServletContext
   */
  public MockHttpServletRequest(ServletContext servletContext, String method,
      String requestURI) {
    this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
    this.method = method;
    this.requestURI = requestURI;
    this.locales.add(Locale.ENGLISH);
  }

  // ---------------------------------------------------------------------
  // Lifecycle methods
  // ---------------------------------------------------------------------

  /**
   * Return the ServletContext that this request is associated with. (Not
   * available in the standard HttpServletRequest interface for some reason.)
   */
  public ServletContext getServletContext() {
    return this.servletContext;
  }

  /**
   * Return whether this request is still active (that is, not completed yet).
   */
  public boolean isActive() {
    return this.active;
  }

  /**
   * Mark this request as completed, keeping its state.
   */
  public void close() {
    this.active = false;
  }

  /**
   * Invalidate this request, clearing its state.
   */
  public void invalidate() {
    close();
    clearAttributes();
  }

  /**
   * Check whether this request is still active (that is, not completed yet),
   * throwing an IllegalStateException if not active anymore.
   */
  protected void checkActive() throws IllegalStateException {
    if (!this.active) {
      throw new IllegalStateException("Request is not active anymore");
    }
  }

  @Override
  public Object getAttribute(String name) {
    checkActive();
    return this.attributes.get(name);
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    checkActive();
    return this.attributes.keys();
  }

  @Override
  public String getCharacterEncoding() {
    return this.characterEncoding;
  }

  @Override
  public void setCharacterEncoding(String characterEncoding) {
    this.characterEncoding = characterEncoding;
  }

  /**
   * Sets the request body.
   * 
   * @param content
   *          the request content
   */
  public void setContent(byte[] content) {
    this.content = content;
  }

  @Override
  public int getContentLength() {
    return (this.content != null ? this.content.length : -1);
  }

  @Override
  public long getContentLengthLong() {
    return (this.content != null ? this.content.length : -1);
  }

  /**
   * Sets the request content type.
   * 
   * @param contentType
   *          the content type
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  @Override
  public String getContentType() {
    return this.contentType;
  }

  @Override
  public ServletInputStream getInputStream() {
    if (this.content != null) {
      return new DelegatingServletInputStream(new ByteArrayInputStream(this.content));
    } else {
      return null;
    }
  }

  /**
   * Set a single value for the specified HTTP parameter.
   * <p>
   * If there are already one or more values registered for the given parameter
   * name, they will be replaced.
   */
  public void setParameter(String name, String value) {
    setParameter(name, new String[] { value });
  }

  /**
   * Set an array of values for the specified HTTP parameter.
   * <p>
   * If there are already one or more values registered for the given parameter
   * name, they will be replaced.
   */
  public void setParameter(String name, String[] values) {
    if (name == null)
      throw new IllegalArgumentException("Parameter name must not be null");
    this.parameters.put(name, values);
  }

  /**
   * Sets all provided parameters <emphasis>replacing any existing values for
   * the provided parameter names. To add without replacing existing values, use
   * {@link #addParameters(Map)}.
   */
  public void setParameters(Map<String, ?> params) {
    if (params == null)
      throw new IllegalArgumentException("Parameter map must not be null");
    for (Iterator<String> it = params.keySet().iterator(); it.hasNext();) {
      String key = it.next();
      Object value = params.get(key);
      if (value instanceof String) {
        this.setParameter(key, (String) value);
      } else if (value instanceof String[]) {
        this.setParameter(key, (String[]) value);
      } else {
        throw new IllegalArgumentException("Parameter map value must be single value " + " or array of type [" + String.class.getName() + "]");
      }
    }
  }

  /**
   * Add a single value for the specified HTTP parameter.
   * <p>
   * If there are already one or more values registered for the given parameter
   * name, the given value will be added to the end of the list.
   */
  public void addParameter(String name, String value) {
    addParameter(name, new String[] { value });
  }

  /**
   * Add an array of values for the specified HTTP parameter.
   * <p>
   * If there are already one or more values registered for the given parameter
   * name, the given values will be added to the end of the list.
   */
  public void addParameter(String name, String[] values) {
    if (name == null)
      throw new IllegalArgumentException("Parameter name must not be null");
    String[] oldArr = this.parameters.get(name);
    if (oldArr != null) {
      String[] newArr = new String[oldArr.length + values.length];
      System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
      System.arraycopy(values, 0, newArr, oldArr.length, values.length);
      this.parameters.put(name, newArr);
    } else {
      this.parameters.put(name, values);
    }
  }

  /**
   * Adds all provided parameters <emphasis>without</emphasis> replacing any
   * existing values. To replace existing values, use
   * {@link #setParameters(Map)}.
   */
  public void addParameters(Map<String, ?> params) {
    if (params == null)
      throw new IllegalArgumentException("Parameter map must not be null");
    for (Iterator<String> it = params.keySet().iterator(); it.hasNext();) {
      String key = it.next();
      Object value = params.get(key);
      if (value instanceof String) {
        this.addParameter(key, (String) value);
      } else if (value instanceof String[]) {
        this.addParameter(key, (String[]) value);
      } else {
        throw new IllegalArgumentException("Parameter map value must be single value " + " or array of type [" + String.class.getName() + "]");
      }
    }
  }

  /**
   * Remove already registered values for the specified HTTP parameter, if any.
   */
  public void removeParameter(String name) {
    if (name == null)
      throw new IllegalArgumentException("Parameter name must not be null");
    this.parameters.remove(name);
  }

  /**
   * Removes all existing parameters.
   */
  public void removeAllParameters() {
    this.parameters.clear();
  }

  @Override
  public String getParameter(String name) {
    if (name == null)
      throw new IllegalArgumentException("Parameter name must not be null");
    String[] arr = this.parameters.get(name);
    return (arr != null && arr.length > 0 ? arr[0] : null);
  }

  @Override
  public Enumeration<String> getParameterNames() {
    return Collections.enumeration(this.parameters.keySet());
  }

  @Override
  public String[] getParameterValues(String name) {
    if (name == null)
      throw new IllegalArgumentException("Parameter name must not be null");
    return this.parameters.get(name);
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    return Collections.unmodifiableMap(this.parameters);
  }

  /**
   * Sets the request protocol to a value different from the default value of
   * <code>http</code>.
   * 
   * @param protocol
   *          the protocol
   */
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  @Override
  public String getProtocol() {
    return this.protocol;
  }

  /**
   * Sets the request scheme to a value different from the default value of
   * <code></code>.
   * 
   * @param scheme
   */
  public void setScheme(String scheme) {
    this.scheme = scheme;
  }

  public String getScheme() {
    return this.scheme;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  public String getServerName() {
    return this.serverName;
  }

  /**
   * Sets the server port to a value different from the default value of
   * <code>80</code>.
   * 
   * @param serverPort
   *          the server port number
   */
  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  public int getServerPort() {
    return this.serverPort;
  }

  public BufferedReader getReader() throws UnsupportedEncodingException {
    if (this.content != null) {
      InputStream sourceStream = new ByteArrayInputStream(this.content);
      Reader sourceReader = (this.characterEncoding != null) ? new InputStreamReader(sourceStream, this.characterEncoding) : new InputStreamReader(sourceStream);
      return new BufferedReader(sourceReader);
    } else {
      return null;
    }
  }

  public void setRemoteAddr(String remoteAddr) {
    this.remoteAddr = remoteAddr;
  }

  public String getRemoteAddr() {
    return this.remoteAddr;
  }

  public void setRemoteHost(String remoteHost) {
    this.remoteHost = remoteHost;
  }

  public String getRemoteHost() {
    return this.remoteHost;
  }

  public void setAttribute(String name, Object value) {
    checkActive();
    if (name == null)
      throw new IllegalArgumentException("Attribute name must not be null");
    if (value != null) {
      this.attributes.put(name, value);
    } else {
      this.attributes.remove(name);
    }
  }

  public void removeAttribute(String name) {
    checkActive();
    if (name == null)
      throw new IllegalArgumentException("Attribute name must not be null");
    this.attributes.remove(name);
  }

  /**
   * Clear all of this request's attributes.
   */
  public void clearAttributes() {
    this.attributes.clear();
  }

  /**
   * Add a new preferred locale, before any existing locales.
   */
  public void addPreferredLocale(Locale locale) {
    if (locale == null)
      throw new IllegalArgumentException("Locale must not be null");
    this.locales.add(0, locale);
  }

  @Override
  public Locale getLocale() {
    return this.locales.get(0);
  }

  @Override
  public Enumeration<Locale> getLocales() {
    return this.locales.elements();
  }

  /**
   * Sets the request security.
   * 
   * @param secure
   *          <code>true</code> to make this a secure request
   */
  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  @Override
  public boolean isSecure() {
    return this.secure;
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String path) {
    return new MockRequestDispatcher(path);
  }

  @Override
  public String getRealPath(String path) {
    return this.servletContext.getRealPath(path);
  }

  /**
   * Sets the remote port number to a value different from the default value of
   * <code>80</code>.
   * 
   * @param remotePort
   *          the remote port number
   */
  public void setRemotePort(int remotePort) {
    this.remotePort = remotePort;
  }

  @Override
  public int getRemotePort() {
    return this.remotePort;
  }

  /**
   * Sets the local server name to a value different from the default value of
   * <code>localhost</code>.
   * 
   * @param localName
   *          the local server name
   */
  public void setLocalName(String localName) {
    this.localName = localName;
  }

  @Override
  public String getLocalName() {
    return this.localName;
  }

  /**
   * Sets the local address to a value different from the default value of
   * <code>127.0.0.1</code>.
   * 
   * @param localAddr
   *          the local server address
   */
  public void setLocalAddr(String localAddr) {
    this.localAddr = localAddr;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletRequest#getLocalAddr()
   */
  public String getLocalAddr() {
    return this.localAddr;
  }

  /**
   * Sets the local port number to a value different from the default value of
   * <code>80</code>.
   * 
   * @param localPort
   *          the local port number
   */
  public void setLocalPort(int localPort) {
    this.localPort = localPort;
  }

  @Override
  public int getLocalPort() {
    return this.localPort;
  }

  /**
   * Sets the authentication type.
   * 
   * @param authType
   *          the authentication type
   */
  public void setAuthType(String authType) {
    this.authType = authType;
  }

  @Override
  public String getAuthType() {
    return this.authType;
  }

  /**
   * Sets the request cookies. Pass <code>null</code> to remove all cookies.
   * 
   * @param cookies
   *          the cookies
   */
  public void setCookies(Cookie[] cookies) {
    this.cookies = cookies;
  }

  @Override
  public Cookie[] getCookies() {
    return this.cookies;
  }

  /**
   * Add a header entry for the given name.
   * <p>
   * If there was no entry for that header name before, the value will be used
   * as-is. In case of an existing entry, a String array will be created, adding
   * the given value (more specifically, its toString representation) as further
   * element.
   * <p>
   * Multiple values can only be stored as list of Strings, following the
   * Servlet spec (see <code>getHeaders accessor).
   * As alternative to repeated <code>addHeader calls for
   * individual elements, you can use a single call with an entire
   * array or Collection of values as parameter.
   * 
   * @see #getHeaderNames
   * @see #getHeader
   * @see #getHeaders
   * @see #getDateHeader
   * @see #getIntHeader
   */
  @SuppressWarnings("unchecked")
  public void addHeader(String name, Object value) {
    HeaderValueCollection header = HeaderValueCollection.getByName(this.headers, name);
    if (value == null)
      throw new IllegalArgumentException("Header value must not be null");
    if (header == null) {
      header = new HeaderValueCollection();
      this.headers.put(name, header);
    }
    if (value instanceof Collection) {
      header.addValues((Collection<String>) value);
    } else if (value.getClass().isArray()) {
      header.addValueArray(value);
    } else {
      header.addValue(value.toString());
    }
  }

  @Override
  public long getDateHeader(String name) {
    HeaderValueCollection header = HeaderValueCollection.getByName(this.headers, name);
    Object value = (header != null ? header.getValue() : null);
    if (value instanceof Date) {
      return ((Date) value).getTime();
    } else if (value instanceof Number) {
      return ((Number) value).longValue();
    } else if (value != null) {
      throw new IllegalArgumentException("Value for header '" + name + "' is neither a Date nor a Number: " + value);
    } else {
      return -1L;
    }
  }

  @Override
  public String getHeader(String name) {
    HeaderValueCollection header = HeaderValueCollection.getByName(this.headers, name);
    return (header != null ? header.getValue().toString() : null);
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    HeaderValueCollection header = HeaderValueCollection.getByName(this.headers, name);
    return Collections.enumeration(header != null ? header.getValues() : Collections.EMPTY_LIST);
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    return this.headers.keys();
  }

  @Override
  public int getIntHeader(String name) {
    HeaderValueCollection header = HeaderValueCollection.getByName(this.headers, name);
    Object value = (header != null ? header.getValue() : null);
    if (value instanceof Number) {
      return ((Number) value).intValue();
    } else if (value instanceof String) {
      return Integer.parseInt((String) value);
    } else if (value != null) {
      throw new NumberFormatException("Value for header '" + name + "' is not a Number: " + value);
    } else {
      return -1;
    }
  }

  /**
   * Sets the request method to a value different from the default value of
   * <code>http</code>.
   * 
   * @param method
   *          the request method
   */
  public void setMethod(String method) {
    this.method = method;
  }

  @Override
  public String getMethod() {
    return this.method;
  }

  /**
   * Sets the full request path starting at the servlet path, thereby ommitting
   * the servlet mountpoint.
   * 
   * @param pathInfo
   *          the path info
   */
  public void setPathInfo(String pathInfo) {
    this.pathInfo = pathInfo;
  }

  @Override
  public String getPathInfo() {
    return this.pathInfo;
  }

  @Override
  public String getPathTranslated() {
    return (this.pathInfo != null ? getRealPath(this.pathInfo) : null);
  }

  /**
   * Sets the context path. For servlets in the default (root) context, this is
   * set to "".
   * 
   * @param contextPath
   *          the context path
   */
  public void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }

  @Override
  public String getContextPath() {
    return this.contextPath;
  }

  /**
   * Sets the query string.
   * 
   * @param queryString
   *          the query string
   */
  public void setQueryString(String queryString) {
    this.queryString = queryString;
  }

  @Override
  public String getQueryString() {
    return this.queryString;
  }

  /**
   * Sets the login of the remote user. Pass <code>null</code> to indicate a
   * user that is not authenticated.
   * 
   * @param remoteUser
   *          the remote user
   */
  public void setRemoteUser(String remoteUser) {
    this.remoteUser = remoteUser;
  }

  @Override
  public String getRemoteUser() {
    return this.remoteUser;
  }

  /**
   * Adds a role for the current user.
   * 
   * @param role
   *          the role
   */
  public void addUserRole(String role) {
    this.userRoles.add(role);
  }

  @Override
  public boolean isUserInRole(String role) {
    return this.userRoles.contains(role);
  }

  /**
   * Sets the user principal containing the name of the authenticated user.
   * 
   * @param userPrincipal
   *          the user principal
   */
  public void setUserPrincipal(Principal userPrincipal) {
    this.userPrincipal = userPrincipal;
  }

  @Override
  public Principal getUserPrincipal() {
    return this.userPrincipal;
  }

  @Override
  public String getRequestedSessionId() {
    HttpSession session = getSession();
    return (session != null ? session.getId() : null);
  }

  /**
   * Sets the request uri. The request uri contains the full path, starting from
   * the first slash after the server name and port number and ends right before
   * the query string.
   * 
   * @param requestURI
   *          the request uri
   */
  public void setRequestURI(String requestURI) {
    this.requestURI = requestURI;
  }

  @Override
  public String getRequestURI() {
    return this.requestURI;
  }

  @Override
  public StringBuffer getRequestURL() {
    StringBuffer url = new StringBuffer(this.scheme);
    url.append("://").append(this.serverName).append(':').append(this.serverPort);
    url.append(getRequestURI());
    return url;
  }

  /**
   * Sets the servlet path which is the path that was used to match the servlet.
   * If the servlet was matched using <code>/*</code>, the servlet path should
   * be <code>""</code>.
   * 
   * @param servletPath
   *          the servlet path
   */
  public void setServletPath(String servletPath) {
    this.servletPath = servletPath;
  }

  @Override
  public String getServletPath() {
    return this.servletPath;
  }

  /**
   * Sets the Http session.
   * 
   * @param session
   *          the session.
   */
  public void setSession(HttpSession session) {
    this.session = session;
    if (session instanceof MockHttpSession) {
      MockHttpSession mockSession = ((MockHttpSession) session);
      mockSession.access();
    }
  }

  @Override
  public HttpSession getSession(boolean create) {
    checkActive();
    // Reset session if invalidated.
    if (this.session instanceof MockHttpSession && ((MockHttpSession) this.session).isInvalid()) {
      this.session = null;
    }
    // Create new session if necessary.
    if (this.session == null && create) {
      this.session = new MockHttpSession(this.servletContext);
    }
    return this.session;
  }

  @Override
  public HttpSession getSession() {
    return getSession(true);
  }

  /**
   * Sets the validity of the request session.
   * 
   * @param requestedSessionIdValid
   *          the session validity
   */
  public void setRequestedSessionIdValid(boolean requestedSessionIdValid) {
    this.requestedSessionIdValid = requestedSessionIdValid;
  }

  @Override
  public boolean isRequestedSessionIdValid() {
    return this.requestedSessionIdValid;
  }

  /**
   * Sets whether the requested session id is to be determined from the cookies
   * rather than from the request url.
   * 
   * @param requestedSessionIdFromCookie
   *          <code>true</code> to gather a session id from the cookies
   */
  public void setRequestedSessionIdFromCookie(
      boolean requestedSessionIdFromCookie) {
    this.requestedSessionIdFromCookie = requestedSessionIdFromCookie;
  }

  @Override
  public boolean isRequestedSessionIdFromCookie() {
    return this.requestedSessionIdFromCookie;
  }

  /**
   * Sets whether the requested session id is to be determined from the query
   * string of the url rather than from the cookies.
   * 
   * @param requestedSessionIdFromURL
   *          <code>true</code> to gather a session id from the query string
   */
  public void setRequestedSessionIdFromURL(boolean requestedSessionIdFromURL) {
    this.requestedSessionIdFromURL = requestedSessionIdFromURL;
  }

  @Override
  public boolean isRequestedSessionIdFromURL() {
    return this.requestedSessionIdFromURL;
  }

  @Override
  public boolean isRequestedSessionIdFromUrl() {
    return isRequestedSessionIdFromURL();
  }

  @Override
  public AsyncContext startAsync() throws IllegalStateException {
    throw new UnsupportedOperationException("NIO not supported yet.");
  }

  @Override
  public AsyncContext startAsync(ServletRequest servletRequest,
      ServletResponse servletResponse) throws IllegalStateException {
    throw new UnsupportedOperationException("NIO not supported yet.");
  }

  @Override
  public boolean isAsyncStarted() {
    throw new UnsupportedOperationException("NIO not supported yet.");
  }

  @Override
  public boolean isAsyncSupported() {
    return false;
  }

  @Override
  public AsyncContext getAsyncContext() {
    throw new UnsupportedOperationException("NIO not supported yet.");
  }

  @Override
  public DispatcherType getDispatcherType() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String changeSessionId() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean authenticate(HttpServletResponse response)
      throws IOException, ServletException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void login(String username, String password) throws ServletException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void logout() throws ServletException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Collection<Part> getParts() throws IOException, ServletException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Part getPart(String name) throws IOException, ServletException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass)
      throws IOException, ServletException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}