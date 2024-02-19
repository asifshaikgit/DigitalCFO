package model;

import com.idos.cache.IdosConfigParamCache;
import com.idos.cache.OrganizationConfigCache;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import javax.inject.Inject;

public class ConfigParams {

	private String companyApp = null;
	private String companyName = null;
	private String companyTitle = null;
	private String companyLogoPath = null;
	private String companyLogoPath2 = null;
	private String companyFeviconPath = null;
	private String companyOwner = null;
	private String companyAddress = null;
	private String companyPhone = null;
	private String companyMobile = null;
	private String companyEmail = null;
	private String CompanySupportEmail = null;
	private String companyWebsite = null;
	private String companyTheme = null;
	private String companyUrl = null;
	private String deployMode = null;
	private static String ssoPwcApiHost = null;
	private static String ssoPwcClientId = null;
	private static String ssoPwcClientSecret = null;
	private static String ssoPwcRedirectUrl = null;
	private static String ssoPwcScope = null;
	// private Boolean deploymentSingleUser = null;
	private Integer userMode = 0;
	private Integer readPaymodeOnApproval = null;
	private static ConfigParams configParams = null;
	private Integer instanceDeploymentMode = null;
	private Boolean isMailOff = null;
	private String isProcurementEnabled = "false";
	private String deploymentType = null;
	private String httpHost = null;
	private String httpsHost = null;
	private String numberOfDocUploadsAllowed = null;
	static {
		configParams = new ConfigParams();
	}

	public ConfigParams() {
		Config config = ConfigFactory.load();
		System.out.println("SK >>>>>>>>>>>>>>>>> Inside ConfigParams const " + configParams);
		companyApp = config.getString("company.app");
		companyName = config.getString("company.name");
		companyTitle = config.getString("company.title");
		companyLogoPath = config.getString("company.logo.path");
		// companyLogoPath2 = config.getString("company.logo.path2");
		companyFeviconPath = config.getString("company.favicon.path");
		companyOwner = config.getString("company.owner");
		companyAddress = config.getString("company.address");
		companyPhone = config.getString("company.phone");
		companyMobile = config.getString("company.mobile");
		companyEmail = config.getString("company.email");
		CompanySupportEmail = config.getString("company.support.email");
		companyWebsite = config.getString("company.website");
		companyTheme = config.getString("company.theme");
		// deploymentSingleUser =
		// Play.application().configuration().getBoolean("deployment.single.user");
		companyUrl = config.getString("company.url");
		deployMode = config.getString("company.deploy.mode"); // SINGLE,MULTI,MIX
		readPaymodeOnApproval = config.getInt("read.paymode.onapproval");
		instanceDeploymentMode = config.getInt("instance.deployment.mode");
		ssoPwcApiHost = config.getString("sso.pwcApiHost");
		ssoPwcClientId = config.getString("sso.pwcClientId");
		ssoPwcClientSecret = config.getString("sso.pwcClientSecret");
		ssoPwcRedirectUrl = config.getString("sso.pwcRedirectUrl");
		ssoPwcScope = config.getString("sso.pwcScope");
		isProcurementEnabled = config.getString("isProcurementEnabled");
		deploymentType = config.getString("deployment.type");
		httpHost = config.getString("http.host");
		httpsHost = config.getString("https.host");
		numberOfDocUploadsAllowed = config.getString("numberOfDocUploadsAllowed");
	}

	public static ConfigParams getInstance() {
		System.out.println("SK >>>>>>>>>>>>>>>>> Inside ConfigParams Instance" + configParams);
		return configParams;
	}

	public static void initialize() {
		configParams = new ConfigParams();
	}

	public String getCompanyApp() {
		return companyApp;
	}

	public String getCompanyName() {
		return companyName;
	}

	public String getCompanyTitle() {
		return companyTitle;
	}

	public String getCompanyFeviconPath() {
		return companyFeviconPath;
	}

	public String getCompanyLogoPath() {
		return companyLogoPath;
	}

	public String getCompanyOwner() {
		return companyOwner;
	}

	public String getCompanyAddress() {
		return companyAddress;
	}

	public String getCompanyPhone() {
		return companyPhone;
	}

	public String getCompanyMobile() {
		return companyMobile;
	}

	public String getCompanyEmail() {
		return companyEmail;
	}

	public String getDeployMode() {
		return deployMode;
	}

	public Boolean isDeploymentSingleUser(Users user) {
		if (user != null && user.getOrganization().getUserMode() != null && user.getOrganization().getUserMode() == 1) {
			return true;
		}
		return false;
	}

	public String getCompanySupportEmail() {
		return CompanySupportEmail;
	}

	public String getCompanyWebsite() {
		return companyWebsite;
	}

	public String getCompanyTheme() {
		return companyTheme;
	}

	public String getPoweredBy() {
		String powerBy = "Powered by " + getCompanyApp();
		if (getCompanyWebsite() != null) {
			powerBy += " (" + getCompanyWebsite() + ")";
		}
		return powerBy;
	}

	public String getCompanyUrl() {
		return this.companyUrl;
	}

	public Integer getReadPaymodeOnApproval() {
		return readPaymodeOnApproval;
	}

	public Integer getInstanceDeploymentMode() {
		return this.instanceDeploymentMode;
	}

	public void setInstanceDeploymentMode(final Integer instanceDeploymentMode) {
		this.instanceDeploymentMode = instanceDeploymentMode;
	}

	public String getIdosConfigParamValue(String key) {
		return IdosConfigParamCache.getParamValue(key);
	}

	public String getOrganizationConfigParamValue(long orgId, String key) {
		return OrganizationConfigCache.getParamValue(orgId, key);
	}

	public Boolean getIsMailOff(String key) {
		if (IdosConfigParamCache.getParamValue(key) != null
				&& IdosConfigParamCache.getParamValue(key).equalsIgnoreCase("1")) {
			return true;
		} else {
			return false;
		}
	}

	public void setIsMailOff(final Boolean mailDisabled) {
		this.isMailOff = mailDisabled;
	}

	public String getCompanyLogoPath2() {
		return this.companyLogoPath2;
	}

	public void setCompanyLogoPath2(final String companyLogoPath2) {
		this.companyLogoPath2 = companyLogoPath2;
	}

	public static String getSsoPwcApiHost() {
		return ssoPwcApiHost;
	}

	public static String getSsoPwcClientId() {
		return ssoPwcClientId;
	}

	public static String getSsoPwcClientSecret() {
		return ssoPwcClientSecret;
	}

	public static String getSsoPwcRedirectUrl() {
		return ssoPwcRedirectUrl;
	}

	public static String getSsoPwcScope() {
		return ssoPwcScope;
	}

	public String getDeploymentType() {
		return deploymentType;
	}

	public void setDeploymentType(String deploymentType) {
		this.deploymentType = deploymentType;
	}

	public String getHttpHost() {
		return httpHost;
	}

	public void setHttpHost(String httpHost) {
		this.httpHost = httpHost;
	}

	public String getHttpsHost() {
		return httpsHost;
	}

	public void setHttpsHost(String httpsHost) {
		this.httpsHost = httpsHost;
	}

	public String getNumberOfDocUploadsAllowed() {
		return numberOfDocUploadsAllowed;
	}

	public void setNumberOfDocUploadsAllowed(String numberOfDocUploadsAllowed) {
		this.numberOfDocUploadsAllowed = numberOfDocUploadsAllowed;
	}

	public String getIsProcurementEnabled() {
		return isProcurementEnabled;
	}

	public void setIsProcurementEnabled(final String isProcurementEnabled) {
		this.isProcurementEnabled = isProcurementEnabled;
	}
}
