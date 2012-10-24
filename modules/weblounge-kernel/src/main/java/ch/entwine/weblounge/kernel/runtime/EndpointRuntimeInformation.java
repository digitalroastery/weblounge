/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.kernel.runtime;

import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.kernel.publisher.EndpointPublishingService;
import ch.entwine.weblounge.kernel.publisher.JAXRSServlet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;

import java.util.Map;

/**
 * Returns runtime information on the available endpoints.
 */
public class EndpointRuntimeInformation implements RuntimeInformationProvider {

  /** Endpoint publishing service */
  protected EndpointPublishingService publishingService = null;

  /** The bundle context */
  private BundleContext bundleCtx = null;

  /**
   * OSGi callback on component activation.
   * 
   * @param ctx
   *          the component context
   */
  void activate(ComponentContext ctx) {
    this.bundleCtx = ctx.getBundleContext();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.kernel.runtime.RuntimeInformationProvider#getComponentId()
   */
  public String getComponentId() {
    return "endpoints";
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.kernel.runtime.RuntimeInformationProvider#getRuntimeInformation(ch.entwine.weblounge.common.site.Site,
   *      ch.entwine.weblounge.common.security.User,
   *      ch.entwine.weblounge.common.language.Language, Environment)
   */
  public String getRuntimeInformation(Site site, User user, Language language,
      Environment environment) {
    StringBuffer xml = new StringBuffer();
    for (Map.Entry<String, ServiceRegistration> entry : publishingService.getEndpoints().entrySet()) {
      ServiceRegistration sr = entry.getValue();
      JAXRSServlet servlet = (JAXRSServlet)bundleCtx.getService(sr.getReference());
      Object service = servlet.getService();
      xml.append("<endpoint>");
      xml.append("<name><![CDATA[").append(service.toString()).append("]]></name>");
      xml.append("<path>").append(entry.getKey()).append("</path>");
      xml.append("<service>").append(service.getClass().getName()).append("</service>");
      xml.append("</endpoint>");
    }
    return xml.toString();
  }

  /**
   * OSGi callback to set the endpoint publishing service.
   * 
   * @param service
   *          the publishing service
   */
  void setEndpointPublishingService(EndpointPublishingService service) {
    this.publishingService = service;
  }

}
