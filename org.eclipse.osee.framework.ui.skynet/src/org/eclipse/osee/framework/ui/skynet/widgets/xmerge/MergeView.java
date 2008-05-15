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

package org.eclipse.osee.framework.ui.skynet.widgets.xmerge;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.osee.framework.skynet.core.access.AccessControlManager;
import org.eclipse.osee.framework.skynet.core.access.PermissionEnum;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchPersistenceManager;
import org.eclipse.osee.framework.skynet.core.conflict.Conflict;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.AbstractSelectionEnabledHandler;
import org.eclipse.osee.framework.ui.plugin.util.Commands;
import org.eclipse.osee.framework.ui.plugin.util.Displays;
import org.eclipse.osee.framework.ui.plugin.util.Jobs;
import org.eclipse.osee.framework.ui.skynet.SkynetContributionItem;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.ats.IActionable;
import org.eclipse.osee.framework.ui.skynet.branch.BranchView;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.part.ViewPart;

/**
 * @see ViewPart
 * @author Donald G. Dunne
 */
public class MergeView extends ViewPart implements IActionable {
   private static final RendererManager rendererManager = RendererManager.getInstance();
   private static final AccessControlManager accessControlManager = AccessControlManager.getInstance();
   public static final String VIEW_ID = "org.eclipse.osee.framework.ui.skynet.widgets.xmerge.MergeView";
   private static String HELP_CONTEXT_ID = "MergeManagerView";
   private XMergeViewer xMergeViewer;
   private Conflict[] conflicts;

   /*
    *   Code development
    *   BranchView.getBranchView().
    */

   private IHandlerService handlerService;

   /**
    * @author Donald G. Dunne
    */
   public MergeView() {
   }

   public static void openViewUpon(final Conflict[] conflicts) {
      Job job = new Job("Open Merge View") {

         @Override
         protected IStatus run(IProgressMonitor monitor) {
            Displays.ensureInDisplayThread(new Runnable() {
               public void run() {
                  try {
                     if (conflicts.length == 0) {
                        AWorkbench.popup("Attention:", "There are no conflicts between this Branch and it's Parent.");
                     } else {
                        IWorkbenchPage page = AWorkbench.getActivePage();
                        MergeView mergeView =
                              (MergeView) page.showView(MergeView.VIEW_ID,
                                    String.valueOf(conflicts[0].getMergeBranchID()), IWorkbenchPage.VIEW_VISIBLE);
                        mergeView.explore(conflicts);
                     }
                  } catch (Exception ex) {
                     OSEELog.logException(SkynetGuiPlugin.class, ex, true);
                  }
               }
            });

            monitor.done();
            return Status.OK_STATUS;
         }
      };

      Jobs.startJob(job);
   }

   @Override
   public void dispose() {
      super.dispose();
   }

   public void setFocus() {
   }

   /*
    * @see IWorkbenchPart#createPartControl(Composite)
    */
   public void createPartControl(Composite parent) {
      /*
       * Create a grid layout object so the text and treeviewer are layed out the way I want.
       */

      //      if (!DbConnectionExceptionComposite.dbConnectionIsOk(parent)) return;
      PlatformUI.getWorkbench().getService(IHandlerService.class);
      handlerService = (IHandlerService) getSite().getService(IHandlerService.class);

      GridLayout layout = new GridLayout();
      layout.numColumns = 1;
      layout.verticalSpacing = 0;
      layout.marginWidth = 0;
      layout.marginHeight = 0;
      parent.setLayout(layout);
      parent.setLayoutData(new GridData(GridData.FILL_BOTH));

      //      PlatformUI.getWorkbench().getService(IHandlerService.class);
      //      handlerService = (IHandlerService) getSite().getService(IHandlerService.class);

      xMergeViewer = new XMergeViewer();
      xMergeViewer.setDisplayLabel(false);
      xMergeViewer.createWidgets(parent, 1);

      try {
         if (conflicts != null) xMergeViewer.setConflicts(conflicts);
      } catch (SQLException ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, true);
      }

      MenuManager menuManager = new MenuManager();
      menuManager.setRemoveAllWhenShown(true);
      menuManager.addMenuListener(new IMenuListener() {
         public void menuAboutToShow(IMenuManager manager) {
            MenuManager menuManager = (MenuManager) manager;
            fillPopupMenu(menuManager);
         }

         private void fillPopupMenu(MenuManager menuManager) {
            addDestBranchDefaultMenuItem(menuManager);
            addSourceBranchDefaultMenuItem(menuManager);
            menuManager.add(new Separator());
            addPreviewMenuItem(menuManager);
            menuManager.add(new Separator());
            menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
         }
      });

      xMergeViewer.getXViewer().getTree().setMenu(menuManager.createContextMenu(xMergeViewer.getXViewer().getTree()));

      createDestBranchDefaultMenuItem(menuManager);
      createSourceBranchDefaultMenuItem(menuManager);
      menuManager.add(new Separator());
      createPreviewMenuItem(menuManager);
      menuManager.add(new Separator());
      menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

      SkynetContributionItem.addTo(this, true);
      getSite().registerContextMenu("org.eclipse.osee.framework.ui.skynetd.widgets.xmerge.MergeView", menuManager,
            xMergeViewer.getXViewer());

      getSite().setSelectionProvider(xMergeViewer.getXViewer());
      SkynetGuiPlugin.getInstance().setHelp(parent, HELP_CONTEXT_ID);
   }

   /**
    * @param menuManager
    */
   private void addPreviewMenuItem(MenuManager menuManager) {
      MenuManager subMenuManager = new MenuManager("Preview", "previewTransaction");
      menuManager.add(subMenuManager);
      addPreviewItems(subMenuManager, "Preview Source Artifact");
      addPreviewItems(subMenuManager, "Preview Destination Artifact");
      addPreviewItems(subMenuManager, "Preview Merge Artifact");
   }

   /**
    * @param menuManager
    */
   private void createPreviewMenuItem(MenuManager menuManager) {
      MenuManager subMenuManager = new MenuManager("Preview", "previewTransaction");
      menuManager.add(subMenuManager);
      createPreviewItems(subMenuManager, new PreviewHandler(menuManager, 1), "Preview Source Artifact");
      createPreviewItems(subMenuManager, new PreviewHandler(menuManager, 2), "Preview Destination Artifact");
      createPreviewItems(subMenuManager, new PreviewHandler(menuManager, 3), "Preview Merge Artifact");
   }

   /**
    * @param subMenuManager
    */
   private String addPreviewItems(MenuManager subMenuManager, String command) {
      CommandContributionItem previewCommand =
            Commands.getLocalCommandContribution(getSite(), subMenuManager.getId() + command, command, null, null,
                  SkynetGuiPlugin.getInstance().getImageDescriptor("preview_artifact.gif"), null, null, null);
      subMenuManager.add(previewCommand);
      return previewCommand.getId();
   }

   /**
    * @param subMenuManager
    */
   private void createPreviewItems(MenuManager subMenuManager, PreviewHandler handler, String command) {
      handlerService.activateHandler(addPreviewItems(subMenuManager, command), handler);
   }

   /**
    * @param menuManager
    */
   private String addDestBranchDefaultMenuItem(MenuManager menuManager) {
      CommandContributionItem setDestBranchDefaultCommand =
            Commands.getLocalCommandContribution(getSite(), "setDestBranchDefaultCommand",
                  "Set Destination as Default Branch", null, null, null, "S", null,
                  "branch_manager_default_branch_menu");
      menuManager.add(setDestBranchDefaultCommand);
      return setDestBranchDefaultCommand.getId();
   }

   /**
    * @param menuManager
    */
   private void createDestBranchDefaultMenuItem(MenuManager menuManager) {

      handlerService.activateHandler(addDestBranchDefaultMenuItem(menuManager),

      new AbstractSelectionEnabledHandler(menuManager) {
         @Override
         public Object execute(ExecutionEvent event) throws ExecutionException {
            BranchView branchView = BranchView.getBranchView();
            if (branchView != null) {
               branchView.setDefaultBranch(conflicts[0].getDestBranch());
            } else {
               BranchPersistenceManager.getInstance().setDefaultBranch(conflicts[0].getDestBranch());
            }
            return null;
         }

         @Override
         public boolean isEnabled() {
            if (conflicts == null || conflicts.length == 0) return false;
            return conflicts[0].getDestBranch() != BranchPersistenceManager.getInstance().getDefaultBranch();
         }
      });
   }

   /**
    * @param menuManager
    */
   private String addSourceBranchDefaultMenuItem(MenuManager menuManager) {
      CommandContributionItem setSourceBranchDefaultCommand;
      if (conflicts != null && conflicts.length != 0 && conflicts[0].getSourceBranch() == BranchPersistenceManager.getInstance().getDefaultBranch()) {
         setSourceBranchDefaultCommand =
               Commands.getLocalCommandContribution(getSite(), "setSourceBranchDefaultCommand",
                     "Set Source as Default Branch", null, null, SkynetGuiPlugin.getInstance().getImageDescriptor(
                           "chkbox_enabled.gif"), "S", null, "branch_manager_default_branch_menu");
      } else {
         setSourceBranchDefaultCommand =
               Commands.getLocalCommandContribution(getSite(), "setSourceBranchDefaultCommand",
                     "Set Source as Default Branch", null, null, null, "S", null, "branch_manager_default_branch_menu");
      }
      menuManager.add(setSourceBranchDefaultCommand);
      return setSourceBranchDefaultCommand.getId();
   }

   /**
    * @param menuManager
    */
   private void createSourceBranchDefaultMenuItem(MenuManager menuManager) {

      handlerService.activateHandler(addSourceBranchDefaultMenuItem(menuManager),

      new AbstractSelectionEnabledHandler(menuManager) {
         @Override
         public Object execute(ExecutionEvent event) throws ExecutionException {
            BranchView branchView = BranchView.getBranchView();
            if (branchView != null) {
               branchView.setDefaultBranch(conflicts[0].getSourceBranch());
            } else {
               BranchPersistenceManager.getInstance().setDefaultBranch(conflicts[0].getSourceBranch());
            }
            return null;
         }

         @Override
         public boolean isEnabled() {
            if (conflicts == null || conflicts.length == 0) return false;
            return conflicts[0].getSourceBranch() != BranchPersistenceManager.getInstance().getDefaultBranch();
         }
      });
   }

   public void explore(Conflict[] conflicts) {
      this.conflicts = conflicts;
      try {
         if (conflicts != null) {
            xMergeViewer.setConflicts(conflicts);
         }
         setPartName("Merge Manager: ");

      } catch (SQLException ex) {
         OSEELog.logException(MergeView.class, ex, true);
      }
   }

   public String getActionDescription() {
      return "";
   }

   @Override
   public void init(IViewSite site, IMemento memento) throws PartInitException {
      super.init(site, memento);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
    */
   @Override
   public void saveState(IMemento memento) {
      super.saveState(memento);
   }

   private class PreviewHandler extends AbstractSelectionEnabledHandler {
      private static final String PREVIEW_ARTIFACT = "PREVIEW_ARTIFACT";
      private int partToPreview;
      private List<Artifact> artifacts;

      public PreviewHandler(MenuManager menuManager, int partToPreview) {
         super(menuManager);
         this.partToPreview = partToPreview;
      }

      @Override
      public Object execute(ExecutionEvent event) throws ExecutionException {
         if (!artifacts.isEmpty()) {
            rendererManager.previewInJob(artifacts, PREVIEW_ARTIFACT);
         }
         return null;
      }

      @Override
      public boolean isEnabled() {
         artifacts = new LinkedList<Artifact>();
         List<Conflict> conflicts = xMergeViewer.getSelectedConflicts();
         for (Conflict conflict : conflicts) {
            try {
               switch (partToPreview) {
                  case 1:
                     if (conflict.getSourceArtifact() != null) {
                        artifacts.add(conflict.getSourceArtifact());
                     }
                     break;
                  case 2:
                     if (conflict.getDestArtifact() != null) {
                        artifacts.add(conflict.getDestArtifact());
                     }
                     break;
                  case 3:
                     if (conflict.getArtifact() != null) {
                        artifacts.add(conflict.getArtifact());
                     }
                     break;
               }
            } catch (Exception ex) {
               OSEELog.logException(MergeView.class, ex, true);
            }
         }

         return accessControlManager.checkObjectListPermission(artifacts, PermissionEnum.READ);
      }
   }

}