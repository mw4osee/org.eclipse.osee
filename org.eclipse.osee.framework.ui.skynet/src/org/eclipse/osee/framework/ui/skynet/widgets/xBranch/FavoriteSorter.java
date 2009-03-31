/**
 * 
 */
package org.eclipse.osee.framework.ui.skynet.widgets.xBranch;

import java.util.logging.Level;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerSorter;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;

/**
 * @author Jeff C. Phillips
 */
public class FavoriteSorter extends XViewerSorter {
   private boolean favoritesFirst;

   /**
    * @param labelProvider
    */
   public FavoriteSorter(XViewer viewer) {
      super(viewer);
//      super(labelProvider);

      this.favoritesFirst = false;
   }

   @Override
   public int compare(Viewer viewer, Object o1, Object o2) {

      if (favoritesFirst) {
         if (o1 instanceof Branch && o2 instanceof Branch) {
            try {
               User user = UserManager.getUser();
               boolean fav1 = user.isFavoriteBranch((Branch) o1);
               boolean fav2 = user.isFavoriteBranch((Branch) o2);

               if (fav1 ^ fav2) {
                  return fav1 ? -1 : 1;
               }
            } catch (OseeCoreException ex) {
               OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
            }
         } else if (o1 instanceof Branch && !(o2 instanceof Branch)) {
            return -1;
         } else if (!(o1 instanceof Branch) && o2 instanceof Branch) {
            return 1;
         }
      }
      return super.compare(viewer, o1, o2);
   }

   /**
    * @return Returns the favoritesFirst.
    */
   public boolean isFavoritesFirst() {
      return favoritesFirst;
   }

   /**
    * @param favoritesFirst The favoritesFirst to set.
    */
   public void setFavoritesFirst(boolean favoritesFirst) {
      this.favoritesFirst = favoritesFirst;
   }
}