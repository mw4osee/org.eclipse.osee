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
package org.eclipse.osee.coverage.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.coverage.util.CoverageImage;
import org.eclipse.osee.coverage.util.CoverageMetrics;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.GeneralData;
import org.eclipse.osee.framework.skynet.core.artifact.KeyValueArtifact;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.OseeImage;

/**
 * Single code unit (file/procedure/function) that can contain other Coverage Unit or Coverage Items
 * 
 * @author Donald G. Dunne
 */
public class CoverageUnit implements ICoverage, ICoverageUnitProvider, ICoverageItemProvider {

   public static String ARTIFACT_NAME = "Coverage Unit";
   private String name;
   private String namespace;
   private boolean folder;
   private String notes;
   private String assignees;
   private String guid = GUID.create();
   private String text;
   private final List<CoverageItem> coverageItems = new ArrayList<CoverageItem>();
   private String location;
   private final List<CoverageUnit> coverageUnits = new ArrayList<CoverageUnit>();
   private ICoverage parentCoverageEditorItem;
   private Artifact artifact;

   public CoverageUnit(ICoverage parentCoverageEditorItem, String name, String location) {
      super();
      this.parentCoverageEditorItem = parentCoverageEditorItem;
      this.name = name;
      this.location = location;
   }

   public CoverageUnit(Artifact artifact) throws OseeCoreException {
      this.artifact = artifact;
      load();
   }

   @Override
   public Collection<? extends ICoverage> getCoverageEditorItems(boolean recurse) {
      Set<ICoverage> items = new HashSet<ICoverage>(coverageItems);
      for (CoverageUnit coverageUnit : getCoverageUnits()) {
         items.add(coverageUnit);
         if (recurse) {
            items.addAll(coverageUnit.getCoverageEditorItems(recurse));
         }
      }
      return items;
   }

   public void addCoverageUnit(CoverageUnit coverageUnit) {
      coverageUnit.setParentCoverageEditorItem(this);
      coverageUnits.add(coverageUnit);
   }

   public List<CoverageUnit> getCoverageUnits() {
      return coverageUnits;
   }

   public void addCoverageItem(CoverageItem coverageItem) {
      coverageItems.add(coverageItem);
   }

   public List<CoverageItem> getCoverageItems(boolean recurse) {
      if (!recurse) {
         return coverageItems;
      }
      List<CoverageItem> items = new ArrayList<CoverageItem>(coverageItems);
      for (CoverageUnit coverageUnit : coverageUnits) {
         items.addAll(coverageUnit.getCoverageItems(true));
      }
      return items;
   }

   public CoverageItem getCoverageItem(String methodNum, String executionLine) {
      for (CoverageItem coverageItem : getCoverageItems(true)) {
         if (coverageItem.getMethodNum().equals(methodNum) && coverageItem.getExecuteNum().equals(executionLine)) {
            return coverageItem;
         }
      }
      return null;
   }

   public CoverageUnit getCoverageUnit(String index) {
      return coverageUnits.get(new Integer(index).intValue() - 1);
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getLocation() {
      return location;
   }

   public void setLocation(String location) {
      this.location = location;
   }

   public String getText() {
      return text;
   }

   public void setText(String text) {
      this.text = text;
   }

   public String getGuid() {
      return guid;
   }

   public CoverageUnit getParentCoverageUnit() {
      if (parentCoverageEditorItem instanceof CoverageUnit) {
         return (CoverageUnit) parentCoverageEditorItem;
      }
      return null;
   }

   @Override
   public String toString() {
      return getName();
   }

   @Override
   public Result isEditable() {
      return Result.TrueResult;
   }

   @Override
   public boolean isCompleted() {
      for (CoverageItem coverageItem : getCoverageItems(true)) {
         if (!coverageItem.isCompleted()) return false;
      }
      return true;
   }

   @Override
   public OseeImage getOseeImage() {
      if (isFolder()) {
         return FrameworkImage.FOLDER;
      } else if (isCovered()) {
         return CoverageImage.UNIT_GREEN;
      }
      return CoverageImage.UNIT_RED;
   }

   @Override
   public boolean isCovered() {
      for (CoverageItem coverageItem : getCoverageItems(true)) {
         if (!coverageItem.isCovered()) return false;
      }
      return true;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((guid == null) ? 0 : guid.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      CoverageUnit other = (CoverageUnit) obj;
      if (guid == null) {
         if (other.guid != null) return false;
      } else if (!guid.equals(other.guid)) return false;
      return true;
   }

   @Override
   public ICoverage getParent() {
      return parentCoverageEditorItem;
   }

   public Artifact getArtifact(boolean create) throws OseeCoreException {
      if (artifact == null && create) {
         artifact = ArtifactTypeManager.addArtifact(ARTIFACT_NAME, BranchManager.getCommonBranch(), guid, null);
      }
      return artifact;
   }

   public void delete(SkynetTransaction transaction, boolean purge) throws OseeCoreException {
      if (getArtifact(false) != null) {
         if (purge)
            getArtifact(false).purgeFromBranch();
         else
            getArtifact(false).deleteAndPersist(transaction);
      }
      for (CoverageUnit coverageUnit : coverageUnits) {
         coverageUnit.delete(transaction, purge);
      }
      for (CoverageItem coverageItem : coverageItems) {
         coverageItem.delete(transaction, purge);
      }
   }

   public void setParentCoverageEditorItem(ICoverage parent) {
      this.parentCoverageEditorItem = parent;
   }

   public void setGuid(String guid) {
      this.guid = guid;
   }

   public String getNamespace() {
      if (namespace == null) {
         return getParent().getNamespace();
      }
      return namespace;
   }

   public void setNamespace(String namespace) {
      this.namespace = namespace;
   }

   public String getAssignees() throws OseeCoreException {
      return assignees;
   }

   public void setAssignees(String assignees) {
      this.assignees = assignees;
   }

   @Override
   public boolean isAssignable() {
      return true;
   }

   @Override
   public String getNotes() {
      return notes;
   }

   public void setNotes(String notes) {
      this.notes = notes;
   }

   public List<CoverageItem> getCoverageItemsCovered(boolean recurse) {
      List<CoverageItem> items = new ArrayList<CoverageItem>();
      for (CoverageItem coverageItem : getCoverageItems(recurse)) {
         if (coverageItem.getCoverageMethod() != CoverageMethodEnum.Not_Covered) {
            items.add(coverageItem);
         }
      }
      return items;
   }

   public List<CoverageItem> getCoverageItemsCovered(boolean recurse, CoverageMethodEnum... coverageMethodEnum) {
      List<CoverageMethodEnum> coverageMethods = Collections.getAggregate(coverageMethodEnum);
      List<CoverageItem> items = new ArrayList<CoverageItem>();
      for (CoverageItem coverageItem : getCoverageItems(recurse)) {
         if (coverageMethods.contains(coverageItem.getCoverageMethod())) {
            items.add(coverageItem);
         }
      }
      return items;
   }

   @Override
   public Collection<? extends ICoverage> getChildrenItems() {
      List<ICoverage> children = new ArrayList<ICoverage>();
      children.addAll(getCoverageUnits());
      children.addAll(getCoverageItems(false));
      return children;
   }

   @Override
   public void removeCoverageUnit(CoverageUnit coverageUnit) {
      coverageUnits.remove(coverageUnit);
   }

   @Override
   public List<CoverageItem> getCoverageItems() {
      return coverageItems;
   }

   @Override
   public void removeCoverageItem(CoverageItem coverageItem) {
      coverageItems.remove(coverageItem);
   }

   public void load() throws OseeCoreException {
      coverageItems.clear();
      coverageUnits.clear();
      getArtifact(false);
      if (artifact != null) {
         setName(artifact.getName());
         setGuid(artifact.getGuid());
         KeyValueArtifact keyValueArtifact =
               new KeyValueArtifact(artifact, GeneralData.GENERAL_STRING_ATTRIBUTE_TYPE_NAME);
         for (String line : keyValueArtifact.getValues("cvgItem")) {
            coverageItems.add(new CoverageItem(this, line));
         }
         String text = keyValueArtifact.getValue("text");
         if (Strings.isValid(text)) {
            setText(text);
         }
         String notes = keyValueArtifact.getValue("notes");
         if (Strings.isValid(notes)) {
            setNotes(notes);
         }
         setFolder(keyValueArtifact.getValue("folder") != null && keyValueArtifact.getValue("folder").equals("true"));
         String assignees = keyValueArtifact.getValue("assignees");
         if (Strings.isValid(assignees)) {
            setAssignees(assignees);
         }
         String namespace = keyValueArtifact.getValue("namespace");
         if (Strings.isValid(namespace)) {
            setNamespace(namespace);
         }
         String location = keyValueArtifact.getValue("location");
         if (Strings.isValid(location)) {
            setLocation(location);
         }
         for (Artifact childArt : artifact.getChildren()) {
            if (childArt.getArtifactTypeName().equals(CoverageUnit.ARTIFACT_NAME)) {
               addCoverageUnit(new CoverageUnit(childArt));
            }
         }
      }
   }

   public CoverageUnit copy(boolean includeItems) throws OseeCoreException {
      CoverageUnit coverageUnit = new CoverageUnit(parentCoverageEditorItem, name, location);
      coverageUnit.setGuid(guid);
      coverageUnit.setNamespace(namespace);
      coverageUnit.setNotes(notes);
      coverageUnit.setText(text);
      coverageUnit.setFolder(folder);
      coverageUnit.setAssignees(assignees);
      coverageUnit.setLocation(location);
      if (includeItems) {
         for (CoverageItem coverageItem : coverageItems) {
            coverageUnit.addCoverageItem(new CoverageItem(coverageUnit, coverageItem.toXml()));
         }
      }
      return coverageUnit;
   }

   public void save(SkynetTransaction transaction) throws OseeCoreException {
      getArtifact(true);
      artifact.setName(getName());

      KeyValueArtifact keyValueArtifact =
            new KeyValueArtifact(artifact, GeneralData.GENERAL_STRING_ATTRIBUTE_TYPE_NAME);
      List<String> items = new ArrayList<String>();
      for (CoverageItem coverageItem : coverageItems) {
         items.add(coverageItem.toXml());
         coverageItem.save(transaction);
      }
      keyValueArtifact.setValues("cvgItem", items);
      if (notes != null) {
         keyValueArtifact.setValue("notes", notes.toString());
      }
      if (Strings.isValid(namespace)) {
         keyValueArtifact.setValue("namespace", namespace);
      }
      if (Strings.isValid(text)) {
         keyValueArtifact.setValue("text", text);
      }
      if (folder) {
         keyValueArtifact.setValue("folder", String.valueOf(folder));
      }
      if (Strings.isValid(assignees)) {
         keyValueArtifact.setValue("assignees", assignees);
      }
      if (Strings.isValid(location)) {
         keyValueArtifact.setValue("location", location);
      }
      keyValueArtifact.save();
      if (parentCoverageEditorItem != null) {
         parentCoverageEditorItem.getArtifact(false).addChild(artifact);
      }
      for (CoverageUnit coverageUnit : coverageUnits) {
         coverageUnit.save(transaction);
      }
      artifact.persist(transaction);
   }

   @Override
   public String getCoveragePercentStr() {
      return CoverageMetrics.getPercent(getCoverageItemsCovered(true).size(), getCoverageItems(true).size()).getSecond();
   }

   public int getCoveragePercent() {
      return CoverageMetrics.getPercent(getCoverageItemsCovered(true).size(), getCoverageItems(true).size()).getFirst();
   }

   public boolean isFolder() {
      return folder;
   }

   public void setFolder(boolean folder) {
      this.folder = folder;
   }

   public void updateAssigneesAndNotes(CoverageUnit coverageUnit) throws OseeCoreException {
      setNotes(coverageUnit.getNotes());
      setAssignees(coverageUnit.getAssignees());
   }
}
