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

package ch.entwine.weblounge.kernel.publisher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

/**
 * This servlet output stream keeps everything that is written to it in a byte
 * array for later reference.
 */
public class ByteArrayServletOutputStream extends ServletOutputStream {

  /** The output stream */
  private ByteArrayOutputStream outputStream = null;

  /**
   * Creates a new output stream.
   */
  public ByteArrayServletOutputStream() {
    outputStream = new ByteArrayOutputStream();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.io.OutputStream#write(int)
   */
  @Override
  public void write(int b) throws IOException {
    outputStream.write(b);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.io.OutputStream#write(byte[])
   */
  @Override
  public void write(byte[] b) throws IOException {
    outputStream.write(b);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.io.OutputStream#write(byte[], int, int)
   */
  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    outputStream.write(b, off, len);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.io.OutputStream#close()
   */
  @Override
  public void close() throws IOException {
    outputStream.close();
  }

  /**
   * Returns the bytes that have been written to this output stream.
   * 
   * @return the output stream's content
   */
  public byte[] getBytes() {
    return outputStream.toByteArray();
  }

}