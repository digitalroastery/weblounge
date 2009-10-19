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

package ch.o2it.weblounge.common.request;

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.content.Tag;
import ch.o2it.weblounge.common.content.Taggable;

import java.util.Collection;
import java.util.Iterator;

/**
 * Identifies a cached object. Implementing classes must provide meaningful
 * <code>hashCode()</code> and </code>equals()</code> methods.
 */
public interface CacheHandle extends Times, Taggable {

  /**
   * Returns the time the cached object expires
   * 
   * @return the expiration time
   */
  long getExpires();

  /**
   * Sets the time the cached object expires.
   * 
   * @param expires
   *          the expiration time to set
   */
  void setExpires(long expires);

  /**
   * Returns the recheck time of the cached objects.
   * 
   * @return the recheck time
   */
  long getRecheck();

  /**
   * Sets the recheck time of the cached object.
   * 
   * @param recheck
   *          the new recheck time
   */
  void setRecheck(long recheck);

  /**
   * Returns the short name of this cache handle.
   * 
   * @return the short name of this handle
   */
  String getShortName();

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#addTag(java.lang.String,
   *      java.lang.Object)
   */
  boolean addTag(String key, Object value);

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#addTag(ch.o2it.weblounge.api.util.Tag)
   */
  boolean addTag(Tag tag);

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#addTags(java.util.Collection)
   */
  boolean addTags(Collection<Tag> t);

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#clearTags()
   */
  void clearTags();

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#containsTag(ch.o2it.weblounge.api.util.Tag)
   */
  boolean containsTag(Tag tag);

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#isTagged()
   */
  boolean isTagged();

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#removeTag(ch.o2it.weblounge.api.util.Tag)
   */
  boolean removeTag(Tag tag);

  /**
   * @see ch.o2it.weblounge.api.util.Taggable#tags()
   */
  Iterator<Tag> tags();

}