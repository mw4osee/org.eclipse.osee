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
package org.eclipse.osee.ats.mocks;

import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.ats.api.components.AtsSearchHeaderComponent;
import org.eclipse.osee.display.api.data.WebId;

/**
 * @author John Misinco
 */
public class MockAtsSearchHeaderComponent implements AtsSearchHeaderComponent {

   List<WebId> programs = new LinkedList<WebId>();
   List<WebId> builds = new LinkedList<WebId>();
   WebId selectedProgram, selectedBuild;
   String errorMessage = "";
   boolean clearAllCalled = false;

   public boolean isClearAllCalled() {
      return clearAllCalled;
   }

   public List<WebId> getPrograms() {
      return programs;
   }

   public List<WebId> getBuilds() {
      return builds;
   }

   public WebId getSelectedProgram() {
      return selectedProgram;
   }

   public WebId getSelectedBuild() {
      return selectedBuild;
   }

   public String getErrorMessage() {
      return errorMessage;
   }

   @Override
   public void clearAll() {
      clearAllCalled = true;
      programs.clear();
      builds.clear();
   }

   @Override
   public void setErrorMessage(String message) {
      errorMessage = message;
   }

   @Override
   public void addProgram(WebId program) {
      programs.add(program);
   }

   @Override
   public void clearBuilds() {
      builds.clear();
   }

   @Override
   public void addBuild(WebId build) {
      builds.add(build);
   }

   @Override
   public void setSearchCriteria(WebId program, WebId build, boolean nameOnly, String searchPhrase) {
   }

   @Override
   public void setProgram(WebId program) {
      selectedProgram = program;
   }

   @Override
   public void setBuild(WebId build) {
      selectedBuild = build;
   }

   @Override
   public void setShowVerboseSearchResults(boolean showVerboseSearchResults) {
   }

}