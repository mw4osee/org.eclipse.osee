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
package org.eclipse.osee.framework.core.test.mocks;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.osee.framework.core.cache.ArtifactTypeCache;
import org.eclipse.osee.framework.core.cache.AttributeTypeCache;
import org.eclipse.osee.framework.core.cache.BranchCache;
import org.eclipse.osee.framework.core.cache.OseeEnumTypeCache;
import org.eclipse.osee.framework.core.cache.RelationTypeCache;
import org.eclipse.osee.framework.core.cache.TransactionCache;
import org.eclipse.osee.framework.core.data.ArtifactChangeItem;
import org.eclipse.osee.framework.core.data.CacheUpdateRequest;
import org.eclipse.osee.framework.core.data.DefaultBasicArtifact;
import org.eclipse.osee.framework.core.data.IBasicArtifact;
import org.eclipse.osee.framework.core.data.IOseeType;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.OseeCacheEnum;
import org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes;
import org.eclipse.osee.framework.core.enums.RelationTypeMultiplicity;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.ArtifactType;
import org.eclipse.osee.framework.core.model.ArtifactTypeFactory;
import org.eclipse.osee.framework.core.model.AttributeType;
import org.eclipse.osee.framework.core.model.AttributeTypeFactory;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.BranchFactory;
import org.eclipse.osee.framework.core.model.OseeCachingService;
import org.eclipse.osee.framework.core.model.OseeEnumEntry;
import org.eclipse.osee.framework.core.model.OseeEnumType;
import org.eclipse.osee.framework.core.model.OseeEnumTypeFactory;
import org.eclipse.osee.framework.core.model.OseeModelFactoryService;
import org.eclipse.osee.framework.core.model.RelationType;
import org.eclipse.osee.framework.core.model.RelationTypeFactory;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.model.TransactionRecordFactory;
import org.eclipse.osee.framework.core.services.IOseeCachingService;
import org.eclipse.osee.framework.core.services.IOseeCachingServiceProvider;
import org.eclipse.osee.framework.core.services.IOseeModelFactoryService;
import org.eclipse.osee.framework.core.services.IOseeModelFactoryServiceProvider;
import org.eclipse.osee.framework.jdk.core.util.GUID;

/**
 * @author Roberto E. Escobar
 */
public final class MockDataFactory {

   private MockDataFactory() {
   }

   public static IBasicArtifact<?> createArtifact(int index) {
      return new DefaultBasicArtifact(index * 37, GUID.create(), "user_" + index);
   }

   public static ArtifactChangeItem createArtifactChangeItem() throws OseeArgumentException{
      int artId = (int)Math.random();
      int transactionNumber = (int)Math.random();
      Long gammaIdNumber = Long.valueOf((int)Math.random());
      
      ArtifactChangeItem changeItem = new ArtifactChangeItem(gammaIdNumber, ModificationType.getMod(1), transactionNumber, artId);
      changeItem.getDestinationVersion().setGammaId(11L);
      changeItem.getDestinationVersion().setModType(ModificationType.getMod(1));
      changeItem.getDestinationVersion().setTransactionNumber(1);
      changeItem.getDestinationVersion().setValue("Value");
      return changeItem;
   }
   
   public static AttributeType createAttributeType() throws OseeCoreException {
      OseeEnumTypeFactory oseeEnumTypeFactory = new OseeEnumTypeFactory();
      AttributeType attributeType =
            new AttributeType(GUID.create(), "name", "baseType", "providerName", ".xml", "", 1, 1, "description",
                  "tagger");
      attributeType.setOseeEnumType(oseeEnumTypeFactory.createEnumType(GUID.create(), "enum type name"));
      return attributeType;
   }

   public static Branch createBranch(int index) {
      BranchState branchState = BranchState.values()[Math.abs(index % BranchState.values().length)];
      BranchType branchType = BranchType.values()[Math.abs(index % BranchType.values().length)];
      boolean isArchived = index % 2 == 0 ? true : false;
      return new Branch(GUID.create(), "branch_" + index, branchType, branchState, isArchived);
   }

   public static TransactionRecord createTransaction(int index, int branchId) {
      TransactionDetailsType type =
            TransactionDetailsType.values()[Math.abs(index % TransactionDetailsType.values().length)];
      int value = index;
      if (value == 0) {
         value++;
      }
      return new TransactionRecord(value * 47, branchId, "comment_" + value, new Date(), value * 37, value * 42, type);
   }

   public static OseeEnumEntry createEnumEntry(int index) {
      return new OseeEnumEntry(GUID.create(), "entry_" + index, Math.abs(index * 37));
   }

   public static OseeEnumType createEnumType(int index) {
      return new OseeEnumType(GUID.create(), "enum_" + index);
   }

   public static AttributeType createAttributeType(int index, OseeEnumType oseeEnumType) throws OseeCoreException {
      AttributeType type =
            new AttributeType(GUID.create(), "attrType_" + index, "baseClass_" + index, "providerId_" + index,
                  "ext_" + index, "default_" + index, index * 2, index * 7, "description_" + index, "tag_" + index);
      type.setOseeEnumType(oseeEnumType);
      return type;
   }

   public static ArtifactType createArtifactType(int index) {
      return new ArtifactType(GUID.create(), "art_" + index, index % 2 == 0);
   }

   public static ArtifactType createBaseArtifactType() {
      IOseeType baseType = CoreArtifactTypes.Artifact;
      return new ArtifactType(baseType.getGuid(), baseType.getName(), true);
   }

   public static RelationType createRelationType(int index, ArtifactType artTypeA, ArtifactType artTypeB) {
      RelationTypeMultiplicity multiplicity =
            RelationTypeMultiplicity.values()[Math.abs(index % RelationTypeMultiplicity.values().length)];
      String order = RelationOrderBaseTypes.values()[index % RelationTypeMultiplicity.values().length].getGuid();
      return new RelationType(GUID.create(), "relType_" + index, "sideA_" + index, "sideB_" + index, artTypeA,
            artTypeB, multiplicity, order);
   }

   public static CacheUpdateRequest createRequest(int index) {
      OseeCacheEnum cacheEnum = OseeCacheEnum.values()[Math.abs(index % OseeCacheEnum.values().length)];
      List<String> guids = new ArrayList<String>();
      for (int j = 1; j <= index * 3; j++) {
         guids.add(GUID.create());
      }
      return new CacheUpdateRequest(cacheEnum, guids);
   }

   public static IOseeModelFactoryService createFactoryService() {
      return new OseeModelFactoryService(new BranchFactory(), new TransactionRecordFactory(),
            new ArtifactTypeFactory(), new AttributeTypeFactory(), new RelationTypeFactory(), new OseeEnumTypeFactory());
   }

   public static IOseeModelFactoryServiceProvider createFactoryProvider() {
      return new MockOseeModelFactoryServiceProvider(createFactoryService());
   }

   public static IOseeCachingServiceProvider createCachingProvider() {
      BranchCache brCache = new BranchCache(new MockOseeDataAccessor<Branch>());
      TransactionCache txCache = new TransactionCache(new MockOseeTransactionDataAccessor());
      ArtifactTypeCache artCache = new ArtifactTypeCache(new MockOseeDataAccessor<ArtifactType>());
      AttributeTypeCache attrCache = new AttributeTypeCache(new MockOseeDataAccessor<AttributeType>());
      RelationTypeCache relCache = new RelationTypeCache(new MockOseeDataAccessor<RelationType>());
      OseeEnumTypeCache enumCache = new OseeEnumTypeCache(new MockOseeDataAccessor<OseeEnumType>());

      IOseeCachingService service = new OseeCachingService(brCache, txCache, artCache, attrCache, relCache, enumCache);
      return new MockOseeCachingServiceProvider(service);
   }
}
