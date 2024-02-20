package healthy.lifestyle.backend.user.validation.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class FieldsValuesMatchOptionalValidator
        implements ConstraintValidator<FieldsValuesMatchOptionalValidation, Object> {
    private String field;
    private String fieldMatch;

    @Override
    public void initialize(FieldsValuesMatchOptionalValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        this.field = constraintAnnotation.field();
        this.fieldMatch = constraintAnnotation.fieldMatch();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Object fieldValue = new BeanWrapperImpl(value).getPropertyValue(field);
        Object fieldMatchValue = new BeanWrapperImpl(value).getPropertyValue(fieldMatch);
        if (fieldValue == null && fieldMatchValue == null) return true;
        if (fieldValue != null && fieldMatchValue != null) return fieldValue.equals(fieldMatchValue);
        return false;
    }
}
