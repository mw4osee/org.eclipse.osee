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
package org.eclipse.osee.ats.util.widgets;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.ats.artifact.AbstractReviewArtifact;
import org.eclipse.osee.ats.artifact.AbstractReviewArtifact.ReviewBlockType;
import org.eclipse.osee.ats.artifact.AtsAttributeTypes;
import org.eclipse.osee.ats.artifact.DecisionReviewArtifact;
import org.eclipse.osee.ats.artifact.DecisionReviewState;
import org.eclipse.osee.ats.artifact.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.ats.util.AtsRelationTypes;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.TeamState;
import org.eclipse.osee.ats.util.TransitionOption;
import org.eclipse.osee.ats.workflow.TransitionManager;
import org.eclipse.osee.ats.workflow.item.AtsWorkDefinitions;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.utility.UsersByIds;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.IWorkPage;

/**
 * @author Donald G. Dunne
 */
public class ReviewManager {

   private final static String VALIDATE_REVIEW_TITLE = "Is the resolution of this Action valid?";

   public ReviewManager() {
      super();
   }

   /**
    * Create a new decision review configured and transitioned to handle action validation
    * 
    * @param force will force the creation of the review without checking that a review should be created
    * @param createdDate TODO
    * @param createdBy TODO
    */
   public static DecisionReviewArtifact createValidateReview(TeamWorkFlowArtifact teamArt, boolean force, Date createdDate, User createdBy, SkynetTransaction transaction) throws OseeCoreException {
      // If not validate page, don't do anything
      if (!force && !AtsWorkDefinitions.isValidatePage(teamArt.getWorkPageDefinition())) {
         return null;
      }
      // If validate review already created for this state, return
      if (!force && getReviewsFromCurrentState(teamArt).size() > 0) {
         for (AbstractReviewArtifact rev : getReviewsFromCurrentState(teamArt)) {
            if (rev.getName().equals(VALIDATE_REVIEW_TITLE)) {
               return null;
            }
         }
      }
      // Create validate review
      try {

         DecisionReviewArtifact decRev =
            ReviewManager.createNewDecisionReview(
               teamArt,
               AtsWorkDefinitions.isValidateReviewBlocking(teamArt.getWorkPageDefinition()) ? ReviewBlockType.Transition : ReviewBlockType.None,
               true, createdDate, createdBy);
         decRev.setName(VALIDATE_REVIEW_TITLE);
         decRev.setSoleAttributeValue(AtsAttributeTypes.DecisionReviewOptions,
            "No;Followup;" + getValidateReviewFollowupUsersStr(teamArt) + "\n" + "Yes;Completed;");

         TransitionManager transitionMgr = new TransitionManager(decRev);
         transitionMgr.transition(DecisionReviewState.Decision, teamArt.getCreatedBy(), transaction,
            TransitionOption.Persist);

         return decRev;

      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return null;
   }

   public static DecisionReviewArtifact createNewDecisionReview(TeamWorkFlowArtifact teamArt, String reviewTitle, String description, String againstState, ReviewBlockType reviewBlockType, String options, Collection<User> assignees, Date createdDate, User createdBy, SkynetTransaction transaction) throws OseeCoreException {
      DecisionReviewArtifact decRev =
         ReviewManager.createNewDecisionReview(teamArt, reviewBlockType, reviewTitle, againstState, description,
            options, assignees, createdDate, createdBy);
      return decRev;
   }

   public static DecisionReviewArtifact createNewDecisionReviewAndTransitionToDecision(TeamWorkFlowArtifact teamArt, String reviewTitle, String description, String againstState, ReviewBlockType reviewBlockType, String options, Collection<User> assignees, Date createdDate, User createdBy, SkynetTransaction transaction) throws OseeCoreException {
      DecisionReviewArtifact decRev =
         ReviewManager.createNewDecisionReview(teamArt, reviewBlockType, reviewTitle, againstState, description,
            options, assignees, createdDate, createdBy);
      decRev.persist(transaction);

      TransitionManager transitionMgr = new TransitionManager(decRev);
      transitionMgr.transition(DecisionReviewState.Decision, assignees, transaction, TransitionOption.Persist,
         TransitionOption.OverrideAssigneeCheck);
      return decRev;
   }

   public static PeerToPeerReviewArtifact createNewPeerToPeerReview(TeamWorkFlowArtifact teamArt, String reviewTitle, String againstState, SkynetTransaction transaction) throws OseeCoreException {
      return createNewPeerToPeerReview(teamArt, reviewTitle, againstState, new Date(), UserManager.getUser(),
         transaction);
   }

   public static PeerToPeerReviewArtifact createNewPeerToPeerReview(TeamWorkFlowArtifact teamArt, String reviewTitle, String againstState, Date createdDate, User createdBy, SkynetTransaction transaction) throws OseeCoreException {
      PeerToPeerReviewArtifact peerToPeerRev =
         (PeerToPeerReviewArtifact) ArtifactTypeManager.addArtifact(AtsArtifactTypes.PeerToPeerReview,
            AtsUtil.getAtsBranch(), reviewTitle == null ? "Peer to Peer Review" : reviewTitle);
      // Initialize state machine
      peerToPeerRev.initializeNewStateMachine(DecisionReviewState.Prepare, null, new Date(), createdBy);

      if (teamArt != null) {
         teamArt.addRelation(AtsRelationTypes.TeamWorkflowToReview_Review, peerToPeerRev);
         if (againstState != null) {
            peerToPeerRev.setSoleAttributeValue(AtsAttributeTypes.RelatedToState, againstState);
         }
      }
      peerToPeerRev.setSoleAttributeValue(AtsAttributeTypes.ReviewBlocks, ReviewBlockType.None.name());
      peerToPeerRev.persist(transaction);
      return peerToPeerRev;
   }

   /**
    * Return Remain Hours for all reviews
    * 
    * @return remain hours
    */
   public static double getRemainHours(TeamWorkFlowArtifact teamArt) throws OseeCoreException {
      double hours = 0;
      for (AbstractReviewArtifact reviewArt : getReviews(teamArt)) {
         hours += reviewArt.getRemainHoursFromArtifact();
      }
      return hours;

   }

   /**
    * Return Estimated Review Hours of "Related to State" stateName
    * 
    * @param relatedToState state name of parent workflow's state
    * @return Returns the Estimated Hours
    */
   public static double getEstimatedHours(TeamWorkFlowArtifact teamArt, IWorkPage relatedToState) throws OseeCoreException {
      double hours = 0;
      for (AbstractReviewArtifact revArt : getReviews(teamArt, relatedToState)) {
         hours += revArt.getEstimatedHoursTotal();
      }
      return hours;
   }

   /**
    * Return Estimated Hours for all reviews
    * 
    * @return estimated hours
    */
   public static double getEstimatedHours(TeamWorkFlowArtifact teamArt) throws OseeCoreException {
      double hours = 0;
      for (AbstractReviewArtifact revArt : getReviews(teamArt)) {
         hours += revArt.getEstimatedHoursTotal();
      }
      return hours;

   }

   public static String getValidateReviewFollowupUsersStr(TeamWorkFlowArtifact teamArt) {
      try {
         return UsersByIds.getStorageString(getValidateReviewFollowupUsers(teamArt));
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
         return ex.getLocalizedMessage();
      }
   }

   public static Collection<User> getValidateReviewFollowupUsers(TeamWorkFlowArtifact teamArt) throws OseeCoreException {
      Collection<User> users = teamArt.getStateMgr().getAssignees(TeamState.Implement);
      if (users.size() > 0) {
         return users;
      }

      // Else if Team Workflow , return it to the leads of this team
      return teamArt.getTeamDefinition().getLeads();

   }

   public static Collection<AbstractReviewArtifact> getReviews(TeamWorkFlowArtifact teamArt) throws OseeCoreException {
      return teamArt.getRelatedArtifacts(AtsRelationTypes.TeamWorkflowToReview_Review, AbstractReviewArtifact.class);
   }

   public static Collection<AbstractReviewArtifact> getReviewsFromCurrentState(TeamWorkFlowArtifact teamArt) throws OseeCoreException {
      return getReviews(teamArt, teamArt.getStateMgr().getCurrentState());
   }

   public static Collection<AbstractReviewArtifact> getReviews(TeamWorkFlowArtifact teamArt, IWorkPage state) throws OseeCoreException {
      Set<AbstractReviewArtifact> arts = new HashSet<AbstractReviewArtifact>();
      for (AbstractReviewArtifact revArt : getReviews(teamArt)) {
         if (revArt.getSoleAttributeValue(AtsAttributeTypes.RelatedToState, "").equals(state.getPageName())) {
            arts.add(revArt);
         }
      }
      return arts;
   }

   public static boolean hasReviews(TeamWorkFlowArtifact teamArt) {
      return teamArt.getRelatedArtifactsCount(AtsRelationTypes.TeamWorkflowToReview_Review) > 0;
   }

   public static Result areReviewsComplete(TeamWorkFlowArtifact teamArt) {
      return areReviewsComplete(teamArt, true);
   }

   public static Result areReviewsComplete(TeamWorkFlowArtifact teamArt, boolean popup) {
      try {
         for (AbstractReviewArtifact reviewArt : getReviews(teamArt)) {
            if (!reviewArt.isCompleted() && reviewArt.isCancelled()) {
               return new Result("Not Complete");
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
      return Result.TrueResult;
   }

   /**
    * Return Hours Spent for Reviews of "Related to State" stateName
    * 
    * @param relatedToState state name of parent workflow's state
    * @return Returns the Hours Spent
    */
   public static double getHoursSpent(TeamWorkFlowArtifact teamArt, IWorkPage relatedToState) throws OseeCoreException {
      double spent = 0;
      for (AbstractReviewArtifact reviewArt : getReviews(teamArt, relatedToState)) {
         spent += reviewArt.getHoursSpentSMATotal();
      }
      return spent;
   }

   /**
    * Return Total Percent Complete / # Reviews for "Related to State" stateName
    * 
    * @param relatedToState state name of parent workflow's state
    * @return Returns the Percent Complete.
    */
   public static int getPercentComplete(TeamWorkFlowArtifact teamArt, IWorkPage relatedToState) throws OseeCoreException {
      int spent = 0;
      Collection<AbstractReviewArtifact> reviewArts = getReviews(teamArt, relatedToState);
      for (AbstractReviewArtifact reviewArt : reviewArts) {
         spent += reviewArt.getPercentCompleteSMATotal();
      }
      if (spent == 0) {
         return 0;
      }
      return spent / reviewArts.size();
   }

   public static DecisionReviewArtifact createNewDecisionReview(TeamWorkFlowArtifact teamArt, ReviewBlockType reviewBlockType, boolean againstCurrentState, Date createdDate, User createdBy) throws OseeCoreException {
      return createNewDecisionReview(teamArt, reviewBlockType,
         "Should we do this?  Yes will require followup, No will not",
         againstCurrentState ? teamArt.getStateMgr().getCurrentStateName() : null,
         "Enter description of the decision, if any", getDefaultDecisionReviewOptions(), null, createdDate, createdBy);
   }

   public static String getDefaultDecisionReviewOptions() throws OseeCoreException {
      return "Yes;Followup;<" + UserManager.getUser().getUserId() + ">\n" + "No;Completed;";
   }

   public static DecisionReviewArtifact createNewDecisionReview(TeamWorkFlowArtifact teamArt, ReviewBlockType reviewBlockType, String title, String relatedToState, String description, String options, Collection<User> assignees, Date createdDate, User createdBy) throws OseeCoreException {
      DecisionReviewArtifact decRev =
         (DecisionReviewArtifact) ArtifactTypeManager.addArtifact(AtsArtifactTypes.DecisionReview,
            AtsUtil.getAtsBranch(), title);

      // Initialize state machine
      decRev.initializeNewStateMachine(DecisionReviewState.Prepare, assignees, createdDate, createdBy);

      if (teamArt != null) {
         teamArt.addRelation(AtsRelationTypes.TeamWorkflowToReview_Review, decRev);
      }
      if (Strings.isValid(relatedToState)) {
         decRev.setSoleAttributeValue(AtsAttributeTypes.RelatedToState, relatedToState);
      }
      if (Strings.isValid(description)) {
         decRev.setSoleAttributeValue(AtsAttributeTypes.Description, description);
      }
      if (Strings.isValid(options)) {
         decRev.setSoleAttributeValue(AtsAttributeTypes.DecisionReviewOptions, options);
      }
      if (reviewBlockType != null) {
         decRev.setSoleAttributeFromString(AtsAttributeTypes.ReviewBlocks, reviewBlockType.name());
      }

      return decRev;
   }

}
