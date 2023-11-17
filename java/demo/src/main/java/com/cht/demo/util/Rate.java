package com.cht.demo.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * 採移動平均方式，計算每秒鐘的數據量，例如評估最近一分鐘每秒塞入資料庫的筆數。
 * 
 * @author rickwang
 */

public class Rate {

	final static long MILLIS_PER_SECOND = 1000L;

	long counter = 0L;

	final long intervalInSeconds; // seconds

	LinkedList<Slot> slots = new LinkedList<>(); // XXX - using ring buffer should be better

	long total = 0L;

	/**
	 * @param intervalInSeconds 時間區間，單位為秒
	 */
	public Rate(long intervalInSeconds) {
		this.intervalInSeconds = intervalInSeconds;
	}
	
	public Rate(long duration, TimeUnit unit) {
		this(unit.toSeconds(duration));
	}

	public Rate flush(long timestamp) {
		increase(timestamp, 0);
		return this;
	}

	/**
	 * 取得在 intervalInSeconds 時間內的累計數據量
	 * @return
	 */
	public long getCounter() {
		return counter;
	}

	/**
	 * 取得在 intervalInSeconds 時間內，每秒的數據量
	 * @return
	 */
	public synchronized long getCounterPerSecond() {
		if (slots.isEmpty()) { // not enough
			return 0L;
		}

		Slot s = slots.getFirst();

		if (slots.size() == 1) { // just one slot
			return s.counter;
		}

		Slot e = slots.getLast();

		long elapse = (e.seconds - s.seconds) + 1L;
		if (elapse <= 0) {	// XXX - impossible
			return 0L;
		}

		return counter / elapse;
	}

	public long getIntervalInSeconds() {
		return intervalInSeconds;
	}

	public long getTotal() {
		return total;
	}

	/**
	 * 累加數據量
	 * @param timestamp 時間 (ms)
	 * @param count 此時間發生的數據量
	 * @return
	 */
	public synchronized long increase(long timestamp, long count) {
		long seconds = timestamp / MILLIS_PER_SECOND; // just want the tick in second

		total += count;
		counter += count;

		Iterator<Slot> it = slots.iterator(); // from the last one to the recent one
		while (it.hasNext()) {
			Slot s = it.next();
			if (s.seconds == seconds) { // same time-slot
				s.increase(count);
				return counter;

			} else if ((seconds - s.seconds) >= intervalInSeconds) {
				counter -= s.counter; // decrease the expired record
				it.remove(); // expired
			}
		}

		// new time-slot
		Slot s = new Slot(seconds, count);
		slots.add(s);

		return counter;
	}

	static class Slot {
		long counter = 0;
		final long seconds;	// timeslot in second

		Slot(long seconds, long count) {
			this.seconds = seconds;
			this.counter += count;
		}

		void increase(long count) {
			counter += count;
		}
	}
}
