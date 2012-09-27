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
package ch.entwine.weblounge.common.timeline;

import java.util.Set;

/**
 * Timeline represents a collection of eras and points in time.
 */
public interface Timeline {

  /**
   * Sets the headline.
   * 
   * @param headline
   *          the headline
   */
  void setHeadline(String headline);

  /**
   * Returns the headline or <code>null</code> if no headline has been set.
   * 
   * @return the headline
   */
  String getHeadline();

  /**
   * Sets the description.
   * 
   * @param description
   *          the description
   */
  void setDescription(String description);

  /**
   * Returns the description or <code>null</code> if no description has been
   * set.
   * 
   * @return the description
   */
  String getDescription();

  /**
   * Sets the timeline's asset.
   * 
   * @param asset
   *          the asset
   */
  void setAsset(Asset asset);

  /**
   * Returns the timline's asset.
   * 
   * @return the asset
   */
  Asset getAsset();

  /**
   * Adds the era to the timeline.
   * 
   * @param era
   *          the era
   */
  void addEra(Era era);

  /**
   * Returns the eras.
   * 
   * @return the eras
   */
  Set<Era> getEras();

  /**
   * Adds a point in time.
   * 
   * @param pointInTime
   *          a point in time
   */
  void addPointInTime(PointInTime pointInTime);

  /**
   * Returns the points in time.
   * 
   * @return the points in time
   */
  Set<PointInTime> getPointsInTime();

  /**
   * Adds the chart to the timeline.
   * 
   * @param chart
   *          the chart
   */
  void addChart(Chart chart);

  /**
   * Returns the charts.
   * 
   * @return the charts
   */
  Set<Chart> getCharts();

}
