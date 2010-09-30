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

package ch.o2it.weblounge.taglib;

import ch.o2it.weblounge.common.impl.request.WebloungeRequestImpl;
import ch.o2it.weblounge.common.impl.request.WebloungeResponseImpl;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Base class for weblounge tags which implements most of the standard
 * <code>HTML 4</code> tag attributes.
 */
public class WebloungeTag extends BodyTagSupport {

  /** Serial version id */
  private static final long serialVersionUID = 1754816467985401658L;

  /** Css class attribute */
  protected String css = null;

  /** Css style attribute */
  protected String style = null;

  /** lang attribute */
  protected String lang = null;

  /** dir (reading direction) attribute */
  protected String dir = null;

  /** Html title */
  protected String title = null;

  /** Html name */
  protected String name = null;

  /** OnClick Event */
  protected String onclick = null;

  /** OnDblClick Event */
  protected String ondblclick = null;

  /** OnMouseDown Event */
  protected String onmousedown = null;

  /** OnMouseUp Event */
  protected String onmouseup = null;

  /** OnMouseMove Event */
  protected String onmousemove = null;

  /** OnMouseOver Event */
  protected String onmouseover = null;

  /** OnMouseOut Event */
  protected String onmouseout = null;

  /** OnKeyDown Event */
  protected String onkeydown = null;

  /** OnKeyPress Event */
  protected String onkeypress = null;

  /** OnKeyUp Event */
  protected String onkeyup = null;

  /** The weblounge request */
  protected WebloungeRequest request = null;

  /** The weblounge response */
  protected WebloungeResponse response = null;

  /**
   * Resets the properties of this tag to default values.
   */
  protected void reset() {
    css = null;
    style = null;
    lang = null;
    dir = null;
    title = null;
    name = null;
    onclick = null;
    ondblclick = null;
    onmousedown = null;
    onmouseup = null;
    onmousemove = null;
    onmouseover = null;
    onmouseout = null;
    onkeydown = null;
    onkeypress = null;
    onkeyup = null;
  }

  /**
   * Sets the standard <code>HTML</code> <code>class</code> attribute.
   * 
   * @param c
   *          the css class
   */
  public void setClass(String c) {
    css = c;
  }

  /**
   * Sets the standard <code>HTML</code> <code>class</code> attribute.
   * 
   * @param c
   *          the css class
   */
  public void setCss(String c) {
    css = c;
  }

  /**
   * Adds the class to the css class attribute.
   * 
   * @param c
   *          the class name
   */
  protected void addCssClass(String c) {
    if (StringUtils.trimToNull(c) == null)
      return;
    if (css == null)
      css = c;
    else if (!css.startsWith(c + " ") && !css.endsWith(" " + c) && !css.contains(" " + c + " "))
      css += " " + c;
  }

  /**
   * Sets the standard <code>HTML</code> <code>style</code> attribute.
   * 
   * @param style
   *          the html style
   */
  public void setStyle(String style) {
    this.style = style;
  }

  /**
   * Sets the standard <code>HTML</code> <code>lang</code> attribute.
   * 
   * @param lang
   *          the language
   */
  public void setLang(String lang) {
    this.lang = lang;
  }

  /**
   * Sets the standard <code>HTML</code> <code>dir</code> attribute.
   * 
   * @param dir
   *          the reading direction
   */
  public void setDir(String dir) {
    this.dir = dir;
  }

  /**
   * Sets the standard <code>HTML</code> <code>title</code> attribute which is
   * used to display tooltips.
   * 
   * @param title
   *          the html title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Sets the standard <code>HTML</code> <code>name</code> attribute.
   * 
   * @param name
   *          the html name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>onclick</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOnclick(String handler) {
    this.onclick = handler;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>onclick</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOnClick(String handler) {
    this.onclick = handler;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>ondblclick</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOndblclick(String handler) {
    this.ondblclick = handler;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>ondblclick</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOnDblClick(String handler) {
    this.ondblclick = handler;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>onmousedown</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOnmousedown(String handler) {
    this.onmousedown = handler;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>onmousedown</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOnMouseDown(String handler) {
    this.onmousedown = handler;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>onmousemove</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOnmousemove(String handler) {
    this.onmousemove = handler;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>onmousemove</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOnMouseMove(String handler) {
    this.onmousemove = handler;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>onmouseout</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOnmouseout(String handler) {
    this.onmouseout = handler;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>onmouseout</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOnMouseOut(String handler) {
    this.onmouseout = handler;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>onmouseover</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOnmouseover(String handler) {
    this.onmouseover = handler;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>onmouseover</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOnMouseOver(String handler) {
    this.onmouseover = handler;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>onmouseup</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOnmouseup(String handler) {
    this.onmouseup = handler;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>onmouseup</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOnMouseUp(String handler) {
    this.onmouseup = handler;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>onkeydown</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOnkeydown(String handler) {
    this.onkeydown = handler;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>onkeydown</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOnKeyDown(String handler) {
    this.onkeydown = handler;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>onkeypress</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOnkeypress(String handler) {
    this.onkeypress = handler;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>onkeypress</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOnKeyPress(String handler) {
    this.onkeypress = handler;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>onkeyup</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOnkeyup(String handler) {
    this.onkeyup = handler;
  }

  /**
   * Sets the standard <code>HTML</code> event <code>onkeyup</code> event
   * handler.
   * 
   * @param handler
   *          the event handler code
   */
  public void setOnKeyUp(String handler) {
    this.onkeyup = handler;
  }

  /**
   * Returns the weblounge request. This method searches the probably wrapped
   * request hierarchy for the original weblounge request.
   * 
   * @return the weblounge request
   */
  public WebloungeRequest getRequest() {
    return unwrapRequest(pageContext.getRequest());
  }

  /**
   * Returns the weblounge response. This method searches the probably wrapped
   * response hierarchy for the original weblounge response.
   * 
   * @return the weblounge response
   */
  public WebloungeResponse getResponse() {
    return unwrapResponse(pageContext.getResponse());
  }

  /**
   * Returns the standard attributes ready to inserted in an html tag.
   * 
   * @return the standard html attributes
   */
  protected Map<String, String> getStandardAttributes() {
    Map<String, String> attributes = new HashMap<String, String>();
    if (css != null)
      attributes.put("class", css);
    if (getId() != null)
      attributes.put("id", getId());
    if (name != null)
      attributes.put("name", name);
    if (style != null)
      attributes.put("style", style);
    if (lang != null)
      attributes.put("lang", lang);
    if (dir != null)
      attributes.put("dir", dir);
    if (title != null)
      attributes.put("title", title);

    // Mouse events
    if (onclick != null)
      attributes.put("onclick", name);
    if (ondblclick != null)
      attributes.put("ondblclick", ondblclick);
    if (onmousedown != null)
      attributes.put("onmousedown", onmousedown);
    if (onmousemove != null)
      attributes.put("onmousemove", onmousemove);
    if (onmouseout != null)
      attributes.put("onmouseout", onmouseout);
    if (onmouseover != null)
      attributes.put("onmouseover", onmouseover);
    if (onmouseup != null)
      attributes.put("onmouseup", onmouseup);

    // Keyboard events
    if (onkeydown != null)
      attributes.put("onkeydown", onkeydown);
    if (onkeypress != null)
      attributes.put("onkeypress", onkeypress);
    if (onkeyup != null)
      attributes.put("onkeyup", onkeyup);

    return attributes;
  }

  /**
   * Overwritten to extract <code>WebloungeRequest</code> and
   * <code>WebloungeResponse</code>.
   * 
   * @see javax.servlet.jsp.tagext.Tag#setPageContext(javax.servlet.jsp.PageContext)
   */
  public void setPageContext(PageContext ctxt) {
    super.setPageContext(ctxt);
    request = unwrapRequest(pageContext.getRequest());
    response = unwrapResponse(pageContext.getResponse());
  }

  /**
   * Returns the map of attributes as a string. If there are no attributes in
   * the map, an empty string is returned.
   * 
   * @param attributes
   *          the attributes as a string
   * @return the attributes ready to be added to a tag
   */
  protected static String attributesToString(Map<String, String> attributes) {
    if (attributes == null || attributes.size() == 0)
      return "";
    StringBuffer buf = new StringBuffer();
    for (Map.Entry<String, String> attribute : attributes.entrySet()) {
      if (buf.length() > 0)
        buf.append(" ");
      buf.append(attribute.getKey());
      buf.append("=\"");
      buf.append(attribute.getValue());
      buf.append("\"");
    }
    return buf.toString();
  }

  /**
   * Extract the wrapped <code>WebloungeResponse</code> from a <code>
	 * ServletResponse</code>.
   * 
   * @param response
   *          the wrapping response
   * @return the wrapped <code>WebloungeResponse</code> or <code>null</code> if
   *         no such response exists
   */
  private static WebloungeResponse unwrapResponse(ServletResponse response) {
    while (response != null) {
      if (response instanceof WebloungeResponse)
        return (WebloungeResponse) response;
      if (!(response instanceof ServletResponseWrapper))
        break;
      response = ((ServletResponseWrapper) response).getResponse();
    }

    // Last resort
    if (response instanceof HttpServletResponse)
      return new WebloungeResponseImpl((HttpServletResponse) response);

    return null;
  }

  /**
   * Extract the wrapped <code>WebloungeRequest</code> from a <code>
	 * ServletRequest</code>
   * .
   * 
   * @param request
   *          the wrapping request
   * @return the wrapped <code>WebloungeRequest</code> or <code>null</code> if
   *         no such response exists
   */
  private static WebloungeRequest unwrapRequest(ServletRequest request) {
    while (request != null) {
      if (request instanceof WebloungeRequest)
        return (WebloungeRequest) request;
      if (!(request instanceof ServletRequestWrapper))
        break;
      request = ((ServletRequestWrapper) request).getRequest();
    }

    // Last resort
    if (request instanceof HttpServletRequest)
      return new WebloungeRequestImpl((HttpServletRequest)request);

    return null;
  }

}