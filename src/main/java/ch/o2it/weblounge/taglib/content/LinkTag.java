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

import ch.o2it.weblounge.common.impl.site.SiteImpl;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * This tag provides an implementation for a link into the weblounge system. The
 * link consists mainly of the target site, and the target url.
 */
public class LinkTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = -346125549847501844L;

  /** Logging facility provided by log4j */
  private final static Logger log_ = LoggerFactory.getLogger(LinkTag.class.getName());

  /** Target site */
  private String site = null;

  /** Target path within the site and partition */
  private String path = null;

  /** The target partition */
  private String partition = null;

  /** The target anchor */
  private String anchor = null;

  /** The target */
  private String target = null;

  /**
   * Sets the target partition. The partition is mounted to a certain mountpoint
   * but by referring to the partition, it is irrelevant for the construction of
   * the url where exactly the partition is mounted.
   * 
   * @param partition
   *          the target partition
   */
  public void setPartition(String partition) {
    this.partition = partition;
  }

  /**
   * Sets the path within the specified partition.
   * 
   * @param path
   *          path within the partition
   */
  public final void setPath(String path) {
    this.path = path;
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
   * Sets the site. This parameter is optional. If not specified, the site is
   * beeing extracted from the request.
   * 
   * @param site
   *          The site.
   */
  public final void setSite(String site) {
    this.site = site;
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
    target = target;
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    super.doStartTag();

    try {
      JspWriter writer = pageContext.getOut();
      String link = "";

      // Site
      Site site = null;
      if (site != null) {
        Registry sites = SystemRegistries.get(SiteRegistry.ID);
        site = (SiteImpl) sites.get(site);
        if (site != null) {
          String[] siteUrls = site.getServerNames();
          if (siteUrls.length > 0)
            link = "http://" + siteUrls[0];
        } else {
          log_.warn("Site '" + site + "' (configured in <a> tag) not found!");
          site = request.getSite();
        }
      } else {
        site = request.getSite();
      }

      if (path == null) {
        path = "/";
      }

      // Path
      link = UrlSupport.concat(link, UrlSupport.getLink(site, partition, path));

      // Anchor
      if (anchor != null && anchor.length() > 0)
        link += "#" + anchor;

      String attributes = "";

      // Target
      attributes += (target != null) ? "target=\"" + target + "\"" : "";
      attributes += getStandardAttributes();

      writer.write("<a href=\"" + XMLEncoding.toXML(link) + "\"" + attributes + ">");
      writer.flush();
      return EVAL_BODY_INCLUDE;

    } catch (Throwable t) {
      log_.error("Error when evaluating starting tag: " + t.getMessage());
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
    partition = null;
    path = null;
    site = null;
  }

}
