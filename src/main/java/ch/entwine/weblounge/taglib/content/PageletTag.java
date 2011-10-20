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

import ch.entwine.weblounge.common.content.Renderer;
import ch.entwine.weblounge.common.impl.content.page.PageletImpl;
import ch.entwine.weblounge.common.impl.language.LocalizableContent;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * <code>RendererTag</code> will embed the given renderer at the current
 * position.
 */
public class PageletTag extends WebloungeTag {

  /** The serial version id */
  private static final long serialVersionUID = 2814154234296626814L;

  /** Logging facility provided by log4j */
  private static final Logger logger = LoggerFactory.getLogger(PageletTag.class);

  /** The module identifier */
  private Module module = null;

  /** The renderer identifier */
  private String rendererId = null;

  /** The pagelet properties */
  private Map<String, String> properties = null;

  /** The elements */
  private LocalizableContent<Map<String, String>> elements = null;

  /**
   * Sets the module identifier. This method throws an exception if no module
   * with the given identifier can be found.
   * 
   * @param module
   *          The module to set.
   */
  public void setModule(String module) throws JspTagException {
    Site site = request.getSite();
    this.module = site.getModule(module);
    if (module == null) {
      String msg = "Module '" + module + "' not found!";
      throw new JspTagException(msg);
    }
  }

  /**
   * Sets the renderer.
   * 
   * @param value
   *          the renderer to be used
   */
  public void setId(String value) {
    this.rendererId = value;
  }

  /**
   * Process the end tag for this instance.
   * 
   * @return either EVAL_PAGE or SKIP_PAGE
   */
  public int doEndTag() throws JspException {
    Language language = request.getLanguage();

    // Get renderer and include in request
    Renderer renderer = module.getRenderer(this.rendererId);
    if (renderer != null) {
      try {
        pageContext.getOut().flush();

        // Create the virtual pagelet, define properties and elements
        PageletImpl pagelet = new PageletImpl(module.getIdentifier(), rendererId);
        if (properties != null) {
          for (Map.Entry<String, String> property : properties.entrySet()) {
            pagelet.addProperty(property.getKey(), property.getValue());
          }
        }
        if (elements != null) {
          for (Language l : elements.languages()) {
            for (Map.Entry<String, String> element : elements.get(l).entrySet()) {
              pagelet.setContent(element.getKey(), element.getValue(), language);
            }
          }
        }

        // Add cache support
        response.addTag("webl:module", pagelet.getModule());
        response.addTag("webl:renderer", pagelet.getIdentifier());

        // Finally, render the pagelet
        request.setAttribute(WebloungeRequest.PAGELET, pagelet);
        renderer.render(request, response);
      } catch (Exception e) {
        logger.warn("Unable to render '" + renderer + "': " + e.getMessage());
      } finally {
        request.removeAttribute(WebloungeRequest.PAGELET);
      }
    }
    return super.doEndTag();
  }

  /**
   * Sets a property value for this pagelet.
   * 
   * @param name
   *          the property name
   * @param value
   *          the property value
   */
  void setProperty(String name, String value) {
    if (properties == null)
      properties = new HashMap<String, String>();
    properties.put(name, value);
  }

  /**
   * Sets an element value for this pagelet.
   * 
   * @param name
   *          the element name
   * @param value
   *          the element value
   * @param language
   *          the language
   */
  void setElement(String name, String value, Language language) {
    if (elements == null)
      elements = new LocalizableContent<Map<String, String>>();
    Map<String, String> e = elements.get(language);
    if (e == null) {
      e = new HashMap<String, String>();
      elements.put(e, language);
    }
    e.put(name, value);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.taglib.WebloungeTag#reset()
   */
  @Override
  protected void reset() {
    super.reset();
    elements = null;
    module = null;
    properties = null;
    rendererId = null;
  }

}
