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

package ch.o2it.weblounge.common.resource;

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
 * A <code>RepositoryCollection</code> represents a collection of items in the
 * repository.
 */
public interface Collection extends Localizable, Publishable, Modifiable, Securable {

  /** The collection's permissions */
  static final Permission[] permissions = new Permission[] {
      SystemPermission.READ,
      SystemPermission.WRITE,
      SystemPermission.DELETE,
      SystemPermission.APPEND,
      SystemPermission.LIST,
      SystemPermission.TRANSLATE,
      SystemPermission.MANAGE };

  /**
   * Returns the <code>RepositoryURI</code> that identifies this collection.
   * 
   * @return the uri
   */
  RepositoryURI getURI();

  /**
   * Returns the repository collection's name in the given language.
   * 
   * @return language the language
   * @return the name
   */
  String getName(Language language);

  /**
   * Returns the repository collection's description in the given language.
   * 
   * @return language the language
   * @return the description
   */
  String getDescription(Language language);

  /**
   * Returns the physical representation of this collection in the local
   * filesystem.
   * 
   * @return the collection directory
   */
  File getFile();

  /**
   * Returns a link to this collection.
   * 
   * @return the link
   */
  WebUrl getLink();

  /**
   * Returns an iteration of all subcollections.
   * 
   * @return the subcollections
   */
  Iterator<Collection> collections();

  /**
   * Returns <code>true</code> if the collection contains an collection named
   * <code>localName</code>.
   * 
   * @param localName
   *          the collection name
   * @return <code>true</code> if the collection is contained
   */
  boolean containsCollection(String localName);

  /**
   * Returns the collection named <code>localName</code> or <code>null</code> if
   * no suche collection exists.
   * 
   * @param localName
   *          the collection name
   * @return the collection
   */
  Collection getCollection(String localName);

  /**
   * Returns the number of collections that are directly contained in this
   * collection.
   * 
   * @return the number of subcollections
   */
  int getCollectionCount();

  /**
   * Lists all resources contained in the current collection.
   * 
   * @return the resources
   */
  Iterator<Resource> resources();

  /**
   * Returns <code>true</code> if the collection contains an collection named
   * <code>localName</code>.
   * 
   * @param localName
   *          the collection name
   * @return <code>true</code> if the collection is contained
   */
  boolean containsResource(String localName);

  /**
   * Returns the collection named <code>localName</code> or <code>null</code> if
   * no suche collection exists.
   * 
   * @param localName
   *          the collection name
   * @return the collection
   */
  Resource getResource(String localName);

  /**
   * Returns the number of items that are directly contained in the collection.
   * 
   * @return the number of resources
   */
  int getResourceCount();

  /**
   * Returns the size of this collection, including subcollections and their
   * items in bytes.
   * 
   * @return the collection's size
   */
  long getSize();

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