/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
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

package org.eclipse.osee.orcs.core.internal.attribute.primitives;

import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.core.annotations.OseeAttribute;

/**
 * @author Ryan D. Brooks
 */
@OseeAttribute("LongAttribute")
public class LongAttribute extends CharacterBackedAttribute<Long> {
   public static final String NAME = LongAttribute.class.getSimpleName();
   private static final Long DEFAULT_Long = Long.MIN_VALUE;

   public LongAttribute(Long id) {
      super(id);
   }

   @Override
   public Long convertStringToValue(String value) {
      Long toReturn = null;
      if (isValidLong(value)) {
         toReturn = Long.valueOf(value);
      } else {
         toReturn = getDefaultValue();
      }
      return toReturn;
   }

   public Long getDefaultValue() {
      Long toReturn = DEFAULT_Long;
      String defaultValue = getDefaultValueFromMetaData();
      if (isValidLong(defaultValue)) {
         toReturn = Long.valueOf(defaultValue);
      }
      return toReturn;
   }

   private boolean isValidLong(String value) {
      boolean result = false;
      if (Strings.isValid(value)) {
         try {
            Long.parseLong(value);
            result = true;
         } catch (NumberFormatException ex) {
            // Do Nothing;
         }
      }
      return result;
   }
}