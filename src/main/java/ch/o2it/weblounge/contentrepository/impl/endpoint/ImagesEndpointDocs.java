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

    // GET /{image}/metadata
    Endpoint getImageMetadata = new Endpoint("/{image}/metadata", Method.GET, "getimagemetadata");
    getImageMetadata.setDescription("Returns the image with the given identifier");
    getImageMetadata.addFormat(new Format("image", null, null));
    getImageMetadata.addStatus(OK("the image was found and is returned as part of the response"));
    getImageMetadata.addStatus(NOT_FOUND("the image was not found or could not be loaded"));
    getImageMetadata.addStatus(BAD_REQUEST("an invalid image identifier was received"));
    getImageMetadata.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    getImageMetadata.addPathParameter(new Parameter("image", Parameter.Type.String, "The resource identifier"));
    getImageMetadata.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getImageMetadata);

    // GET /{image}/original
    Endpoint getOriginalImage = new Endpoint("/{image}/original", Method.GET, "originalimage");
    getOriginalImage.setDescription("Returns the original version of the image with the given identifier");
    getOriginalImage.addFormat(new Format("image", null, null));
    getOriginalImage.addStatus(OK("the image was found and is returned as part of the response"));
    getOriginalImage.addStatus(NOT_FOUND("the image was not found or could not be loaded"));
    getOriginalImage.addStatus(BAD_REQUEST("an invalid image identifier was received"));
    getOriginalImage.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    getOriginalImage.addPathParameter(new Parameter("image", Parameter.Type.String, "The image identifier"));
    getOriginalImage.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getOriginalImage);

    // GET /{image}/locales/{language}/original
    Endpoint getOriginalLocalizedImage = new Endpoint("/{image}/locales/{language}/original", Method.GET, "originallocalizedimage");
    getOriginalLocalizedImage.setDescription("Returns the original version of the image with the given identifier and language");
    getOriginalLocalizedImage.addFormat(new Format("image", null, null));
    getOriginalLocalizedImage.addStatus(OK("the image was found and is returned as part of the response"));
    getOriginalLocalizedImage.addStatus(NOT_FOUND("the image was not found or could not be loaded"));
    getOriginalLocalizedImage.addStatus(NOT_FOUND("the image does not exist in the specified language"));
    getOriginalLocalizedImage.addStatus(BAD_REQUEST("an invalid image identifier was received"));
    getOriginalLocalizedImage.addStatus(BAD_REQUEST("an invalid language identifier was specified"));
    getOriginalLocalizedImage.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    getOriginalLocalizedImage.addPathParameter(new Parameter("image", Parameter.Type.String, "The image identifier"));
    getOriginalLocalizedImage.addPathParameter(new Parameter("language", Parameter.Type.String, "The language identifier"));
    getOriginalLocalizedImage.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getOriginalLocalizedImage);

    // GET /{image}/styles/{styleid}
    Endpoint getScaleImage = new Endpoint("/{image}/styles/{style}", Method.GET, "styledimage");
    getScaleImage.setDescription("Returns a scaled version of the image with the given identifier");
    getScaleImage.addFormat(new Format("image", null, null));
    getScaleImage.addStatus(OK("the image was scaled using the specified image style and is returned as part of the response"));
    getScaleImage.addStatus(NOT_FOUND("the image was not found or could not be loaded"));
    getScaleImage.addStatus(BAD_REQUEST("an invalid image or image style identifier was received"));
    getScaleImage.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    getScaleImage.addPathParameter(new Parameter("image", Parameter.Type.String, "The image identifier"));
    getScaleImage.addPathParameter(new Parameter("style", Parameter.Type.String, "The image style identifier"));
    getScaleImage.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getScaleImage);

    // GET /{image}/locales/{language}/styles/{styleid}
    Endpoint getScaleLocalizedImage = new Endpoint("/{image}/locales/{language}/styles/{style}", Method.GET, "localizedstyledimage");
    getScaleLocalizedImage.setDescription("Returns a scaled version of the image with the given identifier and language");
    getScaleLocalizedImage.addFormat(new Format("image", null, null));
    getScaleLocalizedImage.addStatus(OK("the image was scaled using the specified image style and is returned as part of the response"));
    getScaleLocalizedImage.addStatus(NOT_FOUND("the image was not found or could not be loaded"));
    getScaleLocalizedImage.addStatus(NOT_FOUND("the image does not exist in the specified language"));
    getScaleLocalizedImage.addStatus(BAD_REQUEST("an invalid image or image style identifier was received"));
    getScaleLocalizedImage.addStatus(BAD_REQUEST("an invalid language identifier was specified"));
    getScaleLocalizedImage.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    getScaleLocalizedImage.addPathParameter(new Parameter("image", Parameter.Type.String, "The image identifier"));
    getScaleLocalizedImage.addPathParameter(new Parameter("language", Parameter.Type.String, "The language identifier"));
    getScaleLocalizedImage.addPathParameter(new Parameter("style", Parameter.Type.String, "The image style identifier"));
    getScaleLocalizedImage.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getScaleLocalizedImage);

    // GET /styles
    Endpoint getImageStyles = new Endpoint("/styles", Method.GET, "getstyles");
    getImageStyles.setDescription("Returns the image style");
    getImageStyles.addFormat(Format.xml());
    getImageStyles.addStatus(OK("the image styles are returned as part of the response"));
    getImageStyles.addStatus(SERVICE_UNAVAILABLE("the site is temporarily offline"));
    getImageStyles.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getImageStyles);

    // GET /styles/{styleid}
    Endpoint getImageStyle = new Endpoint("/styles/{style}", Method.GET, "getstyle");
    getImageStyle.setDescription("Returns the image style");
    getImageStyle.addFormat(Format.xml());
    getImageStyle.addStatus(OK("the image style was found and is returned as part of the response"));
    getImageStyle.addStatus(NOT_FOUND("the image style was not found"));
    getImageStyle.addStatus(BAD_REQUEST("an invalid image style identifier was received"));
    getImageStyle.addStatus(SERVICE_UNAVAILABLE("the site is temporarily offline"));
    getImageStyle.addPathParameter(new Parameter("style", Parameter.Type.String, "The image style identifier"));
    getImageStyle.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getImageStyle);

    return EndpointDocumentationGenerator.generate(docs);
  }

}
