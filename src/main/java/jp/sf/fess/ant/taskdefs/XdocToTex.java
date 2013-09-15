package jp.sf.fess.ant.taskdefs;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import jp.sf.fess.ant.sax.XdocToTexParser;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ResourceCollection;

public class XdocToTex extends Task {

    private File destDir;

    private String encoding;

    protected Set<ResourceCollection> rcs = new LinkedHashSet<ResourceCollection>();

    public void execute() throws BuildException { //override
        validateAttributes();

        for (ResourceCollection rc : rcs) {
            if (rc instanceof FileSet && rc.isFilesystemOnly()) {
                FileSet fs = (FileSet) rc;
                DirectoryScanner ds = null;
                try {
                    ds = fs.getDirectoryScanner(getProject());
                } catch (BuildException e) {
                    log("Warning: " + getMessage(e), Project.MSG_ERR);
                    continue;
                }
                File fromDir = fs.getDir(getProject());
                String[] srcFiles = ds.getIncludedFiles();
                for (String src : srcFiles) {
                    File srcFile = new File(fromDir, src);
                    XdocToTexParser parser = new XdocToTexParser();
                    parser.setTask(this);
                    if (encoding != null) {
                        parser.setCharset(encoding);
                    }
                    parser.parse(srcFile, new File(destDir, srcFile.getName()
                            .replace(".xml", ".tex")));
                }
            }
        }
    }

    protected void validateAttributes() throws BuildException {
        if (this.destDir == null) {
            throw new BuildException("todir must be set.");
        }
        if (rcs.isEmpty()) {
            throw new BuildException("fileSet must be set.");
        }

    }

    /**
     * Handle getMessage() for exceptions.
     * @param ex the exception to handle
     * @return ex.getMessage() if ex.getMessage() is not null
     *         otherwise return ex.toString()
     */
    private String getMessage(Exception ex) {
        return ex.getMessage() == null ? ex.toString() : ex.getMessage();
    }

    /**
     * Set the destination directory.
     * @param destDir the destination directory.
     */
    public void setTodir(File destDir) {
        this.destDir = destDir;
    }

    /**
     * Add a set of files to copy.
     * @param set a set of files to copy.
     */
    public void addFileset(FileSet set) {
        add(set);
    }

    /**
     * Add a collection of files to copy.
     * @param res a resource collection to copy.
     * @since Ant 1.7
     */
    public void add(ResourceCollection res) {
        rcs.add(res);
    }

    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
