/*********************************************************************
 * Copyright (c) 2018 Boeing
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

package org.eclipse.osee.ats.rest.internal.workitem;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.api.workflow.AtsTeamWfEndpointApi;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.core.model.change.ChangeItem;

/**
 * @author Donald G. Dunne
 */
@Path("teamwf")
public class AtsTeamWfEndpointImpl implements AtsTeamWfEndpointApi {

   private final AtsApi services;

   public AtsTeamWfEndpointImpl(AtsApi services) {
      this.services = services;
   }

   @Override
   @GET
   @Path("{id}/changedata")
   @Produces({MediaType.APPLICATION_JSON})
   public List<ChangeItem> getChangeData(@PathParam("id") String id) {
      IAtsWorkItem workItem = services.getWorkItemService().getWorkItemByAnyId(id);
      if (!workItem.isTeamWorkflow()) {
         throw new UnsupportedOperationException();
      }
      IAtsTeamWorkflow teamWf = workItem.getParentTeamWorkflow();
      TransactionToken trans = services.getBranchService().getEarliestTransactionId(teamWf);
      if (trans.isValid()) {
         return services.getBranchService().getChangeData(trans);
      }
      BranchId branch = services.getBranchService().getWorkingBranch(teamWf);
      if (branch.isValid()) {
         return services.getBranchService().getChangeData(branch);
      }
      return Collections.<ChangeItem> emptyList();
   }

   @Override
   @GET
   @Path("{aiId}/version")
   @Produces({MediaType.APPLICATION_JSON})
   public Collection<IAtsVersion> getVersionsbyTeamDefinition(@PathParam("aiId") String aiId) {
      IAtsActionableItem ai = services.getActionableItemService().getActionableItem(aiId);
      IAtsTeamDefinition impactedTeamDef = services.getTeamDefinitionService().getImpactedTeamDef(ai);
      IAtsTeamDefinition teamDefHoldingVersions =
         services.getTeamDefinitionService().getTeamDefinitionHoldingVersions(impactedTeamDef);

      return services.getVersionService().getVersions(teamDefHoldingVersions);
   }
}
