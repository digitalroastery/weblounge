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

package ch.o2it.weblounge.common.repository;

import java.io.File;
import java.util.Iterator;

import ch.o2it.weblounge.common.content.Modifiable;
import ch.o2it.weblounge.common.content.Publishable;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.Securable;
import ch.o2it.weblounge.common.security.SystemPermission;
import ch.o2it.weblounge.common.url.WebUrl;

/**
 * A repository item is part of the file repository and may be contained in a
 * repository collection. It represents a downloadable item, e. g. a picture or
 * other document.
 */
public interface Resource extends Localizable, Publishable, Modifiable, Securable {

  /** The resource's permissions */
  static final Permission[] permissions = new Permission[] {
      SystemPermission.READ,
      SystemPermission.WRITE,
      SystemPermission.MODIFY,
      SystemPermission.TRANSLATE,
      SystemPermission.MANAGE };

  /**
   * Returns the <code>RepositoryURI</code> that identifies this collection.
   * 
   * @return the uri
   */
  RepositoryURI getURI();

  /**
   * Returns the resource's parent collection.
   * 
   * @return the parent collection
   */
  Collection getParent();

  /**
   * Returns the repository item's name in the given language.
   * 
   * @param language
   *          the language
   * @return the name
   */
  String getName(Language language);

  /**
   * Returns the repository item's description in the given language.
   * 
   * @param the
   *          language
   * @return the description
   */
  String getDescription(Language language);

  /**
   * Returns the physical representation of this item in the repository
   * filesystem.
   * 
   * @param language
   *          the language
   * @return the item file
   */
  File getFile(Language language);

  /**
   * Returns a link to this item.
   * 
   * @return the link
   */
  WebUrl getLink();

  /**
   * Returns the repository item's mime type.
   * 
   * @return language the language
   * @return the mime type
   */
  String getContentType(Language language);

  /**
   * Returns the resource's filesize in bytes, which is equal to the sum of the
   * localized file sizes.
   * 
   * @return the file size
   */
  long getSize();

  /**
   * Returns the item's filesize in bytes.
   * 
   * @return language the language
   * @return the file size
   */
  long getSize(Language language);

  /**
   * Returns an iteration of the items keywords.
   * 
   * @return the keywords
   */
  Iterator<String> keywords();

  /**
   * Method used to implement the visitor pattern. Implementations will call the
   * visiting iterator.
   * 
   * @param visitor
   *          the visiting iterator
   */
  void accept(RepositoryIterator visitor);

}