/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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
package ch.entwine.weblounge.common.impl.timeline;

import ch.entwine.weblounge.common.timeline.Era;

import java.util.Date;

/**
 * An era is a period of time on the timeline.
 */
public class EraImpl implements Era {

  /** The era's title */
  protected String headline = null;

  /** The era's tag */
  protected String tag = null;

  /** The start date */
  protected Date startDate = null;

  /** The end date */
  protected Date endDate = null;

  /**
   * Creates a new era with the given title, start and end date.
   * 
   * @param headline
   *          the headline
   * @param start
   *          the start date
   * @param end
   *          the end date
   * @throws IllegalArgumentException
   *           if either one of <code>start</code>, <code>end</code> is
   *           <code>null</code>
   */
  public EraImpl(String headline, Date start, Date end) {
    if (start == null)
      throw new IllegalArgumentException("Start date must not be null");
    if (end == null)
      throw new IllegalArgumentException("End date must not be null");
    this.headline = headline;
    this.startDate = start;
    this.endDate = end;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Era#setStart(java.util.Date)
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
   * @see ch.entwine.weblounge.common.timeline.Era#getStart()
   */
  @Override
  public Date getStart() {
    return startDate;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Era#setEnd(java.util.Date)
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
   * @see ch.entwine.weblounge.common.timeline.Era#getEnd()
   */
  @Override
  public Date getEnd() {
    return endDate;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Era#setHeadline(java.lang.String)
   */
  @Override
  public void setHeadline(String headline) {
    this.headline = headline;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Era#getHeadline()
   */
  @Override
  public String getHeadline() {
    return headline;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Era#setTag(java.lang.String)
   */
  @Override
  public void setTag(String tag) {
    this.tag = tag;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Era#getTag()
   */
  @Override
  public String getTag() {
    return tag;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(Era o) {
    if (!startDate.equals(o.getStart()))
      return startDate.compareTo(o.getStart());
    else if (endDate != null)
      return endDate.compareTo(o.getEnd());
    return 0;
  }

}
