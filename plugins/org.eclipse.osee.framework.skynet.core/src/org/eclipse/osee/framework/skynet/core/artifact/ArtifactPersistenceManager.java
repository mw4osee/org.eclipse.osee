/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.framework.skynet.core.artifact;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.RelationTypeToken;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.jdk.core.result.XResultData;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.internal.ServiceUtil;
import org.eclipse.osee.framework.skynet.core.relation.RelationLink;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Ryan D. Brooks
 * @author Robert A. Fisher
 */
public class ArtifactPersistenceManager {
   /**
    * @param transaction if the transaction is null then persist is not called
    * @param overrideDeleteCheck if <b>true</b> deletes without checking preconditions
    * @param artifacts The artifacts to delete.
    */
   public static XResultData deleteArtifact(SkynetTransaction transaction, boolean overrideDeleteCheck, XResultData rd, final Artifact... artifacts) {
      return deleteArtifactCollection(transaction, overrideDeleteCheck, rd, Arrays.asList(artifacts));
   }

   public static XResultData deleteArtifactCollection(SkynetTransaction transaction, boolean overrideDeleteCheck, XResultData rd, final Collection<Artifact> artifacts) {
      if (rd == null) {
         rd = new XResultData();
      }
      if (artifacts.isEmpty()) {
         return rd;
      }

      bulkLoadRelatives(artifacts);

      if (!overrideDeleteCheck) {
         performDeleteChecks(artifacts, rd);
      }

      boolean reorderRelations = true;
      for (Artifact artifact : artifacts) {
         deleteTrace(artifact, transaction, reorderRelations, rd);
      }
      return rd;
   }

   // Confirm artifacts are fit to delete
   private static XResultData performDeleteChecks(Collection<Artifact> artifacts, XResultData rd) {
      return ServiceUtil.getOseeClient().getAccessControlService().isDeleteable(artifacts, rd);
   }

   // Confirm relations are fit to delete
   public static XResultData performDeleteRelationChecks(Artifact artifact, RelationTypeToken relationType, XResultData rd) {
      return ServiceUtil.getOseeClient().getAccessControlService().isDeleteableRelation(artifact, relationType, rd);
   }

   private static void bulkLoadRelatives(Collection<Artifact> artifacts) {
      Collection<ArtifactId> relatives = new HashSet<>();
      for (Artifact artifact : artifacts) {
         for (RelationLink link : artifact.getRelationsAll(DeletionFlag.EXCLUDE_DELETED)) {
            relatives.add(link.getArtifactIdA());
            relatives.add(link.getArtifactIdB());
         }
      }
      BranchId branch = artifacts.iterator().next().getBranch();
      ArtifactQuery.getArtifactListFrom(relatives, branch);
   }

   private static XResultData deleteTrace(Artifact artifact, SkynetTransaction transaction, boolean reorderRelations, XResultData rd) {
      if (!artifact.isDeleted()) {
         // This must be done first since the the actual deletion of an
         // artifact clears out the link manager
         for (Artifact childArtifact : artifact.getChildren()) {
            deleteTrace(childArtifact, transaction, false, rd);
         }
         try {
            // calling deCache here creates a race condition when the handleRelationModifiedEvent listeners fire - RS
            //          ArtifactCache.deCache(artifact);
            artifact.internalSetDeleted();
            RelationManager.deleteRelationsAll(artifact, reorderRelations, transaction, rd);
            if (transaction != null) {
               artifact.persist(transaction);
            }
         } catch (OseeCoreException ex) {
            artifact.resetToPreviousModType();
            throw ex;
         }
      }
      return rd;
   }
}