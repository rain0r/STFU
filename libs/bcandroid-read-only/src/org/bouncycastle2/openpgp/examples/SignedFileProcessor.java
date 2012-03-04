package org.bouncycastle2.openpgp.examples;

import java.io.File;
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

import org.bouncycastle2.bcpg.ArmoredOutputStream;
import org.bouncycastle2.bcpg.BCPGOutputStream;

import org.bouncycastle2.jce.provider.BouncyCastleProvider;
import org.bouncycastle2.openpgp.PGPException;
import org.bouncycastle2.openpgp.PGPLiteralData;
import org.bouncycastle2.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle2.openpgp.PGPCompressedData;
import org.bouncycastle2.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle2.openpgp.PGPObjectFactory;
import org.bouncycastle2.openpgp.PGPOnePassSignature;
import org.bouncycastle2.openpgp.PGPOnePassSignatureList;
import org.bouncycastle2.openpgp.PGPPrivateKey;
import org.bouncycastle2.openpgp.PGPPublicKey;
import org.bouncycastle2.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle2.openpgp.PGPSecretKey;
import org.bouncycastle2.openpgp.PGPSecretKeyRing;
import org.bouncycastle2.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle2.openpgp.PGPSignature;
import org.bouncycastle2.openpgp.PGPSignatureGenerator;
import org.bouncycastle2.openpgp.PGPSignatureList;
import org.bouncycastle2.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle2.openpgp.PGPUtil;

/**
 * A simple utility class that signs and verifies files.
 * <p>
 * To sign a file: SignedFileProcessor -s [-a] fileName secretKey passPhrase.<br>
 * If -a is specified the output file will be "ascii-armored".
 * <p>
 * To decrypt: SignedFileProcessor -v fileName publicKeyFile.
 * <p>
 * <b>Note</b>: this example will silently overwrite files, nor does it pay any attention to
 * the specification of "_CONSOLE" in the filename. It also expects that a single pass phrase
 * will have been used.
 * <p>
 * <b>Note</b>: the example also makes use of PGP compression. If you are having difficulty getting it
 * to interoperate with other PGP programs try removing the use of compression first.
 */
public class SignedFileProcessor
{
    /**
     * A simple routine that opens a key ring file and loads the first available key suitable for
     * signature generation.
     * 
     * @param in
     * @return
     * @throws IOException
     * @throws PGPException
     */
    private static PGPSecretKey readSecretKey(
        InputStream    in)
        throws IOException, PGPException
    {
        in = PGPUtil.getDecoderStream(in);
        
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
            Iterator            kIt = kRing.getSecretKeys();
            
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
    
    /**
     * verify the passed in file as being correctly signed.
     */
    private static void verifyFile(
        InputStream        in,
        InputStream        keyIn)
        throws Exception
    {
        in = PGPUtil.getDecoderStream(in);
        
        PGPObjectFactory            pgpFact = new PGPObjectFactory(in);

        PGPCompressedData           c1 = (PGPCompressedData)pgpFact.nextObject();

        pgpFact = new PGPObjectFactory(c1.getDataStream());
            
        PGPOnePassSignatureList     p1 = (PGPOnePassSignatureList)pgpFact.nextObject();
            
        PGPOnePassSignature         ops = p1.get(0);
            
        PGPLiteralData              p2 = (PGPLiteralData)pgpFact.nextObject();

        InputStream                 dIn = p2.getInputStream();
        int                         ch;
        PGPPublicKeyRingCollection  pgpRing = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(keyIn));

        PGPPublicKey                key = pgpRing.getPublicKey(ops.getKeyID());
        FileOutputStream            out = new FileOutputStream(p2.getFileName());

        ops.initVerify(key, "BC2");
            
        while ((ch = dIn.read()) >= 0)
        {
            ops.update((byte)ch);
            out.write(ch);
        }

        out.close();
        
        PGPSignatureList            p3 = (PGPSignatureList)pgpFact.nextObject();

        if (ops.verify(p3.get(0)))
        {
            System.out.println("signature verified.");
        }
        else
        {
            System.out.println("signature verification failed.");
        }
    }

    /**
     * Generate an encapsulated signed file.
     * 
     * @param fileName
     * @param keyIn
     * @param out
     * @param pass
     * @param armor
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws PGPException
     * @throws SignatureException
     */
    private static void signFile(
        String          fileName,
        InputStream     keyIn,
        OutputStream    out,
        char[]          pass,
        boolean         armor)
        throws IOException, NoSuchAlgorithmException, NoSuchProviderException, PGPException, SignatureException
    {    
        if (armor)
        {
            out = new ArmoredOutputStream(out);
        }
        
        PGPSecretKey                pgpSec = readSecretKey(keyIn);
        PGPPrivateKey               pgpPrivKey = pgpSec.extractPrivateKey(pass, "BC2");        
        PGPSignatureGenerator       sGen = new PGPSignatureGenerator(pgpSec.getPublicKey().getAlgorithm(), PGPUtil.SHA1, "BC2");
        
        sGen.initSign(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);
        
        Iterator    it = pgpSec.getPublicKey().getUserIDs();
        if (it.hasNext())
        {
            PGPSignatureSubpacketGenerator  spGen = new PGPSignatureSubpacketGenerator();
            
            spGen.setSignerUserID(false, (String)it.next());
            sGen.setHashedSubpackets(spGen.generate());
        }
        
        PGPCompressedDataGenerator  cGen = new PGPCompressedDataGenerator(
                                                                PGPCompressedData.ZLIB);
        
        BCPGOutputStream            bOut = new BCPGOutputStream(cGen.open(out));
        
        sGen.generateOnePassVersion(false).encode(bOut);
        
        File                        file = new File(fileName);
        PGPLiteralDataGenerator     lGen = new PGPLiteralDataGenerator();
        OutputStream                lOut = lGen.open(bOut, PGPLiteralData.BINARY, file);
        FileInputStream             fIn = new FileInputStream(file);
        int                         ch = 0;
        
        while ((ch = fIn.read()) >= 0)
        {
            lOut.write(ch);
            sGen.update((byte)ch);
        }
        
        lGen.close();
        
        sGen.generate().encode(bOut);
        
        cGen.close();
        
        out.close();
    }

    public static void main(
        String[] args)
        throws Exception
    {
        Security.addProvider(new BouncyCastleProvider());

        if (args[0].equals("-s"))
        {
            if (args[1].equals("-a"))
            {
                FileInputStream     keyIn = new FileInputStream(args[3]);
                FileOutputStream    out = new FileOutputStream(args[2] + ".asc");
                
                signFile(args[2], keyIn, out, args[4].toCharArray(), true);
            }
            else
            {
                FileInputStream     keyIn = new FileInputStream(args[2]);
                FileOutputStream    out = new FileOutputStream(args[1] + ".bpg");
                
                signFile(args[1], keyIn, out, args[3].toCharArray(), false);
            }
        }
        else if (args[0].equals("-v"))
        {
            FileInputStream    in = new FileInputStream(args[1]);
            FileInputStream    keyIn = new FileInputStream(args[2]);
            
            verifyFile(in, keyIn);
        }
        else
        {
            System.err.println("usage: SignedFileProcessor -v|-s [-a] file keyfile [passPhrase]");
        }
    }
}