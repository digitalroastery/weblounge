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

package ch.entwine.weblounge.common.impl.content;

import ch.entwine.weblounge.common.Times;
import ch.entwine.weblounge.common.content.Composeable;
import ch.entwine.weblounge.common.content.page.HTMLHeadElement;
import ch.entwine.weblounge.common.site.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * Base implementation for composeable objects like page templates or images.
 */
public class GeneralComposeable implements Composeable {

  /** Object identifier */
  protected String identifier = null;

  /** Composeable flag */
  protected boolean composeable = true;

  /** Milliseconds until validity check is recommended */
  protected long recheckTime = Times.MS_PER_HOUR;

  /** Milliseconds until content using this object becomes invalid */
  protected long validTime = Times.MS_PER_WEEK;

  /** Name of this composeable */
  protected String name = null;

  /** The execution environment */
  protected Environment environment = Environment.Production;

  /** HTML head elements */
  protected List<HTMLHeadElement> headers = null;

  /**
   * Creates a new composeable instance with a recheck time of a day and a valid
   * time of a week.
   */
  protected GeneralComposeable() {
    this(null, Times.MS_PER_HOUR, Times.MS_PER_WEEK);
  }

  /**
   * Creates a new composeable instance with a recheck time of a day and a valid
   * time of a week.
   * 
   * @param identifier
   *          the identifier
   */
  protected GeneralComposeable(String identifier) {
    this(identifier, Times.MS_PER_HOUR, Times.MS_PER_WEEK);
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
    this.recheckTime = recheckTime;
    this.validTime = validTime;
    this.identifier = identifier;
    this.headers = new ArrayList<HTMLHeadElement>();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Composeable#setIdentifier(java.lang.String)
   */
  public void setIdentifier(String identifier) {
    if (identifier == null)
      throw new IllegalArgumentException("Identifier cannot be null");
    this.identifier = identifier;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Composeable#getIdentifier()
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Composeable#setName(java.lang.String)
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Composeable#getName()
   */
  public String getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Composeable#setComposeable(boolean)
   */
  public void setComposeable(boolean composeable) {
    this.composeable = composeable;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Composeable#isComposeable()
   */
  public boolean isComposeable() {
    return composeable;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Composeable#setClientRevalidationTime(long)
   */
  public void setClientRevalidationTime(long time) {
    if (time < 0)
      throw new IllegalArgumentException("Recheck time must be greater than or equal to zero");
    this.recheckTime = time;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Composeable#getRecheckTime()
   */
  public long getRecheckTime() {
    return recheckTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Composeable#setCacheExpirationTime(long)
   */
  public void setCacheExpirationTime(long time) {
    if (time < 0)
      throw new IllegalArgumentException("Valid time must be greater than or equal to zero");
    this.validTime = time;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Composeable#getValidTime()
   */
  public long getValidTime() {
    return validTime;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Composeable#addHTMLHeader(HTMLHeadElement)
   */
  public void addHTMLHeader(HTMLHeadElement header) {
    if (!headers.contains(header))
      headers.add(header);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Composeable#removeHTMLHeader(HTMLHeadElement)
   */
  public void removeHTMLHeader(HTMLHeadElement header) {
    headers.remove(header);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Composeable#getHTMLHeaders()
   */
  public HTMLHeadElement[] getHTMLHeaders() {
    return headers.toArray(new HTMLHeadElement[headers.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    if (identifier == null)
      throw new IllegalStateException("Composeable object needs an identifier");
    return identifier.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    if (identifier == null)
      throw new IllegalStateException("Composeable object needs an identifier");
    if (o instanceof Composeable) {
      Composeable c = (Composeable) o;
      return c.getIdentifier().equals(identifier);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.language.LocalizableObject#toString()
   */
  @Override
  public String toString() {
    return identifier;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Composeable#setEnvironment(ch.entwine.weblounge.common.site.Environment)
   */
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

}
