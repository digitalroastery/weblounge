package ch.entwine.weblounge.tools.importer;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.UUID;

public class PagePathImporterCallback extends AbstractImporterCallback {

  /** name of the root partition */
  String rootPartition = "/home";

  PagePathImporterCallback(File src, File dest, String rootpartition) {
    super(src, dest);
    if (StringUtils.isNotBlank(rootpartition))
      this.rootPartition = rootpartition;
    if (!this.rootPartition.startsWith("/"))
      this.rootPartition = "/" + this.rootPartition;
  }  

  public boolean folderImported(File f) {
    if (f.isDirectory() && !f.equals(srcDir)) {
      String path = f.getPath().replace(this.srcDir.getAbsolutePath(), "");
      if (path.startsWith(this.rootPartition)) {
        path = path.substring(this.rootPartition.length(), path.length());
      }
      if (!path.startsWith("/")) {
        path = "/".concat(path);
      }

      if (!path.startsWith(".") && !path.contains("/.")) {
        UUID uuid = UUID.randomUUID();
        path = path.toLowerCase();
        ImporterState.getInstance().putUUID(path, uuid);
        System.out.println(path.concat(" --> ").concat(uuid.toString()));
      }
    }
    return true;
  }

  @Override
  public boolean fileImported(File f) throws Exception {
    // this method doesn't have to be implemented in this callback
    return true;
  }

}
