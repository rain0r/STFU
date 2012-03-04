package org.bouncycastle2.jce.provider;

import org.bouncycastle2.jce.ProviderConfigurationPermission;
import org.bouncycastle2.jce.provider.asymmetric.ec.EC5Util;
import org.bouncycastle2.jce.interfaces.ConfigurableProvider;
import org.bouncycastle2.jce.spec.ECParameterSpec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Permission;

public class ProviderUtil
{
    private static final long  MAX_MEMORY = Runtime.getRuntime().maxMemory();

    private static Permission BC_EC_LOCAL_PERMISSION = new ProviderConfigurationPermission(
                                                   "BC2", ConfigurableProvider.THREAD_LOCAL_EC_IMPLICITLY_CA);
    private static Permission BC_EC_PERMISSION = new ProviderConfigurationPermission(
                                                   "BC2", ConfigurableProvider.EC_IMPLICITLY_CA);

    private static ThreadLocal threadSpec = new ThreadLocal();
    private static volatile ECParameterSpec ecImplicitCaParams;

    static void setParameter(String parameterName, Object parameter)
    {
        SecurityManager securityManager = System.getSecurityManager();

        if (parameterName.equals(ConfigurableProvider.THREAD_LOCAL_EC_IMPLICITLY_CA))
        {
            ECParameterSpec curveSpec;

            if (securityManager != null)
            {
                securityManager.checkPermission(BC_EC_LOCAL_PERMISSION);
            }

            if (parameter instanceof ECParameterSpec || parameter == null)
            {
                curveSpec = (ECParameterSpec)parameter;
            }
            else  // assume java.security.spec
            {
                curveSpec = EC5Util.convertSpec((java.security.spec.ECParameterSpec)parameter, false);
            }

            if (curveSpec == null)
            {
                threadSpec.remove();
            }
            else
            {
                threadSpec.set(curveSpec);
            }
        }
        else if (parameterName.equals(ConfigurableProvider.EC_IMPLICITLY_CA))
        {
            if (securityManager != null)
            {
                securityManager.checkPermission(BC_EC_PERMISSION);
            }

            if (parameter instanceof ECParameterSpec || parameter == null)
            {
                ecImplicitCaParams = (ECParameterSpec)parameter;
            }
            else  // assume java.security.spec
            {
                ecImplicitCaParams = EC5Util.convertSpec((java.security.spec.ECParameterSpec)parameter, false);
            }
        }
    }

    public static ECParameterSpec getEcImplicitlyCa()
    {
        ECParameterSpec spec = (ECParameterSpec)threadSpec.get();

        if (spec != null)
        {
            return spec;
        }

        return ecImplicitCaParams;
    }

    static int getReadLimit(InputStream in)
        throws IOException
    {
        if (in instanceof ByteArrayInputStream)
        {
            return in.available();
        }

        if (MAX_MEMORY > Integer.MAX_VALUE)
        {
            return Integer.MAX_VALUE;
        }

        return (int)MAX_MEMORY;
    }
}
