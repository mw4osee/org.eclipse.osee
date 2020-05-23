/*********************************************************************
 * Copyright (c) 2010 Boeing
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

package org.eclipse.osee.framework.skynet.core.utility;

import org.eclipse.osee.framework.core.data.ApplicabilityId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.GammaId;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.TxCurrent;

/**
 * @author Ryan D. Brooks
 */
public final class Address implements Comparable<Address> {
   private final BranchId branch;
   private final int itemId;
   private final Long transactionId;
   private final GammaId gammaId;
   private ModificationType modType;
   private ApplicabilityId appId;
   private final TxCurrent txCurrent;
   private final boolean isBaseline;
   private TxCurrent correctedTxCurrent;
   private boolean purge;

   public Address(boolean isBaseline, BranchId branch, int itemId, Long transactionId, GammaId gammaId, ModificationType modType, ApplicabilityId appId, TxCurrent txCurrent) {
      this.branch = branch;
      this.itemId = itemId;
      this.transactionId = transactionId;
      this.gammaId = gammaId;
      this.modType = modType;
      this.appId = appId;
      this.txCurrent = txCurrent;
      this.isBaseline = isBaseline;
   }

   public boolean isBaselineTx() {
      return isBaseline;
   }

   public boolean isSimilar(Address other) {
      return other != null && other.itemId == itemId && other.branch.equals(branch);
   }

   public boolean isSameTransaction(Address other) {
      return other != null && transactionId.equals(other.transactionId);
   }

   public boolean hasSameGamma(Address other) {
      return other != null && gammaId == other.gammaId;
   }

   public boolean hasSameModType(Address other) {
      return modType == other.modType;
   }

   public boolean hasSameApplicability(Address other) {
      return appId.equals(other.appId);
   }

   public void ensureCorrectCurrent() {
      TxCurrent correctCurrent = TxCurrent.getCurrent(modType);
      if (txCurrent != correctCurrent) {
         correctedTxCurrent = correctCurrent;
      }
   }

   public void ensureNotCurrent() {
      if (txCurrent != TxCurrent.NOT_CURRENT) {
         correctedTxCurrent = TxCurrent.NOT_CURRENT;
      }
   }

   public boolean hasIssue() {
      return purge || correctedTxCurrent != null;
   }

   public TxCurrent getCorrectedTxCurrent() {
      return correctedTxCurrent;
   }

   public void setCorrectedTxCurrent(TxCurrent correctedTxCurrent) {
      this.correctedTxCurrent = correctedTxCurrent;
   }

   public boolean isPurge() {
      return purge;
   }

   public void setPurge(boolean purge) {
      this.purge = purge;
   }

   public BranchId getBranch() {
      return branch;
   }

   public int getItemId() {
      return itemId;
   }

   public Long getTransactionId() {
      return transactionId;
   }

   public GammaId getGammaId() {
      return gammaId;
   }

   public ModificationType getModType() {
      return modType;
   }

   public void setModType(ModificationType modType) {
      this.modType = modType;
   }

   public ApplicabilityId getApplicabilityId() {
      return appId;
   }

   public void setApplicabilityId(ApplicabilityId appId) {
      this.appId = appId;
   }

   public TxCurrent getTxCurrent() {
      return txCurrent;
   }

   public boolean isBaseline() {
      return isBaseline;
   }

   @Override
   public String toString() {
      return "Address [branchUuid=" + branch + ", gammaId=" + gammaId + ", itemId=" + itemId + ", modType=" + modType + ", transactionId=" + transactionId + ", txCurrent=" + txCurrent + "]";
   }

   @Override
   public int compareTo(Address otherAddress) {
      if (!transactionId.equals(otherAddress.transactionId)) {
         return Long.compare(transactionId, otherAddress.transactionId);
      } else {
         return Long.compare(gammaId.getId(), otherAddress.gammaId.getId());
      }
   }
}