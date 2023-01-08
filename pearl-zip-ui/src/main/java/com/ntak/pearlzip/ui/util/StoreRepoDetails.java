/*
 * Copyright © 2022 92AK
 */
package com.ntak.pearlzip.ui.util;

import java.io.Serializable;

// The connection is to a publicly accessible aspect of a DB and so is expected to be permission appropriately
// for read-only access only. Hence, not considering a security issue as login is publicly known.
public record StoreRepoDetails(String name, String url, String username, String password) implements Serializable {
}
