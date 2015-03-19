/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.rest.internal;

import static org.eclipse.osee.framework.jdk.core.util.Compare.isDifferent;
import static org.eclipse.osee.orcs.rest.internal.OrcsRestUtil.asBranch;
import static org.eclipse.osee.orcs.rest.internal.OrcsRestUtil.asBranches;
import static org.eclipse.osee.orcs.rest.internal.OrcsRestUtil.asIntegerList;
import static org.eclipse.osee.orcs.rest.internal.OrcsRestUtil.asResponse;
import static org.eclipse.osee.orcs.rest.internal.OrcsRestUtil.asTransaction;
import static org.eclipse.osee.orcs.rest.internal.OrcsRestUtil.asTransactions;
import static org.eclipse.osee.orcs.rest.internal.OrcsRestUtil.executeCallable;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.model.change.ChangeItem;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.framework.jdk.core.util.Compare;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.jaxrs.OseeWebApplicationException;
import org.eclipse.osee.orcs.ApplicationContext;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.OrcsBranch;
import org.eclipse.osee.orcs.data.ArchiveOperation;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.BranchReadable;
import org.eclipse.osee.orcs.data.CreateBranchData;
import org.eclipse.osee.orcs.data.TransactionReadable;
import org.eclipse.osee.orcs.rest.model.Branch;
import org.eclipse.osee.orcs.rest.model.BranchCommitOptions;
import org.eclipse.osee.orcs.rest.model.BranchEndpoint;
import org.eclipse.osee.orcs.rest.model.BranchQueryData;
import org.eclipse.osee.orcs.rest.model.CompareResults;
import org.eclipse.osee.orcs.rest.model.NewBranch;
import org.eclipse.osee.orcs.rest.model.NewTransaction;
import org.eclipse.osee.orcs.rest.model.Transaction;
import org.eclipse.osee.orcs.search.BranchQuery;
import org.eclipse.osee.orcs.search.QueryFactory;
import org.eclipse.osee.orcs.search.TransactionQuery;
import org.eclipse.osee.orcs.transaction.TransactionBuilder;
import org.eclipse.osee.orcs.transaction.TransactionFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

/**
 * @author Roberto E. Escobar
 */
public class BranchEndpointImpl implements BranchEndpoint {

   private final OrcsApi orcsApi;

   @Context
   private UriInfo uriInfo;

   public BranchEndpointImpl(OrcsApi orcsApi) {
      this.orcsApi = orcsApi;
   }

   public void setUriInfo(UriInfo uriInfo) {
      this.uriInfo = uriInfo;
   }

   public UriInfo getUriInfo() {
      return uriInfo;
   }

   private ApplicationContext newContext() {
      return new ApplicationContext() {

         @Override
         public String getSessionId() {
            return null;
         }
      };
   }

   private QueryFactory newQuery() {
      return orcsApi.getQueryFactory(newContext());
   }

   private BranchQuery newBranchQuery() {
      return newQuery().branchQuery();
   }

   private TransactionQuery newTxQuery() {
      return newQuery().transactionQuery();
   }

   private OrcsBranch getBranchOps() {
      return orcsApi.getBranchOps(newContext());
   }

   private ArtifactReadable getArtifactById(IOseeBranch branch, int id) {
      ArtifactReadable artifact = null;
      if (id > 0) {
         artifact = newQuery().fromBranch(branch).andLocalId(id).getResults().getExactlyOne();
      }
      return artifact;
   }

   private Long getBranchUuidFromTxId(int tx) {
      TransactionQuery txQuery = newQuery().transactionQuery();
      Long branchId = txQuery.andTxId(tx).getResults().getExactlyOne().getBranchId();
      return branchId;
   }

   private BranchReadable getBranchById(long branchUuid) {
      ResultSet<BranchReadable> results = newBranchQuery().andUuids(branchUuid)//
      .includeArchived()//
      .includeDeleted()//
      .getResults();
      return results.getExactlyOne();
   }

   private TransactionReadable getTxByBranchAndId(long branchUuid, int txId) {
      ResultSet<TransactionReadable> results = newTxQuery().andBranchIds(branchUuid).andTxId(txId).getResults();
      return results.getExactlyOne();
   }

   private TransactionFactory newTxFactory() {
      return orcsApi.getTransactionFactory(newContext());
   }

   @Override
   public List<Branch> getBranches() {
      ResultSet<BranchReadable> results = newBranchQuery()//
      .includeArchived()//
      .includeDeleted()//
      .getResults();
      return asBranches(results);
   }

   @Override
   public List<Branch> getBranches(BranchQueryData options) {
      ResultSet<BranchReadable> results = searchBranches(options);
      return asBranches(results);
   }

   @Override
   public List<Branch> getBranches(String branchUuids, String branchTypes, String branchStates, boolean deleted, boolean archived, String nameEquals, String namePattern, Long childOf, Long ancestorOf) {
      BranchQueryData options = new BranchQueryData();
      if (Strings.isValid(branchUuids)) {
         List<Long> branchUuidVals = new LinkedList<Long>();
         for (String branchUuid : branchUuids.split(",")) {
            branchUuidVals.add(Long.parseLong(branchUuid));
         }
         options.setBranchIds(branchUuidVals);
      }

      if (Strings.isValid(branchTypes)) {
         List<BranchType> branchTypeVals = new LinkedList<BranchType>();
         for (String branchType : branchTypes.split(",")) {
            branchTypeVals.add(BranchType.valueOf(branchType.toUpperCase()));
         }
         options.setBranchTypes(branchTypeVals);
      }

      if (Strings.isValid(branchStates)) {
         List<BranchState> branchStateVals = new LinkedList<BranchState>();
         for (String branchState : branchStates.split(",")) {
            branchStateVals.add(BranchState.valueOf(branchState.toUpperCase()));
         }
         options.setBranchStates(branchStateVals);
      }

      options.setIncludeDeleted(deleted);
      options.setIncludeArchived(archived);

      if (Strings.isValid(nameEquals)) {
         options.setNameEquals(nameEquals);
      }

      if (Strings.isValid(namePattern)) {
         options.setNamePattern(namePattern);
      }

      if (childOf != null) {
         options.setIsChildOf(childOf);
      }

      if (ancestorOf != null) {
         options.setIsAncestorOf(ancestorOf);
      }
      return getBranches(options);
   }

   @Override
   public List<Branch> getBaselineBranches() {
      ResultSet<BranchReadable> results = newBranchQuery()//
      .includeArchived(false) //
      .includeDeleted(false) //
      .andIsOfType(BranchType.BASELINE)//
      .getResults();
      return asBranches(results);
   }

   @Override
   public List<Branch> getWorkingBranches() {
      ResultSet<BranchReadable> results = newBranchQuery()//
      .includeArchived(false) //
      .includeDeleted(false) //
      .andIsOfType(BranchType.WORKING)//
      .getResults();
      return asBranches(results);
   }

   @Override
   public Branch getBranch(long branchUuid) {
      BranchReadable branch = getBranchById(branchUuid);
      return asBranch(branch);
   }

   @Override
   public List<Transaction> getAllBranchTxs(long branchUuid) {
      ResultSet<TransactionReadable> results = newTxQuery().andBranchIds(branchUuid).getResults();
      return asTransactions(results);
   }

   @Override
   public Transaction getBranchTx(long branchUuid, int txId) {
      TransactionReadable tx = getTxByBranchAndId(branchUuid, txId);
      return asTransaction(tx);
   }

   @Override
   public CompareResults compareBranches(long branchUuid, long branchUuid2) {
      TransactionReadable sourceTx = newTxQuery().andIsHead(branchUuid).getResults().getExactlyOne();
      TransactionReadable destinationTx = newTxQuery().andIsHead(branchUuid2).getResults().getExactlyOne();

      Callable<List<ChangeItem>> op = getBranchOps().compareBranch(sourceTx, destinationTx);
      List<ChangeItem> changes = executeCallable(op);

      CompareResults data = new CompareResults();
      data.setChanges(changes);
      return data;
   }

   @Override
   public Response createBranch(NewBranch data) {
      long branchUuid = Lib.generateUuid();
      return createBranchWithId(branchUuid, data);
   }

   @Override
   public Response createBranchWithId(long branchUuid, NewBranch data) {
      if (branchUuid <= 0) {
         throw new OseeWebApplicationException(Status.BAD_REQUEST, "branchUuid [%d] uuid must be > 0", branchUuid);
      }

      CreateBranchData createData = new CreateBranchData();
      createData.setUuid(branchUuid);
      createData.setName(data.getBranchName());
      createData.setBranchType(data.getBranchType());
      createData.setCreationComment(data.getCreationComment());

      createData.setUserArtifact(getArtifactById(CoreBranches.COMMON, data.getAuthorId()));
      createData.setAssociatedArtifact(getArtifactById(CoreBranches.COMMON, data.getAssociatedArtifactId()));

      createData.setFromTransaction(TokenFactory.createTransaction(data.getSourceTransactionId()));
      createData.setParentBranchUuid(getBranchUuidFromTxId(data.getSourceTransactionId()));

      createData.setMergeDestinationBranchId(data.getMergeDestinationBranchId());
      createData.setMergeAddressingQueryId(data.getMergeAddressingQueryId());

      createData.setTxCopyBranchType(data.isTxCopyBranchType());

      Callable<BranchReadable> op = getBranchOps().createBranch(createData);
      BranchReadable result = executeCallable(op);

      UriInfo uriInfo = getUriInfo();
      URI uri = getBranchLocation(uriInfo, result);
      return Response.created(uri).entity(asBranch(result)).build();
   }

   private URI getBranchLocation(UriInfo uriInfo, BranchReadable branch) {
      URI location = null;
      String path = uriInfo.getPath();
      if (Strings.isValid(path)) {
         String value = path.replace("branches", "");
         value = value.replaceAll("/", "");
         if (Strings.isNumeric(value)) {
            try {
               Long id = Long.parseLong(value);
               if (branch.getGuid().equals(id)) {
                  location = uriInfo.getRequestUri();
               }
            } catch (Exception ex) {
               // do nothing
            }
         }
      }

      if (location == null) {
         location = uriInfo.getRequestUriBuilder().path("{branch-uuid}").build(branch.getGuid());
      }
      return location;
   }

   @Override
   public Response commitBranch(long branchUuid, long destinationBranchUuid, BranchCommitOptions options) {
      BranchReadable srcBranch = getBranchById(branchUuid);
      BranchReadable destBranch = getBranchById(destinationBranchUuid);

      ArtifactReadable committer = getArtifactById(CoreBranches.COMMON, options.getCommitterId());
      Callable<TransactionReadable> op = getBranchOps().commitBranch(committer, srcBranch, destBranch);
      TransactionReadable tx = executeCallable(op);

      if (options.isArchive()) {
         Callable<?> op2 = getBranchOps().archiveUnarchiveBranch(srcBranch, ArchiveOperation.ARCHIVE);
         executeCallable(op2);
      }

      UriInfo uriInfo = getUriInfo();
      URI location = getTxLocation(uriInfo, tx);
      return Response.created(location).entity(asTransaction(tx)).build();
   }

   private URI getTxLocation(UriInfo uriInfo, TransactionReadable tx) {
      UriBuilder builder = uriInfo.getRequestUriBuilder();
      URI location = builder.path("..").path("..").path("txs").path("{tx-id}").build(tx.getGuid());
      return location;
   }

   @Override
   public Response archiveBranch(long branchUuid) {
      BranchReadable branch = getBranchById(branchUuid);

      boolean modified = false;
      BranchArchivedState currentState = branch.getArchiveState();
      if (BranchArchivedState.UNARCHIVED == currentState) {
         Callable<?> op = getBranchOps().archiveUnarchiveBranch(branch, ArchiveOperation.ARCHIVE);
         executeCallable(op);
         modified = true;
      }
      return asResponse(modified);
   }

   @Override
   public Response writeTx(long branchUuid, NewTransaction data) {
      String comment = data.getComment();
      ArtifactReadable userArtifact = null;

      TransactionFactory txFactory = newTxFactory();
      TransactionBuilder txBuilder = txFactory.createTransaction(branchUuid, userArtifact, comment);

      //TODO: Integrate data with TxBuilder

      TransactionReadable tx = txBuilder.commit();

      URI location = uriInfo.getRequestUriBuilder().path("{tx-id}").build(tx.getGuid());
      return Response.created(location).entity(asTransaction(tx)).build();
   }

   @Override
   public Response setBranchName(long branchUuid, String newName) {
      BranchReadable branch = getBranchById(branchUuid);
      boolean modified = false;
      if (isDifferent(branch.getName(), newName)) {
         Callable<?> op = getBranchOps().changeBranchName(branch, newName);
         executeCallable(op);
         modified = true;
      }
      return asResponse(modified);
   }

   @Override
   public Response setBranchType(long branchUuid, BranchType newType) {
      BranchReadable branch = getBranchById(branchUuid);
      boolean modified = false;
      if (isDifferent(branch.getBranchType(), newType)) {
         Callable<?> op = getBranchOps().changeBranchType(branch, newType);
         executeCallable(op);
         modified = true;
      }
      return asResponse(modified);
   }

   @Override
   public Response setBranchState(long branchUuid, BranchState newState) {
      BranchReadable branch = getBranchById(branchUuid);
      boolean modified = false;
      if (isDifferent(branch.getBranchState(), newState)) {
         Callable<?> op = getBranchOps().changeBranchState(branch, newState);
         executeCallable(op);
         modified = true;
      }
      return asResponse(modified);
   }

   @Override
   public Response associateBranchToArtifact(long branchUuid, int artifactId) {
      BranchReadable branch = getBranchById(branchUuid);
      boolean modified = false;
      if (isDifferent(branch.getAssociatedArtifactId(), artifactId)) {
         ArtifactReadable artifact =
            newQuery().fromBranch(CoreBranches.COMMON).andLocalId(artifactId).getResults().getExactlyOne();
         Callable<?> op = getBranchOps().associateBranchToArtifact(branch, artifact);
         executeCallable(op);
         modified = true;
      }
      return asResponse(modified);
   }

   @Override
   public Response setTxComment(long branchUuid, int txId, String comment) {
      TransactionReadable tx = getTxByBranchAndId(branchUuid, txId);
      boolean modified = false;
      if (Compare.isDifferent(tx.getComment(), comment)) {
         TransactionFactory txFactory = newTxFactory();
         txFactory.setTransactionComment(tx, comment);
         modified = true;
      }
      return asResponse(modified);
   }

   @Override
   public Response purgeBranch(long branchUuid, boolean recurse) {
      boolean modified = false;
      BranchReadable branch = getBranchById(branchUuid);
      if (branch != null) {
         Callable<?> op = getBranchOps().purgeBranch(branch, recurse);
         executeCallable(op);
         modified = true;
      }
      return asResponse(modified);
   }

   @Override
   public Response unarchiveBranch(long branchUuid) {
      BranchReadable branch = getBranchById(branchUuid);
      BranchArchivedState state = branch.getArchiveState();

      boolean modified = false;
      if (BranchArchivedState.ARCHIVED == state) {
         Callable<?> op = getBranchOps().archiveUnarchiveBranch(branch, ArchiveOperation.UNARCHIVE);
         executeCallable(op);
         modified = true;
      }
      return asResponse(modified);
   }

   @Override
   public Response unCommitBranch(long branchUuid, long destinationBranchUuid) {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   @Override
   public Response unassociateBranch(long branchUuid) {
      BranchReadable branch = getBranchById(branchUuid);
      boolean modified = false;
      if (branch.getAssociatedArtifactId() != -1) {
         Callable<?> op = getBranchOps().unassociateBranch(branch);
         executeCallable(op);
         modified = true;
      }
      return asResponse(modified);
   }

   @Override
   public Response purgeTxs(long branchUuid, String txIds) {
      boolean modified = false;
      List<Integer> txsToDelete = asIntegerList(txIds);
      if (!txsToDelete.isEmpty()) {
         ResultSet<TransactionReadable> results =
            newTxQuery().andBranchIds(branchUuid).andTxIds(txsToDelete).getResults();
         if (!results.isEmpty()) {
            checkAllTxFoundAreOnBranch("Purge Transaction", branchUuid, txsToDelete, results);
            List<TransactionReadable> list = Lists.newArrayList(results);
            Callable<?> op = newTxFactory().purgeTransaction(list);
            executeCallable(op);
            modified = true;
         }
      }
      return asResponse(modified);
   }

   private void checkAllTxFoundAreOnBranch(String opName, long branchUuid, List<Integer> txIds, ResultSet<TransactionReadable> result) {
      if (txIds.size() != result.size()) {
         Set<Integer> found = new HashSet<Integer>();
         for (TransactionReadable tx : result) {
            found.add(tx.getGuid());
         }
         SetView<Integer> difference = Sets.difference(Sets.newHashSet(txIds), found);
         if (!difference.isEmpty()) {
            throw new OseeWebApplicationException(
               Status.BAD_REQUEST,
               "%s Error - The following transactions from %s were not found on branch [%s] - txs %s - Please remove them from the request and try again.",
               opName, txIds, branchUuid, difference);
         }
      }
   }

   private ResultSet<BranchReadable> searchBranches(BranchQueryData options) {
      BranchQuery query = orcsApi.getQueryFactory(null).branchQuery();
      if (Conditions.hasValues(options.getBranchIds())) {
         query.andUuids(options.getBranchIds());
      }

      List<BranchState> branchStates = options.getBranchStates();
      if (Conditions.hasValues(branchStates)) {
         query.andStateIs(branchStates.toArray(new BranchState[branchStates.size()]));
      }

      List<BranchType> branchTypes = options.getBranchTypes();
      if (Conditions.hasValues(branchTypes)) {
         query.andIsOfType(branchTypes.toArray(new BranchType[branchTypes.size()]));
      }

      List<Long> branchUuids = options.getBranchIds();
      if (Conditions.hasValues(branchUuids)) {
         query.andUuids(branchUuids);
      }

      if (options.isIncludeArchived()) {
         query.includeArchived();
      }

      if (options.isIncludeDeleted()) {
         query.includeDeleted();
      }

      String nameEquals = options.getNameEquals();
      if (Strings.isValid(nameEquals)) {
         query.andNameEquals(nameEquals);
      }

      String namePattern = options.getNamePattern();
      if (Strings.isValid(namePattern)) {
         query.andNamePattern(namePattern);
      }

      Long ancestorOf = options.getIsAncestorOf();
      if (ancestorOf > 0) {
         IOseeBranch ancestorOfToken = TokenFactory.createBranch(ancestorOf, "queryAncestorOf");
         query.andIsAncestorOf(ancestorOfToken);
      }

      Long childOf = options.getIsChildOf();
      if (childOf > 0) {
         IOseeBranch childOfToken = TokenFactory.createBranch(ancestorOf, "queryChildOf");
         query.andIsAncestorOf(childOfToken);
      }
      return query.getResults();
   }
}
