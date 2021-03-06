package com.gentics.mesh.core.graphql;

import com.gentics.mesh.FieldUtil;
import com.gentics.mesh.core.rest.node.NodeCreateRequest;
import com.gentics.mesh.core.rest.schema.impl.SchemaReferenceImpl;
import com.gentics.mesh.core.rest.user.NodeReference;
import com.gentics.mesh.parameter.impl.NodeParametersImpl;
import com.gentics.mesh.test.TestSize;
import com.gentics.mesh.test.context.MeshTestSetting;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.List;

import static com.gentics.mesh.test.ClientHelper.call;
import static com.gentics.mesh.test.TestDataProvider.PROJECT_NAME;

@MeshTestSetting(useElasticsearch = true, testSize = TestSize.FULL, startServer = true)
@RunWith(Parameterized.class)
public class GraphQLNodeScrollSearchEndpointTest extends AbstractGraphQLSearchEndpointTest {

	@Parameters(name = "query={0}")
	public static List<String> paramData() {
		List<String> testQueries = new ArrayList<>();
		testQueries.add("node-elasticsearch-scroll-query");
		return testQueries;
	}

	public GraphQLNodeScrollSearchEndpointTest(String queryName) {
		super(queryName);
	}

	@Before
	public void createNodes() {
		String parentNodeUuid = tx(() -> folder("2015").getUuid());
		for (int i = 0; i < 100; i++) {
			NodeCreateRequest nodeCreateRequest = new NodeCreateRequest();
			nodeCreateRequest.setParentNode(new NodeReference().setUuid(parentNodeUuid));
			nodeCreateRequest.setSchema(new SchemaReferenceImpl().setName("content"));
			nodeCreateRequest.setLanguage("en");
			nodeCreateRequest.getFields().put("slug", FieldUtil.createStringField("test" + i));
			nodeCreateRequest.getFields().put("teaser", FieldUtil.createStringField("some teaser"));
			nodeCreateRequest.getFields().put("content", FieldUtil.createStringField("Blessed mealtime again!"));
			call(() -> client().createNode(PROJECT_NAME, nodeCreateRequest, new NodeParametersImpl().setLanguages("en")));
		}
	}
}
