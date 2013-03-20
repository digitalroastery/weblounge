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

package ch.entwine.weblounge.editor;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * The editor's "main" class
 */
@SuppressWarnings("serial")
public class WebloungeUI extends UI {

  @Override
  protected void init(VaadinRequest request) {
    getPage().setTitle("Weblounge Editor");

    TabSheet tabsheet = new TabSheet();
    setContent(tabsheet);

    tabsheet.addTab(createDesigner());
    tabsheet.addTab(createPageBrowser());
    tabsheet.addTab(createMediaBrowser());

  }

  private Component createDesigner() {
    final VerticalLayout layout = new VerticalLayout();
    layout.setCaption("Designer");
    layout.setMargin(true);

    BrowserFrame page = new BrowserFrame("Site", new ExternalResource("http://localhost:8080"));
    page.setSizeFull();
    layout.addComponent(page);

    return layout;
  }

  private Component createPageBrowser() {
    final VerticalLayout layout = new VerticalLayout();
    layout.setCaption("Pages");
    layout.setMargin(true);
    return layout;
  }

  private Component createMediaBrowser() {
    final VerticalLayout layout = new VerticalLayout();
    layout.setCaption("Media");
    layout.setMargin(true);
    return layout;
  }

}
