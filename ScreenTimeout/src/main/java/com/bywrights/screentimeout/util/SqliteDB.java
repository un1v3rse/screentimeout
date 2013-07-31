package com.bywrights.screentimeout.util;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by chris on 13-07-25.
 */
public class SqliteDB {
    private static final String
            TAG = "util.SqliteDB";
    //private static final boolean
    //    ENCRYPTION = !BuildOptions.NO_ENCRYPTION;

    public static final void init( Context context ) {
        //if (ENCRYPTION)
        //    info.guardianproject.database.sqlcipher.SQLiteDatabase.loadLibs( context );
    }

    private final String
        folder_,
        file_name_;
    private Object
        db_;

    public SqliteDB( String folder, String file_name ) {
        folder_ = folder;
        file_name_ = file_name;
    }

    private void db_close() {
//        if (ENCRYPTION)
//            ((info.guardianproject.database.sqlcipher.SQLiteDatabase)db_).close();
//        else
        ((android.database.sqlite.SQLiteDatabase)db_).close();
    }

    private int db_version() {
//        if (ENCRYPTION)
//            return ((info.guardianproject.database.sqlcipher.SQLiteDatabase)db_).getVersion();
//        else
        return ((android.database.sqlite.SQLiteDatabase)db_).getVersion();
    }

    private void db_set_version( int version ) {
//        if (ENCRYPTION)
//            ((info.guardianproject.database.sqlcipher.SQLiteDatabase)db_).setVersion( version );
//        else
        ((android.database.sqlite.SQLiteDatabase)db_).setVersion( version );
    }

    private void db_begin_transaction() {
//        if (ENCRYPTION)
//            ((info.guardianproject.database.sqlcipher.SQLiteDatabase)db_).beginTransaction();
//        else
        ((android.database.sqlite.SQLiteDatabase)db_).beginTransaction();
    }

    private void db_set_transaction_successful() {
//        if (ENCRYPTION)
//            ((info.guardianproject.database.sqlcipher.SQLiteDatabase)db_).setTransactionSuccessful();
//        else
        ((android.database.sqlite.SQLiteDatabase)db_).setTransactionSuccessful();
    }

    private void db_end_transaction() {
//        if (ENCRYPTION)
//            ((info.guardianproject.database.sqlcipher.SQLiteDatabase)db_).endTransaction();
//        else
        ((android.database.sqlite.SQLiteDatabase)db_).endTransaction();
    }

    public final Cursor db_raw_query( String sql, String[] args ) throws Exception {
//        if (ENCRYPTION)
//            return ((info.guardianproject.database.sqlcipher.SQLiteDatabase)db_).rawQuery(sql, args);
//        else
        return ((android.database.sqlite.SQLiteDatabase)db_).rawQuery(sql, args);
    }

    public final void db_exec_sql( String sql, Object[] args ) throws Exception {
//        if (ENCRYPTION)  {
//            if (args == null)
//                ((info.guardianproject.database.sqlcipher.SQLiteDatabase)db_).execSQL(sql);
//            else
//                ((info.guardianproject.database.sqlcipher.SQLiteDatabase)db_).execSQL(sql, args);
//        }
//        else {
        if (args == null)
            ((android.database.sqlite.SQLiteDatabase)db_).execSQL(sql);
        else
            ((android.database.sqlite.SQLiteDatabase)db_).execSQL(sql, args);
//        }
    }

    private int
        open_result_sets_;
    public void reset( String password ) throws Exception {
        if (db_ == null) {
            // delete the old database
            File path = new File(folder_ + file_name_);
            if (path.exists())
                path.delete();

            // open the db, this will apply the migrations
            open( password );

        } else {
            synchronized(db_) {
                if (open_result_sets_ > 0) {
                    Log.i( TAG, new StringBuilder("DB close delayed, ").append(open_result_sets_).append(" open result sets").toString() );
                    try {
                        Thread.sleep( 20 ); // give someone else a chance
                    } catch (InterruptedException e) {
                        // interrupted
                    }
                    reset( password );
                } else {
                    // only close if we haven't reopened already
                    db_close();

                    // delete the old database
                    File path = new File(folder_  + file_name_);
                    if (path.exists())
                        path.delete();

                    // open the db, this will apply the migrations
                    open( password );
                }
            }
        }
    }

    public final boolean is_open() {
        return db_ != null;
    }

    public final void open( String password ) throws Exception {
        new File( folder_ ).mkdirs();

        File path = new File(folder_ + file_name_);
//        if (ENCRYPTION)
//            db_ = info.guardianproject.database.sqlcipher.SQLiteDatabase.openDatabase(path.getAbsolutePath(), password, null, info.guardianproject.database.sqlcipher.SQLiteDatabase.OPEN_READWRITE | info.guardianproject.database.sqlcipher.SQLiteDatabase.CREATE_IF_NECESSARY );
//        else
        db_ = android.database.sqlite.SQLiteDatabase.openDatabase(path.getAbsolutePath(), null, android.database.sqlite.SQLiteDatabase.OPEN_READWRITE | android.database.sqlite.SQLiteDatabase.CREATE_IF_NECESSARY );
    }

    public final void close() {
        try {
            if (db_ != null) {
                db_close();
                db_ = null;
            }
        } catch (Exception e) {
            Log.e( TAG, "close()", e );
        }
    }

    public static final String string( Cursor cursor, int offset, String def ) {
        String result = cursor.getString( offset );
        return result == null || result.equals("") ? def : result;
    }

    public static final String utf8( Cursor cursor, int offset ) {
        // fix UTF8 encoding problem, as described at
        //  http://supportforums.blackberry.com/t5/Java-Development/UTF8-encoding-and-SQLite-database-problems/m-p/462189
        String temp = cursor.getString( offset );
        if (temp != null) {
            try {
                return new String( temp.getBytes(), "UTF-8" );
            } catch (Exception e) {
                Log.e( TAG, "utf8", e );
            }
        }
        return temp;
    }
    public static final String utf8( Cursor cursor, int offset, String def ) {
        String result = utf8( cursor, offset );
        return result == null || result.equals("") ? def : result;
    }

//    public static final Integer integerObj( Cursor cursor, int offset ) {
//        try {
//            if (row.getObject( offset ) != null)
//                return new Integer( row.getInteger( offset ) );
//        } catch (DataTypeException e) {
//            // treat as a null field
//        }
//        return null;
//    }

    public static final boolean int_as_bool( Cursor cursor, int offset ) {
        return cursor.isNull( offset ) ? false : cursor.getInt( offset ) == 1;
    }

    public static final int safe_int( Cursor cursor, int offset ) {
        if (!cursor.isNull( offset )) {
            try {
                return cursor.getInt( offset );
            } catch (Exception e) {
                // try a string interpretation
                try {
                    String s = cursor.getString( offset );
                    if (s != null)
                        return Integer.parseInt( s );
                } catch (Exception nfe) {
                    // ignore
                }
            }
        }
        return 0;
    }

    public static final Double doubleObj( Cursor cursor, int offset ) {
        return cursor.isNull( offset ) ? null : new Double( cursor.getDouble( offset ) );
    }

    public static final int color( Cursor cursor, int r_offset, int g_offset, int b_offset, int def ) {
        try {
            if ((!cursor.isNull( r_offset )) && (!cursor.isNull( g_offset )) && (!cursor.isNull( b_offset ))) {
                int
                        r = cursor.getInt( r_offset ),
                        g = cursor.getInt( g_offset ),
                        b = cursor.getInt( b_offset );
                return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
            }
        } catch (Exception e) {
            // treat as a null field
        }
        return def;
    }

    public static final Date datetime( Cursor cursor, int offset ) {
        return cursor.isNull( offset ) ? null : DateUtil.datetime_from_iso( cursor.getString( offset ) );
    }

    public static final String datetime( Date date ) {
        return DateUtil.datetime_to_iso( date );
    }

    public static final byte[] blob( Cursor cursor, int offset ) {
        return cursor.isNull( offset ) ? null : cursor.getBlob( offset );
    }

    // unused at present, when used we need to abstract SQLiteStatement to handle enrypt/non difference
//    public final SQLiteStatement prepare(String sql) throws Exception {
//    	return db_.compileStatement(sql);
//    }

    public final Cursor query(String sql) throws Exception {
        return db_raw_query(sql, null);
    }

    public final Cursor query(String sql, String[] args) throws Exception {
        return db_raw_query(sql, args);
    }

    public final int count(String table) {
        int result = 0;
        try
        {
            Cursor cursor = query("SELECT COUNT(*) FROM " + table);
            if (cursor.moveToFirst())
            {
                result = cursor.getInt( 0 );
            }
            cursor.close();
        } catch (Throwable t)
        {
            Log.e(TAG, "count( " + table + " )", t);
        }
        return result;
    }

    public final String get_ascii( String sql ) {
        String result = null;
        try {
            Cursor cursor = query( sql );
            if (cursor.moveToFirst()) {
                result = cursor.getString(0);
            }
            cursor.close();
        } catch (Throwable t) {
            Log.e( TAG, "get_ascii( " + sql + " )", t );
        }
        return result;
    }

    public final String get_utf8( String sql ) {
        String result = null;
        try {
            Cursor cursor = query( sql );
            if (cursor.moveToFirst()) {
                result = utf8(cursor, 0);
            }
            cursor.close();
        } catch (Throwable t) {
            Log.e( TAG, "get_utf8( " + sql + " )", t );
        }
        return result;
    }

    public final Double get_double( String sql ) {
        Double result = null;
        try {
            Cursor cursor = query( sql );
            if (cursor.moveToFirst()) {
                result = doubleObj(cursor, 0);
            }
            cursor.close();
        } catch (Throwable t) {
            Log.e( TAG, "get_utf8( " + sql + " )", t );
        }
        return result;
    }

    public final Date get_datetime( String sql ) {
        Date result = null;
        try {
            Cursor cursor = query( sql );
            if (cursor.moveToFirst()) {
                result = datetime(cursor, 0);
            }
            cursor.close();
        } catch (Throwable t) {
            Log.e( TAG, "get_datetime( " + sql + " )", t );
        }
        return result;
    }

    public final byte[] get_blob( String sql ) {
        byte[] result = null;
        try {
            Cursor cursor = query( sql );
            if (cursor.moveToFirst()) {
                result = cursor.getBlob(0);
            }
            cursor.close();
        } catch (Throwable t) {
            Log.e( TAG, "get_datetime( " + sql + " )", t );
        }
        return result;
    }

    public final void execute( String sql ) throws Exception {
        db_exec_sql( sql, null );
    }

    public final void execute( String sql, Object[] args ) throws Exception {
        db_exec_sql( sql, args );
    }

    public final void execute( InputStream input ) throws Exception {
        boolean
                in_escape = false;
        StringBuilder
                sql = new StringBuilder();
        int
                quote_char = 0,
                c = input.read();
        while (c != -1) {
            boolean had_escape = false;
            if (c == '\\') {
                in_escape = had_escape = true;
            } else if (c == '\'') {
                if (!in_escape) {
                    quote_char = quote_char == c ? 0 : c;
                }
            } else if (c == ';') {
                if (quote_char == 0 && !in_escape) {
                    db_exec_sql( sql.toString(), null );
                    sql = new StringBuilder();
                    c = input.read();
                    continue;
                }
            }

            sql.append( (char)c );
            c = input.read();

            if (!had_escape)
                in_escape = false;
        }
    }

    public boolean table_exists( String table_name ) {
        boolean result = false;
        Cursor cursor = null;
        try {
            cursor = query( "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = ?", new String[] { table_name } );
            if (cursor.moveToNext())
                result = true;
        }
        catch (Exception e) {
            Log.e(TAG, "table_exists", e);
        }
        finally {
            if (cursor != null) {
                try { cursor.close(); } catch (Exception e) {}
            }
        }
        return result;
    }

    public boolean column_exists( String table_name ) {
        return false;
    }

}
