package uk.ac.ebi.subs.validator.core.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.data.component.StudyDataType;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.validator.core.utils.TestUtils;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
public class StudyTypeValidatorTest {

    private StudyTypeValidator studyTypeValidator = new StudyTypeValidator();
    private Study study;

    @Before
    public void buildUp() {
        study = new Study();
        study.setId("12345");
    }

    @Test
    public void studyTypePresentSoPass() {
        study.setStudyType(StudyDataType.Metabolomics_MS);

        SingleValidationResult result = studyTypeValidator.validate(study);

        assertEquals(SingleValidationResultStatus.Pass, result.getValidationStatus());
        assertNull(result.getMessage());
        assertEquals(study.getId(), result.getEntityUuid());
    }

    @Test
    public void studyTypeAbsentSoFail() {
        study.setStudyType(null);

        SingleValidationResult result = studyTypeValidator.validate(study);

        assertEquals(SingleValidationResultStatus.Error, result.getValidationStatus());
        assertNotNull(result.getMessage());
        assertEquals(study.getId(), result.getEntityUuid());
    }

}
