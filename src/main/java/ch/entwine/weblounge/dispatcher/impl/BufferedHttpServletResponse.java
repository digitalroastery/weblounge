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

package ch.entwine.weblounge.dispatcher.impl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

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
class BufferedHttpServletResponse extends HttpServletResponseWrapper {

  /**
   * Holds the special tee writer that copies the output to the network and to
   * the cache.
   */
  private PrintWriter out = null;

  /** The output stream */
  private BufferedServletOutputStream os = null;

  /** The character encoding of this reply. */
  private String encoding = null;

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
  BufferedHttpServletResponse(HttpServletResponse response) {
    super(response);
    os = new BufferedServletOutputStream();
  }

  /**
   * Returns the modified writer that enables the <code>CacheManager</cache>
   * to copy the response to the cache.
   * 
   * @return a PrintWriter object that can return character data to the client
   * @throws IOException
   *           if the writer could not be allocated
   * @see javax.servlet.ServletResponse#getWriter()
   * @see ch.entwine.weblounge.OldCacheManager.cache.CacheManager
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

    // Install the writer
    try {
      out = new PrintWriter(new OutputStreamWriter(os, encoding));
    } catch (UnsupportedEncodingException e) {
      throw new IOException(e.getMessage());
    }

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
    return os;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponseWrapper#flushBuffer()
   */
  @Override
  public void flushBuffer() throws IOException {
    if (out != null) {
      out.flush();
      out.close();
      out = null;
    }

    try {
      if (isCommitted())
        return;

      // Get the buffered content
      byte[] content = os.getContent();

      // Set content-related headers
      setContentLength(content.length);
      setCharacterEncoding(encoding);

      // Write the buffered content to the underlying output stream
      super.getOutputStream().write(content);

      // Flush the underlying buffer
      super.flushBuffer();

    } finally {
      os = null;
    }
  }

}
