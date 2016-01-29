/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.service.resources;

import guru.bubl.service.resources.vertex.GraphElementIdentificationResourceTest;
import guru.bubl.service.resources.vertex.VertexOwnedSurroundGraphResouceTest;
import guru.bubl.service.resources.vertex.VertexSuggestionResourceTest;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
@Ignore
@RunWith(Suite.class)
@Suite.SuiteClasses({
        UserResourceTest.class
})
public class ServiceSpecificClassTest extends ServiceTestRunner {
}
