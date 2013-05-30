package com.aprendeandroid.sqlitealbum;

import android.database.Cursor;

public class Hoja {

	private long id;
	private String title;
	private String comment;
	private String image;
	private long lastTime;

	public Hoja(long id, long lastTime, String comment, String comments, String image) {
		this.id = id;
		this.title = comment;
		this.comment =  comments;
		this.image =  image;
		this.lastTime = lastTime;
	}

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String comment) {
		this.title = comment;
	}
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}
	
	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	public static Hoja cursorToHoja(Cursor cursor) {
		Hoja hoja = new Hoja(cursor.getLong(0), cursor.getLong(1),
				cursor.getString(2), cursor.getString(3), cursor.getString(4));
		return hoja;
	}
}
