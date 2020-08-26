/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.ats.ide.editor.tab.workflow.widget;

import java.util.Collections;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.workdef.IStateToken;
import org.eclipse.osee.ats.core.util.HoursSpentUtil;
import org.eclipse.osee.ats.ide.editor.WorkflowEditor;
import org.eclipse.osee.ats.ide.editor.tab.workflow.util.WfePromptChangeStatus;
import org.eclipse.osee.ats.ide.internal.Activator;
import org.eclipse.osee.ats.ide.internal.AtsApiService;
import org.eclipse.osee.ats.ide.workdef.StateXWidgetPage;
import org.eclipse.osee.ats.ide.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.ide.workflow.review.AbstractReviewArtifact;
import org.eclipse.osee.ats.ide.workflow.review.ReviewManager;
import org.eclipse.osee.ats.ide.workflow.teamwf.TeamWorkFlowArtifact;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.widgets.XHyperlinkLabelValueSelection;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;

/**
 * @author Donald G. Dunne
 */
public class StateHoursSpentXWidget extends XHyperlinkLabelValueSelection {

   private final AbstractWorkflowArtifact sma;
   private final StateXWidgetPage page;
   private final boolean isCurrentState;
   private final WorkflowEditor editor;

   public StateHoursSpentXWidget(IManagedForm managedForm, StateXWidgetPage page, final AbstractWorkflowArtifact sma, Composite composite, int horizontalSpan, XModifiedListener xModListener, boolean isCurrentState, WorkflowEditor editor) {
      super("\"" + page.getName() + "\"" + " State Hours Spent");
      this.page = page;
      this.sma = sma;
      this.isCurrentState = isCurrentState;
      this.editor = editor;
      if (xModListener != null) {
         addXModifiedListener(xModListener);
      }
      setEditable(isCurrentState && !sma.isReadOnly());
      setFillHorizontally(true);
      setToolTip(TOOLTIP);
      super.createWidgets(managedForm, composite, horizontalSpan);
   }

   @Override
   public boolean handleSelection() {
      try {
         WfePromptChangeStatus.promptChangeStatus(Collections.singleton(sma), false);
         editor.onDirtied();
         return true;
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return false;
   }

   public final static String TOOLTIP = "Calculation: \n     State Hours Spent: amount entered by user\n" +
   //
      "     Task Hours Spent: total hours spent of all tasks related to state\n" +
      //
      "     Review Hours Spent: total hours spent of all reviews related to state\n" +
      //
      "Total State Hours Spent: state hours + all task hours + all review hours";

   @Override
   public String getCurrentValue() {
      if (page == null) {
         return "page == null";
      }
      try {
         StringBuffer sb = new StringBuffer(
            String.format("        State Hours: %5.2f", sma.getStateMgr().getHoursSpent(page.getName())));
         setEditable(isCurrentState && !sma.isReadOnly());
         boolean breakoutNeeded = false;
         if (sma instanceof TeamWorkFlowArtifact && AtsApiService.get().getTaskService().hasTasks(
            (TeamWorkFlowArtifact) sma)) {
            sb.append(String.format("\n        Task  Hours: %5.2f",
               HoursSpentUtil.getHoursSpentFromStateTasks(sma, page, AtsApiService.get())));
            breakoutNeeded = true;
         }
         if (sma.isTeamWorkflow() && AtsApiService.get().getReviewService().hasReviews((TeamWorkFlowArtifact) sma)) {
            sb.append(String.format("\n     Review Hours: %5.2f", getHoursSpent((TeamWorkFlowArtifact) sma, page)));
            breakoutNeeded = true;
         }
         if (breakoutNeeded) {
            setToolTip(sb.toString());
            return String.format("%5.2f",
               HoursSpentUtil.getHoursSpentStateTotal(sma, page, AtsApiService.get()));
         } else {
            return String.format("%5.2f", sma.getStateMgr().getHoursSpent(page.getName()));
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return ex.getLocalizedMessage();
      }
   }

   /**
    * Return Hours Spent for Reviews of "Related to State" stateName
    *
    * @param relatedToState state name of parent workflow's state
    */
   private double getHoursSpent(TeamWorkFlowArtifact teamArt, IStateToken relatedToState) {
      double spent = 0;
      for (AbstractReviewArtifact reviewArt : ReviewManager.getReviews(teamArt, relatedToState)) {
         spent += HoursSpentUtil.getHoursSpentTotal(reviewArt, AtsApiService.get());
      }
      return spent;
   }

}
