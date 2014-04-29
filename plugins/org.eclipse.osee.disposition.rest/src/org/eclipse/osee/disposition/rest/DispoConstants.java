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
package org.eclipse.osee.disposition.rest;

import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.orcs.data.ArtifactId;
import org.eclipse.osee.orcs.utility.OrcsUtil;

/**
 * @author Angel Avila
 */
public final class DispoConstants {

   // @formatter:off
   public static final IArtifactType DispoSet = TokenFactory.createArtifactType(0x0000000000000327, "Disposition Set");
   public static final IArtifactType DispoItem = TokenFactory.createArtifactType(0x0000000000000328, "Dispositionable Item");

   public static final IAttributeType ImportPath= TokenFactory.createAttributeType(0x1000000000000389L, "dispo.Import Path");
   public static final IAttributeType ImportState= TokenFactory.createAttributeType(0x3000000000000196L, "dispo.Import State");
   public static final IAttributeType DispoAnnotationsJson = TokenFactory.createAttributeType(0x1000000000000386L, "dispo.Annotations JSON");
   public static final IAttributeType DispoDiscrepanciesJson = TokenFactory.createAttributeType(0x1000000000000387L, "dispo.Discrepancies JSON");
   public static final IAttributeType DispoNotesJson = TokenFactory.createAttributeType(0x1000000000000388L, "dispo.Notes JSON");
   public static final IAttributeType DispoDateCreated = TokenFactory.createAttributeType(0x1000000000000391L, "dispo.Date Created");
   public static final IAttributeType DispoLastUpdated = TokenFactory.createAttributeType(0x1000000000000392L, "dispo.Last Updated");   
   public static final IAttributeType DispoItemStatus = TokenFactory.createAttributeType(0x3000000000000198L, "dispo.Item Status"); 
   public static final IAttributeType DispoItemTotalPoints = TokenFactory.createAttributeType(0x3000000000000203L, "dispo.Total Points"); 
   public static final IAttributeType DispoItemNeedsRerun = TokenFactory.createAttributeType(0x3000000000000204L, "dispo.Needs Rerun"); 
   public static final IAttributeType DispoItemVersion = TokenFactory.createAttributeType(0x3000000000000200L, "dispo.Item Version"); 
   public static final IAttributeType DispoItemAssignee = TokenFactory.createAttributeType(0x3000000000000201L, "dispo.Assignee"); 
   
   public static final ArtifactId DispoTypesArtifact = OrcsUtil.newArtifactId("BEQGMZJDBHPd4OeWg6AA", "DispositionTypes");
   // @formatter:on

   private DispoConstants() {
      // Constants
   }

}
