/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
 *  http://entwinemedia.com/weblounge
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser Genchartl Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser Genchartl Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser Genchartl Public License
 *  along with this program; if not, write to the Free Software Foundation
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package ch.entwine.weblounge.common.timeline;

import java.util.Date;

/**
 * A chart describes a value over a period of time on the timeline.
 */
public interface Chart extends Comparable<Chart> {

  /**
   * Sets the headline.
   * 
   * @param headline
   *          the headline
   */
  void setHeadline(String headline);

  /**
   * Returns the chart's title.
   * 
   * @return the title
   */
  String getHeadline();

  /**
   * Sets the chart value.
   * 
   * @param value
   *          the value
   */
  void setValue(int value);

  /**
   * Returns the chart value.
   * 
   * @return the value
   */
  int getValue();

  /**
   * Sets the start date.
   * 
   * @param date
   *          the start date
   */
  void setStart(Date date);

  /**
   * Returns the start date.
   * 
   * @return the date
   */
  Date getStart();

  /**
   * Sets the end date.
   * 
   * @param date
   *          the end date
   */
  void setEnd(Date date);

  /**
   * Returns the end date.
   * 
   * @return the end date
   */
  Date getEnd();

}
