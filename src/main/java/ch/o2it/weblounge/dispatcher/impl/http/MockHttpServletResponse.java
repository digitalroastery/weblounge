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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Mock implementation of the Servlet 2.4 API
 * {@link javax.servlet.http.HttpServletResponse} interface.
 */
public class MockHttpServletResponse implements HttpServletResponse {

  /** Default http server port */
  public static final int DEFAULT_SERVER_PORT = 80;

  /** Determines the beginning of a character set definition */
  private static final String CHARSET_PREFIX = "charset=";

  /** Determines whether access to the output stream is allowed */
  private boolean outputStreamAccessAllowed = true;

  /** Determines whether writing to the output stream is allowed */
  private boolean writerAccessAllowed = true;

  /** The response character encoding */
  private String characterEncoding = null;

  /** The response */
  private final ByteArrayOutputStream response = new ByteArrayOutputStream();

  /** The servlet output stream */
  private final ServletOutputStream outputStream = new ResponseServletOutputStream(this.response);

  /** Response writer */
  private PrintWriter writer = null;

  /** The content length */
  private int contentLength = 0;

  /** The content mime type */
  private String contentType = null;

  /** Size of the output buffer */
  private int bufferSize = 4096;

  /** Determines whether the response has already been committed */
  private boolean committed = false;

  /** The response locale */
  private Locale locale = Locale.getDefault();

  /** The list of cookies */
  private final List<Cookie> cookies = new ArrayList<Cookie>();

  /** The response headers */
  private final Map<String, HeaderValueCollection> headers = new HashMap<String, HeaderValueCollection>();

  /** The response status, default to {@link HttpServletResponse#SC_OK} */
  private int status = HttpServletResponse.SC_OK;

  /** The error message */
  private String errorMessage = null;

  /** The redirect url, used in conjunction with the mock request dispatcher */
  private String redirectedUrl = null;

  /** The forward url, used in conjunction with the mock request dispatcher */
  private String forwardedUrl = null;

  /** The include url, used in conjunction with the mock request dispatcher */
  private String includedUrl = null;

  /**
   * Set whether {@link #getOutputStream()} access is allowed.
   * <p>
   * Default is true.
   */
  public void setOutputStreamAccessAllowed(boolean outputStreamAccessAllowed) {
    this.outputStreamAccessAllowed = outputStreamAccessAllowed;
  }

  /**
   * Return whether {@link #getOutputStream()} access is allowed.
   */
  public boolean isOutputStreamAccessAllowed() {
    return this.outputStreamAccessAllowed;
  }

  /**
   * Set whether {@link #getWriter()} access is allowed.
   * <p>
   * Default is true.
   */
  public void setWriterAccessAllowed(boolean writerAccessAllowed) {
    this.writerAccessAllowed = writerAccessAllowed;
  }

  /**
   * Return whether {@link #getOutputStream()} access is allowed.
   */
  public boolean isWriterAccessAllowed() {
    return this.writerAccessAllowed;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
   */
  public void setCharacterEncoding(String characterEncoding) {
    this.characterEncoding = characterEncoding;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponse#getCharacterEncoding()
   */
  public String getCharacterEncoding() {
    return this.characterEncoding;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponse#getOutputStream()
   */
  public ServletOutputStream getOutputStream() {
    if (!this.outputStreamAccessAllowed) {
      throw new IllegalStateException("OutputStream access not allowed");
    }
    return this.outputStream;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponse#getWriter()
   */
  public PrintWriter getWriter() throws UnsupportedEncodingException {
    if (!this.writerAccessAllowed) {
      throw new IllegalStateException("Writer access not allowed");
    }
    if (this.writer == null) {
      Writer targetWriter = (this.characterEncoding != null ? new OutputStreamWriter(this.response, this.characterEncoding) : new OutputStreamWriter(this.response));
      this.writer = new ResponsePrintWriter(targetWriter);
    }
    return this.writer;
  }

  /**
   * Returns access to the response as a <code>byte</code> array.
   * 
   * @return the response body as a <code>byte</code> array
   */
  public byte[] getContentAsByteArray() {
    flushBuffer();
    return this.response.toByteArray();
  }

  /**
   * Returns access to the response as a <code>String</code>.
   * 
   * @return the response body as a string
   * @throws UnsupportedEncodingException
   *           if the platform does not support the response encoding
   */
  public String getContentAsString() throws UnsupportedEncodingException {
    flushBuffer();
    return (this.characterEncoding != null) ? this.response.toString(this.characterEncoding) : this.response.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponse#setContentLength(int)
   */
  public void setContentLength(int contentLength) {
    this.contentLength = contentLength;
  }

  /**
   * Returns the value that was set as the content length.
   * 
   * @return the content length
   */
  public int getContentLength() {
    return this.contentLength;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
    if (contentType != null) {
      int charsetIndex = contentType.toLowerCase().indexOf(CHARSET_PREFIX);
      if (charsetIndex != -1) {
        String encoding = contentType.substring(charsetIndex + CHARSET_PREFIX.length());
        setCharacterEncoding(encoding);
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponse#getContentType()
   */
  public String getContentType() {
    return this.contentType;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponse#setBufferSize(int)
   */
  public void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponse#getBufferSize()
   */
  public int getBufferSize() {
    return this.bufferSize;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponse#flushBuffer()
   */
  public void flushBuffer() {
    setCommitted(true);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponse#resetBuffer()
   */
  public void resetBuffer() {
    if (isCommitted()) {
      throw new IllegalStateException("Cannot reset buffer - response is already committed");
    }
    this.response.reset();
  }

  /**
   * Switches the implementation to committing the response once the buffer size
   * has been exceeded.
   */
  private void setCommittedIfBufferSizeExceeded() {
    int bufSize = getBufferSize();
    if (bufSize > 0 && this.response.size() > bufSize) {
      setCommitted(true);
    }
  }

  /**
   * Marks the response as being committed.
   * 
   * @param committed
   *          <code>true</code> to mark the response as being committed
   */
  public void setCommitted(boolean committed) {
    this.committed = committed;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponse#isCommitted()
   */
  public boolean isCommitted() {
    return this.committed;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponse#reset()
   */
  public void reset() {
    resetBuffer();
    this.characterEncoding = null;
    this.contentLength = 0;
    this.contentType = null;
    this.locale = null;
    this.cookies.clear();
    this.headers.clear();
    this.status = HttpServletResponse.SC_OK;
    this.errorMessage = null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
   */
  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponse#getLocale()
   */
  public Locale getLocale() {
    return this.locale;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
   */
  public void addCookie(Cookie cookie) {
    if (cookie == null)
      throw new IllegalArgumentException("Cookie must not be null");
    this.cookies.add(cookie);
  }

  /**
   * Returns the cookies that have been set on the response.
   * 
   * @return the cookies
   */
  public Cookie[] getCookies() {
    return this.cookies.toArray(new Cookie[this.cookies.size()]);
  }

  /**
   * Returns the cookie with name <code>name</code> or <code>null</code> if no
   * such cookie was defined.
   * 
   * @param name
   *          the cookie name
   * @return the cookie
   */
  public Cookie getCookie(String name) {
    if (name == null)
      throw new IllegalArgumentException("Cookie name must not be null");
    for (Iterator<Cookie> it = this.cookies.iterator(); it.hasNext();) {
      Cookie cookie = it.next();
      if (name.equals(cookie.getName())) {
        return cookie;
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
   */
  public boolean containsHeader(String name) {
    return (HeaderValueCollection.getByName(this.headers, name) != null);
  }

  /**
   * Return the names of all specified headers as a Set of Strings.
   * 
   * @return the <code>Set of header name Strings, or an empty Set if none
   */
  public Set<String> getHeaderNames() {
    return this.headers.keySet();
  }

  /**
   * Return the primary value for the given header, if any.
   * <p>
   * Will return the first value in case of multiple values.
   * 
   * @param name
   *          the name of the header
   * @return the associated header value, or <code>null if none
   */
  public Object getHeader(String name) {
    HeaderValueCollection header = HeaderValueCollection.getByName(this.headers, name);
    return (header != null ? header.getValue() : null);
  }

  /**
   * Return all values for the given header as a List of value objects.
   * 
   * @param name
   *          the name of the header
   * @return the associated header values, or an empty List if none
   */
  public List<Object> getHeaderValues(String name) {
    HeaderValueCollection header = HeaderValueCollection.getByName(this.headers, name);
    return (header != null ? header.getValues() : new ArrayList<Object>());
  }

  /**
   * The default implementation returns the given URL String as-is.
   * <p>
   * Can be overridden in subclasses, appending a session id or the like.
   */
  public String encodeURL(String url) {
    return url;
  }

  /**
   * The default implementation delegates to {@link #encodeURL}, returning the
   * given URL String as-is.
   * <p>
   * Can be overridden in subclasses, appending a session id or the like in a
   * redirect-specific fashion. For general URL encoding rules, override the
   * common {@link #encodeURL} method instead, appyling to redirect URLs as well
   * as to general URLs.
   */
  public String encodeRedirectURL(String url) {
    return encodeURL(url);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
   */
  public String encodeUrl(String url) {
    return encodeURL(url);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
   */
  public String encodeRedirectUrl(String url) {
    return encodeRedirectURL(url);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServletResponse#sendError(int,
   *      java.lang.String)
   */
  public void sendError(int status, String errorMessage) throws IOException {
    if (isCommitted()) {
      throw new IllegalStateException("Cannot set error status - response is already committed");
    }
    this.status = status;
    this.errorMessage = errorMessage;
    setCommitted(true);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServletResponse#sendError(int)
   */
  public void sendError(int status) throws IOException {
    if (isCommitted()) {
      throw new IllegalStateException("Cannot set error status - response is already committed");
    }
    this.status = status;
    setCommitted(true);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
   */
  public void sendRedirect(String url) throws IOException {
    if (isCommitted()) {
      throw new IllegalStateException("Cannot send redirect - response is already committed");
    }
    if (url == null)
      throw new IllegalArgumentException("Redirect URL must not be null");
    this.redirectedUrl = url;
    setCommitted(true);
  }

  /**
   * Returns the redirected url.
   * 
   * @return the url
   */
  public String getRedirectedUrl() {
    return this.redirectedUrl;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String,
   *      long)
   */
  public void setDateHeader(String name, long value) {
    setHeaderValue(name, new Long(value));
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String,
   *      long)
   */
  public void addDateHeader(String name, long value) {
    addHeaderValue(name, new Long(value));
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String,
   *      java.lang.String)
   */
  public void setHeader(String name, String value) {
    setHeaderValue(name, value);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String,
   *      java.lang.String)
   */
  public void addHeader(String name, String value) {
    addHeaderValue(name, value);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String,
   *      int)
   */
  public void setIntHeader(String name, int value) {
    setHeaderValue(name, new Integer(value));
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String,
   *      int)
   */
  public void addIntHeader(String name, int value) {
    addHeaderValue(name, new Integer(value));
  }

  /**
   * Sets the value of the response header identified by <code>name</code>.
   * 
   * @param name
   *          the the header name
   * @param value
   *          the header value
   */
  private void setHeaderValue(String name, Object value) {
    doAddHeaderValue(name, value, true);
  }

  /**
   * Adds a value to the list of header values.
   * 
   * @param name
   *          the header name
   * @param value
   *          the additional header value
   */
  private void addHeaderValue(String name, Object value) {
    doAddHeaderValue(name, value, false);
  }

  /**
   * Adds the given value to the response header, optionally replacing any
   * header values that were there previously.
   * 
   * @param name
   *          the header name
   * @param value
   *          the header value
   * @param replace
   *          <code>true</code> to replace existing headers
   */
  private void doAddHeaderValue(String name, Object value, boolean replace) {
    HeaderValueCollection header = HeaderValueCollection.getByName(this.headers, name);
    if (value == null)
      throw new IllegalArgumentException("Header value must not be null");
    if (header == null) {
      header = new HeaderValueCollection();
      this.headers.put(name, header);
    }
    if (replace) {
      header.setValue(value);
    } else {
      header.addValue(value);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServletResponse#setStatus(int)
   */
  public void setStatus(int status) {
    this.status = status;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.http.HttpServletResponse#setStatus(int,
   *      java.lang.String)
   */
  public void setStatus(int status, String errorMessage) {
    this.status = status;
    this.errorMessage = errorMessage;
  }

  /**
   * Returns the response state.
   * 
   * @return the response state
   */
  public int getStatus() {
    return this.status;
  }

  /**
   * Returns the error message or <code>null</code> if no header has been
   * reported so far.
   * 
   * @return the error message
   */
  public String getErrorMessage() {
    return this.errorMessage;
  }

  /**
   * Sets the forwarded url for the mock request dispatcher.
   * 
   * @param forwardedUrl
   *          the forwarded url
   */
  public void setForwardedUrl(String forwardedUrl) {
    this.forwardedUrl = forwardedUrl;
  }

  /**
   * Returns the forwarded url or <code>null</code> if no url has been set so
   * far.
   * 
   * @return the forwarded url
   */
  public String getForwardedUrl() {
    return this.forwardedUrl;
  }

  /**
   * Sets the included url for the mock request dispatcher.
   * 
   * @param includedUrl
   *          the include url
   */
  public void setIncludedUrl(String includedUrl) {
    this.includedUrl = includedUrl;
  }

  /**
   * Returns the included url or <code>null</code> if no url has been set so
   * far.
   * 
   * @return the included url
   */
  public String getIncludedUrl() {
    return this.includedUrl;
  }

  /**
   * Inner class that adapts the ServletOutputStream to mark the response as
   * committed once the buffer size is exceeded.
   */
  private class ResponseServletOutputStream extends DelegatingServletOutputStream {

    public ResponseServletOutputStream(OutputStream out) {
      super(out);
    }

    public void write(int b) throws IOException {
      super.write(b);
      super.flush();
      setCommittedIfBufferSizeExceeded();
    }

    public void flush() throws IOException {
      super.flush();
      setCommitted(true);
    }
  }

  /**
   * Inner class that adapts the PrintWriter to mark the response as committed
   * once the buffer size is exceeded.
   */
  private final class ResponsePrintWriter extends PrintWriter {

    public ResponsePrintWriter(Writer out) {
      super(out, true);
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.PrintWriter#write(char[], int, int)
     */
    public void write(char buf[], int off, int len) {
      super.write(buf, off, len);
      super.flush();
      setCommittedIfBufferSizeExceeded();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.PrintWriter#write(java.lang.String, int, int)
     */
    public void write(String s, int off, int len) {
      super.write(s, off, len);
      super.flush();
      setCommittedIfBufferSizeExceeded();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.PrintWriter#write(int)
     */
    public void write(int c) {
      super.write(c);
      super.flush();
      setCommittedIfBufferSizeExceeded();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.PrintWriter#flush()
     */
    public void flush() {
      super.flush();
      setCommitted(true);
    }
  }

}
