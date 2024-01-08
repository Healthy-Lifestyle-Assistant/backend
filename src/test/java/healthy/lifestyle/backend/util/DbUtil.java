package healthy.lifestyle.backend.util;

import static java.util.Objects.isNull;

import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.repository.CountryRepository;
import healthy.lifestyle.backend.users.repository.RoleRepository;
import healthy.lifestyle.backend.users.repository.UserRepository;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.model.Workout;
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
import healthy.lifestyle.backend.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import healthy.lifestyle.backend.workout.repository.WorkoutRepository;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@TestComponent
public class DbUtil implements Util {
    @Autowired
    BodyPartRepository bodyPartRepository;

    @Autowired
    HttpRefRepository httpRefRepository;

    @Autowired
    ExerciseRepository exerciseRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    WorkoutRepository workoutRepository;

    @Autowired
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Transactional
    public void deleteAll() {
        workoutRepository.deleteAll();
        exerciseRepository.deleteAll();
        bodyPartRepository.deleteAll();
        httpRefRepository.deleteAll();
        userRepository.deleteAll();
        countryRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Override
    public BodyPart createBodyPart(int seed) {
        return bodyPartRepository.save(BodyPart.builder().name("Name " + seed).build());
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
        userRepository.save(user);
        return httpRef;
    }

    private HttpRef createHttpRefBase(int seed, boolean isCustom, User user) {
        return httpRefRepository.save(HttpRef.builder()
                .name("Name " + seed)
                .ref("Ref " + seed)
                .description("Desc " + seed)
                .isCustom(isCustom)
                .user(user)
                .build());
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
        userRepository.save(user);
        return exercise;
    }

    private Exercise createExerciseBase(
            int seed,
            boolean isCustom,
            boolean needsEquipment,
            List<BodyPart> bodyParts,
            List<HttpRef> httpRefs,
            User user) {
        Exercise exercise = Exercise.builder()
                .title("Exercise " + seed)
                .description("Desc " + seed)
                .isCustom(isCustom)
                .needsEquipment(needsEquipment)
                .user(user)
                .bodyParts(new HashSet<>(bodyParts))
                .httpRefs(new HashSet<>(httpRefs))
                .build();
        return exerciseRepository.save(exercise);
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
        userRepository.save(user);
        return workout;
    }

    private Workout createWorkoutBase(int seed, boolean isCustom, List<Exercise> exercises, User user) {
        Workout workout = Workout.builder()
                .title("Workout " + seed)
                .description("Desc " + seed)
                .isCustom(isCustom)
                .user(user)
                .exercises(new HashSet<>(exercises))
                .build();
        return workoutRepository.save(workout);
    }

    @Override
    public User createUser(int seed) {
        Role role = roleRepository.save(Role.builder().name("ROLE_USER").build());
        Country country =
                countryRepository.save(Country.builder().name("Country-" + seed).build());
        return this.createUserBase(seed, role, country, null, null, null, null);
    }

    @Override
    public User createAdminUser(int seed) {
        Role role = roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
        Country country =
                countryRepository.save(Country.builder().name("Country-" + seed).build());
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
        User user = User.builder()
                .username("Username-" + seed)
                .email("email-" + seed + "@email.com")
                .fullName("Full Name " + Shared.numberToText(seed))
                .role(role)
                .country(country)
                .age(isNull(age) ? AGE_CONST + seed : age)
                .password(passwordEncoder().encode("Password-" + seed))
                .httpRefs(isNull(httpRefs) ? null : new HashSet<>(httpRefs))
                .exercises(isNull(exercises) ? null : new HashSet<>(exercises))
                .workouts(isNull(workouts) ? null : new HashSet<>(workouts))
                .build();
        return userRepository.save(user);
    }

    @Override
    public Role createUserRole() {
        return this.createRoleBase("USER");
    }

    @Override
    public Role createAdminRole() {
        return this.createRoleBase("ADMIN");
    }

    private Role createRoleBase(String role) {
        return roleRepository.save(Role.builder().name("ROLE_" + role).build());
    }

    @Override
    public Country createCountry(int seed) {
        return countryRepository.save(Country.builder().name("Country " + seed).build());
    }

    public boolean httpRefsExistByIds(List<Long> ids) {
        return !httpRefRepository.findAllById(ids).isEmpty();
    }

    public boolean exercisesExistByIds(List<Long> ids) {
        return !exerciseRepository.findAllById(ids).isEmpty();
    }

    public boolean workoutsExistByIds(List<Long> ids) {
        return !workoutRepository.findAllById(ids).isEmpty();
    }

    public boolean userExistsById(long userId) {
        return userRepository.findById(userId).isPresent();
    }

    public void saveUserChanges(User user) {
        userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.getReferenceById(id);
    }

    public BodyPart getBodyPartById(long id) {
        return bodyPartRepository.findById(id).orElse(null);
    }

    public HttpRef getHttpRefById(long id) {
        return httpRefRepository.findById(id).orElse(null);
    }

    public Exercise getExerciseById(long id) {
        return exerciseRepository.findById(id).orElse(null);
    }

    public Workout getWorkoutById(long id) {
        return workoutRepository.findById(id).orElse(null);
    }
}
