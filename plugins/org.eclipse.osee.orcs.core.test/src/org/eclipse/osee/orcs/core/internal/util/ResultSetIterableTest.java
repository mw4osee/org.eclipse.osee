/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.internal.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.framework.core.exception.ItemDoesNotExist;
import org.eclipse.osee.framework.core.exception.MultipleItemsExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;

/**
 * Test Case for {@link ResultSetIterable}
 * 
 * @author Roberto E. Escobar
 */
public class ResultSetIterableTest {

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   private ResultSet<String> result;
   private Set<String> data;

   @Before
   public void init() {
      MockitoAnnotations.initMocks(this);

      data = new LinkedHashSet<String>();
      result = new ResultSetIterable<String>(data);
   }

   @Test
   public void testSizeAndEmpty() {
      data.add("a");
      data.add("b");
      data.add("c");

      assertFalse(result.isEmpty());
      assertEquals(3, result.size());

      data.clear();
      assertTrue(result.isEmpty());
      assertEquals(0, result.size());

      result.iterator();
   }

   @Test
   public void testGetExactlyOneMultipleException() throws OseeCoreException {
      data.add("a");
      data.add("b");

      thrown.expect(MultipleItemsExist.class);
      thrown.expectMessage("Multiple items found - total [2]");
      result.getExactlyOne();
   }

   @Test
   public void testGetExactlyOneNoneExistException() throws OseeCoreException {
      thrown.expect(ItemDoesNotExist.class);
      thrown.expectMessage("No item found");
      result.getExactlyOne();
   }

   @Test
   public void testGetExactlyOne() throws OseeCoreException {
      data.add("c");
      assertEquals("c", result.getExactlyOne());
   }

   @Test
   public void testGetOneOrNull() throws OseeCoreException {
      Assert.assertNull(result.getOneOrNull());

      data.add("a");
      data.add("b");
      data.add("c");
      assertEquals("a", result.getOneOrNull());
   }

   @Test
   public void testGetAtMostOneOrNullExceptionMoreThanOne() throws OseeCoreException {
      assertNull(result.getAtMostOneOrNull());

      data.add("a");
      data.add("b");
      data.add("c");
      thrown.expect(MultipleItemsExist.class);
      thrown.expectMessage("Multiple items found - total [3]");
      assertEquals("a", result.getAtMostOneOrNull());
   }

   @Test
   public void testGetAtMostOneOrNull() throws OseeCoreException {
      assertNull(result.getAtMostOneOrNull());

      data.add("a");
      assertEquals("a", result.getAtMostOneOrNull());
   }

   @Test
   public void testIterator() {
      data.add("a");
      data.add("b");
      data.add("c");

      Iterator<String> iterator = result.iterator();
      assertEquals("a", iterator.next());
      assertEquals("b", iterator.next());
      assertEquals("c", iterator.next());
   }

}
