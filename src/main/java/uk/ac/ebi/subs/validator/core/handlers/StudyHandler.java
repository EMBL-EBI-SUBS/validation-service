package uk.ac.ebi.subs.validator.core.handlers;

import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.validator.core.validators.AttributeValidator;
import uk.ac.ebi.subs.validator.core.validators.ReferenceValidator;
import uk.ac.ebi.subs.validator.core.validators.StudyTypeValidator;
import uk.ac.ebi.subs.validator.core.validators.ValidatorHelper;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.StudyValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.ValidationMessageEnvelope;

import java.util.Arrays;
import java.util.List;

/**
 * This class responsible for handle {@link Study} validation.
 * <p>
 * A Study refers to no other object.
 * A study must have a studyType.
 */
@Service
public class StudyHandler extends AbstractHandler<StudyValidationMessageEnvelope> {

    private StudyTypeValidator studyTypeValidator;
    private AttributeValidator attributeValidator;
    private ReferenceValidator referenceValidator;

    public StudyHandler(StudyTypeValidator studyTypeValidator, AttributeValidator attributeValidator, ReferenceValidator referenceValidator) {
        this.studyTypeValidator = studyTypeValidator;
        this.attributeValidator = attributeValidator;
        this.referenceValidator = referenceValidator;

    }

    @Override
    public List<SingleValidationResult> validateSubmittable(StudyValidationMessageEnvelope envelope) {
        Study study = envelope.getEntityToValidate();

        List<SingleValidationResult> results = Arrays.asList(
                referenceValidator.validate(study.getId(), study.getProjectRef(), envelope.getProject()),
                studyTypeValidator.validate(study)
        );

        return results;
    }

    @Override
    List<SingleValidationResult> validateAttributes(StudyValidationMessageEnvelope envelope) {
        Study study = envelope.getEntityToValidate();
        return ValidatorHelper.validateAttribute(study.getAttributes(), study.getId(), attributeValidator);
    }

}
