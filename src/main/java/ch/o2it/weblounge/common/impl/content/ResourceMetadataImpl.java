/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.content;

import ch.o2it.weblounge.common.content.ResourceMetadata;
import ch.o2it.weblounge.common.language.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation for the {@link ResourceMetadata}.
 */
public class ResourceMetadataImpl<T> implements ResourceMetadata<T> {

  /** The name of this metadata item */
  protected String name = null;

  /** Values */
  protected List<T> values = new ArrayList<T>();

  /** Localized values */
  protected Map<Language, List<T>> localizedValues = new HashMap<Language, List<T>>();

  /** True to add the values to the fulltext index */
  protected boolean addToFulltext = true;

  /**
   * Creates a new metadata object with the given name and values.
   * 
   * @param name
   *          the metadata name
   * @param values
   *          the language neutral values
   * @param localizedValues
   *          the localized values
   * @param addToFulltext
   *          <code>true</code> to add these items to the fulltext index
   */
  public ResourceMetadataImpl(String name, List<T> values,
      Map<Language, List<T>> localizedValues, boolean addToFulltext) {
    this.name = name;
    this.values = values;
    this.localizedValues = localizedValues;
    this.addToFulltext = addToFulltext;
  }

  /**
   * Creates a new metadata item with the given name.
   * 
   * @param name
   *          the name
   */
  public ResourceMetadataImpl(String name) {
    this.name = name;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.ResourceMetadata#getName()
   */
  public String getName() {
    return name;
  }

  /**
   * Adds <code>value</code> to the list of language neutral values.
   * 
   * @param language
   *          the language
   * @param v
   *          the value
   */
  public void addLocalizedValue(Language language, T v) {
    if (localizedValues == null)
      localizedValues = new HashMap<Language, List<T>>();
    List<T> values = localizedValues.get(language);
    if (values == null)
      values = new ArrayList<T>();
    if (!values.contains(v))
      values.add(v);
    localizedValues.put(language, values);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.ResourceMetadata#getLocalizedValues()
   */
  public Map<Language, List<T>> getLocalizedValues() {
    if (localizedValues == null)
      return Collections.emptyMap();
    return localizedValues;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.ResourceMetadata#addValue(java.lang.Object)
   */
  public void addValue(T v) {
    if (values == null)
      values = new ArrayList<T>();
    if (!values.contains(v))
      values.add(v);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.ResourceMetadata#getValues()
   */
  public List<T> getValues() {
    if (values == null)
      return Collections.emptyList();
    return values;
  }

  /**
   * Adds the metadata values to the fulltext index.
   * 
   * @param addToFulltext
   *          <code>true</code> to add the values to the fulltext index
   */
  public void setAddToFulltext(boolean addToFulltext) {
    this.addToFulltext = addToFulltext;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.ResourceMetadata#addToFulltext()
   */
  public boolean addToFulltext() {
    return addToFulltext;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return name.hashCode();
  }
  
  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ResourceMetadata<?>))
      return false;
    return name.equals(((ResourceMetadata<?>)obj).getName());
  }
  
  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return name;
  }

}
