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
package org.eclipse.osee.framework.skynet.core.artifact;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.relation.RelationLink;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Ryan D. Brooks
 * @author Robert A. Fisher
 */
public class ArtifactPersistenceManager {
   private static final String ARTIFACT_NEW_ON_BRANCH =
      "SELECT count(1) FROM osee_artifact art, osee_txs txs WHERE art.art_id = ? and art.gamma_id = txs.gamma_id and txs.branch_id = ? and txs.transaction_id = ?";

   private static final String RELATION_NEW_ON_BRANCH =
      "SELECT count(1) FROM osee_relation_link rel, osee_txs txs WHERE rel.a_art_id = ? and rel.b_art_id = ? and rel.rel_link_type_id = ? and rel.gamma_id = txs.gamma_id and txs.branch_id = ? and txs.transaction_id = ?";

   /**
    * @param transaction if the transaction is null then persist is not called
    * @param overrideDeleteCheck if <b>true</b> deletes without checking preconditions
    * @param artifacts The artifacts to delete.
    */
   public static void deleteArtifact(SkynetTransaction transaction, boolean overrideDeleteCheck, final Artifact... artifacts) throws OseeCoreException {
      deleteArtifactCollection(transaction, overrideDeleteCheck, Arrays.asList(artifacts));
   }

   public static void deleteArtifactCollection(SkynetTransaction transaction, boolean overrideDeleteCheck, final Collection<Artifact> artifacts) throws OseeCoreException {
      if (artifacts.isEmpty()) {
         return;
      }

      if (!overrideDeleteCheck) {
         performDeleteChecks(artifacts);
      }

      bulkLoadRelatives(artifacts);

      boolean reorderRelations = true;
      for (Artifact artifact : artifacts) {
         deleteTrace(artifact, transaction, reorderRelations);
      }
   }

   private static void performDeleteChecks(Collection<Artifact> artifacts) throws OseeCoreException {
      // Confirm artifacts are fit to delete
      for (IArtifactCheck check : ArtifactChecks.getArtifactChecks()) {
         IStatus result = check.isDeleteable(artifacts);
         if (!result.isOK()) {
            throw new OseeStateException(result.getMessage());
         }
      }
   }

   public static void performDeleteRelationChecks(Artifact artifact, IRelationType relationType) throws OseeCoreException {
      // Confirm relations are fit to delete
      for (IArtifactCheck check : ArtifactChecks.getArtifactChecks()) {
         IStatus result = check.isDeleteableRelation(artifact, relationType);
         if (!result.isOK()) {
            throw new OseeStateException(result.getMessage());
         }
      }
   }

   private static void bulkLoadRelatives(Collection<Artifact> artifacts) throws OseeCoreException {
      Collection<Integer> artIds = new HashSet<Integer>();
      for (Artifact artifact : artifacts) {
         for (RelationLink link : artifact.getRelationsAll(DeletionFlag.EXCLUDE_DELETED)) {
            artIds.add(link.getAArtifactId());
            artIds.add(link.getBArtifactId());
         }
      }
      IOseeBranch branch = artifacts.iterator().next().getBranch();
      ArtifactQuery.getArtifactListFromIds(artIds, branch);
   }

   private static void deleteTrace(Artifact artifact, SkynetTransaction transaction, boolean reorderRelations) throws OseeCoreException {
      if (!artifact.isDeleted()) {
         // This must be done first since the the actual deletion of an
         // artifact clears out the link manager
         for (Artifact childArtifact : artifact.getChildren()) {
            deleteTrace(childArtifact, transaction, false);
         }
         try {
            // calling deCache here creates a race condition when the handleRelationModifiedEvent listeners fire - RS
            //          ArtifactCache.deCache(artifact);
            artifact.internalSetDeleted();
            RelationManager.deleteRelationsAll(artifact, reorderRelations, transaction);
            if (transaction != null) {
               artifact.persist(transaction);
            }
         } catch (OseeCoreException ex) {
            artifact.resetToPreviousModType();
            throw ex;
         }
      }
   }

   public static boolean isArtifactNewOnBranch(Artifact artifact) throws OseeCoreException {
      Branch branch = BranchManager.getBranch(artifact.getBranch());
      return ConnectionHandler.runPreparedQueryFetchInt(0, ARTIFACT_NEW_ON_BRANCH, artifact.getArtId(), branch.getId(),
         branch.getBaseTransaction().getId()) == 0;
   }

   public static boolean isRelationNewOnBranch(RelationLink relation) throws OseeCoreException {
      Branch branch = BranchManager.getBranch(relation.getBranch());
      return ConnectionHandler.runPreparedQueryFetchInt(-1, RELATION_NEW_ON_BRANCH, relation.getAArtifactId(),
         relation.getBArtifactId(), relation.getRelationType().getId(), branch.getId(),
         branch.getBaseTransaction().getId()) == 0;
   }
}