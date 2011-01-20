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

package ch.o2it.weblounge.cache.impl;

import ch.o2it.weblounge.cache.StreamFilter;
import ch.o2it.weblounge.cache.impl.filter.FilterWriter;
import ch.o2it.weblounge.common.request.CacheHandle;

import org.apache.commons.io.output.TeeOutputStream;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Implementation of a <code>HttpServletResponseWrapper</code> that allows for
 * response caching by installing a custom version of an output stream which
 * works like the <code>tee</code> command in un*x systems. Like this, the
 * output can be written to the response cache <i>and</i> to the client at the
 * same time.
 */
class CacheableHttpServletResponse extends HttpServletResponseWrapper {

  /**
   * Holds the special tee writer that copies the output to the network and to
   * the cache.
   */
  private PrintWriter out = null;

  /** The character encoding of this reply. */
  private String encoding = null;

  /** The cache transaction for this response */
  private CacheTransaction tx = null;

  /** The format used for date headers */
  private DateFormat format = null;

  /** The content type */
  private String contentType = null;

  /** Whether the getOuputStream has already been called */
  private boolean osCalled = false;

  /** Default encoding */
  private static final String DEFAULT_ENCODING = "utf-8";

  /**
   * Creates a <code>CacheableHttpServletResponse</code> that is writing any
   * content to the wrapped response as well as to the cached output stream,
   * given a preceding call to {@link #startTransaction(CacheTransaction)}.
   * 
   * @param tx
   *          the cached transaction represented by this cacheable response
   */
  CacheableHttpServletResponse(HttpServletResponse response) {
    super(response);
  }

  /**
   * Starts a cache transaction.
   * 
   * @param handle
   *          the cache handle
   * @param filter
   *          the stream filter
   * @return the transaction
   */
  public CacheTransaction startTransaction(CacheHandle handle,
      StreamFilter filter) {
    tx = new CacheTransaction(handle, filter);
    return tx;
  }

  /**
   * Returns the modified writer that enables the <code>CacheManager</cache>
   * to copy the response to the cache.
   * 
   * @return a PrintWriter object that can return character data to the client
   * @throws IOException
   *           if the writer could not be allocated
   * @see javax.servlet.ServletResponse#getWriter()
   * @see ch.o2it.weblounge.OldCacheManager.cache.CacheManager
   */
  @Override
  public PrintWriter getWriter() throws IOException {
    // Check whether there's already a writer allocated
    if (out != null)
      return out;

    // Check whether getOutputStream() has already been called
    if (osCalled)
      throw new IllegalStateException("An output stream has already been allocated");

    // Get the character encoding
    encoding = getCharacterEncoding();
    if (encoding == null)
      encoding = DEFAULT_ENCODING;

    // Allocate a new writer. If there is a transaction, the output is written
    // to both the original response and the cache output stream.
    OutputStream os = null;
    if (tx != null)
      os = new TeeOutputStream(getResponse().getOutputStream(), tx.getOutputStream());
    else
      os = getResponse().getOutputStream();

    // Install the writer
    try {
      if (tx == null || tx.getFilter() == null)
        out = new PrintWriter(new OutputStreamWriter(os, encoding));
      else
        out = new PrintWriter(new BufferedWriter(new FilterWriter(new OutputStreamWriter(os, encoding), tx.getFilter(), contentType)));
    } catch (UnsupportedEncodingException e) {
      throw new IOException(e.getMessage());
    }

    // Check whether the new writer is usable
    if (out == null)
      throw new IOException("Unable to allocate writer");

    // Return the new writer
    return out;
  }

  /**
   * @see javax.servlet.ServletResponseWrapper#getOutputStream()
   */
  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    if (out != null)
      throw new IllegalStateException("A writer has already been allocated");
    osCalled = true;
    return tx != null ? tx.getOutputStream() : getResponse().getOutputStream();
  }

  /**
   * Signals the end of a cache entry.
   * 
   * @param hnd
   *          the handle to end
   */
  void endEntry(CacheHandle hnd) {
    if (out != null)
      out.flush();
  }

  /**
   * Signals that the page display is finished and flushes the buffer.
   * 
   * @return the cached transaction for this page
   */
  CacheTransaction endOutput() {
    if (out != null) {
      out.flush();
      out.close();
      out = null;
    }
    return tx;
  }

  /**
   * Invalidate the output. Tells the cache writer to stop adding output to the
   * cache.
   */
  void invalidate() {
    if (tx != null)
      tx.invalidate();
  }

  /**
   * Returns <code>true</code> if the response has been invalidated.
   * 
   * @return <code>true</code> if the response has been invalidated
   */
  public boolean isValid() {
    return tx != null ? tx.isValid() : false;
  }

  /**
   * Returns the active cache transaction.
   * 
   * @return the transaction
   */
  public CacheTransaction getTransaction() {
    return tx;
  }

  /**
   * @see javax.servlet.ServletResponse#setContentType(String)
   */
  @Override
  public void setContentType(String type) {
    super.setContentType(type);
    contentType = type;
    if (tx != null)
      tx.getHeaders().setHeader("Content-Type", type);

    /* check whether the encoding has changed */
    if (encoding == null || !encoding.equals(getCharacterEncoding())) {
      if (out != null) {
        out.flush();
        out = null;
      }
    }
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void addHeader(String name, String value) {
    super.addHeader(name, value);
    if (tx != null)
      tx.getHeaders().addHeader(name, value);
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void setHeader(String name, String value) {
    super.setHeader(name, value);
    if (tx != null)
      tx.getHeaders().setHeader(name, value);
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String,
   *      long)
   */
  @Override
  public void addDateHeader(String name, long date) {
    super.addDateHeader(name, date);
    if (tx != null)
      tx.getHeaders().addHeader(name, formatDate(date));
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String,
   *      int)
   */
  @Override
  public void addIntHeader(String name, int value) {
    super.addIntHeader(name, value);
    tx.getHeaders().addHeader(name, Integer.toString(value));
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String,
   *      long)
   */
  @Override
  public void setDateHeader(String name, long date) {
    super.setDateHeader(name, date);
    if (tx != null)
      tx.getHeaders().setHeader(name, formatDate(date));
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String,
   *      int)
   */
  @Override
  public void setIntHeader(String name, int value) {
    super.setIntHeader(name, value);
    if (tx != null)
      tx.getHeaders().setHeader(name, Integer.toString(value));
  }

  /**
   * Format the date for an HTTP header. The resulting date will match the
   * following example:
   * 
   * <pre>
   * EEE, dd MMM yyyy HH:mm:ss 'GMT'
   * </pre>
   * 
   * @param date
   *          the date to format
   * @return the formatted date
   */
  private String formatDate(long date) {
    if (format == null) {
      format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
      format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    return format.format(new Date(date));
  }

}
