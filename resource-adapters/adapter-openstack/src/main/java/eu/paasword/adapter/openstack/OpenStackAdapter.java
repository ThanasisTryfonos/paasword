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
package eu.paasword.adapter.openstack;

import eu.paasword.adapter.openstack.exceptions.IaaSAuthenticationException;
import eu.paasword.adapter.openstack.neutron.NeutronNetwork;
import eu.paasword.adapter.openstack.nova.NovaKeyPair;
import eu.paasword.adapter.openstack.nova.NovaServer;
import eu.paasword.adapter.openstack.util.CloudCfgPaaSword;
import eu.paasword.adapter.openstack.util.KeyPairObj;
import eu.paasword.adapter.openstack.util.KeyPairUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.openstack4j.api.OSClient;
import org.openstack4j.api.exceptions.AuthenticationException;
import org.openstack4j.model.compute.FloatingIP;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.State;
import org.openstack4j.openstack.OSFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.Server.Status;
import org.apache.commons.codec.binary.Base64;
import org.openstack4j.model.network.NetFloatingIP;

/**
 * Created by smantzouratos
 */
public class OpenStackAdapter {

    private static final Logger logger = Logger.getLogger(OpenStackAdapter.class.getName());

    public static void main(String[] args) {

        try {
            testConnectionV3("http://192.168.3.253:5000/v3/", "demo1", "!1q2w3e!", "default", "defaultproject");
        } catch (IaaSAuthenticationException e) {
            e.printStackTrace();
        }

        // openstackName / flavorID / imageID / connectionURL / username / password / tenantName / dbName / dbUsername / dbPassword / keyPairName
//        createNewInstance("227server", "2", "", );
    }

    private static OSClient.OSClientV2 authentication(int IaaSselector) {
        OSClient.OSClientV2 os;
        switch (IaaSselector) {

            case 1:
                // Select IaaS1
                os = OSFactory.builderV2()
                        .endpoint("http://195.46.17.227:5000/v2.0")// Put IP off IaaS2
                        .credentials("admin", "!1q2w3e!")// username passwd
                        .tenantName("demo")
                        .authenticate();
                break;

            case 2:
                // Select IaaS2
                os = OSFactory.builderV2()
                        .endpoint("http://195.46.17.233:5000/v2.0")// Put IP off IaaS2
                        .credentials("admin", "!1q2w3e!")// username passwd
                        .tenantName("demo")
                        .authenticate();
                break;

            default:
                os = null;
                System.out.println("Not a valid Action argument! Please retry");
                break;
        }

        return os;

    }

    public static boolean testConnectionV2(String connectionURL, String username, String password, String tenantName) {

        logger.info("Testing OpenStack connection...");

        boolean isSuccess = false;
        try {

            OSClient.OSClientV2 os = OSFactory.builderV2()
                    .endpoint(connectionURL)
                    .credentials(username, password)
                    .tenantName(tenantName)
                    .authenticate();

            if (null != os.compute().flavors().list() && os.compute().flavors().list().size() > 0) {
                isSuccess = true;
                logger.info("Connection tested successfully!");
            }

        } catch (AuthenticationException e) {
            logger.severe(e.getMessage());
        }

        return isSuccess;
    }

    public static boolean testConnectionV3(String connectionURL, String username, String password, String domainstr, String projectstr) throws IaaSAuthenticationException {

        logger.info("Testing OpenStack connection...");

        boolean isSuccess = false;
        try {

            Identifier domain = Identifier.byName(domainstr);
            Identifier project = Identifier.byName(projectstr);

            OSClient.OSClientV3 os = OSFactory.builderV3()
                    .endpoint(connectionURL)
                    .credentials(username, password, domain).scopeToProject(project, domain).authenticate();

            if (null != os.compute().flavors().list() && os.compute().flavors().list().size() > 0) {
                isSuccess = true;
                logger.info("Connection tested successfully!");
            }

        } catch (AuthenticationException e) {
            logger.severe(e.getMessage());
            throw new IaaSAuthenticationException("Could not authenticate to " + connectionURL);
        }

        return isSuccess;
    }//EoM    

    public static boolean testConnectionV3(IaaS iaas) throws IaaSAuthenticationException {
        logger.info("Testing OpenStack connection...");
        boolean isSuccess = false;
        try {
            Identifier domain = Identifier.byName(iaas.getDomain());
            Identifier project = Identifier.byName(iaas.getProject());

            OSClient.OSClientV3 os = OSFactory.builderV3()
                    .endpoint(iaas.getConnectionURL())
                    .credentials(iaas.getUsername(), iaas.getPassword(), domain).scopeToProject(project, domain).authenticate();
            if (null != os.compute().flavors().list() && os.compute().flavors().list().size() > 0) {
                isSuccess = true;
                logger.info("Connection tested successfully!");
            }
        } catch (AuthenticationException e) {
            logger.severe(e.getMessage());
            throw new IaaSAuthenticationException("Could not authenticate to " + iaas.getConnectionURL() + " " + iaas.getUsername());
        }
        return isSuccess;
    }//EoM    

    public static String createNewInstanceV3WithoutFloating(String instanceName, String flavorID, String imageID, String connectionURL, String username, String password, String domainstr, String projectstr,
                                                            String dbName, String dbUsername, String dbPassword, String keyPairName) {
        logger.info("Creating new Instance...");

        NovaServer newServer = null;
        String serverid = null;
        NeutronNetwork network;

        try {
            Identifier domain = Identifier.byName(domainstr);
            Identifier project = Identifier.byName(projectstr);

            OSClient.OSClientV3 os = OSFactory.builderV3()
                    .endpoint(connectionURL)
                    .credentials(username, password, domain).scopeToProject(project, domain).authenticate();

            if (null != os.compute().flavors().list() && os.compute().flavors().list().size() > 0) {
                newServer = new NovaServer(os);
                network = new NeutronNetwork(os);

                List<String> selectedNets = new ArrayList<>();
                List<? extends Network> netList = network.listNetworksV3();
                logger.info("netList.size() " + netList.size());

                for (Network availableNet : netList) {

                    logger.info("Testing: " + availableNet.getName() + ", State: " + availableNet.getStatus());

                    if (availableNet.getStatus() == State.ACTIVE) {
                        selectedNets.add(availableNet.getId());
                    }

                        //&& (netList.get(i).isAdminStateUp() == Boolean.TRUE)

//                            && (netList.get(i).isShared() == Boolean.FALSE)) {

                }//for

                logger.info("Available Networks.size(): " + selectedNets.size());

                // Create string builder to use with cloud-int
                StringBuilder sb = new StringBuilder();
                sb.append("#!/bin/bash\n\n");
                sb.append("sudo -u postgres bash -c \"psql -c \\\"ALTER ROLE postgres WITH ENCRYPTED PASSWORD '");
                sb.append(dbPassword);
                sb.append("';\\\"\"\n");
                sb.append("sudo -u postgres bash -c \"psql -c \\\"CREATE DATABASE ");
                sb.append(dbName);
                sb.append(";\\\"\"\n");
                sb.append("exit 0");
                CloudCfgPaaSword temp = new CloudCfgPaaSword(sb.toString());
                //
                byte[] encodedBytes = Base64.encodeBase64(temp.getText().getBytes());

                // Generate KeyPairObj for instance
                java.security.KeyPair genKeyPair = KeyPairUtil.generateKeypair();
                KeyPairObj keyPairObj = new KeyPairObj(KeyPairUtil.getPublicKey(genKeyPair), KeyPairUtil.getPrivateKey(genKeyPair), keyPairName);
                NovaKeyPair novaKeypair = new NovaKeyPair(os);
                novaKeypair.createKeypairPreGenPubKeyV3(keyPairObj.getName(), keyPairObj.getPublicKey());

                String pemPath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + keyPairObj.getName() + ".pem";
                File file = new File(pemPath);

                // write the file to a FileOutputStream
                try (OutputStream outputStream = new FileOutputStream(file);) {
                    // if file doesnt exists, then create it
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    // get the content in bytes
                    byte[] contentInBytes = keyPairObj.getPrivateKey().getBytes();
                    outputStream.write(contentInBytes);
                    outputStream.flush();
                    outputStream.close();
                    System.out.println("Done generating PEM file for the Created instance!");
                } catch (IOException e) {
                    e.printStackTrace();

                }
                Server myServer = newServer.createNewServerV3(instanceName,
                        flavorID,
                        imageID,
                        selectedNets,
                        keyPairObj.getName(),
                        new String(encodedBytes)
                );

                serverid = myServer.getId();

                Status stat = Status.BUILD;
                while (stat != Status.ACTIVE) {
                    stat = os.compute().servers().get(serverid).getStatus();
                    //logger.info("stat "+instanceName+": " + stat.toString());
                }//while

                logger.info("Instance created successfully!");
            } // if authenticated

        } catch (AuthenticationException e) {
            logger.severe(e.getMessage());
        }

        return serverid;
    }//EoM

    public static String createNewInstanceV3WithoutFloating(FragServer fragment) {

        String instanceName = fragment.getDeploymentinstanceid() + "" + fragment.getName();
        String flavorID = fragment.getIaas().getFlavorid(); //"1ebf540d-6a2d-490a-8872-6538ba744dde";
        String imageID = fragment.getIaas().getImageid();
        String networkID = fragment.getIaas().getNetworkid();
        String connectionURL = fragment.getIaas().getConnectionURL();
        String username = fragment.getIaas().getUsername();
        String password = fragment.getIaas().getPassword();
        String domainstr = fragment.getIaas().getDomain();
        String projectstr = fragment.getIaas().getProject();
        String dbName = fragment.getName();
        String dbUsername = fragment.getUser();
        String dbPassword = fragment.getPassword();
        String keyPairName = fragment.getDeploymentinstanceid() + "" + fragment.getName();

        logger.info("Creating new Instance...");

        NovaServer newServer = null;
        String serverid = null;
        NeutronNetwork network;

        try {
            Identifier domain = Identifier.byName(domainstr);
            Identifier project = Identifier.byName(projectstr);

            OSClient.OSClientV3 os = OSFactory.builderV3()
                    .endpoint(connectionURL)
                    .credentials(username, password, domain).scopeToProject(project, domain).authenticate();

            if (null != os.compute().flavors().list() && os.compute().flavors().list().size() > 0) {
                newServer = new NovaServer(os);
                network = new NeutronNetwork(os);

                List<String> selectedNets = new ArrayList<>();
                List<? extends Network> netList = network.listNetworksV3();
                logger.info("netList.size() " + netList.size());

                for (Network availableNet : netList) {

                    logger.info("Testing: " + availableNet.getName() + ", State: " + availableNet.getStatus());

                    if (availableNet.getStatus() == State.ACTIVE && availableNet.getId().equals(networkID)) {// availableNet.isAdminStateUp() == Boolean.TRUE && availableNet.isShared() == Boolean.FALSE) {
                        selectedNets.add(availableNet.getId());
                    }

                }//for

                logger.info("Available Networks.size(): " + selectedNets.size());

                // Create string builder to use with cloud-int
                StringBuilder sb = new StringBuilder();
                sb.append("#!/bin/bash\n\n");
                sb.append("sudo -u postgres bash -c \"psql -c \\\"ALTER ROLE postgres WITH ENCRYPTED PASSWORD '");
                sb.append(dbPassword);
                sb.append("';\\\"\"\n");
                sb.append("sudo -u postgres bash -c \"psql -c \\\"CREATE DATABASE ");
                sb.append(dbName);
                sb.append(";\\\"\"\n");
                sb.append("exit 0");
                CloudCfgPaaSword temp = new CloudCfgPaaSword(sb.toString());
                //
                byte[] encodedBytes = Base64.encodeBase64(temp.getText().getBytes());

                // Generate KeyPairObj for instance
                java.security.KeyPair genKeyPair = KeyPairUtil.generateKeypair();
                KeyPairObj keyPairObj = new KeyPairObj(KeyPairUtil.getPublicKey(genKeyPair), KeyPairUtil.getPrivateKey(genKeyPair), keyPairName);
                NovaKeyPair novaKeypair = new NovaKeyPair(os);
                novaKeypair.createKeypairPreGenPubKeyV3(keyPairObj.getName(), keyPairObj.getPublicKey());

                String pemPath = System.getProperty("user.home") + File.separator + keyPairObj.getName() + ".pem";
//                File file = new File(pemPath);
//                 write the file to a FileOutputStream
//                try (OutputStream outputStream = new FileOutputStream(file);) {
//                    // if file doesnt exists, then create it
//                    if (!file.exists()) {
//                        file.createNewFile();
//                    }
//                    // get the content in bytes
//                    byte[] contentInBytes = keyPairObj.getPrivateKey().getBytes();
//                    outputStream.write(contentInBytes);
//                    outputStream.flush();
//                    outputStream.close();
//                    System.out.println("Done generating PEM file for the Created instance!");
//                } catch (IOException e) {
//                    e.printStackTrace();
//
//                }

                Server myServer = newServer.createNewServerV3(instanceName,
                        flavorID,
                        imageID,
                        selectedNets,
                        keyPairObj.getName(),
                        new String(encodedBytes)
                );

                serverid = myServer.getId();

                Status stat = Status.BUILD;
                while (stat != Status.ACTIVE) {
                    stat = os.compute().servers().get(serverid).getStatus();
                    //logger.info("stat "+instanceName+": " + stat.toString());
                }//while

                logger.info("Instance created successfully!");
            } // if authenticated

        } catch (AuthenticationException e) {
            logger.severe(e.getMessage());
        }

        return serverid;
    }//EoM

    public static String assignFloatingToServer(String serverid, IaaS iaas) {

        Identifier domain = Identifier.byName(iaas.getDomain());
        Identifier project = Identifier.byName(iaas.getProject());

        OSClient.OSClientV3 os = OSFactory.builderV3()
                .endpoint(iaas.getConnectionURL())
                .credentials(iaas.getUsername(), iaas.getPassword(), domain).scopeToProject(project, domain).authenticate();

//                Assign floating
        FloatingIP chosenfloatip = null;

        List<? extends FloatingIP> ips = os.compute().floatingIps().list();

        logger.info("Floating IP Size: " + ips.size());

        for (FloatingIP floatip : ips) {

            logger.info("floating: " + floatip + " fixed: " + floatip.getFixedIpAddress());

            if (floatip.getFixedIpAddress() == null) {
                chosenfloatip = floatip;
                break;
            }
        }//for

        logger.info("ChosenFloating for " + serverid + " : " + chosenfloatip.getFloatingIpAddress());

        os.compute().floatingIps().addFloatingIP(os.compute().servers().get(serverid), chosenfloatip.getFloatingIpAddress());

        logger.info("Floating successfully!");
        return chosenfloatip.getFloatingIpAddress();
    }//EoM    

    public static boolean destroyServer(String instanceid, String connectionurl, String username, String password, String dom, String proj, String serverId, String fragname) throws IaaSAuthenticationException {
        logger.info("Testing OpenStack connection...");
        boolean isSuccess = true;
        try {
            Identifier domain = Identifier.byName(dom);
            Identifier project = Identifier.byName(proj);

            OSClient.OSClientV3 os = OSFactory.builderV3()
                    .endpoint(connectionurl)
                    .credentials(username, password, domain).scopeToProject(project, domain).authenticate();
            if (null != os.compute().flavors().list() && os.compute().flavors().list().size() > 0) {
                isSuccess = true;
                logger.info("Connection successful!");
            }
            String instanceName = instanceid + "" + fragname;
            logger.info("Deleting key: " + instanceName);
            //delete key
            os.compute().keypairs().delete(instanceName);
            //delete image
            os.compute().servers().delete(serverId);
            isSuccess = true;
        } catch (AuthenticationException e) {
            logger.severe(e.getMessage());
//            throw new IaaSAuthenticationException("Could not authenticate to " + connectionurl + " " + username);
            isSuccess = false;
        }
        return isSuccess;
    }//EoM    

}//EoC
