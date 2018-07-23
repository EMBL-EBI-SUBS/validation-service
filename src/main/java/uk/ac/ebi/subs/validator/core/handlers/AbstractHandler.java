package uk.ac.ebi.subs.validator.core.handlers;

import uk.ac.ebi.subs.validator.core.validators.ValidatorHelper;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.ValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractHandler<T extends ValidationMessageEnvelope> {

    abstract List<SingleValidationResult> validateSubmittable(T envelope);

    abstract List<SingleValidationResult> validateAttributes(T envelope);

    public SingleValidationResultsEnvelope handleValidationRequest(T envelope) {
        List<SingleValidationResult> resultList = new ArrayList<>();

        resultList.addAll(validateSubmittable(envelope));
        resultList.addAll(validateAttributes(envelope));

        List<SingleValidationResult> interestingResults = resultList.stream()
                .filter(AbstractHandler::statusIsNotPassOrPending)
                .collect(Collectors.toList());

        if (interestingResults.isEmpty()) {
            SingleValidationResult r = ValidatorHelper.getDefaultSingleValidationResult(
                    envelope.getEntityToValidate().getId(), ValidationAuthor.Core);
            interestingResults = Arrays.asList(r);
        }

        return generateSingleValidationResultsEnvelope(envelope, interestingResults );
    }

    private static boolean statusIsNotPassOrPending(SingleValidationResult r) {
        return !(r.getValidationStatus().equals(SingleValidationResultStatus.Pass)
                || r.getValidationStatus().equals(SingleValidationResultStatus.Pending));
    }


    SingleValidationResultsEnvelope generateSingleValidationResultsEnvelope(ValidationMessageEnvelope envelope, List<SingleValidationResult> singleValidationResults) {
        return new SingleValidationResultsEnvelope(
                singleValidationResults,
                envelope.getValidationResultVersion(),
                envelope.getValidationResultUUID(),
                ValidationAuthor.Core
        );
    }

}
