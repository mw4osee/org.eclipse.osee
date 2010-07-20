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
package org.eclipse.osee.coverage.merge;

import org.eclipse.osee.framework.core.exception.OseeArgumentException;

/**
 * @author Donald G. Dunne
 */
public abstract class MergeItemBase implements IMergeItem {

   private final MergeType mergeType;
   private boolean checked = false;
   private boolean isCheckable = true;
   private boolean importAllowed = true;

   public MergeItemBase(MergeType mergeType, boolean isCheckable) {
      this.mergeType = mergeType;
      this.isCheckable = isCheckable;
   }

   public MergeType getMergeType() {
      return mergeType;
   }

   public boolean isChecked() {
      return checked;
   }

   public void setChecked(boolean checked) throws OseeArgumentException {
      if (!isCheckable) {
         throw new OseeArgumentException(String.format("Merge Item is not checkable [%s]", this));
      }
      this.checked = checked;
   }

   public boolean isImportAllowed() {
      return importAllowed;
   }

   public void setImportAllowed(boolean importAllowed) {
      this.importAllowed = importAllowed;
   }

   public boolean isCheckable() {
      return isCheckable;
   }

}
