package com.omertron.pushoverapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.lang3.StringUtils;

public class PushoverApi {

    /**
     * The application token from PushoverApi.net's application section.
     */
    private String appToken;
    /**
     * The user token, from a user's user key page.
     */
    private String userToken;
    /**
     * The device, an optional field that allows a user to specify a device on the account to which the message should be sent.
     */
    private String device = "";
    /**
     * The generated authentication string based on the tokens and device
     */
    private String authenticationTokens;

    /*
     * Constants
     */
    private static final String PUSHOVER_URL = "https://api.pushover.net/1/messages.json";
    private static final String PO_MESSAGE = "&message=";
    private static final String PO_TITLE = "&title=";
    private static final String PO_PRIORITY = "&priority=";
    private static final String PO_URL = "&url=";
    private static final String ENCODING = "UTF-8";
    private static final Integer PRIORITY_DEF = 0;
    private static final Integer PRIORITY_MIN = -1;
    private static final Integer PRIORITY_MAX = 1;

    /**
     * Initializes a PushoverApi object for talking to PushoverApi.
     *
     * @param appToken Your application token, generated from PushoverApi.net
     * @param userToken A user's usertoken, found on the user page from PushoverApi.net
     */
    public PushoverApi(String appToken, String userToken) {
        this.appToken = appToken;
        this.userToken = userToken;
        this.authenticationTokens = getAuthenticationTokens();
    }

    /**
     * Initializes a PushoverApi object for talking to PushoverApi.
     *
     * @param appToken Your application token, generated from PushoverApi.net
     * @param userToken A user's usertoken, found on the user page from PushoverApi.net
     * @param device The device to send the message to.
     */
    public PushoverApi(String appToken, String userToken, String device) {
        this.appToken = appToken;
        this.userToken = userToken;
        this.device = device;
        this.authenticationTokens = getAuthenticationTokens();
    }

    /**
     * Gets the application token associated with this object
     *
     * @return appToken The application token
     */
    public String getAppToken() {
        return appToken;
    }

    /**
     * Sets a new application token for interfacing with PushoverApi. All further requests will be sent using this app token.
     *
     * @param appToken
     */
    public void setAppToken(String appToken) {
        this.appToken = appToken;
    }

    /**
     * Gets the user token associated with this object
     *
     * @return userToken
     */
    public String getUserToken() {
        return userToken;
    }

    /**
     * Sets a new user token for interfacing with PushoverApi. All further requests will be sent using this user token.
     *
     * @param userToken
     */
    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    /**
     * Gets the user's device.
     *
     * @return device
     */
    public String getDevice() {
        return device;
    }

    /**
     * Sets the user's destination device.
     *
     * @param device The device name as set by the user in PushoverApi
     */
    public void setDevice(String device) {
        this.device = device;
    }

    /**
     * Sends a message.
     *
     * @param message The message to send.
     * @param title The title.
     * @param url A URL.
     * @param urlTitle A URL title.
     * @param priority The priority of the message
     * @return JSON reply from PushoverApi.
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public String sendMessage(String message, String title, String url, String urlTitle,Integer priority) throws UnsupportedEncodingException, IOException {
        return sendToPushover(sendMessage(message, title, url, urlTitle, priority));
    }

    /**
     * Sends a message.
     *
     * @param message The message to send.
     * @param title The title.
     * @param url A URL.
     * @param urlTitle A URL title.
     * @return JSON reply from PushoverApi.
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public String sendMessage(String message, String title, String url, String urlTitle) throws UnsupportedEncodingException, IOException {
        return sendMessage(message, title, url, urlTitle, PRIORITY_DEF);
    }

    /**
     * Sends a message.
     *
     * @param message The message to send.
     * @param title The title.
     * @param url A URL.
     * @return JSON reply from PushoverApi.
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public String sendMessage(String message, String title, String url) throws UnsupportedEncodingException, IOException {
        return sendMessage(message, title, url, "");
    }

    /**
     * Sends a message.
     *
     * @param message The message to send.
     * @param title The title.
     * @return JSON reply from PushoverApi.
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public String sendMessage(String message, String title) throws UnsupportedEncodingException, IOException {
        return sendMessage(message, title, "");
    }

    /**
     * Sends a message.
     *
     * @param message The message to send.
     * @return JSON reply from PushoverApi.
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public String sendMessage(String message) throws UnsupportedEncodingException, IOException {
        return sendMessage(message, "");
    }

    /**
     * Generate the string to send to PushoverApi
     *
     * @param message The message to send
     * @param title The title
     * @param url A URL
     * @param urlTitle A URL Title
     * @param priority Priority of the message, default = 0
     * @return The generated string for PushoverApi
     * @throws UnsupportedEncodingException
     */
    private String generateMessageString(String message, String title, String url, String urlTitle, int priority) throws UnsupportedEncodingException {
        StringBuilder poMessage = new StringBuilder(authenticationTokens);

        if (StringUtils.isNotBlank(message)) {
            poMessage.append(PO_MESSAGE).append(URLEncoder.encode(message, ENCODING));
        }

        if (StringUtils.isNotBlank(title)) {
            poMessage.append(PO_TITLE).append(URLEncoder.encode(title, ENCODING));
        }

        if (StringUtils.isNotBlank(url)) {
            poMessage.append(PO_URL).append(URLEncoder.encode(url, ENCODING));
        }

        if (priority != PRIORITY_DEF) {
            poMessage.append(PO_PRIORITY);
            if (priority <= PRIORITY_MIN) {
                poMessage.append(PRIORITY_MIN);
            } else if (priority >= PRIORITY_MAX) {
                poMessage.append(PRIORITY_MAX);
            } else {
                poMessage.append(priority);
            }
        }

        return poMessage.toString();
    }

    /**
     * Gets a string with the auth tokens already made.
     *
     * @return String of auth tokens
     * @throws UnsupportedEncodingException
     */
    private String getAuthenticationTokens() {
        StringBuilder authToken = new StringBuilder("token=");
        authToken.append(getAppToken());
        authToken.append("&user=");
        authToken.append(getUserToken());

        if (StringUtils.isNotBlank(device)) {
            authToken.append("&device=");
            authToken.append(getDevice());
        }

        return authToken.toString();
    }

    /**
     * Sends a raw bit of text via POST to PushoverApi.
     *
     * @param message
     * @return JSON reply from PushoverApi.
     * @throws IOException
     */
    private String sendToPushover(String message) throws IOException {
        URL pushoverUrl = new URL(PUSHOVER_URL);

        HttpsURLConnection connection = (HttpsURLConnection) pushoverUrl.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);

        OutputStream outputStream = null;
        try {
            outputStream = connection.getOutputStream();
            outputStream.write(message.getBytes(Charset.forName("UTF-8")));
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }

        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuilder output = new StringBuilder();
        try {
            isr = new InputStreamReader(connection.getInputStream());
            br = new BufferedReader(isr);
            connection.disconnect();

            String outputCache;
            while ((outputCache = br.readLine()) != null) {
                output.append(outputCache);
            }
        } finally {
            if (isr != null) {
                isr.close();
            }
            if (br != null) {
                br.close();
            }
        }
        return output.toString();
    }
}