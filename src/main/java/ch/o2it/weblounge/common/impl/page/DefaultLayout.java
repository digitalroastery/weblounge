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

package ch.o2it.weblounge.common.impl.page;

import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.language.LocalizableObject;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.page.Layout;
import ch.o2it.weblounge.common.page.Pagelet;

/**
 * This class represents the default implementation of a {@link Layout}.
 * Its implementation is very simple, and it allows any kind of elements
 * to be placed anywhere on the page.
 * <p>
 * You may however implement more restrictive layouts by subclassing
 * this implementation and overwriting <code>allows</code>.
 */
public class DefaultLayout extends LocalizableObject implements Layout {

	/** Default layout identifier */
	protected String identifier = "default";
	
  /** The layout name */
  LocalizableContent<String> name = null;
	
	/**
	 * Creates a new default layout.
	 */
	public DefaultLayout() {
	  name = new LocalizableContent<String>();
	}

	 /**
   * Returns the layout identifier. In case of this default layout implementation,
   * this method always returns <tt>default</tt>.
   * 
   * @return the layout identifier
   * @see ch.o2it.weblounge.core.content.Layout#getIdentifier()
   */
  public String getIdentifier() {
    return identifier;
  }

	/**
	 * The default layout allows all elements to be placed anywhere, so this
	 * method always returns <code>true</code>.
	 * 
	 * @param element the pagelet to be placed
	 * @param composer the composer where the pagelet will be placed
	 * @param position the composer target position
	 * @return <code>true</code> if the pagelet is allowed
	 * @see ch.o2it.weblounge.core.content.Layout#allows(ch.o2it.weblounge.api.content.Pagelet, java.lang.String, int)
	 */
	public boolean allows(Pagelet element, String composer, int position) {
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

}