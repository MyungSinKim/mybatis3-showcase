

package com.ly.zmn48644.mybatis.type;

import java.time.*;
import java.time.chrono.JapaneseDate;

/**
 * @since 3.4.5
 */ //@UsesJava8

public abstract class Java8TypeHandlersRegistrar {

  public static void registerDateAndTimeHandlers(TypeHandlerRegistry registry) {
    registry.register(Instant.class, InstantTypeHandler.class);
    registry.register(LocalDateTime.class, LocalDateTimeTypeHandler.class);
    registry.register(LocalDate.class, LocalDateTypeHandler.class);
    registry.register(LocalTime.class, LocalTimeTypeHandler.class);
    registry.register(OffsetDateTime.class, OffsetDateTimeTypeHandler.class);
    registry.register(OffsetTime.class, OffsetTimeTypeHandler.class);
    registry.register(ZonedDateTime.class, ZonedDateTimeTypeHandler.class);
    registry.register(Month.class, MonthTypeHandler.class);
    registry.register(Year.class, YearTypeHandler.class);
    registry.register(YearMonth.class, YearMonthTypeHandler.class);
    registry.register(JapaneseDate.class, JapaneseDateTypeHandler.class);
  }

}
