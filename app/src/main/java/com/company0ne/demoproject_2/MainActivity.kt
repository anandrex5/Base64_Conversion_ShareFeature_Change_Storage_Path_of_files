    package com.company0ne.demoproject_2

    import android.Manifest
    import android.content.Intent
    import android.content.pm.PackageManager
    import android.graphics.Bitmap
    import android.graphics.BitmapFactory
    import android.os.Bundle
    import android.os.Environment
    import android.util.Base64
    import android.util.Log
    import android.view.View
    import android.widget.Button
    import android.widget.ImageView
    import android.widget.TextView
    import android.widget.Toast
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.app.ActivityCompat
    import androidx.core.content.ContextCompat
    import androidx.core.content.FileProvider
    import androidx.documentfile.provider.DocumentFile
    import java.io.BufferedWriter
    import java.io.ByteArrayOutputStream
    import java.io.File
    import java.io.FileWriter
    import android.net.Uri as Uri1

    class MainActivity : AppCompatActivity() {

        lateinit var imgView: ImageView
        lateinit var btnChange: Button
        lateinit var imageUri: Uri1
        lateinit var btnConvert: Button
        lateinit var txtView: TextView
        lateinit var btnShare: Button
    //    lateinit var btnChoose: Button
        lateinit var folder_select: Button
//        lateinit var txt_log: TextView


        private var selectedFolderPath: Uri1? = null // To store the selected folder's URI

        private val REQUEST_STORAGE_PERMISSION = 101
        private val PICK_FOLDER_REQUEST = 104
        private val PICK_FILE_REQUEST = 103

        //give uri to the camera apk
        private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()) {
            //assign null bcz when we click next image it will update
            imgView.setImageURI(null)
            //callback - calls when the data comes from the camera
            imgView.setImageURI(imageUri)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            imgView = findViewById(R.id.imgView)
            btnChange = findViewById(R.id.btnChange)
            btnConvert = findViewById(R.id.button)
            txtView = findViewById(R.id.txtView)
            btnShare = findViewById(R.id.buttonShare)
            folder_select = findViewById(R.id.btnChoose)


            //call fun createImageUri -so it can be set
            imageUri = createImageUrl()!!

            btnConvert.setOnClickListener {

                //text assign to base64Image variable
                val base64Image = getFileToByte(imageUri)
                txtView.text = base64Image

                if (base64Image != null) {
                    saveBase64AsTextFile(base64Image)
                }
            }
            folder_select.setOnClickListener {

                //to open the file manager
//                openFileManager()
                openFolderPicker()

            }

            btnShare.setOnClickListener {

                try {
                    val file = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                        "base64AsText.txt"
                    )
                    if (file.exists()) {
                        val fileUri = FileProvider.getUriForFile(
                            this,
                            "com.company0ne.demoproject_2.fileProvider", file
                        )

                        val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/*"
                            putExtra(Intent.EXTRA_STREAM, fileUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivity(Intent.createChooser(sharingIntent, "share file with"))
                        Log.e("File", "File is sent")
                    } else {
                        Toast.makeText(this, "File not found!", Toast.LENGTH_SHORT).show()

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error sharing the file", Toast.LENGTH_SHORT).show()
                }

                //to share conversion base64 text

    //            val sendIntent: Intent = Intent().apply {
    //                action = Intent.ACTION_SEND
    //                putExtra(Intent.EXTRA_TEXT, txtView.text)
    //                type = "text/plain"
    //            }
    //
    //            val shareIntent = Intent.createChooser(sendIntent, null)
    //            startActivity(shareIntent)


                //for sharing click image from camera
    //            val shareIntent: Intent = Intent().apply {
    //                action = Intent.ACTION_SEND
    //                // Example: content://com.google.android.apps.photos.contentprovider/...
    //                putExtra(Intent.EXTRA_STREAM, imageUri)
    //                type = "image/jpeg"
    //            }
    //            startActivity(Intent.createChooser(shareIntent, null))
    //            val file = File(Environment.getExternalStorageDirectory().toString() + "/" + "base64AsText.txt")
    //            val sharingIntent = Intent(Intent.ACTION_SEND)
    //            sharingIntent.type = "text/*"
    //            sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.absolutePath))
    //            startActivity(Intent.createChooser(sharingIntent, "share file with"))


            }

            btnChange.setOnClickListener {
                contract.launch(imageUri)
            }
        }

        private fun openFolderPicker() {
            // Method to open the folder picker
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                startActivityForResult(intent, PICK_FOLDER_REQUEST)
            }


        //to open file manager
        private fun openFileManager() {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(android.content.Intent.CATEGORY_OPENABLE)
                    type = "*/*"  // All file types
                }
                startActivityForResult(intent, PICK_FILE_REQUEST)
            }

            override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
                super.onActivityResult(requestCode, resultCode, resultData)
                if (requestCode == PICK_FILE_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
                    resultData?.data?.also { uri ->
                        // Use the uri to get the file path or do what you need with it.
                        // Displaying the URI in the TextView for the demonstration.
                        txtView.text = "Selected File URI: $uri"
                    }
                }
                if (requestCode == PICK_FOLDER_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
                    resultData?.data?.also { uri ->
                        selectedFolderPath = uri // Save the selected folder's URI
                        Toast.makeText(this, "Folder selected: $uri", Toast.LENGTH_LONG).show()
                    }
                }
            }

        private fun getFileToByte(uri: Uri1): String? {

            //allows us to set decoding preferences when decoding a bitmap.
            val options = BitmapFactory.Options()

            options.inSampleSize =
                4 // Reduces the decoded image decoded will be halved twice (1/4 of the original).

            var bmp: Bitmap? = null
            var bos: ByteArrayOutputStream? = null
            var bt: ByteArray? = null
            var encodeString: String? = null
            try {

                // Decode the image file referenced by the URI into a Bitmap.
                // The third argument provides decoding options.
                // The result is a smaller version of the image to reduce memory usage.

                bmp = BitmapFactory.decodeStream(contentResolver.openInputStream(uri), null, options)
                //will capture the output of the compressed bitmap
                bos = ByteArrayOutputStream()
                // Compress the bitmap into JPEG format and write the bytes to the ByteArrayOutputStream.
                // The second argument is the quality setting, which is set to 50 (range is 0-100).
                // This reduces the quality to save space and speed up encoding.

                bmp!!.compress(Bitmap.CompressFormat.JPEG, 50, bos) // 50% quality compression

                // Convert the ByteArrayOutputStream to a byte array.
                bt = bos.toByteArray()
                // Convert the byte array to a Base64 encoded string.
                encodeString = Base64.encodeToString(bt, Base64.DEFAULT)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // Always close the ByteArrayOutputStream to free up resources.
                bos?.close()
            }
            return encodeString
        }

        //fun for create image file in private space
        private fun createImageUrl(): Uri1? {
            //find Uri (for it we use FileProvider) and then return it.
            val image = File(applicationContext.filesDir, "camera_photo.png")
            return FileProvider.getUriForFile(
                applicationContext,
                "com.company0ne.demoproject_2.fileProvider",
                image
            )
        }

        //for text file in storage
        private fun saveBase64AsTextFile(base64: String) {

            if (selectedFolderPath == null) {
                Toast.makeText(this, "Please select a folder first", Toast.LENGTH_SHORT).show()
                return
            }
//            // Check for storage permissions before accessing external storage
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                // Request permission
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),    REQUEST_STORAGE_PERMISSION
//
////                    101
//
//                )
//            } else {
            try {


/*                    // Define the path and file name
                    val path =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                            .absolutePath + "/base64AsText.txt"
                    val file = File(path)

                    // Use FileWriter and BufferedWriter to write the Base64 string to the file
                    // Open the file in append mode if it exists, otherwise create a new file
                    val fileWriter = FileWriter(file, true)
                    val bufferedWriter = BufferedWriter(fileWriter)

                    // Append a delimiter or identifier
                    bufferedWriter.append("---- New Data ----\n")
                    bufferedWriter.append(base64)
                    bufferedWriter.append("\n") // This is just to make sure each image content is in a new line.
                    bufferedWriter.close()


                    Toast.makeText(this, "Text file saved", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error saving text file", Toast.LENGTH_SHORT).show()
                }
            }
        }

 */

                val parentDocument = DocumentFile.fromTreeUri(this, selectedFolderPath!!)
                if (parentDocument == null || !parentDocument.exists() || !parentDocument.isDirectory) {
                    Toast.makeText(
                        this,
                        "The selected folder seems to be invalid.",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }
                // Check if 'base64AsText.txt' already exists in the directory
                var fileDocument: DocumentFile? = parentDocument.findFile("base64AsText.txt")

                // If the file does not exist, create it
                if (fileDocument == null) {
                    fileDocument = parentDocument.createFile("text/plain", "base64AsText.txt")
                }

                fileDocument?.uri?.let { uri ->
                    contentResolver.openOutputStream(uri, "wa")?.use { outputStream ->  // "wa" mode to append
                        BufferedWriter(outputStream.writer()).use { writer ->
                            writer.write("---- New Data ----\n")
                            writer.write(base64)
                            writer.write("\n")
                        }
                    }
                }

                Toast.makeText(this, "Text file saved in selected folder", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this,
                    "Error saving text file in selected folder: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

                    //        private fun shareTextFile() {
    //            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "base64AsText.txt")
    //            if (file.exists()) {
    //                val fileUri = FileProvider.getUriForFile(this, "com.company0ne.demoproject_2.fileProvider", file)
    //                val sharingIntent = Intent(Intent.ACTION_SEND).apply {
    //                    type = "text/*"
    //                    putExtra(Intent.EXTRA_STREAM, fileUri)
    //                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    //                }
    //                startActivity(Intent.createChooser(sharingIntent, "share file with"))
    //                Log.e("File", "File is sent")
    //            } else {
    //                Toast.makeText(this, "File not found!", Toast.LENGTH_SHORT).show()
    //            }
    //        }
    }
