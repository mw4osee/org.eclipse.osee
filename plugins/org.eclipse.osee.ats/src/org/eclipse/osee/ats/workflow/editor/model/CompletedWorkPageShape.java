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
package org.eclipse.osee.ats.workflow.editor.model;

import java.util.logging.Level;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.workflow.page.AtsCompletedWorkPageDefinition;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkPageDefinition;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkPageType;

/**
 * @author Donald G. Dunne
 */
public class CompletedWorkPageShape extends WorkPageShape {

   public CompletedWorkPageShape() {
      super(new WorkPageDefinition("Completed", "NEW", AtsCompletedWorkPageDefinition.ID, WorkPageType.Completed));
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof WorkPageShape) {
         try {
            return ((WorkPageShape) obj).isCompletedState();
         } catch (OseeCoreException ex) {
            OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
         }
      }
      return super.equals(obj);
   }

   public CompletedWorkPageShape(WorkPageDefinition workPageDefinition) {
      super(workPageDefinition);
   }

}
