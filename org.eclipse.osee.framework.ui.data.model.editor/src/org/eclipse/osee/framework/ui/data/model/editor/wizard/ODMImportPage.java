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
package org.eclipse.osee.framework.ui.data.model.editor.wizard;

import java.sql.DatabaseMetaData;
import java.util.logging.Level;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.data.model.editor.ODMEditorActivator;
import org.eclipse.osee.framework.ui.swt.StackedViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Widget;

/**
 * @author Roberto E. Escobar
 */
public class ODMImportPage extends WizardPage {

   private static final String DATASTORE_OPTIONS = "data.store.import.options";
   private static final String XML_FILE_OPTIONS = "xml.import.options";

   private StackedViewer stackedViewer;
   private boolean isDataStoreExport;

   private Button xmlExportButton;
   private Button dataStoreExportButton;

   private FileOrFolderSelectPanel xmlSingleOption;
   private FileOrFolderSelectPanel xmlMultiOption;

   protected ODMImportPage(String pageName) {
      super(pageName, "Select import location", null);
      setDescription("Select an import location");
      isDataStoreExport = false;
   }

   public boolean isDataStoreExport() {
      return isDataStoreExport;
   }

   /* (non-Javadoc)
    * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
    */
   @Override
   public void createControl(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      composite.setLayout(new GridLayout());
      composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

      dataStoreExportButton = createDataStoreButton(composite);

      xmlExportButton = new Button(composite, SWT.CHECK);
      xmlExportButton.setText("From XML");

      stackedViewer = new StackedViewer(composite, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.marginWidth = 0;
      layout.marginHeight = 0;
      stackedViewer.setLayout(layout);
      stackedViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      stackedViewer.addControl(DATASTORE_OPTIONS, createDataStoreOptions(stackedViewer.getStackComposite()));
      stackedViewer.addControl(XML_FILE_OPTIONS, createXmlFileOptions(stackedViewer.getStackComposite()));

      SelectionAdapter listener = new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            Object object = e.getSource();
            if (object instanceof Button) {
               Button button = (Button) object;
               setImportFrom(button.equals(dataStoreExportButton) && button.getSelection());
            }
         }
      };

      setImportFrom(isDataStoreExport);

      xmlExportButton.addSelectionListener(listener);
      dataStoreExportButton.addSelectionListener(listener);

      setControl(composite);
   }

   private Control createDataStoreOptions(Composite parent) {
      Group group = new Group(parent, SWT.NONE);
      group.setLayout(new GridLayout());
      group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
      group.setText("DataStore Import Options");

      //      dataStoreBackupOption =
      //            FileOrFolderSelectPanel.createFileSelectPanel(group, SWT.NONE, "Backup Data Store Types",
      //                  ButtonType.CHECK_BOX, new String[] {"xml"});
      //      dataStoreBackupOption.setDefaultFileName("osee.types.db.backup.xml");

      return group;
   }

   private Control createXmlFileOptions(Composite parent) {
      Group group = new Group(parent, SWT.NONE);
      group.setLayout(new GridLayout());
      group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
      group.setText("Xml Import Options");

      //      xmlSingleOption =
      //            FileOrFolderSelectPanel.createFileSelectPanel(group, SWT.NONE, "Export as single file",
      //                  ButtonType.CHECK_BOX, new String[] {"xml"});
      //      xmlSingleOption.setDefaultFileName("osee.types.xml");
      //
      //      xmlMultiOption =
      //            FileOrFolderSelectPanel.createFolderSelectPanel(group, SWT.NONE, "Export as multiple files",
      //                  ButtonType.CHECK_BOX);
      //
      //      Listener selectListener = new Listener() {
      //
      //         @Override
      //         public void handleEvent(Event event) {
      //            if (event.widget instanceof Button) {
      //               Button button = (Button) event.widget;
      //               if (button.getText().equals("Export as multiple files") && xmlMultiOption.isSelected()) {
      //                  xmlSingleOption.setSelected(false);
      //               } else if (button.getText().equals("Export as single file") && xmlSingleOption.isSelected()) {
      //                  xmlMultiOption.setSelected(false);
      //               }
      //            }
      //         }
      //      };
      //
      //      xmlMultiOption.addListener(selectListener);
      //      xmlSingleOption.addListener(selectListener);
      return group;
   }

   private Button createDataStoreButton(Composite parent) {
      Button dataStoreButton = new Button(parent, SWT.CHECK);
      String message = null;
      try {
         DatabaseMetaData meta = ConnectionHandler.getMetaData();
         String product = meta.getDatabaseProductName();
         int majorVersion = meta.getDatabaseMajorVersion();
         int minorVersion = meta.getDatabaseMinorVersion();
         message =
               String.format("%s %s.%s - %s as %s", product, majorVersion, minorVersion,
                     ClientSessionManager.getDataStoreName(), ClientSessionManager.getDataStoreLoginName());

      } catch (Exception ex) {
         OseeLog.log(ODMEditorActivator.class, Level.WARNING, ex);
         message = "Data Store";
      }
      dataStoreButton.setText(String.format("%s", message));
      return dataStoreButton;
   }

   private void setImportFrom(boolean isDataStoreExport) {
      this.isDataStoreExport = isDataStoreExport;
      if (isWidgetValid(dataStoreExportButton) && isWidgetValid(xmlExportButton) && isWidgetValid(stackedViewer)) {
         if (isDataStoreExport) {
            xmlExportButton.setSelection(false);
            dataStoreExportButton.setSelection(true);
            stackedViewer.displayArea(DATASTORE_OPTIONS);
         } else {
            dataStoreExportButton.setSelection(false);
            xmlExportButton.setSelection(true);
            stackedViewer.displayArea(XML_FILE_OPTIONS);
         }
      }
   }

   private boolean isWidgetValid(Widget widget) {
      return widget != null & !widget.isDisposed();
   }

}
