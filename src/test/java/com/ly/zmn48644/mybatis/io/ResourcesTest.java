package com.ly.zmn48644.mybatis.io;

import java.io.File;
import java.io.IOException;

public class ResourcesTest {

    /**
     * 使用类加载器加载资源,需要深入了解java 的类加载机制.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        File resourceAsFile = Resources.getResourceAsFile("nodelet_test.xml");
        System.out.println(resourceAsFile.getAbsoluteFile());
    }


}
