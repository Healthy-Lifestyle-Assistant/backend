package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.workout.dto.CreateExerciseRequestDto;
import healthy.lifestyle.backend.workout.dto.CreateExerciseResponseDto;

public interface ExerciseService {
    CreateExerciseResponseDto createExercise(CreateExerciseRequestDto requestDto, Long userId);
}