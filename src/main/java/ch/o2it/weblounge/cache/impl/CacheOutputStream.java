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

import javax.servlet.ServletOutputStream;

/**
 * Extension to the <code>ServletOutputStream</code> that allows to copy the
 * output that has been written to a <code>HtttpServletResponse</code> to the
 * response cache as well as to the client.
 */
final class CacheOutputStream extends ServletOutputStream {

  /** Default buffer size for the response */
  private static final int BUFFER_SIZE = 20 * 1024;

  /** Output buffer */
  private byte[] buf = new byte[BUFFER_SIZE];

  /** Write position in the output buffer */
  private int pos = 0;

  /**
   * {@inheritDoc}
   * 
   * @see java.io.OutputStream#write(int)
   */
  @Override
  public synchronized void write(int b) {
    int newpos = pos + 1;
    if (newpos > buf.length)
      extendBuffer(newpos);
    buf[pos] = (byte) b;
    pos = newpos;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.io.OutputStream#write(byte[], int, int)
   */
  @Override
  public synchronized void write(byte[] b, int off, int len) {
    int newpos = pos + len;
    if (newpos > buf.length)
      extendBuffer(newpos);
    System.arraycopy(b, off, buf, pos, len);
    pos = newpos;
  }

  /**
   * Extend the buffer to at least <code>size</code> bytes.
   * 
   * @param size
   *          the minimum length of the new buffer
   */
  private void extendBuffer(int size) {
    int goal = buf.length << 1;
    byte[] newbuf = new byte[goal > size ? goal : size];
    System.arraycopy(buf, 0, newbuf, 0, pos);
    buf = newbuf;
  }

  /**
   * Returns the content of this stream as a byte array.
   * 
   * @return the content
   */
  byte[] getContent() {
    byte[] newbuf = new byte[pos];
    System.arraycopy(buf, 0, newbuf, 0, pos);
    return newbuf;
  }

}