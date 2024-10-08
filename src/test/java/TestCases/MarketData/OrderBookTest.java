package TestCases.MarketData;

import Common.EndPoints;
import Common.Uri;
import TestCases.BaseTest;
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
import java.util.List;

import static org.hamcrest.Matchers.lessThan;

public class OrderBookTest extends BaseTest implements Runnable {
    //https://www.okx.com/docs-v5/en/?shell#order-book-trading-market-data-get-order-book

    String endPoint = EndPoints.ORDERBOOK;
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

    @DataProvider(name="OrderBookTestData")
    public Object[][] getData() throws IOException {
        //read data from excel
        String path = System.getProperty("user.dir") + "/src/test/java/resources/OrderBookTestData.xlsx";
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



    @Test(dataProvider = "OrderBookTestData")
    public void verifyPositive(String instId) {
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyPositive", "xx");
        try {
            RestAssured.baseURI = Uri.PRODUCTION;
            Response request = RestAssured
                    .given()
                        .params("instId", instId)
                    .when().get(endPoint);

            JsonPath jsonPath = request.jsonPath();
            List<String> asks = jsonPath.getList("data.asks[0][0]");
            List<String> bids = jsonPath.getList("data.bids[0][0]");

            Double depthAsksPrice = Double.parseDouble(asks.get(0));
            Double qtyDepthAsksPrice = Double.parseDouble(asks.get(1));
            Double alwaysZeroAsks = Double.parseDouble(asks.get(2));
            Double numberOfAsksOrder = Double.parseDouble(asks.get(3));
            Double depthBidsPrice = Double.parseDouble(bids.get(0));
            Double qtyDepthBidsPrice = Double.parseDouble(bids.get(1));
            Double alwaysZeroBids = Double.parseDouble(bids.get(2));
            Double numberOfBidsOrder = Double.parseDouble(bids.get(3));

            SoftAssert softAssert = new SoftAssert();
            softAssert.assertTrue(depthAsksPrice > 1, "1");
            softAssert.assertTrue(depthBidsPrice > 1, "2");
            softAssert.assertTrue(qtyDepthAsksPrice > 0.00000001, "3");
            softAssert.assertTrue(qtyDepthBidsPrice > 0.00000001, "4");
            softAssert.assertTrue(alwaysZeroAsks == 0, "5");
            softAssert.assertTrue(alwaysZeroBids == 0, "6");
            softAssert.assertTrue(numberOfAsksOrder >= 1, "7");
            softAssert.assertTrue(numberOfBidsOrder >= 1, "8");
            softAssert.assertAll();
        } catch (AssertionError e) {
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;
        System.out.println(status);
    }

    @Test
    public void verifyNegativeInvalidInstId() {
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyNegativeInvalidInstId", "xx");
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
            softAssert.assertEquals(msg, "Instrument ID does not exist.");
            softAssert.assertAll();
        } catch (AssertionError e) {
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;

    }

    @Test
    public void verifyNegativeMissingInstId() {
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyNegativeMissingInstId", "xx");
        try {
            RestAssured.baseURI = Uri.PRODUCTION;
            Response request = RestAssured
                    .when()
                        .get(endPoint);

            JsonPath jsonPath = request.jsonPath();
            String status = jsonPath.getString("status");
            String error = jsonPath.getString("error");

            SoftAssert softAssert = new SoftAssert();
            softAssert.assertEquals(status, "400");
            softAssert.assertEquals(error, "Bad Request");
            softAssert.assertAll();
        } catch (AssertionError e) {
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;

    }

    @Test
    public void verifyNegativeInvalidEndpoint() {
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyNegativeInvalidEndpoint", "xx");
        try {
            RestAssured.baseURI = Uri.PRODUCTION;
            Response request = RestAssured
                    .when()
                        .get(endPoint + "x");

            JsonPath jsonPath = request.jsonPath();
            String status = jsonPath.getString("status");
            String error = jsonPath.getString("error");

            SoftAssert softAssert = new SoftAssert();
            softAssert.assertEquals(status, "404");
            softAssert.assertEquals(error, "Not Found");
            softAssert.assertAll();
        } catch (AssertionError e) {
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;

    }

    @Test
    public void verifyNegativeInvalidMethod() {
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyNegativeInvalidMethod", "xx");
        try {
            RestAssured.baseURI = Uri.PRODUCTION;
            Response request = RestAssured
                    .given()
                        .params("instId", "BTC-USDT")
                    .when()
                        .post(endPoint);

            JsonPath jsonPath = request.jsonPath();
            String status = jsonPath.getString("status");
            String error = jsonPath.getString("error");

            SoftAssert softAssert = new SoftAssert();
            softAssert.assertEquals(status, "405");
            softAssert.assertEquals(error, "Method Not Allowed");
            softAssert.assertAll();
        } catch (AssertionError e) {
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

        } catch (Exception e) {
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;

    }

    @Test
    public void verifyRateLimit() throws InterruptedException{
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "verifyRateLimit", "xx");
        try{

            OrderBookTest myRunnable = new OrderBookTest();
            for (int i = 0; i < 51; i++) {
                Thread thread = new Thread(myRunnable);
                thread.start();
            }

            Thread.sleep(3000);
            System.out.println("tooManyRequestCount: " + tooManyRequestCount);
            SoftAssert softAssert = new SoftAssert();
            softAssert.assertTrue(tooManyRequestCount > 0, "Rate Limit is not triggered");
            softAssert.assertAll();
        }catch (AssertionError e){
            e.printStackTrace();
            extentTest.fail(e);
            extentTest.log(Status.FAIL, e.getMessage());
        }

        status = '"' +extentTest.getStatus().toString() + '"' ;
        System.out.println(status);
    }

    @AfterMethod
    public void saveTestCase(Method method){

        String caseName = this.getClass().getName();
        String key = caseName + "_" + method.getName();

        jedis.set(key, status);
        System.out.println(key + " : " + jedis.get(key));
    }
}
