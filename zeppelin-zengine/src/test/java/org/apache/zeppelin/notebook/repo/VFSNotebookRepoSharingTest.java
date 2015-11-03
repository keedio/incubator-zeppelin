package org.apache.zeppelin.notebook.repo;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.zeppelin.conf.ZeppelinConfiguration;
import org.apache.zeppelin.conf.ZeppelinConfiguration.ConfVars;
import org.apache.zeppelin.interpreter.InterpreterFactory;
import org.apache.zeppelin.interpreter.InterpreterOption;
import org.apache.zeppelin.interpreter.mock.MockInterpreter1;
import org.apache.zeppelin.interpreter.mock.MockInterpreter2;
import org.apache.zeppelin.notebook.JobListenerFactory;
import org.apache.zeppelin.notebook.Note;
import org.apache.zeppelin.scheduler.Job;
import org.apache.zeppelin.scheduler.Job.Status;
import org.apache.zeppelin.scheduler.JobListener;
import org.apache.zeppelin.scheduler.SchedulerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is a class test for testing utility methods in VFSNotebookRepo about sharing notes in
 * the working filesystem provided by VFS2's file system manager.
 */

/**
 * Created by luislazaro on 28/10/15.
 * lalazaro@keedio.com
 * Keedio
 */
public class VFSNotebookRepoSharingTest implements JobListenerFactory {
    private File tmpDir;
    private ZeppelinConfiguration conf;
    private SchedulerFactory schedulerFactory;
    private File notebookDir;
    private NotebookRepo notebookRepo;
    private InterpreterFactory factory;

    @Before
    public void setUp() throws Exception {
        tmpDir = new File(System.getProperty("java.io.tmpdir")+"/ZeppelinLTest_"+System.currentTimeMillis());
        tmpDir.mkdirs();
        new File(tmpDir, "conf").mkdirs();
        notebookDir = new File(System.getProperty("java.io.tmpdir")+"/ZeppelinLTest_"+System.currentTimeMillis()+"/notebook");
        notebookDir.mkdirs();

        System.setProperty(ConfVars.ZEPPELIN_HOME.getVarName(), tmpDir.getAbsolutePath());
        System.setProperty(ConfVars.ZEPPELIN_NOTEBOOK_DIR.getVarName(), notebookDir.getAbsolutePath());
        System.setProperty(ConfVars.ZEPPELIN_INTERPRETERS.getVarName(), "org.apache.zeppelin.interpreter.mock.MockInterpreter1,org.apache.zeppelin.interpreter.mock.MockInterpreter2");

        conf = ZeppelinConfiguration.create();

        this.schedulerFactory = new SchedulerFactory();

        MockInterpreter1.register("mock1", "org.apache.zeppelin.interpreter.mock.MockInterpreter1");
        MockInterpreter2.register("mock2", "org.apache.zeppelin.interpreter.mock.MockInterpreter2");

        factory = new InterpreterFactory(conf, new InterpreterOption(false), null);
        notebookRepo = new VFSNotebookRepo(conf);
    }

    @After
    public void tearDown() throws Exception {
        delete(tmpDir);

    }

    @Override
    public JobListener getParagraphJobListener(Note note) {
        return new JobListener(){

            @Override
            public void onProgressUpdate(Job job, int progress) {
            }

            @Override
            public void beforeStatusChange(Job job, Status before, Status after) {
            }

            @Override
            public void afterStatusChange(Job job, Status before, Status after) {
            }
        };
    }

    private void delete(File file){
        if(file.isFile()) file.delete();
        else if(file.isDirectory()){
            File [] files = file.listFiles();
            if(files!=null && files.length>0){
                for(File f : files){
                    delete(f);
                }
            }
            file.delete();
        }
    }

    /**
     * test vfsrepo share note via setting note's flag isShared to true and
     * adding new owner to owners's list.
     * @throws IOException
     */
    @Test
    public void testShare() throws IOException {
        File srcDir = new File("src/test/resources/2B4Z1MYWC");
        File destDir = new File(notebookDir.getAbsolutePath() + "/users/anonymous/2B4Z1MYWC");
        destDir.getParentFile().mkdirs();

        try {
            FileUtils.copyDirectory(srcDir, destDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert(notebookRepo.share("2B4Z1MYWC", "anonymous", "user1"));
        assertEquals(1, notebookRepo.listShared("user1").size());
    }

    /**
     * test vfsrepo revokeShare note via setting note's flag isShared to false.
     * @throws IOException
     */
    @Test
    public void testRevokeShare() throws IOException {
        File srcDir = new File("src/test/resources/2B4Z1MYWC");
        File destDir = new File(notebookDir.getAbsolutePath() + "/users/anonymous/2B4Z1MYWC_testRevoke");

        destDir.getParentFile().mkdirs();

        try {
            FileUtils.copyDirectory(srcDir, destDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        notebookRepo.share("2B4Z1MYWC_testRevoke", "anonymous", "user1");
        assertEquals(1, notebookRepo.listShared("user1").size());
        assertFalse(notebookRepo.revokeShare("2B4Z1MYWC_testRevoke", "anonymous"));
        assertEquals(0, notebookRepo.listShared("user1").size());
    }


    /**
     * Test method listShared(owner). This method returns list of
     * shared notesInfo with owner.
     * @throws IOException
     */
    @Test
    public void testListShared() throws IOException{

        for (int i = 0 ; i < 4 ; i++){
            File srcDir = new File("src/test/resources/2B4Z1MYWC");
            File destDir = new File(notebookDir.getAbsolutePath() + "/users/anonymous/2B4Z1MYWC_" + i);

            destDir.getParentFile().mkdirs();

            try {
                FileUtils.copyDirectory(srcDir, destDir);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (i == 2){
                notebookRepo.share("2B4Z1MYWC_" + i, "anonymous", "user1");
            }
        }
        assertEquals(1, notebookRepo.listShared("user1").size());

        notebookRepo.revokeShare("2B4Z1MYWC_2", "anonymous");
        assertEquals(0, notebookRepo.listShared("user1").size());

        notebookRepo.share("2B4Z1MYWC_1", "anonymous", "user1");
        notebookRepo.share("2B4Z1MYWC_2" , "anonymous", "user1");
        assertEquals(2, notebookRepo.listShared("user1").size());
        notebookRepo.share("2B4Z1MYWC_3" , "anonymous", "user1");
        assertEquals(3, notebookRepo.listShared("user1").size());
    }

    /**
     * Test method list().This method returns all notes stored in users/.
     * This method should be called when all notes are required.
     */
    @Test
    public void testList() throws IOException{
        for ( int i = 0 ; i < 4 ; i++){
            File srcDir = new File("src/test/resources/2B4Z1MYWC");
            File destDirUser1 = new File(notebookDir.getAbsolutePath() + "/users/user1/2B4Z1MYWC_" + i);
            File destDirUser2 = new File(notebookDir.getAbsolutePath() + "/users/user2/2B4Z1MYWC_" + i);
            destDirUser1.getParentFile().mkdirs();
            destDirUser2.getParentFile().mkdirs();

            try {
                FileUtils.copyDirectory(srcDir, destDirUser1);
                FileUtils.copyDirectory(srcDir, destDirUser2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        assertEquals(8, notebookRepo.list().size());
    }

    /**
     * Test method list(owner). This method returns all notes created and shared with owner.
     */
    @Test
    public void testListOwner() throws IOException{

        File srcDir = new File("src/test/resources/2B4Z1MYWC");
        File destDirAnom = new File(notebookDir.getAbsolutePath() + "/users/anonymous/2B4Z1MYWC");
        try {
            FileUtils.copyDirectory(srcDir, destDirAnom);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for ( int i = 0 ; i < 4 ; i++){

            File destDirUser1 = new File(notebookDir.getAbsolutePath() + "/users/user1/2B4Z1MYWC_" + i);
            File destDirUser2 = new File(notebookDir.getAbsolutePath() + "/users/user2/2B4Z1MYWC_" + i);
            destDirUser1.getParentFile().mkdirs();
            destDirUser2.getParentFile().mkdirs();

            try {
                FileUtils.copyDirectory(srcDir, destDirUser1);
                FileUtils.copyDirectory(srcDir, destDirUser2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        assertEquals(4, notebookRepo.list("user1").size());
        assertEquals(4, notebookRepo.list("user2").size());
        notebookRepo.share("2B4Z1MYWC", "anonymous", "user2");
        assertEquals(5, notebookRepo.list("user2").size());
        notebookRepo.share("2B4Z1MYWC_3", "user1", "user2");
        assertEquals(6, notebookRepo.list("user2").size());

    }

    @Test
    public void testKickOut() throws IOException {
        File srcDir = new File("src/test/resources/2B4Z1MYWC");
        File destDir = new File(notebookDir.getAbsolutePath() + "/users/anonymous/2B4Z1MYWC");
        destDir.getParentFile().mkdirs();

        try {
            FileUtils.copyDirectory(srcDir, destDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert(notebookRepo.share("2B4Z1MYWC", "anonymous", "user1"));
        assert(notebookRepo.share("2B4Z1MYWC", "anonymous", "user2"));
        assertEquals(1, notebookRepo.listShared("user1").size());
        assertEquals("user1", notebookRepo.kickOut("2B4Z1MYWC", "anonymous", "user1"));
        assertEquals(0, notebookRepo.listShared("user1").size());
    }


}
