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

import ch.o2it.weblounge.common.content.Pagelet;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.util.Templates;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.taglib.ParseException;
import ch.o2it.weblounge.taglib.TagVariableDefinition;
import ch.o2it.weblounge.taglib.TagVariableDefinitionParser;
import ch.o2it.weblounge.taglib.TagVariableDefinitions;
import ch.o2it.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * Tag to provide support for &lt;ifelement&gt; and &lt;ifnotelement&gt; tag
 * implementations.
 */
public abstract class ElementCheckTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = -2050782761487616751L;

  /** Logging facility provided by log4j */
  private final static Logger log_ = LoggerFactory.getLogger(ElementCheckTag.class);

  /** The element identifier */
  private String name = null;

  /** The element language */
  private Language language = null;

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
   * <li><code>element</code> - the element value</li>
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
        log_.error("Error parsing tag variable definitions: " + value);
        throw new JspTagException(ex.getMessage());
      }
    }
    definitions = value;
  }

  /**
   * Sets the element name.
   * 
   * @param value
   *          the element name
   */
  public final void setName(String value) {
    name = value;
  }

  /**
   * Sets the element language. If the language is provided, then the element
   * will be forced with that language and will be treated as non existent, if
   * it is not available in that language.
   * 
   * @param value
   *          the element language
   */
  public final void setLanguage(String value) throws JspException {
    language = LanguageSupport.getLanguage(value);
    if (language == null) {
      throw new JspException("Language '" + value + "' does not exist!");
    }
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
   * This method is called if the start of a <code>&lt;ifelement&gt;</code> tag
   * is found.
   * 
   * @return the corresponding <code>int</code> to skip or include the tag body
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    super.doStartTag();

    pagelet = (Pagelet) request.getAttribute(WebloungeRequest.PAGELET);
    String content = getElement(name);
    if (skip(content)) {
      return SKIP_BODY;
    } else if (content != null) {
      pageContext.setAttribute(name, content);
    }

    // Defined variable export

    if (variables != null) {
      Iterator<TagVariableDefinition> vars = variables.variables();
      while (vars.hasNext()) {
        TagVariableDefinition def = vars.next();
        String name = def.getName();
        String alias = def.getAlias();
        String value = getElement(name);
        if (value == null)
          value = "";
        if (templates)
          Templates.format(value, false, request.getSite());
        pageContext.setAttribute(alias, value);
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
      if (variables != null) {
        Iterator<TagVariableDefinition> vars = variables.variables();
        while (vars.hasNext()) {
          TagVariableDefinition def = vars.next();
          String alias = def.getAlias();
          pageContext.removeAttribute(alias);
        }
      }
    }
    if (name != null)
      pageContext.removeAttribute(name);
    reset();
    super.doEndTag();
    return EVAL_PAGE;
  }

  /**
   * Does the cleanup by resetting the instance variables to their initial
   * values.
   */
  public void reset() {
    name = null;
    language = null;
    pagelet = null;
  }

  /**
   * This method is used to define whether this tag skips on an available or a
   * non-available element.
   * 
   * @param element
   *          the element to check
   * @return <code>true</code> to skip the tag body
   */
  protected abstract boolean skip(String element);

  /**
   * Returns the element value with the given name or the empty string, if no
   * such element exists.
   * 
   * @param name
   *          the element name
   * @return the element
   */
  private String getElement(String name) {
    String element = null;
    if (pagelet != null) {
      if (language != null) {
        element = pagelet.getContent(name, language, true);
      } else {
        element = pagelet.getContent(name, request.getLanguage());
      }
    }
    if (element == null)
      element = "";
    return element;
  }

}