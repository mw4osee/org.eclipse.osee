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
package org.eclipse.osee.orcs.account.admin.internal;

import java.util.Date;
import org.eclipse.osee.account.admin.Account;
import org.eclipse.osee.account.admin.AccountPreferences;
import org.eclipse.osee.account.admin.AccountSession;
import org.eclipse.osee.account.admin.SubscriptionGroup;
import org.eclipse.osee.account.rest.model.AccountWebPreferences;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.framework.jdk.core.type.ResultSetTransform.Function;
import org.eclipse.osee.framework.jdk.core.type.ResultSets;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Roberto E. Escobar
 */
public class AccountFactory {

   private final Function<String, ArtifactReadable, AccountPreferences> function2 = new ArtifactToAccountPreferences();

   public ResultSet<Account> newAccountResultSet(ResultSet<ArtifactReadable> results) {
      return ResultSets.transform(results, this::newAccount);
   }

   public ResultSet<AccountPreferences> newAccountPreferencesResultSet(ResultSet<ArtifactReadable> results) {
      return ResultSets.transform(results, function2);
   }

   private Account newAccount(ArtifactReadable account) {
      AccountPreferences preferences = new AccountPreferencesArtifact(account.getGuid(), account);
      AccountWebPreferences webPreferences = newAccountWebPreferences(account);
      return new AccountArtifact(account.getGuid(), account, preferences, webPreferences);
   }

   public AccountPreferences newAccountPreferencesResultSet(ArtifactReadable account) {
      return new AccountPreferencesArtifact(account.getGuid(), account);
   }

   public AccountPreferences newAccountPreferences(ArtifactReadable artifact) {
      String id = artifact.getGuid();
      return new AccountPreferencesArtifact(id, artifact);
   }

   public AccountWebPreferences newAccountWebPreferences(ArtifactReadable artifact) {
      String webPreferencesJson = artifact.getSoleAttributeAsString(CoreAttributeTypes.WebPreferences, "{}");
      return new AccountWebPreferences(webPreferencesJson, artifact.getName());
   }

   private class ArtifactToAccountPreferences implements Function<String, ArtifactReadable, AccountPreferences> {

      @Override
      public AccountPreferences apply(ArtifactReadable source) {
         return newAccountPreferences(source);
      }
   }

   public AccountSession newAccountSession(ArtifactId accountId, String sessionToken, String accessedFrom, String accessDetails) {
      Date currentDate = new Date();
      return newAccountSession(accountId, sessionToken, currentDate, currentDate, accessedFrom, accessDetails);
   }

   public AccountSession newAccountSession(ArtifactId accountId, String sessionToken, Date createdOn, Date lastAccessedOn, String accessedFrom, String accessDetails) {
      AccountSessionImpl session = new AccountSessionImpl();
      session.setAccountId(accountId.getUuid());
      session.setSessionToken(sessionToken);
      session.setCreatedOn(createdOn);
      session.setLastAccessedOn(lastAccessedOn);
      session.setAccessDetails(accessDetails);
      session.setAccessedFrom(accessedFrom);
      return session;
   }

   private SubscriptionGroup newAccountSubscriptionGroup(ArtifactReadable source) {
      return new AccountSubscriptionGroupImpl(source);
   }

   public ResultSet<SubscriptionGroup> newAccountSubscriptionGroupResultSet(ResultSet<ArtifactReadable> results) {
      return ResultSets.transform(results, this::newAccountSubscriptionGroup);
   }
}