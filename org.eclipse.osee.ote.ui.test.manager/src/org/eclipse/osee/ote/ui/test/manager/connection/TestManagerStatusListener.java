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
package org.eclipse.osee.ote.ui.test.manager.connection;

import java.rmi.RemoteException;
import org.eclipse.osee.ote.core.environment.status.IServiceStatusData;
import org.eclipse.osee.ote.core.environment.status.IServiceStatusListener;
import org.eclipse.osee.ote.ui.test.manager.core.TestManagerEditor;

/**
 * @author Andrew M. Finkbeiner
 */
public final class TestManagerStatusListener implements IServiceStatusListener {

   private TestManagerServiceStatusDataVisitor testManagerServiceDataVisitor;

   public TestManagerStatusListener(TestManagerEditor testManagerEditor, ScriptManager userEnvironment) {
      this.testManagerServiceDataVisitor = new TestManagerServiceStatusDataVisitor(userEnvironment, testManagerEditor);
   }

   public void statusBoardUpdated(IServiceStatusData statusData) throws RemoteException {
      statusData.accept(testManagerServiceDataVisitor);
   }
}
