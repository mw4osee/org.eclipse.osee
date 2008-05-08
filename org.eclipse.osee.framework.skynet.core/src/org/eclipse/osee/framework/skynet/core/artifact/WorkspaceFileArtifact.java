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
package org.eclipse.osee.framework.skynet.core.artifact;

import static org.eclipse.osee.framework.skynet.core.artifact.search.Operator.EQUAL;
import java.io.File;
import java.sql.SQLException;
import java.util.Collection;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.framework.skynet.core.artifact.factory.IArtifactFactory;
import org.eclipse.osee.framework.skynet.core.artifact.search.AttributeValueSearch;
import org.eclipse.osee.framework.skynet.core.attribute.ArtifactSubtypeDescriptor;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.skynet.core.attribute.ConfigurationPersistenceManager;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Ryan D. Brooks
 */
public class WorkspaceFileArtifact extends Artifact {
   public static final String ARTIFACT_NAME = "Workspace File";
   private static final BranchPersistenceManager branchManager = BranchPersistenceManager.getInstance();

   /**
    * @param parentFactory
    * @param guid
    * @param branch
    * @throws SQLException
    */
   public WorkspaceFileArtifact(IArtifactFactory parentFactory, String guid, String humanReadableId, Branch branch, ArtifactSubtypeDescriptor artifactType) throws SQLException {
      super(parentFactory, guid, humanReadableId, branch, artifactType);
   }

   public static Artifact getArtifactFromWorkspaceFile(String location, Shell shell) throws IllegalStateException {
      Artifact artifact = null;
      int descriptorSelected = -1;
      ArtifactSubtypeDescriptor descriptor = null;
      ArtifactDescriptorDialog dialog = null;
      Collection<Artifact> artifacts;

      try {
         artifacts =
               artifactManager.getArtifacts(new AttributeValueSearch("Content URL", location, EQUAL),
                     branchManager.getDefaultBranch());
      } catch (SQLException ex) {
         throw new IllegalStateException("Sql exception: " + ex.getMessage());
      }

      if (artifacts.size() > 1)
         throw new IllegalStateException("More than one artifact available for this location: " + location);

      else if (artifacts.isEmpty()) {
         try {
            Collection<ArtifactSubtypeDescriptor> descriptors =
                  ConfigurationPersistenceManager.getInstance().getArtifactTypesFromAttributeType(
                        AttributeTypeManager.getType("Content URL"));
            dialog =
                  new ArtifactDescriptorDialog(
                        shell,
                        "Artifact Descriptor",
                        null,
                        "No Artifact could be found for this file. To create a new artifact please" + " select an artfact descriptor.",
                        MessageDialog.QUESTION, new String[] {"OK", "Cancel"}, 0, descriptors);
         } catch (SQLException ex) {
            ex.printStackTrace();
         }
         descriptorSelected = dialog.open();
         if (descriptorSelected == 0) {
            descriptor = dialog.getEntry();
            try {
               artifact = descriptor.makeNewArtifact(branchManager.getDefaultBranch());
               artifact.setSoleStringAttributeValue("Content URL", location);
               artifact.setSoleStringAttributeValue("Name", new File(location).getName());
               artifact.persistAttributes();
            } catch (SQLException ex) {
               ex.printStackTrace();
            }
         }
      } else {
         artifact = artifacts.iterator().next();
      }

      return artifact;
   }
}
