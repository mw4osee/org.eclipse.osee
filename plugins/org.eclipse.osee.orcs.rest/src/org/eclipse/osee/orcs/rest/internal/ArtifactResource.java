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
package org.eclipse.osee.orcs.rest.internal;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.LoadLevel;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.eclipse.osee.orcs.search.QueryFactory;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactResource {

   @Context
   UriInfo uriInfo;
   @Context
   Request request;

   String branchUuid;
   String artifactUuid;

   public ArtifactResource(UriInfo uriInfo, Request request, String branchUuid, String artifactUuid) {
      this.uriInfo = uriInfo;
      this.request = request;
      this.branchUuid = branchUuid;
      this.artifactUuid = artifactUuid;
   }

   @Path("version")
   public VersionsResource getArtifactVersions() {
      return new VersionsResource(uriInfo, request, branchUuid, artifactUuid);
   }

   @GET
   @Produces(MediaType.TEXT_PLAIN)
   public String getAsText() throws OseeCoreException {
      IOseeBranch branch = TokenFactory.createBranch(branchUuid, "");
      QueryFactory factory = OrcsApplication.getOseeApi().getQueryFactory(null);
      List<ReadableArtifact> arts = factory.fromGuidOrHrid(branch, artifactUuid).build(LoadLevel.SHALLOW).getList();

      StringBuilder builder =
         new StringBuilder(String.format("BranchUuid [%s] ArtifactUuid [%s]\n", branchUuid, artifactUuid));
      for (ReadableArtifact art : arts) {
         for (IAttributeType type : art.getAttributeTypes()) {
            builder.append(art.getAttributes(type).toString());
            builder.append("\n");
         }
      }
      return builder.toString();
   }
}
