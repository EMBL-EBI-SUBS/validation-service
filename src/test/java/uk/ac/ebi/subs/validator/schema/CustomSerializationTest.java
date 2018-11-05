package uk.ac.ebi.subs.validator.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.subs.validator.TestUtils.createStaticSample;
import static uk.ac.ebi.subs.validator.schema.custom.SchemaObjectMapperProvider.createCustomObjectMapper;

public class CustomSerializationTest {

    private static ObjectMapper mapper;

    @BeforeClass
    public static void setUp() {
        mapper = createCustomObjectMapper();
    }

    @Test
    public void localDateSerializationTest() {
        assertEquals(TextNode.valueOf("2018-10-24"), mapper.valueToTree(LocalDate.of(2018,10,24)));
    }

    @Test
    public void localDateWithLeadingZerosSerializationTest() {
        assertEquals(TextNode.valueOf("2018-01-01"), mapper.valueToTree(LocalDate.of(2018,1,1)));
    }

    @Test
    public void localDateYearWithLeadingZerosSerializationTest() {
        assertEquals(TextNode.valueOf("0008-11-01"), mapper.valueToTree(LocalDate.of(8,11,1)));
    }

    @Test
    public void sampleSerializationTest() {
        assertEquals(
                "{\"id\":\"test-sample-id\",\"accession\":\"ABC12345\",\"alias\":\"test-alias\",\"team\":" +
                        "{\"name\":\"testTeam\"},\"description\":\"Description for a test sample.\",\"taxonId\":9606," +
                        "\"taxon\":\"test-taxonomy\",\"releaseDate\":\"2018-01-01\"}",
                mapper.valueToTree(createStaticSample()).toString());
    }
}
