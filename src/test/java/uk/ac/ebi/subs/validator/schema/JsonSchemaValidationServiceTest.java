package uk.ac.ebi.subs.validator.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.validator.schema.model.JsonSchemaValidationError;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.ac.ebi.subs.validator.TestUtils.createStaticSample;
import static uk.ac.ebi.subs.validator.schema.custom.SchemaObjectMapperProvider.createCustomObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JsonSchemaValidationServiceTest {

    @Autowired
    private JsonSchemaValidationService jsonSchemaValidationService;

    private ObjectMapper mapper;

    private JsonNode sampleSchema;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        mapper = createCustomObjectMapper();
        sampleSchema = loadSampleSchema();
    }


    @Test
    public void errorList_ShouldBe_Empty() throws IOException {
        List<JsonSchemaValidationError> errorList = jsonSchemaValidationService.validate(mapper.readTree("{}"), mapper.readTree("{}"));
        assertThat(errorList, empty());
    }

    @Test
    public void errorList_ShouldHave_OneError() throws IOException {
        List<JsonSchemaValidationError> errorList = jsonSchemaValidationService.validate(mapper.readTree("{\"required\": [ \"alias\" ]}"), mapper.valueToTree(new Sample()));
        assertThat(errorList, hasSize(1));
    }

    @Test
    public void errorList_ShouldHave_ErrorOnMissingAlias() throws IOException {
        List<JsonSchemaValidationError> errorList = jsonSchemaValidationService.validate(mapper.readTree("{\"required\": [ \"alias\" ]}"), mapper.valueToTree(new Sample()));
        assertThat(errorList.get(0).getDataPath(), is(".alias"));
    }

    @Test
    public void errorList_ShouldHave_OneErrorMessageOnMissingAlias() throws IOException {
        List<JsonSchemaValidationError> errorList = jsonSchemaValidationService.validate(mapper.readTree("{\"required\": [ \"alias\" ]}"), mapper.valueToTree(new Sample()));
        assertThat(errorList.get(0).getErrors(), hasSize(1));
    }

    @Test
    public void errorList_ErrorMessageOnMissingAlias_ShouldBe() throws IOException {
        List<JsonSchemaValidationError> errorList = jsonSchemaValidationService.validate(mapper.readTree("{\"required\": [ \"alias\" ]}"), mapper.valueToTree(new Sample()));
        assertThat(errorList.get(0).getErrors().get(0), is("should have required property 'alias'"));
    }

    @Test
    public void errorList_shouldHave_ThreeErrors() throws IOException {

        List<JsonSchemaValidationError> errorList = jsonSchemaValidationService.validate(sampleSchema, mapper.readTree("{}"));
        assertThat(errorList, hasSize(3));
    }

    @Test
    public void emptySample_hasThreeErrors() {

        List<JsonSchemaValidationError> errorList = jsonSchemaValidationService.validate(sampleSchema, mapper.valueToTree(new Sample()));
        assertThat(errorList, hasSize(3));
    }

    @Test
    public void sample_mustHaveReleaseDate() {

        List<JsonSchemaValidationError> errorList = jsonSchemaValidationService.validate(sampleSchema, mapper.valueToTree(createStaticSample()));
        assertThat(errorList, hasSize(0));
    }

    private JsonNode loadSampleSchema() throws URISyntaxException, IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource("sample.schema.json").getFile());
        JsonNode node = new ObjectMapper().readTree(file);
        return node;
    }

    private List<String> getResourceFiles( String path ) throws IOException {
        List<String> filenames = new ArrayList<>();

        try(
                InputStream in = getResourceAsStream( path );
                BufferedReader br = new BufferedReader( new InputStreamReader( in ) ) ) {
            String resource;

            while( (resource = br.readLine()) != null ) {
                filenames.add( resource );
            }
        }

        return filenames;
    }

    private InputStream getResourceAsStream( String resource ) {
        final InputStream in
                = getContextClassLoader().getResourceAsStream( resource );

        return in == null ? getClass().getResourceAsStream( resource ) : in;
    }

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
