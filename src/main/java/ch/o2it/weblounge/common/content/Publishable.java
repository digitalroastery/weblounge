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

package ch.o2it.weblounge.common.content;

import ch.o2it.weblounge.common.user.User;

import java.util.Date;

/**
 * This interface is used for objects that can be published from a given start
 * date to an end date.
 */
public interface Publishable {

  /**
   * Returns the user that published the resource.
   * 
   * @return the publisher
   */
  User getPublisher();
  
  /**
   * Returns the publishing start date.
   * 
   * @return the publishing start date
   */
  Date getPublishFrom();

  /**
   * Returns the publishing end date.
   * 
   * @return the publishing end date
   */
  Date getPublishTo();

  /**
   * Returns <code>true</code> if the object is published right now.
   * 
   * @return <code>true</code> if published
   */
  boolean isPublished();

  /**
   * Returns <code>true</code> if the object is published on the given date.
   * 
   * @return <code>true</code> if published on the given date
   */
  boolean isPublished(Date date);
  
  /**
   * Sets the publisher and the publishing start and end date.
   * 
   * @param publisher
   *          the publisher
   * @param from
   *          publishing start date
   * @param to
   *          publishing end date
   */
  void setPublished(User publisher, Date from, Date to);

  /**
   * Sets the user that published or unpublished the page.
   * 
   * @param user
   *          the publisher
   */
  void setPublisher(User user);

  /**
   * Sets the publishing start date. Pass <code>null</code> to set no start date
   * at all.
   * 
   * @param from
   *          the start date
   */
  void setPublishFrom(Date from);
  
  /**
   * Sets the publishing end date. Pass <code>null</code> to set no end date at
   * all.
   * 
   * @param to
   *          the end date
   */
  void setPublishTo(Date to);

}