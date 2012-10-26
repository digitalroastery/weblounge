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
package ch.entwine.weblounge.common.impl.request;

import static org.junit.Assert.assertEquals;

import ch.entwine.weblounge.common.impl.url.WebUrlImpl;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.site.Action;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

/**
 * Test cases RequestUtilsTest
 */
public class RequestUtilsTest {

  /** The request */
  private WebloungeRequest request = null;

  /** The action */
  private Action action = null;

  /** The path of the request */
  private static final String REQUEST_PATH = "/my/action/my/param/";

  /** The value of the first url param */
  private static final String URL_PARAM_1 = "my";

  /** The value of the second url param */
  private static final String URL_PARAM_2 = "param";

  /** The action mountpoint */
  private static final String ACTION_MOUNTPOINT = "/my/action/";

  @Before
  public void setUp() {
    Site site = EasyMock.createNiceMock(Site.class);
    WebUrl url = new WebUrlImpl(site, REQUEST_PATH);

    request = EasyMock.createNiceMock(WebloungeRequest.class);
    EasyMock.expect(request.getUrl()).andReturn(url).anyTimes();

    action = EasyMock.createNiceMock(Action.class);
    EasyMock.expect(action.getPath()).andReturn(ACTION_MOUNTPOINT).anyTimes();
  }

  @Test
  @Ignore
  public void testGetUrlParams() {
    // TODO fix this test case
    List<String> urlParams = RequestUtils.getUrlParams(request, action);
    assertEquals(URL_PARAM_1, urlParams.get(0));
    assertEquals(URL_PARAM_2, urlParams.get(1));
  }

  /**
   * Test for {@link RequestUtils#decode(java.lang.String, java.lang.String)}
   */
  @Test
  public void testDecode() throws Exception {
    assertEquals("%test", RequestUtils.decode("%test", "utf-8"));
    assertEquals("%test", RequestUtils.decode("%25test", "utf-8"));
  }

}
