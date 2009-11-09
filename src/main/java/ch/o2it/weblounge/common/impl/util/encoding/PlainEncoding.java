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

package ch.o2it.weblounge.common.impl.util.encoding;

/**
 * The PlainEncoding is a dummy implementation of an encoding, which returns the
 * text just as it has been entered.
 */
public class PlainEncoding implements Encoding {

  /** The singleton instance */
  private static PlainEncoding instance_ = null;

  /**
   * Creates a new instance of PlainEncoding.
   */
  private PlainEncoding() {
  }

  public static PlainEncoding getInstance() {
    if (instance_ == null) {
      instance_ = new PlainEncoding();
    }
    return instance_;
  }

  /**
   * Encodes a text such that the result is the same text as entered (plain
   * text).
   * 
   * @param text
   *          the element to be encoded
   * @return the encoded text
   */
  public String encode(String text) {
    return text;
  }

}