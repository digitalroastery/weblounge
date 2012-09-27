/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
 *  http://entwinemedia.com/weblounge
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser GenpointInTimel Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser GenpointInTimel Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser GenpointInTimel Public License
 *  along with this program; if not, write to the Free Software Foundation
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package ch.entwine.weblounge.common.impl.timeline;

import ch.entwine.weblounge.common.timeline.Asset;
import ch.entwine.weblounge.common.timeline.PointInTime;

import org.apache.commons.lang.StringUtils;

import java.util.Date;

/**
 * An <code>PointInTime</code> represents an event on the timeline.
 */
public class PointInTimeImpl implements PointInTime {

  /** The pointInTime's title */
  protected String headline = null;

  /** Text describing the point in time */
  protected String text = null;

  /** The start date */
  protected Date startDate = null;

  /** The end date */
  protected Date endDate = null;

  /** The timepoint's asset */
  protected Asset asset = null;

  /** The tag */
  protected String tag = null;

  /**
   * Creates a new point in time with the given title, start and end date.
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
  public PointInTimeImpl(String headline, Date start, Date end) {
    if (start == null)
      throw new IllegalArgumentException("Start date must not be null");
    this.headline = StringUtils.trimToNull(headline);
    this.startDate = start;
    this.endDate = end;
  }

  /**
   * Creates a new point in time with the given title.
   * 
   * @param headline
   *          the headline
   * @param date
   *          the date
   * @throws IllegalArgumentException
   *           if either one of <code>start</code>, <code>end</code> is
   *           <code>null</code>
   */
  public PointInTimeImpl(String headline, Date date) {
    this(headline, date, null);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.PointInTime#setStart(java.util.Date)
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
   * @see ch.entwine.weblounge.common.timeline.PointInTime#getStart()
   */
  @Override
  public Date getStart() {
    return startDate;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.PointInTime#setEnd(java.util.Date)
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
   * @see ch.entwine.weblounge.common.timeline.PointInTime#getEnd()
   */
  @Override
  public Date getEnd() {
    return endDate;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.PointInTime#setHeadline(java.lang.String)
   */
  @Override
  public void setHeadline(String headline) {
    this.headline = headline;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.PointInTime#getHeadline()
   */
  @Override
  public String getHeadline() {
    return headline;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.PointInTime#setText(java.lang.String)
   */
  @Override
  public void setText(String text) {
    this.text = StringUtils.trimToNull(text);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.PointInTime#getText()
   */
  @Override
  public String getText() {
    return text;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.PointInTime#setTag(java.lang.String)
   */
  @Override
  public void setTag(String tag) {
    this.tag = tag;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.PointInTime#getTag()
   */
  @Override
  public String getTag() {
    return tag;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.PointInTime#setAsset(ch.entwine.weblounge.common.timeline.Asset)
   */
  @Override
  public void setAsset(Asset asset) {
    this.asset = asset;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.PointInTime#getAsset()
   */
  @Override
  public Asset getAsset() {
    return asset;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(PointInTime o) {
    if (!startDate.equals(o.getStart()))
      return startDate.compareTo(o.getStart());
    else if (endDate != null)
      return endDate.compareTo(o.getEnd());
    return 0;
  }

}
