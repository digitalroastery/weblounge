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

package ch.o2it.weblounge.common.impl.content;

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.content.Composeable;
import ch.o2it.weblounge.common.content.HTMLHeadElement;
import ch.o2it.weblounge.common.content.HTMLInclude;
import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.language.LocalizableObject;
import ch.o2it.weblounge.common.language.Language;

import java.util.HashSet;
import java.util.Set;

/**
 * Base implementation for composeable objects like page templates or images.
 */
public class GeneralComposeable extends LocalizableObject implements Composeable {

  /** Object identifier */
  protected String identifier = null;

  /** Composeable flag */
  protected boolean composeable = true;

  /** Milliseconds until validity check is recommended */
  protected long recheckTime = Times.MS_PER_DAY;

  /** Milliseconds until content using this object becomes invalid */
  protected long validTime = Times.MS_PER_WEEK;

  /** Name of this composeable */
  protected LocalizableContent<String> name = null;
  
  /** HTML head elements */
  protected Set<HTMLHeadElement> headers = null;

  /**
   * Creates a new composeable instance with a recheck time of a day and a valid
   * time of a week.
   */
  protected GeneralComposeable() {
    this(null, Times.MS_PER_DAY, Times.MS_PER_WEEK);
  }

  /**
   * Creates a new composeable instance with a recheck time of a day and a valid
   * time of a week.
   * 
   * @param identifier
   *          the identifier
   */
  protected GeneralComposeable(String identifier) {
    this(identifier, Times.MS_PER_DAY, Times.MS_PER_WEEK);
  }

  /**
   * Creates a new composeable instance with the given recheck and valid time.
   * 
   * @param recheckTime
   *          recheck time in milliseconds
   * @param validTime
   *          valid time in milliseconds
   * @see #setRecheckTime()
   * @see #setValidTime()
   */
  protected GeneralComposeable(long recheckTime, long validTime) {
    this(null, recheckTime, validTime);
  }

  /**
   * Creates a new composeable instance with the given recheck and valid time.
   * 
   * @param identifier
   *          the identifier
   * @param recheckTime
   *          recheck time in milliseconds
   * @param validTime
   *          valid time in milliseconds
   * @see #setRecheckTime()
   * @see #setValidTime()
   */
  protected GeneralComposeable(String identifier, long recheckTime,
      long validTime) {
    if (recheckTime < 0)
      throw new IllegalArgumentException("Recheck time must be greater than or equal to zero");
    if (validTime < 0)
      throw new IllegalArgumentException("Valid time must be greater than or equal to zero");
    this.identifier = identifier;
    this.name = new LocalizableContent<String>(this);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Composeable#setIdentifier(java.lang.String)
   */
  public void setIdentifier(String identifier) {
    if (identifier == null)
      throw new IllegalArgumentException("Identifier cannot be null");
    this.identifier = identifier;
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Composeable#getIdentifier()
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Composeable#setName(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public void setName(String name, Language language) {
    this.name.put(name, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Composeable#getName()
   */
  public String getName() {
    return name.get();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Composeable#getName(ch.o2it.weblounge.common.language.Language)
   */
  public String getName(Language language) {
    return name.get(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Composeable#getName(ch.o2it.weblounge.common.language.Language,
   *      boolean)
   */
  public String getName(Language language, boolean force) {
    return name.get(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Composeable#setComposeable(boolean)
   */
  public void setComposeable(boolean composeable) {
    this.composeable = composeable;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Composeable#isComposeable()
   */
  public boolean isComposeable() {
    return composeable;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Composeable#setRecheckTime(long)
   */
  public void setRecheckTime(long time) {
    if (time < 0)
      throw new IllegalArgumentException("Recheck time must be greater than or equal to zero");
    this.recheckTime = time;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Composeable#getRecheckTime()
   */
  public long getRecheckTime() {
    return recheckTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Composeable#setValidTime(long)
   */
  public void setValidTime(long time) {
    if (time < 0)
      throw new IllegalArgumentException("Valid time must be greater than or equal to zero");
    this.validTime = time;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Composeable#getValidTime()
   */
  public long getValidTime() {
    return validTime;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Composeable#addHTMLHeader(HTMLHeadElement)
   */
  public void addHTMLHeader(HTMLHeadElement header) {
    if (headers == null)
      headers = new HashSet<HTMLHeadElement>();
    headers.add(header);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Composeable#removeHTMLHeader(HTMLHeadElement)
   */
  public void removeHTMLHeader(HTMLHeadElement header) {
    if (headers == null)
      return;
    headers.remove(header);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Composeable#getHTMLHeaders()
   */
  public HTMLHeadElement[] getHTMLHeaders() {
    if (headers != null) {
      return headers.toArray(new HTMLInclude[headers.size()]);
    }
    return new HTMLInclude[] {};
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    if (identifier == null)
      throw new IllegalStateException("Composeable object need an identifier");
    return identifier.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    if (identifier == null)
      throw new IllegalStateException("Composeable object need an identifier");
    if (o instanceof Composeable) {
      Composeable c = (Composeable) o;
      return c.getIdentifier().equals(identifier);
    }
    return false;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.impl.language.LocalizableObject#toString()
   */
  @Override
  public String toString() {
    return identifier;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.impl.language.LocalizableObject#toString(ch.o2it.weblounge.common.language.Language)
   */
  @Override
  public String toString(Language language) {
    return identifier;
  }

}
