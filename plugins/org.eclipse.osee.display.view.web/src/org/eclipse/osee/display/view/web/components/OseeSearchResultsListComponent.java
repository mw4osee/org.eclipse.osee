/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/

package org.eclipse.osee.display.view.web.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.eclipse.osee.display.api.components.SearchResultComponent;
import org.eclipse.osee.display.api.components.SearchResultsListComponent;
import org.eclipse.osee.display.view.web.CssConstants;
import com.vaadin.Application;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Shawn F. Cook
 */
@SuppressWarnings("serial")
public class OseeSearchResultsListComponent extends VerticalLayout implements SearchResultsListComponent {

   VerticalLayout bottomSpacer = new VerticalLayout();
   HorizontalLayout manySearchResultsHorizLayout = new HorizontalLayout();

   @Override
   public void attach() {
      super.attach();
      Application app = this.getApplication();
   }

   public OseeSearchResultsListComponent() {
      addComponent(manySearchResultsHorizLayout);
      Label spacer2 = new Label("");
      spacer2.setHeight(15, UNITS_PIXELS);
      addComponent(spacer2);

      bottomSpacer.setSizeFull();
      addComponent(bottomSpacer);
      setExpandRatio(bottomSpacer, 1.0f);
   }

   @Override
   public void clearAll() {
      Collection<Component> removeTheseComponents = new ArrayList<Component>();
      for (Iterator<Component> iter = getComponentIterator(); iter.hasNext();) {
         Component component = iter.next();
         if (component.getClass() == OseeSearchResultComponent.class) {
            removeTheseComponents.add(component);
         }
      }

      //Remove the components
      for (Component component : removeTheseComponents) {
         removeComponent(component);
      }
   }

   private int getManySearchResultComponents() {
      int many = 0;
      for (Iterator<Component> iter = getComponentIterator(); iter.hasNext();) {
         Component component = iter.next();
         if (component.getClass() == OseeSearchResultComponent.class) {
            many++;
         }
      }
      return many;
   }

   private void updateManySearchResultsLabel() {
      int manySearchResultComponents = getManySearchResultComponents();
      manySearchResultsHorizLayout.removeAllComponents();

      Label manySearchResults = new Label(String.format("[%d] ", manySearchResultComponents));
      Label manySearchResults_suffix = new Label("search result(s) found.");
      manySearchResultsHorizLayout.addComponent(manySearchResults);
      manySearchResultsHorizLayout.addComponent(manySearchResults_suffix);
      manySearchResults.setStyleName(CssConstants.OSEE_SEARCHRESULT_MATCH_MANY);

   }

   @Override
   public SearchResultComponent createSearchResult() {
      OseeSearchResultComponent searchResultComp = new OseeSearchResultComponent();
      int spacerIndex = this.getComponentIndex(bottomSpacer);
      addComponent(searchResultComp, spacerIndex);
      updateManySearchResultsLabel();

      return searchResultComp;
   }

   @Override
   public void setErrorMessage(String message) {
   }
}
