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

package ch.o2it.weblounge.common.language;

/**
 * The <code>LocalizationListener</code> is notified if {@link Localizable}
 * objects are switched to another language.
 * <p>
 * The purpose of this listener is to allow for easy switching of hierarchies of
 * <code>Localizable</code> objects, for example a <code>Page</code>, which
 * could provide multiple versions of its content may contain a localized title
 * field. If the page is now switched to another language, and the title has
 * registered with the page as a <code>LocalizationListener</code>, then the
 * title can easily be switched as well, so a call to the
 * <code>getTitle()</code> method of the page will return the title in the
 * page's current language.
 */
public interface LocalizationListener {

  /**
   * Notification about a language switch to <code>language</code> that was
   * triggered by a request to switch to <code>requestedLanguage</code>. A
   * difference between <code>language</code> and <code>requestedLanguage</code>
   * will usually be due to a <code>Localizable</code> not being able to deliver
   * content in the requested language and therefore switch to either the
   * original language or the default one.
   * 
   * @param language
   *          the language that has been switched to
   * @param requestedLanguage
   *          the language that was originally requested
   */
  void switchedTo(Language language, Language requestedLanguage);

}