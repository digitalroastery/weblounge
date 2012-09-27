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
package ch.entwine.weblounge.common.impl.timeline;

import ch.entwine.weblounge.common.timeline.Chart;

import java.util.Date;

/**
 * A chart is a period of time on the timeline that displays a value.
 */
public class ChartImpl implements Chart {

  /** The chart's title */
  protected String headline = null;

  /** The chart's value */
  protected int value = -1;

  /** The start date */
  protected Date startDate = null;

  /** The end date */
  protected Date endDate = null;

  /**
   * Creates a new chart with the given title, start and end date.
   * 
   * @param headline
   *          the headline
   * @param start
   *          the start date
   * @param end
   *          the end date
   * @param value
   *          the chart value
   * @throws IllegalArgumentException
   *           if either one of <code>start</code>, <code>end</code> is
   *           <code>null</code>
   */
  public ChartImpl(String headline, Date start, Date end, int value) {
    if (start == null)
      throw new IllegalArgumentException("Start date must not be null");
    if (end == null)
      throw new IllegalArgumentException("End date must not be null");
    this.headline = headline;
    this.startDate = start;
    this.endDate = end;
    this.value = value;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Chart#setStart(java.util.Date)
   */
  @Override
  public void setStart(Date date) {
    if (date == null)
      throw new IllegalArgumentException("Start date must not be null");
    this.startDate = date;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Chart#getStart()
   */
  @Override
  public Date getStart() {
    return startDate;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Chart#setEnd(java.util.Date)
   */
  @Override
  public void setEnd(Date date) {
    if (date == null)
      throw new IllegalArgumentException("End date must not be null");
    this.endDate = date;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Chart#getEnd()
   */
  @Override
  public Date getEnd() {
    return endDate;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Chart#setHeadline(java.lang.String)
   */
  @Override
  public void setHeadline(String headline) {
    this.headline = headline;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Chart#getHeadline()
   */
  @Override
  public String getHeadline() {
    return headline;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Chart#setValue(int)
   */
  @Override
  public void setValue(int value) {
    this.value = value;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Chart#getValue()
   */
  @Override
  public int getValue() {
    return value;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(Chart o) {
    if (!startDate.equals(o.getStart()))
      return startDate.compareTo(o.getStart());
    else if (endDate != null)
      return endDate.compareTo(o.getEnd());
    return 0;
  }

}
