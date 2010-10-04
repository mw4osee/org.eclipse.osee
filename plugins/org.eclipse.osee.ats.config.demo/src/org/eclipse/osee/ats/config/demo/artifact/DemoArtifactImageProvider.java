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
package org.eclipse.osee.ats.config.demo.artifact;

import org.eclipse.osee.ats.config.demo.util.DemoImage;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageManager;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageProvider;
import org.eclipse.osee.support.test.util.DemoArtifactTypes;

/**
 * @author Donald G. Dunne
 */
public class DemoArtifactImageProvider extends ArtifactImageProvider {

   @Override
   public void init() throws OseeCoreException {
      ArtifactImageManager.registerBaseImage(DemoArtifactTypes.DemoCodeTeamWorkflow, DemoImage.DEMO_WORKFLOW, this);
      ArtifactImageManager.registerBaseImage(DemoArtifactTypes.DemoReqTeamWorkflow, DemoImage.DEMO_WORKFLOW, this);
      ArtifactImageManager.registerBaseImage(DemoArtifactTypes.DemoTestTeamWorkflow, DemoImage.DEMO_WORKFLOW, this);
   }
}