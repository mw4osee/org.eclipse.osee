/*
 * Created on Apr 24, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.db.connection;

import org.eclipse.osee.framework.db.connection.info.DbInformation;

/**
 * @author Andrew M. Finkbeiner
 */
public interface IDbConnectionInformation {

   public DbInformation getDatabaseInfo(String servicesId);

   public DbInformation getDefaultDatabaseInfo();
}
