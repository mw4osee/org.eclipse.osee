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
package org.eclipse.osee.ats.core.workdef;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Donald G. Dunne
 */
public class DecisionReviewOption {

   public String name;
   public boolean followupRequired;
   public List<String> userIds;
   public List<String> userNames = new LinkedList<String>();

   public DecisionReviewOption(String name) {
      this(name, false, new ArrayList<String>());
   }

   public DecisionReviewOption(String name, boolean isFollowupRequired, List<String> userIds) {
      this.name = name;
      this.followupRequired = isFollowupRequired;
      if (userIds == null) {
         this.userIds = new ArrayList<String>();
      } else {
         this.userIds = userIds;
      }
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public List<String> getUserIds() {
      return userIds;
   }

   public void setUserIds(List<String> userIds) {
      this.userIds = userIds;
   }

   public boolean isFollowupRequired() {
      return followupRequired;
   }

   public void setFollowupRequired(boolean followupRequired) {
      this.followupRequired = followupRequired;
   }

   @Override
   public String toString() {
      return name + (followupRequired ? " - Followup Required" : " - No Followup Required");
   }

   public List<String> getUserNames() {
      return userNames;
   }

   public void setUserNames(List<String> userNames) {
      this.userNames = userNames;
   }

}
