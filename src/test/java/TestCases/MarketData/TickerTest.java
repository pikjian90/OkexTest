package TestCases.MarketData;

import Common.EndPoints;
import Common.Uri;
import MarketData.Ticker;
import TestCases.BaseTest;
import TestCases.PublicData.InstrumentTest;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import util.XLUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.lessThan;

public class TickerTest extends BaseTest implements Runnable{
    String endPoint = EndPoints.TICKER;
    static int tooManyRequestCount = 0;
    String status ;

    @Override
    public void run() {
        try{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            RestAssured.baseURI = Uri.PRODUCTION;
            Response response = RestAssured
                    .given()
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

    @DataProvider(name="TickerTestData")
    public Object[][] getData() throws IOException {
        //read data from excel
        String path = System.getProperty("user.dir") + "/src/test/java/resources/TickerTestData.xlsx";
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



    @Test(dataProvider = "TickerTestData")
    public void verifyPositive(String instId){
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyPositive", "xx");

        try {
            RestAssured.baseURI = Uri.PRODUCTION;
            Response request = RestAssured
                    .given()
                        .params("instId", instId)
                    .when()
                        .get(endPoint);

            JsonPath jsonPath = request.jsonPath();

            Ticker[] tickers = jsonPath.getObject("data", Ticker[].class);

            SoftAssert softAssert = new SoftAssert();
            for (Ticker ticker : tickers) {
                softAssert.assertEquals(ticker.instId, instId);
                softAssert.assertEquals(ticker.instType, "SPOT");
                softAssert.assertTrue(Double.parseDouble(ticker.last) > 1);
                softAssert.assertTrue(Double.parseDouble(ticker.lastSz) > 0.000001);
                softAssert.assertTrue(Double.parseDouble(ticker.askPx) > 1);
                softAssert.assertTrue(Double.parseDouble(ticker.askSz) > 0.000001);
                softAssert.assertTrue(Double.parseDouble(ticker.bidPx) > 1);
                softAssert.assertTrue(Double.parseDouble(ticker.bidSz) > 0.000001);
                softAssert.assertTrue(Double.parseDouble(ticker.open24h) > 1);
                softAssert.assertTrue(Double.parseDouble(ticker.high24h) > 1);
                softAssert.assertTrue(Double.parseDouble(ticker.low24h) > 1);
                softAssert.assertTrue(Double.parseDouble(ticker.high24h) > Double.parseDouble(ticker.low24h));
                softAssert.assertTrue(Double.parseDouble(ticker.volCcy24h) > 1);
                softAssert.assertTrue(Double.parseDouble(ticker.vol24h) > 1);
                softAssert.assertTrue(Long.parseLong(ticker.ts) < System.currentTimeMillis());
                softAssert.assertTrue(Double.parseDouble(ticker.sodUtc0) > 1);
                softAssert.assertTrue(Double.parseDouble(ticker.sodUtc8) > 1);
            }
            softAssert.assertAll();
        }catch (AssertionError e){
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;

    }

    @Test
    public void verifyNegativeInvalidInstId() {
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyPositive", "xx");
        try {
            RestAssured.baseURI = Uri.PRODUCTION;
            Response request = RestAssured
                    .given()
                        .params("instId", "BTC-USDTX")
                    .when()
                        .get(endPoint);

            JsonPath jsonPath = request.jsonPath();
            String code = jsonPath.getString("code");
            String msg = jsonPath.getString("msg");
            Ticker[] tickers = jsonPath.getObject("data", Ticker[].class);

            SoftAssert softAssert = new SoftAssert();
            softAssert.assertEquals(code, "51001");
            softAssert.assertEquals(msg, "Instrument ID does not exist");
            softAssert.assertTrue(tickers.length == 0);
            softAssert.assertAll();
        }catch (AssertionError e){
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;

    }

    @Test
    public void verifyNegativeMissingInstId() {
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyPositive", "xx");
        try {
            RestAssured.baseURI = Uri.PRODUCTION;

            Response request = RestAssured
                    .when()
                        .get(endPoint);

            JsonPath jsonPath = request.jsonPath();
            String code = jsonPath.getString("code");
            String msg = jsonPath.getString("msg");
            Ticker[] tickers = jsonPath.getObject("data", Ticker[].class);

            SoftAssert softAssert = new SoftAssert();
            softAssert.assertEquals(code, "50014");
            softAssert.assertEquals(msg, "Parameter instId can not be empty");
            softAssert.assertTrue(tickers.length == 0);
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
                        .get(endPoint);

            JsonPath jsonPath = request.jsonPath();
            String code = jsonPath.getString("code");
            String msg = jsonPath.getString("msg");

            SoftAssert softAssert = new SoftAssert();
            softAssert.assertEquals(code, "50014");
            softAssert.assertEquals(msg, "Parameter instId can not be empty");
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
                    .given()
                        .params("instId", "BTC-USDT")
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
                    .given()
                    .params("instId", "BTC-USDT")
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
        TickerTest myRunnable = new TickerTest();
        for (int i = 0; i < 21; i++) {
            Thread thread = new Thread(myRunnable);
            thread.start();
        }

        Thread.sleep(1000);
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
