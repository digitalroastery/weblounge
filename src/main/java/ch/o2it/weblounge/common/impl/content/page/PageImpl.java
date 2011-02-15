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

package ch.o2it.weblounge.common.impl.content.page;

import ch.o2it.weblounge.common.content.ResourceContent;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.page.Composer;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.content.page.PageContentListener;
import ch.o2it.weblounge.common.content.page.PagePreviewMode;
import ch.o2it.weblounge.common.content.page.PageTemplate;
import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.content.page.PageletRenderer;
import ch.o2it.weblounge.common.content.page.PageletURI;
import ch.o2it.weblounge.common.impl.content.ResourceImpl;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A <code>Page</code> encapsulates all data that is attached with a site URL.
 */
public class PageImpl extends ResourceImpl<ResourceContent> implements Page {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(PageImpl.class);

  /** Renderer identifier */
  protected String template = null;

  /** Layout identifier */
  protected String layout = null;

  /** The preview pagelets */
  protected List<Pagelet> preview = null;

  /** The pagelet container */
  protected Map<String, List<Pagelet>> composers = null;

  /** The page content listeners */
  private List<PageContentListener> contentListeners = null;

  /**
   * Creates a new page for the given page uri.
   * 
   * @param uri
   *          the page uri
   */
  public PageImpl(ResourceURI uri) {
    super(uri);
    this.composers = new HashMap<String, List<Pagelet>>();
  }

  /**
   * Returns the layout associated with this page.
   * 
   * @return the associated layout
   */
  public String getLayout() {
    return layout;
  }

  /**
   * Sets the layout that is used to determine default content and initial
   * layout.
   * 
   * @param layout
   *          the layout identifier
   */
  public void setLayout(String layout) {
    this.layout = layout;
  }

  /**
   * Returns the template that is used to render this page.
   * 
   * @return the renderer
   */
  public String getTemplate() {
    return template;
  }

  /**
   * Sets the renderer that is used to render this page.
   * 
   * @param template
   *          the template identifier
   */
  public void setTemplate(String template) {
    this.template = template;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getComposer(java.lang.String)
   */
  public Composer getComposer(String composerId) {
    Composer composer = null;
    List<Pagelet> pagelets = composers.get(composerId);
    if (pagelets != null)
      composer = new ComposerImpl(composerId, pagelets);
    return composer;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.page.Page#getStage()
   */
  public Composer getStage() {
    PageTemplate t = null;
    if (StringUtils.isNotBlank(template)) {
      t = uri.getSite().getTemplate(template);
    } else {
      t = uri.getSite().getDefaultTemplate();
      logger.warn("Page {} has no template associated", uri);
    }
    if (t == null)
      throw new IllegalStateException("Page template '" + template + "' not found");
    String stage = t.getStage();
    if (StringUtils.isBlank(stage))
      throw new IllegalStateException("Page template '" + template + "' does not define a stage");

    Composer c = getComposer(stage);
    if (c == null) {
      List<Pagelet> pagelets = new ArrayList<Pagelet>();
      c = new ComposerImpl(stage, pagelets);
      composers.put(stage, pagelets);
    }
      
    return c;
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getComposers()
   */
  public Composer[] getComposers() {
    List<Composer> composerList = new ArrayList<Composer>();
    for (String name : composers.keySet()) {
      composerList.add(new ComposerImpl(name, composers.get(name)));
    }
    return composerList.toArray(new Composer[composerList.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#addPagelet(ch.o2it.weblounge.common.content.page.Pagelet,
   *      java.lang.String)
   */
  public Pagelet addPagelet(Pagelet pagelet, String composer) {
    List<Pagelet> c = composers.get(composer);
    int position = (c == null) ? 0 : c.size();
    return addPagelet(pagelet, composer, position);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#addPagelet(ch.o2it.weblounge.common.content.page.Pagelet,
   *      java.lang.String, int)
   */
  public Pagelet addPagelet(Pagelet pagelet, String composer, int position) {
    List<Pagelet> c = composers.get(composer);
    if (c == null) {
      c = new ArrayList<Pagelet>();
      composers.put(composer, c);
    }

    // Test position
    if (position < 0 || position > c.size())
      throw new IndexOutOfBoundsException("There are only " + c.size() + " pagelets in the composer");

    // Insert
    if (position < c.size()) {
      c.add(position, pagelet);
      for (int i = position + 1; i < c.size(); i++) {
        c.get(i).getURI().setPosition(i);
      }
    }

    // Append
    else {
      c.add(pagelet);
    }

    // Adjust pagelet location
    PageletURI location = pagelet.getURI();
    if (location == null) {
      location = new PageletURIImpl(uri, composer, position);
      pagelet.setURI(location);
    } else {
      location.setURI(uri);
      location.setComposer(composer);
      location.setPosition(position);
    }
    return pagelet;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getPagelets()
   */
  public Pagelet[] getPagelets() {
    List<Pagelet> result = new ArrayList<Pagelet>();
    for (List<Pagelet> pagelets : composers.values()) {
      result.addAll(pagelets);
    }
    return result.toArray(new Pagelet[result.size()]);
  }

  /**
   * Returns the pagelets that are contained in the specified composer.
   * 
   * @param composer
   *          the composer identifier
   * @return the pagelets
   */
  public Pagelet[] getPagelets(String composer) {
    List<Pagelet> c = composers.get(composer);
    if (c == null) {
      c = new ArrayList<Pagelet>();
    }
    Pagelet[] pagelets = new Pagelet[c.size()];
    return c.toArray(pagelets);
  }

  /**
   * Returns a copy of the pagelets of the given module and renderer that are
   * contained in the specified composer.
   * 
   * @param composer
   *          the composer identifier
   * @param module
   *          the module identifier
   * @param id
   *          the renderer id
   * @return the pagelets
   */
  public Pagelet[] getPagelets(String composer, String module, String id) {
    List<Pagelet> l = new ArrayList<Pagelet>();
    List<Pagelet> c = composers.get(composer);
    if (c != null) {
      l.addAll(c);
      int i = 0;
      while (i < l.size()) {
        Pagelet p = l.get(i);
        if (!p.getModule().equals(module) || !p.getIdentifier().equals(id)) {
          l.remove(i);
        } else {
          i++;
        }
      }
    }
    Pagelet[] pagelets = new Pagelet[l.size()];
    return l.toArray(pagelets);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#removePagelet(java.lang.String,
   *      int)
   */
  public Pagelet removePagelet(String composer, int position) {
    List<Pagelet> pagelets = composers.get(composer);

    // Test index
    if (pagelets == null || pagelets.size() < position)
      throw new IndexOutOfBoundsException("No pagelet found at position " + position);

    // Remove the pagelet and update uris of following pagelets
    Pagelet pagelet = pagelets.remove(position);
    for (int i = position; i < pagelets.size(); i++) {
      pagelets.get(i).getURI().setPosition(i);
    }
    return pagelet;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getPreview()
   */
  public Pagelet[] getPreview() {
    Site site = getURI().getSite();
    PageTemplate t = null;
    if (StringUtils.isNotBlank(template)) {
      t = site.getTemplate(template);
    } else {
      t = site.getDefaultTemplate();
      logger.warn("Page {} has no template associated, using site default '{}'", uri, t.getIdentifier());
    }
    if (preview == null && t == null) {
      logger.warn("Can't calculate the page preview due to missing page template '{}'", template);
      return new Pagelet[] {};
    } else if (preview == null && t.getStage() == null) {
      logger.warn("Can't calculate the page preview due to missing stage definition in page template '{}'", template);
      return new Pagelet[] {};
    } else if (preview == null) {
      this.preview = new ArrayList<Pagelet>();
      List<Pagelet> stage = composers.get(t.getStage());
      Set<PageletRenderer> previewRenderers = new HashSet<PageletRenderer>();
      if (stage != null) {
        for (Pagelet p : stage) {

          // Load the pagelet's module
          Module m = site.getModule(p.getModule());
          if (m == null) {
            logger.warn("Skipping pagelet '{}' for preview calculation: module '{}' can't be found", p, p.getModule());
            continue;
          }

          // Load the pagelet's renderer
          PageletRenderer r = m.getRenderer(p.getIdentifier());
          if (r == null) {
            logger.warn("Skipping pagelet '{}' for preview calculation: pagelet renderer '{}' can't be found", p, p.getIdentifier());
            continue;
          }

          // Evaluate the preview mode
          PagePreviewMode previewMode = r.getPreviewMode();
          if (previewMode.equals(PagePreviewMode.First.equals(previewMode) && !previewRenderers.contains(r))) {
            preview.add(p);
            previewRenderers.add(r);
          } else if (PagePreviewMode.All.equals(previewMode)) {
            preview.add(p);
            previewRenderers.add(r);
          } else if (PagePreviewMode.Boundary.equals(previewMode)) {
            preview.clear();
            for (Pagelet p2 : stage) {
              if (p2.equals(p))
                break;
              preview.add(p2);
            }
            break;
          }
        }
      }
    }
    return preview.toArray(new Pagelet[preview.size()]);
  }

  /**
   * Adds a <code>PageContentListener</code> to this page, who will be notified
   * (amongst others) about new, moved, deleted or altered pagelets.
   * 
   * @param listener
   *          the new page content listener
   */
  public void addPageContentListener(PageContentListener listener) {
    if (contentListeners == null)
      contentListeners = new ArrayList<PageContentListener>();
    contentListeners.add(listener);
  }

  /**
   * Removes a <code>PageContentListener</code> from this page.
   * 
   * @param listener
   *          the page content listener
   */
  public void removePageContentListener(PageContentListener listener) {
    if (contentListeners == null)
      return;
    contentListeners.remove(listener);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.impl.content.ResourceImpl#toXmlRootTag()
   */
  @Override
  protected String toXmlRootTag() {
    return "page";
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.impl.content.ResourceImpl#toXmlHead(java.lang.StringBuffer)
   */
  @Override
  protected void toXmlHead(StringBuffer buffer) {
    if (template != null) {
      buffer.append("<template>");
      buffer.append(template);
      buffer.append("</template>");
    }
    if (layout != null) {
      buffer.append("<layout>");
      buffer.append(layout);
      buffer.append("</layout>");
    }
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.impl.content.ResourceImpl#toXmlBody(java.lang.StringBuffer)
   */
  @Override
  protected StringBuffer toXmlBody(StringBuffer buffer) {
    for (Map.Entry<String, List<Pagelet>> entry : composers.entrySet()) {
      buffer.append("<composer id=\"");
      buffer.append(entry.getKey());
      buffer.append("\">");
      for (Pagelet pagelet : entry.getValue()) {
        buffer.append(pagelet.toXml());
      }
      buffer.append("</composer>");
    }
    return super.toXmlBody(buffer);
  }

}