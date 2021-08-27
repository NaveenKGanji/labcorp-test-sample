import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;
import cucumber.api.junit.Cucumber;


@RunWith(Cucumber.class)
@CucumberOptions(features = "src/main/resources/features",
        tags = {"@LABCORP_TEST"},
        plugin = {"pretty", "html:target/cucumber", "json:target/cucumber.json"},
        glue = {"steps"})
public class RunCukesTest {
}

