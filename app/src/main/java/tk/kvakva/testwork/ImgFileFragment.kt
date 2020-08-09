package tk.kvakva.testwork

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.exifinterface.media.ExifInterface
import kotlinx.android.synthetic.main.fragment_img_file.view.*
import java.io.FileNotFoundException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "ifUri"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ImgFileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ImgFileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var ifUri: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            ifUri = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_img_file, container, false)
        // v.imgFileId.setImageURI(Uri.parse(arguments?.getString("ifUri")))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val inStream = v.context.contentResolver.openInputStream(Uri.parse(ifUri))
                if (inStream != null) {
                    val exif = ExifInterface(inStream)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    val matrix = Matrix()
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90F)
                        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180F)
                        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270F)
                    }
                    inStream.close()
                    val inStream = v.context.contentResolver.openInputStream(Uri.parse(ifUri))
                    val bm = BitmapFactory.decodeStream(inStream)
                    if (bm != null) {
                        val rotatedBitmap =
                            Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, matrix, true)
                        v.imgFileId.setImageBitmap(rotatedBitmap)
                    }
                    return v
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        } else {
            TODO("VERSION.SDK_INT < N")
        }
        v.imgFileId.setImageURI(Uri.parse(ifUri))
        return v
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ImgFileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ImgFileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}