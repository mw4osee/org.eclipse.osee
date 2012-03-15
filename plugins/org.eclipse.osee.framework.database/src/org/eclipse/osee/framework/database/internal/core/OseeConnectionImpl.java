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
package org.eclipse.osee.framework.database.internal.core;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.database.core.OseeConnection;

public class OseeConnectionImpl extends OseeConnection {
   final static private long timeout = 60000;
   private final OseeConnectionPoolImpl pool;
   private final Connection conn;
   private volatile boolean inuse;
   private volatile long lastUsedTime;

   public OseeConnectionImpl(Connection conn, OseeConnectionPoolImpl pool) {
      super();
      this.conn = conn;
      this.pool = pool;
      this.inuse = true;
      this.lastUsedTime = 0;
   }

   @Override
   public void close() throws OseeCoreException {
      if (isClosed()) {
         destroy();
      } else {
         pool.returnConnection(this);
      }
   }

   @Override
   public boolean isClosed() throws OseeCoreException {
      try {
         return conn.isClosed();
      } catch (SQLException ex) {
         OseeExceptions.wrapAndThrow(ex);
         return false; // unreachable since wrapAndThrow() always throws an exception
      }
   }

   @Override
   public boolean isStale() {
      return !inUse() && getLastUse() + timeout < System.currentTimeMillis();
   }

   @Override
   public DatabaseMetaData getMetaData() throws OseeCoreException {
      try {
         return conn.getMetaData();
      } catch (SQLException ex) {
         OseeExceptions.wrapAndThrow(ex);
         return null; // unreachable since wrapAndThrow() always throws an exception
      }
   }

   PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
   }

   PreparedStatement prepareStatement(String sql) throws SQLException {
      return prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
   }

   CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
   }

   synchronized boolean lease() {
      if (inuse) {
         return false;
      } else {
         inuse = true;
         return true;
      }
   }

   @Override
   protected void destroy() throws OseeCoreException {
      pool.removeConnection(this);
      try {
         conn.close();
      } catch (SQLException ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
   }

   boolean inUse() {
      return inuse;
   }

   long getLastUse() {
      return lastUsedTime;
   }

   synchronized void expireLease() {
      inuse = false;
      lastUsedTime = System.currentTimeMillis();
   }

   @Override
   protected void setAutoCommit(boolean autoCommit) throws OseeCoreException {
      try {
         conn.setAutoCommit(autoCommit);
      } catch (SQLException ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
   }

   @Override
   protected boolean getAutoCommit() throws OseeCoreException {
      try {
         return conn.getAutoCommit();
      } catch (SQLException ex) {
         OseeExceptions.wrapAndThrow(ex);
         return false; // unreachable since wrapAndThrow() always throws an exception
      }
   }

   @Override
   protected void commit() throws OseeCoreException {
      try {
         conn.commit();
      } catch (SQLException ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
   }

   @Override
   protected void rollback() throws OseeCoreException {
      try {
         conn.rollback();
      } catch (SQLException ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
   }

}