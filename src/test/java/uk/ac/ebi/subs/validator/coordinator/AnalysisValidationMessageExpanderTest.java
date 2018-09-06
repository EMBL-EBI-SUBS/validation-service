package uk.ac.ebi.subs.validator.coordinator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.component.StudyRef;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.repository.model.Assay;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.Study;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;
import uk.ac.ebi.subs.repository.repos.submittables.StudyRepository;
import uk.ac.ebi.subs.validator.config.MongoDBDependentTest;
import uk.ac.ebi.subs.validator.data.AnalysisValidationEnvelope;
import uk.ac.ebi.subs.validator.data.AssayValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackageClasses = {SampleRepository.class, StudyRepository.class, SubmissionRepository.class, SubmissionStatusRepository.class})
@Category(MongoDBDependentTest.class)
@EnableAutoConfiguration
@SpringBootTest(classes = AnalysisValidationMessageEnvelopeExpander.class)
public class AnalysisValidationMessageExpanderTest {

    @Autowired
    private SampleRepository sampleRepository;
    @Autowired
    private StudyRepository studyRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private SubmissionStatusRepository submissionStatusRepository;

    @Autowired
    AnalysisValidationMessageEnvelopeExpander expander;

    private Team team;
    private Submission submission;
    private Study savedStudy;
    private Sample savedSample;

    @Before
    public void setup() {
        team = MesssageEnvelopeTestHelper.createTeam();
        submission = MesssageEnvelopeTestHelper.saveNewSubmission(submissionStatusRepository, submissionRepository, team);
        savedStudy = MesssageEnvelopeTestHelper.createAndSaveStudy(studyRepository, submission, team);
        savedSample = MesssageEnvelopeTestHelper.createAndSaveSamples(sampleRepository, submission, team, 1).get(0);
    }

    @After
    public void finish() {
        studyRepository.delete(savedStudy);
        sampleRepository.delete(savedSample);
        submissionRepository.delete(submission);
        submissionStatusRepository.delete(submission.getSubmissionStatus());
    }

    @Test
    public void test_analysis_expansion() {
        AnalysisValidationEnvelope analysisValidationEnvelope = createAnalysisValidationEnvelope();

        SampleRef sampleRef = new SampleRef();
        sampleRef.setAlias(savedSample.getAlias());
        sampleRef.setTeam(team.getName());

        analysisValidationEnvelope.getEntityToValidate().getSampleRefs().add(sampleRef);

        StudyRef studyRef = new StudyRef();
        studyRef.setAccession(savedStudy.getAccession());

        analysisValidationEnvelope.getEntityToValidate().getStudyRefs().add(studyRef);


        expander.expandEnvelope(analysisValidationEnvelope);

        assertThat(analysisValidationEnvelope.getStudies().get(0).getBaseSubmittable(), is(savedStudy));
        final Submittable<uk.ac.ebi.subs.data.submittable.Sample> sampleFromExpandedEnvelope = analysisValidationEnvelope.getSamples().get(0);
        assertThat(sampleFromExpandedEnvelope.getAccession(), is(savedSample.getAccession()));
        assertThat(sampleFromExpandedEnvelope.getId(), is(savedSample.getId()));
        assertThat(sampleFromExpandedEnvelope.getAlias(), is(savedSample.getAlias()));
        assertThat(sampleFromExpandedEnvelope.getDescription(), is(savedSample.getDescription()));
        assertThat(sampleFromExpandedEnvelope.getSubmissionId(), is(savedSample.getSubmission().getId()));
        assertThat(sampleFromExpandedEnvelope.getTeam(), is(savedSample.getTeam()));
        assertThat(sampleFromExpandedEnvelope.getTitle(), is(savedSample.getTitle()));
    }

    private AnalysisValidationEnvelope createAnalysisValidationEnvelope() {
        AnalysisValidationEnvelope analysisValidationEnvelope = new AnalysisValidationEnvelope();
        uk.ac.ebi.subs.data.submittable.Analysis submittableAnalysis = new uk.ac.ebi.subs.data.submittable.Analysis();
        submittableAnalysis.setTeam(team);
        submittableAnalysis.setAccession(UUID.randomUUID().toString());
        submittableAnalysis.setAlias(UUID.randomUUID().toString());
        analysisValidationEnvelope.setEntityToValidate(submittableAnalysis);
        return analysisValidationEnvelope;
    }

}
