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
package org.eclipse.osee.ats.operation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.ats.AtsPlugin;
import org.eclipse.osee.ats.artifact.TaskArtifact;
import org.eclipse.osee.ats.artifact.TaskableStateMachineArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.editor.SMAEditor;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.transaction.AbstractSkynetTxTemplate;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.Displays;
import org.eclipse.osee.framework.ui.skynet.blam.BlamVariableMap;
import org.eclipse.osee.framework.ui.skynet.blam.operation.AbstractBlam;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.osee.framework.ui.skynet.widgets.XListDropViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.DynamicXWidgetLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Donald G. Dunne
 */
public class ImportTasksFromSimpleList extends AbstractBlam {

   public static String ASSIGNEES = "Assignees";
   public static String TASK_IMPORT_TITLES = "Task Import Titles";
   public static String TEAM_WORKFLOW = "Team Workflow (drop here)";
   private TaskableStateMachineArtifact taskableStateMachineArtifact;

   public ImportTasksFromSimpleList() throws IOException {
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.blam.operation.BlamOperation#runOperation(org.eclipse.osee.framework.ui.skynet.blam.BlamVariableMap, org.eclipse.osee.framework.skynet.core.artifact.Branch, org.eclipse.core.runtime.IProgressMonitor)
    */
   public void runOperation(final BlamVariableMap variableMap, IProgressMonitor monitor) throws OseeCoreException {
      Displays.ensureInDisplayThread(new Runnable() {
         public void run() {
            try {
               List<Artifact> artifacts = variableMap.getArtifacts(TEAM_WORKFLOW);
               final List<Artifact> assignees = variableMap.getArtifacts(ASSIGNEES);
               final List<String> titles = new ArrayList<String>();
               for (String title : variableMap.getString(TASK_IMPORT_TITLES).split("\n")) {
                  title = title.replaceAll("\r", "");
                  if (!title.equals("")) titles.add(title);
               }

               if (artifacts.size() == 0) {
                  AWorkbench.popup("ERROR", "Must drag in Team Workflow to add tasks.");
                  return;
               }
               if (artifacts.size() > 1) {
                  AWorkbench.popup("ERROR", "Only drag ONE Team Workflow.");
                  return;
               }
               Artifact artifact = artifacts.iterator().next();
               if (!(artifact instanceof TeamWorkFlowArtifact)) {
                  AWorkbench.popup("ERROR", "Artifact MUST be Team Workflow");
                  return;
               }
               if (titles == null || titles.size() == 0) {
                  AWorkbench.popup("ERROR", "Must enter title(s).");
                  return;
               }
               try {
                  final TeamWorkFlowArtifact teamArt = (TeamWorkFlowArtifact) artifact;
                  AbstractSkynetTxTemplate txWrapper =
                        new AbstractSkynetTxTemplate(BranchManager.getAtsBranch()) {
                           @Override
                           protected void handleTxWork() throws OseeCoreException {
                              handleCreateTasks(assignees, titles, teamArt);
                              teamArt.persistAttributesAndRelations();
                           }
                        };
                  txWrapper.execute();
               } catch (Exception ex) {
                  OSEELog.logException(AtsPlugin.class, ex, true);
                  return;
               }

               SMAEditor.editArtifact(artifact);
            } catch (Exception ex) {
               OSEELog.logException(AtsPlugin.class, ex, true);
            }
         };
      });
   }

   private void handleCreateTasks(List<Artifact> assignees, List<String> titles, TeamWorkFlowArtifact teamArt) throws OseeCoreException {
      for (String title : titles) {
         TaskArtifact taskArt = teamArt.getSmaMgr().getTaskMgr().createNewTask(title, false);
         if (assignees != null && assignees.size() > 0) {
            Set<User> users = new HashSet<User>();
            for (Artifact art : assignees) {
               if (art instanceof User) {
                  users.add((User) art);
               }
            }
            taskArt.getSmaMgr().getStateMgr().setAssignees(users);
         }
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.blam.operation.AbstractBlam#widgetCreated(org.eclipse.osee.framework.ui.skynet.widgets.XWidget, org.eclipse.ui.forms.widgets.FormToolkit, org.eclipse.osee.framework.skynet.core.artifact.Artifact, org.eclipse.osee.framework.ui.skynet.widgets.workflow.DynamicXWidgetLayout, org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener, boolean)
    */
   @Override
   public void widgetCreated(XWidget xWidget, FormToolkit toolkit, Artifact art, DynamicXWidgetLayout dynamicXWidgetLayout, XModifiedListener modListener, boolean isEditable) throws OseeCoreException {
      super.widgetCreated(xWidget, toolkit, art, dynamicXWidgetLayout, modListener, isEditable);
      if (xWidget.getLabel().equals(TEAM_WORKFLOW) && taskableStateMachineArtifact != null) {
         XListDropViewer viewer = (XListDropViewer) xWidget;
         viewer.setInput(Arrays.asList(taskableStateMachineArtifact));
      }
   }

   /*
    * (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.blam.operation.BlamOperation#getXWidgetXml()
    */
   @Override
   public String getXWidgetsXml() {
      StringBuffer buffer = new StringBuffer("<xWidgets>");
      buffer.append("<XWidget xwidgetType=\"XListDropViewer\" displayName=\"" + TEAM_WORKFLOW + "\" />");
      buffer.append("<XWidget xwidgetType=\"XText\" fill=\"Vertically\" displayName=\"" + TASK_IMPORT_TITLES + "\" />");
      buffer.append("<XWidget xwidgetType=\"XMembersList\" displayName=\"" + ASSIGNEES + "\" />");
      buffer.append("</xWidgets>");
      return buffer.toString();
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.blam.operation.AbstractBlam#getDescriptionUsage()
    */
   @Override
   public String getDescriptionUsage() {
      return "Import tasks from spreadsheet into given Team Workflow";
   }

   /**
    * @return the TaskableStateMachineArtifact
    */
   public TaskableStateMachineArtifact getTaskableStateMachineArtifact() {
      return taskableStateMachineArtifact;
   }

   /**
    * @param defaultTeamWorkflowArtifact the defaultTeamWorkflowArtifact to set
    */
   public void setTaskableStateMachineArtifact(TaskableStateMachineArtifact taskableStateMachineArtifact) {
      this.taskableStateMachineArtifact = taskableStateMachineArtifact;
   }

}