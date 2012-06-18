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
package org.eclipse.osee.orcs.db.internal.loader;

import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.cache.AttributeTypeCache;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.core.ds.DataProxy;
import org.eclipse.osee.orcs.core.ds.DataProxyFactory;

/**
 * @author Roberto E. Escobar
 */
public class AttributeDataProxyFactory implements ProxyDataFactory {

   private final DataProxyFactoryProvider proxyProvider;
   private final AttributeTypeCache attributeTypeCache;

   public AttributeDataProxyFactory(DataProxyFactoryProvider proxyProvider, AttributeTypeCache attributeTypeCache) {
      super();
      this.proxyProvider = proxyProvider;
      this.attributeTypeCache = attributeTypeCache;
   }

   @Override
   public DataProxy createProxy(long typeUuid, String value, String uri) throws OseeCoreException {
      return createProxy(typeUuid, value, uri);
   }

   private boolean isEnumOrBoolean(AttributeType attributeType) {
      boolean isEnumAttribute = attributeType.isEnumerated();

      String baseType = attributeType.getBaseAttributeTypeId();
      boolean isBooleanAttribute = baseType.toLowerCase().contains("boolean");

      return isBooleanAttribute || isEnumAttribute;
   }

   @Override
   public DataProxy createProxy(long typeUuid, Object... data) throws OseeCoreException {
      AttributeType attributeType = attributeTypeCache.getByGuid(typeUuid);
      Conditions.checkNotNull(attributeType, "AttributeType", "Unable to find attributeType for [%s]", typeUuid);

      String dataProxyFactoryId = attributeType.getAttributeProviderId();
      if (dataProxyFactoryId.contains(".")) {
         dataProxyFactoryId = Lib.getExtension(dataProxyFactoryId);
      }

      DataProxyFactory factory = proxyProvider.getFactory(dataProxyFactoryId);
      Conditions.checkNotNull(factory, "DataProxyFactory", "Unable to find data proxy factory for [%s]",
         dataProxyFactoryId);

      String shortValue = String.valueOf(data[0]);
      String uri = String.valueOf(data[1]);

      if (isEnumOrBoolean(attributeType)) {
         shortValue = Strings.intern(shortValue);
      }

      DataProxy proxy = factory.createInstance(dataProxyFactoryId);
      proxy.setData(shortValue, uri);
      return proxy;
   }
}
