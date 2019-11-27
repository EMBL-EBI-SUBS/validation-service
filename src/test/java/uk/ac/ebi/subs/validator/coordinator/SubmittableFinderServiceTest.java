package uk.ac.ebi.subs.validator.coordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SubmittableFinderServiceTest {

    @Autowired
    private SubmittableFinderService submittableFinderService;

    @Autowired
    SubmissionStatusRepository submissionStatusRepository;

    @Autowired
    SubmissionRepository submissionRepository;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private SampleRepository sampleRepository;

    private Team team;
    private Submission submission;
    private Sample sample;

    @Before
    public void setup() {
        team = MessageEnvelopeTestHelper.createTeam();
        submission = MessageEnvelopeTestHelper.saveNewSubmission(submissionStatusRepository, submissionRepository, team);
        sample = MessageEnvelopeTestHelper.createSamples(submission, team, 1).get(0);
    }

    @Test
    public void whenSampleExistWithGivenAccessionInRepo_ThenReturnsCorrectSample() {
        String existingAccessionIDInRepo = sample.getAccession();

        Mockito.when(sampleRepository.findByAccession(existingAccessionIDInRepo))
                .thenReturn(sample);

        assertThat(submittableFinderService.findSampleByAccession(existingAccessionIDInRepo), is(equalTo(sample)));
    }

    @Test
    public void whenSampleExistWithGivenAccessionInArchiveStorage_ThenReturnsCorrectSample() throws Exception {
        String existingAccessionIDInArchiveStorage = sample.getAccession();

        Mockito.when(sampleRepository.findByAccession(existingAccessionIDInArchiveStorage))
                .thenReturn(null);

        Mockito.when(restTemplate.getForEntity(anyString(), anyObject()))
                .thenReturn(new ResponseEntity<>(
                        objectMapper.writeValueAsString(sample), HttpStatus.OK));

        assertThat(submittableFinderService.findSampleByAccession(existingAccessionIDInArchiveStorage), is(equalTo(sample)));
    }

    @Test
    public void whenSampleWithGivenAccessionNotExistInRepoOrArchiveStorage_ThenReturnsNullSample() throws Exception {
        String notExistingAccessionID = sample.getAccession() + "_NOT_EXISTS";

        Mockito.when(sampleRepository.findByAccession(notExistingAccessionID))
                .thenReturn(null);

        Mockito.when(restTemplate.getForEntity(anyString(), anyObject()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThat(submittableFinderService.findSampleByAccession(notExistingAccessionID), is(nullValue()));
    }
}