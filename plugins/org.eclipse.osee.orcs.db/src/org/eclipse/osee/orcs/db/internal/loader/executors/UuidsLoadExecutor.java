/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.db.internal.loader.executors;

import java.util.Collection;
import org.eclipse.osee.executor.admin.HasCancellation;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.jdbc.JdbcStatement;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.core.ds.LoadDataHandler;
import org.eclipse.osee.orcs.core.ds.Options;
import org.eclipse.osee.orcs.core.ds.OptionsUtil;
import org.eclipse.osee.orcs.db.internal.loader.LoadSqlContext;
import org.eclipse.osee.orcs.db.internal.loader.LoadUtil;
import org.eclipse.osee.orcs.db.internal.loader.SqlObjectLoader;
import org.eclipse.osee.orcs.db.internal.loader.criteria.CriteriaOrcsLoad;
import org.eclipse.osee.orcs.db.internal.sql.join.ArtifactJoinQuery;
import org.eclipse.osee.orcs.db.internal.sql.join.CharJoinQuery;
import org.eclipse.osee.orcs.db.internal.sql.join.SqlJoinFactory;

/**
 * @author Roberto E. Escobar
 */
public class UuidsLoadExecutor extends AbstractLoadExecutor {

   private static final String GUIDS_TO_IDS =
      "SELECT art.art_id FROM osee_join_char_id jid, osee_artifact art WHERE jid.query_id = ? AND jid.id = art.guid";

   private final SqlJoinFactory joinFactory;
   private final OrcsSession session;
   private final IOseeBranch branch;
   private final Collection<String> artifactIds;

   public UuidsLoadExecutor(SqlObjectLoader loader, JdbcClient jdbcClient, SqlJoinFactory joinFactory, OrcsSession session, IOseeBranch branch, Collection<String> artifactIds) {
      super(loader, jdbcClient);
      this.joinFactory = joinFactory;
      this.session = session;
      this.branch = branch;
      this.artifactIds = artifactIds;
   }

   @Override
   public void load(HasCancellation cancellation, LoadDataHandler handler, CriteriaOrcsLoad criteria, Options options) throws OseeCoreException {
      checkCancelled(cancellation);
      if (!artifactIds.isEmpty()) {
         ArtifactJoinQuery join = createIdJoin(getJdbcClient(), options);
         LoadSqlContext loadContext = new LoadSqlContext(session, options, branch);
         int fetchSize = LoadUtil.computeFetchSize(artifactIds.size());
         getLoader().loadArtifacts(cancellation, handler, join, criteria, loadContext, fetchSize);
      }
   }

   private ArtifactJoinQuery createIdJoin(JdbcClient jdbcClient, Options options) throws OseeCoreException {

      ArtifactJoinQuery toReturn = joinFactory.createArtifactJoinQuery();

      CharJoinQuery guidJoin = joinFactory.createCharJoinQuery();
      try {
         for (String id : artifactIds) {
            guidJoin.add(id);
         }
         guidJoin.store();

         Integer transactionId = OptionsUtil.getFromTransaction(options);

         JdbcStatement chStmt = null;
         try {
            chStmt = jdbcClient.getStatement();
            chStmt.runPreparedQuery(artifactIds.size(), GUIDS_TO_IDS, guidJoin.getQueryId());
            while (chStmt.next()) {
               Integer artId = chStmt.getInt("art_id");
               toReturn.add(artId, branch.getUuid(), transactionId);
            }
         } finally {
            Lib.close(chStmt);
         }
      } finally {
         guidJoin.delete();
      }
      return toReturn;
   }
}
