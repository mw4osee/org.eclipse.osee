/*********************************************************************
 * Copyright (c) 2012 Robert Bosch Engineering and Business Solutions Ltd India
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Robert Bosch Engineering and Business Solutions Ltd India - initial API and implementation
 **********************************************************************/
package org.eclipse.osee.doors.connector.ui.handler;

import static org.eclipse.osee.framework.core.enums.RelationSorter.PREEXISTING;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.ui.PlatformUI;

/**
 * Class to create Doors Artifact in iCTeam
 *
 * @author Chandan Bandemutt
 */
public class DoorsCreator {

   /*
    * Collection of the display names which are merged into the branch.
    */
   private final Set<String> nodeDisplayNamesAdded = new HashSet<>();

   /*
    * Collection of the display names which cannot be merged into the branch.
    */
   private final Set<String> nodeDisplayNamesnotAdded = new HashSet<>();

   /**
    * @param url : Url of the module from DWA response
    * @param reqName : name of the artifact to be created
    * @param branch on which artifact is created
    */
   public void createCQRequirement(final String url, final String reqName, final BranchId branch) {

      Artifact newArtifact = null;

      try {
         SkynetTransaction trans = TransactionManager.createTransaction(branch, "Import Door Requirement");

         Artifact parentArtifact =
            ArtifactQuery.checkArtifactFromTypeAndName(CoreArtifactTypes.Folder, "Door Requirements", branch);
         if (parentArtifact == null) {
            // create the folder
            Artifact rootArtifact = ArtifactQuery.checkArtifactFromTypeAndName(CoreArtifactTypes.RootArtifact,
               "Default Hierarchy Root", branch);
            if (rootArtifact != null) {
               parentArtifact = rootArtifact.addNewChild(PREEXISTING, CoreArtifactTypes.Folder, "Door Requirements");
            }
         }

         if (parentArtifact != null) {

            Artifact artifact =
               ArtifactQuery.checkArtifactFromTypeAndName(DoorsOseeTypes.DoorsRequirement, reqName, branch);
            if (artifact == null) {
               newArtifact = parentArtifact.addNewChild(PREEXISTING, DoorsOseeTypes.DoorsRequirement, reqName);
               newArtifact.setSoleAttributeFromString(DoorsOseeTypes.DoorReqName, reqName);
               newArtifact.setSoleAttributeFromString(DoorsOseeTypes.DoorReqUrl, url);

               newArtifact.setName(reqName);
               newArtifact.persist(trans);
               trans.execute();
            } else {
               artifact.setSoleAttributeFromString(DoorsOseeTypes.DoorReqName, reqName);
               artifact.setSoleAttributeFromString(DoorsOseeTypes.DoorReqUrl, url);

               artifact.setName(reqName);
               artifact.persist(trans);
               trans.execute();
            }
         }
      } catch (OseeCoreException e) {
         MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Error", e.getMessage());
      }

   }

   /**
    * Get the List of Display Names merged in the Branch.
    *
    * @return NodeDisplayNames
    */
   public Set<String> getNodeDisplayNamesAdded() {
      return this.nodeDisplayNamesAdded;
   }

   /**
    * Get the List of Display Names not merged in the Branch.
    *
    * @return NodeDisplayNamesNotAdded
    */
   public Set<String> getNodeDisplayNamesNotAdded() {
      return this.nodeDisplayNamesnotAdded;
   }
}
