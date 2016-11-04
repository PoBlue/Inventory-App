package com.blues.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.blues.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by blues on 2016/11/1.
 */

public class InventoryAdapter extends CursorAdapter {
    public InventoryAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTv = (TextView) view.findViewById(R.id.name);
        TextView priceTv = (TextView) view.findViewById(R.id.price);
        TextView quantityTv = (TextView) view.findViewById(R.id.quantity);

        int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY);

        final String name = cursor.getString(nameColumnIndex);
        final int price = cursor.getInt(priceColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        final int rowId = cursor.getInt(idColumnIndex);

        nameTv.setText(name);
        priceTv.setText(formatPrice(price));
        quantityTv.setText(formatQuantity(quantity));

        Button saleBtn = (Button) view.findViewById(R.id.saleButton);
        saleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri currentInventoryUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, rowId);

                ContentValues values = new ContentValues();
                if (quantity > 0){
                    values.put(InventoryEntry.COLUMN_INVENTORY_NAME, name);
                    values.put(InventoryEntry.COLUMN_INVENTORY_PRICE,price);
                    values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantity - 1);
                } else {
                    return;
                }

                view.getContext().getContentResolver().update(currentInventoryUri, values, null, null);
            }
        });
    }


    private void setTextView(View view, Cursor cursor) {
    }

    private String formatPrice(int price){
        return "Price: $" + price;
    }

    private String formatQuantity(int quantity){
        return "Quantity: " + quantity;
    }
}
