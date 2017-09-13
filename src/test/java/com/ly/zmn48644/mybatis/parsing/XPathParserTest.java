package com.ly.zmn48644.mybatis.parsing;

import com.ly.zmn48644.mybatis.io.Resources;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class XPathParserTest {
    String resource = "nodelet_test.xml";
    XPathParser parser;
    InputStream inputStream;
    @Before
    public void setup() throws IOException {
        inputStream = Resources.getResourceAsStream(resource);
        parser = new XPathParser(inputStream, false, null, null);
    }

    @Test
    public void testEvalLong() throws IOException {

        assertEquals((Long) 1970l, parser.evalLong("/employee/birth_date/year"));
    }

    @Test
    public void testNodes() {
        XNode node = parser.evalNode("/employee/height");
        assertEquals("employee/height", node.getPath());
        assertEquals("employee[${id_var}]_height", node.getValueBasedIdentifier());
    }

}
