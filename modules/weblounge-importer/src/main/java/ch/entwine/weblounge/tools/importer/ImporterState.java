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

package ch.entwine.weblounge.tools.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ImporterState {

  /** instance of this class */
  private static final ImporterState INSTANCE = new ImporterState();

  /** map with path-uuid pairs */
  private Map<String, UUID> uuidStore = new HashMap<String, UUID>();

  /** keys without uuid */
  private List<String> invalidPaths = new ArrayList<String>();

  /** the logging facility */
  private static final Logger log_ = LoggerFactory.getLogger(ImporterState.class);

  // Private constructor prevents instantiation from other classes
  private ImporterState() {
  }

  /**
   * Returns instance of this class
   * 
   * @return instance of this class
   */
  public static ImporterState getInstance() {
    return INSTANCE;
  }

  /**
   * Puts a path-uuid pair into the
   * 
   * @param path
   *          path (ex. '/my/page/structure')
   * @param uuid
   *          uuid
   */
  public void putUUID(String path, UUID uuid) {
    try {
      uuidStore.put(path.toLowerCase(), uuid);
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  /**
   * Returns the UUID for a given path. If no UUID for the given path is found,
   * the UUID of the root element ('/') will be returned.
   * 
   * @param path
   *          path to search for corresponding UUID
   * @return UUID of the given path or if path was not found, UUID of the root
   *         element
   */
  public String getUUID(String path) {
    if (!path.startsWith("/"))
      path = "/".concat(path);

    if (path.length() > 1 && path.endsWith("/"))
      path = path.substring(0, path.length() - 1);

    UUID uuid = uuidStore.get(path.toLowerCase());

    if (uuid != null) {
      // log_.info("Returning UUID ".concat(uuidStore.get(path).toString().concat(" for path ").concat(path)));
      return uuid.toString();
    } else {
      log_.warn("No UUID found for path ".concat(path));
      invalidPaths.add(path);
      return "";
    }
  }

  /**
   * Returns the size of the UUID store
   * 
   * @return size of the UUID store
   */
  public int getUUIDMapSize() {
    return uuidStore.size();
  }

  /**
   * Returns a list of invalid (no corresponding UUID) paths
   * 
   * @return list of invalid paths
   */
  public List<String> getInvalidKeys() {
    return invalidPaths;
  }

}