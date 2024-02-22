package utilities;

public class Configs {
    // FOR MORE INFO CHECK THE DOCUMENT - https://developer.rimit.in/getting-started/readme
    public static String BASE_URL = "https://uat-gateway.rimit.in/api/client/rimit/v1"; // FOR UAT API
    // public static String BASE_URL: 'https://api-gateway.rimit.in/api/client/rimit/v1', // FOR PRODUCTION API

    // FOR MORE INFO CHECK THE DOCUMENT - https://developer.rimit.in/getting-started/readme#multi-tenant
    public static String IS_MULTY_TENANT_PLATFORM = "NO"; // OPTIONS - YES/NO
    public static String MULTY_TENANT_MODE = "QUERY"; // OPTIONS - QUERY/PARAMS
}
