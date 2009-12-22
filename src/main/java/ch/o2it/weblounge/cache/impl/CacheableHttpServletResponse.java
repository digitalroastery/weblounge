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

import ch.o2it.weblounge.cache.impl.filter.FilterWriter;
import ch.o2it.weblounge.common.request.CacheHandle;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Implementation of a <code>HttpServletResponseWrapper</code> that allows for
 * response caching by installing a custom version off an output stream which
 * works like the <code>tee</code> command in un*x systems. Like this, the
 * output can be written to the response cache <i>and</i> to the client at the
 * same time.
 */
class CacheableHttpServletResponse extends HttpServletResponseWrapper {

  /**
   * holds the special tee writer that copies the output to the network and to
   * the cache.
   */
  private PrintWriter out = null;

  /** the character encoding of this reply. */
  private String encoding = null;

  /** the cached transaction for this page */
  CacheTransaction tx = null;

  /** the format used for date headers */
  private DateFormat format = null;

  /** the content type */
  private String contentType = null;

  /** whether the getOuputStream has already been called */
  private boolean osCalled = false;

  /**
   * Creates a <code>CacheableHttpServletResponse</code> using the given
   * cacheWriter stream to write the output to the cache.
   * 
   * @param tx
   *          the cached transaction represented by this cacheable response
   */
  CacheableHttpServletResponse(CacheTransaction tx) {
    super(tx.resp);

    /* the cached transaction for this page */
    this.tx = tx;
    cacheMiss(tx.hnd);
  }

  /**
   * Returns the modified writer that enables the <code>CacheManager</cache>
	 * to copy the response to the cache.
   * 
   * @return a PrintWriter object that can return character data to the client
   * @throws IOException
   *           if the writer could not be allocated
   * @see javax.servlet.ServletResponse#getWriter()
   * @see ch.o2it.weblounge.service.cache.CacheManager
   */
  @Override
  public PrintWriter getWriter() throws IOException {
    /* check whether there's already a writer allocated */
    if (out != null)
      return out;

    /* check whether getOutputStream() has already been called */
    if (osCalled)
      throw new IllegalStateException("getOutputStream() has already been called");

    /* get the character encoding */
    encoding = getCharacterEncoding();
    if (encoding == null)
      encoding = CacheManager.DEFAULT_ENCODING;

    /* allocate a new writer */
    try {
      if (tx.filter == null)
        out = new PrintWriter(new OutputStreamWriter(tx.os, encoding));
      else
        out = new PrintWriter(new BufferedWriter(new FilterWriter(new OutputStreamWriter(tx.os, encoding), tx.filter, contentType)));
    } catch (UnsupportedEncodingException e) {
      throw new IOException(e.getMessage());
    }

    /* check whether the new writer is usable */
    if (out == null)
      throw new IOException("unable to allocate writer");

    /* return the new writer */
    return out;
  }

  /**
   * @see javax.servlet.ServletResponseWrapper#getOutputStream()
   */
  @Override
  public ServletOutputStream getOutputStream() {
    if (out != null)
      throw new IllegalStateException("getWriter() has already been called");
    osCalled = true;
    return tx.os;
  }

  /**
   * Signals a cache hit for the given handle.
   * 
   * @param hnd
   *          the handle that produced the cache hit
   * @param buf
   *          the content from the cache
   */
  void cacheHit(CacheHandle hnd, byte buf[]) {
    if (out != null)
      out.flush();
    tx.os.newEntry(hnd, true);
    try {
      tx.os.write(buf);
    } catch (IOException e) {
      /* this will never happen! */
    }
    tx.os.endEntry(hnd);
  }

  /**
   * Signals a cache miss for the given handle.
   * 
   * @param hnd
   *          the handle that produced a cache miss
   */
  void cacheMiss(CacheHandle hnd) {
    if (out != null)
      out.flush();
    tx.os.newEntry(hnd, false);
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
    tx.os.endEntry(hnd);

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
    tx.os.endOutput(tx.meta);
    return tx;
  }

  /**
   * Invalidate the output. Tells the cache writer to stop adding output to the
   * cache.
   */
  void invalidateOutput() {
    tx.os.invalidateOutput();
    tx.invalidated = true;
  }

  /**
   * Returns <code>true</code> if the response has been invalidated.
   * 
   * @return <code>true</code> if the response has been invalidated
   */
  public boolean isInvalidated() {
    return tx.invalidated;
  }

  /**
   * @see javax.servlet.ServletResponse#setContentType(String)
   */
  @Override
  public void setContentType(String type) {
    super.setContentType(type);
    contentType = type;
    tx.meta.contentType = type;

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
    tx.meta.addHeader(name, value);
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void setHeader(String name, String value) {
    super.setHeader(name, value);
    tx.meta.setHeader(name, value);
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String,
   *      long)
   */
  @Override
  public void addDateHeader(String name, long date) {
    super.addDateHeader(name, date);
    tx.meta.addHeader(name, formatDate(date));
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String,
   *      int)
   */
  @Override
  public void addIntHeader(String name, int value) {
    super.addIntHeader(name, value);
    tx.meta.addHeader(name, "" + value);
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String,
   *      long)
   */
  @Override
  public void setDateHeader(String name, long date) {
    super.setDateHeader(name, date);
    tx.meta.setHeader(name, formatDate(date));
  }

  /**
   * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String,
   *      int)
   */
  @Override
  public void setIntHeader(String name, int value) {
    super.setIntHeader(name, value);
    tx.meta.setHeader(name, "" + value);
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
