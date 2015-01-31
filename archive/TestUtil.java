package org.rochlitz.hbci.callback;


public class TestUtil extends HBCIAsyncTask {

	
	private static final String APP_STORE = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/Android/data/"
			+ TestUtil.class.getPackage().getName() + "/files/";
	
	public TestUtil( ) {
	}

	public void doPublishProgress( ){
		publishProgress( );
	}
	
	public void test() {
		 
	 
	}

	private void readAsset(Context context) {
		AssetManager assetManager = context.getResources().getAssets();
		String[] files;
		try {

			files = assetManager.list("res");
			Log.d("raw file", files.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void testReadFile() {
		try {
			File myFile = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ "/Android/data/"
					+ this.getClass().getPackage().getName()
					+ "/files"
					+ "/test.txt");
			FileInputStream fIn = new FileInputStream(myFile);
			BufferedReader myReader = new BufferedReader(new InputStreamReader(
					fIn));
			String aDataRow = "";
			String aBuffer = "";
			while ((aDataRow = myReader.readLine()) != null) {
				aBuffer += aDataRow + "\n";
			}

			myReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeToExternalStoragePublic() {
		String packageName = this.getClass().getPackage().getName();
		String extPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		boolean existsExt = (new File(extPath)).exists();
		boolean externalStorageReadOnly = isExternalStorageReadOnly();
		boolean externalStorageWritable = isExternalStorageWritable();
		if (externalStorageWritable && !externalStorageReadOnly) {
			try {
				String path = Environment.getExternalStorageDirectory()
						.getAbsolutePath()
						+ "/Android/data/"
						+ this.getClass().getPackage().getName();
				boolean exists = (new File(path)).exists();
				if (!exists) {
					new File(path).mkdirs();
				}
				// Open output stream
				FileOutputStream fOut = new FileOutputStream(path, true);
				// write integers as separated ascii's
				fOut.write((Integer.valueOf("1234").toString() + " ")
						.getBytes());
				fOut.write((Integer.valueOf("1234").toString() + " ")
						.getBytes());
				// Close output stream
				fOut.flush();
				fOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(state)) {
			return true;
		}
		return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadOnly() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(state)
				&& Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	private void testWriteFIle() {
		String msg = "123";
		try {
			// File path = Environment.getExternalStoragePublicDirectory(
			// Environment.DIRECTORY_MOVIES);
			File file = new File(APP_STORE + "/test.txt");
			boolean canW = file.canWrite();
			boolean canR = file.canRead();
			if (!canW) {
				return;
			}
			OutputStream os;
			FileInputStream fIn = new FileInputStream(file);
			BufferedReader myReader = new BufferedReader(new InputStreamReader(
					fIn));
			String aDataRow = "";
			String aBuffer = "";
			while ((aDataRow = myReader.readLine()) != null) {
				aBuffer += aDataRow + "\n";
			}

			myReader.close();

			os = new FileOutputStream(file);

			byte[] data = msg.getBytes();
			os.write(data);
			os.close();

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
