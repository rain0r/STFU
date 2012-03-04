package org.bouncycastle2.jce;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.X509CRL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.SimpleTimeZone;
import java.util.Vector;

import org.bouncycastle2.asn1.ASN1EncodableVector;
import org.bouncycastle2.asn1.DERBitString;
import org.bouncycastle2.asn1.DEREncodable;
import org.bouncycastle2.asn1.DERInteger;
import org.bouncycastle2.asn1.DERObjectIdentifier;
import org.bouncycastle2.asn1.DEROctetString;
import org.bouncycastle2.asn1.DEROutputStream;
import org.bouncycastle2.asn1.DERSequence;
import org.bouncycastle2.asn1.DERUTCTime;
import org.bouncycastle2.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle2.asn1.x509.CertificateList;
import org.bouncycastle2.asn1.x509.TBSCertList;
import org.bouncycastle2.asn1.x509.V2TBSCertListGenerator;
import org.bouncycastle2.asn1.x509.X509Extension;
import org.bouncycastle2.asn1.x509.X509Extensions;
import org.bouncycastle2.asn1.x509.X509Name;
import org.bouncycastle2.jce.provider.X509CRLObject;
import org.bouncycastle2.util.Strings;

/**
 * class to produce an X.509 Version 2 CRL.
 * <p>
 * @deprecated use the equivalent class in org.bouncycastle2.x509
 */
public class X509V2CRLGenerator
{
    private SimpleDateFormat            dateF = new SimpleDateFormat("yyMMddHHmmss");
    private SimpleTimeZone              tz = new SimpleTimeZone(0, "Z");
    private V2TBSCertListGenerator      tbsGen;
    private DERObjectIdentifier         sigOID;
    private AlgorithmIdentifier         sigAlgId;
    private String                      signatureAlgorithm;
    private Hashtable                   extensions = null;
    private Vector                      extOrdering = null;

    private static Hashtable            algorithms = new Hashtable();

    static
    {
        algorithms.put("MD2WITHRSAENCRYPTION", new DERObjectIdentifier("1.2.840.113549.1.1.2"));
        algorithms.put("MD2WITHRSA", new DERObjectIdentifier("1.2.840.113549.1.1.2"));
        algorithms.put("MD5WITHRSAENCRYPTION", new DERObjectIdentifier("1.2.840.113549.1.1.4"));
        algorithms.put("MD5WITHRSA", new DERObjectIdentifier("1.2.840.113549.1.1.4"));
        algorithms.put("SHA1WITHRSAENCRYPTION", new DERObjectIdentifier("1.2.840.113549.1.1.5"));
        algorithms.put("SHA1WITHRSA", new DERObjectIdentifier("1.2.840.113549.1.1.5"));
        algorithms.put("RIPEMD160WITHRSAENCRYPTION", new DERObjectIdentifier("1.3.36.3.3.1.2"));
        algorithms.put("RIPEMD160WITHRSA", new DERObjectIdentifier("1.3.36.3.3.1.2"));
        algorithms.put("SHA1WITHDSA", new DERObjectIdentifier("1.2.840.10040.4.3"));
        algorithms.put("DSAWITHSHA1", new DERObjectIdentifier("1.2.840.10040.4.3"));
        algorithms.put("SHA1WITHECDSA", new DERObjectIdentifier("1.2.840.10045.4.1"));
        algorithms.put("ECDSAWITHSHA1", new DERObjectIdentifier("1.2.840.10045.4.1"));
    }

    public X509V2CRLGenerator()
    {
        dateF.setTimeZone(tz);

        tbsGen = new V2TBSCertListGenerator();
    }

    /**
     * reset the generator
     */
    public void reset()
    {
        tbsGen = new V2TBSCertListGenerator();
    }


    /**
     * Set the issuer distinguished name - the issuer is the entity whose private key is used to sign the
     * certificate.
     */
    public void setIssuerDN(
        X509Name   issuer)
    {
        tbsGen.setIssuer(issuer);
    }

    public void setThisUpdate(
        Date    date)
    {
        tbsGen.setThisUpdate(new DERUTCTime(dateF.format(date) + "Z"));
    }

    public void setNextUpdate(
        Date    date)
    {
        tbsGen.setNextUpdate(new DERUTCTime(dateF.format(date) + "Z"));
    }

    /**
     * Reason being as indicated by CRLReason, i.e. CRLReason.KEY_COMPROMISE
     * or 0 if CRLReason are not to be used
     **/
    public void addCRLEntry(BigInteger userCertificate, Date revocationDate, int reason)
    {
        tbsGen.addCRLEntry(new DERInteger(userCertificate), new DERUTCTime(dateF.format(revocationDate) + "Z"), reason);
    }

    public void setSignatureAlgorithm(
        String  signatureAlgorithm)
    {
        this.signatureAlgorithm = signatureAlgorithm;

        sigOID = (DERObjectIdentifier)algorithms.get(Strings.toUpperCase(signatureAlgorithm));

        if (sigOID == null)
        {
            throw new IllegalArgumentException("Unknown signature type requested");
        }

        sigAlgId = new AlgorithmIdentifier(this.sigOID, null);

        tbsGen.setSignature(sigAlgId);
    }

    /**
     * add a given extension field for the standard extensions tag (tag 3)
     */
    public void addExtension(
        String          OID,
        boolean         critical,
        DEREncodable    value)
    {
        this.addExtension(new DERObjectIdentifier(OID), critical, value);
    }

    /**
     * add a given extension field for the standard extensions tag (tag 0)
     */
    public void addExtension(
        DERObjectIdentifier OID,
        boolean             critical,
        DEREncodable        value)
    {
        if (extensions == null)
        {
            extensions = new Hashtable();
            extOrdering = new Vector();
        }

        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        DEROutputStream         dOut = new DEROutputStream(bOut);

        try
        {
            dOut.writeObject(value);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("error encoding value: " + e);
        }

        this.addExtension(OID, critical, bOut.toByteArray());
    }

    /**
     * add a given extension field for the standard extensions tag (tag 0)
     */
    public void addExtension(
        String          OID,
        boolean         critical,
        byte[]          value)
    {
        this.addExtension(new DERObjectIdentifier(OID), critical, value);
    }

    /**
     * add a given extension field for the standard extensions tag (tag 0)
     */
    public void addExtension(
        DERObjectIdentifier OID,
        boolean             critical,
        byte[]              value)
    {
        if (extensions == null)
        {
            extensions = new Hashtable();
            extOrdering = new Vector();
        }

        extensions.put(OID, new X509Extension(critical, new DEROctetString(value)));
        extOrdering.addElement(OID);
    }

    /**
     * generate an X509 CRL, based on the current issuer and subject
     * using the default provider "BC2".
     */
    public X509CRL generateX509CRL(
        PrivateKey      key)
        throws SecurityException, SignatureException, InvalidKeyException
    {
        try
        {
            return generateX509CRL(key, "BC2", null);
        }
        catch (NoSuchProviderException e)
        {
            throw new SecurityException("BC provider not installed!");
        }
    }

    /**
     * generate an X509 CRL, based on the current issuer and subject
     * using the default provider "BC2" and an user defined SecureRandom object as
     * source of randomness.
     */
    public X509CRL generateX509CRL(
        PrivateKey      key,
        SecureRandom    random)
        throws SecurityException, SignatureException, InvalidKeyException
    {
        try
        {
            return generateX509CRL(key, "BC2", random);
        }
        catch (NoSuchProviderException e)
        {
            throw new SecurityException("BC provider not installed!");
        }
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject
     * using the passed in provider for the signing.
     */
    public X509CRL generateX509CRL(
        PrivateKey      key,
        String          provider)
        throws NoSuchProviderException, SecurityException, SignatureException, InvalidKeyException
    {
        return generateX509CRL(key, provider, null);
    }

    /**
     * generate an X509 CRL, based on the current issuer and subject,
     * using the passed in provider for the signing.
     */
    public X509CRL generateX509CRL(
        PrivateKey      key,
        String          provider,
        SecureRandom    random)
        throws NoSuchProviderException, SecurityException, SignatureException, InvalidKeyException
    {
        Signature sig = null;

        try
        {
            sig = Signature.getInstance(sigOID.getId(), provider);
        }
        catch (NoSuchAlgorithmException ex)
        {
            try
            {
                sig = Signature.getInstance(signatureAlgorithm, provider);
            }
            catch (NoSuchAlgorithmException e)
            {
                throw new SecurityException("exception creating signature: " + e.toString());
            }
        }

        if (random != null)
        {
            sig.initSign(key, random);
        }
        else
        {
            sig.initSign(key);
        }

        if (extensions != null)
        {
            tbsGen.setExtensions(new X509Extensions(extOrdering, extensions));
        }

        TBSCertList tbsCrl = tbsGen.generateTBSCertList();

        try
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);

            dOut.writeObject(tbsCrl);

            sig.update(bOut.toByteArray());
        }
        catch (Exception e)
        {
            throw new SecurityException("exception encoding TBS cert - " + e);
        }

        // Construct the CRL
        ASN1EncodableVector  v = new ASN1EncodableVector();

        v.add(tbsCrl);
        v.add(sigAlgId);
        v.add(new DERBitString(sig.sign()));

        try
        {
            return new X509CRLObject(new CertificateList(new DERSequence(v)));
        }
        catch (CRLException e)
        {
            throw new IllegalStateException("attempt to create malformed CRL: " + e.getMessage());
        }
    }
}
