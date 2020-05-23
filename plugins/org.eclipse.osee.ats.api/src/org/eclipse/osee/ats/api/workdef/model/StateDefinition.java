/*********************************************************************
 * Copyright (c) 2010 Boeing
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

package org.eclipse.osee.ats.api.workdef.model;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.ats.api.workdef.IAtsDecisionReviewDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsLayoutItem;
import org.eclipse.osee.ats.api.workdef.IAtsPeerReviewDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinition;
import org.eclipse.osee.ats.api.workdef.StateColor;
import org.eclipse.osee.ats.api.workdef.StateType;
import org.eclipse.osee.ats.api.workflow.hooks.IAtsTransitionHook;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Donald G. Dunne
 */
public class StateDefinition extends AbstractWorkDefItem implements IAtsStateDefinition {

   private StateType StateType;
   private int ordinal = 0;
   private List<IAtsLayoutItem> stateItems = new ArrayList<>(5);
   private final RuleManager ruleMgr = new RuleManager();
   private final List<IAtsStateDefinition> toStates = new ArrayList<>(5);
   private IAtsStateDefinition defaultToState;
   private final List<IAtsStateDefinition> overrideAttributeValidationStates = new ArrayList<>(5);
   private final List<IAtsDecisionReviewDefinition> decisionReviews = new ArrayList<>();
   private final List<IAtsPeerReviewDefinition> peerReviews = new ArrayList<>();
   private IAtsWorkDefinition workDefinition;
   private int stateWeight = 0;
   private Integer recommendedPercentComplete = null;
   private StateColor color = null;
   private final List<IAtsTransitionHook> transitionListeners = new ArrayList<>();

   public StateDefinition(String name) {
      super(Long.valueOf(name.hashCode()), name);
   }

   @Override
   public List<IAtsLayoutItem> getLayoutItems() {
      return stateItems;
   }

   @Override
   public StateType getStateType() {
      return StateType;
   }

   @Override
   public void setLayoutItems(List<IAtsLayoutItem> layoutToSet) {
      this.stateItems = layoutToSet;
   }

   public void setStateType(StateType StateType) {
      this.StateType = StateType;
   }

   @Override
   public int getOrdinal() {
      return ordinal;
   }

   public void setOrdinal(int ordinal) {
      this.ordinal = ordinal;
   }

   @Override
   public List<IAtsStateDefinition> getToStates() {
      return toStates;
   }

   @Override
   public IAtsWorkDefinition getWorkDefinition() {
      return workDefinition;
   }

   @Override
   public void setWorkDefinition(IAtsWorkDefinition workDefinition) {
      this.workDefinition = workDefinition;
   }

   @Override
   public String toString() {
      return String.format("%s  - (%s)", getName(), getStateType());
   }

   /**
    * Returns fully qualified name of <work definition name>.<this state name
    */

   @Override
   public String getFullName() {
      if (workDefinition != null) {
         return workDefinition.getName() + "." + getName();
      }
      return getName();
   }

   @Override
   public List<IAtsStateDefinition> getOverrideAttributeValidationStates() {
      return overrideAttributeValidationStates;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (getName() == null ? 0 : getName().hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      boolean equals = false;
      if (this == obj) {
         equals = true;
      } else if (obj != null) {
         if (getClass() == obj.getClass()) {
            IAtsStateDefinition other = (IAtsStateDefinition) obj;
            if (Strings.isValid(getName()) && Strings.isValid(other.getName()) && getName().equals(other.getName())) {
               equals = true;
            }
         }
      }
      return equals;
   }

   @Override
   public IAtsStateDefinition getDefaultToState() {
      return defaultToState;
   }

   public void setDefaultToState(IAtsStateDefinition defaultToState) {
      this.defaultToState = defaultToState;
   }

   @Override
   public List<IAtsDecisionReviewDefinition> getDecisionReviews() {
      return decisionReviews;
   }

   @Override
   public List<IAtsPeerReviewDefinition> getPeerReviews() {
      return peerReviews;
   }

   @Override
   public int getStateWeight() {
      return stateWeight;
   }

   /**
    * Set how much (of 100%) this state's percent complete will contribute to the full percent complete of work
    * definitions.
    *
    * @param percentWeight int value where all stateWeights in workdefinition == 100
    */

   public void setStateWeight(int percentWeight) {
      this.stateWeight = percentWeight;
   }

   public void setRecommendedPercentComplete(int recommendedPercentComplete) {
      this.recommendedPercentComplete = recommendedPercentComplete;
   }

   @Override
   public Integer getRecommendedPercentComplete() {
      return recommendedPercentComplete;
   }

   public void setColor(StateColor stateColor) {
      this.color = stateColor;
   }

   @Override
   public StateColor getColor() {
      return color;
   }

   public void removeRule(String rule) {
      ruleMgr.removeRule(rule);
   }

   @Override
   public List<String> getRules() {
      return ruleMgr.getRules();
   }

   public void addRule(String rule) {
      ruleMgr.addRule(rule);
   }

   @Override
   public boolean hasRule(String rule) {
      return ruleMgr.hasRule(rule);
   }

   public void addDecisionReview(DecisionReviewDefinition reviewDefinition) {
      decisionReviews.add(reviewDefinition);
   }

   public void addPeerReview(PeerReviewDefinition reviewDefinition) {
      peerReviews.add(reviewDefinition);
   }

   @Override
   public void addTransitionListener(IAtsTransitionHook transitionListener) {
      transitionListeners.add(transitionListener);
   }

   @Override
   public List<IAtsTransitionHook> getTransitionListeners() {
      return transitionListeners;
   }

}
