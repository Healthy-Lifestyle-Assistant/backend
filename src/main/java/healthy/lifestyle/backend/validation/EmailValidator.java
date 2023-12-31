package healthy.lifestyle.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EmailValidator implements ConstraintValidator<EmailValidation, String> {
    @Override
    public void initialize(EmailValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return validation(value);
    }

    public boolean validation(String input) {
        if (input != null) {
            String trim = input.trim();
            char[] notAllowed = new char[] {
                '!', '#', '$', '%', '^', '&', '*', '(', ')', '+', '=', ',', '<', '>', '?', '\\', '/', '`', '~', ' '
            };
            for (char ch : notAllowed) {
                if (trim.indexOf(ch) != -1) return false;
            }
        }
        return true;
    }
}
