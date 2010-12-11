/*
 * PageImporterCallback.java
 *
 * Copyright 2007 by O2 IT Engineering
 * Zurich, Switzerland (CH)
 * All rights reserved.
 *
 * This software is confidential and proprietary information ("Confidential
 * Information").  You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license
 * agreement you entered into.
 */

package ch.o2it.weblounge.tools.importer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class ModuleImporterCallback extends AbstractImporterCallback {

  /**
   * Creates a new callback for module imports.
   * 
   * @param src
   *          the source root directory
   * @param dest
   *          the destination root directory
   */
  ModuleImporterCallback(File src, File dest,
      Map<String, Collection<?>> clipboard) {
    super(src, dest);
  }

  /**
   * This method is called if a module file has been found.
   * 
   * @see ch.o2it.weblounge.tools.importer.AbstractImporterCallback#fileImported(java.io.File)
   */
  public boolean fileImported(File f) {
    try {
      File dest = createDestination(f);
      if (dest != null) {
        copy(f, dest);
        return true;
      }
      return false;
    } catch (IOException e) {
      System.err.println("Error creating target file for " + f + ": " + e.getMessage());
      return false;
    }
  }

  public boolean folderImported(File f) throws Exception {
    // TODO Auto-generated method stub
    return false;
  }

}