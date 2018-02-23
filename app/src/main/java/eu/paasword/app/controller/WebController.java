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
package eu.paasword.app.controller;

import java.util.*;
import java.util.logging.Logger;

import com.amazonaws.services.opsworks.model.App;
import eu.paasword.api.repository.*;
import eu.paasword.api.repository.exception.application.ApplicationDoesNotExist;
import eu.paasword.api.repository.exception.applicationInstance.ApplicationInstanceDoesNotExist;
import eu.paasword.api.repository.exception.clazz.ClassNameDoesNotExist;
import eu.paasword.api.repository.exception.clazz.ClazzDoesNotExist;
import eu.paasword.api.repository.exception.expression.ExpressionDoesNotExist;
import eu.paasword.api.repository.exception.handler.HandlerDoesNotExist;
import eu.paasword.api.repository.exception.iaasProvider.IaaSProviderDoesNotExist;
import eu.paasword.api.repository.exception.iaasProviderImage.IaaSProviderImageDoesNotExist;
import eu.paasword.api.repository.exception.iaasProviderInstance.IaaSProviderInstanceDoesNotExist;
import eu.paasword.api.repository.exception.instance.InstanceDoesNotExist;
import eu.paasword.api.repository.exception.namespace.NamespaceDoesNotExist;
import eu.paasword.api.repository.exception.paasProvider.PaaSProviderDoesNotExist;
import eu.paasword.api.repository.exception.policy.PolicyDoesNotExist;
import eu.paasword.api.repository.exception.policy.PolicyNameDoesNotExist;
import eu.paasword.api.repository.exception.policySet.PolicySetDoesNotExist;
import eu.paasword.api.repository.exception.policySet.PolicySetNameDoesNotExist;
import eu.paasword.api.repository.exception.property.PropertyDoesNotExist;
import eu.paasword.api.repository.exception.rule.RuleDoesNotExist;
import eu.paasword.api.repository.exception.rule.RuleNameDoesNotExist;
import eu.paasword.api.repository.exception.user.UserDoesNotExist;
import eu.paasword.repository.relational.dao.*;
import eu.paasword.repository.relational.domain.*;
import eu.paasword.repository.relational.util.RepositoryUtil;
import eu.paasword.rest.global.PageWrapper;
import eu.paasword.rest.repository.transferobject.TApplicationInstanceHandler;
import eu.paasword.spi.adapter.ProxyAdapter;
import eu.paasword.spi.model.*;
import eu.paasword.spi.response.BasicResponseCode;
import eu.paasword.spi.response.SPIResponse;
import eu.paasword.util.Util;
import eu.paasword.util.entities.AnnotatedAnnotation;
import eu.paasword.util.entities.AnnotatedCode;
import eu.paasword.util.entities.AnnotatedMethod;
import eu.paasword.util.security.auth.UserAuthentication;

import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.method.P;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.ws.rs.Path;

/**
 * @author smantzouratos
 */
@Controller
public class WebController {

    private static final Logger logger = Logger.getLogger(WebController.class.getName());

    @Resource(name = "proxyAdaptersList")
    private List proxyAdapters;

    @Autowired
    IUserService userService;

    @Autowired
    IClazzService clazzService;

    @Autowired
    IInstanceService<Instance, Clazz> instanceService;

    @Autowired
    IIaaSProviderTypeService iaasProviderTypeService;

    @Autowired
    IIaaSProviderService iaasProviderService;

    @Autowired
    IIaaSProviderImageService iaasProviderImageService;

    @Autowired
    IIaaSProviderInstanceService iaasProviderInstanceService;

    @Autowired
    IPaaSProviderTypeService paasProviderTypeService;

    @Autowired
    IPaaSProviderService<PaaSProvider, User> paasProviderService;

    @Autowired
    IExpressionService expressionService;

    @Autowired
    IRuleService ruleService;

    @Autowired
    IPolicyService policyService;

    @Autowired
    IPolicySetService policySetService;

    @Autowired
    IPropertyTypeService propertyTypeService;

    @Autowired
    IApplicationService applicationService;

    @Autowired
    IPropertyService propertyService;

    @Autowired
    INamespaceService namespaceService;

    @Autowired
    IHandlerService handlerService;

    @Autowired
    ICombiningAlgorithmService combiningAlgorithmService;

    @Autowired
    IAPIKeyService apiKeyService;

    @Autowired
    IApplicationPrivacyConstraintService applicationPrivacyConstraintService;

    @Autowired
    IApplicationAffinityConstraintService applicationAffinityConstraintService;

    @Autowired
    IApplicationInstanceService<ApplicationInstance> applicationInstanceService;

    @Autowired
    IApplicationInstanceActivityService<ApplicationInstanceActivity> applicationInstanceActivityService;

    @Autowired
    IProxyCloudProviderService<ProxyCloudProvider, User> proxyCloudProviderService;

    @Autowired
    IUserCredentialService<UserCredential, ProxyCloudProvider, User> userCredentialService;

    @Autowired
    RuleRepository ruleRepository;

    @Autowired
    ExpressionRepository expressionRepository;

    @Autowired
    PolicyRepository policyRepository;

    @Autowired
    PolicySetRepository policySetRepository;

    @Autowired
    InstanceRepository instanceRepository;

    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    ApplicationInstanceTenantConfigRepository applicationInstanceTenantConfigRepository;

    @Autowired
    ApplicationInstanceDBProxyConfigRepository applicationInstanceDBProxyConfigRepository;

    @Autowired
    ApplicationInstanceUserRepository applicationInstanceUserRepository;

    @Autowired
    IApplicationInstanceHandlerService<ApplicationInstanceHandler, ApplicationInstance> applicationInstanceHandlerService;


    /*
     *  Generic Methods
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String indexView(Model model) {
        return "index";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/login");
        return "redirect:/";
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String registerView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/register");
        return "redirect:/";
    }

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String dashboardView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/dashboard");
        return "redirect:/";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login() {
        return "auth::login";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register() {
        return "auth::register";
    }

    @RequestMapping(value = "/header", method = RequestMethod.POST)
    public String header() {
        return "inc::header";
    }

    @RequestMapping(value = "/footer", method = RequestMethod.POST)
    public String footer() {
        return "inc::footer";
    }

    @RequestMapping(value = "/dashboard", method = RequestMethod.POST)
    public String dashboard(Model model) {

        model.addAttribute("applications", applicationService.findAllWithoutBlob(null).getTotalElements());

        int resources = iaasProviderService.findAll().size() + paasProviderService.findAll().size();
        model.addAttribute("resources", resources);

        return "dashboard::home";
    }

    @RequestMapping(value = "/content", method = RequestMethod.POST)
    public String content() {
        return "inc::content";
    }

    @RequestMapping(value = "/notification", method = RequestMethod.POST)
    public String notification() {
        return "dashboard::notification";
    }

//    @RequestMapping(value = "/search", method = RequestMethod.POST)
//    public String search() {
//        return "hub::search";
//    }

    /*
     * User
     */
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public String userListView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/user");
        return "redirect:/";
    }

    @RequestMapping(value = "/user/add", method = RequestMethod.GET)
    public String userAddView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/user/add");
        return "redirect:/";
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
    public String userEditView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/user/" + id);
        return "redirect:/";
    }

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public String user(Model model, @PageableDefault(size = PageWrapper.MAX_PAGE_ITEM_DISPLAY) Pageable pageable) {
        model.addAttribute("page", new PageWrapper<>(userService.findAll(pageable), "/user"));
        return "user::list";
    }

    @RequestMapping(value = "/user/add", method = RequestMethod.POST)
    public String userAdd() {
        return "user::edit";
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.POST)
    public String userEditView(Model model, @PathVariable("id") long id) {
        try {
            model.addAttribute("user", userService.findOne(id));
        } catch (UserDoesNotExist e) {
            e.printStackTrace();
        }
        return "user::edit";
    }


    /*
     * Documentation
     */
    @RequestMapping(value = "/documentation", method = RequestMethod.GET)
    public String documentationListView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/documentation");
        return "redirect:/";
    }

    @RequestMapping(value = "/documentation", method = RequestMethod.POST)
    public String documentation(Model model) {
        return "documentation::list";
    }

    @RequestMapping(value = "/documentation/webapp", method = RequestMethod.GET)
    public String documentationWebAppView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/documentation/webapp");
        return "redirect:/";
    }

    @RequestMapping(value = "/documentation/webapp", method = RequestMethod.POST)
    public String documentationWebApp(Model model) {
        return "documentation::webapp";
    }

    @RequestMapping(value = "/documentation/jpa", method = RequestMethod.GET)
    public String documentationJPAListView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/documentation/jpa");
        return "redirect:/";
    }

    @RequestMapping(value = "/documentation/jpa", method = RequestMethod.POST)
    public String documentationJPA(Model model) {
        return "documentation::jpa";
    }

    @RequestMapping(value = "/documentation/pep", method = RequestMethod.GET)
    public String documentationPEPListView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/documentation/pep");
        return "redirect:/";
    }

    @RequestMapping(value = "/documentation/pep", method = RequestMethod.POST)
    public String documentationPEP(Model model) {
        return "documentation::pep";
    }

    /*
     * Application
     */
    @RequestMapping(value = "/application", method = RequestMethod.GET)
    public String applicationListView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application");
        return "redirect:/";
    }

    @RequestMapping(value = "/application/add", method = RequestMethod.GET)
    public String applicationAddView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/add");
        return "redirect:/";
    }

    @RequestMapping(value = "/application/{id}", method = RequestMethod.GET)
    public String applicationEditView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id);
        return "redirect:/";
    }

    @RequestMapping(value = "/application", method = RequestMethod.POST)
    public String application(Model model, @PageableDefault(size = PageWrapper.MAX_PAGE_ITEM_DISPLAY) Pageable pageable) {

        model.addAttribute("page", new PageWrapper<>(applicationRepository.findAllByOrderByNameAsc(pageable), "/application"));

        return "application::list";
    }

    @RequestMapping(value = "/application/add", method = RequestMethod.POST)
    public String applicationAddView(Model model) {
        return "application::add";
    }

    @RequestMapping(value = "/application/{id}", method = RequestMethod.POST)
    public String applicationEditView(Model model, @PathVariable("id") long id) {
        try {

            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));

        } catch (ApplicationDoesNotExist e) {
            e.printStackTrace();
        }
        return "application::edit";
    }

    @RequestMapping(value = "/application/{id}/appkey", method = RequestMethod.GET)
    public String applicationAPIKeysView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/appkey");
        return "redirect:/";
    }

    @RequestMapping(value = "/application/{id}/appkey", method = RequestMethod.POST)
    public String applicationAPIKeysList(Model model, @PathVariable("id") long id) {
        try {
            model.addAttribute("apiKeys", apiKeyService.findByApplicationID(id));
            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));
        } catch (ApplicationDoesNotExist e) {
            e.printStackTrace();
        }
        return "application::apikey";
    }

    @RequestMapping(value = "/application/{id}/privacy", method = RequestMethod.GET)
    public String applicationPrivacyConstraintsView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/privacy");
        return "redirect:/";
    }

    @RequestMapping(value = "/application/{id}/privacy", method = RequestMethod.POST)
    public String applicationPrivacyConstraintsList(Model model, @PathVariable("id") long id) {
        try {

            Application app = (Application) applicationService.findOneWithoutBlob(id).get();

            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));

            if (app.isDataModel()) {
                model.addAttribute("isDataModel", true);
                model.addAttribute("privacyConstraints", applicationPrivacyConstraintService.findByApplicationID(app.getId()));
            } else {
                model.addAttribute("isDataModel", false);
            }

        } catch (ApplicationDoesNotExist e) {
            e.printStackTrace();
        }
        return "application::privacy";
    }

    @RequestMapping(value = "/application/{id}/privacy/add", method = RequestMethod.GET)
    public String applicationAddPrivacyConstraintView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/privacy/add");
        return "redirect:/";
    }


    @RequestMapping(value = "/application/{id}/privacy/add", method = RequestMethod.POST)
    public String applicationAddPrivacyConstraints(Model model, @PathVariable("id") long id) {
        try {

            Application app = (Application) applicationService.findOneWithoutBlob(id).get();

            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));

            if (app.isDataModel()) {
                model.addAttribute("isDataModel", true);

//                List<AnnotatedCode> annotatedCode = Util.parseAnnotatedSourceCodeJSONOnlyForDataModels(app.getAnnotatedCodeDataModel());

                if (!app.getAnnotatedCodePEP().isEmpty()) {

//                    List<String> privacyConstraints = new ArrayList<>();
//
//                    annotatedCode.stream().forEach(code -> {
//
//                        code.getFields().stream().forEach(field -> {
//
//                             privacyConstraints.add(code.getName().substring(code.getName().lastIndexOf(".") + 1) + "." + field.getName());
//
//                        });
//
//                    });

                    List<String> fields = new ArrayList<>();
                    JSONArray fieldsArray = new JSONObject(app.getAnnotatedCodePEP()).getJSONObject("dbProxy").getJSONArray("fields");
                    for (Object field : fieldsArray) {
                        fields.add((String) field);
                    }

                    model.addAttribute("privacyConstraints", fields);
                }

            } else {
                model.addAttribute("isDataModel", false);
            }

        } catch (ApplicationDoesNotExist e) {
            e.printStackTrace();
        }
        return "application::privacy-add";
    }

    @RequestMapping(value = "/application/{id}/privacy/{privacyConstraintID}", method = RequestMethod.GET)
    public String applicationEditPrivacyConstraintView(@PathVariable("id") long id, @PathVariable("privacyConstraintID") long privacyConstraintID, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/privacy/" + privacyConstraintID);
        return "redirect:/";
    }


    @RequestMapping(value = "/application/{id}/privacy/{privacyConstraintID}", method = RequestMethod.POST)
    public String applicationEditPrivacyConstraints(Model model, @PathVariable("id") long id, @PathVariable("privacyConstraintID") long privacyConstraintID) {
        try {

            Application app = (Application) applicationService.findOneWithoutBlob(id).get();

            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));

            if (app.isDataModel()) {
                model.addAttribute("isDataModel", true);


                List<AnnotatedCode> annotatedCode = Util.parseAnnotatedSourceCodeJSONOnlyForDataModels(app.getAnnotatedCodeDataModel());

                if (!annotatedCode.isEmpty()) {

//                    List<String> privacyConstraints = new ArrayList<>();
//
//                    annotatedCode.stream().forEach(code -> {
//
//                        code.getFields().stream().forEach(field -> {
//
//                            privacyConstraints.add(code.getName().substring(code.getName().lastIndexOf(".") + 1) + "." + field.getName());
//
//                        });
//
//                    });
//
//                    model.addAttribute("privacyConstraints", privacyConstraints);

                    List<String> fields = new ArrayList<>();
                    JSONArray fieldsArray = new JSONObject(app.getAnnotatedCodePEP()).getJSONObject("dbProxy").getJSONArray("fields");
                    for (Object field : fieldsArray) {
                        fields.add((String) field);
                    }

                    model.addAttribute("privacyConstraints", fields);
                }

                model.addAttribute("privacyConstraint", applicationPrivacyConstraintService.findOneWithoutApplication(privacyConstraintID));

            } else {
                model.addAttribute("isDataModel", false);
            }

        } catch (ApplicationDoesNotExist e) {
            e.printStackTrace();
        }
        return "application::privacy-edit";
    }

    @RequestMapping(value = "/application/{id}/affinity", method = RequestMethod.GET)
    public String applicationAffinityConstraintsView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/affinity");
        return "redirect:/";
    }

    @RequestMapping(value = "/application/{id}/affinity", method = RequestMethod.POST)
    public String applicationAffinityConstraintsList(Model model, @PathVariable("id") long id) {
        try {

            Application app = (Application) applicationService.findOneWithoutBlob(id).get();

            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));

            if (app.isDataModel()) {
                model.addAttribute("isDataModel", true);
                model.addAttribute("affinityConstraints", applicationAffinityConstraintService.findByApplicationID(app.getId()));
            } else {
                model.addAttribute("isDataModel", false);
            }

        } catch (ApplicationDoesNotExist e) {
            e.printStackTrace();
        }
        return "application::affinity";
    }

    @RequestMapping(value = "/application/{id}/affinity/add", method = RequestMethod.GET)
    public String applicationAddAffinityConstraintView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/affinity/add");
        return "redirect:/";
    }


    @RequestMapping(value = "/application/{id}/affinity/add", method = RequestMethod.POST)
    public String applicationAddAffinityConstraints(Model model, @PathVariable("id") long id) {
        try {

            Application app = (Application) applicationService.findOneWithoutBlob(id).get();

            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));

            if (app.isDataModel()) {
                model.addAttribute("isDataModel", true);

//                List<AnnotatedCode> annotatedCode = Util.parseAnnotatedSourceCodeJSONOnlyForDataModels(app.getAnnotatedCodeDataModel());

                if (!app.getAnnotatedCodePEP().isEmpty()) {

//                    List<String> privacyConstraints = new ArrayList<>();
//
//                    annotatedCode.stream().forEach(code -> {
//
//                        code.getFields().stream().forEach(field -> {
//
//                             privacyConstraints.add(code.getName().substring(code.getName().lastIndexOf(".") + 1) + "." + field.getName());
//
//                        });
//
//                    });

                    List<String> fields = new ArrayList<>();
                    JSONArray fieldsArray = new JSONObject(app.getAnnotatedCodePEP()).getJSONObject("dbProxy").getJSONArray("fields");
                    for (Object field : fieldsArray) {
                        fields.add((String) field);
                    }

                    model.addAttribute("affinityConstraints", fields);
                }

            } else {
                model.addAttribute("isDataModel", false);
            }

        } catch (ApplicationDoesNotExist e) {
            e.printStackTrace();
        }
        return "application::affinity-add";
    }

    @RequestMapping(value = "/application/{id}/affinity/{affinityConstraintID}", method = RequestMethod.GET)
    public String applicationEditAffinityConstraintView(@PathVariable("id") long id, @PathVariable("affinityConstraintID") long affinityConstraintID, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/affinity/" + affinityConstraintID);
        return "redirect:/";
    }


    @RequestMapping(value = "/application/{id}/affinity/{affinityConstraintID}", method = RequestMethod.POST)
    public String applicationEditAffinityConstraint(Model model, @PathVariable("id") long id, @PathVariable("affinityConstraintID") long affinityConstraintID) {
        try {

            Application app = (Application) applicationService.findOneWithoutBlob(id).get();

            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));

            if (app.isDataModel()) {
                model.addAttribute("isDataModel", true);


                List<AnnotatedCode> annotatedCode = Util.parseAnnotatedSourceCodeJSONOnlyForDataModels(app.getAnnotatedCodeDataModel());

                if (!annotatedCode.isEmpty()) {

//                    List<String> privacyConstraints = new ArrayList<>();
//
//                    annotatedCode.stream().forEach(code -> {
//
//                        code.getFields().stream().forEach(field -> {
//
//                            privacyConstraints.add(code.getName().substring(code.getName().lastIndexOf(".") + 1) + "." + field.getName());
//
//                        });
//
//                    });
//
//                    model.addAttribute("privacyConstraints", privacyConstraints);

                    List<String> fields = new ArrayList<>();
                    JSONArray fieldsArray = new JSONObject(app.getAnnotatedCodePEP()).getJSONObject("dbProxy").getJSONArray("fields");
                    for (Object field : fieldsArray) {
                        fields.add((String) field);
                    }

                    model.addAttribute("affinityConstraints", fields);
                }

                model.addAttribute("affinityConstraint", applicationAffinityConstraintService.findOneWithoutApplication(affinityConstraintID));

            } else {
                model.addAttribute("isDataModel", false);
            }

        } catch (ApplicationDoesNotExist e) {
            e.printStackTrace();
        }
        return "application::affinity-edit";
    }

    @RequestMapping(value = "/application/{id}/instance", method = RequestMethod.GET)
    public String applicationApplicationInstancesView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/instance");
        return "redirect:/";
    }

    @RequestMapping(value = "/application/{id}/instance", method = RequestMethod.POST)
    public String applicationApplicationInstancesList(Model model, @PathVariable("id") long id) {
        try {

            Application currentApp = (Application) applicationService.findOneWithoutBlob(id).get();

            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));

            if (currentApp.isDataModel()) {
                model.addAttribute("privacyConstraints", applicationPrivacyConstraintService.findByApplicationID(currentApp.getId()));
                model.addAttribute("affinityConstraints", applicationAffinityConstraintService.findByApplicationID(currentApp.getId()));
            }

            model.addAttribute("appInstance", applicationInstanceService.findByApplicationID(currentApp.getId()));

        } catch (ApplicationDoesNotExist e) {
            e.printStackTrace();
        }
        return "application::instance";
    }


    @RequestMapping(value = "/application/{id}/instance/new", method = RequestMethod.GET)
    public String applicationApplicationInstancesNewView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/instance/new");
        return "redirect:/";
    }


    @RequestMapping(value = "/application/{id}/instance/new", method = RequestMethod.POST)
    public String applicationApplicationInstancesNew(Model model, @PathVariable("id") long id) {

        try {

            Application app = (Application) applicationService.findOneWithoutBlob(id).get();

            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));

            if (app.isDataModel()) {
                model.addAttribute("isDataModel", true);
            } else {
                model.addAttribute("isDataModel", false);
            }

            if (app.isPep()) {
                model.addAttribute("isPEP", true);
            } else {
                model.addAttribute("isPEP", false);
            }


        } catch (ApplicationDoesNotExist e) {
            e.printStackTrace();
        }

        return "application::instance-new";
    }

    @RequestMapping(value = "/application/{id}/instance/validate", method = RequestMethod.GET)
    public String applicationApplicationInstancesValidateView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/instance/validate");
        return "redirect:/";
    }


    @RequestMapping(value = "/application/{id}/instance/validate", method = RequestMethod.POST)
    public String applicationApplicationInstancesValidate(Model model, @PathVariable("id") long id) {
        try {

            Application app = (Application) applicationService.findOneWithoutBlob(id).get();

            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));

            model.addAttribute("paasProviders", paasProviderService.findAll());

            if (app.isDataModel()) {
                model.addAttribute("isDataModel", true);
                model.addAttribute("privacyConstraints", applicationPrivacyConstraintService.findByApplicationID(app.getId()));
                model.addAttribute("affinityConstraints", applicationAffinityConstraintService.findByApplicationID(app.getId()));
            } else {
                model.addAttribute("isDataModel", false);
            }

        } catch (ApplicationDoesNotExist e) {
            e.printStackTrace();
        }
        return "application::instance-add";
    }

    @RequestMapping(value = "/application/{id}/instance/{instanceID}", method = RequestMethod.GET)
    public String applicationApplicationInstanceInfoView(@PathVariable("id") long id, @PathVariable("instanceID") long instanceID, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/instance/" + instanceID);
        return "redirect:/";
    }


    @RequestMapping(value = "/application/{id}/instance/{instanceID}", method = RequestMethod.POST)
    public String applicationApplicationInstanceInfo(Model model, @PathVariable("id") long id, @PathVariable("instanceID") long instanceID) {
        try {

            Application app = (Application) applicationService.findOneWithoutBlob(id).get();

            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));

            ApplicationInstance appInstance = (ApplicationInstance) applicationInstanceService.findOneWithoutApplication(instanceID);

            model.addAttribute("appInstance", appInstance);

            APIKey apiKey = (APIKey) apiKeyService.findByApplicationID(app.getId()).get(0);
            model.addAttribute("apiKey", apiKey.getUniqueID());

        } catch (ApplicationDoesNotExist e) {
            e.printStackTrace();
        }

        return "application::instance-info";
    }

//    @RequestMapping(value = "/application/{id}/instance/{instanceID}", method = RequestMethod.GET)
//    public String applicationApplicationInstanceEditView(@PathVariable("id") long id, @PathVariable("instanceID") long instanceID, final RedirectAttributes redirectAttributes) {
//        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/instance/" + instanceID);
//        return "redirect:/";
//    }
//
//
//    @RequestMapping(value = "/application/{id}/instance/{instanceID}", method = RequestMethod.POST)
//    public String applicationApplicationInstanceEdit(Model model, @PathVariable("id") long id, @PathVariable("instanceID") long instanceID) {
//        try {
//
//            Application app = (Application) applicationService.findOneWithoutBlob(id).get();
//
//            APIKey apiKey = (APIKey) apiKeyService.findByApplicationID(app.getId()).get(0);
//
//            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));
//
//            ApplicationInstance appInstance = (ApplicationInstance) applicationInstanceService.findOneWithoutApplication(instanceID);
//
//            model.addAttribute("apiKey", apiKey.getUniqueID());
//
//            if (appInstance.getOverallStatus() == 3) {
//
//                model.addAttribute("appInstance", applicationInstanceService.findOneWithPaaSProviderAndOnlyWithApplicationID(instanceID));
//
//            } else {
//
//                model.addAttribute("appInstance", applicationInstanceService.findOneWithoutApplication(instanceID));
//
//            }
//
//            model.addAttribute("iaasProviders", iaasProviderService.findAll());
//
//            model.addAttribute("paasProviders", paasProviderService.findAll());
//
//            if (app.isDataModel()) {
//                model.addAttribute("isDataModel", true);
//                model.addAttribute("privacyConstraints", applicationPrivacyConstraintService.findByApplicationID(app.getId()));
//            } else {
//                model.addAttribute("isDataModel", false);
//            }
//
//        } catch (ApplicationDoesNotExist e) {
//            e.printStackTrace();
//        }
//
//        return "application::instance-edit";
//    }


    @RequestMapping(value = "/application/{id}/instance/{instanceID}/keymgmt", method = RequestMethod.GET)
    public String applicationKeyManagementView(@PathVariable("id") long id, @PathVariable("instanceID") long instanceID, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/instance/" + instanceID + "/keymgmt");
        return "redirect:/";
    }

    @RequestMapping(value = "/application/{id}/instance/{instanceID}/keymgmt", method = RequestMethod.POST)
    public String applicationKeyManagementList(Model model, @PathVariable("id") long id, @PathVariable("instanceID") long instanceID, @PageableDefault(size = PageWrapper.MAX_PAGE_ITEM_DISPLAY) Pageable pageable) {

        try {

            Application app = (Application) applicationService.findOneWithoutBlob(id).get();
            ApplicationInstance applicationInstance = applicationInstanceService.findOneWithoutApplication(instanceID);

            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));
            model.addAttribute("appInstance", applicationInstance);

            // Check if key management is required
            if (app.isDataModel()) {
                model.addAttribute("isDataModel", true);

                // Check for DB Proxy
                if (applicationInstance.getOverallStatus() >= 5 && applicationInstance.getOverallStatus() != 6) {

                    model.addAttribute("keyMgmtConfigured", true);

                    // Fetch application instance users
                    model.addAttribute("page", new PageWrapper<>(applicationInstanceUserRepository.findByApplicationInstanceID(applicationInstance.getId(), pageable), "/application/" + id + "/instance/"  + instanceID + "/keymgmt"));

                } else {

                    model.addAttribute("keyMgmtConfigured", false);

                }


            } else {
                model.addAttribute("isDataModel", false);
            }


        } catch (ApplicationDoesNotExist e) {
            e.printStackTrace();
        }

        return "application::instance-keymgmt";
    }

    @RequestMapping(value = "/application/{id}/instance/{instanceID}/keymgmt/adduser", method = RequestMethod.GET)
    public String applicationKeyManagementAddUserView(@PathVariable("id") long id, @PathVariable("instanceID") long instanceID, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/instance/" + instanceID + "/keymgmt/adduser");
        return "redirect:/";
    }

    @RequestMapping(value = "/application/{id}/instance/{instanceID}/keymgmt/adduser", method = RequestMethod.POST)
    public String applicationKeyManagementAddUser(Model model, @PathVariable("id") long id, @PathVariable("instanceID") long instanceID, @PageableDefault(size = PageWrapper.MAX_PAGE_ITEM_DISPLAY) Pageable pageable) {

        try {

            Application app = (Application) applicationService.findOneWithoutBlob(id).get();
            ApplicationInstance applicationInstance = applicationInstanceService.findOneWithoutApplication(instanceID);

            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));
            model.addAttribute("appInstance", applicationInstance);

            // Check if key management is required
            if (app.isDataModel()) {
                model.addAttribute("isDataModel", true);

                // Check if key management is initialized or not
                if (applicationInstance.getOverallStatus() >= 5 && applicationInstance.getOverallStatus() != 6) {

                    model.addAttribute("keyMgmtConfigured", true);

                } else {

                    model.addAttribute("keyMgmtConfigured", false);

                }

            } else {
                model.addAttribute("isDataModel", false);
            }


        } catch (ApplicationDoesNotExist e) {
            e.printStackTrace();
        }

        return "application::instance-keymgmt-add-user";
    }

    @RequestMapping(value = "/application/{id}/instance/{instanceID}/handler", method = RequestMethod.GET)
    public String applicationHandlerView(@PathVariable("id") long id, @PathVariable("instanceID") long instanceID, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/instance/" + instanceID + "/handler");
        return "redirect:/";
    }

    @RequestMapping(value = "/application/{id}/instance/{instanceID}/handler", method = RequestMethod.POST)
    public String applicationHandlerList(Model model, @PathVariable("id") long id, @PathVariable("instanceID") long instanceID) {

        try {

            ApplicationInstance appInstance = applicationInstanceService.findOne(instanceID).get();

            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));
            model.addAttribute("appInstance", appInstance);

            List<Handler> neededHandlers = RepositoryUtil.identifyHandlersPerApplication((Application) applicationService.findOneWithoutBlob(id).get(), expressionRepository, ruleRepository, policyRepository, policySetRepository, instanceRepository, propertyRepository);


//            List<ApplicationInstanceHandler> applicationInstanceHandlers = applicationInstanceHandlerService.findByApplicationInstanceID(appInstance.getId());
            List<ApplicationInstanceHandler> applicationInstanceHandlers = appInstance.getApplicationInstanceHandlers();

            List<Handler> existingHandlers = handlerService.findAll();

            List<Handler> missingHandlers = new ArrayList<>();

            List<Handler> identifiedHandlers = new ArrayList<>();

            List<Handler> newNeededHandlers = new ArrayList<>();

            neededHandlers.stream().forEach(handler -> {

                if (!newNeededHandlers.contains(handler)) {
                    newNeededHandlers.add(handler);
                }

            });

            if (null == applicationInstanceHandlers || applicationInstanceHandlers.isEmpty()) {

                if (!neededHandlers.isEmpty()) {

                    // Check if needed handlers exist
                    neededHandlers.stream().forEach(neededHandler -> {

                        if (!existingHandlers.isEmpty()) {

                            existingHandlers.stream().forEach(existingHandler -> {

                                if (existingHandler.getHasInput().getId() == neededHandler.getHasInput().getId() && existingHandler.getHasOutput().getId() == neededHandler.getHasOutput().getId()) {

//                                    logger.info("Existing handler for: " + existingHandler.getHasInput().getClassName() + ", " + existingHandler.getHasOutput().getClassName());

                                    // Handler exists
                                    if (!identifiedHandlers.contains(existingHandler)) {
//                                        logger.info("Identified handler for : " + existingHandler.getHasInput().getClassName() + ", " + existingHandler.getHasOutput().getClassName());
                                        identifiedHandlers.add(existingHandler);
                                    }

                                    newNeededHandlers.remove(neededHandler);

                                }

                            });

                        }

                    });

                }

                if (!newNeededHandlers.isEmpty()) {

                    newNeededHandlers.stream().forEach(neededHandler -> {


                        if (!missingHandlers.contains(neededHandler)) {
                            missingHandlers.add(neededHandler);
                        }

                    });

                }


                model.addAttribute("missingHandlers", missingHandlers);

                model.addAttribute("identifiedHandlers", identifiedHandlers);

            } else {

                model.addAttribute("assignedHandlers", applicationInstanceHandlers);

                if (!neededHandlers.isEmpty()) {

                    // Check if needed handlers exist
                    neededHandlers.stream().forEach(neededHandler -> {

                        if (!applicationInstanceHandlers.stream().filter(applicationInstanceHandler ->
                                applicationInstanceHandler.getHandlerID().getHasInput().getId() == neededHandler.getHasInput().getId()
                                        && applicationInstanceHandler.getHandlerID().getHasOutput().getId() == neededHandler.getHasOutput().getId()
                        ).collect(Collectors.toList()).isEmpty()) {

                            // DO NOTHING

                        } else {

                            if (!existingHandlers.isEmpty()) {

                                existingHandlers.stream().forEach(existingHandler -> {

                                    if (existingHandler.getHasInput().getId() == neededHandler.getHasInput().getId() && existingHandler.getHasOutput().getId() == neededHandler.getHasOutput().getId()) {

                                        // Handler exists
                                        if (!identifiedHandlers.contains(existingHandler)) {
//                                            logger.info("Identified handler for : " + existingHandler.getHasInput().getClassName() + ", " + existingHandler.getHasOutput().getClassName());
                                            identifiedHandlers.add(existingHandler);
                                        }

                                        newNeededHandlers.remove(neededHandler);

                                    }

                                });

                            }

                        }

                    });

                }

                if (!newNeededHandlers.isEmpty()) {

                    newNeededHandlers.stream().forEach(neededHandler -> {
                        if (!missingHandlers.contains(neededHandler)) {

                            // Check if already exist
                            if (applicationInstanceHandlers.stream().filter(applicationInstanceHandler -> applicationInstanceHandler.getHandlerID().getHasInput().getId() == neededHandler.getHasInput().getId()
                                    && applicationInstanceHandler.getHandlerID().getHasOutput().getId() == neededHandler.getHasOutput().getId()).collect(Collectors.toList()).isEmpty()) {
                                missingHandlers.add(neededHandler);
                            }


                        }

                    });

                }

                model.addAttribute("missingHandlers", missingHandlers);

                model.addAttribute("identifiedHandlers", identifiedHandlers);
            }

        } catch (ApplicationDoesNotExist | ApplicationInstanceDoesNotExist e) {
            e.printStackTrace();
        }

        return "application::instance-handler";
    }

    @RequestMapping(value = "/application/{id}/instance/{instanceID}/proxy", method = RequestMethod.GET)
    public String applicationApplicationInstanceDBProxyView(@PathVariable("id") long id, @PathVariable("instanceID") long instanceID, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/instance/" + instanceID + "/proxy");
        return "redirect:/";
    }


    @RequestMapping(value = "/application/{id}/instance/{instanceID}/proxy", method = RequestMethod.POST)
    public String applicationApplicationInstanceProxy(Model model, @PathVariable("id") long id, @PathVariable("instanceID") long instanceID) {

        try {

            Application app = (Application) applicationService.findOneWithoutBlob(id).get();

            ApplicationInstance appInstance = (ApplicationInstance) applicationInstanceService.findOneWithoutApplication(instanceID);

            boolean dbProxyReady = false;

            if (appInstance.getOverallStatus() <= 12) {

                if (app.isDataModel()) {

                    // Check for DB Proxy
                    if (appInstance.getOverallStatus() >= 5 && appInstance.getOverallStatus() != 6) {
                        dbProxyReady = true;
                    }

                    model.addAttribute("privacyConstraints", applicationPrivacyConstraintService.findByApplicationID(app.getId()));
                    model.addAttribute("affinityConstraints", applicationAffinityConstraintService.findByApplicationID(app.getId()));

                }

                if (appInstance.getOverallStatus() >= 3) {
                    model.addAttribute("iaasProviders", iaasProviderService.findAll());
                    model.addAttribute("proxyCloudProviders", proxyCloudProviderService.findAll());
                }

                model.addAttribute("isDataModel", app.isDataModel());
                model.addAttribute("dbProxyReady", dbProxyReady);

                model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));
                model.addAttribute("appInstance", applicationInstanceService.findOneWithoutApplication(instanceID));

                APIKey apiKey = (APIKey) apiKeyService.findByApplicationID(app.getId()).get(0);
                model.addAttribute("apiKey", apiKey.getUniqueID());


            } else {

                // TODO
                // Either error or deployed


            }

        } catch (ApplicationDoesNotExist e) {
            e.printStackTrace();
        }

        return "application::instance-proxy";

    }

    @RequestMapping(value = "/application/{id}/instance/{instanceID}/deploy", method = RequestMethod.GET)
    public String applicationApplicationInstanceDeployView(@PathVariable("id") long id, @PathVariable("instanceID") long instanceID, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/instance/" + instanceID + "/deploy");
        return "redirect:/";
    }


    @RequestMapping(value = "/application/{id}/instance/{instanceID}/deploy", method = RequestMethod.POST)
    public String applicationApplicationInstanceDeploy(Model model, @PathVariable("id") long id, @PathVariable("instanceID") long instanceID) {

        try {

            Application app = (Application) applicationService.findOneWithoutBlob(id).get();

            ApplicationInstance appInstance = applicationInstanceService.findOne(instanceID).get();

            boolean pepsReady = false;
            boolean dbProxyReady = false;
            boolean handlersReady = false;

            if (appInstance.getOverallStatus() < 9) {

                if (app.isDataModel()) {

                    // Check for DB Proxy
                    if (appInstance.getOverallStatus() >= 5 && appInstance.getOverallStatus() != 6) {
                        dbProxyReady = true;
                    }

                } else {
                    dbProxyReady = true;
                }

                if (app.isPep()) {

                    // Check for red flags in PEPs
                    int redFlags = 0;

                    List<AnnotatedCode> peps = Util.parseAnnotatedSourceCodeJSONOnlyForPEPs(app.getAnnotatedCodePEP());

                    if (null != peps & !peps.isEmpty()) {

                        for (AnnotatedCode pep : peps) {

                            List<AnnotatedAnnotation> classAnnotation = pep.getAnnotations();

                            if (null != classAnnotation && !classAnnotation.isEmpty()) {

                                for (AnnotatedAnnotation annot : classAnnotation) {

                                    String value = annot.getValue().substring(1, annot.getValue().length() - 1);

                                    switch (annot.getType()) {
                                        case "RULE":
                                            try {
                                                Rule existingRule = (Rule) ruleService.findByRuleName(value).get();
                                                if (null == existingRule) {
                                                    annot.setExists(false);
                                                    annot.setRedirectURL("/model/rule/add?name=" + value + "&amp;object=" + pep.getName());
                                                    redFlags++;
                                                }
                                            } catch (RuleNameDoesNotExist e) {
//                                            e.printStackTrace();
                                                annot.setExists(false);
                                                annot.setRedirectURL("/model/rule/add?name=" + value + "&amp;object=" + pep.getName());
                                                redFlags++;
                                            }
                                            break;
                                        case "POLICY":
                                            try {
                                                Policy existingPolicy = (Policy) policyService.findByPolicyName(value).get();
                                                if (null == existingPolicy) {
                                                    annot.setExists(false);
                                                    annot.setRedirectURL("/model/policy/add?name=" + value);
                                                    redFlags++;
                                                }
                                            } catch (PolicyNameDoesNotExist e) {
//                                            e.printStackTrace();
                                                annot.setExists(false);
                                                annot.setRedirectURL("/model/policy/add?name=" + value);
                                                redFlags++;
                                            }
                                            break;
                                        case "POLICY_SET":
                                            try {
                                                PolicySet existingPolicySet = (PolicySet) policySetService.findByPolicySetName(value).get();
                                                if (null == existingPolicySet) {
                                                    annot.setExists(false);
                                                    annot.setRedirectURL("/model/policyset/add?name=" + value);
                                                    redFlags++;
                                                }
                                            } catch (PolicySetNameDoesNotExist e) {
//                                            e.printStackTrace();
                                                annot.setExists(false);
                                                annot.setRedirectURL("/model/policyset/add?name=" + value);
                                                redFlags++;
                                            }
                                            break;
                                    }

                                }

                            }

                            List<AnnotatedMethod> methods = pep.getMethods();

                            if (null != methods && !methods.isEmpty()) {

                                for (AnnotatedMethod method : methods) {

                                    for (AnnotatedAnnotation annot : method.getMethodAnnotations()) {

                                        String value = annot.getValue().substring(1, annot.getValue().length() - 1);

                                        switch (annot.getType()) {
                                            case "RULE":
                                                try {
                                                    Rule existingRule = (Rule) ruleService.findByRuleName(value).get();
                                                    if (null == existingRule) {
                                                        annot.setExists(false);
                                                        annot.setRedirectURL("/model/rule/add?name=" + value + "&amp;object=" + pep.getName() + "." + method.getName());
                                                        redFlags++;
                                                    }
                                                } catch (RuleNameDoesNotExist e) {
//                                                e.printStackTrace();
                                                    annot.setExists(false);
                                                    annot.setRedirectURL("/model/rule/add?name=" + value + "&amp;object=" + pep.getName() + "." + method.getName());
                                                    redFlags++;
                                                }
                                                break;
                                            case "POLICY":
                                                try {
                                                    Policy existingPolicy = (Policy) policyService.findByPolicyName(value).get();
                                                    if (null == existingPolicy) {
                                                        annot.setExists(false);
                                                        annot.setRedirectURL("/model/policy/add?name=" + value);
                                                        redFlags++;
                                                    }
                                                } catch (PolicyNameDoesNotExist e) {
//                                                e.printStackTrace();
                                                    annot.setExists(false);
                                                    annot.setRedirectURL("/model/policy/add?name=" + value);
                                                    redFlags++;
                                                }
                                                break;
                                            case "POLICY_SET":
                                                try {
                                                    PolicySet existingPolicySet = (PolicySet) policySetService.findByPolicySetName(value).get();
                                                    if (null == existingPolicySet) {
                                                        annot.setExists(false);
                                                        annot.setRedirectURL("/model/policyset/add?name=" + value);
                                                        redFlags++;
                                                    }
                                                } catch (PolicySetNameDoesNotExist e) {
//                                                e.printStackTrace();
                                                    annot.setExists(false);
                                                    annot.setRedirectURL("/model/policyset/add?name=" + value);
                                                    redFlags++;
                                                }
                                                break;
                                        }

                                    }

                                }
                            }

                        }


                    }

                    if (redFlags > 0) {

                        pepsReady = false;
                        // Show notifications
                        model.addAttribute("redFlagsNum", redFlags);
                        model.addAttribute("listOfAnnotatedCode", peps);


                    } else {
                        pepsReady = true;
                    }


                }

                // Check for handlers

//                List<ApplicationInstanceHandler> applicationInstanceHandlers = applicationInstanceHandlerService.findByApplicationInstanceID(appInstance);
                List<ApplicationInstanceHandler> applicationInstanceHandlers = appInstance.getApplicationInstanceHandlers();

                boolean isSuccess = RepositoryUtil.checkHandlersPerApplicationInstance((Application) applicationService.findOneWithoutBlob(id).get(), applicationInstanceHandlers, expressionRepository, ruleRepository, policyRepository, policySetRepository, instanceRepository, propertyRepository);

                if (!isSuccess) {
                    handlersReady = false;
                } else {
                    handlersReady = true;
                }

                model.addAttribute("handlersReady", handlersReady);

                if (pepsReady && dbProxyReady) {
                    model.addAttribute("paasProviders", paasProviderService.findAll());
                    model.addAttribute("proxyCloudProviders", proxyCloudProviderService.findAll());
                }

                model.addAttribute("pepsReady", pepsReady);
                model.addAttribute("dbProxyReady", dbProxyReady);
                model.addAttribute("isDataModel", app.isDataModel());

                model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));
                model.addAttribute("appInstance", applicationInstanceService.findOneWithoutApplication(instanceID));

                APIKey apiKey = (APIKey) apiKeyService.findByApplicationID(app.getId()).get(0);
                model.addAttribute("apiKey", apiKey.getUniqueID());


            } else if (appInstance.getOverallStatus() == 12) {

                // Instance is deploying to SlipStream

                pepsReady = true;
                dbProxyReady = true;

                // Check for handlers

//                List<ApplicationInstanceHandler> applicationInstanceHandlers = applicationInstanceHandlerService.findByApplicationInstanceID(appInstance);
                List<ApplicationInstanceHandler> applicationInstanceHandlers = appInstance.getApplicationInstanceHandlers();

                boolean isSuccess = RepositoryUtil.checkHandlersPerApplicationInstance((Application) applicationService.findOneWithoutBlob(id).get(), applicationInstanceHandlers, expressionRepository, ruleRepository, policyRepository, policySetRepository, instanceRepository, propertyRepository);

                if (!isSuccess) {
                    handlersReady = false;
                } else {
                    handlersReady = true;
                }

                model.addAttribute("handlersReady", handlersReady);

                model.addAttribute("pepsReady", pepsReady);
                model.addAttribute("dbProxyReady", dbProxyReady);
                model.addAttribute("isDataModel", app.isDataModel());

                model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));
                model.addAttribute("appInstance", applicationInstanceService.findOneWithoutApplication(instanceID));

                APIKey apiKey = (APIKey) apiKeyService.findByApplicationID(app.getId()).get(0);
                model.addAttribute("apiKey", apiKey.getUniqueID());

            } else if (appInstance.getOverallStatus() == 9) {

                // TODO
                // Error in deployment

                pepsReady = true;
                dbProxyReady = true;

                // Check for handlers

//                List<ApplicationInstanceHandler> applicationInstanceHandlers = applicationInstanceHandlerService.findByApplicationInstanceID(appInstance);
                List<ApplicationInstanceHandler> applicationInstanceHandlers = appInstance.getApplicationInstanceHandlers();

                boolean isSuccess = RepositoryUtil.checkHandlersPerApplicationInstance((Application) applicationService.findOneWithoutBlob(id).get(), applicationInstanceHandlers, expressionRepository, ruleRepository, policyRepository, policySetRepository, instanceRepository, propertyRepository);

                if (!isSuccess) {
                    handlersReady = false;
                } else {
                    handlersReady = true;
                }

                model.addAttribute("handlersReady", handlersReady);

                model.addAttribute("pepsReady", pepsReady);
                model.addAttribute("dbProxyReady", dbProxyReady);
                model.addAttribute("isDataModel", app.isDataModel());

                model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));
                model.addAttribute("appInstance", applicationInstanceService.findOneWithoutApplication(instanceID));

                APIKey apiKey = (APIKey) apiKeyService.findByApplicationID(app.getId()).get(0);
                model.addAttribute("apiKey", apiKey.getUniqueID());

            } else {

                // TODO
                // Deployed

                pepsReady = true;
                dbProxyReady = true;

                // Check for handlers

//                List<ApplicationInstanceHandler> applicationInstanceHandlers = applicationInstanceHandlerService.findByApplicationInstanceID(appInstance);
                List<ApplicationInstanceHandler> applicationInstanceHandlers = appInstance.getApplicationInstanceHandlers();

                boolean isSuccess = RepositoryUtil.checkHandlersPerApplicationInstance((Application) applicationService.findOneWithoutBlob(id).get(), applicationInstanceHandlers, expressionRepository, ruleRepository, policyRepository, policySetRepository, instanceRepository, propertyRepository);

                if (!isSuccess) {
                    handlersReady = false;
                } else {
                    handlersReady = true;
                }

                model.addAttribute("handlersReady", handlersReady);

                model.addAttribute("pepsReady", pepsReady);
                model.addAttribute("dbProxyReady", dbProxyReady);
                model.addAttribute("isDataModel", app.isDataModel());

                model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));
                if (appInstance.getDeploymentType() == 1) {
                    model.addAttribute("appInstance", applicationInstanceService.findOneWithPaaSProviderWithoutApplication(instanceID));
                } else {
                    model.addAttribute("appInstance", appInstance);
                }

                APIKey apiKey = (APIKey) apiKeyService.findByApplicationID(app.getId()).get(0);
                model.addAttribute("apiKey", apiKey.getUniqueID());


            }

        } catch (ApplicationDoesNotExist | ApplicationInstanceDoesNotExist e) {
            e.printStackTrace();
        }

        return "application::instance-deploy";

    }

    /*
     * Activity
     */
    @RequestMapping(value = "/application/{id}/instance/{instanceID}/activity", method = RequestMethod.GET)
    public String activityListView(@PathVariable("id") long id, @PathVariable("instanceID") long instanceID, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/instance/" + instanceID + "/activity");
        return "redirect:/";
    }

    @RequestMapping(value = "/application/{id}/instance/{instanceID}/activity", method = RequestMethod.POST)
    public String activity(Model model, @PathVariable("id") long id, @PathVariable("instanceID") long instanceID) {

        try {

            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));

            ApplicationInstance applicationInstance = applicationInstanceService.findOne(instanceID).get();

            if (null != applicationInstance) {

                if (applicationInstance.getOverallStatus() >= 10 && applicationInstance.getOverallStatus() != 12) {

                    if (applicationInstance.getDeploymentType() == 1) {

                        model.addAttribute("appInstance", applicationInstanceService.findOneWithPaaSProviderWithoutApplication(instanceID));

                    } else {

                        model.addAttribute("appInstance", applicationInstance);
                    }
                    List<ApplicationInstanceActivity> applicationInstanceActivities = applicationInstanceActivityService.findByApplicationInstanceIDFirst100(instanceID);
                    model.addAttribute("activities", applicationInstanceActivities);

                } else {
                    model.addAttribute("appInstance", applicationInstance);
                    List<ApplicationInstanceActivity> applicationInstanceActivities = applicationInstanceActivityService.findByApplicationInstanceIDFirst100(instanceID);
                    model.addAttribute("activities", applicationInstanceActivities);
                }

            } else {
                // TODO
                // ERROR PAGE
            }


        } catch (ApplicationDoesNotExist | ApplicationInstanceDoesNotExist e) {
            e.printStackTrace();
        }

        return "application::instance-activity";
    }

    @RequestMapping(value = "/application/{id}/upload", method = RequestMethod.GET)
    public String applicationUploadSourceCodeView(@PathVariable("id") long id,
                                                  final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/upload");
        return "redirect:/";
    }

    @RequestMapping(value = "/application/{id}/upload", method = RequestMethod.POST)
    public String applicationUploadSourceCode(Model model, @PathVariable("id") long id) {
        try {
            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));

        } catch (ApplicationDoesNotExist e) {
            e.printStackTrace();
        }
        return "application::upload";
    }

    @RequestMapping(value = "/application/{id}/pep", method = RequestMethod.GET)
    public String applicationPEPView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/pep");
        return "redirect:/";
    }

    @RequestMapping(value = "/application/{id}/pep", method = RequestMethod.POST)
    public String applicationPEPList(Model model, @PathVariable("id") long id) {
        try {
            Application application = (Application) applicationService.findOneWithoutBlob(id).get();
            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));

            if (application.isPep()) {

                List<AnnotatedCode> peps = Util.parseAnnotatedSourceCodeJSONOnlyForPEPs(application.getAnnotatedCodePEP());

                if (null != peps & !peps.isEmpty()) {

                    peps.stream().forEach(pep -> {

                        List<AnnotatedAnnotation> classAnnotation = pep.getAnnotations();

                        if (null != classAnnotation && !classAnnotation.isEmpty()) {

                            classAnnotation.stream().forEach(annot -> {

                                String value = annot.getValue().substring(1, annot.getValue().length() - 1);

                                switch (annot.getType()) {
                                    case "RULE":
                                        try {
                                            Rule existingRule = (Rule) ruleService.findByRuleName(value).get();
                                            if (null != existingRule) {
                                                annot.setExists(true);
                                                annot.setEntityID(existingRule.getId());
                                                annot.setRedirectURL("/model/rule/" + existingRule.getId());
                                            } else {
                                                annot.setExists(false);
                                                annot.setRedirectURL("/model/rule/add?name=" + value + "&amp;object=" + pep.getName());
                                            }
                                        } catch (RuleNameDoesNotExist e) {
//                                            e.printStackTrace();
                                            annot.setExists(false);
                                            annot.setRedirectURL("/model/rule/add?name=" + value + "&amp;object=" + pep.getName());
                                        }
                                        break;
                                    case "POLICY":
                                        try {
                                            Policy existingPolicy = (Policy) policyService.findByPolicyName(value).get();
                                            if (null != existingPolicy) {
                                                annot.setExists(true);
                                                annot.setEntityID(existingPolicy.getId());
                                                annot.setRedirectURL("/model/policy/" + existingPolicy.getId());
                                            } else {
                                                annot.setExists(false);
                                                annot.setRedirectURL("/model/policy/add?name=" + value);
                                            }
                                        } catch (PolicyNameDoesNotExist e) {
//                                            e.printStackTrace();
                                            annot.setExists(false);
                                            annot.setRedirectURL("/model/policy/add?name=" + value);
                                        }
                                        break;
                                    case "POLICY_SET":
                                        try {
                                            PolicySet existingPolicySet = (PolicySet) policySetService.findByPolicySetName(value).get();
                                            if (null != existingPolicySet) {
                                                annot.setExists(true);
                                                annot.setEntityID(existingPolicySet.getId());
                                                annot.setRedirectURL("/model/policyset/" + existingPolicySet.getId());
                                            } else {
                                                annot.setExists(false);
                                                annot.setRedirectURL("/model/policyset/add?name=" + value);
                                            }
                                        } catch (PolicySetNameDoesNotExist e) {
//                                            e.printStackTrace();
                                            annot.setExists(false);
                                            annot.setRedirectURL("/model/policyset/add?name=" + value);
                                        }
                                        break;
                                }

                            });

                        }

                        List<AnnotatedMethod> methods = pep.getMethods();

                        if (null != methods && !methods.isEmpty()) {

                            methods.stream().forEach(method -> {

                                method.getMethodAnnotations().stream().forEach(annot -> {

                                    String value = annot.getValue().substring(1, annot.getValue().length() - 1);

                                    switch (annot.getType()) {
                                        case "RULE":
                                            try {
                                                Rule existingRule = (Rule) ruleService.findByRuleName(value).get();
                                                if (null != existingRule) {
                                                    annot.setExists(true);
                                                    annot.setEntityID(existingRule.getId());
                                                    annot.setRedirectURL("/model/rule/" + existingRule.getId());
                                                } else {
                                                    annot.setExists(false);
                                                    annot.setRedirectURL("/model/rule/add?name=" + value + "&amp;object=" + pep.getName() + "." + method.getName());
                                                }
                                            } catch (RuleNameDoesNotExist e) {
//                                                e.printStackTrace();
                                                annot.setExists(false);
                                            }
                                            break;
                                        case "POLICY":
                                            try {
                                                Policy existingPolicy = (Policy) policyService.findByPolicyName(value).get();
                                                if (null != existingPolicy) {
                                                    annot.setExists(true);
                                                    annot.setEntityID(existingPolicy.getId());
                                                    annot.setRedirectURL("/model/policy/" + existingPolicy.getId());
                                                } else {
                                                    annot.setExists(false);
                                                    annot.setRedirectURL("/model/policy/add?name=" + value);
                                                }
                                            } catch (PolicyNameDoesNotExist e) {
//                                                e.printStackTrace();
                                                annot.setExists(false);
                                                annot.setRedirectURL("/model/policy/add?name=" + value);
                                            }
                                            break;
                                        case "POLICY_SET":
                                            try {
                                                PolicySet existingPolicySet = (PolicySet) policySetService.findByPolicySetName(value).get();
                                                if (null != existingPolicySet) {
                                                    annot.setExists(true);
                                                    annot.setEntityID(existingPolicySet.getId());
                                                    annot.setRedirectURL("/model/policyset/" + existingPolicySet.getId());
                                                } else {
                                                    annot.setExists(false);
                                                    annot.setRedirectURL("/model/policyset/add?name=" + value);
                                                }
                                            } catch (PolicySetNameDoesNotExist e) {
//                                                e.printStackTrace();
                                                annot.setExists(false);
                                                annot.setRedirectURL("/model/policyset/add?name=" + value);
                                            }
                                            break;
                                    }

                                });

                            });
                        }

                    });

                }

                model.addAttribute("listOfAnnotatedCode", peps);

            } else {

                model.addAttribute("listOfAnnotatedCode", null);

            }

        } catch (ApplicationDoesNotExist e) {
            e.printStackTrace();
        }
        return "application::pep";
    }

    @RequestMapping(value = "/application/{id}/entity", method = RequestMethod.GET)
    public String applicationDataModelView(@PathVariable("id") long id,
                                           final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/application/" + id + "/entity");
        return "redirect:/";
    }

    @RequestMapping(value = "/application/{id}/entity", method = RequestMethod.POST)
    public String applicationDataModelList(Model model, @PathVariable("id") long id) {
        try {
            Application application = (Application) applicationService.findOneWithoutBlob(id).get();
            model.addAttribute("currentApp", applicationService.findOneWithoutBlob(id));

            if (application.isDataModel()) {
                model.addAttribute("listOfAnnotatedCode", Util.parseAnnotatedSourceCodeJSONOnlyForDataModels(application.getAnnotatedCodeDataModel()));
            } else {
                model.addAttribute("listOfAnnotatedCode", null);
            }

        } catch (ApplicationDoesNotExist e) {
            e.printStackTrace();
        }
        return "application::datamodel";
    }

    /*
     * Model
     */
    @RequestMapping(value = "/model", method = RequestMethod.GET)
    public String modelView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/context");
        return "redirect:/";
    }

    /*
    * Context Model
    */
    @RequestMapping(value = "/model/lifecycle", method = RequestMethod.GET)
    public String lifecycleView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/lifecycle");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/lifecycle", method = RequestMethod.POST)
    public String lifecycleList(Model model) {
        return "model::model-lifecycle";
    }

    @RequestMapping(value = "/model/hlo", method = RequestMethod.GET)
    public String hloView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/hlo");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/hlo", method = RequestMethod.POST)
    public String hloList(Model model) {
        return "model::model-hlo";
    }

    @RequestMapping(value = "/model/context", method = RequestMethod.GET)
    public String contextModelView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/context");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/context", method = RequestMethod.POST)
    public String contextModelList(Model model) {
        return "model::model-context";
    }

    @RequestMapping(value = "/model/context/object", method = RequestMethod.GET)
    public String contextModelObjectView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/context/object");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/context/object", method = RequestMethod.POST)
    public String contextModelObjectList(Model model) {
        return "model::model-context-object";
    }

    @RequestMapping(value = "/model/context/subject", method = RequestMethod.GET)
    public String contextModelSubjectView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/context/subject");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/context/subject", method = RequestMethod.POST)
    public String contextModelSubjectList(Model model) {
        return "model::model-context-subject";
    }

    @RequestMapping(value = "/model/context/request", method = RequestMethod.GET)
    public String contextModelRequestView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/context/request");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/context/request", method = RequestMethod.POST)
    public String contextModelRequestList(Model model) {
        return "model::model-context-request";
    }

    @RequestMapping(value = "/model/context/pattern", method = RequestMethod.GET)
    public String contextModelPatternView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/context/pattern");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/context/pattern", method = RequestMethod.POST)
    public String contextModelPatternList(Model model) {
        return "model::model-context-pattern";
    }

    @RequestMapping(value = "/model/context/class/add", method = RequestMethod.GET)
    public String addClassContextModelView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/context/class/add");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/context/class/add", method = RequestMethod.POST)
    public String addClassContextModelList(Model model, @RequestParam(name = "r", defaultValue = "") String
            rootClassID) {

        try {

            model.addAttribute("rootClassID", rootClassID);

            Clazz clazz = (Clazz) clazzService.findOne(Long.valueOf(rootClassID)).get();

            if (clazz.getId() == 1) {
                model.addAttribute("rootClassName", "security context element");
                model.addAttribute("rootURL", "/model/context");
            } else if (clazz.getId() == 2) {
                model.addAttribute("rootClassName", "object");
                model.addAttribute("rootURL", "/model/context/object");
            } else if (clazz.getId() == 3) {
                model.addAttribute("rootClassName", "request");
                model.addAttribute("rootURL", "/model/context/request");
            } else if (clazz.getId() == 4) {
                model.addAttribute("rootClassName", "subject");
                model.addAttribute("rootURL", "/model/context/subject");
            } else {
                model.addAttribute("rootClassName", "context pattern");
                model.addAttribute("rootURL", "/model/context/pattern");
            }

            model.addAttribute("namespaces", namespaceService.findAllByOrderByPrefixAsc());

        } catch (ClazzDoesNotExist c) {
            c.printStackTrace();
        }

        return "model::model-context-class-add";
    }

    @RequestMapping(value = "/model/context/class/{id}", method = RequestMethod.GET)
    public String editClassContextModelView(@PathVariable("id") long id,
                                            final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/context/class/" + id);
        return "redirect:/";
    }

    @RequestMapping(value = "/model/context/class/{id}", method = RequestMethod.POST)
    public String classContextModelEditView(Model model, @PathVariable("id") long id) {
        try {

            Clazz clazz = (Clazz) clazzService.findOne(id).get();

            model.addAttribute("clazz", clazzService.findOne(id));
            model.addAttribute("namespaces", namespaceService.findAllByOrderByPrefixAsc());

            Clazz rootClazz = (Clazz) clazzService.findOne(clazz.getRootID().getId()).get();

            model.addAttribute("rootClassID", rootClazz.getId());

            if (rootClazz.getId() == 1) {
                model.addAttribute("rootClassName", "security context element");
                model.addAttribute("rootURL", "/model/context");
            } else if (rootClazz.getId() == 2) {
                model.addAttribute("rootClassName", "object");
                model.addAttribute("rootURL", "/model/context/object");
            } else if (rootClazz.getId() == 3) {
                model.addAttribute("rootClassName", "request");
                model.addAttribute("rootURL", "/model/context/request");
            } else if (rootClazz.getId() == 4) {
                model.addAttribute("rootClassName", "subject");
                model.addAttribute("rootURL", "/model/context/subject");
            } else {
                model.addAttribute("rootClassName", "context pattern");
                model.addAttribute("rootURL", "/model/context/pattern");
            }

        } catch (ClazzDoesNotExist e) {
            e.printStackTrace();
        }
        return "model::model-context-class-edit";
    }

    @RequestMapping(value = "/model/context/class/{id}/instance", method = RequestMethod.GET)
    public String viewInstancesContextModelView(@PathVariable("id") long id,
                                                final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/context/class/" + id + "/instance");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/context/class/{id}/instance", method = RequestMethod.POST)
    public String instancesContextModelListView(Model model, @PathVariable("id") long id) {
        try {

            Clazz clazz = (Clazz) clazzService.findOne(id).get();
            model.addAttribute("clazz", clazz);

            Clazz rootClazz = (Clazz) clazzService.findOne(clazz.getRootID().getId()).get();

            model.addAttribute("rootClassID", rootClazz.getId());

            if (rootClazz.getId() == 1) {
                model.addAttribute("rootClassName", "security context element");
                model.addAttribute("rootURL", "/model/context");
            } else if (rootClazz.getId() == 2) {
                model.addAttribute("rootClassName", "object");
                model.addAttribute("rootURL", "/model/context/object");
            } else if (rootClazz.getId() == 3) {
                model.addAttribute("rootClassName", "request");
                model.addAttribute("rootURL", "/model/context/request");
            } else if (rootClazz.getId() == 4) {
                model.addAttribute("rootClassName", "subject");
                model.addAttribute("rootURL", "/model/context/subject");
            } else {
                model.addAttribute("rootClassName", "context pattern");
                model.addAttribute("rootURL", "/model/context/pattern");
            }

        } catch (ClazzDoesNotExist e) {
            e.printStackTrace();
        }
        return "model::model-context-class-instances";
    }

    @RequestMapping(value = "/model/context/class/{id}/instance/add", method = RequestMethod.GET)
    public String addInstanceContextModelView(@PathVariable("id") long id,
                                              final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/context/class/" + id + "/instance/add");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/context/class/{id}/instance/add", method = RequestMethod.POST)
    public String addInstanceContextModelView(Model model, @PathVariable("id") long id) {
        try {

            Clazz clazz = ((Clazz) clazzService.findOne(id).get());
            model.addAttribute("clazz", clazz);
            model.addAttribute("properties", clazz.getAllProperties());

            model.addAttribute("namespaces", namespaceService.findAllByOrderByPrefixAsc());

            Clazz rootClazz = (Clazz) clazzService.findOne(clazz.getRootID().getId()).get();

            model.addAttribute("rootClassID", rootClazz.getId());

            if (rootClazz.getId() == 1) {
                model.addAttribute("rootClassName", "security context element");
                model.addAttribute("rootURL", "/model/context");
            } else if (rootClazz.getId() == 2) {
                model.addAttribute("rootClassName", "object");
                model.addAttribute("rootURL", "/model/context/object");
            } else if (rootClazz.getId() == 3) {
                model.addAttribute("rootClassName", "request");
                model.addAttribute("rootURL", "/model/context/request");
            } else if (rootClazz.getId() == 4) {
                model.addAttribute("rootClassName", "subject");
                model.addAttribute("rootURL", "/model/context/subject");
            } else {
                model.addAttribute("rootClassName", "context pattern");
                model.addAttribute("rootURL", "/model/context/pattern");
            }

        } catch (ClazzDoesNotExist e) {
            e.printStackTrace();
        }
        return "model::model-context-class-instance-add";
    }

    @RequestMapping(value = "/model/context/class/{id}/instance/{instanceID}", method = RequestMethod.GET)
    public String editInstanceContextModelView(@PathVariable("id") long id,
                                               @PathVariable("instanceID") long instanceID, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/context/class/" + id + "/instance/" + instanceID);
        return "redirect:/";
    }

    @RequestMapping(value = "/model/context/class/{id}/instance/{instanceID}", method = RequestMethod.POST)
    public String editInstanceContextModelView(Model model, @PathVariable("id") long id,
                                               @PathVariable("instanceID") long instanceID) {
        try {
            Clazz clazz = ((Clazz) clazzService.findOne(id).get());

            model.addAttribute("clazz", clazz);
            model.addAttribute("properties", clazz.getAllProperties());
            model.addAttribute("instance", instanceService.findOne(instanceID));
            model.addAttribute("namespaces", namespaceService.findAllByOrderByPrefixAsc());

            Clazz rootClazz = (Clazz) clazzService.findOne(clazz.getRootID().getId()).get();

            model.addAttribute("rootClassID", rootClazz.getId());

            if (rootClazz.getId() == 1) {
                model.addAttribute("rootClassName", "security context element");
                model.addAttribute("rootURL", "/model/context");
            } else if (rootClazz.getId() == 2) {
                model.addAttribute("rootClassName", "object");
                model.addAttribute("rootURL", "/model/context/object");
            } else if (rootClazz.getId() == 3) {
                model.addAttribute("rootClassName", "request");
                model.addAttribute("rootURL", "/model/context/request");
            } else if (rootClazz.getId() == 4) {
                model.addAttribute("rootClassName", "subject");
                model.addAttribute("rootURL", "/model/context/subject");
            } else {
                model.addAttribute("rootClassName", "context pattern");
                model.addAttribute("rootURL", "/model/context/pattern");
            }

        } catch (InstanceDoesNotExist | ClazzDoesNotExist e) {
            e.printStackTrace();
        }
        return "model::model-context-class-instance-add";
    }

    @RequestMapping(value = "/model/context/class/{id}/property", method = RequestMethod.GET)
    public String viewPropertiesContextModelView(@PathVariable("id") long id,
                                                 final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/context/class/" + id + "/property");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/context/class/{id}/property", method = RequestMethod.POST)
    public String propertiesContextModelListView(Model model, @PathVariable("id") long id) {
        try {

            Clazz clazz = ((Clazz) clazzService.findOne(id).get());

            model.addAttribute("clazz", clazz);

            model.addAttribute("properties", clazz.getAllProperties());
            model.addAttribute("namespaces", namespaceService.findAllByOrderByPrefixAsc());

            Clazz rootClazz = (Clazz) clazzService.findOne(clazz.getRootID().getId()).get();

            model.addAttribute("rootClassID", rootClazz.getId());

            if (rootClazz.getId() == 1) {
                model.addAttribute("rootClassName", "security context element");
                model.addAttribute("rootURL", "/model/context");
            } else if (rootClazz.getId() == 2) {
                model.addAttribute("rootClassName", "object");
                model.addAttribute("rootURL", "/model/context/object");
            } else if (rootClazz.getId() == 3) {
                model.addAttribute("rootClassName", "request");
                model.addAttribute("rootURL", "/model/context/request");
            } else if (rootClazz.getId() == 4) {
                model.addAttribute("rootClassName", "subject");
                model.addAttribute("rootURL", "/model/context/subject");
            } else {
                model.addAttribute("rootClassName", "context pattern");
                model.addAttribute("rootURL", "/model/context/pattern");
            }

        } catch (ClazzDoesNotExist e) {
            e.printStackTrace();
        }
        return "model::model-context-class-properties";
    }

    @RequestMapping(value = "/model/context/class/{id}/property/add", method = RequestMethod.GET)
    public String addPropertyContextModelView(@PathVariable("id") long id,
                                              final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/context/class/" + id + "/property/add");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/context/class/{id}/property/add", method = RequestMethod.POST)
    public String addPropertyContextModelView(Model model, @PathVariable("id") long id) {
        try {

            Clazz clazz = ((Clazz) clazzService.findOne(id).get());

            model.addAttribute("clazz", clazz);

            model.addAttribute("propertyTypes", propertyTypeService.findAll());
            model.addAttribute("namespaces", namespaceService.findAllByOrderByPrefixAsc());

            Clazz rootClazz = (Clazz) clazzService.findOne(clazz.getRootID().getId()).get();

            model.addAttribute("rootClassID", rootClazz.getId());

            if (rootClazz.getId() == 1) {
                model.addAttribute("rootClassName", "security context element");
                model.addAttribute("rootURL", "/model/context");
            } else if (rootClazz.getId() == 2) {
                model.addAttribute("rootClassName", "object");
                model.addAttribute("rootURL", "/model/context/object");
            } else if (rootClazz.getId() == 3) {
                model.addAttribute("rootClassName", "request");
                model.addAttribute("rootURL", "/model/context/request");
            } else if (rootClazz.getId() == 4) {
                model.addAttribute("rootClassName", "subject");
                model.addAttribute("rootURL", "/model/context/subject");
            } else {
                model.addAttribute("rootClassName", "context pattern");
                model.addAttribute("rootURL", "/model/context/pattern");
            }

        } catch (ClazzDoesNotExist e) {
            e.printStackTrace();
        }
        return "model::model-context-class-property-add";
    }

    @RequestMapping(value = "/model/context/class/{id}/property/{propertyID}", method = RequestMethod.GET)
    public String editPropertyContextModelView(@PathVariable("id") long id,
                                               @PathVariable("propertyID") long propertyID, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/context/class/" + id + "/property/" + propertyID);
        return "redirect:/";
    }

    @RequestMapping(value = "/model/context/class/{id}/property/{propertyID}", method = RequestMethod.POST)
    public String editPropertyContextModelView(Model model, @PathVariable("id") long id,
                                               @PathVariable("propertyID") long propertyID) {
        try {

            Clazz clazz = ((Clazz) clazzService.findOne(id).get());

            model.addAttribute("clazz", clazz);
            model.addAttribute("property", (Property) propertyService.findOne(propertyID).get());
            model.addAttribute("propertyTypes", propertyTypeService.findAll());
            model.addAttribute("namespaces", namespaceService.findAllByOrderByPrefixAsc());

            Clazz rootClazz = (Clazz) clazzService.findOne(clazz.getRootID().getId()).get();

            model.addAttribute("rootClassID", rootClazz.getId());

            if (rootClazz.getId() == 1) {
                model.addAttribute("rootClassName", "security context element");
                model.addAttribute("rootURL", "/model/context");
            } else if (rootClazz.getId() == 2) {
                model.addAttribute("rootClassName", "object");
                model.addAttribute("rootURL", "/model/context/object");
            } else if (rootClazz.getId() == 3) {
                model.addAttribute("rootClassName", "request");
                model.addAttribute("rootURL", "/model/context/request");
            } else if (rootClazz.getId() == 4) {
                model.addAttribute("rootClassName", "subject");
                model.addAttribute("rootURL", "/model/context/subject");
            } else {
                model.addAttribute("rootClassName", "context pattern");
                model.addAttribute("rootURL", "/model/context/pattern");
            }


        } catch (ClazzDoesNotExist | PropertyDoesNotExist e) {
            e.printStackTrace();
        }
        return "model::model-context-class-property-add";
    }

    /*
    * Namespace
    */
    @RequestMapping(value = "/model/namespace", method = RequestMethod.GET)
    public String namespaceModelView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/namespace");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/namespace", method = RequestMethod.POST)
    public String namespaceModelList(Model model, @PageableDefault(size = PageWrapper.MAX_PAGE_ITEM_DISPLAY) Pageable pageable) {

        model.addAttribute("page", new PageWrapper<>(namespaceService.findAll(pageable), "/model/namespace"));

        return "model::model-namespace";
    }

    @RequestMapping(value = "/model/namespace/add", method = RequestMethod.GET)
    public String addNamespaceModelView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/namespace/add");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/namespace/add", method = RequestMethod.POST)
    public String addNamespaceModelList() {
        return "model::model-namespace-add";
    }

    @RequestMapping(value = "/model/namespace/{id}", method = RequestMethod.GET)
    public String editNamespaceModelView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/namespace/" + id);
        return "redirect:/";
    }

    @RequestMapping(value = "/model/namespace/{id}", method = RequestMethod.POST)
    public String editNamespaceModelList(Model model, @PathVariable("id") long id) {
        try {
            model.addAttribute("namespace", namespaceService.findOne(id));
        } catch (NamespaceDoesNotExist e) {
            e.printStackTrace();
        }
        return "model::model-namespace-edit";
    }

    /*
    * Handlers
    */
    @RequestMapping(value = "/model/handler", method = RequestMethod.GET)
    public String handlerModelView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/handler");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/handler", method = RequestMethod.POST)
    public String handlerModelList(Model model, @PageableDefault(size = PageWrapper.MAX_PAGE_ITEM_DISPLAY) Pageable pageable) {

        model.addAttribute("page", new PageWrapper<>(handlerService.findAll(pageable), "/model/handler"));

        return "model::model-handler";
    }

    @RequestMapping(value = "/model/handler/add", method = RequestMethod.GET)
    public String addHandlerModelView(final RedirectAttributes redirectAttributes, @RequestParam(value = "input", required = false) String inputID, @RequestParam(value = "output", required = false) String outputID) {

        if (null != inputID && !inputID.isEmpty() && null != outputID && !outputID.isEmpty()) {

            redirectAttributes.addFlashAttribute("redirectToPage", "/model/handler/add?input=" + inputID + "&amp;output=" + outputID);

        } else {
            redirectAttributes.addFlashAttribute("redirectToPage", "/model/handler/add");
        }

        return "redirect:/";
    }

    @RequestMapping(value = "/model/handler/add", method = RequestMethod.POST)
    public String addHandlerModelList(Model model, @RequestParam(value = "input", required = false) String inputID, @RequestParam(value = "output", required = false) String outputID) {

        if (null != inputID && !inputID.isEmpty() && null != outputID && !outputID.isEmpty()) {
            try {

                model.addAttribute("predefinedInputID", inputID);
                model.addAttribute("predefinedInputName", ((Clazz) clazzService.findOne(Long.valueOf(inputID)).get()).getClassName());

                model.addAttribute("predefinedOutputID", outputID);
                model.addAttribute("predefinedOutputName", ((Clazz) clazzService.findOne(Long.valueOf(outputID)).get()).getClassName());

            } catch (ClazzDoesNotExist e) {
                e.printStackTrace();
            }
        }

        model.addAttribute("namespaces", namespaceService.findAllByOrderByPrefixAsc());

        return "model::model-handler-add";


    }

    @RequestMapping(value = "/model/handler/{id}", method = RequestMethod.GET)
    public String editHandlerModelView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/handler/" + id);
        return "redirect:/";
    }

    @RequestMapping(value = "/model/handler/{id}", method = RequestMethod.POST)
    public String editHandlerModelList(Model model, @PathVariable("id") long id) {
        try {
            model.addAttribute("handler", handlerService.findOne(id));
            model.addAttribute("namespaces", namespaceService.findAllByOrderByPrefixAsc());
        } catch (HandlerDoesNotExist e) {
            e.printStackTrace();
        }
        return "model::model-handler-edit";
    }

    /*
    * Expression
    */
    @RequestMapping(value = "/model/expression", method = RequestMethod.GET)
    public String expressionModelView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/expression");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/expression", method = RequestMethod.POST)
    public String expressionModelList(Model model, @PageableDefault(size = PageWrapper.MAX_PAGE_ITEM_DISPLAY) Pageable pageable) {

        model.addAttribute("page", new PageWrapper<>(expressionService.findAll(pageable), "/model/expression"));

        return "model::model-expression";
    }

    @RequestMapping(value = "/model/expression/add", method = RequestMethod.GET)
    public String addExpressionModelView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/expression/add");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/expression/add", method = RequestMethod.POST)
    public String addExpressionModelList(Model model) {

        try {

            model.addAttribute("namespaces", namespaceService.findAllByOrderByPrefixAsc());

            Clazz subject = (Clazz) clazzService.findByClassName("Subject").get();

            List<Clazz> subjectClasses = clazzService.findByParentID(subject);

            subjectClasses.add(subject);

            //

            Clazz request = (Clazz) clazzService.findByClassName("Request").get();

            List<Clazz> requestClasses = clazzService.findByParentID(request);

            requestClasses.add(request);

            //

            Clazz object = (Clazz) clazzService.findByClassName("Object").get();

            List<Clazz> objectClasses = clazzService.findByParentID(object);

            objectClasses.add(object);

            //

//            List<Instance> instances = new ArrayList<>();
//
//            if (null != subjectClasses && !subjectClasses.isEmpty()) {
//
//                subjectClasses.stream().forEach(tempClazz -> {
//
//                    if (null != tempClazz.getInstances() && !tempClazz.getInstances().isEmpty()) {
//
//                        tempClazz.getInstances().stream().forEach(tempInstance -> {
//
//                            if (!instances.contains(tempInstance)) { // TODO Any Subject && tempInstance.getId() != 2L) {
//                                instances.add(tempInstance);
//                            }
//
//                        });
//
//                    }
//
//                });
//
//            }
//
//            if (null != requestClasses && !requestClasses.isEmpty()) {
//
//                requestClasses.stream().forEach(tempClazz -> {
//
//                    if (null != tempClazz.getInstances() && !tempClazz.getInstances().isEmpty()) {
//
//                        tempClazz.getInstances().stream().forEach(tempInstance -> {
//
//                            if (!instances.contains(tempInstance)) {
//                                instances.add(tempInstance);
//                            }
//
//                        });
//
//                    }
//
//                });
//
//            }
//
//            if (null != objectClasses && !objectClasses.isEmpty()) {
//
//                objectClasses.stream().forEach(tempClazz -> {
//
//                    if (null != tempClazz.getInstances() && !tempClazz.getInstances().isEmpty()) {
//
//                        tempClazz.getInstances().stream().forEach(tempInstance -> {
//
//                            if (!instances.contains(tempInstance)) {
//                                instances.add(tempInstance);
//                            }
//
//                        });
//
//                    }
//
//                });
//
//            }
//
//            instances.sort(new Comparator<Instance>() {
//                @Override
//                public int compare(Instance o1, Instance o2) {
//                    int f = o1.getInstanceName().compareTo(o2.getInstanceName());
//                    return f;
//                }
//            });

            List<Instance> instances = instanceService.findByOrderByInstanceName();

            model.addAttribute("instances", instances);

            List<Expression> expressions = expressionService.findByOrderByExpressionName();

            model.addAttribute("allExpressions", expressions);


        } catch (ClassNameDoesNotExist | ClazzDoesNotExist e) {
            e.printStackTrace();
        }

        return "model::model-expression-add";
    }

    @RequestMapping(value = "/model/expression/{id}", method = RequestMethod.GET)
    public String editExpressionModelView(@PathVariable("id") long id,
                                          final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/expression/" + id);
        return "redirect:/";
    }

    @RequestMapping(value = "/model/expression/{id}", method = RequestMethod.POST)
    public String editExpressionModelList(Model model, @PathVariable("id") long id) {
        try {

            Expression thisExp = (Expression) expressionService.findOne(id).get();

            model.addAttribute("expression", expressionService.findOne(id));
            model.addAttribute("namespaces", namespaceService.findAllByOrderByPrefixAsc());

            List<Instance> instances = instanceService.findByOrderByInstanceName();

            model.addAttribute("instances", instances);

            List<Expression> expressions = expressionService.findByOrderByExpressionName();

            if (expressions.contains(thisExp)) {
                expressions.remove(thisExp);
            }

            model.addAttribute("allExpressions", expressions);


        } catch (ExpressionDoesNotExist e) {
            e.printStackTrace();
        }
        return "model::model-expression-add";
    }

    /*
     * Policy Set
     */
    @RequestMapping(value = "/model/policyset", method = RequestMethod.GET)
    public String policySetModelView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/policyset");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/policyset", method = RequestMethod.POST)
    public String policySetModelList(Model model, @PageableDefault(size = PageWrapper.MAX_PAGE_ITEM_DISPLAY) Pageable pageable) {

        model.addAttribute("page", new PageWrapper<>(policySetService.findAll(pageable), "/model/policyset"));

        return "model::model-policyset";
    }

    @RequestMapping(value = "/model/policyset/add", method = RequestMethod.GET)
    public String addPolicySetModelView(final RedirectAttributes redirectAttributes,
                                        @RequestParam(value = "name", required = false) String name) {

        if (null != name && !name.isEmpty()) {

            redirectAttributes.addFlashAttribute("redirectToPage", "/model/policyset/add?name=" + name);

        } else {
            redirectAttributes.addFlashAttribute("redirectToPage", "/model/policyset/add");
        }

        return "redirect:/";
    }

    @RequestMapping(value = "/model/policyset/add", method = RequestMethod.POST)
    public String addPolicySetModelList(Model model, @RequestParam(value = "name", required = false) String name) {

        if (null != name && !name.isEmpty()) {
            model.addAttribute("predefinedValue", name);
        }

        model.addAttribute("combiningAlgorithms", combiningAlgorithmService.findAllByOrderByNameAsc());
        model.addAttribute("namespaces", namespaceService.findAllByOrderByPrefixAsc());

        return "model::model-policyset-add";
    }

    @RequestMapping(value = "/model/policyset/{id}", method = RequestMethod.GET)
    public String editPolicySetModelView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/policyset/" + id);
        return "redirect:/";
    }

    @RequestMapping(value = "/model/policyset/{id}", method = RequestMethod.POST)
    public String editPolicySetModelList(Model model, @PathVariable("id") long id) {
        try {

            PolicySet policySet = (PolicySet) policySetService.findOne(id).get();
            model.addAttribute("policySet", policySet);

            model.addAttribute("namespaces", namespaceService.findAllByOrderByPrefixAsc());
            model.addAttribute("combiningAlgorithms", combiningAlgorithmService.findAllByOrderByNameAsc());

        } catch (PolicySetDoesNotExist e) {
            e.printStackTrace();
        }

        return "model::model-policyset-add";
    }

    @RequestMapping(value = "/model/policyset/{id}/policy", method = RequestMethod.GET)
    public String addRemovePolicyToPolicySetModelView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/policyset/" + id + "/policy");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/policyset/{id}/policy", method = RequestMethod.POST)
    public String addRemovePolicyToPolicySetModelList(Model model, @PathVariable("id") long id) {
        try {

            PolicySet policySet = (PolicySet) policySetService.findOne(id).get();
            model.addAttribute("policySet", policySet);

//            String selectedPolicies = "";
//            String selectedPoliciesTotal = "";

            List<Policy> allPolicies = policyService.findAll();

            List<Policy> selectedPolicies = new ArrayList<>();

            if (null != policySet.getPolicySetPolicies() && !policySet.getPolicySetPolicies().isEmpty()) {

                for (PolicySetPolicy policySetPolicy : policySet.getPolicySetPolicies()) {
                    selectedPolicies.add(policySetPolicy.getPolicy());
                    allPolicies.remove(policySetPolicy.getPolicy());
                }


            }

            model.addAttribute("unSelectedPolicies", allPolicies);
            model.addAttribute("selectedPolicies", selectedPolicies);

        } catch (PolicySetDoesNotExist e) {
            e.printStackTrace();
        }

        return "model::model-policyset-policy";
    }


    /*
     * Policy
     */
    @RequestMapping(value = "/model/policy", method = RequestMethod.GET)
    public String policyModelView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/policy");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/policy", method = RequestMethod.POST)
    public String policyModelList(Model model, @PageableDefault(size = PageWrapper.MAX_PAGE_ITEM_DISPLAY) Pageable pageable) {

        model.addAttribute("page", new PageWrapper<>(policyService.findAll(pageable), "/model/policy"));

        return "model::model-policy";
    }

    @RequestMapping(value = "/model/policy/add", method = RequestMethod.GET)
    public String addPolicyModelView(final RedirectAttributes redirectAttributes,
                                     @RequestParam(value = "name", required = false) String name) {

        if (null != name && !name.isEmpty()) {

            redirectAttributes.addFlashAttribute("redirectToPage", "/model/policy/add?name=" + name);

        } else {
            redirectAttributes.addFlashAttribute("redirectToPage", "/model/policy/add");
        }

        return "redirect:/";
    }

    @RequestMapping(value = "/model/policy/add", method = RequestMethod.POST)
    public String addPolicyModelList(Model model, @RequestParam(value = "name", required = false) String name) {

        if (null != name && !name.isEmpty()) {
            model.addAttribute("predefinedValue", name);
        }

        model.addAttribute("rules", ruleService.findAll());
        model.addAttribute("combiningAlgorithms", combiningAlgorithmService.findAllByOrderByNameAsc());
        model.addAttribute("namespaces", namespaceService.findAllByOrderByPrefixAsc());

        return "model::model-policy-add";
    }

    @RequestMapping(value = "/model/policy/{id}", method = RequestMethod.GET)
    public String editPolicyModelView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/policy/" + id);
        return "redirect:/";
    }

    @RequestMapping(value = "/model/policy/{id}", method = RequestMethod.POST)
    public String editPolicyModelList(Model model, @PathVariable("id") long id) {
        try {
            Policy policy = (Policy) policyService.findOne(id).get();

            model.addAttribute("policy", policy);

            String selectedRules = "";
            String selectedRulesTotal = "";

            for (PolicyRule policyRule : policy.getPolicyRules()) {
                selectedRules += policyRule.getRule().getId() + ",";
            }

            if (!selectedRules.isEmpty()) {

                selectedRules = selectedRules.substring(0, selectedRules.length() - 1);

                if (selectedRules.contains(",")) {

                    for (String rule : selectedRules.split("\\,")) {
                        selectedRulesTotal += rule + ",";
                    }

                    selectedRulesTotal = selectedRulesTotal.substring(0, selectedRulesTotal.length() - 1);
                } else {
                    selectedRulesTotal = selectedRules;
                }

                model.addAttribute("selectedRules", selectedRulesTotal);

            }
            model.addAttribute("namespaces", namespaceService.findAllByOrderByPrefixAsc());
            model.addAttribute("combiningAlgorithms", combiningAlgorithmService.findAllByOrderByNameAsc());
            model.addAttribute("rules", ruleService.findAll());
        } catch (PolicyDoesNotExist e) {
            e.printStackTrace();
        }
        return "model::model-policy-add";
    }

    @RequestMapping(value = "/model/policy/{id}/rule", method = RequestMethod.GET)
    public String addRemoveRuleToPolicyModelView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/policy/" + id + "/rule");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/policy/{id}/rule", method = RequestMethod.POST)
    public String addRemoveRuleToPolicySetModelList(Model model, @PathVariable("id") long id) {
        try {

            Policy policy = (Policy) policyService.findOne(id).get();
            model.addAttribute("policy", policy);

            List<Rule> allRules = ruleService.findAll();

            List<Rule> selectedRules = new ArrayList<>();

            if (null != policy.getPolicyRules() && !policy.getPolicyRules().isEmpty()) {

                for (PolicyRule policyRule : policy.getPolicyRules()) {
                    selectedRules.add(policyRule.getRule());
                    allRules.remove(policyRule.getRule());
                }

            }

            model.addAttribute("unSelectedRules", allRules);
            model.addAttribute("selectedRules", selectedRules);

        } catch (PolicyDoesNotExist e) {
            e.printStackTrace();
        }

        return "model::model-policy-rule";
    }

    /*
    * Rule
    */
    @RequestMapping(value = "/model/rule", method = RequestMethod.GET)
    public String ruleModelView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/rule");
        return "redirect:/";
    }

    @RequestMapping(value = "/model/rule", method = RequestMethod.POST)
    public String ruleModelList(Model model, @PageableDefault(size = PageWrapper.MAX_PAGE_ITEM_DISPLAY) Pageable pageable) {

        model.addAttribute("page", new PageWrapper<>(ruleService.findAll(pageable), "/model/rule"));

        return "model::model-rule";
    }

    @RequestMapping(value = "/model/rule/add", method = RequestMethod.GET)
    public String addRuleModelView(final RedirectAttributes redirectAttributes,
                                   @RequestParam(value = "name", required = false) String name,
                                   @RequestParam(value = "object", required = false) String object) {

        if (null != name && !name.isEmpty() && null != object && !object.isEmpty()) {

            redirectAttributes.addFlashAttribute("redirectToPage", "/model/rule/add?name=" + name + "&object=" + object);
        } else {
            redirectAttributes.addFlashAttribute("redirectToPage", "/model/rule/add");
        }

        return "redirect:/";
    }

    @RequestMapping(value = "/model/rule/add", method = RequestMethod.POST)
    public String addRuleModelList(Model model, @RequestParam(value = "name", required = false) String
            name, @RequestParam(value = "object", required = false) String object) {
        try {

            if (null != name && !name.isEmpty()) {
                model.addAttribute("predefinedValue", name);
            }

            if (null != object && !object.isEmpty()) {
                model.addAttribute("predefinedObject", object);
            }

            // Expressions
            model.addAttribute("expressions", expressionService.findAll());

            // Actor
            List<Instance> actorInstances = new ArrayList<>();

            // TODO

            List<Clazz> classes = clazzService.findByParentID((Clazz) clazzService.findOne(12L).get());

            classes.add((Clazz) clazzService.findOne(12L).get());

            List<Clazz> subjectClasses = RepositoryUtil.constructClassesList(classes, 4);

            subjectClasses.stream().forEach(clazz -> {
                clazz.getInstances().stream().forEach(instance -> {
                    if (!actorInstances.contains(instance)) {
                        actorInstances.add(instance);
                    }
                });
            });

            actorInstances.sort(new Comparator<Instance>() {
                @Override
                public int compare(Instance o1, Instance o2) {
                    int f = o1.getInstanceName().compareTo(o2.getInstanceName());
                    return f;
                }
            });

            model.addAttribute("actors", actorInstances);

            // Permission Type
            List<Clazz> permissionTypeInstances = new ArrayList<>();

            List<Clazz> permissionElementClasses = RepositoryUtil.constructClassesList(clazzService.findByParentID(clazzService.findByClassName("Permission Element").get()), 6);

            permissionElementClasses.stream().forEach(clazz -> {

                try {
                    if (!clazzService.findByParentID(clazz).isEmpty()) {
                        clazzService.findByParentID(clazz).stream().forEach(child -> {

                            Clazz childClazz = (Clazz) child;

                            if (!permissionTypeInstances.contains(childClazz) && !childClazz.getInstances().isEmpty()) {
                                permissionTypeInstances.add(childClazz);
                            }

                        });
                    }
                } catch (ClazzDoesNotExist e) {
                    e.printStackTrace();
                }

                if (!permissionTypeInstances.contains(clazz) && !clazz.getInstances().isEmpty()) {
                    permissionTypeInstances.add(clazz);
                }

            });

            permissionTypeInstances.sort(new Comparator<Clazz>() {
                @Override
                public int compare(Clazz o1, Clazz o2) {
                    int f = o1.getClassName().compareTo(o2.getClassName());
                    return f;
                }
            });

            model.addAttribute("permissionTypes", permissionTypeInstances);

            // Object
            List<String> objects = new ArrayList<>();

            instanceService.findByClassID((Clazz) clazzService.findByClassName("Object").get(), null)
                    .getContent().stream().forEach(instance -> {

                String instanceNameWithNamespace = "";

                if (null != instance.getNamespaceID()) {
                    instanceNameWithNamespace = instance.getNamespaceID().getPrefix() + ":" + instance.getInstanceName();
                } else {
                    instanceNameWithNamespace = "pcm:" + instance.getInstanceName();
                }

                if (!objects.contains(instanceNameWithNamespace)) {

                    objects.add(instanceNameWithNamespace);
                }

            });

            objects.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    int f = o1.compareTo(o2);
                    return f;
                }
            });


            model.addAttribute("objects", objects);

            model.addAttribute("namespaces", namespaceService.findAllByOrderByPrefixAsc());

        } catch (ClazzDoesNotExist | ClassNameDoesNotExist e) {
            e.printStackTrace();
        }

        return "model::model-rule-add";
    }

    @RequestMapping(value = "/model/rule/{id}", method = RequestMethod.GET)
    public String editRuleModelView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/model/rule/" + id);
        return "redirect:/";
    }

    @RequestMapping(value = "/model/rule/{id}", method = RequestMethod.POST)
    public String editRuleModelList(Model model, @PathVariable("id") long id) {
        try {

            Rule rule = (Rule) ruleService.findOne(id).get();

            model.addAttribute("rule", ruleService.findOne(id));

            // Expressions
            model.addAttribute("expressions", expressionService.findAll());

            // Actor
            List<Instance> actorInstances = new ArrayList<>();

            List<Clazz> classes = clazzService.findByParentID((Clazz) clazzService.findOne(12L).get());

            classes.add((Clazz) clazzService.findOne(12L).get());

            List<Clazz> subjectClasses = RepositoryUtil.constructClassesList(classes, 4);

            subjectClasses.stream().forEach(clazz -> {
                clazz.getInstances().stream().forEach(instance -> {
                    if (!actorInstances.contains(instance)) {
                        actorInstances.add(instance);
                    }
                });
            });

            actorInstances.sort(new Comparator<Instance>() {
                @Override
                public int compare(Instance o1, Instance o2) {
                    int f = o1.getInstanceName().compareTo(o2.getInstanceName());
                    return f;
                }
            });

            model.addAttribute("actors", actorInstances);

            // Permission Type
            List<Clazz> permissionTypeInstances = new ArrayList<>();

            List<Clazz> permissionElementClasses = RepositoryUtil.constructClassesList(clazzService.findByParentID(clazzService.findByClassName("Permission Element").get()), 6);

            permissionElementClasses.stream().forEach(clazz -> {

                try {
                    if (!clazzService.findByParentID(clazz).isEmpty()) {
                        clazzService.findByParentID(clazz).stream().forEach(child -> {

                            Clazz childClazz = (Clazz) child;

                            if (!permissionTypeInstances.contains(childClazz) && !childClazz.getInstances().isEmpty()) {
                                permissionTypeInstances.add(childClazz);
                            }

                        });
                    }
                } catch (ClazzDoesNotExist e) {
                    e.printStackTrace();
                }

                if (!permissionTypeInstances.contains(clazz) && !clazz.getInstances().isEmpty()) {
                    permissionTypeInstances.add(clazz);
                }

            });

            permissionTypeInstances.sort(new Comparator<Clazz>() {
                @Override
                public int compare(Clazz o1, Clazz o2) {
                    int f = o1.getClassName().compareTo(o2.getClassName());
                    return f;
                }
            });

            model.addAttribute("permissionTypes", permissionTypeInstances);


            // Action
            List<Instance> actionInstances = ((Clazz) clazzService.findByClassName(rule.getPermissionType()).get()).getInstances();

            actionInstances.sort(new Comparator<Instance>() {
                @Override
                public int compare(Instance o1, Instance o2) {
                    int f = o1.getInstanceName().compareTo(o2.getInstanceName());
                    return f;
                }
            });

            model.addAttribute("actions", actionInstances);

            // Object
//            List<Application> apps = applicationService.findAllWithoutBlob(null).getContent();
//            List<String> objects = new ArrayList<>();
//
//            apps.stream().filter(app -> app.isPep() || app.isDataModel())
//                    .collect(Collectors.toList()).forEach(app -> {
//
//                List<AnnotatedCode> annotatedCode = null;
//
//                if (app.isPep()) {
//                    annotatedCode = Util.parseAnnotatedSourceCodeJSONOnlyForPEPs(app.getAnnotatedCodeDataModel());
//                }
//
//                annotatedCode.stream().forEach(annotCode -> {
//                    if (null != annotCode.getMethods() && !annotCode.getMethods().isEmpty()) {
//                        annotCode.getMethods().stream().forEach(method -> {
//                            objects.add(annotCode.getName() + "." + method.getName());
//                        });
//                    } else {
//                        objects.add(annotCode.getName());
//                    }
//                });
//
//            });

            // Object
            List<String> objects = new ArrayList<>();

            instanceService.findByClassID((Clazz) clazzService.findByClassName("Object").get(), null)
                    .getContent().stream().forEach(instance -> {

                String instanceNameWithNamespace = "";

                if (null != instance.getNamespaceID()) {
                    instanceNameWithNamespace = instance.getNamespaceID().getPrefix() + ":" + instance.getInstanceName();
                } else {
                    instanceNameWithNamespace = "pcm:" + instance.getInstanceName();
                }

                if (!objects.contains(instanceNameWithNamespace)) {

                    objects.add(instanceNameWithNamespace);
                }

            });

            objects.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    int f = o1.compareTo(o2);
                    return f;
                }
            });


            model.addAttribute("objects", objects);

            model.addAttribute("namespaces", namespaceService.findAllByOrderByPrefixAsc());

        } catch (RuleDoesNotExist | ClazzDoesNotExist | ClassNameDoesNotExist e) {
            e.printStackTrace();
        }

        return "model::model-rule-add";
    }

    /*
     * Resources Registration Management
     */
    @RequestMapping(value = "/resource", method = RequestMethod.GET)
    public String resourcesRegistrationListView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/resource");
        return "redirect:/";
    }

    @RequestMapping(value = "/resource", method = RequestMethod.POST)
    public String getResourcesList(Model model, @PageableDefault(size = PageWrapper.MAX_PAGE_ITEM_DISPLAY) Pageable pageable) {

        UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
        String username = userauthentication.getDetails().getUsername();

        model.addAttribute("page", new PageWrapper<>(paasProviderService.findByUserID((User) userService.findByUsername(username).get(), pageable), "/resource"));

        return "resource::paas-list";
    }

    @RequestMapping(value = "/resource/paas", method = RequestMethod.GET)
    public String PaaSRegistrationListView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/resource/paas");
        return "redirect:/";
    }

    @RequestMapping(value = "/resource/paas", method = RequestMethod.POST)
    public String getPaaSRegistrationList(Model model, @PageableDefault(size = PageWrapper.MAX_PAGE_ITEM_DISPLAY) Pageable pageable) {

        UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
        String username = userauthentication.getDetails().getUsername();

        model.addAttribute("page", new PageWrapper<>(paasProviderService.findByUserID((User) userService.findByUsername(username).get(), pageable), "/resource/paas"));

        return "resource::paas-list";
    }

    @RequestMapping(value = "/resource/paas/add", method = RequestMethod.GET)
    public String paasProviderAddView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/resource/paas/add");
        return "redirect:/";
    }

    @RequestMapping(value = "/resource/paas/add", method = RequestMethod.POST)
    public String addPaaSProvider(Model model) {
        //Retrieve backend structures
        List<PaaSProviderType> paasProviderTypesList = paasProviderTypeService.findByOrderByNameAsc();
        //enrich model
        model.addAttribute("paasProviderTypesList", paasProviderTypesList);

        return "resource::paas-add";
    }

    @RequestMapping(value = "/resource/paas/{id}", method = RequestMethod.GET)
    public String paasProviderEditView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/resource/paas/" + id);
        return "redirect:/";
    }

    @RequestMapping(value = "/resource/paas/{id}", method = RequestMethod.POST)
    public String editPaaSProvider(Model model, @PathVariable("id") long id) {
        //Retrieve backend structures
        try {
            PaaSProvider paasProvider = (PaaSProvider) paasProviderService.findOne(id).get();
            //enrich model
            model.addAttribute("paasProvider", paasProvider);

            List<PaaSProviderType> paasProviderTypesList = paasProviderTypeService.findByOrderByNameAsc();
            //enrich model
            model.addAttribute("paasProviderTypesList", paasProviderTypesList);

        } catch (PaaSProviderDoesNotExist e) {
            e.printStackTrace();
        }

        return "resource::paas-edit";
    }

    @RequestMapping(value = "/resource/iaas", method = RequestMethod.GET)
    public String IaaSRegistrationListView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/resource/iaas");
        return "redirect:/";
    }

    @RequestMapping(value = "/resource/iaas", method = RequestMethod.POST)
    public String getIaaSRegistrationList(Model model, @PageableDefault(size = PageWrapper.MAX_PAGE_ITEM_DISPLAY) Pageable pageable) {

        UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
        String username = userauthentication.getDetails().getUsername();

        model.addAttribute("page", new PageWrapper<>(iaasProviderService.findByUserID((User) userService.findByUsername(username).get(), pageable), "/resource/iaas"));

        return "resource::iaas-list";
    }

    @RequestMapping(value = "/resource/iaas/add", method = RequestMethod.GET)
    public String IaaSRegistrationAddView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/resource/iaas/add");
        return "redirect:/";
    }

    @RequestMapping(value = "/resource/iaas/add", method = RequestMethod.POST)
    public String addIaaSRegistration(Model model) {
        //Retrieve backend structures
        List<IaaSProviderType> iaasProviderTypesList = iaasProviderTypeService.findAll();
        //enrich model
        model.addAttribute("iaasProviderTypesList", iaasProviderTypesList);

        return "resource::iaas-add";
    }

    @RequestMapping(value = "/resource/iaas/{id}", method = RequestMethod.GET)
    public String IaaSRegistrationEditView(@PathVariable("id") long id,
                                           final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/resource/iaas/" + id);
        return "redirect:/";
    }

    @RequestMapping(value = "/resource/iaas/{id}", method = RequestMethod.POST)
    public String getIaaSRegistration(Model model, @PathVariable("id") long id) {
        //Retrieve backend structures
        try {
            IaaSProvider iaasProvider = (IaaSProvider) iaasProviderService.findOne(id).get();
            List<IaaSProviderType> iaasProviderTypesList = iaasProviderTypeService.findAll();
            //enrich model
            model.addAttribute("iaasProvider", iaasProvider);
            model.addAttribute("iaasProviderTypesList", iaasProviderTypesList);

        } catch (IaaSProviderDoesNotExist e) {
            e.printStackTrace();
        }

        return "resource::iaas-edit";
    }

    @RequestMapping(value = "/resource/iaas/{id}/image", method = RequestMethod.GET)
    public String IaaSProviderImagesView(@PathVariable("id") long id, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/resource/iaas/" + id + "/image");
        return "redirect:/";
    }

    @RequestMapping(value = "/resource/iaas/{id}/image", method = RequestMethod.POST)
    public String IaaSProviderImagesViewList(Model model, @PathVariable("id") long id) {
        //Retrieve backend structures
        try {
            IaaSProvider iaasProvider = (IaaSProvider) iaasProviderService.findOne(id).get();
            List<IaaSProviderImage> iaasProviderImagesList = iaasProvider.getIaasProviderImages();
            //enrich model
            model.addAttribute("iaasProvider", iaasProvider);
            model.addAttribute("images", iaasProviderImagesList);


        } catch (IaaSProviderDoesNotExist e) {
            e.printStackTrace();
        }

        return "resource::iaas-images";
    }

    @RequestMapping(value = "/resource/iaas/{id}/image/add", method = RequestMethod.GET)
    public String IaaSProviderImageAddView(@PathVariable("id") long id,
                                           final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/resource/iaas/" + id + "/image/add");
        return "redirect:/";
    }

    @RequestMapping(value = "/resource/iaas/{id}/image/add", method = RequestMethod.POST)
    public String IaaSProviderImageViewAdd(Model model, @PathVariable("id") long id) {
        //Retrieve backend structures
        try {
            IaaSProvider iaasProvider = (IaaSProvider) iaasProviderService.findOne(id).get();
            //enrich model
            model.addAttribute("iaasProvider", iaasProvider);

        } catch (IaaSProviderDoesNotExist e) {
            e.printStackTrace();
        }

        return "resource::iaas-images-add";
    }

    @RequestMapping(value = "/resource/iaas/{id}/image/{imageID}", method = RequestMethod.GET)
    public String IaaSProviderImageEditView(@PathVariable("id") long id, @PathVariable("imageID") long imageID,
                                            final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/resource/iaas/" + id + "/image/" + imageID);
        return "redirect:/";
    }

    @RequestMapping(value = "/resource/iaas/{id}/image/{imageID}", method = RequestMethod.POST)
    public String IaaSProviderImageViewEdit(Model model, @PathVariable("id") long id,
                                            @PathVariable("imageID") long imageID) {
        //Retrieve backend structures
        try {
            IaaSProvider iaasProvider = (IaaSProvider) iaasProviderService.findOne(id).get();
            //enrich model
            model.addAttribute("iaasProvider", iaasProvider);

            model.addAttribute("image", iaasProviderImageService.findOne(imageID).get());

        } catch (IaaSProviderDoesNotExist | IaaSProviderImageDoesNotExist e) {
            e.printStackTrace();
        }

        return "resource::iaas-images-edit";
    }

    @RequestMapping(value = "/resource/iaas/{id}/instance", method = RequestMethod.GET)
    public String IaaSProviderInstancesView(@PathVariable("id") long id,
                                            final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/resource/iaas/" + id + "/instance");
        return "redirect:/";
    }

    @RequestMapping(value = "/resource/iaas/{id}/instance", method = RequestMethod.POST)
    public String IaaSProviderInstancesViewList(Model model, @PathVariable("id") long id) {
        //Retrieve backend structures
        try {
            IaaSProvider iaasProvider = (IaaSProvider) iaasProviderService.findOne(id).get();
            List<IaaSProviderInstance> iaasProviderInstancesList = iaasProvider.getIaasProviderInstances();
            //enrich model
            model.addAttribute("iaasProvider", iaasProvider);
            model.addAttribute("instances", iaasProviderInstancesList);


        } catch (IaaSProviderDoesNotExist e) {
            e.printStackTrace();
        }

        return "resource::iaas-instances";
    }

    @RequestMapping(value = "/resource/iaas/{id}/instance/add", method = RequestMethod.GET)
    public String IaaSProviderInstanceAddView(@PathVariable("id") long id,
                                              final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/resource/iaas/" + id + "/instance/add");
        return "redirect:/";
    }

    @RequestMapping(value = "/resource/iaas/{id}/instance/add", method = RequestMethod.POST)
    public String IaaSProviderInstanceViewAdd(Model model, @PathVariable("id") long id) {
        //Retrieve backend structures
        try {
            IaaSProvider iaasProvider = (IaaSProvider) iaasProviderService.findOne(id).get();
            //enrich model
            model.addAttribute("iaasProvider", iaasProvider);

            model.addAttribute("images", iaasProviderImageService.findIaaSProviderImagesByIaaSProvider(id));

        } catch (IaaSProviderDoesNotExist e) {
            e.printStackTrace();
        }

        return "resource::iaas-instances-add";
    }

    @RequestMapping(value = "/resource/iaas/{id}/instance/{instanceID}", method = RequestMethod.GET)
    public String IaaSProviderInstanceEditView(@PathVariable("id") long id,
                                               @PathVariable("instanceID") long imageID, final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/resource/iaas/" + id + "/instance/" + imageID);
        return "redirect:/";
    }

    @RequestMapping(value = "/resource/iaas/{id}/instance/{instanceID}", method = RequestMethod.POST)
    public String IaaSProviderInstanceViewEdit(Model model, @PathVariable("id") long id,
                                               @PathVariable("instanceID") long instanceID) {
        //Retrieve backend structures
        try {
            IaaSProvider iaasProvider = (IaaSProvider) iaasProviderService.findOne(id).get();
            //enrich model
            model.addAttribute("iaasProvider", iaasProvider);
            model.addAttribute("images", iaasProviderImageService.findIaaSProviderImagesByIaaSProvider(id));
            model.addAttribute("instance", iaasProviderInstanceService.findOne(instanceID).get());

        } catch (IaaSProviderDoesNotExist | IaaSProviderInstanceDoesNotExist e) {
            e.printStackTrace();
        }

        return "resource::iaas-instances-edit";
    }

    @RequestMapping(value = "/resource/slipstream", method = RequestMethod.GET)
    public String slipStreamListView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/resource/slipstream");
        return "redirect:/";
    }

    @RequestMapping(value = "/resource/slipstream", method = RequestMethod.POST)
    public String slipstreamList(Model model) {

        UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
        String username = userauthentication.getDetails().getUsername();

        User user = (User) userService.findByUsername(username).get();

        List<ProxyCloudProvider> proxyCloudProviders = proxyCloudProviderService.findByUser(user);

        UserCredential userCredential = userCredentialService.findByProxyCloudProviderAndUser(proxyCloudProviders.get(0).getId(), user.getId());

        List<CloudProviderModel> providers = null;
        List<VirtualMachineModel> vms = null;
        List<RunningInstanceModel> runningInstances = null;
        List<UsageModel> usages = null;

        if (null != userCredential) {

            model.addAttribute("credential", userCredential);

            model.addAttribute("proxyCloudProvider", proxyCloudProviders.get(0));

            model.addAttribute("user", user);

            // Fetch all info from SlipStream

            ProxyAdapter proxyAdapter = (ProxyAdapter) ((List) proxyAdapters.stream().filter(service -> service.getClass().getName().equals("eu.paasword.adapter.slipstream.SlipStreamAdapter")).collect(Collectors.toList())).get(0);

            CredentialsModel credentialsModel = new CredentialsModel();

            PaaSOfferingModel paaSOfferingModelSPI = new PaaSOfferingModel();
            paaSOfferingModelSPI.setEndpointURI(proxyCloudProviders.get(0).getConnectionURL());
            credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

            credentialsModel.setUsername(userCredential.getUsername());
            credentialsModel.setPassword(userCredential.getPassword());
            credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

            SPIResponse response = proxyAdapter.getCloudProviders(credentialsModel);

            if (null != response && response.getCode().equals(BasicResponseCode.SUCCESS)) {
                providers = (null != (List<CloudProviderModel>) response.getReturnobject()) ? (List<CloudProviderModel>) response.getReturnobject() : new ArrayList<>();
                model.addAttribute("providers", providers);
                model.addAttribute("providersSize", providers.size());
            }

            response = proxyAdapter.getVirtualMachines(credentialsModel);

            if (null != response && response.getCode().equals(BasicResponseCode.SUCCESS)) {
                vms = (null != (List<VirtualMachineModel>) response.getReturnobject()) ? (List<VirtualMachineModel>) response.getReturnobject() : new ArrayList<>();
                model.addAttribute("vmsSize", vms.size());
                model.addAttribute("vms", vms);
            }

            response = proxyAdapter.getRunningInstances(credentialsModel);

            if (null != response && response.getCode().equals(BasicResponseCode.SUCCESS)) {
                runningInstances = (null != (List<RunningInstanceModel>) response.getReturnobject()) ? (List<RunningInstanceModel>) response.getReturnobject() : new ArrayList<>();

                model.addAttribute("runningInstancesSize", runningInstances.size());

                model.addAttribute("runningInstances", runningInstances);
            }

//            response = proxyAdapter.getUsages(credentialsModel);
//
//            if (null != response && response.getCode().equals(BasicResponseCode.SUCCESS)) {
//                usages = (null != (List<UsageModel>) response.getReturnobject()) ? (List<UsageModel>) response.getReturnobject() : new ArrayList<>();
//
//                model.addAttribute("usagesSize", usages.size());
//                model.addAttribute("usages", usages);
//            }

        }

        return "resource::slipstream-list";

    }

    @RequestMapping(value = "/resource/slipstream/vm", method = RequestMethod.GET)
    public String slipStreamVmListView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/resource/slipstream/vm");
        return "redirect:/";
    }

    @RequestMapping(value = "/resource/slipstream/vm", method = RequestMethod.POST)
    public String slipstreamVmList(Model model) {

        UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
        String username = userauthentication.getDetails().getUsername();

        User user = (User) userService.findByUsername(username).get();

        List<ProxyCloudProvider> proxyCloudProviders = proxyCloudProviderService.findByUser(user);

        UserCredential userCredential = userCredentialService.findByProxyCloudProviderAndUser(proxyCloudProviders.get(0).getId(), user.getId());

        List<VirtualMachineModel> vms = null;

        if (null != userCredential) {

            model.addAttribute("credential", userCredential);

            model.addAttribute("proxyCloudProvider", proxyCloudProviders.get(0));

            model.addAttribute("user", user);

            // Fetch all info from SlipStream

            ProxyAdapter proxyAdapter = (ProxyAdapter) ((List) proxyAdapters.stream().filter(service -> service.getClass().getName().equals("eu.paasword.adapter.slipstream.SlipStreamAdapter")).collect(Collectors.toList())).get(0);

            CredentialsModel credentialsModel = new CredentialsModel();

            PaaSOfferingModel paaSOfferingModelSPI = new PaaSOfferingModel();
            paaSOfferingModelSPI.setEndpointURI(proxyCloudProviders.get(0).getConnectionURL());
            credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

            credentialsModel.setUsername(userCredential.getUsername());
            credentialsModel.setPassword(userCredential.getPassword());
            credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

            SPIResponse response = proxyAdapter.getVirtualMachines(credentialsModel);

            if (null != response && response.getCode().equals(BasicResponseCode.SUCCESS)) {
                vms = (null != (List<VirtualMachineModel>) response.getReturnobject()) ? (List<VirtualMachineModel>) response.getReturnobject() : new ArrayList<>();
                model.addAttribute("vmsSize", vms.size());
                model.addAttribute("vms", vms);
            }

        }

        return "resource::slipstream-list-vm";
    }

    @RequestMapping(value = "/resource/slipstream/instance", method = RequestMethod.GET)
    public String slipStreamInstanceListView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/resource/slipstream/instance");
        return "redirect:/";
    }

    @RequestMapping(value = "/resource/slipstream/instance", method = RequestMethod.POST)
    public String slipstreamInstanceList(Model model) {

        UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
        String username = userauthentication.getDetails().getUsername();

        User user = (User) userService.findByUsername(username).get();

        List<ProxyCloudProvider> proxyCloudProviders = proxyCloudProviderService.findByUser(user);

        UserCredential userCredential = userCredentialService.findByProxyCloudProviderAndUser(proxyCloudProviders.get(0).getId(), user.getId());

        List<RunningInstanceModel> runningInstances = null;

        if (null != userCredential) {

            model.addAttribute("credential", userCredential);

            model.addAttribute("proxyCloudProvider", proxyCloudProviders.get(0));

            model.addAttribute("user", user);

            // Fetch all info from SlipStream

            ProxyAdapter proxyAdapter = (ProxyAdapter) ((List) proxyAdapters.stream().filter(service -> service.getClass().getName().equals("eu.paasword.adapter.slipstream.SlipStreamAdapter")).collect(Collectors.toList())).get(0);

            CredentialsModel credentialsModel = new CredentialsModel();

            PaaSOfferingModel paaSOfferingModelSPI = new PaaSOfferingModel();
            paaSOfferingModelSPI.setEndpointURI(proxyCloudProviders.get(0).getConnectionURL());
            credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

            credentialsModel.setUsername(userCredential.getUsername());
            credentialsModel.setPassword(userCredential.getPassword());
            credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

            SPIResponse response = proxyAdapter.getRunningInstances(credentialsModel);

            if (null != response && response.getCode().equals(BasicResponseCode.SUCCESS)) {
                runningInstances = (null != (List<RunningInstanceModel>) response.getReturnobject()) ? (List<RunningInstanceModel>) response.getReturnobject() : new ArrayList<>();

                model.addAttribute("runningInstancesSize", runningInstances.size());

                model.addAttribute("runningInstances", runningInstances);
            }

        }

        return "resource::slipstream-list-instance";
    }

    @RequestMapping(value = "/resource/slipstream/usage", method = RequestMethod.GET)
    public String slipStreamUsageListView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/resource/slipstream/usage");
        return "redirect:/";
    }

    @RequestMapping(value = "/resource/slipstream/usage", method = RequestMethod.POST)
    public String slipstreamUsageList(Model model) {

        UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
        String username = userauthentication.getDetails().getUsername();

        User user = (User) userService.findByUsername(username).get();

        List<ProxyCloudProvider> proxyCloudProviders = proxyCloudProviderService.findByUser(user);

        UserCredential userCredential = userCredentialService.findByProxyCloudProviderAndUser(proxyCloudProviders.get(0).getId(), user.getId());

        List<UsageModel> usages = null;

        if (null != userCredential) {

            model.addAttribute("credential", userCredential);

            model.addAttribute("proxyCloudProvider", proxyCloudProviders.get(0));

            model.addAttribute("user", user);

            // Fetch all info from SlipStream

            ProxyAdapter proxyAdapter = (ProxyAdapter) ((List) proxyAdapters.stream().filter(service -> service.getClass().getName().equals("eu.paasword.adapter.slipstream.SlipStreamAdapter")).collect(Collectors.toList())).get(0);

            CredentialsModel credentialsModel = new CredentialsModel();

            PaaSOfferingModel paaSOfferingModelSPI = new PaaSOfferingModel();
            paaSOfferingModelSPI.setEndpointURI(proxyCloudProviders.get(0).getConnectionURL());
            credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

            credentialsModel.setUsername(userCredential.getUsername());
            credentialsModel.setPassword(userCredential.getPassword());
            credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

            SPIResponse response = proxyAdapter.getUsages(credentialsModel);

            if (null != response && response.getCode().equals(BasicResponseCode.SUCCESS)) {
                usages = (null != (List<UsageModel>) response.getReturnobject()) ? (List<UsageModel>) response.getReturnobject() : new ArrayList<>();

                model.addAttribute("usagesSize", usages.size());
                model.addAttribute("usages", usages);
            }
        }

        return "resource::slipstream-list-usage";
    }

    @RequestMapping(value = "/resource/slipstream/authorize", method = RequestMethod.GET)
    public String SlipStreamAuthorizeView(final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("redirectToPage", "/resource/slipstream/authorize");
        return "redirect:/";
    }

    @RequestMapping(value = "/resource/slipstream/authorize", method = RequestMethod.POST)
    public String SlipStreamAuthorize(Model model) {

        UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
        String username = userauthentication.getDetails().getUsername();

        User user = (User) userService.findByUsername(username).get();

        model.addAttribute("user", user);

        return "resource::slipstream-authorize";
    }


}//EoC
