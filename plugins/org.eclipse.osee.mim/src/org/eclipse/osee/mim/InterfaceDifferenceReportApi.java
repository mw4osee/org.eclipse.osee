/*********************************************************************
 * Copyright (c) 2022 Boeing
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
package org.eclipse.osee.mim;

import java.util.Map;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.mim.types.MimDifferenceItem;
import org.eclipse.osee.mim.types.MimDifferenceReport;

/**
 * @author Ryan T. Baldwin
 */
public interface InterfaceDifferenceReportApi {

   Map<ArtifactId, MimDifferenceItem> getDifferences(BranchId branch1, BranchId branch2);

   MimDifferenceReport getDifferenceReport(BranchId branch, BranchId compareBranch);

}
