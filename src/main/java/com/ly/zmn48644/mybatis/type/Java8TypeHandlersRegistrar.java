/**
 *    Copyright 2009-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.ly.zmn48644.mybatis.type;

import org.apache.ibatis.lang.UsesJava8;

import java.time.*;
import java.time.chrono.JapaneseDate;

/**
 * @since 3.4.5
 */
@UsesJava8
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
