package com.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HistoryEvent {
    private int eventId;
    private int userId;
    private String eventType;
    private String operation;
    private int entityId;
    private int timestamp;
}
