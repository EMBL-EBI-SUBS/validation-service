package uk.ac.ebi.subs.validator.core.handlers;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.submittable.AssayData;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.validator.core.validators.AttributeValidator;
import uk.ac.ebi.subs.validator.core.validators.ReferenceValidator;
import uk.ac.ebi.subs.validator.core.validators.ValidatorHelper;
import uk.ac.ebi.subs.validator.data.AssayDataValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import java.util.List;

/**
 * This class responsible for handle {@link AssayData} validation.
 * <p>
 * An AssayData refers to an Assay via {@link uk.ac.ebi.subs.data.component.AssayRef AssayRef} and to
 * a Sample via {@link uk.ac.ebi.subs.data.component.SampleRef SampleRef}.
 */
@Service
@RequiredArgsConstructor
public class AssayDataHandler extends AbstractHandler<AssayDataValidationMessageEnvelope> {

    @NonNull
    private ReferenceValidator refValidator;
    @NonNull
    @Getter
    private AttributeValidator attributeValidator;

    @NonNull
    private DataTypeRepository dataTypeRepository;

    @Override
    List<SingleValidationResult> validateSubmittable(AssayDataValidationMessageEnvelope envelope) {
        AssayData assayData = envelope.getEntityToValidate();

        DataType dataType = dataTypeRepository.findOne(envelope.getDataTypeId());


        return refValidator.validate(
                assayData,
                dataType,
                assayData.getAssayRefs(),
                envelope.getAssays()
        );
    }

}
