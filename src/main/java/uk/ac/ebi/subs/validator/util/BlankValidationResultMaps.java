package uk.ac.ebi.subs.validator.util;

import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rolando on 19/06/2017.
 * Modified by karoly on 05/07/2017.
 */
public class BlankValidationResultMaps {

    private static final List<ValidationAuthor> SAMPLE_VALIDATION_SERVICES_REQUIRED =
            Arrays.asList(ValidationAuthor.Biosamples, ValidationAuthor.Ena, ValidationAuthor.Core, ValidationAuthor.Taxonomy, ValidationAuthor.JsonSchema);
    private static final List<ValidationAuthor> STUDY_VALIDATION_SERVICES_REQUIRED =
            Arrays.asList(ValidationAuthor.Core, ValidationAuthor.Ena, ValidationAuthor.JsonSchema);
    private static final List<ValidationAuthor> ASSAY_VALIDATION_SERVICES_REQUIRED =
            Arrays.asList(ValidationAuthor.Core, ValidationAuthor.Ena, ValidationAuthor.JsonSchema);
    private static final List<ValidationAuthor> ASSAY_DATA_VALIDATION_SERVICES_REQUIRED =
            Arrays.asList(ValidationAuthor.Core, ValidationAuthor.Ena, ValidationAuthor.JsonSchema);
    private static final List<ValidationAuthor> ANALYSIS_VALIDATION_SERVICES_REQUIRED =
            Arrays.asList(ValidationAuthor.Core, ValidationAuthor.Ena, ValidationAuthor.JsonSchema);
    private static final List<ValidationAuthor> FILE_REF_VALIDATION_SERVICES_REQUIRED =
            Arrays.asList(ValidationAuthor.FileReference, ValidationAuthor.FileContent);

    private static final List<ValidationAuthor> PROJECT_VALIDATION_SERVICES_REQUIRED = Arrays.asList(ValidationAuthor.BioStudies);

    /**
     * Generates an initial default or "blank" map to hold results of validation outcomes required for each submittable.
     *
     * @return an initialised mapping from the required Validation service(ValidationAuthor) to it's respected validation result(boolean, false by default)
     */
    public static Map<ValidationAuthor, List<SingleValidationResult>> forSample() {
        return generateDefaultMap(SAMPLE_VALIDATION_SERVICES_REQUIRED);
    }

    public static Map<ValidationAuthor, List<SingleValidationResult>> forProject() {
        return generateDefaultMap(PROJECT_VALIDATION_SERVICES_REQUIRED);
    }

    public static Map<ValidationAuthor, List<SingleValidationResult>> forStudy() {
        return generateDefaultMap(STUDY_VALIDATION_SERVICES_REQUIRED);
    }

    public static Map<ValidationAuthor, List<SingleValidationResult>> forAssay() {
        return generateDefaultMap(ASSAY_VALIDATION_SERVICES_REQUIRED);
    }

    public static Map<ValidationAuthor, List<SingleValidationResult>> forAssayData() {
        return generateDefaultMap(ASSAY_DATA_VALIDATION_SERVICES_REQUIRED);
    }

    public static Map<ValidationAuthor, List<SingleValidationResult>> forAnalysis() {
        return generateDefaultMap(ANALYSIS_VALIDATION_SERVICES_REQUIRED);
    }

    public static Map<ValidationAuthor, List<SingleValidationResult>> forFile() {
        return generateDefaultMap(FILE_REF_VALIDATION_SERVICES_REQUIRED);
    }

    private static Map<ValidationAuthor, List<SingleValidationResult>> generateDefaultMap(List<ValidationAuthor> validationAuthors) {
        Map<ValidationAuthor, List<SingleValidationResult>> blankValidationResultMap = new HashMap<>();

        for(ValidationAuthor author: validationAuthors) {
            blankValidationResultMap.put(author, new ArrayList<>());
        }

        return blankValidationResultMap;
    }
}
