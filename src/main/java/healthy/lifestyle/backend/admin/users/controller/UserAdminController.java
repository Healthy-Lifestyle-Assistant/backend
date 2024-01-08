package healthy.lifestyle.backend.admin.users.controller;

import healthy.lifestyle.backend.admin.users.service.UserAdminService;
import healthy.lifestyle.backend.users.dto.UserResponseDto;
import healthy.lifestyle.backend.validation.*;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.basePath}/${api.version}/admin")
public class UserAdminController {
    private final UserAdminService adminService;

    public UserAdminController(UserAdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers(
            @RequestParam(name = "role", required = false) Long roleId,
            @RequestParam(name = "username", required = false) @UsernameValidation String username,
            @RequestParam(name = "email", required = false) @EmailValidation String email,
            @RequestParam(name = "fullName", required = false) @FullNameValidation String fullName,
            @RequestParam(name = "country", required = false) Long countryId,
            @RequestParam(name = "age", required = false) @AgeValidation Integer age) {
        return ResponseEntity.ok(adminService.getUsersByFilters(roleId, username, email, fullName, countryId, age));
    }
}
