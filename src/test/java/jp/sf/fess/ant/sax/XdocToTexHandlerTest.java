package jp.sf.fess.ant.sax;

import junit.framework.TestCase;

public class XdocToTexHandlerTest extends TestCase {
    public void test_escape() {
        XdocToTexHandler handler = new XdocToTexHandler(null);
        assertEquals("", handler.escape(""));
        assertEquals("\\$", handler.escape("$"));
        assertEquals("\\#", handler.escape("#"));
        assertEquals("\\_", handler.escape("_"));
        assertEquals("\\&", handler.escape("&"));
    }
}
