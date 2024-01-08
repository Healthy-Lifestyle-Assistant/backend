package healthy.lifestyle.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AgeValidator.class)
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AgeValidation {
    String message() default "Age should be in range from 16 to 120";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
