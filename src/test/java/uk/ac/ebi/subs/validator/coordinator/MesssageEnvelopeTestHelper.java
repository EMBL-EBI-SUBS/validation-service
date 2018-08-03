package uk.ac.ebi.subs.validator.coordinator;

import uk.ac.ebi.subs.data.component.*;
import uk.ac.ebi.subs.data.status.SubmissionStatusEnum;
import uk.ac.ebi.subs.repository.model.Protocol;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.Study;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.model.SubmissionStatus;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;
import uk.ac.ebi.subs.repository.repos.submittables.ProtocolRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;
import uk.ac.ebi.subs.repository.repos.submittables.StudyRepository;
import uk.ac.ebi.subs.validator.data.SampleValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

public class MesssageEnvelopeTestHelper {

    static Submission saveNewSubmission(SubmissionStatusRepository submissionStatusRepository, SubmissionRepository submissionRepository, Team team) {
        Submission submission = createNewSubmission(team);
        submissionStatusRepository.insert(submission.getSubmissionStatus());
        submissionRepository.save(submission);
        return submission;
    }

    static Submission createNewSubmission(Team team) {
        Submission submssion = new Submission();
        submssion.setId(UUID.randomUUID().toString());

        submssion.setTeam(team);

        submssion.setSubmissionStatus(new SubmissionStatus(SubmissionStatusEnum.Draft));
        submssion.getSubmissionStatus().setTeam(team);
        return submssion;
    }

    static Team createTeam() {
        Team team = new Team();
        team.setName(UUID.randomUUID().toString());
        return team;
    }

    static List<Sample> createAndSaveSamples(SampleRepository sampleRepository, Submission submission, Team team, int sampleNumber) {
        List<Sample> sampleList = createSamples(submission, team, sampleNumber);

        for (Sample sample : sampleList) {
            sampleRepository.save(sample);
        }

        return sampleList;
    }

    static List<Sample> createSamples(Submission submission, Team team, int sampleNumber) {
        List<Sample> sampleList = new ArrayList<>(sampleNumber);
        for (int i = 0; i < sampleNumber; i++) {
            Sample sample = new Sample();
            sample.setTeam(team);
            String alias = UUID.randomUUID().toString();
            String accession = UUID.randomUUID().toString();
            sample.setAlias(alias);
            sample.setAccession(accession);
            sample.setSubmission(submission);
            sample.setAttributes(generateUsiAttributes());
            sample.setTaxonId(10090L);
            sample.setReleaseDate(LocalDate.now());
            sampleList.add(sample);
        }
        return sampleList;
    }

    static Map<String, Collection<Attribute>> generateUsiAttributes() {
        Map<String, Collection<Attribute>> usiAttributes = new HashMap<>();

        Attribute usiAttribute_1 = new Attribute();
        usiAttribute_1.setValue("1.5");
        Term term = new Term();
        term.setUrl("http://purl.obolibrary.org/obo/UO_0000036");
        usiAttribute_1.setTerms(Arrays.asList(term));
        usiAttribute_1.setUnits("year");
        usiAttributes.put("age", Arrays.asList(usiAttribute_1));

        Attribute usiAttribute_2 = new Attribute();
        usiAttribute_2.setValue(Instant.now().toString());
        usiAttributes.put("update", Arrays.asList(usiAttribute_2));

        Attribute usiAttribute_3 = new Attribute();
        usiAttribute_3.setValue("mouse");
        Term t = new Term();
        t.setUrl("http://purl.obolibrary.org/obo/NCBITaxon_10090");
        usiAttribute_3.setTerms(Arrays.asList(t));
        usiAttributes.put("synonym", Arrays.asList(usiAttribute_3));

        return usiAttributes;
    }

    static List<Protocol> createProtocols(Submission submission, Team team, int protocolNumber) {
        List<Protocol> protocols = new ArrayList<>(protocolNumber);
        for (int i = 0; i < protocolNumber; i++) {
            Protocol protocol = new Protocol();
            protocol.setAccession(UUID.randomUUID().toString());
            protocol.setAlias(UUID.randomUUID().toString());
            protocol.setTitle("Sample collection");
            protocol.setDescription("Test collection");
            protocol.setTeam(team);
            protocol.setSubmission(submission);
            protocols.add(protocol);
        }
        return protocols;
    }

    static List<ProtocolRef> createProtocolRefs(List<Protocol> protocols) {
        List<ProtocolRef> refs = new ArrayList<>();
        for (Protocol protocol : protocols) {
            refs.add((ProtocolRef) protocol.asRef());
        }
        return refs;
    }

    public static Study createAndSaveStudy(StudyRepository studyRepository, Submission submission, Team team) {
        Study study = createStudy(submission, team);
        return studyRepository.save(study);
    }

    public static Study createStudy(Submission submission, Team team) {
        Study study = new Study();
        study.setTeam(team);
        String projectAccession = UUID.randomUUID().toString();
        String projectAlias = UUID.randomUUID().toString();
        study.setAlias(projectAlias);
        study.setAccession(projectAccession);
        study.setSubmission(submission);
        study.setStudyType(StudyDataType.Metabolomics_LCMS);
        return study;
    }


    public static List<Protocol> createAndSaveProtocols(ProtocolRepository protocolRepository, Submission submission, Team team) {
        List<Protocol> protocols = MesssageEnvelopeTestHelper.createProtocols(submission, team, 3);
        return protocolRepository.save(protocols);
    }

    public static SampleValidationMessageEnvelope getSampleValidationEnvelope() {
        SampleValidationMessageEnvelope sampleValidationMessageEnvelope = new SampleValidationMessageEnvelope();
        Team team = createTeam();
        Submission submission = createNewSubmission(team);

        sampleValidationMessageEnvelope.setSubmissionId(submission.getId());
        sampleValidationMessageEnvelope.setValidationResultUUID(UUID.randomUUID().toString());

        List<Sample> samples = createSamples(submission, team, 3);
        sampleValidationMessageEnvelope.setEntityToValidate(getSubmittableSample(samples.get(0)));

        sampleValidationMessageEnvelope.setSampleList(getSubmittableSamples(samples, submission.getId()));
        return sampleValidationMessageEnvelope;
    }

    static uk.ac.ebi.subs.data.submittable.Sample getSubmittableSample(Sample sample){
        uk.ac.ebi.subs.data.submittable.Sample sample1 = new uk.ac.ebi.subs.data.submittable.Sample();
        sample1.setTeam(sample.getTeam());
        sample1.setAlias(sample.getAlias());
        sample1.setAccession(sample.getAccession());
        sample1.setAttributes(sample.getAttributes());
        sample1.setTaxonId(sample.getTaxonId());
        sample1.setReleaseDate(sample.getReleaseDate());
        return sample1;
    }

    static List<uk.ac.ebi.subs.validator.model.Submittable<uk.ac.ebi.subs.data.submittable.Sample>> getSubmittableSamples(List<Sample> samples, String submissionID) {
        List<uk.ac.ebi.subs.validator.model.Submittable<uk.ac.ebi.subs.data.submittable.Sample>> submittableSamples = new ArrayList<>();
        for (Sample sample : samples) {
            submittableSamples.add(new Submittable<uk.ac.ebi.subs.data.submittable.Sample>(sample, submissionID));
        }
        return submittableSamples;
    }
}
