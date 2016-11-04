package com.blues.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.blues.inventoryapp.data.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{


    private static final int INVENTORY_LOADER = 0;
    InventoryAdapter mInventoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setListView();
        setFloatingBtn();
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    private void setFloatingBtn(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                insertInventory();
            }
        });
    }

    private void insertInventory() {
        Intent intent = new Intent(this,EditorActivity.class);
        startActivity(intent);
    }

    private void updateInventory(long rowId) {
        Intent intent = new Intent(this,EditorActivity.class);

        Uri currentInventoryUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, rowId);
        intent.setData(currentInventoryUri);

        startActivity(intent);
    }

    private void deleteAllInventories(){
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI,null,null);
        Toast.makeText(this, rowsDeleted + "rows deleted", Toast.LENGTH_SHORT).show();
    }


    private void setListView(){
        ListView inventoryListView = (ListView) findViewById(R.id.list);

        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        mInventoryAdapter = new InventoryAdapter(this,null);
        inventoryListView.setAdapter(mInventoryAdapter);

        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowId) {
                updateInventory(rowId);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inventory,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_insert_new_data:
                insertInventory();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllInventories();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_IMAGE_PATH
        };

        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {
        mInventoryAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        mInventoryAdapter.swapCursor(null);
    }
}

