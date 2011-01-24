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

import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.jsp.JspException;

/**
 * This tag inserts a <code>title</code> meta tag into the head section of an
 * html or jsp page.
 */
public class TitleTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = -2728129601060527783L;

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(TitleTag.class);

  /**
   * Writes the title tag to the output.
   * 
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    Page p = (Page) request.getAttribute(WebloungeRequest.PAGE);
    Language l = getRequest().getLanguage();
    try {
      pageContext.getOut().write(p.getTitle(l));
    } catch (IOException e) {
      logger.warn("Error writing title element to page");
    }
    return super.doEndTag();
  }

}
