package healthy.lifestyle.backend.mentals.service;

import healthy.lifestyle.backend.mentals.dto.MentalResponseDto;
import java.util.List;
import org.springframework.data.domain.Page;

public interface MentalService {
    MentalResponseDto getMentalById(long mentalId, boolean requiredDefault, Long userId);

    Page<MentalResponseDto> getMentalWithFilter(
            Boolean isCustom,
            Long userId,
            String title,
            String description,
            Long mentalTypeId,
            String sortField,
            String sortDirection,
            int currentPageNumber,
            int pageSize);

    List<MentalResponseDto> getDefaultMentals();

    List<MentalResponseDto> getCustomMentals(long userId);
}
