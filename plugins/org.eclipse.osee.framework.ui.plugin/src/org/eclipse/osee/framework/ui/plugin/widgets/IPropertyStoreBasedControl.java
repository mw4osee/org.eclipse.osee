/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.framework.ui.plugin.widgets;

import org.eclipse.osee.framework.jdk.core.type.IPropertyStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Roberto E. Escobar
 */
public interface IPropertyStoreBasedControl {

   public Control createControl(Composite parent);

   public void save(IPropertyStore propertyStore);

   public void load(IPropertyStore propertyStore);

   public boolean areSettingsValid();

   public String getErrorMessage();

   public int getPriority();
}
