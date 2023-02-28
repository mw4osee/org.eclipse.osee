/*********************************************************************
 * Copyright (c) 2016 Boeing
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

package org.eclipse.osee.orcs.rest.internal.applicability;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import org.eclipse.osee.framework.core.applicability.ApplicabilityUseResultToken;
import org.eclipse.osee.framework.core.applicability.FeatureDefinition;
import org.eclipse.osee.framework.core.applicability.ProductTypeDefinition;
import org.eclipse.osee.framework.core.data.ApplicabilityData;
import org.eclipse.osee.framework.core.data.ApplicabilityId;
import org.eclipse.osee.framework.core.data.ApplicabilityToken;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.data.ArtifactTypeToken;
import org.eclipse.osee.framework.core.data.AttributeTypeToken;
import org.eclipse.osee.framework.core.data.BlockApplicabilityStageRequest;
import org.eclipse.osee.framework.core.data.Branch;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.ConfigurationGroupDefinition;
import org.eclipse.osee.framework.core.data.CreateViewDefinition;
import org.eclipse.osee.framework.core.data.GammaId;
import org.eclipse.osee.framework.core.data.OseeClient;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.enums.CoreTupleTypes;
import org.eclipse.osee.framework.core.enums.CoreUserGroups;
import org.eclipse.osee.framework.jdk.core.result.XResultData;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.OseeProperties;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.OrcsApplicability;
import org.eclipse.osee.orcs.rest.internal.writer.ApplicabilityFeatureMatrixStreamingOutput;
import org.eclipse.osee.orcs.rest.model.ApplicabilityEndpoint;
import org.eclipse.osee.orcs.search.ApplicabilityQuery;
import org.eclipse.osee.orcs.transaction.TransactionBuilder;

/**
 * @author Donald G. Dunne
 */
public class ApplicabilityEndpointImpl implements ApplicabilityEndpoint {

   private final OrcsApi orcsApi;
   private final BranchId branch;
   private final ApplicabilityQuery applicabilityQuery;
   private final OrcsApplicability ops;

   public ApplicabilityEndpointImpl(OrcsApi orcsApi, BranchId branch) {
      this.orcsApi = orcsApi;
      this.branch = branch;
      this.applicabilityQuery = orcsApi.getQueryFactory().applicabilityQuery();
      ops = orcsApi.getApplicabilityOps();
   }

   @Override
   public Collection<ApplicabilityToken> getApplicabilityTokens() {
      return applicabilityQuery.getApplicabilityTokens(branch).values();
   }

   @Override
   public List<Pair<ArtifactId, ApplicabilityToken>> getApplicabilityTokens(List<? extends ArtifactId> artIds) {
      return applicabilityQuery.getApplicabilityTokens(artIds, branch);
   }

   @Override
   public ApplicabilityToken getApplicabilityToken(ArtifactId artId) {
      return applicabilityQuery.getApplicabilityToken(artId, branch);
   }

   @Override
   public List<ApplicabilityId> getApplicabilitiesReferenced(ArtifactId artifact) {
      return applicabilityQuery.getApplicabilitiesReferenced(artifact, branch);
   }

   public List<Pair<ArtifactId, ApplicabilityToken>> getApplicabilityTokensForArts(Collection<? extends ArtifactId> artIds) {
      List<Pair<ArtifactId, ApplicabilityToken>> artToApplicToken = new ArrayList<>();
      for (ArtifactId artId : artIds) {
         artToApplicToken.add(new Pair<>(artId, getApplicabilityToken(artId)));
      }
      return artToApplicToken;
   }

   @Override
   public List<ApplicabilityToken> getApplicabilityReferenceTokens(ArtifactId artifact) {
      return applicabilityQuery.getApplicabilityReferenceTokens(artifact, branch);
   }

   @Override
   public List<ApplicabilityToken> getViewApplicabilityTokens(ArtifactId view) {
      return applicabilityQuery.getViewApplicabilityTokens(view, branch);
   }

   @Override
   public List<ArtifactToken> getViews() {
      return applicabilityQuery.getViewsForBranch(branch);
   }

   @Override
   public List<ArtifactToken> getCfgGroup() {
      return applicabilityQuery.getConfigurationGroupsForBranch(branch);
   }

   @Override
   public List<FeatureDefinition> getFeatureDefinitionData() {
      return applicabilityQuery.getFeatureDefinitionData(branch);
   }

   @Override
   public List<BranchId> getAffectedBranches(Long injectDateMs, Long removalDateMs, List<ApplicabilityId> applicabilityIds) {
      return applicabilityQuery.getAffectedBranches(injectDateMs, removalDateMs, applicabilityIds, branch);
   }

   @Override
   public List<BranchId> getAffectedBranches(TransactionId injectionTx, TransactionId removalTx, List<ApplicabilityId> applicabilityIds) {
      return applicabilityQuery.getAffectedBranches(injectionTx, removalTx, applicabilityIds, branch);
   }

   @Override
   public TransactionToken setApplicability(ApplicabilityId applicId, List<? extends ArtifactId> artifacts) {
      XResultData access = isAccess();
      if (access.isErrors()) {
         return TransactionToken.SENTINEL;
      }
      TransactionBuilder tx =
         orcsApi.getTransactionFactory().createTransaction(branch, "Set Applicability Ids for Artifacts");
      tx.setApplicability(applicId, artifacts);
      return tx.commit();
   }

   @Override
   public TransactionToken setApplicabilityByString(String applicTag, List<? extends ArtifactId> artifacts) {
      XResultData access = isAccess();
      if (access.isErrors()) {
         return TransactionToken.SENTINEL;
      }
      TransactionBuilder tx =
         orcsApi.getTransactionFactory().createTransaction(branch, "Set Applicability Ids for Artifacts");
      Long putIfAbsent = orcsApi.getKeyValueOps().putIfAbsent(applicTag);
      ApplicabilityToken applicId = new ApplicabilityToken(putIfAbsent, applicTag);
      tx.setApplicability(applicId, artifacts);
      return tx.commit();
   }

   @Override
   public TransactionToken setApplicabilityReference(List<ApplicabilityData> appDatas) {
      XResultData access = isAccess();
      if (access.isErrors()) {
         return TransactionToken.SENTINEL;
      }
      TransactionBuilder tx =
         orcsApi.getTransactionFactory().createTransaction(branch, "Set Reference Applicability Ids for Artifacts");
      HashMap<ArtifactId, List<ApplicabilityId>> artToApplMap = new HashMap<ArtifactId, List<ApplicabilityId>>();
      for (ApplicabilityData data : appDatas) {
         artToApplMap.put(data.getArtifact(), Collections.castAll(data.getApplIds()));
      }
      tx.setApplicabilityReference(artToApplMap);
      return tx.commit();
   }

   @Override
   public XResultData createApplicabilityForView(ArtifactId viewId, String applicability) {
      XResultData results = isAccess();
      if (results.isErrors()) {
         return results;
      }
      return ops.createApplicabilityForView(viewId, applicability, branch);
   }

   @Override
   public XResultData createCompoundApplicabilityForBranch(String applicability) {
      XResultData results = isAccess();
      if (results.isErrors()) {
         return results;
      }
      return ops.createCompoundApplicabilityForBranch(applicability, branch);
   }

   @Override
   public XResultData deleteCompoundApplicabilityFromBranch(ApplicabilityId compApplicId) {
      XResultData results = isAccess();
      if (results.isErrors()) {
         return results;
      }
      return ops.deleteCompoundApplicabilityFromBranch(compApplicId, branch);
   }

   @Override
   public XResultData validate() {
      XResultData results = isAccess();
      results = ops.validateCompoundApplicabilities(branch, false);
      return results;
   }

   @Override
   public XResultData removeApplicabilityFromView(ArtifactId viewId, String applicability) {
      orcsApi.userService().requireRole(CoreUserGroups.OseeAccessAdmin);
      XResultData results = isAccess();
      if (results.isErrors()) {
         return results;
      }

      return ops.removeApplicabilityFromView(branch, viewId, applicability);
   }

   @Override
   public void addMissingApplicabilityFromParentBranch() {
      orcsApi.getBranchOps().addMissingApplicabilityFromParentBranch(branch);
   }

   @Override
   public ArtifactId getVersionConfig(ArtifactId version) {
      return applicabilityQuery.getVersionConfig(version, branch);
   }

   @Override
   public String getViewTable(String filter) {
      return applicabilityQuery.getViewTable(branch, filter);
   }

   @Override
   public String getConfigMatrix(String matrixType, String filter) {
      String mType = "all";
      if (matrixType != null) {
         mType = matrixType;
      }
      return applicabilityQuery.getConfigMatrix(branch, mType, filter);
   }

   @Override
   public FeatureDefinition getFeature(String featureNameOrId) {
      return ops.getFeature(featureNameOrId, branch);
   }

   @Override
   public XResultData updateFeature(FeatureDefinition feature) {
      XResultData access = isAccess();
      if (access.isErrors()) {
         return access;
      }
      return ops.updateFeature(feature, branch);
   }

   @Override
   public XResultData createFeature(FeatureDefinition feature) {
      XResultData access = isAccess();
      if (access.isErrors()) {
         return access;
      }
      return ops.createFeature(feature, branch);
   }

   @Override
   public XResultData deleteFeature(ArtifactId feature) {
      orcsApi.userService().requireRole(CoreUserGroups.OseeAccessAdmin);

      XResultData access = isAccess();
      if (access.isErrors()) {
         return access;
      }
      return ops.deleteFeature(feature, branch);
   }

   @Override
   public XResultData updateView(CreateViewDefinition view) {
      XResultData access = isAccess();
      if (access.isErrors()) {
         return access;
      }
      return ops.updateView(view, branch);
   }

   @Override
   public XResultData createView(CreateViewDefinition view) {
      XResultData access = isAccess();
      if (access.isErrors()) {
         return access;
      }
      return ops.createView(view, branch);
   }

   @Override
   public XResultData deleteView(@PathParam("id") String id) {
      orcsApi.userService().requireRole(CoreUserGroups.OseeAccessAdmin);

      XResultData access = isAccess();
      if (access.isErrors()) {
         return access;
      }
      return ops.deleteView(id, branch);
   }

   @Override
   public CreateViewDefinition getView(String id) {
      return ops.getView(id, branch);
   }

   @Override
   public XResultData isAccess() {
      XResultData rd = new XResultData();
      if (OseeProperties.isInTest()) {
         rd.logf("Access granted to branch cause isInTest");
      }
      Branch brch = orcsApi.getQueryFactory().branchQuery().andId(branch).getResults().getAtMostOneOrNull();
      if (brch.getBranchType() == BranchType.WORKING) {
         rd.log("Access granted to working branch");
      } else {
         rd.error("Access denied to non-working branch");
      }
      return rd;
   }

   @Override
   public XResultData createCfgGroup(ConfigurationGroupDefinition group) {
      XResultData access = isAccess();
      if (access.isErrors()) {
         return access;
      }
      return ops.createCfgGroup(group, branch);
   }

   @Override
   public XResultData updateCfgGroup(ConfigurationGroupDefinition group) {
      XResultData access = isAccess();
      if (access.isErrors()) {
         return access;
      }
      return ops.updateCfgGroup(group, branch);
   }

   @Override
   public XResultData relateCfgGroupToView(String groupId, String viewId) {
      orcsApi.userService().requireRole(CoreUserGroups.OseeAccessAdmin);

      XResultData access = isAccess();
      if (access.isErrors()) {
         return access;
      }
      return ops.relateCfgGroupToView(groupId, viewId, branch);
   }

   @Override
   public XResultData unrelateCfgGroupToView(String groupId, String viewId) {
      XResultData access = isAccess();
      if (access.isErrors()) {
         return access;
      }
      return ops.unrelateCfgGroupToView(groupId, viewId, branch);
   }

   @Override
   public XResultData deleteCfgGroup(String id) {
      XResultData access = isAccess();
      if (access.isErrors()) {
         return access;
      }
      return ops.deleteCfgGroup(id, branch);
   }

   @Override
   public XResultData syncCfgGroup(String id) {
      XResultData access = isAccess();
      if (access.isErrors()) {
         return access;
      }
      return ops.syncConfigGroup(branch, id, null);

   }

   @Override
   public XResultData syncCfgGroup() {
      XResultData access = isAccess();
      if (access.isErrors()) {
         return access;
      }
      return ops.syncConfigGroup(branch);

   }

   @Override
   public Response getFeatureMatrixExcel(BranchId branchId, String filter) {
      StreamingOutput streamingOutput = new ApplicabilityFeatureMatrixStreamingOutput(orcsApi, branchId, filter);
      ResponseBuilder builder = Response.ok(streamingOutput);
      builder.header("Content-Disposition", "attachment; filename=" + "FeatureMatrix.xml");
      return builder.build();
   }

   @Override
   public XResultData applyBlockVisibility(BlockApplicabilityStageRequest data) {
      return ops.applyApplicabilityToFiles(data, branch);
   }

   @Override
   public XResultData refreshStagedFiles(BlockApplicabilityStageRequest data) {
      return ops.refreshStagedFiles(data, branch);
   }

   @Override
   public XResultData startBlockVisibilityWatcher(BlockApplicabilityStageRequest data) {
      return ops.startWatcher(data, branch);
   }

   @Override
   public XResultData stopBlockVisibilityWatcher() {
      return ops.stopWatcher();
   }

   @Override
   public ConfigurationGroupDefinition getConfigurationGroup(String id) {
      return ops.getConfigurationGroup(id, branch);
   }

   @Override
   public ApplicabilityToken getApplicabilityTokenFromId(String id) {
      Optional<ApplicabilityToken> findAny =
         getApplicabilityTokens().stream().filter(a -> a.getIdString().equals(id)).findAny();
      if (findAny.isPresent()) {
         return findAny.get();
      }
      return ApplicabilityToken.SENTINEL;
   }

   @Override
   public List<ApplicabilityUseResultToken> getApplicabilityUsage(String applic, List<ArtifactTypeToken> artTypes, List<AttributeTypeToken> attrTypes) {

      return applicabilityQuery.getApplicabilityUsage(branch, applic, artTypes, attrTypes);

   }

   @Override
   public XResultData createProductType(ProductTypeDefinition productType) {
      XResultData access = isAccess();
      if (access.isErrors()) {
         return access;
      }
      return ops.createProductType(productType, branch);
   }

   @Override
   public ProductTypeDefinition getProductType(String id) {
      return ops.getProductType(id, branch);
   }

   @Override
   public Collection<ProductTypeDefinition> getProductTypes(long pageNum, long pageSize, AttributeTypeToken orderByAttributeType) {
      return ops.getProductTypeDefinitions(branch, pageNum, pageSize, orderByAttributeType);
   }

   @Override
   public XResultData updateProductType(ProductTypeDefinition productType) {
      XResultData access = isAccess();
      if (access.isErrors()) {
         return access;
      }
      return ops.updateProductType(productType, branch);
   }

   @Override
   public XResultData deleteProductType(ArtifactId id) {
      XResultData access = isAccess();
      if (access.isErrors()) {
         return access;
      }
      return ops.deleteProductType(id, branch);
   }

   @Override
   public String uploadBlockApplicability(InputStream zip) {
      return ops.uploadBlockApplicability(zip);
   }

   @Override
   public XResultData applyBlockVisibilityOnServer(String blockApplicId, BlockApplicabilityStageRequest data) {
      return ops.applyBlockVisibilityOnServer(blockApplicId, data, branch);
   }

   @Override
   public Response downloadBlockApplicability(String blockApplicId) {
      String serverDataPath = System.getProperty(OseeClient.OSEE_APPLICATION_SERVER_DATA);
      if (serverDataPath == null) {
         serverDataPath = System.getProperty("user.home");
      }
      File serverApplicDir = new File(serverDataPath + File.separator + "blockApplicability");
      if (!serverApplicDir.isDirectory()) {
         return Response.noContent().build();
      }

      File stagingZip = new File(String.format("%s%s%s%sstaging.zip", serverApplicDir.getPath(), File.separator,
         blockApplicId, File.separator));

      if (stagingZip.exists()) {
         try {
            return Response.ok(Files.readAllBytes(stagingZip.toPath())).type("application/zip").header(
               "Content-Disposition", "attachment; filename=\"staging.zip\"").build();
         } catch (IOException ex) {
            return Response.serverError().build();
         }
      }
      return Response.noContent().build();
   }

   @Override
   public XResultData deleteBlockApplicability(String blockApplicId) {
      return ops.deleteBlockApplicability(blockApplicId);
   }

   @Override
   public Response uploadRunBlockApplicability(Long view, InputStream zip) {
      String id = ops.uploadRunBlockApplicability(view, zip, branch);

      return downloadBlockApplicability(id);
   }

   @Override
   public XResultData addApplicabilityConstraint(ApplicabilityId applicability1, ApplicabilityId applicability2) {
      XResultData response = new XResultData();
      TransactionBuilder tx = orcsApi.getTransactionFactory().createTransaction(branch, "Add Applicability Constraint");
      GammaId gamma = GammaId.SENTINEL;
      if (!(gamma = orcsApi.getQueryFactory().tupleQuery().getTuple2GammaFromE1E2(
         CoreTupleTypes.ApplicabilityConstraint, applicability1, applicability2)).isValid()) {
         tx.addTuple2(CoreTupleTypes.ApplicabilityConstraint, applicability1, applicability2);
      } else {
         tx.introduceTuple(CoreTupleTypes.ApplicabilityConstraint, gamma);
      }

      TransactionToken commit = tx.commit();
      if (commit.isValid()) {
         response.addRaw(commit.toString());
      } else {
         response.error(
            "Error occurred during commit of adding app Constraint: " + applicability1.getIdString() + " and " + applicability2.getIdString());
      }
      return response;
   }

   @Override
   public XResultData removeApplicabilityConstraint(ApplicabilityId applicability1, ApplicabilityId applicability2) {
      XResultData response = new XResultData();
      if (orcsApi.getQueryFactory().tupleQuery().doesTuple2Exist(CoreTupleTypes.ApplicabilityConstraint, applicability1,
         applicability2)) {
         TransactionBuilder tx =
            orcsApi.getTransactionFactory().createTransaction(branch, "Add Applicability Constraint");
         tx.deleteTuple2(CoreTupleTypes.ApplicabilityConstraint, applicability1, applicability2);
         TransactionToken commit = tx.commit();
         if (commit.isValid()) {
            response.addRaw(commit.toString());
         } else {
            response.error(
               "Error occurred during commit of adding app Constraint: " + applicability1.getIdString() + " and " + applicability2.getIdString());
         }
      }
      return response;
   }

   @Override
   public List<Pair<String, String>> getApplicabilityConstraints() {
      List<Pair<String, String>> constraints = new ArrayList<>();
      HashMap<Long, Long> list = new HashMap<>();
      orcsApi.getQueryFactory().tupleQuery().getTuple2E1E2FromType(CoreTupleTypes.ApplicabilityConstraint, branch,
         list::put);
      for (Entry<Long, Long> entry : list.entrySet()) {
         constraints.add(new Pair<>(orcsApi.getKeyValueOps().getByKey(entry.getKey()),
            orcsApi.getKeyValueOps().getByKey(entry.getValue())));
      }
      return constraints;
   }
}