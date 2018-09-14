package uk.ac.ebi.subs.validator.messaging;

import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

/**
 * This class defines the validation routing keys for the schema validator.
 */
public class SchemaRoutingKeys {

    public static final String EVENT_SCHEMA_SAMPLE_VALIDATION = "jsonschema.sample.validation";

    public static final String EVENT_SCHEMA_STUDY_VALIDATION = "jsonschema.study.validation";

    public static final String EVENT_SCHEMA_ASSAY_VALIDATION = "jsonschema.assay.validation";

    public static final String EVENT_SCHEMA_ASSAYDATA_VALIDATION = "jsonschema.assaydata.validation";

    public static final String EVENT_SCHEMA_ANALYSIS_VALIDATION = "jsonschema.analysis.validation";

}
