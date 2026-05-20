package com.example.digital_asset_risk_platform.kyt.dto;

public record KytLookupRequest(
        String chainType,
        String address
) {
}
