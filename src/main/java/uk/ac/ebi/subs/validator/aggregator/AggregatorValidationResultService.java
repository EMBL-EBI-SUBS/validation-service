package uk.ac.ebi.subs.validator.aggregator;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.data.structures.GlobalValidationStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

@Service
public class AggregatorValidationResultService {

    private MongoTemplate mongoTemplate;

    public AggregatorValidationResultService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public boolean updateValidationResult(SingleValidationResultsEnvelope envelope) {

        final boolean isFileContentValidation = envelope.getValidationAuthor().equals(ValidationAuthor.FileContent);

        Query query = new Query(Criteria.where("_id").is(envelope.getValidationResultUUID()));

        if (!isFileContentValidation) {
            query.addCriteria(Criteria.where("version").is(envelope.getValidationResultVersion()));
        }

        Update update = new Update().set("expectedResults." + envelope.getValidationAuthor(), envelope.getSingleValidationResults());

        ValidationResult validationResult = mongoTemplate.findAndModify(query, update, ValidationResult.class);

        if (isFileContentValidation) {
            validationResult.setValidationStatus(GlobalValidationStatus.Complete);
            mongoTemplate.save(validationResult);
        }

        return validationResult != null;
    }

}