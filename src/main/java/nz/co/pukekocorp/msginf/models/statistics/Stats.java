package nz.co.pukekocorp.msginf.models.statistics;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * The statistics.
 * @param systemStatsList the system stats.
 */
@Schema(description = "The Statistics model")
public record Stats(@Schema(description = "The system statistics list") List<SystemStats> systemStatsList) {
}
