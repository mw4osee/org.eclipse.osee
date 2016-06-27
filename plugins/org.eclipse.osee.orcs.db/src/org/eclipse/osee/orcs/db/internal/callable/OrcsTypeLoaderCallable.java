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
package org.eclipse.osee.orcs.db.internal.callable;

import static org.eclipse.osee.framework.core.enums.CoreBranches.COMMON;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashSet;
import org.eclipse.osee.framework.core.data.OrcsTypesData;
import org.eclipse.osee.framework.core.enums.CoreTupleTypes;
import org.eclipse.osee.framework.core.enums.TxChange;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.resource.management.IResource;
import org.eclipse.osee.framework.resource.management.IResourceLocator;
import org.eclipse.osee.framework.resource.management.IResourceManager;
import org.eclipse.osee.framework.resource.management.StandardOptions;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.jdbc.JdbcStatement;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.OrcsSession;

/**
 * @author Roberto E. Escobar
 */
public class OrcsTypeLoaderCallable extends AbstractDatastoreCallable<IResource> {

   /**
    * e2 stores
    */
   private static final String LOAD_OSEE_TYPE_DEF_URIS =
      "select uri from osee_tuple2 t2, osee_txs txs1, osee_attribute attr, osee_txs txs2 where tuple_type = ? and " //
         + "t2.gamma_id = txs1.gamma_id and txs1.branch_id = ? and txs1.tx_current = ? and e1 = ? and e2 = attr.attr_id and " //
         + "attr.gamma_id = txs2.gamma_id and txs2.branch_id = txs1.branch_id and txs2.tx_current = ?";

   private final IResourceManager resourceManager;

   public OrcsTypeLoaderCallable(Log logger, OrcsSession session, JdbcClient jdbcClient, IResourceManager resourceManager) {
      super(logger, session, jdbcClient);
      this.resourceManager = resourceManager;
   }

   @Override
   public IResource call() throws Exception {
      String resourceUri = String.format("osee:/datastore.orcs.types_%s.osee", Lib.getDateTimeString());
      URI uri = new URI(resourceUri);
      Collection<String> uriPaths = findOseeTypeData();

      Conditions.checkExpressionFailOnTrue(uriPaths.isEmpty(), "No orcs types found");
      return new OrcsTypesResource(uri, uriPaths);
   }

   private Collection<String> findOseeTypeData() throws OseeCoreException {
      Collection<String> paths = new LinkedHashSet<>();

      JdbcStatement chStmt = null;
      try {
         chStmt = getJdbcClient().getStatement();

         chStmt.runPreparedQuery(LOAD_OSEE_TYPE_DEF_URIS, CoreTupleTypes.OseeTypeDef, COMMON,
            TxChange.CURRENT.getValue(), OrcsTypesData.OSEE_TYPE_VERSION, TxChange.CURRENT.getValue());

         while (chStmt.next()) {
            String uri = chStmt.getString("uri");
            paths.add(uri);
         }
      } finally {
         Lib.close(chStmt);
      }
      return paths;
   }

   private final class OrcsTypesResource implements IResource {

      private final URI uri;
      private final Collection<String> resources;

      public OrcsTypesResource(URI uri, Collection<String> resources) {
         this.uri = uri;
         this.resources = resources;
      }

      @Override
      public InputStream getContent() throws OseeCoreException {
         return asInputStream(resources);
      }

      @Override
      public URI getLocation() {
         return uri;
      }

      @Override
      public String getName() {
         String value = uri.toASCIIString();
         return value.substring(value.lastIndexOf("/") + 1, value.length());
      }

      @Override
      public boolean isCompressed() {
         return false;
      }

      private InputStream asInputStream(Collection<String> resources) throws OseeCoreException {
         PropertyStore options = new PropertyStore();
         options.put(StandardOptions.DecompressOnAquire.name(), "true");

         StringBuilder builder = new StringBuilder();
         for (String path : resources) {
            IResourceLocator locator = resourceManager.getResourceLocator(path);
            IResource resource = resourceManager.acquire(locator, options);

            InputStream inputStream = null;
            try {
               inputStream = resource.getContent();
               String oseeTypeFragment = Lib.inputStreamToString(inputStream);
               oseeTypeFragment = oseeTypeFragment.replaceAll("import\\s+\"", "// import \"");
               builder.append("\n//////////////     ");
               builder.append(resource.getName());
               builder.append("\n\n");
               builder.append(oseeTypeFragment);
            } catch (IOException ex) {
               OseeCoreException.wrapAndThrow(ex);
            } finally {
               Lib.close(inputStream);
            }
         }
         InputStream toReturn = null;
         try {
            toReturn = Lib.stringToInputStream(builder.toString());
         } catch (UnsupportedEncodingException ex) {
            OseeCoreException.wrapAndThrow(ex);
         }
         return toReturn;
      }
   }

}
