package tk.kvakva.testwork.ui.main

import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.database.DatabaseUtils
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.system.Os
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tk.kvakva.testwork.DumptxtFragment

private const val TAG = "MY_MainViewModel"

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
       // val l = makefilelist()
        /*       l.forEach {
                   c.add(ImageFile(it.fName, it.fPath, it.fUri.toString()))
               }*/
      //  _data.value = l
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

    private val _ur = MutableLiveData<Uri>()
    val ur: LiveData<Uri>
        get() = _ur

    fun sUr(u: Uri) {
        _ur.value = u
        val filelist = arrayListOf<ImageFile>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val docUriTree = DocumentsContract.buildDocumentUriUsingTree(
                u,
                DocumentsContract.getTreeDocumentId(u)
            )
            Log.d(
                "MY_MainViewModel",
                "++\n         u: $u\n++++++++++++++++++++++++\ndocUriTree: $docUriTree"
            )

            app.contentResolver.query(docUriTree, null, null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    Log.d(TAG, "sUr: +++++++++++++++++++++")
                    DatabaseUtils.dumpCurrentRow(cursor)
                    Log.d(TAG, "sUr: +++++++++++++++++++++")
                }
                cursor.close()
            }

            val fd = app.contentResolver.openFileDescriptor(docUriTree,"r")
            val st = Os.fstatvfs(fd?.fileDescriptor)
            Log.d(TAG, "sUr: Free ${st.f_bsize * st.f_bavail / 1024 / 1024} MByes  f_bsize: ${st.f_bsize} Bytes  f_bavail: ${st.f_bavail}")

/*            app.contentResolver.query(u,null,null,null,null)?.columnNames?.toString()

            app.applicationContext.contentResolver.query(
                u,null,null,null, null
            )?.use {cursor ->
                DumptxtFragment.dumptxtString.clear()
                while (cursor.moveToNext()){
                    DatabaseUtils.dumpCurrentRow(cursor)
                    DatabaseUtils.dumpCurrentRow(cursor, DumptxtFragment.dumptxtString)
                    DumptxtFragment.dumptxtString.append("\n-----------------------\n")
                    filelist.add(
                        ImageFile(
                            Name = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)),
                            fId = cursor.getInt(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)).toLong(),
                            fUri = DocumentsContract.buildDocumentUriUsingTree(u,cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)))
                    ))
                }
            }
            _data.value=filelist
            return*/
        } else {
            TODO("VERSION.SDK_INT < LOLLIPOP")
        }

        val df = DocumentFile.fromTreeUri(app.applicationContext, u)

        //if(df.isDirectory)
        DumptxtFragment.dumptxtString.clear()

        val dfl = df?.listFiles()
        var iidx = 0
        dfl?.forEach {
            iidx++
            filelist.add(
                ImageFile(
                    fUri = it.uri,
                    Name = it.name,
                    bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                        DocumentsContract.getDocumentThumbnail(
                            app.contentResolver, it.uri,
                            Point(190, 108), null
                        )
                    else
                        null
                )
            )

            app.contentResolver.query(it.uri, null, null, null, null)?.use { cursor ->
                /*DatabaseUtils.dumpCursor(cursor)
                DatabaseUtils.dumpCursor(cursor,DumptxtFragment.dumptxtString)
                DumptxtFragment.dumptxtString.append("+++++++++++++++++++++  $iidx  +++++++++++++++++\n")*/

                while (cursor.moveToNext()) {
                    DatabaseUtils.dumpCurrentRow(cursor)
                    DatabaseUtils.dumpCurrentRow(cursor, DumptxtFragment.dumptxtString)
                    DumptxtFragment.dumptxtString.append("------------------ $iidx -------------\n")
                }
            }

            //Log.d("MY_MainViewModel","${app.contentResolver.query(it.uri,null,null,null,null)?.columnNames?.toList()}")

            Log.d(
                "MY_MainViewModel",
                "\n-------------------\n$it\nname: ${it.name}\nparentFile: ${it.parentFile}\nuri: ${it.uri}\ntype: ${it.type}\nlength: ${it.length()}\nlastModified: ${it.lastModified()}\n-------------------------\n"
            )
        }
        _data.value = filelist
    }

    private val _progressVisable = MutableLiveData<Boolean>(false)
    val progressVisable: LiveData<Boolean>
        get() = _progressVisable

    fun progressOff() {
        _progressVisable.value=false
    }

    fun progressOn() {
        _progressVisable.value=true
    }

    private val _progressHorizont = MutableLiveData<Int>(0)
    val progressHorizont: LiveData<Int>
        get() = _progressHorizont

    private val _progressText = MutableLiveData<String>("10 from 100 (10%)")
    val progressText: LiveData<String>
        get() = _progressText

    fun makefilelist() {
        _progressVisable.value=true
        viewModelScope.launch(Dispatchers.IO) {

            val filelist = arrayListOf<ImageFile>()

            getApplication<Application>().applicationContext.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,//projection,
                null,//selection,
                null,//selectionArgs,
                null//sortOrder
            )?.use { cursor ->
                var i = 0
                DumptxtFragment.dumptxtString.clear()
                //DatabaseUtils.dumpCursor(cursor,DumptxtFragment.dumptxtString)
                while (cursor.moveToNext()) {
                    val p = cursor.position * 100 / cursor.count
                    _progressHorizont.postValue(p)
                    _progressText.postValue("${cursor.position} from ${cursor.count} ($p%)")

                    ++i
                    DatabaseUtils.dumpCurrentRow(cursor, DumptxtFragment.dumptxtString)
                    DumptxtFragment.dumptxtString.append("\n-----------------------\n")
                    Log.d(
                        "cursor",
                        "${cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID))} $cursor"
                    )
                    val picUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
                    )

                    filelist.add(
                        ImageFile(
                            cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)),
                            bitmap = /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                app.contentResolver.loadThumbnail(
                                    ContentUris.withAppendedId(
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
                                    ),
                                    Size(64, 24), null
                                )
                            } else*/ if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                DocumentsContract.getDocumentThumbnail(
                                    app.contentResolver,
                                    ContentUris.withAppendedId(
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
                                    ),
                                    Point(190, 108),
                                    null
                                )
                            } else {
                                null
                            },
                            //cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.VOLUME_NAME)) + " " +
                            //cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)),
//                        cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)),
                            fUri = ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
                            ),
                            fId = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))//,
                            //email = cursor.getString(cursor.getColumnIndex())
                        )
                    )
                    // Use an ID column from the projection to get
                    // a URI representing the media item itself.
                    //if (i > 100)
                    //    break
                }
                cursor.close()
            }
            _data.postValue(filelist)
        }
    }

    fun onRecyViewClicked(imageFile: ImageFile) {
        Toast.makeText(app.applicationContext, "$imageFile", Toast.LENGTH_SHORT).show()
        Log.v("OnRecyViewClicked", "$imageFile")
        _navigateToShowImageFile.value = imageFile
    }

    private val _navigateToShowImageFile = MutableLiveData<ImageFile?>()
    val navigateToShowImageFile: LiveData<ImageFile?>
        get() = _navigateToShowImageFile

    fun doneNavidateToShowImageFile() {
        _navigateToShowImageFile.value = null
    }
}
