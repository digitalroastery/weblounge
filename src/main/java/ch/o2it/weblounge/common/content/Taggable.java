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

import java.util.Collection;
import java.util.Iterator;

/**
 * This interface defines methods and fields for objects which are able to
 * collect tagging information.
 */
public interface Taggable {

  /**
   * Inserts the new tag into the tag set, provided an identical tag is not
   * already contained.
   * 
   * @param tag
   *          the tag to add
   * @return <code>true</code> if the tag could be inserted
   */
  boolean addTag(Tag tag);

  /**
   * Adds a new tag with the given name and value and inserts it into the set,
   * provided an identical tag is not already contained.
   * 
   * @param name
   *          the tag name
   * @param value
   *          the tag value
   * @return <code>true</code> if the tag could be inserted
   */
  boolean addTag(String name, Object value);

  /**
   * Adds those tags from <code>tags</code> to the tag set that are not already
   * contained and returns <code>true</code> if at least one tag was added.
   * 
   * @param tags
   *          the tags to add
   * @return <code>true</code> if the tag set was modified
   */
  boolean addTags(Collection<Tag> tags);

  /**
   * Removes all tags from the tag set.
   */
  void clearTags();

  /**
   * Returns <code>true</code> if the tag with the specified name and value is
   * contained in the current tag set.
   * 
   * @param tag
   *          the tag
   * @return <code>true</code> if the tag is contained in the tag set
   */
  boolean containsTag(Tag tag);

  /**
   * Returns <code>true</code> if at least one tag with the specified name is
   * contained in the current tag set.
   * 
   * @param name
   *          the tag name
   * @return <code>true</code> if a tag with this name is contained in the tag
   *         set
   */
  boolean containsTag(String name);

  /**
   * Returns <code>true</code> if the tag with the specified name and value is
   * contained in the current tag set.
   * 
   * @param name
   *          the tag name
   * @param value
   *          the tag value
   * @return <code>true</code> if the tag is contained in the tag set
   */
  boolean containsTag(String name, String value);

  /**
   * Returns an iteration of all tags.
   * 
   * @return the tags
   */
  Iterator<Tag> tags();

  /**
   * Removes the tag from the tag set and returns <code>true</code> if it was
   * removed and the set changed due to the removal.
   * 
   * @param tag
   *          the tag to remove
   * @return <code>true</code> if the set changed
   */
  boolean removeTag(Tag tag);

  /**
   * Removes all tags from the tag set with the given name and returns
   * <code>true</code> if at least one tag was removed and the set changed due
   * to the removal.
   * 
   * @param tag
   *          the tag to remove
   * @return <code>true</code> if the set changed
   */
  boolean removeTag(String name);

  /**
   * Removes the tag from the tag set and returns <code>true</code> if it was
   * removed and the set changed due to the removal.
   * 
   * @param name
   *          the tag name
   * @param value
   *          the tag value
   * @return <code>true</code> if the set changed
   */
  boolean removeTag(String name, String value);

  /**
   * Returns <code>true</code> if at least one tag is defined.
   * 
   * @return <code>true</code> if there is at least one tag
   */
  boolean isTagged();

  /**
   * Returns an array consisting of the current tags.
   * 
   * @param tag
   *          the array
   * @return an array of tags
   */
  Tag[] getTags(Tag[] tag);

}