package uk.ac.ebi.subs.validator.messaging;

public class CoordinatorRoutingKeys {

    public static final String EVENT_FILE_CREATED = "usi.file.created";
    public static final String EVENT_FILE_REF_VALIDATION = "file.reference.validation";
    public static final String EVENT_FILE_DELETED = "file.deleted.validation";

    private static final String SUBMITTABLE_CREATED = "usi.submittable.created";
    private static final String SUBMITTABLE_UPDATED = "usi.submittable.updated";

    static final String SUBMITTABLE_SAMPLE_CREATED = SUBMITTABLE_CREATED + ".sample";
    static final String SUBMITTABLE_SAMPLE_UPDATED = SUBMITTABLE_UPDATED + ".sample";

    static final String SUBMITTABLE_STUDY_CREATED = SUBMITTABLE_CREATED + ".study";
    static final String SUBMITTABLE_STUDY_UPDATED = SUBMITTABLE_UPDATED + ".study";

    static final String SUBMITTABLE_ASSAY_CREATED = SUBMITTABLE_CREATED + ".assay";
    static final String SUBMITTABLE_ASSAY_UPDATED = SUBMITTABLE_UPDATED + ".assay";

    static final String SUBMITTABLE_ASSAYDATA_CREATED = SUBMITTABLE_CREATED + ".assaydata";
    static final String SUBMITTABLE_ASSAYDATA_UPDATED = SUBMITTABLE_UPDATED + ".assaydata";

    static final String SUBMITTABLE_PROJECT_CREATED = SUBMITTABLE_CREATED + ".project";
    static final String SUBMITTABLE_PROJECT_UPDATED = SUBMITTABLE_UPDATED + ".project";

    static final String SUBMITTABLE_ANALYSIS_CREATED = SUBMITTABLE_CREATED + ".analysis";
    static final String SUBMITTABLE_ANALYSIS_UPDATED = SUBMITTABLE_UPDATED + ".analysis";

    static final String EVENT_SUBMITTABLE_DELETED = "usi.submittable.deletion";
}
