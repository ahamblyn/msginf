/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.models.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request validation errors model.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "The Validation Errors model")
public class ValidationErrors {

    @Schema(description = "The list of validation errors")
    private List<String> errors;
}
