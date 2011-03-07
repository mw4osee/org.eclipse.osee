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
package org.eclipse.osee.ats.editor.widget;

import java.util.Collections;
import java.util.logging.Level;
import org.eclipse.osee.ats.artifact.AbstractTaskableArtifact;
import org.eclipse.osee.ats.artifact.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.column.HoursSpentStateTasksColumn;
import org.eclipse.osee.ats.column.HoursSpentStateTotalColumn;
import org.eclipse.osee.ats.editor.SMAEditor;
import org.eclipse.osee.ats.editor.SMAPromptChangeStatus;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.widgets.ReviewManager;
import org.eclipse.osee.ats.workdef.StateXWidgetPage;
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
   private final SMAEditor editor;

   public StateHoursSpentXWidget(IManagedForm managedForm, StateXWidgetPage page, final AbstractWorkflowArtifact sma, Composite composite, int horizontalSpan, XModifiedListener xModListener, boolean isCurrentState, SMAEditor editor) {
      super("\"" + page.getPageName() + "\"" + " State Hours Spent");
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
         SMAPromptChangeStatus.promptChangeStatus(Collections.singleton(sma), false);
         editor.onDirtied();
         return true;
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
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
         StringBuffer sb =
            new StringBuffer(String.format("        State Hours: %5.2f", sma.getStateMgr().getHoursSpent(page)));
         setEditable(isCurrentState && !sma.isReadOnly());
         boolean breakoutNeeded = false;
         if (sma instanceof AbstractTaskableArtifact && ((AbstractTaskableArtifact) sma).hasTaskArtifacts()) {
            sb.append(String.format("\n        Task  Hours: %5.2f",
               HoursSpentStateTasksColumn.getHoursSpentFromStateTasks(sma, page)));
            breakoutNeeded = true;
         }
         if (sma.isTeamWorkflow() && ReviewManager.hasReviews((TeamWorkFlowArtifact) sma)) {
            sb.append(String.format("\n     Review Hours: %5.2f",
               ReviewManager.getHoursSpent((TeamWorkFlowArtifact) sma, page)));
            breakoutNeeded = true;
         }
         if (breakoutNeeded) {
            setToolTip(sb.toString());
            return String.format("%5.2f", HoursSpentStateTotalColumn.getHoursSpentStateTotal(sma, page));
         } else {
            return String.format("%5.2f", sma.getStateMgr().getHoursSpent(page));
         }
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
         return ex.getLocalizedMessage();
      }
   }
}
