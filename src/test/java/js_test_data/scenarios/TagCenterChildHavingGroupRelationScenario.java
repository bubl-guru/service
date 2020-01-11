/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package js_test_data.scenarios;

import com.google.inject.Inject;
import guru.bubl.module.common_utils.NoEx;
import guru.bubl.module.model.graph.ShareLevel;
import guru.bubl.module.model.graph.SubGraphJson;
import guru.bubl.module.model.graph.edge.EdgeOperator;
import guru.bubl.module.model.graph.tag.TagPojo;
import guru.bubl.module.model.graph.subgraph.SubGraphPojo;
import guru.bubl.module.model.test.scenarios.TestScenarios;
import guru.bubl.test.module.utils.ModelTestScenarios;
import js_test_data.AbstractScenario;
import js_test_data.JsTestScenario;
import org.codehaus.jettison.json.JSONObject;

public class TagCenterChildHavingGroupRelationScenario extends AbstractScenario implements JsTestScenario {

    /*
     * center - r1 -> b1
     * r1 - meta -> human
     * b1 - r2 -> b2
     * b1 - r3 -> b3
     * r3 - meta -> r2
     */

    @Inject
    ModelTestScenarios modelTestScenarios;

    TagPojo r2AsMeta;

    @Override
    public Object build() {
        createUserGraph();
        createVertices();
        createEdges();
        /*
        I have to write a depth of 2 but a depth of one should be enough, I don't understand
         */
        SubGraphPojo metaCenter = userGraph.aroundVertexUriInShareLevelsWithDepth(
                r2AsMeta.uri(),
                ShareLevel.allShareLevels,
                2
        );
        SubGraphPojo childOfB1 = userGraph.aroundVertexUriInShareLevels(
                b1.uri(),
                ShareLevel.allShareLevels
        );
        return NoEx.wrap(() -> new JSONObject()
                .put(
                        "metaCenter",
                        SubGraphJson.toJson(metaCenter)
                )
                .put(
                        "childOfB1",
                        SubGraphJson.toJson(childOfB1)
                )
        ).get();
    }

    public void createEdges() {
        EdgeOperator r1 = center.addRelationToVertex(b1);
        r1.label("r1");
        r1.addMeta(
                modelTestScenarios.human()
        );
        EdgeOperator r2 = b1.addRelationToVertex(b2);
        r2.label("r2");
        EdgeOperator r3 = b1.addRelationToVertex(b3);
        r3.label("r3");
        r2AsMeta = TestScenarios.identificationFromFriendlyResource(
                r2
        );
        r3.addMeta(
                r2AsMeta
        );
    }

}