/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.taglib.content;

import ch.o2it.weblounge.taglib.ParseException;
import ch.o2it.weblounge.taglib.TagVariableDefinition;
import ch.o2it.weblounge.taglib.TagVariableDefinitionParser;
import ch.o2it.weblounge.taglib.TagVariableDefinitions;

import java.util.Iterator;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 * This class provides runtime information about the <code>ElementTag</code> jsp
 * tag.
 */
public class ElementTagExtraInfo extends TagExtraInfo {

  /**
   * Returns the information on the exported tag variables.
   * 
   * @see javax.servlet.jsp.tagext.TagExtraInfo#getVariableInfo(javax.servlet.jsp.tagext.TagData)
   */
  public VariableInfo[] getVariableInfo(TagData tagData) {
    String definitions = tagData.getAttributeString("define");
    String element = tagData.getAttributeString("name");
    TagVariableDefinitions variables = null;
    int size = (element != null) ? 1 : 0;
    if (definitions != null) {
      try {
        variables = TagVariableDefinitionParser.parse(definitions);
        size += variables.size();
      } catch (ParseException e) {
      }
    }

    VariableInfo[] varinfo = new VariableInfo[size];
    int i = 0;

    // Add default element variable

    if (element != null) {
      varinfo[i++] = new VariableInfo(element, java.lang.String.class.getName(), true, VariableInfo.NESTED);
    }

    // Add defined variables

    if (definitions != null && variables != null) {
      Iterator<TagVariableDefinition> vars = variables.variables();
      while (vars.hasNext()) {
        TagVariableDefinition def = vars.next();
        String alias = def.getAlias();
        varinfo[i++] = new VariableInfo(alias, java.lang.String.class.getName(), true, VariableInfo.NESTED);
      }
    }
    return varinfo;
  }

  /**
   * Returns <code>true</code> if the tag data is valid. This is the case if the
   * <code>define</code> attribute can be parsed without exception.
   * 
   * @see javax.servlet.jsp.tagext.TagExtraInfo#isValid(javax.servlet.jsp.tagext.TagData)
   */
  public boolean isValid(TagData tagData) {
    String definitions = tagData.getAttributeString("define");
    if (definitions != null) {
      try {
        TagVariableDefinitionParser.parse(definitions);
      } catch (ParseException ex) {
        return false;
      }
    }
    return true;
  }
  
}
