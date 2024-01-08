package healthy.lifestyle.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FullNameValidator.class)
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FullNameValidation {
    String message() default "Not allowed symbols";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
