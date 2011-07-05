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

package ch.entwine.weblounge.contentrepository.impl.endpoint;

import static ch.entwine.weblounge.common.impl.util.doc.Status.badRequest;
import static ch.entwine.weblounge.common.impl.util.doc.Status.notFound;
import static ch.entwine.weblounge.common.impl.util.doc.Status.ok;
import static ch.entwine.weblounge.common.impl.util.doc.Status.serviceUnavailable;

import ch.entwine.weblounge.common.impl.util.doc.Endpoint;
import ch.entwine.weblounge.common.impl.util.doc.Endpoint.Method;
import ch.entwine.weblounge.common.impl.util.doc.EndpointDocumentation;
import ch.entwine.weblounge.common.impl.util.doc.EndpointDocumentationGenerator;
import ch.entwine.weblounge.common.impl.util.doc.Format;
import ch.entwine.weblounge.common.impl.util.doc.Parameter;
import ch.entwine.weblounge.common.impl.util.doc.TestForm;

/**
 * Previews endpoint documentation generator.
 */
public final class PreviewsEndpointDocs {

  /**
   * No need to instantiate this utility class.
   */
  private PreviewsEndpointDocs() {
  }

  /**
   * Creates the documentation.
   * 
   * @param endpointUrl
   *          the endpoint address
   * @return the endpoint documentation
   */
  public static String createDocumentation(String endpointUrl) {
    EndpointDocumentation docs = new EndpointDocumentation(endpointUrl, "previews");
    docs.setTitle("Weblounge Previews");

    // GET /{resource}/locales/{language}/styles/{styleid}
    Endpoint getScaleLocalizedImage = new Endpoint("/{resource}/locales/{language}/styles/{style}", Method.GET, "localizedstyledimage");
    getScaleLocalizedImage.setDescription("Returns a scaled version of the resource with the given identifier and language");
    getScaleLocalizedImage.addFormat(new Format("resource", null, null));
    getScaleLocalizedImage.addStatus(ok("the resource was scaled using the specified image style and is returned as part of the response"));
    getScaleLocalizedImage.addStatus(notFound("the resource was not found or could not be loaded"));
    getScaleLocalizedImage.addStatus(notFound("the resource does not exist in the specified language"));
    getScaleLocalizedImage.addStatus(badRequest("an invalid image or image style identifier was received"));
    getScaleLocalizedImage.addStatus(badRequest("an invalid language identifier was specified"));
    getScaleLocalizedImage.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    getScaleLocalizedImage.addPathParameter(new Parameter("resource", Parameter.Type.String, "The resource identifier"));
    getScaleLocalizedImage.addPathParameter(new Parameter("language", Parameter.Type.String, "The language identifier"));
    getScaleLocalizedImage.addPathParameter(new Parameter("style", Parameter.Type.String, "The image style identifier"));
    getScaleLocalizedImage.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getScaleLocalizedImage);

    // GET /styles
    Endpoint getImageStyles = new Endpoint("/styles", Method.GET, "getstyles");
    getImageStyles.setDescription("Returns the image style");
    getImageStyles.addFormat(Format.xml());
    getImageStyles.addStatus(ok("the image styles are returned as part of the response"));
    getImageStyles.addStatus(serviceUnavailable("the site is temporarily offline"));
    getImageStyles.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getImageStyles);

    // GET /styles/{styleid}
    Endpoint getImageStyle = new Endpoint("/styles/{style}", Method.GET, "getstyle");
    getImageStyle.setDescription("Returns the image style");
    getImageStyle.addFormat(Format.xml());
    getImageStyle.addStatus(ok("the image style was found and is returned as part of the response"));
    getImageStyle.addStatus(notFound("the image style was not found"));
    getImageStyle.addStatus(badRequest("an invalid image style identifier was received"));
    getImageStyle.addStatus(serviceUnavailable("the site is temporarily offline"));
    getImageStyle.addPathParameter(new Parameter("style", Parameter.Type.String, "The image style identifier"));
    getImageStyle.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getImageStyle);

    return EndpointDocumentationGenerator.generate(docs);
  }

}
