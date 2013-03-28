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
package org.eclipse.osee.ats.core.client;

import org.eclipse.osee.ats.core.client.internal.config.InternalConfigTestSuite;
import org.eclipse.osee.ats.core.client.internal.workdef.WorkDefTestSuite;
import org.eclipse.osee.ats.core.client.review.ReviewTestSuite;
import org.eclipse.osee.ats.core.client.workflow.WorkflowSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Donald G. Dunne
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({InternalConfigTestSuite.class, ReviewTestSuite.class, WorkDefTestSuite.class, WorkflowSuite.class})
public class AllAtsCoreClientTestSuite {
   // Test Suite
}
