package com.blues.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY);

        String name = cursor.getString(nameColumnIndex);
        int price = cursor.getInt(priceColumnIndex);
        int quantity = cursor.getInt(quantityColumnIndex);

        nameTv.setText(name);
        priceTv.setText(formatPrice(price));
        quantityTv.setText(formatQuantity(quantity));
    }

    private String formatPrice(int price){
        return "Price: $" + price;
    }

    private String formatQuantity(int quantity){
        return "Quantity: " + quantity;
    }
}
