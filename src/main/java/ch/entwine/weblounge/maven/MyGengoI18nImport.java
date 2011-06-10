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

package ch.entwine.weblounge.maven;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 
 * 
 * @goal import
 */
public class MyGengoI18nImport extends AbstractMojo {

  /**
   * Name of the site.
   * 
   * @parameter alias="site"
   * @required
   */
  private String siteId;

  /**
   * Location of the site folder.
   * 
   * @parameter alias="site-root"
   * @required
   */
  private File siteRoot;

  /**
   * URL pointing to the myGengo i18n files zip package.
   * 
   * @parameter alias="mygengo-url"
   * @required
   */
  private URL myGengoUrl;

  /**
   * Username used to access the myGengo i18n files.
   * 
   * @parameter alias="mygengo-username"
   */
  private String myGengoUsername;

  /**
   * Password used to access the myGengo i18n files.
   * 
   * @parameter alias="mygengo-password"
   */
  private String myGengoPassword;

  /**
   * Password used to access the myGengo i18n files.
   * 
   * @parameter
   */
  private boolean isWeblounge2 = false;

  /** the logging facility */
  private Log log = getLog();

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    log.info("Start importing i18n files from myGengo.");
    URLConnection conn = getMyGengoConn();
    ZipFile zip = extractZipFileFromConn(conn);

    if (zip != null) {
      Enumeration<? extends ZipEntry> entries = zip.entries();
      ZipEntry entry;
      while (entries.hasMoreElements()) {
        try {
          entry = entries.nextElement();
          importLanguageFile(entry.getName(), zip.getInputStream(entry));
        } catch (FileNotFoundException e) {
          log.error("Error while reading zip file.");
        } catch (IOException e) {
          log.error("Error while reading zip file.");
        }
      }
    }
    log.info("Importing i18n files from myGengo finished.");
  }

  private void importLanguageFile(String name, InputStream file) {
    String lang = name.substring(0, name.indexOf("/"));
    String filename = name.substring(name.indexOf("/") + 1);
    String path;
    if (filename.startsWith(siteId)) {
      log.info("Importing i18n file " + name);
      if ((siteId + "_site.xml").equals(filename)) {
        if (isWeblounge2)
          path = "/conf/i18n/message_" + lang + ".xml";
        else
          path = "/i18n/message_" + lang + ".xml";
      } else {
        String filenameNoSite = filename.substring(filename.indexOf("_") + 1);
        String module = filenameNoSite.substring(filenameNoSite.indexOf("_") + 1, filenameNoSite.indexOf("."));
        if (isWeblounge2)
          path = "/module/" + module + "/conf/i18n/message_" + lang + ".xml";
        else
          path = "/modules/" + module + "/i18n/message_" + lang + ".xml";
      }

      File dest = new File(siteRoot, path);
      try {
        FileUtils.forceMkdir(dest.getParentFile());
        if (!dest.exists())
          dest.createNewFile();
        IOUtils.copy(file, new FileOutputStream(dest));
      } catch (IOException e) {
        log.error("Error importing i18n file " + name);
        return;
      }
    }
  }

  private ZipFile extractZipFileFromConn(URLConnection conn) {
    if (conn != null) {
      try {
        File temp = File.createTempFile("mygengo-i18n", ".zip");
        temp.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(temp);
        IOUtils.copy(conn.getInputStream(), fos);
        return new ZipFile(temp);
      } catch (IOException e) {
        log.error("Error reading zip file from response.");
        return null;
      }
    } else {
      return null;
    }
  }

  private URLConnection getMyGengoConn() {
    // Request MyGengo i18n file
    URLConnection conn;
    try {
      conn = myGengoUrl.openConnection();
      if (StringUtils.isNotBlank(myGengoUsername) && StringUtils.isNotBlank(myGengoPassword))
        conn.setRequestProperty("Authorization", "Basic " + (new Base64()).encode((myGengoUsername + ":" + myGengoPassword).getBytes()).toString());
    } catch (IOException e) {
      log.error("Error getting zip file from myGengo.");
      return null;
    }
    return conn;
  }

}
