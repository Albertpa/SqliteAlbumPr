package com.aprendeandroid.sqlitealbum;

import android.R.color;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.MediaColumns;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.aprendeandroid.sqlitealbum.DBDetail.DBDetailListener;
import com.aprendeandroid.sqlitealbum.DBListFragment.DBLFListener;
import com.aprendeandroid.sqlitealbum.R;
import com.aprendeandroid.sqlitealbum.CustomListFragment.ListItemSelectedListener;
import com.aprendeandroid.sqlitealbum.EditNameDialog.EditNameDialogListener;

public class SqlActivity extends Activity implements
		EditNameDialogListener, ListItemSelectedListener, DBDetailListener, DBLFListener{
	
	private DBListFragment listFrag;
	private boolean isTwoPane = false;
	
	private final int SELECT_PHOTO = 2;
	private Uri imagenAsociada = null;
	private String imagenPath = null;	
	private boolean tieneIMG = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sql);
		
		if (findViewById(R.id.detailPlace) != null) {
        	isTwoPane = true;
        }
		Log.e("PracticaIntents", "isTwoPane"+isTwoPane);
		createListFragment();				
	}

	private void createListFragment() {
		listFrag = new DBListFragment();
		
		Bundle parametros = new Bundle();
		parametros.putInt("listLayoutId", R.layout.list_fragment);
		parametros.putInt("emptyViewId", android.R.id.empty);
		parametros.putBoolean("isTwoPane",isTwoPane);
		listFrag.setArguments(parametros);
		
		FragmentManager fm = getFragmentManager();
		/**
		 * Starting a fragment transaction to dynamically add fragment to the
		 * application
		 */
		FragmentTransaction ft = fm.beginTransaction();

		/** Adding fragment to the fragment transaction */
		ft.add(R.id.listPlace, listFrag, "LIST");

		/** The contact dialog fragment is effectively added and opened */
		
		ft.commit();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
	    getMenuInflater().inflate(R.menu.activity_menu, menu);
	    
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {	    	
	        case R.id.menu_add:
	        	imagenAsociada = null;
	    		imagenPath = null;
	    		tieneIMG = false;
	    		
	        	listFrag.addHoja();
	            return true;
	            /*
	        case R.id.menu_edit:
	        	
	        	TextView modeView;
	        	TextView tituloView;
	        	TextView commentView;
	        	
	        	modeView = (TextView) findViewById(R.id.textMode);
				commentView = (EditText) findViewById(R.id.editComment);
				tituloView = (EditText) findViewById(R.id.editTitle);
				modeView.setText("Escritura");
				
				//Permitir o no la escritura
				tituloView.setFocusableInTouchMode(true);
				tituloView.setFocusable(true);		
				
				
				commentView.setFocusableInTouchMode(true);
				commentView.setFocusable(true);
				
	        	
				//textView.setTextColor(getResources().getColor(R.color.fondoDetalle));
	            return true;
	            */
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	//--------------MENU CONTEXTUAL DE LA LISTA (deletes)
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.context_menu, menu);
	}	

	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch (item.getItemId()) {
	        case R.id.menu_delete:
	        	listFrag.deleteHoja(info.position);
	            return true;	       
	        default:
	            return super.onContextItemSelected(item);
	    }
	}	


	
	
	//---------------LISTENERS de los interfaces
	@Override
	public void onFinishEditDialog(boolean result, String editedName) {
		// Save the new hoja to the database
		listFrag.onFinishEditDialog(result, editedName);
	}

	@Override
	public String getNameToEdit() {
		return listFrag.getNameToEdit();
	}

	@Override
	public void onListItemSelected(int index, String tag, long id) {
		// TODO Auto-generated method stub		
	}
	@Override
	public void guardarDatos(String noutext, String noucomentari){		
		//Le pasamos tambien la imagenAsociada, la cual se carga desde la activity siempre que se seleccione
		listFrag.guardarDatos(noutext, noucomentari, imagenPath, tieneIMG);
		
	}
	@Override
	public void limpiezaIMG(){		
		imagenAsociada = null;
		imagenPath = null;
		tieneIMG = false;
		
	}
	
	/*
	@Override
	public void primerElemento(){
		Log.e("PracticaIntents", "primerElemento");
		listFrag.primerElementoDetalle();
	}
	
	*/
	
	
	
	
	//------------------FUNCIONES PARA LA IMAGEN
	
	public void seleccionaImagen(View v){		
		Intent i;		
		i = new Intent(Intent.ACTION_PICK);
		i.setType("image/*");
		startActivityForResult(i, SELECT_PHOTO);
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		
		//Si retorna un resultado correcto
		
		//Dependiendo de la actividad llamada que ha retornado el resultado
		switch(requestCode){			
			case SELECT_PHOTO:
				
				if(resultCode == RESULT_OK){
					ImageView view = (ImageView) findViewById(R.id.editImage);
					
					Uri imageUri = data.getData();
					
					//nos guardamos la uri de la imagen
					imagenAsociada = imageUri;
					imagenPath = getPathFromUri(imageUri);
					tieneIMG = true;
					
					Bitmap galleryPic = scaleBitmap(getPathFromUri(imageUri), view.getHeight());
					
					if(galleryPic != null){
						view.setImageBitmap(galleryPic);
						
					}else{
						Toast toast = Toast.makeText(this, "Imagen fallida", Toast.LENGTH_LONG);
						toast.show();
					}
					
				}
				break;
			
		}
		
	}
	
	//----------------------------Metodos para la selección de imagenes
		/**
		 * Convierte una URI generada por el provider MediaStore (base de datos SQL de archivos media)
		 * p. ej. content://media/external/images/media/36
		 * en un path del sistema de ficheros, p. ej. /mnt/sdcard/DCIM/Camera/IMG_20121127_053546.jpg
		 * @param uri la URI
		 * @return el path
		 */
		private String getPathFromUri(Uri uri) { 
			String path = "";
			String[] projection = { MediaColumns.DATA };
			Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
			try {
				int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
				if (cursor.moveToFirst())
					path = cursor.getString(column_index);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			cursor.close();
			return path;
		}
		

			//metodo de rescalado
		private Bitmap scaleBitmap(String image_path, int maxDimension) {
			Bitmap scaledBitmap;
			
			BitmapFactory.Options op = new BitmapFactory.Options();
			op.inJustDecodeBounds = true; // solo devuelve las dimensiones, no carga bitmap
			scaledBitmap = BitmapFactory.decodeFile(image_path, op); //en op est‡n las dimensiones

			// usamos Math.max porque es mejor que la imagen sea un poco mayor que el
			// control donde se muestra, que un poco menor. Ya que si es menor el control
			// la agranda para ajustarla y se podria pixelar un poco.
			if ((maxDimension < op.outHeight) || (maxDimension < op.outWidth)) {
				// cada dimensiÃ³n de la imagen se dividir por op.inSampleSize al cargar
				op.inSampleSize = Math.round(Math.max((float) op.outHeight / (float) maxDimension,(float) op.outWidth / (float) maxDimension)); //calculamos la proporcion de la escala para que no deforme la imagen y entre en las dimensiones fijadas en la vista
			}

			op.inJustDecodeBounds = false; // ponemos a false op...
			scaledBitmap = BitmapFactory.decodeFile(image_path, op); //...para que ya el bitmap se cargue realmente
			
			return scaledBitmap;
		}
	
}
