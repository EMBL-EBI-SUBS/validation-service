package uk.ac.ebi.subs.validator.filereference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.repository.model.AssayData;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.model.fileupload.File;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.fileupload.FileRepository;
import uk.ac.ebi.subs.repository.repos.submittables.AnalysisRepository;
import uk.ac.ebi.subs.repository.repos.submittables.AssayDataRepository;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.ac.ebi.subs.validator.TestUtils.createAssayData;

@RunWith(SpringRunner.class)
public class FileReferenceValidatorTest {

    private static final CharSequence PATH_SEPARATOR = System.getProperty("file.separator");
    @MockBean
    private FileRepository fileRepository;

    @MockBean
    private AssayDataRepository assayDataRepository;

    @MockBean
    private AnalysisRepository analysisRepository;

    @MockBean
    private SubmissionRepository submissionRepository;

    private FileReferenceValidator fileReferenceValidator;

    private final String SUBMISSION_ID = "abcdefgh-1234-abcd-1234-1234567890ab";
    private final String[] ASSAYDATA_IDS = {"assayDataId1", "assayDataId2"};
    private final String[] FILE_IDS = {"fileId1", "fileId2"};
    private final String[] FILENAMES = {"testfile1", "testfile2"};
    private final String TARGET_BASE_PATH = "/target/base/path";

    private List<File> uploadedFiles;
    private File uploadedFile1;
    private File uploadedFile2;
    private List<AssayData> assayDataList;
    private List<AssayData> assayDataListWithFiles;
    private AssayData assayDataWithoutFile1;
    private AssayData assayDataWithoutFile2;
    private AssayData assayDataWithFile1;
    private AssayData assayDataWithFile2;

    private List<uk.ac.ebi.subs.data.component.File> emptyFileList = Collections.emptyList();

    @Before
    public void setup() {
        Submission submission = submissionRepository.findById(SUBMISSION_ID).orElse(null);
        uploadedFile1 = createFile(FILE_IDS[0], FILENAMES[0], TARGET_BASE_PATH);
        uploadedFile2 = createFile(FILE_IDS[1], FILENAMES[1], TARGET_BASE_PATH);
        final uk.ac.ebi.subs.data.component.File fileMetaData1 = createFileMetadata(FILENAMES[0], TARGET_BASE_PATH);
        final uk.ac.ebi.subs.data.component.File fileMetaData2 = createFileMetadata(FILENAMES[1], TARGET_BASE_PATH);

        uploadedFiles = Arrays.asList(uploadedFile1, uploadedFile2);
        assayDataWithoutFile1 = createAssayData(ASSAYDATA_IDS[0], submission, emptyFileList);
        assayDataWithoutFile2 = createAssayData(ASSAYDATA_IDS[1], submission, emptyFileList);

        assayDataWithFile1 = createAssayData(ASSAYDATA_IDS[0], submission, Collections.singletonList(fileMetaData1));
        assayDataWithFile2 = createAssayData(ASSAYDATA_IDS[1], submission, Collections.singletonList(fileMetaData2));

        assayDataList = Arrays.asList( assayDataWithoutFile1, assayDataWithoutFile2 );
        assayDataListWithFiles = Arrays.asList( assayDataWithFile1, assayDataWithFile2 );

        fileReferenceValidator = new FileReferenceValidator(fileRepository, assayDataRepository, analysisRepository);
    }

    @Test
    public void whenStorageFileNotReferencedInFileMetadata_ThenUploadedFileValidationShouldFail() {
        SingleValidationResult singleValidationResultTestFile1 = createSingleValidationResult(
                FILE_IDS[0], SingleValidationResultStatus.Error, String.format(FileReferenceValidator.STORED_FILE_NOT_REFERENCED, FILENAMES[0])
        );

        given(this.fileRepository.findBySubmissionId(SUBMISSION_ID))
                .willReturn(uploadedFiles);

        given(this.assayDataRepository.findBySubmissionId(SUBMISSION_ID))
                .willReturn(assayDataList);

        List<SingleValidationResult> validationResults = fileReferenceValidator.validate(uploadedFile1);

        assertThat(validationResults.size(), is(equalTo(1)));

        assertThat(validationResults, hasItem(singleValidationResultTestFile1));
    }

    @Test
    public void whenThereAreUploadedFileButAssayDataHasNotGotAnyFileReferences_ThenAssayDataValidationShouldPass() {
        SingleValidationResult singleValidationResultAssayData1 = createSingleValidationResult(
                ASSAYDATA_IDS[0], SingleValidationResultStatus.Pass, FileReferenceValidator.SUCCESS_FILE_VALIDATION_MESSAGE_SUBMITTABLE
        );

        given(this.fileRepository.findBySubmissionId(SUBMISSION_ID))
                .willReturn(uploadedFiles);

        given(this.assayDataRepository.findBySubmissionId(SUBMISSION_ID))
                .willReturn(assayDataList);

        List<SingleValidationResult> validationResults = fileReferenceValidator.validate(assayDataWithoutFile1, SUBMISSION_ID, assayDataWithoutFile1.getId());

        assertThat(validationResults.size(), is(equalTo(1)));

        assertThat(validationResults, hasItem(singleValidationResultAssayData1));
    }

    @Test
    public void whenFileMetadataReferenceAFileThatIsNotInStorage_ThenAssayDataValidationShouldFail() {
        SingleValidationResult singleValidationResultAssayData1 = createSingleValidationResult(
                ASSAYDATA_IDS[0], SingleValidationResultStatus.Error,
                String.format(FileReferenceValidator.FILE_METADATA_NOT_EXISTS_AS_UPLOADED_FILE, FILENAMES[0])
        );

        given(this.fileRepository.findBySubmissionId(SUBMISSION_ID))
                .willReturn(Collections.emptyList());

        given(this.assayDataRepository.findBySubmissionId(SUBMISSION_ID))
                .willReturn(assayDataListWithFiles);

        List<SingleValidationResult> validationResults = fileReferenceValidator.validate(assayDataWithFile1, SUBMISSION_ID, assayDataWithFile1.getId());

        assertThat(validationResults.size(), is(equalTo(1)));

        assertThat(validationResults, hasItem(singleValidationResultAssayData1));
    }

    @Test
    public void whenAllStorageFilesReferenced_And_AllFileMetadataReferenceAFileInStorage_ThenUploadedFileValidationShouldSucceed() {
        SingleValidationResult singleValidationResultTestFile1 = createSingleValidationResult(
                FILE_IDS[0], SingleValidationResultStatus.Pass, FileReferenceValidator.SUCCESS_FILE_VALIDATION_MESSAGE_UPLOADED_FILE
        );

        given(this.fileRepository.findBySubmissionId(SUBMISSION_ID))
                .willReturn(uploadedFiles);

        given(this.assayDataRepository.findBySubmissionId(SUBMISSION_ID))
                .willReturn(assayDataListWithFiles);

        List<SingleValidationResult> validationResults = fileReferenceValidator.validate(uploadedFile1);

        assertThat(validationResults.size(), is(equalTo(1)));

        assertThat(validationResults, hasItem(singleValidationResultTestFile1));
    }

    @Test
    public void whenAllStorageFilesReferenced_And_AllFileMetadataReferenceAFileInStorage_ThenAssayDataValidationShouldSucceed() {
        SingleValidationResult singleValidationResultAssayData2 = createSingleValidationResult(
                ASSAYDATA_IDS[1], SingleValidationResultStatus.Pass, FileReferenceValidator.SUCCESS_FILE_VALIDATION_MESSAGE_SUBMITTABLE
        );

        given(this.fileRepository.findBySubmissionId(SUBMISSION_ID))
                .willReturn(uploadedFiles);

        given(this.assayDataRepository.findBySubmissionId(SUBMISSION_ID))
                .willReturn(assayDataListWithFiles);

        List<SingleValidationResult> validationResults = fileReferenceValidator.validate(assayDataWithFile2, SUBMISSION_ID, assayDataWithFile2.getId());

        assertThat(validationResults.size(), is(equalTo(1)));

        assertThat(validationResults, hasItem(singleValidationResultAssayData2));
    }

    private File createFile(String fileId, String filename, String targetPathBase) {
        File file = new File();
        file.setId(fileId);
        file.setSubmissionId(SUBMISSION_ID);
        file.setFilename(filename);
        file.setTargetPath(String.join(
                PATH_SEPARATOR, targetPathBase,
                buildBaseFilePathBySubmissionID(SUBMISSION_ID), filename));

        return file;
    }

    private uk.ac.ebi.subs.data.component.File createFileMetadata(String filename, String targetPathBase) {
        uk.ac.ebi.subs.data.component.File file = new uk.ac.ebi.subs.data.component.File();
        file.setName(filename);
        file.setChecksum("1234567890abcdefabcd1234567890ab");

        return file;
    }

    private SingleValidationResult createSingleValidationResult(String entityId, SingleValidationResultStatus status, String message) {
        SingleValidationResult result = new SingleValidationResult(ValidationAuthor.FileReference, entityId);
        result.setValidationStatus(status);
        result.setMessage(message);
        return result;
    }

    private String buildBaseFilePathBySubmissionID(String submissionID) {
        StringBuilder baseFilePath = new StringBuilder();
        baseFilePath.append(submissionID.substring(0, 1));
        baseFilePath.append(PATH_SEPARATOR);
        baseFilePath.append(submissionID.substring(1, 2));
        baseFilePath.append(PATH_SEPARATOR);
        baseFilePath.append(submissionID);

        return baseFilePath.toString();
    }



}
