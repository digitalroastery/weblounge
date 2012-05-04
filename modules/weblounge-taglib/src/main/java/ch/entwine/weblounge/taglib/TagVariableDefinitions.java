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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Container for a set of export variable definitions.
 */
public class TagVariableDefinitions implements Iterable<TagVariableDefinition> {

	/** The alias - definition mapping */
	private Map<String, TagVariableDefinition> variables_ = null;

	/** The aliases */
	private List<String> aliases_ = null;
	
	/**
	 * Creates a new and empty set of variable definitions.
	 */
	public TagVariableDefinitions() {
		variables_ = new HashMap<String, TagVariableDefinition>();
		aliases_ = new ArrayList<String>();
	}

	/**
	 * Defines the given variable.
	 * 
	 * @param variable the tag variable
	 */
	void define(TagVariableDefinition variable) {
		variables_.put(variable.getName(), variable);
		aliases_.add(variable.getAlias());
	}
	
	/**
	 * Returns the variable definition for the given name.
	 * 
	 * @param variableName name of the variable
	 * @return the variable definition
	 */
	public TagVariableDefinition get(String variableName) {
		return variables_.get(variableName);
	}
	
	/**
	 * Returns the alias for the given name.
	 * 
	 * @param name the variable name
	 * @return the variable alias
	 */
	public String getAlias(String name) {
		TagVariableDefinition var = variables_.get(name);
		return (var != null) ? var.getAlias() : null;
	}
	
	/**
	 * Returns <code>true</code> if a variable with name <code>name</code>
	 * exists.
	 * 
	 * @param name the variable nam
	 * @return <code>true</code> if the variable exists
	 */
	public boolean exists(String name) {
		return variables_.get(name) != null;
	}
	
	/**
	 * Returns an iteration of variable definitions.
	 * 
	 * @return the variable definitions
	 */
	public Iterator<TagVariableDefinition> variables() {
		return variables_.values().iterator();
	}
	
	/**
	 * Returns the number of variable definitions.
	 * 
	 * @return the number of defined variables
	 */
	public int size() {
		return variables_.size();
	}

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Iterable#iterator()
   */
  public Iterator<TagVariableDefinition> iterator() {
    return variables_.values().iterator();
  }
	
}