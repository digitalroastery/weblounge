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

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.taglib.ComposerTagSupport;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

/**
 * <code>ComposerTag</code> implements the handler for <code>module</code> tags
 * embedded in a jsp file. The handler will request the specified module to
 * return some resources using the requested view.
 */
public class ComposerTag extends ComposerTagSupport {

  /** Serial version uid */
  private static final long serialVersionUID = 3832079623323702494L;

  /** True if the composer should not be editable */
  protected boolean isLocked = false;

  /**
   * Sets the composer to a locked state, preventing editing at all.
   * 
   * @param value
   *          <code>true</code> to lock the composer
   */
  public void setLocked(String value) {
    isLocked = ConfigurationUtils.isTrue(value);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.taglib.ComposerTagSupport#beforeComposer(javax.servlet.jsp.JspWriter)
   */
  @Override
  protected void beforeComposer(JspWriter writer) throws IOException {
    User user = request.getUser();
    long version = request.getVersion();
    Page targetPage = getTargetPage();
    Page contentPage = getContentProvider();

    boolean isLocked = targetPage.isLocked();
    boolean isLockedByCurrentUser = isLocked && user.equals(targetPage.getLockOwner());
    boolean isWorkVersion = version == Resource.WORK;
    boolean allowContentInheritance = !isLockedByCurrentUser && !isWorkVersion;
    
    // Enable / disable content inheritance for this composer
    setInherit(allowContentInheritance);
    
    // Mark inherited composer and ghost content in locked work mode
    if (isWorkVersion && isLockedByCurrentUser) {
      if (allowContentInheritance)
        addCssClass(CLASS_INHERIT_CONTENT);
      if (!targetPage.equals(contentPage))
        addCssClass(CLASS_GHOST_CONTENT);
    }

    // Let the default implementation kick in
    super.beforeComposer(writer);
    
    // Add first handle
    // if (version == Page.WORK && isLockedByCurrentUser) {
    // request.setAttribute(WebloungeRequest.COMPOSER, composer);
    // PageletEditorTag editorTag = new PageletEditorTag();
    // editorTag.showPageletEditor(getRequest(), getResponse(), writer);
    // writer.flush();
    // }

  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.taglib.ComposerTagSupport#beforePagelet(ch.o2it.weblounge.common.content.page.Pagelet, int, javax.servlet.jsp.JspWriter)
   */
  @Override
  protected int beforePagelet(Pagelet pagelet, int position, JspWriter writer)
      throws IOException {

    // Start editing support
    // if (version == Page.WORK && isLockedByCurrentUser) {
    // writer.println("<div class=\"pagelet\">");
    // writer.flush();
    // }

    return super.beforePagelet(pagelet, position, writer);
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.taglib.ComposerTagSupport#afterPagelet(ch.o2it.weblounge.common.content.page.Pagelet, int, javax.servlet.jsp.JspWriter)
   */
  @Override
  protected void afterPagelet(Pagelet pagelet, int position, JspWriter writer)
      throws IOException {

    // If user is not editing this page, then we are finished with
    // the current pagelet.
    // finally {
    // if (version == Page.WORK && isLockedByCurrentUser &&
    // request.getAttribute(PageletEditorTag.ID) == null) {
    // request.setAttribute(WebloungeRequest.PAGE, targetPage);
    // request.setAttribute(WebloungeRequest.PAGELET, pagelet);
    // request.setAttribute(WebloungeRequest.COMPOSER, composer);
    // PageletEditorTag editorTag = new PageletEditorTag();
    // editorTag.showPageletEditor(getRequest(), getResponse(), writer);
    // writer.println("</div>");
    // }
    // }

    // Remove temporary request attributes
    // request.removeAttribute(PageletEditorTag.ID);

    super.afterPagelet(pagelet, position, writer);
  }
  
}
