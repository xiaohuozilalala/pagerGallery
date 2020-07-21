package com.example.pagerGallery

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.get
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.fragment_pager_photo.*
import kotlinx.android.synthetic.main.pager_photo_view.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val REQUEST_WRITE_EXTERNAL_STORAGE = 1
class PagerPhotoFragment : Fragment() {
    //提升viewModel至两个fragment共享（activity）
    val galleryViewModel by activityViewModels<GalleryViewModel> ()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pager_photo, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)

//        val photoList = arguments?.getParcelableArrayList<PhotoItem>("PHOTO_LIST")
        val adapter = PagerPhotoListAdapter()
        viewPager2.adapter = adapter
        galleryViewModel.pagedListLiveData.observe(viewLifecycleOwner , Observer {
            adapter.submitList(it)
            viewPager2.setCurrentItem(arguments?.getInt("PHOTO_POSITION")?:0,false)
        })

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
//                photoTag.text = "${position+1}/${photoList?.size}"  格式优化
                photoTag.text = getString(R.string.photo_tag,position+1,galleryViewModel.pagedListLiveData.value?.size)

            }
        })


        /**
         * 可通过
         * viewPager2.orientation = ViewPager2.ORIENTATION_VERTICAL
         * 设置为竖直滚动
         */


        /**
         * 29以下缺乏权限，进行检查，若无，则添加到数组（可多个权限）中请求
         */
        saveButton.setOnClickListener {
            if (Build.VERSION.SDK_INT<29 && ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED) {
                requestPermissions((arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)),
                    REQUEST_WRITE_EXTERNAL_STORAGE)
            }else {
                //跟随fragment生命周期,确定作用线程
                viewLifecycleOwner.lifecycleScope.launch {
                    savePhoto()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //跟随fragment生命周期
                    viewLifecycleOwner.lifecycleScope.launch {
                        savePhoto()
                    }
                } else {
                    Toast.makeText(requireContext(),"saved failed",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //suspend 允许挂起让其他线程执行
    private suspend fun savePhoto() {
        //允许挂起执行后完自动切回主线程。标明为耗时IO操作
        withContext(Dispatchers.IO){
            /**
             * 从viewPager2中反向一步步找viewholder
             */
            val holder =
                (viewPager2[0] as RecyclerView).findViewHolderForAdapterPosition(viewPager2.currentItem)
                        as PagerPhotoViewHolder

            //获取展示的图（与原图有出入），以位图方式保存至手机
            val bitmap = holder.itemView.pagerPhoto.drawable.toBitmap()

            /**
             *  API 29 新方式
             *  1.先占坑
             *  2.打开写入流,use(会自动关闭)
             */

            val saveUri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues()
            )?: kotlin.run {
                MainScope().launch { Toast.makeText(requireContext(),"saved failed",Toast.LENGTH_SHORT).show() }
                return@withContext
            }
            requireContext().contentResolver.openOutputStream(saveUri).use {
                if (bitmap.compress(Bitmap.CompressFormat.JPEG,90,it)) {
                    MainScope().launch { Toast.makeText(requireContext(),"saved sucessfully",Toast.LENGTH_SHORT).show() }
                } else {
                    MainScope().launch { Toast.makeText(requireContext(),"saved failed",Toast.LENGTH_SHORT).show() }
                }
            }
        }


    }
}