/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.o2it.weblounge.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal export
 * @phase process-sources
 */
public class MyGengoI18nExport extends AbstractMojo {

  /**
   * The name of the site
   * 
   * @parameter
   * @required
   */
  private String siteName;

  /**
   * Location of the site folder.
   * 
   * @parameter
   * @required
   */
  private File siteDir;

  /**
   * Location of the output.
   * 
   * @parameter
   * @required
   */
  private File outputDir;

  /**
   * Languages to process (ex. 'de, en, it')
   * 
   * @parameter default-value="de, en, fr, it"
   */
  private String languages;

  public void execute() throws MojoExecutionException {

    // evaluate langs string
    StringTokenizer langTokens = new StringTokenizer(languages, " ,");
    List<String> langs = new ArrayList<String>();
    while (langTokens.hasMoreTokens()) {
      langs.add(langTokens.nextToken());
    }

    // setup output dir
    if (!outputDir.exists()) {
      outputDir.mkdirs();
    }
    for (String lang : langs) {
      File langDir = new File(outputDir, lang);
      langDir.mkdir();
    }

    // get site specific i18n files
    File folder = new File(siteDir, "conf/i18n");
    for (String lang : langs) {
      File file = new File(folder, "message_".concat(lang).concat(".xml"));
      if (file.exists()) {
        try {
          copyFile(file, new File(outputDir, lang.concat("/").concat(siteName).concat("_site.xml")));
        } catch (IOException e) {
          this.getLog().equals("Error collecting i18n files: ".concat(e.getMessage()));
        }
      }
    }

    // get i18n files of modules
    File modules = new File(siteDir, "modules");
    if (!modules.exists()) {
      modules = new File(siteDir, "module"); // fallback for Weblounge 2 site structure
    }
    if (modules.exists()) {
      for (File module : modules.listFiles()) {
        if (module.isDirectory()) {
          for (String lang : langs) {
            File moduleI18n = new File(module, "i18n");
            if(!moduleI18n.exists()) {
              moduleI18n = new File(module, "conf/i18n"); // fallback for Weblounge 2 site structure
            }
            File file = new File(moduleI18n, "message_".concat(lang).concat(".xml"));
            if (file.exists()) {
              try {
                copyFile(file, new File(outputDir, lang.concat("/").concat(siteName).concat("_module_").concat(module.getName()).concat(".xml")));
              } catch (IOException e) {
                this.getLog().equals("Error collecting i18n files: ".concat(e.getMessage()));
              }
            }
          }
        }
      }
    }

  }

  private void copyFile(File src, File dest) throws IOException {
    FileReader in = new FileReader(src);
    FileWriter out = new FileWriter(dest);
    int c;

    while ((c = in.read()) != -1)
      out.write(c);

    in.close();
    out.close();
  }
}
