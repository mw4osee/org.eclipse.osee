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

package org.eclipse.osee.framework.skynet.core.linking;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.SkynetActivator;
import org.eclipse.osee.framework.skynet.core.linking.HttpRequest.HttpMethod;

/**
 * @author Roberto E. Escobar
 */
public class HttpRequestHandler implements Runnable {

   private static Map<HttpMethod, IHttpMethod> httpMethodHandlers;
   static {
      httpMethodHandlers = new HashMap<HttpMethod, IHttpMethod>();
      httpMethodHandlers.put(HttpMethod.RESOURCE_GET, HttpResourceRequest.getInstance());
      httpMethodHandlers.put(HttpMethod.GET, new HttpGetMethod());
   }
   private Socket socket;

   public HttpRequestHandler(Socket socket) throws Exception {
      this.socket = socket;
   }

   public void run() {
      try {
         processRequest();
      } catch (Exception ex) {
         OseeLog.log(SkynetActivator.class, Level.SEVERE, "Error Processing Request: ", ex);
      }
   }

   private void processRequest() throws Exception {
      HttpRequest httpRequest = null;
      HttpResponse httpResponse = null;
      try {
         httpRequest = new HttpRequest(socket);
         httpResponse = new HttpResponse(socket);

         try {
            processRequest(httpRequest, httpResponse);
         } catch (Exception ex) {
            httpResponse.outputStandardError(400, "Exception in processing Request ", ex);
         }
      } finally {
         // Close streams and socket.
         if (httpResponse != null && httpResponse.getOutputStream() != null) {
            httpResponse.getOutputStream().close();
         }
         if (httpRequest != null && httpRequest.getInputStream() != null) {
            httpRequest.getInputStream().close();
         }

         if (socket != null && !socket.isClosed()) {
            socket.close();
         }
      }
   }

   private void processRequest(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
      IHttpMethod httpMethod = httpMethodHandlers.get(httpRequest.getMethod());
      if (httpMethod != null) {
         httpMethod.processRequest(httpRequest, httpResponse);
      } else {
         httpResponse.sendResponseHeaders(405, 0);
      }
   }
}
