/*
 * Created on Feb 9, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.jdk.core.util.io.xml;

import java.io.IOException;
import java.util.Collection;

/**
 * @author Ryan D. Brooks
 */
public abstract class AbstractSheetWriter implements ISheetWriter {
   private boolean startRow;
   private int defaultCellIndex;

   public AbstractSheetWriter() {
      startRow = true;
      defaultCellIndex = 0;
   }

   /**
    * must be called by subclasses in their implementations of writeCell(String data, int cellIndex)
    * 
    * @throws IOException
    */
   protected void startRowIfNecessary() throws IOException {
      if (startRow) {
         startRow();
         startRow = false;
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.jdk.core.util.io.xml.excel.ISheetWriter#writeRow(java.util.Collection)
    */
   public void writeRow(Collection<String> row) throws IOException {
      writeRow(row.toArray(new String[row.size()]));
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.jdk.core.util.io.xml.excel.ISheetWriter#writeRow(java.lang.String)
    */
   public void writeRow(String... row) throws IOException {
      for (int i = 0; i < row.length; i++) {
         writeCell(row[i], i);
      }

      endRow();
   }

   public void writeCell(String data, int cellIndex) throws IOException {
      startRowIfNecessary();
      defaultCellIndex = cellIndex + 1;
      writeCellText(data, cellIndex);
   }

   public void endRow() throws IOException {
      startRowIfNecessary();
      startRow = true;
      defaultCellIndex = 0;
      writeEndRow();
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.jdk.core.util.io.xml.ISheetWriter#writeCell(java.lang.String)
    */
   public void writeCell(String cellData) throws IOException {
      writeCell(cellData, defaultCellIndex);
   }

   protected abstract void startRow() throws IOException;

   protected abstract void writeEndRow() throws IOException;

   protected abstract void writeCellText(String data, int cellIndex) throws IOException;
}