/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package js_test_data.scenarios;

import guru.bubl.module.model.User;
import guru.bubl.module.model.graph.FriendlyResourcePojo;
import guru.bubl.module.model.graph.GraphFactory;
import guru.bubl.module.model.graph.ShareLevel;
import guru.bubl.module.model.graph.SubGraphJson;
import guru.bubl.module.model.graph.relation.RelationOperator;
import guru.bubl.module.model.graph.tag.TagPojo;
import guru.bubl.module.model.graph.subgraph.UserGraph;
import guru.bubl.module.model.graph.vertex.VertexFactory;
import guru.bubl.module.model.graph.vertex.VertexOperator;
import guru.bubl.test.module.utils.ModelTestScenarios;
import js_test_data.JsTestScenario;

import javax.inject.Inject;
import java.net.URI;

public class ThreeLevelDeepGroupRelationScenario implements JsTestScenario {
    /*
     fast charging station -region-> {
        -sub-region-a->{
            -r1->b1
            -r2->b2
        }
        -sub-region-b->{
            -r3->b3
            -r4->b4
        }
     }
    */

    private TagPojo region = new TagPojo(
            URI.create(
                    "https://mindrespect.com/e6452d32-8015-4d8e-89ad-58f14699680d"
            ),
            new FriendlyResourcePojo(
                    "region"
            )
    );
    private TagPojo subRegion = new TagPojo(
            URI.create(
                    "https://mindrespect.com/077e5cd5-0adf-471a-8145-228107cf66e5"
            ),
            new FriendlyResourcePojo(
                    "sub-region"
            )
    );
    private TagPojo subRegionA = new TagPojo(
            URI.create(
                    "https://mindrespect.com/f893f25b-7cf1-4fee-860a-18b0764949d3"
            ),
            new FriendlyResourcePojo(
                    "sub-region-a"
            )
    );

    private TagPojo subRegionB = new TagPojo(
            URI.create(
                    "https://mindrespect.com/fc5379ff-fdc8-4b9d-ab40-d9adcac83315"
            ),
            new FriendlyResourcePojo(
                    "sub-region-b"
            )
    );

    private VertexOperator
            fastChargingStation,
            b1,
            b2,
            b3,
            b4;

    private RelationOperator
            r1,
            r2,
            r3,
            r4;

    @Inject
    GraphFactory graphFactory;

    @Inject
    VertexFactory vertexFactory;

    @Inject
    ModelTestScenarios modelTestScenarios;

    User user = User.withEmailAndUsername(
            "a",
            "églantier"
    );

    @Override
    public Object build() {
        UserGraph userGraph = graphFactory.loadForUser(user);
        createVertices();
        createEdges();
        return SubGraphJson.toJson(
                userGraph.aroundVertexUriInShareLevels(
                        fastChargingStation.uri(),
                        ShareLevel.allShareLevelsInt
                )
        );
    }

    private void createVertices() {
        fastChargingStation = vertexFactory.createForOwner(
                user.username()
        );
        fastChargingStation.label("fast charging station");
        b1 = vertexFactory.createForOwner(
                user.username()
        );
        b1.label("b1");
        b2 = vertexFactory.createForOwner(
                user.username()
        );
        b2.label("b2");
        b3 = vertexFactory.createForOwner(
                user.username()
        );
        b3.label("b3");
        b4 = vertexFactory.createForOwner(
                user.username()
        );
        b4.label("b4");
    }

    private void createEdges() {
        r1 = fastChargingStation.addRelationToVertex(b1);
        r1.label("r1");
        r1.addTag(region);
        r1.addTag(subRegion);
        r1.addTag(subRegionA);

        r2 = fastChargingStation.addRelationToVertex(b2);
        r2.label("r2");
        r2.addTag(region);
        r2.addTag(subRegion);
        r2.addTag(subRegionA);

        r3 = fastChargingStation.addRelationToVertex(b3);
        r3.label("r3");
        r3.addTag(region);
        r3.addTag(subRegion);
        r3.addTag(subRegionB);

        r4 = fastChargingStation.addRelationToVertex(b4);
        r4.label("r4");
        r4.addTag(region);
        r4.addTag(subRegion);
        r4.addTag(subRegionB);
    }
}
