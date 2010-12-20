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
package org.eclipse.osee.framework.ui.skynet;

import java.util.logging.Level;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.relation.RelationLink;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;
import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;
import org.eclipse.swt.widgets.Item;

/**
 * @author Ryan D. Brooks
 */
public class RelationCellModifier implements ICellModifier {
   private final TreeViewer treeViewer;
   private final IDirtiableEditor editor;

   public RelationCellModifier(TreeViewer treeViewer, IDirtiableEditor editor) {
      this.treeViewer = treeViewer;
      this.editor = editor;
   }

   @Override
   public boolean canModify(Object element, String property) {
      if (element instanceof WrapperForRelationLink) {
         WrapperForRelationLink relLink = (WrapperForRelationLink) element;
         return !(relLink.getArtifactA().isReadOnly() || relLink.getArtifactB().isReadOnly());
      }
      return false;
   }

   @Override
   public Object getValue(Object element, String property) {
      WrapperForRelationLink relLink = (WrapperForRelationLink) element;
      RelationLink link = getRelationLink(relLink);
      return link != null ? link.getRationale() : Strings.emptyString();
   }

   @Override
   public void modify(Object element, String property, Object value) {
      // Note that it is possible for an SWT Item to be passed instead of the model element.
      if (element instanceof Item) {
         element = ((Item) element).getData();
      }
      WrapperForRelationLink relLink = (WrapperForRelationLink) element;
      RelationLink link = getRelationLink(relLink);
      if (link != null) {
         link.setRationale(value.toString());
      }
      treeViewer.update(element, null);
      editor.onDirtied();
   }

   private RelationLink getRelationLink(WrapperForRelationLink relLink) {
      try {
         return RelationManager.getRelationLink(relLink.getArtifactA(), relLink.getArtifactB(),
            relLink.getRelationType());
      } catch (OseeCoreException ex) {
         OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
      }
      return null;
   }
}
