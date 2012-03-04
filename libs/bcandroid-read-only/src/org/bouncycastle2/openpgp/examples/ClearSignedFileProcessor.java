package org.bouncycastle2.openpgp.examples;

import org.bouncycastle2.bcpg.ArmoredInputStream;
import org.bouncycastle2.bcpg.ArmoredOutputStream;
import org.bouncycastle2.bcpg.BCPGOutputStream;
import org.bouncycastle2.jce.provider.BouncyCastleProvider;
import org.bouncycastle2.openpgp.PGPException;
import org.bouncycastle2.openpgp.PGPObjectFactory;
import org.bouncycastle2.openpgp.PGPPrivateKey;
import org.bouncycastle2.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle2.openpgp.PGPSecretKey;
import org.bouncycastle2.openpgp.PGPSecretKeyRing;
import org.bouncycastle2.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle2.openpgp.PGPSignature;
import org.bouncycastle2.openpgp.PGPSignatureGenerator;
import org.bouncycastle2.openpgp.PGPSignatureList;
import org.bouncycastle2.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle2.openpgp.PGPUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.util.Iterator;

/**
 * A simple utility class that creates clear signed files and verifies them.
 * <p>
 * To sign a file: ClearSignedFileProcessor -s fileName secretKey passPhrase.<br>
 * <p>
 * To decrypt: ClearSignedFileProcessor -v fileName signatureFile publicKeyFile.
 */
public class ClearSignedFileProcessor
{
    /**
     * A simple routine that opens a key ring file and loads the first available key suitable for
     * signature generation.
     * 
     * @param in  stream to read the secret key ring collection from.
     * @return  a secret key.
     * @throws IOException on a problem with using the input stream.
     * @throws PGPException if there is an issue parsing the input stream.
     */
    private static PGPSecretKey readSecretKey(
        InputStream    in)
        throws IOException, PGPException
    {    
        PGPSecretKeyRingCollection        pgpSec = new PGPSecretKeyRingCollection(in);

        //
        // we just loop through the collection till we find a key suitable for encryption, in the real
        // world you would probably want to be a bit smarter about this.
        //
        PGPSecretKey    key = null;
        
        //
        // iterate through the key rings.
        //
        Iterator rIt = pgpSec.getKeyRings();
        
        while (key == null && rIt.hasNext())
        {
            PGPSecretKeyRing    kRing = (PGPSecretKeyRing)rIt.next();    
            Iterator                        kIt = kRing.getSecretKeys();
            
            while (key == null && kIt.hasNext())
            {
                PGPSecretKey    k = (PGPSecretKey)kIt.next();
                
                if (k.isSigningKey())
                {
                    key = k;
                }
            }
        }
        
        if (key == null)
        {
            throw new IllegalArgumentException("Can't find signing key in key ring.");
        }
        
        return key;
    }

    private static int readInputLine(ByteArrayOutputStream bOut, InputStream fIn)
        throws IOException
    {
        bOut.reset();

        int lookAhead = -1;
        int ch;

        while ((ch = fIn.read()) >= 0)
        {
            bOut.write(ch);
            if (ch == '\r' || ch == '\n')
            {
                lookAhead = readPassedEOL(bOut, ch, fIn);
                break;
            }
        }

        return lookAhead;
    }

    private static int readInputLine(ByteArrayOutputStream bOut, int lookAhead, InputStream fIn)
        throws IOException
    {
        bOut.reset();

        int ch = lookAhead;

        do
        {
            bOut.write(ch);
            if (ch == '\r' || ch == '\n')
            {
                lookAhead = readPassedEOL(bOut, ch, fIn);
                break;
            }
        }
        while ((ch = fIn.read()) >= 0);

        if (ch < 0)
        {
            lookAhead = -1;
        }
        
        return lookAhead;
    }

    private static int readPassedEOL(ByteArrayOutputStream bOut, int lastCh, InputStream fIn)
        throws IOException
    {
        int lookAhead = fIn.read();

        if (lastCh == '\r' && lookAhead == '\n')
        {
            bOut.write(lookAhead);
            lookAhead = fIn.read();
        }

        return lookAhead;
    }

    /*
     * verify a clear text signed file
     */
    private static void verifyFile(
        InputStream        in,
        InputStream        keyIn,
        String             resultName)
        throws Exception
    {
        ArmoredInputStream    aIn = new ArmoredInputStream(in);
        OutputStream          out = new BufferedOutputStream(new FileOutputStream(resultName));



        //
        // write out signed section using the local line separator.
        // note: although we leave it in trailing white space as it is not verifiable.
        // Some people prefer to remove it.
        //
        ByteArrayOutputStream lineOut = new ByteArrayOutputStream();
        int                   lookAhead = readInputLine(lineOut, aIn);
        byte[]                lineSep = getLineSeparator();

        if (lookAhead != -1 && aIn.isClearText())
        {
            byte[] line = lineOut.toByteArray();
            out.write(line, 0, getLengthWithoutSeparator(line));
            out.write(lineSep);

            while (lookAhead != -1 && aIn.isClearText())
            {
                lookAhead = readInputLine(lineOut, lookAhead, aIn);
                
                line = lineOut.toByteArray();
                out.write(line, 0, getLengthWithoutSeparator(line));
                out.write(lineSep);
            }
        }

        out.close();

        PGPPublicKeyRingCollection pgpRings = new PGPPublicKeyRingCollection(keyIn);

        PGPObjectFactory           pgpFact = new PGPObjectFactory(aIn);
        PGPSignatureList           p3 = (PGPSignatureList)pgpFact.nextObject();
        PGPSignature               sig = p3.get(0);

        sig.initVerify(pgpRings.getPublicKey(sig.getKeyID()), "BC2");

        //
        // read the input, making sure we ingore the last newline.
        //

        InputStream sigIn = new BufferedInputStream(new FileInputStream(resultName));

        lookAhead = readInputLine(lineOut, sigIn);

        processLine(sig, lineOut.toByteArray());

        if (lookAhead != -1)
        {
            do
            {
                lookAhead = readInputLine(lineOut, lookAhead, sigIn);

                sig.update((byte)'\r');
                sig.update((byte)'\n');

                processLine(sig, lineOut.toByteArray());
            }
            while (lookAhead != -1);
        }

        if (sig.verify())
        {
            System.out.println("signature verified.");
        }
        else
        {
            System.out.println("signature verification failed.");
        }
    }

    private static byte[] getLineSeparator()
    {
        String nl = System.getProperty("line.separator");
        byte[] nlBytes = new byte[nl.length()];

        for (int i = 0; i != nlBytes.length; i++)
        {
            nlBytes[i] = (byte)nl.charAt(i);
        }

        return nlBytes;
    }

    /*
     * create a clear text signed file.
     */
    private static void signFile(
        String          fileName,
        InputStream     keyIn,
        OutputStream    out,
        char[]          pass,
        String          digestName)
        throws IOException, NoSuchAlgorithmException, NoSuchProviderException, PGPException, SignatureException
    {    
        int digest;
        
        if (digestName.equals("SHA256"))
        {
            digest = PGPUtil.SHA256;
        }
        else if (digestName.equals("SHA384"))
        {
            digest = PGPUtil.SHA384;
        }
        else if (digestName.equals("SHA512"))
        {
            digest = PGPUtil.SHA512;
        }
        else if (digestName.equals("MD5"))
        {
            digest = PGPUtil.MD5;
        }
        else if (digestName.equals("RIPEMD160"))
        {
            digest = PGPUtil.RIPEMD160;
        }
        else
        {
            digest = PGPUtil.SHA1;
        }
        
        PGPSecretKey                    pgpSecKey = readSecretKey(keyIn);
        PGPPrivateKey                   pgpPrivKey = pgpSecKey.extractPrivateKey(pass, "BC2");        
        PGPSignatureGenerator           sGen = new PGPSignatureGenerator(pgpSecKey.getPublicKey().getAlgorithm(), digest, "BC2");
        PGPSignatureSubpacketGenerator  spGen = new PGPSignatureSubpacketGenerator();
        
        sGen.initSign(PGPSignature.CANONICAL_TEXT_DOCUMENT, pgpPrivKey);
        
        Iterator    it = pgpSecKey.getPublicKey().getUserIDs();
        if (it.hasNext())
        {
            spGen.setSignerUserID(false, (String)it.next());
            sGen.setHashedSubpackets(spGen.generate());
        }
        
        FileInputStream        fIn = new FileInputStream(fileName);
        ArmoredOutputStream    aOut = new ArmoredOutputStream(out);
        
        aOut.beginClearText(digest);

        //
        // note the last \n/\r/\r\n in the file is ignored
        //
        ByteArrayOutputStream lineOut = new ByteArrayOutputStream();
        int lookAhead = readInputLine(lineOut, fIn);

        processLine(aOut, sGen, lineOut.toByteArray());

        if (lookAhead != -1)
        {
            do
            {
                lookAhead = readInputLine(lineOut, lookAhead, fIn);

                sGen.update((byte)'\r');
                sGen.update((byte)'\n');

                processLine(aOut, sGen, lineOut.toByteArray());
            }
            while (lookAhead != -1);
        }
        
        aOut.endClearText();
        
        BCPGOutputStream            bOut = new BCPGOutputStream(aOut);
        
        sGen.generate().encode(bOut);

        aOut.close();
    }

    private static void processLine(PGPSignature sig, byte[] line)
        throws SignatureException, IOException
    {
        int length = getLengthWithoutWhiteSpace(line);
        if (length > 0)
        {
            sig.update(line, 0, length);
        }
    }

    private static void processLine(OutputStream aOut, PGPSignatureGenerator sGen, byte[] line)
        throws SignatureException, IOException
    {
        int length = getLengthWithoutWhiteSpace(line);
        if (length > 0)
        {
            sGen.update(line, 0, length);
        }

        aOut.write(line, 0, line.length);
    }

    private static int getLengthWithoutSeparator(byte[] line)
    {
        int    end = line.length - 1;

        while (end >= 0 && isLineEnding(line[end]))
        {
            end--;
        }

        return end + 1;
    }

    private static boolean isLineEnding(byte b)
    {
        return b == '\r' || b == '\n';
    }

    private static int getLengthWithoutWhiteSpace(byte[] line)
    {
        int    end = line.length - 1;

        while (end >= 0 && isWhiteSpace(line[end]))
        {
            end--;
        }

        return end + 1;
    }

    private static boolean isWhiteSpace(byte b)
    {
        return b == '\r' || b == '\n' || b == '\t' || b == ' ';
    }

    public static void main(
        String[] args)
        throws Exception
    {
        Security.addProvider(new BouncyCastleProvider());

        if (args[0].equals("-s"))
        {
            InputStream        keyIn = PGPUtil.getDecoderStream(new FileInputStream(args[2]));
            FileOutputStream   out = new FileOutputStream(args[1] + ".asc");
            
            if (args.length == 4)
            {
                signFile(args[1], keyIn, out, args[3].toCharArray(), "SHA1");
            }
            else
            {
                signFile(args[1], keyIn, out, args[3].toCharArray(), args[4]);
            }
        }
        else if (args[0].equals("-v"))
        {
            if (args[1].indexOf(".asc") < 0)
            {
                System.err.println("file needs to end in \".asc\"");
                System.exit(1);
            }
            FileInputStream    in = new FileInputStream(args[1]);
            InputStream        keyIn = PGPUtil.getDecoderStream(new FileInputStream(args[2]));
                
            verifyFile(in, keyIn, args[1].substring(0, args[1].length() - 4));
        }
        else
        {
            System.err.println("usage: ClearSignedFileProcessor [-s file keyfile passPhrase]|[-v sigFile keyFile]");
        }
    }
}
