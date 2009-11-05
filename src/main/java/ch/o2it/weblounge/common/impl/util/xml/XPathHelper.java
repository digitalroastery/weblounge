/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.util.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

/**
 * Utility class to handle and simplify XPath queries.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public class XPathHelper {

  // Logging

  /** the class name, used for the logging facility */
  private final static String className = XPathHelper.class.getName();

  /** Logging facility */
  protected final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Returns the query result or <code>null</code>.
   * @param node
   *          the context node
   * @param xpath
   *          the xpath expression
   * @param processor
   *          the xpath engine
   * 
   * @return the selected string or <code>null</code> if the query didn't yield
   *         a result
   */
  public static String valueOf(Node node, String xpath, XPath processor) {
    return valueOf(node, xpath, true, processor);
  }

  /**
   * Returns the query result or <code>null</code>.
   * @param node
   *          the context node
   * @param xpath
   *          the xpath expression
   * @param defaultValue
   *          the default value
   * @param processor
   *          the xpath engine
   * 
   * @return the selected string or <code>defaultValue</code> if the query
   *         didn't yield a result
   */
  public static String valueOf(Node node, String xpath, String defaultValue,
      XPath processor) {
    String value = valueOf(node, xpath, true, processor);
    return (value != null) ? value : defaultValue;
  }

  /**
   * Returns the query result.
   * @param node
   *          the context node
   * @param xpath
   *          the xpath expression
   * @param nullable
   *          if <code>null</code> should be returned or the empty string in
   *          case of no search result
   * @param processor
   *          the xpath engine
   * 
   * @return the selected string or <code>null</code> / "" if the query didn't
   *         yield a result
   */
  public static String valueOf(Node node, String xpath, boolean nullable,
      XPath processor) {
    if (node == null || processor == null) {
      return nullable ? null : "";
    }
    try {
      String value = processor.evaluate(xpath, node);
      return nullable && value.length() == 0 ? null : value;
    } catch (XPathExpressionException e) {
      log_.warn("Error when selecting '" + xpath + "' from " + node, e);
      return nullable ? null : "";
    }
  }

  /**
   * Returns the query result as a <code>Node</code> or <code>null</code>.
   * 
   * @return the selected string
   */
  public static Node select(Node node, String xpath, XPath processor) {
    if (node == null || processor == null) {
      return null;
    }
    try {
      return (Node) processor.evaluate(xpath, node, XPathConstants.NODE);
    } catch (XPathExpressionException e) {
      log_.warn("Error when selecting '" + xpath + "' from " + node, e);
      return null;
    }
  }

  /**
   * Returns the query result or <code>null</code>.
   * 
   * @return the selected node list
   */
  public static NodeList selectList(Node node, String xpath, XPath processor) {
    if (node == null || processor == null) {
      return null;
    }
    try {
      return (NodeList) processor.evaluate(xpath, node, XPathConstants.NODESET);
    } catch (XPathExpressionException e) {
      log_.warn("Error when selecting '" + xpath + "' from " + node, e);
      return null;
    }
  }

}