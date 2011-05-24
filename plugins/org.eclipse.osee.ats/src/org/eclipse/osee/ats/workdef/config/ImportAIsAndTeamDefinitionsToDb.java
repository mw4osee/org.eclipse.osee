/*
 * Created on Feb 2, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.workdef.config;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.emf.common.util.EList;
import org.eclipse.osee.ats.core.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.core.config.TeamDefinitionManager;
import org.eclipse.osee.ats.core.type.AtsArtifactTypes;
import org.eclipse.osee.ats.core.type.AtsAttributeTypes;
import org.eclipse.osee.ats.core.type.AtsRelationTypes;
import org.eclipse.osee.ats.core.workdef.provider.BooleanDefUtil;
import org.eclipse.osee.ats.core.workdef.provider.UserRefUtil;
import org.eclipse.osee.ats.core.workflow.ActionableItemManagerCore;
import org.eclipse.osee.ats.dsl.atsDsl.ActionableItemDef;
import org.eclipse.osee.ats.dsl.atsDsl.AtsDsl;
import org.eclipse.osee.ats.dsl.atsDsl.TeamDef;
import org.eclipse.osee.ats.dsl.atsDsl.UserDef;
import org.eclipse.osee.ats.dsl.atsDsl.VersionDef;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.data.IUserToken;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

public class ImportAIsAndTeamDefinitionsToDb {

   private final AtsDsl atsDsl;
   private final SkynetTransaction transaction;
   private final Map<String, Artifact> newTeams = new HashMap<String, Artifact>();
   private final Map<String, Artifact> newAIs = new HashMap<String, Artifact>();
   private final Map<String, Artifact> newVersions = new HashMap<String, Artifact>();
   private final String modelName;

   public ImportAIsAndTeamDefinitionsToDb(String modelName, AtsDsl atsDsl, SkynetTransaction transaction) {
      this.modelName = modelName;
      this.atsDsl = atsDsl;
      this.transaction = transaction;
   }

   public void execute() throws OseeCoreException {
      importUserDefinitions(atsDsl.getUserDef());
      importTeamDefinitions(atsDsl.getTeamDef(), TeamDefinitionManager.getTopTeamDefinition());
      importActionableItems(atsDsl.getActionableItemDef(), ActionableItemManagerCore.getTopActionableItem());
   }

   public void importUserDefinitions(EList<UserDef> userDefs) throws OseeCoreException {
      for (UserDef dslUserDef : userDefs) {
         String dslUserName = Strings.unquote(dslUserDef.getName());
         Artifact userArt = null;
         if (dslUserDef.getUserDefOption().contains("GetOrCreate")) {
            userArt = UserManager.createUser(getOseeUser(dslUserDef), transaction);
         }
         if (userArt == null) {
            userArt = ArtifactTypeManager.addArtifact(CoreArtifactTypes.User, AtsUtil.getAtsBranch(), dslUserName);
         }
         if (userArt == null) {
            throw new OseeStateException(String.format("No user found in datbase with name [%s] from [%s]",
               dslUserName, modelName), modelName);
         }
      }
   }

   private IUserToken getOseeUser(final UserDef dslUserDef) {
      return TokenFactory.createUserToken(GUID.create(), Strings.unquote(dslUserDef.getName()),
         Strings.isValid(dslUserDef.getEmail()) ? dslUserDef.getEmail() : Strings.unquote(dslUserDef.getName()),
         Strings.isValid(dslUserDef.getUserId()) ? dslUserDef.getUserId() : Strings.unquote(dslUserDef.getName()), BooleanDefUtil.get(dslUserDef.getActive(), true),
         false, true);
   }

   public void importTeamDefinitions(EList<TeamDef> teamDefs, Artifact parentArtifact) throws OseeCoreException {
      for (TeamDef dslTeamDef : teamDefs) {
         String dslTeamName = Strings.unquote(dslTeamDef.getName());
         //         System.out.println("   - Importing Team " + dslTeamName);
         Artifact newTeam = null;
         if (dslTeamDef.getTeamDefOption().contains("GetOrCreate")) {
            newTeam = getOrCreate(dslTeamName, true, parentArtifact);
         }
         if (newTeam == null) {
            newTeam =
               ArtifactTypeManager.addArtifact(AtsArtifactTypes.TeamDefinition, AtsUtil.getAtsBranch(), dslTeamName);
         }
         if (parentArtifact != null && !parentArtifact.equals(newTeam)) {
            parentArtifact.addChild(newTeam);
         }
         newTeams.put(newTeam.getName(), newTeam);

         newTeam.getAttributes(AtsAttributeTypes.Active).iterator().next().setValue(
            BooleanDefUtil.get(dslTeamDef.getActive(), true));
         //         newTeam.setSoleAttributeValue(CoreAttributeTypes.Active, BooleanDefUtil.get(dslTeamDef.getActive(), true));
         boolean configuredForTeamUsesVersions = BooleanDefUtil.get(dslTeamDef.getUsesVersions(), false);
         boolean hasVersions = dslTeamDef.getVersion().size() > 0;
         newTeam.setSoleAttributeValue(AtsAttributeTypes.TeamUsesVersions, configuredForTeamUsesVersions | hasVersions);
         for (String staticId : dslTeamDef.getStaticId()) {
            newTeam.setSingletonAttributeValue(CoreAttributeTypes.StaticId, staticId);
         }
         for (Artifact user : UserRefUtil.getUsers(dslTeamDef.getLead())) {
            newTeam.addRelation(AtsRelationTypes.TeamLead_Lead, user);
         }
         for (Artifact user : UserRefUtil.getUsers(dslTeamDef.getMember())) {
            newTeam.addRelation(AtsRelationTypes.TeamMember_Member, user);
         }
         for (Artifact user : UserRefUtil.getUsers(dslTeamDef.getPriviledged())) {
            newTeam.addRelation(AtsRelationTypes.PrivilegedMember_Member, user);
         }
         if (Strings.isValid(dslTeamDef.getWorkDefinition())) {
            newTeam.setSoleAttributeValue(AtsAttributeTypes.WorkflowDefinition, dslTeamDef.getWorkDefinition());
         }
         if (Strings.isValid(dslTeamDef.getRelatedTaskWorkDefinition())) {
            newTeam.setSoleAttributeValue(AtsAttributeTypes.RelatedTaskWorkDefinition,
               dslTeamDef.getRelatedTaskWorkDefinition());
         }
         importAccessContextIds(newTeam, dslTeamDef.getAccessContextId());
         importVersionDefinitions(dslTeamDef.getVersion(), (TeamDefinitionArtifact) newTeam);
         // process children
         importTeamDefinitions(dslTeamDef.getChildren(), newTeam);
         newTeam.persist(transaction);
      }

   }

   private void importAccessContextIds(Artifact teamOrAi, EList<String> contextIds) throws OseeCoreException {
      for (String accessContextId : contextIds) {
         teamOrAi.addAttribute(CoreAttributeTypes.AccessContextId, accessContextId);
      }
   }

   public void importVersionDefinitions(EList<VersionDef> versionDefs, TeamDefinitionArtifact teamDef) throws OseeCoreException {
      Map<String, Artifact> nameToVerArt = new HashMap<String, Artifact>();
      for (VersionDef dslVersionDef : versionDefs) {
         String dslVerName = Strings.unquote(dslVersionDef.getName());
         // System.out.println("   - Importing Version " + dslVerName);
         Artifact newVer =
            ArtifactTypeManager.addArtifact(AtsArtifactTypes.Version, AtsUtil.getAtsBranch(), dslVerName);
         teamDef.addRelation(AtsRelationTypes.TeamDefinitionToVersion_Version, newVer);
         nameToVerArt.put(newVer.getName(), newVer);
         newVersions.put(newVer.getName(), newVer);
         newVer.setSoleAttributeValue(CoreAttributeTypes.Active, BooleanDefUtil.get(dslVersionDef.getActive(), true));
         newVer.setSoleAttributeValue(AtsAttributeTypes.AllowCommitBranch,
            BooleanDefUtil.get(dslVersionDef.getAllowCommitBranch(), true));
         newVer.setSoleAttributeValue(AtsAttributeTypes.AllowCreateBranch,
            BooleanDefUtil.get(dslVersionDef.getAllowCreateBranch(), true));
         newVer.setSoleAttributeValue(AtsAttributeTypes.NextVersion, BooleanDefUtil.get(dslVersionDef.getNext(), false));
         newVer.setSoleAttributeValue(AtsAttributeTypes.Released,
            BooleanDefUtil.get(dslVersionDef.getReleased(), false));
         if (Strings.isValid(dslVersionDef.getBaselineBranchGuid())) {
            newVer.setSoleAttributeValue(AtsAttributeTypes.BaselineBranchGuid, dslVersionDef.getBaselineBranchGuid());
         }
         for (String staticId : dslVersionDef.getStaticId()) {
            newVer.setSingletonAttributeValue(CoreAttributeTypes.StaticId, staticId);
         }
      }
      // Handle parallel versions
      for (VersionDef dslVersionDef : versionDefs) {
         String aiName = Strings.unquote(dslVersionDef.getName());
         Artifact verArt = nameToVerArt.get(aiName);
         for (String parallelVerStr : dslVersionDef.getParallelVersion()) {
            // System.out.println(String.format("   - Importing Parallel Version [%s] -> Child [%s]", aiName, parallelVerStr));
            Artifact childArt = nameToVerArt.get(parallelVerStr);
            verArt.addRelation(AtsRelationTypes.ParallelVersion_Child, childArt);
         }
      }
   }

   public void importActionableItems(EList<ActionableItemDef> aiDefs, Artifact parentArtifact) throws OseeCoreException {
      for (ActionableItemDef dslAIDef : aiDefs) {
         String dslAIName = Strings.unquote(dslAIDef.getName());
         // System.out.println("   - Importing Actionable Item " + dslAIName);
         Artifact newAi = null;
         if (dslAIDef.getAiDefOption().contains("GetOrCreate")) {
            newAi = getOrCreate(dslAIName, false, parentArtifact);
         }
         if (newAi == null) {
            newAi = ArtifactTypeManager.addArtifact(AtsArtifactTypes.ActionableItem, AtsUtil.getAtsBranch(), dslAIName);
         }
         if (parentArtifact != null && !parentArtifact.equals(newAi)) {
            parentArtifact.addChild(newAi);
         }
         newAIs.put(newAi.getName(), newAi);
         newAi.getAttributes(AtsAttributeTypes.Active).iterator().next().setValue(
            BooleanDefUtil.get(dslAIDef.getActive(), true));
         //         newAi.setSoleAttributeValue(CoreAttributeTypes.Active, BooleanDefUtil.get(dslAIDef.getActive(), true));
         newAi.setSoleAttributeValue(AtsAttributeTypes.Actionable, BooleanDefUtil.get(dslAIDef.getActionable(), true));
         for (String staticId : dslAIDef.getStaticId()) {
            newAi.setSingletonAttributeValue(CoreAttributeTypes.StaticId, staticId);
         }
         for (Artifact user : UserRefUtil.getUsers(dslAIDef.getLead())) {
            newAi.addRelation(AtsRelationTypes.TeamLead_Lead, user);
         }
         if (dslAIDef.getTeamDef() != null) {
            if (dslAIDef.getTeamDef() == null) {
               throw new OseeStateException(String.format("No Team Definition defined for Actionable Item [%s]",
                  dslAIName));
            }
            newAi.addRelation(AtsRelationTypes.TeamActionableItem_Team, newTeams.get(dslAIDef.getTeamDef()));
         }
         importAccessContextIds(newAi, dslAIDef.getAccessContextId());
         importActionableItems(dslAIDef.getChildren(), newAi);
         newAi.persist(transaction);
      }
   }

   private Artifact getOrCreate(String artifactName, boolean isTeamDef, Artifact parentArtifact) throws OseeCoreException {
      Artifact parent = parentArtifact;
      if (parent == null) {
         if (isTeamDef) {
            parent = TeamDefinitionManager.getTopTeamDefinition();
         } else {
            parent = ActionableItemManagerCore.getTopActionableItem();
         }
      }
      if (parent.getName().equals(artifactName)) {
         return parent;
      }
      for (Artifact child : parent.getChildren()) {
         if (isTeamDef && !(child.isOfType(AtsArtifactTypes.TeamDefinition))) {
            continue;
         }
         if (!isTeamDef && !(child.isOfType(AtsArtifactTypes.ActionableItem))) {
            continue;
         }
         if (child.getName().equals(artifactName)) {
            return child;
         }
      }
      return null;
   }

}
