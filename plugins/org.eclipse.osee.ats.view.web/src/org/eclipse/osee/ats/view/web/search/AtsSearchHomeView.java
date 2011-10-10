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
package org.eclipse.osee.ats.view.web.search;

import org.eclipse.osee.ats.api.search.AtsSearchPresenter;
import org.eclipse.osee.ats.view.web.AtsUiApplication;
import org.eclipse.osee.ats.view.web.components.AtsSearchHeaderComponent;
import org.eclipse.osee.display.view.web.search.OseeSearchHeaderComponent;
import org.eclipse.osee.display.view.web.search.OseeSearchHomeView;

/**
 * @author Shawn F. Cook
 */
@SuppressWarnings("serial")
public class AtsSearchHomeView extends OseeSearchHomeView {

   private boolean populated = false;
   private AtsSearchPresenter searchPresenter = null;

   @Override
   public void attach() {
      if (!populated) {
         try {
            AtsUiApplication app = (AtsUiApplication) getApplication();
            searchPresenter = app.getAtsWebSearchPresenter();
            callInitSearchHome();
            createLayout();
         } catch (Exception e) {
            System.out.println("OseeArtifactNameLinkComponent.attach - CRITICAL ERROR: casting threw an exception.");
         }
      }
      populated = true;
   }

   @Override
   protected OseeSearchHeaderComponent getOseeSearchHeader() {
      return new AtsSearchHeaderComponent(true);
   }

   private void callInitSearchHome() {
      if (searchPresenter != null) {
         try {
            AtsSearchHeaderComponent atsSearchHeaderComp = (AtsSearchHeaderComponent) oseeSearchHeader;
            searchPresenter.initSearchHome(atsSearchHeaderComp);
         } catch (Exception e) {
            System.out.println("OseeArtifactNameLinkComponent.navigateTo - CRITICAL ERROR: casting threw an exception.");
         }
      }
   }

   @Override
   public void navigateTo(String requestedDataId) {
      callInitSearchHome();
   }
}
