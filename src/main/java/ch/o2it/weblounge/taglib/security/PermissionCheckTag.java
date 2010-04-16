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

package ch.o2it.weblounge.taglib.security;

import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.Pagelet;
import ch.o2it.weblounge.common.impl.security.PermissionImpl;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.PermissionSet;
import ch.o2it.weblounge.common.security.Securable;
import ch.o2it.weblounge.taglib.WebloungeTag;

import java.util.StringTokenizer;

import javax.servlet.jsp.JspException;

/**
 * The body of this tag is only evaluated if the user has the given permissions.
 */
public class PermissionCheckTag extends WebloungeTag {

  /** Series version uid */
  private static final long serialVersionUID = -7303582098967319722L;
  
  /** The set of permissions */
  private PermissionSet permissions_ = null;

  /**
   * Constructor for class CheckPermissionTag.
   */
  public PermissionCheckTag() {
    permissions_ = new PermissionSetImpl();
  }

  /**
   * Specifies which permission has to be acquired for the tag body to be
   * displayed. The permission definition must consist of the form
   * <code>context::id</code>.
   * 
   * @param value
   *          the permission
   */
  public void setPermission(String value) {
    Permission p = new PermissionImpl(value);
    permissions_.addPermission(p, PermissionSet.MATCH_ALL);
  }

  /**
   * Specifies a permissionset. The user must own one of these permissions for
   * the tag body to be displayed. The permissions must be provided as a comma
   * separated list of permission definitions, e.g.
   * <code>system::admin, system::editor</code>.
   * 
   * @param value
   *          the permissionset
   */
  public void setOneof(String value) {
    StringTokenizer tok = new StringTokenizer(value, ",; ");
    while (tok.hasMoreTokens()) {
      Permission p = new PermissionImpl(tok.nextToken());
      permissions_.addPermission(p, PermissionSet.MATCH_SOME);
    }
  }

  /**
   * Specifies a permissionset. The user must own all of these permissions for
   * the tag body to be displayed. The permissions must be provided as a coma
   * separated list of permission definitions, e.g.
   * <code>system::admin, system::editor</code>.
   * 
   * @param value
   *          the permission set
   */
  public void setAllof(String value) {
    StringTokenizer tok = new StringTokenizer(value, ",; ");
    while (tok.hasMoreTokens()) {
      Permission p = new PermissionImpl(tok.nextToken());
      permissions_.addPermission(p, PermissionSet.MATCH_ALL);
    }
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    reset();
    return super.doEndTag();
  }

  /**
   * Called when this tag instance is released.
   */
  public void reset() {
    permissions_.clear();
    super.release();
  }

  /**
   * Returns the permissions that have been set by this tag implementation.
   * 
   * @return the permission set
   */
  protected PermissionSet getPermissions() {
    return permissions_;
  }

  /**
   * Returns the secured object or <code>null</code> if no secured object can be
   * found.
   * 
   * @return the secured object
   */
  protected Securable getSecured() {
    if (pageContext.getAttribute(WebloungeRequest.PAGELET) != null)
      return (Pagelet) pageContext.getAttribute(WebloungeRequest.PAGELET);
    else if (pageContext.getAttribute(WebloungeRequest.PAGE) != null)
      return (Page) pageContext.getAttribute(WebloungeRequest.PAGE);
    else
      return null;
  }

}