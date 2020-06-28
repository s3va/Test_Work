package tk.kvakva.testwork.ui.main

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application

    // A "projection" defines the columns that will be returned for each row
    private val mProjection: Array<String> = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.Contacts.HAS_PHONE_NUMBER
    )

    // Defines a string to contain the selection clause
    private var selectionClause: String? = null

    // Declares an array to contain selection arguments
    private lateinit var selectionArgs: Array<String>


    fun addtotextstr(searchstrng: String?) {
        val c = mutableListOf<ImageFile>()
        val l = makefilelist()
 /*       l.forEach {
            c.add(ImageFile(it.fName, it.fPath, it.fUri.toString()))
        }*/
        _data.value = l
        return


        Log.d("uuuu", "============== $searchstrng")

        // Gets a word from the UI
        // Remember to insert code here to check for invalid or malicious input.

        // If the word is the empty string, gets everything
        selectionArgs = searchstrng?.takeIf { it.isNotEmpty() }?.let {
            selectionClause = "${ContactsContract.Contacts.CONTENT_URI} = ?"
            arrayOf(it)
        } ?: run {
            selectionClause = null
            emptyArray<String>()
        }

        Log.d("uuu", "+++++++ ${selectionArgs.toList()} $selectionClause")
        // Does a query against the table and returns a Cursor object
        val mCursor = app.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,  // The content URI of the words table
            null, null, null,
            /*  mProjection,                       // The columns to return for each row
              selectionClause,                  // Either null, or the word the user entered
              selectionArgs,                    // Either empty, or the string the user entered*/
            null                         // The sort order for the returned rows
        )

        Log.d(
            "uuuu",
            "+++++ cursor.columnNames = ${mCursor?.columnNames?.toList()} cursor.count = ${mCursor?.count}"
        )
// Some providers return null if an error occurs, others throw an exception
        when (mCursor?.count) {
            null -> {
                _textstr.value = "mCursor==null"
                Log.d("uuuu", "+++++N cursor.count = ${mCursor?.count}")
            }
            0 -> {
                _textstr.value = "mCursor==0"
                Log.d("uuuu", "+++++0 cursor.count = ${mCursor.count}")
            }
            else -> {
                Log.d("uuuu", "+++++-> cursor.count = ${mCursor.count}")

                mCursor.apply {
                    // Determine the column index of the column named "word"
                    val index = getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)

                    /*
                 * Moves to the next row in the cursor. Before the first movement in the cursor, the
                 * "row pointer" is -1, and if you try to retrieve data at that position you will get an
                 * exception.
                 */
                    while (moveToNext()) {
                        // Gets the value from the column.
                        val newWord = getString(index)
                        val phoneNumbers = mutableListOf<String>()
                        val emails = mutableListOf<String>()
                        Log.d("qwe", "----------- $newWord ------------")
                        // Insert code here to process the retrieved word.
                        if (getString(getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)).toInt() > 0) {
                            val mmCursor = app.contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? ",
                                arrayOf(mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts._ID))),
                                null
                            )
                            when (mmCursor?.count) {
                                null -> Log.d(
                                    "uuuu",
                                    "mmCursor.count = null (error to get phone number)"
                                )
                                0 -> Log.d("uuuu", "mmCursor.count = 0 (No Phone Number")
                                else -> {
                                    Log.d("uuuu", "mmCursor.count = ${mmCursor.count}")
                                    mmCursor.apply {
                                        while (moveToNext()) {
                                            phoneNumbers.add(
                                                getString(
                                                    getColumnIndex(
                                                        ContactsContract.CommonDataKinds.Phone.NUMBER
                                                    )
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }


                        val mmmCursor = app.contentResolver.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ? ",
                            arrayOf(mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts._ID))),
                            null
                        )
                        when (mmmCursor?.count) {
                            null -> Log.d("uuuu", "mmmCursor.count = null (error to get e-mail)")
                            0 -> Log.d("uuuu", "mmmCursor.count = 0 (No e-mails")
                            else -> {
                                Log.d("uuuu", "mmCursor.count = ${mmmCursor.count}")
                                mmmCursor.apply {
                                    while (moveToNext()) {
                                        emails.add(getString(getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)))
                                    }
                                }
                            }
                        }


                        _data.value = data.value.orEmpty()
                            .plus(ImageFile(newWord, "$phoneNumbers", "$emails"))

                        Log.d("uuuu", "DATA: ${data.value}")

                        // end of while loop
                    }
                }
            }
        }

        mCursor?.close()
        _textstr.value = textstr.value + "\n" + searchstrng + "\n---------------"

    }

    fun setdata(l: List<ImageFile>) {
        _data.value = l
    }

    private val _data = MutableLiveData<List<ImageFile>>()
    val data: LiveData<List<ImageFile>>
        get() = _data

    private val _textstr = MutableLiveData<String>("yyyyyyyyyyyy")
    val textstr: LiveData<String>
        get() = _textstr


    fun makefilelist(): List<ImageFile> {
        val filelist = arrayListOf<ImageFile>()
        getApplication<Application>().applicationContext.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,//projection,
            null,//selection,
            null,//selectionArgs,
            null//sortOrder
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                filelist.add(
                    ImageFile(
                        cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.VOLUME_NAME)) + " " +
                        cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)),
                        fUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
                        ),
                        fId = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
                    )
                )
                // Use an ID column from the projection to get
                // a URI representing the media item itself.
            }
            cursor.close()
        }
        return filelist
    }

    fun onRecyViewClicked(imageFile: ImageFile) {
        Toast.makeText(app.applicationContext, "$imageFile", Toast.LENGTH_SHORT).show()
        Log.v("OnRecyViewClicked","$imageFile")
        _navigateToShowImageFile.value=imageFile
    }

    private val _navigateToShowImageFile = MutableLiveData<ImageFile>()
    val navigateToShowImageFile: LiveData<ImageFile>
        get() = _navigateToShowImageFile

     fun doneNavidateToShowImageFile() {
        _navigateToShowImageFile.value = null
    }

}

