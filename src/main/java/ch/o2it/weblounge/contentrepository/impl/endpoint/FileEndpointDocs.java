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

package ch.o2it.weblounge.contentrepository.impl.endpoint;

import static ch.o2it.weblounge.common.impl.util.doc.Status.BAD_REQUEST;
import static ch.o2it.weblounge.common.impl.util.doc.Status.NOT_FOUND;
import static ch.o2it.weblounge.common.impl.util.doc.Status.OK;
import static ch.o2it.weblounge.common.impl.util.doc.Status.SERVICE_UNAVAILABLE;

import ch.o2it.weblounge.common.impl.util.doc.Endpoint;
import ch.o2it.weblounge.common.impl.util.doc.EndpointDocumentation;
import ch.o2it.weblounge.common.impl.util.doc.EndpointDocumentationGenerator;
import ch.o2it.weblounge.common.impl.util.doc.Format;
import ch.o2it.weblounge.common.impl.util.doc.Parameter;
import ch.o2it.weblounge.common.impl.util.doc.TestForm;
import ch.o2it.weblounge.common.impl.util.doc.Endpoint.Method;

/**
 * File endpoint documentation generator.
 */
public final class FileEndpointDocs {

  /**
   * Creates the documentation.
   * 
   * @param endpointUrl
   *          the endpoint address
   * @return the endpoint documentation
   */
  public static String createDocumentation(String endpointUrl) {
    EndpointDocumentation docs = new EndpointDocumentation(endpointUrl, "files");
    docs.setTitle("Weblounge Resources");

    // GET /{resourceid}
    Endpoint getResourceEndpoint = new Endpoint("/{resourceid}", Method.GET, "getresource");
    getResourceEndpoint.setDescription("Returns the resource with the given id");
    getResourceEndpoint.addFormat(new Format("binary", null, null));
    getResourceEndpoint.addStatus(OK("the resource was found and is returned as part of the response"));
    getResourceEndpoint.addStatus(NOT_FOUND("the resource was not found or could not be loaded"));
    getResourceEndpoint.addStatus(BAD_REQUEST("an invalid resource identifier was received"));
    getResourceEndpoint.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    getResourceEndpoint.addPathParameter(new Parameter("resourceid", Parameter.Type.STRING, "The resource identifier"));
    getResourceEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getResourceEndpoint);

    return EndpointDocumentationGenerator.generate(docs);
  }

}
