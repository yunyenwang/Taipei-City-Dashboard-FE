package com.cht.demo.util;

import java.util.LinkedList;
import java.util.List;

/**
 * 累積足夠的量，再發射！適合批次處理工作
 * 
 * @author rickwang
 *
 * @param <B>
 */
public class Silo<B> {

	final int capacity;
	final Listener<B> listener;

	LinkedList<B> clip = new LinkedList<>();

	public Silo(int capacity) {
		this.capacity = capacity;
		this.listener = (bullets) -> {
			Silo.this.onFired(bullets);
		};
	}
	
	public Silo(int capacity, Listener<B> listener) {
		this.capacity = capacity;
		this.listener = listener;
	}

	public synchronized void put(B bullet) {
		clip.add(bullet);

		if (clip.size() >= capacity) {
			flush();
		}
	}

	public synchronized void flush() {
		if (clip.isEmpty()) {
			return;
		}
		
		var bullets = clip;
		clip = new LinkedList<>();
		
		listener.onFired(bullets);
	}

	protected void onFired(List<B> bullets) {		
	}
	
	public static interface Listener<B> {
		
		void onFired(List<B> bullets); 
	}
}
