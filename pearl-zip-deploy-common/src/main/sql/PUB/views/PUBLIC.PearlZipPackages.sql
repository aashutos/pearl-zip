/*
 * Copyright © 2022 92AK
 */

-- DEFINITION
CREATE VIEW public.PearlZipPackages AS
SELECT pe.ID,
       PackageName,
       packageURL,
       packageHash,
       description,
       minVersionId,
       min.version AS minVersion,
       maxVersionId,
       max.version AS maxVersion,
       typeName,
       providerName,
       about
FROM DBO.PEARLZIPPACKAGEENTRY pe
INNER JOIN DBO.PEARLZIPPACKAGEPROVIDER pr
ON pe.PROVIDERID  = pr.id
INNER JOIN DBO.PEARLZIPPACKAGETYPE pt
ON pe.PACKAGETYPEID  = pt.id
INNER JOIN DBO.PEARLZIPVERSION min
ON pe.MINVERSIONID  = min.id
INNER JOIN DBO.PEARLZIPVERSION max
ON pe.MAXVERSIONID  = max.id
ORDER BY pe.ID

-- PERMISSIONS
GRANT SELECT ON public.PearlZipPackages TO pub;
GRANT SELECT ON public.PearlZipPackages TO app;