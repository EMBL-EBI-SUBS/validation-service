package uk.ac.ebi.subs.validator.coordinator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackageClasses = ValidationResultRepository.class)
@EnableAutoConfiguration
@SpringBootTest(classes = ValidationResultService.class)
public class ValidationResultServiceTest {

    public static final String SUBMISSION_ID = "123";

    @Autowired
    ValidationResultRepository repository;

    @Autowired
    ValidationResultService service;

    @Before
    public void setUp() {
        repository.deleteAll();
    }

    @Test
    public void getVersionTest() {
        ValidationResult validationResult = new ValidationResult();
        Sample sample = createSampe();

        for (int i = 0; i < 5; i++) {
            validationResult = service.generateValidationResultDocument(sample, SUBMISSION_ID);
        }

        Assert.assertEquals(4, validationResult.getVersion());
    }

    private Sample createSampe() {
        Sample sample = new Sample();
        sample.setId(UUID.randomUUID().toString());

        return sample;
    }
}
