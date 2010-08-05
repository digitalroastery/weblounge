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

package ch.o2it.weblounge.common.content.page;

import org.apache.commons.lang.StringUtils;

/**
 * The page preview enumeration defines whether a pagelet will be part of the
 * page preview.
 * <p>
 * It can be not included at all (<code>None</code>), on the first occurrence
 * only (<code>First</code>), on every occurrence ( <code>All</code>) or it may
 * trigger inclusion of all pagelets that are in the stage composer before this
 * pagelet ( <code>Boundary</code>).
 */
public enum PagePreviewMode {

  /**
   * Do not include the pagelet at all (unless included by a subsequent pagelet
   * with the value {@link #Boundary}.
   */
  None,

  /**
   * Include the pagelet on the first occurrence only. (additional occurrences
   * may be included by a subsequent pagelet with the value {@link #Boundary}.
   * <p>
   * This would be the typical setting for a <code>title</code> element, since
   * you would want to include the page's first title element in a preview.
   */
  First,

  /**
   * Include all occurrences of the pagelet. (some occurrences may be skipped if
   * they appear after a pagelet with the value {@link #Boundary}.
   */
  All,

  /**
   * Triggers the inclusion of all the pagelets that appear before this pagelet
   * and the exclusion of everything that appears after it, regardless of an
   * {@link #All} setting on a pagelet.
   * <p>
   * This setting should be used for the typical preview marker, which splits
   * the teaser from the main article, e. g. for feed generation etc.
   */
  Boundary;

  /**
   * Parses the page preview.
   * 
   * @param mode
   *          the mode
   * @return the page preview
   */
  public static PagePreviewMode parse(String mode) {
    if (mode == null)
      throw new IllegalArgumentException("Mode must not be null");
    mode = StringUtils.capitalize(mode.toLowerCase());
    return PagePreviewMode.valueOf(mode);
  }

}