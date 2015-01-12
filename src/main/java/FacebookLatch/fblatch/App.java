package FacebookLatch.fblatch;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.elevenpaths.latch.Latch;
import com.elevenpaths.latch.LatchResponse;
import com.google.gson.JsonElement;

public class App{

    private static String USER = "YOUR_FACEBOOK_ACCOUNT";
    private static String PASSWORD = "YOUR_FACEBOOK_PASSWORD";
    private static String APP_ID = "YOUR_APP_ID";
    private static String APP_SECRET = "YOUR_APP_SECRET";
    private static String ACCOUNT_ID = "YOUR_ACCOUNT_ID";

    private static List<String> ids = new ArrayList<String>();

    public static void main(String[] args) {

        //Perform facebook login for the first time.
        WebDriver driver = new FirefoxDriver();
        driver.get("https://m.facebook.com/settings/security/?active_sessions");

        WebElement user = driver.findElement(By.name("email"));
        user.sendKeys(USER);

        WebElement pass = driver.findElement(By.name("pass"));
        pass.sendKeys(PASSWORD);

        pass.submit();

        //Wait a reasonable time until login is complete.
        //TODO Add proper condition of loading complete.
        try {
            Thread.currentThread().sleep(5000);
        } catch (InterruptedException e1) {}


        while(true){
            driver.get("https://m.facebook.com/settings/security/?active_sessions");

            try{
                List<String> newIds = new ArrayList<String>();

                //Get Active sessions
                List<WebElement> checks = driver.findElements(By.name("active_session_removal[]"));
                boolean shouldFinishSessions = false;

                //Iterate over all sessions to detect new ones.
                for(WebElement e : checks){

                    //New session detected
                    if (!ids.contains(e.getAttribute("value"))){ 
                        Latch api = new Latch(APP_ID, APP_SECRET);
                        LatchResponse response = api.status(ACCOUNT_ID);
                        if (response.getData() != null && response.getData().has("operations")){
                            JsonElement el = response.getData().get("operations").getAsJsonObject()
                                    .get(APP_ID).getAsJsonObject()
                                    .get("status");

                            //Latch = off means we should close this session
                            if(el.getAsString().equals("off")){
                                e.click();
                                shouldFinishSessions = true;
                            }
                        }
                    }

                    //Include the id in the new ids collection.
                    newIds.add(e.getAttribute("value"));
                }

                //Close session if needed
                if (shouldFinishSessions){
                    WebElement element = driver.findElement(By.name("save"));
                    element.click();
                }

                //Update ids collection.
                ids.clear();
                ids.addAll(newIds);

            }catch(NoSuchElementException e){}
        }
    }
}
