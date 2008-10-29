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
package org.eclipse.osee.framework.core.server.internal;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.data.OseeServerInfo;
import org.eclipse.osee.framework.core.server.CoreServerActivator;
import org.eclipse.osee.framework.core.server.IApplicationServerManager;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.db.connection.exception.OseeDataStoreException;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Roberto E. Escobar
 */
public class ApplicationServerManager implements IApplicationServerManager {

   private Map<String, OseeServerThreadFactory> threadFactories;
   private final Map<String, InternalOseeHttpServlet> oseeHttpServlets;

   private final OseeServerInfo applicationServerInfo;
   private boolean isRegistered;

   public ApplicationServerManager() {
      this.oseeHttpServlets = Collections.synchronizedMap(new HashMap<String, InternalOseeHttpServlet>());
      this.threadFactories = Collections.synchronizedMap(new HashMap<String, OseeServerThreadFactory>());
      this.applicationServerInfo = InternalOseeServerInfo.createFromLocalInfo();
      this.isRegistered = false;
   }

   private void checkDbRegistration() {
      if (!this.isRegistered) {
         try {
            ApplicationServerDataStore.deregisterWithDb(getApplicationServerInfo());
            ApplicationServerDataStore.registerWithDb(getApplicationServerInfo());
            this.isRegistered = true;
         } catch (Exception ex) {
            OseeLog.log(CoreServerActivator.class, Level.SEVERE, ex);
         }
      }
   }

   void register(String context, InternalOseeHttpServlet servlets) {
      checkDbRegistration();
      servlets.setRequestsAllowed(getApplicationServerInfo().isAcceptingRequests());
      this.oseeHttpServlets.put(context, servlets);
   }

   void unregister(String key) {
      this.oseeHttpServlets.remove(key);
      this.threadFactories.remove(key);
   }

   public OseeServerInfo getApplicationServerInfo() {
      return applicationServerInfo;
   }

   public ThreadFactory createNewThreadFactory(String name, int priority) {
      checkDbRegistration();
      OseeServerThreadFactory factory = new OseeServerThreadFactory(name, priority);
      this.threadFactories.put(name, factory);
      return factory;
   }

   private List<OseeServerThread> getThreadsFromFactory(String key) {
      OseeServerThreadFactory factory = threadFactories.get(key);
      return factory.getThreads();
   }

   public boolean isSystemIdle() {
      boolean result = true;
      for (String contexts : oseeHttpServlets.keySet()) {
         InternalOseeHttpServlet servlets = oseeHttpServlets.get(contexts);
         result &= !servlets.getState().equals(ProcessingStateEnum.BUSY);
      }

      for (String key : threadFactories.keySet()) {
         for (OseeServerThread thread : getThreadsFromFactory(key)) {
            State state = thread.getState();
            result &= !state.equals(State.TERMINATED);
         }
      }
      return result;
   }

   public synchronized void setServletRequestsAllowed(final boolean value) throws OseeDataStoreException {
      checkDbRegistration();
      if (getApplicationServerInfo().isAcceptingRequests() != value) {
         boolean wasSuccessful = ApplicationServerDataStore.updateServerState(getApplicationServerInfo(), value);
         if (wasSuccessful) {
            ((InternalOseeServerInfo) getApplicationServerInfo()).setAcceptingRequests(value);
            for (String contexts : oseeHttpServlets.keySet()) {
               InternalOseeHttpServlet servlets = oseeHttpServlets.get(contexts);
               servlets.setRequestsAllowed(value);
            }
         }
      }
   }

   public void shutdown() throws OseeCoreException {
      setServletRequestsAllowed(false);
      ApplicationServerDataStore.deregisterWithDb(getApplicationServerInfo());
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.resource.common.IApplicationServerManager#getCurrentProcesses()
    */
   @Override
   public List<String> getCurrentProcesses() {
      List<String> processList = new ArrayList<String>();
      for (String key : threadFactories.keySet()) {
         for (OseeServerThread thread : getThreadsFromFactory(key)) {
            State state = thread.getState();
            if (!state.equals(State.TERMINATED)) {
               processList.add(thread.getName());
            }
         }
      }
      for (String contexts : oseeHttpServlets.keySet()) {
         InternalOseeHttpServlet servlets = oseeHttpServlets.get(contexts);
         if (servlets.getState().equals(ProcessingStateEnum.BUSY)) {
            processList.add(servlets.getCurrentRequest());
         }
      }
      return processList;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.resource.common.IApplicationServerManager#getNumberOfActiveThreads()
    */
   @Override
   public int getNumberOfActiveThreads() {
      int totalProcesses = 0;
      for (String contexts : oseeHttpServlets.keySet()) {
         InternalOseeHttpServlet servlets = oseeHttpServlets.get(contexts);
         if (servlets.getState().equals(ProcessingStateEnum.BUSY)) {
            totalProcesses++;
         }
      }

      for (String key : threadFactories.keySet()) {
         for (OseeServerThread thread : getThreadsFromFactory(key)) {
            State state = thread.getState();
            if (!state.equals(State.TERMINATED)) {
               totalProcesses++;
            }
         }
      }
      return totalProcesses;
   }

}
