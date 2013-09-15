package jp.sf.fess.ant.sax;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class XdocToTexParser {
    private String charset = "UTF-8";

    private Task task;

    public void parse(File srcFile, File destFile) {
        log("Input:  " + srcFile.getAbsolutePath());
        log("Output: " + destFile.getAbsolutePath());

        FileInputStream fis = null;
        BufferedWriter bw = null;
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();

            fis = new FileInputStream(srcFile);
            bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(destFile), charset));

            XdocToTexHandler sh = new XdocToTexHandler(bw);

            sp.parse(fis, sh);
            bw.flush();
        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void log(String msg) {
        if (task != null) {
            task.log(msg);
        } else {
            System.out.println(msg);
        }
    }

    /**
     * @param charset the charset to set
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setTask(Task Task) {
        this.task = task;
    }

}
