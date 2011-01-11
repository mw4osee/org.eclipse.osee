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

package org.eclipse.osee.framework.ui.skynet.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.BranchDoesNotExist;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.IActionable;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.UniversalGroup;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.event.listener.IArtifactEventListener;
import org.eclipse.osee.framework.skynet.core.event.model.ArtifactEvent;
import org.eclipse.osee.framework.skynet.core.event.model.Sender;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.ui.plugin.OseeUiActions;
import org.eclipse.osee.framework.ui.plugin.PluginUiImage;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.ArtifactDoubleClick;
import org.eclipse.osee.framework.ui.skynet.OpenContributionItem;
import org.eclipse.osee.framework.ui.skynet.OseeStatusContributionItemFactory;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.listener.IRebuildMenuListener;
import org.eclipse.osee.framework.ui.skynet.util.DbConnectionExceptionComposite;
import org.eclipse.osee.framework.ui.skynet.widgets.XBranchSelectWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.EntryDialog;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Donald G. Dunne
 */
public class GroupExplorer extends ViewPart implements IArtifactEventListener, IActionable, IRebuildMenuListener {
   public static final String VIEW_ID = "org.eclipse.osee.framework.ui.skynet.group.GroupExplorer";
   private GroupTreeViewer treeViewer;
   private Artifact rootArt;
   private GroupExplorerItem rootItem;
   private Collection<GroupExplorerItem> selected;
   private Object[] expanded = new Object[] {};
   private XBranchSelectWidget branchSelect;
   private Branch branch;
   private GroupExplorerDragAndDrop groupExpDnd;

   private NeedProjectMenuListener needProjectListener;

   @Override
   public void createPartControl(Composite parent) {

      if (!DbConnectionExceptionComposite.dbConnectionIsOk(parent)) {
         return;
      }

      GridData gridData = new GridData();
      gridData.verticalAlignment = GridData.FILL;
      gridData.horizontalAlignment = GridData.FILL;
      gridData.grabExcessVerticalSpace = true;
      gridData.grabExcessHorizontalSpace = true;

      GridLayout gridLayout = new GridLayout(1, false);
      gridData.heightHint = 1000;
      gridData.widthHint = 1000;

      parent.setLayout(gridLayout);
      parent.setLayoutData(gridData);

      branchSelect = new XBranchSelectWidget("");
      branchSelect.setDisplayLabel(false);
      branchSelect.setSelection(branch);
      branchSelect.createWidgets(parent, 1);

      branchSelect.addListener(new Listener() {
         @Override
         public void handleEvent(Event event) {
            try {
               branch = branchSelect.getData();
               refresh();
               groupExpDnd.setBranch(branch);
            } catch (Exception ex) {
               OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
            }
         }

      });

      treeViewer = new GroupTreeViewer(this, parent);
      treeViewer.setContentProvider(new GroupContentProvider());
      treeViewer.setLabelProvider(new GroupLabelProvider());
      treeViewer.setUseHashlookup(true);
      treeViewer.addDoubleClickListener(new ArtifactDoubleClick());
      treeViewer.getControl().setLayoutData(gridData);

      OseeStatusContributionItemFactory.addTo(this, true);

      OseeEventManager.addListener(this);

      groupExpDnd = new GroupExplorerDragAndDrop(treeViewer, VIEW_ID, branch);

      getSite().setSelectionProvider(treeViewer);
      parent.layout();
      createActions();
      getViewSite().getActionBars().updateActionBars();
      setupPopupMenu();
      refresh();
   }

   @Override
   public void rebuildMenu() {
      setupPopupMenu();
   }

   public void setupPopupMenu() {

      Menu popupMenu = new Menu(treeViewer.getTree().getParent());
      needProjectListener = new NeedProjectMenuListener();
      popupMenu.addMenuListener(needProjectListener);

      OpenContributionItem contrib = new OpenContributionItem(getClass().getSimpleName() + ".open");
      contrib.fill(popupMenu, -1);

      new MenuItem(popupMenu, SWT.SEPARATOR);

      MenuItem item = new MenuItem(popupMenu, SWT.PUSH);
      item.setText("&Remove from Group");
      item.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            try {
               handleRemoveFromGroup();
            } catch (Exception ex) {
               OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
            }
         }
      });

      item = new MenuItem(popupMenu, SWT.PUSH);
      item.setText("&Delete Group");
      item.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            try {
               handleDeleteGroup();
            } catch (Exception ex) {
               OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
            }
         }
      });

      item = new MenuItem(popupMenu, SWT.PUSH);
      item.setText("&New Group");
      item.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            handleNewGroup();
         }
      });

      new MenuItem(popupMenu, SWT.SEPARATOR);

      item = new MenuItem(popupMenu, SWT.PUSH);
      item.setText("&Select All\tCtrl+A");
      item.addListener(SWT.Selection, new Listener() {
         @Override
         public void handleEvent(org.eclipse.swt.widgets.Event event) {
            treeViewer.getTree().selectAll();
         }
      });

      item = new MenuItem(popupMenu, SWT.PUSH);
      item.setText("Expand All\tCtrl+X");
      item.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            expandAll((IStructuredSelection) treeViewer.getSelection());
         }
      });

      treeViewer.getTree().setMenu(popupMenu);
   }

   protected void createActions() {
      Action refreshAction = new Action("Refresh", IAction.AS_PUSH_BUTTON) {

         @Override
         public void run() {
            refresh();
         }
      };
      refreshAction.setImageDescriptor(ImageManager.getImageDescriptor(PluginUiImage.REFRESH));
      refreshAction.setToolTipText("Refresh");

      IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
      toolbarManager.add(refreshAction);

      OseeUiActions.addBugToViewToolbar(this, this, SkynetGuiPlugin.PLUGIN_ID, VIEW_ID, "Group Explorer");
   }

   private void handleNewGroup() {
      if (branch == null) {
         AWorkbench.popup("Must select branch first");
         return;
      }
      EntryDialog ed =
         new EntryDialog(Displays.getActiveShell(), "Create New Group", null, "Enter Group Name",
            MessageDialog.QUESTION, new String[] {"OK", "Cancel"}, 0);
      if (ed.open() == 0) {
         try {
            UniversalGroup.addGroup(ed.getEntry(), branch);
            treeViewer.refresh();
         } catch (Exception ex) {
            OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
         }
      }
   }

   private void handleRemoveFromGroup() {
      if (getSelectedUniversalGroupItems().size() > 0) {
         AWorkbench.popup("ERROR", "Can't remove Group, use \"Delete Group\".");
         return;
      }
      final List<GroupExplorerItem> items = getSelectedItems();
      if (items.isEmpty()) {
         AWorkbench.popup("ERROR", "No Items Selected");
         return;
      }
      if (MessageDialog.openConfirm(Displays.getActiveShell(), "Remove From Group",
         "Remove From Group - (Artifacts will not be deleted)\n\nAre you sure?")) {
         try {
            SkynetTransaction transaction = new SkynetTransaction(branch, "Artifacts removed from group");
            for (GroupExplorerItem item : items) {
               item.getArtifact().deleteRelation(CoreRelationTypes.Universal_Grouping__Group,
                  item.getParentItem().getArtifact());
               item.getArtifact().persist(transaction);
            }
            transaction.execute();
         } catch (Exception ex) {
            OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
         }
      }
   }

   private void handleDeleteGroup() {
      final ArrayList<GroupExplorerItem> items = getSelectedUniversalGroupItems();
      if (items.isEmpty()) {
         AWorkbench.popup("ERROR", "No groups selected.");
         return;
      }
      if (getSelectedItems().size() != items.size()) {
         AWorkbench.popup("ERROR", "Only select groups to be deleted.");
         return;
      }

      try {
         String names = "";
         for (GroupExplorerItem item : items) {
            if (item.isUniversalGroup()) {
               names += String.format("%s\n", item.getArtifact().getName());
            }
         }
         if (MessageDialog.openConfirm(Displays.getActiveShell(), "Delete Groups",
            "Delete Groups - (Contained Artifacts will not be deleted)\n\n" + names + "\nAre you sure?")) {

            SkynetTransaction transaction = new SkynetTransaction(branch, "Delete Groups: " + names);
            for (GroupExplorerItem item : items) {
               item.getArtifact().deleteAndPersist(transaction);
            }
            transaction.execute();
         }
      } catch (Exception ex) {
         OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   public void storeExpandedAndSelection() {
      // Store selected so can re-select after event re-draw
      selected = getSelectedItems();
      if (treeViewer != null && !treeViewer.getTree().isDisposed()) {
         expanded = treeViewer.getExpandedElements();
      }
   }

   public void restoreExpandedAndSelection() {
      if (expanded != null && expanded.length > 0 && rootArt != null) {
         treeViewer.setExpandedElements(expanded);
      }
      if (selected != null && selected.size() > 0 && rootArt != null) {
         treeViewer.setSelection(new StructuredSelection(selected.toArray(new Object[selected.size()])));
      }
   }

   private ArrayList<GroupExplorerItem> getSelectedItems() {
      ArrayList<GroupExplorerItem> arts = new ArrayList<GroupExplorerItem>();
      Iterator<?> i = ((IStructuredSelection) treeViewer.getSelection()).iterator();
      while (i.hasNext()) {
         Object obj = i.next();
         if (obj instanceof GroupExplorerItem) {
            arts.add((GroupExplorerItem) obj);
         }
      }
      return arts;
   }

   private ArrayList<GroupExplorerItem> getSelectedUniversalGroupItems() {
      ArrayList<GroupExplorerItem> arts = new ArrayList<GroupExplorerItem>();
      Iterator<?> i = ((IStructuredSelection) treeViewer.getSelection()).iterator();
      while (i.hasNext()) {
         Object obj = i.next();
         if (obj instanceof GroupExplorerItem && ((GroupExplorerItem) obj).isUniversalGroup()) {
            arts.add((GroupExplorerItem) obj);
         }
      }
      return arts;
   }

   private void expandAll(IStructuredSelection selection) {
      Iterator<?> iter = selection.iterator();
      while (iter.hasNext()) {
         treeViewer.expandToLevel(iter.next(), AbstractTreeViewer.ALL_LEVELS);
      }
   }

   @Override
   public void setFocus() {
      // do nothing
   }

   public void refresh() {
      if (rootItem != null) {
         rootItem.dispose();
      }

      Artifact topArt = null;
      if (branch != null) {
         try {
            topArt = UniversalGroup.getTopUniversalGroupArtifact(branch);
         } catch (Exception ex) {
            OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
         }
      }
      if (topArt == null) {
         rootArt = null;
         rootItem = null;
      } else {
         rootArt = topArt;
         rootItem = new GroupExplorerItem(treeViewer, rootArt, null, this);
      }

      if (treeViewer != null) {
         treeViewer.setInput(rootItem);
      }

   }

   @Override
   public void dispose() {
      OseeEventManager.removeListener(this);
      super.dispose();
   }

   @Override
   public String getActionDescription() {
      return "";
   }

   private class NeedProjectMenuListener implements MenuListener {
      Collection<MenuItem> items;

      public NeedProjectMenuListener() {
         this.items = new LinkedList<MenuItem>();
      }

      @Override
      public void menuHidden(MenuEvent e) {
         // do nothing
      }

      @Override
      public void menuShown(MenuEvent e) {
         boolean valid = treeViewer.getInput() != null;
         for (MenuItem item : items) {
            if (!(item.getData() instanceof Exception)) {
               // Only modify
               // enabling if no
               // error is
               // associated
               item.setEnabled(valid);
            }
         }
      }
   }

   private static final String INPUT = "input";
   private static final String BRANCH_ID = "branchId";

   @Override
   public void saveState(IMemento memento) {
      super.saveState(memento);
      memento = memento.createChild(INPUT);
      if (branch != null) {
         memento.putInteger(BRANCH_ID, branch.getId());
      }
   }

   @Override
   public void init(IViewSite site, IMemento memento) throws PartInitException {
      super.init(site, memento);
      try {
         Integer branchId = null;

         if (memento != null) {
            memento = memento.getChild(INPUT);
            if (memento != null) {
               branchId = memento.getInteger(BRANCH_ID);
               if (branchId != null) {
                  try {
                     branch = BranchManager.getBranch(branchId);
                     if (branch.getBranchState().isDeleted() || branch.getArchiveState().isArchived()) {
                        branch = null;
                     }
                  } catch (BranchDoesNotExist ex) {
                     branch = null;
                  }
               }
            }
         }
      } catch (Exception ex) {
         OseeLog.log(SkynetGuiPlugin.class, Level.WARNING, "Group Explorer exception on init", ex);
      }
   }

   @Override
   public List<? extends IEventFilter> getEventFilters() {
      return null;
   }

   @Override
   public void handleArtifactEvent(ArtifactEvent artifactEvent, Sender sender) {
      if (rootArt == null || branch == null || !artifactEvent.isForBranch(branch)) {
         return;
      }
      try {
         Artifact topArt = UniversalGroup.getTopUniversalGroupArtifact(branch);
         if (topArt != null) {
            Displays.ensureInDisplayThread(new Runnable() {
               @Override
               public void run() {
                  storeExpandedAndSelection();
                  refresh();
                  restoreExpandedAndSelection();
               }
            });
            return;
         }
      } catch (Exception ex) {
         // do nothing
      }

   }

}