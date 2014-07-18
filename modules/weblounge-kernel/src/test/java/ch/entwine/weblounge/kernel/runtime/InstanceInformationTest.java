/*
 * Weblounge: Web Content Management System Copyright (c) 2014 The Weblounge
 * Team http://entwinemedia.com/weblounge
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package ch.entwine.weblounge.kernel.runtime;

import static org.junit.Assert.assertEquals;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

/**
 * Tests for class {@link InstanceInformation}
 */
public class InstanceInformationTest {

  private static final String OPT_INSTANCE_NAME = "ch.entwine.weblounge.name";
  
  private InstanceInformation service;
  
  @Before
  public void setUp() {
    service = new InstanceInformation();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testActivateWithNullContext() {
    service.activate(null);
  }
  
  @Test
  public void testActivate() {
    BundleContext bundleContext = createNiceMock(BundleContext.class);
    expect(bundleContext.getProperty(OPT_INSTANCE_NAME)).andStubReturn(null);
    replay(bundleContext);
    
    ComponentContext context = createNiceMock(ComponentContext.class);
    expect(context.getBundleContext()).andStubReturn(bundleContext);
    replay(context);
    
    service.activate(context);
    assertNull(service.getName());
    

    bundleContext = createNiceMock(BundleContext.class);
    expect(bundleContext.getProperty(OPT_INSTANCE_NAME)).andStubReturn("instance01");
    replay(bundleContext);
    
    context = createNiceMock(ComponentContext.class);
    expect(context.getBundleContext()).andStubReturn(bundleContext);
    replay(context);
    
    service.activate(context);
    assertEquals("instance01", service.getName());
  }
  
}
