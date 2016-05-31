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
package org.eclipse.osee.framework.core.data;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.eclipse.osee.framework.jdk.core.type.BaseIdentity;
import org.eclipse.osee.framework.jdk.core.type.Id;
import org.eclipse.osee.framework.jdk.core.type.IdSerializer;
import org.eclipse.osee.framework.jdk.core.type.Identity;

/**
 * @author Ryan D. Brooks
 */
@JsonSerialize(using = IdSerializer.class)
public interface TransactionId extends Identity<Integer> {
   TransactionId SENTINEL = valueOf(Id.SENTINEL);

   @JsonCreator
   public static TransactionId valueOf(long id) {
      final class TransactionToken extends BaseIdentity<Integer> implements TransactionId {
         public TransactionToken(Integer txId) {
            super(txId);
         }
      }
      return new TransactionToken((int) id);
   }

   default boolean isValid() {
      return !isInvalid();
   }

   default boolean isInvalid() {
      return Id.SENTINEL.equals(getGuid().longValue());
   }

   default boolean equals(Long id) {
      return getGuid().equals(id.intValue());
   }

   default boolean isOlderThan(TransactionId other) {
      return getGuid() < other.getGuid();
   }

   default boolean notEqual(TransactionId other) {
      return !equals(other);
   }

   default Integer getId() {
      return getGuid();
   }
}