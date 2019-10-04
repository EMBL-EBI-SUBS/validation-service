package uk.ac.ebi.subs.validator.coordinator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.data.component.AssayRef;
import uk.ac.ebi.subs.data.component.ProtocolRef;
import uk.ac.ebi.subs.data.component.ProtocolUse;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.repository.model.Assay;
import uk.ac.ebi.subs.repository.model.Protocol;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;
import uk.ac.ebi.subs.repository.repos.submittables.AssayRepository;
import uk.ac.ebi.subs.repository.repos.submittables.ProtocolRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;
import uk.ac.ebi.subs.validator.data.AssayDataValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackageClasses = {SampleRepository.class, AssayRepository.class, SubmissionRepository.class, SubmissionStatusRepository.class, ValidationResultRepository.class})
@EnableAutoConfiguration
@SpringBootTest(classes = AssayDataValidationMessageEnvelopeExpander.class)
public class AssayDataValidationMessageEnvelopeExpanderTest {

    @Autowired
    AssayRepository assayRepository;

    @Autowired
    SampleRepository sampleRepository;

    @Autowired
    SubmissionStatusRepository submissionStatusRepository;

    @Autowired
    SubmissionRepository submissionRepository;

    @Autowired
    ProtocolRepository protocolRepository;

    @Autowired
    AssayDataValidationMessageEnvelopeExpander assayDataValidationMessageEnvelopeExpander;

    Team team;
    Submission submission;
    Assay savedAssay;
    Assay secondAssay;
    Sample savedSample;
    List<Protocol> savedProtocols;

    @Before
    public void setup() {
        team = MessageEnvelopeTestHelper.createTeam();
        submission = MessageEnvelopeTestHelper.saveNewSubmission(submissionStatusRepository, submissionRepository, team);
        savedAssay = createAndSaveAssay(submission,team);
        secondAssay = createAndSaveAssay(submission,team);
        savedSample = MessageEnvelopeTestHelper.createAndSaveSamples(sampleRepository, submission, team, 1).get(0);
        savedProtocols = MessageEnvelopeTestHelper.createAndSaveProtocols(protocolRepository,submission,team);
    }

    @After
    public void finish() {
        assayRepository.delete(savedAssay);
        assayRepository.delete(secondAssay);
        sampleRepository.delete(savedSample);
        submissionRepository.delete(submission);
        submissionStatusRepository.delete(submission.getSubmissionStatus());
        protocolRepository.delete(savedProtocols);
    }

    @Test
    public void testExpandEnvelopeWithNotExistingAssayRefs() {
        AssayDataValidationMessageEnvelope assayDataValidationMessageEnvelope = createAssayDataValidationMessageEnvelope();
        assayDataValidationMessageEnvelope.setSubmissionId(submission.getId());
        AssayRef assayRef = new AssayRef();
        assayRef.setAlias("Dummy assay");
        assayDataValidationMessageEnvelope.getEntityToValidate().setAssayRefs(Collections.singletonList(assayRef));

        assayDataValidationMessageEnvelopeExpander.expandEnvelope(assayDataValidationMessageEnvelope);

        assertThat(assayDataValidationMessageEnvelope.getAssays(), is(empty()));
    }

    @Test
    public void testExpandEnvelopeWithoutAssayRefs() {
        AssayDataValidationMessageEnvelope assayDataValidationMessageEnvelope = createAssayDataValidationMessageEnvelope();
        assayDataValidationMessageEnvelope.setSubmissionId(submission.getId());

        assayDataValidationMessageEnvelopeExpander.expandEnvelope(assayDataValidationMessageEnvelope);

        assertThat(assayDataValidationMessageEnvelope.getAssays(), is(empty()));
    }

    @Test
    public void testExpandEnvelopeSameSubmissionByAccessionForAssay() {
        AssayDataValidationMessageEnvelope assayDataValidationMessageEnvelope = createAssayDataValidationMessageEnvelope();
        assayDataValidationMessageEnvelope.setSubmissionId(submission.getId());
        AssayRef assayRef = new AssayRef();
        assayRef.setAccession(savedAssay.getAccession());
        assayDataValidationMessageEnvelope.getEntityToValidate().setAssayRefs(Arrays.asList(assayRef));

        assayDataValidationMessageEnvelopeExpander.expandEnvelope(assayDataValidationMessageEnvelope);

        assertThat(savedAssay,is(assayDataValidationMessageEnvelope.getAssays().iterator().next().getBaseSubmittable()));
    }

    @Test
    public void testExpandEnvelopeSameSubmissionByAliasForAssay() {
        AssayDataValidationMessageEnvelope assayDataValidationMessageEnvelope = createAssayDataValidationMessageEnvelope();
        assayDataValidationMessageEnvelope.setSubmissionId(submission.getId());
        AssayRef assayRef = new AssayRef();
        assayRef.setAlias(savedAssay.getAlias());
        assayRef.setTeam(team.getName());
        assayDataValidationMessageEnvelope.getEntityToValidate().setAssayRefs(Collections.singletonList(assayRef));

        assayDataValidationMessageEnvelopeExpander.expandEnvelope(assayDataValidationMessageEnvelope);

        assertThat(savedAssay,is(assayDataValidationMessageEnvelope.getAssays().iterator().next().getBaseSubmittable()));
    }

    private AssayDataValidationMessageEnvelope createAssayDataValidationMessageEnvelope() {
        AssayDataValidationMessageEnvelope assayDataValidationMessageEnvelope = new AssayDataValidationMessageEnvelope();
        uk.ac.ebi.subs.data.submittable.AssayData assayData = new uk.ac.ebi.subs.data.submittable.AssayData();
        assayData.setTeam(team);
        assayData.setAccession(UUID.randomUUID().toString());
        assayData.setAlias(UUID.randomUUID().toString());
        assayDataValidationMessageEnvelope.setEntityToValidate(assayData);
        return assayDataValidationMessageEnvelope;
    }

    private Assay createAndSaveAssay (Submission submission, Team team) {
        Assay assay = new Assay();
        assay.setTeam(team);
        String accession = UUID.randomUUID().toString();
        String alias = UUID.randomUUID().toString();
        assay.setAlias(alias);
        assay.setAccession(accession);
        assay.setSubmission(submission);
        return assayRepository.save(assay);
    }

    @Test
    public void testExpandEnvelopeSameSubmissionByAccessionForProtocol() {
        AssayDataValidationMessageEnvelope assayDataValidationMessageEnvelope = createAssayDataValidationMessageEnvelope();
        assayDataValidationMessageEnvelope.setSubmissionId(submission.getId());
        List<ProtocolUse> protocolUses = new ArrayList<>();
        for(Protocol protocol : savedProtocols){
            ProtocolUse protocolUse = new ProtocolUse();
            protocolUse.setProtocolRef((ProtocolRef) protocol.asRef());
            protocolUses.add(protocolUse);
        }
        savedAssay.setProtocolUses(protocolUses);
        savedAssay = assayRepository.save(savedAssay);

        secondAssay.setProtocolUses(protocolUses);
        assayRepository.save(secondAssay);
        
        List<AssayRef> assayRefs  = new ArrayList<>();
        assayRefs.add((AssayRef) savedAssay.asRef());
        assayRefs.add((AssayRef) secondAssay.asRef());


        assayDataValidationMessageEnvelope.getEntityToValidate().setAssayRefs(assayRefs);
        assayDataValidationMessageEnvelopeExpander.expandEnvelope(assayDataValidationMessageEnvelope);
        assertEquals(assayDataValidationMessageEnvelope.getProtocols().size(),3);
    }

}