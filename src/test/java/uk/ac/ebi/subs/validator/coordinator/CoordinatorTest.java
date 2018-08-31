package uk.ac.ebi.subs.validator.coordinator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.ValidationServiceApplication;
import uk.ac.ebi.subs.data.submittable.Project;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.validator.TestUtils;
import uk.ac.ebi.subs.validator.config.RabbitMQDependentTest;
import uk.ac.ebi.subs.validator.data.ProjectValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.data.SampleValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static uk.ac.ebi.subs.validator.TestUtils.createProject;
import static uk.ac.ebi.subs.validator.TestUtils.createSample;
import static uk.ac.ebi.subs.validator.TestUtils.createValidationResult;

/**
 * Created by karoly on 30/05/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
@Category(RabbitMQDependentTest.class)
@SpringBootTest(classes = ValidationServiceApplication.class )
public class CoordinatorTest {

    @Autowired
    private CoordinatorListener coordinator;

    @Autowired
    private ValidationResultRepository validationResultRepository;

    @Autowired
    private DataTypeRepository dataTypeRepository;


    private Sample sample;
    private Project project;
    private DataType dataType;


    @Before
    public void setUp() {
        clearDbs();

        dataType = TestUtils.createDataType("aDataType", Collections.emptyList())      ;
        dataTypeRepository.insert(
                dataType
        );

        sample = TestUtils.createSample();
        validationResultRepository.save(createValidationResult(sample.getId()));

        project = createProject();
        validationResultRepository.save(createValidationResult(project.getId()));
    }

    @After
    public void clearDbs() {
        validationResultRepository.deleteAll();
        dataTypeRepository.deleteAll();
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testSubmissionWithoutContent() {
        String expectedErrorMessage = "The envelope should contain a sample.";
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(expectedErrorMessage);

        SampleValidationEnvelopeToCoordinator submittableEnvelopWithoutSample = createSubmittableEnvelopeWithoutSample();
        coordinator.processSampleSubmission(submittableEnvelopWithoutSample);
    }

    @Test
    public void testSubmissionWithSample() {
        SampleValidationEnvelopeToCoordinator submittableValidationEnvelope = createSubmittableEnvelopeWithSample(sample);

        try {
            coordinator.processSampleSubmission(submittableValidationEnvelope);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void handleDeletedSubmittable() {
        Sample sample = createSample();
        SampleValidationEnvelopeToCoordinator envelope = new SampleValidationEnvelopeToCoordinator();
        envelope.setEntityToValidate(sample);
        envelope.setDataTypeId(dataType.getId());

        try {
            coordinator.processSampleSubmission(envelope);
        } catch (IllegalStateException exception) {
            assertTrue(exception.getMessage().startsWith("Could not find ValidationResult for submittable with ID"));
        }
    }

    @Test
    public void testSubmissionWithProject() {
        ProjectValidationEnvelopeToCoordinator envelope = new ProjectValidationEnvelopeToCoordinator();
        envelope.setEntityToValidate(project);
        envelope.setDataTypeId(dataType.getId());
        envelope.setSubmissionId(UUID.randomUUID().toString());

        coordinator.processProjectSubmission(envelope);
    }

    private SampleValidationEnvelopeToCoordinator createSubmittableEnvelopeWithoutSample() {
        String submissionId = "testSubmissionId1";

        SampleValidationEnvelopeToCoordinator envelope = new SampleValidationEnvelopeToCoordinator();
        envelope.setSubmissionId(submissionId);
        envelope.setDataTypeId(dataType.getId());

        return envelope;
    }

    private SampleValidationEnvelopeToCoordinator createSubmittableEnvelopeWithSample(Sample sample) {
        SampleValidationEnvelopeToCoordinator envelope = createSubmittableEnvelopeWithoutSample();
        envelope.setEntityToValidate(sample);

        return envelope;
    }

}
