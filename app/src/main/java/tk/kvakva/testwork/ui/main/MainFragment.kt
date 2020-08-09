package tk.kvakva.testwork.ui.main

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.main_fragment.*
import tk.kvakva.testwork.DumptxtFragment
import tk.kvakva.testwork.ImageFileFragment
import tk.kvakva.testwork.R
import tk.kvakva.testwork.databinding.MainFragmentBinding

private const val TAG = "MY_MainFragment"
class MainFragment : Fragment() {

    /**
     * Receive the result from a previous call to
     * [.startActivityForResult].  This follows the
     * related Activity API as described there in
     * [Activity.onActivityResult].
     *
     * @param requestCode The integer request code originally supplied to
     * startActivityForResult(), allowing you to identify who this
     * result came from.
     * @param resultCode The integer result code returned by the child activity
     * through its setResult().
     * @param data An Intent, which can return result data to the caller
     * (various data can be attached to Intent "extras").
     *
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            10->{
                if(resultCode==Activity.RESULT_OK){
                    data?.data?.also {
                        viewModel.sUr(it)
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: MainFragmentBinding


    val aDialog: AlertDialog? by lazy {
        activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
//                setPositiveButton("Ok",
//                    DialogInterface.OnClickListener { dialog, id ->
//                        Log.d("AlerDialog","Ok")
//                    })
                setNeutralButton("Ok"){ dialogInterface: DialogInterface, i: Int ->
                    Log.d("AlertDialog","dialogInterface: $dialogInterface, i: $i")
                }
                setNegativeButton("Close",
                    DialogInterface.OnClickListener { dialog, id ->
                        Log.d("AlerDialog","Cancel $id")
                    })
                //setMessage("$imageFile")
                //setTitle("Alert! Id: ${imageFile.fId}!")
            }
            // Set other dialog properties
            // Create the AlertDialog
            builder.create()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding=DataBindingUtil.inflate(inflater,R.layout.main_fragment,container,false)
        binding.fvModel=viewModel
        binding.lifecycleOwner = this



        viewModel.ur.observe(viewLifecycleOwner, Observer {
            Log.d("OBSERVER","---------------------- Uri! : $it")
        })

        //val registerActivity=registerForActivityResult(ActivityResultContracts.OpenDocumentTree()){
        //    viewModel.sUr(it)
        //}
        //registerActivity.launch()

        //registerActivity.apply {
        //    Intent.FLAG_GRANT_READ_URI_PERMISSION
        //    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        //    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        //    Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        //}

        binding.buttonContact.setOnClickListener {

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                // Provide read access to files and sub-directories in the user-selected
                // directory.
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                // Optionally, specify a URI for the directory that should be opened in
                // the system file picker when it loads.
//            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
            }

            startActivityForResult(intent,10)

            //viewModel.setdata(emptyList())
            //viewModel.addtotextstr(binding.editTextTextPersonName.text.toString())
        }

        binding.showDumpBt.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToDumptxtFragment())
        }

        val recyViewAdap=RecyViewAdap(RecyViewListener {view, imageFile ->
            //Toast.makeText(context, "${nightId}", Toast.LENGTH_LONG).show()
            when(view.id) {
                R.id.imageView2 ->
                    viewModel.onRecyViewClicked(imageFile)
                R.id.nameTV -> {

                    aDialog?.setTitle("Alert! Id: ${imageFile.fId}!")
                    aDialog?.setMessage("$imageFile".replace(", ","\n"))
                    aDialog?.show()

                }
            }
        })
        binding.recyclerView.adapter=recyViewAdap

        binding.recyclerView.layoutManager=LinearLayoutManager(context)

        viewModel.data.observe(viewLifecycleOwner, Observer{
            recyViewAdap.data=it
            Log.d("Fragment","observer data => $it")
        })

        // Add an Observer on the state variable for Navigating when and item is clicked.
        viewModel.navigateToShowImageFile.observe(viewLifecycleOwner, Observer { imageFile ->
            imageFile?.let {

                Log.v("MainFragment","$$$$$$$$$$$$$$$$$$$$$$$$$$ imageFile: $it $$$$$$$$$$$$$$$$$$$$$")
                val act = MainFragmentDirections.actionMainFragmentToImgFileFragment(it.fUri.toString())
                findNavController().navigate(act)
                viewModel.doneNavidateToShowImageFile()

            }
            Log.v("MainFragment","viewModel.navigateToShowImageFile.observe(viewLifecycleOwner, Observer { imageFile ->")
        })




        return binding.root
    }


    /**
     * Called when the view previously created by [.onCreateView] has
     * been detached from the fragment.  The next time the fragment needs
     * to be displayed, a new view will be created.  This is called
     * after [.onStop] and before [.onDestroy].  It is called
     * *regardless* of whether [.onCreateView] returned a
     * non-null view.  Internally it is called after the view's state has
     * been saved but before it has been removed from its parent.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("Fragment","OnDestoryView")
        aDialog?.dismiss()
    }
}
