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
import ch.o2it.weblounge.common.impl.content.page.PageURIImpl;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryFactory;
import ch.o2it.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * This tag provides an implementation for a link into the weblounge system. The
 * link consists mainly of the target site, and the target url.
 * 
 * @author Tobias Wunden <tobias.wunden@o2it.ch>
 * @version 1.0
 */

public class LinkTag extends WebloungeTag {

  /** serial version id */
  private static final long serialVersionUID = 4109014697686228019L;

  /** id of page to link */
  private String resourceid_;

  /** link anchor */
  private String anchor_;

  /** link target */
  private String target_;

  // Logging

  /** the logging facility provided by log4j */
  private static final Logger log = LoggerFactory.getLogger(LinkTag.class);

  /**
   * Sets the id of the page to link.
   * 
   * @param page
   *          id
   */
  public final void setResourceid(String resourceid) {
    resourceid_ = resourceid;
  }

  /**
   * Sets the anchor.
   * 
   * @param anchor
   *          anchor on the target page
   */
  public final void setAnchor(String anchor) {
    anchor_ = anchor;
  }

  /**
   * Sets the target. Target specifies the link target, which is one of
   * <ul>
   * <li>blank_</li>
   * <li>parent_</li>
   * <li>self_</li>
   * <li>top_</li>
   * </ul>
   * 
   * @param target
   *          the target
   */
  public final void setTarget(String target) {
    target_ = target;
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    super.doStartTag();

    try {
      ContentRepository repo = ContentRepositoryFactory.getRepository(request.getSite());
      Page page = (Page) repo.get(new PageURIImpl(request.getSite(), null, resourceid_));
      String link = page.getURI().getPath();

      // anchor
      if (anchor_ != null && anchor_.length() > 0)
        link += "#" + anchor_;

      String attributes = "";

      // target
      attributes += (target_ != null) ? "target=\"" + target_ + "\"" : "";
      attributes += getStandardAttributes();

      JspWriter writer = pageContext.getOut();
      writer.write("<a href=\"".concat(link).concat("\"").concat(attributes).concat(">"));
      writer.flush();
      return EVAL_BODY_INCLUDE;

    } catch (Throwable t) {
      log.error("Error when evaluating starting tag: " + t.getMessage());
      return SKIP_BODY;
    }
  }

  /**
   * Writes the ending tag to the output, which is a <code>&lt;/a&gt;</code>.
   * 
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    JspWriter writer = pageContext.getOut();
    try {
      writer.write("</a>");
      writer.flush();
    } catch (IOException e) {
    }
    reset();
    super.doEndTag();
    return EVAL_PAGE;
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#release()
   */
  public void reset() {
    resourceid_ = null;
    anchor_ = null;
    target_ = null;
  }

}