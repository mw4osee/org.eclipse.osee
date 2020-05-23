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

package org.eclipse.osee.orcs.core.ds;

import org.eclipse.osee.framework.core.executor.HasCancellation;

/**
 * @author Andrew M. Finkbeiner
 */
public interface Loader {

   Loader setOptions(Options sourceOptions);

   void load(HasCancellation cancellation, LoadDataHandler handler);

   void load(LoadDataHandler handler);

}
