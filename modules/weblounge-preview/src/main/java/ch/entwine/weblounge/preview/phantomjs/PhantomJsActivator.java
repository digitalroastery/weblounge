/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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

package ch.entwine.weblounge.preview.phantomjs;

import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.page.PagePreviewGenerator;
import ch.entwine.weblounge.common.impl.util.process.ProcessExcecutorException;
import ch.entwine.weblounge.common.impl.util.process.ProcessExecutor;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Activator that will register the page preview generator depending on the
 * availability of <code>PhantomJS</code> binaries.
 */
public class PhantomJsActivator {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(PhantomJsActivator.class);

  /** The page preview generator */
  private PhantomJsPagePreviewGenerator previewGenerator = null;

  /** The service reference */
  private ServiceRegistration service = null;

  /**
   * Callback from the OSGi container on component activation.
   * 
   * @param ctx
   *          the component context
   */
  void activate(ComponentContext ctx) {
    if (phantomJSAvailable()) {
      logger.debug("Registering PhantomJS page preview generator");
      String[] classes = new String[] {
          PreviewGenerator.class.getName(),
          PagePreviewGenerator.class.getName() };
      previewGenerator = new PhantomJsPagePreviewGenerator();
      previewGenerator.activate(ctx);
      service = ctx.getBundleContext().registerService(classes, previewGenerator, null);
      logger.info("PhantomJS page preview generator registered");
    } else {
      logger.info("PhantomJS not found");
    }
  }

  /**
   * Callback from the OSGi container on component inactivation.
   * 
   * @param ctx
   *          the component context
   */
  void deactivate(ComponentContext ctx) {
    if (service == null)
      return;
    try {
      logger.debug("Unregistering PhantomJS page preview generator");
      previewGenerator.deactivate();
      ctx.getBundleContext().ungetService(service.getReference());
    } catch (Throwable t) {
      // Never mind, seems like we are shutting down
    } finally {
      previewGenerator = null;
      service = null;
    }
  }

  /**
   * Returns <code>true</code> if the <code>PhantomJS</code> library is
   * available.
   * 
   * @return <code>true</code> if the library is available
   */
  private boolean phantomJSAvailable() {
    PhantomJsProcessExecutor executor = new PhantomJsProcessExecutor();
    try {
      executor.execute();
    } catch (ProcessExcecutorException e) {
      logger.warn("PhantomJS availability check failed: {}", e.getMessage());
      return false;
    }
    return executor.getExitCode() == 0;
  }

  /**
   * This process executor is used to run <code>PhantomJS</code> in the system
   * shell.
   */
  private static class PhantomJsProcessExecutor extends ProcessExecutor<IOException> {

    /** The exit code */
    private int exitCode = 0;

    /**
     * Creates a process executor to check for the PhantomJS availability.
     */
    protected PhantomJsProcessExecutor() {
      super("phantomjs --version");
    }

    /**
     * {@inheritDoc}
     * 
     * @see ch.entwine.weblounge.common.impl.util.process.ProcessExecutor#onProcessFinished(int)
     */
    @Override
    protected void onProcessFinished(int exitCode) throws IOException {
      super.onProcessFinished(exitCode);
      this.exitCode = exitCode;
    }

    /**
     * Returns the processe's exit code.
     * 
     * @return the exitCode
     */
    public int getExitCode() {
      return exitCode;
    }

  }

}
