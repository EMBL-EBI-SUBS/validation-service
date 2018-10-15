package uk.ac.ebi.subs.validator.core.handlers;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.validator.core.validators.AttributeValidator;
import uk.ac.ebi.subs.validator.data.SampleGroupValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SampleGroupHandler extends AbstractHandler<SampleGroupValidationMessageEnvelope> {

    @NonNull
    @Getter
    private AttributeValidator attributeValidator;

    @Override
    public List<SingleValidationResult> validateSubmittable(SampleGroupValidationMessageEnvelope envelope) {
        //TODO - verify samples exist
        return Collections.emptyList();
    }
}
