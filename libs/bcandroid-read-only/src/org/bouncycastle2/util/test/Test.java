package org.bouncycastle2.util.test;

public interface Test
{
    String getName();

    TestResult perform();
}
