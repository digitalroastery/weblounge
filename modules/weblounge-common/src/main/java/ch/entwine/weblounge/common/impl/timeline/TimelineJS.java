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

import ch.entwine.weblounge.common.timeline.Asset;
import ch.entwine.weblounge.common.timeline.Chart;
import ch.entwine.weblounge.common.timeline.Era;
import ch.entwine.weblounge.common.timeline.PointInTime;
import ch.entwine.weblounge.common.timeline.Timeline;

import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Java object representation for the TimelineJS javascript library.
 */
public class TimelineJS implements Timeline {

  /** Timeline types */
  public enum Type {
    Default
  };

  /** The timeline's title */
  protected String headline = null;

  /** The timeline's type */
  protected Type type = null;

  /** The start date */
  protected Date startDate = null;

  /** The overall asset */
  protected Asset asset = null;

  /** The timeline's era */
  protected EraImpl era = null;

  /** The timeline's description */
  protected String description = null;

  /** The eras */
  protected Set<Era> eras = new HashSet<Era>();

  /** The points in time */
  protected Set<PointInTime> pointsInTime = new HashSet<PointInTime>();

  /** The eras */
  protected Set<Chart> charts = new HashSet<Chart>();

  /**
   * Creates a new timeline.
   */
  public TimelineJS() {
    this.type = Type.Default;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Timeline#setDescription(java.lang.String)
   */
  public void setDescription(String description) {
    this.description = StringUtils.trimToNull(description);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Timeline#getDescription()
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Timeline#setHeadline(java.lang.String)
   */
  @Override
  public void setHeadline(String headline) {
    this.headline = headline;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Timeline#getHeadline()
   */
  @Override
  public String getHeadline() {
    return headline;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Timeline#setAsset(ch.entwine.weblounge.common.timeline.Asset)
   */
  @Override
  public void setAsset(Asset asset) {
    this.asset = asset;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Timeline#getAsset()
   */
  @Override
  public Asset getAsset() {
    return asset;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Timeline#addEra(ch.entwine.weblounge.common.timeline.Era)
   */
  public void addEra(Era era) {
    if (era == null)
      throw new IllegalArgumentException("Era must not be null");
    if (era.getStart() == null)
      throw new IllegalArgumentException("Era must have a start date");
    if (era.getEnd() == null)
      throw new IllegalArgumentException("Era must have an end date");
    eras.add(era);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Timeline#getEras()
   */
  @Override
  public Set<Era> getEras() {
    return eras;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Timeline#addPointInTime(ch.entwine.weblounge.common.timeline.PointInTime)
   */
  @Override
  public void addPointInTime(PointInTime pointInTime) {
    if (pointInTime == null)
      throw new IllegalArgumentException("Point in time must not be null");
    if (pointInTime.getStart() == null)
      throw new IllegalArgumentException("Point in time must have a start date");
    pointsInTime.add(pointInTime);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Timeline#getPointsInTime()
   */
  @Override
  public Set<PointInTime> getPointsInTime() {
    return pointsInTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Timeline#addChart(ch.entwine.weblounge.common.timeline.Chart)
   */
  @Override
  public void addChart(Chart chart) {
    if (chart == null)
      throw new IllegalArgumentException("Chart must not be null");
    if (chart.getStart() == null)
      throw new IllegalArgumentException("Chart must have a start date");
    if (chart.getEnd() == null)
      throw new IllegalArgumentException("Chart must have an end date");
    charts.add(chart);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Timeline#getCharts()
   */
  @Override
  public Set<Chart> getCharts() {
    return charts;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();

    boolean elementAdded = false;

    buf.append("{ \"timeline\" : { ");

    elementAdded |= keyValue(buf, "headline", headline, true, false);
    elementAdded |= keyValue(buf, "type", type.toString().toLowerCase(), true, elementAdded);
    elementAdded |= keyValue(buf, "description", description, true, elementAdded);
    if (startDate != null)
      elementAdded |= keyValue(buf, "startDate", TimelineUtils.format(startDate), true, elementAdded);
    if (asset != null) {
      elementAdded = false;
      buf.append(" asset : { ");
      elementAdded |= keyValue(buf, "media", asset.getMedia().toExternalForm(), true, false);
      elementAdded |= keyValue(buf, "credit", asset.getCredit(), true, elementAdded);
      keyValue(buf, "caption", asset.getCaption(), true, elementAdded);
      buf.append(" }, ");
    }

    // Eras

    if (eras.size() > 0) {
      buf.append(", ");
      buf.append("\"era\" : ");

      if (eras.size() > 1)
        buf.append(" [ ");

      int i = 0;
      for (Era era : eras) {
        elementAdded = false;
        buf.append("{ ");
        elementAdded |= keyValue(buf, "startDate", TimelineUtils.format(era.getStart()), true, false);
        elementAdded |= keyValue(buf, "endDate", TimelineUtils.format(era.getEnd()), true, elementAdded);
        keyValue(buf, "headline", era.getHeadline(), true, elementAdded);
        buf.append(" }");
        if (i++ < eras.size() - 1)
          buf.append(",");
      }

      if (eras.size() > 1)
        buf.append(" ] ");
    }

    // Points in time

    if (pointsInTime.size() > 0) {
      buf.append(", ");
      buf.append("\"date\" : ");

      if (pointsInTime.size() > 1)
        buf.append(" [ ");

      int i = 0;
      for (PointInTime pit : pointsInTime) {
        elementAdded = false;
        buf.append("{ ");
        elementAdded |= keyValue(buf, "startDate", TimelineUtils.format(pit.getStart()), true, false);
        if (pit.getEnd() != null)
          elementAdded |= keyValue(buf, "endDate", TimelineUtils.format(pit.getEnd()), true, elementAdded);
        elementAdded |= keyValue(buf, "headline", pit.getHeadline(), true, elementAdded);
        keyValue(buf, "text", pit.getText(), true, elementAdded);
        if (pit.getAsset() != null) {
          elementAdded = false;
          Asset asset = pit.getAsset();
          buf.append(" asset : { ");
          elementAdded |= keyValue(buf, "media", asset.getMedia().toExternalForm(), true, false);
          elementAdded |= keyValue(buf, "credit", asset.getCredit(), true, elementAdded);
          elementAdded |= keyValue(buf, "caption", asset.getCaption(), true, elementAdded);
          buf.append(" }, ");
        }
        buf.append(" }");
        if (i++ < pointsInTime.size() - 1)
          buf.append(",");
      }

      if (pointsInTime.size() > 1)
        buf.append(" ] ");
    }

    // Charts

    if (charts.size() > 0) {
      buf.append(", ");
      buf.append("\"chart\" : ");

      if (charts.size() > 1)
        buf.append(" [ ");

      int i = 0;
      for (Chart chart : charts) {
        elementAdded = false;
        buf.append("{ ");
        elementAdded |= keyValue(buf, "startDate", TimelineUtils.format(chart.getStart()), true, false);
        elementAdded |= keyValue(buf, "endDate", TimelineUtils.format(chart.getEnd()), true, elementAdded);
        elementAdded |= keyValue(buf, "headline", chart.getHeadline(), true, elementAdded);
        elementAdded |= keyValue(buf, "value", Integer.toString(chart.getValue()), true, elementAdded);
        buf.append(" }");
        if (i++ < charts.size() - 1)
          buf.append(",");
      }

      if (charts.size() > 1)
        buf.append(" ] ");
    }

    buf.append(" } }");

    return buf.toString();
  }

  /**
   * Adds the key value pair to the string buffer and returns <code>true</code>
   * if the element was added, <code>false</code> otherwise.
   * 
   * @param buf
   *          the buffer to add to
   * @param key
   *          the key
   * @param value
   *          the value
   * @param quote
   *          <code>true</code> to put the value in quotes
   * @param prependComma
   *          TODO
   * @return the <code>true</code> if the element was added
   */
  private boolean keyValue(StringBuffer buf, String key, String value,
      boolean quote, boolean prependComma) {
    if (value == null)
      return false;
    if (prependComma)
      buf.append(", ");
    buf.append(" \"").append(key).append("\" : ");
    if (quote)
      buf.append("\"");
    buf.append(value);
    if (quote)
      buf.append("\"");
    return true;
  }

}
