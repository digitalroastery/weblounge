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

package ch.entwine.weblounge.cache.impl.endpoint;

import static ch.entwine.weblounge.common.impl.util.doc.Status.notFound;
import static ch.entwine.weblounge.common.impl.util.doc.Status.notModified;
import static ch.entwine.weblounge.common.impl.util.doc.Status.ok;
import static ch.entwine.weblounge.common.impl.util.doc.Status.serviceUnavailable;

import ch.entwine.weblounge.common.impl.util.doc.Endpoint;
import ch.entwine.weblounge.common.impl.util.doc.Endpoint.Method;
import ch.entwine.weblounge.common.impl.util.doc.EndpointDocumentation;
import ch.entwine.weblounge.common.impl.util.doc.EndpointDocumentationGenerator;
import ch.entwine.weblounge.common.impl.util.doc.Format;
import ch.entwine.weblounge.common.impl.util.doc.TestForm;

/**
 * Cache endpoint documentation generator.
 */
public final class CacheEndpointDocs {

  /**
   * No need to instantiate this utility class.
   */
  private CacheEndpointDocs() {
  }

  /**
   * Creates the documentation.
   * 
   * @param endpointUrl
   *          the endpoint address
   * @return the endpoint documentation
   */
  public static String createDocumentation(String endpointUrl) {
    EndpointDocumentation docs = new EndpointDocumentation(endpointUrl, "cache");
    docs.setTitle("Weblounge Cache");

    // GET /
    Endpoint getStatistics = new Endpoint("/", Method.GET, "stats");
    getStatistics.setDescription("Returns cache statistics");
    getStatistics.addFormat(Format.xml());
    getStatistics.addStatus(ok("statistics have been compiled and sent back to the client"));
    getStatistics.addStatus(notFound("the site does not exist"));
    getStatistics.addStatus(serviceUnavailable("the site is temporarily offline"));
    getStatistics.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getStatistics);

    // PUT /
    Endpoint startCacheEndpoint = new Endpoint("/", Method.PUT, "start");
    startCacheEndpoint.setDescription("Enables caching for the current site");
    startCacheEndpoint.addFormat(Format.xml());
    startCacheEndpoint.addStatus(ok("the cache was enabled"));
    startCacheEndpoint.addStatus(notModified("the cache was already enabled"));
    startCacheEndpoint.addStatus(notFound("the site does not exist"));
    startCacheEndpoint.addStatus(serviceUnavailable("the site is temporarily offline"));
    startCacheEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, startCacheEndpoint);

    // DELETE /
    Endpoint stopCacheEndpoint = new Endpoint("/", Method.DELETE, "stop");
    stopCacheEndpoint.setDescription("Disables caching for the current site");
    stopCacheEndpoint.addFormat(Format.xml());
    stopCacheEndpoint.addStatus(ok("the cache was disabled"));
    stopCacheEndpoint.addStatus(notModified("the cache was already disabled"));
    stopCacheEndpoint.addStatus(notFound("the site does not exist"));
    stopCacheEndpoint.addStatus(serviceUnavailable("the site is temporarily offline"));
    stopCacheEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, stopCacheEndpoint);

    // DELETE /content
    Endpoint clearCacheEndpoint = new Endpoint("/content", Method.DELETE, "clear");
    clearCacheEndpoint.setDescription("Clear the cache for the current site");
    clearCacheEndpoint.addFormat(Format.xml());
    clearCacheEndpoint.addStatus(ok("the cache was cleared"));
    clearCacheEndpoint.addStatus(notFound("the site does not exist"));
    clearCacheEndpoint.addStatus(serviceUnavailable("the site is temporarily offline"));
    clearCacheEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, clearCacheEndpoint);

    return EndpointDocumentationGenerator.generate(docs);
  }

}
