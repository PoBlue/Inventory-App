package com.blues.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.blues.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by blues on 2016/10/31.
 */

public class InventoryProvider extends ContentProvider {

    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    private static final int INVENTORIES = 100;
    private static final int INVENTORY_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORIES, INVENTORIES);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORIES + "/#", INVENTORY_ID);
    }

    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORIES:
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null,null,sortOrder);
                break;
            case INVENTORY_ID:
                selection = INVENTORY_ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case INVENTORIES:
                return insertInventory(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertInventory(Uri uri, ContentValues values) {
        String name = values.getAsString(InventoryEntry.COLUMN_INVENTORY_NAME);
        Integer price = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_PRICE);
        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_QUANTITY);

        if (!InventoryContract.isValidName(name)){
            throw new IllegalArgumentException("Inventory requires a name");
        }

        if (!InventoryContract.isValidPrice(price)){
            throw new IllegalArgumentException("Inventory requires a price is large than 0");
        }

        if (!InventoryContract.isValidQuantity(quantity)) {
            throw new IllegalArgumentException("Inventory requires a quantity is large than or equal 0");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.insert(InventoryEntry.TABLE_NAME, null, values);
        if (id == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORIES:
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not support for " + uri);
        }

        if (rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case INVENTORIES:
                return update(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateInventory(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for" + uri);
        }
    }

    private int updateInventory(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_NAME)){
            String name = values.getAsString(InventoryEntry.COLUMN_INVENTORY_NAME);
            if (!InventoryContract.isValidName(name)){
                throw new IllegalArgumentException("Inventory requires a name");
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_PRICE)){
            Integer price = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_PRICE);
            if (!InventoryContract.isValidPrice(price)){
                throw new IllegalArgumentException("Inventory requires a price is large than 0");
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_QUANTITY)){
            Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
            if (!InventoryContract.isValidQuantity(quantity)) {
                throw new IllegalArgumentException("Inventory requires a quantity is large than or equal 0");
            }
        }

        if (values.size() == 0){
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
