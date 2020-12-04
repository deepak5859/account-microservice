package io.omnirio.accountservice.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class DateUtil {

	public static boolean isMinor(Date date) {
		Instant instant = date.toInstant();
		
		ZonedDateTime zone = instant.atZone(ZoneId.systemDefault());
		LocalDate givenDate = zone.toLocalDate();
		
		Period period = Period.between(givenDate, LocalDate.now());

		return !(period.getYears() >= 18);
	}

}
