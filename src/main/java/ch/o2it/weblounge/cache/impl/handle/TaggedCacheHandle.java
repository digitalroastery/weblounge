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

package ch.o2it.weblounge.cache.impl.handle;

import ch.o2it.weblounge.cache.CacheHandle;
import ch.o2it.weblounge.cache.impl.CacheTag;
import ch.o2it.weblounge.common.content.Tag;
import ch.o2it.weblounge.common.content.Taggable;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * This is an implementation of a {@link CacheHandle} that is identified by
 * tags. By implementing the {@link Taggable} interface, the handle can be
 * qualified
 */
public class TaggedCacheHandle extends CacheHandleImpl {

  /** the default expiration time for a page */
  protected static final long DEFAULT_EXPIRES = MS_PER_DAY;

  /** the default recheck time for a page */
  protected static final long DEFAULT_RECHECK = MS_PER_HOUR;

  /** the hash code */
  private int hashCode;

  /** the unique tag set of this cache handle */
  private Tag[] primaryTags;

  /**
   * Creates a new <code>TaggedCacheHandle</code>.
   * 
   * @param primary
   *          the primary key set
   * @param expires
   *          number of milliseconds that it takes for the handle to expire
   * @param recheck
   *          number of milliseconds that this handle is likely to need to be
   *          rechecked
   */
  public TaggedCacheHandle(Iterable<Tag> primary, long expires, long recheck) {
    super(expires, recheck);
    Set<Tag> s = new TreeSet<Tag>(new TagComparator());
    hashCode = 0;
    for (Tag t : primary) {
      if (t == null)
        throw new NullPointerException("Tag must no be null");
      if (t.getName() == null)
        throw new IllegalArgumentException("No keyless unique tags allowed");
      if (t.getValue() == null)
        throw new IllegalArgumentException("No valueless unique tags allowed");
      if (t.getValue() == CacheTag.ANY)
        throw new IllegalArgumentException("No wildcard tags allowed as primary tag");
      if (!s.add(t))
        throw new IllegalArgumentException("No duplicate unique tags allowed");
      hashCode += t.hashCode();
      addTag(t);
    }
    primaryTags = s.toArray(new Tag[s.size()]);
  }

  /**
   * @see ch.o2it.weblounge.cache.CacheHandle.cache.CacheHandle#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (o instanceof TaggedCacheHandle) {
      TaggedCacheHandle h = (TaggedCacheHandle) o;
      if (hashCode != h.hashCode)
        return false;
      if (primaryTags.length != h.primaryTags.length)
        return false;
      for (Tag tag : primaryTags) {
        boolean found = false;
        for (Tag otherTag : h.primaryTags) {
          if (tag.equals(otherTag)) {
            found = true;
            break;
          }
        }
        if (!found)
          return false;
      }
      return true;
    }
    return false;
  }

  /**
   * Returns the tag with the specified name.
   * 
   * @param name
   *          the tag name
   * @return the tag
   */
  protected Tag getTag(String name) {
    for (Tag t : primaryTags) {
      if (t.getName().equals(name))
        return t;
    }
    return null;
  }

  /**
   * @see ch.o2it.weblounge.cache.CacheHandle.cache.CacheHandle#getShortName()
   */
  @Override
  public String getShortName() {
    return toString();
  }

  /**
   * @see ch.o2it.weblounge.cache.CacheHandle.cache.CacheHandle#hashCode()
   */
  @Override
  public int hashCode() {
    return hashCode;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "TaggedCacheHandle";
  }

  /**
   * Implementation of a comparator used to sort the primary tag set. Ordering
   * works as follows:
   * <ul>
   * <li>hash code</li>
   * <li>keys alphabetically</li>
   * <li>value equality</li>
   * <li>value hash codes</li>
   * </ul>
   */
  private static class TagComparator implements Comparator<Tag> {

    public int compare(Tag o1, Tag o2) {
      int diff = o1.hashCode() - o2.hashCode();
      if (diff != 0)
        return diff;
      diff = o1.getName().compareTo(o2.getName());
      if (diff != 0)
        return diff;
      if (o1.getValue().equals(o2.getValue()))
        return 0;
      diff = o1.getValue().hashCode() - o2.getValue().hashCode();
      if (diff == 0)
        throw new IllegalArgumentException("tags incomparable (" + o1 + ", " + o2 + ")");
      return diff;
    }

  }
}