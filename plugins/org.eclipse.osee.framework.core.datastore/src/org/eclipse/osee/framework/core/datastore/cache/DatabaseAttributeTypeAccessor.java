/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.datastore.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.datastore.internal.Activator;
import org.eclipse.osee.framework.core.enums.StorageState;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.cache.IOseeCache;
import org.eclipse.osee.framework.core.model.cache.OseeEnumTypeCache;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.eclipse.osee.framework.core.model.type.AttributeTypeFactory;
import org.eclipse.osee.framework.core.model.type.OseeEnumType;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.database.core.SQL3DataType;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Roberto E. Escobar
 */
public class DatabaseAttributeTypeAccessor extends AbstractDatabaseAccessor<AttributeType> {

   private static final String SELECT_ATTRIBUTE_TYPES =
      "SELECT * FROM osee_attribute_type aty1, osee_attribute_base_type aby1, osee_attribute_provider_type apy1 WHERE aty1.attr_base_type_id = aby1.attr_base_type_id AND aty1.attr_provider_type_id = apy1.attr_provider_type_id";
   private static final String INSERT_ATTRIBUTE_TYPE =
      "INSERT INTO osee_attribute_type (attr_type_id, attr_type_guid, attr_base_type_id, attr_provider_type_id, file_type_extension, name, default_value, enum_type_id, min_occurence, max_occurence, tip_text, tagger_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
   private static final String UPDATE_ATTRIBUTE_TYPE =
      "update osee_attribute_type SET attr_base_type_id=?, attr_provider_type_id=?, file_type_extension=?, name=?, default_value=?, enum_type_id=?, min_occurence=?, max_occurence=?, tip_text=?, tagger_id=? where attr_type_id = ?";

   private static final String INSERT_BASE_ATTRIBUTE_TYPE =
      "INSERT INTO osee_attribute_base_type (attr_base_type_id, attribute_class) VALUES (?, ?)";
   private static final String INSERT_ATTRIBUTE_PROVIDER_TYPE =
      "INSERT INTO osee_attribute_provider_type (attr_provider_type_id, attribute_provider_class) VALUES (?, ?)";
   private static final String SELECT_ATTRIBUTE_BASE_TYPE =
      "SELECT attr_base_type_id FROM osee_attribute_base_type WHERE attribute_class = ?";
   private static final String SELECT_ATTRIBUTE_PROVIDER_TYPE =
      "SELECT attr_provider_type_id FROM osee_attribute_provider_type WHERE attribute_provider_class = ?";

   private final OseeEnumTypeCache enumCache;
   private final AttributeTypeFactory attributeTypeFactory;

   public DatabaseAttributeTypeAccessor(IOseeDatabaseService databaseService, OseeEnumTypeCache enumCache, AttributeTypeFactory attributeTypeFactory) {
      super(databaseService);
      this.enumCache = enumCache;
      this.attributeTypeFactory = attributeTypeFactory;
   }

   @Override
   public void load(IOseeCache<AttributeType> cache) throws OseeCoreException {
      enumCache.ensurePopulated();

      IOseeStatement chStmt = getDatabaseService().getStatement();

      try {
         chStmt.runPreparedQuery(SELECT_ATTRIBUTE_TYPES);

         while (chStmt.next()) {
            int attributeTypeId = chStmt.getInt("attr_type_id");
            String baseAttributeTypeId = Strings.intern(chStmt.getString("attribute_class"));
            String attributeProviderNameId = Strings.intern(chStmt.getString("attribute_provider_class"));
            try {
               String guid = chStmt.getString("attr_type_guid");
               String typeName = chStmt.getString("name");
               String fileTypeExtension = Strings.intern(chStmt.getString("file_type_extension"));
               String defaultValue = chStmt.getString("default_value");
               int minOccurrences = chStmt.getInt("min_occurence");
               int maxOccurrences = chStmt.getInt("max_occurence");
               String description = chStmt.getString("tip_text");
               String taggerId = Strings.intern(chStmt.getString("tagger_id"));

               int enumTypeId = chStmt.getInt("enum_type_id");
               OseeEnumType oseeEnumType = enumCache.getById(enumTypeId);

               AttributeType attributeType =
                  attributeTypeFactory.createOrUpdate(cache, attributeTypeId, StorageState.LOADED, guid, typeName,
                     baseAttributeTypeId, attributeProviderNameId, fileTypeExtension, defaultValue, oseeEnumType,
                     minOccurrences, maxOccurrences, description, taggerId);

               attributeType.clearDirty();
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      } finally {
         chStmt.close();
      }
   }

   @Override
   public void store(Collection<AttributeType> types) throws OseeCoreException {
      List<Object[]> insertData = new ArrayList<Object[]>();
      List<Object[]> updateData = new ArrayList<Object[]>();
      for (AttributeType type : types) {
         if (type.isDirty()) {
            switch (type.getStorageState()) {
               case CREATED:
                  type.setId(getSequence().getNextAttributeTypeId());
                  insertData.add(toInsertValues(type));
                  break;
               case MODIFIED:
                  updateData.add(toUpdateValues(type));
                  break;
               default:
                  break;
            }
         }
      }
      getDatabaseService().runBatchUpdate(INSERT_ATTRIBUTE_TYPE, insertData);
      getDatabaseService().runBatchUpdate(UPDATE_ATTRIBUTE_TYPE, updateData);
      for (AttributeType type : types) {
         type.clearDirty();
      }
   }

   private Object[] toInsertValues(AttributeType type) throws OseeCoreException {
      int attrBaseTypeId = getOrCreateAttributeBaseType(type.getBaseAttributeTypeId());
      int attrProviderTypeId = getOrCreateAttributeProviderType(type.getAttributeProviderId());
      return new Object[] {
         type.getId(),
         type.getGuid(),
         attrBaseTypeId,
         attrProviderTypeId,
         type.getFileTypeExtension() == null ? SQL3DataType.VARCHAR : type.getFileTypeExtension(),
         type.getName() == null ? SQL3DataType.VARCHAR : type.getName(),
         type.getDefaultValue() == null ? SQL3DataType.VARCHAR : type.getDefaultValue(),
         type.getOseeEnumTypeId(),
         type.getMinOccurrences(),
         type.getMaxOccurrences(),
         type.getDescription() == null ? SQL3DataType.VARCHAR : type.getDescription(),
         type.getTaggerId() == null ? SQL3DataType.VARCHAR : type.getTaggerId()};
   }

   private Object[] toUpdateValues(AttributeType type) throws OseeCoreException {
      int attrBaseTypeId = getOrCreateAttributeBaseType(type.getBaseAttributeTypeId());
      int attrProviderTypeId = getOrCreateAttributeProviderType(type.getAttributeProviderId());
      return new Object[] {
         attrBaseTypeId,
         attrProviderTypeId,
         type.getFileTypeExtension() == null ? SQL3DataType.VARCHAR : type.getFileTypeExtension(),
         type.getName() == null ? SQL3DataType.VARCHAR : type.getName(),
         type.getDefaultValue() == null ? SQL3DataType.VARCHAR : type.getDefaultValue(),
         type.getOseeEnumTypeId(),
         type.getMinOccurrences(),
         type.getMaxOccurrences(),
         type.getDescription() == null ? SQL3DataType.VARCHAR : type.getDescription(),
         type.getTaggerId() == null ? SQL3DataType.VARCHAR : type.getTaggerId(),
         type.getId()};
   }

   @SuppressWarnings("unchecked")
   private int getOrCreateAttributeProviderType(String attrProviderExtension) throws OseeCoreException {
      int attrBaseTypeId = -1;
      IOseeStatement chStmt = getDatabaseService().getStatement();
      try {
         chStmt.runPreparedQuery(SELECT_ATTRIBUTE_PROVIDER_TYPE, attrProviderExtension);
         if (chStmt.next()) {
            attrBaseTypeId = chStmt.getInt("attr_provider_type_id");
         } else {
            attrBaseTypeId = getSequence().getNextAttributeProviderTypeId();
            getDatabaseService().runPreparedUpdate(INSERT_ATTRIBUTE_PROVIDER_TYPE, attrBaseTypeId,
               attrProviderExtension);
         }
      } finally {
         chStmt.close();
      }
      return attrBaseTypeId;
   }

   @SuppressWarnings("unchecked")
   private int getOrCreateAttributeBaseType(String attrBaseExtension) throws OseeCoreException {
      int attrBaseTypeId = -1;
      IOseeStatement chStmt = getDatabaseService().getStatement();
      try {
         chStmt.runPreparedQuery(SELECT_ATTRIBUTE_BASE_TYPE, attrBaseExtension);
         if (chStmt.next()) {
            attrBaseTypeId = chStmt.getInt("attr_base_type_id");
         } else {
            attrBaseTypeId = getSequence().getNextAttributeBaseTypeId();
            getDatabaseService().runPreparedUpdate(INSERT_BASE_ATTRIBUTE_TYPE, attrBaseTypeId, attrBaseExtension);
         }
      } finally {
         chStmt.close();
      }
      return attrBaseTypeId;
   }
}
