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

package ch.o2it.weblounge.common.impl.language;

import ch.o2it.weblounge.common.language.Language;

import java.util.Locale;

/**
 * This is a special instance of a <code>Language</code>, representing then
 * English language.
 * 
 * @see Language
 */
public final class English extends LanguageImpl {

  /** Serial version uid */
  private static final long serialVersionUID = 6926231984130068388L;

  /** The singleton instance of English */
  private static Language language = null;

  /**
   * Creates an instance of the English language.
   */
  private English() {
    super(Locale.ENGLISH);
  }

  /**
   * Returns the singleton instance of this class.
   * 
   * @return the only instance of this class
   */
  public static Language getInstance() {
    if (language == null) {
      language = new English();
    }
    return language;
  }

}