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

package org.eclipse.osee.framework.skynet.core.artifact;

import static org.eclipse.osee.framework.core.enums.CoreRelationTypes.Default_Hierarchical__Child;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.IRelationSorterId;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.NamedIdentity;
import org.eclipse.osee.framework.core.data.SystemUser;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.EditState;
import org.eclipse.osee.framework.core.enums.IRelationEnumeration;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.AttributeDoesNotExist;
import org.eclipse.osee.framework.core.exception.MultipleArtifactsExist;
import org.eclipse.osee.framework.core.exception.MultipleAttributesExist;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.RelationTypeSide;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.model.event.DefaultBasicGuidArtifact;
import org.eclipse.osee.framework.core.model.event.DefaultBasicGuidRelationReorder;
import org.eclipse.osee.framework.core.model.event.IBasicGuidArtifact;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.core.services.IAccessControlService;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.database.core.DbTransaction;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.HumanReadableId;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.messaging.event.res.AttributeEventModificationType;
import org.eclipse.osee.framework.messaging.event.skynet.event.SkynetAttributeChange;
import org.eclipse.osee.framework.skynet.core.OseeSystemArtifacts;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.annotation.ArtifactAnnotation;
import org.eclipse.osee.framework.skynet.core.artifact.annotation.AttributeAnnotationManager;
import org.eclipse.osee.framework.skynet.core.artifact.annotation.IArtifactAnnotation;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.skynet.core.event2.artifact.AttributeChange;
import org.eclipse.osee.framework.skynet.core.internal.Activator;
import org.eclipse.osee.framework.skynet.core.relation.RelationLink;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;
import org.eclipse.osee.framework.skynet.core.relation.RelationTypeManager;
import org.eclipse.osee.framework.skynet.core.relation.RelationTypeSideSorter;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.skynet.core.types.IArtifact;
import org.osgi.framework.Bundle;

public class Artifact extends NamedIdentity implements IArtifact, IAdaptable, Comparable<Artifact>, IBasicGuidArtifact {
   public static final String UNNAMED = "Unnamed";
   public static final String BEFORE_GUID_STRING = "/BeforeGUID/PrePend";
   public static final String AFTER_GUID_STRING = "/AfterGUID";
   public static final int TRANSACTION_SENTINEL = -1;

   private final HashCollection<IAttributeType, Attribute<?>> attributes =
      new HashCollection<IAttributeType, Attribute<?>>(false, LinkedList.class, 12);
   private final Set<DefaultBasicGuidRelationReorder> relationOrderRecords =
      new HashSet<DefaultBasicGuidRelationReorder>();
   private final Branch branch;
   private String humanReadableId;
   private ArtifactType artifactType;
   private final ArtifactFactory parentFactory;
   private AttributeAnnotationManager annotationMgr;
   private int transactionId = TRANSACTION_SENTINEL;
   private int artId;
   private int gammaId;
   private boolean linksLoaded;
   private boolean historical;
   private ModificationType modType;
   private ModificationType lastValidModType;
   private EditState objectEditState;

   public Artifact(ArtifactFactory parentFactory, String guid, String humanReadableId, Branch branch, ArtifactType artifactType) throws OseeDataStoreException {
      super(guid, "");
      objectEditState = EditState.NO_CHANGE;
      modType = ModificationType.NEW;

      if (humanReadableId == null) {
         populateHumanReadableID();
      } else {
         this.humanReadableId = humanReadableId;
      }

      this.parentFactory = parentFactory;
      this.branch = branch;
      this.artifactType = artifactType;
   }

   public boolean isInDb() {
      return transactionId != TRANSACTION_SENTINEL;
   }

   /**
    * A historical artifact always corresponds to a fixed revision of an artifact
    * 
    * @return whether this artifact represents a fixed revision
    */
   public boolean isHistorical() {
      return historical;
   }

   public Set<ArtifactAnnotation> getAnnotations() {
      Set<ArtifactAnnotation> annotations = new HashSet<ArtifactAnnotation>();
      for (IArtifactAnnotation annotation : getAnnotationExtensions()) {
         annotation.getAnnotations(this, annotations);
      }
      return annotations;
   }

   public boolean isAnnotationWarning() {
      for (ArtifactAnnotation notify : getAnnotations()) {
         if (notify.getType() == ArtifactAnnotation.Type.Warning || notify.getType() == ArtifactAnnotation.Type.Error) {
            return true;
         }
      }
      return false;
   }

   /**
    * All the artifacts related to this artifact by relations of type relationType are returned in a list order based on
    * the stored relation order
    */
   public List<? extends IArtifact> getRelatedArtifacts(RelationType relationType) throws OseeCoreException {
      return RelationManager.getRelatedArtifacts(this, new RelationTypeSide(relationType, RelationSide.SIDE_B));
   }

   @Override
   public List<? extends IArtifact> getRelatedArtifacts(RelationTypeSide relationTypeSide) throws OseeCoreException {
      return RelationManager.getRelatedArtifacts(this, relationTypeSide);
   }

   public List<Artifact> getRelatedArtifactsUnSorted(IRelationEnumeration relationEnum) throws OseeCoreException {
      return RelationManager.getRelatedArtifactsUnSorted(this, relationEnum);
   }

   public List<Artifact> getRelatedArtifacts(IRelationEnumeration relationEnum) throws OseeCoreException {
      return RelationManager.getRelatedArtifacts(this, relationEnum);
   }

   public List<Artifact> getRelatedArtifacts(IRelationEnumeration relationEnum, boolean includeDeleted) throws OseeCoreException {
      return RelationManager.getRelatedArtifacts(this, relationEnum, includeDeleted);
   }

   public String getRelationRationale(Artifact artifact, IRelationEnumeration relationTypeSide) throws OseeCoreException {
      Pair<Artifact, Artifact> sides = determineArtifactSides(artifact, relationTypeSide);
      return RelationManager.getRelationRationale(sides.getFirst(), sides.getSecond(),
         RelationTypeManager.getType(relationTypeSide));
   }

   public void setRelationRationale(Artifact artifact, IRelationEnumeration relationTypeSide, String rationale) throws OseeCoreException {
      Pair<Artifact, Artifact> sides = determineArtifactSides(artifact, relationTypeSide);
      RelationManager.setRelationRationale(sides.getFirst(), sides.getSecond(),
         RelationTypeManager.getType(relationTypeSide), rationale);
   }

   private Pair<Artifact, Artifact> determineArtifactSides(Artifact artifact, IRelationEnumeration relationSide) {
      boolean sideA = relationSide.getSide().isSideA();
      Artifact artifactA = sideA ? artifact : this;
      Artifact artifactB = sideA ? this : artifact;
      return new Pair<Artifact, Artifact>(artifactA, artifactB);
   }

   /**
    * Check if artifacts are related to each other by relation type
    */
   public boolean isRelated(IRelationEnumeration relationEnum, Artifact other) throws OseeCoreException {
      List<Artifact> relatedArtifacts = getRelatedArtifacts(relationEnum);
      return relatedArtifacts.contains(other);
   }

   /**
    * Get the exactly one artifact related to this artifact by relations of type relationType are returned in a list
    * order based on
    */
   public Artifact getRelatedArtifact(IRelationEnumeration relationEnum) throws OseeCoreException {
      return RelationManager.getRelatedArtifact(this, relationEnum);
   }

   public int getRelatedArtifactsCount(IRelationEnumeration relationEnum) throws OseeCoreException {
      return RelationManager.getRelatedArtifactsCount(this, RelationTypeManager.getType(relationEnum),
         relationEnum.getSide());
   }

   public int getRelatedArtifactsCount(RelationTypeSideSorter relationSorter) throws OseeCoreException {
      return RelationManager.getRelatedArtifactsCount(relationSorter.getArtifact(), relationSorter.getRelationType(),
         relationSorter.getSide());
   }

   public <A extends Artifact> List<A> getRelatedArtifactsUnSorted(IRelationEnumeration side, Class<A> clazz) throws OseeCoreException {
      return Collections.castAll(getRelatedArtifactsUnSorted(side));
   }

   public <A extends Artifact> List<A> getRelatedArtifacts(IRelationEnumeration side, Class<A> clazz) throws OseeCoreException {
      return Collections.castAll(getRelatedArtifacts(side));
   }

   @SuppressWarnings("unchecked")
   public <A extends Artifact> List<A> getRelatedArtifactsOfType(IRelationEnumeration side, Class<A> clazz) throws OseeCoreException {
      List<A> objs = new ArrayList<A>();
      for (Artifact art : getRelatedArtifacts(side)) {
         if (clazz.isInstance(art)) {
            objs.add((A) art);
         }
      }
      return objs;
   }

   /**
    * Called upon completion of the initialization of an artifact when it is initially created. This allows sub-class
    * artifacts to set default attributes or do default processing.
    */
   @SuppressWarnings("unused")
   public void onBirth() throws OseeCoreException {
      // provided for subclass implementation
   };

   /**
    * Called upon completion of the initialization of an artifact when loaded from the persistence layer, and when
    * initially created. When called upon initial creation, it is called after <code>onBirth()</code>. This allows
    * sub-class artifacts to set default attributes or do default processing.
    */
   @SuppressWarnings("unused")
   public void onInitializationComplete() throws OseeCoreException {
      // provided for subclass implementation
   }

   @Deprecated
   public void onAttributePersist(SkynetTransaction transaction) {
      // provided for subclass implementation
   }

   @Override
   public int getArtId() {
      return artId;
   }

   public int getArtTypeId() {
      return artifactType.getId();
   }

   @Override
   public Branch getBranch() {
      return branch;
   }

   public String getArtifactTypeName() {
      return artifactType.getName();
   }

   /**
    * Determines if this artifact's type equals, or is a sub-type of, at least one of the given artifact types.
    */
   public boolean isOfType(IArtifactType... artifactTypes) {
      return artifactType.inheritsFrom(artifactTypes);
   }

   @Override
   public String toString() {
      return getName();
   }

   /*
    * Provide easy way to display/report [hrid][name]
    */
   public String toStringWithId() {
      return String.format("[%s][%s]", getHumanReadableId(), getName());
   }

   // TODO should not return null but currently application code expects it to
   /**
    * The method should be used when the caller expects this artifact to have exactly one parent. Otherwise use
    * hasParent() to safely determine whether
    */
   public Artifact getParent() throws OseeCoreException {
      if (hasParent()) {
         return RelationManager.getRelatedArtifact(this, CoreRelationTypes.Default_Hierarchical__Parent);
      }
      return null;
   }

   public Attribute<?> getAttributeById(int attrId, boolean includeDeleted) throws OseeCoreException {
      for (Attribute<?> attribute : getAttributes(includeDeleted)) {
         if (attribute.getId() == attrId) {
            return attribute;
         }
      }
      return null;
   }

   /**
    * @return whether this artifact has exactly one parent artifact related by a relation of type default hierarchical
    * @throws MultipleArtifactsExist if this artifact has more than one parent
    */
   public boolean hasParent() throws OseeCoreException {
      int parentCount = getRelatedArtifactsUnSorted(CoreRelationTypes.Default_Hierarchical__Parent).size();
      if (parentCount > 1) {
         throw new MultipleArtifactsExist(humanReadableId + " has " + parentCount + " parents");
      }

      return parentCount == 1;
   }

   public boolean isOrphan() throws OseeCoreException {
      Artifact root = OseeSystemArtifacts.getDefaultHierarchyRootArtifact(getBranch());

      if (root.equals(getArtifactRoot())) {
         return false;
      } else {
         return true;
      }
   }

   public Artifact getArtifactRoot() throws OseeCoreException {
      Artifact artifactRoot = null;

      for (Artifact parent = getParent(); parent != null; parent = parent.getParent()) {
         artifactRoot = parent;
      }
      return artifactRoot;
   }

   public Artifact getChild(String descriptiveName) throws OseeCoreException {
      for (Artifact artifact : getChildren()) {
         if (artifact.getName().equals(descriptiveName)) {
            return artifact;
         }
      }
      throw new ArtifactDoesNotExist("\"" + getName() + "\" has no child with the name \"" + descriptiveName + "\"");
   }

   public boolean hasChild(String descriptiveName) throws OseeCoreException {
      for (Artifact artifact : getChildren()) {
         if (artifact.getName().equals(descriptiveName)) {
            return true;
         }
      }
      return false;
   }

   /**
    * @return set of the direct children of this artifact
    */
   public List<Artifact> getChildren() throws OseeCoreException {
      return getRelatedArtifacts(Default_Hierarchical__Child);
   }

   /**
    * @return set of the direct children of this artifact
    */
   public List<Artifact> getChildren(boolean includeDeleted) throws OseeCoreException {
      return getRelatedArtifacts(Default_Hierarchical__Child, includeDeleted);
   }

   /**
    * @return a list of artifacts ordered by a depth first traversal of this artifact's descendants
    */
   public List<Artifact> getDescendants() throws OseeCoreException {
      List<Artifact> descendants = new LinkedList<Artifact>();
      getDescendants(descendants);
      return descendants;
   }

   private void getDescendants(Collection<Artifact> descendants) throws OseeCoreException {
      for (Artifact child : getChildren()) {
         descendants.add(child);
         child.getDescendants(descendants);
      }
   }

   public void addChild(Artifact artifact) throws OseeCoreException {
      addChild(null, artifact);
   }

   public void addChild(IRelationSorterId sorterId, Artifact artifact) throws OseeCoreException {
      addRelation(sorterId, Default_Hierarchical__Child, artifact);
   }

   public Artifact addNewChild(IRelationSorterId sorterId, ArtifactType artifactType, String name) throws OseeCoreException {
      Artifact child = ArtifactTypeManager.makeNewArtifact(artifactType, branch);
      child.setName(name);
      addChild(sorterId, child);
      return child;
   }

   /**
    * Creates an instance of <code>Attribute</code> of the given attribute type. This method should not be called by
    * applications. Use addAttribute() instead
    */
   @SuppressWarnings("unchecked")
   private <T> Attribute<T> createAttribute(IAttributeType attributeType) throws OseeCoreException {
      Class<? extends Attribute<T>> attributeClass =
         (Class<? extends Attribute<T>>) AttributeTypeManager.getAttributeBaseClass(attributeType);
      Attribute<T> attribute = null;
      try {
         attribute = attributeClass.newInstance();
         attributes.put(attributeType, attribute);
      } catch (InstantiationException ex) {
         OseeExceptions.wrapAndThrow(ex);
      } catch (IllegalAccessException ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return attribute;
   }

   private <T> Attribute<T> initializeAttribute(IAttributeType attributeType, ModificationType modificationType, boolean markDirty, boolean setDefaultValue) throws OseeCoreException {
      Attribute<T> attribute = createAttribute(attributeType);
      attribute.internalInitialize(attributeType, this, modificationType, markDirty, setDefaultValue);
      return attribute;
   }

   public <T> Attribute<T> internalInitializeAttribute(IAttributeType attributeType, int attributeId, int gammaId, ModificationType modificationType, boolean markDirty, Object... data) throws OseeCoreException {
      Attribute<T> attribute = createAttribute(attributeType);
      attribute.internalInitialize(attributeType, this, modificationType, attributeId, gammaId, markDirty, false);
      attribute.getAttributeDataProvider().loadData(data);
      return attribute;
   }

   public boolean isAttributeTypeValid(IAttributeType attributeType) throws OseeCoreException {
      return getArtifactType().isValidAttributeType(attributeType, branch);
   }

   public boolean isRelationTypeValid(IRelationType relationType) throws OseeCoreException {
      return getValidRelationTypes().contains(relationType);
   }

   public Collection<RelationType> getValidRelationTypes() throws OseeCoreException {
      return RelationTypeManager.getValidTypes(getArtifactType(), branch);
   }

   /**
    * The use of this method is discouraged since it directly returns Attributes.
    */
   public <T> List<Attribute<T>> getAttributes(IAttributeType attributeType, Object value) throws OseeCoreException {
      List<Attribute<?>> filteredList = new ArrayList<Attribute<?>>();
      for (Attribute<?> attribute : getAttributes(attributeType)) {
         if (attribute.getValue().equals(value)) {
            filteredList.add(attribute);
         }
      }
      return Collections.castAll(filteredList);
   }

   /**
    * The use of this method is discouraged since it directly returns Attributes.
    * 
    * @return attributes All attributes including deleted and artifact deleted
    */
   public List<Attribute<?>> getAllAttributesIncludingHardDeleted() throws OseeCoreException {
      return getAttributesByModificationType(ModificationType.getAllStates());
   }

   /**
    * The use of this method is discouraged since it directly returns Attributes.
    * 
    * @return attributes All attributes of the specified type name including deleted and artifact deleted
    */
   public List<Attribute<?>> getAllAttributesIncludingHardDeleted(IAttributeType attributeType) throws OseeCoreException {
      return getAttributesByModificationType(attributeType, ModificationType.getAllStates());
   }

   /**
    * The use of this method is discouraged since it directly returns Attributes.
    * 
    * @return attributes
    */
   public List<Attribute<?>> getAttributes() throws OseeCoreException {
      return getAttributes(false);
   }

   public List<Attribute<?>> getAttributes(boolean includeDeleted) throws OseeCoreException {
      List<Attribute<?>> attributes;
      if (includeDeleted) {
         attributes = getAttributesByModificationType(ModificationType.getAllCurrentModTypes());
      } else {
         attributes = getAttributesByModificationType(ModificationType.getCurrentModTypes());
      }
      return attributes;
   }

   /**
    * The use of this method is discouraged since it directly returns Attributes.
    */
   public <T> List<Attribute<T>> getAttributes(IAttributeType attributeType) throws OseeCoreException {
      return Collections.castAll(getAttributesByModificationType(attributeType, ModificationType.getCurrentModTypes()));
   }

   private List<Attribute<?>> getAttributesByModificationType(Set<ModificationType> allowedModTypes) throws OseeCoreException {
      ensureAttributesLoaded();
      return filterByModificationType(attributes.getValues(), allowedModTypes);
   }

   private List<Attribute<?>> getAttributesByModificationType(IAttributeType attributeType, Set<ModificationType> allowedModTypes) throws OseeCoreException {
      ensureAttributesLoaded();
      return filterByModificationType(attributes.getValues(attributeType), allowedModTypes);
   }

   private List<Attribute<?>> filterByModificationType(Collection<Attribute<?>> attributes, Set<ModificationType> allowedModTypes) {
      List<Attribute<?>> filteredList = new ArrayList<Attribute<?>>();
      if (allowedModTypes != null && !allowedModTypes.isEmpty() && attributes != null && !attributes.isEmpty()) {
         for (Attribute<?> attribute : attributes) {
            if (allowedModTypes.contains(attribute.getModificationType())) {
               filteredList.add(attribute);
            }
         }
      }
      return filteredList;
   }

   /**
    * @return all attributes including deleted ones
    */
   public List<Attribute<?>> internalGetAttributes() {
      return attributes.getValues();
   }

   /**
    * Deletes all attributes of the given type, if any
    */
   public void deleteAttributes(IAttributeType attributeType) throws OseeCoreException {
      for (Attribute<?> attribute : getAttributes(attributeType)) {
         attribute.delete();
      }
   }

   private void ensureAttributesLoaded() throws OseeCoreException {
      if (!isAttributesLoaded() && isInDb()) {
         ArtifactLoader.loadArtifactData(this, LoadLevel.ATTRIBUTE);
      }
   }

   public boolean isAttributesLoaded() {
      return !attributes.isEmpty();
   }

   public Collection<AttributeType> getAttributeTypes() throws OseeCoreException {
      return getArtifactType().getAttributeTypes(branch);
   }

   public <T> Attribute<T> getSoleAttribute(IAttributeType attributeType) throws OseeCoreException {
      ensureAttributesLoaded();
      List<Attribute<T>> soleAttributes = getAttributes(attributeType);
      if (soleAttributes.isEmpty()) {
         return null;
      } else if (soleAttributes.size() > 1) {
         throw new MultipleAttributesExist(String.format(
            "The attribute \'%s\' can have no more than one instance for sole attribute operations; guid \'%s\'",
            attributeType, getGuid()));
      }
      return soleAttributes.iterator().next();
   }

   private <T> Attribute<T> getOrCreateSoleAttribute(IAttributeType attributeType) throws OseeCoreException {
      Attribute<T> attribute = getSoleAttribute(attributeType);
      if (attribute == null) {
         attribute = initializeAttribute(attributeType, ModificationType.NEW, true, true);
      }
      return attribute;
   }

   /**
    * Return he existing attribute value or the default value from a newly initialized attribute if none previously
    * existed
    */
   public <T> T getOrInitializeSoleAttributeValue(IAttributeType attributeType) throws OseeCoreException {
      Attribute<T> attribute = getOrCreateSoleAttribute(attributeType);
      return attribute.getValue();
   }

   /**
    * Return sole attribute value for given attribute type name. Will throw exceptions if "Sole" nature of attribute is
    * invalid.<br>
    * <br>
    * Used for quick access to attribute value that should only have 0 or 1 instances of the attribute.
    */
   public <T> T getSoleAttributeValue(IAttributeType attributeType) throws OseeCoreException {
      List<Attribute<T>> soleAttributes = getAttributes(attributeType);
      if (soleAttributes.isEmpty()) {
         if (!isAttributeTypeValid(attributeType)) {
            throw new OseeArgumentException(String.format(
               "The attribute type %s is not valid for artifacts of type [%s]", attributeType, getArtifactTypeName()));
         }
         throw new AttributeDoesNotExist("Attribute \"" + attributeType + "\" does not exist for artifact " + getGuid());
      } else if (soleAttributes.size() > 1) {
         throw new MultipleAttributesExist(
            "Attribute \"" + attributeType + "\" must have exactly one instance.  It currently has " + soleAttributes.size() + " for artifact " + getGuid());
      }
      return soleAttributes.iterator().next().getValue();
   }

   /**
    * Return sole attribute string value for given attribute type name. Handles AttributeDoesNotExist case by returning
    * defaultReturnValue.<br>
    * <br>
    * Used for display purposes where toString() of attribute is to be displayed.
    * 
    * @param defaultReturnValue return value if attribute instance does not exist
    * @throws MultipleAttributesExist if multiple attribute instances exist
    */

   public String getSoleAttributeValueAsString(IAttributeType attributeType, String defaultReturnValue) throws OseeCoreException, MultipleAttributesExist {

      String toReturn = null;
      Object value = getSoleAttributeValue(attributeType, defaultReturnValue);
      if (value instanceof InputStream) {
         InputStream inputStream = (InputStream) value;
         try {
            toReturn = Lib.inputStreamToString(inputStream);
         } catch (IOException ex) {
            OseeExceptions.wrapAndThrow(ex);
         } finally {
            try {
               inputStream.close();
            } catch (IOException ex) {
               OseeExceptions.wrapAndThrow(ex);
            }
         }
      } else {
         if (value != null) {
            toReturn = value.toString();
         }
      }
      return toReturn;
   }

   /**
    * Return sole attribute value for given attribute type name Handles AttributeDoesNotExist case by returning
    * defaultReturnValue.<br>
    * <br>
    * Used for purposes where attribute value of specified type is desired.
    * 
    * @throws MultipleAttributesExist if multiple attribute instances exist
    */
   public <T> T getSoleAttributeValue(IAttributeType attributeType, T defaultReturnValue) throws OseeCoreException {
      List<Attribute<T>> soleAttributes = getAttributes(attributeType);
      if (soleAttributes.size() == 1) {
         T value = soleAttributes.iterator().next().getValue();
         if (value == null) {
            OseeLog.log(
               Activator.class,
               Level.SEVERE,
               "Attribute \"" + attributeType + "\" has null value for Artifact " + getGuid() + " \"" + getName() + "\"");
            return defaultReturnValue;
         }
         return value;
      } else if (soleAttributes.size() > 1) {
         throw new MultipleAttributesExist(
            "Attribute \"" + attributeType + "\" must have exactly one instance.  It currently has " + soleAttributes.size() + " for artifact " + getGuid());
      } else {
         return defaultReturnValue;
      }
   }

   /**
    * Delete attribute if exactly one exists. Does nothing if attribute does not exist and throw MultipleAttributesExist
    * is more than one instance of the attribute type exsits for this artifact
    */
   public void deleteSoleAttribute(IAttributeType attributeType) throws OseeCoreException {
      Attribute<?> attribute = getSoleAttribute(attributeType);
      if (attribute != null) {
         if (!attribute.isInDb()) {
            attributes.removeValue(attributeType, attribute);
         } else {
            attribute.delete();
         }
      }
   }

   public void deleteAttribute(IAttributeType attributeType, Object value) throws OseeCoreException {
      for (Attribute<Object> attribute : getAttributes(attributeType)) {
         if (attribute.getValue().equals(value)) {
            attribute.delete();
         }
      }
   }

   /**
    * Used on attribute types with no more than one instance. If the attribute exists, it's value is changed, otherwise
    * a new attribute is added and its value set.
    */
   public <T> void setSoleAttributeValue(IAttributeType attributeType, T value) throws OseeCoreException {
      getOrCreateSoleAttribute(attributeType).setValue(value);
   }

   public <T> void setSoleAttributeFromString(IAttributeType attributeType, String value) throws OseeCoreException {
      getOrCreateSoleAttribute(attributeType).setFromString(value);
   }

   public void setSoleAttributeFromStream(IAttributeType attributeType, InputStream stream) throws OseeCoreException {
      getOrCreateSoleAttribute(attributeType).setValueFromInputStream(stream);
   }

   /**
    * @return comma delimited representation of all the attributes of the type attributeName
    */
   public String getAttributesToString(IAttributeType attributeType) throws OseeCoreException {
      StringBuffer sb = new StringBuffer();
      for (Attribute<?> attr : getAttributes(attributeType)) {
         sb.append(attr);
         sb.append(", ");
      }
      return sb.toString().replaceFirst(", $", "");
   }

   /**
    * All existing attributes matching a new value will be left untouched. Then for any remaining values, other existing
    * attributes will be changed to match or if need be new attributes will be added to stored these values. Finally any
    * excess attributes will be deleted.
    */
   public void setAttributeValues(IAttributeType attributeType, Collection<String> newValues) throws OseeCoreException {
      ensureAttributesLoaded();
      // ensure new values are unique
      HashSet<String> uniqueNewValues = new HashSet<String>(newValues);

      List<Attribute<Object>> remainingAttributes = getAttributes(attributeType);
      List<String> remainingNewValues = new ArrayList<String>(uniqueNewValues.size());

      // all existing attributes matching a new value will be left untouched
      for (String newValue : uniqueNewValues) {
         boolean found = false;
         for (Attribute<Object> attribute : remainingAttributes) {
            if (attribute.getValue().toString().equals(newValue)) {
               remainingAttributes.remove(attribute);
               found = true;
               break;
            }
         }
         if (!found) {
            remainingNewValues.add(newValue);
         }
      }

      for (String newValue : remainingNewValues) {
         if (remainingAttributes.isEmpty()) {
            setOrAddAttribute(attributeType, newValue);
         } else {
            int index = remainingAttributes.size() - 1;
            remainingAttributes.get(index).setValue(newValue);
            remainingAttributes.remove(index);
         }
      }

      for (Attribute<Object> attribute : remainingAttributes) {
         attribute.delete();
      }
   }

   /**
    * adds a new attribute of the type named attributeTypeName and assigns it the given value
    */
   public <T> void addAttribute(IAttributeType attributeType, T value) throws OseeCoreException {
      initializeAttribute(attributeType, ModificationType.NEW, true, false).setValue(value);
   }

   /**
    * adds a new attribute of the type named attributeTypeName. The attribute is set to the default value for its type,
    * if any.
    */
   public void addAttribute(IAttributeType attributeType) throws OseeCoreException {
      initializeAttribute(attributeType, ModificationType.NEW, true, true);
   }

   /**
    * adds a new attribute of the type named attributeTypeName. The attribute is set to the default value for its type,
    * if any.
    */
   public void addAttribute(AttributeType attributeType) throws OseeCoreException {
      initializeAttribute(attributeType, ModificationType.NEW, true, true);
   }

   /**
    * adds a new attribute of the type named attributeTypeName and assigns it the given value
    */
   public void addAttributeFromString(IAttributeType attributeType, String value) throws OseeCoreException {
      initializeAttribute(attributeType, ModificationType.NEW, true, false).setFromString(value);
   }

   /**
    * we do not what duplicated enumerated values so this method silently returns if the specified attribute type is
    * enumerated and value is already present
    */
   public <T> void setOrAddAttribute(IAttributeType attributeType, T value) throws OseeCoreException {
      List<Attribute<String>> attributes = getAttributes(attributeType);
      for (Attribute<String> canidateAttribute : attributes) {
         if (canidateAttribute.getValue().equals(value)) {
            return;
         }
      }
      addAttribute(attributeType, value);
   }

   /**
    * @return string collection containing of all the attribute values of type attributeType
    */
   public List<String> getAttributesToStringList(IAttributeType attributeType) throws OseeCoreException {
      ensureAttributesLoaded();

      List<String> items = new ArrayList<String>();
      for (Attribute<?> attribute : getAttributes(attributeType)) {
         items.add(attribute.getDisplayableString());
      }
      return items;
   }

   @Override
   public String getName() {
      String name = null;
      try {
         ensureAttributesLoaded();
         // use the first name attribute whether deleted or not.
         for (Attribute<?> attribute : internalGetAttributes()) {
            if (attribute.isOfType(CoreAttributeTypes.Name)) {
               name = (String) attribute.getValue();
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      if (!Strings.isValid(name)) {
         return UNNAMED;
      }
      return name;
   }

   @Override
   public void setName(String name) throws OseeCoreException {
      setSoleAttributeValue(CoreAttributeTypes.Name, name);
   }

   public ArtifactFactory getFactory() {
      return parentFactory;
   }

   /**
    * This is used to mark that the artifact deleted.
    */
   public void internalSetDeleted() throws OseeCoreException {
      this.modType = ModificationType.DELETED;

      for (Attribute<?> attribute : getAttributes()) {
         attribute.setArtifactDeleted();
      }
   }

   public void internalSetDeletedFromRemoteEvent() throws OseeCoreException {
      if (!isHistorical()) {
         this.modType = ModificationType.DELETED;
         ArtifactCache.deCache(this);

         for (Attribute<?> attribute : getAttributes()) {
            attribute.internalSetDeletedFromRemoteEvent();
         }
      }
   }

   /**
    * This is used to mark that the artifact not deleted. This should only be called by the RemoteEventManager.
    */
   public void resetToPreviousModType() {
      this.modType = lastValidModType;

      for (Attribute<?> attribute : attributes.getValues()) {
         if (attribute.getModificationType() == ModificationType.ARTIFACT_DELETED) {
            attribute.resetModType();
         }
      }
   }

   /**
    * @return whether this artifact has unsaved attribute changes
    */
   public boolean hasDirtyAttributes() {
      for (Attribute<?> attribute : internalGetAttributes()) {
         if (attribute.isDirty()) {
            return true;
         }
      }
      return false;
   }

   /**
    * @return whether this artifact has unsaved relation changes
    */
   public boolean hasDirtyRelations() {
      return RelationManager.hasDirtyLinks(this);
   }

   public EditState getEditState() {
      return objectEditState;
   }

   public boolean hasDirtyArtifactType() {
      return objectEditState.isArtifactTypeChange();
   }

   /**
    * @return whether this artifact has unsaved relation changes
    */
   public boolean isDirty() {
      return hasDirtyAttributes() || hasDirtyRelations() || hasDirtyArtifactType();
   }

   private IAccessControlService getAccessControlService() {
      return Activator.getInstance().getAccessControlService();
   }

   public boolean isReadOnly() {
      try {
         return isDeleted() || isHistorical() || !getBranch().isEditable() || !getAccessControlService().hasPermission(
            this, PermissionEnum.WRITE);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return true;
      }
   }

   public void revert() throws OseeCoreException {
      DbTransaction dbTransaction = new DbTransaction() {
         @Override
         protected void handleTxWork(OseeConnection connection) throws OseeCoreException {
            ArtifactPersistenceManager.revertArtifact(connection, Artifact.this);
         }
      };
      dbTransaction.execute();
   }

   /**
    * Reloads this artifact's attributes and relations back to the last state saved. This will have no effect if the
    * artifact has never been saved.
    */
   public void reloadAttributesAndRelations() throws OseeCoreException {
      if (!isInDb()) {
         return;
      }

      ArtifactQuery.reloadArtifactFromId(getArtId(), getBranch());
   }

   void prepareForReload() {
      attributes.clear();
      linksLoaded = false;

      RelationManager.prepareRelationsForReload(this);
   }

   private final void persistAttributes(SkynetTransaction transaction) throws OseeCoreException {
      if (!UserManager.duringMainUserCreation() && !getAccessControlService().hasPermission(getBranch(),
         PermissionEnum.WRITE)) {
         throw new OseeArgumentException(
            "No write permissions for the branch that this artifact belongs to:" + getBranch());
      }
      if (isHistorical()) {
         throw new OseeArgumentException(
            "The artifact " + getGuid() + " must be at the head of the branch to be edited.");
      }

      if (hasDirtyAttributes() || hasDirtyArtifactType()) {
         transaction.addArtifactAndAttributes(this);
         onAttributePersist(transaction);
      }
   }

   private final void persistRelations(SkynetTransaction transaction) throws OseeCoreException {
      RelationManager.persistRelationsFor(transaction, this, null);
   }

   public final void persist() throws OseeCoreException {
      persist("artifact.persist() default transaction");
   }

   public final void persist(String comment) throws OseeCoreException {
      SkynetTransaction transaction = new SkynetTransaction(branch, comment);
      persist(transaction);
      transaction.execute();
   }

   public final void persist(SkynetTransaction transaction) throws OseeCoreException {
      if (transaction == null) {
         persist();
      } else {
         persistAttributes(transaction);
         persistRelations(transaction);
      }
   }

   /**
    * Returns all of the descendants through the primary decomposition tree that have a particular human readable id.
    * This will not return the called upon node if the name matches since it can not be a descendant of itself.
    * 
    * @param humanReadableId The human readable id text to match against.
    * @param caseSensitive Whether to use case sensitive matching.
    * @return <code>Collection</code> of <code>Artifact</code>'s that match.
    */
   public Collection<Artifact> getDescendants(String humanReadableId, boolean caseSensitive) throws OseeCoreException {
      Collection<Artifact> descendants = new LinkedList<Artifact>();

      for (Artifact child : getChildren()) {
         if (caseSensitive && child.getName().equals(humanReadableId) || !caseSensitive && child.getName().equalsIgnoreCase(
            humanReadableId)) {
            descendants.add(child);
         }
         descendants.addAll(child.getDescendants(humanReadableId, caseSensitive));
      }

      return descendants;
   }

   /**
    * Starting from this artifact, walks down the child hierarchy based on the list of child names provided and returns
    * the child of the last name provided. ArtifactDoesNotExist exception is thrown ff any child along the path does not
    * exist.
    * 
    * @return child at the leaf (bottom) of the specified hierarchy.
    * @throws OseeCoreException
    */
   public Artifact getDescendant(String... names) throws OseeCoreException {
      if (names.length == 0) {
         throw new OseeArgumentException("Must suply at least one name to getDescendant()");
      }
      Artifact descendant = this;
      for (String name : names) {
         descendant = descendant.getChild(name);
      }
      return descendant;
   }

   /**
    * Removes artifact from a specific branch
    */
   public void deleteAndPersist() throws OseeCoreException {
      SkynetTransaction transaction = new SkynetTransaction(branch, "Delete artifact from a specific branch");
      deleteAndPersist(transaction);
      transaction.execute();
   }

   public void deleteAndPersist(SkynetTransaction transaction, boolean overrideChecks) throws OseeCoreException {
      ArtifactPersistenceManager.deleteArtifact(transaction, overrideChecks, this);
   }

   /**
    * Removes artifact from a specific branch
    */
   public void deleteAndPersist(SkynetTransaction transaction) throws OseeCoreException {
      ArtifactPersistenceManager.deleteArtifact(transaction, false, this);
   }

   public void delete() throws OseeCoreException {
      ArtifactPersistenceManager.deleteArtifact(null, false, this);
   }

   /**
    * Remove artifact from a specific branch in the database
    */
   public void purgeFromBranch() throws OseeCoreException {
      new PurgeArtifacts(Arrays.asList(this)).execute();
   }

   public boolean isDeleted() {
      return modType == ModificationType.DELETED;
   }

   public void setLinksLoaded(boolean loaded) {
      linksLoaded = loaded;
   }

   public void addRelation(IRelationSorterId sorterId, IRelationEnumeration relationTypeSide, Artifact artifact, String rationale) throws OseeCoreException {
      Pair<Artifact, Artifact> sides = determineArtifactSides(artifact, relationTypeSide);
      RelationManager.addRelation(sorterId, RelationTypeManager.getType(relationTypeSide), sides.getFirst(),
         sides.getSecond(), rationale);
   }

   public void addRelation(IRelationEnumeration relationSide, Artifact artifact) throws OseeCoreException {
      addRelation(null, relationSide, artifact, null);
   }

   public void addRelation(IRelationSorterId sorterId, IRelationEnumeration relationSide, Artifact artifact) throws OseeCoreException {
      addRelation(sorterId, relationSide, artifact, null);
   }

   public void addRelation(IRelationSorterId sorterId, IRelationEnumeration relationEnumeration, Artifact targetArtifact, boolean insertAfterTarget, Artifact itemToAdd, String rationale) throws OseeCoreException {
      boolean sideA = relationEnumeration.getSide().isSideA();
      Artifact artifactA = sideA ? itemToAdd : this;
      Artifact artifactB = sideA ? this : itemToAdd;

      RelationManager.addRelation(sorterId, RelationTypeManager.getType(relationEnumeration), artifactA, artifactB,
         rationale);
      setRelationOrder(relationEnumeration, targetArtifact, insertAfterTarget, itemToAdd);
   }

   public void setRelationOrder(IRelationEnumeration relationSide, List<Artifact> artifactsInNewOrder) throws OseeCoreException {
      RelationManager.setRelationOrder(this, RelationTypeManager.getType(relationSide), relationSide.getSide(),
         RelationOrderBaseTypes.USER_DEFINED, artifactsInNewOrder);
   }

   public void setRelationOrder(IRelationEnumeration relationEnumeration, IRelationSorterId orderId) throws OseeCoreException {
      if (RelationOrderBaseTypes.USER_DEFINED == orderId) {
         setRelationOrder(relationEnumeration, getRelatedArtifacts(relationEnumeration));
      } else {
         List<Artifact> empty = java.util.Collections.emptyList();
         RelationManager.setRelationOrder(this, RelationTypeManager.getType(relationEnumeration),
            relationEnumeration.getSide(), orderId, empty);
      }
   }

   public void setRelationOrder(IRelationEnumeration relationEnumeration, Artifact targetArtifact, boolean insertAfterTarget, Artifact itemToAdd) throws OseeCoreException {
      List<Artifact> currentOrder = getRelatedArtifacts(relationEnumeration, Artifact.class);
      // target artifact doesn't exist
      if (!currentOrder.contains(targetArtifact)) {
         // add to end of list if not already in list
         if (!currentOrder.contains(itemToAdd)) {
            currentOrder.add(itemToAdd);
         }
      }
      boolean result = Collections.moveItem(currentOrder, itemToAdd, targetArtifact, insertAfterTarget);
      if (!result) {
         throw new OseeStateException("Could not set Relation Order");
      }

      RelationManager.setRelationOrder(this, RelationTypeManager.getType(relationEnumeration),
         relationEnumeration.getSide(), RelationOrderBaseTypes.USER_DEFINED, currentOrder);
   }

   public void deleteRelation(IRelationEnumeration relationTypeSide, Artifact artifact) throws OseeCoreException {
      Pair<Artifact, Artifact> sides = determineArtifactSides(artifact, relationTypeSide);
      RelationManager.deleteRelation(RelationTypeManager.getType(relationTypeSide), sides.getFirst(), sides.getSecond());
   }

   public void deleteRelations(IRelationEnumeration relationSide) throws OseeCoreException {
      for (Artifact art : getRelatedArtifacts(relationSide)) {
         deleteRelation(relationSide, art);
      }
   }

   /**
    * Creates new relations that don't already exist and removes relations to artifacts that are not in collection
    */
   public void setRelations(IRelationSorterId sorterId, IRelationEnumeration relationSide, Collection<? extends Artifact> artifacts) throws OseeCoreException {
      Collection<Artifact> currentlyRelated = getRelatedArtifacts(relationSide, Artifact.class);
      // Remove relations that have been removed
      for (Artifact artifact : currentlyRelated) {
         if (!artifacts.contains(artifact)) {
            deleteRelation(relationSide, artifact);
         }
      }
      // Add new relations if don't exist
      for (Artifact artifact : artifacts) {
         if (!currentlyRelated.contains(artifact)) {
            addRelation(sorterId, relationSide, artifact);
         }
      }
   }

   /**
    * Creates new relations that don't already exist and removes relations to artifacts that are not in collection
    */
   public void setRelations(IRelationEnumeration relationSide, Collection<? extends Artifact> artifacts) throws OseeCoreException {
      setRelations(null, relationSide, artifacts);
   }

   /**
    * Creates new relations that don't already exist and removes relations to artifacts that are not in collection
    */
   public void setRelationsOfTypeUseCurrentOrder(IRelationEnumeration relationSide, Collection<? extends Artifact> artifacts, Class<?> clazz) throws OseeCoreException {
      RelationTypeSideSorter sorter =
         RelationManager.createTypeSideSorter(this, RelationTypeManager.getType(relationSide), relationSide.getSide());
      Collection<Artifact> currentlyRelated = getRelatedArtifacts(relationSide, Artifact.class);
      // Add new relations if don't exist
      for (Artifact artifact : artifacts) {
         if (clazz.isInstance(artifact) && !currentlyRelated.contains(artifact)) {
            addRelation(sorter.getSorterId(), relationSide, artifact);
         }
      }
      // Remove relations that have been removed
      for (Artifact artifact : currentlyRelated) {
         if (clazz.isInstance(artifact) && !artifacts.contains(artifact)) {
            deleteRelation(relationSide, artifact);
         }
      }
   }

   public final boolean isLinksLoaded() {
      return linksLoaded;
   }

   public String getHumanReadableId() {
      return humanReadableId;
   }

   private void populateHumanReadableID() throws OseeDataStoreException {
      String hrid = HumanReadableId.generate();
      humanReadableId = isUniqueHRID(hrid) ? hrid : HumanReadableId.generate();
   }

   public static boolean isUniqueHRID(String id) throws OseeDataStoreException {
      String DUPLICATE_HRID_SEARCH =
         "select count(1) from (select DISTINCT(art_id) from osee_artifact where human_readable_id = ?) t1";
      return ConnectionHandler.runPreparedQueryFetchLong(0L, DUPLICATE_HRID_SEARCH, id) <= 0;
   }

   /**
    * @return Returns the descriptor.
    */
   @Override
   public ArtifactType getArtifactType() {
      return artifactType;
   }

   public String getVersionedName() {
      String name = getName();

      if (isHistorical()) {
         name += " [Rev:" + transactionId + "]";
      }

      return name;
   }

   /**
    * Return true if this artifact any of it's links specified or any of the artifacts on the other side of the links
    * are dirty
    */
   public String isRelationsAndArtifactsDirty(Set<IRelationEnumeration> links) {
      try {
         if (hasDirtyAttributes()) {

            for (Attribute<?> attribute : internalGetAttributes()) {
               if (attribute.isDirty()) {
                  return "===> Dirty Attribute - " + attribute.getAttributeType().getName() + "\n";
               }
            }
            return "Artifact isDirty == true??";
         }
         // Loop through all relations
         for (IRelationEnumeration side : links) {
            for (Artifact art : getRelatedArtifacts(side)) {
               // Check artifact dirty
               if (art.hasDirtyAttributes()) {
                  return art.getArtifactTypeName() + " \"" + art + "\" => dirty\n";
               }
               // Check the links to this artifact
               for (RelationLink link : getRelations(side, art)) {
                  if (link.isDirty()) {
                     return "Link \"" + link + "\" => dirty\n";
                  }
               }
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return null;
   }

   /**
    * Creates a new artifact and duplicates all of its attribute data.
    */
   public Artifact duplicate(Branch branch) throws OseeCoreException {
      Artifact newArtifact = ArtifactTypeManager.makeNewArtifact(artifactType, branch);
      // we do this because attributes were added on creation to meet the
      // minimum attribute requirements
      newArtifact.attributes.clear();
      copyAttributes(newArtifact);
      return newArtifact;
   }

   private void copyAttributes(Artifact artifact) throws OseeCoreException {
      for (Attribute<?> attribute : getAttributes()) {
         if (isCopyAllowed(attribute)) {
            artifact.addAttribute(attribute.getAttributeType(), attribute.getValue());
         }
      }
   }

   private boolean isCopyAllowed(Attribute<?> attribute) {
      return attribute != null && !attribute.isOfType(CoreAttributeTypes.RelationOrder);
   }

   /**
    * An artifact reflected about its own branch returns itself. Otherwise a new artifact is introduced on the
    * destinationBranch
    * 
    * @return the newly created artifact or this artifact if the destinationBranch is this artifact's branch
    */
   public Artifact reflect(IOseeBranch destinationBranch) throws OseeCoreException {
      if (branch.equals(destinationBranch)) {
         return this;
      }
      return reflectHelper(destinationBranch);
   }

   private Artifact reflectHelper(IOseeBranch branch) throws OseeCoreException {
      Artifact reflectedArtifact =
         ArtifactTypeManager.getFactory(artifactType).reflectExisitingArtifact(artId, getGuid(), humanReadableId,
            artifactType, gammaId, branch, ModificationType.INTRODUCED);

      for (Attribute<?> sourceAttribute : attributes.getValues()) {
         // In order to reflect attributes they must exist in the data store
         // and be valid for the destination branch as well
         if (sourceAttribute.isInDb() && reflectedArtifact.isAttributeTypeValid(sourceAttribute.getAttributeType())) {
            reflectedArtifact.internalInitializeAttribute(sourceAttribute.getAttributeType(), sourceAttribute.getId(),
               sourceAttribute.getGammaId(), ModificationType.INTRODUCED, true,
               sourceAttribute.getAttributeDataProvider().getData());
         }
      }
      return reflectedArtifact;
   }

   public void updateArtifactFromBranch(IOseeBranch updateSourceBranch) throws OseeCoreException {
      // Do not update the artifact with itself.
      if (branch.equals(updateSourceBranch)) {
         return;
      }
      updateAttributesFromBranch(updateSourceBranch);
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   private void updateAttributesFromBranch(IOseeBranch updateSourceBranch) throws OseeCoreException {
      Artifact updateSourceArtifact = ArtifactQuery.getArtifactFromId(this.getArtId(), updateSourceBranch);
      for (Attribute<?> updateSourceAttr : updateSourceArtifact.getAttributes()) {
         Attribute thisAttr = getAttributeById(updateSourceAttr.getId(), true);
         if (isCopyAllowed(updateSourceAttr)) {
            if (thisAttr != null && thisAttr.getGammaId() != updateSourceAttr.getGammaId()) {
               thisAttr.setValue(updateSourceAttr.getValue());
            }
         }
      }
   }

   /**
    * @return the transaction number that was set when this artifact was loaded
    */
   public int getTransactionNumber() {
      return transactionId;
   }

   public TransactionRecord getTransactionRecord() throws OseeCoreException {
      if (transactionId == TRANSACTION_SENTINEL) {
         return null;
      }
      return TransactionManager.getTransactionId(transactionId);
   }

   /**
    * @return Returns the gammaId.
    */
   public int getGammaId() {
      return gammaId;
   }

   /**
    * @return Returns dirty attributes.
    */
   public Collection<SkynetAttributeChange> getDirtySkynetAttributeChanges() throws OseeDataStoreException {
      List<SkynetAttributeChange> dirtyAttributes = new LinkedList<SkynetAttributeChange>();

      for (Attribute<?> attribute : internalGetAttributes()) {
         if (attribute.isDirty()) {
            dirtyAttributes.add(new SkynetAttributeChange(attribute.getAttributeType().getId(),
               attribute.getAttributeDataProvider().getData(), attribute.getModificationType(), attribute.getId(),
               attribute.getGammaId()));
         }
      }
      return dirtyAttributes;
   }

   public Collection<AttributeChange> getDirtyFrameworkAttributeChanges() throws OseeDataStoreException {
      List<AttributeChange> dirtyAttributes = new LinkedList<AttributeChange>();

      for (Attribute<?> attribute : internalGetAttributes()) {
         if (attribute.isDirty()) {
            AttributeChange change = new AttributeChange();
            change.setAttrTypeGuid(attribute.getAttributeType().getGuid());
            change.setGammaId(attribute.getGammaId());
            change.setAttributeId(attribute.getId());
            change.setModTypeGuid(AttributeEventModificationType.getType(attribute.getModificationType()).getGuid());
            for (Object obj : attribute.getAttributeDataProvider().getData()) {
               if (obj == null) {
                  change.getData().add("");
               } else if (obj instanceof String) {
                  change.getData().add((String) obj);
               } else {
                  OseeLog.log(Activator.class, Level.SEVERE, "Unhandled data type " + obj.getClass().getSimpleName());
               }
            }
            dirtyAttributes.add(change);
         }
      }
      return dirtyAttributes;
   }

   /**
    * Changes the artifact type.
    */
   public void setArtifactType(ArtifactType artifactType) {
      if (!this.artifactType.equals(artifactType)) {
         this.artifactType = artifactType;
         objectEditState = EditState.ARTIFACT_TYPE_MODIFIED;
         if (isInDb()) {
            lastValidModType = modType;
            modType = ModificationType.MODIFIED;
         }
      }
   }

   public void clearEditState() {
      objectEditState = EditState.NO_CHANGE;
      resetToPreviousModType();
   }

   private static final Pattern safeNamePattern = Pattern.compile("[^A-Za-z0-9 ]");
   private static final String[] NUMBER = new String[] {"Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven",
      "Eight", "Nine"};

   /**
    * Since artifact names are free text it is important to reformat the name to ensure it is suitable as an element
    * name
    * 
    * @return artifact name in a form that is valid as an XML element
    */
   public String getSafeName() {
      String elementName = safeNamePattern.matcher(getName()).replaceAll("_");

      // Fix the first character if it is a number by replacing it with its name
      char firstChar = elementName.charAt(0);
      if (firstChar >= '0' && firstChar <= '9') {
         elementName = NUMBER[firstChar - '0'] + elementName.substring(1);
      }

      if (elementName.length() > 75) {
         elementName = elementName.substring(0, 75);
      }

      return elementName;
   }

   private Set<IArtifactAnnotation> artifactAnnotationExtensions;

   private Set<IArtifactAnnotation> getAnnotationExtensions() {
      if (artifactAnnotationExtensions != null) {
         return artifactAnnotationExtensions;
      }
      artifactAnnotationExtensions = new HashSet<IArtifactAnnotation>();
      IExtensionPoint point =
         Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.osee.framework.skynet.core.ArtifactAnnotation");
      if (point == null) {
         System.err.println("Can't access ArtifactAnnotation extension point");
         return artifactAnnotationExtensions;
      }
      IExtension[] extensions = point.getExtensions();
      for (IExtension extension : extensions) {
         IConfigurationElement[] elements = extension.getConfigurationElements();
         String classname = null;
         String bundleName = null;
         for (IConfigurationElement el : elements) {
            if (el.getName().equals("ArtifactAnnotation")) {
               classname = el.getAttribute("classname");
               bundleName = el.getContributor().getName();
               if (classname != null && bundleName != null) {
                  Bundle bundle = Platform.getBundle(bundleName);
                  try {
                     Class<?> taskClass = bundle.loadClass(classname);
                     Object obj = taskClass.newInstance();
                     artifactAnnotationExtensions.add((IArtifactAnnotation) obj);
                  } catch (Exception ex) {
                     ex.printStackTrace();
                  }
               }

            }
         }
      }
      return artifactAnnotationExtensions;
   }

   public AttributeAnnotationManager getAnnotationMgr() {
      if (annotationMgr == null) {
         annotationMgr = new AttributeAnnotationManager(this);
      }
      return annotationMgr;
   }

   @Override
   @SuppressWarnings({"rawtypes"})
   public Object getAdapter(Class adapter) {
      if (adapter == null) {
         throw new IllegalArgumentException("adapter can not be null");
      }

      if (adapter.isInstance(this)) {
         return this;
      }
      return null;
   }

   @Override
   public final int compareTo(Artifact otherArtifact) {
      if (otherArtifact == null || otherArtifact.isDeleted()) {
         return -1;
      } else if (this.isDeleted()) {
         return 1;
      }

      int diff;
      if (otherArtifact.equals(this)) {
         diff = 0;
      } else {
         try {
            diff = getName().compareTo(otherArtifact.getName());
         } catch (Exception ex) {
            diff = 0;
         }
      }

      return diff;
   }

   @Override
   public final int hashCode() {
      int hashCode = 11;
      hashCode = hashCode * 37 + getGuid().hashCode();
      hashCode = hashCode * 37 + getBranch().hashCode();
      return hashCode;
   }

   /**
    * @param obj the reference object with which to compare.
    * @return <code>true</code> if this artifact has the same GUID and branch <code>false</code> otherwise.
    */
   @Override
   public final boolean equals(Object obj) {
      if (obj instanceof IBasicGuidArtifact) {
         IBasicGuidArtifact other = (IBasicGuidArtifact) obj;
         boolean result = getGuid().equals(other.getGuid());
         if (result) {
            if (getBranchGuid() != null && other.getBranchGuid() != null) {
               result = getBranchGuid().equals(other.getBranchGuid());
            }
         }
         return result;
      }
      if (obj instanceof IArtifact) {
         IArtifact other = (IArtifact) obj;
         boolean result = getArtId() == other.getArtId();
         if (result) {
            if (getBranch() != null && other.getBranch() != null) {
               result = getBranch().equals(other.getBranch());
            } else {
               result = getBranch() == null && other.getBranch() == null;
            }
         }
         return result;
      }
      return false;
   }

   public int getRemainingAttributeCount(AttributeType attributeType) throws OseeCoreException {
      return attributeType.getMaxOccurrences() - getAttributeCount(attributeType);
   }

   public int getAttributeCount(IAttributeType attributeType) throws OseeCoreException {
      ensureAttributesLoaded();
      return getAttributes(attributeType).size();
   }

   void setArtId(int artifactId) {
      this.artId = artifactId;
   }

   /**
    * Return relations that exist between artifacts
    */
   public ArrayList<RelationLink> internalGetRelations(Artifact artifact) throws OseeCoreException {
      ArrayList<RelationLink> relations = new ArrayList<RelationLink>();
      for (RelationLink relation : getRelationsAll(false)) {
         try {
            if (relation.getArtifactOnOtherSide(this).equals(artifact)) {
               relations.add(relation);
            }
         } catch (ArtifactDoesNotExist ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
      return relations;
   }

   public List<RelationLink> getRelations(IRelationEnumeration relationEnum) throws OseeCoreException {
      return RelationManager.getRelations(this, RelationTypeManager.getType(relationEnum), relationEnum.getSide());
   }

   /**
    * Return relations that exist between artifacts of type side
    */
   @Deprecated
   public ArrayList<RelationLink> getRelations(IRelationEnumeration side, Artifact artifact) throws OseeCoreException {
      ArrayList<RelationLink> relations = new ArrayList<RelationLink>();
      for (RelationLink relation : getRelations(side)) {
         try {
            if (relation.getArtifactOnOtherSide(this).equals(artifact)) {
               relations.add(relation);
            }
         } catch (ArtifactDoesNotExist ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
      return relations;
   }

   /**
    * @return a list of relations from a specific relation type
    */
   @Deprecated
   public List<RelationLink> getRelations(RelationType relationType) {
      return RelationManager.getRelations(this, relationType, null);
   }

   public List<RelationLink> getRelationsAll(boolean includeDeleted) {
      return RelationManager.getRelationsAll(getArtId(), getBranch().getId(), includeDeleted);
   }

   /**
    * This method should never be called from outside the OSEE Application Framework
    */
   void internalSetPersistenceData(int gammaId, int transactionId, ModificationType modType, boolean historical) {
      this.gammaId = gammaId;
      this.transactionId = transactionId;
      this.historical = historical;
      this.modType = modType;
      this.lastValidModType = modType;
      this.objectEditState = EditState.NO_CHANGE;
   }

   /**
    * This method should never be called from outside the OSEE Application Framework
    */
   public void setTransactionId(int transactionId) {
      this.transactionId = transactionId;
   }

   public Date getLastModified() throws OseeCoreException {
      if (transactionId == TRANSACTION_SENTINEL) {
         return new Date();
      }
      return getTransactionRecord().getTimeStamp();
   }

   public User getLastModifiedBy() throws OseeCoreException {
      TransactionRecord transactionRecord = getTransactionRecord();
      if (transactionRecord == null) {
         return UserManager.getUser(SystemUser.OseeSystem);
      }
      return UserManager.getUserByArtId(transactionRecord.getAuthor());
   }

   void meetMinimumAttributeCounts(boolean isNewArtifact) throws OseeCoreException {
      if (modType == ModificationType.DELETED) {
         return;
      }
      for (AttributeType attributeType : getAttributeTypes()) {
         int missingCount = attributeType.getMinOccurrences() - getAttributeCount(attributeType);
         for (int i = 0; i < missingCount; i++) {
            initializeAttribute(attributeType, ModificationType.NEW, isNewArtifact, true);
         }
      }
   }

   public ModificationType getModType() {
      return modType;
   }

   @Override
   public Artifact getFullArtifact() {
      return this;
   }

   public DefaultBasicGuidArtifact getBasicGuidArtifact() {
      return new DefaultBasicGuidArtifact(getBranch().getGuid(), getArtifactType().getGuid(), getGuid());
   }

   @Override
   public String getArtTypeGuid() {
      return getArtifactType().getGuid();
   }

   @Override
   public String getBranchGuid() {
      return getBranch().getGuid();
   }

   public Set<DefaultBasicGuidRelationReorder> getRelationOrderRecords() {
      return relationOrderRecords;
   }
}
