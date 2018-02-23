/*
 *  Copyright 2016 PaaSword Framework, http://www.paasword.eu/
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.paasword.repository.relational.dao;

import eu.paasword.repository.relational.domain.ApplicationInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 * @author smantzouratos
 */
@Repository
@Transactional
public interface ApplicationInstanceRepository extends JpaRepository<ApplicationInstance, Long> {

    /**
     *
     * @param name
     * @return
     */
    public ApplicationInstance findByName(String name);

    @Query("select new eu.paasword.repository.relational.domain.ApplicationInstance(a.id, a.name, a.uniqueID, a.validator, a.description, a.encryptionAlgorithm, a.privacyConstraintsSet, a.affinityConstraintsSet, a.locationConstraints, a.fragmentationSchema, a.configurationFile, a.lastModified, a.dateCreated, a.iaasProviders, a.iaasProviderInstances, a.runningEndpointURL, a.gitURL, a.paaSProviderID, a.overallStatus, a.deploymentType, a.dbProxyDeploymentType) from ApplicationInstance a where a.uniqueID = ?1")
    public ApplicationInstance findByUniqueID(String uniqueID);

    public ApplicationInstance findByValidator(String validator);

    @Query("select new eu.paasword.repository.relational.domain.ApplicationInstance(a.id, a.name, a.uniqueID, a.validator, a.description, a.encryptionAlgorithm, a.privacyConstraintsSet, a.affinityConstraintsSet, a.locationConstraints, a.fragmentationSchema, a.configurationFile, a.lastModified, a.dateCreated, a.iaasProviders, a.iaasProviderInstances, a.runningEndpointURL, a.gitURL, a.overallStatus, a.deploymentType, a.dbProxyDeploymentType) from ApplicationInstance a where a.id = ?1")
    public ApplicationInstance findOneWithoutApplication(long applicationInstanceID);

    @Query("select new eu.paasword.repository.relational.domain.ApplicationInstance(a.id, a.name, a.uniqueID, a.validator, a.description, a.encryptionAlgorithm, a.privacyConstraintsSet, a.affinityConstraintsSet, a.locationConstraints, a.fragmentationSchema, a.configurationFile, a.lastModified, a.dateCreated, a.iaasProviders, a.iaasProviderInstances, a.runningEndpointURL, a.gitURL, a.paaSProviderID, a.overallStatus, a.deploymentType, a.dbProxyDeploymentType) from ApplicationInstance a where a.id = ?1")
    public ApplicationInstance findOneWithPaaSProviderWithoutApplication(long applicationInstanceID);

    @Query("select new eu.paasword.repository.relational.domain.ApplicationInstance(a.id, a.name, a.uniqueID, a.validator, a.description, a.encryptionAlgorithm, a.privacyConstraintsSet, a.affinityConstraintsSet, a.locationConstraints, a.fragmentationSchema, a.configurationFile, a.lastModified, a.dateCreated, a.iaasProviders, a.iaasProviderInstances, a.runningEndpointURL, a.gitURL, a.overallStatus, a.deploymentType, a.dbProxyDeploymentType) from ApplicationInstance a where a.applicationID.id = ?1")
    public ApplicationInstance findByApplicationID(long applicationID);

    @Query("select new eu.paasword.repository.relational.domain.ApplicationInstance(a.id, a.name, a.uniqueID, a.validator, a.description, a.encryptionAlgorithm, a.privacyConstraintsSet, a.affinityConstraintsSet, a.locationConstraints, a.fragmentationSchema, a.configurationFile, a.lastModified, a.dateCreated, a.iaasProviders, a.iaasProviderInstances, a.runningEndpointURL, a.gitURL, a.paaSProviderID, a.overallStatus, a.deploymentType, a.dbProxyDeploymentType) from ApplicationInstance a where a.applicationID.id = ?1")
    public ApplicationInstance findByApplicationIDWithPaaS(long applicationID);

}
