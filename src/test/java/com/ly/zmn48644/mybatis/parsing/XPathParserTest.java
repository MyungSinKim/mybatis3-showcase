package com.ly.zmn48644.mybatis.parsing;

import com.ly.zmn48644.mybatis.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class XPathParserTest {

    @Test
    public void te() throws IOException {
        String resource = "nodelet_test.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        XPathParser parser = new XPathParser(inputStream, false, null, null);
        assertEquals((Long)1970l, parser.evalLong("/employee/birth_date/year"));
    }
}
