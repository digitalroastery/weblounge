/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.util;

import java.io.OutputStream;

/**
 * The <code>NullOutputStream</code> simply discards all output. It is similar
 * to output redirection to <code>/dev/null</code>.
 * 
 * @version $Revision: 1059 $ $Date: 2009-09-05 02:45:07 +0200 (Sa, 05 Sep 2009)
 *          $
 * @author Daniel Steiner
 */
public class NullOutputStream extends OutputStream {

  /**
   * @see java.io.OutputStream#write(byte[], int, int)
   */
  @Override
  public void write(byte[] b, int off, int len) {
    /* check parameters */
    if (b == null)
      throw new NullPointerException();
    else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0))
      throw new IndexOutOfBoundsException();

    /* discard all output */
  }

  /**
   * @see java.io.OutputStream#write(byte[])
   */
  @Override
  public void write(byte[] b) {
    /* check parameter */
    if (b == null)
      throw new NullPointerException();

    /* discard all output */
  }

  /**
   * @see java.io.OutputStream#write(int)
   */
  @Override
  public void write(int b) {
    /* discard all output */
  }

}
