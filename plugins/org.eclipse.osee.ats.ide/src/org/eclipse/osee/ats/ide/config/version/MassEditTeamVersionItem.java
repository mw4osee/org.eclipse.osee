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

package org.eclipse.osee.ats.ide.config.version;

import java.util.List;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.ide.internal.Activator;
import org.eclipse.osee.ats.ide.internal.AtsClientService;
import org.eclipse.osee.ats.ide.util.widgets.dialog.TeamDefinitionDialog;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;
import org.eclipse.osee.framework.ui.skynet.artifact.massEditor.MassArtifactEditor;
import org.eclipse.osee.framework.ui.swt.KeyedImage;

/**
 * @author Donald G. Dunne
 */
public class MassEditTeamVersionItem extends XNavigateItemAction {

   private IAtsTeamDefinition selectedTeamDef;

   public MassEditTeamVersionItem(String name, XNavigateItem parent, KeyedImage oseeImage) {
      super(parent, name, oseeImage);
   }

   private IAtsTeamDefinition getTeamDefinition() {
      if (selectedTeamDef != null) {
         return selectedTeamDef;
      }
      TeamDefinitionDialog dialog = new TeamDefinitionDialog();
      dialog.setInput(AtsClientService.get().getTeamDefinitionService().getTeamReleaseableDefinitions(Active.Active));
      int result = dialog.open();
      if (result == 0) {
         return dialog.getSelectedFirst();
      }
      return null;
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) {
      try {
         selectedTeamDef = getTeamDefinition();
         if (selectedTeamDef == null) {
            return;
         }
         if (AtsClientService.get().getTeamDefinitionService().getTeamDefHoldingVersions(selectedTeamDef) == null) {
            AWorkbench.popup("ERROR", "Team is not configured to use versions.");
            return;
         }
         MassArtifactEditor.editArtifacts(getName(), getResults());
         selectedTeamDef = null;
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   public List<Artifact> getResults() {
      return AtsClientService.get().getConfigArtifacts(
         AtsClientService.get().getVersionService().getVersionsFromTeamDefHoldingVersions(selectedTeamDef));
   }

   /**
    * @param selectedTeamDef the selectedTeamDef to set
    */
   public void setSelectedTeamDef(IAtsTeamDefinition selectedTeamDef) {
      this.selectedTeamDef = selectedTeamDef;
   }

}
