
package com.ly.zmn48644.mybatis.io;

import org.junit.Assert;
import org.junit.Test;


public class VFSTest {


    /**
     * 测试 getInstance 是否能够正常返回
     *
     * @throws Exception
     */
    @Test
    public void getInstanceShouldNotBeNull() throws Exception {
        VFS vsf = VFS.getInstance();
        Assert.assertNotNull(vsf);
    }


    /**
     * 测试多线程情况下,是否能够保证全局唯一性
     *
     * @throws InterruptedException
     */
    @Test
    public void getInstanceShouldNotBeNullInMultiThreadEnv() throws InterruptedException {
        final int threadCount = 3;

        Thread[] threads = new Thread[threadCount];
        InstanceGetterProcedure[] procedures = new InstanceGetterProcedure[threadCount];

        for (int i = 0; i < threads.length; i++) {
            String threadName = "Thread##" + i;

            procedures[i] = new InstanceGetterProcedure();
            threads[i] = new Thread(procedures[i], threadName);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // All caller got must be the same instance
        for (int i = 0; i < threadCount - 1; i++) {
            Assert.assertEquals(procedures[i].instanceGot, procedures[i + 1].instanceGot);
        }
    }

    private class InstanceGetterProcedure implements Runnable {

        volatile VFS instanceGot;

        @Override
        public void run() {
            instanceGot = VFS.getInstance();
        }
    }
}
