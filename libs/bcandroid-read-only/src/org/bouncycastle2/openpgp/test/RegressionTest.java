package org.bouncycastle2.openpgp.test;

import java.security.Security;

import org.bouncycastle2.util.test.Test;
import org.bouncycastle2.util.test.TestResult;

public class RegressionTest
{
    public static Test[]    tests = {
        new PGPKeyRingTest(),
        new PGPRSATest(),
        new PGPDSATest(),
        new PGPDSAElGamalTest(),
        new PGPPBETest(),
        new PGPMarkerTest(),
        new PGPPacketTest(),
        new PGPArmoredTest(),
        new PGPSignatureTest(),
        new PGPClearSignedSignatureTest(),
        new PGPCompressionTest()
    };

    public static void main(
        String[]    args)
    {
        Security.addProvider(new org.bouncycastle2.jce.provider.BouncyCastleProvider());

        for (int i = 0; i != tests.length; i++)
        {
            TestResult  result = tests[i].perform();
            System.out.println(result);
        }
    }
}

