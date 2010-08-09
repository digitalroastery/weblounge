/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.Securable;
import ch.o2it.weblounge.common.security.SystemPermission;
import ch.o2it.weblounge.common.user.User;

import java.util.Date;

/**
 * A resource is the basic storage unit, also referred to as <tt>content</tt>.
 * Typical incarnations of resources are pages and files.
 */
public interface Resource extends Localizable, Creatable, Modifiable, Publishable, Securable {

  /** The resource's permissions */
  static final Permission[] permissions = new Permission[] {
      SystemPermission.READ,
      SystemPermission.WRITE,
      SystemPermission.TRANSLATE,
      SystemPermission.PUBLISH,
      SystemPermission.MANAGE };
  
  /** Live version of a page */
  long LIVE = 0;

  /** Work version of a page */
  long WORK = 1;

  /**
   * Returns the resource uri.
   * 
   * @return the resource url
   */
  ResourceURI getURI();

  /**
   * Sets the resource type.
   * 
   * @param type
   *          the resource type
   */
  void setType(String type);

  /**
   * Returns the resource type, which is used to include this resource into news
   * lists etc.
   * 
   * @return the resource type
   */
  String getType();

  /**
   * Makes this a promoted resource. Specifying a collection of promoted pages
   * throughout a site will allow for building a sitemap or a list of points of
   * entrance.
   * 
   * @param promoted
   *          <code>true</code> to make this a promoted resource
   */
  void setPromoted(boolean promoted);

  /**
   * Returns <code>true</code> if this resource is considered important enough
   * to include it in a sitemap and similar listings.
   * 
   * @return <code>true</code> if this is a promoted resource
   */
  boolean isPromoted();

  /**
   * Sets this resource to be either included or excluded from the search index.
   * Setting this property to <code>false</code> enables pages that can only be
   * found by people who know the link.
   * 
   * @param index
   *          <code>true</code> to have this resource indexed
   */
  void setIndexed(boolean index);

  /**
   * Returns <code>true</code> if the resource should be added to the search
   * index.
   * 
   * @return <code>true</code> if the resource is indexed
   */
  boolean isIndexed();

  /**
   * Returns <code>true</code> if the resource is locked. A resource is locked
   * if an editor is currently editing the resource and therefore holding the
   * lock.
   * 
   * @return <code>true</code> if this resource is locked
   * @see #getLockOwner()
   */
  boolean isLocked();

  /**
   * Returns the user holding the lock for this resource. This method returns
   * <code>null</code> if the resource is not locked.
   * 
   * @return the user holding the lock for this resource
   */
  User getLockOwner();

  /**
   * Locks this resource for editing by <code>user</code>. This method will
   * throw an <code>IllegalStateException</code> if the resource is already
   * locked by a different user.
   * 
   * @param user
   *          the user locking the resource
   * @throws IllegalStateException
   *           if the resource is already locked by a different user
   */
  void setLocked(User user) throws IllegalStateException;

  /**
   * Removes the editing lock from this resource and returns the user if the
   * resource was locked prior to this call, <code>null</code> otherwise.
   * 
   * @return the user that had locked the resource
   */
  User setUnlocked();

  /**
   * Adds <code>subject</code> to the set of subjects if it is not already
   * contained.
   * <p>
   * The term <tt>subject</tt> is taken from the Dublin Core specification. In
   * the web world, subjects are usually referred to as <tt>tags<tt>.
   * 
   * @param subject
   *          the subject to add
   */
  void addSubject(String subject);

  /**
   * Removes <tt>subject</tt> from the set of subjects.
   * 
   * @param subject
   *          the subject to remove
   */
  void removeSubject(String subject);

  /**
   * Returns <code>true</code> if <code>subject</code> is amongst the pages
   * subjects.
   * 
   * @param subject
   *          the subject
   * @return <code>true</code> if the resource contains the subject
   */
  boolean hasSubject(String subject);

  /**
   * Returns the topics that are defined for this resource. Page topics are also
   * known as tags.
   * 
   * @return the resource topics
   */
  String[] getSubjects();

  /**
   * Returns the resource title in the current language.
   * 
   * @return the content
   * @see #switchTo(Language)
   * @see #getTitle(Language)
   * @see #getTitle(Language, boolean)
   */
  String getTitle();

  /**
   * Returns the resource title in the specified language. If there is no title
   * in that language, then the original title is looked up and returned. If
   * that is not available as well, <code>null</code> is returned.
   * 
   * @param language
   *          the language identifier
   * @return the resource title
   * @see #getTitle()
   * @see #getTitle(Language, boolean)
   */
  String getTitle(Language language);

  /**
   * Returns the resource title in the specified language. If that title can't
   * be found, it will be looked up in the original language (unless
   * <code>force</code> is set to <code>true</code>). If that doesn't produce a
   * result as well, <code>null</code> is returned.
   * 
   * @param language
   *          the title language
   * @param force
   *          <code>true</code> to force the language
   * @return the content
   * @see #getTitle()
   * @see #getTitle(Language)
   */
  String getTitle(Language language, boolean force);

  /**
   * Sets the resource title in the given language.
   * 
   * @param title
   *          the resource title
   * @param language
   *          the language
   */
  void setTitle(String title, Language language);

  /**
   * Returns the resource description in the current language. If there is no
   * description in that language, then the original description is looked up
   * and returned. If that is not available as well, <code>null</code> is
   * returned.
   * 
   * @return the content
   * @see #switchTo(Language)
   * @see #getDescription(Language)
   * @see #getDescription(Language, boolean)
   */
  String getDescription();

  /**
   * Returns the resource description in the specified language. If there is no
   * description in that language, then the original description is looked up
   * and returned. If that is not available as well, <code>null</code> is
   * returned.
   * 
   * @param language
   *          the language identifier
   * @return the resource description
   * @see #getDescription()
   * @see #getDescription(Language, boolean)
   */
  String getDescription(Language language);

  /**
   * Returns the resource description in the specified language. If that
   * description can't be found, it will be looked up in the default language
   * (unless <code>force</code> is set to <code>true</code>). If that doesn't
   * produce a result as well, <code>null</code> is returned.
   * 
   * @param language
   *          the description language
   * @param force
   *          <code>true</code> to force the language
   * @return the content
   * @see #getDescription()
   * @see #getDescription(Language)
   */
  String getDescription(Language language, boolean force);

  /**
   * Sets the resource description in the given language.
   * 
   * @param description
   *          the resource description
   * @param language
   *          the language
   */
  void setDescription(String description, Language language);

  /**
   * Returns the resource coverage in the current language. If there is no
   * coverage in that language, then the original coverage is looked up and
   * returned. If that is not available as well, <code>null</code> is returned.
   * 
   * @return the content
   * @see #switchTo(Language)
   * @see #getCoverage(Language)
   * @see #getCoverage(Language, boolean)
   */
  String getCoverage();

  /**
   * Returns the resource coverage in the specified language. If there is no
   * coverage in that language, then the original coverage is looked up and
   * returned. If that is not available as well, <code>null</code> is returned.
   * 
   * @param language
   *          the language identifier
   * @return the resource coverage
   * @see #getCoverage()
   * @see #getCoverage(Language, boolean)
   */
  String getCoverage(Language language);

  /**
   * Returns the resource coverage in the specified language. If that coverage
   * can't be found, it will be looked up in the default language (unless
   * <code>force</code> is set to <code>true</code>). If that doesn't produce a
   * result as well, <code>null</code> is returned.
   * 
   * @param language
   *          the coverage language
   * @param force
   *          <code>true</code> to force the language
   * @return the content
   * @see #getCoverage()
   * @see #getCoverage(Language)
   */
  String getCoverage(Language language, boolean force);

  /**
   * Sets the resource coverage in the given language.
   * 
   * @param coverage
   *          the resource coverage
   * @param language
   *          the language
   */
  void setCoverage(String coverage, Language language);

  /**
   * Returns the resource rights in the current language.
   * 
   * @return the content
   * @see #switchTo(Language)
   * @see #getRights(Language)
   * @see #getRights(Language, boolean)
   */
  String getRights();

  /**
   * Returns the resource rights in the specified language. If there is no title
   * in that language, then the original resource rights are looked up and
   * returned. If that is not available as well, <code>null</code> is returned.
   * 
   * @param language
   *          the language identifier
   * @return the resource rights
   * @see #getRights()
   * @see #getRights(Language, boolean)
   */
  String getRights(Language language);

  /**
   * Returns the pages rights declaration in the specified language. If no
   * rights declaration can be found in that language, it will be looked up in
   * the default language (unless <code>force</code> is set to <code>true</code>
   * ). If that doesn't produce a result as well, <code>null</code> is returned.
   * 
   * @param language
   *          the rights declaration language
   * @param force
   *          <code>true</code> to force the language
   * @return the content
   * @see #getRights()
   * @see #getRights(Language)
   */
  String getRights(Language language, boolean force);

  /**
   * Sets the pages rights declaration in the given language.
   * 
   * @param rights
   *          the pages rights declaration
   * @param language
   *          the language
   */
  void setRights(String rights, Language language);

  /**
   * Indicates the date of the last modification as well as the person who
   * modified it.
   * 
   * @param user
   *          the user that last modified the object
   * @param date
   *          the date of modification
   */
  void setModified(User user, Date date);

  /**
   * Sets the publisher and the publishing start and end date.
   * 
   * @param publisher
   *          the publisher
   * @param from
   *          publishing start date
   * @param to
   *          publishing end date
   */
  void setPublished(User publisher, Date from, Date to);

  /**
   * Returns an XML representation of this resource header.
   * 
   * @return an XML representation of this resource header
   */
  String toXml();

}
