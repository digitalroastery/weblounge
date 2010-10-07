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

package ch.o2it.weblounge.common.impl.url;

import java.io.File;

/**
 * <code>PathSupport</code> is a helper class to deal with filesystem paths.
 */
public class PathSupport {

  /**
   * This class should not be instantiated, since it only provides static
   * utility methods.
   */
  private PathSupport() {
  }

  /**
   * Concatenates the two urls with respect to leading and trailing slashes.
   * 
   * @return the concatenated url of the two arguments
   */
  public static String concat(String prefix, String suffix) {
    if (prefix == null)
      throw new IllegalArgumentException("Prefix cannot be null");
    if (suffix == null)
      throw new IllegalArgumentException("Suffix cannot be null");

    prefix = adjustSeparator(prefix);
    suffix = adjustSeparator(suffix);
    prefix = removeDoubleSeparator(prefix);
    suffix = removeDoubleSeparator(suffix);

    if (!prefix.endsWith(File.separator) && !suffix.startsWith(File.separator))
      prefix += File.separator;
    if (prefix.endsWith(File.separator) && suffix.startsWith(File.separator))
      suffix = suffix.substring(1);

    prefix += suffix;
    return prefix;
  }

  /**
   * Concatenates the path elements with respect to leading and trailing
   * slashes.
   * 
   * @param parts
   *          the parts to concat
   * @return the concatenated path
   */
  public static String concat(String[] parts) {
    if (parts == null)
      throw new IllegalArgumentException("Parts cannot be null");
    if (parts.length == 0)
      throw new IllegalArgumentException("Array parts is empty");
    String path = removeDoubleSeparator(adjustSeparator(parts[0]));
    for (int i = 1; i < parts.length; i++) {
      path = concat(path, removeDoubleSeparator(adjustSeparator(parts[i])));
    }
    return path;
  }

  /**
   * Returns the trimmed url. Trimmed means that the url is free from leading or
   * trailing whitespace characters, and that a directory url like
   * <code>/news/</code> is closed by a slash (<code>/</code>).
   * 
   * @param path
   *          the path to trim
   * @return the trimmed path
   */
  public static String trim(String path) {
    if (path == null)
      throw new IllegalArgumentException("Path cannot be null");
    path = path.trim();
    path = removeDoubleSeparator(adjustSeparator(path));
    if (path.endsWith(File.separator) || (path.length() == 1))
      return path;

    int index = path.lastIndexOf(File.separator);
    index = path.indexOf(".", index);
    if (index == -1)
      path += File.separator;
    return path;
  }

  /**
   * Returns the file extension. If the file does not have an extension, then
   * <code>null</code> is returned.
   * 
   * @param path
   *          the file path
   * @return the file extension
   */
  public static String getFileExtension(String path) {
    if (path == null) {
      throw new IllegalArgumentException("Path cannot be null");
    }
    int index = path.lastIndexOf('.');
    if (index > 0 && index < path.length()) {
      return path.substring(index + 1);
    }
    return null;
  }

  /**
   * Checks that the path only contains the system path separator. If not, wrong
   * ones are replaced.
   */
  private static String adjustSeparator(String path) {
    String sp = File.separator;
    if ("\\".equals(sp))
      sp = "\\\\";
    return path.replaceAll("/", sp);
  }

  /**
   * Removes any occurence of double file separators and replaces it with a
   * single one.
   * 
   * @param path
   *          the path to check
   * @return the corrected path
   */
  private static String removeDoubleSeparator(String path) {
    int index = 0;
    String s = File.separator + File.separatorChar;
    while ((index = path.indexOf(s, index)) != -1) {
      path = path.substring(0, index) + path.substring(index + 1);
    }
    return path;
  }

}