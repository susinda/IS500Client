package org.susi.IS500ClientProject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.carbon.identity.entitlement.filter.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.proxy.PEPProxy;
import org.wso2.carbon.identity.entitlement.proxy.PEPProxyConfig;

public class IS500PEPTestClient {

    public static void main( String[] args ) throws Exception
    {
    	setKeyStores();
    	
    	String currentTime = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
    	
    	//Configurations
		String backEndServerURL = "https://localhost:9443/services/";
		String adminUserName = "admin";
		String adminPassword = "admin";
        String cacheType = "simple";
        int invalidationInterval = 0;
        int maxCacheEntries = 0;
        String appName = "TestApp";
    	
        Map<String,Map<String,String>> appToPDPClientConfigMap = new HashMap<String, Map<String,String>>();
        Map<String,String> clientConfigMap = new HashMap<String, String>();
        clientConfigMap.put(EntitlementConstants.SERVER_URL, backEndServerURL);
        clientConfigMap.put(EntitlementConstants.USERNAME, adminUserName);
        clientConfigMap.put(EntitlementConstants.PASSWORD, adminPassword);
        appToPDPClientConfigMap.put(appName, clientConfigMap); 
        PEPProxyConfig config = new PEPProxyConfig(appToPDPClientConfigMap, appName, cacheType, invalidationInterval, maxCacheEntries);

        
        //Calling getDesision
        String subject = "susinda";
        String resource = "ABCresource";
        String action = "read";
        String environment = currentTime;
        PEPProxy pepProxy = new PEPProxy(config);
        String decision = pepProxy.getDecision(subject, resource, action, environment);
       
        
        //output
        System.out.println(decision);
        OMElement decisionElement = AXIOMUtil.stringToOM(decision);
        OMElement child2 = decisionElement.getFirstChildWithName(new QName("urn:oasis:names:tc:xacml:3.0:core:schema:wd-17", "Result"));
        OMElement child3 = child2.getFirstChildWithName(new QName("urn:oasis:names:tc:xacml:3.0:core:schema:wd-17", "Decision"));
        System.out.println(child3.getText());
    	
    }
    
    public static void setKeyStores() throws Exception {
        //set trust store, you need to import server's certificate
		System.setProperty("javax.net.ssl.trustStoreType", "JKS");
		System.setProperty("javax.net.ssl.trustStore", getKeyStorePath("wso2carbon.jks")); // keep wso2carbon.jks at src/main/resources/wso2carbon.jks
		System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
	}
    
    private static String getKeyStorePath(String keyStoreName) throws Exception {
		File file = new File((new File(".")).getCanonicalPath() + File.separator + "src" + File.separator + 
									"main" + File.separator + "resources" +  File.separator + keyStoreName);
		if(!file.exists()){
            throw new Exception("Key Store file can not be found in " + file.getCanonicalPath());
        }
		return file.getCanonicalPath();
	}
}
