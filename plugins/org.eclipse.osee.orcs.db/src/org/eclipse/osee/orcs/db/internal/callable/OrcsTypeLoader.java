/*********************************************************************
 * Copyright (c) 2013 Boeing
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

package org.eclipse.osee.orcs.db.internal.callable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedHashSet;
import org.eclipse.osee.framework.core.data.OrcsTypesData;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.CoreTupleTypes;
import org.eclipse.osee.framework.core.enums.TxCurrent;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.resource.management.IResource;
import org.eclipse.osee.framework.resource.management.IResourceLocator;
import org.eclipse.osee.framework.resource.management.IResourceManager;
import org.eclipse.osee.framework.resource.management.StandardOptions;
import org.eclipse.osee.jdbc.JdbcClient;
import org.eclipse.osee.orcs.OrcsTypes;

/**
 * @author Roberto E. Escobar
 */
public class OrcsTypeLoader {

   private final IResourceManager resourceManager;
   private final JdbcClient jdbcClient;

   public OrcsTypeLoader(JdbcClient jdbcClient, IResourceManager resourceManager) {
      this.resourceManager = resourceManager;
      this.jdbcClient = jdbcClient;
   }

   public IResource load() {
      try {
         Collection<String> uriPaths = new LinkedHashSet<>();
         URI uri = getUriPaths(uriPaths);
         Conditions.checkExpressionFailOnTrue(uriPaths.isEmpty(), "No orcs types found");
         return new OrcsTypesResource(uri, uriPaths);
      } catch (URISyntaxException ex) {
         throw OseeCoreException.wrap(ex);
      }
   }

   /**
    * @return false if types resources don't exist or are not in file-system
    */
   public boolean isTypesResourcesValid() {
      boolean valid = true;
      try {
         Collection<String> uriPaths = new LinkedHashSet<>();
         getUriPaths(uriPaths);
         if (uriPaths.isEmpty()) {
            return false;
         }

         for (String path : uriPaths) {
            PropertyStore options = new PropertyStore();
            IResourceLocator locator = resourceManager.getResourceLocator(path);
            IResource resource = resourceManager.acquire(locator, options);
            if (resource == null) {
               valid = false;
               break;
            }
         }
      } catch (URISyntaxException ex) {
         throw OseeCoreException.wrap(ex);
      }
      return valid;
   }

   private URI getUriPaths(Collection<String> uriPaths) throws URISyntaxException {
      String resourceUri = String.format("osee:/datastore.orcs.types_%s.osee", Lib.getDateTimeString());
      URI uri = new URI(resourceUri);

      jdbcClient.runQuery(stmt -> uriPaths.add(stmt.getString("uri")), OrcsTypes.LOAD_OSEE_TYPE_DEF_URIS,
         CoreTupleTypes.OseeTypeDef, CoreBranches.COMMON, TxCurrent.CURRENT, OrcsTypesData.OSEE_TYPE_VERSION,
         TxCurrent.CURRENT);
      return uri;
   }

   private final class OrcsTypesResource implements IResource {
      private final URI uri;
      private final Collection<String> resources;

      public OrcsTypesResource(URI uri, Collection<String> resources) {
         this.uri = uri;
         this.resources = resources;
      }

      @Override
      public InputStream getContent() {
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

      private InputStream asInputStream(Collection<String> resources) {
         PropertyStore options = new PropertyStore();
         options.put(StandardOptions.DecompressOnAquire.name(), "true");
         StringBuilder builder = new StringBuilder();
         for (String path : resources) {
            IResourceLocator locator = resourceManager.getResourceLocator(path);
            IResource resource = resourceManager.acquire(locator, options);
            if (resource == null) {
               throw new OseeStateException("Types resource can not be null for %s", path);
            }

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