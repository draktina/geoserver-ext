package eu.europa.emsa.http;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HttpUrlBuilder {

    private URL baseUrl;
    private StringBuilder baseUrlStr;
    private Map<String, String> paramsMap;
    private boolean isFirstAdd = true;

    private static final String QUESTION_MARK = "?";
    private static final String EQUAL = "=";
    private static final String AMPERSAND = "&amp;";
    private static final String UTF_8 = "UTF-8";

    public HttpUrlBuilder(URL baseUrl) throws MalformedURLException {
        if(baseUrl == null)
            throw new MalformedURLException("Null URL passed in constructor");

        String str = baseUrl.toString();
        if(str.isEmpty() || str ==null)
            throw new MalformedURLException("String Value of Url was blank.");

        this.baseUrl = baseUrl;
        this.baseUrlStr = new StringBuilder(str);
        paramsMap = new HashMap<String, String>();
    }

    public void put(String name, String value) {
        paramsMap.put(name, value);
    }

    public void remove(String name) {
        paramsMap.remove(name);
    }

    public URL toUrl() throws MalformedURLException {
        Iterator<String> it = paramsMap.keySet().iterator();
        while(it.hasNext()) {
            String key = it.next();
            String value = paramsMap.get(key);
            if(isFirstAdd) {
                isFirstAdd = false;
                if(baseUrlStr.indexOf(QUESTION_MARK) < 1) {
                    //does not contain "?"
                    baseUrlStr.append(QUESTION_MARK);
                    encode(key, value);
                } else {
                    //contains "?"
                    baseUrlStr.append(AMPERSAND);
                   encode(key, value);
                }
            } else {
                baseUrlStr.append(AMPERSAND);
                encode(key, value);
            }
        }

        URL retVal = new URL(baseUrlStr.toString());
        baseUrlStr = new StringBuilder(baseUrl.toString());
        isFirstAdd = true;

        return retVal;
    }

    private void encode(String name, String value) {
        try{
            baseUrlStr.append(URLEncoder.encode(name, UTF_8));
            baseUrlStr.append(EQUAL);
            baseUrlStr.append(URLEncoder.encode(value, UTF_8));
        } catch (UnsupportedEncodingException  uee) {
            throw new RuntimeException("VM does not support UTF-8 encoding");
        }
    }


}
