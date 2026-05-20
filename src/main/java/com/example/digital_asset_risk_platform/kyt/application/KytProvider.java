package com.example.digital_asset_risk_platform.kyt.application;

import com.example.digital_asset_risk_platform.kyt.dto.KytLookupRequest;
import com.example.digital_asset_risk_platform.kyt.dto.KytLookupResult;

public interface KytProvider {

    KytLookupResult lookup(KytLookupRequest request);

    String providerName();
}
