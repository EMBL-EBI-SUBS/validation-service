package uk.ac.ebi.subs.validator.core.handlers;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.validator.core.validators.AttributeValidator;
import uk.ac.ebi.subs.validator.core.validators.ReferenceValidator;
import uk.ac.ebi.subs.validator.data.SampleValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import java.util.List;

/**
 * This class responsible for handle {@link Sample} validation.
 *
 * A sample may refer to other samples or itself
 * using {@link  uk.ac.ebi.subs.data.component.SampleRelationship SampleRelationship}
 */
@Service
public class SampleHandler extends AbstractHandler<SampleValidationMessageEnvelope> {

    @NonNull private ReferenceValidator referenceValidator;

    @NonNull @Getter
    private AttributeValidator attributeValidator;

    public SampleHandler(@NonNull ReferenceValidator referenceValidator, @NonNull AttributeValidator attributeValidator,
                         DataTypeRepository dataTypeRepository) {
        super(dataTypeRepository);
        this.referenceValidator = referenceValidator;
        this.attributeValidator = attributeValidator;
    }

    @Override
    List<SingleValidationResult> validateSubmittable(SampleValidationMessageEnvelope envelope) {
        Sample sample = envelope.getEntityToValidate();

        DataType dataType = getDataTypeFromRepository(envelope.getDataTypeId());

        return referenceValidator.validate(
                sample,
                dataType,
                sample.getSampleRelationships(),
                envelope.getSampleList()
        );
    }
}
