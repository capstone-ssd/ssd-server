package or.hyu.ssd.domain.document.controller.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateCheckListRequest(
        @NotNull Boolean checked
) {}

