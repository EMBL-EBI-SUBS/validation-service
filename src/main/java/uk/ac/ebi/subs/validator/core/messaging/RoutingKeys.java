package uk.ac.ebi.subs.validator.core.messaging;

/**
 * This class holds the validation routing keys for the core-validator.
 *
 */
public class RoutingKeys {

    public static final String EVENT_VALIDATION_SUCCESS = "validation.success";

    public static final String EVENT_VALIDATION_ERROR = "validation.error";

    // Core Validator specific events
    public static final String EVENT_CORE_ASSAY_VALIDATION = "core.assay.validation";

    public static final String EVENT_CORE_ASSAYDATA_VALIDATION = "core.assaydata.validation";

    public static final String EVENT_CORE_SAMPLE_VALIDATION = "core.sample.validation";

    public static final String EVENT_CORE_STUDY_VALIDATION = "core.study.validation";
}
