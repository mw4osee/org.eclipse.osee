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
package org.eclipse.osee.framework.derby;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import org.eclipse.osee.framework.db.connection.IConnection;

/**
 * @author Roberto E. Escobar
 */
public class EmbeddedDerbyConnection implements IConnection {

   private static final String driver = "org.apache.derby.jdbc.EmbeddedDriver";

   public Connection getConnection(Properties properties, String connectionURL) throws Exception {
      Class.forName(driver);
      Connection connection = DriverManager.getConnection(connectionURL, properties);
      return connection;
   }

   public String getDriver() {
      return driver;
   }

}
