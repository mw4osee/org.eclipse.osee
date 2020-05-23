/*********************************************************************
 * Copyright (c) 2020 Boeing
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

package org.eclipse.osee.mbse.cameo;

import com.nomagic.magicdraw.hyperlinks.HyperlinksHandlersRegistry;
import com.nomagic.magicdraw.plugins.Plugin;

/**
 * @author David W. Miller
 */
public class OSEEHyperlinkPlugin extends Plugin {
   @Override
   public void init() {
      HyperlinksHandlersRegistry.addHandler(new OSEEHyperlinkHandler());
   }

   @Override
   public boolean close() {
      return true;
   }

   @Override
   public boolean isSupported() {
      return true;
   }
}
