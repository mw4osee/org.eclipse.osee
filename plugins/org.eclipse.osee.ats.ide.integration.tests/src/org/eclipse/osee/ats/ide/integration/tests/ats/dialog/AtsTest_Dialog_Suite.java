/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.ide.integration.tests.ats.dialog;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Donald G. Dunne
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ //
   ActionableItemListDialogTest.class,
   NewActionWizardTest.class,
   AICheckedTreeDialogTest.class,
   ActionActionableItemListDialogTest.class,
   ActionableItemTreeWithChildrenDialogTest.class,
   TeamDefinitionCheckTreeDialogTest.class,
   TeamDefinitionTreeWithChildrenDialogTest.class //
})
public class AtsTest_Dialog_Suite {
   // do nothing
}
