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
 * Exception that will be thrown if no default language can be determined.
 * WebLounge is depended on a default language, provided in the community.xml
 * configuration file.
 */
public class UnknownLanguageException extends RuntimeException {

  /** Serial version UID */
  private static final long serialVersionUID = 9164162600017258853L;

  /** The unknown language identifier */
  private String language_;

  /**
   * Constructor for class UnknownLanguageException.
   */
  public UnknownLanguageException() {
    super();
  }

  /**
   * Constructor for class UnknownLanguageException.
   * 
   * @param language
   *          the unknown language identifier
   */
  public UnknownLanguageException(String language) {
    super("Language " + language + " not found!");
    language_ = language;
  }

  /**
   * Returns the unknown language identifier.
   * 
   * @return the language identifier
   */
  public String getLanguage() {
    return language_;
  }

}