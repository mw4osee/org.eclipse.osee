/*
 * Created on Oct 9, 2009
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.coverage.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.coverage.editor.ICoverageEditorItem;
import org.eclipse.osee.coverage.editor.xmerge.CoverageMergeXViewer;
import org.eclipse.osee.coverage.editor.xmerge.XCoverageMergeViewer;
import org.eclipse.osee.coverage.util.CoverageImage;
import org.eclipse.osee.framework.ui.skynet.ImageManager;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

/**
 * @author Donald G. Dunne
 */
public class LinkWithImportItemAction extends Action {
   private XCoverageMergeViewer packageXViewer;
   private XCoverageMergeViewer importXViewer;

   public LinkWithImportItemAction() {
      super("Link with Import Item", Action.AS_CHECK_BOX);
   }

   private void updateSelection() {
      if (isChecked() && ((ISelectedCoverageEditorItem) importXViewer.getXViewer()).getSelectedCoverageEditorItems().size() == 1) {
         ICoverageEditorItem importCoverageEditorItem =
               ((ISelectedCoverageEditorItem) importXViewer.getXViewer()).getSelectedCoverageEditorItems().iterator().next();
         ICoverageEditorItem packageCoverageEditorItem =
               ((CoverageMergeXViewer) importXViewer.getXViewer()).getPackageItemForImportItem(
                     importCoverageEditorItem, true);
         if (packageCoverageEditorItem != null) {
            ((ISelectedCoverageEditorItem) packageXViewer.getXViewer()).setSelectedCoverageEditorItem(packageCoverageEditorItem);
         }
      }
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(CoverageImage.LINK);
   }

   @Override
   public void run() {
      updateSelection();
   }

   public void setPackageXViewer(XCoverageMergeViewer packageXViewer) {
      this.packageXViewer = packageXViewer;
   }

   public XCoverageMergeViewer getImportXViewer() {
      return importXViewer;
   }

   public void setImportXViewer(XCoverageMergeViewer importXViewer) {
      this.importXViewer = importXViewer;
      this.importXViewer.getXViewer().getTree().addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            updateSelection();
         }
      });

   }
}
