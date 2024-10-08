package TestCases.MarketData;

import Common.EndPoints;
import Common.Uri;
import MarketData.Trades;
import TestCases.BaseTest;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.configuration.Theme;
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
import java.util.List;

import static org.hamcrest.Matchers.lessThan;

public class TradesTest extends BaseTest implements Runnable{
    String endPoint = EndPoints.TRADES;
    volatile static int tooManyRequestCount = 0;
    String status ;

    @Override
    public void run() {

        try{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            RestAssured.baseURI = Uri.PRODUCTION;
            Response response = RestAssured
                    .given()
                    .params("instId", "BTC-USDT")
                    .params("limit", "1")
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

    @DataProvider(name="TradeTestData")
    public Object[][] getData() throws IOException {
        //read data from excel
        String path = System.getProperty("user.dir") + "/src/test/java/resources/TradeTestData.xlsx";
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



    @Test(dataProvider = "TradeTestData")
    public void verifyPositiveSingleFieldValidation(String instId, String limit){
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyPositive", "xx");
        try {
            RestAssured.baseURI = Uri.PRODUCTION;
            Response response = RestAssured
                    .given()
                        .params("instId", instId)
                        .params("limit", limit)
                    .when()
                        .get(endPoint);

            JsonPath jsonPath = response.jsonPath();
            Trades[] tradesList = jsonPath.getObject("data", Trades[].class);

            for (Trades trade : tradesList) {
                SoftAssert softAssert = new SoftAssert();
                softAssert.assertEquals(trade.instId, instId);
                softAssert.assertTrue(Double.parseDouble(trade.tradeId) > 1);
                softAssert.assertTrue(Double.parseDouble(trade.px) > 1);
                softAssert.assertTrue(Double.parseDouble(trade.sz) > 0.000000001);
                softAssert.assertTrue(trade.side.equals("buy") || trade.side.equals("sell"));
                softAssert.assertTrue(Long.parseLong(trade.ts) > 1);
                softAssert.assertAll();
            }

        }catch (Exception e){
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;

    }

    @Test
    public void verifyPositiveMultipleFieldValidation(){
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyPositive", "xx");
        try {
            RestAssured.baseURI = Uri.PRODUCTION;
            Response response = RestAssured
                    .given()
                        .params("instId", "BTC-USDT")
                        .params("limit", "5")
                    .when()
                        .get(endPoint);

            JsonPath jsonPath = response.jsonPath();
            Trades[] tradesList = jsonPath.getObject("data", Trades[].class);

            for (Trades trade : tradesList) {
                SoftAssert softAssert = new SoftAssert();
                softAssert.assertEquals(trade.instId, "BTC-USDT");
                softAssert.assertTrue(Double.parseDouble(trade.tradeId) > 1);
                softAssert.assertTrue(Double.parseDouble(trade.px) > 1);
                softAssert.assertTrue(Double.parseDouble(trade.sz) > 0.000000001);
                softAssert.assertTrue(trade.side.equals("buy") || trade.side.equals("sell"));
                softAssert.assertTrue(Long.parseLong(trade.ts) > 1);
                softAssert.assertAll();
            }
        }catch (AssertionError e){
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;

    }

    @Test
    public void verifyPositive1LimitSize(){
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyPositive", "xx");
        try {
            RestAssured.baseURI = Uri.PRODUCTION;
            Response response = RestAssured
                    .given()
                        .params("instId", "BTC-USDT")
                        .params("limit", "1")
                    .when()
                        .get(endPoint);

            JsonPath jsonPath = response.jsonPath();
            int defaultLimitSize = jsonPath.getList("data").size();

            SoftAssert softAssert = new SoftAssert();
            softAssert.assertTrue(defaultLimitSize == 1);
            softAssert.assertAll();
        }catch (AssertionError e){
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;

    }

    @Test
    public void verifyPositiveDefaultLimitSize() {
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyPositive", "xx");
        try {
            RestAssured.baseURI = Uri.PRODUCTION;
            Response response = RestAssured
                    .given()
                        .params("instId", "BTC-USDT")
                    .when()
                        .get(endPoint);

            JsonPath jsonPath = response.jsonPath();
            int defaultLimitSize = jsonPath.getList("data").size();

            SoftAssert softAssert = new SoftAssert();
            softAssert.assertTrue(defaultLimitSize == 100);
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
                        .when().get(endPoint);

            JsonPath jsonPath = request.jsonPath();
            String code = jsonPath.getString("code");
            String msg = jsonPath.getString("msg");

            SoftAssert softAssert = new SoftAssert();
            softAssert.assertEquals(code, "51001");
            softAssert.assertEquals(msg, "Instrument ID does not exist");
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
                    .params("limit", "1")
                    .when()
                    .post(endPoint)
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
        TradesTest tradesTest = new TradesTest();
        for (int i = 0; i < 103; i++) {
            Thread thread = new Thread(tradesTest);
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
