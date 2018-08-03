package uk.ac.ebi.subs.validator.coordinator;

import uk.ac.ebi.subs.data.component.AbstractSubsRef;
import uk.ac.ebi.subs.data.component.ProtocolRef;
import uk.ac.ebi.subs.data.component.StudyDataType;
import uk.ac.ebi.subs.data.component.Team;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MesssageEnvelopeTestHelper {

    static Submission saveNewSubmission(SubmissionStatusRepository submissionStatusRepository, SubmissionRepository submissionRepository, Team team) {
        Submission submssion = new Submission();
        submssion.setId(UUID.randomUUID().toString());

        submssion.setTeam(team);

        submssion.setSubmissionStatus(new SubmissionStatus(SubmissionStatusEnum.Draft));
        submssion.getSubmissionStatus().setTeam(team);

        submissionStatusRepository.insert(submssion.getSubmissionStatus());
        submissionRepository.save(submssion);
        return submssion;
    }

    static Team createTeam () {
        Team team = new Team();
        team.setName(UUID.randomUUID().toString());
        return team;
    }

    static List<Sample> createAndSaveSamples (SampleRepository sampleRepository, Submission submission, Team team, int sampleNumber) {
        List<Sample> sampleList = createSamples(submission,team,sampleNumber);

        for (Sample sample : sampleList) {
            sampleRepository.save(sample);
        }

        return sampleList;
    }

    static List<Sample> createSamples (Submission submission, Team team, int sampleNumber) {
        List<Sample> sampleList = new ArrayList<>(sampleNumber);
        for (int i = 0; i < sampleNumber; i++ ) {
            Sample sample = new Sample();
            sample.setTeam(team);
            String alias = UUID.randomUUID().toString();
            String accession = UUID.randomUUID().toString();
            sample.setAlias(alias);
            sample.setAccession(accession);
            sample.setSubmission(submission);
            sampleList.add(sample);
        }
        return sampleList;
    }

    static List<Protocol> createProtocols (Submission submission, Team team, int protocolNumber) {
        List<Protocol> protocols = new ArrayList<>(protocolNumber);
        for (int i = 0; i < protocolNumber; i++ ) {
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

    static List<ProtocolRef> createProtocolRefs(List<Protocol> protocols){
        List<ProtocolRef> refs = new ArrayList<>();
        for(Protocol protocol : protocols){
            refs.add((ProtocolRef) protocol.asRef());
        }
        return refs;
    }

    public static Study createAndSaveStudy (StudyRepository studyRepository, Submission submission, Team team) {
        Study study = new Study();
        study.setTeam(team);
        String projectAccession = UUID.randomUUID().toString();
        String projectAlias = UUID.randomUUID().toString();
        study.setAlias(projectAlias);
        study.setAccession(projectAccession);
        study.setSubmission(submission);
        study.setStudyType(StudyDataType.Metabolomics_LCMS);
        return studyRepository.save(study);
    }


    public static List<Protocol> createAndSaveProtocols(ProtocolRepository protocolRepository, Submission submission, Team team) {
        List<Protocol> protocols = MesssageEnvelopeTestHelper.createProtocols(submission,team, 3);
        return protocolRepository.save(protocols);
    }
}
