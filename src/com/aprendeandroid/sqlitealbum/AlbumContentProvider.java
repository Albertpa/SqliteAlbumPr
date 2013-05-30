package com.aprendeandroid.sqlitealbum;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class AlbumContentProvider extends ContentProvider {
	
	//obligado para conectar con un ContentProvider
	//da permisos de uso, el paquete de la aplicacion
    public static final String AUTHORITY = "com.aprendeandroid.sqlitealbum";
    //direccion de la base de datos 
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/albumitems");
	
	
	
	public static final String COLUMN_ID = MySQLiteHelper.COLUMN_ID;
	public static final String COLUMN_TITLE = MySQLiteHelper.COLUMN_TITLE;
	public static final String COLUMN_COMMENT = MySQLiteHelper.COLUMN_COMMENT;
	public static final String COLUMN_IMAGE = MySQLiteHelper.COLUMN_IMAGE;
	public static final String COLUMN_LAST_TIME = MySQLiteHelper.COLUMN_LAST_TIME;

	public static final String[] PROJECTION = MySQLiteHelper.PROJECTION;
    public static final String DEFAULT_SORT_ORDER = MySQLiteHelper.DEFAULT_SORT_ORDER;
    public static final String TITLES_SORT_ORDER = MySQLiteHelper.TITLES_SORT_ORDER;

	private MySQLiteHelper myOpenHelper;

	private static final int ALLROWS = 1;
	private static final int SINGLE_ROW = 2;

	private static final UriMatcher uriMatcher;
	
	
	
	
	@Override
	public boolean onCreate() {
		// Construct the underlying database.
		// Defer opening the database until you need to perform
		// a query or transaction.
		myOpenHelper = new MySQLiteHelper(getContext());

		return true;
	}

	

	// Populate the UriMatcher object, where a URI ending in 'albumitems' will
	// correspond to a request for all items, and 'albumitems/[rowID]'
	// represents a single row.
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("com.aprendeandroid.sqlitealbum", "albumitems",ALLROWS);
		uriMatcher.addURI("com.aprendeandroid.sqlitealbum", "albumitems/#",SINGLE_ROW);//solo 1
	}

	
	
	//Devuelve el MIMETYPE (por ejemplo mediaSore seria JPG)
	@Override
	public String getType(Uri uri) {
		// Return a string that identifies the MIME type
		// for a Content Provider URI
		switch (uriMatcher.match(uri)) {
		case ALLROWS://da la direccion del directorio
			return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.aprendeandroid.album";
		case SINGLE_ROW://da la direccion del item
			return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.aprendeandroid.album";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// Open a read-only database.
		SQLiteDatabase db = myOpenHelper.getWritableDatabase();

		// Replace these with valid SQL statements if necessary.
		String groupBy = null;
		String having = null;

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(MySQLiteHelper.DATABASE_TABLE);

		// If this is a row query, limit the result set to the passed in row.
		switch (uriMatcher.match(uri)) {
		case SINGLE_ROW: //solo mira si le viene con 1 columna
			String rowID = uri.getPathSegments().get(1);
			queryBuilder.appendWhere(COLUMN_ID + "=" + rowID);
		default:
			break;
		}
		//query
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, groupBy, having, sortOrder);

		return cursor;
	}

	
	
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// Open a read / write database to support the transaction.
		SQLiteDatabase db = myOpenHelper.getWritableDatabase();

		// If this is a row URI, limit the deletion to the specified row.
		switch (uriMatcher.match(uri)) {
		case SINGLE_ROW:
			String rowID = uri.getPathSegments().get(1);
			selection = COLUMN_ID + "=" + rowID + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
		default:
			break;
		}

		// To return the number of deleted items, you must specify a where
		// clause. To delete all rows and return a value, pass in "1".
		if (selection == null)
			selection = "1";

		// Execute the deletion.
		int deleteCount = db.delete(MySQLiteHelper.DATABASE_TABLE, selection,
				selectionArgs);

		// Notify any observers of the change in the data set.
		getContext().getContentResolver().notifyChange(uri, null);

		return deleteCount;
	}

	
	
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Open a read / write database to support the transaction.
		SQLiteDatabase db = myOpenHelper.getWritableDatabase();
		String nullColumnHack = null;
		values.put(COLUMN_LAST_TIME, System.currentTimeMillis());

		// Insert the values into the table
		long id = db.insert(MySQLiteHelper.DATABASE_TABLE, nullColumnHack,values);

		//Si se ha producido un error, devuelve -1
		if (id > -1) {
			// Construct and return the URI of the newly inserted row.
			Uri insertedId = ContentUris.withAppendedId(CONTENT_URI, id);

			// Notify any observers of the change in the data set.
			getContext().getContentResolver().notifyChange(insertedId, null);

			return insertedId;
		} else
			return null;
	}

	
	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		// Open a read / write database to support the transaction.
		SQLiteDatabase db = myOpenHelper.getWritableDatabase();

		//EN esta ocasion miramos si se esta pidiendo 1 o mas 
		switch (uriMatcher.match(uri)) {
		case SINGLE_ROW:
			String rowID = uri.getPathSegments().get(1);
			selection = COLUMN_ID + "=" + rowID + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
		default:
			break;
		}

		values.put(COLUMN_LAST_TIME, System.currentTimeMillis());
		// Perform the update.
		int updateCount = db.update(MySQLiteHelper.DATABASE_TABLE, values,
				selection, selectionArgs);

		// Notify any observers of the change in the data set.
		getContext().getContentResolver().notifyChange(uri, null);

		return updateCount;
	}

}
