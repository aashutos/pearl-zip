/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.util;

import java.util.Comparator;
import java.util.Objects;

import static com.ntak.pearlzip.ui.constants.ResourceConstants.DSV;

public class VersionComparator implements Comparator<String> {

    private static VersionComparator instance;

    private VersionComparator() {}

    @Override
    public int compare(String left, String right) {
        try {
            String[] compLeft = DSV.split(left);
            String[] compRight = DSV.split(right);
            if (compLeft.length == 4 && compRight.length == 4) {
                for (int i = 0; i < 4; i++) {
                    int value = Integer.parseInt(compLeft[i]) - Integer.parseInt(compRight[i]);
                    if (value != 0) {
                        return value;
                    }
                }
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    public static VersionComparator getInstance() {
        if (Objects.isNull(instance)) {
            instance = new VersionComparator();
        }

        return instance;
    }
}
