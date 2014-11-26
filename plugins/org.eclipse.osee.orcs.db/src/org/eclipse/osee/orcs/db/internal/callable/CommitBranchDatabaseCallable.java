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
package org.eclipse.osee.orcs.db.internal.callable;

import java.util.List;
import java.util.concurrent.Callable;
import org.eclipse.osee.executor.admin.CancellableCallable;
import org.eclipse.osee.framework.core.model.change.ChangeItem;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.BranchReadable;
import org.eclipse.osee.orcs.data.TransactionReadable;
import org.eclipse.osee.orcs.db.internal.IdentityManager;
import org.eclipse.osee.orcs.db.internal.change.ComputeNetChangeCallable;
import org.eclipse.osee.orcs.db.internal.change.LoadDeltasBetweenBranches;
import org.eclipse.osee.orcs.db.internal.change.MissingChangeItemFactory;
import org.eclipse.osee.orcs.db.internal.sql.join.SqlJoinFactory;

/**
 * @author Roberto E. Escobar
 */
public class CommitBranchDatabaseCallable extends AbstractDatastoreCallable<Integer> {

   private final SqlJoinFactory joinFactory;
   private final IdentityManager idManager;
   private final ArtifactReadable committer;
   private final TransactionReadable sourceHead;
   private final BranchReadable source;
   private final TransactionReadable destinationHead;
   private final BranchReadable destination;
   private final MissingChangeItemFactory missingChangeItemFactory;

   private static final String SELECT_MERGE_BRANCH_UUID =
      "select merge_branch_id from osee_merge where source_branch_id = ? and dest_branch_id = ?";
   private static final String SELECT_MERGE_BRANCH_HEAD_TX =
      "select max(transaction_id) from osee_tx_details where branch_id = ?";

   public CommitBranchDatabaseCallable(Log logger, OrcsSession session, JdbcClient service, SqlJoinFactory joinFactory, IdentityManager idManager, ArtifactReadable committer, BranchReadable source, TransactionReadable sourceHead, BranchReadable destination, TransactionReadable destinationHead, MissingChangeItemFactory missingChangeItemFactory) {
      super(logger, session, service);
      this.joinFactory = joinFactory;
      this.idManager = idManager;
      this.committer = committer;
      this.source = source;
      this.sourceHead = sourceHead;
      this.destination = destination;
      this.destinationHead = destinationHead;
      this.missingChangeItemFactory = missingChangeItemFactory;
   }

   private int getUserArtId() {
      return committer != null ? committer.getLocalId() : -1;
   }

   private List<ChangeItem> callComputeChanges(Long mergeBranch) throws Exception {
      Long mergeBranchId = null;
      Integer mergeTxId = null;
      if (mergeBranch != null && mergeBranch > 0) {
         mergeBranchId = mergeBranch;
         mergeTxId = getJdbcClient().runPreparedQueryFetchObject(-1, SELECT_MERGE_BRANCH_HEAD_TX, mergeBranchId);
      }

      Callable<List<ChangeItem>> loadChanges =
         new LoadDeltasBetweenBranches(getLogger(), getSession(), getJdbcClient(), joinFactory,
            sourceHead.getBranchId(), destinationHead.getBranchId(), destinationHead.getGuid(), mergeBranchId,
            mergeTxId);
      List<ChangeItem> changes = callAndCheckForCancel(loadChanges);

      changes.addAll(missingChangeItemFactory.createMissingChanges(this, getSession(), changes, sourceHead,
         destinationHead));

      Callable<List<ChangeItem>> computeChanges = new ComputeNetChangeCallable(changes);
      return callAndCheckForCancel(computeChanges);
   }

   @Override
   public Integer call() throws Exception {
      Long mergeBranchUuid =
         getJdbcClient().runPreparedQueryFetchObject(-1L, SELECT_MERGE_BRANCH_UUID, source.getGuid(),
            destination.getGuid());
      List<ChangeItem> changes = callComputeChanges(mergeBranchUuid);

      CancellableCallable<Integer> commitCallable =
         new CommitBranchDatabaseTxCallable(getLogger(), getSession(), getJdbcClient(), joinFactory, idManager,
            getUserArtId(), source, destination, mergeBranchUuid, changes);
      Integer newTx = callAndCheckForCancel(commitCallable);

      return newTx;
   }
}
