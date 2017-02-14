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

package org.eclipse.osee.framework.ui.skynet.render;

import static org.eclipse.osee.framework.core.enums.CoreAttributeTypes.ContentUrl;
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.GENERALIZED_EDIT;
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.GENERAL_REQUESTED;
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.PREVIEW;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.types.IArtifact;
import org.eclipse.osee.framework.ui.plugin.PluginUiImage;
import org.eclipse.osee.framework.ui.skynet.MenuCmdDef;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.program.Program;

/**
 * @author Ryan D. Brooks
 */
public class UrlRenderer extends DefaultArtifactRenderer {

   @Override
   public UrlRenderer newInstance() {
      return new UrlRenderer();
   }

   @Override
   public String getName() {
      return "UrlRenderer";
   }

   @Override
   public int getApplicabilityRating(PresentationType presentationType, IArtifact artifact, Object... objects) throws OseeCoreException {
      Artifact aArtifact = artifact.getFullArtifact();
      if (!presentationType.matches(GENERALIZED_EDIT,
         GENERAL_REQUESTED) && aArtifact.getAttributeCount(ContentUrl) > 0) {
         return ARTIFACT_TYPE_MATCH;
      }
      return NO_MATCH;
   }

   @Override
   public void addMenuCommandDefinitions(ArrayList<MenuCmdDef> commands, Artifact artifact) {
      if (artifact.getAttributeCount(ContentUrl) > 0) {
         commands.add(new MenuCmdDef(CommandGroup.PREVIEW, PREVIEW, "Web Browser", PluginUiImage.URL));
      }
   }

   @Override
   public void open(final List<Artifact> artifacts, PresentationType presentationType) throws OseeCoreException {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            try {
               for (Artifact artifact : artifacts) {
                  Program.launch(artifact.getSoleAttributeValueAsString(ContentUrl, getName()));
               }
            } catch (Exception ex) {
               OseeLog.log(UrlRenderer.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      });
   }
}