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

package ch.entwine.weblounge.common.impl.request;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

/**
 * Delegating implementation of {@link javax.servlet.ServletOutputStream} that
 * will pass bytes written to it on to the enclosed target output stream.
 */
public class DelegatingServletOutputStream extends ServletOutputStream {

  /** Target output stream */
  private final OutputStream targetStream;

  /**
   * Create a DelegatingServletOutputStream for the given target stream.
   * 
   * @param targetStream
   *          the target stream (never <code>null)
   */
  public DelegatingServletOutputStream(OutputStream targetStream) {
    if (targetStream == null)
      throw new IllegalArgumentException("Target OutputStream must not be null");
    this.targetStream = targetStream;
  }

  /**
   * Return the underlying target stream (never <code>null).
   */
  public final OutputStream getTargetStream() {
    return this.targetStream;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.io.OutputStream#write(int)
   */
  public void write(int b) throws IOException {
    this.targetStream.write(b);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.io.OutputStream#flush()
   */
  public void flush() throws IOException {
    super.flush();
    this.targetStream.flush();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.io.OutputStream#close()
   */
  public void close() throws IOException {
    super.close();
    this.targetStream.close();
  }

}
