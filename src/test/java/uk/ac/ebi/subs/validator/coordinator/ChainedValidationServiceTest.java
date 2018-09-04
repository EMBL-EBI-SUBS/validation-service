package uk.ac.ebi.subs.validator.coordinator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.Submittable;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.StoredSubmittable;
import uk.ac.ebi.subs.repository.model.Study;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;
import uk.ac.ebi.subs.repository.repos.submittables.StudyRepository;
import uk.ac.ebi.subs.validator.TestUtils;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.data.structures.GlobalValidationStatus;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ChainedValidationServiceTest {

    @Autowired
    private ChainedValidationService service;
    @MockBean
    private SubmittableHandler submittableHandler;

    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private SampleRepository sampleRepository;
    @Autowired
    private StudyRepository studyRepository;
    @Autowired
    private ValidationResultRepository validationResultRepository;
    @Autowired
    private DataTypeRepository dataTypeRepository;


    private Submission submission;
    private Study study;

    private DataType dataType;


    @Before
    public void setUp() {
        clearDB();

        dataType = TestUtils.createDataType("aDataType", Collections.emptyList())      ;
        dataTypeRepository.insert(
                dataType
        );

        submission = createSubmission();
        submissionRepository.insert(submission);
        createSamples();
        study = createStudy();
    }

    @Test
    public void findSubmittablesInSubmissionTest() {
        Stream<? extends StoredSubmittable> submittablesStream = service.streamSubmittablesInSubmission(submission.getId());

        Map<String, List<StoredSubmittable>> submittablesInSubmission = groupSubmittablesByClassName(submittablesStream);

        Assert.assertEquals(3, submittablesInSubmission.get("Sample").size());
        Assert.assertFalse(submittablesInSubmission.containsKey("Assay"));
        Assert.assertEquals(1, submittablesInSubmission.get("Study").size());
        Assert.assertFalse(submittablesInSubmission.containsKey("AssayData"));
    }

    private Map<String, List<StoredSubmittable>> groupSubmittablesByClassName(Stream<? extends StoredSubmittable> submittablesStream) {
        Map<String, List<StoredSubmittable>> submittablesInSubmission = new HashMap<>();

        submittablesStream.forEach(submittable -> {
            String key = submittable.getClass().getSimpleName();

            if (!submittablesInSubmission.containsKey(key)) {
                submittablesInSubmission.put(key, new ArrayList<>());
            }
            submittablesInSubmission.get(key).add(submittable);
        });
        return submittablesInSubmission;
    }

    @Test
    public void filterOutTriggerSubmittableTest() {
        Stream<? extends StoredSubmittable> submittablesStream = service.streamSubmittablesInSubmissionExceptTriggerSubmittable(study, submission.getId());

        Map<String, List<StoredSubmittable>> submittablesInSubmission = groupSubmittablesByClassName(submittablesStream);

        Assert.assertFalse(submittablesInSubmission.containsKey("Study"));

        Assert.assertEquals(3, submittablesInSubmission.get("Sample").size());
        Assert.assertFalse(submittablesInSubmission.containsKey("Assay"));
        Assert.assertFalse(submittablesInSubmission.containsKey("AssayData"));
    }

    @Test
    public void triggerChainedValidationTest() {
        service.triggerChainedValidation(study, submission.getId());
        verify(submittableHandler, times(3)).handleSubmittable(any(), any(), any());
    }

    private Submission createSubmission() {
        Submission sub = new Submission();
        sub.setId(UUID.randomUUID().toString());
        Team d = generateTestTeam();
        sub.setTeam(d);
        return sub;
    }

    private void createSamples() {
        for (int i = 0; i < 3; i++) {
            Sample sample = new Sample();
            sample.setId(UUID.randomUUID().toString());
            sample.setSubmission(submission);
            sample.setDataType(dataType);
            sampleRepository.insert(sample);
            generateValidationResult(sample.getId());
        }
    }

    private Study createStudy() {
        Study study = new Study();
        study.setId(UUID.randomUUID().toString());
        study.setSubmission(submission);
        study.setDataType(dataType);
        Team d = generateTestTeam();
        study.setTeam(d);
        generateValidationResult(study.getId());
        return studyRepository.insert(study);
    }

    private static Team generateTestTeam() {
        Team d = new Team();
        d.setName("self.usi-user");
        return d;
    }

    private void generateValidationResult(String submittableId) {
        ValidationResult validationResult = new ValidationResult();
        validationResult.setUuid(UUID.randomUUID().toString());
        validationResult.setEntityUuid(submittableId);
        validationResult.setVersion(1);
        validationResult.setSubmissionId(submission.getId());
        validationResult.setValidationStatus(GlobalValidationStatus.Complete);
        validationResultRepository.save(validationResult);
    }

    private void clearDB() {
        submissionRepository.deleteAll();
        sampleRepository.deleteAll();
        studyRepository.deleteAll();
        dataTypeRepository.deleteAll();
    }
}
