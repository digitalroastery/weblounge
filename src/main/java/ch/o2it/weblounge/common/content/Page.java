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

package ch.o2it.weblounge.common.content;

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.Securable;
import ch.o2it.weblounge.common.security.SystemPermission;
import ch.o2it.weblounge.common.user.User;

import java.util.Date;

/**
 * A <code>Page</code> encapsulates all data that is attached to a site url and
 * that can be edited in terms of composers and pagelets. All the content may
 * also be supplied in multiple languages.
 * <p>
 * During lifetime, the page keeps track of creation, modification and
 * publishing processes. Note that a page can exist in many versions, with a few
 * of them being special:
 * <ul>
 * <li>{@link LIVE}: the live version of the page</li>
 * <li>{@link WORK}: the work version of the page</li>
 * </ul>
 */
public interface Page extends Localizable, Creatable, Modifiable, Publishable, Securable {

  /** Live version of a page */
  long LIVE = 0;

  /** Work version of a page */
  long WORK = 1;

  /** The page's permissions */
  static final Permission[] permissions = new Permission[] {
      SystemPermission.READ,
      SystemPermission.WRITE,
      SystemPermission.TRANSLATE,
      SystemPermission.PUBLISH,
      SystemPermission.MANAGE };

  /**
   * Returns the page uri.
   * 
   * @return the page url
   */
  PageURI getURI();

  /**
   * Sets the page type.
   * 
   * @param type
   *          the page type
   */
  void setType(String type);

  /**
   * Returns the page type, which is used to include this page into news lists
   * etc.
   * 
   * @return the page type
   */
  String getType();

  /**
   * Makes this a promoted page. Specifying a collection of promoted pages
   * throughout a site will allow for building a sitemap or a list of points of
   * entrance.
   * 
   * @param promoted
   *          <code>true</code> to make this a promoted page
   */
  void setPromoted(boolean promoted);

  /**
   * Returns <code>true</code> if this page is considered important enough to
   * include it in a sitemap and similar listings.
   * 
   * @return <code>true</code> if this is a promoted page
   */
  boolean isPromoted();

  /**
   * Sets this page to be either included or excluded from the search index.
   * Setting this property to <code>false</code> enables pages that can only be
   * found by people who know the link.
   * 
   * @param index
   *          <code>true</code> to have this page indexed
   */
  void setIndexed(boolean index);

  /**
   * Returns <code>true</code> if the page should be added to the search index.
   * 
   * @return <code>true</code> if the page is indexed
   */
  boolean isIndexed();

  /**
   * Returns <code>true</code> if the page is locked. A page is locked if an
   * editor is currently editing the page and therefore holding the lock.
   * 
   * @return <code>true</code> if this page is locked
   * @see #getLockOwner()
   */
  boolean isLocked();

  /**
   * Returns the user holding the lock for this page. This method returns
   * <code>null</code> if the page is not locked.
   * 
   * @return the user holding the lock for this page
   */
  User getLockOwner();

  /**
   * Locks this page for editing by <code>user</code>. This method will throw an
   * <code>IllegalStateException</code> if the page is already locked by a
   * different user.
   * 
   * @param user
   *          the user locking the page
   * @throws IllegalStateException
   *           if the page is already locked by a different user
   */
  void setLocked(User user) throws IllegalStateException;

  /**
   * Removes the editing lock from this page and returns the user if the page
   * was locked prior to this call, <code>null</code> otherwise.
   * 
   * @return the user that had locked the page
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
   * @return <code>true</code> if the page contains the subject
   */
  boolean hasSubject(String subject);

  /**
   * Returns the topics that are defined for this page. Page topics are also
   * known as tags.
   * 
   * @return the page topics
   */
  String[] getSubjects();

  /**
   * Returns the page title in the current language.
   * 
   * @return the content
   * @see #switchTo(Language)
   * @see #getTitle(Language)
   * @see #getTitle(Language, boolean)
   */
  String getTitle();

  /**
   * Returns the page title in the specified language. If there is no title in
   * that language, then the original title is looked up and returned. If that
   * is not available as well, <code>null</code> is returned.
   * 
   * @param language
   *          the language identifier
   * @return the page title
   * @see #getTitle()
   * @see #getTitle(Language, boolean)
   */
  String getTitle(Language language);

  /**
   * Returns the page title in the specified language. If that title can't be
   * found, it will be looked up in the original language (unless
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
   * Sets the page title in the given language.
   * 
   * @param title
   *          the page title
   * @param language
   *          the language
   */
  void setTitle(String title, Language language);

  /**
   * Returns the page description in the current language. If there is no
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
   * Returns the page description in the specified language. If there is no
   * description in that language, then the original description is looked up
   * and returned. If that is not available as well, <code>null</code> is
   * returned.
   * 
   * @param language
   *          the language identifier
   * @return the page description
   * @see #getDescription()
   * @see #getDescription(Language, boolean)
   */
  String getDescription(Language language);

  /**
   * Returns the page description in the specified language. If that description
   * can't be found, it will be looked up in the default language (unless
   * <code>force</code> is set to <code>true</code>). If that doesn't produce a
   * result as well, <code>null</code> is returned.
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
   * Sets the page description in the given language.
   * 
   * @param description
   *          the page description
   * @param language
   *          the language
   */
  void setDescription(String description, Language language);

  /**
   * Returns the page coverage in the current language. If there is no coverage
   * in that language, then the original coverage is looked up and returned. If
   * that is not available as well, <code>null</code> is returned.
   * 
   * @return the content
   * @see #switchTo(Language)
   * @see #getCoverage(Language)
   * @see #getCoverage(Language, boolean)
   */
  String getCoverage();

  /**
   * Returns the page coverage in the specified language. If there is no
   * coverage in that language, then the original coverage is looked up and
   * returned. If that is not available as well, <code>null</code> is returned.
   * 
   * @param language
   *          the language identifier
   * @return the page coverage
   * @see #getCoverage()
   * @see #getCoverage(Language, boolean)
   */
  String getCoverage(Language language);

  /**
   * Returns the page coverage in the specified language. If that coverage can't
   * be found, it will be looked up in the default language (unless
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
   * Sets the page coverage in the given language.
   * 
   * @param coverage
   *          the page coverage
   * @param language
   *          the language
   */
  void setCoverage(String coverage, Language language);

  /**
   * Returns the page rights in the current language.
   * 
   * @return the content
   * @see #switchTo(Language)
   * @see #getRights(Language)
   * @see #getRights(Language, boolean)
   */
  String getRights();

  /**
   * Returns the page rights in the specified language. If there is no title in
   * that language, then the original page rights are looked up and returned. If
   * that is not available as well, <code>null</code> is returned.
   * 
   * @param language
   *          the language identifier
   * @return the page rights
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
   * Sets the layout that should be applied to this page. The layout controls
   * which pagelets to place into a composer by default, which ones to protect
   * and which ones to allow for editing.
   * 
   * @param layout
   *          the page layout
   */
  void setLayout(String layout);

  /**
   * Returns the identifier of the layout associated with this page.
   * 
   * @return the associated layout
   */
  String getLayout();

  /**
   * Sets the page template. The parameter <code>template</code> represents the
   * identifier of a renderer that is used to render the page.
   * 
   * @param template
   *          the template to use
   */
  void setTemplate(String template);

  /**
   * Returns the identifier of the template that is used to render this page.
   * 
   * @return the template
   */
  String getTemplate();

  /**
   * Adds <code>pagelet</code> as the last pagelet in the specified composer and
   * returns it with an updated {@link PageletURI}.
   * 
   * @param pagelet
   *          the pagelet to add
   * @param composer
   *          the composer to put the pagelet
   * @return the updated pagelet
   */
  Pagelet addPagelet(Pagelet pagelet, String composer);

  /**
   * Adds <code>pagelet</code> as the last pagelet in the specified composer and
   * returns it with an updated {@link PageletURI}.
   * 
   * @param pagelet
   *          the pagelet to add
   * @param composer
   *          the composer to put the pagelet
   * @param index
   *          the position where the pagelets needs to be put
   * @return the updated pagelet
   * @throws IndexOutOfBoundsException
   *           if <code>index</code> is either smaller than <code>zero</code> or
   *           equals or larger than the number of pagelets already contained in
   *           the composer
   */
  Pagelet addPagelet(Pagelet pagelet, String composer, int index)
      throws IndexOutOfBoundsException;

  /**
   * Removes the pagelet at position <code>index</code> from the specified
   * composer and returns it.
   * <p>
   * <b>Note:</b> The uris of all subsequent pagelet will be updated with their
   * new position (<code>current - 1</code>).
   * 
   * @param composer
   *          the composer
   * @param index
   *          position of the pagelet within the composer
   * @return the removed pagelet
   * @throws IndexOutOfBoundsException
   *           if <code>index</code> is either smaller than <code>zero</code> or
   *           equals or larger than the number of pagelets already contained in
   *           the composer
   */
  Pagelet removePagelet(String composer, int index)
      throws IndexOutOfBoundsException;

  /**
   * Returns the pagelets that are contained in the specified composer.
   * 
   * @param composer
   *          the composer identifier
   * @return the pagelets
   */
  Pagelet[] getPagelets(String composer);

  /**
   * Returns the pagelets of the given module and renderer that are contained in
   * the specified composer.
   * 
   * @param composer
   *          the composer identifier
   * @param module
   *          the module identifier
   * @param id
   *          the renderer id
   * @return the pagelets
   */
  Pagelet[] getPagelets(String composer, String module, String id);

  /**
   * Adds a <code>PageContentListener</code> to this page, who will be notified
   * (amongst others) about new, moved, deleted or altered pagelets.
   * 
   * @param listener
   *          the new page content listener
   */
  void addPageContentListener(PageContentListener listener);

  /**
   * Removes a <code>PageContentListener</code> from this page.
   * 
   * @param listener
   *          the page content listener
   */
  void removePageContentListener(PageContentListener listener);

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
   * Returns an XML representation of this page header.
   * 
   * @return an XML representation of this page header
   */
  String toXml();

}