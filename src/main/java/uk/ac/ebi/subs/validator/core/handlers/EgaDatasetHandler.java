package uk.ac.ebi.subs.validator.core.handlers;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.validator.core.validators.AttributeValidator;
import uk.ac.ebi.subs.validator.data.EgaDatasetValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import java.util.Collections;
import java.util.List;

@Service
public class EgaDatasetHandler extends AbstractHandler<EgaDatasetValidationMessageEnvelope> {

    @NonNull
    @Getter
    private AttributeValidator attributeValidator;

    public EgaDatasetHandler(@NonNull AttributeValidator attributeValidator, DataTypeRepository dataTypeRepository) {
        super(dataTypeRepository);
        this.attributeValidator = attributeValidator;
    }

    @Override
    public List<SingleValidationResult> validateSubmittable(EgaDatasetValidationMessageEnvelope envelope) {
        //TODO - verify analysis, assay and DAC policy exists
        return Collections.emptyList();
    }
}
