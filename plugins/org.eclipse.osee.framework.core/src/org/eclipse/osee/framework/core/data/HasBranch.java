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
package org.eclipse.osee.framework.core.data;

/**
 * @author Roberto E. Escobar
 */
public interface HasBranch {

   default IOseeBranch getBranch() {
      return TokenFactory.createBranch(getBranchId(), null);
   }

   default Long getBranchId() {
      return getBranch().getUuid();
   }
   
   default boolean isOnBranch(Long branchId) {
      return getBranchId().equals(branchId);
   }

   default boolean isOnSameBranch(HasBranch other) {
      return other == null ? false : getBranchId().equals(other.getBranchId());
   }

   default boolean isOnBranch(IOseeBranch branch) {
      return getBranchId().equals(branch.getUuid());
   }
}