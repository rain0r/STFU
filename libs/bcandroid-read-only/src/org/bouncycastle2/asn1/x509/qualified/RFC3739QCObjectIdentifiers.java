package org.bouncycastle2.asn1.x509.qualified;

import org.bouncycastle2.asn1.DERObjectIdentifier;

public interface RFC3739QCObjectIdentifiers
{
    //
    // base id
    //
    static final String                 id_qcs             = "1.3.6.1.5.5.7.11";

    static final DERObjectIdentifier    id_qcs_pkixQCSyntax_v1                = new DERObjectIdentifier(id_qcs+".1");
    static final DERObjectIdentifier    id_qcs_pkixQCSyntax_v2                 = new DERObjectIdentifier(id_qcs+".2");
}
