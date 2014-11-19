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

package org.eclipse.osee.orcs.db.internal.search.indexer.callable.producer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.data.AttributeTypes;
import org.eclipse.osee.orcs.db.internal.callable.AbstractDatastoreTxCallable;
import org.eclipse.osee.orcs.db.internal.search.indexer.IndexingTaskConsumer;
import org.eclipse.osee.orcs.db.internal.sql.join.SqlJoinFactory;
import org.eclipse.osee.orcs.db.internal.sql.join.TagQueueJoinQuery;
import org.eclipse.osee.orcs.search.IndexerCollector;

/**
 * @author Roberto E. Escobar
 */
public abstract class AbstractIndexerTxDatabaseCallable extends AbstractDatastoreTxCallable<List<Future<?>>> {

   private final SqlJoinFactory joinFactory;
   private final AttributeTypes types;
   private final IndexerCollector collector;
   private final int cacheLimit;
   private final boolean isCacheAll;
   private final List<Integer> queryIds;
   private final IndexingTaskConsumer consumer;
   private TagQueueJoinQuery currentJoinQuery;
   private boolean isOkToDispatch;
   private long totalGammas;
   private List<Future<?>> futures;

   protected AbstractIndexerTxDatabaseCallable(Log logger, OrcsSession session, IOseeDatabaseService dbService, SqlJoinFactory joinFactory, AttributeTypes types, IndexingTaskConsumer consumer, IndexerCollector collector, boolean isCacheAll, int cacheLimit) {
      super(logger, session, dbService, "Indexing Database Transaction");
      this.joinFactory = joinFactory;
      this.types = types;
      this.consumer = consumer;
      this.collector = collector;
      this.cacheLimit = cacheLimit;
      this.isCacheAll = isCacheAll;
      this.queryIds = new ArrayList<Integer>();
      this.isOkToDispatch = false;
      this.currentJoinQuery = null;
   }

   @Override
   protected List<Future<?>> handleTxWork(OseeConnection connection) throws OseeCoreException {
      totalGammas = 0;
      try {
         convertInput(connection);
      } catch (Exception ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      storeQueryIds(connection);
      if (collector != null) {
         collector.onIndexTaskTotalToProcess(this.queryIds.size());
         collector.onIndexTotalTaskItems(totalGammas);
      }
      isOkToDispatch = true;
      futures = new LinkedList<Future<?>>();
      return futures;
   }

   @Override
   protected void handleTxException(Exception ex) {
      isOkToDispatch = false;
      if (collector != null) {
         if (queryIds.isEmpty()) {
            collector.onIndexTaskError(-2, ex);
         } else {
            for (Integer queryId : queryIds) {
               collector.onIndexTaskError(queryId, ex);
            }
         }
      }
   }

   @Override
   protected void handleTxFinally() throws OseeCoreException {
      super.handleTxFinally();
      if (isOkToDispatch && !queryIds.isEmpty()) {
         for (int queryId : queryIds) {
            try {
               Future<?> future = consumer.submitTaskId(getSession(), types, collector, queryId);
               futures.add(future);
            } catch (Exception ex) {
               OseeExceptions.wrapAndThrow(ex);
            }
         }
      }
   }

   protected void addEntry(OseeConnection connection, long gammaId) throws OseeCoreException {
      if (currentJoinQuery == null) {
         currentJoinQuery = joinFactory.createTagQueueJoinQuery();
      }
      currentJoinQuery.add(gammaId);
      if (isStorageNeeded()) {
         storeQueryIds(connection);
      }
   }

   private boolean isStorageNeeded() {
      return !isCacheAll && currentJoinQuery != null && currentJoinQuery.size() > cacheLimit;
   }

   private void storeQueryIds(OseeConnection connection) throws OseeCoreException {
      if (currentJoinQuery != null && !currentJoinQuery.isEmpty()) {
         currentJoinQuery.store(connection);
         queryIds.add(currentJoinQuery.getQueryId());
         totalGammas += currentJoinQuery.size();
      }
      currentJoinQuery = null;
   }

   abstract protected void convertInput(OseeConnection connection) throws Exception;
}
