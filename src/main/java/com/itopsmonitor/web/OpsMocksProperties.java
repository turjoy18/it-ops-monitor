package com.itopsmonitor.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ops.mocks")
public class OpsMocksProperties {

    /**
     * When true, {@code /mocks/ledger} returns 503 so demos can force a failure.
     */
    private boolean ledgerForceDown = true;

    public boolean isLedgerForceDown() {
        return ledgerForceDown;
    }

    public void setLedgerForceDown(boolean ledgerForceDown) {
        this.ledgerForceDown = ledgerForceDown;
    }
}
