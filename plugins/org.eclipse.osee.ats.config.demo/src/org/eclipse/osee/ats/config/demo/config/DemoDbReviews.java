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
package org.eclipse.osee.ats.config.demo.config;

import static org.eclipse.osee.framework.core.enums.DeletionFlag.EXCLUDE_DELETED;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.ats.artifact.DecisionReviewArtifact;
import org.eclipse.osee.ats.artifact.DecisionReviewState;
import org.eclipse.osee.ats.artifact.DecisionReviewWorkflowManager;
import org.eclipse.osee.ats.artifact.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.artifact.PeerToPeerReviewState;
import org.eclipse.osee.ats.artifact.PeerToPeerReviewWorkflowManager;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.config.demo.internal.OseeAtsConfigDemoActivator;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.widgets.ReviewManager;
import org.eclipse.osee.ats.util.widgets.defect.DefectItem;
import org.eclipse.osee.ats.util.widgets.defect.DefectItem.Disposition;
import org.eclipse.osee.ats.util.widgets.defect.DefectItem.InjectionActivity;
import org.eclipse.osee.ats.util.widgets.defect.DefectItem.Severity;
import org.eclipse.osee.ats.util.widgets.role.UserRole;
import org.eclipse.osee.ats.util.widgets.role.UserRole.Role;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.support.test.util.DemoArtifactTypes;
import org.eclipse.osee.support.test.util.DemoUsers;

/**
 * @author Donald G. Dunne
 */
public class DemoDbReviews {

   public static void createReviews() throws Exception {
      SkynetTransaction transaction =
         new SkynetTransaction(AtsUtil.getAtsBranch(), "Demo Database Config - Create Reviews");
      createPeerToPeerReviews(transaction);
      createDecisionReviews(transaction);
      transaction.execute();
   }

   /**
    * Create Decision Reviews<br>
    * 1) ALREADY CREATED: Decision review created through the validation flag being set on a workflow<br>
    * 2) Decision in ReWork state w Joe Smith assignee and 2 reviewers<br>
    * 3) Decision in Complete state w Joe Smith assignee and completed<br>
    * <br>
    */
   public static void createDecisionReviews(SkynetTransaction transaction) throws Exception {

      Date createdDate = new Date();
      User createdBy = UserManager.getUser();

      OseeLog.log(OseeAtsConfigDemoActivator.class, Level.INFO, "Create Decision reviews");
      TeamWorkFlowArtifact firstTestArt = getSampleReviewTestWorkflows().get(0);
      TeamWorkFlowArtifact secondTestArt = getSampleReviewTestWorkflows().get(1);

      // Create a Decision review and transition to ReWork
      DecisionReviewArtifact reviewArt =
         ReviewManager.createValidateReview(firstTestArt, true, createdDate, createdBy, transaction);
      Result result =
         DecisionReviewWorkflowManager.transitionTo(reviewArt, DecisionReviewState.Followup, UserManager.getUser(),
            false, transaction);
      if (result.isFalse()) {
         throw new IllegalStateException("Failed transitioning review to Followup: " + result.getText());
      }
      reviewArt.persist(transaction);

      // Create a Decision review and transition to Completed
      reviewArt = ReviewManager.createValidateReview(secondTestArt, true, createdDate, createdBy, transaction);
      DecisionReviewWorkflowManager.transitionTo(reviewArt, DecisionReviewState.Completed, UserManager.getUser(),
         false, transaction);
      if (result.isFalse()) {
         throw new IllegalStateException("Failed transitioning review to Completed: " + result.getText());
      }
      reviewArt.persist(transaction);

   }

   private static List<TeamWorkFlowArtifact> reviewTestArts;

   private static List<TeamWorkFlowArtifact> getSampleReviewTestWorkflows() throws Exception {
      if (reviewTestArts == null) {
         reviewTestArts = new ArrayList<TeamWorkFlowArtifact>();
         for (String actionName : new String[] {"Button W doesn't work on%", "%Diagram Tree"}) {
            for (Artifact art : ArtifactQuery.getArtifactListFromName(actionName, AtsUtil.getAtsBranch(),
               EXCLUDE_DELETED)) {
               if (art.isOfType(DemoArtifactTypes.DemoTestTeamWorkflow)) {
                  reviewTestArts.add((TeamWorkFlowArtifact) art);
               }
            }
         }
      }
      return reviewTestArts;
   }

   /**
    * Create<br>
    * 1) PeerToPeer in Prepare state w Joe Smith assignee<br>
    * 2) PeerToPeer in Review state w Joe Smith assignee and 2 reviewers<br>
    * 3) PeerToPeer in Prepare state w Joe Smith assignee and completed<br>
    * <br>
    */
   public static void createPeerToPeerReviews(SkynetTransaction transaction) throws Exception {

      OseeLog.log(OseeAtsConfigDemoActivator.class, Level.INFO, "Create Peer To Peer reviews");
      TeamWorkFlowArtifact firstCodeArt = DemoDbUtil.getSampleCodeWorkflows().get(0);
      TeamWorkFlowArtifact secondCodeArt = DemoDbUtil.getSampleCodeWorkflows().get(1);

      // Create a PeerToPeer review and leave in Prepare state
      PeerToPeerReviewArtifact reviewArt =
         ReviewManager.createNewPeerToPeerReview(firstCodeArt, "Peer Review first set of code changes",
            firstCodeArt.getStateMgr().getCurrentStateName(), transaction);
      reviewArt.persist(transaction);

      // Create a PeerToPeer review and transition to Review state
      reviewArt =
         ReviewManager.createNewPeerToPeerReview(firstCodeArt, "Peer Review algorithm used in code",
            firstCodeArt.getStateMgr().getCurrentStateName(), transaction);
      List<UserRole> roles = new ArrayList<UserRole>();
      roles.add(new UserRole(Role.Author, DemoDbUtil.getDemoUser(DemoUsers.Joe_Smith)));
      roles.add(new UserRole(Role.Reviewer, DemoDbUtil.getDemoUser(DemoUsers.Kay_Jones)));
      roles.add(new UserRole(Role.Reviewer, DemoDbUtil.getDemoUser(DemoUsers.Alex_Kay), 2.0, true));
      Result result =
         PeerToPeerReviewWorkflowManager.transitionTo(reviewArt, PeerToPeerReviewState.Review, roles, null,
            UserManager.getUser(), false, transaction);
      if (result.isFalse()) {
         throw new IllegalStateException("Failed transitioning review to Review: " + result.getText());
      }
      reviewArt.persist(transaction);

      // Create a PeerToPeer review and transition to Completed
      reviewArt =
         ReviewManager.createNewPeerToPeerReview(secondCodeArt, "Review new logic",
            firstCodeArt.getStateMgr().getCurrentStateName(), DemoDbUtil.getDemoUser(DemoUsers.Kay_Jones), new Date(),
            transaction);
      roles = new ArrayList<UserRole>();
      roles.add(new UserRole(Role.Author, DemoDbUtil.getDemoUser(DemoUsers.Kay_Jones), 2.3, true));
      roles.add(new UserRole(Role.Reviewer, DemoDbUtil.getDemoUser(DemoUsers.Joe_Smith), 4.5, true));
      roles.add(new UserRole(Role.Reviewer, DemoDbUtil.getDemoUser(DemoUsers.Alex_Kay), 2.0, true));

      List<DefectItem> defects = new ArrayList<DefectItem>();
      defects.add(new DefectItem(DemoDbUtil.getDemoUser(DemoUsers.Alex_Kay), Severity.Issue, Disposition.Accept,
         InjectionActivity.Code, "Problem with logic", "Fixed", "Line 234", new Date()));
      defects.add(new DefectItem(DemoDbUtil.getDemoUser(DemoUsers.Alex_Kay), Severity.Issue, Disposition.Accept,
         InjectionActivity.Code, "Using getInteger instead", "Fixed", "MyWorld.java:Line 33", new Date()));
      defects.add(new DefectItem(DemoDbUtil.getDemoUser(DemoUsers.Alex_Kay), Severity.Major, Disposition.Reject,
         InjectionActivity.Code, "Spelling incorrect", "Is correct", "MyWorld.java:Line 234", new Date()));
      defects.add(new DefectItem(DemoDbUtil.getDemoUser(DemoUsers.Joe_Smith), Severity.Minor, Disposition.Reject,
         InjectionActivity.Code, "Remove unused code", "", "Here.java:Line 234", new Date()));
      defects.add(new DefectItem(DemoDbUtil.getDemoUser(DemoUsers.Joe_Smith), Severity.Major, Disposition.Accept,
         InjectionActivity.Code, "Negate logic", "Fixed", "There.java:Line 234", new Date()));
      result =
         PeerToPeerReviewWorkflowManager.transitionTo(reviewArt, PeerToPeerReviewState.Completed, roles, defects,
            UserManager.getUser(), false, transaction);
      reviewArt.persist(transaction);
      if (result.isFalse()) {
         throw new IllegalStateException("Failed transitioning review to Completed: " + result.getText());
      }
   }
}
