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

package ch.entwine.weblounge.common.impl.security;

import static ch.entwine.weblounge.common.security.Securable.Order.AllowDeny;
import static ch.entwine.weblounge.common.security.SystemAction.READ;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.security.User;

import org.junit.Test;

public class WebloungePermissionUtilsTest {

  private static final User systemAdmin = new WebloungeAdminImpl("admin");
  private static final User anonymous = new Guest();

  @Test
  public void testCheckResourceReadPermission() throws Exception {
    Page publicPage = createMock(Page.class);
    expect(publicPage.getAllowDenyOrder()).andStubReturn(AllowDeny);
    expect(publicPage.isAllowed(READ, systemAdmin)).andStubReturn(true);
    expect(publicPage.isAllowed(READ, anonymous)).andStubReturn(true);
    replay(publicPage);

    WebloungePermissionUtils.checkResourceReadPermission(systemAdmin, publicPage);
    WebloungePermissionUtils.checkResourceReadPermission(anonymous, publicPage);
  }

}