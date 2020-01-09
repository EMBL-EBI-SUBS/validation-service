package uk.ac.ebi.subs.validator.flipper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.validator.data.AggregatorToFlipperEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.data.structures.GlobalValidationStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by karoly on 18/05/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackageClasses = ValidationResultRepository.class)
@EnableAutoConfiguration
@SpringBootTest(classes = StatusFlipperValidationResultService.class)
public class StatusFlipperValidationResultServiceTest {

    @Autowired
    ValidationResultRepository repository;

    @Autowired
    StatusFlipperValidationResultService service;

    private ValidationResult existingValidationResult;
    private AggregatorToFlipperEnvelope envelope;

    @Before
    public void setUp() {
        repository.deleteAll();
        existingValidationResult = createValidationResult(getInitialExpectedResults(), 3, "123456");
        envelope = new AggregatorToFlipperEnvelope("123456", 3);
        repository.insert(existingValidationResult);
    }

    @Test
    public void updatingNotExistingValidationResultDocumentReturnFalse() {
        repository.delete(existingValidationResult);

        assertThat(service.updateValidationResult(envelope), is(false));
    }

    @Test
    public void notAllEntityHasBeenValidatedShouldLeaveValidationStatusPending() {
        assertThat(existingValidationResult.getValidationStatus() == GlobalValidationStatus.Pending, is(true));

        existingValidationResult.getExpectedResults().put(ValidationAuthor.Taxonomy, Collections.singletonList(new SingleValidationResult()));
        repository.save(existingValidationResult);

        service.updateValidationResult(envelope);

        ValidationResult actualValidationResultDocument = repository.findById(existingValidationResult.getUuid()).orElse(null);

        assertNotNull(actualValidationResultDocument);
        assertThat(actualValidationResultDocument.getValidationStatus() == GlobalValidationStatus.Pending, is(true));
        assertThat(actualValidationResultDocument.getExpectedResults().get(ValidationAuthor.Taxonomy).isEmpty(), is(false));
        assertThat(actualValidationResultDocument.getExpectedResults().get(ValidationAuthor.Biosamples), is(new ArrayList<>()));
        assertThat(actualValidationResultDocument.getExpectedResults().get(ValidationAuthor.Ena), is(new ArrayList<>()));

    }

    @Test
    public void allEntityHasBeenValidatedShouldChangeValidationStatusToComplete() {
        assertThat(existingValidationResult.getValidationStatus() == GlobalValidationStatus.Pending, is(true));

        existingValidationResult.getExpectedResults().put(ValidationAuthor.Taxonomy, Collections.singletonList(new SingleValidationResult()));
        existingValidationResult.getExpectedResults().put(ValidationAuthor.Biosamples, Collections.singletonList(new SingleValidationResult()));
        existingValidationResult.getExpectedResults().put(ValidationAuthor.Ena, Collections.singletonList(new SingleValidationResult()));
        repository.save(existingValidationResult);

        service.updateValidationResult(envelope);

        ValidationResult actualValidationResultDocument = repository.findById(existingValidationResult.getUuid()).orElse(null);

        assertNotNull(actualValidationResultDocument);
        assertThat(actualValidationResultDocument.getValidationStatus() == GlobalValidationStatus.Complete, is(true));
        assertThat(actualValidationResultDocument.getExpectedResults().get(ValidationAuthor.Taxonomy).isEmpty(), is(false));
        assertThat(actualValidationResultDocument.getExpectedResults().get(ValidationAuthor.Biosamples).isEmpty(), is(false));
        assertThat(actualValidationResultDocument.getExpectedResults().get(ValidationAuthor.Ena).isEmpty(), is(false));

    }

    private ValidationResult createValidationResult(Map<ValidationAuthor, List<SingleValidationResult>> expectedResults, int version, String resultUuid) {
        ValidationResult validationResult = new ValidationResult();
        validationResult.setUuid(resultUuid);
        validationResult.setExpectedResults(expectedResults);
        validationResult.setVersion(version);
        return validationResult;
    }

    private Map<ValidationAuthor, List<SingleValidationResult>> getInitialExpectedResults() {
        Map<ValidationAuthor, List<SingleValidationResult>> expectedResults = new HashMap<>();
        expectedResults.put(ValidationAuthor.Taxonomy, new ArrayList<>());
        expectedResults.put(ValidationAuthor.Biosamples, new ArrayList<>());
        expectedResults.put(ValidationAuthor.Ena, new ArrayList<>());
        return expectedResults;
    }
}
