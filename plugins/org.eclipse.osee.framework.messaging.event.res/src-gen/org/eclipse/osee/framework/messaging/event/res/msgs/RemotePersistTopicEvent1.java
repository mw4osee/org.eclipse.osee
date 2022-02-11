//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2012.09.14 at 05:06:42 PM MST
//

package org.eclipse.osee.framework.messaging.event.res.msgs;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.messaging.event.res.RemoteEvent;

/**
 * <p>
 * Java class for RemotePersistTopicEvent1 complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="RemotePersistTopicEvent1">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="branchGuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="transactionId" type="{http://www.w3.org/2001/XMLSchema}TransactionId"/>
 *         &lt;element name="artifacts" type="{}RemoteTopicArtifact1" maxOccurs="unbounded"/>
 *         &lt;element name="relations" type="{}RemoteTopicRelation1" maxOccurs="unbounded"/>
 *         &lt;element name="relationReorders" type="{}RemoteTopicRelationReorder1" maxOccurs="unbounded"/>
 *         &lt;element name="networkSender" type="{}RemoteNetworkSender1"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RemotePersistTopicEvent1", propOrder = {
   "branchGuid",
   "transactionId",
   "artifacts",
   "relations",
   "relationReorders",
   "networkSender"})
public class RemotePersistTopicEvent1 extends RemoteEvent {

   @XmlElement(required = true)
   protected String branchGuid;
   protected TransactionId transactionId;
   @XmlElement(required = true)
   protected List<RemoteTopicArtifact1> artifacts;
   @XmlElement(required = true)
   protected List<RemoteTopicRelation1> relations;
   @XmlElement(required = true)
   protected List<RemoteTopicRelationReorder1> relationReorders;
   @XmlElement(required = true)
   protected RemoteNetworkSender1 networkSender;

   /**
    * Gets the value of the branchGuid property.
    *
    * @return possible object is {@link String }
    */
   public String getBranchGuid() {
      return branchGuid;
   }

   public void setBranchGuid(BranchId branch) {
      this.branchGuid = branch.getIdString();
   }

   /**
    * Gets the value of the transactionId property.
    */
   public TransactionToken getTransaction() {
      return TransactionToken.valueOf(transactionId, BranchId.valueOf(branchGuid));
   }

   /**
    * Sets the value of the transactionId property.
    */
   public void setTransactionId(TransactionId value) {
      this.transactionId = value;
   }

   public void setTransaction(TransactionId tx) {
      this.transactionId = tx;
   }

   /**
    * Gets the value of the artifacts property.
    * <p>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the artifacts property.
    * <p>
    * For example, to add a new item, do as follows:
    *
    * <pre>
    * getArtifacts().add(newItem);
    * </pre>
    * <p>
    * Objects of the following type(s) are allowed in the list {@link RemoteTopicArtifact1 }
    */
   public List<RemoteTopicArtifact1> getArtifacts() {
      if (artifacts == null) {
         artifacts = new ArrayList<>();
      }
      return this.artifacts;
   }

   /**
    * Gets the value of the relations property.
    * <p>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the relations property.
    * <p>
    * For example, to add a new item, do as follows:
    *
    * <pre>
    * getRelations().add(newItem);
    * </pre>
    * <p>
    * Objects of the following type(s) are allowed in the list {@link RemoteTopicRelation1 }
    */
   public List<RemoteTopicRelation1> getRelations() {
      if (relations == null) {
         relations = new ArrayList<>();
      }
      return this.relations;
   }

   /**
    * Gets the value of the relationReorders property.
    * <p>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the relationReorders property.
    * <p>
    * For example, to add a new item, do as follows:
    *
    * <pre>
    * getRelationReorders().add(newItem);
    * </pre>
    * <p>
    * Objects of the following type(s) are allowed in the list {@link RemoteTopicRelationReorder1 }
    */
   public List<RemoteTopicRelationReorder1> getRelationReorders() {
      if (relationReorders == null) {
         relationReorders = new ArrayList<>();
      }
      return this.relationReorders;
   }

   /**
    * Gets the value of the networkSender property.
    *
    * @return possible object is {@link RemoteNetworkSender1 }
    */
   @Override
   public RemoteNetworkSender1 getNetworkSender() {
      return networkSender;
   }

   /**
    * Sets the value of the networkSender property.
    *
    * @param value allowed object is {@link RemoteNetworkSender1 }
    */
   public void setNetworkSender(RemoteNetworkSender1 value) {
      this.networkSender = value;
   }

}
