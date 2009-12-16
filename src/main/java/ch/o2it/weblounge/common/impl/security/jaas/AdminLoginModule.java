/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.security.jaas;

import ch.o2it.weblounge.common.impl.security.SystemRole;
import ch.o2it.weblounge.common.impl.user.SiteAdminImpl;
import ch.o2it.weblounge.common.impl.user.WebloungeAdminImpl;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.SiteAdmin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

/**
 * Implementation of the Weblounge login module, which will login the admin
 * user.
 */
public class AdminLoginModule extends AbstractLoginModule {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(AdminLoginModule.class);

  /**
   * @see ch.o2it.weblounge.core.security.jaas.AbstractLoginModule#checkUserAndPassword()
   */
  protected boolean checkUserAndPassword() throws LoginException {
    Site site = null;
    if (callbackHandler instanceof HttpAuthCallback) {
      site = ((HttpAuthCallback) callbackHandler).getRequest().getSite();
      SiteAdmin siteadmin = site.getAdministrator();
      WebloungeAdminImpl sysadmin = WebloungeAdminImpl.getInstance();

      // Test for site admin
      if (siteadmin.getLogin().equals(username) && !sysadmin.getLogin().equals(username)) {
        if (Arrays.equals(siteadmin.getPassword(), password)) {
          user = siteadmin;
          return true;
        } else {
          throw new FailedLoginException("Wrong password");
        }
      }

      // Test for weblounge super user
      else if (sysadmin.getLogin().equals(username) && !siteadmin.getLogin().equals(username)) {
        if (Arrays.equals(sysadmin.getPassword(), password)) {
          user = sysadmin;
          return true;
        } else {
          throw new FailedLoginException("Wrong password");
        }
      }

      // Special case where siteadmin has same login name than sysadmin
      else if (sysadmin.getLogin().equals(username) && siteadmin.getLogin().equals(username)) {
        if (Arrays.equals(siteadmin.getPassword(), password)) {
          user = siteadmin;
          return true;
        } else if (Arrays.equals(sysadmin.getPassword(), password)) {
          user = sysadmin;
          return true;
        } else {
          throw new FailedLoginException("Wrong password");
        }
      }

      return false;
    } else {
      log_.warn("Admin login module received unknown callback handler!");
      return false;
    }
  }

  /**
   * <p>
   * This method is called if the LoginContext's overall authentication
   * succeeded (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL
   * LoginModules succeeded).
   * <p>
   * If this LoginModule's own authentication attempt succeeded (checked by
   * retrieving the private state saved by the <code>login</code> method), then
   * this method associates a <code>User</code> with the <code>Subject</code>
   * located in the <code>LoginModule</code>. If this LoginModule's own
   * authentication attempted failed, then this method removes any state that
   * was originally saved.
   * 
   * @exception LoginException
   *              if the commit fails.
   * @return true if this LoginModule's own login and commit attempts succeeded,
   *         or false otherwise.
   */
  public boolean commit() throws LoginException {
    if (!succeeded) {
      return false;
    } else {
      if (user instanceof SiteAdminImpl) {
        subject.getPublicCredentials().add(SystemRole.SITEADMIN);
      } else if (user instanceof WebloungeAdminImpl) {
        subject.getPublicCredentials().add(SystemRole.SYSTEMADMIN);
      }
    }
    return super.commit();
  }

  /**
   * Returns a namespace for this login context. The namespace is used to store
   * and identify user profile data in the weblounge database.
   * 
   * @return the login module namespace
   */
  public String getNamespace() {
    return "weblounge-admins";
  }

}