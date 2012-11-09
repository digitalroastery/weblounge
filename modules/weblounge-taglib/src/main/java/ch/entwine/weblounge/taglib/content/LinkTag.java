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

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.repository.ContentRepository;
import ch.entwine.weblounge.common.request.CacheTag;
import ch.entwine.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * This tag provides an implementation for a link into the weblounge system. The
 * link consists mainly of the target site, and the target url.
 */
public class LinkTag extends WebloungeTag {

  /** serial version id */
  private static final long serialVersionUID = 4109014697686228019L;

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(LinkTag.class);

  /** id of page to link */
  private String resourceid;

  /** link anchor */
  private String anchor;

  /** link target */
  private String target;

  /**
   * Sets the id of the page to link.
   * 
   * @param resourceid
   *          the page identifier
   */
  public final void setResourceid(String resourceid) {
    this.resourceid = resourceid;
  }

  /**
   * Sets the anchor.
   * 
   * @param anchor
   *          anchor on the target page
   */
  public final void setAnchor(String anchor) {
    this.anchor = anchor;
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
    this.target = target;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
   */
  public int doStartTag() throws JspException {
    super.doStartTag();

    try {
      ContentRepository repository = request.getSite().getContentRepository();
      if (repository == null) {
        logger.debug("Content repository is offline");
        response.invalidate();
        return SKIP_BODY;
      }
      
      ResourceURI pageURI = new PageURIImpl(request.getSite(), null, resourceid);
      Page page = (Page) repository.get(pageURI);
      if (page == null) {
        logger.warn("Unable to link to non-existing page {}", pageURI);
        return SKIP_BODY;
      }

      // Add cache tag
      response.addTag(CacheTag.Resource, page.getURI().getIdentifier());
      response.addTag(CacheTag.Url, page.getURI().getPath());
      
      // Adjust modification date
      response.setModificationDate(page.getLastModified());

      String link = page.getURI().getPath();

      // anchor
      if (anchor != null && anchor.length() > 0)
        link += "#" + anchor;

      StringBuffer attributes = new StringBuffer();

      // target
      attributes.append((target != null) ? "target=\"" + target + "\"" : "");

      // Add tag attributes
      for (Map.Entry<String, String> attribute : getStandardAttributes().entrySet()) {
        attributes.append(" ").append(attribute.getKey()).append("=\"").append(attribute.getValue()).append("\"");
      }

      JspWriter writer = pageContext.getOut();
      writer.write("<a href=\"".concat(link).concat("\"").concat(attributes.toString()).concat(">"));
      writer.flush();
      return EVAL_BODY_INCLUDE;

    } catch (Throwable t) {
      logger.error("Error when evaluating starting tag: " + t.getMessage(), t);
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
      logger.warn("Error writing link tag to page ", e);
    }
    return super.doEndTag();
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#release()
   */
  public void reset() {
    resourceid = null;
    anchor = null;
    target = null;
  }

}