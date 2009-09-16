/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.language;

import ch.o2it.weblounge.common.language.Language;

import java.util.Locale;

/**
 * This is a special instance of <code>Language</code>, representing the French
 * language.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 1.0
 */

public class Francais extends LanguageImpl {

  /** The language identifier */
  public final static String ID = "fr";

  /** The singleton instance of Francais */
  private static Language language_ = null;

  /**
   * Creates an instance of the Francais language.
   */
  private Francais() {
    super(Locale.FRENCH);
  }

  /**
   * Returns the singleton instance of this class.
   * 
   * @return the only instance of this class
   */
  public static Language getInstance() {
    if (language_ == null) {
      language_ = new Francais();
    }
    return language_;
  }

}