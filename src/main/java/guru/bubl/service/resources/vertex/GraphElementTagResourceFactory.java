/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.service.resources.vertex;

import guru.bubl.module.model.graph.graph_element.GraphElementOperator;
import guru.bubl.service.resources.GraphElementTagResource;

public interface GraphElementTagResourceFactory {
    GraphElementTagResource forGraphElement(
            GraphElementOperator graphElement
    );
}
