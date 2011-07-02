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
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.kernel.publisher.EndpointPublishingService;

import java.util.Map;

/**
 * Returns runtime information on the available endpoints.
 */
public class EndpointRuntimeInformation implements RuntimeInformationProvider {

  /** Endpoint publishing service */
  protected EndpointPublishingService publishingService = null;

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
   *      ch.entwine.weblounge.common.language.Language)
   */
  public String getRuntimeInformation(Site site, User user, Language language) {
    StringBuffer xml = new StringBuffer();
    for (Map.Entry<String, Object> entry : publishingService.getEndpoints().entrySet()) {
      xml.append("<endpoint>");
      xml.append("<name><![CDATA[").append(entry.getValue().toString()).append("]]></name>");
      xml.append("<path>").append(entry.getKey()).append("</path>");
      xml.append("<service>").append(entry.getValue().getClass().getName()).append("</service>");
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
