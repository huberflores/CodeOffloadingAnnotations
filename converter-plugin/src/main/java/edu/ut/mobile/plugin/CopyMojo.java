package edu.ut.mobile.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.*;
import java.nio.channels.FileChannel;


/**
 * Copy the needed artifacts during deployment
 *
 * @goal copy
 * @phase process-resources
 */
public class CopyMojo extends AbstractMojo {

    /**
     * Location of the files processed after processing.
     * <p/>
     * expression="${copy.destination}"  default-value="${project.build.sourceDirectory}"
     *
     * @parameter expression="${copy.destination}"  default-value="${project.basedir/src/main/resources}"
     */
    private File destination;


    /**
     * The Location of the files to copy.
     *
     * @parameter expression="${copy.source}"  default-value="${project.build.sourceDirectory/edu/ut/mobile/network}"
     * @required
     */
    private File source;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!source.isDirectory()) {
            getLog().error("The input directory should be a directory");
        }
        if (!destination.isDirectory()) {
            getLog().error("The output directory should be a directory");
        }
        if (source.isDirectory() && destination.isDirectory()) {
            File[] filenames = source.listFiles();
            if (filenames != null) {
                for (File file : filenames) {
                    File renamedDestination = new File(destination.getAbsolutePath() + File.separator + file.getName().replace("java", "txt"));
                    copyFile(file, renamedDestination);
                    getLog().info("copied file  " + file.getAbsolutePath() + " to " + renamedDestination.getAbsolutePath());
                }
            }
        }
    }


    public void copyFile(File in, File out) {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(in).getChannel();
            outChannel = new FileOutputStream(out).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (FileNotFoundException e) {
            getLog().debug("directory not found", e);
        } catch (IOException e) {
            getLog().debug("", e);
        } finally {
            if (inChannel != null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    getLog().debug("input channel not closed", e);
                }
            }
            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    getLog().debug("output channel not closed", e);
                }
            }
        }
    }
}
