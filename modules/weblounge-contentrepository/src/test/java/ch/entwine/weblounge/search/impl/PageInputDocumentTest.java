/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2014 The Weblounge Team
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

package ch.entwine.weblounge.search.impl;

import static org.junit.Assert.assertEquals;

import ch.entwine.weblounge.common.content.ResourceMetadata;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.impl.content.page.PageReader;
import ch.entwine.weblounge.common.impl.security.AccessRuleImpl;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.impl.site.SiteImpl;
import ch.entwine.weblounge.common.security.Rule;
import ch.entwine.weblounge.common.security.Securable.Order;
import ch.entwine.weblounge.common.security.SystemAction;
import ch.entwine.weblounge.common.site.Site;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/** Test cases for {@link PageInputDocument} */
public class PageInputDocumentTest {

  private static Site site;
  private Page page;

  @BeforeClass
  public static void setUpClass() {
    site = new SiteImpl();
    site.setIdentifier("test");
  }

  @Before
  public void setUp() throws Exception {
    PageReader reader = new PageReader();
    try (InputStream is = getClass().getResourceAsStream("/page.xml")) {
      page = reader.read(is, site);
    }
    
    page.setAllowDenyOrder(Order.AllowDeny);
    page.addAccessRule(new AccessRuleImpl(new UserImpl("test"), SystemAction.READ, Rule.Allow));
  }

  @Test
  public void testResourceMetadata() {
    Map<String, ResourceMetadata<?>> metadata = new PageInputDocument(page).metadata;
    assertEquals(page.getURI().getUID(), metadata.get(IndexSchema.UID).getValue());
    assertEquals(page.getURI().getIdentifier(), metadata.get(IndexSchema.RESOURCE_ID).getValue());
    assertEquals(page.getURI().getPath(), metadata.get(IndexSchema.PATH).getValue());
    assertEquals(page.getURI().getType(), metadata.get(IndexSchema.TYPE).getValue());
    assertEquals(page.getURI().getVersion(), metadata.get(IndexSchema.VERSION).getValue());
  }

}
