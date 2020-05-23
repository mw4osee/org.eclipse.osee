/*********************************************************************
 * Copyright (c) 2016 Boeing
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

package org.eclipse.osee.framework.jdk.core.type;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author Angel Avila
 */
public class NamedIdSerializer extends StdSerializer<NamedId> {

   public NamedIdSerializer() {
      super(NamedId.class);
   }

   @Override
   public void serialize(NamedId id, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      jgen.writeStartObject();
      jgen.writeStringField("id", id.getIdString());
      jgen.writeStringField("name", id.getName());
      jgen.writeEndObject();
   }

   /**
    * Default implementation will write type prefix, call regular serialization method (since assumption is that value
    * itself does not need JSON Array or Object start/end markers), and then write type suffix. This should work for
    * most cases; some sub-classes may want to change this behavior.
    */
   @Override
   public void serializeWithType(NamedId id, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonGenerationException {
      typeSer.writeTypePrefixForScalar(id, jgen);
      serialize(id, jgen, provider);
      typeSer.writeTypeSuffixForScalar(id, jgen);
   }

   @Override
   public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
      return createSchemaNode("string", true);
   }
}
