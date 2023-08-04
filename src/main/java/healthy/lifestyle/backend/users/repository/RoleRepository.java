package healthy.lifestyle.backend.users.repository;

import healthy.lifestyle.backend.users.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {}