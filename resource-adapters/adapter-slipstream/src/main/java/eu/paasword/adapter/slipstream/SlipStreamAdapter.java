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
package eu.paasword.adapter.slipstream;

import eu.paasword.spi.adapter.ProxyAdapter;
import eu.paasword.spi.model.*;
import eu.paasword.spi.response.BasicResponseCode;
import eu.paasword.spi.response.SPIResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by smantzouratos
 */
public class SlipStreamAdapter implements ProxyAdapter {

    static final Logger logger = Logger.getLogger(SlipStreamAdapter.class.getName());

    static final SimpleDateFormat ft = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

    @Override
    public SPIResponse validateCredentials(CredentialsModel credentials) {

        logger.info("SlipStream Adapter invoked for validating credentials: " + credentials.getUsername() + ", at: " + credentials.getPaaSOffering().getEndpointURI());

        RestTemplate restTemplate = new RestTemplate();

        String username = credentials.getUsername();
        String password = credentials.getPassword();

        ResponseEntity<String> responseEntity = restTemplate.exchange(credentials.getPaaSOffering().getEndpointURI() + "auth/login?username=" + username + "&password=" + password, HttpMethod.POST, null, String.class);

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

            return new SPIResponse(BasicResponseCode.SUCCESS, "Credentials are validated!");

        } else {

            return new SPIResponse(BasicResponseCode.EXCEPTION, "Credentials cannot be validated!");

        }
    }

    @Override
    public SPIResponse getCloudProviders(CredentialsModel credentials) {

        RestTemplate restTemplate = new RestTemplate();

        List<CloudProviderModel> providers = new ArrayList<>();

        String username = credentials.getUsername();
        String password = credentials.getPassword();
        List<String> cookies = new ArrayList<>();

        ResponseEntity<String> responseEntity = restTemplate.exchange(credentials.getPaaSOffering().getEndpointURI() + "auth/login?username=" + username + "&password=" + password, HttpMethod.POST, null, String.class);

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

            responseEntity.getHeaders().get("Set-Cookie").stream().forEach(cookie -> {
                cookies.add(cookie);
            });

            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", cookies.stream().collect(Collectors.joining(";")));
            HttpEntity<String> entity = new HttpEntity<String>(headers);

            ResponseEntity<String> cloudProvidersResponse = restTemplate.exchange(credentials.getPaaSOffering().getEndpointURI() + "/user/" + username, HttpMethod.GET, entity, String.class);

            if (null != cloudProvidersResponse && cloudProvidersResponse.getStatusCode() == HttpStatus.OK) {

                try {

                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(new InputSource(new StringReader(cloudProvidersResponse.getBody())));

                    doc.getDocumentElement().normalize();

                    NodeList nList = doc.getDocumentElement().getElementsByTagName("parameters");

//                    logger.info("Parameters: " + nList.getLength());

                    for (int temp = 0; temp < nList.getLength(); temp++) {

                        Node nNode = nList.item(temp);

                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                            Element eElement = (Element) nNode;

//                            logger.info("Node: " + eElement.getNodeName());

                            for (int z = 0; z < nNode.getChildNodes().getLength(); z++) {

                                if (nNode.getChildNodes().item(z).getNodeName().equals("entry")) {

                                    NodeList children = nNode.getChildNodes().item(z).getChildNodes();

                                    for (int i = 0; i < children.getLength(); i++) {

                                        if (children.item(i).getNodeName().equals("string") && children.item(i).getTextContent().indexOf("username") != -1) {

                                            String name = children.item(i).getTextContent();

                                            String cloudName = name.substring(0, name.lastIndexOf("."));

                                            if (null != children.item(i + 2) && children.item(i + 2).getNodeName().equals("parameter")) {

                                                for (int x = 0; x < children.item(i + 2).getChildNodes().getLength(); x++) {

                                                    if (children.item(i + 2).getChildNodes().item(x).getNodeName().equals("value") && null != children.item(i + 2).getChildNodes().item(x).getTextContent() && !children.item(i + 2).getChildNodes().item(x).getTextContent().isEmpty()) {

                                                        CloudProviderModel provider = new CloudProviderModel();

                                                        provider.setName(cloudName);
                                                        CredentialsModel credentialsModel = new CredentialsModel();
                                                        credentialsModel.setUsername(children.item(i + 2).getChildNodes().item(x).getTextContent());
                                                        provider.setCredentialsModel(credentialsModel);
                                                        providers.add(provider);

                                                    }
                                                }
                                            }

                                        }

                                    }

                                }

                            }

                        }
                    }

                    return new SPIResponse(BasicResponseCode.SUCCESS, "Cloud Providers fetched successfully!", providers);

                } catch (Exception e) {
                    logger.severe(e.getMessage());
                    return new SPIResponse(BasicResponseCode.EXCEPTION, e.getMessage());
                }

            } else {
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Cloud Providers aren't fetched successfully!");
            }
        } else {
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Cloud Providers aren't fetched successfully!");
        }
    }

    @Override
    public SPIResponse getVirtualMachines(CredentialsModel credentials) {
        RestTemplate restTemplate = new RestTemplate();

        String username = credentials.getUsername();
        String password = credentials.getPassword();
        List<String> cookies = new ArrayList<>();
        List<VirtualMachineModel> vms = new ArrayList<>();

        ResponseEntity<String> responseEntity = restTemplate.exchange(credentials.getPaaSOffering().getEndpointURI() + "auth/login?username=" + username + "&password=" + password, HttpMethod.POST, null, String.class);

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

            responseEntity.getHeaders().get("Set-Cookie").stream().forEach(cookie -> {
                cookies.add(cookie);
            });

            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", cookies.stream().collect(Collectors.joining(";")));
            HttpEntity<String> entity = new HttpEntity<String>(headers);

            ResponseEntity<String> cloudProvidersResponse = restTemplate.exchange(credentials.getPaaSOffering().getEndpointURI() + "/vms", HttpMethod.GET, entity, String.class);

            if (null != cloudProvidersResponse && cloudProvidersResponse.getStatusCode() == HttpStatus.OK) {

                try {

                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(new InputSource(new StringReader(cloudProvidersResponse.getBody())));

                    doc.getDocumentElement().normalize();

                    NodeList nList = doc.getDocumentElement().getElementsByTagName("vm");

                    for (int temp = 0; temp < nList.getLength(); temp++) {

                        Node nNode = nList.item(temp);

                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                            Element eElement = (Element) nNode;

                            VirtualMachineModel vm = new VirtualMachineModel();
                            vm.setName(eElement.getAttribute("name"));

                            CloudProviderModel cloudProviderModel = new CloudProviderModel();
                            cloudProviderModel.setName(eElement.getAttribute("cloud"));
                            vm.setCloudProvider(cloudProviderModel);
                            vm.setRam(eElement.getAttribute("ram"));
                            vm.setCpu(eElement.getAttribute("cpu"));
                            vm.setDisk(eElement.getAttribute("disk"));
                            vm.setInstanceID(eElement.getAttribute("instanceId"));
                            vm.setInstanceType(eElement.getAttribute("instanceType"));
                            vm.setIp(eElement.getAttribute("ip"));
                            vm.setMeasurement(ft.parse(eElement.getAttribute("measurement")));
                            vm.setUsable(Boolean.valueOf(eElement.getAttribute("isUsable")));
                            vm.setRunningUUID(eElement.getAttribute("runUuid"));
                            vm.setState(eElement.getAttribute("state"));

                            UserModel user = new UserModel();
                            user.setUsername(eElement.getAttribute("user"));
                            vm.setUser(user);

                            vms.add(vm);


                        }

                    }

                    return new SPIResponse(BasicResponseCode.SUCCESS, "Virtual Machines fetched successfully!", vms);

                } catch (Exception e) {
                    logger.severe(e.getMessage());
                    return new SPIResponse(BasicResponseCode.EXCEPTION, e.getMessage());
                }

            } else {
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Virtual Machines aren't fetched successfully!");
            }
        } else

        {
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Virtual Machines aren't fetched successfully!");
        }

    }

    @Override
    public SPIResponse getRunningInstances(CredentialsModel credentials) {
        RestTemplate restTemplate = new RestTemplate();

        String username = credentials.getUsername();
        String password = credentials.getPassword();
        List<String> cookies = new ArrayList<>();
        List<RunningInstanceModel> runningInstances = new ArrayList<>();

        ResponseEntity<String> responseEntity = restTemplate.exchange(credentials.getPaaSOffering().getEndpointURI() + "auth/login?username=" + username + "&password=" + password, HttpMethod.POST, null, String.class);

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

            responseEntity.getHeaders().get("Set-Cookie").stream().forEach(cookie -> {
                cookies.add(cookie);
            });

            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", cookies.stream().collect(Collectors.joining(";")));
            HttpEntity<String> entity = new HttpEntity<String>(headers);

            ResponseEntity<String> cloudProvidersResponse = restTemplate.exchange(credentials.getPaaSOffering().getEndpointURI() + "/run", HttpMethod.GET, entity, String.class);

            if (null != cloudProvidersResponse && cloudProvidersResponse.getStatusCode() == HttpStatus.OK) {

                try {

                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(new InputSource(new StringReader(cloudProvidersResponse.getBody())));

                    doc.getDocumentElement().normalize();

                    NodeList nList = doc.getDocumentElement().getElementsByTagName("item");

                    for (int temp = 0; temp < nList.getLength(); temp++) {

                        Node nNode = nList.item(temp);

                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                            Element eElement = (Element) nNode;

                            int activeVM = Integer.valueOf(eElement.getAttribute("activeVm"));

                            if (activeVM != 0) {

                                RunningInstanceModel runningInstance = new RunningInstanceModel();
                                runningInstance.setResourceURI(eElement.getAttribute("resourceUri"));

                                CloudProviderModel cloudProviderModel = new CloudProviderModel();
                                cloudProviderModel.setName(eElement.getAttribute("cloudServiceNames"));
                                runningInstance.setCloudProvider(cloudProviderModel);

                                runningInstance.setUUID(eElement.getAttribute("uuid"));
                                runningInstance.setModuleResourceURI(eElement.getAttribute("moduleResourceUri"));
                                runningInstance.setStatus(eElement.getAttribute("status"));
                                runningInstance.setAbort(eElement.getAttribute("abort"));
                                runningInstance.setType(eElement.getAttribute("type"));
                                runningInstance.setTags(eElement.getAttribute("tags"));
                                runningInstance.setStartTime(ft.parse(eElement.getAttribute("startTime")));
                                runningInstance.setServiceURL(eElement.getAttribute("serviceUrl"));
                                runningInstance.setActiveVM(Integer.valueOf(eElement.getAttribute("activeVm")));

                                UserModel user = new UserModel();
                                user.setUsername(eElement.getAttribute("username"));
                                runningInstance.setUser(user);

                                runningInstances.add(runningInstance);

                            }


                        }

                    }

                    return new SPIResponse(BasicResponseCode.SUCCESS, "Running Instances fetched successfully!", runningInstances);

                } catch (Exception e) {
                    logger.severe(e.getMessage());
                    return new SPIResponse(BasicResponseCode.EXCEPTION, e.getMessage());
                }

            } else {
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Running Instances aren't fetched successfully!");
            }
        } else

        {
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Running Instances aren't fetched successfully!");
        }
    }

    @Override
    public SPIResponse getUsages(CredentialsModel credentials) {
        RestTemplate restTemplate = new RestTemplate();

        String username = credentials.getUsername();
        String password = credentials.getPassword();
        List<String> cookies = new ArrayList<>();
        List<UsageModel> usages = new ArrayList<>();

        ResponseEntity<String> responseEntity = restTemplate.exchange(credentials.getPaaSOffering().getEndpointURI() + "auth/login?username=" + username + "&password=" + password, HttpMethod.POST, null, String.class);

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

            responseEntity.getHeaders().get("Set-Cookie").stream().forEach(cookie -> {
                cookies.add(cookie);
            });

            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", cookies.stream().collect(Collectors.joining(";")));
            HttpEntity<String> entity = new HttpEntity<String>(headers);

            ResponseEntity<String> cloudProvidersResponse = restTemplate.exchange(credentials.getPaaSOffering().getEndpointURI() + "/api/usage", HttpMethod.GET, entity, String.class);

            if (null != cloudProvidersResponse && cloudProvidersResponse.getStatusCode() == HttpStatus.OK) {

                if (!cloudProvidersResponse.getBody().isEmpty()) {

                    logger.info("Response: " + cloudProvidersResponse.getBody());

                    try {

                        JSONObject response = new JSONObject(cloudProvidersResponse.getBody());

                        JSONArray usagesArray = response.getJSONArray("usages");

                        for (Object usageObj : usagesArray) {

                            JSONObject usageJSON = (JSONObject) usageObj;

                            UsageModel usage = new UsageModel();
                            usage.setUsageID(usageJSON.getString("id"));

                            CloudProviderModel cloudProviderModel = new CloudProviderModel();
                            cloudProviderModel.setName(usageJSON.getString("cloud"));
                            usage.setCloudProvider(cloudProviderModel);
                            UserModel user = new UserModel();
                            user.setUsername(usageJSON.getString("user"));
                            usage.setUser(user);

                            usage.setComputeTimestamp(usageJSON.getString("compute-timestamp"));
                            usage.setEndTimestamp(usageJSON.getString("end-timestamp"));
                            usage.setStartTimestamp(usageJSON.getString("start-timestamp"));

                            usage.setFrequency(usageJSON.getString("frequency"));

                            usage.setCpu(usageJSON.getJSONObject("usage").has("cpu") ? usageJSON.getJSONObject("usage").getJSONObject("cpu").getDouble("unit-minutes") : 0);
                            usage.setRam(usageJSON.getJSONObject("usage").has("ram") ? usageJSON.getJSONObject("usage").getJSONObject("ram").getDouble("unit-minutes") : 0);
                            usage.setVm(usageJSON.getJSONObject("usage").has("vm") ? usageJSON.getJSONObject("usage").getJSONObject("vm").getDouble("unit-minutes") : 0);
                            usage.setDisk(usageJSON.getJSONObject("usage").has("disk") ? usageJSON.getJSONObject("usage").getJSONObject("disk").getDouble("unit-minutes") : 0);
                            usage.setInstanceTypeMicro(usageJSON.getJSONObject("usage").has("instance-type_Micro") ? usageJSON.getJSONObject("usage").getJSONObject("instance-type_Micro").getDouble("unit-minutes") : 0);

                            usages.add(usage);

                        }


                        return new SPIResponse(BasicResponseCode.SUCCESS, "Usages fetched successfully!", usages);

                    } catch (Exception e) {
                        logger.severe(e.getMessage());
                        return new SPIResponse(BasicResponseCode.EXCEPTION, e.getMessage());
                    }

                } else {
                    return new SPIResponse(BasicResponseCode.SUCCESS, "Usages fetched successfully!", usages);
                }

            } else {
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Usages aren't fetched successfully!");
            }
        } else

        {
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Usages aren't fetched successfully!");
        }
    }

}//EoC
