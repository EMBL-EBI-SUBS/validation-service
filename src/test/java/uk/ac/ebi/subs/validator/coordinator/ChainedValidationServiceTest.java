package uk.ac.ebi.subs.validator.coordinator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.data.component.AbstractSubsRef;
import uk.ac.ebi.subs.data.component.AnalysisRef;
import uk.ac.ebi.subs.data.component.AssayRef;
import uk.ac.ebi.subs.data.component.EgaDacPolicyRef;
import uk.ac.ebi.subs.data.component.EgaDacRef;
import uk.ac.ebi.subs.data.component.EgaDatasetRef;
import uk.ac.ebi.subs.data.component.ProjectRef;
import uk.ac.ebi.subs.data.component.ProtocolRef;
import uk.ac.ebi.subs.data.component.SampleGroupRef;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.component.StudyRef;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.Analysis;
import uk.ac.ebi.subs.data.submittable.EgaDac;
import uk.ac.ebi.subs.data.submittable.EgaDataset;
import uk.ac.ebi.subs.data.submittable.Project;
import uk.ac.ebi.subs.data.submittable.SampleGroup;
import uk.ac.ebi.subs.data.submittable.Submittable;
import uk.ac.ebi.subs.repository.model.Assay;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.model.EgaDacPolicy;
import uk.ac.ebi.subs.repository.model.Protocol;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.Study;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.repos.submittables.AssayRepository;
import uk.ac.ebi.subs.repository.repos.submittables.StudyRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
public class ChainedValidationServiceTest {

    //service under test
    private ChainedValidationService service;

    //mock beans
    private SubmittableHandler submittableHandler;
    private StudyRepository studyRepository;
    private AssayRepository assayRepository;


    //test data
    private Study study;
    private Assay assay;

    private String submissionId = "submission";
    private String dataTypeId = "dt1";

    @Before
    public void setUp() {
        submittableHandler = Mockito.mock(SubmittableHandler.class);
        studyRepository = Mockito.mock(StudyRepository.class);
        assayRepository = Mockito.mock(AssayRepository.class);

        service = new ChainedValidationService(Arrays.asList(assayRepository, studyRepository), submittableHandler);

        DataType dataType = new DataType();
        dataType.setId(dataTypeId);

        Submission submission = new Submission();
        submission.setId(submissionId);

        assay = new Assay();
        assay.setDataType(dataType);
        assay.setSubmission(submission);

        study = new Study();
        fillInNames(study);
        study.setSubmission(submission);
    }

    @Test
    public void test_chaining_validation() {
        AbstractSubsRef ref = study.asRef();

        Mockito.when(
                assayRepository.findBySubmissionIdAndReference(submissionId, ref)
        ).thenReturn(Collections.singletonList(assay));

        Mockito.when(
                studyRepository.findBySubmissionIdAndReference(submissionId, ref)
        ).thenReturn(Collections.emptyList());

        service.triggerChainedValidation(study, submissionId);

        Mockito.verify(submittableHandler).handleSubmittable(assay, submissionId, dataTypeId, null);

    }

    @Test
    public void test_ref_generation() {
        List<Submittable> submittables = Arrays.asList(
                new Analysis(),
                new Assay(),
                new EgaDac(),
                new EgaDacPolicy(),
                new EgaDataset(),
                new Project(),
                new Protocol(),
                new Sample(),
                new SampleGroup(),
                new Study()
        );
        submittables.forEach(this::fillInNames);

        List<AbstractSubsRef> expectedRefs = Arrays.asList(
                new AnalysisRef(),
                new AssayRef(),
                new EgaDacRef(),
                new EgaDacPolicyRef(),
                new EgaDatasetRef(),
                new ProjectRef(),
                new ProtocolRef(),
                new SampleRef(),
                new SampleGroupRef(),
                new StudyRef()
        );
        expectedRefs.forEach(this::fillInNames);

        List<AbstractSubsRef> actualRefs = submittables.stream()
                .map(submittable -> service.submittableToRef(submittable))
                .collect(Collectors.toList());

        Assert.assertEquals(
                expectedRefs,
                actualRefs
        );

    }

    private void fillInNames(Submittable submittable) {
        submittable.setAlias("foo");
        submittable.setTeam(Team.build("bar"));
        submittable.setAccession("X1234");
    }

    private void fillInNames(AbstractSubsRef ref) {
        ref.setAlias("foo");
        ref.setTeam("bar");
        ref.setAccession("X1234");
    }


    private static Team generateTestTeam() {
        Team d = new Team();
        d.setName("self.usi-user");
        return d;
    }


}
