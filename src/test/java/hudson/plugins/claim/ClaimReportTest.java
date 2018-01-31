package hudson.plugins.claim;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.FreeStyleProject;
import hudson.model.ListView;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.FailureBuilder;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ClaimReportTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();


    private static final String JOB_NAME = "myjob";
    private FreeStyleProject job;
    private ListView view;

    @Before
    public void setUp() throws Exception {

        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.SEVERE);

        job = createFailingJobWithName(JOB_NAME);

        view = new ListView("DefaultView");
        j.jenkins.addView(view);
        j.jenkins.setPrimaryView(view);

    }

    private FreeStyleProject createFailingJobWithName(String jobName) throws IOException,
            InterruptedException, ExecutionException {
        FreeStyleProject project = j.createFreeStyleProject(jobName);
        project.getBuildersList().add(new FailureBuilder());
        project.getPublishersList().add(new ClaimPublisher());
        project.scheduleBuild2(0).get();
        return project;
    }

    @Test
    public void jobIsVisibleInClaimReport() throws Exception {
        // Given:
        view.add(job);
        //j.interactiveBreak();
        // When:
        HtmlPage page = j.createWebClient().goTo("claims/");
        // Then:
        DomElement element = page.getElementById("claim.build." + JOB_NAME);
        assertThat(element.isDisplayed(), is(true));
    }

    @Test
    public void jobNotPresentInDefaultViewIsVisibleInClaimReport() throws Exception {
        // When:
        HtmlPage page = j.createWebClient().goTo("claims/");
        // Then:
        DomElement element = page.getElementById("claim.build." + JOB_NAME);
        assertThat(element.isDisplayed(), is(true));
    }

}
