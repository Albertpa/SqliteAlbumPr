package com.aprendeandroid.sqlitealbum;

import com.aprendeandroid.sqlitealbum.R;
import com.aprendeandroid.sqlitealbum.CustomListFragment.ListItemSelectedListener;
import com.aprendeandroid.sqlitealbum.DBDetail.DBDetailListener;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

//public class DBListFragment extends CustomListFragment implements 
//			OnQueryTextListener, OnCloseListener, LoaderManager.LoaderCallbacks<Cursor> ,, no funciona OnCloseListener  en >= 3.0
public class DBListFragment extends CustomListFragment implements
		OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> { //hay que implementar los callbacks del Loader
	
	private Handler mHandler = new Handler();
	
	private MyCursorAdapter adapter;
	private Hoja selectedHoja = null;
	
	private SearchView mSearchView;
	private String mCurFilter = null;
	private boolean isTwoPane = false;
	
	private boolean haCargado = false;
	
	//listener para llamar a guardarDatos
		DBLFListener listener;
	
	//Este no tiene la base de datos, porque Cursor Loader 
	//se encarga de operar
	
	//1¼ Content provider permite que esta base de datos sea compartida 
		//con otras app voluntariamente, mediaStore, Diccionario, contactos...
	//2» Content provider usa LoaderManager para hacer operaciones de 
		//base de datos en segundoPlano (usa hilos), y es lo recomendado 
		
		
	
	//---------------------------------- CICLO VIDA DEL FRAGMENT
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//obtenemos parametros
		Bundle parametros = getArguments(); //Aqui vienen los parametro de este Layout
		if(parametros != null) {
			isTwoPane = parametros.getBoolean("isTwoPane");
		}	
				
		//Constructor especial del adapter para relaccionarse con cursores
		adapter = new MyCursorAdapter(getActivity(), R.layout.row, null,
						new String[] { MySQLiteHelper.COLUMN_LAST_TIME, MySQLiteHelper.COLUMN_TITLE },
						new int[] {R.id.txtTime, R.id.txtTitle },
						CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		
		setListAdapter(adapter);

		registerForContextMenu(getListView());		
		
		getActivity().getLoaderManager().initLoader(0, null, this);//recarga lista 
		//la primera vez va con initLoader
	}

	
	
	
	

	
	
	
	//------------------------------ BLOQUE A„ADIR HOJA --> 
	public void addHoja() {
		selectedHoja = null;
		showEditDialog();
	}

	//------------------------------ BLOQUE BORRAR HOJA
	public void deleteHoja(int position) {
		ContentResolver cr = getActivity().getContentResolver();
		
		Cursor cursor = adapter.getCursor();
		
		int colId = cursor.getColumnIndex(MySQLiteHelper.COLUMN_ID); //id
		long id = cursor.getLong(colId); 
		
		Uri uri = ContentUris.withAppendedId(AlbumContentProvider.CONTENT_URI,id);
		
		cr.delete(uri, null, null);
		
		getActivity().getLoaderManager().restartLoader(0, null, this);
		
	}
	
	//------------------------------ BLOQUE EDITAR HOJA
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		index = position;
		itemId = id;
		if (isResumed()) {
			
			Cursor cursor = (Cursor) adapter.getItem(index);			
			selectedHoja = Hoja.cursorToHoja(cursor);
			
			
			//AHORA SERA VER HOJA, NUEVO FRAGMENT			
			DBDetail dbdetalle = new DBDetail();
			Bundle parametros = new Bundle();
			 
			parametros.putString("title", getNameToEdit());
			parametros.putString("comment", getCommentToEdit());
			parametros.putString("image", getImageToEdit());
			
			//parametros.putInt("emptyViewId", android.R.id.empty);
			//parametros.putBoolean("isTwoPane",isTwoPane);
			dbdetalle.setArguments(parametros);
			
			FragmentManager fm = getFragmentManager();
			FragmentTransaction transaction = getFragmentManager().beginTransaction();			
			
			
			if(isTwoPane){
				transaction.replace(R.id.detailPlace, dbdetalle);
				//fm.beginTransaction().replace(R.id.detailPlace, dbdetalle).commit();
			
			}else{
				transaction.replace(R.id.listPlace, dbdetalle);
				//fm.beginTransaction().replace(R.id.listPlace, dbdetalle).commit();
				
			}
			transaction.addToBackStack(null);
			transaction.commit();
			
			//cada vez que se selecciona un elemento, por seguridad limpiamos las posibles variables de la activity
			listener.limpiezaIMG();
			
			//ANTES ERA EDITAR HOJA
			//showEditDialog();
		}
	}
	
	//------------------------------ BLOQUE DIALOGO EDITOR/CREADOR HOJA	
	public void showEditDialog() {
		// FragmentManager fm = getChildFragmentManager(); // sin support solo a
		// partir de API 17
		FragmentManager fm = getFragmentManager();
		EditNameDialog editNameDialog = new EditNameDialog();
		editNameDialog.show(fm, "fragment_edit_name");
	}
	
	public String getNameToEdit() {
		String name = null;

		if (selectedHoja != null)
			name = selectedHoja.getTitle();
		//Log.e("PracticaIntents", "name "+name);	
		return name;
	}
	
	public String getCommentToEdit() {
		String comment = null;

		if (selectedHoja != null)
			comment = selectedHoja.getComment();	
		//Log.e("PracticaIntents", "comment "+comment);	
		return comment;
	}
	
	public String getImageToEdit() {
		String image = null;
		
		if (selectedHoja != null)
			image = selectedHoja.getImage();

		
		//Log.e("PracticaIntents", "imagen "+image);	
		return image;
	}
	
	public void onFinishEditDialog(boolean result, String editedName) {
		// Inserta o actualiza una Hoja
		Uri uri;
		if (result) {
			if (selectedHoja == null) { // adding
				ContentResolver cr = getActivity().getContentResolver();
				
				//creamos los valores
				ContentValues values = new ContentValues();
				values.put(MySQLiteHelper.COLUMN_TITLE, editedName);

				cr.insert(AlbumContentProvider.CONTENT_URI, values);
				getActivity().getLoaderManager().restartLoader(0, null, this);
			} 
			else { // editing
				selectedHoja.setTitle(editedName);
				mCurFilter = null;

				ContentResolver cr = getActivity().getContentResolver();
				Cursor cursor = adapter.getCursor();
				int colId = cursor.getColumnIndex(MySQLiteHelper.COLUMN_ID);
				long id = cursor.getLong(colId);
				uri = ContentUris.withAppendedId(AlbumContentProvider.CONTENT_URI, id);

				ContentValues values = new ContentValues();
				values.put(MySQLiteHelper.COLUMN_TITLE, editedName);

				cr.update(uri, values, null, null);
				getActivity().getLoaderManager().restartLoader(0, null, this); //this es es listener
			}
		}
		selectedHoja = null;
	}
	
	
	
	//------------------------------ BLOQUE BUSQUEDA Y FILTRADO NOMBRE HOJA
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Place an action bar item for searching.
		final MenuItem item = menu.findItem(R.id.menu_search);

		mSearchView = (SearchView) item.getActionView();
		mSearchView.setOnQueryTextListener(this);
		// mSearchView.setOnCloseListener(this); // no funciona en >= 3.0
		item.setOnActionExpandListener(new OnActionExpandListener() {

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				mSearchView.setQuery(null, true);
				return true; // Return true to collapse action view
			}

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				// TODO Auto-generated method stub
				return true;
			}
		});
	}
	
	
	
	
	
	
	
	//la forma de hablar con la base de datos cambia, se hace mediante el loader
	@Override
	public boolean onQueryTextChange(String newText) {
		String newFilter = !TextUtils.isEmpty(newText) ? newText : null;//cuando escribimos en bara de busqueda
		
		if (mCurFilter == null && newFilter == null) {
			return true;
		}
		if (mCurFilter != null && mCurFilter.equals(newFilter)) {
			return true;
		}
		
		mCurFilter = newFilter;
		getLoaderManager().restartLoader(0, null, this);//recarga lista
		//el resto de veces se hace con restartLoader
		return true;
	}
	
	
	@Override
	public boolean onQueryTextSubmit(String query) {
		// Don't care about this.
		return true;
	}

	
	

	

	

	

	
	
	
	
	//---------------------------ACTUACION DEL LoaderCallbacks<Cursor>
	

	//Todo esto lo hace cada vez que llamamos por getActivity().getLoaderManager().restartLoader(0, null, this);
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		String select = null;
		if (mCurFilter != null && !mCurFilter.trim().equals("")) { //filtro de busqueda
			select = MySQLiteHelper.COLUMN_TITLE + " LIKE " + '"'+ mCurFilter + '%' + '"'; // % es necesario para que funcione like
		}
		return new CursorLoader(getActivity(),
				AlbumContentProvider.CONTENT_URI, AlbumContentProvider.PROJECTION, select, null, AlbumContentProvider.TITLES_SORT_ORDER);
	}

	//Cuando termina de recorrer la base de datos
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Save the new hoja to the database
		adapter.swapCursor(data);//quita el viejo cursor y pone el nuevo
		adapter.notifyDataSetChanged();//actualizamos la lista
		
		 mHandler.post(new Runnable() {

	            @Override
	            public void run() {
	        		int totales = (int) adapter.getCount();		        							
	        		//Log.e("PracticaIntents", "totales->"+totales);
	        		//Log.e("PracticaIntents", "haCargadoYa?->"+haCargado);
	        		
	        		//Solo debe entrar la primera vez que se genera la lista
	        		if(totales >0 && !haCargado){
	        			haCargado = true;	        			
	        			Cursor cursor = (Cursor) adapter.getItem(0);	        			
	        			selectedHoja = Hoja.cursorToHoja(cursor);
	        			
	        			DBDetail dbdetalle = new DBDetail();
	        			Bundle parametros = new Bundle();
	        			 
	        			parametros.putString("title", getNameToEdit());
	        			parametros.putString("comment", getCommentToEdit());
	        			parametros.putString("image", getImageToEdit());
	        			
	        			//parametros.putInt("emptyViewId", android.R.id.empty);
	        			//parametros.putBoolean("isTwoPane",isTwoPane);
	        			dbdetalle.setArguments(parametros);
	        			
	        			FragmentManager fm = getFragmentManager();
	        			FragmentTransaction transaction = getFragmentManager().beginTransaction();			
	        			
	        			
	        			if(isTwoPane){
	        				transaction.replace(R.id.detailPlace, dbdetalle);
	        				//fm.beginTransaction().replace(R.id.detailPlace, dbdetalle).commit();
	        			
	        			}else{
	        				transaction.replace(R.id.listPlace, dbdetalle);
	        				//fm.beginTransaction().replace(R.id.listPlace, dbdetalle).commit();
	        				
	        			}
	        			transaction.addToBackStack(null);
	        			transaction.commit();
	        			/*
	        			//cada vez que se selecciona un elemento, por seguridad limpiamos las posibles variables de la activity	
	        			//listener.limpiezaIMG();
	        			*/
	        		}
	            }
	        });
		
		//listener.primerElemento();
	}

	//Si se hace reset y se recarga la lista
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);//se limpia el cursor
	}
	
	
	
	public void guardarDatos(String nouText, String nouComment, String imagen, boolean tieneIMG) {
		Uri uri;		
		
		ContentResolver cr = getActivity().getContentResolver();
		/* Edita otros elementos
		Cursor cursor = adapter.getCursor();		
		int colId = cursor.getColumnIndex(MySQLiteHelper.COLUMN_ID);
		long id = cursor.getLong(colId);
		*/
		long id = selectedHoja.getId();
		uri = ContentUris.withAppendedId(AlbumContentProvider.CONTENT_URI, id);

		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_TITLE, nouText);
		values.put(MySQLiteHelper.COLUMN_COMMENT, nouComment);
		
		if(tieneIMG)
			values.put(MySQLiteHelper.COLUMN_IMAGE, imagen);
		
		
		if(nouText.length() != 0){
			cr.update(uri, values, null, null);
			Toast toast = Toast.makeText(getActivity(), "Guardado Correctamente", Toast.LENGTH_LONG);
			toast.show();
		}else{
			Toast toast = Toast.makeText(getActivity(), "Titulo no puede estar vacio", Toast.LENGTH_LONG);
			toast.show();
		}
		
		getActivity().getLoaderManager().restartLoader(0, null, this); //this es es listener

	}
	/*
	public void primerElementoDetalle(){
	
		

		
	}
	*/
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		try{
			listener = (DBLFListener) activity;
			
		}catch (ClassCastException e){
			throw new ClassCastException(activity.toString()
					+ "debes implementar DBLFListener en la Activity");
		}
	}
	
	public static interface DBLFListener{		
		public void limpiezaIMG();
		//public void primerElemento();
	}

}
