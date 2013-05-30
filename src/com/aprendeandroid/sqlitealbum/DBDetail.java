package com.aprendeandroid.sqlitealbum;



import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.MediaColumns;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.MenuItem.OnActionExpandListener;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

public class DBDetail extends Fragment {
	final static String ARG_POSITION = "position";
    int mCurrentPosition = -1;
    //private final int SELECT_PHOTO = 2;

	//elementos de la vista de edicion
    private boolean escritura = false;
    private boolean lectura = true;
    
    private TextView modoView;
	private TextView tituloView;
	private TextView commentView;
	private ImageView imagenView;
	
	private String nouText;
	private String nouComment;
	
	//private Uri imagenAsociada = null;
	//private String imagenPath = null;	
	//private View vtot;
	
	//listener para llamar a guardarDatos
	DBDetailListener listener;
	
	
	 @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
	        Bundle savedInstanceState) {
		
	        // If activity recreated (such as from screen rotate), restore
	        // the previous article selection set by onSaveInstanceState().
	        // This is primarily necessary when in the two-pane layout.
	        if (savedInstanceState != null) {
	            mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);
	        }
	        View v = inflater.inflate(R.layout.activity_dbdetail, container, false);
	       // vtot = v;
	        
	    	//Constructor especial del adapter para relaccionarse con cursores
	        /*
			adapter = new MyCursorAdapter(getActivity(), R.layout.row, null,
							new String[] { MySQLiteHelper.COLUMN_TITLE, MySQLiteHelper.COLUMN_COMMENT, MySQLiteHelper.COLUMN_IMAGE },
							new int[] {R.id.editTitle, R.id.editComment, R.id.editImage },
							CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
			*/
	     
	    	//Constructor especial del adapter para relaccionarse con cursores
			
			
	        Bundle parametros = getArguments(); //Aqui vienen los parametro de este Layout
	        String titulo = "";
	        String comentario = "";
	        String imagen = "";
			if(parametros != null) {
				
				titulo = parametros.getString("title");
				comentario = parametros.getString("comment");
				imagen = parametros.getString("image");
			}				
			
			
			//campo donde se mostrara el resultado
			modoView = (TextView) v.findViewById(R.id.textMode);
			modoView.setText("Lectura");
			
			tituloView = (EditText) v.findViewById(R.id.editTitle);
			tituloView.setText(titulo);
		
			
			commentView = (EditText) v.findViewById(R.id.editComment);
			commentView.setText(comentario);
			
			imagenView = (ImageView) v.findViewById(R.id.editImage);
			
			if(imagen != null){				
				//Log.e("PracticaIntents", "el tamaño es."+ imagenView.getHeight());
				//en este caso parece ser que la vista aun no esta disponible, por lo que el view.getHeight() devuelve 0
				//Bitmap galleryPic = scaleBitmap(imagenPath, view.getHeight());
				//En estos casos es mejor usar:
					//view.getLayoutParams().height;
					//view.getLayoutParams().width;
				Bitmap galleryPic = scaleBitmap(imagen, imagenView.getLayoutParams().height);
				
				if(galleryPic != null){
					imagenView.setImageBitmap(galleryPic);
					
				}
			}
			//No se permite la escritura la primera vez que se muestra
			tituloView.setFocusableInTouchMode(escritura);
			tituloView.setFocusable(escritura);

			commentView.setFocusableInTouchMode(escritura);
			commentView.setFocusable(escritura);
	        
			
			imagenView.setEnabled(escritura); 
			//EditText imageView = (EditText)v.findViewById(R.id.txt_your_name);		
			//imageView.setText(txt_your_name);	
			
			setHasOptionsMenu(true);

			
		    return v;
	 }	
	 

	//------------------------------ BLOQUE EDITAR DETALLE 1: ESCUCHA EL OPTION MENU ??
	 
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			// Place an action bar item for searching.
		
			super.onCreateOptionsMenu(menu, inflater);
			inflater.inflate(R.menu.dbdetail, menu);
			
			final MenuItem save_edit = menu.findItem(R.id.save_edit);
			save_edit.setVisible(false);
			
			final MenuItem item = menu.findItem(R.id.menu_edit);			
			
			item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
		  
				@Override
				public boolean onMenuItemClick(MenuItem item) {				
					
					if(lectura){
						//pasamos a escritura
						modoView.setText("Escritura");
						escritura=true;
						lectura=false;
						save_edit.setVisible(true);
						
						
					}else{
						//pasamos a lectura
						modoView.setText("Lectura");
						escritura=false;
						lectura=true;
						save_edit.setVisible(false);
						
					}					
					
					//Permitir o no la escritura
					tituloView.setFocusableInTouchMode(escritura);
					tituloView.setFocusable(escritura);						
					
					commentView.setFocusableInTouchMode(escritura);
					commentView.setFocusable(escritura);
					
					imagenView.setEnabled(escritura); 
					
		        	
					//textView.setTextColor(getResources().getColor(R.color.fondoDetalle));
		            return true;
				
				}
			});				
			
			save_edit.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			    //item.setOnActionExpandListener(new OnActionExpandListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {						
										
						nouText = tituloView.getText().toString();
						nouComment = commentView.getText().toString();
						listener.guardarDatos(nouText, nouComment);
			            return true;
					
					}
				});	
			
			
		}	
		
				
		
		@Override
		public void onAttach(Activity activity){
			super.onAttach(activity);
			try{
				listener = (DBDetailListener) activity;
				
			}catch (ClassCastException e){
				throw new ClassCastException(activity.toString()
						+ "debes implementar DBDetailListener en la Activity");
			}
		}
		
		public static interface DBDetailListener{
			//public String getNameToEdit();
			public void guardarDatos(String nouText, String nouComentari);
		}
		
		
		//metodo de rescalado para mostrar correctamente la imagen
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
