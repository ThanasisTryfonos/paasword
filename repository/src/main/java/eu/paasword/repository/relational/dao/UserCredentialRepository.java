package eu.paasword.repository.relational.dao;

import eu.paasword.repository.relational.domain.ProxyCloudProvider;
import eu.paasword.repository.relational.domain.User;
import eu.paasword.repository.relational.domain.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Created by smantzouratos on 29/11/2016.
 */
@Repository
@Transactional
public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {

    public List<UserCredential> findByProxyCloudProvider(ProxyCloudProvider proxyCloudProvider);

    @Query("select u from UserCredential u where u.user.id = ?2 and u.proxyCloudProvider.id = ?1")
    public UserCredential findByProxyCloudProviderAndUser(Long proxyCloudProvider, Long userID);

    public List<UserCredential> findByUser(User user);

}
