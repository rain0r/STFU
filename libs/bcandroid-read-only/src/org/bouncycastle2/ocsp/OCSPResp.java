package org.bouncycastle2.ocsp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bouncycastle2.asn1.ASN1InputStream;
import org.bouncycastle2.asn1.ASN1OutputStream;
import org.bouncycastle2.asn1.ocsp.BasicOCSPResponse;
import org.bouncycastle2.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle2.asn1.ocsp.OCSPResponse;
import org.bouncycastle2.asn1.ocsp.ResponseBytes;

public class OCSPResp
{
    private OCSPResponse    resp;

    public OCSPResp(
        OCSPResponse    resp)
    {
        this.resp = resp;
    }

    public OCSPResp(
        byte[]          resp)
        throws IOException
    {
        this(new ASN1InputStream(resp));
    }

    public OCSPResp(
        InputStream     in)
        throws IOException
    {
        this(new ASN1InputStream(in));
    }

    private OCSPResp(
        ASN1InputStream aIn)
        throws IOException
    {
        try
        {
            this.resp = OCSPResponse.getInstance(aIn.readObject());
        }
        catch (IllegalArgumentException e)
        {
            throw new IOException("malformed response: " + e.getMessage());
        }
        catch (ClassCastException e)
        {
            throw new IOException("malformed response: " + e.getMessage());
        }
    }

    public int getStatus()
    {
        return this.resp.getResponseStatus().getValue().intValue();
    }

    public Object getResponseObject()
        throws OCSPException
    {
        ResponseBytes   rb = this.resp.getResponseBytes();

        if (rb == null)
        {
            return null;
        }

        if (rb.getResponseType().equals(OCSPObjectIdentifiers.id_pkix_ocsp_basic))
        {
            try
            {
                ASN1InputStream aIn = new ASN1InputStream(rb.getResponse().getOctets());
                return new BasicOCSPResp(
                            BasicOCSPResponse.getInstance(aIn.readObject()));
            }
            catch (Exception e)
            {
                throw new OCSPException("problem decoding object: " + e, e);
            }
        }

        return rb.getResponse();
    }

    /**
     * return the ASN.1 encoded representation of this object.
     */
    public byte[] getEncoded()
        throws IOException
    {
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        ASN1OutputStream        aOut = new ASN1OutputStream(bOut);

        aOut.writeObject(resp);

        return bOut.toByteArray();
    }
    
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }
        
        if (!(o instanceof OCSPResp))
        {
            return false;
        }
        
        OCSPResp r = (OCSPResp)o;
        
        return resp.equals(r.resp);
    }
    
    public int hashCode()
    {
        return resp.hashCode();
    }
}
