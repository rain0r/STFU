package org.bouncycastle2.ocsp;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import org.bouncycastle2.asn1.ASN1InputStream;
import org.bouncycastle2.asn1.ASN1OctetString;
import org.bouncycastle2.asn1.DERInteger;
import org.bouncycastle2.asn1.DERNull;
import org.bouncycastle2.asn1.DERObjectIdentifier;
import org.bouncycastle2.asn1.DEROctetString;
import org.bouncycastle2.asn1.ocsp.CertID;
import org.bouncycastle2.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle2.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle2.jce.PrincipalUtil;
import org.bouncycastle2.jce.X509Principal;

public class CertificateID
{
    public static final String HASH_SHA1 = "1.3.14.3.2.26";

    private final CertID id;

    public CertificateID(
        CertID id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("'id' cannot be null");
        }
        this.id = id;
    }

    /**
     * create from an issuer certificate and the serial number of the
     * certificate it signed.
     *
     * @param hashAlgorithm hash algorithm to use
     * @param issuerCert issuing certificate
     * @param number serial number
     * @param provider provider to use for hashAlgorithm, null if the default one should be used.
     *
     * @exception OCSPException if any problems occur creating the id fields.
     */
    public CertificateID(
        String          hashAlgorithm,
        X509Certificate issuerCert,
        BigInteger      number,
        String          provider)
        throws OCSPException
    {
        AlgorithmIdentifier hashAlg = new AlgorithmIdentifier(
            new DERObjectIdentifier(hashAlgorithm), DERNull.INSTANCE);

        this.id = createCertID(hashAlg, issuerCert, new DERInteger(number), provider);
    }

    /**
     * create using the BC provider
     */
    public CertificateID(
        String          hashAlgorithm,
        X509Certificate issuerCert,
        BigInteger      number)
        throws OCSPException
    {
        this(hashAlgorithm, issuerCert, number, "BC2");
    }

    public String getHashAlgOID()
    {
        return id.getHashAlgorithm().getObjectId().getId();
    }

    public byte[] getIssuerNameHash()
    {
        return id.getIssuerNameHash().getOctets();
    }

    public byte[] getIssuerKeyHash()
    {
        return id.getIssuerKeyHash().getOctets();
    }

    /**
     * return the serial number for the certificate associated
     * with this request.
     */
    public BigInteger getSerialNumber()
    {
        return id.getSerialNumber().getValue();
    }

    public boolean matchesIssuer(X509Certificate issuerCert, String provider)
        throws OCSPException
    {
        return createCertID(id.getHashAlgorithm(), issuerCert, id.getSerialNumber(), provider)
            .equals(id);
    }

    public CertID toASN1Object()
    {
        return id;
    }

    public boolean equals(
        Object  o)
    {
        if (!(o instanceof CertificateID))
        {
            return false;
        }

        CertificateID   obj = (CertificateID)o;

        return id.getDERObject().equals(obj.id.getDERObject());
    }

    public int hashCode()
    {
        return id.getDERObject().hashCode();
    }

    private static CertID createCertID(AlgorithmIdentifier hashAlg, X509Certificate issuerCert,
        DERInteger serialNumber, String provider)
        throws OCSPException
    {
        try
        {
            MessageDigest digest = OCSPUtil.createDigestInstance(hashAlg.getObjectId().getId(),
                provider);

            X509Principal issuerName = PrincipalUtil.getSubjectX509Principal(issuerCert);

            digest.update(issuerName.getEncoded());

            ASN1OctetString issuerNameHash = new DEROctetString(digest.digest());
            PublicKey issuerKey = issuerCert.getPublicKey();

            ASN1InputStream aIn = new ASN1InputStream(issuerKey.getEncoded());
            SubjectPublicKeyInfo info = SubjectPublicKeyInfo.getInstance(aIn.readObject());

            digest.update(info.getPublicKeyData().getBytes());

            ASN1OctetString issuerKeyHash = new DEROctetString(digest.digest());

            return new CertID(hashAlg, issuerNameHash, issuerKeyHash, serialNumber);
        }
        catch (Exception e)
        {
            throw new OCSPException("problem creating ID: " + e, e);
        }
    }
}
