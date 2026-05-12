package com.example.digital_asset_risk_platform.common.logging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogMaskingUtils {

    public static String maskAddress(String address) {
        if (address == null || address.isBlank()) {
            return address;
        }

        if (address.length() <= 10) {
            return "****";
        }

        return address.substring(0, 6) + "..." + address.substring(address.length() - 4);
    }

    public static String maskIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return ip;
        }

        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return ip;
        }

        return parts[0] + "." + parts[1] + "." + parts[2] + ".xxx";
    }
}
