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

package ch.entwine.weblounge.taglib.content;

import ch.entwine.weblounge.taglib.ParseException;
import ch.entwine.weblounge.taglib.TagVariableDefinition;
import ch.entwine.weblounge.taglib.TagVariableDefinitionParser;
import ch.entwine.weblounge.taglib.TagVariableDefinitions;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 * This class provides runtime information about the
 * <code>ContentIteratorTag</code> jsp tag.
 */
public class ContentIteratorTagExtraInfo extends TagExtraInfo {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ContentIteratorTagExtraInfo.class);

  /**
   * Returns the information on the exported tag variables.
   * 
   * @see javax.servlet.jsp.tagext.TagExtraInfo#getVariableInfo(javax.servlet.jsp.tagext.TagData)
   */
  public VariableInfo[] getVariableInfo(TagData tagData) {
    List<VariableInfo> varinfo = new ArrayList<VariableInfo>();

    // Add the default variables
    varinfo.add(new VariableInfo(ContentIteratorTagVariables.INDEX, Integer.class.getName(), true, VariableInfo.NESTED));
    varinfo.add(new VariableInfo(ContentIteratorTagVariables.ITERATIONS, Integer.class.getName(), true, VariableInfo.NESTED));

    // Define elements
    String elements = tagData.getAttributeString("elements");
    if (StringUtils.isNotBlank(elements)) {
      try {
        TagVariableDefinitions elementVariables = TagVariableDefinitionParser.parse(elements);
        for (TagVariableDefinition def : elementVariables) {
          String name = def.getAlias() != null ? def.getAlias() : def.getName();
          varinfo.add(new VariableInfo(name, String.class.getName(), true, VariableInfo.NESTED));
        }
      } catch (ParseException e) {
        logger.info("Error parsing element definition '{}': {}", elements, e.getMessage());
      }
    }

    // Define properties
    String properties = tagData.getAttributeString("properties");
    if (StringUtils.isNotBlank(properties)) {
      try {
        TagVariableDefinitions propertyVariables = TagVariableDefinitionParser.parse(properties);
        for (TagVariableDefinition def : propertyVariables) {
          String name = def.getAlias() != null ? def.getAlias() : def.getName();
          varinfo.add(new VariableInfo(name, String.class.getName(), true, VariableInfo.NESTED));
        }
      } catch (ParseException e) {
        logger.info("Error parsing property definition '{}': {}", properties, e.getMessage());
      }
    }

    return varinfo.toArray(new VariableInfo[varinfo.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.TagExtraInfo#isValid(javax.servlet.jsp.tagext.TagData)
   */
  public boolean isValid(TagData tagData) {
    return true;
  }

}
