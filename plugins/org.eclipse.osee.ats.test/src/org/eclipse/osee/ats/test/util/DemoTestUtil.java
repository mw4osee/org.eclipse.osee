/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.test.util;

import static org.eclipse.osee.framework.core.enums.DeletionFlag.EXCLUDE_DELETED;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import junit.framework.Assert;
import org.eclipse.osee.ats.artifact.ActionArtifact;
import org.eclipse.osee.ats.artifact.ActionableItemArtifact;
import org.eclipse.osee.ats.artifact.TaskArtifact;
import org.eclipse.osee.ats.artifact.TeamDefinitionArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.ActionManager;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.exception.OseeAuthenticationException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.PurgeArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.util.ChangeType;
import org.eclipse.osee.support.test.util.DemoActionableItems;
import org.eclipse.osee.support.test.util.DemoArtifactTypes;
import org.eclipse.osee.support.test.util.DemoSawBuilds;
import org.eclipse.osee.support.test.util.DemoUsers;
import org.eclipse.osee.support.test.util.DemoWorkType;
import org.eclipse.osee.support.test.util.TestUtil;

/**
 * @author Donald G. Dunne
 */
public class DemoTestUtil {
   public static Map<DemoWorkType, Artifact> unCommittedWorkflows;
   public static Map<DemoWorkType, Artifact> committedWorkflows;
   public static TeamWorkFlowArtifact toolsTeamWorkflow;

   public static Result isDbPopulatedWithDemoData() throws Exception {
      Branch branch = BranchManager.getBranchByGuid(DemoSawBuilds.SAW_Bld_1.getGuid());

      Collection<Artifact> robotArtifacts =
         ArtifactQuery.getArtifactListFromTypeAndName(CoreArtifactTypes.SoftwareRequirement, "%Robot%", branch);
      if (robotArtifacts.size() < 6) {
         return new Result(
            String.format(
               "Expected at least 6 Software Requirements with name \"Robot\" but found [%s].  Database is not be populated with demo data.",
               robotArtifacts.size()));
      }
      return Result.TrueResult;
   }

   public static Collection<String> getTaskTitles(boolean firstTaskWorkflow) {
      if (firstTaskWorkflow) {
         firstTaskWorkflow = false;
         return Arrays.asList("Look into Graph View.", "Redesign how view shows values.",
            "Discuss new design with Senior Engineer", "Develop prototype", "Show prototype to management",
            "Create development plan", "Create test plan", "Make changes");
      } else {
         return Arrays.asList("Document how Graph View works", "Update help contents", "Review new documentation",
            "Publish documentation to website", "Remove old viewer", "Deploy release");
      }
   }

   public static int getNumTasks() {
      return getTaskTitles(false).size() + getTaskTitles(true).size();
   }

   public static User getDemoUser(DemoUsers demoUser) throws OseeCoreException {
      return UserManager.getUserByName(demoUser.getName());
   }

   /**
    * Creates an action with the name title and demo code workflow
    */
   public static TeamWorkFlowArtifact createSimpleAction(String title, SkynetTransaction transaction) throws OseeCoreException {
      ActionArtifact actionArt =
         ActionManager.createAction(null, title, "Description", ChangeType.Improvement, "2", false, null,
            ActionableItemArtifact.getActionableItems(Arrays.asList(DemoActionableItems.SAW_Code.getName())),
            new Date(), UserManager.getUser(), transaction);

      TeamWorkFlowArtifact teamArt = null;
      for (TeamWorkFlowArtifact team : actionArt.getTeamWorkFlowArtifacts()) {
         if (team.getTeamDefinition().getName().contains("Code")) {
            teamArt = team;
         }
      }
      return teamArt;
   }

   public static TeamWorkFlowArtifact addTeamWorkflow(ActionArtifact actionArt, String title, SkynetTransaction transaction) throws OseeCoreException {
      Set<ActionableItemArtifact> actionableItems =
         ActionableItemArtifact.getActionableItems(Arrays.asList(DemoActionableItems.SAW_Test.getName()));
      Collection<TeamDefinitionArtifact> teamDefs = TeamDefinitionArtifact.getImpactedTeamDefs(actionableItems);

      ActionManager.createTeamWorkflow(actionArt, teamDefs.iterator().next(), actionableItems,
         Arrays.asList(UserManager.getUser()), transaction, new Date(), UserManager.getUser());

      TeamWorkFlowArtifact teamArt = null;
      for (TeamWorkFlowArtifact team : actionArt.getTeamWorkFlowArtifacts()) {
         if (team.getTeamDefinition().getName().contains("Test")) {
            teamArt = team;
         }
      }
      return teamArt;
   }

   /**
    * Create tasks named title + <num>
    */
   public static Collection<TaskArtifact> createSimpleTasks(TeamWorkFlowArtifact teamArt, String title, int numTasks, SkynetTransaction transaction) throws Exception {
      List<String> names = new ArrayList<String>();
      for (int x = 1; x < numTasks + 1; x++) {
         names.add(title + " " + x);
      }
      return teamArt.createTasks(names, Arrays.asList(UserManager.getUser()), new Date(), UserManager.getUser(),
         transaction);
   }

   /**
    * Deletes all artifacts with names that start with any title given
    */
   public static void cleanupSimpleTest(Collection<String> titles) throws Exception {
      List<Artifact> artifacts = new ArrayList<Artifact>();
      for (String title : titles) {
         artifacts.addAll(ArtifactQuery.getArtifactListFromName(title + "%", AtsUtil.getAtsBranch(), EXCLUDE_DELETED));
      }
      new PurgeArtifacts(artifacts).execute();
      TestUtil.sleep(4000);
   }

   public static TeamWorkFlowArtifact getToolsTeamWorkflow() throws OseeCoreException {
      if (toolsTeamWorkflow == null) {
         for (Artifact art : ArtifactQuery.getArtifactListFromName("Button S doesn't work on help",
            AtsUtil.getAtsBranch(), EXCLUDE_DELETED)) {
            if (art instanceof TeamWorkFlowArtifact) {
               toolsTeamWorkflow = (TeamWorkFlowArtifact) art;
            }
         }
      }
      return toolsTeamWorkflow;
   }

   public static Artifact getUncommittedActionWorkflow(DemoWorkType demoWorkType) throws OseeCoreException {
      if (unCommittedWorkflows == null) {
         unCommittedWorkflows = new HashMap<DemoWorkType, Artifact>();
         for (Artifact art : ArtifactQuery.getArtifactListFromName(
            "SAW (uncommitted) More Reqt Changes for Diagram View", AtsUtil.getAtsBranch(), EXCLUDE_DELETED)) {
            if (art.isOfType(DemoArtifactTypes.DemoCodeTeamWorkflow)) {
               unCommittedWorkflows.put(DemoWorkType.Code, art);
            } else if (art.isOfType(DemoArtifactTypes.DemoTestTeamWorkflow)) {
               unCommittedWorkflows.put(DemoWorkType.Test, art);
            } else if (art.isOfType(DemoArtifactTypes.DemoReqTeamWorkflow)) {
               unCommittedWorkflows.put(DemoWorkType.Requirements, art);
            } else if (art.isOfType(AtsArtifactTypes.TeamWorkflow)) {
               unCommittedWorkflows.put(DemoWorkType.SW_Design, art);
            }
         }
      }
      return unCommittedWorkflows.get(demoWorkType);
   }

   public static Artifact getCommittedActionWorkflow(DemoWorkType demoWorkType) throws OseeCoreException {
      if (committedWorkflows == null) {
         committedWorkflows = new HashMap<DemoWorkType, Artifact>();
         for (Artifact art : ArtifactQuery.getArtifactListFromName("SAW (committed) Reqt Changes for Diagram View",
            AtsUtil.getAtsBranch(), EXCLUDE_DELETED)) {
            if (art.isOfType(DemoArtifactTypes.DemoCodeTeamWorkflow)) {
               committedWorkflows.put(DemoWorkType.Code, art);
            } else if (art.isOfType(DemoArtifactTypes.DemoTestTeamWorkflow)) {
               committedWorkflows.put(DemoWorkType.Test, art);
            } else if (art.isOfType(DemoArtifactTypes.DemoReqTeamWorkflow)) {
               committedWorkflows.put(DemoWorkType.Requirements, art);
            } else if (art.isOfType(AtsArtifactTypes.TeamWorkflow)) {
               committedWorkflows.put(DemoWorkType.SW_Design, art);
            }
         }
      }
      return committedWorkflows.get(demoWorkType);
   }

   /**
    * Deletes any artifact with name that starts with title
    */
   public static void cleanupSimpleTest(String title) throws Exception {
      cleanupSimpleTest(Arrays.asList(title));
   }

   public static void setUpTest() throws Exception {
      try {
         // This test should only be run on test db
         Assert.assertFalse(AtsUtil.isProductionDb());
         // Confirm test setup with demo data
         Result result = isDbPopulatedWithDemoData();
         Assert.assertTrue(result.getText(), result.isTrue());
         // Confirm user is Joe Smith
         Assert.assertTrue("User \"Joe Smith\" does not exist in DB.  Run Demo DBInit prior to this test.",
            UserManager.getUserByUserId("Joe Smith") != null);
         // Confirm user is Joe Smith
         Assert.assertTrue(
            "Authenticated user should be \"Joe Smith\" and is not.  Check that Demo Application Server is being run.",
            UserManager.getUser().getUserId().equals("Joe Smith"));
      } catch (OseeAuthenticationException ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
         Assert.fail("Can't authenticate, either Demo Application Server is not running or Demo DbInit has not been performed");
      }

   }

}
