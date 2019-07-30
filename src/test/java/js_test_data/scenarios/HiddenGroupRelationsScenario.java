/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package js_test_data.scenarios;

import com.google.common.collect.Sets;
import guru.bubl.module.common_utils.NoEx;
import guru.bubl.module.model.graph.GraphFactory;
import guru.bubl.module.model.graph.ShareLevel;
import guru.bubl.test.module.utils.ModelTestScenarios;
import guru.bubl.module.model.graph.subgraph.SubGraphPojo;
import guru.bubl.module.model.graph.subgraph.UserGraph;
import js_test_data.JsTestScenario;
import org.codehaus.jettison.json.JSONObject;
import guru.bubl.module.model.Image;
import guru.bubl.module.model.User;
import guru.bubl.module.model.graph.edge.EdgeOperator;
import guru.bubl.module.model.graph.vertex.VertexFactory;
import guru.bubl.module.model.graph.vertex.VertexOperator;
import guru.bubl.module.model.graph.SubGraphJson;

import javax.inject.Inject;
import java.net.URI;

public class HiddenGroupRelationsScenario implements JsTestScenario {

    /*
     * b1-r1->b2
     * b2 has hidden relations
     * b2 has an image
     * b2-shirt1->shirt1
     * b2-shirt2->shirt2
     * shirt2-color->red
     * shirt2 has an image
     * relations T-shirt are identified to Freebase T-shirt.
     */

    /*
    * Distant graph
    * bubble labeled "distant bubble" will eventually connect to b2 or b1
    */

    @Inject
    protected GraphFactory graphFactory;

    @Inject
    protected VertexFactory vertexFactory;

    @Inject
    ModelTestScenarios modelTestScenarios;

    User user = User.withEmailAndUsername("a", "b");

    private VertexOperator
            b1,
            b2,
            shirt1,
            shirt2,
            red,
            distantBubble;

    @Override
    public JSONObject build() {
        UserGraph userGraph = graphFactory.loadForUser(user);
        createVertices();
        createRelations();
        SubGraphPojo b2Graph = userGraph.aroundVertexUriInShareLevels(
                b2.uri(),
                ShareLevel.allShareLevels
        );
        SubGraphPojo b1Graph = userGraph.aroundVertexUriInShareLevels(
                b1.uri(),
                ShareLevel.allShareLevels
        );
        SubGraphPojo shirt2Graph = userGraph.aroundVertexUriInShareLevels(
                shirt2.uri(),
                ShareLevel.allShareLevels
        );
        SubGraphPojo distantBubbleGraph = userGraph.aroundVertexUriInShareLevels(
                distantBubble.uri(),
                ShareLevel.allShareLevels
        );
        EdgeOperator distantToB2 = distantBubble.addRelationToVertex(b2);
        SubGraphPojo b2GraphWhenConnectedToDistantBubble = userGraph.aroundVertexUriInShareLevels(
                b2.uri(),
                ShareLevel.allShareLevels
        );
        distantToB2.remove();
        b1.addRelationToVertex(distantBubble);
        SubGraphPojo distantBubbleGraphWhenConnectedToBubble1 = userGraph.aroundVertexUriInShareLevels(
                distantBubble.uri(),
                ShareLevel.allShareLevels
        );

        return NoEx.wrap(() ->
                new JSONObject().put(
                        "b1Graph",
                        SubGraphJson.toJson(b1Graph)
                ).put(
                        "b2Graph",
                        SubGraphJson.toJson(b2Graph)
                ).put(
                        "distantBubbleGraph",
                        SubGraphJson.toJson(distantBubbleGraph)
                ).put(
                        "b2GraphWhenConnectedToDistantBubble",
                        SubGraphJson.toJson(b2GraphWhenConnectedToDistantBubble)
                ).put(
                        "distantBubbleGraphWhenConnectedToBubble1",
                        SubGraphJson.toJson(distantBubbleGraphWhenConnectedToBubble1)
                ).put(
                        "distantBubbleUri",
                        distantBubble.uri()
                ).put(
                        "shirt2Graph",
                        SubGraphJson.toJson(shirt2Graph)
                )
                .put(
                        "shirt2BubbleUri",
                        shirt2.uri()
                )
        ).get();
    }

    private void createVertices() {
        b1 = vertexFactory.createForOwner(
                user.username()
        );
        b1.label("b1");
        b2 = vertexFactory.createForOwner(
                user.username()
        );
        b2.label("b2");
        b2.addImages(Sets.newHashSet(
                Image.withUrlForSmallAndUriForBigger(
                        "base64ForB2",
                        URI.create("http://example.org/bigImageForB2")
                )
                )
        );
        shirt1 = vertexFactory.createForOwner(
                user.username()
        );
        shirt1.label("shirt1");
        shirt2 = vertexFactory.createForOwner(
                user.username()
        );
        shirt2.label("shirt2");
        shirt2.addImages(Sets.newHashSet(
                Image.withUrlForSmallAndUriForBigger(
                        "base64ForShirt2",
                        URI.create("http://example.org/bigImageForShirt2")
                )
                )
        );
        red = vertexFactory.createForOwner(
                user.username()
        );
        red.label("red");
        distantBubble = vertexFactory.createForOwner(
                user.username()
        );
        distantBubble.label("distant bubble");
    }

    private void createRelations() {
        b1.addRelationToVertex(b2).label("r1");
        EdgeOperator shirt1Relation = b2.addRelationToVertex(shirt1);
        shirt1Relation.label("shirt1");
        shirt1Relation.addMeta(modelTestScenarios.tShirt());
        EdgeOperator shirt2Relation = b2.addRelationToVertex(shirt2);
        shirt2Relation.label("shirt2");
        shirt2Relation.addMeta(modelTestScenarios.tShirt());
        EdgeOperator color = shirt2.addRelationToVertex(red);
        color.label("color");
    }
}