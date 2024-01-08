package healthy.lifestyle.backend.util;

import static java.util.Objects.isNull;

import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.dto.*;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.model.Workout;
import java.util.*;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TestUtil implements Util {
    private final ModelMapper modelMapper = new ModelMapper();

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public BodyPart createBodyPart(int seed) {
        return BodyPart.builder().id((long) seed).name("Body part " + seed).build();
    }

    @Override
    public HttpRef createDefaultHttpRef(int seed) {
        return this.createHttpRefBase(seed, false, null);
    }

    @Override
    public HttpRef createCustomHttpRef(int seed, User user) {
        HttpRef httpRef = this.createHttpRefBase(seed, true, user);
        if (user.getHttpRefs() == null) user.setHttpRefs(new HashSet<>());
        user.getHttpRefs().add(httpRef);
        return httpRef;
    }

    private HttpRef createHttpRefBase(int seed, boolean isCustom, User user) {
        return HttpRef.builder()
                .id((long) seed)
                .name("Name " + seed)
                .ref("Ref " + seed)
                .description("Desc " + seed)
                .isCustom(isCustom)
                .user(user)
                .build();
    }

    @Override
    public Exercise createDefaultExercise(
            int seed, boolean needsEquipment, List<BodyPart> bodyParts, List<HttpRef> httpRefs) {
        return this.createExerciseBase(seed, false, needsEquipment, bodyParts, httpRefs, null);
    }

    @Override
    public Exercise createCustomExercise(
            int seed, boolean needsEquipment, List<BodyPart> bodyParts, List<HttpRef> httpRefs, User user) {
        Exercise exercise = this.createExerciseBase(seed, true, needsEquipment, bodyParts, httpRefs, user);
        if (user.getExercises() == null) user.setExercises(new HashSet<>());
        user.getExercises().add(exercise);
        return exercise;
    }

    private Exercise createExerciseBase(
            int seed,
            boolean isCustom,
            boolean needsEquipment,
            List<BodyPart> bodyParts,
            List<HttpRef> httpRefs,
            User user) {
        return Exercise.builder()
                .id((long) seed)
                .title("Exercise " + seed)
                .description("Desc " + seed)
                .isCustom(isCustom)
                .needsEquipment(needsEquipment)
                .user(user)
                .bodyParts(new HashSet<>(bodyParts))
                .httpRefs(new HashSet<>(httpRefs))
                .build();
    }

    @Override
    public Workout createDefaultWorkout(int seed, List<Exercise> exercises) {
        return this.createWorkoutBase(seed, false, exercises, null);
    }

    @Override
    public Workout createCustomWorkout(int seed, List<Exercise> exercises, User user) {
        Workout workout = this.createWorkoutBase(seed, true, exercises, user);
        if (user.getWorkouts() == null) user.setWorkouts(new HashSet<>());
        user.getWorkouts().add(workout);
        return workout;
    }

    private Workout createWorkoutBase(int seed, boolean isCustom, List<Exercise> exercises, User user) {
        return Workout.builder()
                .id((long) seed)
                .title("Workout " + seed)
                .description("Desc " + seed)
                .isCustom(isCustom)
                .user(user)
                .exercises(new HashSet<>(exercises))
                .build();
    }

    @Override
    public User createUser(int seed) {
        Role role = Role.builder().id((long) seed).name("ROLE_USER").build();
        Country country =
                Country.builder().id((long) seed).name("Country-" + seed).build();
        return this.createUserBase(seed, role, country, null, null, null, null);
    }

    @Override
    public User createAdminUser(int seed) {
        Role role = Role.builder().name("ROLE_ADMIN").build();
        Country country = Country.builder().name("Country-" + seed).build();
        return this.createUserBase(seed, role, country, null, null, null, null);
    }

    @Override
    public User createUser(int seed, Role role, Country country) {
        return this.createUserBase(seed, role, country, null, null, null, null);
    }

    @Override
    public User createUser(int seed, Role role, Country country, int age) {
        return this.createUserBase(seed, role, country, age, null, null, null);
    }

    private User createUserBase(
            int seed,
            Role role,
            Country country,
            Integer age,
            List<HttpRef> httpRefs,
            List<Exercise> exercises,
            List<Workout> workouts) {
        int AGE_CONST = 20;
        return User.builder()
                .id((long) seed)
                .username("Username-" + seed)
                .email("email-" + seed + "@email.com")
                .fullName("Full Name " + Shared.numberToText(seed))
                .role(role)
                .country(country)
                .age(age == null ? AGE_CONST + seed : age)
                .password(passwordEncoder.encode("Password-" + seed))
                .httpRefs(isNull(httpRefs) ? null : new HashSet<>(httpRefs))
                .exercises(isNull(exercises) ? null : new HashSet<>(exercises))
                .workouts(isNull(workouts) ? null : new HashSet<>(workouts))
                .build();
    }

    @Override
    public Country createCountry(int seed) {
        return Country.builder().id((long) seed).name("Country " + seed).build();
    }

    @Override
    public Role createUserRole() {
        return this.createRoleBase("USER", null);
    }

    public Role createUserRole(int id) {
        return this.createRoleBase("USER", (long) id);
    }

    @Override
    public Role createAdminRole() {
        return this.createRoleBase("ADMIN", null);
    }

    public Role createAdminRole(int id) {
        return this.createRoleBase("ADMIN", (long) id);
    }

    private Role createRoleBase(String role, Long id) {
        return Role.builder().id(id).name("ROLE_" + role).build();
    }
}
