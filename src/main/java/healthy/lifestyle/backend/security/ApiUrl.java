package healthy.lifestyle.backend.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Service
public class ApiUrl {
    @Value("/error/**")
    private String errorUrl;

    @Value("${api.basePath}/${api.version}/users/auth/**")
    private String authUrl;

    @Value("${api.basePath}/${api.version}/users")
    private String signupUrl;

    @Value("${api.basePath}/${api.version}/workouts/bodyParts")
    private String bodyPartsUrl;

    @Value("${api.basePath}/${api.version}/workouts/exercises/default")
    private String defaultExercisesUrl;

    @Value("${api.basePath}/${api.version}/users/countries")
    private String countriesUrl;

    @Value("${api.basePath}/${api.version}/users/timezones")
    private String timezonesUrl;

    @Value("${api.basePath}/${api.version}/workouts/exercises/default/{exercise_id}")
    private String defaultExerciseDetailsUrl;

    @Value("${api.basePath}/${api.version}/workouts/httpRefs/default")
    private String defaultHttpRefsUrl;

    @Value("${api.basePath}/${api.version}/workouts/default")
    private String defaultWorkoutsUrl;

    @Value("${api.basePath}/${api.version}/workouts/default/{workout_id}")
    private String defaultWorkoutDetailsUrl;

    @Value("${api.basePath}/${api.version}/nutritions/default/{nutrition_id}")
    private String defaultNutritionDetailsUrl;

    @Value("${api.basePath}/${api.version}/mental_activities/default/{mental_activity_id}")
    private String defaultMentalActivityDetailsUrl;

    @Value("${api.basePath}/${api.version}/mental_activities/all_mental_activities")
    private String allMentalActivitiesUrl;

    @Value("${api.basePath}/${api.version}/mental_workouts/default/{mental_workout_id}")
    private String defaultMentalWorkoutsDetailsUrl;

    @Value("${api.basePath}/${api.version}/mental_workouts/default_mental_workouts")
    private String defaultMentalWorkouts;
}
