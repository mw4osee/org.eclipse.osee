/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.account.rest.internal;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.osee.account.rest.model.AccountSessionDetailsData;
import org.eclipse.osee.framework.jdk.core.type.SystemRoles;

/**
 * @author Roberto E. Escobar
 */
public class AccountSessionsResource {

   private final AccountOps accountOps;
   private final String accountId;

   public AccountSessionsResource(AccountOps accountOps, String accountId) {
      this.accountOps = accountOps;
      this.accountId = accountId;
   }

   /**
    * Get all sessions for this account
    * 
    * @return account sessions
    */
   @GET
   @RolesAllowed(SystemRoles.ROLES_AUTHENTICATED)
   @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
   public AccountSessionDetailsData[] getAccountSessions() {
      return accountOps.getAccountSessionById(accountId).toArray(new AccountSessionDetailsData[] {});
   }

}
