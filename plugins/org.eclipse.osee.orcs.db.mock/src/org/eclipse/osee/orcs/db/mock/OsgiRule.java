/*********************************************************************
 * Copyright (c) 2012 Boeing
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

package org.eclipse.osee.orcs.db.mock;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.framework.jdk.core.annotation.AnnotationProcessor;
import org.eclipse.osee.framework.jdk.core.annotation.FieldAnnotationHandler;
import org.eclipse.osee.orcs.db.mock.internal.OsgiServiceFieldAnnotationHandler;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * @author Roberto E. Escobar
 */
public class OsgiRule extends TestWatcher {

   private static final AnnotationProcessor processor = createProcessor();
   private final Object[] objects;

   public OsgiRule(Object... objects) {
      this.objects = objects;
   }

   @Override
   protected void starting(Description description) {
      super.starting(description);
      try {
         processor.initAnnotations(objects);
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }
   }

   private static AnnotationProcessor createProcessor() {
      Map<Class<? extends Annotation>, FieldAnnotationHandler<?>> annotationHandlers =
         new HashMap<>();

      annotationHandlers.put(OsgiService.class, new OsgiServiceFieldAnnotationHandler());
      return new AnnotationProcessor(annotationHandlers);
   }
}