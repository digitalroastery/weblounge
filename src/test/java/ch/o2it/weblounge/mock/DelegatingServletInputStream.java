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

package ch.o2it.weblounge.mock;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

/**
 * Delegating implementation of {@link javax.servlet.ServletInputStream}.
 */
public class DelegatingServletInputStream extends ServletInputStream {

  /** The underlying input stream */
  private final InputStream sourceStream;

  /**
   * Create a DelegatingServletInputStream for the given source stream.
   * 
   * @param sourceStream
   *          the source stream (never <code>null)
   */
  public DelegatingServletInputStream(InputStream sourceStream) {
    assertNotNull("Source InputStream must not be null", sourceStream);
    this.sourceStream = sourceStream;
  }

  /**
   * Return the underlying source stream.
   * 
   * @return the underlying source stream
   */
  public final InputStream getSourceStream() {
    return this.sourceStream;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.io.InputStream#read()
   */
  public int read() throws IOException {
    return this.sourceStream.read();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.io.InputStream#close()
   */
  public void close() throws IOException {
    super.close();
    this.sourceStream.close();
  }

}
