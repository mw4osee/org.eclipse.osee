/*******************************************************************************
 * Copyright (c) 2022 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.server.application.internal.operations;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.jdbc.JdbcClient;

/**
 * @author Donald G. Dunne
 */
public class ServerUtils {

   private static final String GET_VALUE_SQL = "Select OSEE_VALUE FROM osee_info where OSEE_KEY = ?";
   public static final String OSEE_HEALTH_SERVERS_KEY = "osee.health.servers";
   public static final String GREEN_DOT = "greenDot.png";
   public static final String RED_DOT = "redDot.png";

   private ServerUtils() {
   }

   public static String getOseeInfoValue(JdbcClient jdbcClient, String key) {
      String toReturn = jdbcClient.fetch("", GET_VALUE_SQL, key);
      return toReturn;
   }

   public static List<String> getServers(JdbcClient jdbcClient) {
      List<String> servers = new ArrayList<>();
      // Retrieve servers from OseeInfo
      String serversStr = ServerUtils.getOseeInfoValue(jdbcClient, OSEE_HEALTH_SERVERS_KEY);
      serversStr = serversStr.replaceAll(" ", "");
      for (String server : serversStr.split(",")) {
         servers.add(server);
      }
      return servers;
   }

   public static String getImage(String imageName, String url) {
      return String.format("<a href=\"%s\" target=_blank><img src=\"/server/status/images/%s\"></img></a>", url,
         imageName);
   }

}
