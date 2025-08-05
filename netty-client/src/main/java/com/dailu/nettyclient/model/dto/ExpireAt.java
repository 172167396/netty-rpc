package com.dailu.nettyclient.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
public class ExpireAt {

    private long unit;

    private ChronoUnit timeUnit;

    private final LocalDateTime createTime = LocalDateTime.now();

    public static ExpireAt defaultExpire() {
        return of(2, ChronoUnit.MINUTES);
    }

    public static ExpireAt of(long unit, ChronoUnit timeUnit) {
        negativeCheck(unit);
        Assert.notNull(timeUnit, "timeUnit cannot be null");
        ExpireAt expireAt = new ExpireAt();
        expireAt.setUnit(unit);
        expireAt.setTimeUnit(timeUnit);
        return expireAt;
    }

    public static ExpireAt ofSeconds(long seconds) {
        negativeCheck(seconds);
        return of(seconds, ChronoUnit.SECONDS);
    }

    public static ExpireAt ofMinutes(long minutes) {
        negativeCheck(minutes);
        return of(minutes, ChronoUnit.MINUTES);
    }

    public static ExpireAt ofHours(long hours) {
        negativeCheck(hours);
        return of(hours, ChronoUnit.HOURS);
    }

    private static void negativeCheck(long unit) {
        if (unit < 0) {
            throw new IllegalArgumentException("unit cannot be negative");
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(createTime.plus(unit, timeUnit));
    }

    public long getTimeout(ChronoUnit targetUnit) {
        return createTime.plus(unit, timeUnit).until(LocalDateTime.now(), targetUnit);
    }
}
