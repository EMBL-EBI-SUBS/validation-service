package uk.ac.ebi.subs.validator.core.handlers;

import uk.ac.ebi.subs.data.submittable.BaseSubmittable;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.validator.core.validators.AttributeValidator;
import uk.ac.ebi.subs.validator.core.validators.ValidatorHelper;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.ValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.error.EntityNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractHandler<T extends ValidationMessageEnvelope<?>> {

    private DataTypeRepository dataTypeRepository;

    public AbstractHandler(DataTypeRepository dataTypeRepository) {
        this.dataTypeRepository = dataTypeRepository;
    }

    abstract List<SingleValidationResult> validateSubmittable(T envelope);

    abstract AttributeValidator getAttributeValidator();

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
            interestingResults = Collections.singletonList(r);
        }

        return generateSingleValidationResultsEnvelope(envelope, interestingResults );
    }

    DataType getDataTypeFromRepository(String dataTypeId) {
        return dataTypeRepository
                .findById(dataTypeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("DataType entity with ID: %s is not found in the database.", dataTypeId)));
    }

    List<SingleValidationResult> validateAttributes(T envelope) {
        BaseSubmittable submittable = envelope.getEntityToValidate();
        return ValidatorHelper.validateAttribute(submittable.getAttributes(), submittable.getId(), getAttributeValidator());
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
