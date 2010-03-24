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

package ch.o2it.weblounge.common.impl.content;

import ch.o2it.weblounge.common.content.Page;

/**
 * This class contains utility methods intended to facilitate dealing with page
 * versions and names.
 */
public class PageUtils {

  /**
   * Returns the version for the given version identifier. Available versions
   * are:
   * <ul>
   * <li>{@link Page#LIVE}</li>
   * <li>{@link Page#WORK}</li>
   * 
   * @param version
   *          the version identifier
   * @return the version string
   */
  public static long getVersion(String version) {
    if (version.equals("index")) {
      return Page.LIVE;
    } else if (version.equals("work")) {
      return Page.WORK;
    } else {
      try {
        return Long.parseLong(version);
      } catch (NumberFormatException e) {
        return -1;
      }
    }
  }

  /**
   * Returns the document name for the given version. For the live version, this
   * method will return <code>index.xml</code>. Available versions are:
   * <ul>
   * <li>{@link Page#LIVE}</li>
   * <li>{@link Page#WORK}</li>
   * 
   * @param version
   *          the version identifier
   * @return the version string
   */
  public static String getDocument(long version) {
    if (version == Page.LIVE)
      return "index.xml";
    else if (version == Page.WORK)
      return "work.xml";
    else
      return Long.toString(version) + ".xml";
  }

  /**
   * Returns the version identifier for the given version. Available versions
   * are:
   * <ul>
   * <li>{@link Page#LIVE}</li>
   * <li>{@link Page#WORK}</li>
   * 
   * @param version
   *          the version identifier
   * @return the version string
   */
  public static String getVersionString(long version) {
    if (version == Page.LIVE)
      return "live";
    else if (version == Page.WORK)
      return "work";
    else
      return Long.toString(version);
  }

}