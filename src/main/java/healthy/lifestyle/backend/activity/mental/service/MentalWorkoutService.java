package healthy.lifestyle.backend.activity.mental.service;

import healthy.lifestyle.backend.activity.mental.dto.MentalWorkoutCreateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalWorkoutResponseDto;
import org.springframework.data.domain.Page;

public interface MentalWorkoutService {

    MentalWorkoutResponseDto createCustomMentalWorkout(long userId, MentalWorkoutCreateRequestDto requestDto);

    MentalWorkoutResponseDto getMentalWorkoutById(long mentalWorkoutId, boolean requiredDefault, Long userId);

    Page<MentalWorkoutResponseDto> getMentalWorkoutsWithFilters(
            Boolean isCustom,
            Long userId,
            String title,
            String description,
            Long mentalTypeId,
            String sortField,
            String sortDirection,
            int currentPageNumber,
            int pageSize);
}
