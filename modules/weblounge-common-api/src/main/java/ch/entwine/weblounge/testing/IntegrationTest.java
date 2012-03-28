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

package ch.entwine.weblounge.testing;

/**
 * Interface definition for integration tests. An integration test is executed
 * against a running instance of a system.
 * <p>
 * This interface defines the notion of groups and execution orders. A groups
 * defines a number of integration tests that belong together. In addition, an
 * execution order may be defined that specifies the order in which tests within
 * each group are executed.
 */
public interface IntegrationTest {

  /** Special group for weblounge content tests */
  String WEBLOUNGE_CONTENT_TEST_GROUP = "WEBLOUNGE CONTENT TESTS";

  /** Special group for weblounge rest endpoint tests */
  String WEBLOUNGE_ENDPOINT_TEST_GROUP = "WEBLOUNGE ENDPOINT TESTS";

  /**
   * Runs the integration test.
   * 
   * @param serverUrl
   *          address of the server
   * @throws Exception
   *           if the test fails
   */
  void execute(String serverUrl) throws Exception;

  /**
   * Returns the name of this test
   * 
   * @return the name
   */
  String getName();

  /**
   * Returns the group of that this test belongs to or <code>null</code> if it
   * doesn't belong to any group.
   * 
   * @return the group name
   */
  String getGroup();

  /**
   * Returns a number that indicates the order relative to the order of other
   * tests in the same test group. Lower numbers will be executed first.
   * 
   * @return the order in which this test should be executed
   */
  int getExecutionOrder();

}
