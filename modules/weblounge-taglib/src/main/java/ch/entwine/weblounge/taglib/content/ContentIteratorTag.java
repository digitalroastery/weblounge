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
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.taglib.ParseException;
import ch.entwine.weblounge.taglib.TagVariableDefinition;
import ch.entwine.weblounge.taglib.TagVariableDefinitionParser;
import ch.entwine.weblounge.taglib.TagVariableDefinitions;
import ch.entwine.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;

/**
 * This tag iterates over the values of multiple elements or properties and is
 * intended to be used to loop over tabular data.
 */
public class ContentIteratorTag extends WebloungeTag {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ContentIteratorTag.class);

  /** The serial version id */
  private static final long serialVersionUID = -5705402493357299735L;

  /** The iteration index */
  protected int index = 0;

  /** The number of iterations */
  protected int iterations = -1;

  /** Maximum number of element or property values */
  protected int cardinality = -1;

  /** The minimum number of iterations */
  protected int minOccurs = -1;

  /** The maximum number of iterations */
  protected int maxOccurs = -1;

  /** The pagelet */
  protected Pagelet pagelet = null;

  /** The element names */
  protected TagVariableDefinitions elementNames = null;

  /** The property names */
  protected TagVariableDefinitions propertyNames = null;

  /**
   * Sets the elements that will be used to iterate over. Note that element
   * names be provide as is, including the possibility to specify aliases.
   * 
   * @param value
   *          the regular expression for the element name
   * @throws JspException
   *           if malformed element definitions are provided
   */
  public void setElements(String value) throws JspException {
    try {
      elementNames = TagVariableDefinitionParser.parse(value);
    } catch (ParseException e) {
      throw new JspException(e);
    }
  }

  /**
   * Sets the elements that will be used to iterate over. Note that element
   * names be provide as is, including the possibility to specify aliases.
   * 
   * @param value
   *          the regular expression for the element name
   * @throws JspException
   *           if malformed element definitions are provided
   */
  public void setProperties(String value) throws JspException {
    try {
      propertyNames = TagVariableDefinitionParser.parse(value);
    } catch (ParseException e) {
      throw new JspException(e);
    }
  }

  /**
   * Sets the minimum number of iterations.
   * 
   * @param value
   *          the minimum number of iterations
   */
  public void setMinOccurs(String value) {
    minOccurs = Integer.parseInt(value);
  }

  /**
   * Sets the maximum number of iterations.
   * 
   * @param value
   *          the maximum number of iterations
   */
  public void setMaxOccurs(String value) {
    maxOccurs = Integer.parseInt(value);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
   */
  public int doStartTag() throws JspException {

    // If pagelet is null, then this is the first iteration and the tag needs
    // to be initialized
    if (cardinality == -1) {
      pagelet = (Pagelet) request.getAttribute(WebloungeRequest.PAGELET);

      // Do we have a pagelet?
      if (pagelet == null)
        return SKIP_BODY;

      cardinality = getCardinality(pagelet);

      // Adjust according to maximum number of iterations
      if (maxOccurs > -1)
        iterations = Math.min(cardinality, maxOccurs);
      else
        iterations = cardinality;

      // Adjust according to minimum number of iterations
      if (minOccurs > -1)
        iterations = Math.max(iterations, minOccurs);

      if (iterations == 0)
        return SKIP_BODY;
    }

    stashAndSetAttribute(ContentIteratorTagVariables.ITERATIONS, new Integer(iterations));
    stashAndSetAttribute(ContentIteratorTagVariables.INDEX, new Integer(index));

    defineElementsAndProperties(index);

    return EVAL_BODY_INCLUDE;
  }

  /**
   * Returns the number of data values.
   */
  private int getCardinality(Pagelet pagelet) {

    // Check remaining elements
    if (elementNames != null) {
      Language language = request.getLanguage();
      for (TagVariableDefinition element : elementNames) {
        String name = element.getAlias();
        String[] elementValues = pagelet.getMultiValueContent(name, language);
        if (elementValues == null)
          continue;
        cardinality = Math.max(cardinality, elementValues.length);
      }
    }

    // Check remaining properties
    if (propertyNames != null) {
      for (TagVariableDefinition property : propertyNames) {
        String name = property.getAlias();
        String[] propertyValues = pagelet.getMultiValueProperty(name);
        if (propertyValues == null)
          continue;
        cardinality = Math.max(cardinality, propertyValues.length);
      }
    }

    // Are there enough values to iterate over?
    if (minOccurs >= 0 && minOccurs > cardinality)
      return 0;
    if (maxOccurs >= 0 && maxOccurs >= index)
      return 0;

    // Calculate how many values are left
    return cardinality;
  }

  /**
   * Defines the variables and their current values in the page context
   * depending on the <code>iteration</code> value.
   * 
   * @param iteration
   *          the iteration
   */
  protected void defineElementsAndProperties(int iteration) {
    Language language = request.getLanguage();

    // Define the elements
    for (TagVariableDefinition variable : elementNames) {
      Object value = null;
      String[] values = pagelet.getMultiValueContent(variable.getName(), language);
      if (values != null && iteration < values.length) {
        value = values[iteration];
      }
      String name = variable.getAlias();
      logger.debug("Defining element '{}' as '{}'", name, value != null ? null : "null");
      stashAttribute(name);
      pageContext.setAttribute(name, value);
    }

    // Define the properties
    for (TagVariableDefinition variable : propertyNames) {
      Object value = null;
      String[] values = pagelet.getMultiValueProperty(variable.getName());
      if (values != null && iteration < values.length) {
        value = values[iteration];
      }
      String name = variable.getAlias();
      logger.debug("Defining property '{}' as '{}'", name, value != null ? null : "null");
      stashAttribute(name);
      pageContext.setAttribute(name, value);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.content.AbstractContentIteratorTag#doAfterBody()
   */
  public int doAfterBody() {
    index++;
    if (index >= iterations)
      return SKIP_BODY;

    // Get the current element value
    defineElementsAndProperties(index);

    pageContext.setAttribute(ContentIteratorTagVariables.INDEX, new Integer(index));
    defineElementsAndProperties(index);

    return EVAL_BODY_AGAIN;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.WebloungeTag#doEndTag()
   */
  public int doEndTag() throws JspException {
    removeAndUnstashAttributes();
    return super.doEndTag();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.content.AbstractContentIteratorTag#reset()
   */
  @Override
  protected void reset() {
    super.reset();
    pagelet = null;
    elementNames = null;
    propertyNames = null;
    index = 0;
    iterations = -1;
    cardinality = -1;
    minOccurs = -1;
    maxOccurs = -1;
  }

}
