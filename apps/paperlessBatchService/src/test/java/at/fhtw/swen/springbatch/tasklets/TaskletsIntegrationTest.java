package at.fhtw.swen.springbatchdemo.tasklets;

import at.fhtw.swen.springbatchdemo.SpringBatchDemoApplication;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBatchTest
@SpringJUnitConfig(SpringBatchDemoApplication.class)
@ContextConfiguration(classes = {TaskletsConfig.class, SpringBatchDemoApplication.class})
public class TaskletsIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    public void givenTaskletsJob_whenJobEnds_thenStatusCompleted()
            throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
    }
}