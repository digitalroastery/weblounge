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
 * Image endpoint documentation generator.
 */
public final class ImagesEndpointDocs {

  /**
   * Creates the documentation.
   * 
   * @param endpointUrl
   *          the endpoint address
   * @return the endpoint documentation
   */
  public static String createDocumentation(String endpointUrl) {
    EndpointDocumentation docs = new EndpointDocumentation(endpointUrl, "images");
    docs.setTitle("Weblounge Images");

    // GET /{image}
    Endpoint getImageEndpoint = new Endpoint("/{image}", Method.GET, "getimage");
    getImageEndpoint.setDescription("Returns the image with the given identifier");
    getImageEndpoint.addFormat(new Format("image", null, null));
    getImageEndpoint.addStatus(OK("the image was found and is returned as part of the response"));
    getImageEndpoint.addStatus(NOT_FOUND("the image was not found or could not be loaded"));
    getImageEndpoint.addStatus(BAD_REQUEST("an invalid image identifier was received"));
    getImageEndpoint.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    getImageEndpoint.addPathParameter(new Parameter("image", Parameter.Type.String, "The resource identifier"));
    getImageEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getImageEndpoint);

    // GET /{image}/original
    Endpoint originalImageEndpoint = new Endpoint("/{image}/original", Method.GET, "originalimage");
    originalImageEndpoint.setDescription("Returns the original version of the image with the given identifier");
    originalImageEndpoint.addFormat(new Format("image", null, null));
    originalImageEndpoint.addStatus(OK("the image was found and is returned as part of the response"));
    originalImageEndpoint.addStatus(NOT_FOUND("the image was not found or could not be loaded"));
    originalImageEndpoint.addStatus(BAD_REQUEST("an invalid image identifier was received"));
    originalImageEndpoint.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    originalImageEndpoint.addPathParameter(new Parameter("image", Parameter.Type.String, "The image identifier"));
    originalImageEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, originalImageEndpoint);

    // GET /{image}/{language}/original
    Endpoint originalLocalizedImageEndpoint = new Endpoint("/{image}/{language}/original", Method.GET, "originallocalizedimage");
    originalLocalizedImageEndpoint.setDescription("Returns the original version of the image with the given identifier and language");
    originalLocalizedImageEndpoint.addFormat(new Format("image", null, null));
    originalLocalizedImageEndpoint.addStatus(OK("the image was found and is returned as part of the response"));
    originalLocalizedImageEndpoint.addStatus(NOT_FOUND("the image was not found or could not be loaded"));
    originalLocalizedImageEndpoint.addStatus(NOT_FOUND("the image does not exist in the specified language"));
    originalLocalizedImageEndpoint.addStatus(BAD_REQUEST("an invalid image identifier was received"));
    originalLocalizedImageEndpoint.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    originalLocalizedImageEndpoint.addPathParameter(new Parameter("image", Parameter.Type.String, "The image identifier"));
    originalLocalizedImageEndpoint.addPathParameter(new Parameter("language", Parameter.Type.String, "The language identifier"));
    originalLocalizedImageEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, originalLocalizedImageEndpoint);

    // GET /{resourceid}/styles/{styleid}
    Endpoint scaleImageEndpoint = new Endpoint("/{image}/styles/{style}", Method.GET, "styledimage");
    scaleImageEndpoint.setDescription("Returns a scaled version of the image with the given identifier");
    scaleImageEndpoint.addFormat(new Format("image", null, null));
    scaleImageEndpoint.addStatus(OK("the image was scaled using the specified image style and is returned as part of the response"));
    scaleImageEndpoint.addStatus(NOT_FOUND("the image was not found or could not be loaded"));
    scaleImageEndpoint.addStatus(BAD_REQUEST("an invalid image or image style identifier was received"));
    scaleImageEndpoint.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    scaleImageEndpoint.addPathParameter(new Parameter("image", Parameter.Type.String, "The image identifier"));
    scaleImageEndpoint.addPathParameter(new Parameter("style", Parameter.Type.String, "The image style identifier"));
    scaleImageEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, scaleImageEndpoint);

    // GET /{resourceid}/styles/{styleid}
    Endpoint scaleLocalizedImageEndpoint = new Endpoint("/{image}/{language}/styles/{style}", Method.GET, "localizedstyledimage");
    scaleLocalizedImageEndpoint.setDescription("Returns a scaled version of the image with the given identifier and language");
    scaleLocalizedImageEndpoint.addFormat(new Format("image", null, null));
    scaleLocalizedImageEndpoint.addStatus(OK("the image was scaled using the specified image style and is returned as part of the response"));
    scaleLocalizedImageEndpoint.addStatus(NOT_FOUND("the image was not found or could not be loaded"));
    scaleLocalizedImageEndpoint.addStatus(NOT_FOUND("the image does not exist in the specified language"));
    scaleLocalizedImageEndpoint.addStatus(BAD_REQUEST("an invalid image or image style identifier was received"));
    scaleLocalizedImageEndpoint.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    scaleLocalizedImageEndpoint.addPathParameter(new Parameter("image", Parameter.Type.String, "The image identifier"));
    scaleLocalizedImageEndpoint.addPathParameter(new Parameter("language", Parameter.Type.String, "The language identifier"));
    scaleLocalizedImageEndpoint.addPathParameter(new Parameter("style", Parameter.Type.String, "The image style identifier"));
    scaleLocalizedImageEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, scaleLocalizedImageEndpoint);

    return EndpointDocumentationGenerator.generate(docs);
  }

}
