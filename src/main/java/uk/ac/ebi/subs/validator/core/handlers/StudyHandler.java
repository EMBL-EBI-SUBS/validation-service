package uk.ac.ebi.subs.validator.core.handlers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.validator.core.validators.AttributeValidator;
import uk.ac.ebi.subs.validator.core.validators.ReferenceValidator;
import uk.ac.ebi.subs.validator.core.validators.StudyTypeValidator;
import uk.ac.ebi.subs.validator.core.validators.ValidatorHelper;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.StudyValidationMessageEnvelope;

import java.util.Arrays;
import java.util.List;

/**
 * This class responsible for handle {@link Study} validation.
 * <p>
 * A Study refers to no other object.
 * A study must have a studyType.
 */
@Service
@RequiredArgsConstructor
public class StudyHandler extends AbstractHandler<StudyValidationMessageEnvelope> {

    @NonNull
    private StudyTypeValidator studyTypeValidator;
    @NonNull
    private AttributeValidator attributeValidator;
    @NonNull
    private ReferenceValidator referenceValidator;

    @NonNull
    private DataTypeRepository dataTypeRepository;



    @Override
    public List<SingleValidationResult> validateSubmittable(StudyValidationMessageEnvelope envelope) {
        Study study = envelope.getEntityToValidate();

        DataType dataType = dataTypeRepository.findOne(envelope.getDataTypeId());

        List<SingleValidationResult> results =
                referenceValidator.validate(study, dataType, study.getProjectRef(), envelope.getProject());

        results.add(
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
