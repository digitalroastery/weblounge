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

import java.util.Date;

/**
 * TODO: Comment PublishingContext
 */
public interface PublishingContext extends Cloneable {

  /**
   * Returns the publishing start date.
   * 
   * @return the publishing start date
   */
  Date getFrom();

  /**
   * Sets the publishing start date.
   * 
   * @param from
   *          the start date
   */
  void setFrom(Date from);

  /**
   * Returns the publishing end date.
   * 
   * @return the publishing end date
   */
  Date getTo();

  /**
   * Sets the publishing end date.
   * 
   * @param to
   *          the end date
   */
  void setTo(Date to);

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
   * Returns an XML representation of this context.
   * 
   * @return an XML representation of this context
   */
  String toXml();

}