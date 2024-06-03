/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.models.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Register role model.
 */
@Getter
@NoArgsConstructor
@Schema(description = "The Register Role model")
public class RegisterRole {
    @Schema(description = "The role name")
    private String name;
}
