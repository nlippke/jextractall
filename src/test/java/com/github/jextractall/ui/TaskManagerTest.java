package com.github.jextractall.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.junit.Before;
import org.junit.Test;

import com.github.jextractall.ui.model.ConfigModelFactory;
import com.github.jextractall.ui.model.ExtractorTask;
import com.github.jextractall.unpack.DummyExtractor;
import com.github.jextractall.unpack.common.FileUtils;
import com.github.jextractall.unpack.common.Result.ResultBuilder;

import javafx.embed.swing.JFXPanel;

public class TaskManagerTest {

    @Before
    public void initJavaFx() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new JFXPanel(); // initializes JavaFX environment
                latch.countDown();
            }
        });
        latch.await();
    }

    private Path getRootDirectory() {
        return new File("./test").getAbsoluteFile().toPath();
    }

    @Test
    public void testMe() throws InterruptedException {
    	
        ExtractorTask t1 = new ExtractorTask(
                new DummyExtractor(ResultBuilder.newInstance().withExtractedFile(Paths.get("test.txt")).create()),
                FileUtils.adjustPath(getRootDirectory(), "test.zip"));
        
        ExtractorTask t2 = new ExtractorTask(
                new DummyExtractor(ResultBuilder.newInstance().withException(new NullPointerException()).create()),
                FileUtils.adjustPath(getRootDirectory(), "test.zip"));
        CountDownLatch success = new CountDownLatch(1);
        CountDownLatch failure = new CountDownLatch(1);
        TaskManager manager = new TaskManager(Arrays.asList(t1, t2));
        manager.registerCallback(new TaskCallback() {
            @Override
            public void onComplete(ExtractorTask task) {
                success.countDown();
            }

            @Override
            public void onFailure(ExtractorTask task) {
                failure.countDown();
            }

			@Override
			public void onCancelled(ExtractorTask task) {
			}
        });
        manager.runTasks(ConfigModelFactory.defaults());
        success.await(200,TimeUnit.SECONDS);
        failure.await(200, TimeUnit.SECONDS);
        assertEquals(0, success.getCount());
        assertEquals(0, failure.getCount());
        assertFalse(manager.isRunning());
    }

}
