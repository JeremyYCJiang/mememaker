package com.teamtreehouse.mememaker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.teamtreehouse.mememaker.models.Meme;
import com.teamtreehouse.mememaker.models.MemeAnnotation;

import java.util.ArrayList;
import java.util.Date;

public class MemeDatasource {

    private Context mContext;
    private MemeSQLiteHelper mMemeSqlLiteHelper;

    public MemeDatasource(Context context) {
        mContext = context;
        mMemeSqlLiteHelper = new MemeSQLiteHelper(mContext);
//        SQLiteDatabase database = mMemeSqlLiteHelper.getReadableDatabase();
//        database.close();
    }

    private SQLiteDatabase open(){
        return mMemeSqlLiteHelper.getWritableDatabase();
    }

    private void close(SQLiteDatabase database){
        database.close();
    }


    public void create(Meme meme){
        SQLiteDatabase database = open();
        database.beginTransaction();
        //implementation details
        ContentValues memeValues = new ContentValues();
        //get the actual meme model object into the content values
        memeValues.put(MemeSQLiteHelper.COLUMN_MEME_NAME, meme.getName());
        memeValues.put(MemeSQLiteHelper.COLUMN_MEME_ASSET, meme.getAssetLocation());
        memeValues.put(MemeSQLiteHelper.COLUMN_MEME_CREATED_DATE, new Date().getTime());
        long memeID = database.insert(MemeSQLiteHelper.MEMES_TABLE, null, memeValues);

        for (MemeAnnotation annotation : meme.getAnnotations()){
            ContentValues annotationValues = new ContentValues();
            annotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_COLOR, annotation.getColor());
            annotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_TITLE, annotation.getTitle());
            annotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_X, annotation.getLocationX());
            annotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_Y, annotation.getLocationY());
            annotationValues.put(MemeSQLiteHelper.COLUMN_FOREIGN_KEY_MEME, memeID);
            database.insert(MemeSQLiteHelper.ANNOTATIONS_TABLE, null, annotationValues);
        }

        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);
    }


    public ArrayList<Meme> read(){
        ArrayList<Meme> memes = readMemes();
        addMemeAnnotations(memes);
        return memes;
    }

    public ArrayList<Meme> readMemes(){
        SQLiteDatabase database = open();
        Cursor cursor = database.query(
                MemeSQLiteHelper.MEMES_TABLE,
                new String[]{BaseColumns._ID, MemeSQLiteHelper.COLUMN_MEME_NAME, MemeSQLiteHelper.COLUMN_MEME_ASSET},
                null, //selection
                null, //selection args
                null, //group by
                null, //having
                MemeSQLiteHelper.COLUMN_MEME_CREATED_DATE + " DESC"); //order(DESC/ASC)
        ArrayList<Meme> memes = new ArrayList<Meme>();
        if(cursor.moveToFirst()){
            do{
                Meme meme = new Meme(getIntFromColumnName(cursor, BaseColumns._ID),
                                     getStringFromColumnName(cursor, MemeSQLiteHelper.COLUMN_MEME_ASSET),
                                     getStringFromColumnName(cursor, MemeSQLiteHelper.COLUMN_MEME_NAME),
                                     null
                                    );
                memes.add(meme);
            }while (cursor.moveToNext());
        }
        cursor.close();
        close(database);
        return memes;
    }

    public void addMemeAnnotations(ArrayList<Meme> memes){
        SQLiteDatabase database = open();
        for(Meme meme : memes){
            ArrayList<MemeAnnotation> annotations = new ArrayList<MemeAnnotation>();
                Cursor cursor = database.rawQuery("SELECT * FROM "+ MemeSQLiteHelper.ANNOTATIONS_TABLE +
                                                  " WHERE MEME_ID = "+ meme.getId(), null);
                if(cursor.moveToFirst()){
                    do {
                        MemeAnnotation annotation = new MemeAnnotation(
                            getIntFromColumnName(cursor, BaseColumns._ID),
                            getStringFromColumnName(cursor, MemeSQLiteHelper.COLUMN_ANNOTATION_COLOR),
                            getStringFromColumnName(cursor, MemeSQLiteHelper.COLUMN_ANNOTATION_TITLE),
                            getIntFromColumnName(cursor, MemeSQLiteHelper.COLUMN_ANNOTATION_X),
                            getIntFromColumnName(cursor, MemeSQLiteHelper.COLUMN_ANNOTATION_Y)
                        );
                        annotations.add(annotation);
                    }while (cursor.moveToNext());
                }
            meme.setAnnotations(annotations);
            cursor.close();
        }
        close(database);
    }

    private int getIntFromColumnName(Cursor cursor, String columnName){
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getInt(columnIndex);
    }

    private String getStringFromColumnName(Cursor cursor, String columnName){
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getString(columnIndex);
    }


    public void update(Meme meme){
        SQLiteDatabase database = open();
        database.beginTransaction();
        ContentValues updateMemeValues = new ContentValues();
        updateMemeValues.put(MemeSQLiteHelper.COLUMN_MEME_NAME, meme.getName());
        database.update(MemeSQLiteHelper.MEMES_TABLE, updateMemeValues,
                String.format("%s=%d", BaseColumns._ID, meme.getId()),
                null);
        for (MemeAnnotation annotation : meme.getAnnotations()){
            ContentValues updateMemeAnnotationValues = new ContentValues();
            updateMemeAnnotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_TITLE, annotation.getTitle());
            updateMemeAnnotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_COLOR, annotation.getColor());
            updateMemeAnnotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_X, annotation.getLocationX());
            updateMemeAnnotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_Y, annotation.getLocationY());
            updateMemeAnnotationValues.put(MemeSQLiteHelper.COLUMN_FOREIGN_KEY_MEME, meme.getId());
            if(annotation.hasBeenSaved()){
                database.update(MemeSQLiteHelper.ANNOTATIONS_TABLE, updateMemeAnnotationValues,
                        String.format("%s=%d", BaseColumns._ID, annotation.getId()), null);
            }
            else {
                database.insert(MemeSQLiteHelper.ANNOTATIONS_TABLE, null, updateMemeAnnotationValues);
            }
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);
    }


    public void delete(int memeId){
        SQLiteDatabase database = open();
        database.beginTransaction();
        database.delete(MemeSQLiteHelper.ANNOTATIONS_TABLE,
                String.format("%s=%s", MemeSQLiteHelper.COLUMN_FOREIGN_KEY_MEME, String.valueOf(memeId)),
                null);
        database.delete(MemeSQLiteHelper.MEMES_TABLE,
                String.format("%s=%s", BaseColumns._ID, memeId),
                null);
        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);
    }


}
