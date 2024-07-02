package healthy.lifestyle.backend.activity.mental.repository;

import healthy.lifestyle.backend.activity.mental.model.MentalWorkout;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MentalWorkoutRepository extends JpaRepository<MentalWorkout, Long> {

    @Query("SELECT w FROM MentalWorkout w WHERE (w.title = :title AND w.isCustom = true AND w.user.id = :userId) "
            + "OR (w.title = :title AND w.isCustom = false)")
    List<MentalWorkout> findDefaultAndCustomByTitleAndUserId(String title, Long userId);

    @Query("SELECT DISTINCT w FROM MentalWorkout w JOIN w.mentalActivities.type tp WHERE tp.id IN :mentalTypeId "
            + "AND (w.isCustom = false OR (w.isCustom = true AND w.user.id = :userId)) "
            + "AND (:title IS NULL OR w.title ILIKE %:title%) "
            + "AND (:description IS NULL OR w.description ILIKE %:description%)")
    Page<MentalWorkout> findDefaultAndCustomMentalWorkoutsWithFilter(
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("description") String description,
            @Param("mentalTypeId") Long mentalTypeId,
            Pageable pageable);

    @Query("SELECT DISTINCT w FROM MentalWorkout w  JOIN w.mentalActivities.type tp WHERE tp.id IN :mentalTypeId "
            + "AND (:userId IS NULL OR w.user.id = :userId) AND w.isCustom = :isCustom "
            + "AND (:title IS NULL OR w.title ILIKE %:title%) "
            + "AND (:description IS NULL OR w.description ILIKE %:description%)")
    Page<MentalWorkout> findDefaultOrCustomMentalWorkoutsWithFilter(
            @Param("isCustom") boolean isCustom,
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("description") String description,
            @Param("mentalTypeId") Long mentalTypeId,
            Pageable pageable);
}
