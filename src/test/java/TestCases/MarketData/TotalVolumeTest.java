package TestCases.MarketData;

import Common.EndPoints;
import Common.Uri;
import MarketData.TotalVolume;
import TestCases.BaseTest;
import TestCases.PublicData.InstrumentTest;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.lessThan;

public class TotalVolumeTest extends BaseTest implements Runnable{
    String endPoint = EndPoints.TOTALVOLUME;
    static int tooManyRequestCount = 0;
    String status ;

    @Override
    public void run() {

        try{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            RestAssured.baseURI = Uri.PRODUCTION;
            Response response = RestAssured
                    .when()
                        .get(endPoint);
            int statusCode = response.getStatusCode();
            String statusLine = response.getStatusLine();

            if(statusCode == 429){
                tooManyRequestCount++;
            }

            System.out.println(Thread.currentThread().getName()+ " " + timestamp + " " + statusCode + " " + statusLine);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void verifyPositive(){
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyPositive", "xx");
        try {
            RestAssured.baseURI = Uri.PRODUCTION;

            Response request = RestAssured
                    .when()
                        .get(endPoint);

            JsonPath jsonPath = request.jsonPath();
            TotalVolume asks = jsonPath.getObject("data[0]", TotalVolume.class);

            SoftAssert softAssert = new SoftAssert();
            softAssert.assertTrue(Double.parseDouble(asks.volUsd) > 1);
            softAssert.assertTrue(Double.parseDouble(asks.volCny) > 1);
            softAssert.assertAll();
        }catch (AssertionError e){
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;

    }

    @Test
    public void verifyNegativeInvalidEndpoint() {
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyPositive", "xx");
        try {
            RestAssured.baseURI = Uri.PRODUCTION;
            Response request = RestAssured
                    .when()
                        .get(endPoint + "x");

            JsonPath jsonPath = request.jsonPath();
            String code = jsonPath.getString("code");
            String msg = jsonPath.getString("msg");

            SoftAssert softAssert = new SoftAssert();
            softAssert.assertEquals(code, "404");
            softAssert.assertEquals(msg, "Not Found");
            softAssert.assertAll();
        }catch (AssertionError e){
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;

    }

    @Test
    public void verifyNegativeInvalidMethod() {
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyPositive", "xx");
        try {
            RestAssured.baseURI = Uri.PRODUCTION;

            Response request = RestAssured
                    .when()
                        .post(endPoint);

            JsonPath jsonPath = request.jsonPath();
            String code = jsonPath.getString("code");
            String msg = jsonPath.getString("msg");

            SoftAssert softAssert = new SoftAssert();
            softAssert.assertEquals(code, "50115");
            softAssert.assertEquals(msg, "Invalid request method.");
            softAssert.assertAll();
        }catch (AssertionError e){
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;

    }

    @Test
    public void verifyResponseTime() {
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "verifyResponseTime", "xx");
        try {
            RestAssured.baseURI = Uri.PRODUCTION;

            RestAssured
                    .when()
                        .get(endPoint)
                    .then()
                    .time(lessThan(5000L));

        }catch (Exception e){
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;

    }

    @Test
    public void verifyRateLimit() throws InterruptedException{
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "verifyResponseTime", "xx");
        TotalVolumeTest myRunnable = new TotalVolumeTest();
        for (int i = 0; i < 4; i++) {
            Thread thread = new Thread(myRunnable);
            thread.start();
        }

        Thread.sleep(2000);
        System.out.println("tooManyRequestCount: " + tooManyRequestCount);
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertTrue(tooManyRequestCount > 0, "Rate Limit is not triggered");
        softAssert.assertAll();
        status = '"' +extentTest.getStatus().toString() + '"' ;

    }

    @AfterMethod
    public void saveTestCase(Method method){

        String caseName = this.getClass().getName();
        String key = caseName + "_" + method.getName();

        jedis.set(key, status);
        System.out.println(key + " : " + jedis.get(key));
    }
}
