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

import java.util.StringTokenizer;

/**
 * Simple parser for export attributes in tags. The parser takes the attribute
 * and extracts the variable definitions found there. Supported are the following
 * formats:
 * <ul>
 * 	<li>site</li>
 * 	<li>s=site</li>
 * 	<li>site, u=url, l = language</li>
 * 	<li>site; u=url; l = language</li>
 * 	<li>site u=url l = language</li>
 * </ul>
 */
public class TagVariableDefinitionParser {

	public static TagVariableDefinitions parse(String export) throws ParseException {
		TagVariableDefinitions variables = new TagVariableDefinitions();
		if (export != null) {
			StringTokenizer tok = new StringTokenizer(export, " ,;");
			while (tok.hasMoreTokens()) {
				String varDef = tok.nextToken();
				String[] varDefParts = varDef.split("=");
				TagVariableDefinition variable = null;
				if (varDefParts.length > 1) {
					String alias = varDefParts[0].trim();
					String name = varDefParts[1].trim();
					variable = new TagVariableDefinition(name, alias);
				} else {
					String name = varDefParts[0].trim();
					variable = new TagVariableDefinition(name);
				}
				if (variables.exists(variable.getAlias()))
					throw new ParseException("Variable '" + variable + "' is defined twice!");
				variables.define(variable);
			}
			return variables;
		}
		return null;
	}
	
}