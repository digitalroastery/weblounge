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

package ch.o2it.weblounge.common.page;

import ch.o2it.weblounge.common.language.Localizable;

/**
 * A <code>Layout</code> enforces the placement of certain elements
 * on the page.
 */
public interface Layout extends Localizable {

	/**
	 * Returns the layout identifier.
	 * 
	 * @return the layout identifier
	 */
	String getIdentifier();
	
	/**
	 * Returns <code>true</code> if the specified element may be placed
	 * inside composer <code>composer</code> at position <code>position</code>,
	 * false otherwise.
	 * 
	 * @param element the element to place
	 * @param composer the composer name
	 * @param position the position to place the element
	 * @return <code>true</code> if the element may be placed as specified
	 */
	boolean allows(Pagelet element, String composer, int position);

}