package TestCases;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;

public class BaseTest {
    public static ExtentHtmlReporter htmlReporter;
    public static ExtentReports extentReports;
    public static Jedis jedis;

    @BeforeSuite
    public void beforeSuite() {
        htmlReporter = new ExtentHtmlReporter(System.getProperty("user.dir") + "/test-output/testReport.html");
        htmlReporter.config().setDocumentTitle("API Testing Report");
        htmlReporter.config().setReportName("OKX REST API Test");
        htmlReporter.config().setTheme(Theme.DARK);

        extentReports = new ExtentReports();
        extentReports.attachReporter(htmlReporter);
        extentReports.setSystemInfo("Host Name", "Local Host");
        extentReports.setSystemInfo("Environment", "QA");
        extentReports.setSystemInfo("Tester", "QA");

        jedis = new Jedis("localhost",6379);

    }

    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost",6379);
        System.out.println(jedis.ping());
        System.out.println(jedis.get("Key1"));
        System.out.println();
    }

    @AfterSuite
    public void afterSuit(){
        Set<String> keys = jedis.keys("*");
        System.out.println("=== Print all cases and executed time ===");
        for(String k : keys){
            System.out.println(k + " : " + jedis.get(k));
        }


        extentReports.flush();

    }
}