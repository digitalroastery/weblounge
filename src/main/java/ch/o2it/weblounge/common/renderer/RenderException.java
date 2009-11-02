/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.renderer;

/**
 * A <code>RendererException</code> is thrown if an exceptional state is reached
 * when executing a <code>Renderer</code> to create the output for either a
 * page or a single page name.
 */
public class RenderException extends RuntimeException {

  /** The serial version id */
  private static final long serialVersionUID = -857423335304601456L;

  /** Renderer name, e. g. <code>XSLRenderer</code> */
  private Renderer renderer_ = null;

  /** Rendering method, e. g. <code>HTML</code>" */
  private String method_ = null;

  /**
   * Creates a new <code>RendererException</code> providing the information
   * passed by the parameters.
   * 
   * @param renderer
   *          the renderer, e. g. <code>XLSElementRenderer</code>
   * @param method
   *          the rendering output method, e. g. <code>HTML</code>
   */
  public RenderException(Renderer renderer, String method) {
    renderer_ = renderer;
    method_ = method;
  }

  /**
   * Creates a new <code>RendererException</code> providing the information
   * passed by the parameters.
   * 
   * @param renderer
   *          the renderer, e. g. <code>XLSElementRenderer</code>
   * @param method
   *          the rendering output method, e. g. <code>HTML</code>
   * @param t
   *          the exception caught when executing the renderer
   */
  public RenderException(Renderer renderer, String method, Throwable t) {
    super(t);
    renderer_ = renderer;
    method_ = method;
  }

  /**
   * Returns the renderer that raised this exception.
   * 
   * @return the renderer
   */
  public Renderer getRenderer() {
    return renderer_;
  }

  /**
   * Returns the rendering method, e. g. <code>HTML</code>.
   * 
   * @return the rendering output method
   */
  public String getRenderingMethod() {
    return method_;
  }

  /**
   * Returns the exception detail message.
   * 
   * @see java.lang.Throwable#getMessage()
   */
  public String getMessage() {
    if (getCause() != null) {
      return getCause().getMessage();
    } else {
      return super.getMessage();
    }
  }

}