package uk.ac.ebi.subs.validator.core.handlers;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.AbstractSubsRef;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.validator.core.validators.AttributeValidator;
import uk.ac.ebi.subs.validator.core.validators.ReferenceValidator;
import uk.ac.ebi.subs.validator.core.validators.ValidatorHelper;
import uk.ac.ebi.subs.validator.data.SampleValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class responsible for handle {@link Sample} validation.
 *
 * A sample may refer to other samples or itself
 * using {@link  uk.ac.ebi.subs.data.component.SampleRelationship SampleRelationship}
 */
@Service
@RequiredArgsConstructor
public class SampleHandler extends AbstractHandler<SampleValidationMessageEnvelope> {

    @NonNull private ReferenceValidator referenceValidator;

    @NonNull @Getter
    private AttributeValidator attributeValidator;

    @NonNull
    private DataTypeRepository dataTypeRepository;

    @Override
    List<SingleValidationResult> validateSubmittable(SampleValidationMessageEnvelope envelope) {
        Sample sample = envelope.getEntityToValidate();

        DataType dataType = dataTypeRepository.findById(envelope.getDataTypeId()).orElse(null);

        List<SingleValidationResult> results = referenceValidator.validate(
                sample,
                dataType,
                sample.getSampleRelationships(),
                envelope.getSampleList()
        );

        return results;
    }

}
