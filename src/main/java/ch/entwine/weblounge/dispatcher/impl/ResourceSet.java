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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A <code>ResourceSet</code> is used to make sure content is only loaded from
 * directories on the file system that should be accessible.
 */
public class ResourceSet {

  /** the include pattern */
  private Pattern include = null;

  /** the exclude pattern */
  private List<Pattern> excludes = new ArrayList<Pattern>();

  /**
   * Creates a new <code>ResourceSet</code> that specifies a path without any
   * restrictions.
   * 
   * @param include
   *          the path to include
   */
  protected ResourceSet(String include) {
    this(include, null);
  }

  /**
   * Creates a new <code>ResourceSet</code>.
   * 
   * @param include
   *          the include pattern
   * @param excludes
   *          the exclude pattern
   */
  public ResourceSet(String include, String[] excludes) {
    setInclude(include);
    setExcludes(excludes);
  }

  /**
   * Specifies which resources to include by means of a regular expression.
   * 
   * @param include
   *          the include
   */
  public void setInclude(String include) {
    if (include == null)
      throw new IllegalArgumentException("Include expression may not be null");
    try {
      this.include = Pattern.compile(include);
    } catch (PatternSyntaxException e) {
      throw new IllegalArgumentException("Include expression '" + include + "' is invalid", e);
    }
  }

  /**
   * Specifies which resources to include by means of a regular expression.
   * 
   * @param include
   *          the include
   */
  public void setExcludes(String[] excludes) {
    this.excludes.clear();
    if (excludes == null)
      return;

    // Compile the exclude patterns
    for (String exclude : excludes) {
      try {
        this.excludes.add(Pattern.compile(exclude));
      } catch (PatternSyntaxException e) {
        throw new IllegalArgumentException("Exclude expression '" + exclude + "' is invalid", e);
      }
    }
  }

  /**
   * Excludes the given pattern from the resource set.
   * 
   * @param exclude
   *          the pattern to exclude
   */
  public void exclude(String exclude) {
    try {
      this.excludes.add(Pattern.compile(exclude));
    } catch (PatternSyntaxException e) {
      throw new IllegalArgumentException("Exclude expression '" + exclude + "' is invalid", e);
    }
  }

  /**
   * Checks whether this <code>ResourceSet</code> includes the given path.
   * 
   * @param path
   *          the path to check
   * @return <code>true</code> if the path is included
   */
  public boolean includes(String path) {
    return include.matcher(path).lookingAt();
  }

  /**
   * Checks whether this <code>ResourceSet</code> excludes the given path.
   * 
   * @param path
   *          the path to check
   * @return <code>true</code> if the path is excluded
   */
  public boolean excludes(String path) {
    for (Pattern pattern : excludes)
      if (pattern.matcher(path).lookingAt())
        return true;
    return false;
  }

}
