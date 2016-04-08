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

package ch.entwine.weblounge.common.content.movie;

import org.apache.commons.lang3.StringUtils;

/**
 * The scan format of a video is either interlaced or progressive. In interlaced
 * mode, only every other line of pixels is updated per frame, while in
 * progressive mode, the whole frame is updated at once.
 */
public enum ScanType {

  /** Interlaced */
  Interlaced,

  /** Progressive */
  Progressive;

  /**
   * Creates a new scan type object from the given argument.
   * 
   * @param value
   *          the scan type as a string
   * @return the scan type
   * @throws IllegalArgumentException
   *           if no such scan type exists
   */
  public static ScanType fromString(String value) {
    return ScanType.valueOf(StringUtils.capitalize(value));
  }

}
