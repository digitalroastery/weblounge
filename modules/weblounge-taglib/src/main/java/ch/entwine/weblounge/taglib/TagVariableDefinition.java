/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.taglib;

/**
 * Represents an export variable.
 */
public class TagVariableDefinition {

	private String name_ = null;
	
	private String alias_ = null;
	
	public TagVariableDefinition(String name) {
		name_ = name;
	}
	
	public TagVariableDefinition(String name, String alias) {
		this(name);
		alias_ = alias;
	}
	
	/**
	 * @return Returns the alias.
	 */
	public String getAlias() {
		return (alias_ != null) ? alias_ : name_;
	}

	/**
	 * @param alias The alias to set.
	 */
	void setAlias(String alias) {
		alias_ = alias;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name_;
	}

	/**
	 * @param name The name to set.
	 */
	void setName(String name) {
		name_ = name;
	}
	
	public String toString() {
		return getAlias();
	}

}