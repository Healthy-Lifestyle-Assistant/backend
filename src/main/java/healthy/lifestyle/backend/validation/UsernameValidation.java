package healthy.lifestyle.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UsernameValidator.class)
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UsernameValidation {
    String message() default "Not allowed symbols";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
