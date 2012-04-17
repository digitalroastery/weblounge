/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
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

package ch.entwine.weblounge.common.content;

import ch.entwine.weblounge.common.language.Language;

import java.util.List;
import java.util.Map;

/**
 * Resource metadata models a piece of metadata that describes one aspect of a
 * resource, e. g. the title.
 */
public interface ResourceMetadata<T> {

  /**
   * Returns the name of this metadata item.
   * 
   * @return the name
   */
  String getName();

  /**
   * Adds <code>value</code> to the list of language neutral values.
   * 
   * @param language
   *          the language
   * @param v
   *          the value
   */
  void addLocalizedValue(Language language, T v);

  /**
   * Returns the values for this metadata item, mapped to their respective
   * languages.
   * 
   * @return the localized values
   */
  Map<Language, List<T>> getLocalizedValues();

  /**
   * Returns <code>true</code> if this metadata item has been localized.
   * 
   * @return <code>true</code> if the metadata item is localized
   */
  boolean isLocalized();

  /**
   * Adds <code>value</code> to the list of language neutral values.
   * 
   * @param v
   *          the value
   */
  void addValue(T v);

  /**
   * Returns a list of all all non-localized values. In order to retrieve
   * localized values for this metadata field, use {@link #getLocalizedValues()}
   * .
   * 
   * @return the list of language neutral values
   */
  List<T> getValues();

  /**
   * Removes all values currently in the metadata container.
   */
  void clear();

  /**
   * Adds the metadata values to the fulltext index.
   * 
   * @param addToFulltext
   *          <code>true</code> to add the values to the fulltext index
   */
  void setAddToFulltext(boolean addToFulltext);

  /**
   * Returns <code>true</code> if the values should be added to the fulltext
   * search index.
   * 
   * @return <code>true</code> if the metadata values should be added to the
   *         fulltext index
   */
  boolean addToFulltext();

}
