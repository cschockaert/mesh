package com.gentics.mesh.core.field;

import static com.gentics.mesh.demo.TestDataProvider.PROJECT_NAME;
import static com.gentics.mesh.util.MeshAssert.assertSuccess;
import static com.gentics.mesh.util.MeshAssert.latchFor;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.gentics.mesh.core.AbstractSpringVerticle;
import com.gentics.mesh.core.data.node.Node;
import com.gentics.mesh.core.rest.node.NodeCreateRequest;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.node.NodeUpdateRequest;
import com.gentics.mesh.core.rest.node.field.Field;
import com.gentics.mesh.core.rest.schema.SchemaReference;
import com.gentics.mesh.core.verticle.node.NodeVerticle;
import com.gentics.mesh.query.impl.NodeRequestParameter;
import com.gentics.mesh.test.AbstractRestVerticleTest;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;

public abstract class AbstractGraphFieldNodeVerticleTest extends AbstractRestVerticleTest {

	@Autowired
	private NodeVerticle verticle;

	@Override
	public List<AbstractSpringVerticle> getAdditionalVertices() {
		List<AbstractSpringVerticle> list = new ArrayList<>();
		list.add(verticle);
		return list;
	}

	protected NodeResponse readNode(Node node, String... expandedFieldNames) {
		NodeRequestParameter parameters = new NodeRequestParameter();
		parameters.setLanguages("en");
		parameters.setExpandedFieldNames(expandedFieldNames);
		Future<NodeResponse> future = getClient().findNodeByUuid(PROJECT_NAME, node.getUuid(), parameters);
		latchFor(future);
		assertSuccess(future);
		NodeResponse response = future.result();
		return response;
	}

	protected void createNodeAndExpectFailure(String fieldKey, Field field, HttpResponseStatus status, String bodyMessageI18nKey,
			String... i18nParams) {
		Node node = folder("2015");
		NodeCreateRequest nodeCreateRequest = new NodeCreateRequest();
		nodeCreateRequest.setParentNodeUuid(node.getUuid());
		nodeCreateRequest.setSchema(new SchemaReference().setName("folder"));
		nodeCreateRequest.setLanguage("en");
		if (fieldKey != null) {
			nodeCreateRequest.getFields().put(fieldKey, field);
		}

		Future<NodeResponse> future = getClient().createNode(PROJECT_NAME, nodeCreateRequest, new NodeRequestParameter().setLanguages("en"));
		latchFor(future);
		expectException(future, status, bodyMessageI18nKey, i18nParams);
	}

	/**
	 * Update the test node using the provided field field and field key as update data.
	 * 
	 * @param fieldKey
	 * @param field
	 * @return
	 */
	protected NodeResponse updateNode(String fieldKey, Field field) {
		return updateNode(fieldKey, field, false);
	}

	/**
	 * Update the test node using the provided field field and field key as update data.
	 * 
	 * @param fieldKey
	 * @param field
	 * @param expandAll
	 * @return
	 */
	protected NodeResponse updateNode(String fieldKey, Field field, boolean expandAll) {
		Node node = folder("2015");
		NodeUpdateRequest nodeUpdateRequest = new NodeUpdateRequest();
		nodeUpdateRequest.setSchema(new SchemaReference().setName("folder"));
		nodeUpdateRequest.setLanguage("en");
		nodeUpdateRequest.getFields().put(fieldKey, field);

		NodeRequestParameter requestParameters = new NodeRequestParameter().setLanguages("en");
		requestParameters.setExpandAll(expandAll);

		Future<NodeResponse> future = getClient().updateNode(PROJECT_NAME, node.getUuid(), nodeUpdateRequest, requestParameters);
		latchFor(future);
		assertSuccess(future);
		assertNotNull("The response could not be found in the result of the future.", future.result());
		assertNotNull("The field was not included in the response.", future.result().getFields().hasField(fieldKey));
		return future.result();
	}

	protected void updateNodeFailure(String fieldKey, Field field, HttpResponseStatus status, String bodyMessageI18nKey, String... i18nParams) {
		Node node = folder("2015");
		NodeUpdateRequest nodeUpdateRequest = new NodeUpdateRequest();
		nodeUpdateRequest.setSchema(new SchemaReference().setName("folder"));
		nodeUpdateRequest.setLanguage("en");
		nodeUpdateRequest.getFields().put(fieldKey, field);

		Future<NodeResponse> future = getClient().updateNode(PROJECT_NAME, node.getUuid(), nodeUpdateRequest,
				new NodeRequestParameter().setLanguages("en"));
		latchFor(future);
		expectException(future, status, bodyMessageI18nKey, i18nParams);
	}

	/**
	 * Read a node that already contains a filled field. Make sure the response contains the expected field data.
	 * 
	 * @throws IOException
	 */
	abstract public void testReadNodeWithExistingField() throws IOException;

	/**
	 * Update a node with a currently filled field. Change the field and make sure the changes were applied correctly.
	 */
	abstract public void testUpdateNodeFieldWithField();

	/**
	 * Create a new node and set field values. Make sure the node was correctly created and the field was populated with the correct data.
	 */
	abstract public void testCreateNodeWithField();

	/**
	 * Create a new node and set no field value for the field. Make sure the node was correctly loaded and that the field was set to an empty value. Basic
	 * fields must be set to null.
	 */
	abstract public void testCreateNodeWithNoField();

	// TODO testcases for mandatory fields? deletion testcases? We can use explicit null values to delete a field.

}