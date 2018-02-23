package eu.paasword.rest.bounce;

import eu.paasword.repository.relational.domain.ApplicationInstanceUser;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class BounceService {

    static final Logger logger = Logger.getLogger(BounceService.class.getName());

    @Autowired
    Environment environment;

    static RestTemplate restTemplate;

    static ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault());

    public void sendEmailForUserKey(ApplicationInstanceUser user) {

        if (null != user) {

            EmailMessage message = new EmailMessage();

            // Creating Email
            message.setMailTo(user.getEmail());
            message.setMailToName(user.getPrincipal());
            message.setMailFrom("paasword-noreply@ubitech.eu");
            message.setMailFromName("PaaSword Framework");
            message.setSubject("[PAASWORD] User's key part for application: " + user.getApplicationInstanceID().getApplicationID().getName());

            String emailContent = EMAIL_TEMPLATE;
            emailContent = emailContent.replace("^USER_KEY^", user.getUserKey());
            emailContent = emailContent.replace("^APP_NAME^", user.getApplicationInstanceID().getApplicationID().getName());
            emailContent = emailContent.replace("^USERNAME^", user.getPrincipal());

            message.setHtml(emailContent);
            message.setImportant(true);
            message.setTrackOpens(true);
            message.setTrackClicks(true);

            sendEmail(message);

        }

    }

    private void sendEmail(EmailMessage email) {

        String bounceURL = environment.getProperty("bounce.service.url") + "messages/send.json";
        String apiKey = environment.getProperty("bounce.service.apikey");

        // Construct Message
        JSONObject emailJSON = new JSONObject();
        emailJSON.put("key", apiKey);
        emailJSON.put("async", email.isAsync());

        JSONObject messageJSON = new JSONObject();
        if (null != email.getHtml() && !email.getHtml().isEmpty()) {
            messageJSON.put("html", email.getHtml());
        }
        if (null != email.getText() && !email.getText().isEmpty()) {
            messageJSON.put("text", email.getText());
        }
        if (null != email.getSubject() && !email.getSubject().isEmpty()) {
            messageJSON.put("subject", email.getSubject());
        }
        if (null != email.getMailFrom() && !email.getMailFrom().isEmpty()) {
            messageJSON.put("from_email", email.getMailFrom());
        }
        if (null != email.getMailFromName() && !email.getMailFromName().isEmpty()) {
            messageJSON.put("from_name", email.getMailFromName());
        }

        // TODO
        if (null != email.getMailTo() && !email.getMailTo().isEmpty()) {

            JSONArray toArray = new JSONArray();

            if (email.getMailTo().contains(",")) {

                String[] emails = email.getMailTo().split("\\,");

                for (String tempEmail : emails) {

                    JSONObject to = new JSONObject();
                    to.put("email", tempEmail);
                    to.put("name", tempEmail);
                    to.put("type", "to");
                    toArray.put(to);
                }

            } else {

                JSONObject to = new JSONObject();
                to.put("email", email.getMailTo());
                to.put("name", email.getMailToName());
                to.put("type", "to");
                toArray.put(to);
            }
            messageJSON.put("to", toArray);
        }

        if (null != email.getReplyTo() && !email.getReplyTo().isEmpty()) {

            JSONArray headersArray = new JSONArray();

            JSONObject replyTo = new JSONObject();
            replyTo.put("Reply-To", email.getReplyTo());
            headersArray.put(replyTo);

            messageJSON.put("headers", headersArray);

        } else {

            JSONArray headersArray = new JSONArray();

            JSONObject replyTo = new JSONObject();
            replyTo.put("Reply-To", "mantzouratos.s@gmail.com");

            headersArray.put(replyTo);

            messageJSON.put("headers", headersArray);

        }

        messageJSON.put("important", email.isImportant());
        messageJSON.put("track_opens", email.isTrackOpens());
        messageJSON.put("track_clicks", email.isTrackClicks());
        messageJSON.put("auto_html", true);
        messageJSON.put("inline_css", true);

        if (null != email.getMailBcc() && !email.getMailBcc().isEmpty()) {

            messageJSON.put("bcc_address", email.getMailBcc());

        }

        if (null != email.getImages() && !email.getImages().isEmpty()) {

            JSONArray imagesArray = new JSONArray();

            email.getImages().stream().forEach(emailImage -> {

                JSONObject image = new JSONObject();
                image.put("type", emailImage.getType());
                image.put("name", emailImage.getName());
                image.put("content", emailImage.getContent());

                imagesArray.put(image);
            });

            messageJSON.put("images", imagesArray);

        } else {

            JSONArray imagesArray = new JSONArray();

            JSONObject image = new JSONObject();
            image.put("type", "image/png");
            image.put("name", "paaswordlogo");
            image.put("content", PAASWORD_LOGO_BASE64);

            imagesArray.put(image);

            messageJSON.put("images", imagesArray);

        }

        emailJSON.put("message", messageJSON);

//        logger.info("Email Message: " + emailJSON.toString());

        // Send email via Mandrill
        if (null == restTemplate) {
            restTemplate = new RestTemplate(requestFactory);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.put("Content-Type", Arrays.asList("application/json; charset=utf-8"));
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity entity = new HttpEntity(emailJSON.toString(), headers);

        try {

            ResponseEntity<String> responseEntity = restTemplate.exchange(bounceURL, HttpMethod.POST, entity, String.class);

            if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

                try {

                    JSONArray bounceResponse = new JSONArray(responseEntity.getBody());

                    JSONObject messageResponseJSON = bounceResponse.getJSONObject(0);

                    if (!messageResponseJSON.getString("status").equals("rejected") && !messageResponseJSON.getString("status").equals("invalid")) {

                        logger.info("Response (SUCCESS): " + responseEntity.getBody());

                    } else {

                        logger.severe(messageResponseJSON.toString());

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }

            } else {
                logger.severe("Response (FAILED): " + responseEntity.getBody());
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

    }

    ///
    private static final String PAASWORD_LOGO_BASE64 = "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";
    private static final String EMAIL_TEMPLATE = "<!DOCTYPE html><html> <head> <title>[PAASWORD] User key part</title> <meta charset=\"utf-8\" /> <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" /> <meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge\" /> </head> <body> <div align=\"center\" marginheight=\"0\" marginwidth=\"0\" style=\"font-family:'Avenir Next','Helvetica,sans-serif'\"> <table align=\"center\" bgcolor=\"#fafafa\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"> <tbody> <tr> <td align=\"center\" bgcolor=\"#fafafa\" valign=\"top\" width=\"100%\"> <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:600px\" width=\"100%\"> <tbody> <tr> <td> <div style=\"font-size:1px;color:#ffffff;line-height:1px;max-height:0px;max-width:0px;overflow:hidden\"> </div> </td> </tr> <tr> <td align=\"center\" style=\"height:10px\"> </td> </tr> <tr> <td align=\"center\" bgcolor=\"#fafafa\" width=\"100%\"> </td> </tr> <tr> <td align=\"center\" bgcolor=\"#fafafa\" width=\"100%\"> <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"> <tbody> <tr> <td align=\"center\" style=\"padding-bottom:20px\"> <img  src=\"cid:paaswordlogo\" style=\"align:center;width: 300px;\" /> </td> </tr> </tbody> </table> </td> </tr> </tbody> </table> <table align=\"center\" bgcolor=\"#FFFFFF\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\"> <tbody> <tr> <td align=\"center\" style=\"height:40px\"></td> </tr> <tr> <td align=\"center\" bgcolor=\"#FFFFFF\" style=\"padding:0 40px\"> <table align=\"center\"> <tbody> <tr> <td align=\"left\" style=\"font-family:'Avenir Next',Helvetica,sans-serif!important;font-size:14px;color:#363636;line-height:20px\"> <p style=\"line-height:150%;padding-bottom:5px\">Dear ^USERNAME^,</p> </td> </tr> <tr> <td align=\"left\" style=\"font-family:'Avenir Next',Helvetica,sans-serif!important;font-size:14px;color:#363636;line-height:20px\"> Your key for application ^APP_NAME^ is ^USER_KEY^. If you received this by mistake or were not expecting it, please disregard this email. </td> </tr> <tr> <td align=\"left\" style=\"font-family:'Avenir Next',Helvetica,sans-serif!important;font-size:14px;color:#363636;line-height:20px\"> <p style=\"line-height:150%;padding-bottom:5px\">Kind regards,</p> <p style=\"margin-top:20;line-height:150%;padding-bottom:5px\"> The PaaSword team. </p> </td> </tr> </tbody> </table> </td> </tr> </tbody> </table> <table align=\"center\" bgcolor=\"#FFFFFF\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\"> <tbody> <tr> <td align=\"center\" style=\"padding:0 40px\" width=\"600\"> <table> <tbody> <tr> <td align=\"center\"></td> </tr> <tr> <td style=\"height:10px\"></td> </tr> </tbody> </table> </td> </tr> </tbody> </table> <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:600px\" width=\"100%\"> <tbody> <tr> <td> <div style=\"font-size:1px;color:#ffffff;line-height:1px;max-height:0px;max-width:0px;overflow:hidden\"> </div> </td> </tr> <tr> <td align=\"center\" style=\"height:10px\"> </td> </tr> <tr> <td align=\"center\" bgcolor=\"#fafafa\" width=\"100%\"> </td> </tr> <tr> <td align=\"center\" bgcolor=\"#fafafa\" width=\"100%\" style=\"padding-bottom:60px\"></td> </tr> </tbody> </table> </td> </tr> </tbody> </table> </div> </body></html>";

}
