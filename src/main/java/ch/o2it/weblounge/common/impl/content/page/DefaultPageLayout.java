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

package ch.o2it.weblounge.common.impl.content.page;

import ch.o2it.weblounge.common.content.page.PageLayout;
import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.language.LocalizableObject;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.user.User;

/**
 * This class represents the default implementation of a {@link PageLayout}. Its
 * implementation is very simple, and it allows any kind of elements to be
 * placed anywhere on the page, edit existing elements, moving them around and
 * even removing them.
 * <p>
 * You may however implement more restrictive layouts by sub-classing this
 * implementation and overwriting <code>allows</code>.
 */
public class DefaultPageLayout extends LocalizableObject implements PageLayout {

  /** Default layout identifier */
  protected String identifier = "default";

  /** The layout name */
  private LocalizableContent<String> name = null;

  /**
   * Creates a new default layout.
   */
  public DefaultPageLayout() {
    name = new LocalizableContent<String>();
  }

  /**
   * Returns the layout identifier. In case of this default layout
   * implementation, this method always returns <tt>default</tt>.
   * 
   * @return the layout identifier
   * @see ch.o2it.weblounge.common.content.page.PageLayout.content.Layout#getLanguage()
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * The default layout allows all elements to be placed anywhere, so this
   * method always returns <code>true</code>.
   * 
   * @param element
   *          the pagelet to be placed
   * @param composer
   *          the composer where the pagelet will be placed
   * @param position
   *          the composer target position
   * @param user
   *          the user
   * @return <code>true</code> if the pagelet is allowed
   * @see PageLayout#accepts(Pagelet, String, int, User)
   */
  public boolean accepts(Pagelet element, String composer, int position,
      User user) {
    return true;
  }

  /**
   * This implementation allows editing of every composer and therefore always
   * returns <code>true</code>.
   * 
   * @param composer
   *          the composer name
   * @param user
   *          the user
   * @return <code>true</code>
   * @see ch.o2it.weblounge.common.content.page.PageLayout#isEditable(java.lang.String,
   *      ch.o2it.weblounge.common.user.User)
   */
  public boolean isEditable(String composer, User user) {
    return true;
  }

  /**
   * This implementation allows editing of every composer and pagelet and
   * therefore always returns <code>true</code>.
   * 
   * @param composer
   *          the composer name
   * @param position
   *          the pagelet position within the composer
   * @param user
   *          the user
   * @see ch.o2it.weblounge.common.content.page.PageLayout#isEditable(java.lang.String,
   *      int, ch.o2it.weblounge.common.user.User)
   */
  public boolean isEditable(String composer, int position, User user) {
    return true;
  }

  /**
   * This implementation allows moving of every pagelet in every composer and
   * therefore always returns <code>true</code>.
   * 
   * @param composer
   *          the composer name
   * @param position
   *          the pagelet position within the composer
   * @param user
   *          the user
   * 
   * @see ch.o2it.weblounge.common.content.page.PageLayout#isMovable(java.lang.String,
   *      int, ch.o2it.weblounge.common.user.User)
   */
  public boolean isMovable(String composer, int position, User user) {
    return true;
  }

  /**
   * This implementation allows removing removing of pagelets and therefore
   * always returns <code>true</code>.
   * 
   * @param composer
   *          the composer name
   * @param position
   *          the pagelet position within the composer
   * @param user
   *          the user
   * @see ch.o2it.weblounge.common.content.page.PageLayout#isRemovable(java.lang.String,
   *      int, ch.o2it.weblounge.common.user.User)
   */
  public boolean isRemovable(String composer, int position, User user) {
    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#compareTo(ch.o2it.weblounge.common.language.Localizable,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public int compareTo(Localizable o, Language l) {
    return name.compareTo(o, l);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return identifier.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PageLayout) {
      return identifier.equals(((PageLayout) obj).getIdentifier());
    }
    return false;
  }

  /**
   * Returns the layout identifier.
   * 
   * @return the layout identifier
   * @see ch.o2it.weblounge.common.impl.language.LocalizableObject#toString()
   */
  public String toString() {
    return identifier;
  }

}