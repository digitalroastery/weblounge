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

import java.net.URL;

/**
 * Resource content represents the actual content of a resource.
 */
public interface ResourceContent extends Creatable {

  /**
   * Sets the resource name.
   * 
   * @param name
   *          the name
   */
  void setFilename(String name);

  /**
   * Returns the resource name. Depending on the type of resource, this is
   * likely a filename.
   * 
   * @return the name
   */
  String getFilename();

  /**
   * Sets the identifier that was used when the content was initially acquired.
   * 
   * @param source
   *          the source
   */
  void setSource(String source);

  /**
   * Returns the resource content's original identifier.
   * 
   * @return the source
   */
  String getSource();

  /**
   * Sets the resource content's external location.
   * 
   * @param location
   *          the external location
   */
  void setExternalLocation(URL location);

  /**
   * Returns the resource content's external location.
   * 
   * @return the external location
   */
  URL getExternalLocation();

  /**
   * Returns the name of the author of this resource
   * 
   * @return the author's name
   */
  String getAuthor();

  /**
   * Set the name of the author
   * 
   * @param author
   *          the author to set
   */
  void setAuthor(String author);

  /**
   * Sets the content mime type.
   * 
   * @param mimetype
   *          the mime type
   */
  void setMimetype(String mimetype);

  /**
   * Returns the content type.
   * 
   * @return the mime type
   */
  String getMimetype();

  /**
   * Sets the content size in bytes.
   * 
   * @param size
   *          the size
   */
  void setSize(long size);

  /**
   * Returns the content size in bytes.
   * 
   * @return the content size
   */
  long getSize();

  /**
   * Sets the content language.
   * 
   * @param language
   *          the language
   */
  void setLanguage(Language language);

  /**
   * Returns the content language.
   * 
   * @return the language
   */
  Language getLanguage();

  /**
   * Returns an xml representation of this content.
   * 
   * @return the xml representation
   */
  String toXml();

}
