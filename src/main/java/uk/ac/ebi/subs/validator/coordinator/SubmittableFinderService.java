package uk.ac.ebi.subs.validator.coordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.subs.data.component.SampleRelationship;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubmittableFinderService {

    @NonNull
    private SampleRepository sampleRepository;
    @NonNull
    private RestTemplate restTemplate;
    @NonNull
    private ObjectMapper objectMapper;

    @Value("${biosamples.client.uri}")
    private String sampleStorageURI;

    Sample findSampleByAccession(String accessionID) {
        Sample sample = sampleRepository.findByAccession(accessionID);

        if (sample == null) {
            try {
                final ResponseEntity<String> response =
                        restTemplate.getForEntity(sampleStorageURI + "samples/" + accessionID, String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    sample = objectMapper.readValue(response.getBody(), Sample.class);
                }
            } catch (IOException e) {
                log.error("Processing sample search from Sample storage repository has failed with sample ID: {}", accessionID);
            } catch (RestClientException rce) {
                log.error("Getting sample information from Sample storage repository resulted with an error.");
                log.error("Error message: " + rce.getMessage());
            }

        }

        return sample;
    }

    Sample findSampleByTeamNameAndAlias(SampleRelationship sampleRelationship) {
        return sampleRepository.findFirstByTeamNameAndAliasOrderByCreatedDateDesc(
                sampleRelationship.getTeam(), sampleRelationship.getAlias());
    }
}
