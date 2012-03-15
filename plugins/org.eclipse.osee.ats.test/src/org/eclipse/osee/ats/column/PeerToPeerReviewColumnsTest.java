/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.column;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.eclipse.osee.ats.core.client.AtsTestUtil;
import org.eclipse.osee.ats.core.client.review.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.core.client.review.PeerToPeerReviewManager;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectItem;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectItem.Disposition;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectItem.InjectionActivity;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectItem.Severity;
import org.eclipse.osee.ats.core.client.review.defect.ReviewDefectManager;
import org.eclipse.osee.ats.core.client.review.role.Role;
import org.eclipse.osee.ats.core.client.review.role.UserRole;
import org.eclipse.osee.ats.core.client.review.role.UserRoleManager;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.util.AtsUsersClient;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.DemoTestUtil;
import org.eclipse.osee.framework.logging.SevereLoggingMonitor;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.support.test.util.DemoUsers;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

/**
 * @tests CancelledDateColumn
 * @author Donald G. Dunne
 */
public class PeerToPeerReviewColumnsTest {

   @AfterClass
   @BeforeClass
   public static void cleanup() throws Exception {
      AtsTestUtil.cleanupSimpleTest(PeerToPeerReviewColumnsTest.class.getSimpleName());
   }

   @org.junit.Test
   public void testGetColumnText() throws Exception {
      SevereLoggingMonitor loggingMonitor = TestUtil.severeLoggingStart();

      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtil.getAtsBranch(), PeerToPeerReviewColumnsTest.class.getSimpleName());
      TeamWorkFlowArtifact teamArt =
         DemoTestUtil.createSimpleAction(PeerToPeerReviewColumnsTest.class.getSimpleName(), transaction);
      PeerToPeerReviewArtifact peerArt =
         PeerToPeerReviewManager.createNewPeerToPeerReview(teamArt, getClass().getSimpleName(),
            teamArt.getStateMgr().getCurrentStateName(), transaction);
      peerArt.persist(transaction);
      transaction.execute();

      Assert.assertEquals("0", ReviewNumIssuesColumn.getInstance().getColumnText(peerArt, null, 0));
      Assert.assertEquals("0", ReviewNumMajorDefectsColumn.getInstance().getColumnText(peerArt, null, 0));
      Assert.assertEquals("0", ReviewNumMinorDefectsColumn.getInstance().getColumnText(peerArt, null, 0));
      Assert.assertEquals("", ReviewAuthorColumn.getInstance().getColumnText(peerArt, null, 0));
      Assert.assertEquals("", ReviewModeratorColumn.getInstance().getColumnText(peerArt, null, 0));
      Assert.assertEquals("", ReviewReviewerColumn.getInstance().getColumnText(peerArt, null, 0));

      transaction =
         TransactionManager.createTransaction(AtsUtil.getAtsBranch(), PeerToPeerReviewColumnsTest.class.getSimpleName());
      ReviewDefectItem item =
         new ReviewDefectItem(AtsUsersClient.getUser(), Severity.Issue, Disposition.None, InjectionActivity.Code,
            "description", "resolution", "location", new Date());
      ReviewDefectManager defectManager = new ReviewDefectManager(peerArt);
      defectManager.addOrUpdateDefectItem(item);
      item =
         new ReviewDefectItem(AtsUsersClient.getUser(), Severity.Issue, Disposition.None, InjectionActivity.Code,
            "description 2", "resolution", "location", new Date());
      defectManager.addOrUpdateDefectItem(item);
      item =
         new ReviewDefectItem(AtsUsersClient.getUser(), Severity.Issue, Disposition.None, InjectionActivity.Code,
            "description 3", "resolution", "location", new Date());
      defectManager.addOrUpdateDefectItem(item);
      item =
         new ReviewDefectItem(AtsUsersClient.getUser(), Severity.Issue, Disposition.None, InjectionActivity.Code,
            "description 34", "resolution", "location", new Date());
      defectManager.addOrUpdateDefectItem(item);
      item =
         new ReviewDefectItem(AtsUsersClient.getUser(), Severity.Major, Disposition.None, InjectionActivity.Code,
            "description 4", "resolution", "location", new Date());
      defectManager.addOrUpdateDefectItem(item);
      item =
         new ReviewDefectItem(AtsUsersClient.getUser(), Severity.Minor, Disposition.None, InjectionActivity.Code,
            "description 5", "resolution", "location", new Date());
      defectManager.addOrUpdateDefectItem(item);
      item =
         new ReviewDefectItem(AtsUsersClient.getUser(), Severity.Minor, Disposition.None, InjectionActivity.Code,
            "description 6", "resolution", "location", new Date());
      defectManager.addOrUpdateDefectItem(item);
      item =
         new ReviewDefectItem(AtsUsersClient.getUser(), Severity.Minor, Disposition.None, InjectionActivity.Code,
            "description 6", "resolution", "location", new Date());
      defectManager.addOrUpdateDefectItem(item);
      defectManager.saveToArtifact(peerArt);

      UserRole role = new UserRole(Role.Author, AtsUsersClient.getUserFromToken(DemoUsers.Alex_Kay));
      UserRoleManager roleMgr = new UserRoleManager(peerArt);
      roleMgr.addOrUpdateUserRole(role);

      role = new UserRole(Role.Moderator, AtsUsersClient.getUserFromToken(DemoUsers.Jason_Michael));
      roleMgr.addOrUpdateUserRole(role);

      role = new UserRole(Role.Reviewer, AtsUsersClient.getUserFromToken(DemoUsers.Joe_Smith));
      roleMgr.addOrUpdateUserRole(role);
      role = new UserRole(Role.Reviewer, AtsUsersClient.getUserFromToken(DemoUsers.Kay_Jones));
      roleMgr.addOrUpdateUserRole(role);
      roleMgr.saveToArtifact(transaction);
      peerArt.persist(transaction);
      transaction.execute();

      Assert.assertEquals("4", ReviewNumIssuesColumn.getInstance().getColumnText(peerArt, null, 0));
      Assert.assertEquals("1", ReviewNumMajorDefectsColumn.getInstance().getColumnText(peerArt, null, 0));
      Assert.assertEquals("3", ReviewNumMinorDefectsColumn.getInstance().getColumnText(peerArt, null, 0));
      Assert.assertEquals(DemoUsers.Alex_Kay.getName(),
         ReviewAuthorColumn.getInstance().getColumnText(peerArt, null, 0));
      Assert.assertEquals(DemoUsers.Jason_Michael.getName(),
         ReviewModeratorColumn.getInstance().getColumnText(peerArt, null, 0));
      List<String> results =
         Arrays.asList(DemoUsers.Kay_Jones.getName() + "; " + DemoUsers.Joe_Smith.getName(),
            DemoUsers.Joe_Smith.getName() + "; " + DemoUsers.Kay_Jones.getName());
      Assert.assertTrue(results.contains(ReviewReviewerColumn.getInstance().getColumnText(peerArt, null, 0)));

      TestUtil.severeLoggingEnd(loggingMonitor);
   }
}
