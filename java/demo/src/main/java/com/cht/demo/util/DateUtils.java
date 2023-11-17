package com.cht.demo.util;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.chrono.MinguoDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.cht.demo.Constants;

public class DateUtils {

	public static final DateTimeFormatter standardDTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	public static final LocalDateTime newLocalDateTime() {
		return ZonedDateTime.now(Constants.TAIWAN).toLocalDateTime();
	}
	
	public static final LocalDateTime toLocalDateTime(Instant i) {		
		return ZonedDateTime
				.ofInstant(i, Constants.TAIWAN)
				.toLocalDateTime();
	}
	
	public static final LocalDate newLocalDate() {
		return ZonedDateTime.now(Constants.TAIWAN).toLocalDate();
	}
	
	public static final LocalDate toLocalDate(Instant i) {		
		return ZonedDateTime
				.ofInstant(i, Constants.TAIWAN)
				.toLocalDate();
	}
	
	public static final Instant toInstant(LocalDate ld) {
		return ld.atStartOfDay(Constants.TAIWAN).toInstant();
	}
	
	public static final Instant toInstant(LocalDateTime ldt) {
		return ldt.atZone(Constants.TAIWAN).toInstant();
	}
	
	// ======
	
	public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
		return (date.compareTo(start) >= 0) && (date.compareTo(end) <= 0);
	}
	
	public static boolean isBetween(LocalDateTime dateTime, List<LocalDateTime> compareTimes) {
		boolean isBetweened = true;
		for(LocalDateTime compareTime : compareTimes) {
			var startTime = compareTime;
			var endTime = compareTime.plusHours(1);
			if((dateTime.compareTo(startTime) >= 0) && (dateTime.compareTo(endTime) < 0)) {
				isBetweened = false;
			}
		}
		
		return isBetweened;
	}
	
	public static boolean isEqual(LocalDateTime dateTime, List<LocalDateTime> compareTimes) {
		 for (LocalDateTime compareTime : compareTimes) {
		        if (dateTime.isEqual(compareTime)) {
		            return true;
		        }
		    }
		    return false;
	}
	
	public static int betweenInDays(LocalDateTime a, LocalDateTime z) {
		return (int) Math.abs(Duration.between(a, z).toDays());
	}

	public static String toTaiwanDate(LocalDate date, String symbol) {
		return String.format("%03d%s%02d%s%02d", date.getYear() - 1911, symbol, date.getMonthValue(), symbol, date.getDayOfMonth());
	}
	
	public static LocalDate fromTaiwanDatetoLocalDate(String date, String symbol) {
		String[] s = date.split(symbol);
		int year = Integer.valueOf(s[0]) + 1911;
		int month = Integer.valueOf(s[1]);
		int day = Integer.valueOf(s[2]);
		
		return LocalDate.of(year, month, day);
	}
	
	/* Transfer AD date to minguo date.
	 * 西元年 yyyyMMdd 轉 民國年 yyyMMdd
	 *
	 * @param dateString the String dateString
	 * @return the string
	 */
	public static MinguoDate transferADDateToMinguoDate(LocalDateTime date) {
		return MinguoDate.from(date);
	}
	
	/**
	 * 取得當週,星期一的日期 
	 * @return
	 */
	public static LocalDate firstDateOfWeek(LocalDate date) {

        int daysUntilMonday = DayOfWeek.MONDAY.getValue() - date.getDayOfWeek().getValue();

        if (daysUntilMonday > 0) {
            daysUntilMonday -= 7;
        }

        // 获取本周的星期一日期
        LocalDate thisWeekMonday = date.plusDays(daysUntilMonday);
        return thisWeekMonday;
	}
}
