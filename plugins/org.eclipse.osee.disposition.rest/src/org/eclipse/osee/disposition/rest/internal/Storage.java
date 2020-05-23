/*********************************************************************
 * Copyright (c) 2013 Boeing
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

package org.eclipse.osee.disposition.rest.internal;

import org.eclipse.osee.framework.resource.management.IResource;

/**
 * @author Angel Avila
 */
public interface Storage extends DispoQuery, DispoWriter {

   boolean typesExist();

   void storeTypes(IResource resource);

}