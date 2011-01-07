/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.o2it.weblounge.workbench;

import ch.o2it.weblounge.common.content.page.Pagelet;

/**
 * Class containing all the information that is needed to edit a certain
 * pagelet.
 * 
 * TODO: Add tests
 */
public class PageletEditor {

  /** The pagelet */
  protected Pagelet pagelet = null;

  /**
   * Creates a new pagelet editor for the given pagelet.
   * 
   * @param pagelet
   *          the pagelet
   */
  public PageletEditor(Pagelet pagelet) {
    this.pagelet = pagelet;
  }

  /**
   * Returns the pagelet.
   * 
   * @return the pagelet
   */
  public Pagelet getPagelet() {
    return pagelet;
  }

  /**
   * Returns the <code>XML</code> representation of this pagelet.
   * 
   * @return the pagelet
   */
  public String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append(pagelet.toXml());
    return buf.toString();
  }

}
