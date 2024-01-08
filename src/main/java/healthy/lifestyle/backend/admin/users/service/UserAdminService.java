package healthy.lifestyle.backend.admin.users.service;

import healthy.lifestyle.backend.users.dto.UserResponseDto;
import java.util.List;

public interface UserAdminService {
    List<UserResponseDto> getUsersByFilters(
            Long roleId, String username, String email, String fullName, Long countryId, Integer age);
}
