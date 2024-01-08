package healthy.lifestyle.backend.util;

import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.model.Workout;
import java.util.List;

public interface Util {
    BodyPart createBodyPart(int seed);

    HttpRef createDefaultHttpRef(int seed);

    HttpRef createCustomHttpRef(int seed, User user);

    Exercise createDefaultExercise(int seed, boolean needsEquipment, List<BodyPart> bodyParts, List<HttpRef> httpRefs);

    Exercise createCustomExercise(
            int seed, boolean needsEquipment, List<BodyPart> bodyParts, List<HttpRef> httpRefs, User user);

    Workout createDefaultWorkout(int seed, List<Exercise> exercises);

    Workout createCustomWorkout(int seed, List<Exercise> exercises, User user);

    User createUser(int seed);

    User createAdminUser(int seed);

    User createUser(int seed, Role role, Country country);

    User createUser(int seed, Role role, Country country, int age);

    Role createUserRole();

    Role createAdminRole();

    Country createCountry(int seed);
}
