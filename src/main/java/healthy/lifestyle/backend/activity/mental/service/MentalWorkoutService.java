package healthy.lifestyle.backend.activity.mental.service;

import healthy.lifestyle.backend.activity.mental.dto.MentalWorkoutCreateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalWorkoutResponseDto;
import org.springframework.data.domain.Page;

public interface MentalWorkoutService {

    MentalWorkoutResponseDto createCustomMentalWorkout(long userId, MentalWorkoutCreateRequestDto requestDto);

    Page<MentalWorkoutResponseDto> getMentalWorkouts(
            Long userId, String sortField, String sortDirection, int currentPageNumber, int pageSize);
}