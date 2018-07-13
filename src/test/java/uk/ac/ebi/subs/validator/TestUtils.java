package uk.ac.ebi.subs.validator;

import uk.ac.ebi.subs.data.component.File;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.fileupload.FileStatus;
import uk.ac.ebi.subs.data.submittable.Project;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.repository.model.AssayData;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.util.BlankValidationResultMaps;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by karoly on 18/07/2017.
 */
public final class TestUtils {

    private static Team testTeam = Team.build("testTeam");

    public static Sample createSample() {
        Sample sample = new Sample();
        String id = UUID.randomUUID().toString();
        sample.setId("TEST_SAMPLE_" + id);
        sample.setTaxon("testTaxon_" + id);
        sample.setTaxonId(1234L);
        sample.setAccession("ABC_" + id);
        sample.setAlias("TestAlias_" + id);
        sample.setDescription("Description for sample with id: " + id);
        sample.setTeam(testTeam);

        return sample;
    }

    public static Sample createStaticSample() {
        return createStaticSampleWithReleaseDate(LocalDate.of(2018,1,1));
    }

    public static Sample createStaticSampleWithReleaseDate(LocalDate date) {
        Sample sample = new Sample();
        sample.setId("test-sample-id");
        sample.setTaxon("test-taxonomy");
        sample.setTaxonId(9606L);
        sample.setAccession("ABC12345");
        sample.setAlias("test-alias");
        sample.setDescription("Description for a test sample.");
        sample.setTeam(testTeam);
        sample.setReleaseDate(date);
        return sample;
    }

    public static ValidationResult createValidationResult(String submittableUuid) {
        ValidationResult validationResult = new ValidationResult();
        validationResult.setUuid(UUID.randomUUID().toString());
        validationResult.setSubmissionId("Test_submissionId");
        validationResult.setEntityUuid(submittableUuid);

        return validationResult;
    }

    public static Project createProject() {
        Project project = new Project();
        String id = UUID.randomUUID().toString();
        project.setId("TEST_PROJECT_" + id);
        project.setReleaseDate(LocalDate.now());
        return project;
    }

    public static AssayData createAssayData(String assayDataId, Submission submission, List<File> files) {
        AssayData assayData = new AssayData();
        assayData.setId(assayDataId);
        assayData.setSubmission(submission);
        assayData.setFiles(files);

        return assayData;
    }

    public static uk.ac.ebi.subs.repository.model.fileupload.File createFile() {
        uk.ac.ebi.subs.repository.model.fileupload.File file = new uk.ac.ebi.subs.repository.model.fileupload.File();
        file.setId(UUID.randomUUID().toString());
        file.setStatus(FileStatus.READY_FOR_ARCHIVE);
        file.setFilename("test_1.fastq.gz");
        file.setChecksum("12345678901234567890abcdeabcde12");
        file.setSubmissionId("Test_submissionId");

        return file;
    }

    public static ValidationResult createValidationResultForFileWithExistingFileContentAndFileReferenceValidationResults(String fileUUID) {
        ValidationResult validationResult = createValidationResult(fileUUID);

        Map<ValidationAuthor, List<SingleValidationResult>> expectedResults = BlankValidationResultMaps.forFile();

        SingleValidationResult fileContentValidationResult1 =
                buildSingleValidationResult(fileUUID, ValidationAuthor.FileContent, "This is an error message");
        SingleValidationResult fileContentValidationResult2 =
                buildSingleValidationResult(fileUUID, ValidationAuthor.FileContent, "This is another error message");

        List<SingleValidationResult> fileContentValidationResults =
                Arrays.asList(fileContentValidationResult1, fileContentValidationResult2);
        expectedResults.put(ValidationAuthor.FileContent, fileContentValidationResults);

        SingleValidationResult fileReferenceValidationResult1 =
                buildSingleValidationResult(fileUUID, ValidationAuthor.FileReference, "This is a file reference error message");

        List<SingleValidationResult> fileReferenceValidationResults =
                Collections.singletonList(fileReferenceValidationResult1);
        expectedResults.put(ValidationAuthor.FileReference, fileReferenceValidationResults);

        validationResult.setExpectedResults(expectedResults);

        return validationResult;
    }

    private static SingleValidationResult buildSingleValidationResult(String entityUUID, ValidationAuthor validationAuthor,
                                                                      String errorMessage) {
        SingleValidationResult fileContentValidationResult1 = new SingleValidationResult();
        fileContentValidationResult1.setEntityUuid(entityUUID);
        fileContentValidationResult1.setValidationAuthor(validationAuthor);
        fileContentValidationResult1.setValidationStatus(SingleValidationResultStatus.Error);
        fileContentValidationResult1.setMessage(errorMessage);
        return fileContentValidationResult1;
    }
}
