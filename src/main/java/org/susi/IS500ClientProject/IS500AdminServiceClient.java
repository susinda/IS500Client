package org.susi.IS500ClientProject;

import java.io.File;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.identity.application.common.model.xsd.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.xsd.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.xsd.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.um.ws.api.WSUserStoreManager;
import org.wso2.carbon.user.core.UserStoreException;


public class IS500AdminServiceClient 
{
	static String backEndServerURL = "https://localhost:9443/services/";
	static ConfigurationContext configCtx = null;
	static String authCokie = null;
	static ApplicationManagementClient appManagementClient = null;
	static WSUserStoreManager remoteUserStoreManager = null;
	
    public static void main( String[] args ) throws Exception
    {
    	setKeyStores();
    	
    	configCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
    	authCokie = authenticateUser("admin", "admin");
    	appManagementClient = new ApplicationManagementClient(authCokie, backEndServerURL, configCtx);
    	remoteUserStoreManager = new WSUserStoreManager(backEndServerURL, authCokie, configCtx);
    	
    	//step 2
    	String applicationName = "App4gs";
    	ServiceProvider sp = createApp(applicationName);
    	
    	//step 4
    	updateApp(sp.getApplicationName());
    	
    	//step 6 and 7
    	String userName = applicationName+"User";
    	String password = userName + "passwd";
    	createUserandAssignRole(userName, password, applicationName);
    	
    	//to view the current apps
    	getAllApplicationBasicInfo();
    }


	private static ServiceProvider createApp(String appName) throws Exception {
		ServiceProvider sp = new ServiceProvider();
    	sp.setApplicationName(appName);
    	sp.setDescription(appName + " created from my java client");
    	appManagementClient.createApplication(sp);
    	System.out.println( "App created sucessfully!" );
		return sp;
	}
    
	private static void updateApp(String appName) throws Exception {
		PermissionsAndRoleConfig pnrConfig = new PermissionsAndRoleConfig();
    	ApplicationPermission permission = new ApplicationPermission();
    	permission.setValue(appName + "Permission1");
    	pnrConfig.setPermissions(new ApplicationPermission[] {permission});
    	
    	ServiceProvider sp = appManagementClient.getApplication(appName);
    	sp.setPermissionAndRoleConfig(pnrConfig);
    	appManagementClient.updateApplicationData(sp);
    	System.out.println( "App updated sucessfully!" );
	}

	private static void createUserandAssignRole(String userName, String password, String roleName) throws AxisFault, UserStoreException {
		remoteUserStoreManager.addUser(userName, password, new String[]{"Internal/" + roleName}, null, null);
        System.out.println("Added user: " + userName + " with role: " + roleName);
	}
	
	private static void getAllApplicationBasicInfo() throws Exception {
		ApplicationBasicInfo[] arr = appManagementClient.getAllApplicationBasicInfo();
		System.out.println("Current App list size is " + arr.length);
	}
	
    
    private static void setKeyStores() throws Exception {
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
	
	private static String authenticateUser(String userName, String password) throws Exception {

	        ServiceClient client = null;
	        Options option = null;
	        String authCookie = null;
	 
	        String serviceURL = backEndServerURL + "AuthenticationAdmin";
	        AuthenticationAdminStub authStub = new AuthenticationAdminStub(configCtx, serviceURL);
	        client = authStub._getServiceClient();
	        option = client.getOptions();
	        option.setManageSession(true);
	        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, authCookie);
	        boolean isAuthenticated = authStub.login(userName, password, "127.0.0.1");
	        authCookie = (String) authStub._getServiceClient().getServiceContext().getProperty(HTTPConstants.COOKIE_STRING);
	        if (isAuthenticated) {
	        	 return authCookie;
	        } else {
	        	 return null;
	        }
	   }
}
