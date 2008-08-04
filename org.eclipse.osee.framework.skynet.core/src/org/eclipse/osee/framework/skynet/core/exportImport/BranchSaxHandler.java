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
package org.eclipse.osee.framework.skynet.core.exportImport;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.osee.framework.jdk.core.util.io.xml.AbstractSaxHandler;
import org.eclipse.osee.framework.plugin.core.config.ConfigUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Robert A. Fisher
 */
public abstract class BranchSaxHandler extends AbstractSaxHandler {
   private static final Logger logger = ConfigUtil.getConfigFactory().getLogger(BranchSaxHandler.class);
   private static final String ARTIFACT = "artifact";
   private static final String ATTRIBUTE = "attribute";
   private static final String BRANCH = "branch";
   private static final String COMMENT = "comment";
   private static final String BINARY_DATA = "binarydata";
   private static final String LINK = "link";
   private static final String NAME = "name";
   private static final String RATIONALE = "rationale";
   private static final String STRING_VALUE = "stringvalue";
   private static final String TRANSACTION = "transaction";

   public BranchSaxHandler() {
      super();
   }

   @Override
   public void startElementFound(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      try {
         if (localName.equalsIgnoreCase(BRANCH)) {
            handleBranch(attributes);
         } else if (localName.equalsIgnoreCase(NAME)) {
            handleName(attributes);
         } else if (localName.equalsIgnoreCase(TRANSACTION)) {
            handleTransaction(attributes);
         } else if (localName.equalsIgnoreCase(COMMENT)) {
            handleComment(attributes);
         } else if (localName.equalsIgnoreCase(ARTIFACT)) {
            handleArtifact(attributes);
         } else if (localName.equalsIgnoreCase(ATTRIBUTE)) {
            handleAttribute(attributes);
         } else if (localName.equalsIgnoreCase(STRING_VALUE)) {
            handleStringValue(attributes);
         } else if (localName.equalsIgnoreCase(BINARY_DATA)) {
            handleBinaryData(attributes);
         } else if (localName.equalsIgnoreCase(LINK)) {
            handleLink(attributes);
         } else if (localName.equalsIgnoreCase(RATIONALE)) {
            handleRationale(attributes);
         }
      } catch (Exception ex) {
         throw new IllegalStateException(ex);
      }
   }

   @Override
   public void endElementFound(String uri, String localName, String qName) throws SAXException {
      try {
         if (localName.equalsIgnoreCase(BRANCH)) {
            finishBranch();
         } else if (localName.equalsIgnoreCase(NAME)) {
            finishName();
         } else if (localName.equalsIgnoreCase(TRANSACTION)) {
            finishTransaction();
         } else if (localName.equalsIgnoreCase(COMMENT)) {
            finishComment();
         } else if (localName.equalsIgnoreCase(ARTIFACT)) {
            finishArtifact();
         } else if (localName.equalsIgnoreCase(ATTRIBUTE)) {
            finishAttribute();
         } else if (localName.equalsIgnoreCase(STRING_VALUE)) {
            finishStringValue();
         } else if (localName.equalsIgnoreCase(BINARY_DATA)) {
            finishBinaryData();
         } else if (localName.equalsIgnoreCase(LINK)) {
            finishLink();
         } else if (localName.equalsIgnoreCase(RATIONALE)) {
            finishRationale();
         }
      } catch (Exception ex) {
         throw new IllegalStateException(ex);
      }
   }

   private Timestamp currentBranchTime = null;
   private String currentBranchAssociatedArtGuid = null;

   /**
    * @param attributes
    * @throws SQLException
    * @throws IOException
    * @throws UnsupportedEncodingException
    */
   private void handleBranch(Attributes attributes) throws SQLException, UnsupportedEncodingException, IOException {
      currentBranchTime = Timestamp.valueOf(attributes.getValue("time"));
      currentBranchAssociatedArtGuid = attributes.getValue("associated_guid");
   }

   private void finishBranch() {
      processBranchDone();

      currentBranchTime = null;
      currentBranchAssociatedArtGuid = null;
   }

   protected void processBranchDone() {
   }

   private void handleName(Attributes attributes) {
      // No attributes, just content -- all handling in the finish method
   }

   private void finishName() throws Exception {
      final String name = getContents();

      processBranch(name, currentBranchTime, currentBranchAssociatedArtGuid);
   }

   protected abstract void processBranch(String name, Timestamp time, String associatedArtGuid) throws Exception;

   private String currentTransactionAuthorGuid = null;
   private Timestamp currentTransactionTime = null;
   private String currentCommitArtGuid = null;

   /**
    * @param attributes
    * @throws SQLException
    * @throws IOException
    * @throws UnsupportedEncodingException
    */
   private void handleTransaction(Attributes attributes) throws SQLException, UnsupportedEncodingException, IOException {
      currentTransactionAuthorGuid = attributes.getValue("author");
      currentTransactionTime = Timestamp.valueOf(attributes.getValue("time"));
      currentCommitArtGuid = attributes.getValue("commitArtGuid");
   }

   private void finishTransaction() {
      processTransactionDone();
   }

   protected void processTransactionDone() {
   }

   private void handleComment(Attributes attributes) {
      // No attributes, just content -- all handling in the finish method
   }

   private void finishComment() throws Exception {
      String comment = getContents();

      wrapUpTransaction(comment);
   }

   private void wrapUpTransaction(String comment) throws Exception {
      processTransaction(currentTransactionAuthorGuid, currentTransactionTime, comment, currentCommitArtGuid);

      currentTransactionAuthorGuid = null;
      currentTransactionTime = null;
      currentCommitArtGuid = null;
   }

   protected abstract void processTransaction(String author, Timestamp time, String comment, String commitArtId) throws Exception;

   /**
    * @param attributes
    * @throws SQLException
    */
   private void handleArtifact(Attributes attributes) throws Exception {
      // If there is a currentTransaction out there, then it had no comment and needs to be processed first
      if (currentTransactionAuthorGuid != null && currentTransactionTime != null) {
         wrapUpTransaction("");
      }

      final String guid = attributes.getValue("guid");
      final String type = attributes.getValue("type");
      currentArtifactHrid = attributes.getValue("hrid");
      String deletedStr = attributes.getValue("deleted");
      final boolean deleted = deletedStr == null ? false : Boolean.valueOf(deletedStr);

      processArtifact(guid, type, currentArtifactHrid, deleted);
   }

   protected abstract void processArtifact(String guid, String type, String hrid, boolean deleted) throws Exception;

   private void finishArtifact() {
      processArtifactDone();
   }

   protected void processArtifactDone() {
      currentArtifactHrid = null;
   }

   private String currentAttributeType;
   private String currentAttributeGuid;
   private String currentAttributeStringValue;
   private String currentAttributeContentValue;
   private String currentArtifactHrid;
   private Boolean currentAttributeDeleted;

   /**
    * @param attributes
    * @throws IOException
    * @throws UnsupportedEncodingException
    */
   private void handleAttribute(Attributes attributes) throws UnsupportedEncodingException, IOException {
      currentAttributeType = attributes.getValue("type");
      currentAttributeGuid = attributes.getValue("guid");
      String deletedStr = attributes.getValue("deleted");
      currentAttributeDeleted = deletedStr == null ? Boolean.FALSE : Boolean.valueOf(deletedStr);
      currentAttributeStringValue = "";
      currentAttributeContentValue = "";
   }

   private void handleStringValue(Attributes attribtues) {
      // No attributes, just content -- all handling in the finish method
   }

   private void finishStringValue() throws UnsupportedEncodingException, IOException {
      currentAttributeStringValue = getContents();
   }

   private void handleBinaryData(Attributes attributes) {
      currentAttributeContentValue = attributes.getValue("location");
   }

   private void finishBinaryData() throws IOException {
      // Nothing to do here
   }

   private void finishAttribute() throws Exception {// Skip this attribute if the artifact is not being included
      try {
         processAttribute(currentArtifactHrid, currentAttributeGuid, currentAttributeType, currentAttributeStringValue,
               currentAttributeContentValue, currentAttributeDeleted);
      }// catch Illegal argument exception so import does not fail if attribute type does not exist
      catch (IllegalArgumentException ex) {
         logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }

      currentAttributeType = null;
      currentAttributeGuid = null;
      currentAttributeDeleted = null;
      currentAttributeContentValue = "";
      currentAttributeStringValue = "";
   }

   protected abstract void processAttribute(String artifactHrid, String attributeGuid, String attributeType, String stringValue, String uriValue, boolean deleted) throws Exception;

   private String currentLinkType = null;
   private String currentLinkGuid = null;
   private String currentLinkAGuid = null;
   private String currentLinkBGuid = null;
   private Integer currentLinkAOrder = null;
   private Integer currentLinkBOrder = null;
   private Boolean currentLinkDeleted = null;
   private String currentLinkRationale = null;

   /**
    * @param attributes
    * @throws Exception
    * @throws SQLException
    * @throws IOException
    * @throws UnsupportedEncodingException
    */
   private void handleLink(Attributes attributes) throws Exception {
      // If there is a currentTransaction out there, then it had no comment and needs to be processed first
      if (currentTransactionAuthorGuid != null && currentTransactionTime != null) {
         wrapUpTransaction("");
      }

      currentLinkType = attributes.getValue("type");
      currentLinkGuid = attributes.getValue("guid");
      currentLinkAGuid = attributes.getValue("aguid");
      currentLinkBGuid = attributes.getValue("bguid");
      currentLinkAOrder = Integer.parseInt(attributes.getValue("aorder"));
      currentLinkBOrder = Integer.parseInt(attributes.getValue("border"));
      String deletedStr = attributes.getValue("deleted");
      currentLinkDeleted = deletedStr == null ? false : Boolean.valueOf(deletedStr);

   }

   protected abstract void processLink(String guid, String type, String aguid, String bguid, int aOrder, int bOrder, String rationale, boolean deleted) throws Exception;

   private void finishLink() throws Exception {
      processLink(currentLinkGuid, currentLinkType, currentLinkAGuid, currentLinkBGuid, currentLinkAOrder,
            currentLinkBOrder, currentLinkRationale, currentLinkDeleted);

      currentLinkType = null;
      currentLinkGuid = null;
      currentLinkAGuid = null;
      currentLinkBGuid = null;
      currentLinkAOrder = null;
      currentLinkBOrder = null;
      currentLinkRationale = null;
      currentLinkDeleted = null;
   }

   private void handleRationale(Attributes attributes) {
      // No attributes, just content -- all handling in the finish method
   }

   private void finishRationale() throws Exception {
      currentLinkRationale = getContents();
   }
}
