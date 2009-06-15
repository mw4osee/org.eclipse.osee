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
package org.eclipse.osee.framework.ui.skynet.test.cases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactType;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.ImageManager;
import org.eclipse.osee.framework.ui.skynet.OseeImage;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;

/**
 * @author Donald G. Dunne
 */
public abstract class ImageManagerTest {

   private final OseeImage[] oseeImages;
   private final String imageClassName;

   public ImageManagerTest(String imageClassName, OseeImage[] oseeImages) {
      this.imageClassName = imageClassName;
      this.oseeImages = oseeImages;
   }

   @org.junit.Test
   public void testFrameworkImageMissing() throws Exception {
      assertEquals(ImageManager.getImage(MissingImage.ACCEPT), ImageManager.getImage(FrameworkImage.MISSING));
   }

   @org.junit.Test
   public void testFrameworkImageEnums() throws Exception {
      StringBuffer sb = new StringBuffer();
      for (OseeImage oseeImage : oseeImages) {
         if (oseeImage == FrameworkImage.MISSING) continue;
         assertNotNull(String.format("[%s] Image not defined for [%s]", imageClassName, oseeImage),
               ImageManager.getImage(oseeImage));
         if (ImageManager.getImage(oseeImage).equals(ImageManager.getImage(FrameworkImage.MISSING))) {
            sb.append(String.format("\n[%s] Image not defined for [%s]", imageClassName, oseeImage));
         }
      }
      assertEquals("", sb.toString());
   }

   @org.junit.Test
   public void testArtifactImage() throws Exception {
      for (ArtifactType artifactType : ArtifactTypeManager.getAllTypes()) {
         assertNotNull(String.format("[%s] Image not defined for artifactType [%s]", imageClassName, artifactType),
               ImageManager.getImage(artifactType));
      }
   }

   public enum MissingImage implements OseeImage {
      ACCEPT("nothere.gif");

      private final String fileName;

      private MissingImage(String fileName) {
         this.fileName = fileName;
      }

      /* (non-Javadoc)
       * @see org.eclipse.osee.framework.ui.skynet.OseeImage#getImageDescriptor()
       */
      @Override
      public ImageDescriptor createImageDescriptor() {
         return ImageManager.createImageDescriptor(SkynetGuiPlugin.PLUGIN_ID, "images", fileName);
      }

      /* (non-Javadoc)
       * @see org.eclipse.osee.framework.ui.skynet.OseeImage#getImageKey()
       */
      @Override
      public String getImageKey() {
         return SkynetGuiPlugin.PLUGIN_ID + "." + fileName;
      }
   }

}
