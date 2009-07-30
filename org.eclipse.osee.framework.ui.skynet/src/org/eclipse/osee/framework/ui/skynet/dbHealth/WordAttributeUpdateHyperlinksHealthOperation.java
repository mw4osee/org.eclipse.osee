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
package org.eclipse.osee.framework.ui.skynet.dbHealth;

import java.util.Collection;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.text.change.ChangeSet;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.linking.WordMlLinkHandler;
import org.eclipse.osee.framework.skynet.core.linking.WordMlLinkHandler.MatchRange;

/**
 * @author Roberto E. Escobar
 */
public class WordAttributeUpdateHyperlinksHealthOperation extends AbstractWordAttributeHealthOperation {

   public WordAttributeUpdateHyperlinksHealthOperation() {
      super("Word Attribute Old style hyperlinks");
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.dbHealth.AbstractWordAttributeHealthOperation#applyFix(org.eclipse.osee.framework.ui.skynet.dbHealth.AbstractWordAttributeHealthOperation.AttrData)
    */
   @SuppressWarnings("unchecked")
   @Override
   protected void applyFix(AttrData attrData) throws OseeCoreException {
      Object additionalData = attrData.getAdditionalData();
      if (additionalData instanceof HashCollection) {
         String original = attrData.getResource().getData();
         String converted = convertWordMlLinks(original, (HashCollection<String, MatchRange>) additionalData);
         attrData.getResource().setData(converted);
      } else {
         throw new OseeArgumentException(String.format("AttrData: gamma_id [%s] had invalid AdditionalData object",
               attrData.getGammaId()));
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.dbHealth.AbstractWordAttributeHealthOperation#isFixRequired(org.eclipse.osee.framework.ui.skynet.dbHealth.AbstractWordAttributeHealthOperation.AttrData, org.eclipse.osee.framework.ui.skynet.dbHealth.AbstractWordAttributeHealthOperation.Resource)
    */
   @Override
   protected boolean isFixRequired(AttrData attrData, Resource resource) throws OseeCoreException {
      boolean result = false;
      String content = resource.getData();
      if (Strings.isValid(content)) {
         HashCollection<String, MatchRange> matches = WordMlLinkHandler.parseOseeWordMLLinks(content);
         result = !matches.isEmpty();
         attrData.setAdditionalData(matches);
      }
      return result;
   }

   private String convertWordMlLinks(String original, HashCollection<String, MatchRange> matches) {
      ChangeSet changeSet = new ChangeSet(original);
      for (String guid : matches.keySet()) {
         Collection<MatchRange> matchRanges = matches.getValues(guid);
         if (matchRanges != null) {
            for (MatchRange match : matchRanges) {
               String replaceWith = WordMlLinkHandler.getOseeLinkMarker(guid);
               changeSet.replace(match.start(), match.end(), replaceWith);
            }
         }
      }
      return changeSet.applyChangesToSelf().toString();
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.dbHealth.DatabaseHealthOperation#getDescription()
    */
   @Override
   public String getCheckDescription() {
      return "Checks Word Attribute data to detect old style hyperlinks";
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.dbHealth.DatabaseHealthOperation#getFixDescription()
    */
   @Override
   public String getFixDescription() {
      return "Converts old style hyperlinks to new style";
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.dbHealth.AbstractWordAttributeHealthOperation#getBackUpPrefix()
    */
   @Override
   protected String getBackUpPrefix() {
      return "HyperlinkFix_";
   }
}
