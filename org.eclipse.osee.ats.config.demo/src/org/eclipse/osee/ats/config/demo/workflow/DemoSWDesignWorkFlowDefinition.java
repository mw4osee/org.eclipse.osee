/*
 * Created on Jun 3, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.config.demo.workflow;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.ats.workflow.flow.TeamWorkflowDefinition;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkItemDefinition;

/**
 * @author Donald G. Dunne
 */
public class DemoSWDesignWorkFlowDefinition extends TeamWorkflowDefinition {

   public static String ID = "demo.swdesign";

   public DemoSWDesignWorkFlowDefinition() {
      super("Demo SW Design Work Flow Definition", ID, TeamWorkflowDefinition.ID);
   }

   public static List<WorkItemDefinition> getWorkItemDefinitions() {
      List<WorkItemDefinition> workItems = new ArrayList<WorkItemDefinition>();
      workItems.add(new DemoSWDesignWorkFlowDefinition());
      return workItems;
   }

}
