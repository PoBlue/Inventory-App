package com.blues.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.blues.inventoryapp.data.InventoryContract;
import com.blues.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Integer.parseInt;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private Bitmap mImageBitmap;
    private static final String IMAGE_PATH_KEY = "imagePathKey";
    String mCurrentPhotoPath = "";
    private ImageView mImageView;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int EXISTING_INVENTORY_LOADER = 0;
    private Uri mCurrentInventoryUri;
    private boolean mInventoryHasChange = false;

    EditText mNameEditText,mPriceEditText,mQuantityEditText;
    String mName,mPrice,mQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            mCurrentPhotoPath = savedInstanceState.getString(IMAGE_PATH_KEY);
        }
        setContentView(R.layout.activity_editor);

        initBarTitle();
        initEditText();
        initImage();
    }

    private void initImage(){
        mImageView = (ImageView) findViewById(R.id.product_image);
    }

    private void initBarTitle(){
        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();

        if (mCurrentInventoryUri == null){
            setTitle("Add a Inventory");
            setButtonVisable(false);
        } else {
            setTitle("Edit Inventory");
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
            setButtonVisable(true);
            Log.e("uri",mCurrentInventoryUri.toString());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(IMAGE_PATH_KEY, mCurrentPhotoPath);
        super.onSaveInstanceState(outState);
    }

    private void initEditText(){
        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
    }

    private void setButtonVisable(boolean isVisable){
        Button orderBtn = (Button) findViewById(R.id.orderBtn);
        Button saleBtn = (Button) findViewById(R.id.saleBtn);
        Button buyBtn = (Button) findViewById(R.id.buyBtn);

        if (isVisable){
            orderBtn.setVisibility(View.VISIBLE);
            saleBtn.setVisibility(View.VISIBLE);
            buyBtn.setVisibility(View.VISIBLE);
        } else {
            orderBtn.setVisibility(View.GONE);
            saleBtn.setVisibility(View.GONE);
            buyBtn.setVisibility(View.GONE);
        }
    }

    public void pickImage(View v){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i(LOG_TAG, "IOException");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            displayImageWithBitmap();
        }
    }

    private void saveInventory(){
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();

        if (mCurrentInventoryUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(mCurrentPhotoPath)){
            Toast.makeText(this, "Error data so that not save", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_NAME, nameString);
        values.put(InventoryEntry.COLUMN_INVENTORY_IMAGE_PATH, mCurrentPhotoPath);

        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = parseInt(priceString);
        }
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, price);

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)){
            quantity = parseInt(quantityString);
        }
        values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantity);

        if ( !InventoryContract.isValidQuantity(quantity)
                || !InventoryContract.isValidPrice(price)
                || !InventoryContract.isValidName(nameString)
                || !InventoryContract.isValidImagePath(mCurrentPhotoPath)){
            Toast.makeText(this,"Error data so that not save",Toast.LENGTH_SHORT).show();
            return;
        }

        if (mCurrentInventoryUri == null){
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            if (newUri == null){
                Toast.makeText(this, "Error with saving inventory", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Inventory saved", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentInventoryUri, values, null, null);

            if (rowsAffected == 0){
                Toast.makeText(this, "Error with updating inventory", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Inventory updated", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener(){

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventoryHasChange = true;
            return false;
        }
    };

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mCurrentInventoryUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_save:
                saveInventory();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mInventoryHasChange){
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mInventoryHasChange){
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[]  projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_IMAGE_PATH
        };

        return new CursorLoader(this,
                mCurrentInventoryUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
            int imagePathColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_IMAGE_PATH);

            mName = cursor.getString(nameColumnIndex);
            mCurrentPhotoPath = cursor.getString(imagePathColumnIndex);
            mPrice = String.valueOf(cursor.getInt(priceColumnIndex));
            mQuantity = String.valueOf(cursor.getInt(quantityColumnIndex));

            mNameEditText.setText(mName);
            mPriceEditText.setText(mPrice);
            mQuantityEditText.setText(mQuantity);
            displayImageWithBitmap();
        }

    }

    private void  displayImageWithBitmap(){
        initImage();
        try {
            mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            float scaleHt =(float) width/mImageBitmap.getWidth();
            Log.e("Scaled percent ", " "+scaleHt);
            Bitmap scaled = Bitmap.createScaledBitmap(mImageBitmap, width, (int)(mImageBitmap.getWidth()*scaleHt), true);

            mImageView.setImageBitmap(scaled);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void orderClick(View v){

        String mes = "I need " + mName + "\n"
                + "Please send me " + mQuantity + " " + mName + "s"
                + " in " + mPrice +" dollar";

        String[] TO = {""};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Need more");
        emailIntent.putExtra(Intent.EXTRA_TEXT, mes);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
        }
        catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public void quantityClick(View v){
        String quantityString = mQuantityEditText.getText().toString().trim();

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)){
            quantity = parseInt(quantityString);
        }

        switch (v.getId()){
            case R.id.buyBtn:
                quantity += 1;
                break;
            case R.id.saleBtn:
                quantity -= 1;
                break;
        }

        if (!InventoryContract.isValidQuantity(quantity)){
            Toast.makeText(this,"quantity is not valid, put other", Toast.LENGTH_SHORT).show();
        } else {
            mQuantityEditText.setText(String.valueOf(quantity));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this inventory?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteInventory();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void deleteInventory(){
        if (mCurrentInventoryUri != null){
            int rowsDeleted = getContentResolver().delete(mCurrentInventoryUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, "Error with deleting Inventory", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Inventory deleted", Toast.LENGTH_SHORT).show();
            }

            finish();
        }
    }
}
