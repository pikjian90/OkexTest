package TestCases.PublicData;

import Common.EndPoints;
import Common.Uri;
import PublicData.Instruments;
import TestCases.BaseTest;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;
import util.XLUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.lessThan;

public class InstrumentTest extends BaseTest implements Runnable {
    String endPoint = EndPoints.INSTRUMENTS;
    static int tooManyRequestCount = 0;
    String status ;

    @Override
    public void run() {
        String endPoint = EndPoints.INSTRUMENTS;

        try{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            RestAssured.baseURI = Uri.PRODUCTION;
            Response response = RestAssured
                    .given()
                    .params("instType", "SPOT")
                    .params("instId", "BTC-USDT")
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

    @DataProvider(name="InstrumentTestData")
    public Object[][] getData() throws IOException {
        //read data from excel
        String path = System.getProperty("user.dir") + "/src/test/java/resources/InstrumentTestData.xlsx";
        int rowNum = XLUtils.getRowCount(path,"Sheet1");
        int colNum = XLUtils.getCellCount(path,"sheet1",1);
        String[][] testData = new String [rowNum][colNum];

        for(int i=1;i<=rowNum;i++){
            for(int j=0;j<colNum;j++){
                testData[i-1][j] = XLUtils.getCellData(path,"Sheet1",i,j);
            }
        }
        return testData;
    }



    @Test(dataProvider = "InstrumentTestData")
    public void verifyPositiveInstIdAndInstType(String instType, String instId) {
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_" + "verifyPositiveInstIdAndInstType","xxx");

        try{
            RestAssured.baseURI = Uri.PRODUCTION;
            Response response = RestAssured
                    .given()
                        .params("instType", instType)
                        .params("instId", instId)
                    .when()
                        .get(endPoint);
            JsonPath jsonPath = response.jsonPath();
            Instruments[] instruments = jsonPath.getObject("data", Instruments[].class);

            SoftAssert softAssert = new SoftAssert();
            for(Instruments i : instruments){
                String expectedQuoteCcy = i.baseCcy + "-" + i.quoteCcy;
                System.out.println(i.baseCcy + " " + i.quoteCcy + " " +  i.instId + " " + i.instType);
                softAssert.assertEquals(i.instId, expectedQuoteCcy, "Unexpected quoteCcy");
                softAssert.assertEquals(i.instType, instType, "Unexpected instType");
            }
            softAssert.assertAll();
        }catch (Exception e){
            e.printStackTrace();
            extentTest.log(Status.FAIL,e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;



    }

    @Test
    public void verifyNegativeInvalidInstId() {
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_" + "verifyNegativeInvalidInstId","xxx");
        try{
            RestAssured.baseURI = Uri.PRODUCTION;
            Response request = RestAssured
                    .given()
                        .params("instType", "XXX")
                    .when()
                        .get(endPoint);

            JsonPath jsonPath = request.jsonPath();
            String code = jsonPath.getString("code");
            String msg = jsonPath.getString("msg");

            SoftAssert softAssert = new SoftAssert();
            softAssert.assertEquals(code, "51000");
            softAssert.assertEquals(msg, "Parameter instType error");
            softAssert.assertAll();
        }catch (AssertionError e){
            e.printStackTrace();
            extentTest.log(Status.FAIL,e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;


    }

    @Test
    public void verifyNegativeMissingInstId() {
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_" + "verifyNegativeMissingInstId","xxx");
        try{
            RestAssured.baseURI = Uri.PRODUCTION;
            Response request = RestAssured
                    .given()
                    .when()
                        .get(endPoint);

            JsonPath jsonPath = request.jsonPath();
            String code = jsonPath.getString("code");
            String msg = jsonPath.getString("msg");

            SoftAssert softAssert = new SoftAssert();
            softAssert.assertEquals(code, "50014");
            softAssert.assertEquals(msg, "instType cannot be empty");
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
                this.getClass().getName() + "_" + "verifyNegativeInvalidEndpoint","xxx");
        try{
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
    public void verifyNegativeInvalidMethod(){
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_" + "verifyNegativeInvalidMethod","xxx");
        try{
            RestAssured.baseURI = Uri.PRODUCTION;
            Response response = RestAssured
                    .given()
                        .params("instId", "BTC-USDT")
                        .params("limit", "1")
                    .when()
                        .post(endPoint);

            JsonPath jsonPath = response.jsonPath();
            String code = jsonPath.getString("code");
            String msg = jsonPath.getString("msg");

            SoftAssert softAssert = new SoftAssert();
            softAssert.assertEquals(code, "50115");
            softAssert.assertEquals(msg, "Invalid request type");
            softAssert.assertAll();
        }catch (AssertionError e){
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;

    }

    @Test
    public void verifyResponseTime(){
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_" + "verifyResponseTime","xxx");
        try{
            RestAssured.baseURI = Uri.PRODUCTION;
            RestAssured
                    .given()
                    .params("instId", "BTC-USDT")
                    .params("limit", "1")
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
                this.getClass().getName() + "_" + "verifyResponseTime","xxx");
        InstrumentTest myRunnable = new InstrumentTest();
        for (int i = 0; i < 24; i++) {
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
        jedis.set(key, String.valueOf(status));
        System.out.println(key + " : " + jedis.get(key));
    }
}
