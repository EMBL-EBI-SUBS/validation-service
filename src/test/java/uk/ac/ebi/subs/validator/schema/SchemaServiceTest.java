package uk.ac.ebi.subs.validator.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.subs.data.submittable.Analysis;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.AssayData;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.validator.schema.custom.JsonAsTextPlainHttpMessageConverter;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SchemaServiceTest {
    private String schemaRootUrl = "https://raw.githubusercontent.com/EMBL-EBI-SUBS/validation-schemas/master";

    private String studySchemaUrl = schemaRootUrl + "/study/study-schema.json";
    private String mlStudySchemaUrl = schemaRootUrl + "/study/ml-study-schema.json";
    private String sampleSchemaUrl = schemaRootUrl + "/sample/sample-schema.json";
    private String mlSampleSchemaUrl = schemaRootUrl + "/sample/ml-sample-schema.json";
    private String assaySchemaUrl = schemaRootUrl + "/assay/assay-schema.json";
    private String mlAssaySchemaUrl = schemaRootUrl + "/assay/ml-assay-schema.json";
    private String assayDataSchemaUrl = schemaRootUrl + "/assaydata/assaydata-schema.json";
    private String evaSeqVarSchemaUrl = schemaRootUrl + "/analysis/eva-sequence-variation-schema.json";

    private RestTemplate restTemplate = new RestTemplate();
    private SchemaService schemaService;

    @Before
    public void setUp() {
        schemaService = new SchemaService(restTemplate);
    }

    @Test
    public void getSchemaForSample() {
        JsonNode sampleSchema = schemaService.getSchemaFor(Sample.class.getTypeName(), sampleSchemaUrl);
        assertThat(sampleSchema.get("title").asText(), is("Submissions Sample Schema"));
    }

    @Test
    public void getSchemaForMLSample() {
        JsonNode sampleSchema = schemaService.getSchemaFor(Sample.class.getTypeName(), mlSampleSchemaUrl);
        assertThat(sampleSchema.get("description").asText(), is("Schema based on rules found in http://isa-specs.readthedocs.io/en/latest/isajson.html#sample-schema-json"));
    }

    @Test
    public void getSchemaForStudy() {
        JsonNode studySchema = schemaService.getSchemaFor(Study.class.getTypeName(), studySchemaUrl);
        assertThat(studySchema.get("title").asText(), is("Submissions Study Schema"));
    }

    @Test
    public void getSchemaForMLStudy() {
        JsonNode studySchema = schemaService.getSchemaFor(Study.class.getTypeName(), mlStudySchemaUrl);
        assertThat(studySchema.get("author").asText(), is("metabolights"));
    }

    @Test
    public void getSchemaForAssay() {
        JsonNode assaySchema = schemaService.getSchemaFor(Assay.class.getTypeName(), assaySchemaUrl);
        assertThat(assaySchema.get("title").asText(), is("Submissions Assay Schema"));
    }

    @Test
    public void getSchemaForMLAssay() {
        JsonNode assaySchema = schemaService.getSchemaFor(Assay.class.getTypeName(), mlAssaySchemaUrl);
        assertThat(assaySchema.get("description").asText(), is("Schema based on rules found in http://isa-specs.readthedocs.io/en/latest/isajson.html#assay-schema-json"));
    }

    @Test
    public void getSchemaForAssayData() {
        JsonNode assayDataSchema = schemaService.getSchemaFor(AssayData.class.getTypeName(), assayDataSchemaUrl);
        assertThat(assayDataSchema.get("title").asText(), is("Submissions Assay Data Schema"));
    }

    @Test
    public void getSchemaForEvaSeqVarAnalysis() {
        JsonNode analysisDataSchema = schemaService.getSchemaFor(Analysis.class.getTypeName(), evaSeqVarSchemaUrl);
        assertThat(analysisDataSchema.get("title").asText(), is("EVA Sequence variation checklist"));
    }
}
