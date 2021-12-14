package com.llamasoft.envi.ui.simulator

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.llamasoft.envi.R
import com.llamasoft.envi.databinding.FragmentMenuBinding
import com.llamasoft.envi.util.BundleName
import com.llamasoft.envi.util.LoadImage
import com.llamasoft.envi.util.mimeTypes
import com.tektonlabs.americancolors.app.util.getTmpFileUri
import com.theartofdev.edmodo.cropper.CropImage

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnTakePicture.setOnClickListener {
            takeImage()
        }
        binding.btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageResult.launch(intent)
        }
        permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            readPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE]?: readPermissionGranted
            writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]?: writePermissionGranted
        }
        updateOrRequestPermissions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val pickImageResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.data != null && result.resultCode == Activity.RESULT_OK) {
            val uriImage = result.data!!.data
            launchCropImageResult(uriImage)
        }
    }

    private fun launchCropImageResult(uriImage: Uri?) = cropImageActivityResult
        .launch(
            CropImage.activity(uriImage)
            .setOutputCompressQuality(90)
            .setAllowRotation(false)
            .setAllowFlipping(false)
            .getIntent(requireContext())
            .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        )

    private val cropImageActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {

            val parsedResult = CropImage.getActivityResult(result.data)?.uri
            parsedResult?.let {
                view?.findNavController()?.navigate(
                    R.id.simulatorFragment,
                    bundleOf(
                        BundleName.UriImage.value to it.toString(),
                        BundleName.LoadImage.value to LoadImage.Local.name
                    )
                )
            }
        }
    }

    private val takeImageResult = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            latestTmpUri?.let { uri ->
                launchCropImageResult(uri)
            }
        }
    }

    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takeImageResult.launch(uri)
            }
        }
    }

    private var latestTmpUri: Uri? = null

    private fun updateOrRequestPermissions() {
        val hasReadPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()
        if(!writePermissionGranted) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(!readPermissionGranted) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(permissionsToRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}