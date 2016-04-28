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

package ch.entwine.weblounge.common.security;

import static ch.entwine.weblounge.common.security.SystemAction.READ;
import static ch.entwine.weblounge.common.security.SystemAction.WRITE;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import org.junit.Assert;
import org.junit.Test;

import java.util.SortedSet;

public class PermissionExceptionTest {

  @Test
  public void testExceptionMessage() {
    User user = createMock(User.class);
    expect(user.getLogin()).andStubReturn("thomas.graf");
    replay(user);

    PermissionException noReadPermEx = new PermissionException(user, READ, new TestSecurable());
    PermissionException noWritePermEx = new PermissionException(user, WRITE, new TestSecurable());

    Assert.assertEquals("User 'thomas.graf' does not have permission 'weblounge:read' on 'page:3C9D1C88-0C8D-11E6-AAF9-AB2752357C51'", noReadPermEx.getMessage());
    Assert.assertEquals("User 'thomas.graf' does not have permission 'weblounge:write' on 'page:3C9D1C88-0C8D-11E6-AAF9-AB2752357C51'", noWritePermEx.getMessage());
  }

  /**
   * Non-functional implementation of {@code Securable} which overrides
   * {@code toString()}.
   */
  private static final class TestSecurable implements Securable {

    @Override
    public String toString() {
      return "page:3C9D1C88-0C8D-11E6-AAF9-AB2752357C51";
    }

    @Override
    public void setOwner(User owner) {

    }

    @Override
    public User getOwner() {
      return null;
    }

    @Override
    public boolean isDefaultAccess() {
      return false;
    }

    @Override
    public void setAllowDenyOrder(Securable.Order order) {

    }

    @Override
    public Securable.Order getAllowDenyOrder() {
      return null;
    }

    @Override
    public void addAccessRule(AccessRule rule) {

    }

    @Override
    public boolean isAllowed(Action action, Authority authority) {
      return false;
    }

    @Override
    public boolean isDenied(Action action, Authority authority) {
      return false;
    }

    @Override
    public Action[] getActions() {
      return new Action[0];
    }

    @Override
    public SortedSet<AccessRule> getAccessRules() {
      return null;
    }

    @Override
    public void addSecurityListener(SecurityListener listener) {

    }

    @Override
    public void removeSecurityListener(SecurityListener listener) {

    }
  }

}