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
package org.eclipse.osee.framework.ui.skynet.blam.operation;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.db.connection.info.SQL3DataType;
import org.eclipse.osee.framework.jdk.core.util.time.GlobalTime;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactLoader;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.change.TxChange;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionIdManager;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.blam.BlamVariableMap;

/**
 * @author Ryan D. Brooks
 */
public class UpdateFromParentBranch extends AbstractBlam {
   private static final String DELETE_GAMMAS_FOR_UPDATES =
         "DELETE FROM osee_txs WHERE (transaction_id, gamma_id) IN (Select tx1.transaction_id, tx1.gamma_id FROM osee_txs tx1, osee_tx_details td1, osee_artifact_version av1, osee_join_artifact ja1 WHERE td1.branch_id = ? AND td1.transaction_id = tx1.transaction_id AND tx1.gamma_id = av1.gamma_id AND av1.art_id = ja1.art_id AND ja1.branch_id = ? AND ja1.query_id = ? UNION Select tx2.transaction_id, tx2.gamma_id FROM osee_txs tx2, osee_tx_details td2, osee_attribute at2, osee_join_artifact ja2 WHERE td2.branch_id = ? AND td2.transaction_id = tx2.transaction_id AND tx2.gamma_id = at2.gamma_id AND at2.art_id = ja2.art_id AND ja2.branch_id = ? AND ja2.query_id = ? UNION Select tx3.transaction_id, tx3.gamma_id FROM osee_txs tx3, osee_tx_details td3, osee_relation_link rl3, osee_join_artifact ja3 WHERE td3.branch_id = ? AND td3.transaction_id = tx3.transaction_id AND tx3.gamma_id = rl3.gamma_id AND (rl3.a_art_id = ja3.art_id OR rl3.b_art_id = ja3.art_id) AND ja3.branch_id = ? AND ja3.query_id = ?)";
   private static final String INSERT_UPDATED_ARTIFACTS =
         "INSERT INTO osee_txs (transaction_id, gamma_id, mod_type, tx_current) SELECT ?, tx1.gamma_id, tx1.mod_type, tx1.tx_current FROM osee_txs tx1, osee_tx_details td1, osee_artifact_version av1, osee_join_artifact ja1 WHERE td1.branch_id = ? AND td1.transaction_id = tx1.transaction_id AND tx1.tx_current = " + TxChange.CURRENT.getValue() + " AND tx1.gamma_id = av1.gamma_id AND td1.branch_id = ja1.branch_id AND av1.art_id = ja1.art_id AND ja1.query_id = ?";
   private static final String INSERT_UPDATED_ATTRIBUTES_GAMMAS =
         "INSERT INTO osee_txs (transaction_id, gamma_id, mod_type, tx_current) SELECT ?, tx1.gamma_id, tx1.mod_type, tx1.tx_current FROM osee_txs tx1, osee_tx_details td1, osee_attribute at1, osee_join_artifact ja1 WHERE td1.branch_id = ? AND td1.transaction_id = tx1.transaction_id AND tx1.tx_current = " + TxChange.CURRENT.getValue() + " AND tx1.gamma_id = at1.gamma_id AND td1.branch_id = ja1.branch_id AND at1.art_id = ja1.art_id AND ja1.query_id = ?";
   private static final String INSERT_UPDATED_LINKS_GAMMAS =
         "INSERT INTO osee_txs (transaction_id, gamma_id, mod_type, tx_current) SELECT DISTINCT ?, tx1.gamma_id, tx1.mod_type, tx1.tx_current FROM osee_txs tx1, osee_tx_details td1, osee_relation_link rl1, osee_join_artifact ja1 WHERE td1.branch_id = ? AND td1.transaction_id = tx1.transaction_id AND tx1.tx_current = " + TxChange.CURRENT.getValue() + " AND tx1.gamma_id = rl1.gamma_id AND td1.branch_id = ja1.branch_id AND (rl1.a_art_id = ja1.art_id OR rl1.b_art_id = ja1.art_id) AND ja1.query_id = ?";

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.blam.operation.BlamOperation#runOperation(org.eclipse.osee.framework.ui.skynet.blam.BlamVariableMap, org.eclipse.osee.framework.skynet.core.artifact.Branch, org.eclipse.core.runtime.IProgressMonitor)
    */
   public void runOperation(BlamVariableMap variableMap, IProgressMonitor monitor, SkynetTransaction transaction) throws Exception {
      monitor.beginTask("Update From Parent Branch", IProgressMonitor.UNKNOWN);

      List<Artifact> artifacts = variableMap.getArtifacts("Parent Branch Artifacts to update to Child Branch");
      Branch childBranch = variableMap.getBranch("Child Branch Name");

      Branch parentBranch = childBranch.getParentBranch();
      int baselineTransactionNumber =
            TransactionIdManager.getStartEndPoint(childBranch).getKey().getTransactionNumber();
      int queryId = ArtifactLoader.getNewQueryId();
      Timestamp insertTime = GlobalTime.GreenwichMeanTimestamp();
      List<Object[]> insertParameters = new LinkedList<Object[]>();

      try {
         // insert into the artifact_join_table
         for (Artifact artifact : artifacts) {
            insertParameters.add(new Object[] {queryId, insertTime, artifact.getArtId(), parentBranch.getBranchId(),
                  SQL3DataType.INTEGER});
         }
         ArtifactLoader.selectArtifacts(insertParameters);

         int count =
               ConnectionHandler.runPreparedUpdate(DELETE_GAMMAS_FOR_UPDATES, childBranch.getBranchId(),
                     parentBranch.getBranchId(), queryId, childBranch.getBranchId(), parentBranch.getBranchId(),
                     queryId, childBranch.getBranchId(), parentBranch.getBranchId(), queryId);
         OseeLog.log(SkynetGuiPlugin.class, Level.INFO,  "deleted " + count + " gammas");

         count =
               ConnectionHandler.runPreparedUpdate(INSERT_UPDATED_ARTIFACTS, baselineTransactionNumber,
                     parentBranch.getBranchId(), queryId);
         OseeLog.log(SkynetGuiPlugin.class, Level.INFO,  "inserted " + count + " artifacts");

         count =
               ConnectionHandler.runPreparedUpdate(INSERT_UPDATED_ATTRIBUTES_GAMMAS, baselineTransactionNumber,
                     parentBranch.getBranchId(), queryId);
         OseeLog.log(SkynetGuiPlugin.class, Level.INFO,  "inserted " + count + " attributes");

         count =
               ConnectionHandler.runPreparedUpdate(INSERT_UPDATED_LINKS_GAMMAS, baselineTransactionNumber,
                     parentBranch.getBranchId(), queryId);
         OseeLog.log(SkynetGuiPlugin.class, Level.INFO,  "inserted " + count + " relations");

         monitor.done();
      } finally {
         ArtifactLoader.clearQuery(queryId);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.blam.operation.BlamOperation#getXWidgetXml()
    */
   public String getXWidgetsXml() {
      return "<xWidgets><XWidget xwidgetType=\"XBranchSelectWidget\" displayName=\"Child Branch Name\" /><XWidget xwidgetType=\"XListDropViewer\" displayName=\"Parent Branch Artifacts to update to Child Branch\" /></xWidgets>";
   }
}