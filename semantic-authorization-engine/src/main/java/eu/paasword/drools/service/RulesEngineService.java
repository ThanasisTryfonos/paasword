package eu.paasword.drools.service;

import eu.paasword.drools.*;

import static eu.paasword.drools.config.Initializer.KIE_BASE_MODEL_PREFIX;
import static eu.paasword.drools.config.Initializer.KNOWLEDGEBASE_PREFIX;
import static eu.paasword.drools.config.Initializer.PACKAGE_NAME;
import static eu.paasword.drools.config.Initializer.SESSION_PREFIX;

import eu.paasword.drools.util.ReflectionUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.paasword.rest.semanticauthorizationengine.transferobject.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.ObjectFilter;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.Match;
import org.kie.internal.io.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static eu.paasword.drools.config.Initializer.RULESPACKAGE;

import eu.paasword.rest.response.PaaSwordObjectResponse;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

/**
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 */
@Service
public class RulesEngineService {

    private static final Logger logger = Logger.getLogger(RulesEngineService.class.getName());

    private final ReleaseId releaseId = KieServices.Factory.get().newReleaseId(PACKAGE_NAME, "expert-system", "1.0");
    private final KieServices kieServices;
    private final KieFileSystem kieFileSystem;
    private final KieModuleModel kieModuleModel;
    private KieSession ksession;
    private KieContainer kieContainer;
    private RestTemplate restTemplate;

    @Value("${ontology.path}")
    private String ontologypath;
    @Value("${rules.path}")
    private String rulespath;
    @Value("${ontology.webpath}")
    private String ontologywebpath;
    @Value("${rules.webpath}")
    private String ruleswebpath;

    @Autowired
    public RulesEngineService() {
        logger.info("Rule Engine initializing...");
        this.kieServices = KieServices.Factory.get();
        this.kieFileSystem = kieServices.newKieFileSystem();
        this.kieModuleModel = kieServices.newKieModuleModel();
    }//EoC    

    private synchronized void setSession(KieSession session) {
        this.ksession = session;
    }

    private synchronized KieSession getSession() {
        return this.ksession;
    }

    public KieContainer refreshKnowledgebase() {

        try {
            String current_dir = System.getProperty("user.dir");
            logger.info("Using current directory: " + current_dir);
            //TODO create /rules if it does not exist
            new File(current_dir + "/" + RULESPACKAGE).mkdirs();
            FileUtils.cleanDirectory(new File(current_dir + "/" + RULESPACKAGE));
        } catch (IOException ex) {
            logger.severe("Error cleaning the rules directory" + ex.getMessage());
        }

        String knowledgebasename = KNOWLEDGEBASE_PREFIX + "default";  //runningapplication.getId()
        logger.info("Knowledge-base name: " + knowledgebasename);
        KieBaseModel kbase = kieModuleModel.newKieBaseModel(KIE_BASE_MODEL_PREFIX + knowledgebasename).setDefault(true); //.setEventProcessingMode(EventProcessingOption.STREAM);
        kbase.addPackage(RULESPACKAGE + "." + knowledgebasename);
        kbase.setEventProcessingMode(EventProcessingOption.CLOUD);
        String factSessionName = SESSION_PREFIX + knowledgebasename;
        KieSessionModel ksessionmodel = kbase.newKieSessionModel(factSessionName);                                                                       //.setClockType(ClockTypeOption.get("realtime"));
        ksessionmodel.setDefault(true).setType(KieSessionModel.KieSessionType.STATEFUL);

        Double newversion = Double.parseDouble(releaseId.getVersion()) + 0.1;
        ReleaseId newReleaseId = kieServices.newReleaseId(PACKAGE_NAME, "expert-system", newversion.toString());
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);

        kieFileSystem.generateAndWritePomXML(newReleaseId);
        kieFileSystem.writeKModuleXML(kieModuleModel.toXML());
        logger.log(java.util.logging.Level.INFO, "kieModuleModel--ToXML\n{0}", kieModuleModel.toXML());

        //adding rules
        loadRules();

        logger.info("kieBuilder.buildAll()");
        kieBuilder.buildAll();
        //
        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kieBuilder.getResults().toString());
        }//if
        logger.info("Spawning new Container");
        kieContainer = kieServices.newKieContainer(newReleaseId);

        KieSession kieSession = kieContainer.newKieSession(factSessionName);
        setSession(kieSession);

        return kieContainer;

    }//EoM   

    /*
    ******* Rules
     */
    public void loadRules() {
        String rules = getRulesFromWebEndpoint();
        if (rules != null) {
            loadRulesFromUrlData(rules);
        } else {
            loadRulesFromFile();
        }
    }//EoM

    public void loadRulesFromUrlData(String rules) {
        logger.info("Loading Rules from URL");
        String knowledgebasename = KNOWLEDGEBASE_PREFIX + "default";   //runningapplication.getId()
        String drlPath4deployment = RULESPACKAGE + "/" + knowledgebasename + "/" + knowledgebasename + ".drl";
        try {
            String current_dir = System.getProperty("user.dir");
            Path policyPackagePath = Paths.get(current_dir + "/" + RULESPACKAGE + "/" + knowledgebasename);

            String data = getRulesFromFile();
            data += "\n\n";
            data += rules;

            logger.info("All rules: \n" + data);

            Files.createDirectories(policyPackagePath);
            FileOutputStream out = new FileOutputStream(current_dir + "/" + drlPath4deployment);
            out.write(data.getBytes());
            out.close();
            logger.info("Writing rules at: " + current_dir + "/" + RULESPACKAGE + "/" + knowledgebasename + "/" + knowledgebasename + ".drl");
            kieFileSystem.write(ResourceFactory.newFileResource(current_dir + "/" + RULESPACKAGE + "/" + knowledgebasename + "/" + knowledgebasename + ".drl"));
        } catch (Exception ex) {
            logger.severe("Error during the creation of production memory: " + ex.getMessage());
            ex.printStackTrace();
        }
    }//EoM

    private String getRulesFromWebEndpoint() {
        String ret = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            PaaSwordObjectResponse response = restTemplate.getForObject(ruleswebpath, PaaSwordObjectResponse.class);
            ret = (String) response.getReturnobject();
        } catch (Exception ex) {
            Logger.getLogger(RulesEngineService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }//EoM    

    private void loadRulesFromFile() {
        logger.info("Loading Rules from File to production memory");
        String knowledgebasename = KNOWLEDGEBASE_PREFIX + "default";   //runningapplication.getId()
        String drlPath4deployment = RULESPACKAGE + "/" + knowledgebasename + "/" + knowledgebasename + ".drl";
        try {
            String current_dir = System.getProperty("user.dir");
            Path policyPackagePath = Paths.get(current_dir + "/" + RULESPACKAGE + "/" + knowledgebasename);
            String data = "";
            //1st add default rules
            data += getRulesFromFile();

            logger.info("All rules: \n" + data);

            Files.createDirectories(policyPackagePath);
            FileOutputStream out = new FileOutputStream(current_dir + "/" + drlPath4deployment);
            out.write(data.getBytes());
            out.close();
            logger.info("Writing rules at: " + current_dir + "/" + RULESPACKAGE + "/" + knowledgebasename + "/" + knowledgebasename + ".drl");
            kieFileSystem.write(ResourceFactory.newFileResource(current_dir + "/" + RULESPACKAGE + "/" + knowledgebasename + "/" + knowledgebasename + ".drl"));
        } catch (Exception ex) {
            logger.severe("Error during the creation of production memory: " + ex.getMessage());
            ex.printStackTrace();
        }
    }//EoM    

    private String getRulesFromFile() {
        String ret = "";
        try {
            logger.info("Loading resource: " + rulespath);
            InputStream inputstream = this.getClass().getResourceAsStream(rulespath);
            ret = IOUtils.toString(inputstream, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(RulesEngineService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }//EoM    

    /*
    ************ Ontology
     */
    public void loadOntology() {
        logger.info("load Ontology");
        String ontology = getOntologyFromWebEndpoint();
        if (ontology != null) {
            loadOntologyFromLoadedData(ontology);
        } else {
            loadOntologyFromFile();
        }
        logger.info("Ontology loaded!");
    }//EoM    

    private String getOntologyFromWebEndpoint() {
        String ret = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            PaaSwordObjectResponse response = restTemplate.getForObject(ontologywebpath, PaaSwordObjectResponse.class);
            ret = (String) response.getReturnobject();
        } catch (Exception ex) {
            Logger.getLogger(RulesEngineService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }//EoM

    public void loadOntologyFromFile() {
        Map<String, Object> intiorphanclassobjectmap = new HashMap<>();
        Map<String, Object> intermediateclassobjectmap = new HashMap<>();
        Map<String, Object> finalclassobjectmap = new HashMap<>();
        Map<String, Object> finalinstanceobjectmap = new HashMap<>();
        Map<String, Object> initopobjectmap = new HashMap<>();
        Map<String, Object> intermediateopobjectmap = new HashMap<>();
        Map<String, Object> finalopobjectmap = new HashMap<>();
        List<Object> finalktriplesobjectlist = new ArrayList<>();

        try {
            /*
            *   LOAD CLASSES
             */
            //Fetch all Classes that are orphan
            Stream<String> stream = Files.lines(Paths.get(ontologypath));
            intiorphanclassobjectmap = stream
                    .filter(line -> !line.startsWith("#") && !line.trim().equalsIgnoreCase("") && ((line.split(",")[0]).trim().equalsIgnoreCase("C") || (line.split(",")[0]).trim().equalsIgnoreCase("Class"))) //&& (line.split(",")[2]).trim().equalsIgnoreCase("null")
                    .map(line -> ReflectionUtil.createOrphanClazz(ReflectionUtil.getClassLabelFromLine(line)))
                    .collect(Collectors.toMap(o -> ReflectionUtil.getNameOfObject(o), o -> o));
            final Map<String, Object> temp1 = intiorphanclassobjectmap;
//            logger.info("1st Pass of Classes: ");
//            intiorphanclassobjectmap.values().forEach(System.out::println);

            //Handle Non Orphan
            Stream<String> stream2 = Files.lines(Paths.get(ontologypath));
            intermediateclassobjectmap = stream2
                    .filter(line -> !line.startsWith("#") && !line.trim().equalsIgnoreCase("") && ((line.split(",")[0]).trim().equalsIgnoreCase("C") || (line.split(",")[0]).trim().equalsIgnoreCase("Class")) && !(line.split(",")[2]).trim().equalsIgnoreCase("null"))
                    .map(line -> ReflectionUtil.setParentToClazzObject(temp1.get(ReflectionUtil.getClassLabelFromLine(line)), temp1.get(ReflectionUtil.getParentalClassLabelFromLine(line))))
                    .collect(Collectors.toMap(o -> ReflectionUtil.getNameOfObject(o), o -> o));

            final Map<String, Object> temp2 = intermediateclassobjectmap;
//            logger.info("Intermediate Pass of Classes: ");
//            intermediateclassobjectmap.values().forEach(System.out::println);

            //Create Final list
            finalclassobjectmap = temp1.values().stream()
                    .filter(clazz -> !temp2.containsKey(ReflectionUtil.getNameOfObject(clazz)))
                    .collect(Collectors.toMap(o -> ReflectionUtil.getNameOfObject(o), o -> o));
            //append non orphan
            for (Object object : intermediateclassobjectmap.values()) {
                finalclassobjectmap.put(ReflectionUtil.getNameOfObject(object), object);
            }
            final Map<String, Object> temp3 = finalclassobjectmap;              //FINAL CLASSES
//            logger.info("\nClasses: ");
//            finalclassobjectmap.values().forEach(System.out::println);

            /*
            *   LOAD CLASS INSTANCES
             */
            Stream<String> stream3 = Files.lines(Paths.get(ontologypath));
            finalinstanceobjectmap = stream3
                    .filter(line -> !line.startsWith("#") && !line.trim().equalsIgnoreCase("") && ((line.split(",")[0]).trim().equalsIgnoreCase("IoC") || (line.split(",")[0]).trim().equalsIgnoreCase("InstanceOfClass"))) //&& (line.split(",")[2]).trim().equalsIgnoreCase("null")
                    .map(line -> ReflectionUtil.createInstanceOfClazz(ReflectionUtil.getInstanceLabelFromLine(line), temp3.get(ReflectionUtil.getInstanceClassLabelFromLine(line))))
                    .collect(Collectors.toMap(o -> ReflectionUtil.getNameOfObject(o), o -> o));

            final Map<String, Object> temp4 = finalinstanceobjectmap;              //FINAL IOC            

//            logger.info("\nInstances Of Classes: ");
//            finalinstanceobjectmap.values().forEach(System.out::println);

            /*
            *   LOAD OBJECT PROPERTIES
             */
            Stream<String> stream4 = Files.lines(Paths.get(ontologypath));
            initopobjectmap = stream4
                    .filter(line -> !line.startsWith("#") && !line.trim().equalsIgnoreCase("") && ((line.split(",")[0]).trim().equalsIgnoreCase("OP") || (line.split(",")[0]).trim().equalsIgnoreCase("ObjectProperty"))) //&& (line.split(",")[2]).trim().equalsIgnoreCase("null")
                    .map(line -> ReflectionUtil.createOrphanObjectProperty(ReflectionUtil.getOPLabelFromLine(line), temp3.get(ReflectionUtil.getDomainOPLabelFromLine(line)), temp3.get(ReflectionUtil.getRangeOPLabelFromLine(line)), new Boolean(ReflectionUtil.getTransitiveFromLine(line))))
                    .collect(Collectors.toMap(o -> ReflectionUtil.getNameOfObject(o), o -> o));
            final Map<String, Object> temp5 = initopobjectmap;
//            logger.info("Init Pass Of Object Properties: ");
//            initopobjectmap.values().forEach(System.out::println);

            Stream<String> stream5 = Files.lines(Paths.get(ontologypath));
            intermediateopobjectmap = stream5
                    .filter(line -> !line.startsWith("#") && !line.trim().equalsIgnoreCase("") && ((line.split(",")[0]).trim().equalsIgnoreCase("OP") || (line.split(",")[0]).trim().equalsIgnoreCase("ObjectProperty")) && !(line.split(",")[5]).trim().equalsIgnoreCase("null"))
                    .map(line -> ReflectionUtil.setParentToObjectPropertyObject(temp5.get(ReflectionUtil.getOPLabelFromLine(line)), temp5.get(ReflectionUtil.getParentalOPLabelFromLine(line))))
                    .collect(Collectors.toMap(o -> ReflectionUtil.getNameOfObject(o), o -> o));
            final Map<String, Object> temp12 = intermediateopobjectmap;
//            logger.info("Intermediate Object Properties: ");
//            intermediateopobjectmap.values().forEach(System.out::println);

            //Create Final list
            finalopobjectmap = temp5.values().stream()
                    .filter(clazz -> !temp12.containsKey(ReflectionUtil.getNameOfObject(clazz)))
                    .collect(Collectors.toMap(o -> ReflectionUtil.getNameOfObject(o), o -> o));
            //append non orphan
            for (Object object : intermediateopobjectmap.values()) {
                finalopobjectmap.put(ReflectionUtil.getNameOfObject(object), object);
            }
            final Map<String, Object> temp6 = finalopobjectmap;              //FINAL OBJECT PROPERTIES
//            logger.info("\nObject Properties: ");
//            finalopobjectmap.values().forEach(System.out::println);


            /*
            *   LOAD KNOWLEDGE TRIPLES
             */
            Stream<String> stream6 = Files.lines(Paths.get(ontologypath));
            finalktriplesobjectlist = stream6
                    .filter(line -> !line.startsWith("#") && !line.trim().equalsIgnoreCase("") && ((line.split(",")[0]).trim().equalsIgnoreCase("KT") || (line.split(",")[0]).trim().equalsIgnoreCase("KnowledgeTriple"))) //&& (line.split(",")[2]).trim().equalsIgnoreCase("null")
                    .map(line -> ReflectionUtil.createIKnowledgeTriple(
                            temp4.get(ReflectionUtil.getKTDomainFromLine(line)),
                            temp6.get(ReflectionUtil.getKTObjectPropertyFromLine(line)),
                            temp4.get(ReflectionUtil.getKTRangeFromLine(line))
                    ))
                    .collect(Collectors.toList());

            logger.info("\nKnowledge Triples: ");
            finalktriplesobjectlist.forEach(System.out::println);

            synchronized (ksession) {
                finalclassobjectmap.values().forEach(ksession::insert);
                finalinstanceobjectmap.values().forEach(ksession::insert);
                finalopobjectmap.values().forEach(ksession::insert);
                finalktriplesobjectlist.forEach(ksession::insert);

                //fire the rule to check logical consistency of triples
                logger.info("Fire Once in order to get the logical errors");

                ksession.fireAllRules(new AgendaFilter() {
                    public boolean accept(Match match) {
                        String rulename = match.getRule().getName();
                        if (rulename.toLowerCase().startsWith("inference") || rulename.toLowerCase().startsWith("combining")) {
                            return true;
                        }
                        return false;
                    }
                });
                //handle logical errors
                List<LogicalError> errors = new ArrayList<>();

                for (FactHandle handle : ksession.getFactHandles(new ObjectFilter() {
                    public boolean accept(Object object) {
                        if (LogicalError.class.equals(object.getClass())) {
                            return true;
                        }
                        return false;
                    }
                })) {
                    errors.add((LogicalError) ksession.getFactHandle(handle));
                }

                logger.info("Amount of Logical Errors: " + errors.size());
            }//synchronized

        } catch (IOException e) {
            logger.severe("Structural Errors During Ontology Parsing");
//            e.printStackTrace();
        }

    }//EoM

    public void loadOntologyFromLoadedData(String lineseparatedontology) {

        Map<String, Object> intiorphanclassobjectmap = new HashMap<>();
        Map<String, Object> intermediateclassobjectmap = new HashMap<>();
        Map<String, Object> finalclassobjectmap = new HashMap<>();
        Map<String, Object> finalinstanceobjectmap = new HashMap<>();
        Map<String, Object> initopobjectmap = new HashMap<>();
        Map<String, Object> intermediateopobjectmap = new HashMap<>();
        Map<String, Object> finalopobjectmap = new HashMap<>();
        List<Object> finalktriplesobjectlist = new ArrayList<>();

        String[] lines = lineseparatedontology.split("\n");

        try {
            /*
            *   LOAD CLASSES
             */
            //Fetch all Classes that are orphan
            Stream<String> stream = Arrays.stream(lines);
            intiorphanclassobjectmap = stream
                    .filter(line -> !line.startsWith("#") && !line.trim().equalsIgnoreCase("") && ((line.split(",")[0]).trim().equalsIgnoreCase("C") || (line.split(",")[0]).trim().equalsIgnoreCase("Class"))) //&& (line.split(",")[2]).trim().equalsIgnoreCase("null")
                    .map(line -> ReflectionUtil.createOrphanClazz(ReflectionUtil.getClassLabelFromLine(line)))
                    .collect(Collectors.toMap(o -> ReflectionUtil.getNameOfObject(o), o -> o));
            final Map<String, Object> temp1 = intiorphanclassobjectmap;
//            logger.info("1st Pass of Classes: ");
//            intiorphanclassobjectmap.values().forEach(System.out::println);

            //Handle Non Orphan
            Stream<String> stream2 = Arrays.stream(lines);
            intermediateclassobjectmap = stream2
                    .filter(line -> !line.startsWith("#") && !line.trim().equalsIgnoreCase("") && ((line.split(",")[0]).trim().equalsIgnoreCase("C") || (line.split(",")[0]).trim().equalsIgnoreCase("Class")) && !(line.split(",")[2]).trim().equalsIgnoreCase("null"))
                    .map(line -> ReflectionUtil.setParentToClazzObject(temp1.get(ReflectionUtil.getClassLabelFromLine(line)), temp1.get(ReflectionUtil.getParentalClassLabelFromLine(line))))
                    .collect(Collectors.toMap(o -> ReflectionUtil.getNameOfObject(o), o -> o));

            final Map<String, Object> temp2 = intermediateclassobjectmap;
//            logger.info("Intermediate Pass of Classes: ");
//            intermediateclassobjectmap.values().forEach(System.out::println);

            //Create Final list
            finalclassobjectmap = temp1.values().stream()
                    .filter(clazz -> !temp2.containsKey(ReflectionUtil.getNameOfObject(clazz)))
                    .collect(Collectors.toMap(o -> ReflectionUtil.getNameOfObject(o), o -> o));
            //append non orphan
            for (Object object : intermediateclassobjectmap.values()) {
                finalclassobjectmap.put(ReflectionUtil.getNameOfObject(object), object);
            }
            final Map<String, Object> temp3 = finalclassobjectmap;              //FINAL CLASSES
//            logger.info("\nClasses: ");
//            finalclassobjectmap.values().forEach(System.out::println);

            /*
            *   LOAD CLASS INSTANCES
             */
            Stream<String> stream3 = Arrays.stream(lines);
            finalinstanceobjectmap = stream3
                    .filter(line -> !line.startsWith("#") && !line.trim().equalsIgnoreCase("") && ((line.split(",")[0]).trim().equalsIgnoreCase("IoC") || (line.split(",")[0]).trim().equalsIgnoreCase("InstanceOfClass"))) //&& (line.split(",")[2]).trim().equalsIgnoreCase("null")
                    .map(line -> ReflectionUtil.createInstanceOfClazz(ReflectionUtil.getInstanceLabelFromLine(line), temp3.get(ReflectionUtil.getInstanceClassLabelFromLine(line))))
                    .collect(Collectors.toMap(o -> ReflectionUtil.getNameOfObject(o), o -> o));

            final Map<String, Object> temp4 = finalinstanceobjectmap;              //FINAL IOC            

//            logger.info("\nInstances Of Classes: ");
//            finalinstanceobjectmap.values().forEach(System.out::println);

            /*
            *   LOAD OBJECT PROPERTIES
             */
            Stream<String> stream4 = Arrays.stream(lines);
            initopobjectmap = stream4
                    .filter(line -> !line.startsWith("#") && !line.trim().equalsIgnoreCase("") && ((line.split(",")[0]).trim().equalsIgnoreCase("OP") || (line.split(",")[0]).trim().equalsIgnoreCase("ObjectProperty"))) //&& (line.split(",")[2]).trim().equalsIgnoreCase("null")
                    .map(line -> ReflectionUtil.createOrphanObjectProperty(ReflectionUtil.getOPLabelFromLine(line), temp3.get(ReflectionUtil.getDomainOPLabelFromLine(line)), temp3.get(ReflectionUtil.getRangeOPLabelFromLine(line)), new Boolean(ReflectionUtil.getTransitiveFromLine(line))))
                    .collect(Collectors.toMap(o -> ReflectionUtil.getNameOfObject(o), o -> o));
            final Map<String, Object> temp5 = initopobjectmap;
//            logger.info("Init Pass Of Object Properties: ");
//            initopobjectmap.values().forEach(System.out::println);

            Stream<String> stream5 = Arrays.stream(lines);
            intermediateopobjectmap = stream5
                    .filter(line -> !line.startsWith("#") && !line.trim().equalsIgnoreCase("") && ((line.split(",")[0]).trim().equalsIgnoreCase("OP") || (line.split(",")[0]).trim().equalsIgnoreCase("ObjectProperty")) && !(line.split(",")[5]).trim().equalsIgnoreCase("null"))
                    .map(line -> ReflectionUtil.setParentToObjectPropertyObject(temp5.get(ReflectionUtil.getOPLabelFromLine(line)), temp5.get(ReflectionUtil.getParentalOPLabelFromLine(line))))
                    .collect(Collectors.toMap(o -> ReflectionUtil.getNameOfObject(o), o -> o));
            final Map<String, Object> temp12 = intermediateopobjectmap;
//            logger.info("Intermediate Object Properties: ");
//            intermediateopobjectmap.values().forEach(System.out::println);

            //Create Final list
            finalopobjectmap = temp5.values().stream()
                    .filter(clazz -> !temp12.containsKey(ReflectionUtil.getNameOfObject(clazz)))
                    .collect(Collectors.toMap(o -> ReflectionUtil.getNameOfObject(o), o -> o));
            //append non orphan
            for (Object object : intermediateopobjectmap.values()) {
                finalopobjectmap.put(ReflectionUtil.getNameOfObject(object), object);
            }
            final Map<String, Object> temp6 = finalopobjectmap;              //FINAL OBJECT PROPERTIES
//            logger.info("\nObject Properties: ");
//            finalopobjectmap.values().forEach(System.out::println);


            /*
            *   LOAD KNOWLEDGE TRIPLES
             */
            Stream<String> stream6 = Arrays.stream(lines);
            finalktriplesobjectlist = stream6
                    .filter(line -> !line.startsWith("#") && !line.trim().equalsIgnoreCase("") && ((line.split(",")[0]).trim().equalsIgnoreCase("KT") || (line.split(",")[0]).trim().equalsIgnoreCase("KnowledgeTriple"))) //&& (line.split(",")[2]).trim().equalsIgnoreCase("null")
                    .map(line -> ReflectionUtil.createIKnowledgeTriple(
                            temp4.get(ReflectionUtil.getKTDomainFromLine(line)),
                            temp6.get(ReflectionUtil.getKTObjectPropertyFromLine(line)),
                            temp4.get(ReflectionUtil.getKTRangeFromLine(line))
                    ))
                    .collect(Collectors.toList());

//            logger.info("\nKnowledge Triples: ");
            finalktriplesobjectlist.forEach(System.out::println);

            synchronized (ksession) {
                finalclassobjectmap.values().forEach(ksession::insert);
                finalinstanceobjectmap.values().forEach(ksession::insert);
                finalopobjectmap.values().forEach(ksession::insert);
                finalktriplesobjectlist.forEach(ksession::insert);

                //fire the rule to check logical consistency of triples
                logger.info("Fire Once in order to get the logical errors");

                ksession.fireAllRules(new AgendaFilter() {
                    public boolean accept(Match match) {
                        String rulename = match.getRule().getName();
                        if (rulename.toLowerCase().startsWith("inference") || rulename.toLowerCase().startsWith("combining")) {    // debug is missing
                            return true;
                        }
                        return false;
                    }
                });
                //handle logical errors
                List<LogicalError> errors = new ArrayList<>();

                for (FactHandle handle : ksession.getFactHandles(new ObjectFilter() {
                    public boolean accept(Object object) {
                        if (LogicalError.class.equals(object.getClass())) {
                            return true;
                        }
                        return false;
                    }
                })) {
                    errors.add((LogicalError) ksession.getFactHandle(handle));
                }

                logger.info("Amount of Logical Errors: " + errors.size());
            }//synchronized

        } catch (Exception ex) {
            logger.severe("Structural Errors During Ontology Parsing");
        }
    }//EoM    

    /*
    ******************   REQUEST
     */
    public String handleRequest(AuthorizationRequest authrequest) {

        logger.info("Request: " + authrequest.getRequestid());

        String ret = eu.paasword.drools.util.Message.REQUEST_DENY;
        KieSession kieSession = (KieSession) getSession();
        String requestid = authrequest.getRequestid();

        //extract rule identifiers
        ArrayList<String> rulelist = new ArrayList<>();
        //perform iteration on PolicySets
        List<PolicySet> policysets = authrequest.getPolicysets();
        if (policysets != null) {
            for (PolicySet policyset : policysets) {
                List<Policy> policies = policyset.getPolicies();
                for (Policy policy : policies) {
                    List<Rule> rules = policy.getRules();
                    for (Rule rule : rules) {
                        rulelist.add(rule.getRuleidentifier());
                    }//for Rule
                }//for Policy
            }//for PolicySet
        }

        //perform iteration on Policies                
        List<Policy> policies = authrequest.getPolicies();
        if (policies != null) {
            for (Policy policy : policies) {
                List<Rule> rules = policy.getRules();
                for (Rule rule : rules) {
                    rulelist.add(rule.getRuleidentifier());
                }//for Rule
            }//for Policy        
        }
        //perform iteration on Rules
        List<Rule> rules = authrequest.getRules();
        if (rules != null) {
            for (Rule rule : rules) {
                rulelist.add(rule.getRuleidentifier());
            }//for Rule
        }

        //add default rules
        rulelist.add("inference");
//        rulelist.add("debug");
//        rulelist.add("combining-DefaultPermitAll");
//        rulelist.add("combining-DefaultDenyAll");

        rulelist.stream().forEach(rule -> {
            logger.info("Performing execution on RuleIDs: " + rule);
        });

        synchronized (kieSession) {

            //**** Parsing of requests
            String ipaddress = authrequest.getRemoteAddress();      //OP hasIP   
            String subjectname = authrequest.getSubjectinstance();  //OP requestHasSubject
            String objectname = authrequest.getObjectinstance();    //OP requestHasObject
            String actionname = authrequest.getActioninstance();    //OP requestHasAction

            logger.info("IP: " + ipaddress + ", Subject: " + subjectname + ", Object: " + objectname + ", Action: " + actionname);

            JSONArray contextArray = new JSONArray(authrequest.getRequestContext());

            ObjectProperty op1 = (ObjectProperty) ksession.getObject((FactHandle) getObjectFromAgenda(ksession, ObjectProperty.class, "hasIP"));
            ObjectProperty op2 = (ObjectProperty) ksession.getObject((FactHandle) getObjectFromAgenda(ksession, ObjectProperty.class, "requestHasSubject"));
            ObjectProperty op3 = (ObjectProperty) ksession.getObject((FactHandle) getObjectFromAgenda(ksession, ObjectProperty.class, "requestHasObject"));
            ObjectProperty op4 = (ObjectProperty) ksession.getObject((FactHandle) getObjectFromAgenda(ksession, ObjectProperty.class, "requestHasAction"));

            //create knowledge
            //1 create request instance
            FactHandle reqclassf = (FactHandle) getObjectFromAgenda(ksession, Clazz.class, "Request");
            Clazz reqclass = (Clazz) ksession.getObject(reqclassf);
            InstanceOfClazz ioreq = new InstanceOfClazz(requestid, reqclass);

            //2 handle subject
            FactHandle f1 = (FactHandle) getObjectFromAgenda(ksession, Clazz.class, "Subject");
            Clazz cf1 = (Clazz) ksession.getObject(f1);
            FactHandle f2 = (FactHandle) getObjectFromAgenda(ksession, InstanceOfClazz.class, subjectname);
            InstanceOfClazz iocsubject;
            if (f2 != null) {
                iocsubject = (InstanceOfClazz) ksession.getObject(f2);
            } else {
                iocsubject = new InstanceOfClazz(subjectname, cf1);
                kieSession.insert(iocsubject);
            }

            //Any

            //3 handle object
            FactHandle f3 = (FactHandle) getObjectFromAgenda(ksession, Clazz.class, "Object");
            Clazz cf3 = (Clazz) ksession.getObject(f3);
            FactHandle f4 = (FactHandle) getObjectFromAgenda(ksession, InstanceOfClazz.class, objectname);
            InstanceOfClazz iocobject;
            if (f4 != null) {
                iocobject = (InstanceOfClazz) ksession.getObject(f4);
            } else {
                iocobject = new InstanceOfClazz(objectname, cf3);
                kieSession.insert(iocobject);
            }
            //4 handle action
            FactHandle f5 = (FactHandle) getObjectFromAgenda(ksession, Clazz.class, "Permission_Element");
            Clazz cf5 = (Clazz) ksession.getObject(f5);
            FactHandle f6 = (FactHandle) getObjectFromAgenda(ksession, InstanceOfClazz.class, actionname);
            InstanceOfClazz iocaction;
            if (f6 != null) {
                iocaction = (InstanceOfClazz) ksession.getObject(f6);
            } else {
                iocaction = new InstanceOfClazz(actionname, cf5);
                kieSession.insert(iocaction);
            }

            //5 handle IP
            FactHandle f7 = (FactHandle) getObjectFromAgenda(ksession, Clazz.class, "IPAddress");
            Clazz cf7 = (Clazz) ksession.getObject(f7);
            FactHandle f8 = (FactHandle) getObjectFromAgenda(ksession, InstanceOfClazz.class, ipaddress);
            InstanceOfClazz iocip;
            if (f8 != null) {
                iocip = (InstanceOfClazz) ksession.getObject(f8);
            } else {
                iocip = new InstanceOfClazz(ipaddress, cf7);
                kieSession.insert(iocip);
            }

            //Handlers
            KnowledgeTriple t1 = new KnowledgeTriple(ioreq, op2, iocsubject);       //request  requestHasSubject Subject
            KnowledgeTriple t2 = new KnowledgeTriple(ioreq, op3, iocobject);        //request  requestHasObject  Object
            KnowledgeTriple t3 = new KnowledgeTriple(ioreq, op4, iocaction);        //request  requestHasAction  Object
            KnowledgeTriple t4 = new KnowledgeTriple(ioreq, op1, iocip);            //request  hasIP  IP    

            //insert additional knowledge
            kieSession.insert(ioreq);
            kieSession.insert(t1);
            kieSession.insert(t2);
            kieSession.insert(t3);
            kieSession.insert(t4);

            // Call other handlers and insert additional knowledge
            if (null == restTemplate) {
                restTemplate = new RestTemplate();
            }

            if (null != authrequest.getHandlers() && !authrequest.getHandlers().isEmpty()) {

                authrequest.getHandlers().stream().forEach(handler -> {

                    KeyValue keyValue = new KeyValue();


                    if (handler.getDomainargumentinstance().equals("Any Subject")) {
                        keyValue.setKey(authrequest.getSubjectinstance());
                    } else {
                        keyValue.setKey(handler.getDomainargumentinstance());
                    }

                    HttpEntity entity = new HttpEntity(keyValue, null);

                    try {

                        ResponseEntity<String> responseEntity = restTemplate.exchange(handler.getRestendpoint(), HttpMethod.POST, entity, String.class);

                        // TODO
                        if (null != responseEntity && null != responseEntity.getStatusCode() && responseEntity.getStatusCode().is2xxSuccessful()) {

                            if (null != responseEntity.getBody() && !responseEntity.getBody().isEmpty()) {

                                logger.info("MSG: " + responseEntity.getBody());

                                // InstanceOfClassDomain
                                InstanceOfClazz instanceOfClazzDomain = null;
                                if (handler.getDomainclazzname().equals("Request")) {
                                    instanceOfClazzDomain = ioreq;
                                } else if (handler.getDomainclazzname().equals("Subject") && handler.getDomainargumentinstance().equals("Any Subject")) {
                                    instanceOfClazzDomain = iocsubject;
                                } else if (handler.getDomainclazzname().equals("Object")) {
                                    instanceOfClazzDomain = iocobject;
                                } else {

                                    FactHandle tempF = (FactHandle) getObjectFromAgenda(ksession, Clazz.class, handler.getDomainclazzname().replace(" ", "_"));
                                    Clazz tempClazz = (Clazz) ksession.getObject(tempF);
                                    FactHandle tempF2 = (FactHandle) getObjectFromAgenda(ksession, InstanceOfClazz.class, handler.getDomainargumentinstance().replace(" ", "_"));
                                    if (tempF2 != null) {
                                        instanceOfClazzDomain = (InstanceOfClazz) ksession.getObject(tempF2);
                                    } else {
                                        instanceOfClazzDomain = new InstanceOfClazz(handler.getDomainargumentinstance().replace(" ", "_"), tempClazz);
                                        kieSession.insert(instanceOfClazzDomain);
                                    }

                                }

                                // ObjectProperty
                                ObjectProperty tempObjectProperty = (ObjectProperty) ksession.getObject((FactHandle) getObjectFromAgenda(ksession, ObjectProperty.class, handler.getPropertyname()));

                                logger.info("Domain: " + tempObjectProperty.getDomain().getName() + ", Range: " + tempObjectProperty.getRange().getName());

                                // InstanceOfClassRange
                                InstanceOfClazz instanceOfClazzRange = null;

                                FactHandle tempF = (FactHandle) getObjectFromAgenda(ksession, Clazz.class, handler.getRangeclazzname());
                                Clazz tempClazz = (Clazz) ksession.getObject(tempF);
                                FactHandle tempF2 = (FactHandle) getObjectFromAgenda(ksession, InstanceOfClazz.class, responseEntity.getBody().replace(" ", "_"));

                                if (tempF2 != null) {
                                    instanceOfClazzRange = (InstanceOfClazz) ksession.getObject(tempF2);
                                } else {
                                    instanceOfClazzRange = new InstanceOfClazz(responseEntity.getBody().replace(" ", "_"), tempClazz);
                                    kieSession.insert(instanceOfClazzRange);
                                }

                                KnowledgeTriple tempKnowledgeTriple = new KnowledgeTriple(instanceOfClazzDomain, tempObjectProperty, instanceOfClazzRange);

                                // Cleaning stale KTs
                                for (Object factHandle : getKnowledgeTriplesForDomainPredicate(ksession, instanceOfClazzDomain.getName(), tempObjectProperty.getName())) {
                                    kieSession.delete((FactHandle) factHandle);
                                    logger.info("Deleting: " + ((FactHandle) factHandle).toString());
                                }

                                logger.info("Inserting KT: " + tempKnowledgeTriple.getSubject().getName() + ", " + tempKnowledgeTriple.getPredicate().getName() + ", " + tempKnowledgeTriple.getObject().getName());

                                kieSession.insert(tempKnowledgeTriple);

                            }

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.severe("Problem with: " + e.getMessage());
                    }

                });

            }

            String combiningAlgorithm = null;
            if (null != policies && !policies.isEmpty()) {

                for (Policy policy : policies) {

                    switch (policy.getPolicycombiningalgorithm()) {
                        case "xca:permitOverrides":
                            combiningAlgorithm = "combiningC";
                            rulelist.add("combining-DefaultPermitAll");
                            break;
                        case "xca:denyOverrides":
                            combiningAlgorithm = "combiningA";
                            rulelist.add("combining-DefaultDenyAll");
                            break;
                        case "xca:denyUnlessPermit":
                            combiningAlgorithm = "combiningC";
                            rulelist.add("combining-DefaultPermitAll");
                            break;
                        case "xca:permitUnlessDeny":
                            combiningAlgorithm = "combiningA";
                            rulelist.add("combining-DefaultDenyAll");
                            break;
                    }

                }

            }

            if (null != combiningAlgorithm) {
                logger.info("Using Combining Algorithm: " + combiningAlgorithm);
                rulelist.add(combiningAlgorithm);
            } else {
                rulelist.add("combining-DefaultPermitAll");
                rulelist.add("combining-DefaultDenyAll");
            }

            kieSession.fireAllRules(new AgendaFilter() {
                public boolean accept(Match match) {
                    String rulename = match.getRule().getName();
                    for (String activerule : rulelist) {
                        if (rulename.startsWith(activerule) || rulename.equals(activerule)) {
                            logger.info("Fired rule: " + activerule + ", " + rulename);
                            return true;
                        }
                    }//for
                    return false;
                }
            });

            //****** get Result Advice
            CombinedAdvice retadv = null;
            for (FactHandle handle : ksession.getFactHandles(new ObjectFilter() {
                public boolean accept(Object object) {

//                    logger.info("Checking: " + object.getClass().getName() + ", RequestID: " + ((CombinedAdvice) object).getRequestid());

                    if (CombinedAdvice.class.equals(object.getClass()) && ((CombinedAdvice) object).getRequestid().equalsIgnoreCase(requestid)) {
                        return true;
                    }
                    return false;
                }
            })) {
                retadv = (CombinedAdvice) ksession.getObject(handle);
                logger.info("CombinedAdvice: " + retadv.getAdvice());
                if (retadv != null) {
                    ret = retadv.getAdvice();
                }
                break;
            }

//            Advice retadv = null;
//            for (FactHandle handle : ksession.getFactHandles(new ObjectFilter() {
//                public boolean accept(Object object) {
//                    if (Advice.class.equals(object.getClass()) && ((Advice) object).getRequestid().equalsIgnoreCase(requestid)) {
//                        return true;
//                    }
//                    return false;
//                }
//            })) {
//                retadv = (Advice) ksession.getObject(handle);
//                logger.info("Advice: " + retadv.getAdvice());
//                if (retadv != null) {
//                    ret = retadv.getAdvice();
//                }
//
//            }//synchronized

        }

        return ret;
    }//EoM

    public static Object getObjectFromAgenda(KieSession ksession, Class cl, String objectname) {
        Object obj = null;

        Collection<FactHandle> factHandles = ksession.getFactHandles(new ObjectFilter() {
            public boolean accept(Object object) {

                if (null != objectname && !objectname.isEmpty() && object.getClass().equals(cl) && ReflectionUtil.getNameOfObject(object).equalsIgnoreCase(objectname)) {
                    return true;
                }
                return false;
            }
        });
        logger.info("Query for " + objectname + "(" + cl.getName() + ") returned: " + factHandles.size());
        if (!factHandles.isEmpty()) {
            return factHandles.iterator().next();
        }
        return obj;
    }//EoM

    public static List<Object> getKnowledgeTriplesForDomainPredicate(KieSession ksession, String domainArg, String predicateArg) {

        List<Object> listOfObjects = new ArrayList<>();

        Collection<FactHandle> factHandles = ksession.getFactHandles(new ObjectFilter() {
            public boolean accept(Object object) {

                if (null != domainArg && !domainArg.isEmpty() && null != predicateArg && !predicateArg.isEmpty()
                        && object.getClass().equals(KnowledgeTriple.class)
                        && ReflectionUtil.getNameOfObject(((KnowledgeTriple) object).getSubject()).equalsIgnoreCase(domainArg)
                        && ReflectionUtil.getNameOfObject(((KnowledgeTriple) object).getPredicate()).equalsIgnoreCase(predicateArg)) {
                    return true;
                }
                return false;
            }
        });
        logger.info("Query for KT with domain: " + domainArg + ", predicate " + predicateArg + "(" + KnowledgeTriple.class.getName() + ") returned: " + factHandles.size());
        if (!factHandles.isEmpty()) {
            listOfObjects.addAll(factHandles);
//            return factHandles.iterator().next();
        }

        return listOfObjects;

    }

}//EoC
