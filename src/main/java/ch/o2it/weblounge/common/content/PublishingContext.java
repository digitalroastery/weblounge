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
 * This interface is used for objects that can be published from a given start
 * date to an end date.
 */
public interface PublishingContext extends Publishable, Cloneable {

  /**
   * Sets the publishing start date.
   * 
   * @param from
   *          the start date
   */
  void setPublishFrom(Date from);

  /**
   * Sets the publishing end date.
   * 
   * @param to
   *          the end date
   */
  void setPublishTo(Date to);

  /**
   * Returns an XML representation of this context.
   * 
   * @return an XML representation of this context
   */
  String toXml();

}