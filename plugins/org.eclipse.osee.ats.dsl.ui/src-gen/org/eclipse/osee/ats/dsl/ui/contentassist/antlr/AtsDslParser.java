/*
* generated by Xtext
*/
package org.eclipse.osee.ats.dsl.ui.contentassist.antlr;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.RecognitionException;
import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.ui.editor.contentassist.antlr.AbstractContentAssistParser;
import org.eclipse.xtext.ui.editor.contentassist.antlr.FollowElement;
import org.eclipse.xtext.ui.editor.contentassist.antlr.internal.AbstractInternalContentAssistParser;

import com.google.inject.Inject;

import org.eclipse.osee.ats.dsl.services.AtsDslGrammarAccess;

public class AtsDslParser extends AbstractContentAssistParser {
	
	@Inject
	private AtsDslGrammarAccess grammarAccess;
	
	private Map<AbstractElement, String> nameMappings;
	
	@Override
	protected org.eclipse.osee.ats.dsl.ui.contentassist.antlr.internal.InternalAtsDslParser createParser() {
		org.eclipse.osee.ats.dsl.ui.contentassist.antlr.internal.InternalAtsDslParser result = new org.eclipse.osee.ats.dsl.ui.contentassist.antlr.internal.InternalAtsDslParser(null);
		result.setGrammarAccess(grammarAccess);
		return result;
	}
	
	@Override
	protected String getRuleName(AbstractElement element) {
		if (nameMappings == null) {
			nameMappings = new HashMap<AbstractElement, String>() {
				private static final long serialVersionUID = 1L;
				{
					put(grammarAccess.getUserRefAccess().getAlternatives(), "rule__UserRef__Alternatives");
					put(grammarAccess.getLayoutTypeAccess().getAlternatives(), "rule__LayoutType__Alternatives");
					put(grammarAccess.getLayoutItemAccess().getAlternatives(), "rule__LayoutItem__Alternatives");
					put(grammarAccess.getUserDefOptionAccess().getAlternatives(), "rule__UserDefOption__Alternatives");
					put(grammarAccess.getTeamDefOptionAccess().getAlternatives(), "rule__TeamDefOption__Alternatives");
					put(grammarAccess.getActionableItemOptionAccess().getAlternatives(), "rule__ActionableItemOption__Alternatives");
					put(grammarAccess.getCompositeOptionAccess().getAlternatives(), "rule__CompositeOption__Alternatives");
					put(grammarAccess.getTransitionOptionAccess().getAlternatives(), "rule__TransitionOption__Alternatives");
					put(grammarAccess.getRuleAccess().getAlternatives(), "rule__Rule__Alternatives");
					put(grammarAccess.getWidgetOptionAccess().getAlternatives(), "rule__WidgetOption__Alternatives");
					put(grammarAccess.getPageTypeAccess().getAlternatives(), "rule__PageType__Alternatives");
					put(grammarAccess.getStateColorAccess().getAlternatives(), "rule__StateColor__Alternatives");
					put(grammarAccess.getBooleanDefAccess().getAlternatives(), "rule__BooleanDef__Alternatives");
					put(grammarAccess.getWorkflowEventTypeAccess().getAlternatives(), "rule__WorkflowEventType__Alternatives");
					put(grammarAccess.getReviewBlockingTypeAccess().getAlternatives(), "rule__ReviewBlockingType__Alternatives");
					put(grammarAccess.getAtsDslAccess().getGroup(), "rule__AtsDsl__Group__0");
					put(grammarAccess.getAtsDslAccess().getGroup_0(), "rule__AtsDsl__Group_0__0");
					put(grammarAccess.getAtsDslAccess().getGroup_1(), "rule__AtsDsl__Group_1__0");
					put(grammarAccess.getAtsDslAccess().getGroup_2(), "rule__AtsDsl__Group_2__0");
					put(grammarAccess.getAtsDslAccess().getGroup_3(), "rule__AtsDsl__Group_3__0");
					put(grammarAccess.getUserDefAccess().getGroup(), "rule__UserDef__Group__0");
					put(grammarAccess.getUserDefAccess().getGroup_2(), "rule__UserDef__Group_2__0");
					put(grammarAccess.getUserDefAccess().getGroup_2_1(), "rule__UserDef__Group_2_1__0");
					put(grammarAccess.getUserDefAccess().getGroup_2_2(), "rule__UserDef__Group_2_2__0");
					put(grammarAccess.getUserDefAccess().getGroup_2_3(), "rule__UserDef__Group_2_3__0");
					put(grammarAccess.getUserDefAccess().getGroup_2_4(), "rule__UserDef__Group_2_4__0");
					put(grammarAccess.getTeamDefAccess().getGroup(), "rule__TeamDef__Group__0");
					put(grammarAccess.getTeamDefAccess().getGroup_3(), "rule__TeamDef__Group_3__0");
					put(grammarAccess.getTeamDefAccess().getGroup_4(), "rule__TeamDef__Group_4__0");
					put(grammarAccess.getTeamDefAccess().getGroup_5(), "rule__TeamDef__Group_5__0");
					put(grammarAccess.getTeamDefAccess().getGroup_6(), "rule__TeamDef__Group_6__0");
					put(grammarAccess.getTeamDefAccess().getGroup_7(), "rule__TeamDef__Group_7__0");
					put(grammarAccess.getTeamDefAccess().getGroup_8(), "rule__TeamDef__Group_8__0");
					put(grammarAccess.getTeamDefAccess().getGroup_9(), "rule__TeamDef__Group_9__0");
					put(grammarAccess.getTeamDefAccess().getGroup_10(), "rule__TeamDef__Group_10__0");
					put(grammarAccess.getTeamDefAccess().getGroup_11(), "rule__TeamDef__Group_11__0");
					put(grammarAccess.getTeamDefAccess().getGroup_12(), "rule__TeamDef__Group_12__0");
					put(grammarAccess.getTeamDefAccess().getGroup_13(), "rule__TeamDef__Group_13__0");
					put(grammarAccess.getTeamDefAccess().getGroup_13_2(), "rule__TeamDef__Group_13_2__0");
					put(grammarAccess.getActionableItemDefAccess().getGroup(), "rule__ActionableItemDef__Group__0");
					put(grammarAccess.getActionableItemDefAccess().getGroup_2(), "rule__ActionableItemDef__Group_2__0");
					put(grammarAccess.getActionableItemDefAccess().getGroup_2_1(), "rule__ActionableItemDef__Group_2_1__0");
					put(grammarAccess.getActionableItemDefAccess().getGroup_2_2(), "rule__ActionableItemDef__Group_2_2__0");
					put(grammarAccess.getActionableItemDefAccess().getGroup_2_3(), "rule__ActionableItemDef__Group_2_3__0");
					put(grammarAccess.getActionableItemDefAccess().getGroup_2_4(), "rule__ActionableItemDef__Group_2_4__0");
					put(grammarAccess.getActionableItemDefAccess().getGroup_2_5(), "rule__ActionableItemDef__Group_2_5__0");
					put(grammarAccess.getActionableItemDefAccess().getGroup_2_6(), "rule__ActionableItemDef__Group_2_6__0");
					put(grammarAccess.getActionableItemDefAccess().getGroup_2_7(), "rule__ActionableItemDef__Group_2_7__0");
					put(grammarAccess.getActionableItemDefAccess().getGroup_2_7_2(), "rule__ActionableItemDef__Group_2_7_2__0");
					put(grammarAccess.getVersionDefAccess().getGroup(), "rule__VersionDef__Group__0");
					put(grammarAccess.getVersionDefAccess().getGroup_2(), "rule__VersionDef__Group_2__0");
					put(grammarAccess.getVersionDefAccess().getGroup_3(), "rule__VersionDef__Group_3__0");
					put(grammarAccess.getVersionDefAccess().getGroup_4(), "rule__VersionDef__Group_4__0");
					put(grammarAccess.getVersionDefAccess().getGroup_5(), "rule__VersionDef__Group_5__0");
					put(grammarAccess.getVersionDefAccess().getGroup_6(), "rule__VersionDef__Group_6__0");
					put(grammarAccess.getVersionDefAccess().getGroup_7(), "rule__VersionDef__Group_7__0");
					put(grammarAccess.getVersionDefAccess().getGroup_8(), "rule__VersionDef__Group_8__0");
					put(grammarAccess.getVersionDefAccess().getGroup_9(), "rule__VersionDef__Group_9__0");
					put(grammarAccess.getWorkDefAccess().getGroup(), "rule__WorkDef__Group__0");
					put(grammarAccess.getWorkDefAccess().getGroup_2(), "rule__WorkDef__Group_2__0");
					put(grammarAccess.getWorkDefAccess().getGroup_3(), "rule__WorkDef__Group_3__0");
					put(grammarAccess.getWidgetDefAccess().getGroup(), "rule__WidgetDef__Group__0");
					put(grammarAccess.getWidgetDefAccess().getGroup_3(), "rule__WidgetDef__Group_3__0");
					put(grammarAccess.getWidgetDefAccess().getGroup_4(), "rule__WidgetDef__Group_4__0");
					put(grammarAccess.getWidgetDefAccess().getGroup_5(), "rule__WidgetDef__Group_5__0");
					put(grammarAccess.getWidgetDefAccess().getGroup_6(), "rule__WidgetDef__Group_6__0");
					put(grammarAccess.getWidgetDefAccess().getGroup_7(), "rule__WidgetDef__Group_7__0");
					put(grammarAccess.getWidgetDefAccess().getGroup_8(), "rule__WidgetDef__Group_8__0");
					put(grammarAccess.getWidgetRefAccess().getGroup(), "rule__WidgetRef__Group__0");
					put(grammarAccess.getAttrWidgetAccess().getGroup(), "rule__AttrWidget__Group__0");
					put(grammarAccess.getAttrWidgetAccess().getGroup_2(), "rule__AttrWidget__Group_2__0");
					put(grammarAccess.getStateDefAccess().getGroup(), "rule__StateDef__Group__0");
					put(grammarAccess.getStateDefAccess().getGroup_3(), "rule__StateDef__Group_3__0");
					put(grammarAccess.getStateDefAccess().getGroup_9(), "rule__StateDef__Group_9__0");
					put(grammarAccess.getStateDefAccess().getGroup_12(), "rule__StateDef__Group_12__0");
					put(grammarAccess.getStateDefAccess().getGroup_13(), "rule__StateDef__Group_13__0");
					put(grammarAccess.getStateDefAccess().getGroup_14(), "rule__StateDef__Group_14__0");
					put(grammarAccess.getDecisionReviewRefAccess().getGroup(), "rule__DecisionReviewRef__Group__0");
					put(grammarAccess.getDecisionReviewDefAccess().getGroup(), "rule__DecisionReviewDef__Group__0");
					put(grammarAccess.getDecisionReviewDefAccess().getGroup_7(), "rule__DecisionReviewDef__Group_7__0");
					put(grammarAccess.getDecisionReviewDefAccess().getGroup_12(), "rule__DecisionReviewDef__Group_12__0");
					put(grammarAccess.getDecisionReviewDefAccess().getGroup_13(), "rule__DecisionReviewDef__Group_13__0");
					put(grammarAccess.getDecisionReviewOptAccess().getGroup(), "rule__DecisionReviewOpt__Group__0");
					put(grammarAccess.getPeerReviewRefAccess().getGroup(), "rule__PeerReviewRef__Group__0");
					put(grammarAccess.getPeerReviewDefAccess().getGroup(), "rule__PeerReviewDef__Group__0");
					put(grammarAccess.getPeerReviewDefAccess().getGroup_3(), "rule__PeerReviewDef__Group_3__0");
					put(grammarAccess.getPeerReviewDefAccess().getGroup_6(), "rule__PeerReviewDef__Group_6__0");
					put(grammarAccess.getPeerReviewDefAccess().getGroup_7(), "rule__PeerReviewDef__Group_7__0");
					put(grammarAccess.getPeerReviewDefAccess().getGroup_12(), "rule__PeerReviewDef__Group_12__0");
					put(grammarAccess.getFollowupRefAccess().getGroup(), "rule__FollowupRef__Group__0");
					put(grammarAccess.getFollowupRefAccess().getGroup_1(), "rule__FollowupRef__Group_1__0");
					put(grammarAccess.getUserByUserIdAccess().getGroup(), "rule__UserByUserId__Group__0");
					put(grammarAccess.getUserByNameAccess().getGroup(), "rule__UserByName__Group__0");
					put(grammarAccess.getToStateAccess().getGroup(), "rule__ToState__Group__0");
					put(grammarAccess.getLayoutDefAccess().getGroup(), "rule__LayoutDef__Group__0");
					put(grammarAccess.getLayoutCopyAccess().getGroup(), "rule__LayoutCopy__Group__0");
					put(grammarAccess.getCompositeAccess().getGroup(), "rule__Composite__Group__0");
					put(grammarAccess.getCompositeAccess().getGroup_5(), "rule__Composite__Group_5__0");
					put(grammarAccess.getAtsDslAccess().getWorkDefAssignment_0_1(), "rule__AtsDsl__WorkDefAssignment_0_1");
					put(grammarAccess.getAtsDslAccess().getUserDefAssignment_1_1(), "rule__AtsDsl__UserDefAssignment_1_1");
					put(grammarAccess.getAtsDslAccess().getTeamDefAssignment_2_1(), "rule__AtsDsl__TeamDefAssignment_2_1");
					put(grammarAccess.getAtsDslAccess().getActionableItemDefAssignment_3_1(), "rule__AtsDsl__ActionableItemDefAssignment_3_1");
					put(grammarAccess.getUserDefAccess().getNameAssignment_0(), "rule__UserDef__NameAssignment_0");
					put(grammarAccess.getUserDefAccess().getUserDefOptionAssignment_1(), "rule__UserDef__UserDefOptionAssignment_1");
					put(grammarAccess.getUserDefAccess().getActiveAssignment_2_1_1(), "rule__UserDef__ActiveAssignment_2_1_1");
					put(grammarAccess.getUserDefAccess().getUserIdAssignment_2_2_1(), "rule__UserDef__UserIdAssignment_2_2_1");
					put(grammarAccess.getUserDefAccess().getEmailAssignment_2_3_1(), "rule__UserDef__EmailAssignment_2_3_1");
					put(grammarAccess.getUserDefAccess().getAdminAssignment_2_4_1(), "rule__UserDef__AdminAssignment_2_4_1");
					put(grammarAccess.getTeamDefAccess().getNameAssignment_0(), "rule__TeamDef__NameAssignment_0");
					put(grammarAccess.getTeamDefAccess().getTeamDefOptionAssignment_1(), "rule__TeamDef__TeamDefOptionAssignment_1");
					put(grammarAccess.getTeamDefAccess().getActiveAssignment_3_1(), "rule__TeamDef__ActiveAssignment_3_1");
					put(grammarAccess.getTeamDefAccess().getUsesVersionsAssignment_4_1(), "rule__TeamDef__UsesVersionsAssignment_4_1");
					put(grammarAccess.getTeamDefAccess().getStaticIdAssignment_5_1(), "rule__TeamDef__StaticIdAssignment_5_1");
					put(grammarAccess.getTeamDefAccess().getLeadAssignment_6_1(), "rule__TeamDef__LeadAssignment_6_1");
					put(grammarAccess.getTeamDefAccess().getMemberAssignment_7_1(), "rule__TeamDef__MemberAssignment_7_1");
					put(grammarAccess.getTeamDefAccess().getPriviledgedAssignment_8_1(), "rule__TeamDef__PriviledgedAssignment_8_1");
					put(grammarAccess.getTeamDefAccess().getWorkDefinitionAssignment_9_1(), "rule__TeamDef__WorkDefinitionAssignment_9_1");
					put(grammarAccess.getTeamDefAccess().getRelatedTaskWorkDefinitionAssignment_10_1(), "rule__TeamDef__RelatedTaskWorkDefinitionAssignment_10_1");
					put(grammarAccess.getTeamDefAccess().getAccessContextIdAssignment_11_1(), "rule__TeamDef__AccessContextIdAssignment_11_1");
					put(grammarAccess.getTeamDefAccess().getVersionAssignment_12_1(), "rule__TeamDef__VersionAssignment_12_1");
					put(grammarAccess.getTeamDefAccess().getChildrenAssignment_13_2_1(), "rule__TeamDef__ChildrenAssignment_13_2_1");
					put(grammarAccess.getActionableItemDefAccess().getNameAssignment_0(), "rule__ActionableItemDef__NameAssignment_0");
					put(grammarAccess.getActionableItemDefAccess().getAiDefOptionAssignment_1(), "rule__ActionableItemDef__AiDefOptionAssignment_1");
					put(grammarAccess.getActionableItemDefAccess().getActiveAssignment_2_1_1(), "rule__ActionableItemDef__ActiveAssignment_2_1_1");
					put(grammarAccess.getActionableItemDefAccess().getActionableAssignment_2_2_1(), "rule__ActionableItemDef__ActionableAssignment_2_2_1");
					put(grammarAccess.getActionableItemDefAccess().getLeadAssignment_2_3_1(), "rule__ActionableItemDef__LeadAssignment_2_3_1");
					put(grammarAccess.getActionableItemDefAccess().getStaticIdAssignment_2_4_1(), "rule__ActionableItemDef__StaticIdAssignment_2_4_1");
					put(grammarAccess.getActionableItemDefAccess().getTeamDefAssignment_2_5_1(), "rule__ActionableItemDef__TeamDefAssignment_2_5_1");
					put(grammarAccess.getActionableItemDefAccess().getAccessContextIdAssignment_2_6_1(), "rule__ActionableItemDef__AccessContextIdAssignment_2_6_1");
					put(grammarAccess.getActionableItemDefAccess().getChildrenAssignment_2_7_2_1(), "rule__ActionableItemDef__ChildrenAssignment_2_7_2_1");
					put(grammarAccess.getVersionDefAccess().getNameAssignment_0(), "rule__VersionDef__NameAssignment_0");
					put(grammarAccess.getVersionDefAccess().getActiveAssignment_2_1(), "rule__VersionDef__ActiveAssignment_2_1");
					put(grammarAccess.getVersionDefAccess().getStaticIdAssignment_3_1(), "rule__VersionDef__StaticIdAssignment_3_1");
					put(grammarAccess.getVersionDefAccess().getNextAssignment_4_1(), "rule__VersionDef__NextAssignment_4_1");
					put(grammarAccess.getVersionDefAccess().getReleasedAssignment_5_1(), "rule__VersionDef__ReleasedAssignment_5_1");
					put(grammarAccess.getVersionDefAccess().getAllowCreateBranchAssignment_6_1(), "rule__VersionDef__AllowCreateBranchAssignment_6_1");
					put(grammarAccess.getVersionDefAccess().getAllowCommitBranchAssignment_7_1(), "rule__VersionDef__AllowCommitBranchAssignment_7_1");
					put(grammarAccess.getVersionDefAccess().getBaselineBranchGuidAssignment_8_1(), "rule__VersionDef__BaselineBranchGuidAssignment_8_1");
					put(grammarAccess.getVersionDefAccess().getParallelVersionAssignment_9_1(), "rule__VersionDef__ParallelVersionAssignment_9_1");
					put(grammarAccess.getWorkDefAccess().getNameAssignment_0(), "rule__WorkDef__NameAssignment_0");
					put(grammarAccess.getWorkDefAccess().getIdAssignment_2_1(), "rule__WorkDef__IdAssignment_2_1");
					put(grammarAccess.getWorkDefAccess().getStartStateAssignment_3_1(), "rule__WorkDef__StartStateAssignment_3_1");
					put(grammarAccess.getWorkDefAccess().getWidgetDefsAssignment_4(), "rule__WorkDef__WidgetDefsAssignment_4");
					put(grammarAccess.getWorkDefAccess().getDecisionReviewDefsAssignment_5(), "rule__WorkDef__DecisionReviewDefsAssignment_5");
					put(grammarAccess.getWorkDefAccess().getPeerReviewDefsAssignment_6(), "rule__WorkDef__PeerReviewDefsAssignment_6");
					put(grammarAccess.getWorkDefAccess().getStatesAssignment_7(), "rule__WorkDef__StatesAssignment_7");
					put(grammarAccess.getWidgetDefAccess().getNameAssignment_1(), "rule__WidgetDef__NameAssignment_1");
					put(grammarAccess.getWidgetDefAccess().getAttributeNameAssignment_3_1(), "rule__WidgetDef__AttributeNameAssignment_3_1");
					put(grammarAccess.getWidgetDefAccess().getDescriptionAssignment_4_1(), "rule__WidgetDef__DescriptionAssignment_4_1");
					put(grammarAccess.getWidgetDefAccess().getXWidgetNameAssignment_5_1(), "rule__WidgetDef__XWidgetNameAssignment_5_1");
					put(grammarAccess.getWidgetDefAccess().getDefaultValueAssignment_6_1(), "rule__WidgetDef__DefaultValueAssignment_6_1");
					put(grammarAccess.getWidgetDefAccess().getHeightAssignment_7_1(), "rule__WidgetDef__HeightAssignment_7_1");
					put(grammarAccess.getWidgetDefAccess().getOptionAssignment_8_1(), "rule__WidgetDef__OptionAssignment_8_1");
					put(grammarAccess.getWidgetRefAccess().getWidgetAssignment_1(), "rule__WidgetRef__WidgetAssignment_1");
					put(grammarAccess.getAttrWidgetAccess().getAttributeNameAssignment_1(), "rule__AttrWidget__AttributeNameAssignment_1");
					put(grammarAccess.getAttrWidgetAccess().getOptionAssignment_2_1(), "rule__AttrWidget__OptionAssignment_2_1");
					put(grammarAccess.getStateDefAccess().getNameAssignment_1(), "rule__StateDef__NameAssignment_1");
					put(grammarAccess.getStateDefAccess().getDescriptionAssignment_3_1(), "rule__StateDef__DescriptionAssignment_3_1");
					put(grammarAccess.getStateDefAccess().getPageTypeAssignment_5(), "rule__StateDef__PageTypeAssignment_5");
					put(grammarAccess.getStateDefAccess().getOrdinalAssignment_7(), "rule__StateDef__OrdinalAssignment_7");
					put(grammarAccess.getStateDefAccess().getTransitionStatesAssignment_8(), "rule__StateDef__TransitionStatesAssignment_8");
					put(grammarAccess.getStateDefAccess().getRulesAssignment_9_1(), "rule__StateDef__RulesAssignment_9_1");
					put(grammarAccess.getStateDefAccess().getDecisionReviewsAssignment_10(), "rule__StateDef__DecisionReviewsAssignment_10");
					put(grammarAccess.getStateDefAccess().getPeerReviewsAssignment_11(), "rule__StateDef__PeerReviewsAssignment_11");
					put(grammarAccess.getStateDefAccess().getPercentWeightAssignment_12_1(), "rule__StateDef__PercentWeightAssignment_12_1");
					put(grammarAccess.getStateDefAccess().getRecommendedPercentCompleteAssignment_13_1(), "rule__StateDef__RecommendedPercentCompleteAssignment_13_1");
					put(grammarAccess.getStateDefAccess().getColorAssignment_14_1(), "rule__StateDef__ColorAssignment_14_1");
					put(grammarAccess.getStateDefAccess().getLayoutAssignment_15(), "rule__StateDef__LayoutAssignment_15");
					put(grammarAccess.getDecisionReviewRefAccess().getDecisionReviewAssignment_1(), "rule__DecisionReviewRef__DecisionReviewAssignment_1");
					put(grammarAccess.getDecisionReviewDefAccess().getNameAssignment_1(), "rule__DecisionReviewDef__NameAssignment_1");
					put(grammarAccess.getDecisionReviewDefAccess().getTitleAssignment_4(), "rule__DecisionReviewDef__TitleAssignment_4");
					put(grammarAccess.getDecisionReviewDefAccess().getDescriptionAssignment_6(), "rule__DecisionReviewDef__DescriptionAssignment_6");
					put(grammarAccess.getDecisionReviewDefAccess().getRelatedToStateAssignment_7_1(), "rule__DecisionReviewDef__RelatedToStateAssignment_7_1");
					put(grammarAccess.getDecisionReviewDefAccess().getBlockingTypeAssignment_9(), "rule__DecisionReviewDef__BlockingTypeAssignment_9");
					put(grammarAccess.getDecisionReviewDefAccess().getStateEventAssignment_11(), "rule__DecisionReviewDef__StateEventAssignment_11");
					put(grammarAccess.getDecisionReviewDefAccess().getAssigneeRefsAssignment_12_1(), "rule__DecisionReviewDef__AssigneeRefsAssignment_12_1");
					put(grammarAccess.getDecisionReviewDefAccess().getAutoTransitionToDecisionAssignment_13_1(), "rule__DecisionReviewDef__AutoTransitionToDecisionAssignment_13_1");
					put(grammarAccess.getDecisionReviewDefAccess().getOptionsAssignment_14(), "rule__DecisionReviewDef__OptionsAssignment_14");
					put(grammarAccess.getDecisionReviewOptAccess().getNameAssignment_1(), "rule__DecisionReviewOpt__NameAssignment_1");
					put(grammarAccess.getDecisionReviewOptAccess().getFollowupAssignment_2(), "rule__DecisionReviewOpt__FollowupAssignment_2");
					put(grammarAccess.getPeerReviewRefAccess().getPeerReviewAssignment_1(), "rule__PeerReviewRef__PeerReviewAssignment_1");
					put(grammarAccess.getPeerReviewDefAccess().getNameAssignment_1(), "rule__PeerReviewDef__NameAssignment_1");
					put(grammarAccess.getPeerReviewDefAccess().getTitleAssignment_3_1(), "rule__PeerReviewDef__TitleAssignment_3_1");
					put(grammarAccess.getPeerReviewDefAccess().getDescriptionAssignment_5(), "rule__PeerReviewDef__DescriptionAssignment_5");
					put(grammarAccess.getPeerReviewDefAccess().getLocationAssignment_6_1(), "rule__PeerReviewDef__LocationAssignment_6_1");
					put(grammarAccess.getPeerReviewDefAccess().getRelatedToStateAssignment_7_1(), "rule__PeerReviewDef__RelatedToStateAssignment_7_1");
					put(grammarAccess.getPeerReviewDefAccess().getBlockingTypeAssignment_9(), "rule__PeerReviewDef__BlockingTypeAssignment_9");
					put(grammarAccess.getPeerReviewDefAccess().getStateEventAssignment_11(), "rule__PeerReviewDef__StateEventAssignment_11");
					put(grammarAccess.getPeerReviewDefAccess().getAssigneeRefsAssignment_12_1(), "rule__PeerReviewDef__AssigneeRefsAssignment_12_1");
					put(grammarAccess.getFollowupRefAccess().getAssigneeRefsAssignment_1_1(), "rule__FollowupRef__AssigneeRefsAssignment_1_1");
					put(grammarAccess.getUserByUserIdAccess().getUserIdAssignment_1(), "rule__UserByUserId__UserIdAssignment_1");
					put(grammarAccess.getUserByNameAccess().getUserNameAssignment_1(), "rule__UserByName__UserNameAssignment_1");
					put(grammarAccess.getToStateAccess().getStateAssignment_1(), "rule__ToState__StateAssignment_1");
					put(grammarAccess.getToStateAccess().getOptionsAssignment_2(), "rule__ToState__OptionsAssignment_2");
					put(grammarAccess.getLayoutDefAccess().getLayoutItemsAssignment_2(), "rule__LayoutDef__LayoutItemsAssignment_2");
					put(grammarAccess.getLayoutCopyAccess().getStateAssignment_1(), "rule__LayoutCopy__StateAssignment_1");
					put(grammarAccess.getCompositeAccess().getNumColumnsAssignment_3(), "rule__Composite__NumColumnsAssignment_3");
					put(grammarAccess.getCompositeAccess().getLayoutItemsAssignment_4(), "rule__Composite__LayoutItemsAssignment_4");
					put(grammarAccess.getCompositeAccess().getOptionsAssignment_5_1(), "rule__Composite__OptionsAssignment_5_1");
				}
			};
		}
		return nameMappings.get(element);
	}
	
	@Override
	protected Collection<FollowElement> getFollowElements(AbstractInternalContentAssistParser parser) {
		try {
			org.eclipse.osee.ats.dsl.ui.contentassist.antlr.internal.InternalAtsDslParser typedParser = (org.eclipse.osee.ats.dsl.ui.contentassist.antlr.internal.InternalAtsDslParser) parser;
			typedParser.entryRuleAtsDsl();
			return typedParser.getFollowElements();
		} catch(RecognitionException ex) {
			throw new RuntimeException(ex);
		}		
	}
	
	@Override
	protected String[] getInitialHiddenTokens() {
		return new String[] { "RULE_WS", "RULE_ML_COMMENT", "RULE_SL_COMMENT" };
	}
	
	public AtsDslGrammarAccess getGrammarAccess() {
		return this.grammarAccess;
	}
	
	public void setGrammarAccess(AtsDslGrammarAccess grammarAccess) {
		this.grammarAccess = grammarAccess;
	}
}
