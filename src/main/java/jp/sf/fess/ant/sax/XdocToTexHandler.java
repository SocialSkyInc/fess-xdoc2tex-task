package jp.sf.fess.ant.sax;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class XdocToTexHandler extends DefaultHandler {

    private LinkedList<TagInfo> tagInfoList = new LinkedList<TagInfo>();

    private Writer writer;

    private TableInfo tableInfo = null;

    public XdocToTexHandler(Writer writer) {
        this.writer = writer;
    }

    public void startDocument() {
    }

    public void startElement(String namespaceURI, String localName,
            String qName, Attributes attrs) {

        TagInfo tagInfo = new TagInfo();
        tagInfo.setName(qName);
        for (int i = 0; i < attrs.getLength(); i++) {
            tagInfo.add(attrs.getQName(i), attrs.getValue(i));
        }
        tagInfoList.add(tagInfo);

        if ("section".equalsIgnoreCase(qName)) {
            write("\\section{");
            write(tagInfo.getAttr("name").trim());
            write("}\n");
        } else if ("subsection".equalsIgnoreCase(qName)) {
            write("\\subsection{");
            write(tagInfo.getAttr("name").trim());
            write("}\n");
        } else if ("ul".equalsIgnoreCase(qName)) {
            write("\\begin{itemize}");
        } else if ("table".equalsIgnoreCase(qName)) {
            tableInfo = new TableInfo();
            String columnInfo = tagInfo.getAttr("columninfo");
            if (columnInfo != null) {
                tableInfo.setColumnInfo(columnInfo);
            }
            String widthInfo = tagInfo.getAttr("widthinfo");
            if (widthInfo != null) {
                tableInfo.setWidthInfo(widthInfo);
            }
        } else if ("tr".equalsIgnoreCase(qName)) {
            tableInfo.newRow();
        } else if ("th".equalsIgnoreCase(qName)) {
            tableInfo.newBuffer();
        } else if ("td".equalsIgnoreCase(qName)) {
            tableInfo.newBuffer();
        }

    }

    public void endElement(String namespaceURI, String localName, String qName) {
        TagInfo tagInfo = tagInfoList.removeLast();

        if (tableInfo != null) {
            if ("table".equalsIgnoreCase(qName)) {
                String lineInfo = tagInfo.getAttr("lineinfo");
                if (lineInfo == null) {
                    tableInfo.endTable("\\hline");
                } else {
                    tableInfo.endTable(lineInfo);
                }
                write(tableInfo.toTexString());
                tableInfo = null;
            } else if ("tr".equalsIgnoreCase(qName)) {
                String lineInfo = tagInfo.getAttr("lineinfo");
                if (lineInfo == null) {
                    tableInfo.endRow("\\hline");
                } else {
                    tableInfo.endRow(lineInfo);
                }
            } else if ("th".equalsIgnoreCase(qName)) {
                tableInfo.addCell();
            } else if ("td".equalsIgnoreCase(qName)) {
                tableInfo.addCell();
            } else if ("caption".equalsIgnoreCase(qName)) {
                tableInfo.setCaption(tagInfo.getBody());
            }
        } else if ("img".equalsIgnoreCase(qName)) {
            String src = tagInfo.getAttr("src");
            String srcName = src.replaceAll(".*/([^/]+)\\.[^/]+", "$1");
            String title = tagInfo.getAttr("alt");
            if (title == null) {
                title = tagInfo.getAttr("title");
            }
            String sizeInfo = tagInfo.getAttr("sizeinfo");
            if (sizeInfo == null) {
                sizeInfo = "width=10cm";
            }
            write("\\begin{figure}[ht]\n");
            write("\\begin{center}\n");
            write("\\includegraphics[");
            write(sizeInfo);
            write("]{images/");
            write(srcName);
            write(".eps}\n");
            write("\\caption{");
            write(title);
            write("}\n");
            write("\\label{fig:");
            write(srcName);
            write("}\n");
            write("\\end{center}\n");
            write("\\end{figure}\n");
        } else if ("ul".equalsIgnoreCase(qName)) {
            write("\n\\end{itemize}\n");
        } else if ("p".equalsIgnoreCase(qName)) {
            write("\n");
        } else if ("source".equalsIgnoreCase(qName)) {
            write("\\begin{screen}\n");
            write("\\begin{small}\n");
            write("\\begin{verbatim}\n");
            write(tagInfo.getBody().trim());
            write("\n\\end{verbatim}\n");
            write("\n\\end{small}\n");
            write("\n\\end{screen}\n");
        }

    }

    public void endDocument() {
    }

    public void characters(char[] ch, int start, int length) {
        TagInfo tagInfo = tagInfoList.getLast();
        String tagName = tagInfo.getName();

        if ("p".equalsIgnoreCase(tagName)) {
            String str = escape(new String(ch, start, length));
            write(str.trim());
            write("\n");
        } else if ("a".equalsIgnoreCase(tagName)) {
            String str = escape(new String(ch, start, length));
            write(str.trim());
        } else if ("code".equalsIgnoreCase(tagName)) {
            String str = escape(new String(ch, start, length));
            write("「" + str.trim() + "」");
        } else if ("b".equalsIgnoreCase(tagName)) {
            String str = escape(new String(ch, start, length));
            write("\\textbf{" + str.trim() + "}");
        } else if ("li".equalsIgnoreCase(tagName)) {
            String str = escape(new String(ch, start, length));
            write("\n\\item ");
            write(str.trim());
        } else if ("source".equalsIgnoreCase(tagName)) {
            String str = new String(ch, start, length);
            tagInfo.addBody(str);
        } else {
            String str = escape(new String(ch, start, length));
            if (tableInfo != null && tableInfo.hasBuffer()) {
                tableInfo.appendToBuf(str);
            } else {
                tagInfo.addBody(str);
            }
        }

    }

    private void write(String str) {
        if (tableInfo != null && tableInfo.hasBuffer()) {
            tableInfo.appendToBuf(str);
        } else {
            try {
                writer.write(str);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    String escape(String str) {
        if (str != null) {
            return str//
                    .replace("\\", "\\textbackslash{}")//
                    .replace("<", "\\<")//
                    .replace(">", "\\>")//
                    .replace("$", "\\$")//
                    .replace("#", "\\#")//
                    .replace("_", "\\_")//
                    .replace("&", "\\&")//
                    .replace("^", "\\^{}")//
                    .replace("~", "\\~{}")//
                    .replace("%", "\\%")//
                    .replace("～", "$\\sim$")//
            ;
        }
        return str;
    }

    private static class TagInfo {
        private String name;

        private StringBuilder bodyBuf = new StringBuilder();

        private Map<String, String> attrMap = new HashMap<String, String>();

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        public String getAttr(String key) {
            return attrMap.get(key);
        }

        public void add(String key, String value) {
            this.attrMap.put(key, value);
        }

        public void addBody(String str) {
            bodyBuf.append(str);
        }

        public String getBody() {
            return bodyBuf.toString();
        }
    }

    private static class TableInfo {
        private List<List<String>> rowList = new ArrayList<List<String>>();

        private List<String> lineList = new ArrayList<String>();

        private String lineTop;

        private String caption = null;

        private String columnInfo = null;

        private StringBuilder buf;

        private String widthInfo;

        public String toTexString() {
            StringBuilder contentBuf = new StringBuilder();
            contentBuf.append("\\begin{table}[htb]\n");
            contentBuf.append("\\begin{center}\n");
            if (caption != null) {
                contentBuf.append("\\caption{").append(caption).append("}\n");
            }
            if (widthInfo != null) {
                contentBuf.append("\\begin{tabular*}{").append(widthInfo)
                        .append("}{@{¥extracolsep{¥fill}}")
                        .append(getColumnInfo()).append('}');
            } else {
                contentBuf.append("\\begin{tabular}{").append(getColumnInfo())
                        .append('}');
            }
            if (lineTop != null) {
                contentBuf.append(' ').append(lineTop);
            }
            contentBuf.append('\n');
            int rowSize = rowList.size();
            for (int i = 0; i < rowSize; i++) {
                List<String> columnList = rowList.get(i);
                int columnSize = columnList.size();
                for (int j = 0; j < columnSize; j++) {
                    contentBuf.append(columnList.get(j));
                    if (j == columnSize - 1) {
                        contentBuf.append(" \\\\");
                    } else {
                        contentBuf.append(" & ");
                    }
                }
                String lineEnd = lineList.get(i);
                if (lineEnd != null) {
                    contentBuf.append(' ').append(lineEnd);
                }
                contentBuf.append('\n');
            }
            if (widthInfo != null) {
                contentBuf.append("\\end{tabular*}\n");
            } else {
                contentBuf.append("\\end{tabular}\n");
            }
            contentBuf.append("\\end{center}\n");
            contentBuf.append("\\end{table}\n");
            return contentBuf.toString();
        }

        public void setWidthInfo(String widthInfo) {
            this.widthInfo = widthInfo;
        }

        private String getColumnInfo() {
            if (columnInfo != null) {
                return columnInfo;
            }
            StringBuilder builder = new StringBuilder();
            int size = rowList.get(rowList.size() - 1).size();
            for (int i = 0; i < size; i++) {
                builder.append('l');
            }
            return builder.toString();
        }

        public void newRow() {
            rowList.add(new ArrayList<String>());
        }

        public void appendToBuf(String str) {
            buf.append(str);
        }

        public boolean hasBuffer() {
            return buf != null;
        }

        public void newBuffer() {
            buf = new StringBuilder();
        }

        public void endTable(String lineTopInfo) {
            this.lineTop = lineTopInfo;
        }

        public void endRow(String line) {
            lineList.add(line);
        }

        public void addCell() {
            rowList.get(rowList.size() - 1).add(buf.toString());
            buf = null;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }

        public void setColumnInfo(String info) {
            this.columnInfo = info;
        }
    }

}
