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

import static ch.entwine.weblounge.common.impl.util.doc.Status.conflict;
import static ch.entwine.weblounge.common.impl.util.doc.Status.forbidden;
import static ch.entwine.weblounge.common.impl.util.doc.Status.ok;
import static ch.entwine.weblounge.common.impl.util.doc.Status.preconditionFailed;
import static ch.entwine.weblounge.common.impl.util.doc.Status.serviceUnavailable;

import ch.entwine.weblounge.common.impl.util.doc.Endpoint;
import ch.entwine.weblounge.common.impl.util.doc.Endpoint.Method;
import ch.entwine.weblounge.common.impl.util.doc.EndpointDocumentation;
import ch.entwine.weblounge.common.impl.util.doc.EndpointDocumentationGenerator;
import ch.entwine.weblounge.common.impl.util.doc.Format;
import ch.entwine.weblounge.common.impl.util.doc.TestForm;

/**
 * Index endpoint documentation generator.
 */
public final class IndexEndpointDocs {

  /**
   * No need to instantiate this utility class.
   */
  private IndexEndpointDocs() {
  }

  /**
   * Creates the documentation.
   * 
   * @param endpointUrl
   *          the endpoint address
   * @return the endpoint documentation
   */
  public static String createDocumentation(String endpointUrl) {
    EndpointDocumentation docs = new EndpointDocumentation(endpointUrl, "index");
    docs.setTitle("Weblounge Index");

    // GET /
    Endpoint getStatistics = new Endpoint("/statistics", Method.GET, "getstatistics");
    getStatistics.setDescription("Returns index statistics");
    getStatistics.addFormat(Format.xml());
    getStatistics.addStatus(ok("the index statistics are returned as part of the response"));
    getStatistics.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    getStatistics.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getStatistics);

    // DELETE /{page}/unlock
    Endpoint reindex = new Endpoint("/", Method.DELETE, "reindex");
    reindex.setDescription("Triggers an index operation and returns immediately");
    reindex.addFormat(Format.xml());
    reindex.addStatus(ok("the index operation was started"));
    reindex.addStatus(preconditionFailed("the content repository is read only"));
    reindex.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    reindex.addStatus(conflict("the index is already being rebuilt"));
    reindex.addStatus(forbidden("the current user does not have the rights to rebuild the index"));
    reindex.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, reindex);

    return EndpointDocumentationGenerator.generate(docs);
  }

}
