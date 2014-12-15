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

package ch.entwine.weblounge.kernel.security;

import ch.entwine.weblounge.common.impl.security.PasswordImpl;
import ch.entwine.weblounge.common.impl.security.RoleImpl;
import ch.entwine.weblounge.common.impl.security.SecurityUtils;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.security.DigestType;
import ch.entwine.weblounge.common.security.Password;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.SiteDirectory;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.easymock.EasyMock;
import org.junit.Before;

/**
 * Tests the combined user and role directory service.
 */
public class DirectoryServiceImplTest {

  /** The user and role directory */
  protected DirectoryServiceImpl directory = null;

  /** An organization */
  protected Site site = null;

  /** Login name */
  protected String login = "john";

  /** A user */
  protected User john = null;

  /** Another user */
  protected User johnAlterEgo = null;

  /** A first role */
  protected Role roleA = new RoleImpl("test:role_a");

  /** A second role */
  protected Role roleB = new RoleImpl("test:role_b");

  /** A third role */
  protected Role roleC = new RoleImpl("test:role_c");

  /** the secret password */
  protected Password password = new PasswordImpl("secret", DigestType.plain);

  @Before
  public void setUp() {
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(site.getIdentifier()).andReturn("testsite").anyTimes();
    EasyMock.replay(site);

    User john = new UserImpl(login);
    john.addPublicCredentials(roleA);
    john.addPublicCredentials(roleB);

    User johnAlterEgo = new UserImpl(login);
    johnAlterEgo.addPrivateCredentials(password);
    johnAlterEgo.addPublicCredentials(roleB);
    johnAlterEgo.addPublicCredentials(roleC);

    SiteDirectory directoryA = EasyMock.createNiceMock(SiteDirectory.class);
    EasyMock.expect(directoryA.getIdentifier()).andReturn(site.getIdentifier()).anyTimes();
    EasyMock.expect(directoryA.loadUser((String) EasyMock.anyObject(), (Site) EasyMock.anyObject())).andReturn(john).anyTimes();
    EasyMock.replay(directoryA);

    SiteDirectory directoryB = EasyMock.createNiceMock(SiteDirectory.class);
    EasyMock.expect(directoryB.getIdentifier()).andReturn(site.getIdentifier()).anyTimes();
    EasyMock.expect(directoryB.loadUser((String) EasyMock.anyObject(), (Site) EasyMock.anyObject())).andReturn(johnAlterEgo).anyTimes();
    EasyMock.replay(directoryB);

    SecurityUtils.setSite(site);

    directory = new DirectoryServiceImpl();
    directory.addDirectoryProvider(directoryA);
    directory.addDirectoryProvider(directoryB);
  }

}
