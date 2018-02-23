package eu.paasword.repository.relational.dao;

import eu.paasword.repository.relational.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Created by smantzouratos on 29/11/2016.
 */
@Repository
@Transactional
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

}
