package ru.yandex.practicum.telemetry.collector.service.handler.hub.mapper;

import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.List;
import java.util.stream.Collectors;

public class ClassToAvroMapper {

    public static List<ScenarioConditionAvro> mapConditionsToAvro(List<ScenarioConditionProto> conditions) {
        return conditions.stream()
                .map(ClassToAvroMapper::mapConditionToAvro)
                .collect(Collectors.toList());
    }

    private static ScenarioConditionAvro mapConditionToAvro(ScenarioConditionProto condition) {
        ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()));

        // Обработка oneof value
        if (condition.hasBoolValue()) {
            builder.setValue(condition.getBoolValue());  // boolean
        } else if (condition.hasIntValue()) {
            builder.setValue(condition.getIntValue());   // int
        } else {
            builder.setValue(null);  // VALUE_NOT_SET
        }

        return builder.build();
    }

    public static List<DeviceActionAvro> mapActionsToAvro(List<DeviceActionProto> actions) {
        return actions.stream()
                .map(ClassToAvroMapper::mapActionToAvro)
                .collect(Collectors.toList());
    }

    private static DeviceActionAvro mapActionToAvro(DeviceActionProto action) {
        DeviceActionAvro.Builder builder = DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()));

        // Для union {null, int} - явно передаем null когда значение не нужно
        if (action.getValue() != 0) {
            builder.setValue(action.getValue());  // int значение
        } else {
            builder.setValue(null);  // null из union
        }

        return builder.build();
    }
}
