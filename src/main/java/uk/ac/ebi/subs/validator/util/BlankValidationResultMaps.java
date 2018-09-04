package uk.ac.ebi.subs.validator.util;

import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rolando on 19/06/2017.
 * Modified by karoly on 05/07/2017.
 */
public class BlankValidationResultMaps {

    private static final List<ValidationAuthor> FILE_REF_VALIDATION_SERVICES_REQUIRED =
            Arrays.asList(ValidationAuthor.FileReference, ValidationAuthor.FileContent);

    public static Map<ValidationAuthor, List<SingleValidationResult>> forFile() {
        return generateDefaultMap(FILE_REF_VALIDATION_SERVICES_REQUIRED);
    }

    public static Map<ValidationAuthor, List<SingleValidationResult>> generateDefaultMap(Collection<ValidationAuthor> validationAuthors) {
        Map<ValidationAuthor, List<SingleValidationResult>> blankValidationResultMap = new HashMap<>();

        for(ValidationAuthor author: validationAuthors) {
            blankValidationResultMap.put(author, new ArrayList<>());
        }

        return blankValidationResultMap;
    }
}
