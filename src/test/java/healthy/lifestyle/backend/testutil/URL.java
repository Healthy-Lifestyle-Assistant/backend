package healthy.lifestyle.backend.testutil;

public class URL {
    public static final String LOGIN = "/api/v1/users/auth/login";

    public static final String VALIDATE = "/api/v1/users/auth/validate";

    public static final String USERS = "/api/v1/users";

    public static final String USER_ID = "/api/v1/users/{userId}";

    public static final String COUNTRIES = "/api/v1/users/countries";
    public static final String TIMEZONES = "/api/v1/users/timezones";

    public static final String BODY_PARTS = "/api/v1/workouts/bodyParts";

    public static final String CUSTOM_HTTP_REFS = "/api/v1/workouts/httpRefs";

    public static final String DEFAULT_HTTP_REFS = "/api/v1/workouts/httpRefs/default";

    public static final String CUSTOM_HTTP_REF_ID = "/api/v1/workouts/httpRefs/{httpRefId}";

    public static final String DEFAULT_HTTP_REF_ID = "/api/v1/workouts/httpRefs/default/{httpRefId}";

    public static final String CUSTOM_EXERCISES = "/api/v1/workouts/exercises";

    public static final String DEFAULT_EXERCISES = "/api/v1/workouts/exercises/default";

    public static final String CUSTOM_EXERCISE_ID = "/api/v1/workouts/exercises/{exerciseId}";

    public static final String DEFAULT_EXERCISE_ID = "/api/v1/workouts/exercises/default/{exerciseId}";

    public static final String ADMIN_EXERCISES = "/api/v1/admin/exercises";

    public static final String CUSTOM_WORKOUTS = "/api/v1/workouts";

    public static final String DEFAULT_WORKOUTS = "/api/v1/workouts/default";

    public static final String CUSTOM_WORKOUT_ID = "/api/v1/workouts/{workoutId}";

    public static final String DEFAULT_WORKOUT_ID = "/api/v1/workouts/default/{workoutId}";

    public static final String ADMIN_USERS = "/api/v1/admin/users";

    public static final String DEFAULT_MENTAL_ID = "/api/v1/mental_activities/default/{mental_activity_id}";

    public static final String DEFAULT_NUTRITION_ID = "/api/v1/nutritions/default/{nutrition_id}";

    public static final String CUSTOM_MENTAL_ID = "/api/v1/mental_activities/{mental_activity_id}";

    public static final String ALL_MENTALS = "/api/v1/mental_activities/all_mental_activities";

    public static final String CUSTOM_MENTALS = "/api/v1/mental_activities";

    public static final String CUSTOM_MENTAL_WORKOUTS = "/api/v1/mental_workouts";

    public static final String DEFAULT_MENTAL_WORKOUTS_LIST = "/api/v1/mental_workouts/default_mental_workouts";

    public static final String WORKOUT_PLANS = "/api/v1/calendar/workouts/plans";

    public static final String DEFAULT_MENTAL_WORKOUT_ID = "/api/v1/mental_workouts/default/{mental_workout_id}";

    public static final String CUSTOM_MENTAL_WORKOUT_ID = "/api/v1/mental_workouts/{mental_workout_id}";
}
