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

import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;

import org.apache.commons.lang.StringUtils;

import java.util.Date;

/**
 * Default implementation of a resource content.
 */
public class ResourceContentImpl implements ResourceContent {

  /** The content's name */
  protected String filename = null;

  /** The file's mime type */
  protected String mimetype = null;

  /** The file size in bytes */
  protected long size = -1L;

  /** The content's language */
  protected Language language = null;

  /** Creation information */
  protected CreationContext creationCtx = new CreationContext();

  /** True if this is the original content */
  protected boolean isOriginal = false;

  /**
   * Creates a new resource content representation.
   */
  protected ResourceContentImpl() {
  }

  /**
   * Creates a new resource content representation.
   * 
   * @param language
   *          the content language
   * @param name
   *          the content name
   */
  protected ResourceContentImpl(Language language, String name) {
    if (language == null)
      throw new IllegalArgumentException("Language cannot be null");
    this.language = language;
    this.filename = name;
  }

  /**
   * Creates a new resource content representation.
   * 
   * @param language
   *          the content language
   * @param name
   *          the content name
   * @param mimetype
   *          the content's mimetype
   * @param size
   *          the content size in bytes
   */
  protected ResourceContentImpl(Language language, String name,
      String mimetype, long size) {
    if (language == null)
      throw new IllegalArgumentException("Language cannot be null");
    this.language = language;
    this.filename = name;
    this.mimetype = mimetype;
    this.size = size;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceContent#setFilename(java.lang.String)
   */
  public void setFilename(String name) {
    this.filename = name;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceContent#getFilename()
   */
  public String getFilename() {
    return filename;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceContent#setLanguage(ch.entwine.weblounge.common.language.Language)
   */
  public void setLanguage(Language language) {
    if (language == null)
      throw new IllegalArgumentException("Language must not be null");
    this.language = language;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceContent#getLanguage()
   */
  public Language getLanguage() {
    return language;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#setCreationDate(java.util.Date)
   */
  public void setCreationDate(Date date) {
    creationCtx.setCreationDate(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#getCreationDate()
   */
  public Date getCreationDate() {
    return creationCtx.getCreationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#isCreatedAfter(java.util.Date)
   */
  public boolean isCreatedAfter(Date date) {
    return creationCtx.isCreatedAfter(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#setCreator(ch.entwine.weblounge.common.security.User)
   */
  public void setCreator(User user) {
    creationCtx.setCreator(user);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#getCreator()
   */
  public User getCreator() {
    return creationCtx.getCreator();
  }

  /**
   * Sets the creation date and the user who created the content.
   * 
   * @param date
   *          the creation date
   * @param user
   *          the creator
   */
  public void setCreated(Date date, User user) {
    creationCtx.setCreated(user, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceContent#setMimetype(java.lang.String)
   */
  public void setMimetype(String mimetype) {
    if (mimetype == null)
      throw new IllegalArgumentException("Mimetype must not be null");
    this.mimetype = mimetype;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceContent#getMimetype()
   */
  public String getMimetype() {
    return mimetype;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceContent#setSize(long)
   */
  public void setSize(long size) {
    if (size <= 0)
      throw new IllegalArgumentException("Content size must be greater than 0 bytes");
    this.size = size;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceContent#getSize()
   */
  public long getSize() {
    return size;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ResourceContent) {
      ResourceContent c = (ResourceContent) obj;
      if (!StringUtils.trimToEmpty(filename).equals(c.getFilename()))
        return false;
      return language.equals(c.getLanguage());
    }
    return false;
  }

  /**
   * Callback for subclasses that need to add additional information to the file
   * content representation. Implementations should append their data to the
   * <code>StringBuffer</code> and return it once they're done.
   * 
   * @param xml
   *          the string buffer
   * @return the modified string buffer
   */
  protected StringBuffer extendXml(StringBuffer xml) {
    return xml;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceContent#toXml()
   */
  public String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<content language=\"").append(getLanguage().getIdentifier()).append("\">");
    buf.append(creationCtx.toXml());
    if (filename != null)
      buf.append("<filename><![CDATA[").append(filename).append("]]></filename>");
    if (mimetype != null)
      buf.append("<mimetype>").append(mimetype).append("</mimetype>");
    if (size >= 0)
      buf.append("<size>").append(size).append("</size>");
    extendXml(buf);
    buf.append("</content>");
    return buf.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    if (filename != null) {
      return filename;
    } else {
      StringBuffer buf = new StringBuffer(language.toString().toLowerCase());
      buf.append(" content");
      return buf.toString();
    }
  }

}
