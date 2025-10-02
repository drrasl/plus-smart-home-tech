package ru.yandex.practicum.telemetry.collector.model.sensor;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class SwitchSensorEvent extends SensorEvent {
    @Builder.Default
    private boolean state = false;

    @Override
    public SensorEventType getType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }
}
