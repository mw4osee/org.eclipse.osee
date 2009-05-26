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
package org.eclipse.osee.framework.plugin.core.util;

import java.util.HashMap;
import org.eclipse.osee.framework.plugin.core.PluginCoreActivator;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * @author Ken J. Aguilar
 */
public class ExportClassLoader extends ClassLoader {

   private static ExportClassLoader exportClassloaderInstance;
   private final PackageAdmin packageAdmin;
   private final HashMap<String, Bundle> cache = new HashMap<String, Bundle>(1024);

   public static ExportClassLoader getInstance(){
      if(exportClassloaderInstance == null){
         exportClassloaderInstance = new ExportClassLoader();
      }
      return exportClassloaderInstance;
   }

   public ExportClassLoader(PackageAdmin packageAdmin) {
      super(ExportClassLoader.class.getClassLoader());
      this.packageAdmin = packageAdmin;
   }

   public ExportClassLoader()
   {
      this(PluginCoreActivator.getInstance().getPackageAdmin());
   }

   /* (non-Javadoc)
    * @see java.lang.ClassLoader#findClass(java.lang.String)
    */
   @Override
   protected Class<?> findClass(String name) throws ClassNotFoundException {
      try {
         Bundle bundle = getExportingBundle(name);
         if (bundle != null) {
            return bundle.loadClass(name);
         }
         throw new ClassNotFoundException("could not locate a class for " + name);
      } catch (Exception e) {
         throw new ClassNotFoundException("could not locate a class for " + name, e);
      }
   }

   public Bundle getExportingBundle(String name) {
      final String pkg = name.substring(0, name.lastIndexOf('.'));
      Bundle cachedBundle = cache.get(pkg);
      if (cachedBundle != null && cachedBundle.getState() != Bundle.UNINSTALLED) {
         return cachedBundle;
      }
      ExportedPackage[] list = packageAdmin.getExportedPackages(pkg);
      if (list != null) {
         for (ExportedPackage ep : list) {
            final Bundle bundle = ep.getExportingBundle();
            final int state = bundle.getState();
            if (state == Bundle.RESOLVED || state == Bundle.STARTING
                  || state == Bundle.ACTIVE || state == Bundle.STOPPING) {
               cache.put(pkg, bundle);
               return bundle;
            }
         }
      }
      return null;
   }

}
