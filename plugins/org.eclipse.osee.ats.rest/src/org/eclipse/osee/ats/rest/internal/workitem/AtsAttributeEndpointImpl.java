/*********************************************************************
 * Copyright (c) 2017 Boeing
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
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.user.AtsUser;
import org.eclipse.osee.ats.api.workflow.AttributeKey;
import org.eclipse.osee.ats.api.workflow.attr.AtsAttributeEndpointApi;
import org.eclipse.osee.ats.api.workflow.attr.AtsAttributes;
import org.eclipse.osee.framework.core.data.AttributeTypeGeneric;
import org.eclipse.osee.framework.core.data.AttributeTypeToken;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.AttributeTypes;
import org.eclipse.osee.orcs.data.EnumEntry;

/**
 * @author Donald G. Dunne
 */
@Path("attr")
public final class AtsAttributeEndpointImpl implements AtsAttributeEndpointApi {

   private final AtsApi atsApi;
   private final OrcsApi orcsApi;

   @Context
   private HttpHeaders httpHeaders;

   public AtsAttributeEndpointImpl(AtsApi atsApi, OrcsApi orcsApi) {
      this.atsApi = atsApi;
      this.orcsApi = orcsApi;
   }

   @Override
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public AtsAttributes get() {
      AtsAttributes attrs = new AtsAttributes();
      for (AttributeKey key : AttributeKey.values()) {
         attrs.add(key.name(), key.name(),
            key.getUrl().equals("N/A") ? key.getUrl() : System.getProperty("OseeApplicationServer") + key.getUrl());
      }
      for (AttributeTypeGeneric<?> attrType : orcsApi.tokenService().getAttributeTypes()) {
         if (attrType.isEnumerated()) {
            attrs.add(attrType.getIdString(), attrType.getName(),
               System.getProperty("OseeApplicationServer") + "/ats/attr/" + attrType.getIdString() + "/");
         }
      }
      return attrs;
   }

   @Path("{id}")
   @Override
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<String> getValidValues(@PathParam("id") String id) {
      List<String> values = new LinkedList<>();
      if (id.equals(AttributeKey.Assignee.name()) || id.equals(AttributeKey.Originator.name())) {
         Collection<AtsUser> active = atsApi.getUserService().getUsers(Active.Active);
         for (AtsUser user : active) {
            values.add(user.getName());
         }
      } else if (id.equals(AttributeKey.ColorTeam.name())) {
         getEnumValues(values, AtsAttributeTypes.ColorTeam.getId());
      } else if (id.equals(AttributeKey.IPT.name())) {
         getEnumValues(values, AtsAttributeTypes.IPT.getId());
      } else if (id.equals(AttributeKey.Priority.name())) {
         getEnumValues(values, AtsAttributeTypes.Priority.getId());
      } else if (Strings.isNumeric(id)) {
         getEnumValues(values, Long.valueOf(id));
      }
      if (!values.isEmpty()) {
         Collections.sort(values);
      }
      return values;
   }

   private void getEnumValues(List<String> values, Long id) {
      AttributeTypes attrTypes = orcsApi.getOrcsTypes().getAttributeTypes();
      AttributeTypeToken attrType = orcsApi.tokenService().getAttributeType(id);
      if (attrType.isEnumerated()) {
         for (EnumEntry entry : attrTypes.getEnumType(attrType).values()) {
            values.add(entry.getName());
         }
      }
   }

}
