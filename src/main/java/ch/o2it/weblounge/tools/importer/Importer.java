/**
 * Importer.java
 *
 * Copyright 2005 by O2 IT Engineering
 * Zurich,  Switzerland (CH)
 * All rights reserved.
 * 
 * This software is confidential and proprietary information ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into.
 */

package ch.o2it.weblounge.tools.importer;

import ch.o2it.weblounge.tools.util.CommandLineParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

/**
 * Tool to import data from earlier versions of weblounge.
 */
public class Importer {

  /** Command line parameter "dir" */
  private static final String[] OPT_SRC_DIRECTORY = { "i", "in" };

  /** Command line parameter "dest" */
  private static final String[] OPT_DEST_DIRECTORY = { "o", "out" };

  /** Command line parameter "force" */
  private static final String[] CMD_FORCE = { "f", "force" };

  /** Command line parameter "help" */
  private static final String[] CMD_HELP = { "h", "help", "usage" };

  /** Command line parameter "quiet" */
  private static final String[] CMD_QUIET = { "q", "quiet" };

  /** Root collection */
  public static final String ROOT_COLLECTION = "/db/weblounge";

  /** Output properties */
  public final static Properties defaultOutputProperties = new Properties();

  /** Clipboard */
  public static Map<String, Collection<?>> clipboard = new HashMap<String, Collection<?>>();

  /** The command line parser */
  private static CommandLineParser cmd = new CommandLineParser();

  static {
    cmd.defineRequiredOption(OPT_SRC_DIRECTORY);
    cmd.defineRequiredOption(OPT_DEST_DIRECTORY);
    cmd.defineCommand(CMD_FORCE);
    cmd.defineCommand(CMD_HELP);
    cmd.defineCommand(CMD_QUIET);
  }

  private File srcDir;
  private File destDir;
  private boolean quiet;
  long pageCount;
  long errorCount;
  List<String> errorUrls = new ArrayList<String>();

  /**
   * Creates a new importer process for the specified source directory.
   * 
   * @param srcDir
   *          the src directory
   * @param destDir
   *          the target directory
   * @param rootCollection
   *          the root collection to import
   * @param quiet
   *          <code>true</code> to suppress output
   */
  public Importer(String srcDir, String destDir, boolean quiet) {
    this.srcDir = new File(srcDir);
    this.destDir = new File(destDir);
    this.quiet = quiet;
    pageCount = 0;
    errorCount = 0;
  }

  public void importPagePaths() throws Exception {
    File siteRoot = new File(srcDir, "site");
    ImporterCallback callback = new PagePathImporterCallback(siteRoot, destDir, "/home");
    try {
      traverse(siteRoot, callback);
    } catch (Throwable t) {
      System.err.println("Error importing " + siteRoot + ": " + t.getMessage());
      return;
    }
    if (!quiet) {
      System.out.println("\n" + pageCount + " page pahts imported.");
    }
    if (errorCount > 0) {
      System.out.println("\n" + errorCount + " errors occured:\n");
      for (int i = 0; i < errorUrls.size(); i++) {
        System.out.println("\n" + errorUrls.get(i));
      }
    }
  }

  /**
   * Starts the import process for the site pages.
   * 
   * @throws Exception
   */
  public void importPages() throws Exception {
    File siteRoot = new File(srcDir, "site");
    ImporterCallback callback = new PageImporterCallback(srcDir, destDir, clipboard);
    try {
      traverse(siteRoot, callback);
    } catch (Throwable t) {
      System.err.println("Error importing " + siteRoot + ": " + t.getMessage());
      return;
    }
    if (!quiet) {
      System.out.println("\n" + pageCount + " pages imported.");
    }
    if (errorCount > 0) {
      System.out.println("\n" + errorCount + " errors occured:\n");
      for (int i = 0; i < errorUrls.size(); i++) {
        System.out.println("\n" + errorUrls.get(i));
      }
    }
  }

  /**
   * Starts the import process for the repository.
   * 
   * @throws Exception
   */
  public void importResources() throws Exception {
    File repositoryRoot = new File(srcDir, "repository");
    ImporterCallback callback = new ResourceImporterCallback(repositoryRoot, destDir, clipboard);
    try {
      traverse(repositoryRoot, callback);
    } catch (Throwable t) {
      System.err.println("Error importing " + repositoryRoot + ": " + t.getMessage());
      return;
    }
    if (!quiet) {
      System.out.println("\n" + pageCount + " resources imported.");
    }
    if (errorCount > 0) {
      System.out.println("\n" + errorCount + " errors occured:\n");
      for (int i = 0; i < errorUrls.size(); i++) {
        System.out.println("\n" + errorUrls.get(i));
      }
    }
  }

  /**
   * Starts the import process for the users.
   * 
   * @throws Exception
   */
  public void importUsers() throws Exception {
    File usersRoot = new File(srcDir, "user");
    ImporterCallback callback = new UserImporterCallback(srcDir, destDir, clipboard);
    Collection<String> users = new ArrayList<String>();
    clipboard.put("users", users);
    try {
      traverse(usersRoot, callback);
    } catch (Throwable t) {
      System.err.println("Error importing " + usersRoot + ": " + t.getMessage());
      return;
    }
    if (!quiet) {
      System.out.println("\n" + pageCount + " users imported.");
    }
    if (errorCount > 0) {
      System.out.println("\n" + errorCount + " errors occured:\n");
      for (int i = 0; i < errorUrls.size(); i++) {
        System.out.println("\n" + errorUrls.get(i));
      }
    }
  }

  /**
   * Starts the import process for the site pages.
   * 
   * @throws Exception
   */
  public void importModules() throws Exception {
    File modulesRoot = new File(srcDir, "modules");
    ImporterCallback callback = new ModuleImporterCallback(srcDir, destDir, clipboard);
    try {
      traverse(modulesRoot, callback);
    } catch (Throwable t) {
      System.err.println("Error importing " + modulesRoot + ": " + t.getMessage());
      return;
    }
    if (!quiet) {
      System.out.println("\n" + pageCount + " module files imported.");
    }
    if (errorCount > 0) {
      System.out.println("\n" + errorCount + " errors occured:\n");
      for (int i = 0; i < errorUrls.size(); i++) {
        System.out.println("\n" + errorUrls.get(i));
      }
    }
  }

  /**
   * Main method that will read in the command line arguments and start the
   * backup process accordingly.
   * 
   * @param args
   *          the commandline arguments
   */
  public static void main(String[] args) {
    try {
      cmd.parse(args);
    } catch (Exception e) {
      System.err.println("\n" + e.getMessage());
      printUsage();
    }

    // Help
    if (cmd.providesCommand(CMD_HELP)) {
      printUsage();
    }

    String src = cmd.getOption(OPT_SRC_DIRECTORY);
    String dest = cmd.getOption(OPT_DEST_DIRECTORY);
    boolean quiet = cmd.providesCommand(CMD_QUIET);

    // Check for existing backup directory
    if (!cmd.providesCommand(CMD_FORCE)) {
      File f = new File(dest);
      if (f.exists()) {
        System.err.println("The destination directory " + src + " already exists. Use --force to overwrite.");
        return;
      }
      f.delete();
    }

    Date start = new Date();

    try {
      Importer importer = new Importer(src, dest, quiet);
      // importer.importUsers();
      // importer.importModules();
      importer.importPagePaths();
      importer.importResources();
      importer.importPages();
    } catch (NoClassDefFoundError e) {
      System.err.println("Required class " + e.getMessage() + " not found");
    } catch (Exception e) {
      System.err.println("Unknown error occured: " + e.getMessage());
      e.printStackTrace();
    }

    Date end = new Date();
    long time = end.getTime() - start.getTime();

    long seconds = time / 1000L;
    long minutes = 0;

    while (seconds >= 60) {
      minutes++;
      seconds -= 60;
    }

    System.out.println("Import took " + minutes + ":" + seconds);
  }

  public static String getUUID(String partition, String path) {
    return ImporterState.getInstance().getUUID(getPath(partition, path));
  }

  public static String getUUID(String path) {
    return ImporterState.getInstance().getUUID(path);
  }

  public static String getOrigPath(String partition, String path) {
    if (!partition.startsWith("/")) {
      partition = "/".concat(partition);
    }
    if (path.length() == 1) {
      path = "";
    }
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    if (path.length() > 1 && !path.startsWith("/")) {
      path = "/".concat(path);
    }
    return partition.concat(path).toLowerCase();
  }

  public static String getPath(String partition, String path) {
    String path_ = getOrigPath(partition, path);
    if (path_.startsWith(PageImporterCallback.ROOT_PARTITION)) {
      path_ = path_.substring(PageImporterCallback.ROOT_PARTITION.length(), path_.length());
    }
    if ("".equals(path_)) {
      path_ = "/";
    }
    return path_.toLowerCase();
  }

  /**
   * Prints out the usage of this tool with its parameters.
   */
  private static void printUsage() {
    System.err.println("\nUsage: importer [options]");
    System.err.println("Options: -[ioqs] -d");
    System.err.println();
    System.err.println("Help (this screen):");
    System.err.println("       --help (-h short option)");
    System.err.println("               Displays this help screen.");
    System.err.println();
    System.err.println("Options:");
    System.err.println("       --in (-i short option)");
    System.err.println("               Source directory for the import.");
    System.err.println();
    System.err.println("       --out (-o short option)");
    System.err.println("               Target directory for the imported file. If the directory does not exist");
    System.err.println("               yet, it will be created.");
    System.err.println();
    System.err.println("       --quiet (-q short option)");
    System.err.println("               Starts the backup in quiet mode. Like this, only error messages.");
    System.err.println("               will be written to stderr.");
    System.err.println();
    System.err.println("       --site (-s short option)");
    System.err.println("               The site to export. This option will set the collection accordingly,");
    System.err.println("               therefore the 'collection' option is not allowed in conjunction with 'site'.");
    System.exit(0);
  }

  /**
   * Traverses the directory structure and calls the callback on every file and
   * folder.
   * 
   * @param root
   *          the root directory
   * @param callback
   *          the callback
   * @param clipboard
   *          the shared memory
   * @throws XMLDBException
   */
  private void traverse(File root, ImporterCallback callback) throws Exception {
    Stack<File> directories = new Stack<File>();
    directories.push(root);
    try {
      while (!directories.empty()) {

        // process current directory
        File current = directories.pop();
        if (!callback.folderImported(current)) {
          System.err.println("Error importing folder " + current);
          return;
        }

        File[] files = current.listFiles();
        for (int i = 0; i < files.length; i++) {
          try {
            if (files[i].isDirectory()) {
              directories.push(files[i]);
            } else if (!"__contents__.xml".equals(files[i].getName()) && !".DS_Store".equals(files[i].getName()) && !"repository.xml".equals(files[i].getName())) {
              if (!callback.fileImported(files[i])) {
                System.err.println("Error importing file " + current);
                return;
              }
            }
          } catch (Exception e) {
            System.err.println("Error importing " + files[i] + ": " + e.getMessage());
          }
        }
      }
    } catch (Throwable t) {
      System.err.println("Error importing " + root + ": " + t.getMessage());
      return;
    }
    if (errorCount > 0) {
      System.out.println("\n" + errorCount + " errors occured:\n");
      for (int i = 0; i < errorUrls.size(); i++) {
        System.out.println("\n" + errorUrls.get(i));
      }
    }
  }

}