package uk.ac.ebi.subs.validator.core.handlers;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.validator.core.validators.AttributeValidator;
import uk.ac.ebi.subs.validator.core.validators.ReferenceValidator;
import uk.ac.ebi.subs.validator.core.validators.ValidatorHelper;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.StudyValidationMessageEnvelope;

import java.util.ArrayList;
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
    @Getter
    private AttributeValidator attributeValidator;
    @NonNull
    private ReferenceValidator referenceValidator;

    @NonNull
    private DataTypeRepository dataTypeRepository;



    @Override
    public List<SingleValidationResult> validateSubmittable(StudyValidationMessageEnvelope envelope) {
        Study study = envelope.getEntityToValidate();

        DataType dataType = dataTypeRepository.findById(envelope.getDataTypeId()).orElse(null);

        List<SingleValidationResult> results = new ArrayList<>();

        results.addAll(
                referenceValidator.validate(study, dataType, study.getProjectRef(), envelope.getProject())
        );

        return results;
    }


}
