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

import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.impl.util.Templates;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.taglib.ParseException;
import ch.entwine.weblounge.taglib.TagVariableDefinition;
import ch.entwine.weblounge.taglib.TagVariableDefinitionParser;
import ch.entwine.weblounge.taglib.TagVariableDefinitions;
import ch.entwine.weblounge.taglib.WebloungeTag;

import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * This tag checks if the property specified by the <code>name</code> parameter
 * exists in the current pagelet. If so, the tag body is executed, otherwise
 * not.
 */
public abstract class PropertyCheckTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = -9052959916607661780L;

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
   * This method is called if the start of a <code>&lt;ifproperty&gt;</code> tag
   * is found.
   * 
   * @return the corresponding <code>int</code> to skip or include the tag body
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    super.doStartTag();

    pagelet = (Pagelet) request.getAttribute(WebloungeRequest.PAGELET);
    String property = getProperty(name);
    if (skip(property)) {
      return SKIP_BODY;
    } else if (property != null) {
      stashAndSetAttribute(name, property);
    }

    // Tag variable export

    if (variables != null) {
      Iterator<TagVariableDefinition> vars = variables.variables();
      while (vars.hasNext()) {
        TagVariableDefinition def = vars.next();
        String name = def.getName();
        String alias = def.getAlias();
        String value = getProperty(name);
        if (value == null)
          value = "";
        if (templates)
          Templates.format(value, false, request.getSite());
        stashAndSetAttribute(alias, value);
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
    removeAndUnstashAttributes();
    return super.doEndTag();
  }

  /**
   * Does the cleanup by resetting the instance variables to their initial
   * values.
   * 
   * @see javax.servlet.jsp.tagext.Tag#release()
   */
  public void reset() {
    name = null;
    pagelet = null;
    templates = false;
  }

  /**
   * This method is used to define wether this tag skips on an available or a
   * non-available property.
   * 
   * @param property
   *          the property to check
   * @return <code>true</code> to skip the tag body
   */
  protected abstract boolean skip(String property);

  /**
   * Returns the property value with the given name or the empty string, if no
   * such property exists.
   * 
   * @param name
   *          the property name
   * @return the property
   */
  private String getProperty(String name) {
    String property = null;
    if (pagelet != null) {
      property = pagelet.getProperty(name);
    }
    if (property == null)
      property = "";
    return property;
  }

}
