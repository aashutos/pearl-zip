/*
 * Copyright © 2022 92AK
 */

-- DECLARATION
CREATE TABLE DBO.PearlZipPackageEntry(
      id INT GENERATED BY DEFAULT AS IDENTITY,
      PackageName VARCHAR(255) NOT NULL,
      packageURL VARCHAR(2000) NOT NULL,
      packageHash VARCHAR(2000) NOT NULL,
      description VARCHAR(2000) NOT NULL,
      minVersionId INT NOT NULL REFERENCES DBO.PearlZipVersion(id),
      maxVersionId INT NOT NULL REFERENCES DBO.PearlZipVersion(id),
      packageTypeId INT NOT NULL REFERENCES DBO.PearlZipPackageType(id),
      providerId INT NOT NULL REFERENCES DBO.PearlZipPackageProvider(id),
      PRIMARY KEY(PackageName)
);
-- PERMISSIONS
GRANT SELECT ON DBO.PearlZipPackageEntry TO PUB;
