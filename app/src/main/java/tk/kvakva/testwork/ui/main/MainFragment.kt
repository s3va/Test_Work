package tk.kvakva.testwork.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import tk.kvakva.testwork.ImageFileFragment
import tk.kvakva.testwork.R
import tk.kvakva.testwork.databinding.MainFragmentBinding

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: MainFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding=DataBindingUtil.inflate(inflater,R.layout.main_fragment,container,false)
        binding.fvModel=viewModel
        binding.lifecycleOwner = this

        binding.buttonContact.setOnClickListener {
            viewModel.setdata(emptyList())
            viewModel.addtotextstr(binding.editTextTextPersonName.text.toString())
        }

        val recyViewAdap=RecyViewAdap(RecyViewListener {imageFile ->
            //Toast.makeText(context, "${nightId}", Toast.LENGTH_LONG).show()
            viewModel.onRecyViewClicked(imageFile)
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



}
