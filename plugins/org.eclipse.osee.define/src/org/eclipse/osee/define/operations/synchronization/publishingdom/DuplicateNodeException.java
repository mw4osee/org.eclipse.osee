/*********************************************************************
 * Copyright (c) 2023 Boeing
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

package org.eclipse.osee.define.operations.synchronization.publishingdom;

import org.eclipse.osee.define.operations.synchronization.identifier.Identifier;
import org.eclipse.osee.framework.jdk.core.util.Message;

/**
 * {@link RuntimeException} which is thrown when an attempt is made to add a {@link Node} to a {@link DocumentMapImpl}
 * when the {@link DocumentMapImpl} already contains an entry with the same {@link Identifier} as the {@link Node}.
 *
 * @author Loren K. Ashley
 */

class DuplicateNodeException extends RuntimeException {

   /**
    * Serialization version identifier
    */

   private static final long serialVersionUID = 1L;

   /**
    * Creates a new {@link RuntimeException} with a message describing the {@link DocumentMapImpl} duplicate entry
    * error.
    *
    * @param nodeIdentifier the duplicate {@link Node} {@link Identifier}.
    */

   DuplicateNodeException(Identifier nodeIdentifier) {
      super(DuplicateNodeException.buildMessage(nodeIdentifier));
   }

   /**
    * Creates a new {@link RuntimeException} with a message describing the {@link DocumentMapImpl} duplicate entry
    * error.
    *
    * @param nodeIdentifier the duplicate {@link Node} {@link Identifier}.
    * @param cause the {@link Throwable} which led to this exception being thrown. This parameter maybe
    * <code>null</code>.
    */

   DuplicateNodeException(Identifier nodeIdentifier, Throwable cause) {
      this(nodeIdentifier);

      this.initCause(cause);
   }

   /**
    * Builds an error message {@link String} describing the exception.
    *
    * @param nodeIdentifier the duplicate {@link Node} {@link Identifier}.
    * @return a {@link String} message describing the exception condition.
    */

   static String buildMessage(Identifier nodeIdentifier) {
      //@formatter:off
      return
         new Message()
                .title( "DocumentMapImpl already contains an entry for the specified Node Identifier." )
                .indentInc()
                .segment( "Node Identifier", nodeIdentifier )
                .toString()
                ;
      //@formatter:on
   }
}

/* EOF */
