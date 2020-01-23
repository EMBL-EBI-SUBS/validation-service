package uk.ac.ebi.subs.validator.aggregator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@EnableMongoRepositories(basePackageClasses = ValidationResultRepository.class)
@EnableAutoConfiguration
@SpringBootTest(classes = AggregatorValidationResultService.class)
public class AggregatorValidationResultServiceTest {

    @Autowired
    private ValidationResultRepository repository;
    @Autowired
    private AggregatorValidationResultService service;

    private static final String UUID_1 = UUID.randomUUID().toString();
    private static final String UUID_2 = UUID.randomUUID().toString();
    private static final String entityUUID_1 = "22334455";
    private static final String entityUUID_2 = "99882255";

    @Before
    public void setUp() {
        repository.deleteAll();
        repository.insert(generateValidationResults());
    }

    /**
     * Update validation result document successfully.
     */
    @Test
    public void updateValidationResultSuccessfully() {
        SingleValidationResultsEnvelope singleValidationResultsEnvelope = new SingleValidationResultsEnvelope(
                Collections.singletonList(generateSingleValidationResult(entityUUID_1)),
                1,
                UUID_1,
                ValidationAuthor.Biosamples
        );
        assertTrue(service.updateValidationResult(singleValidationResultsEnvelope));

        ValidationResult validationResult = repository.findOne(UUID_1);
        assertNotNull(validationResult);
        assertFalse(validationResult.getExpectedResults().get(ValidationAuthor.Biosamples).isEmpty());
    }

    /**
     * Ignore entity validation result that refers to an older version and skip the validation result document update.
     */
    @Test
    public void ignoreObsoleteSingleValidationResult() {

        SingleValidationResultsEnvelope singleValidationResultsEnvelope = new SingleValidationResultsEnvelope(
                Collections.singletonList(generateSingleValidationResult(entityUUID_2)),
                1,
                UUID_2,
                ValidationAuthor.Biosamples
        );
        assertFalse(service.updateValidationResult(singleValidationResultsEnvelope));
    }

    /**
     * Ignore if it can't find the Validation Result.
     */
    @Test
    public void handleDeletedSubmittable() {
        SingleValidationResultsEnvelope envelope = new SingleValidationResultsEnvelope();
        envelope.setValidationResultUUID("missing");
        envelope.setValidationAuthor(ValidationAuthor.Biosamples);

        assertFalse(service.updateValidationResult(envelope));
    }

    private List<ValidationResult> generateValidationResults() {
        List<ValidationResult> validationResults = new ArrayList<>();

        Map<ValidationAuthor, List<SingleValidationResult>> validationAuthorListMap = new HashMap<>();
        validationAuthorListMap.put(ValidationAuthor.Taxonomy, new ArrayList<>());
        validationAuthorListMap.put(ValidationAuthor.Biosamples, new ArrayList<>());

        // First
        ValidationResult validationResult1 = new ValidationResult();
        validationResult1.setUuid(UUID_1);
        validationResult1.setExpectedResults(validationAuthorListMap);
        validationResult1.setVersion(1);
        validationResult1.setSubmissionId("123");
        validationResult1.setEntityUuid(entityUUID_1);
        validationResults.add(validationResult1);

        // Second
        ValidationResult validationResult2 = new ValidationResult();
        validationResult2.setUuid(UUID_2);
        validationResult2.setExpectedResults(validationAuthorListMap);
        validationResult2.setVersion(2);
        validationResult2.setSubmissionId("123");
        validationResult2.setEntityUuid(entityUUID_2);
        validationResults.add(validationResult2);

        return validationResults;
    }

    private SingleValidationResult generateSingleValidationResult(String entityUUID) {
        SingleValidationResult singleValidationResult = new SingleValidationResult();
        singleValidationResult.setEntityUuid(entityUUID);
        singleValidationResult.setValidationStatus(SingleValidationResultStatus.Pass);
        singleValidationResult.setValidationAuthor(ValidationAuthor.Biosamples);
        return singleValidationResult;
    }

}
