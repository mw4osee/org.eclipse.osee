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
package org.eclipse.osee.ats.config.demo.config;

import org.eclipse.osee.framework.database.init.DatabaseInitConfiguration;
import org.eclipse.osee.framework.database.init.DefaultOseeTypeDefinitions;
import org.eclipse.osee.framework.database.init.IAddDbInitChoice;
import org.eclipse.osee.framework.database.init.IGroupSelector;

/**
 * * Add the ability to wipe an OSEE database and configure it for the ATS Demo Configuration which will showcase ATS
 * functionality.
 * 
 * @author Donald G. Dunne
 */
public class AddDbInitDemoChoice implements IAddDbInitChoice {

   @Override
   public void addDbInitChoice(IGroupSelector groupSelection) {
      DatabaseInitConfiguration config = new DatabaseInitConfiguration();
      config.addTask("org.eclipse.osee.ats.config.demo.AddCommonBranchForAtsDemo");
      config.addTask("org.eclipse.osee.framework.access.provider.FrameworkAccessConfig");
      config.addTask("org.eclipse.osee.ats.config.demo.AtsConfigAddDemoUsers");
      config.addTask("org.eclipse.osee.framework.database.init.SimpleTemplateProviderTask");
      config.addTask("org.eclipse.osee.ats.AtsDatabaseConfig");
      config.addTask("org.eclipse.osee.ats.config.demo.DemoDatabaseConfig");

      for (DefaultOseeTypeDefinitions types : DefaultOseeTypeDefinitions.values()) {
         config.addOseeType(types);
      }
      config.addOseeType("org.eclipse.osee.ats.config.demo.OseeTypes_Demo");
      groupSelection.addChoice("OSEE Demo Database", config);
   }
}
