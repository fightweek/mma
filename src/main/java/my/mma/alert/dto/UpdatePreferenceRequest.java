package my.mma.alert.dto;

import my.mma.alert.constant.AlertTarget;

public record UpdatePreferenceRequest(boolean on, AlertTarget alertTarget) {
}
