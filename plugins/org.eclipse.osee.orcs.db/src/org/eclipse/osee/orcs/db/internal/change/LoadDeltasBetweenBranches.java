/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.db.internal.change;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.osee.framework.core.data.ApplicabilityId;
import org.eclipse.osee.framework.core.data.ApplicabilityToken;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactTypeId;
import org.eclipse.osee.framework.core.data.AttributeId;
import org.eclipse.osee.framework.core.data.AttributeTypeId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.GammaId;
import org.eclipse.osee.framework.core.data.RelationId;
import org.eclipse.osee.framework.core.data.RelationTypeId;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.core.data.TupleTypeId;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.TxChange;
import org.eclipse.osee.framework.core.model.change.ChangeItem;
import org.eclipse.osee.framework.core.model.change.ChangeItemUtil;
import org.eclipse.osee.framework.core.model.change.ChangeVersion;
import org.eclipse.osee.framework.jdk.core.type.DoubleKeyHashMap;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.jdbc.JdbcConstants;
import org.eclipse.osee.jdbc.JdbcStatement;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.db.internal.callable.AbstractDatastoreCallable;
import org.eclipse.osee.orcs.db.internal.sql.join.ExportImportJoinQuery;
import org.eclipse.osee.orcs.db.internal.sql.join.SqlJoinFactory;
import org.eclipse.osee.orcs.search.ApplicabilityQuery;

/**
 * @author Ryan D. Brooks
 * @author Roberto E. Escobar
 * @author Ryan Schmitt
 * @author Jeff C. Phillips
 */
public class LoadDeltasBetweenBranches extends AbstractDatastoreCallable<List<ChangeItem>> {
   // @formatter:off
   private static final String SELECT_ALL_SOURCE_ADDRESSING =
      "with\n" + "txsOuter as (select transaction_id, gamma_id, mod_type, app_id from osee_txs txs where \n" +
         "branch_id = ? and txs.tx_current <> ? and transaction_id <> ? AND \n" +
         "NOT EXISTS (SELECT 1 FROM osee_txs txs1 WHERE txs1.branch_id = ? AND txs1.transaction_id = ? \n" +
         "AND txs1.gamma_id = txs.gamma_id and txs1.mod_type = txs.mod_type and txs1.app_id = txs.app_id)) \n" +
         "SELECT 1 as table_type, attr_type_id as item_type_id, attr_id as item_id, art_id as item_first, 0 as item_second, 0 as item_third, 0 as item_fourth, value as item_value, item.gamma_id, mod_type, app_id \n" +
         "FROM osee_attribute item, txsOuter where txsOuter.gamma_id = item.gamma_id\n" +
         "UNION ALL\n" +
         "SELECT 2 as table_type, art_type_id as item_type_id, art_id as item_id, 0 as item_first, 0 as item_second, 0 as item_third, 0 as item_fourth, 'na' as item_value, item.gamma_id, mod_type, app_id \n" +
         "FROM osee_artifact item, txsOuter where txsOuter.gamma_id = item.gamma_id\n" +
         "UNION ALL\n" +
         "SELECT 3 as table_type, rel_link_type_id as item_type_id, rel_link_id as item_id,  a_art_id as item_first, b_art_id as item_second, 0 as item_third, 0 as item_fourth, rationale as item_value, item.gamma_id, mod_type, app_id \n" +
         "FROM osee_relation_link item, txsOuter where txsOuter.gamma_id = item.gamma_id\n" +
         "UNION ALL\n" +
         "SELECT 4 as table_type, tuple_type as item_type_id, 0 as item_id, e1 as item_first, e2 as item_second, 0 as item_third, 0 as item_fourth, 'na' as item_value, item.gamma_id, mod_type, app_id \n" +
         "from osee_tuple2 item, txsOuter where txsOuter.gamma_id = item.gamma_id\n" +
         "UNION ALL\n" +
         "SELECT 5 as table_type, tuple_type as item_type_id, 0 as item_id, e1 as item_first, e2 as item_second, e3 as item_third, 0 as item_fourth, 'na' as item_value, item.gamma_id, mod_type, app_id \n" +
         "from osee_tuple3 item, txsOuter where txsOuter.gamma_id = item.gamma_id\n" +
         "UNION ALL\n" +
         "SELECT 6 as table_type, tuple_type as item_type_id, 0 as item_id, e1 as item_first, e2 as item_second, e3 as item_third, e4 as item_fourth, 'na' as item_value, item.gamma_id, mod_type, app_id \n" +
         "from osee_tuple4 item, txsOuter where txsOuter.gamma_id = item.gamma_id";

   // @formatter:on
   private static final String SELECT_BASE_TX = "select baseline_transaction_id from osee_branch where branch_id = ?";
   private final BranchId sourceBranch, destinationBranch;
   private final BranchId mergeBranch;
   private final TransactionId mergeTxId;
   private final TransactionId destinationHeadTxId;
   private final SqlJoinFactory joinFactory;
   private final HashMap<Long, ApplicabilityToken> applicTokens;

   public LoadDeltasBetweenBranches(Log logger, OrcsSession session, JdbcClient jdbcClient, SqlJoinFactory joinFactory, BranchId sourceBranch, BranchId destinationBranch, TransactionId destinationHeadTxId, BranchId mergeBranch, TransactionId mergeTxId, ApplicabilityQuery applicQuery) {
      super(logger, session, jdbcClient);
      this.joinFactory = joinFactory;
      this.sourceBranch = sourceBranch;
      this.destinationBranch = destinationBranch;
      this.destinationHeadTxId = destinationHeadTxId;
      this.mergeBranch = mergeBranch;
      this.mergeTxId = mergeTxId;
      this.applicTokens = applicQuery.getApplicabilityTokens(sourceBranch, destinationBranch);
   }

   private ApplicabilityToken getApplicabilityToken(ApplicabilityId appId) {
      ApplicabilityToken toReturn = applicTokens.get(appId.getId());
      if (toReturn != null) {
         return toReturn;
      }
      return ApplicabilityToken.BASE;
   }

   private boolean hasMergeBranch() {
      return mergeBranch.isValid();
   }

   @Override
   public List<ChangeItem> call() throws Exception {
      Conditions.checkExpressionFailOnTrue(sourceBranch.equals(destinationBranch),
         "Unable to compute deltas between transactions on the same branch [%s]", sourceBranch);

      TransactionId sourceBaselineTxId = getJdbcClient().fetch(TransactionId.SENTINEL, SELECT_BASE_TX, sourceBranch);
      DoubleKeyHashMap<Integer, Long, ChangeItem> newChangeData = this.loadSourceBranchChanges(sourceBaselineTxId);
      return loadItemsbyId(newChangeData, sourceBaselineTxId);
   }

   private List<ChangeItem> loadItemsbyId(DoubleKeyHashMap<Integer, Long, ChangeItem> changeData, TransactionId sourceBaselineTxId) {
      try (ExportImportJoinQuery idJoin = joinFactory.createExportImportJoinQuery()) {
         for (Integer i : changeData.getKeySetOne()) {
            for (ChangeItem item : changeData.allValues(i)) {
               idJoin.add(i, item.getItemId());
            }
         }
         idJoin.store();

         if (hasMergeBranch()) {
            loadCurrentVersionData(idJoin, changeData, mergeBranch, mergeTxId, true);
         }
         loadCurrentVersionData(idJoin, changeData, destinationBranch, destinationHeadTxId, false);

         loadNonCurrentSourceVersionData(idJoin, changeData, sourceBaselineTxId);
      }
      List<ChangeItem> list = new LinkedList<ChangeItem>(changeData.allValues());
      return list;
   }

   private DoubleKeyHashMap<Integer, Long, ChangeItem> loadSourceBranchChanges(TransactionId sourceBaselineTxId) throws OseeCoreException {
      DoubleKeyHashMap<Integer, Long, ChangeItem> hashChangeData = new DoubleKeyHashMap<>();

      Consumer<JdbcStatement> consumer = stmt -> {
         checkForCancelled();
         GammaId gammaId = GammaId.valueOf(stmt.getLong("gamma_id"));
         ModificationType modType = ModificationType.getMod(stmt.getInt("mod_type"));
         ApplicabilityId appId = ApplicabilityId.valueOf(stmt.getLong("app_id"));
         int tableType = stmt.getInt("table_type");
         Long itemId = stmt.getLong("item_id");
         Long itemTypeId = stmt.getLong("item_type_id");
         switch (tableType) {
            case 1:
               ArtifactId artId = ArtifactId.valueOf(stmt.getLong("item_first"));
               String value = stmt.getString("item_value");
               hashChangeData.put(1, itemId, ChangeItemUtil.newAttributeChange(AttributeId.valueOf(itemId),
                  AttributeTypeId.valueOf(itemTypeId), artId, gammaId, modType, value, getApplicabilityToken(appId)));
               break;

            case 2: {
               hashChangeData.put(2, itemId, ChangeItemUtil.newArtifactChange(ArtifactId.valueOf(itemId),
                  ArtifactTypeId.valueOf(itemTypeId), gammaId, modType, getApplicabilityToken(appId)));
               break;
            }
            case 3: {
               ArtifactId aArtId = ArtifactId.valueOf(stmt.getLong("item_first"));
               ArtifactId bArtId = ArtifactId.valueOf(stmt.getLong("item_second"));
               String rationale = stmt.getString("item_value");
               hashChangeData.put(3, itemId,
                  ChangeItemUtil.newRelationChange(RelationId.valueOf(itemId), RelationTypeId.valueOf(itemTypeId),
                     gammaId, modType, aArtId, bArtId, rationale, getApplicabilityToken(appId)));
               break;
            }
            case 4: {
               long e1 = stmt.getLong("item_first");
               long e2 = stmt.getLong("item_second");
               hashChangeData.put(4, gammaId.getId(), ChangeItemUtil.newTupleChange(TupleTypeId.valueOf(itemTypeId),
                  gammaId, getApplicabilityToken(appId), e1, e2));
               break;
            }
            case 5: {
               long e1 = stmt.getLong("item_first");
               long e2 = stmt.getLong("item_second");
               long e3 = stmt.getLong("item_third");
               hashChangeData.put(5, gammaId.getId(), ChangeItemUtil.newTupleChange(TupleTypeId.valueOf(itemTypeId),
                  gammaId, getApplicabilityToken(appId), e1, e2, e3));
               break;
            }
            case 6: {
               long e1 = stmt.getLong("item_first");
               long e2 = stmt.getLong("item_second");
               long e3 = stmt.getLong("item_third");
               long e4 = stmt.getLong("item_fourth");
               hashChangeData.put(6, gammaId.getId(), ChangeItemUtil.newTupleChange(TupleTypeId.valueOf(itemTypeId),
                  gammaId, getApplicabilityToken(appId), e1, e2, e3, e4));
               break;
            }

         }
      };
      getJdbcClient().runQuery(consumer, JdbcConstants.JDBC__MAX_FETCH_SIZE, SELECT_ALL_SOURCE_ADDRESSING, sourceBranch,
         TxChange.NOT_CURRENT.getValue(), sourceBaselineTxId, sourceBranch, sourceBaselineTxId);

      return hashChangeData;
   }

   private void loadCurrentVersionData(ExportImportJoinQuery idJoin, DoubleKeyHashMap<Integer, Long, ChangeItem> changesByItemId, BranchId txBranchId, TransactionId txId, boolean isMergeBranch) throws OseeCoreException {
      Consumer<JdbcStatement> consumer = stmt -> {
         checkForCancelled();

         Long itemId = stmt.getLong("item_id");
         Integer tableType = stmt.getInt("table_type");
         ApplicabilityId appId = ApplicabilityId.valueOf(stmt.getLong("app_id"));
         GammaId gammaId = GammaId.valueOf(stmt.getLong("gamma_id"));
         ChangeItem change = changesByItemId.get(tableType, itemId);

         if (isMergeBranch) {
            change.getNetChange().setGammaId(gammaId);
            change.getNetChange().setModType(ModificationType.MERGED);
            change.getNetChange().setApplicabilityToken(getApplicabilityToken(appId));
         } else {
            change.getDestinationVersion().setModType(ModificationType.getMod(stmt.getInt("mod_type")));
            change.getDestinationVersion().setGammaId(gammaId);
            change.getDestinationVersion().setApplicabilityToken(getApplicabilityToken(appId));
         }
      };

      String query =
         "select txs.gamma_id, txs.mod_type, txs.app_id, item.art_id as item_id, 2 as table_type from osee_join_export_import idj," + //
            " osee_artifact item, osee_txs txs where idj.query_id = ? and idj.id2 = item.art_id and idj.id1 = 2" + //
            " and item.gamma_id = txs.gamma_id and txs.tx_current <> ? and txs.branch_id = ? and txs.transaction_id <= ?" + //
            " union all select txs.gamma_id, txs.mod_type, txs.app_id, item.attr_id as item_id, 1 as table_type from osee_join_export_import idj," + //
            " osee_attribute item, osee_txs txs where idj.query_id = ? and idj.id2 = item.attr_id and idj.id1 = 1" + //
            " and item.gamma_id = txs.gamma_id and txs.tx_current <> ? and txs.branch_id = ? and txs.transaction_id <= ?" + //
            " union all select txs.gamma_id, txs.mod_type, txs.app_id, item.rel_link_id as item_id, 3 as table_type from osee_join_export_import idj," + //
            " osee_relation_link item, osee_txs txs where idj.query_id = ? and idj.id2 = item.rel_link_id and idj.id1 = 3" + //
            " and item.gamma_id = txs.gamma_id and txs.tx_current <> ? and txs.branch_id = ? and txs.transaction_id <= ?";

      getJdbcClient().runQuery(consumer, JdbcConstants.JDBC__MAX_FETCH_SIZE, query, idJoin.getQueryId(),
         TxChange.NOT_CURRENT.getValue(), txBranchId, txId, idJoin.getQueryId(), TxChange.NOT_CURRENT.getValue(),
         txBranchId, txId, idJoin.getQueryId(), TxChange.NOT_CURRENT.getValue(), txBranchId, txId);

   }

   private void loadNonCurrentSourceVersionData(ExportImportJoinQuery idJoin, DoubleKeyHashMap<Integer, Long, ChangeItem> changesByItemId, TransactionId sourceBaselineTxId) throws OseeCoreException {
      try (JdbcStatement chStmt = getJdbcClient().getStatement()) {
         String query =
            "select * from (select null as value, item.art_id as item_id, txs.gamma_id, txs.mod_type, txs.app_id, txs.transaction_id, idj.id2, 2 as table_type from osee_join_export_import idj, " + //
               "osee_artifact item, osee_txs txs where idj.query_id = ? and idj.id2 = item.art_id and idj.id1 = 2 " + //
               "and item.gamma_id = txs.gamma_id and txs.tx_current = ? and txs.branch_id = ? " + //
               "union all select item.value as value, item.attr_id as item_id, txs.gamma_id, txs.mod_type, txs.app_id, txs.transaction_id, idj.id2, 1 as table_type from osee_join_export_import idj, " + //
               "osee_attribute item, osee_txs txs where idj.query_id = ? and idj.id2 = item.attr_id and idj.id1 = 1 " + //
               "and item.gamma_id = txs.gamma_id and txs.tx_current = ? and txs.branch_id = ? " + //
               "union all select null as value, item.rel_link_id as item_id, txs.gamma_id, txs.mod_type, txs.app_id, txs.transaction_id, idj.id2, 3 as table_type from osee_join_export_import idj, " + //
               "osee_relation_link item, osee_txs txs where idj.query_id = ? and idj.id2 = item.rel_link_id and idj.id1 = 3 " + //
               "and item.gamma_id = txs.gamma_id and txs.tx_current = ? and txs.branch_id = ?) t order by t.id2, t.transaction_id asc";

         chStmt.runPreparedQuery(JdbcConstants.JDBC__MAX_FETCH_SIZE, query, idJoin.getQueryId(),
            TxChange.NOT_CURRENT.getValue(), sourceBranch, idJoin.getQueryId(), TxChange.NOT_CURRENT.getValue(),
            sourceBranch, idJoin.getQueryId(), TxChange.NOT_CURRENT.getValue(), sourceBranch);

         Long previousItemId = -1L;
         boolean isFirstSet = false;
         while (chStmt.next()) {
            checkForCancelled();
            Long itemId = chStmt.getLong("item_id");
            Integer tableType = chStmt.getInt("table_type");
            Long transactionId = chStmt.getLong("transaction_id");
            ApplicabilityId appId = ApplicabilityId.valueOf(chStmt.getLong("app_id"));
            ModificationType modType = ModificationType.getMod(chStmt.getInt("mod_type"));
            GammaId gammaId = GammaId.valueOf(chStmt.getLong("gamma_id"));

            String value = chStmt.getString("value");

            ChangeItem change = changesByItemId.get(tableType, itemId);
            if (previousItemId != itemId) {
               isFirstSet = false;
            }
            if (sourceBaselineTxId.equals(transactionId)) {
               setVersionData(change.getBaselineVersion(), gammaId, modType, value, appId);
            } else if (!isFirstSet) {
               setVersionData(change.getFirstNonCurrentChange(), gammaId, modType, value, appId);
               isFirstSet = true;
            }
            previousItemId = itemId;
         }
      }
   }

   private void setVersionData(ChangeVersion versionedChange, GammaId gammaId, ModificationType modType, String value, ApplicabilityId appId) {
      // Tolerates the case of having more than one version of an item on a
      // baseline transaction by picking the most recent one
      if (versionedChange.getGammaId() == null || versionedChange.getGammaId().getId().compareTo(gammaId.getId()) < 0) {
         versionedChange.setValue(value);
         versionedChange.setModType(modType);
         versionedChange.setGammaId(gammaId);
         versionedChange.setApplicabilityToken(getApplicabilityToken(appId));
      }
   }
}