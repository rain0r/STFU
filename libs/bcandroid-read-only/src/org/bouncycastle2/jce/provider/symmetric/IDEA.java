package org.bouncycastle2.jce.provider.symmetric;

import org.bouncycastle2.crypto.CipherKeyGenerator;
import org.bouncycastle2.crypto.macs.CFBBlockCipherMac;
import org.bouncycastle2.crypto.macs.CBCBlockCipherMac;
import org.bouncycastle2.crypto.engines.IDEAEngine;
import org.bouncycastle2.crypto.modes.CBCBlockCipher;
import org.bouncycastle2.jce.provider.JCEBlockCipher;
import org.bouncycastle2.jce.provider.JCEKeyGenerator;
import org.bouncycastle2.jce.provider.JDKAlgorithmParameterGenerator;
import org.bouncycastle2.jce.provider.JDKAlgorithmParameters;
import org.bouncycastle2.jce.provider.JCEMac;
import org.bouncycastle2.jce.provider.JCESecretKeyFactory;
import org.bouncycastle2.asn1.misc.IDEACBCPar;
import org.bouncycastle2.asn1.ASN1InputStream;
import org.bouncycastle2.asn1.ASN1Sequence;

import javax.crypto.spec.IvParameterSpec;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.io.IOException;

public final class IDEA
{
    private IDEA()
    {
    }
    
    public static class ECB
        extends JCEBlockCipher
    {
        public ECB()
        {
            super(new IDEAEngine());
        }
    }

    public static class CBC
       extends JCEBlockCipher
    {
        public CBC()
        {
            super(new CBCBlockCipher(new IDEAEngine()), 64);
        }
    }

    public static class KeyGen
        extends JCEKeyGenerator
    {
        public KeyGen()
        {
            super("IDEA", 128, new CipherKeyGenerator());
        }
    }

    public static class PBEWithSHAAndIDEAKeyGen
       extends JCESecretKeyFactory.PBEKeyFactory
    {
       public PBEWithSHAAndIDEAKeyGen()
       {
           super("PBEwithSHAandIDEA-CBC", null, true, PKCS12, SHA1, 128, 64);
       }
    }

    static public class PBEWithSHAAndIDEA
        extends JCEBlockCipher
    {
        public PBEWithSHAAndIDEA()
        {
            super(new CBCBlockCipher(new IDEAEngine()));
        }
    }

    public static class AlgParamGen
        extends JDKAlgorithmParameterGenerator
    {
        protected void engineInit(
            AlgorithmParameterSpec genParamSpec,
            SecureRandom random)
            throws InvalidAlgorithmParameterException
        {
            throw new InvalidAlgorithmParameterException("No supported AlgorithmParameterSpec for IDEA parameter generation.");
        }

        protected AlgorithmParameters engineGenerateParameters()
        {
            byte[] iv = new byte[8];

            if (random == null)
            {
                random = new SecureRandom();
            }

            random.nextBytes(iv);

            AlgorithmParameters params;

            try
            {
                params = AlgorithmParameters.getInstance("IDEA", "BC2");
                params.init(new IvParameterSpec(iv));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e.getMessage());
            }

            return params;
        }
    }

    public static class AlgParams
        extends JDKAlgorithmParameters
    {
        private byte[]  iv;

        protected byte[] engineGetEncoded()
            throws IOException
        {
            return engineGetEncoded("ASN.1");
        }

        protected byte[] engineGetEncoded(
            String format)
            throws IOException
        {
            if (isASN1FormatString(format))
            {
                return new IDEACBCPar(engineGetEncoded("RAW")).getEncoded();
            }

            if (format.equals("RAW"))
            {
                byte[]  tmp = new byte[iv.length];

                System.arraycopy(iv, 0, tmp, 0, iv.length);
                return tmp;
            }

            return null;
        }

        protected AlgorithmParameterSpec localEngineGetParameterSpec(
            Class paramSpec)
            throws InvalidParameterSpecException
        {
            if (paramSpec == IvParameterSpec.class)
            {
                return new IvParameterSpec(iv);
            }

            throw new InvalidParameterSpecException("unknown parameter spec passed to IV parameters object.");
        }

        protected void engineInit(
            AlgorithmParameterSpec paramSpec)
            throws InvalidParameterSpecException
        {
            if (!(paramSpec instanceof IvParameterSpec))
            {
                throw new InvalidParameterSpecException("IvParameterSpec required to initialise a IV parameters algorithm parameters object");
            }

            this.iv = ((IvParameterSpec)paramSpec).getIV();
        }

        protected void engineInit(
            byte[] params)
            throws IOException
        {
            this.iv = new byte[params.length];

            System.arraycopy(params, 0, iv, 0, iv.length);
        }

        protected void engineInit(
            byte[] params,
            String format)
            throws IOException
        {
            if (format.equals("RAW"))
            {
                engineInit(params);
                return;
            }
            if (format.equals("ASN.1"))
            {
                ASN1InputStream aIn = new ASN1InputStream(params);
                IDEACBCPar      oct = new IDEACBCPar((ASN1Sequence)aIn.readObject());

                engineInit(oct.getIV());
                return;
            }

            throw new IOException("Unknown parameters format in IV parameters object");
        }

        protected String engineToString()
        {
            return "IDEA Parameters";
        }
    }
    
    public static class Mac
        extends JCEMac
    {
        public Mac()
        {
            super(new CBCBlockCipherMac(new IDEAEngine()));
        }
    }

    public static class CFB8Mac
        extends JCEMac
    {
        public CFB8Mac()
        {
            super(new CFBBlockCipherMac(new IDEAEngine()));
        }
    }
}
