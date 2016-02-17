package com.gentics.mesh.core.schema;

import static com.gentics.mesh.util.MeshAssert.assertSuccess;
import static com.gentics.mesh.util.MeshAssert.failingLatch;
import static com.gentics.mesh.util.MeshAssert.latchFor;
import static io.netty.handler.codec.http.HttpResponseStatus.CONFLICT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.gentics.mesh.core.data.MicroschemaContainer;
import com.gentics.mesh.core.data.NodeGraphFieldContainer;
import com.gentics.mesh.core.data.node.Node;
import com.gentics.mesh.core.data.root.MeshRoot;
import com.gentics.mesh.core.rest.common.GenericMessageResponse;
import com.gentics.mesh.core.rest.micronode.MicronodeResponse;
import com.gentics.mesh.core.rest.microschema.impl.MicroschemaImpl;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.node.field.impl.StringFieldImpl;
import com.gentics.mesh.core.rest.schema.MicronodeFieldSchema;
import com.gentics.mesh.core.rest.schema.Microschema;
import com.gentics.mesh.core.rest.schema.MicroschemaReference;
import com.gentics.mesh.core.rest.schema.Schema;
import com.gentics.mesh.core.rest.schema.change.impl.SchemaChangeModel;
import com.gentics.mesh.core.rest.schema.change.impl.SchemaChangesListModel;
import com.gentics.mesh.core.rest.schema.impl.MicronodeFieldSchemaImpl;

import io.vertx.core.Future;

public class MicroschemaChangesVerticleTest extends AbstractChangesVerticleTest {

	@Test
	public void testRemoveField() throws Exception {
		// 1. Update folder schema
		Schema schema = schemaContainer("folder").getSchema();
		MicronodeFieldSchema microschemaFieldSchema = new MicronodeFieldSchemaImpl();
		microschemaFieldSchema.setName("micronodeField");
		microschemaFieldSchema.setLabel("Some label");
		microschemaFieldSchema.setAllowedMicroSchemas(new String[] { "vcard" });
		schema.addField(microschemaFieldSchema);
		schemaContainer("folder").setSchema(schema);

		// 2. Create node that uses the microschema
		MicronodeResponse micronode = new MicronodeResponse();
		MicroschemaReference ref = new MicroschemaReference();
		ref.setName("vcard");
		micronode.setMicroschema(ref);
		micronode.getFields().put("firstName", new StringFieldImpl().setString("Max"));
		micronode.getFields().put("lastName", new StringFieldImpl().setString("Mustermann"));
		NodeResponse response = createNode("micronodeField", micronode);
		Node node = MeshRoot.getInstance().getNodeRoot().findByUuid(response.getUuid()).toBlocking().single();
		assertNotNull("The node should have been created.", node);
		assertNotNull("The node should have a micronode graph field", node.getGraphFieldContainer("en").getMicronode("micronodeField"));

		// 3. Create changes
		SchemaChangesListModel listOfChanges = new SchemaChangesListModel();
		SchemaChangeModel change = SchemaChangeModel.createRemoveFieldChange("firstName");
		listOfChanges.getChanges().add(change);

		// 4. Setup eventbus bridged latch
		CountDownLatch latch = latchForMigrationCompleted();

		// 5. Invoke migration
		MicroschemaContainer container = microschemaContainer("vcard");
		assertNull("The microschema should not yet have any changes", container.getNextChange());
		Future<GenericMessageResponse> future = getClient().applyChangesToMicroschema(container.getUuid(), listOfChanges);
		latchFor(future);
		assertSuccess(future);
		container.reload();
		assertNotNull("The change should have been added to the schema.", container.getNextChange());

		// 6. Wait for migration to finish
		failingLatch(latch);

		// 7. Assert migrated node
		node.reload();
		NodeGraphFieldContainer fieldContainer = node.getGraphFieldContainer("en");
		fieldContainer.reload();
		assertNotNull("The node should have a micronode graph field", fieldContainer.getMicronode("micronodeField"));
	}

	@Test
	public void testAddField() {
		MicroschemaContainer microschema = microschemaContainer("vcard");
		assertNull("The microschema should not yet have any changes", microschema.getNextChange());
		SchemaChangesListModel listOfChanges = new SchemaChangesListModel();
		SchemaChangeModel change = SchemaChangeModel.createAddChange("newField", "html");
		listOfChanges.getChanges().add(change);

		Future<GenericMessageResponse> future = getClient().applyChangesToMicroschema(microschema.getUuid(), listOfChanges);
		latchFor(future);
		assertSuccess(future);
		microschema.reload();
		assertNotNull("The change should have been added to the schema.", microschema.getNextChange());
	}

	@Test
	public void testUpdateWithConflictingName() {
		String name = "captionedImage";
		String originalSchemaName = "vcard";
		MicroschemaContainer microschema = microschemaContainers().get(originalSchemaName);
		assertNotNull(microschema);
		Microschema request = new MicroschemaImpl();
		request.setName(name);

		Future<GenericMessageResponse> future = getClient().updateMicroschema(microschema.getUuid(), request);
		latchFor(future);
		expectException(future, CONFLICT, "schema_conflicting_name", name);
		microschema.reload();
		assertEquals("The name of the microschema was updated but it should not.", originalSchemaName, microschema.getName());
	}

}
