/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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
package ch.entwine.weblounge.contentrepository.impl.index.elasticsearch;

import ch.entwine.weblounge.common.url.PathUtils;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.contentrepository.index.SearchIndexTest;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Utilities to ease dealing with Elastic Search.
 */
public final class ElasticSearchUtils {

  /**
   * Private constructor to make sure this class is used as a utility class.
   */
  private ElasticSearchUtils() {
  }

  /**
   * Creates an elastic search index configuration inside the given directory by
   * loading the relevant configuration files from the bundle. The final
   * location will be <code>homeDirectory/etc/index</code>.
   * 
   * @param homeDirectory
   *          the configuration directory
   * @throws IOException
   *           if creating the configuration fails
   */
  public static void createIndexConfigurationAt(File homeDirectory)
      throws IOException {

    // Load the index configuration and move it into place
    File configurationRoot = new File(PathUtils.concat(homeDirectory.getAbsolutePath(), "etc", "index"));
    FileUtils.deleteQuietly(configurationRoot);
    if (!configurationRoot.mkdirs())
      throw new IOException("Error creating " + configurationRoot);

    String[] files = new String[] {
        "default-mapping.json",
        "file-mapping.json",
        "image-mapping.json",
        "movie-mapping.json",
        "names.txt",
        "page-mapping.json",
        "settings.yml",
    "version-mapping.json" };

    for (String file : files) {
      String bundleLocation = UrlUtils.concat("/elasticsearch", file);
      File fileLocation = new File(configurationRoot, file);
      FileUtils.copyInputStreamToFile(SearchIndexTest.class.getResourceAsStream(bundleLocation), fileLocation);
    }

  }

}
