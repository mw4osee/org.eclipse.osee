/*********************************************************************
 * Copyright (c) 2015 Boeing
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

package org.eclipse.osee.ats.api.program;

import org.eclipse.osee.ats.api.config.JaxNewAtsConfigObject;

/**
 * @author Donald G. Dunne
 */
public class JaxProgram extends JaxNewAtsConfigObject {

   long countryId;

   public long getCountryId() {
      return countryId;
   }

   public void setCountryId(long countryId) {
      this.countryId = countryId;
   }
}
