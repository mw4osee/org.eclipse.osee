/*********************************************************************
 * Copyright (c) 2021 Boeing
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
package org.eclipse.osee.mim.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.concurrent.ThreadLocalRandom;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Luciano T. Vaglienti
 */
public class NodeViewData extends PLGenericDBObject {

   private String bgColor = generateColor() ? "#81d4fa" : "#c5e1a5"; //has to be called bgColor due to @swimlane/ngx-graph having weird handling behavior of node.data.color

   public NodeViewData(ArtifactToken art) {
      this((ArtifactReadable) art);
   }

   public NodeViewData(ArtifactReadable art) {
      super(art);
   }

   public NodeViewData(Long id, String name) {
      super(id, name);
   }

   public NodeViewData() {
   }

   /**
    * @return the color
    */
   public String getbgColor() {
      return bgColor;
   }

   /**
    * @param color the color to set
    */
   public void setbgColor(String color) {
      this.bgColor = color;
   }

   @JsonIgnore
   private boolean generateColor() {
      return ThreadLocalRandom.current().nextInt(1, 3) > 1 ? true : false;
   }

}
