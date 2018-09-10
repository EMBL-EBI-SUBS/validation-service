package uk.ac.ebi.subs.validator.core.validators;

import uk.ac.ebi.subs.data.component.Attribute;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ValidatorHelper {

    public static SingleValidationResult singleValidationResult(String id, ValidationAuthor validationAuthor, SingleValidationResultStatus status) {
        SingleValidationResult singleValidationResult = new SingleValidationResult(validationAuthor, id);
        singleValidationResult.setValidationStatus(status);
        return singleValidationResult;
    }

    public static SingleValidationResult getDefaultSingleValidationResult(String id, ValidationAuthor validationAuthor) {
        return singleValidationResult(id,validationAuthor,SingleValidationResultStatus.Pass);
    }

    public static List<SingleValidationResult> validateAttribute(Map<String, Collection<Attribute>> attributes,
                                                                 String submittableId,
                                                                 AttributeValidator attributeValidator) {
        List<SingleValidationResult> validationResults = new ArrayList<>();

        for (Map.Entry<String, Collection<Attribute>> entry: attributes.entrySet()) {
            validationResults.addAll(attributeValidator.validate(entry.getKey(), entry.getValue(), submittableId));
        }

        return validationResults;
    }
}
