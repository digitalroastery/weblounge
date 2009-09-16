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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResourceLoader {

  // Logging

  /** the class name, used for the loggin facility */
  private final static String className = ResourceLoader.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Reads the specified resource and returns it as a string.
   * 
   * @param resourceUri
   *          the resource location
   * @param the
   *          parent class
   * @param encoding
   *          the encoding used to read the resource
   * @return the resource
   */
  public static String load(String resourceUri, Class parent, String encoding)
      throws FileNotFoundException, IOException {
    InputStream is = parent.getResourceAsStream(resourceUri);
    InputStreamReader isr = null;
    StringBuffer buf = new StringBuffer();
    if (is != null) {
      try {
        log_.debug("Loading resource " + resourceUri);
        if (encoding != null) {
          isr = new InputStreamReader(is, encoding);
        } else {
          isr = new InputStreamReader(is);
        }
        char[] chars = new char[1024];
        int count = 0;
        while ((count = isr.read(chars)) > 0) {
          for (int i = 0; i < count; buf.append(chars[i++]))
            ;
        }
        return buf.toString();
      } finally {
        try {
          isr.close();
          is.close();
        } catch (Exception e) {
        }
      }
    }
    log_.error("Repository item not found: " + resourceUri);
    throw new FileNotFoundException(resourceUri);
  }

  /**
   * Reads the specified resource and returns it as a string.
   * 
   * @return the resource
   */
  public static String load(String resourceUri, Class parent)
      throws FileNotFoundException, IOException {
    return load(resourceUri, parent, null);
  }

}