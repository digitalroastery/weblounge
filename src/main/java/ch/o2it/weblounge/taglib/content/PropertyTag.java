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

import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.impl.util.Templates;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.taglib.ParseException;
import ch.o2it.weblounge.taglib.TagVariableDefinition;
import ch.o2it.weblounge.taglib.TagVariableDefinitionParser;
import ch.o2it.weblounge.taglib.TagVariableDefinitions;
import ch.o2it.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * This tag writes the property with the given name to the page and optionally
 * defines any tag variables.
 */
public class PropertyTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = -4170115524572662846L;

  /** Logging facility provided by log4j */
  private static final Logger logger = LoggerFactory.getLogger(PropertyTag.class);

  /** The property identifier */
  private String name = null;

  /** The pagelet */
  private Pagelet pagelet = null;

  /** The tag variable definitions */
  private String definitions = null;

  /** The parsed variable definitions */
  private TagVariableDefinitions variables = null;

  /** True to process templates */
  private boolean templates = false;

  /**
   * Sets the tag variable definitions. This tag supports the following
   * variable:
   * <ul>
   * <li><code>property</code> - the property value</li>
   * </ul>
   * 
   * @param value
   *          the variable definitions
   * @throws JspException
   *           if the definition cannot be parsed
   */
  public final void setDefine(String value) throws JspException {
    if (definitions == null || !definitions.equals(value)) {
      try {
        variables = TagVariableDefinitionParser.parse(value);
      } catch (ParseException ex) {
        logger.error("Error parsing tag variable definitions: " + value);
        throw new JspTagException(ex.getMessage());
      }
    }
    definitions = value;
  }

  /**
   * Sets the property name.
   * 
   * @param value
   *          the property name
   */
  public final void setName(String value) {
    name = value;
  }

  /**
   * Switches templates processing on or off.
   * 
   * @param value
   *          the templates attribute value
   */
  public final void setTemplates(String value) {
    templates = "true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value);
  }

  /**
   * This method is called if the start of a <code>&lt;property&gt;</code> tag
   * is found.
   * 
   * @return the corresponding <code>int</code> to skip or include the tag body
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    super.doStartTag();

    pagelet = (Pagelet) getRequest().getAttribute(WebloungeRequest.PAGELET);

    // Tag variable export

    if (variables != null) {
      Iterator<TagVariableDefinition> vars = variables.variables();
      while (vars.hasNext()) {
        TagVariableDefinition def = vars.next();
        String name = def.getName();
        String alias = def.getAlias();
        String value = getProperty(name, request.getSite());
        logger.trace("Defining variable '" + alias + "': " + value);
        if (value == null)
          value = "";
        pageContext.setAttribute(alias, value);
      }
    }

    // Print out the specified property

    if (name != null) {
      String property = getProperty(name, request.getSite());
      pageContext.setAttribute(name, property);
      try {
        PrintWriter out = response.getWriter();
        pageContext.getOut().flush();
        out.print(property);
      } catch (IOException e) {
        logger.error("Unable to write to http response");
      }
    }

    return EVAL_BODY_INCLUDE;
  }

  /**
   * Resets the tag variables that have been set up by the tag.
   * 
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    if (variables != null) {
      Iterator<TagVariableDefinition> vars = variables.variables();
      while (vars.hasNext()) {
        TagVariableDefinition def = vars.next();
        String alias = def.getAlias();
        pageContext.removeAttribute(alias);
      }
    }
    if (name != null) {
      pageContext.removeAttribute(name);
    }
    return super.doEndTag();
  }

  /**
   * Does the cleanup by resetting the instance variables to their initial
   * values.
   */
  public void reset() {
    name = null;
    pagelet = null;
  }

  /**
   * Returns the property value with the given name or the empty string, if no
   * such property exists.
   * 
   * @param name
   *          the property name
   * @return the property
   * @param site
   *          the site
   */
  private String getProperty(String name, Site site) {
    String property = null;
    AbstractContentIteratorTag iterator = null;
    if (pagelet != null) {
      iterator = (AbstractContentIteratorTag) findAncestorWithClass(this, AbstractContentIteratorTag.class);
      if (iterator != null) {
        int index = iterator.getIndex(name);
        String[] props = pagelet.getMultiValueProperty(name);
        if (props != null && index < props.length) {
          property = props[index];
        }
      } else {
        property = pagelet.getProperty(name);
      }
    }
    if (property == null)
      property = "";
    if (templates)
      property = Templates.format(property, false, site);
    return property;
  }

}
