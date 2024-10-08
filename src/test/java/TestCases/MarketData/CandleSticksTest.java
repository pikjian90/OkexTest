package TestCases.MarketData;

import Common.EndPoints;
import Common.Uri;
import MarketData.CandleSticks;
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

public class CandleSticksTest extends BaseTest implements Runnable{
    String endPoint = EndPoints.CANDLESTICKS;
    static int tooManyRequestCount = 0;
    String status ;

    @Override
    public void run() {

        try{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            RestAssured.baseURI = Uri.PRODUCTION;
            Response response = RestAssured
                    .given()
                    .params("instId", "BTC-SPOT")
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

    @DataProvider(name="CandleSticksTestData")
    public Object[][] getData() throws IOException {
        //read data from excel
        String path = System.getProperty("user.dir") + "/src/test/java/resources/CandleSticksTestData.xlsx";
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

    @Test(dataProvider = "CandleSticksTestData" )
    public void verifyPositiveSingleFieldValidation(String instId, String limit ){
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyPositiveSingleFieldValidation", "xx");
        try{
            RestAssured.baseURI = Uri.PRODUCTION;
            Response response = RestAssured
                    .given()
                    .params("instId", instId)
                    .params("limit", limit)
                    .when()
                    .get(endPoint);

            JsonPath jsonPath = response.jsonPath();
            List<String> candleSticksList = jsonPath.getList("data[0]");

            CandleSticks cs = new CandleSticks();
            cs.ts = candleSticksList.get(0);
            cs.o = candleSticksList.get(1);
            cs.h = candleSticksList.get(2);
            cs.l = candleSticksList.get(3);
            cs.c = candleSticksList.get(4);
            cs.vol = candleSticksList.get(5);
            cs.volCcy = candleSticksList.get(6);
            cs.volCcyQuote = candleSticksList.get(7);
            cs.confirm = candleSticksList.get(8);

            SoftAssert softAssert = new SoftAssert();
            softAssert.assertTrue(Double.parseDouble(cs.ts) > 1 );
            softAssert.assertTrue(Double.parseDouble(cs.o) > 1);
            softAssert.assertTrue(Double.parseDouble(cs.h) > 1);
            softAssert.assertTrue(Double.parseDouble(cs.l) > 1);
            softAssert.assertTrue(Double.parseDouble(cs.c) > 1);
            softAssert.assertTrue(Double.parseDouble(cs.vol) >= 0);
            softAssert.assertTrue(Double.parseDouble(cs.volCcy) >= 0);
            softAssert.assertTrue(Double.parseDouble(cs.volCcyQuote) >= 0);
            softAssert.assertTrue(Double.parseDouble(cs.confirm) == 0 || Double.parseDouble(cs.confirm) == 1);
            softAssert.assertAll();
        }catch (AssertionError e){
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;
    }

    @Test
    public void verifyPositiveMultipleFieldValidation(){
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyPositiveMultipleFieldValidation", "xx");
        try{
            RestAssured.baseURI = Uri.PRODUCTION;

            Response response = RestAssured
                    .given()
                        .params("instId", "BTC-USDT")
                        .params("limit", "3")
                    .when()
                        .get(endPoint);

            JsonPath jsonPath = response.jsonPath();
            List<List<String>> candleSticksList = jsonPath.getList("data");
            for (List<String> candleStick : candleSticksList){
                CandleSticks cs = new CandleSticks();
                cs.ts = candleStick.get(0);
                cs.o = candleStick.get(1);
                cs.h = candleStick.get(2);
                cs.l = candleStick.get(3);
                cs.c = candleStick.get(4);
                cs.vol = candleStick.get(5);
                cs.volCcy = candleStick.get(6);
                cs.volCcyQuote = candleStick.get(7);
                cs.confirm = candleStick.get(8);

                SoftAssert softAssert = new SoftAssert();
                softAssert.assertTrue(Double.parseDouble(cs.ts) > 1);
                softAssert.assertTrue(Double.parseDouble(cs.o) > 1);
                softAssert.assertTrue(Double.parseDouble(cs.h) > 1);
                softAssert.assertTrue(Double.parseDouble(cs.l) > 1);
                softAssert.assertTrue(Double.parseDouble(cs.c) > 1);
                softAssert.assertTrue(Double.parseDouble(cs.vol) > 0.000000001);
                softAssert.assertTrue(Double.parseDouble(cs.volCcy) > 1);
                softAssert.assertTrue(Double.parseDouble(cs.volCcyQuote) > 1);
                softAssert.assertTrue(Double.parseDouble(cs.confirm) == 0 || Double.parseDouble(cs.confirm) == 1);
                softAssert.assertAll();
            }
        }catch (AssertionError e){
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;



    }

    @Test
    public void verifyPositiveBar1mValidation(){
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyPositiveBar1mValidation", "xx");
        try{
            RestAssured.baseURI = Uri.PRODUCTION;
            Response response = RestAssured
                    .given()
                        .params("instId", "BTC-USDT")
                        .params("bar", "1m")
                        .params("limit", "10")
                    .when()
                        .get(endPoint);

            JsonPath jsonPath = response.jsonPath();
            List<List<String>> candleSticksList = jsonPath.getList("data");
            SoftAssert softAssert = new SoftAssert();

            Long timeDiff = 60000L;
            Long prevTime = 0L;
            for (int i = 0; i < candleSticksList.size(); i++) {
                Long respTime = Long.parseLong(candleSticksList.get(i).get(0));

                if(i != 0) {
                    softAssert.assertTrue(prevTime - respTime == timeDiff);
                }
                prevTime = respTime;

            }

            softAssert.assertAll();
        }catch (AssertionError e){
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;



    }

    @Test
    public void verifyPositiveBar5mValidation(){
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyPositiveBar5mValidation", "xx");
        try{
            RestAssured.baseURI = Uri.PRODUCTION;
            Response response = RestAssured
                    .given()
                        .params("instId", "BTC-USDT")
                        .params("bar", "5m")
                        .params("limit", "10")
                    .when()
                        .get(endPoint);

            JsonPath jsonPath = response.jsonPath();
            List<List<String>> candleSticksList = jsonPath.getList("data");
            SoftAssert softAssert = new SoftAssert();

            Long timeDiff = 300000L;
            Long prevTime = 0L;
            for (int i = 0; i < candleSticksList.size(); i++) {
                Long respTime = Long.parseLong(candleSticksList.get(i).get(0));

                if(i != 0) {
                    softAssert.assertTrue(prevTime - respTime == timeDiff);
                }
                prevTime = respTime;

            }
            softAssert.assertAll();
        }catch (AssertionError e){
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;



    }

    @Test
    public void verifyPositiveBar1hValidation(){
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyPositiveBar1hValidation", "xx");
        try {
            RestAssured.baseURI = Uri.PRODUCTION;
            Response response = RestAssured
                    .given()
                        .params("instId", "BTC-USDT")
                        .params("bar", "1H")
                        .params("limit", "10")
                    .when()
                        .get(endPoint);


            JsonPath jsonPath = response.jsonPath();
            List<List<String>> candleSticksList = jsonPath.getList("data");
            SoftAssert softAssert = new SoftAssert();

            Long timeDiff = 3600000L;
            Long prevTime = 0L;
            for (int i = 0; i < candleSticksList.size(); i++) {
                Long respTime = Long.parseLong(candleSticksList.get(i).get(0));

                if (i != 0) {
                    softAssert.assertTrue(prevTime - respTime == timeDiff);
                }
                prevTime = respTime;

            }

            softAssert.assertAll();
        }catch (AssertionError e){
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;


    }

    @Test
    public void verifyPositiveAfterValidation(){
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyPositiveAfterValidation", "xx");
        try {
            Long afterTime = System.currentTimeMillis() - 1000;

            RestAssured.baseURI = Uri.PRODUCTION;
            Response response = RestAssured
                    .given()
                        .params("instId", "BTC-USDT")
                        .params("after", afterTime.toString())
                        .params("limit", "3")
                    .when()
                        .get(endPoint);

            JsonPath jsonPath = response.jsonPath();
            List<List<String>> candleSticksList = jsonPath.getList("data");
            for (List<String> candleStick : candleSticksList) {
                CandleSticks cs = new CandleSticks();
                cs.ts = candleStick.get(0);
                System.out.println(cs.ts);
                SoftAssert softAssert = new SoftAssert();
                softAssert.assertTrue(Long.parseLong(cs.ts) < afterTime);
                softAssert.assertAll();
            }
        }catch (AssertionError e){
            e.printStackTrace();
            extentTest.log(Status.FAIL, e.getMessage());
        }
        status = '"' +extentTest.getStatus().toString() + '"' ;

    }

    @Test
    public void verifyPositiveBeforeValidation(){
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyPositiveBeforeValidation", "xx");
        try {
            Long before = System.currentTimeMillis() - 900000;

            RestAssured.baseURI = Uri.PRODUCTION;
            Response response = RestAssured
                    .given()
                        .params("instId", "BTC-USDT")
                        .params("before", before.toString())
                        .params("limit", "3")
                    .when()
                        .get(endPoint);

            JsonPath jsonPath = response.jsonPath();
            List<List<String>> candleSticksList = jsonPath.getList("data");
            for (List<String> candleStick : candleSticksList) {
                CandleSticks cs = new CandleSticks();
                cs.ts = candleStick.get(0);
                System.out.println(cs.ts);
                SoftAssert softAssert = new SoftAssert();
                softAssert.assertTrue(Long.parseLong(cs.ts) > before);
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
                this.getClass().getName() + "_verifyPositive1LimitSize", "xx");
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
    public void verifyPositiveDefaultLimitSize(){
        ExtentTest extentTest = extentReports.createTest(
                this.getClass().getName() + "_verifyPositiveDefaultLimitSize", "xx");
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
                this.getClass().getName() + "_verifyNegativeInvalidInstId", "xx");
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
                this.getClass().getName() + "_verifyNegativeMissingInstId", "xx");
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
                this.getClass().getName() + "_verifyNegativeInvalidEndpoint", "xx");
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
            }
            catch (AssertionError e){
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
            Response response = RestAssured
                    .given()
                        .params("instId", "BTC-USDT")
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
        CandleSticksTest myRunnable = new CandleSticksTest();
        for (int i = 0; i < 42; i++) {
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
