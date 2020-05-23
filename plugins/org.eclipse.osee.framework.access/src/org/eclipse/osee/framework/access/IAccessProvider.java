/*********************************************************************
 * Copyright (c) 2010 Boeing
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

package org.eclipse.osee.framework.access;

import java.util.Collection;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.model.access.AccessData;
import org.eclipse.osee.framework.lifecycle.LifecycleHandler;

/**
 * @author Roberto E. Escobar
 */
public interface IAccessProvider extends LifecycleHandler {

   void computeAccess(ArtifactToken userArtifact, Collection<?> objToCheck, AccessData accessData);

}
