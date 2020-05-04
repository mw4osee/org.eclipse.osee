/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.rest.internal.cpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.cpa.DecisionUpdate;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.user.AtsCoreUsers;
import org.eclipse.osee.ats.api.user.AtsUser;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.api.workflow.WorkItemType;
import org.eclipse.osee.ats.api.workflow.transition.TransitionOption;
import org.eclipse.osee.ats.api.workflow.transition.TransitionResults;
import org.eclipse.osee.ats.core.workflow.state.TeamState;
import org.eclipse.osee.ats.core.workflow.transition.TransitionHelper;
import org.eclipse.osee.ats.core.workflow.transition.TransitionManager;
import org.eclipse.osee.framework.jdk.core.result.XResultData;
import org.eclipse.osee.framework.jdk.core.util.Collections;

/**
 * @author Donald G. Dunne
 */
public class DecisionUpdater {

   private final DecisionUpdate update;
   private final AtsApi atsApi;

   public DecisionUpdater(final DecisionUpdate update, AtsApi atsApi) {
      this.update = update;
      this.atsApi = atsApi;
   }

   public XResultData update() {
      XResultData rd = new XResultData(false);
      Collection<IAtsTeamWorkflow> teamWfs =
         atsApi.getQueryService().createQuery(WorkItemType.TeamWorkflow).andAtsIds(update.getIds()).getItems();
      IAtsChangeSet changes =
         atsApi.getStoreService().createAtsChangeSet("Update CPA Decision", AtsCoreUsers.SYSTEM_USER);
      for (IAtsTeamWorkflow teamWf : teamWfs) {
         if (!rd.isErrors()) {
            updateRationale(update, changes, teamWf, rd);
         }
         if (!rd.isErrors()) {
            updateDuplicatedPcrId(update, changes, teamWf, rd);
         }
         if (!rd.isErrors()) {
            updateApplicability(update, changes, teamWf, rd);
         }
         if (!rd.isErrors()) {
            updateAssignees(update, changes, teamWf, rd);
         }
      }
      if (!rd.isErrors() && !changes.isEmpty()) {
         changes.execute();
      }
      return rd;
   }

   private void updateApplicability(final DecisionUpdate update, IAtsChangeSet changes, IAtsTeamWorkflow teamWf, XResultData rd) {
      // update applicability - transition
      if (update.getApplicability() != null) {
         String appl = update.getApplicability();
         if (appl.isEmpty()) {
            // transition to analyze
            changes.deleteAttributes(teamWf, AtsAttributeTypes.ApplicableToProgram);

            TransitionHelper helper = new TransitionHelper("Transition " + teamWf.getAtsId(), Arrays.asList(teamWf),
               TeamState.Analyze.getName(), teamWf.getAssignees(), "", changes, atsApi,
               TransitionOption.OverrideAssigneeCheck);
            helper.setTransitionUser(AtsCoreUsers.SYSTEM_USER);
            TransitionManager mgr = new TransitionManager(helper);
            TransitionResults results = mgr.handleAll();
            if (!results.isEmpty()) {
               rd.error(results.toString());
            }

         } else {
            // transition to completed
            changes.setSoleAttributeValue(teamWf, AtsAttributeTypes.ApplicableToProgram, appl);

            TransitionHelper helper = new TransitionHelper("Transition " + teamWf.getAtsId(), Arrays.asList(teamWf),
               TeamState.Completed.getName(), null, "", changes, atsApi, TransitionOption.OverrideAssigneeCheck);
            helper.setTransitionUser(AtsCoreUsers.SYSTEM_USER);
            TransitionManager mgr = new TransitionManager(helper);
            TransitionResults results = mgr.handleAll();
            if (!results.isEmpty()) {
               rd.error(results.toString());
            }
         }
      }
   }

   private void updateAssignees(final DecisionUpdate update, IAtsChangeSet changes, IAtsTeamWorkflow teamWf, XResultData rd) {
      if (update.getAssignees() != null) {
         List<AtsUser> assignees = new ArrayList<>();
         for (String userId : update.getAssignees()) {
            AtsUser user = atsApi.getUserService().getUserByUserId(userId);
            if (user == null) {
               rd.errorf("Invalid userId [%s]", userId);
            }
            assignees.add(user);
         }
         List<AtsUser> currentAssignees = teamWf.getAssignees();
         if (assignees.isEmpty()) {
            assignees.add(AtsCoreUsers.UNASSIGNED_USER);
         } else if (assignees.size() > 1 && assignees.contains(AtsCoreUsers.UNASSIGNED_USER)) {
            assignees.remove(AtsCoreUsers.UNASSIGNED_USER);
         }
         if (!Collections.isEqual(currentAssignees, assignees)) {
            teamWf.getStateMgr().setAssignees(assignees);
            changes.add(teamWf);
         }
      }
   }

   private void updateRationale(final DecisionUpdate update, IAtsChangeSet changes, IAtsTeamWorkflow teamWf, XResultData rd) {
      // update rationale
      if (update.getRationale() != null) {
         if (update.getRationale().equals("")) {
            changes.deleteAttributes(teamWf, AtsAttributeTypes.Rationale);
         } else {
            changes.setSoleAttributeValue(teamWf, AtsAttributeTypes.Rationale, update.getRationale());
         }
      }
   }

   private void updateDuplicatedPcrId(final DecisionUpdate update, IAtsChangeSet changes, IAtsTeamWorkflow teamWf, XResultData rd) {
      if (update.getDuplicatedPcrId() != null) {
         if (update.getDuplicatedPcrId().equals("")) {
            changes.deleteAttributes(teamWf, AtsAttributeTypes.DuplicatedPcrId);
         } else {
            changes.setSoleAttributeValue(teamWf, AtsAttributeTypes.DuplicatedPcrId, update.getDuplicatedPcrId());
         }
      }
   }

}
