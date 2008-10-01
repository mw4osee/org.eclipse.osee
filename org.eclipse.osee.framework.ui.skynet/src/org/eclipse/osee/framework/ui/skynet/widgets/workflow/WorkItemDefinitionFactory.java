/*
 * Created on May 28, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.ui.skynet.widgets.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.relation.CoreRelationEnumeration;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkItemDefinition.WriteType;

/**
 * @author Donald G. Dunne
 */
public class WorkItemDefinitionFactory {

   private static Map<String, WorkItemDefinition> itemIdToDefinition;
   private static Map<String, Artifact> itemIdToWidArtifact;

   public static void clearCache() {
      itemIdToDefinition = null;
      itemIdToWidArtifact = null;
   }

   public synchronized static void loadDefinitions() throws OseeCoreException {
      if (itemIdToDefinition == null) {
         OSEELog.logInfo(SkynetGuiPlugin.class, "Loading Work Item Definitions", false);
         itemIdToDefinition = new HashMap<String, WorkItemDefinition>();
         itemIdToWidArtifact = new HashMap<String, Artifact>();

         // Add all work item definitions provided through extension points
         for (IWorkDefinitionProvider provider : WorkDefinitionProvider.getWorkDefinitionProviders()) {
            for (WorkItemDefinition def : provider.getProgramaticWorkItemDefinitions()) {
               addItemDefinition(WriteType.New, def);
            }
         }

         // This load is faster than loading each by artifact type
         for (Artifact art : ArtifactQuery.getArtifactsFromAttributeType(
               WorkItemAttributes.WORK_ID.getAttributeTypeName(), BranchPersistenceManager.getCommonBranch())) {
            if (art.getArtifactTypeName().equals(WorkRuleDefinition.ARTIFACT_NAME)) {
               addItemDefinition(WriteType.New, new WorkRuleDefinition(art), art);
            } else if (art.getArtifactTypeName().equals(WorkWidgetDefinition.ARTIFACT_NAME)) {
               addItemDefinition(WriteType.New, new WorkWidgetDefinition(art), art);
            } else if (art.getArtifactTypeName().equals(WorkPageDefinition.ARTIFACT_NAME)) {
               addItemDefinition(WriteType.New, new WorkPageDefinition(art), art);
            } else if (art.getArtifactTypeName().equals(WorkFlowDefinition.ARTIFACT_NAME)) {
               addItemDefinition(WriteType.New, new WorkFlowDefinition(art), art);
            }
         }
      }
   }

   /**
    * This should only be called on database initialization or when new work item definitions are created during
    * run-time.
    * 
    * @param workItemDefinition
    * @param artifact
    */
   public static void cacheWorkItemDefinitionArtifact(WriteType writeType, WorkItemDefinition workItemDefinition, Artifact artifact) {
      addItemDefinition(writeType, workItemDefinition, artifact);
   }

   public static void relateWorkItemDefinitions(String parentWorkflowId, String childWorkflowId) throws OseeCoreException {
      List<Artifact> parentArts =
            ArtifactQuery.getArtifactsFromAttribute(WorkItemAttributes.WORK_ID.getAttributeTypeName(),
                  parentWorkflowId, BranchPersistenceManager.getCommonBranch());
      if (parentArts == null || parentArts.size() == 0) {
         throw new IllegalArgumentException("Can't access parentWorkflowId " + parentWorkflowId);
      }
      Artifact parentArt = parentArts.iterator().next();
      List<Artifact> childArts =
            ArtifactQuery.getArtifactsFromAttribute(WorkItemAttributes.WORK_ID.getAttributeTypeName(), childWorkflowId,
                  BranchPersistenceManager.getCommonBranch());
      if (childArts == null || childArts.size() == 0) {
         throw new IllegalArgumentException("Can't access childWorkflowId " + childWorkflowId);
      }
      Artifact childArt = childArts.iterator().next();
      if (!parentArt.getRelatedArtifacts(CoreRelationEnumeration.WorkItem__Child, Artifact.class).contains(childArt)) {
         parentArt.addRelation(CoreRelationEnumeration.WorkItem__Child, childArt);
         parentArt.persistRelations();
      }
   }

   private static void addItemDefinition(WriteType writeType, WorkItemDefinition workItemDefinition) {
      if (workItemDefinition.getId() == null) throw new IllegalArgumentException("Item Id can't be null");
      if (writeType == WriteType.New && itemIdToDefinition.containsKey(workItemDefinition.getId())) throw new IllegalArgumentException(
            "Item Id must be unique.  Already work item with id \"" + workItemDefinition.getId() + "\"");
      itemIdToDefinition.put(workItemDefinition.getId(), workItemDefinition);
   }

   private static void addItemDefinition(WriteType writeType, WorkItemDefinition workItemDefinition, Artifact artifact) {
      addItemDefinition(writeType, workItemDefinition);
      itemIdToWidArtifact.put(workItemDefinition.id, artifact);
   }

   public static void loadDefinitions(Collection<Artifact> arts) throws OseeCoreException {
      for (Artifact art : arts) {
         if (art.getArtifactTypeName().equals(WorkRuleDefinition.ARTIFACT_NAME)) {
            System.out.println("Updating WorkItemDefinition cache with " + art);
            addItemDefinition(WriteType.New, new WorkRuleDefinition(art), art);
         }
         if (art.getArtifactTypeName().equals(WorkWidgetDefinition.ARTIFACT_NAME)) {
            System.out.println("Updating WorkItemDefinition cache with " + art);
            addItemDefinition(WriteType.New, new WorkWidgetDefinition(art), art);
         }
         if (art.getArtifactTypeName().equals(WorkPageDefinition.ARTIFACT_NAME)) {
            System.out.println("Updating WorkItemDefinition cache with " + art);
            addItemDefinition(WriteType.New, new WorkPageDefinition(art), art);
         }
         if (art.getArtifactTypeName().equals(WorkFlowDefinition.ARTIFACT_NAME)) {
            System.out.println("Updating WorkItemDefinition cache with " + art);
            addItemDefinition(WriteType.New, new WorkFlowDefinition(art), art);
         }
      }
   }

   public static WorkItemDefinition getWorkItemDefinition(String id) throws OseeCoreException {
      if (id == null) throw new IllegalStateException("WorkItemDefinition id can't be null");
      loadDefinitions();
      WorkItemDefinition wid = itemIdToDefinition.get(id);
      if (wid == null) {
         // Attempt to get from DB
         loadDefinitions(ArtifactQuery.getArtifactsFromAttribute(WorkItemAttributes.WORK_ID.getAttributeTypeName(), id,
               BranchPersistenceManager.getAtsBranch()));
      }
      return itemIdToDefinition.get(id);
   }

   public static Artifact getWorkItemDefinitionArtifact(String id) throws OseeCoreException {
      if (id == null) throw new IllegalStateException("WorkItemDefinition id can't be null");
      loadDefinitions();
      Artifact art = itemIdToWidArtifact.get(id);
      if (art == null) {
         // Attempt to get from DB
         loadDefinitions(ArtifactQuery.getArtifactsFromAttribute(WorkItemAttributes.WORK_ID.getAttributeTypeName(), id,
               BranchPersistenceManager.getAtsBranch()));
      }
      return itemIdToWidArtifact.get(id);
   }

   public static List<WorkItemDefinition> getWorkItemDefinition(java.util.Collection<String> ids) throws OseeCoreException {
      loadDefinitions();
      List<WorkItemDefinition> defs = new ArrayList<WorkItemDefinition>();
      for (String id : ids) {
         WorkItemDefinition def = getWorkItemDefinition(id);
         if (def == null) throw new IllegalArgumentException("Work Item Id \"" + id + "\" is not a defined work item");
         defs.add(def);
      }
      return defs;
   }

   public static List<WorkItemDefinition> getWorkItemDefinitionsStartsWithId(String id) throws OseeCoreException {
      loadDefinitions();
      List<WorkItemDefinition> defs = new ArrayList<WorkItemDefinition>();
      for (Entry<String, WorkItemDefinition> entry : itemIdToDefinition.entrySet()) {
         if (entry.getKey().startsWith(id)) {
            defs.add(entry.getValue());
         }
      }
      return defs;
   }

   /**
    * Call to get dynamic definitions based on data specified. This is intended for extenders to be able to provide
    * widgets that are either conditionally added or are configured dynamically based on dynamic circumstances
    * 
    * @param data
    * @return list of WorkItemDefinitions
    */
   public static List<WorkItemDefinition> getDynamicWorkItemDefintions(WorkFlowDefinition workFlowDefinition, WorkPageDefinition workPageDefinition, Object data) throws OseeCoreException {
      List<WorkItemDefinition> dynamicDefinitions = new ArrayList<WorkItemDefinition>();
      for (IWorkDefinitionProvider provider : WorkDefinitionProvider.getWorkDefinitionProviders()) {
         dynamicDefinitions.addAll(provider.getDynamicWorkItemDefinitionsForPage(workFlowDefinition,
               workPageDefinition, data));
      }
      return dynamicDefinitions;
   }

   public static List<WorkItemDefinition> getWorkItemDefinitions(Collection<String> pageids) throws OseeCoreException {
      loadDefinitions();
      List<WorkItemDefinition> defs = new ArrayList<WorkItemDefinition>();
      for (String itemId : pageids) {
         WorkItemDefinition def = getWorkItemDefinition(itemId);
         if (def == null) throw new IllegalArgumentException("Item Id \"" + itemId + "\" is not a defined item");
         defs.add(def);
      }
      return defs;
   }

   public static Collection<WorkItemDefinition> getWorkItemDefinitions() throws OseeCoreException {
      loadDefinitions();
      return itemIdToDefinition.values();
   }

}
