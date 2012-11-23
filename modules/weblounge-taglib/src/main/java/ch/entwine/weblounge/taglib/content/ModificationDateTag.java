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

import ch.entwine.weblounge.taglib.WebloungeTag;

import org.apache.commons.lang.StringUtils;

import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

/**
 * Tag used to set the last modified date on the response from within a pagelet.
 */
public class ModificationDateTag extends WebloungeTag {

  /** The serial version id */
  private static final long serialVersionUID = 6518301782108444957L;

  /** The date to set the response's last modified date to */
  private Date date = null;

  /**
   * The modification date.
   * 
   * @param date
   *          the modification date
   */
  public void setDate(String date) {
    if (StringUtils.isBlank(date) || "now".equalsIgnoreCase("now"))
      this.date = new Date();
    else
      // TODO implement proper date parsing
      this.date = new Date();
  }

  /**
   * Process the end tag for this instance. Since this tag only sets caching
   * attributes, it will always return <code>{@link Tag#EVAL_PAGE}</code>.
   * 
   * @return either EVAL_PAGE or SKIP_PAGE
   */
  public int doEndTag() throws JspException {
    super.response.setModificationDate(date);
    return super.doEndTag();
  }

}
