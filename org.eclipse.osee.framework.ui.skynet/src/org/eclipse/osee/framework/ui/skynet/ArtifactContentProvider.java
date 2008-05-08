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
package org.eclipse.osee.framework.ui.skynet;

import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osee.framework.plugin.core.config.ConfigUtil;
import org.eclipse.osee.framework.skynet.core.SkynetAuthentication;
import org.eclipse.osee.framework.skynet.core.access.AccessControlManager;
import org.eclipse.osee.framework.skynet.core.access.PermissionEnum;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactChangeListener;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactModifiedEvent;
import org.eclipse.osee.framework.skynet.core.event.SkynetEventManager;
import org.eclipse.osee.framework.skynet.core.relation.RelationModifiedEvent;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;

/**
 * The basis for the comments in this class can be found at
 * http://www.eclipse.org/articles/treeviewer-cg/TreeViewerArticle.htm
 * 
 * @author Ryan D. Brooks
 */
public class ArtifactContentProvider implements ITreeContentProvider, ArtifactChangeListener {
   private static final SkynetAuthentication skynetAuth = SkynetAuthentication.getInstance();
   private static final Logger logger = ConfigUtil.getConfigFactory().getLogger(ArtifactContentProvider.class);
   private static Object[] EMPTY_ARRAY = new Object[0];
   protected TreeViewer viewer;
   private final ArtifactExplorer artifactExplorer;
   private static final AccessControlManager accessManager = AccessControlManager.getInstance();
   private boolean noChildren;

   public ArtifactContentProvider(ArtifactExplorer artifactExplorer) {
      this.artifactExplorer = artifactExplorer;
      this.noChildren = false;
   }

   /*
    * @see IContentProvider#dispose()
    */
   public void dispose() {
   }

   /**
    * Notifies this content provider that the given viewer's input has been switched to a different element.
    * <p>
    * A typical use for this method is registering the content provider as a listener to changes on the new input (using
    * model-specific means), and deregistering the viewer from the old input. In response to these change notifications,
    * the content provider propagates the changes to the viewer.
    * </p>
    * 
    * @param viewer the viewer
    * @param oldInput the old input element, or <code>null</code> if the viewer did not previously have an input
    * @param newInput the new input element, or <code>null</code> if the viewer does not have an input
    * @see IContentProvider#inputChanged(Viewer, Object, Object)
    */
   public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      // this.viewer = (TreeViewer) viewer;

      noChildren = newInput instanceof Collection;
      // if (oldInput != null) {
      // ((Artifact) oldInput).removeListenerRecursively(this);
      // }
      // if (newInput != null) { // TODO: must handle fact that only root item has a listener added
      // ((Artifact) newInput).addListener(this);
      // }
   }

   /**
    * The tree viewer calls its content provider&#8217;s getChildren method when it needs to create or display the child
    * elements of the domain object, <b>parent </b>. This method should answer an array of domain objects that represent
    * the unfiltered children of <b>parent </b>
    * 
    * @see ITreeContentProvider#getChildren(Object)
    */
   @SuppressWarnings("unchecked")
   public Object[] getChildren(Object parentElement) {
      if (parentElement instanceof Artifact && !noChildren) {
         Artifact parentItem = (Artifact) parentElement;

         try {
            if (accessManager.checkObjectPermission(skynetAuth.getAuthenticatedUser(), parentItem, PermissionEnum.READ)) {
               Collection<Artifact> children = parentItem.getChildren();
               if (children != null) {
                  for (Artifact art : children) {
                     SkynetEventManager.getInstance().register(ArtifactModifiedEvent.class, art, artifactExplorer);
                     SkynetEventManager.getInstance().register(RelationModifiedEvent.class, art, artifactExplorer);
                  }
                  return children.toArray();
               }
            }
         } catch (SQLException ex) {
            SkynetGuiPlugin.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
         }
      } else if (parentElement instanceof Collection) {
         return ((Collection) parentElement).toArray();
      }

      return EMPTY_ARRAY;
   }

   /*
    * @see ITreeContentProvider#getParent(Object)
    */
   public Object getParent(Object element) {
      if (element instanceof Artifact) {
         try {
            return ((Artifact) element).getParent();
         } catch (SQLException ex) {
            OSEELog.logException(SkynetGuiPlugin.class, ex, true);
         }
      }
      return null;
   }

   /**
    * The tree viewer asks its content provider if the domain object represented by <b>element </b> has any children.
    * This method is used by the tree viewer to determine whether or not a plus or minus should appear on the tree
    * widget.
    * 
    * @see ITreeContentProvider#hasChildren(Object)
    */
   public boolean hasChildren(Object element) {

      if (noChildren) return false;

      /*
       * might be inefficient if getChildren is not a lightweight opperation; return true; is very
       * efficient, but will initially cause all nodes to display a plus which is removed if you try
       * to expand the node and it has no children
       */

      /*
       * If the item is an artifact, then use it's optimized check. If it is not an artifact, then
       * resort to asking the general children
       */
      if (element instanceof Artifact) {
         Artifact artifact = (Artifact) element;

         if (accessManager.checkObjectPermission(skynetAuth.getAuthenticatedUser(), artifact, PermissionEnum.READ)) {
            if (artifact.isDeleted()) return false;

            try {
               return artifact.getChildren().size() > 0;
            } catch (SQLException ex) {
               logger.log(Level.SEVERE, ex.toString(), ex);
               // Assume it has children if an error happens
               return true;
            }
         } else {
            return false;
         }
      } else {
         return getChildren(element).length > 0;
      }
   }

   /**
    * This is the method invoked by calling the <b>setInput </b> method on the tree viewer. In fact, the <b>getElements
    * </b> method is called only in response to the tree viewer's <b>setInput </b> method and should answer with the
    * appropriate domain objects of the inputElement. The <b>getElements </b> and <b>getChildren </b> methods operate in
    * a similar way. Depending on your domain objects, you may have the <b>getElements </b> simply return the result of
    * calling <b>getChildren </b>. The two methods are kept distinct because it provides a clean way to differentiate
    * between the root domain object and all other domain objects.
    * 
    * @see IStructuredContentProvider#getElements(Object)
    */
   public Object[] getElements(Object inputElement) {
      return getChildren(inputElement);
   }
}