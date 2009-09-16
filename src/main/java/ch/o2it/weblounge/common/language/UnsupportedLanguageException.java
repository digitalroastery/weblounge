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
 * This exception is thrown if someone tries to get an unsupported language from
 * the site's <code>LanguageRegistry</code>.
 */
public class UnsupportedLanguageException extends RuntimeException {

  /** Serial version UID */
  private static final long serialVersionUID = -2196419109593080065L;

  /** The unsupported language identifier */
  private String language_;

  /**
   * Constructor for class UnknownLanguageException.
   */
  public UnsupportedLanguageException() {
    super();
  }

  /**
   * Constructor for class UnknownLanguageException.
   * 
   * @param language
   *          the unsupported language identifier
   */
  public UnsupportedLanguageException(String language) {
    super("Language " + language + " not supported!");
  }

  /**
   * Returns the unsupported language identifier.
   * 
   * @return the language identifier
   */
  public String getLanguage() {
    return language_;
  }

}