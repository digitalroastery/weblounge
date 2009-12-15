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

package ch.o2it.weblounge.common;

import org.junit.Ignore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Utility class containing a few helper methods.
 */
@Ignore
public class TestUtils {
  
  /**
   * Loads the <code>XML</code> data from the specified file on the class
   * path and returns it after having stripped off any newlines, line breaks
   * and otherwise disturbing spaces and characters.
   * 
   * @param path the resource path
   * @return the contents of the resource
   */
  public static String loadXmlFromFile(String path) {
    File templateFile = new File(TestUtils.class.getResource(path).getPath());
    String template = null;
    try {
      byte[] buffer = new byte[(int)templateFile.length()];
      FileInputStream f = new FileInputStream(templateFile);
      f.read(buffer);
      template = new String(buffer).replaceFirst("<\\?.*?>", "");
      template = template.replaceAll("(>\\s*)+", ">").replaceAll("(\\s*<)+", "<");
    } catch (IOException e) {
      throw new RuntimeException("Error reading test resource at " + path);
    }
    return template;
  }  

}