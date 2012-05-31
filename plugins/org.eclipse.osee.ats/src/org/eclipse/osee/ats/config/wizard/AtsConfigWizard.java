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
package org.eclipse.osee.ats.config.wizard;

import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osee.ats.AtsOpenOption;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.config.AtsConfigManager;
import org.eclipse.osee.ats.config.AtsConfigManager.Display;
import org.eclipse.osee.ats.core.client.config.ActionableItemArtifact;
import org.eclipse.osee.ats.core.client.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.core.workdef.WorkDefinition;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.progress.UIJob;

/**
 * Create new new .shape-file. Those files can be used with the ShapesEditor (see plugin.xml).
 * 
 * @author Donald G. Dunne
 */
public class AtsConfigWizard extends Wizard implements INewWizard {

   private AtsConfigWizardPage1 page1;

   @Override
   public void addPages() {
      addPage(page1);
   }

   @Override
   public void init(IWorkbench workbench, IStructuredSelection selection) {
      page1 = new AtsConfigWizardPage1();
   }

   @Override
   public boolean performFinish() {
      try {
         String teamDefName = page1.getTeamDefName();
         Collection<String> aias = page1.getActionableItems();
         Collection<String> versionNames = page1.getVersions();
         String workDefName = page1.getWorkDefinitionName();

         AtsConfigManager.Display display = new OpenAtsConfigEditors();
         IOperation operation = new AtsConfigManager(display, workDefName, teamDefName, versionNames, aias);
         Operations.executeAsJob(operation, true);

      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         return false;
      }
      return true;
   }

   private static final class OpenAtsConfigEditors implements Display {

      @Override
      public void openAtsConfigurationEditors(final TeamDefinitionArtifact teamDef, final Collection<ActionableItemArtifact> aias, final WorkDefinition workDefinition) {
         Job job = new UIJob("Open Ats Configuration Editors") {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
               AtsUtil.openATSAction(teamDef, AtsOpenOption.OpenAll);
               for (ActionableItemArtifact aia : aias) {
                  AtsUtil.openATSAction(aia, AtsOpenOption.OpenAll);
               }
               try {
                  RendererManager.open(ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.WorkDefinition,
                     workDefinition.getName(), AtsUtil.getAtsBranch()), PresentationType.SPECIALIZED_EDIT, monitor);
               } catch (OseeCoreException ex) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               }
               return Status.OK_STATUS;
            }
         };
         Jobs.startJob(job, true);
      }
   }
}
