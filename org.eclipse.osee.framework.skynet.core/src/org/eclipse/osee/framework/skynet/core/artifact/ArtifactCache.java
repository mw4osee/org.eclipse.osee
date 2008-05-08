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

import org.eclipse.osee.framework.jdk.core.type.CompositeKeyHashMap;
import org.eclipse.osee.framework.jdk.core.type.DoubleKeyHashMap;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionId;

/**
 * @author Ryan D. Brooks
 */
public class ArtifactCache {
   // The keys for this are <artId, transactionId>
   private final DoubleKeyHashMap<Integer, TransactionId, Artifact> artifactIdCache =
         new DoubleKeyHashMap<Integer, TransactionId, Artifact>();
   private final DoubleKeyHashMap<String, TransactionId, Artifact> artifactGuidCache =
         new DoubleKeyHashMap<String, TransactionId, Artifact>();

   private final CompositeKeyHashMap<Integer, Branch, Artifact> artifactBranchCache =
         new CompositeKeyHashMap<Integer, Branch, Artifact>(2000);

   private static final ArtifactCache instance = new ArtifactCache();

   private ArtifactCache() {
   }

   public static ArtifactCache getInstance() {
      return instance;
   }

   /**
    * Make the factory aware of an artifact. This is necessary when a new Artifact is persisted.
    * 
    * @param artifact
    */
   public void cache(Artifact artifact) {
      if (artifact.getPersistenceMemo().getTransactionId() == null) {
         artifactBranchCache.put(artifact.getArtId(), artifact.getBranch(), artifact);
      } else {
         artifactIdCache.put(artifact.getArtId(), artifact.getPersistenceMemo().getTransactionId(), artifact);
         artifactGuidCache.put(artifact.getGuid(), artifact.getPersistenceMemo().getTransactionId(), artifact);
      }
   }

   public void deCache(Artifact artifact) {
      artifactIdCache.remove(artifact.getArtId(), artifact.getPersistenceMemo().getTransactionId());
      artifactGuidCache.remove(artifact.getGuid(), artifact.getPersistenceMemo().getTransactionId());
      artifactBranchCache.remove(artifact.getArtId(), artifact.getBranch());
   }

   public Artifact getArtifactFromCache(int artId, TransactionId transactionId) {
      return artifactIdCache.get(artId, transactionId);
   }

   public Artifact getArtifactFromCache(String guid, TransactionId transactionId) {
      return artifactGuidCache.get(guid, transactionId);
   }

   public static Artifact getArtifact(int artId, Branch branch) {
      return instance.artifactBranchCache.get(artId, branch);
   }
}
