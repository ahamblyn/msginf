package nz.co.pukekocorp.msginf.models.message;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Transaction status enum
 */
@Schema(description = "The Transaction status model")
public enum TransactionStatus {
    SUCCESS,
    FAILURE
}
