/*********************************************************************
 * Copyright (c) 2019 Boeing
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

package org.eclipse.osee.ats.api.demo.enums.token;

import org.eclipse.osee.ats.api.demo.enums.token.CodeCategoryAttributeType.CodeCategoryEnum;
import org.eclipse.osee.framework.core.data.AttributeTypeEnum;
import org.eclipse.osee.framework.core.data.NamespaceToken;
import org.eclipse.osee.framework.core.data.TaggerTypeToken;
import org.eclipse.osee.framework.core.enums.EnumToken;

/**
 * @author Stephen J. Molaro
 */
public class CodeCategoryAttributeType extends AttributeTypeEnum<CodeCategoryEnum> {

   public final CodeCategoryEnum CodeProblem = new CodeCategoryEnum(0, "Code Problem");
   public final CodeCategoryEnum DesignImplementation = new CodeCategoryEnum(1, "Design/Implementation");
   public final CodeCategoryEnum CommentChangeOnly = new CodeCategoryEnum(2, "Comment Change Only");
   public final CodeCategoryEnum NonMission = new CodeCategoryEnum(3, "Non-Mission");
   public final CodeCategoryEnum Workaround = new CodeCategoryEnum(4, "Workaround");

   public CodeCategoryAttributeType(TaggerTypeToken taggerType, String mediaType, NamespaceToken namespace) {
      super(1152921504606847238L, namespace, "demo.code.Category", mediaType, "", taggerType, 5);
   }

   public class CodeCategoryEnum extends EnumToken {
      public CodeCategoryEnum(int ordinal, String name) {
         super(ordinal, name);
         addEnum(this);
      }
   }
}